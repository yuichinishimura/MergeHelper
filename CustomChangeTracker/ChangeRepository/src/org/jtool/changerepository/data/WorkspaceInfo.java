/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changerepository.data;

import org.jtool.changerepository.operation.OperationManager;
import org.jtool.changerepository.operation.UnifiedOperation;
import java.util.List;
import java.util.ArrayList;

/**
 * Stores information on a workspace.
 * @author Katsuhisa Maruyama
 */
public class WorkspaceInfo extends RepositoryElementInfo {
    
    /**
     * The collection of all projects within this workspace.
     */
    private List<ProjectInfo> projects = new ArrayList<ProjectInfo>();
    
    /**
     * The collection of all files within this workspace.
     */
    private List<FileInfo> files = new ArrayList<FileInfo>();
    
    /**
     * The operations related to the whole repository.
     */
    private List<UnifiedOperation> operations;
    
    /**
     * Creates an instance that stores information on this workspace.
     * @param path the path for the root of the workspace
     */
    public WorkspaceInfo(String path) {
        super(path);
    }
    
    /**
     * Clears the information on this workspace
     */
    public void clear() {
        projects.clear();
        files.clear();
        operations.clear();
    }
    
    /**
     * Creates an instance that stores information on this workspace.
     * @param ops all the operations related to this workspace
     */
    public void setOperations(List<UnifiedOperation> ops) {
        operations = ops;
        OperationManager.sort(operations);
    }
    
    /**
     * Sets the time range for workspace, projects, packages, and files.
     */
    public void setTimeRange() {
        super.setTimeRange();
        
        for (FileInfo finfo : files) {
            finfo.setTimeRange();
        }
        
        for (ProjectInfo pinfo : projects) {
            pinfo.setTimeRange();
            
            for (PackageInfo painfo : pinfo.getAllPackageInfo()) {
                painfo.setTimeRange();
            }
        }
    }
    
    /**
     * Fixes mismatches between two operations.
     */
    public void fixMismatches() {
        for (FileInfo finfo : files) {
            finfo.checkMismatches();
            finfo.fixCloseOpenMismatches();
        }
    }
    
    /**
     * Registers the project information.
     * @param pinfo the project information to be stored
     */
    public void addProjectInfo(ProjectInfo pinfo) {
        projects.add(pinfo);
    }
    
    /**
     * Registers the file information.
     * @param finfo the file information to be stored
     */
    public void addFileInfo(FileInfo finfo) {
        files.add(finfo);
    }
    
    /**
     * Obtains project information within this workspace.
     * @return the collection of information on the projects
     */
    public List<ProjectInfo> getAllProjectInfo() {
        RepositoryElementInfo.sort(projects);
        return projects;
    }
    
    /**
     * Obtains file information within this workspace.
     * @return the collection of information on the files
     */
    public List<FileInfo> getAllFileInfo() {
        RepositoryElementInfo.sort(files);
        return files;
    }
    
    /**
     * Returns all the operations performed on files within this workspace.
     * @return the collection of the performed operations
     */
    public List<UnifiedOperation> getOperations() {
        return operations;
    }
    
    /**
     * Returns the number of the operations performed on files within this workspace.
     * @return the number of the operations
     */
    public int getOperationNumber() {
        return operations.size();
    }
    
    /**
     * Obtains the project information with a given key.
     * @param key the key of the project to be retrieved
     * @return the matched project, or <code>null</code> if none
     */
    public ProjectInfo getProjectInfo(String key) {
        for (ProjectInfo pinfo : projects) {
            if (pinfo.getKey().compareTo(key) == 0) {
                return pinfo;
            }
        }
        
        return null;
    }
    
    /**
     * Obtains the file information with a given key.
     * @param key the key of the file to be retrieved
     * @return the matched file, or <code>null</code> if none
     */
    public FileInfo getFileInfo(String key) {
        for (FileInfo finfo : files) {
            if (finfo.getKey().compareTo(key) == 0) {
                return finfo;
            }
        }
        
        return null;
    }
    
    /**
     * Obtains the file information that will be replaced with given file information.
     * @param ofinfo the old file information
     * @return the new file information that contains operations stored in the old information
     */
    public FileInfo getNewFileInfo(FileInfo ofinfo) {
        if (ofinfo == null) {
            return null;
        }
        
        String okey = ofinfo.getKey();
        String opath = ofinfo.getFilePath();
        
        UnifiedOperation oop = ofinfo.getOperation(0);
        for (FileInfo finfo : files) {
            
            if (finfo.getKey().compareTo(okey) != 0 && finfo.getFilePath().compareTo(opath) == 0) {
                UnifiedOperation op = finfo.getOperation(0);
                
                if (oop.getTime() == op.getTime() && oop.getSequenceNumber() == op.getSequenceNumber()) {
                    return finfo;
                }
            }
        }
        return null;
    }
}
