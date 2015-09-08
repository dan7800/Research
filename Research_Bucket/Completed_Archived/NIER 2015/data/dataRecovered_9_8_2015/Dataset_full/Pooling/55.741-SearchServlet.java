/*
  $Id: SearchServlet.java 538 2009-09-02 20:04:15Z marvin.addison $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 538 $
  Updated: $Date: 2009-09-02 13:04:15 -0700 (Wed, 02 Sep 2009) $
*/
package edu.vt.middleware.ldap.search.servlets;

import java.io.IOException;
import java.security.cert.X509Certificate;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import edu.vt.middleware.ldap.search.PeopleSearch;
import edu.vt.middleware.ldap.search.PeopleSearch.OutputFormat;
import edu.vt.middleware.ldap.search.PeopleSearchException;
import edu.vt.middleware.ldap.search.Query;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <code>SearchServlet</code> queries a LDAP and attempts to find the best fit
 * results based on the query. The results are returned as XML in DSML format.
 *
 * @author  Middleware Services
 * @version  $Revision: 538 $ $Date: 2009-09-02 13:04:15 -0700 (Wed, 02 Sep 2009) $
 */

public final class SearchServlet extends HttpServlet
{

  /** Domain to look for properties in. */
  public static final String PROPERTIES_DOMAIN =
    "edu.vt.middleware.ldap.search.";

  /** Output type. */
  public static final String OUTPUT_TYPE = PROPERTIES_DOMAIN + "outputType";

  /** Default output type. */
  public static final String DEFAULT_OUTPUT_TYPE = "DSML";

  /** Output type. */
  public static final String SPRING_CONTEXT_PATH = PROPERTIES_DOMAIN +
    "springContextPath";

  /** Default output type. */
  public static final String DEFAULT_SPRING_CONTEXT_PATH =
    "/peoplesearch-context.xml";

  /** Name of PeopleSearch bean in Spring context. */
  public static final String PEOPLE_SEARCH_BEAN_NAME = PROPERTIES_DOMAIN +
    "peopleSearchBeanName";

  /** Default output type. */
  public static final String DEFAULT_PEOPLE_SEARCH_BEAN_NAME = "peopleSearch";

  /** serial version uid. */
  private static final long serialVersionUID = -2489565202170951966L;

  /** Log for this class. */
  private static final Log LOG = LogFactory.getLog(SearchServlet.class);

  /** Types of available output. */
  private enum OutputType {

    /** LDIF output type. */
    LDIF,

    /** DSML output type. */
    DSML
  }

  /** Type of output to produce. */
  private OutputType output;

  /** Search object to use for searching. */
  private PeopleSearch search;


  /**
   * Initialize this servlet.
   *
   * @param  config  <code>ServletConfig</code>
   *
   * @throws  ServletException  if an error occurs
   */
  public void init(final ServletConfig config)
    throws ServletException
  {
    super.init(config);

    // construct the people search object
    String springContextPath = getInitParameter(SPRING_CONTEXT_PATH);
    if (springContextPath == null) {
      springContextPath = DEFAULT_SPRING_CONTEXT_PATH;
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug(SPRING_CONTEXT_PATH + " = " + springContextPath);
    }
    String peopleSearchBeanName = getInitParameter(PEOPLE_SEARCH_BEAN_NAME);
    if (peopleSearchBeanName == null) {
      peopleSearchBeanName = DEFAULT_PEOPLE_SEARCH_BEAN_NAME;
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug(PEOPLE_SEARCH_BEAN_NAME + " = " + peopleSearchBeanName);
    }
    this.search = PeopleSearch.createFromSpringContext(
        springContextPath, peopleSearchBeanName);

    // determine output type
    String outputType = getInitParameter(OUTPUT_TYPE);
    if (outputType == null) {
      outputType = DEFAULT_OUTPUT_TYPE;
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug(OUTPUT_TYPE + " = " + outputType);
    }
    this.output = OutputType.valueOf(outputType);
  }


