/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changereplayer.ui;

import org.jtool.changereplayer.Activator;
import org.jtool.changereplayer.event.ViewChangedListener;
import org.jtool.changereplayer.event.ViewChangedEvent;
import org.jtool.changereplayer.event.ViewEventSource;
import org.jtool.changerepository.data.FileInfo;
import org.jtool.changerepository.operation.UnifiedOperation;
import org.jtool.changerepository.event.RepositoryChangedEvent;
import org.jtool.changerepository.event.RepositoryChangedListener;
import org.jtool.changerepository.event.RepositoryEventSource;
import org.jtool.changerecorder.operation.NormalOperation;
import org.jtool.changerecorder.operation.CopyOperation;
import org.jtool.changerecorder.operation.FileOperation;
import org.jtool.changerecorder.util.Time;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseListener;
import java.util.List;

/**
 * Creates a view that displays the history of operations.
 * @author Takayuki Omori
 * @author Katsuhisa Maruyama
 */
public class HistoryView extends ViewPart implements RepositoryChangedListener, ViewChangedListener {
    
    /**
     * The identification string that is used to register this view.
     */
    public static final String ID = "ChangeHistory.view.HistoryView";
    
    /**
     * The SWT widget that represents the table of operation history.
     */
    protected Table operationTable;
    
    /**
     * The information on the file of interest.
     */
    protected FileInfo fileInfo;
    
    /**
     * The sequence number of the operation of interest.
     */
    protected int currentOperationIndex = 0;
    
    /**
     * The listener that receives an event related to the selection of table items.
     */
    protected SelectionListener selectionListener;
    
    /**
     * The listener that receives an event related to the key press and release.
     */
    protected KeyListener keyListener;
    
    /**
     * The listener that receives an event related to the traverse.
     */
    protected TraverseListener traverseListener;
    
    /**
     * The listener that receives an event related to the check of table items.
     */
    protected Listener checkListener;
    
    /**
     * Creates an instance of a history view.
     */
    public HistoryView() {
        RepositoryEventSource.getInstance().addEventListener(this);
        ViewEventSource.getInstance().addEventListener(this);
    }
    
    /**
     * Creates the SWT control for this view.
     * @param parent the parent control
     */
    @Override
    public void createPartControl(Composite parent) {
        operationTable = new Table(parent, SWT.BORDER | SWT.SINGLE | SWT.CHECK | SWT.VIRTUAL | SWT.H_SCROLL | SWT.V_SCROLL);
        operationTable.setLinesVisible(true);
        operationTable.setHeaderVisible(true);
        
        TableColumn idColumn = new TableColumn(operationTable, SWT.LEFT);
        idColumn.setText("id");
        idColumn.setWidth(40);
        idColumn.setResizable(true);
        
        TableColumn timeColumn = new TableColumn(operationTable, SWT.LEFT);
        timeColumn.setText("time");
        timeColumn.setWidth(140);
        timeColumn.setResizable(false);
        
        TableColumn typeColumn = new TableColumn(operationTable, SWT.LEFT);
        typeColumn.setText("type");
        typeColumn.setWidth(100);
        typeColumn.setResizable(false);
        
        TableColumn detailsColumn = new TableColumn(operationTable, SWT.LEFT);
        detailsColumn.setText("contents");
        detailsColumn.setWidth(300);
        detailsColumn.setResizable(true);
        
        final int MARGIN = 2;
        FormData opdata = new FormData();
        opdata.top = new FormAttachment(0, MARGIN);
        opdata.bottom = new FormAttachment(100, -MARGIN);
        opdata.left = new FormAttachment(0, MARGIN);
        opdata.right = new FormAttachment(100, -MARGIN);
        operationTable.setLayoutData(opdata);
        
        selectionListener = new OperationTableSelectionListener();
        operationTable.addSelectionListener(selectionListener);
        
        checkListener = new OperationCheckListener();
        operationTable.addListener(SWT.Selection, checkListener);
        
        setOperationTable();
    }
    
    /**
     * Returns the sequence number of the operation of interest.
     * @return the sequence number of the operation
     */
    public int getCurrentOperationIndex() {
        return currentOperationIndex;
    }
    
    /**
     * Returns the table that displays the operation history.
     * @return the table for the operation history
     */
    public Table getOperationTable() {
        return operationTable;
    }
    
