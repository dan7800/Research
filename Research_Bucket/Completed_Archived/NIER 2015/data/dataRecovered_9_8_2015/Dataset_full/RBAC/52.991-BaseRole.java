/*-*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 0 -*-*/
/*.IH,BaseRole,======================================*/
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
/*.IA,	PUBLIC Include File BaseRole.java			*/
package org.openebxml.comp.security;


/************************************************
	Includes
\************************************************/
import java.util.Enumeration;       /* JME CLDC 1.0 */

/**
 *  Class BaseRole
 *
 * @author <a href="mailto:anderst@toolsmiths.se">Anders W. Tell   Financial Toolsmiths AB</a>
 * @version $Id: BaseRole.java,v 1.1 2002/04/14 20:20:05 awtopensource Exp $
 */

public abstract class BaseRole implements Role {

	/****/
	protected String	fDescription;
	/****/
	protected String	fRoleName;

    /****/
    public BaseRole()
    {
		fDescription	= null;
		fRoleName		= null;
    }

   /****/
    public BaseRole(String roleName, String descr)
    {
		fDescription	= descr;
		fRoleName		= roleName;
    }

	/**
	 * Get the value of ID.
	 * @return value of ID.
	 */
	public String getID(){return fRoleName;};

    /****/
    public boolean isEqual(PrivilegeAttribute match)
    {
        if( this == match )
            return true;
        if( ! (match instanceof BaseRole) )
            return false;
        BaseRole  lM = (BaseRole)match;
        if( ! this.getSecurityRealm().equals(lM.getSecurityRealm()) )
            return false;

        if( ! getID().equals(lM.getID()) )
            {return false;}

        /*.TODO need more tests ??*/
        return true;
    }

	/**
	 * Get the value of Description.
	 * @return value of Description.
	 */
	public String getDescription(){return fDescription;}

	/**
	 * Set the value of Description.
	 * @param v  Value to assign to Description.
	 */
	public void setDescription(String  v) {fDescription = v;};

	/**
	 * Get the value of RoleName.
	 * @return value of RoleName.
	 */
	public String getRoleName(){return fRoleName;};

	/**
	 * Set the value of RoleName.
	 * @param v  Value to assign to RoleName.
	 */
	public void setRoleName(String  v){fRoleName = v;};

	/***/
	public abstract  Enumeration getGroups();
	/****/
	public abstract  void     addGroup(Group group);
	/****/
	public abstract  void     removeGroup(String name);
	/****/
	public abstract  void     removeAllGroups();
	/****/
	public abstract  boolean  isMemberInGroup(String groupID);
	/****/
	public abstract  boolean  isMemberInGroup(Group group);
 
	/***/
	public abstract  Enumeration getIdentitys();
	/****/
	public abstract  void     addIdentity(Identity identity);
	/****/
	public abstract  void     removeIdentity(String name);
	/****/
	public abstract  void     removeAllIdentitys();
	/****/
	public abstract  boolean  hasIdentity(String identityID);
	/****/
	public abstract  boolean  hasIdentity(Identity identity);
 

}


/*.IEnd,BaseRole,====================================*/
