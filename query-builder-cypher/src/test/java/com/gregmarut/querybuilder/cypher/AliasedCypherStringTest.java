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

import com.gregmarut.querybuilder.cypher.function.Count;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AliasedCypherStringTest
{
	@Test
	public void aliasedCypherStringRejectsBlankAlias()
	{
		final var inner = Count.of(LiteralCypherString.of("x"));

		Assertions.assertThrows(IllegalArgumentException.class, () -> inner.as(""));
		Assertions.assertThrows(IllegalArgumentException.class, () -> inner.as("   "));
	}

	@Test
	public void aliasedCypherStringRejectsNullAlias()
	{
		final var inner = Count.of(LiteralCypherString.of("x"));

		Assertions.assertThrows(IllegalArgumentException.class, () -> inner.as(null));
	}

	@Test
	public void aliasedCypherStringRejectsInvalidIdentifier()
	{
		final var inner = Count.of(LiteralCypherString.of("x"));

		//leading digit and special characters are invalid cypher identifiers
		Assertions.assertThrows(IllegalArgumentException.class, () -> inner.as("1bad"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> inner.as("has-dash"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> inner.as("with space"));
	}

	@Test
	public void aliasedCypherStringAcceptsValidIdentifiers()
	{
		final var inner = Count.of(LiteralCypherString.of("x"));

		Assertions.assertDoesNotThrow(() -> inner.as("myAlias"));
		Assertions.assertDoesNotThrow(() -> inner.as("_leadingUnderscore"));
		Assertions.assertDoesNotThrow(() -> inner.as("alias_1"));
	}

	@Test
	public void aliasedCypherStringConstructorViaGeneratorIsValid()
	{
		//the IdentifierGenerator-based constructor must also produce a valid alias
		final var inner = Count.of(LiteralCypherString.of("x"));
		final var generator = new IdentifierGenerator();

		Assertions.assertDoesNotThrow(() -> new AliasedCypherString<>(inner, generator));
	}
}
