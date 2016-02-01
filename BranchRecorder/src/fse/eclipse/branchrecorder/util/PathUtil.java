package fse.eclipse.branchrecorder.util;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.jtool.changerecorder.history.OperationHistory;

public class PathUtil {

    /**
     * History格納ディレクトリ名を返す
     * @return History格納ディレクトリ名
     */
    public static String getHistoryDirName() {
        String historyDirPath = getOperationHistoryDirPath();
        int indexOf = historyDirPath.lastIndexOf(File.separator);
        if (indexOf == -1) {
            indexOf = historyDirPath.lastIndexOf("/");
        }
        return historyDirPath.substring(indexOf + 1);
    }

    /**
     * Historyディレクトリの絶対パスを返す
     * @return {@link org.jtool.changerecorder.history.OperationHistory#getOperationHistoryDirPath()}
     */
    public static String getOperationHistoryDirPath() {
        return OperationHistory.getOperationHistoryDirPath();
    }

    /**
     * Workspaceの絶対パスを返す
     * @return Workspaceの絶対パス
     */
    public static String getWorkspacePath() {
        return ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
    }

    public static IPath getProjectIPath(String projName) {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projName);
        return project.getLocation();
    }

    /**
     * プロジェクト名とブランチ名からHistoryファイルを格納する絶対パスを構成し，返す
     * @return Historyファイル格納先の絶対パス
     */
    public static String getProjectBranchPath(IProject project, String branchName) {
        IPath path = project.getLocation();
        path = path.append(File.separator).append(getHistoryDirName());
        path = path.append(File.separator).append(branchName);
        return path.toString();
    }

    public static boolean isEqualPath(String path, String path2) {
        path = path.replace("/", File.separator);
        path = path.replace("\\", File.separator);

        path2 = path2.replace("/", File.separator);
        path2 = path2.replace("\\", File.separator);

        return path.equals(path2);
    }
}
