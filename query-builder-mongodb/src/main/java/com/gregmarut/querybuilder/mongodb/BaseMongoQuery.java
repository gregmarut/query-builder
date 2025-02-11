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

package com.gregmarut.querybuilder.mongodb;

import com.gregmarut.querybuilder.Expression;
import com.gregmarut.querybuilder.Path;
import com.gregmarut.querybuilder.Sort;
import com.gregmarut.querybuilder.mongodb.predicate.LongDateRangePredicate;
import com.gregmarut.querybuilder.mongodb.util.DateRangeUtil;
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
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * The base class for generating a mongo query with predicates and sorts
 */
public abstract class BaseMongoQuery
{
	private final List<Predicate> predicates;
	private final List<Sort> sortList;
	
	public BaseMongoQuery(final List<Predicate> predicates, final List<Sort> sortList)
	{
		this.predicates = predicates;
		this.sortList = sortList;
	}
	
	protected Optional<Criteria> buildCriteria()
	{
		//convert the list of predicates to criteria
		final var andCriteria = predicates.stream().map(this::toCriteria).toList();
		if (!andCriteria.isEmpty())
		{
			final Criteria criteria = new Criteria();
			criteria.andOperator(andCriteria);
			return Optional.of(criteria);
		}
		else
		{
			return Optional.empty();
		}
	}
	
	protected Optional<org.springframework.data.domain.Sort> buildSort()
	{
		//for each of the sort objects
		return sortList
			.stream()
			.map(this::toSort)
			.reduce(org.springframework.data.domain.Sort::and);
	}
	
	private org.springframework.data.domain.Sort toSort(final Sort sort)
	{
		final var direction = switch (sort.direction())
		{
			case ASC -> org.springframework.data.domain.Sort.Direction.ASC;
			case DESC -> org.springframework.data.domain.Sort.Direction.DESC;
		};
		return org.springframework.data.domain.Sort.by(direction, toPath(sort.column()));
	}
	
	/**
	 * Converts a {@link Predicate} to a {@link Criteria} object
	 *
	 * @param predicate
	 * @return
	 */
	private Criteria toCriteria(final Predicate predicate)
	{
		if (predicate instanceof EqualsPredicate p)
		{
			final var path = joinPath(p.getPath());
			return Criteria.where(path).is(p.getValue());
		}
		else if (predicate instanceof NotEqualsPredicate p)
		{
			final var path = joinPath(p.getPath());
			return Criteria.where(path).not().is(p.getValue());
		}
		else if (predicate instanceof InPredicate p)
		{
			final var path = joinPath(p.getPath());
			return Criteria.where(path).in(p.getValue());
		}
		else if (predicate instanceof NotInPredicate p)
		{
			final var path = joinPath(p.getPath());
			return Criteria.where(path).not().in(p.getValue());
		}
		else if (predicate instanceof NullPredicate p)
		{
			final var path = joinPath(p.getPath());
			return Criteria.where(path).isNull();
		}
		else if (predicate instanceof NotNullPredicate p)
		{
			final var path = joinPath(p.getPath());
			return Criteria.where(path).not().isNull();
		}
		else if (predicate instanceof FuzzyMatchPredicate p)
		{
			final var path = joinPath(p.getPath());
			return Criteria.where(path).regex(p.getValue());
		}
		else if (predicate instanceof DateRangePredicate p)
		{
			final var path = joinPath(p.getPath());
			return DateRangeUtil.toCriteria(p.getValue(), path);
		}
		else if (predicate instanceof LongDateRangePredicate p)
		{
			final var path = joinPath(p.getPath());
			return DateRangeUtil.toCriteria(p.getValue(), path);
		}
		else if (predicate instanceof OrPredicate p)
		{
			return new Criteria().orOperator(Arrays.stream(p.predicates())
				.map(this::toCriteria)
				.toArray(Criteria[]::new));
		}
		else if (predicate instanceof AndPredicate p)
		{
			return new Criteria().andOperator(Arrays.stream(p.predicates())
				.map(this::toCriteria)
				.toArray(Criteria[]::new));
		}
		else
		{
			throw new IllegalStateException("Unknown predicate type: " + predicate.getClass().getName());
		}
	}
	
	/**
	 * Converts a {@link Expression} to a String path
	 *
	 * @param expression
	 * @return
	 */
	private String toPath(final Expression expression)
	{
		if (expression instanceof Path e)
		{
			return joinPath(e.path());
		}
		else
		{
			throw new IllegalStateException("Unknown expression type: " + expression.getClass().getName());
		}
	}
	
	private String joinPath(final String... path)
	{
		return String.join(".", path);
	}
}
