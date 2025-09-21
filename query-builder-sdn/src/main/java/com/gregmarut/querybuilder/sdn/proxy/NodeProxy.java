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

import lombok.Getter;
import lombok.SneakyThrows;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.FieldPersistence;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A proxy that is capable of tracking changes to the fields of an object.
 */
@Getter
public class NodeProxy
{
	public static final String SNAPSHOT_FIELD_NAME = "__snapshot";
	
	private static final Map<Class<?>, Class<?>> PROXY_CLASS_MAP = new ConcurrentHashMap<>();
	
	public static boolean isProxy(final Object object)
	{
		return object.getClass().getName().contains("$ByteBuddy$");
	}
	
	@SneakyThrows
	@SuppressWarnings("unchecked")
	public static <T> T createProxy(final T target)
	{
		final Class<T> targetClass = (Class<T>) target.getClass();
		
		//get or create the proxy class
		final Class<T> proxyClass = (Class<T>) PROXY_CLASS_MAP.computeIfAbsent(targetClass, key -> {
			try (var unloaded = new ByteBuddy()
				.subclass(targetClass)
				.defineField(SNAPSHOT_FIELD_NAME, targetClass, FieldPersistence.TRANSIENT)
				.make())
			{
				return unloaded
					.load(targetClass.getClassLoader())
					.getLoaded();
			}
		});
		
		// Create the proxy object using ByteBuddy
		final T proxy = proxyClass
			.getDeclaredConstructor()
			.newInstance();
		
		//copy all of the fields from the target object to the proxy object
		NodeProxyUtil.copyFields(target, proxy);
		
		//set the snapshot object as a clone of the target
		getSnapshotField(proxy).set(proxy, NodeProxyUtil.clone(target));
		
		return proxy;
	}
	
	static Field getSnapshotField(final Object proxy) throws NoSuchFieldException
	{
		//find the snapshot field from this proxy
		final var snapshotField = proxy.getClass().getDeclaredField(SNAPSHOT_FIELD_NAME);
		snapshotField.setAccessible(true);
		return snapshotField;
	}
}
