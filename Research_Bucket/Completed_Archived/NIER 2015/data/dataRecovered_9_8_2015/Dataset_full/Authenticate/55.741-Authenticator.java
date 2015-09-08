/*
  $Id: Authenticator.java 466 2009-08-20 18:05:31Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 466 $
  Updated: $Date: 2009-08-20 11:05:31 -0700 (Thu, 20 Aug 2009) $
*/
package edu.vt.middleware.ldap;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.StartTlsResponse;
import edu.vt.middleware.ldap.handler.AuthenticationCriteria;
import edu.vt.middleware.ldap.handler.AuthenticationResultHandler;

/**
 * <code>Authenticator</code> contains functions for authenticating a user
 * against a LDAP.
 *
 * @author  Middleware Services
 * @version  $Revision: 466 $ $Date: 2009-08-20 11:05:31 -0700 (Thu, 20 Aug 2009) $
 */
public class Authenticator extends AbstractLdap<AuthenticatorConfig>
{

  /** serial version uid. */
  private static final long serialVersionUID = -5125279149546967709L;

  /** Default constructor. */
  public Authenticator() {}


  /**
   * This will create a new <code>Authenticator</code> with the supplied <code>
   * AuthenticatorConfig</code>.
   *
   * @param  authConfig  <code>AuthenticatorConfig</code>
   */
  public Authenticator(final AuthenticatorConfig authConfig)
  {
    this.setAuthenticatorConfig(authConfig);
  }


  /**
   * This will set the config parameters of this <code>Authenticator</code>.
   *
   * @param  authConfig  <code>AuthenticatorConfig</code>
   */
  public void setAuthenticatorConfig(final AuthenticatorConfig authConfig)
  {
    super.setLdapConfig(authConfig);
  }


  /**
   * This returns the <code>AuthenticatorConfig</code> of the <code>
   * Authenticator</code>.
   *
   * @return  <code>AuthenticatorConfig</code>
   */
  public AuthenticatorConfig getAuthenticatorConfig()
  {
    return this.config;
  }


  /**
   * This will set the config parameters of this <code>Authenticator</code>
   * using the default properties file, which must be located in your classpath.
   */
  public void loadFromProperties()
  {
    this.setAuthenticatorConfig(AuthenticatorConfig.createFromProperties(null));
  }


  /**
   * This will set the config parameters of this <code>Authenticator</code>
   * using the supplied input stream.
   *
   * @param  is  <code>InputStream</code>
   */
  public void loadFromProperties(final InputStream is)
  {
    this.setAuthenticatorConfig(AuthenticatorConfig.createFromProperties(is));
  }


