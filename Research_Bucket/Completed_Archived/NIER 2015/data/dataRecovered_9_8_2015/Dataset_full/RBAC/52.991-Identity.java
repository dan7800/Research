/*-*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 0 -*-*/
/*.IH,Identity,======================================*/
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
/*.IA,	PUBLIC Include File Identity.java			*/
package org.openebxml.comp.security;

/************************************************
	Includes
\************************************************/
import java.util.Vector;	        /* JME CLDC 1.0 */
import java.util.Enumeration;       /* JME CLDC 1.0 */


/**
 *  Class Identity
 *
 * @author <a href="mailto:anderst@toolsmiths.se">Anders W. Tell   Financial Toolsmiths AB</a>
 * @version $Id: Identity.java,v 1.1 2002/04/14 20:20:05 awtopensource Exp $
 */

public interface Identity  extends PrivilegeAttribute{

	
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
	 * Get the value of IdentityName.
	 * @return value of IdentityName.
	 */
	public String getIdentityName();

	/**
	 * Set the value of IdentityName.
	 * @param v  Value to assign to IdentityName.
	 */
	public void setIdentityName(String  v);

	/**
	 * Get the value of Password.
	 * @return value of Password.
	 */
	public byte[] getPassword();

	/**
	 * Set the value of Password.
	 * @param v  Value to assign to Password.
	 */
	public void setPassword(byte[]  v);

	/***/
	public Enumeration getGroups();
	/****/
	public void     addGroup(Group group);
	/****/
	public void     removeGroup(String name);
	/****/
	public void     removeAllGroups();
	/****/
	public boolean  isMemberInGroup(String groupID);
	/****/
	public boolean  isMemberInGroup(Group group);
 
	/***/
	public Enumeration getRoles();
	/****/
	public void     addRole(Role role);
	/****/
	public void     removeRole(String name);
	/****/
	public void     removeAllRoles();
	/****/
	public boolean  hasRole(String roleID);
	/****/
	public boolean  hasRole(Role role);
 
}


/*.IEnd,Identity,====================================*/
