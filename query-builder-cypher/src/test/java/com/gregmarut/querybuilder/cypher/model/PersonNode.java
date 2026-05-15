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

package com.gregmarut.querybuilder.cypher.model;

import com.gregmarut.querybuilder.cypher.node.MutableNode;

public class PersonNode extends MutableNode<Person>
{
	public static final String LABEL = "Person";
	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String BORN = "born";
	public static final String EMAIL = "email";

	public PersonNode()
	{
		super(Person.class, LABEL);
		setIdField(ID);
	}

	public PersonNode withId(final String id)
	{
		withProperty(ID, id);
		return this;
	}

	public PersonNode withName(final String name)
	{
		withProperty(NAME, name);
		return this;
	}

	public PersonNode withBorn(final Integer born)
	{
		withProperty(BORN, born);
		return this;
	}

	public PersonNode withEmail(final String email)
	{
		withProperty(EMAIL, email);
		return this;
	}
}
