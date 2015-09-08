/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

// TranslatorConfiguration.java

package vu.globe.svcs.gtrans.cfg;


import java.net.*;
import java.io.IOException;
import java.util.*;

import vu.globe.util.debug.DebugOutput;
import vu.globe.svcs.gids.client.*;
import vu.globe.util.parse.SettingFileException;


/**
 * Container class for the translator settings.
 */
public class TranslatorConfiguration
{
  /**
   * Translator operation modes. The translator can operate as a HTTP
   * server, a HTTP proxy or both.
   */
  public static final int MODE_SERVER      = 1;
  public static final int MODE_PROXY       = 2;
  public static final int MODE_PROXYSERVER = 3;

  /** Default debug level. Level 0 disables debugging output. */
  public static final int DEF_DEBUGLEVEL = 0;

  /** Default operation mode. */
  public static final int DEF_MODE = MODE_PROXYSERVER;

  /** Default 'Globe host' and embbeded URN prefix. */
  public static final String DEF_HOSTPORT = null;
  public static final String DEF_EMBEDDED_URN_PREFIX = null;

  /** Default number of threads in the thread pool. */
  public static final int DEF_THREADPOOL_SIZE = 100;

  /** Default client connection timeout (sec). */
  public static final int DEF_CLIENT_TIMEOUT = 15;

  /** Default HTTP server connection timeout (sec). */
  public static final int DEF_HTTP_SERVER_TIMEOUT = 15;

  /** Default Globe gateway connection timeout (sec). */
  public static final int DEF_GATEWAY_TIMEOUT = 15;

  /** Default HTTP proxy address and port. */
  public static final InetAddress DEF_HTTP_PROXY_ADDR = null;
  public static final int DEF_HTTP_PROXY_PORT = 80;

  /**
   * Default URL. This URL is used if a request is sent to the
   * the redirector without an object name.
   */
  public static final String DEF_DEFURL = null;

  /** Default value for enabling access logging. */
  public static final boolean DEF_ACCESSLOG_ENABLED_FLAG = true;

  /** Default file name of the access log. */
  public static final String DEF_ACCESSLOGFILE = "gtrans.access_log";

  /** Default file name of the error log. */
  public static final String DEF_ERRORLOGFILE = "gtrans.error_log";


  private int _debugLevel;
  private int _translatorMode;
  private String _hostPort;
  private String _embeddedURNPrefix;
  private int _threadPoolSize;
  private int _clientTimeout;
  private InetAddress _gatewayAddr;
  private int _gatewayPort;
  private int _gatewayTimeout;
  private int _httpServerTimeout;
  private InetAddress _httpProxyAddr;
  private int _httpProxyPort;
  private String _defURL;
  private boolean _accessLogEnabledFlag;
  private String _accessLogFileName;
  private String _errorLogFileName;

  private GIDSSite _gids;


  /**
   * Instance creation. Sets all settings to their default value.
   */
  public TranslatorConfiguration()
  {
    _debugLevel = DEF_DEBUGLEVEL;
    _translatorMode = DEF_MODE;
    _hostPort = DEF_HOSTPORT;
    _embeddedURNPrefix = DEF_EMBEDDED_URN_PREFIX;
    _threadPoolSize = DEF_THREADPOOL_SIZE;
    _clientTimeout = DEF_CLIENT_TIMEOUT;
    _gatewayAddr = null;
    _gatewayPort = -1;
    _gatewayTimeout = DEF_GATEWAY_TIMEOUT;
    _httpServerTimeout = DEF_HTTP_SERVER_TIMEOUT;
    _httpProxyAddr = DEF_HTTP_PROXY_ADDR;
    _httpProxyPort = DEF_HTTP_PROXY_PORT;
    _defURL = DEF_DEFURL;
    _accessLogEnabledFlag = DEF_ACCESSLOG_ENABLED_FLAG;
    _accessLogFileName = DEF_ACCESSLOGFILE;
    _errorLogFileName = DEF_ERRORLOGFILE;
    _gids = null;
  }


  /**
   * Return the GIDS object.
   */
  public GIDSSite getGIDS()
  {
    return _gids;
  }


  /**
   * Set the GIDS object.
   */
  public void setGIDS(GIDSSite gids)
  {
    _gids = gids;
  }

  /**
   * Return the debug level setting.
   */
  public int getDebugLevel()
  {
    return _debugLevel;
  }


  /**
   * Set the debug level setting.
   */
  public void setDebugLevel(int n)
  {
    DebugOutput.setLogLevel (n);
    _debugLevel = n;
  }


  /**
   * Return the translator mode.
   */
  public int getTranslatorMode()
  {
    return _translatorMode;
  }


  /**
   * Set the translator mode.
   *
   * @exception  IllegalArgumentException  if the mode is invalid
   */
  public void setTranslatorMode(int mode)
    throws IllegalArgumentException
  {
    if (mode != MODE_SERVER && mode != MODE_PROXY && mode != MODE_PROXYSERVER) {
      throw new IllegalArgumentException();
    }

    _translatorMode = mode;
  }


