/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changereplayer.event;

import java.util.EventObject;

/**
 * Manages a changed event indicating that the state of the view has changed.
 * @author Katsuhisa Maruyama
 */
public class ViewChangedEvent extends EventObject {
    
    private static final long serialVersionUID = 799662734740338051L;
    
    /**
     * Defines the type of view changed event.
     */
    public enum Type {
        FILE_SELECT, DEFAULT;
    }
    
    /**
     * The type of this event.
     */
    private ViewChangedEvent.Type type = ViewChangedEvent.Type.DEFAULT;
    
    /**
     * The object sent by this event.
     */
    private Object object = null;
    
    
    /**
     * Creates an instance containing information on a changed event.
     * @param source the instance on which the event initially occurred
     */
    public ViewChangedEvent(Object source) {
        super(source);
    }
    
    /**
     * Creates an instance containing information on a changed event.
     * @param source the instance on which the event initially occurred
     * @param object the object sent by this event
     */
    public ViewChangedEvent(Object source, Object object) {
        this(source);
        this.object = object;
    }
    
    /**
     * Creates an instance containing information on a changed event.
     * @param source the instance on which the event initially occurred
     * @param object the object sent by this event
     */
    public ViewChangedEvent(Object source, ViewChangedEvent.Type type, Object object) {
        this(source);
        this.type = type;
        this.object = object;
    }
    
    /**
     * Returns the type of this event.
     * @return the event type
     */
    public ViewChangedEvent.Type getType() {
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
