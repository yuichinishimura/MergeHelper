/*
 *  Copyright 2015
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changereplayer.ui;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import java.util.List;

/**
 * Manages a time-line bar.
 * @author Takayuki Omori
 * @author Katsuhisa Maruyama
 */
public class TimelineBar {
    
    /**
     * The time-line view.
     */
    private TimelineControl timelineControl;
    
    /**
     * The collection of time periods during which the file is live.
     */
    private List<FileLiveRange> fileLiveRanges;
    
    /**
     * The collection of highlights.
     */
    private List<Highlight> highlights;
    
    /**
     * The area of this time-line bar
     */
    private Rectangle area;
    
    /**
     * The constant value that indicates the height of each time-line area.
     */
    private final int TIMELINE_HEIGHT = 20;
    
    /**
     * The percentage for the visible time range.
     */
    private int scale = 100;
    
    /**
     * Creates a time line bar.
     * @param control the time-line control
     */
    public TimelineBar(TimelineControl control) {
        timelineControl = control;
    }
    
    /**
     * Tests if this time line bar has shown source code.
     * @return <code>true</code> if the time line bar has been shown, or <code>false</code> 
     */
    public boolean hasShown() {
        return fileLiveRanges != null;
    }
    
    /**
     * Returns the focal time.
     * @return the focal time 
     */
    public long getFocalTime() {
        return timelineControl.getFocalTime();
    }
    
    /**
     * Updates the start and end times for this time-line bar on this highlight view.
     * @param the whole time range
     */
    public void setFileLiveRange(List<FileLiveRange> ranges) {
        this.fileLiveRanges = ranges;
        updateTimeRange();
    }
    
    /**
     * Sets highlights drawn on this time-line bar.
     * @param highlights the collection of the highlights
     */
    public void setHighlights(List<Highlight> highlights) {
        this.highlights = highlights;
    }
    
    /**
     * Updates the start and end times for this time-line bar on this highlight view.
     * @param the whole time range
     */
    public void updateTimeRange() {
        final int GAP_FOR_TIME = 10;
        int x = 0;
        for (FileLiveRange range : fileLiveRanges) {
            range.setFirstX(x);
            x = x + (int)(range.getPeriod() / scale);
            range.setLastX(x);
            x = x + GAP_FOR_TIME;
        }
        
        setArea();
    }
    
    /**
     * Sets the area displaying this time-line bar.
     */
    private void setArea() {
        if (fileLiveRanges.size() == 0) {
            area = new Rectangle(0, 0, 0, TIMELINE_HEIGHT);
            return;
        }
        
        FileLiveRange firstRange = fileLiveRanges.get(0);
        FileLiveRange lastRange = fileLiveRanges.get(fileLiveRanges.size() - 1);
        
        area = new Rectangle(0, 0, lastRange.getLastX() - firstRange.getFirstX(), TIMELINE_HEIGHT);
    }
    
    /**
     * Returns the area displaying this time-line bar.
     * @return the area, or the area whose size is zero if it has undefined
     */
    Rectangle getArea() {
        if (area == null) {
            return new Rectangle(0, 0, 0, 0);
        }
        return area;
    }
    
    /**
     * Sets the percentage for the time range.
     * @param scale the percentage
     */
    void zoomin() {
        if (scale > 1) {
            scale = scale / 2;
        }
        
        updateTimeRange();
    }
    
    /**
     * Sets the percentage for the time range.
     * @param scale the percentage
     */
    void zoomout() {
        if (scale < 10000) {
            scale = scale * 2;
        }
        
        updateTimeRange();
    }
    
    /**
     * Returns the scale for the visible time range.
     * @return the percentage of the scale
     */
    int getScale() {
        return scale;
    }
    
    /**
     * Sets the scale for the visible time range.
     * @param scale the percentage of the scale
     */
    void setScale(int scale) {
        this.scale = scale;
    }
    
