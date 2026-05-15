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

import com.gregmarut.querybuilder.cypher.condition.Condition;
import com.gregmarut.querybuilder.cypher.node.Node;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@Getter
public class Path extends Condition
{
	private final Node startNode;
	private final List<PartialPattern> partialPatterns;
	
	public Path(final Node startNode)
	{
		this.startNode = startNode;
		this.partialPatterns = new ArrayList<>();
	}
	
	private Path(final Node startNode, final List<PartialPattern> partialPatterns)
	{
		this.startNode = startNode;
		this.partialPatterns = partialPatterns;
	}
	
	/**
	 * Returns a list of all the patterns that make up this path
	 *
	 * @return
	 */
	public List<Pattern> getPatterns()
	{
		final List<Pattern> patterns = new ArrayList<>();
		
		//for each of the partial patterns
		Node lastNode = startNode;
		for (PartialPattern pp : partialPatterns)
		{
			patterns.add(new Pattern(lastNode, pp));
			lastNode = pp.getEndNode();
		}
		
		return patterns;
	}
	
	@Override
	protected String _build(final QueryBuilderContext context)
	{
		//track both instances and identifiers so we catch both the common case (same Node reused) and
		//the case where two distinct Java objects share an identifier (which would still produce a cycle
		//in the rendered cypher)
		final Set<Node> processedNodes = new HashSet<>();
		final Set<String> processedIdentifiers = new HashSet<>();

		final var sb = new StringBuilder();
		sb.append(startNode.build(context));
		processedNodes.add(startNode);
		if (null != startNode.getIdentifier())
		{
			processedIdentifiers.add(startNode.getIdentifier());
		}

		//for each of the partial patterns
		for (PartialPattern pp : partialPatterns)
		{
			final var endNode = pp.getEndNode();
			final var endIdentifier = endNode.getIdentifier();

			final var newInstance = processedNodes.add(endNode);
			final var newIdentifier = (null == endIdentifier) || processedIdentifiers.add(endIdentifier);

			//make sure this node was not already processed by either instance identity or by identifier
			if (newInstance && newIdentifier)
			{
				sb.append(pp.build(context));
			}
			else
			{
				//cyclical reference
				throw new RuntimeException("Cyclical reference detected. Unable to build path.");
			}
		}

		return sb.toString();
	}
	
	@Override
	public Stream<Map.Entry<String, Object>> getParameterStream(final QueryBuilderContext context)
	{
		return Stream.of(startNode.getParameterStream(context), partialPatterns.stream().flatMap(pp -> pp.getParameterStream(context)))
			.flatMap(s -> s);
	}
	
	public static PathBuilder start(final Node startNode)
	{
		return new PathBuilder(startNode);
	}
	
	public static class PathBuilder
	{
		private final Node startNode;
		private final List<PartialPattern> partialPatterns;
		
		public PathBuilder(final Node startNode)
		{
			this.startNode = startNode;
			this.partialPatterns = new ArrayList<>();
		}
		
		public PartialPattern.PartialPatternBuilder.PartialPatternWithRelationshipBuilder<PathBuilder> in(final String relationship)
		{
			return in(relationship, null);
		}
		
		public PartialPattern.PartialPatternBuilder.PartialPatternWithRelationshipBuilder<PathBuilder> in(final String relationship,
			final String identifier)
		{
			return in(Relationship.of(relationship, identifier));
		}
		
		public PartialPattern.PartialPatternBuilder.PartialPatternWithRelationshipBuilder<PathBuilder> in(final Relationship relationship)
		{
			return new PartialPattern.PartialPatternBuilder.PartialPatternWithRelationshipBuilder<>(
				partialPattern -> {
					partialPatterns.add(partialPattern);
					return this;
				}, relationship, Direction.INCOMING);
		}
		
		public PartialPattern.PartialPatternBuilder.PartialPatternWithRelationshipBuilder<PathBuilder> out(final String relationship)
		{
			return out(relationship, null);
		}
		
		public PartialPattern.PartialPatternBuilder.PartialPatternWithRelationshipBuilder<PathBuilder> out(final String relationship,
			final String identifier)
		{
			return out(Relationship.of(relationship, identifier));
		}
		
		public PartialPattern.PartialPatternBuilder.PartialPatternWithRelationshipBuilder<PathBuilder> out(final Relationship relationship)
		{
			return new PartialPattern.PartialPatternBuilder.PartialPatternWithRelationshipBuilder<>(partialPattern -> {
				partialPatterns.add(partialPattern);
				return this;
			}, relationship, Direction.OUTGOING);
		}
		
		public PartialPattern.PartialPatternBuilder.PartialPatternWithDirectionBuilder<PathBuilder> direction(final Direction direction)
		{
			return new PartialPattern.PartialPatternBuilder.PartialPatternWithDirectionBuilder<>(partialPattern -> {
				partialPatterns.add(partialPattern);
				return this;
			}, direction);
		}
		
		public Path build()
		{
			return new Path(startNode, partialPatterns);
		}
	}
}
