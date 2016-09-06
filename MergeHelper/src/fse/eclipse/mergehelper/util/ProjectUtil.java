package fse.eclipse.mergehelper.util;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

import fse.eclipse.branchrecorder.util.PathUtil;

public class ProjectUtil {
    private static String HISTORY_DIR_NAME = PathUtil.getHistoryDirName();
    private static IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();

    /**
     * Workspaceに存在する全てのプロジェクトをIProjectで返す
     * @return 存在するIProject[]
     */
    public static IProject[] getProjects() {
        return workspace.getProjects();
    }

    public static IProject convertIProject(String projectName) {
        return workspace.getProject(projectName);
    }

    public static String getProjectPath(IProject project) {
        return getProjectIPath(project).toString();
    }

    public static IPath getProjectIPath(IProject project) {
        return project.getLocation();
    }

    public static boolean hasMultipleBranchHistory(IProject project) {
        File pFile = new File(getProjectHistoryPath(project));
        if (!pFile.exists()) {
            System.err.println("not found history folder: " + project.getName());
            return false;
        }

        File[] branchDirs = pFile.listFiles();
        if (branchDirs == null) {
            return false;
        }

        int count = 0;
        for (File dir : branchDirs) {
            if (dir.isDirectory()) {
                if (hasXmlFile(dir)) {
                    count++;
                    if (count >= 2) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static List<String> getBranchHistoryNameList(IProject project) {
        List<String> nameList = new LinkedList<String>();

        File[] branchDirs = new File(getProjectHistoryPath(project)).listFiles();
        for (File dir : branchDirs) {
            if (dir.isDirectory()) {
                nameList.add(dir.getName());
            }
        }
        return nameList;
    }

    public static String getProjectHistoryPath(String projectName) {
        return getProjectHistoryPath(convertIProject(projectName));
    }

    public static String getProjectHistoryPath(IProject project) {
        IPath path = getProjectIPath(project);
        return path.append(File.separator).append(HISTORY_DIR_NAME).toString();
    }

    private static boolean hasXmlFile(File dir) {
        File[] fileList = dir.listFiles();
        if (fileList == null) {
            return false;
        }

        for (File file : fileList) {
            if (file.isFile()) {
                if (file.getName().endsWith(".xml")) {
                    return true;
                }
            }
        }
        return false;
    }
}
