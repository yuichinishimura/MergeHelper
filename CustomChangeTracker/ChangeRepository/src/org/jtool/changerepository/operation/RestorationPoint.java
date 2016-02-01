/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changerepository.operation;

import org.jtool.changerecorder.util.Time;

/**
 * Manages the restoration point for source code affected by the operation histories.
 * @author Katsuhisa Maruyama
 */
class RestorationPoint {
    
    /**
     * The sequence number indicating the operation at this restoration point.
     */
    private int idx;
    
    /**
     * The time at this restoration point.
     */
    private long time;
    
    /**
     * The source code restored at this restoration point.
     */
    private String code;
    
    /**
     * Creates an instance for storing information on restoration point.
     * @param idx the sequence number indicating the operation at this restoration point
     * @param time the time at this restoration point
     * @param code the source code restored at this restoration point
     */
    RestorationPoint(int idx, long time, String code) {
        this.idx = idx;
        this.time = time;
        this.code = code;
    }
    
    /**
     * Returns the sequence number indicating the operation at this restoration point.
     * @return the sequence number
     */
    int getIndex() {
        return idx;
    }
    
    /**
     * Returns the time at this restoration point.
     * @return the time
     */
    long getTime() {
        return time;
    }
    
    /**
     * Returns the source code restored at this restoration point.
     * @return the restored source code
     */
    String getCode() {
        return code;
    }
    
    /**
     * Returns information for debugging.
     * @return the string for debug information. 
     */
    public String toString() {
        return "time:" + Time.toUsefulFormat(time) + " idx:" + idx;
    }
}
