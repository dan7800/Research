/*
  $Id: BlockingLdapPool.java 282 2009-05-29 21:57:32Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 282 $
  Updated: $Date: 2009-05-29 14:57:32 -0700 (Fri, 29 May 2009) $
*/
package edu.vt.middleware.ldap.pool;

import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import edu.vt.middleware.ldap.Ldap;

/**
 * <code>BlockingLdapPool</code> implements a pool of ldap objects that has a
 * set minimum and maximum size. The pool will not grow beyond the maximum size
 * and when the pool is exhausted, requests for new objects will block. The
 * length of time the pool will block is determined by {@link
 * #getBlockWaitTime()}. By default the pool will block indefinitely and there
 * is no guarantee that waiting threads will be serviced in the order in which
 * they made their request. This implementation should be used when you need to
 * control the <em>exact</em> number of ldap connections that can be created.
 * See {@link AbstractLdapPool}.
 *
 * @author  Middleware Services
 * @version  $Revision: 282 $ $Date: 2009-05-29 14:57:32 -0700 (Fri, 29 May 2009) $
 */
public class BlockingLdapPool extends AbstractLdapPool<Ldap>
{

  /** Time in milliseconds to wait for an available ldap object. */
  private long blockWaitTime;


  /** Creates a new ldap pool using {@link DefaultLdapFactory}. */
  public BlockingLdapPool()
  {
    super(new LdapPoolConfig(), new DefaultLdapFactory());
  }


  /**
   * Creates a new ldap pool with the supplied ldap factory.
   *
   * @param  lf  ldap factory
   */
  public BlockingLdapPool(final LdapFactory<Ldap> lf)
  {
    super(new LdapPoolConfig(), lf);
  }


  /**
   * Creates a new ldap pool with the supplied ldap config and factory.
   *
   * @param  lpc  ldap pool configuration
   * @param  lf  ldap factory
   */
  public BlockingLdapPool(final LdapPoolConfig lpc, final LdapFactory<Ldap> lf)
  {
    super(lpc, lf);
  }


  /**
   * Returns the block wait time. Default time is 0, which will wait
   * indefinitely.
   *
   * @return  time in milliseconds to wait for available ldap objects
   */
  public long getBlockWaitTime()
  {
    return this.blockWaitTime;
  }


  /**
   * Sets the block wait time. Default time is 0, which will wait indefinitely.
   *
   * @param  time  in milliseconds to wait for available ldap objects
   */
  public void setBlockWaitTime(final long time)
  {
    if (time >= 0) {
      this.blockWaitTime = time;
    }
  }


  /** {@inheritDoc}. */
  public Ldap checkOut()
    throws LdapPoolException
  {
    Ldap l = null;
    boolean create = false;
    if (this.logger.isTraceEnabled()) {
      this.logger.trace(
        "waiting on pool lock for check out " + this.poolLock.getQueueLength());
    }
    this.poolLock.lock();
    try {
      // if an available object exists, use it
      // if no available objects and the pool can grow, attempt to create
      // otherwise the pool is full, block until an object is returned
      if (this.available.size() > 0) {
        try {
          if (this.logger.isTraceEnabled()) {
            this.logger.trace("retrieve available ldap object");
          }
          l = this.retrieveAvailable();
        } catch (NoSuchElementException e) {
          if (this.logger.isErrorEnabled()) {
            this.logger.error("could not remove ldap object from list", e);
          }
          throw new IllegalStateException("Pool is empty", e);
        }
      } else if (this.active.size() < this.poolConfig.getMaxPoolSize()) {
        if (this.logger.isTraceEnabled()) {
          this.logger.trace("pool can grow, attempt to create ldap object");
        }
        create = true;
      } else {
        if (this.logger.isTraceEnabled()) {
          this.logger.trace(
            "pool is full, block until ldap object " +
            "is available");
        }
        l = this.blockAvailable();
      }
    } finally {
      this.poolLock.unlock();
    }

    if (create) {
      // previous block determined a creation should occur
      // block here until create occurs without locking the whole pool
      // if the pool is already maxed or creates are failing,
      // block until an object is available
      this.checkOutLock.lock();
      try {
        boolean b = true;
        this.poolLock.lock();
        try {
          if (
            this.available.size() + this.active.size() ==
              this.poolConfig.getMaxPoolSize()) {
            b = false;
          }
        } finally {
          this.poolLock.unlock();
        }
        if (b) {
          l = this.createActive();
          if (this.logger.isTraceEnabled()) {
            this.logger.trace("created new active ldap object: " + l);
          }
        }
      } finally {
        this.checkOutLock.unlock();
      }
      if (l == null) {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug(
            "create failed, block until ldap object " +
            "is available");
        }
        l = this.blockAvailable();
      }
    }