  /**
   * Handle all requests sent to this servlet. Valid parameters are: query,
   * attrs, content-type, and dsml-version. content-type can be set to 'text',
   * otherwise the default is 'xml'. dsml-version can be set to '2', otherwise
   * the default is 1.
   *
   * @param  request  <code>HttpServletRequest</code>
   * @param  response  <code>HttpServletResponse</code>
   *
   * @throws  ServletException  if an error occurs
   * @throws  IOException  if an error occurs
   */
  public void service(
    final HttpServletRequest request,
    final HttpServletResponse response)
    throws ServletException, IOException
  {
    final X509Certificate[] certChain = (X509Certificate[])
      request.getAttribute("javax.servlet.request.X509Certificate");
    String certSubject = null;
    if (certChain != null && certChain[0] != null) {
      certSubject = "dn:" + certChain[0].getSubjectX500Principal().getName();
    }
    if (LOG.isDebugEnabled()) {
      if (certSubject != null) {
        if (LOG.isDebugEnabled()) {
          LOG.debug(
            "Received the following client certificate: " + certSubject);
        }
      } else {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Did not receive a client certificate");
        }
      }
    }

    // determine content
    OutputFormat format = null;
    if (this.output == OutputType.LDIF) {
      response.setContentType("text/plain");
      format = OutputFormat.LDIF;
    } else {
      final String dsmlVersion = request.getParameter("dsml-version");
      if (dsmlVersion != null && dsmlVersion.equals("2")) {
        format = OutputFormat.DSMLV2;
      } else {
        format = OutputFormat.DSMLV1;
      }

      final String content = request.getParameter("content-type");
      if (content != null && content.equalsIgnoreCase("text")) {
        response.setContentType("text/plain");
      } else {
        response.setContentType("text/xml");
      }
    }

    Integer fromResult = null;
    if (request.getParameter("from-result") != null) {
      try {
        fromResult = Integer.valueOf(request.getParameter("from-result"));
      } catch (NumberFormatException e) {
        if (LOG.isWarnEnabled()) {
          LOG.warn(
            "Received invalid fromResult parameter: " +
            request.getParameter("from-result"));
        }
      }
    }

    Integer toResult = null;
    if (request.getParameter("to-result") != null) {
      try {
        toResult = Integer.valueOf(request.getParameter("to-result"));
      } catch (NumberFormatException e) {
        if (LOG.isWarnEnabled()) {
          LOG.warn(
            "Received invalid toResult parameter: " +
            request.getParameter("to-result"));
        }
      }
    }
    try {
      final Query query = new Query();
      query.setRawQuery(request.getParameter("query"));
      query.setQueryAttributes(request.getParameterValues("attrs"));
      query.setSearchRestrictions(request.getParameter("search-restrictions"));
      query.setFromResult(fromResult);
      query.setToResult(toResult);
      if (certSubject != null) {
        query.setSaslAuthorizationId(certSubject);
      }
      if (LOG.isInfoEnabled()) {
        LOG.info("Performing search with: " + query);
      }
      this.search.search(query, format, response.getOutputStream());
    } catch (PeopleSearchException e) {
      if (LOG.isErrorEnabled()) {
        LOG.error("Error performing search", e);
      }
      throw new ServletException(e);
    }
  }


  /**
   * Called by the servlet container to indicate to a servlet that the servlet
   * is being taken out of service.
   */
  public void destroy()
  {
    try {
      this.search.getLdapPoolManager().close();
    } catch (Exception e) {
      if (LOG.isErrorEnabled()) {
        LOG.error("Error closing ldap connections", e);
      }
    } finally {
      super.destroy();
    }
  }
}
/*
  $Id: SearchServlet.java 326 2009-07-15 20:58:45Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 326 $
  Updated: $Date: 2009-07-15 13:58:45 -0700 (Wed, 15 Jul 2009) $
*/
package edu.vt.middleware.ldap.servlets;

