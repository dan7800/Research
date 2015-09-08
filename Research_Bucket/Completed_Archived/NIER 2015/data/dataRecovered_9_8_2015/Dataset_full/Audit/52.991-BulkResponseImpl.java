/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/client/xml/registry/BulkResponseImpl.java,v 1.13 2004/03/24 17:21:50 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.FindException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryException;
import javax.xml.registry.infomodel.RegistryObject;

import org.freebxml.omar.client.xml.registry.infomodel.AssociationImpl;
import org.freebxml.omar.client.xml.registry.infomodel.AuditableEventImpl;
import org.freebxml.omar.client.xml.registry.infomodel.ClassificationImpl;
import org.freebxml.omar.client.xml.registry.infomodel.ClassificationSchemeImpl;
import org.freebxml.omar.client.xml.registry.infomodel.ConceptImpl;
import org.freebxml.omar.client.xml.registry.infomodel.ExternalIdentifierImpl;
import org.freebxml.omar.client.xml.registry.infomodel.ExternalLinkImpl;
import org.freebxml.omar.client.xml.registry.infomodel.ExtrinsicObjectImpl;
import org.freebxml.omar.client.xml.registry.infomodel.OrganizationImpl;
import org.freebxml.omar.client.xml.registry.infomodel.RegistryPackageImpl;
import org.freebxml.omar.client.xml.registry.infomodel.ServiceBindingImpl;
import org.freebxml.omar.client.xml.registry.infomodel.ServiceImpl;
import org.freebxml.omar.client.xml.registry.infomodel.SpecificationLinkImpl;
import org.freebxml.omar.client.xml.registry.infomodel.UserImpl;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponseType;
import org.oasis.ebxml.registry.bindings.query.FilterQueryResultType;
import org.oasis.ebxml.registry.bindings.query.GetContentResponseType;
import org.oasis.ebxml.registry.bindings.rim.AssociationType1;
import org.oasis.ebxml.registry.bindings.rim.AuditableEventType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationNodeType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationSchemeType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationType;
import org.oasis.ebxml.registry.bindings.rim.ExternalIdentifierType;
import org.oasis.ebxml.registry.bindings.rim.ExternalLinkType;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;
import org.oasis.ebxml.registry.bindings.rim.OrganizationType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;
import org.oasis.ebxml.registry.bindings.rim.RegistryPackageType;
import org.oasis.ebxml.registry.bindings.rim.ServiceBindingType;
import org.oasis.ebxml.registry.bindings.rim.ServiceType;
import org.oasis.ebxml.registry.bindings.rim.SpecificationLinkType;
import org.oasis.ebxml.registry.bindings.rim.UserType;
import org.oasis.ebxml.registry.bindings.rs.RegistryErrorListType;
import org.oasis.ebxml.registry.bindings.rs.RegistryErrorType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;
import org.oasis.ebxml.registry.bindings.rs.Status;


/**
 * Class Declaration for Class1
 *
 */
public class BulkResponseImpl implements BulkResponse {
    String requestId = null;
    private int status;
    private ArrayList collection = new ArrayList();
    private ArrayList registryExceptions;
    private RegistryResponseType ebResponse = null;

    /**
     * Construct an empty successful BulkResponse
     */
    BulkResponseImpl() throws JAXRException {
        status = STATUS_SUCCESS;
    }

