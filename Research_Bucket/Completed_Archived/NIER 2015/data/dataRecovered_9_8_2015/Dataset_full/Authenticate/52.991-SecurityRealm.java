/*-*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 0 -*-*/
/*.IH,SecurityRealm,======================================*/
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
/*.IA,	PUBLIC Include File SecurityRealm.java			*/
package org.openebxml.comp.security;

/************************************************
	Includes
\************************************************/
import org.openebxml.comp.util.*;

/**
 *  Class SecurityRealm
 *
 * @author <a href="mailto:anderst@toolsmiths.se">Anders W. Tell   Financial Toolsmiths AB</a>
 * @version $Id: SecurityRealm.java,v 1.1 2002/04/14 20:20:06 awtopensource Exp $
 */

public interface SecurityRealm {

	/**
	 * Get the value of Name.
	 * @return value of Name.
	 */
	public String getName();


    /*------------------------------------------------------------------*/
	/****/
	public  AccessControlPolicy  AuthenticateResource(String resourceID, String credentials);
	/****/
	public  AccessControlPolicy  AuthenticateResource(String resourceID, byte[] credentials);
	/****/
	public  AccessControlPolicy  AuthenticateResource(String resourceID, Object[] certificates);
	/****/
	public  AccessControlPolicy  AuthenticateResource(String resourceID, String digest,
                                              String uniqueToken,
                                              String secondMD5);

    /*------------------------------------------------------------------*/
	/****/
	public  Principal  Authenticate(String userName, String credentials);
	/****/
	public  Principal  Authenticate(String userName, byte[] credentials);
	/****/
	public  Principal  Authenticate(String userName, Object[] certificates);
	/****/
	public  Principal  Authenticate(String userName, String digest,
									String uniqueToken,
									String secondMD5);

	/****/
	public boolean havePermission(Principal principal, AccessControlPolicy policy, String methodName);

}


/*.IEnd,SecurityRealm,====================================*/
