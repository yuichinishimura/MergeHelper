package fse.eclipse.mergehelper.ui.dialog;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.jtool.changerecorder.util.Time;
import org.jtool.changerepository.operation.UnifiedOperation;

import fse.eclipse.mergehelper.element.BranchInfo;
import fse.eclipse.mergehelper.element.BranchRootInfo;
import fse.eclipse.mergehelper.element.ConflictInfo;
import fse.eclipse.mergehelper.element.ElementSlice;
import fse.eclipse.mergehelper.element.MergePoint;
import fse.eclipse.mergehelper.element.MergeType;

public class ResultDialog extends AbstractUIDialog {

    private static final String TITLE = "Detecion Result";

    public ResultDialog(Composite parent) {
        super(parent);
    }

    @Override
    protected void execute() {
        // no execute
    }

    @Override
    protected void nextProgress() {
        finish();
    }

    @Override
    protected void createDialog() {
        dialog.setLayout(new GridLayout(1, false));

        BranchRootInfo rootInfo = BranchRootInfo.getInstance();
        BranchInfo srcInfo = rootInfo.getBranchInfo(MergeType.SRC);
        BranchInfo destInfo = rootInfo.getBranchInfo(MergeType.DEST);

        StringBuilder sb = new StringBuilder();
        sb.append("Total number of operations\n");
        sb.append(srcInfo.getName()).append(":");
        sb.append(srcInfo.getProjectInfo().getOperationNumber()).append("\n");

        sb.append(destInfo.getName()).append(":");
        sb.append(destInfo.getProjectInfo().getOperationNumber()).append("\n");

        sb.append("\n");

        sb.append("Following program elements are edited by each branch");
        Label conflictLabel = new Label(dialog, SWT.NONE);
        conflictLabel.setText(sb.toString());
        conflictLabel.setLayoutData(grabExFILLGridData());

        Table elemTable = new Table(dialog, SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER | SWT.VIRTUAL | SWT.H_SCROLL | SWT.V_SCROLL);
        elemTable.setLinesVisible(true);
        elemTable.setHeaderVisible(true);

        TableColumn elemColumn = new TableColumn(elemTable, SWT.LEFT);
        elemColumn.setText("Element");
        elemColumn.setWidth(230);
        elemColumn.setResizable(true);

        TableColumn srcPoint = new TableColumn(elemTable, SWT.LEFT);
        srcPoint.setText(srcInfo.getName());
        srcPoint.setWidth(170);
        srcPoint.setResizable(true);

        TableColumn destPoint = new TableColumn(elemTable, SWT.LEFT);
        destPoint.setText(destInfo.getName());
        destPoint.setWidth(170);
        destPoint.setResizable(true);

        GridData elemTableData = grabExFILLGridData();
        elemTableData.heightHint = tableHeight * 3 / 2;
        elemTableData.widthHint = 230 + 340;
        elemTable.setLayoutData(elemTableData);
        setElemTableItem(elemTable, rootInfo.getConflictInfo());

        addHorizontalLine();

        MergePoint mPoint = rootInfo.getMergePoint();
        int s_point = mPoint.getMergePoint(MergeType.SRC);
        UnifiedOperation s_op = srcInfo.getProjectInfo().getOperation(s_point);

        int d_point = mPoint.getMergePoint(MergeType.DEST);
        UnifiedOperation d_op = destInfo.getProjectInfo().getOperation(d_point);

        StringBuilder sb2 = new StringBuilder();
        sb2.append("Artificial Merge Result\n");
        sb2.append(srcInfo.getName()).append(":");
        sb2.append(createTextOpRepresentation(s_op)).append("\n");

        sb2.append(destInfo.getName()).append(":");
        sb2.append(createTextOpRepresentation(d_op)).append("\n");
        Label mergeLabel = new Label(dialog, SWT.NONE);
        mergeLabel.setText(sb2.toString());
        mergeLabel.setLayoutData(grabExFILLGridData());

        addHorizontalLine();

        Button okButton = new Button(dialog, SWT.PUSH);

        okButton.setText("OK");
        GridData okButtonGrid = new GridData(GridData.HORIZONTAL_ALIGN_END);
        okButtonGrid.widthHint = buttonWidth;
        okButton.setLayoutData(okButtonGrid);
        okButton.setEnabled(true);
        okButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                nextProgress();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
            }
        });
    }

    @Override
    protected String getDialogTitle() {
        return TITLE;
    }

    private void setElemTableItem(Table elemTable, ConflictInfo cInfo) {
        TableItem item;

        Map<ElementSlice, ElementSlice> confMap = cInfo.getConflictSliceMap();
        for (Entry<ElementSlice, ElementSlice> entry : confMap.entrySet()) {
            ElementSlice s_slice = entry.getKey();
            ElementSlice d_slice = entry.getValue();

            UnifiedOperation s_op = s_slice.getOperations().get(0);
            UnifiedOperation d_op = d_slice.getOperations().get(0);

            item = new TableItem(elemTable, SWT.NONE);
            item.setText(0, s_slice.getFullName());
            item.setText(1, createTextOpRepresentation(s_op));
            item.setText(2, createTextOpRepresentation(d_op));
        }
    }

    private String createTextOpRepresentation(UnifiedOperation op) {
        StringBuilder sb = new StringBuilder();
        sb.append(op.getId()).append(" [");
        sb.append(Time.toUsefulFormat(op.getTime())).append("]");
        return sb.toString();
    }
}