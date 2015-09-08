/*
  $Id: AuthenticatorConfig.java 494 2009-08-28 02:31:50Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 494 $
  Updated: $Date: 2009-08-27 19:31:50 -0700 (Thu, 27 Aug 2009) $
*/
package edu.vt.middleware.ldap;

import java.io.InputStream;
import java.util.Arrays;
import edu.vt.middleware.ldap.handler.AuthenticationResultHandler;
import edu.vt.middleware.ldap.props.LdapProperties;
import edu.vt.middleware.ldap.props.PropertyInvoker;

/**
 * <code>AuthenticatorConfig</code> contains all the configuration data that the
 * <code>Authenticator</code> needs to control authentication.
 *
 * @author  Middleware Services
 * @version  $Revision: 494 $ $Date: 2009-08-27 19:31:50 -0700 (Thu, 27 Aug 2009) $
 */
public class AuthenticatorConfig extends LdapConfig
{

  /** Domain to look for ldap properties in, value is {@value}. */
  public static final String PROPERTIES_DOMAIN = "edu.vt.middleware.ldap.auth.";

  /** Invoker for ldap properties. */
  private static final PropertyInvoker PROPERTIES = new PropertyInvoker(
    AuthenticatorConfig.class,
    PROPERTIES_DOMAIN);

  /** Directory user field. */
  private String[] userField = new String[] {
    LdapConstants.DEFAULT_USER_FIELD,
  };

  /** User to authenticate. */
  private String user;

  /** Credential for authenticating user. */
  private Object credential;

  /** Filter for authorizing user. */
  private String authorizationFilter;

  /** Whether to construct the DN when authenticating. */
  private boolean constructDn = LdapConstants.DEFAULT_CONSTRUCT_DN;

  /** Whether to perform subtree searches for DNs. */
  private boolean subtreeSearch = LdapConstants.DEFAULT_SUBTREE_SEARCH;

  /** Handlers to process authentications. */
  private AuthenticationResultHandler[] authenticationResultHandlers;


  /** Default constructor. */
  public AuthenticatorConfig() {}


  /**
   * This will create a new <code>AuthenticatorConfig</code> with the supplied
   * ldap url and base Strings.
   *
   * @param  ldapUrl  <code>String</code> LDAP URL
   * @param  base  <code>String</code> LDAP base DN
   */
  public AuthenticatorConfig(final String ldapUrl, final String base)
  {
    this();
    this.setLdapUrl(ldapUrl);
    this.setBase(base);
  }


  /**
   * This returns the user field(s) of the <code>Authenticator</code>.
   *
   * @return  <code>String[]</code> - user field name(s)
   */
  public String[] getUserField()
  {
    return this.userField;
  }


  /**
   * This returns the user of the <code>Authenticator</code>.
   *
   * @return  <code>String</code> - user name
   */
  public String getUser()
  {
    return this.user;
  }


  /**
   * This returns the credential of the <code>Authenticator</code>.
   *
   * @return  <code>Object</code> - user credential
   */
  public Object getCredential()
  {
    return this.credential;
  }


  /**
   * This returns the filter used to authorize users.
   *
   * @return  <code>String</code> - filter
   */
  public String getAuthorizationFilter()
  {
    return this.authorizationFilter;
  }


  /**
   * This returns the constructDn of the <code>Authenticator</code>.
   *
   * @return  <code>boolean</code> - whether the DN will be constructed
   */
  public boolean getConstructDn()
  {
    return this.constructDn;
  }


  /**
   * This returns the subtreeSearch of the <code>Authenticator</code>.
   *
   * @return  <code>boolean</code> - whether the DN will be searched for over
   * the entire base
   */
  public boolean getSubtreeSearch()
  {
    return this.subtreeSearch;
  }


  /**
   * This returns the handlers to use for processing authentications.
   *
   * @return  <code>AuthenticationResultHandler[]</code>
   */
  public AuthenticationResultHandler[] getAuthenticationResultHandlers()
  {
    return this.authenticationResultHandlers;
  }


