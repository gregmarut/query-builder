package com.gregmarut.querybuilder.sdn.batch;

import com.gregmarut.querybuilder.cypher.CypherQuery;
import com.gregmarut.querybuilder.sdn.QueryExecutor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record BatchMergeQuery(CypherQuery query, String nodeLabel, int batchSize)
{
	public void execute(QueryExecutor queryExecutor)
	{
		log.trace("Batch merging {} {} nodes", batchSize, nodeLabel);
		queryExecutor.prepare(query).run();
	}
}
