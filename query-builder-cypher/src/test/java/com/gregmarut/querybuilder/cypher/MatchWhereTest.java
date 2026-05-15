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

import com.gregmarut.querybuilder.cypher.condition.EqualsCondition;
import com.gregmarut.querybuilder.cypher.condition.GreaterThanCondition;
import com.gregmarut.querybuilder.cypher.condition.NotNullCondition;
import com.gregmarut.querybuilder.cypher.model.PersonNode;
import com.gregmarut.querybuilder.cypher.phrase.Match;
import com.gregmarut.querybuilder.cypher.phrase.Where;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class MatchWhereTest
{
	@BeforeAll
	static void setUp()
	{
		QueryBuilderContext.defaultPrettyPrint = true;
	}

	@Test
	public void singleWhereOnMatchEmitsSingleWhereKeyword()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);

		final var match = Match.of(personNode)
			.where(new EqualsCondition(personNode.getProperty(PersonNode.NAME), "Alice"));

		final var query = CypherBuilder.create()
			.match(match)
			.addReturn(personNode)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person) WHERE p.name = $_v0
			RETURN p""", query.getQuery());
		Assertions.assertEquals("Alice", query.getParams().get("_v0"));
	}

	@Test
	public void multipleWhereCallsAreCombinedWithAndUnderSingleWhereKeyword()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);

		final var match = Match.of(personNode)
			.where(new EqualsCondition(personNode.getProperty(PersonNode.NAME), "Alice"))
			.where(new NotNullCondition(personNode.getProperty(PersonNode.BORN)));

		final var query = CypherBuilder.create()
			.match(match)
			.addReturn(personNode)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person) WHERE p.name = $_v0 AND p.born IS NOT null
			RETURN p""", query.getQuery());
		Assertions.assertEquals("Alice", query.getParams().get("_v0"));
	}

	@Test
	public void whereInstancePassedToMatchEmitsSingleWhereKeyword()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);

		final var where = Where.from(new EqualsCondition(personNode.getProperty(PersonNode.NAME), "Alice"));
		final var match = Match.of(personNode).where(where);

		final var query = CypherBuilder.create()
			.match(match)
			.addReturn(personNode)
			.build();

		Assertions.assertEquals("""
			MATCH (p:Person) WHERE p.name = $_v0
			RETURN p""", query.getQuery());
		Assertions.assertEquals("Alice", query.getParams().get("_v0"));
	}

	@Test
	public void optionalMatchWithWhereEmitsSingleWhereKeyword()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);

		final var match = Match.optional(personNode)
			.where(new GreaterThanCondition(personNode.getProperty(PersonNode.BORN), 1980));

		final var query = CypherBuilder.create()
			.match(match)
			.addReturn(personNode)
			.build();

		Assertions.assertEquals("""
			OPTIONAL MATCH (p:Person) WHERE p.born > 1980
			RETURN p""", query.getQuery());
		Assertions.assertTrue(query.getParams().isEmpty());
	}
}
