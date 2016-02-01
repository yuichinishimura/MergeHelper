/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changerepository.dependencygraph;

import org.jtool.changerepository.operation.UnifiedOperation;
import org.jtool.changerepository.data.FileInfo;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/**
 * Stores information on the operation dependency graph.
 * @author Katsuhisa Maruyama
 */
public class FileOpDepGraph {
    
    /**
     * The information of the file corresponding to this graph.
     */
    private FileInfo fileInfo;
    
    /**
     * The collection of nodes of this graph.
     */
    private Set<OpDepGraphNode> nodes = new HashSet<OpDepGraphNode>();
    
    /**
     * The collection of edges of this graph.
     */
    private Set<OpDepGraphEdge> edges = new HashSet<OpDepGraphEdge>();
    
    /**
     * The time when the file information was last generated or modified.
     */
    private long lastModifiedTime;
    
    /**
     * Creates a new object storing information on the operation dependency graph.
     * @param finfo the information of the file corresponding to this operation dependency graph
     */
    protected FileOpDepGraph(FileInfo finfo) {
        this.fileInfo = finfo;
    }
    
    /**
     * Returns information of the file corresponding to this graph.
     * @return the file information.
     */
    public FileInfo getFileInfo() {
        return fileInfo;
    }
    
    /**
     * Adds the specified node to this graph.
     * @param node the node to be added
     */
    void add(OpDepGraphNode node) {
        nodes.add(node);
    }
    
    /**
     * Adds the specified edge to this graph.
     * @param node the edge to be added
     */
    void add(OpDepGraphEdge edge) {
        edges.add(edge);
        edge.getSrcNode().addOutgoingEdge(edge);
        edge.getDstNode().addIncomingEdge(edge);
    }
    
    /**
     * Returns all the nodes in this graph.
     * @return the collection of the nodes
     */
    public Set<OpDepGraphNode> getAllNodes() {
        return nodes;
    }
    
    /**
     * Returns all the edges in this graph.
     * @return the collection of the edges
     */
    public Set<OpDepGraphEdge> getAllEdges() {
        return edges;
    }
    
    /**
     * Obtains the operation node with the specified sequence number.
     * @param gid the identification number of the node
     * @return the found operation node, or <code>null</code> if none
     */
    public OpDepGraphNode getNode(int gid) {
        for (OpDepGraphNode n : nodes) {
            if (n.getGId() == gid) {
                return n;
            }
        }
        return null;
    }
    
