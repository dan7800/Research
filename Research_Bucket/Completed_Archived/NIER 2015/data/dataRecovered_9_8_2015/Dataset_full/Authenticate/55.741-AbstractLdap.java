/*
  $Id: AbstractLdap.java 466 2009-08-20 18:05:31Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 466 $
  Updated: $Date: 2009-08-20 11:05:31 -0700 (Thu, 20 Aug 2009) $
*/
package edu.vt.middleware.ldap;

import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import javax.naming.Binding;
import javax.naming.CommunicationException;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.StartTlsResponse;
import edu.vt.middleware.ldap.handler.AttributeHandler;
import edu.vt.middleware.ldap.handler.AttributesProcessor;
import edu.vt.middleware.ldap.handler.CopyResultHandler;
import edu.vt.middleware.ldap.handler.SearchCriteria;
import edu.vt.middleware.ldap.handler.SearchResultHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <code>AbstractLdap</code> contains the functions for basic interaction with a
 * LDAP. Methods are provided for connecting, binding, querying and updating.
 *
 * @param  <T>  type of LdapConfig
 *
 * @author  Middleware Services
 * @version  $Revision: 466 $ $Date: 2009-08-20 11:05:31 -0700 (Thu, 20 Aug 2009) $
 */
public abstract class AbstractLdap<T extends LdapConfig> implements BaseLdap
{

  /** Default copy search result handler, used if none supplied. */
  protected static final CopyResultHandler<SearchResult>
  SR_COPY_RESULT_HANDLER = new CopyResultHandler<SearchResult>();

  /** Default copy name class pair handler. */
  protected static final CopyResultHandler<NameClassPair>
  NCP_COPY_RESULT_HANDLER = new CopyResultHandler<NameClassPair>();

  /** Default copy binding handler. */
  protected static final CopyResultHandler<Binding>
  BINDING_COPY_RESULT_HANDLER = new CopyResultHandler<Binding>();

  /** Default copy result handler. */
  protected static final CopyResultHandler<Object> COPY_RESULT_HANDLER =
    new CopyResultHandler<Object>();

  /** Log for this class. */
  protected final Log logger = LogFactory.getLog(this.getClass());

  /** LDAP interface for directory operations. */
  protected LdapContext context;

  /** TLS Session. */
  protected StartTlsResponse tlsResponse;

  /** LDAP configuration environment. */
  protected T config;


  /**
   * This will set the config parameters of this <code>Ldap</code>.
   *
   * @param  ldapConfig  <code>LdapConfig</code>
   */
  protected void setLdapConfig(final T ldapConfig)
  {
    if (this.config != null) {
      this.config.checkImmutable();
    }
    this.config = ldapConfig;
    if (this.config.isTlsEnabled()) {
      try {
        this.useTls(true);
      } catch (NamingException e) {
        if (this.logger.isErrorEnabled()) {
          this.logger.error("Error using TLS", e);
        }
      }
    }
  }


