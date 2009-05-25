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
import java.util.List;
import java.util.Map;

import org.apache.openejb.devtools.core.CompilationUnitCache;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class IncrementalProjectBuilder extends org.eclipse.core.resources.IncrementalProjectBuilder {

	public static final String MARKER_TYPE_DEPENDS_ON = "org.apache.openejb.builder.dependsonMarker";
	private Model model;
	private CompilationUnitCache cache;

	public IncrementalProjectBuilder() {
		model = Activator.getPlugin().getModel();
		cache = new CompilationUnitCache(JavaCore.create(getProject()));
	}

	@SuppressWarnings("unchecked")
	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		cache.clear();
		ProjectModel projectModel = model.getModel(getProject());
		
		if (projectModel.getDependencies() == null || projectModel.getDependencies().size() == 0) {
			performFullBuild();
			return new IProject[] { getProject() };
		}

		if (kind != org.eclipse.core.resources.IncrementalProjectBuilder.INCREMENTAL_BUILD && kind != org.eclipse.core.resources.IncrementalProjectBuilder.AUTO_BUILD) {
			performFullBuild();
			return new IProject[] { getProject() };
		}

		performIncrementalBuild();
		return new IProject[] { getProject() };
	}

	private void performIncrementalBuild() throws CoreException {
		final ProjectModel projectModel = model.getModel(getProject());
		
		IResourceDelta delta = getDelta(getProject());
		delta.accept(new IResourceDeltaVisitor() {

			public boolean visit(IResourceDelta delta) throws CoreException {
				if (delta.getResource() instanceof IFolder) {
					return true;
				}
				
				IJavaProject javaProject = JavaCore.create(getProject());
				IClasspathEntry[] rawClasspath = javaProject.getRawClasspath();
				IPath path = delta.getFullPath();

				for (IClasspathEntry classpathEntry : rawClasspath) {
					if (IClasspathEntry.CPE_SOURCE == classpathEntry.getEntryKind()) {
						if (classpathEntry.getPath().isPrefixOf(path)) {
							StringBuffer pathBuffer = new StringBuffer();
							for (int i = classpathEntry.getPath().segments().length; i < path.segments().length; i++) {
								if (i > classpathEntry.getPath().segments().length) {
									pathBuffer.append(IPath.SEPARATOR);
								}
								
								pathBuffer.append(path.segment(i));
							}
							
							path = Path.fromOSString(pathBuffer.toString());
						}
					}
				}
				// this is the project
				if (path.segmentCount() == 1) {
					return true;
				}
				
				try {
					IJavaElement element = javaProject.findElement(path);
					
					if (element instanceof ICompilationUnit) {
						ICompilationUnit compilationUnit = (ICompilationUnit) element;
						IType[] types = compilationUnit.getTypes();
						
						for (IType type : types) {
							if (isSingleton(type)) {
								updateInterfaces(type.getFullyQualifiedName());
							}
							
							List<String> singletons = projectModel.getBeansDependentOn(type.getFullyQualifiedName());
							for (String singleton : singletons) {
								try {
									checkDependency(singleton);
								} catch (SingletonDependencyFinderException e) {
								}
							}
						}
					}
				} catch (Exception e) {
				}

				return true;
			}
		});
	}

	protected void updateInterfaces(String singleton) {
		try {
			ProjectModel projectModel = model.getModel(getProject());
			SingletonDependencyFinder finder = new SingletonDependencyFinder(projectModel);
			List<String> interfaces = finder.getInterfacesFor(singleton);
			projectModel.setSingletonBeanInterfaces(singleton, interfaces);
		} catch (SingletonDependencyFinderException e) {
			e.printStackTrace();
		}
	}

	protected boolean isSingleton(IType type) {
		TypeDeclaration typeDeclaration = cache.getTypeDeclaration(type);
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		
		IAnnotationBinding[] annotations = typeBinding.getAnnotations();
		for (IAnnotationBinding annotation : annotations) {
			String qualifiedName = annotation.getAnnotationType().getQualifiedName();
			if ("javax.ejb.Singleton".equals(qualifiedName)) {
				return true;
			}
		}
		
		return false;
	}

	
	protected void performFullBuild() throws CoreException, JavaModelException {
		ProjectModel projectModel = model.getModel(getProject());
		projectModel.clear();
		
		getProject().deleteMarkers(MARKER_TYPE_DEPENDS_ON, true, IResource.DEPTH_INFINITE);

		try {
			SingletonDependencyFinder finder = new SingletonDependencyFinder(projectModel);
			String[] singletons = finder.findSingletons();

			for (String singleton : singletons) {
				List<String> interfaces = finder.getInterfacesFor(singleton);
				projectModel.setSingletonBeanInterfaces(singleton, interfaces);
			}
			
			for (String singleton : singletons) {				
				checkDependency(singleton);
			}

		} catch (SingletonDependencyFinderException e) {
		}
	}

	private void checkDependency(String singleton) throws JavaModelException, CoreException, SingletonDependencyFinderException {
		ProjectModel projectModel = model.getModel(getProject());
		removeMarkersFor(singleton);
		
		List<String> interfacesToSearchFor = projectModel.getInterfacesToSearchFor(singleton);
		
		IJavaProject javaProject = JavaCore.create(getProject());
		SingletonDependencyFinder finder = new SingletonDependencyFinder(projectModel);
		Dependency[] dependencies = finder.findSingletonDependencies(singleton, interfacesToSearchFor);
		
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

		List<String> expectedDependencies = new ArrayList<String>();

		if (dependencies != null) {
			for (Dependency dependency : dependencies) {
				expectedDependencies.add(dependency.getDependsOn());
			}
		}

		boolean matches = expectedDependencies.size() == declaredDependencies.size();

		if (matches) {
			for (String dependency : declaredDependencies) {
				if (!expectedDependencies.contains(dependency)) {
					matches = false;
					break;
				}
			}
		}

		if (!matches) {
			IMarker marker = type.getUnderlyingResource().createMarker(MARKER_TYPE_DEPENDS_ON);
			Map attributes = new HashMap();

			attributes.put(IMarker.LINE_NUMBER, compilationUnit.getLineNumber(sourceRange.getOffset()));
			attributes.put(IMarker.CHAR_START, sourceRange.getOffset());
			attributes.put(IMarker.CHAR_END, sourceRange.getOffset() + sourceRange.getLength());
			attributes.put(IMarker.LINE_NUMBER, compilationUnit.getLineNumber(sourceRange.getOffset()));
			attributes.put(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
			attributes.put(IMarker.MESSAGE, expectedDependencies.size() == 0 ? "This bean should not have the @DependsOn annotation" : "This bean requires the @DependsOn annotation, and depends on: " + getDependencyList(expectedDependencies));
			attributes.put(ISingletonDependencyMarker.DEPENDENCIES, expectedDependencies.toArray(new String[expectedDependencies.size()]));
			attributes.put(ISingletonDependencyMarker.BEAN, singleton);
			marker.setAttributes(attributes);
		}

		projectModel.addDependencies(Arrays.asList(dependencies));
	}

	private void removeMarkersFor(String singleton) throws CoreException {
		IMarker[] markers = getProject().findMarkers(MARKER_TYPE_DEPENDS_ON, true, IResource.DEPTH_INFINITE);
		for (IMarker marker : markers) {
			try {
				String bean = (String) marker.getAttribute(ISingletonDependencyMarker.BEAN);
				if (bean.equals(singleton)) {
					marker.delete();
				}
			} catch (Exception e) {
			}
		}
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
