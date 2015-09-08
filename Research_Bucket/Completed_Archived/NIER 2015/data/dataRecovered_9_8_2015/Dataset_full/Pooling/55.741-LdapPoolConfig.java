/*
  $Id: LdapPoolConfig.java 183 2009-05-06 02:45:57Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 183 $
  Updated: $Date: 2009-05-05 19:45:57 -0700 (Tue, 05 May 2009) $
*/
package edu.vt.middleware.ldap.pool;

import edu.vt.middleware.ldap.props.AbstractPropertyConfig;
import edu.vt.middleware.ldap.props.LdapProperties;
import edu.vt.middleware.ldap.props.PropertyInvoker;

/**
 * <code>LdapPoolConfig</code> contains all the configuration data that the
 * pooling implementations need to control the pool.
 *
 * @author  Middleware Services
 * @version  $Revision: 183 $ $Date: 2009-05-05 19:45:57 -0700 (Tue, 05 May 2009) $
 */
public class LdapPoolConfig extends AbstractPropertyConfig
{

  /** Domain to look for ldap properties in, value is {@value}. */
  public static final String PROPERTIES_DOMAIN = "edu.vt.middleware.ldap.pool.";

  /** Default min pool size, value is {@value}. */
  public static final int DEFAULT_MIN_POOL_SIZE = 3;

  /** Default max pool size, value is {@value}. */
  public static final int DEFAULT_MAX_POOL_SIZE = 10;

  /** Default validate on check in, value is {@value}. */
  public static final boolean DEFAULT_VALIDATE_ON_CHECKIN = false;

  /** Default validate on check out, value is {@value}. */
  public static final boolean DEFAULT_VALIDATE_ON_CHECKOUT = false;

  /** Default validate periodically, value is {@value}. */
  public static final boolean DEFAULT_VALIDATE_PERIODICALLY = false;

  /** Default validate timer period, value is {@value}. */
  public static final long DEFAULT_VALIDATE_TIMER_PERIOD = 1800000;

  /** Default prune timer period, value is {@value}. */
  public static final long DEFAULT_PRUNE_TIMER_PERIOD = 300000;

  /** Default expiration time, value is {@value}. */
  public static final long DEFAULT_EXPIRATION_TIME = 600000;

  /** Invoker for ldap properties. */
  private static final PropertyInvoker PROPERTIES = new PropertyInvoker(
    LdapPoolConfig.class,
    PROPERTIES_DOMAIN);

  /** Min pool size. */
  private int minPoolSize = DEFAULT_MIN_POOL_SIZE;

  /** Max pool size. */
  private int maxPoolSize = DEFAULT_MAX_POOL_SIZE;

  /** Whether the ldap object should be validated when returned to the pool. */
  private boolean validateOnCheckIn = DEFAULT_VALIDATE_ON_CHECKIN;

  /** Whether the ldap object should be validated when given from the pool. */
  private boolean validateOnCheckOut = DEFAULT_VALIDATE_ON_CHECKOUT;

  /** Whether the pool should be validated periodically. */
  private boolean validatePeriodically = DEFAULT_VALIDATE_PERIODICALLY;

  /** Time in milliseconds that the validate pool timer should repeat. */
  private long validateTimerPeriod = DEFAULT_VALIDATE_TIMER_PERIOD;

  /** Time in milliseconds that the prune pool timer should repeat. */
  private long pruneTimerPeriod = DEFAULT_PRUNE_TIMER_PERIOD;

  /** Time in milliseconds that ldap objects should be considered expired. */
  private long expirationTime = DEFAULT_EXPIRATION_TIME;


  /** Default constructor. */
  public LdapPoolConfig() {}


  /**
   * This returns the min pool size for the <code>LdapPoolConfig</code>. Default
   * value is {@link #DEFAULT_MIN_POOL_SIZE}. This value represents the size of
   * the pool after the prune timer has run.
   *
   * @return  <code>int</code> - min pool size
   */
  public int getMinPoolSize()
  {
    return this.minPoolSize;
  }