  /**
   * This sets the user fields for the <code>Authenticator</code>. The user
   * field is used to lookup a user's dn.
   *
   * @param  userField  <code>String[]</code> username
   */
  public void setUserField(final String[] userField)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace(
        "setting userField: " +
        (userField == null ? "null" : Arrays.asList(userField)));
    }
    this.userField = userField;
  }


  /**
   * This sets the username for the <code>Authenticator</code> to use for
   * authentication.
   *
   * @param  user  <code>String</code> username
   */
  public void setUser(final String user)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting user: " + user);
    }
    this.user = user;
  }

  /**
   * This sets the credential for the <code>Authenticator</code> to use for
   * authentication.
   *
   * @param  credential  <code>Object</code>
   */
  public void setCredential(final Object credential)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      if (this.getLogCredentials()) {
        this.logger.trace("setting credential: " + credential);
      } else {
        this.logger.trace("setting credential: <suppressed>");
      }
    }
    this.credential = credential;
  }


  /**
   * This sets the filter used to authorize users. If not set, no authorization
   * is performed.
   *
   * @param  authorizationFilter  <code>String</code>
   */
  public void setAuthorizationFilter(final String authorizationFilter)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting authorizationFilter: " + authorizationFilter);
    }
    this.authorizationFilter = authorizationFilter;
  }


  /**
   * This sets the constructDn for the <code>Authenticator</code>. If true, the
   * DN used for authenticating will be constructed using the {@link #userField}
   * and {@link LdapConfig#getBase()}. In the form: dn =
   * userField+'='+user+','+base Otherwise the DN will be looked up in the LDAP.
   *
   * @param  constructDn  <code>boolean</code>
   */
  public void setConstructDn(final boolean constructDn)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting constructDn: " + constructDn);
    }
    this.constructDn = constructDn;
  }


  /**
   * This sets the subtreeSearch for the <code>Authenticator</code>. If true,
   * the DN used for authenticating will be searched for over the entire {@link
   * LdapConfig#getBase()}. Otherwise the DN will be search for in the {@link
   * LdapConfig#getBase()} context.
   *
   * @param  subtreeSearch  <code>boolean</code>
   */
  public void setSubtreeSearch(final boolean subtreeSearch)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting subtreeSearch: " + subtreeSearch);
    }
    this.subtreeSearch = subtreeSearch;
  }


  /**
   * This sets the handlers for processing authentications.
   *
   * @param  handlers  <code>AuthenticationResultHandler[]</code>
   */
  public void setAuthenticationResultHandlers(
    final AuthenticationResultHandler[] handlers)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting authenticationResultHandlers: " + handlers);
    }
    this.authenticationResultHandlers = handlers;
  }


  /** {@inheritDoc}. */
  public String getPropertiesDomain()
  {
    return PROPERTIES_DOMAIN;
  }


  /** {@inheritDoc}. */
  public void setEnvironmentProperties(final String name, final String value)
  {
    checkImmutable();
    if (name != null && value != null) {
      if (PROPERTIES.hasProperty(name)) {
        PROPERTIES.setProperty(this, name, value);
      } else {
        super.setEnvironmentProperties(name, value);
      }
    }
  }


  /** {@inheritDoc}. */
  public boolean hasEnvironmentProperty(final String name)
  {
    return PROPERTIES.hasProperty(name);
  }


  /**
   * Create an instance of this class initialized with properties from the input
   * stream. If the input stream is null, load properties from the default
   * properties file.
   *
   * @param  is  to load properties from
   *
   * @return  <code>AuthenticatorConfig</code> initialized ldap pool config
   */
  public static AuthenticatorConfig createFromProperties(final InputStream is)
  {
    final AuthenticatorConfig authConfig = new AuthenticatorConfig();
    LdapProperties properties = null;
    if (is != null) {
      properties = new LdapProperties(authConfig, is);
    } else {
      properties = new LdapProperties(authConfig);
      properties.useDefaultPropertiesFile();
    }
    properties.configure();
    return authConfig;
  }
}
/*
  $Id: AuthenticatorConfig.java 639 2009-09-18 17:55:42Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 639 $
  Updated: $Date: 2009-09-18 10:55:42 -0700 (Fri, 18 Sep 2009) $
*/
package edu.vt.middleware.ldap;

