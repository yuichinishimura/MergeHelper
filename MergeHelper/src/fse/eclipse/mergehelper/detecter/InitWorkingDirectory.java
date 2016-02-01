package fse.eclipse.mergehelper.detecter;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import fse.eclipse.mergehelper.Activator;
import fse.eclipse.mergehelper.ui.dialog.ConflictDetectingDialog;

public class InitWorkingDirectory extends AbstractDetector {

    private static final String MESSAGE = "Clean Working Directory ...";
    private static AbstractDetector instance = new InitWorkingDirectory();

    private InitWorkingDirectory() {
    }

    public static AbstractDetector getInstance() {
        return instance;
    }

    @Override
    public void execute(ConflictDetectingDialog dialog) {
        File dir = Activator.getWorkingDir();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try {
            FileUtils.cleanDirectory(dir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String getMessage() {
        return MESSAGE;
    }

    @Override
    protected String getErrorMessage() {
        return null;
    }

    @Override
    protected void nextState(ConflictDetectingDialog dialog) {
        BranchHistoryCopier.getInstance().detect(dialog);
    }
}
