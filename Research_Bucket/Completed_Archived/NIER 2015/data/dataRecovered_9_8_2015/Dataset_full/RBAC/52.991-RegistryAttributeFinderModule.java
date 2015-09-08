/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/server/security/authorization/RegistryAttributeFinderModule.java,v 1.11 2004/03/31 19:37:31 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.security.authorization;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.OMARException;
import org.freebxml.omar.common.QueryManagerFactory;
import org.freebxml.omar.common.RegistryException;
import org.freebxml.omar.server.persistence.PersistenceManagerFactory;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.query.ResponseOption;
import org.oasis.ebxml.registry.bindings.query.ReturnType;
import org.oasis.ebxml.registry.bindings.rim.Classification;
import org.oasis.ebxml.registry.bindings.rim.ClassificationNode;
import org.oasis.ebxml.registry.bindings.rim.ClassificationNodeType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.ParsingException;
import com.sun.xacml.attr.AnyURIAttribute;
import com.sun.xacml.attr.AttributeDesignator;
import com.sun.xacml.attr.AttributeValue;
import com.sun.xacml.attr.BagAttribute;
import com.sun.xacml.attr.BooleanAttribute;
import com.sun.xacml.attr.DateTimeAttribute;
import com.sun.xacml.attr.IntegerAttribute;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.cond.EvaluationResult;
import com.sun.xacml.ctx.Status;
import com.sun.xacml.finder.AttributeFinderModule;


/** Supports the attributes defined by ebRIM for RegistryObjects.
  *
  * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
  */
public class RegistryAttributeFinderModule extends AttributeFinderModule {
    
    private Log log = LogFactory.getLog(RegistryAttributeFinderModule.class);
    
    /**
     * The prefix for all resource attribute designators as defined by ebRIM.
     */
    public static final String REGISTRY_RESOURCE_PREFIX = "urn:oasis:names:tc:ebxml-regrep:3.0:rim:acp:resource:";

    /**
     * The prefix for all subject attribute designators as defined by ebRIM.
     */
    public static final String REGISTRY_SUBJECT_PREFIX = "urn:oasis:names:tc:ebxml-regrep:3.0:rim:acp:subject:";

    /**
     * The owner resource attribute designator as defined by ebRIM.
     */
    public static final String REGISTRY_RESOURCE_OWNER = REGISTRY_RESOURCE_PREFIX + "owner";

    /**
     * The selector resource attribute designator as defined by ebRIM.
     */
    public static final String REGISTRY_RESOURCE_SELECTOR = REGISTRY_RESOURCE_PREFIX + "selector";
    
    private static BindingUtility bu = BindingUtility.getInstance();

    
    /**
     * Returns true always because this module supports designators.
     *
     * @return true always
     */
    public boolean isDesignatorSupported() {
        return true;
    }

    /**
     * Returns a <code>Set</code> with a single <code>Integer</code>
     * specifying that environment attributes are supported by this
     * module.
     *
     * @return a <code>Set</code> with
     * <code>AttributeDesignator.SUBJECT_TARGET</code> and <code>AttributeDesignator.RESOURCE_TARGET</code> included
     */
    public Set getSupportedDesignatorTypes() {
        HashSet set = new HashSet();
        set.add(new Integer(AttributeDesignator.SUBJECT_TARGET));
        set.add(new Integer(AttributeDesignator.RESOURCE_TARGET));
        return set;
    }

