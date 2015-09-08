/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/server/query/filter/RegistryObjectQueryProcessor.java,v 1.5 2003/11/11 13:46:50 doballve Exp $
 * ====================================================================
 */
/*
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/server/query/filter/RegistryObjectQueryProcessor.java,v 1.5 2003/11/11 13:46:50 doballve Exp $
 */
package org.freebxml.omar.server.query.filter;

import org.freebxml.omar.common.RegistryException;

import org.oasis.ebxml.registry.bindings.query.FilterType;
import org.oasis.ebxml.registry.bindings.query.RegistryObjectQueryType;

import java.util.Iterator;
import java.util.List;


/**
 * Class Declaration for RegistryObjectQueryProcessor
 * @see
 * @author Nikola Stojanovic
 */
public class RegistryObjectQueryProcessor extends QueryProcessor {
    private RegistryObjectQueryType registryObjectQuery = null;

    protected String getName() {
        return ("RegistryObject");
    }

    protected void setNativeQuery(RegistryObjectQueryType query) {
        registryObjectQuery = (RegistryObjectQueryType) query;
    }

    protected void buildFilterClauses() throws RegistryException {
        convertRegistryObjectFilter();
        convertExternalIdentifierFilters();
    }

    protected void buildQueryClauses() throws RegistryException {
        convertAuditableEventQueries();
    }

    protected void buildBranchClauses() throws RegistryException {
        convertNameBranch();
        convertDescriptionBranch();
        convertClassifiedByBranches();
        convertSlotBranches();
        convertSourceAssociationBranches();
        convertTargetAssociationBranches();
    }

    private void convertRegistryObjectFilter() throws RegistryException {
        if (registryObjectQuery.getRegistryObjectFilter() != null) {
            whereClause = filterProcessor.addNativeWhereClause(whereClause,
                    registryObjectQuery.getRegistryObjectFilter());
        }
    }

    private void convertExternalIdentifierFilters() throws RegistryException {
        List externalIdentifierFilter = registryObjectQuery.getExternalIdentifierFilter();
        Iterator iter = externalIdentifierFilter.iterator();

        if (externalIdentifierFilter.size() > 0) {
            filterProcessor.setSelectColumn("registryObject");

            for (int i = 0; i < externalIdentifierFilter.size(); i++) {
                whereClause = filterProcessor.addNativeWhereClause(whereClause,
                        (FilterType) iter.next());
            }
        }
    }

    private void convertAuditableEventQueries() throws RegistryException {
        List auditableEventQuery = registryObjectQuery.getAuditableEventQuery();
        Iterator iter = auditableEventQuery.iterator();

        if (auditableEventQuery.size() > 0) {
            AuditableEventQueryProcessor queryProcessor = new AuditableEventQueryProcessor();
            queryProcessor.setParentJoinColumn("id");
            queryProcessor.setSelectColumn("registryObject");

            for (int i = 0; i < auditableEventQuery.size(); i++) {
                whereClause = queryProcessor.addWhereClause(whereClause,
                        iter.next());
            }
        }
    }

    private void convertNameBranch() throws RegistryException {
        if (registryObjectQuery.getNameBranch() != null) {
            InternationalStringBranchProcessor branchProcessor = new InternationalStringBranchProcessor();
            branchProcessor.setSelectColumn("parent");
            whereClause = branchProcessor.addWhereClause(whereClause,
                    registryObjectQuery.getNameBranch());
        }
    }

    private void convertDescriptionBranch() throws RegistryException {
        if (registryObjectQuery.getDescriptionBranch() != null) {
            InternationalStringBranchProcessor branchProcessor = new InternationalStringBranchProcessor();
            branchProcessor.setSelectColumn("parent");
            whereClause = branchProcessor.addWhereClause(whereClause,
                    registryObjectQuery.getDescriptionBranch());
        }
    }

