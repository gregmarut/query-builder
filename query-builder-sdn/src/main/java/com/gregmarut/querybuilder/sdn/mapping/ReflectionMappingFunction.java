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

package com.gregmarut.querybuilder.sdn.mapping;

import com.gregmarut.querybuilder.sdn.util.ReflectionUtil;
import lombok.RequiredArgsConstructor;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.MapAccessor;
import org.neo4j.driver.types.TypeSystem;
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext;
import org.springframework.data.neo4j.core.schema.Node;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

@RequiredArgsConstructor
public class ReflectionMappingFunction<T> implements BiFunction<TypeSystem, MapAccessor, T>
{
	private final Class<T> clazz;
	private final Neo4jMappingContext mappingContext;
	
	@Override
	public T apply(final TypeSystem typeSystem, final MapAccessor mapAccessor)
	{
		try
		{
			//create a new instance of the return type
			final T instance = clazz.getDeclaredConstructor().newInstance();
			
			//for each of the declared fields in this class
			for (Field field : ReflectionUtil.getAllDeclaredFields(clazz))
			{
				final var value = mapAccessor.get(field.getName());
				field.setAccessible(true);
				
				//check to see if this field is null
				if (value.isNull())
				{
					field.set(instance, null);
				}
				else if (field.getType().isAnnotationPresent(Node.class))
				{
					field.set(instance, mappingContext.getRequiredMappingFunctionFor(field.getType()).apply(typeSystem, mapAccessor));
				}
				//check to see if this field is a collection
				else if (Collection.class.isAssignableFrom(field.getType()))
				{
					//get the generic type of this collection to figure out how to map the values
					final var genericType = (ParameterizedType) field.getGenericType();
					final var itemType = (Class<?>) genericType.getActualTypeArguments()[0];
					final var collection = createCollectionInstance(field.getType());
					for (Value v : value.values())
					{
						collection.add(mapSimpleValue(itemType, v));
					}
					
					field.set(instance, collection);
				}
				else
				{
					field.set(instance, mapSimpleValue(field.getType(), value));
				}
			}
			
			return instance;
		}
		catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private Collection<Object> createCollectionInstance(final Class<?> collectionType)
	{
		if (collectionType.isAssignableFrom(List.class))
		{
			return new ArrayList<>();
		}
		else if (collectionType.isAssignableFrom(Set.class))
		{
			return new HashSet<>();
		}
		else
		{
			throw new IllegalArgumentException("Unsupported collection type: " + collectionType);
		}
	}
	
	private Object mapSimpleValue(final Class<?> itemType, final Value value)
	{
		if (itemType.isAssignableFrom(int.class))
		{
			return value.asInt();
		}
		else if (itemType.isAssignableFrom(long.class))
		{
			return value.asLong();
		}
		else if (itemType.isAssignableFrom(float.class))
		{
			return value.asFloat();
		}
		else if (itemType.isAssignableFrom(double.class))
		{
			return value.asDouble();
		}
		else if (itemType.isAssignableFrom(String.class))
		{
			return value.asString();
		}
		else if (itemType.isEnum())
		{
			return Enum.valueOf((Class<Enum>) itemType, value.asString());
		}
		else
		{
			return value.asObject();
		}
	}
}
