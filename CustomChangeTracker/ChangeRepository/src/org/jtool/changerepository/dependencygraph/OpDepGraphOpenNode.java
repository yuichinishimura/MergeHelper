/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changerepository.dependencygraph;

import org.jtool.changerepository.operation.UnifiedOperation;

/**
 * Manages information on a file open operation node.
 * @author Katsuhisa Maruyama
 */
public class OpDepGraphOpenNode extends OpDepGraphNode {
    
    /**
     * Creates a file open operation node for the operation dependency graph.
     * @param gid the identification number of this node
     * @param op the operation
     */
    public OpDepGraphOpenNode(int gid, UnifiedOperation op) {
        super(gid, op);
        
        reset();
    }
    
    /**
     * Resets the adjustment for offsets.
     */
    public void reset() {
        adjustedOffsets.clear();
    }
    
    /**
     * Tests if node indicates file open.
     * @return always <code>true</code>
     */
    public boolean isOpenNode() {
        return true;
    }
    
    /**
     * Returns the leftmost offset of the text affected by the operation corresponding to this node.
     * @return always <code>0</code> because the open operation seems to override the whole text.
     */
    public int getOffset() {
        return 0;
    }
    
    /**
     * Returns the length of the text affected by the operation corresponding to this node.
     * @return the length of the text
     */
    public int getLength() {
        return operation.getCode().length();
    }
    
    /**
     * Tests if this node depends on a given node.
     * @param node the node on which this node might depend on
     * @return always <code>false</code> because the file open operation never depends on other nodes
     */
    @Override
    public boolean dependsOn(OpDepGraphNode node) {
        return false;
    }
}
