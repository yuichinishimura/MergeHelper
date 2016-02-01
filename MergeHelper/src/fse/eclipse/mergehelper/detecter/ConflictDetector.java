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
        BranchInfo srcInfo = rootInfo.getBranchInfo(MergeType.SRC);
        BranchInfo destInfo = rootInfo.getBranchInfo(MergeType.DEST);

        List<BranchFileInfo> sbfInfos = srcInfo.getAllBranchFileInfo();
        List<BranchFileInfo> dbfInfos = destInfo.getAllBranchFileInfo();

        ConflictInfo cInfo = new ConflictInfo();
        for (BranchFileInfo sbfInfo : sbfInfos) {
            String sName = sbfInfo.getFullName();
            for (BranchFileInfo dbfInfo : dbfInfos) {
                String dName = dbfInfo.getFullName();
                if (sName.equals(dName)) {
                    detectElement(sbfInfo, dbfInfo, cInfo);
                    break;
                }
            }
        }
        isConflict = cInfo.isConflict();
        rootInfo.setConflictInfo(cInfo);
    }

    private void detectElement(BranchFileInfo sbfInfo, BranchFileInfo dbfInfo, ConflictInfo cInfo) {
        List<ElementSlice> sslices = sbfInfo.getAllSlice();
        List<ElementSlice> dslices = dbfInfo.getAllSlice();

        for (ElementSlice sslice : sslices) {
            for (ElementSlice dslice : dslices) {
                if (ElementSlice.equalSliceName(sslice, dslice)) {
                    cInfo.addConflictElements(sslice, dslice);
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
