/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/server/query/filter/AssociationBranchProcessor.java,v 1.5 2003/11/11 13:46:50 doballve Exp $
 * ====================================================================
 */
/*
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/server/query/filter/AssociationBranchProcessor.java,v 1.5 2003/11/11 13:46:50 doballve Exp $
 */
package org.freebxml.omar.server.query.filter;

import org.freebxml.omar.common.RegistryException;

import org.oasis.ebxml.registry.bindings.query.AssociationFilter;
import org.oasis.ebxml.registry.bindings.query.AssociationQueryType;
import org.oasis.ebxml.registry.bindings.query.AuditableEventQueryType;
import org.oasis.ebxml.registry.bindings.query.ClassificationNodeQueryType;
import org.oasis.ebxml.registry.bindings.query.ClassificationQueryType;
import org.oasis.ebxml.registry.bindings.query.ClassificationSchemeQueryType;
import org.oasis.ebxml.registry.bindings.query.ExternalIdentifierFilter;
import org.oasis.ebxml.registry.bindings.query.ExternalLinkFilter;
import org.oasis.ebxml.registry.bindings.query.ExtrinsicObjectQueryType;
import org.oasis.ebxml.registry.bindings.query.OrganizationQueryType;
import org.oasis.ebxml.registry.bindings.query.RegistryEntryQueryType;
import org.oasis.ebxml.registry.bindings.query.RegistryObjectQueryType;
import org.oasis.ebxml.registry.bindings.query.RegistryPackageQueryType;
import org.oasis.ebxml.registry.bindings.query.ServiceBindingBranchType;
import org.oasis.ebxml.registry.bindings.query.ServiceQueryType;
import org.oasis.ebxml.registry.bindings.query.SourceAssociationBranch;
import org.oasis.ebxml.registry.bindings.query.SpecificationLinkBranchType;
import org.oasis.ebxml.registry.bindings.query.TargetAssociationBranch;
import org.oasis.ebxml.registry.bindings.query.UserQueryType;


/**
 * Class Declaration for AssociationBranchProcessor
 * @see
 * @author Nikola Stojanovic
 */
public class AssociationBranchProcessor extends BranchProcessor {
    private SourceAssociationBranch sourceAssociationBranch = null;
    private TargetAssociationBranch targetAssociationBranch = null;

    protected String getName() {
        return "Association";
    }

    protected void setNativeBranch(Object branch) {
        if (branch instanceof org.oasis.ebxml.registry.bindings.query.SourceAssociationBranch) {
            sourceAssociationBranch = (SourceAssociationBranch) branch;
        } else if (branch instanceof org.oasis.ebxml.registry.bindings.query.TargetAssociationBranch) {
            targetAssociationBranch = (TargetAssociationBranch) branch;
        }
    }

    protected void buildFilterClauses() throws RegistryException {
        convertAssociationFilter();
        convertExternalLinkFilter();
        convertExternalIdentifierFilter();
    }

    protected void buildQueryClauses() throws RegistryException {
        convertRegistryEntryQuery();
        convertRegistryObjectQuery();
        convertAssociationQuery();
        convertClassificationQuery();
        convertClassificationSchemeQuery();
        convertClassificationNodeQuery();
        convertOrganizationQuery();
        convertAuditableEventQuery();
        convertRegistryPackageQuery();
        convertExtrinsicObjectQuery();
        convertServiceQuery();
    }

    protected void buildBranchClauses() throws RegistryException {
        convertUserQuery();
        convertServiceBindingBranch();
        convertSpecificationLinkBranch();
    }

    private void convertAssociationFilter() throws RegistryException {
        AssociationFilter associationFilter = null;

        if (sourceAssociationBranch != null) {
            associationFilter = (AssociationFilter) sourceAssociationBranch.getAssociationFilter();
        } else if (targetAssociationBranch != null) {
            associationFilter = (AssociationFilter) targetAssociationBranch.getAssociationFilter();
        }

        if (associationFilter != null) {
            whereClause = filterProcessor.addNativeWhereClause(whereClause,
                    associationFilter);
        }
    }

    private void convertExternalLinkFilter() throws RegistryException {
        ExternalLinkFilter externalLinkFilter = null;

        if (sourceAssociationBranch != null) {
            externalLinkFilter = (ExternalLinkFilter) sourceAssociationBranch.getExternalLinkFilter();
        } else if (targetAssociationBranch != null) {
            externalLinkFilter = (ExternalLinkFilter) targetAssociationBranch.getExternalLinkFilter();
        }

        if (externalLinkFilter != null) {
            whereClause = filterProcessor.addForeignWhereClause(whereClause,
                    externalLinkFilter);
        }
    }

