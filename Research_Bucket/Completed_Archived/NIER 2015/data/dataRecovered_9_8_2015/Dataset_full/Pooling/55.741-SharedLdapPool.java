/*
  $Id: SharedLdapPool.java 149 2009-04-27 20:28:57Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 149 $
  Updated: $Date: 2009-04-27 13:28:57 -0700 (Mon, 27 Apr 2009) $
*/
package edu.vt.middleware.ldap.pool;

import java.util.NoSuchElementException;
import edu.vt.middleware.ldap.Ldap;

/**
 * <code>SharedLdapPool</code> implements a pool of ldap objects that has a set
 * minimum and maximum size. The pool will not grow beyond the maximum size and
 * when the pool is exhausted, requests for new objects will be serviced by
 * objects that are already in use. Since {@link edu.vt.middleware.ldap.Ldap} is
 * a thread safe object this implementation leverages that by sharing ldap
 * objects among requests. See {@link
 * javax.naming.ldap.LdapContext#newInstance(Control[])}. This implementation
 * should be used when you want some control over the maximum number of ldap
 * connections, but can tolerate some new connections under high load. See
 * {@link AbstractLdapPool}.
 *
 * @author  Middleware Services
 * @version  $Revision: 149 $ $Date: 2009-04-27 13:28:57 -0700 (Mon, 27 Apr 2009) $
 */
public class SharedLdapPool extends AbstractLdapPool<Ldap>
{


  /** Creates a new ldap pool using {@link DefaultLdapFactory}. */
  public SharedLdapPool()
  {
    super(new LdapPoolConfig(), new DefaultLdapFactory());
  }


  /**
   * Creates a new ldap pool with the supplied ldap factory.
   *
   * @param  lf  ldap factory
   */
  public SharedLdapPool(final LdapFactory<Ldap> lf)
  {
    super(new LdapPoolConfig(), lf);
  }


  /**
   * Creates a new ldap pool with the supplied ldap config and factory.
   *
   * @param  lpc  ldap pool configuration
   * @param  lf  ldap factory
   */
  public SharedLdapPool(final LdapPoolConfig lpc, final LdapFactory<Ldap> lf)
  {
    super(lpc, lf);
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
      // otherwise the pool is full, return a shared object
      if (this.active.size() < this.available.size()) {
        if (this.logger.isTraceEnabled()) {
          this.logger.trace("retrieve available ldap object");
        }
        l = this.retrieveAvailable();
      } else if (this.active.size() < this.poolConfig.getMaxPoolSize()) {
        if (this.logger.isTraceEnabled()) {
          this.logger.trace("pool can grow, attempt to create ldap object");
        }
        create = true;
      } else {
        if (this.logger.isTraceEnabled()) {
          this.logger.trace(
            "pool is full, " +
            "attempt to retrieve available ldap object");
        }
        l = this.retrieveAvailable();
      }
    } finally {
      this.poolLock.unlock();
    }

    if (create) {
      // previous block determined a creation should occur
      // block here until create occurs without locking the whole pool
      // if the pool is already maxed or creates are failing,
      // return a shared object
      this.checkOutLock.lock();
      try {
        boolean b = true;
        this.poolLock.lock();
        try {
          if (this.available.size() == this.poolConfig.getMaxPoolSize()) {
            b = false;
          }
        } finally {
          this.poolLock.unlock();
        }
        if (b) {
          l = this.createAvailableAndActive();
          if (this.logger.isTraceEnabled()) {
            this.logger.trace(
              "created new available and active ldap object: " + l);
          }
        }
      } finally {
        this.checkOutLock.unlock();
      }
      if (l == null) {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug("create failed, retrieve available ldap object");
        }
        l = this.retrieveAvailable();
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
   * This attempts to retrieve a ldap object from the available queue. This
   * pooling implementation guarantees there is always an object available.
   *
   * @return  ldap object from the pool
   *
   * @throws  IllegalStateException  if an object cannot be removed from the
   * available queue
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
      try {
        final PooledLdap<Ldap> pl = this.available.remove();
        this.active.add(new PooledLdap<Ldap>(pl.getLdap()));
        this.available.add(new PooledLdap<Ldap>(pl.getLdap()));
        l = pl.getLdap();
        if (this.logger.isTraceEnabled()) {
          this.logger.trace("retrieved available ldap object: " + l);
        }
      } catch (NoSuchElementException e) {
        if (this.logger.isErrorEnabled()) {
          this.logger.error("could not remove ldap object from list", e);
        }
        throw new IllegalStateException("Pool is empty", e);
      }
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
        if (this.logger.isDebugEnabled()) {
          this.logger.debug("returned active ldap object: " + l);
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
      if (!valid) {
        this.available.remove(pl);
      }
    } finally {
      this.poolLock.unlock();
    }
  }
}
/*
  $Id: SharedLdapPool.java 149 2009-04-27 20:28:57Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 149 $
  Updated: $Date: 2009-04-27 13:28:57 -0700 (Mon, 27 Apr 2009) $
*/
package edu.vt.middleware.ldap.pool;

