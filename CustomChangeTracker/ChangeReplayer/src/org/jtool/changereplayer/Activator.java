/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changereplayer;

import org.jtool.changereplayer.ui.SourceCodeViewLoader;
import org.jtool.changerepository.data.RepositoryManager;
import org.osgi.framework.BundleContext;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * The activator class that manages plug-in information.
 * @author Katsuhisa Maruyama
 */
public class Activator extends AbstractUIPlugin implements IStartup {
    
    /**
     * The plug-in ID.
     */
    public static final String PLUGIN_ID = "ChangeReplayer";
    
    /**
     * This plug-in.
     */
    private static Activator plugin;
    
    /**
     * Creates a UI plug-in runtime instance.
     */
    public Activator() {
    }
    
    /**
     * Performs actions when the plug-in is activated.
     * @param context the bundle context for this plug-in
     * @throws Exception if this plug-in did not start up properly
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        
        plugin = this;
        
        SourceCodeViewLoader sourceCodeViewLoader = new SourceCodeViewLoader();
        sourceCodeViewLoader.loadExtensions();
        
        RepositoryManager.getInstance().collectOperationsInDefaultPath();
        
        System.out.println("ChangeReplayer activated.");
    }
    
    /**
     * Will be called in a separate thread after the workbench initializes.
     */
    @Override
    public void earlyStartup() { 
    }
    
    /**
     * Performs actions when when the plug-in is shut down.
     * @param context the bundle context for this plug-in
     * @throws Exception if this this plug-in fails to stop
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
        
        plugin = null;
    }
    
    /**
     * Returns the default plug-in instance.
     * @return the default plug-in instance
     */
    public static Activator getDefault() {
        return plugin;
    }
    
    /**
     * Returns the root of the workspace.
     * @return the workspace root
     */
    public static IWorkspaceRoot getWorkspaceRoot() {
        return ResourcesPlugin.getWorkspace().getRoot();
    }
    
    /**
     * Returns the path indicating the workspace.
     * @return the workspace path
     */
    public static IPath getWorkspacePath() {
        IWorkspaceRoot root = getWorkspaceRoot();
        return root.getLocation();
    }
    
    /**
     * Returns the page of the workbench.
     * @return the workbench page
     */
    public static IWorkbenchPage getWorkbenchPage() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        return window.getActivePage();
    }
    
    /**
     * Returns the image descriptor for an image file.
     * @param path the image file
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }
}
