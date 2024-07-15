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

package com.gregmarut.querybuilder;

import com.gregmarut.querybuilder.predicate.Predicate;

import java.util.ArrayList;
import java.util.List;

public abstract class QueryBuilder<B extends QueryBuilder<B, E>, E>
{
	public static final String ROOT = "_ROOT";
	
	protected final Class<E> entityClass;
	protected final List<Sort> sortList;
	
	protected QueryBuilder(final Class<E> entityClass)
	{
		this.entityClass = entityClass;
		this.sortList = new ArrayList<>();
	}
	
	public B addSort(final String column, final SortDirectionType sortDirectionType)
	{
		if (null != column && null != sortDirectionType)
		{
			addSort(new Sort(new Path(ROOT, column), sortDirectionType));
		}
		
		return self();
	}
	
	public B addSort(final Sort sort)
	{
		sortList.add(sort);
		return self();
	}
	
	public B self()
	{
		return (B) this;
	}
	
	protected List<Predicate> buildPredicates()
	{
		final List<Predicate> predicates = new ArrayList<>();
		addPredicates(predicates);
		return predicates;
	}
	
	protected abstract void addPredicates(final List<Predicate> predicates);
	
	protected abstract List<Sort> getDefaultSort();
}
