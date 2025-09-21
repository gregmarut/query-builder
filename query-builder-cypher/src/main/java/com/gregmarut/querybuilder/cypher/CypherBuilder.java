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

import com.gregmarut.querybuilder.cypher.condition.Condition;
import com.gregmarut.querybuilder.cypher.node.Node;
import com.gregmarut.querybuilder.cypher.phrase.CypherPhrase;
import com.gregmarut.querybuilder.cypher.phrase.Delete;
import com.gregmarut.querybuilder.cypher.phrase.Match;
import com.gregmarut.querybuilder.cypher.phrase.Remove;
import com.gregmarut.querybuilder.cypher.phrase.Return;
import com.gregmarut.querybuilder.cypher.phrase.Unwind;
import com.gregmarut.querybuilder.cypher.phrase.UsingIndex;
import com.gregmarut.querybuilder.cypher.phrase.Where;
import com.gregmarut.querybuilder.cypher.phrase.With;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Accessors(chain = true)
public class CypherBuilder
{
	public static final String KEYWORD_ORDER_BY = "ORDER BY";
	public static final String KEYWORD_SKIP = "SKIP";
	public static final String KEYWORD_LIMIT = "LIMIT";
	
	public static final String VARIABLE_PREFIX = "$";
	
	@Getter
	private final List<Supplier<? extends CypherPhrase>> cypherPhrases;
	private final List<ReturnableCypherString> returnValues;
	private final List<OrderBy> orderByList;
	
	@Getter
	private Integer limit;
	
	@Getter
	private Integer skip;
	
	private boolean distinct;
	
	public CypherBuilder()
	{
		this.cypherPhrases = new ArrayList<>();
		this.returnValues = new ArrayList<>();
		this.orderByList = new ArrayList<>();
	}
	
	public CypherBuilder call(final Call call)
	{
		this.cypherPhrases.add(() -> call);
		return this;
	}
	
	public CypherBuilder match(final Supplier<Match> matchSupplier)
	{
		this.cypherPhrases.add(matchSupplier);
		return this;
	}
	
	public CypherBuilder match(final Match match)
	{
		this.cypherPhrases.add(() -> match);
		return this;
	}
	
	public CypherBuilder match(final Path path)
	{
		return match(Match.of(path));
	}
	
	public CypherBuilder match(final List<Match> match)
	{
		match.forEach(m -> this.cypherPhrases.add(() -> m));
		return this;
	}
	
	public CypherBuilder match(final Node node)
	{
		return match(new Match(node));
	}
	
	public CypherBuilder usingIndex(final UsingIndex usingIndex)
	{
		this.cypherPhrases.add(() -> usingIndex);
		return this;
	}
	
	public CypherBuilder upsert(final Upsert upsert)
	{
		this.cypherPhrases.add(() -> upsert);
		return this;
	}
	
	public CypherBuilder merge(final Merge merge)
	{
		this.cypherPhrases.add(() -> merge);
		return this;
	}
	
	public CypherBuilder merge(final Collection<Merge> merge)
	{
		merge.forEach(m -> this.cypherPhrases.add(() -> m));
		return this;
	}
	
	public CypherBuilder set(final Set set)
	{
		this.cypherPhrases.add(() -> set);
		return this;
	}
	
	public CypherBuilder remove(final Remove remove)
	{
		this.cypherPhrases.add(() -> remove);
		return this;
	}
	
	public CypherBuilder where(final Condition condition)
	{
		return where(Where.from(condition));
	}
	
	public CypherBuilder where(final Where where)
	{
		this.cypherPhrases.add(() -> where);
		return this;
	}
	
	public CypherBuilder with(final Supplier<With> withSupplier)
	{
		this.cypherPhrases.add(withSupplier);
		return this;
	}
	
	public CypherBuilder with(final With with)
	{
		return with(() -> with);
	}
	
	public CypherBuilder unwind(final Unwind<?> unwind)
	{
		this.cypherPhrases.add(() -> unwind);
		return this;
	}
	
	public CypherBuilder addReturn(final String... returnValues)
	{
		this.returnValues.addAll(Arrays.stream(returnValues).map(LiteralCypherString::of).toList());
		return this;
	}
	