    if (l != null) {
      this.activateAndValidate(l);
    } else {
      if (this.logger.isErrorEnabled()) {
        this.logger.error("Could not service check out request");
      }
      throw new LdapPoolExhaustedException(
        "Pool is empty and object creation failed");
    }

    return l;
  }


  /**
   * This attempts to retrieve a ldap object from the available queue.
   *
   * @return  ldap object from the pool
   *
   * @throws  NoSuchElementException  if the available queue is empty
   */
  protected Ldap retrieveAvailable()
  {
    Ldap l = null;
    if (this.logger.isTraceEnabled()) {
      this.logger.trace(
        "waiting on pool lock for retrieve available " +
        this.poolLock.getQueueLength());
    }
    this.poolLock.lock();
    try {
      final PooledLdap<Ldap> pl = this.available.remove();
      this.active.add(new PooledLdap<Ldap>(pl.getLdap()));
      l = pl.getLdap();
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("retrieved available ldap object: " + l);
      }
    } finally {
      this.poolLock.unlock();
    }
    return l;
  }


  /**
   * This blocks until a ldap object can be aquired.
   *
   * @return  ldap object from the pool
   *
   * @throws  LdapPoolException  if this method fails
   * @throws  BlockingTimeoutException  if this pool is configured with a block
   * time and it occurs
   * @throws  PoolInterruptedException  if the current thread is interrupted
   */
  protected Ldap blockAvailable()
    throws LdapPoolException
  {
    Ldap l = null;
    if (this.logger.isTraceEnabled()) {
      this.logger.trace(
        "waiting on pool lock for block available " +
        this.poolLock.getQueueLength());
    }
    this.poolLock.lock();
    try {
      while (l == null) {
        if (this.logger.isTraceEnabled()) {
          this.logger.trace("available pool is empty, waiting...");
        }
        if (this.blockWaitTime > 0) {
          if (
            !this.poolNotEmpty.await(
                this.blockWaitTime,
                TimeUnit.MILLISECONDS)) {
            if (this.logger.isDebugEnabled()) {
              this.logger.debug("block time exceeded, throwing exception");
            }
            throw new BlockingTimeoutException("Block time exceeded");
          }
        } else {
          this.poolNotEmpty.await();
        }
        if (this.logger.isTraceEnabled()) {
          this.logger.trace("notified to continue...");
        }
        try {
          l = this.retrieveAvailable();
        } catch (NoSuchElementException e) {
          if (this.logger.isTraceEnabled()) {
            this.logger.trace("notified to continue but pool was empty");
          }
        }
      }
    } catch (InterruptedException e) {
      if (this.logger.isErrorEnabled()) {
        this.logger.error("waiting for available object interrupted", e);
      }
      throw new PoolInterruptedException(
        "Interrupted while waiting for an available object",
        e);
    } finally {
      this.poolLock.unlock();
    }
    return l;
  }


  /** {@inheritDoc}. */
  public void checkIn(final Ldap l)
  {
    final boolean valid = this.validateAndPassivate(l);
    final PooledLdap<Ldap> pl = new PooledLdap<Ldap>(l);
    if (this.logger.isTraceEnabled()) {
      this.logger.trace(
        "waiting on pool lock for check in " + this.poolLock.getQueueLength());
    }
    this.poolLock.lock();
    try {
      if (this.active.remove(pl)) {
        if (valid) {
          this.available.add(pl);
          if (this.logger.isTraceEnabled()) {
            this.logger.trace("returned active ldap object: " + l);
          }
          this.poolNotEmpty.signal();
        }
      } else if (this.available.contains(pl)) {
        if (this.logger.isWarnEnabled()) {
          this.logger.warn("returned available ldap object: " + l);
        }
      } else {
        if (this.logger.isWarnEnabled()) {
          this.logger.warn("attempt to return unknown ldap object: " + l);
        }
      }
    } finally {
      this.poolLock.unlock();
    }
  }
}
/*
  $Id: BlockingLdapPool.java 282 2009-05-29 21:57:32Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 282 $
  Updated: $Date: 2009-05-29 14:57:32 -0700 (Fri, 29 May 2009) $
*/
package edu.vt.middleware.ldap.pool;

