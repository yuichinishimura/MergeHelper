/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changerecorder.history;

import org.jtool.changerecorder.operation.CompoundOperation;
import org.jtool.changerecorder.operation.CopyOperation;
import org.jtool.changerecorder.operation.FileOperation;
import org.jtool.changerecorder.operation.IOperation;
import org.jtool.changerecorder.operation.MenuOperation;
import org.jtool.changerecorder.operation.NormalOperation;
import org.jtool.changerecorder.util.StringComparator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import java.util.List;
import java.util.ArrayList;

/**
 * Converts the XML representation into the operation history.
 * @author Takayuki Omori
 * @author Katsuhisa Maruyama
 */
public class Xml2OperationOR {
    
    /**
     * The additional element names, attribute names, and values.
     */
    private static final String DeveloperElem = "developer";
    private static final String FileElem = "file";
    private static final String SourceCodeElem = "sourceCode";
    private static final String CompoundedOperationElem = "compoundedOperations";
    private static final String CCPAttr = "ccp";
    private static final String CCPTypeAttr = "cptype";
    private static final String TypeAttr = "type";
    private static final String NoneValue = "NONE";
    private static final String NoValue = "NO";
    
    /**
     * The name of a developer who performed an operation.
     */
    private static String developer = "";
    
    /**
     * The path of a file on which an operation was performed.
     */
    private static String path = "";
    
    /**
     * Obtains the operations from the DOM element.
     * @param doc the content of the DOM instance
     * @return the operation history after the conversion
     */
    public static OperationHistory getOperations(Document doc) {
        NodeList developers = doc.getElementsByTagName(DeveloperElem);
        if (developers == null) {
            developer = "Unknown";
        } else {
            developer = getFirstChildText(developers);
        }
        
        NodeList paths = doc.getElementsByTagName(FileElem);
        if (paths == null) {
            path = "Unknown";
        } else {
            path = getFirstChildText(paths);
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
        for(int i = 0; i < childOperations.getLength(); i++) {
            Node node = childOperations.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                IOperation op = getOperation(node);
                if (op != null) {
                    ops.add(op);
                }
            }
        }
        OperationHistory history = new OperationHistory(addOperations(ops));
        return history;
    }
    
    /**
     * Adds a file new operation and a normal edit operation before the first file open operation.
     * @param ops the collection of operations.
     * @return the collection of operations containing the added operations
     */
    private static List<IOperation> addOperations(List<IOperation> ops) {
        List<IOperation> newOps = new ArrayList<IOperation>();
        for (int i = 0; i < ops.size(); i++) {
            IOperation op = ops.get(i);
            
            if (op.getOperationType() == IOperation.Type.FILE) {
                FileOperation fop = (FileOperation)op;
                
                if (fop.getActionType() == FileOperation.Type.OPEN) {
                    newOps.add(new FileOperation(fop.getTime(), 
                            fop.getFilePath(), fop.getAuthor(), FileOperation.Type.NEW, ""));
                    
                    newOps.add(new NormalOperation(fop.getTime(), 0,
                            fop.getFilePath(), fop.getAuthor(), 0, fop.getCode(), "", NormalOperation.Type.EDIT));
                }
            }
            newOps.add(op);
        }
        
        return newOps;
    }
    
    /**
     * Obtains the operation from the DOM element.
     * @param node the DOM element
     * @return the operation
     */
    private static IOperation getOperation(Node node) {
        Element elem = (Element)node;
        if (StringComparator.isSame(elem.getNodeName(), XmlConstantStrings.NormalOperationElem)) {
            return getNormalOperation(elem);
            
        } else if (StringComparator.isSame(elem.getNodeName(), XmlConstantStrings.CompoundOperationElem) ||
                   StringComparator.isSame(elem.getNodeName(), CompoundedOperationElem)) {
            return getCompoundOperation(elem);
            
        } else if (StringComparator.isSame(elem.getNodeName(), XmlConstantStrings.FileOperationElem)) {
            return getFileOperations(elem);
            
        } else if (StringComparator.isSame(elem.getNodeName(), XmlConstantStrings.MenuOperationElem)) {
            return getMenuOperation(elem);
            
        } else if (StringComparator.isSame(elem.getNodeName(), XmlConstantStrings.CopyOperationElem)) {
            return getCopyOperation(elem);
        }
        return null;
    }
    
