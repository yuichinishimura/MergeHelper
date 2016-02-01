package fse.eclipse.mergehelper.ui;

import java.util.List;

import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorSite;
import org.jtool.changereplayer.event.ViewChangedEvent;
import org.jtool.changereplayer.event.ViewEventSource;
import org.jtool.changereplayer.ui.SourceCodeView;
import org.jtool.changereplayer.ui.TimelineControl;
import org.jtool.changerepository.operation.UnifiedOperation;

public class MH_SourceCodeView extends SourceCodeView {

    public MH_SourceCodeView() {
        ViewEventSource.getInstance().addEventListener(this);
    }

    @Override
    public void createBottonActions(IEditorSite site) {
        buttonControl = new MH_ButtonControl(this);
        buttonControl.makeToolBarActions(site);
    }

    @Override
    public void createControls(Composite parent) {
        FormLayout layout = new FormLayout();
        parent.setLayout(layout);

        timelineControl = new TimelineControl(this);
        timelineControl.createPartControl(parent);

        sourcecodeControl = new MH_SourceCodeControl(this);
        sourcecodeControl.createPartControl(parent, timelineControl.getControl());
    }

    @Override
    public void goTo(int idx) {
        if (getFileInfo() == null && idx < 0) {
            return;
        }

        int tableSize = MH_HistoryView.getInstance().getTableSize();
        if (tableSize <= idx) {
            return;
        }

        List<UnifiedOperation> ops = getFileInfo().getOperations();
        int opsSize = ops.size();
        if (opsSize <= idx) {
            goTo(idx, ops.get(opsSize - 1).getTime());
        } else {
            goTo(idx, ops.get(idx).getTime());
        }
    }

    public void update(String code) {
        ((MH_SourceCodeControl) sourcecodeControl).update(code);
    }

    private void goTo(int idx, long time) {
        setFocalTime(idx, time);

        List<UnifiedOperation> ops = getFileInfo().getOperations();
        buttonControl.updateButtonStates(idx, ops);

        setFocus();

        sendViewEvent();
    }
    
    @Override
    public void setFocalTime(int idx, long time) {
        currentOperationIndex = idx;
        focalTime = time;
        
        //sourcecodeControl.update();
        timelineControl.update();
    };

    private void sendViewEvent() {
        ViewChangedEvent event = new ViewChangedEvent(this);
        ViewEventSource.getInstance().fire(event);
    }
}
