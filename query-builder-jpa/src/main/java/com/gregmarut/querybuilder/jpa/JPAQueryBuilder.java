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

import com.gregmarut.querybuilder.QueryBuilder;
import jakarta.persistence.metamodel.Attribute;
import lombok.NonNull;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.Set;

public abstract class JPAQueryBuilder<B extends JPAQueryBuilder<B, E>, E> extends QueryBuilder<B, E>
{
	private final LinkedHashMap<String, JPAJoin> joinMap;
	
	protected JPAQueryBuilder(final Class<E> entityClass)
	{
		super(entityClass);
		
		//define the join map as a linked hash map to ensure the order is maintained
		this.joinMap = new LinkedHashMap<>();
	}
	
	public JPALimitedQuery<E> buildQuery(final int limit)
	{
		//determine which sort to use
		final var sort = sortList.isEmpty() ? getDefaultSort() : sortList;
		
		return new JPALimitedQuery<>(entityClass, buildPredicates(), sort, buildFetchSet(), joinMap, limit);
	}
	
	public JPAQuery<E> buildQuery()
	{
		//determine which sort to use
		final var sort = sortList.isEmpty() ? getDefaultSort() : sortList;
		
		return new JPAQuery<E>(entityClass, buildPredicates(), sort, buildFetchSet(), joinMap);
	}
	
	public JPAPaginatedSearchQuery<E> buildSearch()
	{
		return buildSearch(0, JPAPaginatedSearchQuery.DEFAULT_PAGE_SIZE);
	}
	
	/**
	 * Builds a {@link JPAPaginatedSearchQuery} using the given pageIndex and pageSize. If either are null, default values are provided
	 *
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 */
	public JPAPaginatedSearchQuery<E> buildSearch(@Nullable final Integer pageIndex, @Nullable final Integer pageSize)
	{
		//determine which sort to use
		final var sort = sortList.isEmpty() ? getDefaultSort() : sortList;
		
		return new JPAPaginatedSearchQuery<E>(entityClass, buildPredicates(), sort, buildFetchSet(), joinMap,
			Optional.ofNullable(pageIndex).orElse(0),
			Optional.ofNullable(pageSize).orElse(JPAPaginatedSearchQuery.DEFAULT_PAGE_SIZE));
	}
	
	protected String alias(final String alias)
	{
		//check to see if this root has not already loaded into the join map
		if (!joinMap.containsKey(alias))
		{
			//define the JPAJoin object for this root
			final var join = defineAlias(alias);
			
			//make sure the from is not the root
			if (!ROOT.equals(join.getFrom()))
			{
				//recursively load the root for this join before adding the current join to the map to ensure the correct order
				alias(join.getFrom());
			}
			
			//add this join to the map
			joinMap.put(alias, join);
		}
		
		return alias;
	}
	
	@NonNull
	protected JPAJoin defineAlias(final String alias)
	{
		throw new IllegalArgumentException("alias " + alias + " is not defined");
	}
	
	protected Set<Attribute<? super E, ?>> buildFetchSet()
	{
		return Set.of();
	}
}
