/*
 * $Header: /cvsroot/sino/jaxr/src/com/sun/xml/registry/ebxml/QueryManagerImpl.java,v 1.16 2003/05/22 12:29:13 farrukh_najmi Exp $
 *
 *
 */

package com.sun.xml.registry.ebxml;

import com.sun.xml.registry.ebxml.infomodel.ConceptImpl;
import javax.xml.registry.*;
import java.util.*;
import javax.xml.registry.infomodel.*;


/**
 * Class Declaration for Class1
 *
 */
public abstract class QueryManagerImpl implements QueryManager {
    private HashMap schemeToValueMap;
    private HashMap valueToConceptMap;
    protected DeclarativeQueryManagerImpl dqm;
    protected RegistryServiceImpl regService;
    protected BusinessLifeCycleManagerImpl lcm;
    protected User callersUser = null;
    
    QueryManagerImpl(RegistryServiceImpl regService,
    BusinessLifeCycleManagerImpl lcm, DeclarativeQueryManagerImpl dqm) throws JAXRException {
        this.regService = regService;
        this.lcm = lcm;
        this.dqm = dqm;
        if (this.dqm == null) {
            this.dqm = (DeclarativeQueryManagerImpl)this;
        }
    }
    
    /**
     * Gets the RegistryObject specified by the Id and type of object.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param id is the id of the Key for a RegistryObject.
     * @param objectType is a constant definition from LifeCycleManager
     * that specifies the type of object desired.
     * @return RegistryObject Is the object is returned as their concrete
     * type (e.g. Organization, User etc.).
     */
    public RegistryObject getRegistryObject(String id, String objectType)
    throws JAXRException {
        String queryStr = "SELECT * FROM " + regService.mapJAXRNameToEbXMLName(objectType) + " WHERE id = '" + id + "'";
        Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);
        BulkResponse resp = dqm.executeQuery(query);
        
