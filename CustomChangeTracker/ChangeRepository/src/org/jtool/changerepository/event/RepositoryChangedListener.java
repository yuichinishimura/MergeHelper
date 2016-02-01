/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changerepository.event;

import java.util.EventListener;

/**
 * Defines the listener interface for receiving a changed event.
 * @author Katsuhisa Maruyama
 */
public interface RepositoryChangedListener extends EventListener {
    
    /**
     * Notifies the changed event sent from the event source to event listener.
     * @param evt the sent and received event
     */
    public void notify(RepositoryChangedEvent evt);
}
