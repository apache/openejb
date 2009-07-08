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
import java.util.Iterator;
import java.util.List;

import org.apache.openejb.devtools.core.CompilationUnitCache;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;

public class SingletonDependencyFinder {

	private final ProjectModel model;

	public class SingletonBeanSearchRequestor extends SearchRequestor {
		private List<SearchMatch> matches = new ArrayList<SearchMatch>();

		@Override
		public void acceptSearchMatch(SearchMatch match) throws CoreException {
			IJavaElement javaElement = (IJavaElement) ((PlatformObject) (match.getElement())).getAdapter(IJavaElement.class);
			if (IJavaElement.TYPE == javaElement.getElementType()) {
				matches.add(match);
			}
		}

		public SearchMatch[] getMatches() {
			return matches.toArray(new SearchMatch[matches.size()]);
		}
	}

	public SingletonDependencyFinder(ProjectModel model) {
		super();
		this.model = model;
		javaProject = getProject();
	}

	private IJavaProject getProject() {
		String projectName = model.getProjectName();
		
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (project == null) {
			return null;
		}
		
		return JavaCore.create(project);
	}

	private IJavaProject javaProject;
	private CompilationUnitCache compilationUnitCache = new CompilationUnitCache(javaProject);

	public String[] findSingletons() throws SingletonDependencyFinderException {
		List<String> results = new ArrayList<String>();
		
		try {
			IType singleton = javaProject.findType("javax.ejb.Singleton");
			
			SearchEngine searchEngine = new SearchEngine();
			SearchPattern pattern = SearchPattern.createPattern(singleton, IJavaSearchConstants.REFERENCES);
			SearchParticipant[] participants = new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() };
			IJavaSearchScope scope = SearchEngine.createWorkspaceScope();

			SingletonBeanSearchRequestor requestor = new SingletonBeanSearchRequestor();
			searchEngine.search(pattern, participants, scope, requestor, null);

			SearchMatch[] matches = requestor.getMatches();

			for (SearchMatch searchMatch : matches) {
				IType type = (IType) searchMatch.getElement();
				String singletonBeanClass = type.getFullyQualifiedName();
				
				results.add(singletonBeanClass);
			}
		} catch (Exception e) {
			throw new SingletonDependencyFinderException("Unable to find singleton beans");
		}

		return results.toArray(new String[results.size()]);
	}

	public List<String> getInterfacesFor(String singletonBeanClass) throws SingletonDependencyFinderException {
		List<String> result = new ArrayList<String>();
		
		try {
			IType type = javaProject.findType(singletonBeanClass);
			TypeDeclaration typeDeclaration = compilationUnitCache.getTypeDeclaration(type);

			ITypeBinding[] interfaces = typeDeclaration.resolveBinding().getInterfaces();

			for (int i = 0; i < interfaces.length; i++) {
				result.add(interfaces[i].getQualifiedName());
			}

			return result;
		} catch (Exception e) {
			throw new SingletonDependencyFinderException("Unable to get interfaces for " + singletonBeanClass);
		}
	}

	public Dependency[] findSingletonDependencies(String singleton, List<String> interfaces) throws SingletonDependencyFinderException {
		List<Dependency> result = new ArrayList<Dependency>();
		
		try {
			IType type = javaProject.findType(singleton);

			TypeDeclaration declaration = compilationUnitCache.getTypeDeclaration(type);
			
			MethodDeclaration[] methodDeclarations = declaration.getMethods();
			for (MethodDeclaration methodDeclaration : methodDeclarations) {
				IAnnotationBinding[] annotations = methodDeclaration.resolveBinding().getAnnotations();
				for (IAnnotationBinding annotationBinding : annotations) {
					if (annotationBinding.getAnnotationType().getQualifiedName().equals("javax.annotation.PostConstruct") || annotationBinding.getAnnotationType().getQualifiedName().equals("javax.annotation.PreDestroy")) {
						List<Dependency> dependencies = findReferencesTo(singleton, interfaces, methodDeclaration);
						result.addAll(dependencies);
					}
				}
			}
			
		} catch (JavaModelException e) {
		}
		
		return result.toArray(new Dependency[result.size()]);
	}

	private List<Dependency> findReferencesTo(String bean, List<String> interfacesToFind, MethodDeclaration methodDeclaration) {
		return findReferencesTo(bean, interfacesToFind, methodDeclaration, 0);
	}

	private List<Dependency> findReferencesTo(final String bean, final List<String> interfacesToFind, MethodDeclaration methodDeclaration, final int currentDepth) {
		final List<Dependency> results = new ArrayList<Dependency>();

		final MethodCall methodCall = new MethodCall();
		methodCall.setClassName(methodDeclaration.resolveBinding().getDeclaringClass().getQualifiedName());
		methodCall.setMethodName(methodDeclaration.getName().toString());
		methodCall.setCompilationUnit(getCompilationUnit(methodDeclaration).getElementName());
		
		methodDeclaration.accept(new ASTVisitor() {

			@Override
			public boolean visit(MethodInvocation node) {
				ITypeBinding declaringClass = node.resolveMethodBinding().getDeclaringClass();
				
				if (interfacesToFind.contains(declaringClass.getQualifiedName())) {
					Dependency dependency = new Dependency();
					
					// this is wrong!
					String dependsOn = model.getBeanForInterface(declaringClass.getQualifiedName());
					dependency.setDependsOn(dependsOn);
					
					dependency.addMethodCall(methodCall);
					results.add(dependency);
				} else {
					IMethod method = (IMethod) node.resolveMethodBinding().getJavaElement();
					if (! method.isBinary()) {
						MethodDeclaration calleeMethodDeclaration = compilationUnitCache.getMethodDeclaration(method);
					
						if (calleeMethodDeclaration != null) {
							List<Dependency> references = findReferencesTo(bean, interfacesToFind, calleeMethodDeclaration, currentDepth + 1);
							
							for (Iterator<Dependency> iterator = references.iterator(); iterator.hasNext();) {
								Dependency dependency = (Dependency) iterator.next();
								dependency.addMethodCall(methodCall);
								results.add(dependency);
							}
						}
					}
				}
				
				return super.visit(node);
			}
			
		});
		
		return results;
	}

	private IJavaElement getCompilationUnit(MethodDeclaration methodDeclaration) {
		IJavaElement javaElement = methodDeclaration.resolveBinding().getJavaElement();
		return getCompilationUnit(javaElement);
	}

	private IJavaElement getCompilationUnit(IJavaElement javaElement) {
		if (javaElement == null) return null;
		
		if (javaElement.getElementType() == IJavaElement.COMPILATION_UNIT) {
			return javaElement;
		}
		
		return javaElement.getPrimaryElement();
	}
}
