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

import com.gregmarut.querybuilder.Sort;
import com.gregmarut.querybuilder.predicate.Predicate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

/**
 * Represents a list-based query that can be run against a mongo database
 *
 * @param <E>
 */
public class DefaultMongoQuery<E> extends BaseMongoQuery
{
	private final Class<E> documentClass;
	private final Integer limit;
	
	public DefaultMongoQuery(final Class<E> documentClass, final List<Predicate> predicates, final List<Sort> sortList, final Integer limit)
	{
		super(predicates, sortList);
		this.documentClass = documentClass;
		this.limit = limit;
	}
	
	public List<E> run(final MongoTemplate mongoTemplate)
	{
		Query query = new Query();
		
		//build the criteria for this query
		buildCriteria().ifPresent(query::addCriteria);
		
		//build the sort for this query
		buildSort().ifPresent(query::with);
		
		if (null != limit)
		{
			query.limit(limit);
		}
		
		return mongoTemplate.find(query, documentClass);
	}
}
