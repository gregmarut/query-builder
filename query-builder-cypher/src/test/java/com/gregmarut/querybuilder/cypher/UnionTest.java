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

import com.gregmarut.querybuilder.cypher.condition.EqualsCondition;
import com.gregmarut.querybuilder.cypher.model.MovieNode;
import com.gregmarut.querybuilder.cypher.model.Person;
import com.gregmarut.querybuilder.cypher.model.PersonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class UnionTest
{
	@BeforeAll
	static void setUp()
	{
		QueryBuilderContext.defaultPrettyPrint = true;
	}

	@Test
	public void unionTwoBuildersMergesQueriesAndParams()
	{
		final var identifierGenerator1 = new IdentifierGenerator();
		final var alice = new PersonNode().named(identifierGenerator1);

		final var queryA = CypherBuilder.create()
			.match(alice)
			.where(new EqualsCondition(alice.getProperty(PersonNode.NAME), "Alice"))
			.addReturn(alice);

		final var identifierGenerator2 = new IdentifierGenerator();
		final var bob = new PersonNode().named(identifierGenerator2);

		final var queryB = CypherBuilder.create()
			.match(bob)
			.where(new EqualsCondition(bob.getProperty(PersonNode.NAME), "Bob"))
			.addReturn(bob);

		final var union = Union.union(Person.class, queryA, queryB);

		Assertions.assertEquals("""
			MATCH (p:Person)
			WHERE p.name = $_v0
			RETURN p
			UNION
			MATCH (p:Person)
			WHERE p.name = $_v1
			RETURN p""", union.getQuery());

		//both queries share the same variable counter so we expect _v0 and _v1
		Assertions.assertEquals("Alice", union.getParams().get("_v0"));
		Assertions.assertEquals("Bob", union.getParams().get("_v1"));
	}

	@Test
	public void unionAcrossDifferentNodeTypes()
	{
		final var identifierGenerator1 = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator1);
		final var personQuery = CypherBuilder.create().match(personNode).addReturn(personNode);

		final var identifierGenerator2 = new IdentifierGenerator();
		final var movieNode = new MovieNode().named(identifierGenerator2);
		final var movieQuery = CypherBuilder.create().match(movieNode).addReturn(movieNode);

		final var union = Union.union(Object.class, personQuery, movieQuery);

		Assertions.assertEquals("""
			MATCH (p:Person)
			RETURN p
			UNION
			MATCH (m:Movie)
			RETURN m""", union.getQuery());
	}
}
