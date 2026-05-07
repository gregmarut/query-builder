package com.gregmarut.querybuilder.cypher;

import com.gregmarut.querybuilder.cypher.condition.GreaterThanCondition;
import com.gregmarut.querybuilder.cypher.model.MovieNode;
import com.gregmarut.querybuilder.cypher.model.PersonNode;
import com.gregmarut.querybuilder.cypher.model.Relationships;
import com.gregmarut.querybuilder.cypher.phrase.Match;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class OptionalMatchTest
{
	@BeforeAll
	static void setUp()
	{
		QueryBuilderContext.defaultPrettyPrint = true;
	}
	
	@Test
	public void optionalMatch()
	{
		final var identifierGenerator = new IdentifierGenerator();
		
		final var personNode = new PersonNode()
			.withId("abc123")
			.named(identifierGenerator);
		
		final var movieNode = new MovieNode()
			.named(identifierGenerator);
		
		final var query = CypherBuilder.create()
			.match(personNode)
			.match(Match.optional(Path.start(personNode).out(Relationships.DIRECTED).to(movieNode).build()))
			.addReturn(personNode, movieNode)
			.build();
		
		Assertions.assertEquals("""
			MATCH (p:Person{id: $_v0_id})
			OPTIONAL MATCH (p)-[:DIRECTED]->(m:Movie)
			RETURN p, m""", query.getQuery());
		
		Assertions.assertEquals("abc123", query.getParams().get("_v0_id"));
	}
	
	@Test
	public void optionalMatchWithWhere()
	{
		final var identifierGenerator = new IdentifierGenerator();
		
		final var personNode = new PersonNode()
			.named(identifierGenerator);
		
		final var movieNode = new MovieNode()
			.named(identifierGenerator);
		
		final var query = CypherBuilder.create()
			.match(personNode)
			.match(Match.optional(Path.start(personNode).out(Relationships.ACTED_IN).to(movieNode).build()))
			.where(new GreaterThanCondition(movieNode.getProperty(MovieNode.RELEASED), 2000))
			.addReturn(movieNode)
			.build();
		
		Assertions.assertEquals("""
			MATCH (p:Person)
			OPTIONAL MATCH (p)-[:ACTED_IN]->(m:Movie)
			WHERE m.released > 2000
			RETURN m""", query.getQuery());
		
		Assertions.assertTrue(query.getParams().isEmpty());
	}
}
