/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.editrecorder.internal.recorder;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ContentAssistantFacade;
import org.eclipse.jface.text.quickassist.IQuickAssistAssistant;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.text.undo.DocumentUndoManagerRegistry;
import org.eclipse.text.undo.IDocumentUndoManager;
import org.eclipse.ui.IEditorPart;
import org.jtool.editrecorder.internal.diff.DiffMacroGenerator;
import org.jtool.editrecorder.macro.CancelMacro;
import org.jtool.editrecorder.macro.CompoundMacro;
import org.jtool.editrecorder.macro.DiffMacro;
import org.jtool.editrecorder.macro.DocumentMacro;
import org.jtool.editrecorder.macro.ExecutionMacro;
import org.jtool.editrecorder.macro.Macro;
import org.jtool.editrecorder.macro.ResourceMacro;
import org.jtool.editrecorder.macro.TriggerMacro;
import org.jtool.editrecorder.recorder.Recorder;
import org.jtool.editrecorder.util.EditorUtilities;
import java.util.ArrayList;
import java.util.List;

/**
 * Records document macros.
 * @author Katsuhisa Maruyama
 */
public class DocMacroRecorder {
    
    /**
     * A manager that manages document events.
     */
    protected DocumentManager documentManager;
    
    /**
     * A manager that manages code completion events.
     */
    protected CodeCompletionManager completionManager;
    
    /**
     * The collection of raw macros that were recorded.
     */
    protected List<Macro> rawMacros;
    
    /**
     * The path of a file whose contents are changed by macros.
     */
    protected String path;
    
    /**
     * A recorder factory that sends macro events.
     */
    protected Recorder recorder;
    
    /**
     * A compound macro that contains macros.
     */
    protected CompoundMacro compoundMacro;
    
    /**
     * The last raw macro.
     */
    protected Macro lastRawMacro;
    
    /**
     * The last document macro stored for macro compression.
     */
    protected DocumentMacro lastDocumentMacro;
    
    /**
     * The contents of the previous code.
     */
    protected String preCode;
    
    /**
     * Creates an object that records document macros related to a file.
     * @param path the of the file
     * @param recorder a recorder that sends macro events
     */
    protected DocMacroRecorder(String path, Recorder recorder) {
        this.path = path;
        this.recorder = recorder;
        
        this.documentManager = new DocumentManager(this);
        this.completionManager = new CodeCompletionManager(this);
        this.rawMacros = new ArrayList<Macro>();
    }
    
    /**
     * Starts the recording of document macros.
     */
    public void start() {
        rawMacros.clear();
        
        compoundMacro = null;
        lastRawMacro = null;
        lastDocumentMacro = null;
    }
    
    /**
     * Stops the recording of macros.
     */
    public void stop() {
        dumpLastDocumentMacro();
        needDiff();
        
        rawMacros.clear();
    }
    
    /**
     * Registers a document manager with an editor.
     * @param doc the document to be managed
     * @param st the styled text of the editor
     * @param dm the document manager
     */
    protected void register(IDocument doc, StyledText st, DocumentManager dm) {
        if (doc != null) {
            doc.addDocumentListener(dm);
            
            DocumentUndoManagerRegistry.connect(doc);
            IDocumentUndoManager undoManager = DocumentUndoManagerRegistry.getDocumentUndoManager(doc);
            if (undoManager != null) {
                undoManager.addDocumentUndoListener(dm);
            }
        }
        
        if (st != null) {
            st.addListener(SWT.KeyDown, dm);
            st.addListener(SWT.KeyUp, dm);
            st.addListener(SWT.MouseDown, dm);
            st.addListener(SWT.MouseUp, dm);
            st.addListener(SWT.MouseDoubleClick, dm);
        }
    }
    
    /**
     * Unregisters a document manager with an editor.
     * @param doc the document to be managed
     * @param st the styled text of the editor
     * @param dm the document manager
     */
    protected void unregister(IDocument doc, StyledText st, DocumentManager dm) {
        if (doc != null) {
            doc.removeDocumentListener(dm);
            
            IDocumentUndoManager undoManager = DocumentUndoManagerRegistry.getDocumentUndoManager(doc);
            DocumentUndoManagerRegistry.disconnect(doc);
            if (undoManager != null) {
                undoManager.removeDocumentUndoListener(dm);
            }
        }
        
        if (st != null) {
            st.removeListener(SWT.KeyDown, dm);
            st.removeListener(SWT.KeyUp, dm);
            st.removeListener(SWT.MouseDown, dm);
            st.removeListener(SWT.MouseUp, dm);
            st.removeListener(SWT.MouseDoubleClick, dm);
        }
    }
    