    /** Used to get the attributes defined by ebRIM for resources and subjects.
      * If one of those values isn't being asked for, or if the types are wrong,
      * then a empty bag is returned.
      *
      * @param attributeType 
      *     the datatype of the attributes to find
      * @param attributeId 
      *     the identifier of the attributes to find
      * @param issuer 
      *     the issuer of the attributes, or null if unspecified
      * @param subjectCategory 
      *     the category of the attribute or null
      * @param context 
      *     the representation of the request data
      * @param designatorType 
      *     the type of designator
      * @return 
      *     the result of attribute retrieval, which will be a bag with
      *     a single attribute, an empty bag, or an error
      */
    public EvaluationResult findAttribute(URI attributeType, 
                                          URI attributeId,
                                          URI issuer, 
                                          URI subjectCategory, 
                                          EvaluationCtx context,
                                          int designatorType) 
    {
        // we only know about subject and resource attributes
        if ((designatorType != AttributeDesignator.SUBJECT_TARGET) &&
            (designatorType != AttributeDesignator.RESOURCE_TARGET)) 
        {
            return new EvaluationResult(BagAttribute.createEmptyBag(attributeType));
        }

        // figure out which attribute we're looking for
        String attrName = attributeId.toString();
        EvaluationResult res = null;
        if (attrName.startsWith(REGISTRY_RESOURCE_PREFIX)) {
            res = handleRegistryResourceAttribute(attributeId, attributeType, context);
        } 
        else if (attrName.startsWith(REGISTRY_SUBJECT_PREFIX)) {
            res = handleRegistrySubjectAttribute(attributeId, 
                                                 attributeType, 
                                                 subjectCategory,
                                                 context);
        }

        if (res == null) {
            res = new EvaluationResult(BagAttribute.createEmptyBag(attributeType));
        }

        return res;
    }

    /**
     * Handles resource attributes as defined ebRIM.
     */
    private EvaluationResult handleRegistryResourceAttribute(URI attributeId,
                                                             URI type, 
                                                             EvaluationCtx context) 
    {
        EvaluationResult res = handleRegistryObjectAttribute(attributeId, type, context);
        return res;
    }

    /**
     * Handles subject attributes as defined ebRIM.
     */
    private EvaluationResult handleRegistrySubjectAttribute(URI attributeId,
                                                            URI type, 
                                                            URI subjectCategory, 
                                                            EvaluationCtx context)
    {
        EvaluationResult res = null;

        String attributeIdStr = attributeId.toString();

        try {
            // First check if attribute is role or group which are special cases since 
            // they are not actual attributes in ebRIM.
            if (attributeIdStr.equals(AuthorizationServiceImpl.SUBJECT_ATTRIBUTE_ROLES)) {
                Object user = getSubjectObject(context, AuthorizationServiceImpl.SUBJECT_ATTRIBUTE_USER, subjectCategory);
                Set nodeIds = getClassificationNodeIds(user, AuthorizationServiceImpl.CANONICAL_ID_NODE_SUBJECT_ROLE);
                res = makeBag(new URI(StringAttribute.identifier), nodeIds);
            } 
            else if (attributeIdStr.equals(AuthorizationServiceImpl.SUBJECT_ATTRIBUTE_GROUPS)) {
                Object user = getSubjectObject(context, AuthorizationServiceImpl.SUBJECT_ATTRIBUTE_USER, subjectCategory);
                Set nodeIds = getClassificationNodeIds(user, AuthorizationServiceImpl.CANONICAL_ID_NODE_SUBJECT_GROUP);
                res = makeBag(new URI(StringAttribute.identifier), nodeIds);
            } 
            else {
                // Not a role or group attribute
                // See if it is a RegistryObject attribute defined by ebRIM.
//                res = handleRegistryObjectAttribute(attributeId, type, context);
            }
        } 
        catch (URISyntaxException e) {
            e.printStackTrace();
        } 
        catch (RegistryException e) {
            e.printStackTrace();
        } 
        catch (Exception e) {
            e.printStackTrace();
        }

        return res;
    }

