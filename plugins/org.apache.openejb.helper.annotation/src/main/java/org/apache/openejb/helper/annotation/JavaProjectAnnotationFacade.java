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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

/**
 * Add annotations to source files in an Eclipse Java project
 * 
 */
public class JavaProjectAnnotationFacade implements IJavaProjectAnnotationFacade {

	protected IJavaProject javaProject;

	protected Map<String, CompilationUnit> cuMap = new HashMap<String, CompilationUnit>();

	/**
	 * Creates a new annotation facade
	 * 
	 * @param project
	 *            Eclipse project to work on
	 */
	public JavaProjectAnnotationFacade(IProject project) {
		this.javaProject = JavaCore.create(project);
	}

	protected CompilationUnit getCompilationUnit(String targetClass) throws JavaModelException {
		IType type = javaProject.findType(targetClass);
		ICompilationUnit compilationUnit = type.getCompilationUnit();

		String path = compilationUnit.getPath().toString();

		if (cuMap.keySet().contains(path)) {
			return cuMap.get(path);
		}

		CompilationUnit cu = parse(compilationUnit);
		cuMap.put(path, cu);

		return cu;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.openejb.helper.annotation.IAnnotationHelper#addClassAnnotation(java.lang.String,
	 *      java.lang.String)
	 */
	public void addClassAnnotation(String fullClassName, String annotationToAdd) {
		addClassAnnotation(fullClassName, annotationToAdd, null);
	}

	/**
	 * Adds a class to the list of imports for a compilation unit This method
	 * will check to see if the class has already been imported
	 * 
	 * @param classToImport
	 *            The fully qualified name of the class to import
	 * @param compilationUnit
	 *            The compilation unit to add the import to
	 */
	void addImportToCompilationUnit(String classToImport, CompilationUnit compilationUnit) {
		if (!isClassImported(classToImport, compilationUnit)) {
			String[] parts = classToImport.split("\\.");

			AST ast = compilationUnit.getAST();

			Name name = null;

			for (int i = 0; i < parts.length; i++) {
				SimpleName simpleName = ast.newSimpleName(parts[i]);
				if (i == 0) {
					name = simpleName;
				} else {
					name = ast.newQualifiedName(name, simpleName);
				}
			}

			ImportDeclaration importDeclaration = ast.newImportDeclaration();
			importDeclaration.setName(name);
			importDeclaration.setOnDemand(false);
			importDeclaration.setStatic(false);

			compilationUnit.imports().add(importDeclaration);
		}
	}

