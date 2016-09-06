package fse.eclipse.mergehelper.detector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jtool.changerepository.data.FileInfo;
import org.jtool.changerepository.data.ProjectInfo;
import org.jtool.changerepository.operation.UnifiedOperation;
import org.jtool.changerepository.parser.CodeRange;
import org.jtool.changerepository.parser.OpJavaElement;

import fse.eclipse.mergehelper.element.BranchFileInfo;
import fse.eclipse.mergehelper.element.BranchInfo;
import fse.eclipse.mergehelper.element.BranchJavaElement;
import fse.eclipse.mergehelper.element.BranchRootInfo;
import fse.eclipse.mergehelper.element.MergeType;
import fse.eclipse.mergehelper.ui.dialog.ConflictDetectingDialog;
import fse.eclipse.mergehelper.util.Parser;

public class OperationAllocater extends AbstractDetector {
    private static final String MESSAGE = "Allocate Operation ...";
    private static final String ERROR_MESSAGE = "NULL";
    private static AbstractDetector instance = new OperationAllocater();

    private OperationAllocater() {
    }

    public static AbstractDetector getInstace() {
        return instance;
    }

    @Override
    protected void execute() {
        BranchRootInfo rootInfo = BranchRootInfo.getInstance();
        allocateOperation(rootInfo.getBranchInfo(MergeType.ACCEPT));
        allocateOperation(rootInfo.getBranchInfo(MergeType.JOIN));
    }

    private void allocateOperation(BranchInfo bInfo) {
        ProjectInfo pInfo = bInfo.getProjectInfo();
        List<FileInfo> fInfos = pInfo.getAllFileInfo();
        for (FileInfo fInfo : fInfos) {
            BranchFileInfo bfInfo = createBranchFileInfo(bInfo, fInfo);
            if (bfInfo != null) {
                collectBranchJavaElement(bfInfo, fInfo);
                removeNotEditedElement(bfInfo);
            }
        }
    }

    private BranchFileInfo createBranchFileInfo(BranchInfo bInfo, FileInfo fInfo) {
        int size = fInfo.getOperationNumber() - 1;
        for (int i = size; i >= 0; i--) {
            String latestCode = fInfo.getCode(i);
            List<OpJavaElement> elems = new ArrayList<>();
            boolean parseable = Parser.collectElements(fInfo, latestCode, elems);
            if (parseable) {
                return bInfo.createBranchFileInfo(fInfo, elems);
            }
        }
        return null;
    }

    private void collectBranchJavaElement(BranchFileInfo bfInfo, FileInfo fInfo) {
        List<UnifiedOperation> ops = fInfo.getOperations();
        int size = ops.size();
        for (int i = 0; i < size; i++) {
            UnifiedOperation op = ops.get(i);
            String code = fInfo.getCode(i);
            List<OpJavaElement> elems = new ArrayList<>();
            boolean parseable = Parser.collectElements(fInfo, code, elems);
            if (!parseable) {
                throw new Error();
                // continue;
            }

            for (OpJavaElement elem : elems) {
                CodeRange range = elem.getCodeRange();
                if (range.inRange(op.getStart())) {
                    String body = code.substring(range.getStart(), range.getEnd());
                    bfInfo.addOperation(elem, op, body);
                    break;
                }
            }
        }
    }

    private void removeNotEditedElement(BranchFileInfo bfInfo) {
        Map<String, BranchJavaElement> elemMap = bfInfo.getElementMap();
        for (Entry<String, BranchJavaElement> entry : elemMap.entrySet()) {
            if (!entry.getValue().isEdited()) {
                bfInfo.removeBranchJavaElement(entry.getKey());
            }
        }
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
        ConflictDetector.getInstance().detect(dialog);
    }
}
