/*
  $Id: LoginServlet.java 326 2009-07-15 20:58:45Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 326 $
  Updated: $Date: 2009-07-15 13:58:45 -0700 (Wed, 15 Jul 2009) $
*/
package edu.vt.middleware.ldap.servlets;

import java.io.IOException;
import java.net.URLEncoder;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import edu.vt.middleware.ldap.Authenticator;
import edu.vt.middleware.ldap.props.LdapProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <code>LoginServet</code> attempts to authenticate a user against a LDAP. The
 * following init params can be set for this servlet:
 * edu.vt.middleware.ldap.servlets.propertiesFile - to load authenticator
 * properties from edu.vt.middleware.ldap.servlets.sessionId - to set the user
 * identifier in the session edu.vt.middleware.ldap.servlets.loginUrl - to set
 * the URL of your login page edu.vt.middleware.ldap.servlets.errorMsg - to
 * display if authentication fails
 * edu.vt.middleware.ldap.servlets.sessionManager - optional class to perform
 * session management after login and logout (must extend
 * edu.vt.middleware.ldap.servlets.session.SessionManager)
 *
 * <p>The following http params can be sent to this servlet: user - user
 * identifier to authenticate credential - user credential to authenticate with
 * url - to redirect client to after successful authentication</p>
 *
 * @author  Middleware Services
 * @version  $Revision: 326 $ $Date: 2009-07-15 13:58:45 -0700 (Wed, 15 Jul 2009) $
 */
public final class LoginServlet extends CommonServlet
{

  /** serial version uid. */
  private static final long serialVersionUID = -1987565072388102546L;

  /** Log for this class. */
  private static final Log LOG = LogFactory.getLog(LoginServlet.class);

  /** URL of the page that does collects user credentials. */
  private String loginUrl;

  /** Message to display if authentication fails. */
  private String errorMsg;

  /** Used to authenticate against a LDAP. */
  private Authenticator auth;


  /**
   * Initialize this servlet.
   *
   * @param  config  <code>ServletConfig</code>
   *
   * @throws  ServletException  if an error occurs
   */
  public void init(final ServletConfig config)
    throws ServletException
  {
    super.init(config);
    this.loginUrl = getInitParameter(ServletConstants.LOGIN_URL);
    if (this.loginUrl == null) {
      this.loginUrl = ServletConstants.DEFAULT_LOGIN_URL;
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug(ServletConstants.LOGIN_URL + " = " + this.loginUrl);
    }
    this.errorMsg = getInitParameter(ServletConstants.ERROR_MSG);
    if (this.errorMsg == null) {
      this.errorMsg = ServletConstants.DEFAULT_ERROR_MSG;
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug(ServletConstants.ERROR_MSG + " = " + this.errorMsg);
    }

    String propertiesFile = getInitParameter(ServletConstants.PROPERTIES_FILE);
    if (propertiesFile == null) {
      propertiesFile = LdapProperties.PROPERTIES_FILE;
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug(ServletConstants.PROPERTIES_FILE + " = " + propertiesFile);
    }
    this.auth = new Authenticator();
    this.auth.loadFromProperties(
      LoginServlet.class.getResourceAsStream(propertiesFile));
  }


