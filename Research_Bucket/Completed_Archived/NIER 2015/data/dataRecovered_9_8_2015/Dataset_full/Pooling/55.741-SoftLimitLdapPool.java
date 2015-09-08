/*
  $Id: SoftLimitLdapPool.java 263 2009-05-27 01:32:26Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 263 $
  Updated: $Date: 2009-05-26 18:32:26 -0700 (Tue, 26 May 2009) $
*/
package edu.vt.middleware.ldap.pool;

import java.util.NoSuchElementException;
import edu.vt.middleware.ldap.Ldap;

/**
 * <code>SoftLimitLdapPool</code> implements a pool of ldap objects that has a
 * set minimum and maximum size. The pool will grow beyond it's maximum size as
 * necessary based on it's current load. Pool size will return to it's minimum
 * based on the configuration of the prune timer. See {@link
 * LdapPoolConfig#setPruneTimerPeriod} and {@link
 * LdapPoolConfig#setExpirationTime}. This implementation should be used when
 * you have some flexibility in the number of ldap connections that can be
 * created to handle spikes in load. See {@link AbstractLdapPool}. Note that
 * this pool will begin blocking if it cannot create new ldap connections.
 *
 * @author  Middleware Services
 * @version  $Revision: 263 $ $Date: 2009-05-26 18:32:26 -0700 (Tue, 26 May 2009) $
 */
public class SoftLimitLdapPool extends BlockingLdapPool
{


  /** Creates a new ldap pool using {@link DefaultLdapFactory}. */
  public SoftLimitLdapPool()
  {
    super(new LdapPoolConfig(), new DefaultLdapFactory());
  }


  /**
   * Creates a new ldap pool with the supplied ldap factory.
   *
   * @param  lf  ldap factory
   */
  public SoftLimitLdapPool(final LdapFactory<Ldap> lf)
  {
    super(new LdapPoolConfig(), lf);
  }


  /**
   * Creates a new ldap pool with the supplied ldap config and factory.
   *
   * @param  lpc  ldap pool configuration
   * @param  lf  ldap factory
   */
  public SoftLimitLdapPool(final LdapPoolConfig lpc, final LdapFactory<Ldap> lf)
  {
    super(lpc, lf);
  }


  /** {@inheritDoc}. */
  public Ldap checkOut()
    throws LdapPoolException
  {
    Ldap l = null;
    if (this.logger.isTraceEnabled()) {
      this.logger.trace(
        "waiting on pool lock for check out " + this.poolLock.getQueueLength());
    }
    this.poolLock.lock();
    try {
      // if an available object exists, use it
      // if no available objects, attempt to create
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
      }
    } finally {
      this.poolLock.unlock();
    }

    if (l == null) {
      // no object was available, create a new one
      l = this.createActive();
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("created new active ldap object: " + l);
      }
      if (l == null) {
        // create failed, block until an object is available
        if (this.logger.isDebugEnabled()) {
          this.logger.debug(
            "created failed, block until an object " +
            "is available");
        }
        l = this.blockAvailable();
      } else {
        if (this.logger.isTraceEnabled()) {
          this.logger.trace("created new active ldap object: " + l);
        }
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
}
/*
  $Id: SoftLimitLdapPool.java 263 2009-05-27 01:32:26Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 263 $
  Updated: $Date: 2009-05-26 18:32:26 -0700 (Tue, 26 May 2009) $
*/
package edu.vt.middleware.ldap.pool;

import java.util.NoSuchElementException;
import edu.vt.middleware.ldap.Ldap;

/**
 * <code>SoftLimitLdapPool</code> implements a pool of ldap objects that has a
 * set minimum and maximum size. The pool will grow beyond it's maximum size as
 * necessary based on it's current load. Pool size will return to it's minimum
 * based on the configuration of the prune timer. See {@link
 * LdapPoolConfig#setPruneTimerPeriod} and {@link
 * LdapPoolConfig#setExpirationTime}. This implementation should be used when
 * you have some flexibility in the number of ldap connections that can be
 * created to handle spikes in load. See {@link AbstractLdapPool}. Note that
 * this pool will begin blocking if it cannot create new ldap connections.
 *
 * @author  Middleware Services
 * @version  $Revision: 263 $ $Date: 2009-05-26 18:32:26 -0700 (Tue, 26 May 2009) $
 */
public class SoftLimitLdapPool extends BlockingLdapPool
{


  /** Creates a new ldap pool using {@link DefaultLdapFactory}. */
  public SoftLimitLdapPool()
  {
    super(new LdapPoolConfig(), new DefaultLdapFactory());
  }