    private void convertExternalIdentifierFilter() throws RegistryException {
        ExternalIdentifierFilter externalIdentifierFilter = null;

        if (sourceAssociationBranch != null) {
            externalIdentifierFilter = (ExternalIdentifierFilter) sourceAssociationBranch.getExternalIdentifierFilter();
        } else if (targetAssociationBranch != null) {
            externalIdentifierFilter = (ExternalIdentifierFilter) targetAssociationBranch.getExternalIdentifierFilter();
        }

        if (externalIdentifierFilter != null) {
            whereClause = filterProcessor.addForeignWhereClause(whereClause,
                    externalIdentifierFilter);
        }
    }

    private void convertRegistryEntryQuery() throws RegistryException {
        RegistryEntryQueryType registryEntryQuery = null;

        if (sourceAssociationBranch != null) {
            registryEntryQuery = sourceAssociationBranch.getRegistryEntryQuery();

            if (registryEntryQuery != null) {
                RegistryEntryQueryProcessor queryProcessor = new RegistryEntryQueryProcessor();
                queryProcessor.setParentJoinColumn("targetObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause,
                        registryEntryQuery);
            }
        } else if (targetAssociationBranch != null) {
            registryEntryQuery = targetAssociationBranch.getRegistryEntryQuery();

            if (registryEntryQuery != null) {
                RegistryEntryQueryProcessor queryProcessor = new RegistryEntryQueryProcessor();
                queryProcessor.setParentJoinColumn("sourceObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause,
                        registryEntryQuery);
            }
        }
    }

    private void convertRegistryObjectQuery() throws RegistryException {
        RegistryObjectQueryType registryObjectQuery = null;

        if (sourceAssociationBranch != null) {
            registryObjectQuery = sourceAssociationBranch.getRegistryObjectQuery();

            if (registryObjectQuery != null) {
                RegistryObjectQueryProcessor queryProcessor = new RegistryObjectQueryProcessor();
                queryProcessor.setParentJoinColumn("targetObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause,
                        registryObjectQuery);
            }
        } else if (targetAssociationBranch != null) {
            registryObjectQuery = targetAssociationBranch.getRegistryObjectQuery();

            if (registryObjectQuery != null) {
                RegistryObjectQueryProcessor queryProcessor = new RegistryObjectQueryProcessor();
                queryProcessor.setParentJoinColumn("sourceObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause,
                        registryObjectQuery);
            }
        }
    }

    private void convertAssociationQuery() throws RegistryException {
        AssociationQueryType associationQuery = null;

        if (sourceAssociationBranch != null) {
            associationQuery = sourceAssociationBranch.getAssociationQuery();

            if (associationQuery != null) {
                AssociationQueryProcessor queryProcessor = new AssociationQueryProcessor();
                queryProcessor.setParentJoinColumn("targetObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause,
                        associationQuery);
            }
        } else if (targetAssociationBranch != null) {
            associationQuery = targetAssociationBranch.getAssociationQuery();

            if (associationQuery != null) {
                AssociationQueryProcessor queryProcessor = new AssociationQueryProcessor();
                queryProcessor.setParentJoinColumn("sourceObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause,
                        associationQuery);
            }
        }
    }

    private void convertClassificationQuery() throws RegistryException {
        ClassificationQueryType classificationQuery = null;

        if (sourceAssociationBranch != null) {
            classificationQuery = sourceAssociationBranch.getClassificationQuery();

            if (classificationQuery != null) {
                ClassificationQueryProcessor queryProcessor = new ClassificationQueryProcessor();
                queryProcessor.setParentJoinColumn("targetObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause,
                        classificationQuery);
            }
        } else if (targetAssociationBranch != null) {
            classificationQuery = targetAssociationBranch.getClassificationQuery();

            if (classificationQuery != null) {
                ClassificationQueryProcessor queryProcessor = new ClassificationQueryProcessor();
                queryProcessor.setParentJoinColumn("sourceObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause,
                        classificationQuery);
            }
        }
    }

