/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

// CfgFileReader.java

package vu.globe.svcs.gtrans.cfg;


import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownServiceException;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.StringTokenizer;


/**
 * This class represents a reader for a Globe translator configuration file.
 */
public class CfgFileReader
{
  /**
   * Keyword identifiers.
   */
  public static final int SYM_DEBUGLEVEL         = 1;
  public static final int SYM_TRANSLATOR_MODE    = 2;
  public static final int SYM_GLOBE_HOST         = 3;
  public static final int SYM_MAX_CLIENTS        = 4;
  public static final int SYM_CLIENT_TIMEOUT     = 5;
  public static final int SYM_GATEWAY_TIMEOUT    = 6;
  public static final int SYM_HTTPSERVER_TIMEOUT = 7;
  public static final int SYM_GATEWAY            = 8;
  public static final int SYM_HTTPPROXY          = 9;
  public static final int SYM_ACCESSLOG          = 10;
  public static final int SYM_ERRORLOG           = 11;
  public static final int SYM_ENABLE_ACCESSLOG   = 12;
  public static final int SYM_DEFAULT_URL        = 13;

  /**
   * The keywords.
   */
  public static final String SYMSTR_DEBUGLEVEL         = "DebugLevel";
  public static final String SYMSTR_TRANSLATOR_MODE    = "TranslatorMode";
  public static final String SYMSTR_GLOBE_HOST         = "GlobeHost";
  public static final String SYMSTR_MAX_CLIENTS        = "MaxClients";
  public static final String SYMSTR_CLIENT_TIMEOUT     = "ClientTimeout";
  public static final String SYMSTR_GATEWAY_TIMEOUT    = "GatewayTimeout";
  public static final String SYMSTR_HTTPSERVER_TIMEOUT = "HttpServerTimeout";
  public static final String SYMSTR_GATEWAY            = "Gateway";
  public static final String SYMSTR_HTTPPROXY          = "HttpProxy";
  public static final String SYMSTR_ACCESSLOG          = "AccessLog";
  public static final String SYMSTR_ERRORLOG           = "ErrorLog";
  public static final String SYMSTR_ENABLE_ACCESSLOG   = "EnableAccessLog";
  public static final String SYMSTR_DEFAULT_URL        = "DefaultURL";

  /** Lines starting with the following character are treated as comment. */
  public static final char COMMENTCHAR = '#';

  /**
   * Symbolic versions of the translator operation modes.
   */
  private static final String MODESTR_SERVER = "server";
  private static final String MODESTR_PROXY = "proxy";
  private static final String MODESTR_PROXYSERVER = "proxyserver";

  private static Hashtable _symbols;
  private int _lineno;


  /**
   * Instance creation.
   */
  public CfgFileReader()
  {
    _symbols = new Hashtable();

    addSymbol(SYM_DEBUGLEVEL, SYMSTR_DEBUGLEVEL);
    addSymbol(SYM_TRANSLATOR_MODE, SYMSTR_TRANSLATOR_MODE);
    addSymbol(SYM_GLOBE_HOST, SYMSTR_GLOBE_HOST);
    addSymbol(SYM_MAX_CLIENTS, SYMSTR_MAX_CLIENTS);
    addSymbol(SYM_CLIENT_TIMEOUT, SYMSTR_CLIENT_TIMEOUT);
    addSymbol(SYM_GATEWAY_TIMEOUT, SYMSTR_GATEWAY_TIMEOUT);
    addSymbol(SYM_HTTPSERVER_TIMEOUT, SYMSTR_HTTPSERVER_TIMEOUT);
    addSymbol(SYM_GATEWAY, SYMSTR_GATEWAY);
    addSymbol(SYM_HTTPPROXY, SYMSTR_HTTPPROXY);
    addSymbol(SYM_ACCESSLOG, SYMSTR_ACCESSLOG);
    addSymbol(SYM_ERRORLOG, SYMSTR_ERRORLOG);
    addSymbol(SYM_ENABLE_ACCESSLOG, SYMSTR_ENABLE_ACCESSLOG);
    addSymbol(SYM_DEFAULT_URL, SYMSTR_DEFAULT_URL);
  }


  /**
   * Add a symbol to the table.
   */
  private void addSymbol(int id, String name)
  {
    _symbols.put(name, new Symbol(id, name));
  }


