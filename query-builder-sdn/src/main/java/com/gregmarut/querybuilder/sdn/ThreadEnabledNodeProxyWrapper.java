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

package com.gregmarut.querybuilder.sdn;

import com.gregmarut.querybuilder.sdn.proxy.NodeProxy;
import org.neo4j.driver.types.MapAccessor;
import org.springframework.data.neo4j.core.mapping.Neo4jPersistentEntity;
import org.springframework.data.neo4j.core.mapping.callback.AfterConvertCallback;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;

@Component
public class ThreadEnabledNodeProxyWrapper implements AfterConvertCallback<Object>
{
	public static final ThreadLocal<Set<Object>> PROXIES = new ThreadLocal<>();
	
	//the set of annotations that are proxyable
	private static final Set<Class<? extends Annotation>> PROXYABLE = Set.of(Node.class);
	
	@Override
	public Object onAfterConvert(final Object instance, final Neo4jPersistentEntity<Object> entity, final MapAccessor source)
	{
		final var proxySet = PROXIES.get();
		
		//make sure the proxy set is not null and that this class is a node
		if (null != proxySet && Arrays.stream(instance.getClass().getDeclaredAnnotations())
			.anyMatch(annotation -> PROXYABLE.contains(annotation.annotationType())))
		{
			//wrap the instance in a proxy
			final var proxy = NodeProxy.createProxy(instance);
			proxySet.add(proxy);
			return proxy;
		}
		else
		{
			return instance;
		}
	}
}
