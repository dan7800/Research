/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org. All rights reserved.
 *
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/server/event/SubscriptionMatcher.java,v 1.5 2003/11/22 01:10:49 farrukh_najmi Exp $
 *
 * ====================================================================
 */
package org.freebxml.omar.server.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.RegistryException;
import org.freebxml.omar.server.persistence.PersistenceManager;
import org.freebxml.omar.server.persistence.PersistenceManagerFactory;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType;
import org.oasis.ebxml.registry.bindings.query.ReturnType;
import org.oasis.ebxml.registry.bindings.query.SQLQueryType;
import org.oasis.ebxml.registry.bindings.rim.AdhocQueryType;
import org.oasis.ebxml.registry.bindings.rim.AuditableEventType;
import org.oasis.ebxml.registry.bindings.rim.SubscriptionType;


/**
 * Given an AuditableEvent find all Subscriptions whhose selectors potentially match the AuditableEvent. This avoids having to check every single subsription and is an important scalability design element.
 *
 * TODO: Reliable delivery and retries.
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class SubscriptionMatcher {
    
    PersistenceManager pm = PersistenceManagerFactory.getInstance().getPersistenceManager();
    ResponseOptionType responseOption = null;
    List objectRefs = new ArrayList();
    
    HashMap queryToObjectsMap = new HashMap();

    SubscriptionMatcher() throws RegistryException {
        try {
            responseOption = BindingUtility.getInstance().queryFac.createResponseOption();
            responseOption.setReturnType(ReturnType.LEAF_CLASS);
        }
        catch (javax.xml.bind.JAXBException e) {
            throw new RegistryException(e);
        }
    }
            
    /*
     * Gets the List of Subscriptions that definitely match the specified event.
     */
    HashMap getMatchedSubscriptionsMap(AuditableEventType ae) throws RegistryException {
        
        HashMap matchedSubscriptionsMap = new HashMap();
        
        queryToObjectsMap.clear();
       
        List matchedQuerys = getMatchedQuerys(ae);

        if (matchedQuerys.size() > 0) {
            StringBuffer ids = BindingUtility.getInstance().getIdListFromRegistryObjects(matchedQuerys);

            //Get all Subscriptions that use the matched querys as selectors        
            String query = "SELECT * FROM Subscription WHERE selector IN ( " + ids + " )";
            List objectRefs = new ArrayList();
            List matchedSubscriptions = pm.executeSQLQuery(query, responseOption, "Subscription", objectRefs);
            
            Iterator iter = matchedSubscriptions.iterator();
            while (iter.hasNext()) {
                SubscriptionType subscription = (SubscriptionType)iter.next();
                
                matchedSubscriptionsMap.put(subscription, queryToObjectsMap.get(subscription.getSelector()));
            }
        }
        
        return matchedSubscriptionsMap;
    }
    
    /**
     * Gets the List of AdhocQuery that match the specified event. 
     * Initialized the matchedQueryMap with queries matching the event
     * as keys and objects matching the query as values.
     *
     */
    private List getMatchedQuerys(AuditableEventType ae) throws RegistryException {
        List matchedQuerys = new ArrayList();
        
        List targetQuerys = getTargetQuerys(ae);
        List affectedObjects = ae.getAffectedObject().getObjectRef();
        
        Iterator iter = targetQuerys.iterator();
        while (iter.hasNext()) {
            AdhocQueryType query = (AdhocQueryType)iter.next();
            
            if (queryMatches(query, affectedObjects)) {
                matchedQuerys.add(query);                
            }
        }
        return matchedQuerys;
    }
    
    /*
     * Determines whether a specified target (potentially matching) query 
     * actually matches the list of affectedObjects or not.
     *
     */
    private boolean queryMatches(AdhocQueryType query, List affectedObjects) throws RegistryException {
        boolean match = false;        
        
        if (query instanceof SQLQueryType) {
            SQLQueryType sqlQuery = (SQLQueryType)query;
            String queryStr = sqlQuery.getQueryString();
            
            //Get objects that match the selector query (selectedObjects)
            List selectedObjects = pm.executeSQLQuery(queryStr, responseOption, "RegistryObject", objectRefs);
            
            //match is true if the affectedObjects  are a sub-set of the selectedObjects
            List selectedObjectIds = BindingUtility.getInstance().getIdsFromRegistryObjects(selectedObjects);
            List affectedObjectIds = BindingUtility.getInstance().getIdsFromRegistryObjects(affectedObjects);
            
            if (selectedObjectIds.size() > 0) {
                Iterator iter = affectedObjectIds.iterator();
                while (iter.hasNext()) {
                    if (selectedObjectIds.contains(iter.next())) {
                        match = true;
                        break;
                    }                    
                }
            }
            
            //Now remember which objects matched this query
            queryToObjectsMap.put(query.getId(), selectedObjects);
        }
        else {
            throw new RegistryException("Only SQLQuery supported at this time.");
        }
        
        return match;
    }
    
    
    /*
     * Gets the List of AdhocQueries that potentially match the specifid event.
     * This is an essential filtering mechanism to achieve scalability by narrowing
     * the number of Subscriptions to test for a match.
     * <p>
     * Gets all AdhocQuerys that have event type and primary partition 
     * matching this event.
     *
     * TODO: We need to get the List of objectType for affectedObjects somehow
     * to help the filtering. This is a spec issue at the moment because AuditableEvent
     * only contains ObjectRefs and not the actual objects.
     *
     * </p>
     */
    private List getTargetQuerys(AuditableEventType ae) throws RegistryException {   
        List querys= null;
        
        try {
            String eventType = ae.getEventType();

            ResponseOptionType responseOption = BindingUtility.getInstance().queryFac.createResponseOption();
            responseOption.setReturnType(ReturnType.LEAF_CLASS);
            List objectRefs = new ArrayList();

            //TODO: Filter down further based upon primary partitions in future to scale better.

            //Get those AdhocQuerys that match the events eventType or have no eventType specified
            String query = "SELECT * FROM AdhocQuery q, Subscription s WHERE q.id = s.selector AND ((q.query LIKE '%eventType%=%" + eventType + "%') OR (q.query NOT LIKE '%eventType%=%'))";
            querys = pm.executeSQLQuery(query, responseOption, "AdhocQuery", objectRefs);

        }
        catch (javax.xml.bind.JAXBException e) {
            throw new RegistryException(e);
        }
        
        return querys;
    }
    
}
