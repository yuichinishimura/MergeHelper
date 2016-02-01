/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changeslicereplayer.slicer;

import org.jtool.changerepository.dependencygraph.FileOpDepGraph;
import org.jtool.changerepository.dependencygraph.OpDepGraph;
import org.jtool.changerepository.dependencygraph.OpDepGraphNode;
import java.util.List;
import java.util.ArrayList;

/**
 * Constructs an operation graph related to the file to be replayed.
 * @author Katsuhisa Maruyama
 */
public class OpGraphSlicer {
    
    /**
     * Obtains nodes related to the range of a selected code snippet before the snippet was selected.
     * @param fgraph the operation graph
     * @param snippet the selected code snippet
     * @return the collection of the related nodes
     */
    public static List<OpDepGraphNode> getNodesRelatedToSnippetBefore(FileOpDepGraph fgraph, CodeSnippet snippet) {
        List<OpDepGraphNode> ns = new ArrayList<OpDepGraphNode>();
        
        List<OpDepGraphNode> nodes = fgraph.getNodesBefore(snippet.getTime());
        for (OpDepGraphNode node : nodes) {
            if (node.isCopyNode()) {
                continue;
            }
            
            List<OpDepGraphNode> anodes = getNodesAfter(nodes, node);
            OpDepGraph.sortNodes(anodes);
            
            node.reset();
            adjustOffsetsForward(fgraph, anodes, node);
            
            for (Integer offset : node.getAdjustedOffsets()) {
                if (snippet.inRange(offset.intValue())) {
                    ns.add(node);
                    break;
                }
            }
        }
        return ns;
    }
    
    /**
     * Obtains nodes performed after the time of a given node from a given nodes.
     * @param nodes the collection of nodes used for adjustment
     * @param node the node that is the target for adjustment
     * @return the collection of nodes related to adjustment
     */
    private static List<OpDepGraphNode> getNodesAfter(List<OpDepGraphNode> nodes, OpDepGraphNode node) {
        List<OpDepGraphNode> ns = new ArrayList<OpDepGraphNode>();
        for (OpDepGraphNode n : nodes) {
            if (!n.isCopyNode() && n.getTime() > node.getTime()) {
                ns.add(n);
            }
        }
        return ns;
    }
    
    /**
     * Adjusts the offset of a given node.
     * @param fgraph the operation graph
     * @param nodes the collection of nodes used for adjustment
     * @param node the node to be adjusted
     */
    private static void adjustOffsetsForward(FileOpDepGraph fgraph, List<OpDepGraphNode> nodes, OpDepGraphNode node) {
        for (int idx = 0; idx < nodes.size(); idx++) {
            OpDepGraphNode n  = nodes.get(idx);
            if (node.getAdjustedOffsets().size() == 0) {
                break;
            }
            
            if (fgraph.connect(node, n)) {
                node.clearAdjustedOffsets();
                break;
            }
            
            node.adjustOffsetsForward(n);
        }
    }
    
    /**
     * Obtains nodes related to the range of a selected code snippet after the snippet was selected.
     * @param fgraph the operation graph
     * @param snippet the selected code snippet
     * @return the collection of the related nodes
     */
    public static List<OpDepGraphNode> getNodesRelatedToSnippetAfter(FileOpDepGraph fgraph, CodeSnippet snippet) {
        List<OpDepGraphNode> ns = new ArrayList<OpDepGraphNode>();
        
        List<OpDepGraphNode> nodes = fgraph.getNodesAfter(snippet.getTime());
        for (OpDepGraphNode node : nodes) {
            List<OpDepGraphNode> anodes = getNodesBefore(nodes, node);
            OpDepGraph.sortNodes(anodes);
            
            node.reset();
            adjustOffsetsBackward(fgraph, anodes, node);
            
            for (Integer offset : node.getAdjustedOffsets()) {
                if (snippet.inRange(offset.intValue())) {
                    ns.add(node);
                    break;
                }
            }
        }
        return ns;
    }
    
