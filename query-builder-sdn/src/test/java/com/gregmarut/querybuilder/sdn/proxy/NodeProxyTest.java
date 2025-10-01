/*
 * SkyBit Labs CONFIDENTIAL
 *  ---------------------------
 *  Copyright (c) 2025 SkyBit Labs LLC.
 *  All rights reserved.
 *
 *  NOTICE: All information contained herein is, and remains the property of SkyBit Labs LLC
 *  and its suppliers if any. The intellectual and technical concepts contained herein are
 *  proprietary to SkyBit Labs LLC and its suppliers and may be covered by U.S. and
 *  Foreign Patents, patents in process, and are protected by trade secret or copyright law.
 *  Dissemination of this information or reproduction of this material is strictly forbidden
 *  unless prior written permission is obtained from SkyBit Labs LLC.
 *
 *  Contributors:
 *      Greg Marut
 */

package com.gregmarut.querybuilder.sdn.proxy;

import com.gregmarut.querybuilder.sdn.model.UserNode;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;

class NodeProxyTest
{
	@Test
	void fieldTracking1()
	{
		final var entityNode = new UserNode();
		final var proxy = NodeProxy.createProxy(entityNode);
		
		proxy.setName("Greg");
		Assertions.assertEquals("Greg", proxy.getName());
		
		final var accessor = NodeProxyAccessor.extract(proxy);
		final var changedFields = new ArrayList<>(accessor.getChangedFields());
		changedFields.sort(Comparator.comparing(Field::getName));
		Assertions.assertEquals(1, changedFields.size());
		Assertions.assertEquals("name", changedFields.getFirst().getName());
	}
	
	@Test
	void fieldTracking2()
	{
		final var entityNode = new UserNode();
		entityNode.setId("123");
		entityNode.setName("John");
		entityNode.setUpdated(Instant.now());
		
		final var proxy = NodeProxy.createProxy(entityNode);
		
		proxy.setName("Jonathan");
		proxy.setUpdated(null);
		Assertions.assertEquals("Jonathan", proxy.getName());
		Assertions.assertNull(proxy.getUpdated());
		
		final var accessor = NodeProxyAccessor.extract(proxy);
		final var changedFields = new ArrayList<>(accessor.getChangedFields());
		changedFields.sort(Comparator.comparing(Field::getName));
		Assertions.assertEquals(2, changedFields.size());
		Assertions.assertEquals("name", changedFields.get(0).getName());
		Assertions.assertEquals("updated", changedFields.get(1).getName());
	}
	
	@Test
	@SneakyThrows
	void snapshotFieldTransient()
	{
		final var entityNode = new UserNode();
		entityNode.setId("123");
		
		final var proxy = NodeProxy.createProxy(entityNode);
		final var snapshot = NodeProxy.getSnapshotField(proxy);
		
		Assertions.assertTrue(Modifier.isTransient(snapshot.getModifiers()));
	}
	
	@Test
	void verifyProxyClassesEqual()
	{
		final var entityNode = new UserNode();
		final var proxy1 = NodeProxy.createProxy(entityNode);
		final var proxy2 = NodeProxy.createProxy(entityNode);
		
		Assertions.assertEquals(proxy1.getClass(), proxy2.getClass());
	}
	
	@Test
	void verifyProxiesAreEqual()
	{
		final var entityNode1 = new UserNode();
		entityNode1.setId("123");
		
		final var entityNode2 = new UserNode();
		entityNode2.setId("123");
		
		Assertions.assertEquals(entityNode1, entityNode2);
		
		//create proxies for both of the objects
		final var proxy1 = NodeProxy.createProxy(entityNode1);
		final var proxy2 = NodeProxy.createProxy(entityNode2);
		
		Assertions.assertEquals(proxy1, proxy2);
	}
	
	@Test
	void equalsProxyAndNonProxyObjects1()
	{
		final var entityNode = new UserNode();
		entityNode.setId("123");
		
		final var proxy = NodeProxy.createProxy(entityNode);
		
		Assertions.assertEquals(entityNode, proxy);
		Assertions.assertEquals(proxy, entityNode);
	}
	
	@Test
	void equalsProxyAndNonProxyObjects2()
	{
		final var entityNode = new UserNode();
		entityNode.setId("123");
		
		final var proxy = NodeProxy.createProxy(entityNode);
		
		Assertions.assertEquals(entityNode, proxy);
		Assertions.assertEquals(proxy, entityNode);
	}
}