import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.LdapConfig;
import edu.vt.middleware.ldap.dsml.DsmlSearch;
import edu.vt.middleware.ldap.ldif.LdifSearch;
import edu.vt.middleware.ldap.pool.BlockingLdapPool;
import edu.vt.middleware.ldap.pool.DefaultLdapFactory;
import edu.vt.middleware.ldap.pool.LdapPool;
import edu.vt.middleware.ldap.pool.LdapPoolConfig;
import edu.vt.middleware.ldap.pool.SharedLdapPool;
import edu.vt.middleware.ldap.pool.SoftLimitLdapPool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <code>SearchServlet</code> is a servlet which queries a LDAP and returns the
 * result as LDIF or DSML. The following init params can be set for this
 * servlet: edu.vt.middleware.ldap.servlets.propertiesFile - to load ldap
 * properties from edu.vt.middleware.ldap.servlets.outputFormat - type of output
 * to produce, 'ldif' or 'dsml' Example:
 * http://www.server.com/Search?query=uid=dfisher If you need to pass complex
 * queries, such as (&(cn=daniel*)(surname=fisher)), then the query must be form
 * encoded. If you only want to receive a subset of attributes those can be
 * specified. Example:
 * http://www.server.com/Search?query=uid=dfisher&attrs=givenname&attrs=surname
 *
 * <h3>LDIF</h3>
 *
 * <p>The content returned by the servlet is of type text/plain.</p>
 * <hr/>
 * <h3>DSML</h3>
 *
 * <p>The content returned by the servlet is of type text/xml, if you want to
 * receive the content as text/plain that can be specified as well. Example:
 * http://www.server.com/Search?query=uid=dfisher&content-type=text By default
 * DSML version 1 is returned, if you want to receive DSML version 2 then you
 * must pass in the dsml-version parameter. Example:
 * http://www.server.com/Search?query=uid=dfisher&dsml-version=2</p>
 *
 * @author  Middleware Services
 * @version  $Revision: 326 $ $Date: 2009-07-15 13:58:45 -0700 (Wed, 15 Jul 2009) $
 */

public final class SearchServlet extends HttpServlet
{

  /** serial version uid. */
  private static final long serialVersionUID = -507762004623806651L;

  /** Log for this class. */
  private static final Log LOG = LogFactory.getLog(SearchServlet.class);

  /** Types of available pools. */
  private enum PoolType {

    /** blocking. */
    BLOCKING,

    /** soft limit. */
    SOFTLIMIT,

    /** shared. */
    SHARED
  }

  /** Types of available output. */
  private enum OutputType {

    /** LDIF output type. */
    LDIF,

    /** DSML output type. */
    DSML
  }

  /** Type of output to produce. */
  private OutputType output;

  /** Object to use for searching. */
  private LdifSearch ldifSearch;

  /** Object to use for searching. */
  private DsmlSearch dsmlv1Search;

  /** Object to use for searching. */
  private DsmlSearch dsmlv2Search;


