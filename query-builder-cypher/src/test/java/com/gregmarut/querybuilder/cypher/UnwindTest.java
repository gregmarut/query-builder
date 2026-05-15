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

import com.gregmarut.querybuilder.cypher.function.Collect;
import com.gregmarut.querybuilder.cypher.model.MovieNode;
import com.gregmarut.querybuilder.cypher.model.PersonNode;
import com.gregmarut.querybuilder.cypher.model.Relationships;
import com.gregmarut.querybuilder.cypher.node.Node;
import com.gregmarut.querybuilder.cypher.phrase.Unwind;
import com.gregmarut.querybuilder.cypher.phrase.With;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

public class UnwindTest
{
	@BeforeAll
	static void setUp()
	{
		QueryBuilderContext.defaultPrettyPrint = true;
	}

	@Test
	public void unwindSingleVariable()
	{
		final var ids = Variable.of(List.of("a", "b", "c"));
		final var query = CypherBuilder.create()
			.unwind(new Unwind<>(ids, "id"))
			.addReturn("id")
			.build();

		Assertions.assertEquals("""
			UNWIND $_v0 AS id
			RETURN id""", query.getQuery());
		Assertions.assertEquals(List.of("a", "b", "c"), query.getParams().get("_v0"));
	}

	@Test
	public void unwindAsNodeAndUseDownstream()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var movieNode = new MovieNode().named(identifierGenerator);

		final var unwind = new Unwind<>(Variable.of(List.of("m1", "m2")), "id");
		final var movieFromUnwind = unwind.<MovieNode>getAsNode(MovieNode.class);

		//unwind a list of ids, match the movie, return it
		final var query = CypherBuilder.create()
			.unwind(unwind)
			.match(movieNode)
			.where(new com.gregmarut.querybuilder.cypher.condition.EqualsCondition(
				movieNode.getProperty(MovieNode.ID),
				LiteralCypherString.of(movieFromUnwind.getRequiredIdentifier())
			))
			.addReturn(movieNode)
			.build();

		Assertions.assertEquals("""
			UNWIND $_v0 AS id
			MATCH (m:Movie)
			WHERE m.id = id
			RETURN m""", query.getQuery());
		Assertions.assertEquals(List.of("m1", "m2"), query.getParams().get("_v0"));
	}

	/**
	 * Regression for the hidden build-order coupling in {@link Unwind#optional}.
	 *
	 * Previously, both the size() check and the THEN branch called build() on the AliasedCypherString,
	 * which would emit the full "COLLECT(...) AS movies" inside size() when the alias had not been built
	 * by an upstream WITH first — producing invalid cypher. After the fix, Unwind.optional always
	 * references the alias by name only, regardless of build order.
	 */
	@Test
	public void unwindOptionalReferencesAliasNameRegardlessOfBuildOrder()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var movieNode = new MovieNode().named(identifierGenerator);

		final Collect<Node> collected = Collect.<Node>of(movieNode);
		final var aliased = collected.as("movies");

		//deliberately no upstream WITH that builds the aliased collection
		final var query = CypherBuilder.create()
			.unwind(Unwind.optional(aliased, "movie"))
			.addReturn("movie")
			.build();

		//the rendered query should not embed the full collect expression — it must reference the alias name only
		Assertions.assertFalse(query.getQuery().contains("AS movies"),
			"expected only the alias name, not the embedded expression, in: " + query.getQuery());
		Assertions.assertTrue(query.getQuery().contains("size(movies)"),
			"expected size(movies) reference, got: " + query.getQuery());
	}

	@Test
	public void unwindOptionalForEmptyOrNonEmptyCollection()
	{
		//build a Collect aliased to "movies" - used by Unwind.optional
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);
		final var movieNode = new MovieNode().named(identifierGenerator);

		final Collect<Node> collected = Collect.<Node>of(movieNode);
		final var aliased = collected.as("movies");

		final var query = CypherBuilder.create()
			.match(Path.start(personNode).out(Relationships.ACTED_IN).to(movieNode).build())
			.with(new With().add(personNode).add(aliased))
			.unwind(Unwind.optional(aliased, "movie"))
			.addReturn(personNode)
			.addReturn("movie")
			.build();

		//note: Case wraps the rendering across lines using the configured statement separator
		Assertions.assertEquals("""
			MATCH (p:Person)-[:ACTED_IN]->(m:Movie)
			WITH p, COLLECT(m) AS movies
			UNWIND CASE
				WHEN size(movies) > 0 THEN movies
				ELSE [null]
			END AS movie
			RETURN p, movie""", query.getQuery());
		Assertions.assertTrue(query.getParams().isEmpty());
	}
}