  /**
   * This will attempt to find the dn for the supplied user. {@link
   * AuthenticatorConfig#getUserField()} is used to look up the dn. If more than
   * one entry matches the search, the first result is used. If {@link
   * AuthenticatorConfig#setConstructDn(boolean)} has been set to true, then the
   * dn will be created by combining the userField and the base dn.
   *
   * @param  user  <code>String</code> to find dn for
   *
   * @return  <code>String</code> - user's dn
   *
   * @throws  NamingException  if the LDAP search fails
   */
  public String getDn(final String user)
    throws NamingException
  {
    String dn = null;
    if (user != null && !user.equals("")) {
      if (
        this.config.getUserField() == null ||
          this.config.getUserField().length == 0) {
        if (this.logger.isErrorEnabled()) {
          this.logger.error("Invalid userField, cannot be null or empty.");
        }
      } else if (this.config.getConstructDn()) {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug("Constructing DN from first userfield and base");
        }
        dn = this.config.getUserField()[0] + "=" + user + "," +
          this.config.getBase();
      } else {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug("Looking up DN from userfield and base");
        }

        Iterator<SearchResult> answer = null;
        if (this.config.getSubtreeSearch()) {
          final StringBuffer searchFilter = new StringBuffer();
          if (this.config.getUserField().length > 1) {
            searchFilter.append("(|");
            for (int i = 0; i < this.config.getUserField().length; i++) {
              searchFilter.append("(").append(this.config.getUserField()[i])
                .append("=").append(user).append(")");
            }
            searchFilter.append(")");
          } else {
            searchFilter.append("(").append(this.config.getUserField()[0])
              .append("=").append(user).append(")");
          }
          answer = this.search(
            this.config.getBase(),
            searchFilter.toString(),
            null,
            new String[] {},
            this.config.getSearchResultHandlers());
        } else {
          for (int i = 0; i < this.config.getUserField().length; i++) {
            answer = this.searchAttributes(
              this.config.getBase(),
              new BasicAttributes(this.config.getUserField()[i], user),
              new String[] {},
              this.config.getSearchResultHandlers());
            if (answer != null && answer.hasNext()) {
              break;
            }
          }
        }
        // return first match, otherwise user doesn't exist
        if (answer != null && answer.hasNext()) {
          final SearchResult sr = answer.next();
          dn = sr.getName();
        } else {
          if (this.logger.isInfoEnabled()) {
            this.logger.info(
              "Search for user: " + user + " failed using attribute(s): " +
              (this.config.getUserField() == null
                ? "null" : Arrays.asList(this.config.getUserField())));
          }
        }
      }
    } else {
      if (this.logger.isDebugEnabled()) {
        this.logger.debug("Input was empty or null");
      }
    }
    return dn;
  }


  /**
   * This will authenticate credentials by binding to the LDAP using parameters
   * given by {@link AuthenticatorConfig#setUser} and {@link
   * AuthenticatorConfig#setCredential}. See {@link #authenticate(String,
   * Object)} If {@link AuthenticatorConfig#setAuthorizationFilter} has been
   * called, then it will be used to authorize the user by performing an ldap
   * compare. See {@link #authenticate(String, Object)}
   *
   * @return  <code>boolean</code> - whether the bind succeeded
   *
   * @throws  NamingException  if the authentication fails for any other reason
   * than invalid credentials
   */
  public boolean authenticate()
    throws NamingException
  {
    return
      this.authenticate(this.config.getUser(), this.config.getCredential());
  }


  /**
   * This will authenticate credentials by binding to the LDAP with the supplied
   * user and credential. The user's DN will be looked up before performing the
   * bind by searching on the user field, unless constructDn has been set. The
   * user field default is set to 'uid', so to authenticate 'dfisher', uid must
   * equal dfisher in the LDAP. If {@link
   * AuthenticatorConfig#setAuthorizationFilter} has been called, then it will
   * be used to authorize the user by performing an ldap compare. See {@link
   * #authenticateAndAuthorize( String, Object, String,
   * AuthenticationResultHandler...)}
   *
   * @param  user  <code>String</code> username for bind
   * @param  credential  <code>Object</code> credential for bind
   *
   * @return  <code>boolean</code> - whether the bind succeeded
   *
   * @throws  NamingException  if the authentication fails for any other reason
   * than invalid credentials
   */
  public boolean authenticate(final String user, final Object credential)
    throws NamingException
  {
    return
      this.authenticate(user, credential, this.config.getAuthorizationFilter());
  }


  /**
   * This will authenticate credentials by binding to the LDAP with the supplied
   * user and credential. The user's DN will be looked up before performing the
   * bind by searching on the user field, unless constructDn has been set. The
   * user field default is set to 'uid', so to authenticate 'dfisher', uid must
   * equal dfisher in the LDAP. See {@link #authenticateAndAuthorize(String,
   * Object, String, AuthenticationResultHandler...)}
   *
   * @param  user  <code>String</code> username for bind
   * @param  credential  <code>Object</code> credential for bind
   * @param  filter  <code>String</code> to authorize user
   *
   * @return  <code>boolean</code> - whether the bind succeeded
   *
   * @throws  NamingException  if the authentication fails for any other reason
   * than invalid credentials
   */
  public boolean authenticate(
    final String user,
    final Object credential,
    final String filter)
    throws NamingException
  {
    return
      this.authenticateAndAuthorize(
        this.getDn(user),
        credential,
        filter,
        this.config.getAuthenticationResultHandlers());
  }


  /**
   * This will authenticate credentials by binding to the LDAP with the supplied
   * user and credential. The user's DN will be looked up before performing the
   * bind by searching on the user field, unless constructDn has been set. The
   * user field default is set to 'uid', so to authenticate 'dfisher', uid must
   * equal dfisher in the LDAP. See {@link #authenticateAndAuthorize(String,
   * Object, String, AuthenticationResultHandler...)}
   *
   * @param  user  <code>String</code> username for bind
   * @param  credential  <code>Object</code> credential for bind
   * @param  filter  <code>String</code> to authorize user
   * @param  handler  <code>AuthenticationResultHandler[]</code> to post process
   * authentication results
   *
   * @return  <code>boolean</code> - whether the bind succeeded
   *
   * @throws  NamingException  if the authentication fails for any other reason
   * than invalid credentials
   */
  public boolean authenticate(
    final String user,
    final Object credential,
    final String filter,
    final AuthenticationResultHandler... handler)
    throws NamingException
  {
    return
      this.authenticateAndAuthorize(
        this.getDn(user),
        credential,
        filter,
        handler);
  }


  /**
   * This will authenticate credentials by binding to the LDAP with the supplied
   * dn and credential. See {@link #authenticateAndAuthorize(
   * String,Object,String,boolean,String[],AuthenticationResultHandler...)}.
   *
   * @param  dn  <code>String</code> for bind
   * @param  credential  <code>Object</code> for bind
   * @param  filter  <code>String</code> to authorize user
   * @param  handler  <code>AuthenticationResultHandler[]</code> to post process
   * authentication results
   *
   * @return  <code>boolean</code> - whether the bind succeeded
   *
   * @throws  NamingException  if the authentication fails for any other reason
   * than invalid credentials
   */
  private boolean authenticateAndAuthorize(
    final String dn,
    final Object credential,
    final String filter,
    final AuthenticationResultHandler... handler)
    throws NamingException
  {
    boolean success = false;
    try {
      this.authenticateAndAuthorize(
        dn,
        credential,
        filter,
        false,
        null,
        handler);
      success = true;
    } catch (AuthenticationException e) {
      if (this.logger.isDebugEnabled()) {
        this.logger.debug("Authentication failed for dn: " + dn, e);
      }
    }
    return success;
  }


  /**
   * This will authenticate credentials by binding to the LDAP using parameters
   * given by {@link AuthenticatorConfig#setUser} and {@link
   * AuthenticatorConfig#setCredential}. If {@link
   * AuthenticatorConfig#setAuthorizationFilter} has been called, then it will
   * be used to authorize the user by performing an ldap compare. See {@link
   * #authenticate(String,Object,String[])}
   *
   * @param  retAttrs  <code>String[]</code> attributes to return
   *
   * @return  <code>Attributes</code> - of authenticated user
   *
   * @throws  NamingException  if any of the ldap operations fail
   */
  public Attributes authenticate(final String[] retAttrs)
    throws NamingException
  {
    return
      this.authenticate(
        this.config.getUser(),
        this.config.getCredential(),
        retAttrs);
  }


  /**
   * This will authenticate credentials by binding to the LDAP with the supplied
   * user and credential. The user's DN will be looked up before performing the
   * bind by searching on the user field, unless constructDn has been set. The
   * user field default is set to 'uid', so to authenticate 'dfisher', uid must
   * equal dfisher in the LDAP. If {@link
   * AuthenticatorConfig#setAuthorizationFilter} has been called, then it will
   * be used to authorize the user by performing an ldap compare. See {@link
   * #authenticate(String, Object, String, String[])}
   *
   * @param  user  <code>String</code> username for bind
   * @param  credential  <code>Object</code> credential for bind
   * @param  retAttrs  <code>String[]</code> to return
   *
   * @return  <code>Attributes</code> - of authenticated user
   *
   * @throws  NamingException  if any of the ldap operations fail
   */
  public Attributes authenticate(
    final String user,
    final Object credential,
    final String[] retAttrs)
    throws NamingException
  {
    return
      this.authenticate(
        user,
        credential,
        this.config.getAuthorizationFilter(),
        retAttrs);
  }


  /**
   * This will authenticate credentials by binding to the LDAP with the supplied
   * user and credential. The user's DN will be looked up before performing the
   * bind by searching on the user field, unless constructDn has been set. The
   * user field default is set to 'uid', so to authenticate 'dfisher', uid must
   * equal dfisher in the LDAP. See {@link #authenticateAndAuthorize(String,
   * Object, String, boolean, String[], AuthenticationResultHandler...)}
   *
   * @param  user  <code>String</code> username for bind
   * @param  credential  <code>Object</code> credential for bind
   * @param  filter  <code>String</code> to authorize user
   * @param  retAttrs  <code>String[]</code> to return
   *
   * @return  <code>Attributes</code> - of authenticated user
   *
   * @throws  NamingException  if any of the ldap operations fail
   */
  public Attributes authenticate(
    final String user,
    final Object credential,
    final String filter,
    final String[] retAttrs)
    throws NamingException
  {
    return
      this.authenticateAndAuthorize(
        this.getDn(user),
        credential,
        filter,
        true,
        retAttrs,
        this.config.getAuthenticationResultHandlers());
  }


  /**
   * This will authenticate credentials by binding to the LDAP with the supplied
   * user and credential. The user's DN will be looked up before performing the
   * bind by searching on the user field, unless constructDn has been set. The
   * user field default is set to 'uid', so to authenticate 'dfisher', uid must
   * equal dfisher in the LDAP. See {@link #authenticateAndAuthorize(String,
   * Object, String, boolean, String[], AuthenticationResultHandler...)}
   *
   * @param  user  <code>String</code> username for bind
   * @param  credential  <code>Object</code> credential for bind
   * @param  filter  <code>String</code> to authorize user
   * @param  retAttrs  <code>String[]</code> to return
   * @param  handler  <code>AuthenticationResultHandler[]</code> to post process
   * authentication results
   *
   * @return  <code>Attributes</code> - of authenticated user
   *
   * @throws  NamingException  if any of the ldap operations fail
   */
  public Attributes authenticate(
    final String user,
    final Object credential,
    final String filter,
    final String[] retAttrs,
    final AuthenticationResultHandler... handler)
    throws NamingException
  {
    return
      this.authenticateAndAuthorize(
        this.getDn(user),
        credential,
        filter,
        true,
        retAttrs,
        handler);
  }


  /**
   * This will authenticate credentials by binding to the LDAP with the supplied
   * dn and credential. Authentication will never succeed if {@link
   * LdapConfig#getAuthtype()} is set to 'none'. If the supplied filter is not
   * null, then it will be used to do an ldap compare operation against the
   * supplied user. If the compare does not succeed, then this method will
   * return false. The compare is done with the same context used to
   * authenticate the user, if the user does not have permission to view the
   * supplied attributes then the authorization will fail. If retAttrs is null
   * then all user attributes will be returned. If retAttrs is an empty array
   * then no attributes will be returned. This method returns null if
   * authentication or authorization fails.
   *
   * @param  dn  <code>String</code> for bind
   * @param  credential  <code>Object</code> for bind
   * @param  filter  <code>String</code> to authorize user
   * @param  searchAttrs  <code>boolean</code> whether to perform attribute
   * search
   * @param  retAttrs  <code>String[]</code> user attributes to return
   * @param  handler  <code>AuthenticationResultHandler[]</code> to post process
   * authentication results
   *
   * @return  <code>Attribute</code> - belonging to the supplied user, returns
   * null if searchAttrs is false
   *
   * @throws  NamingException  if any of the ldap operations fail
   * @throws  AuthenticationException  if authentication or authorization fails
   */
  private Attributes authenticateAndAuthorize(
    final String dn,
    final Object credential,
    final String filter,
    final boolean searchAttrs,
    final String[] retAttrs,
    final AuthenticationResultHandler... handler)
    throws NamingException
  {
    // check the authentication type
    final String authtype = this.config.getAuthtype();
    if (authtype.equalsIgnoreCase(LdapConstants.NONE_AUTHTYPE)) {
      throw new AuthenticationException(
        "Cannot authenticate dn, authtype is 'none'");
    }

    // check the credential
    if (!LdapUtil.checkCredential(credential)) {
      throw new AuthenticationException(
        "Cannot authenticate dn, invalid credential");
    }

    // check the dn
    if (dn == null || dn.equals("")) {
      throw new AuthenticationException("Cannot authenticate dn, invalid dn");
    }

    Attributes userAttributes = null;

    // attempt to bind as this dn
    final StartTlsResponse tls = null;
    LdapContext ctx = null;
    for (int i = 0; i <= LdapConstants.OPERATION_RETRY; i++) {
      try {
        final AuthenticationCriteria ac = new AuthenticationCriteria(dn);
        ac.setCredential(credential);
        try {
          ctx = this.bind(dn, credential, tls);
          if (this.logger.isInfoEnabled()) {
            this.logger.info("Authentication succeeded for dn: " + dn);
          }
          if (handler != null && handler.length > 0) {
            for (AuthenticationResultHandler ah : handler) {
              ah.process(ac, true);
            }
          }
        } catch (AuthenticationException e) {
          if (this.logger.isInfoEnabled()) {
            this.logger.info("Authentication failed for dn: " + dn);
          }
          if (handler != null && handler.length > 0) {
            for (AuthenticationResultHandler ah : handler) {
              ah.process(ac, false);
            }
          }
          throw e;
        }
        // authentication succeeded, perform authorization if supplied
        if (filter != null) {
          // perform ldap compare operation
          NamingEnumeration<SearchResult> results = null;
          try {
            results = ctx.search(
              dn,
              filter,
              LdapConfig.getCompareSearchControls());
            if (results.hasMore()) {
              if (this.logger.isInfoEnabled()) {
                this.logger.info(
                  "Authorization succeeded for dn: " + dn + " with filter: " +
                  filter);
              }
            } else {
              if (this.logger.isInfoEnabled()) {
                this.logger.info(
                  "Authorization failed for dn: " + dn + " with filter: " +
                  filter);
              }
              throw new AuthenticationException(
                "Authorization failed for dn: " + dn + " with filter: " +
                filter);
            }
          } finally {
            if (results != null) {
              results.close();
            }
          }
        }
        if (searchAttrs) {
          if (this.logger.isDebugEnabled()) {
            this.logger.debug("Returning attributes: ");
            this.logger.debug(
              "    " +
              (retAttrs == null ? "all attributes" : Arrays.asList(retAttrs)));
          }
          userAttributes = ctx.getAttributes(dn, retAttrs);
        }
        break;
      } catch (CommunicationException e) {
        if (i == LdapConstants.OPERATION_RETRY) {
          throw e;
        }
        if (this.logger.isWarnEnabled()) {
          this.logger.warn(
            "Error while communicating with the LDAP, retrying",
            e);
        }
      } finally {
        this.stopTls(tls);
        if (ctx != null) {
          ctx.close();
        }
      }
    }

    return userAttributes;
  }


  /**
   * This will authenticate credentials by binding to the LDAP with the supplied
   * dn and credential. If {@link AuthenticatorConfig#setAuthorizationFilter}
   * has been called, then it will be used to authorize the user by performing
   * an ldap compare. See {@link #authenticateAndAuthorize(String, Object,
   * String, AuthenticationResultHandler...)}
   *
   * @param  dn  <code>String</code> dn for bind
   * @param  credential  <code>Object</code> credential for bind
   *
   * @return  <code>boolean</code> - whether the bind succeeded
   *
   * @throws  NamingException  if the authentication fails for any other reason
   * than invalid credentials
   */
  public boolean authenticateDn(final String dn, final Object credential)
    throws NamingException
  {
    return
      this.authenticateDn(dn, credential, this.config.getAuthorizationFilter());
  }


  /**
   * This will authenticate credentials by binding to the LDAP with the supplied
   * dn and credential. See {@link #authenticateAndAuthorize(String, Object,
   * String, AuthenticationResultHandler...)}
   *
   * @param  dn  <code>String</code> dn for bind
   * @param  credential  <code>Object</code> credential for bind
   * @param  filter  <code>String</code> to authorize user
   *
   * @return  <code>boolean</code> - whether the bind succeeded
   *
   * @throws  NamingException  if the authentication fails for any other reason
   * than invalid credentials
   */
  public boolean authenticateDn(
    final String dn,
    final Object credential,
    final String filter)
    throws NamingException
  {
    return
      this.authenticateDn(
        dn,
        credential,
        filter,
        this.config.getAuthenticationResultHandlers());
  }


  /**
   * This will authenticate credentials by binding to the LDAP with the supplied
   * dn and credential. See {@link #authenticateAndAuthorize(String, Object,
   * String, AuthenticationResultHandler...)}
   *
   * @param  dn  <code>String</code> dn for bind
   * @param  credential  <code>Object</code> credential for bind
   * @param  filter  <code>String</code> to authorize user
   * @param  handler  <code>AuthenticationResultHandler[]</code> to post process
   * authentication results
   *
   * @return  <code>boolean</code> - whether the bind succeeded
   *
   * @throws  NamingException  if the authentication fails for any other reason
   * than invalid credentials
   */
  public boolean authenticateDn(
    final String dn,
    final Object credential,
    final String filter,
    final AuthenticationResultHandler... handler)
    throws NamingException
  {
    return this.authenticateAndAuthorize(dn, credential, filter, handler);
  }


  /**
   * This will authenticate credentials by binding to the LDAP with the supplied
   * dn and credential. If {@link AuthenticatorConfig#setAuthorizationFilter}
   * has been called, then it will be used to authorize the user by performing
   * an ldap compare. See {@link #authenticateDn(String, Object, String,
   * String[], AuthenticationResultHandler...)}
   *
   * @param  dn  <code>String</code> dn for bind
   * @param  credential  <code>Object</code> credential for bind
   * @param  retAttrs  <code>String[]</code> to return
   *
   * @return  <code>Attributes</code> - of authenticated user
   *
   * @throws  NamingException  if any of the ldap operations fail
   */
  public Attributes authenticateDn(
    final String dn,
    final Object credential,
    final String[] retAttrs)
    throws NamingException
  {
    return
      this.authenticateDn(
        dn,
        credential,
        this.config.getAuthorizationFilter(),
        retAttrs);
  }


  /**
   * This will authenticate credentials by binding to the LDAP with the supplied
   * dn and credential. See {@link #authenticateAndAuthorize(String, Object,
   * String, boolean, String[], AuthenticationResultHandler...)}
   *
   * @param  dn  <code>String</code> dn for bind
   * @param  credential  <code>Object</code> credential for bind
   * @param  filter  <code>String</code> to authorize user
   * @param  retAttrs  <code>String[]</code> to return
   *
   * @return  <code>Attributes</code> - of authenticated user
   *
   * @throws  NamingException  if any of the ldap operations fail
   */
  public Attributes authenticateDn(
    final String dn,
    final Object credential,
    final String filter,
    final String[] retAttrs)
    throws NamingException
  {
    return
      this.authenticateAndAuthorize(
        dn,
        credential,
        filter,
        true,
        retAttrs,
        this.config.getAuthenticationResultHandlers());
  }


  /**
   * This will authenticate credentials by binding to the LDAP with the supplied
   * dn and credential. See {@link #authenticateAndAuthorize(String, Object,
   * String, boolean, String[], AuthenticationResultHandler...)}
   *
   * @param  dn  <code>String</code> dn for bind
   * @param  credential  <code>Object</code> credential for bind
   * @param  filter  <code>String</code> to authorize user
   * @param  retAttrs  <code>String[]</code> to return
   * @param  handler  <code>AuthenticationResultHandler[]</code> to post process
   * authentication results
   *
   * @return  <code>Attributes</code> - of authenticated user
   *
   * @throws  NamingException  if any of the ldap operations fail
   */
  public Attributes authenticateDn(
    final String dn,
    final Object credential,
    final String filter,
    final String[] retAttrs,
    final AuthenticationResultHandler... handler)
    throws NamingException
  {
    return
      this.authenticateAndAuthorize(
        dn,
        credential,
        filter,
        true,
        retAttrs,
        handler);
  }
}
/*
  $Id: Authenticator.java 639 2009-09-18 17:55:42Z dfisher $

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.StartTlsResponse;
import edu.vt.middleware.ldap.handler.AuthenticationCriteria;
import edu.vt.middleware.ldap.handler.AuthenticationResultHandler;

/**
 * <code>Authenticator</code> contains functions for authenticating a user
 * against a LDAP.
 *
 * @author  Middleware Services
 * @version  $Revision: 639 $ $Date: 2009-09-18 10:55:42 -0700 (Fri, 18 Sep 2009) $
 */
