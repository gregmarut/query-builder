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

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.stream.Stream;

public class AliasedCypherString<E extends CypherString> extends ReturnableCypherString implements Identifiable
{
	@Getter
	private final E cypherString;
	private final String alias;
	
	public AliasedCypherString(final E cypherString, final String alias)
	{
		this.cypherString = cypherString;
		this.alias = alias;
	}
	
	public AliasedCypherString(final E cypherString, final IdentifierGenerator generator)
	{
		this.cypherString = cypherString;
		this.alias = generator.next();
	}
	
	@Override
	protected String _build(final QueryBuilderContext context)
	{
		if (StringUtils.isBlank(alias))
		{
			throw new IllegalArgumentException("Alias cannot be null or empty.");
		}
		
		if (isBuilt(context))
		{
			return alias;
		}
		else
		{
			final var value = cypherString.build(context) + " AS " + alias;
			context.built(this);
			return value;
		}
	}
	
	@Override
	public String getIdentifier()
	{
		return alias;
	}
	
	@Override
	public Stream<Map.Entry<String, Object>> getParameterStream(final QueryBuilderContext context)
	{
		return cypherString.getParameterStream(context);
	}
}
