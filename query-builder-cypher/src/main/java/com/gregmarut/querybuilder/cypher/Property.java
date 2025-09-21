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

import lombok.Getter;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class Property extends AliasableCypherString<Property>
{
	public static final String REGEX = "^[a-zA-Z][a-zA-Z0-9_]*$";
	private final Identifiable item;
	
	@Getter
	private final String propertyName;
	
	public Property(final Identifiable item, final String propertyName)
	{
		if (!propertyName.matches(REGEX))
		{
			throw new IllegalArgumentException("Invalid property name: " + propertyName);
		}
		
		this.item = item;
		this.propertyName = propertyName;
	}
	
	@Override
	protected String _build(final QueryBuilderContext context)
	{
		return item.getRequiredIdentifier() + "." + propertyName;
	}
	
	public String buildVariable(final QueryBuilderContext context)
	{
		return context.getVariableName(item) + "_" + propertyName;
	}
	
	public CypherString getSubProperty(final String subPropertyName)
	{
		return new CypherString()
		{
			@Override
			public Stream<Map.Entry<String, Object>> getParameterStream(final QueryBuilderContext context)
			{
				return Property.this.getParameterStream(context);
			}
			
			@Override
			protected String _build(final QueryBuilderContext context)
			{
				return Property.this._build(context) + "." + subPropertyName;
			}
		};
	}
	
	@Override
	protected Property getThis()
	{
		return this;
	}
	
	@Override
	public Stream<Map.Entry<String, Object>> getParameterStream(final QueryBuilderContext context)
	{
		return Stream.empty();
	}
	
	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		final Property property = (Property) o;
		return Objects.equals(item.getRequiredIdentifier(), property.item.getRequiredIdentifier()) && Objects.equals(propertyName,
			property.propertyName);
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(item.getRequiredIdentifier(), propertyName);
	}
}
