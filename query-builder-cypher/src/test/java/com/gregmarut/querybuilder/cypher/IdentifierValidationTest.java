package com.gregmarut.querybuilder.cypher;

import com.gregmarut.querybuilder.cypher.model.PersonNode;
import com.gregmarut.querybuilder.cypher.model.Relationships;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IdentifierValidationTest
{
	@Test
	public void identifierBeginningWithDigitIsRejectedOnNode()
	{
		final var personNode = new PersonNode();
		//cypher identifiers must start with a letter or underscore
		Assertions.assertThrows(RuntimeException.class, () -> personNode.named("1bad"));
	}

	@Test
	public void identifierBeginningWithDigitIsRejectedOnRelationship()
	{
		final var rel = Relationship.of(Relationships.ACTED_IN);
		Assertions.assertThrows(RuntimeException.class, () -> rel.named("9rel"));
	}

	@Test
	public void allDigitIdentifierIsRejected()
	{
		final var personNode = new PersonNode();
		Assertions.assertThrows(RuntimeException.class, () -> personNode.named("123"));
	}

	@Test
	public void identifierContainingSpecialCharactersIsRejected()
	{
		final var personNode = new PersonNode();
		Assertions.assertThrows(RuntimeException.class, () -> personNode.named("has-dash"));
		Assertions.assertThrows(RuntimeException.class, () -> personNode.named("has space"));
		Assertions.assertThrows(RuntimeException.class, () -> personNode.named("has.dot"));
	}

	@Test
	public void identifierStartingWithLetterIsAccepted()
	{
		final var personNode = new PersonNode();
		Assertions.assertDoesNotThrow(() -> personNode.named("alice"));
	}

	@Test
	public void identifierStartingWithUnderscoreIsAccepted()
	{
		final var personNode = new PersonNode();
		//cypher accepts leading underscores; the previous regex incorrectly rejected them
		Assertions.assertDoesNotThrow(() -> personNode.named("_alice"));
	}

	@Test
	public void identifierContainingUnderscoreInMiddleIsAccepted()
	{
		final var personNode = new PersonNode();
		Assertions.assertDoesNotThrow(() -> personNode.named("first_name"));
	}

	@Test
	public void identifierContainingDigitsAfterFirstCharIsAccepted()
	{
		final var personNode = new PersonNode();
		Assertions.assertDoesNotThrow(() -> personNode.named("alice42"));
	}

	@Test
	public void identifierRegexAcceptsTypicalCamelCaseAndSnakeCase()
	{
		Assertions.assertTrue("camelCase".matches(CypherConstants.IDENTIFIER_REGEX));
		Assertions.assertTrue("snake_case".matches(CypherConstants.IDENTIFIER_REGEX));
		Assertions.assertTrue("_leading".matches(CypherConstants.IDENTIFIER_REGEX));
		Assertions.assertTrue("a".matches(CypherConstants.IDENTIFIER_REGEX));
	}

	@Test
	public void identifierRegexRejectsLeadingDigitAndSpecialChars()
	{
		Assertions.assertFalse("1abc".matches(CypherConstants.IDENTIFIER_REGEX));
		Assertions.assertFalse("123".matches(CypherConstants.IDENTIFIER_REGEX));
		Assertions.assertFalse("a-b".matches(CypherConstants.IDENTIFIER_REGEX));
		Assertions.assertFalse("".matches(CypherConstants.IDENTIFIER_REGEX));
	}
}