  /**
   * Read a configuration file.
   *
   * @param  fname  file's URI name
   * @param  cfg    configuration object to store the settings in
   *
   * @exception  CfgFileException       if the file is corrupted
   * @exception  FileNotFoundException  if the file is not found
   * @exception  IOException            if an I/O error occurs
   */
  public void read(String fname, TranslatorConfiguration cfg)
    throws CfgFileException, FileNotFoundException, IOException
  {

    BufferedReader in = null;

    URL uri = new URL(fname);
    try {
      URLConnection con = uri.openConnection();
      con.connect();
      in = new BufferedReader(new InputStreamReader(con.getInputStream()));
    } catch (UnknownServiceException e) {
      throw new IOException("Cannot read from '"+fname+"'.");
    }

    try {
      readFile(in, cfg);
    }
    finally {
      if (in != null) {
        in.close();
      }
    }
  }

  
  /**
   * Read a configuration file from a buffered reader. Assumes that
   * the first line to be read corresponds to line number 1.
   *
   * @param  in   buffered reader to read from
   * @param  cfg  configuration object to store the settings in
   *
   * @exception  CfgFileException  if the file is corrupted
   * @exception  IOException       if an I/O error occurs
   */ 
  private void readFile(BufferedReader in, TranslatorConfiguration cfg)
    throws CfgFileException, IOException
  {
    String line;
    StringTokenizer st;
    String s, t, kw, ext;
    int i;
    Symbol sym;

    _lineno = 0;
    while ( (line = in.readLine()) != null) {
      _lineno++;

      line = line.trim();

      /*
       * Skip empty lines and comment.
       */
      if (line.length() == 0 || line.charAt(0) == COMMENTCHAR) {
        continue;
      }

      st = new StringTokenizer(line);

      if (st.hasMoreTokens() == false) {
        continue;
      }

      kw = st.nextToken();

      if ( (sym = (Symbol)_symbols.get(kw)) == null) {
        errorAt("unknown keyword `" + kw + "'");
      }

      switch(sym.id) {
        case SYM_DEBUGLEVEL:
          if ( (s = getNextToken(st)) == null) {
            errorAt(SYMSTR_DEBUGLEVEL + " value expected");
          }
          try {
            cfg.setDebugLevel(Integer.parseInt(s));
          }
          catch(NumberFormatException e) {
            errorAt("`" + s + "' is not an integer");
          }
          checkNewLine(st);
          break;

        case SYM_TRANSLATOR_MODE:
          if ( (s = getNextToken(st)) == null) {
            errorAt(SYMSTR_TRANSLATOR_MODE + " value expected");
          }

          if (s.equals(MODESTR_SERVER)) {
            cfg.setTranslatorMode(TranslatorConfiguration.MODE_SERVER);
          }
          else if (s.equals(MODESTR_PROXY)) {
            cfg.setTranslatorMode(TranslatorConfiguration.MODE_PROXY);
          }
          else if (s.equals(MODESTR_PROXYSERVER)) {
            cfg.setTranslatorMode(TranslatorConfiguration.MODE_PROXYSERVER);
          }
          else {
            errorAt("invalid " + SYMSTR_TRANSLATOR_MODE + " value");
          }
          checkNewLine(st);
          break;

        case SYM_GLOBE_HOST:
          if ( (s = getNextToken(st)) == null) {
            errorAt("Globe host value expected");
          }
          cfg.setHostPort(s);
          checkNewLine(st);
          break;

        case SYM_MAX_CLIENTS:
          if ( (s = getNextToken(st)) == null) {
            errorAt(SYMSTR_MAX_CLIENTS + " value expected");
          }
          try {
            cfg.setThreadPoolSize(Integer.parseInt(s));
          }
          catch(NumberFormatException e) {
            errorAt("`" + s + "' is not an integer");
          }
          catch(IllegalArgumentException e) {
            errorAt("value out of range");
          }
          checkNewLine(st);
          break;

        case SYM_CLIENT_TIMEOUT:
          if ( (s = getNextToken(st)) == null) {
            errorAt("timeout value expected");
          }
          try {
            cfg.setClientTimeout(Integer.parseInt(s));
          }
          catch(NumberFormatException e) {
            errorAt("`" + s + "' is not an integer");
          }
          catch(IllegalArgumentException e) {
            errorAt("timeout value out of range");
          }
          checkNewLine(st);
          break;

        case SYM_GATEWAY_TIMEOUT:
          if ( (s = getNextToken(st)) == null) {
            errorAt("timeout value expected");
          }
          try {
            cfg.setGatewayTimeout(Integer.parseInt(s));
          }
          catch(NumberFormatException e) {
            errorAt("`" + s + "' is not an integer");
          }
          catch(IllegalArgumentException e) {
            errorAt("timeout value out of range");
          }
          break;

        case SYM_HTTPSERVER_TIMEOUT:
          if ( (s = getNextToken(st)) == null) {
            errorAt("timeout value expected");
          }
          try {
            cfg.setHttpServerTimeout(Integer.parseInt(s));
          }
          catch(NumberFormatException e) {
            errorAt("`" + s + "' is not an integer");
          }
          catch(IllegalArgumentException e) {
            errorAt("timeout value out of range");
          }
          break;

        case SYM_GATEWAY:
          if ( (s = getNextToken(st)) == null) {
            errorAt("gateway hostname expected");
          }
          if ( (t = getNextToken(st)) == null) {
            errorAt("gateway port number expected");
          }

          try {
            cfg.setGatewayAddress(InetAddress.getByName(s));
          }
          catch(UnknownHostException e) {
            errorAt(s + ": unknown host");
          }

          try {
            cfg.setGatewayPort(Integer.parseInt(t));
          }
          catch(NumberFormatException e) {
            errorAt("`" + t + "' is not an integer");
          }
          catch(IllegalArgumentException e) {
            errorAt("port number out of range");
          }
          checkNewLine(st);
          break;

        case SYM_HTTPPROXY:
          if ( (s = getNextToken(st)) == null) {
            errorAt("HTTP proxy hostname expected");
          }
          if ( (t = getNextToken(st)) == null) {
            errorAt("HTTP proxy port number expected");
          }

          try {
            cfg.setHttpProxyAddress(InetAddress.getByName(s));
          }
          catch(UnknownHostException e) {
            errorAt(s + ": unknown host");
          }

          try {
            cfg.setHttpProxyPort(Integer.parseInt(t));
          }
          catch(NumberFormatException e) {
            errorAt("`" + t + "' is not an integer");
          }
          catch(IllegalArgumentException e) {
            errorAt("port number out of range");
          }
          checkNewLine(st);
          break;

        case SYM_ENABLE_ACCESSLOG:
          if ( (s = getNextToken(st)) == null) {
            errorAt(SYMSTR_ENABLE_ACCESSLOG + " value expected");
          }
          if (isOn(s)) {
            cfg.setAccessLogEnabledFlag(true);
          }
          else if (isOff(s)) {
            cfg.setAccessLogEnabledFlag(false);
          }
          else {
            errorAt("invalid " + SYMSTR_ENABLE_ACCESSLOG + " value");
          }
          checkNewLine(st);
          break;

        case SYM_DEFAULT_URL:
          s = getNextToken(st);                    // may be null
          cfg.setDefaultURL(s);
          checkNewLine(st);
          break;

        case SYM_ACCESSLOG:
          if ( (s = getNextToken(st)) == null) {
            errorAt("file name expected");
          }

          checkFile(s, false);

          cfg.setAccessLogFileName(s);
          checkNewLine(st);
          break;

        case SYM_ERRORLOG:
          if ( (s = getNextToken(st)) == null) {
            errorAt("file name expected");
          }

          checkFile(s, false);

          cfg.setErrorLogFileName(s);
          checkNewLine(st);
          break;

        default:
          throw new CfgFileException("internal error -- unknown symbol `"
                                     + sym.sym + "'");
      }
    }
  }


