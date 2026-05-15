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
