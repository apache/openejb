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

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;

public class JDTUtils {

	public static Type createQualifiedType(AST ast, String targetClass) {
		String[] parts = targetClass.split("\\."); //$NON-NLS-1$

		Type type = null;

		for (int i = 0; i < parts.length; i++) {
			SimpleName name = ast.newSimpleName(parts[i]);
			if (i == 0) {
				type = ast.newSimpleType(name);
			} else {
				type = ast.newQualifiedType(type, name);
			}
		}

		return type;
	}

	public static Block parseBlock(String block) {
		char[] source = block.toCharArray();

		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_STATEMENTS);

		parser.setSource(source);
		parser.setResolveBindings(true);

		Block val = (Block) parser.createAST(null);
		return val;
	}

}
