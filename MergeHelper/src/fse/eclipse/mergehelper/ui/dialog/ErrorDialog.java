package fse.eclipse.mergehelper.ui.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import fse.eclipse.mergehelper.element.BranchInfo;
import fse.eclipse.mergehelper.element.BranchRootInfo;
import fse.eclipse.mergehelper.element.ConflictInfo;
import fse.eclipse.mergehelper.element.ElementSlice;
import fse.eclipse.mergehelper.element.MergePoint;
import fse.eclipse.mergehelper.element.MergeType;

public class ErrorDialog extends AbstractUIDialog {

    private static final String TITLE = "Detecion Failed";
    private final String errorMessage;

    ErrorDialog(Composite parent, String errorMessage) {
        super(parent);
        this.errorMessage = errorMessage;
    }

    @Override
    protected void nextProgress() {
        finish();
    }

    @Override
    protected void createDialog() {
        dialog.setLayout(new GridLayout(1, false));

        Color color = Display.getCurrent().getSystemColor(SWT.COLOR_RED);

        Label messageLabel = new Label(dialog, SWT.NONE);
        messageLabel.setText(errorMessage);
        messageLabel.setForeground(color);
        messageLabel.setLayoutData(grabExFILLGridData());

        addHorizontalLine();

        Label detailLabel = new Label(dialog, SWT.NONE);
        detailLabel.setText(createDetailText());
        detailLabel.setLayoutData(grabExFILLGridData());

        addHorizontalLine();

        Button okButton = new Button(dialog, SWT.PUSH);
        okButton.setText("OK");
        GridData okButtonGrid = new GridData(GridData.HORIZONTAL_ALIGN_END);
        okButtonGrid.widthHint = buttonWidth;
        okButton.setLayoutData(okButtonGrid);
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

    private String createDetailText() {
        BranchRootInfo rootInfo = BranchRootInfo.getInstance();
        BranchInfo acceptInfo = rootInfo.getBranchInfo(MergeType.ACCEPT);
        BranchInfo joinInfo = rootInfo.getBranchInfo(MergeType.JOIN);

        String projectName = rootInfo.getName();
        String acceptName = acceptInfo.getName();
        String joinName = joinInfo.getName();

        StringBuilder sb = new StringBuilder();
        sb.append("Project: ").append(projectName).append("\n");
        sb.append("Branch: ").append(acceptName).append(" , ").append(joinName).append("\n");

        sb.append("\n");

        sb.append(acceptName).append("-Slice:\n");
        for (ElementSlice slice : acceptInfo.getAllSlice()) {
            sb.append(slice).append("\n");
        }
        sb.append("\n");
        sb.append(joinName).append("-Slice\n");
        for (ElementSlice slice : joinInfo.getAllSlice()) {
            sb.append(slice).append("\n");
        }
        sb.append("\n");

        ConflictInfo cInfo = rootInfo.getConflictInfo();
        if (cInfo == null || cInfo.isConflict()) {
            sb.append("Conflict Element is NULL");
            return sb.toString();
        }

        sb.append("Conflict Element:\n");
        for (String elemName : cInfo.getAllConflictElement()) {
            sb.append(elemName).append(",");
        }
        sb.delete(sb.length() - 1, sb.length());
        sb.append("\n\n");

        MergePoint mPoint = rootInfo.getMergePoint();
        if (mPoint == null) {
            sb.append("Merge Point is NULL");
            return sb.toString();
        }

        sb.append("Merge Point: ").append(acceptName).append("-").append(mPoint.getMergePoint(MergeType.ACCEPT));
        sb.append(" , ").append(joinName).append("-").append(mPoint.getMergePoint(MergeType.JOIN));
        sb.append("\n");
        sb.append("Target Element: ").append(mPoint.getTargetElement());
        return sb.toString();
    }
}