package fse.eclipse.mergehelper.element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jtool.changerepository.data.FileInfo;
import org.jtool.changerepository.operation.UnifiedOperation;
import org.jtool.changeslicereplayer.slicer.Slice;

import fse.eclipse.mergehelper.util.Parser;

public class ElementSlice {

    private final ElementSliceCriterion criterion;
    private final List<UnifiedOperation> ops;
    private final Map<Integer, String> bodies;

    public ElementSlice(Slice slice) {
        criterion = (ElementSliceCriterion) slice.getCriterion();
        ops = normalizeOperations(slice.getOperations());
        bodies = collectBody(ops);
    }

    public String getName() {
        return criterion.getName();
    }

    public String getFullName() {
        return criterion.getFullName();
    }

    public String getFileName() {
        return criterion.getFileName();
    }

    public String getExtensionFileName() {
        return criterion.getExtensionFileName();
    }

    public String getPackageName() {
        return criterion.getPackageName();
    }

    public int size() {
        return ops.size();
    }

    public List<UnifiedOperation> getOperations() {
        return Collections.unmodifiableList(ops);
    }

    public UnifiedOperation getOperation(int idx) {
        for (UnifiedOperation op : ops) {
            if (op.getId() == idx) {
                return op;
            }
        }
        return null;
    }

    public Map<Integer, String> getBodies() {
        return Collections.unmodifiableMap(bodies);
    }

    public String getBody(int id) {
        return bodies.get(id);
    }

    public boolean contains(UnifiedOperation op2) {
        for (UnifiedOperation op : ops) {
            if (op.equals(op2)) {
                return true;
            }
        }
        return false;
    }

    private List<UnifiedOperation> normalizeOperations(List<UnifiedOperation> ops) {
        Set<UnifiedOperation> set = new HashSet<UnifiedOperation>();
        set.addAll(ops);

        ops = new ArrayList<UnifiedOperation>(set.size());
        for (UnifiedOperation op : set) {
            if (op.isTextChangedOperation()) {
                ops.add(op);
            }
        }

        List<UnifiedOperation> nops = new ArrayList<UnifiedOperation>(ops);
        Collections.sort(nops, (o1, o2) -> Integer.compare(o1.getId(), o2.getId()));
        return nops;
    }

    private Map<Integer, String> collectBody(List<UnifiedOperation> ops) {
        int size = ops.size();
        FileInfo fInfo = ops.get(0).getFileInfo();

        Map<Integer, String> bodies = new HashMap<Integer, String>(size);
        String elemName = getName();
        for (UnifiedOperation op : ops) {
            int id = op.getId();
            String code = fInfo.getCode(id);
            String body = Parser.getElementBody(fInfo, code, elemName);
            bodies.put(id, body);
        }
        return bodies;
    }

    public static boolean equalSliceName(ElementSlice slice, ElementSlice slice2) {
        return slice.getFullName().equals(slice2.getFullName());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName()).append(" op[ ");
        for (UnifiedOperation op : ops) {
            sb.append(op.getId()).append(" ");
        }
        sb.append("]");
        return sb.toString();
    }
}
