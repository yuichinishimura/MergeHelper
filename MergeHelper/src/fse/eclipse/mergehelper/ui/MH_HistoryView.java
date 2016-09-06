package fse.eclipse.mergehelper.ui;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.jtool.changerecorder.util.StringComparator;
import org.jtool.changerecorder.util.Time;
import org.jtool.changereplayer.event.ViewChangedEvent;
import org.jtool.changereplayer.event.ViewEventSource;
import org.jtool.changereplayer.ui.HistoryView;
import org.jtool.changerepository.data.FileInfo;
import org.jtool.changerepository.event.RepositoryEventSource;
import org.jtool.changerepository.operation.CodeInsertedOperation;
import org.jtool.changerepository.operation.UnifiedOperation;

import fse.eclipse.mergehelper.Activator;
import fse.eclipse.mergehelper.element.BranchFileInfo;
import fse.eclipse.mergehelper.element.BranchJavaElement;
import fse.eclipse.mergehelper.element.BranchRootInfo;
import fse.eclipse.mergehelper.element.ConflictInfo;

public class MH_HistoryView extends HistoryView {
    public static final String ID = "ChangeHistory.view.MH_HistoryView";

    private static ImageDescriptor mergeIcon = Activator.getImageDescriptor("icons/m_arrow.gif");
    private static Image conflictIcon = Activator.getImage("icons/conflict.gif");

    private static MH_HistoryView instance;

    private BranchFileInfo bfInfo;
    private ConflictInfo cInfo;
    private int mergeIdx;

    private Action elementButton, mergedButton;

    private static final String DefaultElementButtonText = "All Elements";
    private static final int IDX_COL = 0, UID_COL = 1, TIME_COL = 2, OP_COL = 3, ELEM_COL = 4;
    private static final String MERGE_MARK = "☆";

    public MH_HistoryView() {
        RepositoryEventSource.getInstance().addEventListener(this);
        ViewEventSource.getInstance().addEventListener(this);
        instance = this;
    }

    public static MH_HistoryView getInstance() {
        return instance;
    }

    public int getTableSize() {
        return operationTable.getItemCount();
    }

    public int getAMergeIndex() {
        return mergeIdx;
    }

    @Override
    public void createPartControl(Composite parent) {
        operationTable = new Table(parent, SWT.BORDER | SWT.SINGLE | SWT.CHECK | SWT.VIRTUAL | SWT.H_SCROLL | SWT.V_SCROLL);
        operationTable.setLinesVisible(true);
        operationTable.setHeaderVisible(true);

        TableColumn idxColumn = new TableColumn(operationTable, SWT.LEFT);
        idxColumn.setText("idx");
        idxColumn.setWidth(80);
        idxColumn.setResizable(true);

        TableColumn idColumn = new TableColumn(operationTable, SWT.LEFT);
        idColumn.setText("UID");
        idColumn.setWidth(80);
        idColumn.setResizable(true);

        TableColumn timeColumn = new TableColumn(operationTable, SWT.LEFT);
        timeColumn.setText("time");
        timeColumn.setWidth(10);
        timeColumn.setResizable(true);

        TableColumn typeColumn = new TableColumn(operationTable, SWT.LEFT);
        typeColumn.setText("operation");
        typeColumn.setWidth(200);
        typeColumn.setResizable(true);

        TableColumn detailsColumn = new TableColumn(operationTable, SWT.LEFT);
        detailsColumn.setText("element");
        detailsColumn.setWidth(300);
        detailsColumn.setResizable(true);

        final int MARGIN = 2;
        FormData opdata = new FormData();
        opdata.top = new FormAttachment(0, MARGIN);
        opdata.bottom = new FormAttachment(100, -MARGIN);
        opdata.left = new FormAttachment(0, MARGIN);
        opdata.right = new FormAttachment(100, -MARGIN);
        operationTable.setLayoutData(opdata);

        makeToolBarActions(parent);

        selectionListener = new OperationTableSelectionListener();
        operationTable.addSelectionListener(selectionListener);

        setOperationTable();
    }

