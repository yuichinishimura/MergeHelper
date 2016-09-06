package fse.eclipse.mergehelper.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.jtool.changereplayer.ui.ChangeHistoryEditor;
import org.jtool.changereplayer.ui.SourceCodeViewLoader;

import fse.eclipse.mergehelper.Activator;

public class MH_ChangeHistoryEditor extends ChangeHistoryEditor {
    public static final String ID = "ChangeReplayer.editor.MH_ChangeHistoryEditor";

    private static MH_ChangeHistoryEditor editor;

    public static MH_ChangeHistoryEditor open() {
        if (editor != null) {
            MH_HistoryView.getInstance().refresh();
            return editor;
        }
        try {
            IWorkbenchPage page = Activator.getWorkbenchPage();
            IEditorPart editorPart = IDE.openEditor(page, new EmptyEditorInput(), ID, true);
            if (editorPart instanceof MH_ChangeHistoryEditor) {
                editor = (MH_ChangeHistoryEditor) editorPart;
            }
        } catch (PartInitException e) {
            e.printStackTrace();
        }
        return editor;
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        setSite(site);
        setInput(input);
        activeView = MH_viewLoader();
        activeView.createBottonActions(site);
    }

    private MH_SourceCodeView MH_viewLoader() {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint point = registry.getExtensionPoint(SourceCodeViewLoader.EXTENSION_POINT_ID);
        if (point == null) {
            return null;
        }

        IExtension[] extensions = point.getExtensions();
        if (extensions.length > 0) {
            for (IExtension ex : extensions) {
                if (ex.getNamespaceIdentifier().equals(Activator.PLUGIN_ID)) {
                    IConfigurationElement[] elems = ex.getConfigurationElements();
                    for (IConfigurationElement h : elems) {
                        if (h.getName().compareTo("sourcecodeview") == 0) {
                            Object obj = null;
                            try {
                                obj = h.createExecutableExtension("class");
                            } catch (CoreException e) {
                                e.printStackTrace();
                            }
                            if (obj instanceof MH_SourceCodeView) {
                                return (MH_SourceCodeView) obj;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}

/**
 * {@link org.jtool.changereplayer.ui.ChangeHistoryEditor#EmptyEditorInput}
 */
class EmptyEditorInput implements IEditorInput {

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return ImageDescriptor.getMissingImageDescriptor();
    }

    @Override
    public String getName() {
        return "Non-file";
    }

    @Override
    public String getToolTipText() {
        return getName();
    }

    @Override
    public IPersistableElement getPersistable() {
        return null;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object getAdapter(Class adapter) {
        return null;
    }
}
