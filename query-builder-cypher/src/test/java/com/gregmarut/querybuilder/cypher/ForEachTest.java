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

import com.gregmarut.querybuilder.cypher.condition.NotEqualsCondition;
import com.gregmarut.querybuilder.cypher.function.Collect;
import com.gregmarut.querybuilder.cypher.model.MovieNode;
import com.gregmarut.querybuilder.cypher.model.PersonNode;
import com.gregmarut.querybuilder.cypher.model.Relationships;
import com.gregmarut.querybuilder.cypher.phrase.Delete;
import com.gregmarut.querybuilder.cypher.phrase.ForEach;
import com.gregmarut.querybuilder.cypher.phrase.Match;
import com.gregmarut.querybuilder.cypher.phrase.With;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ForEachTest
{
	@BeforeAll
	static void setUp()
	{
		QueryBuilderContext.defaultPrettyPrint = true;
	}

	/**
	 * Verifies the OPTIONAL MATCH + COLLECT + FOREACH pattern used to replace a singular
	 * relationship. The query finds any stale DIRECTED relationship on the movie that points to a
	 * different person, collects it to avoid a DELETE null error, then deletes it and merges the
	 * correct one.
	 */
	@Test
	public void replaceSingularRelationship()
	{
		final var generator = new IdentifierGenerator();

		final var movieNode = new MovieNode().named(generator);
		final var personNode = new PersonNode().named(generator);

		//stale node matches any existing director — no id filter, just the label
		final var stalePersonNode = new PersonNode().named(generator);
		final var directedRel = Relationship.of(Relationships.DIRECTED, generator);

		final var stalePath = Path.start(stalePersonNode).out(directedRel).to(movieNode).build();

		//WHERE stale <> intended person — skip when the existing director is already correct
		final var whereNotSame = new NotEqualsCondition(
			LiteralCypherString.of(stalePersonNode.getRequiredIdentifier()),
			LiteralCypherString.of(personNode.getRequiredIdentifier())
		);

		//collect(rel) so FOREACH is a no-op (iterates empty list) when nothing matched
		final var staleRelsAlias = generator.next();
		final var collectedStaleRels = Collect.of(LiteralCypherString.of(directedRel.getRequiredIdentifier())).as(staleRelsAlias);

		//loop variable — same Identifiable passed to both ForEach (header) and Delete (body)
		final var loopVarName = generator.next();
		final Identifiable loopVar = () -> loopVarName;

		final var query = CypherBuilder.create()
			.match(movieNode)
			.match(personNode)
			.match(Match.optional(stalePath).where(whereNotSame))
			.with(new With().add(movieNode).add(personNode).add(collectedStaleRels))
			.forEach(new ForEach(loopVar, LiteralCypherString.of(staleRelsAlias), Delete.delete(loopVar)))
			.merge(new Merge(Path.start(personNode).out(new Relationship(Relationships.DIRECTED)).to(movieNode).build()))
			.build();

		Assertions.assertEquals("""
			MATCH (m:Movie)
			MATCH (p:Person)
			OPTIONAL MATCH (pe:Person)-[d:DIRECTED]->(m) WHERE pe <> p
			WITH m, p, COLLECT(d) AS _i_1
			FOREACH (_i_2 IN _i_1 | DELETE _i_2)
			MERGE (p)-[:DIRECTED]->(m)""", query.getQuery());

		Assertions.assertTrue(query.getParams().isEmpty());
	}
}
