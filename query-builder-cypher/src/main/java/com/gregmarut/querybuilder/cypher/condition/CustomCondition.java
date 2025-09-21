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

package com.gregmarut.querybuilder.cypher.condition;

import com.gregmarut.querybuilder.cypher.CypherBuilder;
import com.gregmarut.querybuilder.cypher.Property;
import com.gregmarut.querybuilder.cypher.QueryBuilderContext;

import java.util.Map;
import java.util.stream.Stream;

public class CustomCondition
{
	public static Condition propertyGTENowMinusDays(final Property property, final int days)
	{
		return new Condition()
		{
			@Override
			protected String _build(final QueryBuilderContext context)
			{
				return property.build(context) + " >= datetime() - duration(" + CypherBuilder.VARIABLE_PREFIX + context.getVariableName(this) + ")";
			}
			
			@Override
			public Stream<Map.Entry<String, Object>> getParameterStream(final QueryBuilderContext context)
			{
				final Object value = "P" + days + "D";
				return Map.of(context.getVariableName(this), value).entrySet().stream();
			}
		};
	}
	
	public static Condition propertyGTENowMinusMonths(final Property property, final int months)
	{
		return new Condition()
		{
			@Override
			protected String _build(final QueryBuilderContext context)
			{
				return property.build(context) + " >= datetime() - duration(" + CypherBuilder.VARIABLE_PREFIX + context.getVariableName(this) + ")";
			}
			
			@Override
			public Stream<Map.Entry<String, Object>> getParameterStream(final QueryBuilderContext context)
			{
				final Object value = "P" + months + "M";
				return Map.of(context.getVariableName(this), value).entrySet().stream();
			}
		};
	}
	
	public static Condition falseOrNull(final Property property)
	{
		return new OrCondition(
			new NullCondition(property),
			new EqualsCondition(property, false)
		);
	}
}
