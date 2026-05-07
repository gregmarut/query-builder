package com.gregmarut.querybuilder.sdn.batch;

import com.gregmarut.querybuilder.cypher.CypherQuery;
import com.gregmarut.querybuilder.sdn.QueryExecutor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record BatchLinkQuery(CypherQuery query, String relationship, int batchSize)
{
	public void execute(QueryExecutor queryExecutor)
	{
		log.trace("Batch linking {} {} relationships", batchSize, relationship);
		queryExecutor.prepare(query).run();
	}
}
