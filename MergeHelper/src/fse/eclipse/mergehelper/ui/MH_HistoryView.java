package fse.eclipse.mergehelper.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.jtool.changerecorder.util.Time;
import org.jtool.changereplayer.event.ViewChangedEvent;
import org.jtool.changereplayer.event.ViewEventSource;
import org.jtool.changereplayer.ui.HistoryView;
import org.jtool.changerepository.data.FileInfo;
import org.jtool.changerepository.event.RepositoryEventSource;
import org.jtool.changerepository.operation.UnifiedOperation;

import fse.eclipse.mergehelper.Activator;
import fse.eclipse.mergehelper.element.BranchFileInfo;
import fse.eclipse.mergehelper.element.BranchFileInfo.MergedResult;
import fse.eclipse.mergehelper.element.BranchRootInfo;
import fse.eclipse.mergehelper.element.ConflictInfo;
import fse.eclipse.mergehelper.element.ElementSlice;
import fse.eclipse.mergehelper.element.MergePoint;
import fse.eclipse.mergehelper.element.MergeType;
import fse.eclipse.mergehelper.util.RepositoryElementInfoUtil;

public class MH_HistoryView extends HistoryView {

    public static final String ID = "ChangeHistory.view.MH_HistoryView";

    private static ImageDescriptor mergeIcon = Activator.getImageDescriptor("icons/m_arrow.gif");
    private static Image conflictIcon = Activator.getImage("icons/conflict.gif");

    private static MH_HistoryView instance;

    private BranchFileInfo bfInfo;
    private ConflictInfo cInfo;
    private MergePoint mPoint;
    private int mergeIdx;

    private Action elementButton, mergedButton;

    private static final String DefaultElementButtonText = "All Elements";
    private static final int ID_COL = 0, TIME_COL = 1, OP_COL = 2, ELEM_COL = 3;
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

