package fse.eclipse.branchrecorder.commit.history;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jtool.changerecorder.history.Operation2Xml;
import org.jtool.changerecorder.history.OperationHistory;
import org.jtool.changerecorder.history.XmlConstantStrings;
import org.jtool.changerecorder.operation.FileOperation;
import org.jtool.changerecorder.operation.IOperation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import fse.eclipse.branchrecorder.commit.operation.CommitOperation;

public class MH_Operation2Xml extends Operation2Xml {

    private static final Operation2Xml instance = new Operation2Xml();
    private static final Class<Operation2Xml> cls = Operation2Xml.class;

    /**
     * {@link org.jtool.changerecorder.history.Operation2Xml#convert(OperationHistory)}
     */
    public static Document convert(OperationHistory history) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();
            generateTree(doc, history);
            return doc;
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void generateTree(Document doc, OperationHistory history) {
        Element rootElem = doc.createElement(XmlConstantStrings.OperationHistoryElem);
        rootElem.setAttribute(XmlConstantStrings.VersionAttr, XmlConstantStrings.OperationHistoryVersion);
        doc.appendChild(rootElem);

        Element operations = doc.createElement(XmlConstantStrings.OperationsElem);
        rootElem.appendChild(operations);

        for (IOperation op : history.getOperations()) {
            createOperationsElement(doc, op, operations);
        }
    }

    private static void createOperationsElement(Document doc, IOperation op, Element parent) {
        if (op == null) {
            return;
        } else if (op.getOperationType() == CommitOperation.TYPE) {
            Element opElem = appendCommitOperationElement(doc, (CommitOperation) op);
            parent.appendChild(opElem);
        } else {
            createBaseOperationsElement(doc, op, parent);
        }
    }

    private static Element appendCommitOperationElement(Document doc, CommitOperation op) {
        Element opElem = doc.createElement(XmlCommitWriter.CommitOperationElem);
        opElem.setAttribute(XmlConstantStrings.TimeAttr, String.valueOf(op.getTime()));
        opElem.setAttribute(XmlConstantStrings.FileAttr, op.getFilePath());
        opElem.setAttribute(XmlConstantStrings.ActionAttr, String.valueOf(FileOperation.Type.SAVE));
        opElem.setAttribute(XmlCommitWriter.CommitIdAttr, op.getCommitId());
        opElem.setAttribute(XmlCommitWriter.ParentCommitIdAttr, op.getParentCommitId());
        opElem.setAttribute(XmlConstantStrings.AuthorAttr, op.getAuthor());

        Element codeElem = doc.createElement(XmlConstantStrings.CodeElem);
        opElem.appendChild(codeElem);
        codeElem.appendChild(doc.createTextNode(op.getCode()));

        return opElem;
    }

    /**
     * {@link org.jtool.changerecorder.history.Operation2Xml#createOperationsElement(Document, IOperation, Element)}
     */
    private static void createBaseOperationsElement(Document doc, IOperation op, Element parent) {
        try {
            Method m = cls.getDeclaredMethod("createOperationsElement", Document.class, IOperation.class, Element.class);
            m.setAccessible(true);
            m.invoke(instance, doc, op, parent);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }
    }
}
