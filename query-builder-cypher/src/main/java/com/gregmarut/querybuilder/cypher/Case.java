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

import lombok.Builder;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Builder
@RequiredArgsConstructor
public class Case extends CypherString
{
	private static final String KEYWORD_CASE = "CASE";
	private static final String KEYWORD_ELSE = "ELSE";
	private static final String KEYWORD_END = "END";
	
	private final List<WhenThen> whenThens;
	
	@Nullable
	private final String property;
	
	@Nullable
	private final CypherString elseReturn;
	
	@Override
	public final String _build(final QueryBuilderContext context)
	{
		if (whenThens.isEmpty())
		{
			throw new IllegalStateException("At least one WHEN-THEN pair is required.");
		}
		
		final var sb = new StringBuilder();
		sb.append(KEYWORD_CASE);
		
		if (property != null)
		{
			sb.append(" ");
			sb.append(property);
		}
		
		sb.append(context.getStatementSeparator());
		sb.append(whenThens.stream().map(wt -> context.getIndentCharacter() + wt.build(context))
			.collect(Collectors.joining(Character.toString(context.getStatementSeparator()))));
		
		if (null != elseReturn)
		{
			sb.append(context.getStatementSeparator());
			sb.append(context.getIndentCharacter());
			sb.append(KEYWORD_ELSE);
			sb.append(" ");
			sb.append(elseReturn.build(context));
		}
		
		sb.append(context.getStatementSeparator());
		sb.append(KEYWORD_END);
		
		return sb.toString();
	}
	
	@Override
	public Stream<Map.Entry<String, Object>> getParameterStream(final QueryBuilderContext context)
	{
		if (null != elseReturn)
		{
			return Stream.concat(whenThens.stream().flatMap(wt -> wt.getParameterStream(context)), elseReturn.getParameterStream(context));
		}
		else
		{
			return whenThens.stream().flatMap(wt -> wt.getParameterStream(context));
		}
	}
	
	public static class CaseBuilder
	{
		public CaseBuilder()
		{
			this.whenThens = new ArrayList<>();
		}
		
		public CaseBuilder whenThen(final CypherString when, final CypherString then)
		{
			this.whenThens.add(new WhenThen(when, then));
			return this;
		}
		
		public WhenBuilder when(final CypherString when)
		{
			return new WhenBuilder(this, when);
		}
		
		@RequiredArgsConstructor
		public static class WhenBuilder
		{
			private final CaseBuilder caseBuilder;
			private final CypherString when;
			
			public CaseBuilder then(final CypherString then)
			{
				caseBuilder.whenThens.add(new WhenThen(when, then));
				return caseBuilder;
			}
		}
	}
}
