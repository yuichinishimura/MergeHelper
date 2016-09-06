package fse.eclipse.mergehelper.ui.dialog;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Composite;

import fse.eclipse.mergehelper.detector.AbstractDetector;
import fse.eclipse.mergehelper.detector.InitWorkingDirectory;
import fse.eclipse.mergehelper.ui.MH_PackageExplorerView;

public class ConflictDetectingDialog extends AbstractMHDialog implements IRunnableWithProgress {
    private static final String TITLE = "Please Wait";
    private IProgressMonitor monitor;

    ConflictDetectingDialog(Composite parent) {
        super(parent);
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        this.monitor = monitor;
        monitor.beginTask("", 7);
        monitor.setCanceled(false);

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
        ProgressMonitorDialog pDialog = new ProgressMonitorDialog(null);
        pDialog.open();

        dialog = pDialog.getShell();
        dialog.setText(TITLE);
        dialog.update();

        try {
            pDialog.run(false, true, this);
        } catch (InvocationTargetException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String getDialogTitle() {
        return TITLE;
    }

    public void setMessage(String message) {
        monitor.setTaskName(message);
    }

    public void incrementProgressBar() {
        monitor.worked(1);
    }

    @Override
    protected void open() {
        // no execute
    }

    public void detectSuccess() {
        monitor.done();
        nextProgress();
    }

    public void detectFailed(String errorMessage) {
        ErrorDialog nextDialog = new ErrorDialog(parent, errorMessage);
        nextDialog.show();

        finish();
    }
}
