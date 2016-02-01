/*
 *  Copyright 2015
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changerepository.operation;

import java.util.ArrayList;
import java.util.List;

import org.jtool.changerecorder.operation.AbstractOperation;
import org.jtool.changerecorder.operation.CompoundOperation;
import org.jtool.changerecorder.operation.CopyOperation;
import org.jtool.changerecorder.operation.FileOperation;
import org.jtool.changerecorder.operation.IOperation;
import org.jtool.changerecorder.operation.MenuOperation;
import org.jtool.changerecorder.operation.NormalOperation;
import org.jtool.changerecorder.operation.ResourceOperation;
import org.jtool.changerecorder.util.Time;
import org.jtool.changerepository.data.FileInfo;
import org.jtool.changerepository.data.PackageInfo;
import org.jtool.changerepository.data.ProjectInfo;

import fse.eclipse.branchrecorder.commit.operation.CommitOperation;

/**
 * Manages information on operations to be replayed.
 * @author Katsuhisa Maruyama
 */
public class UnifiedOperation {
    
    /**
     * The original operation recorded by OperationRecorder
     */
    private IOperation operation;
    
    /**
     * The identification number of this operation.
     */
    private int id;
    
    /**
     * The project related to this operation.
     */
    private ProjectInfo projectInfo;
    
    /**
     * The package related to this operation.
     */
    private PackageInfo packageInfo;
    
    /**
     * The file related to this operation.
     */
    private FileInfo fileInfo;
    
    /**
     * Creates an instance storing information on the operation.
     * @param iop the operation recorded by OperationRecorder
     */
    protected UnifiedOperation(IOperation op) {
        this.operation = op;
    }
    
    /**
     * Creates the collection of new instances storing information on a given operation and returns it.
     * @param op the operation actually recorded
     * @return the collection of the created operations.
     * The size of collection is more than one if the compound operation was given, otherwise only one was contained.
     */
    public static List<UnifiedOperation> create(IOperation op) {
        List<UnifiedOperation> ops = new ArrayList<UnifiedOperation>();
        collectOperations(ops, op);
        return ops;
    }
    
    /**
     * Collects all operations dangling a given operation.
     * @param ops the collection of the dangling operations
     * @param op the operation to be flattened
     */
    private static void collectOperations(List<UnifiedOperation> ops, IOperation op) {
        if (op.getOperationType() == IOperation.Type.COMPOUND) {
            CompoundOperation cop = (CompoundOperation)op;
            for (IOperation leaf : cop.getLeaves()) {
                collectOperations(ops, leaf);
            }
            
        } if (op.getOperationType() == IOperation.Type.NORMAL ||
              op.getOperationType() == IOperation.Type.COPY) {
            addOperation(ops, op);
            
        } else if (op.getOperationType() == IOperation.Type.FILE) {
            FileOperation fop = (FileOperation)op;
            if (fop.getActionType() == FileOperation.Type.NEW ||
                fop.getActionType() == FileOperation.Type.OPEN ||
                fop.getActionType() == FileOperation.Type.CLOSE ||
                fop.getActionType() == FileOperation.Type.DELETE) {
                addOperation(ops, op);
                
            }
        } else if (op.getOperationType() == IOperation.Type.MENU) {
        
        } else if (op.getOperationType() == IOperation.Type.RESOURCE) {
        
        } else if (UnifiedOperation.isCommitOperation(op)) {
            addOperation(ops, op);
        }
    }
    
    /**
     * Adds an operation.
     * @param ops the collection of the operations, which contains the added operation
     * @param op the operation to be added
     */
    private static void addOperation(List<UnifiedOperation> ops, IOperation op) {
        UnifiedOperation uop = new UnifiedOperation(op);
        uop.setSequenceNumber(ops);
        ops.add(uop);
    }
    
    /**
     * Sets the sequence number of this operation.
     * @param ops the collection of operations already stored
     */
    private void setSequenceNumber(List<UnifiedOperation> ops) {
        if (ops.size() == 0 || getSequenceNumber() != 0) {
            return;
        }
        
        UnifiedOperation prevOperation = ops.get(ops.size() - 1);
        if (getTime() == prevOperation.getTime()) {
            if (operation instanceof AbstractOperation) {
                AbstractOperation aop = (AbstractOperation)operation;
                aop.setSequenceNumber(prevOperation.getSequenceNumber() + 1);
            }
        }
    }
    