    private void convertClassificationSchemeQuery() throws RegistryException {
        ClassificationSchemeQueryType classificationSchemeQuery = null;

        if (sourceAssociationBranch != null) {
            classificationSchemeQuery = sourceAssociationBranch.getClassificationSchemeQuery();

            if (classificationSchemeQuery != null) {
                ClassificationSchemeQueryProcessor queryProcessor = new ClassificationSchemeQueryProcessor();
                queryProcessor.setParentJoinColumn("targetObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause,
                        classificationSchemeQuery);
            }
        } else if (targetAssociationBranch != null) {
            classificationSchemeQuery = targetAssociationBranch.getClassificationSchemeQuery();

            if (classificationSchemeQuery != null) {
                ClassificationSchemeQueryProcessor queryProcessor = new ClassificationSchemeQueryProcessor();
                queryProcessor.setParentJoinColumn("sourceObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause,
                        classificationSchemeQuery);
            }
        }
    }

    private void convertClassificationNodeQuery() throws RegistryException {
        ClassificationNodeQueryType classificationNodeQuery = null;

        if (sourceAssociationBranch != null) {
            classificationNodeQuery = sourceAssociationBranch.getClassificationNodeQuery();

            if (classificationNodeQuery != null) {
                ClassificationNodeQueryProcessor queryProcessor = new ClassificationNodeQueryProcessor();
                queryProcessor.setParentJoinColumn("targetObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause,
                        classificationNodeQuery);
            }
        } else if (targetAssociationBranch != null) {
            classificationNodeQuery = targetAssociationBranch.getClassificationNodeQuery();

            if (classificationNodeQuery != null) {
                ClassificationNodeQueryProcessor queryProcessor = new ClassificationNodeQueryProcessor();
                queryProcessor.setParentJoinColumn("sourceObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause,
                        classificationNodeQuery);
            }
        }
    }

    private void convertOrganizationQuery() throws RegistryException {
        OrganizationQueryType organizationQuery = null;

        if (sourceAssociationBranch != null) {
            organizationQuery = sourceAssociationBranch.getOrganizationQuery();

            if (organizationQuery != null) {
                OrganizationQueryProcessor queryProcessor = new OrganizationQueryProcessor();
                queryProcessor.setParentJoinColumn("sourceObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause,
                        organizationQuery);
            }
        } else if (targetAssociationBranch != null) {
            organizationQuery = targetAssociationBranch.getOrganizationQuery();

            if (organizationQuery != null) {
                OrganizationQueryProcessor queryProcessor = new OrganizationQueryProcessor();
                queryProcessor.setParentJoinColumn("targetObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause,
                        organizationQuery);
            }
        }
    }

    private void convertAuditableEventQuery() throws RegistryException {
        AuditableEventQueryType auditableEventQuery = null;

        if (sourceAssociationBranch != null) {
            auditableEventQuery = sourceAssociationBranch.getAuditableEventQuery();

            if (auditableEventQuery != null) {
                AuditableEventQueryProcessor queryProcessor = new AuditableEventQueryProcessor();
                queryProcessor.setParentJoinColumn("sourceObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause,
                        auditableEventQuery);
            }
        } else if (targetAssociationBranch != null) {
            auditableEventQuery = targetAssociationBranch.getAuditableEventQuery();

            if (auditableEventQuery != null) {
                AuditableEventQueryProcessor queryProcessor = new AuditableEventQueryProcessor();
                queryProcessor.setParentJoinColumn("targetObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause,
                        auditableEventQuery);
            }
        }
    }

    private void convertRegistryPackageQuery() throws RegistryException {
        RegistryPackageQueryType registryPackageQuery = null;

        if (sourceAssociationBranch != null) {
            registryPackageQuery = sourceAssociationBranch.getRegistryPackageQuery();

            if (registryPackageQuery != null) {
                RegistryPackageQueryProcessor queryProcessor = new RegistryPackageQueryProcessor();
                queryProcessor.setParentJoinColumn("sourceObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause,
                        registryPackageQuery);
            }
        } else if (targetAssociationBranch != null) {
            registryPackageQuery = targetAssociationBranch.getRegistryPackageQuery();

            if (registryPackageQuery != null) {
                RegistryPackageQueryProcessor queryProcessor = new RegistryPackageQueryProcessor();
                queryProcessor.setParentJoinColumn("targetObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause,
                        registryPackageQuery);
            }
        }
    }

