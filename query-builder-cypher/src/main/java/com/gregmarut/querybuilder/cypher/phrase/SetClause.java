/*
 * Copyright 2026 Greg Marut
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

import com.gregmarut.querybuilder.cypher.CypherString;
import com.gregmarut.querybuilder.cypher.Property;
import com.gregmarut.querybuilder.cypher.QueryBuilderContext;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Renders a SET clause that assigns property values, e.g. {@code SET n.name = $_v0, n.age = $_v1}.
 * Property assignments are emitted in alphabetical order by property name for deterministic output.
 *
 * Named SetClause (rather than Set) to avoid colliding with {@link java.util.Set} at every call site.
 */
public class SetClause extends CypherPhrase
{
	private static final String KEYWORD_SET = "SET";

	private final Map<Property, CypherString> properties;

	/**
	 * Constructs a SetClause for the given property-to-value assignments.
	 *
	 * @param properties the assignments to render; must be non-null and non-empty
	 * @throws IllegalArgumentException if {@code properties} is null or empty
	 */
	public SetClause(final Map<Property, CypherString> properties)
	{
		if (null == properties || properties.isEmpty())
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
		return SetClause.KEYWORD_SET + " " + getWritePropertySet(context);
	}

	/**
	 * Renders the comma-separated property assignments, sorted alphabetically by property name.
	 *
	 * @param context the build context
	 * @return a string like {@code n.born = $_v0, n.name = $_v1}
	 */
	private String getWritePropertySet(final QueryBuilderContext context)
	{
		return this.properties.entrySet().stream()
			.sorted(Comparator.comparing(e -> e.getKey().getPropertyName()))
			.map(e -> e.getKey().build(context) + " = " + e.getValue().build(context))
			.collect(Collectors.joining(", "));
	}
}
