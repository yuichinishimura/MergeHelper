/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changerepository.parser;

import org.jtool.changerepository.data.FileInfo;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Collects members within the compilation unit.
 * @author Katsuhisa Maruyama
 */
public class OpJavaVisitor extends ASTVisitor {
    
    /**
     * The file information on the source code.
     */
    private FileInfo finfo;
    
    /**
     * The collection of Java elements within the source code.
     */
    private List<OpJavaElement> elements = new ArrayList<OpJavaElement>();
        
    /**
     * The stack that stores classes.
     */
    private Stack<OpClass> classes = new Stack<OpClass>();
    
    /**
     * The stack that stores parent elements.
     */
    private Stack<OpJavaElement> parents = new Stack<OpJavaElement>();
    
    /**
     * Creates a visitor visiting the AST.
     */
    public OpJavaVisitor(FileInfo finfo) {
        this.finfo = finfo;
    }
    
    /**
     * Returns the Java elements within the source code.
     * @return the collection of the Java elements
     */
    public List<OpJavaElement> getJavaElements() {
        return elements;
    }
    
    /**
     * Stores class information and collects Java elements within a class.
     * @param node the type node of the AST
     * @return always <code>true</code> to visit its child nodes next
     */
    @Override
    public boolean visit(TypeDeclaration node) {
        int start = node.getStartPosition();
        int end = start + node.getLength() - 1;
        String name = node.getName().getFullyQualifiedName();
        OpClass clazz = new OpClass(start, end, finfo, name);
        
        if (parents.size() > 0) {
            elements.add(clazz);
            OpJavaElement parent = (OpJavaElement)parents.peek();
            parent.addJavaElement(clazz);
        }
        
        parents.push(clazz);
        classes.push(clazz);
        
        return true;
    }
    
    /**
     * Discards the visited class.
     * @param node the visited node
     */
    @Override
    public void endVisit(TypeDeclaration node) {
        classes.pop();
        parents.pop();
    }
    
    /**
     * Stores class information and collects Java elements within an enum.
     * @param node the enum node of the AST
     * @return always <code>true</code> to visit its child nodes next
     */
    @Override
    public boolean visit(EnumDeclaration node) {
        int start = node.getStartPosition();
        int end = start + node.getLength() - 1;
        String name = node.getName().getFullyQualifiedName();
        OpClass clazz = new OpClass(start, end, finfo, name);
        
        if (parents.size() > 0) {
            elements.add(clazz);
            OpJavaElement parent = (OpJavaElement)parents.peek();
            parent.addJavaElement(clazz);
        }
        
        parents.push(clazz);
        classes.push(clazz);
        
        return true;
    }
    
    /**
     * Discards the visited enum.
     * @param node the visited node
     */
    @Override
    public void endVisit(EnumDeclaration node) {
        classes.pop();
        parents.pop();
    }
    
    /**
     * Stores class information and collects Java elements within the class
     * @param node the type node of the AST
     * @return always <code>true</code> to visit its child nodes next
     */
    @Override
    public boolean visit(AnonymousClassDeclaration node) {
        int start = node.getStartPosition();
        int end = start + node.getLength() - 1;
        String name = "$";
        OpClass clazz = new OpClass(start, end, finfo, name);
        
        if (parents.size() > 0) {
            elements.add(clazz);
            OpJavaElement parent = (OpJavaElement)parents.peek();
            parent.addJavaElement(clazz);
        }
        parents.push(clazz);
        classes.push(clazz);
        
        return true;
    }
    
    /**
     * Discards the visited class.
     * @param node the visited node
     */
    @Override
    public void endVisit(AnonymousClassDeclaration node) {
        classes.pop();
        parents.pop();
    }
    
    /**
     * Returns the name of a class that is currently visited.
     * @return the class name.
     */
    private String getClassName() {
        StringBuffer buf = new StringBuffer();
        for (OpClass c: classes) {
            buf.append("%");
            buf.append(c.getName());
        }
        return buf.substring(1);
    }
    