public class Authenticator extends AbstractLdap<AuthenticatorConfig>
{

  /** serial version uid. */
  private static final long serialVersionUID = -5125279149546967709L;

  /** Default constructor. */
  public Authenticator() {}


  /**
   * This will create a new <code>Authenticator</code> with the supplied <code>
   * AuthenticatorConfig</code>.
   *
   * @param  authConfig  <code>AuthenticatorConfig</code>
   */
  public Authenticator(final AuthenticatorConfig authConfig)
  {
    this.setAuthenticatorConfig(authConfig);
  }


  /**
   * This will set the config parameters of this <code>Authenticator</code>.
   *
   * @param  authConfig  <code>AuthenticatorConfig</code>
   */
  public void setAuthenticatorConfig(final AuthenticatorConfig authConfig)
  {
    super.setLdapConfig(authConfig);
  }


  /**
   * This returns the <code>AuthenticatorConfig</code> of the <code>
   * Authenticator</code>.
   *
   * @return  <code>AuthenticatorConfig</code>
   */
  public AuthenticatorConfig getAuthenticatorConfig()
  {
    return this.config;
  }


  /**
   * This will set the config parameters of this <code>Authenticator</code>
   * using the default properties file, which must be located in your classpath.
   */
  public void loadFromProperties()
  {
    this.setAuthenticatorConfig(AuthenticatorConfig.createFromProperties(null));
  }


