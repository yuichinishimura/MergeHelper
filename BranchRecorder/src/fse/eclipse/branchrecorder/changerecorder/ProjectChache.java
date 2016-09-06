package fse.eclipse.branchrecorder.changerecorder;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public class ProjectChache {

    private static ProjectChache instance;

    /**
     * projName: xmlからの名前 <br>
     * projName != project.getName()
     */
    private String projName;

    /**
     * project: 実際のProject名 <br>
     * projName != project.getName()
     */
    private IProject project;

    private boolean isGitProject;
    private String branchName;

    private ProjectChache() {
    }

    public static ProjectChache getInstance() {
        if (instance == null) {
            instance = new ProjectChache();
        }
        return instance;
    }

    public void init(String projName) {
        this.projName = projName;
        isGitProject = false;
        branchName = "";

        IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(projName);
        if (p == null) {
            return;
        }

        File file = null;
        try {
            file = new File(p.getLocation().toString());
        } catch (NullPointerException e) {
            return;
        }
        File gParent = file.getParentFile().getParentFile();

        FileRepositoryBuilder b = new FileRepositoryBuilder();
        b.addCeilingDirectory(gParent);
        b.findGitDir(file);

        try {
            Git git = Git.open(b.getGitDir());
            Repository repository = git.getRepository();

            project = p;
            isGitProject = true;
            branchName = repository.getBranch();
        } catch (RepositoryNotFoundException | NullPointerException rn) {
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isGitProject() {
        return isGitProject;
    }

    public String getBranchName() {
        return branchName;
    }

    public IProject getProject() {
        return project;
    }

    public void refreshIProject() {
        refreshIProject(project);
    }

    public static void refreshIProject(IProject p) {
        try {
            p.refreshLocal(IResource.DEPTH_INFINITE, null);
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }

    public boolean equals(String projName2) {
        if (projName == null) {
            return false;
        }
        return projName.equals(projName2);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Project: ");
        if (projName != null && projName.length() > 0) {
            sb.append(projName);
        } else {
            sb.append("null");
        }

        sb.append("  isGitProject: ");
        if (isGitProject) {
            sb.append("true  Branch: " + branchName);
        } else {
            sb.append("false");
        }
        return sb.toString();
    }
}
