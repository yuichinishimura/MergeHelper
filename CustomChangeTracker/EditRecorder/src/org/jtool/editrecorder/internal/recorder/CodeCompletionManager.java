/*
 *  Copyright 2015
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.editrecorder.internal.recorder;

import org.jtool.editrecorder.macro.TriggerMacro;
import org.eclipse.jface.text.contentassist.ContentAssistEvent;
import org.eclipse.jface.text.contentassist.ICompletionListener;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * Manages code completion events (quick assist or content assist).
 * @author Katsuhisa Maruyama
 */
public class CodeCompletionManager implements ICompletionListener {
    
    /**
     * A recorder that records macros.
     */
    private DocMacroRecorder recorder;
    
    /**
     * Creates an object that records code completion events.
     * @param recorder a recorder that records macros
     */
    public CodeCompletionManager(DocMacroRecorder recorder) {
        this.recorder = recorder;
    }
    
    /**
     * Receives an event when code assist is invoked.
     * @param event the content assist event
     */
    @Override
    public void assistSessionStarted(ContentAssistEvent event) {
        if (event.assistant == null) {
            return;
        }
        
        long time = Time.getCurrentTime();
        String path = recorder.getPath();
        String commandId = event.assistant.getClass().getCanonicalName();
        
        TriggerMacro trigger = new TriggerMacro(time, commandId, path, TriggerMacro.Kind.BEGIN);
        recorder.recordTriggerMacro(trigger);
    }
    
    /**
     * Receives an event when a code assist session ends.
     * @param event the content assist event
     */
    @Override
    public void assistSessionEnded(ContentAssistEvent event) {
        if (event.assistant == null) {
            return;
        }
        long time = Time.getCurrentTime();
        String path = recorder.getPath();
        String commandId = event.assistant.getClass().getCanonicalName();
        
        TriggerMacro trigger = new TriggerMacro(time, commandId, path, TriggerMacro.Kind.BEGIN);
        recorder.recordTriggerMacro(trigger);
    }
    
    /**
     * Receives information when the selection in the proposal pop-up is changed or if the insert-mode changed.
     * @param proposal the newly selected proposal, possibly <code>null</code>
     * @param smartToggle <code>true</code> if the insert-mode toggle is being pressed, otherwise <code>false</code>
     */
    public void selectionChanged(ICompletionProposal proposal, boolean smartToggle) {
    }
}
