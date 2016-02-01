package fse.eclipse.mergehelper.detecter;

import fse.eclipse.mergehelper.ui.dialog.ConflictDetectingDialog;

public abstract class AbstractDetector implements IDetectorState {

    protected abstract String getMessage();

    protected abstract String getErrorMessage();

    protected abstract void nextState(ConflictDetectingDialog dialog);

    public void detect(ConflictDetectingDialog dialog) {
        setMessage(dialog);

        execute(dialog);

        incrementProgressBar(dialog);

        nextState(dialog);
    }

    private void setMessage(ConflictDetectingDialog dialog) {
        String message = getMessage();
        System.out.println(message);
        dialog.setMessage(message);
    }

    private void incrementProgressBar(ConflictDetectingDialog dialog) {
        dialog.incrementProgressBar();
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
