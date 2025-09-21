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

import com.gregmarut.querybuilder.sdn.model.View;
import lombok.SneakyThrows;
import org.neo4j.driver.types.MapAccessor;
import org.neo4j.driver.types.TypeSystem;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class ReflectionMappingFunctionFactory
{
	/**
	 * Creates a new mapping function supplier that scans the given package for classes annotated with @View
	 * and creates a new reflection mapping function for each class found.
	 *
	 * @param mappingContext the Neo4j mapping context
	 * @param viewPackage    the package to scan for @View classes
	 * @return a new mapping function supplier
	 */
	public static MappingFunctionSupplier mappingFunctionSupplier(final Neo4jMappingContext mappingContext, final String viewPackage)
	{
		final Map<Class<?>, BiFunction<TypeSystem, MapAccessor, ?>> customMappingFunctions = new HashMap<>();
		
		//create a classpath scanning provider to find all classes annotated with @View
		final var classpathScanningProvider = new ClassPathScanningCandidateComponentProvider(false);
		classpathScanningProvider.addIncludeFilter(new AnnotationTypeFilter(View.class));
		final var results = classpathScanningProvider.findCandidateComponents(viewPackage);
		
		//for each class found, create a new reflection mapping function
		results.stream().map(ReflectionMappingFunctionFactory::getClassFromBeanDefinition)
			.forEach(clazz -> customMappingFunctions.put(clazz, new ReflectionMappingFunction<>(clazz, mappingContext)));
		
		//return the default mapping function supplier
		return new DefaultMappingFunctionSupplier(customMappingFunctions, mappingContext);
	}
	
	@SneakyThrows
	private static Class<?> getClassFromBeanDefinition(final BeanDefinition beanDefinition)
	{
		return Class.forName(beanDefinition.getBeanClassName());
	}
}
