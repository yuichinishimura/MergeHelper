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
    private ElementSlice s_slice, d_slice;

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
        ProjectInfo s_pInfo = rootInfo.getBranchInfo(MergeType.SRC).getProjectInfo();
        ProjectInfo d_pInfo = rootInfo.getBranchInfo(MergeType.DEST).getProjectInfo();

        isFind = searchPointBeforeConflictElement(cInfo, s_pInfo, d_pInfo);
        if (!isFind) {
            return;
        }

        int s_point = mPoint.getMergePoint(MergeType.SRC) + 1;
        int d_point = mPoint.getMergePoint(MergeType.DEST) + 1;

        List<ElementSlice> s_slices = rootInfo.getBranchInfo(MergeType.SRC).getAllSlice();
        int s_limit = searchPointRange(cInfo, s_pInfo, s_slices, s_point);

        List<ElementSlice> d_slices = rootInfo.getBranchInfo(MergeType.DEST).getAllSlice();
        int d_limit = searchPointRange(cInfo, d_pInfo, d_slices, d_point);

        searchPointEqualBody(cInfo, s_pInfo, d_pInfo, s_point, s_limit, d_point, d_limit);

        rootInfo.setMergePoint(mPoint);
    }

    private boolean searchPointBeforeConflictElement(ConflictInfo cInfo, ProjectInfo spInfo, ProjectInfo dpInfo) {
        Map<ElementSlice, ElementSlice> elemMap = cInfo.getConflictSliceMap();
        String targetElement = null;
        int old_s_point = -1;
        int old_d_point = -1;

        for (Entry<ElementSlice, ElementSlice> entry : elemMap.entrySet()) {
            ElementSlice s_slice = entry.getKey();
            ElementSlice d_slice = entry.getValue();

            UnifiedOperation s_op = s_slice.getOperations().get(0);
            UnifiedOperation d_op = d_slice.getOperations().get(0);

            int new_s_point = s_op.getId() - 1;
            int new_d_point = d_op.getId() - 1;

            if (isNarrowPoint(new_s_point, new_d_point, old_s_point, old_d_point)) {
                old_s_point = new_s_point;
                old_d_point = new_d_point;

                targetElement = s_slice.getFullName();
                this.s_slice = s_slice;
                this.d_slice = d_slice;
            }
        }

        if (targetElement != null) {
            mPoint = new MergePoint(targetElement);
            mPoint.setMergePoint(old_s_point, old_d_point);
            return true;
        }
        return false;
    }

    private void searchPointEqualBody(ConflictInfo cInfo, ProjectInfo s_pInfo, ProjectInfo d_pInfo, int s_idx, int s_limit, int d_idx, int d_limit) {
        List<UnifiedOperation> s_ops = s_pInfo.getOperations();
        List<UnifiedOperation> d_ops = d_pInfo.getOperations();

        for (int i = s_idx; i < s_limit; i++) {
            UnifiedOperation s_op = s_ops.get(i);
            if (s_slice.contains(s_op)) {
                int s_id = s_op.getId();
                String s_body = s_slice.getBody(s_id);

                for (int j = d_idx; j < d_limit; j++) {
                    UnifiedOperation d_op = d_ops.get(j);
                    if (d_slice.contains(d_op)) {
                        int d_id = d_op.getId();
                        String d_body = d_slice.getBody(d_id);

                        if (s_body.equals(d_body)) {
                            if (isNarrowPoint(s_id, d_id)) {
                                mPoint.setMergePoint(s_id, d_id);
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

    private boolean isNarrowPoint(int s_point, int d_point) {
        int old_s_point = mPoint.getMergePoint(MergeType.SRC);
        int old_d_point = mPoint.getMergePoint(MergeType.DEST);

        return isNarrowPoint(s_point, d_point, old_s_point, old_d_point);
    }

    private boolean isNarrowPoint(int new_s_point, int new_d_point, int old_s_point, int old_d_point) {
        int newPoint = new_s_point + new_d_point;
        int oldPoint = old_s_point + old_d_point;

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
