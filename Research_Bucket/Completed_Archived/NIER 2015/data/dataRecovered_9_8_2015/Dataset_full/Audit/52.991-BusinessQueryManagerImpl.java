/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/client/xml/registry/BusinessQueryManagerImpl.java,v 1.4 2003/11/12 03:36:30 farrukh_najmi Exp $
 * ====================================================================
 */
/*
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/client/xml/registry/BusinessQueryManagerImpl.java,v 1.4 2003/11/12 03:36:30 farrukh_najmi Exp $
 *
 *
 */
package org.freebxml.omar.client.xml.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.freebxml.omar.common.BindingUtility;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.BusinessQueryManager;
import javax.xml.registry.FindQualifier;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.Query;
import javax.xml.registry.UnexpectedObjectException;
import javax.xml.registry.infomodel.Classification;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.Key;
import javax.xml.registry.infomodel.User;


/**
 * Class Declaration for Class1
 */
public class BusinessQueryManagerImpl extends QueryManagerImpl
    implements BusinessQueryManager {
    private static final String WHERE_KEYWORD = "WHERE";
    private static final String PRIMARY_TABLE_NAME = "ptn";
    private static HashMap schemeNameToIdMap = new HashMap();

    static {
        schemeNameToIdMap.put("ObjectType",
            "urn:uuid:3188a449-18ac-41fb-be9f-99a1adca02cb");
        schemeNameToIdMap.put("PhoneType",
            "urn:uuid:de95a42e-a0e3-40a3-abcc-ee6d88492639");
        schemeNameToIdMap.put("AssociationType",
            "urn:uuid:6902675f-2f18-44b8-888b-c91db8b96b4d");
        schemeNameToIdMap.put("URLType",
            "urn:uuid:7817755e-8842-44b2-84f4-bf8a765619be"); //??Only needed for UDDI providers. Need to fix JAXR spec and TCK
        schemeNameToIdMap.put("PostalAddressAttributes", "ClassScheme"); //??Only needed for UDDI providers. Need to fix JAXR spec and TCK
    }

    org.freebxml.omar.client.xml.registry.util.QueryUtil qu = org.freebxml.omar.client.xml.registry.util.QueryUtil.getInstance();

    BusinessQueryManagerImpl(RegistryServiceImpl regService,
        BusinessLifeCycleManagerImpl lcm) throws JAXRException {
        super(regService, lcm,
            (DeclarativeQueryManagerImpl) (regService.getDeclarativeQueryManager()));
    }

    public BulkResponse findAssociations(Collection findQualifiers,
        String sourceObjectId, String targetObjectId,
        Collection associationTypes) throws JAXRException {
        String queryStr = "SELECT * FROM Association WHERE ";

        String andStr = "";

        if (sourceObjectId != null) {
            queryStr += (" (sourceObject = '" + sourceObjectId + "') ");
            andStr = " AND ";
        }

        if (targetObjectId != null) {
            queryStr += (andStr + " (targetObject = '" + targetObjectId +
            "') ");
            andStr = " AND ";
        }

        if (associationTypes != null) {
            /*
            ??Unimplemented
            queryStr += andStr + " (";

            Iterator iter = associationTypes.iterator();
            while (iter.hasNext()) {

            }
            */
        }

        Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);
        BulkResponse resp = dqm.executeQuery(query);

        return resp;
    }

    public BulkResponse findCallerAssociations(Collection findQualifiers,
        Boolean confirmedByCaller, Boolean confirmedByOtherParty,
        Collection associationTypes) throws JAXRException {
        // ??eeg ToDO implement findQualifiers and associationTypes
        if ((confirmedByCaller == null) && (confirmedByOtherParty == null)) {
            return new BulkResponseImpl();
        }

        User user = getCallersUser();
        String userId = user.getKey().getId();
        String qs = "SELECT * FROM Association a, AuditableEvent e WHERE " +
            "e.user = '" + userId + "' AND e.eventType = '" + BindingUtility.CANONICAL_EVENT_TYPE_Created + "'";

        if (confirmedByCaller != null) {
            String confirmedByCallerStr = confirmedByCaller.toString();
            qs += (" AND (e.registryObject = a.sourceObject AND " +
            "a.isConfirmedBySourceOwner = " + confirmedByCallerStr +
            " OR e.registryObject = a.targetObject AND " +
            "a.isConfirmedByTargetOwner = " + confirmedByCallerStr + ")");
        }

        if (confirmedByOtherParty != null) {
            String confirmedByOtherStr = confirmedByOtherParty.toString();
            qs += (" AND (e.registryObject = a.sourceObject AND " +
            "a.isConfirmedByTargetOwner = " + confirmedByOtherStr +
            " OR e.registryObject = a.targetObject AND " +
            "a.isConfirmedBySourceOwner = " + confirmedByOtherStr + ")");
        }

        Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, qs);

        return dqm.executeQuery(query);
    }

    //??JAXR 2.0
    public BulkResponse findObjects(String objectType,
        Collection findQualifiers, Collection namePatterns,
        Collection classifications, Collection specifications,
        Collection externalIdentifiers, Collection externalLinks)
        throws JAXRException {
        if (objectType.equals("Concept")) {
            objectType = "ClassificationNode";
        }

        Query query = createQueryByName(findQualifiers, objectType, namePatterns);
        query = addClassifications(query, classifications);

        return dqm.executeQuery(query);
    }

    /**
     * Finds all Organizations that match ALL of the criteria specified by
     * the parameters of this call.  This is a Logical AND operation
     * between all non-null parameters.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @return BulkResponse containing Collection of Organizations
     *
     */
    public BulkResponse findOrganizations(Collection findQualifiers,
        Collection namePatterns, Collection classifications,
        Collection specifications, Collection externalIdentifiers,
        Collection externalLinks) throws JAXRException {
        Query query = createQueryByName(findQualifiers, "Organization",
                namePatterns);
        query = addClassifications(query, classifications);

        return dqm.executeQuery(query);
    }

    /**
     * Finds all Services that match ALL of the criteria specified by the
     * parameters of this call.  This is a Logical AND operation between
     * all non-null parameters.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     *
     * @param orgKey Key identifying an Organization. Required for UDDI
     * providers.
     */
    public BulkResponse findServices(Key orgKey, Collection findQualifiers,
        Collection namePatterns, Collection classifications,
        Collection specifications) throws JAXRException {
        Query query = createQueryByName(findQualifiers, "Service", namePatterns);
        query = addClassifications(query, classifications);

        return dqm.executeQuery(query);
    }

    /**
     * Finds all ServiceBindings that match ALL of the criteria specified by the parameters of this call.
     * This is a Logical AND operation between all non-null parameters.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param serviceKey Key identifying a Service. Required for UDDI providers.
     *
     *
     * @return BulkResponse containing Collection of ServiceBindings
     */
    public BulkResponse findServiceBindings(Key serviceKey,
        Collection findQualifiers, Collection classifications,
        Collection specifications) throws JAXRException {
        Query query = createQueryByName(findQualifiers, "ServiceBinding", null);
        query = addClassifications(query, classifications);

        return dqm.executeQuery(query);
    }

    /**
     * Finds all ClassificationSchemes that match ALL of the criteria
     * specified by the parameters of this call.  This is a Logical AND
     * operation between all non-null parameters.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @return BulkResponse containing Collection of ClassificationSchemes
     */
    public BulkResponse findClassificationSchemes(Collection findQualifiers,
        Collection namePatterns, Collection classifications,
        Collection externalLinks) throws JAXRException {
        Query query = createQueryByName(findQualifiers, "ClassificationScheme",
                namePatterns);
        query = addClassifications(query, classifications);

        return dqm.executeQuery(query);
    }

    /**
     * Find a ClassificationScheme by name based on the specified name pattern.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param namePattern Is a String that is a partial or full
     * name pattern with wildcard searching as specified by the SQL-92 LIKE
     * specification.
     *
     * @return The ClassificationScheme matching the namePattern. If none match
     * return null. If multiple match then throw an InvalidRequestException.
     *
     */
    public ClassificationScheme findClassificationSchemeByName(
        Collection findQualifiers, String namePattern)
        throws JAXRException {
        Collection namePatterns = new ArrayList();
        namePatterns.add(namePattern);

        Query query = createQueryByName(findQualifiers, "ClassificationScheme",
                namePatterns);
        BulkResponse br = dqm.executeQuery(query);

        Iterator i = br.getCollection().iterator();
        ClassificationScheme cs = null;

        if (i.hasNext()) {
            cs = (ClassificationScheme) i.next();
        }

        // needs to check if more then 1 return and raise InvalidRequestException
        if (i.hasNext()) {
            throw new InvalidRequestException(
                "Error: findClassificationSchemeByName call cannot match more than one ClassificationScheme");
        }

        return cs;
    }

    /**
     * Finds all Concepts that match ALL of the criteria specified by the
     * parameters of this call.  This is a Logical AND operation between
     * all non-null parameters.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param findQualifier specifies qualifiers that effect string
     * matching, sorting etc.
     *
     *
     * @return BulkResponse containing Collection of Concepts
     */
    public BulkResponse findConcepts(Collection findQualifiers,
        Collection namePatterns, Collection classifications,
        Collection externalIdentifiers, Collection externalLinks)
        throws JAXRException {
        Query query = createQueryByName(findQualifiers, "ClassificationNode",
                namePatterns);
        query = addClassifications(query, classifications);

        return dqm.executeQuery(query);
    }

    /**
     * Find a Concept based on the path specified.
     * If specified path matches more than one ClassificationScheme then
     * the one that is most general (higher in the concept hierarchy) is returned.
     *
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param path Is a canonical path expression as defined in the JAXR specification that identifies the Concept.
     *
     */
    public Concept findConceptByPath(String path) throws JAXRException {
        //Kludge to work around JAXR 1.0 spec wierdness
        path = fixConceptPathForEbXML(path);

        String likeOrEqual = "=";

        if (path.indexOf('%') != -1) {
            likeOrEqual = "LIKE";
        }

        Query query = dqm.createQuery(Query.QUERY_TYPE_SQL,
                "SELECT * from ClassificationNode WHERE path " + likeOrEqual +
                " '" + path + "'");
        BulkResponse resp = dqm.executeQuery(query);

        return (Concept) (((BulkResponseImpl) resp).getRegistryObject());
    }

    /**
     * Handles a quirk of theJAXR spec. Fix in JAXR 2.0 spec??
     * Replace schemeName with schemeId.
     * Prefix value with wild card to account for fact that it may be in different place in ebXML Registry
     *
     */
    private String fixConceptPathForEbXML(String path) {
        String newPath = path;

        //Get the first element of the path.
        StringTokenizer st = new StringTokenizer(path, "/");
        int cnt = st.countTokens();

        //JAXR 1.0 assumes only a single level below root scheme
        if (cnt == 2) {
            String fistElem = st.nextToken();

            //Replace fistElem with schemeId if fistElem is a pre-defined concept 
            //name as defined in Appendix A of the JAXR specification.
            //Prefix value with wild card to account for fact that it may be in different place in ebXML Registry
            if (!(fistElem.startsWith("urn:"))) {
                String schemeId = (String) (schemeNameToIdMap.get(fistElem));

                if (schemeId != null) {
                    String value = st.nextToken();
                    newPath = "/" + schemeId + "%/" + value;
                }
            }
        }

        return newPath;
    }

    /**
     * Find all Concept that match the path specified. For JAXR 2.0??
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param path Is a canonical path expression as defined in the JAXR specification that identifies the Concept.
     *
     */
    public Collection findConceptsByPath(String path) throws JAXRException {
        String likeOrEqual = "=";

        if (path.indexOf('%') != -1) {
            likeOrEqual = "LIKE";
        }

        Query query = dqm.createQuery(Query.QUERY_TYPE_SQL,
                "SELECT * from ClassificationNode WHERE path " + likeOrEqual +
                " '" + path + "'");
        BulkResponse resp = dqm.executeQuery(query);

        return resp.getCollection();
    }

    /**
     * Finds all RegistryPackages that match ALL of the criteria specified by the parameters of this call.
     * This is a Logical AND operation between all non-null parameters.
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL>
     *
     *
     * @param findQualifier specifies qualifiers that effect string matching, sorting etc.
     *
     * @return BulkResponse containing Collection of RegistryPackages
     */
    public BulkResponse findRegistryPackages(Collection findQualifiers,
        Collection namePatterns, Collection classifications,
        Collection externalLinks) throws JAXRException {
        Query query = createQueryByName(findQualifiers, "RegistryPackage",
                namePatterns);
        query = addClassifications(query, classifications);

        return dqm.executeQuery(query);
    }

    static private String namePatternsToLikeExpr(Collection namePatterns,
        String term, boolean caseSensitive) {
        // XXX Assumes namePatterns are Strings
        if ((namePatterns == null) || (namePatterns.size() == 0)) {
            return null;
        } else if (namePatterns.size() == 1) {
            Object[] namesArray = namePatterns.toArray();

            if (namesArray[0].equals("%")) {
                return null;
            }
        }

        Iterator i = namePatterns.iterator();
        StringBuffer result = new StringBuffer("(" +
                caseSensitise(term, caseSensitive) + " LIKE " +
                caseSensitise("'" + (String) i.next() + "'", caseSensitive));

        while (i.hasNext()) {
            result.append(" OR " + caseSensitise(term, caseSensitive) +
                " LIKE " +
                caseSensitise("'" + (String) i.next() + "'", caseSensitive));
        }

        return result.append(")").toString();
    }

    public static String caseSensitise(String term, boolean caseSensitive) {
        String newTerm = term;

        if (!caseSensitive) {
            newTerm = "UPPER(" + term + ")";
        }

        return newTerm;
    }

    static private String classificationToConceptId(Object obj)
        throws JAXRException {
        if (!(obj instanceof Classification)) {
            throw new UnexpectedObjectException(
                "Expected Collection object type to be Classification");
        }

        Classification cl = (Classification) obj;

        if (cl.isExternal()) {
            throw new JAXRException(
                "External classification qualifiers not yet supported");
        }

        Concept concept = cl.getConcept();

        if (concept == null) {
            throw new JAXRException(
                "Concept of internal Classification is null");
        }

        return concept.getKey().getId();
    }

    private Query createQueryByName(Collection findQualifiers,
        String tableName, Collection namePatterns) throws JAXRException {
        boolean caseSensitive = false;

        if ((findQualifiers != null) &&
                (findQualifiers.contains(FindQualifier.CASE_SENSITIVE_MATCH))) {
            caseSensitive = true;
        }

        StringBuffer qs = new StringBuffer("SELECT * FROM " + tableName + " " +
                PRIMARY_TABLE_NAME);
        String likeExpr = namePatternsToLikeExpr(namePatterns, "n.value",
                caseSensitive);

        if (likeExpr != null) {
            qs.append(", Name n " + WHERE_KEYWORD + " " + likeExpr +
                " AND n.parent = " + PRIMARY_TABLE_NAME + ".id");
        }

        return dqm.createQuery(Query.QUERY_TYPE_SQL, qs.toString());
    }

    private Query addClassifications(Query query, Collection classifications)
        throws JAXRException {
        String q = query.toString();
        StringBuffer qs = new StringBuffer(q);
        String clExpr = qu.classificationsToPred(classifications,
                PRIMARY_TABLE_NAME + ".id");

        if (clExpr != null) {
            if (q.indexOf(WHERE_KEYWORD) != -1) {
                // where clause already created
                qs.append(" AND ");
            } else {
                qs.append(" " + WHERE_KEYWORD + " ");
            }

            qs.append(clExpr);
        } else {
            // No qualifiers are specified
        }

        return dqm.createQuery(Query.QUERY_TYPE_SQL, qs.toString());
    }
}
/*
 * $Header: /cvsroot/sino/jaxr/src/com/sun/xml/registry/ebxml/BusinessQueryManagerImpl.java,v 1.24 2003/08/13 16:59:19 farrukh_najmi Exp $
 *
 *
 */