  /**
   * This returns the max pool size for the <code>LdapPoolConfig</code>. Default
   * value is {@link #DEFAULT_MAX_POOL_SIZE}. This value may or may not be
   * strictly enforced depending on the pooling implementation.
   *
   * @return  <code>int</code> - max pool size
   */
  public int getMaxPoolSize()
  {
    return this.maxPoolSize;
  }


  /**
   * This returns the validate on check in flag for the <code>
   * LdapPoolConfig</code>. Default value is {@link
   * #DEFAULT_VALIDATE_ON_CHECKIN}.
   *
   * @return  <code>boolean</code> - validate on check in
   */
  public boolean isValidateOnCheckIn()
  {
    return this.validateOnCheckIn;
  }


  /**
   * This returns the validate on check out flag for the <code>
   * LdapPoolConfig</code>. Default value is {@link
   * #DEFAULT_VALIDATE_ON_CHECKOUT}.
   *
   * @return  <code>boolean</code> - validate on check in
   */
  public boolean isValidateOnCheckOut()
  {
    return this.validateOnCheckOut;
  }


  /**
   * This returns the validate periodically flag for the <code>
   * LdapPoolConfig</code>. Default value is {@link
   * #DEFAULT_VALIDATE_PERIODICALLY}.
   *
   * @return  <code>boolean</code> - validate periodically
   */
  public boolean isValidatePeriodically()
  {
    return this.validatePeriodically;
  }


  /**
   * This returns the prune timer period for the <code>LdapPoolConfig</code>.
   * Default value is {@link #DEFAULT_PRUNE_TIMER_PERIOD}. The prune timer
   * attempts to execute {@link LdapPool#prune()}.
   *
   * @return  <code>long</code> - prune timer period in milliseconds
   */
  public long getPruneTimerPeriod()
  {
    return this.pruneTimerPeriod;
  }


  /**
   * This returns the validate timer period for the <code>LdapPoolConfig</code>.
   * Default value is {@link #DEFAULT_VALIDATE_TIMER_PERIOD}. The validate timer
   * attempts to execute {@link LdapPool#validate()}.
   *
   * @return  <code>long</code> - validate timer period in milliseconds
   */
  public long getValidateTimerPeriod()
  {
    return this.validateTimerPeriod;
  }


  /**
   * This returns the expiration time for the <code>LdapPoolConfig</code>.
   * Default value is {@link #DEFAULT_EXPIRATION_TIME}. The expiration time
   * represents the max time a ldap object should be available before it is
   * considered stale. This value does not apply to objects in the pool if the
   * pool has only a minimum number of objects available.
   *
   * @return  <code>long</code> - expiration time in milliseconds
   */
  public long getExpirationTime()
  {
    return this.expirationTime;
  }


  /**
   * This sets the min pool size for the <code>LdapPoolConfig</code>.
   *
   * @param  size  <code>int</code>
   */
  public void setMinPoolSize(final int size)
  {
    checkImmutable();
    if (size >= 0) {
      this.minPoolSize = size;
    }
  }


  /**
   * This sets the max pool size for the <code>LdapPoolConfig</code>.
   *
   * @param  size  <code>int</code>
   */
  public void setMaxPoolSize(final int size)
  {
    checkImmutable();
    if (size >= 0) {
      this.maxPoolSize = size;
    }
  }


  /**
   * This sets the validate on check in flag for the <code>
   * LdapPoolConfig</code>.
   *
   * @param  b  <code>boolean</code>
   */
  public void setValidateOnCheckIn(final boolean b)
  {
    checkImmutable();
    this.validateOnCheckIn = b;
  }


  /**
   * This sets the validate on check out flag for the <code>
   * LdapPoolConfig</code>.
   *
   * @param  b  <code>boolean</code>
   */
  public void setValidateOnCheckOut(final boolean b)
  {
    checkImmutable();
    this.validateOnCheckOut = b;
  }


  /**
   * This sets the validate periodically flag for the <code>
   * LdapPoolConfig</code>.
   *
   * @param  b  <code>boolean</code>
   */
  public void setValidatePeriodically(final boolean b)
  {
    checkImmutable();
    this.validatePeriodically = b;
  }


