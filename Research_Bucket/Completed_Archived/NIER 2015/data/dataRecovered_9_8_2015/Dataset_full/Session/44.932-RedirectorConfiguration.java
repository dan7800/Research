/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

// RedirectorConfiguration.java

package vu.globe.svcs.gred.cfg;


import java.net.InetAddress;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import vu.globe.svcs.gls.types.Coordinate;
import vu.globe.svcs.gls.types.CoordinateImpl;

import vu.globe.svcs.gred.*;


/**
 * Container class for the redirector settings.
 */
public class RedirectorConfiguration
{
  /** Default host name. */
  public static final String DEF_HOSTNAME = null;

  /** Default host address. */
  public static final InetAddress DEF_HOSTADDR = null;

  /** Default listening port. */
  public static final int DEF_LISTENING_PORT = 80;

  /**
   * Default signature. To be used when sending a HTML reload page
   * to the client.
   */
  public static final String DEF_SIGNATURE = null;

  /** Default debug level. Level 0 disables debugging output. */
  public static final int DEF_DEBUGLEVEL = 0;

  /** Default file name of the access log. */
  public static final String DEF_ACCESSLOGFILE = "gred.access_log";

  /** Default value for enabling access logging. */
  public static final boolean DEF_ACCESSLOG_ENABLED_FLAG = true;

  /** Default file name of the error log. */
  public static final String DEF_ERRORLOGFILE = "gred.error_log";

  /**
   * Default URL. This URL is used if a request is sent to the
   * the redirector without an object name.
   */
  public static final String DEF_DEFURL = null;

  /** Default client connection timeout (sec). */
  public static final int DEF_CLIENT_TIMEOUT = 15;

  /** Default number of threads in the thread pool. */
  public static final int DEF_THREADPOOL_SIZE = 100;

  /** Default address and port number of the default GAP. */
  public static final InetAddress DEF_DEFGAP_ADDR = null;
  public static final String DEF_DEFGAP_HOST = null;
  public static final int DEF_DEFGAP_PORT = -1;

  /** Default latitude and longitude coordinates of the default GAP. */
  public static int DEF_DEFGAP_LATITUDE = 1;
  public static int DEF_DEFGAP_LONGITUDE = 1;
  public static CoordinateImpl DEF_DEFGAP_COORDS
    = new CoordinateImpl(DEF_DEFGAP_LATITUDE, DEF_DEFGAP_LONGITUDE);

  /** Default URL of the well-known GAP list. */
  public static final URL DEF_GAPLIST_URL = null;

  /** Default refresh interval of the well-known GAP list (sec). */
  public static final int DEF_GAPLIST_REFRESH = 1 * 24 * 3600;

  /** Default GAP. */
  public static final GlobeAccessPointRecord DEF_GAP = null;

  /** Default value of the cookie-support flag. */
  public static final boolean DEF_COOKIE_ENABLED_FLAG = true;

  /** Default path attribute of a redirector HTTP cookie. */
  public static final String DEF_COOKIE_PATH = "/";

  /** Default domain attribute of a redirector HTTP cookie. */
  public static final String DEF_COOKIE_DOMAIN = null;
    
  /** Default time-to-live value for redirector GAP HTTP cookies (sec). */
  public static final long DEF_GAP_COOKIE_TTL = 7*24*3600;

  /** Default time-to-live value for redirector location HTTP cookies (sec). */
  public static final long DEF_LOCATION_COOKIE_TTL = 30*24*3600;

  /** Default IP-to-GAP cache size. */
  public static final int DEF_IP2GAPCACHE_SIZE = 1000;

  /** Default IP-to-GAP cache TTL (sec). */
  public static final long DEF_IP2GAPCACHE_TTL = 12*3600;

  /** Default Geo database file name. */
  public static final String DEF_DB_FNAME = "gred.db";

  /** Default Geo database type. */
  public static final String DEF_DB_TYPE = "berkeleydb";

  /** Default value for synchronous Geo database writes. */
  public static final boolean DEF_DB_SYNCWRITES = false;

  /** Default value for the Expires field of a HTTP message that is returned
      to the client (sec). */
  public static final int DEF_HTTP_EXPIRES = 1 * 24 * 3600;

  /** Default value for sending a HTTP redirect message to the client. */
  public static final boolean DEF_HTTP_REDIRECT_FLAG = false;

