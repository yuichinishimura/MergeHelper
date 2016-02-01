/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changerepository.dependencygraph;

import org.jtool.changerepository.data.FileInfo;
import org.jtool.changerepository.data.ProjectInfo;
import org.jtool.changerecorder.util.Time;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages operation dependency graphs within this workspace.
 * @author Katsuhisa Maruyama
 */
public class OpDepGraphInfo {
    
    /**
     * The collection of all operation dependency graphs within this workspace.
     */
    private static Map<String, OpDepGraph> allGraphs = new HashMap<String, OpDepGraph>();
    
    /**
     * Resets the operation dependency graph information.
     */
    public static void reset() {
        allGraphs.clear();
    }
    
    /**
     * Returns the operation dependency graph of the specified project.
     * @param pinfo the information on the project
     * @return the found operation dependency graph, or <code>null</code> if none
     */
    public static OpDepGraph getGraph(ProjectInfo pinfo) {
        return allGraphs.get(pinfo.getKey());
    }
    
    /**
     * Creates the operation dependency graph of the specified project and returns it.
     * @param pinfo the information on the project
     * @return the created operation dependency graph
     */
    public static OpDepGraph createGraph(ProjectInfo pinfo) {
        if (pinfo == null) {
            return null;
        }
        
        OpDepGraph graph = allGraphs.get(pinfo.getKey());
        
        List<FileInfo> files = new ArrayList<FileInfo>(); 
        if (graph != null) {
            for (FileInfo finfo : pinfo.getAllFileInfo()) {
                FileOpDepGraph fgraph = graph.get(finfo);
                if (finfo.getLastModifiedTime() > fgraph.getLastModifiedTime()) {
                    files.add(finfo);
                }
            }
            
            if (files.size() != 0) {
                for (FileInfo finfo : files) {
                    FileOpDepGraph fgraph = OpDepGraphConstructor.createGraph(finfo);
                    fgraph.setLastModifiedTime(Time.getCurrentTime());
                    
                    graph.remove(finfo);
                    graph.regist(fgraph);
                }
                graph.removeAllEdges();
                
                OpDepGraphConstructor.collectInterEdges(graph);
            }
            
        } else {
            graph = new OpDepGraph(pinfo);
            allGraphs.put(pinfo.getKey(), graph);
            
            for (FileInfo finfo : pinfo.getAllFileInfo()) {
                FileOpDepGraph fgraph = OpDepGraphConstructor.createGraph(finfo);
                if (fgraph == null) {
                    reset();
                    break;
                }
                
                fgraph.setLastModifiedTime(Time.getCurrentTime());
                graph.regist(fgraph);
            }
            
            if (graph.size() != 0) {
                OpDepGraphConstructor.collectInterEdges(graph);
            }
        }
        
        // System.out.println(graph.toString());
        
        return graph;
    }
    
    /**
     * Tests if the operation dependency graph already exists. 
     * @param pinfo the information on the project
     * @return <code>true</code> if the operation dependency graph exists, otherwise <code>false</code>
     */
    public static boolean existGraph(ProjectInfo pinfo) {
        if (pinfo == null) {
            return false; 
        }
        
        OpDepGraph graph = allGraphs.get(pinfo.getKey());
        if (graph == null) {
            return false;
        }
        
        for (FileInfo finfo : pinfo.getAllFileInfo()) {
            FileOpDepGraph fgraph = graph.get(finfo);
            if (finfo.getLastModifiedTime() > fgraph.getLastModifiedTime()) {
                return false;
            }
        }
        return true;
    }
}