  /**
   * Return <code>true</code> if the given string refers to the boolean
   * `on' value; <code>false</code> otherwise.
   */
  private boolean isOn(String s)
  {
    return s.equalsIgnoreCase("on")
           || s.equalsIgnoreCase("yes")
           || s.equalsIgnoreCase("true");
  }


  /**
   * Return <code>true</code> if the given string refers to the boolean
   * `off' value; <code>false</code> otherwise.
   */
  private boolean isOff(String s)
  {
    return s.equalsIgnoreCase("off")
           || s.equalsIgnoreCase("no")
           || s.equalsIgnoreCase("false");
  }


  /**
   * Throw a <code>CfgFileException</code>. Its detail message includes
   * the current line number and <code>s</code>.
   */
  private void errorAt(String s)
    throws CfgFileException
  {
    throw new CfgFileException("line " + _lineno + ": " + s);
  }


  /**
   * Check if a string tokenizer has no more tokens. Throw an exception
   * if there is not the case.
   */
  private void checkNewLine(StringTokenizer st)
    throws CfgFileException
  {
    if (st.hasMoreTokens()) {
      errorAt(st.nextToken() + ": new line expected");
    }
  }


  /**
   * Check if a file is normal, if it exists.
   *
   * @param  fname   name of the file to be checked
   * @param  exists  if set, the file should exist
   *
   * @exception  CfgFileException  if the file is not normal
   */
  private void checkFile(String fname, boolean exists)
    throws CfgFileException
  {
    File f = new File(fname);

    if (f.exists() == false) {
      if (exists) {
        errorAt(fname + ": file does not exist");
      }
    }
    else if (f.isFile() == false) {
      if (f.isDirectory()) {
        errorAt(fname + ": is a directory");
      }
      else {
        errorAt(fname + ": not a regular file");
      }
    }
  }


