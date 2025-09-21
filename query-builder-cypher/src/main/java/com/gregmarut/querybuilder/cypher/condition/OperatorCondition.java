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

import com.gregmarut.querybuilder.cypher.CypherString;
import com.gregmarut.querybuilder.cypher.LiteralCypherString;
import com.gregmarut.querybuilder.cypher.QueryBuilderContext;
import lombok.Getter;
import lombok.NonNull;

import java.util.Map;
import java.util.stream.Stream;

@Getter
public abstract class OperatorCondition extends Condition
{
	private final CypherString value1;
	private final String operator;
	private final CypherString value2;
	
	OperatorCondition(@NonNull final CypherString value1, @NonNull final String operator, @NonNull final CypherString value2)
	{
		this.value1 = value1;
		this.operator = operator;
		this.value2 = value2;
	}
	
	OperatorCondition(@NonNull final CypherString value1, @NonNull final String operator, @NonNull final Number value2)
	{
		this.value1 = value1;
		this.operator = operator;
		this.value2 = LiteralCypherString.of(value2);
	}
	
	OperatorCondition(@NonNull final CypherString value1, @NonNull final String operator, @NonNull final boolean value2)
	{
		this.value1 = value1;
		this.operator = operator;
		this.value2 = LiteralCypherString.of(value2);
	}
	
	@Override
	public Stream<Map.Entry<String, Object>> getParameterStream(final QueryBuilderContext context)
	{
		return Stream.concat(value1.getParameterStream(context), value2.getParameterStream(context));
	}
	
	@Override
	protected String _build(final QueryBuilderContext context)
	{
		final var sb = new StringBuilder();
		
		sb.append(value1.build(context));
		sb.append(" ");
		sb.append(operator);
		sb.append(" ");
		sb.append(value2.build(context));
		
		return sb.toString();
	}
}
