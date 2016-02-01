/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.editrecorder.internal.recorder;

import org.jtool.editrecorder.Activator;
import org.jtool.editrecorder.macro.CompoundMacro;
import org.jtool.editrecorder.macro.ExecutionMacro;
import org.jtool.editrecorder.macro.Macro;
import org.jtool.editrecorder.macro.ResourceMacro;
import org.jtool.editrecorder.macro.TriggerMacro;
import org.jtool.editrecorder.recorder.Recorder;
import org.jtool.editrecorder.util.EditorUtilities;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.history.IRefactoringHistoryService;
import java.util.ArrayList;
import java.util.List;

/**
 * Records macros performed on the editor.
 * @author Katsuhisa Maruyama
 */
public class MenuMacroRecorder {
    
    /**
     * A recorder that sends all kinds of macro events.
     */
    private Recorder recorder;
    
    /**
     * The collection of raw macros that were recorded.
     */
    private List<Macro> rawMacros = new ArrayList<Macro>();
    
    /**
     * A compound macro that contains macros.
     */
    private CompoundMacro compoundMacro;
    
    /**
     * A manger that manages command execution events.
     */
    private CommandExecutionManager commandManager;
    
    /**
     * A manger that manages refactoring execution events.
     */
    private RefactoringExecutionManager refactoringManager;
    
    /**
     * A manager that records operations related to resource changes in the Java model.
     */
    private ResourceChangedManager resourceChangeManager;
    
    /**
     * A parent macro on which the current recorded macro dangles
     */
    private Macro parentMacro;
    
    /**
     * The single instance of this menu recorder
     */
    private static MenuMacroRecorder instance = new MenuMacroRecorder();
    
    /**
     * Creates an empty object.
     */
    private MenuMacroRecorder() {
        commandManager = new CommandExecutionManager(this);
        refactoringManager = new RefactoringExecutionManager(this);
        resourceChangeManager = new ResourceChangedManager(this);
    }
    
    /**
     * Sets the recorder that sends all kinds of macro events.
     * @param recorder the recorder.
     */
    public void setRecorder(Recorder recorder) {
        this.recorder = recorder;
    }
    
    /**
     * Returns the single instance of this menu recorder.
     * @return the single instance
     */
    public static MenuMacroRecorder getInstance() {
        return instance;
    }
    
    /**
     * Starts the recording of menu actions.
     */
    public void start() {
        register(commandManager);
        register(refactoringManager);
        register(resourceChangeManager);
        
        rawMacros.clear();
        compoundMacro = null;
        parentMacro = null;
    }
    
    /**
     * Stops the recording of menu actions.
     */
    public void stop() {
        unregister(commandManager);
        unregister(refactoringManager);
        unregister(resourceChangeManager);
        
        rawMacros.clear();
    }
    
    /**
     * Registers a command manager with the command service of the workbench.
     * @param cm the command manager
     */
    protected void register(CommandExecutionManager cm) {
        ICommandService cs = (ICommandService)PlatformUI.getWorkbench().getService(ICommandService.class);
        if (cs != null) {
            cs.addExecutionListener(cm);
        }
    }
    
    /**
     * Unregisters a command manager with the command service of the workbench.
     * @param cm the command manager
     */
    protected void unregister(CommandExecutionManager cm) {
        ICommandService cs = (ICommandService)PlatformUI.getWorkbench().getService(ICommandService.class);
        if (cs != null) {
            cs.removeExecutionListener(cm);
        }
    }
    
    /**
     * Registers a refactoring execution manager with the refactoring history service.
     * @param rm the refactoring execution manager
     */
    protected void register(RefactoringExecutionManager rm) {
        IRefactoringHistoryService rs = RefactoringCore.getHistoryService();
        if (rs != null) {
            rs.addExecutionListener(rm);
            // rs.addHistoryListener(rm);
        }
    }
    
    /**
     * Unregisters a refactoring execution manager with the refactoring history service.
     * @param rm the refactoring execution manager
     */
    protected void unregister(RefactoringExecutionManager rm) {
        IRefactoringHistoryService rs = RefactoringCore.getHistoryService();
        if (rs != null) {
            rs.removeExecutionListener(rm);
            // rs.removeHistoryListener(rm);
        }
    }
    
    /**
     * Registers an element change manager with the Java model.
     * @param em the element change manager
     */
    public static void register(ResourceChangedManager em) {
        JavaCore.addElementChangedListener(em);
    }
    