  /**
   * Handle all requests sent to this servlet.
   *
   * @param  request  <code>HttpServletRequest</code>
   * @param  response  <code>HttpServletResponse</code>
   *
   * @throws  ServletException  if this request cannot be serviced
   * @throws  IOException  if a response cannot be sent
   */
  public void service(
    final HttpServletRequest request,
    final HttpServletResponse response)
    throws ServletException, IOException
  {
    boolean validCredentials = false;
    String user = request.getParameter(ServletConstants.USER_PARAM);
    if (user != null) {
      user = user.trim().toLowerCase();
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug("Received user param = " + user);
    }

    final String credential = request.getParameter(
      ServletConstants.CREDENTIAL_PARAM);
    String url = request.getParameter(ServletConstants.URL_PARAM);
    if (url == null) {
      url = "";
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug("Received url param = " + url);
    }

    final StringBuffer error = new StringBuffer(this.errorMsg);

    try {
      if (this.auth.authenticate(user, credential)) {
        validCredentials = true;
      }
    } catch (Exception e) {
      if (LOG.isErrorEnabled()) {
        LOG.error("Error authenticating user " + user, e);
      }
      if (
        e.getCause() != null &&
          e.getCause().getMessage() != null &&
          !e.getCause().getMessage().equals("null")) {
        error.append(": ").append(e.getCause().getMessage());
      } else if (e.getMessage() != null && !e.getMessage().equals("null")) {
        error.append(": ").append(e.getMessage());
      }
    }

    if (validCredentials) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Authentication succeeded for user " + user);
      }
      try {
        this.sessionManager.login(request.getSession(true), user);
        if (LOG.isDebugEnabled()) {
          LOG.debug("Initialized session for user " + user);
        }
        response.sendRedirect(url);
        if (LOG.isDebugEnabled()) {
          LOG.debug("Redirected user to " + url);
        }
        return;
      } catch (Exception e) {
        if (LOG.isErrorEnabled()) {
          LOG.error("Error authorizing user " + user, e);
        }
        if (
          e.getCause() != null &&
            e.getCause().getMessage() != null &&
            !e.getCause().getMessage().equals("null")) {
          error.append(": ").append(e.getCause().getMessage());
        } else if (e.getMessage() != null && !e.getMessage().equals("null")) {
          error.append(": ").append(e.getMessage());
        }
      }
    }

    final StringBuffer errorUrl = new StringBuffer(this.loginUrl);
    if (error != null) {
      errorUrl.append("?error=").append(
        URLEncoder.encode(error.toString(), "UTF-8"));
    }
    if (user != null) {
      errorUrl.append("&user=").append(URLEncoder.encode(user, "UTF-8"));
    }
    if (url != null) {
      errorUrl.append("&url=").append(URLEncoder.encode(url, "UTF-8"));
    }
    response.sendRedirect(errorUrl.toString());
    if (LOG.isDebugEnabled()) {
      LOG.debug("Redirected user to " + errorUrl.toString());
    }
  }


  /**
   * Called by the servlet container to indicate to a servlet that the servlet
   * is being taken out of service.
   */
  public void destroy()
  {
    super.destroy();
  }
}
/*
  $Id: LoginServlet.java 269 2009-05-28 14:24:37Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 269 $
  Updated: $Date: 2009-05-28 07:24:37 -0700 (Thu, 28 May 2009) $
*/
package edu.vt.middleware.ldap.servlets;

import java.io.IOException;
import java.net.URLEncoder;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import edu.vt.middleware.ldap.Authenticator;
import edu.vt.middleware.ldap.props.LdapProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <code>LoginServet</code> attempts to authenticate a user against a LDAP. The
 * following init params can be set for this servlet:
 * edu.vt.middleware.ldap.servlets.propertiesFile - to load authenticator
 * properties from edu.vt.middleware.ldap.servlets.sessionId - to set the user
 * identifier in the session edu.vt.middleware.ldap.servlets.loginUrl - to set
 * the URL of your login page edu.vt.middleware.ldap.servlets.errorMsg - to
 * display if authentication fails
 * edu.vt.middleware.ldap.servlets.sessionManager - optional class to perform
 * session management after login and logout (must extend
 * edu.vt.middleware.ldap.servlets.session.SessionManager)
 *
 * <p>The following http params can be sent to this servlet: user - user
 * identifier to authenticate credential - user credential to authenticate with
 * url - to redirect client to after successful authentication</p>
 *
 * @author  Middleware Services
 * @version  $Revision: 269 $ $Date: 2009-05-28 07:24:37 -0700 (Thu, 28 May 2009) $
 */
public final class LoginServlet extends CommonServlet
{

  /** serial version uid. */
  private static final long serialVersionUID = -1987565072388102546L;

  /** Log for this class. */
  private static final Log LOG = LogFactory.getLog(LoginServlet.class);

  /** URL of the page that does collects user credentials. */
  private String loginUrl;

  /** Message to display if authentication fails. */
  private String errorMsg;

  /** Used to authenticate against a LDAP. */
  private Authenticator auth;


  /**
   * Initialize this servlet.
   *
   * @param  config  <code>ServletConfig</code>
   *
   * @throws  ServletException  if an error occurs
   */
  public void init(final ServletConfig config)
    throws ServletException
  {
    super.init(config);
    this.loginUrl = getInitParameter(ServletConstants.LOGIN_URL);
    if (this.loginUrl == null) {
      this.loginUrl = ServletConstants.DEFAULT_LOGIN_URL;
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug(ServletConstants.LOGIN_URL + " = " + this.loginUrl);
    }
    this.errorMsg = getInitParameter(ServletConstants.ERROR_MSG);
    if (this.errorMsg == null) {
      this.errorMsg = ServletConstants.DEFAULT_ERROR_MSG;
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug(ServletConstants.ERROR_MSG + " = " + this.errorMsg);
    }

    String propertiesFile = getInitParameter(ServletConstants.PROPERTIES_FILE);
    if (propertiesFile == null) {
      propertiesFile = LdapProperties.PROPERTIES_FILE;
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug(ServletConstants.PROPERTIES_FILE + " = " + propertiesFile);
    }
    this.auth = new Authenticator();
    this.auth.loadFromProperties(propertiesFile);
  }


