/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changeslicereplayer.slicer;

import org.jtool.changerepository.parser.OpJavaElement;
import org.jtool.changeslicereplayer.ui.SliceSourceCodeView;
import java.util.List;

/**
 * Performs the action for slicing operations.
 * @author Katsuhisa Maruyama
 */
public class BackwardSliceActionOnAll extends BackwardSliceActionOn {
    
    /**
     * The Java elements appearing in the snapshot of the source code.
     */
    private List<OpJavaElement> elements;
    
    /**
     * Creates an instance for performing the slicing.
     * @param view the view that displays sliced code
     * @param idx the sequence number of the snapshot of the source code
     * @param elems the collection of Java elements appearing in the snapshot of the source code
     */
    public BackwardSliceActionOnAll(SliceSourceCodeView view, int idx, List<OpJavaElement> elems) {
        super(view, idx, elems.get(0));
        setText("*All Java elements (for experiments)");
        
        this.elements = elems;
    }
    
    /**
     * Constructs a slice based on the class member with the specified name.
     */
    @Override
    public void run() {
        if (setUp()) {
            System.out.println(fgraph.toStringSimple());
            System.out.println("pid , index, name , slice size , left , right , left , right , ");
            
            int num = 0;
            for (OpJavaElement elem : elements) {
                Slice slice = getBackwardSlice(elem);
                System.out.println(String.valueOf(num) + " , " + slice.toString());
                num++;
            }
        }
        
        tearDown();
    }
}
