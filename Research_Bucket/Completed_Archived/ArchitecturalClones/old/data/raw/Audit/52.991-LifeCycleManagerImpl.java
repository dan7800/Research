/*
 * $Header: /cvsroot/sino/ebxmlrr/src/share/com/sun/ebxml/registry/lcm/LifeCycleManagerImpl.java,v 1.79 2003/05/22 12:08:04 farrukh_najmi Exp $
 */

package com.sun.ebxml.registry.lcm;

import org.oasis.ebxml.registry.bindings.rs.*;
import org.oasis.ebxml.registry.bindings.rs.types.*;
import org.oasis.ebxml.registry.bindings.rim.types.*;
import org.oasis.ebxml.registry.bindings.rim.*;

import com.sun.ebxml.registry.util.*;

import java.io.*;
import java.util.*;
import java.sql.*;
import javax.activation.*;

import com.sun.ebxml.registry.*;
import com.sun.ebxml.registry.lcm.quota.*;
import com.sun.ebxml.registry.persistence.*;
import com.sun.ebxml.registry.repository.*;
import com.sun.ebxml.registry.security.*;

import org.apache.commons.logging.*;

/**
 * Implementation of the LifeCycleManager interface
 * @see
 * @author Farrukh S. Najmi
 * @author Adrian Chong
 */
public class LifeCycleManagerImpl implements LifeCycleManager {
    
    /**
     * @link
     * @shapeType PatternLink
     * @pattern Singleton
     * @supplierRole Singleton factory
     */
    /*# private LifeCycleManagerImpl _objectManagerImpl; */
    private static LifeCycleManagerImpl instance = null;
    
    /**
     *
     * @associates <{com.sun.ebxml.registry.persistence.PersistenceManagerImpl}>
     */
    PersistenceManager pm = PersistenceManagerImpl.getInstance();
    BindingUtility bu = BindingUtility.getInstance();
    QuotaServiceImpl qs = QuotaServiceImpl.getInstance();
    Utility util = Utility.getInstance();
    RepositoryManager rm = RepositoryManagerFactory.getInstance().getRepositoryManager();
    UUIDFactory uf = UUIDFactory.getInstance();
    
    /**
     *
     * @associates <{com.sun.ebxml.registry.persistence.ContentIndexingManager}>
     */
    ContentIndexingManager cim = ContentIndexingManager.getInstance();
    
    private org.apache.commons.logging.Log log = LogFactory.getLog(this.getClass());

    
    protected LifeCycleManagerImpl(){}
    
    public static LifeCycleManagerImpl getInstance(){
        if (instance == null) {
            synchronized(LifeCycleManagerImpl.class) {
                if (instance == null) {
                    instance = new LifeCycleManagerImpl();
                }
            }
        }
        return instance;
    }
    
    /**
     * Submits one or more RegistryObjects and one or more repository items.
     * <br>
     * <br>
     * Note: One more special feature that is not in the RS spec. version 2.
     * The SubmitObjectsRequest allows updating objects.If a object of a particular
     * id already exist, it is updated instead of trying to be inserted.
     * @param idToRepositoryItemMap is a HashMap with key that is id of a RegistryObject and value that is a RepositoryItem instance.
     */
    public RegistryResponse submitObjects(User user, SubmitObjectsRequest req, HashMap idToRepositoryItemMap) throws RegistryException {
        
        RegistryResponse resp = new RegistryResponse();
        RegistryErrorList el = new RegistryErrorList();
        try {
            LeafRegistryObjectList objs = req.getLeafRegistryObjectList();
            ArrayList al = bu.getRegistryObjectList(objs);
            
            //Replace temporary ids with proper UUID.
            
            HashMap idMap = new HashMap();
            fixTemporaryIds(al, idMap);
            
             /*
              * check whether the referenced object for
              * Association/Classification/ClassificationNode/Organization/ExternalIdentifier
              * exist within the request document and whether REs has objectStatus,
              * majorVersion or minorVersion.
              */
            checkObjects(al, el);
            
            if (idToRepositoryItemMap != null) {
                qs.checkQuota(user.getId());

                //fix ri ID to match 
                //first ExtrinsicObject (in case where ri is submitted without id)
                //only works for submission of one ri and one ExtrinsicObject
                correctRepositoryItemId(al, idToRepositoryItemMap);

                // It will select which repository items already exist and update
                HashMap idToNotExistingItemsMap = updateExistingRepositoryItems(
                idToRepositoryItemMap);
                storeRepositoryItems(idMap, idToNotExistingItemsMap);
            }
            
            /*
             * For RegistryObjects, the DAO will take care which objects already
             * exist and update them instead
             */
            log.info("Calling pm.insert at: " + System.currentTimeMillis());
            pm.insert(user, al);
            log.info("Done Calling pm.insert at: " + System.currentTimeMillis());
            
            resp.setStatus(org.oasis.ebxml.registry.bindings.rs.types.StatusType.SUCCESS);
            if (el.getRegistryErrorCount() > 0) {
                // warning exists
                resp.setRegistryErrorList(el);
            }
            
            //Now perform any content indexing for ExtrinsicObjects
            log.info("Calling indexContent at: " + System.currentTimeMillis());
            indexContent(user, al, idToRepositoryItemMap, idMap);
            log.info("Done Calling indexContent at: " + System.currentTimeMillis());
            
        }
        catch (InvalidURLsException e) {
            // We should have unified mapping in util to generate
            // RegistryErrorList Later
            
            Iterator roIter = e.getSourceRegistryObjects().iterator();
            while (roIter.hasNext()) {
                Object ro = roIter.next();
                RegistryError re = new RegistryError();
                if (ro instanceof ExternalLink) {
                    ExternalLink extLink = (ExternalLink) ro;
                    re.setContent("The ExternalLink with id " + extLink.getId()
                    + " is not resolvable, the Http URL is "
                    + extLink.getExternalURI());
                }
                else if (ro instanceof ServiceBinding) {
                    ServiceBinding serviceBinding = (ServiceBinding) ro;
                    re.setContent("The ServiceBinding with id " + serviceBinding
                    .getId() + " is not resolvable, the Http URL is "
                    + serviceBinding.getAccessURI());
                }
                else {
                    re.setContent("Internal Error happens, unknown "
                    + "RegistryObjectType");
                }
                re.setErrorCode("unknown");
                re.setCodeContext("LifeCycleManagerImpl.submitObjects");
                el.addRegistryError(re);
            }
            resp = new RegistryResponse();
            resp.setStatus(org.oasis.ebxml.registry.bindings.rs.types.StatusType
            .FAILURE);
            resp.setRegistryErrorList(el);
        }
        catch (ReferencesExistException e) {
            RegistryError re = new RegistryError();
            re.setContent(e.getMessage());
            re.setErrorCode("unknown");
            re.setCodeContext("LifeCycleManagerImpl.submitObjects");
            el.addRegistryError(re);
            resp = new RegistryResponse();
            resp.setStatus(org.oasis.ebxml.registry.bindings.rs.types.StatusType
            .FAILURE);
            resp.setRegistryErrorList(el);
        }
        catch (UUIDNotUniqueException e) {
            RegistryError re = new RegistryError();
            re.setContent(e.getMessage());
            re.setErrorCode("unknown");
            re.setCodeContext("LifeCycleManagerImpl.submitObjects");
            el.addRegistryError(re);
            resp = new RegistryResponse();
            resp.setStatus(org.oasis.ebxml.registry.bindings.rs.types.StatusType
            .FAILURE);
            resp.setRegistryErrorList(el);
        }
        catch (ReferencedObjectNotFoundException e) {
            RegistryError re = new RegistryError();
            re.setContent(e.getMessage());
            re.setErrorCode("unknown");
            re.setCodeContext("LifeCycleManagerImpl.submitObjects");
            el.addRegistryError(re);
            resp = new RegistryResponse();
            resp.setStatus(org.oasis.ebxml.registry.bindings.rs.types.StatusType
            .FAILURE);
            resp.setRegistryErrorList(el);
        }
        catch (AssociateToDeprecatedRegistryEntryException e) {
            RegistryError re = new RegistryError();
            re.setContent(e.getMessage());
            re.setErrorCode("unknown");
            re.setCodeContext("LifeCycleManagerImpl.submitObjects");
            el.addRegistryError(re);
            resp = new RegistryResponse();
            resp.setStatus(org.oasis.ebxml.registry.bindings.rs.types.StatusType
            .FAILURE);
            resp.setRegistryErrorList(el);
        }
        catch (RegistryObjectExistsException e) {
            RegistryError re = new RegistryError();
            re.setContent(e.getMessage());
            re.setErrorCode("unknown");
            re.setCodeContext("LifeCycleManagerImpl.submitObjects");
            el.addRegistryError(re);
            resp = new RegistryResponse();
            resp.setStatus(org.oasis.ebxml.registry.bindings.rs.types.StatusType
            .FAILURE);
            resp.setRegistryErrorList(el);
        }
        catch (QuotaExceededException e) {
            RegistryError re = new RegistryError();
            re.setContent(e.getMessage());
            re.setErrorCode("unknown");
            re.setCodeContext("LifeCycleManagerImpl.submitObjects");
            el.addRegistryError(re);
            resp = new RegistryResponse();
            resp.setStatus(org.oasis.ebxml.registry.bindings.rs.types.StatusType
            .FAILURE);
            resp.setRegistryErrorList(el);
        }
        catch (UnauthorizedRequestException e) {
            // UnauthorizedRequestException will be thrown if updating objects through SubmitObjectsRequest but the user is not authorized
            RegistryError re = new RegistryError();
            re.setContent(e.getMessage());
            re.setErrorCode("unknown");
            re.setCodeContext("LifeCycleManagerImpl.submitObjects");
            el.addRegistryError(re);
            resp = new RegistryResponse();
            resp.setStatus(org.oasis.ebxml.registry.bindings.rs.types.StatusType
            .FAILURE);
            resp.setRegistryErrorList(el);
        }
        catch (RegistryException e) {
            resp = util.createRegistryResponseFromThrowable(e, "LifeCycleManagerImpl.submitObjects", "Unknown");
            // Append any warnings
            RegistryError [] errs = el.getRegistryError();
            RegistryErrorList newEl = resp.getRegistryErrorList();
            for (int i=0; i < errs.length ;i++) {
                newEl.addRegistryError(errs[i]);
            }
        }
        return resp;
    }
    
    
    /**
     * Stores the repository items in idToRepositoryItemMap in the repository
     * @throws RegistryException when the items already exist
     */
    private void storeRepositoryItems(HashMap idMap, HashMap idToRepositoryItemMap) throws RegistryException {
        if (idToRepositoryItemMap != null) {
            
            Collection keySet = idToRepositoryItemMap.keySet();
            
            if (keySet != null) {
                
                Iterator iter = keySet.iterator();
                
                while (iter.hasNext()) {
                    
                    String id = (String)iter.next();
                    RepositoryItem ri = (RepositoryItem)idToRepositoryItemMap.get(id);
                    
                    DataHandler dh = ri.getDataHandler();
                    
                    //Replace id if found in idMap
                    if (idMap.containsKey(id)) {
                        id = (String)idMap.get(id);
                        
                        //Fix the id permanenetly within the repositoryItem
                        ri.setId(id);
                    }
                    
                    // Inserting the repository item
                    rm.insert(ri);
                }
            }
        }
    }
    
    /**
     * Check whether the id is a valid UUID.
     * @return false if id is null, or id does not start with "urn:uuid:", or
     * it is not valid.
     */
    public boolean isValidRegistryId(String id) {
        boolean isValid = false;
        
        if (id !=null && id.startsWith("urn:uuid:")) {
            String uuidStr = id.substring(9);
            
            if (uf.isValidUUID(uuidStr)) {
                isValid = true;
            }
        }
        
        return isValid;
    }
    
    /**
     * It should be called by submitObjects() to update existing Repository Items
     * @return HashMap of id To RepositoryItem, which are not exisitng
     */
    private HashMap updateExistingRepositoryItems(HashMap idToRepositoryItemMap) throws RegistryException {
        Iterator itemsIdsIter = idToRepositoryItemMap.keySet().iterator();
        
        // To find out which RI with a id does not exist
        ArrayList itemsIds = new ArrayList();
        while(itemsIdsIter.hasNext()) {
            String itemsId = (String)itemsIdsIter.next();
            itemsIds.add(itemsId);
        }
        ArrayList notExistItemsIds = rm.itemsExist(itemsIds);
        //System.err.println((String)notExistItemsIds.get(0) + "!!!!!");
        
        // Create two maps to store existing and non-existing items
        HashMap notExistItems = new HashMap();
        HashMap existingItems = new HashMap();
        
        for(int i=0; i < itemsIds.size(); i++) {
            String id = (String) itemsIds.get(i);
            String longId = null;
            if (!id.startsWith("urn:uuid:")) {
                longId = "urn:uuid:" + id;
            }
            else {
                longId = id;
            }
            if (notExistItemsIds.contains(longId)) {
                notExistItems.put(id, idToRepositoryItemMap.get(id));
            }
            else {
                existingItems.put(id, idToRepositoryItemMap.get(id));
            }
        }
        
        
        updateRepositoryItems(existingItems);
        return notExistItems;
    }
    
