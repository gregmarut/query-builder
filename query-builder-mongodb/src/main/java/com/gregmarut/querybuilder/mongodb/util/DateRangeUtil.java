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

package com.gregmarut.querybuilder.mongodb.util;

import com.gregmarut.querybuilder.DateRange;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.Optional;

public class DateRangeUtil
{
	/**
	 * Converts a DateRange object into a Criteria object for querying purposes.
	 *
	 * @param dateRange The DateRange object to convert.
	 * @param field     The field name to apply the criteria on.
	 * @return The Criteria object representing the DateRange query.
	 * @throws IllegalArgumentException if the DateRange object is invalid.
	 */
	public static Criteria toCriteria(final DateRange dateRange, final String field)
	{
		final var before = Optional.ofNullable(dateRange).map(DateRange::before).orElse(null);
		final var after = Optional.ofNullable(dateRange).map(DateRange::after).orElse(null);
		
		///check to see if there is a before timestamp
		if (null != before && null != after)
		{
			return Criteria.where(field).lt(before.toEpochMilli()).gte(after.toEpochMilli());
		}
		else if (null != before)
		{
			return Criteria.where(field).lt(before.toEpochMilli());
		}
		else if (null != after)
		{
			return Criteria.where(field).gte(after.toEpochMilli());
		}
		else
		{
			throw new IllegalArgumentException("Invalid date range: " + dateRange);
		}
	}
}