  /**
   * This will set the config parameters of this <code>Authenticator</code>
   * using the supplied input stream.
   *
   * @param  is  <code>InputStream</code>
   */
  public void loadFromProperties(final InputStream is)
  {
    this.setAuthenticatorConfig(AuthenticatorConfig.createFromProperties(is));
  }


  /**
   * This will attempt to find the dn for the supplied user. {@link
   * AuthenticatorConfig#getUserField()} is used to look up the dn. If more than
   * one entry matches the search, the first result is used. If {@link
   * AuthenticatorConfig#setConstructDn(boolean)} has been set to true, then the
   * dn will be created by combining the userField and the base dn.
   *
   * @param  user  <code>String</code> to find dn for
   *
   * @return  <code>String</code> - user's dn
   *
   * @throws  NamingException  if the LDAP search fails
   */
  public String getDn(final String user)
    throws NamingException
  {
    String dn = null;
    if (user != null && !user.equals("")) {
      if (this.config.getConstructDn()) {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug("Constructing DN from first userfield and base");
        }
        dn = this.config.getUserField()[0] + "=" + user + "," +
          this.config.getBase();
      } else {
        // create the search filter
        final SearchFilter filter = new SearchFilter();
        if (this.config.getUserFilter() != null) {
          if (this.logger.isDebugEnabled()) {
            this.logger.debug("Looking up DN using userFilter");
          }
          filter.setFilter(this.config.getUserFilter());
          filter.setFilterArgs(this.config.getUserFilterArgs());
        } else {
          if (this.logger.isDebugEnabled()) {
            this.logger.debug("Looking up DN using userField");
          }
          if (this.config.getUserField() == null ||
              this.config.getUserField().length == 0) {
            if (this.logger.isErrorEnabled()) {
              this.logger.error("Invalid userField, cannot be null or empty.");
            }
          } else {
            final StringBuffer searchFilter = new StringBuffer();
            if (this.config.getUserField().length > 1) {
              searchFilter.append("(|");
              for (int i = 0; i < this.config.getUserField().length; i++) {
                searchFilter.append("(").append(this.config.getUserField()[i])
                  .append("=").append(user).append(")");
              }
              searchFilter.append(")");
            } else {
              searchFilter.append("(").append(this.config.getUserField()[0])
                .append("=").append(user).append(")");
            }
            filter.setFilter(searchFilter.toString());
          }
        }

        if (filter.getFilter() != null) {
          // make user the first filter arg
          final List<Object> filterArgs = new ArrayList<Object>();
          filterArgs.add(user);
          filterArgs.addAll(filter.getFilterArgs());
          final Iterator<SearchResult> answer = this.search(
            this.config.getBase(),
            filter.getFilter(),
            filterArgs.toArray(),
            new String[] {},
            this.config.getSearchResultHandlers());
          // return first match, otherwise user doesn't exist
          if (answer != null && answer.hasNext()) {
            final SearchResult sr = answer.next();
            dn = sr.getName();
          } else {
            if (this.logger.isInfoEnabled()) {
              this.logger.info(
                "Search for user: " + user +
                " failed using filter: " + filter.getFilter());
            }
          }
        } else {
          if (this.logger.isErrorEnabled()) {
            this.logger.error(
              "DN search filter not found, no search performed");
          }
        }
      }
    } else {
      if (this.logger.isDebugEnabled()) {
        this.logger.debug("User input was empty or null");
      }
    }
    return dn;
  }


  /**
   * This will authenticate credentials by binding to the LDAP using parameters
   * given by {@link AuthenticatorConfig#setUser} and {@link
   * AuthenticatorConfig#setCredential}. See {@link #authenticate(String,
   * Object)} If {@link AuthenticatorConfig#setAuthorizationFilter} has been
   * called, then it will be used to authorize the user by performing an ldap
   * compare. See {@link #authenticate(String, Object)}
   *
   * @return  <code>boolean</code> - whether the bind succeeded
   *
   * @throws  NamingException  if the authentication fails for any other reason
   * than invalid credentials
   */
  public boolean authenticate()
    throws NamingException
  {
    return
      this.authenticate(this.config.getUser(), this.config.getCredential());
  }


  /**
   * This will authenticate credentials by binding to the LDAP with the supplied
   * user and credential. The user's DN will be looked up before performing the
   * bind by searching on the user field, unless constructDn has been set. The
   * user field default is set to 'uid', so to authenticate 'dfisher', uid must
   * equal dfisher in the LDAP. If {@link
   * AuthenticatorConfig#setAuthorizationFilter} has been called, then it will
   * be used to authorize the user by performing an ldap compare. See {@link
   * #authenticateAndAuthorize( String, Object, SearchFilter,
   * AuthenticationResultHandler...)}
   *
   * @param  user  <code>String</code> username for bind
   * @param  credential  <code>Object</code> credential for bind
   *
   * @return  <code>boolean</code> - whether the bind succeeded
   *
   * @throws  NamingException  if the authentication fails for any other reason
   * than invalid credentials
   */
  public boolean authenticate(final String user, final Object credential)
    throws NamingException
  {
    return
      this.authenticate(
        user,
        credential,
        new SearchFilter(
          this.config.getAuthorizationFilter(),
          this.config.getAuthorizationFilterArgs()));
  }


  /**
   * This will authenticate credentials by binding to the LDAP with the supplied
   * user and credential. The user's DN will be looked up before performing the
   * bind by searching on the user field, unless constructDn has been set. The
   * user field default is set to 'uid', so to authenticate 'dfisher', uid must
   * equal dfisher in the LDAP. See {@link #authenticateAndAuthorize(String,
   * Object, SearchFilter, AuthenticationResultHandler...)}
   *
   * @param  user  <code>String</code> username for bind
   * @param  credential  <code>Object</code> credential for bind
   * @param  filter  <code>SearchFilter</code> to authorize user
   *
   * @return  <code>boolean</code> - whether the bind succeeded
   *
   * @throws  NamingException  if the authentication fails for any other reason
   * than invalid credentials
   */
  public boolean authenticate(
    final String user,
    final Object credential,
    final SearchFilter filter)
    throws NamingException
  {
    return
      this.authenticateAndAuthorize(
        this.getDn(user),
        credential,
        filter,
        this.config.getAuthenticationResultHandlers());
  }


  /**
   * This will authenticate credentials by binding to the LDAP with the supplied
   * user and credential. The user's DN will be looked up before performing the
   * bind by searching on the user field, unless constructDn has been set. The
   * user field default is set to 'uid', so to authenticate 'dfisher', uid must
   * equal dfisher in the LDAP. See {@link #authenticateAndAuthorize(String,
   * Object, SearchFilter, AuthenticationResultHandler...)}
   *
   * @param  user  <code>String</code> username for bind
   * @param  credential  <code>Object</code> credential for bind
   * @param  filter  <code>SearchFilter</code> to authorize user
   * @param  handler  <code>AuthenticationResultHandler[]</code> to post process
   * authentication results
   *
   * @return  <code>boolean</code> - whether the bind succeeded
   *
   * @throws  NamingException  if the authentication fails for any other reason
   * than invalid credentials
   */
  public boolean authenticate(
    final String user,
    final Object credential,
    final SearchFilter filter,
    final AuthenticationResultHandler... handler)
    throws NamingException
  {
    return
      this.authenticateAndAuthorize(
        this.getDn(user),
        credential,
        filter,
        handler);
  }


  /**
   * This will authenticate credentials by binding to the LDAP with the supplied
   * dn and credential. See {@link #authenticateAndAuthorize(
   * String,Object,String,boolean,String[],AuthenticationResultHandler...)}.
   *
   * @param  dn  <code>String</code> for bind
   * @param  credential  <code>Object</code> for bind
   * @param  filter  <code>SearchFilter</code> to authorize user
   * @param  handler  <code>AuthenticationResultHandler[]</code> to post process
   * authentication results
   *
   * @return  <code>boolean</code> - whether the bind succeeded
   *
   * @throws  NamingException  if the authentication fails for any other reason
   * than invalid credentials
   */
  private boolean authenticateAndAuthorize(
    final String dn,
    final Object credential,
    final SearchFilter filter,
    final AuthenticationResultHandler... handler)
    throws NamingException
  {
    boolean success = false;
    try {
      this.authenticateAndAuthorize(
        dn,
        credential,
        filter,
        false,
        null,
        handler);
      success = true;
    } catch (AuthenticationException e) {
      if (this.logger.isDebugEnabled()) {
        this.logger.debug("Authentication failed for dn: " + dn, e);
      }
    }
    return success;
  }


  /**
   * This will authenticate credentials by binding to the LDAP using parameters
   * given by {@link AuthenticatorConfig#setUser} and {@link
   * AuthenticatorConfig#setCredential}. If {@link
   * AuthenticatorConfig#setAuthorizationFilter} has been called, then it will
   * be used to authorize the user by performing an ldap compare. See {@link
   * #authenticate(String,Object,String[])}
   *
   * @param  retAttrs  <code>String[]</code> attributes to return
   *
   * @return  <code>Attributes</code> - of authenticated user
   *
   * @throws  NamingException  if any of the ldap operations fail
   */
  public Attributes authenticate(final String[] retAttrs)
    throws NamingException
  {
    return
      this.authenticate(
        this.config.getUser(),
        this.config.getCredential(),
        retAttrs);
  }


  /**
   * This will authenticate credentials by binding to the LDAP with the supplied
   * user and credential. The user's DN will be looked up before performing the
   * bind by searching on the user field, unless constructDn has been set. The
   * user field default is set to 'uid', so to authenticate 'dfisher', uid must
   * equal dfisher in the LDAP. If {@link
   * AuthenticatorConfig#setAuthorizationFilter} has been called, then it will
   * be used to authorize the user by performing an ldap compare. See {@link
   * #authenticate(String, Object, SearchFilter, String[])}
   *
   * @param  user  <code>String</code> username for bind
   * @param  credential  <code>Object</code> credential for bind
   * @param  retAttrs  <code>String[]</code> to return
   *
   * @return  <code>Attributes</code> - of authenticated user
   *
   * @throws  NamingException  if any of the ldap operations fail
   */
  public Attributes authenticate(
    final String user,
    final Object credential,
    final String[] retAttrs)
    throws NamingException
  {
    return
      this.authenticate(
        user,
        credential,
        new SearchFilter(
          this.config.getAuthorizationFilter(),
          this.config.getAuthorizationFilterArgs()),
        retAttrs);
  }


  /**
   * This will authenticate credentials by binding to the LDAP with the supplied
   * user and credential. The user's DN will be looked up before performing the
   * bind by searching on the user field, unless constructDn has been set. The
   * user field default is set to 'uid', so to authenticate 'dfisher', uid must
   * equal dfisher in the LDAP. See {@link #authenticateAndAuthorize(String,
   * Object, SearchFilter, boolean, String[], AuthenticationResultHandler...)}
   *
   * @param  user  <code>String</code> username for bind
   * @param  credential  <code>Object</code> credential for bind
   * @param  filter  <code>SearchFilter</code> to authorize user
   * @param  retAttrs  <code>String[]</code> to return
   *
   * @return  <code>Attributes</code> - of authenticated user
   *
   * @throws  NamingException  if any of the ldap operations fail
   */
  public Attributes authenticate(
    final String user,
    final Object credential,
    final SearchFilter filter,
    final String[] retAttrs)
    throws NamingException
  {
    return
      this.authenticateAndAuthorize(
        this.getDn(user),
        credential,
        filter,
        true,
        retAttrs,
        this.config.getAuthenticationResultHandlers());
  }


  /**
   * This will authenticate credentials by binding to the LDAP with the supplied
   * user and credential. The user's DN will be looked up before performing the
   * bind by searching on the user field, unless constructDn has been set. The
   * user field default is set to 'uid', so to authenticate 'dfisher', uid must
   * equal dfisher in the LDAP. See {@link #authenticateAndAuthorize(String,
   * Object, SearchFilter, boolean, String[], AuthenticationResultHandler...)}
   *
   * @param  user  <code>String</code> username for bind
   * @param  credential  <code>Object</code> credential for bind
   * @param  filter  <code>SearchFilter</code> to authorize user
   * @param  retAttrs  <code>String[]</code> to return
   * @param  handler  <code>AuthenticationResultHandler[]</code> to post process
   * authentication results
   *
   * @return  <code>Attributes</code> - of authenticated user
   *
   * @throws  NamingException  if any of the ldap operations fail
   */
  public Attributes authenticate(
    final String user,
    final Object credential,
    final SearchFilter filter,
    final String[] retAttrs,
    final AuthenticationResultHandler... handler)
    throws NamingException
  {
    return
      this.authenticateAndAuthorize(
        this.getDn(user),
        credential,
        filter,
        true,
        retAttrs,
        handler);
  }


  /**
   * This will authenticate credentials by binding to the LDAP with the supplied
   * dn and credential. Authentication will never succeed if {@link
   * LdapConfig#getAuthtype()} is set to 'none'. If the supplied filter is not
   * null, then it will be used to do an ldap compare operation against the
   * supplied user. If the compare does not succeed, then this method will
   * return false. The compare is done with the same context used to
   * authenticate the user, if the user does not have permission to view the
   * supplied attributes then the authorization will fail. If retAttrs is null
   * then all user attributes will be returned. If retAttrs is an empty array
   * then no attributes will be returned. This method returns null if
   * authentication or authorization fails.
   *
   * @param  dn  <code>String</code> for bind
   * @param  credential  <code>Object</code> for bind
   * @param  filter  <code>SearchFilter</code> to authorize user
   * @param  searchAttrs  <code>boolean</code> whether to perform attribute
   * search
   * @param  retAttrs  <code>String[]</code> user attributes to return
   * @param  handler  <code>AuthenticationResultHandler[]</code> to post process
   * authentication results
   *
   * @return  <code>Attribute</code> - belonging to the supplied user, returns
   * null if searchAttrs is false
   *
   * @throws  NamingException  if any of the ldap operations fail
   * @throws  AuthenticationException  if authentication or authorization fails
   */
  private Attributes authenticateAndAuthorize(
    final String dn,
    final Object credential,
    final SearchFilter filter,
    final boolean searchAttrs,
    final String[] retAttrs,
    final AuthenticationResultHandler... handler)
    throws NamingException
  {
    // check the authentication type
    final String authtype = this.config.getAuthtype();
    if (authtype.equalsIgnoreCase(LdapConstants.NONE_AUTHTYPE)) {
      throw new AuthenticationException(
        "Cannot authenticate dn, authtype is 'none'");
    }

    // check the credential
    if (!LdapUtil.checkCredential(credential)) {
      throw new AuthenticationException(
        "Cannot authenticate dn, invalid credential");
    }

    // check the dn
    if (dn == null || dn.equals("")) {
      throw new AuthenticationException("Cannot authenticate dn, invalid dn");
    }

    Attributes userAttributes = null;

    // attempt to bind as this dn
    final StartTlsResponse tls = null;
    LdapContext ctx = null;
    for (int i = 0; i <= LdapConstants.OPERATION_RETRY; i++) {
      try {
        final AuthenticationCriteria ac = new AuthenticationCriteria(dn);
        ac.setCredential(credential);
        try {
          ctx = this.bind(dn, credential, tls);
          if (this.logger.isInfoEnabled()) {
            this.logger.info("Authentication succeeded for dn: " + dn);
          }
          if (handler != null && handler.length > 0) {
            for (AuthenticationResultHandler ah : handler) {
              ah.process(ac, true);
            }
          }
        } catch (AuthenticationException e) {
          if (this.logger.isInfoEnabled()) {
            this.logger.info("Authentication failed for dn: " + dn);
          }
          if (handler != null && handler.length > 0) {
            for (AuthenticationResultHandler ah : handler) {
              ah.process(ac, false);
            }
          }
          throw e;
        }
        // authentication succeeded, perform authorization if supplied
        if (filter != null && filter.getFilter() != null) {
          // make DN the first filter arg
          final List<Object> filterArgs = new ArrayList<Object>();
          filterArgs.add(dn);
          filterArgs.addAll(filter.getFilterArgs());
          // perform ldap compare operation
          NamingEnumeration<SearchResult> results = null;
          try {
            results = ctx.search(
              dn,
              filter.getFilter(),
              filterArgs.toArray(),
              LdapConfig.getCompareSearchControls());
            if (results.hasMore()) {
              if (this.logger.isInfoEnabled()) {
                this.logger.info(
                  "Authorization succeeded for dn: " + dn + " with filter: " +
                  filter);
              }
            } else {
              if (this.logger.isInfoEnabled()) {
                this.logger.info(
                  "Authorization failed for dn: " + dn + " with filter: " +
                  filter);
              }
              throw new AuthenticationException(
                "Authorization failed for dn: " + dn + " with filter: " +
                filter);
            }
          } finally {
            if (results != null) {
              results.close();
            }
          }
        }
        if (searchAttrs) {
          if (this.logger.isDebugEnabled()) {
            this.logger.debug("Returning attributes: ");
            this.logger.debug(
              "    " +
              (retAttrs == null ? "all attributes" : Arrays.asList(retAttrs)));
          }
          userAttributes = ctx.getAttributes(dn, retAttrs);
        }
        break;
      } catch (CommunicationException e) {
        if (i == LdapConstants.OPERATION_RETRY) {
          throw e;
        }
        if (this.logger.isWarnEnabled()) {
          this.logger.warn(
            "Error while communicating with the LDAP, retrying",
            e);
        }
      } finally {
        this.stopTls(tls);
        if (ctx != null) {
          ctx.close();
        }
      }
    }

    return userAttributes;
  }


  /**
   * This will authenticate credentials by binding to the LDAP with the supplied
   * dn and credential. If {@link AuthenticatorConfig#setAuthorizationFilter}
   * has been called, then it will be used to authorize the user by performing
   * an ldap compare. See {@link #authenticateAndAuthorize(String, Object,
   * SearchFilter, AuthenticationResultHandler...)}
   *
   * @param  dn  <code>String</code> dn for bind
   * @param  credential  <code>Object</code> credential for bind
   *
   * @return  <code>boolean</code> - whether the bind succeeded
   *
   * @throws  NamingException  if the authentication fails for any other reason
   * than invalid credentials
   */
  public boolean authenticateDn(final String dn, final Object credential)
    throws NamingException
  {
    return
      this.authenticateDn(
        dn,
        credential,
        new SearchFilter(
          this.config.getAuthorizationFilter(),
          this.config.getAuthorizationFilterArgs()));
  }


  /**
   * This will authenticate credentials by binding to the LDAP with the supplied
   * dn and credential. See {@link #authenticateAndAuthorize(String, Object,
   * SearchFilter, AuthenticationResultHandler...)}
   *
   * @param  dn  <code>String</code> dn for bind
   * @param  credential  <code>Object</code> credential for bind
   * @param  filter  <code>SearchFilter</code> to authorize user
   *
   * @return  <code>boolean</code> - whether the bind succeeded
   *
   * @throws  NamingException  if the authentication fails for any other reason
   * than invalid credentials
   */
  public boolean authenticateDn(
    final String dn,
    final Object credential,
    final SearchFilter filter)
    throws NamingException
  {
    return
      this.authenticateDn(
        dn,
        credential,
        filter,
        this.config.getAuthenticationResultHandlers());
  }


  /**
   * This will authenticate credentials by binding to the LDAP with the supplied
   * dn and credential. See {@link #authenticateAndAuthorize(String, Object,
   * SearchFilter, AuthenticationResultHandler...)}
   *
   * @param  dn  <code>String</code> dn for bind
   * @param  credential  <code>Object</code> credential for bind
   * @param  filter  <code>SearchFilter</code> to authorize user
   * @param  handler  <code>AuthenticationResultHandler[]</code> to post process
   * authentication results
   *
   * @return  <code>boolean</code> - whether the bind succeeded
   *
   * @throws  NamingException  if the authentication fails for any other reason
   * than invalid credentials
   */
  public boolean authenticateDn(
    final String dn,
    final Object credential,
    final SearchFilter filter,
    final AuthenticationResultHandler... handler)
    throws NamingException
  {
    return this.authenticateAndAuthorize(dn, credential, filter, handler);
  }


  /**
   * This will authenticate credentials by binding to the LDAP with the supplied
   * dn and credential. If {@link AuthenticatorConfig#setAuthorizationFilter}
   * has been called, then it will be used to authorize the user by performing
   * an ldap compare. See {@link #authenticateDn(String, Object, SearchFilter,
   * String[], AuthenticationResultHandler...)}
   *
   * @param  dn  <code>String</code> dn for bind
   * @param  credential  <code>Object</code> credential for bind
   * @param  retAttrs  <code>String[]</code> to return
   *
   * @return  <code>Attributes</code> - of authenticated user
   *
   * @throws  NamingException  if any of the ldap operations fail
   */
  public Attributes authenticateDn(
    final String dn,
    final Object credential,
    final String[] retAttrs)
    throws NamingException
  {
    return
      this.authenticateDn(
        dn,
        credential,
        new SearchFilter(
          this.config.getAuthorizationFilter(),
          this.config.getAuthorizationFilterArgs()),
        retAttrs);
  }


  /**
   * This will authenticate credentials by binding to the LDAP with the supplied
   * dn and credential. See {@link #authenticateAndAuthorize(String, Object,
   * SearchFilter, boolean, String[], AuthenticationResultHandler...)}
   *
   * @param  dn  <code>String</code> dn for bind
   * @param  credential  <code>Object</code> credential for bind
   * @param  filter  <code>SearchFilter</code> to authorize user
   * @param  retAttrs  <code>String[]</code> to return
   *
   * @return  <code>Attributes</code> - of authenticated user
   *
   * @throws  NamingException  if any of the ldap operations fail
   */
  public Attributes authenticateDn(
    final String dn,
    final Object credential,
    final SearchFilter filter,
    final String[] retAttrs)
    throws NamingException
  {
    return
      this.authenticateAndAuthorize(
        dn,
        credential,
        filter,
        true,
        retAttrs,
        this.config.getAuthenticationResultHandlers());
  }


  /**
   * This will authenticate credentials by binding to the LDAP with the supplied
   * dn and credential. See {@link #authenticateAndAuthorize(String, Object,
   * SearchFilter, boolean, String[], AuthenticationResultHandler...)}
   *
   * @param  dn  <code>String</code> dn for bind
   * @param  credential  <code>Object</code> credential for bind
   * @param  filter  <code>SearchFilter</code> to authorize user
   * @param  retAttrs  <code>String[]</code> to return
   * @param  handler  <code>AuthenticationResultHandler[]</code> to post process
   * authentication results
   *
   * @return  <code>Attributes</code> - of authenticated user
   *
   * @throws  NamingException  if any of the ldap operations fail
   */
  public Attributes authenticateDn(
    final String dn,
    final Object credential,
    final SearchFilter filter,
    final String[] retAttrs,
    final AuthenticationResultHandler... handler)
    throws NamingException
  {
    return
      this.authenticateAndAuthorize(
        dn,
        credential,
        filter,
        true,
        retAttrs,
        handler);
  }
}
/*
  $Id: Authenticator.java 269 2009-05-28 14:24:37Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 269 $
  Updated: $Date: 2009-05-28 07:24:37 -0700 (Thu, 28 May 2009) $
*/
package edu.vt.middleware.ldap;

