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
import com.gregmarut.querybuilder.sdn.proxy.NodeProxyAccessor;
import org.springframework.data.neo4j.core.Neo4jTemplate;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class PreparedResultCypherQuery<T> extends PreparedCypherQuery
{
	private final Class<T> resultClass;
	
	public PreparedResultCypherQuery(final Class<T> resultClass, final CypherQuery cypherQuery, final Neo4jTemplate neo4jTemplate,
		final MappingFunctionSupplier mappingFunctionSupplier)
	{
		super(cypherQuery, neo4jTemplate, mappingFunctionSupplier);
		this.resultClass = resultClass;
	}
	
	public T get()
	{
		return proxyNodes(() -> neo4jTemplate.toExecutableQuery(buildPreparedQuery(resultClass, mappingFunctionSupplier)).getRequiredSingleResult());
	}
	
	public Optional<T> first()
	{
		return proxyNodes(() -> neo4jTemplate.toExecutableQuery(buildPreparedQuery(resultClass, mappingFunctionSupplier)).getSingleResult());
	}
	
	public Stream<T> list()
	{
		return proxyNodes(() -> neo4jTemplate.toExecutableQuery(buildPreparedQuery(resultClass, mappingFunctionSupplier)).getResults().stream());
	}
	
	private <R> R proxyNodes(final Supplier<R> function)
	{
		//enable all nodes returned by these results to be proxied
		final Set<Object> proxies = new HashSet<>();
		ThreadEnabledNodeProxyWrapper.PROXIES.set(proxies);
		
		//execute the query
		final var results = function.get();
		
		//take snapshots of all proxies that were created during the query execution
		proxies.stream().map(NodeProxyAccessor::extract).forEach(NodeProxyAccessor::snapshot);
		
		//return the results
		return results;
	}
}
