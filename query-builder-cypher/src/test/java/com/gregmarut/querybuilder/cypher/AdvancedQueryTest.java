/*
 * Copyright 2026 Greg Marut
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.gregmarut.querybuilder.cypher;

import com.gregmarut.querybuilder.cypher.condition.AndCondition;
import com.gregmarut.querybuilder.cypher.condition.EqualsCondition;
import com.gregmarut.querybuilder.cypher.condition.GreaterThanCondition;
import com.gregmarut.querybuilder.cypher.condition.InCondition;
import com.gregmarut.querybuilder.cypher.function.Collect;
import com.gregmarut.querybuilder.cypher.function.Count;
import com.gregmarut.querybuilder.cypher.function.Max;
import com.gregmarut.querybuilder.cypher.function.Size;
import com.gregmarut.querybuilder.cypher.function.ToLower;
import com.gregmarut.querybuilder.cypher.model.GenreNode;
import com.gregmarut.querybuilder.cypher.model.MovieNode;
import com.gregmarut.querybuilder.cypher.model.PersonNode;
import com.gregmarut.querybuilder.cypher.model.Relationships;
import com.gregmarut.querybuilder.cypher.phrase.Delete;
import com.gregmarut.querybuilder.cypher.phrase.Match;
import com.gregmarut.querybuilder.cypher.phrase.Where;
import com.gregmarut.querybuilder.cypher.phrase.With;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

public class AdvancedQueryTest
{
	@BeforeAll
	static void setUp()
	{
		QueryBuilderContext.defaultPrettyPrint = true;
	}

	@Test
	public void aggregateMoviesByPersonWithFilter()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);
		final var movieNode = new MovieNode().named(identifierGenerator);

		final var movieCount = Count.of(movieNode).as("movieCount");

		final var query = CypherBuilder.create()
			.match(Path.start(personNode).out(Relationships.ACTED_IN).to(movieNode).build())
			.where(new GreaterThanCondition(movieNode.getProperty(MovieNode.RELEASED), 2000))
			.with(new With().add(personNode).add(movieCount))
			.where(new GreaterThanCondition(LiteralCypherString.of("movieCount"), 3))
			.addReturn(personNode, movieCount)
			.orderBy("movieCount", OrderType.DESC)
			.limit(10)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)-[:ACTED_IN]->(m:Movie)
			WHERE m.released > 2000
			WITH p, COUNT(m) AS movieCount
			WHERE movieCount > 3
			RETURN p, movieCount
			ORDER BY movieCount DESC
			LIMIT 10""", query.getQuery());
		Assertions.assertTrue(query.getParams().isEmpty());
	}

	@Test
	public void matchMultipleRelationshipsCollectedIntoWith()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);
		final var actedIn = new MovieNode().named(identifierGenerator);
		final var directed = new MovieNode().named(identifierGenerator);

		final var actedInCollected = Collect.of(actedIn).as("actedIn");
		final var directedCollected = Collect.of(directed).as("directed");

		final var query = CypherBuilder.create()
			.match(personNode)
			.match(Match.optional(Path.start(personNode).out(Relationships.ACTED_IN).to(actedIn).build()))
			.match(Match.optional(Path.start(personNode).out(Relationships.DIRECTED).to(directed).build()))
			.with(new With().add(personNode).add(actedInCollected).add(directedCollected))
			.addReturn(personNode, actedInCollected, directedCollected)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)
			OPTIONAL MATCH (p)-[:ACTED_IN]->(m:Movie)
			OPTIONAL MATCH (p)-[:DIRECTED]->(mo:Movie)
			WITH p, COLLECT(m) AS actedIn, COLLECT(mo) AS directed
			RETURN p, actedIn, directed""", query.getQuery());
		Assertions.assertTrue(query.getParams().isEmpty());
	}

	@Test
	public void detachDeleteRelationshipsAndNodes()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().withId("abc123").named(identifierGenerator);
		final var movieNode = new MovieNode().named(identifierGenerator);

		final var query = CypherBuilder.create()
			.match(personNode)
			.match(Path.start(personNode).out(Relationships.ACTED_IN).to(movieNode).build())
			.delete(Delete.detachDelete(personNode))
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person{id: $_v0_id})
			MATCH (p)-[:ACTED_IN]->(m:Movie)
			DETACH DELETE p""", query.getQuery());
		Assertions.assertEquals("abc123", query.getParams().get("_v0_id"));
	}

	@Test
	public void prettyPrintDisabledRendersOnSingleLine()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);

		final var context = QueryBuilderContext.createDefault().setPrettyPrint(false);

		final var query = CypherBuilder.create()
			.match(personNode)
			.where(new EqualsCondition(personNode.getProperty(PersonNode.NAME), "Alice"))
			.addReturn(personNode)
			.build(context);

		Assertions.assertEquals("MATCH (p:Person) WHERE p.name = $_v0 RETURN p", query.getQuery());
		Assertions.assertEquals("Alice", query.getParams().get("_v0"));
	}

@Test
	public void buildTypedQueryReturnsTypedResult()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);

		final var typedQuery = CypherBuilder.create()
			.match(personNode)
			.addReturn(personNode)
			.build(PersonNode.class);

		Assertions.assertEquals(PersonNode.class, typedQuery.getType());
		Assertions.assertEquals("""
			MATCH (p:Person)
			RETURN p""", typedQuery.getQuery());
	}

	@Test
	public void whereOptionalAppendsClauseWhenPresent()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);

		//WhereBuilder.build() returns Optional<Where>; CypherBuilder.where(Optional) should accept it inline
		final var whereOpt = Where.builder()
			.add(new EqualsCondition(personNode.getProperty(PersonNode.NAME), "Alice"))
			.build();

		final var query = CypherBuilder.create()
			.match(personNode)
			.where(whereOpt)
			.addReturn(personNode)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)
			WHERE p.name = $_v0
			RETURN p""", query.getQuery());
		Assertions.assertEquals("Alice", query.getParams().get("_v0"));
	}

	@Test
	public void whereOptionalIsNoOpWhenEmpty()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);

		//empty WhereBuilder produces Optional.empty(); CypherBuilder.where should silently skip it
		final var emptyWhere = Where.builder().build();

		final var query = CypherBuilder.create()
			.match(personNode)
			.where(emptyWhere)
			.addReturn(personNode)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)
			RETURN p""", query.getQuery());
		Assertions.assertTrue(query.getParams().isEmpty());
	}

	@Test
	public void inConditionWithStringValuesViaArray()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var movieNode = new MovieNode().named(identifierGenerator);

		final var query = CypherBuilder.create()
			.match(movieNode)
			.where(new InCondition(movieNode.getProperty(MovieNode.TITLE), Array.ofValues(List.of("Matrix", "Inception", "Dune"))))
			.addReturn(movieNode)
			.build();

		Assertions.assertEquals("""
			MATCH (m:Movie)
			WHERE m.title IN [$_v0, $_v1, $_v2]
			RETURN m""", query.getQuery());
		Assertions.assertEquals("Matrix", query.getParams().get("_v0"));
		Assertions.assertEquals("Inception", query.getParams().get("_v1"));
		Assertions.assertEquals("Dune", query.getParams().get("_v2"));
	}

	@Test
	public void whereCondensesUsingHelperFactory()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);

		final var query = CypherBuilder.create()
			.match(personNode)
			.where(Where.from(new AndCondition(
				new EqualsCondition(personNode.getProperty(PersonNode.NAME), "Alice"),
				new EqualsCondition(personNode.getProperty(PersonNode.BORN), 1980)
			)))
			.addReturn(personNode)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)
			WHERE (p.name = $_v0 AND p.born = 1980)
			RETURN p""", query.getQuery());
		Assertions.assertEquals("Alice", query.getParams().get("_v0"));
	}

	@Test
	public void maxAndSizeAcrossGenresWithGrouping()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var genreNode = new GenreNode().named(identifierGenerator);
		final var movieNode = new MovieNode().named(identifierGenerator);

		final var latestRelease = Max.of(movieNode.getProperty(MovieNode.RELEASED)).as("latest");
		final var totalMovies = Count.of(movieNode).as("total");

		final var query = CypherBuilder.create()
			.match(Path.start(movieNode).out(Relationships.IN_GENRE).to(genreNode).build())
			.with(new With().add(genreNode).add(latestRelease).add(totalMovies))
			.addReturn(genreNode, latestRelease, totalMovies)
			.orderBy("latest", OrderType.DESC)
			.build();

		Assertions.assertEquals("""
			MATCH (m:Movie)-[:IN_GENRE]->(g:Genre)
			WITH g, max(m.released) AS latest, COUNT(m) AS total
			RETURN g, latest, total
			ORDER BY latest DESC""", query.getQuery());
		Assertions.assertTrue(query.getParams().isEmpty());
	}

	@Test
	public void caseInsensitiveSearchWithToLower()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);

		final var lowered = ToLower.of(personNode.getProperty(PersonNode.NAME));

		final var query = CypherBuilder.create()
			.match(personNode)
			.where(new EqualsCondition(lowered, Variable.of("alice")))
			.addReturn(personNode)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)
			WHERE tolower(p.name) = $_v0
			RETURN p""", query.getQuery());
		Assertions.assertEquals("alice", query.getParams().get("_v0"));
	}

	@Test
	public void existsCollectionViaSize()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);
		final var movieNode = new MovieNode().named(identifierGenerator);

		final var collected = Collect.of(movieNode).as("movies");

		final var query = CypherBuilder.create()
			.match(Path.start(personNode).out(Relationships.ACTED_IN).to(movieNode).build())
			.with(new With().add(personNode).add(collected))
			.where(new GreaterThanCondition(Size.of(LiteralCypherString.of("movies")), 0))
			.addReturn(personNode)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)-[:ACTED_IN]->(m:Movie)
			WITH p, COLLECT(m) AS movies
			WHERE size(movies) > 0
			RETURN p""", query.getQuery());
		Assertions.assertTrue(query.getParams().isEmpty());
	}
}
