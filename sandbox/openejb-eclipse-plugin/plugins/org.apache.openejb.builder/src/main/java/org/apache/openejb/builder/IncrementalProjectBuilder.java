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

package org.apache.openejb.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class IncrementalProjectBuilder extends org.eclipse.core.resources.IncrementalProjectBuilder {

	public IncrementalProjectBuilder() {
	}

	@SuppressWarnings("unchecked")
	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		getProject().deleteMarkers("org.apache.openejb.builder.dependsonMarker", true, IResource.DEPTH_INFINITE);
		
		CompilationUnitCache cache = new CompilationUnitCache();
		try {
			SingletonDependencyFinder finder = new SingletonDependencyFinder(getProject());
			finder.findDependencies();
			
			String[] singletons = finder.getSingletons();
			IJavaProject javaProject = JavaCore.create(getProject());
			
			for (String singleton : singletons) {
				IType type = javaProject.findType(singleton);
				ISourceRange sourceRange = type.getSourceRange();
				
				CompilationUnit compilationUnit = cache.getCompilationUnit(type.getCompilationUnit());
				TypeDeclaration typeDeclaration = cache.getTypeDeclaration(type);
				IAnnotationBinding[] annotations = typeDeclaration.resolveBinding().getAnnotations();
				
				List<String> declaredDependencies = new ArrayList<String>();
				
				for (IAnnotationBinding annotation : annotations) {
					if ("javax.ejb.DependsOn".equals(annotation.getAnnotationType().getQualifiedName())) {
						IMemberValuePairBinding[] pairs = annotation.getDeclaredMemberValuePairs();
						for (int i = 0; i < pairs.length; i++) { 
							if ("value".equals(pairs[i].getName())) {
								if (pairs[i].getValue() instanceof Object[]) {
									Object[] values = (Object[]) pairs[i].getValue();
									for (int j = 0; j < values.length; j++) {
										declaredDependencies.add(values[j].toString());
									}
								}
							}
						}
					}
				}

				Dependency[] dependencies = finder.getDependencies(singleton);
				List<String> expectedDependencies = new ArrayList<String>();

				if (dependencies != null) {
					for (Dependency dependency : dependencies) {
						expectedDependencies.add(dependency.getDependsOn());
					}
				}
				
				boolean matches = expectedDependencies.size() == declaredDependencies.size();

				if (matches) {
					for (String dependency : declaredDependencies) {
						if (! expectedDependencies.contains(dependency)) {
							matches = false;
							break;
						}
					}
				}
				
				if (! matches) {
					IMarker marker = type.getUnderlyingResource().createMarker("org.apache.openejb.builder.dependsonMarker");
					Map attributes = new HashMap();
					
					attributes.put(IMarker.LINE_NUMBER, compilationUnit.getLineNumber(sourceRange.getOffset()));
					attributes.put(IMarker.CHAR_START, sourceRange.getOffset());
					attributes.put(IMarker.CHAR_END, sourceRange.getOffset() + sourceRange.getLength());
					attributes.put(IMarker.LINE_NUMBER, compilationUnit.getLineNumber(sourceRange.getOffset()));
					attributes.put(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
					attributes.put(IMarker.MESSAGE, expectedDependencies.size() == 0 ? "This bean should not have the @DependsOn annotation" : "This bean requires the @DependsOn annotation, and depends on: " + getDependencyList(expectedDependencies));
					attributes.put(ISingletonDependencyMarker.DEPENDENCIES, expectedDependencies.toArray(new String[expectedDependencies.size()]));
					attributes.put(ISingletonDependencyMarker.BEAN, singleton);
					marker.setAttributes(attributes);
				}
			}
			
		} catch (SingletonDependencyFinderException e) {
			throw new CoreException(new Status(IStatus.ERROR, "org.apache.openejb.builder", e.getMessage(), e));
		}
		
		return new IProject[] { getProject() };
	}

	private String getDependencyList(List<String> dependencies) {
		StringBuffer buffer = new StringBuffer();
		
		for (int i = 0; i < dependencies.size(); i++) {
			if (i > 0) {
				buffer.append(", ");
			}
			
			buffer.append(dependencies.get(i));
		}
		
		return buffer.toString();
	}


}