    /**
     * Obtains nodes performed before the time of a given node from a given nodes.
     * @param nodes the collection of nodes used for adjustment
     * @param node the node that is the target for adjustment
     * @return the collection of nodes related to adjustment
     */
    private static List<OpDepGraphNode> getNodesBefore(List<OpDepGraphNode> nodes, OpDepGraphNode node) {
        List<OpDepGraphNode> ns = new ArrayList<OpDepGraphNode>();
        for (OpDepGraphNode n : nodes) {
            if (n.getTime() < node.getTime()) {
                ns.add(n);
            }
        }
        return ns;
    }
    
    /**
     * Adjusts the offset of a given node.
     * @param fgraph the operation graph
     * @param the collection of nodes used for adjustment
     * @param node the node to be adjusted
     */
    private static void adjustOffsetsBackward(FileOpDepGraph fgraph, List<OpDepGraphNode> nodes, OpDepGraphNode node) {
        for (int idx = 0; idx < nodes.size(); idx++) {
            OpDepGraphNode n  = nodes.get(idx);
            if (node.getAdjustedOffsets().size() == 0) {
                break;
            }
            
            if (fgraph.connect(n, node)) {
                node.clearAdjustedOffsets();
                break;
            }
            
            node.adjustOffsetsBackward(n);
        }
    }
    
    /**
     * Constructs the backward operation slice on the nodes as slice criterion.
     * @param criterion the slice criterion
     * @return the created slice
     */
    public static Slice constructBackwardSlice(SliceCriterion criterion) {
        List<OpDepGraphNode> ns = new ArrayList<OpDepGraphNode>();
        for (OpDepGraphNode node : criterion.getNodes()) {
            collectAllBackwardReachableNodes(ns, node);
        }
        return new Slice(criterion, ns);
    }
    
    /**
     * Collects all nodes that reach the specified node.
     * @param ns the collection of the reachable nodes
     * @param node the specified node 
     */
    private static void collectAllBackwardReachableNodes(List<OpDepGraphNode> ns, OpDepGraphNode node) {
        if (ns.contains(node)) {
            return;
        }
        
        ns.add(node);
        
        for (OpDepGraphNode n : node.getSrcNodes()) {
            collectAllBackwardReachableNodes(ns, n);
        }
    }
    
    /**
     * Constructs the forward operation slice on the nodes as slice criterion.
     * @param criterion the slice criterion
     * @return the created the slice
     */
    public static Slice constructForwardSlice(SliceCriterion criterion) {
        List<OpDepGraphNode> ns = new ArrayList<OpDepGraphNode>();
        for (OpDepGraphNode node : criterion.getNodes()) {
            collectAllForwardReachableNodes(ns, node);
        }
        return new Slice(criterion, ns);
    }
    
    /**
     * Collects all nodes that the specified node reaches.
     * @param ns the collection of the reachable nodes
     * @param node the specified node 
     */
    private static void collectAllForwardReachableNodes(List<OpDepGraphNode> ns, OpDepGraphNode node) {
        if (ns.contains(node)) {
            return;
        }
        
        ns.add(node);
        
        for (OpDepGraphNode n : node.getDstNodes()) {
            collectAllForwardReachableNodes(ns, n);
        }
    }
    
    /**
     * Returns the string for printing nodes, which does not contain a new line character at its end.
     * @param the collection of the nodes contained in the slice
     * @return the string for printing
     */
    public static String toString(List<OpDepGraphNode> nodes) {
        OpDepGraph.sortNodes(nodes);
        
        StringBuilder buf = new StringBuilder();
        buf.append("(" + nodes.size() + "): ");
        buf.append("{ ");
        for (OpDepGraphNode node : nodes) {
            buf.append(node.getGId());
            buf.append(" ");
        }
        buf.append("}\n");
        return buf.toString();
    }
}
