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

package com.gregmarut.querybuilder.cypher;

import com.gregmarut.querybuilder.cypher.node.Node;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public class PartialPattern extends CypherString
{
	protected final Relationship relationship;
	protected final Direction direction;
	protected final Node endNode;
	
	public PartialPattern(final PartialPattern partialPattern)
	{
		this(partialPattern.getRelationship(), partialPattern.getDirection(), partialPattern.getEndNode());
	}
	
	@Override
	protected String _build(final QueryBuilderContext context)
	{
		return getDirection().getStart() +
			   getRelationship().build(context) +
			   getDirection().getEnd() +
			   getEndNode().build(context);
	}
	
	@Override
	public Stream<Map.Entry<String, Object>> getParameterStream(final QueryBuilderContext context)
	{
		return Stream.of(relationship.getParameterStream(context), endNode.getParameterStream(context)).flatMap(s -> s);
	}
	
	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		final PartialPattern that = (PartialPattern) o;
		return Objects.equals(relationship, that.relationship) && direction == that.direction && Objects.equals(endNode,
			that.endNode);
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(relationship, direction, endNode);
	}
	
	@RequiredArgsConstructor
	public static class PartialPatternBuilder<R>
	{
		private final Function<PartialPattern, R> transform;
		
		public PartialPatternWithDirectionBuilder<R> direction(final Direction direction)
		{
			return new PartialPatternWithDirectionBuilder<>(transform, direction);
		}
		
		public PartialPatternWithRelationshipBuilder<R> in(final String relationship)
		{
			return in(Relationship.of(relationship));
		}
		
		public PartialPatternWithRelationshipBuilder<R> in(final Relationship relationship)
		{
			return direction(Direction.INCOMING).relationship(relationship);
		}
		
		public PartialPatternWithRelationshipBuilder<R> out(final String relationship)
		{
			return out(Relationship.of(relationship));
		}
		
		public PartialPatternWithRelationshipBuilder<R> out(final Relationship relationship)
		{
			return direction(Direction.OUTGOING).relationship(relationship);
		}
		
		@RequiredArgsConstructor
		public static class PartialPatternWithDirectionBuilder<R>
		{
			private final Function<PartialPattern, R> transform;
			private final Direction direction;
			
			public PartialPatternWithRelationshipBuilder<R> relationship(final String relationship)
			{
				return new PartialPatternWithRelationshipBuilder<>(transform, new Relationship(relationship), direction);
			}
			
			public PartialPatternWithRelationshipBuilder<R> relationship(final Relationship relationship)
			{
				return new PartialPatternWithRelationshipBuilder<>(transform, relationship, direction);
			}
		}
		
		@RequiredArgsConstructor
		public static class PartialPatternWithRelationshipBuilder<R>
		{
			private final Function<PartialPattern, R> transform;
			private final Relationship relationship;
			private final Direction direction;
			
			public R to(final Node endNode)
			{
				return transform.apply(new PartialPattern(relationship, direction, endNode));
			}
		}
	}
}
