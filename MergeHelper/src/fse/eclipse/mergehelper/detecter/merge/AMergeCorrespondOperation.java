package fse.eclipse.mergehelper.detecter.merge;

import java.util.ArrayList;
import java.util.List;

import org.jtool.changerecorder.util.FileStream;
import org.jtool.changerepository.data.FileInfo;
import org.jtool.changerepository.operation.UnifiedOperation;
import org.jtool.changerepository.parser.OpJavaElement;

import fse.eclipse.mergehelper.detecter.AbstractDetector;
import fse.eclipse.mergehelper.element.BranchFileInfo;
import fse.eclipse.mergehelper.element.BranchInfo;
import fse.eclipse.mergehelper.element.BranchRootInfo;
import fse.eclipse.mergehelper.element.ElementSlice;
import fse.eclipse.mergehelper.element.MergePoint;
import fse.eclipse.mergehelper.element.MergeType;
import fse.eclipse.mergehelper.ui.dialog.ConflictDetectingDialog;
import fse.eclipse.mergehelper.util.Parser;

public class AMergeCorrespondOperation extends AbstractDetector {

    private static final String MESSAGE = "Correspond Operation ...";
    private static final String ERROR_MESSAGE = "NULL";
    private static AbstractDetector instance = new AMergeCorrespondOperation();

    private AMergeCorrespondOperation() {
    }

    public static AbstractDetector getInstance() {
        return instance;
    }

    @Override
    public void execute(ConflictDetectingDialog dialog) {
        BranchRootInfo rootInfo = BranchRootInfo.getInstance();
        MergePoint mPoint = rootInfo.getMergePoint();

        int point = mPoint.getMergePoint(MergeType.ACCEPT);
        correspond(rootInfo.getBranchInfo(MergeType.ACCEPT), point);

        point = mPoint.getMergePoint(MergeType.JOIN);
        correspond(rootInfo.getBranchInfo(MergeType.JOIN), point);
    }

    private void correspond(BranchInfo bInfo, int point) {
        List<FileInfo> fInfos = bInfo.getProjectInfo().getAllFileInfo();
        for (FileInfo fInfo : fInfos) {
            int size = fInfo.getOperationNumber();
            int lastId = fInfo.getOperation(size - 1).getId();
            if (point <= lastId) {
                BranchFileInfo bfInfo = bInfo.getBranchFileInfo(fInfo);
                if (bfInfo != null) {
                    String mergedCode = readResultMergedCode(fInfo, bInfo.getName());
                    correspond(fInfo, bfInfo, mergedCode, point);
                }
            }
        }
    }

    private void correspond(FileInfo fInfo, BranchFileInfo bfInfo, String mergedCode, int point) {
        bfInfo.addMergedResult(point, mergedCode, -1);

        List<UnifiedOperation> ops = fInfo.getOperations();
        int size = ops.size();
        for (int i = 0; i < size; i++) {
            UnifiedOperation op = ops.get(i);
            int id = op.getId();

            if (point < id) {
                List<ElementSlice> slices = bfInfo.getSlices(op);
                if (slices.size() == 0) {
                    // TODO: sliceに存在しないopの対応
                } else {
                    mergedCode = replaceElementBody(fInfo, bfInfo, mergedCode, slices.get(0), id);
                }
            }
        }
    }

    private String replaceElementBody(FileInfo fInfo, BranchFileInfo bfInfo, String code, ElementSlice slice, int id) {
        String elemName = slice.getName();
        String body = slice.getBody(id);
        UnifiedOperation op = slice.getOperation(id);

        List<OpJavaElement> elems = new ArrayList<OpJavaElement>();
        Parser.collectElements(fInfo, code, elems);
        for (OpJavaElement elem : elems) {
            if (elem.getSimpleName().equals(elemName)) {
                int start = elem.getStart();
                int end = elem.getEnd() + 1;

                StringBuilder sb = new StringBuilder(code);
                sb.replace(start, end, body);
                code = sb.toString();
                int opOffset = collectOperationOffset(fInfo, id, code, elem, op);
                bfInfo.addMergedResult(id, code, opOffset);
                break;
            }
        }
        return code;
    }

    private int collectOperationOffset(FileInfo fInfo, int id, String newCode, OpJavaElement elem, UnifiedOperation op) {
        OpJavaElement oldElem = Parser.getElement(fInfo, fInfo.getCode(id), elem.getSimpleName());
        OpJavaElement newElem = Parser.getElement(fInfo, newCode, oldElem.getSimpleName());
        if (oldElem == null || newElem == null) {
            return op.getStart();
        }

        int diff = newElem.getStart() - oldElem.getStart();
        return op.getStart() + diff;
    }

    private String readResultMergedCode(FileInfo fInfo, String branchName) {
        String path = AMerge.createMergePath(fInfo, branchName);
        return FileStream.read(path);
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
        finish(dialog);
    }
}
