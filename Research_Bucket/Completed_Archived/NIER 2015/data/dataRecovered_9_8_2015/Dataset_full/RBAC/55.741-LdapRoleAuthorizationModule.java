/*
  $Id: LdapRoleAuthorizationModule.java 639 2009-09-18 17:55:42Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 639 $
  Updated: $Date: 2009-09-18 10:55:42 -0700 (Fri, 18 Sep 2009) $
*/
package edu.vt.middleware.ldap.jaas;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.naming.NamingException;
import javax.naming.directory.SearchResult;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import com.sun.security.auth.callback.TextCallbackHandler;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.SearchFilter;

/**
 * <code>LdapRoleAuthorizationModule</code> provides a JAAS authentication hook
 * into LDAP roles. No authentication is performed in this module. Role data is
 * set for the login name in the shared state or for the name returned by the
 * CallbackHandler.
 *
 * @author  Middleware Services
 * @version  $Revision: 639 $ $Date: 2009-09-18 10:55:42 -0700 (Fri, 18 Sep 2009) $
 */
public class LdapRoleAuthorizationModule extends AbstractLoginModule
  implements LoginModule
{

  /** Ldap filter for role searches. */
  private String roleFilter;

  /** Role attribute to add to role data. */
  private String[] roleAttribute;

  /** Ldap to use for searching roles against the LDAP. */
  private Ldap ldap;


  /** {@inheritDoc}. */
  public void initialize(
    final Subject subject,
    final CallbackHandler callbackHandler,
    final Map<String, ?> sharedState,
    final Map<String, ?> options)
  {
    super.initialize(subject, callbackHandler, sharedState, options);

    final Iterator<String> i = options.keySet().iterator();
    while (i.hasNext()) {
      final String key = i.next();
      final String value = (String) options.get(key);
      if (key.equalsIgnoreCase("roleFilter")) {
        this.roleFilter = value;
      } else if (key.equalsIgnoreCase("roleAttribute")) {
        this.roleAttribute = value.split(",");
      }
    }

    if (this.logger.isDebugEnabled()) {
      this.logger.debug("roleFilter = " + this.roleFilter);
      this.logger.debug(
        "roleAttribute = " +
        (this.roleAttribute == null ? "null"
                                    : Arrays.asList(this.roleAttribute)));
    }

    this.ldap = createLdap(options);
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("Created ldap: " + this.ldap.getLdapConfig());
    }
  }


  /** {@inheritDoc}. */
  public boolean login()
    throws LoginException
  {
    try {
      final NameCallback nameCb = new NameCallback("Enter user: ");
      final PasswordCallback passCb = new PasswordCallback(
        "Enter user password: ",
        false);
      this.getCredentials(nameCb, passCb, false);

      if (nameCb.getName() == null && this.tryFirstPass) {
        this.getCredentials(nameCb, passCb, true);
      }

      final String loginName = nameCb.getName();
      if (loginName != null && this.setLdapPrincipal) {
        this.principals.add(new LdapPrincipal(loginName));
        this.success = true;
      }

      final String loginDn = (String) this.sharedState.get(LOGIN_DN);
      if (loginDn != null && this.setLdapDnPrincipal) {
        this.principals.add(new LdapDnPrincipal(loginDn));
        this.success = true;
      }

      final List<LdapRole> roles = new ArrayList<LdapRole>();
      if (this.roleFilter != null) {
        final Object[] filterArgs = new Object[] {loginDn, loginName, };
        final Iterator<SearchResult> results = this.ldap.search(
          new SearchFilter(this.roleFilter, filterArgs),
          this.roleAttribute);
        while (results.hasNext()) {
          final SearchResult sr = results.next();
          roles.addAll(this.attributesToRoles(sr.getAttributes()));
        }
      }
      if (!roles.isEmpty()) {
        this.principals.addAll(roles);
        this.success = true;
      }
      this.storeCredentials(nameCb, passCb, null);
    } catch (NamingException e) {
      this.success = false;
      throw new LoginException(e.toString());
    } finally {
      this.ldap.close();
    }
    return true;
  }


  /**
   * This provides command line access to a <code>LdapRoleLoginModule</code>.
   *
   * @param  args  <code>String[]</code>
   *
   * @throws  Exception  if an error occurs
   */
  public static void main(final String[] args)
    throws Exception
  {
    String name = "vt-ldap-role";
    if (args.length > 0) {
      name = args[0];
    }

    final LoginContext lc = new LoginContext(name, new TextCallbackHandler());
    lc.login();
    System.out.println("Authorization succeeded");

    final Set<Principal> principals = lc.getSubject().getPrincipals();
    System.out.println("Subject Principal(s): ");

    final Iterator<Principal> i = principals.iterator();
    while (i.hasNext()) {
      final Principal p = i.next();
      System.out.println("  " + p.getName());
    }
    lc.logout();
  }
}
/*
  $Id: LdapRoleAuthorizationModule.java 466 2009-08-20 18:05:31Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 466 $
  Updated: $Date: 2009-08-20 11:05:31 -0700 (Thu, 20 Aug 2009) $
*/
package edu.vt.middleware.ldap.jaas;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.naming.NamingException;
import javax.naming.directory.SearchResult;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import com.sun.security.auth.callback.TextCallbackHandler;
import edu.vt.middleware.ldap.Ldap;

