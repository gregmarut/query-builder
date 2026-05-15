package com.gregmarut.querybuilder.cypher;

import com.gregmarut.querybuilder.cypher.model.GenreNode;
import com.gregmarut.querybuilder.cypher.model.MovieNode;
import com.gregmarut.querybuilder.cypher.model.PersonNode;
import com.gregmarut.querybuilder.cypher.model.Relationships;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class PathTest
{
	@BeforeAll
	static void setUp()
	{
		QueryBuilderContext.defaultPrettyPrint = true;
	}

	@Test
	public void incomingPath()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);
		final var movieNode = new MovieNode().named(identifierGenerator);

		final var query = CypherBuilder.create()
			.match(Path.start(movieNode).in(Relationships.ACTED_IN).to(personNode).build())
			.addReturn(personNode, movieNode)
			.build();

		Assertions.assertEquals("""
			MATCH (m:Movie)<-[:ACTED_IN]-(p:Person)
			RETURN p, m""", query.getQuery());
		Assertions.assertTrue(query.getParams().isEmpty());
	}

	@Test
	public void undirectedPath()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var person1 = new PersonNode().named(identifierGenerator);
		final var person2 = new PersonNode().named(identifierGenerator);

		final var query = CypherBuilder.create()
			.match(Path.start(person1).direction(Direction.UNDIRECTED).relationship(Relationships.FOLLOWS).to(person2).build())
			.addReturn(person1, person2)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)-[:FOLLOWS]-(pe:Person)
			RETURN p, pe""", query.getQuery());
		Assertions.assertTrue(query.getParams().isEmpty());
	}

	@Test
	public void multiHopPath()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);
		final var movieNode = new MovieNode().named(identifierGenerator);
		final var genreNode = new GenreNode().named(identifierGenerator);

		//chain multiple hops in a single path
		final var query = CypherBuilder.create()
			.match(Path.start(personNode)
				.out(Relationships.ACTED_IN).to(movieNode)
				.out(Relationships.IN_GENRE).to(genreNode)
				.build())
			.addReturn(personNode, genreNode)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)-[:ACTED_IN]->(m:Movie)-[:IN_GENRE]->(g:Genre)
			RETURN p, g""", query.getQuery());
		Assertions.assertTrue(query.getParams().isEmpty());
	}

	@Test
	public void cyclicalPathThrows()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);
		final var movieNode = new MovieNode().named(identifierGenerator);

		//attempt to revisit a node already on the path - this is cyclical
		final var path = Path.start(personNode)
			.out(Relationships.ACTED_IN).to(movieNode)
			.in(Relationships.DIRECTED).to(personNode)
			.build();

		final var builder = CypherBuilder.create()
			.match(path)
			.addReturn(personNode);

		Assertions.assertThrows(RuntimeException.class, builder::build);
	}

	@Test
	public void relationshipWithIdentifierAndProperties()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);
		final var movieNode = new MovieNode().named(identifierGenerator);

		final var actedIn = Relationship.of(Relationships.ACTED_IN, identifierGenerator)
			.withProperty(Relationships.ACTED_IN_ROLES, "Neo");

		final var query = CypherBuilder.create()
			.match(Path.start(personNode).out(actedIn).to(movieNode).build())
			.addReturn(actedIn)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)-[a:ACTED_IN{roles: $_v0_roles}]->(m:Movie)
			RETURN a""", query.getQuery());
		Assertions.assertEquals("Neo", query.getParams().get("_v0_roles"));
	}

	@Test
	public void recursiveRelationship()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var person1 = new PersonNode().named(identifierGenerator);
		final var person2 = new PersonNode().named(identifierGenerator);

		final var followsRecursive = Relationship.of(Relationships.FOLLOWS).recursive();

		final var query = CypherBuilder.create()
			.match(Path.start(person1).out(followsRecursive).to(person2).build())
			.addReturn(person1, person2)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)-[:FOLLOWS*]->(pe:Person)
			RETURN p, pe""", query.getQuery());
		Assertions.assertTrue(query.getParams().isEmpty());
	}

	@Test
	public void anyRelationshipRendersAsWildcard()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);
		final var movieNode = new MovieNode().named(identifierGenerator);

		final var query = CypherBuilder.create()
			.match(Path.start(personNode).out(Relationship.any()).to(movieNode).build())
			.addReturn(personNode, movieNode)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person)-[*]->(m:Movie)
			RETURN p, m""", query.getQuery());
		Assertions.assertTrue(query.getParams().isEmpty());
	}

	@Test
	public void relationshipNamedRejectsInvalidIdentifier()
	{
		final var rel = Relationship.of(Relationships.ACTED_IN);
		//identifier regex disallows special characters
		Assertions.assertThrows(RuntimeException.class, () -> rel.named("has-dash"));
	}
}
