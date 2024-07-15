/*
 * Copyright 2024 Greg Marut
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

package com.gregmarut.querybuilder.jpa;

import com.gregmarut.querybuilder.Sort;
import com.gregmarut.querybuilder.predicate.Predicate;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
public class JPALimitedQuery<E> extends JPAQuery<E>
{
	private final int limit;
	
	public JPALimitedQuery(final Class<E> entityClass, final List<Predicate> predicates, final List<Sort> sortList,
		final Set<JPAJoin> fetchSet, final Map<String, JPAJoin> joinMap, final int limit)
	{
		super(entityClass, predicates, sortList, fetchSet, joinMap);
		
		this.limit = limit;
	}
}
