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

package com.gregmarut.querybuilder.jpa.util;

import com.gregmarut.querybuilder.DateRange;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import java.time.Instant;
import java.util.Optional;

public class DateRangeUtil
{
	/**
	 * Converts a DateRange into a Predicate criteria for searching a Path of Instant values.
	 *
	 * @param dateRange       the DateRange object representing the range of dates to search
	 * @param field           the Path of Instant values to apply the criteria on
	 * @param criteriaBuilder the CriteriaBuilder used to create the criteria
	 * @return the Predicate criteria representing the date range search
	 * @throws IllegalArgumentException if the dateRange is invalid or null
	 */
	public static Predicate toCriteria(final DateRange dateRange, final Path<Instant> field, final CriteriaBuilder criteriaBuilder)
	{
		final var before = Optional.ofNullable(dateRange).map(DateRange::before).orElse(null);
		final var after = Optional.ofNullable(dateRange).map(DateRange::after).orElse(null);
		
		///check to see if there is a before timestamp
		if (null != before && null != after)
		{
			return criteriaBuilder.and(
				criteriaBuilder.lessThan(field, before),
				criteriaBuilder.greaterThanOrEqualTo(field, after)
			);
		}
		else if (null != before)
		{
			return criteriaBuilder.lessThan(field, before);
		}
		else if (null != after)
		{
			return criteriaBuilder.greaterThanOrEqualTo(field, after);
		}
		else
		{
			throw new IllegalArgumentException("Invalid date range: " + dateRange);
		}
	}
}
