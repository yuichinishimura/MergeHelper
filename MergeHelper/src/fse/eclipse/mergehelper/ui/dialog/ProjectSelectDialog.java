package fse.eclipse.mergehelper.ui.dialog;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import fse.eclipse.mergehelper.element.BranchRootInfo;
import fse.eclipse.mergehelper.util.ProjectUtil;

public class ProjectSelectDialog extends AbstractMHDialog {
    private static final String TITLE = "MergeHelper";

    private IProject project;
    private Table projTable, acceptTable, joinTable;
    private Button okButton;

    public ProjectSelectDialog(Composite parent) {
        super(parent);
    }

    @Override
    protected void nextProgress() {
        String acceptName = getSelectionItem(acceptTable);
        String joinName = getSelectionItem(joinTable);

        BranchRootInfo.create(project, acceptName, joinName);

        ConflictDetectingDialog nextDialog = new ConflictDetectingDialog(parent);
        nextDialog.show();

        finish();
    }

    @Override
    protected void createDialog() {
        dialog.setLayout(new GridLayout(1, false));

        Label projectLabel = new Label(dialog, SWT.NONE);
        projectLabel.setText("Please select the project which has operations file.");
        projectLabel.setLayoutData(grabExFILLGridData());

        projTable = new Table(dialog, SWT.SINGLE | SWT.FULL_SELECTION);
        GridData projTableData = grabExFILLGridData();
        projTableData.heightHint = tableHeight;
        projTableData.widthHint = tableWidth / 2;
        projTable.setLayoutData(projTableData);
        projTable.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                okButton.setEnabled(false);

                String projName = getSelectionItem(projTable);
                project = ProjectUtil.convertIProject(projName);

                List<String> nameList = ProjectUtil.getBranchHistoryNameList(project);
                setBranchNameTableItem(acceptTable, nameList);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
            }
        });
        setProjectTableItem();

        addHorizontalLine();

        Label acceptBranchLabel = new Label(dialog, SWT.NONE);
        acceptBranchLabel.setText("Please select the branch which is targeted for merge.");
        acceptBranchLabel.setLayoutData(grabExFILLGridData());

        acceptTable = new Table(dialog, SWT.SINGLE | SWT.FULL_SELECTION);
        GridData acceptTableData = grabExFILLGridData();
        acceptTableData.heightHint = tableHeight;
        acceptTableData.widthHint = tableWidth / 2;
        acceptTable.setLayoutData(acceptTableData);
        acceptTable.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                okButton.setEnabled(false);

                List<String> nameList = ProjectUtil.getBranchHistoryNameList(project);
                nameList.remove(getSelectionItem(acceptTable));
                setBranchNameTableItem(joinTable, nameList);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
            }
        });

        Label joinBranchLabel = new Label(dialog, SWT.NONE);
        joinBranchLabel.setText("Please select the another branch.");
        joinBranchLabel.setLayoutData(grabExFILLGridData());

        joinTable = new Table(dialog, SWT.SINGLE | SWT.FULL_SELECTION);
        GridData joinTableData = grabExFILLGridData();
        joinTableData.heightHint = tableHeight;
        joinTableData.widthHint = tableWidth / 2;
        joinTable.setLayoutData(joinTableData);
        joinTable.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                okButton.setEnabled(true);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
            }
        });

        addHorizontalLine();

        okButton = new Button(dialog, SWT.PUSH);
        okButton.setText("OK");
        GridData okButtonGrid = new GridData(GridData.HORIZONTAL_ALIGN_END);
        okButtonGrid.widthHint = buttonWidth;
        okButton.setLayoutData(okButtonGrid);
        okButton.setEnabled(false);
        okButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                nextProgress();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
            }
        });
    }

    @Override
    protected String getDialogTitle() {
        return TITLE;
    }

    private void setProjectTableItem() {
        TableItem item;
        IProject[] ps = ProjectUtil.getProjects();
        for (IProject p : ps) {
            if (ProjectUtil.hasMultipleBranchHistory(p)) {
                item = new TableItem(projTable, SWT.NONE);
                item.setText(p.getName());
            }
        }
    }

    private void setBranchNameTableItem(Table table, List<String> nameList) {
        table.removeAll();

        TableItem item;
        for (String name : nameList) {
            item = new TableItem(table, SWT.NONE);
            item.setText(name);
        }
    }

    /**
     * 指定Tableの選択されているItem(文字列)を返す
     *
     * @param table 指定Table
     * @return 選択されているItem(文字列)
     */
    private String getSelectionItem(Table table) {
        TableItem[] item = table.getSelection();
        return item[0].getText(0);
    }
}
