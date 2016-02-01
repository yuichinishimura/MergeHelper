/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changeslicereplayer.slicer;

import org.jtool.changerepository.data.FileInfo;
import org.jtool.changerepository.dependencygraph.FileOpDepGraph;
import org.jtool.changerepository.dependencygraph.OpDepGraph;
import org.jtool.changerepository.dependencygraph.OpDepGraphInfo;
import org.jtool.changeslicereplayer.ui.SliceSourceCodeView;
import org.eclipse.jface.action.Action;

/**
 * Performs the action for backward slicing.
 * @author Katsuhisa Maruyama
 */
public class BackwardSliceAction extends Action {
    
    /**
     * The source code view.
     */
    private SliceSourceCodeView sourcecodeView;
    
    /**
     * The information on a file that is the target of slicing.
     */
    private FileInfo fileInfo;
    
    /**
     * The code snippet that was selected.
     */
    private CodeSnippet snippet;
    
    /**
     * Creates an instance for performing the backward slicing.
     * @param view the view that displays sliced code
     * @param the code snippet that was selected
     */
    public BackwardSliceAction(SliceSourceCodeView view, CodeSnippet snippet) {
        super();
        setText("Backward operation slicing");
        
        this.sourcecodeView = view;
        this.fileInfo = view.getFileInfo();
        this.snippet = snippet;
    }
    
    /**
     * Constructs a backward slice based on the criterion.
     */
    @Override
    public void run() {
        OpDepGraph graph = OpDepGraphInfo.createGraph(fileInfo.getProjectInfo());
        if (graph != null) {
            FileOpDepGraph fgraph = graph.get(fileInfo);
            // System.out.println(fgraph.toString());
            
            SliceCriterion criterion = new SliceCriterion(fgraph, "@SNIPPET", snippet);
            Slice slice = OpGraphSlicer.constructBackwardSlice(criterion);
            System.out.println("SLICE ON " + snippet.toSimpleString() + "\n" + slice.toSimpleString());
            
            sourcecodeView.setSlice(slice);
        }
    }
}
