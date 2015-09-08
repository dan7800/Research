/*
  $Id: ServletConstants.java 302 2009-07-02 20:01:04Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 302 $
  Updated: $Date: 2009-07-02 13:01:04 -0700 (Thu, 02 Jul 2009) $
*/
package edu.vt.middleware.ldap.servlets;

/**
 * <code>ServletConstants</code> contains all the constants needed by the ldap
 * servlet package.
 *
 * @author  Middleware Services
 * @version  $Revision: 302 $ $Date: 2009-07-02 13:01:04 -0700 (Thu, 02 Jul 2009) $
 */
public final class ServletConstants
{

  /** Domain to look for properties in, value is {@value}. */
  public static final String PROPERTIES_DOMAIN =
    "edu.vt.middleware.ldap.servlets.";

  /** LDAP initialization properties file, value is {@value}. */
  public static final String PROPERTIES_FILE = PROPERTIES_DOMAIN +
    "propertiesFile";

  /** LDAP pool initialization properties file, value is {@value}. */
  public static final String POOL_PROPERTIES_FILE = PROPERTIES_DOMAIN +
    "poolPropertiesFile";

  /** Format of search output, value is {@value}. */
  public static final String OUTPUT_FORMAT = PROPERTIES_DOMAIN + "outputFormat";

  /** Default format of search output, value is {@value}. */
  public static final String DEFAULT_OUTPUT_FORMAT = "DSML";

  /** Type of pool used, value is {@value}. */
  public static final String POOL_TYPE = PROPERTIES_DOMAIN + "poolType";

  /**
   * Identifier to set in the session after valid authentication, value is
   * {@value}.
   */
  public static final String SESSION_ID = PROPERTIES_DOMAIN + "sessionId";

  /**
   * Default identifier to set in the session after valid authentication, value
   * is {@value}.
   */
  public static final String DEFAULT_SESSION_ID = "user";

  /** Whether to invalidate the user session at logout, value is {@value}. */
  public static final String INVALIDATE_SESSION = PROPERTIES_DOMAIN +
    "invalidateSession";

  /**
   * Default behavior for invalidating the user session at logout, value is
   * {@value}.
   */
  public static final String DEFAULT_INVALIDATE_SESSION = "true";

  /** URL of the page that collects user credentials, value is {@value}. */
  public static final String LOGIN_URL = PROPERTIES_DOMAIN + "loginUrl";

  /**
   * Default URL of the page that does collects user credentials, value is
   * {@value}.
   */
  public static final String DEFAULT_LOGIN_URL = "/";

  /** Error message to display if authentication fails, value is {@value}. */
  public static final String ERROR_MSG = PROPERTIES_DOMAIN + "errorMsg";

  /** Class used to initialize http sessions. */
  public static final String SESSION_MANAGER = PROPERTIES_DOMAIN +
    "sessionManager";

  /** Default session initializer, value is {@value}. */
  public static final String DEFAULT_SESSION_MANAGER =
    "edu.vt.middleware.ldap.servlets.session.DefaultSessionManager";

  /** Default error message, value is {@value}. */
  public static final String DEFAULT_ERROR_MSG =
    "Could not authenticate or authorize user";

  /** HTTP parameter used to transmit the user identifier, value is {@value}. */
  public static final String USER_PARAM = "user";

  /** HTTP parameter used to transmit the user credential, value is {@value}. */
  public static final String CREDENTIAL_PARAM = "credential";

  /** HTTP parameter used to transmit the redirect url, value is {@value}. */
  public static final String URL_PARAM = "url";


  /** Default constructor. */
  private ServletConstants() {}
}
/*
  $Id: ServletConstants.java 183 2009-05-06 02:45:57Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 183 $
  Updated: $Date: 2009-05-05 19:45:57 -0700 (Tue, 05 May 2009) $
*/
package edu.vt.middleware.ldap.servlets;

/**
 * <code>ServletConstants</code> contains all the constants needed by the ldap
 * servlet package.
 *
 * @author  Middleware Services
 * @version  $Revision: 183 $ $Date: 2009-05-05 19:45:57 -0700 (Tue, 05 May 2009) $
 */
public final class ServletConstants
{

  /** Domain to look for properties in, value is {@value}. */
  public static final String PROPERTIES_DOMAIN =
    "edu.vt.middleware.ldap.servlets.";

  /** LDAP initialization properties file, value is {@value}. */
  public static final String PROPERTIES_FILE = PROPERTIES_DOMAIN +
    "propertiesFile";

  /** LDAP pool initialization properties file, value is {@value}. */
  public static final String POOL_PROPERTIES_FILE = PROPERTIES_DOMAIN +
    "poolPropertiesFile";

  /** Format of search output, value is {@value}. */
  public static final String OUTPUT_FORMAT = PROPERTIES_DOMAIN + "outputFormat";

  /** Type of pool used, value is {@value}. */
  public static final String POOL_TYPE = PROPERTIES_DOMAIN + "poolType";

  /**
   * Identifier to set in the session after valid authentication, value is
   * {@value}.
   */
  public static final String SESSION_ID = PROPERTIES_DOMAIN + "sessionId";

  /**
   * Default identifier to set in the session after valid authentication, value
   * is {@value}.
   */
  public static final String DEFAULT_SESSION_ID = "user";

