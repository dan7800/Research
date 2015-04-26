/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package org.apache.directory.triplesec.adminui.security;

import wicket.protocol.http.WebApplication;
import wicket.protocol.http.WebSession;

import org.apache.directory.triplesec.adminui.models.ConnectionInfo;
import org.apache.directory.triplesec.adminui.services.ConnectionManager;

/**
 * Basic authenticated web session.  Subclasses must provide an implementation
 * of the <code>authenticate</code> method based on connection and credential
 * information contained within an instance of the
 * {@link org.apache.directory.triplesec.adminui.models.ConnectionInfo} bean.
 */
public class AuthenticatedWebSession extends WebSession
{
    private static final long serialVersionUID = 1L;
    /**
     * Manages an authenticated connection to the Triplesec server.
     */
    private ConnectionManager connection = null;


    /**
     * Construct the authenticated web session.
     *
     * @param application the web application
     */
    public AuthenticatedWebSession( final WebApplication application ) {
        super( application );
    }


    /**
     * Authenticates this session to the Triplesec server using the given
     * {@link org.apache.directory.triplesec.adminui.models.ConnectionInfo} properties.
     *
     * @param info the <code>ConnectionInfo</code> properties
     * @return <code>true</code> if the user was authenticated successfully
     */
    public boolean authenticate( ConnectionInfo info ) {

        // if the user is (somehow) already signed in, then wave 'em on by...
        if ( isAuthenticated() )
        {
            return true;
        }

        // create a session-level instance of the Triplesec server connection manager
        connection = new ConnectionManager();

        // TODO -- delete this once the login dialogs are completed...
        if ( info == null ) {
            info = new ConnectionInfo();
            info.setCredentials( "secret" );
            info.setHost( "localhost" );
            info.setKrb5Port( 88 );
            info.setLdapPort( 10389 );
            info.setPrincipal( "admin" );
            info.setRealm( "EXAMPLE.COM" );
            info.setUseLdaps( false );
        }

        // return the results of our attempt to connect to the Triplesec server
        return connection.connect( info );
    }


    /**
     * @return <code>true</code> if the user is signed in to this session
     */
    public boolean isAuthenticated()
    {
        return connection != null;
    }


    public Throwable getAuthenticationError()
    {
        if ( connection == null )
        {
            throw new IllegalStateException(
                    "Instance of " + ConnectionManager.class.getName() + " not yet instantiated"
            );
        }
        return connection.getLastFailure();
    }

    /**
     * Invalidate the session by disconnecting from the Triplesec server.
     */
    public void invalidate() {
        if ( connection != null )
        {
            connection.disconnect();
            connection = null;
        }
        super.invalidate();
    }


    /**
     * Returns the Triplesec server admin connection manager.
     */
    public ConnectionManager getConnection()
    {
        if ( connection == null )
        {
            throw new IllegalStateException(
                    "Instance of " + ConnectionManager.class.getName() + " not yet instantiated"
            );
        }
        return connection;
    }
}
/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package org.apache.directory.triplesec.demo.security;

import org.apache.directory.triplesec.demo.service.Registry;
import org.apache.directory.triplesec.guardian.Profile;
import org.apache.directory.triplesec.jaas.AccountLockedOutException;
import org.apache.directory.triplesec.jaas.PreauthFailedException;
import org.apache.directory.triplesec.jaas.ResynchInProgressException;
import org.apache.directory.triplesec.jaas.ResynchStartingException;
import org.apache.directory.triplesec.jaas.SafehausPrincipal;
import wicket.protocol.http.WebApplication;
import wicket.protocol.http.WebSession;
import wicket.util.string.Strings;

import javax.security.auth.login.LoginException;

/**
 * Basic authenticated web session.
 */
public class AuthenticatedWebSession extends WebSession
{
    private static final long serialVersionUID = -6787285847550864854L;

    private String errorMessage = null;
    private String username;
    private String password;
    private SafehausPrincipal principal = null;

    /**
     * Construct the authenticated web session.
     *
     * @param application the web application
     */
    public AuthenticatedWebSession(final WebApplication application)
    {
        super( application );
    }


    /**
     * Authenticates this session.
     *
     * @param username the username
     * @param password the password
     * @param passcode the passcode
     * @return <b>true</b> if the user was authenticated successfully
     */
    public boolean authenticate( String username, String password, String passcode )
    {
        this.username = username;
        if ( Strings.isEmpty( username ) )
        {
            errorMessage = "Missing credentials: Username not provided.";
            principal = null;
            return isAuthenticated();
        }
        this.password = password;
        if ( Strings.isEmpty( password ) )
        {
            errorMessage = "Missing credentials: Password not provided.";
            principal = null;
            return isAuthenticated();
        }

        try
        {
            principal = Registry.policyManager().getPrincipal( username,
                    password, passcode );
        }
        catch ( AccountLockedOutException e )
        {
            errorMessage = "Account locked for user '" + username + "'!";
        }
        catch ( PreauthFailedException e )
        {
            errorMessage = "Hotp authentication failed for user '" + username + "'!";
        }
        catch ( ResynchInProgressException e )
        {
            errorMessage = "User '" + username + "' is still out of sych! Please " +
                    "enter another consecutive single-use password.";
        }
        catch ( ResynchStartingException e )
        {
            errorMessage = "User '" + username + "' is out of synch! Initiating " +
                    "resynch protocol. Enter another consecutive single-use " +
                    "password.";
        }
        catch ( LoginException e )
        {
            errorMessage = "User '" + username + "' failed authentication: " +
                    e.getMessage();
        }
        return isAuthenticated();
    }


    public boolean isAuthenticated()
    {
        return principal != null;
    }


    public String getErrorMessage()
    {
        return errorMessage;
    }


    public String getPassword()
    {
        return password;
    }


    public String getUsername()
    {
        return username;
    }


    public Profile getUserProfile()
    {
        return principal != null ? principal.getAuthorizationProfile() : null;
    }
}
