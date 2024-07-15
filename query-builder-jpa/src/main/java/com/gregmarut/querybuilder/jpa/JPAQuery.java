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

import com.gregmarut.querybuilder.Expression;
import com.gregmarut.querybuilder.QueryBuilder;
import com.gregmarut.querybuilder.Sort;
import com.gregmarut.querybuilder.jpa.expression.Coalesce;
import com.gregmarut.querybuilder.jpa.expression.Greatest;
import com.gregmarut.querybuilder.jpa.function.GreatestFunction;
import com.gregmarut.querybuilder.jpa.util.DateRangeUtil;
import com.gregmarut.querybuilder.jpa.util.PredicateUtil;
import com.gregmarut.querybuilder.predicate.AndPredicate;
import com.gregmarut.querybuilder.predicate.DateRangePredicate;
import com.gregmarut.querybuilder.predicate.EqualsPredicate;
import com.gregmarut.querybuilder.predicate.FuzzyMatchPredicate;
import com.gregmarut.querybuilder.predicate.InPredicate;
import com.gregmarut.querybuilder.predicate.NotEqualsPredicate;
import com.gregmarut.querybuilder.predicate.NotInPredicate;
import com.gregmarut.querybuilder.predicate.NotNullPredicate;
import com.gregmarut.querybuilder.predicate.NullPredicate;
import com.gregmarut.querybuilder.predicate.OrPredicate;
import com.gregmarut.querybuilder.predicate.Predicate;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JPAQuery<E>
{
	private final Class<E> entityClass;
	private final Map<String, JPAJoin> joinMap;
	private final Set<JPAJoin> fetchSet;
	private final List<Predicate> predicates;
	private final List<Sort> sortList;
	
	public JPAQuery(final Class<E> entityClass, final List<Predicate> predicates, final List<Sort> sortList, final Set<JPAJoin> fetchSet,
		final Map<String, JPAJoin> joinMap)
	{
		this.entityClass = entityClass;
		this.joinMap = joinMap;
		this.fetchSet = fetchSet;
		this.predicates = predicates;
		this.sortList = sortList;
	}
	
	/**
	 * Builds the {@link CriteriaQuery} to return the results of the query
	 *
	 * @param criteriaBuilder
	 * @return
	 */
	public CriteriaQuery<E> buildCriteriaQuery(final CriteriaBuilder criteriaBuilder)
	{
		final CriteriaQuery<E> query = criteriaBuilder.createQuery(entityClass);
		final Root<E> root = query.from(entityClass);
		
		//build the root map and add the root object
		final Map<String, From<?, ?>> fromMap = new HashMap<>();
		fromMap.put(QueryBuilder.ROOT, root);
		
		//build the joins for the query
		buildJoins(fromMap, criteriaBuilder);
		
		//build the fetches
		buildFetches(fromMap);
		
		//build the predicates for this search
		final List<jakarta.persistence.criteria.Predicate> predicates = buildJPAPredicates(criteriaBuilder, fromMap);
		if (!predicates.isEmpty())
		{
			query.where(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
		}
		
		//check to see if there is a sort list
		if (!sortList.isEmpty())
		{
			query.orderBy(sortList.stream().map(sort -> toOrder(criteriaBuilder, fromMap, sort)).toList());
		}
		
		return query;
	}
	
	/**
	 * Builds a query that returns count of all of the results matching the query
	 *
	 * @param criteriaBuilder
	 * @return
	 */
	public CriteriaQuery<Long> buildCountQuery(final CriteriaBuilder criteriaBuilder)
	{
		//create the query that will count the total results
		final CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
		final Root<E> root = countQuery.from(entityClass);
		
		//build the root map and add the root object
		final Map<String, From<?, ?>> fromMap = new HashMap<>();
		fromMap.put(QueryBuilder.ROOT, root);
		
		//build the joins for the query
		buildJoins(fromMap, criteriaBuilder);
		
		//select the count of the root object
		countQuery.select(criteriaBuilder.count(root));
		
		//build the predicates for this search
		final List<jakarta.persistence.criteria.Predicate> predicates = buildJPAPredicates(criteriaBuilder, fromMap);
		if (!predicates.isEmpty())
		{
			countQuery.where(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
		}
		
		return countQuery;
	}
	
	/**
	 * Builds the joins for this query with the given map of from objects
	 *
	 * @param fromMap
	 * @return
	 */
	private void buildJoins(final Map<String, From<?, ?>> fromMap, final CriteriaBuilder criteriaBuilder)
	{
		final Map<JPAJoin, Join<?, ?>> joinFromMap = new HashMap<>();
		
		//for each of the joins
		for (final var entry : joinMap.entrySet())
		{
			final var alias = entry.getKey();
			final var join = entry.getValue();
			
			//find the from object for this join
			final var from = fromMap.get(join.getFrom());
			if (null == from)
			{
				throw new IllegalStateException("Unable to find the from object for join: " + join.getFrom());
			}
			
			//build this join and add it to the map
			final var j = from.join(join.getColumn(), join.getJoinType());
			fromMap.put(alias, j);
			joinFromMap.put(join, j);
		}
		
		//for each of the join in the join map
		joinFromMap.entrySet()
			.stream()
			//filter on entries that have predicates in the JPAJoin object
			.filter(e -> null != e.getKey().getPredicates())
			//for each of the entries
			.forEach(entry -> {
				//convert the predicates to JPA criteria predicates
				final var predicates = Arrays.stream(entry.getKey().getPredicates())
					.map(predicate -> toPredicate(criteriaBuilder, fromMap, predicate))
					.toArray(jakarta.persistence.criteria.Predicate[]::new);
				
				//set the "ON" clause for the join
				entry.getValue().on(predicates);
			});
	}
	
	private void buildFetches(final Map<String, From<?, ?>> fromMap)
	{
		//for each of the fetches
		for (final var fetch : fetchSet)
		{
			//find the from object for this join
			final var from = fromMap.get(fetch.getFrom());
			if (null == from)
			{
				throw new IllegalStateException("Unable to find the from object for join: " + fetch.getFrom());
			}
			
			from.fetch(fetch.getColumn(), fetch.getJoinType());
		}
	}
	
	/**
	 * Builds the list of JPA predicates for this search
	 *
	 * @param criteriaBuilder
	 * @param fromMap
	 * @return
	 */
	private List<jakarta.persistence.criteria.Predicate> buildJPAPredicates(final CriteriaBuilder criteriaBuilder,
		final Map<String, From<?, ?>> fromMap)
	{
		return predicates.stream()
			.map(predicate -> toPredicate(criteriaBuilder, fromMap, predicate))
			.toList();
	}
	
	/**
	 * Converts a {@link Predicate} to a JPA {@link jakarta.persistence.criteria.Predicate}
	 *
	 * @param fromMap
	 * @param criteriaBuilder
	 * @param predicate
	 * @return
	 */
	private jakarta.persistence.criteria.Predicate toPredicate(final CriteriaBuilder criteriaBuilder, final Map<String, From<?, ?>> fromMap,
		final Predicate predicate)
	{
		if (predicate instanceof EqualsPredicate p)
		{
			final var from = fromMap.get(p.getRoot());
			final var path = walk(from, p.getPath());
			return criteriaBuilder.equal(path, p.getValue());
		}
		else if (predicate instanceof NotEqualsPredicate p)
		{
			final var from = fromMap.get(p.getRoot());
			final var path = walk(from, p.getPath());
			return criteriaBuilder.notEqual(path, p.getValue());
		}
		else if (predicate instanceof InPredicate p)
		{
			final var from = fromMap.get(p.getRoot());
			final var path = walk(from, p.getPath());
			return path.in(p.getValue());
		}
		else if (predicate instanceof NotInPredicate p)
		{
			final var from = fromMap.get(p.getRoot());
			final var path = walk(from, p.getPath());
			return path.in(p.getValue()).not();
		}
		else if (predicate instanceof NullPredicate p)
		{
			final var from = fromMap.get(p.getRoot());
			final var path = walk(from, p.getPath());
			return criteriaBuilder.isNull(path);
		}
		else if (predicate instanceof NotNullPredicate p)
		{
			final var from = fromMap.get(p.getRoot());
			final var path = walk(from, p.getPath());
			return criteriaBuilder.isNotNull(path);
		}
		else if (predicate instanceof FuzzyMatchPredicate p)
		{
			final var from = fromMap.get(p.getRoot());
			final var path = (Path<String>) walk(from, p.getPath());
			return criteriaBuilder.like(path, PredicateUtil.wildcard(p.getValue()));
		}
		else if (predicate instanceof DateRangePredicate p)
		{
			final var from = fromMap.get(p.getRoot());
			final var path = (Path<Instant>) walk(from, p.getPath());
			return DateRangeUtil.toCriteria(p.getValue(), path, criteriaBuilder);
		}
		else if (predicate instanceof OrPredicate p)
		{
			return criteriaBuilder.or(Arrays.stream(p.predicates())
				.map(subPredicate -> toPredicate(criteriaBuilder, fromMap, subPredicate))
				.toArray(jakarta.persistence.criteria.Predicate[]::new));
		}
		else if (predicate instanceof AndPredicate p)
		{
			return criteriaBuilder.and(Arrays.stream(p.predicates())
				.map(subPredicate -> toPredicate(criteriaBuilder, fromMap, subPredicate))
				.toArray(jakarta.persistence.criteria.Predicate[]::new));
		}
		else
		{
			throw new IllegalStateException("Unknown predicate type: " + predicate.getClass().getName());
		}
	}
	
	private jakarta.persistence.criteria.Expression<?> toExpression(final CriteriaBuilder criteriaBuilder, final Map<String, From<?, ?>> fromMap,
		final Expression expression)
	{
		if (expression instanceof com.gregmarut.querybuilder.Path e)
		{
			final var from = fromMap.get(e.root());
			return walk(from, e.path());
		}
		else if (expression instanceof Coalesce<?> e)
		{
			final var e1 = toExpression(criteriaBuilder, fromMap, e.expression());
			return criteriaBuilder.coalesce(e1, e.value());
		}
		else if (expression instanceof Greatest<?> e)
		{
			final var e1 = toExpression(criteriaBuilder, fromMap, e.expression1());
			final var e2 = toExpression(criteriaBuilder, fromMap, e.expression2());
			return GreatestFunction.build(criteriaBuilder, e.clazz(), e1, e2);
		}
		else
		{
			throw new IllegalStateException("Unknown expression type: " + expression.getClass().getName());
		}
	}
	
	/**
	 * Given a start path and an array of paths, this method will walk the path and return the final path object
	 *
	 * @param start
	 * @param paths
	 * @return
	 */
	private Path<?> walk(final Path<?> start, final String[] paths)
	{
		return Arrays.stream(paths).reduce(start, Path::get, (p1, p2) -> p2);
	}
	
	private Order toOrder(final CriteriaBuilder criteriaBuilder, final Map<String, From<?, ?>> fromMap, final Sort sort)
	{
		return switch (sort.direction())
		{
			case ASC -> criteriaBuilder.asc(toExpression(criteriaBuilder, fromMap, sort.column()));
			case DESC -> criteriaBuilder.desc(toExpression(criteriaBuilder, fromMap, sort.column()));
		};
	}
}
