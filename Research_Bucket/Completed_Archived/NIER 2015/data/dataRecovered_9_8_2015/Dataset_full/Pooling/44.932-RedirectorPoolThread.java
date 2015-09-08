/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

// RedirectorPoolThread.java

package vu.globe.svcs.gred;


import java.io.*;
import java.net.*;
import java.util.Hashtable;
import java.util.Date;
import java.util.Random;

import vu.globe.svcs.gls.types.Coordinate;
import vu.globe.svcs.gls.types.CoordinateImpl;

import vu.globe.util.db.dbif.*;
import vu.globe.util.http.*;
import vu.globe.util.log.*;
import vu.globe.util.char8.DataInputStreamUtil;
import vu.globe.util.html.HTMLErrorPage;

import vu.globe.svcs.gred.cfg.*;
import vu.globe.svcs.gred.blocklist.*;
import vu.globe.svcs.gred.util.*;


/**
 * This class represents a Globe Redirector pool thread. Unlike it's name
 * suggests, this thread is used as a mini server, not as a thread that is
 * part of a thread pool. The thread waits in an infinite loop for HTTP
 * clients to connect. When a client connects, it redirects the client to
 * the client's nearest GAP (Globe Access point). Next, it waits for
 * another client to connect.
 */
public class RedirectorPoolThread
  extends Thread
{
  private static final boolean DEBUG = true;               // debug flag

  // The Cookie TTL value to be used when the client is redirected to a
  // random GAP (sec).
  private static final long RANDOM_GAP_COOKIE_TTL = 24 * 3600;

  // Maximum number of HTML links to put on the HTML reload page.
  private static final int MAX_HTML_LINKS = 20;


  // The configuration object of the redirector.
  private static RedirectorConfiguration _config = GlobeRedirector.config;

  private static BlockList _blockList = GlobeRedirector.blockList;

  /** `Factory' for dates in the common log file format. */
  private static CommonLogFileDateFormat _clfDateFormatter =
                                            new CommonLogFileDateFormat();

  /** `Factory' for dates in the HTTP date format. */
  private static HTTPDateFormat _httpDateFormatter = new HTTPDateFormat();

  /** `Factory' for HTTP redirector cookies. */
  public static RedirectorCookieFactory _cookieFactory =
           new RedirectorCookieFactory(GlobeRedirector.config.getCookiePath(),
                                    GlobeRedirector.config.getCookieDomain());

  /** Randomizer. */
  private Random _random = new Random();


  private ServerSocket _serverSock;   // server listening socket
  private int _debugLevel;            // debug level
  private long _id;                   // pool thread ID
  private GAPList _gapList;           // current GAP list

  // The following members are reused to avoid excessive object creation.
  private Request _req;               // client request
  private Date _date;                 // used to set dates
  private HTTPCookie _httpCookie;     // HTTP cookie from the client
  private StringBuffer _strBuf;       // string buffer
  private HTTPRespHdr _httpRespHdr;   // header for HTTP responses

  // The latitude/longitude coordinates of the client. When needed, these
  // coordinates are obtained from a) the client's cookie; or b) the
  // Geo database; or c) or the NetGeo server.
  private FloatCoordinate _cookieCoords;


  /**
   * Instance creation. Note that the specified GAP list represents the
   * GAP list at the time of the creation of this RedirectorPoolThread.
   *
   * @param  id          thread ID (for debugging purposes)
   * @param  group       the thread group to which this pool thread belongs
   * @param  serverSock  the server socket
   */
  public RedirectorPoolThread(long id, ThreadGroup group,
                              ServerSocket serverSock)
  {
    super(group, null, "RedirectorPoolThread:" + id);

    _id = id;
    _serverSock = serverSock;
    _debugLevel = _config.getDebugLevel();
    _gapList = null;
    _req = new Request();
    _date = new Date();
    _httpCookie = new HTTPCookie();
    _strBuf = new StringBuffer(1024);
    _httpRespHdr = new HTTPRespHdr();
    _cookieCoords = null;
  }


  /**
   * Wait for HTTP clients to connect, and then redirect them
   * to their nearest GAP. This method does not return.
   */
  public void run()
  {
    Connection conn;
    Socket clientSock;
    int timeout;

    conn = new Connection();
    timeout = _config.getClientTimeout() * 1000;

    for ( ; ; ) {
      /*
       * Wait for connection request.
       */
      try {
        clientSock = _serverSock.accept();
      }
      catch(IOException e) {
        logError("accept() failure: " + e.getMessage());
        continue;
      }

      try {
        clientSock.setSoTimeout(timeout);
      }
      catch(SocketException e) {
        logError("Cannot set timeout of client socket: " + e.getMessage()
                 + " -- closing socket");

        try {
          clientSock.close();
        }
        catch(IOException e2) {
          logError("Cannot close socket: " + e2.getMessage());
        }
        continue;
      }

      /*
       * `Create' a new connection.
       */
      try {
        conn.init(clientSock);
      }
      catch(IOException e) {
        logError("Cannot open client streams" + getExceptionMessage(e));
        try {
          clientSock.close();
        }
        catch(IOException e2) {
          logError("Cannot close client socket" + getExceptionMessage(e2));
        }
        continue;
      }

      if (DEBUG && _debugLevel > 0) {
        debugPrintLn("Reading HTTP Request...");
      }

      /*
       * `Create' a new container for the request.
       */
      setCurrentDate(_date);
      _req.init(conn, _clfDateFormatter.format(_date));

      // Get the current GAP list.
      _gapList = GlobeRedirector.getGAPList();

      processRequest(conn);

      /*
       * If access logging is enabled, write an entry to the access log.
       */
      if (_config.getAccessLogEnabledFlag()) {
        GlobeRedirector.accessLog.write(_req.getCommonLogFileStatistics());
      } 

      if (DEBUG && _debugLevel > 1) {
        debugPrintLn("Closing client connection");
      }

      try {
        conn.close();
      }
      catch(IOException e) {
        logError("Cannot close client connection" + getExceptionMessage(e));
      }

      /*
       * Release references that are no longer needed. To reduce the
       * memory footprint, these references are released now because it
       * may take a long time before this thread handles a new request
       * (in which case the references are released too).
       */
      _gapList = null;
      _req.clear();
      _httpRespHdr.clear();
      _cookieCoords = null;
    }
  }


  /**
   * Read the client request and process it.
   *
   * @param  conn  the connection associated with the request
   * @return <code>true</code>;
   *         <code>false</code> if the request was not processed successfully
   */
  private boolean processRequest(Connection conn)
  {
    String line = null;
    URL url;
    String path;

    /*
     * Read the request line.
     */
    try {
      line = DataInputStreamUtil.readLineIntr(conn.getInputStream());
    }
    catch(InterruptedException e) {
      logError("Cannot read request line: connection timeout");
      _req.sendHtmlErrorPage(HTTPStatusCode.BAD_REQUEST, "connection timeout");
      return false;
    }
    catch(IOException e) {
      logError("Cannot read request line: I/O error"
               + getExceptionMessage(e));
      _req.sendHtmlErrorPage(HTTPStatusCode.SRVCE_NOT_AVAILABLE,
                             "I/O error" + getExceptionMessage(e));
      return false;
    }

    /*
     * Parse the request line, and read the request headers.
     */
    try {
      _req.read(line);
    }
    catch(MalformedURLException e) {
      logError("Malformed URI in HTTP request" + getExceptionMessage(e));
      _req.sendHtmlErrorPage(HTTPStatusCode.BAD_REQUEST,
                             "Invalid URI in HTTP request"
                             + getExceptionMessage(e));
      return false;
    }
    catch(MalformedHTTPReqException e) {
      logError("Malformed HTTP request" + getExceptionMessage(e));
      _req.sendHtmlErrorPage(HTTPStatusCode.BAD_REQUEST,
                             "Invalid HTTP request" + getExceptionMessage(e));
      return false;
    }
    catch(InterruptedException e) {
      logError("Cannot read request: connection timeout");
      _req.sendHtmlErrorPage(HTTPStatusCode.BAD_REQUEST, "connection timeout");
      return false;
    }
    catch(IOException e) {
      logError("Cannot read request: I/O error" + getExceptionMessage(e));
      _req.sendHtmlErrorPage(HTTPStatusCode.SRVCE_NOT_AVAILABLE,
                             "I/O error" + getExceptionMessage(e));
      return false;
    }

    if (DEBUG && _debugLevel > 1) {
      debugPrintLn("Request: '" + _req.getReqLine() + "'");
    }

    /*
     * Process the request based on the request-URI type. The asterisk
     * option (RFC2068, sec. 5.1.2) is not supported.
     */
    if ( (path = _req.getPath()) != null) {
      return redirectClient(path);
    }
    else {
      if ( (url = _req.getURL()) != null) {
        // The redirector is being accessed as a proxy.

        _req.sendHtmlErrorPage(HTTPStatusCode.BAD_REQUEST,
                               "Cannot retrieve URL " + url
                               + "\n<P>\n"
                               + "Invalid URL: unexpected access protocol "
                               + "(e.g., `http://')\n");
      }
      else {
        logError("unsupported request-URI: " + _req.getReqLine());
        _req.sendHtmlErrorPage(HTTPStatusCode.BAD_REQUEST,
                               "unsupported request-URI");
      }
      return false;
    }
  }


  /**
   * Send a HTTP message to the client which redirects the client to
   * nearest GAP.
   *
   * @param  path  the path name inside the client's HTTP request
   * @return <code>true</code>;
   *         <code>false</code> if an error occurred
   */
  private boolean redirectClient(String path)
  {
    URL url;
    String reqHost = _req.requestHdr.getHeader("Host");
    String fullspec = "http://" + reqHost + path;
    String location = null;
    String gapAddress = null;
    boolean randomGAP = false;
    boolean haveValidGAPCookie = false;
    boolean haveValidLocationCookie = false;
    String s = null;

    InetAddress cliAddr = _req.connection.getSocket().getInetAddress();

    if (DEBUG && _debugLevel > 0) {
     debugPrintLn("Got request from " + cliAddr.toString() + " "
                  + _req.requestHdr.getVersion() );
    }

    /*
     * Check if the client's host is blocked.
     */
    if (_blockList.isBlockedHost(cliAddr.getHostAddress())) {
      _req.sendHtmlErrorPage(HTTPStatusCode.FORBIDDEN,
         "Not allowed: your host is blocked by the Globe redirector.");
      return false;
    }

    try {
      s = URIDecoder.decode(path);
    }
    catch(IllegalArgumentException e) {
      _req.sendHtmlErrorPage(HTTPStatusCode.BAD_REQUEST,
         "Format error: request contains an invalid escape sequence: "
         + e.getMessage());
      return false;
    }

    /*
     * Check if the file requested is blocked.
     */
    if (_blockList.isBlockedFile(s)) {
      _req.sendHtmlErrorPage(HTTPStatusCode.FORBIDDEN,
         "Not allowed: the requested file is blocked by the Globe redirector.");
      return false;
    }

    try {
      s = URIDecoder.decode(fullspec);
    }
    catch(IllegalArgumentException e) {
      _req.sendHtmlErrorPage(HTTPStatusCode.BAD_REQUEST,
         "Format error: request contains an invalid escape sequence: "
         + e.getMessage());
      return false;
    }

    /*
     * Check if the URL requested is blocked.
     */
    if (_blockList.isBlockedURL(s)) {
      _req.sendHtmlErrorPage(HTTPStatusCode.FORBIDDEN,
         "Not allowed: the requested URL is blocked by the Globe redirector.");
      return false;
    }

    try {
      url = new URL(fullspec);
    }
    catch( MalformedURLException e ) {
      logError("Unsupported request-URI: " + fullspec);
      _req.sendHtmlErrorPage(HTTPStatusCode.BAD_REQUEST,
                             "unsupported request-URI");
      return false;
    }

    String file = url.getFile();

    /*
     * Redirect the client to the default URL (if defined) if the
     * object name is absent.
     */
    if (file.equals("/")) {
      s = _config.getDefaultURL();

      if (s != null) {
        if (DEBUG && _debugLevel > 1) {
          debugPrintLn("no object name specified -- using default URL");
        }

        return sendHTTPRedirectMessage(s, null, null);
      }
    }

    _cookieCoords = null;

    /*
     * If the client sent a redirector cookie, the cookie contains the
     * hostname and port number of the client's nearest GAP, and the
     * geographical coordinates associated with the client's IP address.
     */
    if (_config.getCookieEnabledFlag()) {
      HTTPCookie clientCookie = null;

      if ( (s = _req.requestHdr.getHeader("Cookie")) != null) {
        try {
          _httpCookie.init(s);
          clientCookie = _httpCookie;
        }
        catch(IllegalArgumentException e) {
          logError("Malformed cookie: " + e.getMessage());

          // CONTINUE - cookie will be replaced
        }

        if (clientCookie != null) {
          if (DEBUG & _debugLevel > 1) {
            debugPrintLn("Cookie: " + clientCookie.toString());
          }

          String gap = clientCookie.getAttribute(
                         RedirectorCookieFactory.COOKIE_GAP_ATTRIB);

          /*
           * Set the nearest GAP to the GAP indicated by the cookie. If
           * the GAP address inside the cookie is invalid or if it does
           * not refer to a GAP, the cookie is discarded (and replaced).
           */
          if (gap != null) {
            HostAddress gapHost = null;

            try {
              gapHost = new HostAddress(gap);
              s = gapHost.toString();

              // Check if gapHost still refers to an active GAP.
              if (_gapList.get(s) != null) {
                gapAddress = s;
                haveValidGAPCookie = true;
              }
            }
            catch(UnknownHostException e) {
              logError("Unknown host in cookie: " + gap);

              // CONTINUE - GAP cookie will be replaced
            }
            catch(IllegalArgumentException e) {
              logError("Malformed host address in cookie: " + gap);

              // CONTINUE - GAP cookie will be replaced
            }
          }
          else {
            if (DEBUG & _debugLevel > 1) {
              debugPrintLn("Cookie does not contain a "
                         + RedirectorCookieFactory.COOKIE_GAP_ATTRIB
                         + " attribute");
            }
          }

          /*
           * If the cookie does not contain a valid nearest GAP attribute,
           * we may need the cookie's coordinates attribute to determine the
           * nearest GAP.
           */
          if (gapAddress == null) {
            s = clientCookie.getAttribute(
                    RedirectorCookieFactory.COOKIE_COORDS_ATTRIB);

            if (s != null) {
              try {
                _cookieCoords = new FloatCoordinate(s);
                haveValidLocationCookie = true;
              }
              catch(IllegalArgumentException e) {
                logError("Malformed coordinates in cookie: " + s);

                // CONTINUE - location cookie will be replaced
              }
            }
            else {
              if (DEBUG & _debugLevel > 1) {
                debugPrintLn("Cookie does not contain a "
                             + RedirectorCookieFactory.COOKIE_COORDS_ATTRIB
                             + " attribute");
              }
            }
          }
        }
      }
    }

    /*
     * If there is no (valid) GAP cookie, find the location of the nearest
     * GAP. Pick a random GAP if the nearest GAP could not be determined.
     */
    if (gapAddress == null) {
      GlobeAccessPointRecord gaprec;

      if ( (gaprec = findNearestGAP(cliAddr)) == null) {
        gaprec = getRandomGAP();
        randomGAP = true;
      }
      gapAddress = gaprec.hostport;

      // _cookieCoords set
    }

    String gapCookie = null, locationCookie = null;

    /*
     * Create the GAP cookie value if cookies are enabled and the client
     * does not have a (valid) GAP cookie. If a GAP cookie is created,
     * create a location cookie if the client doesn't have a valid one.
     */
    if (_config.getCookieEnabledFlag()) {
      if (! haveValidGAPCookie) {
        if (randomGAP) {
          setExpiresDate(_date, 1000 * RANDOM_GAP_COOKIE_TTL);
        }
        else {
          setExpiresDate(_date, 1000 * _config.getGAPCookieTTL());
        }

        gapCookie = _cookieFactory.getGAPValue(gapAddress, _date);

        if ( ! haveValidLocationCookie) {
          if (_cookieCoords != null) {
            setExpiresDate(_date, 1000 * _config.getLocationCookieTTL());
            locationCookie = _cookieFactory.getLocationValue(_cookieCoords,
                                                             _date);
          }
        }
      }
    }

    /*
     * Send a reply to redirect the client to the nearest GAP.
     */
    if (_config.getHTTPRedirectFlag()) {
      return sendHTTPRedirectMessage(gapAddress, file,
                                     gapCookie, locationCookie);
    }
    else {
      return sendHTMLReloadPage(gapAddress, file,
                                gapCookie, locationCookie);
    }
  }


  /**
   * Send a HTTP redirect message to the client.
   *
   * @param  gapAddress      the address of the nearest GAP
   * @param  file            the file being redirected to
   * @param  gapCookie       optional redirector GAP cookie
   * @param  locationCookie  optional redirector location cookie
   * @return <code>true</code>;
   *         <code>false</code> if an error occurs
   */
  private boolean sendHTTPRedirectMessage(String gapAddress, String file,
                                     String gapCookie, String locationCookie)
  {
    return sendHTTPRedirectMessage("http://" + gapAddress + file,
                                   gapCookie, locationCookie);
  }


  /**
   * Send a HTTP redirect message to the client.
   *
   * @param  location        the location to redirect the client to
   * @param  gapCookie       optional redirector GAP cookie
   * @param  locationCookie  optional redirector location cookie
   * @return <code>true</code>;
   *         <code>false</code> if an error occurs
   */
  private boolean sendHTTPRedirectMessage(String location,
                                     String gapCookie, String locationCookie)
  {
    HTTPRespHdr resp = _httpRespHdr;

    /*
     * Create a reply with a 307 (HTTP/1.1) or 302 reply code.
     */
    if (_req.requestHdr.getVersionNumber() == 1.0) {
      _req.responseStatus = 302;

      resp.init( "HTTP/1.0", 302, "Moved Temporarily" );
      resp.addHeader( "Location", location );
      resp.addHeader( "Content-Type", "text/html" );
    }
    else {
      _req.responseStatus = 307;

      resp.init( "HTTP/1.1", 307, "Temporary Redirect" );
	    
      /*
       * Put Location: right after status line so hopefully the stupid
       * Mozilla 0.6 will read it correctly (apparently it can't deal
       * with a response that is not delivered to it in a single
       * read() on the TCP socket).
       */
      resp.addHeader( "Location", location );

      resp.addHeader("Server", GlobeRedirector.SERVER_NAME);
      setCurrentDate(_date);
      resp.addHeader("Date", _httpDateFormatter.format(_date));
      resp.addHeader( "Content-Type", "text/html" );

      /*
       * 307 responses are not cachable by default, so we add an Expires:
       * header to have it cached for a while.
       */
      setExpiresDate(_date, 1000 * _config.getHTTPExpires());
      resp.addHeader("Expires", _httpDateFormatter.format(_date));

      // Netscape Enterprise puts location here
      // resp.addHeader( "Location", location );

      resp.addHeader( "Connection", "close" );
    }

    if (gapCookie != null) {
      if (DEBUG && _debugLevel > 1) {
        debugPrintLn("writing GAP cookie: " + gapCookie);
      }
      resp.addHeader("Set-Cookie", gapCookie);
    }

    if (locationCookie != null) {
      if (DEBUG && _debugLevel > 1) {
        debugPrintLn("writing location cookie: " + locationCookie);
      }
      resp.addHeader("Set-Cookie", locationCookie);
    }

    // both 1.0 and 1.1 want a little message just in case
    String body = null;
    if (!_req.requestHdr.getMethod().toUpperCase().equals( "HEAD" )) {
      // message for ancient browsers

      // reset string buffer
      _strBuf.setLength(0);
      _strBuf.append("<HEAD><TITLE>Temporary Redirect</TITLE></HEAD>\n"
                     + "<BODY><H1> Temporary Redirect </H1>\n"
                     + "You are being redirected to <A HREF=\"");
      _strBuf.append(location);
      _strBuf.append("\">");
      _strBuf.append(location);
      _strBuf.append("</A>.</BODY>");
       body = _strBuf.toString();
    }

    try {
      DataOutputStream cliOut = _req.connection.getOutputStream();

      resp.write(cliOut);
      if (body != null) {
        cliOut.write( body.getBytes() );
        _req.bytesSent = body.length();
      }
      else {
        _req.bytesSent = 0;
      }
      cliOut.flush();
      return true;
    }
    catch( IOException e ) {
      logError("Cannot send HTTP redirect message" + getExceptionMessage(e));
      return false;
    }
  }


  /**
   * Send a HTML page to the client which refreshes itself after 0
   * seconds (i.e., immediately). The refreshed HTML page will redirect
   * the client to the nearest GAP. The HTML page contains a list of
   * fallback GAPs which the client may use if the nearest GAP is
   * down.
   *
   * @param  gapAddress      the address of the nearest GAP
   * @param  file            the file being redirected to
   * @param  gapCookie       optional redirector GAP cookie
   * @param  locationCookie  optional redirector location cookie
   * @return <code>true</code>;
   *         <code>false</code> if an error occurs
   */
  private boolean sendHTMLReloadPage(String gapAddress, String file,
                                     String gapCookie, String locationCookie)
  {
    String location = "http://" + gapAddress + file;

    HTTPRespHdr resp = _httpRespHdr;

    if (_req.requestHdr.getVersionNumber() == 1.0) {
      _req.responseStatus = 200;

      resp.init( "HTTP/1.0", 200, "OK" );
      resp.addHeader( "Content-Type", "text/html" );
    }
    else {
      _req.responseStatus = 200;

      resp.init( "HTTP/1.1", 200, "OK" );
      resp.addHeader("Server", GlobeRedirector.SERVER_NAME);
      setCurrentDate(_date);
      resp.addHeader("Date", _httpDateFormatter.format(_date));

      // Netscape Enterprise Server returns Content-type ("type" in lower case)!
      resp.addHeader( "Content-Type", "text/html" );

      /*
       * 307 responses are not cachable by default, so we add an Expires:
       * header to have it cached for a while.
       */
      setExpiresDate(_date, 1000 * _config.getHTTPExpires());
      resp.addHeader("Expires", _httpDateFormatter.format(_date));

      // Netscape Enterprise puts location here
      // resp.addHeader( "Location", location );

      resp.addHeader( "Connection", "close" );
    }

    if (gapCookie != null) {
      if (DEBUG && _debugLevel > 1) {
        debugPrintLn("writing GAP cookie: " + gapCookie);
      }
      resp.addHeader("Set-Cookie", gapCookie);
    }

    if (locationCookie != null) {
      if (DEBUG && _debugLevel > 1) {
        debugPrintLn("writing location cookie: " + locationCookie);
      }
      resp.addHeader("Set-Cookie", locationCookie);
    }

    String body = null;

    if (!_req.requestHdr.getMethod().toUpperCase().equals( "HEAD" )) {
      String s;

      // reset string buffer
      _strBuf.setLength(0);

      _strBuf.append("<HTML>\n<HEAD>\n<META HTTP-EQUIV=\"Refresh\" "
                     + "CONTENT=\"0;URL=");
      _strBuf.append(location);
      _strBuf.append("\">"
        + "<TITLE>\nFallback Page\n</TITLE>\n</HEAD>\n"
        + "<BODY><H2>Redirector Fallback Page</H2>\n"
        + "If the Globe Access Point (GAP) you are being redirected "
        + "to is\ndown, please select another GAP from the following list:\n"
        + "<UL>\n");

      // Write the links to the alternate GAPs.
      _gapList.writeHTMLList(_strBuf, file, MAX_HTML_LINKS);

      _strBuf.append("</UL>\n");

      // Add signature (if any).
      if ( (s = _config.getSignature()) != null) {
        _strBuf.append("<HR>\n");
        _strBuf.append("<ADDRESS>" + s + "</ADDRESS>");
      }
      _strBuf.append("</BODY></HTML>\n");
      body = _strBuf.toString(); 
    }

    try {
      DataOutputStream cliOut = _req.connection.getOutputStream();

      resp.write(cliOut);
      if (body != null) {
        cliOut.write( body.getBytes() );
        _req.bytesSent = body.length();
      }
      else {
        _req.bytesSent = 0;
      }
      cliOut.flush();
      return true;
    }
    catch( IOException e ) {
      logError("Cannot send HTML reload page" + getExceptionMessage(e));
      return false;
    }
  }


  /**
   * Set the given date to the current date/time.
   */
  private void setCurrentDate(Date date)
  {
    date.setTime(System.currentTimeMillis());
  }


  /**
   * Set the given date to the current date/time plus the given offset (msec).
   */
  private void setExpiresDate(Date date, long offset)
  {
    date.setTime(System.currentTimeMillis() + offset);
  }


  /**
   * Find the Globe Access Point geographically nearest to the given IP
   * address. Sets <code>_cookieCoords</code> to the geographical
   * coordinates associated with the IP address in case
   * <code>_cookieCoords</code> equals <code>null</code> and these
   * coordinates need to be determined.
   *
   * @param  srcAddr  the IP address
   * @return the record of the nearest GAP;
   *         <code>null</code> if the nearest GAP could not be determined
   */
  private GlobeAccessPointRecord findNearestGAP(InetAddress srcAddr)
  {
    GlobeAccessPointRecord nearestGAPrec;
    FloatCoordinate coords = null;

    // look in our GAP lookup cache. It looks like IE5 ignores our
    // Expires: header so we might really need this for performance 
    // reasons.
    synchronized(GlobeRedirector.ip2GAPCache) {
      nearestGAPrec =
        (GlobeAccessPointRecord) GlobeRedirector.ip2GAPCache.get(srcAddr);
    }

    /*
     * Return the GAP record if it is in the cache.
     */
    if (nearestGAPrec != null) {
      if (DEBUG && _debugLevel > 0) {
        debugPrintLn("(cached) Nearest GAP is " + nearestGAPrec.hostport
                     + " at (" + nearestGAPrec.coords.getLatitude()
                     + "," + nearestGAPrec.coords.getLongitude() + ")");
      }
      return nearestGAPrec;
    }

    /*
     * There is no {srcAddr, nearest GAP record} pair in the cache. We need
     * the geographical coordinates associated with srcAddr to determine
     * the nearest GAP.
     */

    if (_cookieCoords != null) {        // client cookie contains coordinates
      coords = _cookieCoords;
    }
    else {
      coords = getCoordinates(srcAddr);

      if (coords == null) {
        return null;
      }
    }

    /*
     * Calculate which GAP is nearest.
     */
    int i, dist, min = Integer.MAX_VALUE;
    CoordinateImpl srcCoords = new CoordinateImpl((int)coords.lat,
                                                  (int)coords.longi);

    for (i = 0; i < _gapList.size(); i++) {
      GlobeAccessPointRecord gaprec = _gapList.get(i);
      dist = gaprec.coords.computeDistance(srcCoords);

      if (dist < min) {
        nearestGAPrec = gaprec;
        min = dist;
      }
    }

    // Cache the result.
    synchronized(GlobeRedirector.ip2GAPCache) {
      GlobeRedirector.ip2GAPCache.put(srcAddr, nearestGAPrec);
    }

    if (DEBUG && _debugLevel > 0) {
      debugPrintLn("Nearest GAP of " + srcAddr.getHostAddress()
                   + " at (" + (int)coords.lat + "," + (int)coords.longi
                   + ") is " + nearestGAPrec.hostport
                   + " at (" + nearestGAPrec.coords.getLatitude()
                   + "," + nearestGAPrec.coords.getLongitude() + ")");
    }

    _cookieCoords = coords;
    return nearestGAPrec;
  }


  /**
   * Select a GAP record from the GAP list at random and return it.
   */
  private GlobeAccessPointRecord getRandomGAP()
  {
    int n = _random.nextInt();

    if (n < 0) {
      n *= -1;
    }

    return _gapList.get(n % _gapList.size());
  }


  /**
   * Return the geographical coordinates associated with the given
   * IP address; <code>null</code> if an error occurs.
   */
  private FloatCoordinate getCoordinates(InetAddress addr)
  {
    FloatCoordinate coords = null;

    /*
     * Look for the geographical coordinates in our database.
     */
    try {
      if (DEBUG && _debugLevel > 1) {
        debugPrintLn("Looking for coordinates associated with "
                     + addr.getHostAddress() + " in Geo db");
      }
      coords = GlobeRedirector.geoDB.read(addr);
    }
    catch(DBException e) {
      logError("GeoDB read failure" + getExceptionMessage(e));

      // CONTINUE
    }

    /*
     * If the coordinates are not in the database, retrieve them from
     * the Net GEO server (and store them in the database).
     */
    if (coords == null) {
      if (DEBUG && _debugLevel > 1) {
        debugPrintLn("Retrieving coordinates from GEO server");
      }

      if ( (coords = askNetGeo(addr)) == null) {
        if (DEBUG && _debugLevel > 1) {
          debugPrintLn("Cannot determine coordinates");
        }
        return null;
      }
      else {
        try {
          GlobeRedirector.geoDB.write(addr, coords);
        }
        catch(DBException e) {
          logError("GeoDB write failure" + getExceptionMessage(e));

          // CONTINUE
        }
      }
    }
    return coords;
  }


  /**
   * Ask the NetGeo server to determine the geographical coordinates
   * of the given IP address.
   *
   * @param  srcAddr  the IP address
   * @return a FloatCoordinate holding the coordinates;
   *         <code>null</code> if an error occurs
   */
  private FloatCoordinate askNetGeo(InetAddress srcAddr)
  { 
    String s;
    Hashtable latLongHash = GlobeRedirector.netgeo.getLatLong(
                              srcAddr.getHostAddress() );

    if (latLongHash == null) {
      logError("NetGeo error: getLatLong() returned null");
      return null;
    }

    float flat=0, flongi=0;

    if (NetGeoClient.OK.equals(latLongHash.get("STATUS"))) {
      try {
        if ( (s = (String)latLongHash.get("LAT")) == null) {
          logError("NetGeo error: LatLong hashtable does not contain ``LAT''");
          return null;
        }
        flat = Float.parseFloat(s);
      }
      catch(NumberFormatException e) {
        logError("NetGeo error: latitude format error" + e.getMessage());
        return null;
      }

      try {
        if ( (s = (String)latLongHash.get("LONG")) == null) {
          logError("NetGeo error: LatLong hashtable does not contain ``LONG''");
          return null;
        }
        flongi = Float.parseFloat(s);
      }
      catch(NumberFormatException e) {
        logError("NetGeo error: longitude format error" + e.getMessage());
        return null;
      }

      if (DEBUG && _debugLevel > 0) {
        debugPrintLn("NetGeo locates client at (" + flat + "," + flongi + ")");
      }
      return new FloatCoordinate( flat, flongi );
    }
    else {
      if (DEBUG && _debugLevel > 0) {
        debugPrintLn("NetGeo could not locate client: "
                     + latLongHash.get("STATUS") );
      }
      return null;
    }
  }


  /**
   * Write a message to the error log.
   *
   * @param  msg  error message
   */
  private void logError(String msg)
  {
    GlobeRedirector.errorLog.write(msg);
  }


  /**
   * Return the detail message of an exception.
   *
   * @param  e  the exception
   * @return the detail message;
   *         <code>""</code> if there is no detail message
   */
  public String getExceptionMessage(Exception e)
  {
    String s = e.getMessage();

    return (s == null) ? "" : ": " + s;
  }
  

  /**
   * Print a debug message to standard output.
   */
  private void debugPrint(String msg)
  {
    System.out.print(";; [RedirectorPoolThread:" + _id + "] " + msg);
  }


  /**
   * Print a debug message to standard output.
   */
  private void debugPrintLn(String msg)
  {
    System.out.println(";; [RedirectorPoolThread:" + _id + "] " + msg);
  }
}
