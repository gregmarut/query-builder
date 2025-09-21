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

import com.gregmarut.querybuilder.cypher.Identifiable;
import com.gregmarut.querybuilder.cypher.QueryBuilderContext;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class Delete extends CypherPhrase
{
	private static final String KEYWORD_DELETE = "DELETE";
	private static final String KEYWORD_DETACH = "DETACH";
	
	private final List<Identifiable> identifiables;
	private final boolean detach;
	
	private Delete(final boolean detach, final Identifiable... identifiables)
	{
		if (null == identifiables)
		{
			throw new IllegalArgumentException("nodes cannot be null");
		}
		
		this.identifiables = List.of(identifiables);
		this.detach = detach;
	}
	
	@Override
	protected String _build(final QueryBuilderContext context)
	{
		final var sb = new StringBuilder();
		
		if (detach)
		{
			sb.append(KEYWORD_DETACH);
			sb.append(" ");
		}
		
		sb.append(KEYWORD_DELETE);
		sb.append(" ");
		
		sb.append(identifiables.stream().map(Identifiable::getRequiredIdentifier).collect(Collectors.joining(", ")));
		
		return sb.toString();
	}
	
	@Override
	public Stream<Map.Entry<String, Object>> getParameterStream(final QueryBuilderContext context)
	{
		return Stream.empty();
	}
	
	public static Delete delete(final Identifiable... identifiables)
	{
		return new Delete(false, identifiables);
	}
	
	public static Delete detachDelete(final Identifiable... identifiables)
	{
		return new Delete(true, identifiables);
	}
}
