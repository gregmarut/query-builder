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

package com.gregmarut.mongodb.sdn;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.IOException;
import java.io.Writer;
import java.util.Optional;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

@SupportedAnnotationTypes("org.springframework.data.mongodb.core.mapping.Document")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class DocumentMetaModelProcessor extends AbstractProcessor
{
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
	{
		for (Element element : roundEnv.getElementsAnnotatedWith(Document.class))
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
			processFields(node, writer);
			writer.write("}\n");
		}
	}
	
	private void processFields(TypeElement node, Writer writer) throws IOException
	{
		// Process superclass fields
		TypeElement superclass = (TypeElement) processingEnv.getTypeUtils().asElement(node.getSuperclass());
		if (superclass != null && !superclass.getQualifiedName().toString().equals(Object.class.getName()))
		{
			processFields(superclass, writer);
		}
		
		for (Element enclosed : node.getEnclosedElements())
		{
			if (enclosed.getKind() == ElementKind.FIELD && !enclosed.getModifiers().contains(Modifier.STATIC))
			{
				//set the fieldname to the value of the @Field annotation if it exists, otherwise use the field name as is
				final String fieldName =
					Optional.ofNullable(enclosed.getAnnotation(Field.class))
						.map(Field::value)
						.orElseGet(() -> enclosed.getSimpleName().toString());
				writer.write(
					"    public static final String " + toUpperSnakeCase(enclosed.getSimpleName().toString()) +
						" = \"" + fieldName + "\";\n");
			}
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
