/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org. All rights reserved.
 *
 * $Header:
 * /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/persistence/rdb/NotifyActionDAO.java,v
 * 1.3 2003/11/03 01:10:09 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.event;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.RegistryException;
import org.oasis.ebxml.registry.bindings.rim.ActionType;
import org.oasis.ebxml.registry.bindings.rim.AuditableEventType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefListType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;
import org.oasis.ebxml.registry.bindings.rim.SubscriptionType;


/**
 * The top level manager that manges all aspects of event management in the registry. This includes listening for events, matching events to subscriptions and notifying subscribers when an event matching their Subscription occurs.
 *
 * @author Farrukh S. Najmi
 * @author Nikola Stojanovic
 */
public class EventManager implements AuditableEventListener {
    
    private org.apache.commons.logging.Log log = LogFactory.getLog(this.getClass());
    
    /** Creates a new instance of SubscriptionManager */
    protected EventManager() {
        try {
            subscriptionMatcher = new SubscriptionMatcher();
            notifier = new NotifierImpl();
        }
        catch (RegistryException e) {
            log.error(e);
        }
    }
    
    /*
     * Responds to an AuditableEvent. Called by the PersistenceManager.
     * Gets the subscriptions that match this event.
     * For each matching Subscription, sends notifications to subscribers
     * regarding this event.
     *
     * @see org.freebxml.omar.server.persistence.AuditableEventListener#onEvent(org.oasis.ebxml.registry.bindings.rim.AuditableEventType)
     */
    public void onEvent(AuditableEventType ae)  {
        
        try {
        
            javax.xml.bind.Marshaller marshaller = BindingUtility.getInstance().getJAXBContext().createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT,
                Boolean.TRUE);
            //marshaller.marshal(ae, System.err);
            
            //Get the HashMap where keys are the Subscriptions that match this event
            //and values are the matchedObjects for that Subscription.
            HashMap subscriptionsMap = subscriptionMatcher.getMatchedSubscriptionsMap(ae);

            //Process each matching Subscription
            Iterator subscriptionsIter = subscriptionsMap.keySet().iterator();
            while (subscriptionsIter.hasNext()) {
                SubscriptionType subscription = (SubscriptionType)subscriptionsIter.next();

                processSubscription(subscription, (List)(subscriptionsMap.get(subscription)));
            }
        }
        catch (Exception e) {
            log.error(e);
        }
    }
    
    private void processSubscription(SubscriptionType subscription, 
        List matchedObjects) throws RegistryException {

        notifier.sendNotifications(subscription, matchedObjects);
                    
    }
        
    /**
     * Main method used as unit test.
     */
    public static void main(String[] args) {
        try {
            EventManager em = EventManager.getInstance();
            AuditableEventType ae = BindingUtility.getInstance().rimFac.createAuditableEventType();
            ae.setEventType(BindingUtility.CANONICAL_EVENT_TYPE_Created);

            ObjectRefListType affectedObjects = BindingUtility.getInstance().rimFac.createObjectRefList();
            List refs = affectedObjects.getObjectRef();
            ObjectRefType ref = BindingUtility.getInstance().rimFac.createObjectRef();
            ref.setId("urn:uuid:4bcfc370-7a71-4c50-a6ed-ade41d836613");
            refs.add(ref);
            ae.setAffectedObject(affectedObjects);            

            em.onEvent(ae);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static EventManager getInstance(){
        if (instance == null) {
            synchronized(EventManager.class) {
                if (instance == null) {
                    instance = new EventManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * @link aggregationByValue
     */
    private SubscriptionMatcher subscriptionMatcher;
    
    /**
     * @link aggregationByValue
     */
    private NotifierImpl notifier;
    
    /**
     * @link
     * @shapeType PatternLink
     * @pattern Singleton
     * @supplierRole Singleton factory
     */
    /*# private EventManager _eventManager; */
    private static EventManager instance = null;
}