    private void convertClassifiedByBranches() throws RegistryException {
        List classifiedByBranch = registryObjectQuery.getClassifiedByBranch();
        Iterator iter = classifiedByBranch.iterator();

        if (classifiedByBranch.size() > 0) {
            for (int i = 0; i < classifiedByBranch.size(); i++) {
                ClassifiedByBranchProcessor branchProcessor = new ClassifiedByBranchProcessor();
                branchProcessor.setSelectColumn("classifiedObject");
                whereClause = branchProcessor.addWhereClause(whereClause,
                        iter.next());
            }
        }
    }

    private void convertSlotBranches() throws RegistryException {
        List slotBranch = registryObjectQuery.getSlotBranch();
        Iterator iter = slotBranch.iterator();

        if (slotBranch.size() > 0) {
            for (int i = 0; i < slotBranch.size(); i++) {
                SlotBranchProcessor branchProcessor = new SlotBranchProcessor();
                branchProcessor.setSelectColumn("parent");
                whereClause = branchProcessor.addWhereClause(whereClause,
                        iter.next());
            }
        }
    }

    private void convertSourceAssociationBranches() throws RegistryException {
        List sourceAssociationBranch = registryObjectQuery.getSourceAssociationBranch();
        Iterator iter = sourceAssociationBranch.iterator();

        if (sourceAssociationBranch.size() > 0) {
            for (int i = 0; i < sourceAssociationBranch.size(); i++) {
                AssociationBranchProcessor branchProcessor = new AssociationBranchProcessor();
                branchProcessor.setSelectColumn("sourceObject");
                whereClause = branchProcessor.addWhereClause(whereClause,
                        iter.next());
            }
        }
    }

    private void convertTargetAssociationBranches() throws RegistryException {
        List targetAssociationBranch = registryObjectQuery.getTargetAssociationBranch();
        Iterator iter = targetAssociationBranch.iterator();

        if (targetAssociationBranch.size() > 0) {
            for (int i = 0; i < targetAssociationBranch.size(); i++) {
                AssociationBranchProcessor branchProcessor = new AssociationBranchProcessor();
                branchProcessor.setSelectColumn("targetObject");
                whereClause = branchProcessor.addWhereClause(whereClause,
                        iter.next());
            }
        }
    }
}
/*
 * $Header: /cvsroot/sino/ebxmlrr/src/share/com/sun/ebxml/registry/query/filter/RegistryObjectQueryProcessor.java,v 1.7 2003/01/14 22:19:45 nstojano Exp $
 */

package com.sun.ebxml.registry.query.filter;

import com.sun.ebxml.registry.*;

import org.oasis.ebxml.registry.bindings.query.*;
import org.oasis.ebxml.registry.bindings.rs.*;


/**
 * Class Declaration for RegistryObjectQueryProcessor
 * @see
 * @author Nikola Stojanovic
 */
public class RegistryObjectQueryProcessor extends QueryProcessor {
    
