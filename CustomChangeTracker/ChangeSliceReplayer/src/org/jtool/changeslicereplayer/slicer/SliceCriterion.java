/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changeslicereplayer.slicer;

import org.jtool.changerecorder.util.Time;
import org.jtool.changerepository.dependencygraph.FileOpDepGraph;
import org.jtool.changerepository.dependencygraph.OpDepGraphNode;

import java.util.List;
import java.util.ArrayList;

/**
 * Stores information on a code snippet.
 * @author Katsuhisa Maruyama
 */
public class SliceCriterion {
    
    /**
     * The collection of operations as this slice criterion.
     */
    private List<OpDepGraphNode> nodes;
    
    /**
     * The snippet as the slice criterion.
     */
    private List<CodeSnippet> snippets;
    
    /**
     * The name of the slice criterion.
     */
    private String name;
    
    /**
     * Creates an instance that stores information on the slice criterion.
     * @param graph operation dependency graph
     * @param name the name of the slice criterion
     * @param snippet the code snippet selected as the slice criterion
     */
    public SliceCriterion(FileOpDepGraph graph, String name, CodeSnippet snippet) {
        this.name = name;
        this.snippets = new ArrayList<CodeSnippet>();
        this.snippets.add(snippet);
        
        nodes = OpGraphSlicer.getNodesRelatedToSnippetBefore(graph, snippet);
    }
    
    /**
     * Creates an instance that stores information on the code snippet.
     * @param graph operation dependency graph for a file containing the snippet
     * @param snippet the collection of code snippets selected as the slice criterion
     */
    public SliceCriterion(FileOpDepGraph graph, String name, List<CodeSnippet> snippets) {
        this.name = name;
        this.snippets = snippets;
        
        nodes = new ArrayList<OpDepGraphNode>();
        for (CodeSnippet snippet : snippets) {
            nodes.addAll(OpGraphSlicer.getNodesRelatedToSnippetBefore(graph, snippet));
        }
    }
    
    /**
     * Returns the code snippet selected as the slice criterion.
     * @return the selected code snippet
     */
    public CodeSnippet getSnippet() {
        return snippets.get(0);
    }
    
    /**
     * Returns the collection of code snippets selected as the slice criterion.
     * @return the selected code snippets
     */
    public List<CodeSnippet> getSnippets() {
        return snippets;
    }
    
    /**
     * Returns the sequence number of the snapshot containing this code snippet.
     * @return the sequence number for the code snippet
     */
    public long getIndex() {
        return snippets.get(0).getIndex();
    }
    
    /**
     * Returns the time of the snapshot containing the selected snippet(s).
     * @return the time for the selected snippet(s)
     */
    public long getTime() {
        return snippets.get(0).getTime();
    }
    
    /**
     * Returns the name of this slice criterion.
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the collection of operation nodes as this slice criterion.
     * @return the collection of the operation nodes
     */
    public List<OpDepGraphNode> getNodes() {
        return nodes;
    }
    
    /**
     * Returns the string for printing, which does not contain a new line character at its end.
     * @return the string for printing
     */
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(Time.toUsefulFormat(getTime()));
        buf.append(" ");
        for (CodeSnippet snippet : snippets) {
            buf.append("[");
            buf.append(snippet.toSimpleString());
            buf.append("] ");
        }
        
        buf.append(OpGraphSlicer.toString(nodes));
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
