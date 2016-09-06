package fse.eclipse.mergehelper.detector.merge;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import org.jtool.changerepository.operation.CodeInsertedOperation;
import org.jtool.changerepository.operation.UnifiedOperation;

import fse.eclipse.mergehelper.Activator;
import fse.eclipse.mergehelper.detector.AbstractDetector;
import fse.eclipse.mergehelper.detector.BranchHistoryCopier;
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

    private boolean isSuccess;
    private List<MergeIndexHolder> mergeIdxHoloders = new ArrayList<>();

    private AMerge() {
    }

    public static AbstractDetector getInstance() {
        return instance;
    }

    @Override
    public void execute() {
        BranchRootInfo rootInfo = BranchRootInfo.getInstance();
        BranchInfo a_bInfo = rootInfo.getBranchInfo(MergeType.ACCEPT);
        BranchInfo j_bInfo = rootInfo.getBranchInfo(MergeType.JOIN);
        MergePoint mPoint = rootInfo.getMergePoint();

        Git git = init(rootInfo);
        try {
            commit(git, mPoint.getMergePoint(MergeType.ACCEPT), a_bInfo);
            commit(git, mPoint.getMergePoint(MergeType.JOIN), j_bInfo);

            Ref ref = git.getRepository().getRef(a_bInfo.getName());
            isSuccess = git.merge().include(ref).call().getMergeStatus().isSuccessful();
            if (isSuccess) {
                insertAMergeOperation(a_bInfo);
                insertAMergeOperation(j_bInfo);
            }
        } catch (GitAPIException | IOException e) {
            e.printStackTrace();
        }
    }

    private Git init(BranchRootInfo rootInfo) {
        File dir = new File(Activator.getWorkingDirPath());
        try {
            Git.init().setDirectory(dir).call();
            Git git = Git.open(dir);

            BranchInfo a_bInfo = rootInfo.getBranchInfo(MergeType.ACCEPT);
            String branchName = a_bInfo.getName();

            ProjectInfo a_pInfo = a_bInfo.getProjectInfo();
            List<FileInfo> fInfos = a_pInfo.getAllFileInfo();
            for (FileInfo fInfo : fInfos) {
                String filePath = createMergePath(fInfo, branchName);
                FileStream.write(filePath, fInfo.getCode(0));
            }

            git.add().addFilepattern(rootInfo.getName()).call();
            git.commit().setMessage("MergeHelper-init").call();

            // master
            String nowBranchName = git.getRepository().getBranch();

            String a_branchName = rootInfo.getBranchName(MergeType.ACCEPT);
            String j_branchName = rootInfo.getBranchName(MergeType.JOIN);

            if (!nowBranchName.equals(a_branchName)) {
                git.branchCreate().setName(a_branchName).call();
            }

            if (!nowBranchName.equals(j_branchName)) {
                git.branchCreate().setName(j_branchName).call();
            }
            return git;
        } catch (IllegalStateException | GitAPIException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void commit(Git git, UnifiedOperation op, BranchInfo bInfo)
            throws RefAlreadyExistsException, RefNotFoundException, InvalidRefNameException, CheckoutConflictException, GitAPIException {
        String branchName = bInfo.getName();
        git.checkout().setName(branchName).call();

        List<FileInfo> fInfos = bInfo.getProjectInfo().getAllFileInfo();
        for (FileInfo fInfo : fInfos) {
            String filePath = createMergePath(fInfo, branchName);
            int idx;
            if (op.getFileInfo().equals(fInfo)) {
                idx = fInfo.indexOfOperation(op.getId());
            } else {
                idx = RepositoryElementInfoUtil.indexOfJustBeforeFileOperation(fInfo, op.getTime());
            }
            String code = fInfo.getCode(idx);
            FileStream.write(filePath, code);
            mergeIdxHoloders.add(new MergeIndexHolder(fInfo, idx));
        }

        git.add().addFilepattern(BranchRootInfo.getInstance().getName()).call();
        git.commit().setMessage("MergeHelper-" + branchName).call();
    }

    private void insertAMergeOperation(BranchInfo bInfo) {
        String branchName = bInfo.getName();
        List<FileInfo> fInfos = bInfo.getProjectInfo().getAllFileInfo();
        for (FileInfo fInfo : fInfos) {
            String filePath = createMergePath(fInfo, branchName);
            String code = FileStream.read(filePath);

            int size = mergeIdxHoloders.size();
            for (int i = 0; i < size; i++) {
                MergeIndexHolder holder = mergeIdxHoloders.get(i);
                if (holder.equals(fInfo)) {
                    int insIdx = holder.idx + 1;
                    CodeInsertedOperation ciop = new CodeInsertedOperation(fInfo, code);
                    List<UnifiedOperation> ops = fInfo.getOperations();
                    ops.add(insIdx, ciop);

                    bInfo.getBranchFileInfo(fInfo).setMergedResult(insIdx, code);

                    mergeIdxHoloders.remove(i);
                    break;
                }
            }
        }
    }

    private String createMergePath(FileInfo fInfo, String branchName) {
        String filePath = fInfo.getFilePath();
        int idx = filePath.indexOf(BranchHistoryCopier.BRANCH_NAME_MARK);
        int idx2 = idx + BranchHistoryCopier.BRANCH_NAME_MARK.length() + branchName.length();

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
        if (isSuccess) {
            AMergedAdjustOperationOffset.getInstance().detect(dialog);
        } else {
            error(dialog);
        }
    }

    private class MergeIndexHolder {
        String name;
        int idx;

        MergeIndexHolder(FileInfo fInfo, int idx) {
            name = fInfo.getQualifiedName();
            this.idx = idx;
        }

        boolean equals(FileInfo fInfo) {
            return name.equals(fInfo.getQualifiedName());
        }
    }
}