    /**
     * Stores method information and collects Java elements within the method
     * @param node the method node of the AST
     * @return always <code>true</code> to visit its child nodes next
     */
    @Override
    public boolean visit(MethodDeclaration node) {
        int start = node.getStartPosition();
        int end = start + node.getLength() - 1;
        @SuppressWarnings("unchecked")
        String name = getClassName() + "#" + node.getName().getIdentifier() + getFormalParameters(node.parameters());
        
        OpMethod method = new OpMethod(start, end, finfo, name);
        elements.add(method);
        
        if (parents.size() > 0) {
            OpJavaElement parent = (OpJavaElement)parents.peek();
            parent.addJavaElement(method);
        }
        parents.push(method);
        
        return true;
    }
    
    /**
     * Discards the visited method.
     * @param node the visited node
     */
    @Override
    public void endVisit(MethodDeclaration node) {
        parents.pop();
    }
    
    /**
     * Stores initializer information and collects Java elements within the initializer
     * @param node the initializer node of the AST
     * @return always <code>true</code> to visit its child nodes next
     */
    @Override
    public boolean visit(Initializer node) {
        int start = node.getStartPosition();
        int end = start + node.getLength() - 1;
        String name = getClassName() + "#" + "$Init()";
        
        OpMethod method = new OpMethod(start, end, finfo, name);
        elements.add(method);
        
        if (parents.size() > 0) {
            OpJavaElement parent = (OpJavaElement)parents.peek();
            parent.addJavaElement(method);
        }
        parents.push(method);
        
        return true;
    }
    
    /**
     * Discards the visited initializer.
     * @param node the visited node
     */
    @Override
    public void endVisit(Initializer node) {
        parents.pop();
    }
    
    /**
     * Returns the string representing information on the parameters of the method.
     * @param params the parameters of the method
     * @return the string representing the parameters
     */
    private String getFormalParameters(List<SingleVariableDeclaration> params) {
        StringBuffer buf = new StringBuffer();
        for (SingleVariableDeclaration param : params) {
            buf.append(" ");
            buf.append(param.toString());
        }
        if (buf.length() != 0) {
            return "(" + buf.substring(1) + ")";
        }
        return "()";
    }
    
    /**
     * Stores field information and collects Java elements within the field
     * @param node the field node of the AST
     * @return always <code>true</code> to visit its child nodes next
     */
    @Override
    public boolean visit(FieldDeclaration node) {
        int start = node.getStartPosition();
        int end = node.getStartPosition() + node.getLength() - 1;
        
        @SuppressWarnings("unchecked")
        List<VariableDeclarationFragment> fields = (List<VariableDeclarationFragment>)node.fragments();
        Map<OpField, CodeRange> franges = new HashMap<OpField, CodeRange>();
        for (VariableDeclarationFragment f : fields) {
            String name = getClassName() + "#" + f.getName().getIdentifier();
            OpField field = new OpField(start, end, finfo, name);
            elements.add(field);
            
            int fstart = f.getStartPosition();
            int fend = fstart + f.getLength() - 1;
            franges.put(field, new CodeRange(fstart, fend)); 
        }
        
        for (OpField f: franges.keySet()) {
            for (CodeRange r : franges.values()) {
                if (franges.get(f) != r) {
                    f.addExcludedCodeRange(r.getStart(), r.getEnd());
                }
            }
        }
        
        OpField wfield = new OpField(start, end, finfo, "");
        if (parents.size() > 0) {
            OpJavaElement parent = (OpJavaElement)parents.peek();
            parent.addJavaElement(wfield);
        }
        parents.push(wfield);
        
        return true;
    }
    
    /**
     * Discards the visited field.
     * @param node the visited node
     */
    @Override
    public void endVisit(FieldDeclaration node) {
        parents.pop();
    }
}
