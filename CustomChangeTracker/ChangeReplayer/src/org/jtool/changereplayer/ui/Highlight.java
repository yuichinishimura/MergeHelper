/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changereplayer.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.swt.graphics.RGB;

/**
 * Manages information on any highlight.
 * @author Takayuki Omori
 * @author Katsuhisa Maruyama
 */
public class Highlight {
    
    /**
     * The foreground color of this highlight.
     */
    protected RGB foregroundColor;
    
    /**
     * The background color of this highlight.
     */
    protected RGB backgroundColor;
    
    /**
     * The priority when drawing multiple highlights.
     */
    private int priority;
    
    /**
     * Creates an instance that stores information on the highlight with dot representation.
     * @param fcolor the foreground color of this highlight
     * @param bcolor the background color of this highlight
     * @param priority the priority when drawing multiple highlights
     */
    public Highlight(RGB fcolor, RGB bcolor, int priority) {
        this.foregroundColor = fcolor;
        this.backgroundColor = bcolor;
        this.priority = priority;
    }
    
    /**
     * Returns the color of this highlight.
     * @return the color information
     */
    public RGB getForegroubdColor() {
        return foregroundColor;
    }
    
    /**
     * Returns the background color of this highlight.
     * @return the color information
     */
    public RGB getBackgroubdColor() {
        return backgroundColor;
    }
    
    /**
     * Returns the priority value for this highlight.
     * @return the priority value
     */
    public int getPriority() {
        return priority;
    }
    
    /**
     * Sorts the highlights based on respective priorities.
     */
    public static void sortByPriority(List<Highlight> highlights) {
        Collections.sort(highlights, new Comparator<Highlight>() {
            public int compare(Highlight h1, Highlight h2) {
                return h1.getPriority() - h2.getPriority();
            }
        });
    }
}
