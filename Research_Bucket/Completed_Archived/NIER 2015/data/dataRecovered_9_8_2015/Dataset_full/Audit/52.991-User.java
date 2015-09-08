/*
 * $Header: /cvsroot/sino/ebxmlrr-spec/src/share/org/oasis/ebxml/registry/infomodel/User.java,v 1.9 2003/05/14 21:00:22 farrukh_najmi Exp $
 * 
 * Copyright (c) 1999 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of Sun
 * Microsystems, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Sun.
 * 
 * SUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 * 
 */
package org.oasis.ebxml.registry.infomodel;
import java.net.*;
import java.util.*;

/**
 * A registered user of the registry.
 * User instances are used in an AuditableEvents to keep track of 
 * the identity of the requestor that sent the request that 
 * generated the AuditableEvent.
 */
public abstract class User extends RegistryObject {

    /**
     * Gets the Submitting Organization that sent the request that effected
     * this change.
     * 
     * @supplierCardinality 0..*
     * @associates <{Organization}>
     * @supplierRole affiliatedWith
     * @clientCardinality 0..*
     * @undirected
     * @associationAsClass Association*/
    public Organization organization;

    /**
     * Name of contact person
     */
    public PersonName personName;

    /**
     * The postal address for this Contact.
     * @link aggregationByValue
     * @supplierCardinality 1
     * @undirected
     */
    public PostalAddress address;

	/**
	 * The various EmailAddresses for this User. 
	 * 
	 * @supplierCardinality 1..*
	 * @associates <{EmailAddress}>
	 * @link aggregationByValue
	 * @undirected
	 */
	public Collection emailAddresses;

	/**
	 * The various telephone numbers for this User. 
	 * 
	 * @supplierCardinality 1..*
	 * @associates <{TelephoneNumber}>
	 * @link aggregationByValue
	 * @undirected
	 */
	public Collection telephoneNumbers;

    /**
     * The URL to the web page for this contact.
     */
    public URL url;
}
