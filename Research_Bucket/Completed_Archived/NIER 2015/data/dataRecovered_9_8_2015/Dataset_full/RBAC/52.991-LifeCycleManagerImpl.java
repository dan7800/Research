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
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/server/lcm/LifeCycleManagerImpl.java,v 1.26 2004/03/22 03:22:50 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.lcm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.activation.DataHandler;

import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.LifeCycleManager;
import org.freebxml.omar.common.OMARException;
import org.freebxml.omar.common.RegistryException;
import org.freebxml.omar.common.RepositoryItem;
import org.freebxml.omar.common.UUIDFactory;
import org.freebxml.omar.server.common.UUIDNotUniqueException;
import org.freebxml.omar.server.lcm.quota.QuotaExceededException;
import org.freebxml.omar.server.lcm.quota.QuotaServiceImpl;
import org.freebxml.omar.server.persistence.AssociateToDeprecatedRegistryEntryException;
import org.freebxml.omar.server.persistence.DuplicateSlotsException;
import org.freebxml.omar.server.persistence.InvalidURLsException;
import org.freebxml.omar.server.persistence.NonRegistryEntryFoundException;
import org.freebxml.omar.server.persistence.ObjectsNotFoundException;
import org.freebxml.omar.server.persistence.ReferencedObjectNotFoundException;
import org.freebxml.omar.server.persistence.ReferencesExistException;
import org.freebxml.omar.server.persistence.RegistryObjectExistsException;
import org.freebxml.omar.server.persistence.SlotNotExistException;
import org.freebxml.omar.server.persistence.SlotsExistException;
import org.freebxml.omar.server.persistence.SlotsParentNotExistException;
import org.freebxml.omar.server.repository.RepositoryManager;
import org.freebxml.omar.server.repository.RepositoryManagerFactory;
import org.freebxml.omar.server.security.UnauthorizedRequestException;
import org.freebxml.omar.server.security.authorization.AuthorizationResult;
import org.freebxml.omar.server.security.authorization.AuthorizationServiceImpl;
import org.oasis.ebxml.registry.bindings.lcm.AddSlotsRequest;
import org.oasis.ebxml.registry.bindings.lcm.ApproveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.DeletionScope;
import org.oasis.ebxml.registry.bindings.lcm.DeprecateObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.RemoveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.RemoveSlotsRequest;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.UndeprecateObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.UpdateObjectsRequest;
import org.oasis.ebxml.registry.bindings.rim.ExternalLink;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObject;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;
import org.oasis.ebxml.registry.bindings.rim.RegistryEntryType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.ServiceBinding;
import org.oasis.ebxml.registry.bindings.rim.UserType;
import org.oasis.ebxml.registry.bindings.rs.RegistryError;
import org.oasis.ebxml.registry.bindings.rs.RegistryErrorList;
import org.oasis.ebxml.registry.bindings.rs.RegistryErrorListType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponse;


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
    private static BindingUtility bu = BindingUtility.getInstance();

    /**
     *
     * @associates <{org.freebxml.omar.server.persistence.PersistenceManagerImpl}>
     */
    org.freebxml.omar.server.persistence.PersistenceManager pm = org.freebxml.omar.server.persistence.PersistenceManagerFactory.getInstance()
                                                                                                                               .getPersistenceManager();
    /**
     *
     * @associates <{org.freebxml.omar.common.QueryManagerImpl}>
     */
    org.freebxml.omar.common.QueryManager qm = org.freebxml.omar.common.QueryManagerFactory.getInstance().getQueryManager();
    QuotaServiceImpl qs = QuotaServiceImpl.getInstance();
    org.freebxml.omar.server.common.Utility util = org.freebxml.omar.server.common.Utility.getInstance();
    RepositoryManager rm = RepositoryManagerFactory.getInstance()
                                                   .getRepositoryManager();
    UUIDFactory uf = UUIDFactory.getInstance();

    /**
     *
     * @associates <{org.freebxml.omar.server.persistence.ContentIndexingManager}>
     */
    ContentIndexingManager cim = ContentIndexingManager.getInstance();
    private org.apache.commons.logging.Log log = LogFactory.getLog(this.getClass());

    protected LifeCycleManagerImpl() {
    }

    public static LifeCycleManagerImpl getInstance() {
        if (instance == null) {
            synchronized (LifeCycleManagerImpl.class) {
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
    public RegistryResponse submitObjects(UserType user,
        SubmitObjectsRequest req, HashMap idToRepositoryItemMap)
        throws RegistryException {
        RegistryResponse resp = null;
        RegistryErrorList el = null;

        try {
            AuthorizationResult authRes = 
                AuthorizationServiceImpl.getInstance().checkAuthorization(user, req);
            authRes.throwExceptionOn(AuthorizationResult.PERMIT_NONE|AuthorizationResult.PERMIT_SOME);
                
            resp = bu.rsFac.createRegistryResponse();
            el = bu.rsFac.createRegistryErrorList();

            RegistryObjectListType objs = req.getRegistryObjectList();
            
            List roList = new ArrayList();
            Map orefMap = new HashMap();
            bu.getObjectRefsAndRegistryObjects(objs, roList, orefMap);

            checkObjects(roList, orefMap, el);

            if ((idToRepositoryItemMap != null) &&
                    (!(idToRepositoryItemMap.isEmpty()))) {
                qs.checkQuota(user.getId());

                //fix ri ID to match 
                //first ExtrinsicObject (in case where ri is submitted without id)
                //only works for submission of one ri and one ExtrinsicObject
                correctRepositoryItemId(roList, idToRepositoryItemMap);

                // It will select which repository items already exist and update
                HashMap idToNotExistingItemsMap = updateExistingRepositoryItems(idToRepositoryItemMap);
                storeRepositoryItems(idToNotExistingItemsMap);
            }

            /*
             * For RegistryObjects, the DAO will take care which objects already
             * exist and update them instead
             */
            log.info("Calling pm.insert at: " + System.currentTimeMillis());
            pm.insert(user, roList);
                        
            log.info("Done Calling pm.insert at: " +
                System.currentTimeMillis());

            resp.setStatus(org.oasis.ebxml.registry.bindings.rs.Status.SUCCESS);

            if (el.getRegistryError().size() > 0) {
                // warning exists
                resp.setRegistryErrorList(el);
            }

            //Now perform any content indexing for ExtrinsicObjects
            log.info("Calling indexContent at: " + System.currentTimeMillis());
            indexContent(user, roList, idToRepositoryItemMap);
            log.info("Done Calling indexContent at: " +
                System.currentTimeMillis());
        } catch (InvalidURLsException e) {
            // We should have unified mapping in util to generate
            // RegistryErrorList Later
            try {
                Iterator roIter = e.getSourceRegistryObjects().iterator();

                while (roIter.hasNext()) {
                    Object ro = roIter.next();
                    RegistryError re = bu.rsFac.createRegistryError();

                    if (ro instanceof ExternalLink) {
                        ExternalLink extLink = (ExternalLink) ro;
                        re.setValue("The ExternalLink with id " +
                            extLink.getId() +
                            " is not resolvable, the Http URL is " +
                            extLink.getExternalURI());
                    } else if (ro instanceof ServiceBinding) {
                        ServiceBinding serviceBinding = (ServiceBinding) ro;
                        re.setValue("The ServiceBinding with id " +
                            serviceBinding.getId() +
                            " is not resolvable, the Http URL is " +
                            serviceBinding.getAccessURI());
                    } else {
                        re.setValue("Internal Error happens, unknown " +
                            "RegistryObjectType");
                    }

                    re.setErrorCode("unknown");
                    re.setCodeContext("LifeCycleManagerImpl.submitObjects");
                    el.getRegistryError().add(re);
                }

                resp = bu.rsFac.createRegistryResponse();
                resp.setStatus(org.oasis.ebxml.registry.bindings.rs.Status.FAILURE);
                resp.setRegistryErrorList(el);
            } catch (javax.xml.bind.JAXBException e1) {
                log.error(e1);
            }
        } catch (ReferencesExistException e) {
            try {
                RegistryError re = bu.rsFac.createRegistryError();
                re.setValue(e.getMessage());
                re.setErrorCode("unknown");
                re.setCodeContext("LifeCycleManagerImpl.submitObjects");
                el.getRegistryError().add(re);
                resp = bu.rsFac.createRegistryResponse();
                resp.setStatus(org.oasis.ebxml.registry.bindings.rs.Status.FAILURE);
                resp.setRegistryErrorList(el);
            } catch (javax.xml.bind.JAXBException e1) {
                log.error(e1);
            }
        } catch (UUIDNotUniqueException e) {
            try {
                RegistryError re = bu.rsFac.createRegistryError();
                re.setValue(e.getMessage());
                re.setErrorCode("unknown");
                re.setCodeContext("LifeCycleManagerImpl.submitObjects");
                el.getRegistryError().add(re);
                resp = bu.rsFac.createRegistryResponse();
                resp.setStatus(org.oasis.ebxml.registry.bindings.rs.Status.FAILURE);
                resp.setRegistryErrorList(el);
            } catch (javax.xml.bind.JAXBException e1) {
                log.error(e1);
            }
        } catch (ReferencedObjectNotFoundException e) {
            try {
                RegistryError re = bu.rsFac.createRegistryError();
                re.setValue(e.getMessage());
                re.setErrorCode("unknown");
                re.setCodeContext("LifeCycleManagerImpl.submitObjects");
                el.getRegistryError().add(re);
                resp = bu.rsFac.createRegistryResponse();
                resp.setStatus(org.oasis.ebxml.registry.bindings.rs.Status.FAILURE);
                resp.setRegistryErrorList(el);
            } catch (javax.xml.bind.JAXBException e1) {
                log.error(e1);
            }
        } catch (AssociateToDeprecatedRegistryEntryException e) {
            try {
                RegistryError re = bu.rsFac.createRegistryError();
                re.setValue(e.getMessage());
                re.setErrorCode("unknown");
                re.setCodeContext("LifeCycleManagerImpl.submitObjects");
                el.getRegistryError().add(re);
                resp = bu.rsFac.createRegistryResponse();
                resp.setStatus(org.oasis.ebxml.registry.bindings.rs.Status.FAILURE);
                resp.setRegistryErrorList(el);
            } catch (javax.xml.bind.JAXBException e1) {
                log.error(e1);
            }
        } catch (RegistryObjectExistsException e) {
            try {
                RegistryError re = bu.rsFac.createRegistryError();
                re.setValue(e.getMessage());
                re.setErrorCode("unknown");
                re.setCodeContext("LifeCycleManagerImpl.submitObjects");
                el.getRegistryError().add(re);
                resp = bu.rsFac.createRegistryResponse();
                resp.setStatus(org.oasis.ebxml.registry.bindings.rs.Status.FAILURE);
                resp.setRegistryErrorList(el);
            } catch (javax.xml.bind.JAXBException e1) {
                log.error(e1);
            }
        } catch (QuotaExceededException e) {
            try {
                RegistryError re = bu.rsFac.createRegistryError();
                re.setValue(e.getMessage());
                re.setErrorCode("unknown");
                re.setCodeContext("LifeCycleManagerImpl.submitObjects");
                el.getRegistryError().add(re);
                resp = bu.rsFac.createRegistryResponse();
                resp.setStatus(org.oasis.ebxml.registry.bindings.rs.Status.FAILURE);
                resp.setRegistryErrorList(el);
            } catch (javax.xml.bind.JAXBException e1) {
                log.error(e1);
            }
        } catch (UnauthorizedRequestException e) {
            try {
                // UnauthorizedRequestException will be thrown if updating objects through SubmitObjectsRequest but the user is not authorized
                RegistryError re = bu.rsFac.createRegistryError();
                re.setValue(e.getMessage());
                re.setErrorCode("unknown");
                re.setCodeContext("LifeCycleManagerImpl.submitObjects");
                el.getRegistryError().add(re);
                resp = bu.rsFac.createRegistryResponse();
                resp.setStatus(org.oasis.ebxml.registry.bindings.rs.Status.FAILURE);
                resp.setRegistryErrorList(el);
            } catch (javax.xml.bind.JAXBException e1) {
                log.error(e1);
            }
        } catch (RegistryException e) {
            resp = util.createRegistryResponseFromThrowable(e,
                    "LifeCycleManagerImpl.submitObjects", "Unknown");

            // Append any warnings
            List errs = el.getRegistryError();
            RegistryErrorListType newEl = resp.getRegistryErrorList();
            newEl.getRegistryError().addAll(errs);
        } catch (javax.xml.bind.JAXBException e) {
            e.printStackTrace();
        } catch (org.freebxml.omar.common.OMARException e) {
            e.printStackTrace();
        }

        return resp;
    }

    /**
     * Stores the repository items in idToRepositoryItemMap in the repository
     * @throws RegistryException when the items already exist
     */
    private void storeRepositoryItems(HashMap idToRepositoryItemMap) throws RegistryException {
        if (idToRepositoryItemMap != null) {
            Collection keySet = idToRepositoryItemMap.keySet();

            if (keySet != null) {
                Iterator iter = keySet.iterator();

                while (iter.hasNext()) {
                    String id = (String) iter.next();
                    RepositoryItem ri = (RepositoryItem) idToRepositoryItemMap.get(id);

                    DataHandler dh = ri.getDataHandler();


                    // Inserting the repository item
                    rm.insert(ri);
                }
            }
        }
    }

    /**
     * It should be called by submitObjects() to update existing Repository Items
     * @return HashMap of id To RepositoryItem, which are not exisitng
     */
    private HashMap updateExistingRepositoryItems(HashMap idToRepositoryItemMap)
        throws RegistryException {
        Iterator itemsIdsIter = idToRepositoryItemMap.keySet().iterator();

        // To find out which RI with a id does not exist
        List itemsIds = new java.util.ArrayList();

        while (itemsIdsIter.hasNext()) {
            String itemsId = (String) itemsIdsIter.next();
            itemsIds.add(itemsId);
        }

        List notExistItemsIds = rm.itemsExist(itemsIds);

        //System.err.println((String)notExistItemsIds.get(0) + "!!!!!");
        // Create two maps to store existing and non-existing items
        HashMap notExistItems = new HashMap();
        HashMap existingItems = new HashMap();

        for (int i = 0; i < itemsIds.size(); i++) {
            String id = (String) itemsIds.get(i);
            String longId = null;

            if (!id.startsWith("urn:uuid:")) {
                longId = "urn:uuid:" + id;
            } else {
                longId = id;
            }

            if (notExistItemsIds.contains(longId)) {
                notExistItems.put(id, idToRepositoryItemMap.get(id));
            } else {
                existingItems.put(id, idToRepositoryItemMap.get(id));
            }
        }

        updateRepositoryItems(existingItems);

        return notExistItems;
    }


    /**
     * Checks each object including composed objects.
     */
    private void checkObjects(List objs, Map orefMap, RegistryErrorList errorList)
        throws RegistryException {

        //Get all submitted objects including composed objects that are part of the submission
        //so that they can be used to resolve references
        Set submittedObjects = new HashSet();
        submittedObjects.addAll(objs);
        Set composedObjects = bu.getComposedRegistryObjects(objs, -1);
        submittedObjects.addAll(composedObjects);
        
        //Get set of ids for all submitted objects
        List submittedIds = bu.getIdsFromRegistryObjects(submittedObjects);

        HashMap idMap = new HashMap();
        
        //Check each 
        Iterator iter = submittedObjects.iterator();
        while (iter.hasNext()) {
            RegistryObjectType object = (RegistryObjectType)iter.next();
            checkObject(object, orefMap, submittedIds, idMap);
        }
    }
    
    /**
     * Checks specified object.
     *
     * @param ro the RegistryObjectType to be checked.
     *
     * @param submittedIds ids of all submited objects whether top level or composed.
     */
    private void checkObject(RegistryObjectType ro, Map orefMap, List submittedIds, HashMap idMap)
        throws RegistryException {

        checkId(ro, submittedIds, idMap);
        resolveObjectReferences(ro, orefMap, submittedIds, idMap);
    }
    
    /**
     * Check if id is a proper UUID. If not make a proper UUID based URN and add
     * a mapping in idMap between old and new Id.
     *
     * @param submittedIds The ArrayList holding ids of all objects (including composed objects) submitted.
     *
     * @param idMap The HashMap with old temporary id to new permanent id mapping.
     *
     * @throws UUIDNotUniqueException if any UUID is not unique within a
     * SubmitObjectsRequest
     */
    private void checkId(RegistryObjectType ro, List submittedIds, HashMap idMap)
    throws RegistryException {
        String id = ro.getId();;
                
        org.freebxml.omar.common.Utility util = org.freebxml.omar.common.Utility.getInstance();
        if (!util.isValidRegistryId(id)) {
            // Generate permanent id for this tyemporary id
            String newId = util.createId();
            ro.setId(newId);
            idMap.put(id, newId);
        }        
    }
        
    /*
     * Resolves each ObjectRef within the specified object.
     *
     * @paqram obj the object whose reference attribute are being checked for being resolvable.
     * 
     * @param idMap The HashMap with old temporary id to new permanent id mapping.
     *
     * @param submittedIds Set ids of all submited objects whether top level or composed. Used to resolve references to objects that are part of submission.
     */
    private void resolveObjectReferences(RegistryObjectType obj, Map orefMap, List submittedIds, HashMap idMap) 
        throws RegistryException {

        try {
            if (obj == null) {
                return;
            }                        
            
            //Get Set of ids for objects referenced from obj
            Set refIds = bu.getObjectRefsInRegistryObject(obj, idMap);                        
                        
            //Check that each ref is resolvable
            Iterator iter = refIds.iterator();
            while (iter.hasNext()) {
                
                String refId = (String)iter.next();;

                ObjectRefType ref = (ObjectRefType)orefMap.get(refId);
                
                String refHome = null;
                if (ref != null) {
                    refHome = ref.getHome();
                }
                
                if (refHome != null) {
                    //This is a Remote ObjectRef. Resolve reference by creating a replica
                    ReplicationManagerImpl.getInstance().createReplica(ref);                    
                }  else {                
                    //First check if resolved within submittedIds
                    if (!(submittedIds.contains(refId))) {
                        //ref not resolved within submitted objects
                        //Next check if it resolves within registry.
                        qm.getRegistryObject(refId);
                    }
                }
            }            
        }
        catch (OMARException e) {
            throw new RegistryException(e);
        }
    }

    private void indexContent(UserType user, List al,
        HashMap idToRepositoryItemMap) {
        try {
            Iterator iter = al.iterator();

            while (iter.hasNext()) {
                Object obj = iter.next();

                if (obj instanceof ExtrinsicObjectType) {
                    ExtrinsicObjectType eo = (ExtrinsicObjectType) obj;

                    try {
                        RepositoryItem indexableContent = (RepositoryItem) idToRepositoryItemMap.get(eo.getId());
                        RegistryObjectListType objs = cim.catalogContent(eo,
                                indexableContent);

                        List indexedMetadata = bu.getRegistryObjectList(objs);


                        /*
                         * For RegistryObjects, the DAO will take care which objects already
                         * exist and update them instead.
                         *
                         * Metadata for each ExtrisnicObject is stored independently in a separate transaction.
                         */
                        pm.insert(user, indexedMetadata);
                    } catch (RegistryException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (OMARException e) {
            e.printStackTrace();
        }
    }

    /** Approves one or more previously submitted objects */
    public RegistryResponse approveObjects(UserType user,
        ApproveObjectsRequest req) {
        RegistryResponse resp = null;
        RegistryErrorList el = null;

        try {
            AuthorizationResult authRes = 
                AuthorizationServiceImpl.getInstance().checkAuthorization(user, req);
            authRes.throwExceptionOn(AuthorizationResult.PERMIT_NONE|AuthorizationResult.PERMIT_SOME);
            
            el = bu.rsFac.createRegistryErrorList();

            List idList = new java.util.ArrayList();
            List orefs = req.getObjectRefList().getObjectRef();
            Iterator orefsIter = orefs.iterator();

            while (orefsIter.hasNext()) {
                ObjectRefType oref = (ObjectRefType) orefsIter.next();
                idList.add(oref.getId());
            }

            pm.updateStatus(user, idList,
                org.oasis.ebxml.registry.bindings.rim.Status.APPROVED, el);
            resp = bu.rsFac.createRegistryResponse();
            resp.setStatus(org.oasis.ebxml.registry.bindings.rs.Status.SUCCESS);

            if (el.getRegistryError().size() > 0) {
                // warning exists
                resp.setRegistryErrorList(el);
            }
        } catch (ObjectsNotFoundException e) {
            try {
                Iterator notExistIt = e.getNotExistIds().iterator();

                while (notExistIt.hasNext()) {
                    String id = (String) notExistIt.next();
                    RegistryError re = bu.rsFac.createRegistryError();
                    re.setValue("The object with id " + id + " does not exist");
                    re.setErrorCode("unknown");
                    re.setCodeContext("LifeCycleManagerImpl.approveObjects");
                    el.getRegistryError().add(re);
                }

                resp = bu.rsFac.createRegistryResponse();
                resp.setStatus(org.oasis.ebxml.registry.bindings.rs.Status.FAILURE);
                resp.setRegistryErrorList(el);
            } catch (javax.xml.bind.JAXBException e1) {
                log.error(e1);
            }
        } catch (NonRegistryEntryFoundException e) {
            try {
                RegistryError re = bu.rsFac.createRegistryError();
                re.setValue(e.getMessage());
                re.setErrorCode("unknown");
                re.setCodeContext("LifeCycleManagerImpl.approveObjects");
                el.getRegistryError().add(re);
                resp = bu.rsFac.createRegistryResponse();
                resp.setStatus(org.oasis.ebxml.registry.bindings.rs.Status.FAILURE);
                resp.setRegistryErrorList(el);
            } catch (javax.xml.bind.JAXBException e1) {
                log.error(e1);
            }
        } catch (RegistryException e) {
            resp = util.createRegistryResponseFromThrowable(e,
                    "LifeCycleManagerImpl.approveObjects", "Unknown");

            // Append any warnings
            List errs = el.getRegistryError();
            RegistryErrorListType newEl = resp.getRegistryErrorList();
            newEl.getRegistryError().addAll(errs);
        } catch (javax.xml.bind.JAXBException e) {
            resp = util.createRegistryResponseFromThrowable(e,
                    "LifeCycleManagerImpl.approveObjects", "Unknown");

            // Append any warnings
            List errs = el.getRegistryError();
            RegistryErrorListType newEl = resp.getRegistryErrorList();
            newEl.getRegistryError().addAll(errs);
        }

        return resp;
    }

    /**
     * @throws RegistryException when the Repository items do not exist
     */
    private void updateRepositoryItems(HashMap idToRepositoryItemMap)
        throws RegistryException {
        if (idToRepositoryItemMap != null) {
            Collection keySet = idToRepositoryItemMap.keySet();

            if (keySet != null) {
                Iterator iter = keySet.iterator();

                while (iter.hasNext()) {
                    String id = (String) iter.next();
                    RepositoryItem ri = (RepositoryItem) idToRepositoryItemMap.get(id);

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

    public RegistryResponse updateObjects(UserType user,
        UpdateObjectsRequest req, HashMap idToRepositoryMap) {
        RegistryResponse resp = null;
        RegistryErrorList el = null;

        try {
            try {
                AuthorizationResult authRes = 
                    AuthorizationServiceImpl.getInstance().checkAuthorization(user, req);
                authRes.throwExceptionOn(AuthorizationResult.PERMIT_NONE|AuthorizationResult.PERMIT_SOME);
                
                el = bu.rsFac.createRegistryErrorList();

                RegistryObjectListType objs = req.getRegistryObjectList();
                
                List roList = new ArrayList();
                Map orefMap = new HashMap();
                bu.getObjectRefsAndRegistryObjects(objs, roList, orefMap);
                System.err.println("LifeCycleManager, size: " + roList.size());
                checkObjects(roList, orefMap, el);

                if (idToRepositoryMap != null) {
                    qs.checkQuota(user.getId());

                    //fix ri ID to match first ExtrinsicObject 
                    //(in case where ri is submitted without id OR
                    //id doesn't match EO)
                    //only works for submission of one ri and one ExtrinsicObject
                    correctRepositoryItemId(roList, idToRepositoryMap);

                    updateRepositoryItems(idToRepositoryMap);
                }

                pm.update(user, roList);
                resp = bu.rsFac.createRegistryResponse();
                resp.setStatus(org.oasis.ebxml.registry.bindings.rs.Status.SUCCESS);

                if (el.getRegistryError().size() > 0) {
                    // warning exists
                    resp.setRegistryErrorList(el);
                }
            } catch (ObjectsNotFoundException e) {
                Iterator notExistIt = e.getNotExistIds().iterator();

                while (notExistIt.hasNext()) {
                    String id = (String) notExistIt.next();
                    RegistryError re = bu.rsFac.createRegistryError();
                    re.setValue("The object with id " + id + " does not exist");
                    re.setErrorCode("unknown");
                    re.setCodeContext("LifeCycleManagerImpl.updateObjects");
                    el.getRegistryError().add(re);
                }

                resp = bu.rsFac.createRegistryResponse();
                resp.setStatus(org.oasis.ebxml.registry.bindings.rs.Status.FAILURE);
                resp.setRegistryErrorList(el);
            } catch (ReferencedObjectNotFoundException e) {
                RegistryError re = bu.rsFac.createRegistryError();
                re.setValue(e.getMessage());
                re.setErrorCode("unknown");
                re.setCodeContext("LifeCycleManagerImpl.updateObjects");
                el.getRegistryError().add(re);
                resp = bu.rsFac.createRegistryResponse();
                resp.setStatus(org.oasis.ebxml.registry.bindings.rs.Status.FAILURE);
                resp.setRegistryErrorList(el);
            } catch (AssociateToDeprecatedRegistryEntryException e) {
                RegistryError re = bu.rsFac.createRegistryError();
                re.setValue(e.getMessage());
                re.setErrorCode("unknown");
                re.setCodeContext("LifeCycleManagerImpl.submitObjects");
                el.getRegistryError().add(re);
                resp = bu.rsFac.createRegistryResponse();
                resp.setStatus(org.oasis.ebxml.registry.bindings.rs.Status.FAILURE);
                resp.setRegistryErrorList(el);
            } catch (InvalidURLsException e) {
                // We should have unified mapping in util to generate
                // RegistryErrorList Later
                Iterator roIter = e.getSourceRegistryObjects().iterator();

                while (roIter.hasNext()) {
                    Object ro = roIter.next();
                    RegistryError re = bu.rsFac.createRegistryError();

                    if (ro instanceof ExternalLink) {
                        ExternalLink extLink = (ExternalLink) ro;
                        re.setValue("The ExternalLink with id " +
                            extLink.getId() +
                            " is not resolvable, the Http URL is " +
                            extLink.getExternalURI());
                    } else if (ro instanceof ServiceBinding) {
                        ServiceBinding serviceBinding = (ServiceBinding) ro;
                        re.setValue("The ServiceBinding with id " +
                            serviceBinding.getId() +
                            " is not resolvable, the Http URL is " +
                            serviceBinding.getAccessURI());
                    } else {
                        re.setValue("Internal Error happens, unknown " +
                            "RegistryObjectType");
                    }

                    re.setErrorCode("unknown");
                    re.setCodeContext("LifeCycleManagerImpl.submitObjects");
                    el.getRegistryError().add(re);
                }

                resp = bu.rsFac.createRegistryResponse();
                resp.setStatus(org.oasis.ebxml.registry.bindings.rs.Status.FAILURE);
                resp.setRegistryErrorList(el);
            } catch (ReferencesExistException e) {
                RegistryError re = bu.rsFac.createRegistryError();
                re.setValue("Deleting object denied. " + e.getMessage());
                re.setErrorCode("unknown");
                re.setCodeContext("LifeCycleManagerImpl.updateObjects");
                el.getRegistryError().add(re);
                resp = bu.rsFac.createRegistryResponse();
                resp.setStatus(org.oasis.ebxml.registry.bindings.rs.Status.FAILURE);
                resp.setRegistryErrorList(el);
            } catch (QuotaExceededException e) {
                RegistryError re = bu.rsFac.createRegistryError();
                re.setValue(e.getMessage());
                re.setErrorCode("unknown");
                re.setCodeContext("LifeCycleManagerImpl.updateObjects");
                el.getRegistryError().add(re);
                resp = bu.rsFac.createRegistryResponse();
                resp.setStatus(org.oasis.ebxml.registry.bindings.rs.Status.FAILURE);
                resp.setRegistryErrorList(el);
            } catch (OMARException e) {
                resp = util.createRegistryResponseFromThrowable(e,
                        "LifeCycleManagerImpl.updateObjects", "Unknown");

                // Append any warnings
                List errs = el.getRegistryError();
                RegistryErrorListType newEl = resp.getRegistryErrorList();
                newEl.getRegistryError().addAll(errs);
            }
        } catch (javax.xml.bind.JAXBException e) {
            log.error(e);
        }

        return resp;
    }

    /** Deprecates one or more previously submitted objects */
    public RegistryResponse deprecateObjects(UserType user,
        DeprecateObjectsRequest req) {
        RegistryResponse resp = null;

        try {
            RegistryErrorList el = bu.rsFac.createRegistryErrorList();

            try {
                AuthorizationResult authRes = 
                    AuthorizationServiceImpl.getInstance().checkAuthorization(user, req);
                authRes.throwExceptionOn(AuthorizationResult.PERMIT_NONE|AuthorizationResult.PERMIT_SOME);
                List idList = new java.util.ArrayList();
                List orefs = req.getObjectRefList().getObjectRef();
                Iterator orefsIter = orefs.iterator();

                while (orefsIter.hasNext()) {
                    ObjectRefType oref = (ObjectRefType) orefsIter.next();
                    idList.add(oref.getId());
                }

                pm.updateStatus(user, idList,
                    org.oasis.ebxml.registry.bindings.rim.Status.DEPRECATED, el);
                resp = bu.rsFac.createRegistryResponse();
                resp.setStatus(org.oasis.ebxml.registry.bindings.rs.Status.SUCCESS);

                if (el.getRegistryError().size() > 0) {
                    // warning exists
                    resp.setRegistryErrorList(el);
                }
            } catch (ObjectsNotFoundException e) {
                Iterator notExistIt = e.getNotExistIds().iterator();

                while (notExistIt.hasNext()) {
                    String id = (String) notExistIt.next();
                    RegistryError re = bu.rsFac.createRegistryError();
                    re.setValue("The object with id " + id + " does not exist");
                    re.setErrorCode("unknown");
                    re.setCodeContext("LifeCycleManagerImpl.deprecateObjects");
                    el.getRegistryError().add(re);
                }

                resp = bu.rsFac.createRegistryResponse();
                resp.setStatus(org.oasis.ebxml.registry.bindings.rs.Status.FAILURE);
                resp.setRegistryErrorList(el);
            } catch (NonRegistryEntryFoundException e) {
                RegistryError re = bu.rsFac.createRegistryError();
                re.setValue(e.getMessage());
                re.setErrorCode("unknown");
                re.setCodeContext("LifeCycleManagerImpl.deprecateObjects");
                el.getRegistryError().add(re);
                resp = bu.rsFac.createRegistryResponse();
                resp.setStatus(org.oasis.ebxml.registry.bindings.rs.Status.FAILURE);
                resp.setRegistryErrorList(el);
            } catch (RegistryException e) {
                resp = util.createRegistryResponseFromThrowable(e,
                        "LifeCycleManagerImpl.deprecateObjects", "Unknown");

                // Append any warnings
                List errs = el.getRegistryError();
                RegistryErrorListType newEl = resp.getRegistryErrorList();
                newEl.getRegistryError().addAll(errs);
            }
        } catch (javax.xml.bind.JAXBException e) {
            log.error(e);
        }

        return resp;
    }
    
    //TODO:
    public RegistryResponse unDeprecateObjects(UserType user, UndeprecateObjectsRequest req) throws RegistryException {
        throw new RegistryException("Unimplemented");
    }    

    /**
     * Removes one or more previously submitted objects from the registry. If the
     * deletionScope is "DeleteRepositoryItemOnly", it will assume all the
     * ObjectRef under ObjectRefList is referencing repository items. If the
     * deletionScope is "DeleteAll", the reference may be either RegistryObject
     * or repository item. In both case, if the referenced object cannot be found,
     * RegistryResponse with errors list will be returned.
     */
    public RegistryResponse removeObjects(UserType user,
        RemoveObjectsRequest req) {
        RegistryResponse resp = null;

        try {
            try {
                AuthorizationResult authRes = 
                    AuthorizationServiceImpl.getInstance().checkAuthorization(user, req);
                authRes.throwExceptionOn(AuthorizationResult.PERMIT_NONE|AuthorizationResult.PERMIT_SOME);
                
                List idList = new java.util.ArrayList();
                List orefs = req.getObjectRefList().getObjectRef();
                Iterator orefsIter = orefs.iterator();

                while (orefsIter.hasNext()) {
                    ObjectRefType oref = (ObjectRefType) orefsIter.next();
                    idList.add(oref.getId());
                }

                DeletionScope deletionScope = DeletionScope.DELETE_ALL;

                if (req.getDeletionScope() != null) {
                    deletionScope = req.getDeletionScope();
                }

                //DeletionScope=DeleteRepositoryItemOnly. If any repository item
                //does not exist, it will stop
                if (deletionScope == DeletionScope.DELETE_REPOSITORY_ITEM_ONLY) {
                    List notExist = rm.itemsExist(idList);

                    if (notExist.size() > 0) {
                        throw new ObjectsNotFoundException(notExist);
                    }

                    rm.delete(idList);
                } else if (deletionScope == DeletionScope.DELETE_ALL) {
                    //Check all referenced objects exist, all the objects should
                    //be repository items or/and Registry Object                
                    List notExist = pm.registryObjectsExist(user, idList);

                    if (notExist.size() > 0) {
                        throw new ObjectsNotFoundException(notExist);
                    }

                    //find out which id is not an id of a repository item (i.e.
                    //referencing RO only
                    List nonItemsIds = rm.itemsExist(idList);

                    //find out which id is an id of a repository item
                    List itemsIds = new java.util.ArrayList();
                    Iterator idListIt = idList.iterator();

                    while (idListIt.hasNext()) {
                        Object id = idListIt.next();

                        if (!nonItemsIds.contains(id)) {
                            itemsIds.add(id);
                        }
                    }

                    // Delete the repository items
                    rm.delete(itemsIds);

                    //Delete all ROs with the ids
                    pm.delete(user, idList);
                } else {
                    throw new RegistryException("Undefined deletionScope");
                }

                resp = bu.rsFac.createRegistryResponse();
                resp.setStatus(org.oasis.ebxml.registry.bindings.rs.Status.SUCCESS);
            } catch (ObjectsNotFoundException e) {
                RegistryErrorList el = bu.rsFac.createRegistryErrorList();
                Iterator notExistIt = e.getNotExistIds().iterator();

                while (notExistIt.hasNext()) {
                    String id = (String) notExistIt.next();
                    RegistryError re = bu.rsFac.createRegistryError();
                    re.setValue("The object with id " + id + " does not exist");
                    re.setErrorCode("unknown");
                    re.setCodeContext("LifeCycleManagerImpl.removeObjects");
                    el.getRegistryError().add(re);
                }

                resp = bu.rsFac.createRegistryResponse();
                resp.setStatus(org.oasis.ebxml.registry.bindings.rs.Status.FAILURE);
                resp.setRegistryErrorList(el);
            } catch (ReferencesExistException e) {
                RegistryErrorList el = bu.rsFac.createRegistryErrorList();
                RegistryError re = bu.rsFac.createRegistryError();
                re.setValue("Deleting object denied. " + e.getMessage());
                re.setErrorCode("unknown");
                re.setCodeContext("LifeCycleManagerImpl.removeObjects");
                el.getRegistryError().add(re);
                resp = bu.rsFac.createRegistryResponse();
                resp.setStatus(org.oasis.ebxml.registry.bindings.rs.Status.FAILURE);
                resp.setRegistryErrorList(el);
            } catch (RegistryException e) {
                resp = util.createRegistryResponseFromThrowable(e,
                        "LifeCycleManagerImpl.removeObjects", "Unknown");
            }
        } catch (javax.xml.bind.JAXBException e) {
            log.error(e);
        }

        return resp;
    }

    /** Add slots to one or more registry entries. */    
    public RegistryResponse addSlots(UserType user, AddSlotsRequest req) {
        //TODO: Check V3 spec to make sure user is specified as parameter.
        RegistryResponse resp = null;

        try {
            try {
                AuthorizationResult authRes = 
                    AuthorizationServiceImpl.getInstance().checkAuthorization(user, req);
                authRes.throwExceptionOn(AuthorizationResult.PERMIT_NONE|AuthorizationResult.PERMIT_SOME);
            
                String objectRefId = req.getObjectRef().getId();
                List slots = req.getSlot();
                pm.addSlots(user, objectRefId, slots);

                resp = bu.rsFac.createRegistryResponse();
                resp.setStatus(org.oasis.ebxml.registry.bindings.rs.Status.SUCCESS);
            } catch (SlotsParentNotExistException e) {
                RegistryErrorList el = bu.rsFac.createRegistryErrorList();
                String parentId = e.getParentId();
                RegistryError re = bu.rsFac.createRegistryError();
                re.setValue("The parent '" + parentId +
                    "' of these slots does " + "not exist");
                re.setErrorCode("unknown");
                re.setCodeContext("LifeCycleManagerImpl.addSlots");
                el.getRegistryError().add(re);
                resp = bu.rsFac.createRegistryResponse();
                resp.setStatus(org.oasis.ebxml.registry.bindings.rs.Status.FAILURE);
                resp.setRegistryErrorList(el);
            } catch (SlotsExistException e) {
                RegistryErrorList el = bu.rsFac.createRegistryErrorList();
                Iterator iter = e.getSlotsNames().iterator();
                String parentId = e.getParentId();

                while (iter.hasNext()) {
                    RegistryError re = bu.rsFac.createRegistryError();
                    String slotName = (String) iter.next();
                    re.setValue("The Slot '" + slotName + "' for the" +
                        " RegistryObject" + " '" + parentId +
                        "' already exists");
                    re.setErrorCode("unknown");
                    re.setCodeContext("LifeCycleManagerImpl.addSlots");
                    el.getRegistryError().add(re);
                }

                resp = bu.rsFac.createRegistryResponse();
                resp.setStatus(org.oasis.ebxml.registry.bindings.rs.Status.FAILURE);
                resp.setRegistryErrorList(el);
            } catch (DuplicateSlotsException e) {
                RegistryErrorList el = bu.rsFac.createRegistryErrorList();
                Iterator iter = e.getSlotsNames().iterator();
                String parentId = e.getParentId();

                while (iter.hasNext()) {
                    RegistryError re = bu.rsFac.createRegistryError();
                    String slotName = (String) iter.next();
                    re.setValue("More than one Slot have the name '" +
                        slotName + "' for the RegistryObject " + "'" +
                        parentId + "'");
                    re.setErrorCode("unknown");
                    re.setCodeContext("LifeCycleManagerImpl.addSlots");
                    el.getRegistryError().add(re);
                }

                resp = bu.rsFac.createRegistryResponse();
                resp.setStatus(org.oasis.ebxml.registry.bindings.rs.Status.FAILURE);
                resp.setRegistryErrorList(el);
            } catch (RegistryException e) {
                resp = util.createRegistryResponseFromThrowable(e,
                        "LifeCycleManagerImpl.addSlots", "Unknown");
            }
        } catch (javax.xml.bind.JAXBException e) {
            log.error(e);
        }

        return resp;
    }

    /** Remove specified slots from one or more registry entries. */
    public RegistryResponse removeSlots(UserType user, RemoveSlotsRequest req) {
        RegistryResponse resp = null;

        try {
            try {
                AuthorizationResult authRes = 
                    AuthorizationServiceImpl.getInstance().checkAuthorization(user, req);
                authRes.throwExceptionOn(AuthorizationResult.PERMIT_NONE|AuthorizationResult.PERMIT_SOME);
                
                String objectRefId = req.getObjectRef().getId();
                List slotsList = req.getSlot();
                pm.removeSlots(user, objectRefId, slotsList);
                resp = bu.rsFac.createRegistryResponse();
                resp.setStatus(org.oasis.ebxml.registry.bindings.rs.Status.SUCCESS);
            } catch (SlotNotExistException e) {
                RegistryError re = bu.rsFac.createRegistryError();
                re.setValue(e.getMessage());
                re.setErrorCode("unknown");
                re.setCodeContext("LifeCycleManagerImpl.removeSlots");

                RegistryErrorList el = bu.rsFac.createRegistryErrorList();
                el.getRegistryError().add(re);

                resp = bu.rsFac.createRegistryResponse();
                resp.setStatus(org.oasis.ebxml.registry.bindings.rs.Status.FAILURE);
                resp.setRegistryErrorList(el);
            } catch (RegistryException e) {
                resp = util.createRegistryResponseFromThrowable(e,
                        "LifeCycleManagerImpl.removeSlots", "Unknown");
            }
        } catch (javax.xml.bind.JAXBException e) {
            log.error(e);
        }

        return resp;
    }

    public static void printUsage() {
        System.out.println(
            "usage: -submitObjects | -approveObjects | -updateObjects " +
            "| -deprecateObjects | -removeObjects | -addSlots | - removeSlots " +
            "[SubmitObjectsRequest | " + "ApproveObjectsRequest | " +
            "UpdateObjectsRequest | " + "DeprecateObjectsRequest | " +
            "RemoveObjectsRequest | " + "AddSlotsRequest] " +
            "-repItem [repository item] -repItemId [id] -contentId [contentId] " +
            "-userId [user id]");
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
    private void correctRepositoryItemId(List objs,
        HashMap idToRepositoryItemMap) {
        if (objs.size() == 1) {
            Object obj = objs.get(0);

            if (obj instanceof RegistryEntryType) {
                RegistryEntryType firstRe = (RegistryEntryType) objs.get(0);

                if ((firstRe != null) && firstRe instanceof ExtrinsicObject) {
                    String correctId = firstRe.getId();

                    if (idToRepositoryItemMap.size() == 1) {
                        Iterator attachIter = idToRepositoryItemMap.keySet()
                                                                   .iterator();
                        String attachIdKey = (String) attachIter.next();
                        RepositoryItem attachRi = (RepositoryItem) idToRepositoryItemMap.get(attachIdKey);
                        String attachId = attachRi.getId();

                        if ((correctId != null) && !correctId.equals(attachId)) {
                            System.err.println(
                                "[LifeCycleManager::correctRepositoryItemId()]" +
                                " RepositoryItem id [" + attachId +
                                "] does not match Registry Object id [" +
                                correctId + "]");
                            System.err.println(
                                "[LifeCycleManager::correctRepositoryItemId()] " +
                                " Updating RepositoryItem id to " + correctId);

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