    /**
     * Sets the identification number of this operation.
     * @param id the identification number
     */
    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * Returns the identification number of this operation.
     * @return the identification number
     */
    public int getId() {
        return id;
    }
    
    /**
     * Sets the project information related to this operation.
     * @param pinfo the project information to be stored
     */
    public void setProjectInfo(ProjectInfo pinfo) {
        projectInfo = pinfo;
    }
    
    /**
     * Returns the project information related to this operation.
     * @return the related project information
     */
    public ProjectInfo getProjectInfo() {
        return projectInfo;
    }
    
    /**
     * Sets the package information related to this operation.
     * @param pinfo the package information to be stored
     */
    public void setPackageInfo(PackageInfo pinfo) {
        packageInfo = pinfo;
    }
    
    /**
     * Returns the package information related to this operation.
     * @return the related package information
     */
    public PackageInfo getPackageInfo() {
        return packageInfo;
    }
    
    /**
     * Sets the file information related to this operation.
     * @param pinfo the file information to be stored
     */
    public void setFileInfo(FileInfo finfo) {
        fileInfo = finfo;
    }
    
    /**
     * Returns the file information related to this operation.
     * @return the related file information
     */
    public FileInfo getFileInfo() {
        return fileInfo;
    }
    
    /**
     * Tests if this operation is a normal operation.
     * @return <code>true</code> if this operation is a normal operation
     */
    public boolean isNormalOperation() {
         return operation.getOperationType() == IOperation.Type.NORMAL;
    }
    
    /**
     * Tests if this operation is a compound operation.
     * @return <code>true</code> if this operation is a compound operation
     */
    public boolean isCompoundOperation() {
         return operation.getOperationType() == IOperation.Type.COMPOUND;
    }
    
    /**
     * Tests if this operation is a copy operation.
     * @return <code>true</code> if this operation is a copy operation
     */
    public boolean isCopyOperation() {
         return operation.getOperationType() == IOperation.Type.COPY;
    }
    
    /**
     * Tests if this operation is a file operation.
     * @return <code>true</code> if this operation is a file operation
     */
    public boolean isFileOperation() {
         return operation.getOperationType() == IOperation.Type.FILE;
    }
    
    /**
     * Tests if this operation is a menu operation.
     * @return <code>true</code> if this operation is a menu operation
     */
    public boolean isMenuOperation() {
         return operation.getOperationType() == IOperation.Type.MENU;
    }
    
    /**
     * Tests if this operation is a resource operation.
     * @return <code>true</code> if this operation is a commit operation
     */
    public boolean isResourceOperation() {
         return operation.getOperationType() == IOperation.Type.RESOURCE;
    }
    
    /**
     * Tests if this operation is a null operation.
     * @return <code>true</code> if this operation is a null operation
     */
    public boolean isNullOperation() {
         return operation.getOperationType() == IOperation.Type.NULL;
    }
    
    /**
     * Tests if a given operation edits any text.
     * @return <code>true</code> if the operation is a text edit operation
     */
    public boolean isTextEditOperation() {
        return  operation.isTextEditOperation();
    }
    
    /**
     * Tests if a given operation changes any text.
     * @return <code>true</code> if the operation is a text change operation
     */
    public boolean isTextChangedOperation() {
        IOperation.Type type = operation.getOperationType();
        return type == IOperation.Type.NORMAL ||
               type == IOperation.Type.COMPOUND;
    }
    
    /**
     * Returns the content of source code when its file was opened. 
     * @return the content of the source code
     */
    public String getCode() {
        if (isFileNewOperation() || isFileOpenOperation() || isFileCloseOperation() || isFileDeleteOperation()) {
            FileOperation fop = (FileOperation)operation;
            return fop.getCode();
        } else if (isCommitOpeartion()) {
            CommitOperation cop = (CommitOperation)operation;
            return cop.getCode();
        }
        return null;
    }
    
    /**
     * Returns the leftmost offset of the text affected by this operation.
     * @return the leftmost offset value of the modified text
     */
    public int getStart() {
        if (isNormalOperation()) {
            NormalOperation op = (NormalOperation)operation;
            return op.getStart();
        
        } else if (isCopyOperation()) {
            CopyOperation op = (CopyOperation)operation;
            return op.getStart();
        }
        
        return -1;
    }
    