  /**
   * Initialize this servlet.
   *
   * @param  config  <code>ServletConfig</code>
   *
   * @throws  ServletException  if an error occurs
   */
  public void init(final ServletConfig config)
    throws ServletException
  {
    super.init(config);

    final String propertiesFile = getInitParameter(
      ServletConstants.PROPERTIES_FILE);
    if (LOG.isDebugEnabled()) {
      LOG.debug(ServletConstants.PROPERTIES_FILE + " = " + propertiesFile);
    }

    final LdapConfig ldapConfig = LdapConfig.createFromProperties(
      SearchServlet.class.getResourceAsStream(propertiesFile));

    final String poolPropertiesFile = getInitParameter(
      ServletConstants.POOL_PROPERTIES_FILE);
    if (LOG.isDebugEnabled()) {
      LOG.debug(
        ServletConstants.POOL_PROPERTIES_FILE + " = " + poolPropertiesFile);
    }

    final LdapPoolConfig ldapPoolConfig = LdapPoolConfig.createFromProperties(
      SearchServlet.class.getResourceAsStream(poolPropertiesFile));

    LdapPool<Ldap> ldapPool = null;
    final String poolType = getInitParameter(ServletConstants.POOL_TYPE);
    if (LOG.isDebugEnabled()) {
      LOG.debug(ServletConstants.POOL_TYPE + " = " + poolType);
    }
    if (PoolType.BLOCKING == PoolType.valueOf(poolType)) {
      ldapPool = new BlockingLdapPool(
        ldapPoolConfig,
        new DefaultLdapFactory(ldapConfig));
    } else if (PoolType.SOFTLIMIT == PoolType.valueOf(poolType)) {
      ldapPool = new SoftLimitLdapPool(
        ldapPoolConfig,
        new DefaultLdapFactory(ldapConfig));
    } else if (PoolType.SHARED == PoolType.valueOf(poolType)) {
      ldapPool = new SharedLdapPool(
        ldapPoolConfig,
        new DefaultLdapFactory(ldapConfig));
    } else {
      throw new ServletException("Unknown pool type: " + poolType);
    }
    ldapPool.initialize();

    this.ldifSearch = new LdifSearch(ldapPool);
    this.dsmlv1Search = new DsmlSearch(ldapPool);
    this.dsmlv1Search.setVersion(DsmlSearch.Version.ONE);
    this.dsmlv2Search = new DsmlSearch(ldapPool);
    this.dsmlv2Search.setVersion(DsmlSearch.Version.TWO);

    String outputType = getInitParameter(ServletConstants.OUTPUT_FORMAT);
    if (outputType == null) {
      outputType = ServletConstants.DEFAULT_OUTPUT_FORMAT;
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug(ServletConstants.OUTPUT_FORMAT + " = " + outputType);
    }
    this.output = OutputType.valueOf(outputType);
  }


  /**
   * Handle all requests sent to this servlet.
   *
   * @param  request  <code>HttpServletRequest</code>
   * @param  response  <code>HttpServletResponse</code>
   *
   * @throws  ServletException  if an error occurs
   * @throws  IOException  if an error occurs
   */
  public void service(
    final HttpServletRequest request,
    final HttpServletResponse response)
    throws ServletException, IOException
  {
    if (LOG.isInfoEnabled()) {
      LOG.info(
        "Performing search: " + request.getParameter("query") +
        " for attributes: " + request.getParameter("attrs"));
    }
    try {
      if (this.output == OutputType.LDIF) {
        response.setContentType("text/plain");
        this.ldifSearch.search(
          request.getParameter("query"),
          request.getParameterValues("attrs"),
          response.getOutputStream());
      } else {
        final String content = request.getParameter("content-type");
        if (content != null && content.equalsIgnoreCase("text")) {
          response.setContentType("text/plain");
        } else {
          response.setContentType("text/xml");
        }

        final String dsmlVersion = request.getParameter("dsml-version");
        if (dsmlVersion != null && dsmlVersion.equals("2")) {
          this.dsmlv2Search.searchToStream(
            request.getParameter("query"),
            request.getParameterValues("attrs"),
            response.getOutputStream());
        } else {
          this.dsmlv1Search.searchToStream(
            request.getParameter("query"),
            request.getParameterValues("attrs"),
            response.getOutputStream());
        }
      }
    } catch (Exception e) {
      if (LOG.isErrorEnabled()) {
        LOG.error("Error performing search", e);
      }
      throw new ServletException(e);
    }
  }


  /**
   * Called by the servlet container to indicate to a servlet that the servlet
   * is being taken out of service.
   */
  public void destroy()
  {
    try {
      // all search instances share the same pool
      // only need to close one of them
      this.ldifSearch.close();
    } finally {
      super.destroy();
    }
  }
}
/*
  $Id: SearchServlet.java 269 2009-05-28 14:24:37Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 269 $
  Updated: $Date: 2009-05-28 07:24:37 -0700 (Thu, 28 May 2009) $
*/
package edu.vt.middleware.ldap.servlets;