import java.io.InputStream;
import java.util.Arrays;
import edu.vt.middleware.ldap.handler.AuthenticationResultHandler;
import edu.vt.middleware.ldap.props.LdapProperties;
import edu.vt.middleware.ldap.props.PropertyInvoker;

/**
 * <code>AuthenticatorConfig</code> contains all the configuration data that the
 * <code>Authenticator</code> needs to control authentication.
 *
 * @author  Middleware Services
 * @version  $Revision: 639 $ $Date: 2009-09-18 10:55:42 -0700 (Fri, 18 Sep 2009) $
 */
public class AuthenticatorConfig extends LdapConfig
{

  /** Domain to look for ldap properties in, value is {@value}. */
  public static final String PROPERTIES_DOMAIN = "edu.vt.middleware.ldap.auth.";

  /** Invoker for ldap properties. */
  private static final PropertyInvoker PROPERTIES = new PropertyInvoker(
    AuthenticatorConfig.class,
    PROPERTIES_DOMAIN);

  /** Directory user field. */
  private String[] userField = new String[] {
    LdapConstants.DEFAULT_USER_FIELD,
  };

  /** Filter for searching for the user. */
  private String userFilter;

  /** Filter arguments for searching for the user. */
  private Object[] userFilterArgs;

  /** User to authenticate. */
  private String user;

  /** Credential for authenticating user. */
  private Object credential;

  /** Filter for authorizing user. */
  private String authorizationFilter;

  /** Filter arguments for authorizing user. */
  private Object[] authorizationFilterArgs;

  /** Whether to construct the DN when authenticating. */
  private boolean constructDn = LdapConstants.DEFAULT_CONSTRUCT_DN;

  /** Handlers to process authentications. */
  private AuthenticationResultHandler[] authenticationResultHandlers;


  /** Default constructor. */
  public AuthenticatorConfig()
  {
    this.setSearchScope(SearchScope.ONELEVEL);
  }


  /**
   * This will create a new <code>AuthenticatorConfig</code> with the supplied
   * ldap url and base Strings.
   *
   * @param  ldapUrl  <code>String</code> LDAP URL
   * @param  base  <code>String</code> LDAP base DN
   */
  public AuthenticatorConfig(final String ldapUrl, final String base)
  {
    this();
    this.setLdapUrl(ldapUrl);
    this.setBase(base);
  }


  /**
   * This returns the user field(s) of the <code>Authenticator</code>.
   *
   * @return  <code>String[]</code> - user field name(s)
   */
  public String[] getUserField()
  {
    return this.userField;
  }


  /**
   * This returns the filter used to search for the user.
   *
   * @return  <code>String</code> - filter
   */
  public String getUserFilter()
  {
    return this.userFilter;
  }


  /**
   * This returns the filter arguments used to search for the user.
   *
   * @return  <code>Object[]</code> - filter arguments
   */
  public Object[] getUserFilterArgs()
  {
    return this.userFilterArgs;
  }


  /**
   * This returns the user of the <code>Authenticator</code>.
   *
   * @return  <code>String</code> - user name
   */
  public String getUser()
  {
    return this.user;
  }


  /**
   * This returns the credential of the <code>Authenticator</code>.
   *
   * @return  <code>Object</code> - user credential
   */
  public Object getCredential()
  {
    return this.credential;
  }


  /**
   * This returns the filter used to authorize users.
   *
   * @return  <code>String</code> - filter
   */
  public String getAuthorizationFilter()
  {
    return this.authorizationFilter;
  }


  /**
   * This returns the filter arguments used to authorize users.
   *
   * @return  <code>Object[]</code> - filter arguments
   */
  public Object[] getAuthorizationFilterArgs()
  {
    return this.authorizationFilterArgs;
  }


  /**
   * This returns the constructDn of the <code>Authenticator</code>.
   *
   * @return  <code>boolean</code> - whether the DN will be constructed
   */
  public boolean getConstructDn()
  {
    return this.constructDn;
  }


  /**
   * This returns the subtreeSearch of the <code>Authenticator</code>.
   *
   * @return  <code>boolean</code> - whether the DN will be searched for over
   * the entire base
   */
  public boolean getSubtreeSearch()
  {
    return SearchScope.SUBTREE == this.getSearchScope();
  }