import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import edu.vt.middleware.ldap.Ldap;

/**
 * <code>BlockingLdapPool</code> implements a pool of ldap objects that has a
 * set minimum and maximum size. The pool will not grow beyond the maximum size
 * and when the pool is exhausted, requests for new objects will block. The
 * length of time the pool will block is determined by {@link
 * #getBlockWaitTime()}. By default the pool will block indefinitely and there
 * is no guarantee that waiting threads will be serviced in the order in which
 * they made their request. This implementation should be used when you need to
 * control the <em>exact</em> number of ldap connections that can be created.
 * See {@link AbstractLdapPool}.
 *
 * @author  Middleware Services
 * @version  $Revision: 282 $ $Date: 2009-05-29 14:57:32 -0700 (Fri, 29 May 2009) $
 */
public class BlockingLdapPool extends AbstractLdapPool<Ldap>
{

  /** Time in milliseconds to wait for an available ldap object. */
  private long blockWaitTime;


  /** Creates a new ldap pool using {@link DefaultLdapFactory}. */
  public BlockingLdapPool()
  {
    super(new LdapPoolConfig(), new DefaultLdapFactory());
  }


  /**
   * Creates a new ldap pool with the supplied ldap factory.
   *
   * @param  lf  ldap factory
   */
  public BlockingLdapPool(final LdapFactory<Ldap> lf)
  {
    super(new LdapPoolConfig(), lf);
  }


  /**
   * Creates a new ldap pool with the supplied ldap config and factory.
   *
   * @param  lpc  ldap pool configuration
   * @param  lf  ldap factory
   */
  public BlockingLdapPool(final LdapPoolConfig lpc, final LdapFactory<Ldap> lf)
  {
    super(lpc, lf);
  }


  /**
   * Returns the block wait time. Default time is 0, which will wait
   * indefinitely.
   *
   * @return  time in milliseconds to wait for available ldap objects
   */
  public long getBlockWaitTime()
  {
    return this.blockWaitTime;
  }


  /**
   * Sets the block wait time. Default time is 0, which will wait indefinitely.
   *
   * @param  time  in milliseconds to wait for available ldap objects
   */
  public void setBlockWaitTime(final long time)
  {
    if (time >= 0) {
      this.blockWaitTime = time;
    }
  }


  /** {@inheritDoc}. */
  public Ldap checkOut()
    throws LdapPoolException
  {
    Ldap l = null;
    boolean create = false;
    if (this.logger.isTraceEnabled()) {
      this.logger.trace(
        "waiting on pool lock for check out " + this.poolLock.getQueueLength());
    }
    this.poolLock.lock();
    try {
      // if an available object exists, use it
      // if no available objects and the pool can grow, attempt to create
      // otherwise the pool is full, block until an object is returned
      if (this.available.size() > 0) {
        try {
          if (this.logger.isTraceEnabled()) {
            this.logger.trace("retrieve available ldap object");
          }
          l = this.retrieveAvailable();
        } catch (NoSuchElementException e) {
          if (this.logger.isErrorEnabled()) {
            this.logger.error("could not remove ldap object from list", e);
          }
          throw new IllegalStateException("Pool is empty", e);
        }
      } else if (this.active.size() < this.poolConfig.getMaxPoolSize()) {
        if (this.logger.isTraceEnabled()) {
          this.logger.trace("pool can grow, attempt to create ldap object");
        }
        create = true;
      } else {
        if (this.logger.isTraceEnabled()) {
          this.logger.trace(
            "pool is full, block until ldap object " +
            "is available");
        }
        l = this.blockAvailable();
      }
    } finally {
      this.poolLock.unlock();
    }

    if (create) {
      // previous block determined a creation should occur
      // block here until create occurs without locking the whole pool
      // if the pool is already maxed or creates are failing,
      // block until an object is available
      this.checkOutLock.lock();
      try {
        boolean b = true;
        this.poolLock.lock();
        try {
          if (
            this.available.size() + this.active.size() ==
              this.poolConfig.getMaxPoolSize()) {
            b = false;
          }
        } finally {
          this.poolLock.unlock();
        }
        if (b) {
          l = this.createActive();
          if (this.logger.isTraceEnabled()) {
            this.logger.trace("created new active ldap object: " + l);
          }
        }
      } finally {
        this.checkOutLock.unlock();
      }
      if (l == null) {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug(
            "create failed, block until ldap object " +
            "is available");
        }
        l = this.blockAvailable();
      }
    }

