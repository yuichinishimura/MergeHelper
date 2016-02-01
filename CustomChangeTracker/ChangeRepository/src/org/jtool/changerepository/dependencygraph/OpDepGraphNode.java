/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changerepository.dependencygraph;

import org.jtool.changerepository.operation.UnifiedOperation;
import org.jtool.changerecorder.util.Time;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

/**
 * Stores the information on the node of the operation dependency graph.
 * @author Katsuhisa Maruyama
 */
public abstract class OpDepGraphNode {
    
    /**
     * The operation corresponding to this node.
     */
    protected UnifiedOperation operation;
    
    /**
     * The index number of this node within the file information.
     */
    protected int gid = -1;
    
    /**
     * The collection of edges incoming to this node.
     */
    private HashSet<OpDepGraphEdge> incomingEdges = new HashSet<OpDepGraphEdge>();
    
    /**
     * The collection of edges outgoing from this node.
     */
    private HashSet<OpDepGraphEdge> outgoingEdges = new HashSet<OpDepGraphEdge>();
    
    /**
     * The collection of the offsets after adjustment.
     */
    protected List<Integer> adjustedOffsets = new ArrayList<Integer>();
    
    /**
     * Creates a operation node for the operation dependency graph.
     * @param gid the identification number of this node
     * @param op the operation
     */
    public OpDepGraphNode(int gid, UnifiedOperation op) {
        this.gid = gid;
        this.operation = op;
    }
    
    /**
     * Resets the adjustment for offsets.
     */
    public void reset() {
        adjustedOffsets.clear();
        
        if (getLength() > 0) {
            for (int o = getOffset(); o < getOffset() + getLength(); o++) {
                adjustedOffsets.add(new Integer(o));
            }
        }
    }
    
    /**
     * Returns the index number of this node within the file information.
     * @return the index number of this node
     */
    public int getGId() {
        return gid;
    }
    
    /**
     * Returns the operation corresponding to this operation node.
     * @return the corresponding operation
     */
    public UnifiedOperation getOperation() {
        return operation;
    }
    
    /**
     * Returns the identification number of this node within the project information.
     * @return the identification number of this node
     */
    public int getId() {
        return operation.getId();
    }
    
    /**
     * Returns the time when the operation was performed.
     * @return the time of the operation corresponding to this node.
     */
    public long getTime() {
        return operation.getTime();
    }
    
    /**
     * Returns the leftmost offset of the text affected by the operation corresponding to this node.
     * @return always <code>0</code> by default
     */
    public int getOffset() {
        return 0;
    }
    
    /**
     * Returns the length of the text affected by the operation corresponding to this node.
     * @return always <code>0</code> by default
     */
    public int getLength() {
        return 0;
    }
    
    /**
     * Returns the length of the text, which is used for adjustment.
     * @return the positive length for the addition or open operation,
     *         the negative length for the removal operation, or
     *         <code>0</code> for copy the removal operation
     */
    public int getAdjustedLength() {
        if (isAddNode() || isOpenNode()) {
            return -1 * getLength();
        } else if (isRemoveNode()) {
            return getLength();
        }
        // isCopyNode()
        return 0;
    }
    
    /**
     * Tests if a given graph node is the same as this.
     * @param node the node of the operation dependency graph
     * @return <code>true</code> if the two nodes are the same, otherwise <code>false</code>
     */
    public boolean equals(OpDepGraphNode node) {
        return node != null && gid == node.getGId() && operation.getFile().compareTo(node.operation.getFile()) == 0;
    }
    
    /**
     * Tests if the operation of a given graph node is the same as the operation of this
     * @param node the node of the operation dependency graph
     * @return <code>true</code> if the two nodes correspond to the same operation, otherwise <code>false</code>
     */
    public boolean equalsByOperation(OpDepGraphNode node) {
        return node != null && operation.getId() == node.operation.getId();
    }
    
    /**
     * Adds the specified edge incoming to this node. 
     * @param edge the incoming edge
     */
    void addIncomingEdge(OpDepGraphEdge edge) {
        incomingEdges.add(edge);
    }
    
