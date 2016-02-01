/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changerepository.data;

import org.jtool.changerecorder.util.Time;

/**
 * Represents the time range.
 * @author Katsuhisa Maruyama
 */
public class TimeRange {
    
    /**
     * The start point of this time range.
     */
    private long from;
    
    /**
     * The end point of this time range.
     */
    private long to;
    
    /**
     * Creates an instance that represents the time range.
     * @param from the start point of the time range
     * @param to the end point of the time range
     */
    public TimeRange(long from, long to) {
        if (from <= to) {
            this.from = from;
            this.to = to;
            
        } else {
            this.from = to;
            this.to = from;
        }
    }
    
    /**
     * Returns the start point of this time range
     * @return the start point 
     */
    public long getFrom() {
        return from;
    }
    
    /**
     * Returns the end point of this time range.
     * @return the end point
     */
    public long getTo() {
        return to;
    }
    
    /**
     * Returns the time period between the start and end points.
     * @return the time period for this time range
     */
    public long getPeriod() {
        return getTo() - getFrom();
    }
    
    /**
     * Tests if the specified time is between this time range.
     * @param time the specified time
     * @return <code>true</code> if the time is between this time range, otherwise <code>false</code>
     */
    public boolean isInside(long time) {
        return from <= time && time <= to;
    }
    
    /**
     * Returns the information for debugging.
     * @return the string of the debug information
     */
    public String toString() {
        return "from:" + Time.toUsefulFormat(from) + " to:" + Time.toUsefulFormat(to);
    }
}
