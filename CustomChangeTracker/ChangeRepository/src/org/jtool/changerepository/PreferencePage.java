/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changerepository;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;

/**
 * Manages the preference page.
 * @author Katsuhisa Maruyama
 */
public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
    
    /**
     * Merges two successive operations with the same string whose content is stored as the inserted text of
     *   the former operation and stored as the deleted text of the latter one if this value is true.
     */
    static final String MERGE_OPERATIONS = "merge.opetrations";
    
    /**
     * The default value that indicates if two successive operations will be merged.
     */
    static final boolean MERGE_OPERATIONS_DEFAULT = true;
    
    /**
     * Creates an object for a preference page.
     */
    public PreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("A preference page of the Operation RecorderJ/ReplayerJ");
    }
    
    /**
     * Creates the field editors for preference settings.
     */
    public void createFieldEditors() {
        addField(new BooleanFieldEditor(MERGE_OPERATIONS,
          "Merges two successive operations with respect to the Kana-Kanji conversion", getFieldEditorParent()));
        
        addField(new BooleanFieldEditor(MERGE_OPERATIONS,
          "Merges two successive operations with respect to the Kana-Kanji conversion", getFieldEditorParent()));
    }
    
    /**
     * Initializes a preference page for a given workbench.
     */
    public void init(IWorkbench workbench) {
    }
}
