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

package com.gregmarut.querybuilder.sdn.util;

import com.gregmarut.querybuilder.cypher.Direction;
import com.gregmarut.querybuilder.cypher.IdentifierGenerator;
import com.gregmarut.querybuilder.cypher.Path;
import com.gregmarut.querybuilder.cypher.node.Node;
import com.gregmarut.querybuilder.sdn.model.BaseNode;
import com.gregmarut.querybuilder.sdn.model.SDNNode;
import com.gregmarut.querybuilder.sdn.proxy.NodeProxy;
import com.gregmarut.querybuilder.sdn.proxy.NodeProxyAccessor;
import com.gregmarut.querybuilder.sdn.proxy.NodeProxyUtil;
import lombok.Getter;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SDNUtil
{
	//cache the id field for each node class
	private static final Map<Class<? extends BaseNode>, Field> ID_FIELD_CACHE = new ConcurrentHashMap<>();
	
	public static Field getIDField(final BaseNode node)
	{
		return getIDField(getOriginalClass(node));
	}
	
	public static Field getIDField(final Class<? extends BaseNode> nodeClass)
	{
		return ID_FIELD_CACHE.computeIfAbsent(nodeClass, c -> ReflectionUtil.getAllDeclaredFields(c).stream()
			.filter(field -> field.getDeclaredAnnotation(org.springframework.data.neo4j.core.schema.Id.class) != null)
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("No @Id field found on " + nodeClass.getName())));
	}
	
	/**
	 * Gets the original class of the given node object regardless whether it is a proxy or not.
	 *
	 * @param node the node object for which to get the original class
	 * @param <N>  the type of node
	 * @return the original class of the given node object
	 */
	public static <N extends BaseNode> Class<N> getOriginalClass(final N node)
	{
		return (Class<N>) NodeProxyUtil.getUserClass(node.getClass());
	}
	
	/**
	 * Extracts the properties from the given {@link BaseNode} object that are considered to be modified. If the node is a proxy, then the modified
	 * fields were tracked by the proxy and only those fields will be returned. If the node is not a proxy, then all of the non-null fields will be
	 * considered modified.
	 *
	 * @param node the {@link BaseNode} object from which to extract the properties
	 * @return
	 */
	public static Map<String, Object> extractModifiedProperties(final BaseNode node)
	{
		//check to see if this node is a proxy
		if (NodeProxy.isProxy(node))
		{
			//retrieve the accessor for this proxy and extract all of the changed properties, even if null
			final var accessor = NodeProxyAccessor.extract(node);
			return extractProperties(node, accessor.getChangedFields(), true);
		}
		else
		{
			//extract all of the non-null properties from this node
			return extractProperties(node, Set.copyOf(ReflectionUtil.getAllDeclaredFields(node.getClass())), false);
		}
	}
	
	/**
	 * Extracts the relationships from the given {@link BaseNode} object that are considered modified. If the node is a proxy, then the non-null,
	 * modified relationship fields were tracked by the proxy and only those fields will be returned. If the node is not a proxy, then all of the
	 * non-null relationship fields will be considered modified.
	 *
	 * @param node the {@link BaseNode} object from which to extract the properties
	 * @return
	 */
	public static List<RelationshipField<BaseNode>> extractModifiedRelationships(final BaseNode node)
	{
		//check to see if this node is a proxy
		if (NodeProxy.isProxy(node))
		{
			final var accessor = NodeProxyAccessor.extract(node);
			
			//extract only of the non-null relationships that have changed from this node
			return extractNonNullRelationships(node, accessor.getChangedFields());
		}
		else
		{
			//extract all of the non-null relationships from this node
			return extractNonNullRelationships(node, Set.copyOf(ReflectionUtil.getAllDeclaredFields(node.getClass())));
		}
	}
	
	/**
	 * Extracts the non-null properties from the given {@link BaseNode} object and returns them as a {@link Map}. Fields annotated with
	 * {@link Relationship} are ignored.
	 *
	 * @param node the {@link BaseNode} object from which to extract the properties
	 * @return a {@link Map} containing the non-null properties of the {@code node}
	 */
	public static Map<String, Object> extractProperties(final BaseNode node, final Set<Field> fields, final boolean allowNullValues)
	{
		final var entries = fields.stream()
			//ignore transient fields
			.filter(field -> !Modifier.isTransient(field.getModifiers()))
			//filter out all of the fields that are annotated with @Relationship
			.filter(field -> null == field.getDeclaredAnnotation(Relationship.class))
			//map the field to a key value pair of the field name and the value of that field
			.map(field -> new AbstractMap.SimpleEntry<>(field.getName(), getFieldValue(field, node)))
			.toList();
		
		final var properties = new HashMap<String, Object>();
		entries.stream()
			//filter out all of the fields that have a null value unless allowNullValues is true
			.filter(entry -> allowNullValues || null != entry.getValue())
			//add all of the properties to the map
			.forEach(e -> properties.put(e.getKey(), e.getValue()));
		
		return properties;
	}
	
	/**
	 * Extracts the non-null relationships from the given {@link BaseNode} object and returns them as a flat list of {@link RelationshipField}.
	 *
	 * @param node the {@link BaseNode} object from which to extract the relationships
	 * @return a list of {@link RelationshipField} representing the non-null relationships of the {@code node}
	 */
	private static List<RelationshipField<BaseNode>> extractNonNullRelationships(final BaseNode node, final Set<Field> fields)
	{
		return fields.stream()
			//ignore transient fields
			.filter(field -> !Modifier.isTransient(field.getModifiers()))
			//filter out all of the fields that are not annotated with @Relationship
			.filter(field -> null != field.getDeclaredAnnotation(Relationship.class))
			//map the field to a key value pair of the field name and the value of that field
			.map(field -> new RelationshipField<>(field.getDeclaredAnnotation(Relationship.class), getFieldValue(field, node)))
			//filter out all of the fields that have a null value
			.filter(relationshipField -> null != relationshipField.target)
			//flatten the list of relationships into a single list
			.flatMap(relationship -> {
				if (Collection.class.isAssignableFrom(relationship.target.getClass()))
				{
					final var c = (Collection<?>) relationship.target;
					return c.stream().map(value -> toRelationshipField(relationship.annotation, value));
				}
				else
				{
					return Stream.of(toRelationshipField(relationship.annotation, relationship.target));
				}
			})
			.toList();
	}
	
	/**
	 * Converts a given {@link BaseNode} object and a collection of {@link RelationshipField} objects into a list of {@link Path} objects.
	 *
	 * @param node               the {@link SDNNode} object as the source of the pattern
	 * @param relationshipFields a collection of {@link RelationshipField} objects representing the relationships of the {@code node}
	 * @return a list of {@link Path} objects representing the paths formed by the {@code node} and its relationships
	 */
	public static List<Path> toPaths(final SDNNode<?> node, final Collection<RelationshipField<BaseNode>> relationshipFields,
		final IdentifierGenerator generator)
	{
		return relationshipFields.stream()
			.map(r -> {
				final com.gregmarut.querybuilder.cypher.Relationship relationship =
					new com.gregmarut.querybuilder.cypher.Relationship(Objects.requireNonNull(r.getAnnotation().value()));
				relationship.getProperties().putAll(r.getProperties());
				final Node endNode = SDNNode.withIdPropertyOnly(r.getTarget()).named(generator);
				final Direction direction = switch (r.getAnnotation().direction())
				{
					case INCOMING -> Direction.INCOMING;
					case OUTGOING -> Direction.OUTGOING;
				};
				
				return Path.start(node).direction(direction).relationship(relationship).to(endNode).build();
			})
			.toList();
	}
	
	private static RelationshipField<BaseNode> toRelationshipField(final Relationship annotation, final Object value)
	{
		//check to see if the value is an INode object
		if (value instanceof BaseNode n)
		{
			return new RelationshipField<>(annotation, n);
		}
		//check to see if the value is a relationship
		else if (isRelationship(value))
		{
			//find all of the properties on the relationship object
			final var fields = ReflectionUtil.getAllDeclaredFields(NodeProxyUtil.getUserClass(value.getClass()));
			
			//find the id field and ensure that there is exactly 1 id field
			final var idList = fields.stream().filter(f -> findAnnotation(f.getDeclaredAnnotations(), Id.class).isPresent()).toList();
			if (idList.size() != 1)
			{
				throw new IllegalArgumentException("Relationship objects must have exactly 1 @Id field.");
			}
			final var idField = idList.getFirst();
			
			//find the target object and ensure that there is exactly 1 target node
			final var targetList = fields.stream().filter(f -> findAnnotation(f.getDeclaredAnnotations(), TargetNode.class).isPresent()).toList();
			if (targetList.size() != 1)
			{
				throw new IllegalArgumentException("Relationship objects must have exactly 1 field annotated with @TargetNode.");
			}
			final var targetField = targetList.getFirst();
			final var targetNode = validateIsNode(getFieldValue(targetField, value));
			
			//find all of the other properties on this relationship
			final var properties = fields.stream()
				.filter(f -> !f.equals(targetField) && !f.equals(idField))
				.map(f -> new AbstractMap.SimpleEntry<>(f.getName(), getFieldValue(f, value)))
				.filter(e -> null != e.getValue())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
			
			return new RelationshipField<>(annotation, targetNode, properties);
		}
		else
		{
			throw new IllegalArgumentException(
				"Only INode objects or classes annotated with @RelationshipProperties are supported for relationships. Found: " + value.getClass()
					.getName());
		}
	}
	
	private static BaseNode validateIsNode(final Object value)
	{
		if (value instanceof BaseNode n)
		{
			return n;
		}
		else
		{
			final var className = Optional.ofNullable(value).map(Object::getClass).map(Class::getName).orElse(null);
			throw new IllegalArgumentException("Only INode objects are supported. Found: " + className);
		}
	}
	
	/**
	 * Determines if the given object is a relationship object.
	 *
	 * @param value the object to check
	 * @return true if the object is a relationship object; false otherwise
	 */
	private static boolean isRelationship(final Object value)
	{
		return findAnnotation(NodeProxyUtil.getUserClass(value.getClass()).getDeclaredAnnotations(), RelationshipProperties.class).isPresent();
	}
	
	private static Optional<Annotation> findAnnotation(final Annotation[] annotations, final Class<? extends Annotation> annotationClass)
	{
		return Arrays.stream(annotations)
			.filter(a -> a.annotationType().equals(annotationClass))
			.findFirst();
	}
	
	private static Object getFieldValue(final Field field, final Object node)
	{
		try
		{
			field.setAccessible(true);
			return field.get(node);
		}
		catch (IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	@Getter
	public static final class RelationshipField<V>
	{
		private final Relationship annotation;
		private final V target;
		private final Map<String, Object> properties;
		
		public RelationshipField(final Relationship annotation, final V target)
		{
			this(annotation, target, Map.of());
		}
		
		public RelationshipField(final Relationship annotation, final V target, final Map<String, Object> properties)
		{
			this.annotation = annotation;
			this.target = target;
			this.properties = properties;
		}
	}
}