  /**
   * Return the host name that is used to recognize embedded Globe URNs.
   */
  public String getHostPort()
  {
    return _hostPort;
  }


  /**
   * Set the host name which is used to recognize embedded Globe URNs.
   */
  public void setHostPort(String s)
  {
    _hostPort = s;
    _embeddedURNPrefix = "http://" + s + "/";
  }


  /**
   * Return the URN prefix of embedded Globe URNs. This prefix starts with
   * the HTTP scheme followed by the `Globe host'.
   */
  public String getEmbeddedURNPrefix()
  {
    return _embeddedURNPrefix;
  }


  /**
   * Return the size of the thread pool.
   */
  public int getThreadPoolSize()
  {
    return _threadPoolSize;
  }


  /**
   * Set the size of the thread pool.
   *
   * @exception  IllegalArgumentException  if the size is out of range
   */
  public void setThreadPoolSize(int n)
    throws IllegalArgumentException
  {
    if (n < 1) {
      throw new IllegalArgumentException();
    }
    _threadPoolSize = n;
  }


  /**
   * Return the client connection timeout (seconds).
   */
  public int getClientTimeout()
  {
    return _clientTimeout;
  }


  /**
   * Set the client connection timeout.
   *
   * @param  timeout  the timeout (seconds)
   *
   * @exception  IllegalArgumentException  if the timeout is out of range
   */
  public void setClientTimeout(int timeout)
    throws IllegalArgumentException
  {
    if (timeout < 0) {
      throw new IllegalArgumentException();
    }
    _clientTimeout = timeout;
  }


  /**
   * Return the address of the Globe gateway. Initialises from GIDS if
   * necessary.
   */
  public InetAddress getGatewayAddress()
    throws GatewayNotFoundException
  {
    if (_gatewayAddr == null) {
      contactGateway();
    }
    return _gatewayAddr;
  }

  /**
   * Set the address of the Globe gateway.
   */
  public void setGatewayAddress(InetAddress addr)
  {
    _gatewayAddr = addr;
  }


  /**
   * Return the port number of the Globe gateway. Initialises from GIDS if
   * necessary.
   */
  public int getGatewayPort()
    throws GatewayNotFoundException
  {
    if (_gatewayAddr == null) {
      contactGateway();
    }
    return _gatewayPort;
  }

  /**
   * Set the port number of the Globe gateway.
   *
   * @exception  IllegalArgumentException  if the port number is out of range
   */
  public void setGatewayPort(int port)
    throws IllegalArgumentException
  {
    if (port <= 0) {
      throw new IllegalArgumentException();
    }
    _gatewayPort = port;
  }

  /**
   * Return the Globe gateway connection timeout (seconds).
   */
  public int getGatewayTimeout()
  {
    return _gatewayTimeout;
  }


  /**
   * Set the Globe gateway connection timeout.
   *
   * @param  timeout  the timeout (seconds)
   *
   * @exception  IllegalArgumentException  if the timeout is out of range
   */
  public void setGatewayTimeout(int timeout)
    throws IllegalArgumentException
  {
    if (timeout < 0) {
      throw new IllegalArgumentException();
    }
    _gatewayTimeout = timeout;
  }


  /**
   * Return the HTTP server connection timeout (seconds).
   */
  public int getHttpServerTimeout()
  {
    return _httpServerTimeout;
  }


  /**
   * Set the HTTP server connection timeout.
   *
   * @param  timeout  the timeout (seconds)
   *
   * @exception  IllegalArgumentException  if the timeout is out of range
   */
  public void setHttpServerTimeout(int timeout)
    throws IllegalArgumentException
  {
    if (timeout < 0) {
      throw new IllegalArgumentException();
    }
    _httpServerTimeout = timeout;
  }


  /**
   * Return the address of the HTTP proxy; <code>null</code> if not defined.
   */
  public InetAddress getHttpProxyAddress()
  {
    return _httpProxyAddr;
  }


  /**
   * Set the address of the HTTP proxy.
   */
  public void setHttpProxyAddress(InetAddress addr)
  {
    _httpProxyAddr = addr;
  }


  /**
   * Return the port number of the HTTP proxy.
   */
  public int getHttpProxyPort()
  {
    return _httpProxyPort;
  }


  /**
   * Set the port number of the HTTP proxy.
   *
   * @exception  IllegalArgumentException  if the port number is out of range
   */
  public void setHttpProxyPort(int port)
    throws IllegalArgumentException
  {
    if (port <= 0) {
      throw new IllegalArgumentException();
    }
    _httpProxyPort = port;
  }


  /**
   * Return the default URL; <code>null</code> if this URL is not set.
   */
  public String getDefaultURL()
  {
    return _defURL;
  }


