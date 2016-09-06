package fse.eclipse.mergehelper.element;

import org.eclipse.core.resources.IProject;
import org.jtool.changerepository.data.FileInfo;

import fse.eclipse.mergehelper.util.RepositoryElementInfoUtil;

public class BranchRootInfo {
    private static BranchRootInfo instance;
    private final BranchInfo acceptInfo, joinInfo;
    private final IProject project;

    private ConflictInfo cInfo;
    private MergePoint mPoint;

    private BranchRootInfo(IProject project, String acceptName, String joinName) {
        this.project = project;
        acceptInfo = new BranchInfo(acceptName, MergeType.ACCEPT);
        joinInfo = new BranchInfo(joinName, MergeType.JOIN);
    }

    public static BranchRootInfo create(IProject project, String acceptName, String joinName) {
        instance = new BranchRootInfo(project, acceptName, joinName);
        return instance;
    }

    public static BranchRootInfo getInstance() {
        if (instance == null) {
            System.err.println("BranchRootInfo instance is Null");
        }
        return instance;
    }

    public IProject getProject() {
        return project;
    }

    public String getName() {
        return project.getName();
    }

    public BranchInfo getBranchInfo(MergeType type) {
        if (MergeType.isAccept(type)) {
            return acceptInfo;
        } else {
            return joinInfo;
        }
    }

    public String getBranchName(MergeType type) {
        return getBranchInfo(type).getName();
    }

    public MergeType getType(String branchName) {
        if (acceptInfo.getName().equals(branchName)) {
            return MergeType.ACCEPT;
        } else if (joinInfo.getName().equals(branchName)) {
            return MergeType.JOIN;
        } else {
            return null;
        }
    }

    public BranchFileInfo getBranchFileInfo(FileInfo fInfo) {
        String branchName = RepositoryElementInfoUtil.getBranchName(fInfo);
        if (acceptInfo.getName().equals(branchName)) {
            return acceptInfo.getBranchFileInfo(fInfo);
        } else if (joinInfo.getName().equals(branchName)) {
            return joinInfo.getBranchFileInfo(fInfo);
        } else {
            return null;
        }
    }

    public ConflictInfo getConflictInfo() {
        return cInfo;
    }

    public void setConflictInfo(ConflictInfo cInfo) {
        this.cInfo = cInfo;
    }

    public MergePoint getMergePoint() {
        return mPoint;
    }

    public void setMergePoint(MergePoint mPoint) {
        this.mPoint = mPoint;
    }
}
