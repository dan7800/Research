/*
  $Id: LdapFactory.java 149 2009-04-27 20:28:57Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 149 $
  Updated: $Date: 2009-04-27 13:28:57 -0700 (Mon, 27 Apr 2009) $
*/
package edu.vt.middleware.ldap.pool;

import edu.vt.middleware.ldap.BaseLdap;

/**
 * <code>LdapFactory</code> provides an interface for creating, activating,
 * validating, and destroying ldap objects.
 *
 * @param  <T>  type of ldap object
 *
 * @author  Middleware Services
 * @version  $Revision: 149 $ $Date: 2009-04-27 13:28:57 -0700 (Mon, 27 Apr 2009) $
 */
public interface LdapFactory<T extends BaseLdap>
{


  /**
   * Create a new ldap object.
   *
   * @return  ldap object
   */
  T create();


  /**
   * Destroy a ldap object.
   *
   * @param  t  ldap object
   */
  void destroy(T t);


  /**
   * Prepare the supplied object for placement in the pool.
   *
   * @param  t  ldap object
   *
   * @return  whether the supplied object successfully activated
   */
  boolean activate(T t);


  /**
   * Prepare the supplied object for removal from the pool.
   *
   * @param  t  ldap object
   *
   * @return  whether the supplied object successfully passivated
   */
  boolean passivate(T t);


  /**
   * Verify a ldap object is still viable for use in the pool.
   *
   * @param  t  ldap object
   *
   * @return  whether the supplied object is ready for use
   */
  boolean validate(T t);
}
/*
  $Id: LdapFactory.java 149 2009-04-27 20:28:57Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 149 $
  Updated: $Date: 2009-04-27 13:28:57 -0700 (Mon, 27 Apr 2009) $
*/
package edu.vt.middleware.ldap.pool;

import edu.vt.middleware.ldap.BaseLdap;

/**
 * <code>LdapFactory</code> provides an interface for creating, activating,
 * validating, and destroying ldap objects.
 *
 * @param  <T>  type of ldap object
 *
 * @author  Middleware Services
 * @version  $Revision: 149 $ $Date: 2009-04-27 13:28:57 -0700 (Mon, 27 Apr 2009) $
 */
public interface LdapFactory<T extends BaseLdap>
{


  /**
   * Create a new ldap object.
   *
   * @return  ldap object
   */
  T create();


  /**
   * Destroy a ldap object.
   *
   * @param  t  ldap object
   */
  void destroy(T t);


  /**
   * Prepare the supplied object for placement in the pool.
   *
   * @param  t  ldap object
   *
   * @return  whether the supplied object successfully activated
   */
  boolean activate(T t);


  /**
   * Prepare the supplied object for removal from the pool.
   *
   * @param  t  ldap object
   *
   * @return  whether the supplied object successfully passivated
   */
  boolean passivate(T t);


  /**
   * Verify a ldap object is still viable for use in the pool.
   *
   * @param  t  ldap object
   *
   * @return  whether the supplied object is ready for use
   */
  boolean validate(T t);
}
/*
  $Id: LdapFactory.java 149 2009-04-27 20:28:57Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 149 $
  Updated: $Date: 2009-04-27 13:28:57 -0700 (Mon, 27 Apr 2009) $
*/
package edu.vt.middleware.ldap.pool;

import edu.vt.middleware.ldap.BaseLdap;

/**
 * <code>LdapFactory</code> provides an interface for creating, activating,
 * validating, and destroying ldap objects.
 *
 * @param  <T>  type of ldap object
 *
 * @author  Middleware Services
 * @version  $Revision: 149 $ $Date: 2009-04-27 13:28:57 -0700 (Mon, 27 Apr 2009) $
 */
public interface LdapFactory<T extends BaseLdap>
{


  /**
   * Create a new ldap object.
   *
   * @return  ldap object
   */
  T create();


  /**
   * Destroy a ldap object.
   *
   * @param  t  ldap object
   */
  void destroy(T t);


  /**
   * Prepare the supplied object for placement in the pool.
   *
   * @param  t  ldap object
   *
   * @return  whether the supplied object successfully activated
   */
  boolean activate(T t);


  /**
   * Prepare the supplied object for removal from the pool.
   *
   * @param  t  ldap object
   *
   * @return  whether the supplied object successfully passivated
   */
  boolean passivate(T t);


  /**
   * Verify a ldap object is still viable for use in the pool.
   *
   * @param  t  ldap object
   *
   * @return  whether the supplied object is ready for use
   */
  boolean validate(T t);
}