  /**
   * Sets the period for which the prune pool timer will run.
   *
   * @param  time  in milliseconds
   */
  public void setPruneTimerPeriod(final long time)
  {
    checkImmutable();
    if (time >= 0) {
      this.pruneTimerPeriod = time;
    }
  }


  /**
   * Sets the period for which the validate pool timer will run.
   *
   * @param  time  in milliseconds
   */
  public void setValidateTimerPeriod(final long time)
  {
    checkImmutable();
    if (time >= 0) {
      this.validateTimerPeriod = time;
    }
  }


  /**
   * Sets the time that an ldap object should be considered stale and ready for
   * removal from the pool.
   *
   * @param  time  in milliseconds
   */
  public void setExpirationTime(final long time)
  {
    checkImmutable();
    if (time >= 0) {
      this.expirationTime = time;
    }
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
   * @return  <code>LdapPoolConfig</code> initialized ldap pool config
   */
  public static LdapPoolConfig createFromProperties(final String propertiesFile)
  {
    final LdapPoolConfig poolConfig = new LdapPoolConfig();
    LdapProperties properties = null;
    if (propertiesFile != null) {
      properties = new LdapProperties(poolConfig, propertiesFile);
    } else {
      properties = new LdapProperties(poolConfig);
    }
    properties.configure();
    return poolConfig;
  }
}
/*
  $Id: LdapPoolConfig.java 493 2009-08-28 02:21:35Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 493 $
  Updated: $Date: 2009-08-27 19:21:35 -0700 (Thu, 27 Aug 2009) $
*/
package edu.vt.middleware.ldap.pool;

import java.io.InputStream;
import edu.vt.middleware.ldap.props.AbstractPropertyConfig;
import edu.vt.middleware.ldap.props.LdapProperties;
import edu.vt.middleware.ldap.props.PropertyInvoker;

/**
 * <code>LdapPoolConfig</code> contains all the configuration data that the
 * pooling implementations need to control the pool.
 *
 * @author  Middleware Services
 * @version  $Revision: 493 $ $Date: 2009-08-27 19:21:35 -0700 (Thu, 27 Aug 2009) $
 */
public class LdapPoolConfig extends AbstractPropertyConfig
{

  /** Domain to look for ldap properties in, value is {@value}. */
  public static final String PROPERTIES_DOMAIN = "edu.vt.middleware.ldap.pool.";

  /** Default min pool size, value is {@value}. */
  public static final int DEFAULT_MIN_POOL_SIZE = 3;

  /** Default max pool size, value is {@value}. */
  public static final int DEFAULT_MAX_POOL_SIZE = 10;

  /** Default validate on check in, value is {@value}. */
  public static final boolean DEFAULT_VALIDATE_ON_CHECKIN = false;

  /** Default validate on check out, value is {@value}. */
  public static final boolean DEFAULT_VALIDATE_ON_CHECKOUT = false;

  /** Default validate periodically, value is {@value}. */
  public static final boolean DEFAULT_VALIDATE_PERIODICALLY = false;

  /** Default validate timer period, value is {@value}. */
  public static final long DEFAULT_VALIDATE_TIMER_PERIOD = 1800000;

  /** Default prune timer period, value is {@value}. */
  public static final long DEFAULT_PRUNE_TIMER_PERIOD = 300000;

  /** Default expiration time, value is {@value}. */
  public static final long DEFAULT_EXPIRATION_TIME = 600000;

  /** Invoker for ldap properties. */
  private static final PropertyInvoker PROPERTIES = new PropertyInvoker(
    LdapPoolConfig.class,
    PROPERTIES_DOMAIN);

  /** Min pool size. */
  private int minPoolSize = DEFAULT_MIN_POOL_SIZE;

  /** Max pool size. */
  private int maxPoolSize = DEFAULT_MAX_POOL_SIZE;

  /** Whether the ldap object should be validated when returned to the pool. */
  private boolean validateOnCheckIn = DEFAULT_VALIDATE_ON_CHECKIN;

  /** Whether the ldap object should be validated when given from the pool. */
  private boolean validateOnCheckOut = DEFAULT_VALIDATE_ON_CHECKOUT;

