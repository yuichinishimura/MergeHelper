package fse.eclipse.mergehelper.ui;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.ScrollBar;
import org.jtool.changereplayer.ui.SourceCodeControl;
import org.jtool.changereplayer.ui.SourceCodeView;
import org.jtool.changerepository.data.FileInfo;

public class MH_SourceCodeControl extends SourceCodeControl {

    public MH_SourceCodeControl(SourceCodeView view) {
        super(view);
    }

    @Override
    public void update() {
        FileInfo fInfo = sourcecodeView.getFileInfo();
        int idx = sourcecodeView.getCurrentOperationIndex();
        int mergeIdx = MH_HistoryView.getInstance().getAMergeIndex();
        if (fileInfo != null && fileInfo.equals(fInfo)) {
            if ((idx < mergeIdx && mergeIdx >= 0) || currentCode == null) {
                currentCode = fInfo.getCode(idx);
            } else {
                currentCode = fInfo.getCode(currentCode, prevCodeIndex, idx);
            }
        } else {
            currentCode = fInfo.getCode(idx);
        }

        fileInfo = fInfo;
        prevCodeIndex = idx;

        if (currentCode == null) {
            System.out.println("### Error occurred during the replay = " + (idx + 1));
            return;
        }

        // setText(currentCode);
        // decorateCode(currentCode);

        StyledText widget = sourceViewer.getTextWidget();
        int topIdx = widget.getTopIndex();
        ScrollBar vbar = widget.getVerticalBar();
        ScrollBar hbar = widget.getVerticalBar();
        int vsel = vbar.getSelection();
        int hsel = hbar.getSelection();

        widget.setText(currentCode);
        decorateCode(currentCode);
        widget.setTopIndex(topIdx);
        vbar.setSelection(vsel);
        hbar.setSelection(hsel);
    }
}
