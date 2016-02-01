package fse.eclipse.mergehelper.detecter.merge;

import java.util.ArrayList;
import java.util.Collections;
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
import fse.eclipse.mergehelper.util.RepositoryElementInfoUtil;

// TODO: アルゴリズム改善 + コード綺麗に
public class AMergePointSearcher extends AbstractDetector {

    private static final String MESSAGE = "Search Artificial Merge Point";
    private static final String ERROR_MESSAGE = "Artificial Merge Point was Not Found";
    private static AbstractDetector instance = new AMergePointSearcher();

    private MergePoint mPoint;

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

        List<ElementSlice> s_slices = rootInfo.getBranchInfo(MergeType.SRC).getAllSlice();
        List<OperationInfo> s_opInfos = new ArrayList<OperationInfo>(s_pInfo.getOperationNumber());
        initOperationInfos(s_opInfos, s_pInfo, s_slices);

        List<ElementSlice> d_slices = rootInfo.getBranchInfo(MergeType.DEST).getAllSlice();
        List<OperationInfo> d_opInfos = new ArrayList<OperationInfo>(d_pInfo.getOperationNumber());
        initOperationInfos(d_opInfos, d_pInfo, d_slices);

        searchPointEqualBody(cInfo, s_pInfo, d_pInfo, s_opInfos, d_opInfos);

        rootInfo.setMergePoint(mPoint);
    }

    private boolean searchPointBeforeConflictElement(ConflictInfo cInfo, ProjectInfo spInfo, ProjectInfo dpInfo) {
        Map<ElementSlice, ElementSlice> elemMap = cInfo.getConflictSliceMap();
        String targetElement = null;
        int s_point = Integer.MAX_VALUE;
        int s_fileIdx = Integer.MAX_VALUE;

        int d_point = Integer.MAX_VALUE;
        int d_fileIdx = Integer.MAX_VALUE;

        for (Entry<ElementSlice, ElementSlice> entry : elemMap.entrySet()) {
            ElementSlice s_slice = entry.getKey();
            ElementSlice d_slice = entry.getValue();

            UnifiedOperation s_op = s_slice.getOperations().get(0);
            UnifiedOperation d_op = d_slice.getOperations().get(0);

            int s_point2 = RepositoryElementInfoUtil.getUId(s_op) - 1;
            int d_point2 = RepositoryElementInfoUtil.getUId(d_op) - 1;

            if (s_point > s_point2 && d_point > d_point2) {
                // TODO: 範囲を考慮して改善
                s_point = s_point2;
                s_fileIdx = RepositoryElementInfoUtil.getFileId(s_op);

                d_point = d_point2;
                d_fileIdx = RepositoryElementInfoUtil.getFileId(d_op);

                targetElement = s_slice.getFullName();
            }
        }

        if (targetElement != null) {
            mPoint = new MergePoint(targetElement);
            mPoint.setMergePoint(s_point, s_fileIdx, d_point, d_fileIdx);
            return true;
        }
        return false;
    }

    private void initOperationInfos(List<OperationInfo> opInfos, ProjectInfo pInfo, List<ElementSlice> slices) {
        List<UnifiedOperation> ops = pInfo.getOperations();
        for (UnifiedOperation op : ops) {
            OperationInfo opInfo = new OperationInfo(op);

            int id = RepositoryElementInfoUtil.getFileId(op);
            List<ElementSlice> ss = selectSlice(op, slices);
            for (ElementSlice slice : ss) {
                opInfo.addElement(slice.getFileName(), slice.getBody(id));
            }

            opInfos.add(opInfo);
        }
    }

    private void searchPointEqualBody(ConflictInfo cInfo, ProjectInfo s_pInfo, ProjectInfo d_pInfo, List<OperationInfo> s_opInfos,
            List<OperationInfo> d_opInfos) {
        int s_point2 = mPoint.getMergePoint(MergeType.SRC) + 1;
        int d_point2 = mPoint.getMergePoint(MergeType.DEST) + 1;

        int s_limit = searchPointOtherConflictElement(cInfo, s_opInfos, s_point2);
        int d_limit = searchPointOtherConflictElement(cInfo, d_opInfos, d_point2);

        for (int i = s_point2; i < s_limit; i++) {
            OperationInfo s_opInfo = selectOperationInfo(i, s_opInfos);
            if (!s_opInfo.isEdit()) {
                continue;
            }

            List<String> s_bodies = s_opInfo.getBodies();
            for (int j = d_point2; j < d_limit; j++) {
                OperationInfo d_opInfo = selectOperationInfo(j, d_opInfos);
                if (!d_opInfo.isEdit()) {
                    continue;
                }

                List<String> d_bodies = d_opInfo.getBodies();
                for (String s_body : s_bodies) {
                    for (String d_body : d_bodies) {
                        if (s_body.equals(d_body)) {
                            // TODO: 範囲を考慮して改善
                            int s_fileIdx = s_opInfo.getFileId();
                            int d_fileIdx = d_opInfo.getFileId();

                            mPoint.setMergePoint(i, s_fileIdx, j, d_fileIdx);
                        }
                    }
                }
            }
        }
    }

    private int searchPointOtherConflictElement(ConflictInfo cInfo, List<OperationInfo> opInfos, int idx) {
        String targetElement = mPoint.getTargetElement();
        int limit = opInfos.size();
        for (; idx < limit; idx++) {
            OperationInfo opInfo = opInfos.get(idx);
            if (!opInfo.isEdit()) {
                continue;
            }

            List<String> names = opInfo.getElemNames();
            for (String name : names) {
                if (!name.equals(targetElement) && cInfo.isConflictElement(name)) {
                    return limit;
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

    private OperationInfo selectOperationInfo(int uid, List<OperationInfo> opInfos) {
        for (OperationInfo opInfo : opInfos) {
            if (uid == opInfo.getUId()) {
                return opInfo;
            }
        }
        return null;
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

    private class OperationInfo {
        private final UnifiedOperation op;
        private final List<String> elemNames;
        private final List<String> bodies;

        OperationInfo(UnifiedOperation op) {
            this.op = op;
            elemNames = new ArrayList<String>();
            bodies = new ArrayList<String>();
        }

        int getUId() {
            return op.getId();
        }

        int getFileId() {
            return RepositoryElementInfoUtil.getFileId(op);
        }

        void addElement(String name, String body) {
            elemNames.add(name);
            bodies.add(body);
        }

        List<String> getElemNames() {
            return Collections.unmodifiableList(elemNames);
        }

        List<String> getBodies() {
            return Collections.unmodifiableList(bodies);
        }

        boolean isEdit() {
            return elemNames.size() > 0 && bodies.size() > 0;
        }

        @Override
        public String toString() {
            int uid = getUId();
            int idx = getFileId();

            StringBuilder sb = new StringBuilder();
            sb.append(uid).append("-").append(idx).append(" elem:");
            for (String elemName : elemNames) {
                sb.append(elemName).append(" ");
            }
            return sb.toString();
        }
    }
}
