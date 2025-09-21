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

import com.gregmarut.querybuilder.sdn.util.Diff;
import com.gregmarut.querybuilder.sdn.util.ReflectionUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@RequiredArgsConstructor
@Getter
public class NodeProxyAccessor<N>
{
	//the snapshot object that was used when creating the proxy
	private final N snapshot;
	
	//the proxied object
	private final N proxy;
	
	@SneakyThrows
	public void snapshot()
	{
		//copy all of the fields from the proxy object to the snapshot object
		NodeProxyUtil.copyFields(proxy, snapshot);
	}
	
	/**
	 * Returns a set of fields that have changed between the target object and the given object.
	 *
	 * @return a set of fields that have changed
	 */
	public Set<Field> getChangedFields()
	{
		return getDiff().keySet();
	}
	
	/**
	 * Calculates the difference between the snapshot and the proxy object.
	 *
	 * @return a map of fields that have changed and the diff object that contains the old and new values
	 */
	public Map<Field, Diff<Object>> getDiff()
	{
		final Map<Field, Diff<Object>> diff = new HashMap<>();
		
		for (Field field : ReflectionUtil.getAllDeclaredFields(snapshot.getClass()))
		{
			field.setAccessible(true);
			try
			{
				//check to see if the field has changed
				final var oldValue = field.get(snapshot);
				final var newValue = field.get(proxy);
				if (!Objects.equals(oldValue, newValue))
				{
					diff.put(field, new Diff<>(oldValue, newValue));
				}
			}
			catch (IllegalAccessException e)
			{
				throw new RuntimeException(e);
			}
		}
		
		return diff;
	}
	
	public static <N> NodeProxyAccessor<N> extract(final N proxy)
	{
		if (NodeProxy.isProxy(proxy))
		{
			try
			{
				//get the snapshot field from this proxy
				final var snapshotField = NodeProxy.getSnapshotField(proxy);
				
				//get the snapshot object from this proxy
				final var snapshot = (N) snapshotField.get(proxy);
				
				return new NodeProxyAccessor<>(snapshot, proxy);
			}
			catch (IllegalAccessException | NoSuchFieldException e)
			{
				throw new RuntimeException(e);
			}
		}
		else
		{
			throw new IllegalArgumentException("Expected a proxied node. Found: " + proxy.getClass().getName());
		}
	}
}
