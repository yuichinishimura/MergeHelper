/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changerepository.data;

import org.jtool.changerepository.operation.OperationManager;
import org.jtool.changerepository.operation.UnifiedOperation;
import org.jtool.changerecorder.diff.DiffOperationGenerator;
import org.jtool.changerecorder.operation.NormalOperation;
import java.util.List;
import java.util.ArrayList;

/**
 * Stores information on a file.
 * @author Katsuhisa Maruyama
 */
public class FileInfo extends RepositoryElementInfo {
    
    /**
     * The path string representing this file.
     */
    private String path;
    
    /**
     * The project containing this file.
     */
    private ProjectInfo projectInfo;
    
    /**
     * The package containing this file.
     */
    private PackageInfo packageInfo;
    
    /**
     * The operation history for this file.
     */
    private OperationManager operationManager;
    
    /**
     * The time when the file information was last generated or modified.
     */
    private long lastModifiedTime;
    
    /**
     * The information on the file which exists before the file rename or move.
     */
    private FileInfo fileInfoFrom = null;
    
    /**
     * The information on the file which exists after the file rename or move.
     */
    private FileInfo fileInfoTo = null;
    
    /**
     * Creates an instance that stores information on this file.
     * @param name the name of this file
     * @param path the path for this file
     * @param pinfo the information on the project containing this file
     * @param painfo the information on the package containing this file
     */
    public FileInfo(String name, String path, ProjectInfo pinfo, PackageInfo painfo) {
        super(name);
        this.path = path;
        this.projectInfo = pinfo;
        this.packageInfo = painfo;
    }
    
    /**
     * Returns the path for this file.
     * @return the file path
     */
    public String getFilePath() {
        return path;
    }
    
    /**
     * Sets the time range for this file.
     */
    protected void setTimeRange() {
        operationManager = new OperationManager(this);
        operations = operationManager.createOperationInfo(operations);
        
        super.setTimeRange();
    }
    
    /**
     * Sets the information on the file which exists before the file rename or move.
     * @param finfo the previous file information which is backward connected to this file
     */
    public void setFileInfoFrom(FileInfo finfo) {
        fileInfoFrom = finfo;
    }
    
    /**
     * Returns the information on the file which exists before the file rename or move.
     * @return the previous file information
     */
    public FileInfo getFileInfoFrom() {
        return fileInfoFrom;
    }
    
    /**
     * Sets the information on the file which exists after the file rename or move.
     * @param finfo the next file information which is forward connected to this file
     */
    public void setFileInfoTo(FileInfo finfo) {
        fileInfoTo = finfo;
    }
    
    /**
     * Returns the information on the file which exists after the file rename or move.
     * @return the next file information
     */
    public FileInfo getFileInfoTo() {
        return fileInfoTo;
    }

    /**
     * Returns the unique key for this file.
     * @return the unique key
     */
    public String getKey() {
        return getKey(getQualifiedName(), getTimeFrom(), getTimeTo());
    }
    
    /**
     * Returns the qualified name of this file.
     * @return the qualified name of the file
     */
    public String getQualifiedName() {
        String paname = packageInfo.getName();
        if (paname.length() == 0) {
            return projectInfo.getName() + "#" + getName();
        }
        return projectInfo.getName() + "#" + paname + "." + getName();
    }
    
    /**
     * Returns the information on the project containing this file. 
     * @return the project information on this file
     */
    public ProjectInfo getProjectInfo() {
        return projectInfo;
    }
    
    /**
     * Returns the information on the package containing this file. 
     * @return the package information on this file
     */
    public PackageInfo getPackageInfo() {
        return packageInfo;
    }
    
    /**
     * Retrieves the operation performed at the specified time and returns it.
     * @param time the specified time
     * @return the found operation, where the first matched one if multiple ones are matched by using the binary search
     */
    public UnifiedOperation getOperationByTime(long time) {
        return operationManager.getOperationByTime(time);
    }
    
    /**
     * Restores source code at the specified time and returns it.
     * @param time the specified time
     * @return the content of the restored source code, or the error message that will be displayed on the editor
     */
    public String getCode(String code, int from, int to) {
        return operationManager.restore(code, from, to);
    }
    
