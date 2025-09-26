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

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class Relationship extends ReturnableCypherString implements Identifiable
{
	public static final String WILDCARD = "*";
	
	private final Map<String, Object> properties;
	private final String relationshipType;
	private String identifier;
	private boolean recursive;
	
	public Relationship(final String relationshipType)
	{
		this.relationshipType = relationshipType;
		this.properties = new HashMap<>();
	}
	
	@Override
	protected String _build(final QueryBuilderContext context)
	{
		final var inBuildableContext = isInBuildableContext(context);
		
		//check to see if this node is in a buildable context
		if (inBuildableContext)
		{
			final var sb = new StringBuilder();
			sb.append("[");
			
			//check to see if there is an identifier
			if (null != identifier)
			{
				sb.append(identifier);
			}
			
			//check to see if this node has already been built.
			if (isBuilt(context))
			{
				//do not allow a relationship to be built twice
				throw new IllegalStateException("Relationship cannot be built twice.");
			}
			else
			{
				//check to see if there is no identifier or relationship type (this is a wildcard)
				if (null == relationshipType)
				{
					sb.append(WILDCARD);
				}
				else
				{
					sb.append(":");
					sb.append(relationshipType);
					
					//check to see if this relationship is recursive
					if (recursive)
					{
						sb.append(WILDCARD);
					}
				}
				
				//decide which properties to use
				final var props = context.getFlags().contains(CypherConstants.FLAG_ID_PROPERTY_ONLY) ?
					Collections.<String, Object>emptyMap() :
					properties;
				
				//check to see if there are any properties
				if (!props.isEmpty())
				{
					addInlineProperties(context, sb, props);
				}
				
				context.built(this);
			}
			
			sb.append("]");
			
			return sb.toString();
		}
		//ensure that this relationship has previously been built
		else if (isBuilt(context))
		{
			return getRequiredIdentifier();
		}
		else
		{
			throw new IllegalStateException("Node cannot be used as it has not been built yet.");
		}
	}
	
	public Relationship named(final String identifier)
	{
		//validate that the identifier is valid
		if (!identifier.matches(CypherConstants.IDENTIFIER_REGEX))
		{
			throw new RuntimeException("Invalid identifier: " + identifier);
		}
		
		this.identifier = identifier;
		return this;
	}
	
	public Relationship named(final IdentifierGenerator generator)
	{
		this.identifier = generator.unique(getRelationshipType());
		return this;
	}
	
	public Relationship withProperty(final String property, final Object value)
	{
		if (null == value)
		{
			throw new IllegalArgumentException("value cannot be null.");
		}
		
		properties.put(property, value);
		return this;
	}
	
	public Relationship recursive()
	{
		this.recursive = true;
		return this;
	}
	
	private void addInlineProperties(final QueryBuilderContext context, final StringBuilder sb, final Map<String, Object> properties)
	{
		sb.append("{");
		
		//for all of the property keys
		boolean first = true;
		for (String propertyName : properties.keySet())
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
	
	/**
	 * Determines if this node is currently in a "buildable" context.
	 *
	 * @param context
	 * @return
	 */
	private boolean isInBuildableContext(final QueryBuilderContext context)
	{
		//get the parent class
		final var parent = context.getParentFromStack();
		return null == parent || parent == PartialPattern.class;
	}
	
	public Map<Property, Object> getMappedProperties()
	{
		return properties.entrySet()
			.stream()
			.collect(Collectors.toMap(
				k -> getProperty(k.getKey()),
				Map.Entry::getValue
			));
	}
	
	@Override
	public Stream<Map.Entry<String, Object>> getParameterStream(final QueryBuilderContext context)
	{
		return properties.entrySet().stream()
			.map(e -> Map.entry(buildVariable(context, e.getKey()), e.getValue()));
	}
	
	public static Relationship of(final String relationshipType)
	{
		return new Relationship(relationshipType);
	}
	
	public static Relationship of(final String relationshipType, @Nullable final IdentifierGenerator generator)
	{
		final var relationship = new Relationship(relationshipType);
		
		if (null != generator)
		{
			relationship.named(generator);
		}
		
		return relationship;
	}
	
	public static Relationship of(final String relationshipType, @Nullable final String identifier)
	{
		final var relationship = new Relationship(relationshipType);
		
		if (null != identifier)
		{
			relationship.named(identifier);
		}
		
		return relationship;
	}
	
	public static Relationship any()
	{
		return new Relationship(null);
	}
	
	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		if (!super.equals(o))
			return false;
		final Relationship that = (Relationship) o;
		return Objects.equals(relationshipType, that.relationshipType);
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(super.hashCode(), relationshipType);
	}
}
