package fse.eclipse.mergehelper.element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jtool.changerepository.data.FileInfo;
import org.jtool.changerepository.data.PackageInfo;

import fse.eclipse.mergehelper.util.RepositoryElementInfoUtil;

public class ConflictInfo {

    private final Map<ElementSlice, ElementSlice> elemMap;

    public ConflictInfo() {
        elemMap = new HashMap<ElementSlice, ElementSlice>();
    }

    public Map<ElementSlice, ElementSlice> getConflictSliceMap() {
        return Collections.unmodifiableMap(elemMap);
    }

    public List<String> getAllConflictElement() {
        List<String> elemNames = new ArrayList<String>();
        for (Entry<ElementSlice, ElementSlice> entry : elemMap.entrySet()) {
            elemNames.add(entry.getKey().getName());
        }
        return Collections.unmodifiableList(elemNames);
    }

    public void addConflictElements(ElementSlice sslice, ElementSlice dslice) {
        elemMap.put(sslice, dslice);
    }

    public boolean isConflict() {
        return elemMap.size() > 0;
    }

    public boolean isConflictPackage(PackageInfo paInfo) {
        String paName = paInfo.getName();
        for (Entry<ElementSlice, ElementSlice> entry : elemMap.entrySet()) {
            if (entry.getKey().getPackageName().equals(paName)) {
                return true;
            }
        }
        return false;
    }

    public boolean isConflictFile(FileInfo fInfo) {
        String fName = RepositoryElementInfoUtil.getFileNameExceptExtension(fInfo);
        String paName = fInfo.getPackageInfo().getName();
        for (Entry<ElementSlice, ElementSlice> entry : elemMap.entrySet()) {
            if (entry.getKey().getPackageName().equals(paName)) {
                if (entry.getKey().getFileName().equals(fName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isConflictElement(String elemName) {
        for (Entry<ElementSlice, ElementSlice> entry : elemMap.entrySet()) {
            if (entry.getKey().getName().equals(elemName)) {
                return true;
            }
        }
        return false;
    }
}