  /** Whether the pool should be validated periodically. */
  private boolean validatePeriodically = DEFAULT_VALIDATE_PERIODICALLY;

  /** Time in milliseconds that the validate pool timer should repeat. */
  private long validateTimerPeriod = DEFAULT_VALIDATE_TIMER_PERIOD;

  /** Time in milliseconds that the prune pool timer should repeat. */
  private long pruneTimerPeriod = DEFAULT_PRUNE_TIMER_PERIOD;

  /** Time in milliseconds that ldap objects should be considered expired. */
  private long expirationTime = DEFAULT_EXPIRATION_TIME;


  /** Default constructor. */
  public LdapPoolConfig() {}


  /**
   * This returns the min pool size for the <code>LdapPoolConfig</code>. Default
   * value is {@link #DEFAULT_MIN_POOL_SIZE}. This value represents the size of
   * the pool after the prune timer has run.
   *
   * @return  <code>int</code> - min pool size
   */
  public int getMinPoolSize()
  {
    return this.minPoolSize;
  }


  /**
   * This returns the max pool size for the <code>LdapPoolConfig</code>. Default
   * value is {@link #DEFAULT_MAX_POOL_SIZE}. This value may or may not be
   * strictly enforced depending on the pooling implementation.
   *
   * @return  <code>int</code> - max pool size
   */
  public int getMaxPoolSize()
  {
    return this.maxPoolSize;
  }


  /**
   * This returns the validate on check in flag for the <code>
   * LdapPoolConfig</code>. Default value is {@link
   * #DEFAULT_VALIDATE_ON_CHECKIN}.
   *
   * @return  <code>boolean</code> - validate on check in
   */
  public boolean isValidateOnCheckIn()
  {
    return this.validateOnCheckIn;
  }


  /**
   * This returns the validate on check out flag for the <code>
   * LdapPoolConfig</code>. Default value is {@link
   * #DEFAULT_VALIDATE_ON_CHECKOUT}.
   *
   * @return  <code>boolean</code> - validate on check in
   */
  public boolean isValidateOnCheckOut()
  {
    return this.validateOnCheckOut;
  }


  /**
   * This returns the validate periodically flag for the <code>
   * LdapPoolConfig</code>. Default value is {@link
   * #DEFAULT_VALIDATE_PERIODICALLY}.
   *
   * @return  <code>boolean</code> - validate periodically
   */
  public boolean isValidatePeriodically()
  {
    return this.validatePeriodically;
  }


  /**
   * This returns the prune timer period for the <code>LdapPoolConfig</code>.
   * Default value is {@link #DEFAULT_PRUNE_TIMER_PERIOD}. The prune timer
   * attempts to execute {@link LdapPool#prune()}.
   *
   * @return  <code>long</code> - prune timer period in milliseconds
   */
  public long getPruneTimerPeriod()
  {
    return this.pruneTimerPeriod;
  }


  /**
   * This returns the validate timer period for the <code>LdapPoolConfig</code>.
   * Default value is {@link #DEFAULT_VALIDATE_TIMER_PERIOD}. The validate timer
   * attempts to execute {@link LdapPool#validate()}.
   *
   * @return  <code>long</code> - validate timer period in milliseconds
   */
  public long getValidateTimerPeriod()
  {
    return this.validateTimerPeriod;
  }


  /**
   * This returns the expiration time for the <code>LdapPoolConfig</code>.
   * Default value is {@link #DEFAULT_EXPIRATION_TIME}. The expiration time
   * represents the max time a ldap object should be available before it is
   * considered stale. This value does not apply to objects in the pool if the
   * pool has only a minimum number of objects available.
   *
   * @return  <code>long</code> - expiration time in milliseconds
   */
  public long getExpirationTime()
  {
    return this.expirationTime;
  }


  /**
   * This sets the min pool size for the <code>LdapPoolConfig</code>.
   *
   * @param  size  <code>int</code>
   */
  public void setMinPoolSize(final int size)
  {
    checkImmutable();
    if (size >= 0) {
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("setting minPoolSize: " + size);
      }
      this.minPoolSize = size;
    }
  }


