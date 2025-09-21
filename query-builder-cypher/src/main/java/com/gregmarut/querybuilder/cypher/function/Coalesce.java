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
import com.gregmarut.querybuilder.cypher.NodeType;
import com.gregmarut.querybuilder.cypher.QueryBuilderContext;
import com.gregmarut.querybuilder.cypher.node.TypedNode;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class Coalesce extends AliasableCypherString<Coalesce>
{
	private static final String KEYWORD_MAX = "COALESCE";
	
	private final CypherString cypherString;
	private final CypherString defaultValue;
	
	@Override
	protected String _build(final QueryBuilderContext context)
	{
		return KEYWORD_MAX + "(" + cypherString.build(context) + ", " + defaultValue.build(context) + ")";
	}
	
	@Override
	public Stream<Map.Entry<String, Object>> getParameterStream(final QueryBuilderContext context)
	{
		return Stream.concat(cypherString.getParameterStream(context), defaultValue.getParameterStream(context));
	}
	
	public <N> TypedNode<N> getAsNode(final Class<N> nodeClass, final String identifier)
	{
		return new TypedNode<N>(nodeClass, NodeType.YIELDED_NODE, identifier)
		{
			@Override
			public boolean isBuilt(final QueryBuilderContext context)
			{
				return context.isBuilt(Coalesce.this);
			}
		};
	}
	
	@Override
	protected Coalesce getThis()
	{
		return this;
	}
	
	public static Coalesce of(final CypherString cypherString, final CypherString defaultValue)
	{
		return new Coalesce(cypherString, defaultValue);
	}
}