    private void makeToolBarActions(Composite parent) {
        elementButton = new Action(DefaultElementButtonText, IAction.AS_DROP_DOWN_MENU) {
            @Override
            public void run() {
                sendViewEvent();
            }
        };
        elementButton.setEnabled(false);
        elementButton.setChecked(false);

        mergedButton = new Action("Only After Merge", IAction.AS_CHECK_BOX) {
            @Override
            public void run() {
                updateTableItemCheck(elementButton.getText(), mergedButton.isChecked());
                sendViewEvent();
            }
        };
        mergedButton.setImageDescriptor(mergeIcon);
        mergedButton.setEnabled(false);
        mergedButton.setChecked(true);

        IToolBarManager manager = getViewSite().getActionBars().getToolBarManager();
        manager.add(elementButton);
        manager.add(mergedButton);
    }

    @Override
    protected void setOperationTable(FileInfo fInfo) {
        List<UnifiedOperation> ops = fInfo.getOperations();
        if (ops.size() == 0) {
            return;
        }

        if (fileInfo == null || fInfo.getFilePath().compareTo(fileInfo.getFilePath()) != 0) {
            fileInfo = fInfo;
            BranchRootInfo rootInfo = BranchRootInfo.getInstance();

            bfInfo = rootInfo.getBranchFileInfo(fInfo);
            cInfo = rootInfo.getConflictInfo();

            createTableItems(bfInfo);

            setupToolBarActions();

            operationTable.deselectAll();
            operationTable.update();

            sendViewEvent();
            goToTop();
        }
    }

    private void createTableItems(BranchFileInfo bfInfo) {
        operationTable.removeAll();

        List<UnifiedOperation> ops = fileInfo.getOperations();
        for (int i = 0; i < ops.size(); i++) {
            String id;
            String time;
            String text;
            String elemName;

            UnifiedOperation op = ops.get(i);
            if (CodeInsertedOperation.isCodeInsertedOperation(op)) {
                mergeIdx = i;
                id = MERGE_MARK;
                time = "";
                elemName = "";
            } else {
                id = String.valueOf(op.getId());
                time = Time.toUsefulFormat(op.getTime());
                BranchJavaElement elem = bfInfo.getBranchJavaElement(op);
                if (elem != null) {
                    elemName = elem.getSimpleName();
                } else {
                    elemName = "";
                }
            }
            text = OperationRepresentation.createOperationTextualRepresentation(op);

            TableItem item = new TableItem(operationTable, SWT.NONE);
            item.setText(IDX_COL, String.valueOf(i + 1));
            item.setText(UID_COL, id);
            item.setText(TIME_COL, time);
            item.setText(OP_COL, text);
            item.setText(ELEM_COL, elemName);
        }
    }

    private void setupToolBarActions() {
        List<BranchJavaElement> elems = bfInfo.getAllBranchJavaElement();

        elementButton.setText(indentText(DefaultElementButtonText));
        if (elems.size() > 0) {
            elementButton.setMenuCreator(new MenuCreator(elems));
            elementButton.setEnabled(true);

            mergedButton.setEnabled(true);
            mergedButton.setChecked(true);
            updateTableItemCheck(DefaultElementButtonText, true);
        } else {
            elementButton.setEnabled(false);

            mergedButton.setEnabled(false);
            mergedButton.setChecked(false);
            updateTableItemCheck(DefaultElementButtonText, false);
        }
    }

    @Override
    public void notify(ViewChangedEvent evt) {
        Object source = evt.getSource();

        if (source instanceof MH_SourceCodeView) {
            if (operationTable != null && operationTable.getItemCount() > 0) {
                MH_SourceCodeView view = (MH_SourceCodeView) source;
                setOperationTable(view.getFileInfo());
                goTo(view.getCurrentOperationIndex());
                return;
            }
        }
        super.notify(evt);
    }

    @Override
    public void setFocus() {
        operationTable.setFocus();
    };

    public void refresh() {
        bfInfo = null;
        cInfo = null;
        mergeIdx = -1;
        if (operationTable != null && !operationTable.isDisposed()) {
            operationTable.removeAll();
        }
    }

    @Override
    public void goTo(int idx) {
        if (fileInfo == null && idx < 0) {
            return;
        }

        if (operationTable.getItemCount() <= idx) {
            return;
        }

        currentOperationIndex = idx;
        operationTable.select(idx);
        reveal(idx);
    }

