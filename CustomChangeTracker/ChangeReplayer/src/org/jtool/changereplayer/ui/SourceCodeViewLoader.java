/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changereplayer.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

/**
 * Loads extensions for a source code view.
 * @author Katsuhisa Maruyama
 */
public class SourceCodeViewLoader {
    
    /**
     * The ID of the extension point.
     */
    public static final String EXTENSION_POINT_ID = "ChangeReplayer.sourcecodeviewPoint";
    
    /**
     * The factory for the source code viewer instance
     */
    private static SourceCodeView sourceCodeView;
    
    /**
     * Creates an instance for supporting extension loading and assigns the default loader.
     */
    public SourceCodeViewLoader() {
        sourceCodeView = new SourceCodeView();
    }
    
    /**
     * Loads the extension for the source code view. 
     */
    public void loadExtensions() {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint point = registry.getExtensionPoint(EXTENSION_POINT_ID);
        if (point == null) {
            return;
        }
        
        IExtension[] extensions = point.getExtensions();
        if (extensions.length > 0) {
            IExtension ex = extensions[0];
            IConfigurationElement[] elems = ex.getConfigurationElements();
            for (IConfigurationElement h : elems) {
                Object obj = null;
                if (h.getName().compareTo("sourcecodeview") == 0) {
                    
                    try {
                        obj = h.createExecutableExtension("class");
                    } catch (CoreException e) {
                        e.printStackTrace();
                    }
                    if (obj instanceof SourceCodeView) {
                        sourceCodeView = (SourceCodeView)obj;
                    }
                }
            }
        }
    }
    
    /**
     * Creates an instance for a source code view.
     * @return the instance for the source code view
     */
    public static SourceCodeView create() {
        return sourceCodeView;
    }
}