    /**
     * Returns the content of the text inserted by this operation.
     * @return the content of the inserted text, or the empty string
     */
    public String getInsertedText() {
        if (isNormalOperation()) {
            NormalOperation op = (NormalOperation)operation;
            return op.getInsertedText();
        }
        
        return "";
    }
    
    /**
     * Sets the content of the text inserted by this operation.
     * @param text the content of the inserted text
     */
    public void setInsertedText(String text) {
        if (isNormalOperation()) {
            NormalOperation op = (NormalOperation)operation;
            op.setInsertedText(text);
        }
    }
    
    /**
     * Returns the content of the text deleted by this operation.
     * @return the content of the deleted text, or the empty string
     */
    public String getDeletedText() {
        if (isNormalOperation()) {
            NormalOperation op = (NormalOperation)operation;
            return op.getDeletedText();
        }
        
        return "";
    }
    
    /**
     * Sets the content of the text deleted by this operation.
     * @param text text the content of the deleted text
     */
    public void setDeletedText(String text) {
        if (isNormalOperation()) {
            NormalOperation op = (NormalOperation)operation;
            op.setDeletedText(text);
        }
    }
    
    /**
     * Returns the content of the text copied by this operation.
     * @return the content of the copied text, or the empty string
     */
    public String getCopiedText() {
        if (isCopyOperation()) {
            CopyOperation op = (CopyOperation)operation;
            return op.getCopiedText();
        }
        
        return "";
    }
    
    /**
     * Returns the content of the text cut or copied by this operation.
     * @return the content of the copied text, or the empty string
     */
    public String getCutCopiedText() {
        if (isCutOperation()) {
            NormalOperation op = (NormalOperation)operation;
            return op.getDeletedText();
        }
        
        if (isCopyOperation()) {
            CopyOperation op = (CopyOperation)operation;
            return op.getCopiedText();
        }
        
        return "";
    }
    
    /**
     * Returns the maximum length of the deleted or copied text affected by this operation.
     * @return the maximum length of the deleted text
     */
    public int maxDeletedOrCopiedTextLength() {
        int max = 0;
        if (max < getDeletedText().length()) {
            max = getDeletedText().length();
        }
        if (max < getCopiedText().length()) {
            max = getCopiedText().length();
        }
        return max;
    }
    
    /**
     * Returns the maximum length of the inserted text affected by this operation.
     * @return the maximum length of the inserted text
     */
    public int maxInsertedTextLength() {
        return getInsertedText().length();
    }
    
    /**
     * Returns the maximum length of the text affected by this operation.
     * @return the maximum length of the text
     */
    public int maxTextLength() {
        int dmax = maxDeletedOrCopiedTextLength();
        int imax = maxInsertedTextLength();
        
        if (dmax > imax) {
            return dmax;
        }
        return imax;
    }
    
    /**
     * Returns the path name of the file on which this operation was performed.
     * @return the path name containing the operation
     */
    public String getFile() {
        String path = operation.getFilePath();
        if (path != null && path.endsWith(".java")) {
            return path;
        }
        
        return "";
    }
   
    /**
     * Returns the time when the operation was performed.
     * @return the time of the operation
     */
    public long getTime() {
        return operation.getTime();
    }
    
    /**
     * Returns the time when the operation was performed.
     * @return the time string of the operation
     */
    public String getTimeString() {
        return Time.toUsefulFormat(getTime());
    }
    
    /**
     * Returns the sequence number that indicates the order of operations in the same time.
     * @return the sequence number of this operation
     */
    public int getSequenceNumber() {
        return operation.getSequenceNumber();
    }
    
    /**
     * Returns the interface of the operation recorded by OperationRecorder
     * @return the recorded operation
     */
    public IOperation getIOperation() {
        return operation;
    }
    
    /**
     * Returns the sort of this operation.
     * @return the string indicating the operation sort
     */
    public IOperation.Type getOperationType() {
        return operation.getOperationType();
    }
    
    /**
     * Returns the sort of the edit action of this operation.
     * @return the string indicating the edit operation sort, or <code>null</code> if this is not a normal operation
     */
    protected NormalOperation.Type getEditActionType() {
        if (isNormalOperation()) {
            NormalOperation op = (NormalOperation)operation;
            return op.getActionType();
        }
        return null;
    }
    
    /**
     * Tests if a given operation is a cut operation.
     * @return <code>true</code> if the operation is a cut operation, otherwise <code>false</code>
     */
    public boolean isCutOperation() {
        return getEditActionType() == NormalOperation.Type.CUT;
    }
    
