/*
 * $Header: /cvsroot/sino/ebxmlrr-spec/src/share/org/oasis/ebxml/registry/infomodel/Principal.java,v 1.2 2001/08/05 14:55:18 najmi Exp $
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
import java.util.*;

/**
 * Principal is a completely generic term used by the security community to
 * include both people and software systems. The Principal object is an entity,
 * which has a set of PrivilegeAttributes. These PrivilegeAttributes include at
 * least one identity, and optionally a set of role memberships, group memberships
 * or security clearances. A principal is used to authenticate a requestor and to
 * authorize the requested action based on the PriviligeAttributes associated
 * with the Principal.
 * 
 * @see PrivilegeAttributes
 * @see Privilege
 * @see Permission
 */
public interface Principal {

    /**
     * Gets the Identities associated with this Principal.
     * @link association
     * @associates <{Identity}>
     * @supplierCardinality 1..*
     * @clientCardinality 0..*
     * @label identities
     * @see Identity
     * @undirected
     */
    Collection getIdentities();

    /**
     * Gets the Groups associated with this Principal.
     * @link association
     * @associates <{Group}>
     * @supplierCardinality 0..*
     * @clientCardinality 0..*
     * @label groups
     * @see Group
     * @undirected
     */
    Collection getGroups();

    /**
     * Gets the Roles associated with this Principal.
     * @link association
     * @associates <{Role}>
     * @supplierCardinality 0..*
     * @clientCardinality 0..*
     * @label roles
     * @see Role
     * @undirected
     */
    Collection getRoles();
}
