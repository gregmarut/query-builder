package com.gregmarut.querybuilder.cypher;

import com.gregmarut.querybuilder.cypher.model.PersonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class MergeTest
{
	@BeforeAll
	static void setUp()
	{
		QueryBuilderContext.defaultPrettyPrint = true;
	}

	@Test
	public void merge()
	{
		final var identifierGenerator = new IdentifierGenerator();

		final var personNode = new PersonNode()
			.withId("abc123")
			.named(identifierGenerator);

		final var query = CypherBuilder.create()
			.merge(new Merge(personNode))
			.addReturn(personNode)
			.build();

		Assertions.assertEquals("""
			MERGE (p:Person{id: $_v0_id})
			RETURN p""", query.getQuery());

		Assertions.assertEquals("abc123", query.getParams().get("_v0_id"));
	}

	@Test
	public void upsert()
	{
		final var identifierGenerator = new IdentifierGenerator();

		final var personNode = new PersonNode()
			.withId("abc123")
			.withName("Tom")
			.named(identifierGenerator);

		final var query = CypherBuilder.create()
			.upsert(new Upsert(personNode))
			.addReturn(personNode)
			.build();

		Assertions.assertEquals("""
			MERGE (p:Person{id: $_v0_id})
			SET p.name = $_v1
			RETURN p""", query.getQuery());

		Assertions.assertEquals("abc123", query.getParams().get("_v0_id"));
		Assertions.assertEquals("Tom", query.getParams().get("_v1"));
	}
}
