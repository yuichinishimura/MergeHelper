package fse.eclipse.mergehelper.util;

import java.util.List;

import org.jtool.changerepository.data.FileInfo;
import org.jtool.changerepository.data.PackageInfo;
import org.jtool.changerepository.data.RepositoryElementInfo;
import org.jtool.changerepository.operation.UnifiedOperation;

import fse.eclipse.mergehelper.detector.BranchHistoryCopier;

public class RepositoryElementInfoUtil {

    public static String getBranchName(RepositoryElementInfo rInfo) {
        String name;
        if (rInfo instanceof PackageInfo) {
            PackageInfo paInfo = (PackageInfo) rInfo;
            name = paInfo.getProjectInfo().getName();
        } else if (rInfo instanceof FileInfo) {
            FileInfo fInfo = (FileInfo) rInfo;
            name = fInfo.getProjectInfo().getName();
        } else {
            name = rInfo.getName();
        }

        int idx = name.indexOf(BranchHistoryCopier.BRANCH_NAME_MARK);
        if (idx != -1) {
            idx += BranchHistoryCopier.BRANCH_NAME_MARK.length();
            return name.substring(idx);
        }
        return null;
    }

    public static String getNameExcludeBranchName(String name) {
        int idx = name.indexOf(BranchHistoryCopier.BRANCH_NAME_MARK);
        if (idx != -1) {
            return name.substring(0, idx);
        }
        return name;
    }

    public static int indexOfJustBeforeFileOperation(FileInfo fInfo, long time) {
        List<UnifiedOperation> ops = fInfo.getOperations();
        for (int i = ops.size() - 1; i >= 0; i--) {
            UnifiedOperation op = ops.get(i);
            if (op.getTime() < time) {
                return i;
            }
        }
        return -1;
    }
}
