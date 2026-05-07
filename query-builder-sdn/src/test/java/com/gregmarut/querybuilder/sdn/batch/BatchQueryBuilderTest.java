package com.gregmarut.querybuilder.sdn.batch;

import com.gregmarut.querybuilder.cypher.QueryBuilderContext;
import com.gregmarut.querybuilder.sdn.model.PersonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
		final var userNodes = List.of(
			new PersonNode("1", "Greg", 1989, "greg@example.com"),
			new PersonNode("2", "John", 1976, "john@example.com"),
			new PersonNode("3", "Jane", 2002, "jane@example.com")
		);
		
		final var batchMerges = BatchQueryBuilder.buildBatchMergeQueries(userNodes);
		
		assertEquals(1, batchMerges.size());
		
		Assertions.assertEquals("""
			UNWIND $_v0 AS row
			MERGE (n:Person{id: row.id})
			SET n += row""", batchMerges.getFirst().query().getQuery());
	}
}
