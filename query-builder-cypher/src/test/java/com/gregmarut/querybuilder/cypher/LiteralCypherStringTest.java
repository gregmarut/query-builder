package com.gregmarut.querybuilder.cypher;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LiteralCypherStringTest
{
	@Test
	public void nullLiteralBuildReturnsStringNull()
	{
		final var context = QueryBuilderContext.createDefault();
		final var result = LiteralCypherString.NULL.build(context);

		Assertions.assertNotNull(result);
		Assertions.assertEquals("null", result);
	}
}
