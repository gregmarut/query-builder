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

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Getter
public class CypherQuery
{
	private final String query;
	private final Map<String, Object> params;
	
	public CypherQuery(final String query, final Map<String, Object> params)
	{
		this.query = query;
		this.params = Collections.unmodifiableMap(params);
	}
	
	@Override
	public String toString()
	{
		return getQuery();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
			return true;
		if (obj == null || obj.getClass() != this.getClass())
			return false;
		var that = (CypherQuery) obj;
		return Objects.equals(this.query, that.query) &&
			   Objects.equals(this.params, that.params);
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(query, params);
	}
}