  /**
   * Return the next token from a string tokenizer; <code>null</code> if
   * there is no token.
   */
  private String getNextToken(StringTokenizer st)
  {
    if (st.hasMoreTokens()) {
      return st.nextToken();
    }
    return null;
  }


  /**
   * This class represents a symbol. A symbol has a name and a numerical
   * identifier.
   */
  private class Symbol
  {
    String sym;
    int id;

    public Symbol(int id, String sym)
    {
      this.id = id;
      this.sym = sym;
    }
  }
}
/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

// CfgFileReader.java

package vu.globe.svcs.gred.cfg;


import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownServiceException;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.StringTokenizer;


/**
 * This class represents a reader for a Globe redirector configuration file.
 */
public class CfgFileReader
{
  /**
   * Keyword identifiers.
   */
  public static final int SYM_CLIENT_TIMEOUT      = 1;
  public static final int SYM_ACCESSLOG           = 2;
  public static final int SYM_ERRORLOG            = 3;
  public static final int SYM_DEBUGLEVEL          = 4;
  public static final int SYM_MAX_CLIENTS         = 5;
  public static final int SYM_DEFAULT_GAP         = 6;
  public static final int SYM_DEFAULT_GAP_COORDS  = 7;
  public static final int SYM_GAPLIST_URL         = 8;
  public static final int SYM_GAPLIST_REFRESH     = 9;
  public static final int SYM_ENABLE_COOKIE       = 10;
  public static final int SYM_COOKIE_DOMAIN       = 11; 
  public static final int SYM_COOKIE_PATH         = 12; 
  public static final int SYM_GAP_COOKIE_TTL      = 13; 
  public static final int SYM_LOCATION_COOKIE_TTL = 14; 
  public static final int SYM_IP2GAPCACHE_SIZE    = 15;
  public static final int SYM_IP2GAPCACHE_TTL     = 16;
  public static final int SYM_DB_FNAME            = 17;
  public static final int SYM_DB_TYPE             = 18;
  public static final int SYM_DB_SYNCWRITES       = 19;
  public static final int SYM_ENABLE_ACCESSLOG    = 20;
  public static final int SYM_HTTP_EXPIRES        = 21;
  public static final int SYM_HTTP_REDIRECT       = 22;
  public static final int SYM_DEFAULT_URL         = 23;
  public static final int SYM_BLOCKLIST           = 24;

  /**
   * The keywords.
   */
  public static final String SYMSTR_CLIENT_TIMEOUT      = "ClientTimeout";
  public static final String SYMSTR_ACCESSLOG           = "AccessLog";
  public static final String SYMSTR_ERRORLOG            = "ErrorLog";
  public static final String SYMSTR_DEBUGLEVEL          = "DebugLevel";
  public static final String SYMSTR_MAX_CLIENTS         = "MaxClients";
  public static final String SYMSTR_DEFAULT_GAP         = "DefaultGap";
  public static final String SYMSTR_DEFAULT_GAP_COORDS="DefaultGapCoordinates";
  public static final String SYMSTR_GAPLIST_URL         = "GapList";
  public static final String SYMSTR_GAPLIST_REFRESH     = "GapListRefresh";
  public static final String SYMSTR_ENABLE_COOKIE       = "EnableCookie";
  public static final String SYMSTR_COOKIE_DOMAIN       = "CookieDomain";
  public static final String SYMSTR_COOKIE_PATH         = "CookiePath";
  public static final String SYMSTR_GAP_COOKIE_TTL      = "GapCookieTTL";
  public static final String SYMSTR_LOCATION_COOKIE_TTL = "LocationCookieTTL";
  public static final String SYMSTR_IP2GAPCACHE_SIZE    = "IP2GAPCacheSize";
  public static final String SYMSTR_IP2GAPCACHE_TTL     = "IP2GAPCacheTTL";
  public static final String SYMSTR_DB_FNAME            = "GeoDB";
  public static final String SYMSTR_DB_TYPE             = "GeoDBType";
  public static final String SYMSTR_DB_SYNCWRITES       = "GeoDBSyncWrites";
  public static final String SYMSTR_ENABLE_ACCESSLOG    = "EnableAccessLog";
  public static final String SYMSTR_HTTP_EXPIRES        = "HttpExpires";
  public static final String SYMSTR_HTTP_REDIRECT       = "HttpRedirect";
  public static final String SYMSTR_DEFAULT_URL         = "DefaultURL";
  public static final String SYMSTR_BLOCKLIST           = "BlockList";

