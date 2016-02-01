/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changeslicereplayer.ui;

import org.jtool.changeslicereplayer.slicer.BackwardSliceActionOn;
import org.jtool.changeslicereplayer.slicer.BackwardSliceActionOnAll;
import org.jtool.changeslicereplayer.slicer.ForwardSliceAction;
import org.jtool.changeslicereplayer.slicer.BackwardSliceAction;
import org.jtool.changeslicereplayer.slicer.CodeSnippet;
import org.jtool.changeslicereplayer.slicer.RandomSliceOnFileAction;
import org.jtool.changeslicereplayer.slicer.RandomSliceOnProjectAction;
import org.jtool.changerepository.data.FileInfo;
import org.jtool.changerepository.operation.UnifiedOperation;
import org.jtool.changerepository.parser.OpJavaElement;
import org.jtool.changerepository.parser.OpJavaParser;
import org.jtool.changerepository.parser.OpJavaVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.swt.widgets.Menu;
import java.util.List;
import java.util.ArrayList;

/**
 * Manages a context menu on the source code view.
 * @author Katsuhisa Maruyama
 */
public class SliceContextMenu {
    
    /**
     * The source code view.
     */
    private SliceSourceCodeView sourcecodeView;
    
    /**
     * The source code control which this context menu is shown.
     */
    private SliceSourceCodeControl sourcecodeControl;
    
    /**
     * Creates an instance that manages the context menu for slicing.
     * @param contorol the source code control on which the context menu is shown
     */
    public SliceContextMenu(SliceSourceCodeControl control) {
        this.sourcecodeControl = control;
        this.sourcecodeView = control.getSliceSourceCodeView();
    }
    
    /**
     * Creates the context menu for slicing.
     */
    public void create() {
        MenuManager menuManager = new MenuManager();
        menuManager.addMenuListener(new SourceCodeMenuListener());
        
        Menu menu = menuManager.createContextMenu(sourcecodeControl.getControl());
        sourcecodeControl.getControl().setMenu(menu); 
    }
    
    /**
     * Receives an event related to the context menu. 
     */
    private class SourceCodeMenuListener implements IMenuListener {
        
        /**
         * Receives the event when the menu is about to be shown by the given menu manager.
         * @param manager the menu manager
         */
        @Override
        public void menuAboutToShow(IMenuManager manager) {
            manager.setRemoveAllWhenShown(true);
            
            CodeSnippet snippet = getCodeSnippet();
            if (snippet != null) {
                String title = "On selected code " + snippet.getRangeString();
                manager.add(new LabelAction(title));
                manager.add(new BackwardSliceAction(sourcecodeView, snippet));
                manager.add(new ForwardSliceAction(sourcecodeView, snippet));
                
            } else {
                List<OpJavaElement> elems = new ArrayList<OpJavaElement>();
                StringBuilder text = new StringBuilder();
                int idx = collectJavaElementsInFile(elems, text);
                if (idx >= 0) {
                    MenuManager smanager = new MenuManager("Operation slicing on " + text);
                    manager.add(smanager);
                    smanager.removeAll();
                    
                    for (OpJavaElement elem : elems) {
                        smanager.add(new BackwardSliceActionOn(sourcecodeView, idx, elem));
                    }
                    
                    experimentalMenuAboutToShow(smanager, idx, elems);
                }
            }
        }
        
        /**
         * Shows experimental menu items.
         * @param manager the menu manager
         * @param idx the sequence number of the snapshot of the source code
         * @param elems the collection of Java elements appearing in the snapshot of the source code
         */
        private void experimentalMenuAboutToShow(MenuManager manager, int idx, List<OpJavaElement> elems) {
            manager.add(new LabelAction(""));
            if (elems.size() > 0) {
                manager.add(new BackwardSliceActionOnAll(sourcecodeView, idx, elems));
            }
            manager.add(new RandomSliceOnFileAction(sourcecodeView));
            manager.add(new RandomSliceOnProjectAction(sourcecodeView));
        }
        
        /**
         * Obtains a code snippet.
         * @return the code snippet, or <code>null</code> if the snippet was improperly selected.
         */
        private CodeSnippet getCodeSnippet() {
            int idx = sourcecodeView.getCurrentOperationIndex();
            FileInfo finfo = sourcecodeView.getFileInfo();
            List<UnifiedOperation> ops = finfo.getOperations();
            UnifiedOperation op = ops.get(idx);
            
            TextSelection selection = sourcecodeView.getSelection();
            if (selection != null && selection.getLength() > 0) {
                
                int start = selection.getOffset();
                int end = start + selection.getLength() - 1;
                String code = finfo.getCode(idx);
                CodeSnippet snippet = new CodeSnippet(start, end, idx, op.getTime(), code.substring(start, end));
                return snippet;
            }
            
            return null;
        }
        
        /**
         * Collects Java elements within source code.
         * @param elems the collection that stores the Java elements
         * @param text the string that stores the text for the menu item 
         * @return the sequence number of the snapshot, or <code>-1</code> if no parse-able snapshot was found
         */
        private int collectJavaElementsInFile(List<OpJavaElement> elems, StringBuilder text) {
            int idx = sourcecodeView.getCurrentOperationIndex();
            FileInfo finfo = sourcecodeView.getFileInfo();
            List<UnifiedOperation> ops = finfo.getOperations();
            boolean collection = collectJavaElementsInFile(idx, elems);
            
            if (collection && elems.size() != 0) {
                text.append(String.valueOf(idx + 1));
                return idx;
            }
            
            for (int i = idx - 1; i >= 0; i--) {
                collection = collectJavaElementsInFile(i, elems);
                if (collection && elems.size() != 0) {
                    text.append(String.valueOf(i + 1) + " [" + String.valueOf(idx - i) +" back]");
                    return i;
                }
            }
            
            for (int i = idx + 1; i < ops.size(); i++) {
                collection = collectJavaElementsInFile(i, elems);
                if (collection && elems.size() != 0) {
                    text.append(String.valueOf(i + 1) + " [" + String.valueOf(i - idx) +" ahead]");
                    return i;
                }
            }
            
            return -1;
        }
        
        /**
         * Collects Java elements within source code when an operation with a given sequence number was performed.
         * @param idx the sequence number of the specified operation
         * @param elems the collection that stores the Java elements
         * @return <code>true</code> if the source code is parse-able, otherwise <code>false</code>
         */
        private boolean collectJavaElementsInFile(int idx, List<OpJavaElement> elems) {
            FileInfo finfo = sourcecodeView.getFileInfo();
            String code = finfo.getCode(idx);
            OpJavaParser parser = new OpJavaParser();
            boolean parseable = parser.parse(code);
            
            if (parseable) {
                CompilationUnit cu = parser.getCompilationUnit();
                OpJavaVisitor visitor = new OpJavaVisitor(finfo);
                cu.accept(visitor);
                
                elems.addAll(visitor.getJavaElements());
                return true;
            }
            return false;
        }
    }
}
