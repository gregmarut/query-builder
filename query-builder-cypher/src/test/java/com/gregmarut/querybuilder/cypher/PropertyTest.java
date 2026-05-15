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
	public void subPropertyRejectsInvalidName()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);
		final var address = personNode.getProperty("address");

		//leading digit is invalid
		Assertions.assertThrows(IllegalArgumentException.class, () -> address.getSubProperty("1bad"));
		//special characters that would inject into the query are rejected
		Assertions.assertThrows(IllegalArgumentException.class, () -> address.getSubProperty("city; DROP"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> address.getSubProperty("has-dash"));
	}

	@Test
	public void subPropertyAcceptsValidNames()
	{
		final var identifierGenerator = new IdentifierGenerator();
		final var personNode = new PersonNode().named(identifierGenerator);
		final var address = personNode.getProperty("address");

		//valid names should not throw
		Assertions.assertDoesNotThrow(() -> address.getSubProperty("city"));
		Assertions.assertDoesNotThrow(() -> address.getSubProperty("postal_code"));
		Assertions.assertDoesNotThrow(() -> address.getSubProperty("line2"));
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
