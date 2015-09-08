/*
  $Id: RemoteUserAuthenticator.java 454 2009-08-19 20:19:21Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 454 $
  Updated: $Date: 2009-08-19 13:19:21 -0700 (Wed, 19 Aug 2009) $
*/
package edu.vt.middleware.catalina.authenticator;

import java.io.IOException;
import java.security.Principal;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Session;
import org.apache.catalina.authenticator.AuthenticatorBase;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.deploy.LoginConfig;

/**
 * An <b>Authenticator</b> and <b>Valve</b> implementation of authentication
 * that utilizes the REMOTE_USER header to invoke a realm.
 * Authentication is implied by the existence of the REMOTE_USER.
 * Any realm invoked by this authenticator must be prepared to accept a null password.
 *
 * @author  Middleware Services
 * @version  $Revision: 454 $
 */
public class RemoteUserAuthenticator extends AuthenticatorBase {

    public static final String REMOTE_USER_METHOD = "REMOTE-USER";

    // ------------------------------------------------------------- Properties

    /**
     * Descriptive information about this implementation.
     */
    protected static final String info =
        "edu.vt.middleware.catalina.authenticator.RemoteUserAuthenticator/1.0";


    /**
     * Return descriptive information about this Valve implementation.
     */
    public String getInfo() {
        return (info);
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Authenticate the user by checking for the existence of a principal.
     * If found, the principal name is passed to realm authenticate with a null password.
     *
     * @param request Request we are processing
     * @param response Response we are creating
     * @param config    Login configuration describing how authentication
     *              should be performed
     *
     * @exception IOException if an input/output error occurs
     */
    public boolean authenticate(Request request,
                                Response response,
                                LoginConfig config)
        throws IOException {

        // Confirm the user has been authenticated
        Principal principal = request.getUserPrincipal();

        if (principal == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                               "Principal not found");
            return (false);
        }

        // See if we have already authenticated this principal
        Session session = request.getSessionInternal(true);
        Principal sessionPrincipal = session.getPrincipal();
        if (sessionPrincipal != null) {
            if (REMOTE_USER_METHOD.equals(session.getAuthType()) &&
                sessionPrincipal.getName().equals(principal.getName())) {
                request.setAuthType(REMOTE_USER_METHOD);
                request.setUserPrincipal(sessionPrincipal);
                return (true);
            }
        }

        // Re-authenticate the principal
        Principal newPrincipal = context.getRealm().authenticate(
            principal.getName(), (String) null);
        if (newPrincipal == null) {
            response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Cannot authenticate with the provided credentials");
            return (false);
        }

        // Cache the new principal (if requested) and record this authentication
        register(request, response, newPrincipal, REMOTE_USER_METHOD,
                 principal.getName(), null);
        return (true);
    }

    // ------------------------------------------------------ Lifecycle Methods

    /**
     * Initialize the database we will be using for client verification
     * and certificate validation (if any).
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    public void start() throws LifecycleException {
        super.start();
    }


    /**
     * Finalize the database we used for client verification and
     * certificate validation (if any).
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    public void stop() throws LifecycleException {
        super.stop();
    }
}
/*
  $Id: RemoteUserAuthenticator.java 454 2009-08-19 20:19:21Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 454 $
  Updated: $Date: 2009-08-19 13:19:21 -0700 (Wed, 19 Aug 2009) $
*/
package edu.vt.middleware.catalina.authenticator;

import java.io.IOException;
import java.security.Principal;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Session;
import org.apache.catalina.authenticator.AuthenticatorBase;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.deploy.LoginConfig;

/**
 * An <b>Authenticator</b> and <b>Valve</b> implementation of authentication
 * that utilizes the REMOTE_USER header to invoke a realm.
 * Authentication is implied by the existence of the REMOTE_USER.
 * Any realm invoked by this authenticator must be prepared to accept a null password.
 *
 * @author  Middleware Services
 * @version  $Revision: 454 $
 */
public class RemoteUserAuthenticator extends AuthenticatorBase {

    public static final String REMOTE_USER_METHOD = "REMOTE-USER";

    // ------------------------------------------------------------- Properties

    /**
     * Descriptive information about this implementation.
     */
    protected static final String info =
        "edu.vt.middleware.catalina.authenticator.RemoteUserAuthenticator/1.0";


    /**
     * Return descriptive information about this Valve implementation.
     */
    public String getInfo() {
        return (info);
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Authenticate the user by checking for the existence of a principal.
     * If found, the principal name is passed to realm authenticate with a null password.
     *
     * @param request Request we are processing
     * @param response Response we are creating
     * @param config    Login configuration describing how authentication
     *              should be performed
     *
     * @exception IOException if an input/output error occurs
     */
    public boolean authenticate(Request request,
                                Response response,
                                LoginConfig config)
        throws IOException {

        // Confirm the user has been authenticated
        Principal principal = request.getUserPrincipal();

        if (principal == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                               "Principal not found");
            return (false);
        }

        // See if we have already authenticated this principal
        Session session = request.getSessionInternal(true);
        Principal sessionPrincipal = session.getPrincipal();
        if (sessionPrincipal != null) {
            if (REMOTE_USER_METHOD.equals(session.getAuthType()) &&
                sessionPrincipal.getName().equals(principal.getName())) {
                request.setAuthType(REMOTE_USER_METHOD);
                request.setUserPrincipal(sessionPrincipal);
                return (true);
            }
        }

        // Re-authenticate the principal
        Principal newPrincipal = context.getRealm().authenticate(
            principal.getName(), (String) null);
        if (newPrincipal == null) {
            response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Cannot authenticate with the provided credentials");
            return (false);
        }

        // Cache the new principal (if requested) and record this authentication
        register(request, response, newPrincipal, REMOTE_USER_METHOD,
                 principal.getName(), null);
        return (true);
    }

    // ------------------------------------------------------ Lifecycle Methods

    /**
     * Initialize the database we will be using for client verification
     * and certificate validation (if any).
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    public void start() throws LifecycleException {
        super.start();
    }


    /**
     * Finalize the database we used for client verification and
     * certificate validation (if any).
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    public void stop() throws LifecycleException {
        super.stop();
    }
}
