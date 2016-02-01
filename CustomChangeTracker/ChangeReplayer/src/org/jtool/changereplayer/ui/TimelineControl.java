/*
 *  Copyright 2015
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changereplayer.ui;

import org.jtool.changerepository.data.FileInfo;
import org.jtool.changerepository.data.TimeRange;
import org.jtool.changerepository.operation.UnifiedOperation;
import org.jtool.changerecorder.util.Time;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import java.util.List;
import java.util.ArrayList;

/**
 * Creates a time-line control for replay.
 * @author Takayuki Omori
 * @author Katsuhisa Maruyama
 */
public class TimelineControl {
    
    /**
     * The source code view that contains this time-line control.
     */
    protected SourceCodeView sourcecodeView;
    
    /**
     * The collection of highlights.
     */
    protected List<Highlight> highlights;
    
    /**
     * The canvas on which the time-line bar displays.
     */
    protected Canvas canvas;
    
    /**
     * The time-line bar displaying the whole operations.
     */
    protected TimelineBar timelineBar;
    
    /**
     * The scroll-bar of the canvas.
     */
    protected ScrollBar scrollBar;
    
    /**
     * The value that indicates the move distance by scrolling.
     */
    protected int moveX;
    
    /**
     * The triangle that indicates the focal time.
     */
    protected FocalTimeTriangle focalTimeTriangle;
    
    /**
     * The listener that receives an event related to the paint event.
     */
    private TimeLinePaintListener timeLinePaintListener;
    
    /**
     * The listener that receives an event related to the mouse click event.
     */
    protected TimeLineMouseClickListener timeLineMouseClickListener;
    
    /**
     * The listener that receives an event related to the mouse move event.
     */
    protected TimeLineMouseMoveListener timeLineMouseMoveListener;
    
    /**
     * The listener that receives an event related to the mouse wheel event.
     */
    protected TimeLineMouseWheelListener timeLineMouseWheelListener;
    
    /**
     * The listener that receives an event related to the key event.
     */
    protected KeyListener timeLineKeyListener;
    
    /**
     * The listener that receives an event related to the traverse.
     */
    protected TimeLineTraverseListener timeLineTraverseListener;
    
    /**
     * The listener that receives an event related to the selection events.
     */
    protected TimeLineSelectionListener timeLineSelectionListener;
    
    /**
     * The default value of the priority of line highlights.
     */
    protected final static int DEFAULT_LINE_PRIORITY = 200;
    
    /**
     * The default value of the priority of rectangle highlights.
     */
    protected final static int DEFAULT_RECT_PRIORITY = 100;
    
    /**
     * Creates a time-line control.
     * @param view source code view that contains this time-line control
     */
    public TimelineControl(SourceCodeView view) {
       this.sourcecodeView = view;
    }
    
    /**
     * Creates a control for this time-line control.
     * @param parent the parent control
     */
    public void createPartControl(Composite parent) {
        timelineBar = new TimelineBar(this);
        
        canvas = new Canvas(parent, SWT.DOUBLE_BUFFERED | SWT.H_SCROLL);
        
        FormData data = new FormData();
        data.top = new FormAttachment(0, 0);
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(100, 0);
        data.height = timelineBar.getHeight();
        canvas.setLayoutData(data);
        
        scrollBar = canvas.getHorizontalBar();
        scrollBar.setVisible(true);
        scrollBar.setMinimum(0);
        scrollBar.setEnabled(true);
        moveX = 0;
        
        focalTimeTriangle = new FocalTimeTriangle();
        
        timeLinePaintListener = createTimeLinePaintListener();
        canvas.addPaintListener(timeLinePaintListener);
        
        timeLineMouseClickListener = createTimeLineMouseClickListener();
        canvas.addMouseListener(timeLineMouseClickListener);
        
        timeLineMouseMoveListener = createTimeLineMouseMoveListener();
        canvas.addMouseMoveListener(timeLineMouseMoveListener);
        
        timeLineMouseWheelListener = createTimeLineMouseWheelListener();
        canvas.addListener(SWT.MouseWheel, timeLineMouseWheelListener);
        
        timeLineKeyListener = createTimeLineKeyListener();
        canvas.addKeyListener(timeLineKeyListener);
        
        timeLineTraverseListener = createTimeLineTraverseListener();
        canvas.addTraverseListener(timeLineTraverseListener);
        
        timeLineSelectionListener = createTimeLineSelectionListener();
        scrollBar.addSelectionListener(timeLineSelectionListener);
    }
    
