package com.gregmarut.querybuilder.sdn.batch;

import com.gregmarut.querybuilder.cypher.QueryBuilderContext;
import com.gregmarut.querybuilder.sdn.model.MovieNode;
import com.gregmarut.querybuilder.sdn.model.PersonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

class BatchQueryBuilderTest
{
	@BeforeAll
	static void setUp()
	{
		QueryBuilderContext.defaultPrettyPrint = true;
	}
	
	@Test
	void mergeAll()
	{
		final var personNodes = List.of(
			new PersonNode("1", "Greg", 1989, "greg@example.com"),
			new PersonNode("2", "John", 1976, "john@example.com"),
			new PersonNode("3", "Jane", 2002, "jane@example.com")
		);
		
		final var batchMergeQueries = BatchQueryBuilder.buildBatchMergeQueries(personNodes);
		
		Assertions.assertEquals(1, batchMergeQueries.size());
		
		final var query = batchMergeQueries.getFirst().query();
		Assertions.assertEquals(1, query.getParams().size());
		
		Assertions.assertEquals("""
			UNWIND $_v0 AS row
			MERGE (n:Person{id: row.id})
			SET n += row""", batchMergeQueries.getFirst().query().getQuery());
	}
	
	@Test
	void linkAll()
	{
		final var personNode1 = new PersonNode("1", "Greg", 1989, "greg@example.com");
		
		final var movie1 = new MovieNode("1", "Movie 1");
		final var movie2 = new MovieNode("2", "Movie 2");
		final var movie3 = new MovieNode("3", "Movie 3");
		
		personNode1.setActedInMovies(List.of(movie1, movie2, movie3));
		
		final var batchLinkQueries = BatchQueryBuilder.buildBatchLinkQueries(List.of(personNode1));
		
		Assertions.assertEquals(1, batchLinkQueries.size());
		Assertions.assertEquals(3, batchLinkQueries.getFirst().batchSize());
		
		final var query = batchLinkQueries.getFirst().query();
		Assertions.assertEquals(1, query.getParams().size());
		
		Assertions.assertEquals("""
			UNWIND $_v0 AS row
			MATCH (a:Movie{id: row.fromId})
			MATCH (b:Person{id: row.toId})
			MERGE (b)-[:ACTED_IN]->(a)""", batchLinkQueries.getFirst().query().getQuery());
	}
}