  /**
   * This will perform an LDAP compare operation with the supplied filter and
   * dn.
   *
   * @param  dn  <code>String</code> name to compare
   * @param  filter  <code>String</code> expression to use for compare
   *
   * @return  <code>boolean</code> - result of compare operation
   *
   * @throws  NamingException  if the LDAP returns an error
   */
  protected boolean compare(final String dn, final String filter)
    throws NamingException
  {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("Compare with the following parameters:");
      this.logger.debug("  dn = " + dn);
      this.logger.debug("  filter = " + filter);
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("  config = " + this.config.getEnvironment());
      }
    }

    boolean success = false;
    LdapContext ctx = null;
    NamingEnumeration<SearchResult> en = null;
    try {
      for (int i = 0; i <= this.config.getOperationRetry(); i++) {
        try {
          ctx = this.getContext();
          en = ctx.search(dn, filter, LdapConfig.getCompareSearchControls());
          break;
        } catch (CommunicationException e) {
          if (i == this.config.getOperationRetry()) {
            throw e;
          }
          if (this.logger.isWarnEnabled()) {
            this.logger.warn(
              "Error while communicating with the LDAP, retrying",
              e);
          }
          this.reconnect();
        }
      }

      if (en.hasMore()) {
        success = true;
      }
    } finally {
      if (en != null) {
        en.close();
      }
      if (ctx != null) {
        ctx.close();
      }
    }
    return success;
  }


  /**
   * This will query the LDAP with the supplied dn, filter, filter arguments,
   * and return attributes. This method will perform a search whose scope is
   * defined in the <code>LdapConfig</code>, the default scope is subtree. The
   * resulting <code>Iterator</code> is a deep copy of the original search
   * results. If filterArgs is null, then no variable substitution will occur If
   * retAttrs is null then all attributes will be returned. If retAttrs is an
   * empty array then no attributes will be returned.
   *
   * @param  dn  <code>String</code> name to begin search at
   * @param  filter  <code>String</code> expression to use for the search
   * @param  filterArgs  <code>Object[]</code> to substitute for variables in
   * the filter
   * @param  retAttrs  <code>String[]</code> attributes to return
   * @param  handler  <code>SearchResultHandler[]</code> to post process results
   *
   * @return  <code>Iterator</code> - of LDAP search results
   *
   * @throws  NamingException  if the LDAP returns an error
   */
  protected Iterator<SearchResult> search(
    final String dn,
    final String filter,
    final Object[] filterArgs,
    final String[] retAttrs,
    final SearchResultHandler... handler)
    throws NamingException
  {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("Search with the following parameters:");
      this.logger.debug("  dn = " + dn);
      this.logger.debug("  filter = " + filter);
      this.logger.debug(
        "  filterArgs = " +
        (filterArgs == null ? "none" : Arrays.asList(filterArgs)));
      this.logger.debug(
        "  retAttrs = " +
        (retAttrs == null ? "all attributes" : Arrays.asList(retAttrs)));
      this.logger.debug(
        "  handler = " + (handler == null ? "null" : Arrays.asList(handler)));
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("  config = " + this.config.getEnvironment());
      }
    }

    List<SearchResult> results = null;
    LdapContext ctx = null;
    NamingEnumeration<SearchResult> en = null;
    try {
      for (int i = 0; i <= this.config.getOperationRetry(); i++) {
        try {
          ctx = this.getContext();
          en = ctx.search(
            dn,
            filter,
            filterArgs,
            this.config.getSearchControls(retAttrs));
          break;
        } catch (CommunicationException e) {
          if (i == this.config.getOperationRetry()) {
            throw e;
          }
          if (this.logger.isWarnEnabled()) {
            this.logger.warn(
              "Error while communicating with the LDAP, retrying",
              e);
          }
          this.reconnect();
        }
      }

      final SearchCriteria sc = new SearchCriteria();
      if (ctx != null && !ctx.getNameInNamespace().equals("")) {
        sc.setDn(ctx.getNameInNamespace());
      } else {
        sc.setDn(dn);
      }
      sc.setFilter(filter);
      sc.setFilterArgs(filterArgs);
      sc.setReturnAttrs(retAttrs);
      if (handler != null && handler.length > 0) {
        for (int i = 0; i < handler.length; i++) {
          if (i == 0) {
            results = handler[i].process(sc, en);
          } else {
            results = handler[i].process(sc, results);
          }
        }
      } else {
        results = SR_COPY_RESULT_HANDLER.process(sc, en);
      }
    } finally {
      if (en != null) {
        en.close();
      }
      if (ctx != null) {
        ctx.close();
      }
    }
    return results.iterator();
  }


  /**
   * This will query the LDAP for the supplied dn, matching attributes and
   * return attributes. This method will always perform a one level search. The
   * resulting <code>Iterator</code> is a deep copy of the original search
   * results. If matchAttrs is empty or null then all objects in the target
   * context are returned. If retAttrs is null then all attributes will be
   * returned. If retAttrs is an empty array then no attributes will be
   * returned.
   *
   * @param  dn  <code>String</code> name to search in
   * @param  matchAttrs  <code>Attributes</code> attributes to match
   * @param  retAttrs  <code>String[]</code> attributes to return
   * @param  handler  <code>SearchResultHandler[]</code> to post process results
   *
   * @return  <code>Iterator</code> - of LDAP search results
   *
   * @throws  NamingException  if the LDAP returns an error
   */
  protected Iterator<SearchResult> searchAttributes(
    final String dn,
    final Attributes matchAttrs,
    final String[] retAttrs,
    final SearchResultHandler... handler)
    throws NamingException
  {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("One level search with the following parameters:");
      this.logger.debug("  dn = " + dn);
      this.logger.debug("  matchAttrs = " + matchAttrs);
      this.logger.debug(
        "  retAttrs = " +
        (retAttrs == null ? "all attributes" : Arrays.asList(retAttrs)));
      this.logger.debug(
        "  handler = " + (handler == null ? "null" : Arrays.asList(handler)));
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("  config = " + this.config.getEnvironment());
      }
    }

    List<SearchResult> results = null;
    LdapContext ctx = null;
    NamingEnumeration<SearchResult> en = null;
    try {
      for (int i = 0; i <= this.config.getOperationRetry(); i++) {
        try {
          ctx = this.getContext();
          en = ctx.search(dn, matchAttrs, retAttrs);
          break;
        } catch (CommunicationException e) {
          if (i == this.config.getOperationRetry()) {
            throw e;
          }
          if (this.logger.isWarnEnabled()) {
            this.logger.warn(
              "Error while communicating with the LDAP, retrying",
              e);
          }
          this.reconnect();
        }
      }

      final SearchCriteria sc = new SearchCriteria();
      if (ctx != null && !ctx.getNameInNamespace().equals("")) {
        sc.setDn(ctx.getNameInNamespace());
      } else {
        sc.setDn(dn);
      }
      sc.setMatchAttrs(matchAttrs);
      sc.setReturnAttrs(retAttrs);
      if (handler != null && handler.length > 0) {
        for (int i = 0; i < handler.length; i++) {
          if (i == 0) {
            results = handler[i].process(sc, en);
          } else {
            results = handler[i].process(sc, results);
          }
        }
      } else {
        results = SR_COPY_RESULT_HANDLER.process(sc, en);
      }
    } finally {
      if (en != null) {
        en.close();
      }
      if (ctx != null) {
        ctx.close();
      }
    }
    return results.iterator();
  }


  /**
   * This will enumerate the names bounds to the specified context, along with
   * the class names of objects bound to them. The resulting <code>
   * Iterator</code> is a deep copy of the original search results.
   *
   * @param  dn  <code>String</code> LDAP context to list
   *
   * @return  <code>Iterator</code> - LDAP search result
   *
   * @throws  NamingException  if the LDAP returns an error
   */
  protected Iterator<NameClassPair> list(final String dn)
    throws NamingException
  {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("list with the following parameters:");
      this.logger.debug("  dn = " + dn);
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("  config = " + this.config.getEnvironment());
      }
    }

    List<NameClassPair> results = null;
    LdapContext ctx = null;
    NamingEnumeration<NameClassPair> en = null;
    try {
      for (int i = 0; i <= this.config.getOperationRetry(); i++) {
        try {
          ctx = this.getContext();
          en = ctx.list(dn);
          break;
        } catch (CommunicationException e) {
          if (i == this.config.getOperationRetry()) {
            throw e;
          }
          if (this.logger.isWarnEnabled()) {
            this.logger.warn(
              "Error while communicating with the LDAP, retrying",
              e);
          }
          this.reconnect();
        }
      }

      final SearchCriteria sc = new SearchCriteria();
      if (ctx != null && !ctx.getNameInNamespace().equals("")) {
        sc.setDn(ctx.getNameInNamespace());
      } else {
        sc.setDn(dn);
      }
      results = NCP_COPY_RESULT_HANDLER.process(sc, en);
    } finally {
      if (en != null) {
        en.close();
      }
      if (ctx != null) {
        ctx.close();
      }
    }
    return results.iterator();
  }


  /**
   * This will enumerate the names bounds to the specified context, along with
   * the objects bound to them. The resulting <code>Iterator</code> is a deep
   * copy of the original search results.
   *
   * @param  dn  <code>String</code> LDAP context to list
   *
   * @return  <code>Iterator</code> - LDAP search result
   *
   * @throws  NamingException  if the LDAP returns an error
   */
  protected Iterator<Binding> listBindings(final String dn)
    throws NamingException
  {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("listBindings with the following parameters:");
      this.logger.debug("  dn = " + dn);
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("  config = " + this.config.getEnvironment());
      }
    }

    List<Binding> results = null;
    LdapContext ctx = null;
    NamingEnumeration<Binding> en = null;
    try {
      for (int i = 0; i <= this.config.getOperationRetry(); i++) {
        try {
          ctx = this.getContext();
          en = ctx.listBindings(dn);
          break;
        } catch (CommunicationException e) {
          if (i == this.config.getOperationRetry()) {
            throw e;
          }
          if (this.logger.isWarnEnabled()) {
            this.logger.warn(
              "Error while communicating with the LDAP, retrying",
              e);
          }
          this.reconnect();
        }
      }

      final SearchCriteria sc = new SearchCriteria();
      if (ctx != null && !ctx.getNameInNamespace().equals("")) {
        sc.setDn(ctx.getNameInNamespace());
      } else {
        sc.setDn(dn);
      }
      results = BINDING_COPY_RESULT_HANDLER.process(sc, en);
    } finally {
      if (en != null) {
        en.close();
      }
      if (ctx != null) {
        ctx.close();
      }
    }
    return results.iterator();
  }


  /**
   * This will return the matching attributes associated with the supplied dn.
   * If retAttrs is null then all attributes will be returned. If retAttrs is an
   * empty array then no attributes will be returned.
   *
   * @param  dn  <code>String</code> named object in the LDAP
   * @param  retAttrs  <code>String[]</code> attributes to return
   * @param  handler  <code>AttributeHandler[]</code> to post process results
   *
   * @return  <code>Attributes</code>
   *
   * @throws  NamingException  if the LDAP returns an error
   */
  protected Attributes getAttributes(
    final String dn,
    final String[] retAttrs,
    final AttributeHandler... handler)
    throws NamingException
  {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("Attribute search with the following parameters:");
      this.logger.debug("  dn = " + dn);
      this.logger.debug(
        "  retAttrs = " +
        (retAttrs == null ? "all attributes" : Arrays.asList(retAttrs)));
      this.logger.debug(
        "  handler = " + (handler == null ? "null" : Arrays.asList(handler)));
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("  config = " + this.config.getEnvironment());
      }
    }

    LdapContext ctx = null;
    Attributes attrs = null;
    try {
      for (int i = 0; i <= this.config.getOperationRetry(); i++) {
        try {
          ctx = this.getContext();
          attrs = ctx.getAttributes(dn, retAttrs);
          break;
        } catch (CommunicationException e) {
          if (i == this.config.getOperationRetry()) {
            throw e;
          }
          if (this.logger.isWarnEnabled()) {
            this.logger.warn(
              "Error while communicating with the LDAP, retrying",
              e);
          }
          this.reconnect();
        }
      }

      if (handler != null && handler.length > 0) {
        final SearchCriteria sc = new SearchCriteria();
        if (ctx != null && !ctx.getNameInNamespace().equals("")) {
          sc.setDn(ctx.getNameInNamespace());
        } else {
          sc.setDn(dn);
        }
        for (int i = 0; i < handler.length; i++) {
          attrs = AttributesProcessor.executeHandler(sc, attrs, handler[i]);
        }
      }
    } finally {
      if (ctx != null) {
        ctx.close();
      }
    }
    return attrs;
  }


  /**
   * This will return the LDAP schema associated with the supplied dn. The
   * resulting <code>Iterator</code> is a deep copy of the original search
   * results.
   *
   * @param  dn  <code>String</code> named object in the LDAP
   *
   * @return  <code>Iterator</code> - LDAP search result
   *
   * @throws  NamingException  if the LDAP returns an error
   */
  protected Iterator<SearchResult> getSchema(final String dn)
    throws NamingException
  {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("Schema search with the following parameters:");
      this.logger.debug("  dn = " + dn);
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("  config = " + this.config.getEnvironment());
      }
    }

    List<SearchResult> results = null;
    LdapContext ctx = null;
    DirContext schema = null;
    NamingEnumeration<SearchResult> en = null;
    try {
      for (int i = 0; i <= this.config.getOperationRetry(); i++) {
        try {
          ctx = this.getContext();
          schema = ctx.getSchema(dn);
          en = schema.search("", null);
          break;
        } catch (CommunicationException e) {
          if (i == this.config.getOperationRetry()) {
            throw e;
          }
          if (this.logger.isWarnEnabled()) {
            this.logger.warn(
              "Error while communicating with the LDAP, retrying",
              e);
          }
          this.reconnect();
        }
      }

      final SearchCriteria sc = new SearchCriteria();
      if (ctx != null && !ctx.getNameInNamespace().equals("")) {
        sc.setDn(ctx.getNameInNamespace());
      } else {
        sc.setDn(dn);
      }
      results = SR_COPY_RESULT_HANDLER.process(sc, en);
    } finally {
      if (schema != null) {
        schema.close();
      }
      if (en != null) {
        en.close();
      }
      if (ctx != null) {
        ctx.close();
      }
    }
    return results.iterator();
  }


  /**
   * This will modify the supplied attributes for the supplied value given by
   * the modification operation. modOp must be one of: ADD_ATTRIBUTE,
   * REPLACE_ATTRIBUTE, REMOVE_ATTRIBUTE.
   *
   * @param  dn  <code>String</code> named object in the LDAP
   * @param  modOp  <code>int</code> modification operation
   * @param  attrs  <code>Attributes</code> attributes to be used for the
   * operation, may be null
   *
   * @throws  NamingException  if the LDAP returns an error
   */
  protected void modifyAttributes(
    final String dn,
    final int modOp,
    final Attributes attrs)
    throws NamingException
  {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("Modifiy attributes with the following parameters:");
      this.logger.debug("  dn = " + dn);
      this.logger.debug("  modOp = " + modOp);
      this.logger.debug("  attrs = " + attrs);
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("  config = " + this.config.getEnvironment());
      }
    }

    LdapContext ctx = null;
    try {
      for (int i = 0; i <= this.config.getOperationRetry(); i++) {
        try {
          ctx = this.getContext();
          ctx.modifyAttributes(dn, modOp, attrs);
          break;
        } catch (CommunicationException e) {
          if (i == this.config.getOperationRetry()) {
            throw e;
          }
          if (this.logger.isWarnEnabled()) {
            this.logger.warn(
              "Error while communicating with the LDAP, retrying",
              e);
          }
          this.reconnect();
        }
      }
    } finally {
      if (ctx != null) {
        ctx.close();
      }
    }
  }


  /**
   * This will create the supplied dn in the LDAP namespace with the supplied
   * attributes.
   *
   * @param  dn  <code>String</code> named object in the LDAP
   * @param  attrs  <code>Attributes</code> attributes to be added to this entry
   *
   * @throws  NamingException  if the LDAP returns an error
   */
  protected void create(final String dn, final Attributes attrs)
    throws NamingException
  {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("Create name with the following parameters:");
      this.logger.debug("  dn = " + dn);
      this.logger.debug("  attrs = " + attrs);
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("  config = " + this.config.getEnvironment());
      }
    }

    LdapContext ctx = null;
    try {
      for (int i = 0; i <= this.config.getOperationRetry(); i++) {
        try {
          ctx = this.getContext();
          ctx.createSubcontext(dn, attrs).close();
          break;
        } catch (CommunicationException e) {
          if (i == this.config.getOperationRetry()) {
            throw e;
          }
          if (this.logger.isWarnEnabled()) {
            this.logger.warn(
              "Error while communicating with the LDAP, retrying",
              e);
          }
          this.reconnect();
        }
      }
    } finally {
      if (ctx != null) {
        ctx.close();
      }
    }
  }


  /**
   * This will delete the supplied dn from the LDAP namespace.
   *
   * @param  dn  <code>String</code> named object in the LDAP
   *
   * @throws  NamingException  if the LDAP returns an error
   */
  protected void delete(final String dn)
    throws NamingException
  {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("Delete name with the following parameters:");
      this.logger.debug("  dn = " + dn);
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("  config = " + this.config.getEnvironment());
      }
    }

    LdapContext ctx = null;
    try {
      for (int i = 0; i <= this.config.getOperationRetry(); i++) {
        try {
          ctx = this.getContext();
          ctx.destroySubcontext(dn);
          break;
        } catch (CommunicationException e) {
          if (i == this.config.getOperationRetry()) {
            throw e;
          }
          if (this.logger.isWarnEnabled()) {
            this.logger.warn(
              "Error while communicating with the LDAP, retrying",
              e);
          }
          this.reconnect();
        }
      }
    } finally {
      if (ctx != null) {
        ctx.close();
      }
    }
  }


  /**
   * This returns whether the <code>Ldap</code> is using the TLS protocol for
   * connections.
   *
   * @return  <code>boolean</code> - whether the TLS protocol is being used
   */
  public boolean isTlsEnabled()
  {
    return this.config.isTlsEnabled();
  }


  /**
   * This sets this <code>Ldap</code> to use the TLS protocol for connections.
   * If tls is true this method will upgrade the existing connection if it has
   * already been established. If tls is false this method will downgrade the
   * existing connection if it has already been established. If no connection
   * has been established, then the next connection made will take this setting
   * into effect.
   *
   * @param  tls  <code>boolean</code> - whether LDAP connections should use the
   * TLS protocol
   *
   * @throws  NamingException  if an error occurs while requesting an extended
   * operation
   */
  public void useTls(final boolean tls)
    throws NamingException
  {
    if (this.context != null) {
      if (tls) {
        if (this.tlsResponse == null) {
          this.tlsResponse = this.startTls(this.context);
        }
      } else {
        this.stopTls(this.tlsResponse);
      }
    }
  }


  /**
   * This will establish a connection if one does not already exist by binding
   * to the LDAP using parameters given by {@link
   * LdapConfig#setServiceUser(String)} and {@link
   * LdapConfig#setServiceCredential(Object)}. If these parameters have not been
   * set then an anonymous bind will be attempted. This connection must be
   * closed using {@link #close}. Any method which requires a LDAP connection
   * will call this method independently. This method should only be used if you
   * need to verify that you can connect to the LDAP.
   *
   * @return  <code>boolean</code> - whether the connection was successfull
   *
   * @throws  NamingException  if the LDAP cannot be reached
   */
  public synchronized boolean connect()
    throws NamingException
  {
    boolean success = false;
    if (this.context != null) {
      success = true;
    } else {
      this.context = this.bind(
        this.config.getServiceUser(),
        this.config.getServiceCredential(),
        this.tlsResponse);
      success = true;
    }
    return success;
  }


  /**
   * This will close the current connection to the LDAP and establish a new
   * connection to the LDAP using {@link #connect}.
   *
   * @return  <code>boolean</code> - whether the connection was successfull
   *
   * @throws  NamingException  if the LDAP cannot be reached
   */
  public synchronized boolean reconnect()
    throws NamingException
  {
    this.close();
    return this.connect();
  }


  /** This will close the connection to the LDAP. */
  public void close()
  {
    try {
      this.stopTls(this.tlsResponse);
    } catch (NamingException e) {
      if (this.logger.isErrorEnabled()) {
        this.logger.error("Error stopping TLS", e);
      }
    } finally {
      try {
        if (this.context != null) {
          this.context.close();
          this.context = null;
        }
      } catch (NamingException e) {
        if (this.logger.isErrorEnabled()) {
          this.logger.error("Error closing connection with the LDAP", e);
        }
      }
    }
  }


  /**
   * This will return an initialized connection to the LDAP.
   *
   * @return  <code>LdapContext</code>
   *
   * @throws  NamingException  if the LDAP returns an error
   */
  protected LdapContext getContext()
    throws NamingException
  {
    this.connect();
    return this.context.newInstance(null);
  }


  /**
   * This initiates a new connection to the LDAP. If {@link
   * LdapConfig#getAuthtype()} is 'none', then anonymous auth is attempted. If
   * dn or credential is null, then anonymous auth is attempted. tls can be null
   * unless {@link #useTls(boolean)} has been set to true.
   *
   * @param  dn  <code>String</code> to attempt bind with
   * @param  credential  <code>Object</code> to attempt bind with
   * @param  tls  <code>StartTlsResponse</code> to control TLS
   *
   * @return  <code>boolean</code> - whether the connection succeeded
   *
   * @throws  NamingException  if an error occurs while creating the Context or
   * requesting an extended operation
   */
  protected LdapContext bind(
    final String dn,
    final Object credential,
    StartTlsResponse tls)
    throws NamingException
  {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("Bind with the following parameters:");
      this.logger.debug("  dn = " + dn);
      if (this.config.getLogCredentials()) {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug("  credential = " + credential);
        }
      } else {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug("  credential = <suppressed>");
        }
      }
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("  config = " + this.config.getEnvironment());
      }
    }

    final Hashtable<String, Object> environment = new Hashtable<String, Object>(
      this.config.getEnvironment());
    String authtype = this.config.getAuthtype();

    // set authtype to none if no credentials and not using SASL
    if (!this.config.isSaslAuth() && (dn == null || credential == null)) {
      if (this.logger.isTraceEnabled()) {
        this.logger.trace(
          "dn or credential is null, authtype set to " +
          LdapConstants.NONE_AUTHTYPE);
      }
      authtype = LdapConstants.NONE_AUTHTYPE;
    }

    // if using TLS, then credentials must be added after connection is made
    if (this.config.isTlsEnabled()) {
      environment.put(LdapConstants.VERSION, LdapConstants.VERSION_THREE);
    } else {
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("TLS not used");
        this.logger.trace("authtype is " + authtype);
      }
      environment.put(LdapConstants.AUTHENTICATION, authtype);
      // do not set credentials if authtype is none, sasl external, or gssapi
      if (
        !this.config.isExternalAuth() &&
          !this.config.isGSSAPIAuth() &&
          !authtype.equals(LdapConstants.NONE_AUTHTYPE)) {
        environment.put(LdapConstants.PRINCIPAL, dn);
        environment.put(LdapConstants.CREDENTIALS, credential);
      }
    }

    LdapContext context = null;
    try {
      context = new InitialLdapContext(environment, null);

      if (this.config.isTlsEnabled()) {
        if (this.logger.isTraceEnabled()) {
          this.logger.trace("TLS will be used");
          this.logger.trace("authtype is " + authtype);
        }
        tls = this.startTls(context);
        context.addToEnvironment(LdapConstants.AUTHENTICATION, authtype);
        // do not set credentials if authtype is none, sasl external, or gssapi
        if (
          !this.config.isExternalAuth() &&
            !this.config.isGSSAPIAuth() &&
            !authtype.equals(LdapConstants.NONE_AUTHTYPE)) {
          context.addToEnvironment(LdapConstants.PRINCIPAL, dn);
          context.addToEnvironment(LdapConstants.CREDENTIALS, credential);
        }
        context.reconnect(null);
      }
    } catch (NamingException e) {
      if (context != null) {
        context.close();
      }
      throw e;
    }

    return context;
  }


  /**
   * This will attempt to StartTLS with the supplied <code>LdapContext</code>.
   *
   * @param  context  <code>LdapContext</code>
   *
   * @return  <code>StartTlsResponse</code>
   *
   * @throws  NamingException  if an error occurs while requesting an extended
   * operation
   */
  protected StartTlsResponse startTls(final LdapContext context)
    throws NamingException
  {
    StartTlsResponse tls = null;
    try {
      for (int i = 0; i <= this.config.getOperationRetry(); i++) {
        try {
          tls = (StartTlsResponse) context.extendedOperation(
            new StartTlsRequest());
          if (this.config.useHostnameVerifier()) {
            if (this.logger.isTraceEnabled()) {
              this.logger.trace(
                "TLS hostnameVerifier = " + this.config.getHostnameVerifier());
            }
            tls.setHostnameVerifier(this.config.getHostnameVerifier());
          }
          if (this.config.useSslSocketFactory()) {
            if (this.logger.isTraceEnabled()) {
              this.logger.trace(
                "TLS sslSocketFactory = " + this.config.getSslSocketFactory());
            }
            tls.negotiate(this.config.getSslSocketFactory());
          } else {
            tls.negotiate();
          }
          break;
        } catch (CommunicationException e) {
          if (i == this.config.getOperationRetry()) {
            throw e;
          }
          if (this.logger.isWarnEnabled()) {
            this.logger.warn(
              "Error while communicating with the LDAP, retrying",
              e);
          }
          this.reconnect();
        }
      }
    } catch (IOException e) {
      if (this.logger.isErrorEnabled()) {
        this.logger.error("Could not negotiate TLS connection", e);
      }
      throw new CommunicationException(e.getMessage());
    }
    return tls;
  }


  /**
   * This will attempt to StopTLS with the supplied <code>
   * StartTlsResponse</code>.
   *
   * @param  tls  <code>StartTlsResponse</code>
   *
   * @throws  NamingException  if an error occurs while closing the TLS
   * connection
   */
  protected void stopTls(final StartTlsResponse tls)
    throws NamingException
  {
    if (tls != null) {
      try {
        tls.close();
      } catch (IOException e) {
        if (this.logger.isErrorEnabled()) {
          this.logger.error("Could not close TLS connection", e);
        }
        throw new CommunicationException(e.getMessage());
      }
    }
  }


  /**
   * Provides a descriptive string representation of this instance.
   *
   * @return  String of the form $Classname@hashCode::config=$config.
   */
  @Override
  public String toString()
  {
    return
      String.format(
        "%s@%d::config=%s",
        this.getClass().getName(),
        this.hashCode(),
        this.config);
  }


  /**
   * Called by the garbage collector on an object when garbage collection
   * determines that there are no more references to the object.
   *
   * @throws  Throwable  if an exception is thrown by this method
   */
  protected void finalize()
    throws Throwable
  {
    try {
      this.close();
    } finally {
      super.finalize();
    }
  }
}
/*
  $Id: AbstractLdap.java 663 2009-09-24 02:06:06Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 663 $
  Updated: $Date: 2009-09-23 19:06:06 -0700 (Wed, 23 Sep 2009) $
*/
package edu.vt.middleware.ldap;

