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
import org.jtool.changerepository.operation.UnifiedOperation;
import org.jtool.changerepository.parser.OpJavaElement;
import org.jtool.changerepository.parser.OpJavaParser;
import org.jtool.changerepository.parser.OpJavaVisitor;
import org.jtool.changeslicereplayer.ui.SliceSourceCodeView;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.action.Action;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Performs the action for slicing operations.
 * @author Katsuhisa Maruyama
 */
public class RandomSliceOnFileAction extends Action {
    
    /**
     * The information on a file that is the target of slicing.
     */
    private FileInfo fileInfo;
    
    /**
     * The operation dependency graph.
     */
    private OpDepGraph graph;
    
    /**
     * Creates an instance for performing the slicing.
     * @param view the view that displays sliced code
     */
    public RandomSliceOnFileAction(SliceSourceCodeView view) {
        super();
        setText("*Random for each Java element on file (for experiments)");
        
        this.fileInfo = view.getFileInfo();
    }
    
    /**
     * Constructs a slice based on the class member with the specified name.
     */
    @Override
    public void run() {
        if (setUp()) {
            System.out.println("Random slicing on " + fileInfo.getQualifiedName());
            System.out.println("pid , index, name , slice size , left , right , left , right , ");
            
            List<SliceCriterion> criteria = getCriteria();
            int num = 0;
            for (SliceCriterion criterion : criteria) {
                Slice slice = getBackwardSlice(criterion);
                System.out.println(String.valueOf(num) + " , " + slice.toString());
                num++;
            }
        }
        
        tearDown();
    }
    
    /**
     * Sets up the slicing.
     * @return <code>true</code> if the setup succeeded, otherwise <code>false</code>
     */
    protected boolean setUp() {
        RunningTime.start();
        graph = OpDepGraphInfo.createGraph(fileInfo.getProjectInfo());
        if (graph == null) {
            return false;
        }
        RunningTime.stop();
        
        return true;
    }
    
    /**
     * Tears down the slicing.
     */
    protected void tearDown() {
        RunningTime.stop();
    }
    
    /**
     * Picks up Java elements at random that will be the slice criteria.
     */
    private List<SliceCriterion> getCriteria() {
        List<SliceCriterion> criteria = collectCriteria();
        
        List<SliceCriterion> clone = new ArrayList<SliceCriterion>(criteria);
        Collections.shuffle(clone);
        Collections.shuffle(clone);
        Collections.shuffle(clone);
        
        List<SliceCriterion> sel = new ArrayList<SliceCriterion>();
        int max = (clone.size() / 10) + 1;
        for (int num = 0; num < max; num++) {
            sel.add(clone.get(num));
        }
        return sel;
    }
    
    /**
     * Collects slice criteria.
     */
    private List<SliceCriterion> collectCriteria() {
        List<SliceCriterion> criteria = new ArrayList<SliceCriterion>();
        
        FileOpDepGraph fgraph = graph.get(fileInfo);
        List<OpJavaElement> elems = new ArrayList<OpJavaElement>();
        for (int idx = 0; idx < fileInfo.getOperations().size(); idx++) {
            RandomSliceOnFileAction.collectJavaElementsInFile(fileInfo, idx, elems);
            
            UnifiedOperation op = fileInfo.getOperations().get(idx);
            long time = op.getTime();
            
            for (OpJavaElement elem : elems) {
                int start = elem.getStart();
                int end = elem.getEnd();
                String code = fileInfo.getCode(idx);
                CodeSnippet snippet = new CodeSnippet(start, end, idx, time, code.substring(start, end));
                SliceCriterion criterion = new SliceCriterion(fgraph, elem.getName(), snippet);
                criteria.add(criterion);
            }
        }
        
        return criteria;
    }
    
    /**
     * Obtains a slice based on an Java element.
     * @param elem the Java element as a slice criterion
     */
    protected Slice getBackwardSlice(SliceCriterion criterion) {
        return OpGraphSlicer.constructBackwardSlice(criterion);
    }
    
    /**
     * Collects Java elements within a snapshot.
     * @param finfo the information on the file related to the snapshot.
     * @param idx the sequence number of the snapshot
     * @param elems the collection that stores the Java elements
     * @return <code>true</code> if the source code is parse-able, otherwise <code>false</code>
     */
    static boolean collectJavaElementsInFile(FileInfo finfo, int idx, List<OpJavaElement> elems) {
        String code = finfo.getCode(idx);
        OpJavaParser parser = new OpJavaParser();
        boolean parseable = parser.parse(code);
        
        if (parseable) {
            CompilationUnit cu = parser.getCompilationUnit();
            OpJavaVisitor visitor = new OpJavaVisitor(finfo);
            cu.accept(visitor);
            
            elems.addAll(visitor.getJavaElements());
            return true;
        }
        return false;
    }
}
