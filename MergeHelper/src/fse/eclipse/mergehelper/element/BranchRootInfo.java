package fse.eclipse.mergehelper.element;

import org.eclipse.core.resources.IProject;
import org.jtool.changerepository.data.FileInfo;

import fse.eclipse.mergehelper.util.RepositoryElementInfoUtil;

public class BranchRootInfo {

    private static BranchRootInfo instance;

    private final IProject project;
    private final BranchInfo srcInfo, destInfo;

    private ConflictInfo cInfo;
    private MergePoint mPoint;

    private BranchRootInfo(IProject project, String srcName, String destName) {
        this.project = project;
        srcInfo = new BranchInfo(srcName, MergeType.SRC);
        destInfo = new BranchInfo(destName, MergeType.DEST);
    }

    public static BranchRootInfo create(IProject project, String srcName, String destName) {
        instance = new BranchRootInfo(project, srcName, destName);
        return instance;
    }

    public static BranchRootInfo getInstance() {
        if (instance == null) {
            System.err.println("GitProjectInfo instance is Null");
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
        if (MergeType.isMergeSrc(type)) {
            return srcInfo;
        } else {
            return destInfo;
        }
    }

    public String getBranchName(MergeType type) {
        return getBranchInfo(type).getName();
    }

    public MergeType getType(String branchName) {
        if (srcInfo.getName().equals(branchName)) {
            return MergeType.SRC;
        } else if (destInfo.getName().equals(branchName)) {
            return MergeType.DEST;
        } else {
            return null;
        }
    }

    public BranchFileInfo getBranchFileInfo(FileInfo fInfo) {
        String branchName = RepositoryElementInfoUtil.getBranchName(fInfo);
        if (srcInfo.getName().equals(branchName)) {
            return srcInfo.getBranchFileInfo(fInfo);
        } else if (destInfo.getName().equals(branchName)) {
            return destInfo.getBranchFileInfo(fInfo);
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
