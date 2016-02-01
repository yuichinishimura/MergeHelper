/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changeslicereplayer.ui;

import org.jtool.changeslicereplayer.Activator;
import org.jtool.changeslicereplayer.slicer.Slice;
import org.jtool.changereplayer.ui.ButtonControl;
import org.jtool.changerepository.operation.UnifiedOperation;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorSite;
import java.util.List;

/**
 * Creates a button control for replaying the change history.
 * @author Katsuhisa Maruyama
 */
public class SliceButtonControl extends ButtonControl {
    
    /**
     * The action for jumping to the previous slice operation.
     */
    protected Action prevAction;
    
    /**
     * The action for jumping to the next slice operation.
     */
    protected Action nextAction;
    
    /**
     * The action for refreshing the results of slicing.
     */
    protected Action refreshAction;
    
    /**
     * The icon images.
     */
    private static ImageDescriptor nextSliceIcon =  Activator.getImageDescriptor("icons/right3.gif");
    private static ImageDescriptor prevSliceIcon =  Activator.getImageDescriptor("icons/left3.gif");
    private static ImageDescriptor refreshIcon = Activator.getImageDescriptor("icons/nav_refresh.gif");
    
    /**
     * Creates a button control.
     * @param view source code view that contains this time-line control
     */
    public SliceButtonControl(SliceSourceCodeView view) {
        super(view);
    }
    
    /**
     * Registers actions on the tool bar.
     * @param site the primary interface between the editor and the workbench
     * @param actions the collection of actions that will be registered
     */
    @Override
    public void createAction(IEditorSite site, List<Action> actions) {
        super.createAction(site, actions);
        
        prevAction = new Action("Prev") {
            public void run() {
                if (sourcecodeView.getFileInfo() != null) {
                    List<UnifiedOperation> ops = sourcecodeView.getFileInfo().getOperations();
                    
                    int idx = getPreviousSliceOperation(ops, sourcecodeView.getCurrentOperationIndex());
                    sourcecodeView.goTo(idx);
                }
            }
        };
        prevAction.setToolTipText("Go to the previous slice operation");
        prevAction.setImageDescriptor(prevSliceIcon);
        prevAction.setEnabled(false);
        actions.add(1, prevAction);
        
        nextAction = new Action("Next") {
            public void run() {
                if (sourcecodeView.getFileInfo() != null) {
                    List<UnifiedOperation> ops = sourcecodeView.getFileInfo().getOperations();
                    
                    int idx = getNextSliceOperation(ops, sourcecodeView.getCurrentOperationIndex());
                    sourcecodeView.goTo(idx);
                }
            }
        };
        nextAction.setToolTipText("Go to the next slice operation");
        nextAction.setImageDescriptor(nextSliceIcon);
        nextAction.setEnabled(false);
        actions.add(4, nextAction);
        
        refreshAction = new Action("Refresh") {
            public void run() {
                getSliceSourceCodeView().setSlice(null);
                refreshAction.setEnabled(false);
            }
        };
        refreshAction.setToolTipText("Refresh the results of slicing");
        refreshAction.setImageDescriptor(refreshIcon);
        refreshAction.setEnabled(false);
        actions.add(6, refreshAction);
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
     * Retrieves the previous slice operation and returns its index number.
     * @param ops the collection of operations
     * @param cur the index number of the current operation
     * @return the index number of the found operation, or <code>-1</code> if none
     */
    private int getPreviousSliceOperation(List<UnifiedOperation> ops, int cur) {
        for (int idx = cur - 1; 0 <= idx; idx--) {
            UnifiedOperation op = ops.get(idx);
            if (getSlice() != null && getSlice().contain(op)) {
                return idx;
            }
        }
        return -1;
    }
    
    /**
     * Retrieves the next slice operation and returns its index number.
     * @param ops the collection of slice operations
     * @param cur the index number of the current operation
     * @return the sequence number of the found operation, or <code>-1</code> if none
     */
    private int getNextSliceOperation(List<UnifiedOperation> ops, int cur) {
        for (int idx = cur + 1; idx < ops.size(); idx++) {
            UnifiedOperation op = ops.get(idx);
            if (getSlice() != null && getSlice().contain(op)) {
                return idx;
            }
        }
        return -1;
    }
    
    /**
     * Updates the states of the buttons.
     * @param idx the sequence number of the operation
     * @param ops the collection of the operations
     */
    @Override
    public void updateButtonStates(int idx, List<UnifiedOperation> ops) {
        super.updateButtonStates(idx, ops);
        
        int prev = getPreviousSliceOperation(ops, sourcecodeView.getCurrentOperationIndex());
        if (prev != -1) {
            prevAction.setEnabled(true);
        } else {
            prevAction.setEnabled(false);
        }
        
        int next = getNextSliceOperation(ops, sourcecodeView.getCurrentOperationIndex());
        if (next != -1) {
            nextAction.setEnabled(true);
        } else {
            nextAction.setEnabled(false);
        }
        
        if (getSlice() != null) {
            refreshAction.setEnabled(true);
        }
    }
}
