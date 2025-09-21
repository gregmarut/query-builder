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

package com.gregmarut.querybuilder.cypher.node;

import com.gregmarut.querybuilder.cypher.CypherBuilder;
import com.gregmarut.querybuilder.cypher.CypherConstants;
import com.gregmarut.querybuilder.cypher.IdentifierGenerator;
import com.gregmarut.querybuilder.cypher.Property;
import com.gregmarut.querybuilder.cypher.QueryBuilderContext;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@Getter
public class MutableNode<N> extends LabeledNode<N>
{
	private final Map<String, Object> properties;
	
	@Getter
	private String idField;
	
	public MutableNode(final Class<N> nodeClass, final String label)
	{
		super(nodeClass, label);
		properties = new HashMap<>();
	}
	
	public MutableNode<N> named(final String identifier)
	{
		//validate that the identifier is valid
		if (!identifier.matches(CypherConstants.IDENTIFIER_REGEX))
		{
			throw new RuntimeException("Invalid identifier: " + identifier);
		}
		
		this.identifier = identifier;
		return this;
	}
	
	public MutableNode<N> named(final IdentifierGenerator generator)
	{
		this.identifier = generator.next();
		return this;
	}
	
	public MutableNode<N> withProperty(final String property, final Object value)
	{
		properties.put(property, value);
		return this;
	}
	
	protected MutableNode<N> setIdField(final String idField)
	{
		this.idField = idField;
		return this;
	}
	
	public Map<Property, Object> getMappedProperties()
	{
		final var props = new HashMap<Property, Object>();
		this.properties.forEach((key, value) -> props.put(getProperty(key), value));
		
		return props;
	}
	
	@Override
	public Stream<Map.Entry<String, Object>> getParameterStream(final QueryBuilderContext context)
	{
		return properties.entrySet()
			.stream()
			.filter(e -> null != e.getValue())
			.map(e -> Map.entry(buildVariable(context, e.getKey()), e.getValue()));
	}
	
	@Override
	protected void buildNode(final QueryBuilderContext context, final StringBuilder sb)
	{
		super.buildNode(context, sb);
		
		//decide which properties to use
		final var props = context.getFlags().contains(CypherConstants.FLAG_ID_PROPERTY_ONLY) ?
			Map.of(idField, Objects.requireNonNull(properties.get(idField), "ID field is not set.")) :
			properties;
		
		//check to see if there are any properties
		if (!props.isEmpty())
		{
			addInlineProperties(context, sb, props);
		}
	}
	
	private void addInlineProperties(final QueryBuilderContext context, final StringBuilder sb, final Map<String, Object> properties)
	{
		sb.append("{");
		
		final var nonNullPropertyValues = properties.entrySet()
			.stream()
			.filter(e -> null != e.getValue())
			.map(Map.Entry::getKey)
			.toList();
		
		//for all of the property keys
		boolean first = true;
		for (String propertyName : nonNullPropertyValues)
		{
			if (!first)
			{
				sb.append(", ");
			}
			
			sb.append(propertyName);
			sb.append(": ");
			sb.append(CypherBuilder.VARIABLE_PREFIX);
			sb.append(buildVariable(context, propertyName));
			
			first = false;
		}
		
		sb.append("}");
	}
}
