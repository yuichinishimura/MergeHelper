/*
 *  Copyright 2015
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changereplayer.ui;

import org.jtool.changereplayer.event.ViewChangedListener;
import org.jtool.changereplayer.event.ViewChangedEvent;
import org.jtool.changereplayer.event.ViewEventSource;
import org.jtool.changerepository.event.RepositoryChangedEvent;
import org.jtool.changerepository.event.RepositoryChangedListener;
import org.jtool.changerepository.event.RepositoryEventSource;
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.CompareViewerSwitchingPane;
import org.eclipse.compare.IEncodedStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * Creates a project selection view for replayer perspective.
 * @author Katsuhisa Maruyama
 */
public class CodeCompareView extends ViewPart implements RepositoryChangedListener, ViewChangedListener {
    
    /**
     * The identification string that is used to register this view
     */
    public static final String ID = "operationreplayerj.ui.codecompareview";
    
    /**
     * The viewer that displays differences between codes before and after a change
     */
    private CompareViewerSwitchingPane compareView;
    
    /**
     * Creates the SWT control for this view.
     * @param parent the parent control
     */
    @Override
    public void createPartControl(Composite parent) {
        final CompareConfiguration compareConfiguration = new CompareConfiguration();
        compareConfiguration.setLeftLabel("Source before the change"); 
        compareConfiguration.setLeftEditable(false);
        compareConfiguration.setRightLabel("Source after the change");
        compareConfiguration.setRightEditable(false);
        compareConfiguration.setProperty(CompareConfiguration.IGNORE_WHITESPACE, Boolean.FALSE);
        
        compareView = new CompareViewerSwitchingPane(parent, SWT.BORDER | SWT.FLAT) {
            
            protected Viewer getViewer(Viewer oldViewer, Object input) {
                Viewer viewer = CompareUI.findContentViewer(oldViewer, input, this, compareConfiguration);
                viewer.getControl().setData(CompareUI.COMPARE_VIEWER_TITLE, "Change Compare");
                
                return viewer;
            }
        };
        
        compareView.setContent(compareView.getViewer().getControl());
        
        FormData mdata = new FormData();
        mdata.top = new FormAttachment(0, 0);
        mdata.bottom = new FormAttachment(100, 0);
        mdata.left = new FormAttachment(0, 0);
        mdata.right = new FormAttachment(100, 0);
        compareView.setLayoutData(mdata);
        
        RepositoryEventSource.getInstance().addEventListener(this);
        ViewEventSource.getInstance().addEventListener(this);
    }
    
    /**
     * Sets the focus to the viewer's control.
     */
    @Override
    public void setFocus() {
        compareView.setFocus();
    }
    
    /**
     * Receives the changed event and updates this view.
     * @param evt the sent and received event
     */
    public void notify(RepositoryChangedEvent evt) {
        // System.out.println("NOTIFY: SOURCE CODE VIEW " + evt.getSource() + " " + this);
        
        /*
        if (evt instanceof RepositoryUpdatedEvent) {
            WorkspaceInfo winfo = RepositoryManager.getWorkspaceInfo();
            for (CTabItem tab : tabFolder.getItems()) {
                SourceCodeViewer viewer = (SourceCodeViewer)tab.getData();
                FileInfo finfo = winfo.getNewFileInfo(viewer.getFileInfo());
                
                if (finfo != null) {
                    viewer.unconfigure();
                    tabFolder.setData(viewer.getFileInfo().getKey(), null);
                    tab.dispose();
                }
            }
            
        } else if (evt instanceof RepositoryClearedEvent) {
            for (CTabItem tab : tabFolder.getItems()) {
                SourceCodeViewer viewer = (SourceCodeViewer)tab.getData();
                viewer.unconfigure();
                tabFolder.setData(viewer.getFileInfo().getKey(), null);
                tab.dispose();
            }
        }
        */
    }
    
    /**
     * Receives the changed event and creates operation list for the project or source file.
     * @param evt the sent and received event
     */
    public void notify(ViewChangedEvent evt) {
        Object source = evt.getSource();
        
        if (source instanceof SourceCodeView) {
            SourceCodeView sview = (SourceCodeView)evt.getSource();
            if (sview == null || sview.getFileInfo() == null) {
                return;
            }
            
            String beforeCode = sview.getPreviousCode();
            String afterCode = sview.getCurrentCode();
            if (afterCode != null) {
                showDiff(beforeCode, afterCode);
            }
        }
    }
    
    /**
     * Shows differences between codes containing errors.
     * @param beforeCode the code before the operation was performed
     * @param afterCode the code after the operation was performed
     */
    private void showDiff(String beforeCode, String afterCode) {
        if (beforeCode == null) {
            beforeCode = "";
        }
        
        CompareElement left = new CompareElement(beforeCode);
        CompareElement right = new CompareElement(afterCode);
        compareView.setInput(new DiffNode(left, right));
    }
    
    /**
     * Defines an element to be compared.
     */
    private class CompareElement implements ITypedElement, IEncodedStreamContentAccessor {
        
        /**
         * The contents of this element
         */
        private String contents;
        
        /**
         * Creates an element.
         * @param contents contents of this element
         */
        public CompareElement(String contents) {
            this.contents = contents;
        }
        
        /**
         * Returns the name of this element.
         * @return always the empty string
         */
        public String getName() {
            return "";
        }
        
        /**
         * Returns the image for this element.
         * @return always <code>null</code>
         */
        public Image getImage() {
            return null;
        }
        
        /**
         * Returns the type of this element.
         * @return always the empty string
         */
        public String getType() {
            return "";
        }
        
        /**
         * Returns an open input stream for this element containing the contents of this element.
         * @return the input stream
         * @exception CoreException if the contents of this object could not be accessed
         */
        public InputStream getContents() {
            try {
                return new ByteArrayInputStream(contents.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                return new ByteArrayInputStream(contents.getBytes());
            }
        }
        
        /**
         * Returns the name of a charset encoding to be used 
         * @return always UTF-8
         * @exception CoreException if an error happens while determining the charset
         */
        public String getCharset() throws CoreException {
            return "UTF-8";
        }
    }
}
