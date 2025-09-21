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
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
@Accessors(chain = true)
@RequiredArgsConstructor
public class QueryBuilderContext
{
	public static boolean defaultPrettyPrint = false;
	
	private final Set<CypherString> builtQueryPhrases;
	private final Map<Object, String> variableNameMap;
	private final Set<String> flags;
	private final Stack<Class<? extends CypherString>> buildStack;
	
	private AtomicInteger variableCounter;
	private boolean prettyPrint;
	
	public QueryBuilderContext()
	{
		this.builtQueryPhrases = new HashSet<>();
		this.variableNameMap = new HashMap<>();
		this.flags = new HashSet<>();
		this.buildStack = new Stack<>();
		
		this.variableCounter = new AtomicInteger();
		this.prettyPrint = defaultPrettyPrint;
	}
	
	public void built(final CypherString cypherString)
	{
		builtQueryPhrases.add(cypherString);
	}
	
	public boolean isBuilt(final CypherString cypherString)
	{
		return builtQueryPhrases.contains(cypherString);
	}
	
	public String getVariableName(final Object key)
	{
		return variableNameMap.computeIfAbsent(key, k -> "_v" + variableCounter.getAndIncrement());
	}
	
	public void pushBuildStack(final Class<? extends CypherString> clazz)
	{
		buildStack.push(clazz);
	}
	
	public void popBuildStack(final Class<? extends CypherString> clazz)
	{
		final var poppedClass = buildStack.pop();
		
		if (!poppedClass.equals(clazz))
		{
			throw new IllegalStateException("The class that was popped off the build stack does not match the expected class.");
		}
	}
	
	/**
	 * Returns the parent class from the build stack if it is available.
	 * The top of the build stack is the current class that is being built. The 2nd top most class is the parent.
	 *
	 * @return the parent class from the build stack if the stack contains at least two elements, otherwise null.
	 */
	public Class<? extends CypherString> getParentFromStack()
	{
		if (buildStack.size() >= 2)
		{
			return buildStack.get(buildStack.size() - 2);
		}
		else
		{
			return null;
		}
	}
	
	public static QueryBuilderContext createDefault()
	{
		return new QueryBuilderContext();
	}
	
	public char getStatementSeparator()
	{
		return prettyPrint ? '\n' : ' ';
	}
	
	public String getIndentCharacter()
	{
		return prettyPrint ? "\t" : "";
	}
}
