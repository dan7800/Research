/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/common/BindingUtility.java,v 1.27 2004/03/31 19:33:24 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.oasis.ebxml.registry.bindings.lcm.AddSlotsRequest;
import org.oasis.ebxml.registry.bindings.lcm.ApproveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.DeprecateObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.RemoveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.RemoveSlotsRequest;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.UpdateObjectsRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.query.GetContentRequest;
import org.oasis.ebxml.registry.bindings.rim.ClassificationNodeType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationSchemeType;
import org.oasis.ebxml.registry.bindings.rim.Description;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.LocalizedString;
import org.oasis.ebxml.registry.bindings.rim.Name;
import org.oasis.ebxml.registry.bindings.rim.ObjectRef;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefList;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefListType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.RegistryPackageType;
import org.oasis.ebxml.registry.bindings.rim.ServiceBindingType;
import org.oasis.ebxml.registry.bindings.rim.ServiceType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;
import org.oasis.ebxml.registry.bindings.rs.Status;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;



/**
 * Utilities used with castor generated XML Data Binding objects.
 *
 * @author Farrukh S. Najmi
 * @version   1.2, 05/02/00
 */
public class BindingUtility {
    
    //Canonical asscoiation types codes
    public static final String CANONICAL_ASSOCIATION_TYPE_CODE_RELATED_TO = "RelatedTo";
    public static final String CANONICAL_ASSOCIATION_TYPE_CODE_HAS_FEDERATION_MEMBER = "HasFederationMember";
    public static final String CANONICAL_ASSOCIATION_TYPE_CODE_HAS_MEMBER = "HasMember";
    public static final String CANONICAL_ASSOCIATION_TYPE_CODE_EXTERNALLY_LINKS = "ExternallyLinks";
    public static final String CANONICAL_ASSOCIATION_TYPE_CODE_CONTAINS = "Contains";
    public static final String CANONICAL_ASSOCIATION_TYPE_CODE_EQUIVALENT_TO = "EquivalentTo";
    public static final String CANONICAL_ASSOCIATION_TYPE_CODE_EXTENDS = "Extends";
    public static final String CANONICAL_ASSOCIATION_TYPE_CODE_IMPLEMENTS = "Implements";
    public static final String CANONICAL_ASSOCIATION_TYPE_CODE_INSTANCE_OF = "InstanceOf";
    public static final String CANONICAL_ASSOCIATION_TYPE_CODE_SUPERSEDES = "Supersedes";
    public static final String CANONICAL_ASSOCIATION_TYPE_CODE_USES = "Uses";
    public static final String CANONICAL_ASSOCIATION_TYPE_CODE_REPLACES = "Replaces";
    public static final String CANONICAL_ASSOCIATION_TYPE_CODE_SUBMITTER_OF = "SubmitterOf";
    public static final String CANONICAL_ASSOCIATION_TYPE_CODE_RESPONSIBLE_FOR = "ResponsibleFor";
    public static final String CANONICAL_ASSOCIATION_TYPE_CODE_OFFERS_SERVICE = "OffersService";
    public static final String CANONICAL_ASSOCIATION_TYPE_CODE_CONTENT_MANAGEMENT_SERVICE_FOR = "ContentManagementServiceFor";
    public static final String CANONICAL_ASSOCIATION_TYPE_CODE_INVOCATION_CONTROL_FILE_FOR = "InvocationControlFileFor";
    