  /** Default file name of the block-list. */
  public static final String DEF_BLOCKLIST_FNAME = null;


  private String _hostName;
  private InetAddress _hostAddr;
  private int _listeningPort;
  private String _signature;
  private int _debugLevel;
  private String _accessLogFileName;
  private String _errorLogFileName;
  private String _defURL;
  private InetAddress _defGAPAddr;
  private String _defGAPHost;
  private int _defGAPPort;
  private CoordinateImpl _defGAPCoords;
  private int _defGAPLatitude;
  private int _defGAPLongitude;
  private URL _gapListURL;
  private int _gapListRefresh;
  private GlobeAccessPointRecord _gap;
  private int _clientTimeout;
  private int _threadPoolSize;
  private boolean _cookieEnabledFlag;
  private String _cookieDomain;
  private String _cookiePath;
  private long _gapCookieTTL;
  private long _locationCookieTTL;
  private int _ip2GAPCacheSize;
  private long _ip2GAPCacheTTL;
  private String _dbFileName;
  private String _dbType;
  private boolean _dbSyncWritesFlag;
  private boolean _accessLogEnabledFlag;
  private int _httpExpires;
  private boolean _httpRedirectFlag;
  private String _blockListFileName;


  /**
   * Instance creation. Sets all settings to their default value.
   */
  public RedirectorConfiguration()
  {
    _hostName = DEF_HOSTNAME;
    _hostAddr = DEF_HOSTADDR;
    _listeningPort = DEF_LISTENING_PORT;
    _signature = DEF_SIGNATURE;
    _debugLevel = DEF_DEBUGLEVEL;
    _accessLogFileName = DEF_ACCESSLOGFILE;
    _errorLogFileName = DEF_ERRORLOGFILE;
    _defURL = DEF_DEFURL;
    _defGAPAddr = DEF_DEFGAP_ADDR;
    _defGAPHost = DEF_DEFGAP_HOST;
    _defGAPPort = DEF_DEFGAP_PORT;
    _defGAPLatitude = DEF_DEFGAP_LATITUDE;
    _defGAPLongitude = DEF_DEFGAP_LONGITUDE;
    _defGAPCoords = DEF_DEFGAP_COORDS;
    _gapListURL = DEF_GAPLIST_URL;
    _gapListRefresh = DEF_GAPLIST_REFRESH;
    _gap = DEF_GAP;
    _clientTimeout = DEF_CLIENT_TIMEOUT;
    _threadPoolSize = DEF_THREADPOOL_SIZE;
    _cookieEnabledFlag = DEF_COOKIE_ENABLED_FLAG;
    _cookieDomain = DEF_COOKIE_DOMAIN;
    _cookiePath = DEF_COOKIE_PATH;
    _gapCookieTTL = DEF_GAP_COOKIE_TTL;
    _locationCookieTTL = DEF_LOCATION_COOKIE_TTL;
    _ip2GAPCacheSize = DEF_IP2GAPCACHE_SIZE;
    _ip2GAPCacheTTL = DEF_IP2GAPCACHE_TTL;
    _dbFileName = DEF_DB_FNAME;
    _dbType = DEF_DB_TYPE;
    _dbSyncWritesFlag = DEF_DB_SYNCWRITES;
    _accessLogEnabledFlag = DEF_ACCESSLOG_ENABLED_FLAG;
    _httpExpires = DEF_HTTP_EXPIRES;
    _httpRedirectFlag = DEF_HTTP_REDIRECT_FLAG;
    _blockListFileName = DEF_BLOCKLIST_FNAME;
  }


  /**
   * Get the host name.
   */
  public String getHostName()
  {
    return _hostName;
  }


  /**
   * Set the host name.
   */
  public void setHostName(String name)
  {
    _hostName = name;
  }


  /**
   * Get the host address.
   */
  public InetAddress getHostAddress()
  {
    return _hostAddr;
  }


  /**
   * Set the host address.
   */
  public void setHostAddress(InetAddress addr)
  {
    _hostAddr = addr;
  }


  /**
   * Return the listening port number.
   */
  public int getListeningPort()
  {
    return _listeningPort;
  }


  /**
   * Set the listening port number.
   */
  public void setListeningPort(int port)
  {
    _listeningPort = port;
  }


  /**
   * Return the signature; <code>null</code> if not defined.
   */
  public String getSignature()
  {
    return _signature;
  }


