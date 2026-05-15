package com.gregmarut.querybuilder.cypher;

import com.gregmarut.querybuilder.cypher.model.PersonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PropertyTest
{
	@Test
	public void propertyWithInvalidNameThrows()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);

		//invalid property name starts with a digit
		Assertions.assertThrows(IllegalArgumentException.class, () -> new Property(personNode, "1bad"));
	}

	@Test
	public void propertyAcceptsValidName()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);

		final var property = new Property(personNode, "firstName_2");
		Assertions.assertEquals("firstName_2", property.getPropertyName());
	}

	@Test
	public void subPropertyAppendsDotName()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);

		final var subProperty = personNode.getProperty("address").getSubProperty("city");
		final var context = QueryBuilderContext.createDefault();

		Assertions.assertEquals("p.address.city", subProperty.build(context));
		Assertions.assertEquals(0, subProperty.getParameterStream(context).count());
	}

	@Test
	public void equalsAndHashCodeForSameIdentifierAndPropertyName()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);

		final var prop1 = personNode.getProperty(PersonNode.NAME);
		final var prop2 = personNode.getProperty(PersonNode.NAME);

		Assertions.assertEquals(prop1, prop2);
		Assertions.assertEquals(prop1.hashCode(), prop2.hashCode());
	}

	@Test
	public void buildVariableUsesContextVariableName()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);

		final var context = QueryBuilderContext.createDefault();
		final var prop = personNode.getProperty(PersonNode.NAME);

		Assertions.assertEquals("_v0_name", prop.buildVariable(context));
	}
}
