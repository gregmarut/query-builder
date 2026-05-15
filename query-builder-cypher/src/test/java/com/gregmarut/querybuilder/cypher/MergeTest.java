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
