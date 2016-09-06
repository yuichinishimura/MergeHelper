package fse.eclipse.mergehelper.detector.merge;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jtool.changerepository.data.ProjectInfo;
import org.jtool.changerepository.operation.UnifiedOperation;

import fse.eclipse.mergehelper.detector.AbstractDetector;
import fse.eclipse.mergehelper.element.BranchInfo;
import fse.eclipse.mergehelper.element.BranchJavaElement;
import fse.eclipse.mergehelper.element.BranchRootInfo;
import fse.eclipse.mergehelper.element.ConflictInfo;
import fse.eclipse.mergehelper.element.MergePoint;
import fse.eclipse.mergehelper.element.MergeType;
import fse.eclipse.mergehelper.ui.dialog.ConflictDetectingDialog;

public class AMergePointSearcher extends AbstractDetector {
    private static final String MESSAGE = "Search Artificial Merge Point";
    private static final String ERROR_MESSAGE = "Artificial Merge Point was Not Found";
    private static AbstractDetector instance = new AMergePointSearcher();

    private MergePoint mPoint;

    private AMergePointSearcher() {
    }

    public static AbstractDetector getInstance() {
        return instance;
    }

    @Override
    public void execute() {
        BranchRootInfo rootInfo = BranchRootInfo.getInstance();
        BranchInfo a_bInfo = rootInfo.getBranchInfo(MergeType.ACCEPT);
        BranchInfo j_bInfo = rootInfo.getBranchInfo(MergeType.JOIN);
        ConflictInfo cInfo = rootInfo.getConflictInfo();

        ProjectInfo a_pInfo = a_bInfo.getProjectInfo();
        ProjectInfo j_pInfo = j_bInfo.getProjectInfo();

        searchPointBeforeConflictElement(cInfo, a_pInfo, j_pInfo);
        if (mPoint == null) {
            return;
        }

        searchPointEqualBody(mPoint.getElement(MergeType.ACCEPT), mPoint.getElement(MergeType.JOIN));
        rootInfo.setMergePoint(mPoint);
    }

    private void searchPointBeforeConflictElement(ConflictInfo cInfo, ProjectInfo a_pInfo, ProjectInfo j_pInfo) {
        Map<BranchJavaElement, BranchJavaElement> elemMap = cInfo.getConflictElementMap();

        BranchJavaElement a_elem = null;
        BranchJavaElement j_elem = null;
        UnifiedOperation a_op = null;
        UnifiedOperation j_op = null;

        for (Entry<BranchJavaElement, BranchJavaElement> entry : elemMap.entrySet()) {
            BranchJavaElement new_a_elem = entry.getKey();
            BranchJavaElement new_j_elem = entry.getValue();
            UnifiedOperation new_a_op = new_a_elem.getOperations().get(0);
            UnifiedOperation new_j_op = new_j_elem.getOperations().get(0);

            if (isNarrowPoint(new_a_op, new_j_op, a_op, j_op)) {
                a_elem = new_a_elem;
                j_elem = new_j_elem;
                a_op = a_pInfo.getOperation(a_pInfo.indexOfOperation(new_a_op.getId()) - 1);
                j_op = j_pInfo.getOperation(j_pInfo.indexOfOperation(new_j_op.getId()) - 1);
            }
        }

        if (a_elem != null) {
            mPoint = new MergePoint(a_elem, j_elem);
            mPoint.setMergePoint(a_op, j_op);
        }
    }

    private void searchPointEqualBody(BranchJavaElement aElem, BranchJavaElement jElem) {
        List<UnifiedOperation> a_ops = aElem.getOperations();
        List<UnifiedOperation> j_ops = jElem.getOperations();
        int a_size = a_ops.size() - 1;
        int j_size = j_ops.size() - 1;

        UnifiedOperation a_op = null;
        UnifiedOperation j_op = null;

        for (int a = a_size; a >= 0; a--) {
            UnifiedOperation new_a_op = a_ops.get(a);
            String a_body = aElem.getBody(new_a_op);
            for (int j = j_size; j >= 0; j--) {
                UnifiedOperation new_j_op = j_ops.get(j);
                String j_body = jElem.getBody(new_j_op);

                if (a_body.equals(j_body) && isNarrowPoint(new_a_op, new_j_op, a_op, j_op)) {
                    a_op = new_a_op;
                    j_op = new_j_op;
                }
            }
        }

        if (a_op != null && j_op != null) {
            mPoint.setMergePoint(a_op, j_op);
        }
    }

    private boolean isNarrowPoint(UnifiedOperation new_a_op, UnifiedOperation new_j_op, UnifiedOperation a_op, UnifiedOperation j_op) {
        if (a_op == null && j_op == null) {
            return true;
        }

        int new_idx = new_a_op.indexOfProjectInfo() + new_j_op.indexOfProjectInfo();
        int old_idx = a_op.indexOfProjectInfo() + j_op.indexOfProjectInfo();
        return new_idx > old_idx;
    }

    @Override
    protected String getMessage() {
        return MESSAGE;
    }

    @Override
    protected String getErrorMessage() {
        return ERROR_MESSAGE;
    }

    @Override
    protected void nextState(ConflictDetectingDialog dialog) {
        if (mPoint != null) {
            AMerge.getInstance().detect(dialog);
        } else {
            error(dialog);
        }
    }
}
