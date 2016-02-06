package fse.eclipse.mergehelper.detecter;

import java.io.File;
import java.util.Iterator;

import org.eclipse.core.resources.ResourcesPlugin;
import org.jtool.changerecorder.history.OperationHistory;
import org.jtool.changerecorder.history.XmlConstantStrings;
import org.jtool.changerecorder.operation.FileOperation;
import org.jtool.changerecorder.operation.IOperation;
import org.jtool.changerecorder.operation.NormalOperation;
import org.jtool.changerecorder.util.XmlFileStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import fse.eclipse.branchrecorder.commit.history.MH_Operation2Xml;
import fse.eclipse.branchrecorder.commit.history.MH_Xml2Operation;
import fse.eclipse.branchrecorder.commit.history.XmlCommitWriter;
import fse.eclipse.branchrecorder.commit.operation.CommitOperation;
import fse.eclipse.mergehelper.Activator;
import fse.eclipse.mergehelper.Attr;
import fse.eclipse.mergehelper.element.BranchRootInfo;
import fse.eclipse.mergehelper.element.MergeType;
import fse.eclipse.mergehelper.ui.dialog.ConflictDetectingDialog;
import fse.eclipse.mergehelper.util.ProjectUtil;

public class BranchHistoryCopier extends AbstractDetector {

    private static final String MESSAGE = "Copy History ...";
    private static final String ERROR_MESSAGE = "NULL";
    private static AbstractDetector instance = new BranchHistoryCopier();

    private static final String ENCODING = ResourcesPlugin.getEncoding();

    private BranchHistoryCopier() {
    }

    public static AbstractDetector getInstance() {
        return instance;
    }

    @Override
    public void execute(ConflictDetectingDialog dialog) {
        BranchRootInfo rootInfo = BranchRootInfo.getInstance();
        copy(rootInfo, MergeType.ACCEPT);
        copy(rootInfo, MergeType.JOIN);
    }

    private void copy(BranchRootInfo rootInfo, MergeType type) {
        String branchName = rootInfo.getBranchName(type);

        StringBuilder sb = new StringBuilder();
        sb.append(ProjectUtil.getProjectHistoryPath(rootInfo.getProject()));
        sb.append(File.separator).append(branchName);
        String historyPath = sb.toString();

        if (MergeType.isAccept(type)) {
            int length = sb.length();
            sb.delete(length - branchName.length(), length);
            sb.append(rootInfo.getBranchName(MergeType.JOIN));
            String joinHistoryPath = sb.toString();

            copyFromAcceptDir(historyPath, joinHistoryPath, branchName);
        } else {
            copyFromJoinDir(historyPath, branchName);
        }
    }

    private void copyFromAcceptDir(String historyPath, String joinHistoryPath, String name) {
        String parentId = null;
        File[] files = new File(joinHistoryPath).listFiles();
        for (File file : files) {
            if (file.getName().endsWith(XmlCommitWriter.CommitXmlFileName)) {
                parentId = convertCommitOperation(file).getParentCommitId();
                break;
            }
        }

        if (parentId != null) {
            files = new File(historyPath).listFiles();
            int length = files.length;
            for (int i = length - 1; i >= 0; i--) {
                File file = files[i];
                copyFromFile(file, name);

                if (file.getName().endsWith(XmlCommitWriter.CommitXmlFileName)) {
                    String commitId = convertCommitOperation(file).getCommitId();
                    if (commitId.equals(parentId)) {
                        break;
                    }
                }
            }
        }
    }

    private void copyFromJoinDir(String historyPath, String name) {
        File[] files = new File(historyPath).listFiles();
        for (File file : files) {
            copyFromFile(file, name);
        }
    }

    private void copyFromFile(File file, String name) {
        OperationHistory history = readOperationHistory(file);
        if (history != null && history.size() > 0) {
            Document doc = MH_Operation2Xml.convert(history);
            Document renameDoc = renameFileAttr(doc, name);
            copy(renameDoc, name, file);
        }
    }

