/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changerepository.parser;

/**
 * Stores information on the range of code.
 * @author Katsuhisa Maruyama
 */
public class CodeRange {
    
    /**
     * The offset value of the start point on the source code.
     */
    protected int start;
    
    /**
     * The offset value of the end point on the source code.
     */
    protected int end;
    
    /**
     * Creates an instance that stores information on the code range.
     * @param start the start point on the range of source code
     * @param end the end point on the range of source code
     */
    public CodeRange(int start, int end) {
        if (start > end) {
            this.start = end;
            this.end = start;
        } else {
            this.start = start;
            this.end = end;
        }
    }
    
    /**
     * Returns the start point on this code range.
     * @return the offset value of this start point.
     */
    public int getStart() {
        return start;
    }
    
    /**
     * Returns the start point on this code range.
     * @return the offset value of this start point.
     */
    public int getEnd() {
        return end;
    }
    
    /**
     * Tests if this code range is valid.
     * @return <code>true</code> if the code range is valid, otherwise <code>false</code>
     */
    public boolean isValid() {
        return start <= end;
    }
    
    /**
     * Tests if the offset value of a character is in this code range.
     * @param offset the offset value to be checked
     * @return <code>true</code> if the offset value is in, otherwise <code>false</code>
     */
    public boolean inRange(int offset) {
        if (start <= offset && offset <= end) {
            return true;
        }
        return false;
    }
    
    /**
     * Tests if the offset value of a character is in this code range.
     * @param offset the offset value to be checked
     * @return <code>true</code> if the offset value is in, or <code>false</code> if the offset value is not in or is equals to the start value 
     */
    public boolean inRangeMore(int offset) {
        if (start < offset && offset <= end) {
            return true;
        }
        return false;
    }
    
    /**
     * Tests if the offset value of a character is in this code range.
     * @param offset the offset value to be checked
     * @return <code>true</code> if the offset value is in, or <code>false</code> if the offset value is not in or is equals to the end value 
     */
    public boolean inRangeLess(int offset) {
        if (start <= offset && offset < end) {
            return true;
        }
        return false;
    }
    
    /**
     * Tests if the offset value is in this code range.   
     * @param range the code range to be checked
     * @return <code>true</code> if the offset value is in, otherwise <code>false</code>
     */
    public boolean inRangeTotally(CodeRange range) {
        if (start <= range.getStart() && range.getEnd() <= end) {
            return true;
        }
        return false;
    }
    
    /**
     * Tests if the offset value is in this code range.   
     * @param range the code range to be checked
     * @return <code>true</code> if the offset value is in, otherwise <code>false</code>
     */
    public boolean inRangePartially(CodeRange range) {
        if (range.getEnd() < start || end < range.getStart()) {
            return false;
        }
        return true;
    }
    
    /**
     * Returns the string for printing, which does not contain a new line character at its end. 
     * @return the string for printing
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("[");
        buf.append(String.valueOf(getStart()));
        buf.append("-");
        buf.append(String.valueOf(getEnd()));
        buf.append("] ");
        return buf.toString();
    }
}
