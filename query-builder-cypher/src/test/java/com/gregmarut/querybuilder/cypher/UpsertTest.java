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

import com.gregmarut.querybuilder.cypher.function.DateTime;
import com.gregmarut.querybuilder.cypher.model.PersonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class UpsertTest
{
	@BeforeAll
	static void setUp()
	{
		QueryBuilderContext.defaultPrettyPrint = true;
	}

	@Test
	public void upsertWithCypherStringPropertyEmbedsExpressionRatherThanWrappingInParam()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode()
			.withId("abc123")
			.withName("Alice")
			.named(identifierGenerator);

		//attaching a CypherString-valued property should render directly as cypher (e.g. datetime()),
		//not become a parameter whose value is the CypherString object itself
		personNode.withProperty("createdAt", new DateTime());

		final var query = CypherBuilder.create()
			.upsert(new Upsert(personNode))
			.addReturn(personNode)
			.build();

		Assertions.assertTrue(query.getQuery().contains("p.createdAt = datetime()"),
			"expected datetime() literal in SET clause, got: " + query.getQuery());

		//no parameter should hold a CypherString instance — it would never serialize to a real Neo4j param
		final var hasCypherStringParam = query.getParams()
			.values()
			.stream()
			.anyMatch(v -> v instanceof CypherString);
		Assertions.assertFalse(hasCypherStringParam, "no parameter should hold a CypherString object");
	}

	@Test
	public void upsertWithLiteralCypherStringPropertyEmbedsLiteral()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode()
			.withId("abc123")
			.named(identifierGenerator);

		//literal cypher fragment used as a property value
		personNode.withProperty("status", LiteralCypherString.of("'active'"));

		final var query = CypherBuilder.create()
			.upsert(new Upsert(personNode))
			.addReturn(personNode)
			.build();

		Assertions.assertTrue(query.getQuery().contains("p.status = 'active'"),
			"expected literal embedded directly, got: " + query.getQuery());
	}

	@Test
	public void upsertWithPlainValuePropertyStillParameterizes()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode()
			.withId("abc123")
			.withName("Alice")
			.named(identifierGenerator);

		final var query = CypherBuilder.create()
			.upsert(new Upsert(personNode))
			.addReturn(personNode)
			.build();

		//plain values still become parameters as before
		Assertions.assertEquals("""
			MERGE (p:Person{id: $_v0_id})
			SET p.name = $_v1
			RETURN p""", query.getQuery());
		Assertions.assertEquals("abc123", query.getParams().get("_v0_id"));
		Assertions.assertEquals("Alice", query.getParams().get("_v1"));
	}
}
