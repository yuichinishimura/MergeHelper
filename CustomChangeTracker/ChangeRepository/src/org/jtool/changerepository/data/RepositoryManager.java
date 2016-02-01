/*
 *  Copyright 2015
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changerepository.data;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IWorkbenchWindow;
import org.jtool.changerecorder.history.OperationHistory;
import org.jtool.changerecorder.operation.IOperation;
import org.jtool.changerecorder.util.XmlFileStream;
import org.jtool.changerepository.Activator;
import org.jtool.changerepository.event.RepositoryChangedEvent;
import org.jtool.changerepository.event.RepositoryEventSource;
import org.jtool.changerepository.operation.UnifiedOperation;
import org.w3c.dom.Document;

import fse.eclipse.branchrecorder.commit.history.MH_Xml2Operation;

/**
 * Collects information on a workspace and elements (projects, packages, and files) under it.
 * @author Katsuhisa Maruyama
 */
public class RepositoryManager {

    /**
     * The single instance of this repository manager.
     */
    private static RepositoryManager singleton = new RepositoryManager();

    /**
     * The map stores projects currently existing in this repository.
     */
    private Map<String, ProjectInfo> projectInfoMap = new HashMap<String, ProjectInfo>();

    /**
     * The map stores packages currently existing in this repository.
     */
    private Map<String, PackageInfo> packageInfoMap = new HashMap<String, PackageInfo>();

    /**
     * The map stores files currently existing in this repository.
     */
    private Map<String, FileInfo> fileInfoMap = new HashMap<String, FileInfo>();

    /**
     * The information on the workspace, which indicates either internal one or external one.
     */
    private WorkspaceInfo workspaceInfo;

    /**
     * The information on the internal workspace, which indicates Eclipse's default workspace.
     */
    private WorkspaceInfo internalWorkspaceInfo;

    /**
     * The information on the external workspace, which is explicitly specified by users.
     */
    private WorkspaceInfo externalWorkspaceInfo;

    /**
     * Creates an empty object.
     */
    private RepositoryManager() {
    }

    /**
     * Returns the single instance of this repository manager.
     * @return the single instance
     */
    public static RepositoryManager getInstance() {
        return singleton;
    }

    /**
     * Returns information on the current workspace.
     * @return workspace information
     */
    public WorkspaceInfo getWorkspaceInfo() {
        return workspaceInfo;
    }

    /**
     * Sets the internal workspace as the current one.
     */
    public void setInternalWorkspace() {
        workspaceInfo = internalWorkspaceInfo;
    }

    /**
     * Sets the external workspace as the current one.
     */
    public void setExternalWorkspace() {
        workspaceInfo = externalWorkspaceInfo;
    }

    /**
     * Collects all operations stored in the history files existing in a specified directory.
     * @param path the top path for the directory storing the history files
     */
    public void collectOperationsInDefaultPath() {
        String defaultDirPath = OperationHistory.getOperationHistoryDirPath().toString();
        internalWorkspaceInfo = registOperations(defaultDirPath);
    }

    /**
     * Collects all operations stored in the history files existing in a specified directory.
     * @param path the top path for the directory storing the history files
     */
    public void collectOperationsInRepository(String path) {
        if (externalWorkspaceInfo != null) {
            externalWorkspaceInfo.clear();
        }
        externalWorkspaceInfo = registOperations(path);
    }

    /**
     * Collects all operations stored in the history files existing in a specified directory.
     * @param path the top path for the directory storing the history files
     * @return the information on the workspace
     */
    private WorkspaceInfo registOperations(String path) {
        if (path == null) {
            return null;
        }

        List<File> files = getAllHistoryFiles(path);
        if (files.size() == 0) {
            return null;
        }

        workspaceInfo = collectOperations(path, files);

        if (workspaceInfo != null) {
            RepositoryChangedEvent evt = new RepositoryChangedEvent(this);
            RepositoryEventSource.getInstance().fire(evt);
        }

        return workspaceInfo;
    }

    /**
     * Clears all information related to the whole repository.
     */
    private void clearAllInfo() {
        projectInfoMap.clear();
        packageInfoMap.clear();
        fileInfoMap.clear();
    }

