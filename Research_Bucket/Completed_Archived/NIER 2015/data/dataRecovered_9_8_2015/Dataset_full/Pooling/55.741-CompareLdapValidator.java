/*
  $Id: CompareLdapValidator.java 149 2009-04-27 20:28:57Z dfisher $

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
 * <code>CompareLdapValidator</code> validates a ldap connection is healthy by
 * performing a compare operation.
 *
 * @author  Middleware Services
 * @version  $Revision: 149 $ $Date: 2009-04-27 13:28:57 -0700 (Mon, 27 Apr 2009) $
 */
public class CompareLdapValidator implements LdapValidator<Ldap>
{

  /** Log for this class. */
  protected final Log logger = LogFactory.getLog(this.getClass());

  /** DN for validating connections. */
  private String validateDn;

  /** Filter for validating connections. */
  private String validateFilter;


  /**
   * Creates a new <code>CompareLdapValiadtor</code> with the supplied compare
   * dn and filter.
   *
   * @param  dn  to use for compares
   * @param  filter  to use for compares
   */
  public CompareLdapValidator(final String dn, final String filter)
  {
    this.validateDn = dn;
    this.validateFilter = filter;
  }


  /** {@inheritDoc}. */
  public boolean validate(final Ldap l)
  {
    boolean success = false;
    if (l != null) {
      try {
        success = l.compare(this.validateDn, this.validateFilter);
      } catch (NamingException e) {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug(
            "validation failed for compare " + this.validateFilter,
            e);
        }
      }
    }
    return success;
  }
}
/*
  $Id: CompareLdapValidator.java 639 2009-09-18 17:55:42Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 639 $
  Updated: $Date: 2009-09-18 10:55:42 -0700 (Fri, 18 Sep 2009) $
*/
package edu.vt.middleware.ldap.pool;

import javax.naming.NamingException;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.SearchFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <code>CompareLdapValidator</code> validates a ldap connection is healthy by
 * performing a compare operation.
 *
 * @author  Middleware Services
 * @version  $Revision: 639 $ $Date: 2009-09-18 10:55:42 -0700 (Fri, 18 Sep 2009) $
 */
public class CompareLdapValidator implements LdapValidator<Ldap>
{

  /** Log for this class. */
  protected final Log logger = LogFactory.getLog(this.getClass());

  /** DN for validating connections. */
  private String validateDn;

  /** Filter for validating connections. */
  private SearchFilter validateFilter;


  /**
   * Creates a new <code>CompareLdapValiadtor</code> with the supplied compare
   * dn and filter.
   *
   * @param  dn  to use for compares
   * @param  filter  to use for compares
   */
  public CompareLdapValidator(final String dn, final SearchFilter filter)
  {
    this.validateDn = dn;
    this.validateFilter = filter;
  }


  /** {@inheritDoc}. */
  public boolean validate(final Ldap l)
  {
    boolean success = false;
    if (l != null) {
      try {
        success = l.compare(this.validateDn, this.validateFilter);
      } catch (NamingException e) {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug(
            "validation failed for compare " + this.validateFilter,
            e);
        }
      }
    }
    return success;
  }
}
/*
  $Id: CompareLdapValidator.java 149 2009-04-27 20:28:57Z dfisher $

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
 * <code>CompareLdapValidator</code> validates a ldap connection is healthy by
 * performing a compare operation.
 *
 * @author  Middleware Services
 * @version  $Revision: 149 $ $Date: 2009-04-27 13:28:57 -0700 (Mon, 27 Apr 2009) $
 */
public class CompareLdapValidator implements LdapValidator<Ldap>
{

  /** Log for this class. */
  protected final Log logger = LogFactory.getLog(this.getClass());

  /** DN for validating connections. */
  private String validateDn;

  /** Filter for validating connections. */
  private String validateFilter;


  /**
   * Creates a new <code>CompareLdapValiadtor</code> with the supplied compare
   * dn and filter.
   *
   * @param  dn  to use for compares
   * @param  filter  to use for compares
   */
  public CompareLdapValidator(final String dn, final String filter)
  {
    this.validateDn = dn;
    this.validateFilter = filter;
  }


  /** {@inheritDoc}. */
  public boolean validate(final Ldap l)
  {
    boolean success = false;
    if (l != null) {
      try {
        success = l.compare(this.validateDn, this.validateFilter);
      } catch (NamingException e) {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug(
            "validation failed for compare " + this.validateFilter,
            e);
        }
      }
    }
    return success;
  }
}
