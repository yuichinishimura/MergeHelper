/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changereplayer;

import org.eclipse.jface.dialogs.MessageDialog;

/**
 * Displays dialogs with messages.
 * @author Katsuhisa Maruyama
 */
public class Message {
    
    /**
     * The flag indicating if this plug-in runs under the debug mode.
     */
    private final static boolean DEBUG = true;
    
    /**
     * Displays an information dialog presenting the specified message.
     * @param msg the message to be presented
     */
    public static void informationDialog(String msg) {
        MessageDialog.openInformation(null, "OperationReplayerJ", msg);
    }
    
    /**
     * Displays an error dialog presenting the specified message.
     * @param msg the message to be presented
     */
    public static void errorDialog(String msg) {
        MessageDialog.openError(null, "OperationReplayerJ", msg);
    }
    
    /**
     * Displays a yes/no question dialog presenting the specified message.
     * @param msg the message to be presented
     * @return <code>true</code> if yes button was pushed, otherwise <code>false</code>
     */
    public static boolean yesnoDialog(String msg) {
        return MessageDialog.openQuestion(null, "OperationReplayerJ", msg);
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
}
