/*
 *  Copyright 2015
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changereplayer.ui;

import org.jtool.changereplayer.Activator;
import org.jtool.changerepository.data.FileInfo;
import org.jtool.changerepository.event.RepositoryChangedEvent;
import org.jtool.changerepository.event.RepositoryChangedListener;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPersistableElement;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages an editor displaying source code to be replayed.
 * @author Katsuhisa Maruyama
 */
public class ChangeHistoryEditor extends EditorPart implements RepositoryChangedListener {
    
    /**
     * The identification string that is used to register the editor.
     */
    public static final String ID = "ChangeReplayer.editor.ChangeHistoryEditor";
    
    /**
     * The editor displaying source code to be replayed.
     */
    private static ChangeHistoryEditor cheditor = null;
    
    /**
     * The source code view currently active.
     */
    protected SourceCodeView activeView = null;
    
    /**
     * The information on the file of interest.
     */
    protected FileInfo fileInfo;
    
    /**
     * The storage that memorizing the state of the source code view.
     */
    protected Map<String, SourceCodeViewState> viewStates = new HashMap<String, SourceCodeViewState>();
    
    /**
     * Creates an editor that manages source code views.
     */
    public ChangeHistoryEditor() {
        super();
    }
    
    /**
     * Opens an editor on the workbench.
     * @return the opened editor 
     */
    public static ChangeHistoryEditor open() {
        if (cheditor != null) {
            return cheditor;
        }
        
        try {
            IWorkbenchPage page = Activator.getWorkbenchPage();
            IEditorPart editor = IDE.openEditor(page, new EmptyEditorInput(), ChangeHistoryEditor.ID, true);
            if (editor instanceof ChangeHistoryEditor) {
                cheditor = (ChangeHistoryEditor)editor;
            }
        } catch (PartInitException e) {
            e.printStackTrace();
        }
        
        return cheditor;
    }
    
    /**
     * Initializes the editor part with a site and input.
     * @param site the editor site
     * @param input the editor input
     * @exception PartInitException if this editor was not initialized successfully
     */
    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        setSite(site);
        setInput(input);
        
        activeView = SourceCodeViewLoader.create();
        activeView.createBottonActions(site);
    }
    
    /**
     * Creates the SWT control for this editor.
     * @param parent the parent control
     */
    @Override
    public void createPartControl(Composite parent) {
        FillLayout layout = new FillLayout();
        parent.setLayout(layout);
        
        activeView.createControls(parent);
    }
    
    /**
     * Sets the focus to the viewer's control.
     */
    @Override
    public void setFocus() {
        if (activeView != null) {
            activeView.setFocus();
        }
    }
    
    /**
     * Disposes of this view.
     */
    @Override
    public void dispose() {
        if (activeView != null) {
            activeView.dispose();
        }
        
        cheditor = null;
        activeView = null;
        viewStates.clear();
        
        super.dispose();
    }
    
    /**
     * Receives a repository changed event.
     * @param evt the sent and received event
     */
    @Override
    public void notify(RepositoryChangedEvent evt) {
        dispose();
    }
    
    /**
     * Returns the source code view currently active.
     * @return the source code view
     */
    public SourceCodeView getActiveView() {
        return activeView;
    }
    
    /**
     * Show a source code view.
     * @param finfo the file information on the file displayed on the source code view
     */
    public void show(FileInfo finfo) {
        if (activeView == null) {
            return;
        }
        
        if (activeView.getFileInfo() != null) {
            if (finfo.getFilePath().compareTo(activeView.getFileInfo().getFilePath()) == 0) {
                return;
            }
            
            SourceCodeViewState prevState = activeView.createSourceCodeViewState();
            viewStates.put(activeView.getFileInfo().getKey(), prevState);
        }
        
        activeView.setFileInfo(finfo);
        activeView.setTimelineBar();
        setPartName("#" + finfo.getName());
        
        SourceCodeViewState curState = viewStates.get(finfo.getKey());
        if (curState == null) {
            curState = new SourceCodeViewState(finfo.getOperation(0).getTime(), 0, 100);
        }
        activeView.restoreSourceCodeViewState(curState);
        
        setFocus();
    }
    
    /**
     * Does nothing because this editor has no file although this method saves the contents of this editor.
     * @param monitor the progress monitor
     */
    @Override
    public void doSave(IProgressMonitor monitor) {
    }
    
    /**
     * Does nothing because this editor has no file although this method saves the contents of this editor.
     */
    @Override
    public void doSaveAs() {
    }
    
    /**
     * Tests if the save-as operation is supported by this part.
     * @return always <code>false</code> because this editor has no file
     */
    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }
    
    /**
     * Tests if the contents of this part have changed since the last save operation.
     * @return always <code>false</code> because this editor has no file
     */
    @Override
    public boolean isDirty() {
        return false;
    }
}

/**
 * The implementation of the editor input, which is used for opening an editor without a file.
 * @author Katsuhisa Maruyama
 */
class EmptyEditorInput implements IEditorInput {
    
    /**
     * Tests if the editor input exists.
     * @return always true
     */
    @Override
    public boolean exists() {
        return true;
    }
    
    /**
     * Returns the image descriptor for this input.
     * @return the default return value
     */
    @Override
    public ImageDescriptor getImageDescriptor() {
        return ImageDescriptor.getMissingImageDescriptor();
    }
    
    /**
     * Returns the name of this editor input like a file name.
     * @return the name
     */
    @Override
    public String getName() {
        return "Non-file";
    }
    
    /**
     * Returns the tool tip text for this editor input.
     * @return the tool tip text
     */
    @Override
    public String getToolTipText() {
        return getName();
    }
    
    /**
     * Returns an object that can be used to save the state of this editor input.
     * @return always <code>null</code>
     */
    @Override
    public IPersistableElement getPersistable() {
        return null;
    }
    
    /**
     * Returns an instance of the given class associated with this instance.
     * @param adapter the adapter class to look up
     * @return always <code>null</code>
     */
    @Override
    @SuppressWarnings("rawtypes")
    public Object getAdapter(Class adapter) {
        return null;
    }
}
