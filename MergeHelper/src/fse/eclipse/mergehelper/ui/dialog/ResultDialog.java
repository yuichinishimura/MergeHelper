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
import fse.eclipse.mergehelper.element.BranchJavaElement;
import fse.eclipse.mergehelper.element.BranchRootInfo;
import fse.eclipse.mergehelper.element.ConflictInfo;
import fse.eclipse.mergehelper.element.MergePoint;
import fse.eclipse.mergehelper.element.MergeType;

public class ResultDialog extends AbstractMHDialog {
    private static final String TITLE = "Detecion Result";

    public ResultDialog(Composite parent) {
        super(parent);
    }

    @Override
    protected void nextProgress() {
        finish();
    }

    @Override
    protected void createDialog() {
        dialog.setLayout(new GridLayout(1, false));

        BranchRootInfo rootInfo = BranchRootInfo.getInstance();
        BranchInfo a_bInfo = rootInfo.getBranchInfo(MergeType.ACCEPT);
        BranchInfo j_bInfo = rootInfo.getBranchInfo(MergeType.JOIN);

        StringBuilder sb = new StringBuilder();
        sb.append("Total number of operations\n");
        sb.append(a_bInfo.getName()).append(":");
        sb.append(a_bInfo.getProjectInfo().getOperationNumber()).append("\n");

        sb.append(j_bInfo.getName()).append(":");
        sb.append(j_bInfo.getProjectInfo().getOperationNumber()).append("\n");

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

        TableColumn acceptPoint = new TableColumn(elemTable, SWT.LEFT);
        acceptPoint.setText(a_bInfo.getName());
        acceptPoint.setWidth(170);
        acceptPoint.setResizable(true);

        TableColumn joinPoint = new TableColumn(elemTable, SWT.LEFT);
        joinPoint.setText(j_bInfo.getName());
        joinPoint.setWidth(170);
        joinPoint.setResizable(true);

        GridData elemTableData = grabExFILLGridData();
        elemTableData.heightHint = tableHeight * 3 / 2;
        elemTableData.widthHint = 230 + 340;
        elemTable.setLayoutData(elemTableData);
        setElemTableItem(elemTable, rootInfo.getConflictInfo());

        addHorizontalLine();

        MergePoint mPoint = rootInfo.getMergePoint();
        if (mPoint != null) {
            UnifiedOperation a_op = mPoint.getMergePoint(MergeType.ACCEPT);
            UnifiedOperation j_op = mPoint.getMergePoint(MergeType.JOIN);

            StringBuilder sb2 = new StringBuilder();
            sb2.append("Artificial Merge Result\n");
            sb2.append(a_bInfo.getName()).append(":");
            sb2.append(createTextOpRepresentation(a_op)).append("\n");

            sb2.append(j_bInfo.getName()).append(":");
            sb2.append(createTextOpRepresentation(j_op)).append("\n");
            Label mergeLabel = new Label(dialog, SWT.NONE);
            mergeLabel.setText(sb2.toString());
            mergeLabel.setLayoutData(grabExFILLGridData());

            addHorizontalLine();
        }

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
        Map<BranchJavaElement, BranchJavaElement> confMap = cInfo.getConflictElementMap();
        for (Entry<BranchJavaElement, BranchJavaElement> entry : confMap.entrySet()) {
            BranchJavaElement a_elem = entry.getKey();
            BranchJavaElement j_elem = entry.getValue();

            UnifiedOperation a_op = a_elem.getOperations().get(0);
            UnifiedOperation j_op = j_elem.getOperations().get(0);

            TableItem item = new TableItem(elemTable, SWT.NONE);
            item.setText(0, a_elem.getFullName());
            item.setText(1, createTextOpRepresentation(a_op));
            item.setText(2, createTextOpRepresentation(j_op));
        }
    }

    private String createTextOpRepresentation(UnifiedOperation op) {
        StringBuilder sb = new StringBuilder();
        sb.append(op.indexOfProjectInfo()).append(" uid:");
        sb.append(op.getId()).append(" [");
        sb.append(Time.toUsefulFormat(op.getTime())).append("]");
        return sb.toString();
    }
}