    if (l != null) {
      this.activateAndValidate(l);
    } else {
      if (this.logger.isErrorEnabled()) {
        this.logger.error("Could not service check out request");
      }
      throw new LdapPoolExhaustedException(
        "Pool is empty and object creation failed");
    }

    return l;
  }


  /**
   * This attempts to retrieve a ldap object from the available queue.
   *
   * @return  ldap object from the pool
   *
   * @throws  NoSuchElementException  if the available queue is empty
   */
  protected Ldap retrieveAvailable()
  {
    Ldap l = null;
    if (this.logger.isTraceEnabled()) {
      this.logger.trace(
        "waiting on pool lock for retrieve available " +
        this.poolLock.getQueueLength());
    }
    this.poolLock.lock();
    try {
      final PooledLdap<Ldap> pl = this.available.remove();
      this.active.add(new PooledLdap<Ldap>(pl.getLdap()));
      l = pl.getLdap();
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("retrieved available ldap object: " + l);
      }
    } finally {
      this.poolLock.unlock();
    }
    return l;
  }


  /**
   * This blocks until a ldap object can be aquired.
   *
   * @return  ldap object from the pool
   *
   * @throws  LdapPoolException  if this method fails
   * @throws  BlockingTimeoutException  if this pool is configured with a block
   * time and it occurs
   * @throws  PoolInterruptedException  if the current thread is interrupted
   */
  protected Ldap blockAvailable()
    throws LdapPoolException
  {
    Ldap l = null;
    if (this.logger.isTraceEnabled()) {
      this.logger.trace(
        "waiting on pool lock for block available " +
        this.poolLock.getQueueLength());
    }
    this.poolLock.lock();
    try {
      while (l == null) {
        if (this.logger.isTraceEnabled()) {
          this.logger.trace("available pool is empty, waiting...");
        }
        if (this.blockWaitTime > 0) {
          if (
            !this.poolNotEmpty.await(
                this.blockWaitTime,
                TimeUnit.MILLISECONDS)) {
            if (this.logger.isDebugEnabled()) {
              this.logger.debug("block time exceeded, throwing exception");
            }
            throw new BlockingTimeoutException("Block time exceeded");
          }
        } else {
          this.poolNotEmpty.await();
        }
        if (this.logger.isTraceEnabled()) {
          this.logger.trace("notified to continue...");
        }
        try {
          l = this.retrieveAvailable();
        } catch (NoSuchElementException e) {
          if (this.logger.isTraceEnabled()) {
            this.logger.trace("notified to continue but pool was empty");
          }
        }
      }
    } catch (InterruptedException e) {
      if (this.logger.isErrorEnabled()) {
        this.logger.error("waiting for available object interrupted", e);
      }
      throw new PoolInterruptedException(
        "Interrupted while waiting for an available object",
        e);
    } finally {
      this.poolLock.unlock();
    }
    return l;
  }


  /** {@inheritDoc}. */
  public void checkIn(final Ldap l)
  {
    final boolean valid = this.validateAndPassivate(l);
    final PooledLdap<Ldap> pl = new PooledLdap<Ldap>(l);
    if (this.logger.isTraceEnabled()) {
      this.logger.trace(
        "waiting on pool lock for check in " + this.poolLock.getQueueLength());
    }
    this.poolLock.lock();
    try {
      if (this.active.remove(pl)) {
        if (valid) {
          this.available.add(pl);
          if (this.logger.isTraceEnabled()) {
            this.logger.trace("returned active ldap object: " + l);
          }
          this.poolNotEmpty.signal();
        }
      } else if (this.available.contains(pl)) {
        if (this.logger.isWarnEnabled()) {
          this.logger.warn("returned available ldap object: " + l);
        }
      } else {
        if (this.logger.isWarnEnabled()) {
          this.logger.warn("attempt to return unknown ldap object: " + l);
        }
      }
    } finally {
      this.poolLock.unlock();
    }
  }
}
/*
  $Id: BlockingLdapPool.java 282 2009-05-29 21:57:32Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 282 $
  Updated: $Date: 2009-05-29 14:57:32 -0700 (Fri, 29 May 2009) $
*/
package edu.vt.middleware.ldap.pool;

