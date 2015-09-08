/*
  $Id: AbstractLdapPool.java 370 2009-07-27 21:54:31Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 370 $
  Updated: $Date: 2009-07-27 14:54:31 -0700 (Mon, 27 Jul 2009) $
*/
package edu.vt.middleware.ldap.pool;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import edu.vt.middleware.ldap.BaseLdap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <code>AbstractLdapPool</code> contains the basic implementation for pooling
 * ldap objects. The main design objective for the supplied pooling
 * implementations is to provide a pool that does not block on object creation
 * or destruction. This is what accounts for the multiple locks available on
 * this class. The pool is backed by two queues, one for available objects and
 * one for active objects. Objects that are available for {@link #checkOut()}
 * exist in the available queue. Objects that are actively in use exist in the
 * active queue. Note that depending on the implementation an object can exist
 * in both queues at the same time.
 *
 * @param  <T>  type of ldap object
 *
 * @author  Middleware Services
 * @version  $Revision: 370 $ $Date: 2009-07-27 14:54:31 -0700 (Mon, 27 Jul 2009) $
 */
public abstract class AbstractLdapPool<T extends BaseLdap>
  implements LdapPool<T>
{

  /** Lock for the entire pool. */
  protected final ReentrantLock poolLock = new ReentrantLock();

  /** Condition for notifying threads that an object was returned. */
  protected final Condition poolNotEmpty = poolLock.newCondition();

  /** Lock for check ins. */
  protected final ReentrantLock checkInLock = new ReentrantLock();

  /** Lock for check outs. */
  protected final ReentrantLock checkOutLock = new ReentrantLock();

  /** Log for this class. */
  protected final Log logger = LogFactory.getLog(this.getClass());

  /** List of available ldap objects in the pool. */
  protected Queue<PooledLdap<T>> available = new LinkedList<PooledLdap<T>>();

  /** List of ldap objects in use. */
  protected Queue<PooledLdap<T>> active = new LinkedList<PooledLdap<T>>();

  /** Ldap pool config. */
  protected LdapPoolConfig poolConfig;

  /** Factory to create ldap objects. */
  protected LdapFactory<T> ldapFactory;

  /** Timer for scheduling pool tasks. */
  private Timer poolTimer = new Timer(true);


  /**
   * Creates a new pool with the supplied pool configuration and ldap factory.
   * The pool configuration will be marked as immutable by this pool.
   *
   * @param  lpc  <code>LdapPoolConfig</code>
   * @param  lf  <code>LdapFactory</code>
   */
  public AbstractLdapPool(final LdapPoolConfig lpc, final LdapFactory<T> lf)
  {
    this.poolConfig = lpc;
    this.poolConfig.makeImmutable();
    this.ldapFactory = lf;
  }


  /** {@inheritDoc}. */
  public LdapPoolConfig getLdapPoolConfig()
  {
    return this.poolConfig;
  }


  /** {@inheritDoc}. */
  public void setPoolTimer(final Timer t)
  {
    this.poolTimer = t;
  }


  /** {@inheritDoc}. */
  public void initialize()
  {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("beginning pool initialization");
    }

    this.poolTimer.scheduleAtFixedRate(
      new PrunePoolTask<T>(this),
      this.poolConfig.getPruneTimerPeriod(),
      this.poolConfig.getPruneTimerPeriod());
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("prune pool task scheduled");
    }

    this.poolTimer.scheduleAtFixedRate(
      new ValidatePoolTask<T>(this),
      this.poolConfig.getValidateTimerPeriod(),
      this.poolConfig.getValidateTimerPeriod());
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("validate pool task scheduled");
    }

    this.initializePool();

    if (this.logger.isDebugEnabled()) {
      this.logger.debug("pool initialized to size " + this.available.size());
    }
  }


  /** Attempts to fill the pool to its minimum size. */
  private void initializePool()
  {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug(
        "checking ldap pool size >= " + this.poolConfig.getMinPoolSize());
    }

    int count = 0;
    this.poolLock.lock();
    try {
      while (
        this.available.size() < this.poolConfig.getMinPoolSize() &&
          count < this.poolConfig.getMinPoolSize() * 2) {
        final T t = this.createAvailable();
        if (this.poolConfig.isValidateOnCheckIn()) {
          if (this.ldapFactory.validate(t)) {
            if (this.logger.isTraceEnabled()) {
              this.logger.trace(
                "ldap object passed initialize validation: " + t);
            }
          } else {
            if (this.logger.isWarnEnabled()) {
              this.logger.warn(
                "ldap object failed initialize validation: " + t);
            }
            this.removeAvailable(t);
          }
        }
        count++;
      }
    } finally {
      this.poolLock.unlock();
    }
  }


  /** {@inheritDoc}. */
  public void close()
  {
    this.poolLock.lock();
    try {
      while (this.available.size() > 0) {
        final PooledLdap<T> pl = this.available.remove();
        this.ldapFactory.destroy(pl.getLdap());
      }
      while (this.active.size() > 0) {
        final PooledLdap<T> pl = this.active.remove();
        this.ldapFactory.destroy(pl.getLdap());
      }
      if (this.logger.isDebugEnabled()) {
        this.logger.debug("pool closed");
      }
    } finally {
      this.poolLock.unlock();
    }

    this.poolTimer.cancel();
  }


  /**
   * Create a new ldap object and place it in the available pool.
   *
   * @return  ldap object that was placed in the available pool
   */
  protected T createAvailable()
  {
    final T t = this.ldapFactory.create();
    if (t != null) {
      final PooledLdap<T> pl = new PooledLdap<T>(t);
      this.poolLock.lock();
      try {
        this.available.add(pl);
      } finally {
        this.poolLock.unlock();
      }
    } else {
      if (this.logger.isWarnEnabled()) {
        this.logger.warn("unable to create available ldap object");
      }
    }
    return t;
  }


  /**
   * Create a new ldap object and place it in the active pool.
   *
   * @return  ldap object that was placed in the active pool
   */
  protected T createActive()
  {
    final T t = this.ldapFactory.create();
    if (t != null) {
      final PooledLdap<T> pl = new PooledLdap<T>(t);
      this.poolLock.lock();
      try {
        this.active.add(pl);
      } finally {
        this.poolLock.unlock();
      }
    } else {
      if (this.logger.isWarnEnabled()) {
        this.logger.warn("unable to create active ldap object");
      }
    }
    return t;
  }


  /**
   * Create a new ldap object and place it in both the available and active
   * pools.
   *
   * @return  ldap object that was placed in the available and active pools
   */
  protected T createAvailableAndActive()
  {
    final T t = this.ldapFactory.create();
    if (t != null) {
      final PooledLdap<T> pl = new PooledLdap<T>(t);
      this.poolLock.lock();
      try {
        this.available.add(pl);
        this.active.add(pl);
      } finally {
        this.poolLock.unlock();
      }
    } else {
      if (this.logger.isWarnEnabled()) {
        this.logger.warn("unable to create available and active ldap object");
      }
    }
    return t;
  }


  /**
   * Remove a ldap object from the available pool.
   *
   * @param  t  ldap object that exists in the available pool
   */
  protected void removeAvailable(final T t)
  {
    boolean destroy = false;
    final PooledLdap<T> pl = new PooledLdap<T>(t);
    this.poolLock.lock();
    try {
      if (this.available.remove(pl)) {
        destroy = true;
      } else {
        if (this.logger.isWarnEnabled()) {
          this.logger.warn(
            "attempt to remove unknown available ldap object: " + t);
        }
      }
    } finally {
      this.poolLock.unlock();
    }
    if (destroy) {
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("removing available ldap object: " + t);
      }
      this.ldapFactory.destroy(t);
    }
  }


  /**
   * Remove a ldap object from the active pool.
   *
   * @param  t  ldap object that exists in the active pool
   */
  protected void removeActive(final T t)
  {
    boolean destroy = false;
    final PooledLdap<T> pl = new PooledLdap<T>(t);
    this.poolLock.lock();
    try {
      if (this.active.remove(pl)) {
        destroy = true;
      } else {
        if (this.logger.isWarnEnabled()) {
          this.logger.warn(
            "attempt to remove unknown active ldap object: " + t);
        }
      }
    } finally {
      this.poolLock.unlock();
    }
    if (destroy) {
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("removing active ldap object: " + t);
      }
      this.ldapFactory.destroy(t);
    }
  }


  /**
   * Remove a ldap object from both the available and active pools.
   *
   * @param  t  ldap object that exists in the both the available and active
   * pools
   */
  protected void removeAvailableAndActive(final T t)
  {
    boolean destroy = false;
    final PooledLdap<T> pl = new PooledLdap<T>(t);
    this.poolLock.lock();
    try {
      if (this.available.remove(pl)) {
        destroy = true;
      } else {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug(
            "attempt to remove unknown available ldap object: " + t);
        }
      }
      if (this.active.remove(pl)) {
        destroy = true;
      } else {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug(
            "attempt to remove unknown active ldap object: " + t);
        }
      }
    } finally {
      this.poolLock.unlock();
    }
    if (destroy) {
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("removing active ldap object: " + t);
      }
      this.ldapFactory.destroy(t);
    }
  }


  /**
   * Attempts to activate and validate a ldap object. Performed before an object
   * is returned from {@link LdapPool#checkOut()}.
   *
   * @param  t  ldap object
   *
   * @throws  LdapPoolException  if this method fais
   * @throws  LdapActivationException  if the ldap object cannot be activated
   * @throws  LdapValidateException  if the ldap object cannot be validated
   */
  protected void activateAndValidate(final T t)
    throws LdapPoolException
  {
    if (!this.ldapFactory.activate(t)) {
      if (this.logger.isWarnEnabled()) {
        this.logger.warn("ldap object failed activation: " + t);
      }
      this.removeAvailableAndActive(t);
      throw new LdapActivationException("Activation of ldap object failed");
    }
    if (
      this.poolConfig.isValidateOnCheckOut() &&
        !this.ldapFactory.validate(t)) {
      if (this.logger.isWarnEnabled()) {
        this.logger.warn("ldap object failed check out validation: " + t);
      }
      this.removeAvailableAndActive(t);
      throw new LdapValidationException("Validation of ldap object failed");
    }
  }


  /**
   * Attempts to validate and passivate a ldap object. Performed when an object
   * is given to {@link LdapPool#checkIn}.
   *
   * @param  t  ldap object
   *
   * @return  whether both validate and passivation succeeded
   */
  protected boolean validateAndPassivate(final T t)
  {
    boolean valid = false;
    if (this.poolConfig.isValidateOnCheckIn()) {
      if (!this.ldapFactory.validate(t)) {
        if (this.logger.isWarnEnabled()) {
          this.logger.warn("ldap object failed check in validation: " + t);
        }
      } else {
        valid = true;
      }
    } else {
      valid = true;
    }
    if (valid && !this.ldapFactory.passivate(t)) {
      valid = false;
      if (this.logger.isWarnEnabled()) {
        this.logger.warn("ldap object failed activation: " + t);
      }
    }
    return valid;
  }


  /** {@inheritDoc}. */
  public void prune()
  {
    if (this.logger.isTraceEnabled()) {
      this.logger.trace(
        "waiting for pool lock to prune " + this.poolLock.getQueueLength());
    }
    this.poolLock.lock();
    try {
      if (this.active.size() == 0) {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug("pruning pool of size " + this.available.size());
        }
        while (this.available.size() > this.poolConfig.getMinPoolSize()) {
          PooledLdap<T> pl = this.available.peek();
          final long time = System.currentTimeMillis() - pl.getCreatedTime();
          if (time > this.poolConfig.getExpirationTime()) {
            pl = this.available.remove();
            if (this.logger.isTraceEnabled()) {
              this.logger.trace(
                "removing " + pl.getLdap() + " in the pool for " + time + "ms");
            }
            this.ldapFactory.destroy(pl.getLdap());
          } else {
            break;
          }
        }
        if (this.logger.isDebugEnabled()) {
          this.logger.debug("pool size pruned to " + this.available.size());
        }
      } else {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug("pool is currently active, no objects pruned");
        }
      }
    } finally {
      this.poolLock.unlock();
    }
  }


  /** {@inheritDoc}. */
  public void validate()
  {
    this.poolLock.lock();
    try {
      if (this.active.size() == 0) {
        if (this.poolConfig.isValidatePeriodically()) {
          if (this.logger.isDebugEnabled()) {
            this.logger.debug(
              "validate for pool of size " + this.available.size());
          }

          final Queue<PooledLdap<T>> remove = new LinkedList<PooledLdap<T>>();
          for (PooledLdap<T> pl : this.available) {
            if (this.logger.isTraceEnabled()) {
              this.logger.trace("validating " + pl.getLdap());
            }
            if (this.ldapFactory.validate(pl.getLdap())) {
              if (this.logger.isTraceEnabled()) {
                this.logger.trace(
                  "ldap object passed validation: " + pl.getLdap());
              }
            } else {
              if (this.logger.isWarnEnabled()) {
                this.logger.warn(
                  "ldap object failed validation: " + pl.getLdap());
              }
              remove.add(pl);
            }
          }
          for (PooledLdap<T> pl : remove) {
            if (this.logger.isTraceEnabled()) {
              this.logger.trace("removing " + pl.getLdap() + " from the pool");
            }
            this.available.remove(pl);
            this.ldapFactory.destroy(pl.getLdap());
          }
        }
        this.initializePool();
        if (this.logger.isDebugEnabled()) {
          this.logger.debug(
            "pool size after validation is " + this.available.size());
        }
      } else {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug(
            "pool is currently active, " +
            "no validation performed");
        }
      }
    } finally {
      this.poolLock.unlock();
    }
  }


  /** {@inheritDoc}. */
  public int availableCount()
  {
    return this.available.size();
  }


  /** {@inheritDoc}. */
  public int activeCount()
  {
    return this.active.size();
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


  /**
   * <code>PooledLdap</code> contains a ldap object that is participating in a
   * pool. Used to track how long a ldap object has been in either the available
   * or active queues.
   *
   * @param  <T>  type of ldap object
   */
  static protected class PooledLdap<T extends BaseLdap>
  {

    /** hash code seed. */
    protected static final int HASH_CODE_SEED = 89;

    /** Underlying ldap object. */
    private T ldap;

    /** Time this object was created. */
    private long createdTime;


    /**
     * Creates a new <code>PooledLdap</code> with the supplied ldap object.
     *
     * @param  t  ldap object
     */
    public PooledLdap(final T t)
    {
      this.ldap = t;
      this.createdTime = System.currentTimeMillis();
    }


    /**
     * Returns the ldap object.
     *
     * @return  underlying ldap object
     */
    public T getLdap()
    {
      return this.ldap;
    }


    /**
     * Returns the time this object was created.
     *
     * @return  creation time
     */
    public long getCreatedTime()
    {
      return this.createdTime;
    }


    /**
     * Returns whether the supplied <code>Object</code> contains the same data
     * as this bean.
     *
     * @param  o  <code>Object</code>
     *
     * @return  <code>boolean</code>
     */
    public boolean equals(final Object o)
    {
      if (o == null) {
        return false;
      }
      return
        o == this ||
          (this.getClass() == o.getClass() &&
            o.hashCode() == this.hashCode());
    }


    /**
     * This returns the hash code for this object.
     *
     * @return  <code>int</code>
     */
    public int hashCode()
    {
      int hc = HASH_CODE_SEED;
      if (this.ldap != null) {
        hc += this.ldap.hashCode();
      }
      return hc;
    }
  }
}
/*
  $Id: AbstractLdapPool.java 370 2009-07-27 21:54:31Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 370 $
  Updated: $Date: 2009-07-27 14:54:31 -0700 (Mon, 27 Jul 2009) $
*/
package edu.vt.middleware.ldap.pool;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import edu.vt.middleware.ldap.BaseLdap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <code>AbstractLdapPool</code> contains the basic implementation for pooling
 * ldap objects. The main design objective for the supplied pooling
 * implementations is to provide a pool that does not block on object creation
 * or destruction. This is what accounts for the multiple locks available on
 * this class. The pool is backed by two queues, one for available objects and
 * one for active objects. Objects that are available for {@link #checkOut()}
 * exist in the available queue. Objects that are actively in use exist in the
 * active queue. Note that depending on the implementation an object can exist
 * in both queues at the same time.
 *
 * @param  <T>  type of ldap object
 *
 * @author  Middleware Services
 * @version  $Revision: 370 $ $Date: 2009-07-27 14:54:31 -0700 (Mon, 27 Jul 2009) $
 */
public abstract class AbstractLdapPool<T extends BaseLdap>
  implements LdapPool<T>
{

  /** Lock for the entire pool. */
  protected final ReentrantLock poolLock = new ReentrantLock();

  /** Condition for notifying threads that an object was returned. */
  protected final Condition poolNotEmpty = poolLock.newCondition();

  /** Lock for check ins. */
  protected final ReentrantLock checkInLock = new ReentrantLock();

  /** Lock for check outs. */
  protected final ReentrantLock checkOutLock = new ReentrantLock();

  /** Log for this class. */
  protected final Log logger = LogFactory.getLog(this.getClass());

  /** List of available ldap objects in the pool. */
  protected Queue<PooledLdap<T>> available = new LinkedList<PooledLdap<T>>();

  /** List of ldap objects in use. */
  protected Queue<PooledLdap<T>> active = new LinkedList<PooledLdap<T>>();

  /** Ldap pool config. */
  protected LdapPoolConfig poolConfig;

  /** Factory to create ldap objects. */
  protected LdapFactory<T> ldapFactory;

  /** Timer for scheduling pool tasks. */
  private Timer poolTimer = new Timer(true);


  /**
   * Creates a new pool with the supplied pool configuration and ldap factory.
   * The pool configuration will be marked as immutable by this pool.
   *
   * @param  lpc  <code>LdapPoolConfig</code>
   * @param  lf  <code>LdapFactory</code>
   */
  public AbstractLdapPool(final LdapPoolConfig lpc, final LdapFactory<T> lf)
  {
    this.poolConfig = lpc;
    this.poolConfig.makeImmutable();
    this.ldapFactory = lf;
  }


  /** {@inheritDoc}. */
  public LdapPoolConfig getLdapPoolConfig()
  {
    return this.poolConfig;
  }


  /** {@inheritDoc}. */
  public void setPoolTimer(final Timer t)
  {
    this.poolTimer = t;
  }


  /** {@inheritDoc}. */
  public void initialize()
  {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("beginning pool initialization");
    }

    this.poolTimer.scheduleAtFixedRate(
      new PrunePoolTask<T>(this),
      this.poolConfig.getPruneTimerPeriod(),
      this.poolConfig.getPruneTimerPeriod());
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("prune pool task scheduled");
    }

    this.poolTimer.scheduleAtFixedRate(
      new ValidatePoolTask<T>(this),
      this.poolConfig.getValidateTimerPeriod(),
      this.poolConfig.getValidateTimerPeriod());
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("validate pool task scheduled");
    }

    this.initializePool();

    if (this.logger.isDebugEnabled()) {
      this.logger.debug("pool initialized to size " + this.available.size());
    }
  }


  /** Attempts to fill the pool to its minimum size. */
  private void initializePool()
  {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug(
        "checking ldap pool size >= " + this.poolConfig.getMinPoolSize());
    }

    int count = 0;
    this.poolLock.lock();
    try {
      while (
        this.available.size() < this.poolConfig.getMinPoolSize() &&
          count < this.poolConfig.getMinPoolSize() * 2) {
        final T t = this.createAvailable();
        if (this.poolConfig.isValidateOnCheckIn()) {
          if (this.ldapFactory.validate(t)) {
            if (this.logger.isTraceEnabled()) {
              this.logger.trace(
                "ldap object passed initialize validation: " + t);
            }
          } else {
            if (this.logger.isWarnEnabled()) {
              this.logger.warn(
                "ldap object failed initialize validation: " + t);
            }
            this.removeAvailable(t);
          }
        }
        count++;
      }
    } finally {
      this.poolLock.unlock();
    }
  }


  /** {@inheritDoc}. */
  public void close()
  {
    this.poolLock.lock();
    try {
      while (this.available.size() > 0) {
        final PooledLdap<T> pl = this.available.remove();
        this.ldapFactory.destroy(pl.getLdap());
      }
      while (this.active.size() > 0) {
        final PooledLdap<T> pl = this.active.remove();
        this.ldapFactory.destroy(pl.getLdap());
      }
      if (this.logger.isDebugEnabled()) {
        this.logger.debug("pool closed");
      }
    } finally {
      this.poolLock.unlock();
    }

    this.poolTimer.cancel();
  }


  /**
   * Create a new ldap object and place it in the available pool.
   *
   * @return  ldap object that was placed in the available pool
   */
  protected T createAvailable()
  {
    final T t = this.ldapFactory.create();
    if (t != null) {
      final PooledLdap<T> pl = new PooledLdap<T>(t);
      this.poolLock.lock();
      try {
        this.available.add(pl);
      } finally {
        this.poolLock.unlock();
      }
    } else {
      if (this.logger.isWarnEnabled()) {
        this.logger.warn("unable to create available ldap object");
      }
    }
    return t;
  }


  /**
   * Create a new ldap object and place it in the active pool.
   *
   * @return  ldap object that was placed in the active pool
   */
  protected T createActive()
  {
    final T t = this.ldapFactory.create();
    if (t != null) {
      final PooledLdap<T> pl = new PooledLdap<T>(t);
      this.poolLock.lock();
      try {
        this.active.add(pl);
      } finally {
        this.poolLock.unlock();
      }
    } else {
      if (this.logger.isWarnEnabled()) {
        this.logger.warn("unable to create active ldap object");
      }
    }
    return t;
  }


  /**
   * Create a new ldap object and place it in both the available and active
   * pools.
   *
   * @return  ldap object that was placed in the available and active pools
   */
  protected T createAvailableAndActive()
  {
    final T t = this.ldapFactory.create();
    if (t != null) {
      final PooledLdap<T> pl = new PooledLdap<T>(t);
      this.poolLock.lock();
      try {
        this.available.add(pl);
        this.active.add(pl);
      } finally {
        this.poolLock.unlock();
      }
    } else {
      if (this.logger.isWarnEnabled()) {
        this.logger.warn("unable to create available and active ldap object");
      }
    }
    return t;
  }


  /**
   * Remove a ldap object from the available pool.
   *
   * @param  t  ldap object that exists in the available pool
   */
  protected void removeAvailable(final T t)
  {
    boolean destroy = false;
    final PooledLdap<T> pl = new PooledLdap<T>(t);
    this.poolLock.lock();
    try {
      if (this.available.remove(pl)) {
        destroy = true;
      } else {
        if (this.logger.isWarnEnabled()) {
          this.logger.warn(
            "attempt to remove unknown available ldap object: " + t);
        }
      }
    } finally {
      this.poolLock.unlock();
    }
    if (destroy) {
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("removing available ldap object: " + t);
      }
      this.ldapFactory.destroy(t);
    }
  }


  /**
   * Remove a ldap object from the active pool.
   *
   * @param  t  ldap object that exists in the active pool
   */
  protected void removeActive(final T t)
  {
    boolean destroy = false;
    final PooledLdap<T> pl = new PooledLdap<T>(t);
    this.poolLock.lock();
    try {
      if (this.active.remove(pl)) {
        destroy = true;
      } else {
        if (this.logger.isWarnEnabled()) {
          this.logger.warn(
            "attempt to remove unknown active ldap object: " + t);
        }
      }
    } finally {
      this.poolLock.unlock();
    }
    if (destroy) {
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("removing active ldap object: " + t);
      }
      this.ldapFactory.destroy(t);
    }
  }


  /**
   * Remove a ldap object from both the available and active pools.
   *
   * @param  t  ldap object that exists in the both the available and active
   * pools
   */
  protected void removeAvailableAndActive(final T t)
  {
    boolean destroy = false;
    final PooledLdap<T> pl = new PooledLdap<T>(t);
    this.poolLock.lock();
    try {
      if (this.available.remove(pl)) {
        destroy = true;
      } else {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug(
            "attempt to remove unknown available ldap object: " + t);
        }
      }
      if (this.active.remove(pl)) {
        destroy = true;
      } else {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug(
            "attempt to remove unknown active ldap object: " + t);
        }
      }
    } finally {
      this.poolLock.unlock();
    }
    if (destroy) {
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("removing active ldap object: " + t);
      }
      this.ldapFactory.destroy(t);
    }
  }


  /**
   * Attempts to activate and validate a ldap object. Performed before an object
   * is returned from {@link LdapPool#checkOut()}.
   *
   * @param  t  ldap object
   *
   * @throws  LdapPoolException  if this method fais
   * @throws  LdapActivationException  if the ldap object cannot be activated
   * @throws  LdapValidateException  if the ldap object cannot be validated
   */
  protected void activateAndValidate(final T t)
    throws LdapPoolException
  {
    if (!this.ldapFactory.activate(t)) {
      if (this.logger.isWarnEnabled()) {
        this.logger.warn("ldap object failed activation: " + t);
      }
      this.removeAvailableAndActive(t);
      throw new LdapActivationException("Activation of ldap object failed");
    }
    if (
      this.poolConfig.isValidateOnCheckOut() &&
        !this.ldapFactory.validate(t)) {
      if (this.logger.isWarnEnabled()) {
        this.logger.warn("ldap object failed check out validation: " + t);
      }
      this.removeAvailableAndActive(t);
      throw new LdapValidationException("Validation of ldap object failed");
    }
  }


  /**
   * Attempts to validate and passivate a ldap object. Performed when an object
   * is given to {@link LdapPool#checkIn}.
   *
   * @param  t  ldap object
   *
   * @return  whether both validate and passivation succeeded
   */
  protected boolean validateAndPassivate(final T t)
  {
    boolean valid = false;
    if (this.poolConfig.isValidateOnCheckIn()) {
      if (!this.ldapFactory.validate(t)) {
        if (this.logger.isWarnEnabled()) {
          this.logger.warn("ldap object failed check in validation: " + t);
        }
      } else {
        valid = true;
      }
    } else {
      valid = true;
    }
    if (valid && !this.ldapFactory.passivate(t)) {
      valid = false;
      if (this.logger.isWarnEnabled()) {
        this.logger.warn("ldap object failed activation: " + t);
      }
    }
    return valid;
  }


  /** {@inheritDoc}. */
  public void prune()
  {
    if (this.logger.isTraceEnabled()) {
      this.logger.trace(
        "waiting for pool lock to prune " + this.poolLock.getQueueLength());
    }
    this.poolLock.lock();
    try {
      if (this.active.size() == 0) {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug("pruning pool of size " + this.available.size());
        }
        while (this.available.size() > this.poolConfig.getMinPoolSize()) {
          PooledLdap<T> pl = this.available.peek();
          final long time = System.currentTimeMillis() - pl.getCreatedTime();
          if (time > this.poolConfig.getExpirationTime()) {
            pl = this.available.remove();
            if (this.logger.isTraceEnabled()) {
              this.logger.trace(
                "removing " + pl.getLdap() + " in the pool for " + time + "ms");
            }
            this.ldapFactory.destroy(pl.getLdap());
          } else {
            break;
          }
        }
        if (this.logger.isDebugEnabled()) {
          this.logger.debug("pool size pruned to " + this.available.size());
        }
      } else {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug("pool is currently active, no objects pruned");
        }
      }
    } finally {
      this.poolLock.unlock();
    }
  }


  /** {@inheritDoc}. */
  public void validate()
  {
    this.poolLock.lock();
    try {
      if (this.active.size() == 0) {
        if (this.poolConfig.isValidatePeriodically()) {
          if (this.logger.isDebugEnabled()) {
            this.logger.debug(
              "validate for pool of size " + this.available.size());
          }

          final Queue<PooledLdap<T>> remove = new LinkedList<PooledLdap<T>>();
          for (PooledLdap<T> pl : this.available) {
            if (this.logger.isTraceEnabled()) {
              this.logger.trace("validating " + pl.getLdap());
            }
            if (this.ldapFactory.validate(pl.getLdap())) {
              if (this.logger.isTraceEnabled()) {
                this.logger.trace(
                  "ldap object passed validation: " + pl.getLdap());
              }
            } else {
              if (this.logger.isWarnEnabled()) {
                this.logger.warn(
                  "ldap object failed validation: " + pl.getLdap());
              }
              remove.add(pl);
            }
          }
          for (PooledLdap<T> pl : remove) {
            if (this.logger.isTraceEnabled()) {
              this.logger.trace("removing " + pl.getLdap() + " from the pool");
            }
            this.available.remove(pl);
            this.ldapFactory.destroy(pl.getLdap());
          }
        }
        this.initializePool();
        if (this.logger.isDebugEnabled()) {
          this.logger.debug(
            "pool size after validation is " + this.available.size());
        }
      } else {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug(
            "pool is currently active, " +
            "no validation performed");
        }
      }
    } finally {
      this.poolLock.unlock();
    }
  }


  /** {@inheritDoc}. */
  public int availableCount()
  {
    return this.available.size();
  }


  /** {@inheritDoc}. */
  public int activeCount()
  {
    return this.active.size();
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


  /**
   * <code>PooledLdap</code> contains a ldap object that is participating in a
   * pool. Used to track how long a ldap object has been in either the available
   * or active queues.
   *
   * @param  <T>  type of ldap object
   */
  static protected class PooledLdap<T extends BaseLdap>
  {

    /** hash code seed. */
    protected static final int HASH_CODE_SEED = 89;

    /** Underlying ldap object. */
    private T ldap;

    /** Time this object was created. */
    private long createdTime;


    /**
     * Creates a new <code>PooledLdap</code> with the supplied ldap object.
     *
     * @param  t  ldap object
     */
    public PooledLdap(final T t)
    {
      this.ldap = t;
      this.createdTime = System.currentTimeMillis();
    }


    /**
     * Returns the ldap object.
     *
     * @return  underlying ldap object
     */
    public T getLdap()
    {
      return this.ldap;
    }


    /**
     * Returns the time this object was created.
     *
     * @return  creation time
     */
    public long getCreatedTime()
    {
      return this.createdTime;
    }


    /**
     * Returns whether the supplied <code>Object</code> contains the same data
     * as this bean.
     *
     * @param  o  <code>Object</code>
     *
     * @return  <code>boolean</code>
     */
    public boolean equals(final Object o)
    {
      if (o == null) {
        return false;
      }
      return
        o == this ||
          (this.getClass() == o.getClass() &&
            o.hashCode() == this.hashCode());
    }


    /**
     * This returns the hash code for this object.
     *
     * @return  <code>int</code>
     */
    public int hashCode()
    {
      int hc = HASH_CODE_SEED;
      if (this.ldap != null) {
        hc += this.ldap.hashCode();
      }
      return hc;
    }
  }
}
/*
  $Id: AbstractLdapPool.java 282 2009-05-29 21:57:32Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 282 $
  Updated: $Date: 2009-05-29 14:57:32 -0700 (Fri, 29 May 2009) $
*/
package edu.vt.middleware.ldap.pool;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import edu.vt.middleware.ldap.BaseLdap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <code>AbstractLdapPool</code> contains the basic implementation for pooling
 * ldap objects. The main design objective for the supplied pooling
 * implementations is to provide a pool that does not block on object creation
 * or destruction. This is what accounts for the multiple locks available on
 * this class. The pool is backed by two queues, one for available objects and
 * one for active objects. Objects that are available for {@link #checkOut()}
 * exist in the available queue. Objects that are actively in use exist in the
 * active queue. Note that depending on the implementation an object can exist
 * in both queues at the same time.
 *
 * @param  <T>  type of ldap object
 *
 * @author  Middleware Services
 * @version  $Revision: 282 $ $Date: 2009-05-29 14:57:32 -0700 (Fri, 29 May 2009) $
 */
public abstract class AbstractLdapPool<T extends BaseLdap>
  implements LdapPool<T>
{

  /** Lock for the entire pool. */
  protected final ReentrantLock poolLock = new ReentrantLock();

  /** Condition for notifying threads that an object was returned. */
  protected final Condition poolNotEmpty = poolLock.newCondition();

  /** Lock for check ins. */
  protected final ReentrantLock checkInLock = new ReentrantLock();

  /** Lock for check outs. */
  protected final ReentrantLock checkOutLock = new ReentrantLock();

  /** Log for this class. */
  protected final Log logger = LogFactory.getLog(this.getClass());

  /** List of available ldap objects in the pool. */
  protected Queue<PooledLdap<T>> available = new LinkedList<PooledLdap<T>>();

  /** List of ldap objects in use. */
  protected Queue<PooledLdap<T>> active = new LinkedList<PooledLdap<T>>();

  /** Ldap pool config. */
  protected LdapPoolConfig poolConfig;

  /** Factory to create ldap objects. */
  protected LdapFactory<T> ldapFactory;

  /** Timer for scheduling pool tasks. */
  private Timer poolTimer = new Timer(true);


  /**
   * Creates a new pool with the supplied pool configuration and ldap factory.
   * The pool configuration will be marked as immutable by this pool.
   *
   * @param  lpc  <code>LdapPoolConfig</code>
   * @param  lf  <code>LdapFactory</code>
   */
  public AbstractLdapPool(final LdapPoolConfig lpc, final LdapFactory<T> lf)
  {
    this.poolConfig = lpc;
    this.poolConfig.makeImmutable();
    this.ldapFactory = lf;
  }


  /** {@inheritDoc}. */
  public LdapPoolConfig getLdapPoolConfig()
  {
    return this.poolConfig;
  }


  /** {@inheritDoc}. */
  public void setPoolTimer(final Timer t)
  {
    this.poolTimer = t;
  }


  /** {@inheritDoc}. */
  public void initialize()
  {
    this.poolTimer.scheduleAtFixedRate(
      new PrunePoolTask<T>(this),
      this.poolConfig.getPruneTimerPeriod(),
      this.poolConfig.getPruneTimerPeriod());

    this.poolTimer.scheduleAtFixedRate(
      new ValidatePoolTask<T>(this),
      this.poolConfig.getValidateTimerPeriod(),
      this.poolConfig.getValidateTimerPeriod());

    this.initializePool();

    if (this.available.size() == 0) {
      throw new IllegalStateException("unnable to initialize ldap pool");
    }
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("pool initialized to size " + this.available.size());
    }
  }


  /** Attempts to fill the pool to its minimum size. */
  private void initializePool()
  {
    int count = 0;
    this.poolLock.lock();
    try {
      while (
        this.available.size() < this.poolConfig.getMinPoolSize() &&
          count < this.poolConfig.getMinPoolSize() * 2) {
        final T t = this.createAvailable();
        if (this.poolConfig.isValidateOnCheckIn()) {
          if (this.ldapFactory.validate(t)) {
            if (this.logger.isTraceEnabled()) {
              this.logger.trace(
                "ldap object passed initialize validation: " + t);
            }
          } else {
            if (this.logger.isWarnEnabled()) {
              this.logger.warn(
                "ldap object failed initialize validation: " + t);
            }
            this.removeAvailable(t);
          }
        }
        count++;
      }
    } finally {
      this.poolLock.unlock();
    }
  }


  /** {@inheritDoc}. */
  public void close()
  {
    this.poolLock.lock();
    try {
      while (this.available.size() > 0) {
        final PooledLdap<T> pl = this.available.remove();
        this.ldapFactory.destroy(pl.getLdap());
      }
      while (this.active.size() > 0) {
        final PooledLdap<T> pl = this.active.remove();
        this.ldapFactory.destroy(pl.getLdap());
      }
      if (this.logger.isDebugEnabled()) {
        this.logger.debug("pool closed");
      }
    } finally {
      this.poolLock.unlock();
    }

    this.poolTimer.cancel();
  }


  /**
   * Create a new ldap object and place it in the available pool.
   *
   * @return  ldap object that was placed in the available pool
   */
  protected T createAvailable()
  {
    final T t = this.ldapFactory.create();
    if (t != null) {
      final PooledLdap<T> pl = new PooledLdap<T>(t);
      this.poolLock.lock();
      try {
        this.available.add(pl);
      } finally {
        this.poolLock.unlock();
      }
    } else {
      if (this.logger.isWarnEnabled()) {
        this.logger.warn("unable to create available ldap object");
      }
    }
    return t;
  }


  /**
   * Create a new ldap object and place it in the active pool.
   *
   * @return  ldap object that was placed in the active pool
   */
  protected T createActive()
  {
    final T t = this.ldapFactory.create();
    if (t != null) {
      final PooledLdap<T> pl = new PooledLdap<T>(t);
      this.poolLock.lock();
      try {
        this.active.add(pl);
      } finally {
        this.poolLock.unlock();
      }
    } else {
      if (this.logger.isWarnEnabled()) {
        this.logger.warn("unable to create active ldap object");
      }
    }
    return t;
  }


  /**
   * Create a new ldap object and place it in both the available and active
   * pools.
   *
   * @return  ldap object that was placed in the available and active pools
   */
  protected T createAvailableAndActive()
  {
    final T t = this.ldapFactory.create();
    if (t != null) {
      final PooledLdap<T> pl = new PooledLdap<T>(t);
      this.poolLock.lock();
      try {
        this.available.add(pl);
        this.active.add(pl);
      } finally {
        this.poolLock.unlock();
      }
    } else {
      if (this.logger.isWarnEnabled()) {
        this.logger.warn("unable to create available and active ldap object");
      }
    }
    return t;
  }


  /**
   * Remove a ldap object from the available pool.
   *
   * @param  t  ldap object that exists in the available pool
   */
  protected void removeAvailable(final T t)
  {
    boolean destroy = false;
    final PooledLdap<T> pl = new PooledLdap<T>(t);
    this.poolLock.lock();
    try {
      if (this.available.remove(pl)) {
        destroy = true;
      } else {
        if (this.logger.isWarnEnabled()) {
          this.logger.warn(
            "attempt to remove unknown available ldap object: " + t);
        }
      }
    } finally {
      this.poolLock.unlock();
    }
    if (destroy) {
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("removing available ldap object: " + t);
      }
      this.ldapFactory.destroy(t);
    }
  }


  /**
   * Remove a ldap object from the active pool.
   *
   * @param  t  ldap object that exists in the active pool
   */
  protected void removeActive(final T t)
  {
    boolean destroy = false;
    final PooledLdap<T> pl = new PooledLdap<T>(t);
    this.poolLock.lock();
    try {
      if (this.active.remove(pl)) {
        destroy = true;
      } else {
        if (this.logger.isWarnEnabled()) {
          this.logger.warn(
            "attempt to remove unknown active ldap object: " + t);
        }
      }
    } finally {
      this.poolLock.unlock();
    }
    if (destroy) {
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("removing active ldap object: " + t);
      }
      this.ldapFactory.destroy(t);
    }
  }


  /**
   * Remove a ldap object from both the available and active pools.
   *
   * @param  t  ldap object that exists in the both the available and active
   * pools
   */
  protected void removeAvailableAndActive(final T t)
  {
    boolean destroy = false;
    final PooledLdap<T> pl = new PooledLdap<T>(t);
    this.poolLock.lock();
    try {
      if (this.available.remove(pl)) {
        destroy = true;
      } else {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug(
            "attempt to remove unknown available ldap object: " + t);
        }
      }
      if (this.active.remove(pl)) {
        destroy = true;
      } else {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug(
            "attempt to remove unknown active ldap object: " + t);
        }
      }
    } finally {
      this.poolLock.unlock();
    }
    if (destroy) {
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("removing active ldap object: " + t);
      }
      this.ldapFactory.destroy(t);
    }
  }


  /**
   * Attempts to activate and validate a ldap object. Performed before an object
   * is returned from {@link LdapPool#checkOut()}.
   *
   * @param  t  ldap object
   *
   * @throws  LdapPoolException  if this method fais
   * @throws  LdapActivationException  if the ldap object cannot be activated
   * @throws  LdapValidateException  if the ldap object cannot be validated
   */
  protected void activateAndValidate(final T t)
    throws LdapPoolException
  {
    if (!this.ldapFactory.activate(t)) {
      if (this.logger.isWarnEnabled()) {
        this.logger.warn("ldap object failed activation: " + t);
      }
      this.removeAvailableAndActive(t);
      throw new LdapActivationException("Activation of ldap object failed");
    }
    if (
      this.poolConfig.isValidateOnCheckOut() &&
        !this.ldapFactory.validate(t)) {
      if (this.logger.isWarnEnabled()) {
        this.logger.warn("ldap object failed check out validation: " + t);
      }
      this.removeAvailableAndActive(t);
      throw new LdapValidationException("Validation of ldap object failed");
    }
  }


  /**
   * Attempts to validate and passivate a ldap object. Performed when an object
   * is given to {@link LdapPool#checkIn}.
   *
   * @param  t  ldap object
   *
   * @return  whether both validate and passivation succeeded
   */
  protected boolean validateAndPassivate(final T t)
  {
    boolean valid = false;
    if (this.poolConfig.isValidateOnCheckIn()) {
      if (!this.ldapFactory.validate(t)) {
        if (this.logger.isWarnEnabled()) {
          this.logger.warn("ldap object failed check in validation: " + t);
        }
      } else {
        valid = true;
      }
    } else {
      valid = true;
    }
    if (valid && !this.ldapFactory.passivate(t)) {
      valid = false;
      if (this.logger.isWarnEnabled()) {
        this.logger.warn("ldap object failed activation: " + t);
      }
    }
    return valid;
  }


  /** {@inheritDoc}. */
  public void prune()
  {
    if (this.logger.isTraceEnabled()) {
      this.logger.trace(
        "waiting for pool lock to prune " + this.poolLock.getQueueLength());
    }
    this.poolLock.lock();
    try {
      if (this.active.size() == 0) {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug("pruning pool of size " + this.available.size());
        }
        while (this.available.size() > this.poolConfig.getMinPoolSize()) {
          PooledLdap<T> pl = this.available.peek();
          final long time = System.currentTimeMillis() - pl.getCreatedTime();
          if (time > this.poolConfig.getExpirationTime()) {
            pl = this.available.remove();
            if (this.logger.isTraceEnabled()) {
              this.logger.trace(
                "removing " + pl.getLdap() + " in the pool for " + time + "ms");
            }
            this.ldapFactory.destroy(pl.getLdap());
          } else {
            break;
          }
        }
        if (this.logger.isDebugEnabled()) {
          this.logger.debug("pool size pruned to " + this.available.size());
        }
      } else {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug("pool is currently active, no objects pruned");
        }
      }
    } finally {
      this.poolLock.unlock();
    }
  }


  /** {@inheritDoc}. */
  public void validate()
  {
    this.poolLock.lock();
    try {
      if (this.active.size() == 0) {
        if (this.poolConfig.isValidatePeriodically()) {
          if (this.logger.isDebugEnabled()) {
            this.logger.debug(
              "validate for pool of size " + this.available.size());
          }

          final Queue<PooledLdap<T>> remove = new LinkedList<PooledLdap<T>>();
          for (PooledLdap<T> pl : this.available) {
            if (this.logger.isTraceEnabled()) {
              this.logger.trace("validating " + pl.getLdap());
            }
            if (this.ldapFactory.validate(pl.getLdap())) {
              if (this.logger.isTraceEnabled()) {
                this.logger.trace(
                  "ldap object passed validation: " + pl.getLdap());
              }
            } else {
              if (this.logger.isWarnEnabled()) {
                this.logger.warn(
                  "ldap object failed validation: " + pl.getLdap());
              }
              remove.add(pl);
            }
          }
          for (PooledLdap<T> pl : remove) {
            if (this.logger.isTraceEnabled()) {
              this.logger.trace("removing " + pl.getLdap() + " from the pool");
            }
            this.available.remove(pl);
            this.ldapFactory.destroy(pl.getLdap());
          }
        }
        this.initializePool();
        if (this.logger.isDebugEnabled()) {
          this.logger.debug(
            "pool size after validation is " + this.available.size());
        }
      } else {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug(
            "pool is currently active, " +
            "no validation performed");
        }
      }
    } finally {
      this.poolLock.unlock();
    }
  }


  /** {@inheritDoc}. */
  public int availableCount()
  {
    return this.available.size();
  }


  /** {@inheritDoc}. */
  public int activeCount()
  {
    return this.active.size();
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


  /**
   * <code>PooledLdap</code> contains a ldap object that is participating in a
   * pool. Used to track how long a ldap object has been in either the available
   * or active queues.
   *
   * @param  <T>  type of ldap object
   */
  static protected class PooledLdap<T extends BaseLdap>
  {

    /** hash code seed. */
    protected static final int HASH_CODE_SEED = 89;

    /** Underlying ldap object. */
    private T ldap;

    /** Time this object was created. */
    private long createdTime;


    /**
     * Creates a new <code>PooledLdap</code> with the supplied ldap object.
     *
     * @param  t  ldap object
     */
    public PooledLdap(final T t)
    {
      this.ldap = t;
      this.createdTime = System.currentTimeMillis();
    }


    /**
     * Returns the ldap object.
     *
     * @return  underlying ldap object
     */
    public T getLdap()
    {
      return this.ldap;
    }


    /**
     * Returns the time this object was created.
     *
     * @return  creation time
     */
    public long getCreatedTime()
    {
      return this.createdTime;
    }


    /**
     * Returns whether the supplied <code>Object</code> contains the same data
     * as this bean.
     *
     * @param  o  <code>Object</code>
     *
     * @return  <code>boolean</code>
     */
    public boolean equals(final Object o)
    {
      if (o == null) {
        return false;
      }
      return
        o == this ||
          (this.getClass() == o.getClass() &&
            o.hashCode() == this.hashCode());
    }


    /**
     * This returns the hash code for this object.
     *
     * @return  <code>int</code>
     */
    public int hashCode()
    {
      int hc = HASH_CODE_SEED;
      if (this.ldap != null) {
        hc += this.ldap.hashCode();
      }
      return hc;
    }
  }
}