    /**
     * Obtains the package explorer view.
     * @return the package explorer view instance, or <code>null</code> if such view was not found
     */
    protected PackageExplorerView getPackageExplorerView() {
        IWorkbenchPage workbenchPage = Activator.getWorkbenchPage();
        return (PackageExplorerView)workbenchPage.findView(PackageExplorerView.ID);
    }
    
    /**
     * Sets the contents of operation table.
     */
    protected void setOperationTable() {
        PackageExplorerView pview = getPackageExplorerView();
        if (pview == null) {
            operationTable.removeAll();
            return;
        }
        
        FileInfo finfo = pview.getFileInfo();
        if (finfo == null) {
            operationTable.removeAll();
            return;
        }
        
        setOperationTable(finfo);
        
        int idx = pview.getEditor().getActiveView().getCurrentOperationIndex();
        operationTable.select(idx);
    }
    
    /**
     * Sets the contents of operation table for file information.
     * @param finfo the file information
     */
    protected void setOperationTable(FileInfo finfo) {
        List<UnifiedOperation> ops = finfo.getOperations();
        if (ops.size() == 0) {
            return;
        }
        
        if (fileInfo == null || finfo.getFilePath().compareTo(fileInfo.getFilePath()) != 0) {
            fileInfo = finfo;
            createTableItems(ops);
            
            operationTable.deselectAll();
            operationTable.update();
            
            currentOperationIndex = 0;
            operationTable.select(0);
        }
    }
    
    /**
     * Resets the contents of operation table.
     */
    protected void resetOperationTable() {
        operationTable.removeAll();
        operationTable.update();
    }
    
    /**
     * Assigns information on operations to respective table items.
     * @param ops the collection of the operations to be displayed
     */
    private void createTableItems(List <UnifiedOperation> ops) {
        operationTable.removeAll();
        for (int i = 0; i < ops.size(); i++) {
            UnifiedOperation op = ops.get(i);
            
            TableItem item = new TableItem(operationTable, SWT.NONE);
            item.setText(0, String.valueOf(i + 1));
            item.setText(1, Time.toUsefulFormat(op.getTime()));
            item.setText(2, createOperationTextualRepresentation(op));
            item.setChecked(true);
        }
    }
    
    /**
     * Creates textual representation for a given operation.
     * @param op the operation
     * @return the string representing the operation
     */
    private String createOperationTextualRepresentation(UnifiedOperation op) {
        if (op.isNormalOperation()) {
            return createNormalOperationTextualRepresentation((NormalOperation)op.getIOperation());
            
        } else if (op.isCopyOperation()) {
            return createCopyOperationTextualRepresentation((CopyOperation)op.getIOperation());
        } else if (op.isFileOperation()) {
            return createFileOperationTextualRepresentation((FileOperation)op.getIOperation());
        }
        return "";
    }
    
    /**
     * Creates textual representation for a given normal operation.
     * @param op the normal operation
     * @return the string representing the operation
     */
    private String createNormalOperationTextualRepresentation(NormalOperation op) {
        StringBuilder buf = new StringBuilder();
        
        if (op.getActionType() != NormalOperation.Type.NO) {
            buf.append(op.getActionType().toString());
            buf.append(" ");
        }
        
        buf.append(op.getStart());
        buf.append(" ");
        
        if (op.getInsertedText().length() > 0) {
            buf.append("ins[");
            buf.append(getText(op.getInsertedText()));
            buf.append("] ");
        }
        if (op.getDeletedText().length() > 0) {
            buf.append("del[");
            buf.append(getText(op.getDeletedText()));
            buf.append("]");
        }
        
        return buf.toString();
    }
    
    /**
     * Creates textual representation for a given copy operation.
     * @param op the copy operation
     * @return the string representing the operation
     */
    private String createCopyOperationTextualRepresentation(CopyOperation op) {
        StringBuilder buf = new StringBuilder();
        
        buf.append("COPY ");
        buf.append(op.getStart());
        buf.append(" copied[");
        buf.append(getText(op.getCopiedText()));
        buf.append("]");
        
        return buf.toString();
    }
    
    /**
     * Creates textual representation for a given file operation.
     * @param op the file operation
     * @return the string representing the operation
     */
    private String createFileOperationTextualRepresentation(FileOperation op) {
        StringBuilder buf = new StringBuilder();
        
        if (op.getActionType() != FileOperation.Type.NONE) {
            buf.append(op.getActionType().toString());
            buf.append(" ");
        }
        
        return buf.toString();
    }
    
