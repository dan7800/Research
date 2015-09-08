/*-*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 0 -*-*/
/*.IH,SecurityContext,======================================*/
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
/*.IA,	PUBLIC Include File SecurityContext.java			*/
package org.openebxml.comp.security.sc;

/************************************************
	Includes
\************************************************/
import java.util.Enumeration;       /* JME CLDC 1.0 */
import java.util.Vector;	        /* JME CLDC 1.0 */

import org.openebxml.comp.security.*;

/**
 *  Class SecurityContext
 *
 * @author <a href="mailto:anderst@toolsmiths.se">Anders W. Tell   Financial Toolsmiths AB</a>
 * @version $Id: SecurityContext.java,v 1.1 2002/04/14 20:20:06 awtopensource Exp $
 */

public interface SecurityContext {


	/****/
	public String	getID();

	/****/
	public SecurityRealm	getSecurityRealm();

	/*-------------------------------------------------------------*/
	/****/
	public Enumeration	getPrincipals();
	/****/
	public Principal	makePrincipal(SecurityRealm realm,String principalName, 
                                      byte[] credentials,
                                      String description);
	/****/
	public Principal	findPrincipal(String name);
	/****/
	public void			removePrincipal(Principal principal);

	/*-------------------------------------------------------------*/
	/****/
	public Enumeration	getGroups();
	/****/
	public Group		makeGroup(String groupName, String description);
	/****/
	public Group        addGroup(Group group);
	/****/
	public Group		findGroup(String name);
	/****/
	public void			removeGroup(String name);
	/****/
	public void			removeAllGroups();

    /****/
    public void         addAssociation(Group parent, Group child);
    /****/
    public void         removeAssociation(Group parent, Group child);
    /****/
    public void         addAssociation(Group group, Role role);
    /****/
    public void         removeAssociation(Group group, Role role);
    /****/
    public void         addAssociation(Group group, Identity identity);
    /****/
    public void         removeAssociation(Group group, Identity identity);
    /****/
    public void         addAssociation(Identity identity, Role role);
    /****/
    public void         removeAssociation( Identity identity, Role role);

	/*-------------------------------------------------------------*/
	/****/
	public Enumeration	getRoles();
	/****/
	public Role			makeRole(String roleName, String description);
	/****/
	public Role			findRole(String name);
	/****/
	public void			removeRole(String name);
	/*-------------------------------------------------------------*/
	/****/
	public Enumeration	getIdentitys();
	/****/
	public Identity		makeIdentity(String identityName, byte[] credentials,
                                     String description);
	/****/
	public Identity		findIdentity(String name);
	/****/
	public void			removeIdentity(String name);

	/*-------------------------------------------------------------*/
	/****/
	public PrivilegeAttribute[] getValidPrivileges(Identity identity);

}


/*.IEnd,SecurityContext,====================================*/
