/*
  $Id: AbstractLoginModule.java 466 2009-08-20 18:05:31Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 466 $
  Updated: $Date: 2009-08-20 11:05:31 -0700 (Thu, 20 Aug 2009) $
*/
package edu.vt.middleware.ldap.jaas;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import edu.vt.middleware.ldap.Authenticator;
import edu.vt.middleware.ldap.AuthenticatorConfig;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.LdapConfig;
import edu.vt.middleware.ldap.bean.LdapAttribute;
import edu.vt.middleware.ldap.bean.LdapAttributes;
import edu.vt.middleware.ldap.props.LdapProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <code>AbstractLoginModule</code> provides functionality common to ldap based
 * login modules.
 *
 * @author  Middleware Services
 * @version  $Revision: 466 $ $Date: 2009-08-20 11:05:31 -0700 (Thu, 20 Aug 2009) $
 */
public abstract class AbstractLoginModule implements LoginModule
{

  /** Constant for login name stored in shared state. */
  public static final String LOGIN_NAME = "javax.security.auth.login.name";

  /** Constant for entryDn stored in shared state. */
  public static final String LOGIN_DN =
    "edu.vt.middleware.ldap.jaas.login.entryDn";

  /** Constant for login password stored in shared state. */
  public static final String LOGIN_PASSWORD =
    "javax.security.auth.login.password";

  /** Regular expression for ldap properties to ignore. */
  private static final String IGNORE_LDAP_REGEX =
    "useFirstPass|tryFirstPass|storePass|setLdapPrincipal|" +
    "setLdapDnPrincipal|setLdapCredential|userRoleAttribute|" +
    "roleFilter|roleAttribute";

  /** Log for this class. */
  protected final Log logger = LogFactory.getLog(this.getClass());

  /** Initialized subject. */
  protected Subject subject;

  /** Initialized callback handler. */
  protected CallbackHandler callbackHandler;

  /** Shared state from other login module. */
  @SuppressWarnings("unchecked")
  protected Map sharedState;

  /** Whether credentials from the shared state should be used. */
  protected boolean useFirstPass;

  /**
   * Whether credentials from the shared state should be used if they are
   * available.
   */
  protected boolean tryFirstPass;

  /** Whether credentials should be stored in the shared state map. */
  protected boolean storePass;

  /** Whether ldap principal data should be set. */
  protected boolean setLdapPrincipal;

  /** Whether ldap dn principal data should be set. */
  protected boolean setLdapDnPrincipal;

  /** Whether ldap credential data should be set. */
  protected boolean setLdapCredential;

  /** Whether authentication was successful. */
  protected boolean success;

  /** Principals to add to the subject. */
  protected Set<Principal> principals;

  /** Credentials to add to the subject. */
  protected Set<LdapCredential> credentials;


  /** {@inheritDoc}. */
  public void initialize(
    final Subject subject,
    final CallbackHandler callbackHandler,
    final Map<String, ?> sharedState,
    final Map<String, ?> options)
  {
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("Begin initialize");
    }
    this.subject = subject;
    this.callbackHandler = callbackHandler;
    this.sharedState = sharedState;

    final Iterator<String> i = options.keySet().iterator();
    while (i.hasNext()) {
      final String key = i.next();
      final String value = (String) options.get(key);
      if (key.equalsIgnoreCase("useFirstPass")) {
        this.useFirstPass = Boolean.valueOf(value);
      } else if (key.equalsIgnoreCase("tryFirstPass")) {
        this.tryFirstPass = Boolean.valueOf(value);
      } else if (key.equalsIgnoreCase("storePass")) {
        this.storePass = Boolean.valueOf(value);
      } else if (key.equalsIgnoreCase("setLdapPrincipal")) {
        this.setLdapPrincipal = Boolean.valueOf(value);
      } else if (key.equalsIgnoreCase("setLdapDnPrincipal")) {
        this.setLdapDnPrincipal = Boolean.valueOf(value);
      } else if (key.equalsIgnoreCase("setLdapCredential")) {
        this.setLdapCredential = Boolean.valueOf(value);
      }
    }