  /**
   * Handle all requests sent to this servlet.
   *
   * @param  request  <code>HttpServletRequest</code>
   * @param  response  <code>HttpServletResponse</code>
   *
   * @throws  ServletException  if this request cannot be serviced
   * @throws  IOException  if a response cannot be sent
   */
  public void service(
    final HttpServletRequest request,
    final HttpServletResponse response)
    throws ServletException, IOException
  {
    boolean validCredentials = false;
    String user = request.getParameter(ServletConstants.USER_PARAM);
    if (user != null) {
      user = user.trim().toLowerCase();
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug("Received user param = " + user);
    }

    final String credential = request.getParameter(
      ServletConstants.CREDENTIAL_PARAM);
    String url = request.getParameter(ServletConstants.URL_PARAM);
    if (url == null) {
      url = "";
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug("Received url param = " + url);
    }

    final StringBuffer error = new StringBuffer(this.errorMsg);

    try {
      if (this.auth.authenticate(user, credential)) {
        validCredentials = true;
      }
    } catch (Exception e) {
      if (LOG.isErrorEnabled()) {
        LOG.error("Error authenticating user " + user, e);
      }
      if (
        e.getCause() != null &&
          e.getCause().getMessage() != null &&
          !e.getCause().getMessage().equals("null")) {
        error.append(": ").append(e.getCause().getMessage());
      } else if (e.getMessage() != null && !e.getMessage().equals("null")) {
        error.append(": ").append(e.getMessage());
      }
    }

    if (validCredentials) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Authentication succeeded for user " + user);
      }
      try {
        this.sessionManager.login(request.getSession(true), user);
        if (LOG.isDebugEnabled()) {
          LOG.debug("Initialized session for user " + user);
        }
        response.sendRedirect(url);
        if (LOG.isDebugEnabled()) {
          LOG.debug("Redirected user to " + url);
        }
        return;
      } catch (Exception e) {
        if (LOG.isErrorEnabled()) {
          LOG.error("Error authorizing user " + user, e);
        }
        if (
          e.getCause() != null &&
            e.getCause().getMessage() != null &&
            !e.getCause().getMessage().equals("null")) {
          error.append(": ").append(e.getCause().getMessage());
        } else if (e.getMessage() != null && !e.getMessage().equals("null")) {
          error.append(": ").append(e.getMessage());
        }
      }
    }

    final StringBuffer errorUrl = new StringBuffer(this.loginUrl);
    if (error != null) {
      errorUrl.append("?error=").append(
        URLEncoder.encode(error.toString(), "UTF-8"));
    }
    if (user != null) {
      errorUrl.append("&user=").append(URLEncoder.encode(user, "UTF-8"));
    }
    if (url != null) {
      errorUrl.append("&url=").append(URLEncoder.encode(url, "UTF-8"));
    }
    response.sendRedirect(errorUrl.toString());
    if (LOG.isDebugEnabled()) {
      LOG.debug("Redirected user to " + errorUrl.toString());
    }
  }


  /**
   * Called by the servlet container to indicate to a servlet that the servlet
   * is being taken out of service.
   */
  public void destroy()
  {
    super.destroy();
  }
}
/*
  $Id: LoginServlet.java 326 2009-07-15 20:58:45Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 326 $
  Updated: $Date: 2009-07-15 13:58:45 -0700 (Wed, 15 Jul 2009) $
*/
package edu.vt.middleware.ldap.servlets;

import java.io.IOException;
import java.net.URLEncoder;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import edu.vt.middleware.ldap.Authenticator;
import edu.vt.middleware.ldap.props.LdapProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <code>LoginServet</code> attempts to authenticate a user against a LDAP. The
 * following init params can be set for this servlet:
 * edu.vt.middleware.ldap.servlets.propertiesFile - to load authenticator
 * properties from edu.vt.middleware.ldap.servlets.sessionId - to set the user
 * identifier in the session edu.vt.middleware.ldap.servlets.loginUrl - to set
 * the URL of your login page edu.vt.middleware.ldap.servlets.errorMsg - to
 * display if authentication fails
 * edu.vt.middleware.ldap.servlets.sessionManager - optional class to perform
 * session management after login and logout (must extend
 * edu.vt.middleware.ldap.servlets.session.SessionManager)
 *
 * <p>The following http params can be sent to this servlet: user - user
 * identifier to authenticate credential - user credential to authenticate with
 * url - to redirect client to after successful authentication</p>
 *
 * @author  Middleware Services
 * @version  $Revision: 326 $ $Date: 2009-07-15 13:58:45 -0700 (Wed, 15 Jul 2009) $
 */
public final class LoginServlet extends CommonServlet
{

  /** serial version uid. */
  private static final long serialVersionUID = -1987565072388102546L;

