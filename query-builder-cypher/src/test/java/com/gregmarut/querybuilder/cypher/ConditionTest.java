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
import com.gregmarut.querybuilder.cypher.condition.CustomCondition;
import com.gregmarut.querybuilder.cypher.condition.EqualsCondition;
import com.gregmarut.querybuilder.cypher.condition.GreaterThanCondition;
import com.gregmarut.querybuilder.cypher.condition.GreaterThanEqualCondition;
import com.gregmarut.querybuilder.cypher.condition.InCondition;
import com.gregmarut.querybuilder.cypher.condition.LessThanCondition;
import com.gregmarut.querybuilder.cypher.condition.LessThanEqualCondition;
import com.gregmarut.querybuilder.cypher.condition.NotEqualsCondition;
import com.gregmarut.querybuilder.cypher.condition.NotPathCondition;
import com.gregmarut.querybuilder.cypher.condition.OrCondition;
import com.gregmarut.querybuilder.cypher.model.MovieNode;
import com.gregmarut.querybuilder.cypher.model.PersonNode;
import com.gregmarut.querybuilder.cypher.model.Relationships;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ConditionTest
{
	@BeforeAll
	static void setUp()
	{
		QueryBuilderContext.defaultPrettyPrint = true;
	}

	@Test
	public void notEqualsConditionWithString()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);

		final var query = CypherBuilder.create()
			.match(personNode)
			.where(new NotEqualsCondition(personNode.getProperty(PersonNode.NAME), "Alice"))
			.addReturn(personNode)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)
			WHERE p.name <> $_v0
			RETURN p""", query.getQuery());
		Assertions.assertEquals("Alice", query.getParams().get("_v0"));
	}

	@Test
	public void inConditionWithCollection()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);

		//check that the name property is in a list of values
		final var values = List.of(Variable.of("Alice"), Variable.of("Bob"), Variable.of("Carol"));
		final var query = CypherBuilder.create()
			.match(personNode)
			.where(new InCondition(personNode.getProperty(PersonNode.NAME), values))
			.addReturn(personNode)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)
			WHERE p.name IN [$_v0, $_v1, $_v2]
			RETURN p""", query.getQuery());
		Assertions.assertEquals("Alice", query.getParams().get("_v0"));
		Assertions.assertEquals("Bob", query.getParams().get("_v1"));
		Assertions.assertEquals("Carol", query.getParams().get("_v2"));
	}

	@Test
	public void inConditionWithCypherStringRightHandSide()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);

		final var query = CypherBuilder.create()
			.match(personNode)
			.where(new InCondition(personNode.getProperty(PersonNode.NAME), Variable.of(List.of("Alice", "Bob"))))
			.addReturn(personNode)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)
			WHERE p.name IN $_v0
			RETURN p""", query.getQuery());
		Assertions.assertEquals(List.of("Alice", "Bob"), query.getParams().get("_v0"));
	}

	@Test
	public void lessThanCondition()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);

		final var query = CypherBuilder.create()
			.match(personNode)
			.where(new LessThanCondition(personNode.getProperty(PersonNode.BORN), 1980))
			.addReturn(personNode)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)
			WHERE p.born < 1980
			RETURN p""", query.getQuery());
		Assertions.assertTrue(query.getParams().isEmpty());
	}

	@Test
	public void lessThanEqualCondition()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);

		final var query = CypherBuilder.create()
			.match(personNode)
			.where(new LessThanEqualCondition(personNode.getProperty(PersonNode.BORN), 1980))
			.addReturn(personNode)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)
			WHERE p.born <= 1980
			RETURN p""", query.getQuery());
		Assertions.assertTrue(query.getParams().isEmpty());
	}

	@Test
	public void greaterThanEqualCondition()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);

		final var query = CypherBuilder.create()
			.match(personNode)
			.where(new GreaterThanEqualCondition(personNode.getProperty(PersonNode.BORN), Variable.of(1980)))
			.addReturn(personNode)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)
			WHERE p.born >= $_v0
			RETURN p""", query.getQuery());
		Assertions.assertEquals(1980, query.getParams().get("_v0"));
	}

	@Test
	public void equalsConditionWithBoolean()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);

		final var query = CypherBuilder.create()
			.match(personNode)
			.where(new EqualsCondition(personNode.getProperty("active"), true))
			.addReturn(personNode)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)
			WHERE p.active = true
			RETURN p""", query.getQuery());
		Assertions.assertTrue(query.getParams().isEmpty());
	}

	@Test
	public void equalsConditionWithNumber()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);

		final var query = CypherBuilder.create()
			.match(personNode)
			.where(new EqualsCondition(personNode.getProperty(PersonNode.BORN), 1970))
			.addReturn(personNode)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)
			WHERE p.born = 1970
			RETURN p""", query.getQuery());
		Assertions.assertTrue(query.getParams().isEmpty());
	}

	@Test
	public void nestedAndOrCondition()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);

		//mix AND with nested OR: name = "Alice" AND (born > 1980 OR born < 1950)
		final var query = CypherBuilder.create()
			.match(personNode)
			.where(new AndCondition(
				new EqualsCondition(personNode.getProperty(PersonNode.NAME), "Alice"),
				new OrCondition(
					new GreaterThanCondition(personNode.getProperty(PersonNode.BORN), 1980),
					new LessThanCondition(personNode.getProperty(PersonNode.BORN), 1950)
				)
			))
			.addReturn(personNode)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)
			WHERE (p.name = $_v0 AND (p.born > 1980 OR p.born < 1950))
			RETURN p""", query.getQuery());
		Assertions.assertEquals("Alice", query.getParams().get("_v0"));
	}

	@Test
	public void andConditionFromList()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);

		//symmetric with OrCondition(List): accept a list of conditions
		final var conditions = List.of(
			new EqualsCondition(personNode.getProperty(PersonNode.NAME), "Alice"),
			new GreaterThanCondition(personNode.getProperty(PersonNode.BORN), 1980)
		);
		final var query = CypherBuilder.create()
			.match(personNode)
			.where(new AndCondition(conditions))
			.addReturn(personNode)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)
			WHERE (p.name = $_v0 AND p.born > 1980)
			RETURN p""", query.getQuery());
		Assertions.assertEquals("Alice", query.getParams().get("_v0"));
	}

	@Test
	public void orConditionFromList()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);

		final var conditions = List.of(
			new EqualsCondition(personNode.getProperty(PersonNode.NAME), "Alice"),
			new EqualsCondition(personNode.getProperty(PersonNode.NAME), "Bob")
		);
		final var query = CypherBuilder.create()
			.match(personNode)
			.where(new OrCondition(conditions))
			.addReturn(personNode)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)
			WHERE (p.name = $_v0 OR p.name = $_v1)
			RETURN p""", query.getQuery());
		Assertions.assertEquals("Alice", query.getParams().get("_v0"));
		Assertions.assertEquals("Bob", query.getParams().get("_v1"));
	}

	@Test
	public void notPathCondition()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);
		final var movieNode = new MovieNode().named(identifierGenerator);

		//find any person who has not acted in the movie
		final var query = CypherBuilder.create()
			.match(personNode)
			.match(movieNode)
			.where(new NotPathCondition(Path.start(personNode).out(Relationships.ACTED_IN).to(movieNode).build()))
			.addReturn(personNode)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)
			MATCH (m:Movie)
			WHERE NOT (p)-[:ACTED_IN]->(m)
			RETURN p""", query.getQuery());
		Assertions.assertTrue(query.getParams().isEmpty());
	}

	@Test
	public void customConditionPropertyGTENowMinusDays()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);

		final var query = CypherBuilder.create()
			.match(personNode)
			.where(CustomCondition.propertyGTENowMinusDays(personNode.getProperty("createdAt"), 7))
			.addReturn(personNode)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)
			WHERE p.createdAt >= datetime() - duration($_v0)
			RETURN p""", query.getQuery());
		Assertions.assertEquals("P7D", query.getParams().get("_v0"));
	}

	@Test
	public void customConditionPropertyGTENowMinusMonths()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);

		final var query = CypherBuilder.create()
			.match(personNode)
			.where(CustomCondition.propertyGTENowMinusMonths(personNode.getProperty("createdAt"), 3))
			.addReturn(personNode)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)
			WHERE p.createdAt >= datetime() - duration($_v0)
			RETURN p""", query.getQuery());
		Assertions.assertEquals("P3M", query.getParams().get("_v0"));
	}

	@Test
	public void customConditionFalseOrNull()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);

		final var query = CypherBuilder.create()
			.match(personNode)
			.where(CustomCondition.falseOrNull(personNode.getProperty("deleted")))
			.addReturn(personNode)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)
			WHERE (p.deleted IS null OR p.deleted = false)
			RETURN p""", query.getQuery());
		Assertions.assertTrue(query.getParams().isEmpty());
	}
}