    private void convertExtrinsicObjectQuery() throws RegistryException {
        ExtrinsicObjectQueryType extrinsicObjectQuery = null;

        if (sourceAssociationBranch != null) {
            extrinsicObjectQuery = sourceAssociationBranch.getExtrinsicObjectQuery();

            if (extrinsicObjectQuery != null) {
                ExtrinsicObjectQueryProcessor queryProcessor = new ExtrinsicObjectQueryProcessor();
                queryProcessor.setParentJoinColumn("sourceObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause,
                        extrinsicObjectQuery);
            }
        } else if (targetAssociationBranch != null) {
            extrinsicObjectQuery = targetAssociationBranch.getExtrinsicObjectQuery();

            if (extrinsicObjectQuery != null) {
                ExtrinsicObjectQueryProcessor queryProcessor = new ExtrinsicObjectQueryProcessor();
                queryProcessor.setParentJoinColumn("targetObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause,
                        extrinsicObjectQuery);
            }
        }
    }

    private void convertServiceQuery() throws RegistryException {
        ServiceQueryType serviceQuery = null;

        if (sourceAssociationBranch != null) {
            serviceQuery = sourceAssociationBranch.getServiceQuery();

            if (serviceQuery != null) {
                ServiceQueryProcessor queryProcessor = new ServiceQueryProcessor();
                queryProcessor.setParentJoinColumn("sourceObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause,
                        serviceQuery);
            }
        } else if (targetAssociationBranch != null) {
            serviceQuery = targetAssociationBranch.getServiceQuery();

            if (serviceQuery != null) {
                ServiceQueryProcessor queryProcessor = new ServiceQueryProcessor();
                queryProcessor.setParentJoinColumn("targetObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause,
                        serviceQuery);
            }
        }
    }

    private void convertUserQuery() throws RegistryException {
        UserQueryType userQuery = null;

        if (sourceAssociationBranch != null) {
            userQuery = sourceAssociationBranch.getUserQuery();

            if (userQuery != null) {
                UserQueryProcessor queryProcessor = new UserQueryProcessor();
                queryProcessor.setParentJoinColumn("sourceObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause,
                        userQuery);
            }
        } else if (targetAssociationBranch != null) {
            userQuery = targetAssociationBranch.getUserQuery();

            if (userQuery != null) {
                UserQueryProcessor queryProcessor = new UserQueryProcessor();
                queryProcessor.setParentJoinColumn("targetObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause,
                        userQuery);
            }
        }
    }

    private void convertServiceBindingBranch() throws RegistryException {
        ServiceBindingBranchType serviceBindingBranch = null;

        if (sourceAssociationBranch != null) {
            serviceBindingBranch = sourceAssociationBranch.getServiceBindingBranch();

            if (serviceBindingBranch != null) {
                ServiceBindingBranchProcessor branchProcessor = new ServiceBindingBranchProcessor();
                branchProcessor.setParentJoinColumn("sourceObject");
                branchProcessor.setSelectColumn("id");
                whereClause = branchProcessor.addWhereClause(whereClause,
                        serviceBindingBranch);
            }
        } else if (targetAssociationBranch != null) {
            serviceBindingBranch = targetAssociationBranch.getServiceBindingBranch();

            if (serviceBindingBranch != null) {
                ServiceBindingBranchProcessor branchProcessor = new ServiceBindingBranchProcessor();
                branchProcessor.setParentJoinColumn("targetObject");
                branchProcessor.setSelectColumn("id");
                whereClause = branchProcessor.addWhereClause(whereClause,
                        serviceBindingBranch);
            }
        }
    }

    private void convertSpecificationLinkBranch() throws RegistryException {
        SpecificationLinkBranchType specificationLinkBranch = null;

        if (sourceAssociationBranch != null) {
            specificationLinkBranch = sourceAssociationBranch.getSpecificationLinkBranch();

            if (specificationLinkBranch != null) {
                SpecificationLinkBranchProcessor branchProcessor = new SpecificationLinkBranchProcessor();
                branchProcessor.setParentJoinColumn("sourceObject");
                branchProcessor.setSelectColumn("id");
                whereClause = branchProcessor.addWhereClause(whereClause,
                        specificationLinkBranch);
            }
        } else if (targetAssociationBranch != null) {
            specificationLinkBranch = targetAssociationBranch.getSpecificationLinkBranch();

            if (specificationLinkBranch != null) {
                SpecificationLinkBranchProcessor branchProcessor = new SpecificationLinkBranchProcessor();
                branchProcessor.setParentJoinColumn("targetObject");
                branchProcessor.setSelectColumn("id");
                whereClause = branchProcessor.addWhereClause(whereClause,
                        specificationLinkBranch);
            }
        }
    }
}
/*
 * $Header: /cvsroot/sino/ebxmlrr/src/share/com/sun/ebxml/registry/query/filter/AssociationBranchProcessor.java,v 1.6 2002/03/06 04:40:40 nstojano Exp $
 */

