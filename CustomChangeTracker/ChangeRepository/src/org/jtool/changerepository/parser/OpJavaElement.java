/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changerepository.parser;

import org.jtool.changerepository.data.FileInfo;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Stores information on a Java element (a class, method, or field) of source code.
 * @author Katsuhisa Maruyama
 */
public class OpJavaElement {
    
    /**
     * The file information.
     */
    protected FileInfo finfo;
    
    /**
     * The name for this member.
     */
    protected String name;
    
    /**
     * The code range that stores two offset values of the start and end points on the source code for this member.
     */
    protected CodeRange range;
    
    /**
     * The collection of ranges that indicate where this Java element exists.
     */
    private List<CodeRange> ranges = null;
    
    /**
     * The code ranges of code fragments that are not included in the whole code range.
     */
    protected List<CodeRange> excludedRanges = new ArrayList<CodeRange>();
    
    /**
     * The Java elements enclosed in this element.
     */
    protected List<OpJavaElement> elements = new ArrayList<OpJavaElement>(); 
    
    /**
     * Creates an instance that stores information on a Java element.
     * @param start the start point on the source code for the element
     * @param end the end point on the source code for the element
     * @param name the name of the element
     */
    public OpJavaElement(int start, int end, FileInfo finfo, String name) {
        range = new CodeRange(start, end);
        this.finfo = finfo;
        this.name = name;
    }
    
    /**
     * Returns the code range of this Java element.
     * @return the code range of this element
     */
    public CodeRange getCodeRange() {
        return range;
    }
    
    /**
     * Returns the start point on the source code for this Java element.
     * @return the offset value of the start point.
     */
    public int getStart() {
        return range.getStart();
    }
    
    /**
     * Returns the start point on the source code for this Java element.
     * @return the offset value of the start point.
     */
    public int getEnd() {
        return range.getEnd();
    }
    
    /**
     * Returns the name of this Java element.
     * @return the element name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the full name of this Java element.
     * @return the element name with its file name
     */
    public String getFullName() {
        return finfo.getQualifiedName() + ":" + name;
    }
    
    /**
     * Returns the simple name of this Java element.
     * @return the element name not containing class name
     */
    public String getSimpleName() {
        int index = name.indexOf('#');
        if (index != -1) {
            return name.substring(index + 1);
        }
        return "";
    }
    
    /**
     * Adds the code range that indicates exclusion of a code fragment
     * @param start the start point of the excluded code range 
     * @param end the end point of the excluded code range
     */
    public void addExcludedCodeRange(int start, int end) {
        CodeRange r = new CodeRange(start, end);
        if (range.inRangePartially(r)) {
            excludedRanges.add(r);
            sortByEnd(excludedRanges);
            sortByStart(excludedRanges);
        }
    }
    
    /**
     * Adds a Java element enclosed in this element. It is considered to be excluded from this element under range check.
     * @param member the element included in this element.
     */
    public void addJavaElement(OpJavaElement element) {
        elements.add(element);
        addExcludedCodeRange(element.getStart(), element.getEnd());
    }
    
    /**
     * Returns the elements enclosed in this Java element.
     * @return the collection of the elements.
     */
    public List<OpJavaElement> getEnclosedJavaElements() {
        return elements;
    }
    
    /**
     * Returns the ranges that correspond to this Java element, not including the excluded code.
     * @return the collection of the ranges
     */
    public List<CodeRange> getRanges() {
        if (ranges != null) {
            return ranges;
        }
        
        ranges = new ArrayList<CodeRange>();
        if (excludedRanges.size() == 0) {
            ranges.add(new CodeRange(range.getStart(), range.getEnd()));
            return ranges;
        }
        
        int len = range.getEnd() - range.getStart() + 1;
        boolean code[] = new boolean[len];
        for (int i = 0; i < len; i++) {
            code[i] = true;
        }
        
        for (CodeRange r : excludedRanges) {
            for (int j = r.getStart(); j <= r.getEnd(); j++) {
                if (range.inRange(j)) {
                    code[j - range.getStart()] = false;
                }
            }
        }
        
        int start = -1;
        int end = -1;
        for (int i = 0; i < len; i++) {
            if (code[i]) {
                if (start < 0) {
                    start = i;
                    if (i == len - 1) {
                        ranges.add(new CodeRange(start + range.getStart(), start + range.getStart()));
                    }
                    
                } else {
                    end = i;
                    if (i == len - 1) {
                        ranges.add(new CodeRange(start + range.getStart(), end + range.getStart()));
                    }
                }
                
            } else {
                if (start >= 0) {
                    end = i - 1;
                    ranges.add(new CodeRange(start + range.getStart(), end + range.getStart()));
                    
                    start = -1;
                    end = -1;
                }
            }
        }
        
        return ranges;
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
        
        if (excludedRanges.size() > 0) {
            buf.append("!");
            for (CodeRange r : excludedRanges) {
                buf.append("[");
                buf.append(String.valueOf(r.getStart()));
                buf.append("-");
                buf.append(String.valueOf(r.getEnd()));
                buf.append("] ");
            }
        }
        
        buf.append(getFullName());
        return buf.toString();
    }
    
    /**
     * Sorts the code range of this Java element.
     * @param es the collection of the code ranges to be sorted
     */
    private static void sortByStart(List<CodeRange> r) {
        Collections.sort(r, new Comparator<CodeRange>() {
            public int compare(CodeRange r1, CodeRange r2) {
                int start1 = r1.getStart();
                int start2 = r2.getStart();
                if (start2 > start1) {
                    return -1;
                } else if (start2 == start1) {
                    return 0;
                }else{
                    return 1;
                }
            }
        });
    }
    
    /**
     * Sorts the edges of the operation graph.
     * @param es the collection of the edges to be sorted
     */
    private static void sortByEnd(List<CodeRange> r) {
        Collections.sort(r, new Comparator<CodeRange>() {
            public int compare(CodeRange r1, CodeRange r2) {
                int start1 = r1.getEnd();
                int start2 = r2.getEnd();
                if (start2 > start1) {
                    return -1;
                } else if (start2 == start1) {
                    return 0;
                }else{
                    return 1;
                }
            }
        });
    }
}