    //Canonical AssociationTypes ids
    public static final String CANONICAL_ASSOCIATION_TYPE_ID_RELATED_TO = "urn:uuid:7aad31a7-501e-4cbc-ac37-ddc44fdf3e1a";
    public static final String CANONICAL_ASSOCIATION_TYPE_ID_HAS_FEDERATION_MEMBER = "urn:uuid:13ee5ce0-0843-4153-8199-8a7e0b2ecef3";
    public static final String CANONICAL_ASSOCIATION_TYPE_ID_HAS_MEMBER = "urn:uuid:2d03bffb-f426-4830-8413-bab8537a995b";
    public static final String CANONICAL_ASSOCIATION_TYPE_ID_EXTERNALLY_LINKS = "urn:uuid:92d03292-84a0-4b86-8139-dd244173ddbb";
    public static final String CANONICAL_ASSOCIATION_TYPE_ID_CONTAINS = "urn:uuid:fc158238-96bd-41f4-8ac0-b8953c071b41";
    public static final String CANONICAL_ASSOCIATION_TYPE_ID_EQUIVALENT_TO = "urn:uuid:9e662e80-8b5c-48c3-b39b-995fd00c48e4";
    public static final String CANONICAL_ASSOCIATION_TYPE_ID_EXTENDS = "urn:uuid:7ca2c718-3bf6-4f6f-9570-c619e52c6443";
    public static final String CANONICAL_ASSOCIATION_TYPE_ID_IMPLEMENTS = "urn:uuid:6e12e663-e525-4518-8700-884ca6c5c6aa";
    public static final String CANONICAL_ASSOCIATION_TYPE_ID_INSTANCE_OF = "urn:uuid:4dbd5404-f720-44fe-8298-e1ba462c5f2d";
    public static final String CANONICAL_ASSOCIATION_TYPE_ID_SUPERSEDES = "urn:uuid:d6babbf7-7911-417e-8e6c-c8a7e6f6ada1";
    public static final String CANONICAL_ASSOCIATION_TYPE_ID_USES = "urn:uuid:fa0282a3-1b6c-41a3-aa2c-bbd1c432e1ec";
    public static final String CANONICAL_ASSOCIATION_TYPE_ID_REPLACES = "urn:uuid:efeda5e9-57ff-4f62-82f5-aa5be40385f9";
    public static final String CANONICAL_ASSOCIATION_TYPE_ID_SUBMITTER_OF = "urn:uuid:ed22135f-fab8-4492-8c85-b84a914f1038";
    public static final String CANONICAL_ASSOCIATION_TYPE_ID_RESPONSIBLE_FOR = "urn:uuid:124b974f-503e-4f40-9223-91e161631eb8";
    public static final String CANONICAL_ASSOCIATION_TYPE_ID_OFFERS_SERVICE = "urn:uuid:7942ea51-2b0b-4798-a15d-b9528e48342e";
    public static final String CANONICAL_ASSOCIATION_TYPE_ID_CONTENT_MANAGEMENT_SERVICE_FOR = "urn:uuid:7931e965-ac48-442a-93db-b5ca0c49929d";
    public static final String CANONICAL_ASSOCIATION_TYPE_ID_INVOCATION_CONTROL_FILE_FOR = "urn:uuid:261ab741-57f4-4fd5-86ee-a15311ec3213";

    
    //Canonical ObjectTypes ids
    public static final String CANONICAL_OBJECT_TYPE_ID_RegistryObject = "urn:uuid:a7ec3db9-9342-4016-820c-cff66c0bb021";
    public static final String CANONICAL_OBJECT_TYPE_ID_AdhocQuery = "urn:uuid:ccac6140-ce43-4d83-b067-f44d303f7c4c";
    //TODO: Make the next 2 canonical objectTypes  and assign ids.
    public static final String CANONICAL_OBJECT_TYPE_ID_SQLQuery = "urn:uuid:ccac6140-ce43-4d83-b067-f44d303f7c4c";
    public static final String CANONICAL_OBJECT_TYPE_ID_FilterQuery = "urn:uuid:ccac6140-ce43-4d83-b067-f44d303f7c4c";
    