    /**
     * Tests if a given operation is a paste operation.
     * @return <code>true</code> if the operation is a paste operation, otherwise <code>false</code>
     */
    public boolean isPasteOperation() {
        return getEditActionType() == NormalOperation.Type.PASTE;
    }
    
    /**
     * Tests if a given operation is an undo operation.
     * @return <code>true</code> if the operation is an undo operation, otherwise <code>false</code>
     */
    public boolean isUndoOperation() {
        return getEditActionType() == NormalOperation.Type.UNDO;
    }
    
    /**
     * Tests if a given operation is a redo operation.
     * @return <code>true</code> if the operation is a redo operation, otherwise <code>false</code>
     */
    public boolean isRedoOperation() {
        return getEditActionType() == NormalOperation.Type.REDO;
    }
    
    /**
     * Tests if a given operation is a diff operation.
     * @return <code>true</code> if the operation is a diff operation, otherwise <code>false</code>
     */
    public boolean isDiffOperation() {
        return getEditActionType() == NormalOperation.Type.DIFF;
    }
    
    /**
     * Returns the sort of the file action of this operation.
     * @return the string indicating the file action sort, or <code>null</code> if this is not a file operation
     */
    protected FileOperation.Type getFileActionType() {
        if (isFileOperation()) {
            FileOperation op = (FileOperation)operation;
            return op.getActionType();
        }
        return null;
    }
    
    /**
     * Tests if a given operation is a file new operation.
     * @return <code>true</code> if the operation is a file new operation, otherwise <code>false</code>
     */
    public boolean isFileNewOperation() {
        return getFileActionType() == FileOperation.Type.NEW;
    }
    
    /**
     * Tests if a given operation is a file open operation.
     * @return <code>true</code> if the operation is a file open operation, otherwise <code>false</code>
     */
    public boolean isFileOpenOperation() {
        return getFileActionType() == FileOperation.Type.OPEN;
    }
    
    /**
     * Tests if a given operation is a file close operation.
     * @return <code>true</code> if the operation is a file close operation, otherwise <code>false</code>
     */
    public boolean isFileCloseOperation() {
        return getFileActionType() == FileOperation.Type.CLOSE;
    }
    
    /**
     * Tests if a given operation is a file save operation.
     * @return <code>true</code> if the operation is a file save operation, otherwise <code>false</code>
     */
    public boolean isFileSaveOperation() {
        return getFileActionType() == FileOperation.Type.SAVE;
    }
    
    /**
     * Tests if a given operation is a file delete operation.
     * @return <code>true</code> if the operation is a file delete operation, otherwise <code>false</code>
     */
    public boolean isFileDeleteOperation() {
        return getFileActionType() == FileOperation.Type.DELETE;
    }
    
    /**
     * Tests if a given operation is a file activation operation.
     * @return <code>true</code> if the operation is a file activation operation, otherwise <code>false</code>
     */
    public boolean isFileActivationOperation() {
        return getFileActionType() == FileOperation.Type.ACT;
    }
    
    /**
     * Returns the developer name who performed this operation
     * @return the developer name
     */
    public String getAuthor() {
        if (operation.getAuthor() == null) {
            return "unknown";
        } else {
            return operation.getAuthor();
        }
    }
    
    /**
     * Returns the label indicating the name of an Eclipse's operation related to this operation.
     * @return the label string, or an empty string if the original operation has no label
     */
    protected String getLabel() {
        if (isCompoundOperation()) {
            CompoundOperation op = (CompoundOperation)operation;
            return op.getLabel();
            
        } else if (isMenuOperation()) {
            MenuOperation op = (MenuOperation)operation;
            return op.getLabel();
        }
        return "";
    }
    
    /**
     * Returns the change type of this resource operation.
     * @return the string indicating the change type of the operation, or <code>null</code> if this is not a resource operation
     */
    protected ResourceOperation.Type getResourceChangeType() {
        if (isResourceOperation()) {
            ResourceOperation op = (ResourceOperation)operation;
            return op.getActionType();
        }
        return null;
    }
    
    /**
     * Tests if a given operation is related to the added resource.
     * @return <code>true</code> if this is a resource added operation, otherwise <code>false</code>
     */
    public boolean isResourceAddedOperation() {
        return getResourceChangeType() == ResourceOperation.Type.ADDED;
    }
    
