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

import org.apache.commons.lang3.ObjectUtils;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Optional;

/**
 * Represents a range of dates, before and after. Either before or after can be null to represent an open ended range.
 * Both dates can be null to represent that there is no restriction on a date range.
 *
 * @param after
 * @param before
 */
public record DateRange(@Nullable Instant after, @Nullable Instant before)
{
	public static DateRange after(@Nullable final Instant after)
	{
		return new DateRange(after, null);
	}
	
	public static DateRange before(@Nullable final Instant before)
	{
		return new DateRange(null, before);
	}
	
	@Nullable
	public static DateRange between(@Nullable final Instant after, @Nullable final Instant before)
	{
		return ObjectUtils.anyNotNull(after, before) ? new DateRange(after, before) : null;
	}
	
	@Nullable
	public static DateRange betweenMillis(@Nullable final Long after, @Nullable final Long before)
	{
		if (ObjectUtils.anyNotNull(after, before))
		{
			return new DateRange(
				Optional.ofNullable(after).map(Instant::ofEpochMilli).orElse(null),
				Optional.ofNullable(before).map(Instant::ofEpochMilli).orElse(null)
			);
		}
		else
		{
			return null;
		}
	}
}