    /**
     * Note: BulkResponseImpl is not an infomodel object even though this
     * constructor looks like constructors in the infomodel subpackage.
     * Therefore, the LifeCycleManagerImpl argument is not stored.
     */
    public BulkResponseImpl(LifeCycleManagerImpl lcm,
        RegistryResponseType ebResponse, HashMap responseAttachments)
        throws JAXRException {
        requestId = lcm.createId();

        this.ebResponse = ebResponse;
        Status ebStatus = ebResponse.getStatus();

        if (ebStatus.equals(Status.SUCCESS)) {
            status = STATUS_SUCCESS;
        } else if (ebStatus.equals(Status.UNAVAILABLE)) {
            status = STATUS_UNAVAILABLE;
        } else {
            status = STATUS_FAILURE;
        }

        if (ebResponse instanceof AdhocQueryResponseType) {
            AdhocQueryResponseType aqr = (AdhocQueryResponseType) ebResponse;
            RegistryObjectListType sqlResult = aqr.getSQLQueryResult();

            if (sqlResult != null) {
                processSqlQueryResult(sqlResult, lcm);
            } else {
                // assert(filterResult != null);
                // B/c at this point Castor enforces this fact via
                // validation
                FilterQueryResultType filterResult = aqr.getFilterQueryResult();
                processFilterQueryResult(filterResult, lcm);
            }
        } else if (ebResponse instanceof GetContentResponseType) {
            GetContentResponseType gcr = (GetContentResponseType) ebResponse;
            processGetContentResponse(responseAttachments, lcm);
        }

        RegistryErrorListType errList = ebResponse.getRegistryErrorList();

        if (errList != null) {
            List errs = errList.getRegistryError();
            Iterator iter = errs.iterator();

            while (iter.hasNext()) {
                Object obj = iter.next();
                RegistryErrorType error = (RegistryErrorType) obj;

                // XXX Need to add additional error info to exception somehow
                addRegistryException(new FindException(error.getValue()));
            }

            // XXX What to do about optional highestSeverity attr???
            //             errList.getHighestSeverity();
        }
    }

    /**
     * Get the Collection of of objects returned as a response of a
     * bulk operation.
     * Caller thread will block here if result is not yet available.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     */
    public Collection getCollection() throws JAXRException {
        return collection;
    }

    /**
     * Sets the Collection of objects returned for the response
     * Package protected access meant to be called only by provider impl.
     *
     */
    void setCollection(Collection c) {
        collection.clear();
        collection.addAll(c);
    }

    /**
     * Get the JAXRException(s) Collection in case of partial commit.
     * Caller thread will block here if result is not yet available.
     * Return null if result is available and there is no JAXRException(s).
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     */
    public Collection getExceptions() throws JAXRException {
        return registryExceptions;
    }

    /**
     * Returns true if the reponse is a partial response due to large result set
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     */
    public boolean isPartialResponse() throws JAXRException {
        // Write your code here
        return false;
    }

    /**
     * Returns the unique id for the request that generated this response.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     */
    public String getRequestId() throws JAXRException {
        // Write your code here
        return requestId;
    }

    /**
     * Returns the status for this response.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     */
    public int getStatus() throws JAXRException {
        return status;
    }

    void setStatus(int status) throws JAXRException {
        this.status = status;
    }

    /**
     * Returns true if a response is available, false otherwise.
     * This is a polling method and must not block.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     */
    public boolean isAvailable() throws JAXRException {
        //?? stub
        return true;
    }

    void addExceptions(Collection exes) {
        if (registryExceptions == null) {
            registryExceptions = new ArrayList();
        }

        registryExceptions.addAll(exes);
    }

    private void addRegistryException(RegistryException rex) {
        if (registryExceptions == null) {
            registryExceptions = new ArrayList();
        }

        registryExceptions.add(rex);
    }

