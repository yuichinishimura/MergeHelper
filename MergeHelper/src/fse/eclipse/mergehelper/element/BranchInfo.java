package fse.eclipse.mergehelper.element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jtool.changerepository.data.FileInfo;
import org.jtool.changerepository.data.ProjectInfo;
import org.jtool.changerepository.parser.OpJavaElement;

public class BranchInfo {
    private final String name;
    private final MergeType type;
    private final Map<String, BranchFileInfo> bfInfoMap = new ConcurrentHashMap<>();

    private ProjectInfo pInfo;

    BranchInfo(String name, MergeType type) {
        this.name = name;
        this.type = type;
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
        return new ArrayList<>(bfInfoMap.values());
    }

    public BranchFileInfo getBranchFileInfo(FileInfo fInfo) {
        return bfInfoMap.get(fInfo.getQualifiedName());
    }

    public BranchFileInfo createBranchFileInfo(FileInfo fInfo, List<OpJavaElement> elems) {
        BranchFileInfo bfInfo = new BranchFileInfo(fInfo, elems);
        bfInfoMap.put(fInfo.getQualifiedName(), bfInfo);
        return bfInfo;
    }
    
    public List<BranchJavaElement> getAllBranchJavaElement(){
        List<BranchJavaElement> elems = new ArrayList<>();
        for(BranchFileInfo bfInfo : bfInfoMap.values()){
            elems.addAll(bfInfo.getAllBranchJavaElement());
        }
        return elems;
    }
}