    private void copy(Document doc, String name, File file) {
        StringBuilder sb = new StringBuilder();
        sb.append(Activator.getWorkingDirPath()).append(File.separator);
        sb.append(name).append(File.separator);
        sb.append(file.getName());
        XmlFileStream.write(doc, sb.toString(), ENCODING);
    }

    private OperationHistory readOperationHistory(File file) {
        Document doc = XmlFileStream.read(file.getAbsolutePath());
        OperationHistory history = MH_Xml2Operation.convert(doc);
        removeOperationAfterMergeConflicts(history);
        return history;
    }

    private Document renameFileAttr(Document doc, String name) {
        // operation まで下がる
        NodeList nodeList = doc.getFirstChild().getChildNodes();
        int length = nodeList.getLength();
        int i;
        for (i = 0; i < length; i++) {
            if (nodeList.item(i).getNodeName().equals(XmlConstantStrings.OperationsElem)) {
                break;
            }
        }
        return renameFileAttr(doc, nodeList.item(i).getChildNodes(), name);
    }

    private Document renameFileAttr(Document doc, NodeList nodeList, String name) {
        // fileAttributeプロジェクト名の部分にブランチ名を足す
        int length = nodeList.getLength();
        for (int i = 0; i < length; i++) {
            Element e = (Element) nodeList.item(i);
            String fileAttr = e.getAttribute(XmlConstantStrings.FileAttr);
            if (!e.getNodeName().equals(XmlConstantStrings.CompoundOperationElem)) {
                if (!fileAttr.equals("null") && fileAttr.length() != 0) {
                    fileAttr = renameFileAttr(fileAttr, name);
                    e.setAttribute(XmlConstantStrings.FileAttr, fileAttr);
                }
            } else {
                renameFileAttr(doc, e.getChildNodes(), name);
            }
        }
        return doc;
    }

    private String renameFileAttr(String fileAttr, String name) {
        int idx = fileAttr.indexOf("/", 1);
        if (idx == -1) {
            idx = fileAttr.indexOf("\\", 1);
        }

        StringBuilder sb = new StringBuilder(fileAttr);
        sb.insert(idx, Attr.BRANCH_NAME_MARK);
        sb.insert(idx + Attr.BRANCH_NAME_MARK.length(), name);
        return sb.toString();
    }

    private CommitOperation convertCommitOperation(File file) {
        OperationHistory history = readOperationHistory(file);
        return (CommitOperation) history.getFirstOperation();
    }

    private void removeOperationAfterMergeConflicts(OperationHistory history) {
        history.sort();
        Iterator<IOperation> ops = history.getOperations().iterator();
        boolean isAfterFlag = false;
        while (ops.hasNext()) {
            IOperation op = ops.next();
            if (!isAfterFlag) {
                if (op.getOperationType() == IOperation.Type.NORMAL) {
                    NormalOperation nop = (NormalOperation) op;
                    String insCode = nop.getInsertedText();
                    String delCode = nop.getDeletedText();

                    if (isConflictedCode(insCode) && !isConflictedCode(delCode)) {
                        isAfterFlag = true;
                    }
                } else if (op.getOperationType() == IOperation.Type.FILE) {
                    if (op instanceof FileOperation) {
                        FileOperation fop = (FileOperation) op;
                        String code = fop.getCode();

                        if (code == null && isConflictedCode(code)) {
                            isAfterFlag = true;
                        }
                    }
                }
            }

            if (isAfterFlag) {
                ops.remove();
            }
        }
    }

    private boolean isConflictedCode(String code) {
        return code.indexOf("<<<<<<< HEAD\n") != -1 && code.indexOf("=======\n") != -1 && code.indexOf(">>>>>>> ") != -1;
    }

    @Override
    protected String getMessage() {
        return MESSAGE;
    }

    @Override
    protected String getErrorMessage() {
        return ERROR_MESSAGE;
    }

    @Override
    protected void nextState(ConflictDetectingDialog dialog) {
        SliceManager.getInstance().detect(dialog);
    }
}
