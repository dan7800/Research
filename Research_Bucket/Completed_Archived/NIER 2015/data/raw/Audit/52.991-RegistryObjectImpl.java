/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/client/xml/registry/infomodel/RegistryObjectImpl.java,v 1.9 2004/03/29 14:32:52 farrukh_najmi Exp $
 * ====================================================================
 */
/*
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/client/xml/registry/infomodel/RegistryObjectImpl.java,v 1.9 2004/03/29 14:32:52 farrukh_najmi Exp $
 *
 *
 */
package org.freebxml.omar.client.xml.registry.infomodel;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.JAXRException;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.Query;
import javax.xml.registry.RegistryException;
import javax.xml.registry.infomodel.Association;
import javax.xml.registry.infomodel.AuditableEvent;
import javax.xml.registry.infomodel.Classification;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.ExternalIdentifier;
import javax.xml.registry.infomodel.ExternalLink;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.Key;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.User;

import org.apache.commons.logging.Log;
import org.freebxml.omar.client.xml.registry.BusinessQueryManagerImpl;
import org.freebxml.omar.client.xml.registry.DeclarativeQueryManagerImpl;
import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.freebxml.omar.client.xml.registry.RegistryServiceImpl;
import org.freebxml.omar.common.BindingUtility;
import org.oasis.ebxml.registry.bindings.rim.ClassificationType;
import org.oasis.ebxml.registry.bindings.rim.ExternalIdentifierType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;


