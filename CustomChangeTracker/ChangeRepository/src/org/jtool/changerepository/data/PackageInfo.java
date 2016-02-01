/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changerepository.data;

import org.jtool.changerepository.operation.UnifiedOperation;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores information on a package.
 * @author Katsuhisa Maruyama
 */
public class PackageInfo extends RepositoryElementInfo {
    
    /**
     * The project containing this package.
     */
    private ProjectInfo projectInfo;
    
    /**
     * The collection of all files in this package.
     */
    private List<FileInfo> files = new ArrayList<FileInfo>();
    
    /**
     * Creates an instance that stores information on this package.
     * @param name the name of this package
     * @param pinfo the information on the project containing this project 
     */
    public PackageInfo(String name, ProjectInfo pinfo) {
        super(name);
        this.projectInfo = pinfo;
    }
    
    /**
     * Returns the unique key for this package.
     * @return the unique key
     */
    public String getKey() {
        return getKey(getQualifiedName(), getTimeFrom(), getTimeTo());
    }
    
    /**
     * Returns the qualified name of this package.
     * @return the qualified name of the package
     */
    public String getQualifiedName() {
        String paname = getName();
        if (paname.length() == 0) {
            return projectInfo.getName() + "#";
        }
        return projectInfo.getName() + "#" + paname;
    }
    
    /**
     * Returns the information on the project containing this package. 
     * @return the project information on this package
     */
    public ProjectInfo getProjectInfo() {
        return projectInfo;
    }
    
    /**
     * Registers the file information on this package. 
     * @param finfo the file information to be stored
     */
    public void addFileInfo(FileInfo finfo) {
        if (!files.contains(finfo)) {
            files.add(finfo);
        }
    }
    
    /**
     * Obtains all the files in this package.
     * @return the collection of the file information
     */
    public List<FileInfo> getAllFileInfo() {
        FileInfo.sort(files);
        return files;
    }
    
    /**
     * Sets the time range for this package.
     */
    protected void setTimeRange() {
        for (FileInfo finfo : files) {
            for (UnifiedOperation op : finfo.getOperations()) {
                op.setPackageInfo(this);
                addOperation(op);
            }
        }
        
        super.setTimeRange();
    }
    
    /**
     * Tests if a given package is equals to this.
     * @param painfo the package to be checked
     * @return <code>true</code> if both the packages are the same, otherwise <code>false</code>
     */
    public boolean equals(PackageInfo painfo) {
        if (painfo == null) {
            return false;
        }
        
        return getKey().compareTo(painfo.getKey()) == 0;
    }
}