  /**
   * This returns the handlers to use for processing authentications.
   *
   * @return  <code>AuthenticationResultHandler[]</code>
   */
  public AuthenticationResultHandler[] getAuthenticationResultHandlers()
  {
    return this.authenticationResultHandlers;
  }


  /**
   * This sets the user fields for the <code>Authenticator</code>. The user
   * field is used to lookup a user's dn.
   *
   * @param  userField  <code>String[]</code> username
   */
  public void setUserField(final String[] userField)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace(
        "setting userField: " +
        (userField == null ? "null" : Arrays.asList(userField)));
    }
    this.userField = userField;
  }


  /**
   * This sets the filter used to search for users. If not set, the user field
   * is used to build a search filter.
   *
   * @param  userFilter  <code>String</code>
   */
  public void setUserFilter(final String userFilter)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting userFilter: " + userFilter);
    }
    this.userFilter = userFilter;
  }


  /**
   * This sets the filter arguments used to search for users.
   *
   * @param  userFilterArgs  <code>Object[]</code>
   */
  public void setUserFilterArgs(final Object[] userFilterArgs)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace(
        "setting userFilterArgs: " +
        (userFilterArgs == null ?
          "null" : Arrays.asList(userFilterArgs)));
    }
    this.userFilterArgs = userFilterArgs;
  }


  /**
   * This sets the username for the <code>Authenticator</code> to use for
   * authentication.
   *
   * @param  user  <code>String</code> username
   */
  public void setUser(final String user)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting user: " + user);
    }
    this.user = user;
  }

  /**
   * This sets the credential for the <code>Authenticator</code> to use for
   * authentication.
   *
   * @param  credential  <code>Object</code>
   */
  public void setCredential(final Object credential)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      if (this.getLogCredentials()) {
        this.logger.trace("setting credential: " + credential);
      } else {
        this.logger.trace("setting credential: <suppressed>");
      }
    }
    this.credential = credential;
  }


  /**
   * This sets the filter used to authorize users. If not set, no authorization
   * is performed.
   *
   * @param  authorizationFilter  <code>String</code>
   */
  public void setAuthorizationFilter(final String authorizationFilter)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting authorizationFilter: " + authorizationFilter);
    }
    this.authorizationFilter = authorizationFilter;
  }


  /**
   * This sets the filter arguments used to authorize users.
   *
   * @param  authorizationFilterArgs  <code>Object[]</code>
   */
  public void setAuthorizationFilterArgs(final Object[] authorizationFilterArgs)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace(
        "setting authorizationFilterArgs: " +
        (authorizationFilterArgs == null ?
          "null" : Arrays.asList(authorizationFilterArgs)));
    }
    this.authorizationFilterArgs = authorizationFilterArgs;
  }


  /**
   * This sets the constructDn for the <code>Authenticator</code>. If true, the
   * DN used for authenticating will be constructed using the {@link #userField}
   * and {@link LdapConfig#getBase()}. In the form: dn =
   * userField+'='+user+','+base Otherwise the DN will be looked up in the LDAP.
   *
   * @param  constructDn  <code>boolean</code>
   */
  public void setConstructDn(final boolean constructDn)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting constructDn: " + constructDn);
    }
    this.constructDn = constructDn;
  }


  /**
   * This sets the subtreeSearch for the <code>Authenticator</code>. If true,
   * the DN used for authenticating will be searched for over the entire {@link
   * LdapConfig#getBase()}. Otherwise the DN will be search for in the {@link
   * LdapConfig#getBase()} context.
   *
   * @param  subtreeSearch  <code>boolean</code>
   */
  public void setSubtreeSearch(final boolean subtreeSearch)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting subtreeSearch: " + subtreeSearch);
    }
    if (subtreeSearch) {
      this.setSearchScope(SearchScope.SUBTREE);
    } else {
      this.setSearchScope(SearchScope.ONELEVEL);
    }
  }


  /**
   * This sets the handlers for processing authentications.
   *
   * @param  handlers  <code>AuthenticationResultHandler[]</code>
   */
  public void setAuthenticationResultHandlers(
    final AuthenticationResultHandler[] handlers)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting authenticationResultHandlers: " + handlers);
    }
    this.authenticationResultHandlers = handlers;
  }


  /** {@inheritDoc}. */
  public String getPropertiesDomain()
  {
    return PROPERTIES_DOMAIN;
  }


  /** {@inheritDoc}. */
  public void setEnvironmentProperties(final String name, final String value)
  {
    checkImmutable();
    if (name != null && value != null) {
      if (PROPERTIES.hasProperty(name)) {
        PROPERTIES.setProperty(this, name, value);
      } else {
        super.setEnvironmentProperties(name, value);
      }
    }
  }


  /** {@inheritDoc}. */
  public boolean hasEnvironmentProperty(final String name)
  {
    return PROPERTIES.hasProperty(name);
  }


  /**
   * Create an instance of this class initialized with properties from the input
   * stream. If the input stream is null, load properties from the default
   * properties file.
   *
   * @param  is  to load properties from
   *
   * @return  <code>AuthenticatorConfig</code> initialized ldap pool config
   */
  public static AuthenticatorConfig createFromProperties(final InputStream is)
  {
    final AuthenticatorConfig authConfig = new AuthenticatorConfig();
    LdapProperties properties = null;
    if (is != null) {
      properties = new LdapProperties(authConfig, is);
    } else {
      properties = new LdapProperties(authConfig);
      properties.useDefaultPropertiesFile();
    }
    properties.configure();
    return authConfig;
  }
}
/*
  $Id: AuthenticatorConfig.java 183 2009-05-06 02:45:57Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 183 $
  Updated: $Date: 2009-05-05 19:45:57 -0700 (Tue, 05 May 2009) $
*/
package edu.vt.middleware.ldap;