    if (this.logger.isDebugEnabled()) {
      this.logger.debug("useFirstPass = " + this.useFirstPass);
      this.logger.debug("tryFirstPass = " + this.tryFirstPass);
      this.logger.debug("storePass = " + this.storePass);
      this.logger.debug("setLdapPrincipal = " + this.setLdapPrincipal);
      this.logger.debug("setLdapDnPrincipal = " + this.setLdapDnPrincipal);
      this.logger.debug("setLdapCredential = " + this.setLdapCredential);
    }

    this.principals = new TreeSet<Principal>();
    this.credentials = new HashSet<LdapCredential>();
  }


  /** {@inheritDoc}. */
  public abstract boolean login()
    throws LoginException;


  /** {@inheritDoc}. */
  public boolean commit()
    throws LoginException
  {
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("Begin commit");
    }
    if (this.success) {
      if (this.subject.isReadOnly()) {
        throw new LoginException("Subject is read-only.");
      }
      this.subject.getPrincipals().addAll(this.principals);
      if (this.logger.isDebugEnabled()) {
        this.logger.debug(
          "Committed the following principals: " + this.principals);
      }
      this.subject.getPrivateCredentials().addAll(this.credentials);
    }
    this.principals.clear();
    this.credentials.clear();
    return true;
  }


  /** {@inheritDoc}. */
  public boolean abort()
  {
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("Begin abort");
    }
    this.success = false;
    logout();
    return true;
  }


  /** {@inheritDoc}. */
  public boolean logout()
  {
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("Begin logout");
    }
    this.principals.clear();
    this.credentials.clear();

    final Iterator<LdapPrincipal> prinIter = this.subject.getPrincipals(
      LdapPrincipal.class).iterator();
    while (prinIter.hasNext()) {
      this.subject.getPrincipals().remove(prinIter.next());
    }

    final Iterator<LdapDnPrincipal> dnPrinIter = this.subject.getPrincipals(
      LdapDnPrincipal.class).iterator();
    while (dnPrinIter.hasNext()) {
      this.subject.getPrincipals().remove(dnPrinIter.next());
    }

    final Iterator<LdapRole> roleIter = this.subject.getPrincipals(
      LdapRole.class).iterator();
    while (roleIter.hasNext()) {
      this.subject.getPrincipals().remove(roleIter.next());
    }

    final Iterator<LdapCredential> credIter = this.subject
        .getPrivateCredentials(LdapCredential.class).iterator();
    while (credIter.hasNext()) {
      this.subject.getPrivateCredentials().remove(credIter.next());
    }

    return true;
  }


  /**
   * This constructs a new <code>Ldap</code> with the supplied jaas options.
   *
   * @param  options  <code>Map</code>
   *
   * @return  <code>Ldap</code>
   */
  public static Ldap createLdap(final Map<String, ?> options)
  {
    final LdapConfig ldapConfig = new LdapConfig();
    final LdapProperties ldapProperties = new LdapProperties(ldapConfig);
    final Iterator<String> i = options.keySet().iterator();
    while (i.hasNext()) {
      final String key = i.next();
      final String value = (String) options.get(key);
      if (!key.matches(IGNORE_LDAP_REGEX)) {
        ldapProperties.setProperty(key, value);
      }
    }
    ldapProperties.configure();
    return new Ldap(ldapConfig);
  }


  /**
   * This constructs a new <code>Authenticator</code> with the supplied jaas
   * options.
   *
   * @param  options  <code>Map</code>
   *
   * @return  <code>Authenticator</code>
   */
  public static Authenticator createAuthenticator(final Map<String, ?> options)
  {
    final AuthenticatorConfig authConfig = new AuthenticatorConfig();
    final LdapProperties authProperties = new LdapProperties(authConfig);
    final Iterator<String> i = options.keySet().iterator();
    while (i.hasNext()) {
      final String key = i.next();
      final String value = (String) options.get(key);
      if (!key.matches(IGNORE_LDAP_REGEX)) {
        authProperties.setProperty(key, value);
      }
    }
    authProperties.configure();
    return new Authenticator(authConfig);
  }


  /**
   * This attempts to retrieve credentials for the supplied name and password
   * callbacks. If useFirstPass or tryFirstPass is set, then name and password
   * data is retrieved from shared state. Otherwise a callback handler is used
   * to get the data. Set useCallback to force a callback handler to be used.
   *
   * @param  nameCb  to set name for
   * @param  passCb  to set password for
   * @param  useCallback  whether to force a callback handler
   *
   * @throws  LoginException  if the callback handler fails
   */
  protected void getCredentials(
    final NameCallback nameCb,
    final PasswordCallback passCb,
    final boolean useCallback)
    throws LoginException
  {
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("Begin getCredentials");
      this.logger.trace("  useFistPass = " + this.useFirstPass);
      this.logger.trace("  tryFistPass = " + this.tryFirstPass);
      this.logger.trace("  useCallback = " + useCallback);
      this.logger.trace(
        "  callbackhandler class = " +
        this.callbackHandler.getClass().getName());
      this.logger.trace(
        "  name callback class = " + nameCb.getClass().getName());
      this.logger.trace(
        "  password callback class = " + passCb.getClass().getName());
    }
    try {
      if ((this.useFirstPass || this.tryFirstPass) && !useCallback) {
        nameCb.setName((String) this.sharedState.get(LOGIN_NAME));
        passCb.setPassword((char[]) this.sharedState.get(LOGIN_PASSWORD));
      } else if (this.callbackHandler != null) {
        this.callbackHandler.handle(new Callback[] {nameCb, passCb});
      } else {
        throw new LoginException(
          "No CallbackHandler available. " +
          "Set useFirstPass, tryFirstPass, or provide a CallbackHandler");
      }
    } catch (IOException e) {
      if (this.logger.isErrorEnabled()) {
        this.logger.error("Error reading data from callback handler", e);
      }
      this.success = false;
      throw new LoginException(e.toString());
    } catch (UnsupportedCallbackException e) {
      if (this.logger.isErrorEnabled()) {
        this.logger.error("Unsupported callback", e);
      }
      this.success = false;
      throw new LoginException(e.toString());
    }
  }


  /**
   * This will store the supplied name, password, and entry dn in the stored
   * state map. storePass must be set for this method to have any affect.
   *
   * @param  nameCb  to store
   * @param  passCb  to store
   * @param  loginDn  to store
   */
  @SuppressWarnings("unchecked")
  protected void storeCredentials(
    final NameCallback nameCb,
    final PasswordCallback passCb,
    final String loginDn)
  {
    if (this.storePass) {
      if (nameCb != null && nameCb.getName() != null) {
        this.sharedState.put(LOGIN_NAME, nameCb.getName());
      }
      if (passCb != null && passCb.getPassword() != null) {
        this.sharedState.put(LOGIN_PASSWORD, passCb.getPassword());
      }
      if (loginDn != null) {
        this.sharedState.put(LOGIN_DN, loginDn);
      }
    }
  }


  /**
   * This parses the supplied attributes and returns them as a list of <code>
   * LdapRole</code>s.
   *
   * @param  attributes  <code>Attributes</code>
   *
   * @return  <code>List</code>
   *
   * @throws  NamingException  if the attributes cannot be parsed
   */
  protected List<LdapRole> attributesToRoles(final Attributes attributes)
    throws NamingException
  {
    final List<LdapRole> roles = new ArrayList<LdapRole>();
    final LdapAttributes ldapAttrs = new LdapAttributes(attributes);
    for (LdapAttribute ldapAttr : ldapAttrs.getAttributes()) {
      for (String attrValue : ldapAttr.getStringValues()) {
        roles.add(new LdapRole(attrValue));
      }
    }
    return roles;
  }
}
/*
  $Id: AbstractLoginModule.java 466 2009-08-20 18:05:31Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 466 $
  Updated: $Date: 2009-08-20 11:05:31 -0700 (Thu, 20 Aug 2009) $
*/
package edu.vt.middleware.ldap.jaas;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import edu.vt.middleware.ldap.Authenticator;
import edu.vt.middleware.ldap.AuthenticatorConfig;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.LdapConfig;
import edu.vt.middleware.ldap.bean.LdapAttribute;
import edu.vt.middleware.ldap.bean.LdapAttributes;
import edu.vt.middleware.ldap.props.LdapProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <code>AbstractLoginModule</code> provides functionality common to ldap based
 * login modules.
 *
 * @author  Middleware Services
 * @version  $Revision: 466 $ $Date: 2009-08-20 11:05:31 -0700 (Thu, 20 Aug 2009) $
 */