package com.sun.ebxml.registry.query.filter;

import com.sun.ebxml.registry.*;

import org.oasis.ebxml.registry.bindings.query.*;
import org.oasis.ebxml.registry.bindings.rs.*;

/**
 * Class Declaration for AssociationBranchProcessor
 * @see
 * @author Nikola Stojanovic
 */
public class AssociationBranchProcessor extends BranchProcessor {
    
    private SourceAssociationBranch sourceAssociationBranch = null;
    private TargetAssociationBranch targetAssociationBranch = null;
    
    
    protected String getName() {
        
        return "Association";
    }
    
    
    protected void setNativeBranch(Object branch) {
        
        if (branch instanceof org.oasis.ebxml.registry.bindings.query.SourceAssociationBranch) {
            sourceAssociationBranch = (SourceAssociationBranch)branch;
        }
        else if (branch instanceof org.oasis.ebxml.registry.bindings.query.TargetAssociationBranch) {
            targetAssociationBranch = (TargetAssociationBranch)branch;
        }
    }
    
    
    protected void buildFilterClauses() throws RegistryException {
        
        convertAssociationFilter();
        convertExternalLinkFilter();
        convertExternalIdentifierFilter();
    }
    
    
    protected void buildQueryClauses() throws RegistryException {
        
        convertRegistryEntryQuery();
        convertRegistryObjectQuery();
        convertAssociationQuery();
        convertClassificationQuery();
        convertClassificationSchemeQuery();
        convertClassificationNodeQuery();
        convertOrganizationQuery();
        convertAuditableEventQuery();
        convertRegistryPackageQuery();
        convertExtrinsicObjectQuery();
        convertServiceQuery();
    }
    
    
    protected void buildBranchClauses() throws RegistryException {
        
        convertUserBranch();
        convertServiceBindingBranch();
        convertSpecificationLinkBranch();
    }
    
    
    private void convertAssociationFilter() throws RegistryException {
        
        AssociationFilter associationFilter = null;
        
        if (sourceAssociationBranch != null) {
            associationFilter = sourceAssociationBranch.getAssociationFilter();
        }
        else if (targetAssociationBranch != null){
            associationFilter = targetAssociationBranch.getAssociationFilter();
        }
        
        if (associationFilter != null) {
            whereClause = filterProcessor.addNativeWhereClause(whereClause, associationFilter);
        }
    }
    
    
    private void convertExternalLinkFilter() throws RegistryException {
        
        ExternalLinkFilter externalLinkFilter = null;
        
        if (sourceAssociationBranch != null) {
            externalLinkFilter = sourceAssociationBranch.getAssociationBranchTypeChoice().getExternalLinkFilter();
        }
        else if (targetAssociationBranch != null){
            externalLinkFilter = targetAssociationBranch.getAssociationBranchTypeChoice().getExternalLinkFilter();
        }
        
        if (externalLinkFilter != null) {
            whereClause = filterProcessor.addForeignWhereClause(whereClause, externalLinkFilter);
        }
    }
    
    
    private void convertExternalIdentifierFilter() throws RegistryException {
        
        ExternalIdentifierFilter externalIdentifierFilter = null;
        
        if (sourceAssociationBranch != null) {
            externalIdentifierFilter = sourceAssociationBranch.getAssociationBranchTypeChoice().getExternalIdentifierFilter();
        }
        else if (targetAssociationBranch != null){
            externalIdentifierFilter = targetAssociationBranch.getAssociationBranchTypeChoice().getExternalIdentifierFilter();
        }
        
        if (externalIdentifierFilter != null) {
            whereClause = filterProcessor.addForeignWhereClause(whereClause, externalIdentifierFilter);
        }
    }
    
    
    private void convertRegistryEntryQuery() throws RegistryException {
        
        RegistryEntryQuery registryEntryQuery = null;
        
        if (sourceAssociationBranch != null) {
            registryEntryQuery = sourceAssociationBranch.getAssociationBranchTypeChoice().getRegistryEntryQuery();
            
            if (registryEntryQuery != null) {
                RegistryEntryQueryProcessor queryProcessor = new RegistryEntryQueryProcessor();
                queryProcessor.setParentJoinColumn("targetObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause, registryEntryQuery);
            }
        }
        else if (targetAssociationBranch != null){
            registryEntryQuery = targetAssociationBranch.getAssociationBranchTypeChoice().getRegistryEntryQuery();
            
            if (registryEntryQuery != null) {
                RegistryEntryQueryProcessor queryProcessor = new RegistryEntryQueryProcessor();
                queryProcessor.setParentJoinColumn("sourceObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause, registryEntryQuery);
            }
        }
    }
    
    
    private void convertRegistryObjectQuery() throws RegistryException {
        
        RegistryObjectQuery registryObjectQuery = null;
        
        if (sourceAssociationBranch != null) {
            registryObjectQuery = sourceAssociationBranch.getAssociationBranchTypeChoice().getRegistryObjectQuery();
            
            if (registryObjectQuery != null) {
                RegistryObjectQueryProcessor queryProcessor = new RegistryObjectQueryProcessor();
                queryProcessor.setParentJoinColumn("targetObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause, registryObjectQuery);
            }
        }
        else if (targetAssociationBranch != null){
            registryObjectQuery = targetAssociationBranch.getAssociationBranchTypeChoice().getRegistryObjectQuery();
            
            if (registryObjectQuery != null) {
                RegistryObjectQueryProcessor queryProcessor = new RegistryObjectQueryProcessor();
                queryProcessor.setParentJoinColumn("sourceObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause, registryObjectQuery);
            }
        }
    }
    
    
    private void convertAssociationQuery() throws RegistryException {
        
        AssociationQuery associationQuery = null;
        
        if (sourceAssociationBranch != null) {
            associationQuery = sourceAssociationBranch.getAssociationBranchTypeChoice().getAssociationQuery();
            
            if (associationQuery != null) {
                AssociationQueryProcessor queryProcessor = new AssociationQueryProcessor();
                queryProcessor.setParentJoinColumn("targetObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause, associationQuery);
            }
        }
        else if (targetAssociationBranch != null){
            associationQuery = targetAssociationBranch.getAssociationBranchTypeChoice().getAssociationQuery();
            
            if (associationQuery != null) {
                AssociationQueryProcessor queryProcessor = new AssociationQueryProcessor();
                queryProcessor.setParentJoinColumn("sourceObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause, associationQuery);
            }
        }
    }
    
    
    private void convertClassificationQuery() throws RegistryException {
        
        ClassificationQuery classificationQuery = null;
        
        if (sourceAssociationBranch != null) {
            classificationQuery = sourceAssociationBranch.getAssociationBranchTypeChoice().getClassificationQuery();
            
            if (classificationQuery != null) {
                ClassificationQueryProcessor queryProcessor = new ClassificationQueryProcessor();
                queryProcessor.setParentJoinColumn("targetObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause, classificationQuery);
            }
        }
        else if (targetAssociationBranch != null){
            classificationQuery = targetAssociationBranch.getAssociationBranchTypeChoice().getClassificationQuery();
            
            if (classificationQuery != null) {
                ClassificationQueryProcessor queryProcessor = new ClassificationQueryProcessor();
                queryProcessor.setParentJoinColumn("sourceObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause, classificationQuery);
            }
        }
    }
    
    
    private void convertClassificationSchemeQuery() throws RegistryException {
        
        ClassificationSchemeQuery classificationSchemeQuery = null;
        
        if (sourceAssociationBranch != null) {
            classificationSchemeQuery = sourceAssociationBranch.getAssociationBranchTypeChoice().getClassificationSchemeQuery();
            
            if (classificationSchemeQuery != null) {
                ClassificationSchemeQueryProcessor queryProcessor = new ClassificationSchemeQueryProcessor();
                queryProcessor.setParentJoinColumn("targetObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause, classificationSchemeQuery);
            }
        }
        else if (targetAssociationBranch != null){
            classificationSchemeQuery = targetAssociationBranch.getAssociationBranchTypeChoice().getClassificationSchemeQuery();
            
            if (classificationSchemeQuery != null) {
                ClassificationSchemeQueryProcessor queryProcessor = new ClassificationSchemeQueryProcessor();
                queryProcessor.setParentJoinColumn("sourceObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause, classificationSchemeQuery);
            }
        }
    }
    
    
    private void convertClassificationNodeQuery() throws RegistryException {
        
        ClassificationNodeQuery classificationNodeQuery = null;
        
        if (sourceAssociationBranch != null) {
            classificationNodeQuery = sourceAssociationBranch.getAssociationBranchTypeChoice().getClassificationNodeQuery();
            
            if (classificationNodeQuery != null) {
                ClassificationNodeQueryProcessor queryProcessor = new ClassificationNodeQueryProcessor();
                queryProcessor.setParentJoinColumn("targetObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause, classificationNodeQuery);
            }
        }
        else if (targetAssociationBranch != null){
            classificationNodeQuery = targetAssociationBranch.getAssociationBranchTypeChoice().getClassificationNodeQuery();
            
            if (classificationNodeQuery != null) {
                ClassificationNodeQueryProcessor queryProcessor = new ClassificationNodeQueryProcessor();
                queryProcessor.setParentJoinColumn("sourceObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause, classificationNodeQuery);
            }
        }
    }
    
    
    private void convertOrganizationQuery() throws RegistryException {
        
        OrganizationQuery organizationQuery = null;
        
        if (sourceAssociationBranch != null) {
            organizationQuery = sourceAssociationBranch.getAssociationBranchTypeChoice().getOrganizationQuery();
            
            if (organizationQuery != null) {
                OrganizationQueryProcessor queryProcessor = new OrganizationQueryProcessor();
                queryProcessor.setParentJoinColumn("sourceObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause, organizationQuery);
            }
        }
        else if (targetAssociationBranch != null) {
            organizationQuery = targetAssociationBranch.getAssociationBranchTypeChoice().getOrganizationQuery();
            
            if (organizationQuery != null) {
                OrganizationQueryProcessor queryProcessor = new OrganizationQueryProcessor();
                queryProcessor.setParentJoinColumn("targetObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause, organizationQuery);
            }
        }
    }
    
    
    private void convertAuditableEventQuery() throws RegistryException {
        
        AuditableEventQuery auditableEventQuery = null;
        
        if (sourceAssociationBranch != null) {
            auditableEventQuery = sourceAssociationBranch.getAssociationBranchTypeChoice().getAuditableEventQuery();
            
            if (auditableEventQuery != null) {
                AuditableEventQueryProcessor queryProcessor = new AuditableEventQueryProcessor();
                queryProcessor.setParentJoinColumn("sourceObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause, auditableEventQuery);
            }
        }
        else if (targetAssociationBranch != null) {
            auditableEventQuery = targetAssociationBranch.getAssociationBranchTypeChoice().getAuditableEventQuery();
            
            if (auditableEventQuery != null) {
                AuditableEventQueryProcessor queryProcessor = new AuditableEventQueryProcessor();
                queryProcessor.setParentJoinColumn("targetObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause, auditableEventQuery);
            }
        }
    }
    
    
    private void convertRegistryPackageQuery() throws RegistryException {
        
        RegistryPackageQuery registryPackageQuery = null;
        
        if (sourceAssociationBranch != null) {
            registryPackageQuery = sourceAssociationBranch.getAssociationBranchTypeChoice().getRegistryPackageQuery();
            
            if (registryPackageQuery != null) {
                RegistryPackageQueryProcessor queryProcessor = new RegistryPackageQueryProcessor();
                queryProcessor.setParentJoinColumn("sourceObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause, registryPackageQuery);
            }
        }
        else if (targetAssociationBranch != null) {
            registryPackageQuery = targetAssociationBranch.getAssociationBranchTypeChoice().getRegistryPackageQuery();
            
            if (registryPackageQuery != null) {
                RegistryPackageQueryProcessor queryProcessor = new RegistryPackageQueryProcessor();
                queryProcessor.setParentJoinColumn("targetObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause, registryPackageQuery);
            }
        }
    }
    
    
    private void convertExtrinsicObjectQuery() throws RegistryException {
        
        ExtrinsicObjectQuery extrinsicObjectQuery = null;
        
        if (sourceAssociationBranch != null) {
            extrinsicObjectQuery = sourceAssociationBranch.getAssociationBranchTypeChoice().getExtrinsicObjectQuery();
            
            if (extrinsicObjectQuery != null) {
                ExtrinsicObjectQueryProcessor queryProcessor = new ExtrinsicObjectQueryProcessor();
                queryProcessor.setParentJoinColumn("sourceObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause, extrinsicObjectQuery);
            }
        }
        else if (targetAssociationBranch != null) {
            extrinsicObjectQuery = targetAssociationBranch.getAssociationBranchTypeChoice().getExtrinsicObjectQuery();
            
            if (extrinsicObjectQuery != null) {
                ExtrinsicObjectQueryProcessor queryProcessor = new ExtrinsicObjectQueryProcessor();
                queryProcessor.setParentJoinColumn("targetObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause, extrinsicObjectQuery);
            }
        }
    }
    
    
    private void convertServiceQuery() throws RegistryException {
        
        ServiceQuery serviceQuery = null;
        
        if (sourceAssociationBranch != null) {
            serviceQuery = sourceAssociationBranch.getAssociationBranchTypeChoice().getServiceQuery();
            
            if (serviceQuery != null) {
                ServiceQueryProcessor queryProcessor = new ServiceQueryProcessor();
                queryProcessor.setParentJoinColumn("sourceObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause, serviceQuery);
            }
        }
        else if (targetAssociationBranch != null) {
            serviceQuery = targetAssociationBranch.getAssociationBranchTypeChoice().getServiceQuery();
            
            if (serviceQuery != null) {
                ServiceQueryProcessor queryProcessor = new ServiceQueryProcessor();
                queryProcessor.setParentJoinColumn("targetObject");
                queryProcessor.setSelectColumn("id");
                whereClause = queryProcessor.addWhereClause(whereClause, serviceQuery);
            }
        }
    }
    
    
    private void convertUserBranch() throws RegistryException {
        
        UserBranch userBranch = null;
        
        if (sourceAssociationBranch != null) {
            userBranch = sourceAssociationBranch.getAssociationBranchTypeChoice().getUserBranch();
            
            if (userBranch != null) {
                UserBranchProcessor branchProcessor = new UserBranchProcessor();
                branchProcessor.setParentJoinColumn("sourceObject");
                branchProcessor.setSelectColumn("id");
                whereClause = branchProcessor.addWhereClause(whereClause, userBranch);
            }
        }
        else if (targetAssociationBranch != null) {
            userBranch = targetAssociationBranch.getAssociationBranchTypeChoice().getUserBranch();
            
            if (userBranch != null) {
                UserBranchProcessor branchProcessor = new UserBranchProcessor();
                branchProcessor.setParentJoinColumn("targetObject");
                branchProcessor.setSelectColumn("id");
                whereClause = branchProcessor.addWhereClause(whereClause, userBranch);
            }
        }
    }
    
    
    private void convertServiceBindingBranch() throws RegistryException {
        
        ServiceBindingBranch serviceBindingBranch = null;
        
        if (sourceAssociationBranch != null) {
            serviceBindingBranch = sourceAssociationBranch.getAssociationBranchTypeChoice().getServiceBindingBranch();
            
            if (serviceBindingBranch != null) {
                ServiceBindingBranchProcessor branchProcessor = new ServiceBindingBranchProcessor();
                branchProcessor.setParentJoinColumn("sourceObject");
                branchProcessor.setSelectColumn("id");
                whereClause = branchProcessor.addWhereClause(whereClause, serviceBindingBranch);
            }
        }
        else if (targetAssociationBranch != null) {
            serviceBindingBranch = targetAssociationBranch.getAssociationBranchTypeChoice().getServiceBindingBranch();
            
            if (serviceBindingBranch != null) {
                ServiceBindingBranchProcessor branchProcessor = new ServiceBindingBranchProcessor();
                branchProcessor.setParentJoinColumn("targetObject");
                branchProcessor.setSelectColumn("id");
                whereClause = branchProcessor.addWhereClause(whereClause, serviceBindingBranch);
            }
        }
    }
    
    
    private void convertSpecificationLinkBranch() throws RegistryException {
        
        SpecificationLinkBranch specificationLinkBranch = null;
        
        if (sourceAssociationBranch != null) {
            specificationLinkBranch = sourceAssociationBranch.getAssociationBranchTypeChoice().getSpecificationLinkBranch();
            
            if (specificationLinkBranch != null) {
                SpecificationLinkBranchProcessor branchProcessor = new SpecificationLinkBranchProcessor();
                branchProcessor.setParentJoinColumn("sourceObject");
                branchProcessor.setSelectColumn("id");
                whereClause = branchProcessor.addWhereClause(whereClause, specificationLinkBranch);
            }
        }
        else if (targetAssociationBranch != null) {
            specificationLinkBranch = targetAssociationBranch.getAssociationBranchTypeChoice().getSpecificationLinkBranch();
            
            if (specificationLinkBranch != null) {
                SpecificationLinkBranchProcessor branchProcessor = new SpecificationLinkBranchProcessor();
                branchProcessor.setParentJoinColumn("targetObject");
                branchProcessor.setSelectColumn("id");
                whereClause = branchProcessor.addWhereClause(whereClause, specificationLinkBranch);
            }
        }
    }
}