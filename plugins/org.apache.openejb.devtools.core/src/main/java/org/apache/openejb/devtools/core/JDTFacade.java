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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.openejb.plugins.common.IJDTFacade;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.MethodReferenceMatch;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.ltk.core.refactoring.Change;

/**
 * Add annotations to source files in an Eclipse Java project
 * 
 */
@SuppressWarnings("unchecked")//$NON-NLS-1$
public class JDTFacade implements IJDTFacade {

	public class BasicSearchRequestor extends SearchRequestor {
		private List<SearchMatch> matches = new ArrayList<SearchMatch>();

		@Override
		public void acceptSearchMatch(SearchMatch match) throws CoreException {
			matches.add(match);
		}

		public SearchMatch[] getMatches() {
			return matches.toArray(new SearchMatch[0]);
		}
	}

	protected IJavaProject javaProject;
	protected Map<String, CompilationUnit> cuMap = new HashMap<String, CompilationUnit>();
	protected List<String> warnings = new ArrayList<String>();
	public CompilationUnitCache compilationUnitCache;

	/**
	 * Creates a new annotation facade
	 * 
	 * @param project Eclipse project to work on
	 */
	public JDTFacade(IProject project) {
		this.javaProject = JavaCore.create(project);
		compilationUnitCache = new CompilationUnitCache(javaProject);
	}

	/**
	 * Adds a class to the list of imports for a compilation unit. This method
	 * will check to see if the class has already been imported
	 * 
	 * @param classToImport The fully qualified name of the class to import
	 * @param compilationUnit The compilation unit to add the import to
	 */
	void addImportToCompilationUnit(String classToImport, CompilationUnit compilationUnit) {
		if (!isClassImported(classToImport, compilationUnit)) {
			AST ast = compilationUnit.getAST();

			Name name = createQualifiedName(ast, classToImport);
			ImportDeclaration importDeclaration = ast.newImportDeclaration();
			importDeclaration.setName(name);
			importDeclaration.setOnDemand(false);
			importDeclaration.setStatic(false);

			compilationUnit.imports().add(importDeclaration);
		}
	}

	private Name createQualifiedName(AST ast, String classToImport) {
		String[] parts = classToImport.split("\\."); //$NON-NLS-1$

		Name name = null;

		for (int i = 0; i < parts.length; i++) {
			SimpleName simpleName = ast.newSimpleName(parts[i]);
			if (i == 0) {
				name = simpleName;
			} else {
				name = ast.newQualifiedName(name, simpleName);
			}
		}
		return name;
	}