import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.LdapConfig;
import edu.vt.middleware.ldap.dsml.DsmlSearch;
import edu.vt.middleware.ldap.ldif.LdifSearch;
import edu.vt.middleware.ldap.pool.BlockingLdapPool;
import edu.vt.middleware.ldap.pool.DefaultLdapFactory;
import edu.vt.middleware.ldap.pool.LdapPool;
import edu.vt.middleware.ldap.pool.LdapPoolConfig;
import edu.vt.middleware.ldap.pool.SharedLdapPool;
import edu.vt.middleware.ldap.pool.SoftLimitLdapPool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <code>SearchServlet</code> is a servlet which queries a LDAP and returns the
 * result as LDIF or DSML. The following init params can be set for this
 * servlet: edu.vt.middleware.ldap.servlets.propertiesFile - to load ldap
 * properties from edu.vt.middleware.ldap.servlets.outputFormat - type of output
 * to produce, 'ldif' or 'dsml' Example:
 * http://www.server.com/Search?query=uid=dfisher If you need to pass complex
 * queries, such as (&(cn=daniel*)(surname=fisher)), then the query must be form
 * encoded. If you only want to receive a subset of attributes those can be
 * specified. Example:
 * http://www.server.com/Search?query=uid=dfisher&attrs=givenname&attrs=surname
 *
 * <h3>LDIF</h3>
 *
 * <p>The content returned by the servlet is of type text/plain.</p>
 * <hr/>
 * <h3>DSML</h3>
 *
 * <p>The content returned by the servlet is of type text/xml, if you want to
 * receive the content as text/plain that can be specified as well. Example:
 * http://www.server.com/Search?query=uid=dfisher&content-type=text By default
 * DSML version 1 is returned, if you want to receive DSML version 2 then you
 * must pass in the dsml-version parameter. Example:
 * http://www.server.com/Search?query=uid=dfisher&dsml-version=2</p>
 *
 * @author  Middleware Services
 * @version  $Revision: 269 $ $Date: 2009-05-28 07:24:37 -0700 (Thu, 28 May 2009) $
 */

public final class SearchServlet extends HttpServlet
{

  /** serial version uid. */
  private static final long serialVersionUID = -507762004623806651L;

  /** Log for this class. */
  private static final Log LOG = LogFactory.getLog(SearchServlet.class);

  /** Types of available output. */
  private enum OutputType {

    /** LDIF output type. */
    LDIF,

    /** DSML output type. */
    DSML
  }

  /** Type of output to produce. */
  private OutputType output;

  /** Object to use for searching. */
  private LdifSearch ldifSearch;

  /** Object to use for searching. */
  private DsmlSearch dsmlv1Search;

  /** Object to use for searching. */
  private DsmlSearch dsmlv2Search;


