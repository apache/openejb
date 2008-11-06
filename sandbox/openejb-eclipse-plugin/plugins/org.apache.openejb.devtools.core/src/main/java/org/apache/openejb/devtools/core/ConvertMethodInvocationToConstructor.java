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

import java.util.List;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public final class ConvertMethodInvocationToConstructor implements BlockModifier {
	private final String toClass;
	private final IMethod fromMethod;

	public ConvertMethodInvocationToConstructor(IMethod fromMethod, String toClass) {
		this.toClass = toClass;
		this.fromMethod = fromMethod;
	}

	public void modify(Block block) {
		block.accept(new ASTVisitor() {

			@SuppressWarnings("unchecked")
			@Override
			public boolean visit(MethodInvocation node) {
					IMethod invokedMethod = (IMethod) node.resolveMethodBinding().getJavaElement();
					
					try {
						if (invokedMethod.getElementName().equals(fromMethod.getElementName())
							&& invokedMethod.getSignature().equals(fromMethod.getSignature())){

							ASTNode varDeclarationStmt = node.getParent().getParent();
							if (varDeclarationStmt instanceof VariableDeclarationStatement) {
								VariableDeclarationStatement vds = (VariableDeclarationStatement) varDeclarationStmt;
								
								// change the type
								Type returnType = JDTUtils.createQualifiedType(vds.getAST(), toClass);
								vds.setType(returnType);
							}
							
							ASTNode varDeclarationFragment = node.getParent();
							if (varDeclarationFragment instanceof VariableDeclarationFragment) {
								VariableDeclarationFragment vdf = (VariableDeclarationFragment) varDeclarationFragment;
								
								ClassInstanceCreation newClassInstanceCreation = vdf.getAST().newClassInstanceCreation();
								Type newType = JDTUtils.createQualifiedType(vdf.getAST(), toClass);
								newClassInstanceCreation.setType(newType);
								List arguments = newClassInstanceCreation.arguments();
								arguments.clear();
								
								MethodInvocation initializer = (MethodInvocation) vdf.getInitializer();
								List newArguments = ASTNode.copySubtrees(newClassInstanceCreation.getAST(), initializer.arguments());
								arguments.addAll(newArguments);
									
								vdf.setInitializer(newClassInstanceCreation);
							}
						}
					} catch (JavaModelException e) {
						e.printStackTrace();
					}

				return true;
			}
		});
	}
}