import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import javax.naming.Binding;
import javax.naming.CommunicationException;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.StartTlsResponse;
import edu.vt.middleware.ldap.handler.AttributeHandler;
import edu.vt.middleware.ldap.handler.AttributesProcessor;
import edu.vt.middleware.ldap.handler.CopyResultHandler;
import edu.vt.middleware.ldap.handler.SearchCriteria;
import edu.vt.middleware.ldap.handler.SearchResultHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <code>AbstractLdap</code> contains the functions for basic interaction with a
 * LDAP. Methods are provided for connecting, binding, querying and updating.
 *
 * @param  <T>  type of LdapConfig
 *
 * @author  Middleware Services
 * @version  $Revision: 663 $ $Date: 2009-09-23 19:06:06 -0700 (Wed, 23 Sep 2009) $
 */
public abstract class AbstractLdap<T extends LdapConfig> implements BaseLdap
{

  /** Default copy search result handler, used if none supplied. */
  protected static final CopyResultHandler<SearchResult>
  SR_COPY_RESULT_HANDLER = new CopyResultHandler<SearchResult>();

  /** Default copy name class pair handler. */
  protected static final CopyResultHandler<NameClassPair>
  NCP_COPY_RESULT_HANDLER = new CopyResultHandler<NameClassPair>();

  /** Default copy binding handler. */
  protected static final CopyResultHandler<Binding>
  BINDING_COPY_RESULT_HANDLER = new CopyResultHandler<Binding>();

  /** Default copy result handler. */
  protected static final CopyResultHandler<Object> COPY_RESULT_HANDLER =
    new CopyResultHandler<Object>();

  /** Log for this class. */
  protected final Log logger = LogFactory.getLog(this.getClass());

  /** LDAP interface for directory operations. */
  protected LdapContext context;

  /** TLS Session. */
  protected StartTlsResponse tlsResponse;

  /** LDAP configuration environment. */
  protected T config;


  /**
   * This will set the config parameters of this <code>Ldap</code>.
   *
   * @param  ldapConfig  <code>LdapConfig</code>
   */
  protected void setLdapConfig(final T ldapConfig)
  {
    if (this.config != null) {
      this.config.checkImmutable();
    }
    this.config = ldapConfig;
    if (this.config.isTlsEnabled()) {
      try {
        this.useTls(true);
      } catch (NamingException e) {
        if (this.logger.isErrorEnabled()) {
          this.logger.error("Error using TLS", e);
        }
      }
    }
  }


