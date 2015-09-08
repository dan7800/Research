/*
 * DAOContext.java
 *
 * Created on November 5, 2003, 3:01 PM
 */

package org.freebxml.omar.server.persistence.rdb;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.RegistryException;
import org.freebxml.omar.server.event.EventManager;
import org.freebxml.omar.server.event.EventManagerFactory;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType;
import org.oasis.ebxml.registry.bindings.query.ReturnType;
import org.oasis.ebxml.registry.bindings.rim.AuditableEventType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefListType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;
import org.oasis.ebxml.registry.bindings.rim.UserType;

/**
 * Keeps track of various contextual information for an operation
 * within the PersistenceManager.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class DAOContext {
    
    private UserType user=null;
    private Connection connection=null;
    private AuditableEventType createEvent;
    private AuditableEventType updateEvent;
    private AuditableEventType deleteEvent;
    private ResponseOptionType responseOption;
    private ArrayList objectRefs;
    
    /** Creates a new instance of DAOContext */
    DAOContext(UserType user, Connection connection)  {
        this.user = user;
        this.connection = connection;
        BindingUtility bu = BindingUtility.getInstance();
        try {
            objectRefs = new ArrayList();
            
            responseOption = bu.queryFac.createResponseOption();
            responseOption.setReturnType(ReturnType.LEAF_CLASS);
            responseOption.setReturnComposedObjects(true);
                        
            createEvent = bu.rimFac.createAuditableEvent();
            createEvent.setEventType(BindingUtility.CANONICAL_EVENT_TYPE_Created);
            createEvent.setId(org.freebxml.omar.common.Utility.getInstance().createId());
            createEvent.setRequestId("//TODO");
            if (user != null) {
                createEvent.setUser(user.getId());
            }
            ObjectRefListType createRefList = bu.rimFac.createObjectRefListType();
            createEvent.setAffectedObject(createRefList);
            
            updateEvent = bu.rimFac.createAuditableEvent();
            updateEvent.setEventType(BindingUtility.CANONICAL_EVENT_TYPE_Updated);
            updateEvent.setId(org.freebxml.omar.common.Utility.getInstance().createId());
            updateEvent.setRequestId("//TODO");
            if (user != null) {
                updateEvent.setUser(user.getId());
            }
            ObjectRefListType updateRefList = bu.rimFac.createObjectRefListType();
            updateEvent.setAffectedObject(updateRefList);
            
            deleteEvent = bu.rimFac.createAuditableEvent();
            deleteEvent.setEventType(BindingUtility.CANONICAL_EVENT_TYPE_Deleted);
            deleteEvent.setId(org.freebxml.omar.common.Utility.getInstance().createId());
            deleteEvent.setRequestId("//TODO");
            if (user != null) {
                deleteEvent.setUser(user.getId());
            }
            ObjectRefListType deleteRefList = bu.rimFac.createObjectRefListType();
            deleteEvent.setAffectedObject(deleteRefList);
        }
        catch (JAXBException e) {
            //Should never happen.
            e.printStackTrace();
        }
    }
    
    /*
     * Should not be used.
     */
    private DAOContext() {
    }
    
    ResponseOptionType getResponseOption() {
        return responseOption;
    }
    
    void setResponseOption(ResponseOptionType responseOption) {
        this.responseOption = responseOption;
    }
    
    List getObjectRefs() {
        return objectRefs;
    }
    
    AuditableEventType getCreateEvent() {
        return createEvent;
    }
    
    AuditableEventType getUpdateEvent() {
        return updateEvent;
    }
    
    AuditableEventType getDeleteEvent() {
        return deleteEvent;
    }
    
    UserType getUser() {
        return user;
    }
    
    Connection getConnection() {
        return connection;
    }
    
    /*
     * Called to commit the transaction
     * Saves auditable events for this transaction prior to commit.
     * Notifies EventManager after commit.
     */
    void commit() throws SQLException, RegistryException {
        //Save auditable events prior to commit
        saveAuditableEvents();
        
        connection.commit();
        
        sendEventsToEventManager();
    }
    
    private void saveAuditableEvents() throws RegistryException {
        ArrayList events = new ArrayList();
        Calendar timeNow = Calendar.getInstance();        
        
        if (eventOccured(createEvent)) {
            createEvent.setTimestamp(timeNow);
            removeDuplicateAffectedObjects(createEvent);
            events.add(createEvent);
        }
        
        //Delete during update should be ignored as they are an impl artifact
        if (eventOccured(updateEvent)) {
            updateEvent.setTimestamp(timeNow);
            removeDuplicateAffectedObjects(updateEvent);
            events.add(updateEvent);
        }         
        else if (eventOccured(deleteEvent)) {
            deleteEvent.setTimestamp(timeNow);
            removeDuplicateAffectedObjects(deleteEvent);
            events.add(deleteEvent);
        }
        
        if (events.size() > 0) {
            AuditableEventDAO aeDAO = new AuditableEventDAO(this);
            createEvent.setTimestamp(timeNow);
            aeDAO.insert(events);
        }
    }
    
    /**
     * Delete of composed objects such as ClassificationNodes within Schemes
     * can result in duplicate ObjectRefs being deleted.
     */
    private void removeDuplicateAffectedObjects(AuditableEventType ae) {
        HashSet ids = new HashSet();
        HashSet duplicateObjectRefs = new HashSet();
        
        //Determine duplicate ObjectRefs
        Iterator iter = ae.getAffectedObject().getObjectRef().iterator();
        while (iter.hasNext()) {
            ObjectRefType oref = (ObjectRefType)iter.next();
            String id = oref.getId();
           if (ids.contains(id)) {
               duplicateObjectRefs.add(oref);
           } else {
               ids.add(id);
           }            
        }
        
        //Now remove duplicate ObjectRefs
        iter = duplicateObjectRefs.iterator();
        while (iter.hasNext()) {
            ae.getAffectedObject().getObjectRef().remove(iter.next());            
        }
        
    }
    
    private boolean eventOccured(AuditableEventType ae) {
        boolean occured = false;
        
        if ((ae.getAffectedObject() != null) &&
        (ae.getAffectedObject().getObjectRef() != null) &&
        (ae.getAffectedObject().getObjectRef().size() > 0)) {
            
            occured = true;
        }
        
        return occured;
        
    }
    
    private void sendEventsToEventManager() {
        EventManager eventManager = EventManagerFactory.getInstance().getEventManager();
        
        if (eventOccured(createEvent)) {
            eventManager.onEvent(createEvent);
        }        
        
        //Delete during update should be ignored as they are an impl artifact
        if (eventOccured(updateEvent)) {
            eventManager.onEvent(updateEvent);
        }
        else if (eventOccured(deleteEvent)) {
            eventManager.onEvent(deleteEvent);
        }
    }
}