        return ((BulkResponseImpl)resp).getRegistryObject();
    }
    
    /**
     * Gets the RegistryObject specified by the Id.
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL>
     *
     * @return RegistryObject Is the object is returned as their concrete
     * type (e.g. Organization, User etc.).
     */
    public RegistryObject getRegistryObject(String id) throws JAXRException {
        return getRegistryObject(id, "RegistryObject");
    }
    
    
    /**
     * Gets the specified RegistryObjects.  The objects are returned as
     * their concrete type (e.g. Organization, User etc.).
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL>
     *
     *
     * @return BulkResponse containing a hetrogeneous Collection of
     * RegistryObjects (e.g. Organization, User etc.).
     */
    public BulkResponse getRegistryObjects(Collection objectKeys)
    throws JAXRException {
        return getRegistryObjects(objectKeys, "RegistryObject");
    }
    
    /**
     * Gets the RegistryObjects owned by the Caller.  The objects are returned as
     * their concrete type (e.g. Organization, User etc.).
     * For to JAXR 2.0??
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL>
     *
     *
     * @return BulkResponse containing a hetrogeneous Collection of
     * RegistryObjects (e.g. Organization, User etc.).
     */
    public BulkResponse getCallersRegistryObjects() throws JAXRException {
        BulkResponse resp = null;
        
        User user = getCallersUser();
        
        String queryStr = "SELECT * FROM " + regService.mapJAXRNameToEbXMLName("RegistryObject") + " ro, " +
            regService.mapJAXRNameToEbXMLName("User") + " u, " +              
            regService.mapJAXRNameToEbXMLName("AuditableEvent") + " ae " +             
            " WHERE  ae.user_ = '" + user.getKey().getId() + "' AND ae.registryObject = ro.id" ;
        Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);
        resp = dqm.executeQuery(query);

        return resp;
    }
    
    
    /**
     * Gets the specified RegistryObjects of the specified object type.  The objects are returned as
     * their concrete type (e.g. Organization, User etc.).
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     *
     * @return BulkResponse containing a hetrogeneous Collection of
     * RegistryObjects (e.g. Organization, User etc.).
     */
    public BulkResponse getRegistryObjects(Collection objectKeys, String objectType)
    throws JAXRException {
        StringBuffer queryStr = new StringBuffer("SELECT * FROM ");
        queryStr.append(regService.mapJAXRNameToEbXMLName(objectType));
        queryStr.append(" WHERE id in (");
        Iterator iter = objectKeys.iterator();
        while (iter.hasNext()) {
            String id = ((Key)iter.next()).getId();
            queryStr.append("'").append(id).append("'");
            if (iter.hasNext()) {
                queryStr.append(", ");
            }
        }
        queryStr.append(")");
        
        Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr.toString());
        BulkResponse resp = dqm.executeQuery(query);
        
        return resp;
    }
    
    /**
     * Gets the RegistryObjects owned by the caller.  The objects are
     * returned as their concrete type (e.g. Organization, User etc.).
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @return BulkResponse containing a hetrogeneous Collection of
     * RegistryObjects (e.g. Organization, User etc.).
     */
    public javax.xml.registry.BulkResponse getRegistryObjects() throws javax.xml.registry.JAXRException {
        // Write your code here
        return null;
    }
    
    /**
     * Gets the RegistryObjects owned by the caller, that are of the
     * specified type.  The objects are returned as their concrete type
     * (e.g. Organization, User etc.).
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param objectType Is a constant that defines the type of object
     * sought. See LifeCycleManager for constants for object types.
     * @see LifeCycleManager for supported objectTypes
     * @return BulkResponse containing a hetrogeneous Collection of
     * RegistryObjects (e.g. Organization, User etc.).
     */
    public BulkResponse getRegistryObjects(String objectType)
    throws JAXRException {
        // Write your code here
        return null;
    }
    
    
    /*
     * Gets the specified pre-defined concept as defined in Appendix A
     * of the JAXR specification.
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL>
     *
     * @return The pre-defined Concept
     *
     * Implementation internal
     *
    public Concept getPredefinedConcept(String schemeName, String value)
        throws JAXRException
    {
        if (schemeToValueMap == null) {
            schemeToValueMap = new HashMap();
        }
        HashMap valueToConceptMap = (HashMap)schemeToValueMap.get(schemeName);
        if (valueToConceptMap == null) {
            valueToConceptMap = new HashMap();
            schemeToValueMap.put(schemeName, valueToConceptMap);
        }
        ConceptImpl concept = (ConceptImpl)valueToConceptMap.get(value);
        if (concept == null) {
            // Existing ConceptImpl not found so create a new one
            concept = new ConceptImpl(lcm);
            concept.setValue(value);
            // XXX set other Concept parts, like path
            valueToConceptMap.put(value, concept);
        }
        return concept;
    }
     **/


    public javax.xml.registry.RegistryService getRegistryService() throws javax.xml.registry.JAXRException {
        return regService;
    }
    
    //Add as Level 1 call in JAXR 2.0??
    public User getCallersUser() throws JAXRException {
        if (callersUser == null) { 
                
            //submit an object and read it back then get its owner

            RegistryPackage pkg = lcm.createRegistryPackage("Temporary object of no value");

            String id = pkg.getKey().getId();
            ArrayList objs = new ArrayList();
            objs.add(pkg);
            lcm.saveObjects(objs);

            try {
                String queryStr = "SELECT * FROM " + regService.mapJAXRNameToEbXMLName("User") + " u, " + 
                    regService.mapJAXRNameToEbXMLName("RegistryObject") + " ro, " + 
                    regService.mapJAXRNameToEbXMLName("AuditableEvent") + " ae " +             
                    " WHERE ro.id = '" + id + "' AND ae.registryObject = ro.id AND ae.user_ = u.id";
                Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);
                BulkResponse resp = dqm.executeQuery(query);

                callersUser = (User)(((BulkResponseImpl)resp).getRegistryObject());
            }
            catch (JAXRException e) {
                throw e;
            }
            finally {
                //Now remove the object that was created
                ArrayList keys = new ArrayList();
                keys.add(pkg.getKey());
                lcm.deleteObjects(keys);
            }

            if (callersUser == null) {
                //Caller is no registered so create new User so it can register
                callersUser = lcm.createUser();
            }
        }                

        return callersUser;
    }
    
}
/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/client/xml/registry/QueryManagerImpl.java,v 1.8 2004/03/31 19:30:40 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.JAXRException;
import javax.xml.registry.Query;
import javax.xml.registry.QueryManager;
import javax.xml.registry.infomodel.Key;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.User;

import org.freebxml.omar.client.xml.registry.util.ProviderProperties;
import org.freebxml.omar.common.CredentialInfo;
import org.freebxml.omar.common.QueryManagerLocalProxy;
import org.freebxml.omar.common.QueryManagerSOAPProxy;

/**
 * Class Declaration for Class1
 *
 */