    private void processSqlQueryResult(RegistryObjectListType sqlResult,
        LifeCycleManagerImpl lcm) throws JAXRException {
        ObjectCache objCache = ((RegistryServiceImpl) (lcm.getRegistryService())).getObjectCache();
        List items = sqlResult.getIdentifiable();
        Iterator iter = items.iterator();

        while (iter.hasNext()) {
            IdentifiableType obj = (IdentifiableType) iter.next();

            if (obj instanceof ClassificationSchemeType) {
                ClassificationSchemeImpl scheme = new ClassificationSchemeImpl(lcm,
                        (ClassificationSchemeType) obj);
                objCache.putRegistryObject(scheme);
                collection.add(scheme);

                continue;
            } else if (obj instanceof ClassificationType) {
                ClassificationImpl cls = new ClassificationImpl(lcm,
                        (ClassificationType) obj, null);
                objCache.putRegistryObject(cls);
                collection.add(cls);

                continue;
            } else if (obj instanceof OrganizationType) {
                OrganizationImpl org = new OrganizationImpl(lcm,
                        (OrganizationType) obj);
                objCache.putRegistryObject(org);
                collection.add(org);

                continue;
            } else if (obj instanceof AssociationType1) {
                AssociationImpl ass = new AssociationImpl(lcm,
                        (AssociationType1) obj);
                objCache.putRegistryObject(ass);
                collection.add(ass);

                continue;
            } else if (obj instanceof RegistryPackageType) {
                RegistryPackageImpl pkg = new RegistryPackageImpl(lcm,
                        (RegistryPackageType) obj);
                objCache.putRegistryObject(pkg);
                collection.add(pkg);

                continue;
            } else if (obj instanceof ExternalLinkType) {
                ExternalLinkImpl extLink = new ExternalLinkImpl(lcm,
                        (ExternalLinkType) obj);
                objCache.putRegistryObject(extLink);
                collection.add(extLink);

                continue;
            } else if (obj instanceof ExternalLinkType) {
                //??Need to pass parent object and not null
                ExternalIdentifierImpl extIdentifier = new ExternalIdentifierImpl(lcm,
                        (ExternalIdentifierType) obj, null);
                objCache.putRegistryObject(extIdentifier);
                collection.add(extIdentifier);

                continue;
            } else if (obj instanceof ExtrinsicObjectType) {
                ExtrinsicObjectImpl extrinsicObj = new ExtrinsicObjectImpl(lcm,
                        (ExtrinsicObjectType) obj);
                objCache.putRegistryObject(extrinsicObj);
                collection.add(extrinsicObj);

                continue;
            } else if (obj instanceof ServiceType) {
                ServiceImpl service = new ServiceImpl(lcm, (ServiceType) obj);
                objCache.putRegistryObject(service);
                collection.add(service);

                continue;
            } else if (obj instanceof ServiceBindingType) {
                ServiceBindingImpl binding = new ServiceBindingImpl(lcm,
                        (ServiceBindingType) obj);
                objCache.putRegistryObject(binding);
                collection.add(binding);

                continue;
            } else if (obj instanceof SpecificationLinkType) {
                SpecificationLinkImpl specLink = new SpecificationLinkImpl(lcm,
                        (SpecificationLinkType) obj);
                objCache.putRegistryObject(specLink);
                collection.add(specLink);

                continue;
            } else if (obj instanceof ClassificationNodeType) {
                ConceptImpl concept = new ConceptImpl(lcm,
                        (ClassificationNodeType) obj);
                objCache.putRegistryObject(concept);
                collection.add(concept);

                continue;
            } else if (obj instanceof ObjectRefType) {
                // ObjectRef-s are processed by leaf components
                continue;
            } else if (obj instanceof AuditableEventType) {
                AuditableEventImpl ae = new AuditableEventImpl(lcm,
                        (AuditableEventType) obj);
                objCache.putRegistryObject(ae);
                collection.add(ae);

                continue;
            } else if (obj instanceof UserType) {
                UserImpl user = new UserImpl(lcm, (UserType) obj);
                objCache.putRegistryObject(user);
                collection.add(user);

                continue;
            }

            System.err.println("Not implemented " + sqlResult);

            //throw new JAXRException("Not Yet Implemented");
        }
    }

    private void processFilterQueryResult(FilterQueryResultType filterResult,
        LifeCycleManagerImpl lcm) throws JAXRException {
        throw new JAXRException("Only SQLQueryResult is implemented");
    }

    private void processGetContentResponse(HashMap attachments,
        LifeCycleManagerImpl lcm) throws JAXRException {
        if (attachments == null) {
            // Possible if request contains no ObjectRef-s, but not typical
            return;
        }

        ObjectCache objCache = ((RegistryServiceImpl) (lcm.getRegistryService())).getObjectCache();

        for (Iterator it = attachments.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            String contentId = (String) entry.getKey();
            ExtrinsicObjectImpl extrinsicObj = (ExtrinsicObjectImpl) objCache.getReference(contentId,
                    "ExtrinsicObject").get();
            DataHandler dh = (DataHandler) entry.getValue();
            extrinsicObj.setRepositoryItem(dh);
            collection.add(extrinsicObj);
        }
    }

