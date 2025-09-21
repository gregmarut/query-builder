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

import com.gregmarut.querybuilder.cypher.CypherBuilder;
import com.gregmarut.querybuilder.cypher.CypherString;
import com.gregmarut.querybuilder.cypher.QueryBuilderContext;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Stream;

@NoArgsConstructor
@AllArgsConstructor
public class DateTime extends CypherString
{
	@Nullable
	private Instant dateTime;
	
	@Override
	protected String _build(final QueryBuilderContext context)
	{
		if (null != dateTime)
		{
			return "datetime(" + CypherBuilder.VARIABLE_PREFIX + context.getVariableName(dateTime) + ")";
		}
		else
		{
			return "datetime()";
		}
	}
	
	@Override
	public Stream<Map.Entry<String, Object>> getParameterStream(final QueryBuilderContext context)
	{
		if (null != dateTime)
		{
			return Map.of(context.getVariableName(dateTime), (Object) dateTime).entrySet().stream();
		}
		else
		{
			return Stream.empty();
		}
	}
}
