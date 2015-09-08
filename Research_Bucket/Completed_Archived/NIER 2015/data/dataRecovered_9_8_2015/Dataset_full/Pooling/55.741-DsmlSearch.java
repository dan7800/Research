/*
  $Id: DsmlSearch.java 269 2009-05-28 14:24:37Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 269 $
  Updated: $Date: 2009-05-28 07:24:37 -0700 (Thu, 28 May 2009) $
*/
package edu.vt.middleware.ldap.dsml;

import java.io.IOException;
import java.io.OutputStream;
import javax.naming.NamingException;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.LdapSearch;
import edu.vt.middleware.ldap.pool.LdapPool;

/**
 * <code>DsmlSearch</code> queries an LDAP and returns the result as DSML. Each
 * instance of <code>DsmlSearch</code> maintains it's own pool of LDAP
 * connections.
 *
 * @author  Middleware Services
 * @version  $Revision: 269 $ $Date: 2009-05-28 07:24:37 -0700 (Thu, 28 May 2009) $
 */

public class DsmlSearch extends LdapSearch
{

  /** Valid DSML versions. */
  public enum Version {

    /** DSML version 1. */
    ONE,

    /** DSML version 2. */
    TWO
  }

  /** Version of DSML to produce, default is 1. */
  private Version version = Version.ONE;

  /** Dsml version 1 object. */
  private Dsmlv1 dsmlv1 = new Dsmlv1();

  /** Dsml version 2 object. */
  private Dsmlv2 dsmlv2 = new Dsmlv2();


  /**
   * This creates a new <code>DsmlSearch</code> with the supplied pool.
   *
   * @param  pool  <code>LdapPool</code>
   */
  public DsmlSearch(final LdapPool<Ldap> pool)
  {
    super(pool);
  }


  /**
   * This gets the version of dsml to produce.
   *
   * @return  <code>Version</code> of DSML to produce
   */
  public Version getVersion()
  {
    return this.version;
  }


  /**
   * This sets the version of dsml to produce.
   *
   * @param  v  <code>Version</code> of DSML to produce
   */
  public void setVersion(final Version v)
  {
    this.version = v;
  }


  /**
   * This will perform a LDAP search with the supplied query and return
   * attributes. The results will be written to the supplied <code>
   * OutputStream</code>. The supplied version should be either '1' or '2',
   * represeting the version of DSML you want returned.
   *
   * @param  query  <code>String</code> to search for
   * @param  attrs  <code>String[]</code> to return
   * @param  out  <code>OutputStream</code> to write to
   *
   * @throws  NamingException  if an error occurs while searching
   * @throws  IOException  if an error occurs while writing search results
   */
  public void searchToStream(
    final String query,
    final String[] attrs,
    final OutputStream out)
    throws NamingException, IOException
  {
    if (this.version == Version.TWO) {
      this.dsmlv2.outputDsml(this.search(query, attrs), out);
    } else {
      this.dsmlv1.outputDsml(this.search(query, attrs), out);
    }
  }


  /**
   * This will perform a LDAP search with the supplied query and return
   * attributes.
   *
   * @param  query  <code>String</code> to search for
   * @param  attrs  <code>String[]</code> to return
   *
   * @return  <code>String</code> of DSML results
   *
   * @throws  NamingException  if an error occurs while searching
   * @throws  IOException  if an error occurs while writing search results
   */
  public String searchToString(final String query, final String[] attrs)
    throws NamingException, IOException
  {
    if (this.version == Version.TWO) {
      return this.dsmlv2.outputDsmlToString(this.search(query, attrs));
    } else {
      return this.dsmlv1.outputDsmlToString(this.search(query, attrs));
    }
  }
}
/*
  $Id: DsmlSearch.java 269 2009-05-28 14:24:37Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 269 $
  Updated: $Date: 2009-05-28 07:24:37 -0700 (Thu, 28 May 2009) $
*/
package edu.vt.middleware.ldap.dsml;

import java.io.IOException;
import java.io.OutputStream;
import javax.naming.NamingException;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.LdapSearch;
import edu.vt.middleware.ldap.pool.LdapPool;

/**
 * <code>DsmlSearch</code> queries an LDAP and returns the result as DSML. Each
 * instance of <code>DsmlSearch</code> maintains it's own pool of LDAP
 * connections.
 *
 * @author  Middleware Services
 * @version  $Revision: 269 $ $Date: 2009-05-28 07:24:37 -0700 (Thu, 28 May 2009) $
 */

public class DsmlSearch extends LdapSearch
{

  /** Valid DSML versions. */
  public enum Version {

    /** DSML version 1. */
    ONE,

    /** DSML version 2. */
    TWO
  }

  /** Version of DSML to produce, default is 1. */
  private Version version = Version.ONE;

  /** Dsml version 1 object. */
  private Dsmlv1 dsmlv1 = new Dsmlv1();

  /** Dsml version 2 object. */
  private Dsmlv2 dsmlv2 = new Dsmlv2();


  /**
   * This creates a new <code>DsmlSearch</code> with the supplied pool.
   *
   * @param  pool  <code>LdapPool</code>
   */
  public DsmlSearch(final LdapPool<Ldap> pool)
  {
    super(pool);
  }