public abstract class AbstractLoginModule implements LoginModule
{

  /** Constant for login name stored in shared state. */
  public static final String LOGIN_NAME = "javax.security.auth.login.name";

  /** Constant for entryDn stored in shared state. */
  public static final String LOGIN_DN =
    "edu.vt.middleware.ldap.jaas.login.entryDn";

  /** Constant for login password stored in shared state. */
  public static final String LOGIN_PASSWORD =
    "javax.security.auth.login.password";

  /** Regular expression for ldap properties to ignore. */
  private static final String IGNORE_LDAP_REGEX =
    "useFirstPass|tryFirstPass|storePass|setLdapPrincipal|" +
    "setLdapDnPrincipal|setLdapCredential|userRoleAttribute|" +
    "roleFilter|roleAttribute";

  /** Log for this class. */
  protected final Log logger = LogFactory.getLog(this.getClass());

  /** Initialized subject. */
  protected Subject subject;

  /** Initialized callback handler. */
  protected CallbackHandler callbackHandler;

  /** Shared state from other login module. */
  @SuppressWarnings("unchecked")
  protected Map sharedState;

  /** Whether credentials from the shared state should be used. */
  protected boolean useFirstPass;

  /**
   * Whether credentials from the shared state should be used if they are
   * available.
   */
  protected boolean tryFirstPass;

