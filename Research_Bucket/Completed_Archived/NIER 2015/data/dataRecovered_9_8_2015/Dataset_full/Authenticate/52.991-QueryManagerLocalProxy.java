/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/common/QueryManagerLocalProxy.java,v 1.1 2004/03/24 17:21:50 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.common;

import java.security.cert.X509Certificate;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequestType;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponseType;
import org.oasis.ebxml.registry.bindings.query.GetContentRequestType;
import org.oasis.ebxml.registry.bindings.query.GetContentResponseType;
import org.oasis.ebxml.registry.bindings.query.GetNotificationsRequestType;
import org.oasis.ebxml.registry.bindings.query.NotificationType;
import org.oasis.ebxml.registry.bindings.query.ResponseOption;
import org.oasis.ebxml.registry.bindings.query.ReturnType;
import org.oasis.ebxml.registry.bindings.query.SQLQuery;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.UserType;


public class QueryManagerLocalProxy implements QueryManager {
    
    private QueryManager qm = QueryManagerFactory.getInstance().getQueryManager();
    private AuthenticationServiceImpl ac = AuthenticationServiceImpl.getInstance();
    private UserType callersUser = null;
    
    
    private String registryURL = null;
    private CredentialInfo credentialInfo = null;
    private SOAPMessenger msgr = null;
    
    public QueryManagerLocalProxy(String registryURL, CredentialInfo credentialInfo) {
        msgr = new SOAPMessenger(registryURL, credentialInfo);
    }
    
    public GetContentResponseType getContent(UserType user, GetContentRequestType partGetContentRequest) throws 
         RegistryException {
             
         return null; //TODO
    }
    
    public AdhocQueryResponseType submitAdhocQuery(UserType user, AdhocQueryRequestType partAdhocQueryRequest) throws 
         RegistryException {
                      
         return qm.submitAdhocQuery(getCallersUser(), partAdhocQueryRequest);
    }
    
    public NotificationType getNotifications(UserType user, GetNotificationsRequestType partGetNotificationsRequest) throws 
         RegistryException {
             
         return qm.getNotifications(getCallersUser(), partGetNotificationsRequest);
    }
    
    public java.util.Set getReferencedRegistryObjects(RegistryObjectType ro, int depth) throws RegistryException {
        return null; //TODO
    }
    
    public RegistryObjectType getRegistryObject(String id) throws RegistryException {
        RegistryObjectType ro = null;
        try {
            AdhocQueryRequestType req = BindingUtility.getInstance().queryFac.createAdhocQueryRequest();
            
            SQLQuery sqlQuery = BindingUtility.getInstance().queryFac.createSQLQuery();
            sqlQuery.setId(Utility.getInstance().createId());
            sqlQuery.setQueryString("SELECT * from RegistryObject WHERE id='" + id + "'");

            req.setSQLQuery(sqlQuery);

            ResponseOption respOption = BindingUtility.getInstance().queryFac.createResponseOption();
            respOption.setReturnComposedObjects(true);
            respOption.setReturnType(ReturnType.LEAF_CLASS_WITH_REPOSITORY_ITEM);
            req.setResponseOption(respOption);
            
            UserType user = null;
            AdhocQueryResponseType resp = submitAdhocQuery(user, req);
            RegistryResponseHolder respHolder = new RegistryResponseHolder(resp, null);
            List results = respHolder.getCollection();
            if (results.size() == 1) {
                ro = (org.oasis.ebxml.registry.bindings.rim.RegistryObjectType) results.get(0);
            }
        }
        catch (JAXBException e) {
            throw new RegistryException(e);
        }

        return ro;
    }
    
    public org.freebxml.omar.common.RepositoryItem getRepositoryItem(String id) throws RegistryException {
        return null; //TODO
    }
    
    private UserType getCallersUser() throws RegistryException {
        X509Certificate cert = null;
        if (credentialInfo != null) {
            cert = credentialInfo.cert;
        }
        return getUser(cert);
    }
            
    /**
     * Looks up the server side User object based upon specified public key certificate.
     */
    public UserType getUser(X509Certificate cert) throws RegistryException {
        
        callersUser = qm.getUser(cert);
        
        return callersUser;
    }
        
    
}
