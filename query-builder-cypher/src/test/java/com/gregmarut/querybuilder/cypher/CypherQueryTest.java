package com.gregmarut.querybuilder.cypher;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class CypherQueryTest
{
	@Test
	public void cypherQueryTrimsQueryString()
	{
		final var query = new CypherQuery("  MATCH (p:Person)  ", Map.of());
		Assertions.assertEquals("MATCH (p:Person)", query.getQuery());
	}

	@Test
	public void cypherQueryParamsAreUnmodifiable()
	{
		final var mutableMap = new HashMap<String, Object>();
		mutableMap.put("a", 1);
		final var query = new CypherQuery("RETURN 1", mutableMap);

		Assertions.assertThrows(UnsupportedOperationException.class, () -> query.getParams().put("b", 2));
	}

	@Test
	public void cypherQueryToStringReturnsQuery()
	{
		final var query = new CypherQuery("RETURN 1", Map.of());
		Assertions.assertEquals("RETURN 1", query.toString());
	}

	@Test
	public void cypherQueryEqualsAndHashCode()
	{
		final var q1 = new CypherQuery("MATCH (p)", Map.of("x", 1));
		final var q2 = new CypherQuery("MATCH (p)", Map.of("x", 1));
		final var q3 = new CypherQuery("MATCH (m)", Map.of("x", 1));

		Assertions.assertEquals(q1, q2);
		Assertions.assertEquals(q1.hashCode(), q2.hashCode());
		Assertions.assertNotEquals(q1, q3);
	}
}
