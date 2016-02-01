package fse.eclipse.branchrecorder;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import fse.eclipse.branchrecorder.changerecorder.DirectoryWatcher;
import fse.eclipse.branchrecorder.commit.CommitListener;
import fse.eclipse.branchrecorder.util.PathUtil;

public class Activator extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "BranchRecorder";

    // The shared instance
    private static Activator plugin;

    public Activator() {
    }

    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;

        // ChangeRecorder出力監視
        DirectoryWatcher.start(PathUtil.getOperationHistoryDirPath());
        // Commit監視
        CommitListener.getInstance().start();

        System.out.println(PLUGIN_ID + " activated.");
    }

    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    public static Activator getPlugin() {
        return plugin;
    }
}
