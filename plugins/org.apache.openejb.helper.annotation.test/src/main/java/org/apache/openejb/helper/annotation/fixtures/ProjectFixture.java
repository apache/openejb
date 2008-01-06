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

package org.apache.openejb.helper.annotation.fixtures;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CompilationParticipant;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.launching.JavaRuntime;

public class ProjectFixture {

	protected IProject project;
	protected IJavaProject javaProject;

	public ProjectFixture() {
		try {
			project = ResourcesPlugin.getWorkspace()
				.getRoot()
				.getProject("TestProject");
			
			project.create(null);
			project.open(null);
			javaProject = JavaCore.create(project);
			
			addJavaNature();
			
			IFile j2eeJar = project.getFile("javaee.jar");
			j2eeJar.create(getClass().getResourceAsStream("javaee.jar"), true, null);
			
			javaProject.setRawClasspath(new IClasspathEntry[]{ JavaRuntime.getDefaultJREContainerEntry(), JavaCore.newLibraryEntry(j2eeJar.getFullPath(), null, null) }, null);
			
			addSrcFolder();
			addBinFolder();
			
		} catch (Exception e) {
			throw new RuntimeException("Unable to create Eclipse project", e);
		}
	}
	
	protected void addJavaNature() throws CoreException {
		IProjectDescription description= project.getDescription();
		description.setNatureIds(new String[] { JavaCore.NATURE_ID });
		project.setDescription(description, null);	}

	protected void addBinFolder() throws CoreException {
		IProject project = javaProject.getProject();
		IFolder folder = project.getFolder("bin");
		
		IPath outputLocation= folder.getFullPath();
		javaProject.setOutputLocation(outputLocation, null);
	}

	protected void addSrcFolder() throws CoreException {
		IFolder folder = project.getFolder("src");
		folder.create(false, true, null);
		IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(folder);

		IClasspathEntry[] oldEntries= javaProject.getRawClasspath();
		IClasspathEntry[] newEntries= new IClasspathEntry[oldEntries.length + 1];
		System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
		newEntries[oldEntries.length]= JavaCore.newSourceEntry(root.getPath());
		javaProject.setRawClasspath(newEntries, null);
	}

	public String getClassContents(String fullClassName) throws CoreException {
		IType type = javaProject.findType(fullClassName);
		String source = type.getCompilationUnit().getSource();
		
		return source;
	}

	public void addClassToProject(String fullClassName, String content) throws CoreException {
		String className = getClassName(fullClassName);
		String packageName = getPackageName(fullClassName);
		
		IFolder folder = project.getFolder("src");
		if (! folder.exists()) {
			addSrcFolder();
		}
		
		IPackageFragmentRoot packageFragmentRoot = javaProject.findPackageFragmentRoot(folder.getFullPath());
		IPackageFragment packageFragment = packageFragmentRoot.createPackageFragment(packageName, true, null);

		ICompilationUnit cu = packageFragment.createCompilationUnit(className + ".java", content, true, null);
		cu.reconcile(AST.JLS3, true, null, null);
	}

	private String getPackageName(String fullClassName) {
		int lastDotPosition = fullClassName.lastIndexOf(".");
		return fullClassName.substring(0, lastDotPosition);
	}

	private String getClassName(String fullClassName) {
		int lastDotPosition = fullClassName.lastIndexOf(".");
		return fullClassName.substring(lastDotPosition + 1);
	}

	public IProject getProject() {
		return project;
	}

	public void delete() throws CoreException {
		
		project.delete(true, null);
	}

	public void reset() throws CoreException {
		javaProject.makeConsistent(null);
		
		project.build(IncrementalProjectBuilder.CLEAN_BUILD, null);
		project.build(IncrementalProjectBuilder.FULL_BUILD, null);
	}
}
