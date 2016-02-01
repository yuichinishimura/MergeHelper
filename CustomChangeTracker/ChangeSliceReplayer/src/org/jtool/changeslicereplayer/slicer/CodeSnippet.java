/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changeslicereplayer.slicer;

import org.jtool.changerecorder.util.Time;

/**
 * Stores information on a code snippet.
 * @author Katsuhisa Maruyama
 */
public class CodeSnippet {
    
    /**
     * The offset value of the start point on this code snippet.
     */
    private int start;
    
    /**
     * The offset value of the end point on this code snippet.
     */
    private int end;
    
    /**
     * The sequence number of the snapshot containing this code snippet.
     */
    private int index;
    
    /**
     * The time when the snapshot containing this code snippet was generated.
     */
    private long time;
    
    /**
     * The contents of this code snippet.
     */
    private String text;
    
    /**
     * Creates an instance that stores information on the code snippet.
     * @param start the start point on the range of the code snippet
     * @param end the end point on the range of the code snippet
     * @param idx the sequence number of the snapshot containing the code snippet
     * @param time the time when the snapshot containing the code snippet was generated
     * @param text the contents of the code snippet
     */
    public CodeSnippet(int start, int end, int idx, long time, String text) {
        if (start > end) {
            this.start = end;
            this.end = start;
        } else {
            this.start = start;
            this.end = end;
        }
        this.index = idx;
        this.time = time;
        this.text = text;
    }
    
    /**
     * Returns the start point on this code range.
     * @return the offset value of this start point
     */
    public int getStart() {
        return start;
    }
    
    /**
     * Returns the start point on this code range.
     * @return the offset value of this start point
     */
    public int getEnd() {
        return end;
    }
    
    /**
     * Tests if the offset value of a character is in this code snippet.
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
     * Returns the sequence number of the snapshot containing this code snippet
     * @return the sequence number for the code snippet
     */
    public long getIndex() {
        return index;
    }
    
    /**
     * Returns the time when the snapshot containing the code snippet was generated.
     * @return the time for the code snippet
     */
    public long getTime() {
        return time;
    }
    
    /**
     * Returns the contents of the code snippet.
     * @return the contents of the code snippet
     */
    public String getText() {
        return text;
    }
    
    /**
     * Returns the string for representing the range of this snippet.
     * @return the text for representing the range
     */
    public String getRangeString() {
        StringBuilder buf = new StringBuilder();
        buf.append("[");
        buf.append(String.valueOf(getStart()));
        buf.append("-");
        buf.append(String.valueOf(getEnd()));
        buf.append("]");
        return buf.toString();
    }
    
    /**
     * Returns the string for printing, which does not contain a new line character at its end.
     * @return the string for printing
     */
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(toSimpleString());
        buf.append(getText());
        return buf.toString();
    }
    
    /**
     * Returns the string for printing, which does not contain a new line character at its end.
     * @return the string for printing
     */
    public String toSimpleString() {
        StringBuilder buf = new StringBuilder();
        buf.append(index);
        buf.append(" ");
        buf.append(Time.toUsefulFormat(getTime()));
        buf.append(" ");
        buf.append(getRangeString());
        return buf.toString();
    }
}
