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
