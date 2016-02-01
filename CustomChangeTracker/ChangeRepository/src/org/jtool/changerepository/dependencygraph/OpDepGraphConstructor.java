/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changerepository.dependencygraph;

import org.jtool.changerepository.Activator;
import org.jtool.changerepository.data.FileInfo;
import org.jtool.changerepository.data.ProjectInfo;
import org.jtool.changerepository.operation.UnifiedOperation;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IWorkbenchWindow;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.InvocationTargetException;

/**
 * Constructs an operation dependency graph related to the file.
 * @author Katsuhisa Maruyama
 */
public class OpDepGraphConstructor {
    
    /**
     * The operation dependency graph under creation, which corresponds to the project.
     */
    private static OpDepGraph pgraph;
    
    /**
     * The operation dependency graph under creation, which corresponds to the file.
     */
    private static FileOpDepGraph fgraph;
    
    /**
     * Creates an operation dependency graph corresponding to the specified file.
     * @param finfo the file information
     * @return the created operation graph for the file
     */
    static FileOpDepGraph createGraph(final FileInfo finfo) {
        try {
            IWorkbenchWindow window = Activator.getWorkbenchWindow();
            window.run(true, true, new IRunnableWithProgress() {
                
                /**
                 * Constructs the operation dependency graph with the given progress monitor.
                 * @param monitor the progress monitor to use to display progress and receive requests for cancellation
                 * @exception InvocationTargetException if the run method must propagate a checked exception
                 * @exception InterruptedException if the operation detects a request to cancel
                 */
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    fgraph = new FileOpDepGraph(finfo);
                    List<UnifiedOperation> ops = finfo.getOperations();
                    
                    monitor.beginTask("Constructing operation graph: " + finfo.getName(), ops.size() * 2);
                    
                    collectOperationNodes(ops, monitor);
                    collectDependenceEdges(monitor);
                    
                    monitor.done();
                }
            });
            
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            return null;
        }
        