import java.util.Arrays;
import java.util.Iterator;
import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.StartTlsResponse;

/**
 * <code>Authenticator</code> contains functions for authenticating a user
 * against a LDAP.
 *
 * @author  Middleware Services
 * @version  $Revision: 269 $ $Date: 2009-05-28 07:24:37 -0700 (Thu, 28 May 2009) $
 */
public class Authenticator extends AbstractLdap<AuthenticatorConfig>
{

  /** serial version uid. */
  private static final long serialVersionUID = -5125279149546967709L;

  /** Default constructor. */
  public Authenticator() {}


  /**
   * This will create a new <code>Authenticator</code> with the supplied <code>
   * AuthenticatorConfig</code>.
   *
   * @param  authConfig  <code>AuthenticatorConfig</code>
   */
  public Authenticator(final AuthenticatorConfig authConfig)
  {
    this.setAuthenticatorConfig(authConfig);
  }


  /**
   * This will set the config parameters of this <code>Authenticator</code>.
   *
   * @param  authConfig  <code>AuthenticatorConfig</code>
   */
  public void setAuthenticatorConfig(final AuthenticatorConfig authConfig)
  {
    super.setLdapConfig(authConfig);
  }


  /**
   * This returns the <code>AuthenticatorConfig</code> of the <code>
   * Authenticator</code>.
   *
   * @return  <code>AuthenticatorConfig</code>
   */
  public AuthenticatorConfig getAuthenticatorConfig()
  {
    return this.config;
  }


