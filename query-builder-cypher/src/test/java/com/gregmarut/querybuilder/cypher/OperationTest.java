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

package com.gregmarut.querybuilder.cypher;

import com.gregmarut.querybuilder.cypher.model.MovieNode;
import com.gregmarut.querybuilder.cypher.operation.Add;
import com.gregmarut.querybuilder.cypher.operation.Divide;
import com.gregmarut.querybuilder.cypher.operation.Multiply;
import com.gregmarut.querybuilder.cypher.operation.Subtract;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class OperationTest
{
	@BeforeAll
	static void setUp()
	{
		QueryBuilderContext.defaultPrettyPrint = true;
	}

	@Test
	public void addOperation()
	{
		final var context = QueryBuilderContext.createDefault();
		final var add = Add.of(Variable.of(1), Variable.of(2));

		Assertions.assertEquals("$_v0 + $_v1", add.build(context));
		final var params = add.getParameterStream(context).toList();
		Assertions.assertEquals(2, params.size());
	}

	@Test
	public void subtractOperation()
	{
		final var context = QueryBuilderContext.createDefault();
		final var subtract = Subtract.of(Variable.of(10), Variable.of(3));

		Assertions.assertEquals("$_v0 - $_v1", subtract.build(context));
	}

	@Test
	public void multiplyOperation()
	{
		final var context = QueryBuilderContext.createDefault();
		final var multiply = Multiply.of(Variable.of(4), Variable.of(5));

		Assertions.assertEquals("$_v0 * $_v1", multiply.build(context));
	}

	@Test
	public void divideOperation()
	{
		final var context = QueryBuilderContext.createDefault();
		final var divide = Divide.of(Variable.of(20), Variable.of(4));

		Assertions.assertEquals("$_v0 / $_v1", divide.build(context));
	}

	@Test
	public void addOperationAgainstPropertyAndLiteral()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var movieNode = new MovieNode().named(identifierGenerator);

		final var context = QueryBuilderContext.createDefault();
		//property + literal renders without parameter wrapping
		final var add = Add.of(movieNode.getProperty(MovieNode.RELEASED), LiteralCypherString.of("5"));

		Assertions.assertEquals("m.released + 5", add.build(context));
		Assertions.assertEquals(0, add.getParameterStream(context).count());
	}

	@Test
	public void compositeOperationProducesNestedExpression()
	{
		final var context = QueryBuilderContext.createDefault();
		//build (a + b) * c
		final var inner = Add.of(Variable.of(1), Variable.of(2));
		final var outer = Multiply.of(inner, Variable.of(3));

		Assertions.assertEquals("$_v0 + $_v1 * $_v2", outer.build(context));
	}
}