  /**
   * Set the signature.
   */
  public void setSignature(String sig)
  {
    _signature = sig;
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
    _debugLevel = n;
  }



  /**
   * Return the host name of the default GAP.
   */
  public String getDefaultGAPHost()
  {
    return _defGAPHost;
  }
  
  
  /**
   * Return the address of the default GAP.
   */
  public InetAddress getDefaultGAPAddress()
  {
    return _defGAPAddr;
  }


  /**
   * Set the address of the default GAP.
   *
   * @exception IllegalArgumentException  if the address is <code>null</code>
   * @exception UnknownHostException      if the host is unknown
   */
  public void setDefaultGAPAddress(String host)
    throws IllegalArgumentException, UnknownHostException
  {
    if (host == null) {
      throw new IllegalArgumentException();
    }
    _defGAPAddr = InetAddress.getByName(host);
    _defGAPHost = host;
  }


  /**
   * Return the port number of the default GAP.
   */
  public int getDefaultGAPPort()
  {
    return _defGAPPort;
  }


  /**
   * Set the port number of the default GAP.
   *
   * @exception  IllegalArgumentException  if the port number is out of range
   */
  public void setDefaultGAPPort(int port)
    throws IllegalArgumentException
  {
    if (port <= 0) {
      throw new IllegalArgumentException();
    }
    _defGAPPort = port;
  }


  /**
   * Return the coordinates of the default GAP.
   */
  public CoordinateImpl getDefaultGAPCoordinates()
  {
    return _defGAPCoords;
  }


  /**
   * Set the latitude and longitude coordinates of the default GAP.
   */
  public void setDefaultGAPCoordinates(int latitude, int longitude)
  {
    _defGAPLatitude = latitude;
    _defGAPLongitude = longitude;
    _defGAPCoords = new CoordinateImpl(latitude, longitude);
  }


  /**
   * Return the GAP record of the default GAP.
   */
  public GlobeAccessPointRecord getDefaultGAP()
  {
    return _gap;
  }


  /**
   * Set the GAP record of the default GAP.
   *
   * @exception IllegalArgumentException  if the record is <code>null</code>
   */
  public void setDefaultGAP(GlobeAccessPointRecord gap)
    throws IllegalArgumentException
  {
    if (gap == null) {
      throw new IllegalArgumentException();
    }
    _gap = gap;
  }


  /**
   * Return the URL of the well-known GAP list.
   */
  public URL getGAPListURL()
  {
    return _gapListURL;
  }


  /**
   * Set the URL of the well-known GAP list.
   *
   * @exception  IllegalArgumentException  if the URL is malformed
   */
  public void setGAPListURL(String url)
    throws IllegalArgumentException
  {
    if (url != null) {
      try {
        _gapListURL = new URL(url);
      }
      catch(MalformedURLException e) {
        throw new IllegalArgumentException(e.getMessage());
      }
    }
  }


  /**
   * Return the GAP list refresh interval (seconds).
   */
  public int getGAPListRefreshInterval()
  {
    return _gapListRefresh;
  }


