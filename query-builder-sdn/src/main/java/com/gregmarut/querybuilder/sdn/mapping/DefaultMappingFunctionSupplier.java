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

import lombok.RequiredArgsConstructor;
import org.neo4j.driver.types.MapAccessor;
import org.neo4j.driver.types.TypeSystem;
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class DefaultMappingFunctionSupplier implements MappingFunctionSupplier
{
	private final Map<Class<?>, BiFunction<TypeSystem, MapAccessor, ?>> customMappingFunctions;
	private final Neo4jMappingContext mappingContext;
	
	public Supplier<BiFunction<TypeSystem, MapAccessor, ?>> getMappingFunction(final Class<?> returnedType)
	{
		return () -> {
			final BiFunction<TypeSystem, MapAccessor, ?> mappingFunction;
			
			if (mappingContext.getConversionService().isSimpleType(returnedType))
			{
				mappingFunction = null;
			}
			else if (customMappingFunctions.containsKey(returnedType))
			{
				mappingFunction = customMappingFunctions.get(returnedType);
			}
			else
			{
				mappingFunction = mappingContext.getRequiredMappingFunctionFor(returnedType);
			}
			
			return mappingFunction;
		};
	}
}
