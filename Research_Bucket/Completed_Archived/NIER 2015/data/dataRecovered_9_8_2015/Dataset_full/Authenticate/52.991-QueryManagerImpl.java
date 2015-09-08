/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/server/query/QueryManagerImpl.java,v 1.23 2004/03/31 19:36:00 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.query;

import java.io.StringReader;
import java.io.StringWriter;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;

import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.OMARException;
import org.freebxml.omar.common.QueryManager;
import org.freebxml.omar.common.RegistryException;
import org.freebxml.omar.server.persistence.ObjectNotFoundException;
import org.freebxml.omar.server.persistence.PersistenceManager;
import org.freebxml.omar.server.persistence.PersistenceManagerFactory;
import org.freebxml.omar.server.repository.RepositoryManagerFactory;
import org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl;
import org.freebxml.omar.server.security.authorization.AuthorizationResult;
import org.freebxml.omar.server.security.authorization.AuthorizationServiceImpl;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequestType;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponseType;
import org.oasis.ebxml.registry.bindings.query.FilterQuery;
import org.oasis.ebxml.registry.bindings.query.FilterQueryType;
import org.oasis.ebxml.registry.bindings.query.GetContentResponseType;
import org.oasis.ebxml.registry.bindings.query.GetNotificationsRequestType;
import org.oasis.ebxml.registry.bindings.query.NotificationType;
import org.oasis.ebxml.registry.bindings.query.SQLQuery;
import org.oasis.ebxml.registry.bindings.query.SQLQueryType;
import org.oasis.ebxml.registry.bindings.rim.AdhocQueryType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.SlotListType;
import org.oasis.ebxml.registry.bindings.rim.SlotType1;
import org.oasis.ebxml.registry.bindings.rim.UserType;
import org.oasis.ebxml.registry.bindings.rim.Value;


