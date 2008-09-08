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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class CompilationUnitCache {

	Map<ICompilationUnit, CompilationUnit> cache = new HashMap<ICompilationUnit, CompilationUnit>();
	
	public CompilationUnit getCompilationUnit(ICompilationUnit compilationUnit) {
		if (cache.containsKey(compilationUnit)) {
			return cache.get(compilationUnit);
		}
		
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(compilationUnit);
		parser.setResolveBindings(true);
		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		
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

}
