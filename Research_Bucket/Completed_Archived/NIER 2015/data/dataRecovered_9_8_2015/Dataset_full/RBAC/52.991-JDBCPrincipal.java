/*-*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 0 -*-*/
/*.IH,JDBCPrincipal,======================================*/
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
/*.IA,	PUBLIC Include File JDBCPrincipal.java			*/
package org.openebxml.comp.security.jdk;


/************************************************
	Includes
\************************************************/
import java.util.Vector;	        /* JME CLDC 1.0 */

import org.openebxml.comp.security.*;

/**
 *  Class JDBCPrincipal
 *
 * @author <a href="mailto:anderst@toolsmiths.se">Anders W. Tell   Financial Toolsmiths AB</a>
 * @version $Id: JDBCPrincipal.java,v 1.1 2002/04/14 20:20:06 awtopensource Exp $
 */

public final class JDBCPrincipal  implements Principal{

	/****/
	private JDBCSecurityRealm	fSecurityRealm;

	/**
	 */
	private String			fID;

	/**
	 * NULL Credential indicates invalid Principal. May occur after revovation of Group,Role or Group.
     */
	private byte[]			fCredentials;

    /****/
    private Identity[]      fIdentitys;
    /****/
    private Group[]         fGroups;
    /****/
    private Role[]          fRoles;

    /****/
    public JDBCPrincipal(JDBCSecurityRealm realm, 
                         String userName, byte[] credentials,
                         Identity[] identitys,
                         Group[] groups,
                         Role[] roles)
    {
 		fSecurityRealm	    = realm;
		fID		            = userName;
		fCredentials	    = credentials;
        fIdentitys          = identitys;
        fGroups             = groups;
        fRoles              = roles;
    }

	/**
	 * Get the value of ID
	 * @return value of ID
	 */
	public String getID(){
		return fID;
	}

	/**
	 * Get the value of SecurityRealm.
	 * @return value of SecurityRealm.
	 */
	public SecurityRealm getSecurityRealm(){
		return fSecurityRealm;
	}


	/**
	 * Get the value of Credentials.
	 * @return value of Credentials.
	 */
	public byte[] getCredentials(){
		return fCredentials;
	}


    /****/
	public boolean hasPrivileges(PrivilegeAttribute[]  test)
    {
		/* Test for revocations */
		if( fID == null || fCredentials == null )
			{return false;}
		if( test == null )
			{return false;}

		int liTestNo = test.length;
		for(int i = 0; i < liTestNo; i++)
			{
            boolean lbMatch = false;
            PrivilegeAttribute  lTest = test[i];
            int liNo;

            /* IDENTITYS */
            liNo = fIdentitys.length;
            for(int j = 0; j < liNo; j++)
                {
                if( lTest.isEqual( fIdentitys[j]) )
                    {
                    lbMatch = true;
                    break;
                    }
                }/*for*/
            if( lbMatch )
                {continue;}
            
            /* GROUPS */
            liNo = fGroups.length;
            for(int j = 0; j < liNo; j++)
                {
                if( lTest.isEqual( fGroups[j]) )
                    {
                    lbMatch = true;
                    break;
                    }
                }/*for*/
            if( lbMatch )
                {continue;}
            
            /* ROLES */
            liNo = fRoles.length;
            for(int j = 0; j < liNo; j++)
                {
                if( lTest.isEqual( fRoles[j]) )
                    {
                    lbMatch = true;
                    break;
                    }
                }/*for*/
            
            /* need at least one match */
            if( ! lbMatch )
                {return false;}
			}/*for test*/

        /* all Privileges was found */
		return true;
	}	

}


/*.IEnd,JDBCPrincipal,====================================*/
