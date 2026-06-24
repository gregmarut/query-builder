/*
 * Copyright 2026 Greg Marut
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

package com.gregmarut.querybuilder.cypher.phrase;

import com.gregmarut.querybuilder.cypher.CypherString;
import com.gregmarut.querybuilder.cypher.Identifiable;
import com.gregmarut.querybuilder.cypher.QueryBuilderContext;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Renders a Cypher FOREACH clause:
 * {@code FOREACH (variable IN collection | innerClause)}
 *
 * <p>
 * The {@code variable} is an {@link Identifiable} so that the same object can be passed to both
 * this clause (as the loop variable name) and to the inner clause (e.g. {@code Delete.delete(variable)}),
 * ensuring the two references always stay in sync.
 * </p>
 *
 * <p>
 * Pair with a preceding {@code WITH ..., COLLECT(rel) AS list} to iterate safely over an
 * OPTIONAL MATCH result without risking a {@code DELETE null} error when nothing matched.
 * </p>
 */
@RequiredArgsConstructor
public class ForEach extends CypherPhrase
{
	private static final String KEYWORD_FOREACH = "FOREACH";

	private final Identifiable variable;
	private final CypherString collection;
	private final CypherPhrase innerClause;

	@Override
	protected String _build(final QueryBuilderContext context)
	{
		return KEYWORD_FOREACH + " (" + variable.getRequiredIdentifier() + " IN " + collection.build(context) + " | " + innerClause.build(context) + ")";
	}

	@Override
	public Stream<Map.Entry<String, Object>> getParameterStream(final QueryBuilderContext context)
	{
		return Stream.concat(collection.getParameterStream(context), innerClause.getParameterStream(context));
	}
}