    /**
     * Sets the time line bar.
     */
    public void setTimelineBar() {
        List<FileLiveRange> fileLiveRanges = collectFileLiveRanges(sourcecodeView.getFileInfo());
        
        highlights = collectHighlights(sourcecodeView.getFileInfo(), fileLiveRanges);
        Highlight.sortByPriority(highlights);
        
        timelineBar.setFileLiveRange(fileLiveRanges);
        timelineBar.setHighlights(highlights);
    }
    
    /**
     * Creates the listener that receives an event related to the paint and returns it.
     * @return the listener for the paint event
     */
    protected TimeLinePaintListener createTimeLinePaintListener() {
        return new TimeLinePaintListener();
    }
    
    /**
     * Creates the listener that receives an event related to the mouse click and returns it.
     * @return the listener for the move click event
     */
    protected TimeLineMouseClickListener createTimeLineMouseClickListener() {
        return new TimeLineMouseClickListener();
    }
    
    /**
     * Creates the listener that receives an event related to the mouse move and returns it.
     * @return the listener for the mouse move event
     */
    protected TimeLineMouseMoveListener createTimeLineMouseMoveListener() {
        return new TimeLineMouseMoveListener();
    }
    
    /**
     * Creates the listener that receives an event related to the mouse wheel move and returns it.
     * @return the listener for the mouse wheel move event
     */
    protected TimeLineMouseWheelListener createTimeLineMouseWheelListener() {
        return new TimeLineMouseWheelListener();
    }
    
    /**
     * Creates the listener that receives an event related to the key event and returns it.
     * @return the listener for the key event
     */
    protected TimeLineKeyListener createTimeLineKeyListener() {
        return new TimeLineKeyListener();
    }
    
    /**
     * Creates the listener that receives an event related to the traverse move and returns it.
     * @return the listener for the traverse event
     */
    protected TimeLineTraverseListener createTimeLineTraverseListener() {
        return new TimeLineTraverseListener();
    }
    
    /**
     * Creates the listener that receives an event related to the selection events.
     * @return the listener for the selection event
     */
    protected TimeLineSelectionListener createTimeLineSelectionListener() {
        return new TimeLineSelectionListener();
    }
    
    /**
     * Returns the control widget of this time-line control.
     * @return the canvas widget
     */
    public Control getControl() {
        return canvas;
    }
    
    /**
     * Sets the focus to this time-line control.
     */
    public void setFocus() {
        canvas.setFocus();
    }
    
    /**
     * Disposes of this time-line control.
     */
    public void dispose() {
        if (!canvas.isDisposed()) {
            canvas.removePaintListener(timeLinePaintListener);
            canvas.removeMouseListener(timeLineMouseClickListener);
            canvas.removeMouseMoveListener(timeLineMouseMoveListener);
            canvas.removeListener(SWT.MouseWheel, timeLineMouseWheelListener);
            canvas.removeKeyListener(timeLineKeyListener);
            canvas.removeTraverseListener(timeLineTraverseListener);
        }
        
        if (!scrollBar.isDisposed()) {
            scrollBar.removeSelectionListener(timeLineSelectionListener);
        }
    }
    
    /**
     * Updates this time-line control.
     */
    public void update() {
        canvas.redraw();
        canvas.update();
    }
    
    /**
     * Returns the focal time.
     * @return the focal time 
     */
    protected long getFocalTime() {
        return sourcecodeView.getFocalTime();
    }
    
    /**
     * Returns the scale for the visible time range.
     * @return the percentage of the scale
     */
    protected int getScale() {
        return timelineBar.getScale();
    }
    
    /**
     * Sets the scale for the visible time range.
     * @param scale the percentage of the scale
     */
    protected void setScale(int scale) {
        timelineBar.setScale(scale);
        timelineBar.updateTimeRange();
    }
    
    /**
     * Collects the collection of time periods during a given file is live.
     * @param finfo the file information
     */
    protected List<FileLiveRange> collectFileLiveRanges(FileInfo finfo) {
        List<UnifiedOperation> ops = finfo.getOperations();
        
        List<FileLiveRange> ranges = new ArrayList<FileLiveRange>();
        UnifiedOperation from = null;
        UnifiedOperation to = null;
        
        for (int i = 0; i < ops.size(); i++) {
            UnifiedOperation op = ops.get(i);
            
            if (op.isFileOpenOperation() || op.isFileNewOperation()) {
                if (to == null) {
                    from = op;
                    to = from;
                }
                
            } else if (op.isFileCloseOperation() || op.isFileDeleteOperation()) {
                if (from != null) {
                    to = op;
                    
                    FileLiveRange range = new FileLiveRange(from, to);
                    ranges.add(range);
                    
                    from = to;
                    to = null;
                }
            }
        }
        
        if (from != null && to != null && from.getTime() == to.getTime()) {
            to = ops.get(ops.size() - 1);
            
            FileLiveRange range = new FileLiveRange(from, to);
            ranges.add(range);
        }
        
        return ranges;
    }
    
