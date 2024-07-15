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

import com.gregmarut.querybuilder.predicate.Predicate;
import jakarta.persistence.criteria.JoinType;
import lombok.Getter;

@Getter
public class JPAJoin
{
	private final String from;
	private final String column;
	private final JoinType joinType;
	private Predicate[] predicates;
	
	public JPAJoin(String from, String column, JoinType joinType)
	{
		this.from = from;
		this.column = column;
		this.joinType = joinType;
	}
	
	public JPAJoin on(final Predicate... predicates)
	{
		this.predicates = predicates;
		return this;
	}
}
