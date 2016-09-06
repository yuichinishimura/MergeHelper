package fse.eclipse.mergehelper.element;

import org.jtool.changerepository.operation.UnifiedOperation;

public class MergePoint {
    private final BranchJavaElement a_elem;
    private final BranchJavaElement j_elem;
    private UnifiedOperation a_op;
    private UnifiedOperation j_op;

    public MergePoint(BranchJavaElement aElem, BranchJavaElement jElem) {
        this.a_elem = aElem;
        this.j_elem = jElem;
    }

    public BranchJavaElement getElement(MergeType type) {
        if (MergeType.isAccept(type)) {
            return a_elem;
        } else {
            return j_elem;
        }
    }

    public UnifiedOperation getMergePoint(MergeType type) {
        if (MergeType.isAccept(type)) {
            return a_op;
        } else {
            return j_op;
        }
    }

    public void setMergePoint(UnifiedOperation a_op, UnifiedOperation j_op) {
        this.a_op = a_op;
        this.j_op = j_op;
    }
}