    /**
     * Collects all operations stored in history files existing in a specified directory.
     * @param path the top path for the directory storing the history files
     * @param files the collection of history files storing the operations
     * @return the information on the workspace
     */
    private WorkspaceInfo collectOperations(String path, final List<File> files) {
        final WorkspaceInfo workspaceInfo = new WorkspaceInfo(path);
        try {
            IWorkbenchWindow window = Activator.getWorkbenchWindow();
            window.run(false, true, new IRunnableWithProgress() {

                /**
                 * Reads history files existing in the specified directory.
                 * @param monitor the progress monitor to use to display progress and receive requests for cancellation
                 * @exception InterruptedException if the operation detects a request to cancel
                 */
                @Override
                public void run(IProgressMonitor monitor) throws InterruptedException {
                    monitor.beginTask("Extracting operations", files.size() * 2);

                    List<UnifiedOperation> ops = readHistoryFiles(files, monitor);
                    workspaceInfo.setOperations(ops);

                    registOperations(workspaceInfo, ops, monitor);

                    workspaceInfo.setTimeRange();
                    workspaceInfo.fixMismatches();

                    monitor.done();
                }
            });

        } catch (InvocationTargetException e) {
            e.printStackTrace();
            System.err.println(e.getCause());
            clearAllInfo();
            return null;

        } catch (InterruptedException e) {
            System.err.println(e.getCause());
            clearAllInfo();
            return null;
        }

        clearAllInfo();

        return workspaceInfo;
    }

    /**
     * Returns all descendant history files of a specified directory.
     * @param path the path of the specified directory
     * @return the descendant files
     */
    private List<File> getAllHistoryFiles(String path) {
        List<File> files = new ArrayList<File>();

        File dir = new File(path);
        if (dir.isFile()) {
            if (path.endsWith(".xml")) {
                files.add(dir);
            }
        } else if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            for (File f : children) {
                files.addAll(getAllHistoryFiles(f.getPath()));
            }
        }