/**
 * Class Declaration for Class1
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public abstract class RegistryObjectImpl extends ExtensibleObjectImpl
    implements RegistryObject {
    private Key key = null;
    private InternationalString description = null;
    protected InternationalString name = null;

    /** The ObjectRef to the ObjectType Concept */
    protected RegistryObjectRef objectTypeRef = null;

    /** Composed objects */
    protected Collection classifications = new ArrayList();
    protected Collection externalIds = new ArrayList();

    /** Even though in JAXR Association-s are non-composed objects, their
    *         save behavior should be similar to composed objects. */
    protected Collection associations = null;

    //Following are collection of non-composed objects that are cached by this 
    //implementation for performance efficiency. They are initialized on first access.
    protected HashSet externalLinks = null;
    protected Collection packages = null;
    protected Collection auditTrail = null;

    //Replace with functions to save memory later??
    protected DeclarativeQueryManagerImpl dqm = null;
    protected BusinessQueryManagerImpl bqm = null;
    private Organization org = null;

    RegistryObjectImpl(LifeCycleManagerImpl lcm) throws JAXRException {
        super(lcm);

        dqm = (DeclarativeQueryManagerImpl) (lcm.getRegistryService()
                                                .getDeclarativeQueryManager());
        bqm = (BusinessQueryManagerImpl) (lcm.getRegistryService()
                                             .getBusinessQueryManager());

        //Assign default key
        key = lcm.createKey();

        String str = getClass().getName();
        
        objectTypeRef = lcm.getObjectTypeRefFromJAXRClassName(this.getClass().getName());
        
    }
    
    RegistryObjectImpl(LifeCycleManagerImpl lcm, RegistryObjectType ebObject)
        throws JAXRException {
        // Pass ebObject to superclass so slot-s can be initialized
        super(lcm, ebObject);

        dqm = (DeclarativeQueryManagerImpl) (lcm.getRegistryService()
                                                .getDeclarativeQueryManager());
        bqm = (BusinessQueryManagerImpl) (lcm.getRegistryService()
                                             .getBusinessQueryManager());
        key = new KeyImpl(lcm);
        key.setId(ebObject.getId());

        if (ebObject.getName() != null) {
            name = new InternationalStringImpl(lcm, ebObject.getName());
        }

        if (ebObject.getDescription() != null) {
            description = new InternationalStringImpl(lcm,
                    ebObject.getDescription());
        }

        List ebClasses = ebObject.getClassification();
        Iterator iter = ebClasses.iterator();

        while (iter.hasNext()) {
            ClassificationType ebClass = (ClassificationType) iter.next();
            internalAddClassification(new ClassificationImpl(lcm, ebClass, this));
        }

        List extIds = ebObject.getExternalIdentifier();
        iter = extIds.iterator();

        while (iter.hasNext()) {
            ExternalIdentifierType ebExtIdentifier = (ExternalIdentifierType) iter.next();
            internalAddExternalIdentifier(new ExternalIdentifierImpl(lcm,
                    ebExtIdentifier, this));
        }

        objectTypeRef = new RegistryObjectRef(lcm, ebObject.getObjectType());
    }

    /**
     * Implementation private
     */
    public void setModified(boolean modified) {
        super.setModified(modified);

        if (modified == true) {
            lcm.addModifiedObject(this);
        } else {
            lcm.removeModifiedObject(this);
        }
    }

    //??JAXR 2.0
    public RegistryObjectRef getObjectTypeRef() throws JAXRException {
        return objectTypeRef;
    }
    
    public Concept getObjectType() throws JAXRException {
        Concept objectType = null;

        if (objectTypeRef != null) {
            objectType = (Concept)objectTypeRef.getRegistryObject("ClassificationNode");
        }

        return objectType;
    }

    /**
     * Internal method to set the objectType
     */
    void setObjectTypeInternal(Concept objectType)
        throws JAXRException {
        objectTypeRef = new RegistryObjectRef(lcm, objectType);
        setModified(true);
    }

    public Key getKey() throws JAXRException {
        return key;
    }

    /**
     * Do we add this to the API??
     */
    public String getId() throws JAXRException {
        return key.getId();
    }

    public InternationalString getDescription() throws JAXRException {
        if (description == null) {
            description = lcm.createInternationalString("");
        }

        return description;
    }

    public void setDescription(InternationalString desc)
        throws JAXRException {
        description = desc;
        setModified(true);
    }

    public InternationalString getName() throws JAXRException {
        if (name == null) {
            name = lcm.createInternationalString("");
        }

        return name;
    }

    public void setName(InternationalString name) throws JAXRException {
        this.name = name;
        setModified(true);
    }

    public void setKey(Key key) throws JAXRException {
        this.key = key;
        setModified(true);
    }

    /** Internal method, does not set modified flag. */
    private void internalAddClassification(Classification c)
        throws JAXRException {
        getClassifications().add(c);
        c.setClassifiedObject(this);
    }

    public void addClassification(Classification c) throws JAXRException {
        internalAddClassification(c);
        setModified(true);
    }

    public void addClassifications(Collection classifications)
        throws JAXRException {
        Iterator iter = classifications.iterator();

        while (iter.hasNext()) {
            Classification cls = (Classification) iter.next();
            internalAddClassification(cls);
        }

        setModified(true);
    }

    public void removeClassification(Classification c)
        throws JAXRException {
        if (classifications != null) {
            getClassifications().remove(c);
            setModified(true);
        }
    }

    public void removeClassifications(Collection classifications)
        throws JAXRException {
        if (classifications != null) {
            getClassifications().removeAll(classifications);
            setModified(true);
        }
    }

    //??Add to JAXR 2.0. Apply same pattern to all Collection attributes in RIM.
    public void removeAllClassifications() throws JAXRException {
        if (classifications != null) {
            removeClassifications(classifications);
            setModified(true);
        }
    }

    public void setClassifications(Collection classifications)
        throws JAXRException {
        removeAllClassifications();

        addClassifications(classifications);
        setModified(true);
    }

    public Collection getClassifications() throws JAXRException {
        if (classifications == null) {
            classifications = new ArrayList();
        }

        return classifications;
    }

    /**
     * Gets all Concepts classifying this object that have specified path as prefix.
     * Used in RegistryObjectsTableModel.getValueAt via reflections API if so configured.
     */
    public Collection getClassificationConceptsByPath(String pathPrefix)
        throws JAXRException {
        Collection matchingClassificationConcepts = new ArrayList();
        Collection _classifications = getClassifications();
        Iterator iter = _classifications.iterator();

        while (iter.hasNext()) {
            Classification cl = (Classification) iter.next();
            Concept concept = cl.getConcept();
            String conceptPath = concept.getPath();

            if (conceptPath.startsWith(pathPrefix)) {
                matchingClassificationConcepts.add(concept);
            }
        }

        return matchingClassificationConcepts;
    }

    public Collection getAuditTrail() throws JAXRException {
        if (auditTrail == null) {
            if (!isNew()) {
		String queryStr = "SELECT id FROM AuditableEvent ae, AffectedObject ao WHERE ao.id = '" + getKey().getId() + "' AND ao.eventId = ae.id ";	
                Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);
                BulkResponse response = dqm.executeQuery(query);

                checkBulkResponseExceptions(response);
                auditTrail = response.getCollection();
            }

            if (auditTrail == null) {
                auditTrail = new ArrayList();
            }
        }

        return auditTrail;
    }

    /**
     * Do we add this to the API??
     *
     * @return owner, ie. creator or null if this is a new object
     */
    public User getOwner() throws JAXRException {
        if (!isNew()) {
            // Ask server who our creator is
            Collection events = getAuditTrail();

            for (Iterator it = events.iterator(); it.hasNext();) {
                AuditableEventImpl ev = (AuditableEventImpl) it.next();

                if (ev.getEventType() == AuditableEvent.EVENT_TYPE_CREATED) {
                    return ev.getUser();
                }
            }
        }

        return null;
    }

    public void addAssociation(Association ass) throws JAXRException {
        getAssociations();

        if (!(associations.contains(ass))) {
            associations.add(ass);
        }

        ((AssociationImpl) ass).setSourceObjectInternal(this);
    }

    public void addAssociations(Collection asses) throws JAXRException {
        for (Iterator it = asses.iterator(); it.hasNext();) {
            Association ass = (Association) it.next();
            addAssociation(ass);
        }
    }

    public void removeAssociation(Association ass) throws JAXRException {
        getAssociations();

        if (associations.contains(ass)) {
            associations.remove(ass);

            //Need to mark as deleted and only remove from server on Save in future.
            //For now leaving as is in order to minimize change.???
            // Remove from server only if Association exists there
            if (!((AssociationImpl) ass).isNew()) {
                // assert(Association must exist on server)
                ArrayList keys = new ArrayList();
                keys.add(ass.getKey());

                BulkResponse response = lcm.deleteObjects(keys);
                JAXRException ex = getBulkResponseException(response);

                if (ex != null) {
                    throw ex;
                }
            }

            //No need to call setModified(true) since RIM modified object is an Assoociation				
            //setModified(true);
        }
    }

    public void removeAssociations(Collection asses) throws JAXRException {
        Collection savedAsses = getAssociations();

        if (associations.removeAll(asses)) {
            // Remove from server only if Association exists there
            ArrayList keys = new ArrayList();

            for (Iterator it = asses.iterator(); it.hasNext();) {
                AssociationImpl ass = (AssociationImpl) it.next();

                if (!ass.isNew()) {
                    // assert(Association must exist on server)
                    keys.add(ass.getKey());
                }
            }

            BulkResponse response = lcm.deleteObjects(keys);
            JAXRException ex = getBulkResponseException(response);

            if (ex != null) {
                // Undo remove
                // ??eeg Assumes all-or-nothing delete
                associations = savedAsses;
                throw ex;
            }
        }
    }

    public void setAssociations(Collection asses) throws JAXRException {
        // We make a copy of this.associations to avoid a 
        // concurrent modification exception 
        removeAssociations((Collection) new ArrayList(getAssociations()));
        addAssociations(asses);
    }

    public Collection getAssociations() throws JAXRException {
        if (associations == null) {
            associations = new HashSet();

            //If existing object then now is the time to do lazy fetch from server
            if (!isNew()) {
                // Return Collection from server
                String id = getKey().getId();
                String queryStr =
                    "SELECT id FROM Association WHERE sourceObject = '" + id +
                    "'";
                Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);
                BulkResponse response = dqm.executeQuery(query);
                checkBulkResponseExceptions(response);
                addAssociations(response.getCollection());
            }
        }

        return associations;
    }

    public Collection getAssociatedObjects() throws JAXRException {
        if (isNew()) {
            // ??eeg Still can have client side associated objects!
            // Return an empty Collection instead of null
            return new ArrayList();
        }

        String id = getKey().getId();
        String queryStr = "SELECT id FROM Association WHERE sourceObject = '" +
            id + "' OR targetObject = '" + id + "'";
        Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);
        BulkResponse response = dqm.executeQuery(query);
        checkBulkResponseExceptions(response);

        return response.getCollection();
    }

    /** Internal method, does not set modified flag. */
    private void internalAddExternalIdentifier(ExternalIdentifier ei)
        throws JAXRException {
        getExternalIdentifiers().add(ei);
    }

    public void addExternalIdentifier(ExternalIdentifier ei)
        throws JAXRException {
        internalAddExternalIdentifier(ei);
        setModified(true);
    }

    public void addExternalIdentifiers(Collection extLink)
        throws JAXRException {
        getExternalIdentifiers().add(extLink);
        setModified(true);
    }

    public void removeExternalIdentifier(ExternalIdentifier extLink)
        throws JAXRException {
        if (externalIds != null) {
            externalIds.remove(extLink);
            setModified(true);
        }
    }

    public void removeExternalIdentifiers(Collection extLink)
        throws JAXRException {
        if (externalIds != null) {
            externalIds.removeAll(extLink);
            setModified(true);
        }
    }

    public void setExternalIdentifiers(Collection extLink)
        throws JAXRException {
        externalIds = extLink;
        setModified(true);
    }

    public Collection getExternalIdentifiers() throws JAXRException {
        if (externalIds == null) {
            externalIds = new ArrayList();
        }

        return externalIds;
    }

    public void addExternalLink(ExternalLink extLink) throws JAXRException {
        getExternalLinks();

        // If the external link is not in this object's in-memory-cache of
        // external links, add it.
        if (!(externalLinks.contains(extLink))) {
            // Check that an ExternallyLinks association exists between this
            // object and its external link.
            boolean associationExists = false;
            BusinessQueryManagerImpl bqm = (BusinessQueryManagerImpl) (lcm.getRegistryService()
                                                                          .getBusinessQueryManager());
            Concept assocType = bqm.findConceptByPath(
                    "/urn:uuid:6902675f-2f18-44b8-888b-c91db8b96b4d/" +
                    AssociationImpl.ASSOCIATION_TYPE_EXTERNALLY_LINKS);
            Collection linkAssociations = extLink.getAssociations();

            if (linkAssociations != null) {
                Iterator assIter = linkAssociations.iterator();

                while (assIter.hasNext()) {
                    Association ass = (Association) assIter.next();

                    if (ass.getSourceObject().equals(extLink) &&
                            ass.getTargetObject().equals(this) &&
                            ass.getAssociationType().equals(assocType)) {
                        associationExists = true;

                        break;
                    }
                }
            }

            // Create the association between the external link and this object,
            // if necessary.
            if (!associationExists) {
                Association ass = lcm.createAssociation(this, assocType);
                extLink.addAssociation(ass);
            }

            externalLinks.add(extLink);

            // Note: There is no need to call setModified(true) since 
            // the RIM modified object is an Association				
        }
    }

    public void addExternalLinks(Collection extLinks) throws JAXRException {
        Iterator iter = extLinks.iterator();

        while (iter.hasNext()) {
            ExternalLink extLink = (ExternalLink) iter.next();
            addExternalLink(extLink);
        }

        //No need to call setModified(true) since RIM modified object is an Assoociation
    }

    public void removeExternalLink(ExternalLink extLink)
        throws JAXRException {
        getExternalLinks();

        if (externalLinks.contains(extLink)) {
            externalLinks.remove(extLink);

            //Now remove the ExternallyLinks association that has extLink as src and this object as target
            // We make a copy of this.externalLinks to avoid a 
            // concurrent modification exception in the removeExternalLinks
            Collection linkAssociations = new ArrayList(extLink.getAssociations());

            if (linkAssociations != null) {
                Iterator iter = linkAssociations.iterator();

                while (iter.hasNext()) {
                    Association ass = (Association) iter.next();

                    if (ass.getTargetObject() == this) {
                        if (ass.getAssociationType().getValue()
                                   .equalsIgnoreCase(AssociationImpl.ASSOCIATION_TYPE_EXTERNALLY_LINKS)) {
                            extLink.removeAssociation(ass);
                        }
                    }
                }
            }

            //No need to call setModified(true) since RIM modified object is an Assoociation				
            //setModified(true);
        }
    }

    public void removeExternalLinks(Collection extLinks)
        throws JAXRException {
        getExternalLinks();

        Iterator iter = extLinks.iterator();

        while (iter.hasNext()) {
            ExternalLink extLink = (ExternalLink) iter.next();
            removeExternalLink(extLink);
        }

        //No need to call setModified(true) since RIM modified object is an Assoociation    }
    }

    /** Set this object's list of external links to the list specified. If the
      * current list of external links contains links that are not in the specified
      * list, they will be removed and the association between them and this object
      * will be removed from the server. For any external links that are in the
      * list specified, an association will be created (in-memory, not on the
      * server) and they will be added to this object's list of external links.
      *
      * @param newExternalLinks
      *     A Collection of ExternalLink objects.
      * @throws JAXRException
      */
    public void setExternalLinks(Collection newExternalLinks)
        throws JAXRException {
        Collection currentExternalLinks = getExternalLinks();

        // Add any external links that are not currently in this object's list.
        Iterator newExtLinksIter = newExternalLinks.iterator();

        while (newExtLinksIter.hasNext()) {
            ExternalLink externalLink = (ExternalLink) newExtLinksIter.next();

            if (!currentExternalLinks.contains(externalLink)) {
                addExternalLink(externalLink);
            }
        }

        // Remove any external links that are currently in this object's list,
        // but are not in the new list.
        Iterator currentExtLinksIter = currentExternalLinks.iterator();

        while (currentExtLinksIter.hasNext()) {
            ExternalLink externalLink = (ExternalLink) currentExtLinksIter.next();

            if (!newExternalLinks.contains(externalLink)) {
                removeExternalLink(externalLink);
            }
        }
    }

    public Collection getExternalLinks() throws JAXRException {
        if (externalLinks == null) {
            externalLinks = new HashSet();

            //If existing object then now is the time to do lazy fetch from server
            if (!isNew()) {
                String id = getId();
                String queryStr =
                    "SELECT id FROM ExternalLink el, Association ass WHERE ass.targetObject = '" +
                    id + "' AND ass.associationType = '" +
                    AssociationImpl.CANONICAL_ID_NODE_ASSOCIATION_TYPE_EXTERNALLY_LINKS +
                    "' AND ass.sourceObject = el.id ";
                Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);

                BulkResponse response = dqm.executeQuery(query);
                checkBulkResponseExceptions(response);
                addExternalLinks(response.getCollection());
            }
        }

        return externalLinks;
    }

    public Organization getSubmittingOrganization() throws JAXRException {
        return org;
    }

    public Collection getRegistryPackages() throws JAXRException {
        if (packages == null) {
            if (!isNew()) {
                String queryStr =
                    "SELECT id FROM RegistryPackage WHERE id IN (RegistryObject_registryPackages('" +
                    getKey().getId() + "'))";
                Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);
                BulkResponse response = dqm.executeQuery(query);
                checkBulkResponseExceptions(response);
                packages = response.getCollection();
            }

            if (packages == null) {
                packages = new ArrayList();
            }
        }

        return packages;
    }

    public String toXML() throws JAXRException {
        try {
            StringWriter sw = new StringWriter();
            Marshaller marshaller = BindingUtility.getInstance().getJAXBContext()
                                                  .createMarshaller();
            marshaller.marshal(toBindingObject(), sw);

            return sw.toString();
        } catch (javax.xml.bind.JAXBException e) {
            throw new JAXRException(e);
        }
    }

    /**
     * This method takes this JAXR infomodel object and returns an
     * equivalent binding object for it.  Note it does the reverse of one
     * of the constructors above.
     */
    abstract public Object toBindingObject() throws JAXRException;

    protected void setBindingObject(
        org.oasis.ebxml.registry.bindings.rim.RegistryObjectType ebObject)
        throws JAXRException {
        // Pass ebObject to superclass so slot-s can be initialized
        super.setBindingObject(ebObject);

        ebObject.setId(key.getId());
        //ebObject.setObjectType(objectTypeStr);

        try {
            org.oasis.ebxml.registry.bindings.rim.ObjectFactory factory = BindingUtility.getInstance().rimFac;
            org.oasis.ebxml.registry.bindings.rim.Name ebName = factory.createName();
            ((InternationalStringImpl) getName()).setBindingObject(ebName);
            ebObject.setName(ebName);

            org.oasis.ebxml.registry.bindings.rim.Description ebDesc = factory.createDescription();
            ((InternationalStringImpl) getDescription()).setBindingObject(ebDesc);
            ebObject.setDescription(ebDesc);

            Iterator iter = getClassifications().iterator();

            while (iter.hasNext()) {
                ClassificationImpl cls = (ClassificationImpl) iter.next();
                org.oasis.ebxml.registry.bindings.rim.Classification ebCls = (org.oasis.ebxml.registry.bindings.rim.Classification) cls.toBindingObject();
                ebObject.getClassification().add(ebCls);
            }

            iter = getExternalIdentifiers().iterator();

            while (iter.hasNext()) {
                ExternalIdentifierImpl extId = (ExternalIdentifierImpl) iter.next();
                org.oasis.ebxml.registry.bindings.rim.ExternalIdentifier ebExtId =
                    (org.oasis.ebxml.registry.bindings.rim.ExternalIdentifier) extId.toBindingObject();
                ebObject.getClassification().add(ebExtId);
            }
        } catch (JAXBException ex) {
            throw new JAXRException(ex.getMessage());
        }
    }

    public void getComposedObjects(HashSet composedObjects)
        throws JAXRException {
        Log log = getLog();
        super.getComposedObjects(composedObjects);

        //log.debug("getComposedObject slots" + this + " composedObjects = " + composedObjects);
        Collection classifications = getClassifications();
        composedObjects.addAll(classifications);

        Iterator iter = classifications.iterator();

        while (iter.hasNext()) {
            ClassificationImpl cls = (ClassificationImpl) iter.next();
            cls.getComposedObjects(composedObjects);
        }

        //log.debug("getComposedObject classifications" + this + " composedObjects = " + composedObjects);
        Collection extIds = getExternalIdentifiers();
        composedObjects.addAll(extIds);
        iter = extIds.iterator();

        while (iter.hasNext()) {
            ExternalIdentifierImpl extId = (ExternalIdentifierImpl) iter.next();
            extId.getComposedObjects(composedObjects);
        }

        //log.debug("getComposedObject externalIdentifiers" + this + " composedObjects = " + composedObjects);
    }

    public LifeCycleManager getLifeCycleManager() throws JAXRException {
        return lcm;
    }

    /**
     * @return First exception in BulkResponse if there is one else null
     */
    RegistryException getBulkResponseException(BulkResponse response)
        throws JAXRException {
        Collection exceptions = response.getExceptions();

        if (exceptions != null) {
            return (RegistryException) exceptions.iterator().next();
        }

        return null;
    }

    /**
     * Throw first exception in BulkResponse if there is one else return
     */
    void checkBulkResponseExceptions(BulkResponse response)
        throws JAXRException {
        RegistryException ex = getBulkResponseException(response);

        if (ex != null) {
            throw ex;
        }

        return;
    }

    /**
     * Gest all Associations and their targets for which this object is a source.
     * Used by LifeCycleManagerImpl.saveObjects
     *
     */
    public HashSet getAssociationsAndAssociatedObjects()
        throws JAXRException {
        HashSet assObjects = new HashSet();

        // Automatically save any Association-s with an object in the save
        // list along with the target object of the Association per JAXR 1.0 spec.
        Collection asses = getAssociations();

        // Add the Association targets
        for (Iterator j = asses.iterator(); j.hasNext();) {
            AssociationImpl ass = (AssociationImpl) j.next();
            RegistryObject target = ass.getTargetObject();
            assObjects.add(target);
        }

        // Add also the Association-s themselves
        assObjects.addAll(asses);

        return assObjects;
    }

    /**
     * Gets all referenced objects for which this object is a referant.
     * Extended by base classes.
     * Used by LifeCycleManagerImpl.saveObjects
     *
     */
    public HashSet getRegistryObjectRefs() {
        HashSet refs = new HashSet();

        return refs;
    }

    public String toString() {
        String str = super.toString();

        try {
            str = getId() + "," + str;
        } catch (JAXRException e) {
            try {
                getLog().warn("Error getting id", e);
            } catch (JAXRException e1) {
            }
        }

        return str;
    }

    /** Returns true if the object specified is a RegistryObjectImpl
      * with the same id.
      *
      * @param o
      *                The object to compare to.
      * @return
      *                <code>true</code> if the objects are equal.
      * @todo
      *                Do we need to ensure the object is the same type as this
      *                instance? For example, this instance could be a ServiceImpl
      *                and the object could be an ExternalLinkImpl. Could these have
      *                the same id?
      */
    public boolean equals(Object o) {
        if (compareTo(o) == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Compares two registries objects.
     * Consider adding Coparable to RegistryObject in JAXR 2.0??
     *
     * @return 0 (equal) is the id of the objects matches this objects id.
     * Otherwise return -1 (this object is less than arg o).
     */
    public int compareTo(Object o) {
        int result = -1;

        if (o instanceof RegistryObject) {
            try {
                String myId = getId();
                String otherId = ((RegistryObject) o).getKey().getId();
                result = myId.compareTo(otherId);
            } catch (JAXRException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    protected Log getLog() throws JAXRException {
        Log log = ((RegistryServiceImpl) (lcm.getRegistryService())).getConnection()
                   .getConnectionFactory().getLog();

        return log;
    }
}
/*
 * $Header: /cvsroot/sino/jaxr/src/com/sun/xml/registry/ebxml/infomodel/RegistryObjectImpl.java,v 1.60 2003/09/13 19:43:54 farrukh_najmi Exp $
 *
 *
 */

package com.sun.xml.registry.ebxml.infomodel;

import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;
import java.util.*;

import com.sun.xml.registry.ebxml.*;
import com.sun.xml.registry.ebxml.util.*;

import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationType;
import org.oasis.ebxml.registry.bindings.rim.ExternalIdentifierType;

import org.apache.commons.logging.Log;


/**
 * Class Declaration for Class1
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public abstract class RegistryObjectImpl extends ExtensibleObjectImpl
    implements RegistryObject
{
    private Key key = null;
    private InternationalString description = null;
    protected InternationalString name = null;

    /** The ObjectType Concept value represented as a String */
    protected String objectTypeStr;
    protected Concept objectType;
	
    /** Composed objects */
    protected Collection classifications = new ArrayList();
    protected Collection externalIds = new ArrayList();
    
    /** Even though in JAXR Association-s are non-composed objects, their
        save behavior should be similar to composed objects. */
    protected Collection associations = null;

    //Following are collection of non-composed objects that are cached by this 
    //implementation for performance efficiency. They are initialized on first access.
    protected HashSet externalLinks = null;
    protected Collection packages = null;
    protected Collection auditTrail = null;

    //Replace with functions to save memory later??
    protected DeclarativeQueryManagerImpl dqm = null;
    protected BusinessQueryManagerImpl bqm = null;

    private Organization org = null;
		
    RegistryObjectImpl(LifeCycleManagerImpl lcm)
        throws JAXRException
    {
        super(lcm);
        
        dqm = (DeclarativeQueryManagerImpl)(lcm.getRegistryService().getDeclarativeQueryManager());
        bqm = (BusinessQueryManagerImpl)(lcm.getRegistryService().getBusinessQueryManager());
		
        //Assign default key
        key = lcm.createKey();

        String str = getClass().getName();
        objectTypeStr = str.substring(str.lastIndexOf('.')+1,
                                             str.length()-4);
    }
	
    RegistryObjectImpl(LifeCycleManagerImpl lcm,
                       RegistryObjectType ebObject)
        throws JAXRException
    {
        // Pass ebObject to superclass so slot-s can be initialized
        super(lcm, ebObject);

        dqm = (DeclarativeQueryManagerImpl)(lcm.getRegistryService().getDeclarativeQueryManager());
        bqm = (BusinessQueryManagerImpl)(lcm.getRegistryService().getBusinessQueryManager());
        key = new KeyImpl(lcm);
        key.setId(ebObject.getId());

        if (ebObject.getName() != null) {
            name = new InternationalStringImpl(lcm, ebObject.getName());
        }

        if (ebObject.getDescription() != null) {
            description = new InternationalStringImpl(
                lcm, ebObject.getDescription());		
        }

        ClassificationType[] ebClasses = ebObject.getClassification();
        for (int i = 0; i < ebClasses.length; i++) {
            internalAddClassification(new ClassificationImpl(lcm, ebClasses[i], this));
        }

        ExternalIdentifierType[] extIds = ebObject.getExternalIdentifier();
        for (int i = 0; i < extIds.length; i++) {
            // XXX Finish this by passing in extIds[i]
            internalAddExternalIdentifier(new ExternalIdentifierImpl(lcm, extIds[i], this));
        }

        String ebObjectType = ebObject.getObjectType();
        objectTypeStr = (ebObjectType == null) ? "Unknown" : ebObjectType;

        // XXX Finish these attrbutes:
        ebObject.getAccessControlPolicy();
    }
    
    /**
     * Implementation private
     */
    public void setModified(boolean modified) {
        super.setModified(modified);
        if (modified == true) {
            lcm.addModifiedObject(this);
        }
        else {
            lcm.removeModifiedObject(this);
        }
    }
    
	
    public Concept getObjectType() throws JAXRException {
        
        if (objectType == null) {
            if (objectTypeStr.startsWith("urn:uuid:")) {
                RegistryObject ro = dqm.getRegistryObject(objectTypeStr, LifeCycleManager.CONCEPT);
                if (ro instanceof Concept) {
                    objectType = (Concept)ro;
                }
            }

            if (objectType == null) {
                String jaxrInterfaceName = getClass().getName();
                jaxrInterfaceName = jaxrInterfaceName.substring(jaxrInterfaceName.lastIndexOf(".")+1);
                if (jaxrInterfaceName.endsWith("Impl")) {
                    //Remove Impl suffix for JAXR provider Impl classes
                    jaxrInterfaceName = jaxrInterfaceName.substring(0, jaxrInterfaceName.length() -4);
                }
                
                objectType = (Concept)(lcm.getObjectTypesMap().get(jaxrInterfaceName));
            }
        }
        return objectType;
    }
    
    public abstract String toXML() throws JAXRException;

    public Key getKey() throws JAXRException {
        return key;
    }

    /**
     * Do we add this to the API??
     */
    public String getId() throws JAXRException {
        return key.getId();
    }

    public InternationalString getDescription() throws JAXRException {
        if (description == null) {
            description = lcm.createInternationalString("");
        }
        return description;
    }

    public void setDescription(InternationalString desc) throws JAXRException {        
        description = desc;        
        setModified(true);
    }

    public InternationalString getName() throws JAXRException {
        if (name == null) {
            name = lcm.createInternationalString("");
        }
        return name;
    }

    public void setName(InternationalString name) throws JAXRException {
        this.name = name;
        setModified(true);
    }

    public void setKey(Key key) throws JAXRException {
        this.key = key;
        setModified(true);
    }

    /** Internal method, does not set modified flag. */
    private void internalAddClassification(Classification c) throws JAXRException {
        getClassifications().add(c);
        c.setClassifiedObject(this);
    }

    public void addClassification(Classification c) throws JAXRException {
        internalAddClassification(c);
        setModified(true);
    }

    public void addClassifications(Collection classifications) throws JAXRException {
        Iterator iter = classifications.iterator();
        while (iter.hasNext()) {
            Classification cls = (Classification)iter.next();
            internalAddClassification(cls);
        }
        setModified(true);
    }

    public void removeClassification(Classification c) throws JAXRException {
        if (classifications != null) {
            getClassifications().remove(c);
            setModified(true);
        }
    }

    public void removeClassifications(Collection classifications) throws JAXRException {
        if (classifications != null) {
            getClassifications().removeAll(classifications);
            setModified(true);
        }
    }

    //??Add to JAXR 2.0. Apply same pattern to all Collection attributes in RIM.
    public void removeAllClassifications() throws JAXRException {
        if (classifications != null) {
            removeClassifications(classifications);
            setModified(true);
        }
    }

    public void setClassifications(Collection classifications) throws JAXRException {
        removeAllClassifications();
        
        addClassifications(classifications);
        setModified(true);
    }

    public Collection getClassifications() throws JAXRException {
        if (classifications == null) {
            classifications = new ArrayList();	
        }
        return classifications;
    }

    /**
     * Gets all Concepts classifying this object that have specified path as prefix.
     * Used in RegistryObjectsTableModel.getValueAt via reflections API if so configured.
     */
    public Collection getClassificationConceptsByPath(String pathPrefix) throws JAXRException {
        Collection matchingClassificationConcepts = new ArrayList();
        Collection _classifications = getClassifications();
        Iterator iter = _classifications.iterator();
        while (iter.hasNext()) {
            Classification cl = (Classification)iter.next();
            Concept concept = cl.getConcept();
            String conceptPath = concept.getPath();
            if (conceptPath.startsWith(pathPrefix)) {
                matchingClassificationConcepts.add(concept);
            }
        }
        return matchingClassificationConcepts;
    }
    
    public Collection getAuditTrail() throws JAXRException {
        if (auditTrail == null) {
            if (!isNew()) {
                String queryStr = "SELECT id FROM AuditableEvent WHERE registryObject = '" + getKey().getId() + "'";  
                Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);
                BulkResponse response = dqm.executeQuery(query);	

                checkBulkResponseExceptions(response);
                auditTrail = response.getCollection();
            }

            if (auditTrail == null) {
                    auditTrail = new ArrayList();
            }
        }
        
        return auditTrail;
    }

    /**
     * Do we add this to the API??
     *
     * @return owner, ie. creator or null if this is a new object
     */
    public User getOwner() throws JAXRException {
        if (!isNew()) {
            // Ask server who our creator is
            Collection events = getAuditTrail();
            for (Iterator it = events.iterator(); it.hasNext(); ) {
                AuditableEventImpl ev = (AuditableEventImpl)it.next();
                if (ev.getEventType() == AuditableEvent.EVENT_TYPE_CREATED) {
                    return ev.getUser();
                }
            }
        }
        return null;
    }

    public void addAssociation(Association ass) throws JAXRException {
        getAssociations();        
        if (!(associations.contains(ass))) {            
            associations.add(ass);
        }        
        ((AssociationImpl)ass).setSourceObjectInternal(this);
    }

    public void addAssociations(Collection asses) throws JAXRException {
        for (Iterator it = asses.iterator(); it.hasNext(); ) {
            Association ass = (Association)it.next();
            addAssociation(ass);
        }
    }

    public void removeAssociation(Association ass) throws JAXRException {
        getAssociations();
        if (associations.contains(ass)) {
            associations.remove(ass);
            
            //Need to mark as deleted and only remove from server on Save in future.
            //For now leaving as is in order to minimize change.???
            // Remove from server only if Association exists there
            if (!((AssociationImpl)ass).isNew()) {
                // assert(Association must exist on server)
                ArrayList keys = new ArrayList();
                keys.add(ass.getKey());
                BulkResponse response = lcm.deleteObjects(keys);
                JAXRException ex = getBulkResponseException(response);
                if (ex != null) {
                    throw ex;
                }
            }
            //No need to call setModified(true) since RIM modified object is an Assoociation				
            //setModified(true);
        }
    }

    public void removeAssociations(Collection asses) throws JAXRException {
        Collection savedAsses = getAssociations();
        if (associations.removeAll(asses)) {
            // Remove from server only if Association exists there
            ArrayList keys = new ArrayList();
            for (Iterator it = asses.iterator(); it.hasNext(); ) {
                AssociationImpl ass = (AssociationImpl)it.next();
                if (!ass.isNew()) {
                    // assert(Association must exist on server)
                    keys.add(ass.getKey());
                }
            }
            BulkResponse response = lcm.deleteObjects(keys);
            JAXRException ex = getBulkResponseException(response);
            if (ex != null) {
                // Undo remove
                // ??eeg Assumes all-or-nothing delete
                associations = savedAsses;
                throw ex;
            }
        }
    }

    public void setAssociations(Collection asses) throws JAXRException {
        // We make a copy of this.associations to avoid a 
        // concurrent modification exception 
        removeAssociations((Collection)new ArrayList(getAssociations()));
        addAssociations(asses);
    }

    public Collection getAssociations() throws JAXRException {
        if (associations == null) {
            associations = new HashSet();
            
            //If existing object then now is the time to do lazy fetch from server
            if (!isNew()) {               
                // Return Collection from server
                String id = getKey().getId();
                String queryStr = "SELECT id FROM Association WHERE sourceObject = '" + id + "'";
                Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);
                BulkResponse response = dqm.executeQuery(query);
                checkBulkResponseExceptions(response);
                addAssociations(response.getCollection());                
            }
        }
        return associations;
    }

    public Collection getAssociatedObjects() throws JAXRException {
        if (isNew()) {
            // ??eeg Still can have client side associated objects!
            // Return an empty Collection instead of null
            return new ArrayList();
        }

        String id = getKey().getId();
        String queryStr =
            "SELECT id FROM Association WHERE sourceObject = '" + id +
            "' OR targetObject = '" + id + "'";
        Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);
        BulkResponse response = dqm.executeQuery(query);
        checkBulkResponseExceptions(response);
        return response.getCollection();
    }

    /** Internal method, does not set modified flag. */
    private void internalAddExternalIdentifier(ExternalIdentifier ei) throws JAXRException {
        getExternalIdentifiers().add(ei);
    }

    public void addExternalIdentifier(ExternalIdentifier ei) throws JAXRException {
        internalAddExternalIdentifier(ei);
        setModified(true);
    }

    public void addExternalIdentifiers(Collection extLink) throws JAXRException {
        getExternalIdentifiers().add(extLink);
        setModified(true);
    }

    public void removeExternalIdentifier(ExternalIdentifier extLink) throws JAXRException {
        if (externalIds != null) {
            externalIds.remove(extLink);
            setModified(true);	
        }
    }

    public void removeExternalIdentifiers(Collection extLink) throws JAXRException {
        if (externalIds != null) {
            externalIds.removeAll(extLink);
            setModified(true);	
        }        
    }

    public void setExternalIdentifiers(Collection extLink) throws JAXRException {
        externalIds = extLink;
        setModified(true);
    }

    public Collection getExternalIdentifiers() throws JAXRException {
        if (externalIds == null) {
            externalIds = new ArrayList();
        }
        return externalIds;
    }

    public void addExternalLink(ExternalLink extLink) throws JAXRException {

        getExternalLinks();        

        // If the external link is not in this object's in-memory-cache of
        // external links, add it.
        if (!(externalLinks.contains(extLink))) {

            // Check that an ExternallyLinks association exists between this
            // object and its external link.
            boolean associationExists = false;
            BusinessQueryManagerImpl bqm = 
                (BusinessQueryManagerImpl)(lcm.getRegistryService().getBusinessQueryManager());
            Concept assocType = 
                bqm.findConceptByPath("/urn:uuid:6902675f-2f18-44b8-888b-c91db8b96b4d/" + 
                                      AssociationImpl.ASSOCIATION_TYPE_EXTERNALLY_LINKS);
            Collection linkAssociations = extLink.getAssociations();
            if (linkAssociations != null) {
                Iterator assIter = linkAssociations.iterator();
                while (assIter.hasNext()) {
                    Association ass = (Association)assIter.next();
                    if (ass.getSourceObject().equals(extLink) &&
                        ass.getTargetObject().equals(this) &&
                        ass.getAssociationType().equals(assocType)) 
                    {
                        associationExists = true;
                        break;
                    }
                }
            }

            // Create the association between the external link and this object,
            // if necessary.
            if (!associationExists) {
                Association ass = lcm.createAssociation(this, assocType);
                extLink.addAssociation(ass); 
            }

            externalLinks.add(extLink);            

            // Note: There is no need to call setModified(true) since 
            // the RIM modified object is an Association				
        }
    }

    public void addExternalLinks(Collection extLinks) throws JAXRException {
        Iterator iter = extLinks.iterator();
        while (iter.hasNext()) {
            ExternalLink extLink = (ExternalLink)iter.next();
            addExternalLink(extLink);
        }
        //No need to call setModified(true) since RIM modified object is an Assoociation
    }

    public void removeExternalLink(ExternalLink extLink) throws JAXRException {
        getExternalLinks();
        if (externalLinks.contains(extLink)) {
            externalLinks.remove(extLink);
            
            //Now remove the ExternallyLinks association that has extLink as src and this object as target
            // We make a copy of this.externalLinks to avoid a 
            // concurrent modification exception in the removeExternalLinks
            Collection linkAssociations = new ArrayList(extLink.getAssociations());
            if (linkAssociations != null) {
                Iterator iter = linkAssociations.iterator();
                while (iter.hasNext()) {
                    Association ass = (Association)iter.next();
                    if (ass.getTargetObject() == this) {
                        if (ass.getAssociationType().getValue().equalsIgnoreCase(AssociationImpl.ASSOCIATION_TYPE_EXTERNALLY_LINKS)) {
                            extLink.removeAssociation(ass);
                        }
                    }
                }
            }
            //No need to call setModified(true) since RIM modified object is an Assoociation				
            //setModified(true);
        }
    }
    
    public void removeExternalLinks(Collection extLinks) throws JAXRException {
        getExternalLinks();
        Iterator iter = extLinks.iterator();
        while (iter.hasNext()) {
        	ExternalLink extLink = (ExternalLink)iter.next();
        	removeExternalLink(extLink);
        }
        //No need to call setModified(true) since RIM modified object is an Assoociation    }
    }
    
    /** Set this object's list of external links to the list specified. If the
      * current list of external links contains links that are not in the specified
      * list, they will be removed and the association between them and this object
      * will be removed from the server. For any external links that are in the 
      * list specified, an association will be created (in-memory, not on the
      * server) and they will be added to this object's list of external links.
      *
      * @param newExternalLinks
      *     A Collection of ExternalLink objects.
      * @throws JAXRException
      */    
    public void setExternalLinks(Collection newExternalLinks) throws JAXRException { 
        
        Collection currentExternalLinks = getExternalLinks();
        
        // Add any external links that are not currently in this object's list.
        Iterator newExtLinksIter = newExternalLinks.iterator();
        while (newExtLinksIter.hasNext()) {
            ExternalLink externalLink = (ExternalLink)newExtLinksIter.next();
            if (!currentExternalLinks.contains(externalLink)) {
                addExternalLink(externalLink);
            }
        }
        
        // Remove any external links that are currently in this object's list,
        // but are not in the new list.
        Iterator currentExtLinksIter = currentExternalLinks.iterator();
        while (currentExtLinksIter.hasNext()) {
            ExternalLink externalLink = (ExternalLink)currentExtLinksIter.next();
            if (!newExternalLinks.contains(externalLink)) {
                removeExternalLink(externalLink);
            }
        }
    }

    public Collection getExternalLinks() throws JAXRException {

        if (externalLinks == null) {
            externalLinks = new HashSet();
            
            //If existing object then now is the time to do lazy fetch from server
            if (!isNew()) {               
                String id = getId();
                String queryStr =
                    "SELECT id FROM ExternalLink el, Association ass WHERE ass.targetObject = '" + id +
                    "' AND ass.associationType = '" + AssociationImpl.CANONICAL_ID_NODE_ASSOCIATION_TYPE_EXTERNALLY_LINKS + 
                    "' AND ass.sourceObject = el.id ";
                Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);
                
                BulkResponse response = dqm.executeQuery(query);	
                checkBulkResponseExceptions(response);
                addExternalLinks(response.getCollection());                
            }
        }
        
        return externalLinks;
    }


    public Organization getSubmittingOrganization() throws JAXRException {
        return org;
    }

    public Collection getRegistryPackages() throws JAXRException {
        if (packages == null) {
            if (!isNew()) {
                String queryStr = "SELECT id FROM RegistryPackage WHERE id IN (RegistryObject_registryPackages('" + getKey().getId() + "'))";  
                Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);
                BulkResponse response = dqm.executeQuery(query);	
                checkBulkResponseExceptions(response);
                packages = response.getCollection();
            }

            if (packages == null) {
                    packages = new ArrayList();
            }
        }
    	return packages;
    }
    
    protected void setBindingObject(org.oasis.ebxml.registry.bindings.rim.RegistryObjectType ebObject) throws JAXRException {
        // Pass ebObject to superclass so slot-s can be initialized
        super.setBindingObject(ebObject);

        ebObject.setId(key.getId());
        ebObject.setObjectType(objectTypeStr);
        
        org.oasis.ebxml.registry.bindings.rim.Name ebName = 
            new org.oasis.ebxml.registry.bindings.rim.Name();
        ((InternationalStringImpl)getName()).setBindingObject(ebName);
        ebObject.setName(ebName);
        
        org.oasis.ebxml.registry.bindings.rim.Description ebDesc = 
            new org.oasis.ebxml.registry.bindings.rim.Description();
        ((InternationalStringImpl)getDescription()).setBindingObject(ebDesc);
        ebObject.setDescription(ebDesc);

        Iterator iter = getClassifications().iterator();
        while (iter.hasNext()) {
            ClassificationImpl cls = (ClassificationImpl)iter.next();
            org.oasis.ebxml.registry.bindings.rim.Classification ebCls = cls.toBindingObject();
            ebObject.addClassification(ebCls);
        }
        
        iter = getExternalIdentifiers().iterator();
        while (iter.hasNext()) {
            ExternalIdentifierImpl extId = (ExternalIdentifierImpl)iter.next();
            org.oasis.ebxml.registry.bindings.rim.ExternalIdentifier ebExtId = extId.toBindingObject();
            ebObject.addExternalIdentifier(ebExtId);
        }
    }
    
    public void getComposedObjects(HashSet composedObjects)
        throws JAXRException
    {
        Log log = getLog();
        super.getComposedObjects(composedObjects);        
        //log.debug("getComposedObject slots" + this + " composedObjects = " + composedObjects);
        
        Collection classifications = getClassifications();
        composedObjects.addAll(classifications);
        Iterator iter = classifications.iterator();
        while (iter.hasNext()) {            
            ClassificationImpl cls = (ClassificationImpl)iter.next();
            cls.getComposedObjects(composedObjects);
        }
        //log.debug("getComposedObject classifications" + this + " composedObjects = " + composedObjects);
        
        Collection extIds = getExternalIdentifiers();
        composedObjects.addAll(extIds);
        iter = extIds.iterator();
        while (iter.hasNext()) {
            ExternalIdentifierImpl extId = (ExternalIdentifierImpl)iter.next();
            extId.getComposedObjects(composedObjects);
        }
        //log.debug("getComposedObject externalIdentifiers" + this + " composedObjects = " + composedObjects);
        
    }

    public LifeCycleManager getLifeCycleManager() throws JAXRException {
        return lcm;
    }

    /**
     * @return First exception in BulkResponse if there is one else null
     */
    RegistryException getBulkResponseException(BulkResponse response)
        throws JAXRException
    {
        Collection exceptions = response.getExceptions();
        if (exceptions != null) {
            return (RegistryException)exceptions.iterator().next();
        }
        return null;
    }

    /**
     * Throw first exception in BulkResponse if there is one else return
     */
    void checkBulkResponseExceptions(BulkResponse response)
        throws JAXRException
    {
        RegistryException ex = getBulkResponseException(response);
        if (ex != null) {
            throw ex;
        }
        return;
    }
    
    /**
     * Gest all Associations and their targets for which this object is a source.
     * Used by LifeCycleManagerImpl.saveObjects
     *
     */
    public HashSet getAssociationsAndAssociatedObjects() throws JAXRException {
        HashSet assObjects = new HashSet();
        
        // Automatically save any Association-s with an object in the save
        // list along with the target object of the Association per JAXR 1.0 spec.
        Collection asses = getAssociations();

        // Add the Association targets
        for (Iterator j = asses.iterator(); j.hasNext(); ) {
            AssociationImpl ass = (AssociationImpl)j.next();
            RegistryObject target = ass.getTargetObject();
            assObjects.add(target);
        }

        // Add also the Association-s themselves
        assObjects.addAll(asses);
            
        return assObjects;
    }
    
    
    
    /**
     * Gets all referenced objects for which this object is a referant.
     * Extended by base classes.
     * Used by LifeCycleManagerImpl.saveObjects
     *
     */
    public HashSet getRegistryObjectRefs() {
        HashSet refs = new HashSet();
        
        return refs;
    }
    
    public String toString() {
        String str = super.toString();
        try {
            str = getId() + "," + str;
        }
        catch (JAXRException e) {
            try {
                getLog().warn("Error getting id", e);
            }
            catch (JAXRException e1) {
            }
        }        
        
        return str;
    }    
    
	/** Returns true if the object specified is a RegistryObjectImpl
	  * with the same id.
	  * 
	  * @param o
	  *		The object to compare to.
	  * @return
	  *		<code>true</code> if the objects are equal.
	  * @todo
	  *		Do we need to ensure the object is the same type as this
	  *		instance? For example, this instance could be a ServiceImpl
	  *		and the object could be an ExternalLinkImpl. Could these have
	  *		the same id?
	  */
	public boolean equals(Object o) {
		if (compareTo(o) == 0) {
			return true;
		}
		else {
			return false;
		}
	}
	
    /**
     * Compares two registries objects.
     * Consider adding Coparable to RegistryObject in JAXR 2.0??
     *
     * @return 0 (equal) is the id of the objects matches this objects id. 
     * Otherwise return -1 (this object is less than arg o).
     */
    public int compareTo(Object o) {
        int result = -1;
        
        if (o instanceof RegistryObject) {
            try {
                String myId = getId();        
                String otherId = ((RegistryObject)o).getKey().getId();
                result = myId.compareTo(otherId);
            }
            catch (JAXRException e) {
                e.printStackTrace();
            }
        }
        
        return result;
    }
    
    protected Log getLog() throws JAXRException {
        Log log = ((RegistryServiceImpl)(lcm.getRegistryService())).getConnection().getConnectionFactory().getLog();
        return log;
    }
    
}
