/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changereplayer.event;

import java.util.EventListener;

/**
 * Defines the listener interface for receiving a changed event.
 * @author Katsuhisa Maruyama
 */
public interface ViewChangedListener extends EventListener {
    
    /**
     * Notifies the view changed event sent from the event source to event listener.
     * @param evt the sent and received event
     */
    public void notify(ViewChangedEvent evt);
}