    public static final String CANONICAL_OBJECT_TYPE_ID_Association = "urn:uuid:69399ff8-ca2c-4637-baf0-a157b2466b90";
    public static final String CANONICAL_OBJECT_TYPE_ID_AuditableEvent = "urn:uuid:1945f7e4-cf24-44fd-bbff-cd3d98e78674";
    public static final String CANONICAL_OBJECT_TYPE_ID_Classification = "urn:uuid:65e731a8-3325-4ac5-bd95-d71a277e3216";
    public static final String CANONICAL_OBJECT_TYPE_ID_ClassificationNode = "urn:uuid:247edbdb-31e8-40bc-97bd-fd60497deabb";
    public static final String CANONICAL_OBJECT_TYPE_ID_ExternalIdentifier = "urn:uuid:8280e1b1-84ac-4bca-a0ee-8d7ffe2f2333";
    public static final String CANONICAL_OBJECT_TYPE_ID_ExternalLink = "urn:uuid:9c442a04-8eb6-4595-82c4-d4f96d001409";
    public static final String CANONICAL_OBJECT_TYPE_ID_Organization = "urn:uuid:c7219bab-f78f-4340-b02a-e493617c6952";
    public static final String CANONICAL_OBJECT_TYPE_ID_ServiceBinding = "urn:uuid:3495faba-e699-411a-acfd-efecabc9ef48";
    public static final String CANONICAL_OBJECT_TYPE_ID_SpecificationLink = "urn:uuid:124b974f-503e-4f40-9223-91e161631eb8";
    public static final String CANONICAL_OBJECT_TYPE_ID_Subscription = "urn:uuid:a5fab058-55de-4c9b-9263-9c5de09112f1";
    public static final String CANONICAL_OBJECT_TYPE_ID_User = "urn:uuid:6d07b299-10e7-408f-843d-bb2bc913bfbb";
    public static final String CANONICAL_OBJECT_TYPE_ID_RegistryEntry = "urn:uuid:9d7a35fe-c10b-49c5-b22b-e6aa7c9d7034";
    public static final String CANONICAL_OBJECT_TYPE_ID_ClassificationScheme = "urn:uuid:c8b3dd77-9290-4fa3-a01a-94514d8f89ee";
    public static final String CANONICAL_OBJECT_TYPE_ID_Federation = "urn:uuid:e21b23a1-e34b-4fc1-b878-a73f5598c74b";
    public static final String CANONICAL_OBJECT_TYPE_ID_Registry = "urn:uuid:52b8100a-01ff-4e30-a326-8905e438ca4f";
    public static final String CANONICAL_OBJECT_TYPE_ID_RegistryPackage = "urn:uuid:ca61fbb7-80b2-40d3-95df-b0b9e2694c2a";
    public static final String CANONICAL_OBJECT_TYPE_ID_Service = "urn:uuid:52fc5536-c38f-4e89-b661-9664fa1f592f";
    public static final String CANONICAL_OBJECT_TYPE_ID_ExtrinsicObject = "urn:uuid:baa2e6c8-873e-4624-8f2d-b9c7230eb4f8";
    public static final String CANONICAL_OBJECT_TYPE_ID_xacml = "urn:uuid:3cbbccd3-a482-4dc2-8940-c6d182b3172a";
    public static final String CANONICAL_OBJECT_TYPE_ID_Policy = "urn:uuid:832d1ba8-2c56-4f29-a49d-d10b0bd8f920";
    public static final String CANONICAL_OBJECT_TYPE_ID_PolicySet = "urn:uuid:7c50351e-0022-4a65-950c-ac8cda6f3e0b";
    public static final String CANONICAL_OBJECT_TYPE_ID_xml = "urn:uuid:4a13dacb-a9e8-4819-bd1a-faf50976e5d4";
    public static final String CANONICAL_OBJECT_TYPE_ID_xslt = "urn:uuid:32bbb291-0291-486d-a80d-cdd6cd625c57";
    public static final String CANONICAL_OBJECT_TYPE_ID_xmlSchema = "urn:uuid:41c61205-3e19-46b5-b463-f8140090bd99";

    //Canonical eventTypes
    public static final String CANONICAL_EVENT_TYPE_Approved = "Approved";
    public static final String CANONICAL_EVENT_TYPE_Created = "Created";
    public static final String CANONICAL_EVENT_TYPE_Deleted = "Deleted";
    public static final String CANONICAL_EVENT_TYPE_Deprecated = "Deprecated";
    public static final String CANONICAL_EVENT_TYPE_Downloaded = "Downloaded";
    public static final String CANONICAL_EVENT_TYPE_Relocated = "Relocated";
    
    //TODO: spec issue: mising in RIM 2.5
    public static final String CANONICAL_EVENT_TYPE_Undeprecated = "Undeprecated";
    public static final String CANONICAL_EVENT_TYPE_Updated = "Updated";
    public static final String CANONICAL_EVENT_TYPE_Versioned = "Versioned";
    
    
    /**
     * @link
     * @shapeType PatternLink
     * @pattern Singleton
     * @supplierRole Singleton factory
     */

    /* # private BindingUtility _utility; */
    private static BindingUtility instance = null;
    public org.oasis.ebxml.registry.bindings.rim.ObjectFactory rimFac;
    public org.oasis.ebxml.registry.bindings.rs.ObjectFactory rsFac;
    public org.oasis.ebxml.registry.bindings.lcm.ObjectFactory lcmFac;
    public org.oasis.ebxml.registry.bindings.query.ObjectFactory queryFac;
    public org.oasis.ebxml.registry.bindings.cms.ObjectFactory cmsFac;
    JAXBContext jaxbContext = null;