  /**
   * This sets the max pool size for the <code>LdapPoolConfig</code>.
   *
   * @param  size  <code>int</code>
   */
  public void setMaxPoolSize(final int size)
  {
    checkImmutable();
    if (size >= 0) {
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("setting maxPoolSize: " + size);
      }
      this.maxPoolSize = size;
    }
  }


  /**
   * This sets the validate on check in flag for the <code>
   * LdapPoolConfig</code>.
   *
   * @param  b  <code>boolean</code>
   */
  public void setValidateOnCheckIn(final boolean b)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting validateOnCheckIn: " + b);
    }
    this.validateOnCheckIn = b;
  }


  /**
   * This sets the validate on check out flag for the <code>
   * LdapPoolConfig</code>.
   *
   * @param  b  <code>boolean</code>
   */
  public void setValidateOnCheckOut(final boolean b)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting validateOnCheckOut: " + b);
    }
    this.validateOnCheckOut = b;
  }


  /**
   * This sets the validate periodically flag for the <code>
   * LdapPoolConfig</code>.
   *
   * @param  b  <code>boolean</code>
   */
  public void setValidatePeriodically(final boolean b)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting validatePeriodically: " + b);
    }
    this.validatePeriodically = b;
  }


  /**
   * Sets the period for which the prune pool timer will run.
   *
   * @param  time  in milliseconds
   */
  public void setPruneTimerPeriod(final long time)
  {
    checkImmutable();
    if (time >= 0) {
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("setting pruneTimerPeriod: " + time);
      }
      this.pruneTimerPeriod = time;
    }
  }


  /**
   * Sets the period for which the validate pool timer will run.
   *
   * @param  time  in milliseconds
   */
  public void setValidateTimerPeriod(final long time)
  {
    checkImmutable();
    if (time >= 0) {
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("setting validateTimerPeriod: " + time);
      }
      this.validateTimerPeriod = time;
    }
  }


  /**
   * Sets the time that an ldap object should be considered stale and ready for
   * removal from the pool.
   *
   * @param  time  in milliseconds
   */
  public void setExpirationTime(final long time)
  {
    checkImmutable();
    if (time >= 0) {
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("setting expirationTime: " + time);
      }
      this.expirationTime = time;
    }
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
   * @return  <code>LdapPoolConfig</code> initialized ldap pool config
   */
  public static LdapPoolConfig createFromProperties(final InputStream is)
  {
    final LdapPoolConfig poolConfig = new LdapPoolConfig();
    LdapProperties properties = null;
    if (is != null) {
      properties = new LdapProperties(poolConfig, is);
    } else {
      properties = new LdapProperties(poolConfig);
      properties.useDefaultPropertiesFile();
    }
    properties.configure();
    return poolConfig;
  }
}
/*
  $Id: LdapPoolConfig.java 494 2009-08-28 02:31:50Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 494 $
  Updated: $Date: 2009-08-27 19:31:50 -0700 (Thu, 27 Aug 2009) $
*/
package edu.vt.middleware.ldap.pool;

import java.io.InputStream;
import edu.vt.middleware.ldap.props.AbstractPropertyConfig;
import edu.vt.middleware.ldap.props.LdapProperties;
import edu.vt.middleware.ldap.props.PropertyInvoker;

/**
 * <code>LdapPoolConfig</code> contains all the configuration data that the
 * pooling implementations need to control the pool.
 *
 * @author  Middleware Services
 * @version  $Revision: 494 $ $Date: 2009-08-27 19:31:50 -0700 (Thu, 27 Aug 2009) $
 */
public class LdapPoolConfig extends AbstractPropertyConfig
{

  /** Domain to look for ldap properties in, value is {@value}. */
  public static final String PROPERTIES_DOMAIN = "edu.vt.middleware.ldap.pool.";

  /** Default min pool size, value is {@value}. */
  public static final int DEFAULT_MIN_POOL_SIZE = 3;

  /** Default max pool size, value is {@value}. */
  public static final int DEFAULT_MAX_POOL_SIZE = 10;

  /** Default validate on check in, value is {@value}. */
  public static final boolean DEFAULT_VALIDATE_ON_CHECKIN = false;