    /**
     * Returns the top position of the area of this time-line bar.
     * @return the top position of the area
     */
    int getTop() {
        return 0;
    }
    
    /**
     * Returns the bottom position of the area of this time-line bar.
     * @return the bottom position of the area
     */
    int getBottom() {
        return TIMELINE_HEIGHT;
    }
    
    /**
     * Returns the width of the area of this time-line bar.
     * @return the width of the area
     */
    int getWidth() {
        return area.width;
    }
    
    /**
     * Returns the height of the area of this time-line bar.
     * @return the height of the area
     */
    int getHeight() {
        return TIMELINE_HEIGHT;
    }
    
    /**
     * Tests if the specified coordinate is inside this time-line bar.
     * @param x the x-position
     * @param y the y-position
     * @return <code>true</code> if the coordinate is inside this time-line bar, otherwise <code>false</code>
     */
    boolean isInside2(int x, int y) {
        return area.contains(x, y);
    }
    
    /**
     * Obtains the time from a specified x-position on this time-line bar.
     * @param x the x-position
     * @return the time corresponding to the x-position, or <code>-1</code> if the time was not found
     */
    long point2time(int x) {
        for (FileLiveRange range : fileLiveRanges) {
            if (range.getFirstX() <= x && x <= range.getLastX()) {
                double rate = (double)(x - range.getFirstX()) / (double)range.getDiffX();
                long time = (long)(range.getFrom() + (double)range.getPeriod() * rate);
                return time;
            }
        }
        return -1;
    }
    
    /**
     * Obtains the x-position on this time line bar from the time.
     * @param time the time
     * @return the value of the x position, or <code>-1</code> if the x-position was not found
     */
    public int time2point(long time) {
        for (FileLiveRange range : fileLiveRanges) {
            if (range.getFrom() <= time && time <= range.getTo()) {
                
                double rate = (double)(time - range.getFrom()) / (double)range.getPeriod();
                int x = (int)(range.getFirstX() + range.getDiffX() * rate);
                return x;
            }
        }
        return -1;
    }
    
    /**
     * Draws this time-line bar.
     * @param gc the SWT drawing capabilities
     * @param device the drawable device
     * @param mx the value that indicates the move distance by scrolling
     */
    public void draw(GC gc, Device device, int mx) {
        Color linen = new Color(null, 250, 240, 230);
        gc.setForeground(linen);
        gc.setBackground(linen);
        gc.fillGradientRectangle(area.x, area.y, area.width, area.height, true);
        
        gc.drawRectangle(area);
        if (highlights != null) {
            for (Highlight hl : highlights) {
                if (hl instanceof LineHighlight) {
                    draw(gc, device, (LineHighlight)hl, mx);
                }
                if (hl instanceof RectHighlight) {
                    draw(gc, device, (RectHighlight)hl, mx);
                }
            }
        }
    }
    
    /**
     * Draws a line highlight on this time-line bar.
     * @param gc the SWT drawing capabilities
     * @param device device the drawable device
     * @param hl the line highlight
     * @param mx the value that indicates the move distance by scrolling
     */
    private void draw(GC gc, Device device, LineHighlight hl, int mx) {
        int x = time2point(hl.getTime()) - mx;
        if (x != -1) {
            hl.draw(gc, device, x, getTop(), getBottom());
        }
    }
    
    /**
     * Draws a rectangle highlight on this time-line bar.
     * @param gc the SWT drawing capabilities
     * @param device device the drawable device
     * @param hl the rectangle highlight
     * @param mx the value that indicates the move distance by scrolling
     */
    private void draw(GC gc, Device device, RectHighlight hl, int mx) {
        int left = time2point(hl.getTimeFrom()) - mx;
        int right = time2point(hl.getTimeTo()) - mx;
        
        if (left != -1 && right != -1) {
            int width = right - left + 1;
            int height = getHeight() - 1;
            
            Rectangle rect = new Rectangle(left, getTop(), width, height + 1);
            hl.draw(gc, device, rect);
        }
    }
}