/**
 * Implements the QueryManager interface for ebXML Registry as defined by ebRS spec.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class QueryManagerImpl implements QueryManager { //implements QueryManager {

    private static QueryManagerImpl instance = null;
    
    public static final String SLOT_QUERY_ID = "urn:oasis:names:tc:ebxml-regrep:3.0:rs:AdhocQueryRequest:queryId";
    public static final String SLOT_QUERY_PARAMS = "urn:oasis:names:tc:ebxml-regrep:3.0:rs:AdhocQueryRequest:queryParams";

    /**
     * @directed
     */
    private org.freebxml.omar.server.query.filter.FilterQueryProcessor filterQueryProcessor =
        org.freebxml.omar.server.query.filter.FilterQueryProcessor.getInstance();

    /**
     * @directed
     */
    private org.freebxml.omar.server.query.sql.SQLQueryProcessor sqlQueryProcessor =
        org.freebxml.omar.server.query.sql.SQLQueryProcessor.getInstance();
    
    /**
     * @directed
     */
    private PersistenceManager pm = PersistenceManagerFactory.getInstance().getPersistenceManager();
    

    protected QueryManagerImpl() {
    }

    public static QueryManagerImpl getInstance() {
        if (instance == null) {
            synchronized (QueryManagerImpl.class) {
                if (instance == null) {
                    instance = new QueryManagerImpl();
                }
            }
        }

        return instance;
    }

    /**
     * submitAdhocQuery
     */
    public AdhocQueryResponseType submitAdhocQuery(
        org.oasis.ebxml.registry.bindings.rim.UserType user,
        AdhocQueryRequestType req)
        throws RegistryException {
        org.oasis.ebxml.registry.bindings.query.AdhocQueryResponse ahqr = null;

        try {            
            ahqr = BindingUtility.getInstance().queryFac.createAdhocQueryResponse();
            
            //Process request for the case where it is a parameterized invocation of a stored query
            req = processForParameterizedQuery(req);            
            
            org.oasis.ebxml.registry.bindings.query.SQLQueryType sqlQuery = req.getSQLQuery();
            org.oasis.ebxml.registry.bindings.query.FilterQueryType filterQuery = req.getFilterQuery();
            org.oasis.ebxml.registry.bindings.query.ResponseOptionType responseOption =
                req.getResponseOption();

            if (sqlQuery != null) {
                String queryStr = replaceSpecialVariables(user, sqlQuery.getQueryString());
                org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType sqlResult =
                    sqlQueryProcessor.executeQuery(user,
                        queryStr, responseOption);
                ahqr.setSQLQueryResult(sqlResult);
                ahqr.setStatus(org.oasis.ebxml.registry.bindings.rs.Status.SUCCESS);
                
                            
            
                // Remove any objects from the ad-hoc query result set that the
                // user is not permitted to see.
                RegistryObjectListType rolt = ahqr.getSQLQueryResult();
                List identifiables = rolt.getIdentifiable();
                AuthorizationResult authRes = 
                    AuthorizationServiceImpl.getInstance().checkAuthorization(user, req, identifiables);
                Iterator identifiablesIter = identifiables.iterator();
                while (identifiablesIter.hasNext()) {
                    IdentifiableType identifiableObject = 
                        (IdentifiableType)identifiablesIter.next();
                    String id = identifiableObject.getId();
                    if (authRes.getDeniedResources().contains(id)) {
                        identifiablesIter.remove();
                    }
                }
                
                
            } else if (filterQuery != null) {
                org.oasis.ebxml.registry.bindings.query.FilterQueryResultType fqResult =
                    filterQueryProcessor.executeQuery(user, filterQuery,
                        responseOption);
                ahqr.setFilterQueryResult(fqResult);
                ahqr.setStatus(org.oasis.ebxml.registry.bindings.rs.Status.SUCCESS);
                
                /*
                // Remove any objects from the ad-hoc query result set that the
                // user is not permitted to see.
                RegistryObjectListType rolt = ahqr.getSQLQueryResult();
                List identifiables = rolt.getIdentifiable();
                AuthorizationResult authRes = 
                    AuthorizationServiceImpl.getInstance().checkAuthorization(user, req, identifiables);
                Iterator identifiablesIter = identifiables.iterator();
                while (identifiablesIter.hasNext()) {
                    IdentifiableType identifiableObject = 
                        (IdentifiableType)identifiablesIter.next();
                    String id = identifiableObject.getId();
                    if (authRes.getDeniedResources().contains(id)) {
                        identifiablesIter.remove();
                    }
                }
                 **/
                //TODO: Need to prune protected objects
                  
            } else {
                throw new RegistryException(
                    "Invalid request: no query specified");
            }
        } catch (RegistryException e) {
            e.printStackTrace();
            org.freebxml.omar.server.common.Utility.getInstance()
                                                   .updateRegistryResponseFromThrowable(ahqr,
                e, "QueryManagerImpl", "Unknown");
        } catch (javax.xml.bind.JAXBException e) {
            e.printStackTrace();
            org.freebxml.omar.server.common.Utility.getInstance()
                                                   .updateRegistryResponseFromThrowable(ahqr,
                e, "QueryManagerImpl", "Unknown");
        }

        ahqr.setRequestId(req.getId());
        return ahqr;
    }
    
    /**
     * Checks if supplied query is a parameterized query. If not return the same query.
     * If parametreized query then return a new query after fetching the specified parameterized
     * query from registry, replacing its positional parameters with suppliued parameters.
     */
    private AdhocQueryRequestType processForParameterizedQuery(AdhocQueryRequestType req) throws RegistryException {
        AdhocQueryRequestType newReq = req;
        
        SlotListType slotList = req.getRequestSlotList();
        
        if (slotList == null) {
            return newReq;
        }
        
        List slots = slotList.getSlot();
        
        String queryId = null;
        HashMap queryParams = new HashMap();
        Iterator iter = slots.iterator();
        while (iter.hasNext()) {
            SlotType1 slot = (SlotType1)iter.next();
            String slotName = slot.getName();
            if (slotName.equals(SLOT_QUERY_ID)) {
                Value value = (Value)(slot.getValueList().getValue()).get(0);
                queryId = value.getValue();
            } else if (slotName.startsWith("$")) {
                Value value = (Value)(slot.getValueList().getValue()).get(0);
                String paramValue = value.getValue();
                queryParams.put(slotName, paramValue);
            }            
        }
        
        //If queryId is not null then get the AdhocQuery from registry, plug the parameters
        //and set it as newReq
        if (queryId != null) {
            //TODO: Assumes SQLQuery. Needs to support FilterQuery
            AdhocQueryType adhocQuery = (SQLQuery)pm.getRegistryObject(queryId, "AdhocQuery");
                        
            try {
                adhocQuery = plugQueryParameters(adhocQuery, queryParams);
                
                if (adhocQuery instanceof SQLQueryType) {
                    newReq.setSQLQuery((SQLQueryType)adhocQuery);
                }
                else if (adhocQuery instanceof FilterQueryType) {
                    newReq.setFilterQuery((FilterQueryType)adhocQuery);
                }
            }
            catch (JAXBException e) {
                throw new RegistryException(e);
            }
        }
        
        return newReq;
    }
    
    /**
     * Replaces positional parameters in an SQLQuery
     */
    private AdhocQueryType plugQueryParameters(AdhocQueryType query, HashMap queryParams) throws JAXBException {        
        AdhocQueryType newQuery = query;
        
        //Get the queryString
        String queryStr = null;
        if (query instanceof FilterQuery) {
            FilterQuery fq = (FilterQuery)query;

            StringWriter sw = new StringWriter();

            javax.xml.bind.Marshaller marshaller = BindingUtility.getInstance().queryFac.createMarshaller();
            marshaller.marshal(fq, sw);
            queryStr = sw.toString();
        }
        else if (query instanceof SQLQuery) {
            SQLQuery sqlQuery = (SQLQuery)query;
            queryStr = sqlQuery.getQueryString();
        }

        //Now replace the query parameters in queryString
        Iterator iter = queryParams.keySet().iterator();

        while (iter.hasNext()) {
            Object key = iter.next();
            String value = queryParams.get(key).toString();
            //escape first char as it is a special char '$'
            queryStr = queryStr.replaceAll("\\"+key, value);
        }                
        
        //Now re-constitute as query
        if (query instanceof FilterQuery) {

            javax.xml.bind.Unmarshaller unmarshaller = BindingUtility.getInstance().queryFac.createUnmarshaller();

            StreamSource queryStrSS = new StreamSource(new StringReader(queryStr));                

            newQuery = (FilterQuery)unmarshaller.unmarshal(queryStrSS);
        }
        else if (query instanceof SQLQuery) {
            SQLQuery sqlQuery = (SQLQuery)newQuery;
            sqlQuery.setQueryString(queryStr);
        }
        
        
        return newQuery;
    }


    /**
     * Replaces special environment variables within specified query string.
     */
    private String replaceSpecialVariables(UserType user, String query) {
        String newQuery = query;

        //Replace $currentUser 
        if (user != null) {
            newQuery = newQuery.replaceAll("\\$currentUser", "'"+user.getId()+"'");
        }

        //Replace $currentTime       
        Timestamp currentTime = new Timestamp(Calendar.getInstance().getTimeInMillis());

        //??The timestamp is being truncated to work around a bug in PostgreSQL 7.2.2 JDBC driver
        String currentTimeStr = currentTime.toString().substring(0, 19);
        newQuery = newQuery.replaceAll("\\$currentTime", currentTimeStr);

        return newQuery;
    }
    

    /**
     * getContent
     */
    public GetContentResponseType getContent(UserType user, 
        org.oasis.ebxml.registry.bindings.query.GetContentRequestType req)
        throws RegistryException {
        org.oasis.ebxml.registry.bindings.query.GetContentResponse resp = null;

        try {
            resp = BindingUtility.getInstance().queryFac.createGetContentResponse();
            resp.setRequestId(req.getId());

            AuthorizationResult authRes = 
                AuthorizationServiceImpl.getInstance().checkAuthorization(user, req);
            authRes.throwExceptionOn(AuthorizationResult.PERMIT_NONE|AuthorizationResult.PERMIT_SOME);
            

            List orefs = req.getObjectRefList().getObjectRef();
            List ids = new java.util.ArrayList();

            for (int i = 0; i < orefs.size(); i++) {
                String id = ((ObjectRefType) orefs.get(i)).getId();
                ids.add(id);
            }

            org.freebxml.omar.server.repository.RepositoryManager repositoryMgr = RepositoryManagerFactory.getInstance()
                                                                                                          .getRepositoryManager();
            ids = repositoryMgr.itemsExist(ids);

            if (ids.size() > 0) {
                org.oasis.ebxml.registry.bindings.rs.RegistryErrorList el = BindingUtility.getInstance().rsFac.createRegistryErrorList();

                for (int i = 0; i < ids.size(); i++) {
                    org.oasis.ebxml.registry.bindings.rs.RegistryError re = BindingUtility.getInstance().rsFac.createRegistryError();
                    re.setValue("Repository item id=\"" + (String) ids.get(i) +
                        "\" does not exist!");
                    re.setErrorCode("unknown");
                    re.setCodeContext("QueryManagerImpl.getContent");
                    el.getRegistryError().add(re);
                }

                resp.setRegistryErrorList(el);
                resp.setStatus(org.oasis.ebxml.registry.bindings.rs.Status.FAILURE);
            } else {
                resp.setStatus(org.oasis.ebxml.registry.bindings.rs.Status.SUCCESS);
            }
        } catch (javax.xml.bind.JAXBException e) {
            throw new RegistryException(e);
        }

        return resp;
    }

    /**
     * Gets the RegistryObject referenced by specified ObjectRef.
     * First tries to get object locally and then tries to get it from remote registry.
     * Throws Registry
     * 
     */
    public org.oasis.ebxml.registry.bindings.rim.RegistryObjectType getRegistryObject(
        ObjectRefType ref) throws RegistryException {
        org.oasis.ebxml.registry.bindings.rim.RegistryObjectType ro = null;

        String id = ref.getId();
        ro = getRegistryObject(id);

        if (ro == null) {
            //Object not found locally. See if it is a remote object
            String home = ref.getHome();

            if (home == null) {
                throw new ObjectNotFoundException(id, "unknown type");
            }
            else {
                //Get the objects from remote registry
                throw new RegistryException("Remote object gets not implemented yet");
            }
        }

        return ro;
    }

    /**
     * Gets RegistryObject matching specified id. 
     * This method is added for the REST
     *
     */
    public org.oasis.ebxml.registry.bindings.rim.RegistryObjectType getRegistryObject(
        String id) throws RegistryException {
        org.oasis.ebxml.registry.bindings.rim.RegistryObjectType ro = null;

        try {
            String sqlQuery = "Select * from RegistryObject WHERE id = '" + id +
                "' ";
            org.oasis.ebxml.registry.bindings.query.ResponseOption responseOption =
                BindingUtility.getInstance().queryFac.createResponseOption();
            responseOption.setReturnComposedObjects(true);
            responseOption.setReturnType(org.oasis.ebxml.registry.bindings.query.ReturnType.LEAF_CLASS);

            List objectRefs = new java.util.ArrayList();
            List results = pm.executeSQLQuery(sqlQuery,
                    responseOption, "RegistryObject", objectRefs);

            if (results.size() == 1) {
                ro = (org.oasis.ebxml.registry.bindings.rim.RegistryObjectType) results.get(0);
            }
            else {
                //throw new RegistryException("Unresolved reference to object with id = " + id);
                System.err.println("Warning: Unresolved reference to object with id = " + id);               
            }
        } catch (javax.xml.bind.JAXBException e) {
            throw new RegistryException(e);
        }

        return ro;
    }
    
    /**
     * This method is added for the REST
     * It returns the RepositroyItem give a UUID
     */
    public org.freebxml.omar.common.RepositoryItem getRepositoryItem(
        String id) throws RegistryException {
        org.freebxml.omar.common.RepositoryItem ri = null;
        org.freebxml.omar.server.repository.RepositoryManager rm = RepositoryManagerFactory.getInstance()
                                                                                           .getRepositoryManager();

        String path = rm.getRepositoryItemPath(id);
        java.io.File itemFile = new java.io.File(path);

        if (itemFile.exists()) {
            javax.activation.FileDataSource fs = new javax.activation.FileDataSource(itemFile);
            javax.activation.DataHandler dh = new javax.activation.DataHandler(fs);
            String nullTemp = null;
            ri = new org.freebxml.omar.common.RepositoryItemImpl(id,
                    nullTemp, dh);
        } else {
            System.out.println("File NOT Exists!");
        }

        return ri;
    }
    
    
    /**
     * Gets the RegistryObjects referenced by specified RegistryObject.
     *
     * @param ro specifies the RegistryObject whose referenced objects are being sought.
     * @param depth specifies depth of fetch. -1 implies fetch all levels. 1 implies fetch immediate referenced objects. 
     */
    public Set getReferencedRegistryObjects(RegistryObjectType ro, int depth) throws RegistryException {
        HashSet referencedObjects = new HashSet();                
        
        try {
            HashMap idMap = new HashMap();
            Set immediateObjectRefs = BindingUtility.getInstance().getObjectRefsInRegistryObject(ro, idMap);

            --depth;

            //Get each immediately referenced RegistryObject
            Iterator iter = immediateObjectRefs.iterator();
            while (iter.hasNext()) {
                String ref = (String)iter.next();
                RegistryObjectType obj = getRegistryObject(ref);
                
                referencedObjects.add(obj);

                //If depth != 0 then recurse and get referenced objects for obj
                if (depth != 0) {
                    referencedObjects.addAll(getReferencedRegistryObjects(obj, depth));
                }
            }
        } catch (OMARException e) {
            throw new RegistryException(e);
        }
                        
        return referencedObjects;
    }
    
    public NotificationType getNotifications(UserType user, GetNotificationsRequestType partGetNotificationsRequest) throws 
         RegistryException {
     
         return null; //TODO
    }

    public UserType getUser(X509Certificate cert) throws RegistryException {
        return AuthenticationServiceImpl.getInstance().getUserFromCertificate(cert);
    }    
        
}
