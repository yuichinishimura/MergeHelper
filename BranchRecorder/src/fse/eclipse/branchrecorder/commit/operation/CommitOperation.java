package fse.eclipse.branchrecorder.commit.operation;

import org.jtool.changerecorder.operation.AbstractOperation;
import org.jtool.changerecorder.operation.IOperation;
import org.jtool.changerecorder.util.StringComparator;

public class CommitOperation extends AbstractOperation {

    private final String code;
    private final String id;
    private final String parentId;

    public static final IOperation.Type TYPE = IOperation.Type.NULL;

    public CommitOperation(long time, String path, String author, String code, String id, String parentId) {
        super(time, path, AbstractOperation.getUserName());
        this.code = code;
        this.id = id;
        this.parentId = parentId;
    }

    public String getCommitId() {
        return id;
    }

    public String getShortCommitId() {
        if (id.length() >= 8) {
            return id.substring(0, 7);
        } else {
            return id;
        }
    }

    public String getParentCommitId() {
        return parentId;
    }

    public String getShortParentCommitId() {
        if (parentId.length() >= 8) {
            return parentId.substring(0, 7);
        } else {
            return parentId;
        }
    }

    public String getCode() {
        return code;
    }

    @Override
    public IOperation.Type getOperationType() {
        return TYPE;
    }

    @Override
    public boolean equals(IOperation op) {
        if (!(op instanceof CommitOperation)) {
            return false;
        }

        CommitOperation cop = (CommitOperation) op;
        return super.equals(cop) && StringComparator.isSame(code, cop.getCode()) && StringComparator.isSame(id, cop.getCommitId());
    }
}
