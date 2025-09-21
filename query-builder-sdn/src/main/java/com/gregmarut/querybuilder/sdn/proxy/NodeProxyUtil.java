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

package com.gregmarut.querybuilder.sdn.proxy;

import com.gregmarut.querybuilder.sdn.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NodeProxyUtil
{
	public static Class<?> getUserClass(final Class<?> clazz)
	{
		return clazz.getName().contains("$")
			? clazz.getSuperclass()
			: clazz;
	}
	
	public static <T> T clone(final T source)
	{
		try
		{
			// create a new instance of the object
			T target = (T) source.getClass().getDeclaredConstructor().newInstance();
			
			// copy all of the fields from the source object to the target object
			copyFields(source, target);
			
			return target;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public static <T> void copyFields(final T source, final T target)
	{
		//loop through all of the fields in the source object
		for (Field field : ReflectionUtil.getAllDeclaredFields(getUserClass(source.getClass())))
		{
			field.setAccessible(true);
			
			try
			{
				// get the value from the source object
				Object value = field.get(source);
				
				// check if the value is a collection
				if (value instanceof List)
				{
					value = new ArrayList<>((List<?>) value);
				}
				else if (value instanceof Set)
				{
					value = new HashSet<>((Set<?>) value);
				}
				else if (value instanceof Map)
				{
					value = new HashMap<>((Map<?, ?>) value);
				}
				
				// copy the value from the source object to the target object
				field.set(target, value);
			}
			catch (IllegalAccessException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
}