    /**
     * Check if id is a proper UUID. If not make a proper UUID based URN and add
     * a mapping in idMap between old and new Id. The parent attribute of ClassificationNode
     * will be alo fixed here according to their hierarchy if the parent attributes are
     * not provided explicitely by th clients.
     * @param ids The ArrayList holding all the UUIDs in the SubmitObjectsRequest
     * document
     * @throws UUIDNotUniqueException if any UUID is not unique within a
     * SubmitObjectsRequest
     */
    private void checkId(ArrayList ids, HashMap idMap, RegistryObjectType ro)
    throws RegistryException {
        String id = ro.getId();
        
        // Check for uniqueness
        if (ids != null && ids.contains(id)) {
            throw new UUIDNotUniqueException(id);
        }
        
        if (id !=null) {
            ids.add(id);
        }
        
        if (id == null || !id.startsWith("urn:uuid:")) {
            // Generate UUID if the request does not provide ID for a RO
            // or it does not start with urn:uuid:
            UUID uuid = uf.newUUID();
            String newId = "urn:uuid:" + uuid;
            ro.setId(newId);
            idMap.put(id, newId);
        }
        else {
            // id starts with "urn:uuid:"
            String uuidStr = id.substring(9);
            if (!uf.isValidUUID(uuidStr)) {
                // but invalid
                UUID uuid = uf.newUUID();
                String newId = "urn:uuid:" + uuid;
                idMap.put(id, newId);
            }
        }
        
        Classification[] classifications = ro.getClassification();
        for (int i=0; i<classifications.length; i++) {
            checkId(ids, idMap, classifications[i]);
        }
        
        ExternalIdentifier[] eids = ro.getExternalIdentifier();
        for (int i=0; i<eids.length; i++) {
            checkId(ids, idMap, eids[i]);
        }
        
        if (ro instanceof org.oasis.ebxml.registry.bindings.rim.Association) {
        } else if (ro instanceof org.oasis.ebxml.registry.bindings.rim.AuditableEvent) {
        } else if (ro instanceof org.oasis.ebxml.registry.bindings.rim.Classification) {
        } else if (ro instanceof org.oasis.ebxml.registry.bindings.rim.ClassificationNode) {
            ClassificationNode node = (ClassificationNode)ro;
            
            //Recurse over children nodes
            ClassificationNode[] children = node.getClassificationNode();
            for (int i=0; i<children.length; i++) {
                if (children[i].getParent() == null) {
                    children[i].setParent(node);
                }
                checkId(ids, idMap, children[i]);
            }
            
        } else if (ro instanceof org.oasis.ebxml.registry.bindings.rim.ClassificationScheme) {
            ClassificationScheme sch = (ClassificationScheme)ro;
            
            //Recurse over children nodes
            ClassificationNode[] children = sch.getClassificationNode();
            for (int i=0; i<children.length; i++) {
                if (children[i].getParent() == null) {
                    children[i].setParent(sch);
                }
                checkId(ids, idMap, children[i]);
            }
            
        } else if (ro instanceof org.oasis.ebxml.registry.bindings.rim.ExternalIdentifier) {
        } else if (ro instanceof org.oasis.ebxml.registry.bindings.rim.ExternalLink) {
        } else if (ro instanceof org.oasis.ebxml.registry.bindings.rim.ExtrinsicObject) {
        } else if (ro instanceof org.oasis.ebxml.registry.bindings.rim.Organization) {
        } else if (ro instanceof org.oasis.ebxml.registry.bindings.rim.RegistryPackage) {
            // Recusively check the RO under RegistryPackage
            RegistryPackage pkg = (RegistryPackage) ro;
            RegistryObjectList roList = pkg.getRegistryObjectList();
            ArrayList rosInPkg = bu.getRegistryObjectList(roList);
            Iterator iter = rosInPkg.iterator();
            while (iter.hasNext()) {
                RegistryObjectType roInPkg = (RegistryObjectType)iter.next();
                checkId(ids, idMap, roInPkg);
            }
        } else if (ro instanceof org.oasis.ebxml.registry.bindings.rim.Service) {
            Service service = (Service)ro;
            
            ServiceBinding[] bindings = service.getServiceBinding();
            for (int i=0; i<bindings.length; i++) {
                checkId(ids, idMap, bindings[i]);
            }
        } else if (ro instanceof org.oasis.ebxml.registry.bindings.rim.ServiceBinding) {
            ServiceBinding bind = (ServiceBinding)ro;
            SpecificationLink[] specLinks = bind.getSpecificationLink();
            for (int i=0; i<specLinks.length; i++) {
                checkId(ids, idMap, specLinks[i]);
            }
        } else if (ro instanceof org.oasis.ebxml.registry.bindings.rim.SpecificationLink) {
        } else if (ro instanceof org.oasis.ebxml.registry.bindings.rim.User) {
        } else {
            throw new RegistryException("Unexpected object: " + ro);
        }
    }
    
    /**
     * Fix those ids that are not proper UUID based URNs using idMap produced by checkId.
     */
    private void fixTemporaryId(RegistryObjectType o, HashMap idMap) throws RegistryException {
        //Fix the id of the object if there is an entry in the idMap
        
        // Fix the composed Classification and ExternalIdentifier first
        Classification[] classifications = o.getClassification();
        for (int i=0; i<classifications.length; i++) {
            if (idMap.containsKey(classifications[i].getId())) {
                classifications[i].setId((String)(idMap.get(classifications[i].getId())));
            }
        }
        ExternalIdentifier[] eids = o.getExternalIdentifier();
        for (int i=0; i<eids.length; i++) {
            if (idMap.containsKey(eids[i].getId())) {
                eids[i].setId((String)(idMap.get(eids[i].getId())));
            }
        }
        
        //Fix the id of the object if there is an entry in the idMap
        if (idMap.containsKey(o.getId())) {
            o.setId((String)(idMap.get(o.getId())));
        }
        
        RegistryObjectType acp = (RegistryObjectType)o.getAccessControlPolicy();
        if ((acp != null) && (idMap.containsKey(acp.getId()))) {
            acp.setId((String)(idMap.get(acp.getId())));
        }
        
        //Now fix ID/IDREFs that are specific to the type of RegistryObject
        if (o instanceof org.oasis.ebxml.registry.bindings.rim.Association) {
            Association ass = (Association)o;
            
            Object src = ass.getSourceObject();
            if (src !=null) {
                String id = bu.getObjectId(src);
                if (idMap.containsKey(id)) {
                    bu.setObjectId(src, (String)idMap.get(id));
                }
            }
            
            Object target = ass.getTargetObject();
            if (target !=null) {
                String id = bu.getObjectId(target);
                if (idMap.containsKey(id)) {
                    bu.setObjectId(target, (String)idMap.get(id));
                }
            }
            
        } else if (o instanceof org.oasis.ebxml.registry.bindings.rim.AuditableEvent) {
            
            AuditableEvent ae = (AuditableEvent)o;
            
            RegistryObjectType ro = (RegistryObjectType)ae.getRegistryObject();
            if (idMap.containsKey(ro.getId())) {
                ro.setId((String)(idMap.get(ro.getId())));
            }
            
            RegistryObjectType user = (RegistryObjectType)ae.getUser();
            if (idMap.containsKey(user.getId())) {
                user.setId((String)(idMap.get(user.getId())));
            }
        } else if (o instanceof org.oasis.ebxml.registry.bindings.rim.Classification) {
            Classification cl = (Classification)o;
            
            if (cl.getClassificationScheme() instanceof ClassificationSchemeType) {
                ClassificationScheme sch = (ClassificationScheme)cl.getClassificationScheme();
                if ((sch != null) && (idMap.containsKey(sch.getId()))) {
                    sch.setId((String)(idMap.get(sch.getId())));
                }
            }
            
            if (cl.getClassifiedObject() instanceof RegistryObjectType) {
                RegistryObjectType co = (RegistryObjectType)cl.getClassifiedObject();
                if (co !=null && idMap.containsKey(co.getId())) {
                    co.setId((String)(idMap.get(co.getId())));
                }
            }
            
            if (cl.getClassificationNode() instanceof RegistryObjectType) {
                ClassificationNode node = (ClassificationNode)cl.getClassificationNode();
                if ((node != null) && (idMap.containsKey(node.getId()))) {
                    node.setId((String)(idMap.get(node.getId())));
                }
            }
            
            
        } else if (o instanceof org.oasis.ebxml.registry.bindings.rim.ClassificationNode) {
            ClassificationNode node = (ClassificationNode)o;
            
            ClassificationNode parent = null;
            if (node.getParent() instanceof ClassificationNode) {
                parent = (ClassificationNode) node.getParent();
            }
            
            if ((parent != null) && (idMap.containsKey(parent.getId()))) {
                parent.setId((String)(idMap.get(parent.getId())));
            }
        } else if (o instanceof org.oasis.ebxml.registry.bindings.rim.ClassificationScheme) {
        } else if (o instanceof org.oasis.ebxml.registry.bindings.rim.ExternalIdentifier) {
            ExternalIdentifier ei = (ExternalIdentifier)o;
            
            if (ei.getIdentificationScheme() instanceof ClassificationScheme) {
                ClassificationScheme sch = (ClassificationScheme)ei.getIdentificationScheme();
                if (sch!=null && idMap.containsKey(sch.getId())) {
                    sch.setId((String)(idMap.get(sch.getId())));
                }
            }
            
        } else if (o instanceof org.oasis.ebxml.registry.bindings.rim.ExternalLink) {
        } else if (o instanceof org.oasis.ebxml.registry.bindings.rim.ExtrinsicObject) {
        } else if (o instanceof org.oasis.ebxml.registry.bindings.rim.Organization) {
            Organization org = (Organization)o;
            
            Object obj = org.getPrimaryContact();
            String id = bu.getObjectId(obj);
            
            if (idMap.containsKey(id)) {
                bu.setObjectId(obj, (String)(idMap.get(id)));
            }
            
            OrganizationType parent = null;
            
            if (org.getParent() instanceof OrganizationType) {
                parent = (OrganizationType)org.getParent();
            }
            
            if ((parent != null) && (idMap.containsKey(parent.getId()))) {
                parent.setId((String)(idMap.get(parent.getId())));
            }
        } else if (o instanceof org.oasis.ebxml.registry.bindings.rim.RegistryPackage) {
            
            // Recusively fix the RO under RegistryPackage
            RegistryPackage pkg = (RegistryPackage) o;
            RegistryObjectList roList = pkg.getRegistryObjectList();
            ArrayList rosInPkg = bu.getRegistryObjectList(roList);
            Iterator iter = rosInPkg.iterator();
            while (iter.hasNext()) {
                RegistryObjectType ro = (RegistryObjectType)iter.next();
                fixTemporaryId(ro, idMap);
            }
            
        } else if (o instanceof org.oasis.ebxml.registry.bindings.rim.Service) {
        } else if (o instanceof org.oasis.ebxml.registry.bindings.rim.ServiceBinding) {
            ServiceBinding bind = (ServiceBinding)o;
            
            ServiceBinding target = (ServiceBinding)bind.getTargetBinding();
            if (target != null && idMap.containsKey(target.getId())) {
                target.setId((String)(idMap.get(target.getId())));
            }
            
        } else if (o instanceof org.oasis.ebxml.registry.bindings.rim.SpecificationLink) {
            SpecificationLink sl = (SpecificationLink)o;
            
            Object so = sl.getSpecificationObject();
            if (so !=null) {
                String id = bu.getObjectId(so);
                if (idMap.containsKey(id)) {
                    bu.setObjectId(so, (String)idMap.get(id));
                }
            }
            
            
        } else if (o instanceof org.oasis.ebxml.registry.bindings.rim.User) {
            User u = (User)o;
            
            if (u.getOrganization() instanceof org.oasis.ebxml.registry.bindings.rim.Organization) {
                Organization org = (Organization)u.getOrganization();
                //Org may not yet be submitted if it is to be submitted after the user registration.
                if ((org != null) && idMap.containsKey(org.getId())) {
                    org.setId((String)(idMap.get(org.getId())));
                }
            }
            
        } else {
            throw new RegistryException("Unexpected object: " + o);
        }
    }
    
