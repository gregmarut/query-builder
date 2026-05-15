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