    /**
     * Class Constructor. Protected and only used by getInstance()
     *
     */
    protected BindingUtility() {
        try {
            getJAXBContext();
            rimFac = new org.oasis.ebxml.registry.bindings.rim.ObjectFactory();
            rsFac = new org.oasis.ebxml.registry.bindings.rs.ObjectFactory();
            lcmFac = new org.oasis.ebxml.registry.bindings.lcm.ObjectFactory();
            queryFac = new org.oasis.ebxml.registry.bindings.query.ObjectFactory();
            cmsFac = new org.oasis.ebxml.registry.bindings.cms.ObjectFactory();
        } catch (JAXBException e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    public JAXBContext getJAXBContext() throws JAXBException {
        if (jaxbContext == null) {
            jaxbContext = JAXBContext.newInstance(
                    "org.oasis.ebxml.registry.bindings.rim:org.oasis.ebxml.registry.bindings.rs:org.oasis.ebxml.registry.bindings.lcm:org.oasis.ebxml.registry.bindings.query:org.oasis.ebxml.registry.bindings.cms");
            ;
        }

        return jaxbContext;
    }
    
    /*
     * Gets the id for the objectType for specified RIM class.
     *
     * @retun Return the canonical id for the ClassificationNode representing the 
     * objectType for specified RIM class. 
     */
    public String getObjectTypeId(String rimClassName) throws OMARException {
        String objectTypeId = null;
        
        try {
            Class clazz = this.getClass();
            Field field = clazz.getField("CANONICAL_OBJECT_TYPE_ID_" + rimClassName);
            Object obj = field.get(this);
            objectTypeId = (String)obj;
        }
        catch (NoSuchFieldException e) {
            throw new OMARException(e);
        }
        catch (SecurityException e) {
            throw new OMARException(e);
        }
        catch (IllegalAccessException e) {
            throw new OMARException(e);
        }
        
        return objectTypeId;
    }    

    /**
     * Get List of RegistryObject after filtering out ObjectRef from RegistryObjectList.
     * Does not get composed objects.
     */
    public List getRegistryObjectList(RegistryObjectListType objs) throws OMARException {
        List al = new ArrayList();

        if (objs != null) {
            List identifiables = objs.getIdentifiable();
            Iterator iter = identifiables.iterator();

            while (iter.hasNext()) {
                Object obj = iter.next();

                IdentifiableType identifiable = (IdentifiableType) obj;

                //Identifiable obj1 = (Identifiable)obj;                
                //IdentifiableType identifiable = obj1.getValueObject();                                
                if (!(identifiable instanceof ObjectRefType)) {
                    al.add(identifiable);
                }
            }
        }

        return al;
    }

    /**
     * Get separate List of RegistryObjects and ObjectRefs.
     * Does not get composed objects.
     */
    public void getObjectRefsAndRegistryObjects(RegistryObjectListType objs, List roList, Map orefMap) throws OMARException {

        if (objs != null) {
            List identifiables = objs.getIdentifiable();
            Iterator iter = identifiables.iterator();

            while (iter.hasNext()) {
                Object obj = iter.next();

                IdentifiableType identifiable = (IdentifiableType) obj;

                if (identifiable instanceof RegistryObjectType) {
                    roList.add(identifiable);
                }
                else if (identifiable instanceof ObjectRefType) {
                    orefMap.put(identifiable.getId(), identifiable);
                }
            }
        }

    }

    /**
     * Get the id from an object that could either an ObjectRef or RegistryObject
     */
    public String getObjectId(Object obj) throws OMARException {
        String id = null;

        if (obj != null) {
            if (obj instanceof ObjectRefType) {
                id = ((ObjectRefType) obj).getId();
            } else if (obj instanceof RegistryObjectType) {
                id = ((RegistryObjectType) obj).getId();
            } else if (obj instanceof String) {
                id = (String) obj;
            } else {
                throw new OMARException("Unexpected object of type " +
                    obj.getClass() +
                    ". Expected String or ObjectRef or RegistryObjectType");
            }
        }

        return id;
    }

    /**
     * Set the id for an object that could either an ObjectRef or RegistryObject
     */
    public void setObjectId(Object obj, String id) throws OMARException {
        if (obj != null) {
            if (obj instanceof ObjectRefType) {
                ((ObjectRefType) obj).setId(id);
            } else if (obj instanceof RegistryObjectType) {
                ((RegistryObjectType) obj).setId(id);
            } else {
                throw new OMARException("Unexpected object of type " +
                    obj.getClass() +
                    ". Expected Object or ObjectRef or RegistryObjectType");
            }
        }
    }

    /**
     * Gets trhe root element for a registry request
     * @return the root element as a String
     */
    public String getRequestRootElement(InputStream request)
        throws OMARException {
        String rootElementName = null;

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            dbf.setNamespaceAware(true);

            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(request);

            Element root = doc.getDocumentElement();
            rootElementName = root.getLocalName();
        } catch (IOException e) {
            throw new OMARException(e);
        } catch (ParserConfigurationException e) {
            throw new OMARException(e);
        } catch (SAXException e) {
            throw new OMARException(e);
        }

        return rootElementName;
    }

    /**
     * Gets the binding object representing the request from specufied XML file.
     */
    public Object getRequestObject(File file) throws OMARException {
        Object req = null;

        try {
            Unmarshaller unmarshaller = getUnmarshaller();
            req = unmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            e.printStackTrace();
            throw new OMARException("Unable to Unmarshall request", e);
        }

        return req;
    }

    public Object getRequestObject(String rootElement, String message)
        throws OMARException {
        //TODO: Consider removing String rootElement. Currently not used.
        Object req = null;

        try {
            StreamSource ss = new StreamSource(new StringReader(message));
            Unmarshaller unmarshaller = getUnmarshaller();
            req = unmarshaller.unmarshal(ss);
        } catch (JAXBException e) {
            e.printStackTrace();
            throw new OMARException("Unable to Unmarshall request", e);
        }

        return req;
    }

    private Unmarshaller getUnmarshaller() throws JAXBException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        //unmarshaller.setValidating(true);
        unmarshaller.setEventHandler(new ValidationEventHandler() {
                public boolean handleEvent(ValidationEvent event) {
                    boolean keepOn = false;

                    return keepOn;
                }
            });

        return unmarshaller;
    }

