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

package org.apache.openejb.helper.annotation;

import java.util.Map;

public interface IJavaProjectAnnotationFacade {

	/**
	 * Adds an annotation to a class
	 * @param targetClass Fully qualified name of the class to add the annotation to
	 * @param annotation Fully qualified name of the annotation
	 */
	public void addClassAnnotation(String targetClass,	String annotation);

	/**
	 * Adds an annotation to a class, with the specified properties
	 * @param targetClass Fully qualified name of the class to add the annotation to
	 * @param annotation Fully qualified name of the annotation
	 * @param properties Properties for the annotation to be added
	 */
	public void addClassAnnotation(String targetClass, String annotation, Map<String, Object> properties);

	public void addClassAnnotation(String targetClass, Class annotation, Map<String,Object> properties);

	public void addMethodAnnotation(String fullyQualifiedClassName, String methodName, Class annotationClass, Map<String, Object> properties);

	public void addMethodAnnotation(String targetClass, String methodName, String annotationToAdd, Map<String, Object> properties);

	public void addFieldAnnotation(String targetClass, String targetField, Class annotation, Map<String, Object> properties);

	public void addFieldAnnotation(String targetClass, String targetField, String annotation, Map<String, Object> properties);
	
	
}