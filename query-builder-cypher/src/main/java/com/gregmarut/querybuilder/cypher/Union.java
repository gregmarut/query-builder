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

package com.gregmarut.querybuilder.cypher;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Union
{
	public static final String UNION = "UNION";
	
	public static <T> TypedCypherQuery<T> union(final Class<T> resultClass, final CypherBuilder... queries)
	{
		//holds the variable counter to use when building the queries
		final var variableCounter = new AtomicInteger();
		
		final var typedQueries = Arrays.stream(queries).map(q -> {
			//create the context to use for this query
			final var context = QueryBuilderContext.createDefault().setVariableCounter(variableCounter);
			return q.build(resultClass, context);
		}).toList();
		
		//combine the queries into a single query with a UNION between each one
		final var statementSeparator = QueryBuilderContext.createDefault().getStatementSeparator();
		final var cypher =
			typedQueries.stream().map(CypherQuery::getQuery).collect(Collectors.joining(statementSeparator + UNION + statementSeparator));
		
		//merge the parameters from all of the queries
		final var params = typedQueries.stream().map(CypherQuery::getParams).flatMap(m -> m.entrySet().stream()).collect(Collectors.toMap(
			Map.Entry::getKey, Map.Entry::getValue));
		
		return new TypedCypherQuery<>(new CypherQuery(cypher, params), resultClass);
	}
}