  /**
   * Set the default URL. This URL is used when a request-URI does not
   * contain an object name.
   *
   * @param  url  the URL; if <code>null</code>, the redirector will
   *              reply with a "missing object name" error message
   */
  public String setDefaultURL(String url)
  {
    return _defURL = url;
  }


  /**
   * Return <code>true</code> if access logging should be enabled;
   * <code>false</code> otherwise.
   */
  public boolean getAccessLogEnabledFlag()
  {
    return _accessLogEnabledFlag;
  }


  /**
   * Enable or disable access logging.
   */
  public void setAccessLogEnabledFlag(boolean on)
  {
    _accessLogEnabledFlag = on;
  }


  /**
   * Return the name of the access log.
   */
  public String getAccessLogFileName()
  {
    return _accessLogFileName;
  }


  /**
   * Set the name of the access log.
   */
  public void setAccessLogFileName(String name)
  {
    _accessLogFileName = name;
  }


  /**
   * Return the name of the error log.
   */
  public String getErrorLogFileName()
  {
    return _errorLogFileName;
  }


  /**
   * Set the name of the error log.
   */
  public void setErrorLogFileName(String name)
  {
    _errorLogFileName = name;
  }

  /**
   * When first called, initialises Globe Gateway address settings from GIDS.
   */
  private synchronized void contactGateway()
    throws GatewayNotFoundException
  {
    if (_gatewayAddr != null) {
      return;
    }

    Iterator it = null;
    // prepare search for the globe gateway in GIDS
    try {
      // search in site.cfg and the local base region
      it = _gids.regIterator ("ggateway", true, null);
    }
    catch (SettingFileException e) {
      throw new GatewayNotFoundException (
        "unable to parse site configuration file: " +  e.getMessage());
    }

    String[] attrs = new String[] { "host", "port" };
    while (it.hasNext()) {
      System.out.println("retrieving gateway entry from GIDS");
      HashMap ggatewayEntry = (HashMap) it.next();
      if (! GIDSUtil.hasAttrs (ggatewayEntry, attrs)) {
        _gids.printRegIteratorError (ggatewayEntry, "ggateway", attrs);
        continue;
      }

      String host = GIDSUtil.getOneValue (ggatewayEntry, "host");
      String port = GIDSUtil.getOneValue (ggatewayEntry, "port");

      System.out.println("gateway addr: " + host);
      System.out.println("gateway port: " + port);

      InetAddress gatewayHost; int gatewayPort;
      try {
        gatewayHost = InetAddress.getByName(host);
        gatewayPort = Integer.parseInt(port);
      }
      catch(NumberFormatException e) {
        System.err.println("invalid Globe gateway port: " + e.getMessage());
        _gids.printEntryInfo (ggatewayEntry, "ggateway", attrs);
        continue;
      }
      catch(UnknownHostException e) {
        System.err.println("unable to locate Globe gateway host: " +
                    e.getMessage());
        _gids.printEntryInfo (ggatewayEntry, "ggateway", attrs);
        continue;
      }

      // test whether this gateway is alive
      System.out.println("");
      System.out.println("Testing liveness of gateway...");

      Socket sock;
      try {
        sock = new Socket(gatewayHost, gatewayPort);
        sock.close();
      }
      catch(IOException e) {
        System.err.println("unable to contact gateway in registration entry: " +
	  e.getMessage());
        _gids.printEntryInfo (ggatewayEntry, "ggateway", attrs);
        System.out.println("");
        continue;
      }
      System.out.println("Gateway OK");
      System.out.println("");

      _gatewayAddr = gatewayHost;
      _gatewayPort = gatewayPort;
      return;
    }
    System.err.println("no suitable gateway registration entries found");
    throw new GatewayNotFoundException (
      "no suitable gateway registration entry in GIDS");
  }

  public void print()
  {
    System.out.println("mode " + _translatorMode);
    System.out.println("debug level: " + _debugLevel);
    System.out.println("globe host: " + _hostPort);

    if (_gatewayAddr != null) {
      System.out.println("gateway addr: " + _gatewayAddr);
      System.out.println("gateway port: " + _gatewayPort);
    }
    System.out.println("gateway connection timeout: " + _gatewayTimeout);

    System.out.println("threadpool size: " + _threadPoolSize);
    System.out.println("client connection timeout: " + _clientTimeout);
    System.out.println("HTTP server connection timeout: " + _httpServerTimeout);

    if (_httpProxyAddr != null) {
      System.out.println("proxy addr: " + _httpProxyAddr);
      System.out.println("proxy port: " + _httpProxyPort);
    }

    if (_defURL != null) {
      System.out.println("default URL: " + _defURL);
    }
    else {
      System.out.println("default URL: (none)");
    }

    System.out.println("enable access log: " + _accessLogEnabledFlag);
    System.out.println("access log: " + _accessLogFileName);
    System.out.println("error log: " + _errorLogFileName);
  }
}