	/**
	 * Determines whether the specified class has been imported in the
	 * compilation unit
	 * 
	 * @param importedClass
	 *            The imported (or not) class
	 * @param compilationUnit
	 *            The compilation unit to check
	 * @return Whether of not the class has been imported
	 */
	private boolean isClassImported(String importedClass, CompilationUnit compilationUnit) {
		Iterator iterator = compilationUnit.imports().iterator();
		String packageName = importedClass.substring(0, importedClass.lastIndexOf("."));

		while (iterator.hasNext()) {
			ImportDeclaration importDeclaration = (ImportDeclaration) iterator.next();
			String importedName = importDeclaration.getName().toString();
			if (importedName.equals(packageName) || importedName.equals(importedClass)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Parses the specified compilation unit to obtain an AST
	 * 
	 * @param compilationUnit
	 *            The compilation unit to parse
	 * @return The parsed compilation unit AST object
	 */
	CompilationUnit parse(ICompilationUnit compilationUnit) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(compilationUnit);
		parser.setResolveBindings(true);
		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		cu.recordModifications();
		return cu;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.openejb.helper.annotation.IAnnotationHelper#addClassAnnotation(java.lang.String,
	 *      java.lang.Class, java.util.Map)
	 */
	public void addClassAnnotation(String targetClass, Class annotation, Map<String, Object> properties) {
		addClassAnnotation(targetClass, annotation.getCanonicalName(), properties);
	}

	boolean addModifierToDeclaration(BodyDeclaration declaration, Annotation annotation) {
		Iterator iterator = declaration.modifiers().iterator();
		while (iterator.hasNext()) {
			IExtendedModifier modifier = (IExtendedModifier) iterator.next();
			if (!(modifier instanceof Annotation)) {
				continue;
			}

			if (((Annotation) modifier).getTypeName().toString().equals(annotation.getTypeName().toString())) {
				// break out if this type already has this annotation
				return false;
			}
		}

		declaration.modifiers().add(0, annotation);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.openejb.helper.annotation.IJavaProjectAnnotationFacade#addClassAnnotation(java.lang.String,
	 *      java.lang.String, java.util.Map)
	 */
	public void addClassAnnotation(String targetClass, String annotationToAdd, Map<String, Object> properties) {
		try {

			// try and get a java element that corresponds to the annotation
			IType annotationType = javaProject.findType(annotationToAdd);
			if (!annotationType.isAnnotation()) {
				return;
			}

			CompilationUnit cu = getCompilationUnit(targetClass);
			TypeDeclaration typeDeclaration = getTypeDeclaration(cu, targetClass);

			boolean modifierAdded = addModifierToDeclaration(typeDeclaration, createAnnotationWithProperties(cu.getAST(), annotationType, properties));
			if (modifierAdded) {
				addImportToCompilationUnit(annotationToAdd, cu);
			}

		} catch (MalformedTreeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	TypeDeclaration getTypeDeclaration(CompilationUnit compilationUnit, String targetClass) throws CoreException {
		IType type = javaProject.findType(targetClass);

		Iterator typesIterator = compilationUnit.types().iterator();

		while (typesIterator.hasNext()) {
			TypeDeclaration typeDeclaration = (TypeDeclaration) typesIterator.next();
			if (typeDeclaration.getName().toString().equals(type.getElementName())) {
				return typeDeclaration;
			}
		}

		return null;
	}

	/**
	 * Saves changes made to the AST of the compilation unit. Assumes that
	 * <code>compilationUnit.recordModifications()</code> has been called
	 * 
	 * @param compilationUnit
	 *            The compilation unit to save
	 * @throws CoreException
	 * @throws BadLocationException
	 * @throws JavaModelException
	 * @deprecated
	 */
	void saveChangesTo(CompilationUnit compilationUnit) throws CoreException, BadLocationException, JavaModelException {
		ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
		IPath path = compilationUnit.getJavaElement().getPath();
		bufferManager.connect(path, null);

		ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(path);
		IDocument document = textFileBuffer.getDocument();

		TextEdit edits = compilationUnit.rewrite(document, javaProject.getOptions(true));
		edits.apply(document);
		String newSource = document.get();
		IBuffer buffer = ((ICompilationUnit) compilationUnit.getJavaElement()).getBuffer();
		buffer.setContents(newSource);
		buffer.save(null, true);

		textFileBuffer.commit(null, true);
		bufferManager.disconnect(path, null);
	}

	/**
	 * Creates a new annotation object to be added to the AST, with the
	 * specified properties
	 * 
	 * @param ast
	 *            The AST to create the annotation for
	 * @param annotationType
	 *            The type of annotation to create
	 * @param properties
	 *            The properties for the annotation to add
	 * @return The created annotation AST object
	 */
	Annotation createAnnotationWithProperties(AST ast, IType annotationType, Map<String, Object> properties) {
		SimpleName annotationTypeName = ast.newSimpleName(annotationType.getElementName());
		Annotation annotation;

		if (properties != null) {
			annotation = ast.newNormalAnnotation();
			Iterator<String> propertyIterator = properties.keySet().iterator();
			while (propertyIterator.hasNext()) {
				String propertyName = (String) propertyIterator.next();
				annotation.setProperty(propertyName, properties.get(propertyName));

				MemberValuePair annotationProperty = ast.newMemberValuePair();
				annotationProperty.setName(ast.newSimpleName(propertyName));
				
				QualifiedName expression = ast.newQualifiedName(ast.newSimpleName("TransactionManagementType"), ast.newSimpleName("BEAN"));
				annotationProperty.setValue(expression);

				((NormalAnnotation) annotation).values().add(annotationProperty);
			}
		} else {
			annotation = ast.newMarkerAnnotation();
		}

		annotation.setTypeName(annotationTypeName);
		return annotation;
	}

	public void addMethodAnnotation(String fullyQualifiedClassName, String methodName, Class annotationClass, Map<String, Object> properties) {
		addMethodAnnotation(fullyQualifiedClassName, methodName, annotationClass.getCanonicalName(), properties);
	}

	public void addMethodAnnotation(String targetClass, String methodName, String annotationToAdd, Map<String, Object> properties) {
		try {
			// try and get a java element that corresponds to the annotation
			IType annotationType = javaProject.findType(annotationToAdd);
			if (!annotationType.isAnnotation()) {
				return;
			}

			CompilationUnit cu = getCompilationUnit(targetClass);

			TypeDeclaration typeDeclaration = getTypeDeclaration(cu, targetClass);

			MethodDeclaration[] methods = typeDeclaration.getMethods();
			Iterator<MethodDeclaration> iterator = Arrays.asList(methods).iterator();
			while (iterator.hasNext()) {
				MethodDeclaration method = (MethodDeclaration) iterator.next();
				if (method.getName().toString().equals(methodName)) {
					boolean modifierAdded = addModifierToDeclaration(method, createAnnotationWithProperties(cu.getAST(), annotationType, properties));
					if (modifierAdded) {
						addImportToCompilationUnit(annotationToAdd, cu);
					}
				}
			}

		} catch (CoreException e) {

		}
	}

	public void addFieldAnnotation(String targetClass, String targetField, Class annotation, Map<String, Object> properties) {
		addFieldAnnotation(targetClass, targetField, annotation.getCanonicalName(), properties);
	}

	public void addFieldAnnotation(String targetClass, String targetField, String annotation, Map<String, Object> properties) {
		try {
			// try and get a java element that corresponds to the annotation
			IType annotationType = javaProject.findType(annotation);
			if (!annotationType.isAnnotation()) {
				return;
			}

			CompilationUnit cu = getCompilationUnit(targetClass);

			TypeDeclaration typeDeclaration = getTypeDeclaration(cu, targetClass);
			FieldDeclaration[] fields = typeDeclaration.getFields();

			Iterator<FieldDeclaration> iterator = Arrays.asList(fields).iterator();
			while (iterator.hasNext()) {
				FieldDeclaration field = iterator.next();
				if (field.fragments().size() == 0) {
					continue;
				}

				VariableDeclarationFragment varibleDeclaration = (VariableDeclarationFragment) field.fragments().get(0);
				if (varibleDeclaration.getName().toString().equals(targetField)) {
					boolean modifierAdded = addModifierToDeclaration(field, createAnnotationWithProperties(cu.getAST(), annotationType, properties));
					if (modifierAdded) {
						addImportToCompilationUnit(annotation, cu);
					}
				}
			}

		} catch (CoreException e) {

		}
	}

	public Change getChange() {
		CompositeChange compositeChange = new CompositeChange("Add EJB 3.0 Annotations");

		Iterator<CompilationUnit> iterator = cuMap.values().iterator();
		while (iterator.hasNext()) {
			try {
				CompilationUnit cu = (CompilationUnit) iterator.next();
				ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
				IPath path = cu.getJavaElement().getPath();
				bufferManager.connect(path, null);

				ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(path);
				IDocument document = textFileBuffer.getDocument();

				TextEdit edit = cu.rewrite(document, javaProject.getOptions(true));

				TextFileChange dc = new TextFileChange(path.toString(), (IFile) cu.getJavaElement().getResource());
				//DocumentChange dc = new DocumentChange(path.toString(), document);
				dc.setTextType("java");
				dc.setEdit(edit);
				dc.setSaveMode(TextFileChange.FORCE_SAVE);

				compositeChange.add(dc);

			} catch (CoreException e) {

			}
		}

		return compositeChange;
	}
}
