/*
  $Id: LdapDnAuthorizationModule.java 466 2009-08-20 18:05:31Z dfisher $

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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import com.sun.security.auth.callback.TextCallbackHandler;
import edu.vt.middleware.ldap.Authenticator;

/**
 * <code>LdapDnAuthorizationModule</code> provides a JAAS authentication hook
 * into LDAP DNs. No authentication is performed in this module. The LDAP entry
 * dn can be stored and shared with other modules.
 *
 * @author  Middleware Services
 * @version  $Revision: 466 $ $Date: 2009-08-20 11:05:31 -0700 (Thu, 20 Aug 2009) $
 */
public class LdapDnAuthorizationModule extends AbstractLoginModule
  implements LoginModule
{

  /** Authenticator to use against the LDAP. */
  private Authenticator auth;


  /** {@inheritDoc}. */
  public void initialize(
    final Subject subject,
    final CallbackHandler callbackHandler,
    final Map<String, ?> sharedState,
    final Map<String, ?> options)
  {
    super.initialize(subject, callbackHandler, sharedState, options);

    this.auth = createAuthenticator(options);
    if (this.logger.isDebugEnabled()) {
      this.logger.debug(
        "Created authenticator: " + this.auth.getAuthenticatorConfig());
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

      final String loginDn = this.auth.getDn(nameCb.getName());
      if (loginDn != null && this.setLdapDnPrincipal) {
        this.principals.add(new LdapDnPrincipal(loginDn));
        this.success = true;
      }
      this.storeCredentials(nameCb, passCb, loginDn);
    } catch (NamingException e) {
      this.success = false;
      throw new LoginException(e.toString());
    } finally {
      this.auth.close();
    }
    return true;
  }


  /**
   * This provides command line access to a <code>LdapLoginModule</code>.
   *
   * @param  args  <code>String[]</code>
   *
   * @throws  Exception  if an error occurs
   */
  public static void main(final String[] args)
    throws Exception
  {
    String name = "vt-ldap-dn";
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
  $Id: LdapDnAuthorizationModule.java 466 2009-08-20 18:05:31Z dfisher $

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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import com.sun.security.auth.callback.TextCallbackHandler;
import edu.vt.middleware.ldap.Authenticator;

/**
 * <code>LdapDnAuthorizationModule</code> provides a JAAS authentication hook
 * into LDAP DNs. No authentication is performed in this module. The LDAP entry
 * dn can be stored and shared with other modules.
 *
 * @author  Middleware Services
 * @version  $Revision: 466 $ $Date: 2009-08-20 11:05:31 -0700 (Thu, 20 Aug 2009) $
 */
public class LdapDnAuthorizationModule extends AbstractLoginModule
  implements LoginModule
{

  /** Authenticator to use against the LDAP. */
  private Authenticator auth;


  /** {@inheritDoc}. */
  public void initialize(
    final Subject subject,
    final CallbackHandler callbackHandler,
    final Map<String, ?> sharedState,
    final Map<String, ?> options)
  {
    super.initialize(subject, callbackHandler, sharedState, options);

    this.auth = createAuthenticator(options);
    if (this.logger.isDebugEnabled()) {
      this.logger.debug(
        "Created authenticator: " + this.auth.getAuthenticatorConfig());
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

      final String loginDn = this.auth.getDn(nameCb.getName());
      if (loginDn != null && this.setLdapDnPrincipal) {
        this.principals.add(new LdapDnPrincipal(loginDn));
        this.success = true;
      }
      this.storeCredentials(nameCb, passCb, loginDn);
    } catch (NamingException e) {
      this.success = false;
      throw new LoginException(e.toString());
    } finally {
      this.auth.close();
    }
    return true;
  }


  /**
   * This provides command line access to a <code>LdapLoginModule</code>.
   *
   * @param  args  <code>String[]</code>
   *
   * @throws  Exception  if an error occurs
   */
  public static void main(final String[] args)
    throws Exception
  {
    String name = "vt-ldap-dn";
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
