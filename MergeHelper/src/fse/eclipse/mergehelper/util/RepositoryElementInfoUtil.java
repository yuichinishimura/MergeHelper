package fse.eclipse.mergehelper.util;

import org.jtool.changerepository.data.FileInfo;
import org.jtool.changerepository.data.PackageInfo;
import org.jtool.changerepository.data.ProjectInfo;
import org.jtool.changerepository.data.RepositoryElementInfo;
import org.jtool.changerepository.operation.UnifiedOperation;

import fse.eclipse.mergehelper.Attr;

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

        int idx = name.indexOf(Attr.BRANCH_NAME_MARK);
        if (idx != -1) {
            idx += Attr.BRANCH_NAME_MARK.length();
            return name.substring(idx);
        }
        return null;
    }

    public static String getProjectName(ProjectInfo pInfo) {
        String name = pInfo.getName();
        int idx = name.indexOf(Attr.BRANCH_NAME_MARK);
        if (idx != -1) {
            return name.substring(0, idx);
        }
        return name;
    }

    public static String getFileNameExceptExtension(FileInfo fInfo) {
        String name = fInfo.getName();
        int idx = name.indexOf(".");
        return name.substring(0, idx);
    }

    public static int getJustBeforeFileId(FileInfo fInfo, int id) {
        UnifiedOperation op = fInfo.getProjectInfo().getOperation(id - 1);
        long time = op.getTime();

        int size = fInfo.getOperationNumber();
        for (int i = size - 1; i >= 0; i--) {
            UnifiedOperation fop = fInfo.getOperation(i);
            long ftime = fop.getTime();
            if (ftime <= time) {
                return fop.getId();
            }
        }
        return fInfo.getOperation(0).getId();
    }
}
