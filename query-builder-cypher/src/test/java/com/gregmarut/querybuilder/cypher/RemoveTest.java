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
import com.gregmarut.querybuilder.cypher.phrase.Remove;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

public class RemoveTest
{
	@BeforeAll
	static void setUp()
	{
		QueryBuilderContext.defaultPrettyPrint = true;
	}

	@Test
	public void removeSingleProperty()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode()
			.withId("abc123")
			.named(identifierGenerator);

		final var query = CypherBuilder.create()
			.match(personNode)
			.remove(Remove.of(personNode.getProperty(PersonNode.EMAIL)))
			.addReturn(personNode)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person{id: $_v0_id})
			REMOVE p.email
			RETURN p""", query.getQuery());
		Assertions.assertEquals("abc123", query.getParams().get("_v0_id"));
	}

	@Test
	public void removeMultipleProperties()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);

		final var query = CypherBuilder.create()
			.match(personNode)
			.remove(Remove.of(personNode.getProperty(PersonNode.EMAIL), personNode.getProperty(PersonNode.BORN)))
			.addReturn(personNode)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)
			REMOVE p.email, p.born
			RETURN p""", query.getQuery());
		Assertions.assertTrue(query.getParams().isEmpty());
	}

	@Test
	public void removeWithListFactory()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);

		final var query = CypherBuilder.create()
			.match(personNode)
			.remove(Remove.of(List.of(personNode.getProperty(PersonNode.NAME))))
			.addReturn(personNode)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)
			REMOVE p.name
			RETURN p""", query.getQuery());
		Assertions.assertTrue(query.getParams().isEmpty());
	}

	@Test
	public void removeNullPropertiesThrows()
	{
		Assertions.assertThrows(IllegalArgumentException.class, () -> Remove.of((Property[]) null));
	}
}
