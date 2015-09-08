/*
  $Id: SearchThread.java 649 2009-09-18 20:28:11Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 649 $
  Updated: $Date: 2009-09-18 13:28:11 -0700 (Fri, 18 Sep 2009) $
*/
package edu.vt.middleware.ldap.search;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.naming.NamingException;
import javax.naming.directory.SearchResult;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.SearchFilter;
import edu.vt.middleware.ldap.pool.LdapPool;
import edu.vt.middleware.ldap.pool.LdapPoolException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <code>SearchThread</code> performs an Ldap query.
 *
 * @author  Middleware Services
 * @version  $Revision: 649 $ $Date: 2009-09-18 13:28:11 -0700 (Fri, 18 Sep 2009) $
 */

public class SearchThread implements Runnable
{

  /** Log for this class. */
  private static final Log LOG = LogFactory.getLog(SearchThread.class);

  /** Ldap query to perform. */
  private String ldapQuery;

  /** Ldap attributes to return. */
  private String[] attrs;

  /** Ldap used to perform search. */
  private LdapPool<Ldap> ldapPool;

  /** Ldap search results. */
  private List<QueryResult> results = new ArrayList<QueryResult>();

  /** Thread to perform the search. */
  private Thread searchThread;


  /**
   * This creates a new <code>SearchThread</code>.
   *
   * @param  lp  <code>LdapPool</code>
   * @param  q  <code>String</code>
   * @param  a  <code>String[]</code>
   */
  public SearchThread(final LdapPool<Ldap> lp, final String q, final String[] a)
  {
    this.ldapPool = lp;
    this.ldapQuery = q;
    this.attrs = a;
    this.searchThread = new Thread(this);
  }


  /**
   * This returns the ldap query for this search thread.
   *
   * @return  <code>String</code> ldap query
   */
  public String getLdapQuery()
  {
    return this.ldapQuery;
  }


  /**
   * This returns the return attributes for this search thread.
   *
   * @return  <code>String[]</code> of return attributes
   */
  public String[] getAttrs()
  {
    return this.attrs;
  }


  /** This executes the process of performing the Ldap search. */
  public void startSearch()
  {
    this.searchThread.start();
  }


  /** This executes the process of performing the Ldap search. */
  public void run()
  {
    Ldap ldap = null;
    try {
      ldap = this.ldapPool.checkOut();
      final long beginTime = System.currentTimeMillis();
      if (LOG.isDebugEnabled()) {
        LOG.debug(this.ldapQuery + " started at " + beginTime);
      }
      final Iterator<SearchResult> i = ldap.search(
        new SearchFilter(this.ldapQuery), this.attrs);
      final long searchTime = System.currentTimeMillis() - beginTime;
      while (i.hasNext()) {
        final QueryResult q = new QueryResult(i.next());
        q.setLdapQuery(this.ldapQuery);
        q.setQueryAttributes(this.attrs);
        q.setSearchTime(searchTime);
        this.results.add(q);
      }
      if (LOG.isDebugEnabled()) {
        LOG.debug(
          this.ldapQuery + " found " + this.results.size() + " results in " +
          searchTime + "ms");
      }
    } catch (NamingException e) {
      if (LOG.isErrorEnabled()) {
        LOG.error("Error occured performing LDAP search", e);
      }
    } catch (LdapPoolException e) {
      if (LOG.isErrorEnabled()) {
        LOG.error("Error occured retrieving an object from the pool", e);
      }
    } finally {
      this.ldapPool.checkIn(ldap);
    }
  }


  /**
   * Allows other threads to join with the underlying thread of this class.
   *
   * @throws  InterruptedException  if another thread has interrupted the
   * current thread. The interrupted status of the current thread is cleared
   * when this exception is thrown.
   */
  public void join()
    throws InterruptedException
  {
    this.searchThread.join();
  }


  /**
   * This returns the results of the Ldap search.
   *
   * @return  <code>List</code> of <code>QueryResult</code>
   */
  public List<QueryResult> getResults()
  {
    return this.results;
  }
}
