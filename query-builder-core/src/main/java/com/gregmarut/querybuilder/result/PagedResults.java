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

package com.gregmarut.querybuilder.result;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public record PagedResults<T>(List<T> results, long pageIndex, long total)
{
	public <E> PagedResults<E> transformAllResults(final Function<List<T>, List<E>> transform)
	{
		return new PagedResults<>(transform.apply(results), pageIndex, total);
	}
	
	public <E> PagedResults<E> transformResults(final Function<T, E> transform)
	{
		return new PagedResults<>(results.stream().map(transform).toList(), pageIndex, total);
	}
	
	public <E> PagedResults<E> transformStream(final Function<Stream<T>, Stream<E>> transform)
	{
		return new PagedResults<>(transform.apply(results.stream()).toList(), pageIndex, total);
	}
	
	public <E, X extends Exception> PagedResults<E> transformThrowableResults(final ThrowableFunction<T, E, X> transform) throws X
	{
		final var results = new ArrayList<E>();
		
		//for each of the results
		for (T obj : this.results)
		{
			results.add(transform.apply(obj));
		}
		
		return new PagedResults<>(results, pageIndex, total);
	}
}
