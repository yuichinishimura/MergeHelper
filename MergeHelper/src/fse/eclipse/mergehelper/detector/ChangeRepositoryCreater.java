package fse.eclipse.mergehelper.detector;

import java.util.List;

import org.jtool.changerepository.data.ProjectInfo;
import org.jtool.changerepository.data.RepositoryManager;
import org.jtool.changerepository.data.WorkspaceInfo;

import fse.eclipse.mergehelper.Activator;
import fse.eclipse.mergehelper.element.BranchInfo;
import fse.eclipse.mergehelper.element.BranchRootInfo;
import fse.eclipse.mergehelper.element.MergeType;
import fse.eclipse.mergehelper.ui.dialog.ConflictDetectingDialog;
import fse.eclipse.mergehelper.util.RepositoryElementInfoUtil;

public class ChangeRepositoryCreater extends AbstractDetector {
    private static final String MESSAGE = "Create Change Operation Repository ...";
    private static final String ERROR_MESSAGE = "NULL";
    private static AbstractDetector instance = new ChangeRepositoryCreater();

    private ChangeRepositoryCreater() {
    }

    public static AbstractDetector getInstance() {
        return instance;
    }

    @Override
    protected void execute() {
        RepositoryManager rManager = RepositoryManager.getInstance();
        rManager.collectOperationsInRepository(Activator.getWorkingDirPath());
        WorkspaceInfo wInfo = rManager.getWorkspaceInfo();

        BranchRootInfo rootInfo = BranchRootInfo.getInstance();
        BranchInfo a_bInfo = rootInfo.getBranchInfo(MergeType.ACCEPT);
        BranchInfo j_bInfo = rootInfo.getBranchInfo(MergeType.JOIN);

        List<ProjectInfo> pInfos = wInfo.getAllProjectInfo();
        for (ProjectInfo pInfo : pInfos) {
            String name = RepositoryElementInfoUtil.getBranchName(pInfo);
            if (name == null) {
                continue;
            }

            if (name.equals(a_bInfo.getName())) {
                a_bInfo.setProjectInfo(pInfo);
            } else if (name.equals(j_bInfo.getName())) {
                j_bInfo.setProjectInfo(pInfo);
            } else {
                System.err.println("not found project name: '" + pInfo.getQualifiedName() + "'");
            }
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
        OperationAllocater.getInstace().detect(dialog);
    }
}
