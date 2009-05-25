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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class ConvertMethodInvocationToCode implements BlockModifier {

	private final IMethod fromMethod;
	private final String code;
	public ConvertMethodInvocationToCode(IMethod fromMethod, String code) {
		this.fromMethod = fromMethod;
		this.code = code;
	}

	private Block parseCode(String code) {
		return JDTUtils.parseBlock(code);
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

							Block newBlock = parseCode(code);
							
							ASTNode target = node.getParent();
							
							if (target instanceof VariableDeclarationFragment) {
								target = (Statement) target.getParent();
							}
							
							Block block = getBlock(target);
							
							int index = 0;
							
							for (int i = 0; i < block.statements().size(); i++) {
								if (block.statements().get(i).equals(target)) {
									index = i;
									break;
								}
							}
							
							List statements = ASTNode.copySubtrees(node.getAST(), newBlock.statements());
							
							for (int i = statements.size() - 1; i >= 0; i--) {
								block.statements().add(index, statements.get(i));
							}
							
							int expressionIndex = index + statements.size() - 1;
							boolean assignmentAdded = false;
							
							if (node.getParent() instanceof VariableDeclarationFragment) {
								Statement expressionStatement = (Statement) block.statements().get(expressionIndex);
								
								if (expressionStatement instanceof ExpressionStatement) {
									VariableDeclarationFragment vdf = (VariableDeclarationFragment) node.getParent();
									Expression expression = (Expression) ASTNode.copySubtree(node.getAST(), ((ExpressionStatement) expressionStatement).getExpression());
									vdf.setInitializer(expression);
									block.statements().remove(expressionIndex);
									assignmentAdded = true;
								}
							} 
							
							if (! assignmentAdded) {
								block.statements().remove(expressionIndex + 1);
							}
						}
					} catch (JavaModelException e) {
						e.printStackTrace();
					}

				return true;
			}

			@SuppressWarnings("unchecked")
			private Block getBlock(ASTNode target) {
				ASTNode parent = target.getParent();
				if (parent instanceof Block) {
					return (Block) parent;
				}
				
				Block newBlock = target.getAST().newBlock();
				newBlock.statements().add(ASTNode.copySubtree(target.getAST(), target));
				Field[] declaredFields = parent.getClass().getDeclaredFields();
				for (Field field : declaredFields) {
					if (!(field.getType().isAssignableFrom(Statement.class))) {
						continue;
					}
					
					try {
						field.setAccessible(true);
						if (field.get(parent).equals(target)) {
							String setterName = "set" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
							Method setter = parent.getClass().getDeclaredMethod(setterName, field.getType());
							if (setter != null) {
								setter.invoke(parent, newBlock);
							}
						}
					} catch (Exception e) {
					}
				}
				
				return newBlock;
			}
		});
	}
}
