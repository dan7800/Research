/*-*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 0 -*-*/
/*.IH,Group,======================================*/
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
/*.IA,	PUBLIC Include File Group.java			*/
package org.openebxml.comp.security;

/************************************************
	Includes
\************************************************/
import java.util.Vector;	        /* JME CLDC 1.0 */
import java.util.Enumeration;	        /* JME CLDC 1.0 */

/**
 *  Class Group
 *
 * @author <a href="mailto:anderst@toolsmiths.se">Anders W. Tell   Financial Toolsmiths AB</a>
 * @version $Id: Group.java,v 1.1 2002/04/14 20:20:05 awtopensource Exp $
 */

public interface Group extends PrivilegeAttribute{

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
	 * Get the value of GroupName.
	 * @return value of GroupName.
	 */
	public String getGroupName();

	/**
	 * Set the value of GroupName.
	 * @param v  Value to assign to GroupName.
	 */
	public void setGroupName(String  v);

    /****/
    public Group    getParentGroup();
    /****/
    public void     setParentGroup(Group group);

    /*-------------------------------------------------------------*/
	/**
	 * Get groups assigned to the group..
	 */
	public Enumeration getGroups();
	/****/
	public void     addGroup(Group group);
	/****/
	public Group	findGroup(String name);
	/****/
	public void     removeGroup(String name);
	/****/
	public void     removeAllGroups();
	/****/
	public boolean  isGroupMember(String name);

    /*-------------------------------------------------------------*/
	/**
	 * Get roles assigned to this group.
	 */
	public Enumeration   getRoles();
	/****/
	public void     addRole(Role role);
	/****/
	public void     removeRole(String name);
	/****/
	public void     removeAllRoles();
	/****/
	public boolean  hasRole(String name);
	/****/
	public boolean  hasRole(Role role);

    /*-------------------------------------------------------------*/
	/**
	 * Get Identity that are members of the group.
	 */
	public Enumeration   getIdentitys();
	/****/
	public void     addIdentity(Identity identity);
	/****/
	public void     removeIdentity(String name);
	/****/
	public void     removeAllIdentitys();
	/****/
	public boolean  isMember(String name);
	/****/
	public boolean  isMember(Identity identity);
}


/*.IEnd,Group,====================================*/
