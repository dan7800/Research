// ========================================================================
// $Id: JAASUserRealm.java,v 1.12 2005/03/03 15:32:22 janb Exp $
// Copyright 2003-2004 Mort Bay Consulting Pty. Ltd.
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
import java.util.HashMap;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.UserRealm;
import org.mortbay.jaas.callback.AbstractCallbackHandler;
import org.mortbay.jaas.callback.DefaultCallbackHandler;
import org.mortbay.util.Loader;
import org.mortbay.util.LogSupport;



/* ---------------------------------------------------- */
/** JAASUserRealm
 * <p>
 *
 * <p><h4>Notes</h4>
 * <p>
 *
 * <p><h4>Usage</h4>
 * <pre>
 */
/*
 * </pre>
 *
 * @see
 * @version 1.0 Mon Apr 14 2003
 * @author Jan Bartel (janb)
 */
public class JAASUserRealm implements UserRealm
{
    private static Log log = LogFactory.getLog(JAASUserRealm.class);
	
    protected String callbackHandlerClass;
    protected String realmName;
    protected String loginModuleName;
    protected HashMap userMap;
    protected RoleCheckPolicy roleCheckPolicy;
    
    /* ---------------------------------------------------- */
    /**
     * UserInfo
     *
     * Information cached for an authenticated user.
     * 
     *
     * 
     *
     */
    protected class UserInfo
    {
        String name;
        JAASUserPrincipal principal;
        LoginContext context;

        public UserInfo (String name, JAASUserPrincipal principal, LoginContext context)
        {
            this.name = name;
            this.principal = principal;
            this.context = context;
        }

        public String getName ()
        {
            return name;
        }

        public JAASUserPrincipal getJAASUserPrincipal ()
        {
            return principal;
        }

        public LoginContext getLoginContext ()
        {
            return context;
        }
    }


    /* ---------------------------------------------------- */
    /**
     * Constructor.
     *
     */
    public JAASUserRealm ()
    {
        userMap = new HashMap();
    }
    

    /* ---------------------------------------------------- */
    /**
     * Constructor.
     *
     * @param name the name of the realm
     */
    public JAASUserRealm(String name)
    {
        this();
        realmName = name;
    }


    /* ---------------------------------------------------- */
    /**
     * Get the name of the realm.
     *
     * @return name or null if not set.
     */
    public String getName()
    {
        return realmName;
    }


    /* ---------------------------------------------------- */
    /**
     * Set the name of the realm
     *
     * @param name a <code>String</code> value
     */
    public void setName (String name)
    {
        realmName = name;
    }



    /**
     * Set the name to use to index into the config
     * file of LoginModules.
     *
     * @param name a <code>String</code> value
     */
    public void setLoginModuleName (String name)
    {
        loginModuleName = name;
    }


    public void setCallbackHandlerClass (String classname)
    {
        callbackHandlerClass = classname;
    }

    public void setRoleCheckPolicy (RoleCheckPolicy policy)
    {
        roleCheckPolicy = policy;
    }

    public Principal getPrincipal(String username)
    {
        synchronized (this)
        {
            return (Principal)userMap.get(username);
        }
    }


    /* ------------------------------------------------------------ */
    public boolean isUserInRole(Principal user, String role)
    {
        if (user instanceof JAASUserPrincipal)
            return ((JAASUserPrincipal)user).isUserInRole(role);
        return false;
    }


