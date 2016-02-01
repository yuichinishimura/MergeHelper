/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changerepository.event;

import java.util.HashSet;

/**
 * Manages an event source for the repository.
 * @author Katsuhisa Maruyama
 */
public class RepositoryEventSource {
    
    /**
     * The collection of listeners that receives change events.
     */
    protected HashSet<RepositoryChangedListener> listeners = new HashSet<RepositoryChangedListener>();
    
    /**
     * The single instance.
     */
    private static RepositoryEventSource instance = new RepositoryEventSource();
    
    /**
     * Prohibits an instance.
     */
    private RepositoryEventSource() {
    }
    
    /**
     * Returns the single instance of this event source.
     * @return the event source instance
     */
    public static RepositoryEventSource getInstance() {
        return instance;
    }
    
    /**
     * Adds the listener in order to receive a changed event from this source.
     * @param listener the changed listener to be added
     */
    public void addEventListener(RepositoryChangedListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Removes the listener which no longer receives a changed event from this source.
     * @param listener the changed listener to be removed
     */
    public void removeEventListener(RepositoryChangedListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Sends the changed event to all the listeners.
     * @param evt the changed event.
     */
    public void fire(RepositoryChangedEvent evt) {
        for (RepositoryChangedListener listener : listeners) {
            listener.notify(evt);
        }
    }
}
