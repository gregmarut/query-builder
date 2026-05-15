/*
 * Copyright 2026 Greg Marut
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

import com.gregmarut.querybuilder.cypher.Array;
import com.gregmarut.querybuilder.cypher.CypherString;
import com.gregmarut.querybuilder.cypher.QueryBuilderContext;
import lombok.NonNull;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

public class AnyInCondition extends Condition
{
	private static final String KEYWORD_ANY = "ANY";
	private static final String KEYWORD_WHERE = "WHERE";
	private static final String KEYWORD_IN = "IN";

	private final CypherString value1;
	private final CypherString value2;

	public AnyInCondition(@NonNull final CypherString value1, @NonNull final CypherString value2)
	{
		this.value1 = value1;
		this.value2 = value2;
	}

	public AnyInCondition(@NonNull final CypherString value1, @NonNull final Collection<? extends CypherString> value2)
	{
		this(value1, Array.of(value2));
	}

	@Override
	protected String _build(final QueryBuilderContext context)
	{
		//use a context-generated name so it is unique within this query and consistent across _build and getParameterStream
		final var element = context.getVariableName(this);

		final var sb = new StringBuilder();
		sb.append(KEYWORD_ANY);
		sb.append("(");
		sb.append(element);
		sb.append(" ");
		sb.append(KEYWORD_IN);
		sb.append(" ");
		sb.append(value1.build(context));
		sb.append(" ");
		sb.append(KEYWORD_WHERE);
		sb.append(" ");
		sb.append(element);
		sb.append(" ");
		sb.append(KEYWORD_IN);
		sb.append(" ");
		sb.append(value2.build(context));
		sb.append(")");

		return sb.toString();
	}

	@Override
	public Stream<Map.Entry<String, Object>> getParameterStream(final QueryBuilderContext context)
	{
		return Stream.of(value1.getParameterStream(context), value2.getParameterStream(context))
			.flatMap(s -> s);
	}
}
