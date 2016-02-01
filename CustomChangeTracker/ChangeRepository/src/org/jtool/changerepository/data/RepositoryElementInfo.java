/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changerepository.data;

import org.jtool.changerepository.operation.UnifiedOperation;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Stores information on an element stored into the repository.
 * @author Katsuhisa Maruyama
 */
public class RepositoryElementInfo {
    
    /**
     * The name of this element.
     */
    protected String name;
    
    /**
     * The operations related to this element.
     */
    protected List<UnifiedOperation> operations = new ArrayList<UnifiedOperation>();
    
    /**
     * The time range for this element.
     */
    protected TimeRange timeRange;
    
    /**
     * Creates an instance that stores information on this element.
     * @param name the name of this element
     * @param from the time when this element was created in the repository
     */
    public RepositoryElementInfo(String name) {
        this.name = name;
    }
    
    /**
     * Returns the name of this element.
     * @return the element name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Stores the operation which was performed on a file within this project.
     * @param op the operation to be stored
     */
    public void addOperation(UnifiedOperation op) {
        operations.add(op);
    }
    
    /**
     * Returns all the operations performed on files within this project.
     * @return the collection of the performed operations
     */
    public List<UnifiedOperation> getOperations() {
        return operations;
    }
    
    /**
     * Returns the operation with the sequence number.
     * @param idx the sequence number of an operation to be retrieved
     * @return the found operation operation, or <code>null</code> if none
     */
    public UnifiedOperation getOperation(int idx) {
        if (idx < 0 || idx >= operations.size()) {
            return null;
        }
        return operations.get(idx);
    }
    
    /**
     * Returns the number of the operations performed on files within this project.
     * @return the number of the operations
     */
    public int getOperationNumber() {
        return operations.size();
    }
    
    /**
     * Sets the time range for this element.
     */
    protected void setTimeRange() {
        long from = -1;
        long to = -1;
        if (operations.size() > 0) {
            from = operations.get(0).getTime();
            to = operations.get(operations.size() - 1).getTime();
        }
        timeRange = new TimeRange(from, to);
    }
    
    /**
     * Returns the time range for this element.
     * @return the time range
     */
    public TimeRange getTimeRange() {
        return timeRange;
    }
    
    /**
     * Returns the time when the first operation related to this element was performed.
     * @return the time of the first operation, or <code>-1</code> if no such operation exists
     */
    public long getTimeFrom() {
        return timeRange.getFrom();
    }
    
    /**
     * Returns the time when the last operation related to this element was performed.
     * @return the time of the last operation, or <code>-1</code> if no such operation exists
     */
    public long getTimeTo() {
        return timeRange.getTo();
    }
    
    /**
     * Returns the key for an element, which combines the name and creation time of the element with the symbol <code>"@"</code>. 
     * @param name the name of the element
     * @param time the time at the first operation
     * @param time the time at the last operation
     * @return the key string for a map that stores the element
     */
    static String getKey(String name, long from, long to) {
        return name + "@" + String.valueOf(from) + "-" + String.valueOf(to);
    }
    
    /**
     * Returns the name of an element stored in a map.
     * @param key the key string for a map that stores the element
     * @return the name of the element
     */
    static String getName(String key) {
        int indexOf = key.indexOf('@');
        return key.substring(0, indexOf);
    }
     
    /**
     * Sorts the collection of elements in alphabetical order.
     * @param elements the collection of the elements to be sorted
     */
    public static void sort(List<? extends RepositoryElementInfo> elements) {
        Collections.sort(elements, new Comparator<RepositoryElementInfo>() {
            public int compare(RepositoryElementInfo e1, RepositoryElementInfo e2) {
                String name1 = e1.getName();
                String name2 = e2.getName();
                return name1.compareTo(name2);
            }
        });
    }
}
