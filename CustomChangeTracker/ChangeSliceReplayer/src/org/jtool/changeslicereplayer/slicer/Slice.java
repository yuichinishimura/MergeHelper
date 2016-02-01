/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changeslicereplayer.slicer;

import org.jtool.changerepository.operation.UnifiedOperation;
import org.jtool.changerepository.dependencygraph.OpDepGraphNode;
import org.jtool.changerepository.dependencygraph.OpDepGraph;
import java.util.List;
import java.util.ArrayList;

/**
 * Stores information on a code snippet.
 * @author Katsuhisa Maruyama
 */
public class Slice {
    
    /**
     * The criterion used for creating this slice.
     */
    private SliceCriterion criterion;
    
    /**
     * The collection of operations within this slice.
     */
    private List<OpDepGraphNode> nodes;
    
    /**
     * Creates an instance that stores information on the code snippet.
     * @param criterion the criterion used for creating the slice
     * @param nodes the collection of operations within the slice
     */
    public Slice(SliceCriterion criterion, List<OpDepGraphNode> nodes) {
        this.criterion = criterion;
        this.nodes = nodes;
    }
    
    /**
     * Returns the start point on this code range.
     * @return the offset value of this start point
     */
    public SliceCriterion getCriterion() {
        return criterion;
    }
    
    /**
     * Returns the collection of operation nodes stored in this slice.
     * @return the collection of the operation nodes in the slice
     */
    public List<OpDepGraphNode> getNodes() {
        return nodes;
    }
    
    /**
     * Returns the first node within this slice.
     * @return the first node
     */
    public OpDepGraphNode getFirstNode() {
        if (nodes.size() == 0) {
            return null;
        }
        
        OpDepGraph.sortNodes(nodes);
        return nodes.get(0);
    }
    
    /**
     * Returns the last node within this slice.
     * @return the last node
     */
    public OpDepGraphNode getLastNode() {
        if (nodes.size() == 0) {
            return null;
        }
        
        OpDepGraph.sortNodes(nodes);
        return nodes.get(nodes.size() - 1);
    }
    
    /**
     * Returns the size of this slice.
     * @return the slice size
     */
    public int size() {
        return nodes.size();
    }
    
    /**
     * Returns the collection of operations stored in this slice.
     * @return the collection of the operations in the slice
     */
    public List<UnifiedOperation> getOperations() {
        List<UnifiedOperation> ops = new ArrayList<UnifiedOperation>();
        for (OpDepGraphNode n : nodes) {
            ops.add(n.getOperation());
        }
        return ops;
    }
    
    
    /**
     * Tests if this slice contains a given node.
     * @param node the node to be checked
     * @return <code>true</code> if the slice contains a given node, otherwise <code>false</code>
     */
    public boolean contain(UnifiedOperation op) {
        for (OpDepGraphNode n : nodes) {
            if (n.getId() == op.getId()) {
                return true;
            }
        }
        return false;
    }
    
    
    /**
     * Tests if this slice contains a given node.
     * @param node the node to be checked
     * @return <code>true</code> if the slice contains a given node, otherwise <code>false</code>
     */
    public boolean contain(OpDepGraphNode node) {
        for (OpDepGraphNode n : nodes) {
            if (n.equals(node)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns the string for printing, which does not contain a new line character at its end.
     * @return the string for printing
     */
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(criterion.getIndex());
        buf.append(" , ");
        buf.append(criterion.getName());
        buf.append(" , ");
        
        buf.append(nodes.size());
        buf.append(" , ");
        if (nodes.size() > 0) {
            buf.append(nodes.get(0).getId());
            buf.append(" , ");
            buf.append(nodes.get(nodes.size() - 1).getId());
            buf.append(" , ");
            
            buf.append(nodes.get(0).getGId() + 1);
            buf.append(" , ");
            buf.append(nodes.get(nodes.size() - 1).getGId() + 1);
            buf.append(" , ");
            
        } else {
            buf.append(0);
            buf.append(" , ");
            buf.append(0);
            buf.append(" , ");
            
            buf.append(0);
            buf.append(" , ");
            buf.append(0);
            buf.append(" , ");
        }
        return buf.toString();
    }
    
    /**
     * Returns the string for printing, which does not contain a new line character at its end.
     * @return the string for printing
     */
    public String toSimpleString() {
        return OpGraphSlicer.toString(nodes);
    }
}
