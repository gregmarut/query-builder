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
import com.gregmarut.querybuilder.mongodb.result.PagedDocumentResults;
import com.gregmarut.querybuilder.predicate.Predicate;
import com.gregmarut.querybuilder.result.PagedResults;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Provides a query that can be used to perform paginated searches in a mongo collection.
 *
 * @param <E>
 */
public class PaginatedMongoSearchQuery<E> extends BaseMongoQuery
{
	private final int pageIndex;
	private final int pageSize;
	
	public PaginatedMongoSearchQuery(final List<Predicate> predicates, final List<Sort> sortList, final int pageIndex, final int pageSize)
	{
		super(predicates, sortList);
		this.pageIndex = pageIndex;
		this.pageSize = pageSize;
	}
	
	public PagedResults<E> run(final MongoTemplate mongoTemplate, final String collection)
	{
		//build the aggregation
		final Aggregation aggregation = buildAggregation();
		
		@SuppressWarnings("unchecked") final PagedDocumentResults<E> results =
			mongoTemplate.aggregate(aggregation, collection, PagedDocumentResults.class).getUniqueMappedResult();
		
		return Optional.ofNullable(results).map(PagedDocumentResults::getResults)
			.map(list -> new PagedResults<E>(list, pageIndex, results.getTotal()))
			.orElseGet(() -> new PagedResults<>(Collections.emptyList(), pageIndex, 0));
	}
	
	private Aggregation buildAggregation()
	{
		//holds the list of aggregation operations
		final List<AggregationOperation> aggregationOperations = new ArrayList<>();
		
		//add the criteria to the aggregation as a match expression
		buildCriteria().ifPresent(criteria -> aggregationOperations.add(Aggregation.match(criteria)));
		
		//add the sort operation to the aggregation
		buildSort().map(Aggregation::sort).ifPresent(aggregationOperations::add);
		
		//group the results
		aggregationOperations.add(Aggregation.group().count().as("total").push("$$ROOT").as("results"));
		
		//define the projection of the results
		aggregationOperations.add(Aggregation.project()
			.and("total").as("total")
			.and(ArrayOperators.Slice.sliceArrayOf("results").offset(pageIndex * pageSize).itemCount(pageSize)).as("results"));
		
		//build the entire aggregation
		return Aggregation.newAggregation(aggregationOperations.toArray(AggregationOperation[]::new));
	}
}
