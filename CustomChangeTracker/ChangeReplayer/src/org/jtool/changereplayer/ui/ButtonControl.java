/*
 *  Copyright 2015
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changereplayer.ui;

import org.jtool.changereplayer.Activator;
import org.jtool.changerepository.operation.UnifiedOperation;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorSite;
import java.util.List;
import java.util.ArrayList;

/**
 * Creates a button control for replaying the change history.
 * @author Katsuhisa Maruyama
 */
public class ButtonControl {
    
    /**
     * The source code view that contains this time-line control.
     */
    protected SourceCodeView sourcecodeView;
    
    /**
     * The action for going backward to the previous operation.
     */
    protected Action backwardAction;
    
    /**
     * The action for going forward to the next operation.
     */
    protected Action forwardAction;
    
    /**
     * The action for jumping to the first operation.
     */
    protected Action firstAction;
    
    /**
     * The action for jumping to the last operation.
     */
    protected Action lastAction;
    
    /**
     * The icon images.
     */
    private static ImageDescriptor forwardIcon = Activator.getImageDescriptor("icons/right1.gif");
    private static ImageDescriptor rewindIcon = Activator.getImageDescriptor("icons/left1.gif");
    private static ImageDescriptor fastForwardIcon =  Activator.getImageDescriptor("icons/right2.gif");
    private static ImageDescriptor fastRewindIcon =  Activator.getImageDescriptor("icons/left2.gif");
    
    /**
     * Creates a button control.
     * @param view source code view that contains this time-line control
     */
    public ButtonControl(SourceCodeView view) {
        this.sourcecodeView = view;
    }
    
    /**
     * Makes actions on the tool bar.
     * @param site the primary interface between the editor and the workbench
     */
    public void makeToolBarActions(IEditorSite site) {
        List<Action> actions = new ArrayList<Action>();
        createAction(site, actions);
        
        IToolBarManager manager = site.getActionBars().getToolBarManager();
        for (Action action : actions) {
            manager.add(action);
        }
    }
    
    /**
     * Registers actions on the tool bar.
     * @param site the primary interface between the editor and the workbench
     * @param actions the collection of actions that will be registered
     */
    protected void createAction(IEditorSite site, List<Action> actions) {
        backwardAction = new Action("Backward") {
            public void run() {
                if (sourcecodeView.getFileInfo() != null) {
                    if (sourcecodeView.getCurrentOperationIndex() > 0) {
                        sourcecodeView.goTo(sourcecodeView.getCurrentOperationIndex() - 1);
                    }
                }
            }
        };
        backwardAction.setToolTipText("Go backward for one change operation");
        backwardAction.setImageDescriptor(rewindIcon);
        backwardAction.setEnabled(true);
        actions.add(backwardAction);
        
        forwardAction = new Action("Forward") {
            public void run() {
                if (sourcecodeView.getFileInfo() != null) {
                    if (sourcecodeView.getCurrentOperationIndex() < sourcecodeView.getFileInfo().getOperations().size() - 1) {
                        sourcecodeView.goTo(sourcecodeView.getCurrentOperationIndex() + 1);
                    }
                }
            }
        };
        forwardAction.setToolTipText("Go forward for one change operation");
        forwardAction.setImageDescriptor(forwardIcon);
        forwardAction.setEnabled(true);
        actions.add(forwardAction);
        
        firstAction = new Action("First") {
            public void run() {
                if (sourcecodeView.getFileInfo() != null) {
                    sourcecodeView.goTo(0);
                }
            }
        };
        firstAction.setToolTipText("Go to the first change operation");
        firstAction.setImageDescriptor(fastRewindIcon);
        firstAction.setEnabled(true);
        actions.add(0, firstAction);
        
        lastAction = new Action("Last") {
            public void run() {
                if (sourcecodeView.getFileInfo() != null) {
                    sourcecodeView.goTo(sourcecodeView.getFileInfo().getOperations().size() - 1);
                }
            }
        };
        lastAction.setToolTipText("Go to the last change operation");
        lastAction.setImageDescriptor(fastForwardIcon);
        lastAction.setEnabled(true);
        actions.add(3, lastAction);
    }
    
    /**
     * Updates the states of the buttons.z
     * @param idx the sequence number of the operation
     * @param ops the collection of the operations
     */
    public void updateButtonStates(int idx, List<UnifiedOperation> ops) {
        if (idx == 0) {
            backwardAction.setEnabled(false);
            firstAction.setEnabled(false);
        } else {
            backwardAction.setEnabled(true);
            firstAction.setEnabled(true);
        }
        
        if (idx == ops.size() - 1) {
            forwardAction.setEnabled(false);
            lastAction.setEnabled(false);
        } else {
            forwardAction.setEnabled(true);
            lastAction.setEnabled(true);
        }
    }
}
