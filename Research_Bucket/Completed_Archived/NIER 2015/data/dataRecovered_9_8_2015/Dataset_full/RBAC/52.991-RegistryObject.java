/* * $Header: /cvsroot/sino/ebxmlrr-spec/src/share/org/oasis/ebxml/registry/infomodel/RegistryObject.java,v 1.19 2003/05/14 21:00:21 farrukh_najmi Exp $ * * Copyright (c) 1999 Sun Microsystems, Inc. All Rights Reserved. * * This software is the confidential and proprietary information of Sun * Microsystems, Inc. ("Confidential Information").  You shall not * disclose such Confidential Information and shall use it only in * accordance with the terms of the license agreement you entered into * with Sun. * * SUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR * PURPOSE, OR NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR ANY DAMAGES * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING * THIS SOFTWARE OR ITS DERIVATIVES. * */
package org.oasis.ebxml.registry.infomodel;
import java.net.*;
import java.util.Collection;

/**
 * RegistryObject provides a common base interface for almost all objects in the 
 * information model. Information model classes whose instances have a 
 * unique identity and an independent life cycle are descendents of the 
 * RegistryObject class.  
 * <p> 
 * Note that Contact and Address are not descendents of the Object class 
 * because their instances do not have an independent existence and unique 
 * identity. They are always a part of some other class's instance 
 * (e.g Organization has an Address). 
 * 
 * @author Farrukh S. Najmi 
 * 
 */
public abstract class RegistryObject {

    /**
     * @associates <{AccessControlPolicy}>
     * @supplierCardinality 1
     */
    public AccessControlPolicy accessControlPolicy;

    /** Universally unique ID (UUID) for this object. */
    public String id;

    /**
     * Gets user friendly name of object in repository.
     */
    public InternationalString name;

    /**
     * Gets the textual description for this object.
     */
    public InternationalString description;

    /**
     * Gets the pre-defined object type that best describes the object.
     * Should it return ClassificationNode??
     * @label objectType
     */
    public String objectType;

    /**
     * @supplierCardinality 1..*
     * @clientCardinality 0..*
     * @associationAsClass Association 
     */
    private Organization lnkOrganization;

    /**
     * @link aggregationByValue
     * @label classifications
     * @supplierCardinality 0..* 
     */
    private Classification lnkClassification;

    /**
     * @associationAsClass Association
     * @supplierCardinality 0..*
     * @clientCardinality 0..* 
     */
    private RegistryObject lnkRegistryObject;

	/**
	 * Gets the Classification that classify this object.
	 *
	 */
	public abstract Collection getClassifications();
	
	/**
	 * Gets the Organizations associated with this object.
	 *
	 */
	public abstract Collection getOrganizations();	

    /**
     * Gets all Associations where this object is source.
     *
     */
    public abstract Collection getAssociations();

    /**
     * Gets the ExternalLinks associated with this object.
     * @associationAsClass Association
     * @associates <{ExternalLink}>
     * @supplierCardinality 0..*
     * @clientCardinality 0..*
     * @undirected
     * @supplierRole externalLinks
     * @clientRole linkedObjects
     */
    public abstract Collection getExternalLinks();

    /**
     * Gets the Packages that this object is a member of.
     * @associates <{org.oasis.ebxml.registry.infomodel.RegistryPackage}>
     * @supplierCardinality 0..*
     * @clientCardinality 0..*
     * @associates <{org.oasis.ebxml.registry.infomodel.RegistryPackage}>
     * @undirected
     * @clientRole members
     * @supplierRole packages
     * @associationAsClass <{Association}>
     */
    public abstract Collection getPackages();

    /**
     * Gets the collection of ExternalIdentifiers associated with this object.
     * @associates <{ExternalIdentifier}>
     * @supplierCardinality 0..*
     * @undirected
     * @supplierRole externalIdentifiers
     * @link aggregationByValue
     */
    public abstract Collection getExternalIdentifiers();

    /**
     * @directed
     * @label organization
     */
    /* #Organization lnkOrganization; */

    /**
     * Gets the Slots associated with this object.
     * @supplierCardinality 0..*
     * @associates <{Slot}>
     * @undirected
     * @supplierRole slots
     * @link aggregationByValue
     */
    public abstract Collection getSlots();
	

}
