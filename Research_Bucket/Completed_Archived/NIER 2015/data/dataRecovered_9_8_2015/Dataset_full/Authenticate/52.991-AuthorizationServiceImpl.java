/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/server/security/authorization/AuthorizationServiceImpl.java,v 1.22 2004/03/31 19:36:59 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.security.authorization;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.OMARException;
import org.freebxml.omar.common.RegistryException;
import org.freebxml.omar.server.common.RegistryProperties;
import org.freebxml.omar.server.persistence.PersistenceManager;
import org.freebxml.omar.server.persistence.PersistenceManagerFactory;
import org.freebxml.omar.server.security.UnauthorizedRequestException;
import org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl;
import org.oasis.ebxml.registry.bindings.lcm.AddSlotsRequest;
import org.oasis.ebxml.registry.bindings.lcm.ApproveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.DeprecateObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.RemoveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.RemoveSlotsRequest;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.UpdateObjectsRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.query.GetContentRequest;
import org.oasis.ebxml.registry.bindings.rim.AuditableEventType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.UserType;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.PDP;
import com.sun.xacml.PDPConfig;
import com.sun.xacml.attr.AnyURIAttribute;
import com.sun.xacml.attr.AttributeDesignator;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.cond.FunctionFactory;
import com.sun.xacml.ctx.Attribute;
import com.sun.xacml.ctx.RequestCtx;
import com.sun.xacml.ctx.ResponseCtx;
import com.sun.xacml.ctx.Result;
import com.sun.xacml.ctx.Status;
import com.sun.xacml.ctx.Subject;
import com.sun.xacml.finder.AttributeFinder;
import com.sun.xacml.finder.AttributeFinderModule;
import com.sun.xacml.finder.PolicyFinder;
import com.sun.xacml.finder.impl.CurrentEnvModule;


