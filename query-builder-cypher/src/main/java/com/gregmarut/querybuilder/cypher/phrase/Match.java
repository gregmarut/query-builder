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

import com.gregmarut.querybuilder.cypher.Path;
import com.gregmarut.querybuilder.cypher.QueryBuilderContext;
import com.gregmarut.querybuilder.cypher.condition.Condition;
import com.gregmarut.querybuilder.cypher.node.Node;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Match extends CypherPhrase
{
	private static final String KEYWORD_MATCH = "MATCH";
	private static final String KEYWORD_OPTIONAL = "OPTIONAL";
	public static final String KEYWORD_WHERE = "WHERE";
	
	@Getter
	private final Path path;
	private final List<Where> whereClauses;
	
	@Getter
	private boolean optional;
	
	public Match(final Node node)
	{
		this(new Path(node));
	}
	
	public Match(final Path path)
	{
		this.path = path;
		this.whereClauses = new ArrayList<>();
	}
	
	public Match(final Path path, final boolean optional)
	{
		this(path);
		setOptional(optional);
	}
	
	public Match(final Node node, final boolean optional)
	{
		this(node);
		setOptional(optional);
	}
	
	public Match setOptional(final boolean optional)
	{
		this.optional = optional;
		return this;
	}
	
	public Match where(final Where where)
	{
		this.whereClauses.add(where);
		return this;
	}
	
	public Match where(final Condition condition)
	{
		return where(new Where(condition));
	}
	
	@Override
	public Stream<Map.Entry<String, Object>> getParameterStream(final QueryBuilderContext context)
	{
		return Stream.of(path.getParameterStream(context),
				whereClauses.stream().flatMap(w -> w.getParameterStream(context)))
			.flatMap(s -> s);
	}
	
	@Override
	protected String _build(final QueryBuilderContext context)
	{
		final var sb = new StringBuilder();
		
		if (optional)
		{
			sb.append(KEYWORD_OPTIONAL);
			sb.append(" ");
		}
		
		sb.append(KEYWORD_MATCH);
		sb.append(" ");
		sb.append(path.build(context));
		
		if (!whereClauses.isEmpty())
		{
			sb.append(" ");
			sb.append(KEYWORD_WHERE);
			sb.append(" ");
			sb.append(whereClauses.stream().map(w -> w.build(context)).collect(Collectors.joining(" AND ")));
		}
		
		return sb.toString();
	}
	
	public static Match of(final Node node)
	{
		return new Match(node);
	}
	
	public static Match of(final Path path)
	{
		return new Match(path);
	}
	
	public static Match optional(final Node node)
	{
		return new Match(node, true);
	}
	
	public static Match optional(final Path path)
	{
		return new Match(path, true);
	}
}