  /** Default validate on check out, value is {@value}. */
  public static final boolean DEFAULT_VALIDATE_ON_CHECKOUT = false;

  /** Default validate periodically, value is {@value}. */
  public static final boolean DEFAULT_VALIDATE_PERIODICALLY = false;

  /** Default validate timer period, value is {@value}. */
  public static final long DEFAULT_VALIDATE_TIMER_PERIOD = 1800000;

  /** Default prune timer period, value is {@value}. */
  public static final long DEFAULT_PRUNE_TIMER_PERIOD = 300000;

  /** Default expiration time, value is {@value}. */
  public static final long DEFAULT_EXPIRATION_TIME = 600000;

  /** Invoker for ldap properties. */
  private static final PropertyInvoker PROPERTIES = new PropertyInvoker(
    LdapPoolConfig.class,
    PROPERTIES_DOMAIN);

  /** Min pool size. */
  private int minPoolSize = DEFAULT_MIN_POOL_SIZE;

  /** Max pool size. */
  private int maxPoolSize = DEFAULT_MAX_POOL_SIZE;

  /** Whether the ldap object should be validated when returned to the pool. */
  private boolean validateOnCheckIn = DEFAULT_VALIDATE_ON_CHECKIN;

  /** Whether the ldap object should be validated when given from the pool. */
  private boolean validateOnCheckOut = DEFAULT_VALIDATE_ON_CHECKOUT;

  /** Whether the pool should be validated periodically. */
  private boolean validatePeriodically = DEFAULT_VALIDATE_PERIODICALLY;

  /** Time in milliseconds that the validate pool timer should repeat. */
  private long validateTimerPeriod = DEFAULT_VALIDATE_TIMER_PERIOD;

  /** Time in milliseconds that the prune pool timer should repeat. */
  private long pruneTimerPeriod = DEFAULT_PRUNE_TIMER_PERIOD;

  /** Time in milliseconds that ldap objects should be considered expired. */
  private long expirationTime = DEFAULT_EXPIRATION_TIME;


  /** Default constructor. */
  public LdapPoolConfig() {}


  /**
   * This returns the min pool size for the <code>LdapPoolConfig</code>. Default
   * value is {@link #DEFAULT_MIN_POOL_SIZE}. This value represents the size of
   * the pool after the prune timer has run.
   *
   * @return  <code>int</code> - min pool size
   */
  public int getMinPoolSize()
  {
    return this.minPoolSize;
  }


  /**
   * This returns the max pool size for the <code>LdapPoolConfig</code>. Default
   * value is {@link #DEFAULT_MAX_POOL_SIZE}. This value may or may not be
   * strictly enforced depending on the pooling implementation.
   *
   * @return  <code>int</code> - max pool size
   */
  public int getMaxPoolSize()
  {
    return this.maxPoolSize;
  }


  /**
   * This returns the validate on check in flag for the <code>
   * LdapPoolConfig</code>. Default value is {@link
   * #DEFAULT_VALIDATE_ON_CHECKIN}.
   *
   * @return  <code>boolean</code> - validate on check in
   */
  public boolean isValidateOnCheckIn()
  {
    return this.validateOnCheckIn;
  }


  /**
   * This returns the validate on check out flag for the <code>
   * LdapPoolConfig</code>. Default value is {@link
   * #DEFAULT_VALIDATE_ON_CHECKOUT}.
   *
   * @return  <code>boolean</code> - validate on check in
   */
  public boolean isValidateOnCheckOut()
  {
    return this.validateOnCheckOut;
  }


  /**
   * This returns the validate periodically flag for the <code>
   * LdapPoolConfig</code>. Default value is {@link
   * #DEFAULT_VALIDATE_PERIODICALLY}.
   *
   * @return  <code>boolean</code> - validate periodically
   */
  public boolean isValidatePeriodically()
  {
    return this.validatePeriodically;
  }


  /**
   * This returns the prune timer period for the <code>LdapPoolConfig</code>.
   * Default value is {@link #DEFAULT_PRUNE_TIMER_PERIOD}. The prune timer
   * attempts to execute {@link LdapPool#prune()}.
   *
   * @return  <code>long</code> - prune timer period in milliseconds
   */
  public long getPruneTimerPeriod()
  {
    return this.pruneTimerPeriod;
  }


