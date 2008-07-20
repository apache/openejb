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
import java.util.Map;

public interface IJDTFacade {
	void addClassAnnotation(String targetClass, Class<? extends Annotation> annotation, Map<String,Object> properties);
	void addMethodAnnotation(String fullyQualifiedClassName, String methodName, String[] signature, Class<?> annotationClass, Map<String, Object> properties);
	void addFieldAnnotation(String targetClass, String targetField, Class<?> annotation, Map<String, Object> properties);
	void removeInterface(String targetClass, String interfaceToRemove);
	void removeAbstractModifierFromClass(String targetClass);
	void removeAbstractModifierFromMethod(String targetClass, String methodName, String[] signature, String methodBody);
	String getSuperClass(String targetClass);
	boolean classImplements(String targetClass, String targetInterface);
	String getMethodReturnType(String targetClass, String methodName, String[] signature);
	void addField(String targetClass, String fieldName, String fieldType);
	void addAnnotationToFieldsOfType(String targetClass, Class<?> annotation, Map<String, Object> properties);
	void addInterface(String ejbClass, String interfaceClass);
	void addWarning(String warning);
}
