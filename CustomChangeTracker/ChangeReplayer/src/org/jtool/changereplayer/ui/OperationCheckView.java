/*
 *  Copyright 2015
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changereplayer.ui;

import org.eclipse.ui.PlatformUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareViewerSwitchingPane;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.IEncodedStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.core.runtime.CoreException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * Creates a view that displays differences between codes containing errors.
 * @author Katsuhisa Maruyama
 */
public class OperationCheckView {
    
    /**
     * The shell of this frame.
     */
    private Shell shell = null;
    
    /**
     * The viewer that displays differences between codes containing errors.
     */
    private CompareViewerSwitchingPane compareView;
    
    /**
     * Creates an empty instance.
     */
    public OperationCheckView() {
    }
    
    /**
     * Opens the viewer.
     */
    public void open() {
        if (shell == null) {
            Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
            shell = new Shell(parent.getDisplay());
            shell.setLayout(new FormLayout());
            shell.setSize(1500, 1000);
            
            createCompareViewer(shell);
            
            shell.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e) {
                    shell = null;
                }
            });
        }
        
        shell.open();
    }
    
    /**
     * Creates the viewer.
     * @param parent the parent control.
     */
    private void createCompareViewer(final Composite parent) {
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
    }
    
    /**
     * Shows differences between codes containing errors.
     * @param beforeCode the code before the operation was performed
     * @param afterCode the code after the operation was performed
     */
    public void showDiff(String beforeCode, String afterCode) {
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