    /**
     * Tests if there is an edge connecting between two nodes.
     * @param src the source node of the edge
     * @param dst the destination node of the edge
     * @return <code>true</code> an edge was found, otherwise <code>false</code>
     */
    public boolean connect(OpDepGraphNode src, OpDepGraphNode dst) {
        for (OpDepGraphEdge edge : edges) {
            if (edge.getSrcNode().equals(src) && edge.getDstNode().equals(dst)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Obtains nodes that is the source of a give node.
     * @param src the destination node
     * @return the collection of the destination nodes
     */
    public List<OpDepGraphNode> getSrcNodes(OpDepGraphNode dst) {
        List<OpDepGraphNode> ns = new ArrayList<OpDepGraphNode>();
        for (OpDepGraphEdge edge : edges) {
            if (edge.getDstNode().equals(dst)) {
                ns.add(edge.getSrcNode());
            }
        }
        return ns;
    }
    
    /**
     * Obtains nodes that is the destination of a give node.
     * @param src the source node
     * @return the collection of the destination nodes
     */
    public List<OpDepGraphNode> getDstNodes(OpDepGraphNode src) {
        List<OpDepGraphNode> ns = new ArrayList<OpDepGraphNode>();
        for (OpDepGraphEdge edge : edges) {
            if (edge.getSrcNode().equals(src)) {
                ns.add(edge.getDstNode());
            }
        }
        return ns;
    }
    
    /**
     * Tests if this graph contains the given node.
     * @param node the node to be checked
     * @return <code>true</code> if this graph contains the node, otherwise <code>false</code>
     */
    public boolean contains(OpDepGraphNode node) {
        for (OpDepGraphNode n : nodes) {
            if (n.equals(node)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Tests if this graph contains the given edge.
     * @param edge the edge to be checked
     * @return <code>true</code> if this graph contains the edge, otherwise <code>false</code>
     */
    public boolean contains(OpDepGraphEdge edge) {
        for (OpDepGraphEdge e : edges) {
            if (e.equals(edge)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Obtains operation nodes performed before a given time.
     * @param time the time when a snippet is selected
     * @return the collection of the operation nodes
     */
    public List<OpDepGraphNode> getNodesBefore(long time) {
        List<OpDepGraphNode> ns = new ArrayList<OpDepGraphNode>();
        for (OpDepGraphNode node : nodes) {
            if (time >= node.getTime()) {
                ns.add(node);
            }
        }
        
        OpDepGraph.sortNodes(ns);
        return ns;
    }
    
    /**
     * Obtains operation nodes performed after a given time.
     * @param time the time when a snippet is selected
     * @return the collection of the operation nodes
     */
    public List<OpDepGraphNode> getNodesAfter(long time) {
        List<OpDepGraphNode> ns = new ArrayList<OpDepGraphNode>();
        for (OpDepGraphNode node : nodes) {
            if (time <= node.getTime()) {
                ns.add(node);
            }
        }
        
        OpDepGraph.sortNodes(ns);
        return ns;
    }
    
    /**
     * Sets the time when the file information was last generated or modified
     * @param time the last generated or modified time of this file information
     */
    void setLastModifiedTime(long time) {
        lastModifiedTime = time;
    }
    
    /**
     * Returns the time when the file information was last generated or modified
     * @return the last generated or modified time of this file information
     */
    long getLastModifiedTime() {
        return lastModifiedTime;
    }
    
    
    /**
     * Stores the string into the buffer for printing edges. 
     * @param buf the string buffer
     */
    private void printEdges(StringBuilder buf) {
        ArrayList<OpDepGraphEdge> es = new ArrayList<OpDepGraphEdge>();
        for (OpDepGraphEdge edge : edges) {
            es.add(edge);
        }
        OpDepGraph.sortEdgesByDstId(es);
        OpDepGraph.sortEdgesBySrcId(es);
        
        buf.append("Edges(" + es.size() + "):\n");
        for (OpDepGraphEdge edge : es) {
            buf.append(edge.toString());
            buf.append("\n");
        }
    }
    
    /**
     * Obtains the operation node corresponding to the specified operation.
     * @param op the operation
     * @return the found operation node, or <code>null</code> if node
     */
    public OpDepGraphNode getOperationNode(UnifiedOperation op) {
        for (OpDepGraphNode node : getAllNodes()) {
            if (node.getId() == op.getId()) {
                return node;
            }
        }
        return null;
    }
    
    /**
     * Returns the string for printing.
     * @return the string for printing
     */
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("----- Graph (from here) -----\n");
        printNodes(buf);
        printEdges(buf);
        buf.append("----- Graph (to here) -----\n");
        
        return buf.toString();
    }
    
    /**
     * Returns the string for printing.
     * @return the string for printing
     */
    public String toStringSimple() {
        StringBuilder buf = new StringBuilder();
        buf.append("-- Graph " + getFileInfo().getName());
        buf.append(" N=" + nodes.size());
        buf.append(" E=" + edges.size());
        buf.append("(" + OpDepGraph.extractEdges(edges, OpDepGraphEdge.Sort.NORMAL).size() + ")");
        buf.append("(" + OpDepGraph.extractEdges(edges, OpDepGraphEdge.Sort.CPP).size() + ")");
        return buf.toString();
    }
    
    /**
     * Stores the string into the buffer for printing nodes. 
     * @param buf the string buffer
     */
    private void printNodes(StringBuilder buf) {
        ArrayList<OpDepGraphNode> ns = new ArrayList<OpDepGraphNode>();
        for (OpDepGraphNode node : nodes) {
            ns.add(node);
        }
        OpDepGraph.sortNodes(ns);
        
        buf.append("Nodes(" + ns.size() + "):\n");
        for (OpDepGraphNode node : ns) {
            buf.append(node.toString());
            buf.append("\n");
        }
    }
    
    /**
     * Obtains all the operations stored in this graph.
     * @return the collection of the operations
     */
    public List<UnifiedOperation> getOperations() {
        List<UnifiedOperation> ops = new ArrayList<UnifiedOperation>();
        for (OpDepGraphNode node : getAllNodes()) {
            ops.add(node.getOperation());
        }
        return ops;
    }
}