    public void fixTemporaryIds(ArrayList objs, HashMap idMap) throws RegistryException {
        
        Iterator iter = objs.iterator();
        // For checking UUID uniqueness within a SubmitObjectsRequest
        ArrayList ids = new ArrayList();
        while (iter.hasNext()) {
            Object o = iter.next();
            
            if (o instanceof org.oasis.ebxml.registry.bindings.rim.RegistryObjectType) {
                RegistryObjectType ro = (RegistryObjectType)o;
                checkId(ids, idMap, ro);
            }
            else {
                throw new RegistryException("Unexpected object: " + o);
            }
        }
        
        //Now iterate over each object and replace id defs/refs with new ids.
        iter = objs.iterator();
        while (iter.hasNext()) {
            RegistryObjectType ro = (RegistryObjectType)iter.next();
            fixTemporaryId(ro, idMap);
        }
    }
    
    
    /**
     * check whether the referenced object for	Association/Classification
     * /ClassificationNode/Organization exist within the request document
     */
    private void checkReferencedObject(Object object) throws ReferencedObjectNotFoundException
    , RegistryException {
        if (object instanceof RegistryObjectType) {
            RegistryObjectType ro = (RegistryObjectType) object;
            // Recursively check the nested Classification
            Classification [] classifications = ro.getClassification();
            for(int i=0; i < classifications.length; i++) {
                checkReferencedObject(classifications[i]);
            }
            // Recursively check the nested ExternalIdentifier
            ExternalIdentifier [] extIds = ro.getExternalIdentifier();
            for(int i=0; i < extIds.length; i++) {
                checkReferencedObject(extIds[i]);
            }
        }
        else {
            throw new RegistryException("Unknow Object type!");
        }
        
        
        if (object instanceof org.oasis.ebxml.registry.bindings.rim.Association) {
            Association ass = (Association)object;
            if (ass.getSourceObject()==null) {
                throw new ReferencedObjectNotFoundException("The Association "
                + ass.getId()
                + " is referencing a sourceObject that does not exist "
                + "within the request");
            }
            if (ass.getTargetObject()==null) {
                throw new ReferencedObjectNotFoundException("The Association "
                + ass.getId()
                + " is referencing a targetObject that does not exist "
                + "within the request");
            }
        }
        else if (object instanceof org.oasis.ebxml.registry.bindings.rim
        .Classification) {
            Classification cl = (Classification) object;
            if (cl.getClassifiedObject()==null) {
                throw new ReferencedObjectNotFoundException("The Classification "
                + cl.getId()
                + " is referencing a ClassifiedObject that does not exist "
                + "within the request");
            }
        }
        else if (object instanceof org.oasis.ebxml.registry.bindings.rim
        .ClassificationNode) {
        }
        else if(object instanceof org.oasis.ebxml.registry.bindings.rim.ExternalIdentifier) {
            ExternalIdentifier extId = (ExternalIdentifier) object;
            if (extId.getIdentificationScheme()==null) {
                throw new ReferencedObjectNotFoundException("The ExternalIdentifier "
                + extId.getId()
                + " is referencing a identificationScheme that does not exist "
                + "within the request");
            }
        }
        else if (object instanceof org.oasis.ebxml.registry.bindings.rim
        .Organization) {
            Organization org = (Organization) object;
            if (org.getPrimaryContact()==null) {
                throw new ReferencedObjectNotFoundException("The Organization "
                + org.getId()
                + " is referencing a primaryContact that does not exist "
                + "within the request");
            }
        }
    }
    
    /**
     * check whether the referenced object for	Association/Classification
     * /ClassificationNode/Organization exist within the request document
     * <br>
     * Add a RegistryError (warning) to the RegistryErrorList if a RegistryEntry
     * has object status, majorVersion and minorVersion
     */
    private void checkObjects(ArrayList objs, RegistryErrorList errorList)
    throws RegistryException {
        
        Iterator iter = objs.iterator();
        ArrayList ids = new ArrayList();
        
        while(iter.hasNext()) {
            Object object = iter.next();
            checkReferencedObject(object);
            
            if (object instanceof RegistryEntryType) {
                RegistryEntryType re = (RegistryEntryType) object;
                String warningMessage = null;
                if (re.getStatus() != null) {
                    warningMessage = "status";
                }
                if (re.hasMajorVersion()) {
                    warningMessage = "majorVersion";
                }
                if (re.hasMinorVersion()) {
                    warningMessage = "minorVersion";
                }
                if (warningMessage != null) {
                    warningMessage = "The " + warningMessage + " of RegistryEntry "
                    + re.getId() + " is ignored";
                    RegistryError error = new RegistryError();
                    error.setSeverity(ErrorType.WARNING);
                    error.setContent(warningMessage);
                    error.setErrorCode("unknown");
                    error.setCodeContext("LifeCycleManagerImpl.submitObjects");
                    errorList.addRegistryError(error);
                }
            }
        }
    }
    
