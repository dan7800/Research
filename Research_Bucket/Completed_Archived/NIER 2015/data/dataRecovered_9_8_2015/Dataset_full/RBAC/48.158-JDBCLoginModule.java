// ========================================================================
// $Id: JDBCLoginModule.java,v 1.3 2004/05/09 20:31:18 gregwilkins Exp $
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

package org.mortbay.jaas.spi;

import java.io.IOException;
import java.security.Principal;
import java.security.acl.Group;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jaas.JAASGroup;
import org.mortbay.jaas.JAASPrincipal;
import org.mortbay.jaas.JAASRole;
import org.mortbay.jaas.callback.ObjectCallback;
import org.mortbay.util.Credential;
import org.mortbay.util.Loader;



/* ---------------------------------------------------- */
/** JDBCLoginModule
 * <p>JAAS LoginModule to retrieve user information from
 *  a database and authenticate the user.
 *
 * <p><h4>Notes</h4>
 * <p>This version uses plain old JDBC connections NOT
 * Datasources.
 *
 * <p><h4>Usage</h4>
 * <pre>
 */
/*
 * </pre>
 *
 * @see 
 * @version 1.0 Tue Apr 15 2003
 * @author Jan Bartel (janb)
 */
public class JDBCLoginModule implements LoginModule
{
    private static Log log = LogFactory.getLog(JDBCLoginModule.class);

    private CallbackHandler callbackHandler = null;
 
    private boolean authState = false;
    private boolean commitState = false;
    
    private Subject subject = null;
    private Principal principal = null;
    private Credential credential = null;
    private Group roleGroup = null;
    private String dbCredential = null;
    
    private String dbDriver;
    private String dbUrl;
    private String dbUserName;
    private String dbPassword;
    private String userQuery;
    private String rolesQuery;
    
    
    /* ------------------------------------------------ */
    /** Abort login
     * @return 
     * @exception LoginException 
     */
    public boolean abort()
        throws LoginException
    {
        principal = null;
        credential = null;
        roleGroup = null;
        dbCredential = null;
        
        if (authState && commitState)
            return true;
        else
            return false;
    }

    /* ------------------------------------------------ */
    /** Commit the authenticated user
     * @return 
     * @exception LoginException 
     */
    public boolean commit()
        throws LoginException
    {
        if (!authState)
        {
            principal = null;
            credential = null;
            roleGroup = null;
            commitState = false;
            dbCredential = null;
            return authState;
        }

        subject.getPrincipals().add(principal);
        subject.getPrivateCredentials().add(credential);
        subject.getPrincipals().add(roleGroup);
        commitState = true;
        dbCredential = null;
        
        return true;
    }