  /**
   * Creates a new ldap pool with the supplied ldap factory.
   *
   * @param  lf  ldap factory
   */
  public SoftLimitLdapPool(final LdapFactory<Ldap> lf)
  {
    super(new LdapPoolConfig(), lf);
  }


  /**
   * Creates a new ldap pool with the supplied ldap config and factory.
   *
   * @param  lpc  ldap pool configuration
   * @param  lf  ldap factory
   */
  public SoftLimitLdapPool(final LdapPoolConfig lpc, final LdapFactory<Ldap> lf)
  {
    super(lpc, lf);
  }


  /** {@inheritDoc}. */
  public Ldap checkOut()
    throws LdapPoolException
  {
    Ldap l = null;
    if (this.logger.isTraceEnabled()) {
      this.logger.trace(
        "waiting on pool lock for check out " + this.poolLock.getQueueLength());
    }
    this.poolLock.lock();
    try {
      // if an available object exists, use it
      // if no available objects, attempt to create
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
      }
    } finally {
      this.poolLock.unlock();
    }

    if (l == null) {
      // no object was available, create a new one
      l = this.createActive();
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("created new active ldap object: " + l);
      }
      if (l == null) {
        // create failed, block until an object is available
        if (this.logger.isDebugEnabled()) {
          this.logger.debug(
            "created failed, block until an object " +
            "is available");
        }
        l = this.blockAvailable();
      } else {
        if (this.logger.isTraceEnabled()) {
          this.logger.trace("created new active ldap object: " + l);
        }
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
}
/*
  $Id: SoftLimitLdapPool.java 263 2009-05-27 01:32:26Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 263 $
  Updated: $Date: 2009-05-26 18:32:26 -0700 (Tue, 26 May 2009) $
*/
package edu.vt.middleware.ldap.pool;

import java.util.NoSuchElementException;
import edu.vt.middleware.ldap.Ldap;

/**
 * <code>SoftLimitLdapPool</code> implements a pool of ldap objects that has a
 * set minimum and maximum size. The pool will grow beyond it's maximum size as
 * necessary based on it's current load. Pool size will return to it's minimum
 * based on the configuration of the prune timer. See {@link
 * LdapPoolConfig#setPruneTimerPeriod} and {@link
 * LdapPoolConfig#setExpirationTime}. This implementation should be used when
 * you have some flexibility in the number of ldap connections that can be
 * created to handle spikes in load. See {@link AbstractLdapPool}. Note that
 * this pool will begin blocking if it cannot create new ldap connections.
 *
 * @author  Middleware Services
 * @version  $Revision: 263 $ $Date: 2009-05-26 18:32:26 -0700 (Tue, 26 May 2009) $
 */
public class SoftLimitLdapPool extends BlockingLdapPool
{


  /** Creates a new ldap pool using {@link DefaultLdapFactory}. */
  public SoftLimitLdapPool()
  {
    super(new LdapPoolConfig(), new DefaultLdapFactory());
  }


  /**
   * Creates a new ldap pool with the supplied ldap factory.
   *
   * @param  lf  ldap factory
   */
  public SoftLimitLdapPool(final LdapFactory<Ldap> lf)
  {
    super(new LdapPoolConfig(), lf);
  }


  /**
   * Creates a new ldap pool with the supplied ldap config and factory.
   *
   * @param  lpc  ldap pool configuration
   * @param  lf  ldap factory
   */
  public SoftLimitLdapPool(final LdapPoolConfig lpc, final LdapFactory<Ldap> lf)
  {
    super(lpc, lf);
  }


  /** {@inheritDoc}. */
  public Ldap checkOut()
    throws LdapPoolException
  {
    Ldap l = null;
    if (this.logger.isTraceEnabled()) {
      this.logger.trace(
        "waiting on pool lock for check out " + this.poolLock.getQueueLength());
    }
    this.poolLock.lock();
    try {
      // if an available object exists, use it
      // if no available objects, attempt to create
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
      }
    } finally {
      this.poolLock.unlock();
    }

    if (l == null) {
      // no object was available, create a new one
      l = this.createActive();
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("created new active ldap object: " + l);
      }
      if (l == null) {
        // create failed, block until an object is available
        if (this.logger.isDebugEnabled()) {
          this.logger.debug(
            "created failed, block until an object " +
            "is available");
        }
        l = this.blockAvailable();
      } else {
        if (this.logger.isTraceEnabled()) {
          this.logger.trace("created new active ldap object: " + l);
        }
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
}
