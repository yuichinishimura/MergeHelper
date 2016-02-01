/*
 *  Copyright 2015
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changereplayer.ui;

import org.jtool.changereplayer.Activator;
import org.jtool.changerecorder.util.Time;
import org.jtool.changerepository.data.FileInfo;
import org.jtool.changerepository.data.PackageInfo;
import org.jtool.changerepository.data.ProjectInfo;
import org.jtool.changerepository.data.RepositoryManager;
import org.jtool.changerepository.data.WorkspaceInfo;
import org.jtool.changerepository.event.RepositoryChangedEvent;
import org.jtool.changerepository.event.RepositoryChangedListener;
import org.jtool.changerepository.event.RepositoryEventSource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeNodeContentProvider;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.part.ViewPart;
import java.util.List;

/**
 * Displays a package explorer.
 * @author Katsuhisa Maruyama
 */
public class PackageExplorerView extends ViewPart implements RepositoryChangedListener {
    
    /**
     * The identification string that is used to register this view.
     */
    public static final String ID = "ChangeHistory.view.PackageExplorerView";
    
    /**
     * The icon images.
     */
    private static ImageDescriptor repositoryIcon = Activator.getImageDescriptor("icons/history_rep.gif");
    private static ImageDescriptor refreshIcon = Activator.getImageDescriptor("icons/nav_refresh.gif");
    
    /**
     * The information on the file of interest.
     */
    private FileInfo fileInfo;
    
    /**
     * The table viewer for project selection
     */
    private TreeViewer viewer;
    
    /**
     * The editor that displays the change history.
     */
    private ChangeHistoryEditor editor;
    
    /**
     * A flag that indicates if this view displays a repository in the internal workspace.
     */
    private boolean useInternalWorkspace = true;
    
    /**
     * The checker thats checks if operations are replay-able.
     */
    private OperationChecker operationChecker;
    
    /**
     * Creates a package explorer.
     */
    public PackageExplorerView() {
        RepositoryEventSource.getInstance().addEventListener(this);
        
        operationChecker = new OperationChecker();
    }
    
    /**
     * Creates the SWT control for this view.
     * @param parent the parent control
     */
    @Override
    public void createPartControl(Composite parent) {
        viewer = new TreeViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
        viewer.setContentProvider(new TreeNodeContentProvider());
        viewer.setLabelProvider(new ProjectLabelProvider());
        
        registerDoubleClickAction();
        registerContextMenuAction();
        
        viewer.setInput(getProjectNodes());
        viewer.refresh();
        
        makeToolBarActions();
    }
    
    /**
     * Sets the focus to the viewer's control.
     */
    @Override
    public void setFocus() {
        viewer.getControl().setFocus();
    }
    
    /**
     * Disposes of this view.
     */
    @Override
    public void dispose() {
        RepositoryEventSource.getInstance().removeEventListener(this);
        
        super.dispose();
    }
    
    /**
     * Registers an action when a file is double-clicked.
     */
    private void registerDoubleClickAction() {
        viewer.addDoubleClickListener(new IDoubleClickListener() {
            
            /**
             * Notifies of a double click.
             * @param evt event object describing the double-click
             */
            public void doubleClick(DoubleClickEvent evt) {
                if (viewer.getSelection() instanceof IStructuredSelection) {
                    IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
                    Object element = selection.getFirstElement();
                    
                    if (element instanceof TreeNode) {
                        TreeNode node = (TreeNode)element;
                        Object value = node.getValue();
                        
                        if (value instanceof FileInfo) {
                            fileInfo = (FileInfo)value;
                            
                            editor = ChangeHistoryEditor.open();
                            if (editor != null) {
                                editor.show(fileInfo);
                            }
                        }
                    }
                }
            }
        });
    }
    
    /**
     * Returns the editor that displays the change history.
     * @return the change history editor
     */
    public ChangeHistoryEditor getEditor() {
        return editor;
    }
    
    /**
     * Makes actions on the tool bar.
     */
    private void makeToolBarActions() {
        Action workspaceAction = new Action("Workspace", IAction.AS_CHECK_BOX) {
            
            public void run() {
                if (useInternalWorkspace) {
                    useInternalWorkspace = false;
                    RepositoryManager.getInstance().setExternalWorkspace();
                } else {
                    useInternalWorkspace = true;
                    RepositoryManager.getInstance().setInternalWorkspace();
                }
            }
        };
        workspaceAction.setToolTipText("Use internal/external workspace");
        workspaceAction.setImageDescriptor(repositoryIcon);
        workspaceAction.setEnabled(true);
        workspaceAction.setChecked(true);
        
        Action readAction = new Action("Read") {
            public void run() {
                if (useInternalWorkspace) {
                    System.out.println("REFRESH");
                    RepositoryManager.getInstance().collectOperationsInDefaultPath();
                } else {
                    System.out.println("READ");
                    // RepositoryManager.getInstance().collectOperationsInDefaultPath();
                }
            }
        };
        readAction.setToolTipText("Refresh/Read change history");
        readAction.setImageDescriptor(refreshIcon);
        
        IToolBarManager manager = getViewSite().getActionBars().getToolBarManager();
        manager.add(workspaceAction);
        manager.add(readAction);
    }
    