  /** Whether credentials should be stored in the shared state map. */
  protected boolean storePass;

  /** Whether ldap principal data should be set. */
  protected boolean setLdapPrincipal;

  /** Whether ldap dn principal data should be set. */
  protected boolean setLdapDnPrincipal;

  /** Whether ldap credential data should be set. */
  protected boolean setLdapCredential;

  /** Whether authentication was successful. */
  protected boolean success;

  /** Principals to add to the subject. */
  protected Set<Principal> principals;

  /** Credentials to add to the subject. */
  protected Set<LdapCredential> credentials;


  /** {@inheritDoc}. */
  public void initialize(
    final Subject subject,
    final CallbackHandler callbackHandler,
    final Map<String, ?> sharedState,
    final Map<String, ?> options)
  {
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("Begin initialize");
    }
    this.subject = subject;
    this.callbackHandler = callbackHandler;
    this.sharedState = sharedState;

    final Iterator<String> i = options.keySet().iterator();
    while (i.hasNext()) {
      final String key = i.next();
      final String value = (String) options.get(key);
      if (key.equalsIgnoreCase("useFirstPass")) {
        this.useFirstPass = Boolean.valueOf(value);
      } else if (key.equalsIgnoreCase("tryFirstPass")) {
        this.tryFirstPass = Boolean.valueOf(value);
      } else if (key.equalsIgnoreCase("storePass")) {
        this.storePass = Boolean.valueOf(value);
      } else if (key.equalsIgnoreCase("setLdapPrincipal")) {
        this.setLdapPrincipal = Boolean.valueOf(value);
      } else if (key.equalsIgnoreCase("setLdapDnPrincipal")) {
        this.setLdapDnPrincipal = Boolean.valueOf(value);
      } else if (key.equalsIgnoreCase("setLdapCredential")) {
        this.setLdapCredential = Boolean.valueOf(value);
      }
    }

