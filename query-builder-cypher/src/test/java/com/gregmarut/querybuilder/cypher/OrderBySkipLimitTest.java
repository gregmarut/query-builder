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

public class OrderBySkipLimitTest
{
	@BeforeAll
	static void setUp()
	{
		QueryBuilderContext.defaultPrettyPrint = true;
	}
	
	@Test
	public void orderByAsc()
	{
		final var identifierGenerator = new IdentifierGenerator();
		
		final var personNode = new PersonNode()
			.named(identifierGenerator);
		
		final var query = CypherBuilder.create()
			.match(personNode)
			.addReturn(personNode)
			.orderBy(personNode.getProperty(PersonNode.NAME))
			.build();
		
		Assertions.assertEquals("""
			MATCH (p:Person)
			RETURN p
			ORDER BY p.name ASC""", query.getQuery());
		
		Assertions.assertTrue(query.getParams().isEmpty());
	}
	
	@Test
	public void orderByDesc()
	{
		final var identifierGenerator = new IdentifierGenerator();
		
		final var personNode = new PersonNode()
			.named(identifierGenerator);
		
		final var query = CypherBuilder.create()
			.match(personNode)
			.addReturn(personNode)
			.orderBy(personNode.getProperty(PersonNode.NAME), OrderType.DESC)
			.build();
		
		Assertions.assertEquals("""
			MATCH (p:Person)
			RETURN p
			ORDER BY p.name DESC""", query.getQuery());
		
		Assertions.assertTrue(query.getParams().isEmpty());
	}
	
	@Test
	public void skipAndLimit()
	{
		final var identifierGenerator = new IdentifierGenerator();
		
		final var personNode = new PersonNode()
			.named(identifierGenerator);
		
		final var query = CypherBuilder.create()
			.match(personNode)
			.addReturn(personNode)
			.skip(10)
			.limit(25)
			.build();
		
		Assertions.assertEquals("""
			MATCH (p:Person)
			RETURN p
			SKIP 10
			LIMIT 25""", query.getQuery());
		
		Assertions.assertTrue(query.getParams().isEmpty());
	}
	
	@Test
	public void orderByWithSkipAndLimit()
	{
		final var identifierGenerator = new IdentifierGenerator();

		final var personNode = new PersonNode()
			.named(identifierGenerator);

		final var query = CypherBuilder.create()
			.match(personNode)
			.addReturn(personNode)
			.orderBy(personNode.getProperty(PersonNode.BORN), OrderType.DESC)
			.skip(20)
			.limit(10)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)
			RETURN p
			ORDER BY p.born DESC
			SKIP 20
			LIMIT 10""", query.getQuery());

		Assertions.assertTrue(query.getParams().isEmpty());
	}

	@Test
	public void orderByBuildsPropertyUsingProvidedContext()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);

		final var context = QueryBuilderContext.createDefault();

		final var orderBy = new OrderBy(personNode.getProperty(PersonNode.NAME), OrderType.DESC);
		final var result = orderBy.build(context);

		Assertions.assertEquals("p.name DESC", result);
	}
}
