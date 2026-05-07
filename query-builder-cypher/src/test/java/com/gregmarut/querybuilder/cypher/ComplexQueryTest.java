package com.gregmarut.querybuilder.cypher;

import com.gregmarut.querybuilder.cypher.condition.GreaterThanCondition;
import com.gregmarut.querybuilder.cypher.model.GenreNode;
import com.gregmarut.querybuilder.cypher.model.MovieNode;
import com.gregmarut.querybuilder.cypher.model.PersonNode;
import com.gregmarut.querybuilder.cypher.model.Relationships;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ComplexQueryTest
{
	@BeforeAll
	static void setUp()
	{
		QueryBuilderContext.defaultPrettyPrint = true;
	}
	
	@Test
	public void multiHopRelationship()
	{
		final var identifierGenerator = new IdentifierGenerator();
		
		final var personNode = new PersonNode()
			.withId("abc123")
			.named(identifierGenerator);
		
		final var movieNode = new MovieNode()
			.named(identifierGenerator);
		
		final var genreNode = new GenreNode()
			.named(identifierGenerator);
		
		final var query = CypherBuilder.create()
			.match(Path.start(personNode).out(Relationships.ACTED_IN).to(movieNode).build())
			.match(Path.start(movieNode).out(Relationships.IN_GENRE).to(genreNode).build())
			.addReturn(personNode, genreNode)
			.build();
		
		Assertions.assertEquals("""
			MATCH (p:Person{id: $_v0_id})-[:ACTED_IN]->(m:Movie)
			MATCH (m)-[:IN_GENRE]->(g:Genre)
			RETURN p, g""", query.getQuery());
		
		Assertions.assertEquals("abc123", query.getParams().get("_v0_id"));
	}
	
	@Test
	public void returnDistinct()
	{
		final var identifierGenerator = new IdentifierGenerator();
		
		final var personNode = new PersonNode()
			.named(identifierGenerator);
		
		final var movieNode = new MovieNode()
			.named(identifierGenerator);
		
		final var genreNode = new GenreNode()
			.named(identifierGenerator);
		
		final var query = CypherBuilder.create()
			.match(Path.start(personNode).out(Relationships.ACTED_IN).to(movieNode).build())
			.match(Path.start(movieNode).out(Relationships.IN_GENRE).to(genreNode).build())
			.addReturn(genreNode)
			.distinct()
			.build();
		
		Assertions.assertEquals("""
			MATCH (p:Person)-[:ACTED_IN]->(m:Movie)
			MATCH (m)-[:IN_GENRE]->(g:Genre)
			RETURN DISTINCT g""", query.getQuery());
		
		Assertions.assertTrue(query.getParams().isEmpty());
	}
	
	@Test
	public void matchWithWhereAndOrderByLimit()
	{
		final var identifierGenerator = new IdentifierGenerator();
		
		final var personNode = new PersonNode()
			.named(identifierGenerator);
		
		final var movieNode = new MovieNode()
			.named(identifierGenerator);
		
		final var query = CypherBuilder.create()
			.match(Path.start(personNode).out(Relationships.ACTED_IN).to(movieNode).build())
			.where(new GreaterThanCondition(movieNode.getProperty(MovieNode.RELEASED), 2000))
			.addReturn(personNode, movieNode)
			.orderBy(movieNode.getProperty(MovieNode.RELEASED), OrderType.DESC)
			.limit(10)
			.build();
		
		Assertions.assertEquals("""
			MATCH (p:Person)-[:ACTED_IN]->(m:Movie)
			WHERE m.released > 2000
			RETURN p, m
			ORDER BY m.released DESC
			LIMIT 10""", query.getQuery());
		
		Assertions.assertTrue(query.getParams().isEmpty());
	}
}