    /**
     * Creates a normal operation from the DOM element.
     * @param elem the DOM element
     * @return the created operation
     */
    private static NormalOperation getNormalOperation(Element elem) {
        String time = elem.getAttribute(XmlConstantStrings.TimeAttr);
        String seq = elem.getAttribute(XmlConstantStrings.SeqAttr);
        if (seq.length() == 0) {
            seq = "0";
        }
        String offset = elem.getAttribute(XmlConstantStrings.OffsetAttr);
        String file = elem.getAttribute(XmlConstantStrings.FileAttr);
        if (file.length() == 0) {
            file = path;
        }
        String author = developer;
        String action = elem.getAttribute(CCPTypeAttr);
        if (action.length() == 0) {
            action = elem.getAttribute(CCPAttr);
        }
        if (action.compareTo(NoneValue) == 0) {
            action = NoValue;
        }
        
        String insText = getFirstChildText(elem.getElementsByTagName(XmlConstantStrings.InsertedElem));
        String delText = getFirstChildText(elem.getElementsByTagName(XmlConstantStrings.DeletedElem));
        
        NormalOperation op = new NormalOperation(Long.parseLong(time), Integer.parseInt(seq),
            file, author, Integer.parseInt(offset), insText, delText, NormalOperation.Type.parseType(action));
        return op;
    }
    
    /**
     * Creates a compound operation from the DOM element.
     * @param elem the DOM element
     * @return the created operation
     */
    private static CompoundOperation getCompoundOperation(Element elem) {
        List<IOperation> ops = new ArrayList<IOperation>();
        String time = elem.getAttribute(XmlConstantStrings.TimeAttr);
        String label = elem.getAttribute(XmlConstantStrings.LabelAttr);
        
        long time0 = 0;
        if (time.length() != 0) {
            time0 = Long.parseLong(time);
        }
        
        NodeList childList = elem.getChildNodes();
        for (int i = 0; i < childList.getLength(); i++) {
            if (childList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                IOperation op = getOperation((Element)childList.item(i));
                ops.add(op);
                if (time0 == 0) {
                    time0 = op.getTime();
                }
            }
        }
        
        CompoundOperation op = new CompoundOperation(time0, ops, label);
        return op;
    }
    
    /**
     * Creates a copy operation from the DOM element.
     * @param elem the DOM element
     * @return the created operation
     */
    private static CopyOperation getCopyOperation(Element elem) {
        String time = elem.getAttribute(XmlConstantStrings.TimeAttr);
        String offset = elem.getAttribute(XmlConstantStrings.OffsetAttr);
        String file = elem.getAttribute(XmlConstantStrings.FileAttr);
        if (file.length() == 0) {
            file = path;
        }
        String author = developer;
        
        String copiedText = getFirstChildText(elem.getElementsByTagName(XmlConstantStrings.CopiedElem));
        
        CopyOperation op = new CopyOperation(Long.parseLong(time),
            file, author, Integer.parseInt(offset), copiedText);
        return op;
    }
    
    /**
     * Creates a file operation from the DOM element.
     * @param elem the DOM element
     * @return the created operation
     */
    private static FileOperation getFileOperations(Element elem) {
        String time = elem.getAttribute(XmlConstantStrings.TimeAttr);
        String file = elem.getAttribute(XmlConstantStrings.FileAttr);
        if (file.length() == 0) {
            file = path;
        }
        String action = elem.getAttribute(TypeAttr);
        String author = developer;
        
        String code = getFirstChildText(elem.getElementsByTagName(XmlConstantStrings.CodeElem));
        if (code == null) {
            code = getFirstChildText(elem.getElementsByTagName(SourceCodeElem));
            if (code == null) {
                code = "";
            }
        }
        
        FileOperation op = new FileOperation(Long.parseLong(time),
            file, author, FileOperation.Type.parseType(action), code);
        return op;
    }
    
    /**
     * Creates a menu operation from the DOM element.
     * @param elem the DOM element
     * @return the created operation
     */
    private static MenuOperation getMenuOperation(Element elem) {
        String time = elem.getAttribute(XmlConstantStrings.TimeAttr);
        String file = elem.getAttribute(XmlConstantStrings.FileAttr);
        if (file.length() == 0) {
            file = path;
        }
        String label = elem.getAttribute(XmlConstantStrings.LabelAttr);
        String author = developer;
        
        MenuOperation op = new MenuOperation(Long.parseLong(time), file, author, label);
        return op;
    }
    
    /**
     * Obtains the text of stored in the first child of a given node list.
     * @param nodes the node list of nodes store the text
     * @return the text string, <code>null</code> if no text was found
     */
    private static String getFirstChildText(NodeList nodes) {
        if (nodes == null || nodes.getLength() == 0) {
            return null;
        }
        return getFirstChildText(nodes.item(0));
    }
    
    /**
     * Obtains the text stored in the first child of a given node.
     * @param node the node that stores the text
     * @return the text string, <code>null</code> if no text was found
     */
    private static String getFirstChildText(Node node) {
        if (node == null) {
            return null;
        }
        
        NodeList nodes = node.getChildNodes();
        if (nodes == null || nodes.getLength() == 0) {
            return null;
        }
        
        Node child = nodes.item(0);
        if (child.getNodeType() == Node.TEXT_NODE) {
            return ((Text)child).getNodeValue();
        }
        return null;
    }
}
