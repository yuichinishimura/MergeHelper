/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changeslicereplayer.ui;

import org.jtool.changeslicereplayer.slicer.Slice;
import org.jtool.changereplayer.ui.SourceCodeControl;
import org.jtool.changerepository.dependencygraph.OpDepGraph;
import org.jtool.changerepository.dependencygraph.OpDepGraphInfo;
import org.jtool.changerepository.operation.UnifiedOperation;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import java.util.List;
import java.util.ArrayList;

/**
 * Creates a source code control for replay.
 * @author Katsuhisa Maruyama
 */
public class SliceSourceCodeControl extends SourceCodeControl {
    
    /**
     * The color for items included the slice.
     */
    static final Color sliceColor = BLACK;
    
    /**
     * The color for items not included the slice
     */
    static final Color nonSliceColor = GRAY;
    
    /**
     * Creates an instance for a source code control.
     * @param view the source code view that contains this source code control
     */
    public SliceSourceCodeControl(SliceSourceCodeView view) {
        super(view);
    }
    
    /**
     * Creates a control for this source code control.
     * @param parent the parent control
     * @param top the top control
     */
    @Override
    public void createPartControl(Composite parent, Control top) {
        super.createPartControl(parent, top);
        
        System.out.println("AA");
        
        SliceContextMenu contextMenu = new SliceContextMenu(this);
        contextMenu.create();
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
     * Decorates the representation of the source code displayed on this source code control.
     * @param code the content of the source code
     */
    @Override
    protected void decorateCode(String code) {
        StyledText styledText = getStyledText();
        styledText.setStyleRange(null);
        reset();
        
        OpDepGraph graph = OpDepGraphInfo.createGraph(sourcecodeView.getProjectInfo());
        if (graph == null) {
            StyleRange srange = new StyleRange(0, code.length(), GRAY, WHITE);
            styledText.setStyleRange(srange);
            
        } else {
            int idx = sourcecodeView.getCurrentOperationIndex();
            
            List<Boolean> codeStatus = getCodeStatus(idx);
            if (code.length() != codeStatus.size()) {
                return;
            }
            
            for (int i = 0; i < code.length(); ) {
                int j = 0;
                if (!codeStatus.get(i)) {
                    int start = i;
                    j = i + 1;
                    while (j < code.length() && !codeStatus.get(j)) {
                        j++;
                    }
                    
                    int len = j - start;
                    j = j - i;
                    
                    StyleRange srange = new StyleRange(start, len, nonSliceColor, WHITE);
                    styledText.setStyleRange(srange);
                }
                i = i + j + 1;
            }
            
            List<StyleRange> ranges = getColoredStyleRanges(code);
            for (StyleRange range : ranges) {
                if (range.background == RED || range.background == YELLOW || range.background == BLUE) {
                    if (codeStatus.get(range.start)) {
                        StyleRange srange = new StyleRange(range.start, range.length, sliceColor, range.background);
                        styledText.setStyleRange(srange);
                    } else {
                        StyleRange srange = new StyleRange(range.start, range.length, nonSliceColor, range.background);
                        styledText.setStyleRange(srange);
                    }
                }
            }
            
            reveal(sourcecodeView.getFileInfo().getOperations().get(idx).getStart(), code);
        }
        
        styledText.redraw();
        styledText.update();
    }
    
    /**
     * Obtains the code status indicating which characters of its content are visible.
     * @param idx the sequence number of the current operation that was replayed
     * @return the code status indicating which characters of its content are visible
     */
    private List<Boolean> getCodeStatus(int idx) {
        List<UnifiedOperation> ops = sourcecodeView.getFileInfo().getOperations();
        
        List<Boolean> codeStatus = new ArrayList<Boolean>();
        for (int i = 0; i <= idx; i++) {
            UnifiedOperation op = ops.get(i);
            
            if (op.isNormalOperation()) {
                if (getSlice() != null) {
                    changeCodeStatus(codeStatus, op, getSlice().contain(op));
                } else {
                    changeCodeStatus(codeStatus, op, true);
                }
                
            } else if (op.isFileOpenOperation() && codeStatus.size() == 0) {
                String code = sourcecodeView.getFileInfo().getCode(i);
                for (int j = 0; j < code.length(); j++) {
                    codeStatus.add(j, false);
                }
            }
        }
        
        return codeStatus;
    }
    
    /**
     * Changes code status based on the application of the specified text operation.
     * @param codeStatus the code status
     * @param op the operation to be applied
     * @param in <code>true</code> if the operation is contained in the slice, otherwise <code>false</code>
     */
    private void changeCodeStatus(List<Boolean> codeStatus, UnifiedOperation op, boolean in) {
        int start = op.getStart();
        
        int delLength = op.getDeletedText().length();
        if (delLength > 0) {
            for (int i = start; i < start + delLength; i++) {
                codeStatus.remove(start);
            }
        }
        
        int insLength = op.getInsertedText().length();
        if (insLength > 0) {
            for (int i = start; i < start + insLength; i++) {
                codeStatus.add(i, in); 
            }
        }
    }
}
