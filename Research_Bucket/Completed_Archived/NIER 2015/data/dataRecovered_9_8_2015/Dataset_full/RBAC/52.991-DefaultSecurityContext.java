/*-*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 0 -*-*/
/*.IH,DefaultSecurityContext,======================================*/
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
/*.IA,	PUBLIC Include File DefaultSecurityContext.java			*/
package org.openebxml.comp.security.sc;


/************************************************
	Includes
\************************************************/
import java.util.Enumeration;       /* JME CLDC 1.0 */
import java.util.Hashtable;	        /* JME CLDC 1.0 */
import java.util.Vector;	        /* JME CLDC 1.0 */

import org.openebxml.comp.security.*;

/**
 *  Class DefaultSecurityContext
 *
 * @author <a href="mailto:anderst@toolsmiths.se">Anders W. Tell   Financial Toolsmiths AB</a>
 * @version $Id: DefaultSecurityContext.java,v 1.1 2002/04/14 20:20:06 awtopensource Exp $
 */

public class DefaultSecurityContext  implements SecurityContext {

	/****/
	protected	String		    fID;
    /****/
    protected   SecurityRealm   fSecurityRealm;
	/****/
	protected	Hashtable	fPrincipals;
	/****/
	protected	Hashtable	fGroups;
	/****/
	protected	Hashtable	fRoles;
	/****/
	protected	Hashtable	fIdentitys;
    /****/
    public DefaultSecurityContext(String id, SecurityRealm securityRealm)
    {
		fID		        = id;
        fSecurityRealm  = securityRealm;
		fPrincipals	    = new Hashtable();
		fGroups	        = new Hashtable();
		fRoles	        = new Hashtable();
		fIdentitys	    = new Hashtable();
    }

	/****/
	public String	getID(){return fID;}

	/****/
	public SecurityRealm	getSecurityRealm() {return fSecurityRealm;};

	/*-------------------------------------------------------------*/
	/****/
	public Enumeration	getPrincipals(){
		return fRoles.elements();
	}

	/****/
	public Principal  makePrincipal(SecurityRealm realm,
                                    String principalName, 
                                    byte[] credentials,
                                    String description)
    {
        /*.TODO get Identity for this principal */
        SecurityContextPrincipal  lNew = new SecurityContextPrincipal(realm, 
                                                       null,/*.TODO principalName, */
                                                       credentials, 
                                                       /*.TODO description*/
                                                       null, null,null,
                                                       this);
		synchronized(fPrincipals) 
			{
			fPrincipals.put(lNew.getID(),lNew);
			}
		return lNew;
	}

	/****/
	public Principal		findPrincipal(String name) {
		synchronized(fPrincipals) 
			{
			return (Principal)fPrincipals.get(name);
			}
	}
	/****/
	public void	removePrincipal(Principal principal){
		synchronized(fPrincipals) 
			{
			fPrincipals.remove(((DefaultPrincipal)principal).getID());
			/*.TODO notify Identitys, Roles, Groups of revocations */
			}
	}
	/*-------------------------------------------------------------*/
	/****/
	public Enumeration	getGroups()
    {
		return fGroups.elements();
	}

	/****/
	public Group	makeGroup(String groupName, String description) 
    {
        DefaultGroup   lNew = new DefaultGroup(fSecurityRealm, groupName, description);
			
		return lNew;
	}
	
	/****/
	public Group addGroup(Group group) 
    {
		synchronized(fGroups) 
			{
            Group   lFound = (Group)fGroups.get(group.getGroupName());
            if( lFound != null )
                {return lFound;}
            
			fGroups.put(group.getGroupName(), group);
            return group;
			}
	}
	
	/****/
	public Group	findGroup(String name) 
    {
		synchronized(fGroups) 
			{
			return (Group)fGroups.get(name);
			}
	}
    
    /**
     * Recursive parent to child removal.
     */
    protected void GroupRemove(Group group)
    {
        Enumeration lEnum = group.getRoles();
        while( lEnum.hasMoreElements() )
            {
            String lsRoleName = (String)lEnum.nextElement();

            group.removeRole(lsRoleName);

            BaseRole    lRole = (BaseRole)findRole(lsRoleName);
            if( lRole != null )
                {
                lRole.removeGroup(group.getID());
                }
            }/*while*/

        lEnum = group.getIdentitys();
        while( lEnum.hasMoreElements() )
            {
            String lsIdentityName = (String)lEnum.nextElement();
            group.removeIdentity(lsIdentityName);

            Identity    lIdentity = findIdentity(lsIdentityName);
            if( lIdentity != null )
                {
                lIdentity.removeGroup(group.getID());
                }
            }/*while*/

        /* Children */
        lEnum = group.getGroups();
        while( lEnum.hasMoreElements() )
            {
            String lsGroupName = (String)lEnum.nextElement();
            Group lG = group.findGroup(lsGroupName);
            lG.removeGroup(lsGroupName);
            this.GroupRemove(lG);
            }/*while*/
    }