package com.sun.xml.registry.ebxml;

import com.sun.xml.registry.ebxml.infomodel.*;

import javax.xml.registry.*;
import java.util.*;
import java.io.*;

import javax.xml.registry.infomodel.*;

/**
 * Class Declaration for Class1
 */
public class BusinessQueryManagerImpl extends QueryManagerImpl implements BusinessQueryManager {
    
    private static final String WHERE_KEYWORD = "WHERE";
    private static final String PRIMARY_TABLE_NAME = "ptn";
    
    com.sun.xml.registry.ebxml.util.QueryUtil qu = com.sun.xml.registry.ebxml.util.QueryUtil.getInstance();
    
    private static HashMap schemeNameToIdMap = new HashMap();

    static {        
        schemeNameToIdMap.put("ObjectType", "urn:uuid:3188a449-18ac-41fb-be9f-99a1adca02cb");
        schemeNameToIdMap.put("PhoneType", "urn:uuid:de95a42e-a0e3-40a3-abcc-ee6d88492639");
        schemeNameToIdMap.put("AssociationType", "urn:uuid:6902675f-2f18-44b8-888b-c91db8b96b4d");
        schemeNameToIdMap.put("URLType", "urn:uuid:7817755e-8842-44b2-84f4-bf8a765619be");    //??Only needed for UDDI providers. Need to fix JAXR spec and TCK
        schemeNameToIdMap.put("PostalAddressAttributes", "ClassScheme");    //??Only needed for UDDI providers. Need to fix JAXR spec and TCK
    }                        
    
