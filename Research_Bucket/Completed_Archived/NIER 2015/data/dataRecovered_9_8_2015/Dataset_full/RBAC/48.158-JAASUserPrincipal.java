// ========================================================================
// $Id: JAASUserPrincipal.java,v 1.6 2004/07/01 19:12:08 janb Exp $
// Copyright 2002-2004 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at 
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================

package org.mortbay.jaas;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/* ---------------------------------------------------- */
/** JAASUserPrincipal
 * <p>Implements the JAAS version of the 
 *  org.mortbay.http.UserPrincipal interface.
 *
 * @version $Id: JAASUserPrincipal.java,v 1.6 2004/07/01 19:12:08 janb Exp $
 * @author Jan Bartel (janb)
 */
public class JAASUserPrincipal implements Principal 
{
    private static Log log = LogFactory.getLog(JAASUserPrincipal.class);

    
    
    /* ------------------------------------------------ */
    /** RoleStack
     * <P>
     *
     */
    public static class RoleStack
    {
        private static ThreadLocal local = new ThreadLocal();
        

        public static boolean empty ()
        {
            Stack s = (Stack)local.get();

            if (s == null)
                return false;

            return s.empty();
        }
        


        public static void push (JAASRole role)
        {
            Stack s = (Stack)local.get();

            if (s == null)
            {
                s = new Stack();
                local.set (s);
            }

            s.push (role);
        }


        public static void pop ()
        {
            Stack s = (Stack)local.get();

            if ((s == null) || s.empty())
                return;

            s.pop();
        }

        public static JAASRole peek ()
        {
            Stack s = (Stack)local.get();
            
            if ((s == null) || (s.empty()))
                return null;
            
            
            return (JAASRole)s.peek();
        }
        
        public static void clear ()
        {
            Stack s = (Stack)local.get();

            if ((s == null) || (s.empty()))
                return;

            s.clear();
        }
        
    }
    
    //holds the JAAS Credential and roles associated with
    //this UserPrincipal 
    private Subject subject = null;

    private static RoleStack runAsRoles = new RoleStack();
    
    private RoleCheckPolicy roleCheckPolicy = null;
    private String name = null;
    

    
    
    
    /* ------------------------------------------------ */
    /** Constructor. 
     * @param name the name identifying the user
     */
    public JAASUserPrincipal(String name)
    {
        this.name = name;
    }
    

    /* ------------------------------------------------ */
    /** Check if user is in role
     * @param roleName role to check
     * @return true or false accordint to the RoleCheckPolicy.
     */
    public boolean isUserInRole (String roleName)
    {
        if (roleCheckPolicy == null)
            roleCheckPolicy = new StrictRoleCheckPolicy();

        return roleCheckPolicy.checkRole (new JAASRole(roleName),
                                          runAsRoles.peek(),
                                          getRoles());
    }

    
    /* ------------------------------------------------ */
    /** Determine the roles that the LoginModule has set
     * @return 
     */
    public Group getRoles ()
    {
        Group roleGroup = null;
        
        //try extracting a Group named "Roles" whose members will
        //be Principals that are role names
        Set s = subject.getPrincipals (java.security.acl.Group.class);
        Iterator itor = s.iterator();
        
        while (itor.hasNext() && (roleGroup == null))
        {
            Group g = (Group)itor.next();
            
            if (g.getName().equalsIgnoreCase(JAASGroup.ROLES))
                roleGroup = g;
        }

        if(log.isDebugEnabled())log.debug("Group named \"Roles\""+(roleGroup==null?"does not exist":"does exist"));
        
        
        if (roleGroup != null)
        {
            if(log.isDebugEnabled())
	    {
                Enumeration members = roleGroup.members();
                while (members.hasMoreElements())
                    log.debug("Member = "+((Principal)members.nextElement()).getName());
            }
            
            return roleGroup;
        }

        if(log.isDebugEnabled())log.debug("Trying to find org.mortbay.jaas.JAASRoles instead");

        //try extracting roles put into the Subject directly
        Set roles = subject.getPrincipals (org.mortbay.jaas.JAASRole.class);
        if (!roles.isEmpty())
        {
            roleGroup = new JAASGroup(JAASGroup.ROLES);
            itor = roles.iterator();
            while (itor.hasNext())
                roleGroup.addMember ((JAASRole)itor.next());

            return roleGroup;
        }
        
        //else - user has no roles
        if(log.isDebugEnabled())log.debug("User has no roles");
        
        return new JAASGroup(JAASGroup.ROLES);
    }

    /* ------------------------------------------------ */
    /** Set the type of checking for isUserInRole
     * @param policy 
     */
    public void setRoleCheckPolicy (RoleCheckPolicy policy)
    {
        roleCheckPolicy = policy;
    }
    

    /* ------------------------------------------------ */
    /** Temporarily associate a user with a role.
     * @param roleName 
     */
    public void pushRole (String roleName)
    {
        runAsRoles.push (new JAASRole(roleName));
    }

    
    /* ------------------------------------------------ */
    /** Remove temporary association between user and role.
     */
    public void popRole ()
    {
        runAsRoles.pop ();
    }


    /* ------------------------------------------------ */
    /** Clean out any pushed roles that haven't been popped
     */
    public void disassociate ()
    {
        runAsRoles.clear();
    }


    /* ------------------------------------------------ */
    /** Get the name identifying the user
     * @return 
     */
    public String getName ()
    {
        return name;
    }
    
    
    /* ------------------------------------------------ */
    /** Sets the JAAS subject for this user.
     *  The subject contains:
     * <ul>
     * <li> the user's credentials
     * <li> Principal for the user's roles
     * @param subject 
     */
    protected void setSubject (Subject subject)
    {
        this.subject = subject;
    }
    
    /* ------------------------------------------------ */
    /** Provide access to the current Subject
     * @return 
     */
    public Subject getSubject ()
    {
        return this.subject;
    }
    
}
