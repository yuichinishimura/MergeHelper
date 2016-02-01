/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changeslicereplayer.slicer;

/**
 * Measures the runnnig time
 * @author Katsuhisa Maruyama
 *
 */
public class RunningTime {
    
    /**
     * The time when a process starts.
     */
    private static long start;
    
    /**
     * Invoked when a process starts.
     */
    public static void start() {
        start = System.currentTimeMillis();
        System.out.println("-Starting");
    }
    
    /**
     * Invoked when a process stops.
     */
    public static void stop() {
        long stop = System.currentTimeMillis();
        System.out.println("-Running time = " + (stop - start) + " ms");
    }
}
