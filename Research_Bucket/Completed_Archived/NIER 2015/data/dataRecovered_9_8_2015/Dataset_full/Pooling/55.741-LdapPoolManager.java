/*
  $Id: LdapPoolManager.java 537 2009-09-02 20:02:12Z marvin.addison $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 537 $
  Updated: $Date: 2009-09-02 13:02:12 -0700 (Wed, 02 Sep 2009) $
*/
package edu.vt.middleware.ldap.search;

import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLSocketFactory;

import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.LdapConfig;
import edu.vt.middleware.ldap.pool.BlockingLdapPool;
import edu.vt.middleware.ldap.pool.DefaultLdapFactory;
import edu.vt.middleware.ldap.pool.LdapPool;
import edu.vt.middleware.ldap.pool.LdapPoolConfig;
import edu.vt.middleware.ldap.pool.SharedLdapPool;
import edu.vt.middleware.ldap.pool.SoftLimitLdapPool;

/**
 * <code>LdapPoolManager</code> provides a management interface to a collection
 * of <code>LdapPool</code> objects.
 *
 * @author  Middleware Services
 * @version  $Revision: 537 $ $Date: 2009-09-02 13:02:12 -0700 (Wed, 02 Sep 2009) $
 */

public class LdapPoolManager
{

  /** default name for non sasl authorization pools. */
  private static final String DEFAULT_KEY = "";

  /** default ldap properties file. */
  private static final String DEFAULT_LDAP_PROPERTIES =
    "/peoplesearch.ldap.properties";

  /** Enum to define the type pools to be managed. */
  public enum PoolType {

    /** blocking pool. */
    BLOCKING,

    /** soft limit pool. */
    SOFT_LIMIT,

    /** shared pool. */
    SHARED;
  }

  /** Type of ldap pool to manage. */
  private PoolType poolType = PoolType.SOFT_LIMIT;

  /** Ldap configuration. */
  private String ldapProperties = DEFAULT_LDAP_PROPERTIES;

  /** Ldap pool configuration. */
  private String ldapPoolProperties;
 
  /** SSL socket factory to use for SSL/TLS connections */
  private SSLSocketFactory sslSocketFactory;

  /** Map of name to ldap pools. */
  private Map<String, LdapPool<Ldap>> ldapPools =
    new HashMap<String, LdapPool<Ldap>>();


  /**
   * Sets the type of pool to manage.
   *
   * @param  type  of ldap pool
   */
  public void setPoolType(final PoolType type)
  {
    this.poolType = type;
  }


  /**
   * Returns the pool type for this pool manager.
   *
   * @return  <code>PoolType</code>
   */
  public PoolType getPoolType()
  {
    return this.poolType;
  }


  /**
   * Sets the ldap properties location.
   *
   * @param  properties  for ldap configuration
   */
  public void setLdapProperties(final String properties)
  {
    this.ldapProperties = properties;
  }


  /**
   * Returns the ldap properties location.
   *
   * @return  <code>String</code>
   */
  public String getLdapProperties()
  {
    return this.ldapProperties;
  }


  /**
   * Sets the ldap pool properties location.
   *
   * @param  properties  for ldap pool configuration
   */
  public void setLdapPoolProperties(final String properties)
  {
    this.ldapPoolProperties = properties;
  }


  /**
   * Returns the ldap pool properties location.
   *
   * @return  <code>String</code>
   */
  public String getLdapPoolProperties()
  {
    return this.ldapPoolProperties;
  }


  /**
   * @return  SSL socket factory instance used for SSL/TLS connections.
   */
  public SSLSocketFactory getSslSocketFactory()
  {
    return sslSocketFactory;
  }


  /**
   * @param  factory  SSL socket factory instance used for SSL/TLS connections.
   */
  public void setSslSocketFactory(final SSLSocketFactory factory)
  {
    this.sslSocketFactory = factory;
  }


  /**
   * Returns a ldap pool.
   *
   * @return  <code>LdapPool</code>
   */
  public LdapPool<Ldap> getLdapPool()
  {
    return this.getLdapPool(DEFAULT_KEY);
  }


  /**
   * Returns a ldap pool with the supplied name.
   *
   * @param  name  of ldap pool
   *
   * @return  <code>LdapPool</code>
   */
  public LdapPool<Ldap> getLdapPool(final String name)
  {
    LdapPool<Ldap> pool = null;
    synchronized (this.ldapPools) {
      pool = this.ldapPools.get(name);
      if (pool == null) {
        pool = this.addLdapPool(
          name,
          this.ldapProperties,
          this.ldapPoolProperties);
      }
    }
    return pool;
  }


  /** Close all underlying ldap connections. */
  public synchronized void close()
  {
    for (Map.Entry<String, LdapPool<Ldap>> entry : this.ldapPools.entrySet()) {
      entry.getValue().close();
    }
  }


  /**
   * Creates a new ldap pool with the supplied properties.
   *
   * @param  name  of ldap pool
   * @param  ldapProps  file containing ldap configuration
   * @param  ldapPoolProps  file containing ldap pool configuration
   *
   * @return  <code>LdapPool</code>
   */
  private LdapPool<Ldap> addLdapPool(
    final String name,
    final String ldapProps,
    final String ldapPoolProps)
  {
    final LdapConfig lc = LdapConfig.createFromProperties(
      LdapPoolManager.class.getResourceAsStream(ldapProps));
    if (!DEFAULT_KEY.equals(name)) {
      lc.setSaslAuthorizationId(name);
    }
    if (this.sslSocketFactory != null) {
      lc.setSslSocketFactory(this.sslSocketFactory);
    }

    LdapPoolConfig lpc = null;
    if (ldapPoolProps != null) {
      lpc = LdapPoolConfig.createFromProperties(
        LdapPoolManager.class.getResourceAsStream(ldapPoolProps));
    } else {
      lpc = new LdapPoolConfig();
    }

    final LdapPool<Ldap> ldapPool = this.createLdapPool(this.poolType, lc, lpc);
    this.ldapPools.put(name, ldapPool);
    return ldapPool;
  }


  /**
   * Creates a new ldap pool with the supplied type and configuration data.
   *
   * @param  type  of ldap pool
   * @param  lc  ldap configuration
   * @param  lpc  ldap pool configuration
   *
   * @return  <code>LdapPool</code>
   */
  private LdapPool<Ldap> createLdapPool(
    final PoolType type,
    final LdapConfig lc,
    final LdapPoolConfig lpc)
  {
    LdapPool<Ldap> pool = null;
    if (PoolType.BLOCKING == type) {
      pool = new BlockingLdapPool(lpc, new DefaultLdapFactory(lc));
    } else if (PoolType.SOFT_LIMIT == type) {
      pool = new SoftLimitLdapPool(lpc, new DefaultLdapFactory(lc));
    } else if (PoolType.SHARED == type) {
      pool = new SharedLdapPool(lpc, new DefaultLdapFactory(lc));
    } else {
      throw new IllegalArgumentException("Unknown pool type: " + poolType);
    }
    pool.initialize();
    return pool;
  }
}
