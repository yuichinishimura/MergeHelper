/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changerepository.parser;

import org.jtool.changerepository.data.FileInfo;

/**
 * Stores information on a field.
 * @author Katsuhisa Maruyama
 */
public class OpField extends OpJavaElement {
    
    /**
     * Creates an instance that stores information on the field.
     * @param start the start point on the source code for the field
     * @param end the end point on the source code for the field
     * @param finfo the information on the file containing the field
     * @param name the name of the field
     */
    public OpField(int start, int end, FileInfo finfo, String name) {
        super(start, end, finfo, name);
    }
}