  /**
   * Initialize this servlet.
   *
   * @param  config  <code>ServletConfig</code>
   *
   * @throws  ServletException  if an error occurs
   */
  public void init(final ServletConfig config)
    throws ServletException
  {
    super.init(config);

    final String propertiesFile = getInitParameter(
      ServletConstants.PROPERTIES_FILE);
    if (LOG.isDebugEnabled()) {
      LOG.debug(ServletConstants.PROPERTIES_FILE + " = " + propertiesFile);
    }

    final LdapConfig ldapConfig = LdapConfig.createFromProperties(
      propertiesFile);

    final String poolPropertiesFile = getInitParameter(
      ServletConstants.POOL_PROPERTIES_FILE);
    if (LOG.isDebugEnabled()) {
      LOG.debug(
        ServletConstants.POOL_PROPERTIES_FILE + " = " + poolPropertiesFile);
    }

    final LdapPoolConfig ldapPoolConfig = LdapPoolConfig.createFromProperties(
      poolPropertiesFile);

    LdapPool<Ldap> ldapPool = null;
    final String poolType = getInitParameter(ServletConstants.POOL_TYPE);
    if (LOG.isDebugEnabled()) {
      LOG.debug(ServletConstants.POOL_TYPE + " = " + poolType);
    }
    if (poolType != null && poolType.equalsIgnoreCase("blocking")) {
      ldapPool = new BlockingLdapPool(
        ldapPoolConfig,
        new DefaultLdapFactory(ldapConfig));
    } else if (poolType != null && poolType.equalsIgnoreCase("softlimit")) {
      ldapPool = new SoftLimitLdapPool(
        ldapPoolConfig,
        new DefaultLdapFactory(ldapConfig));
    } else {
      ldapPool = new SharedLdapPool(
        ldapPoolConfig,
        new DefaultLdapFactory(ldapConfig));
    }
    ldapPool.initialize();

    this.ldifSearch = new LdifSearch(ldapPool);
    this.dsmlv1Search = new DsmlSearch(ldapPool);
    this.dsmlv1Search.setVersion(DsmlSearch.Version.ONE);
    this.dsmlv2Search = new DsmlSearch(ldapPool);
    this.dsmlv2Search.setVersion(DsmlSearch.Version.TWO);

    final String outputFormat = getInitParameter(
      ServletConstants.OUTPUT_FORMAT);
    if (LOG.isDebugEnabled()) {
      LOG.debug(ServletConstants.OUTPUT_FORMAT + " = " + outputFormat);
    }
    if (outputFormat != null && outputFormat.equalsIgnoreCase("ldif")) {
      this.output = OutputType.LDIF;
    } else {
      this.output = OutputType.DSML;
    }
  }


  /**
   * Handle all requests sent to this servlet.
   *
   * @param  request  <code>HttpServletRequest</code>
   * @param  response  <code>HttpServletResponse</code>
   *
   * @throws  ServletException  if an error occurs
   * @throws  IOException  if an error occurs
   */
  public void service(
    final HttpServletRequest request,
    final HttpServletResponse response)
    throws ServletException, IOException
  {
    if (LOG.isInfoEnabled()) {
      LOG.info(
        "Performing search: " + request.getParameter("query") +
        " for attributes: " + request.getParameter("attrs"));
    }
    try {
      if (this.output == OutputType.LDIF) {
        response.setContentType("text/plain");
        this.ldifSearch.search(
          request.getParameter("query"),
          request.getParameterValues("attrs"),
          response.getOutputStream());
      } else {
        final String content = request.getParameter("content-type");
        if (content != null && content.equalsIgnoreCase("text")) {
          response.setContentType("text/plain");
        } else {
          response.setContentType("text/xml");
        }

        final String dsmlVersion = request.getParameter("dsml-version");
        if (dsmlVersion != null && dsmlVersion.equals("2")) {
          this.dsmlv2Search.searchToStream(
            request.getParameter("query"),
            request.getParameterValues("attrs"),
            response.getOutputStream());
        } else {
          this.dsmlv1Search.searchToStream(
            request.getParameter("query"),
            request.getParameterValues("attrs"),
            response.getOutputStream());
        }
      }
    } catch (Exception e) {
      if (LOG.isErrorEnabled()) {
        LOG.error("Error performing search", e);
      }
      throw new ServletException(e);
    }
  }


  /**
   * Called by the servlet container to indicate to a servlet that the servlet
   * is being taken out of service.
   */
  public void destroy()
  {
    try {
      // all search instances  share the same pool
      // only need to close one of them
      this.ldifSearch.close();
    } finally {
      super.destroy();
    }
  }
}
/*
  $Id: SearchServlet.java 326 2009-07-15 20:58:45Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 326 $
  Updated: $Date: 2009-07-15 13:58:45 -0700 (Wed, 15 Jul 2009) $
*/
package edu.vt.middleware.ldap.servlets;