    /**
     * Gets a String representation of a list ids from a Collection of RegistryObjects.
     */
    public StringBuffer getIdListFromRegistryObjects(List objs) {
        StringBuffer idList = new StringBuffer();

        Iterator iter = objs.iterator();

        while (iter.hasNext()) {
            RegistryObjectType obj = (RegistryObjectType) iter.next();
            String id = obj.getId();
            idList.append("'" + id + "'");

            if (iter.hasNext()) {
                idList.append(", ");
            }
        }

        return idList;
    }

    /**
     * Get List of id of RegistryObjects
     */
    public List getIdsFromRegistryObjects(Collection objs) {
        List ids = new ArrayList();

        if (objs.size() > 0) {
            Iterator iter = objs.iterator();

            while (iter.hasNext()) {
                IdentifiableType ro = (IdentifiableType) iter.next();
                ids.add(ro.getId());
            }
        }

        return ids;
    }

    /**
     * Get List of ObjectRefs for specified RegistryObjects
     */
    public List getObjectRefsFromRegistryObjects(List objs) throws OMARException {
        List refs = new ArrayList();

        try {
            if (objs.size() > 0) {
                Iterator iter = objs.iterator();

                while (iter.hasNext()) {
                    IdentifiableType ro = (IdentifiableType) iter.next();
                    ObjectRef ref = rimFac.createObjectRef();
                    ref.setId(ro.getId());
                    refs.add(ref);
                }
            }
        }
        catch (JAXBException e) {
            throw new OMARException(e);
        }

        return refs;
    }

    /**
     * Filter out those RegistryObjects whose id are in the List ids
     */
    public List getRegistryObjectsFromIds(List objs, List ids) {
        List ros = new ArrayList();

        if ((ids.size() > 0) && (objs.size() > 0)) {
            Iterator iter = objs.iterator();

            while (iter.hasNext()) {
                RegistryObjectType ro = (RegistryObjectType) iter.next();

                if (ids.contains(ro.getId())) {
                    ros.add(ro);
                }
            }
        }

        return ros;
    }

    /**
     * Gets a String represnetation of a list ids from an ObjectRefList.
     */
    public StringBuffer getIdListFromObjectRefList(ObjectRefList refList) {
        StringBuffer idList = new StringBuffer();

        List refs = refList.getObjectRef();
        Iterator iter = refs.iterator();
        int cnt = refs.size();
        int i = 0;

        while (iter.hasNext()) {
            ObjectRefType ref = (ObjectRefType) iter.next();
            String id = ref.getId();
            idList.append("'" + id + "'");

            if (i < (cnt - 1)) {
                idList.append(", ");
            }

            i++;
        }

        return idList;
    }

    /**
     * Get comma delimited list of quoted id from List of ids.
     */
    public StringBuffer getIdListFromIds(List ids) {
        StringBuffer idList = new StringBuffer();
        Iterator iter = ids.iterator();

        while (iter.hasNext()) {
            String id = (String) iter.next();
            idList.append("'" + id + "'");

            if (iter.hasNext()) {
                idList.append(",");
            }
        }

        return idList;
    }

    /**
     * Get List of id of ObjectRef under ObjectRefList.
     */
    public List getIdsFromObjectRefList(ObjectRefListType refList) {
        List ids = new ArrayList();

        List refs = refList.getObjectRef();
        Iterator iter = refs.iterator();

        while (iter.hasNext()) {
            ObjectRefType ref = (ObjectRefType) iter.next();
            ids.add(ref.getId());
        }

        return ids;
    }

    /**
     * Get the first-level RegistryObject by id from SubmitObjectsRequest.
     */
    public Object getObjectFromRequest(SubmitObjectsRequest registryRequest,
        String id) throws OMARException {
        Object result = null;
        RegistryObjectListType objList = registryRequest.getRegistryObjectList();
        List objs = getRegistryObjectList(objList);

        Iterator iter = objs.iterator();

        while (iter.hasNext()) {
            Object obj = iter.next();
            String objId = getObjectId(obj);

            if (id.equalsIgnoreCase(objId)) {
                result = obj;

                break;
            }
        }

        return result;
    }

