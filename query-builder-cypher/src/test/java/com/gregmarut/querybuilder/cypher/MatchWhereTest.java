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
