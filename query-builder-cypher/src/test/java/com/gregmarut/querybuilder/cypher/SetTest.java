package com.gregmarut.querybuilder.cypher;

import com.gregmarut.querybuilder.cypher.model.PersonNode;
import com.gregmarut.querybuilder.cypher.phrase.SetMerge;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

public class SetTest
{
	@BeforeAll
	static void setUp()
	{
		QueryBuilderContext.defaultPrettyPrint = true;
	}

	@Test
	public void setSingleProperty()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode()
			.withId("abc123")
			.named(identifierGenerator);

		final var properties = Map.<Property, CypherString>of(
			personNode.getProperty(PersonNode.NAME), Variable.of("Alice")
		);

		final var query = CypherBuilder.create()
			.match(personNode)
			.set(new Set(properties))
			.addReturn(personNode)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person{id: $_v0_id})
			SET p.name = $_v1
			RETURN p""", query.getQuery());
		Assertions.assertEquals("abc123", query.getParams().get("_v0_id"));
		Assertions.assertEquals("Alice", query.getParams().get("_v1"));
	}

	@Test
	public void setMultiplePropertiesIsAlphabeticalByName()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);

		//use a LinkedHashMap with insertion order that differs from the expected output to validate sort order
		final var properties = new LinkedHashMap<Property, CypherString>();
		properties.put(personNode.getProperty(PersonNode.NAME), Variable.of("Alice"));
		properties.put(personNode.getProperty(PersonNode.BORN), Variable.of(1980));
		properties.put(personNode.getProperty(PersonNode.EMAIL), Variable.of("a@example.com"));

		final var query = CypherBuilder.create()
			.match(personNode)
			.set(new Set(properties))
			.addReturn(personNode)
			.build();

		//verify properties are emitted sorted alphabetically by property name: born, email, name
		Assertions.assertTrue(query.getQuery().contains("SET p.born = $"));
		final var setIndex = query.getQuery().indexOf("SET ");
		final var bornIndex = query.getQuery().indexOf("p.born", setIndex);
		final var emailIndex = query.getQuery().indexOf("p.email", setIndex);
		final var nameIndex = query.getQuery().indexOf("p.name", setIndex);
		Assertions.assertTrue(bornIndex < emailIndex);
		Assertions.assertTrue(emailIndex < nameIndex);
	}

	@Test
	public void setNullPropertiesThrows()
	{
		Assertions.assertThrows(IllegalArgumentException.class, () -> new Set(null));
	}

	@Test
	public void setEmptyPropertiesThrows()
	{
		Assertions.assertThrows(IllegalArgumentException.class, () -> new Set(Map.of()));
	}

	@Test
	public void setMergeProducesPlusEqualsExpression()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode()
			.withId("abc123")
			.named(identifierGenerator);

		final var query = CypherBuilder.create()
			.match(personNode)
			.set(new SetMerge(personNode, Variable.of(Map.of("name", "Alice"))))
			.addReturn(personNode)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person{id: $_v0_id})
			SET p += $_v1
			RETURN p""", query.getQuery());
		Assertions.assertEquals("abc123", query.getParams().get("_v0_id"));
		Assertions.assertEquals(Map.of("name", "Alice"), query.getParams().get("_v1"));
	}
}