public abstract class QueryManagerImpl implements QueryManager {
    private HashMap schemeToValueMap;
    private HashMap valueToConceptMap;
    protected DeclarativeQueryManagerImpl dqm;
    protected RegistryServiceImpl regService;
    protected BusinessLifeCycleManagerImpl lcm;
    protected User callersUser = null;
    protected org.freebxml.omar.common.QueryManager serverQMProxy = null;
    
    public static final String SLOT_QUERY_ID = "urn:oasis:names:tc:ebxml-regrep:3.0:rs:AdhocQueryRequest:queryId";
    

    QueryManagerImpl(RegistryServiceImpl regService,
        BusinessLifeCycleManagerImpl lcm, DeclarativeQueryManagerImpl dqm)
        throws JAXRException {
        this.regService = regService;
        this.lcm = lcm;
        this.dqm = dqm;
        
        try {
            setCredentialInfo(((ConnectionImpl)regService.getConnection()).getCredentialInfo());
        }
        catch (JAXRException e) {
            throw new UndeclaredThrowableException(e);
        }
        
        if (this.dqm == null) {
            this.dqm = (DeclarativeQueryManagerImpl) this;
        }
    }

    /**
     * Gets the RegistryObject specified by the Id and type of object.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param id is the id of the Key for a RegistryObject.
     * @param objectType is a constant definition from LifeCycleManager
     * that specifies the type of object desired.
     * @return RegistryObject Is the object is returned as their concrete
     * type (e.g. Organization, User etc.).
     */
    public RegistryObject getRegistryObject(String id, String objectType)
        throws JAXRException {
        String queryStr = "SELECT * FROM " +
            regService.mapJAXRNameToEbXMLName(objectType) + " WHERE id = '" +
            id + "'";
        Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);
        BulkResponse resp = dqm.executeQuery(query);