    /* ------------------------------------------------------------ */
    public boolean reauthenticate(Principal user)
    {
        // TODO This is not correct if auth can expire! We need to
        // get the user out of the cache
        synchronized (this)
        {
            return (userMap.get(user.getName()) != null);
        }
    }

    
    /* ---------------------------------------------------- */
    /**
     * Authenticate a user.
     * 
     *
     * @param username provided by the user at login
     * @param credentials provided by the user at login
     * @param request a <code>HttpRequest</code> value
     * @return authenticated JAASUserPrincipal or  null if authenticated failed
     */
    public Principal authenticate(String username,
            Object credentials,
            HttpRequest request)
    {
        try
        {
            UserInfo info = null;
            synchronized (this)
            {
                info = (UserInfo)userMap.get(username);
            }
            
            //user has been previously authenticated, but
            //re-authentication has been requested, so flow that 
            //thru all the way to the login module mechanism and
            //remove their previously authenticated status
            //TODO: ensure cache state and "logged in status" are synchronized
            if (info != null)
            {
                synchronized (this)
                {
                    userMap.remove (username);
                }
            }
            
            
            AbstractCallbackHandler callbackHandler = null;
            
            //user has not been authenticated
            if (callbackHandlerClass == null)
            {
                log.warn("No CallbackHandler configured: using DefaultCallbackHandler");
                callbackHandler = new DefaultCallbackHandler();
            }
            else
            {
                callbackHandler = (AbstractCallbackHandler)Loader.loadClass(JAASUserRealm.class, callbackHandlerClass).getConstructors()[0].newInstance(new Object[0]);
            }
            
            if (callbackHandler instanceof DefaultCallbackHandler)
            {
                ((DefaultCallbackHandler)callbackHandler).setRequest (request);
            }
            
            callbackHandler.setUserName(username);
            callbackHandler.setCredential(credentials);
            
            
            //set up the login context
            LoginContext loginContext = new LoginContext(loginModuleName,
                    callbackHandler);
            
            loginContext.login();
            
            //login success
            JAASUserPrincipal userPrincipal = new JAASUserPrincipal(username);
            userPrincipal.setSubject(loginContext.getSubject());
            userPrincipal.setRoleCheckPolicy (roleCheckPolicy);
            
            synchronized (this)
            {
                userMap.put (username, new UserInfo (username, userPrincipal, loginContext));
            }
            
            return userPrincipal;       
        }
        catch (Exception e)
        {
            log.warn(e);
            return null;
        }     
    }

    

    /* ---------------------------------------------------- */
    /**
     * Removes any auth info associated with eg. the thread.
     *
     * @param user a UserPrincipal to disassociate
     */
    public void disassociate(Principal user)
    {
        if (user != null)
            ((JAASUserPrincipal)user).disassociate();
    }

    

    /* ---------------------------------------------------- */
    /**
     * Temporarily adds a role to a user.
     *
     * Temporarily granting a role pushes the role onto a stack
     * of temporary roles. Temporary roles must therefore be
     * removed in order.
     *
     * @param user the Principal to which to add the role
     * @param role the role name
     * @return the Principal with the role added
     */
    public Principal pushRole(Principal user, String role)
    {
        ((JAASUserPrincipal)user).pushRole(role);
        return user;
    }
    
    /* ------------------------------------------------------------ */
    public Principal popRole(Principal user)
    {
        ((JAASUserPrincipal)user).popRole();
        return user;
    }



    /* ---------------------------------------------------- */
    /**
     * Logout a previously logged in user.
     * This can only work for FORM authentication
     * as BasicAuthentication is stateless.
     * 
     * The user's LoginContext logout() method is called.
     * @param user an <code>Principal</code> value
     */
    public void logout(Principal user)
    {
        try
        {
            if (!(user instanceof JAASUserPrincipal))
                throw new IllegalArgumentException (user + " is not a JAASUserPrincipal");
            
            String key = ((JAASUserPrincipal)user).getName();
            
            UserInfo info = null;
            synchronized (this)
            {
                info = (UserInfo)userMap.get(key);
            }
            
            if (info == null)
                log.warn ("Logout called for user="+user+" who is NOT in the authentication cache");
            else 
                info.getLoginContext().logout();
            
            synchronized (this)
            {
                userMap.remove (key);
            }
            log.debug (user+" has been LOGGED OUT");
        }
        catch (LoginException e)
        {
            log.warn (e);
        }
    }
    
}