import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import edu.vt.middleware.ldap.Ldap;

/**
 * <code>BlockingLdapPool</code> implements a pool of ldap objects that has a
 * set minimum and maximum size. The pool will not grow beyond the maximum size
 * and when the pool is exhausted, requests for new objects will block. The
 * length of time the pool will block is determined by {@link
 * #getBlockWaitTime()}. By default the pool will block indefinitely and there
 * is no guarantee that waiting threads will be serviced in the order in which
 * they made their request. This implementation should be used when you need to
 * control the <em>exact</em> number of ldap connections that can be created.
 * See {@link AbstractLdapPool}.
 *
 * @author  Middleware Services
 * @version  $Revision: 282 $ $Date: 2009-05-29 14:57:32 -0700 (Fri, 29 May 2009) $
 */
public class BlockingLdapPool extends AbstractLdapPool<Ldap>
{

  /** Time in milliseconds to wait for an available ldap object. */
  private long blockWaitTime;


  /** Creates a new ldap pool using {@link DefaultLdapFactory}. */
  public BlockingLdapPool()
  {
    super(new LdapPoolConfig(), new DefaultLdapFactory());
  }


  /**
   * Creates a new ldap pool with the supplied ldap factory.
   *
   * @param  lf  ldap factory
   */
  public BlockingLdapPool(final LdapFactory<Ldap> lf)
  {
    super(new LdapPoolConfig(), lf);
  }


  /**
   * Creates a new ldap pool with the supplied ldap config and factory.
   *
   * @param  lpc  ldap pool configuration
   * @param  lf  ldap factory
   */
  public BlockingLdapPool(final LdapPoolConfig lpc, final LdapFactory<Ldap> lf)
  {
    super(lpc, lf);
  }


  /**
   * Returns the block wait time. Default time is 0, which will wait
   * indefinitely.
   *
   * @return  time in milliseconds to wait for available ldap objects
   */
  public long getBlockWaitTime()
  {
    return this.blockWaitTime;
  }


  /**
   * Sets the block wait time. Default time is 0, which will wait indefinitely.
   *
   * @param  time  in milliseconds to wait for available ldap objects
   */
  public void setBlockWaitTime(final long time)
  {
    if (time >= 0) {
      this.blockWaitTime = time;
    }
  }


  /** {@inheritDoc}. */
  public Ldap checkOut()
    throws LdapPoolException
  {
    Ldap l = null;
    boolean create = false;
    if (this.logger.isTraceEnabled()) {
      this.logger.trace(
        "waiting on pool lock for check out " + this.poolLock.getQueueLength());
    }
    this.poolLock.lock();
    try {
      // if an available object exists, use it
      // if no available objects and the pool can grow, attempt to create
      // otherwise the pool is full, block until an object is returned
      if (this.available.size() > 0) {
        try {
          if (this.logger.isTraceEnabled()) {
            this.logger.trace("retrieve available ldap object");
          }
          l = this.retrieveAvailable();
        } catch (NoSuchElementException e) {
          if (this.logger.isErrorEnabled()) {
            this.logger.error("could not remove ldap object from list", e);
          }
          throw new IllegalStateException("Pool is empty", e);
        }
      } else if (this.active.size() < this.poolConfig.getMaxPoolSize()) {
        if (this.logger.isTraceEnabled()) {
          this.logger.trace("pool can grow, attempt to create ldap object");
        }
        create = true;
      } else {
        if (this.logger.isTraceEnabled()) {
          this.logger.trace(
            "pool is full, block until ldap object " +
            "is available");
        }
        l = this.blockAvailable();
      }
    } finally {
      this.poolLock.unlock();
    }

    if (create) {
      // previous block determined a creation should occur
      // block here until create occurs without locking the whole pool
      // if the pool is already maxed or creates are failing,
      // block until an object is available
      this.checkOutLock.lock();
      try {
        boolean b = true;
        this.poolLock.lock();
        try {
          if (
            this.available.size() + this.active.size() ==
              this.poolConfig.getMaxPoolSize()) {
            b = false;
          }
        } finally {
          this.poolLock.unlock();
        }
        if (b) {
          l = this.createActive();
          if (this.logger.isTraceEnabled()) {
            this.logger.trace("created new active ldap object: " + l);
          }
        }
      } finally {
        this.checkOutLock.unlock();
      }
      if (l == null) {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug(
            "create failed, block until ldap object " +
            "is available");
        }
        l = this.blockAvailable();
      }
    }