    if (this.logger.isDebugEnabled()) {
      this.logger.debug("useFirstPass = " + this.useFirstPass);
      this.logger.debug("tryFirstPass = " + this.tryFirstPass);
      this.logger.debug("storePass = " + this.storePass);
      this.logger.debug("setLdapPrincipal = " + this.setLdapPrincipal);
      this.logger.debug("setLdapDnPrincipal = " + this.setLdapDnPrincipal);
      this.logger.debug("setLdapCredential = " + this.setLdapCredential);
    }

    this.principals = new TreeSet<Principal>();
    this.credentials = new HashSet<LdapCredential>();
  }


  /** {@inheritDoc}. */
  public abstract boolean login()
    throws LoginException;


  /** {@inheritDoc}. */
  public boolean commit()
    throws LoginException
  {
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("Begin commit");
    }
    if (this.success) {
      if (this.subject.isReadOnly()) {
        throw new LoginException("Subject is read-only.");
      }
      this.subject.getPrincipals().addAll(this.principals);
      if (this.logger.isDebugEnabled()) {
        this.logger.debug(
          "Committed the following principals: " + this.principals);
      }
      this.subject.getPrivateCredentials().addAll(this.credentials);
    }
    this.principals.clear();
    this.credentials.clear();
    return true;
  }


  /** {@inheritDoc}. */
  public boolean abort()
  {
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("Begin abort");
    }
    this.success = false;
    logout();
    return true;
  }


  /** {@inheritDoc}. */
  public boolean logout()
  {
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("Begin logout");
    }
    this.principals.clear();
    this.credentials.clear();

    final Iterator<LdapPrincipal> prinIter = this.subject.getPrincipals(
      LdapPrincipal.class).iterator();
    while (prinIter.hasNext()) {
      this.subject.getPrincipals().remove(prinIter.next());
    }

    final Iterator<LdapDnPrincipal> dnPrinIter = this.subject.getPrincipals(
      LdapDnPrincipal.class).iterator();
    while (dnPrinIter.hasNext()) {
      this.subject.getPrincipals().remove(dnPrinIter.next());
    }

    final Iterator<LdapRole> roleIter = this.subject.getPrincipals(
      LdapRole.class).iterator();
    while (roleIter.hasNext()) {
      this.subject.getPrincipals().remove(roleIter.next());
    }

    final Iterator<LdapCredential> credIter = this.subject
        .getPrivateCredentials(LdapCredential.class).iterator();
    while (credIter.hasNext()) {
      this.subject.getPrivateCredentials().remove(credIter.next());
    }

    return true;
  }


  /**
   * This constructs a new <code>Ldap</code> with the supplied jaas options.
   *
   * @param  options  <code>Map</code>
   *
   * @return  <code>Ldap</code>
   */
  public static Ldap createLdap(final Map<String, ?> options)
  {
    final LdapConfig ldapConfig = new LdapConfig();
    final LdapProperties ldapProperties = new LdapProperties(ldapConfig);
    final Iterator<String> i = options.keySet().iterator();
    while (i.hasNext()) {
      final String key = i.next();
      final String value = (String) options.get(key);
      if (!key.matches(IGNORE_LDAP_REGEX)) {
        ldapProperties.setProperty(key, value);
      }
    }
    ldapProperties.configure();
    return new Ldap(ldapConfig);
  }


  /**
   * This constructs a new <code>Authenticator</code> with the supplied jaas
   * options.
   *
   * @param  options  <code>Map</code>
   *
   * @return  <code>Authenticator</code>
   */
  public static Authenticator createAuthenticator(final Map<String, ?> options)
  {
    final AuthenticatorConfig authConfig = new AuthenticatorConfig();
    final LdapProperties authProperties = new LdapProperties(authConfig);
    final Iterator<String> i = options.keySet().iterator();
    while (i.hasNext()) {
      final String key = i.next();
      final String value = (String) options.get(key);
      if (!key.matches(IGNORE_LDAP_REGEX)) {
        authProperties.setProperty(key, value);
      }
    }
    authProperties.configure();
    return new Authenticator(authConfig);
  }


  /**
   * This attempts to retrieve credentials for the supplied name and password
   * callbacks. If useFirstPass or tryFirstPass is set, then name and password
   * data is retrieved from shared state. Otherwise a callback handler is used
   * to get the data. Set useCallback to force a callback handler to be used.
   *
   * @param  nameCb  to set name for
   * @param  passCb  to set password for
   * @param  useCallback  whether to force a callback handler
   *
   * @throws  LoginException  if the callback handler fails
   */
  protected void getCredentials(
    final NameCallback nameCb,
    final PasswordCallback passCb,
    final boolean useCallback)
    throws LoginException
  {
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("Begin getCredentials");
      this.logger.trace("  useFistPass = " + this.useFirstPass);
      this.logger.trace("  tryFistPass = " + this.tryFirstPass);
      this.logger.trace("  useCallback = " + useCallback);
      this.logger.trace(
        "  callbackhandler class = " +
        this.callbackHandler.getClass().getName());
      this.logger.trace(
        "  name callback class = " + nameCb.getClass().getName());
      this.logger.trace(
        "  password callback class = " + passCb.getClass().getName());
    }
    try {
      if ((this.useFirstPass || this.tryFirstPass) && !useCallback) {
        nameCb.setName((String) this.sharedState.get(LOGIN_NAME));
        passCb.setPassword((char[]) this.sharedState.get(LOGIN_PASSWORD));
      } else if (this.callbackHandler != null) {
        this.callbackHandler.handle(new Callback[] {nameCb, passCb});
      } else {
        throw new LoginException(
          "No CallbackHandler available. " +
          "Set useFirstPass, tryFirstPass, or provide a CallbackHandler");
      }
    } catch (IOException e) {
      if (this.logger.isErrorEnabled()) {
        this.logger.error("Error reading data from callback handler", e);
      }
      this.success = false;
      throw new LoginException(e.toString());
    } catch (UnsupportedCallbackException e) {
      if (this.logger.isErrorEnabled()) {
        this.logger.error("Unsupported callback", e);
      }
      this.success = false;
      throw new LoginException(e.toString());
    }
  }


  /**
   * This will store the supplied name, password, and entry dn in the stored
   * state map. storePass must be set for this method to have any affect.
   *
   * @param  nameCb  to store
   * @param  passCb  to store
   * @param  loginDn  to store
   */
  @SuppressWarnings("unchecked")
  protected void storeCredentials(
    final NameCallback nameCb,
    final PasswordCallback passCb,
    final String loginDn)
  {
    if (this.storePass) {
      if (nameCb != null && nameCb.getName() != null) {
        this.sharedState.put(LOGIN_NAME, nameCb.getName());
      }
      if (passCb != null && passCb.getPassword() != null) {
        this.sharedState.put(LOGIN_PASSWORD, passCb.getPassword());
      }
      if (loginDn != null) {
        this.sharedState.put(LOGIN_DN, loginDn);
      }
    }
  }


  /**
   * This parses the supplied attributes and returns them as a list of <code>
   * LdapRole</code>s.
   *
   * @param  attributes  <code>Attributes</code>
   *
   * @return  <code>List</code>
   *
   * @throws  NamingException  if the attributes cannot be parsed
   */
  protected List<LdapRole> attributesToRoles(final Attributes attributes)
    throws NamingException
  {
    final List<LdapRole> roles = new ArrayList<LdapRole>();
    final LdapAttributes ldapAttrs = new LdapAttributes(attributes);
    for (LdapAttribute ldapAttr : ldapAttrs.getAttributes()) {
      for (String attrValue : ldapAttr.getStringValues()) {
        roles.add(new LdapRole(attrValue));
      }
    }
    return roles;
  }
}
