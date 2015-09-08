/*-*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 0 -*-*/
/*.IH,SecurityContextRealm,======================================*/
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
/*.IA,	PUBLIC Include File SecurityContextRealm.java			*/
package org.openebxml.comp.security.sc;

/************************************************
	Includes
\************************************************/
import java.util.Vector;	        /* JME CLDC 1.0 */

import org.openebxml.comp.util.*;
import org.openebxml.comp.security.*;

/**
 *  Class SecurityContextRealm
 *
 * @author <a href="mailto:anderst@toolsmiths.se">Anders W. Tell   Financial Toolsmiths AB</a>
 * @version $Id: SecurityContextRealm.java,v 1.1 2002/04/14 20:20:06 awtopensource Exp $
 */

public class SecurityContextRealm implements  SecurityRealmImpl {

	/****/
	protected String fName;
	
	/****/
	protected SecurityContext fSecurityContext;

    /****/
    public SecurityContextRealm(SecurityContext securityContext)
    {
		fSecurityContext	= securityContext;
		fName				= "";
    }


	/**
	 * Get the value of Name.
	 * @return value of Name.
	 */
	public String getName() {
		return fName;
	}
	


	/**
	 * Get the value of SecurityContext.
	 * @return value of SecurityContext.
	 */
	public SecurityContext getSecurityContext() {
		return fSecurityContext;
	}
	
	/**
	 * Set the value of SecurityContext.
	 * @param v  Value to assign to SecurityContext.
	 */
	public void setSecurityContext(SecurityContext  v) {
		this.fSecurityContext = v;
	}
	
	/****/
	public void Init(String name, Object config)
	throws ConfigurationException
    {
		fName				= name;
    }
	/*----------------------------------------------------------------*/
	/*----------------------------------------------------------------*/
	/****/
	public  AccessControlPolicy  AuthenticateResource(String resourceID, String credentials)
    {
		/*.TODO Check credentials*/

		return null;
    }

	/****/
	public  AccessControlPolicy  AuthenticateResource(String resourceID, byte[] credentials)
    {
		/*.TODO Check credentials*/

		return null;
    }

	/****/
	public  AccessControlPolicy  AuthenticateResource(String resourceID, Object[] certificates)
    {
		/*.TODO Check credentials*/

		return null;
    }

	/****/
	public  AccessControlPolicy  AuthenticateResource(String resourceID, String digest,
                                              String uniqueToken,
                                              String secondMD5)
    {
		/*.TODO Check credentials*/

		return null;
    }



	/*----------------------------------------------------------------*/
	/*----------------------------------------------------------------*/
	/****/
	public  Principal  Authenticate(String identity, String credentials)
    {
		/* Initial test */
		Identity   	lU = fSecurityContext.findIdentity(identity);
		if( lU == null )
			{
			return null;
			}

		/*.TODO Check credentials*/

		
		/* Return snapshot of credentials and roles */
		return new SecurityContextPrincipal(this,
                                            null, null,
                                            null, null, null,
                                            /*.TODO 
                                            lU, credentials,
                                            */
                                            fSecurityContext);
	}
	
	/****/
	public  Principal  Authenticate(String userName, byte[] credentials)
    {
		if( credentials != null )
			{
			return Authenticate(userName, credentials.toString());
			}
		else
			{
			return Authenticate(userName, (String)null);
			}
	}
	/****/
	public  Principal  Authenticate(String userName, Object[] certificates)
    {
		/*.TODO impl this method */
		if( certificates != null )
			{
			return Authenticate(userName, certificates.toString());
			}
		else
			{
			return Authenticate(userName, (String)null);
			}
	}
	/****/
	public  Principal  Authenticate(String userName, String digest,
									String uniqueToken,
									String secondMD5)
	{
		/*.TODO Check credentials*/

		return null;
	}

	/****/
	public boolean havePermission(Principal principal, AccessControlPolicy policy,String methodName)
    {
		if( principal== null || methodName == null )
			{return false;}
		if( !( principal instanceof DefaultPrincipal) )
			{return false;}

		DefaultPrincipal	lP = (DefaultPrincipal)principal;
		if( lP.getSecurityRealm() != this )
			{return false;}

		/*.TODO return lP.hasRole(methodName); */
        return false;
	}

}


/*.IEnd,SecurityContextRealm,====================================*/