    /**
     * Registers a code completion execution manager with the editor.
     * @param editor the editor
     * @param rm the code completion execution manager
     */
    protected void register(IEditorPart editor, CodeCompletionManager cm) {
        IQuickAssistAssistant assistant = EditorUtilities.getQuickAssistAssistant(editor);
        if (assistant != null) {
            assistant.addCompletionListener(cm);
        }
        
        ContentAssistantFacade facade = EditorUtilities.getContentAssistantFacade(editor);
        if (facade != null) {
            facade.addCompletionListener(cm);
        }
    }
    
    /**
     * Unregisters a code completion manager with the editor.
     * @param editor the editor
     * @param rm the code completion execution manager
     */
    protected void unregister(IEditorPart editor, CodeCompletionManager cm) {
        IQuickAssistAssistant assistant = EditorUtilities.getQuickAssistAssistant(editor);
        if (assistant != null) {
            assistant.removeCompletionListener(cm);
        }
        
        ContentAssistantFacade facade = EditorUtilities.getContentAssistantFacade(editor);
        if (facade != null) {
            facade.removeCompletionListener(cm);
        }
    }
    
    /**
     * Returns the path of a file whose contents are changed by macros.
     * @return the file path
     */
    public String getPath() {
        return path;
    }
    
    /**
     * Sets a parent macro.
     * @param parent the parent macro, or <code>null</code> if no parent exists
     */
    void setParentMacro(Macro parent) {
        MenuMacroRecorder.getInstance().setParentMacro(parent);
    }
    
    /**
     * Returns the parent macro.
     * @return the parent macro
     */
    Macro getParentMacro() {
        return MenuMacroRecorder.getInstance().getParentMacro();
    }
    
    /**
     * Records a document macro and its compressed macro.
     * @param macro the document macro
     */
    protected void recordDocumentMacro(DocumentMacro macro) {
        recordRawMacro(macro);
        dumpMacros(macro);
    }
    
    /**
     * Records a command execution macro.
     * @param macro the command execution macro
     */
    protected void recordExecutionMacro(ExecutionMacro macro) {
        recordRawMacro(macro);
        dumpMacros(macro);
    }
    
    /**
     * Records a trigger macro.
     * @param macro the trigger macro
     */
    protected void recordTriggerMacro(TriggerMacro macro) {
        recordRawMacro(macro);
        dumpMacros(macro);
    }
    
    /**
     * Records a resource change macro.
     * @param macro the resource change macro
     */
    protected void recordResourceMacro(ResourceMacro macro) {
        recordRawMacro(macro);
        dumpMacros(macro);
        
        if (macro.isRemoved()) {
            Recorder.removeDocRecorder(macro.getPath());
        }
    }
    
    /**
     * Records a compressed macro into the operation history and its original one in .
     * @param macro a document macro
     */
    protected void recordUndoRedoMacro(DocumentMacro macro) {
        recordRawMacro(macro);
        dumpMacros(macro);
    }
    
    /**
     * Records a raw macro.
     * @param macro the raw macro to be recored
     */
    protected void recordRawMacro(Macro macro) {
        rawMacros.add(macro);
        recorder.notifyRawMacro(macro);
        
        lastRawMacro = macro;
    }
    
