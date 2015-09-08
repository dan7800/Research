/*
  $Id: PeopleSearch.java 538 2009-09-02 20:04:15Z marvin.addison $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 538 $
  Updated: $Date: 2009-09-02 13:04:15 -0700 (Wed, 02 Sep 2009) $
*/
package edu.vt.middleware.ldap.search;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.naming.directory.SearchResult;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.dsml.Dsmlv1;
import edu.vt.middleware.ldap.dsml.Dsmlv2;
import edu.vt.middleware.ldap.ldif.Ldif;
import edu.vt.middleware.ldap.pool.LdapPool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * <code>PeopleSearch</code> queries an LDAP and returns the result as DSML or
 * LDIF. Each instance of <code>PeopleSearch</code> maintains it's own pool of
 * LDAP connections.
 *
 * @author  Middleware Services
 * @version  $Revision: 538 $ $Date: 2009-09-02 13:04:15 -0700 (Wed, 02 Sep 2009) $
 */

public class PeopleSearch
{

  /** Log for this class. */
  private static final Log LOG = LogFactory.getLog(PeopleSearch.class);

  /** Valid DSML versions. */
  public enum OutputFormat {

    /** DSML version 1. */
    DSMLV1,

    /** DSML version 2. */
    DSMLV2,

    /** LDIF. */
    LDIF
  }

  /** Whether to proxy SASL authorization. */
  private boolean proxySaslAuthz;

  /** Search modules. */
  private Map<Integer, SearchExecutor> searchExecutors =
    new HashMap<Integer, SearchExecutor>();

  /** Retrieve ldap objects for searching. */
  private LdapPoolManager ldapPoolManager;

  /** Dsml version 1 object. */
  private Dsmlv1 dsmlv1 = new Dsmlv1();

  /** Dsml version 2 object. */
  private Dsmlv2 dsmlv2 = new Dsmlv2();

  /** Ldif object. */
  private Ldif ldif = new Ldif();


  /** Default constructor. */
  public PeopleSearch() {}


  /**
   * Creates a <code>PeopleSearch</code> using a spring bean configuration
   * context at the supplied path.
   *
   * @param  path  Classpath location of Spring context.
   * @param  beanName  Name of the <code>PeopleSearch</code> bean in the spring
   *                   context.
   *
   * @return  <code>PeopleSearch</code>
   */
  public static PeopleSearch createFromSpringContext(
      final String path,
      final String beanName)
  {
    final ApplicationContext context = new ClassPathXmlApplicationContext(path);
    return (PeopleSearch) context.getBean(beanName);
  }


  /**
   * This returns whether to proxy sasl authorization.
   *
   * @return  <code>boolean</code>
   */
  public boolean getProxySaslAuthorization()
  {
    return this.proxySaslAuthz;
  }


  /**
   * Sets whether to proxy sasl authorization.
   *
   * @param  b  whether to proxy sasl authorization
   */
  public void setProxySaslAuthorization(final boolean b)
  {
    this.proxySaslAuthz = b;
  }


  /**
   * This returns the ldap pool manager.
   *
   * @return  <code>LdapPoolManager</code>
   */
  public LdapPoolManager getLdapPoolManager()
  {
    return this.ldapPoolManager;
  }


  /**
   * This sets the ldap pool manager.
   *
   * @param  lpm  <code>LdapPoolManager</code>
   */
  public void setLdapPoolManager(final LdapPoolManager lpm)
  {
    this.ldapPoolManager = lpm;
  }


  /**
   * This returns the search objects used to formulate queries.
   *
   * @return  <code>Map</code> of term count to search
   */
  public Map<Integer, SearchExecutor> getSearchExecutors()
  {
    return this.searchExecutors;
  }


  /**
   * This returns the search objects used to formulate queries.
   *
   * @param  m  map of term count to search
   */
  public void setSearchExecutors(final Map<Integer, SearchExecutor> m)
  {
    this.searchExecutors = m;
  }


