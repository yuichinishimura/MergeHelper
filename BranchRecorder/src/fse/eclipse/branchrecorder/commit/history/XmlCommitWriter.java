package fse.eclipse.branchrecorder.commit.history;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IProject;
import org.eclipse.jgit.lib.Repository;
import org.jtool.changerecorder.history.OperationHistory;
import org.jtool.changerecorder.history.XmlConstantStrings;
import org.jtool.changerecorder.operation.IOperation;
import org.jtool.changerecorder.util.XmlFileStream;
import org.jtool.editrecorder.util.WorkspaceUtilities;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import fse.eclipse.branchrecorder.changerecorder.ProjectChache;
import fse.eclipse.branchrecorder.commit.operation.CommitOperation;
import fse.eclipse.branchrecorder.util.PathUtil;
import fse.eclipse.branchrecorder.util.RepositoryUtil;

public class XmlCommitWriter {

    public static final String CommitOperationElem = "commitOperation";
    public static final String CommitIdAttr = "commitId";
    public static final String ParentCommitIdAttr = "parentId";

    public static final String CommitXmlFileName = "-commit.xml";

    // エンコードはワークスペースの設定を使用する
    private static String encoding = WorkspaceUtilities.getEncoding();

    public static void writeCommitHistory(Repository repository, OperationHistory commitHistory) throws IOException {
        IProject project = RepositoryUtil.convertIProject(repository);
        String branchName = repository.getBranch();

        String writePath = generateWritePath(project, branchName, commitHistory.getFirstOperation().getTime());

        Document doc = generateCommitDocument(commitHistory);
        XmlFileStream.write(doc, writePath, encoding);

        ProjectChache.refreshIProject(project);
    }

    private static String generateWritePath(IProject project, String branchName, long time) throws IOException {
        String destPath = PathUtil.getProjectBranchPath(project, branchName);
        StringBuilder sb = new StringBuilder();
        sb.append(destPath).append("/").append(time).append(CommitXmlFileName);
        return sb.toString();
    }

    private static Document generateCommitDocument(OperationHistory commitHistory) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();
            generateTree(doc, commitHistory);
            return doc;
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void generateTree(Document doc, OperationHistory commitHistory) {
        Element rootElem = doc.createElement(XmlConstantStrings.OperationHistoryElem);
        rootElem.setAttribute(XmlConstantStrings.VersionAttr, XmlConstantStrings.OperationHistoryVersion);
        doc.appendChild(rootElem);

        Element operationElem = doc.createElement(XmlConstantStrings.OperationsElem);
        rootElem.appendChild(operationElem);

        List<IOperation> ops = commitHistory.getOperations();
        for (IOperation op : ops) {
            if (op instanceof CommitOperation) {
                appendCommitOperationElement(doc, operationElem, (CommitOperation) op);
            } else {
                throw new Error("not commit history");
            }
        }
    }

    /**
     * ソースコードパスから <code>XmlConstantStrings.CodeElem</code>の属性や要素を設定する
     * @param doc Document
     * @param commit CommitOperation
     * @param path ソースコードパス(key)
     * @param code ソースコード(value)
     */
    private static void appendCommitOperationElement(Document doc, Element parent, CommitOperation cop) {
        Element commitElem = doc.createElement(CommitOperationElem);
        commitElem.setAttribute(XmlConstantStrings.AuthorAttr, cop.getAuthor());
        commitElem.setAttribute(XmlConstantStrings.TimeAttr, String.valueOf(cop.getTime()));
        commitElem.setAttribute(XmlConstantStrings.FileAttr, cop.getFilePath());
        commitElem.setAttribute(CommitIdAttr, cop.getCommitId());
        commitElem.setAttribute(ParentCommitIdAttr, cop.getParentCommitId());
        parent.appendChild(commitElem);

        Element codeElem = doc.createElement(XmlConstantStrings.CodeElem);
        codeElem.appendChild(doc.createTextNode(cop.getCode()));
        commitElem.appendChild(codeElem);
    }
}