  /**
   * Set the GAP list refresh interval.
   *
   * @param  n  the interval (seconds)
   *
   * @exception  IllegalArgumentException  if the interval is out of range
   */
  public void setGAPListRefreshInterval(int n)
    throws IllegalArgumentException
  {
    if (n < 0) {
      throw new IllegalArgumentException();
    }
    _gapListRefresh = n;
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
   * Return <code>true</code> if HTTP cookies should be enabled;
   * <code>false</code> otherwise.
   */
  public boolean getCookieEnabledFlag()
  {
    return _cookieEnabledFlag;
  }


  /**
   * Enable or disable HTTP cookies.
   */
  public void setCookieEnabledFlag(boolean on)
  {
    _cookieEnabledFlag = on;
  }


  /**
   * Return the Path attribute to be used in redirector cookies.
   */
  public String getCookiePath()
  {
    return _cookiePath;
  }


  /**
   * Set the Path attribute to be used in redirector cookies.
   *
   * @exception  IllegalArgumentException  if the path is <code>null</code>
   */
  public void setCookiePath(String path)
    throws IllegalArgumentException
  {
    if (path == null) {
      throw new IllegalArgumentException();
    }
    _cookiePath = path;
  }


  /**
   * Return the Domain attribute to be used in redirector cookies.
   */
  public String getCookieDomain()
  {
    return _cookieDomain;
  }


  /**
   * Set the Domain attribute to be used in redirector cookies.
   *
   * @exception  IllegalArgumentException  if the domain is <code>null</code>
   */
  public void setCookieDomain(String domain)
    throws IllegalArgumentException
  {
    if (domain == null) {
      throw new IllegalArgumentException();
    }
    _cookieDomain = domain;
  }


  /**
   * Return the time-to-live value of GAP cookies (seconds).
   */
  public long getGAPCookieTTL()
  {
    return _gapCookieTTL;
  }


  /**
   * Set the time-to-live value of GAP cookies (seconds).
   *
   * @exception  IllegalArgumentException  if the value is out of range
   */
  public void setGAPCookieTTL(long ttl)
    throws IllegalArgumentException
  {
    if (ttl < 1) {
      throw new IllegalArgumentException();
    }
    _gapCookieTTL = ttl;
  }


  /**
   * Return the time-to-live value of location cookies (seconds).
   */
  public long getLocationCookieTTL()
  {
    return _locationCookieTTL;
  }


  /**
   * Set the time-to-live value of location cookies (seconds).
   *
   * @exception  IllegalArgumentException  if the value is out of range
   */
  public void setLocationCookieTTL(long ttl)
    throws IllegalArgumentException
  {
    if (ttl < 1) {
      throw new IllegalArgumentException();
    }
    _locationCookieTTL = ttl;
  }


  /**
   * Return the size of the IPaddress-to-GAPrecord cache.
   */
  public int getIP2GAPCacheSize()
  {
    return _ip2GAPCacheSize;
  }


  /**
   * Set the size of the IPaddress-to-GAPrecord cache.
   *
   * @exception  IllegalArgumentException  if the size is out of range
   */
  public void setIP2GAPCacheSize(int n)
    throws IllegalArgumentException
  {
    if (n < 0) {
      throw new IllegalArgumentException();
    }
    _ip2GAPCacheSize = n;
  }


  /**
   * Return the IP-to-GAP cache TTL (seconds).
   */
  public long getIP2GAPCacheTTL()
  {
    return _ip2GAPCacheTTL;
  }


  /**
   * Set the IPaddress-to-GAPrecord cache time-to-live value.
   *
   * @param  ttl  time-to-live value (seconds)
   *
   * @exception  IllegalArgumentException  if the value is out of range
   */
  public void setIP2GAPCacheTTL(long ttl)
    throws IllegalArgumentException
  {
    if (ttl < 0) {
      throw new IllegalArgumentException();
    }
    _ip2GAPCacheTTL = ttl;
  }


  /**
   * Return the name of the Geo database.
   */
  public String getGeoDBFileName()
  {
    return _dbFileName;
  }


  /**
   * Set the name of the Geo database.
   *
   * @exception  IllegalArgumentException  if the name is <code>null</code>
   */
  public void setGeoDBFileName(String name)
  {
    if (name == null) {
      throw new IllegalArgumentException();
    }
    _dbFileName = name;
  }


  /**
   * Return the Geo database type.
   */
  public String getGeoDBType()
  {
    return _dbType;
  }


  /**
   * Set the Geo database type.
   *
   * @exception  IllegalArgumentException  if the type is <code>null</code>
   */
  public void setGeoDBType(String type)
  {
    if (type == null) {
      throw new IllegalArgumentException();
    }
    _dbType = type;
  }


  /**
   * Return <code>true</code> if the Geo database should use synchronous
   * writes; <code>false</code> otherwise.
   */
  public boolean getGeoDBSyncWritesFlag()
  {
    return _dbSyncWritesFlag;
  }


  /**
   * Enable or disable synchronous writes for the Geo database.
   */
  public void setGeoDBSyncWritesFlag(boolean on)
  {
    _dbSyncWritesFlag = on;
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
   *
   * @exception  IllegalArgumentException  if the name is <code>null</code>
   */
  public void setAccessLogFileName(String name)
  {
    if (name == null) {
      throw new IllegalArgumentException();
    }
    _accessLogFileName = name;
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
   * Return the name of the error log.
   */
  public String getErrorLogFileName()
  {
    return _errorLogFileName;
  }


  /**
   * Set the name of the error log.
   *
   * @exception  IllegalArgumentException  if the name is <code>null</code>
   */
  public void setErrorLogFileName(String name)
  {
    if (name == null) {
      throw new IllegalArgumentException();
    }
    _errorLogFileName = name;
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
   * Return the value to be used for the HTTP Expires header (seconds).
   */
  public int getHTTPExpires()
  {
    return _httpExpires;
  }


  /**
   * Set the value to be used for the HTTP Expires header (seconds).
   */
  public void setHTTPExpires(int expires)
    throws IllegalArgumentException
  {
    if (expires < 0) {
      throw new IllegalArgumentException();
    }

    _httpExpires = expires;
  }


  /**
   * Return <code>true</code> if HTTP redirect messages should be sent;
   * <code>false</code> otherwise.
   */
  public boolean getHTTPRedirectFlag()
  {
    return _httpRedirectFlag;
  }


  /**
   * Return the file name of the block-list. 
   */
  public String getBlockListFileName()
  {
    return _blockListFileName;
  }


  /**
   * Set the name of the block-list.
   *
   * @exception  IllegalArgumentException  if the name is <code>null</code>
   */
  public void setBlockListFileName(String name)
  {
    if (name == null) {
      throw new IllegalArgumentException();
    }
    _blockListFileName = name;
  }


  /**
   * Verify that all mandatory settings are set.
   *
   * @exception  RuntimeException  if a setting is not set; the detail message
   *                               specifies the setting
   */
  public void check()
    throws RuntimeException
  {
    if (_accessLogEnabledFlag) {
      if (_accessLogFileName == null) {
        throw new RuntimeException("access log");
      }
    }

    if (_errorLogFileName == null) {
      throw new RuntimeException("error log");
    }

    if (_gap == null) {
      throw new RuntimeException("default GAP");
    }

    if (_cookieDomain == null) {
      throw new RuntimeException("cookie domain attribute");
    }

    if (_cookiePath == null) {
      throw new RuntimeException("cookie path attribute");
    }

    if (_dbFileName == null) {
      throw new RuntimeException("Geo database");
    }

    if (_dbType == null) {
      throw new RuntimeException("Geo database type");
    }
  }


  /**
   * Enable or disable HTTP redirect messages.
   */
  public void setHTTPRedirectFlag(boolean on)
  {
    _httpRedirectFlag = on;
  }


  public void print()
  {
    System.out.println("host name: " + _hostName);
    System.out.println("host address: " + _hostAddr);
    System.out.println("listening port: " + _listeningPort);
    System.out.println("debug level: " + _debugLevel);
    System.out.println("client connection timeout: " + _clientTimeout);
    System.out.println("threadpool size: " + _threadPoolSize);

    if (_defURL != null) {
      System.out.println("default URL: " + _defURL);
    }
    else {
      System.out.println("default URL: (none)");
    }

    if (_defGAPAddr != null) {
      System.out.println("default GAP addr: " + _defGAPAddr);
      System.out.println("default GAP port: " + _defGAPPort);
    }
    else {
      System.out.println("default GAP addr: (none)");
      System.out.println("default GAP port: (none)");
    }

    System.out.println("default GAP latitude, longitude: "
                       + _defGAPLatitude + ", " + _defGAPLongitude);

    System.out.println("GAP list URL: " + _gapListURL);
    System.out.println("GAP list refresh interval: " + _gapListRefresh);

    System.out.println("enable cookies: " + _cookieEnabledFlag);
    System.out.println("cookie domain: " + _cookieDomain);
    System.out.println("cookie path: " + _cookiePath);
    System.out.println("GAP cookie TTL: " + _gapCookieTTL);
    System.out.println("location cookie TTL: " + _locationCookieTTL);
    System.out.println("HTTP expires: " + _httpExpires);
    System.out.println("HTTP redirect: " + _httpRedirectFlag);

    System.out.println("ip2gap cache size: " + _ip2GAPCacheSize);
    System.out.println("ip2gap cache TTL: " + _ip2GAPCacheTTL);

    System.out.println("Geo db: "+ _dbFileName);
    System.out.println("Geo db type: " + _dbType);
    System.out.println("Geo sync writes: " + _dbSyncWritesFlag);

    System.out.println("enable access log: " + _accessLogEnabledFlag);
    System.out.println("access log: " + _accessLogFileName);
    System.out.println("error log: " + _errorLogFileName);
  }
}
