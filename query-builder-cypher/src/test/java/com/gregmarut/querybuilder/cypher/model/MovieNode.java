/*
 * Copyright 2025 Greg Marut
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

public class MovieNode extends MutableNode<Movie>
{
	public static final String LABEL = "Movie";
	public static final String ID = "id";
	public static final String TITLE = "title";
	public static final String RELEASED = "released";
	public static final String TAGLINE = "tagline";

	public MovieNode()
	{
		super(Movie.class, LABEL);
		setIdField(ID);
	}

	public MovieNode withId(final String id)
	{
		withProperty(ID, id);
		return this;
	}

	public MovieNode withTitle(final String title)
	{
		withProperty(TITLE, title);
		return this;
	}

	public MovieNode withReleased(final Integer released)
	{
		withProperty(RELEASED, released);
		return this;
	}

	public MovieNode withTagline(final String tagline)
	{
		withProperty(TAGLINE, tagline);
		return this;
	}
}