	public CypherBuilder addReturn(final ReturnableCypherString... returnValues)
	{
		this.returnValues.addAll(Arrays.asList(returnValues));
		return this;
	}
	
	public CypherBuilder distinct()
	{
		this.distinct = true;
		return this;
	}
	
	public CypherBuilder delete(final Delete delete)
	{
		this.cypherPhrases.add(() -> delete);
		return this;
	}
	
	public CypherBuilder orderBy(final Property property)
	{
		return orderBy(property, OrderType.ASC);
	}
	
	public CypherBuilder orderBy(final Property property, final OrderType orderType)
	{
		this.orderByList.add(new OrderBy(property, orderType));
		return this;
	}
	
	public CypherBuilder orderBy(final String value)
	{
		return orderBy(value, OrderType.ASC);
	}
	
	public CypherBuilder orderBy(final String value, final OrderType orderType)
	{
		this.orderByList.add(new OrderBy(value, orderType));
		return this;
	}
	
	public CypherBuilder orderBy(final Identifiable identifiable, final OrderType orderType)
	{
		this.orderByList.add(new OrderBy(identifiable.getRequiredIdentifier(), orderType));
		return this;
	}
	
	public CypherBuilder limit(final Integer limit)
	{
		this.limit = limit;
		return this;
	}
	
	public CypherBuilder skip(final Integer skip)
	{
		this.skip = skip;
		return this;
	}
	
	public <T> TypedCypherQuery<T> build(final Class<T> resultClass)
	{
		return new TypedCypherQuery<>(build(QueryBuilderContext.createDefault()), resultClass);
	}
	
	public <T> TypedCypherQuery<T> build(final Class<T> resultClass, final QueryBuilderContext context)
	{
		return new TypedCypherQuery<>(build(context), resultClass);
	}
	
	public CypherQuery build()
	{
		return build(QueryBuilderContext.createDefault());
	}
	
	public CypherQuery build(final QueryBuilderContext context)
	{
		final var sb = new StringBuilder();
		
		final Map<String, Object> params = new HashMap<String, Object>();
		
		//for each of the groups
		cypherPhrases.stream().map(Supplier::get).filter(Objects::nonNull).forEach(cypherPhrase -> {
			sb.append(cypherPhrase.build(context));
			addIfNeeded(sb, context.getStatementSeparator());
			
			cypherPhrase.getParameterStream(context).forEach(e -> params.put(e.getKey(), e.getValue()));
		});
		
		if (!returnValues.isEmpty())
		{
			addIfNeeded(sb, context.getStatementSeparator());
			
			final var returnPhrase = new Return(returnValues, distinct);
			
			sb.append(returnPhrase.build(context));
			returnPhrase.getParameterStream(context).forEach(e -> params.put(e.getKey(), e.getValue()));
		}
		
		//check to see if there is an order by set
		if (!orderByList.isEmpty())
		{
			addIfNeeded(sb, context.getStatementSeparator());
			sb.append(KEYWORD_ORDER_BY);
			sb.append(" ");
			
			sb.append(orderByList.stream().map(OrderBy::toString).collect(Collectors.joining(", ")));
		}
		
		//check to see if there is a skip
		if (null != skip)
		{
			addIfNeeded(sb, context.getStatementSeparator());
			sb.append(KEYWORD_SKIP);
			sb.append(" ");
			sb.append(skip);
		}
		
		//check to see if there is a limit
		if (null != limit)
		{
			addIfNeeded(sb, context.getStatementSeparator());
			sb.append(KEYWORD_LIMIT);
			sb.append(" ");
			sb.append(limit);
		}
		
		return new CypherQuery(sb.toString(), params);
	}
	
	/**
	 * Adds a character to the string builder if it does not already exist
	 *
	 * @param sb
	 */
	private void addIfNeeded(final StringBuilder sb, final char c)
	{
		if (sb.isEmpty() || sb.charAt(sb.length() - 1) != c)
		{
			sb.append(c);
		}
	}
	
	public static CypherBuilder create()
	{
		return new CypherBuilder();
	}
}