  /** Lines starting with the following character are treated as comment. */
  public static final char COMMENTCHAR = '#';

  private static Hashtable _symbols;
  private int _lineno;


  /**
   * Instance creation.
   */
  public CfgFileReader()
  {
    _symbols = new Hashtable();

    addSymbol(SYM_CLIENT_TIMEOUT, SYMSTR_CLIENT_TIMEOUT);
    addSymbol(SYM_ACCESSLOG, SYMSTR_ACCESSLOG);
    addSymbol(SYM_ERRORLOG, SYMSTR_ERRORLOG);
    addSymbol(SYM_DEBUGLEVEL, SYMSTR_DEBUGLEVEL);
    addSymbol(SYM_MAX_CLIENTS, SYMSTR_MAX_CLIENTS);
    addSymbol(SYM_DEFAULT_GAP, SYMSTR_DEFAULT_GAP);
    addSymbol(SYM_DEFAULT_GAP_COORDS, SYMSTR_DEFAULT_GAP_COORDS);
    addSymbol(SYM_GAPLIST_URL, SYMSTR_GAPLIST_URL);
    addSymbol(SYM_GAPLIST_REFRESH, SYMSTR_GAPLIST_REFRESH);
    addSymbol(SYM_ENABLE_COOKIE, SYMSTR_ENABLE_COOKIE);
    addSymbol(SYM_COOKIE_DOMAIN, SYMSTR_COOKIE_DOMAIN);
    addSymbol(SYM_COOKIE_PATH, SYMSTR_COOKIE_PATH);
    addSymbol(SYM_GAP_COOKIE_TTL, SYMSTR_GAP_COOKIE_TTL);
    addSymbol(SYM_LOCATION_COOKIE_TTL, SYMSTR_LOCATION_COOKIE_TTL);
    addSymbol(SYM_IP2GAPCACHE_SIZE, SYMSTR_IP2GAPCACHE_SIZE);
    addSymbol(SYM_IP2GAPCACHE_TTL, SYMSTR_IP2GAPCACHE_TTL);
    addSymbol(SYM_DB_FNAME, SYMSTR_DB_FNAME);
    addSymbol(SYM_DB_TYPE, SYMSTR_DB_TYPE);
    addSymbol(SYM_DB_SYNCWRITES, SYMSTR_DB_SYNCWRITES);
    addSymbol(SYM_ENABLE_ACCESSLOG, SYMSTR_ENABLE_ACCESSLOG);
    addSymbol(SYM_HTTP_EXPIRES, SYMSTR_HTTP_EXPIRES);
    addSymbol(SYM_HTTP_REDIRECT, SYMSTR_HTTP_REDIRECT);
    addSymbol(SYM_DEFAULT_URL, SYMSTR_DEFAULT_URL);
    addSymbol(SYM_BLOCKLIST, SYMSTR_BLOCKLIST);
  }


  /**
   * Add a symbol to the table.
   */
  private void addSymbol(int id, String name)
  {
    _symbols.put(name, new Symbol(id, name));
  }


