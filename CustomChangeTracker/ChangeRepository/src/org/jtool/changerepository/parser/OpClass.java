/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changerepository.parser;

import org.jtool.changerepository.data.FileInfo;

/**
 * Stores information on a class.
 * @author Katsuhisa Maruyama
 */
public class OpClass extends OpJavaElement {
    
    /**
     * Creates an instance that stores information on the class.
     * @param start the start point on the source code for the class
     * @param end the end point on the source code for the class
     * @param finfo the information on the file containing the class
     * @param name the name of the class
     */
    public OpClass(int start, int end, FileInfo finfo, String name) {
        super(start, end, finfo, name);
    }
}
