package fse.eclipse.branchrecorder.commit;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jgit.lib.Repository;
import org.jtool.editrecorder.util.EditorUtilities;
import org.jtool.editrecorder.util.WorkspaceUtilities;

import fse.eclipse.branchrecorder.util.RepositoryUtil;

public class CodeMapManager {
    private static IWorkspaceRoot workspace = WorkspaceUtilities.getWorkspace().getRoot();

    /**
     * 指定リポジトリからマップ(key:ソースコードパス value:ソースコード)を作成する
     * @param repository 指定リポジトリ
     * @return ソースコードマップ
     * @throws CoreException
     */
    public static Map<String, String> generateCodeMap(Repository repository) throws CoreException {
        IProject project = RepositoryUtil.convertIProject(repository);
        if (project == null) {
            return null;
        }

        Map<String, String> returnCodeMap = new HashMap<String, String>();
        project.accept(new IResourceVisitor() {
            @Override
            public boolean visit(IResource resource) throws CoreException {
                int type = resource.getType();
                if (type == IResource.FILE) {
                    if (resource.getName().endsWith(".java")) {
                        String path = resource.getFullPath().toOSString();
                        String code = getSourceCode(path);
                        returnCodeMap.put(path, code);
                    }
                    return false;
                }
                return true;
            }
        });
        return returnCodeMap;
    }

    private static String getSourceCode(String path) {
        return EditorUtilities.getSourceCode(workspace.getFile(new Path(path)));
    }
}