import edu.vt.middleware.ldap.props.LdapProperties;
import edu.vt.middleware.ldap.props.PropertyInvoker;

/**
 * <code>AuthenticatorConfig</code> contains all the configuration data that the
 * <code>Authenticator</code> needs to control authentication.
 *
 * @author  Middleware Services
 * @version  $Revision: 183 $ $Date: 2009-05-05 19:45:57 -0700 (Tue, 05 May 2009) $
 */
public class AuthenticatorConfig extends LdapConfig
{

  /** Domain to look for ldap properties in, value is {@value}. */
  public static final String PROPERTIES_DOMAIN = "edu.vt.middleware.ldap.auth.";

  /** Invoker for ldap properties. */
  private static final PropertyInvoker PROPERTIES = new PropertyInvoker(
    AuthenticatorConfig.class,
    PROPERTIES_DOMAIN);

  /** Directory user field. */
  private String[] userField = new String[] {
    LdapConstants.DEFAULT_USER_FIELD,
  };

  /** User to authenticate. */
  private String user;

  /** Credential for authenticating user. */
  private Object credential;

  /** Filter for authorizing user. */
  private String authorizationFilter;

  /** Whether to construct the DN when authenticating. */
  private boolean constructDn = LdapConstants.DEFAULT_CONSTRUCT_DN;

  /** Whether to perform subtree searches for DNs. */
  private boolean subtreeSearch = LdapConstants.DEFAULT_SUBTREE_SEARCH;


  /** Default constructor. */
  public AuthenticatorConfig() {}


  /**
   * This will create a new <code>AuthenticatorConfig</code> with the supplied
   * ldap url and base Strings.
   *
   * @param  ldapUrl  <code>String</code> LDAP URL
   * @param  base  <code>String</code> LDAP base DN
   */
  public AuthenticatorConfig(final String ldapUrl, final String base)
  {
    this();
    this.setLdapUrl(ldapUrl);
    this.setBase(base);
  }


  /**
   * This returns the user field(s) of the <code>Authenticator</code>.
   *
   * @return  <code>String[]</code> - user field name(s)
   */
  public String[] getUserField()
  {
    return this.userField;
  }


  /**
   * This returns the user of the <code>Authenticator</code>.
   *
   * @return  <code>String</code> - user name
   */
  public String getUser()
  {
    return this.user;
  }


  /**
   * This returns the credential of the <code>Authenticator</code>.
   *
   * @return  <code>Object</code> - user credential
   */
  public Object getCredential()
  {
    return this.credential;
  }


