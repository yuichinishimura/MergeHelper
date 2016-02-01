/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changereplayer.ui;

import org.jtool.changerepository.data.TimeRange;
import org.jtool.changerepository.operation.UnifiedOperation;

/**
 * Represents the time period during which the file is live.
 * @author Katsuhisa Maruyama
 */
public class FileLiveRange extends TimeRange {
    
    /**
     * The operation performed at the beginning of the time period during which the file is live.
     */
    private UnifiedOperation first;
    
    /**
     * The operation performed at the end of the time period during which the file is live.
     */
    private UnifiedOperation last;
    
    /**
     * The x-point of the first operation.
     */
    private int firstX;
    
    /**
     * The x-point of the last operation.
     */
    private int lastX;
    
    /**
     * Creates an instance that represents time period during which the file is live.
     * @param first the first operation in the the time period
     * @param last the last operation in the time period
     */
    FileLiveRange(UnifiedOperation first, UnifiedOperation last) {
        super(first.getTime(), last.getTime());
        this.first = first;
        this.last = last;
    }
    
    /**
     * Sets the x-point of the first operation.
     * @param x the start x-point
     */
    void setFirstX(int x) {
        this.firstX = x;
    }
    
    /**
     * Returns the x-point of the first operation.
     * @return x the start x-point
     */
    int getFirstX() {
        return firstX;
    }
    
    /**
     * Sets the x-point of the last operation.
     * @param x the end x-point
     */
    void setLastX(int x) {
        this.lastX = x;
    }
    
    /**
     * Returns the x-point of the last operation.
     * @return x the end x-point
     */
    int getLastX() {
        return lastX;
    }
    
    /**
     * Returns the difference between the start and end points.
     * @return the difference
     */
    int getDiffX() {
        return lastX - firstX;
    }
    
    /**
     * Returns the first operation in the time period during which the file is live.
     * @return the first operation
     */
    UnifiedOperation getFirstOperation() {
        return first;
    }
    
    /**
     * Returns the last operation in the time period during which the file is live.
     * @return the last operation
     */
    UnifiedOperation getLastOperation() {
        return last;
    }
}
