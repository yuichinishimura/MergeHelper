/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changerepository.parser;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import java.util.List;
import java.util.ArrayList;

/**
 * Stores information on the compilation unit.
 * @author Katsuhisa Maruyama
 */
public class OpCompilationUnit {
    
    /**
     * The Eclipse's compilation unit object corresponding to this compilation unit.
     */
    private ICompilationUnit compilationUnit;
    
    /**
     * The file path of this compilation unit.
     */
    private String path;
    
    /**
     * The collections of Java elements within this compilation unit.
     */
    private ArrayList<OpJavaElement> elements;
    
    /**
     * The time when this compilation unit exists.
     */
    private long time;
    
    /**
     * The index number indicating this compilation unit.
     */
    private int index;
    
    /**
     * Creates an instance that stores information on the given compilation unit.
     * @param cu the compilation unit
     * @param path a file path of the compilation unit
     * @param time the time when the compilation unit exists
     * @param idx the index number of the compilation unit
     * @param members the collection of the class members within the compilation unit
     */
    public OpCompilationUnit(CompilationUnit cu, String path, long time, int idx, ArrayList<OpJavaElement> elements) {
        compilationUnit = (ICompilationUnit)cu.getJavaElement();
        this.path = path;
        this.time = time;
        this.index = idx;
        this.elements = elements;
    }
    
    /**
     * Returns a compilation unit stored in this compilation unit.
     * @return An Eclipse' compilation unit
     */
    public ICompilationUnit getCompilationUnit() {
        return compilationUnit;
    }
    
    /**
     * Returns the path of the original source file.
     * @return the path string.
     */
    public String getFilePath() {
        return path;
    }
    
    /**
     * Returns the time when this compilation unit exists.
     * @return the time of this compilation unit
     */
    public long getTime() {
        return time;
    }
    
    /**
     * Return the index number indicating this compilation unit.
     * @return the index number of this compilation unit
     */
    public int getIndex() {
        return index;
    }
    
    /**
     * Returns the Java elements within the source code.
     * @return the collection of the Java elements
     */
    public List<OpJavaElement> getJavaElements() {
        return elements;
    }
    
    /**
     * Restores the content of the working copy of this compilation unit to the current content.
     * @return <code>true</code> if the original of this compilation unit exists, otherwise <code>false</code>.
     */
    protected boolean restore() {
        try {
            compilationUnit.restore();
        } catch (JavaModelException e) {
            return false;
        }
        return true;
    }
    
    /**
     * Reconciles the contents of the working copy of this compilation unit.
     * @return the compilation unit.
     */
    protected OpCompilationUnit reconcile() {
        try {
            CompilationUnit cu = compilationUnit.reconcile(AST.JLS8, true, null, null);
            return new OpCompilationUnit(cu, path, time, index,elements);
        } catch (JavaModelException e) {
            return null;
        }
    }
    
    /**
     * Returns the file resource for the stored compilation unit.
     * @return the Eclipse's file resource.
     */
    public IFile getIFile() {
        try {
            IResource resource = compilationUnit.getCorrespondingResource();
            if (resource instanceof IFile) {
                return (IFile)resource;
            }
        } catch (CoreException e) {
            // do nothing
        }
        return null;
    }
    
    /**
     * Obtains the source code stored in this compilation unit.
     * @return the text of the source code.
     */
    public String getOriginalCode() {
        try {
            return compilationUnit.getSource();
        } catch (JavaModelException e) {
            // do nothing
        }
        return null;
    }
}