    /**
     * Gets the Set of id Strings for all the nodes that classify the specified object
     * within specified ClassificationScheme
     */
    public Set getClassificationNodeIds(Object obj, String schemeId)
        throws RegistryException 
    {
        Set nodeIds = new HashSet();

        try {
            if (obj instanceof RegistryObjectType) {
                RegistryObjectType ro = (RegistryObjectType) obj;
                List classifications = ro.getClassification();
                Iterator iter = classifications.iterator();

                while (iter.hasNext()) {
                    Classification classification =
                        (Classification) iter.next();
                    String classificationNodeId = bu.getObjectId(classification.getClassificationNode());
                    ClassificationNode node = 
                        (ClassificationNode)(QueryManagerFactory.getInstance()
                                                                .getQueryManager()
                                                                .getRegistryObject(classificationNodeId));
                    String path = node.getPath();

                    if (path.startsWith("/" + schemeId + "/")) {
                        nodeIds.add(new StringAttribute(node.getCode()));
                    }
                }
            }
        } 
        catch (OMARException e) {
            throw new RegistryException(e);
        }

        return nodeIds;
    }

    /**
     * Handles attributes as defined ebRIM for Any RegistryObject.
     * Used by both subject and resource attributes handling methods.
     */
    private EvaluationResult handleRegistryObjectAttribute(URI attributeId,
                                                           URI type, 
                                                           EvaluationCtx context) 
    {
        EvaluationResult res = null;

        Object reqObj = getObject(context, AuthorizationServiceImpl.RESOURCE_ATTRIBUTE_REQUEST);

        if (reqObj != null) {
            //Get the resource id from EvaluationContext                    
            String id = context.getResourceId().encode();
            Object obj = null;

            try {
                if (reqObj instanceof SubmitObjectsRequest) {
                    SubmitObjectsRequest req =(SubmitObjectsRequest)reqObj;

                    //Get the objects state from the request not from persistence layer
                    obj = bu.getObjectFromRequest(req, id);
                } 
                else {
                    //For all other cases read the objects persistent state for attribute value
                    obj = QueryManagerFactory.getInstance().getQueryManager().getRegistryObject(id);
                }

                //Now invoke a get method to get the value for attribute being sought
                Class clazz = obj.getClass();
                String clazzName = clazz.getName();
                String attr = getAttributeFromAttributeId(attributeId);
                PropertyDescriptor propDesc = new PropertyDescriptor(attr, clazz);
                Method method = propDesc.getReadMethod();
                Object attrValObj = method.invoke(obj, null);

                //??Special kludge to handle the fact that current server returns value instead of id
                //for ClassificationNode for objectType
                if (attr.equals("objectType")) {
                    //on writes objectType is often not specified
                    if (attrValObj == null) {
                        attrValObj = clazzName.substring
                            (clazzName.lastIndexOf('.', clazzName.length()) + 1, clazzName.length());
                    }

                    String attrValStr = (String) attrValObj;

                    if (!(attrValStr.startsWith("urn:uuid:"))) {
                        ClassificationNodeType node = getClassificationNode
                            ("/urn:uuid:3188a449-18ac-41fb-be9f-99a1adca02cb/%" + attrValStr);
                        attrValObj = node.getId();
                    }
                }

                AttributeValue attrVal = makeAttribute(attrValObj, type);

                return makeBag(attrVal);
            } 
            catch (InvocationTargetException e) {
                e.printStackTrace();
            } 
            catch (IntrospectionException e) {
                e.printStackTrace();
            } 
            catch (IllegalAccessException e) {
                e.printStackTrace();
            } 
            catch (ParseException e) {
                e.printStackTrace();
            } 
            catch (ParsingException e) {
                e.printStackTrace();
            } 
            catch (URISyntaxException e) {
                e.printStackTrace();
            } 
            catch (OMARException e) {
                e.printStackTrace();
            }
        }

        return res;
    }