  /**
   * This will perform an LDAP compare operation with the supplied filter and
   * dn.
   *
   * @param  dn  <code>String</code> name to compare
   * @param  filter  <code>String</code> expression to use for compare
   * @param  filterArgs  <code>Object[]</code> to substitute for variables in
   * the filter
   *
   * @return  <code>boolean</code> - result of compare operation
   *
   * @throws  NamingException  if the LDAP returns an error
   */
  protected boolean compare(
    final String dn, final String filter, final Object[] filterArgs)
    throws NamingException
  {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("Compare with the following parameters:");
      this.logger.debug("  dn = " + dn);
      this.logger.debug("  filter = " + filter);
      this.logger.debug(
        "  filterArgs = " +
        (filterArgs == null ? "none" : Arrays.asList(filterArgs)));
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("  config = " + this.config.getEnvironment());
      }
    }

    boolean success = false;
    LdapContext ctx = null;
    NamingEnumeration<SearchResult> en = null;
    try {
      for (int i = 0; i <= this.config.getOperationRetry(); i++) {
        try {
          ctx = this.getContext();
          en = ctx.search(
            dn, filter, filterArgs, LdapConfig.getCompareSearchControls());
          break;
        } catch (CommunicationException e) {
          if (i == this.config.getOperationRetry()) {
            throw e;
          }
          if (this.logger.isWarnEnabled()) {
            this.logger.warn(
              "Error while communicating with the LDAP, retrying",
              e);
          }
          this.reconnect();
        }
      }

      if (en.hasMore()) {
        success = true;
      }
    } finally {
      if (en != null) {
        en.close();
      }
      if (ctx != null) {
        ctx.close();
      }
    }
    return success;
  }


  /**
   * This will query the LDAP with the supplied dn, filter, filter arguments,
   * and return attributes. This method will perform a search whose scope is
   * defined in the <code>LdapConfig</code>, the default scope is subtree. The
   * resulting <code>Iterator</code> is a deep copy of the original search
   * results. If filterArgs is null, then no variable substitution will occur.
   * If retAttrs is null then all attributes will be returned. If retAttrs is an
   * empty array then no attributes will be returned.
   *
   * @param  dn  <code>String</code> name to begin search at
   * @param  filter  <code>String</code> expression to use for the search
   * @param  filterArgs  <code>Object[]</code> to substitute for variables in
   * the filter
   * @param  retAttrs  <code>String[]</code> attributes to return
   * @param  handler  <code>SearchResultHandler[]</code> to post process results
   *
   * @return  <code>Iterator</code> - of LDAP search results
   *
   * @throws  NamingException  if the LDAP returns an error
   */
  protected Iterator<SearchResult> search(
    final String dn,
    final String filter,
    final Object[] filterArgs,
    final String[] retAttrs,
    final SearchResultHandler... handler)
    throws NamingException
  {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("Search with the following parameters:");
      this.logger.debug("  dn = " + dn);
      this.logger.debug("  filter = " + filter);
      this.logger.debug(
        "  filterArgs = " +
        (filterArgs == null ? "none" : Arrays.asList(filterArgs)));
      this.logger.debug(
        "  retAttrs = " +
        (retAttrs == null ? "all attributes" : Arrays.asList(retAttrs)));
      this.logger.debug(
        "  handler = " + (handler == null ? "null" : Arrays.asList(handler)));
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("  config = " + this.config.getEnvironment());
      }
    }

    List<SearchResult> results = null;
    LdapContext ctx = null;
    NamingEnumeration<SearchResult> en = null;
    try {
      for (int i = 0; i <= this.config.getOperationRetry(); i++) {
        try {
          ctx = this.getContext();
          en = ctx.search(
            dn,
            filter,
            filterArgs,
            this.config.getSearchControls(retAttrs));
          break;
        } catch (CommunicationException e) {
          if (i == this.config.getOperationRetry()) {
            throw e;
          }
          if (this.logger.isWarnEnabled()) {
            this.logger.warn(
              "Error while communicating with the LDAP, retrying",
              e);
          }
          this.reconnect();
        }
      }

      final SearchCriteria sc = new SearchCriteria();
      if (ctx != null && !ctx.getNameInNamespace().equals("")) {
        sc.setDn(ctx.getNameInNamespace());
      } else {
        sc.setDn(dn);
      }
      sc.setFilter(filter);
      sc.setFilterArgs(filterArgs);
      sc.setReturnAttrs(retAttrs);
      if (handler != null && handler.length > 0) {
        for (int i = 0; i < handler.length; i++) {
          if (i == 0) {
            results = handler[i].process(
              sc, en, this.config.getSearchIgnoreExceptions());
          } else {
            results = handler[i].process(sc, results);
          }
        }
      } else {
        results = SR_COPY_RESULT_HANDLER.process(
          sc, en, this.config.getSearchIgnoreExceptions());
      }
    } finally {
      if (en != null) {
        en.close();
      }
      if (ctx != null) {
        ctx.close();
      }
    }
    return results.iterator();
  }


  /**
   * This will query the LDAP for the supplied dn, matching attributes and
   * return attributes. This method will always perform a one level search. The
   * resulting <code>Iterator</code> is a deep copy of the original search
   * results. If matchAttrs is empty or null then all objects in the target
   * context are returned. If retAttrs is null then all attributes will be
   * returned. If retAttrs is an empty array then no attributes will be
   * returned.
   *
   * @param  dn  <code>String</code> name to search in
   * @param  matchAttrs  <code>Attributes</code> attributes to match
   * @param  retAttrs  <code>String[]</code> attributes to return
   * @param  handler  <code>SearchResultHandler[]</code> to post process results
   *
   * @return  <code>Iterator</code> - of LDAP search results
   *
   * @throws  NamingException  if the LDAP returns an error
   */
  protected Iterator<SearchResult> searchAttributes(
    final String dn,
    final Attributes matchAttrs,
    final String[] retAttrs,
    final SearchResultHandler... handler)
    throws NamingException
  {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("One level search with the following parameters:");
      this.logger.debug("  dn = " + dn);
      this.logger.debug("  matchAttrs = " + matchAttrs);
      this.logger.debug(
        "  retAttrs = " +
        (retAttrs == null ? "all attributes" : Arrays.asList(retAttrs)));
      this.logger.debug(
        "  handler = " + (handler == null ? "null" : Arrays.asList(handler)));
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("  config = " + this.config.getEnvironment());
      }
    }

    List<SearchResult> results = null;
    LdapContext ctx = null;
    NamingEnumeration<SearchResult> en = null;
    try {
      for (int i = 0; i <= this.config.getOperationRetry(); i++) {
        try {
          ctx = this.getContext();
          en = ctx.search(dn, matchAttrs, retAttrs);
          break;
        } catch (CommunicationException e) {
          if (i == this.config.getOperationRetry()) {
            throw e;
          }
          if (this.logger.isWarnEnabled()) {
            this.logger.warn(
              "Error while communicating with the LDAP, retrying",
              e);
          }
          this.reconnect();
        }
      }

      final SearchCriteria sc = new SearchCriteria();
      if (ctx != null && !ctx.getNameInNamespace().equals("")) {
        sc.setDn(ctx.getNameInNamespace());
      } else {
        sc.setDn(dn);
      }
      sc.setMatchAttrs(matchAttrs);
      sc.setReturnAttrs(retAttrs);
      if (handler != null && handler.length > 0) {
        for (int i = 0; i < handler.length; i++) {
          if (i == 0) {
            results = handler[i].process(
              sc, en, this.config.getSearchIgnoreExceptions());
          } else {
            results = handler[i].process(sc, results);
          }
        }
      } else {
        results = SR_COPY_RESULT_HANDLER.process(
          sc, en, this.config.getSearchIgnoreExceptions());
      }
    } finally {
      if (en != null) {
        en.close();
      }
      if (ctx != null) {
        ctx.close();
      }
    }
    return results.iterator();
  }


  /**
   * This will enumerate the names bounds to the specified context, along with
   * the class names of objects bound to them. The resulting <code>
   * Iterator</code> is a deep copy of the original search results.
   *
   * @param  dn  <code>String</code> LDAP context to list
   *
   * @return  <code>Iterator</code> - LDAP search result
   *
   * @throws  NamingException  if the LDAP returns an error
   */
  protected Iterator<NameClassPair> list(final String dn)
    throws NamingException
  {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("list with the following parameters:");
      this.logger.debug("  dn = " + dn);
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("  config = " + this.config.getEnvironment());
      }
    }

    List<NameClassPair> results = null;
    LdapContext ctx = null;
    NamingEnumeration<NameClassPair> en = null;
    try {
      for (int i = 0; i <= this.config.getOperationRetry(); i++) {
        try {
          ctx = this.getContext();
          en = ctx.list(dn);
          break;
        } catch (CommunicationException e) {
          if (i == this.config.getOperationRetry()) {
            throw e;
          }
          if (this.logger.isWarnEnabled()) {
            this.logger.warn(
              "Error while communicating with the LDAP, retrying",
              e);
          }
          this.reconnect();
        }
      }

      final SearchCriteria sc = new SearchCriteria();
      if (ctx != null && !ctx.getNameInNamespace().equals("")) {
        sc.setDn(ctx.getNameInNamespace());
      } else {
        sc.setDn(dn);
      }
      results = NCP_COPY_RESULT_HANDLER.process(
        sc, en, this.config.getSearchIgnoreExceptions());
    } finally {
      if (en != null) {
        en.close();
      }
      if (ctx != null) {
        ctx.close();
      }
    }
    return results.iterator();
  }


  /**
   * This will enumerate the names bounds to the specified context, along with
   * the objects bound to them. The resulting <code>Iterator</code> is a deep
   * copy of the original search results.
   *
   * @param  dn  <code>String</code> LDAP context to list
   *
   * @return  <code>Iterator</code> - LDAP search result
   *
   * @throws  NamingException  if the LDAP returns an error
   */
  protected Iterator<Binding> listBindings(final String dn)
    throws NamingException
  {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("listBindings with the following parameters:");
      this.logger.debug("  dn = " + dn);
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("  config = " + this.config.getEnvironment());
      }
    }

    List<Binding> results = null;
    LdapContext ctx = null;
    NamingEnumeration<Binding> en = null;
    try {
      for (int i = 0; i <= this.config.getOperationRetry(); i++) {
        try {
          ctx = this.getContext();
          en = ctx.listBindings(dn);
          break;
        } catch (CommunicationException e) {
          if (i == this.config.getOperationRetry()) {
            throw e;
          }
          if (this.logger.isWarnEnabled()) {
            this.logger.warn(
              "Error while communicating with the LDAP, retrying",
              e);
          }
          this.reconnect();
        }
      }

      final SearchCriteria sc = new SearchCriteria();
      if (ctx != null && !ctx.getNameInNamespace().equals("")) {
        sc.setDn(ctx.getNameInNamespace());
      } else {
        sc.setDn(dn);
      }
      results = BINDING_COPY_RESULT_HANDLER.process(
        sc, en, this.config.getSearchIgnoreExceptions());
    } finally {
      if (en != null) {
        en.close();
      }
      if (ctx != null) {
        ctx.close();
      }
    }
    return results.iterator();
  }


  /**
   * This will return the matching attributes associated with the supplied dn.
   * If retAttrs is null then all attributes will be returned. If retAttrs is an
   * empty array then no attributes will be returned.
   *
   * @param  dn  <code>String</code> named object in the LDAP
   * @param  retAttrs  <code>String[]</code> attributes to return
   * @param  handler  <code>AttributeHandler[]</code> to post process results
   *
   * @return  <code>Attributes</code>
   *
   * @throws  NamingException  if the LDAP returns an error
   */
  protected Attributes getAttributes(
    final String dn,
    final String[] retAttrs,
    final AttributeHandler... handler)
    throws NamingException
  {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("Attribute search with the following parameters:");
      this.logger.debug("  dn = " + dn);
      this.logger.debug(
        "  retAttrs = " +
        (retAttrs == null ? "all attributes" : Arrays.asList(retAttrs)));
      this.logger.debug(
        "  handler = " + (handler == null ? "null" : Arrays.asList(handler)));
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("  config = " + this.config.getEnvironment());
      }
    }

    LdapContext ctx = null;
    Attributes attrs = null;
    try {
      for (int i = 0; i <= this.config.getOperationRetry(); i++) {
        try {
          ctx = this.getContext();
          attrs = ctx.getAttributes(dn, retAttrs);
          break;
        } catch (CommunicationException e) {
          if (i == this.config.getOperationRetry()) {
            throw e;
          }
          if (this.logger.isWarnEnabled()) {
            this.logger.warn(
              "Error while communicating with the LDAP, retrying",
              e);
          }
          this.reconnect();
        }
      }

      if (handler != null && handler.length > 0) {
        final SearchCriteria sc = new SearchCriteria();
        if (ctx != null && !ctx.getNameInNamespace().equals("")) {
          sc.setDn(ctx.getNameInNamespace());
        } else {
          sc.setDn(dn);
        }
        for (int i = 0; i < handler.length; i++) {
          attrs = AttributesProcessor.executeHandler(sc, attrs, handler[i]);
        }
      }
    } finally {
      if (ctx != null) {
        ctx.close();
      }
    }
    return attrs;
  }


  /**
   * This will return the LDAP schema associated with the supplied dn. The
   * resulting <code>Iterator</code> is a deep copy of the original search
   * results.
   *
   * @param  dn  <code>String</code> named object in the LDAP
   *
   * @return  <code>Iterator</code> - LDAP search result
   *
   * @throws  NamingException  if the LDAP returns an error
   */
  protected Iterator<SearchResult> getSchema(final String dn)
    throws NamingException
  {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("Schema search with the following parameters:");
      this.logger.debug("  dn = " + dn);
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("  config = " + this.config.getEnvironment());
      }
    }

    List<SearchResult> results = null;
    LdapContext ctx = null;
    DirContext schema = null;
    NamingEnumeration<SearchResult> en = null;
    try {
      for (int i = 0; i <= this.config.getOperationRetry(); i++) {
        try {
          ctx = this.getContext();
          schema = ctx.getSchema(dn);
          en = schema.search("", null);
          break;
        } catch (CommunicationException e) {
          if (i == this.config.getOperationRetry()) {
            throw e;
          }
          if (this.logger.isWarnEnabled()) {
            this.logger.warn(
              "Error while communicating with the LDAP, retrying",
              e);
          }
          this.reconnect();
        }
      }

      final SearchCriteria sc = new SearchCriteria();
      if (ctx != null && !ctx.getNameInNamespace().equals("")) {
        sc.setDn(ctx.getNameInNamespace());
      } else {
        sc.setDn(dn);
      }
      results = SR_COPY_RESULT_HANDLER.process(
        sc, en, this.config.getSearchIgnoreExceptions());
    } finally {
      if (schema != null) {
        schema.close();
      }
      if (en != null) {
        en.close();
      }
      if (ctx != null) {
        ctx.close();
      }
    }
    return results.iterator();
  }


  /**
   * This will modify the supplied attributes for the supplied value given by
   * the modification operation. modOp must be one of: ADD_ATTRIBUTE,
   * REPLACE_ATTRIBUTE, REMOVE_ATTRIBUTE. The order of the modifications is not
   * specified. Where possible, the modifications are performed atomically.
   *
   * @param  dn  <code>String</code> named object in the LDAP
   * @param  modOp  <code>int</code> modification operation
   * @param  attrs  <code>Attributes</code> attributes to be used for the
   * operation, may be null
   *
   * @throws  NamingException  if the LDAP returns an error
   */
  protected void modifyAttributes(
    final String dn,
    final int modOp,
    final Attributes attrs)
    throws NamingException
  {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("Modifiy attributes with the following parameters:");
      this.logger.debug("  dn = " + dn);
      this.logger.debug("  modOp = " + modOp);
      this.logger.debug("  attrs = " + attrs);
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("  config = " + this.config.getEnvironment());
      }
    }

    LdapContext ctx = null;
    try {
      for (int i = 0; i <= this.config.getOperationRetry(); i++) {
        try {
          ctx = this.getContext();
          ctx.modifyAttributes(dn, modOp, attrs);
          break;
        } catch (CommunicationException e) {
          if (i == this.config.getOperationRetry()) {
            throw e;
          }
          if (this.logger.isWarnEnabled()) {
            this.logger.warn(
              "Error while communicating with the LDAP, retrying",
              e);
          }
          this.reconnect();
        }
      }
    } finally {
      if (ctx != null) {
        ctx.close();
      }
    }
  }


  /**
   * This will modify the supplied dn using the supplied modifications.
   * The modifications are performed in the order specified. Each modification
   * specifies a modification operation code and an attribute on which to
   * operate. Where possible, the modifications are performed atomically.
   *
   * @param  dn  <code>String</code> named object in the LDAP
   * @param  mods  <code>ModificationItem[]</code> modifications
   *
   * @throws  NamingException  if the LDAP returns an error
   */
  protected void modifyAttributes(
    final String dn,
    final ModificationItem[] mods)
    throws NamingException
  {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("Modifiy attributes with the following parameters:");
      this.logger.debug("  dn = " + dn);
      this.logger.debug(
        "  mods = " + (mods == null ? "null" : Arrays.asList(mods)));
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("  config = " + this.config.getEnvironment());
      }
    }

    LdapContext ctx = null;
    try {
      for (int i = 0; i <= this.config.getOperationRetry(); i++) {
        try {
          ctx = this.getContext();
          ctx.modifyAttributes(dn, mods);
          break;
        } catch (CommunicationException e) {
          if (i == this.config.getOperationRetry()) {
            throw e;
          }
          if (this.logger.isWarnEnabled()) {
            this.logger.warn(
              "Error while communicating with the LDAP, retrying",
              e);
          }
          this.reconnect();
        }
      }
    } finally {
      if (ctx != null) {
        ctx.close();
      }
    }
  }


  /**
   * This will create the supplied dn in the LDAP namespace with the supplied
   * attributes.
   *
   * @param  dn  <code>String</code> named object in the LDAP
   * @param  attrs  <code>Attributes</code> attributes to be added to this entry
   *
   * @throws  NamingException  if the LDAP returns an error
   */
  protected void create(final String dn, final Attributes attrs)
    throws NamingException
  {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("Create name with the following parameters:");
      this.logger.debug("  dn = " + dn);
      this.logger.debug("  attrs = " + attrs);
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("  config = " + this.config.getEnvironment());
      }
    }

    LdapContext ctx = null;
    try {
      for (int i = 0; i <= this.config.getOperationRetry(); i++) {
        try {
          ctx = this.getContext();
          ctx.createSubcontext(dn, attrs).close();
          break;
        } catch (CommunicationException e) {
          if (i == this.config.getOperationRetry()) {
            throw e;
          }
          if (this.logger.isWarnEnabled()) {
            this.logger.warn(
              "Error while communicating with the LDAP, retrying",
              e);
          }
          this.reconnect();
        }
      }
    } finally {
      if (ctx != null) {
        ctx.close();
      }
    }
  }


  /**
   * This will rename the supplied dn in the LDAP namespace.
   *
   * @param  oldDn  <code>String</code> object to rename
   * @param  newDn  <code>String</code> new name
   *
   * @throws  NamingException  if the LDAP returns an error
   */
  protected void rename(final String oldDn, final String newDn)
    throws NamingException
  {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("Rename name with the following parameters:");
      this.logger.debug("  oldDn = " + oldDn);
      this.logger.debug("  newDn = " + newDn);
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("  config = " + this.config.getEnvironment());
      }
    }

    LdapContext ctx = null;
    try {
      for (int i = 0; i <= this.config.getOperationRetry(); i++) {
        try {
          ctx = this.getContext();
          ctx.rename(oldDn, newDn);
          break;
        } catch (CommunicationException e) {
          if (i == this.config.getOperationRetry()) {
            throw e;
          }
          if (this.logger.isWarnEnabled()) {
            this.logger.warn(
              "Error while communicating with the LDAP, retrying",
              e);
          }
          this.reconnect();
        }
      }
    } finally {
      if (ctx != null) {
        ctx.close();
      }
    }
  }


  /**
   * This will delete the supplied dn from the LDAP namespace.
   *
   * @param  dn  <code>String</code> named object in the LDAP
   *
   * @throws  NamingException  if the LDAP returns an error
   */
  protected void delete(final String dn)
    throws NamingException
  {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("Delete name with the following parameters:");
      this.logger.debug("  dn = " + dn);
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("  config = " + this.config.getEnvironment());
      }
    }

    LdapContext ctx = null;
    try {
      for (int i = 0; i <= this.config.getOperationRetry(); i++) {
        try {
          ctx = this.getContext();
          ctx.destroySubcontext(dn);
          break;
        } catch (CommunicationException e) {
          if (i == this.config.getOperationRetry()) {
            throw e;
          }
          if (this.logger.isWarnEnabled()) {
            this.logger.warn(
              "Error while communicating with the LDAP, retrying",
              e);
          }
          this.reconnect();
        }
      }
    } finally {
      if (ctx != null) {
        ctx.close();
      }
    }
  }


  /**
   * This returns whether the <code>Ldap</code> is using the TLS protocol for
   * connections.
   *
   * @return  <code>boolean</code> - whether the TLS protocol is being used
   */
  public boolean isTlsEnabled()
  {
    return this.config.isTlsEnabled();
  }


  /**
   * This sets this <code>Ldap</code> to use the TLS protocol for connections.
   * If tls is true this method will upgrade the existing connection if it has
   * already been established. If tls is false this method will downgrade the
   * existing connection if it has already been established. If no connection
   * has been established, then the next connection made will take this setting
   * into effect.
   *
   * @param  tls  <code>boolean</code> - whether LDAP connections should use the
   * TLS protocol
   *
   * @throws  NamingException  if an error occurs while requesting an extended
   * operation
   */
  public void useTls(final boolean tls)
    throws NamingException
  {
    if (this.context != null) {
      if (tls) {
        if (this.tlsResponse == null) {
          this.tlsResponse = this.startTls(this.context);
        }
      } else {
        this.stopTls(this.tlsResponse);
      }
    }
  }


  /**
   * This will establish a connection if one does not already exist by binding
   * to the LDAP using parameters given by {@link
   * LdapConfig#setServiceUser(String)} and {@link
   * LdapConfig#setServiceCredential(Object)}. If these parameters have not been
   * set then an anonymous bind will be attempted. This connection must be
   * closed using {@link #close}. Any method which requires a LDAP connection
   * will call this method independently. This method should only be used if you
   * need to verify that you can connect to the LDAP.
   *
   * @return  <code>boolean</code> - whether the connection was successfull
   *
   * @throws  NamingException  if the LDAP cannot be reached
   */
  public synchronized boolean connect()
    throws NamingException
  {
    boolean success = false;
    if (this.context != null) {
      success = true;
    } else {
      this.context = this.bind(
        this.config.getServiceUser(),
        this.config.getServiceCredential(),
        this.tlsResponse);
      success = true;
    }
    return success;
  }


  /**
   * This will close the current connection to the LDAP and establish a new
   * connection to the LDAP using {@link #connect}.
   *
   * @return  <code>boolean</code> - whether the connection was successfull
   *
   * @throws  NamingException  if the LDAP cannot be reached
   */
  public synchronized boolean reconnect()
    throws NamingException
  {
    this.close();
    return this.connect();
  }


  /** This will close the connection to the LDAP. */
  public void close()
  {
    try {
      this.stopTls(this.tlsResponse);
    } catch (NamingException e) {
      if (this.logger.isErrorEnabled()) {
        this.logger.error("Error stopping TLS", e);
      }
    } finally {
      try {
        if (this.context != null) {
          this.context.close();
          this.context = null;
        }
      } catch (NamingException e) {
        if (this.logger.isErrorEnabled()) {
          this.logger.error("Error closing connection with the LDAP", e);
        }
      }
    }
  }


  /**
   * This will return an initialized connection to the LDAP.
   *
   * @return  <code>LdapContext</code>
   *
   * @throws  NamingException  if the LDAP returns an error
   */
  protected LdapContext getContext()
    throws NamingException
  {
    this.connect();
    return this.context.newInstance(null);
  }


  /**
   * This initiates a new connection to the LDAP. If {@link
   * LdapConfig#getAuthtype()} is 'none', then anonymous auth is attempted. If
   * dn or credential is null, then anonymous auth is attempted. tls can be null
   * unless {@link #useTls(boolean)} has been set to true.
   *
   * @param  dn  <code>String</code> to attempt bind with
   * @param  credential  <code>Object</code> to attempt bind with
   * @param  tls  <code>StartTlsResponse</code> to control TLS
   *
   * @return  <code>boolean</code> - whether the connection succeeded
   *
   * @throws  NamingException  if an error occurs while creating the Context or
   * requesting an extended operation
   */
  protected LdapContext bind(
    final String dn,
    final Object credential,
    StartTlsResponse tls)
    throws NamingException
  {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("Bind with the following parameters:");
      this.logger.debug("  dn = " + dn);
      if (this.config.getLogCredentials()) {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug("  credential = " + credential);
        }
      } else {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug("  credential = <suppressed>");
        }
      }
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("  config = " + this.config.getEnvironment());
      }
    }

    final Hashtable<String, Object> environment = new Hashtable<String, Object>(
      this.config.getEnvironment());
    String authtype = this.config.getAuthtype();

    // set authtype to none if no credentials and not using SASL
    if (!this.config.isSaslAuth() && (dn == null || credential == null)) {
      if (this.logger.isTraceEnabled()) {
        this.logger.trace(
          "dn or credential is null, authtype set to " +
          LdapConstants.NONE_AUTHTYPE);
      }
      authtype = LdapConstants.NONE_AUTHTYPE;
    }

    // if using TLS, then credentials must be added after connection is made
    if (this.config.isTlsEnabled()) {
      environment.put(LdapConstants.VERSION, LdapConstants.VERSION_THREE);
    } else {
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("TLS not used");
        this.logger.trace("authtype is " + authtype);
      }
      environment.put(LdapConstants.AUTHENTICATION, authtype);
      // do not set credentials if authtype is none, sasl external, or gssapi
      if (
        !this.config.isExternalAuth() &&
          !this.config.isGSSAPIAuth() &&
          !authtype.equals(LdapConstants.NONE_AUTHTYPE)) {
        environment.put(LdapConstants.PRINCIPAL, dn);
        environment.put(LdapConstants.CREDENTIALS, credential);
      }
    }

    LdapContext context = null;
    try {
      context = new InitialLdapContext(environment, null);

      if (this.config.isTlsEnabled()) {
        if (this.logger.isTraceEnabled()) {
          this.logger.trace("TLS will be used");
          this.logger.trace("authtype is " + authtype);
        }
        tls = this.startTls(context);
        context.addToEnvironment(LdapConstants.AUTHENTICATION, authtype);
        // do not set credentials if authtype is none, sasl external, or gssapi
        if (
          !this.config.isExternalAuth() &&
            !this.config.isGSSAPIAuth() &&
            !authtype.equals(LdapConstants.NONE_AUTHTYPE)) {
          context.addToEnvironment(LdapConstants.PRINCIPAL, dn);
          context.addToEnvironment(LdapConstants.CREDENTIALS, credential);
        }
        context.reconnect(null);
      }
    } catch (NamingException e) {
      if (context != null) {
        context.close();
      }
      throw e;
    }

    return context;
  }


  /**
   * This will attempt to StartTLS with the supplied <code>LdapContext</code>.
   *
   * @param  context  <code>LdapContext</code>
   *
   * @return  <code>StartTlsResponse</code>
   *
   * @throws  NamingException  if an error occurs while requesting an extended
   * operation
   */
  protected StartTlsResponse startTls(final LdapContext context)
    throws NamingException
  {
    StartTlsResponse tls = null;
    try {
      for (int i = 0; i <= this.config.getOperationRetry(); i++) {
        try {
          tls = (StartTlsResponse) context.extendedOperation(
            new StartTlsRequest());
          if (this.config.useHostnameVerifier()) {
            if (this.logger.isTraceEnabled()) {
              this.logger.trace(
                "TLS hostnameVerifier = " + this.config.getHostnameVerifier());
            }
            tls.setHostnameVerifier(this.config.getHostnameVerifier());
          }
          if (this.config.useSslSocketFactory()) {
            if (this.logger.isTraceEnabled()) {
              this.logger.trace(
                "TLS sslSocketFactory = " + this.config.getSslSocketFactory());
            }
            tls.negotiate(this.config.getSslSocketFactory());
          } else {
            tls.negotiate();
          }
          break;
        } catch (CommunicationException e) {
          if (i == this.config.getOperationRetry()) {
            throw e;
          }
          if (this.logger.isWarnEnabled()) {
            this.logger.warn(
              "Error while communicating with the LDAP, retrying",
              e);
          }
          this.reconnect();
        }
      }
    } catch (IOException e) {
      if (this.logger.isErrorEnabled()) {
        this.logger.error("Could not negotiate TLS connection", e);
      }
      throw new CommunicationException(e.getMessage());
    }
    return tls;
  }


  /**
   * This will attempt to StopTLS with the supplied <code>
   * StartTlsResponse</code>.
   *
   * @param  tls  <code>StartTlsResponse</code>
   *
   * @throws  NamingException  if an error occurs while closing the TLS
   * connection
   */
  protected void stopTls(final StartTlsResponse tls)
    throws NamingException
  {
    if (tls != null) {
      try {
        tls.close();
      } catch (IOException e) {
        if (this.logger.isErrorEnabled()) {
          this.logger.error("Could not close TLS connection", e);
        }
        throw new CommunicationException(e.getMessage());
      }
    }
  }


  /**
   * Provides a descriptive string representation of this instance.
   *
   * @return  String of the form $Classname@hashCode::config=$config.
   */
  @Override
  public String toString()
  {
    return
      String.format(
        "%s@%d::config=%s",
        this.getClass().getName(),
        this.hashCode(),
        this.config);
  }


  /**
   * Called by the garbage collector on an object when garbage collection
   * determines that there are no more references to the object.
   *
   * @throws  Throwable  if an exception is thrown by this method
   */
  protected void finalize()
    throws Throwable
  {
    try {
      this.close();
    } finally {
      super.finalize();
    }
  }
}
/*
  $Id: AbstractLdap.java 279 2009-05-29 18:34:09Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 279 $
  Updated: $Date: 2009-05-29 11:34:09 -0700 (Fri, 29 May 2009) $
*/
package edu.vt.middleware.ldap;

