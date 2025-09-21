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

import com.gregmarut.querybuilder.cypher.QueryBuilderContext;
import com.gregmarut.querybuilder.cypher.condition.AndCondition;
import com.gregmarut.querybuilder.cypher.condition.Condition;
import com.gregmarut.querybuilder.cypher.condition.OrCondition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class Where extends CypherPhrase
{
	public static final String KEYWORD_WHERE = "WHERE";
	
	private final List<Condition> conditions;
	
	public Where(final Condition... conditions)
	{
		if (null == conditions)
		{
			throw new IllegalArgumentException("conditions cannot be null");
		}
		
		this.conditions = new ArrayList<>();
		this.conditions.addAll(Arrays.asList(conditions));
	}
	
	public Where(final List<Condition> conditions)
	{
		this.conditions = conditions;
	}
	
	@Override
	protected String _build(final QueryBuilderContext context)
	{
		if (conditions.isEmpty())
		{
			throw new IllegalStateException("No conditions have been added to this where clause.");
		}
		
		final var sb = new StringBuilder();
		
		sb.append(KEYWORD_WHERE);
		sb.append(" ");
		
		//for each of the where conditions
		for (int i = 0; i < conditions.size(); i++)
		{
			if (i > 0)
			{
				sb.append(" AND ");
			}
			
			sb.append(conditions.get(i).build(context));
		}
		
		return sb.toString();
	}
	
	@Override
	public Stream<Map.Entry<String, Object>> getParameterStream(final QueryBuilderContext context)
	{
		return conditions.stream().flatMap(c -> c.getParameterStream(context));
	}
	
	public static Where and(final Condition... conditions)
	{
		return new Where(new AndCondition(conditions));
	}
	
	public static Where and(final List<Condition> conditions)
	{
		return and(conditions.toArray(new Condition[0]));
	}
	
	public static Where or(final List<? extends Condition> conditions)
	{
		return new Where(new OrCondition(conditions));
	}
	
	public static Where or(final Condition... conditions)
	{
		return new Where(new OrCondition(conditions));
	}
	
	public static Where from(final Condition condition)
	{
		return new Where(condition);
	}
	
	public static WhereBuilder builder()
	{
		return new WhereBuilder();
	}
	
	public static class WhereBuilder
	{
		private final List<Condition> conditions;
		
		private WhereBuilder()
		{
			this.conditions = new ArrayList<>();
		}
		
		public WhereBuilder add(final Condition condition)
		{
			conditions.add(condition);
			return this;
		}
		
		public Optional<Where> build()
		{
			if (conditions.isEmpty())
			{
				return Optional.empty();
			}
			else
			{
				return Optional.of(new Where(conditions));
			}
		}
	}
}
