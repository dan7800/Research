/*
  $Id: RequestDumperFilter.java 606 2009-09-14 19:41:19Z dfisher $

  Copyright (C) 2003-2009 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 606 $
  Updated: $Date: 2009-09-14 12:41:19 -0700 (Mon, 14 Sep 2009) $
*/
package edu.vt.middleware.servlet.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Locale;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Example filter that dumps interesting state information about a request to
 * the associated servlet context log file, before allowing the servlet to
 * process the request in the usual way. This can be installed as needed to
 * assist in debugging problems.
 *
 * <p>NOTE: This is a modified version of the RequestDumperFilter that is
 * distributed with Tomcat. It has the additional feature of dumping the request
 * body in addition to headers. Another important change is that it uses commons
 * logging instead of {@link javax.servlet.ServletContext#log(String)}. The
 * TRACE level must be specified for this class to dump the request in the
 * application log.</p>
 *
 * @author  Craig McClanahan
 * @author  Middleware Services
 * @version  $Revision: 606 $
 */

public final class RequestDumperFilter implements Filter
{

  /** Logger instance. */
  private final Log logger = LogFactory.getLog(getClass());

  /**
   * The filter configuration object we are associated with. If this value is
   * null, this filter instance is not currently configured.
   */
  private FilterConfig config;


  /** {@inheritDoc}. */
  public void init(final FilterConfig filterConfig)
    throws ServletException
  {
    this.config = filterConfig;
  }


  /** {@inheritDoc}. */
  @SuppressWarnings(value = "unchecked")
  public void doFilter(
    final ServletRequest request,
    final ServletResponse response,
    final FilterChain chain)
    throws IOException, ServletException
  {
    if (this.config == null) {
      return;
    }

    // Just pass through to next filter if we're not at TRACE level
    if (!logger.isTraceEnabled()) {
      chain.doFilter(request, response);
      return;
    }

    // Create a variable to hold the (possibly different) request
    // passed to downstream filters
    ServletRequest downstreamRequest = request;

    // Render the generic servlet request properties
    final StringWriter sw = new StringWriter();
    final PrintWriter writer = new PrintWriter(sw);
    writer.println("Dumping request...");
    writer.println("-----------------------------------------------------");
    writer.println("REQUEST received " + Calendar.getInstance().getTime());
    writer.println(" characterEncoding=" + request.getCharacterEncoding());
    writer.println("     contentLength=" + request.getContentLength());
    writer.println("       contentType=" + request.getContentType());
    writer.println("            locale=" + request.getLocale());
    writer.print("           locales=");

    final Enumeration<Locale> locales = request.getLocales();
    for (int i = 0; locales.hasMoreElements(); i++) {
      if (i > 0) {
        writer.print(", ");
      }
      writer.print(locales.nextElement());
    }
    writer.println();

    final Enumeration<String> paramNames = request.getParameterNames();
    while (paramNames.hasMoreElements()) {
      final String name = paramNames.nextElement();
      writer.print("         parameter=" + name + "=");

      final String[] values = request.getParameterValues(name);
      for (int i = 0; i < values.length; i++) {
        if (i > 0) {
          writer.print(", ");
        }
        writer.print(values[i]);
      }
      writer.println();
    }
    writer.println("          protocol=" + request.getProtocol());
    writer.println("        remoteAddr=" + request.getRemoteAddr());
    writer.println("        remoteHost=" + request.getRemoteHost());
    writer.println("            scheme=" + request.getScheme());
    writer.println("        serverName=" + request.getServerName());
    writer.println("        serverPort=" + request.getServerPort());
    writer.println("          isSecure=" + request.isSecure());

    // Render the HTTP servlet request properties
    if (request instanceof HttpServletRequest) {
      final HttpServletRequest hrequest = (HttpServletRequest) request;
      writer.println("       contextPath=" + hrequest.getContextPath());

      Cookie[] cookies = hrequest.getCookies();
      if (cookies == null) {
        cookies = new Cookie[0];
      }
      for (int i = 0; i < cookies.length; i++) {
        writer.println(
          "            cookie=" + cookies[i].getName() + "=" +
          cookies[i].getValue());
      }

      final Enumeration<String> headerNames = hrequest.getHeaderNames();
      while (headerNames.hasMoreElements()) {
        final String name = headerNames.nextElement();
        final String value = hrequest.getHeader(name);
        writer.println("            header=" + name + "=" + value);
      }
      writer.println("            method=" + hrequest.getMethod());
      writer.println("          pathInfo=" + hrequest.getPathInfo());
      writer.println("       queryString=" + hrequest.getQueryString());
      writer.println("        remoteUser=" + hrequest.getRemoteUser());
      writer.println("requestedSessionId=" + hrequest.getRequestedSessionId());
      writer.println("        requestURI=" + hrequest.getRequestURI());
      writer.println("       servletPath=" + hrequest.getServletPath());

      // Create a wrapped request that contains the request body
      // and that we will pass to downstream filters
      final ByteArrayRequestWrapper wrappedRequest =
        new ByteArrayRequestWrapper(hrequest);
      downstreamRequest = wrappedRequest;
      writer.println(wrappedRequest.getRequestBodyAsString());
    }
    writer.println("-----------------------------------------------------");

    // Log the resulting string
    writer.flush();
    logger.trace(sw.getBuffer().toString());

    // Pass control on to the next filter
    chain.doFilter(downstreamRequest, response);
  }


  /** {@inheritDoc}. */
  public void destroy()
  {
    this.config = null;
  }


  /** {@inheritDoc}. */
  @Override
  public String toString()
  {
    if (this.config == null) {
      return "RequestDumperFilter()";
    }

    final StringBuffer sb = new StringBuffer("RequestDumperFilter(");
    sb.append(this.config);
    sb.append(")");
    return sb.toString();
  }
}
/*
  $Id: RequestDumperFilter.java 606 2009-09-14 19:41:19Z dfisher $

  Copyright (C) 2003-2009 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 606 $
  Updated: $Date: 2009-09-14 12:41:19 -0700 (Mon, 14 Sep 2009) $
*/
package edu.vt.middleware.servlet.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Locale;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Example filter that dumps interesting state information about a request to
 * the associated servlet context log file, before allowing the servlet to
 * process the request in the usual way. This can be installed as needed to
 * assist in debugging problems.
 *
 * <p>NOTE: This is a modified version of the RequestDumperFilter that is
 * distributed with Tomcat. It has the additional feature of dumping the request
 * body in addition to headers. Another important change is that it uses commons
 * logging instead of {@link javax.servlet.ServletContext#log(String)}. The
 * TRACE level must be specified for this class to dump the request in the
 * application log.</p>
 *
 * @author  Craig McClanahan
 * @author  Middleware Services
 * @version  $Revision: 606 $
 */

public final class RequestDumperFilter implements Filter
{

  /** Logger instance. */
  private final Log logger = LogFactory.getLog(getClass());

  /**
   * The filter configuration object we are associated with. If this value is
   * null, this filter instance is not currently configured.
   */
  private FilterConfig config;


  /** {@inheritDoc}. */
  public void init(final FilterConfig filterConfig)
    throws ServletException
  {
    this.config = filterConfig;
  }


  /** {@inheritDoc}. */
  @SuppressWarnings(value = "unchecked")
  public void doFilter(
    final ServletRequest request,
    final ServletResponse response,
    final FilterChain chain)
    throws IOException, ServletException
  {
    if (this.config == null) {
      return;
    }

    // Just pass through to next filter if we're not at TRACE level
    if (!logger.isTraceEnabled()) {
      chain.doFilter(request, response);
      return;
    }

    // Create a variable to hold the (possibly different) request
    // passed to downstream filters
    ServletRequest downstreamRequest = request;

    // Render the generic servlet request properties
    final StringWriter sw = new StringWriter();
    final PrintWriter writer = new PrintWriter(sw);
    writer.println("Dumping request...");
    writer.println("-----------------------------------------------------");
    writer.println("REQUEST received " + Calendar.getInstance().getTime());
    writer.println(" characterEncoding=" + request.getCharacterEncoding());
    writer.println("     contentLength=" + request.getContentLength());
    writer.println("       contentType=" + request.getContentType());
    writer.println("            locale=" + request.getLocale());
    writer.print("           locales=");

    final Enumeration<Locale> locales = request.getLocales();
    for (int i = 0; locales.hasMoreElements(); i++) {
      if (i > 0) {
        writer.print(", ");
      }
      writer.print(locales.nextElement());
    }
    writer.println();

    final Enumeration<String> paramNames = request.getParameterNames();
    while (paramNames.hasMoreElements()) {
      final String name = paramNames.nextElement();
      writer.print("         parameter=" + name + "=");

      final String[] values = request.getParameterValues(name);
      for (int i = 0; i < values.length; i++) {
        if (i > 0) {
          writer.print(", ");
        }
        writer.print(values[i]);
      }
      writer.println();
    }
    writer.println("          protocol=" + request.getProtocol());
    writer.println("        remoteAddr=" + request.getRemoteAddr());
    writer.println("        remoteHost=" + request.getRemoteHost());
    writer.println("            scheme=" + request.getScheme());
    writer.println("        serverName=" + request.getServerName());
    writer.println("        serverPort=" + request.getServerPort());
    writer.println("          isSecure=" + request.isSecure());

    // Render the HTTP servlet request properties
    if (request instanceof HttpServletRequest) {
      final HttpServletRequest hrequest = (HttpServletRequest) request;
      writer.println("       contextPath=" + hrequest.getContextPath());

      Cookie[] cookies = hrequest.getCookies();
      if (cookies == null) {
        cookies = new Cookie[0];
      }
      for (int i = 0; i < cookies.length; i++) {
        writer.println(
          "            cookie=" + cookies[i].getName() + "=" +
          cookies[i].getValue());
      }

      final Enumeration<String> headerNames = hrequest.getHeaderNames();
      while (headerNames.hasMoreElements()) {
        final String name = headerNames.nextElement();
        final String value = hrequest.getHeader(name);
        writer.println("            header=" + name + "=" + value);
      }
      writer.println("            method=" + hrequest.getMethod());
      writer.println("          pathInfo=" + hrequest.getPathInfo());
      writer.println("       queryString=" + hrequest.getQueryString());
      writer.println("        remoteUser=" + hrequest.getRemoteUser());
      writer.println("requestedSessionId=" + hrequest.getRequestedSessionId());
      writer.println("        requestURI=" + hrequest.getRequestURI());
      writer.println("       servletPath=" + hrequest.getServletPath());

      // Create a wrapped request that contains the request body
      // and that we will pass to downstream filters
      final ByteArrayRequestWrapper wrappedRequest =
        new ByteArrayRequestWrapper(hrequest);
      downstreamRequest = wrappedRequest;
      writer.println(wrappedRequest.getRequestBodyAsString());
    }
    writer.println("-----------------------------------------------------");

    // Log the resulting string
    writer.flush();
    logger.trace(sw.getBuffer().toString());

    // Pass control on to the next filter
    chain.doFilter(downstreamRequest, response);
  }


  /** {@inheritDoc}. */
  public void destroy()
  {
    this.config = null;
  }


  /** {@inheritDoc}. */
  @Override
  public String toString()
  {
    if (this.config == null) {
      return "RequestDumperFilter()";
    }

    final StringBuffer sb = new StringBuffer("RequestDumperFilter(");
    sb.append(this.config);
    sb.append(")");
    return sb.toString();
  }
}
/*
  $Id: RequestDumperFilter.java 270 2009-05-28 16:31:55Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 270 $
  Updated: $Date: 2009-05-28 09:31:55 -0700 (Thu, 28 May 2009) $
*/
package edu.vt.middleware.servlet.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Locale;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Example filter that dumps interesting state information about a request to
 * the associated servlet context log file, before allowing the servlet to
 * process the request in the usual way. This can be installed as needed to
 * assist in debugging problems.
 *
 * <p>NOTE: This is a modified version of the RequestDumperFilter that is
 * distributed with Tomcat. It has the additional feature of dumping the request
 * body in addition to headers. Another important change is that it uses commons
 * logging instead of {@link javax.servlet.ServletContext#log(String)}. The
 * TRACE level must be specified for this class to dump the request in the
 * application log.</p>
 *
 * @author  Craig McClanahan
 * @author  Middleware Services
 * @version  $Revision: 270 $
 */

public final class RequestDumperFilter implements Filter
{

  /** Logger instance. */
  private final Log logger = LogFactory.getLog(getClass());

  /**
   * The filter configuration object we are associated with. If this value is
   * null, this filter instance is not currently configured.
   */
  private FilterConfig config;


  /** {@inheritDoc}. */
  public void init(final FilterConfig filterConfig)
    throws ServletException
  {
    this.config = filterConfig;
  }


  /** {@inheritDoc}. */
  @SuppressWarnings(value = "unchecked")
  public void doFilter(
    final ServletRequest request,
    final ServletResponse response,
    final FilterChain chain)
    throws IOException, ServletException
  {
    if (this.config == null) {
      return;
    }

    // Just pass through to next filter if we're not at TRACE level
    if (!logger.isTraceEnabled()) {
      chain.doFilter(request, response);
      return;
    }

    // Create a variable to hold the (possibly different) request
    // passed to downstream filters
    ServletRequest downstreamRequest = request;

    // Render the generic servlet request properties
    final StringWriter sw = new StringWriter();
    final PrintWriter writer = new PrintWriter(sw);
    writer.println("Dumping request...");
    writer.println("-----------------------------------------------------");
    writer.println("REQUEST received " + Calendar.getInstance().getTime());
    writer.println(" characterEncoding=" + request.getCharacterEncoding());
    writer.println("     contentLength=" + request.getContentLength());
    writer.println("       contentType=" + request.getContentType());
    writer.println("            locale=" + request.getLocale());
    writer.print("           locales=");

    final Enumeration<Locale> locales = request.getLocales();
    for (int i = 0; locales.hasMoreElements(); i++) {
      if (i > 0) {
        writer.print(", ");
      }
      writer.print(locales.nextElement());
    }
    writer.println();

    final Enumeration<String> paramNames = request.getParameterNames();
    while (paramNames.hasMoreElements()) {
      final String name = paramNames.nextElement();
      writer.print("         parameter=" + name + "=");

      final String[] values = request.getParameterValues(name);
      for (int i = 0; i < values.length; i++) {
        if (i > 0) {
          writer.print(", ");
        }
        writer.print(values[i]);
      }
      writer.println();
    }
    writer.println("          protocol=" + request.getProtocol());
    writer.println("        remoteAddr=" + request.getRemoteAddr());
    writer.println("        remoteHost=" + request.getRemoteHost());
    writer.println("            scheme=" + request.getScheme());
    writer.println("        serverName=" + request.getServerName());
    writer.println("        serverPort=" + request.getServerPort());
    writer.println("          isSecure=" + request.isSecure());

    // Render the HTTP servlet request properties
    if (request instanceof HttpServletRequest) {
      final HttpServletRequest hrequest = (HttpServletRequest) request;
      writer.println("       contextPath=" + hrequest.getContextPath());

      Cookie[] cookies = hrequest.getCookies();
      if (cookies == null) {
        cookies = new Cookie[0];
      }
      for (int i = 0; i < cookies.length; i++) {
        writer.println(
          "            cookie=" + cookies[i].getName() + "=" +
          cookies[i].getValue());
      }

      final Enumeration<String> headerNames = hrequest.getHeaderNames();
      while (headerNames.hasMoreElements()) {
        final String name = headerNames.nextElement();
        final String value = hrequest.getHeader(name);
        writer.println("            header=" + name + "=" + value);
      }
      writer.println("            method=" + hrequest.getMethod());
      writer.println("          pathInfo=" + hrequest.getPathInfo());
      writer.println("       queryString=" + hrequest.getQueryString());
      writer.println("        remoteUser=" + hrequest.getRemoteUser());
      writer.println("requestedSessionId=" + hrequest.getRequestedSessionId());
      writer.println("        requestURI=" + hrequest.getRequestURI());
      writer.println("       servletPath=" + hrequest.getServletPath());

      // Create a wrapped request that contains the request body
      // and that we will pass to downstream filters
      final ByteArrayRequestWrapper wrappedRequest =
        new ByteArrayRequestWrapper(hrequest);
      downstreamRequest = wrappedRequest;
      writer.println(wrappedRequest.getRequestBodyAsString());
    }
    writer.println("-----------------------------------------------------");

    // Log the resulting string
    writer.flush();
    logger.trace(sw.getBuffer().toString());

    // Pass control on to the next filter
    chain.doFilter(downstreamRequest, response);
  }


  /** {@inheritDoc}. */
  public void destroy()
  {
    this.config = null;
  }


  /** {@inheritDoc}. */
  @Override
  public String toString()
  {
    if (this.config == null) {
      return "RequestDumperFilter()";
    }

    final StringBuffer sb = new StringBuffer("RequestDumperFilter(");
    sb.append(this.config);
    sb.append(")");
    return sb.toString();
  }
}
