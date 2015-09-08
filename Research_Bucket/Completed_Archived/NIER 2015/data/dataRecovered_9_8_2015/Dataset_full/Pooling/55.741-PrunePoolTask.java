/*
  $Id: PrunePoolTask.java 183 2009-05-06 02:45:57Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 183 $
  Updated: $Date: 2009-05-05 19:45:57 -0700 (Tue, 05 May 2009) $
*/
package edu.vt.middleware.ldap.pool;

import java.util.TimerTask;
import edu.vt.middleware.ldap.BaseLdap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <code>PrunePoolTask</code> is a periodic task that removes available ldap
 * objects from the pool if the objects have been in the pool longer than a
 * configured expiration time and the pool size is above it's configured
 * minimum. Task will skip execution if the pool has any active objects.
 *
 * @param  <T>  type of ldap object
 *
 * @author  Middleware Services
 * @version  $Revision: 183 $ $Date: 2009-05-05 19:45:57 -0700 (Tue, 05 May 2009) $
 */
public class PrunePoolTask<T extends BaseLdap> extends TimerTask
{

  /** Log for this class. */
  protected final Log logger = LogFactory.getLog(this.getClass());

  /** Pool to clean. */
  private LdapPool<T> pool;


  /**
   * Creates a new task to periodically prune the supplied pool.
   *
   * @param  lp  ldap pool to periodically inspect
   */
  public PrunePoolTask(final LdapPool<T> lp)
  {
    this.pool = lp;
  }


  /**
   * This attempts to remove idle objects from a pool. See {@link
   * LdapPool#prune()}.
   */
  public void run()
  {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("Begin prune task for " + this.pool);
    }
    this.pool.prune();
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("End prune task for " + this.pool);
    }
  }
}
/*
  $Id: PrunePoolTask.java 183 2009-05-06 02:45:57Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 183 $
  Updated: $Date: 2009-05-05 19:45:57 -0700 (Tue, 05 May 2009) $
*/
package edu.vt.middleware.ldap.pool;

import java.util.TimerTask;
import edu.vt.middleware.ldap.BaseLdap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <code>PrunePoolTask</code> is a periodic task that removes available ldap
 * objects from the pool if the objects have been in the pool longer than a
 * configured expiration time and the pool size is above it's configured
 * minimum. Task will skip execution if the pool has any active objects.
 *
 * @param  <T>  type of ldap object
 *
 * @author  Middleware Services
 * @version  $Revision: 183 $ $Date: 2009-05-05 19:45:57 -0700 (Tue, 05 May 2009) $
 */
public class PrunePoolTask<T extends BaseLdap> extends TimerTask
{

  /** Log for this class. */
  protected final Log logger = LogFactory.getLog(this.getClass());

  /** Pool to clean. */
  private LdapPool<T> pool;


  /**
   * Creates a new task to periodically prune the supplied pool.
   *
   * @param  lp  ldap pool to periodically inspect
   */
  public PrunePoolTask(final LdapPool<T> lp)
  {
    this.pool = lp;
  }


  /**
   * This attempts to remove idle objects from a pool. See {@link
   * LdapPool#prune()}.
   */
  public void run()
  {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("Begin prune task for " + this.pool);
    }
    this.pool.prune();
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("End prune task for " + this.pool);
    }
  }
}
/*
  $Id: PrunePoolTask.java 183 2009-05-06 02:45:57Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 183 $
  Updated: $Date: 2009-05-05 19:45:57 -0700 (Tue, 05 May 2009) $
*/
package edu.vt.middleware.ldap.pool;

import java.util.TimerTask;
import edu.vt.middleware.ldap.BaseLdap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <code>PrunePoolTask</code> is a periodic task that removes available ldap
 * objects from the pool if the objects have been in the pool longer than a
 * configured expiration time and the pool size is above it's configured
 * minimum. Task will skip execution if the pool has any active objects.
 *
 * @param  <T>  type of ldap object
 *
 * @author  Middleware Services
 * @version  $Revision: 183 $ $Date: 2009-05-05 19:45:57 -0700 (Tue, 05 May 2009) $
 */
public class PrunePoolTask<T extends BaseLdap> extends TimerTask
{

  /** Log for this class. */
  protected final Log logger = LogFactory.getLog(this.getClass());

  /** Pool to clean. */
  private LdapPool<T> pool;


  /**
   * Creates a new task to periodically prune the supplied pool.
   *
   * @param  lp  ldap pool to periodically inspect
   */
  public PrunePoolTask(final LdapPool<T> lp)
  {
    this.pool = lp;
  }


  /**
   * This attempts to remove idle objects from a pool. See {@link
   * LdapPool#prune()}.
   */
  public void run()
  {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("Begin prune task for " + this.pool);
    }
    this.pool.prune();
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("End prune task for " + this.pool);
    }
  }
}
