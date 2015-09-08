/*-*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 0 -*-*/
/*.IH,Role,======================================*/
/*.IC,--- COPYRIGHT (c) --  Open ebXML - 2001,2002 ---

     The contents of this file are subject to the Open ebXML Public License
     Version 1.0 (the "License"); you may not use this file except in
     compliance with the License. You may obtain a copy of the License at
     'http://www.openebxml.org/LICENSE/OpenebXML-LICENSE-1.0.txt'

     Software distributed under the License is distributed on an "AS IS"
     basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
     License for the specific language governing rights and limitations
     under the License.

     The Initial Developer of the Original Code is Anders W. Tell.
     Portions created by Financial Toolsmiths AB are Copyright (C) 
     Financial Toolsmiths AB 1993-2001. All Rights Reserved.

     Contributor(s): see author tag.

---------------------------------------------------------------------*/
/*.IA,	PUBLIC Include File Role.java			*/
package org.openebxml.comp.security;


/************************************************
	Includes
\************************************************/

/**
 *  Class Role
 *
 * @author <a href="mailto:anderst@toolsmiths.se">Anders W. Tell   Financial Toolsmiths AB</a>
 * @version $Id: Role.java,v 1.1 2002/04/14 20:20:06 awtopensource Exp $
 */

public interface Role  extends PrivilegeAttribute {
	
	/**
	 * Get the value of SecurityRealm.
	 * @return value of SecurityRealm.
	 */
	public SecurityRealm getSecurityRealm();

	/**
	 * Get the value of Description.
	 * @return value of Description.
	 */
	public String getDescription();

	/**
	 * Set the value of Description.
	 * @param v  Value to assign to Description.
	 */
	public void setDescription(String  v);

	/**
	 * Get the value of RoleName.
	 * @return value of RoleName.
	 */
	public String getRoleName();

	/**
	 * Set the value of RoleName.
	 * @param v  Value to assign to RoleName.
	 */
	public void setRoleName(String  v);
}


/*.IEnd,Role,====================================*/
/*
 * $Header: /cvsroot/sino/ebxmlrr-spec/src/share/org/oasis/ebxml/registry/infomodel/Role.java,v 1.2 2001/08/05 14:55:18 najmi Exp $
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

/**
 * A security Role PrivilegeAttribute. For example a hospital may have Roles such as Nurse, Doctor, Administrator etc. Roles are used to grant Privileges to Principals. For example a Doctor may be allowed to write a prescription but a Nurse may not.
 */
public interface Role extends PrivilegeAttribute {}
