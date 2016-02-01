/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changereplayer.ui;

import org.jtool.changerepository.data.FileInfo;

/**
 * Stores the state of the source code view.
 * @author Katsuhisa Maruyama
 */
public class SourceCodeViewState {
    
    /**
     * The information on the file of interest.
     */
    protected FileInfo fileInfo;
    
    /**
     * The time when the operation of interest was performed.
     */
    private long focalTime;
    
    /**
     * The sequence number of the operation of interest.
     */
    private int currentOperationIndex;
    
    /**
     * The percentage for the visible time range.
     */
    private int scale;
    
    /**
     * 
     * @param time the focal time for the file
     * @param idx the sequence number of the operation
     * @param scale the percentage of the scale
     */
    public SourceCodeViewState(long time, int idx, int scale) {
        this.focalTime = time;
        this.currentOperationIndex = idx;
        this.scale = scale;
    }
    
    /**
     * Returns the time when the operation of interest was performed on a file.
     * @return the focal time for the file
     */
    public long getFocalTime() {
        return focalTime;
    }
    
    /**
     * Returns the sequence number of the operation of interest.
     * @return the sequence number of the operation
     */
    public int getCurrentOperationIndex() {
        return currentOperationIndex;
    }
    
    /**
     * Returns the scale for the visible time range.
     * @return the percentage of the scale
     */
    public int getScale() {
        return scale;
    }
}
