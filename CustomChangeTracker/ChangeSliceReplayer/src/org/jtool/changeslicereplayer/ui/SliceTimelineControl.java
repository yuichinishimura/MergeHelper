/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changeslicereplayer.ui;

import org.jtool.changeslicereplayer.slicer.Slice;
import org.jtool.changereplayer.ui.FileLiveRange;
import org.jtool.changereplayer.ui.Highlight;
import org.jtool.changereplayer.ui.LineHighlight;
import org.jtool.changereplayer.ui.RectHighlight;
import org.jtool.changereplayer.ui.TimelineControl;
import org.jtool.changerepository.data.FileInfo;
import org.jtool.changerepository.data.TimeRange;
import org.jtool.changerepository.operation.UnifiedOperation;
import org.eclipse.swt.graphics.RGB;

import java.util.List;
import java.util.ArrayList;

/**
 * Creates a time-line control for replay.
 * @author Katsuhisa Maruyama
 */
public class SliceTimelineControl extends TimelineControl {
    
    /**
     * Creates a time-line control.
     * @param view source code view that contains this time-line control
     */
    public SliceTimelineControl(SliceSourceCodeView view) {
       super(view);
    }
    
    /**
     * Returns the source code view that contains this source code control
     * @return the source code view
     */
    public SliceSourceCodeView getSliceSourceCodeView() {
        return (SliceSourceCodeView)sourcecodeView;
    }
    
    /**
     * Returns the slice to be displayed.
     * @return the slice
     */
    public Slice getSlice() {
        return getSliceSourceCodeView().getSlice();
    }
    
    /**
     * Collects highlights related to a given file.
     * @param finfo the file information
     * @param the collection of time periods during which the file is live
     */
    @Override
    protected List<Highlight> collectHighlights(FileInfo finfo, List<FileLiveRange> fileLiveRanges) {
        List<Highlight> hs = new ArrayList<Highlight>();
        
        for (UnifiedOperation op : finfo.getOperations()) {
            LineHighlight h;
            if (getSlice() == null || getSlice().contain(op)) {
                h = new LineHighlight(op.getTime(), DEFAULT_LINE_PRIORITY);
            } else {
                RGB fcolor = new RGB(0xff, 0xff, 0x00);
                RGB bcolor = new RGB(0x00, 0x00, 0x00);
                h = new LineHighlight(op.getTime(), fcolor, bcolor, 1, DEFAULT_LINE_PRIORITY);
            }
            hs.add(h);
        }
        
        for (TimeRange range : fileLiveRanges) {
            RectHighlight h = new RectHighlight(range, DEFAULT_RECT_PRIORITY);
            hs.add(h);
        }
        
        return hs;
    }
}