    /**
     * This method used by RegistryAttributeFinderModule
     *
     */
    private static ClassificationNodeType getClassificationNode(String pathPattern) 
        throws RegistryException 
    {
        ClassificationNodeType ro = null;

        try {
            String sqlQuery = "Select * from ClassificationNode WHERE path LIKE '" + pathPattern + "' ";
            ResponseOption responseOption =
                BindingUtility.getInstance().queryFac.createResponseOption();
            responseOption.setReturnComposedObjects(true);
            responseOption.setReturnType(ReturnType.LEAF_CLASS);

            List objectRefs = new ArrayList();
            List results = PersistenceManagerFactory.getInstance()
                                                    .getPersistenceManager()
                                                    .executeSQLQuery(sqlQuery, responseOption, "ClassificationNode", objectRefs);

            if (results.size() == 1) {
                ro = (ClassificationNodeType) results.get(0);
            }
        } 
        catch (JAXBException e) {
            throw new RegistryException(e);
        }

        return ro;
    }

    /**
      *
      * @param context
      * @param attributeId
      * @return
      */    
    private Object getObject(EvaluationCtx context, String attributeId) {
        
        Object obj = null;
        try {
            EvaluationResult result = context.getResourceAttribute
                (new URI(ObjectAttribute.identifier), new URI(attributeId), null);
            AttributeValue attrValue = result.getAttributeValue();
            BagAttribute bagAttr = (BagAttribute)attrValue;
            if (bagAttr.size() == 1) {
                Iterator iter = bagAttr.iterator();
                ObjectAttribute objAttr = (ObjectAttribute)iter.next();
                if (objAttr != null) {
                    obj = objAttr.getValue();
                }
            }
        } 
        catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return obj;
    }

    /** Get the object from the specified subject attribute id.
      *
      * @param context
      * @param subjectAttributeId
      * @return
      */    
    private Object getSubjectObject(EvaluationCtx context, String subjectAttributeId, URI subjectCategory) {
        
        Object obj = null;
        try {
            EvaluationResult result = context.getSubjectAttribute
                (new URI(ObjectAttribute.identifier), new URI(subjectAttributeId), subjectCategory);
            AttributeValue attrValue = result.getAttributeValue();
            BagAttribute bagAttr = (BagAttribute)attrValue;
            if (bagAttr.size() == 1) {
                Iterator iter = bagAttr.iterator();
                ObjectAttribute objAttr = (ObjectAttribute)iter.next();
                if (objAttr != null) {
                    obj = objAttr.getValue();
                }
            }
        } 
        catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return obj;
    }

    /**
     * Parses the attribute name from a URI rep of the Attribute id
     * If input is: "urn:oasis:names:tc:ebxml-regrep:3.0:rim:acp:resource:objectType"
     * then return value will be "objectType".
     **/
    private String getAttributeFromAttributeId(URI attributeId) {
        String attr = null;

        String attrIdStr = attributeId.toString();
        attr = attrIdStr.substring(attrIdStr.lastIndexOf(':') + 1,
                attrIdStr.length());

        return attr;
    }

    /**
     * Makes an AttributeValue from Object param using the attrType param and
     * the mapping specified in ebRIM between RIM types and XACML data types.
     */
    private AttributeValue makeAttribute(Object attrValObj, URI attrType) throws ParsingException, URISyntaxException, ParseException {
        AttributeValue val = null;
        String attrTypeStr = attrType.toString();

        if (attrTypeStr.equals(BooleanAttribute.identifier)) {
            val = BooleanAttribute.getInstance(attrValObj.toString());
        } else if (attrTypeStr.equals(
                    StringAttribute.identifier)) {
            val = StringAttribute.getInstance(attrValObj.toString());
        } else if (attrTypeStr.equals(
                    AnyURIAttribute.identifier)) {
            val = AnyURIAttribute.getInstance(attrValObj.toString());
        } else if (attrTypeStr.equals(
                    IntegerAttribute.identifier)) {
            val = IntegerAttribute.getInstance(attrValObj.toString());
        } else if (attrTypeStr.equals(
                    DateTimeAttribute.identifier)) {
            val = DateTimeAttribute.getInstance(attrValObj.toString());
        }

        return val;
    }

