/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changerepository.data;

import org.jtool.changerepository.operation.UnifiedOperation;
import java.util.List;
import java.util.ArrayList;

/**
 * Stores information on a project.
 * @author Katsuhisa Maruyama
 */
public class ProjectInfo extends RepositoryElementInfo {
    
    /**
     * The workspace containing this project.
     */
    private WorkspaceInfo workspaceInfo;
    
    /**
     * The collection of all files within this project.
     */
    private List<FileInfo> files = new ArrayList<FileInfo>();
    
    /**
     * The collection of all packages within this project.
     */
    private List<PackageInfo> packages = new ArrayList<PackageInfo>();;
    
    /**
     * Creates an instance that stores information on this project.
     * @param name the name of this project
     * @param winfo the workspace containing this project
     */
    public ProjectInfo(String name, WorkspaceInfo winfo) {
        super(name);
        workspaceInfo = winfo;
    }
    
    /**
     * Returns the workspace containing this project
     * @return the workspace information
     */
    public WorkspaceInfo getWorkspace() {
        return workspaceInfo;
    }
    
    /**
     * Returns the unique key for this project.
     * @return the unique key
     */
    public String getKey() {
        return getKey(getQualifiedName(), getTimeFrom(), getTimeTo());
    }
    
    /**
     * Returns the qualified name of this project.
     * @return the qualified name of the project
     */
    public String getQualifiedName() {
        return getName();
    }
    
    /**
     * Registers the file information on this project. 
     * @param finfo the file information to be stored
     */
    public void addFileInfo(FileInfo finfo) {
        if (!files.contains(finfo)) {
            files.add(finfo);
        }
    }
    
    /**
     * Obtains all the files in this project.
     * @return the collection of the file information
     */
    public List<FileInfo> getAllFileInfo() {
        RepositoryElementInfo.sort(files);
        return files;
    }
    
    /**
     * Registers the package information on this project. 
     * @param finfo the package information to be stored
     */
    public void addPackageInfo(PackageInfo painfo) {
        if (!packages.contains(painfo)) {
            packages.add(painfo);
        }
    }
    
    /**
     * Obtains all the packages in this project.
     * @return the collection of the package information
     */
    public List<PackageInfo> getAllPackageInfo() {
        RepositoryElementInfo.sort(packages);
        return packages;
    }
    
    /**
     * Sets the time range for this project.
     */
    protected void setTimeRange() {
        for (FileInfo finfo : files) {
            for (UnifiedOperation op : finfo.getOperations()) {
                op.setProjectInfo(this);
                addOperation(op);
                op.setId(getOperationNumber());
            }
        }
        
        super.setTimeRange();
    }
    
    /**
     * Tests if a given project is equals to this.
     * @param pinfo the project to be checked
     * @return <code>true</code> if both the projects are the same, otherwise <code>false</code>
     */
    public boolean equals(ProjectInfo pinfo) {
        if (pinfo == null) {
            return false;
        }
        
        return getKey().compareTo(pinfo.getKey()) == 0;
    }
}
