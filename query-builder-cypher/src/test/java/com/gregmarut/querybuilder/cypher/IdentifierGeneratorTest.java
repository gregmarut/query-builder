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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IdentifierGeneratorTest
{
	@Test
	public void uniqueReturnsShortestUniquePrefix()
	{
		final var generator = new IdentifierGenerator();

		Assertions.assertEquals("p", generator.unique("Person"));
		//"p" is taken, so next must extend the prefix
		Assertions.assertEquals("pe", generator.unique("Person"));
		//"pe" is taken, so the third must extend further
		Assertions.assertEquals("per", generator.unique("Person"));
	}

	@Test
	public void uniqueExhaustsLabelThenAppendsNumber()
	{
		final var generator = new IdentifierGenerator();

		//exhaust all single-character variants
		Assertions.assertEquals("p", generator.unique("P"));
		//now the full label is taken, so suffix with 1
		Assertions.assertEquals("p1", generator.unique("P"));
		Assertions.assertEquals("p2", generator.unique("P"));
	}

	@Test
	public void uniqueIsCaseInsensitive()
	{
		final var generator = new IdentifierGenerator();

		Assertions.assertEquals("a", generator.unique("ACTED_IN"));
		Assertions.assertEquals("ac", generator.unique("Acted_In"));
	}

	@Test
	public void nextReturnsIncrementingSequence()
	{
		final var generator = new IdentifierGenerator();

		Assertions.assertEquals("_i_1", generator.next());
		Assertions.assertEquals("_i_2", generator.next());
		Assertions.assertEquals("_i_3", generator.next());
	}

	@Test
	public void differentLabelsGetDistinctPrefixes()
	{
		final var generator = new IdentifierGenerator();

		Assertions.assertEquals("p", generator.unique("Person"));
		Assertions.assertEquals("m", generator.unique("Movie"));
		Assertions.assertEquals("g", generator.unique("Genre"));
	}
}
