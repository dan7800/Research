/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/client/xml/registry/infomodel/AuditableEventImpl.java,v 1.11 2003/11/20 15:55:38 farrukh_najmi Exp $
 * ====================================================================
 */
/*
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/client/xml/registry/infomodel/AuditableEventImpl.java,v 1.11 2003/11/20 15:55:38 farrukh_najmi Exp $
 *
 */
package org.freebxml.omar.client.xml.registry.infomodel;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.AuditableEvent;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.User;

import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.oasis.ebxml.registry.bindings.rim.AuditableEventType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;


/**import org.oasis.ebxml.registry.bindings.rim.types.EventTypeType; */
/**
 * Class Declaration for Class1
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class AuditableEventImpl extends RegistryObjectImpl
    implements AuditableEvent {
    private RegistryObjectRef userRef = null;
    private Timestamp timestamp = null;
    private int eventType = AuditableEvent.EVENT_TYPE_CREATED;
    
    //List of RegistryObjectRefs
    private List affectedObjects = new ArrayList();

    public AuditableEventImpl(LifeCycleManagerImpl lcm)
        throws JAXRException {
        super(lcm);
    }

    public AuditableEventImpl(LifeCycleManagerImpl lcm, AuditableEventType ebAE)
        throws JAXRException {
        super(lcm, ebAE);

        // Set the eventType
        String ebEventType = ebAE.getEventType();

        if (ebEventType.equals("Created")) {
            eventType = AuditableEvent.EVENT_TYPE_CREATED;
        } else if (ebEventType.equals("Deleted")) {
            eventType = AuditableEvent.EVENT_TYPE_DELETED;
        } else if (ebEventType.equals("Deprecated")) {
            eventType = AuditableEvent.EVENT_TYPE_DEPRECATED;
        } else if (ebEventType.equals("Updated")) {
            eventType = AuditableEvent.EVENT_TYPE_UPDATED;
        } else if (ebEventType.equals("Versioned")) {
            eventType = AuditableEvent.EVENT_TYPE_VERSIONED;
        }

        List _affectedObjects = ebAE.getAffectedObject().getObjectRef();
        Iterator iter = _affectedObjects.iterator();
        while (iter.hasNext()) {
            ObjectRefType ref = (ObjectRefType)iter.next();            
            RegistryObjectRef registryObjectRef = new RegistryObjectRef(lcm, ref);
            affectedObjects.add(registryObjectRef);
        }
        timestamp = new Timestamp(ebAE.getTimestamp().getTimeInMillis());
        userRef = new RegistryObjectRef(lcm, ebAE.getUser());
    }

    //Possible addition to JAXR 2.0??
    public RegistryObjectRef getUserRef() throws JAXRException {
        return userRef;
    }

    public User getUser() throws JAXRException {
        User user = null;

        if (userRef != null) {
            user = (User) userRef.getRegistryObject("User");
        }

        return user;
    }

    public Timestamp getTimestamp() throws JAXRException {
        return timestamp;
    }

    public int getEventType() throws JAXRException {
        return eventType;
    }

    /**
     * @deprecated: Use getAffectedObjects instead.
     */
    public RegistryObject getRegistryObject() throws JAXRException {
        throw new JAXRException("This method has been deprectaed. Use getAffectedObjects instead.");
    }

    /**
     * Add to JAXR 2.0
     *
     * @return the List of objectReferences for objects affected by this event
     */
    public List getAffectedObjects() throws JAXRException {

        return affectedObjects;
    }
    
    /**
     * This method takes this JAXR infomodel object and returns an
     * equivalent binding object for it.  Note it does the reverse of one
     * of the constructors above.
     */
    public Object toBindingObject() throws JAXRException {
        org.freebxml.omar.common.BindingUtility bu = org.freebxml.omar.common.BindingUtility.getInstance();

        try {
            org.oasis.ebxml.registry.bindings.rim.AuditableEvent ebOrg = bu.rimFac.createAuditableEvent();
            setBindingObject(ebOrg);

            return ebOrg;
        } catch (JAXBException e) {
            throw new JAXRException(e);
        }
    }

    protected void setBindingObject(AuditableEventType ebOrg)
        throws JAXRException {
        super.setBindingObject(ebOrg);
    }
}
/*
 * $Header: /cvsroot/sino/jaxr/src/com/sun/xml/registry/ebxml/infomodel/AuditableEventImpl.java,v 1.13 2003/08/13 04:03:28 farrukh_najmi Exp $
 *
 */

