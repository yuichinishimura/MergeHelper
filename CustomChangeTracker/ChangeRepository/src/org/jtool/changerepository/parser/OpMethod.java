/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changerepository.parser;

import org.jtool.changerepository.data.FileInfo;

/**
 * Stores information on a method.
 * @author Katsuhisa Maruyama
 */
public class OpMethod extends OpJavaElement {
    
    /**
     * Creates an instance that stores information on the method.
     * @param start the start point on the source code for the method
     * @param end the end point on the source code for the method
     * @param finfo the information on the file containing the method
     * @param name the name of the method
     */
    public OpMethod(int start, int end, FileInfo finfo, String name) {
        super(start, end, finfo, name);
    }
}