  /**
   * Read a configuration file.
   *
   * @param  fname  file's URI name
   * @param  cfg    configuration object to store the settings in
   *
   * @exception  CfgFileException       if the file is corrupted
   * @exception  FileNotFoundException  if the file is not found
   * @exception  IOException            if an I/O error occurs
   */
  public void read(String fname, RedirectorConfiguration cfg)
    throws CfgFileException, FileNotFoundException, IOException
  {
    BufferedReader in = null;

    URL uri = new URL(fname);
    try {
      URLConnection con = uri.openConnection();
      con.connect();
      in = new BufferedReader(new InputStreamReader(con.getInputStream()));
    } catch (UnknownServiceException e) {
      throw new IOException("Cannot read from '"+fname+"'.");
    }

    try {
      readFile(in, cfg);
    }
    finally {
      if (in != null) {
        in.close();
      }
    }
  }

  
  /**
   * Read a configuration file from a buffered reader. Assumes that
   * the first line to be read corresponds to line number 1.
   *
   * @param  in   buffered reader to read from
   * @param  cfg  configuration object to store the settings in
   *
   * @exception  CfgFileException  if the file is corrupted
   * @exception  IOException       if an I/O error occurs
   */ 
  private void readFile(BufferedReader in, RedirectorConfiguration cfg)
    throws CfgFileException, IOException
  {
    String line;
    StringTokenizer st;
    String s, t, kw, ext;
    int i;
    Symbol sym;

    _lineno = 0;
    while ( (line = in.readLine()) != null) {
      _lineno++;

      line = line.trim();

      /*
       * Skip empty lines and comment.
       */
      if (line.length() == 0 || line.charAt(0) == COMMENTCHAR) {
        continue;
      }

      st = new StringTokenizer(line);

      if (st.hasMoreTokens() == false) {
        continue;
      }

      kw = st.nextToken();

      if ( (sym = (Symbol)_symbols.get(kw)) == null) {
        errorAt("unknown keyword `" + kw + "'");
      }

      switch(sym.id) {
        case SYM_CLIENT_TIMEOUT:
          if ( (s = getNextToken(st)) == null) {
            errorAt("timeout value expected");
          }
          try {
            cfg.setClientTimeout(Integer.parseInt(s));
          }
          catch(NumberFormatException e) {
            errorAt("`" + s + "' is not an integer");
          }
          catch(IllegalArgumentException e) {
            errorAt("timeout value out of range");
          }
          checkNewLine(st);
          break;

        case SYM_ACCESSLOG:
          if ( (s = getNextToken(st)) == null) {
            errorAt("file name expected");
          }

          checkFile(s, false);

          cfg.setAccessLogFileName(s);
          checkNewLine(st);
          break;

        case SYM_ERRORLOG:
          if ( (s = getNextToken(st)) == null) {
            errorAt("file name expected");
          }

          checkFile(s, false);

          cfg.setErrorLogFileName(s);
          checkNewLine(st);
          break;

        case SYM_DEBUGLEVEL:
          if ( (s = getNextToken(st)) == null) {
            errorAt(SYMSTR_DEBUGLEVEL + " value expected");
          }
          try {
            cfg.setDebugLevel(Integer.parseInt(s));
          }
          catch(NumberFormatException e) {
            errorAt("`" + s + "' is not an integer");
          }
          checkNewLine(st);
          break;

        case SYM_MAX_CLIENTS:
          if ( (s = getNextToken(st)) == null) {
            errorAt(SYMSTR_MAX_CLIENTS + " value expected");
          }
          try {
            cfg.setThreadPoolSize(Integer.parseInt(s));
          }
          catch(NumberFormatException e) {
            errorAt("`" + s + "' is not an integer");
          }
          catch(IllegalArgumentException e) {
            errorAt("value out of range");
          }
          checkNewLine(st);
          break;

        case SYM_DEFAULT_GAP:
          if ( (s = getNextToken(st)) == null) {
            errorAt("GAP hostname expected");
          }
          if ( (t = getNextToken(st)) == null) {
            errorAt("GAP port number expected");
          }

          try {
            cfg.setDefaultGAPAddress(s);
          }
          catch(UnknownHostException e) {
            errorAt(s + ": unknown host");
          }

          try {
            cfg.setDefaultGAPPort(Integer.parseInt(t));
          }
          catch(NumberFormatException e) {
            errorAt("`" + t + "' is not an integer");
          }
          catch(IllegalArgumentException e) {
            errorAt("port number out of range");
          }
          checkNewLine(st);
          break;

        case SYM_DEFAULT_GAP_COORDS:
          if ( (s = getNextToken(st)) == null) {
            errorAt("latitude coordinate expected");
          }
          if ( (t = getNextToken(st)) == null) {
            errorAt("longitude coordinate expected");
          }

          int lat = 0, longi = 0;

          try {
            lat = Integer.parseInt(s);
          }
          catch(NumberFormatException e) {
            errorAt("`" + s + "' is not an integer");
          }

          try {
            longi = Integer.parseInt(t);
          }
          catch(NumberFormatException e) {
            errorAt("`" + t + "' is not an integer");
          }

          cfg.setDefaultGAPCoordinates(lat, longi);
          checkNewLine(st);
          break;

        case SYM_GAPLIST_URL:
          if ( (s = getNextToken(st)) == null) {
            errorAt("URL expected");
          }
          cfg.setGAPListURL(s);
          checkNewLine(st);
          break;

        case SYM_GAPLIST_REFRESH:
          if ( (s = getNextToken(st)) == null) {
            errorAt(SYMSTR_GAPLIST_REFRESH + " value expected");
          }
          try {
            cfg.setGAPListRefreshInterval(Integer.parseInt(s));
          }
          catch(NumberFormatException e) {
            errorAt("`" + s + "' is not an integer");
          }
          catch(IllegalArgumentException e) {
            errorAt(SYMSTR_GAPLIST_REFRESH + " value out of range");
          }
          checkNewLine(st);
          break;

        case SYM_ENABLE_COOKIE:
          if ( (s = getNextToken(st)) == null) {
            errorAt(SYMSTR_ENABLE_COOKIE + " value expected");
          }
          if (isOn(s)) {
            cfg.setCookieEnabledFlag(true);
          }
          else if (isOff(s)) {
            cfg.setCookieEnabledFlag(false);
          }
          else {
            errorAt("invalid " + SYMSTR_ENABLE_COOKIE + " value");
          }
          checkNewLine(st);
          break;

        case SYM_COOKIE_DOMAIN:
          if ( (s = getNextToken(st)) == null) {
            errorAt("domain value expected");
          }
          cfg.setCookieDomain(s);
          checkNewLine(st);
          break;

        case SYM_COOKIE_PATH:
          if ( (s = getNextToken(st)) == null) {
            errorAt("path value expected");
          }
          cfg.setCookiePath(s);
          checkNewLine(st);
          break;

        case SYM_GAP_COOKIE_TTL:                           // FALTHROUGH

        case SYM_LOCATION_COOKIE_TTL:
          if ( (s = getNextToken(st)) == null) {
            errorAt("time-to-live value expected");
          }
          try {
            if (sym.id == SYM_GAP_COOKIE_TTL) {
              cfg.setGAPCookieTTL(Integer.parseInt(s));
            }
            else {
              cfg.setLocationCookieTTL(Long.parseLong(s));
            }
          }
          catch(NumberFormatException e) {
            errorAt("`" + s + "' is not an integer");
          }
          catch(IllegalArgumentException e) {
            errorAt("`" + s + "' time-to-live value out of range");
          }
          checkNewLine(st);
          break;

        case SYM_IP2GAPCACHE_SIZE:
          if ( (s = getNextToken(st)) == null) {
            errorAt(SYMSTR_IP2GAPCACHE_SIZE + " value expected");
          }
          try {
            cfg.setIP2GAPCacheSize(Integer.parseInt(s));
          }
          catch(NumberFormatException e) {
            errorAt("`" + s + "' is not an integer");
          }
          catch(IllegalArgumentException e) {
            errorAt("value out of range");
          }
          checkNewLine(st);
          break;

        case SYM_IP2GAPCACHE_TTL:
          if ( (s = getNextToken(st)) == null) {
            errorAt("time-to-live value expected");
          }
          try {
            cfg.setIP2GAPCacheTTL(Long.parseLong(s));
          }
          catch(NumberFormatException e) {
            errorAt("`" + s + "' is not an integer");
          }
          catch(IllegalArgumentException e) {
            errorAt("`" + s + "' time-to-live value out of range");
          }
          checkNewLine(st);
          break;

        case SYM_DB_FNAME:
          if ( (s = getNextToken(st)) == null) {
            errorAt("file name expected");
          }

          checkFile(s, false);

          cfg.setGeoDBFileName(s);
          checkNewLine(st);
          break;

        case SYM_DB_TYPE:
          if ( (s = getNextToken(st)) == null) {
            errorAt(SYMSTR_DB_TYPE + " value expected");
          }

          cfg.setGeoDBType(s);
          checkNewLine(st);
          break;

        case SYM_DB_SYNCWRITES:
          if ( (s = getNextToken(st)) == null) {
            errorAt(SYMSTR_DB_SYNCWRITES + " value expected");
          }
          if (isOn(s)) {
            cfg.setGeoDBSyncWritesFlag(true);
          }
          else if (isOff(s)) {
            cfg.setGeoDBSyncWritesFlag(false);
          }
          else {
            errorAt("invalid " + SYMSTR_DB_SYNCWRITES + " value");
          }
          checkNewLine(st);
          break;

        case SYM_ENABLE_ACCESSLOG:
          if ( (s = getNextToken(st)) == null) {
            errorAt(SYMSTR_ENABLE_ACCESSLOG + " value expected");
          }
          if (isOn(s)) {
            cfg.setAccessLogEnabledFlag(true);
          }
          else if (isOff(s)) {
            cfg.setAccessLogEnabledFlag(false);
          }
          else {
            errorAt("invalid " + SYMSTR_ENABLE_ACCESSLOG + " value");
          }
          checkNewLine(st);
          break;

        case SYM_HTTP_EXPIRES:
          if ( (s = getNextToken(st)) == null) {
            errorAt(SYMSTR_HTTP_EXPIRES + " value expected");
          }
          try {
            cfg.setHTTPExpires(Integer.parseInt(s));
          }
          catch(NumberFormatException e) {
            errorAt("`" + s + "' is not an integer");
          }
          catch(IllegalArgumentException e) {
            errorAt(SYMSTR_HTTP_EXPIRES + " value out of range");
          }
          checkNewLine(st);
          break;

        case SYM_HTTP_REDIRECT:
          if ( (s = getNextToken(st)) == null) {
            errorAt(SYMSTR_HTTP_REDIRECT + " value expected");
          }
          if (isOn(s)) {
            cfg.setHTTPRedirectFlag(true);
          }
          else if (isOff(s)) {
            cfg.setHTTPRedirectFlag(false);
          }
          else {
            errorAt("invalid " + SYMSTR_HTTP_REDIRECT + " value");
          }
          checkNewLine(st);
          break;

        case SYM_DEFAULT_URL:
          s = getNextToken(st);                  // may be null
          cfg.setDefaultURL(s);
          checkNewLine(st);
          break;

        case SYM_BLOCKLIST:
          if ( (s = getNextToken(st)) == null) {
            errorAt("file name expected");
          }
          cfg.setBlockListFileName(s);
          checkNewLine(st);
          break;

        default:
          throw new CfgFileException("internal error -- unknown symbol `"
                                     + sym.sym + "'");
      }
    }
  }


