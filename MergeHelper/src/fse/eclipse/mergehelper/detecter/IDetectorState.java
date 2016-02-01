package fse.eclipse.mergehelper.detecter;

import fse.eclipse.mergehelper.ui.dialog.ConflictDetectingDialog;

public interface IDetectorState {

    public static final int STATE_NUMBER = 7;

    public void execute(ConflictDetectingDialog dialog);
}
