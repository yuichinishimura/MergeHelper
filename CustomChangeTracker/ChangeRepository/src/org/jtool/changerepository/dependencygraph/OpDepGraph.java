/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changerepository.dependencygraph;

import org.jtool.changerepository.data.FileInfo;
import org.jtool.changerepository.data.ProjectInfo;
import org.jtool.changerepository.operation.UnifiedOperation;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

/**
 * Stores information on the operation dependency graph.
 * @author Katsuhisa Maruyama
 */
public class OpDepGraph {
    
    /**
     * The information of the project corresponding to this graph.
     */
    private ProjectInfo projectInfo;
    
    /**
     * The collection of all operation dependency graphs for files with in the project.
     */
    private HashMap<String, FileOpDepGraph> fileGraphs = new HashMap<String, FileOpDepGraph>();
    
    /**
     * The collection of edges of this graph.
     */
    private Set<OpDepGraphEdge> interEdges = new HashSet<OpDepGraphEdge>();
    
    /**
     * Creates a new object storing information on the operation dependency graph.
     * @param pinfo the information of the project corresponding to this operation dependency graph
     */
    protected OpDepGraph(ProjectInfo pinfo) {
        this.projectInfo = pinfo;
    }
    
    /**
     * Returns information of the project corresponding to this graph.
     * @return the project information.
     */
    public ProjectInfo getProjectInfo() {
        return projectInfo;
    }
    
    /**
     * Registers the operation dependency graph for the file.
     * @param fgraph the operation dependency graph to be registered
     */
    void regist(FileOpDepGraph fgraph) {
        fileGraphs.put(fgraph.getFileInfo().getKey(), fgraph);
    }
    
    /**
     * Obtains operation dependency graphs corresponding to all the files.
     * @return the collection of the operation dependency graphs for the files
     */
    public List<FileOpDepGraph> getFileGraphs() {
        List<FileOpDepGraph> graphs = new ArrayList<FileOpDepGraph>();
        for (FileOpDepGraph fgraph : fileGraphs.values()) {
            graphs.add(fgraph);
        }
        return graphs;
    }
    
    /**
     * Obtains an operation dependency graph corresponding to the specified file.
     * @param finfo the information on the file to be retrieved
     * @return the operation dependency graph for the file
     */
    public FileOpDepGraph get(FileInfo finfo) {
        return fileGraphs.get(finfo.getKey());
    }
    
    /**
     * Removes the operation dependency graph corresponding to the specified file.
     * @param finfo the information on the file to be removed
     */
    void remove(FileInfo finfo) {
        fileGraphs.remove(finfo.getKey());
    }
    
    /**
     * Adds the specified edge to this graph.
     * @param node the edge to be added
     */
    void add(OpDepGraphEdge edge) {
        interEdges.add(edge);
        edge.getSrcNode().addOutgoingEdge(edge);
        edge.getDstNode().addIncomingEdge(edge);
    }
    
    /**
     * Removes the specified edge from this graph.
     * @param node the edge to be added
     */
    void remove(OpDepGraphEdge edge) {
        interEdges.remove(edge);
        edge.getSrcNode().removeOutgoingEdge(edge);
        edge.getDstNode().removeIncomingEdge(edge);
    }
    
