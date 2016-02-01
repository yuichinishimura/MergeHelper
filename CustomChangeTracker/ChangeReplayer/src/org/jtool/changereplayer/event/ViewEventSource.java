/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changereplayer.event;

import java.util.HashSet;

/**
 * Manages an event source for views.
 * @author Katsuhisa Maruyama
 */
public class ViewEventSource {
    
    /**
     * The collection of listeners that receives change events.
     */
    protected HashSet<ViewChangedListener> listeners = new HashSet<ViewChangedListener>();
    
    /**
     * The single instance.
     */
    private static ViewEventSource instance = new ViewEventSource();
    
    /**
     * Prohibits an instance.
     */
    private ViewEventSource() {
    }
    
    /**
     * Returns the single instance of this event source.
     * @return the event source instance
     */
    public static ViewEventSource getInstance() {
        return instance;
    }
    
    /**
     * Adds the listener in order to receive a changed event from this source.
     * @param listener the changed listener to be added
     */
    public void addEventListener(ViewChangedListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Removes the listener which no longer receives a changed event from this source.
     * @param listener the changed listener to be removed
     */
    public void removeEventListener(ViewChangedListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Sends the changed event to all the listeners.
     * @param evt the changed event.
     */
    public void fire(ViewChangedEvent evt) {
        for (ViewChangedListener listener : listeners) {
            listener.notify(evt);
        }
    }
}
