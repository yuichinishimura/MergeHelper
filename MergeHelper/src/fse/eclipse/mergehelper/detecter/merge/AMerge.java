package fse.eclipse.mergehelper.detecter.merge;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.Ref;
import org.jtool.changerecorder.util.FileStream;
import org.jtool.changerepository.data.FileInfo;
import org.jtool.changerepository.data.ProjectInfo;

import fse.eclipse.mergehelper.Activator;
import fse.eclipse.mergehelper.Attr;
import fse.eclipse.mergehelper.detecter.AbstractDetector;
import fse.eclipse.mergehelper.element.BranchInfo;
import fse.eclipse.mergehelper.element.BranchRootInfo;
import fse.eclipse.mergehelper.element.MergePoint;
import fse.eclipse.mergehelper.element.MergeType;
import fse.eclipse.mergehelper.ui.dialog.ConflictDetectingDialog;
import fse.eclipse.mergehelper.util.RepositoryElementInfoUtil;

public class AMerge extends AbstractDetector {

    private static final String MESSAGE = "Do Artificial Merge ...";
    private static final String ERROR_MESSAGE = "Artificial Merge Failed";
    private static AbstractDetector instance = new AMerge();

    private boolean isMergeSuccess;

    private AMerge() {
    }

    public static AbstractDetector getInstance() {
        return instance;
    }

    @Override
    public void execute(ConflictDetectingDialog dialog) {
        BranchRootInfo rootInfo = BranchRootInfo.getInstance();
        MergePoint mPoint = rootInfo.getMergePoint();

        Git git = init(rootInfo);
        try {
            commit(git, mPoint.getMergePoint(MergeType.SRC), rootInfo.getBranchInfo(MergeType.SRC));
            commit(git, mPoint.getMergePoint(MergeType.DEST), rootInfo.getBranchInfo(MergeType.DEST));

            Ref ref = git.getRepository().getRef(rootInfo.getBranchInfo(MergeType.SRC).getName());
            isMergeSuccess = git.merge().include(ref).call().getMergeStatus().isSuccessful();
        } catch (GitAPIException | IOException e) {
            e.printStackTrace();
        }
    }

    private Git init(BranchRootInfo rootInfo) {
        File dir = new File(Activator.getWorkingDirPath());
        try {
            Git.init().setDirectory(dir).call();
            Git git = Git.open(dir);

            BranchInfo srcInfo = rootInfo.getBranchInfo(MergeType.SRC);
            String branchName = srcInfo.getName();

            ProjectInfo s_pInfo = srcInfo.getProjectInfo();
            List<FileInfo> fInfos = s_pInfo.getAllFileInfo();
            for (FileInfo fInfo : fInfos) {
                String filePath = createMergePath(fInfo, branchName);
                FileStream.write(filePath, fInfo.getCode(0));
            }

            git.add().addFilepattern(rootInfo.getName()).call();
            git.commit().setMessage("MergeHelper-init").call();

            // master
            String nowBranchName = git.getRepository().getBranch();

            String s_BranchName = rootInfo.getBranchName(MergeType.SRC);
            String d_BranchName = rootInfo.getBranchName(MergeType.DEST);

            if (!nowBranchName.equals(s_BranchName)) {
                git.branchCreate().setName(s_BranchName).call();
            }

            if (!nowBranchName.equals(d_BranchName)) {
                git.branchCreate().setName(d_BranchName).call();
            }

            return git;
        } catch (IllegalStateException | GitAPIException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void commit(Git git, int point, BranchInfo bInfo) throws RefAlreadyExistsException, RefNotFoundException, InvalidRefNameException,
            CheckoutConflictException, GitAPIException {
        String branchName = bInfo.getName();
        git.checkout().setName(branchName).call();

        ProjectInfo pInfo = bInfo.getProjectInfo();
        List<FileInfo> fInfos = pInfo.getAllFileInfo();
        for (FileInfo fInfo : fInfos) {
            String filePath = createMergePath(fInfo, branchName);
            int id = RepositoryElementInfoUtil.getJustBeforeFileId(fInfo, point);
            FileStream.write(filePath, fInfo.getCode(id));
        }

        git.add().addFilepattern(BranchRootInfo.getInstance().getName()).call();
        git.commit().setMessage("MergeHelper-" + branchName).call();
    }

    public static String createMergePath(FileInfo fInfo, String branchName) {
        String filePath = fInfo.getFilePath();
        int idx = filePath.indexOf(Attr.BRANCH_NAME_MARK);
        int idx2 = idx + Attr.BRANCH_NAME_MARK.length() + branchName.length();

        StringBuilder sb = new StringBuilder(Activator.getWorkingDirPath());
        sb.append(filePath.substring(0, idx)).append(filePath.substring(idx2));
        return sb.toString();
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
        if (isMergeSuccess) {
            AMergeCorrespondOperation.getInstance().detect(dialog);
        } else {
            error(dialog);
        }
    }
}