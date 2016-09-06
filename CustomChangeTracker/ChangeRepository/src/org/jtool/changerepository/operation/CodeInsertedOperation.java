package org.jtool.changerepository.operation;

import org.jtool.changerecorder.operation.FileOperation;
import org.jtool.changerecorder.operation.IOperation;
import org.jtool.changerecorder.operation.IOperation.Type;
import org.jtool.changerecorder.operation.ResourceOperation.Target;
import org.jtool.changerecorder.operation.NormalOperation;
import org.jtool.changerecorder.operation.ResourceOperation;
import org.jtool.changerepository.data.FileInfo;
import org.jtool.changerepository.operation.UnifiedOperation;

public class CodeInsertedOperation extends UnifiedOperation {
    private final String code;

    public CodeInsertedOperation(FileInfo fInfo, String code) {
        super(null);
        this.code = code;
        setProjectInfo(fInfo.getProjectInfo());
        setPackageInfo(fInfo.getPackageInfo());
        setFileInfo(fInfo);
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getName());
        sb.append(":\n");
        sb.append(code);
        return sb.toString();
    }

    public static boolean isCodeInsertedOperation(UnifiedOperation op) {
        return op instanceof CodeInsertedOperation;
    }

    @Override
    public boolean isNormalOperation() {
        return false;
    }

    @Override
    public boolean isCompoundOperation() {
        return false;
    }

    @Override
    public boolean isCopyOperation() {
        return false;
    }

    @Override
    public boolean isFileOperation() {
        return false;
    }

    @Override
    public boolean isMenuOperation() {
        return false;
    }

    @Override
    public boolean isResourceOperation() {
        return false;
    }

    @Override
    public boolean isNullOperation() {
        return false;
    }

    @Override
    public boolean isTextEditOperation() {
        return false;
    }

    @Override
    public boolean isTextChangedOperation() {
        return false;
    }

    @Override
    public int getStart() {
        return -1;
    }

    @Override
    public void setStart(int start) {
        // no execute
    }

    @Override
    public String getInsertedText() {
        return "";
    }

    @Override
    public void setInsertedText(String text) {
        // no execute
    }

    @Override
    public String getDeletedText() {
        return "";
    }

    @Override
    public void setDeletedText(String text) {
        // no execute
    }

    @Override
    public String getCopiedText() {
        return "";
    }

    @Override
    public String getCutCopiedText() {
        return "";
    }

    @Override
    public int maxDeletedOrCopiedTextLength() {
        return 0;
    }

    @Override
    public int maxInsertedTextLength() {
        return 0;
    }

    @Override
    public String getFile() {
        return getFileInfo().getFilePath();
    }

    @Override
    public long getTime() {
        return 0;
    }

    @Override
    public void setTime(long time) {
        // no execute
    }

    @Override
    public String getTimeString() {
        return "0";
    }

    @Override
    public int getSequenceNumber() {
        return 0;
    }

    @Override
    public IOperation getIOperation() {
        return null;
    }

    @Override
    public Type getOperationType() {
        return null;
    }

    @Override
    protected NormalOperation.Type getEditActionType() {
        return null;
    }

    @Override
    public boolean isCutOperation() {
        return false;
    }

    @Override
    public boolean isPasteOperation() {
        return false;
    }

    @Override
    public boolean isUndoOperation() {
        return false;
    }

    @Override
    public boolean isRedoOperation() {
        return false;
    }

    @Override
    public boolean isDiffOperation() {
        return false;
    }

    @Override
    protected FileOperation.Type getFileActionType() {
        return null;
    }

    @Override
    public boolean isFileNewOperation() {
        return false;
    }

    @Override
    public boolean isFileOpenOperation() {
        return false;
    }

    @Override
    public boolean isFileCloseOperation() {
        return false;
    }

    @Override
    public boolean isFileSaveOperation() {
        return false;
    }

    @Override
    public boolean isFileDeleteOperation() {
        return false;
    }

    @Override
    public boolean isFileActivationOperation() {
        return false;
    }

    @Override
    public String getAuthor() {
        return "";
    }

    @Override
    protected String getLabel() {
        return "";
    }

    @Override
    protected ResourceOperation.Type getResourceChangeType() {
        return null;
    }

    @Override
    public boolean isResourceAddedOperation() {
        return false;
    }

    @Override
    public boolean isResourceRemovedOperation() {
        return false;
    }

    @Override
    public boolean isResourceMovedFromOperation() {
        return false;
    }

    @Override
    public boolean isResourceMovedToOperation() {
        return false;
    }

    @Override
    public boolean isResourceMovedOperation() {
        return false;
    }

    @Override
    public boolean isResourceRenamedFromOperation() {
        return false;
    }

    @Override
    public boolean isResourceRenamedToOperation() {
        return false;
    }

    @Override
    public boolean isResourceRenamedOperation() {
        return false;
    }

    @Override
    protected Target getResourceTarget() {
        return null;
    }

    @Override
    public boolean isProjectResourceOperation() {
        return false;
    }

    @Override
    public boolean isPackageResourceOperation() {
        return false;
    }

    @Override
    public boolean isFileResourceOperation() {
        return false;
    }

    @Override
    public boolean isCommitOpeartion() {
        return false;
    }

    @Override
    public String getIdenticalPath() {
        return "";
    }
}
