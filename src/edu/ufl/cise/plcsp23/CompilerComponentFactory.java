/*Copyright 2023 by Beverly A Sanders
 * 
 * This code is provided for solely for use of students in COP4020 Programming Language Concepts at the 
 * University of Florida during the spring semester 2023 as part of the course project.  
 * 
 * No other use is authorized. 
 * 
 * This code may not be posted on a public web site either during or after the course.  
 */

package edu.ufl.cise.plcsp23;

import edu.ufl.cise.plcsp23.ast.ASTVisitor;

public class CompilerComponentFactory {
	public static IScanner makeScanner(String input) {
		//Add statement to return an instance of your scanner
		// return obj of scanner class
		return new Scanner(input);
	}

	public static IParser makeAssignment2Parser(String input) throws LexicalException {
		//add code to create a scanner and parser and return the parser.

		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		Parser parser = new Parser(scanner);

		return parser; // constructor.
	}


	public static IParser makeParser(String input) throws LexicalException {
			//add code to create a scanner and parser and return the parser.
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		Parser parser = new Parser(scanner);

		return parser; // constructor.

	}

	public static ASTVisitor makeTypeChecker() throws LexicalException, PLCException {
		//code to instantiate a return an ASTVisitor for type checking
		return new TypeCheck();
	}

	public static ASTVisitor makeCodeGenerator(String packageName) {
		//code to instantiate a return an ASTVisitor for code generation
		return new CodeGen(packageName);

	}
}
