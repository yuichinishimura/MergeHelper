/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.editrecorder.internal.recorder;

import org.jtool.editrecorder.macro.ExecutionMacro;
import org.jtool.editrecorder.macro.TriggerMacro;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;

/**
 * Manages command events (menu etc.).
 * @author Katsuhisa Maruyama
 */
public class CommandExecutionManager implements IExecutionListener {
    
    /**
     * A recorder that records menu actions.
     */
    private MenuMacroRecorder recorder;
    
    /**
     * Creates an object that records command execution events.
     * @param recorder a recorder that records menu actions
     */
    public CommandExecutionManager(MenuMacroRecorder recorder) {
        this.recorder = recorder;
    }
    
    /**
     * Receives a command that is about to execute.
     * @param commandId the identifier of the command that is about to execute
     * @param event the event that will be passed to the <code>execute</code> method
     */
    @Override
    public void preExecute(String commandId, ExecutionEvent event) {
        long time = Time.getCurrentTime();
        String path = recorder.getActiveInputFilePath();
        
        ExecutionMacro macro = new ExecutionMacro(time, "Exec", path, commandId);
        recorder.recordExecutionMacro(macro);
        
        try {
            String id = event.getCommand().getCategory().getId();
            if (id.endsWith("category.refactoring")) {
                recorder.setParentMacro(macro);
                
                TriggerMacro trigger = new TriggerMacro(time, "Refactoring", path, TriggerMacro.Kind.BEGIN);
                recorder.recordTriggerMacro(trigger);
            }
        } catch (NotDefinedException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Receives a command that has failed to complete execution.
     * @param commandId the identifier of the command that has executed
     * @param returnValue the return value from the command; may be <code>null</code>.
     */
    @Override
    public void postExecuteSuccess(String commandId, Object returnValue) {
    }
    
    /**
     * Receives a command with no handler.
     * @param commandId the identifier of command that is not handled
     * @param exception the exception that occurred
     */
    @Override
    public void notHandled(String commandId, NotHandledException exception) {
    }
    
    /**
     * Receives a command that has completed execution successfully.
     * @param commandId the identifier of the command that has executed
     * @param returnValue the return value from the command
     */
    @Override
    public void postExecuteFailure(String commandId, ExecutionException exception) {
    }
}
