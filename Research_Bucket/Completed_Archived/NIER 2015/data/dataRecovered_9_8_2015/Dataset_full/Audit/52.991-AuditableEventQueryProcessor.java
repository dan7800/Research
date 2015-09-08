/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/server/query/filter/AuditableEventQueryProcessor.java,v 1.5 2003/11/11 13:46:50 doballve Exp $
 * ====================================================================
 */
/*
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/server/query/filter/AuditableEventQueryProcessor.java,v 1.5 2003/11/11 13:46:50 doballve Exp $
 */
package org.freebxml.omar.server.query.filter;

import org.freebxml.omar.common.RegistryException;

import org.oasis.ebxml.registry.bindings.query.AuditableEventQuery;
import org.oasis.ebxml.registry.bindings.query.AuditableEventQueryType;
import org.oasis.ebxml.registry.bindings.query.RegistryObjectQueryType;


/**
 * Class Declaration for AuditableEventQueryProcessor
 * @see
 * @author Nikola Stojanovic
 */
public class AuditableEventQueryProcessor extends RegistryObjectQueryProcessor {
    private AuditableEventQueryType auditableEventQuery = null;

    protected String getName() {
        return "AuditableEvent";
    }

    protected void setNativeQuery(RegistryObjectQueryType query) {
        auditableEventQuery = (AuditableEventQuery) query;
        super.setNativeQuery((RegistryObjectQueryType) auditableEventQuery);
    }

    protected void buildFilterClauses() throws RegistryException {
        convertAuditableEventFilter();
        super.buildFilterClauses();
    }

    protected void buildQueryClauses() throws RegistryException {
        convertRegistryObjectQuery();
        convertRegistryEntryQuery();
        super.buildQueryClauses();
    }

    protected void buildBranchClauses() throws RegistryException {
        convertUserBranch();
        super.buildBranchClauses();
    }

    private void convertAuditableEventFilter() throws RegistryException {
        if (auditableEventQuery.getAuditableEventFilter() != null) {
            whereClause = filterProcessor.addNativeWhereClause(whereClause,
                    auditableEventQuery.getAuditableEventFilter());
        }
    }

    private void convertRegistryObjectQuery() throws RegistryException {
        if (auditableEventQuery.getRegistryObjectQuery() != null) {
            RegistryObjectQueryProcessor queryProcessor = new RegistryObjectQueryProcessor();
            queryProcessor.setParentJoinColumn("registryObject");
            queryProcessor.setSelectColumn("id");
            whereClause = queryProcessor.addWhereClause(whereClause,
                    auditableEventQuery.getRegistryObjectQuery());
        }
    }

    private void convertRegistryEntryQuery() throws RegistryException {
        if (auditableEventQuery.getRegistryEntryQuery() != null) {
            RegistryEntryQueryProcessor queryProcessor = new RegistryEntryQueryProcessor();
            queryProcessor.setParentJoinColumn("registryObject");
            queryProcessor.setSelectColumn("id");
            whereClause = queryProcessor.addWhereClause(whereClause,
                    auditableEventQuery.getRegistryEntryQuery());
        }
    }

    private void convertUserBranch() throws RegistryException {
        if (auditableEventQuery.getUserQuery() != null) {
            UserQueryProcessor queryProcessor = new UserQueryProcessor();
            queryProcessor.setParentJoinColumn("user");
            queryProcessor.setSelectColumn("id");
            whereClause = queryProcessor.addWhereClause(whereClause,
                    auditableEventQuery.getUserQuery());
        }
    }
}
/*
 * $Header: /cvsroot/sino/ebxmlrr/src/share/com/sun/ebxml/registry/query/filter/AuditableEventQueryProcessor.java,v 1.6 2003/01/14 22:22:35 nstojano Exp $
 */

package com.sun.ebxml.registry.query.filter;

import com.sun.ebxml.registry.*;

import org.oasis.ebxml.registry.bindings.query.*;
import org.oasis.ebxml.registry.bindings.rs.*;

/**
 * Class Declaration for AuditableEventQueryProcessor
 * @see
 * @author Nikola Stojanovic
 */
public class AuditableEventQueryProcessor extends RegistryObjectQueryProcessor {
    
    private AuditableEventQuery auditableEventQuery = null;
    
    
    protected String getName() {
        
        return "AuditableEvent";
    }
    
    
    protected void setNativeQuery(RegistryObjectQueryType query) {
        
        auditableEventQuery = (AuditableEventQuery)query;
        super.setNativeQuery((RegistryObjectQueryType)auditableEventQuery);
    }
    
    
    protected void buildFilterClauses() throws RegistryException {
        
        convertAuditableEventFilter();
        super.buildFilterClauses();
    }
    
    
    protected void buildQueryClauses() throws RegistryException {
        
        convertRegistryObjectQuery();
        convertRegistryEntryQuery();
        super.buildQueryClauses();
    }
    
    
    protected void buildBranchClauses() throws RegistryException {
        
        convertUserBranch();
        super.buildBranchClauses();
    }
    
    
    private void convertAuditableEventFilter() throws RegistryException {
        
        if (auditableEventQuery.getAuditableEventFilter() != null) {
            whereClause = filterProcessor.addNativeWhereClause(whereClause, auditableEventQuery.getAuditableEventFilter());
        }
    }
    
    
    private void convertRegistryObjectQuery() throws RegistryException {
        
        if (auditableEventQuery.getRegistryObjectQuery() != null) {
            RegistryObjectQueryProcessor queryProcessor = new RegistryObjectQueryProcessor();
            queryProcessor.setParentJoinColumn("registryObject");
            queryProcessor.setSelectColumn("id");
            whereClause = queryProcessor.addWhereClause(whereClause, auditableEventQuery.getRegistryObjectQuery());
        }
    }
    
    
    private void convertRegistryEntryQuery() throws RegistryException {
        
        if (auditableEventQuery.getRegistryEntryQuery() != null) {
            RegistryEntryQueryProcessor queryProcessor = new RegistryEntryQueryProcessor();
            queryProcessor.setParentJoinColumn("registryObject");
            queryProcessor.setSelectColumn("id");
            whereClause = queryProcessor.addWhereClause(whereClause, auditableEventQuery.getRegistryEntryQuery());
        }
    }      
    
    
    private void convertUserBranch() throws RegistryException {
        
        if (auditableEventQuery.getUserBranch() != null) {
            UserBranchProcessor branchProcessor = new UserBranchProcessor();
            branchProcessor.setParentJoinColumn("user");            
            branchProcessor.setSelectColumn("id");
            whereClause = branchProcessor.addWhereClause(whereClause, auditableEventQuery.getUserBranch());
        }
    }    
}