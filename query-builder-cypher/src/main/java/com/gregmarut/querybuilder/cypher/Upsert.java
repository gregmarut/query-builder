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

import com.gregmarut.querybuilder.cypher.node.MutableNode;
import com.gregmarut.querybuilder.cypher.phrase.CypherPhrase;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The Upsert class represents a MERGE CypherPhrase with an optional SET CypherPhrase that will either create or update a node/relationship
 * in the graph.
 */
public class Upsert extends CypherPhrase
{
	private final Merge merge;
	
	@Nullable
	private final com.gregmarut.querybuilder.cypher.Set set;
	
	public Upsert(final MutableNode<?> node)
	{
		this.merge = new Merge(node);
		
		//get all of the properties except the id property
		this.set = toSet(node.getMappedProperties()
			.entrySet()
			.stream()
			.filter(e -> !e.getKey().getPropertyName().equals(node.getIdField())));
	}
	
	public Upsert(final Path path, final Map<Property, Object> properties)
	{
		this.merge = new Merge(path);
		this.set = toSet(properties.entrySet().stream());
	}
	
	@Override
	public Stream<Map.Entry<String, Object>> getParameterStream(final QueryBuilderContext context)
	{
		if (null == set)
		{
			return merge.getParameterStream(context);
		}
		else
		{
			return Stream.concat(merge.getParameterStream(context), set.getParameterStream(context));
		}
	}
	
	@Override
	protected String _build(final QueryBuilderContext context)
	{
		StringBuilder sb = new StringBuilder();
		
		//write the merge statement but when writing this node, only include the id property
		sb.append(this.merge.build(context, Set.of(CypherConstants.FLAG_ID_PROPERTY_ONLY)));
		
		if (null != this.set)
		{
			sb.append(context.getStatementSeparator());
			sb.append(this.set.build(context));
		}
		
		return sb.toString();
	}
	
	@Nullable
	private com.gregmarut.querybuilder.cypher.Set toSet(final Stream<Map.Entry<Property, Object>> stream)
	{
		final var properties = stream.collect(Collectors.toMap(Map.Entry::getKey, e -> {
			if (null == e.getValue())
			{
				return LiteralCypherString.NULL;
			}
			else
			{
				return Variable.of(e.getValue());
			}
		}));
		
		if (properties.isEmpty())
		{
			return null;
		}
		else
		{
			return new com.gregmarut.querybuilder.cypher.Set(properties);
		}
	}
}
