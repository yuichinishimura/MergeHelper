/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changeslicereplayer.ui;

import org.jtool.changeslicereplayer.slicer.Slice;
import org.jtool.changereplayer.ui.HistoryView;
import org.jtool.changereplayer.ui.SourceCodeView;
import org.jtool.changerepository.dependencygraph.OpDepGraphNode;
import org.jtool.changerepository.operation.UnifiedOperation;
import org.jtool.changerepository.operation.OperationManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorSite;
import java.util.List;

/**
 * Creates a source code viewer for slice replay.
 * @author Katsuhisa Maruyama
 */
public class SliceSourceCodeView extends SourceCodeView {
    
    /**
     * The slice to be replayed.
     */
    private Slice slice;
    
    /**
     * Creates an empty instance.
     */
    public SliceSourceCodeView() {
        super();
    }
    
    /**
     * Creates a button control that manages button actions.
     * @param site the primary interface between the editor and the workbench
     */
    public void createBottonActions(IEditorSite site) {
        buttonControl = new SliceButtonControl(this);
        buttonControl.makeToolBarActions(site);
    }
    
    /**
     * Creates a source code view control.
     * @param parent the parent widget of the source code view
     * @return the created instance
     */
    public Composite createSourceCodeViewControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        FormLayout layout = new FormLayout();
        composite.setLayout(layout);
        
        timelineControl = new SliceTimelineControl(this);
        timelineControl.createPartControl(composite);
        
        sourcecodeControl = new SliceSourceCodeControl(this);
        sourcecodeControl.createPartControl(composite, timelineControl.getControl());
        
        return composite;
    }
    
    /**
     * Sets the slice to be replayed.
     * @param slice the slice
     */
    public void setSlice(Slice slice) {
        this.slice = slice;
        
        setOperationTable(slice);
    }
    
    /**
     * Returns the slice to be replayed.
     * @return the slice
     */
    public Slice getSlice() {
        return slice;
    }
    
    /**
     * Sets the color of items in the operation table.
     * @param slice the slice to be displayed
     */
    private void setOperationTable(Slice slice) {
        HistoryView hview = getHistoryView();
        Table table = hview.getOperationTable();
        
        if (table.getItems().length == 0) {
            return;
        }
        
        for (UnifiedOperation op : fileInfo.getOperations()) {
            int idx = op.getId() - 1;
            TableItem item = table.getItem(idx);
            
            if (slice == null || slice.contain(op)) {
                item.setForeground(SliceSourceCodeControl.sliceColor);
                
            } else {
                item.setForeground(SliceSourceCodeControl.nonSliceColor);
            }
        }
        
        if (slice != null) {
            OpDepGraphNode node = slice.getFirstNode();
            UnifiedOperation op = node.getOperation();
            List<UnifiedOperation> ops = fileInfo.getOperations();
            int idx = OperationManager.getOperation(ops, op.getId());
            goTo(idx);
            
        } else {
            goTo(getCurrentOperationIndex());
        }
    }
}