package com.sun.xml.registry.ebxml.infomodel;

import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;
import java.sql.Timestamp;
import java.io.*;

import com.sun.xml.registry.ebxml.LifeCycleManagerImpl;
import org.oasis.ebxml.registry.bindings.rim.AuditableEventType;
import org.oasis.ebxml.registry.bindings.rim.types.EventTypeType;

/**
 * Class Declaration for Class1
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class AuditableEventImpl extends RegistryObjectImpl
    implements AuditableEvent
{
    private RegistryObjectRef userRef = null;
    private Timestamp timestamp = null;
    private int eventType = AuditableEvent.EVENT_TYPE_CREATED;
    private RegistryObjectRef registryObjectRef = null;

    /**
     * ??eeg Looks like JAXR allows this to be created using
     * LCM.createObject() and server allows such an object to be saved, but
     * I'm not sure why anyone would want to do that.
     */
    public AuditableEventImpl(LifeCycleManagerImpl lcm)
        throws JAXRException
    {
        super(lcm);
    }

    public AuditableEventImpl(LifeCycleManagerImpl lcm, AuditableEventType ebAE)
        throws JAXRException
    {
        super(lcm, ebAE);

        // Set the eventType
        EventTypeType ebEventType = ebAE.getEventType();
        if (ebEventType == EventTypeType.CREATED) {
            eventType = AuditableEvent.EVENT_TYPE_CREATED;
        } else if (ebEventType == EventTypeType.DELETED) {
            eventType = AuditableEvent.EVENT_TYPE_DELETED;
        } else if (ebEventType == EventTypeType.DEPRECATED) {
            eventType = AuditableEvent.EVENT_TYPE_DEPRECATED;
        } else if (ebEventType == EventTypeType.UPDATED) {
            eventType = AuditableEvent.EVENT_TYPE_UPDATED;
        } else if (ebEventType == EventTypeType.VERSIONED) {
            eventType = AuditableEvent.EVENT_TYPE_VERSIONED;
        }

        registryObjectRef = new RegistryObjectRef(
            lcm, ebAE.getRegistryObject());
        timestamp = new Timestamp((ebAE.getTimestamp()).getTime());
        userRef = new RegistryObjectRef(lcm, ebAE.getUser());
    }

    public String toXML() throws JAXRException {
        org.oasis.ebxml.registry.bindings.rim.RegistryObjectType ebObj = toBindingObject();
        StringWriter sw = new StringWriter();
        try {
            ebObj.marshal(sw);
        } catch (org.exolab.castor.xml.MarshalException x) {
            throw new JAXRException(x);
        } catch (org.exolab.castor.xml.ValidationException x) {
            throw new JAXRException(x);
        }
        return sw.toString();
    }

    //Possible addition to JAXR 2.0??
    public RegistryObjectRef getUserRef() throws JAXRException {
        return userRef;
    }
    
    public User getUser() throws JAXRException {
        User user = null;
        if (userRef != null) {
            user = (User)userRef.getRegistryObject("User");
        }

        return user;
    }

    public Timestamp getTimestamp() throws JAXRException {
        return timestamp;
    }

    public int getEventType() throws JAXRException {
        return eventType;
    }

    public RegistryObject getRegistryObject() throws JAXRException {
        RegistryObject ro = null;
        if (registryObjectRef != null) {
            ro = registryObjectRef.getRegistryObject("RegistryObject");
        }

        return ro;
    }

    /**
     * This method takes this JAXR infomodel object and returns an
     * equivalent binding object for it.  Note it does the reverse of one
     * of the constructors above.
     */
    public org.oasis.ebxml.registry.bindings.rim.AuditableEvent toBindingObject()  throws JAXRException
    {
        org.oasis.ebxml.registry.bindings.rim.AuditableEvent ebOrg =
            new org.oasis.ebxml.registry.bindings.rim.AuditableEvent();

        setBindingObject(ebOrg);

        return ebOrg;
    }

    protected void setBindingObject(AuditableEventType ebOrg)
        throws JAXRException
    {
        super.setBindingObject(ebOrg);
    }
}
