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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class IdentifierGenerator
{
	private final AtomicInteger counter;
	private final Set<String> identifiers;
	
	public IdentifierGenerator()
	{
		this.counter = new AtomicInteger();
		this.identifiers = new HashSet<>();
	}
	
	public String next()
	{
		return "_i_" + counter.incrementAndGet();
	}
	
	public String unique(final String label)
	{
		final String lowercaseLabel = label.toLowerCase();
		
		//try increasing prefix length until full label is used
		for (int length = 1; length <= lowercaseLabel.length(); length++)
		{
			String candidate = lowercaseLabel.substring(0, length);
			if (!identifiers.contains(candidate))
			{
				identifiers.add(candidate);
				return candidate;
			}
		}
		
		//if the full label is taken, append numbers until unique identifier is found
		for (int suffix = 1; ; suffix++)
		{
			String candidate = lowercaseLabel + suffix;
			if (!identifiers.contains(candidate))
			{
				identifiers.add(candidate);
				return candidate;
			}
		}
	}
}