    RegistryObject getRegistryObject() throws JAXRException {
        RegistryObject ro = null;

        // check for errors
        Collection exceptions = getExceptions();

        if (exceptions != null) {
            Iterator iter = exceptions.iterator();
            Exception exception = null;

            while (iter.hasNext()) {
                exception = (Exception) iter.next();
                throw new JAXRException(exception);
            }
        }

        Collection results = getCollection();
        Iterator iter = results.iterator();

        if (iter.hasNext()) {
            ro = (RegistryObject) iter.next();
        }

        return ro;
    }
    
    public RegistryResponseType getRegistryResponse() {
        return ebResponse;
    }
}
/*
 * $Header: /cvsroot/sino/jaxr/src/com/sun/xml/registry/ebxml/BulkResponseImpl.java,v 1.16 2003/07/23 01:14:51 farrukh_najmi Exp $
 *
 *
 */

package com.sun.xml.registry.ebxml;

import javax.activation.DataHandler;
import javax.xml.registry.*;
import java.util.*;

import org.oasis.ebxml.registry.bindings.rs.*;
import org.oasis.ebxml.registry.bindings.rs.types.*;
import org.oasis.ebxml.registry.bindings.query.*;
import org.oasis.ebxml.registry.bindings.rim.ClassificationNodeType;
import org.oasis.ebxml.registry.bindings.rim.AuditableEventType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationSchemeType;
import org.oasis.ebxml.registry.bindings.rim.LeafRegistryObjectListTypeItem;
import org.oasis.ebxml.registry.bindings.rim.OrganizationType;
import org.oasis.ebxml.registry.bindings.rim.AssociationType1;
import org.oasis.ebxml.registry.bindings.rim.RegistryPackage;
import org.oasis.ebxml.registry.bindings.rim.ExternalLink;
import org.oasis.ebxml.registry.bindings.rim.ExternalIdentifier;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObject;
import org.oasis.ebxml.registry.bindings.rim.Service;
import org.oasis.ebxml.registry.bindings.rim.ServiceBinding;
import org.oasis.ebxml.registry.bindings.rim.SpecificationLink;
import org.oasis.ebxml.registry.bindings.rim.UserType;

import com.sun.xml.registry.ebxml.infomodel.*;
import javax.xml.registry.infomodel.*;

/**
 * Class Declaration for Class1
 *
 */
public class BulkResponseImpl implements BulkResponse {
    String requestId = null;
    private int status;
    private ArrayList collection = new ArrayList();
    private ArrayList registryExceptions;

    /**
     * Construct an empty successful BulkResponse
     */
    BulkResponseImpl() throws JAXRException {
        status = STATUS_SUCCESS;
    }

    /**
     * Note: BulkResponseImpl is not an infomodel object even though this
     * constructor looks like constructors in the infomodel subpackage.
     * Therefore, the LifeCycleManagerImpl argument is not stored.
     */
    public BulkResponseImpl(LifeCycleManagerImpl lcm,
                            RegistryResponse ebResponse,
                            HashMap responseAttachments)
        throws JAXRException
    {
        requestId = lcm.createId();
        
        StatusType statusType = ebResponse.getStatus();
        if (statusType == StatusType.SUCCESS) {
            status = STATUS_SUCCESS;
        } else if (statusType == StatusType.UNAVAILABLE) {
            status = STATUS_UNAVAILABLE;
        } else {
            status = STATUS_FAILURE;
        }

        RegistryResponseChoice rrc = ebResponse.getRegistryResponseChoice();
        if (rrc != null) {
            AdhocQueryResponse aqr = rrc.getAdhocQueryResponse();
            if (aqr != null) {
                SQLQueryResult sqlResult = aqr.getSQLQueryResult();
                if (sqlResult != null) {
                    processSqlQueryResult(sqlResult, lcm);
                } else {
                    // assert(filterResult != null);
                    // B/c at this point Castor enforces this fact via
                    // validation
                    FilterQueryResult filterResult = aqr.getFilterQueryResult();
                    processFilterQueryResult(filterResult, lcm);
                }
            } else {
                GetContentResponse gcr = rrc.getGetContentResponse();
                processGetContentResponse(responseAttachments, lcm);
            }
        }

        RegistryErrorList errList = ebResponse.getRegistryErrorList();
        if (errList != null) {
            RegistryError[] errs = errList.getRegistryError();
            for (int i = 0; i < errs.length; i++) {
                // XXX Need to add additional error info to exception somehow
                addRegistryException(new FindException(errs[i].getContent()));
            }
            // XXX What to do about optional highestSeverity attr???
//             errList.getHighestSeverity();
        }
    }

