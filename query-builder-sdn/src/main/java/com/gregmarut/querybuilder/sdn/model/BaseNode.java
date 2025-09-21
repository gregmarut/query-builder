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

import com.gregmarut.querybuilder.sdn.proxy.NodeProxyUtil;
import com.gregmarut.querybuilder.sdn.util.SDNUtil;
import lombok.SneakyThrows;

import java.util.Objects;

public abstract class BaseNode
{
	@Override
	@SneakyThrows
	public final boolean equals(final Object that)
	{
		if (this == that)
		{
			return true;
		}
		
		if (that == null)
		{
			return false;
		}
		
		final var thisClass = NodeProxyUtil.getUserClass(getClass());
		final var thatClass = NodeProxyUtil.getUserClass(that.getClass());
		
		if (thisClass != thatClass)
		{
			return false;
		}
		
		//find the id field
		final var idField = SDNUtil.getIDField(this);
		idField.setAccessible(true);
		
		final var thisId = idField.get(this);
		final var thatId = idField.get(that);
		
		return Objects.equals(thisId, thatId);
	}
	
	@Override
	@SneakyThrows
	public final int hashCode()
	{
		//find the id field
		final var idField = SDNUtil.getIDField(this);
		idField.setAccessible(true);
		
		return Objects.hashCode(idField.get(this));
	}
}
