/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changerepository.operation;

import org.jtool.changerecorder.operation.NormalOperation;

import java.util.List;
import java.util.ArrayList;

/**
 * Fabricates operations stored in the history information.
 * @author Katsuhisa Maruyama
 */
public class OperationHistoryFabricator {
    
    /**
     * Fabricates operations stored in the history information.
     * @param ops the operations to be fabricated 
     * @return the collection of the fabricated operations
     */
    public static List<UnifiedOperation> fabricate(List<UnifiedOperation> ops) {
        if (ops == null) {
            return null;
        }
        
        List<UnifiedOperation> newOps = new ArrayList<UnifiedOperation>();
        for (UnifiedOperation op : ops) {
            newOps.add(op);
        }
        
        newOps = eliminateSuccessiveOpenClose(newOps);
        return newOps;
    }
    
    /**
     * Eliminates the repeated save operation.
     * @param ops the whole operations
     * @return the collection of the operations after the elimination.
     */
    private static List<UnifiedOperation> eliminateSuccessiveOpenClose(List<UnifiedOperation> ops) {
        for (int i = 0; i < ops.size() - 1; i++) {
            UnifiedOperation op1 = ops.get(i);
            
            if (op1.isFileOpenOperation()) {
                UnifiedOperation op2 = ops.get(i + 1);
                if (op1.getFileInfo().getQualifiedName().compareTo(op2.getFileInfo().getQualifiedName()) == 0 &&
                    op2.isFileCloseOperation()) {
                    ops.remove(i);
                    ops.remove(i);
                    i--;
                }
            }
        }
        return ops;
    }
    
    /**
     * Merges operations stored in the history information.
     * Does nothing in the current implementation.
     * @param ops the operations to be merge 
     * @return the collection of the merged operations
     */
    public static List<UnifiedOperation> merge(List<UnifiedOperation> ops) {
        if (ops == null) {
            return null;
        }
        
        List<UnifiedOperation> newOps = new ArrayList<UnifiedOperation>();
        for (UnifiedOperation op : ops) {
            newOps.add(op);
        }
        
        newOps = mergeOperationsWithTemporalText(newOps);
        return newOps;
    }
    
    /**
     * Merges operations that contain the temporal text that was inserted and immediately deleted.
     * @param ops the whole operations
     * @return the collection of the operations after the merge
     */
    private static List<UnifiedOperation> mergeOperationsWithTemporalText(List<UnifiedOperation> ops) {
        for (int i = 0; i < ops.size() - 1; i++) {
            UnifiedOperation op1 = ops.get(i);
            UnifiedOperation op2 = ops.get(i + 1);
            
            if (!op1.isNormalOperation() || !op2.isNormalOperation()) {
                continue;
            }
            
            if (op1.getFileInfo().getQualifiedName().compareTo(op2.getFileInfo().getQualifiedName()) != 0) {
                continue;
            }
            
            if (op1.getAuthor().compareTo(op2.getAuthor()) != 0) {
                continue;
            }
            
            if (op1.getEditActionType() != NormalOperation.Type.NO || op2.getEditActionType() != NormalOperation.Type.NO) {
                continue;
            }
            
            String itext1 = op1.getInsertedText();
            String itext2 = op2.getInsertedText();
            String dtext1 = op1.getDeletedText();
            String dtext2 = op2.getDeletedText();
            
            if (itext1.length() == 0 || dtext2.length() == 0) {
                continue;
            }
            
            if (op1.getStart() == op2.getStart() && itext1.compareTo(dtext2) == 0) {
                if (containsMultiByteCode(itext1)) {
                    // System.out.println("MERGE: " + op1.getStart() + " d["+ itext1 + "] s[" + dtext1 + "/" + itext2 + "]");
                    
                    ops.remove(i + 1);
                    if (dtext1.length() == 0 && itext2.length() == 0) {
                        ops.remove(i);
                    } else {
                        op1.setInsertedText(itext2);
                    }
                    i--;
                }
            }
        }
        return ops;
    }
    
    /**
     * Tests if a given string contains the multibyte code.
     * @param str the string to be checked
     * @return <code>true</code> if the string contains the multibyte code, otherwise <code>false</code>
     */
    private static boolean containsMultiByteCode(String str) {
        try {
            byte[] bytes = str.getBytes("UTF8");
            return str.length() != bytes.length;
        } catch (Exception ex) { /* empty */ }
        return false;
    }
}