    /**
     * Get the Collection of of objects returned as a response of a
     * bulk operation.
     * Caller thread will block here if result is not yet available.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     */
    public Collection getCollection() throws JAXRException{
        return collection;
    }

    /**
     * Sets the Collection of objects returned for the response
     * Package protected access meant to be called only by provider impl.
     *
     */
    void setCollection(Collection c) {
        collection.clear();
        collection.addAll(c);
    }

    /**
     * Get the JAXRException(s) Collection in case of partial commit.
     * Caller thread will block here if result is not yet available.
     * Return null if result is available and there is no JAXRException(s).
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     */
    public Collection getExceptions() throws JAXRException {
        return registryExceptions;
    }

    /**
     * Returns true if the reponse is a partial response due to large result set
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     */
    public boolean isPartialResponse() throws JAXRException{
        // Write your code here
        return false;
    }

    /**
     * Returns the unique id for the request that generated this response.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     */
    public String getRequestId() throws JAXRException{
        // Write your code here
        return requestId;
    }

    /**
     * Returns the status for this response.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     */
    public int getStatus() throws JAXRException {
        return status;
    }

    void setStatus(int status) throws JAXRException {
        this.status = status;
    }

    /**
     * Returns true if a response is available, false otherwise.
     * This is a polling method and must not block.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     */
    public boolean isAvailable() throws JAXRException{
        //?? stub
        return true;
    }

    void addExceptions(Collection exes) {
        if (registryExceptions == null) {
            registryExceptions = new ArrayList();
        }
        registryExceptions.addAll(exes);
    }

    private void addRegistryException(RegistryException rex) {
        if (registryExceptions == null) {
            registryExceptions = new ArrayList();
        }
        registryExceptions.add(rex);
    }