    /**
     * Tests if a given operation is related to the removed resource.
     * @return <code>true</code> if this is a resource removed operation, otherwise <code>false</code>
     */
    public boolean isResourceRemovedOperation() {
        return getResourceChangeType() == ResourceOperation.Type.REMOVED;
    }
    
    /**
     * Tests if a given operation is related to the resource moved from another location.
     * @return <code>true</code> if this is a resource moved operation, otherwise <code>false</code>
     */
    public boolean isResourceMovedFromOperation() {
        return getResourceChangeType() == ResourceOperation.Type.MOVED_FROM;
    }
    
    /**
     * Tests if a given operation is related to the resource moved to another location.
     * @return <code>true</code> if this is a resource moved operation, otherwise <code>false</code>
     */
    public boolean isResourceMovedToOperation() {
        return getResourceChangeType() == ResourceOperation.Type.MOVED_TO;
    }
    
    /**
     * Tests if a given operation is related to the resource moved from or to another location.
     * @return <code>true</code> if this is a resource moved operation, otherwise <code>false</code>
     */
    public boolean isResourceMovedOperation() {
        return isResourceMovedFromOperation() || isResourceMovedToOperation(); 
    }
    
    /**
     * Tests if a given operation is related to the resource renamed from another name.
     * @return <code>true</code> if this is a resource renamed operation, otherwise <code>false</code>
     */
    public boolean isResourceRenamedFromOperation() {
        return getResourceChangeType() == ResourceOperation.Type.RENAMED_FROM;
    }
    
    /**
     * Tests if a given operation is related to the resource renamed to another name.
     * @return <code>true</code> if this is a resource renamed operation, otherwise <code>false</code>
     */
    public boolean isResourceRenamedToOperation() {
        return getResourceChangeType() == ResourceOperation.Type.RENAMED_TO;
    }
    
    /**
     * Tests if a given operation is related to the resource renamed from or to another name.
     * @return <code>true</code> if this is a resource renamed operation, otherwise <code>false</code>
     */
    public boolean isResourceRenamedOperation() {
        return isResourceRenamedFromOperation() || isResourceRenamedToOperation(); 
    }
    
    /**
     * Returns the target of this resource operation.
     * @return the string indicating the target of the operation, or <code>null</code> if this is not a resource operation
     */
    protected ResourceOperation.Target getResourceTarget() {
        if (isResourceOperation()) {
            ResourceOperation op = (ResourceOperation)operation;
            return op.getTarget();
        }
        return null;
    }
    
    /**
     * Tests if a given operation is a resource operation related to a project.
     * @return <code>true</code> if the operation is a resource operation for a project
     */
    public boolean isProjectResourceOperation() {
        return getResourceTarget() == ResourceOperation.Target.JPROJECT;
    }
    
    /**
     * Tests if a given operation is a resource operation related to a package.
     * @return <code>true</code> if the operation is a resource operation for a package, otherwise <code>false</code>
     */
    public boolean isPackageResourceOperation() {
        return getResourceTarget() == ResourceOperation.Target.JPACKAGE;
    }
    
    /**
     * Tests if a given operation is a resource operation related to a file.
     * @return <code>true</code> if the operation is a resource operation for a file, otherwise <code>false</code>
     */
    public boolean isFileResourceOperation() {
        return getResourceTarget() == ResourceOperation.Target.JFILE;
    }
    
    public boolean isCommitOpeartion() {
        return operation instanceof CommitOperation;
    }
    
    public static boolean isCommitOperation(IOperation op) {
        return op instanceof CommitOperation;
    }
    
    /**
     * Returns the path of a resource moved/renamed from or to.
     * @return the path of the identical resource
     */
    public String getIdenticalPath() {
        if (isResourceOperation()) {
            ResourceOperation op = (ResourceOperation)operation;
            return op.getIdenticalPath();
        }
        return "";
    }
    
    
    /**
     * Returns the string for printing, which does not contain a new line character at its end. 
     * @return the string for printing
     */
    public String toString() {
        return operation.toString() + " id=" + String.valueOf(id);
    }
    
    /**
     * Returns the string for printing, which does not contain a new line character at its end.
     * @param indent the number of spaces for the indentation.
     * @return the string for printing with indentation.
     */
    public String toString(int indent) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i< indent; i++) {
            buf.append(" ");
        }
        buf.append(toString());
        return buf.toString();
    }
}