  /**
   * This returns the validate timer period for the <code>LdapPoolConfig</code>.
   * Default value is {@link #DEFAULT_VALIDATE_TIMER_PERIOD}. The validate timer
   * attempts to execute {@link LdapPool#validate()}.
   *
   * @return  <code>long</code> - validate timer period in milliseconds
   */
  public long getValidateTimerPeriod()
  {
    return this.validateTimerPeriod;
  }


  /**
   * This returns the expiration time for the <code>LdapPoolConfig</code>.
   * Default value is {@link #DEFAULT_EXPIRATION_TIME}. The expiration time
   * represents the max time a ldap object should be available before it is
   * considered stale. This value does not apply to objects in the pool if the
   * pool has only a minimum number of objects available.
   *
   * @return  <code>long</code> - expiration time in milliseconds
   */
  public long getExpirationTime()
  {
    return this.expirationTime;
  }


  /**
   * This sets the min pool size for the <code>LdapPoolConfig</code>.
   *
   * @param  size  <code>int</code>
   */
  public void setMinPoolSize(final int size)
  {
    checkImmutable();
    if (size >= 0) {
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("setting minPoolSize: " + size);
      }
      this.minPoolSize = size;
    }
  }


  /**
   * This sets the max pool size for the <code>LdapPoolConfig</code>.
   *
   * @param  size  <code>int</code>
   */
  public void setMaxPoolSize(final int size)
  {
    checkImmutable();
    if (size >= 0) {
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("setting maxPoolSize: " + size);
      }
      this.maxPoolSize = size;
    }
  }


  /**
   * This sets the validate on check in flag for the <code>
   * LdapPoolConfig</code>.
   *
   * @param  b  <code>boolean</code>
   */
  public void setValidateOnCheckIn(final boolean b)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting validateOnCheckIn: " + b);
    }
    this.validateOnCheckIn = b;
  }


  /**
   * This sets the validate on check out flag for the <code>
   * LdapPoolConfig</code>.
   *
   * @param  b  <code>boolean</code>
   */
  public void setValidateOnCheckOut(final boolean b)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting validateOnCheckOut: " + b);
    }
    this.validateOnCheckOut = b;
  }


  /**
   * This sets the validate periodically flag for the <code>
   * LdapPoolConfig</code>.
   *
   * @param  b  <code>boolean</code>
   */
  public void setValidatePeriodically(final boolean b)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting validatePeriodically: " + b);
    }
    this.validatePeriodically = b;
  }


  /**
   * Sets the period for which the prune pool timer will run.
   *
   * @param  time  in milliseconds
   */
  public void setPruneTimerPeriod(final long time)
  {
    checkImmutable();
    if (time >= 0) {
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("setting pruneTimerPeriod: " + time);
      }
      this.pruneTimerPeriod = time;
    }
  }


  /**
   * Sets the period for which the validate pool timer will run.
   *
   * @param  time  in milliseconds
   */
  public void setValidateTimerPeriod(final long time)
  {
    checkImmutable();
    if (time >= 0) {
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("setting validateTimerPeriod: " + time);
      }
      this.validateTimerPeriod = time;
    }
  }


  /**
   * Sets the time that an ldap object should be considered stale and ready for
   * removal from the pool.
   *
   * @param  time  in milliseconds
   */
  public void setExpirationTime(final long time)
  {
    checkImmutable();
    if (time >= 0) {
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("setting expirationTime: " + time);
      }
      this.expirationTime = time;
    }
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
   * @return  <code>LdapPoolConfig</code> initialized ldap pool config
   */
  public static LdapPoolConfig createFromProperties(final InputStream is)
  {
    final LdapPoolConfig poolConfig = new LdapPoolConfig();
    LdapProperties properties = null;
    if (is != null) {
      properties = new LdapProperties(poolConfig, is);
    } else {
      properties = new LdapProperties(poolConfig);
      properties.useDefaultPropertiesFile();
    }
    properties.configure();
    return poolConfig;
  }
}
