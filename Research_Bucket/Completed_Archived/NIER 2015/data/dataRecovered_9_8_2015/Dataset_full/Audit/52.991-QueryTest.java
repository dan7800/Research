/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2002 freebxml.org. All rights reserved.
 *
 * ====================================================================
 */
package org.freebxml.omar.server.query;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.OMARException;
import org.freebxml.omar.common.QueryManager;
import org.freebxml.omar.common.QueryManagerFactory;
import org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponseType;
import org.oasis.ebxml.registry.bindings.query.ResponseOption;
import org.oasis.ebxml.registry.bindings.query.ReturnType;
import org.oasis.ebxml.registry.bindings.query.SQLQuery;
import org.oasis.ebxml.registry.bindings.rim.UserType;


/**
 * @author Farrukh Najmi
 */
public class QueryTest extends TestCase {
    
    BindingUtility bu = BindingUtility.getInstance();
    
    /**
     * Constructor for XalanVersionTest.
     *
     * @param name
     */
    public QueryTest(String name) {
        super(name);
    }
    
    public void testSelectAdhocQuery() throws Exception {
        testSelectQuery("AdhocQuery");
    }
    
    public void testSelectAssociation() throws Exception {
        testSelectQuery("Association");
    }
    
    public void testSelectAuditableEvent() throws Exception {
        testSelectQuery("AuditableEvent");
    }
    
    public void testSelectClassification() throws Exception {
        testSelectQuery("Classification");
    }
    
    public void testSelectClassificationNode() throws Exception {
        testSelectQuery("ClassificationNode");
    }
    
    public void testSelectClassificationScheme() throws Exception {
        testSelectQuery("ClassificationScheme");
    }
    
    public void testSelectExternalIdentifier() throws Exception {
        testSelectQuery("ExternalIdentifier");
    }
    
    public void testSelectExternalLink() throws Exception {
        testSelectQuery("ExternalLink");
    }
    
    public void testSelectExtrinsicObject() throws Exception {
        testSelectQuery("ExtrinsicObject");
    }
    
    public void testSelectFederation() throws Exception {
        testSelectQuery("Federation");
    }
    
    public void testSelectOrganization() throws Exception {
        testSelectQuery("Organization");
    }
    
    public void testSelectRegistry() throws Exception {
        testSelectQuery("Registry");
    }
    
    public void testSelectRegistryEntry() throws Exception {
        testSelectQuery("RegistryEntry");
    }
    
    public void testSelectRegistryObject() throws Exception {
        testSelectQuery("RegistryObject");
    }
    
    public void testSelectRegistryPackage() throws Exception {
        testSelectQuery("RegistryPackage");
    }
    
    public void testSelectService() throws Exception {
        testSelectQuery("Service");
    }
    
    public void testSelectServiceBinding() throws Exception {
        testSelectQuery("ServiceBinding");
    }
    
    public void testSelectSpecificationLink() throws Exception {
        testSelectQuery("SpecificationLink");
    }
    
    public void testSelectSubscription() throws Exception {
        testSelectQuery("Subscription");
    }
    
    public void testSelectUser() throws Exception {
        testSelectQuery("User");
    }
    
    private void testSelectQuery(String rimClass) throws Exception {          
        String sqlString = "SELECT * FROM " + rimClass;
        SQLQuery sqlQuery = bu.queryFac.createSQLQuery();
        sqlQuery.setId(org.freebxml.omar.common.Utility.getInstance().createId());
        sqlQuery.setQueryString(sqlString);

        AdhocQueryRequest req = bu.queryFac.createAdhocQueryRequest();
        req.setId(org.freebxml.omar.common.Utility.getInstance().createId());
        req.setSQLQuery(sqlQuery);

        ResponseOption ro = bu.queryFac.createResponseOption();
        ro.setReturnComposedObjects(true);
        ro.setReturnType(ReturnType.LEAF_CLASS_WITH_REPOSITORY_ITEM);
        req.setResponseOption(ro);


        AuthenticationServiceImpl authc = AuthenticationServiceImpl.getInstance();
        UserType user = authc.getUserFromAlias(AuthenticationServiceImpl.ALIAS_REGISTRY_GUEST);
        QueryManager qm = QueryManagerFactory.getInstance().getQueryManager();
        AdhocQueryResponseType resp = qm.submitAdhocQuery(user, req);
        bu.getJAXBContext().createMarshaller().marshal(resp, System.err);
        
        //Make sure that there is at least one object that matched the query
        if (resp.getSQLQueryResult().getIdentifiable().size() <= 0) {
            throw new OMARException("Found 0 " + rimClass + " objects to match the query. Expected at least one.");
        }
    }
    
    public static Test suite() {
        return new TestSuite(QueryTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }    
    
}
