package fse.eclipse.mergehelper.element;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jtool.changerecorder.util.StringComparator;
import org.jtool.changerepository.operation.UnifiedOperation;
import org.jtool.changerepository.parser.OpJavaElement;

public class BranchJavaElement {
    private final BranchFileInfo bfInfo;
    private final OpJavaElement elem;
    private final Map<UnifiedOperation, String> bodyMap = new LinkedHashMap<>();

    BranchJavaElement(BranchFileInfo bfInfo, OpJavaElement elem) {
        this.bfInfo = bfInfo;
        this.elem = elem;
    }

    public BranchFileInfo getBranchFileInfo() {
        return bfInfo;
    }

    public OpJavaElement getElement() {
        return elem;
    }

    public String getName() {
        return elem.getName();
    }

    public String getSimpleName() {
        return elem.getSimpleName();
    }

    public String getFullName() {
        return elem.getFullName();
    }

    public String getPackageName() {
        return elem.getFileInfo().getPackageInfo().getName();
    }

    public String getFileName() {
        return elem.getFileInfo().getName();
    }

    public List<UnifiedOperation> getOperations() {
        return new ArrayList<>(bodyMap.keySet());
    }

    public void addOperation(UnifiedOperation op, String body) {
        bodyMap.put(op, body);
    }

    public boolean isEdited() {
        return bodyMap.size() > 0;
    }

    public boolean contains(UnifiedOperation op) {
        return bodyMap.containsKey(op);
    }

    public String getBody(UnifiedOperation op) {
        return bodyMap.get(op);
    }

    public boolean equalsFileElement(BranchJavaElement elem) {
        if (!StringComparator.isSame(this.getPackageName(), elem.getPackageName())) {
            return false;
        }

        if (!StringComparator.isSame(this.getFileName(), elem.getFileName())) {
            return false;
        }

        if (!StringComparator.isSame(this.getName(), elem.getName())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName()).append(" op[ ");
        for (UnifiedOperation op : getOperations()) {
            sb.append(op.getId()).append(" ");
        }
        sb.append("]");
        return sb.toString();
    }
}