        return ((BulkResponseImpl) resp).getRegistryObject();
    }

    /**
     * Gets the RegistryObject specified by the Id.
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL>
     *
     * @return RegistryObject Is the object is returned as their concrete
     * type (e.g. Organization, User etc.).
     */
    public RegistryObject getRegistryObject(String id)
        throws JAXRException {
        return getRegistryObject(id, "RegistryObject");
    }

    /**
     * Gets the specified RegistryObjects.  The objects are returned as
     * their concrete type (e.g. Organization, User etc.).
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL>
     *
     *
     * @return BulkResponse containing a hetrogeneous Collection of
     * RegistryObjects (e.g. Organization, User etc.).
     */
    public BulkResponse getRegistryObjects(Collection objectKeys)
        throws JAXRException {
        return getRegistryObjects(objectKeys, "RegistryObject");
    }

    /**
     * Gets the RegistryObjects owned by the Caller.  The objects are returned as
     * their concrete type (e.g. Organization, User etc.).
     * For to JAXR 2.0??
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL>
     *
     *
     * @return BulkResponse containing a hetrogeneous Collection of
     * RegistryObjects (e.g. Organization, User etc.).
     */
    public BulkResponse getCallersRegistryObjects() throws JAXRException {
        BulkResponse resp = null;

        User user = getCallersUser();


        //SELECT *  FROM RegistryObject ro, AuditableEvent ae, AffectedObject ao, User_ u WHERE ae.user_ =
        //'urn:uuid:c8e59c45-2288-4c96-adb6-e89395272cb4'  AND ao.id = ro.id AND ao.eventId = ae.id        
        
        String queryStr = "SELECT * FROM " +
            regService.mapJAXRNameToEbXMLName("RegistryObject") + " ro, " +
            regService.mapJAXRNameToEbXMLName("AuditableEvent") + " ae " +
            regService.mapJAXRNameToEbXMLName("AffectedObject") + " ao, " +
            regService.mapJAXRNameToEbXMLName("User") + " u, " +
            " WHERE  ae.user_ = '" + user.getKey().getId() +
            "' AND ao.id = ro.id AND ao.eventId = ae.id";
        Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);
        resp = dqm.executeQuery(query);

        return resp;
    }

    /**
     * Gets the specified RegistryObjects of the specified object type.  The objects are returned as
     * their concrete type (e.g. Organization, User etc.).
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     *
     * @return BulkResponse containing a hetrogeneous Collection of
     * RegistryObjects (e.g. Organization, User etc.).
     */
    public BulkResponse getRegistryObjects(Collection objectKeys,
        String objectType) throws JAXRException {
        StringBuffer queryStr = new StringBuffer("SELECT * FROM ");
        queryStr.append(regService.mapJAXRNameToEbXMLName(objectType));
        queryStr.append(" WHERE id in (");

        Iterator iter = objectKeys.iterator();

        while (iter.hasNext()) {
            String id = ((Key) iter.next()).getId();
            queryStr.append("'").append(id).append("'");

            if (iter.hasNext()) {
                queryStr.append(", ");
            }
        }

        queryStr.append(")");

        Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr.toString());
        BulkResponse resp = dqm.executeQuery(query);

        return resp;
    }

    /**
     * Gets the RegistryObjects owned by the caller.  The objects are
     * returned as their concrete type (e.g. Organization, User etc.).
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @return BulkResponse containing a hetrogeneous Collection of
     * RegistryObjects (e.g. Organization, User etc.).
     */
    public javax.xml.registry.BulkResponse getRegistryObjects()
        throws javax.xml.registry.JAXRException {
        // Write your code here
        return null;
    }

    /**
     * Gets the RegistryObjects owned by the caller, that are of the
     * specified type.  The objects are returned as their concrete type
     * (e.g. Organization, User etc.).
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param objectType Is a constant that defines the type of object
     * sought. See LifeCycleManager for constants for object types.
     * @see LifeCycleManager for supported objectTypes
     * @return BulkResponse containing a hetrogeneous Collection of
     * RegistryObjects (e.g. Organization, User etc.).
     */
    public BulkResponse getRegistryObjects(String objectType)
        throws JAXRException {
        // Write your code here
        return null;
    }

    /*
     * Gets the specified pre-defined concept as defined in Appendix A
     * of the JAXR specification.
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL>
     *
     * @return The pre-defined Concept
     *
     * Implementation internal
     *
    public Concept getPredefinedConcept(String schemeName, String value)
        throws JAXRException
    {
        if (schemeToValueMap == null) {
            schemeToValueMap = new HashMap();
        }
        HashMap valueToConceptMap = (HashMap)schemeToValueMap.get(schemeName);
        if (valueToConceptMap == null) {
            valueToConceptMap = new HashMap();
            schemeToValueMap.put(schemeName, valueToConceptMap);
        }
        ConceptImpl concept = (ConceptImpl)valueToConceptMap.get(value);
        if (concept == null) {
            // Existing ConceptImpl not found so create a new one
            concept = new ConceptImpl(lcm);
            concept.setValue(value);
            // XXX set other Concept parts, like path
            valueToConceptMap.put(value, concept);
        }
        return concept;
    }
     **/
    public javax.xml.registry.RegistryService getRegistryService()
        throws javax.xml.registry.JAXRException {
        return regService;
    }

    //Add as Level 1 call in JAXR 2.0??
    public User getCallersUser() throws JAXRException {
        if (callersUser == null) {
            HashMap paramsMap = new HashMap();
            paramsMap.put(SLOT_QUERY_ID, "urn:uuid:41407b86-6059-4b6c-b1cc-accf19e9703c");
            Query getCallersUserQuery = dqm.createQuery(Query.QUERY_TYPE_SQL, paramsMap);
            BulkResponse br = dqm.executeQuery(getCallersUserQuery);
            
            callersUser = (User)((BulkResponseImpl)br).getRegistryObject();
        }

        return callersUser;
    }
    
    //TODO: Add to JAXR 2.0 API??
    public org.freebxml.omar.common.RepositoryItem getRepositoryItem(String id) throws JAXRException {
        // Execute a getContentRequest to obtain RepositoryItem from server
        /**
        String gcr = null;

        try {
            gcr = BindingUtility.getInstance().toGetContentRequest(id);
            User user = null;
            GetContentResponseType resp = serverQMProxy.getContent(user, gcr);
        } catch (OMARException ex) {
            throw new JAXRException(ex.getMessage());
        }
        */
        return null; //TODO
    }
    
    void setCredentialInfo(CredentialInfo credentialInfo) {
        boolean localCall = Boolean.valueOf(ProviderProperties.getInstance().getProperty("org.freebxml.omar.client.xml.registry.localCall", "false")).booleanValue();
        if (localCall) {
            serverQMProxy = new QueryManagerLocalProxy(
                ((ConnectionImpl)regService.getConnection()).getQueryManagerURL(), 
                credentialInfo);
        }
        else {
            serverQMProxy = new QueryManagerSOAPProxy(
                ((ConnectionImpl)regService.getConnection()).getQueryManagerURL(), 
                credentialInfo);
        }        
    }
}
