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
