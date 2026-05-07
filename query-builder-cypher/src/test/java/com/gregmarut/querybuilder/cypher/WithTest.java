package com.gregmarut.querybuilder.cypher;

import com.gregmarut.querybuilder.cypher.function.Count;
import com.gregmarut.querybuilder.cypher.model.MovieNode;
import com.gregmarut.querybuilder.cypher.model.PersonNode;
import com.gregmarut.querybuilder.cypher.model.Relationships;
import com.gregmarut.querybuilder.cypher.phrase.With;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class WithTest
{
	@BeforeAll
	static void setUp()
	{
		QueryBuilderContext.defaultPrettyPrint = true;
	}

	@Test
	public void matchWithWith()
	{
		final var identifierGenerator = new IdentifierGenerator();

		final var personNode = new PersonNode()
			.withId("abc123")
			.named(identifierGenerator);

		final var movieNode = new MovieNode()
			.named(identifierGenerator);

		final var query = CypherBuilder.create()
			.match(Path.start(personNode).out(Relationships.ACTED_IN).to(movieNode).build())
			.with(new With().add(personNode).add(movieNode))
			.addReturn(personNode, movieNode)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person{id: $_v0_id})-[:ACTED_IN]->(m:Movie)
			WITH p, m
			RETURN p, m""", query.getQuery());

		Assertions.assertEquals("abc123", query.getParams().get("_v0_id"));
	}

	@Test
	public void matchWithWithAndCount()
	{
		final var identifierGenerator = new IdentifierGenerator();

		final var personNode = new PersonNode()
			.named(identifierGenerator);

		final var movieNode = new MovieNode()
			.named(identifierGenerator);

		final var movieCount = Count.of(movieNode).as("movieCount");

		final var query = CypherBuilder.create()
			.match(Path.start(personNode).out(Relationships.ACTED_IN).to(movieNode).build())
			.with(new With().add(personNode).add(movieCount))
			.addReturn(personNode, movieCount)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)-[:ACTED_IN]->(m:Movie)
			WITH p, COUNT(m) AS movieCount
			RETURN p, movieCount""", query.getQuery());

		Assertions.assertTrue(query.getParams().isEmpty());
	}
}
