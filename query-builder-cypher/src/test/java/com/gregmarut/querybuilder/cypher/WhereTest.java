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

import com.gregmarut.querybuilder.cypher.condition.AnyInCondition;
import com.gregmarut.querybuilder.cypher.condition.ContainsCondition;
import com.gregmarut.querybuilder.cypher.condition.EqualsCondition;
import com.gregmarut.querybuilder.cypher.condition.GreaterThanCondition;
import com.gregmarut.querybuilder.cypher.condition.NotNullCondition;
import com.gregmarut.querybuilder.cypher.condition.NullCondition;
import com.gregmarut.querybuilder.cypher.condition.StartsWithCondition;
import com.gregmarut.querybuilder.cypher.model.MovieNode;
import com.gregmarut.querybuilder.cypher.model.PersonNode;
import com.gregmarut.querybuilder.cypher.phrase.Where;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class WhereTest
{
	@BeforeAll
	static void setUp()
	{
		QueryBuilderContext.defaultPrettyPrint = true;
	}

	@Test
	public void matchWithWhereEquals()
	{
		final var identifierGenerator = new IdentifierGenerator();

		final var personNode = new PersonNode()
			.named(identifierGenerator);

		final var query = CypherBuilder.create()
			.match(personNode)
			.where(new EqualsCondition(personNode.getProperty(PersonNode.NAME), "Alice"))
			.addReturn(personNode)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)
			WHERE p.name = $_v0
			RETURN p""", query.getQuery());

		Assertions.assertEquals("Alice", query.getParams().get("_v0"));
	}

	@Test
	public void matchWithWhereContains()
	{
		final var identifierGenerator = new IdentifierGenerator();

		final var personNode = new PersonNode()
			.named(identifierGenerator);

		final var query = CypherBuilder.create()
			.match(personNode)
			.where(new ContainsCondition(personNode.getProperty(PersonNode.NAME), "Tom"))
			.addReturn(personNode)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)
			WHERE p.name CONTAINS $_v0
			RETURN p""", query.getQuery());

		Assertions.assertEquals("Tom", query.getParams().get("_v0"));
	}

	@Test
	public void matchWithWhereStartsWith()
	{
		final var identifierGenerator = new IdentifierGenerator();

		final var personNode = new PersonNode()
			.named(identifierGenerator);

		final var query = CypherBuilder.create()
			.match(personNode)
			.where(new StartsWithCondition(personNode.getProperty(PersonNode.NAME), "Tom"))
			.addReturn(personNode)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)
			WHERE p.name STARTS WITH $_v0
			RETURN p""", query.getQuery());

		Assertions.assertEquals("Tom", query.getParams().get("_v0"));
	}

	@Test
	public void matchWithWhereAnd()
	{
		final var identifierGenerator = new IdentifierGenerator();

		final var personNode = new PersonNode()
			.named(identifierGenerator);

		final var query = CypherBuilder.create()
			.match(personNode)
			.where(Where.and(
				new EqualsCondition(personNode.getProperty(PersonNode.NAME), "Tom"),
				new GreaterThanCondition(personNode.getProperty(PersonNode.BORN), 1960)
			))
			.addReturn(personNode)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)
			WHERE (p.name = $_v0 AND p.born > 1960)
			RETURN p""", query.getQuery());

		Assertions.assertEquals("Tom", query.getParams().get("_v0"));
	}

	@Test
	public void matchWithWhereOr()
	{
		final var identifierGenerator = new IdentifierGenerator();

		final var personNode = new PersonNode()
			.named(identifierGenerator);

		final var query = CypherBuilder.create()
			.match(personNode)
			.where(Where.or(
				new EqualsCondition(personNode.getProperty(PersonNode.NAME), "Tom"),
				new EqualsCondition(personNode.getProperty(PersonNode.NAME), "Jerry")
			))
			.addReturn(personNode)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)
			WHERE (p.name = $_v0 OR p.name = $_v1)
			RETURN p""", query.getQuery());

		Assertions.assertEquals("Tom", query.getParams().get("_v0"));
		Assertions.assertEquals("Jerry", query.getParams().get("_v1"));
	}

	@Test
	public void matchWithWhereIsNull()
	{
		final var identifierGenerator = new IdentifierGenerator();

		final var personNode = new PersonNode()
			.named(identifierGenerator);

		final var query = CypherBuilder.create()
			.match(personNode)
			.where(new NullCondition(personNode.getProperty(PersonNode.EMAIL)))
			.addReturn(personNode)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)
			WHERE p.email IS null
			RETURN p""", query.getQuery());

		Assertions.assertTrue(query.getParams().isEmpty());
	}

	@Test
	public void matchWithWhereIsNotNull()
	{
		final var identifierGenerator = new IdentifierGenerator();

		final var personNode = new PersonNode()
			.named(identifierGenerator);

		final var query = CypherBuilder.create()
			.match(personNode)
			.where(new NotNullCondition(personNode.getProperty(PersonNode.EMAIL)))
			.addReturn(personNode)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)
			WHERE p.email IS NOT null
			RETURN p""", query.getQuery());

		Assertions.assertTrue(query.getParams().isEmpty());
	}

	@Test
	public void anyInConditionDoesNotUseHardcodedElementVariable()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);
		final var movieNode = new MovieNode().named(identifierGenerator);

		final var cond = new AnyInCondition(
			personNode.getProperty("tags"),
			movieNode.getProperty("tags")
		);

		final var result = cond.build(QueryBuilderContext.createDefault());

		Assertions.assertFalse(result.contains("_element"));
	}
}
