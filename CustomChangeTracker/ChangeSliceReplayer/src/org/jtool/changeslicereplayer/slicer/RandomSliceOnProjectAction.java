/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changeslicereplayer.slicer;

import org.jtool.changerepository.data.FileInfo;
import org.jtool.changerepository.data.ProjectInfo;
import org.jtool.changerepository.dependencygraph.FileOpDepGraph;
import org.jtool.changerepository.dependencygraph.OpDepGraph;
import org.jtool.changerepository.dependencygraph.OpDepGraphInfo;
import org.jtool.changerepository.operation.UnifiedOperation;
import org.jtool.changerepository.parser.OpJavaElement;
import org.jtool.changeslicereplayer.ui.SliceSourceCodeView;
import org.eclipse.jface.action.Action;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Performs the action for slicing operations.
 * @author Katsuhisa Maruyama
 */
public class RandomSliceOnProjectAction extends Action {
    
    /**
     * The information on a project that is the target of slicing.
     */
    private ProjectInfo projectInfo;
    
    /**
     * The operation dependency graph.
     */
    private OpDepGraph graph;
    
    /**
     * Creates an instance for performing the slicing.
     * @param view the view that displays sliced code
     */
    public RandomSliceOnProjectAction(SliceSourceCodeView view) {
        super();
        setText("*Random for each Java element on project (for experiments)");
        
        this.projectInfo = view.getProjectInfo();
    }
    
    /**
     * Constructs a slice based on the class member with the specified name.
     */
    @Override
    public void run() {
        if (setUp()) {
            System.out.println("Random slicing on " + projectInfo.getQualifiedName());
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
        graph = OpDepGraphInfo.createGraph(projectInfo);
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
     * Generates slice criteria at random and slicing based on the criteria.
     * @param graph the operation history graph
     */
    @SuppressWarnings("unused")
    private void genSlicesExceptForZeroSizeOnes() {
        List<SliceCriterion> criteria = collectCriteria();
        
        List<SliceCriterion> clone = new ArrayList<SliceCriterion>(criteria);
        Collections.shuffle(clone);
        Collections.shuffle(clone);
        Collections.shuffle(clone);
        
        int max = (clone.size() / 100) + 1;
        int num = 0;
        int i = 0;
        while (i < clone.size()) {
            
            Slice slice = getBackwardSlice(clone.get(i));
            
            if (slice.size() > 0) {
                System.out.println(slice.toSimpleString());
                num++;
                if (num == max) {
                    break;
                }
            }
            i++;
        }
    }
    
    /**
     * Collects slice criteria.
     */
    private List<SliceCriterion> collectCriteria() {
        List<SliceCriterion> criteria = new ArrayList<SliceCriterion>();
        for (FileInfo finfo : projectInfo.getAllFileInfo()) {
            criteria.addAll(collectCriteria(finfo));
        }
        return criteria;
    }
    
    /**
     * Collects slice criteria.
     * @param finfo the file information
     */
    private List<SliceCriterion> collectCriteria(FileInfo finfo) {
        List<SliceCriterion> criteria = new ArrayList<SliceCriterion>();
        
        FileOpDepGraph fgraph = graph.get(finfo);
        List<OpJavaElement> elems = new ArrayList<OpJavaElement>();
        for (int idx = 0; idx < finfo.getOperations().size(); idx++) {
            RandomSliceOnFileAction.collectJavaElementsInFile(finfo, idx, elems);
            
            UnifiedOperation op = finfo.getOperations().get(idx);
            long time = op.getTime();
            
            for (OpJavaElement elem : elems) {
                int start = elem.getStart();
                int end = elem.getEnd();
                String code = finfo.getCode(idx);
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
}
