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
import org.jtool.changerepository.parser.OpJavaElement;
import org.jtool.changerepository.parser.CodeRange;
import org.jtool.changeslicereplayer.ui.SliceSourceCodeView;
import org.eclipse.jface.action.Action;
import java.util.List;
import java.util.ArrayList;

/**
 * Performs the action for slicing operations.
 * @author Katsuhisa Maruyama
 */
public class BackwardSliceActionOn extends Action {
    
    /**
     * The sequence number of the snapshot of the source code.
     */
    private int index;
    
    /**
     * The source code view.
     */
    private SliceSourceCodeView sourcecodeView;
    
    /**
     * The information on a file that is the target of slicing.
     */
    private FileInfo fileInfo;
    
    /**
     * The selected Java element.
     */
    private OpJavaElement element;
    
    /**
     * The operation dependency graph for a file.
     */
    protected FileOpDepGraph fgraph;
    
    /**
     * The contents of the snapshot.
     */
    protected String code;
    
    /**
     * The time of the snapshot containing this the Java element.
     */
    protected long time;
    
    /**
     * Creates an instance for performing the slicing.
     * @param view the view that displays sliced code
     * @param idx the sequence number of the snapshot of the source code
     * @param elem the selected Java element
     */
    public BackwardSliceActionOn(SliceSourceCodeView view, int idx, OpJavaElement elem) {
        super();
        StringBuilder buf = new StringBuilder();
        buf.append(" ");
        for (CodeRange range : elem.getRanges()) {
            buf.append("[" + range.getStart() + "-" + range.getEnd() + "]");
        }
        setText(elem.getName() + buf.toString());
        
        this.sourcecodeView = view;
        this.index = idx;
        this.fileInfo = view.getFileInfo();
        this.element = elem;
    }
    
    /**
     * Constructs a slice based on the class member with the specified name.
     */
    @Override
    public void run() {
        if (setUp()) {
            System.out.println(fgraph.toStringSimple());
            System.out.println("pid , index, name , slice size , left , right , left , right , ");
            
            Slice slice = getBackwardSlice(element);
            int num = 0;
            System.out.println(String.valueOf(num) + " , " + slice.toString());
            
            sourcecodeView.setSlice(slice);
        }
        
        tearDown();
    }
    
    /**
     * Sets up the slicing.
     * @return <code>true</code> if the setup succeeded, otherwise <code>false</code>
     */
    protected boolean setUp() {
        RunningTime.start();
        OpDepGraph graph = OpDepGraphInfo.createGraph(fileInfo.getProjectInfo());
        if (graph == null) {
            return false;
        }
        RunningTime.stop();
        
        fgraph = graph.get(fileInfo);
        code = fileInfo.getCode(index);
        time = fileInfo.getOperations().get(index).getTime();
        
        return true;
    }
    
    /**
     * Tears down the slicing.
     */
    protected void tearDown() {
        RunningTime.stop();
    }
    
    /**
     * Obtains a slice based on an Java element.
     * @param elem the Java element as a slice criterion
     */
    protected Slice getBackwardSlice(OpJavaElement elem) {
        List<CodeSnippet> snippets = new ArrayList<CodeSnippet>();
        for (CodeRange range : elem.getRanges()) {
            String text = code.substring(range.getStart(), range.getEnd());
            snippets.add(new CodeSnippet(range.getStart(), range.getEnd(), index, time, text));
        }
        
        SliceCriterion criterion = new SliceCriterion(fgraph, elem.getName(), snippets);
        
        return OpGraphSlicer.constructBackwardSlice(criterion);
    }
}
