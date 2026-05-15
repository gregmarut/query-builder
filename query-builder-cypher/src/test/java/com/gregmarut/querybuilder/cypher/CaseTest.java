package com.gregmarut.querybuilder.cypher;

import com.gregmarut.querybuilder.cypher.condition.GreaterThanCondition;
import com.gregmarut.querybuilder.cypher.model.MovieNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class CaseTest
{
	@BeforeAll
	static void setUp()
	{
		QueryBuilderContext.defaultPrettyPrint = true;
	}

	@Test
	public void caseWithSingleWhenThen()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var movieNode = new MovieNode().named(identifierGenerator);

		final var caseExpression = Case.builder()
			.whenThen(
				new GreaterThanCondition(movieNode.getProperty(MovieNode.RELEASED), 2000),
				LiteralCypherString.of("'modern'")
			)
			.build();

		final var context = QueryBuilderContext.createDefault();
		final var result = caseExpression.build(context);

		Assertions.assertEquals("""
			CASE
				WHEN m.released > 2000 THEN 'modern'
			END""", result);
	}

	@Test
	public void caseWithMultipleWhenThenAndElse()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var movieNode = new MovieNode().named(identifierGenerator);

		final var caseExpression = Case.builder()
			.whenThen(
				new GreaterThanCondition(movieNode.getProperty(MovieNode.RELEASED), 2000),
				LiteralCypherString.of("'modern'")
			)
			.whenThen(
				new GreaterThanCondition(movieNode.getProperty(MovieNode.RELEASED), 1950),
				LiteralCypherString.of("'classic'")
			)
			.elseReturn(LiteralCypherString.of("'ancient'"))
			.build();

		final var context = QueryBuilderContext.createDefault();
		final var result = caseExpression.build(context);

		Assertions.assertEquals("""
			CASE
				WHEN m.released > 2000 THEN 'modern'
				WHEN m.released > 1950 THEN 'classic'
				ELSE 'ancient'
			END""", result);
	}

	@Test
	public void caseEmptyWhenThenThrows()
	{
		final var caseExpression = Case.builder().build();
		final var context = QueryBuilderContext.createDefault();
		Assertions.assertThrows(IllegalStateException.class, () -> caseExpression.build(context));
	}

	@Test
	public void whenBuilderFluentApi()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var movieNode = new MovieNode().named(identifierGenerator);

		final var caseExpression = Case.builder()
			.when(new GreaterThanCondition(movieNode.getProperty(MovieNode.RELEASED), 2000))
			.then(LiteralCypherString.of("'modern'"))
			.elseReturn(LiteralCypherString.of("'old'"))
			.build();

		final var context = QueryBuilderContext.createDefault();
		final var result = caseExpression.build(context);

		Assertions.assertEquals("""
			CASE
				WHEN m.released > 2000 THEN 'modern'
				ELSE 'old'
			END""", result);
	}
}
