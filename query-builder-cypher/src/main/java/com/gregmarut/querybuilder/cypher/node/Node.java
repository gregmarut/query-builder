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

import com.gregmarut.querybuilder.cypher.Identifiable;
import com.gregmarut.querybuilder.cypher.IdentifierGenerator;
import com.gregmarut.querybuilder.cypher.Merge;
import com.gregmarut.querybuilder.cypher.NodeType;
import com.gregmarut.querybuilder.cypher.PartialPattern;
import com.gregmarut.querybuilder.cypher.Path;
import com.gregmarut.querybuilder.cypher.QueryBuilderContext;
import com.gregmarut.querybuilder.cypher.ReturnableCypherString;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class Node extends ReturnableCypherString implements Identifiable
{
	private final NodeType nodeType;
	protected String identifier;
	
	@Override
	protected final String _build(final QueryBuilderContext context)
	{
		//check to see if this node is in a buildable context
		if (isInBuildableContext(context))
		{
			final var sb = new StringBuilder();
			sb.append("(");
			
			//check to see if this node has already been built
			if (isBuilt(context))
			{
				//reference this node going forward with the identifier only
				sb.append(getRequiredIdentifier());
			}
			else
			{
				//check to see if there is an identifier
				if (null != getIdentifier())
				{
					sb.append(getIdentifier());
				}
				
				buildNode(context, sb);
				context.built(this);
			}
			
			sb.append(")");
			return sb.toString();
		}
		//ensure that this node has previously been built
		else if (nodeType == NodeType.YIELDED_NODE || isBuilt(context))
		{
			return getRequiredIdentifier();
		}
		else
		{
			throw new IllegalStateException("Node cannot be used as it has not been built yet: " + this);
		}
	}
	
	protected void buildNode(final QueryBuilderContext context, final StringBuilder sb)
	{
	
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
		return null == parent || parent == Path.class || parent == PartialPattern.class || parent == Merge.class;
	}
	
	@Override
	public Stream<Map.Entry<String, Object>> getParameterStream(final QueryBuilderContext context)
	{
		return Stream.empty();
	}
	
	public static Node identifiedAs(final IdentifierGenerator generator)
	{
		return new Node(NodeType.NODE, generator.next());
	}
}
