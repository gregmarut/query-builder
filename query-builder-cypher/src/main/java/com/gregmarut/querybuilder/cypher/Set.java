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

import com.gregmarut.querybuilder.cypher.phrase.CypherPhrase;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Set extends CypherPhrase
{
	private static final String KEYWORD_SET = "SET";
	
	private final Map<Property, CypherString> properties;
	
	public Set(final Map<Property, CypherString> properties)
	{
		if (properties == null || properties.isEmpty())
		{
			throw new IllegalArgumentException("properties cannot be null or empty");
		}
		
		this.properties = properties;
	}
	
	@Override
	public Stream<Map.Entry<String, Object>> getParameterStream(final QueryBuilderContext context)
	{
		return properties.values().stream().flatMap(p -> p.getParameterStream(context));
	}
	
	@Override
	protected String _build(final QueryBuilderContext context)
	{
		return KEYWORD_SET + " " + getWritePropertySet(context);
	}
	
	private String getWritePropertySet(final QueryBuilderContext context)
	{
		return this.properties.entrySet().stream()
			.sorted(Comparator.comparing(e -> e.getKey().getPropertyName()))
			.map(e -> e.getKey().build(context) + " = " + e.getValue().build(context))
			.collect(Collectors.joining(", "));
	}
}
