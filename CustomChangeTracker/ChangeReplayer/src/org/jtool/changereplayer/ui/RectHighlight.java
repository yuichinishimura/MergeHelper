/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changereplayer.ui;

import org.jtool.changerepository.data.TimeRange;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * A drawer that draws a rectangle highlight.
 * @author Takayuki Omori
 * @author Katsuhisa Maruyama
 */
public class RectHighlight extends Highlight {
    
    /**
     * The time range related to the drawn rectangle.
     */
    private TimeRange range;
    
    /**
     * Creates a new highlight that draws a rectangle.
     * @param range the time range related to the drawn rectangle
     * @param priority the priority when drawing multiple highlights
     */
    public RectHighlight(TimeRange range, int priority) {
        this(range, new RGB(137, 221, 93), new RGB(198, 255, 134), 100);
    }
    
    /**
     * Creates a new highlight that draws a rectangle.
     * @param range the time range related to the drawn rectangle
     * @param fcolor the foreground color of the rectangle
     * @param bcolor the background color of the rectangle
     * @param priority the priority when drawing multiple highlights
     */
    public RectHighlight(TimeRange range, RGB fcolor, RGB bcolor, int priority) {
        super(fcolor, bcolor, priority);
        this.range = range;
    }
    
    /**
     * Returns the time range for the rectangle.
     * @return the time range 
     */
    public TimeRange getRange() {
        return range;
    }
    
    /**
     * Returns the start time for the rectangle.
     * @return the start time
     */
    public long getTimeFrom() {
        return range.getFrom();
    }
    
    /**
     * Returns the end time for the rectangle.
     * @return the end time
     */
    public long getTimeTo() {
        return range.getTo();
    }
    
    /**
     * Draws a rectangle on the highlight view.
     * @param gc the SWT drawing capabilities
     * @param device the drawable device
     * @param rect the information on the drawn rectangle
     */
    public void draw(GC gc, Device device, Rectangle rect) {
        gc.setForeground(new Color(device, foregroundColor));
        gc.setBackground(new Color(device, backgroundColor));
        
        gc.fillGradientRectangle(rect.x, rect.y, rect.width, rect.height, true);
    }
}