    private void processSqlQueryResult(SQLQueryResult sqlResult,
                                       LifeCycleManagerImpl lcm)
        throws JAXRException
    {
        ObjectCache objCache =
            ((RegistryServiceImpl)(lcm.getRegistryService())).getObjectCache();
        LeafRegistryObjectListTypeItem[] items
            = sqlResult.getLeafRegistryObjectListTypeItem();
        for (int i = 0; i < items.length; i++) {
            ClassificationSchemeType ebScheme
                = items[i].getClassificationScheme();
            if (ebScheme != null) {
                ClassificationSchemeImpl scheme
                    = new ClassificationSchemeImpl(lcm, ebScheme);
                objCache.putRegistryObject(scheme);
                collection.add(scheme);
                continue;
            }

            ClassificationType ebClass = items[i].getClassification();
            if (ebClass != null) {
                ClassificationImpl cls = null;
                cls = new ClassificationImpl(lcm,ebClass,null);
                objCache.putRegistryObject(cls);
                collection.add(cls);
                continue;
            }
            
            OrganizationType ebOrg = items[i].getOrganization();
            if (ebOrg != null) {
                OrganizationImpl org = new OrganizationImpl(lcm, ebOrg);
                objCache.putRegistryObject(org);
                collection.add(org);
                continue;
            }

            AssociationType1 ebAss = items[i].getAssociation();
            if (ebAss != null) {
                AssociationImpl ass = new AssociationImpl(lcm, ebAss);
                objCache.putRegistryObject(ass);
                collection.add(ass);
                continue;
            }

            RegistryPackage ebPkg = items[i].getRegistryPackage();
            if (ebPkg != null) {
                RegistryPackageImpl pkg = new RegistryPackageImpl(lcm, ebPkg);
                objCache.putRegistryObject(pkg);
                collection.add(pkg);
                continue;
            }

            ExternalLink ebExtLink = items[i].getExternalLink();
            if (ebExtLink != null) {
                ExternalLinkImpl extLink = new ExternalLinkImpl(lcm, ebExtLink);
                objCache.putRegistryObject(extLink);
                collection.add(extLink);
                continue;
            }

            ExternalIdentifier ebExtIdentifier
                = items[i].getExternalIdentifier();
            if (ebExtIdentifier != null) {
                //??Need to pass parent object and not null
                ExternalIdentifierImpl extIdentifier
                    = new ExternalIdentifierImpl(lcm, ebExtIdentifier, null);
                objCache.putRegistryObject(extIdentifier);
                collection.add(extIdentifier);
                continue;
            }

            ExtrinsicObject ebExtrinsicObj = items[i].getExtrinsicObject();
            if (ebExtrinsicObj != null) {
                ExtrinsicObjectImpl extrinsicObj
                    = new ExtrinsicObjectImpl(lcm, ebExtrinsicObj);
                objCache.putRegistryObject(extrinsicObj);
                collection.add(extrinsicObj);
                continue;
            }

            Service ebService = items[i].getService();
            if (ebService != null) {
                ServiceImpl service
                    = new ServiceImpl(lcm, ebService);
                objCache.putRegistryObject(service);
                collection.add(service);
                continue;
            }

            ServiceBinding ebBinding = items[i].getServiceBinding();
            if (ebBinding != null) {
                ServiceBindingImpl binding
                    = new ServiceBindingImpl(lcm, ebBinding);
                objCache.putRegistryObject(binding);
                collection.add(binding);
                continue;
            }

            SpecificationLink ebSpecLink = items[i].getSpecificationLink();
            if (ebSpecLink != null) {
                SpecificationLinkImpl specLink
                    = new SpecificationLinkImpl(lcm, ebSpecLink);
                objCache.putRegistryObject(specLink);
                collection.add(specLink);
                continue;
            }

            ClassificationNodeType ebCNode = items[i].getClassificationNode();
            if (ebCNode != null) {
                ConceptImpl concept = new ConceptImpl(lcm, ebCNode);
                objCache.putRegistryObject(concept);
                collection.add(concept);
                continue;
            }

            if (items[i].getObjectRef() != null) {
                // ObjectRef-s are processed by leaf components
                continue;
            }

            AuditableEventType ebAE = items[i].getAuditableEvent();
            if (ebAE != null) {
                AuditableEventImpl ae = new AuditableEventImpl(lcm, ebAE);
                objCache.putRegistryObject(ae);
                collection.add(ae);
                continue;
            }

            UserType ebUser = items[i].getUser();
            if (ebUser != null) {
                UserImpl user = new UserImpl(lcm, ebUser);
                objCache.putRegistryObject(user);
                collection.add(user);
                continue;
            }

            System.err.println("Not implemented " + sqlResult);
            //throw new JAXRException("Not Yet Implemented");
        }
    }

    private void processFilterQueryResult(FilterQueryResult filterResult,
                                          LifeCycleManagerImpl lcm)
        throws JAXRException
    {
        throw new JAXRException("Only SQLQueryResult is implemented");

    }

    private void processGetContentResponse(HashMap attachments,
                                           LifeCycleManagerImpl lcm)
        throws JAXRException
    {
        if (attachments == null) {
            // Possible if request contains no ObjectRef-s, but not typical
            return;
        }

        ObjectCache objCache =
            ((RegistryServiceImpl)(lcm.getRegistryService())).getObjectCache();

        for (Iterator it = attachments.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry)it.next();
            String contentId = (String)entry.getKey();
            ExtrinsicObjectImpl extrinsicObj = (ExtrinsicObjectImpl)
                objCache.getReference(contentId, "ExtrinsicObject").get();
            DataHandler dh = (DataHandler)entry.getValue();
            extrinsicObj.setRepositoryItem(dh);
            collection.add(extrinsicObj);
        }
    }

    RegistryObject getRegistryObject() throws JAXRException {
        RegistryObject ro = null;

        // check for errors
        Collection exceptions = getExceptions();

        if (exceptions != null) {
            Iterator    iter = exceptions.iterator();
            Exception   exception = null;

            while (iter.hasNext()) {
                exception = (Exception) iter.next();
                throw new JAXRException(exception);
            }
        }

        Collection results = getCollection();
        Iterator iter = results.iterator();
        if (iter.hasNext()) {
            ro = (RegistryObject)iter.next();
        }

        return ro;
    }
}