  /**
   * This will perform a LDAP search with the supplied <code>Query</code>. The
   * results will be written to the supplied <code>OutputStream</code>.
   *
   * @param  query  <code>Query</code> to search for
   * @param  format  <code>OutputFormat</code> to return
   * @param  out  <code>OutputStream</code> to write to
   *
   * @throws  PeopleSearchException  if an error occurs while searching
   */
  public void search(
    final Query query,
    final OutputFormat format,
    final OutputStream out)
    throws PeopleSearchException
  {
    if (format == OutputFormat.DSMLV2) {
      try {
        this.dsmlv2.outputDsml(this.doSearch(query), out);
      } catch (Exception e) {
        if (LOG.isErrorEnabled()) {
          LOG.error("Error outputting DSML", e);
        }
        throw new PeopleSearchException(e.getMessage());
      }
    } else if (format == OutputFormat.LDIF) {
      try {
        this.ldif.outputLdif(this.doSearch(query), out);
      } catch (Exception e) {
        if (LOG.isErrorEnabled()) {
          LOG.error("Error outputting LDIF", e);
        }
        throw new PeopleSearchException(e.getMessage());
      }
    } else {
      try {
        this.dsmlv1.outputDsml(this.doSearch(query), out);
      } catch (Exception e) {
        if (LOG.isErrorEnabled()) {
          LOG.error("Error outputting DSML", e);
        }
        throw new PeopleSearchException(e.getMessage());
      }
    }
  }


  /**
   * This will perform a LDAP search with the supplied <code>Query</code>.
   *
   * @param  query  <code>Query</code> to search for
   * @param  format  <code>OutputFormat</code> to return
   *
   * @return  <code>String</code> of results
   *
   * @throws  PeopleSearchException  if an error occurs while searching
   */
  public String searchToString(final Query query, final OutputFormat format)
    throws PeopleSearchException
  {
    final String results;
    if (format == OutputFormat.DSMLV2) {
      results = this.dsmlv2.outputDsmlToString(this.doSearch(query));
    } else if (format == OutputFormat.LDIF) {
      results = this.ldif.createLdif(this.doSearch(query));
    } else {
      results = this.dsmlv1.outputDsmlToString(this.doSearch(query));
    }
    return results;
  }


  /**
   * This provides command line access to a <code>PeopleSearch</code>.
   *
   * @param  args  <code>String[]</code>
   *
   * @throws  Exception  if an error occurs
   */
  public static void main(final String[] args)
    throws Exception
  {
    final PeopleSearch ps = createFromSpringContext(
      "/peoplesearch-context.xml", "peopleSearch");
    final Query query = new Query();
    final List<String> attrs = new ArrayList<String>();

    try {
      for (int i = 0; i < args.length; i++) {
        if (args[i].equals("-query")) {
          query.setRawQuery(args[++i]);
        } else {
          attrs.add(args[i]);
        }
      }

      if (query.getRawQuery() == null) {
        throw new ArrayIndexOutOfBoundsException();
      }

      if (!attrs.isEmpty()) {
        query.setQueryAttributes(attrs.toArray(new String[0]));
      }
      ps.search(query, OutputFormat.LDIF, System.out);

    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.println(
        "Usage: java " + PeopleSearch.class.getName() +
        " -query <query> <attributes>");
      System.exit(1);
    }
  }


  /**
   * This will perform a LDAP search with the supplied <code>Query</code>.
   *
   * @param  query  <code>Query</code> to search for
   *
   * @return  <code>Iterator</code> of search results
   *
   * @throws  PeopleSearchException  if an error occurs while searching
   */
  private Iterator<SearchResult> doSearch(final Query query)
    throws PeopleSearchException
  {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Query: " + query);
    }

    // get an ldap pool
    LdapPool<Ldap> pool = null;
    if (this.proxySaslAuthz) {
      final String saslAuthzId = query.getSaslAuthorizationId();
      if (saslAuthzId != null && !saslAuthzId.equals("")) {
        pool = this.ldapPoolManager.getLdapPool(saslAuthzId);
      } else {
        throw new PeopleSearchException("No SASL Authorization ID found.");
      }
    } else {
      pool = this.ldapPoolManager.getLdapPool();
    }

    // get a search object
    SearchExecutor search = null;
    if (query.getQueryParameters().length > 0) {

      if (LOG.isDebugEnabled()) {
        LOG.debug(
          "Processing valid query: " +
          Arrays.asList(query.getQueryParameters()));
      }

      Integer termCount = new Integer(query.getQueryParameters().length);
      if (termCount.intValue() > this.searchExecutors.size()) {
        termCount = this.searchExecutors.size() - 1;
      }
      search = this.searchExecutors.get(termCount);
      if (LOG.isDebugEnabled()) {
        if (search != null) {
          if (LOG.isDebugEnabled()) {
            LOG.debug("Found search module for query count of " + termCount);
          }
        } else {
          if (LOG.isDebugEnabled()) {
            LOG.debug("No search module found for query count of " + termCount);
          }
        }
      }
    }

    return search.executeSearch(pool, query);
  }
}
