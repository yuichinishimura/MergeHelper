package fse.eclipse.mergehelper;

import java.io.File;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "MergeHelper"; //$NON-NLS-1$

    // The shared instance
    private static Activator plugin;

    private static String WORKING_DIR_PATH;

    /**
     * The constructor
     */
    public Activator() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext )
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;

        WORKING_DIR_PATH = plugin.getStateLocation().toOSString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext )
     */
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static Activator getPlugin() {
        return plugin;
    }

    public static String getWorkingDirPath() {
        return WORKING_DIR_PATH;
    }

    public static File getWorkingDir() {
        return new File(WORKING_DIR_PATH);
    }

    public static IWorkbenchPage getWorkbenchPage() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        return window.getActivePage();
    }

    public static ImageDescriptor getChangeTrackerImageDescriptor(String path) {
        return org.jtool.changereplayer.Activator.getImageDescriptor(path);
    }

    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    public static Image getChangeTrackerImage(String path) {
        return getChangeTrackerImageDescriptor(path).createImage();
    }

    public static Image getImage(String path) {
        return getImageDescriptor(path).createImage();
    }
}