import java.util.NoSuchElementException;
import edu.vt.middleware.ldap.Ldap;

/**
 * <code>SharedLdapPool</code> implements a pool of ldap objects that has a set
 * minimum and maximum size. The pool will not grow beyond the maximum size and
 * when the pool is exhausted, requests for new objects will be serviced by
 * objects that are already in use. Since {@link edu.vt.middleware.ldap.Ldap} is
 * a thread safe object this implementation leverages that by sharing ldap
 * objects among requests. See {@link
 * javax.naming.ldap.LdapContext#newInstance(Control[])}. This implementation
 * should be used when you want some control over the maximum number of ldap
 * connections, but can tolerate some new connections under high load. See
 * {@link AbstractLdapPool}.
 *
 * @author  Middleware Services
 * @version  $Revision: 149 $ $Date: 2009-04-27 13:28:57 -0700 (Mon, 27 Apr 2009) $
 */
public class SharedLdapPool extends AbstractLdapPool<Ldap>
{


  /** Creates a new ldap pool using {@link DefaultLdapFactory}. */
  public SharedLdapPool()
  {
    super(new LdapPoolConfig(), new DefaultLdapFactory());
  }


  /**
   * Creates a new ldap pool with the supplied ldap factory.
   *
   * @param  lf  ldap factory
   */
  public SharedLdapPool(final LdapFactory<Ldap> lf)
  {
    super(new LdapPoolConfig(), lf);
  }


  /**
   * Creates a new ldap pool with the supplied ldap config and factory.
   *
   * @param  lpc  ldap pool configuration
   * @param  lf  ldap factory
   */
  public SharedLdapPool(final LdapPoolConfig lpc, final LdapFactory<Ldap> lf)
  {
    super(lpc, lf);
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
      // otherwise the pool is full, return a shared object
      if (this.active.size() < this.available.size()) {
        if (this.logger.isTraceEnabled()) {
          this.logger.trace("retrieve available ldap object");
        }
        l = this.retrieveAvailable();
      } else if (this.active.size() < this.poolConfig.getMaxPoolSize()) {
        if (this.logger.isTraceEnabled()) {
          this.logger.trace("pool can grow, attempt to create ldap object");
        }
        create = true;
      } else {
        if (this.logger.isTraceEnabled()) {
          this.logger.trace(
            "pool is full, " +
            "attempt to retrieve available ldap object");
        }
        l = this.retrieveAvailable();
      }
    } finally {
      this.poolLock.unlock();
    }

    if (create) {
      // previous block determined a creation should occur
      // block here until create occurs without locking the whole pool
      // if the pool is already maxed or creates are failing,
      // return a shared object
      this.checkOutLock.lock();
      try {
        boolean b = true;
        this.poolLock.lock();
        try {
          if (this.available.size() == this.poolConfig.getMaxPoolSize()) {
            b = false;
          }
        } finally {
          this.poolLock.unlock();
        }
        if (b) {
          l = this.createAvailableAndActive();
          if (this.logger.isTraceEnabled()) {
            this.logger.trace(
              "created new available and active ldap object: " + l);
          }
        }
      } finally {
        this.checkOutLock.unlock();
      }
      if (l == null) {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug("create failed, retrieve available ldap object");
        }
        l = this.retrieveAvailable();
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
   * This attempts to retrieve a ldap object from the available queue. This
   * pooling implementation guarantees there is always an object available.
   *
   * @return  ldap object from the pool
   *
   * @throws  IllegalStateException  if an object cannot be removed from the
   * available queue
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
      try {
        final PooledLdap<Ldap> pl = this.available.remove();
        this.active.add(new PooledLdap<Ldap>(pl.getLdap()));
        this.available.add(new PooledLdap<Ldap>(pl.getLdap()));
        l = pl.getLdap();
        if (this.logger.isTraceEnabled()) {
          this.logger.trace("retrieved available ldap object: " + l);
        }
      } catch (NoSuchElementException e) {
        if (this.logger.isErrorEnabled()) {
          this.logger.error("could not remove ldap object from list", e);
        }
        throw new IllegalStateException("Pool is empty", e);
      }
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
        if (this.logger.isDebugEnabled()) {
          this.logger.debug("returned active ldap object: " + l);
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
      if (!valid) {
        this.available.remove(pl);
      }
    } finally {
      this.poolLock.unlock();
    }
  }
}
/*
  $Id: SharedLdapPool.java 149 2009-04-27 20:28:57Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 149 $
  Updated: $Date: 2009-04-27 13:28:57 -0700 (Mon, 27 Apr 2009) $
*/
package edu.vt.middleware.ldap.pool;

