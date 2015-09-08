/*
  $Id: LdapPassivator.java 149 2009-04-27 20:28:57Z dfisher $

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
 * <code>LdapPasivator</code> provides an interface for passivating ldap objects
 * when they are checked back into the pool.
 *
 * @param  <T>  type of ldap object
 *
 * @author  Middleware Services
 * @version  $Revision: 149 $ $Date: 2009-04-27 13:28:57 -0700 (Mon, 27 Apr 2009) $
 */
public interface LdapPassivator<T extends BaseLdap>
{


  /**
   * Passivate the supplied ldap object.
   *
   * @param  t  ldap object
   *
   * @return  whether passivation was successful
   */
  boolean passivate(T t);
}
/*
  $Id: LdapPassivator.java 149 2009-04-27 20:28:57Z dfisher $

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
 * <code>LdapPasivator</code> provides an interface for passivating ldap objects
 * when they are checked back into the pool.
 *
 * @param  <T>  type of ldap object
 *
 * @author  Middleware Services
 * @version  $Revision: 149 $ $Date: 2009-04-27 13:28:57 -0700 (Mon, 27 Apr 2009) $
 */
public interface LdapPassivator<T extends BaseLdap>
{


  /**
   * Passivate the supplied ldap object.
   *
   * @param  t  ldap object
   *
   * @return  whether passivation was successful
   */
  boolean passivate(T t);
}
/*
  $Id: LdapPassivator.java 149 2009-04-27 20:28:57Z dfisher $

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
 * <code>LdapPasivator</code> provides an interface for passivating ldap objects
 * when they are checked back into the pool.
 *
 * @param  <T>  type of ldap object
 *
 * @author  Middleware Services
 * @version  $Revision: 149 $ $Date: 2009-04-27 13:28:57 -0700 (Mon, 27 Apr 2009) $
 */
public interface LdapPassivator<T extends BaseLdap>
{


  /**
   * Passivate the supplied ldap object.
   *
   * @param  t  ldap object
   *
   * @return  whether passivation was successful
   */
  boolean passivate(T t);
}
