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

import com.gregmarut.querybuilder.cypher.Alias;
import com.gregmarut.querybuilder.cypher.AliasedCypherString;
import com.gregmarut.querybuilder.cypher.Case;
import com.gregmarut.querybuilder.cypher.CypherString;
import com.gregmarut.querybuilder.cypher.LiteralCypherString;
import com.gregmarut.querybuilder.cypher.NodeType;
import com.gregmarut.querybuilder.cypher.QueryBuilderContext;
import com.gregmarut.querybuilder.cypher.condition.GreaterThanCondition;
import com.gregmarut.querybuilder.cypher.function.Collect;
import com.gregmarut.querybuilder.cypher.function.Size;
import com.gregmarut.querybuilder.cypher.node.Node;
import com.gregmarut.querybuilder.cypher.node.TypedNode;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class Unwind<E extends CypherString> extends CypherPhrase
{
	private static final String KEYWORD_UNWIND = "UNWIND";
	
	private final List<E> cypherStrings;
	private final Alias alias;
	
	public Unwind(final E cypherStrings, final String alias)
	{
		this.cypherStrings = List.of(cypherStrings);
		this.alias = Alias.of(alias);
	}
	
	public Unwind(final List<E> cypherStrings, final String alias)
	{
		this.cypherStrings = cypherStrings;
		this.alias = Alias.of(alias);
	}
	
	@Override
	protected String _build(final QueryBuilderContext context)
	{
		return KEYWORD_UNWIND + " " + cypherStrings.stream().map(c -> c.build(context)).collect(Collectors.joining(" + ")) + " AS " +
			   alias.build(context);
	}
	
	@Override
	public Stream<Map.Entry<String, Object>> getParameterStream(final QueryBuilderContext context)
	{
		return cypherStrings.stream().flatMap(c -> c.getParameterStream(context));
	}
	
	public <N> TypedNode<N> getAsNode(final Class<N> nodeClass)
	{
		return new TypedNode<>(nodeClass, NodeType.YIELDED_NODE, alias.getAlias())
		{
			@Override
			public boolean isBuilt(final QueryBuilderContext context)
			{
				return context.isBuilt(Unwind.this);
			}
		};
	}
	
	/**
	 * Generates an optional UNWIND operation for a given collection with a specific alias.
	 * If the size of the collection is greater than zero, it returns the collection.
	 * Otherwise, it returns a singleton list with a null value.
	 *
	 * @param collection The collection to be unwound, wrapped in an AliasedCypherString.
	 * @param alias      The alias to be used for the unwound elements.
	 * @return An Unwind instance with the generated UNWIND statement.
	 */
	public static Unwind<Case> optional(final AliasedCypherString<Collect<Node>> collection, final String alias)
	{
		final var c = Case.builder()
			.whenThen(new GreaterThanCondition(Size.of(collection), 0), collection)
			.elseReturn(LiteralCypherString.of("[null]"))
			.build();
		return new Unwind<>(c, alias);
	}
}
