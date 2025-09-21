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

import com.gregmarut.querybuilder.cypher.Property;
import com.gregmarut.querybuilder.cypher.QueryBuilderContext;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class Remove extends CypherPhrase
{
	private static final String KEYWORD_REMOVE = "REMOVE";
	
	private final List<Property> properties;
	
	@Override
	protected String _build(final QueryBuilderContext context)
	{
		return KEYWORD_REMOVE + " " + properties.stream().map(p -> p.build(context)).collect(Collectors.joining(", "));
	}
	
	@Override
	public Stream<Map.Entry<String, Object>> getParameterStream(final QueryBuilderContext context)
	{
		return properties.stream().flatMap(p -> p.getParameterStream(context));
	}
	
	public static Remove of(final Property... properties)
	{
		if (null == properties)
		{
			throw new IllegalArgumentException("properties cannot be null");
		}
		
		return of(List.of(properties));
	}
	
	public static Remove of(final List<Property> properties)
	{
		return new Remove(properties);
	}
}