import java.util.NoSuchElementException;
import edu.vt.middleware.ldap.Ldap;

/**
 * <code>SharedLdapPool</code> implements a pool of ldap objects that has a set
 * minimum and maximum size. The pool will not grow beyond the maximum size and
 * when the pool is exhausted, requests for new objects will be serviced by
 * objects that are already in use. Since {@link edu.vt.middleware.ldap.Ldap} is
 * a thread safe object this implementation leverages that by sharing ldap
 * objects among requests. See {@link
 * javax.naming.ldap.LdapContext#newInstance(Control[])}. This implementation
 * should be used when you want some control over the maximum number of ldap
 * connections, but can tolerate some new connections under high load. See
 * {@link AbstractLdapPool}.
 *
 * @author  Middleware Services
 * @version  $Revision: 149 $ $Date: 2009-04-27 13:28:57 -0700 (Mon, 27 Apr 2009) $
 */
public class SharedLdapPool extends AbstractLdapPool<Ldap>
{


  /** Creates a new ldap pool using {@link DefaultLdapFactory}. */
  public SharedLdapPool()
  {
    super(new LdapPoolConfig(), new DefaultLdapFactory());
  }


  /**
   * Creates a new ldap pool with the supplied ldap factory.
   *
   * @param  lf  ldap factory
   */
  public SharedLdapPool(final LdapFactory<Ldap> lf)
  {
    super(new LdapPoolConfig(), lf);
  }


  /**
   * Creates a new ldap pool with the supplied ldap config and factory.
   *
   * @param  lpc  ldap pool configuration
   * @param  lf  ldap factory
   */
  public SharedLdapPool(final LdapPoolConfig lpc, final LdapFactory<Ldap> lf)
  {
    super(lpc, lf);
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
      // otherwise the pool is full, return a shared object
      if (this.active.size() < this.available.size()) {
        if (this.logger.isTraceEnabled()) {
          this.logger.trace("retrieve available ldap object");
        }
        l = this.retrieveAvailable();
      } else if (this.active.size() < this.poolConfig.getMaxPoolSize()) {
        if (this.logger.isTraceEnabled()) {
          this.logger.trace("pool can grow, attempt to create ldap object");
        }
        create = true;
      } else {
        if (this.logger.isTraceEnabled()) {
          this.logger.trace(
            "pool is full, " +
            "attempt to retrieve available ldap object");
        }
        l = this.retrieveAvailable();
      }
    } finally {
      this.poolLock.unlock();
    }

    if (create) {
      // previous block determined a creation should occur
      // block here until create occurs without locking the whole pool
      // if the pool is already maxed or creates are failing,
      // return a shared object
      this.checkOutLock.lock();
      try {
        boolean b = true;
        this.poolLock.lock();
        try {
          if (this.available.size() == this.poolConfig.getMaxPoolSize()) {
            b = false;
          }
        } finally {
          this.poolLock.unlock();
        }
        if (b) {
          l = this.createAvailableAndActive();
          if (this.logger.isTraceEnabled()) {
            this.logger.trace(
              "created new available and active ldap object: " + l);
          }
        }
      } finally {
        this.checkOutLock.unlock();
      }
      if (l == null) {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug("create failed, retrieve available ldap object");
        }
        l = this.retrieveAvailable();
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
   * This attempts to retrieve a ldap object from the available queue. This
   * pooling implementation guarantees there is always an object available.
   *
   * @return  ldap object from the pool
   *
   * @throws  IllegalStateException  if an object cannot be removed from the
   * available queue
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
      try {
        final PooledLdap<Ldap> pl = this.available.remove();
        this.active.add(new PooledLdap<Ldap>(pl.getLdap()));
        this.available.add(new PooledLdap<Ldap>(pl.getLdap()));
        l = pl.getLdap();
        if (this.logger.isTraceEnabled()) {
          this.logger.trace("retrieved available ldap object: " + l);
        }
      } catch (NoSuchElementException e) {
        if (this.logger.isErrorEnabled()) {
          this.logger.error("could not remove ldap object from list", e);
        }
        throw new IllegalStateException("Pool is empty", e);
      }
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
        if (this.logger.isDebugEnabled()) {
          this.logger.debug("returned active ldap object: " + l);
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
      if (!valid) {
        this.available.remove(pl);
      }
    } finally {
      this.poolLock.unlock();
    }
  }
}
