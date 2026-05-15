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
import com.gregmarut.querybuilder.cypher.model.MovieNode;
import com.gregmarut.querybuilder.cypher.model.PersonNode;
import com.gregmarut.querybuilder.cypher.model.Relationships;
import com.gregmarut.querybuilder.cypher.phrase.Match;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class OptionalMatchTest
{
	@BeforeAll
	static void setUp()
	{
		QueryBuilderContext.defaultPrettyPrint = true;
	}
	
	@Test
	public void optionalMatch()
	{
		final var identifierGenerator = new IdentifierGenerator();
		
		final var personNode = new PersonNode()
			.withId("abc123")
			.named(identifierGenerator);
		
		final var movieNode = new MovieNode()
			.named(identifierGenerator);
		
		final var query = CypherBuilder.create()
			.match(personNode)
			.match(Match.optional(Path.start(personNode).out(Relationships.DIRECTED).to(movieNode).build()))
			.addReturn(personNode, movieNode)
			.build();
		
		Assertions.assertEquals("""
			MATCH (p:Person{id: $_v0_id})
			OPTIONAL MATCH (p)-[:DIRECTED]->(m:Movie)
			RETURN p, m""", query.getQuery());
		
		Assertions.assertEquals("abc123", query.getParams().get("_v0_id"));
	}
	
	@Test
	public void optionalMatchWithWhere()
	{
		final var identifierGenerator = new IdentifierGenerator();
		
		final var personNode = new PersonNode()
			.named(identifierGenerator);
		
		final var movieNode = new MovieNode()
			.named(identifierGenerator);
		
		final var query = CypherBuilder.create()
			.match(personNode)
			.match(Match.optional(Path.start(personNode).out(Relationships.ACTED_IN).to(movieNode).build()))
			.where(new GreaterThanCondition(movieNode.getProperty(MovieNode.RELEASED), 2000))
			.addReturn(movieNode)
			.build();
		
		Assertions.assertEquals("""
			MATCH (p:Person)
			OPTIONAL MATCH (p)-[:ACTED_IN]->(m:Movie)
			WHERE m.released > 2000
			RETURN m""", query.getQuery());
		
		Assertions.assertTrue(query.getParams().isEmpty());
	}
}