        return files;
    }

    /**
     * Reads the history files.
     * @param files the collection of the history files
     * @param monitor the progress monitor to use to display progress and receive requests for cancellation
     * @return the collection of all the operations stored in the history files
     * @throws InterruptedException if the operation detects a request to cancel or any failure
     */
    private List<UnifiedOperation> readHistoryFiles(List<File> files, IProgressMonitor monitor) throws InterruptedException {
        List<UnifiedOperation> operations = new ArrayList<UnifiedOperation>(65536);
        for (File file : files) {
            String fpath = file.getAbsolutePath();
            Document doc = null;
            try {
                doc = XmlFileStream.read(fpath);
            } catch (Exception e) {
                throw new InterruptedException("Fails to read the history files " + fpath);
            }

            OperationHistory history = MH_Xml2Operation.convert(doc);

            if (history == null) {
                throw new InterruptedException("Fails to convert the history files " + fpath);
            }

            for (int i = 0; i < history.size(); i++) {
                IOperation op = history.getOperation(i);
                List<UnifiedOperation> ops = UnifiedOperation.create(op);
                operations.addAll(ops);
            }

            if (monitor.isCanceled()) {
                monitor.done();
                throw new InterruptedException("User interrupted");
            }

            monitor.worked(1);
        }

        return operations;
    }

    /**
     * Registers operations on information of their respective files, packages, and projects.
     * @param winfo the information on the workspace
     * @param ops the collection of all the operations stored in the history files
     * @param monitor the progress monitor to use to display progress and receive requests for cancellation
     * @throws InterruptedException if the operation detects a request to cancel
     */
    private void registOperations(WorkspaceInfo winfo, List<UnifiedOperation> ops, IProgressMonitor monitor) throws InterruptedException {
        for (int idx = 0; idx < ops.size(); idx++) {
            UnifiedOperation op = ops.get(idx);

            String filePath = op.getFile();
            String projectName = getProjectName(filePath);
            String packageName = getPackageName(filePath);
            String fileName = getFileName(filePath);

            if (op.isResourceOperation()) {
                resourecChange(winfo, op, projectName, packageName, fileName);

            } else {
                registOperation(winfo, op, projectName, packageName, fileName);
            }

            if (monitor.isCanceled()) {
                monitor.done();
                throw new InterruptedException();
            }

            monitor.worked(1);
        }
    }

    /**
     * Registers resource change operations on information of their respective files.
     * @param winfo the information on the workspace
     * @param op the operation to be stored
     * @param projectName the name of the project related to the stored operation
     * @param packageName the name of the package related to the stored operation
     * @param fileName the name of the file related to the stored operation
     */
    private void resourecChange(WorkspaceInfo winfo, UnifiedOperation op, String projectName, String packageName, String fileName) {
        if (op.isFileResourceOperation()) {
            if (op.isResourceAddedOperation()) {
                registOperation(winfo, op, projectName, packageName, fileName);

            } else if (op.isResourceRemovedOperation()) {
                registOperation(winfo, op, projectName, packageName, fileName);
                fileInfoMap.remove(fileName);

            } else if (op.isResourceMovedToOperation() || op.isResourceRenamedToOperation()) {
                registOperation(winfo, op, projectName, packageName, fileName);

            } else if (op.isResourceMovedFromOperation() || op.isResourceRenamedFromOperation()) {
                String oldPath = op.getIdenticalPath();
                String oldFileName = getFileName(oldPath);
                FileInfo oldFileInfo = fileInfoMap.get(oldFileName);
                fileInfoMap.remove(oldFileName);

                registOperation(winfo, op, projectName, packageName, fileName);
                FileInfo newFileInfo = fileInfoMap.get(fileName);

                oldFileInfo.setFileInfoTo(newFileInfo);
                newFileInfo.setFileInfoFrom(oldFileInfo);
            }
        }
    }

    /**
     * Registers operations on information of their respective files, packages, and projects.
     * @param winfo the information on the workspace
     * @param op the operation to be stored
     * @param projectName the name of the project related to the stored operation
     * @param packageName the name of the package related to the stored operation
     * @param fileName the name of the file related to the stored operation
     */
    private void registOperation(WorkspaceInfo winfo, UnifiedOperation op, String projectName, String packageName, String fileName) {
        ProjectInfo projectInfo = projectInfoMap.get(getProjectKey(projectName));
        if (projectInfo == null) {
            projectInfo = new ProjectInfo(projectName, winfo);
            projectInfoMap.put(getProjectKey(projectName), projectInfo);

            winfo.addProjectInfo(projectInfo);
        }

        PackageInfo packageInfo = packageInfoMap.get(getPackageKey(projectName, packageName));
        if (packageInfo == null) {
            packageInfo = new PackageInfo(packageName, projectInfo);
            packageInfoMap.put(getPackageKey(projectName, packageName), packageInfo);

            projectInfo.addPackageInfo(packageInfo);
        }

        FileInfo fileInfo = fileInfoMap.get(getFileKey(projectName, packageName, fileName));
        if (fileInfo == null) {
            fileInfo = new FileInfo(fileName, op.getFile(), projectInfo, packageInfo);
            fileInfoMap.put(getFileKey(projectName, packageName, fileName), fileInfo);

            winfo.addFileInfo(fileInfo);
            projectInfo.addFileInfo(fileInfo);
            packageInfo.addFileInfo(fileInfo);
        }
        op.setFileInfo(fileInfo);
        fileInfo.addOperation(op);
    }

    /**
     * Returns the key for retrieval of project information.
     * @param projectName the project name
     * @return the key string for the project
     */
    private String getProjectKey(String projectName) {
        return projectName;
    }

    /**
     * Returns the key for retrieval of package information.
     * @param projectName the project name
     * @param packageName the package name
     * @return the key string for the package
     */
    private String getPackageKey(String projectName, String packageName) {
        return projectName + "%" + packageName;
    }

    /**
     * Returns the key for retrieval of file information.
     * @param projectName the project name
     * @param packageName the package name
     * @param fileName the file name
     * @return the key string for the file
     */
    private String getFileKey(String projectName, String packageName, String fileName) {
        return projectName + "%" + packageName + "%" + fileName;
    }

    /**
     * Returns the name of the project under a given path.
     * @param the path for the file
     * @return the project name
     */
    private String getProjectName(String path) {
        int firstIndex = path.indexOf('/', 1);
        if (firstIndex == -1) {
            return "Unknown";
        }

        return path.substring(1, firstIndex);
    }

    /**
     * Return the name of the package under a given path.
     * @param path the path for the file
     * @return the package name
     */
    private String getPackageName(String path) {
        final String SRCDIR = "/src/";
        int firstIndex = path.indexOf(SRCDIR);
        int lastIndex = path.lastIndexOf('/') + 1;
        if (firstIndex == -1 || lastIndex == -1) {
            return "Unknown";
        }

        if (firstIndex + SRCDIR.length() == lastIndex) {
            return "(default package)";
        }

        String name = path.substring(firstIndex + SRCDIR.length(), lastIndex - 1);
        return name.replace('/', '.');
    }

    /**
     * Return the name of the file under a given path.
     * @param path the path for the file
     * @return the file name without its path information
     */
    private String getFileName(String path) {
        if (path == null) {
            return "Unknown";
        }

        int lastIndex = path.lastIndexOf('/') + 1;
        if (lastIndex == -1) {
            return path;
        }
        return path.substring(lastIndex);
    }
}
