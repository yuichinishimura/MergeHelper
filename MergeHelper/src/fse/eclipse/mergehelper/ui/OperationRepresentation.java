package fse.eclipse.mergehelper.ui;

import org.jtool.changerecorder.operation.CopyOperation;
import org.jtool.changerecorder.operation.FileOperation;
import org.jtool.changerecorder.operation.NormalOperation;
import org.jtool.changerepository.operation.CodeInsertedOperation;
import org.jtool.changerepository.operation.UnifiedOperation;

import fse.eclipse.branchrecorder.commit.operation.CommitOperation;

/**
 * {@link org.jtool.changereplayer.ui.HistoryView#createOperationTextualRepresentation(UnifiedOperation)}
 */
public class OperationRepresentation {

    public static String createOperationTextualRepresentation(UnifiedOperation op, int offset) {
        if (op.isNormalOperation()) {
            return createNormalOperationTextualRepresentation((NormalOperation) op.getIOperation(), offset);
        } else if (op.isCopyOperation()) {
            return createCopyOperationTextualRepresentation((CopyOperation) op.getIOperation(), offset);
        } else if (op.isFileOperation()) {
            return createFileOperationTextualRepresentation((FileOperation) op.getIOperation());
        } else if (op.isCommitOpeartion()) {
            return createCommitOperationTextualRepresentation((CommitOperation) op.getIOperation());
        } else if (CodeInsertedOperation.isCodeInsertedOperation(op)) {
            return createAMergeOperationTextualRepresentation((CodeInsertedOperation) op);
        }
        return "";
    }

    public static String createOperationTextualRepresentation(UnifiedOperation op) {
        return createOperationTextualRepresentation(op, op.getStart());
    }

    private static String createNormalOperationTextualRepresentation(NormalOperation op, int offset) {
        StringBuilder sb = new StringBuilder();

        if (op.getActionType() != NormalOperation.Type.NO) {
            sb.append(op.getActionType().toString());
            sb.append(" ");
        }

        sb.append(offset);
        sb.append(" ");

        if (op.getInsertedText().length() > 0) {
            sb.append("ins[");
            sb.append(getText(op.getInsertedText()));
            sb.append("] ");
        }
        if (op.getDeletedText().length() > 0) {
            sb.append("del[");
            sb.append(getText(op.getDeletedText()));
            sb.append("]");
        }

        return sb.toString();
    }

    private static String createCopyOperationTextualRepresentation(CopyOperation op, int offset) {
        StringBuilder sb = new StringBuilder();

        sb.append("COPY ");
        sb.append(offset);
        sb.append(" copied[");
        sb.append(getText(op.getCopiedText()));
        sb.append("]");

        return sb.toString();
    }

    private static String createFileOperationTextualRepresentation(FileOperation op) {
        StringBuilder sb = new StringBuilder();

        if (op.getActionType() != FileOperation.Type.NONE) {
            sb.append(op.getActionType().toString());
            sb.append(" ");
        }

        return sb.toString();
    }

    private static String createCommitOperationTextualRepresentation(CommitOperation op) {
        StringBuilder sb = new StringBuilder();

        sb.append("COMMIT ID:");
        sb.append(op.getShortCommitId());
        sb.append(" PARENT:");
        sb.append(op.getShortParentCommitId());

        return sb.toString();
    }

    private static String createAMergeOperationTextualRepresentation(CodeInsertedOperation op) {
        StringBuilder sb = new StringBuilder();

        sb.append("ArtificialMerge");

        return sb.toString();
    }

    private static String getText(String text) {
        final int LESS_LEN = 9;

        String text2;
        if (text.length() < LESS_LEN + 1) {
            text2 = text;
        } else {
            text2 = text.substring(0, LESS_LEN + 1) + "...";
        }

        return text2.replace('\n', '~');
    }
}
