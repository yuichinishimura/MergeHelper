package fse.eclipse.mergehelper.ui;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorSite;
import org.jtool.changereplayer.ui.ButtonControl;
import org.jtool.changereplayer.ui.SourceCodeView;
import org.jtool.changerepository.data.FileInfo;
import org.jtool.changerepository.operation.UnifiedOperation;

import fse.eclipse.mergehelper.Activator;

public class MH_ButtonControl extends ButtonControl {
    private Action prevAction;
    private Action nextAction;

    private static ImageDescriptor prevSliceIcon = Activator.getChangeTrackerImageDescriptor("icons/left3.gif");
    private static ImageDescriptor nextSliceIcon = Activator.getChangeTrackerImageDescriptor("icons/right3.gif");

    public MH_ButtonControl(MH_SourceCodeView view) {
        super((SourceCodeView) view);
    }

    @Override
    protected void createAction(IEditorSite site, List<Action> actions) {
        super.createAction(site, actions);

        String fToolTipText = forwardAction.getToolTipText();
        ImageDescriptor forwardIcon = forwardAction.getImageDescriptor();
        boolean fEnable = forwardAction.isEnabled();
        actions.remove(forwardAction);

        forwardAction = new Action("Forward") {
            public void run() {
                if (sourcecodeView.getFileInfo() != null) {
                    if (sourcecodeView.getCurrentOperationIndex() < getTableSize()) {
                        sourcecodeView.goTo(sourcecodeView.getCurrentOperationIndex() + 1);
                    }
                }
            }
        };
        forwardAction.setToolTipText(fToolTipText);
        forwardAction.setImageDescriptor(forwardIcon);
        forwardAction.setEnabled(fEnable);
        actions.add(2, forwardAction);

        String lToolTipText = lastAction.getToolTipText();
        ImageDescriptor fastForwardIcon = lastAction.getImageDescriptor();
        boolean lEnable = lastAction.isEnabled();
        actions.remove(lastAction);

        lastAction = new Action("Last") {
            public void run() {
                if (sourcecodeView.getFileInfo() != null) {
                    sourcecodeView.goTo(getTableSize() - 1);
                }
            }
        };
        lastAction.setToolTipText(lToolTipText);
        lastAction.setImageDescriptor(fastForwardIcon);
        lastAction.setEnabled(lEnable);
        actions.add(3, lastAction);

        prevAction = new Action("Prev") {
            public void run() {
                FileInfo fInfo = sourcecodeView.getFileInfo();
                if (fInfo != null) {
                    Table table = MH_HistoryView.getInstance().getOperationTable();
                    int idx = getPreviousCheckedOperation(table, sourcecodeView.getCurrentOperationIndex());
                    sourcecodeView.goTo(idx);
                }
            }
        };
        prevAction.setToolTipText("Go to the previous conflict operation");
        prevAction.setImageDescriptor(prevSliceIcon);
        prevAction.setEnabled(false);
        actions.add(1, prevAction);

        nextAction = new Action("Next") {
            public void run() {
                FileInfo fInfo = sourcecodeView.getFileInfo();
                if (fInfo != null) {
                    Table table = MH_HistoryView.getInstance().getOperationTable();
                    int idx = getNextCheckedOperation(table, sourcecodeView.getCurrentOperationIndex());
                    sourcecodeView.goTo(idx);
                }
            }
        };
        nextAction.setToolTipText("Go to the next conflict operation");
        nextAction.setImageDescriptor(nextSliceIcon);
        nextAction.setEnabled(false);
        actions.add(4, nextAction);
    }

    private int getTableSize() {
        return MH_HistoryView.getInstance().getTableSize();
    }

    private int getPreviousCheckedOperation(Table table, int cur) {
        TableItem[] items = table.getItems();
        if (cur > 0) {
            try {
                for (int i = cur - 1; i >= 0; i--) {
                    if (items[i].getChecked()) {
                        return i;
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                return -1;
            }
        }
        return -1;
    }

    private int getNextCheckedOperation(Table table, int cur) {
        TableItem[] items = table.getItems();
        int length = items.length;
        if (length == 0) {
            return 1;
        }

        if (cur + 1 < length) {
            try {
                for (int i = cur + 1; i < length; i++) {
                    if (items[i].getChecked()) {
                        return i;
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                return -1;
            }
        }
        return -1;
    }

    @Override
    public void updateButtonStates(int idx, List<UnifiedOperation> ops) {
        if (idx == 0) {
            backwardAction.setEnabled(false);
            firstAction.setEnabled(false);
        } else {
            backwardAction.setEnabled(true);
            firstAction.setEnabled(true);
        }

        MH_HistoryView view = MH_HistoryView.getInstance();
        if (view != null) {
            int tableMax = view.getTableSize() - 1;
            if (idx == tableMax) {
                forwardAction.setEnabled(false);
                lastAction.setEnabled(false);
            } else {
                forwardAction.setEnabled(true);
                lastAction.setEnabled(true);
            }

            Table table = MH_HistoryView.getInstance().getOperationTable();
            int prev = getPreviousCheckedOperation(table, sourcecodeView.getCurrentOperationIndex());
            if (prev != -1) {
                prevAction.setEnabled(true);
            } else {
                prevAction.setEnabled(false);
            }

            int next = getNextCheckedOperation(table, sourcecodeView.getCurrentOperationIndex());
            if (next != -1) {
                nextAction.setEnabled(true);
            } else {
                nextAction.setEnabled(false);
            }
        }
    }
}