    BusinessQueryManagerImpl(RegistryServiceImpl regService,
    BusinessLifeCycleManagerImpl lcm) throws JAXRException {
        
        super(regService, lcm, (DeclarativeQueryManagerImpl)(regService.getDeclarativeQueryManager()));
    }
    
    
    public BulkResponse findAssociations(Collection findQualifiers,
    String sourceObjectId,
    String targetObjectId,
    Collection associationTypes) throws JAXRException {
        String queryStr = "SELECT * FROM Association WHERE ";
        
        String andStr = "";
        if (sourceObjectId != null) {
            queryStr += " (sourceObject = '" + sourceObjectId + "') ";
            andStr = " AND ";
        }
        
        if (targetObjectId != null) {
            queryStr += andStr + " (targetObject = '" + targetObjectId + "') ";
            andStr = " AND ";
        }
        
        if (associationTypes != null) {
            /*
            ??Unimplemented
            queryStr += andStr + " (";
            
            Iterator iter = associationTypes.iterator();
            while (iter.hasNext()) {
                
            }
            */
        }
                        
        Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);
        BulkResponse resp = dqm.executeQuery(query);
        
        return resp;
    }

    public BulkResponse findCallerAssociations(
        Collection findQualifiers,
        Boolean confirmedByCaller,
        Boolean confirmedByOtherParty,
        Collection associationTypes) throws JAXRException
    {
        // ??eeg ToDO implement findQualifiers and associationTypes
        if (confirmedByCaller == null && confirmedByOtherParty == null) {
            return new BulkResponseImpl();
        }

        User user = getCallersUser();
        String userId = user.getKey().getId();
        String qs = "SELECT * FROM Association a, AuditableEvent e WHERE " +
            "e.user = '" + userId + "' AND e.eventType = Created";
        if (confirmedByCaller != null) {
            String confirmedByCallerStr = confirmedByCaller.toString();
            qs += " AND (e.registryObject = a.sourceObject AND " +
                "a.isConfirmedBySourceOwner = " + confirmedByCallerStr +
                " OR e.registryObject = a.targetObject AND " +
                "a.isConfirmedByTargetOwner = " + confirmedByCallerStr + ")";
        }
        if (confirmedByOtherParty != null) {
            String confirmedByOtherStr = confirmedByOtherParty.toString();
            qs += " AND (e.registryObject = a.sourceObject AND " +
                "a.isConfirmedByTargetOwner = " + confirmedByOtherStr +
                " OR e.registryObject = a.targetObject AND " +
                "a.isConfirmedBySourceOwner = " + confirmedByOtherStr + ")";
        }

        Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, qs);
        return dqm.executeQuery(query);
    }

    //??JAXR 2.0
    public BulkResponse findObjects(String objectType,
    Collection findQualifiers,
    Collection namePatterns,
    Collection classifications,
    Collection specifications,
    Collection externalIdentifiers,
    Collection externalLinks) throws JAXRException {
        
        if (objectType.equals("Concept")) {
            objectType = "ClassificationNode";
        }
        Query query = createQueryByName(findQualifiers, objectType, namePatterns);
        query = addClassifications(query, classifications);
        return dqm.executeQuery(query);
    }

    /**
     * Finds all Organizations that match ALL of the criteria specified by
     * the parameters of this call.  This is a Logical AND operation
     * between all non-null parameters.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @return BulkResponse containing Collection of Organizations
     *
     */
    public BulkResponse findOrganizations(Collection findQualifiers,
    Collection namePatterns,
    Collection classifications,
    Collection specifications,
    Collection externalIdentifiers,
    Collection externalLinks) throws JAXRException {
        
        Query query = createQueryByName(findQualifiers, "Organization", namePatterns);
        query = addClassifications(query, classifications);
        return dqm.executeQuery(query);
    }
    
    /**
     * Finds all Services that match ALL of the criteria specified by the
     * parameters of this call.  This is a Logical AND operation between
     * all non-null parameters.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     *
     * @param orgKey Key identifying an Organization. Required for UDDI
     * providers.
     */
    public BulkResponse findServices(Key orgKey,
    Collection findQualifiers,
    Collection namePatterns,
    Collection classifications,
    Collection specifications) throws JAXRException {
        
        Query query = createQueryByName(findQualifiers, "Service", namePatterns);
        query = addClassifications(query, classifications);
        return dqm.executeQuery(query);
    }
    
    /**
     * Finds all ServiceBindings that match ALL of the criteria specified by the parameters of this call.
     * This is a Logical AND operation between all non-null parameters.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param serviceKey Key identifying a Service. Required for UDDI providers.
     *
     *
     * @return BulkResponse containing Collection of ServiceBindings
     */
    public BulkResponse findServiceBindings(Key serviceKey,
    Collection findQualifiers,
    Collection classifications,
    Collection specifications) throws JAXRException {
        
        Query query = createQueryByName(findQualifiers, "ServiceBinding", null);
        query = addClassifications(query, classifications);
        return dqm.executeQuery(query);
    }
    
    /**
     * Finds all ClassificationSchemes that match ALL of the criteria
     * specified by the parameters of this call.  This is a Logical AND
     * operation between all non-null parameters.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @return BulkResponse containing Collection of ClassificationSchemes
     */
    public BulkResponse findClassificationSchemes(Collection findQualifiers,
    Collection namePatterns,
    Collection classifications,
    Collection externalLinks) throws JAXRException {
        
        Query query = createQueryByName(findQualifiers, "ClassificationScheme", namePatterns);
        query = addClassifications(query, classifications);
        return dqm.executeQuery(query);
    }
    
    /**
     * Find a ClassificationScheme by name based on the specified name pattern.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param namePattern Is a String that is a partial or full
     * name pattern with wildcard searching as specified by the SQL-92 LIKE
     * specification.
     *
     * @return The ClassificationScheme matching the namePattern. If none match
     * return null. If multiple match then throw an InvalidRequestException.
     *
     */
    public ClassificationScheme findClassificationSchemeByName(Collection findQualifiers,
    String namePattern) throws JAXRException {
        
        Collection namePatterns = new ArrayList();
        namePatterns.add(namePattern);
        Query query = createQueryByName(findQualifiers, "ClassificationScheme", namePatterns);
        BulkResponse br = dqm.executeQuery(query);
        
        Iterator i = br.getCollection().iterator();
        ClassificationScheme cs = null;
        if (i.hasNext()) {
            cs = (ClassificationScheme)i.next();
        }
        
        // needs to check if more then 1 return and raise InvalidRequestException
        if (i.hasNext()) {
            throw new InvalidRequestException("Error: findClassificationSchemeByName call cannot match more than one ClassificationScheme");
        }
        
        return cs;
    }
    
    /**
     * Finds all Concepts that match ALL of the criteria specified by the
     * parameters of this call.  This is a Logical AND operation between
     * all non-null parameters.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param findQualifier specifies qualifiers that effect string
     * matching, sorting etc.
     *
     *
     * @return BulkResponse containing Collection of Concepts
     */
    public BulkResponse findConcepts(Collection findQualifiers,
    Collection namePatterns,
    Collection classifications,
    Collection externalIdentifiers,
    Collection externalLinks) throws JAXRException {
        
        Query query = createQueryByName(findQualifiers, "ClassificationNode", namePatterns);
        query = addClassifications(query, classifications);
        return dqm.executeQuery(query);
    }
    
    /**
     * Find a Concept based on the path specified.
     * If specified path matches more than one ClassificationScheme then
     * the one that is most general (higher in the concept hierarchy) is returned.
     *
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param path Is a canonical path expression as defined in the JAXR specification that identifies the Concept.
     *
     */
    public Concept findConceptByPath(String path) throws JAXRException {
        //Kludge to work around JAXR 1.0 spec wierdness
        path = fixConceptPathForEbXML(path);

        String likeOrEqual = "=";
        if (path.indexOf('%') != -1) {
            likeOrEqual = "LIKE";
        }
        Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, "SELECT * from ClassificationNode WHERE path " + likeOrEqual + " '" + path + "'");
        BulkResponse resp = dqm.executeQuery(query);
        
        return (Concept)(((BulkResponseImpl)resp).getRegistryObject());
    }
    
    /**
     * Handles a quirk of theJAXR spec. Fix in JAXR 2.0 spec??
     * Replace schemeName with schemeId.
     * Prefix value with wild card to account for fact that it may be in different place in ebXML Registry
     *
     */
    private String fixConceptPathForEbXML(String path) {        
        String newPath = path;
        
        //Get the first element of the path.
        StringTokenizer st = new StringTokenizer(path, "/");
        int cnt = st.countTokens();
        
        
        //JAXR 1.0 assumes only a single level below root scheme
        if (cnt == 2) {
        
            String fistElem = st.nextToken();            

            //Replace fistElem with schemeId if fistElem is a pre-defined concept 
            //name as defined in Appendix A of the JAXR specification.
            //Prefix value with wild card to account for fact that it may be in different place in ebXML Registry
            if (!(fistElem.startsWith("urn:"))) {
                String schemeId = (String)(schemeNameToIdMap.get(fistElem));
                if (schemeId != null) {
                    String value = st.nextToken();            
                    newPath = "/" + schemeId + "%/" + value;
                }
            }
        }
                 
        return newPath;        
    }
    
    /**
     * Find all Concept that match the path specified. For JAXR 2.0??
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param path Is a canonical path expression as defined in the JAXR specification that identifies the Concept.
     *
     */
    public Collection findConceptsByPath(String path) throws JAXRException {
        String likeOrEqual = "=";
        if (path.indexOf('%') != -1) {
            likeOrEqual = "LIKE";
        }
        Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, "SELECT * from ClassificationNode WHERE path " + likeOrEqual + " '" + path + "'");
        BulkResponse resp = dqm.executeQuery(query);
        
        return resp.getCollection();
    }

    /**
     * Finds all RegistryPackages that match ALL of the criteria specified by the parameters of this call.
     * This is a Logical AND operation between all non-null parameters.
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL>
     *
     *
     * @param findQualifier specifies qualifiers that effect string matching, sorting etc.
     *
     * @return BulkResponse containing Collection of RegistryPackages
     */
    public BulkResponse findRegistryPackages(Collection findQualifiers,
    Collection namePatterns,
    Collection classifications,
    Collection externalLinks) throws JAXRException {
        
        Query query = createQueryByName(findQualifiers, "RegistryPackage", namePatterns);
        query = addClassifications(query, classifications);
        return dqm.executeQuery(query);
    }
    
    static private String namePatternsToLikeExpr(Collection namePatterns,
    String term, boolean caseSensitive) {
        // XXX Assumes namePatterns are Strings
        if (namePatterns == null || namePatterns.size() == 0) {
            return null;
        }
        else if (namePatterns.size() == 1) {
            Object[] namesArray = namePatterns.toArray();
            
            if (namesArray[0].equals("%")) {
                return null;
            }
        }
        Iterator i = namePatterns.iterator();
        StringBuffer result = new StringBuffer(
        "(" + caseSensitise(term, caseSensitive) + " LIKE " + caseSensitise("'"+ (String)i.next() + "'", caseSensitive));
        while (i.hasNext()) {
            result.append(" OR " + caseSensitise(term, caseSensitive) + " LIKE " + caseSensitise("'"+ (String)i.next() + "'", caseSensitive));
        }
        return result.append(")").toString();
    }
    
    public static String caseSensitise(String term, boolean caseSensitive) {
        String newTerm = term;
        
        if (!caseSensitive) {
            newTerm = "UPPER(" + term + ")";
        }
        
        return newTerm;
    }
    
    static private String classificationToConceptId(Object obj)
    throws JAXRException {
        if (!(obj instanceof Classification)) {
            throw new UnexpectedObjectException(
            "Expected Collection object type to be Classification");
        }
        Classification cl = (Classification)obj;
        if (cl.isExternal()) {
            throw new JAXRException(
            "External classification qualifiers not yet supported");
        }
        Concept concept = cl.getConcept();
        if (concept == null) {
            throw new JAXRException(
            "Concept of internal Classification is null");
        }
        return concept.getKey().getId();
    }        
    
    private Query createQueryByName(Collection findQualifiers, String tableName, Collection namePatterns) throws JAXRException {
        
        boolean caseSensitive = false;
        
        if ((findQualifiers != null) && (findQualifiers.contains(FindQualifier.CASE_SENSITIVE_MATCH))) {
            caseSensitive = true;
        }
        
        StringBuffer qs = new StringBuffer("SELECT * FROM " + tableName + " " + PRIMARY_TABLE_NAME);
        String likeExpr = namePatternsToLikeExpr(namePatterns, "n.value", caseSensitive);
        
        if (likeExpr != null) {
            qs.append(", Name n " + WHERE_KEYWORD + " " + likeExpr + " AND n.parent = " + PRIMARY_TABLE_NAME + ".id");
        }
        
        return dqm.createQuery(Query.QUERY_TYPE_SQL, qs.toString());
    }
    
    
    private Query addClassifications(Query query, Collection classifications) throws JAXRException {
        
        String q = query.toString();
        StringBuffer qs = new StringBuffer(q);
        String clExpr = qu.classificationsToPred(classifications, PRIMARY_TABLE_NAME + ".id");
        
        if (clExpr != null) {
            if (q.indexOf(WHERE_KEYWORD) != -1) {
                // where clause already created
                qs.append(" AND ");
            }
            else {
                qs.append(" " + WHERE_KEYWORD + " ");
            }
            qs.append(clExpr);
        }
        else {
            // No qualifiers are specified
        }
        
        return dqm.createQuery(Query.QUERY_TYPE_SQL, qs.toString());
    }
}
