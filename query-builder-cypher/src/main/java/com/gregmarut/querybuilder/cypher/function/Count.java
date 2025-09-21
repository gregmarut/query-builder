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

package com.gregmarut.querybuilder.cypher.function;

import com.gregmarut.querybuilder.cypher.AliasableCypherString;
import com.gregmarut.querybuilder.cypher.CypherString;
import com.gregmarut.querybuilder.cypher.QueryBuilderContext;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class Count extends AliasableCypherString<Count>
{
	private static final String COUNT = "COUNT";
	private static final String DISTINCT = "DISTINCT";
	
	private final CypherString cypherString;
	private final boolean distinct;
	
	@Override
	protected String _build(final QueryBuilderContext context)
	{
		final var sb = new StringBuilder();
		sb.append(COUNT);
		sb.append("(");
		
		if (distinct)
		{
			sb.append(DISTINCT);
			sb.append(" ");
		}
		
		sb.append(cypherString.build(context));
		
		sb.append(")");
		
		return sb.toString();
	}
	
	@Override
	public Stream<Map.Entry<String, Object>> getParameterStream(final QueryBuilderContext context)
	{
		return cypherString.getParameterStream(context);
	}
	
	@Override
	protected Count getThis()
	{
		return this;
	}
	
	public static Count of(final CypherString cypherString)
	{
		return new Count(cypherString, false);
	}
	
	public static Count distinct(final CypherString cypherString)
	{
		return new Count(cypherString, true);
	}
}
