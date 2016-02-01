/*
 *  Copyright 2015
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changereplayer.ui;

import org.jtool.changerepository.data.FileInfo;
import org.jtool.changerepository.operation.UnifiedOperation;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
import org.eclipse.jdt.internal.ui.text.SimpleJavaSourceViewerConfiguration;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import java.util.List;
import java.util.ArrayList;

/**
 * Creates a source code control for replay.
 * @author Takayuki Omori
 * @author Katsuhisa Maruyama
 */
@SuppressWarnings("restriction")
public class SourceCodeControl {
    
    /**
     * The source code view that contains this source code control.
     */
    protected SourceCodeView sourcecodeView;
    
    /**
     * The SWT widget that displays the source code on this source code control.
     */
    protected JavaSourceViewer sourceViewer;
    
    /**
     * The configuration of this source code control.
     */
    protected JavaSourceViewerConfiguration sourceViewerConf;
    
    /**
     * The information on the file of interest.
     */
    protected FileInfo fileInfo = null;
    
    /**
     * The sequence number of the operation replayed previously.
     */
    protected int prevCodeIndex = 0;
    
    /**
     * The the content of the current source code.
     */
    protected String currentCode;
    
    /**
     * The objects coloring the source code.
     */
    protected static final Color BLACK = new Color(null, 0x00, 0x00, 0x00);
    protected static final Color GRAY = new Color(null, 0xaa, 0xaa, 0xaa);
    protected static final Color RED = new Color(null, 0xff, 0xd1, 0xe8);
    protected static final Color YELLOW = new Color(null, 0xff, 0xff, 0xc6);
    protected static final Color BLUE = new Color(null, 0xd1, 0xe8, 0xff);
    protected static final Color WHITE = new Color(null, 0xff, 0xff, 0xff);
    
    /**
     * Creates an instance for a source code control.
     * @param view the source code view that contains this source code control
     */
    public SourceCodeControl(SourceCodeView view) {
        this.sourcecodeView = view;
    }
    
    /**
     * Returns the source code view that contains this source code control
     * @return the source code view
     */
    public SourceCodeView getSourceCodeView() {
        return sourcecodeView;
    }
    
    /**
     * Creates a control for this source code control.
     * @param parent the parent control
     * @param top the top control
     */
    public void createPartControl(Composite parent, Control top) {
        final int MARGIN = 0;
        IDocument document = new Document();
        
        JavaTextTools tools = JavaPlugin.getDefault().getJavaTextTools();
        tools.setupJavaDocumentPartitioner(document, IJavaPartitions.JAVA_PARTITIONING);
        sourceViewerConf = new SimpleJavaSourceViewerConfiguration(tools.getColorManager(),
                JavaPlugin.getDefault().getPreferenceStore(), null, IJavaPartitions.JAVA_PARTITIONING, false);
        
        sourceViewer = new JavaSourceViewer(parent, null, null, false,
                SWT.BORDER | SWT.MULTI | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL,
                JavaPlugin.getDefault().getPreferenceStore());
        sourceViewer.setEditable(false);
        sourceViewer.configure(sourceViewerConf);
        sourceViewer.setDocument(document);
        
        StyledText styledText = sourceViewer.getTextWidget();
        styledText.setFont(JFaceResources.getFont(PreferenceConstants.EDITOR_TEXT_FONT));
        
        FormData data = new FormData();
        data.top = new FormAttachment(top, MARGIN);
        data.bottom = new FormAttachment(100, -MARGIN);
        data.left = new FormAttachment(0, MARGIN);
        data.right = new FormAttachment(100, -MARGIN);
        sourceViewer.getControl().setLayoutData(data);
    }
    
    /**
     * Returns the control widget of this source code control.
     * @return the source viewer widget
     */
    public Control getControl() {
        return sourceViewer.getControl();
    }
    
    /**
     * Sets the focus to this source code control.
     */
    public void setFocus() {
        sourceViewer.getControl().setFocus();
    }
    
    /**
     * Disposes of this source code control.
     */
    public void dispose() {
    }
    
    /**
     * Updates this source code control.
     */
    public void update() {
        FileInfo finfo = sourcecodeView.getFileInfo();
        int idx = sourcecodeView.getCurrentOperationIndex();
        if (fileInfo != null && fileInfo.equals(finfo)) {
            currentCode = finfo.getCode(currentCode, prevCodeIndex, idx);
        } else {
            currentCode = finfo.getCode(idx);
        }
        
        fileInfo = finfo;
        prevCodeIndex = idx;
        
        if (currentCode == null) {
            System.out.println("### Error occurred during the replay = " + (idx + 1));
            return;
        }
        
        setText(currentCode);
        decorateCode(currentCode);
        
        sourceViewer.getTextWidget().setText(currentCode);
    }
    
    /**
     * Resets this source code control.
     */
    protected void reset() {
        sourceViewer.configure(sourceViewerConf);
    }
    
    /**
     * Rolls back the configuration of this source code control.
     */
    protected void unconfigure() {
        sourceViewer.unconfigure();
    }
    
