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

import com.gregmarut.querybuilder.cypher.Array;
import com.gregmarut.querybuilder.cypher.CypherString;
import com.gregmarut.querybuilder.cypher.LiteralCypherString;
import com.gregmarut.querybuilder.cypher.QueryBuilderContext;
import lombok.NonNull;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

public class AnyInCondition extends Condition
{
	private static final String ANY = "ANY";
	private static final String WHERE = "WHERE";
	
	final InCondition inCondition1;
	final InCondition inCondition2;
	
	public AnyInCondition(@NonNull final CypherString value1, @NonNull final CypherString value2)
	{
		final var element = "_element";
		
		this.inCondition1 = new InCondition(LiteralCypherString.of(element), value1);
		this.inCondition2 = new InCondition(LiteralCypherString.of(element), value2);
	}
	
	public AnyInCondition(@NonNull final CypherString value1, @NonNull final Collection<? extends CypherString> value2)
	{
		this(value1, Array.of(value2));
	}
	
	@Override
	protected String _build(final QueryBuilderContext context)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(ANY);
		sb.append("(");
		sb.append(inCondition1.build(context));
		sb.append(" ");
		sb.append(WHERE);
		sb.append(" ");
		sb.append(inCondition2.build(context));
		sb.append(")");
		
		return sb.toString();
	}
	
	@Override
	public Stream<Map.Entry<String, Object>> getParameterStream(final QueryBuilderContext context)
	{
		return Stream.of(inCondition1.getParameterStream(context), inCondition2.getParameterStream(context))
			.flatMap(s -> s);
	}
}