	/**
	 * Determines whether the specified class has been imported in the
	 * compilation unit
	 * 
	 * @param importedClass The imported (or not) class
	 * @param compilationUnit The compilation unit to check
	 * @return Whether of not the class has been imported
	 */
	private boolean isClassImported(String importedClass, CompilationUnit compilationUnit) {
		Iterator<ImportDeclaration> iterator = compilationUnit.imports().iterator();
		String packageName = importedClass.substring(0, importedClass.lastIndexOf(".")); //$NON-NLS-1$

		while (iterator.hasNext()) {
			ImportDeclaration importDeclaration = iterator.next();
			String importedName = importDeclaration.getName().toString();
			if (importedName.equals(packageName) || importedName.equals(importedClass)) {
				return true;
			}
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.openejb.helper.annotation.IAnnotationHelper#addClassAnnotation(java.lang.String,
	 *      java.lang.Class, java.util.Map)
	 */
	public void addClassAnnotation(String targetClass, Class<? extends java.lang.annotation.Annotation> annotation, Map<String, Object> properties) {
		try {
			CompilationUnit cu = compilationUnitCache.getCompilationUnit(targetClass);
			BodyDeclaration typeDeclaration = compilationUnitCache.getTypeDeclaration(targetClass);

			if (isAnnotationAlreadyUsedOnDeclaration(annotation, typeDeclaration)) {
				warnings.add(String.format(Messages.getString("org.apache.openejb.helper.annotation.warnings.1"), annotation.getCanonicalName(), targetClass)); //$NON-NLS-1$
				return;
			}

			Annotation modifier = createModifier(cu.getAST(), annotation, properties, cu);
			typeDeclaration.modifiers().add(0, modifier);
		} catch (Exception e) {
			warnings.add(String.format(Messages.getString("org.apache.openejb.helper.annotation.warnings.2"), annotation.getCanonicalName(), targetClass)); //$NON-NLS-1$
		}
	}

	private boolean isAnnotationAlreadyUsedOnDeclaration(Class<? extends java.lang.annotation.Annotation> annotation, BodyDeclaration declaration) {
		IExtendedModifier[] modifiers = (IExtendedModifier[]) declaration.modifiers().toArray(new IExtendedModifier[0]);
		for (IExtendedModifier modifier : modifiers) {
			if (!(modifier instanceof Annotation)) {
				continue;
			}

			Annotation annotationModifer = (Annotation) modifier;
			if (annotationModifer.getTypeName().toString().equals(annotation.getCanonicalName())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Creates a new annotation object to be added to the AST, with the
	 * specified properties
	 * 
	 * @param ast The AST to create the annotation for
	 * @param annotation The type of annotation to create
	 * @param properties The properties for the annotation to add
	 * @param cu Compilation Unit
	 * @return The created annotation AST object
	 * @throws JavaModelException
	 */
	protected Annotation createModifier(AST ast, Class<?> annotation, Map<String, Object> properties, CompilationUnit cu) throws JavaModelException {
		// try and get a java element that corresponds to the annotation
		IType annotationType = javaProject.findType(annotation.getCanonicalName());
		if (!annotationType.isAnnotation()) {
			return null;
		}

		addImportToCompilationUnit(annotation.getCanonicalName(), cu);

		Annotation result = null;
		Name annotationTypeName = ast.newSimpleName(annotationType.getElementName());

		if (properties != null) {
			result = ast.newNormalAnnotation();

			Method[] methods = annotation.getDeclaredMethods();
			for (Method method : methods) {
				Class<?> returnType = method.getReturnType();

				// get the matching value in the properties
				Object value = properties.get(method.getName());

				if (value == null && method.getDefaultValue() == null) {
					// TODO: throw an exception here
				}

				if (value == null) {
					// no need to do anything - the default will be used
					continue;
				}

				if (value.getClass().isArray() != returnType.isArray()) {
					// TODO: throw an exception here
				}

				MemberValuePair annotationProperty = ast.newMemberValuePair();
				annotationProperty.setName(ast.newSimpleName(method.getName()));

				Expression expression = createAnnotationPropertyValueExpression(cu, returnType, value);

				if (expression != null) {
					annotationProperty.setValue(expression);
					((NormalAnnotation) result).values().add(annotationProperty);
				}

			}
		} else {
			result = ast.newMarkerAnnotation();
		}

		result.setTypeName(annotationTypeName);
		return result;
	}

	/**
	 * Creates an expression to be used as the 'value' part of a MemberValuePair
	 * on an annotation modifier
	 * 
	 * @param cu
	 *            Compilation unit to work on
	 * @param type
	 *            Type of value the annotation expects
	 * @param value
	 *            The value we want to create an expression for
	 * 
	 * @return The expression created
	 * @throws JavaModelException
	 */
	private Expression createAnnotationPropertyValueExpression(CompilationUnit cu, Class<?> type, Object value) throws JavaModelException {
		Expression expression = null;

		AST ast = cu.getAST();

		if (type.isAnnotation() && (value instanceof Map)) {
			return createModifier(ast, type, (Map) value, cu);
		} else if (value.getClass().isArray()) {
			Object[] objects = (Object[]) value;

			expression = ast.newArrayInitializer();

			for (Object object : objects) {
				Expression objExpr = createAnnotationPropertyValueExpression(cu, type.getComponentType(), object);
				((ArrayInitializer) expression).expressions().add(objExpr);
			}
		} else if ((value instanceof String) && (type == Class.class)) {
			expression = ast.newTypeLiteral();
			SimpleType newSimpleType = ast.newSimpleType(ast.newName((String) value));
			((TypeLiteral) expression).setType(newSimpleType);

			addImportToCompilationUnit((String) value, cu);
		} else if (value instanceof String) {
			expression = ast.newStringLiteral();
			((StringLiteral) expression).setLiteralValue(value.toString());
		} else if (value.getClass().isEnum()) {
			String enumClass = value.getClass().getSimpleName();
			String enumVal = value.toString();
			expression = ast.newQualifiedName(ast.newSimpleName(enumClass), ast.newSimpleName(enumVal));

			addImportToCompilationUnit(value.getClass().getCanonicalName(), cu);
		}

		return expression;
	}

	public void addMethodAnnotation(String fullyQualifiedClassName, String methodName, String[] signature, Class<?> annotationClass, Map<String, Object> properties) {
		try {
			CompilationUnit cu = compilationUnitCache.getCompilationUnit(fullyQualifiedClassName);
			MethodDeclaration method = compilationUnitCache.getMethodDeclaration(fullyQualifiedClassName, methodName, signature);
			if (method == null) {
				return;
			}
			Annotation modifier = createModifier(cu.getAST(), annotationClass, properties, cu);
			method.modifiers().add(0, modifier);

			addImportToCompilationUnit(annotationClass.getCanonicalName(), cu);
		} catch (CoreException e) {
			warnings.add(String.format(Messages.getString("org.apache.openejb.helper.annotation.warnings.3"), annotationClass.getCanonicalName(), methodName, fullyQualifiedClassName)); //$NON-NLS-1$
		}
	}

	private MethodDeclaration getMethodDeclaration(TypeDeclaration typeDeclaration, String methodName, String[] signature) {
		try {
			IType type = javaProject.findType(typeDeclaration.resolveBinding().getQualifiedName());
			IMethod method = type.getMethod(methodName, signature);

			return compilationUnitCache.getMethodDeclaration(method);
		} catch (JavaModelException e) {
		}
		
		return null;
	}

	

	public void addFieldAnnotation(String targetClass, String targetField, Class<?> annotation, Map<String, Object> properties) {
		try {
			CompilationUnit cu = compilationUnitCache.getCompilationUnit(targetClass);

			TypeDeclaration typeDeclaration = compilationUnitCache.getTypeDeclaration(targetClass);
			FieldDeclaration[] fields = typeDeclaration.getFields();

			Iterator<FieldDeclaration> iterator = Arrays.asList(fields).iterator();
			while (iterator.hasNext()) {
				FieldDeclaration field = iterator.next();
				if (field.fragments().size() == 0) {
					continue;
				}

				VariableDeclarationFragment varibleDeclaration = (VariableDeclarationFragment) field.fragments().get(0);
				if (varibleDeclaration.getName().toString().equals(targetField)) {
					Annotation modifier = createModifier(cu.getAST(), annotation, properties, cu);
					field.modifiers().add(0, modifier);

					addImportToCompilationUnit(annotation.getCanonicalName(), cu);
				}
			}

		} catch (CoreException e) {
			warnings.add(String.format(Messages.getString("org.apache.openejb.helper.annotation.warnings.4"), annotation.getCanonicalName(), targetField, targetClass)); //$NON-NLS-1$
		}
	}

	public Change getChange() {
		return compilationUnitCache.getChange();
	}

	public String[] getWarnings() {
		return warnings.toArray(new String[0]);
	}

	@SuppressWarnings("unchecked")//$NON-NLS-1$
	public void removeInterface(String targetClass, String interfaceToRemove) {
		try {
			TypeDeclaration typeDeclaration = compilationUnitCache.getTypeDeclaration(targetClass);

			Iterator iter = typeDeclaration.superInterfaceTypes().iterator();
			while (iter.hasNext()) {
				Object obj = iter.next();
				if (obj instanceof SimpleType) {
					SimpleType type = (SimpleType) obj;
					String qualifiedName = type.resolveBinding().getQualifiedName();

					if (qualifiedName.equals(interfaceToRemove)) {
						iter.remove();
					}
				}

			}
		} catch (Exception e) {
			warnings.add(String.format(Messages.getString("org.apache.openejb.helper.annotation.warnings.5"), interfaceToRemove, targetClass)); //$NON-NLS-1$
		}
	}

	public void removeAbstractModifierFromClass(String targetClass) {
		try {
			BodyDeclaration typeDeclaration = compilationUnitCache.getTypeDeclaration(targetClass);
			removeAbstractModifier(typeDeclaration.modifiers());
		} catch (Exception e) {
			warnings.add(String.format(Messages.getString("org.apache.openejb.helper.annotation.warnings.6"), targetClass)); //$NON-NLS-1$
		}
	}

	private void removeAbstractModifier(List modifiers) {
		Iterator iterator = modifiers.iterator();
		while (iterator.hasNext()) {
			IExtendedModifier modifier = (IExtendedModifier) iterator.next();
			if (modifier instanceof Modifier) {
				if (((Modifier) modifier).isAbstract()) {
					iterator.remove();
				}
			}
		}
	}

	public void removeAbstractModifierFromMethod(String targetClass, String methodName, String[] signature, String methodBody) {
		try {
			String code = methodBody;

			TypeDeclaration typeDeclaration = compilationUnitCache.getTypeDeclaration(targetClass);
			MethodDeclaration methodDeclaration = getMethodDeclaration(typeDeclaration, methodName, signature);
			removeAbstractModifier(methodDeclaration.modifiers());

			List parameters = methodDeclaration.parameters();
			for (int i = 0; i < parameters.size(); i++) {
				SingleVariableDeclaration parameter = (SingleVariableDeclaration) parameters.get(i);
				code = code.replaceAll("\\$\\{" + Integer.toString(i) + "\\}", parameter.resolveBinding().getName()); //$NON-NLS-1$ //$NON-NLS-2$
			}

			Block block = JDTUtils.parseBlock(code);
			block = (Block) ASTNode.copySubtree(methodDeclaration.getAST(), block);

			methodDeclaration.setBody((Block) block);
		} catch (Exception e) {
			warnings.add(String.format(Messages.getString("org.apache.openejb.helper.annotation.warnings.7"), targetClass, methodName)); //$NON-NLS-1$
		}
	}

	public boolean classImplements(String targetClass, String targetInterface) {
		try {
			TypeDeclaration typeDeclaration = compilationUnitCache.getTypeDeclaration(targetClass);

			Iterator iter = typeDeclaration.superInterfaceTypes().iterator();
			while (iter.hasNext()) {
				Object obj = iter.next();
				if (obj instanceof SimpleType) {
					SimpleType type = (SimpleType) obj;
					String qualifiedName = type.resolveBinding().getQualifiedName();

					if (qualifiedName.equals(targetInterface)) {
						return true;
					}
				}

			}
		} catch (Exception e) {
			warnings.add(String.format(Messages.getString("org.apache.openejb.helper.annotation.warnings.8"), targetClass)); //$NON-NLS-1$
		}

		return false;
	}

	public String getSuperClass(String targetClass) {
		try {
			TypeDeclaration type = compilationUnitCache.getTypeDeclaration(targetClass);
			Type superclassType = type.getSuperclassType();

			if (superclassType == null) {
				return targetClass;
			}

			return superclassType.resolveBinding().getQualifiedName();
		} catch (Exception e) {
			warnings.add(String.format(Messages.getString("org.apache.openejb.helper.annotation.warnings.9"), targetClass)); //$NON-NLS-1$
		}

		return ""; //$NON-NLS-1$
	}

	public String getMethodReturnType(String targetClass, String methodName, String[] signature) {
		try {
			MethodDeclaration methodDeclaration = compilationUnitCache.getMethodDeclaration(targetClass, methodName, signature);

			return methodDeclaration.resolveBinding().getReturnType().getQualifiedName();
		} catch (Exception e) {
			warnings.add(String.format(Messages.getString("org.apache.openejb.helper.annotation.warnings.10"), targetClass, methodName)); //$NON-NLS-1$
		}

		return null;
	}

	public void addField(String targetClass, String fieldName, String fieldType) {
		try {
			CompilationUnit compilationUnit = compilationUnitCache.getCompilationUnit(targetClass);
			TypeDeclaration typeDeclaration = compilationUnitCache.getTypeDeclaration(targetClass);

			VariableDeclarationFragment variableDeclaration = typeDeclaration.getAST().newVariableDeclarationFragment();
			variableDeclaration.setName(typeDeclaration.getAST().newSimpleName(fieldName));

			FieldDeclaration fieldDeclaration = typeDeclaration.getAST().newFieldDeclaration(variableDeclaration);
			Type type = JDTUtils.createQualifiedType(compilationUnit.getAST(), fieldType);
			fieldDeclaration.setType(type);
			Modifier privateModifier = fieldDeclaration.getAST().newModifier(ModifierKeyword.PRIVATE_KEYWORD);
			fieldDeclaration.modifiers().add(privateModifier);
			typeDeclaration.bodyDeclarations().add(fieldDeclaration);
		} catch (Exception e) {
			warnings.add(String.format(Messages.getString("org.apache.openejb.helper.annotation.warnings.11"), fieldName, targetClass)); //$NON-NLS-1$
		}

	}

	public void addAnnotationToFieldsOfType(String targetClass, Class<?> annotation, Map<String, Object> properties) {
		try {
			IType element = javaProject.findType(targetClass);

			SearchEngine searchEngine = new SearchEngine();
			SearchPattern pattern = SearchPattern.createPattern(element, IJavaSearchConstants.REFERENCES);
			SearchParticipant[] participants = new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() };
			IJavaSearchScope scope = SearchEngine.createWorkspaceScope();

			BasicSearchRequestor requestor = new BasicSearchRequestor();
			searchEngine.search(pattern, participants, scope, requestor, null);

			SearchMatch[] matches = requestor.getMatches();
			for (SearchMatch match : matches) {
				try {
					IJavaElement javaElement = (IJavaElement) ((PlatformObject) (match.getElement())).getAdapter(IJavaElement.class);

					// only support field
					if (javaElement.getElementType() != IJavaElement.FIELD) {
						continue;
					}

					// field name
					String name = javaElement.getElementName();

					// target class to add annotation to
					String foundClass = ((IType) javaElement.getParent()).getFullyQualifiedName();

					addFieldAnnotation(foundClass, name, annotation, properties);
				} catch (Exception e) {

				}
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void addInterface(String targetClass, String interfaceClass) {
		try {
			CompilationUnit cu = compilationUnitCache.getCompilationUnit(targetClass);
			addImportToCompilationUnit(interfaceClass, cu);

			TypeDeclaration typeDeclaration = compilationUnitCache.getTypeDeclaration(targetClass);
			AST ast = cu.getAST();
			SimpleType interfaceType = ast.newSimpleType(createQualifiedName(ast, interfaceClass));

			typeDeclaration.superInterfaceTypes().add(interfaceType);
		} catch (Exception e) {
			warnings.add(String.format(Messages.getString("org.apache.openejb.helper.annotation.warnings.12"), interfaceClass, targetClass)); //$NON-NLS-1$
		}
	}

	public void addWarning(String warning) {
		warnings.add(warning);
	}

	public void removeClassAnnotation(String targetClass, Class<?> cls) {
		try {
			TypeDeclaration typeDeclaration = compilationUnitCache.getTypeDeclaration(targetClass);

			List modifiers = typeDeclaration.modifiers();
			Iterator iterator = modifiers.iterator();

			while (iterator.hasNext()) {
				IExtendedModifier modifier = (IExtendedModifier) iterator.next();
				if (modifier.isAnnotation()) {
					Annotation annotation = (Annotation) modifier;
					if (cls.getCanonicalName().equals(annotation.resolveTypeBinding().getQualifiedName())) {
						iterator.remove();
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void convertMethodToConstructor(String className, String methodName, String[] signature) {
		try {
			TypeDeclaration typeDeclaration = compilationUnitCache.getTypeDeclaration(className);
			MethodDeclaration methodDeclaration = compilationUnitCache.getMethodDeclaration(className, methodName, signature);
			methodDeclaration.setConstructor(true);
			SimpleName newMethodName = methodDeclaration.getAST().newSimpleName(typeDeclaration.getName().toString());
			methodDeclaration.setName(newMethodName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void changeInvocationsToConstructor(String fromClass, final String fromMethodName, String[] fromSignature, final String toClass) {
		MethodDeclaration fromMethodDeclaration = compilationUnitCache.getMethodDeclaration(fromClass, fromMethodName, fromSignature);
		final IMethod fromMethod = (IMethod) fromMethodDeclaration.resolveBinding().getJavaElement();

		BlockModifier blockModifier = new ConvertMethodInvocationToConstructor(fromMethod, toClass);
		modifyBlocks(fromMethod, blockModifier);
	}

	private void modifyBlocks(final IMethod fromMethod, BlockModifier blockModifier) {
		try {
			SearchEngine searchEngine = new SearchEngine();
			SearchPattern pattern = SearchPattern.createPattern(fromMethod, IJavaSearchConstants.ALL_OCCURRENCES | IJavaSearchConstants.IGNORE_RETURN_TYPE);
			SearchParticipant[] participants = new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() };
			IJavaSearchScope scope = SearchEngine.createWorkspaceScope();

			BasicSearchRequestor requestor = new BasicSearchRequestor();
			searchEngine.search(pattern, participants, scope, requestor, null);

			SearchMatch[] matches = requestor.getMatches();
			for (SearchMatch match : matches) {
				try {
					if (!(match instanceof MethodReferenceMatch)) {
						continue;
					}

					IMethod javaElement = (IMethod) ((PlatformObject) (match.getElement())).getAdapter(IJavaElement.class);
					Block block = compilationUnitCache.getMethodDeclaration(javaElement).getBody();
					blockModifier.modify(block);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public List<String[]> getSignatures(String type, String methodName) {
		return compilationUnitCache.getSignatures(type, methodName);
	}

	public void changeInvocationsTo(String fromClass, String fromMethodName, String[] fromSignature, String code) {
		MethodDeclaration fromMethodDeclaration = compilationUnitCache.getMethodDeclaration(fromClass, fromMethodName, fromSignature);
		final IMethod fromMethod = (IMethod) fromMethodDeclaration.resolveBinding().getJavaElement();

		BlockModifier blockModifier = new ConvertMethodInvocationToCode(fromMethod, code);
		modifyBlocks(fromMethod, blockModifier);
	}

	public void addCodeToEndOfMethod(String className, String methodName, String[] signature, String code) {
		// TODO Auto-generated method stub
		
	}

	public void addCodeToStartOfMethod(String className, String methodName, String[] signature, String code) {
		// TODO Auto-generated method stub
		
	}

	public boolean isTypeCollection(String typeName) {
		if ("java.util.Collection".equals(typeName)) {
			return true;
		}
		
		try {
			IType type = javaProject.findType(typeName);
			if (type == null) {
				return false;
			}
			
			String[] superInterfaceNames = type.getSuperInterfaceNames();
			for (String superInterface : superInterfaceNames) {
				if ("java.util.Collection".equals(superInterface)) {
					return true;
				}
				
				if (isTypeCollection(superInterface)) {
					return true;
				}
			}
		} catch (JavaModelException e) {
		}
		
		return false;
	}
}