    /* ------------------------------------------------ */
    /** Authenticate the user.
     * @return 
     * @exception LoginException 
     */
    public boolean login()
        throws LoginException
    {
        if (callbackHandler == null)
            throw new LoginException ("No callback handler");
        Callback[] callbacks = new Callback[2];
        callbacks[0] = new NameCallback("Enter user name");
        callbacks[1] = new ObjectCallback();

        try
        {
            callbackHandler.handle(callbacks);

            String webUserName = ((NameCallback)callbacks[0]).getName();
            Object webCredential = ((ObjectCallback)callbacks[1]).getObject();

            if (webUserName == null)
            {
                authState = false;
                return authState;
            }
            
   
            if (webCredential == null)
            {
                authState = false;
                return authState;
            }

            //load user from database
            loadUser (webUserName);

            principal = new JAASPrincipal(webUserName);
            
            if (dbCredential == null)
            {
                authState = false;
                return authState;
            }
            
            //convert db info into Credential object that can be compared
            credential = Credential.getCredential (dbCredential);
            
            //compare it to the info presented to the web
            authState =  credential.check(webCredential);
            return authState;
        }
        catch (IOException e)
        {
            throw new LoginException (e.toString());
        }
        catch (UnsupportedCallbackException e)
        {
            throw new LoginException (e.toString());
        }
        catch (SQLException e)
        {
            throw new LoginException (e.toString());
        }
        
    }

    
    /* ------------------------------------------------ */
    /** Logout authenticated user
     * @return 
     * @exception LoginException 
     */
    public boolean logout()
        throws LoginException
    {
        subject.getPrincipals().remove(principal);
        subject.getPrivateCredentials().remove(credential);
        subject.getPrincipals().remove(roleGroup);

        return true;
    }

    
    /* ------------------------------------------------ */
    /** Init LoginModule.
     * Called once by JAAS after new instance created.
     * @param subject 
     * @param callbackHandler 
     * @param sharedState 
     * @param options 
     */
    public void initialize(Subject subject,
                           CallbackHandler callbackHandler,
                           Map sharedState,
                           Map options)
    {
        try
        {
            
            //get the jdbc  username/password, jdbc url out of the options
            dbDriver = (String)options.get("dbDriver");
            dbUrl = (String)options.get("dbUrl");
            dbUserName = (String)options.get("dbUserName");
            dbPassword = (String)options.get("dbPassword");

            if (dbUserName == null)
                dbUserName = "";

            if (dbPassword == null)
                dbPassword = "";
            
            if (dbDriver != null)
                Loader.loadClass(this.getClass(), dbDriver).newInstance();
            
            //get the user credential query out of the options
            String dbUserTable = (String)options.get("userTable");
            String dbUserTableUserField = (String)options.get("userField");
            String dbUserTableCredentialField = (String)options.get("credentialField");
            
            userQuery = "select "+dbUserTableCredentialField+" from "+dbUserTable+" where "+dbUserTableUserField+"=?";
            
            
            //get the user roles query out of the options
            String dbUserRoleTable = (String)options.get("userRoleTable");
            String dbUserRoleTableUserField = (String)options.get("userRoleUserField");
            String dbUserRoleTableRoleField = (String)options.get("userRoleRoleField");
            
            rolesQuery = "select "+dbUserRoleTableRoleField+" from "+dbUserRoleTable+" where "+dbUserRoleTableUserField+"=?";
            
            if(log.isDebugEnabled())log.debug("userQuery = "+userQuery);
            if(log.isDebugEnabled())log.debug("rolesQuery = "+rolesQuery);
            
            this.subject = subject;
            this.callbackHandler = callbackHandler;
        }
        catch (ClassNotFoundException e)
        {
            throw new IllegalStateException (e.toString());
        }
        catch (InstantiationException e)
        {
            throw new IllegalStateException (e.toString());
        }
        catch (IllegalAccessException e)
        {
            throw new IllegalStateException (e.toString());
        }
    }


    

    /* ------------------------------------------------ */
    /** Load info from database
     * @param userName user info to load
     * @exception SQLException 
     */
    public void loadUser (String userName)
        throws SQLException
    {
        //connect to database
        Connection connection = null;
        
        try
        {
            if (!((dbDriver != null)
                  &&
                  (dbUrl != null)))
                throw new IllegalStateException ("Database connection information not configured");

            if(log.isDebugEnabled())log.debug("Connecting using dbDriver="+dbDriver+"+ dbUserName="+dbUserName+", dbPassword="+dbUrl);
            
            connection = DriverManager.getConnection (dbUrl,
                                                      dbUserName,
                                                      dbPassword);
            
            //query for credential
            PreparedStatement statement = connection.prepareStatement (userQuery);
            statement.setString (1, userName);
            ResultSet results = statement.executeQuery();
            if (results.next())
            {
                dbCredential = results.getString(1);
            }
            results.close();
            statement.close();
            
            //query for role names
            statement = connection.prepareStatement (rolesQuery);
            statement.setString (1, userName);
            results = statement.executeQuery();
            
            roleGroup = new JAASGroup(JAASGroup.ROLES);
            while (results.next())
            {
                String roleName = results.getString (1);
                roleGroup.addMember (new JAASRole(roleName));
            }
            results.close();
            statement.close();
        }
        finally
        {
            connection.close();
        }
    }
    
    
    
}