  /**
   * This gets the version of dsml to produce.
   *
   * @return  <code>Version</code> of DSML to produce
   */
  public Version getVersion()
  {
    return this.version;
  }


  /**
   * This sets the version of dsml to produce.
   *
   * @param  v  <code>Version</code> of DSML to produce
   */
  public void setVersion(final Version v)
  {
    this.version = v;
  }


  /**
   * This will perform a LDAP search with the supplied query and return
   * attributes. The results will be written to the supplied <code>
   * OutputStream</code>. The supplied version should be either '1' or '2',
   * represeting the version of DSML you want returned.
   *
   * @param  query  <code>String</code> to search for
   * @param  attrs  <code>String[]</code> to return
   * @param  out  <code>OutputStream</code> to write to
   *
   * @throws  NamingException  if an error occurs while searching
   * @throws  IOException  if an error occurs while writing search results
   */
  public void searchToStream(
    final String query,
    final String[] attrs,
    final OutputStream out)
    throws NamingException, IOException
  {
    if (this.version == Version.TWO) {
      this.dsmlv2.outputDsml(this.search(query, attrs), out);
    } else {
      this.dsmlv1.outputDsml(this.search(query, attrs), out);
    }
  }


  /**
   * This will perform a LDAP search with the supplied query and return
   * attributes.
   *
   * @param  query  <code>String</code> to search for
   * @param  attrs  <code>String[]</code> to return
   *
   * @return  <code>String</code> of DSML results
   *
   * @throws  NamingException  if an error occurs while searching
   * @throws  IOException  if an error occurs while writing search results
   */
  public String searchToString(final String query, final String[] attrs)
    throws NamingException, IOException
  {
    if (this.version == Version.TWO) {
      return this.dsmlv2.outputDsmlToString(this.search(query, attrs));
    } else {
      return this.dsmlv1.outputDsmlToString(this.search(query, attrs));
    }
  }
}
/*
  $Id: DsmlSearch.java 269 2009-05-28 14:24:37Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 269 $
  Updated: $Date: 2009-05-28 07:24:37 -0700 (Thu, 28 May 2009) $
*/
package edu.vt.middleware.ldap.dsml;

import java.io.IOException;
import java.io.OutputStream;
import javax.naming.NamingException;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.LdapSearch;
import edu.vt.middleware.ldap.pool.LdapPool;

/**
 * <code>DsmlSearch</code> queries an LDAP and returns the result as DSML. Each
 * instance of <code>DsmlSearch</code> maintains it's own pool of LDAP
 * connections.
 *
 * @author  Middleware Services
 * @version  $Revision: 269 $ $Date: 2009-05-28 07:24:37 -0700 (Thu, 28 May 2009) $
 */

public class DsmlSearch extends LdapSearch
{

  /** Valid DSML versions. */
  public enum Version {

    /** DSML version 1. */
    ONE,

    /** DSML version 2. */
    TWO
  }

  /** Version of DSML to produce, default is 1. */
  private Version version = Version.ONE;

  /** Dsml version 1 object. */
  private Dsmlv1 dsmlv1 = new Dsmlv1();

  /** Dsml version 2 object. */
  private Dsmlv2 dsmlv2 = new Dsmlv2();


  /**
   * This creates a new <code>DsmlSearch</code> with the supplied pool.
   *
   * @param  pool  <code>LdapPool</code>
   */
  public DsmlSearch(final LdapPool<Ldap> pool)
  {
    super(pool);
  }


  /**
   * This gets the version of dsml to produce.
   *
   * @return  <code>Version</code> of DSML to produce
   */
  public Version getVersion()
  {
    return this.version;
  }


  /**
   * This sets the version of dsml to produce.
   *
   * @param  v  <code>Version</code> of DSML to produce
   */
  public void setVersion(final Version v)
  {
    this.version = v;
  }


  /**
   * This will perform a LDAP search with the supplied query and return
   * attributes. The results will be written to the supplied <code>
   * OutputStream</code>. The supplied version should be either '1' or '2',
   * represeting the version of DSML you want returned.
   *
   * @param  query  <code>String</code> to search for
   * @param  attrs  <code>String[]</code> to return
   * @param  out  <code>OutputStream</code> to write to
   *
   * @throws  NamingException  if an error occurs while searching
   * @throws  IOException  if an error occurs while writing search results
   */
  public void searchToStream(
    final String query,
    final String[] attrs,
    final OutputStream out)
    throws NamingException, IOException
  {
    if (this.version == Version.TWO) {
      this.dsmlv2.outputDsml(this.search(query, attrs), out);
    } else {
      this.dsmlv1.outputDsml(this.search(query, attrs), out);
    }
  }


  /**
   * This will perform a LDAP search with the supplied query and return
   * attributes.
   *
   * @param  query  <code>String</code> to search for
   * @param  attrs  <code>String[]</code> to return
   *
   * @return  <code>String</code> of DSML results
   *
   * @throws  NamingException  if an error occurs while searching
   * @throws  IOException  if an error occurs while writing search results
   */
  public String searchToString(final String query, final String[] attrs)
    throws NamingException, IOException
  {
    if (this.version == Version.TWO) {
      return this.dsmlv2.outputDsmlToString(this.search(query, attrs));
    } else {
      return this.dsmlv1.outputDsmlToString(this.search(query, attrs));
    }
  }
}