    /**
     * Get List of Id of first-level RegistryObject or ObjectRef in a request. For
     * those kinds of request having RegistryObject and ObjectRef (e.g. SubmitObjectsRequest),
     * only the id of RegistryObject elements are returned.
     */
    public List getIdsFromRequest(Object registryRequest)
        throws OMARException 
    {
        List ids = new ArrayList();

        if (registryRequest instanceof AdhocQueryRequest) {
        } 
        else if (registryRequest instanceof GetContentRequest) {
        } 
        else if (registryRequest instanceof ApproveObjectsRequest) {
            ObjectRefListType refList = ((ApproveObjectsRequest)registryRequest).getObjectRefList();
            ids.addAll(getIdsFromObjectRefList(refList));
        } 
        else if (registryRequest instanceof DeprecateObjectsRequest) {
            ObjectRefListType refList = ((DeprecateObjectsRequest) registryRequest).getObjectRefList();
            ids.addAll(getIdsFromObjectRefList(refList));
        } 
        else if (registryRequest instanceof RemoveObjectsRequest) {
            ObjectRefListType refList = ((RemoveObjectsRequest) registryRequest).getObjectRefList();
            ids.addAll(getIdsFromObjectRefList(refList));
        } 
        else if (registryRequest instanceof SubmitObjectsRequest) {
            RegistryObjectListType objList =
                ((SubmitObjectsRequest) registryRequest).getRegistryObjectList();
            List objs = getRegistryObjectList(objList);
            ids.addAll(getIdsFromRegistryObjects(objs));
        } 
        else if (registryRequest instanceof UpdateObjectsRequest) {
            RegistryObjectListType objList =
                ((UpdateObjectsRequest) registryRequest).getRegistryObjectList();
            List objs = getRegistryObjectList(objList);
            ids.addAll(getIdsFromRegistryObjects(objs));
        } 
        else if (registryRequest instanceof AddSlotsRequest) {
            ObjectRefType ref = ((AddSlotsRequest) registryRequest).getObjectRef();
            ids.add(ref.getId());
        } 
        else if (registryRequest instanceof RemoveSlotsRequest) {
            ObjectRefType ref = ((RemoveSlotsRequest) registryRequest).getObjectRef();
            ids.add(ref.getId());
        }
        else {
            throw new OMARException("InvalidRequest: Unknown request " +
                registryRequest.getClass().getName());
        }

        return ids;
    }

    /*
     * Create a GetContentRequest
     */
    public String toGetContentRequest(String id) throws OMARException {
        StringWriter sw = new StringWriter();

        try {
            ObjectRef objRef = rimFac.createObjectRef();
            objRef.setId(id);

            ObjectRefListType orl = rimFac.createObjectRefList();
            orl.getObjectRef().add(objRef);

            GetContentRequest gcr = queryFac.createGetContentRequest();
            gcr.setObjectRefList(orl);

            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.marshal(gcr, sw);
        } catch (JAXBException e) {
            throw new OMARException(e);
        }

        return sw.toString();
    }

    public Name getName(String name) throws OMARException {
        Name internationalName = null;

        try {
            internationalName = BindingUtility.getInstance().rimFac.createName();

            LocalizedString ls = BindingUtility.getInstance().rimFac.createLocalizedString();
            ls.setValue(name);
            internationalName.getLocalizedString().add(ls);
        } catch (JAXBException e) {
            throw new OMARException(e);
        }

        return internationalName;
    }

    public Description getDescription(String desc) throws OMARException {
        Description internationalDesc = null;

        try {
            internationalDesc = BindingUtility.getInstance().rimFac.createDescription();

            LocalizedString ls = BindingUtility.getInstance().rimFac.createLocalizedString();
            ls.setValue(desc);
            internationalDesc.getLocalizedString().add(ls);
        } catch (JAXBException e) {
            throw new OMARException(e);
        }

        return internationalDesc;
    }
    
    public SOAPElement getSOAPElementFromBindingObject(Object obj) throws OMARException {
        SOAPElement soapElem = null;
        
        try {
            SOAPElement parent =
                SOAPFactory.newInstance().createElement("dummy");

            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.marshal( obj, new DOMResult(parent) );
            soapElem = (SOAPElement)parent.getChildElements().next();
    
        }
        catch (Exception e) {
            throw new OMARException(e);
        }
        
        return soapElem;
    }
    
    public Object getBindingObjectFromSOAPElement(SOAPElement soapElem) throws OMARException {
        Object obj = null;
        
        try {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            obj = unmarshaller.unmarshal(soapElem);
            
        }
        catch (Exception e) {
            throw new OMARException(e);
        }
        
        return obj;
    }
    