import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.LdapConfig;
import edu.vt.middleware.ldap.dsml.DsmlSearch;
import edu.vt.middleware.ldap.ldif.LdifSearch;
import edu.vt.middleware.ldap.pool.BlockingLdapPool;
import edu.vt.middleware.ldap.pool.DefaultLdapFactory;
import edu.vt.middleware.ldap.pool.LdapPool;
import edu.vt.middleware.ldap.pool.LdapPoolConfig;
import edu.vt.middleware.ldap.pool.SharedLdapPool;
import edu.vt.middleware.ldap.pool.SoftLimitLdapPool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <code>SearchServlet</code> is a servlet which queries a LDAP and returns the
 * result as LDIF or DSML. The following init params can be set for this
 * servlet: edu.vt.middleware.ldap.servlets.propertiesFile - to load ldap
 * properties from edu.vt.middleware.ldap.servlets.outputFormat - type of output
 * to produce, 'ldif' or 'dsml' Example:
 * http://www.server.com/Search?query=uid=dfisher If you need to pass complex
 * queries, such as (&(cn=daniel*)(surname=fisher)), then the query must be form
 * encoded. If you only want to receive a subset of attributes those can be
 * specified. Example:
 * http://www.server.com/Search?query=uid=dfisher&attrs=givenname&attrs=surname
 *
 * <h3>LDIF</h3>
 *
 * <p>The content returned by the servlet is of type text/plain.</p>
 * <hr/>
 * <h3>DSML</h3>
 *
 * <p>The content returned by the servlet is of type text/xml, if you want to
 * receive the content as text/plain that can be specified as well. Example:
 * http://www.server.com/Search?query=uid=dfisher&content-type=text By default
 * DSML version 1 is returned, if you want to receive DSML version 2 then you
 * must pass in the dsml-version parameter. Example:
 * http://www.server.com/Search?query=uid=dfisher&dsml-version=2</p>
 *
 * @author  Middleware Services
 * @version  $Revision: 326 $ $Date: 2009-07-15 13:58:45 -0700 (Wed, 15 Jul 2009) $
 */

public final class SearchServlet extends HttpServlet
{

  /** serial version uid. */
  private static final long serialVersionUID = -507762004623806651L;

  /** Log for this class. */
  private static final Log LOG = LogFactory.getLog(SearchServlet.class);

  /** Types of available pools. */
  private enum PoolType {

    /** blocking. */
    BLOCKING,

    /** soft limit. */
    SOFTLIMIT,

    /** shared. */
    SHARED
  }

  /** Types of available output. */
  private enum OutputType {

    /** LDIF output type. */
    LDIF,

    /** DSML output type. */
    DSML
  }

  /** Type of output to produce. */
  private OutputType output;

  /** Object to use for searching. */
  private LdifSearch ldifSearch;

  /** Object to use for searching. */
  private DsmlSearch dsmlv1Search;

  /** Object to use for searching. */
  private DsmlSearch dsmlv2Search;


