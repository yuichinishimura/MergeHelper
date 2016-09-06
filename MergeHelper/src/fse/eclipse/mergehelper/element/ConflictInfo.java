package fse.eclipse.mergehelper.element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jtool.changerecorder.util.StringComparator;
import org.jtool.changerepository.data.FileInfo;
import org.jtool.changerepository.data.PackageInfo;

public class ConflictInfo {
    private final Map<BranchJavaElement, BranchJavaElement> elemMap = new ConcurrentHashMap<>();

    public ConflictInfo() {
    }

    public Map<BranchJavaElement, BranchJavaElement> getConflictElementMap() {
        return elemMap;
    }

    public List<BranchJavaElement> getAllBranchJavaElement(MergeType type) {
        if (MergeType.isAccept(type)) {
            return new ArrayList<>(elemMap.keySet());
        } else {
            return new ArrayList<>(elemMap.values());
        }
    }

    public void addConflictElements(BranchJavaElement a_elem, BranchJavaElement j_elem) {
        elemMap.put(a_elem, j_elem);
    }

    public boolean isConflict() {
        return elemMap.size() > 0;
    }

    public boolean isConflictPackage(PackageInfo pInfo) {
        String pName = pInfo.getName();
        for (BranchJavaElement elem : elemMap.keySet()) {
            if (StringComparator.isSame(pName, elem.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    public boolean isConflictFile(FileInfo fInfo) {
        String fName = fInfo.getName();
        for (BranchJavaElement elem : elemMap.keySet()) {
            if (StringComparator.isSame(fName, elem.getFileName())) {
                return true;
            }
        }
        return false;
    }

    public boolean isConflictElement(BranchJavaElement element) {
        for (BranchJavaElement elem : elemMap.keySet()) {
            if (element.equalsFileElement(elem)) {
                return true;
            }
        }
        return false;
    }
}
