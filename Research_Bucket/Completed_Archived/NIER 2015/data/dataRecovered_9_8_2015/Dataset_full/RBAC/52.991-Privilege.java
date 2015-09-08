/*
 * $Header: /cvsroot/sino/ebxmlrr-spec/src/share/org/oasis/ebxml/registry/infomodel/Privilege.java,v 1.2 2001/08/05 14:55:18 najmi Exp $
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
 * A Privilege object contains zero or more PrivilegeAttributes. A PrivilegeAttribute can be a SecurityClearence, a Group, a Role, or an Identity.
 * <p>
 * A requesting Principal must have <em>all</em> of the PrivilegeAttributes
 * specified in a Privilege in order to gain access to a method in a protected
 * Object. Permissions defined in the Object's AccessControlPolicy define
 * the Privileges that can authorize access to specific methods.
 * <p>
 * This mechanism enables  the flexibility to have object access control policies that are based on any combination of Roles, Identities, Groups or a SecurityClearences.
 * @see PrivilegeAttribute
 * @see Permission
 */
public interface Privilege {

    /**
     * Gets the PriviligeAttributes associated with this Privilege.
     * @link association
     * @associates <{PrivilegeAttribute}>
     * @supplierCardinality 0..*
     * @clientCardinality 0..*
     * @label privilgeAttributes
     * @see PriviligeAttribute
     * @undirected
     */
    Collection getPriviligeAttributes();
}