  /**
   * Initialize this servlet.
   *
   * @param  config  <code>ServletConfig</code>
   *
   * @throws  ServletException  if an error occurs
   */
  public void init(final ServletConfig config)
    throws ServletException
  {
    super.init(config);

    final String propertiesFile = getInitParameter(
      ServletConstants.PROPERTIES_FILE);
    if (LOG.isDebugEnabled()) {
      LOG.debug(ServletConstants.PROPERTIES_FILE + " = " + propertiesFile);
    }

    final LdapConfig ldapConfig = LdapConfig.createFromProperties(
      SearchServlet.class.getResourceAsStream(propertiesFile));

    final String poolPropertiesFile = getInitParameter(
      ServletConstants.POOL_PROPERTIES_FILE);
    if (LOG.isDebugEnabled()) {
      LOG.debug(
        ServletConstants.POOL_PROPERTIES_FILE + " = " + poolPropertiesFile);
    }

    final LdapPoolConfig ldapPoolConfig = LdapPoolConfig.createFromProperties(
      SearchServlet.class.getResourceAsStream(poolPropertiesFile));

    LdapPool<Ldap> ldapPool = null;
    final String poolType = getInitParameter(ServletConstants.POOL_TYPE);
    if (LOG.isDebugEnabled()) {
      LOG.debug(ServletConstants.POOL_TYPE + " = " + poolType);
    }
    if (PoolType.BLOCKING == PoolType.valueOf(poolType)) {
      ldapPool = new BlockingLdapPool(
        ldapPoolConfig,
        new DefaultLdapFactory(ldapConfig));
    } else if (PoolType.SOFTLIMIT == PoolType.valueOf(poolType)) {
      ldapPool = new SoftLimitLdapPool(
        ldapPoolConfig,
        new DefaultLdapFactory(ldapConfig));
    } else if (PoolType.SHARED == PoolType.valueOf(poolType)) {
      ldapPool = new SharedLdapPool(
        ldapPoolConfig,
        new DefaultLdapFactory(ldapConfig));
    } else {
      throw new ServletException("Unknown pool type: " + poolType);
    }
    ldapPool.initialize();

    this.ldifSearch = new LdifSearch(ldapPool);
    this.dsmlv1Search = new DsmlSearch(ldapPool);
    this.dsmlv1Search.setVersion(DsmlSearch.Version.ONE);
    this.dsmlv2Search = new DsmlSearch(ldapPool);
    this.dsmlv2Search.setVersion(DsmlSearch.Version.TWO);

    String outputType = getInitParameter(ServletConstants.OUTPUT_FORMAT);
    if (outputType == null) {
      outputType = ServletConstants.DEFAULT_OUTPUT_FORMAT;
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug(ServletConstants.OUTPUT_FORMAT + " = " + outputType);
    }
    this.output = OutputType.valueOf(outputType);
  }


  /**
   * Handle all requests sent to this servlet.
   *
   * @param  request  <code>HttpServletRequest</code>
   * @param  response  <code>HttpServletResponse</code>
   *
   * @throws  ServletException  if an error occurs
   * @throws  IOException  if an error occurs
   */
  public void service(
    final HttpServletRequest request,
    final HttpServletResponse response)
    throws ServletException, IOException
  {
    if (LOG.isInfoEnabled()) {
      LOG.info(
        "Performing search: " + request.getParameter("query") +
        " for attributes: " + request.getParameter("attrs"));
    }
    try {
      if (this.output == OutputType.LDIF) {
        response.setContentType("text/plain");
        this.ldifSearch.search(
          request.getParameter("query"),
          request.getParameterValues("attrs"),
          response.getOutputStream());
      } else {
        final String content = request.getParameter("content-type");
        if (content != null && content.equalsIgnoreCase("text")) {
          response.setContentType("text/plain");
        } else {
          response.setContentType("text/xml");
        }

        final String dsmlVersion = request.getParameter("dsml-version");
        if (dsmlVersion != null && dsmlVersion.equals("2")) {
          this.dsmlv2Search.searchToStream(
            request.getParameter("query"),
            request.getParameterValues("attrs"),
            response.getOutputStream());
        } else {
          this.dsmlv1Search.searchToStream(
            request.getParameter("query"),
            request.getParameterValues("attrs"),
            response.getOutputStream());
        }
      }
    } catch (Exception e) {
      if (LOG.isErrorEnabled()) {
        LOG.error("Error performing search", e);
      }
      throw new ServletException(e);
    }
  }


  /**
   * Called by the servlet container to indicate to a servlet that the servlet
   * is being taken out of service.
   */
  public void destroy()
  {
    try {
      // all search instances share the same pool
      // only need to close one of them
      this.ldifSearch.close();
    } finally {
      super.destroy();
    }
  }
}