    @Override
    public void createPartControl(Composite parent) {
        operationTable = new Table(parent, SWT.BORDER | SWT.SINGLE | SWT.CHECK | SWT.VIRTUAL | SWT.H_SCROLL | SWT.V_SCROLL);
        operationTable.setLinesVisible(true);
        operationTable.setHeaderVisible(true);

        TableColumn idColumn = new TableColumn(operationTable, SWT.LEFT);
        idColumn.setText("id");
        idColumn.setWidth(60);
        idColumn.setResizable(true);

        TableColumn timeColumn = new TableColumn(operationTable, SWT.LEFT);
        timeColumn.setText("time");
        timeColumn.setWidth(140);
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
            mPoint = rootInfo.getMergePoint();

            createTableItems(bfInfo);

            setupToolBarActions();

            operationTable.deselectAll();
            operationTable.update();

            sendViewEvent();
            goToTop();
        }
    }

    private void setupToolBarActions() {
        List<ElementSlice> slices = bfInfo.getAllSlice();
        List<String> elemNames = new ArrayList<String>();
        for (ElementSlice slice : slices) {
            elemNames.add(slice.getName());
        }

        elementButton.setText(indentText(DefaultElementButtonText));
        if (elemNames.size() > 0) {
            elementButton.setMenuCreator(new MenuCreator(elemNames));
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

    private void createTableItems(BranchFileInfo bfInfo) {
        operationTable.removeAll();

        String branchName = RepositoryElementInfoUtil.getBranchName(fileInfo);
        MergeType type = BranchRootInfo.getInstance().getType(branchName);
        int mergePoint = mPoint.getFileId(type);
        Map<Integer, MergedResult> resultMap = bfInfo.getMergedResultMap();

        List<UnifiedOperation> ops = fileInfo.getOperations();
        int size = ops.size();

        boolean setMergeItemFlag = false;
        String code = "";
        for (int i = 0; i < size; i++) {
            UnifiedOperation op = ops.get(i);

            int id = op.getId();
            int offset;
            if (!op.isCommitOpeartion()) {
                if (id <= mergePoint + 1) {
                    offset = op.getStart();
                    code = fileInfo.getCode(i);
                } else {
                    offset = resultMap.get(id - 1).getOperationOffset();
                    code = resultMap.get(id - 1).getCode();
                }
            } else {
                offset = -1;
            }
            String text = OperationRepresentation.createOperationTextualRepresentation(op, offset);
            String elemName = getAssociatedName(bfInfo, op);

            TableItem item = new TableItem(operationTable, SWT.NONE);
            item.setText(ID_COL, String.valueOf(i + 1));
            item.setText(TIME_COL, Time.toUsefulFormat(op.getTime()));
            item.setText(OP_COL, text);
            item.setText(ELEM_COL, elemName);
            item.setData(code);

            if (setMergeItemFlag) {
                mergeIdx = i;
                createMergeItem(cInfo, type, resultMap.get(mergePoint).getCode());
                setMergeItemFlag = false;
            } else if (id == mergePoint) {
                setMergeItemFlag = true;
            }
        }

        if (setMergeItemFlag) {
            mergeIdx = size - 1;
            createMergeItem(cInfo, type, resultMap.get(mergePoint).getCode());
        }
    }

    private void createMergeItem(ConflictInfo cInfo, MergeType type, String mergedCode) {
        MergeType aType = MergeType.getAnotherType(type);
        StringBuilder sb = new StringBuilder();
        sb.append(BranchRootInfo.getInstance().getBranchName(aType));
        sb.append(" : ").append(mPoint.getFileId(aType));

        TableItem item = new TableItem(operationTable, SWT.NONE);
        item.setText(ID_COL, MERGE_MARK);
        item.setText(TIME_COL, sb.toString());
        item.setData(mergedCode);
    }

    private String getAssociatedName(BranchFileInfo bfInfo, UnifiedOperation op) {
        List<ElementSlice> slices = bfInfo.getSlices(op);
        if (slices == null || slices.size() == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (ElementSlice slice : slices) {
            sb.append(slice.getName()).append(",");
        }
        sb.delete(sb.length() - 1, sb.length());
        return sb.toString();
    }

    @Override
    public void notify(ViewChangedEvent evt) {
        Object source = evt.getSource();
        if (source instanceof MH_SourceCodeView) {
            if (operationTable != null && operationTable.getItemCount() > 0) {
                MH_SourceCodeView view = (MH_SourceCodeView) source;
                int idx = view.getCurrentOperationIndex();
                String code = operationTable.getItems()[idx].getData().toString();
                view.update(code);
                goTo(idx);
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
        mPoint = null;
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
        elemName = elemName.trim();

        TableItem[] items = operationTable.getItems();
        int length = items.length;
        if (elemName.equals(DefaultElementButtonText)) {
            for (int i = 0; i < length; i++) {
                TableItem item = items[i];
                if (item.getText(ELEM_COL).length() > 0 || i == mergeIdx + 1) {
                    item.setChecked(true);
                } else {
                    item.setChecked(false);
                }
            }
        } else {
            for (int i = 0; i < length; i++) {
                TableItem item = items[i];
                if (i != mergeIdx + 1) {
                    String[] elems = item.getText(ELEM_COL).split(",");
                    boolean isCheck = false;
                    for (String elem : elems) {
                        if (elem.equals(elemName)) {
                            isCheck = true;
                            break;
                        }
                    }
                    item.setChecked(isCheck);
                } else {
                    item.setChecked(true);
                }
            }
        }

        if (isMergedOnly) {
            for (int i = 0; i <= mergeIdx; i++) {
                TableItem item = items[i];
                item.setChecked(false);
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
        final List<String> elemNames;

        MenuCreator(List<String> elemNames) {
            this.elemNames = elemNames;
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

            for (String elemName : elemNames) {
                MenuItem item = new MenuItem(menu, SWT.PUSH);
                item.setText(elemName);
                if (cInfo.isConflictElement(elemName)) {
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