import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import javax.naming.Binding;
import javax.naming.CommunicationException;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.StartTlsResponse;
import edu.vt.middleware.ldap.handler.AttributeHandler;
import edu.vt.middleware.ldap.handler.AttributesProcessor;
import edu.vt.middleware.ldap.handler.CopyResultHandler;
import edu.vt.middleware.ldap.handler.FqdnSearchResultHandler;
import edu.vt.middleware.ldap.handler.SearchCriteria;
import edu.vt.middleware.ldap.handler.SearchResultHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <code>AbstractLdap</code> contains the functions for basic interaction with a
 * LDAP. Methods are provided for connecting, binding, querying and updating.
 *
 * @param  <T>  type of LdapConfig
 *
 * @author  Middleware Services
 * @version  $Revision: 279 $ $Date: 2009-05-29 11:34:09 -0700 (Fri, 29 May 2009) $
 */
public abstract class AbstractLdap<T extends LdapConfig> implements BaseLdap
{

  /** Default copy search result handler, used if none supplied. */
  protected static final CopyResultHandler<SearchResult>
  SR_COPY_RESULT_HANDLER = new CopyResultHandler<SearchResult>();

  /** Default copy name class pair handler. */
  protected static final CopyResultHandler<NameClassPair>
  NCP_COPY_RESULT_HANDLER = new CopyResultHandler<NameClassPair>();

