package com.gregmarut.querybuilder.cypher;

import com.gregmarut.querybuilder.cypher.model.PersonNode;
import com.gregmarut.querybuilder.cypher.phrase.UsingIndex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class UsingIndexTest
{
	@BeforeAll
	static void setUp()
	{
		QueryBuilderContext.defaultPrettyPrint = true;
	}

	@Test
	public void usingIndexAddsHintBetweenMatchAndReturn()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);

		final var query = CypherBuilder.create()
			.match(personNode)
			.usingIndex(new UsingIndex(personNode, PersonNode.NAME))
			.addReturn(personNode)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)
			USING INDEX p:Person(name)
			RETURN p""", query.getQuery());
		Assertions.assertTrue(query.getParams().isEmpty());
	}
}
