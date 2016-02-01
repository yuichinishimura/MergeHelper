/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changerepository.parser;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import java.util.Hashtable;

/**
 * Creates the abstract syntax tree (AST) for the Java source code.
 * @author Katsuhisa Maruyama
 */
public class OpJavaParser {
    
    /**
     * The parser.
     */
    private ASTParser parser;
    
    /**
     * The Eclipse's compilation unit corresponding to this.
     */
    private CompilationUnit compilationUnit = null;
    
    public OpJavaParser() {
        parser = ASTParser.newParser(AST.JLS8);
    }
    
    /**
     * Parses Java source code represented by the specified text.
     * @param text the string representing the Java source code
     * @return <code>true</code> if parsing succeeds, otherwise <code>false</code>
     */
    public boolean parse(String text) {
        return parse(text.toCharArray());
    }
    
    /**
     * Parses Java source code represented by the specified char array.
     * @param code the array of characters representing the Java source code
     * @return <code>true</code> if parsing succeeds, otherwise <code>false</code>
     */
    @SuppressWarnings("unchecked")
    private boolean parse(char[] code) {
        Hashtable<String, String> options = (Hashtable<String, String>)JavaCore.getOptions();
        options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
        options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
        options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
        parser.setCompilerOptions(options);
        
        parser.setSource(code);
        parser.setResolveBindings(false);
        
        ASTNode node = parser.createAST(null);
        if (node instanceof CompilationUnit) {
            compilationUnit = (CompilationUnit)node;
            // if (!((compilationUnit.getFlags() & ASTNode.MALFORMED) == ASTNode.MALFORMED)) {
            for (IProblem problem : compilationUnit.getProblems()) {
                if (problem.isError()) {
                    // System.out.println("SYNTAX ERROR " + problem.toString());
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    
    /**
     * Returns the problems resulting from parsing.
     * @return the parse problems 
     */
    public IProblem[] getProblems() {
        return compilationUnit.getProblems();
    }
     
    /**
     * Returns the compilation unit representing the result of parsing.
     * @return the compilation unit
     */
    public CompilationUnit getCompilationUnit() {
        return compilationUnit;
    }
}