    /**
     * Collects highlights related to a given file.
     * @param finfo the file information
     * @param the collection of time periods during which the file is live
     */
    protected List<Highlight> collectHighlights(FileInfo finfo, List<FileLiveRange> fileLiveRanges) {
        List<Highlight> hs = new ArrayList<Highlight>();
        
        for (UnifiedOperation op : finfo.getOperations()) {
            LineHighlight h = new LineHighlight(op.getTime(), DEFAULT_LINE_PRIORITY);
            hs.add(h);
        }
        
        for (TimeRange range : fileLiveRanges) {
            RectHighlight h = new RectHighlight(range, DEFAULT_RECT_PRIORITY);
            hs.add(h);
        }
        
        return hs;
    }
    
    /**
     * Converts the specified point on the time-line bar to the focal time.
     * @param x the x-position of the specified point
     * @param y the y-position of the specified point
     * @return the value indicating the focal time, or <code>-1</code> if the point is out of the time line bar
     */
    protected long point2time(int x, int y) {
        if (timelineBar.getTop() < y && y < timelineBar.getBottom()) {
            return timelineBar.point2time(x + moveX);
        }
        return -1;
    }
    
    /**
     * Deals with the events that are generated when the control needs to be painted. 
     */
    private class TimeLinePaintListener implements PaintListener {
        
        /**
         * Creates an empty object.
         */
        private TimeLinePaintListener() {
        }
        
        /**
         * Receives the paint event when a paint event occurs for the control.
         * @param evt the event containing information about the paint
         */
        @Override
        public void paintControl(PaintEvent evt) {
            if (!timelineBar.hasShown()) {
                return;
            }
            
            GC gc = evt.gc;
            Display display = canvas.getDisplay();
            
            gc.setBackground(new Color(display, 255, 255, 255));
            gc.fillRectangle(canvas.getBounds());
            
            timelineBar.draw(gc, display, moveX);
            focalTimeTriangle.draw(gc, display, timelineBar, moveX);
            
            scrollBar.setMaximum(timelineBar.getWidth() + 1);
            scrollBar.setThumb(Math.min (timelineBar.getWidth(), canvas.getBounds().width));
        }
    }
    
    /**
     * Deals with the events that are generated as mouse buttons are pressed.
     */
    private class TimeLineMouseClickListener implements MouseListener {
        
        /**
         * Creates an empty object.
         */
        private TimeLineMouseClickListener() {
        }
        
        /**
         * Receives the mouse event when a mouse button is pressed twice within the double click period.
         * @param evt the event containing information about the mouse double click
         */
        @Override
        public void mouseDoubleClick(MouseEvent evt) {
            if (!timelineBar.hasShown()) {
                return;
            }
            
            timelineBar.updateTimeRange();
            
            long time = point2time(evt.x, evt.y);
            sourcecodeView.findFocalTime(time);
            
            update();
        }
        
        /**
         * Receives the mouse event when a mouse button is pressed.
         * @param evt the event containing information about the mouse button press
         */
        @Override
        public void mouseDown(MouseEvent evt) {
        }
        
        /**
         * Receives the mouse event when a mouse button is released.
         * @param evt the event containing information about the mouse button release
         */
        @Override
        public void mouseUp(MouseEvent e) {
        }
    }
    
    /**
     * Deals with the events that are generated as the mouse pointer moves.
     */
    private class TimeLineMouseMoveListener implements MouseMoveListener {
        
        /**
         * Creates an empty object.
         */
        TimeLineMouseMoveListener() {
        }
        
        /**
         * Receives the mouse event when the mouse moves.
         * @param evt an event containing information about the mouse move
         */
        @Override
        public void mouseMove(MouseEvent evt) {
            if (!timelineBar.hasShown()) {
                return;
            }
            
            long time = point2time(evt.x, evt.y);
            if (time == -1) {
                return;
            }
            
            StringBuilder buf = new StringBuilder();
            UnifiedOperation op = getOperation(evt.x, evt.y);
            if (op != null) {
                buf.append(op.getIOperation().toString());
            } else {
                buf.append(Time.toUsefulFormat(time));
            }
            
            canvas.setToolTipText(buf.toString());
        }
        
