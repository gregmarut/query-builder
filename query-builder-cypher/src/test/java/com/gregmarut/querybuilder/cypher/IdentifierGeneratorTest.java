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