    /**
     * Restores source code when an operation with a given sequence number was performed and returns it.
     * @param idx the sequence number of the specified operation
     * @return the content of the restored source code, or the error message that will be displayed on the editor
     */
    public String getCode(int idx) {
        return operationManager.restore(idx);
    }
    
    /**
     * Returns the number of the restoration points.
     * @return the number of the restoration points
     */
    public int getRestorationPointNumber() {
        return operationManager.getRestorationPointNumber();
    }
    
    /**
     * Sets the time when the file information was last generated or modified
     * @param time the last generated or modified time of this file information
     */
    public void setLastModifiedTime(long time) {
        lastModifiedTime = time;
    }
    
    /**
     * Returns the time when the file information was last generated or modified
     * @return the last generated or modified time of this file information
     */
    public long getLastModifiedTime() {
        return lastModifiedTime;
    }
    
    /**
     * Checks mismatches between two operations.
     * @return <code>true</code> if mismatches were found, otherwise <code>false</code>
     */
    public boolean checkMismatches() {
        boolean errflag = false;
        List<UnifiedOperation> ops = getOperations();
        
        for (int i = 0; i < ops.size(); i++) {
            UnifiedOperation op = ops.get(i);
            
            if (op.isNormalOperation()) {
                String code = getCode(i);
                
                if (code == null) {
                    System.out.println(" -- ERROR IN " + getFilePath() + " " + i);
                    errflag = true;
                    
                    int f = i + 1;
                    for ( ; f < ops.size(); f++) {
                        UnifiedOperation o = ops.get(f);
                        if (o.isFileOperation()) {
                            i = f;
                            break;
                        }
                    }
                    
                    if (f >= ops.size()) {
                        break;
                    }
                }
            }
        }
        return errflag;
    }
    
    /**
     * Fixes mismatches between a close operation and a open one.
     */
    public void fixCloseOpenMismatches() {
        int gap = 1;
        List<UnifiedOperation> original = getOperations();
        List<UnifiedOperation> ops = new ArrayList<UnifiedOperation>(original);
        
        for (int i = 0; i < ops.size(); i++) {
            UnifiedOperation op = ops.get(i);
            
            if (op.isFileCloseOperation()) {
                String closedCode = getCode(i);
                
                if (i + 1 < ops.size()) {
                    UnifiedOperation o = ops.get(i + 1);
                    if (o.isFileOpenOperation()) {
                        String openedCode = getCode(i + 1);
                        
                        if (closedCode != null && closedCode.compareTo(openedCode) != 0) {
                            System.out.println("-- CLOSE/OPEN MISMATCH IN " + getFilePath() + " " + (i + 1) + "FIX IT ...");
                            
                            List<UnifiedOperation> dops = generateDiffOperation(o.getTime(), closedCode, openedCode);
                            original.addAll(i + gap, dops);
                            gap = gap + dops.size();
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Generates diff operations and their unified ones.
     * @param time the time when the open operation was performed
     * @param closedCode the contents of code when the file was closed
     * @param openedCode the contents of code when the file was opened
     * @return the collection of generated operations
     */
    private List<UnifiedOperation> generateDiffOperation(long time, String closedCode, String openedCode) {
        List<UnifiedOperation> ops = new ArrayList<UnifiedOperation>();
        List<NormalOperation> dops = DiffOperationGenerator.generate(time, getFilePath(), closedCode, openedCode);
        for (NormalOperation nop : dops) {
            UnifiedOperation uop = UnifiedOperation.create(nop).get(0);
            uop.setFileInfo(this);
            ops.add(uop);
        }
        return ops;
    }
    
    /**
     * Tests if a given file is equals to this.
     * @param finfo the file to be checked
     * @return <code>true</code> if both the files are the same, otherwise <code>false</code>
     */
    public boolean equals(FileInfo finfo) {
        if (finfo == null) {
            return false;
        }
        
        return getFilePath().compareTo(finfo.getFilePath()) == 0 || getKey().compareTo(finfo.getKey()) == 0;
    }
}