  /** Default copy binding handler. */
  protected static final CopyResultHandler<Binding>
  BINDING_COPY_RESULT_HANDLER = new CopyResultHandler<Binding>();

  /** Default copy result handler. */
  protected static final CopyResultHandler<Object> COPY_RESULT_HANDLER =
    new CopyResultHandler<Object>();

  /** Default search result handler, used if none configured. */
  protected static final SearchResultHandler SEARCH_RESULT_HANDLER =
    new FqdnSearchResultHandler();

  /** Log for this class. */
  protected final Log logger = LogFactory.getLog(this.getClass());

  /** LDAP interface for directory operations. */
  protected LdapContext context;

  /** TLS Session. */
  protected StartTlsResponse tlsResponse;

  /** LDAP configuration environment. */
  protected T config;


  /**
   * This will set the config parameters of this <code>Ldap</code>.
   *
   * @param  ldapConfig  <code>LdapConfig</code>
   */
  protected void setLdapConfig(final T ldapConfig)
  {
    if (this.config != null) {
      this.config.checkImmutable();
    }
    this.config = ldapConfig;
    if (this.config.isTlsEnabled()) {
      try {
        this.useTls(true);
      } catch (NamingException e) {
        if (this.logger.isErrorEnabled()) {
          this.logger.error("Error using TLS", e);
        }
      }
    }
  }


