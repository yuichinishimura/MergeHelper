package fse.eclipse.mergehelper.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.jface.viewers.TreeNodeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.jtool.changerecorder.util.Time;
import org.jtool.changereplayer.ui.PackageExplorerView;
import org.jtool.changerepository.data.FileInfo;
import org.jtool.changerepository.data.PackageInfo;
import org.jtool.changerepository.data.ProjectInfo;
import org.jtool.changerepository.data.RepositoryElementInfo;
import org.jtool.changerepository.data.RepositoryManager;
import org.jtool.changerepository.data.WorkspaceInfo;
import org.jtool.changerepository.event.RepositoryChangedEvent;
import org.jtool.changerepository.operation.UnifiedOperation;

import fse.eclipse.mergehelper.Activator;
import fse.eclipse.mergehelper.element.BranchRootInfo;
import fse.eclipse.mergehelper.element.ConflictInfo;
import fse.eclipse.mergehelper.ui.dialog.ProjectSelectDialog;
import fse.eclipse.mergehelper.ui.dialog.ResultDialog;

public class MH_PackageExplorerView extends PackageExplorerView {

    public static final String ID = "ChangeHistory.view.MH_PackageExplorerView";

    private static final ImageDescriptor projectImage = Activator.getChangeTrackerImageDescriptor("icons/projects.gif");
    private static final ImageDescriptor resultDialogImage = Activator.getImageDescriptor("icons/dialog.gif");

    private static MH_PackageExplorerView instance;

    private FileInfo fileInfo;
    private TreeViewer viewer;
    private MH_ChangeHistoryEditor editor;

    private Action resultDialogButton;

    public MH_PackageExplorerView() {
        super();
        instance = this;
    }

    public static MH_PackageExplorerView getInstance() {
        return instance;
    }

    public void setEnableDialogButton(boolean enabled) {
        resultDialogButton.setEnabled(enabled);
    }

    @Override
    public void createPartControl(Composite parent) {
        viewer = new TreeViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
        viewer.setContentProvider(new TreeNodeContentProvider());
        viewer.setLabelProvider(new ProjectLabelProvider());

        registerDoubleClickAction();

        viewer.refresh();

        makeToolBarActions(parent);
    }

    @Override
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    /**
     * {@link org.jtool.changereplayer.ui.PackageExplorerView#registerDoubleClickAction()}
     */
    private void registerDoubleClickAction() {
        viewer.addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent evt) {
                if (viewer.getSelection() instanceof IStructuredSelection) {
                    IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
                    Object element = selection.getFirstElement();

                    if (element instanceof TreeNode) {
                        TreeNode node = (TreeNode) element;
                        Object value = node.getValue();

                        if (value instanceof FileInfo) {

                            fileInfo = (FileInfo) value;

                            editor = MH_ChangeHistoryEditor.open();
                            if (editor != null) {
                                editor.show(fileInfo);
                            }
                        }
                    }
                }
            }
        });
    }

    private void makeToolBarActions(Composite parent) {
        Action runButton = new Action("MergeHelper-Run", IAction.AS_PUSH_BUTTON) {
            @Override
            public void run() {
                resultDialogButton.setEnabled(false);
                ProjectSelectDialog dialog = new ProjectSelectDialog(parent);
                dialog.show();
            }
        };
        runButton.setToolTipText("MergeHelper-Run");
        runButton.setImageDescriptor(projectImage);

        resultDialogButton = new Action("Reopen Result Dialog", IAction.AS_PUSH_BUTTON) {
            @Override
            public void run() {
                ResultDialog dialog = new ResultDialog(parent);
                dialog.show();
            }
        };
        resultDialogButton.setToolTipText("Reopen Result Dialog");
        resultDialogButton.setImageDescriptor(resultDialogImage);
        resultDialogButton.setEnabled(false);

        IToolBarManager manager = getViewSite().getActionBars().getToolBarManager();
        manager.add(runButton);
        manager.add(resultDialogButton);
    }

    @Override
    public void notify(RepositoryChangedEvent evt) {
        // no execute
        return;
    }

    public void refresh() {
        if (viewer != null) {
            viewer.setInput(getProjectNodes());
            viewer.refresh();
            viewer.collapseAll();
            resultDialogButton.setEnabled(true);
        }
    }

    private TreeNode[] getProjectNodes() {
        WorkspaceInfo winfo = RepositoryManager.getInstance().getWorkspaceInfo();
        List<ProjectInfo> projects = winfo.getAllProjectInfo();
        int size = projects.size();
        TreeNode[] nodes = new TreeNode[size];
        for (int i = 0; i < size; i++) {
            ProjectInfo pinfo = projects.get(i);

            TreeNode node = new TreeNode(pinfo);
            TreeNode[] packageNodes = getPackageNodes(pinfo, node);
            node.setChildren(packageNodes);
            node.setParent(null);
            nodes[i] = node;
        }
        return nodes;
    }

    private TreeNode[] getPackageNodes(ProjectInfo pinfo, TreeNode parent) {
        List<PackageInfo> packages = pinfo.getAllPackageInfo();
        int size = packages.size();
        List<TreeNode> nodes = new ArrayList<TreeNode>(size);
        for (int i = 0; i < size; i++) {
            PackageInfo painfo = packages.get(i);

            TreeNode node = new TreeNode(painfo);
            TreeNode[] fileNodes = getFileNodes(painfo, node);
            if (fileNodes.length != 0) {
                node.setChildren(fileNodes);
                node.setParent(parent);
                nodes.add(node);
            }
        }
        return nodes.toArray(new TreeNode[nodes.size()]);
    }

    private TreeNode[] getFileNodes(PackageInfo paInfo, TreeNode parent) {
        List<FileInfo> files = paInfo.getAllFileInfo();
        List<TreeNode> nodes = new ArrayList<TreeNode>(files.size());
        for (FileInfo fInfo : files) {
            if (hasEditOperation(fInfo)) {
                TreeNode node = new TreeNode(fInfo);
                node.setChildren(null);
                node.setParent(parent);

                nodes.add(node);
            }
        }
        return nodes.toArray(new TreeNode[nodes.size()]);
    }

    private boolean hasEditOperation(FileInfo fInfo) {
        List<UnifiedOperation> ops = fInfo.getOperations();
        for (UnifiedOperation op : ops) {
            if (op.isTextChangedOperation()) {
                return true;
            }
        }
        return false;
    }
}

