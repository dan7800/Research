/*-*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 0 -*-*/
/*.IH,SequenceRealm,======================================*/
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
/*.IA,	PUBLIC Include File SequenceRealm.java			*/
package org.openebxml.comp.security;

/************************************************
	Includes
\************************************************/
import java.util.Vector;	        /* JME CLDC 1.0 */

/**
 *  Class SequenceRealm
 *
 * @author <a href="mailto:anderst@toolsmiths.se">Anders W. Tell   Financial Toolsmiths AB</a>
 * @version $Id: SequenceRealm.java,v 1.1 2002/04/14 20:20:06 awtopensource Exp $
 */

public class SequenceRealm implements SecurityRealm {

    /****/
    public static final String      NAME = "Sequence";
    /*----------------------------------------------------------------*/
    /***/
    protected SecurityManager   fSecurityManager;
    /****/
    protected Vector            fRealms;
	
    /****/
    public SequenceRealm(SecurityManager sMgr)
    {
        fRealms             = new Vector();
        fSecurityManager    = sMgr;
    }
	/**
	 * Get the value of Name.
	 * @return value of Name.
	 */
	public String getName(){return NAME;}

	/**
	 * Get the value of SecurityManager.
	 * @return value of SecurityManager.
	 */
	public SecurityManager getSecurityManager(){return fSecurityManager;}

    /****/
    public void addRealm(SecurityRealm realm)
    {
        fRealms.add(realm);
    }
    /****/
    public void removeRealm(SecurityRealm realm)
    {
        fRealms.remove(realm);
    }
    /****/
    public void removeRealms()
    {
        fRealms.clear();
    }

	/*----------------------------------------------------------------*/
	/*----------------------------------------------------------------*/
	/****/
	public  AccessControlPolicy  AuthenticateResource(String resourceID, String credentials)
    {
        int liNo = fRealms.size();
        for(int i = 0; i< liNo;i++)
            {
            AccessControlPolicy  lFound = ((SecurityRealm)fRealms.elementAt(i)).AuthenticateResource(resourceID,credentials);
            if( lFound != null )
                {
                return lFound;
                }
            }/*for*/
        return null;
	}
	/****/
	public  AccessControlPolicy  AuthenticateResource(String resourceID, byte[] credentials)
    {
        int liNo = fRealms.size();
        for(int i = 0; i< liNo;i++)
            {
            AccessControlPolicy  lFound = ((SecurityRealm)fRealms.elementAt(i)).AuthenticateResource(resourceID, credentials);
            if( lFound != null )
                {
                return lFound;
                }
            }/*for*/
        return null;
	}

	/****/
	public  AccessControlPolicy  AuthenticateResource(String resourceID, Object[] certificates)
    {
        int liNo = fRealms.size();
        for(int i = 0; i< liNo;i++)
            {
            AccessControlPolicy  lFound = ((SecurityRealm)fRealms.elementAt(i)).AuthenticateResource(resourceID, certificates);
            if( lFound != null )
                {
                return lFound;
                }
            }/*for*/
        return null;
	}

	/****/
	public  AccessControlPolicy  AuthenticateResource(String resourceID, String digest,
                                              String uniqueToken,
                                              String secondMD5)
	{
        int liNo = fRealms.size();
        for(int i = 0; i< liNo;i++)
            {
            AccessControlPolicy  lFound = ((SecurityRealm)fRealms.elementAt(i)).AuthenticateResource(resourceID, digest, uniqueToken,secondMD5);
            if( lFound != null )
                {
                return lFound;
                }
            }/*for*/
        return null;
	}


	/*----------------------------------------------------------------*/
	/*----------------------------------------------------------------*/
	/****/
	public  Principal  Authenticate(String userName, String credentials)
    {
        int liNo = fRealms.size();
        for(int i = 0; i< liNo;i++)
            {
            Principal  lFound = ((SecurityRealm)fRealms.elementAt(i)).Authenticate(userName,credentials);
            if( lFound != null )
                {
                return lFound;
                }
            }/*for*/
        return null;
	}
	
	/****/
	public  Principal  Authenticate(String userName, byte[] credentials)
    {
        int liNo = fRealms.size();
        for(int i = 0; i< liNo;i++)
            {
            Principal  lFound = ((SecurityRealm)fRealms.elementAt(i)).Authenticate(userName,credentials);
            if( lFound != null )
                {
                return lFound;
                }
            }/*for*/
        return null;
	}
	/****/
	public  Principal  Authenticate(String userName, Object[] certificates)
    {
        int liNo = fRealms.size();
        for(int i = 0; i< liNo;i++)
            {
            Principal  lFound = ((SecurityRealm)fRealms.elementAt(i)).Authenticate(userName, certificates);
            if( lFound != null )
                {
                return lFound;
                }
            }/*for*/
        return null;
	}
	/****/
	public  Principal  Authenticate(String userName, String digest,
									String uniqueToken,
									String secondMD5)
	{
        int liNo = fRealms.size();
        for(int i = 0; i< liNo;i++)
            {
            Principal  lFound = ((SecurityRealm)fRealms.elementAt(i)).Authenticate(userName, digest, uniqueToken,secondMD5);
            if( lFound != null )
                {
                return lFound;
                }
            }/*for*/
        return null;
	}

	/**
     * OR-ing together all realms.
     */
	public boolean havePermission(Principal principal, AccessControlPolicy policy,String methodName)
    {
        int liNo = fRealms.size();
        for(int i = 0; i< liNo;i++)
            {
            boolean lFound = ((SecurityRealm)fRealms.elementAt(i)).havePermission(principal, policy,methodName);
            if( lFound )
                {
                return lFound;
                }
            }/*for*/
        return false;
	}

}


/*.IEnd,SequenceRealm,====================================*/