  /** Whether to invalidate the user session at logout, value is {@value}. */
  public static final String INVALIDATE_SESSION = PROPERTIES_DOMAIN +
    "invalidateSession";

  /**
   * Default behavior for invalidating the user session at logout, value is
   * {@value}.
   */
  public static final String DEFAULT_INVALIDATE_SESSION = "true";

  /** URL of the page that collects user credentials, value is {@value}. */
  public static final String LOGIN_URL = PROPERTIES_DOMAIN + "loginUrl";

  /**
   * Default URL of the page that does collects user credentials, value is
   * {@value}.
   */
  public static final String DEFAULT_LOGIN_URL = "/";

  /** Error message to display if authentication fails, value is {@value}. */
  public static final String ERROR_MSG = PROPERTIES_DOMAIN + "errorMsg";

  /** Class used to initialize http sessions. */
  public static final String SESSION_MANAGER = PROPERTIES_DOMAIN +
    "sessionManager";

  /** Default session initializer, value is {@value}. */
  public static final String DEFAULT_SESSION_MANAGER =
    "edu.vt.middleware.ldap.servlets.session.DefaultSessionManager";

  /** Default error message, value is {@value}. */
  public static final String DEFAULT_ERROR_MSG =
    "Could not authenticate or authorize user";

  /** HTTP parameter used to transmit the user identifier, value is {@value}. */
  public static final String USER_PARAM = "user";

  /** HTTP parameter used to transmit the user credential, value is {@value}. */
  public static final String CREDENTIAL_PARAM = "credential";

  /** HTTP parameter used to transmit the redirect url, value is {@value}. */
  public static final String URL_PARAM = "url";


  /** Default constructor. */
  private ServletConstants() {}
}
/*
  $Id: ServletConstants.java 302 2009-07-02 20:01:04Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 302 $
  Updated: $Date: 2009-07-02 13:01:04 -0700 (Thu, 02 Jul 2009) $
*/
package edu.vt.middleware.ldap.servlets;

/**
 * <code>ServletConstants</code> contains all the constants needed by the ldap
 * servlet package.
 *
 * @author  Middleware Services
 * @version  $Revision: 302 $ $Date: 2009-07-02 13:01:04 -0700 (Thu, 02 Jul 2009) $
 */
public final class ServletConstants
{

  /** Domain to look for properties in, value is {@value}. */
  public static final String PROPERTIES_DOMAIN =
    "edu.vt.middleware.ldap.servlets.";

  /** LDAP initialization properties file, value is {@value}. */
  public static final String PROPERTIES_FILE = PROPERTIES_DOMAIN +
    "propertiesFile";

  /** LDAP pool initialization properties file, value is {@value}. */
  public static final String POOL_PROPERTIES_FILE = PROPERTIES_DOMAIN +
    "poolPropertiesFile";

  /** Format of search output, value is {@value}. */
  public static final String OUTPUT_FORMAT = PROPERTIES_DOMAIN + "outputFormat";

  /** Default format of search output, value is {@value}. */
  public static final String DEFAULT_OUTPUT_FORMAT = "DSML";

  /** Type of pool used, value is {@value}. */
  public static final String POOL_TYPE = PROPERTIES_DOMAIN + "poolType";

  /**
   * Identifier to set in the session after valid authentication, value is
   * {@value}.
   */
  public static final String SESSION_ID = PROPERTIES_DOMAIN + "sessionId";

  /**
   * Default identifier to set in the session after valid authentication, value
   * is {@value}.
   */
  public static final String DEFAULT_SESSION_ID = "user";

  /** Whether to invalidate the user session at logout, value is {@value}. */
  public static final String INVALIDATE_SESSION = PROPERTIES_DOMAIN +
    "invalidateSession";

  /**
   * Default behavior for invalidating the user session at logout, value is
   * {@value}.
   */
  public static final String DEFAULT_INVALIDATE_SESSION = "true";

  /** URL of the page that collects user credentials, value is {@value}. */
  public static final String LOGIN_URL = PROPERTIES_DOMAIN + "loginUrl";

  /**
   * Default URL of the page that does collects user credentials, value is
   * {@value}.
   */
  public static final String DEFAULT_LOGIN_URL = "/";

  /** Error message to display if authentication fails, value is {@value}. */
  public static final String ERROR_MSG = PROPERTIES_DOMAIN + "errorMsg";

  /** Class used to initialize http sessions. */
  public static final String SESSION_MANAGER = PROPERTIES_DOMAIN +
    "sessionManager";

  /** Default session initializer, value is {@value}. */
  public static final String DEFAULT_SESSION_MANAGER =
    "edu.vt.middleware.ldap.servlets.session.DefaultSessionManager";

  /** Default error message, value is {@value}. */
  public static final String DEFAULT_ERROR_MSG =
    "Could not authenticate or authorize user";

  /** HTTP parameter used to transmit the user identifier, value is {@value}. */
  public static final String USER_PARAM = "user";

  /** HTTP parameter used to transmit the user credential, value is {@value}. */
  public static final String CREDENTIAL_PARAM = "credential";

  /** HTTP parameter used to transmit the redirect url, value is {@value}. */
  public static final String URL_PARAM = "url";


  /** Default constructor. */
  private ServletConstants() {}
}
