package fse.eclipse.mergehelper.detecter.merge;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jtool.changerepository.data.ProjectInfo;
import org.jtool.changerepository.operation.UnifiedOperation;

import fse.eclipse.mergehelper.detecter.AbstractDetector;
import fse.eclipse.mergehelper.element.BranchRootInfo;
import fse.eclipse.mergehelper.element.ConflictInfo;
import fse.eclipse.mergehelper.element.ElementSlice;
import fse.eclipse.mergehelper.element.MergePoint;
import fse.eclipse.mergehelper.element.MergeType;
import fse.eclipse.mergehelper.ui.dialog.ConflictDetectingDialog;

public class AMergePointSearcher extends AbstractDetector {

    private static final String MESSAGE = "Search Artificial Merge Point";
    private static final String ERROR_MESSAGE = "Artificial Merge Point was Not Found";
    private static AbstractDetector instance = new AMergePointSearcher();

    private MergePoint mPoint;
    private ElementSlice a_slice, j_slice;

    private boolean isFind;

    private AMergePointSearcher() {
    }

    public static AbstractDetector getInstance() {
        return instance;
    }

    @Override
    public void execute(ConflictDetectingDialog dialog) {
        BranchRootInfo rootInfo = BranchRootInfo.getInstance();
        ConflictInfo cInfo = rootInfo.getConflictInfo();

        isFind = searchPointBeforeConflictElement(cInfo);
        if (!isFind) {
            return;
        }

        ProjectInfo a_pInfo = rootInfo.getBranchInfo(MergeType.ACCEPT).getProjectInfo();
        ProjectInfo j_pInfo = rootInfo.getBranchInfo(MergeType.JOIN).getProjectInfo();

        int a_point = mPoint.getMergePoint(MergeType.ACCEPT) + 1;
        int j_point = mPoint.getMergePoint(MergeType.JOIN) + 1;

        List<ElementSlice> a_slices = rootInfo.getBranchInfo(MergeType.ACCEPT).getAllSlice();
        int a_limit = searchPointRange(cInfo, a_pInfo, a_slices, a_point);

        List<ElementSlice> j_slices = rootInfo.getBranchInfo(MergeType.JOIN).getAllSlice();
        int j_limit = searchPointRange(cInfo, j_pInfo, j_slices, j_point);

        searchPointEqualBody(cInfo, a_pInfo, j_pInfo, a_point, a_limit, j_point, j_limit);

        rootInfo.setMergePoint(mPoint);
    }

    private boolean searchPointBeforeConflictElement(ConflictInfo cInfo) {
        Map<ElementSlice, ElementSlice> elemMap = cInfo.getConflictSliceMap();
        String targetElement = null;
        int old_a_point = -1;
        int old_j_point = -1;

        for (Entry<ElementSlice, ElementSlice> entry : elemMap.entrySet()) {
            ElementSlice a_slice = entry.getKey();
            ElementSlice j_slice = entry.getValue();

            UnifiedOperation a_op = a_slice.getOperations().get(0);
            UnifiedOperation j_op = j_slice.getOperations().get(0);

            int new_a_point = a_op.getId() - 1;
            int new_j_point = j_op.getId() - 1;

            if (isNarrowPoint(new_a_point, new_j_point, old_a_point, old_j_point)) {
                old_a_point = new_a_point;
                old_j_point = new_j_point;

                targetElement = a_slice.getFullName();
                this.a_slice = a_slice;
                this.j_slice = j_slice;
            }
        }

        if (targetElement != null) {
            mPoint = new MergePoint(targetElement);
            mPoint.setMergePoint(old_a_point, old_j_point);
            return true;
        }
        return false;
    }

    private void searchPointEqualBody(ConflictInfo cInfo, ProjectInfo a_pInfo, ProjectInfo j_pInfo, int a_idx, int a_limit, int j_idx, int j_limit) {
        List<UnifiedOperation> a_ops = a_pInfo.getOperations();
        List<UnifiedOperation> j_ops = j_pInfo.getOperations();

        for (int i = a_idx; i < a_limit; i++) {
            UnifiedOperation a_op = a_ops.get(i);
            if (a_slice.contains(a_op)) {
                int a_id = a_op.getId();
                String a_body = a_slice.getBody(a_id);

                for (int j = j_idx; j < j_limit; j++) {
                    UnifiedOperation j_op = j_ops.get(j);
                    if (j_slice.contains(j_op)) {
                        int j_id = j_op.getId();
                        String j_body = j_slice.getBody(j_id);

                        if (a_body.equals(j_body)) {
                            if (isNarrowPoint(a_id, j_id)) {
                                mPoint.setMergePoint(a_id, j_id);
                            }
                        }
                    }
                }
            }
        }
    }

    private int searchPointRange(ConflictInfo cInfo, ProjectInfo pInfo, List<ElementSlice> allSlices, int idx) {
        String targetElement = mPoint.getTargetElement();
        List<UnifiedOperation> ops = pInfo.getOperations();
        int limit = ops.size();
        for (int i = idx; i < limit; i++) {
            UnifiedOperation op = ops.get(i);
            List<ElementSlice> slices = selectSlice(op, allSlices);

            for (ElementSlice slice : slices) {
                String name = slice.getFullName();
                if (!targetElement.equals(name) && cInfo.isConflictElement(name)) {
                    return i;
                }
            }
        }
        return limit;
    }

    private List<ElementSlice> selectSlice(UnifiedOperation op, List<ElementSlice> slices) {
        List<ElementSlice> ss = new ArrayList<ElementSlice>();
        for (ElementSlice slice : slices) {
            if (slice.contains(op)) {
                ss.add(slice);
            }
        }
        return ss;
    }

    private boolean isNarrowPoint(int a_point, int j_point) {
        int old_a_point = mPoint.getMergePoint(MergeType.ACCEPT);
        int old_j_point = mPoint.getMergePoint(MergeType.JOIN);

        return isNarrowPoint(a_point, j_point, old_a_point, old_j_point);
    }

    private boolean isNarrowPoint(int new_a_point, int new_j_point, int old_a_point, int old_j_point) {
        int newPoint = new_a_point + new_j_point;
        int oldPoint = old_a_point + old_j_point;

        return newPoint > oldPoint;
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
        if (isFind) {
            AMerge.getInstance().detect(dialog);
        } else {
            error(dialog);
        }
    }
}
