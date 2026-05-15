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

import com.gregmarut.querybuilder.cypher.condition.GreaterThanCondition;
import com.gregmarut.querybuilder.cypher.function.Coalesce;
import com.gregmarut.querybuilder.cypher.function.Collect;
import com.gregmarut.querybuilder.cypher.function.Count;
import com.gregmarut.querybuilder.cypher.function.Date;
import com.gregmarut.querybuilder.cypher.function.DateTime;
import com.gregmarut.querybuilder.cypher.function.Duration;
import com.gregmarut.querybuilder.cypher.function.Exists;
import com.gregmarut.querybuilder.cypher.function.Max;
import com.gregmarut.querybuilder.cypher.function.Range;
import com.gregmarut.querybuilder.cypher.function.Size;
import com.gregmarut.querybuilder.cypher.function.Sum;
import com.gregmarut.querybuilder.cypher.function.ToFloat;
import com.gregmarut.querybuilder.cypher.function.ToLower;
import com.gregmarut.querybuilder.cypher.function.ToUpper;
import com.gregmarut.querybuilder.cypher.model.MovieNode;
import com.gregmarut.querybuilder.cypher.model.PersonNode;
import com.gregmarut.querybuilder.cypher.model.Relationships;
import com.gregmarut.querybuilder.cypher.phrase.With;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

public class FunctionTest
{
	@BeforeAll
	static void setUp()
	{
		QueryBuilderContext.defaultPrettyPrint = true;
	}