    /**
     * Adds the specified edge outgoing to this node.
     * @param edge the outgoing edge
     */
    void addOutgoingEdge(OpDepGraphEdge edge) {
        outgoingEdges.add(edge);
    }
    
    /**
     * Removes the specified edge incoming from this node. 
     * @param edge the incoming edge
     */
    void removeIncomingEdge(OpDepGraphEdge edge) {
        incomingEdges.remove(edge);
    }
    
    /**
     * Removes the specified edge outgoing from this node.
     * @param edge the outgoing edge
     */
    void removeOutgoingEdge(OpDepGraphEdge edge) {
        outgoingEdges.remove(edge);
    }
    
    /**
     * Returns the edges incoming to this node.
     * @return the set of incoming edges
     */
    public HashSet<OpDepGraphEdge> getIncomingEdges() {
        return incomingEdges;
    }
    
    /**
     * Returns the edges outgoing from this node.
     * @return the set of incoming edges
     */
    public HashSet<OpDepGraphEdge> getOutgoingEdges() {
        return outgoingEdges;
    }
    
    /**
     * Returns the source nodes of this node.
     * @return the set of the source nodes
     */
    public HashSet<OpDepGraphNode> getSrcNodes() {
        HashSet<OpDepGraphNode> nodes = new HashSet<OpDepGraphNode>();
        for (OpDepGraphEdge edge : incomingEdges) {
            nodes.add(edge.getSrcNode());
        }
        return nodes;
    }
    
    /**
     * Returns the destination nodes of this node.
     * @return the set of the destination nodes
     */
    public Set<OpDepGraphNode> getDstNodes() {
        HashSet<OpDepGraphNode> nodes = new HashSet<OpDepGraphNode>();
        for (OpDepGraphEdge edge : outgoingEdges) {
            nodes.add(edge.getDstNode());
        }
        return nodes;
    }
    
    /**
     * Returns the collection of the offsets after adjustment.
     * @return the collection of the adjusted offsets
     */
    public List<Integer> getAdjustedOffsets() {
        return adjustedOffsets;
    }
    
    /**
     * Clears the collection of the offsets after adjustment.
     */
    public void clearAdjustedOffsets() {
        adjustedOffsets.clear();
    }
    
    
    /**
     * Tests if this node indicates addition.
     * @return always <code>false</code>
     */
    public boolean isAddNode() {
        return false;
    }
    
    /**
     * Tests if node indicates removal.
     * @return always <code>false</code>
     */
    public boolean isRemoveNode() {
        return false;
    }
    
    /**
     * Tests if node indicates copy.
     * @return always <code>false</code>
     */
    public boolean isCopyNode() {
        return false;
    }
    
    /**
     * Tests if node indicates file open.
     * @return always <code>false</code>
     */
    public boolean isOpenNode() {
        return false;
    }
    
    /**
     * Tests if this node depends on a given node.
     * @param node the node on which this node might depend on
     * @return <code>true</code> if this node depends on a given node, otherwise <code>false</code>
     */
    public abstract boolean dependsOn(OpDepGraphNode node);
    
    /**
     * Adjusts offsets of this operation without its non-interference to the next operation.
     * @param node the node corresponding to the next operation
     */
    public void adjustOffsetsForward(OpDepGraphNode node) {
        if (adjustedOffsets.size() == 0) {
            return;
        }
        
        int offset_j = node.getOffset();
        List<Integer> adjustedOffsets2 = new ArrayList<Integer>(adjustedOffsets);
        for (Integer offset : adjustedOffsets2) {
            int offset_i = offset.intValue();
            if (isAddNode()) {
                if (offset_i > offset_j) {
                    int adj = adjustedOffsets2.indexOf(offset);
                    adjustedOffsets.remove(adj);
                    int aoffset_i = offset_i - node.getAdjustedLength();
                    adjustedOffsets.add(adj, new Integer(aoffset_i));
                }
                
            } else if (isRemoveNode() || isCopyNode()) {
                if (offset_i >= offset_j) {
                    int adj = adjustedOffsets2.indexOf(offset);
                    adjustedOffsets.remove(adj);
                    int aoffset_i = offset_i - node.getAdjustedLength();
                    adjustedOffsets.add(adj, new Integer(aoffset_i));
                }
            }
        }
    }
    