/**
 * AuthorizationService implementation for the ebxml Registry.
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class AuthorizationServiceImpl {
    /**
     * @link
     * @shapeType PatternLink
     * @pattern Singleton
     * @supplierRole Singleton factory
     */

    /*# private AuthorizationServiceImpl _ authorizationServiceImpl; */
    private static AuthorizationServiceImpl instance = null;

    //Not adding javadoc comments as these will go away soon.
    public static final int SUBMIT_OBJECTS_REQUEST = 0;
    public static final int UPDATE_OBJECTS_REQUEST = 1;
    public static final int REMOVE_OBJECTS_REQUEST = 2;
    public static final int APPROVE_OBJECTS_REQUEST = 3;
    public static final int DEPRECATE_OBJECTS_REQUEST = 4;
    public static final int ADD_SLOTS_REQUEST = 5;
    public static final int REMOVE_SLOTS_REQUEST = 6;
    public static final int ADHOC_QUERY_REQUEST = 7;
    public static final int GET_CONTENT_REQUEST = 8;

    /** The action-id action attribute from V3 spec.*/
    public static final String ACTION_ATTRIBUTE_ID = "urn:oasis:names:tc:xacml:1.0:action:action-id";

    /** The owner resource attribute from V3 spec.*/
    public static final String RESOURCE_ATTRIBUTE_OWNER = "urn:oasis:names:tc:ebxml-regrep:3.0:rim:acp:resource:owner";

    /** The user subject attribute specific to ebxmlrr (not from V3 spec).*/
    public static final String RESOURCE_ATTRIBUTE_REQUEST = "urn:oasis:names:tc:ebxml-regrep:3.0:rim:acp:resource:request";

    /** The subject-id subject attribute from XACML 1.0 spec. Should be in XACML impl??*/
    public static final String SUBJECT_ATTRIBUTE_ID = "urn:oasis:names:tc:xacml:1.0:subject:subject-id";

    /** The role subject attribute from V3 spec.*/
    public static final String SUBJECT_ATTRIBUTE_ROLES = "urn:oasis:names:tc:ebxml-regrep:3.0:rim:acp:subject:roles";

    /** The role subject attribute from V3 spec.*/
    public static final String SUBJECT_ATTRIBUTE_GROUPS = "urn:oasis:names:tc:ebxml-regrep:3.0:rim:acp:subject:groups";

    /** The user subject attribute specific to ebxmlrr (not from V3 spec).*/
    public static final String SUBJECT_ATTRIBUTE_USER = "urn:oasis:names:tc:ebxml-regrep:3.0:rim:acp:subject:user";

    /** The create action from V3 spec.*/
    public static final String ACTION_CREATE = "create";

    /** The read action from V3 spec.*/
    public static final String ACTION_READ = "read";

    /** The update action from V3 spec.*/
    public static final String ACTION_UPDATE = "update";

    /** The delete action from V3 spec.*/
    public static final String ACTION_DELETE = "delete";

    /** The approve action from V3 spec.*/
    public static final String ACTION_APPROVE = "approve";

    /** The deprecate action from V3 spec.*/
    public static final String ACTION_DEPRECATE = "deprecate";

    /** The undeprecate action from V3 spec.*/
    public static final String ACTION_UNDEPRECATE = "undeprecate";

    /** The reference action from V3 spec.*/
    public static final String ACTION_REFERENCE = "reference";
    
    public static final String CANONICAL_ID_NODE_REGISTRY_ADMINISTRATOR = "urn:uuid:970eeed9-1e58-4e97-bd82-eff3651998c2";
    public static final String CANONICAL_ID_NODE_SUBJECT_ROLE = "urn:uuid:41ce5ef5-2117-4304-baf5-feb35295c1c1";
    public static final String CANONICAL_ID_NODE_SUBJECT_GROUP = "urn:uuid:7c07beae-c1c6-4a52-b1db-d3cf9b501b75";
    
    public static final String PROP_REGISTRY_REQUEST = "org.freebxml.omar.server.security.authorization.RegistryRequest";

    /** The standard namespace where all the ebRIM spec-defined functions live */
    public static final String FUNCTION_NS = "urn:oasis:names:tc:ebxml-regrep:3.0:rim:acp:function:";
    
    private static BindingUtility bu = BindingUtility.getInstance();
    private PDP pdp = null;
    private Log log = LogFactory.getLog(this.getClass());
    String idForDefaultACP = RegistryProperties.getInstance().getProperty("omar.security.authorization.defaultACP");
    private AuthenticationServiceImpl ac = AuthenticationServiceImpl.getInstance();

    /**
     * Class Constructor. Protected and only used by getInstance()
     *
     */
    protected AuthorizationServiceImpl() {
    }

    /**
     * Gets the singleton instance as defined by Singleton pattern.
     *
     * @return the singleton instance
     *
     */
    public static AuthorizationServiceImpl getInstance() {
        if (instance == null) {
            synchronized (AuthorizationServiceImpl.class) {
                if (instance == null) {
                    instance = new AuthorizationServiceImpl();
                    instance.initialize();
                }
            }
        }

        return instance;
    }

    private void initialize() {
        // Add any custom functions to XACML engine
        FunctionFactory.addTargetFunction(new ClassificationNodeCompare());

        // Add any custom PolicyFinderModules to XACML engine
        RegistryPolicyFinderModule policyModule = new RegistryPolicyFinderModule();
        PolicyFinder policyFinder = new PolicyFinder();
        Set policyModules = new HashSet();
        policyModules.add(policyModule);
        policyFinder.setModules(policyModules);

        // Add any custom AttributeFinderModules to XACML engine
        RegistryAttributeFinderModule regAttrFinderModule = new RegistryAttributeFinderModule();
        Set customAttributeFinderModules = loadCustomAttributeFinderModules();
        CurrentEnvModule envModule = new CurrentEnvModule();
        List attrModules = new ArrayList();
        attrModules.add(regAttrFinderModule);
        attrModules.addAll(customAttributeFinderModules);
        attrModules.add(envModule);

        AttributeFinder attrFinder = new AttributeFinder();
        attrFinder.setModules(attrModules);

        pdp = new PDP(new PDPConfig(attrFinder, policyFinder, null));
    }
    
    /**
      *
      * @return
      */    
    private Set loadCustomAttributeFinderModules() {
        HashSet customAFMs = new HashSet();
        String customAFMList = RegistryProperties.getInstance().getProperty("omar.security.authorization.customAttributeFinderModules");
        if (customAFMList != null) {
            StringTokenizer st = new StringTokenizer(customAFMList, ",");
            while (st.hasMoreTokens()) {
                String customAFMClassName = st.nextToken();
                try {
                    AttributeFinderModule afm = (AttributeFinderModule)Class.forName(customAFMClassName).newInstance();
                    customAFMs.add(afm);
                    log.debug("Loaded custom attribute finder module '" + customAFMClassName + "'");
                }
                catch (Throwable t) {
                    log.warn("Failed to load custom attribute finder module '" +
                        customAFMClassName + "'. Exception: " + t.getMessage());
                }
            }
        }
        return customAFMs;
    }

    /** Check whether the user is authorised to make requests on the RegistryObjects
      *
      * @throws UnauthorizedRequestException 
      *     if the user is not authorized to make the requests on the objects
      */
    public AuthorizationResult checkAuthorization(UserType user, List ids, int requestType)
        throws RegistryException 
    {
        String userId = user.getId();
        AuthorizationResult result = new AuthorizationResult(userId);

        boolean isAdmin = ac.isRegistryAdministrator(user);

        if (isAdmin) {
            // The user is administrator. He can do anything.
        }
        else if ((requestType == ADHOC_QUERY_REQUEST) ||
                (requestType == GET_CONTENT_REQUEST)) 
        {
            // Everyone can make AdhocQueryRequest or GET_CONTENT_REQUEST
        } 
        else if ((requestType == SUBMIT_OBJECTS_REQUEST) &&
                !userId.equals(AuthenticationServiceImpl.ALIAS_REGISTRY_GUEST)) 
        {
            // non-guest can make SubmitObjectsRequest
        } 
        else if ((requestType == UPDATE_OBJECTS_REQUEST) ||
                (requestType == REMOVE_OBJECTS_REQUEST) ||
                (requestType == APPROVE_OBJECTS_REQUEST) ||
                (requestType == DEPRECATE_OBJECTS_REQUEST) ||
                (requestType == ADD_SLOTS_REQUEST) ||
                (requestType == REMOVE_SLOTS_REQUEST)) 
        {
            // Registry guest is not allowed to make these requests
            if (userId.equals(AuthenticationServiceImpl.ALIAS_REGISTRY_GUEST)) {
                // Should we hardcode who is registry guest ??????
                result.addDeniedResources(ids);
            } 
            else {
                // Check whether the user is the owner
                PersistenceManager pm = PersistenceManagerFactory.getInstance().getPersistenceManager();
                HashMap ownersMap = pm.getOwnersMap(ids);
                Iterator idIter = ids.iterator();
                while (idIter.hasNext()) {
                    String id = (String) idIter.next();
                    String ownerId = (String) ownersMap.get(id);

                    if (ownerId == null) {
                        throw new RegistryException("Owners not found for object " + id);
                    }

                    if (!ownerId.equals(userId)) {
                        // the user is not the owner of the object
                        result.addDeniedResource(id);
                    }
                    else {
                        result.addPermittedResource(id);
                    }
                }
            }
        } 
        else {
            throw new RegistryException("InvalidRequest: Unknown request");
        }
        
        return result;
    }
    

    /** Check if user is authorized to perform specified request using V3 specification.
      * <p>
      * Check if the specified User (requestor) is authorized to make this request or not.
      * The initial subject lists contains the object in the request is a resource.
      * The primary action is determined by the type of request. In addition
      *
      * <ul>
      * <li>
      * <b><i>AdhocQueryRequest: </i></b>
      *     Process query as normal and then filter out objects that should 
      *     not be visible to the client.
      * </li>
      * <li>
      * <b><i>GetContentRequest: </i></b>
      *     Throw ObjectNotFoundException if object client should not be able 
      *		to see object. (document in V3 spec??)
      * </li>
      * <li>
      * <b><i>ApproveObjectRequest: </i></b>
      *     Check if subject is authorized for the approve action.
      * </li>
      * <li>
      * <b><i>Deprecate/UndeprecateRequest: </i></b>
      *     Check if subject is authorized for the deprecate/undeprecate action.
      * </li>
      * <li>
      * <b><i>RemoveObjectRequest: </i></b>
      *     Check if subject is authorized for the delete action.
      * </li>
      * <li>
      * <b><i>SubmitObjectsRequest/UpdateObjectsRequest: </i></b>
      *     Check if subject authorized for the create action. Check any 
      *     referenced objects and see if their policies allows reference action.
      * </li>
      * </ul>
      *  
      * @todo Do we need any new Attribute types by Extending AttributeValue (have string URI etc.)??
      * @todo Do we need any new functions??
      *
      * @param user
      * @param registryRequest
      * @throws RegistryException
      */
    public AuthorizationResult checkAuthorizationV3(UserType user, Object registryRequest, List identifiables)
        throws RegistryException 
    {
        try {
            String userId = user.getId();
            AuthorizationResult authRes = new AuthorizationResult(userId);

            boolean isAdmin = ac.isRegistryAdministrator(user);
            if (isAdmin) {
                // Allow RegistryAdmin role all privileges
                return authRes;
            }

            Set subjects = new HashSet();
            Set actions = new HashSet();
            Set environment = new HashSet();
            Attribute actionAttr = null;
            String ownerId = null;
            boolean readOnly = false;

            // Determine the action attributes.
            if (registryRequest instanceof AdhocQueryRequest) {
                actionAttr = new Attribute(new URI(ACTION_ATTRIBUTE_ID),
                        new URI(StringAttribute.identifier), null, null,
                        new StringAttribute(ACTION_READ));
                readOnly = true;
            } 
            else if (registryRequest instanceof GetContentRequest) {
                actionAttr = new Attribute(new URI(ACTION_ATTRIBUTE_ID),
                        new URI(StringAttribute.identifier), null, null,
                        new StringAttribute(ACTION_READ));
                readOnly = true;
            } 
            else if (registryRequest instanceof SubmitObjectsRequest) {
                ownerId = user.getId();
                actionAttr = new Attribute(new URI(ACTION_ATTRIBUTE_ID),
                        new URI(StringAttribute.identifier), null, null,
                        new StringAttribute(ACTION_CREATE));
            } 
            else if (registryRequest instanceof ApproveObjectsRequest) {
                actionAttr = new Attribute(new URI(ACTION_ATTRIBUTE_ID),
                        new URI(StringAttribute.identifier), null, null,
                        new StringAttribute(ACTION_APPROVE));
            } 
            else if (registryRequest instanceof DeprecateObjectsRequest) {
                actionAttr = new Attribute(new URI(ACTION_ATTRIBUTE_ID),
                        new URI(StringAttribute.identifier), null, null,
                        new StringAttribute(ACTION_DEPRECATE));
            } 
            else if (registryRequest instanceof UpdateObjectsRequest) {
                actionAttr = new Attribute(new URI(ACTION_ATTRIBUTE_ID),
                        new URI(StringAttribute.identifier), null, null,
                        new StringAttribute(ACTION_UPDATE));
            } 
            else if (registryRequest instanceof RemoveObjectsRequest) {
                actionAttr = new Attribute(new URI(ACTION_ATTRIBUTE_ID),
                        new URI(StringAttribute.identifier), null, null,
                        new StringAttribute(ACTION_DELETE));
            } 
            else if (registryRequest instanceof AddSlotsRequest) {
                //??Document in spec the mapping
                actionAttr = new Attribute(new URI(ACTION_ATTRIBUTE_ID),
                        new URI(StringAttribute.identifier), null, null,
                        new StringAttribute(ACTION_UPDATE));
            } 
            else if (registryRequest instanceof RemoveSlotsRequest) {
                //??Document in spec the mapping
                actionAttr = new Attribute(new URI(ACTION_ATTRIBUTE_ID),
                        new URI(StringAttribute.identifier), null, null,
                        new StringAttribute(ACTION_UPDATE));
            } 
            else {
                throw new RegistryException("InvalidRequest: Unknown request " +
                    registryRequest.getClass().getName());
            }
            actions.add(actionAttr);

            // Init subject attributes
            Set userSubjectAttributes = new HashSet();
            Attribute idSubjectAttr = new Attribute(new URI(SUBJECT_ATTRIBUTE_ID),
                                                    new URI(AnyURIAttribute.identifier), 
                                                    null, null,
                                                    new AnyURIAttribute(new URI(userId)));
            userSubjectAttributes.add(idSubjectAttr);
            Attribute userSubjectAttr = new Attribute(new URI(SUBJECT_ATTRIBUTE_USER),
                                                      new URI(ObjectAttribute.identifier), 
                                                      null, null,
                                                      new ObjectAttribute(user));
            userSubjectAttributes.add(userSubjectAttr);
            Subject userSubject = new Subject(new URI(AttributeDesignator.SUBJECT_CATEGORY_DEFAULT), 
                                              userSubjectAttributes);
            subjects.add(userSubject);

            // Encapsulate entire request as a pseudo-resource attribute so it
            // is available to RegistryAttributeFinderModule
            Attribute requestResourceAttr = new Attribute(new URI(RESOURCE_ATTRIBUTE_REQUEST), 
                                                          new URI(ObjectAttribute.identifier), 
                                                          null, null, 
                                                          new ObjectAttribute(registryRequest));

            // Iterate over each resource and see if action is authorized on the
            // resource by the subject
            List ids = new ArrayList();
            if (registryRequest instanceof AdhocQueryRequest) {
                // TO DO: This should be moved into BindingUtility.getIdsFromRequest(), 
                //        but it requires access to the 'user' object. Can the
                //        BindingUtility.getIdsFromRequest() be modified to accept
                //        a UserType parameter? Could the user be obtained from
                //        somewhere else?
                // Execute the query and add the ids of the objects in the result set
                
                //For AdhocQueryRequest query is already done and result is in
                //identifiables. Now do access control check on identifiables.
                Iterator iter = identifiables.iterator();
                while (iter.hasNext()) {
                    RegistryObjectType ro = (RegistryObjectType)iter.next();
                    ids.add(ro.getId());
                }
            }
            else {
                ids.addAll(bu.getIdsFromRequest(registryRequest));
            }
            Iterator idsIter = ids.iterator();
            while (idsIter.hasNext()) {
                String id = (String) idsIter.next();
                if (id != null) {
                    if ((!readOnly) && (id.equals(idForDefaultACP))) {
                        // Auth check for defaultACP is special and requires that
                        // it is submitted by RegistryAdministrator role.
                        // Note this will be generalized when we have better 
                        // Role Based Access Control (RBAC) support
                        if (!isAdmin) {
                            throw new RegistryException("InvalidRequest: Only Users " +
                                "with RegistryAdministrator role can submit default " +
                                "Access Control Policy file with id='" +
                                id + "'");
                        }
                    } 
                    else {
                        try {
                            checkAuthorizationV3(userId, id, ownerId, subjects,
                                actions, environment, requestResourceAttr);
                            authRes.addPermittedResource(id);
                        }
                        catch (UnauthorizedRequestException ure) {
                            authRes.addDeniedResource(id);
                        }
                        catch (RegistryException re) {
                            if (re.getCause() instanceof UnauthorizedRequestException) {
                                authRes.addDeniedResource(id);
                            }
                            else {
                                throw re;
                            }
                        }
                    }
                } 
                else {
                    int i = 0;
                }
            }
            
            return authRes;
        } 
        catch (URISyntaxException e) {
            throw new RegistryException(e);
        } 
        catch (OMARException e) {
            throw new RegistryException(e);
        }
    }

    /** Check if subject is authorized to perform action on the resource
      * RegistryObject.
      *
      * @param userId
      *     UUID of the user making the request.
      * @param id
      *     UUID of the resource being accessed.
      * @param ownerId
      *     UUID of the user who owns the resource. If this is null, the
      *     database will be queried to determine the owner's id.
      * @param subjects
      * @param actions
      *     A list of xacml action Attributes representing the action being
      *     requested.
      * @param environment
      * @param requestResourceAttr
      * @throws RegistryException
      */
    private void checkAuthorizationV3(String userId, 
                                      String id, 
                                      String ownerId,
                                      Set subjects, 
                                      Set actions, 
                                      Set environment, 
                                      Attribute requestResourceAttr)
        throws RegistryException 
    {
        if (ownerId == null) {
            ownerId = getRegistryObjectOwnerId(id);
        }

        try {
            Attribute idResourceAttr = new Attribute
                (new URI(EvaluationCtx.RESOURCE_ID),
                new URI(AnyURIAttribute.identifier), null, null,
                new AnyURIAttribute(new URI(id)));

            Attribute ownerResourceAttr = new Attribute
                (new URI(RESOURCE_ATTRIBUTE_OWNER),
                new URI(AnyURIAttribute.identifier), null, null,
                new AnyURIAttribute(new URI(ownerId)));

            Set resourceAttributes = new HashSet();
            resourceAttributes.add(idResourceAttr);
            resourceAttributes.add(ownerResourceAttr);
            resourceAttributes.add(requestResourceAttr);

            RequestCtx req = new RequestCtx(subjects,
                    resourceAttributes, actions, environment);
            
            ResponseCtx resp = pdp.evaluate(req);

            Set results = resp.getResults();

            // Expecting only one Result
            Result result = (Result)results.iterator().next();
            Status status = result.getStatus();
            log.info("status.message = " + status.getMessage());

            // TO DO: Do we need to check status here?
            
            int decision = result.getDecision();
            if (!(decision == Result.DECISION_PERMIT)) {
                Attribute actionAttr = (Attribute)((actions.toArray())[0]);
                String actionStr = ((StringAttribute)actionAttr.getValue()).getValue();
                throw new UnauthorizedRequestException(id, userId, actionStr);
            }
        } 
        catch (URISyntaxException e) {
            throw new RegistryException(e);
        }
    }

    /** Gets the id of the user who owns the registry object with the specified
      * id.
      *
      * @param id
      *     The UUID of a RegistryObject.
      * @return
      *     The UUID of the user who owns the registry object.
      * @throws RegistryException
      *     Thrown if no owner can be found for the registry object.
      */
    private String getRegistryObjectOwnerId(String id) throws RegistryException {
        
        List ids = new ArrayList();
        ids.add(id);

        PersistenceManager pm = 
            PersistenceManagerFactory.getInstance().getPersistenceManager();
        HashMap ownersMap = pm.getOwnersMap(ids);
        String ownerId = (String)ownersMap.get(id);

        try {
            if (ownerId == null) {
                String className = "Unknown";
                ObjectRefType oref = bu.rimFac.createObjectRef();
                oref.setId(id);
                RegistryObjectType ro = pm.getRegistryObject(oref);
                
                if (ro != null) {
                    className = ro.getClass().getName();
                    //AuditableEvents owner is underfinhed and is implicitly RegistryOperator
                    if (ro instanceof AuditableEventType) {
                        ownerId = ac.ALIAS_REGISTRY_OPERATOR;
                    } else {
                        throw new RegistryException("Owners not found for object " + id + "of type " + className);
                    }
                } else {
                    throw new RegistryException("Object not found in registry" + id);
                }
                
            }
        }
        catch (JAXBException e) {
            throw new RegistryException(e);
        }

        return ownerId;
    }

    public AuthorizationResult checkAuthorization(UserType user, Object registryRequest)
        throws RegistryException {
        return checkAuthorization(user, registryRequest, null);    
    }    
    
    /**
     * Check if user is authorized to perform specified request.
     **/
    public AuthorizationResult checkAuthorization(UserType user, Object registryRequest, List identifiables)
        throws RegistryException 
    {
        AuthorizationResult authResult;
        
        String userId = user.getId();
        
        log.debug("Checking authorization for userId=" + userId + "...");
        
        authResult = checkAuthorizationV3(user, registryRequest, identifiables);
                
        log.debug("userId=" + userId + " is " + 
            (authResult.getResult() == AuthorizationResult.PERMIT_NONE ? "not " : "") +
            "allowed to perform the requested operation.");
        
        return authResult;
    }

}
/*
 * $Header: /cvsroot/sino/ebxmlrr/src/share/com/sun/ebxml/registry/security/authorization/AuthorizationServiceImpl.java,v 1.18 2003/11/19 00:37:59 uday_s Exp $
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

import com.sun.ebxml.registry.RegistryException;
import com.sun.ebxml.registry.security.authentication.AuthenticationServiceImpl;
import com.sun.xacml.attr.AnyURIAttribute;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.ctx.Attribute;
import com.sun.xacml.ctx.Subject;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.oasis.ebxml.registry.bindings.rim.User;

/**
 * AuthorizationService implementation for the ebxml Registry.
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class AuthorizationServiceImpl {
    
    /**
     * @link
     * @shapeType PatternLink
     * @pattern Singleton
     * @supplierRole Singleton factory
     */
    /*# private AuthorizationServiceImpl _ authorizationServiceImpl; */
    private static AuthorizationServiceImpl instance = null;
    
    //Not adding javadoc comments as these will go away soon.
    public static final int SUBMIT_OBJECTS_REQUEST = 0;
    public static final int UPDATE_OBJECTS_REQUEST = 1;
    public static final int REMOVE_OBJECTS_REQUEST = 2;
    public static final int APPROVE_OBJECTS_REQUEST = 3;
    public static final int DEPRECATE_OBJECTS_REQUEST = 4;
    public static final int ADD_SLOTS_REQUEST = 5;
    public static final int REMOVE_SLOTS_REQUEST = 6;
    public static final int ADHOC_QUERY_REQUEST = 7;
    public static final int GET_CONTENT_REQUEST = 8;

    /** The action-id action attribute from V3 spec.*/
    public static final String ACTION_ATTRIBUTE_ID = "urn:oasis:names:tc:xacml:1.0:action:action-id";

    /** The owner resource attribute from V3 spec.*/
    public static final String RESOURCE_ATTRIBUTE_OWNER = "urn:oasis:names:tc:ebxml-regrep:2.5:rim:acp:resource:owner";

    /** The user subject attribute specific to ebxmlrr (not from V3 spec).*/
    public static final String RESOURCE_ATTRIBUTE_REQUEST = "urn:oasis:names:tc:ebxml-regrep:2.5:rim:acp:resource:request";
    
    /** The subject-id subject attribute from XACML 1.0 spec. Should be in XACML impl??*/
    public static final String SUBJECT_ATTRIBUTE_ID = "urn:oasis:names:tc:xacml:1.0:subject:subject-id";
    
    /** The role subject attribute from V3 spec.*/
    public static final String SUBJECT_ATTRIBUTE_ROLES = "urn:oasis:names:tc:ebxml-regrep:2.5:rim:acp:subject:roles";
    
    /** The role subject attribute from V3 spec.*/
    public static final String SUBJECT_ATTRIBUTE_GROUPS = "urn:oasis:names:tc:ebxml-regrep:2.5:rim:acp:subject:groups";
    
    /** The user subject attribute specific to ebxmlrr (not from V3 spec).*/
    public static final String SUBJECT_ATTRIBUTE_USER = "urn:oasis:names:tc:ebxml-regrep:2.5:rim:acp:subject:user";
    
    /** The create action from V3 spec.*/
    public static final String ACTION_CREATE = "create";

    /** The read action from V3 spec.*/
    public static final String ACTION_READ = "read";

    /** The update action from V3 spec.*/
    public static final String ACTION_UPDATE = "update";

    /** The delete action from V3 spec.*/
    public static final String ACTION_DELETE = "delete";

    /** The approve action from V3 spec.*/
    public static final String ACTION_APPROVE = "approve";

    /** The deprecate action from V3 spec.*/
    public static final String ACTION_DEPRECATE = "deprecate";

    /** The undeprecate action from V3 spec.*/
    public static final String ACTION_UNDEPRECATE = "undeprecate";

    /** The reference action from V3 spec.*/
    public static final String ACTION_REFERENCE = "reference";
    
    public static final String CANONICAL_ID_NODE_REGISTRY_ADMINISTRATOR =
        "urn:uuid:970eeed9-1e58-4e97-bd82-eff3651998c2";
    
    public static final String CANONICAL_ID_NODE_SUBJECT_ROLE =
        "urn:uuid:41ce5ef5-2117-4304-baf5-feb35295c1c1";
    
    public static final String CANONICAL_ID_NODE_SUBJECT_GROUP =
        "urn:uuid:7c07beae-c1c6-4a52-b1db-d3cf9b501b75";
    
    public static final String PROP_REGISTRY_REQUEST = 
        "com.sun.ebxml.registry.security.authorization.RegistryRequest";

    /**
     * The standard namespace where all the ebRIM spec-defined functions live
     */
    public static final String FUNCTION_NS =
        "urn:oasis:names:tc:ebxml-regrep:2.5:rim:acp:function:";
    
    
    private static com.sun.ebxml.registry.util.BindingUtility bu = com.sun.ebxml.registry.util.BindingUtility.getInstance();
    private com.sun.xacml.PDP pdp = null;
    
    private org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(this.getClass());    
    boolean useXACML = Boolean.valueOf(com.sun.ebxml.registry.util.RegistryProperties.getInstance().getProperty("ebxmlrr.security.authorization.useXACML")).booleanValue();
    String idForDefaultACP = com.sun.ebxml.registry.util.RegistryProperties.getInstance().getProperty("ebxmlrr.security.authorization.defaultACP");
    
    /**
     * Class Constructor. Protected and only used by getInstance()
     *
     */
    protected AuthorizationServiceImpl(){
    }
    
    /**
     * Gets the singleton instance as defined by Singleton pattern.
     *
     * @return the singleton instance
     *
     */
    public static AuthorizationServiceImpl getInstance(){
        if (instance == null) {
            synchronized(com.sun.ebxml.registry.security.authorization.AuthorizationServiceImpl.class) {
                if (instance == null) {
                    instance = new com.sun.ebxml.registry.security.authorization. AuthorizationServiceImpl();
                    instance.initialize();
                }
            }
        }
        return instance;
    }
    
    private void initialize() {
        if (useXACML) {
            //Add any custom functions to XACML engine
            try {
                com.sun.xacml.cond.FunctionFactory.addTargetFunction(new ClassificationNodeCompare());
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            //Add any custom PolicyFinderModules to XACML engine
            RegistryPolicyFinderModule policyModule = new RegistryPolicyFinderModule();        
            com.sun.xacml.finder.PolicyFinder policyFinder = new com.sun.xacml.finder.PolicyFinder();
            Set policyModules = new HashSet();
            policyModules.add(policyModule);
            policyFinder.setModules(policyModules);

            //Add any custom AttributeFinderModules to XACML engine
            RegistryAttributeFinderModule regAttrFinderModule = new RegistryAttributeFinderModule();        
            com.sun.xacml.finder.impl.CurrentEnvModule envModule = new com.sun.xacml.finder.impl.CurrentEnvModule();
            java.util.List attrModules = new ArrayList();
            attrModules.add(regAttrFinderModule);
            attrModules.add(envModule);
            com.sun.xacml.finder.AttributeFinder attrFinder = new com.sun.xacml.finder.AttributeFinder();
            attrFinder.setModules(attrModules);
            
            pdp = new com.sun.xacml.PDP(new com.sun.xacml.PDPConfig(attrFinder, policyFinder, null));
        }        
    }
    
    /**
     * Check whether the user is authorised to make requests on the RegistryObjects
     * @throws UnauthorizedRequestException if the user is not authorized to make the requests on the objects
     */
    public void checkAuthorization(User user, ArrayList ids, int requestType) throws RegistryException {
        
        String userId = user.getId();
        AuthenticationServiceImpl authc = AuthenticationServiceImpl.getInstance();
        
        boolean isAdmin = isRegistryAdministrator(user);

        // The user is administrator. He can do anything.
        if (isAdmin) {
        }
        // Everyone can make AdhocQueryRequest or GET_CONTENT_REQUEST
        else if (requestType==ADHOC_QUERY_REQUEST || requestType == GET_CONTENT_REQUEST) {
        }
        else if (requestType == SUBMIT_OBJECTS_REQUEST && !userId.equals(authc.ALIAS_REGISTRY_GUEST)) {
            // non-guest can make SubmitObjectsRequest
        }
        else if (requestType ==  UPDATE_OBJECTS_REQUEST ||
        requestType ==  REMOVE_OBJECTS_REQUEST ||
        requestType ==  APPROVE_OBJECTS_REQUEST ||
        requestType ==  DEPRECATE_OBJECTS_REQUEST ||
        requestType ==  ADD_SLOTS_REQUEST ||
        requestType ==  REMOVE_SLOTS_REQUEST) {
            
            // Registry guest is not allowed to make these requests
            if (userId.equals(authc.ALIAS_REGISTRY_GUEST)) {
                // Should we hardcode who is registry guest ??????
                throw new com.sun.ebxml.registry.security.UnauthorizedRequestException((String)ids.get(0), userId);
            }
            else {
                // Check whether the user is the owner
                com.sun.ebxml.registry.persistence.PersistenceManager pm = com.sun.ebxml.registry.persistence.PersistenceManagerImpl.getInstance();
                java.util.HashMap ownersMap = pm.getOwnersMap(ids);
                java.util.Iterator idIter = ids.iterator();
                while(idIter.hasNext()) {
                    String id = (String)idIter.next();
                    String ownerId = (String)ownersMap.get(id);
                    if (ownerId == null) {
                        throw new RegistryException("Owners not found for object " + id);
                    }
                    if (!ownerId.equals(userId)) {
                        // the user is not the owner of the object
                        throw new com.sun.ebxml.registry.security.UnauthorizedRequestException(id, userId);
                    }
                }
                
            }
        }
        else {
            throw new RegistryException("InvalidRequest: Unknown request");
        }
        
    }
    
    
    /**
     * Check if user is authorized to perform specified request using V3 specification. 
     *
     * Check if the specified User (requestor) is authorized to make this request or not.
     * The initial subject lists contains the object in the request is a resource.
     * The primary action is determined by the type of request. In addition 
     *
     * AdhocQueryRequest: process query as normal and then filter out objects that should not be visible to the client.
     * GetContentRequest: throw ObjectNotFoundException if object client should not be able to see object. (document in V3 spec??)
     * ApproveObjectRequest: check if subject is authorized for the approve action
     * Deprecate/UndeprecateRequest: check if subject is authorized for the deprecate/undeprecate action
     * RemoveObjectRequest: check if subject is authorized for the delete action
     * SubmitObjectsRequest/UpdateObjectsRequest: check if subject authorized for the create action
     *  Check any referenced objects and see if their policies allows reference action
     *
     * Do we need any new Attribute types by Extending AttributeValue (have string URI etc.)??
     * Do we need any new functions??
     *
     * @throws UnauthorizedRequestException if the user is not authorized to make the requests on the objects
     */
    public void checkAuthorizationV3(User user, Object registryRequest) throws RegistryException {
        
        try {                                    
            AuthenticationServiceImpl authc = AuthenticationServiceImpl.getInstance();
            String userId = user.getId();
            
            boolean isAdmin = isRegistryAdministrator(user);            
            
            if (isAdmin) {
                //Allow RegistryAdmin role all privileges
                return;
            }
            
            //The subjects Set contain Subject instances now
            //Subject instances are created with a HashSet of subject Attributes
            Set subjects = new HashSet();
            
            Set action = new HashSet();
            Set environment = new HashSet();
            Attribute actionAttr = null;
            String ownerId = null;
            
            ArrayList objects = bu.getIdsFromRequest(registryRequest);
            
            if (registryRequest instanceof org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest) {
                //action = new Attribute(new URI(""), URI type, String issuer, DateTimeAttribute issueInstant, AttributeValue value);
            }
            else if (registryRequest instanceof org.oasis.ebxml.registry.bindings.query.GetContentRequest) {
            }
            else if (registryRequest instanceof org.oasis.ebxml.registry.bindings.rs.SubmitObjectsRequest) {
                ownerId = user.getId();
                
                actionAttr = new Attribute(new URI(ACTION_ATTRIBUTE_ID), 
                    new URI(StringAttribute.identifier), 
                    null, 
                    null, 
                    new StringAttribute(ACTION_CREATE));
            }
            else if (registryRequest instanceof org.oasis.ebxml.registry.bindings.rs.ApproveObjectsRequest) {
                actionAttr = new Attribute(new URI(ACTION_ATTRIBUTE_ID), 
                    new URI(StringAttribute.identifier), 
                    null, 
                    null, 
                    new StringAttribute(ACTION_APPROVE));
            }
            else if (registryRequest instanceof org.oasis.ebxml.registry.bindings.rs.DeprecateObjectsRequest) {
                actionAttr = new Attribute(new URI(ACTION_ATTRIBUTE_ID), 
                    new URI(StringAttribute.identifier), 
                    null, 
                    null, 
                    new StringAttribute(ACTION_DEPRECATE));
            }
            else if (registryRequest instanceof org.oasis.ebxml.registry.bindings.rs.UpdateObjectsRequest) {
                actionAttr = new Attribute(new URI(ACTION_ATTRIBUTE_ID), 
                    new URI(StringAttribute.identifier), 
                    null, 
                    null, 
                    new StringAttribute(ACTION_UPDATE));
            }
            else if (registryRequest instanceof org.oasis.ebxml.registry.bindings.rs.RemoveObjectsRequest) {                            
                actionAttr = new Attribute(new URI(ACTION_ATTRIBUTE_ID), 
                    new URI(StringAttribute.identifier), 
                    null, 
                    null, 
                    new StringAttribute(ACTION_DELETE));
            }
            else if (registryRequest instanceof org.oasis.ebxml.registry.bindings.rs.AddSlotsRequest) {
                //??Document in spec the mapping
                actionAttr = new Attribute(new URI(ACTION_ATTRIBUTE_ID), 
                    new URI(StringAttribute.identifier), 
                    null, 
                    null, 
                    new StringAttribute(ACTION_UPDATE));
            }
            else if (registryRequest instanceof org.oasis.ebxml.registry.bindings.rs.RemoveSlotsRequest) {
                //??Document in spec the mapping
                actionAttr = new Attribute(new URI(ACTION_ATTRIBUTE_ID), 
                    new URI(StringAttribute.identifier), 
                    null, 
                    null, 
                    new StringAttribute(ACTION_UPDATE));
            }
            else {
                throw new RegistryException("InvalidRequest: Unknown request " + registryRequest.getClass().getName());
            }
            
            action.add(actionAttr);
            
            //Init subject attributes            
            Set subjectAttributesForUser = new HashSet();
            
            Attribute idSubjectAttr = new Attribute(new URI(SUBJECT_ATTRIBUTE_ID), 
                new URI(AnyURIAttribute.identifier), 
                null, 
                null, 
                new AnyURIAttribute(new URI(userId)));
                        
            Attribute userSubjectAttr = new Attribute(new URI(SUBJECT_ATTRIBUTE_USER), 
                new URI(ObjectAttribute.identifier), 
                null, 
                null, 
                new ObjectAttribute(user));
            
            subjectAttributesForUser.add(idSubjectAttr);
            subjectAttributesForUser.add(userSubjectAttr);
            //Create a SUbject instance using the subjectAttributes
            Subject subject = new Subject(subjectAttributesForUser);
            
            //Now add subject instance to HashSet subjects
            subjects.add(subject); 
            
            //Encapsuulate entire request as a pseudo-resource atttribute so it is available to RegistryAttributeFinderModule            
            Attribute requestResourceAttr = new Attribute(new URI(RESOURCE_ATTRIBUTE_REQUEST), 
                new URI(ObjectAttribute.identifier), 
                null, 
                null, 
                new ObjectAttribute(registryRequest));                                              
            
            //Iterate over each resource and see if action is authorized on the resource by the subject
            java.util.Collection ids = bu.getIdsFromRequest(registryRequest);
            java.util.Iterator idsIter = ids.iterator();
            while (idsIter.hasNext()) {
                String id = (String)idsIter.next();
                if (id != null) {
                    if (id.equals(idForDefaultACP)) {
                        //Auth check for defaultACP is special and requires that it is submitted by RegistryAdministrator role
                        //Note this will be generalized when we have better Role Based Access Control (RBAC) support
                        if (!isAdmin) {
                            throw new RegistryException("InvalidRequest: Only Users with RegistryAdministrator role can submit default Access Control Policy file with id='" + id + "'");
                        }
                    }
                    else {                        
                        checkAuthorizationV3(userId, id, ownerId, subjects, action, environment, requestResourceAttr);
                    }
                }
                else {
                    int i =0;
                }
            }
        }
        catch (java.net.URISyntaxException e) {
            throw new RegistryException(e);
        }
    }
    
    /**
     * Check if subject is authorized to perform action on the resource ro.
     **/
    private void checkAuthorizationV3(String userId, String id, String ownerId, 
    Set subjects, Set action, Set environment, Attribute requestResourceAttr) throws RegistryException {
        if (ownerId == null) {
            ownerId = getRegistryObjectOwnerId(id);
        }
         
        try {
            Attribute idResourceAttr = new Attribute(new URI(com.sun.xacml.EvaluationCtx.RESOURCE_ID), 
                new URI(AnyURIAttribute.identifier), 
                null, 
                null, 
                new AnyURIAttribute(new URI(id)));

            Attribute ownerResourceAttr = new Attribute(new URI(RESOURCE_ATTRIBUTE_OWNER), 
                new URI(AnyURIAttribute.identifier), 
                null, 
                null, 
                new AnyURIAttribute(new URI(ownerId)));

            Set resourceAttributes = new HashSet();
            resourceAttributes.add(idResourceAttr);
            resourceAttributes.add(ownerResourceAttr);
            resourceAttributes.add(requestResourceAttr);

            com.sun.xacml.ctx.RequestCtx req = new com.sun.xacml.ctx.RequestCtx(subjects, resourceAttributes, action, environment);
            com.sun.xacml.ctx.ResponseCtx resp = pdp.evaluate(req);

            Set results = resp.getResults();
            //Expecting only one Result
            com.sun.xacml.ctx.Result result = (com.sun.xacml.ctx.Result)results.iterator().next();
            com.sun.xacml.ctx.Status status = result.getStatus();
            log.info("status.message = " + status.getMessage());

            //??Need to check status here
            int decision = result.getDecision();
            if (!(decision == com.sun.xacml.ctx.Result.DECISION_PERMIT)) {
                Attribute actionAttr = (Attribute)((action.toArray())[0]);
                String actionStr = ((StringAttribute)actionAttr.getValue()).getValue();
                throw new com.sun.ebxml.registry.security.UnauthorizedRequestException(id, userId, actionStr);
            }
        }
        catch (java.net.URISyntaxException e) {
            throw new RegistryException(e);
        }
    }
    
    /**
     * Gets id for User that is owner of RegistryObject with specified id.
     **/
    private String getRegistryObjectOwnerId(String id) throws RegistryException {
        // Check whether the user is the owner
        com.sun.ebxml.registry.persistence.PersistenceManager pm = com.sun.ebxml.registry.persistence.PersistenceManagerImpl.getInstance();
        ArrayList ids = new ArrayList();
        ids.add(id);
        
        java.util.HashMap ownersMap = pm.getOwnersMap(ids);
        String ownerId = (String)ownersMap.get(id);
        
        if (ownerId == null) {
            throw new RegistryException("Owners not found for object " + id);
        }
        
        return ownerId;
    }      
    
    /**
     * Check if user is authorized to perform specified request.
     **/
    public void checkAuthorization(User user, Object registryRequest) throws RegistryException {
        if (useXACML) {
            checkAuthorizationV3(user, registryRequest);
        } else {
            checkAuthorizationV2(user, registryRequest);
        }
    }
    
    /**
     * Check if user is authorized to perform specified request using V2 specification. 
     **/
    public void checkAuthorizationV2(User user, Object registryRequest) throws RegistryException {
        
        int requestType = -1;
        
        if (registryRequest instanceof org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest) {
            requestType = ADHOC_QUERY_REQUEST;
        }
        else if (registryRequest instanceof org.oasis.ebxml.registry.bindings.query.GetContentRequest) {
            requestType = GET_CONTENT_REQUEST;
        }
        else if (registryRequest instanceof org.oasis.ebxml.registry.bindings.rs.SubmitObjectsRequest) {
            requestType = SUBMIT_OBJECTS_REQUEST;
        }
        else if (registryRequest instanceof org.oasis.ebxml.registry.bindings.rs.ApproveObjectsRequest) {
            requestType = APPROVE_OBJECTS_REQUEST;
        }
        else if (registryRequest instanceof org.oasis.ebxml.registry.bindings.rs.DeprecateObjectsRequest) {
            requestType = DEPRECATE_OBJECTS_REQUEST;
        }
        else if (registryRequest instanceof org.oasis.ebxml.registry.bindings.rs.UpdateObjectsRequest) {
            requestType = UPDATE_OBJECTS_REQUEST;
        }
        else if (registryRequest instanceof org.oasis.ebxml.registry.bindings.rs.RemoveObjectsRequest) {
            requestType = REMOVE_OBJECTS_REQUEST;
        }
        else if (registryRequest instanceof org.oasis.ebxml.registry.bindings.rs.AddSlotsRequest) {
            requestType = ADD_SLOTS_REQUEST;
        }
        else if (registryRequest instanceof org.oasis.ebxml.registry.bindings.rs.RemoveSlotsRequest) {
            requestType = REMOVE_SLOTS_REQUEST;
        }
        else {
            throw new RegistryException("InvalidRequest: Unknown request " + registryRequest.getClass().getName());
        }
        checkAuthorization(user, bu.getIdsFromRequest(registryRequest), requestType);
    }
    
    public boolean isRegistryAdministrator(User user) throws RegistryException {
        boolean isAdmin = false;
        
        
        log.info("isRegistryAdministrator: user=" + user.getId());
        org.oasis.ebxml.registry.bindings.rim.Classification[] classifications = user.getClassification();
        for (int i=0; i<classifications.length; i++) {
            org.oasis.ebxml.registry.bindings.rim.Classification classification = classifications[i];
            String classificationNodeId = bu.getObjectId(classification.getClassificationNode());
            log.info("isRegistryAdministrator: classificationNodeId=" + classificationNodeId);
            if (classificationNodeId.equals(CANONICAL_ID_NODE_REGISTRY_ADMINISTRATOR)) {
                isAdmin = true;
                break;
            }
        }
        
        log.info("isRegistryAdministrator: isAdmin=" + isAdmin);
        return isAdmin;
    }
    
    /**
     * Minimal unit test code. WIll be replaced with junit test later. 
     **/
    public static void main(String[] args) {
        //String req = "c:/osws/ebxmlrr/misc/samples/SubmitObjectsRequest_extSchemes.xml"; //Objects other than ExtrinsicObject being submitted
        String req = "c:/osws/ebxmlrr/misc/samples/SubmitObjectsRequest_SubjectRoleScheme.xml"; //only ExtrinsicObject being submitted
        //String alias = "urn:uuid:b2691323-4aad-46da-9dc7-a842b7e4b1ae"; //An non sysadmin user
        String alias = "urn:uuid:921284f0-bbed-4a4c-9342-ecaf0625f9d7"; //An sysadmin user
        
        try {            
            AuthenticationServiceImpl ac = 
                AuthenticationServiceImpl.getInstance();        

            AuthorizationServiceImpl az = AuthorizationServiceImpl.getInstance();
            User user = ac.getUserFromAlias(alias);
            java.io.File file = new java.io.File(req);
            Object request = bu.getRequestObject(file);
            az.checkAuthorization(user, request);
            
            System.err.println("Request was authorized");
            
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        
        
    }
    

}
