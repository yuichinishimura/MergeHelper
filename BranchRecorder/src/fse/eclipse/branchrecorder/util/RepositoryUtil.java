package fse.eclipse.branchrecorder.util;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.jgit.lib.Repository;
import org.jtool.editrecorder.util.WorkspaceUtilities;

public class RepositoryUtil {

    public static IProject convertIProject(Repository repository) {
        // IWorkspaceRoot#getProject() では拾えきれない場合がある
        IWorkspaceRoot workspace = WorkspaceUtilities.getWorkspace().getRoot();

        String path = repository.getDirectory().getParent();
        for (IProject p : workspace.getProjects()) {
            String path2 = p.getLocation().toOSString();
            if (PathUtil.isEqualPath(path, path2)) {
                return p;
            }
        }
        return null;
    }
}