    /**
     * Tests if this graph contains the given node.
     * @param node the node to be checked
     * @return <code>true</code> if this graph contains the node, otherwise <code>false</code>
     */
    public boolean contains(OpDepGraphNode node) {
        for (OpDepGraphNode n : getAllNodes()) {
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
        for (OpDepGraphEdge e : getAllEdges()) {
            if (e.equals(edge)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns the number of sub-graphs stored in this graph.
     * @return the number of the sub-graphs
     */
    int size() {
        return fileGraphs.values().size();
    }
    
    /**
     * Removes all the edges from this graph.
     */
    void removeAllEdges() {
        for (OpDepGraphEdge edge : interEdges) {
            edge.getSrcNode().removeOutgoingEdge(edge);
            edge.getDstNode().removeIncomingEdge(edge);
        }
        interEdges.clear();
    }
    
    /**
     * Returns all the nodes in this graph.
     * @return the collection of the nodes
     */
    Set<OpDepGraphNode> getAllNodes() {
        Set<OpDepGraphNode> nodes = new HashSet<OpDepGraphNode>();
        for (FileOpDepGraph fgraph : fileGraphs.values()) {
            nodes.addAll(fgraph.getAllNodes());
        }
        return nodes;
    }
    
    /**
     * Returns all the edges in this graph.
     * @return the collection of the edges
     */
    Set<OpDepGraphEdge> getAllEdges() {
        Set<OpDepGraphEdge> edges = new HashSet<OpDepGraphEdge>();
        for (FileOpDepGraph fgraph : fileGraphs.values()) {
            edges.addAll(fgraph.getAllEdges());
        }
        edges.addAll(interEdges);
        return edges;
    }
    
    /**
     * Obtains the operation node with the specified sequence number.
     * @param id the identification number of the operation
     * @return the found operation node, or <code>null</code> if node
     */
    public UnifiedOperation getOperationNode(int id) {
        for (OpDepGraphNode n : getAllNodes()) {
            if (n.getId() == id) {
                return n.getOperation();
            }
        }
        return null;
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
     * Obtains all the operations stored in this graph.
     * @return the collection of the operations
     */
    public List<UnifiedOperation> getOperations() {
        List<UnifiedOperation> ops = new ArrayList<UnifiedOperation>();
        for (OpDepGraphNode node : getAllNodes()) {
            UnifiedOperation op = node.getOperation();
            if (!ops.contains(op)) {
                ops.add(node.getOperation());
            }
        }
        return ops;
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
        buf.append("-- Graph " + getProjectInfo().getName());
        buf.append(" N=" + getAllNodes().size());
        buf.append(" E=" + getAllEdges().size());
        buf.append("(" + OpDepGraph.extractEdges(getAllEdges(), OpDepGraphEdge.Sort.NORMAL).size() + ")");
        buf.append("(" + OpDepGraph.extractEdges(getAllEdges(), OpDepGraphEdge.Sort.CPP).size() + ")");
        return buf.toString();
    }
    
    /**
     * Stores the string into the buffer for printing nodes. 
     * @param buf the string buffer
     */
    public void printNodes(StringBuilder buf) {
        List<OpDepGraphNode> ns = new ArrayList<OpDepGraphNode>();
        for (OpDepGraphNode node : getAllNodes()) {
            ns.add(node);
        }
        sortNodes(ns);
        
        buf.append("Nodes(" + ns.size() + "):\n");
        for (OpDepGraphNode node : ns) {
            buf.append(node.toString());
            buf.append("\n");
        }
    }
    
    /**
     * Stores the string into the buffer for printing edges.
     * @param buf the string buffer
     */
    public void printEdges(StringBuilder buf) {
        List<OpDepGraphEdge> es = new ArrayList<OpDepGraphEdge>();
        for (OpDepGraphEdge edge : getAllEdges()) {
            es.add(edge);
        }
        sortEdgesByDstId(es);
        sortEdgesBySrcId(es);
        
        buf.append("Edges(" + es.size() + "):\n");
        for (OpDepGraphEdge edge : es) {
            buf.append(edge.toString());
            buf.append("\n");
        }
    }
    
    /**
     * Returns edges with a given sort.
     * @param edges the collection of edges
     * @param sort the sort of the edges to be extracted
     * @return the collection of the extracted edges
     */
    public static List<OpDepGraphEdge> extractEdges(Set<OpDepGraphEdge> edges, OpDepGraphEdge.Sort sort) {
        List<OpDepGraphEdge> es = new ArrayList<OpDepGraphEdge>();
        for (OpDepGraphEdge edge : edges) {
            if (sort == edge.getSort()) {
                es.add(edge);
            }
        }
        sortEdgesByDstId(es);
        sortEdgesBySrcId(es);
        
        return es;
    }
    
    /**
     * Sorts the nodes of the operation dependency graph.
     * @param ns the collection of the nodes to be sorted
     */
    public static void sortNodes(List<OpDepGraphNode> ns) {
        Collections.sort(ns, new Comparator<OpDepGraphNode>() {
            public int compare(OpDepGraphNode n1, OpDepGraphNode n2) {
                int gid1 = n1.getGId();
                int gid2 = n2.getGId();
                if (gid2 > gid1) {
                    return -1;
                } else if (gid2 == gid1) {
                    return 0;
                }else{
                    return 1;
                }
            }
        });
    }
    
    /**
     * Sorts the edges of the operation dependency graph.
     * @param es the collection of the edges to be sorted
     */
    public static void sortEdgesBySrcId(List<OpDepGraphEdge> es) {
        Collections.sort(es, new Comparator<OpDepGraphEdge>() {
            public int compare(OpDepGraphEdge e1, OpDepGraphEdge e2) {
                int gid1 = e1.getSrcNode().getGId();
                int gid2 = e2.getSrcNode().getGId();
                if (gid2 > gid1) {
                    return -1;
                } else if (gid2 == gid1) {
                    return 0;
                }else{
                    return 1;
                }
            }
        });
    }
    
    /**
     * Sorts the edges of the operation dependency graph.
     * @param es the collection of the edges to be sorted
     */
    public static void sortEdgesByDstId(List<OpDepGraphEdge> es) {
        Collections.sort(es, new Comparator<OpDepGraphEdge>() {
            public int compare(OpDepGraphEdge e1, OpDepGraphEdge e2) {
                int gid1 = e1.getDstNode().getGId();
                int gid2 = e2.getDstNode().getGId();
                if (gid2 > gid1) {
                    return -1;
                } else if (gid2 == gid1) {
                    return 0;
                }else{
                    return 1;
                }
            }
        });
    }
}