    if (l != null) {
      this.activateAndValidate(l);
    } else {
      if (this.logger.isErrorEnabled()) {
        this.logger.error("Could not service check out request");
      }
      throw new LdapPoolExhaustedException(
        "Pool is empty and object creation failed");
    }

    return l;
  }


  /**
   * This attempts to retrieve a ldap object from the available queue.
   *
   * @return  ldap object from the pool
   *
   * @throws  NoSuchElementException  if the available queue is empty
   */
  protected Ldap retrieveAvailable()
  {
    Ldap l = null;
    if (this.logger.isTraceEnabled()) {
      this.logger.trace(
        "waiting on pool lock for retrieve available " +
        this.poolLock.getQueueLength());
    }
    this.poolLock.lock();
    try {
      final PooledLdap<Ldap> pl = this.available.remove();
      this.active.add(new PooledLdap<Ldap>(pl.getLdap()));
      l = pl.getLdap();
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("retrieved available ldap object: " + l);
      }
    } finally {
      this.poolLock.unlock();
    }
    return l;
  }


  /**
   * This blocks until a ldap object can be aquired.
   *
   * @return  ldap object from the pool
   *
   * @throws  LdapPoolException  if this method fails
   * @throws  BlockingTimeoutException  if this pool is configured with a block
   * time and it occurs
   * @throws  PoolInterruptedException  if the current thread is interrupted
   */
  protected Ldap blockAvailable()
    throws LdapPoolException
  {
    Ldap l = null;
    if (this.logger.isTraceEnabled()) {
      this.logger.trace(
        "waiting on pool lock for block available " +
        this.poolLock.getQueueLength());
    }
    this.poolLock.lock();
    try {
      while (l == null) {
        if (this.logger.isTraceEnabled()) {
          this.logger.trace("available pool is empty, waiting...");
        }
        if (this.blockWaitTime > 0) {
          if (
            !this.poolNotEmpty.await(
                this.blockWaitTime,
                TimeUnit.MILLISECONDS)) {
            if (this.logger.isDebugEnabled()) {
              this.logger.debug("block time exceeded, throwing exception");
            }
            throw new BlockingTimeoutException("Block time exceeded");
          }
        } else {
          this.poolNotEmpty.await();
        }
        if (this.logger.isTraceEnabled()) {
          this.logger.trace("notified to continue...");
        }
        try {
          l = this.retrieveAvailable();
        } catch (NoSuchElementException e) {
          if (this.logger.isTraceEnabled()) {
            this.logger.trace("notified to continue but pool was empty");
          }
        }
      }
    } catch (InterruptedException e) {
      if (this.logger.isErrorEnabled()) {
        this.logger.error("waiting for available object interrupted", e);
      }
      throw new PoolInterruptedException(
        "Interrupted while waiting for an available object",
        e);
    } finally {
      this.poolLock.unlock();
    }
    return l;
  }


  /** {@inheritDoc}. */
  public void checkIn(final Ldap l)
  {
    final boolean valid = this.validateAndPassivate(l);
    final PooledLdap<Ldap> pl = new PooledLdap<Ldap>(l);
    if (this.logger.isTraceEnabled()) {
      this.logger.trace(
        "waiting on pool lock for check in " + this.poolLock.getQueueLength());
    }
    this.poolLock.lock();
    try {
      if (this.active.remove(pl)) {
        if (valid) {
          this.available.add(pl);
          if (this.logger.isTraceEnabled()) {
            this.logger.trace("returned active ldap object: " + l);
          }
          this.poolNotEmpty.signal();
        }
      } else if (this.available.contains(pl)) {
        if (this.logger.isWarnEnabled()) {
          this.logger.warn("returned available ldap object: " + l);
        }
      } else {
        if (this.logger.isWarnEnabled()) {
          this.logger.warn("attempt to return unknown ldap object: " + l);
        }
      }
    } finally {
      this.poolLock.unlock();
    }
  }
}
