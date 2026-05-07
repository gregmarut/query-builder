package com.gregmarut.querybuilder.cypher;

import com.gregmarut.querybuilder.cypher.condition.ContainsCondition;
import com.gregmarut.querybuilder.cypher.condition.EqualsCondition;
import com.gregmarut.querybuilder.cypher.condition.GreaterThanCondition;
import com.gregmarut.querybuilder.cypher.condition.NotNullCondition;
import com.gregmarut.querybuilder.cypher.condition.NullCondition;
import com.gregmarut.querybuilder.cypher.condition.StartsWithCondition;
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
}
