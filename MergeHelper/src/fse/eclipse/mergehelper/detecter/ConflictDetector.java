package fse.eclipse.mergehelper.detecter;

import java.util.List;

import fse.eclipse.mergehelper.detecter.merge.AMergePointSearcher;
import fse.eclipse.mergehelper.element.BranchFileInfo;
import fse.eclipse.mergehelper.element.BranchInfo;
import fse.eclipse.mergehelper.element.BranchRootInfo;
import fse.eclipse.mergehelper.element.ConflictInfo;
import fse.eclipse.mergehelper.element.ElementSlice;
import fse.eclipse.mergehelper.element.MergeType;
import fse.eclipse.mergehelper.ui.dialog.ConflictDetectingDialog;

public class ConflictDetector extends AbstractDetector {

    private static final String MESSAGE = "Detect Conflict ...";
    private static final String ERROR_MESSAGE = "Conflict Element was Not Found";
    private static AbstractDetector instance = new ConflictDetector();

    private boolean isConflict;

    private ConflictDetector() {
    }

    public static AbstractDetector getInstance() {
        return instance;
    }

    @Override
    public void execute(ConflictDetectingDialog dialog) {
        BranchRootInfo rootInfo = BranchRootInfo.getInstance();
        BranchInfo acceptInfo = rootInfo.getBranchInfo(MergeType.ACCEPT);
        BranchInfo joinInfo = rootInfo.getBranchInfo(MergeType.JOIN);

        List<BranchFileInfo> abfInfos = acceptInfo.getAllBranchFileInfo();
        List<BranchFileInfo> jbfInfos = joinInfo.getAllBranchFileInfo();

        ConflictInfo cInfo = new ConflictInfo();
        for (BranchFileInfo abfInfo : abfInfos) {
            String aName = abfInfo.getFullName();
            for (BranchFileInfo jbfInfo : jbfInfos) {
                String jName = jbfInfo.getFullName();
                if (aName.equals(jName)) {
                    detectElement(abfInfo, jbfInfo, cInfo);
                    break;
                }
            }
        }
        isConflict = cInfo.isConflict();
        rootInfo.setConflictInfo(cInfo);
    }

    private void detectElement(BranchFileInfo abfInfo, BranchFileInfo jbfInfo, ConflictInfo cInfo) {
        List<ElementSlice> aslices = abfInfo.getAllSlice();
        List<ElementSlice> jslices = jbfInfo.getAllSlice();

        for (ElementSlice aslice : aslices) {
            for (ElementSlice jslice : jslices) {
                if (ElementSlice.equalSliceName(aslice, jslice)) {
                    cInfo.addConflictElements(aslice, jslice);
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
        if (isConflict) {
            AMergePointSearcher.getInstance().detect(dialog);
        } else {
            error(dialog);
        }
    }
}
