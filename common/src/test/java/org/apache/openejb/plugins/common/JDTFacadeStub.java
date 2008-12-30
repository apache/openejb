/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.plugins.common;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JDTFacadeStub implements IJDTFacade {

	public void addAnnotationToFieldsOfType(String targetClass, Class<?> annotation, Map<String, Object> properties) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[ADD ANNOTATIONS TO FIELDS OF TYPE] Target field type: ");
		buffer.append(targetClass);
		buffer.append("\nAnnotation: ");
		buffer.append(annotation.getCanonicalName());
		buffer.append("\nProperies: {\n");
		writeMapToBuffer(properties, buffer, 1);
		buffer.append("}\n");
		
		System.out.println(buffer.toString());
	}

	@SuppressWarnings("unchecked")
	private void writeMapToBuffer(Map<String, Object> properties, StringBuffer buffer, int indent) {
		if (properties == null) {
			return;
		}
		
		StringBuffer indentStrBuf = new StringBuffer();
		for (int i = 0; i < indent; i++) {
			indentStrBuf.append("\t");
		}
		
		String indentStr = indentStrBuf.toString();
		
		Iterator<String> iterator = properties.keySet().iterator();
		while (iterator.hasNext()) {
			String key = (String) iterator.next();
			buffer.append(indentStr);
			buffer.append(key);
			buffer.append("=");
			
			Object value = properties.get(key);
			if (value == null) {
			
			} else if (value instanceof Map) {
				buffer.append(" {\n");
				writeMapToBuffer((Map<String, Object>) value, buffer, indent + 1);
				buffer.append(indentStr);
				buffer.append("}");
			} else if (value.getClass().isEnum()) {
				buffer.append(value.getClass().getCanonicalName());
				buffer.append(".");
				buffer.append(value.toString());
			} else {
				buffer.append(value.toString());
			}
			
			buffer.append("\n");
		}
	}

	public void addClassAnnotation(String targetClass, Class<? extends Annotation> annotation, Map<String, Object> properties) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[ADD CLASS ANNOTATION] Target class: ");
		buffer.append(targetClass);
		buffer.append("\nAnnotation: ");
		buffer.append(annotation.getCanonicalName());
		buffer.append("\nProperies: {\n");
		writeMapToBuffer(properties, buffer, 1);
		buffer.append("}\n");
		
		System.out.println(buffer.toString());
	}

	public void addField(String targetClass, String fieldName, String fieldType) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[ADD FIELD] Target class: ");
		buffer.append(targetClass);
		buffer.append("\n");
		buffer.append("Field type: ");
		buffer.append(fieldType);
		buffer.append("\n");
		buffer.append("Field name: ");
		buffer.append(fieldName);
		buffer.append("\n");
		System.out.println(buffer.toString());
	}

	public void addFieldAnnotation(String targetClass, String targetField, Class<?> annotation, Map<String, Object> properties) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[ADD FIELD ANNOTATION] Target field type: ");
		buffer.append(targetClass);
		buffer.append("\nTarget field: ");
		buffer.append(targetField);
		buffer.append("\nAnnotation: ");
		buffer.append(annotation.getCanonicalName());
		buffer.append("\nProperies: {\n");
		writeMapToBuffer(properties, buffer, 1);
		buffer.append("}\n");
		
		System.out.println(buffer.toString());
	}

	public void addInterface(String ejbClass, String interfaceClass) {
		// TODO Auto-generated method stub

	}

	public void addMethodAnnotation(String fullyQualifiedClassName, String methodName, String[] signature, Class<?> annotationClass, Map<String, Object> properties) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[ADD METHOD ANNOTATION] Target field type: ");
		buffer.append(fullyQualifiedClassName);
		buffer.append("\nTarget field: ");
		buffer.append(methodName);
		buffer.append("\nAnnotation: ");
		buffer.append(annotationClass.getCanonicalName());
		buffer.append("\nProperties: {\n");
		writeMapToBuffer(properties, buffer, 1);
		buffer.append("}\n");
		
		System.out.println(buffer.toString());
	}

	public void addWarning(String warning) {
		// TODO Auto-generated method stub

	}

	public boolean classImplements(String targetClass, String targetInterface) {
		// TODO Auto-generated method stub
		return false;
	}

	public String getMethodReturnType(String targetClass, String methodName, String[] signature) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getSuperClass(String targetClass) {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeAbstractModifierFromClass(String targetClass) {
		// TODO Auto-generated method stub

	}

	public void removeAbstractModifierFromMethod(String targetClass, String methodName, String[] signature, String methodBody) {
		// TODO Auto-generated method stub

	}

	public void removeInterface(String targetClass, String interfaceToRemove) {
		// TODO Auto-generated method stub

	}

	public void convertMethodToConstructor(String className, String methodName, String[] signature) {
	}

	public void changeInvocationsToConstructor(String fromClass, final String fromMethodName, String[] fromSignature, final String toClass) {
	}

	public List<String[]> getSignatures(String type, String methodName) {
		return null;
	}

	public void changeInvocationsTo(String fromClass, String fromMethodName, String[] fromSignature, String code) {
		throw new UnsupportedOperationException();
	}

	public void addCodeToEndOfMethod(String className, String methodName, String[] signature, String code) {
		// TODO Auto-generated method stub
		
	}

	public void addCodeToStartOfMethod(String className, String methodName, String[] signature, String code) {
		// TODO Auto-generated method stub
		
	}

	public boolean isTypeCollection(String returnType) {
		// TODO Auto-generated method stub
		return false;
	}

}