    /**
     * Private helper that generates a new processing error status and
     * includes the given string.
     */
    private EvaluationResult makeProcessingError(String message) {
        List code = new ArrayList();
        code.add(Status.STATUS_PROCESSING_ERROR);

        return new EvaluationResult(new Status(code, message));
    }

    /**
     * Private helper that makes a bag containing only the given attribute.
     */
    private EvaluationResult makeBag(AttributeValue attribute) {
        Set set = new HashSet();
        set.add(attribute);

        BagAttribute bag = new BagAttribute(attribute.getType(), set);

        return new EvaluationResult(bag);
    }

    /**
     * Private helper that makes a bag containing only the given attribute.
     */
    private EvaluationResult makeBag(URI type, Set attributeValues) {
        BagAttribute bag = new BagAttribute(type, attributeValues);

        return new EvaluationResult(bag);
    }
}
/*
 * $Header: /cvsroot/sino/ebxmlrr/src/share/com/sun/ebxml/registry/security/authorization/RegistryAttributeFinderModule.java,v 1.2 2003/06/26 13:26:44 farrukh_najmi Exp $
 *
 * ====================================================================
 * 
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2003 freebxml.org.  All rights reserved.
 * 
 * ====================================================================
 */

package com.sun.ebxml.registry.security.authorization;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.attr.AttributeValue;
import com.sun.xacml.attr.BagAttribute;
import com.sun.xacml.cond.EvaluationResult;
import java.net.URI;

import java.util.Set;



