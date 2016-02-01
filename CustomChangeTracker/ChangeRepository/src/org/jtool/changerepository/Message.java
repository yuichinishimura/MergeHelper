/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changerepository;

import org.eclipse.jface.dialogs.MessageDialog;

/**
 * Displays a dialog with a message.
 * @author Katsuhisa Maruyama
 */
public class Message {
    
    /**
     * The flag indicating if this plug-in runs under the debug mode.
     */
    private final static boolean DEBUG = true;
    
    /**
     * Displays an information dialog presenting the specified message.
     * @param title the title of the dialog, or <code>null</code> if none
     * @param msg the message to be presented
     */
    public static void informationDialog(String title, String msg) {
        MessageDialog.openInformation(null, title, msg);
    }
    
    /**
     * Displays an error dialog presenting the specified message.
     * @param title the title of the dialog, or <code>null</code> if none
     * @param msg the message to be presented
     */
    public static void errorDialog(String title, String msg) {
        MessageDialog.openError(null, Activator.PLUGIN_ID, msg);
    }
    
    /**
     * Displays a yes/no question dialog presenting the specified message.
     * @param title the title of the dialog, or <code>null</code> if none
     * @param msg the message to be presented
     * @return <code>true</code> if yes button was pushed, otherwise <code>false</code>
     */
    public static boolean yesnoDialog(String title, String msg) {
        return MessageDialog.openQuestion(null, Activator.PLUGIN_ID, msg);
    }
    
    /**
     * Displays a debug message on the console.
     * @param msg the message to be presented
     */
    public static void print(String msg) {
        if (DEBUG) {
            System.out.println(msg);
        }
    }
    
    /**
     * Displays an error message on the console.
     * @param msg the message to be presented
     */
    public static void error(String msg) {
        System.err.println(msg);
    }
}