	@Test
	public void countDistinct()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);
		final var movieNode = new MovieNode().named(identifierGenerator);

		final var distinctMovies = Count.distinct(movieNode).as("uniqueMovies");

		final var query = CypherBuilder.create()
			.match(Path.start(personNode).out(Relationships.ACTED_IN).to(movieNode).build())
			.with(new With().add(personNode).add(distinctMovies))
			.addReturn(personNode, distinctMovies)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)-[:ACTED_IN]->(m:Movie)
			WITH p, COUNT(DISTINCT m) AS uniqueMovies
			RETURN p, uniqueMovies""", query.getQuery());
		Assertions.assertTrue(query.getParams().isEmpty());
	}

	@Test
	public void sumFunction()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);
		final var movieNode = new MovieNode().named(identifierGenerator);

		final var totalReleased = Sum.of(movieNode.getProperty(MovieNode.RELEASED)).as("totalReleased");

		final var query = CypherBuilder.create()
			.match(Path.start(personNode).out(Relationships.ACTED_IN).to(movieNode).build())
			.with(new With().add(personNode).add(totalReleased))
			.addReturn(personNode, totalReleased)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)-[:ACTED_IN]->(m:Movie)
			WITH p, sum(m.released) AS totalReleased
			RETURN p, totalReleased""", query.getQuery());
		Assertions.assertTrue(query.getParams().isEmpty());
	}

	@Test
	public void maxFunction()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var movieNode = new MovieNode().named(identifierGenerator);

		final var latestRelease = Max.of(movieNode.getProperty(MovieNode.RELEASED)).as("latestRelease");

		final var query = CypherBuilder.create()
			.match(movieNode)
			.addReturn(latestRelease)
			.build();

		Assertions.assertEquals("""
			MATCH (m:Movie)
			RETURN max(m.released) AS latestRelease""", query.getQuery());
		Assertions.assertTrue(query.getParams().isEmpty());
	}

	@Test
	public void coalesceWithDefault()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var movieNode = new MovieNode().named(identifierGenerator);

		final var titleOrFallback = Coalesce.of(movieNode.getProperty(MovieNode.TITLE), Variable.of("Untitled")).as("displayTitle");

		final var query = CypherBuilder.create()
			.match(movieNode)
			.addReturn(titleOrFallback)
			.build();

		Assertions.assertEquals("""
			MATCH (m:Movie)
			RETURN coalesce(m.title, $_v0) AS displayTitle""", query.getQuery());
		Assertions.assertEquals("Untitled", query.getParams().get("_v0"));
	}

	@Test
	public void collectFunction()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);
		final var movieNode = new MovieNode().named(identifierGenerator);

		final var collectedMovies = Collect.of(movieNode).as("movies");

		final var query = CypherBuilder.create()
			.match(Path.start(personNode).out(Relationships.ACTED_IN).to(movieNode).build())
			.with(new With().add(personNode).add(collectedMovies))
			.addReturn(personNode, collectedMovies)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)-[:ACTED_IN]->(m:Movie)
			WITH p, COLLECT(m) AS movies
			RETURN p, movies""", query.getQuery());
		Assertions.assertTrue(query.getParams().isEmpty());
	}

	@Test
	public void collectDistinct()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);
		final var movieNode = new MovieNode().named(identifierGenerator);

		final var collectedTitles = Collect.distinct(movieNode.getProperty(MovieNode.TITLE)).as("titles");

		final var query = CypherBuilder.create()
			.match(Path.start(personNode).out(Relationships.ACTED_IN).to(movieNode).build())
			.with(new With().add(personNode).add(collectedTitles))
			.addReturn(personNode, collectedTitles)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)-[:ACTED_IN]->(m:Movie)
			WITH p, COLLECT(DISTINCT m.title) AS titles
			RETURN p, titles""", query.getQuery());
		Assertions.assertTrue(query.getParams().isEmpty());
	}

	@Test
	public void sizeFunction()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);
		final var movieNode = new MovieNode().named(identifierGenerator);

		final var collectedMovies = Collect.of(movieNode).as("movies");

		final var query = CypherBuilder.create()
			.match(Path.start(personNode).out(Relationships.ACTED_IN).to(movieNode).build())
			.with(new With().add(personNode).add(collectedMovies))
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

	@Test
	public void existsFunction()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);
		final var movieNode = new MovieNode().named(identifierGenerator);

		//build a path used by exists() and assign to its own variable to ensure no double-build error
		final var query = CypherBuilder.create()
			.match(personNode)
			.match(movieNode)
			.addReturn(Exists.of(Path.start(personNode).out(Relationships.ACTED_IN).to(movieNode).build()).as("actedIn"))
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)
			MATCH (m:Movie)
			RETURN EXISTS((p)-[:ACTED_IN]->(m)) AS actedIn""", query.getQuery());
		Assertions.assertTrue(query.getParams().isEmpty());
	}

	@Test
	public void rangeFunction()
	{
		final var rangeExpression = Range.of(Variable.of(1), Variable.of(10)).as("nums");

		final var query = CypherBuilder.create()
			.addReturn(rangeExpression)
			.build();

		Assertions.assertEquals("RETURN range($_v0, $_v1) AS nums", query.getQuery());
		Assertions.assertEquals(1, query.getParams().get("_v0"));
		Assertions.assertEquals(10, query.getParams().get("_v1"));
	}

	@Test
	public void toUpperAndToLowerFunctions()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);

		final var upper = ToUpper.of(personNode.getProperty(PersonNode.NAME)).as("upper");
		final var lower = ToLower.of(personNode.getProperty(PersonNode.NAME)).as("lower");

		final var query = CypherBuilder.create()
			.match(personNode)
			.addReturn(upper, lower)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)
			RETURN toupper(p.name) AS upper, tolower(p.name) AS lower""", query.getQuery());
		Assertions.assertTrue(query.getParams().isEmpty());
	}

	@Test
	public void toFloatFunction()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var movieNode = new MovieNode().named(identifierGenerator);

		final var releasedAsFloat = ToFloat.of(movieNode.getProperty(MovieNode.RELEASED)).as("releasedFloat");

		final var query = CypherBuilder.create()
			.match(movieNode)
			.addReturn(releasedAsFloat)
			.build();

		Assertions.assertEquals("""
			MATCH (m:Movie)
			RETURN tofloat(m.released) AS releasedFloat""", query.getQuery());
		Assertions.assertTrue(query.getParams().isEmpty());
	}

	@Test
	public void dateNoArgFunction()
	{
		//date() does not extend ReturnableCypherString, so build via context to verify rendering
		final var context = QueryBuilderContext.createDefault();
		final var date = new Date();

		Assertions.assertEquals("date()", date.build(context));
		Assertions.assertEquals(0, date.getParameterStream(context).count());
	}

	@Test
	public void dateFromLocalDate()
	{
		final var context = QueryBuilderContext.createDefault();
		final var date = Date.from(LocalDate.of(2025, 1, 15));

		Assertions.assertEquals("date($_v0)", date.build(context));
		final var params = date.getParameterStream(context).toList();
		Assertions.assertEquals(1, params.size());
		Assertions.assertEquals(LocalDate.of(2025, 1, 15), params.get(0).getValue());
	}

	@Test
	public void dateTimeNoArgFunction()
	{
		final var context = QueryBuilderContext.createDefault();
		final var dateTime = new DateTime();

		Assertions.assertEquals("datetime()", dateTime.build(context));
		Assertions.assertEquals(0, dateTime.getParameterStream(context).count());
	}

	@Test
	public void dateTimeFromInstant()
	{
		final var context = QueryBuilderContext.createDefault();
		final var instant = Instant.parse("2025-05-15T12:00:00Z");
		final var dateTime = DateTime.from(instant);

		Assertions.assertEquals("datetime($_v0)", dateTime.build(context));
		final var params = dateTime.getParameterStream(context).toList();
		Assertions.assertEquals(1, params.size());
		Assertions.assertEquals(instant, params.get(0).getValue());
	}

	@Test
	public void durationFunction()
	{
		//use a stable ordered map to assert a consistent rendering order
		final var duration = Duration.of(new java.util.LinkedHashMap<Duration.Unit, CypherString>()
		{{
			put(Duration.Unit.DAYS, Variable.of(7));
			put(Duration.Unit.HOURS, Variable.of(12));
		}});

		final var query = CypherBuilder.create()
			.addReturn(duration.as("d"))
			.build();

		Assertions.assertEquals("RETURN duration({days: $_v0, hours: $_v1}) AS d", query.getQuery());
		Assertions.assertEquals(7, query.getParams().get("_v0"));
		Assertions.assertEquals(12, query.getParams().get("_v1"));
	}

	@Test
	public void countOfFactory()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var movieNode = new MovieNode().named(identifierGenerator);

		final var query = CypherBuilder.create()
			.match(movieNode)
			.addReturn(Count.of(movieNode).as("total"))
			.build();

		Assertions.assertEquals("""
			MATCH (m:Movie)
			RETURN COUNT(m) AS total""", query.getQuery());
		Assertions.assertTrue(query.getParams().isEmpty());
	}

	@Test
	public void singleUnitDurationFunction()
	{
		final var duration = Duration.of(Map.of(Duration.Unit.MONTHS, Variable.of(3)));

		final var query = CypherBuilder.create()
			.addReturn(duration.as("d"))
			.build();

		Assertions.assertEquals("RETURN duration({months: $_v0}) AS d", query.getQuery());
		Assertions.assertEquals(3, query.getParams().get("_v0"));
	}
}
