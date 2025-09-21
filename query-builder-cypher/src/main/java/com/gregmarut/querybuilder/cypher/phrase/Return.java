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

package com.gregmarut.querybuilder.cypher.phrase;

import com.gregmarut.querybuilder.cypher.QueryBuilderContext;
import com.gregmarut.querybuilder.cypher.ReturnableCypherString;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class Return extends CypherPhrase
{
	public static final String KEYWORD_RETURN = "RETURN";
	public static final String KEYWORD_DISTINCT = "DISTINCT";
	
	private final List<ReturnableCypherString> returnValues;
	private final boolean distinct;
	
	@Override
	protected String _build(final QueryBuilderContext context)
	{
		if (returnValues.isEmpty())
		{
			throw new IllegalArgumentException("At least one return value must be specified.");
		}
		
		final var sb = new StringBuilder();
		sb.append(KEYWORD_RETURN);
		sb.append(" ");
		
		if (distinct)
		{
			sb.append(KEYWORD_DISTINCT);
			sb.append(" ");
		}
		
		sb.append(returnValues.stream().map(r -> r.build(context)).collect(Collectors.joining(", ")));
		
		return sb.toString();
	}
	
	@Override
	public Stream<Map.Entry<String, Object>> getParameterStream(final QueryBuilderContext context)
	{
		return returnValues.stream().flatMap(r -> r.getParameterStream(context));
	}
}
