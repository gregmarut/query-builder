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
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class Duration extends AliasableCypherString<Duration>
{
	private static final String DURATION = "DURATION";
	
	private final Map<Unit, CypherString> map;
	
	@Override
	protected String _build(final QueryBuilderContext context)
	{
		final var sb = new StringBuilder();
		sb.append(DURATION);
		sb.append("({");
		
		//build the comma separated parameters
		final var params = map.entrySet().stream().map(entry -> entry.getKey().getKey() + ": " + entry.getValue().build(context)).toList();
		sb.append(String.join(", ", params));
		
		sb.append("})");
		
		return sb.toString();
	}
	
	@Override
	public Stream<Map.Entry<String, Object>> getParameterStream(final QueryBuilderContext context)
	{
		return map.values().stream().flatMap(value -> value.getParameterStream(context));
	}
	
	@Override
	protected Duration getThis()
	{
		return this;
	}
	
	public static Duration of(final Map<Unit, CypherString> map)
	{
		return new Duration(map);
	}
	
	@Getter
	@RequiredArgsConstructor
	public enum Unit
	{
		YEARS("years"),
		MONTHS("months"),
		WEEKS("weeks"),
		DAYS("days"),
		HOURS("hours"),
		MINUTES("minutes"),
		SECONDS("seconds"),
		MILLISECONDS("milliseconds"),
		MICROSECONDS("microseconds"),
		NANOSECONDS("nanoseconds");
		
		private final String key;
	}
}
