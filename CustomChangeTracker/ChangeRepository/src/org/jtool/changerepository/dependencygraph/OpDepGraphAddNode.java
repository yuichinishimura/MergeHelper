/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changerepository.dependencygraph;

import org.jtool.changerepository.operation.UnifiedOperation;

/**
 * Manages information on an addition operation node.
 * @author Katsuhisa Maruyama
 */
public class OpDepGraphAddNode extends OpDepGraphNode {
    
    /**
     * Creates an addition operation node for the operation dependency graph.
     * @param @param gid the identification number of this node
     * @param op the operation
     */
    public OpDepGraphAddNode(int gid, UnifiedOperation op) {
        super(gid, op);
        
        reset();
    }
    
    /**
     * Tests if this node indicates addition.
     * @return always <code>true</code>
     */
    public boolean isAddNode() {
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
        return operation.getInsertedText().length();
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
            int offset_j = adjustedOffsets.get(0).intValue();
            
            if (offset_i < offset_j && offset_j < offset_i + length_i) {
                adjustedOffsets.clear();
                return true;
                
            } else {
                return false;
            }
            
        }
        
        // node.isRemoveNode() || node.isCopyNode()
        return false;
    }
}