    /**
     * Registers an action when a file is right-clicked.
     */
    private void registerContextMenuAction() {
        MenuManager menuManager = new MenuManager();
        menuManager.setRemoveAllWhenShown(true);
        menuManager.addMenuListener(new IMenuListener() {
            
            /**
             * Notifies this listener that the menu is about to be shown by the given menu manager.
             * @param manager the menu manager
             */
            public void menuAboutToShow(IMenuManager manager) {
                if (viewer.getSelection() instanceof IStructuredSelection) {
                    IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
                    Object element = selection.getFirstElement();
                    
                    if (element instanceof TreeNode) {
                        TreeNode node = (TreeNode)element;
                        Object value = node.getValue();
                        
                        if (value instanceof FileInfo) {
                            fillContextMenu(manager, (FileInfo)value);
                        }
                    }
                }
            }
        });
        
        Menu menu = menuManager.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuManager, viewer);
    }
    
    /**
     * Fills the menu items.
     * @param manager the menu manager
     * @param finfo the source file information
     */
    private void fillContextMenu(IMenuManager manager, final FileInfo finfo) {
        manager.setRemoveAllWhenShown(true);
        
        Action action = new Action("Check Code...") {
            
            /**
             * Performs the action.
             */
            public void run() {
                operationChecker.open();
                operationChecker.checkCode(finfo);
            }
        };
        
        manager.add(action);
    }
    
    /**
     * Returns the file information.
     * @return the file information
     */
    public FileInfo getFileInfo() {
        return fileInfo;
    }
    
    /**
     * Receives the repository changed event.
     * @param evt the sent and received event
     */
    public void notify(RepositoryChangedEvent evt) {
        viewer.setInput(getProjectNodes());
        viewer.refresh();
    }
    
    /**
     * Obtains all nodes for projects within the workspace.
     * @return the collection of the project nodes
     */
    private TreeNode[] getProjectNodes() {
        WorkspaceInfo winfo = RepositoryManager.getInstance().getWorkspaceInfo();
        List<ProjectInfo> projects = winfo.getAllProjectInfo();
        TreeNode[] nodes = new TreeNode[projects.size()];
        for (int i = 0; i < projects.size(); i++) {
            ProjectInfo pinfo = projects.get(i);
            
            TreeNode node = new TreeNode(pinfo);
            TreeNode[] packageNodes = getPackageNodes(pinfo, node);
            node.setChildren(packageNodes);
            node.setParent(null);
            nodes[i] = node;
        }
        return nodes;
    }
    
    /**
     * Obtains all nodes for packages within the project.
     * @param the project information
     * @param the parent of the created package nodes
     * @return the collection of the package nodes
     */
    private TreeNode[] getPackageNodes(ProjectInfo pinfo, TreeNode parent) {
        List<PackageInfo> packages = pinfo.getAllPackageInfo();
        TreeNode[] nodes = new TreeNode[packages.size()];
        for (int i = 0; i < packages.size(); i++) {
            PackageInfo painfo = packages.get(i);
            
            TreeNode node = new TreeNode(painfo);
            TreeNode[] fileNodes = getFileNodes(painfo, node);
            node.setChildren(fileNodes);
            node.setParent(parent);
            
            nodes[i] = node;
        }
        return nodes;
    }
    
    /**
     * Obtains all nodes for files in the package.
     * @param the package information
     * @param the parent of the created file nodes
     * @return the collection of file nodes
     */
    private TreeNode[] getFileNodes(PackageInfo painfo, TreeNode parent) {
        List<FileInfo> files = painfo.getAllFileInfo();
        TreeNode[] nodes = new TreeNode[files.size()];
        for (int i = 0; i < files.size(); i++) {
            FileInfo finfo = files.get(i);
            
            TreeNode node = new TreeNode(finfo);
            node.setChildren(null);
            node.setParent(parent);
            
            nodes[i] = node;
        }
        return nodes;
    }
}

/**
 * Manages a label provider for the project selection view.
 */
class ProjectLabelProvider extends LabelProvider {
    
    /**
     * The icon images.
     */
    private static final Image projectImage = Activator.getImageDescriptor("icons/projects.gif").createImage();
    private static final Image packageImage = Activator.getImageDescriptor("icons/package_obj.gif").createImage();
    private static final Image fileImage = Activator.getImageDescriptor("icons/jcu_obj.gif").createImage();
    private static final Image warningImage = Activator.getImageDescriptor("icons/warning.gif").createImage();
    
    /**
     * Returns the image for the element.
     * @param element the element displayed in the view
     */
    @Override
    public Image getImage(Object element) {
        if (element instanceof TreeNode) {
            TreeNode node = (TreeNode)element;
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
    
    /**
     * Returns the text string for the element.
     * @param element the element displayed in the view
     */
    @Override
    public String getText(Object element) {
        if (element instanceof TreeNode) {
            TreeNode node = (TreeNode)element;
            Object value = node.getValue();
            
            if (value instanceof ProjectInfo) {
                ProjectInfo pinfo = (ProjectInfo)value;
                
                String timeInfo = "(" + Time.toUsefulFormat(pinfo.getTimeFrom()) +
                                  " - " + Time.toUsefulFormat(pinfo.getTimeTo()) + ")";
                return pinfo.getName() + " " + timeInfo;
            
            } else if (value instanceof PackageInfo) {
                PackageInfo painfo = (PackageInfo)value;
                
                String timeInfo = "(" + Time.toUsefulFormat(painfo.getTimeFrom()) +
                                  " - " + Time.toUsefulFormat(painfo.getTimeTo()) + ")";
                return painfo.getName() + " " + timeInfo;
            
            } else if (value instanceof FileInfo) {
                FileInfo finfo = (FileInfo)value;
                
                int num = finfo.getOperationNumber();
                String timeInfo = "(" + Time.toUsefulFormat(finfo.getTimeFrom()) +
                                  " - " + Time.toUsefulFormat(finfo.getTimeTo()) + ")";
                return finfo.getName() + " " + timeInfo + " [" + num + "]";
            }
        }
        
        return "UNKNOWN";
    }
}
