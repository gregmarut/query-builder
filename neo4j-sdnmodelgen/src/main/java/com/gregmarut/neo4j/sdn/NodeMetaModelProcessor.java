/*
 * Copyright 2024 Greg Marut
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

package com.gregmarut.neo4j.sdn;

import org.springframework.data.neo4j.core.schema.Node;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

@SupportedAnnotationTypes("org.springframework.data.neo4j.core.schema.Node")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class NodeMetaModelProcessor extends AbstractProcessor
{
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
	{
		for (Element element : roundEnv.getElementsAnnotatedWith(Node.class))
		{
			if (element.getKind() == ElementKind.CLASS)
			{
				try
				{
					processNode((TypeElement) element);
				}
				catch (IOException e)
				{
					throw new RuntimeException(e);
				}
			}
		}
		return true;
	}
	
	private void processNode(TypeElement node) throws IOException
	{
		// Generate metamodel class for the Node
		String className = node.getSimpleName() + "_";
		String packageName = processingEnv.getElementUtils().getPackageOf(node).getQualifiedName().toString();
		
		try (Writer writer = processingEnv.getFiler().createSourceFile(packageName + "." + className).openWriter())
		{
			writer.write("package " + packageName + ";\n");
			writer.write("public class " + className + " {\n");
			for (Element enclosed : node.getEnclosedElements())
			{
				if (enclosed.getKind() == ElementKind.FIELD)
				{
					writer.write(
						"    public static final String " + toUpperSnakeCase(enclosed.getSimpleName().toString()) +
						" = \"" + enclosed.getSimpleName() + "\";\n");
				}
			}
			writer.write("}\n");
		}
	}
	
	private String toUpperSnakeCase(final String camelCase)
	{
		StringBuilder result = new StringBuilder();
		char[] chars = camelCase.toCharArray();
		for (char c : chars)
		{
			if (Character.isUpperCase(c))
			{
				result.append("_");
			}
			
			result.append(Character.toUpperCase(c));
		}
		
		return result.toString();
	}
}
