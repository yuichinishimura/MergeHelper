/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changerecorder.diff;

import org.jtool.changerecorder.diff.diff_match_patch.Diff;
import org.jtool.changerecorder.diff.diff_match_patch.Operation;
import org.jtool.changerecorder.operation.NormalOperation;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Generates diff operations from differences between the contents of two the source files.
 * @author Katsuhisa Maruyama
 */
public class DiffOperationGenerator {
    
    /**
     * The cost of an empty edit operation in terms of edit characters.
     */
    private static short editCost = 4;
    
    /**
     * Sets the edit costs for finding difference.
     * @param cost the edit cost to be set
     */
    public static void setEditCost(short cost) {
        editCost = cost;
    }
    
    /**
     * Generates diff operations from the differences between two textual contents.
     * @param time the time when this operation was inserted
     * @param path the name of the file path on which this operation was performed
     * @param otext the old contents of the file
     * @param ntext the new contents of the file
     * @return the collection of the generated diff operations
     */
    public static List<NormalOperation> generate(long time, String path, String otext, String ntext) {
        List<NormalOperation> ops = new ArrayList<NormalOperation>();
        List<CodeDelta> deltas = findDiff(otext, ntext);
        
        int offsetGap = 0;
        for (int idx = 0; idx < deltas.size(); idx++) {
            CodeDelta delta = deltas.get(idx);
            
            String itext = "";
            String dtext = "";
            if (delta.getType() == CodeDelta.Type.DELETE) {
                dtext = delta.getText();
                
            } else if (delta.getType() == CodeDelta.Type.INSERT) {
                itext = delta.getText();
            }
            
            int start = delta.getOffset() + offsetGap;
            NormalOperation op = new NormalOperation(time, idx, path, start, itext, dtext, NormalOperation.Type.DIFF);
            offsetGap = offsetGap - dtext.length();
            
            ops.add(op);
        }
        
        return ops;
    }
    
    /**
     * Finds differences between the contents of two the source code files.
     * @param otext the contents of the source code file to be diffed
     * @param ntext the contents of the source code file to be diffed
     * @return the collection of the code deltas
     */
    private static List<CodeDelta> findDiff(String otext, String ntext) {
        diff_match_patch dmp = new diff_match_patch();
        dmp.Diff_EditCost = editCost;
        
        LinkedList<Diff> diffs = dmp.diff_main(otext, ntext);
        dmp.diff_cleanupEfficiency(diffs);
        List<CodeDelta> deltas = getDeltas(diffs);
        
        return deltas;
    }
    
    /**
     * Obtains the deltas from a given difference information.
     * @param diffs the collection of difference information of the diff utility.
     * @return the collection of the code deltas
     */
    private static List<CodeDelta> getDeltas(LinkedList<Diff> diffs) {
        ArrayList<CodeDelta> deltas = new ArrayList<CodeDelta>();
        int offset = 0;
        
        for (ListIterator<Diff> pointer = diffs.listIterator(); pointer.hasNext(); ) { 
            Diff diff = pointer.next();
            
            if (diff.operation == Operation.INSERT) {
                deltas.add(new CodeDelta(offset, CodeDelta.Type.INSERT, diff.text));
                
            } else if (diff.operation == Operation.DELETE) {
                deltas.add(new CodeDelta(offset, CodeDelta.Type.DELETE, diff.text));
            }
            
            offset = offset + diff.text.length();
        }
        
        return deltas;
    }
}
