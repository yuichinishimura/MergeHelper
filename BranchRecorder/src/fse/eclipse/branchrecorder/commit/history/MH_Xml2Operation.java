package fse.eclipse.branchrecorder.commit.history;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.jtool.changerecorder.history.OperationHistory;
import org.jtool.changerecorder.history.Xml2Operation;
import org.jtool.changerecorder.history.Xml2OperationOR;
import org.jtool.changerecorder.history.XmlConstantStrings;
import org.jtool.changerecorder.operation.IOperation;
import org.jtool.changerecorder.util.StringComparator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import fse.eclipse.branchrecorder.commit.operation.CommitOperation;

/**
 * {@link org.jtool.changerecorder.history.Xml2Operation}
 */
public class MH_Xml2Operation {

    private static final Xml2Operation instance = new Xml2Operation();
    private static final Class<Xml2Operation> cls = Xml2Operation.class;

    /**
     * {@link org.jtool.changerecorder.history.Xml2Operation#convert(Document)}
     */
    public static OperationHistory convert(Document doc) {
        NodeList list = doc.getElementsByTagName(XmlConstantStrings.OperationHistoryElem);
        if (list.getLength() <= 0) {
            System.err.print("invalid operation history format");
            return null;
        }

        Element top = (Element) list.item(0);
        String version = top.getAttribute(XmlConstantStrings.VersionAttr);

        if (version.endsWith("a")) {
            return getOperations(doc);
        } else {
            return Xml2OperationOR.getOperations(doc);
        }
    }

    /**
     * {@link org.jtool.changerecorder.history.Xml2Operation#getOperations(Document)}
     */
    public static OperationHistory getOperations(Document doc) {
        NodeList list = doc.getElementsByTagName(XmlConstantStrings.OperationHistoryElem);
        if (list.getLength() <= 0) {
            System.err.print("invalid operation history format");
            return null;
        }

        NodeList operationList = doc.getElementsByTagName(XmlConstantStrings.OperationsElem);
        if (operationList == null) {
            return null;
        }

        Node operations = operationList.item(0);
        if (operations == null) {
            return null;
        }

        List<IOperation> ops = new ArrayList<IOperation>();
        NodeList childOperations = operations.getChildNodes();
        for (int i = 0; i < childOperations.getLength(); i++) {
            Node node = childOperations.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                IOperation op = getOperation(node);
                if (op != null) {
                    ops.add(op);
                }
            }
        }

        OperationHistory history = new OperationHistory(ops);
        return history;
    }

    private static IOperation getOperation(Node node) {
        Element elem = (Element) node;
        if (StringComparator.isSame(elem.getNodeName(), XmlCommitWriter.CommitOperationElem)) {
            return getCommitOperation(elem);
        } else {
            return getBaseOperation(node);
        }
    }

    private static CommitOperation getCommitOperation(Element elem) {
        String time = elem.getAttribute(XmlConstantStrings.TimeAttr);
        String file = elem.getAttribute(XmlConstantStrings.FileAttr);
        String author = elem.getAttribute(XmlConstantStrings.AuthorAttr);
        String commitId = elem.getAttribute(XmlCommitWriter.CommitIdAttr);
        String parentId = elem.getAttribute(XmlCommitWriter.ParentCommitIdAttr);
        String code = getFirstChildText(elem.getElementsByTagName(XmlConstantStrings.CodeElem));

        CommitOperation op = new CommitOperation(Long.parseLong(time), file, author, code, commitId, parentId);
        return op;
    }

    /**
     * {@link org.jtool.changerecorder.history.Xml2Operation#getOperations(Document)}
     */
    private static IOperation getBaseOperation(Node node) {
        try {
            Method m = cls.getDeclaredMethod("getOperation", Node.class);
            m.setAccessible(true);
            return (IOperation) m.invoke(instance, node);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * {@link org.jtool.changerecorder.history.Xml2Operation#getFirstChildText(NodeList)}
     */
    private static String getFirstChildText(NodeList nodes) {
        try {
            Method m = cls.getDeclaredMethod("getFirstChildText", NodeList.class);
            m.setAccessible(true);
            return m.invoke(instance, nodes).toString();
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }
        return null;
    }
}
