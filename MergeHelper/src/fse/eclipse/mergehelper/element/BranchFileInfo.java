package fse.eclipse.mergehelper.element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jtool.changerepository.data.FileInfo;
import org.jtool.changerepository.operation.UnifiedOperation;
import org.jtool.changerepository.parser.OpJavaElement;

import fse.eclipse.mergehelper.util.RepositoryElementInfoUtil;

public class BranchFileInfo {
    private final FileInfo fInfo;
    private final Map<String, BranchJavaElement> elemMap = new ConcurrentHashMap<>();
    private Map<Integer, String> mergedResultMap;

    public BranchFileInfo(FileInfo fInfo, List<OpJavaElement> elements) {
        this.fInfo = fInfo;
        for (OpJavaElement elem : elements) {
            elemMap.put(createElementMapKey(elem), new BranchJavaElement(this, elem));
        }
    }

    public String getName() {
        return RepositoryElementInfoUtil.getNameExcludeBranchName(fInfo.getName());
    }

    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        sb.append(fInfo.getPackageInfo().getName());
        sb.append("#").append(getName());
        return sb.toString();
    }

    public Map<String, BranchJavaElement> getElementMap() {
        return elemMap;
    }

    public List<String> getAllElementName() {
        return new ArrayList<>(elemMap.keySet());
    }

    public List<BranchJavaElement> getAllBranchJavaElement() {
        return new ArrayList<>(elemMap.values());
    }

    public String createElementMapKey(OpJavaElement elem) {
        return elem.getSimpleName();
    }

    public BranchJavaElement getBranchJavaElement(String key) {
        return elemMap.get(key);
    }

    public BranchJavaElement getBranchJavaElement(UnifiedOperation op) {
        List<BranchJavaElement> elems = getAllBranchJavaElement();
        for (BranchJavaElement elem : elems) {
            if (elem.contains(op)) {
                return elem;
            }
        }
        return null;
    }

    public BranchJavaElement getBranchJavaElement(OpJavaElement elem) {
        String key = createElementMapKey(elem);
        return getBranchJavaElement(key);
    }

    public void addOperation(OpJavaElement elem, UnifiedOperation op, String body) {
        String key = createElementMapKey(elem);
        BranchJavaElement bElem;
        if (elemMap.containsKey(key)) {
            bElem = elemMap.get(key);
            bElem.addOperation(op, body);
        }
    }

    public void removeBranchJavaElement(String key) {
        elemMap.remove(key);
    }

    public int getMergedOperationIdx() {
        return mergedResultMap.keySet().iterator().next();
    }

    public String getMergedResultCode() {
        return mergedResultMap.values().iterator().next();
    }

    public void setMergedResult(int mergeOperationIdx, String code) {
        mergedResultMap = Collections.singletonMap(mergeOperationIdx, code);
    }

    public boolean equalsFileName(BranchFileInfo bfInfo) {
        return this.getFullName().compareTo(bfInfo.getFullName()) == 0;
    }
}
