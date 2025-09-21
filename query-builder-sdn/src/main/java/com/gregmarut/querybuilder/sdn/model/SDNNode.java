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

package com.gregmarut.querybuilder.sdn.model;

import com.gregmarut.querybuilder.cypher.NodeType;
import com.gregmarut.querybuilder.cypher.node.MutableNode;
import com.gregmarut.querybuilder.sdn.proxy.NodeProxyUtil;
import com.gregmarut.querybuilder.sdn.util.ReflectionUtil;
import com.gregmarut.querybuilder.sdn.util.SDNUtil;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.Optional;
import java.util.Set;

/**
 * Represents an SDN Node in a Neo4j graph database.
 * Inherits from the Node class and adds functionality specific to SDN nodes.
 */
@Getter
public class SDNNode<N extends BaseNode> extends MutableNode<N>
{
	private SDNNode(final Class<N> nodeClass)
	{
		super(nodeClass, getLabel(nodeClass));
		
		//find the ID field on this node
		setIdField(SDNUtil.getIDField(nodeClass).getName());
	}
	
	@SneakyThrows
	private SDNNode(final Class<N> nodeClass, final N node, boolean withOnlyIdProperty)
	{
		this(nodeClass);
		
		//check to see if properties other than the id should be included
		if (!withOnlyIdProperty)
		{
			//read all of the non-null properties from the given object and add them to this node
			SDNUtil.extractModifiedProperties(node).forEach(this::withProperty);
		}
		
		//check to see if the id field is not yet set
		if (!getProperties().containsKey(getIdField()))
		{
			//make sure the id field is set
			final var id = ReflectionUtil.getDeclaredField(NodeProxyUtil.getUserClass(node.getClass()), getIdField());
			SDNUtil.extractProperties(node, Set.of(id), false).forEach(this::withProperty);
		}
		
		//make sure the id field is set
		if (null == getProperties().get(getIdField()))
		{
			throw new IllegalArgumentException("ID field " + getIdField() + " not found on " + node.getClass().getName());
		}
	}
	
	@Override
	public NodeType getNodeType()
	{
		return NodeType.NODE;
	}
	
	public static <N extends BaseNode> SDNNode<N> of(final Class<N> nodeClass)
	{
		return new SDNNode<>(nodeClass);
	}
	
	/**
	 * Creates a new instance of the {@code SDNNode} class based on the given {@code INode} object and all non-null fields as properties.
	 *
	 * @param node the node object for which to create the {@code SDNNode} instance
	 * @return a new {@code SDNNode} instance
	 */
	public static <N extends BaseNode> SDNNode<N> from(final N node)
	{
		return new SDNNode<>(SDNUtil.getOriginalClass(node), node, false);
	}
	
	/**
	 * Creates a new instance of the {@code SDNNode} class with only the ID property.
	 *
	 * @param node the node object for which to create the {@code SDNNode} instance
	 * @return a new {@code SDNNode} instance with only the ID property
	 */
	public static <N extends BaseNode> SDNNode<N> withIdPropertyOnly(final N node)
	{
		return new SDNNode<>(SDNUtil.getOriginalClass(node), node, true);
	}
	
	private static String getLabel(final Class<?> nodeClass)
	{
		//extract the NodeEntity annotation from the node class
		return Optional.ofNullable(nodeClass.getDeclaredAnnotation(org.springframework.data.neo4j.core.schema.Node.class))
			.map(org.springframework.data.neo4j.core.schema.Node::value)
			.map(labels -> labels[0])
			.orElseThrow(() -> new IllegalArgumentException("No @Node annotation found on " + nodeClass.getName()));
	}
}