        /**
         * Obtains the operation corresponding to a given point.
         * @param x the x-position of the position
         * @param y the y-position of the position
         * @return the found operation, or <code>null</code> if no operation was found
         */
        private UnifiedOperation getOperation(int x, int y) {
            long from = point2time(x - 1, y);
            long to = point2time(x + 1, y);
            if (from != -1 && to != -1) {
                List<UnifiedOperation> operations = sourcecodeView.getFileInfo().getOperations();
                for (UnifiedOperation op : operations) {
                    if (from < op.getTime() && op.getTime() < to) {
                        return op;
                    }
                }
            }
            return null;
        }
    }
    
    /**
     * Deals with the events that are generated as the mouse wheel moves.
     */
    private class TimeLineMouseWheelListener implements Listener {
        
        /**
         * Creates an empty object.
         */
        private TimeLineMouseWheelListener() {
        }
        
        /**
         * Receives the event when the mouse wheel moves.
         * @param evt an event containing information about the mouse wheel move
         */
        @Override
        public void handleEvent(Event evt) {
            if (!timelineBar.hasShown()) {
                return;
            }
            
            if (sourcecodeView.getFileInfo() == null) {
                return;
            }
            
            if (evt.count > 0) {
                timelineBar.zoomin();
                
            } else {
                timelineBar.zoomout();
                
                if (moveX > canvas.getBounds().width) {
                    moveX = canvas.getBounds().width - scrollBar.getThumbBounds().width;
                } else {
                    moveX = 0;
                }
            }
            
            update();
        }
    }
    
    /**
     * Deals with the events when the key is performed.
     */
    private class TimeLineKeyListener implements KeyListener {
        
        /**
         * Creates an empty object.
         */
        private TimeLineKeyListener() {
        }
        
        /**
         * Receives the key event when a key is pressed in the control.
         * @param evt the event containing information about the key press
         */
        @Override
        public void keyPressed(KeyEvent evt) {
        }
        
        /**
         * Receives the key event when a key is pressed in the control.
         * @param evt the event containing information about the key press
         */
        @Override
        public void keyReleased(KeyEvent evt) {
            if (!timelineBar.hasShown()) {
                return;
            }
            
            if (evt.keyCode == SWT.ARROW_UP) {
                sourcecodeView.goTo(sourcecodeView.getCurrentOperationIndex() - 1);
                
            } else if (evt.keyCode == SWT.ARROW_DOWN) {
                sourcecodeView.goTo(sourcecodeView.getCurrentOperationIndex() + 1);
                
            } else if (evt.keyCode == SWT.ARROW_LEFT) {
                sourcecodeView.goTo(sourcecodeView.getCurrentOperationIndex() - 1);
                
            } else if (evt.keyCode == SWT.ARROW_RIGHT) {
                sourcecodeView.goTo(sourcecodeView.getCurrentOperationIndex() + 1);
            }
            
            update();
        }
    }
    
    /**
     * Deals with the events when the key is performed.
     */
    private class TimeLineTraverseListener implements TraverseListener {
        
        /**
         * Creates a listener that deals with the key events.
         */
        TimeLineTraverseListener() {
        }
        
        /**
         * Receives the traverse event when a traverse key (typically a tab or arrow key) is pressed in the control.
         * @param evt the event containing information about the traverse
         */
        @Override
        public void keyTraversed(TraverseEvent evt) {
            if (!timelineBar.hasShown()) {
                return;
            }
            
            if (evt.detail == SWT.TRAVERSE_ARROW_PREVIOUS || evt.detail == SWT.TRAVERSE_ARROW_NEXT) {
                evt.detail = SWT.TRAVERSE_NONE;
                evt.doit = true;
            }
        }
    }
    
    /**
     * Deals with the events when the selection occurs.
     */
    private class TimeLineSelectionListener implements SelectionListener {
        
        /**
         * Creates an empty object.
         */
        TimeLineSelectionListener() {
        }
        
        /**
         * Receives the event when the default selection occurs in the control
         * @param evt the event containing information about the default selection
         */
        @Override
        public void widgetDefaultSelected(SelectionEvent evt) {
        }
        
        /**
         * Receives the event when the selection occurs in the control
         * @param evt the event containing information about the selection
         */
        @Override
        public void widgetSelected(SelectionEvent evt) {
            if (!timelineBar.hasShown()) {
                return;
            }
            
            int twidth = timelineBar.getWidth();
            int cwidth = canvas.getBounds().width;
            if (twidth > cwidth) {
                moveX = scrollBar.getSelection();
                update();
                
            } else {
                moveX = 0;
            }
        }
    }
}
