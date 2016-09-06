package fse.eclipse.mergehelper.detector;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import fse.eclipse.mergehelper.Activator;
import fse.eclipse.mergehelper.ui.dialog.ConflictDetectingDialog;

public class InitWorkingDirectory extends AbstractDetector {
    private static final String MESSAGE = "Clean Working Directory ...";
    private static final String ERROR_MESSAGE = "NULL";
    private static AbstractDetector instance = new InitWorkingDirectory();

    private InitWorkingDirectory() {
    }

    public static AbstractDetector getInstance() {
        return instance;
    }

    @Override
    public void execute() {
        File dir = Activator.getWorkingDir();
        if (dir.exists()) {
            try {
                FileUtils.cleanDirectory(dir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            dir.mkdirs();
        }
    }

    @Override
    protected String getMessage() {
        return MESSAGE;
    }

    @Override
    protected String getErrorMessage() {
        return ERROR_MESSAGE;
    }

    @Override
    protected void nextState(ConflictDetectingDialog dialog) {
        BranchHistoryCopier.getInstance().detect(dialog);
    }
}
