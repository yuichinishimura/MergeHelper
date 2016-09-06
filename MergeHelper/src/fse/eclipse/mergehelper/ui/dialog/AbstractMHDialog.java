package fse.eclipse.mergehelper.ui.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public abstract class AbstractMHDialog {
    protected Composite parent;
    protected Shell dialog;

    protected int tableHeight = 100;
    protected int tableWidth = 500;

    protected int buttonWidth = 80;

    AbstractMHDialog(Composite parent) {
        this.parent = parent;
        dialog = new Shell(parent.getShell(), SWT.CLOSE | SWT.TITLE | SWT.SYSTEM_MODAL);
    }

    /**
     * 遷移
     */
    abstract protected void nextProgress();

    /**
     * ダイアログレイアウト
     */
    abstract protected void createDialog();

    /**
     * ダイアログのタイトル
     *
     * @return title
     */
    abstract protected String getDialogTitle();

    /**
     * ダイアログ生成
     */
    public void show() {
        setDialogTitle(getDialogTitle());
        createDialog();
        open();
    }

    /**
     * 終了時の処理
     */
    public void finish() {
        if (dialog != null && !dialog.isDisposed()) {
            dialog.dispose();
        }
    }

    /**
     * ダイアログのタイトルを設定する
     */
    protected void setDialogTitle(String title) {
        dialog.setText(title);
    }

    protected void open() {
        if (dialog != null && !dialog.isDisposed()) {
            dialog.pack();
            dialog.open();
        }
    }

    /**
     * grabExcess*Spaceを設定したGridDataを返す
     *
     * @return grabExcess*Space設定済みGridData
     */
    protected GridData grabExFILLGridData() {
        GridData gridData = new GridData(SWT.FILL);
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        return gridData;
    }

    /**
     * 指定コンポジットに横線を加えるもの
     *
     * @param parent 指定コンポジット
     */
    protected void addHorizontalLine() {
        Label labelLine = new Label(dialog, SWT.SEPARATOR | SWT.HORIZONTAL);
        GridData lineData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        labelLine.setLayoutData(lineData);
    }
}