    /**
     * Dumps the last macro.
     */
    public void dumpLastDocumentMacro() {
        if (lastDocumentMacro != null) {
            
            if (!hasMismatch(preCode, lastDocumentMacro)) {
                // System.out.println("LAST MACRO = " + lastDocumentMacro.toString());
                recordMacro(lastDocumentMacro);
                applyMacro(lastDocumentMacro);
                
                lastDocumentMacro = null;
            }
        }
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
     * Dumps both the last macro and the latest one.
     * @param macro the latest macro
     */
    protected void dumpMacros(Macro macro) {
        dumpLastDocumentMacro();
        
        if (!hasMismatch(preCode, macro)) {
            // System.out.println("MACRO = " + macro.toString());
            recordMacro(macro);
            applyMacro(macro);
        }
    }
    
    /**
     * Records a macro.
     * @param macro the macro to be recorded
     */
    protected void recordMacro(Macro macro) {
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
                if (macro instanceof CancelMacro) {
                    CancelMacro cmacro = (CancelMacro)macro;
                    boolean suc = compoundMacro.cancelMacro(cmacro);
                    if (!suc) {
                        System.err.println("Cancellation failed: undo in refactoring");
                    }
                } else {
                    compoundMacro.addMacro(macro);
                }
                
            } else {
                macro.setRawMacros(new ArrayList<Macro>(rawMacros));
                rawMacros.clear();
                
                recorder.notifyMacro(macro);
            }
        }
    }
    
    /**
     * Obtains the current contents of a file under recording.
     * @return the contents of source code, or <code>null</code> if source code does not exist
     */
    protected String getCurrentCode() {
        return null;
    }
    
    /**
     * Confirms the necessity of generation of difference macros.
     * @return <code>true</code> the difference macros are generated, otherwise <code>false</code>
     */
    protected boolean needDiff() {
        String code = getCurrentCode();
        if (code != null && code.compareTo(preCode) != 0) {
            long time = Time.getCurrentTime();
            List<DiffMacro> macros = DiffMacroGenerator.generate(time, path, preCode, code);
            
            if (macros.size() > 0) {
                recordDiffMacros(time, macros);
                
                preCode = code;
                return true;
            }
        }
        
        preCode = code;
        return false;
    }
    
    /**
     * Records difference macros.
     * @param time the time when differences were generated
     * @param macros the collection of difference macros
     */
    protected void recordDiffMacros(long time, List<DiffMacro> macros) {
        dumpLastDocumentMacro();
        
        TriggerMacro trigger = new TriggerMacro(time, "Diff", path, TriggerMacro.Kind.BEGIN);
        recordRawMacro(trigger);
        recordMacro(trigger);
        
        for (DiffMacro macro : macros) {
            // System.out.println("** " + macro.toString());
            recordRawMacro(macro);
            recordMacro(macro);
        }
        
        trigger = new TriggerMacro(time, "Diff", path, TriggerMacro.Kind.END);
        recordRawMacro(trigger);
        recordMacro(trigger);
    }
    
    /**
     * Applies a specified normal operation into given code.
     * @param macro the macro to be applied
     */
    protected boolean applyMacro(Macro macro) {
        if (macro instanceof DocumentMacro) {
            DocumentMacro dmacro = (DocumentMacro)macro;
            StringBuilder postCode = new StringBuilder(preCode);
            
            int start = dmacro.getStart();
            int end = start + dmacro.getDeletedText().length();
            String itext = dmacro.getInsertedText();
            postCode.replace(start, end, itext);
            preCode = postCode.toString();
        }
        return true;
    }
    
    /**
     * Tests if the deletion derives any mismatch.
     * @param code the code before the application
     * @param macro the macro to be applied
     * @return <code>true</code> if a mismatch exists, otherwise <code>false</code>
     */
    private boolean hasMismatch(String code, Macro macro) {
        if (!(macro instanceof DocumentMacro)) {
            return false;
        }
        
        DocumentMacro dmacro = (DocumentMacro)macro;
        int start = dmacro.getStart();
        if (start > code.length()) {
            return true;
        }
        
        String dtext = dmacro.getDeletedText();
        int end = start + dtext.length();
        if (dtext.length() > 0) {
            String rtext = code.substring(start, end);
            if (rtext != null && rtext.compareTo(dtext) != 0) {
                
                for (int i = 0; i < rtext.length(); i++) {
                    if (rtext.charAt(i) == dtext.charAt(i)) {
                        System.out.println(((int)rtext.charAt(i)) + " == " + ((int)dtext.charAt(i)));
                    } else {
                        System.out.println(((int)rtext.charAt(i)) + " != " + ((int)dtext.charAt(i)));
                    }
                }
                return true;
            }
        }
        return false;
    }
}
