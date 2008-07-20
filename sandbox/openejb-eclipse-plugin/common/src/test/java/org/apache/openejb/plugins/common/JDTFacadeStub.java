package org.apache.openejb.plugins.common;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Map;

public class JDTFacadeStub implements IJDTFacade {

	public void addAnnotationToFieldsOfType(String targetClass, Class<?> annotation, Map<String, Object> properties) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Target field type: ");
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
			if (value instanceof Map) {
				buffer.append(" {\n");
				writeMapToBuffer((Map<String, Object>) value, buffer, indent + 1);
				buffer.append(indentStr);
				buffer.append("}");
			} else {
				buffer.append(value.toString());
			}
			
			buffer.append("\n");
		}
	}

	public void addClassAnnotation(String targetClass, Class<? extends Annotation> annotation, Map<String, Object> properties) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Target class: ");
		buffer.append(targetClass);
		buffer.append("\nAnnotation: ");
		buffer.append(annotation.getCanonicalName());
		buffer.append("\nProperies: {\n");
		writeMapToBuffer(properties, buffer, 1);
		buffer.append("}\n");
		
		System.out.println(buffer.toString());
	}

	public void addField(String targetClass, String fieldName, String fieldType) {
		// TODO Auto-generated method stub

	}

	public void addFieldAnnotation(String targetClass, String targetField, Class<?> annotation, Map<String, Object> properties) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Target field type: ");
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
		buffer.append("Target field type: ");
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

}