    private void goToTop() {
        int idx;
        TableItem[] items = operationTable.getItems();
        int length = items.length;
        for (idx = 0; idx < length; idx++) {
            if (items[idx].getChecked()) {
                break;
            }
        }
        goTo(idx);
    }

    private void sendViewEvent() {
        ViewChangedEvent event = new ViewChangedEvent(this);
        ViewEventSource.getInstance().fire(event);
    }

    private void updateTableItemCheck(String elemName, boolean isMergedOnly) {
        TableItem[] items = operationTable.getItems();
        int length = items.length;
        int i;
        if (isMergedOnly) {
            if (mergeIdx + 1 < length) {
                for (i = 0; i < mergeIdx; i++) {
                    items[i].setChecked(false);
                }
                if (mergeIdx != -1) {
                    i = mergeIdx;
                    items[i].setChecked(true);
                    i++;
                }
            } else {
                for (i = 0; i < length; i++) {
                    items[i].setChecked(false);
                }
                return;
            }
        } else {
            i = 0;
        }

        if (StringComparator.isSame(elemName, DefaultElementButtonText)) {
            for (; i < length; i++) {
                items[i].setChecked(true);
            }
        } else {
            for (; i < length; i++) {
                TableItem item = items[i];
                String name = item.getText(ELEM_COL);
                if (StringComparator.isSame(name, elemName) || i == mergeIdx) {
                    items[i].setChecked(true);
                } else {
                    items[i].setChecked(false);
                }
            }
        }

        goToTop();
    }

    // ツールバーのアイコンが消える問題への対処 (対処できてない)
    private String indentText(String elemName) {
        int nameLen = elemName.length();
        int maxLen = DefaultElementButtonText.length();

        if (nameLen == maxLen) {
            return elemName;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(elemName);
        if (nameLen < maxLen) {
            int spaceNum = maxLen - nameLen;
            for (int i = 0; i < spaceNum; i++) {
                sb.insert(0, " ");
            }
        } else {
            sb.delete(maxLen, sb.length());
        }
        return sb.toString();
    }

    private class OperationTableSelectionListener implements SelectionListener {

        OperationTableSelectionListener() {
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent evt) {
        }

        @Override
        public void widgetSelected(SelectionEvent evt) {
            Table table = (Table) evt.getSource();
            int idx = (int) table.getSelectionIndex();

            goTo(idx);

            sendViewEvent();
        }
    }

    private class MenuCreator implements IMenuCreator {
        final List<BranchJavaElement> elems;

        MenuCreator(List<BranchJavaElement> elems) {
            this.elems = elems;
        }

        @Override
        public Menu getMenu(Control parent) {
            Menu menu = new Menu(parent);
            MenuItem item0 = new MenuItem(menu, SWT.PUSH);
            item0.setText(DefaultElementButtonText);
            item0.addSelectionListener(new SelectionListener() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    MenuItem item = (MenuItem) event.widget;
                    String elemName = item.getText();
                    updateTableItemCheck(elemName, mergedButton.isChecked());
                    elementButton.setText(indentText(elemName));

                    sendViewEvent();
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent event) {
                }
            });

            for (BranchJavaElement elem : elems) {
                MenuItem item = new MenuItem(menu, SWT.PUSH);
                item.setText(elem.getSimpleName());
                if (cInfo.isConflictElement(elem)) {
                    item.setImage(conflictIcon);
                }

                item.addSelectionListener(new SelectionListener() {
                    @Override
                    public void widgetSelected(SelectionEvent event) {
                        sendViewEvent();

                        MenuItem item = (MenuItem) event.widget;
                        String elemName = item.getText();
                        updateTableItemCheck(elemName, mergedButton.isChecked());
                        elementButton.setText(indentText(elemName));
                    }

                    @Override
                    public void widgetDefaultSelected(SelectionEvent arg0) {
                    }
                });
            }
            return menu;
        }

        @Override
        public Menu getMenu(Menu menu) {
            return null;
        }

        @Override
        public void dispose() {
        }
    }
}
