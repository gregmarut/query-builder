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
public class Collect<E extends CypherString> extends AliasableCypherString<Collect<E>>
{
	private static final String KEYWORD_COLLECT = "COLLECT";
	private static final String KEYWORD_DISTINCT = "DISTINCT";
	
	private final E cypherString;
	private final boolean distinct;
	
	@Override
	protected String _build(final QueryBuilderContext context)
	{
		final var sb = new StringBuilder();
		sb.append(KEYWORD_COLLECT);
		sb.append("(");
		
		if (distinct)
		{
			sb.append(KEYWORD_DISTINCT);
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
	protected Collect<E> getThis()
	{
		return this;
	}
	
	public static <E extends CypherString> Collect<E> of(final E cypherString)
	{
		return new Collect<>(cypherString, false);
	}
	
	public static <E extends CypherString> Collect<E> distinct(final E cypherString)
	{
		return new Collect<>(cypherString, true);
	}
}
