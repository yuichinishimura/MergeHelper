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
import fse.eclipse.mergehelper.element.BranchJavaElement;
import fse.eclipse.mergehelper.element.BranchRootInfo;
import fse.eclipse.mergehelper.element.ConflictInfo;
import fse.eclipse.mergehelper.element.MergePoint;
import fse.eclipse.mergehelper.element.MergeType;

public class ErrorDialog extends AbstractMHDialog {
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
        BranchInfo a_bInfo = rootInfo.getBranchInfo(MergeType.ACCEPT);
        BranchInfo j_bInfo = rootInfo.getBranchInfo(MergeType.JOIN);

        String projectName = rootInfo.getName();
        String acceptName = a_bInfo.getName();
        String joinName = j_bInfo.getName();

        StringBuilder sb = new StringBuilder();
        sb.append("Project: ").append(projectName).append("\n");
        sb.append("Branch: ").append(acceptName).append(" , ").append(joinName).append("\n");

        sb.append("\n");

        sb.append(acceptName).append("-Element:\n");
        for (BranchJavaElement elem : a_bInfo.getAllBranchJavaElement()) {
            sb.append(elem).append("\n");
        }
        sb.append("\n");
        sb.append(joinName).append("-Element\n");
        for (BranchJavaElement elem : j_bInfo.getAllBranchJavaElement()) {
            sb.append(elem).append("\n");
        }
        sb.append("\n");

        ConflictInfo cInfo = rootInfo.getConflictInfo();
        if (cInfo == null || cInfo.isConflict()) {
            sb.append("Conflict Element is NULL");
            return sb.toString();
        }

        sb.append("Conflict Element:\n");
        for (BranchJavaElement elem : cInfo.getAllBranchJavaElement(MergeType.ACCEPT)) {
            sb.append(elem.getFullName()).append(",");
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
        sb.append("Target Element: ").append(mPoint.getElement(MergeType.ACCEPT).getFullName());
        return sb.toString();
    }
}
