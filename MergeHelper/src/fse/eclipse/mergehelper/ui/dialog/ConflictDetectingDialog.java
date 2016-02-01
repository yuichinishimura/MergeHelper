package fse.eclipse.mergehelper.ui.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;

import fse.eclipse.mergehelper.detecter.AbstractDetector;
import fse.eclipse.mergehelper.detecter.IDetectorState;
import fse.eclipse.mergehelper.detecter.InitWorkingDirectory;
import fse.eclipse.mergehelper.ui.MH_PackageExplorerView;

public class ConflictDetectingDialog extends AbstractUIDialog {

    private static final String TITLE = "Please Wait";

    private Label pLabel;
    private ProgressBar pBar;

    ConflictDetectingDialog(Composite parent) {
        super(parent);
    }

    @Override
    protected void execute() {
        AbstractDetector detector = InitWorkingDirectory.getInstance();
        detector.detect(this);
    }

    @Override
    protected void nextProgress() {
        MH_PackageExplorerView.getInstance().refresh();

        ResultDialog nextDialog = new ResultDialog(parent);
        nextDialog.show();

        finish();
    }

    @Override
    protected void createDialog() {
        dialog.setLayout(new GridLayout(1, false));

        pLabel = new Label(dialog, SWT.NONE);
        pLabel.setText("                                                                                                      ");
        pLabel.setSize(500, 100);
        pLabel.setLayoutData(grabExFILLGridData());

        pBar = new ProgressBar(dialog, SWT.SMOOTH);
        pBar.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        pBar.setMinimum(0);
        pBar.setMaximum(IDetectorState.STATE_NUMBER);
        pBar.setSelection(0);
    }

    @Override
    protected String getDialogTitle() {
        return TITLE;
    }

    public void setMessage(String message) {
        pLabel.setText(message);
    }

    public void incrementProgressBar() {
        pBar.setSelection(pBar.getSelection() + 1);
    }

    public void detectSuccess() {
        nextProgress();
    }

    public void detectFailed(String errorMessage) {
        ErrorDialog nextDialog = new ErrorDialog(parent, errorMessage);
        nextDialog.show();
        
        finish();
    }
}