    private RegistryObjectQueryType registryObjectQuery = new RegistryObjectQuery();
    
    
    protected String getName() {
        
        return("RegistryObject");
    }
    
    
    protected void setNativeQuery(RegistryObjectQueryType query) {
        
        registryObjectQuery = (RegistryObjectQueryType)query;
    }
    
    
    protected void buildFilterClauses() throws RegistryException {
        
        convertRegistryObjectFilter();
        convertExternalIdentifierFilters();
    }
    
    
    protected void buildQueryClauses() throws RegistryException {
        
        convertAuditableEventQueries();
    }
    
    
    protected void buildBranchClauses() throws RegistryException {
        
        convertNameBranch();
        convertDescriptionBranch();
        convertClassifiedByBranches();
        convertSlotBranches();
        convertSourceAssociationBranches();
        convertTargetAssociationBranches();
    }
    
    
    private void convertRegistryObjectFilter() throws RegistryException {
        
        if (registryObjectQuery.getRegistryObjectFilter() != null) {
            whereClause = filterProcessor.addNativeWhereClause(whereClause, registryObjectQuery.getRegistryObjectFilter());
        }
    }
    
    
    private void convertExternalIdentifierFilters() throws RegistryException {
        
        ExternalIdentifierFilter[] externalIdentifierFilter = registryObjectQuery.getExternalIdentifierFilter();
        
        if (externalIdentifierFilter.length > 0) {
            filterProcessor.setSelectColumn("registryObject");
            
            for (int i=0; i<externalIdentifierFilter.length; i++) {
                whereClause = filterProcessor.addNativeWhereClause(whereClause, externalIdentifierFilter[i]);
            }
        }
    }
    
    
    private void convertAuditableEventQueries() throws RegistryException {
        
        AuditableEventQuery[] auditableEventQuery = registryObjectQuery.getAuditableEventQuery();
        
        if (auditableEventQuery.length > 0) {
            AuditableEventQueryProcessor queryProcessor = new AuditableEventQueryProcessor();
            queryProcessor.setParentJoinColumn("id");
            queryProcessor.setSelectColumn("registryObject");
            
            for (int i=0; i<auditableEventQuery.length; i++) {
                whereClause = queryProcessor.addWhereClause(whereClause, auditableEventQuery);
            }
        }
    }
    
    
    private void convertNameBranch() throws RegistryException {
        
        if (registryObjectQuery.getNameBranch() != null) {
            InternationalStringBranchProcessor branchProcessor = new InternationalStringBranchProcessor();
            branchProcessor.setSelectColumn("parent");
            whereClause = branchProcessor.addWhereClause(whereClause, registryObjectQuery.getNameBranch());
        }
    }
    
    
    private void convertDescriptionBranch() throws RegistryException {
        
        if (registryObjectQuery.getDescriptionBranch() != null) {
            InternationalStringBranchProcessor branchProcessor = new InternationalStringBranchProcessor();
            branchProcessor.setSelectColumn("parent");
            whereClause = branchProcessor.addWhereClause(whereClause, registryObjectQuery.getDescriptionBranch());
        }
    }
    
    
    private void convertClassifiedByBranches() throws RegistryException {
        
        ClassifiedByBranch[] classifiedByBranch = registryObjectQuery.getClassifiedByBranch();
        
        if (classifiedByBranch.length > 0) {
            
            for (int i=0; i<classifiedByBranch.length; i++) {
                ClassifiedByBranchProcessor branchProcessor = new ClassifiedByBranchProcessor();
                branchProcessor.setSelectColumn("classifiedObject");
                whereClause = branchProcessor.addWhereClause(whereClause, classifiedByBranch[i]);
            }
        }
    }
    
    
    private void convertSlotBranches() throws RegistryException {
        
        SlotBranch[] slotBranch = registryObjectQuery.getSlotBranch();
        
        if (slotBranch.length > 0) {
           
            for (int i=0; i<slotBranch.length; i++) {
                SlotBranchProcessor branchProcessor = new SlotBranchProcessor();
                branchProcessor.setSelectColumn("parent");
                whereClause = branchProcessor.addWhereClause(whereClause, slotBranch[i]);
            }
        }
    }
    
    
    private void convertSourceAssociationBranches() throws RegistryException {
        
        SourceAssociationBranch[] sourceAssociationBranch = registryObjectQuery.getSourceAssociationBranch();
        
        if (sourceAssociationBranch.length > 0) {
            
            for (int i=0; i<sourceAssociationBranch.length; i++) {
                AssociationBranchProcessor branchProcessor = new AssociationBranchProcessor();
                branchProcessor.setSelectColumn("sourceObject");
                whereClause = branchProcessor.addWhereClause(whereClause, sourceAssociationBranch[i]);
            }
        }
    }
    
    
    private void convertTargetAssociationBranches() throws RegistryException {
        
        TargetAssociationBranch[] targetAssociationBranch = registryObjectQuery.getTargetAssociationBranch();
        
        if (targetAssociationBranch.length > 0) {
            
            for (int i=0; i<targetAssociationBranch.length; i++) {
                AssociationBranchProcessor branchProcessor = new AssociationBranchProcessor();
                branchProcessor.setSelectColumn("targetObject");
                whereClause = branchProcessor.addWhereClause(whereClause, targetAssociationBranch[i]);
           }
        }
    }
}