  /**
   * This will set the config parameters of this <code>Authenticator</code>
   * using the default properties file, which must be located in your classpath.
   */
  public void loadFromProperties()
  {
    this.setAuthenticatorConfig(AuthenticatorConfig.createFromProperties(null));
  }


  /**
   * This will set the config parameters of this <code>Authenticator</code>
   * using the supplied properties file, which must be located in your
   * classpath.
   *
   * @param  propertiesFile  <code>String</code>
   */
  public void loadFromProperties(final String propertiesFile)
  {
    this.setAuthenticatorConfig(
      AuthenticatorConfig.createFromProperties(propertiesFile));
  }


  /**
   * This will attempt to find the dn for the supplied user. {@link
   * AuthenticatorConfig#getUserField()} is used to look up the dn. If more than
   * one entry matches the search, the first result is used. If {@link
   * AuthenticatorConfig#setConstructDn(boolean)} has been set to true, then the
   * dn will be created by combining the userField and the base dn.
   *
   * @param  user  <code>String</code> to find dn for
   *
   * @return  <code>String</code> - user's dn
   *
   * @throws  NamingException  if the LDAP search fails
   */
  public String getDn(final String user)
    throws NamingException
  {
    String dn = null;
    if (user != null && !user.equals("")) {
      if (
        this.config.getUserField() == null ||
          this.config.getUserField().length == 0) {
        if (this.logger.isErrorEnabled()) {
          this.logger.error("Invalid userField, cannot be null or empty.");
        }
      } else if (this.config.getConstructDn()) {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug("Constructing DN from first userfield and base");
        }
        dn = this.config.getUserField()[0] + "=" + user + "," +
          this.config.getBase();
      } else {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug("Looking up DN from userfield and base");
        }

        Iterator<SearchResult> answer = null;
        if (this.config.getSubtreeSearch()) {
          final StringBuffer searchFilter = new StringBuffer();
          if (this.config.getUserField().length > 1) {
            searchFilter.append("(|");
            for (int i = 0; i < this.config.getUserField().length; i++) {
              searchFilter.append("(").append(this.config.getUserField()[i])
                .append("=").append(user).append(")");
            }
            searchFilter.append(")");
          } else {
            searchFilter.append("(").append(this.config.getUserField()[0])
              .append("=").append(user).append(")");
          }
          answer = this.search(
            this.config.getBase(),
            searchFilter.toString(),
            null,
            new String[] {},
            SEARCH_RESULT_HANDLER);
        } else {
          for (int i = 0; i < this.config.getUserField().length; i++) {
            answer = this.searchAttributes(
              this.config.getBase(),
              new BasicAttributes(this.config.getUserField()[i], user),
              new String[] {},
              SEARCH_RESULT_HANDLER);
            if (answer != null && answer.hasNext()) {
              break;
            }
          }
        }
        // return first match, otherwise user doesn't exist
        if (answer != null && answer.hasNext()) {
          final SearchResult sr = answer.next();
          dn = sr.getName();
        } else {
          if (this.logger.isInfoEnabled()) {
            this.logger.info(
              "Search for user: " + user + " failed using attribute(s): " +
              Arrays.asList(this.config.getUserField()));
          }
        }
      }
    } else {
      if (this.logger.isDebugEnabled()) {
        this.logger.debug("Input was empty or null");
      }
    }
    return dn;
  }


  /**
   * This will authenticate credentials by binding to the LDAP using parameters
   * given by {@link AuthenticatorConfig#setUser} and {@link
   * AuthenticatorConfig#setCredential}. See {@link #authenticate(String,
   * Object)} If {@link AuthenticatorConfig#setAuthorizationFilter} has been
   * called, then it will be used to authorize the user by performing an ldap
   * compare. See {@link #authenticate(String, Object)}
   *
   * @return  <code>boolean</code> - whether the bind succeeded
   *
   * @throws  NamingException  if the authentication fails for any other reason
   * than invalid credentials
   */
  public boolean authenticate()
    throws NamingException
  {
    return
      this.authenticate(this.config.getUser(), this.config.getCredential());
  }


  /**
   * This will authenticate credentials by binding to the LDAP with the supplied
   * user and credential. The user's DN will be looked up before performing the
   * bind by searching on the user field, unless constructDn has been set. The
   * user field default is set to 'uid', so to authenticate 'dfisher', uid must
   * equal dfisher in the LDAP. If {@link
   * AuthenticatorConfig#setAuthorizationFilter} has been called, then it will
   * be used to authorize the user by performing an ldap compare. See {@link
   * #authenticateAndAuthorize(String, Object, String)}
   *
   * @param  user  <code>String</code> username for bind
   * @param  credential  <code>Object</code> credential for bind
   *
   * @return  <code>boolean</code> - whether the bind succeeded
   *
   * @throws  NamingException  if the authentication fails for any other reason
   * than invalid credentials
   */
  public boolean authenticate(final String user, final Object credential)
    throws NamingException
  {
    return
      this.authenticate(user, credential, this.config.getAuthorizationFilter());
  }


  /**
   * This will authenticate credentials by binding to the LDAP with the supplied
   * user and credential. The user's DN will be looked up before performing the
   * bind by searching on the user field, unless constructDn has been set. The
   * user field default is set to 'uid', so to authenticate 'dfisher', uid must
   * equal dfisher in the LDAP. See {@link #authenticateAndAuthorize(String,
   * Object, String)}
   *
   * @param  user  <code>String</code> username for bind
   * @param  credential  <code>Object</code> credential for bind
   * @param  filter  <code>String</code> to authorize user
   *
   * @return  <code>boolean</code> - whether the bind succeeded
   *
   * @throws  NamingException  if the authentication fails for any other reason
   * than invalid credentials
   */
  public boolean authenticate(
    final String user,
    final Object credential,
    final String filter)
    throws NamingException
  {
    return this.authenticateAndAuthorize(this.getDn(user), credential, filter);
  }


  /**
   * This will authenticate credentials by binding to the LDAP with the supplied
   * dn and credential. See {@link
   * #authenticateAndAuthorize(String,Object,String,boolean,String[])}.
   *
   * @param  dn  <code>String</code> for bind
   * @param  credential  <code>Object</code> for bind
   * @param  filter  <code>String</code> to authorize user
   *
   * @return  <code>boolean</code> - whether the bind succeeded
   *
   * @throws  NamingException  if the authentication fails for any other reason
   * than invalid credentials
   */
  private boolean authenticateAndAuthorize(
    final String dn,
    final Object credential,
    final String filter)
    throws NamingException
  {
    boolean success = false;
    try {
      this.authenticateAndAuthorize(dn, credential, filter, false, null);
      success = true;
    } catch (AuthenticationException e) {
      if (this.logger.isDebugEnabled()) {
        this.logger.debug("Authentication failed for dn: " + dn, e);
      }
    }
    return success;
  }


  /**
   * This will authenticate credentials by binding to the LDAP using parameters
   * given by {@link AuthenticatorConfig#setUser} and {@link
   * AuthenticatorConfig#setCredential}. If {@link
   * AuthenticatorConfig#setAuthorizationFilter} has been called, then it will
   * be used to authorize the user by performing an ldap compare. See {@link
   * #authenticate(String,Object,String[])}
   *
   * @param  retAttrs  <code>String[]</code> attributes to return
   *
   * @return  <code>Attributes</code> - of authenticated user
   *
   * @throws  NamingException  if any of the ldap operations fail
   */
  public Attributes authenticate(final String[] retAttrs)
    throws NamingException
  {
    return
      this.authenticate(
        this.config.getUser(),
        this.config.getCredential(),
        retAttrs);
  }


  /**
   * This will authenticate credentials by binding to the LDAP with the supplied
   * user and credential. The user's DN will be looked up before performing the
   * bind by searching on the user field, unless constructDn has been set. The
   * user field default is set to 'uid', so to authenticate 'dfisher', uid must
   * equal dfisher in the LDAP. If {@link
   * AuthenticatorConfig#setAuthorizationFilter} has been called, then it will
   * be used to authorize the user by performing an ldap compare. See {@link
   * #authenticate(String, Object, String, String[])}
   *
   * @param  user  <code>String</code> username for bind
   * @param  credential  <code>Object</code> credential for bind
   * @param  retAttrs  <code>String[]</code> to return
   *
   * @return  <code>Attributes</code> - of authenticated user
   *
   * @throws  NamingException  if any of the ldap operations fail
   */
  public Attributes authenticate(
    final String user,
    final Object credential,
    final String[] retAttrs)
    throws NamingException
  {
    return
      this.authenticate(
        user,
        credential,
        this.config.getAuthorizationFilter(),
        retAttrs);
  }


  /**
   * This will authenticate credentials by binding to the LDAP with the supplied
   * user and credential. The user's DN will be looked up before performing the
   * bind by searching on the user field, unless constructDn has been set. The
   * user field default is set to 'uid', so to authenticate 'dfisher', uid must
   * equal dfisher in the LDAP. See {@link #authenticateAndAuthorize(String,
   * Object, String, boolean, String[])}
   *
   * @param  user  <code>String</code> username for bind
   * @param  credential  <code>Object</code> credential for bind
   * @param  filter  <code>String</code> to authorize user
   * @param  retAttrs  <code>String[]</code> to return
   *
   * @return  <code>Attributes</code> - of authenticated user
   *
   * @throws  NamingException  if any of the ldap operations fail
   */
  public Attributes authenticate(
    final String user,
    final Object credential,
    final String filter,
    final String[] retAttrs)
    throws NamingException
  {
    return
      this.authenticateAndAuthorize(
        this.getDn(user),
        credential,
        filter,
        true,
        retAttrs);
  }


  /**
   * This will authenticate credentials by binding to the LDAP with the supplied
   * dn and credential. Authentication will never succeed if {@link
   * LdapConfig#getAuthtype()} is set to 'none'. If the supplied filter is not
   * null, then it will be used to do an ldap compare operation against the
   * supplied user. If the compare does not succeed, then this method will
   * return false. The compare is done with the same context used to
   * authenticate the user, if the user does not have permission to view the
   * supplied attributes then the authorization will fail. If retAttrs is null
   * then all user attributes will be returned. If retAttrs is an empty array
   * then no attributes will be returned. This method returns null if
   * authentication or authorization fails.
   *
   * @param  dn  <code>String</code> for bind
   * @param  credential  <code>Object</code> for bind
   * @param  filter  <code>String</code> to authorize user
   * @param  searchAttrs  <code>boolean</code> whether to perform attribute
   * search
   * @param  retAttrs  <code>String[]</code> user attributes to return
   *
   * @return  <code>Attribute</code> - belonging to the supplied user, returns
   * null if searchAttrs is false
   *
   * @throws  NamingException  if any of the ldap operations fail
   * @throws  AuthenticationException  if authentication or authorization fails
   */
  private Attributes authenticateAndAuthorize(
    final String dn,
    final Object credential,
    final String filter,
    final boolean searchAttrs,
    final String[] retAttrs)
    throws NamingException
  {
    // check the authentication type
    final String authtype = this.config.getAuthtype();
    if (authtype.equalsIgnoreCase(LdapConstants.NONE_AUTHTYPE)) {
      throw new AuthenticationException(
        "Cannot authenticate dn, authtype is 'none'");
    }

    // check the credential
    if (!LdapUtil.checkCredential(credential)) {
      throw new AuthenticationException(
        "Cannot authenticate dn, invalid credential");
    }

    // check the dn
    if (dn == null || dn.equals("")) {
      throw new AuthenticationException("Cannot authenticate dn, invalid dn");
    }

    Attributes userAttributes = null;

    // attempt to bind as this dn
    final StartTlsResponse tls = null;
    LdapContext ctx = null;
    for (int i = 0; i <= LdapConstants.OPERATION_RETRY; i++) {
      try {
        try {
          ctx = this.bind(dn, credential, tls);
          if (this.logger.isInfoEnabled()) {
            this.logger.info("Authentication succeeded for dn: " + dn);
          }
        } catch (AuthenticationException e) {
          if (this.logger.isInfoEnabled()) {
            this.logger.info("Authentication failed for dn: " + dn);
          }
          throw e;
        }
        // authentication succeeded, perform authorization if supplied
        if (filter != null) {
          // perform ldap compare operation
          NamingEnumeration<SearchResult> results = null;
          try {
            results = ctx.search(
              dn,
              filter,
              LdapConfig.getCompareSearchControls());
            if (results.hasMore()) {
              if (this.logger.isInfoEnabled()) {
                this.logger.info(
                  "Authorization succeeded for dn: " + dn + " with filter: " +
                  filter);
              }
            } else {
              if (this.logger.isInfoEnabled()) {
                this.logger.info(
                  "Authorization failed for dn: " + dn + " with filter: " +
                  filter);
              }
              throw new AuthenticationException(
                "Authorization failed for dn: " + dn + " with filter: " +
                filter);
            }
          } finally {
            if (results != null) {
              results.close();
            }
          }
        }
        if (searchAttrs) {
          if (this.logger.isDebugEnabled()) {
            this.logger.debug("Returning attributes: ");
            if (retAttrs == null) {
              if (this.logger.isDebugEnabled()) {
                this.logger.debug("    all attributes");
              }
            } else {
              if (this.logger.isDebugEnabled()) {
                this.logger.debug("    " + Arrays.asList(retAttrs));
              }
            }
          }
          userAttributes = ctx.getAttributes(dn, retAttrs);
        }
        break;
      } catch (CommunicationException e) {
        if (i == LdapConstants.OPERATION_RETRY) {
          throw e;
        }
        if (this.logger.isWarnEnabled()) {
          this.logger.warn(
            "Error while communicating with the LDAP, retrying",
            e);
        }
      } finally {
        this.stopTls(tls);
        if (ctx != null) {
          ctx.close();
        }
      }
    }

    return userAttributes;
  }


  /**
   * This will authenticate credentials by binding to the LDAP with the supplied
   * dn and credential. If {@link AuthenticatorConfig#setAuthorizationFilter}
   * has been called, then it will be used to authorize the user by performing
   * an ldap compare. See {@link #authenticateAndAuthorize(String, Object,
   * String)}
   *
   * @param  dn  <code>String</code> dn for bind
   * @param  credential  <code>Object</code> credential for bind
   *
   * @return  <code>boolean</code> - whether the bind succeeded
   *
   * @throws  NamingException  if the authentication fails for any other reason
   * than invalid credentials
   */
  public boolean authenticateDn(final String dn, final Object credential)
    throws NamingException
  {
    return
      this.authenticateDn(dn, credential, this.config.getAuthorizationFilter());
  }


  /**
   * This will authenticate credentials by binding to the LDAP with the supplied
   * dn and credential. See {@link #authenticateAndAuthorize(String, Object,
   * String)}
   *
   * @param  dn  <code>String</code> dn for bind
   * @param  credential  <code>Object</code> credential for bind
   * @param  filter  <code>String</code> to authorize user
   *
   * @return  <code>boolean</code> - whether the bind succeeded
   *
   * @throws  NamingException  if the authentication fails for any other reason
   * than invalid credentials
   */
  public boolean authenticateDn(
    final String dn,
    final Object credential,
    final String filter)
    throws NamingException
  {
    return this.authenticateAndAuthorize(dn, credential, filter);
  }


  /**
   * This will authenticate credentials by binding to the LDAP with the supplied
   * dn and credential. If {@link AuthenticatorConfig#setAuthorizationFilter}
   * has been called, then it will be used to authorize the user by performing
   * an ldap compare. See {@link #authenticateDn(String, Object, String,
   * String[])}
   *
   * @param  dn  <code>String</code> dn for bind
   * @param  credential  <code>Object</code> credential for bind
   * @param  retAttrs  <code>String[]</code> to return
   *
   * @return  <code>Attributes</code> - of authenticated user
   *
   * @throws  NamingException  if any of the ldap operations fail
   */
  public Attributes authenticateDn(
    final String dn,
    final Object credential,
    final String[] retAttrs)
    throws NamingException
  {
    return
      this.authenticateDn(
        dn,
        credential,
        this.config.getAuthorizationFilter(),
        retAttrs);
  }


  /**
   * This will authenticate credentials by binding to the LDAP with the supplied
   * dn and credential. See {@link #authenticateAndAuthorize(String, Object,
   * String, boolean, String[])}
   *
   * @param  dn  <code>String</code> dn for bind
   * @param  credential  <code>Object</code> credential for bind
   * @param  filter  <code>String</code> to authorize user
   * @param  retAttrs  <code>String[]</code> to return
   *
   * @return  <code>Attributes</code> - of authenticated user
   *
   * @throws  NamingException  if any of the ldap operations fail
   */
  public Attributes authenticateDn(
    final String dn,
    final Object credential,
    final String filter,
    final String[] retAttrs)
    throws NamingException
  {
    return
      this.authenticateAndAuthorize(dn, credential, filter, true, retAttrs);
  }
}