/**
 * Supports the attributes defined by ebRIM for RegistryObjects.
 * 
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class RegistryAttributeFinderModule extends com.sun.xacml.finder.AttributeFinderModule
{
    /**
     * The prefix for all resource attribute designators as defined by ebRIM.
     */
    public static final String REGISTRY_RESOURCE_PREFIX =
        "urn:oasis:names:tc:ebxml-regrep:2.5:rim:acp:resource:";
    
    /**
     * The prefix for all subject attribute designators as defined by ebRIM.
     */
    public static final String REGISTRY_SUBJECT_PREFIX =
        "urn:oasis:names:tc:ebxml-regrep:2.5:rim:acp:subject:";
    
    /**
     * The owner resource attribute designator as defined by ebRIM.
     */
    public static final String REGISTRY_RESOURCE_OWNER =
        REGISTRY_RESOURCE_PREFIX + "owner";

    /**
     * The selector resource attribute designator as defined by ebRIM.
     */
    public static final String REGISTRY_RESOURCE_SELECTOR =
        REGISTRY_RESOURCE_PREFIX + "selector";

    private com.sun.ebxml.registry.security.authorization.AuthorizationServiceImpl az = 
            com.sun.ebxml.registry.security.authorization.AuthorizationServiceImpl.getInstance();
    private static com.sun.ebxml.registry.query.QueryManagerImpl qm = com.sun.ebxml.registry.query.QueryManagerImpl.getInstance();
    private static com.sun.ebxml.registry.util.BindingUtility bu = com.sun.ebxml.registry.util.BindingUtility.getInstance();
    
    
    /**
     * Returns true always because this module supports designators.
     *
     * @return true always
     */
    public boolean isDesignatorSupported() {
        return true;
    }

    /**
     * Returns a <code>Set</code> with a single <code>Integer</code>
     * specifying that environment attributes are supported by this
     * module.
     *
     * @return a <code>Set</code> with
     * <code>AttributeDesignator.SUBJECT_TARGET</code> and <code>AttributeDesignator.RESOURCE_TARGET</code> included
     */
    public Set getSupportedDesignatorTypes() {
        java.util.HashSet set = new java.util.HashSet();
        set.add(new Integer(com.sun.xacml.attr.AttributeDesignator.SUBJECT_TARGET));
        set.add(new Integer(com.sun.xacml.attr.AttributeDesignator.RESOURCE_TARGET));
        return set;
    }

    /**
     * Used to get the attributes defined by ebRIM for resources and subjects.
     * If one of those values isn't being asked for, or if the types are wrong, 
     * then a empty bag is returned.
     *
     * @param attributeType the datatype of the attributes to find
     * @param attributeId the identifier of the attributes to find
     * @param issuer the issuer of the attributes, or null if unspecified
     * @param subjectCategory the category of the attribute or null
     * @param context the representation of the request data
     * @param designatorType the type of designator
     *
     * @return the result of attribute retrieval, which will be a bag with
     *         a single attribute, an empty bag, or an error
     */
    public EvaluationResult findAttribute(URI attributeType, URI attributeId, URI issuer, URI subjectCategory, EvaluationCtx context, int designatorType) {
        // we only know about subject and resource attributes
        if ((designatorType != com.sun.xacml.attr.AttributeDesignator.SUBJECT_TARGET) &&
            (designatorType != com.sun.xacml.attr.AttributeDesignator.RESOURCE_TARGET)) {
            return new EvaluationResult(BagAttribute.
                                        createEmptyBag(attributeType));
        }

        // figure out which attribute we're looking for
        String attrName = attributeId.toString();        
        EvaluationResult res = null;
        
        if (attrName.startsWith(REGISTRY_RESOURCE_PREFIX)) {
            res = handleRegistryResourceAttribute(attributeId, attributeType, context);
        }
        else if (attrName.startsWith(REGISTRY_SUBJECT_PREFIX)) {
            res = handleRegistrySubjectAttribute(attributeId, attributeType, context);
        }
        
        if (res == null) {
            res = new EvaluationResult(BagAttribute.
                                        createEmptyBag(attributeType));
        }
        
        return res;
    }
        
    /**
     * Handles resource attributes as defined ebRIM.
     */
    private EvaluationResult handleRegistryResourceAttribute(URI attributeId, URI type, EvaluationCtx context) {
        EvaluationResult res = handleRegistryObjectAttribute(attributeId, type, context);
        return res;
    }
    
    /**
     * Handles subject attributes as defined ebRIM.
     */
    private EvaluationResult handleRegistrySubjectAttribute(URI attributeId, URI type, EvaluationCtx context) {
        EvaluationResult res = null;
        
        String attributeIdStr = attributeId.toString();
        
        try {
            //First check if attribute is role or group which are special cases since 
            //they are not actual attributes in ebRIM.
            if (attributeIdStr.equals(az.SUBJECT_ATTRIBUTE_ROLES)) {
                Object user = getObject(context, az.SUBJECT_ATTRIBUTE_USER);        
                Set nodeIds = getClassificationNodeIds(user, az.SUBJECT_ATTRIBUTE_ROLES);
                res = makeBag(new URI(com.sun.xacml.attr.AnyURIAttribute.identifier), nodeIds);            
            }
            else if (attributeIdStr.equals(az.SUBJECT_ATTRIBUTE_GROUPS)) {
                Object user = getObject(context, az.SUBJECT_ATTRIBUTE_USER);        
                Set nodeIds = getClassificationNodeIds(user, az.SUBJECT_ATTRIBUTE_GROUPS);
                res = makeBag(new URI(com.sun.xacml.attr.AnyURIAttribute.identifier), nodeIds);            
            }        
            else {
                /*
                //Not a role or group attribute
                //See if it is a RegistryObject attribute defined by ebRIM.            
                res = handleRegistryObjectAttribute(attributeId, type, context);
                 **/
            }
        }
        catch (java.net.URISyntaxException e) {
            e.printStackTrace();
        }
        catch (com.sun.ebxml.registry.RegistryException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
       
        return res;
    }
    
    /**
     * Gets the Set of id STrings for all the nodes that classify the specified object
     * within specified ClassificationScheme
     */
    public Set getClassificationNodeIds(Object obj, String schemeId) throws com.sun.ebxml.registry.RegistryException {
        Set nodeIds = new java.util.HashSet();

        if (obj instanceof org.oasis.ebxml.registry.bindings.rim.RegistryObjectType) {
            org.oasis.ebxml.registry.bindings.rim.RegistryObjectType ro = (org.oasis.ebxml.registry.bindings.rim.RegistryObjectType)obj;
            org.oasis.ebxml.registry.bindings.rim.Classification[] classifications = ro.getClassification();
            for (int i=0; i<classifications.length; i++) {
                org.oasis.ebxml.registry.bindings.rim.Classification classification = classifications[i];
                String classificationNodeId = bu.getObjectId(classification.getClassificationNode());
                org.oasis.ebxml.registry.bindings.rim.ClassificationNode node = (org.oasis.ebxml.registry.bindings.rim.ClassificationNode)qm.getRegistryObject(classificationNodeId);
                String path = node.getPath();
                if (path.startsWith("/"+schemeId+"/")) {
                    nodeIds.add(classificationNodeId);
                }
            }
        }
        
        return nodeIds;
    }
    
    
    /**
     * Handles attributes as defined ebRIM for Any RegistryObject.
     * Used by both subject and resource attributes handling methods.
     */
    private EvaluationResult handleRegistryObjectAttribute(URI attributeId, URI type, EvaluationCtx context) {
        EvaluationResult res = null;
        
        Object reqObj = getObject(context, az.RESOURCE_ATTRIBUTE_REQUEST);
        if (reqObj != null) {
            //Get the resource id from EvaluationContext                    
            String id = context.getResourceId().encode();
            Object obj = null;

            try {
                if (reqObj instanceof org.oasis.ebxml.registry.bindings.rs.SubmitObjectsRequest) {
                    org.oasis.ebxml.registry.bindings.rs.SubmitObjectsRequest req = (org.oasis.ebxml.registry.bindings.rs.SubmitObjectsRequest)reqObj;

                    //Get the objects state from the request not from persistence layer
                    obj = bu.getObjectFromRequest(req, id);
                }
                else {
                    //For all other cases read the objects persistent state for attribute value
                    obj = qm.getRegistryObject(id);                 
                }

                //Now invoke a get method to get the value for attribute being sought
                Class clazz = obj.getClass();
                String clazzName = clazz.getName();	
                String attr = getAttributeFromAttributeId(attributeId);
                java.beans.PropertyDescriptor propDesc = new java.beans.PropertyDescriptor(attr, clazz);
                java.lang.reflect.Method method = propDesc.getReadMethod();
                Object attrValObj = method.invoke(obj, null);

                //??Special kludge to handle the fact that current server returns value instead of id
                //for ClassificationNode for objectType
                if (attr.equals("objectType")) {
                    //on writes objectType is often not specified
                    if (attrValObj == null) {
                        attrValObj = clazzName.substring(clazzName.lastIndexOf('.', clazzName.length())+1, clazzName.length());
                    }
                    String attrValStr = (String)attrValObj;                                
                    if (!(attrValStr.startsWith("urn:uuid:"))) {
                        org.oasis.ebxml.registry.bindings.rim.ClassificationNodeType node = com.sun.ebxml.registry.query.QueryManagerImpl.getClassificationNode("/urn:uuid:3188a449-18ac-41fb-be9f-99a1adca02cb/%"+attrValStr);
                        attrValObj = node.getId();
                    }
                }

                AttributeValue attrVal = makeAttribute(attrValObj, type);
                return makeBag(attrVal);
            }
            catch (java.lang.reflect.InvocationTargetException e) {
                e.printStackTrace();
            }
            catch (java.beans.IntrospectionException e) {
                e.printStackTrace();
            }
            catch (java.lang.IllegalAccessException e) {
                e.printStackTrace();
            }                        
            catch (java.text.ParseException e) {
                e.printStackTrace();
            }                        
            catch (com.sun.xacml.ParsingException e) {
                e.printStackTrace();
            }                        
            catch (java.net.URISyntaxException e) {
                e.printStackTrace();
            }
            catch (com.sun.ebxml.registry.RegistryException e) {
                e.printStackTrace();
            }
        }
               
        return res;
    }
    
    private Object getObject(EvaluationCtx context, String attributeId) {
        Object obj = null;
        try {            
            EvaluationResult res1 = context.getResourceAttribute(new URI(ObjectAttribute.identifier), (new URI(attributeId)), null);
            AttributeValue attrValue = res1.getAttributeValue();
            BagAttribute bagAttr = (BagAttribute)attrValue;
            
            if (bagAttr.size() == 1) {
                java.util.Iterator iter = bagAttr.iterator();
                ObjectAttribute objAttr = (ObjectAttribute)iter.next();
                if (objAttr != null) {
                    obj = objAttr.getValue();
                }
            }
        }
        catch (java.net.URISyntaxException e) {
            e.printStackTrace();
        }
        
        return obj;
    }
        
    /**
     * Parses the attribute name from a URI rep of the Attribute id
     * If input is: "urn:oasis:names:tc:ebxml-regrep:2.5:rim:acp:resource:objectType"
     * then return value will be "objectType".
     **/
    private String getAttributeFromAttributeId(URI attributeId) {
        String attr = null;
               
        String attrIdStr = attributeId.toString();
        attr = attrIdStr.substring(attrIdStr.lastIndexOf(':')+1, attrIdStr.length());
        
        return attr;
    }

    /**
     * Makes an AttributeValue from Object param using the attrType param and 
     * the mapping specified in ebRIM between RIM types and XACML data types.
     */
    private AttributeValue makeAttribute(Object attrValObj, URI attrType) throws com.sun.xacml.ParsingException, java.net.URISyntaxException, java.text.ParseException {
        AttributeValue val=null;
        String attrTypeStr = attrType.toString();
        
        if (attrTypeStr.equals(com.sun.xacml.attr.BooleanAttribute.identifier)) {
            val = com.sun.xacml.attr.BooleanAttribute.getInstance(attrValObj.toString());
        }
        else if (attrTypeStr.equals(com.sun.xacml.attr.StringAttribute.identifier)) {
            val = com.sun.xacml.attr.StringAttribute.getInstance(attrValObj.toString());
        } 
        else if (attrTypeStr.equals(com.sun.xacml.attr.AnyURIAttribute.identifier)) {
            val = com.sun.xacml.attr.AnyURIAttribute.getInstance(attrValObj.toString());
        } 
        else if (attrTypeStr.equals(com.sun.xacml.attr.IntegerAttribute.identifier)) {
            val = com.sun.xacml.attr.IntegerAttribute.getInstance(attrValObj.toString());
        } 
        else if (attrTypeStr.equals(com.sun.xacml.attr.DateTimeAttribute.identifier)) {
            val = com.sun.xacml.attr.DateTimeAttribute.getInstance(attrValObj.toString());
        }
        
        return val;
    }
    
    
    /**
     * Private helper that generates a new processing error status and
     * includes the given string.
     */
    private EvaluationResult makeProcessingError(String message) {
        java.util.ArrayList code = new java.util.ArrayList();
        code.add(com.sun.xacml.ctx.Status.STATUS_PROCESSING_ERROR);
        return new EvaluationResult(new com.sun.xacml.ctx.Status(code, message));
    }

    /**
     * Private helper that makes a bag containing only the given attribute.
     */
    private EvaluationResult makeBag(AttributeValue attribute) {
        Set set = new java.util.HashSet();
        set.add(attribute);

        BagAttribute bag = new BagAttribute(attribute.getType(), set);

        return new EvaluationResult(bag);
    }

    /**
     * Private helper that makes a bag containing only the given attribute.
     */
    private EvaluationResult makeBag(URI type, Set attributeValues) {
        BagAttribute bag = new BagAttribute(type, attributeValues);

        return new EvaluationResult(bag);
    }
}