        return fgraph;
    }
    
    /**
     * Collects operation nodes of the operation dependency graph.
     * @param ops the collection of the operations
     * @param monitor the progress monitor to use to display progress and receive requests for cancellation
     * @exception InterruptedException if the operation detects a request to cancel
     */
    private static void collectOperationNodes(List<UnifiedOperation> ops, IProgressMonitor monitor) throws InterruptedException {
        int gid = 0;
        for (int idx = 0; idx < ops.size(); idx++) {
            
            if (monitor.isCanceled()) {
                monitor.done();
                throw new InterruptedException();
            }
            
            monitor.subTask("Collecting opearation nodes " + String.valueOf(idx + 1) + "/" + ops.size());
            
            UnifiedOperation op = ops.get(idx);
            if (op.isFileOpenOperation()) {
                OpDepGraphNode node = new OpDepGraphOpenNode(gid, op);
                gid++;
                fgraph.add(node);
            }
            
            if (op.getDeletedText().length() != 0) {
                OpDepGraphNode node = new OpDepGraphRemoveNode(gid, op);
                gid++;
                fgraph.add(node);
            }
            
            if (op.getInsertedText().length() != 0) {
                OpDepGraphNode node = new OpDepGraphAddNode(gid, op);
                gid++;
                fgraph.add(node);
            }
            
            if (op.getCopiedText().length() != 0) {
                OpDepGraphNode node = new OpDepGraphCopyNode(gid, op);
                gid++;
                fgraph.add(node);
            }
            
            monitor.worked(1);
        }
    }
    
    /**
     * Collects dependence edges of the operation graph.
     * @param graph the operation graph under creation
     * @param monitor the progress monitor to use to display progress and receive requests for cancellation
     * @throws InterruptedException if the operation detects a request to cancel
     */
    private static void collectDependenceEdges(IProgressMonitor monitor) throws InterruptedException {
        int idx = 0;
        List<OpDepGraphNode> nodes = new ArrayList<OpDepGraphNode>(fgraph.getAllNodes());
        OpDepGraph.sortNodes(nodes);
        for (OpDepGraphNode node : nodes) {
            
            if (monitor.isCanceled()) {
                monitor.done();
                throw new InterruptedException();
            }
            
            monitor.subTask("Collecting dependence edges " + String.valueOf(idx + 1) + "/" + nodes.size());
            
            if (node.getGId() != 0) {
                collectDependenceEdge(node);
            }
            
            idx++;
            monitor.worked(1);
        }
    }
    
    /**
     * Finds a node on which a given node depends and creates a dependence edge between those nodes.
     * @param graph the operation graph under creation
     * @param node the node that depends on the found node
     */
    private static void collectDependenceEdge(OpDepGraphNode node) {
        for (int idx = node.getGId() - 1; idx >= 0; idx--) {
            OpDepGraphNode n = fgraph.getNode(idx);
            
            if (node.getAdjustedOffsets().size() == 0) {
                break;
            }
            
            if (n != null) {
                if (node.dependsOn(n)) {
                    fgraph.add(new OpDepGraphEdge(n, node, OpDepGraphEdge.Sort.NORMAL));
                } else {
                    node.adjustOffsetsBackward(n);
                }
            }
        }
    }
    
    /**
     * Collects inter-edges across operation dependency graphs for files within the specified project.
     * @param graph the operation dependency graph for the project
     * @return the collection of the inter-edges
     */
    public static void collectInterEdges(OpDepGraph graph) {
        pgraph = graph;
        final ProjectInfo pinfo = pgraph.getProjectInfo();
        
        try {
            IWorkbenchWindow window = Activator.getWorkbenchWindow();
            window.run(true, true, new IRunnableWithProgress() {
                
                /**
                 * Collects the inter-edges of the operation graph with the given progress monitor.
                 * @param monitor the progress monitor to use to display progress and receive requests for cancellation
                 * @exception InvocationTargetException if the run method must propagate a checked exception
                 * @exception InterruptedException if the operation detects a request to cancel
                 */
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    List<UnifiedOperation> ops = pinfo.getOperations();
                    
                    monitor.beginTask("Collecting inter-edges in the operation dependency graph: " + pinfo.getName(), ops.size());
                    
                    collectCCPEdges(ops, monitor);
                    
                    monitor.done();
                }
            });
            
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            return;
        }
    }
    
    /**
     * Collects ccp-edges between the node for the copy/copy operation and the node for the paste operation.
     * @param ops the collection of the operations
     * @param monitor the progress monitor to use to display progress and receive requests for cancellation
     * @exception InterruptedException if the operation detects a request to cancel
     */
    private static void collectCCPEdges(List<UnifiedOperation> ops, IProgressMonitor monitor) throws InterruptedException {
        for (int srcIdx = 0; srcIdx < ops.size(); srcIdx++) {
            
            if (monitor.isCanceled()) {
                monitor.done();
                throw new InterruptedException();
            }
            
            monitor.subTask("Collecting ccp edges " + String.valueOf(srcIdx + 1) + "/" + ops.size());
            
            UnifiedOperation srcop = ops.get(srcIdx);
            if (isCutCopyOperation(srcop)) {
                String srcText = srcop.getCutCopiedText();
                
                for (int dstIdx = srcIdx + 1; dstIdx < ops.size(); dstIdx++) {
                    UnifiedOperation dstop = ops.get(dstIdx);
                    if (isCutCopyOperation(dstop)) {
                        dstIdx = ops.size();  // escape the loop
                    }
                    
                    if (isPasteOperation(dstop)) {
                        String dstText = dstop.getInsertedText();
                        
                        if (dstText.endsWith(srcText)) {
                            OpDepGraphNode src = pgraph.getOperationNode(srcop);
                            OpDepGraphNode dst = pgraph.getOperationNode(dstop);
                            if (src != null && dst != null) {
                                
                                OpDepGraphEdge edge = new OpDepGraphEdge(src, dst, OpDepGraphEdge.Sort.CPP);
                                if (!pgraph.contains(edge)) {
                                    pgraph.add(edge);
                                }
                            }
                        }
                    }
                }
            }
            
            monitor.worked(1);
        }
    }
    
    /**
     * Tests if this operation represents cut or copy.
     * @param op the operation to be checked
     * @return <code>true</code> if this operation represents cut or copy, otherwise <code>false</code>
     */
    private static boolean isCutCopyOperation(UnifiedOperation op) {
        return op.isCutOperation() || op.isCopyOperation();
    }
    
    /**
     * Tests if this operation represents paste.
     * @param op the operation to be checked
     * @return <code>true</code> if this operation represents paste, otherwise <code>false</code>
     */
    private static boolean isPasteOperation(UnifiedOperation op) {
        return op.isPasteOperation();
    }
}
