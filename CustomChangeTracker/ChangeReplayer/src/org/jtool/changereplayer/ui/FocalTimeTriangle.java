/*
 *  Copyright 2015
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changereplayer.ui;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;

/**
 * Defines a triangle representing the specified time.
 * @author Takayuki Omori
 * @author Katsuhisa Maruyama
 */
public class FocalTimeTriangle {
    
    /**
     * Draws this focal time triangle mark.
     * @param gc the SWT drawing capabilities
     * @param device the drawable device
     * @param timeLineBar the time line bar for displaying this focal time triangle mark
     * @param mx the value that indicates the move distance by scrolling
     */
    public void draw(GC gc, Device device, TimelineBar timeLineBar, int mx) {
        int center = timeLineBar.time2point(timeLineBar.getFocalTime()) - mx;
        int left = center - 3;
        int right = center + 3;
        int top = (timeLineBar.getTop() * 3 + timeLineBar.getBottom() * 7) / 10;
        int bottom = timeLineBar.getBottom();
        int[] pointArray = new int[] { left, bottom, center, top, right, bottom, left, bottom };
        
        Color RED = new Color(device, 255, 0, 0);
        gc.setForeground(RED);
        gc.setBackground(RED);
        gc.fillPolygon(pointArray);
        gc.drawPolygon(pointArray);
    }
}