    /**
     * Unregisters an element change manager with the Java model.
     * @param em the element change manager
     */
    public static void unregister(ResourceChangedManager em) {
        JavaCore.removeElementChangedListener(em);
    }
    
    /**
     * Sets a parent macro related to a file.
     * @param parent the parent macro, or <code>null</code> if no parent exists
     */
    void setParentMacro(Macro parent) {
        parentMacro = parent;
    }
    
    /**
     * Returns the parent macro.
     * @return the parent macro
     */
    Macro getParentMacro() {
        return parentMacro;
    }
    
    /**
     * Records a command execution macro.
     * @param macro the command execution macro
     */
    protected void recordExecutionMacro(ExecutionMacro macro) {
        breakMacro();
        
        String path = macro.getPath();
        DocMacroRecorder docMacroRecorder = getDocMacroRecorder(path);
        if (docMacroRecorder != null) {
            docMacroRecorder.recordExecutionMacro(macro);
            
        } else {
            recordRawMacro(macro);
            recordMacro(macro);
        }
    }
    
    /**
     * Records a trigger macro.
     * @param macro the trigger macro
     */
    protected void recordTriggerMacro(TriggerMacro macro) {
        breakMacro();
        
        String path = macro.getPath();
        DocMacroRecorder docMacroRecorder = getDocMacroRecorder(path);
        if (docMacroRecorder != null) {
            docMacroRecorder.recordTriggerMacro(macro);
            
        } else {
            recordRawMacro(macro);
            recordMacro(macro);
        }
    }
    
    /**
     * Records a resource change macro.
     * @param macro the resource change macro
     */
    protected void recordResourceMacro(ResourceMacro macro) {
        breakMacro();
        
        String path = macro.getPath();
        DocMacroRecorder docMacroRecorder = getDocMacroRecorder(path);
        
        if (docMacroRecorder != null) {
            macro.setOnEdit(true);
            docMacroRecorder.recordResourceMacro(macro);
            
        } else {
            macro.setOnEdit(false);
            recordRawMacro(macro);
            recordMacro(macro);
        }
    }
    
    /**
     * Records a raw macro.
     * @param macro the raw macro to be recored
     */
    private void recordRawMacro(Macro macro) {
        rawMacros.add(macro);
        recorder.notifyRawMacro(macro);
    }
    
    /**
     * Records a macro.
     * @param macro the macro to be recorded
     */
    private void recordMacro(Macro macro) {
        if (macro instanceof TriggerMacro) {
            TriggerMacro tmacro = (TriggerMacro)macro;
            if (compoundMacro == null && tmacro.isBegin()) {
                compoundMacro = new CompoundMacro(tmacro.getStartTime(), tmacro.getType(), macro.getPath());
                
            } else if (tmacro.isEnd() || tmacro.isCursorChange()) {
                if (compoundMacro != null) {
                    compoundMacro.setRawMacros(new ArrayList<Macro>(rawMacros));
                    compoundMacro.setTimes();
                    rawMacros.clear();
                    
                    recorder.notifyMacro(compoundMacro);
                }
                compoundMacro = null;
            }
            
        } else {
            if (compoundMacro != null) {
                compoundMacro.addMacro(macro);
            } else {
                macro.setRawMacros(new ArrayList<Macro>(rawMacros));
                rawMacros.clear();
                
                recorder.notifyMacro(macro);
            }
        }
    }
    
    /**
     * Returns the a recorder that records macros related to a file
     * @param path the path of the file
     * @return the recorder
     */
    protected DocMacroRecorder getDocMacroRecorder(String path) {
        if (path == null) {
            return null;
        }
        
        return Recorder.getDocRecorder(path);
    }
    
    /**
     * Break the current macro.
     */
    public void breakMacro() {
        for (DocMacroRecorder docRecorder : Recorder.getDocRecorders()) {
            docRecorder.dumpLastDocumentMacro();
            docRecorder.needDiff();
        }
    }
    
    /**
     * Obtains the path of a active file existing on an editor.
     * @return @return the path of the file, or <code>null</code> if none
     */
    protected String getActiveInputFilePath() {
        IWorkbenchWindow window = Activator.getPlugin().getWorkbench().getActiveWorkbenchWindow();
        if (window != null) {
            ISelection selection = window.getSelectionService().getSelection();
            if (selection instanceof ITextSelection) {
                return EditorUtilities.getActiveInputFilePath();
            }
        }
        return null;
    }
}
