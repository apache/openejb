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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.openejb.devtools.core.CompilationUnitCache;
import org.eclipse.core.resources.IProject;
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

	public SingletonDependencyFinder(IProject project) {
		super();
		javaProject = JavaCore.create(project);
	}

	protected Map<String, String[]> singletons = new HashMap<String, String[]>();
	protected Map<String, Dependency[]> dependencies = new HashMap<String, Dependency[]>();
	private IJavaProject javaProject;
	private CompilationUnitCache compilationUnitCache = new CompilationUnitCache(javaProject);

	public void findDependencies() throws SingletonDependencyFinderException {
		findSingletonsAndInterfaces();
		findSingletonDependencies();
	}

	protected void findSingletonsAndInterfaces() throws SingletonDependencyFinderException {
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

				String[] interfaces = getInterfacesFor(singletonBeanClass);
				singletons.put(singletonBeanClass, interfaces);

			}
		} catch (Exception e) {
			throw new SingletonDependencyFinderException("Unable to find singleton beans");
		}

	}

	@SuppressWarnings("unchecked")
	private String[] getInterfacesFor(String singletonBeanClass) throws SingletonDependencyFinderException {
		try {
			IType type = javaProject.findType(singletonBeanClass);
			TypeDeclaration typeDeclaration = compilationUnitCache.getTypeDeclaration(type);

			ITypeBinding[] interfaces = typeDeclaration.resolveBinding().getInterfaces();
			String[] result = new String[interfaces.length];

			for (int i = 0; i < result.length; i++) {
				result[i] = interfaces[i].getQualifiedName();
			}

			return result;
		} catch (Exception e) {
			throw new SingletonDependencyFinderException("Unable to get interfaces for " + singletonBeanClass);
		}
	}

	protected void findSingletonDependencies() {
		Iterator<String> iterator = singletons.keySet().iterator();
		while (iterator.hasNext()) {
			String singleton = (String) iterator.next();
			findSingletonDependencies(singleton);
		}
	}

	protected void findSingletonDependencies(String singleton) {
		try {
			List<String> interfacesToFind = getInterfacesToMatch(singleton);
			IType type = javaProject.findType(singleton);

			TypeDeclaration declaration = compilationUnitCache.getTypeDeclaration(type);

			List<Dependency> result = new ArrayList<Dependency>();
			
			MethodDeclaration[] methodDeclarations = declaration.getMethods();
			for (MethodDeclaration methodDeclaration : methodDeclarations) {
				IAnnotationBinding[] annotations = methodDeclaration.resolveBinding().getAnnotations();
				for (IAnnotationBinding annotationBinding : annotations) {
					if (annotationBinding.getAnnotationType().getQualifiedName().equals("javax.annotation.PostConstruct") || annotationBinding.getAnnotationType().getQualifiedName().equals("javax.annotation.PreDestroy")) {
						List<Dependency> dependencies = findReferencesTo(interfacesToFind, methodDeclaration);
						result.addAll(dependencies);
					}
				}
			}
			
			if (result.size() > 0) {
				dependencies.put(singleton, result.toArray(new Dependency[result.size()]));
			}
			
		} catch (JavaModelException e) {
		}
	}

	private List<Dependency> findReferencesTo(List<String> interfacesToFind, MethodDeclaration methodDeclaration) {
		return findReferencesTo(interfacesToFind, methodDeclaration, 0);
	}

	private List<Dependency> findReferencesTo(final List<String> interfacesToFind, MethodDeclaration methodDeclaration, final int currentDepth) {
		final List<Dependency> results = new ArrayList<Dependency>();

		final MethodCall methodCall = new MethodCall();
		methodCall.setClassName(methodDeclaration.resolveBinding().getDeclaringClass().getQualifiedName());
		methodCall.setMethodName(methodDeclaration.getName().toString());
		
		methodDeclaration.accept(new ASTVisitor() {

			@Override
			public boolean visit(MethodInvocation node) {
				ITypeBinding declaringClass = node.resolveMethodBinding().getDeclaringClass();
				
				if (interfacesToFind.contains(declaringClass.getQualifiedName())) {
					String dependsOn = getBeanForInterface(declaringClass.getQualifiedName());

					if (dependsOn != null) {
						Dependency dependency = new Dependency();
						dependency.setDependsOn(dependsOn);
						
						dependency.addMethodCall(methodCall);
						results.add(dependency);
					}
				} else {
					MethodDeclaration calleeMethodDeclaration = compilationUnitCache.getMethodDeclaration((IMethod) node.resolveMethodBinding().getJavaElement());
					if (calleeMethodDeclaration != null) {
						List<Dependency> references = findReferencesTo(interfacesToFind, calleeMethodDeclaration, currentDepth + 1);
						
						for (Iterator<Dependency> iterator = references.iterator(); iterator.hasNext();) {
							Dependency dependency = (Dependency) iterator.next();
							dependency.addMethodCall(methodCall);
							results.add(dependency);
						}
					}
				}
				
				return super.visit(node);
			}
			
		});
		
		return results;
	}

	private List<String> getInterfacesToMatch(String singleton) {
		List<String> result = new ArrayList<String>();
		Iterator<String> iterator = singletons.keySet().iterator();

		while (iterator.hasNext()) {
			String bean = (String) iterator.next();
			if (bean.equals(singleton)) {
				continue;
			}

			String[] interfaces = singletons.get(bean);
			result.addAll(Arrays.asList(interfaces));
		}

		return result;
	}

	public String[] getSingletons() {
		Set<String> singletonBeans = singletons.keySet();
		return singletonBeans.toArray(new String[singletonBeans.size()]);
	}

	public Dependency[] getDependencies(String singleton) {
		return dependencies.get(singleton);
	}

	public boolean hasDependencies(String singleton) {
		Dependency[] deps = getDependencies(singleton);
		return deps != null && deps.length > 0;
	}
	
	protected String getBeanForInterface(String iface) {
		if (iface == null) {
			return null;
		}
		
		for (String singleton : singletons.keySet()) {
			String[] ifaces = singletons.get(singleton);
			
			if (ifaces == null) {
				continue;
			}
			
			for (int i = 0; i < ifaces.length; i++) {
				if (iface.equals(ifaces[i])) {
					return singleton;
				}
			}
		}
		
		return null;
	}
}
