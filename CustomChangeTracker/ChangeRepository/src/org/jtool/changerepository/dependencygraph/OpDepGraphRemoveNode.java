/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changerepository.dependencygraph;

import org.jtool.changerepository.operation.UnifiedOperation;
import java.util.List;
import java.util.ArrayList;

/**
 * Manages information on a removal operation node.
 * @author Katsuhisa Maruyama
 */
public class OpDepGraphRemoveNode extends OpDepGraphNode {
    
    /**
     * Creates a removal operation node for the operation dependency graph.
     * @param gid the identification number of this node
     * @param op the operation
     */
    public OpDepGraphRemoveNode(int gid, UnifiedOperation op) {
        super(gid, op);
        
        reset();
    }
    
    /**
     * Tests if node indicates removal.
     * @return always <code>true</code>
     */
    public boolean isRemoveNode() {
        return true;
    }
    
    /**
     * Returns the leftmost offset of the text affected by the operation corresponding to this node.
     * @return the leftmost offset value of the text
     */
    public int getOffset() {
        return operation.getStart();
    }
    
    /**
     * Returns the length of the text affected by the operation corresponding to this node.
     * @return the length of the text
     */
    public int getLength() {
        return operation.getDeletedText().length();
    }
    
    /**
     * Tests if this node depends on a given node.
     * @param node the node on which this node might depend on
     * @return <code>true</code> if this node depends on a given node, otherwise <code>false</code>
     */
    @Override
    public boolean dependsOn(OpDepGraphNode node) {
        if (node.isAddNode() || node.isOpenNode()) {
            int offset_i = node.getOffset();
            int length_i = node.getLength();
            
            List<Integer> adjustedOffsets2 = new ArrayList<Integer>(adjustedOffsets);
            for (Integer offset : adjustedOffsets2) {
                int offset_j = offset.intValue();
                if (offset_i <= offset_j && offset_j < offset_i + length_i) {
                    adjustedOffsets.remove(offset);
                }
            }
            
            return adjustedOffsets.size() < adjustedOffsets2.size();
            
        } else if (node.isRemoveNode()) {
            int offset_i = node.getOffset();
            int offset_j = getAdjustedOffsets().get(0).intValue();
            int length_j = getLength();
            
            return offset_j < offset_i && offset_i < offset_j + length_j;
        }
        
        // node.isCopyNode()
        return false;
    }
}
