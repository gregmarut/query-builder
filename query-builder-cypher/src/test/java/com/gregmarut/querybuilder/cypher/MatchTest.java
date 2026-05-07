package com.gregmarut.querybuilder.cypher;

import com.gregmarut.querybuilder.cypher.model.MovieNode;
import com.gregmarut.querybuilder.cypher.model.PersonNode;
import com.gregmarut.querybuilder.cypher.model.Relationships;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class MatchTest
{
	@BeforeAll
	static void setUp()
	{
		QueryBuilderContext.defaultPrettyPrint = true;
	}
	
	@Test
	public void match()
	{
		final var identifierGenerator = new IdentifierGenerator();
		
		//define the person node with an id property and use the identifier generator to generate a unique identifier for this node
		final var personNode = new PersonNode()
			.withId("abc123")
			.named(identifierGenerator);
		
		//build the query that finds a person node with the given id
		final var query = CypherBuilder.create()
			.match(personNode)
			.addReturn(personNode)
			.build();
		
		//verify the query
		Assertions.assertEquals("""
			MATCH (p:Person{id: $_v0_id})
			RETURN p""", query.getQuery());
		
		//verify the parameters
		Assertions.assertEquals("abc123", query.getParams().get("_v0_id"));
	}
	
	@Test
	public void matchWithRelationship()
	{
		final var identifierGenerator = new IdentifierGenerator();
		
		//define the person node with an id property and use the identifier generator to generate a unique identifier for this node
		final var personNode = new PersonNode()
			.withId("abc123");
		final var movieNode = new MovieNode()
			.named(identifierGenerator);
		
		//build the query that finds all movies that a person has acted in
		final var query = CypherBuilder.create()
			.match(Path.start(personNode).out(Relationships.ACTED_IN).to(movieNode).build())
			.addReturn(movieNode)
			.build();
		
		//verify the query
		Assertions.assertEquals("""
			MATCH (:Person{id: $_v0_id})-[:ACTED_IN]->(m:Movie)
			RETURN m""", query.getQuery());
		
		//verify the parameters
		Assertions.assertEquals("abc123", query.getParams().get("_v0_id"));
	}
}