class ProjectLabelProvider extends DecoratingLabelProvider {

    private static final Image projectImage = Activator.getChangeTrackerImage("icons/projects.gif");
    private static final Image packageImage = Activator.getChangeTrackerImage("icons/package_obj.gif");
    private static final Image fileImage = Activator.getChangeTrackerImage("icons/jcu_obj.gif");
    private static final Image warningImage = Activator.getChangeTrackerImage("icons/warning.gif");

    public ProjectLabelProvider() {
        this(new WorkbenchLabelProvider(), org.jtool.changereplayer.Activator.getDefault().getWorkbench().getDecoratorManager().getLabelDecorator());
    }

    public ProjectLabelProvider(ILabelProvider provider, ILabelDecorator decorator) {
        super(provider, decorator);
    }

    @Override
    public Color getForeground(Object element) {
        Display display = Display.getCurrent();
        ConflictInfo cInfo = BranchRootInfo.getInstance().getConflictInfo();
        if (element instanceof TreeNode) {
            TreeNode node = (TreeNode) element;
            Object value = node.getValue();

            if (value instanceof ProjectInfo) {
                if (cInfo.isConflict()) {
                    return display.getSystemColor(SWT.COLOR_BLACK);
                }
            } else if (value instanceof PackageInfo) {
                PackageInfo paInfo = (PackageInfo) value;
                if (cInfo.isConflictPackage(paInfo)) {
                    return display.getSystemColor(SWT.COLOR_BLACK);
                }
            } else if (value instanceof FileInfo) {
                FileInfo fInfo = (FileInfo) value;
                if (cInfo.isConflictFile(fInfo)) {
                    return display.getSystemColor(SWT.COLOR_BLACK);
                }
            }
        }
        return display.getSystemColor(SWT.COLOR_DARK_GRAY);
    }

    @Override
    public Image getImage(Object element) {
        if (element instanceof TreeNode) {
            TreeNode node = (TreeNode) element;
            Object value = node.getValue();

            if (value instanceof ProjectInfo) {
                return projectImage;

            } else if (value instanceof PackageInfo) {
                return packageImage;

            } else if (value instanceof FileInfo) {
                return fileImage;
            }
        }
        return warningImage;
    }

    @Override
    public String getText(Object element) {
        if (element instanceof TreeNode) {
            TreeNode node = (TreeNode) element;
            Object value = node.getValue();

            StringBuilder sb = new StringBuilder();
            if (value instanceof ProjectInfo || value instanceof PackageInfo) {
                RepositoryElementInfo rInfo = (RepositoryElementInfo) value;

                sb.append(rInfo.getName()).append(" ");
                sb.append("(").append(Time.toUsefulFormat(rInfo.getTimeFrom()));
                sb.append(" - ").append(Time.toUsefulFormat(rInfo.getTimeTo()));
                sb.append(")");
                return sb.toString();

            } else if (value instanceof FileInfo) {
                FileInfo fInfo = (FileInfo) value;

                int num = fInfo.getOperationNumber();
                sb.append(fInfo.getName()).append(" ");
                sb.append("(").append(Time.toUsefulFormat(fInfo.getTimeFrom()));
                sb.append(" - ").append(Time.toUsefulFormat(fInfo.getTimeTo()));
                sb.append(") [").append(num).append("]");
                return sb.toString();
            }
        }
        return "UNKNOWN";
    }
}