  /** Log for this class. */
  private static final Log LOG = LogFactory.getLog(LoginServlet.class);

  /** URL of the page that does collects user credentials. */
  private String loginUrl;

  /** Message to display if authentication fails. */
  private String errorMsg;

  /** Used to authenticate against a LDAP. */
  private Authenticator auth;


  /**
   * Initialize this servlet.
   *
   * @param  config  <code>ServletConfig</code>
   *
   * @throws  ServletException  if an error occurs
   */
  public void init(final ServletConfig config)
    throws ServletException
  {
    super.init(config);
    this.loginUrl = getInitParameter(ServletConstants.LOGIN_URL);
    if (this.loginUrl == null) {
      this.loginUrl = ServletConstants.DEFAULT_LOGIN_URL;
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug(ServletConstants.LOGIN_URL + " = " + this.loginUrl);
    }
    this.errorMsg = getInitParameter(ServletConstants.ERROR_MSG);
    if (this.errorMsg == null) {
      this.errorMsg = ServletConstants.DEFAULT_ERROR_MSG;
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug(ServletConstants.ERROR_MSG + " = " + this.errorMsg);
    }

    String propertiesFile = getInitParameter(ServletConstants.PROPERTIES_FILE);
    if (propertiesFile == null) {
      propertiesFile = LdapProperties.PROPERTIES_FILE;
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug(ServletConstants.PROPERTIES_FILE + " = " + propertiesFile);
    }
    this.auth = new Authenticator();
    this.auth.loadFromProperties(
      LoginServlet.class.getResourceAsStream(propertiesFile));
  }


  /**
   * Handle all requests sent to this servlet.
   *
   * @param  request  <code>HttpServletRequest</code>
   * @param  response  <code>HttpServletResponse</code>
   *
   * @throws  ServletException  if this request cannot be serviced
   * @throws  IOException  if a response cannot be sent
   */
  public void service(
    final HttpServletRequest request,
    final HttpServletResponse response)
    throws ServletException, IOException
  {
    boolean validCredentials = false;
    String user = request.getParameter(ServletConstants.USER_PARAM);
    if (user != null) {
      user = user.trim().toLowerCase();
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug("Received user param = " + user);
    }

    final String credential = request.getParameter(
      ServletConstants.CREDENTIAL_PARAM);
    String url = request.getParameter(ServletConstants.URL_PARAM);
    if (url == null) {
      url = "";
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug("Received url param = " + url);
    }

    final StringBuffer error = new StringBuffer(this.errorMsg);

    try {
      if (this.auth.authenticate(user, credential)) {
        validCredentials = true;
      }
    } catch (Exception e) {
      if (LOG.isErrorEnabled()) {
        LOG.error("Error authenticating user " + user, e);
      }
      if (
        e.getCause() != null &&
          e.getCause().getMessage() != null &&
          !e.getCause().getMessage().equals("null")) {
        error.append(": ").append(e.getCause().getMessage());
      } else if (e.getMessage() != null && !e.getMessage().equals("null")) {
        error.append(": ").append(e.getMessage());
      }
    }

    if (validCredentials) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Authentication succeeded for user " + user);
      }
      try {
        this.sessionManager.login(request.getSession(true), user);
        if (LOG.isDebugEnabled()) {
          LOG.debug("Initialized session for user " + user);
        }
        response.sendRedirect(url);
        if (LOG.isDebugEnabled()) {
          LOG.debug("Redirected user to " + url);
        }
        return;
      } catch (Exception e) {
        if (LOG.isErrorEnabled()) {
          LOG.error("Error authorizing user " + user, e);
        }
        if (
          e.getCause() != null &&
            e.getCause().getMessage() != null &&
            !e.getCause().getMessage().equals("null")) {
          error.append(": ").append(e.getCause().getMessage());
        } else if (e.getMessage() != null && !e.getMessage().equals("null")) {
          error.append(": ").append(e.getMessage());
        }
      }
    }

    final StringBuffer errorUrl = new StringBuffer(this.loginUrl);
    if (error != null) {
      errorUrl.append("?error=").append(
        URLEncoder.encode(error.toString(), "UTF-8"));
    }
    if (user != null) {
      errorUrl.append("&user=").append(URLEncoder.encode(user, "UTF-8"));
    }
    if (url != null) {
      errorUrl.append("&url=").append(URLEncoder.encode(url, "UTF-8"));
    }
    response.sendRedirect(errorUrl.toString());
    if (LOG.isDebugEnabled()) {
      LOG.debug("Redirected user to " + errorUrl.toString());
    }
  }


  /**
   * Called by the servlet container to indicate to a servlet that the servlet
   * is being taken out of service.
   */
  public void destroy()
  {
    super.destroy();
  }
}