    /**
     * Adjusts offsets of this operation without its non-interference to the previous operation.
     * @param node the node corresponding to the previous operation
     */
    public void adjustOffsetsBackward(OpDepGraphNode node) {
        if (adjustedOffsets.size() == 0) {
            return;
        }
        
        int offset_i = node.getOffset();
        List<Integer> adjustedOffsets2 = new ArrayList<Integer>(adjustedOffsets);
        for (Integer offset : adjustedOffsets2) {
            int offset_j = offset.intValue();
            
            if (isAddNode()) {
                if (offset_i < offset_j) {
                    int adj = adjustedOffsets2.indexOf(offset);
                    adjustedOffsets.remove(adj);
                    int aoffset_j = offset_j + node.getAdjustedLength();
                    adjustedOffsets.add(adj, new Integer(aoffset_j));
                }
                
            } else if (isRemoveNode() || isCopyNode()) {
                if (offset_i <= offset_j) {
                    int adj = adjustedOffsets2.indexOf(offset);
                    adjustedOffsets.remove(adj);
                    int aoffset_j = offset_j + node.getAdjustedLength();
                    adjustedOffsets.add(adj, new Integer(aoffset_j));
                }
            }
        }
    }
    
    /**
     * Returns the string for printing. 
     * @return the string for printing
     */
    public String toString() {
        StringBuilder buf = new StringBuilder();
        
        buf.append(String.valueOf(getGId()));
        buf.append(" ");
        buf.append("{");
        buf.append(operation.getId());
        buf.append("}");
        buf.append(" ");
        
        buf.append(Time.toUsefulFormat(operation.getTime()) + " ");
        
        if (isAddNode()) {
            int start = operation.getStart();
            int end = start + operation.getInsertedText().length() - 1;
            buf.append("a[" + start + "," + end + "]");
            buf.append("[" + getText(operation.getInsertedText()) + "] ");
            
        } else if (isRemoveNode()) {
            int start = operation.getStart();
            int end = start + operation.getDeletedText().length() - 1;
            buf.append("r[" + start + "," + end + "]");
            buf.append("[" + getText(operation.getDeletedText()) + "] ");
            
        } else if (isCopyNode()) {
            int start = operation.getStart();
            int end = start + operation.getCopiedText().length() - 1;
            buf.append("c[" + start + "," + end + "]");
            buf.append("[" + getText(operation.getCopiedText()) + "] ");
            
        } else if (isOpenNode()) {
            int start = 0;
            int end = start + operation.getCode().length() - 1;
            buf.append("o[" + start + "," + end + "]");
            buf.append("[" + getText(operation.getCode()) + "] ");
        }
        
        buf.append("path:[" + operation.getFile() + "] ");
        
        return buf.toString();
    }
    
    /**
     * Converts a text into its pretty one.
     * @param text the original text
     * @return the text consists of the first four characters not including the new line
     */
    private String getText(String text) {
        final int LESS_LEN = 9;
        
        String text2;
        if (text.length() < LESS_LEN + 1) {
            text2 = text;
        } else {
            text2 = text.substring(0, LESS_LEN + 1);
        }
        
        return text2.replace('\n', '~');
    }
    
    /**
     * Returns the string for printing. 
     * @return the string for printing
     */
    public String toSimpleString() {
        StringBuilder buf = new StringBuilder();
        
        buf.append(String.valueOf(getGId()));
        buf.append(" ");
        buf.append("{");
        buf.append(operation.getId());
        buf.append("}");
        buf.append(" ");
        buf.append(operation.getTime());
        
        return buf.toString();
    }
    
    /**
     * Returns the string for presenting offset values.
     * @return the string of the offset values
     */
    public String getOffsets() {
        StringBuilder buf = new StringBuilder();
        buf.append("{");
        for (Integer offset : adjustedOffsets) {
            buf.append(" ");
            buf.append(offset.intValue());
        }
        buf.append(" }");
        return buf.toString();
    }
}
