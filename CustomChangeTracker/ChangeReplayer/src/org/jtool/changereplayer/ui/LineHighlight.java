/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changereplayer.ui;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;

/**
 * A highlight that draws a line.
 * @author Takayuki Omori
 * @author Katsuhisa Maruyama
 */
public class LineHighlight extends Highlight {
    
    /**
     * The time related to the drawn line.
     */
    private long time;
    
    /**
     * The width of the drawn line.
     */
    private int width;
    
    /**
     * Creates the default highlight that draws a line.
     * @param time the time related to the line
     * @param priority the priority when drawing multiple highlights
     */
    public LineHighlight(long time, int priority) {
        this(time, new RGB(139, 105, 20), new RGB(0x0, 0x0, 0x0), 1, priority);
    }
    
    /**
     * Creates a new highlight that draws a line.
     * @param time the time related to the line
     * @param fcolor the foreground color of the line
     * @param bcolor the background color of the line
     * @param width the width of the line
     * @param priority the priority when drawing multiple highlights
     */
    public LineHighlight(long time, RGB fcolor, RGB bcolor, int width, int priority) {
        super(fcolor, bcolor, priority);
        this.time = time;
        this.width = width;
    }
    
    /**
     * Returns the time for the line.
     * @return the time
     */
    public long getTime() {
        return time;
    }
    
    /**
     * Returns the width of the line.
     * @return the line width
     */
    public int getWidth() {
        return width;
    }
    
    /**
     * Draws a line on the highlight view.
     * @param gc the SWT drawing capabilities
     * @param device the drawable device
     * @param x the x-position of the line
     * @param top the top of the y-position of the line
     * @param bottom the bottom of the y-position of the line
     */
    public void draw(GC gc, Device device, int x, int top, int bottom) {
        gc.setForeground(new Color(device, foregroundColor));
        gc.setBackground(new Color(device, backgroundColor));
        for (int i = 0; i < width; i++) {
            gc.drawLine(x + i - width / 2, top, x + i - width / 2, bottom);
        }
    }
}