	/****/
	public void	removeGroup(String groupName) 
    {
        Group   lFound;
		synchronized(fGroups) 
			{
            lFound = (Group)fGroups.get(groupName);
            if( lFound == null )
                {return;}
			fGroups.remove(groupName);
			}

        GroupRemove(lFound);

        /*.TODO notify Principals of revocations */
	}
    
	/****/
	public void	removeAllGroups()
    {
        Enumeration lEnum = fGroups.keys();    
        while( lEnum.hasMoreElements() )
            {
            String lsGroupName = (String)lEnum.nextElement();
            this.removeGroup(lsGroupName);
            }/*while*/
    }

	/*-------------------------------------------------------------*/
    /****/
    public void         addAssociation(Group parent, Group child)
    {
        parent.addGroup(child);
        child.setParentGroup(parent);
    }

    /****/
    public void         removeAssociation(Group parent, Group child)
    {
        parent.removeGroup(child.getGroupName());
        GroupRemove(child);

        /*.TODO notify Principals of revocations */
    }

    /****/
    public void         addAssociation(Group group, Role role)
    {
        group.addRole(role);
        ((BaseRole)role).addGroup(group);
    }

    /****/
    public void         removeAssociation(Group group, Role role)
    {
        group.removeRole(role.getID());
        ((BaseRole)role).removeGroup(group.getID());
        /*.TODO notify Principals of revocations */
    }

    /****/
    public void         addAssociation(Group group, Identity identity)
    {
        group.addIdentity(identity);
        identity.addGroup(group);
    }

    /****/
    public void         removeAssociation(Group group, Identity identity)
    {
        group.removeIdentity(identity.getID());
        identity.removeGroup(group.getID());
        /*.TODO notify Principals of revocations */
    }

    /****/
    public void         addAssociation(Identity identity, Role role)
    {
        identity.addRole(role);
        ((BaseRole)role).addIdentity(identity);
    }
    /****/
    public void         removeAssociation( Identity identity, Role role)
    {
        identity.removeRole(role.getID());
        ((BaseRole)role).removeIdentity(identity.getID());

        /*.TODO notify Principals of revocations */
    }

	/*-------------------------------------------------------------*/
	/****/
	public Enumeration	getRoles() 
    {
		return fRoles.elements();
	}
	/****/
	public Role		makeRole(String roleName, String description)
     {
         Role lFound = (Role)fRoles.get(roleName);
         if( lFound != null )
             {return lFound;}

         DefaultRole	lNew = new DefaultRole(fSecurityRealm,roleName, description);
         synchronized(fRoles) 
             {
             fRoles.put(lNew.getID(),lNew);
             }
         return lNew;
	}
	/****/
	public Role		findRole(String name) 
    {
		synchronized(fRoles) 
			{
			return (Role)fRoles.get(name);
			}
	}
	/****/
	public void		removeRole(String name)
    {
         Role lFound = (Role)fRoles.get(name);
         if( lFound == null )
             {return ;}

		synchronized(fRoles) 
			{
			fRoles.remove(name);
            /*.TODO remove associations */

			/*.TODO notify Principals of revocations */
			}
	}
	/*-------------------------------------------------------------*/
	/****/
	public Enumeration	getIdentitys()
    {
		return fRoles.elements();
	}
	/****/
	public Identity		makeIdentity(String identityName, 
                                     byte[] credentials,
                                     String description)
    {
        Identity lFound = (Identity)fIdentitys.get(identityName);
        if( lFound != null )
            {
            /*.TODO match credentials */
            return lFound;
            }

		DefaultIdentity	lNew = new DefaultIdentity(fSecurityRealm, identityName, description,credentials);
		synchronized(fIdentitys) 
			{
			fIdentitys.put(lNew.getID(),lNew);
			}
		return lNew;
	}

	/****/
	public Identity		findIdentity(String name) {
		synchronized(fIdentitys) 
			{
			return (Identity)fIdentitys.get(name);
			}
	}
	/****/
	public void	removeIdentity(String name)
    {
        Identity lFound = (Identity)fIdentitys.get(name);
        if( lFound == null )
            return;

		synchronized(fIdentitys) 
			{
			fIdentitys.remove(name);
            /*.TODO remove associations */
			/*.TODO notify Principals of revocations */
			}
	}



	/****/
	public PrivilegeAttribute[] getValidPrivileges(Identity identity)
    {
        if(identity == null )
            return null;

		synchronized(fIdentitys) 
			{
			Vector	lValid = new Vector();
            lValid.add(identity);

            /*.TODO implement roles and groups */

            int liNo = lValid.size();
            PrivilegeAttribute[] lNew = new PrivilegeAttribute[liNo];
			for(int i = 0; i < liNo;i++)
				{
                /*.TODO copy or reference */
                lNew[i] = (PrivilegeAttribute)lValid.elementAt(i);
                }/*for*/

            return lNew;
			}
    }

}


/*.IEnd,DefaultSecurityContext,====================================*/
