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

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * Represents a part of a cypher query that can be built as a string
 */
public abstract class CypherString implements Parameterized
{
	public final String build(final QueryBuilderContext context)
	{
		return build(context, Collections.emptySet());
	}
	
	public final String build(final QueryBuilderContext context, final Set<String> flags)
	{
		//if there are any flags, add them to the context
		Optional.ofNullable(flags).ifPresent(f -> context.getFlags().addAll(f));
		
		context.pushBuildStack(getClass());
		final var result = _build(context);
		context.popBuildStack(getClass());
		
		//if any flags were added, remove them now that this context has finished
		Optional.ofNullable(flags).ifPresent(f -> context.getFlags().removeAll(f));
		
		return result;
	}
	
	protected abstract String _build(QueryBuilderContext context);
	
	public boolean isBuilt(final QueryBuilderContext context)
	{
		return context.isBuilt(this);
	}
	
	@Override
	public String toString()
	{
		return build(QueryBuilderContext.createDefault());
	}
}
