package fse.eclipse.mergehelper.ui;

import org.jtool.changereplayer.ui.SourceCodeControl;
import org.jtool.changereplayer.ui.SourceCodeView;

public class MH_SourceCodeControl extends SourceCodeControl {

    public MH_SourceCodeControl(SourceCodeView view) {
        super(view);
    }

    public void update(String code) {
        setText(code);
    }
}
