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

package org.apache.openejb.devtools.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.TextEdit;

public class CompilationUnitCache {

	Map<ICompilationUnit, CompilationUnit> cache = new HashMap<ICompilationUnit, CompilationUnit>();
	private IJavaProject javaProject;
	
	public CompilationUnitCache(IJavaProject javaProject) {
		super();
		this.javaProject = javaProject;
	}

	public CompilationUnit getCompilationUnit(ICompilationUnit compilationUnit) {
		if (cache.containsKey(compilationUnit)) {
			return cache.get(compilationUnit);
		}
		
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(compilationUnit);
		parser.setResolveBindings(true);
		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		cu.recordModifications();
		
		cache.put(compilationUnit, cu);
		return cu;
	}

	@SuppressWarnings("unchecked")
	public TypeDeclaration getTypeDeclaration(IType type) {
		ICompilationUnit compilationUnit = type.getCompilationUnit();

		CompilationUnit cu = getCompilationUnit(compilationUnit);

		List types = cu.types();
		for (int i = 0; i < types.size(); i++) {
			TypeDeclaration typeDeclaration = (TypeDeclaration) types.get(i);
			if (typeDeclaration.getName().toString().equals(type.getElementName())) {
				return typeDeclaration;
			}
		}
		
		return null;
	}

	public MethodDeclaration getMethodDeclaration(IMethod method) {
		TypeDeclaration typeDeclaration = getTypeDeclaration(method.getDeclaringType());
		MethodDeclaration[] methods = typeDeclaration.getMethods();
		for (MethodDeclaration methodDeclaration : methods) {
			try {
				IMethod m = (IMethod) methodDeclaration.resolveBinding().getJavaElement();
				if (m.equals(method)) {
					return methodDeclaration;
				}
			} catch (Exception e) {
			}
		}
		
		return null;
	}

	public CompilationUnit getCompilationUnit(String cls) {
		try {
			IType type = javaProject.findType(cls);
			ICompilationUnit compilationUnit = type.getCompilationUnit();

			return getCompilationUnit(compilationUnit);
		} catch (JavaModelException e) {
		}
		
		return null;
	}

	public Change getChange() {
		CompositeChange compositeChange = new CompositeChange(Messages.getString("org.apache.openejb.helper.annotation.compositChangeString")); //$NON-NLS-1$

		Iterator<CompilationUnit> iterator = cache.values().iterator();
		while (iterator.hasNext()) {
			try {
				CompilationUnit cu = iterator.next();
				ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
				IPath path = cu.getJavaElement().getPath();
				bufferManager.connect(path, null);

				ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(path);
				IDocument document = textFileBuffer.getDocument();

				TextEdit edit = cu.rewrite(document, javaProject.getOptions(true));
				TextFileChange dc = new TextFileChange(path.toString(), (IFile) cu.getJavaElement().getResource());
				dc.setTextType("java"); //$NON-NLS-1$
				dc.setEdit(edit);
				dc.setSaveMode(TextFileChange.FORCE_SAVE);

				compositeChange.add(dc);
			} catch (CoreException e) {
			}
		}

		return compositeChange;
	}

	public TypeDeclaration getTypeDeclaration(String targetClass) {
		try {
			IType type = javaProject.findType(targetClass);
			return getTypeDeclaration(type);
		} catch (JavaModelException e) {
		}
		
		return null;
	}

	public MethodDeclaration getMethodDeclaration(String fullyQualifiedClassName, String methodName, String[] signature) {
		TypeDeclaration typeDeclaration = getTypeDeclaration(fullyQualifiedClassName);
		
		MethodDeclaration m = null;

		MethodDeclaration[] methods = typeDeclaration.getMethods();
		Iterator<MethodDeclaration> iterator = Arrays.asList(methods).iterator();
		while (iterator.hasNext()) {
			MethodDeclaration method = iterator.next();

			if (method.getName().toString().equals(methodName) && (signature == null || signatureMatches(method, signature))) {
				m = method;
			}
		}
		return m;
	}
	
	private boolean signatureMatches(MethodDeclaration method, String[] signature) {
		if (signature.length != method.parameters().size()) {
			return false;
		}

		for (int i = 0; i < signature.length; i++) {
			SingleVariableDeclaration var = (SingleVariableDeclaration) method.parameters().get(i);
			Type type = var.getType();

			ITypeBinding typeBinding = type.resolveBinding();

			if (!(typeBinding.getQualifiedName().toString().equals(signature[i]))) {
				return false;
			}
		}

		return true;
	}

	public List<String[]> getSignatures(String type, String methodName) {
		List<String[]> result = new ArrayList<String[]>();
		
		TypeDeclaration typeDeclaration = getTypeDeclaration(type);
		MethodDeclaration[] methods = typeDeclaration.getMethods();
		for (MethodDeclaration methodDeclaration : methods) {
			if (methodDeclaration.getName().toString().equals(methodName)) {
				String[] signature = getSignature(methodDeclaration);
				result.add(signature);
			}
		}
		
		return result;
	}

	private String[] getSignature(MethodDeclaration methodDeclaration) {
		List<String> params = new ArrayList<String>();
		
		for (int i = 0; i < methodDeclaration.parameters().size(); i++) {
			SingleVariableDeclaration var = (SingleVariableDeclaration) methodDeclaration.parameters().get(i);
			Type type = var.getType();

			ITypeBinding typeBinding = type.resolveBinding();
			String param = typeBinding.getQualifiedName().toString();
			params.add(param);
		}
		
		return params.toArray(new String[params.size()]);
	}
}
