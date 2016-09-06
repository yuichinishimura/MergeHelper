package fse.eclipse.mergehelper.detector.merge;

import java.util.ArrayList;
import java.util.List;

import org.jtool.changerepository.data.FileInfo;
import org.jtool.changerepository.data.ProjectInfo;
import org.jtool.changerepository.operation.UnifiedOperation;
import org.jtool.changerepository.parser.CodeRange;
import org.jtool.changerepository.parser.OpJavaElement;

import fse.eclipse.mergehelper.detector.AbstractDetector;
import fse.eclipse.mergehelper.element.BranchFileInfo;
import fse.eclipse.mergehelper.element.BranchInfo;
import fse.eclipse.mergehelper.element.BranchJavaElement;
import fse.eclipse.mergehelper.element.BranchRootInfo;
import fse.eclipse.mergehelper.element.MergePoint;
import fse.eclipse.mergehelper.element.MergeType;
import fse.eclipse.mergehelper.ui.dialog.ConflictDetectingDialog;
import fse.eclipse.mergehelper.util.Parser;

public class AMergedAdjustOperationOffset extends AbstractDetector {
    private static final String MESSAGE = "Ajust Operation Offset";
    private static final String ERROR_MESSAGE = "Ajust Offset Failed";
    private static AbstractDetector instance = new AMergedAdjustOperationOffset();

    private boolean isSuccess = true;

    private AMergedAdjustOperationOffset() {
    }

    public static AbstractDetector getInstance() {
        return instance;
    }

    @Override
    protected void execute() {
        BranchRootInfo rootInfo = BranchRootInfo.getInstance();
        BranchInfo a_bInfo = rootInfo.getBranchInfo(MergeType.ACCEPT);
        BranchInfo j_bInfo = rootInfo.getBranchInfo(MergeType.JOIN);
        MergePoint mPoint = rootInfo.getMergePoint();

        try {
            adjustOffset(a_bInfo, mPoint);
            adjustOffset(j_bInfo, mPoint);
        } catch (NullPointerException e) {
            isSuccess = true;
        }
    }

    private void adjustOffset(BranchInfo bInfo, MergePoint mPoint) throws NullPointerException {
        ProjectInfo pInfo = bInfo.getProjectInfo();
        List<UnifiedOperation> ops = pInfo.getOperations();
        int size = ops.size();

        MergeType type = bInfo.getType();
        UnifiedOperation point = mPoint.getMergePoint(type);
        int mIdx = point.indexOfProjectInfo();

        List<MergedResultHolder> holders = new ArrayList<>();
        for (int i = size - 1; i > mIdx; i--) {
            UnifiedOperation op = ops.get(i);
            if (!op.isTextChangedOperation()) {
                continue;
            }

            FileInfo fInfo = op.getFileInfo();
            BranchFileInfo bfInfo = bInfo.getBranchFileInfo(fInfo);

            MergedResultHolder holder = selectHolder(holders, fInfo);
            if (holder == null) {
                String mergedCode = bfInfo.getMergedResultCode();
                List<OpJavaElement> elems = new ArrayList<>();
                Parser.collectElements(fInfo, mergedCode, elems);
                holder = new MergedResultHolder(fInfo, elems);
                holders.add(holder);
            }

            int idx = op.indexOfFileInfo();
            String code = fInfo.getCode(idx);
            List<OpJavaElement> elems = new ArrayList<>();
            Parser.collectElements(fInfo, code, elems);

            BranchJavaElement targetElem = bfInfo.getBranchJavaElement(op);
            for (OpJavaElement elem : elems) {
                if (elem.getFullName().equals(targetElem.getFullName())) {
                    adjustOperationOffset(op, elem.getCodeRange(), holder.getMergedElementRange(elem));
                    break;
                }
            }
        }
    }

    private void adjustOperationOffset(UnifiedOperation op, CodeRange range, CodeRange mergedResultRange) {
        int relativeOffset = op.getStart() - range.getStart();
        int adjustOffset = mergedResultRange.getStart() + relativeOffset;
        op.setStart(adjustOffset);
    }

    private MergedResultHolder selectHolder(List<MergedResultHolder> holders, FileInfo fInfo) {
        for (MergedResultHolder holder : holders) {
            if (holder.equals(fInfo)) {
                return holder;
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
        if (isSuccess) {
            finish(dialog);
        } else {
            error(dialog);
        }
    }

    private class MergedResultHolder {
        String name;
        List<OpJavaElement> elems;

        public MergedResultHolder(FileInfo fInfo, List<OpJavaElement> elems) {
            name = fInfo.getQualifiedName();
            this.elems = elems;
        }

        CodeRange getMergedElementRange(OpJavaElement element) {
            for (OpJavaElement elem : elems) {
                if (elem.getFullName().equals(element.getFullName())) {
                    return elem.getCodeRange();
                }
            }
            return null;
        }

        boolean equals(FileInfo fInfo) {
            return name.equals(fInfo.getQualifiedName());
        }
    }
}
