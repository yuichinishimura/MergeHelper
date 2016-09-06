package fse.eclipse.mergehelper.detector;

import java.util.List;

import org.jtool.changerecorder.util.StringComparator;

import fse.eclipse.mergehelper.detector.merge.OperationSwaper;
import fse.eclipse.mergehelper.element.BranchFileInfo;
import fse.eclipse.mergehelper.element.BranchInfo;
import fse.eclipse.mergehelper.element.BranchRootInfo;
import fse.eclipse.mergehelper.element.ConflictInfo;
import fse.eclipse.mergehelper.element.MergeType;
import fse.eclipse.mergehelper.ui.dialog.ConflictDetectingDialog;

public class ConflictDetector extends AbstractDetector {
    private static final String MESSAGE = "Detect Conflict ...";
    private static final String ERROR_MESSAGE = "Conflict Element was Not Found";
    private static AbstractDetector instance = new ConflictDetector();

    private ConflictInfo cInfo;

    private ConflictDetector() {
    }

    public static AbstractDetector getInstance() {
        return instance;
    }

    @Override
    public void execute() {
        BranchRootInfo rootInfo = BranchRootInfo.getInstance();
        BranchInfo a_bInfo = rootInfo.getBranchInfo(MergeType.ACCEPT);
        BranchInfo j_bInfo = rootInfo.getBranchInfo(MergeType.JOIN);

        List<BranchFileInfo> abfInfos = a_bInfo.getAllBranchFileInfo();
        List<BranchFileInfo> jbfInfos = j_bInfo.getAllBranchFileInfo();

        cInfo = new ConflictInfo();
        for (BranchFileInfo abfInfo : abfInfos) {
            for (BranchFileInfo jbfInfo : jbfInfos) {
                if (abfInfo.equalsFileName(jbfInfo)) {
                    detectElement(abfInfo, jbfInfo);
                    break;
                }
            }
        }
        rootInfo.setConflictInfo(cInfo);
    }

    private void detectElement(BranchFileInfo abfInfo, BranchFileInfo jbfInfo) {
        List<String> aElems = abfInfo.getAllElementName();
        List<String> jElems = jbfInfo.getAllElementName();

        for (String aElem : aElems) {
            for (String jElem : jElems) {
                if (StringComparator.isSame(aElem, jElem)) {
                    cInfo.addConflictElements(abfInfo.getBranchJavaElement(aElem), jbfInfo.getBranchJavaElement(jElem));
                    break;
                }
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
        if (cInfo.isConflict()) {
            OperationSwaper.getInstance().detect(dialog);
        } else {
            error(dialog);
        }
    }
}