    private void indexContent(User user, ArrayList al, HashMap idToRepositoryItemMap, HashMap idMap) {
        Iterator iter = al.iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            
            if (obj instanceof ExtrinsicObjectType) {
                ExtrinsicObjectType eo = (ExtrinsicObjectType)obj;
                try {
                    RepositoryItem indexableContent = (RepositoryItem)idToRepositoryItemMap.get(eo.getId());
                    LeafRegistryObjectList objs = cim.catalogContent(eo, indexableContent);
                    
                    ArrayList indexedMetadata = bu.getRegistryObjectList(objs);
                    
                    //Replace temporary ids with proper UUID.                    
                    fixTemporaryIds(indexedMetadata, idMap);
                                                            
                    /*
                     * For RegistryObjects, the DAO will take care which objects already
                     * exist and update them instead.
                     *
                     * Metadata for each ExtrisnicObject is stored independently in a separate transaction.
                     */
                    pm.insert(user, indexedMetadata);                    
                }
                catch (RegistryException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /** Approves one or more previously submitted objects */
    public RegistryResponse approveObjects(User user, ApproveObjectsRequest req) {
        RegistryResponse resp = null;
        RegistryErrorList el = new RegistryErrorList();
        try {
            ArrayList idList = new ArrayList();
            ObjectRefListItem [] objRefListItems = req.getObjectRefList()
            .getObjectRefListItem();
            for (int i=0; i < objRefListItems.length; i++) {
                idList.add(objRefListItems[i].getObjectRef().getId());
            }
            pm.updateStatus(user, idList
            , org.oasis.ebxml.registry.bindings.rim.types.StatusType.APPROVED
            , el);
            resp = new RegistryResponse();
            resp.setStatus(org.oasis.ebxml.registry.bindings.rs.types.StatusType
            .SUCCESS);
            if (el.getRegistryErrorCount() > 0) {
                // warning exists
                resp.setRegistryErrorList(el);
            }
        }
        catch (ObjectsNotFoundException e) {
            Iterator notExistIt = e.getNotExistIds().iterator();
            while (notExistIt.hasNext()) {
                String id = (String)notExistIt.next();
                RegistryError re = new RegistryError();
                re.setContent("The object with id " + id + " does not exist");
                re.setErrorCode("unknown");
                re.setCodeContext("LifeCycleManagerImpl.approveObjects");
                el.addRegistryError(re);
            }
            resp = new RegistryResponse();
            resp.setStatus(org.oasis.ebxml.registry.bindings.rs.types.StatusType
            .FAILURE);
            resp.setRegistryErrorList(el);
        }
        catch (NonRegistryEntryFoundException e) {
            RegistryError re = new RegistryError();
            re.setContent(e.getMessage());
            re.setErrorCode("unknown");
            re.setCodeContext("LifeCycleManagerImpl.approveObjects");
            el.addRegistryError(re);
            resp = new RegistryResponse();
            resp.setStatus(org.oasis.ebxml.registry.bindings.rs.types.StatusType
            .FAILURE);
            resp.setRegistryErrorList(el);
        }
        catch (RegistryException e) {
            resp = util.createRegistryResponseFromThrowable(e, "LifeCycleManagerImpl.approveObjects", "Unknown");
            // Append any warnings
            RegistryError [] errs = el.getRegistryError();
            RegistryErrorList newEl = resp.getRegistryErrorList();
            for (int i=0; i < errs.length ;i++) {
                newEl.addRegistryError(errs[i]);
            }
        }
        return resp;
    }
    
    /**
     * @throws RegistryException when the Repository items do not exist
     */
    private void updateRepositoryItems(HashMap idToRepositoryItemMap) throws RegistryException {
        if (idToRepositoryItemMap != null) {
            
            Collection keySet = idToRepositoryItemMap.keySet();
            
            if (keySet != null) {
                
                Iterator iter = keySet.iterator();
                
                while (iter.hasNext()) {
                    
                    String id = (String)iter.next();
                    RepositoryItem ri = (RepositoryItem)idToRepositoryItemMap.get(id);
                    if (!(ri.getId().equals(id))) {
                        ri.setId(id);
                    }
                    
                    DataHandler dh = ri.getDataHandler();
                    
                    // Updating the repository item
                    rm.update(ri);
                }
            }
        }
    }
    
    public RegistryResponse updateObjects(User user, UpdateObjectsRequest req
    , HashMap idToRepositoryMap) {
        RegistryResponse resp = null;
        RegistryErrorList el = new RegistryErrorList();
        try {
            LeafRegistryObjectList objs = req.getLeafRegistryObjectList();
            ArrayList al = bu.getRegistryObjectList(objs);
            System.err.println("LifeCycleManager, size: "  + al.size());
            checkObjects(al, el);
            if (idToRepositoryMap != null) {
                qs.checkQuota(user.getId());

                //fix ri ID to match first ExtrinsicObject 
                //(in case where ri is submitted without id OR
                //id doesn't match EO)
                //only works for submission of one ri and one ExtrinsicObject
                correctRepositoryItemId(al, idToRepositoryMap);

                updateRepositoryItems(idToRepositoryMap);
            }
            pm.update(user, al);
            resp = new RegistryResponse();
            resp.setStatus(org.oasis.ebxml.registry.bindings.rs.types.StatusType
            .SUCCESS);
            if (el.getRegistryErrorCount() > 0) {
                // warning exists
                resp.setRegistryErrorList(el);
            }
        }
        catch (ObjectsNotFoundException e) {
            Iterator notExistIt = e.getNotExistIds().iterator();
            while (notExistIt.hasNext()) {
                String id = (String)notExistIt.next();
                RegistryError re = new RegistryError();
                re.setContent("The object with id " + id + " does not exist");
                re.setErrorCode("unknown");
                re.setCodeContext("LifeCycleManagerImpl.updateObjects");
                el.addRegistryError(re);
            }
            resp = new RegistryResponse();
            resp.setStatus(org.oasis.ebxml.registry.bindings.rs.types.StatusType
            .FAILURE);
            resp.setRegistryErrorList(el);
        }
        catch (ReferencedObjectNotFoundException e) {
            RegistryError re = new RegistryError();
            re.setContent(e.getMessage());
            re.setErrorCode("unknown");
            re.setCodeContext("LifeCycleManagerImpl.updateObjects");
            el.addRegistryError(re);
            resp = new RegistryResponse();
            resp.setStatus(org.oasis.ebxml.registry.bindings.rs.types.StatusType
            .FAILURE);
            resp.setRegistryErrorList(el);
        }
        catch (AssociateToDeprecatedRegistryEntryException e) {
            RegistryError re = new RegistryError();
            re.setContent(e.getMessage());
            re.setErrorCode("unknown");
            re.setCodeContext("LifeCycleManagerImpl.submitObjects");
            el.addRegistryError(re);
            resp = new RegistryResponse();
            resp.setStatus(org.oasis.ebxml.registry.bindings.rs.types.StatusType
            .FAILURE);
            resp.setRegistryErrorList(el);
        }
        catch (InvalidURLsException e) {
            // We should have unified mapping in util to generate
            // RegistryErrorList Later
            
            Iterator roIter = e.getSourceRegistryObjects().iterator();
            while (roIter.hasNext()) {
                Object ro = roIter.next();
                RegistryError re = new RegistryError();
                if (ro instanceof ExternalLink) {
                    ExternalLink extLink = (ExternalLink) ro;
                    re.setContent("The ExternalLink with id " + extLink.getId()
                    + " is not resolvable, the Http URL is "
                    + extLink.getExternalURI());
                }
                else if (ro instanceof ServiceBinding) {
                    ServiceBinding serviceBinding = (ServiceBinding) ro;
                    re.setContent("The ServiceBinding with id " + serviceBinding
                    .getId() + " is not resolvable, the Http URL is "
                    + serviceBinding.getAccessURI());
                }
                else {
                    re.setContent("Internal Error happens, unknown "
                    + "RegistryObjectType");
                }
                re.setErrorCode("unknown");
                re.setCodeContext("LifeCycleManagerImpl.submitObjects");
                el.addRegistryError(re);
            }
            resp = new RegistryResponse();
            resp.setStatus(org.oasis.ebxml.registry.bindings.rs.types.StatusType
            .FAILURE);
            resp.setRegistryErrorList(el);
        }
        catch (ReferencesExistException e) {
            RegistryError re = new RegistryError();
            re.setContent("Deleting object denied. " + e.getMessage());
            re.setErrorCode("unknown");
            re.setCodeContext("LifeCycleManagerImpl.updateObjects");
            el.addRegistryError(re);
            resp = new RegistryResponse();
            resp.setStatus(org.oasis.ebxml.registry.bindings.rs.types.StatusType
            .FAILURE);
            resp.setRegistryErrorList(el);
        }
        catch (QuotaExceededException e) {
            RegistryError re = new RegistryError();
            re.setContent(e.getMessage());
            re.setErrorCode("unknown");
            re.setCodeContext("LifeCycleManagerImpl.updateObjects");
            el.addRegistryError(re);
            resp = new RegistryResponse();
            resp.setStatus(org.oasis.ebxml.registry.bindings.rs.types.StatusType
            .FAILURE);
            resp.setRegistryErrorList(el);
        }
        catch (RegistryException e) {
            resp = util.createRegistryResponseFromThrowable(e, "LifeCycleManagerImpl.updateObjects", "Unknown");
            // Append any warnings
            RegistryError [] errs = el.getRegistryError();
            RegistryErrorList newEl = resp.getRegistryErrorList();
            for (int i=0; i < errs.length ;i++) {
                newEl.addRegistryError(errs[i]);
            }
        }
        return resp;
    }
    
    /** Deprecates one or more previously submitted objects */
    public RegistryResponse deprecateObjects(User user, DeprecateObjectsRequest req) {
        RegistryResponse resp = null;
        RegistryErrorList el = new RegistryErrorList();
        try {
            ArrayList idList = new ArrayList();
            ObjectRefListItem [] objRefListItems = req.getObjectRefList()
            .getObjectRefListItem();
            for (int i=0; i < objRefListItems.length; i++) {
                idList.add(objRefListItems[i].getObjectRef().getId());
            }
            pm.updateStatus(user, idList
            , org.oasis.ebxml.registry.bindings.rim.types.StatusType.DEPRECATED
            , el);
            resp = new RegistryResponse();
            resp.setStatus(org.oasis.ebxml.registry.bindings.rs.types.StatusType
            .SUCCESS);
            if (el.getRegistryErrorCount() > 0) {
                // warning exists
                resp.setRegistryErrorList(el);
            }
        }
        catch (ObjectsNotFoundException e) {
            Iterator notExistIt = e.getNotExistIds().iterator();
            while (notExistIt.hasNext()) {
                String id = (String)notExistIt.next();
                RegistryError re = new RegistryError();
                re.setContent("The object with id " + id + " does not exist");
                re.setErrorCode("unknown");
                re.setCodeContext("LifeCycleManagerImpl.deprecateObjects");
                el.addRegistryError(re);
            }
            resp = new RegistryResponse();
            resp.setStatus(org.oasis.ebxml.registry.bindings.rs.types.StatusType
            .FAILURE);
            resp.setRegistryErrorList(el);
        }
        catch (NonRegistryEntryFoundException e) {
            RegistryError re = new RegistryError();
            re.setContent(e.getMessage());
            re.setErrorCode("unknown");
            re.setCodeContext("LifeCycleManagerImpl.deprecateObjects");
            el.addRegistryError(re);
            resp = new RegistryResponse();
            resp.setStatus(org.oasis.ebxml.registry.bindings.rs.types.StatusType
            .FAILURE);
            resp.setRegistryErrorList(el);
        }
        catch (RegistryException e) {
            resp = util.createRegistryResponseFromThrowable(e, "LifeCycleManagerImpl.deprecateObjects", "Unknown");
            // Append any warnings
            RegistryError [] errs = el.getRegistryError();
            RegistryErrorList newEl = resp.getRegistryErrorList();
            for (int i=0; i < errs.length ;i++) {
                newEl.addRegistryError(errs[i]);
            }
        }
        return resp;
    }
    
    /**
     * Removes one or more previously submitted objects from the registry. If the
     * deletionScope is "DeleteRepositoryItemOnly", it will assume all the
     * ObjectRef under ObjectRefList is referencing repository items. If the
     * deletionScope is "DeleteAll", the reference may be either RegistryObject
     * or repository item. In both case, if the referenced object cannot be found,
     * RegistryResponse with errors list will be returned.
     */
    public RegistryResponse removeObjects(User user, RemoveObjectsRequest req) {
        RegistryResponse resp = null;
        try {
            ArrayList idList = new ArrayList();
            ObjectRefListItem [] objRefListItems = req.getObjectRefList()
            .getObjectRefListItem();
            for (int i=0; i < objRefListItems.length; i++) {
                System.err.println("removeObjects: id = " + objRefListItems[i].getObjectRef().getId());
                idList.add(objRefListItems[i].getObjectRef().getId());
            }
            
            int deletionScope = DeletionScopeType.DELETEALL_TYPE;
            if (req.getDeletionScope() != null) {
                deletionScope = req.getDeletionScope().getType();
            }
            
            //DeletionScope=DeleteRepositoryItemOnly. If any repository item
            //does not exist, it will stop
            if (deletionScope == DeletionScopeType
            .DELETEREPOSITORYITEMONLY.getType()) {
                ArrayList notExist = rm.itemsExist(idList);
                if (notExist.size() > 0) {
                    throw new ObjectsNotFoundException(notExist);
                }
                rm.delete(idList);
            }
            else if (deletionScope == DeletionScopeType
            .DELETEALL.getType()) {
                //Check all referenced objects exist, all the objects should
                //be repository items or/and Registry Object                
                ArrayList notExist = pm.registryObjectsExist(idList);
                if (notExist.size() > 0) {
                    throw new ObjectsNotFoundException(notExist);
                }
                
                //find out which id is not an id of a repository item (i.e.
                //referencing RO only
                ArrayList nonItemsIds = rm.itemsExist(idList);
                
                //find out which id is an id of a repository item
                ArrayList itemsIds = new ArrayList();
                Iterator idListIt = idList.iterator();
                while(idListIt.hasNext()) {
                    Object id = idListIt.next();
                    if (!nonItemsIds.contains(id)) {
                        itemsIds.add(id);
                    }
                }
                
                // Delete the repository items
                rm.delete(itemsIds);
                
                //Delete all ROs with the ids
                pm.delete(user, idList);
            }
            else {
                throw new RegistryException("Undefined deletionScope");
            }
            resp = new RegistryResponse();
            resp.setStatus(org.oasis.ebxml.registry.bindings.rs.types.StatusType
            .SUCCESS);
        }
        catch (ObjectsNotFoundException e) {
            RegistryErrorList el = new RegistryErrorList();
            Iterator notExistIt = e.getNotExistIds().iterator();
            while (notExistIt.hasNext()) {
                String id = (String)notExistIt.next();
                RegistryError re = new RegistryError();
                re.setContent("The object with id " + id + " does not exist");
                re.setErrorCode("unknown");
                re.setCodeContext("LifeCycleManagerImpl.removeObjects");
                el.addRegistryError(re);
            }
            resp = new RegistryResponse();
            resp.setStatus(org.oasis.ebxml.registry.bindings.rs.types.StatusType
            .FAILURE);
            resp.setRegistryErrorList(el);
        }
        catch (ReferencesExistException e) {
            RegistryErrorList el = new RegistryErrorList();
            RegistryError re = new RegistryError();
            re.setContent("Deleting object denied. " + e.getMessage());
            re.setErrorCode("unknown");
            re.setCodeContext("LifeCycleManagerImpl.removeObjects");
            el.addRegistryError(re);
            resp = new RegistryResponse();
            resp.setStatus(org.oasis.ebxml.registry.bindings.rs.types.StatusType
            .FAILURE);
            resp.setRegistryErrorList(el);
        }
        catch (RegistryException e)  {
            resp = util.createRegistryResponseFromThrowable(e,
            "LifeCycleManagerImpl.removeObjects", "Unknown");
        }
        return resp;
    }
    
    /** Add slots to one or more registry entries. */
    
    public RegistryResponse addSlots(AddSlotsRequest req) {
        
        RegistryResponse resp = null;
        try {
            String objectRefId = req.getObjectRef().getId();
            Slot[] slots = req.getSlot();
            ArrayList slotsList = new ArrayList();
            for (int i=0; i < slots.length; i++) {
                slotsList.add(slots[i]);
            }
            pm.addSlots(objectRefId, slotsList
            );
            
            resp = new RegistryResponse();
            resp.setStatus(org.oasis.ebxml.registry.bindings.rs.types.StatusType
            .SUCCESS);
        }
        catch (SlotsParentNotExistException e) {
            RegistryErrorList el = new RegistryErrorList();
            String parentId = e.getParentId();
            RegistryError re = new RegistryError();
            re.setContent("The parent '" + parentId + "' of these slots does "
            + "not exist");
            re.setErrorCode("unknown");
            re.setCodeContext("LifeCycleManagerImpl.addSlots");
            el.addRegistryError(re);
            resp = new RegistryResponse();
            resp.setStatus(org.oasis.ebxml.registry.bindings.rs.types.StatusType
            .FAILURE);
            resp.setRegistryErrorList(el);
        }
        catch (SlotsExistException e) {
            RegistryErrorList el = new RegistryErrorList();
            Iterator iter = e.getSlotsNames().iterator();
            String parentId = e.getParentId();
            while(iter.hasNext()) {
                RegistryError re = new RegistryError();
                String slotName = (String)iter.next();
                re.setContent("The Slot '" + slotName + "' for the"
                + " RegistryObject"
                + " '" + parentId + "' already exists");
                re.setErrorCode("unknown");
                re.setCodeContext("LifeCycleManagerImpl.addSlots");
                el.addRegistryError(re);
            }
            resp = new RegistryResponse();
            resp.setStatus(org.oasis.ebxml.registry.bindings.rs.types.StatusType
            .FAILURE);
            resp.setRegistryErrorList(el);
        }
        catch (DuplicateSlotsException e) {
            RegistryErrorList el = new RegistryErrorList();
            Iterator iter = e.getSlotsNames().iterator();
            String parentId = e.getParentId();
            while(iter.hasNext()) {
                RegistryError re = new RegistryError();
                String slotName = (String)iter.next();
                re.setContent("More than one Slot have the name '" + slotName
                + "' for the RegistryObject "
                + "'" + parentId + "'");
                re.setErrorCode("unknown");
                re.setCodeContext("LifeCycleManagerImpl.addSlots");
                el.addRegistryError(re);
            }
            resp = new RegistryResponse();
            resp.setStatus(org.oasis.ebxml.registry.bindings.rs.types.StatusType
            .FAILURE);
            resp.setRegistryErrorList(el);
        }
        catch (RegistryException e) {
            resp = util.createRegistryResponseFromThrowable(e, "LifeCycleManagerImpl.addSlots", "Unknown");
        }
        return resp;
        
    }
    
    
    /** Remove specified slots from one or more registry entries. */
    public RegistryResponse removeSlots(RemoveSlotsRequest req) {
        RegistryResponse resp = null;
        try {
            String objectRefId = req.getObjectRef().getId();
            Slot[] slots = req.getSlot();
            ArrayList slotsList = new ArrayList();
            for (int i=0; i < slots.length; i++) {
                slotsList.add(slots[i]);
            }
            pm.removeSlots(objectRefId, slotsList);
            resp = new RegistryResponse();
            resp.setStatus(org.oasis.ebxml.registry.bindings.rs.types.StatusType
            .SUCCESS);
        }
        catch (SlotNotExistException e) {
            RegistryError re = new RegistryError();
            re.setContent(e.getMessage());
            re.setErrorCode("unknown");
            re.setCodeContext("LifeCycleManagerImpl.removeSlots");
            
            RegistryErrorList el = new RegistryErrorList();
            el.addRegistryError(re);
            
            resp = new RegistryResponse();
            resp.setStatus(org.oasis.ebxml.registry.bindings.rs.types.StatusType
            .FAILURE);
            resp.setRegistryErrorList(el);
        }
        catch (RegistryException e) {
            resp = util.createRegistryResponseFromThrowable(e, "LifeCycleManagerImpl.removeSlots", "Unknown");
        }
        return resp;
    }
    
    public static void printUsage() {
        System.out.println("usage: -submitObjects | -approveObjects | -updateObjects " +
        "| -deprecateObjects | -removeObjects | -addSlots | - removeSlots "
        + "[SubmitObjectsRequest | "
        + "ApproveObjectsRequest | "
        + "UpdateObjectsRequest | "
        + "DeprecateObjectsRequest | "
        + "RemoveObjectsRequest | "
        + "AddSlotsRequest] "
        + "-repItem [repository item] -repItemId [id] -contentId [contentId] "
        + "-userId [user id]");
    }
    
    public static void main(String[] args) {
        if (args.length < 2) {
            printUsage();
            return;
        }
        String command = args[0];
        String requestFile = args[1];
        String itemFile = null;
        String itemId = null;
        String contentId = null;
        String userId = null;
        
        for (int i=2; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-repItem")) {
                itemFile = args[i+1];
            }
            else if (args[i].equalsIgnoreCase("-repItemId")) {
                itemId = args[i+1];
            }
            else if (args[i].equalsIgnoreCase("-contentId")) {
                contentId = args[i+1];
            }
            else if (args[i].equalsIgnoreCase("-userId")) {
                userId = args[i+1];
            }
        }
        if (userId==null) {
            System.err.println("missing userId");
        }
        
        try {
            LifeCycleManagerImpl lcm = LifeCycleManagerImpl.getInstance();
            User user = new User();
            user.setId(userId);
            if (command.equalsIgnoreCase("-submitObjects")) {
                
                FileReader fr = new FileReader(requestFile);
                SubmitObjectsRequest req = SubmitObjectsRequest.unmarshal(fr);
                long t1 = System.currentTimeMillis();
                RegistryResponse response = new RegistryResponse();
                if (itemFile==null) {
                    // without repository item
                    response = lcm.submitObjects(user, req, null);
                }
                else {
                    // with repository item
                    if (itemId==null || contentId==null) {
                        System.err.println("missing repItemId/contentId");
                        return;
                    }
                    File repositoryFile = new File(itemFile);
                    //String id = args[3];
                    //String contentId = args[4];
                    System.out.println("id:" + itemId);
                    System.out.println("contentId:" + contentId);
                    HashMap repositoryMap = new HashMap();
                    //needed so that compiler won't argue about type ambiguity...
                    String nullTemp = null;
                    repositoryMap.put(itemId, new RepositoryItem(contentId, nullTemp, new
                    DataHandler(new FileDataSource(repositoryFile))));
                    response = lcm.submitObjects(user, req, repositoryMap);
                }
                long t2 = System.currentTimeMillis();
                response.marshal(new OutputStreamWriter(System.err));
            }
            
            else if (command.equalsIgnoreCase("-approveObjects")) {
                FileReader fr = new FileReader(requestFile);
                
                ApproveObjectsRequest req = ApproveObjectsRequest.unmarshal(fr);
                
                long t1 = System.currentTimeMillis();
                RegistryResponse response = lcm.approveObjects(user, req);
                long t2 = System.currentTimeMillis();
                
                System.err.println("Elapsed time in seconds: " + (t2-t1)/1000);
                System.err.println("RegistryResponse:");
                response.marshal(new OutputStreamWriter(System.err));
            }
            
            else if (command.equalsIgnoreCase("-updateObjects")) {
                FileReader fr = new FileReader(requestFile);
                UpdateObjectsRequest req = UpdateObjectsRequest.unmarshal(fr);
                long t1 = System.currentTimeMillis();
                RegistryResponse response = new RegistryResponse();
                if (itemFile==null) {
                    // without repository item
                    response = lcm.updateObjects(user, req, null);
                }
                else {
                    // with repository item
                    if (itemId==null || contentId==null) {
                        System.err.println("missing repItemId/contentId");
                        return;
                    }
                    File repositoryFile = new File(itemFile);
                    System.out.println("id:" + itemId);
                    System.out.println("contentId:" + contentId);
                    HashMap repositoryMap = new HashMap();
                    //needed so that compiler won't argue about type ambiguity...
                    String nullTemp = null;
                    repositoryMap.put(itemId, new RepositoryItem(contentId, nullTemp, new
                    DataHandler(new FileDataSource(repositoryFile))));
                    response = lcm.updateObjects(user, req, repositoryMap);
                }
                long t2 = System.currentTimeMillis();
                response.marshal(new OutputStreamWriter(System.err));
            }
            
            else if (command.equalsIgnoreCase("-deprecateObjects")) {
                FileReader fr = new FileReader(requestFile);
                
                DeprecateObjectsRequest req = DeprecateObjectsRequest.unmarshal
                (fr);
                
                long t1 = System.currentTimeMillis();
                RegistryResponse response = lcm.deprecateObjects(user, req);
                long t2 = System.currentTimeMillis();
                
                System.err.println("Elapsed time in seconds: " + (t2-t1)/1000);
                response.marshal(new OutputStreamWriter(System.err));
            }
            else if (command.equalsIgnoreCase("-removeObjects")) {
                FileReader fr = new FileReader(requestFile);
                
                RemoveObjectsRequest req = RemoveObjectsRequest.unmarshal
                (fr);
                
                long t1 = System.currentTimeMillis();
                RegistryResponse response = lcm.removeObjects(user, req);
                long t2 = System.currentTimeMillis();
                
                System.err.println("Elapsed time in seconds: " + (t2-t1)/1000);
                response.marshal(new OutputStreamWriter(System.err));
                
            }
            else if (command.equalsIgnoreCase("-addSlots")) {
                FileReader fr = new FileReader(requestFile);
                
                AddSlotsRequest req = AddSlotsRequest.unmarshal
                (fr);
                
                long t1 = System.currentTimeMillis();
                RegistryResponse response = lcm.addSlots(req);
                long t2 = System.currentTimeMillis();
                
                System.err.println("Elapsed time in seconds: " + (t2-t1)/1000);
                response.marshal(new OutputStreamWriter(System.err));
            }
            else if (command.equalsIgnoreCase("-removeSlots")) {
                FileReader fr = new FileReader(requestFile);
                
                RemoveSlotsRequest req = RemoveSlotsRequest.unmarshal
                (fr);
                
                long t1 = System.currentTimeMillis();
                RegistryResponse response = lcm.removeSlots(req);
                long t2 = System.currentTimeMillis();
                
                System.err.println("Elapsed time in seconds: " + (t2-t1)/1000);
                response.marshal(new OutputStreamWriter(System.err));
            }
            else {
                printUsage();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    * Fix Repository item's ID to match ID in first
    * associated ExtrinsicObject. (in case where ri 
    * is submitted without id or id doesn't match id 
    * of ExtrinsicObject).
    * Currently Only works for submission of one 
    * Repository item and its associated ExtrinsicObject.
    * Called in updateObjects() and submitObjects().
    */
    private void correctRepositoryItemId(ArrayList objs,
                                         HashMap idToRepositoryItemMap){
    if(objs.size() == 1){
        Object obj = objs.get(0);
        if (obj instanceof RegistryEntryType) {
            RegistryEntryType firstRe = (RegistryEntryType)objs.get(0);
            if(firstRe != null && 
               firstRe instanceof ExtrinsicObject){
                String correctId = firstRe.getId();
                if(idToRepositoryItemMap.size() == 1){
                    Iterator attachIter = idToRepositoryItemMap.keySet().iterator();
                    String attachIdKey = (String)attachIter.next();
                    RepositoryItem attachRi = 
                        (RepositoryItem)idToRepositoryItemMap.get(attachIdKey);
                    String attachId = attachRi.getId();

                    if(correctId != null && 
                       !correctId.equals(attachId)){
                        System.err.println("[LifeCycleManager::correctRepositoryItemId()]" +
                                           " RepositoryItem id [" +
                                           attachId + 
                                           "] does not match Registry Object id [" +
                                           correctId +
                                           "]");
                        System.err.println("[LifeCycleManager::correctRepositoryItemId()] " +
                                           " Updating RepositoryItem id to " +
                                           correctId);
                        //remove old key
                        idToRepositoryItemMap.remove(attachRi.getId());
                        attachRi.setId(correctId);
                        //add new key and ri
                        idToRepositoryItemMap.put(correctId, attachRi);
                    }
                }
            }
        }
    }
  }

}
/*
 * $Header: /cvsroot/sino/jaxr/src/com/sun/xml/registry/ebxml/LifeCycleManagerImpl.java,v 1.53 2003/08/21 23:15:36 farrukh_najmi Exp $
 *
 *
 */

package com.sun.xml.registry.ebxml;

import java.util.*;
import java.io.*;
import java.lang.reflect.*;
import javax.activation.*;
import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;

import com.sun.xml.registry.ebxml.RegistryServiceImpl;
import com.sun.xml.registry.ebxml.infomodel.*;
import com.sun.xml.registry.ebxml.util.*;

import org.oasis.ebxml.registry.bindings.rim.LeafRegistryObjectList;
import org.oasis.ebxml.registry.bindings.rim.LeafRegistryObjectListTypeItem;
import org.oasis.ebxml.registry.bindings.rim.ObjectRef;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefList;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefListItem;
import org.oasis.ebxml.registry.bindings.rs.DeprecateObjectsRequest;
import org.oasis.ebxml.registry.bindings.rs.RemoveObjectsRequest;
import org.oasis.ebxml.registry.bindings.rs.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.rs.UpdateObjectsRequest;

import com.sun.ebxml.registry.util.BindingUtility;
import com.sun.ebxml.registry.RegistryException;

import org.apache.commons.logging.Log;

/**
 * Class Declaration for Class1
 * 
 */
public abstract class LifeCycleManagerImpl implements LifeCycleManager {

    RegistryServiceImpl regService = null;
    HashSet modifiedObjects = null;
    private HashMap objectTypesMap = null;
    private Log log;    


    LifeCycleManagerImpl(RegistryServiceImpl regService) {
        this.regService = regService;
        modifiedObjects = new HashSet();
        
        log = regService.getConnection().getConnectionFactory().getLog();
    }

    /**
     * Creates instances of information model
     * interfaces (factory method). To create an Organization, use this
     * method as follows:
     * <pre>
     * Organization org = (Organization)
     *    lifeCycleMgr.createObject(LifeCycleManager.ORGANIZATION);
     * </pre>
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param interfaceName the unqualified name of an interface in the
     * javax.xml.registry.infomodel package
     *
     * @return an Object that can then be cast to an instance of the interface
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     *
     * @throws InvalidRequestException if the interface is not an interface in
     * the javax.xml.registry.infomodel package
     *
     * @throws UnsupportedCapabilityException if the client attempts to
     * create an instance of an infomodel interface that is not supported
     * by the capability level of the JAXR provider
     */
    public Object createObject(String className)
        throws JAXRException, InvalidRequestException,
        UnsupportedCapabilityException
    {
        Object obj = null;

        try {
            className = "com.sun.xml.registry.ebxml.infomodel." + regService.mapEbXMLNameToJAXRName(className) + "Impl";
            
            Class cls = this.getClass().getClassLoader().loadClass(className);
            Class lcmCls = this.getClass().getClassLoader().loadClass("com.sun.xml.registry.ebxml.LifeCycleManagerImpl");
            Class[] parmTypes = {
                lcmCls
            };
            Constructor cons = cls.getDeclaredConstructor(parmTypes);

            Object[] args = {
                this
            };
            obj = cons.newInstance(args);
        }
        catch (ClassNotFoundException e) {
            throw new InvalidRequestException("Invalid className '" + className + "' argument."); 
        }
        catch (NoSuchMethodException e) {
            throw new JAXRException(e);
        }
        catch (InvocationTargetException e) {
            throw new JAXRException(e.getCause());
        }
        catch (IllegalAccessException e) {
            throw new JAXRException(e);
        }
        catch (InstantiationException e) {
            throw new JAXRException(e);
        }
        catch (ExceptionInInitializerError e) {
            throw new JAXRException(e);
        }
        catch (SecurityException e) {
            throw new JAXRException(e);
        }
        
        return obj;
    }

    /**
     * Create an Association instance using the specified 
     * parameters. The sourceObject is left null and will be set
     * when the Association is added to a RegistryObject.
     * <p>
     * Note that for a UDDI provider an Association may only be created
     * between Organizations.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     */
    public Association createAssociation(
        RegistryObject targetObject,
        Concept associationType
        ) throws JAXRException{
        
        AssociationImpl ass = new AssociationImpl(this);
        ass.setTargetObject(targetObject);
        ass.setAssociationType(associationType);
        return ass;
    }

    /**
     * Create a Classification instance for an external
     * Classification using the specified name and value that identifies
     * a taxonomy element within specified ClassificationScheme.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     */
    public Classification createClassification(
        ClassificationScheme scheme,
        String name,
        String value
        ) throws JAXRException{
        
        InternationalString is = createInternationalString(name);
        Classification cl = createClassification(scheme, is, value);
		
        return cl;
    }

    /**
     * Create a Classification instance for an external
     * Classification using the specified name and value that identifies
     * a taxonomy element within specified ClassificationScheme.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     */
    public Classification createClassification(
        ClassificationScheme scheme,
        InternationalString name,
        String value
        ) throws JAXRException{

        ClassificationImpl cl = new ClassificationImpl(this);
        cl.setClassificationScheme(scheme);
        cl.setName(name);
        cl.setValue(value);
        return cl;
    }


    /**
     * Create a Classification instance for an internal
     * Classification using the specified Concept which identifies
     * a taxonomy element within an internal ClassificationScheme.
     * <p>
     * Throws InvalidRequestException if the Concept is not under
     * a ClassificationScheme.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     */
    public Classification createClassification(
        Concept concept
        ) throws JAXRException, InvalidRequestException{

        ClassificationImpl cl = new ClassificationImpl(this);
        cl.setConcept(concept);
        return cl;
    }

    /**
     * Create a scheme given specified parameters.
     * This is the method to use to create a scheme
     * in most situations.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     */		
    public ClassificationScheme createClassificationScheme(
        String name, String description
        ) throws JAXRException, InvalidRequestException{

        InternationalString isName = createInternationalString(name);
        InternationalString isDesc = createInternationalString(description);
		
        ClassificationScheme scheme = createClassificationScheme(isName, isDesc);
	    
        return scheme;
    }

    /**
     * Create a scheme given specified parameters.
     * This is the method to use to create a scheme
     * in most situations.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     */		
    public ClassificationScheme createClassificationScheme(
        InternationalString name, InternationalString description
        ) throws JAXRException, InvalidRequestException{


        ClassificationSchemeImpl scheme = new ClassificationSchemeImpl(this);
        scheme.setName(name);
        scheme.setDescription(description);
	    
        return scheme;
    }

    /**
     * Creates a ClassificationScheme from a Concept that has no
     * ClassificationScheme or parent Concept. 
     * <p>
     * This method is a special case method to do a type safe conversion
     * from Concept to ClassificationScheme.
     * <p>
     * This method is
     * provided to allow for Concepts returned by the BusinessQueryManager
     * findConcepts call to be safely cast to ClassificationScheme. It
     * is up to the programer to make sure that the Concept is indeed
     * semantically a ClassificationScheme. 
     * <p>
     * This method is necessary because in UDDI a tModel may serve
     * multiple purposes and there is no way to know when a tModel
     * maps to a Concept and when it maps to a ClassificationScheme.
     * UDDI leaves the determination to the programmer and consequently so does this
     * method.
     * <p>
     * Throws InvalidRequestException if the Concept has a parent Concept
     * or is under a ClassificationScheme.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     *  
     */
    public ClassificationScheme createClassificationScheme(
        Concept concept
        ) throws JAXRException, InvalidRequestException{
        
        ClassificationSchemeImpl scheme = new ClassificationSchemeImpl(this, concept);                
        return scheme;
    }

    /**
     * Create a Concept instance using the specified 
     * parameters.
     *  
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     * @param parent Is either a reference to a parent ClassificationScheme or Concept
     */
    public Concept createConcept(
        RegistryObject parent,
        String name,
        String value
        ) throws JAXRException{
        
        InternationalString isName = createInternationalString(name);            
        return createConcept(parent, isName, value);
    }

    /**
     * Create a Concept instance using the specified 
     * parameters.
     *  
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     * @param parent Is either a reference to a parent ClassificationScheme or Concept
     */
    public Concept createConcept(
        RegistryObject parent,
        InternationalString name,
        String value
        ) throws JAXRException{
        
        ConceptImpl concept = new ConceptImpl(this);
        
        if (parent instanceof ClassificationScheme) {
            concept.setClassificationScheme((ClassificationScheme)parent);
        }
        else if (parent instanceof Concept) {
            concept.setParentConcept((Concept)parent);
        }
        concept.setName(name);
        concept.setValue(value);
        
        return concept;
    }

    /**
     * Creates an EmailAddress instance using an address as the
     * parameter.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param address the email address
     *
     * @return the EmailAddress instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public EmailAddress createEmailAddress(String address)
        throws JAXRException
    {
        EmailAddressImpl email = new EmailAddressImpl(this);
        email.setAddress(address);
        return email;
    }

    /**
     * Creates an EmailAddress instance using both an address and a type as
     * parameters.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param address the email address
     * @param type the type of the address
     *
     * @return the EmailAddress instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public EmailAddress createEmailAddress(String address, String type)
        throws JAXRException
    {
        EmailAddressImpl email = new EmailAddressImpl(this);
        email.setAddress(address);
        email.setType(type);
        return email;
    }

    /**
     * Create an ExternalIdentifier instance using the specified 
     * parameters.
     *  
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     */
    public ExternalIdentifier createExternalIdentifier(
        ClassificationScheme scheme,
        String name,
        String value
        ) throws JAXRException{


        InternationalString is = createInternationalString(name);
        ExternalIdentifier extId = createExternalIdentifier(scheme, is, value);
	    
        return extId;
    }

    /**
     * Create an ExternalIdentifier instance using the specified 
     * parameters.
     *  
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     */
    public ExternalIdentifier createExternalIdentifier(
        ClassificationScheme scheme,
        InternationalString name,
        String value
        ) throws JAXRException{

        ExternalIdentifierImpl extId = new ExternalIdentifierImpl(this);
        extId.setIdentificationScheme(scheme);
        extId.setName(name);
        extId.setValue(value);
        return extId;

    }

    /**
     * Create an ExternalLink instance using the specified 
     * parameters.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     *  
     */
    public ExternalLink createExternalLink(
        String externalURI,
        String description
        ) throws JAXRException{

        InternationalString isDesc = createInternationalString(description);
        ExternalLink link = createExternalLink(externalURI, isDesc);
        return link;
    }

    /**
     * Create an ExternalLink instance using the specified 
     * parameters.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     *  
     */
    public ExternalLink createExternalLink(
        String externalURI,
        InternationalString description
        ) throws JAXRException{

        ExternalLink link = new ExternalLinkImpl(this);
        link.setDescription(description);
        link.setExternalURI(externalURI);
		
        return link;
    }

    /**
     * Creates an ExtrinsicObject instance using the specified parameters.
     *
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL>
     *
     * @param repositoryItem the DataHandler for the repository item. Must
     * not be null.
     *
     * @return the ExtrinsicObject instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     *
     */
    public ExtrinsicObject createExtrinsicObject(javax.activation.DataHandler
                                                 repositoryItem)
        throws javax.xml.registry.JAXRException
    {
        ExtrinsicObject eo = new ExtrinsicObjectImpl(this);
        if (repositoryItem != null) {
            eo.setRepositoryItem(repositoryItem);
        }
        return eo;
    }

    /**
     * Creates an ExtrinsicObject instance using the specified parameters.
     * JAXR 2.0??
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL>
     *
     *
     * @return the ExtrinsicObject instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     *
     */
    public ExtrinsicObject createExtrinsicObject()
        throws javax.xml.registry.JAXRException
    {        
        return createExtrinsicObject(null);
    }
    
    /**
     * Create a InternationalString instance using the specified 
     * parameters.
     *  
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     */
    public javax.xml.registry.infomodel.InternationalString createInternationalString() throws javax.xml.registry.JAXRException{

        return createInternationalString(Locale.getDefault(), "");
    }

    /**
     * Create a InternationalString instance using the specified 
     * parameters and the default Locale.
     *  
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     */
    public InternationalString createInternationalString(
        String s
        ) throws JAXRException{

        return createInternationalString(Locale.getDefault(), s);
    }

    /**
     * Create a InternationalString instance using the specified 
     * parameters.
     *  
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     */
    public InternationalString createInternationalString(
        Locale l,
        String s
        ) throws JAXRException{

        LocalizedString ls = new LocalizedStringImpl(this);
        ls.setLocale(l);
        ls.setValue(s);
		
        InternationalString is = new InternationalStringImpl(this);
        is.addLocalizedString(ls);
        return is;
    }

	/**
	 * Creates a Key instance from an ID.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param id the ID string from which to create the Key
	 *
	 * @return the Key instance created
	 * 
	 * @throws JAXRException if the JAXR provider encounters an internal error
	 */
    public Key createKey(String id) throws JAXRException {
        KeyImpl key = new KeyImpl(this);
        key.setId(id);
        return key;
    }

    //??Add to level 1 API for JAXR 2.0
    public Key createKey() throws JAXRException {
        String id = createId();
        return createKey(id);
    }

    //??Add to level 1 API for JAXR 2.0
    public String createId() throws JAXRException {
        String id = "urn:uuid:"
            + UUIDFactory.getInstance().newUUID().toString();
        return id;
    }

    /**
     * Creates a LocalizedString instance using the specified Locale and
     * String parameters.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param l the Locale in which to create the LocalizedString
     * @param s the String from which to create the LocalizedString
     *
     * @return the LocalizedString instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public LocalizedString createLocalizedString(Locale l, String s)
        throws JAXRException
    {
        LocalizedStringImpl lString = new LocalizedStringImpl(this);
        lString.setLocale(l);
        lString.setValue(s);
        return lString;
    }

    /**
     * Creates a LocalizedString instance using the specified
     * Locale, String, and character set parameters.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param l the Locale in which to create the LocalizedString
     * @param s the String from which to create the LocalizedString
     * @param charSetName the name of the character set to use
     *
     * @return the LocalizedString instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public LocalizedString createLocalizedString(Locale l, String s,
                                                 String charSetName)
        throws JAXRException
    {
        LocalizedString lString = createLocalizedString(l, s);
        lString.setCharsetName(charSetName);
        return lString;
    }

    /**
     * Create an Organization instance using the specified 
     * parameters.
     *  
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     */
    public Organization createOrganization(
        String name
        ) throws JAXRException{

        InternationalString is = createInternationalString(name);
        Organization org = createOrganization(is);
        
        return org;
    }

    /**
     * Create an Organization instance using the specified 
     * parameters.
     *  
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     */
    public Organization createOrganization(
        InternationalString name
        ) throws JAXRException{

        Organization org = new OrganizationImpl(this);
        org.setName(name);
		
        return org;
    }

    /**
     * Create a PersonName instance using the specified 
     * parameters.
     *  
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
     *
     */
    public PersonName createPersonName(
        String firstName,
        String middleName,
        String lastName
        ) throws JAXRException{
        
        PersonNameImpl personName = new PersonNameImpl(this);
        personName.setFirstName(firstName);
        personName.setMiddleName(middleName);
        personName.setLastName(lastName);
        
        return personName;
    }

    /**
     * Create a PersonName instance using the specified 
     * parameters.
     *  
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     */
    public PersonName createPersonName(
        String fullName
        ) throws JAXRException{
        PersonNameImpl personName = new PersonNameImpl(this);
        personName.setFullName(fullName);
        
        return personName;
    }

    /**
     * Create a PostalAddress instance using the specified 
     * parameters.
     *  
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     */
    public PostalAddress createPostalAddress(
        String streetNumber,
        String street,
        String city,
        String stateOrProvince,
        String country,
        String postalCode,
        String type
        ) throws JAXRException{

        PostalAddress addr = new PostalAddressImpl(this);
    	addr.setStreetNumber(streetNumber);
        addr.setStreet(street);
        addr.setCity(city);
        addr.setStateOrProvince(stateOrProvince);
        addr.setCountry(country);
        addr.setPostalCode(postalCode);
        addr.setType(type);
		
        return addr;
    }

    /**
     * Create a RegistryPackage instance using the specified 
     * parameters.
     *  
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
     *
     */
    public RegistryPackage createRegistryPackage(
        String name
        ) throws JAXRException{
        InternationalString is = createInternationalString(name);
        return createRegistryPackage(is);
    }

    /**
     * Create a RegistryPackage instance using the specified 
     * parameters.
     *  
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
     *
     */
    public RegistryPackage createRegistryPackage(
        InternationalString name
        ) throws JAXRException{
        RegistryPackageImpl pkg = new RegistryPackageImpl(this);
        pkg.setName(name);
        return pkg;
    }

    /**
     * Create an Service instance using the specified 
     * parameters.
     *  
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     */
    public Service createService(
        String name
        ) throws JAXRException{

        InternationalString is = createInternationalString(name);
     
    	return createService(is);
    }

    /**
     * Create an Service instance using the specified 
     * parameters.
     *  
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     */
    public Service createService(
        InternationalString name
        ) throws JAXRException{

        Service service = new ServiceImpl(this);
        service.setName(name);
	    
        return service;
    }

    /**
     * Create an ServiceBinding instance using the specified 
     * parameters.
     *  
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     */
    public javax.xml.registry.infomodel.ServiceBinding createServiceBinding() throws javax.xml.registry.JAXRException{

        ServiceBinding serviceBinding = new ServiceBindingImpl(this);
        return serviceBinding;
    }

    /**
     * Creates a Slot instance using the specified
     * parameters, where the value is a String.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param name the name of the Slot
     * @param value the value (a String)
     * @param slotType the slot type
     *
     * @return the Slot instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public Slot createSlot(String name, String value, String slotType)
        throws JAXRException
    {
        ArrayList al = new ArrayList();
        al.add(value);
        return createSlot(name, al, slotType);
    }

    /**
     * Creates a Slot instance using the specified
     * parameters, where the value is a Collection of Strings.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param name the name of the Slot
     * @param value the value (a Collection of Strings)
     * @param slotType the slot type
     *
     * @return the Slot instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public Slot createSlot(String name, Collection values, String slotType)
        throws JAXRException
    {
        SlotImpl slot = new SlotImpl(this);
        slot.setName(name);
        slot.setValues(values);
        slot.setSlotType(slotType);
        return slot;
    }

    /**
     * Creates an empty SpecificationLink instance.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @return the SpecificationLink instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public SpecificationLink createSpecificationLink()
        throws JAXRException
    {
        SpecificationLinkImpl specLink = new SpecificationLinkImpl(this);
        return specLink;
    }

    /**
     * Create a TelephoneNumber instance using the specified 
     * parameters.
     *  
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     */
    public javax.xml.registry.infomodel.TelephoneNumber createTelephoneNumber() throws javax.xml.registry.JAXRException{

        TelephoneNumber ph = new TelephoneNumberImpl(this);
        return ph;
    }

    /**
     * Create a User instance using the specified 
     * parameters.
     *  
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     */
    public javax.xml.registry.infomodel.User createUser() throws javax.xml.registry.JAXRException{
        UserImpl user = new UserImpl(this);
        
        return user;
    }
    
    //??Add to JAXRAPI 2.0
    public BulkResponse saveAllObjects() throws JAXRException {
        HashSet _modifiedObjects = null;
        synchronized(modifiedObjects) {
            _modifiedObjects = (HashSet)(modifiedObjects.clone());
        }
        
        return saveObjects(_modifiedObjects);
    }
    
    /**
     * Add an object to list set of modified objects
     */
    public void addModifiedObject(RegistryObject ro) {
        synchronized(modifiedObjects) {
            modifiedObjects.add(ro);
        }
    }
    
    /**
     * Remove an object from list set of modified objects
     */
    public void removeModifiedObject(RegistryObject ro) {
        synchronized(modifiedObjects) {
            modifiedObjects.remove(ro);
        }
    }
        
    
    /**
     * Saves one or more Objects to the registry. An object may be a
     * RegistryObject sub-class or a CataloguedObject.  If an object is not
     * in the registry, then it is created in the registry.  If it already
     * exists in the registry and has been modified, then its state is
     * updated (replaced) in the registry.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     * @return BulkResponse containing the Collection of keys for those
     * objects that were saved successfully and any SaveException that was
     * encountered in case of partial commit.
     */
    public BulkResponse saveObjects(Collection objects) throws JAXRException
    {
        if (objects == null || objects.size() == 0) {
            return new BulkResponseImpl();
        }

        HashSet submitObjects = new HashSet();
        HashSet processedObjects = new HashSet();
        HashSet composedObjects = new HashSet();
        String pad = "";
        
        for (Iterator it = objects.iterator(); it.hasNext(); ) {
            Object obj = it.next();
            processObject(obj, submitObjects, processedObjects, composedObjects, pad);
        }
        
        log.debug("submitObjects = " + submitObjects);
        log.debug("processedObjects = " + processedObjects);
                
        // Return value for this method
        BulkResponse response = null;

        // Send SubmitObjectsRequest for new objects
        HashMap attachMap = new HashMap();
        LeafRegistryObjectList list =
            makeLeafRegistryObjectList(submitObjects, processedObjects, composedObjects, attachMap);

        if (list != null) {
            response = doSubmitObjectsRequest(list, attachMap);
            if (response.getStatus() == BulkResponse.STATUS_SUCCESS) {
                // ROs are no longer new
                markRegistryObjectsClean(submitObjects);
            }
        }        

        return response;
    }        

    /**
     * Process an Object that may be a RegistryObject, RegistryObjectRef or something else (e.g. Slot). 
     *
     * Rules:
     *  -Do nothing for objects that are niether RegistryObjectImpl nor RegistryObjectRef (e.g. Slots)
     *  -Don't process the same object more than once
     */
    public void processObject(Object obj, HashSet submitObjects, HashSet processedObjects, HashSet composedObjects, String pad) throws JAXRException
    {
        log.debug(pad + "processObject entered: obj = " + obj);
        
        //Rule: Do nothing for objects that are niether RegistryObjectImpl nor RegistryObjectRef (e.g. Slots)
        if (!((obj instanceof RegistryObjectImpl) || (obj instanceof RegistryObjectRef))) {
            log.debug(pad + "processObject: skipping obj = " + obj);
            return;
        }
        
        //Rule: Don't process the same object more than once
        if (processedObjects.contains(obj)) {
            log.debug(pad + "processObject: returning on already processed obj = " + obj);
            return;
        }
        else {
            log.debug(pad + "processObject: processedObject.add on obj = " + obj + " processedObjects.contains = " + processedObjects.contains(obj));                
            processedObjects.add(obj);
        }
                        
        if (obj instanceof RegistryObjectRef) {
            processRegistryObjectRef((RegistryObjectRef)obj, submitObjects, processedObjects, composedObjects, pad);
        }
        else if (obj instanceof RegistryObjectImpl) {
            processRegistryObject((RegistryObjectImpl)obj, submitObjects, processedObjects, composedObjects, pad);            
        }                            
        
    }        
    
    /**
     * Process a RegistryObjectRef.
     *
     * Rules:
     *  -Submit ObjectRef for references to clean objects
     *  -Call processObject for references to dirty objects
     *
     * The ReferencedObject may be:
     *  a) A new or modified object
     *  b) An ObjectRef to an object in the registry       
     */
    public void processRegistryObjectRef(RegistryObjectRef ref, HashSet submitObjects, HashSet processedObjects, HashSet composedObjects, String pad) throws JAXRException
    {
        log.debug(pad + "processRegistryObjectRef entered: ref = " + ref);        

        //Get RegistryObject from the ref
        //Potential for optimization here?? 
        RegistryObjectImpl ro = (RegistryObjectImpl)ref.getRegistryObject("RegistryObject");
        log.debug(pad + "processRegistryObjectRef: ro = " + ro);

        if (ro != null) {        
            if (!(ro.isNew() || ro.isModified())) {
                //Rule: Submit ObjectRef for references to clean objects
                log.debug(pad + "processRegistryObjectRef: submitObject.add for clean ref  = " + ro);
                submitObjects.add(ref);
            }
            else {
                //Rule: Call processRegistryObject for references to dirty objects
                //log.debug(pad + "processRegistryObjectRef: recursing for dirty ref  = " + ro);
                processObject(ro, submitObjects, processedObjects, composedObjects, pad);
            }
        }
    }
    
    /**
     * Process a RegistryObject.
     *
     * Potential cases:
     *  1) Composed object is submitted as a composed object
     *  2) Composed object is submitted as a top level object 
     *  (e.g. create Classification, call setClassifiedObject on it and save it.)
     *  
     */   
    public void processRegistryObject(RegistryObjectImpl ro, HashSet submitObjects, HashSet processedObjects, HashSet composedObjects, String pad) throws JAXRException
    {        
        //log.debug(pad + "processRegistryObject entered: ro = " + ro);
        
        log.debug(pad + "processRegistryObject: entered submitObject.add on ro  = " + ro);
        submitObjects.add(ro);

        //Get and process Associations and Association targets implicitly for RegistryObjects being saved
        //as required by JAXR 1.0 specification.
        HashSet assObjects = ro.getAssociationsAndAssociatedObjects();
        for (Iterator it = assObjects.iterator(); it.hasNext(); ) {
            RegistryObjectImpl assObj = (RegistryObjectImpl)it.next();
            
            if (assObj.isNew() || assObj.isModified()) {
                //log.debug(pad + "processRegistryObject: recursing on assObj  = " + assObj);
                processObject(assObj, submitObjects, processedObjects, composedObjects, pad.concat(" "));
            }
        }

        //Get and process ExternalLinks implicitly for RegistryObjects being saved
        //as required by JAXR 1.0 specification.
        Collection elObjects = ro.getExternalLinks();
        for (Iterator it = elObjects.iterator(); it.hasNext(); ) {
            Object elObj = it.next();
            //log.debug(pad + "processRegistryObject: recursing on elObj  = " + elObj);
            processObject(elObj, submitObjects, processedObjects, composedObjects, pad.concat(" "));
        }

        //Rule: Don't process composed or referenced objects if object is unmodified
        if (ro.isNew() || ro.isModified()) {
            //Get and process composed objects implicitly for RegistryObjects being saved
            HashSet _composedObjects = new HashSet();
            ro.getComposedObjects(_composedObjects);
            composedObjects.addAll(_composedObjects);
            for (Iterator composedIter = _composedObjects.iterator(); composedIter.hasNext(); ) {
                Object composedObj = composedIter.next();
                //log.debug(pad + "processRegistryObject: recursing on composedObj = " + composedObj);
                processObject(composedObj, submitObjects, processedObjects, composedObjects, pad.concat(" "));
            }

            //Get and process objects referenced by RegistryObject
            HashSet refObjects = ro.getRegistryObjectRefs();
            if (refObjects != null) {
                for (Iterator refIter = refObjects.iterator(); refIter.hasNext(); ) {
                    Object refObj = refIter.next();
                    //log.debug(pad + "processRegistryObject: recursing on refObj = " + refObj);
                    processObject(refObj, submitObjects, processedObjects, composedObjects, pad.concat(" "));
                }
            }
        }        
    }
    
    private LeafRegistryObjectList makeLeafRegistryObjectList(
        HashSet objects, HashSet processedObjects, HashSet composedObjects, HashMap attachMap)
        throws JAXRException
    {
        HashSet roIds = new HashSet();
        HashSet refIds = new HashSet();
        
        if (objects == null || objects.size() == 0) {
            return null;
        }

        LeafRegistryObjectList list = new LeafRegistryObjectList();
        for (Iterator it = objects.iterator(); it.hasNext(); ) {
            LeafRegistryObjectListTypeItem item =
                new LeafRegistryObjectListTypeItem();

            Object obj = it.next();
            RegistryObjectImpl ro = null;
            
            if (obj instanceof RegistryObjectImpl) {
                ro = (RegistryObjectImpl)obj;
                String id = ro.getId();
                
                if (!composedObjects.contains(ro)) {
                
                    log.debug("makeLeafRegistryObjectList: ro=" + ro + " modified=" + ro.isModified() + " new=" + ro.isNew());
                    if ((ro.isModified()) || (ro.isNew())) {                      

                        log.debug("makeLeafRegistryObjectList: submitting " + obj);
                        if (obj instanceof Association) {
                            item.setAssociation(((AssociationImpl)obj).toBindingObject());
                        }
                        else if (obj instanceof AuditableEvent) {
                            item.setAuditableEvent(((AuditableEventImpl)obj).toBindingObject());
                        }  
                        else if (obj instanceof Classification) {
                            item.setClassification(((ClassificationImpl)obj).toBindingObject());
                        }            
                        else if (obj instanceof Concept) {
                            item.setClassificationNode(((ConceptImpl)obj).toBindingObject());
                        }
                        else if (obj instanceof ClassificationScheme) {
                            log.debug("makeLeafRegistryObjectList: setting item to scheme " + obj);
                            item.setClassificationScheme(((ClassificationSchemeImpl)obj).toBindingObject());
                        }
                        else if (obj instanceof ExternalIdentifier) {
                            item.setExternalIdentifier(((ExternalIdentifierImpl)obj).toBindingObject());
                        }            
                        else if (obj instanceof ExternalLink) {
                            item.setExternalLink(((ExternalLinkImpl)obj).toBindingObject());
                        }            
                        else if (obj instanceof ExtrinsicObject) {
                            ExtrinsicObjectImpl eo = (ExtrinsicObjectImpl)obj;
                            item.setExtrinsicObject(eo.toBindingObject());

                            DataHandler ri = eo.getRepositoryItem();
                            // ebXML RS spec 2.0 allows ExtrinsicObject to exist
                            // w/o RepositoryItem, but not JAXR 1.0
                            if (ri != null) {
                                attachMap.put(id, ri);
                            }
                        }
                        else if (obj instanceof Organization) {
                            item.setOrganization(((OrganizationImpl)obj).toBindingObject());
                        }            
                        else if (obj instanceof RegistryPackage) {
                            item.setRegistryPackage(((RegistryPackageImpl)obj).toBindingObject());
                        }
                        else if (obj instanceof Service) {
                            item.setService(((ServiceImpl)obj).toBindingObject());                
                        }            
                        else if (obj instanceof ServiceBinding) {
                            item.setServiceBinding(((ServiceBindingImpl)obj).toBindingObject());
                        }            
                        else if (obj instanceof SpecificationLink) {
                            item.setSpecificationLink(((SpecificationLinkImpl)obj).toBindingObject());
                        }
                        else if (obj instanceof User) {
                            item.setUser(((UserImpl)obj).toBindingObject());
                        }
                        
                        if (!(roIds.contains(id))) {
                            roIds.add(id);                               
                            list.addLeafRegistryObjectListTypeItem(item);
                        }
                    }
                    else { 
                        //Not modified or new. Create an ObjectRef if one has no being marshalled already
                        if (!(refIds.contains(id))) {
                            refIds.add(id); 
                              
                            org.oasis.ebxml.registry.bindings.rim.ObjectRef ebRef = new org.oasis.ebxml.registry.bindings.rim.ObjectRef();
                            ebRef.setId(id);
                            item.setObjectRef(ebRef);                    
                            log.debug("makeLeafRegistryObjectList: submitting ref to unmodified ro=" + ro);
                            list.addLeafRegistryObjectListTypeItem(item);
                        }
                    }                    
                }
                else {
                    //This is a composed object. SKip it as it will be marshalled as part of its composite
                    log.debug("makeLeafRegistryObjectList: skipping composedObject ro=" + ro + " modified=" + ro.isModified() + " new=" + ro.isNew());                    
                }
            }
            else if (obj instanceof RegistryObjectRef) {
                //There may be multiple refs to same object.
                //Only add ObjectRef if it has not been added already
                String id = ((RegistryObjectRef)obj).getId();
                
                if (!(refIds.contains(id))) {
                    refIds.add(id); 
                    org.oasis.ebxml.registry.bindings.rim.ObjectRef ebRef = new org.oasis.ebxml.registry.bindings.rim.ObjectRef();
                    ebRef.setId(id);
                    item.setObjectRef(ebRef);
                    log.debug("makeLeafRegistryObjectList: submitting ref=" + obj);
                    list.addLeafRegistryObjectListTypeItem(item);
                }
            }   
            else {
                throw new JAXRException("Unexpected ebXML object to save" + obj);
            }

        }
        log.debug("makeLeafRegistryObjectList: returning item count = " + list.getLeafRegistryObjectListTypeItemCount());
        return list;
    }

    private BulkResponse doSubmitObjectsRequest(LeafRegistryObjectList list,
                                                HashMap attachMap)
        throws JAXRException
    {
        BulkResponseImpl response = null;
        SubmitObjectsRequest req = new SubmitObjectsRequest();
        req.setLeafRegistryObjectList(list);
        StringWriter sw = new StringWriter();
        try {
            req.marshal(sw);
        } catch (org.exolab.castor.xml.MarshalException x) {
            throw new JAXRException(x);
        } catch (org.exolab.castor.xml.ValidationException x) {
            log.debug(x);
            throw new JAXRException(x);
        }
        response = regService.getSoapMessenger().sendSoapRequest(
            sw.toString(), attachMap, true);
        
        //Now setCollection with ids of objects saved
        setKeysOnBulkResponse(req, response);
        return response;
    }
    
    private BulkResponse doUpdateObjectsRequest(LeafRegistryObjectList list,
                                                HashMap attachMap)
        throws JAXRException
    {
        BulkResponseImpl response = null;
        UpdateObjectsRequest req = new UpdateObjectsRequest();
        req.setLeafRegistryObjectList(list);
        StringWriter sw = new StringWriter();
        try {
            req.marshal(sw);
        } catch (org.exolab.castor.xml.MarshalException x) {
            throw new JAXRException(x);
        } catch (org.exolab.castor.xml.ValidationException x) {
            throw new JAXRException(x);
        }
        response = regService.getSoapMessenger().sendSoapRequest(
            sw.toString(), attachMap, true);
        
        setKeysOnBulkResponse(req, response);
        
        return response;
    }

    private void setKeysOnBulkResponse(Object req, BulkResponseImpl response) throws JAXRException {
        ArrayList idList = null;
        try {
            idList = BindingUtility.getInstance().getIdsFromRequest(req);
        }
        catch (RegistryException e) {
            throw new JAXRException(e);
        }
        
        ArrayList keyList = new ArrayList();
        Iterator iter = idList.iterator();
        while (iter.hasNext()) {
            String id = (String)iter.next();
            Key key = createKey(id);
            keyList.add(key);
        }
        response.setCollection(keyList);
    }

    /**
     * Marks all RegistryObjectImpl-s as clean, ie. not new nor modified.
     *
     * @param objects Collection of RegistryObjectImpl-s
     */
    private void markRegistryObjectsClean(Collection objects) {
        for (Iterator it = objects.iterator(); it.hasNext(); ) {
            Object obj = it.next();
            
            if (obj instanceof RegistryObjectImpl) {
                RegistryObjectImpl ro = (RegistryObjectImpl)obj;
                ro.setNew(false);
                ro.setModified(false);
            }
        }
    }

    /**
     * Deprecates one or more previously submitted objects. Deprecation
     * marks an object as "soon to be deleted".  Once an object is
     * deprecated, the JAXR provider must not allow any new references
     * (e.g. new Associations, Classifications and ExternalLinks) to that
     * object to be submitted. If a client makes an API call that results
     * in a new reference to a deprecated object, the JAXR provider must
     * throw a java.lang.IllegalStateException within a
     * JAXRException. However, existing references to a deprecated object
     * continue to function normally.
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL>
     *
     * @param keys a Collection of keys for the objects to be deprecated
     *
     * @return a BulkResponse containing the Collection of keys for those
     * objects that were deprecated successfully and any JAXRException that
     * was encountered in case of partial commit
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public BulkResponse deprecateObjects(Collection keys) throws JAXRException
    {
        ObjectRefList orl = createObjectRefList(keys);

        DeprecateObjectsRequest dor = new DeprecateObjectsRequest();
        dor.setObjectRefList(orl);
        StringWriter sw = new StringWriter();
        try {
            dor.marshal(sw);
        } catch (org.exolab.castor.xml.MarshalException x) {
            throw new JAXRException(x);
        } catch (org.exolab.castor.xml.ValidationException x) {
            throw new JAXRException(x);
        }
        return regService.getSoapMessenger().sendSoapRequest(
            sw.toString(), null, true);
    }

    /**
     * Undeprecates one or more previously deprecated objects. If an object
     * was not previously deprecated, it is not an error, and no exception
     * is thrown.  Once an object is undeprecated, the JAXR provider must
     * again allow new references (e.g. new Associations, Classifications
     * and ExternalLinks) to that object to be submitted.
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL>
     *
     * @param keys a Collection of keys for the objects to be undeprecated
     *
     * @return a BulkResponse containing the Collection of keys for those
     * objects that were deprecated successfully and any JAXRException that
     * was encountered in case of partial commit
    *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public BulkResponse unDeprecateObjects(Collection keys)
        throws JAXRException
    {
        // ??eeg ebXML RS 2.0 does not have an undeprecateObjectsRequest.
        // Also, I think the method name needs to be changed to
        // "undeprecateObjects" to follow normal Java camel case rules
        // since "un" and "deprecate" are not two separate words.
        return null;
    }

    /**
     * Deletes one or more previously submitted objects from the registry.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @return BulkResponse containing the Collection of keys for those
     * objects that were deleted successfully and any DeleteException that
     * was encountered in case of partial commit.
     */
    public BulkResponse deleteObjects(Collection keys) throws JAXRException
    {
        BulkResponseImpl response = null;
        ObjectRefList orl = createObjectRefList(keys);

        RemoveObjectsRequest ror = new RemoveObjectsRequest();
        ror.setObjectRefList(orl);
        StringWriter sw = new StringWriter();
        try {
            ror.marshal(sw);
        } catch (org.exolab.castor.xml.MarshalException x) {
            throw new JAXRException(x);
        } catch (org.exolab.castor.xml.ValidationException x) {
            throw new JAXRException(x);
        }
        response = regService.getSoapMessenger().sendSoapRequest(
            sw.toString(), null, true);
        
        response.setCollection(keys);
        
        return response;
    }

    /**
     * Create a semantic equivalence between the two specified Concepts.
     * This is a convenience method to create an Association with
     * sourceObject as concept1 and targetObject as concept2 and
     * associationType as EquivalentTo.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     */ 
    public void createConceptEquivalence(Concept concept1, Concept concept2)
        throws JAXRException
    {
        BusinessQueryManagerImpl bqm = (BusinessQueryManagerImpl)regService.getBusinessQueryManager();
        Concept eqConcept =
            bqm.findConceptByPath("/urn:uuid:6902675f-2f18-44b8-888b-c91db8b96b4d/" + "EquivalentTo");
        Association assoc = createAssociation(concept2, eqConcept);
        concept1.addAssociation(assoc);
        //??eeg save assoc to Registry or is an attribute of the Connection??
    }

    /**
     * Removes the semantic equivalence, if any, between the specified two
     * Concepts.  This is a convenience method to to delete any Association
     * sourceObject as concept1 and targetObject as concept2 and
     * associationType as EquivalentTo.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     */ 
    public void deleteConceptEquivalence(Concept concept1, Concept concept2) throws JAXRException{
        // Write your code here
    }

    /**
     * Deletes one or more previously submitted objects from the registry
     * using the object keys and a specified objectType attribute.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param keys a Collection of keys for the objects to be deleted
     * @param objectType the objectType attribute for the objects to be deleted
     *
     * @return a BulkResponse containing the Collection of keys for those
     * objects that were deleted successfully and any DeleteException that
     * was encountered in case of partial commit
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public BulkResponse deleteObjects(Collection keys, String objectType)
        throws JAXRException
    {
        return deleteObjects(keys);
    }

    /**
     * Returns the parent RegistryService that created this object.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     * @return the parent RegistryService
     * 
     * @throws JAXRException if the JAXR provider encounters an internal error
     *
     * @associates <{javax.xml.registry.RegistryService}>
     */
    public RegistryService getRegistryService() throws JAXRException {
        return regService;
    }

    /**
     * @param keys Collection of objects which are typically Key-s.  Non
     * Key objects are ignored.
     *
     * @return an ObjectRefList binding object representing the list of
     * unique Keys
     */
    private ObjectRefList createObjectRefList(Collection keys)
        throws JAXRException
    {
        // Used to prevent duplicate keys from being sent
        HashSet processedIds = new HashSet();
        processedIds.add(null);

        ObjectRefList orl = new ObjectRefList();
        for (Iterator it = keys.iterator(); it.hasNext(); ) {
            Object obj = it.next();
            if (obj instanceof KeyImpl) {
                KeyImpl key = (KeyImpl)obj;
                String id = key.getId();
                if (!processedIds.contains(id)) {
                    processedIds.add(id);
                    ObjectRef ebObjRef = new ObjectRef();
                    ebObjRef.setId(id);
                    ObjectRefListItem orli = new ObjectRefListItem();
                    orli.setObjectRef(ebObjRef);
                    orl.addObjectRefListItem(orli);
                }
            }
        }
        return orl;
    }
    
    private void initializeObjectTypesMap() {
        try {
            DeclarativeQueryManager dqm = getRegistryService().getDeclarativeQueryManager();
            String queryStr = "SELECT * FROM ClassificationNode children, ClassificationNode parent where (parent.path LIKE '/urn:uuid:3188a449-18ac-41fb-be9f-99a1adca02cb/RegistryObject%') AND (parent.path NOT LIKE '/urn:uuid:3188a449-18ac-41fb-be9f-99a1adca02cb/RegistryObject/RegistryEntry/ExtrinsicObject/%') AND parent.id = children.parent";
            Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);
            BulkResponse resp = dqm.executeQuery(query);

            if ((resp!=null) &&(!(resp.getStatus() == JAXRResponse.STATUS_SUCCESS))) {
                Collection exceptions = resp.getExceptions();
                Iterator iter = exceptions.iterator();
                while (iter.hasNext()) {
                    Exception e = (Exception)iter.next();
                    e.printStackTrace();
                }

                return;
            }
            
            objectTypesMap = new HashMap();

            Collection concepts = resp.getCollection();
            Iterator iter = concepts.iterator();
            while (iter.hasNext()) {
                Concept concept = (Concept)iter.next();
                String value = concept.getValue();
                if (value.equals("ClassificationNode")) {
                    value = "Concept";
                }

                objectTypesMap.put(value, concept);            
            }
        }
        catch (JAXRException e) {
            e.printStackTrace();
        }
    }
    
    public HashMap getObjectTypesMap() {
        if (objectTypesMap == null) {
            initializeObjectTypesMap();
        }
        return objectTypesMap;
    }
}
