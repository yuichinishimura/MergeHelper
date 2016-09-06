package fse.eclipse.mergehelper.detector;

import fse.eclipse.mergehelper.ui.dialog.ConflictDetectingDialog;

public abstract class AbstractDetector {

    protected abstract String getMessage();

    protected abstract String getErrorMessage();

    protected abstract void execute();

    protected abstract void nextState(ConflictDetectingDialog dialog);

    public void detect(ConflictDetectingDialog dialog) {
        setMessage(dialog);

        execute();

        dialog.incrementProgressBar();

        nextState(dialog);
    }

    private void setMessage(ConflictDetectingDialog dialog) {
        String message = getMessage();
        System.out.println(message);
        dialog.setMessage(message);
    }

    protected void finish(ConflictDetectingDialog dialog) {
        dialog.detectSuccess();
    }

    protected void error(ConflictDetectingDialog dialog) {
        String errorMessage = getErrorMessage();
        System.err.println(errorMessage);
        dialog.detectFailed(errorMessage);
    }
}