    public void checkRegistryResponse(RegistryResponseType resp) throws OMARException {        
        if (resp.getStatus() != Status.SUCCESS) {
            StringWriter sw = new StringWriter();
            try {
                Marshaller marshaller = jaxbContext.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                    Boolean.TRUE);

                marshaller.marshal(resp, sw);
                throw new OMARException(sw.toString());
            }
            catch (Exception e) {
                throw new OMARException(e);
            }
        }        
    }
    
    /**
     * Gets the ObjectRefs within specified RregistryObject.
     *
     * Reference attributes based on scanning rim.xsd for anyURI.
     *
     * @param ro specifies the RegistryObject whose ObjectRefs are being sought.
     *
     * @param idMap The HashMap with old temporary id to new permanent id mapping.
     *
     */
    public Set getObjectRefsInRegistryObject(RegistryObjectType ro, HashMap idMap) throws OMARException {
        HashSet objectRefs = new HashSet();                

        processRefAttribute(ro, "org.oasis.ebxml.registry.bindings.rim.RegistryObjectType", objectRefs, idMap, "ObjectType");

        processRefAttribute(ro, "org.oasis.ebxml.registry.bindings.rim.ClassificationNodeType", objectRefs, idMap, "Parent");

        processRefAttribute(ro, "org.oasis.ebxml.registry.bindings.rim.ClassificationType", objectRefs, idMap, "ClassificationNode");
        processRefAttribute(ro, "org.oasis.ebxml.registry.bindings.rim.ClassificationType", objectRefs, idMap, "ClassificationScheme");
        processRefAttribute(ro, "org.oasis.ebxml.registry.bindings.rim.ClassificationType", objectRefs, idMap, "ClassifiedObject");
       
        processRefAttribute(ro, "org.oasis.ebxml.registry.bindings.rim.ExternalIdentifierType", objectRefs, idMap, "IdentificationScheme");
        processRefAttribute(ro, "org.oasis.ebxml.registry.bindings.rim.ExternalIdentifierType", objectRefs, idMap, "RegistryObject");
        
        //FederationType fed = (FederationType)ro;
        //TODO: Fix so it adds only Strings not ObjectRefType
        //objectRefs.addAll(fed.getMembers().getObjectRef());
        
        processRefAttribute(ro, "org.oasis.ebxml.registry.bindings.rim.AssociationType1", objectRefs, idMap, "AssociationType");
        processRefAttribute(ro, "org.oasis.ebxml.registry.bindings.rim.AssociationType1", objectRefs, idMap, "SourceObject");
        processRefAttribute(ro, "org.oasis.ebxml.registry.bindings.rim.AssociationType1", objectRefs, idMap, "TargetObject");

        
        processRefAttribute(ro, "org.oasis.ebxml.registry.bindings.rim.AuditableEventType", objectRefs, idMap, "User");
        processRefAttribute(ro, "org.oasis.ebxml.registry.bindings.rim.AuditableEventType", objectRefs, idMap, "RequestId");

        processRefAttribute(ro, "org.oasis.ebxml.registry.bindings.rim.OrganizationType", objectRefs, idMap, "Parent");
        
        processRefAttribute(ro, "org.oasis.ebxml.registry.bindings.rim.RegistryType", objectRefs, idMap, "Operator");

        processRefAttribute(ro, "org.oasis.ebxml.registry.bindings.rim.ServiceBindingType", objectRefs, idMap, "Service");
        processRefAttribute(ro, "org.oasis.ebxml.registry.bindings.rim.ServiceBindingType", objectRefs, idMap, "TargetBinding");

        processRefAttribute(ro, "org.oasis.ebxml.registry.bindings.rim.SpecificationLinkType", objectRefs, idMap, "ServiceBinding");
        processRefAttribute(ro, "org.oasis.ebxml.registry.bindings.rim.SpecificationLinkType", objectRefs, idMap, "SpecificationObject");
        
        processRefAttribute(ro, "org.oasis.ebxml.registry.bindings.rim.SubscriptionType", objectRefs, idMap, "Selector");                
        
        return objectRefs;
    }
    
    /**
     * Gets the ObjectRefs within specified RregistryObject.
     *
     * Reference attributes based on scanning rim.xsd for anyURI.
     *
     * @param ro specifies the RegistryObject whose ObjectRefs are being sought.
     *
     * @param idMap The HashMap with old temporary id to new permanent id mapping.
     *
     */
    private void processRefAttribute(RegistryObjectType ro, String className, HashSet objectRefs, HashMap idMap, String attribute) throws OMARException {
        try {
            //Use reflections API to get the attribute value, check if it needs to be mapped
            //and set it with mapped value if needed and add the final value to objectRefs           
            Class clazz = Class.forName(className);
            if (!(clazz.isInstance(ro))) {
                return;
            }            
            
            //Get the attribute value by calling get method
            String getMethodName = "get" + attribute;
            Method getMethod = clazz.getMethod(getMethodName, null);

            //Invoke getMethod to get the current id value
            Object id = getMethod.invoke(ro, null);

            if (id != null) {
                //Check if id has been mapped to a new id
                if (idMap.containsKey(id)) {
                    //Replace old id with new id
                    id = (String)idMap.get(id);

                    //Use set method to set new value on ro
                    Class[] parameterTypes = new Class[1];
                    Object[] parameterValues = new Object[1];
                    parameterTypes[0] = Class.forName("java.lang.String");                    
                    parameterValues[0] = id;
                    String setMethodName = "set" + attribute;
                    Method setMethod = clazz.getMethod(setMethodName, parameterTypes);
                    setMethod.invoke(ro, parameterValues);
                }
                
                objectRefs.add(id);            
            }                        
            
        }
        catch (Exception e) {
            //throw new OMARExeption("Class = " ro.getClass() + " attribute = " + attribute", e);
            System.err.println("Error: Class = " + ro.getClass() + " attribute = " + attribute);
            e.printStackTrace();
        }

    }
        
    
    /**
     * Gets the composed RegistryObjects within specified RregistryObject.
     * Based on scanning rim.xsd for </sequence>.
     *
     * @param registryObjects specifies the RegistryObjects whose composed objects are being sought.
     * @param depth specifies depth of fetch. -1 implies fetch all levels. 1 implies fetch immediate composed objects. 
     */
    public Set getComposedRegistryObjects(List registryObjects, int depth) {
        HashSet composedObjects = new HashSet();                

        Iterator iter = registryObjects.iterator();
        while (iter.hasNext()) {
            RegistryObjectType ro = (RegistryObjectType)iter.next();
            composedObjects.addAll(getComposedRegistryObjects(ro, depth));
        }
        
        return composedObjects;
    }
    
    /**
     * Gets the composed RegistryObjects within specified RregistryObject.
     * Based on scanning rim.xsd for </sequence>.
     *
     * @param ro specifies the RegistryObject whose composed objects are being sought.
     * @param depth specifies depth of fetch. -1 implies fetch all levels. 1 implies fetch immediate composed objects. 
     */
    public Set getComposedRegistryObjects(RegistryObjectType ro, int depth) {
        HashSet composedObjects = new HashSet();                
        
        List immdeiateComposedObjects = new ArrayList();
                
        immdeiateComposedObjects.addAll(ro.getClassification());
        immdeiateComposedObjects.addAll(ro.getExternalIdentifier());
        
        
        if (ro instanceof ClassificationNodeType) {
            ClassificationNodeType node = (ClassificationNodeType)ro;            
            immdeiateComposedObjects.addAll(node.getClassificationNode());
        }
        else if (ro instanceof ClassificationSchemeType) {
            ClassificationSchemeType scheme = (ClassificationSchemeType)ro;
            immdeiateComposedObjects.addAll(scheme.getClassificationNode());
        }
        else if (ro instanceof ServiceBindingType) {
            ServiceBindingType binding = (ServiceBindingType)ro;
            immdeiateComposedObjects.addAll(binding.getSpecificationLink());
        }
        else if (ro instanceof RegistryPackageType) {
            RegistryPackageType pkg = (RegistryPackageType)ro;
            if (pkg.getRegistryObjectList() != null) {
                immdeiateComposedObjects.addAll(pkg.getRegistryObjectList().getIdentifiable());
            }
        }
        else if (ro instanceof ServiceType) {
            ServiceType service = (ServiceType)ro;
            immdeiateComposedObjects.addAll(service.getServiceBinding());
        }
        
        --depth;
        
        //Add each imediate composedObject
        Iterator iter = immdeiateComposedObjects.iterator();
        while (iter.hasNext()) {
            RegistryObjectType composedObject = (RegistryObjectType)iter.next();
            composedObjects.add(composedObject);
            
            //If depth != 0 then recurse and add descendant composed objects
            if (depth != 0) {
                composedObjects.addAll(getComposedRegistryObjects(composedObject, depth));
            }
        }
        
        return composedObjects;
    }
    
    public void printObject(Object obj) throws JAXBException {
        
        StringWriter sw = new StringWriter();
        javax.xml.bind.Marshaller marshaller = BindingUtility.getInstance().rsFac.createMarshaller();
        marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT,
            Boolean.TRUE);
        marshaller.marshal(obj, sw);
        
        //Now get the object as a String
        String str = sw.toString();
        System.err.println(str);
    }
        
    /**
     * Gets the singleton instance as defined by Singleton pattern.
     *
     * @return the singleton instance
     *
     */
    public static BindingUtility getInstance() {
        if (instance == null) {
            synchronized (BindingUtility.class) {
                if (instance == null) {
                    instance = new BindingUtility();
                }
            }
        }

        return instance;
    }
}
