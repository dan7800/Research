/*
  $Id: ConnectLdapValidator.java 149 2009-04-27 20:28:57Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 149 $
  Updated: $Date: 2009-04-27 13:28:57 -0700 (Mon, 27 Apr 2009) $
*/
package edu.vt.middleware.ldap.pool;

import javax.naming.NamingException;
import edu.vt.middleware.ldap.Ldap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <code>ConnectLdapValidator</code> validates a ldap connection is healthy by
 * testing it is connected.
 *
 * @author  Middleware Services
 * @version  $Revision: 149 $ $Date: 2009-04-27 13:28:57 -0700 (Mon, 27 Apr 2009) $
 */
public class ConnectLdapValidator implements LdapValidator<Ldap>
{

  /** Log for this class. */
  protected final Log logger = LogFactory.getLog(this.getClass());


  /** {@inheritDoc}. */
  public boolean validate(final Ldap l)
  {
    boolean success = false;
    if (l != null) {
      try {
        success = l.connect();
      } catch (NamingException e) {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug("validation failed for " + l, e);
        }
      }
    }
    return success;
  }
}
/*
  $Id: ConnectLdapValidator.java 149 2009-04-27 20:28:57Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 149 $
  Updated: $Date: 2009-04-27 13:28:57 -0700 (Mon, 27 Apr 2009) $
*/
package edu.vt.middleware.ldap.pool;

import javax.naming.NamingException;
import edu.vt.middleware.ldap.Ldap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <code>ConnectLdapValidator</code> validates a ldap connection is healthy by
 * testing it is connected.
 *
 * @author  Middleware Services
 * @version  $Revision: 149 $ $Date: 2009-04-27 13:28:57 -0700 (Mon, 27 Apr 2009) $
 */
public class ConnectLdapValidator implements LdapValidator<Ldap>
{

  /** Log for this class. */
  protected final Log logger = LogFactory.getLog(this.getClass());


  /** {@inheritDoc}. */
  public boolean validate(final Ldap l)
  {
    boolean success = false;
    if (l != null) {
      try {
        success = l.connect();
      } catch (NamingException e) {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug("validation failed for " + l, e);
        }
      }
    }
    return success;
  }
}
/*
  $Id: ConnectLdapValidator.java 149 2009-04-27 20:28:57Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 149 $
  Updated: $Date: 2009-04-27 13:28:57 -0700 (Mon, 27 Apr 2009) $
*/
package edu.vt.middleware.ldap.pool;

import javax.naming.NamingException;
import edu.vt.middleware.ldap.Ldap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <code>ConnectLdapValidator</code> validates a ldap connection is healthy by
 * testing it is connected.
 *
 * @author  Middleware Services
 * @version  $Revision: 149 $ $Date: 2009-04-27 13:28:57 -0700 (Mon, 27 Apr 2009) $
 */
public class ConnectLdapValidator implements LdapValidator<Ldap>
{

  /** Log for this class. */
  protected final Log logger = LogFactory.getLog(this.getClass());


  /** {@inheritDoc}. */
  public boolean validate(final Ldap l)
  {
    boolean success = false;
    if (l != null) {
      try {
        success = l.connect();
      } catch (NamingException e) {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug("validation failed for " + l, e);
        }
      }
    }
    return success;
  }
}