  /**
   * This returns the filter used to authorize users.
   *
   * @return  <code>String</code> - filter
   */
  public String getAuthorizationFilter()
  {
    return this.authorizationFilter;
  }


  /**
   * This returns the constructDn of the <code>Authenticator</code>.
   *
   * @return  <code>boolean</code> - whether the DN will be constructed
   */
  public boolean getConstructDn()
  {
    return this.constructDn;
  }


  /**
   * This returns the subtreeSearch of the <code>Authenticator</code>.
   *
   * @return  <code>boolean</code> - whether the DN will be searched for over
   * the entire base
   */
  public boolean getSubtreeSearch()
  {
    return this.subtreeSearch;
  }


  /**
   * This sets the user fields for the <code>Authenticator</code>. The user
   * field is used to lookup a user's dn.
   *
   * @param  userField  <code>String[]</code> username
   */
  public void setUserField(final String[] userField)
  {
    checkImmutable();
    this.userField = userField;
  }


  /**
   * This sets the username for the <code>Authenticator</code> to use for
   * authentication.
   *
   * @param  user  <code>String</code> username
   */
  public void setUser(final String user)
  {
    checkImmutable();
    this.user = user;
  }

  /**
   * This sets the credential for the <code>Authenticator</code> to use for
   * authentication.
   *
   * @param  credential  <code>Object</code>
   */
  public void setCredential(final Object credential)
  {
    checkImmutable();
    this.credential = credential;
  }


  /**
   * This sets the filter used to authorize users. If not set, no authorization
   * is performed.
   *
   * @param  authorizationFilter  <code>String</code>
   */
  public void setAuthorizationFilter(final String authorizationFilter)
  {
    checkImmutable();
    this.authorizationFilter = authorizationFilter;
  }


  /**
   * This sets the constructDn for the <code>Authenticator</code>. If true, the
   * DN used for authenticating will be constructed using the {@link #userField}
   * and {@link LdapConfig#getBase()}. In the form: dn =
   * userField+'='+user+','+base Otherwise the DN will be looked up in the LDAP.
   *
   * @param  constructDn  <code>boolean</code>
   */
  public void setConstructDn(final boolean constructDn)
  {
    checkImmutable();
    this.constructDn = constructDn;
  }


  /**
   * This sets the subtreeSearch for the <code>Authenticator</code>. If true,
   * the DN used for authenticating will be searched for over the entire {@link
   * LdapConfig#getBase()}. Otherwise the DN will be search for in the {@link
   * LdapConfig#getBase()} context.
   *
   * @param  subtreeSearch  <code>boolean</code>
   */
  public void setSubtreeSearch(final boolean subtreeSearch)
  {
    checkImmutable();
    this.subtreeSearch = subtreeSearch;
  }


  /** {@inheritDoc}. */
  public String getPropertiesDomain()
  {
    return PROPERTIES_DOMAIN;
  }


  /** {@inheritDoc}. */
  public void setEnvironmentProperties(final String name, final String value)
  {
    checkImmutable();
    if (name != null && value != null) {
      if (PROPERTIES.hasProperty(name)) {
        PROPERTIES.setProperty(this, name, value);
      } else {
        super.setEnvironmentProperties(name, value);
      }
    }
  }


  /** {@inheritDoc}. */
  public boolean hasEnvironmentProperty(final String name)
  {
    return PROPERTIES.hasProperty(name);
  }


  /**
   * Create an instance of this class initialized with properties from the
   * properties file. If propertiesFile is null, load properties from the
   * default properties file.
   *
   * @param  propertiesFile  to load properties from
   *
   * @return  <code>AuthenticatorConfig</code> initialized ldap pool config
   */
  public static AuthenticatorConfig createFromProperties(
    final String propertiesFile)
  {
    final AuthenticatorConfig authConfig = new AuthenticatorConfig();
    LdapProperties properties = null;
    if (propertiesFile != null) {
      properties = new LdapProperties(authConfig, propertiesFile);
    } else {
      properties = new LdapProperties(authConfig);
    }
    properties.configure();
    return authConfig;
  }
}
