package fse.eclipse.mergehelper.element;

import java.util.ArrayList;
import java.util.List;

import org.jtool.changerepository.data.FileInfo;
import org.jtool.changerepository.data.ProjectInfo;

public class BranchInfo {

    private final String name;
    private final MergeType type;
    private final List<BranchFileInfo> bfInfos;

    private ProjectInfo pInfo;

    BranchInfo(String name, MergeType type) {
        this.name = name;
        this.type = type;

        bfInfos = new ArrayList<BranchFileInfo>();
    }

    public String getName() {
        return name;
    }

    public MergeType getType() {
        return type;
    }

    public ProjectInfo getProjectInfo() {
        return pInfo;
    }

    public void setProjectInfo(ProjectInfo pInfo) {
        this.pInfo = pInfo;
    }

    public List<BranchFileInfo> getAllBranchFileInfo() {
        return bfInfos;
    }

    public BranchFileInfo getBranchFileInfo(FileInfo fInfo) {
        for (BranchFileInfo bfInfo : bfInfos) {
            if (bfInfo.equals(fInfo)) {
                return bfInfo;
            }
        }
        return null;
    }

    public void addBranchFileInfo(FileInfo fInfo, List<ElementSlice> slices) {
        BranchFileInfo bfInfo = new BranchFileInfo(fInfo, slices);
        bfInfos.add(bfInfo);
    }

    public List<ElementSlice> getAllSlice() {
        List<ElementSlice> slices = new ArrayList<ElementSlice>();
        for (BranchFileInfo bfInfo : bfInfos) {
            slices.addAll(bfInfo.getAllSlice());
        }
        return slices;
    }
}
