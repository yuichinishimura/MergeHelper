/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changerepository.event;

import java.util.EventObject;

/**
 * Manages an event indicating that the state of the repository has been changed.
 * @author Katsuhisa Maruyama
 */
public class RepositoryChangedEvent extends EventObject {
    
    private static final long serialVersionUID = -4730035826183468628L;
    
    /**
     * Defines the type of repository changed event.
     */
    public enum Type {
        CLEAR, UPDATE, DEFAULT;
    }
    
    /**
     * The type of this event.
     */
    private RepositoryChangedEvent.Type type = RepositoryChangedEvent.Type.DEFAULT;
    
    /**
     * The object sent by this event.
     */
    private Object object = null;
    
    /**
     * Creates an instance containing information on a changed event.
     * @param source the instance on which the event initially occurred
     */
    public RepositoryChangedEvent(Object source) {
        super(source);
    }
    
    /**
     * Creates an instance containing information on a changed event.
     * @param source the instance on which the event initially occurred
     * @param type the type of this event
     */
    public RepositoryChangedEvent(Object source, RepositoryChangedEvent.Type type) {
        super(source);
        this.type = type;
    }
    
    /**
     * Creates an instance containing information on a changed event.
     * @param source the instance on which the event initially occurred
     * @param object the object sent by this event
     */
    public RepositoryChangedEvent(Object source, Object object) {
        this(source);
        this.object = object;
    }
    
    /**
     * Creates an instance containing information on a changed event.
     * @param source the instance on which the event initially occurred
     * @param type the type of this event
     * @param object the object sent by this event
     */
    public RepositoryChangedEvent(Object source, RepositoryChangedEvent.Type type, Object object) {
        this(source);
        this.type = type;
        this.object = object;
    }
    
    /**
     * Returns the type of this event.
     * @return the event type
     */
    public RepositoryChangedEvent.Type getType() {
        return type;
    }
    
    /**
     * Returns the object sent by this event.
     * @return the object
     */
    public Object getObject() {
        return object;
    }
}
