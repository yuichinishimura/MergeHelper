/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.editrecorder.internal.recorder;

import org.jtool.editrecorder.macro.Macro;
import org.jtool.editrecorder.macro.TriggerMacro;
import org.eclipse.ltk.core.refactoring.history.IRefactoringExecutionListener;
import org.eclipse.ltk.core.refactoring.history.RefactoringExecutionEvent;
import org.eclipse.ltk.core.refactoring.history.IRefactoringHistoryListener;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistoryEvent;

/**
 * Manages refactoring events.
 * @author Katsuhisa Maruyama
 */
public class RefactoringExecutionManager implements IRefactoringExecutionListener, IRefactoringHistoryListener {
    
    /**
     * A recorder that records menu actions.
     */
    private MenuMacroRecorder recorder;
    
    /**
     * Creates an object that records refectoring execution events.
     * @param recorder a recorder that records menu actions
     */
    public RefactoringExecutionManager(MenuMacroRecorder recorder) {
        this.recorder = recorder;
    }
    
    /**
     * Receives an event when a refactoring execution event happened.
     * @param event the refactoring execution event
     */
    @Override
    public void executionNotification(RefactoringExecutionEvent event) {
        long time = Time.getCurrentTime();
        Macro macro = recorder.getParentMacro();
        String path = null;
        if (macro != null) {
            path = macro.getPath();
        }
        
        String commandId = event.getDescriptor().getDescription();
        if (event.getEventType() == RefactoringExecutionEvent.ABOUT_TO_PERFORM) {
            TriggerMacro trigger = new TriggerMacro(time, commandId, path, TriggerMacro.Kind.BEGIN);
            recorder.recordTriggerMacro(trigger);
            
        } else if (event.getEventType() == RefactoringExecutionEvent.PERFORMED) {
            TriggerMacro trigger = new TriggerMacro(time, commandId, path, TriggerMacro.Kind.END);
            path = null;
            recorder.recordTriggerMacro(trigger);
            
            recorder.setParentMacro(null);
        }
    }
    
    /**
     * Receives an event when a refactoring history event happened.
     * @param event the refactoring history event
     */
    @Override
    public void historyNotification(RefactoringHistoryEvent event) {
    }
}
