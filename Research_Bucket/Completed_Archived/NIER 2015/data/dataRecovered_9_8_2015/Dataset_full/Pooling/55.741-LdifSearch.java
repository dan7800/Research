/*
  $Id: LdifSearch.java 269 2009-05-28 14:24:37Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 269 $
  Updated: $Date: 2009-05-28 07:24:37 -0700 (Thu, 28 May 2009) $
*/
package edu.vt.middleware.ldap.ldif;

import java.io.IOException;
import java.io.OutputStream;
import javax.naming.NamingException;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.LdapSearch;
import edu.vt.middleware.ldap.pool.LdapPool;

/**
 * <code>LdifSearch</code> queries an LDAP and returns the result as a LDIF.
 * Each instance of <code>LdifSearch</code> maintains it's own pool of LDAP
 * connections.
 *
 * @author  Middleware Services
 * @version  $Revision: 269 $ $Date: 2009-05-28 07:24:37 -0700 (Thu, 28 May 2009) $
 */

public class LdifSearch extends LdapSearch
{


  /**
   * This creates a new <code>LdifSearch</code> with the supplied pool.
   *
   * @param  pool  <code>LdapPool</code>
   */
  public LdifSearch(final LdapPool<Ldap> pool)
  {
    super(pool);
  }


  /**
   * This will perform a LDAP search with the supplied query and return
   * attributes. The results will be written to the supplied <code>
   * OutputStream</code>.
   *
   * @param  query  <code>String</code> to search for
   * @param  attrs  <code>String[]</code> to return
   * @param  out  <code>OutputStream</code> to write to
   *
   * @throws  NamingException  if an error occurs while searching
   * @throws  IOException  if an error occurs while writing search results
   */
  public void search(
    final String query,
    final String[] attrs,
    final OutputStream out)
    throws NamingException, IOException
  {
    (new Ldif()).outputLdif(this.search(query, attrs), out);
  }


  /**
   * This will perform a LDAP search with the supplied query and return
   * attributes.
   *
   * @param  query  <code>String</code> to search for
   * @param  attrs  <code>String[]</code> to return
   *
   * @return  <code>String</code> of LDIF results
   *
   * @throws  NamingException  if an error occurs while searching
   * @throws  IOException  if an error occurs while writing search results
   */
  public String searchToString(final String query, final String[] attrs)
    throws NamingException, IOException
  {
    return (new Ldif()).createLdif(this.search(query, attrs));
  }
}
/*
  $Id: LdifSearch.java 269 2009-05-28 14:24:37Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 269 $
  Updated: $Date: 2009-05-28 07:24:37 -0700 (Thu, 28 May 2009) $
*/
package edu.vt.middleware.ldap.ldif;

import java.io.IOException;
import java.io.OutputStream;
import javax.naming.NamingException;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.LdapSearch;
import edu.vt.middleware.ldap.pool.LdapPool;

/**
 * <code>LdifSearch</code> queries an LDAP and returns the result as a LDIF.
 * Each instance of <code>LdifSearch</code> maintains it's own pool of LDAP
 * connections.
 *
 * @author  Middleware Services
 * @version  $Revision: 269 $ $Date: 2009-05-28 07:24:37 -0700 (Thu, 28 May 2009) $
 */

public class LdifSearch extends LdapSearch
{


  /**
   * This creates a new <code>LdifSearch</code> with the supplied pool.
   *
   * @param  pool  <code>LdapPool</code>
   */
  public LdifSearch(final LdapPool<Ldap> pool)
  {
    super(pool);
  }


  /**
   * This will perform a LDAP search with the supplied query and return
   * attributes. The results will be written to the supplied <code>
   * OutputStream</code>.
   *
   * @param  query  <code>String</code> to search for
   * @param  attrs  <code>String[]</code> to return
   * @param  out  <code>OutputStream</code> to write to
   *
   * @throws  NamingException  if an error occurs while searching
   * @throws  IOException  if an error occurs while writing search results
   */
  public void search(
    final String query,
    final String[] attrs,
    final OutputStream out)
    throws NamingException, IOException
  {
    (new Ldif()).outputLdif(this.search(query, attrs), out);
  }


  /**
   * This will perform a LDAP search with the supplied query and return
   * attributes.
   *
   * @param  query  <code>String</code> to search for
   * @param  attrs  <code>String[]</code> to return
   *
   * @return  <code>String</code> of LDIF results
   *
   * @throws  NamingException  if an error occurs while searching
   * @throws  IOException  if an error occurs while writing search results
   */
  public String searchToString(final String query, final String[] attrs)
    throws NamingException, IOException
  {
    return (new Ldif()).createLdif(this.search(query, attrs));
  }
}
/*
  $Id: LdifSearch.java 269 2009-05-28 14:24:37Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 269 $
  Updated: $Date: 2009-05-28 07:24:37 -0700 (Thu, 28 May 2009) $
*/
package edu.vt.middleware.ldap.ldif;

import java.io.IOException;
import java.io.OutputStream;
import javax.naming.NamingException;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.LdapSearch;
import edu.vt.middleware.ldap.pool.LdapPool;

/**
 * <code>LdifSearch</code> queries an LDAP and returns the result as a LDIF.
 * Each instance of <code>LdifSearch</code> maintains it's own pool of LDAP
 * connections.
 *
 * @author  Middleware Services
 * @version  $Revision: 269 $ $Date: 2009-05-28 07:24:37 -0700 (Thu, 28 May 2009) $
 */

public class LdifSearch extends LdapSearch
{


  /**
   * This creates a new <code>LdifSearch</code> with the supplied pool.
   *
   * @param  pool  <code>LdapPool</code>
   */
  public LdifSearch(final LdapPool<Ldap> pool)
  {
    super(pool);
  }


  /**
   * This will perform a LDAP search with the supplied query and return
   * attributes. The results will be written to the supplied <code>
   * OutputStream</code>.
   *
   * @param  query  <code>String</code> to search for
   * @param  attrs  <code>String[]</code> to return
   * @param  out  <code>OutputStream</code> to write to
   *
   * @throws  NamingException  if an error occurs while searching
   * @throws  IOException  if an error occurs while writing search results
   */
  public void search(
    final String query,
    final String[] attrs,
    final OutputStream out)
    throws NamingException, IOException
  {
    (new Ldif()).outputLdif(this.search(query, attrs), out);
  }


  /**
   * This will perform a LDAP search with the supplied query and return
   * attributes.
   *
   * @param  query  <code>String</code> to search for
   * @param  attrs  <code>String[]</code> to return
   *
   * @return  <code>String</code> of LDIF results
   *
   * @throws  NamingException  if an error occurs while searching
   * @throws  IOException  if an error occurs while writing search results
   */
  public String searchToString(final String query, final String[] attrs)
    throws NamingException, IOException
  {
    return (new Ldif()).createLdif(this.search(query, attrs));
  }
}