  /**
   * This will perform an LDAP compare operation with the supplied filter and
   * dn.
   *
   * @param  dn  <code>String</code> name to compare
   * @param  filter  <code>String</code> expression to use for compare
   *
   * @return  <code>boolean</code> - result of compare operation
   *
   * @throws  NamingException  if the LDAP returns an error
   */
  protected boolean compare(final String dn, final String filter)
    throws NamingException
  {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("Compare with the following parameters:");
      this.logger.debug("  dn = " + dn);
      this.logger.debug("  filter = " + filter);
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("  config = " + this.config.getEnvironment());
      }
    }

    boolean success = false;
    LdapContext ctx = null;
    NamingEnumeration<SearchResult> en = null;
    try {
      for (int i = 0; i <= this.config.getOperationRetry(); i++) {
        try {
          ctx = this.getContext();
          en = ctx.search(dn, filter, LdapConfig.getCompareSearchControls());
          break;
        } catch (CommunicationException e) {
          if (i == this.config.getOperationRetry()) {
            throw e;
          }
          if (this.logger.isWarnEnabled()) {
            this.logger.warn(
              "Error while communicating with the LDAP, retrying",
              e);
          }
          this.reconnect();
        }
      }

      if (en.hasMore()) {
        success = true;
      }
    } finally {
      if (en != null) {
        en.close();
      }
      if (ctx != null) {
        ctx.close();
      }
    }
    return success;
  }


  /**
   * This will query the LDAP with the supplied dn, filter, filter arguments,
   * and return attributes. This method will perform a search whose scope is
   * defined in the <code>LdapConfig</code>, the default scope is subtree. The
   * resulting <code>Iterator</code> is a deep copy of the original search
   * results. If filterArgs is null, then no variable substitution will occur If
   * retAttrs is null then all attributes will be returned. If retAttrs is an
   * empty array then no attributes will be returned.
   *
   * @param  dn  <code>String</code> name to begin search at
   * @param  filter  <code>String</code> expression to use for the search
   * @param  filterArgs  <code>Object[]</code> to substitute for variables in
   * the filter
   * @param  retAttrs  <code>String[]</code> attributes to return
   * @param  handler  <code>SearchResultHandler</code> to post process results
   *
   * @return  <code>Iterator</code> - of LDAP search results
   *
   * @throws  NamingException  if the LDAP returns an error
   */
  protected Iterator<SearchResult> search(
    final String dn,
    final String filter,
    final Object[] filterArgs,
    final String[] retAttrs,
    final SearchResultHandler handler)
    throws NamingException
  {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("Search with the following parameters:");
      this.logger.debug("  dn = " + dn);
      this.logger.debug("  filter = " + filter);
      this.logger.debug("  filterArgs = ");
      if (filterArgs == null) {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug("    none");
        }
      } else {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug("    " + Arrays.asList(filterArgs));
        }
      }
      this.logger.debug("  retAttrs = ");
      if (retAttrs == null) {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug("    all attributes");
        }
      } else {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug("    " + Arrays.asList(retAttrs));
        }
      }
      this.logger.debug("  handler = " + handler);
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("  config = " + this.config.getEnvironment());
      }
    }

    List<SearchResult> results = null;
    LdapContext ctx = null;
    NamingEnumeration<SearchResult> en = null;
    try {
      for (int i = 0; i <= this.config.getOperationRetry(); i++) {
        try {
          ctx = this.getContext();
          en = ctx.search(
            dn,
            filter,
            filterArgs,
            this.config.getSearchControls(retAttrs));
          break;
        } catch (CommunicationException e) {
          if (i == this.config.getOperationRetry()) {
            throw e;
          }
          if (this.logger.isWarnEnabled()) {
            this.logger.warn(
              "Error while communicating with the LDAP, retrying",
              e);
          }
          this.reconnect();
        }
      }

      final SearchCriteria sc = new SearchCriteria(dn);
      sc.setFilter(filter);
      sc.setFilterArgs(filterArgs);
      sc.setReturnAttrs(retAttrs);
      if (handler != null) {
        results = handler.process(sc, en);
      } else {
        results = SR_COPY_RESULT_HANDLER.process(sc, en);
      }
    } finally {
      if (en != null) {
        en.close();
      }
      if (ctx != null) {
        ctx.close();
      }
    }
    return results.iterator();
  }


  /**
   * This will query the LDAP for the supplied dn, matching attributes and
   * return attributes. This method will always perform a one level search. The
   * resulting <code>Iterator</code> is a deep copy of the original search
   * results. If matchAttrs is empty or null then all objects in the target
   * context are returned. If retAttrs is null then all attributes will be
   * returned. If retAttrs is an empty array then no attributes will be
   * returned.
   *
   * @param  dn  <code>String</code> name to search in
   * @param  matchAttrs  <code>Attributes</code> attributes to match
   * @param  retAttrs  <code>String[]</code> attributes to return
   * @param  handler  <code>SearchResultHandler</code> to post process results
   *
   * @return  <code>Iterator</code> - of LDAP search results
   *
   * @throws  NamingException  if the LDAP returns an error
   */
  protected Iterator<SearchResult> searchAttributes(
    final String dn,
    final Attributes matchAttrs,
    final String[] retAttrs,
    final SearchResultHandler handler)
    throws NamingException
  {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("One level search with the following parameters:");
      this.logger.debug("  dn = " + dn);
      this.logger.debug("  matchAttrs = " + matchAttrs);
      this.logger.debug("  retAttrs = ");
      if (retAttrs == null) {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug("    all attributes");
        }
      } else {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug("    " + Arrays.asList(retAttrs));
        }
      }
      this.logger.debug("  handler = " + handler);
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("  config = " + this.config.getEnvironment());
      }
    }

    List<SearchResult> results = null;
    LdapContext ctx = null;
    NamingEnumeration<SearchResult> en = null;
    try {
      for (int i = 0; i <= this.config.getOperationRetry(); i++) {
        try {
          ctx = this.getContext();
          en = ctx.search(dn, matchAttrs, retAttrs);
          break;
        } catch (CommunicationException e) {
          if (i == this.config.getOperationRetry()) {
            throw e;
          }
          if (this.logger.isWarnEnabled()) {
            this.logger.warn(
              "Error while communicating with the LDAP, retrying",
              e);
          }
          this.reconnect();
        }
      }

      final SearchCriteria sc = new SearchCriteria(dn);
      sc.setMatchAttrs(matchAttrs);
      sc.setReturnAttrs(retAttrs);
      if (handler != null) {
        results = handler.process(sc, en);
      } else {
        results = SR_COPY_RESULT_HANDLER.process(sc, en);
      }
    } finally {
      if (en != null) {
        en.close();
      }
      if (ctx != null) {
        ctx.close();
      }
    }
    return results.iterator();
  }


  /**
   * This will enumerate the names bounds to the specified context, along with
   * the class names of objects bound to them. The resulting <code>
   * Iterator</code> is a deep copy of the original search results.
   *
   * @param  dn  <code>String</code> LDAP context to list
   *
   * @return  <code>Iterator</code> - LDAP search result
   *
   * @throws  NamingException  if the LDAP returns an error
   */
  protected Iterator<NameClassPair> list(final String dn)
    throws NamingException
  {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("list with the following parameters:");
      this.logger.debug("  dn = " + dn);
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("  config = " + this.config.getEnvironment());
      }
    }

    List<NameClassPair> results = null;
    LdapContext ctx = null;
    NamingEnumeration<NameClassPair> en = null;
    try {
      for (int i = 0; i <= this.config.getOperationRetry(); i++) {
        try {
          ctx = this.getContext();
          en = ctx.list(dn);
          break;
        } catch (CommunicationException e) {
          if (i == this.config.getOperationRetry()) {
            throw e;
          }
          if (this.logger.isWarnEnabled()) {
            this.logger.warn(
              "Error while communicating with the LDAP, retrying",
              e);
          }
          this.reconnect();
        }
      }

      results = NCP_COPY_RESULT_HANDLER.process(new SearchCriteria(dn), en);
    } finally {
      if (en != null) {
        en.close();
      }
      if (ctx != null) {
        ctx.close();
      }
    }
    return results.iterator();
  }


  /**
   * This will enumerate the names bounds to the specified context, along with
   * the objects bound to them. The resulting <code>Iterator</code> is a deep
   * copy of the original search results.
   *
   * @param  dn  <code>String</code> LDAP context to list
   *
   * @return  <code>Iterator</code> - LDAP search result
   *
   * @throws  NamingException  if the LDAP returns an error
   */
  protected Iterator<Binding> listBindings(final String dn)
    throws NamingException
  {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("listBindings with the following parameters:");
      this.logger.debug("  dn = " + dn);
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("  config = " + this.config.getEnvironment());
      }
    }

    List<Binding> results = null;
    LdapContext ctx = null;
    NamingEnumeration<Binding> en = null;
    try {
      for (int i = 0; i <= this.config.getOperationRetry(); i++) {
        try {
          ctx = this.getContext();
          en = ctx.listBindings(dn);
          break;
        } catch (CommunicationException e) {
          if (i == this.config.getOperationRetry()) {
            throw e;
          }
          if (this.logger.isWarnEnabled()) {
            this.logger.warn(
              "Error while communicating with the LDAP, retrying",
              e);
          }
          this.reconnect();
        }
      }

      results = BINDING_COPY_RESULT_HANDLER.process(new SearchCriteria(dn), en);
    } finally {
      if (en != null) {
        en.close();
      }
      if (ctx != null) {
        ctx.close();
      }
    }
    return results.iterator();
  }


  /**
   * This will return the matching attributes associated with the supplied dn.
   * If retAttrs is null then all attributes will be returned. If retAttrs is an
   * empty array then no attributes will be returned.
   *
   * @param  dn  <code>String</code> named object in the LDAP
   * @param  retAttrs  <code>String[]</code> attributes to return
   * @param  handler  <code>AttributeHandler</code> to post process results
   *
   * @return  <code>Attributes</code>
   *
   * @throws  NamingException  if the LDAP returns an error
   */
  protected Attributes getAttributes(
    final String dn,
    final String[] retAttrs,
    final AttributeHandler handler)
    throws NamingException
  {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("Attribute search with the following parameters:");
      this.logger.debug("  dn = " + dn);
      this.logger.debug("  retAttrs = ");
      if (retAttrs == null) {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug("    all attributes");
        }
      } else {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug("    " + Arrays.asList(retAttrs));
        }
      }
      this.logger.debug("  handler = " + handler);
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("  config = " + this.config.getEnvironment());
      }
    }

    LdapContext ctx = null;
    Attributes attrs = null;
    try {
      for (int i = 0; i <= this.config.getOperationRetry(); i++) {
        try {
          ctx = this.getContext();
          attrs = ctx.getAttributes(dn, retAttrs);
          break;
        } catch (CommunicationException e) {
          if (i == this.config.getOperationRetry()) {
            throw e;
          }
          if (this.logger.isWarnEnabled()) {
            this.logger.warn(
              "Error while communicating with the LDAP, retrying",
              e);
          }
          this.reconnect();
        }
      }

      if (handler != null) {
        attrs = AttributesProcessor.executeHandler(
          new SearchCriteria(dn),
          attrs,
          handler);
      }
    } finally {
      if (ctx != null) {
        ctx.close();
      }
    }
    return attrs;
  }


  /**
   * This will return the LDAP schema associated with the supplied dn. The
   * resulting <code>Iterator</code> is a deep copy of the original search
   * results.
   *
   * @param  dn  <code>String</code> named object in the LDAP
   *
   * @return  <code>Iterator</code> - LDAP search result
   *
   * @throws  NamingException  if the LDAP returns an error
   */
  protected Iterator<SearchResult> getSchema(final String dn)
    throws NamingException
  {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("Schema search with the following parameters:");
      this.logger.debug("  dn = " + dn);
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("  config = " + this.config.getEnvironment());
      }
    }

    List<SearchResult> results = null;
    LdapContext ctx = null;
    DirContext schema = null;
    NamingEnumeration<SearchResult> en = null;
    try {
      for (int i = 0; i <= this.config.getOperationRetry(); i++) {
        try {
          ctx = this.getContext();
          schema = ctx.getSchema(dn);
          en = schema.search("", null);
          break;
        } catch (CommunicationException e) {
          if (i == this.config.getOperationRetry()) {
            throw e;
          }
          if (this.logger.isWarnEnabled()) {
            this.logger.warn(
              "Error while communicating with the LDAP, retrying",
              e);
          }
          this.reconnect();
        }
      }

      results = SR_COPY_RESULT_HANDLER.process(new SearchCriteria(dn), en);
    } finally {
      if (schema != null) {
        schema.close();
      }
      if (en != null) {
        en.close();
      }
      if (ctx != null) {
        ctx.close();
      }
    }
    return results.iterator();
  }


  /**
   * This will modify the supplied attributes for the supplied value given by
   * the modification operation. modOp must be one of: ADD_ATTRIBUTE,
   * REPLACE_ATTRIBUTE, REMOVE_ATTRIBUTE.
   *
   * @param  dn  <code>String</code> named object in the LDAP
   * @param  modOp  <code>int</code> modification operation
   * @param  attrs  <code>Attributes</code> attributes to be used for the
   * operation, may be null
   *
   * @throws  NamingException  if the LDAP returns an error
   */
  protected void modifyAttributes(
    final String dn,
    final int modOp,
    final Attributes attrs)
    throws NamingException
  {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("Modifiy attributes with the following parameters:");
      this.logger.debug("  dn = " + dn);
      this.logger.debug("  modOp = " + modOp);
      this.logger.debug("  attrs = " + attrs);
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("  config = " + this.config.getEnvironment());
      }
    }

    LdapContext ctx = null;
    try {
      for (int i = 0; i <= this.config.getOperationRetry(); i++) {
        try {
          ctx = this.getContext();
          ctx.modifyAttributes(dn, modOp, attrs);
          break;
        } catch (CommunicationException e) {
          if (i == this.config.getOperationRetry()) {
            throw e;
          }
          if (this.logger.isWarnEnabled()) {
            this.logger.warn(
              "Error while communicating with the LDAP, retrying",
              e);
          }
          this.reconnect();
        }
      }
    } finally {
      if (ctx != null) {
        ctx.close();
      }
    }
  }


  /**
   * This will create the supplied dn in the LDAP namespace with the supplied
   * attributes.
   *
   * @param  dn  <code>String</code> named object in the LDAP
   * @param  attrs  <code>Attributes</code> attributes to be added to this entry
   *
   * @throws  NamingException  if the LDAP returns an error
   */
  protected void create(final String dn, final Attributes attrs)
    throws NamingException
  {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("Create name with the following parameters:");
      this.logger.debug("  dn = " + dn);
      this.logger.debug("  attrs = " + attrs);
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("  config = " + this.config.getEnvironment());
      }
    }

    LdapContext ctx = null;
    try {
      for (int i = 0; i <= this.config.getOperationRetry(); i++) {
        try {
          ctx = this.getContext();
          ctx.createSubcontext(dn, attrs).close();
          break;
        } catch (CommunicationException e) {
          if (i == this.config.getOperationRetry()) {
            throw e;
          }
          if (this.logger.isWarnEnabled()) {
            this.logger.warn(
              "Error while communicating with the LDAP, retrying",
              e);
          }
          this.reconnect();
        }
      }
    } finally {
      if (ctx != null) {
        ctx.close();
      }
    }
  }


  /**
   * This will delete the supplied dn from the LDAP namespace.
   *
   * @param  dn  <code>String</code> named object in the LDAP
   *
   * @throws  NamingException  if the LDAP returns an error
   */
  protected void delete(final String dn)
    throws NamingException
  {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("Delete name with the following parameters:");
      this.logger.debug("  dn = " + dn);
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("  config = " + this.config.getEnvironment());
      }
    }

    LdapContext ctx = null;
    try {
      for (int i = 0; i <= this.config.getOperationRetry(); i++) {
        try {
          ctx = this.getContext();
          ctx.destroySubcontext(dn);
          break;
        } catch (CommunicationException e) {
          if (i == this.config.getOperationRetry()) {
            throw e;
          }
          if (this.logger.isWarnEnabled()) {
            this.logger.warn(
              "Error while communicating with the LDAP, retrying",
              e);
          }
          this.reconnect();
        }
      }
    } finally {
      if (ctx != null) {
        ctx.close();
      }
    }
  }


  /**
   * This returns whether the <code>Ldap</code> is using the TLS protocol for
   * connections.
   *
   * @return  <code>boolean</code> - whether the TLS protocol is being used
   */
  public boolean isTlsEnabled()
  {
    return this.config.isTlsEnabled();
  }


  /**
   * This sets this <code>Ldap</code> to use the TLS protocol for connections.
   * If tls is true this method will upgrade the existing connection if it has
   * already been established. If tls is false this method will downgrade the
   * existing connection if it has already been established. If no connection
   * has been established, then the next connection made will take this setting
   * into effect.
   *
   * @param  tls  <code>boolean</code> - whether LDAP connections should use the
   * TLS protocol
   *
   * @throws  NamingException  if an error occurs while requesting an extended
   * operation
   */
  public void useTls(final boolean tls)
    throws NamingException
  {
    if (this.context != null) {
      if (tls) {
        if (this.tlsResponse == null) {
          this.tlsResponse = this.startTls(this.context);
        }
      } else {
        this.stopTls(this.tlsResponse);
      }
    }
  }


  /**
   * This will establish a connection if one does not already exist by binding
   * to the LDAP using parameters given by {@link
   * LdapConfig#setServiceUser(String)} and {@link
   * LdapConfig#setServiceCredential(Object)}. If these parameters have not been
   * set then an anonymous bind will be attempted. This connection must be
   * closed using {@link #close}. Any method which requires a LDAP connection
   * will call this method independently. This method should only be used if you
   * need to verify that you can connect to the LDAP.
   *
   * @return  <code>boolean</code> - whether the connection was successfull
   *
   * @throws  NamingException  if the LDAP cannot be reached
   */
  public synchronized boolean connect()
    throws NamingException
  {
    boolean success = false;
    if (this.context != null) {
      success = true;
    } else {
      this.context = this.bind(
        this.config.getServiceUser(),
        this.config.getServiceCredential(),
        this.tlsResponse);
      success = true;
    }
    return success;
  }


  /**
   * This will close the current connection to the LDAP and establish a new
   * connection to the LDAP using {@link #connect}.
   *
   * @return  <code>boolean</code> - whether the connection was successfull
   *
   * @throws  NamingException  if the LDAP cannot be reached
   */
  public synchronized boolean reconnect()
    throws NamingException
  {
    this.close();
    return this.connect();
  }


  /** This will close the connection to the LDAP. */
  public void close()
  {
    try {
      this.stopTls(this.tlsResponse);
    } catch (NamingException e) {
      if (this.logger.isErrorEnabled()) {
        this.logger.error("Error stopping TLS", e);
      }
    } finally {
      try {
        if (this.context != null) {
          this.context.close();
          this.context = null;
        }
      } catch (NamingException e) {
        if (this.logger.isErrorEnabled()) {
          this.logger.error("Error closing connection with the LDAP", e);
        }
      }
    }
  }


  /**
   * This will return an initialized connection to the LDAP.
   *
   * @return  <code>LdapContext</code>
   *
   * @throws  NamingException  if the LDAP returns an error
   */
  protected LdapContext getContext()
    throws NamingException
  {
    this.connect();
    return this.context.newInstance(null);
  }


  /**
   * This initiates a new connection to the LDAP. If {@link
   * LdapConfig#getAuthtype()} is 'none', then anonymous auth is attempted. If
   * dn or credential is null, then anonymous auth is attempted. tls can be null
   * unless {@link #useTls(boolean)} has been set to true.
   *
   * @param  dn  <code>String</code> to attempt bind with
   * @param  credential  <code>Object</code> to attempt bind with
   * @param  tls  <code>StartTlsResponse</code> to control TLS
   *
   * @return  <code>boolean</code> - whether the connection succeeded
   *
   * @throws  NamingException  if an error occurs while creating the Context or
   * requesting an extended operation
   */
  protected LdapContext bind(
    final String dn,
    final Object credential,
    StartTlsResponse tls)
    throws NamingException
  {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("Bind with the following parameters:");
      this.logger.debug("  dn = " + dn);
      if (this.config.getLogCredentials()) {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug("  credential = " + credential);
        }
      } else {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug("  credential = <suppressed>");
        }
      }
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("  config = " + this.config.getEnvironment());
      }
    }

    final Hashtable<String, Object> environment = new Hashtable<String, Object>(
      this.config.getEnvironment());
    String authtype = this.config.getAuthtype();

    // set authtype to none if no credentials and not using SASL
    if (!this.config.isSaslAuth() && (dn == null || credential == null)) {
      if (this.logger.isTraceEnabled()) {
        this.logger.trace(
          "dn or credential is null, authtype set to " +
          LdapConstants.NONE_AUTHTYPE);
      }
      authtype = LdapConstants.NONE_AUTHTYPE;
    }

    // if using TLS, then credentials must be added after connection is made
    if (this.config.isTlsEnabled()) {
      environment.put(LdapConstants.VERSION, LdapConstants.VERSION_THREE);
    } else {
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("TLS not used");
        this.logger.trace("authtype is " + authtype);
      }
      environment.put(LdapConstants.AUTHENTICATION, authtype);
      // do not set credentials if authtype is none, sasl external, or gssapi
      if (
        !this.config.isExternalAuth() &&
          !this.config.isGSSAPIAuth() &&
          !authtype.equals(LdapConstants.NONE_AUTHTYPE)) {
        environment.put(LdapConstants.PRINCIPAL, dn);
        environment.put(LdapConstants.CREDENTIALS, credential);
      }
    }

    LdapContext context = null;
    try {
      context = new InitialLdapContext(environment, null);

      if (this.config.isTlsEnabled()) {
        if (this.logger.isTraceEnabled()) {
          this.logger.trace("TLS will be used");
          this.logger.trace("authtype is " + authtype);
        }
        tls = this.startTls(context);
        context.addToEnvironment(LdapConstants.AUTHENTICATION, authtype);
        // do not set credentials if authtype is none, sasl external, or gssapi
        if (
          !this.config.isExternalAuth() &&
            !this.config.isGSSAPIAuth() &&
            !authtype.equals(LdapConstants.NONE_AUTHTYPE)) {
          context.addToEnvironment(LdapConstants.PRINCIPAL, dn);
          context.addToEnvironment(LdapConstants.CREDENTIALS, credential);
        }
        context.reconnect(null);
      }
    } catch (NamingException e) {
      if (context != null) {
        context.close();
      }
      throw e;
    }

    return context;
  }


  /**
   * This will attempt to StartTLS with the supplied <code>LdapContext</code>.
   *
   * @param  context  <code>LdapContext</code>
   *
   * @return  <code>StartTlsResponse</code>
   *
   * @throws  NamingException  if an error occurs while requesting an extended
   * operation
   */
  protected StartTlsResponse startTls(final LdapContext context)
    throws NamingException
  {
    StartTlsResponse tls = null;
    try {
      for (int i = 0; i <= this.config.getOperationRetry(); i++) {
        try {
          tls = (StartTlsResponse) context.extendedOperation(
            new StartTlsRequest());
          if (this.config.useHostnameVerifier()) {
            if (this.logger.isTraceEnabled()) {
              this.logger.trace(
                "TLS hostnameVerifier = " + this.config.getHostnameVerifier());
            }
            tls.setHostnameVerifier(this.config.getHostnameVerifier());
          }
          if (this.config.useSslSocketFactory()) {
            if (this.logger.isTraceEnabled()) {
              this.logger.trace(
                "TLS sslSocketFactory = " + this.config.getSslSocketFactory());
            }
            tls.negotiate(this.config.getSslSocketFactory());
          } else {
            tls.negotiate();
          }
          break;
        } catch (CommunicationException e) {
          if (i == this.config.getOperationRetry()) {
            throw e;
          }
          if (this.logger.isWarnEnabled()) {
            this.logger.warn(
              "Error while communicating with the LDAP, retrying",
              e);
          }
          this.reconnect();
        }
      }
    } catch (IOException e) {
      if (this.logger.isErrorEnabled()) {
        this.logger.error("Could not negotiate TLS connection", e);
      }
      throw new CommunicationException(e.getMessage());
    }
    return tls;
  }


  /**
   * This will attempt to StopTLS with the supplied <code>
   * StartTlsResponse</code>.
   *
   * @param  tls  <code>StartTlsResponse</code>
   *
   * @throws  NamingException  if an error occurs while closing the TLS
   * connection
   */
  protected void stopTls(final StartTlsResponse tls)
    throws NamingException
  {
    if (tls != null) {
      try {
        tls.close();
      } catch (IOException e) {
        if (this.logger.isErrorEnabled()) {
          this.logger.error("Could not close TLS connection", e);
        }
        throw new CommunicationException(e.getMessage());
      }
    }
  }


  /**
   * Provides a descriptive string representation of this instance.
   *
   * @return  String of the form $Classname@hashCode::config=$config.
   */
  @Override
  public String toString()
  {
    return
      String.format(
        "%s@%d::config=%s",
        this.getClass().getName(),
        this.hashCode(),
        this.config);
  }


  /**
   * Called by the garbage collector on an object when garbage collection
   * determines that there are no more references to the object.
   *
   * @throws  Throwable  if an exception is thrown by this method
   */
  protected void finalize()
    throws Throwable
  {
    try {
      this.close();
    } finally {
      super.finalize();
    }
  }
}