    /**
     * Converts a text into its pretty one.
     * @param text the original text
     * @return the text consists of the first four characters not including the new line
     */
    public String getText(String text) {
        final int LESS_LEN = 9;
        
        String text2;
        if (text.length() < LESS_LEN + 1) {
            text2 = text;
        } else {
            text2 = text.substring(0, LESS_LEN + 1) + "...";
        }
        
        return text2.replace('\n', '~');
    }
    
    /**
     * Receives a repository changed event.
     * @param evt the sent and received event
     */
    @Override
    public void notify(RepositoryChangedEvent evt) {
        resetOperationTable();
    }
    
    /**
     * Receives a view changed event.
     * @param evt the sent and received event
     */
    @Override
    public void notify(ViewChangedEvent evt) {
        Object source = evt.getSource();
        
        if (source instanceof SourceCodeView) {
            SourceCodeView sview = (SourceCodeView)source;
            setOperationTable(sview.getFileInfo());
            goTo(sview.getCurrentOperationIndex());
        }
    }
    
    /**
     * Sets the focus to the viewer's control.
     */
    @Override
    public void setFocus() {
        operationTable.setFocus();
    }
    
    /**
     * Disposes of this view. 
     */
    @Override
    public void dispose() {
        RepositoryEventSource.getInstance().removeEventListener(this);
        ViewEventSource.getInstance().removeEventListener(this);
        
        if (!operationTable.isDisposed()) {
            operationTable.removeSelectionListener(selectionListener);
            operationTable.removeKeyListener(keyListener);
            operationTable.removeTraverseListener(traverseListener);
            operationTable.removeListener(SWT.Selection, checkListener);
        }
        operationTable.dispose();
        
        super.dispose();
    }
    
    /**
     * Goes to a specified operation.
     * @param idx the sequence number of the operation
     */
    public void goTo(int idx) {
        if (fileInfo == null) {
            return;
        }
        
        List<UnifiedOperation> ops = fileInfo.getOperations();
        if (idx < 0 || ops.size() <= idx) {
            return;
        }
        
        currentOperationIndex = idx;
        operationTable.select(idx);
        reveal(idx);
    }
    
    /**
     * Sends the view changed event.
     */
    private void sendViewEvent() {
        ViewChangedEvent event = new ViewChangedEvent(this);
        ViewEventSource.getInstance().fire(event);
    }
    
    
    /**
     * Ensures that the selected operation is visible, scrolling the viewer if necessary.
     * @param idx the sequence number of the selected operation
     */
    protected void reveal(int idx) {
        Rectangle area = operationTable.getClientArea();
        int num = area.height / operationTable.getItemHeight() - 1;
        
        int top = operationTable.getTopIndex();
        if (idx < top) {
            top = idx;
        } else if (idx >= top + num) {
            top = idx - num + 1;
        }
        
        operationTable.setTopIndex(top);
    }
    
    /**
     * Deals with the events that are generated when selection occurs in a control.
     */
    private class OperationTableSelectionListener implements SelectionListener {
        
        /**
         * Creates a listener that deals with the selection events.
         */
        OperationTableSelectionListener() {
        }
        
        /**
         * Receives the selection event when the default selection occurs in the control.
         * @param evt the event containing information about the selection
         */
        @Override
        public void widgetDefaultSelected(SelectionEvent evt) {
        }
        
        /**
         * Receives the selection event when selection occurs in the control.
         * @param evt the event containing information about the selection
         */
        @Override
        public void widgetSelected(SelectionEvent evt) {
            Table table = (Table)evt.getSource();
            int idx = (int)table.getSelectionIndex();
            
            goTo(idx);
            
            sendViewEvent();
        }
    }
    
    /**
     * Deals with the events that are generated when selection occurs in a control.
     */
    private class OperationCheckListener implements Listener {
        
        /**
         * Creates a listener that deals with the key events.
         */
        OperationCheckListener() {
        }
        
        /**
         * Receives the selection event when an item is checked or unchecked.
         * @param evt the event containing information about the check
         */
        @Override
        public void handleEvent(Event evt) {
            if (evt.detail == SWT.CHECK) {
                if (evt.item instanceof TableItem) {
                    TableItem item = (TableItem)evt.item;
                    if (item.getChecked()) {
                        item.setChecked(false);
                    } else {
                        item.setChecked(true);
                    }
                }
            }
        }
    }
}