/**
 * <code>LdapRoleAuthorizationModule</code> provides a JAAS authentication hook
 * into LDAP roles. No authentication is performed in this module. Role data is
 * set for the login name in the shared state or for the name returned by the
 * CallbackHandler.
 *
 * @author  Middleware Services
 * @version  $Revision: 466 $ $Date: 2009-08-20 11:05:31 -0700 (Thu, 20 Aug 2009) $
 */
public class LdapRoleAuthorizationModule extends AbstractLoginModule
  implements LoginModule
{

  /** Ldap filter for role searches. */
  private String roleFilter;

  /** Role attribute to add to role data. */
  private String[] roleAttribute;

  /** Ldap to use for searching roles against the LDAP. */
  private Ldap ldap;


  /** {@inheritDoc}. */
  public void initialize(
    final Subject subject,
    final CallbackHandler callbackHandler,
    final Map<String, ?> sharedState,
    final Map<String, ?> options)
  {
    super.initialize(subject, callbackHandler, sharedState, options);

    final Iterator<String> i = options.keySet().iterator();
    while (i.hasNext()) {
      final String key = i.next();
      final String value = (String) options.get(key);
      if (key.equalsIgnoreCase("roleFilter")) {
        this.roleFilter = value;
      } else if (key.equalsIgnoreCase("roleAttribute")) {
        this.roleAttribute = value.split(",");
      }
    }

    if (this.logger.isDebugEnabled()) {
      this.logger.debug("roleFilter = " + this.roleFilter);
      this.logger.debug(
        "roleAttribute = " +
        (this.roleAttribute == null ? "null"
                                    : Arrays.asList(this.roleAttribute)));
    }

    this.ldap = createLdap(options);
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("Created ldap: " + this.ldap.getLdapConfig());
    }
  }


  /** {@inheritDoc}. */
  public boolean login()
    throws LoginException
  {
    try {
      final NameCallback nameCb = new NameCallback("Enter user: ");
      final PasswordCallback passCb = new PasswordCallback(
        "Enter user password: ",
        false);
      this.getCredentials(nameCb, passCb, false);

      if (nameCb.getName() == null && this.tryFirstPass) {
        this.getCredentials(nameCb, passCb, true);
      }

      final String loginName = nameCb.getName();
      if (loginName != null && this.setLdapPrincipal) {
        this.principals.add(new LdapPrincipal(loginName));
        this.success = true;
      }

      final String loginDn = (String) this.sharedState.get(LOGIN_DN);
      if (loginDn != null && this.setLdapDnPrincipal) {
        this.principals.add(new LdapDnPrincipal(loginDn));
        this.success = true;
      }

      final List<LdapRole> roles = new ArrayList<LdapRole>();
      if (this.roleFilter != null) {
        final Object[] filterArgs = new Object[] {loginDn, loginName, };
        final Iterator<SearchResult> results = this.ldap.search(
          this.roleFilter,
          filterArgs,
          this.roleAttribute);
        while (results.hasNext()) {
          final SearchResult sr = results.next();
          roles.addAll(this.attributesToRoles(sr.getAttributes()));
        }
      }
      if (!roles.isEmpty()) {
        this.principals.addAll(roles);
        this.success = true;
      }
      this.storeCredentials(nameCb, passCb, null);
    } catch (NamingException e) {
      this.success = false;
      throw new LoginException(e.toString());
    } finally {
      this.ldap.close();
    }
    return true;
  }


  /**
   * This provides command line access to a <code>LdapRoleLoginModule</code>.
   *
   * @param  args  <code>String[]</code>
   *
   * @throws  Exception  if an error occurs
   */
  public static void main(final String[] args)
    throws Exception
  {
    String name = "vt-ldap-role";
    if (args.length > 0) {
      name = args[0];
    }

    final LoginContext lc = new LoginContext(name, new TextCallbackHandler());
    lc.login();
    System.out.println("Authorization succeeded");

    final Set<Principal> principals = lc.getSubject().getPrincipals();
    System.out.println("Subject Principal(s): ");

    final Iterator<Principal> i = principals.iterator();
    while (i.hasNext()) {
      final Principal p = i.next();
      System.out.println("  " + p.getName());
    }
    lc.logout();
  }
}
