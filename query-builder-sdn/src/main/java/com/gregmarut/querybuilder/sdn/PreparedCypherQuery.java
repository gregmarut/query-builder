/*
 * Copyright 2025 Greg Marut
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.gregmarut.querybuilder.sdn;

import com.gregmarut.querybuilder.cypher.CypherQuery;
import com.gregmarut.querybuilder.sdn.mapping.MappingFunctionSupplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.data.neo4j.core.PreparedQuery;

@Slf4j
public class PreparedCypherQuery implements Runnable
{
	protected final CypherQuery cypherQuery;
	protected final Neo4jTemplate neo4jTemplate;
	protected final MappingFunctionSupplier mappingFunctionSupplier;
	
	public PreparedCypherQuery(final CypherQuery cypherQuery, final Neo4jTemplate neo4jTemplate,
		final MappingFunctionSupplier mappingFunctionSupplier)
	{
		this.cypherQuery = cypherQuery;
		this.neo4jTemplate = neo4jTemplate;
		this.mappingFunctionSupplier = mappingFunctionSupplier;
	}
	
	@Override
	public void run()
	{
		neo4jTemplate.toExecutableQuery(buildPreparedQuery(Void.class, mappingFunctionSupplier)).getSingleResult();
	}
	
	protected <T> PreparedQuery<T> buildPreparedQuery(final Class<T> clazz, final MappingFunctionSupplier mappingFunctionSupplier)
	{
		if (log.isTraceEnabled())
		{
			log.trace(cypherQuery.getQuery());
			log.trace("Params: {}", cypherQuery.getParams());
		}
		
		return PreparedQuery.queryFor(clazz)
			.withCypherQuery(cypherQuery.getQuery())
			.withParameters(PropertyMapper.map(cypherQuery.getParams()))
			.usingMappingFunction(mappingFunctionSupplier.getMappingFunction(clazz))
			.build();
	}
	
	@Override
	public String toString()
	{
		return cypherQuery.toString();
	}
}