    /**
     * Returns the contents of the current source code.
     * @return the contents of the current source code
     */
    protected String getCurrentCode() {
        return currentCode;
    }
    
    /**
     * Returns the SWT widget that represents the styled text for the source control.
     * @return the styled text widget
     */
    protected StyledText getStyledText() {
        return sourceViewer.getTextWidget();
    }
    
    /**
     * Returns the content of the source code displayed on this source code control.
     * @return the content of the source code
     */
    protected String getText() {
        return sourceViewer.getTextWidget().getText();
    }
    
    /**
     * Sets the content of the source code displayed on this source code control.
     * @param code the content of the source code
     */
    protected void setText(String code) {
        sourceViewer.getTextWidget().setText(code);
    }
    
    /**
     * Obtains the current selection on the source code displayed on this source code control.
     * @return the selection on the code
     */
    protected TextSelection getSelection() {
        ISelection selection = sourceViewer.getSelection();
        if (selection instanceof TextSelection) {
            return (TextSelection)selection;
        }
        return null;
    }
    
    /**
     * Decorates the representation of the source code displayed on this source code control.
     * @param code the content of the source code
     */
    protected void decorateCode(String code) {
        StyledText styledText = getStyledText();
        styledText.setStyleRange(null);
        reset();
        
        List<StyleRange> ranges = getColoredStyleRanges(code);
        int startOffset = -1;
        if (ranges.size() > 0) {
            startOffset = ranges.get(0).start;
        }
        
        for (StyleRange range : ranges) {
            styledText.setStyleRange(range);
        }
        
        reveal(startOffset, code);
    }
    
    /**
     * Obtains the colored style ranges for decorating code.
     * @param code the code to be decorated
     * @return the collection of the style ranges
     */
    protected List<StyleRange> getColoredStyleRanges(String code) {
        List<UnifiedOperation> ops = sourcecodeView.getFileInfo().getOperations();
        int idx = sourcecodeView.getCurrentOperationIndex();
        
        UnifiedOperation op = null;
        if (0 <= idx && idx < ops.size()) {
            op = ops.get(idx);
        }
        UnifiedOperation opn = null;
        if (0 <= idx && idx + 1 < ops.size()) {
            opn = ops.get(idx + 1);
        }
        
        List<StyleRange> ranges = new ArrayList<StyleRange>();
        
        int startOffset = -1;
        if (op != null) {
            if (op.isNormalOperation()) {
                int len = op.getInsertedText().length();
                int start = op.getStart();
                startOffset = start;
                
                if (len > 0 && isWithinCodeRange(start, code) && isWithinCodeRange(start + len, code)) {
                    StyleRange range = new StyleRange(start, len, BLACK, RED);
                    ranges.add(range);
                }
                
            } else if (op.isCopyOperation()) {
                int start = op.getStart();
                int len = op.getCopiedText().length();
                startOffset = start;
                
                if (len > 0 && isWithinCodeRange(start, code) && isWithinCodeRange(start + len, code)) {
                    StyleRange range = new StyleRange(start, len, BLACK, YELLOW);
                    ranges.add(range);
                }
            }
        }
        
        if (opn != null) {
            if (opn.isNormalOperation()) {
                int len = opn.getDeletedText().length();
                int start = opn.getStart();
                if (startOffset < 0) {
                    startOffset = start;
                }
                
                if (len > 0 && isWithinCodeRange(start, code) && isWithinCodeRange(start + len, code)) {
                    StyleRange range = new StyleRange(start, len, BLACK, BLUE);
                    ranges.add(range);
                }
            }
        }
        
        return ranges;
    }
    
    /**
     * Ensures that the code at the current offset is visible, scrolling the control if necessary.
     * @param offset the current offset value
     * @param code the content of the source code
     */
    protected void reveal(int offset, String code) {
        if (code == null) {
            return;
        }
        
        if (offset < 0 || code.length() < offset) {
            return;
        }
        
        StyledText styledText = getStyledText();
        Point selection = styledText.getSelectionRange();
        
        int line = styledText.getLineAtOffset(offset);
        int top = 0;
        if (line - 10 > top) {
            top = styledText.getOffsetAtLine(line - 10);
        }
        
        int bline = styledText.getLineCount() - 1;
        int bottom = styledText.getOffsetAtLine(bline);
        if (line + 10 < bline) {
            bottom = styledText.getOffsetAtLine(line + 10);
        }
        
        if (top <= selection.x && selection.x + selection.y <= bottom) {
            return;
        }
        
        styledText.setSelection(bottom);
    }
    
    /**
     * Tests if a specified point represented by a offset value is within the code range. 
     * @param offset the offset value indicating the point
     * @param the code to be checked
     * @return <code>true</code> if the point is within the code range, otherwise <code>false</code> 
     */
    protected boolean isWithinCodeRange(int offset, String code) {
        if (code == null) {
            return false;
        }
        
        if (offset < 0 || code.length() < offset) {
            return false;
        }
        return true;
    }
}