  /**
   * Return <code>true</code> if the given string refers to the boolean
   * `on' value; <code>false</code> otherwise.
   */
  private boolean isOn(String s)
  {
    return s.equalsIgnoreCase("on")
           || s.equalsIgnoreCase("yes")
           || s.equalsIgnoreCase("true");
  }


  /**
   * Return <code>true</code> if the given string refers to the boolean
   * `off' value; <code>false</code> otherwise.
   */
  private boolean isOff(String s)
  {
    return s.equalsIgnoreCase("off")
           || s.equalsIgnoreCase("no")
           || s.equalsIgnoreCase("false");
  }


  /**
   * Throw a <code>CfgFileException</code>. Its detail message includes
   * the current line number and <code>s</code>.
   */
  private void errorAt(String s)
    throws CfgFileException
  {
    throw new CfgFileException("line " + _lineno + ": " + s);
  }


  /**
   * Check if a string tokenizer has no more tokens. Throw an exception
   * if there is not the case.
   */
  private void checkNewLine(StringTokenizer st)
    throws CfgFileException
  {
    if (st.hasMoreTokens()) {
      errorAt(st.nextToken() + ": new line expected");
    }
  }


  /**
   * Check if a file is normal, if it exists.
   *
   * @param  fname   name of the file to be checked
   * @param  exists  if set, the file should exist
   *
   * @exception  CfgFileException  if the file is not normal
   */
  private void checkFile(String fname, boolean exists)
    throws CfgFileException
  {
    File f = new File(fname);

    if (f.exists() == false) {
      if (exists) {
        errorAt(fname + ": file does not exist");
      }
    }
    else if (f.isFile() == false) {
      if (f.isDirectory()) {
        errorAt(fname + ": is a directory");
      }
      else {
        errorAt(fname + ": not a regular file");
      }
    }
  }


  /**
   * Return the next token from a string tokenizer; <code>null</code> if
   * there is no token.
   */
  private String getNextToken(StringTokenizer st)
  {
    if (st.hasMoreTokens()) {
      return st.nextToken();
    }
    return null;
  }


  /**
   * This class represents a symbol. A symbol has a name and a numerical
   * identifier.
   */
  private class Symbol
  {
    String sym;
    int id;

    public Symbol(int id, String sym)
    {
      this.id = id;
      this.sym = sym;
    }
  }
}
