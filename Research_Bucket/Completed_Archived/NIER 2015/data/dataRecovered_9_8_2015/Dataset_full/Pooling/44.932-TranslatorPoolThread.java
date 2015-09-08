/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

// TranslatorPoolThread.java

package vu.globe.svcs.gtrans;


import java.io.*;
import java.net.*;
import java.util.Date;

import vu.globe.util.http.*;
import vu.globe.util.log.*;
import vu.globe.util.char8.DataInputStreamUtil;
import vu.globe.util.html.HTMLErrorPage;

import vu.globe.svcs.gtrans.cfg.*;
import vu.globe.svcs.gtrans.util.*;


/**
 * This class represents a Globe Translator pool thread. A Globe translator
 * pool thread sits in an infinite loop waiting for HTTP clients to connect.
 * Once a request has been processed, the translator waits for another client
 * to connect.
 * <p>
 * In proxy mode, only requests with an absolute-URI are handled. If such
 * a URI is an embedded Globe URN, then a HTTP request with the corresponding
 * Globe URN is sent to the Globe gateway to get the document's resources.
 * If it is a regular URI, in contrast, the request is forwarded to the
 * designated server.
 * <p>
 * In server mode, only requests with an absolute-path are handled. For each
 * request, a request is sent to the Globe gateway to get the document's
 * resources, just like the way embedded Globe URNs are treated.
 */
public class TranslatorPoolThread extends Thread
{
  private static final boolean DEBUG = true;       // debug flag
  private static final String  CRLF = "\r\n";      // HTTP line terminator
  private static final int     HTTP_PORT = 80;     // default HTTP port
  private static final int     BUFSIZE = 8092;     // read buffer size

  // The configuration object of the redirector.
  private static TranslatorConfiguration _config = GlobeTranslator.config;

  /** `Factory' for dates in the common log file format. */
  private static CommonLogFileDateFormat _clfDateFormatter =
                                            new CommonLogFileDateFormat();
  
  /** `Factory' for dates in the HTTP date format. */
  private static HTTPDateFormat _httpDateFormatter = new HTTPDateFormat();

  // Gateway connection timeout.
  private static int _gatewayTimeout = _config.getGatewayTimeout();

  private static String _hostPort = _config.getHostPort();

   // The HTTP scheme (http://) followed by _hostPort.
  private static String _embeddedURNPrefix = _config.getEmbeddedURNPrefix();

  // Translator operation mode.
  private static int _mode = _config.getTranslatorMode();

  // Debug level.
  private static int _debugLevel = _config.getDebugLevel();

  private ServerSocket _serverSock;        // server listening socket
  private long _id;                        // thread ID
  private byte _buf[];                     // read buffer

  // The following members are reused to avoid excessive object creation.
  private Request _req;                    // client request
  private Date _date;
  private HTTPRespHdr _httpRespHdr;
  private Connection _gatewayConnection;
  private URNTranslator _urnTranslator;
  private ReplyBody _replyBody;


  /*
   * The ReplyBody class represents the raw data of an HTTP entity-body
   * after any Globe URNs inside it have been embedded. The MIME type of the
   * body is txt/html.
   */
   private class ReplyBody
   {
     int numEmbedded;      // number of Globe URNs embedded
     ByteArray body;       // reply body with embedded Globe URNs (if any)


     /**
      * Clear the state of this ReplyBody.
      */
     public void clear()
     {
       numEmbedded = 0;
       body = null;
     }
   }


  /**
   * Instance creation.
   *
   * @param  id          thread ID (for debugging purposes)
   * @param  group       the thread group to which this pool thread belongs
   * @param  serverSock  the server socket
   */
  public TranslatorPoolThread(long id, ThreadGroup group,
                              ServerSocket serverSock)
  {
    super(group, null, "TranslatorPoolThread:" + id);

    _id = id;
    _serverSock = serverSock;
    _buf = new byte[BUFSIZE];
    _req = new Request();
    _date = new Date();
    _httpRespHdr = new HTTPRespHdr();
    _gatewayConnection = new Connection();
    _urnTranslator = new URNTranslator(4096);
    _replyBody = new ReplyBody();
  }


  /**
   * Wait for HTTP clients to connect and then process their request.
   * This method does not return.
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

      processRequest(conn);

      /*
       * If access logging is enabled, write an entry to the access log.
       */
      if (_config.getAccessLogEnabledFlag()) {
        GlobeTranslator.accessLog.write(_req.getCommonLogFileStatistics());
      } 

      if (DEBUG && _debugLevel > 1) {
        debugPrintLn("closing client connection");
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
      _req.clear();
      _httpRespHdr.clear();
      _replyBody.clear();
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
                             "Cannot read request: I/O error"
                             + getExceptionMessage(e));
      return false;
    }

    /*
     * Parse the request line, and read the request headers.
     */
    try {
      _req.read(line);
    }
    catch(MalformedURLException e) {
      String s = "Invalid URI in HTTP request" + getExceptionMessage(e);

      logError(s);
      _req.sendHtmlErrorPage(HTTPStatusCode.BAD_REQUEST, s);
      return false;
    }
    catch(MalformedHTTPReqException e) {
      String s = "Invalid HTTP request" + getExceptionMessage(e);

      logError(s);
      _req.sendHtmlErrorPage(HTTPStatusCode.BAD_REQUEST, s);
      return false;
    }
    catch(InterruptedException e) {
      logError("Cannot read request: connection timeout");
      _req.sendHtmlErrorPage(HTTPStatusCode.BAD_REQUEST, "connection timeout");
      return false;
    }
    catch(IOException e) {
      String s = "Cannot read request: I/O error " + getExceptionMessage(e);

      logError(s);
      _req.sendHtmlErrorPage(HTTPStatusCode.SRVCE_NOT_AVAILABLE, s);
      return false;
    }

    if (DEBUG && _debugLevel > 1) {
      debugPrintLn("Request: '" + _req.getReqLine() + "'");
    }

    /*
     * Process the request based on the request-URI type. The asterisk
     * option (RFC2068, sec. 5.1.2) is not supported.
     */
    if ( (url = _req.getURL()) != null) {                  // proxy
      return processAbsURIReq(url);
    }
    else if ( (path = _req.getPath()) != null) {           // server
      return processAbsPathReq(path);
    }
    else {
      logError("Unsupported request-URI: " + _req.getReqLine());
      _req.sendHtmlErrorPage(HTTPStatusCode.BAD_REQUEST,
                             "unsupported request-URI");
      return false;
    }
  }


  /**
   * Process a request for an absolute-URI, e.g. ``http://a.b.c/index.html''.
   * If an error occurs, an error message is sent to the client.
   *
   * @param  url  the requested URL
   * @return <code>true</code>;
   *         <code>false</code> if an error occurs
   */
  private boolean processAbsURIReq(URL url)
  {
    /*
     * Reject the request if we are running as a HTTP server, as HTTP
     * servers should not process requests with an absolute URL.
     */
    if (_mode == TranslatorConfiguration.MODE_SERVER) {
      logError("Request-URI is not an absolute-path");
      _req.sendHtmlErrorPage(HTTPStatusCode.BAD_REQUEST,
                             "Cannot retrieve URL " + url
                             + "\n<P>\n"
                             + "Invalid URL: unexpected access protocol "
                             + "(e.g., `http://')\n");
      return false;
    }

    String host = url.getHost();

    /*
     * If the request does not contain an embedded Globe URN, forward the
     * request to the HTTP server, and send the reply (after embedding
     * any Globe URNs inside it) to the client.
     */
    if (host.equalsIgnoreCase(_hostPort) == false) {
      int port = url.getPort();

      if (port == -1) {
        port = HTTP_PORT;                       // use default HTTP port
      }
      return httpSrvTrans(host, port);
    }

    /*
     * Otherwise, process request for a Globe object.
     */
    return processGlobeReq(url.getFile());
  }
 
 
  /**
   * Process a request for a file, e.g. ``/index.html''. This request is
   * treated as a request for a Globe object. If an error occurs, an
   * error message is sent to the client.
   *
   * @param  path  the requested file (object name)
   * @return <code>true</code>;
   *         <code>false</code> if an error occurs
   */
  private boolean processAbsPathReq(String path)
  {
    /*
     * Reject request if we are running as a HTTP proxy, as HTTP proxies
     * should only process URLs that are absolute (i.e., URLs that start
     * with a scheme).
     */
    if (_mode == TranslatorConfiguration.MODE_PROXY) {
      logError("Request-URI is not an absolute-URI");
      _req.sendHtmlErrorPage(HTTPStatusCode.BAD_REQUEST,
                             "Cannot retrieve URL " + path
                             + "\n<P>\n"
                             + "Invalid URL: missing or incorrect access "
                             + "protocol (e.g., `http://')\n");
      return false;
    }

    return processGlobeReq(path);
  }


  /**
   * Process a request for a Globe object.
   *
   * @param  objName the object name (possibly followed by an element name)
   * @return <code>true</code>;
   *         <code>false</code> if an error occurs
   */
  private boolean processGlobeReq(String objName)
  {
    String httpMethod = _req.getMethod();

    /*
     * Only GET and HEAD methods are allowed.
     */
    if (httpMethod.equalsIgnoreCase("GET") == false
        && httpMethod.equalsIgnoreCase("HEAD") == false) {
      logError("Unsupported Globe request method '" + httpMethod + "' ");
      _req.sendHtmlErrorPage(HTTPStatusCode.METHOD_NOT_ALLOWED,
                             "Allow: GET, HEAD", "not a GET or HEAD method");
      return false;
    }

    /*
     * Redirect the client to the default URL (if defined) if the
     * object name is absent.
     */
    if (objName.equals("/")) {
      String s = _config.getDefaultURL();

      if (s != null) {
        if (DEBUG && _debugLevel > 1) {
          debugPrintLn("no object name specified -- using default URL");
        }

        return sendHTTPRedirectMessage(s);
      }
    }

    /*
     * Create the corresponding HTTP request-line with a Globe scheme.
     */
    String reqLine = httpMethod + " globe:/" + objName + " "
                     + _req.getVersion();

    /*
     * Send the request to the Globe gateway, and forward the reply
     * (after embedding any Globe URNs) to the client.
     */
    return gatewayTrans(reqLine);
  }


  /**
   * Send the request from the client to a HTTP server and process the reply.
   * If an error occurs, an error message is sent to the client.
   * 
   * @param  srvName  host name of the HTTP server
   * @param  srvPort  server port
   * @return <code>true</code>;
   *         <code>false</code> if an error occurs
   */
  private boolean httpSrvTrans(String srvName, int srvPort)
  {
    Connection httpServerConnection = new Connection();

    if (DEBUG && _debugLevel > 0) {
      debugPrintLn("sending request '"
                   + _req.getReqLine() + "' to HTTP server ["
                   + srvName + "]." + srvPort);
    }

    InetAddress httpProxyAddr = _config.getHttpProxyAddress();

    /*
     * If defined, connect to the HTTP proxy, HTTP server otherwise.
     */
    if (httpProxyAddr != null) {
      if (createConnection(httpProxyAddr,
                           _config.getHttpProxyPort(),
                           _config.getHttpServerTimeout(),
                           "HTTP proxy", httpServerConnection) == false) {
        return false;
      }
    }
    else {
      InetAddress addr;

      try {
        addr = InetAddress.getByName(srvName);
      }
      catch(UnknownHostException e) {
        logError("Unknown host " + srvName);
        _req.sendHtmlErrorPage(HTTPStatusCode.NOT_FOUND,
                               "unable to locate server " + srvName);
        return false;
      }

      if (createConnection(addr, srvPort, _config.getHttpServerTimeout(),
                           "HTTP server", httpServerConnection) == false) {
        return false;
      }
    }

    try {
      /*
       * Send the request to the HTTP server.
       */
      sendClientRequest(_req.requestHdr, _req.connection.getInputStream(),
                        httpServerConnection.getOutputStream());

      if (DEBUG && _debugLevel > 1) {
        debugPrintLn("waiting for reply from HTTP server...");
      }

      /*
       * Wait for the server's reply, then send it to the client.
       */
      return recvHttpReply(httpServerConnection.getInputStream(), false);
    }
    catch(InterruptedException e) {
      logError("Cannot read HTTP server reply: timeout");
      _req.sendHtmlErrorPage(HTTPStatusCode.SRVCE_NOT_AVAILABLE,
                             "HTTP server connection timed out");
      return false;
    }
    catch(IOException e) {
      String s = "I/O error: cannot communicate with HTTP server: "
                 + getExceptionMessage(e);

      logError(s);
      _req.sendHtmlErrorPage(HTTPStatusCode.SRVCE_NOT_AVAILABLE, s);
      return false;
    }
    finally {
      try {
        httpServerConnection.close();
      }
      catch(IOException e) {
        logError("Cannot close HTTP server connection"
                 + getExceptionMessage(e));
      }
    }
  }


  /**
   * Receive a HTTP reply (from the HTTP server or the Globe gateway) and
   * send it to the client. If the reply contains a txt/html MIME part,
   * the Globe URNs inside this part are embedded first. If an error
   * occurs, an error message is sent to the client.
   *
   * @param  srvIn  input stream to read the reply from
   * @param  globe  if set, the reply comes from the Globe gateway
   * @return <code>true</code>;
   *         <code>false</code> if an error occurs
   *
   * @exception  IOException           if an I/O error occurs
   * @exception  InterruptedException  if a read on <code>srvIn</code>
   *                                   times out
   */
  private boolean recvHttpReply(DataInputStream srvIn, boolean globe)
    throws IOException, InterruptedException
  {
    String s, line;
    HTTPRespHdr respHdr;
    HTTPStatLine statLine;
    int n, contentLength;
    DataOutputStream cliOut;
 
    /*
     * Read the status-line from the HTTP response.
     */
    if ( (line = DataInputStreamUtil.readLineIntr(srvIn)) == null) {
      _req.sendHtmlErrorPage(HTTPStatusCode.SRVCE_NOT_AVAILABLE,
                             (globe) ? "empty response from Globe gateway"
                                     : "empty response from HTTP server");
      return false;
    }

    if (DEBUG && _debugLevel > 0) {
      debugPrintLn("received HTTP reply: '" + line + "'");
    }

    statLine = null;
    cliOut = _req.connection.getOutputStream();

    try {
      statLine = new HTTPStatLine(line);
    }
    catch(MalformedHTTPStatLineException e) {
      if (DEBUG && _debugLevel > 1) {
        debugPrintLn("not a full response");
      }
    }
          
    /*
     * If the status-line is illegal or absent (HTTP/1.0 simple-response),
     * forward the response to the client.
     */
    if (statLine == null) {
      // Note: use 0 to indicate that there is no response status.
      _req.responseStatus = 0;

      _req.bytesSent = 0;

      cliOut.writeBytes(line + CRLF);
      while ( (n = srvIn.read(_buf)) != -1) {
        cliOut.write(_buf, 0, n);
        _req.bytesSent += n;
      }
      cliOut.flush();
      return true;
    }

    /*
     * Otherwise, read the remainder of the response header.
     */

    respHdr = _httpRespHdr;

    try {
      respHdr.init(statLine, srvIn);
    }
    catch(MalformedHTTPRespException e) {
      logError("Malformed HTTP header" + getExceptionMessage(e));

      _req.sendHtmlErrorPage(HTTPStatusCode.BAD_GATEWAY,
                             (globe) ? "invalid response from Globe gateway" :
                                       "invalid response from HTTP server");
      return false;
    }

    /*
     * Check if a location header is present and embed the Globe URN
     * (if any) inside it.
     */
    if ( (s = respHdr.getHeader("Location")) != null) {
      if (s.startsWith("globe://")) {
        respHdr.removeHeader("Location");
        respHdr.addHeader("Location", _embeddedURNPrefix + s.substring(8));

        if (DEBUG && _debugLevel > 1) {
          debugPrintLn("Location: " + _embeddedURNPrefix
                       + s.substring(8) + "\n");
        }
      }
    }

    /*
     * Remove proxy-connection header (if any).
     */
    respHdr.removeHeader("Proxy-Connection");
    respHdr.addHeader("Proxy-Connection", "close");

    /*
     * Forward the response to the client if it does not contain a
     * text/html MIME part.
     */
    if ( (s = respHdr.getHeader("Content-Type")) == null
        || s.equalsIgnoreCase("text/html") == false) {

      if (DEBUG && _debugLevel > 1) {
        debugPrintLn("response does not contain a text/html part");
      }

      _req.responseStatus = respHdr.getCode();
      _req.bytesSent = 0;

      respHdr.write(cliOut);
      while ( (n = srvIn.read(_buf)) != -1) {
        cliOut.write(_buf, 0, n);
        _req.bytesSent += n;
      }
      cliOut.flush();
      return true;
    }

    /*
     * Otherwise, embed any Globe URNs.
     */

    contentLength = -1;

    if ( (s = respHdr.getHeader("Content-Length")) != null) {
      try {
        contentLength = Integer.parseInt(s);
      }
      catch(NumberFormatException e) {
        logError("entity-body has invalid content-length: " + s);

        // CONTINUE
      }
    }

    ReplyBody replyBody = null;

    try {
      replyBody = embedURNs(srvIn, contentLength);
    }
    catch(IOException e) {
      s = getExceptionMessage(e);

      logError("I/O error while embedding URNs" + s);

      _req.sendHtmlErrorPage(HTTPStatusCode.SRVCE_NOT_AVAILABLE,
                             "I/O error: cannot read entity-body" + s);
      return false;
    }

    if (DEBUG) {
      if (_debugLevel > 1) {
        debugPrintLn("#" + replyBody.numEmbedded + " URNs embedded");
      }
      else if (_debugLevel > 2) {
        if ( (s = respHdr.getHeader("Content-Length")) == null) {
          s = "(unknown)";
        }
        debugPrintLn("Content-Length: " + s
                     + " result.length: " + replyBody.body.length
                     + " #embedded URNs: " + replyBody.numEmbedded);
      }
    }

    /*
     * If the entity body has been modified (i.e., Globe URNs have
     * been embedded), remove the MD5 digest (if present) and adjust the
     * Content-Length header field.
     */
    if (replyBody.numEmbedded > 0) {
      respHdr.removeHeader("Content-MD5");
      respHdr.removeHeader("Content-Length");
      respHdr.addHeader("Content-Length",
                        Integer.toString(replyBody.body.length));
    }

    /*
     * Write reply to client.
     */
    respHdr.write(cliOut);
    cliOut.write(replyBody.body.buf, 0, replyBody.body.length);
    cliOut.flush();

    _req.responseStatus = respHdr.getCode();
    _req.bytesSent = replyBody.body.length;

    return true;
  }


  /**
   * Embed Globe URNs in a response's entity-body. Return a ReplyBody
   * object that holds the modified (unmodified if there are no Globe URNs)
   * entity-body.
   *
   * @param  in             input stream to read the entity-body from
   * @param  contentLength  the length of the entity-body, as specified
   *                        by the Content-Length header, -1 if the
   *                        length is not specified 
   * @return the reply body;
   *         <code>null</code> if an error occurs
   *
   * @exception  IOException           if an I/O error occurs
   * @exception  InterruptedException  if <code>in</code> times out
   */
  private ReplyBody embedURNs(DataInputStream in, int contentLength)
    throws IOException, InterruptedException
  {
    int n, m, len, off;
    ByteArrayInputStream bin;
    MyByteArrayOutputStream bout;
    boolean eof;

    if (DEBUG && _debugLevel > 1) {
      debugPrintLn("embedding Globe URNs");
    }

    bin = null;
    bout = null;
    eof = false;

    try {
      /*
       * Read as much of the entity-body.
       */
      n = 0;
      off = 0;
      len = _buf.length;
      while (n < len) {
        int count = in.read(_buf, off + n, len - n);
        if (count < 0) {
          eof = true;
          break;
        }
        n += count;

        // if (contentLength != -1 && n >= contentLength) {
        //   eof = true;
        //   break;
        // }
      }

      if (n == 0) {
        if (DEBUG && _debugLevel > 1) {
          debugPrintLn("empty entity-body");
        }

        _replyBody.numEmbedded = 0;
        _replyBody.body = new ByteArray(new byte[0]);
        return _replyBody;
      }

      /*
       * Create a ByteArrayInputStream on top of the buffer holding the
       * entity-body. If necessary, read the remainder of the entity-body.
       * Note that we pre-allocate some room for the output data (128
       * and 1024 bytes).
       */
      if (eof) {
        bin = new ByteArrayInputStream(_buf, 0, n);
        bout = new MyByteArrayOutputStream(n + 128);
      }
      else {
        if (DEBUG && _debugLevel > 1) {
          debugPrintLn("request doesn't fit in buffer - reading remaining "
                       + "bytes...");
        }

        MyByteArrayOutputStream bb = new MyByteArrayOutputStream(n + 1024);

        bb.write(_buf, 0, n);

        len = n;
        while ( (n = in.read(_buf)) != -1) {
          bb.write(_buf, 0, n);
          len += n;
        }

        ByteArray data = bb.getByteArray();

        bin = new ByteArrayInputStream(data.buf, 0, data.length);
        bout = new MyByteArrayOutputStream(data.length + 128);
      }

      _replyBody.numEmbedded = _urnTranslator.replaceGlobeURN(
                                   _embeddedURNPrefix, false, bin,
                                   new DataOutputStream(bout));
      _replyBody.body = bout.getByteArray();

      return _replyBody;
    }
    finally {
      if (bin != null) {
        try {
          bin.close();
        }
        catch(IOException e) {
        }
      }

      if (bout != null) {
        try {
          bout.close();
        }
        catch(IOException e) {
        }
      }

      bin = null;
      bout = null;
    }
  }


  /**
   * Send a HTTP request-line with a Globe scheme to the Globe gateway,
   * embed any Globe URNs in the reply, and send the reply to the client.
   * 
   * @param  reqline  HTTP request-line (not CRLF terminated) with
   *                  globe scheme, e.g. "GET globe://anobj HTTP/1.0"
   * @return <code>true</code>;
   *         <code>false</code> if an error occurs
   */
  private boolean gatewayTrans(String reqline)
  {
    InetAddress gatewayAddr;
    int gatewayPort;

    try {
      gatewayAddr = _config.getGatewayAddress();
      gatewayPort = _config.getGatewayPort();
    }
    catch (GatewayNotFoundException exc) {
      logError("Cannot contact gateway: " + exc.getMessage());
      _req.sendHtmlErrorPage(HTTPStatusCode.SRVCE_NOT_AVAILABLE,
                         "Cannot contact Globe Gateway: " + exc.getMessage());
      return false;
    }

    if (DEBUG && _debugLevel > 0) {
      debugPrintLn("sending request '" + reqline
                   + "' to Globe gateway ["
                   + gatewayAddr + "]." + gatewayPort);
    }

    /*
     * Create the gateway connection.
     */
    if (createConnection(gatewayAddr, gatewayPort, _gatewayTimeout,
                         "Globe gateway", _gatewayConnection) == false) {
      return false;
    }

    try {
      /*
       * Send the request to the gateway. The request method is either a
       * GET or a HEAD, so the request cannot contain a message-body.
       */
      _req.requestHdr.write(_gatewayConnection.getOutputStream(), reqline);

      _gatewayConnection.getOutputStream().flush();

      if (DEBUG && _debugLevel > 1) {
        debugPrintLn("waiting for reply from Globe gateway...");
      }

      return recvHttpReply(_gatewayConnection.getInputStream(), true);
    }
    catch(InterruptedException e) {
      logError("Cannot read gateway reply: timeout");
      _req.sendHtmlErrorPage(HTTPStatusCode.SRVCE_NOT_AVAILABLE,
                             "Globe gateway connection timed out");
      return false;
    }
    catch(IOException e) {
      String s = "I/O error: cannot communicate with Globe gateway"
                 + getExceptionMessage(e);

      logError(s);
      _req.sendHtmlErrorPage(HTTPStatusCode.SRVCE_NOT_AVAILABLE, s);
      return false;
    }
    finally {
      try {
        _gatewayConnection.close();
      }
      catch(IOException e) {
        logError("Cannot close gateway connection" + getExceptionMessage(e));
      }
    }
  }


  /**
   * Send a request to the HTTP server. If it is a full-request, send
   * message-body (if any) as well.
   *
   * @param  hdr  request header
   * @param  in   input stream to read the request's message body from (if any)
   * @param  out  output stream to write to
   *
   * @exception  IOException  if an I/O error occurs
   */
  private void sendClientRequest(HTTPReqHdr hdr, DataInputStream in,
			         DataOutputStream out)
    throws IOException
  {
    String s;
    int n, contentLength;

    hdr.write(out, true);

    /*
     * Only full requests can contain a message-body.
     */
    if (hdr.isFullReq() == false) {
      return;
    }

    s = hdr.getMethod();

    /*
     * Return if it is not a POST or PUT method (these are the only
     * methods that can contain a message-body).
     */
    if (s.equals("POST") == false && s.equals("PUT") == false) {
      return;
    }

    /*
     * Otherwise, send the message-body (if any).
     */

    if ( (s = hdr.getHeader("Content-Length")) == null) {
      logError("Missing Content-Length header");
      return;
    }

    contentLength = 0;

    try {
      contentLength = Integer.parseInt(s);
    }
    catch(NumberFormatException e) {
      logError("Invalid Content-Length: " + s);
      return;
    }

    if (DEBUG && _debugLevel > 1) {
      debugPrintLn("sending client request's message-body ("
                   + contentLength + " bytes)");
    }

    /*
     * Send the message body.
     */
    while (contentLength > 0 && (n = in.read(_buf)) != -1) {
      out.write(_buf, 0, n);
      contentLength -= n;
    }
  }


  /**
   * Send a HTTP redirect message to the client.
   *
   * @param  location  the location to redirect the client to
   * @return <code>true</code>;
   *         <code>false</code> if an error occurs
   */
  private boolean sendHTTPRedirectMessage(String location)
  {
    HTTPRespHdr resp = _httpRespHdr;
    StringBuffer strBuf = new StringBuffer(256);

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

      setCurrentDate(_date);
      resp.addHeader("Date", _httpDateFormatter.format(_date));

      // Netscape Enterprise Server returns Content-type ("type" in lower case)!
      resp.addHeader( "Content-Type", "text/html" );

      resp.addHeader( "Connection", "close" );
    }

    // both 1.0 and 1.1 want a little message just in case
    String body = null;
    if (!_req.requestHdr.getMethod().toUpperCase().equals( "HEAD" )) {
      // message for ancient browsers

      // reset string buffer
      strBuf.setLength(0);
      strBuf.append("<HEAD><TITLE>Temporary Redirect</TITLE></HEAD>\n"
                    + "<BODY><H1> Temporary Redirect </H1>\n"
                    + "You are being redirected to <A HREF=\"");
      strBuf.append(location);
      strBuf.append("\">");
      strBuf.append(location);
      strBuf.append("</A>.</BODY>");
      body = strBuf.toString();
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
   * Create a connection. If an error occurs, an error message is sent to
   * the client.
   *
   * @param  addr      peer address
   * @param  port      peer port
   * @param  timeout   socket timeout (seconds)
   * @param  peerType  description of the peer's type (used in error messages)
   * @param  conn      container for the connection
   * @return <code>true</code>;
   *         <code>false</code> if an error occurs
   */
  private boolean createConnection(InetAddress addr, int port, int  timeout,
                                   String peerType, Connection conn)
  {
    Socket sock = null;

    try {
      sock = new Socket(addr, port);
    }
    catch(IOException e) {
      String s = getExceptionMessage(e);

      logError("Cannot create " + peerType + " socket" + s);
      _req.sendHtmlErrorPage(HTTPStatusCode.SRVCE_NOT_AVAILABLE,
                             "I/O error: cannot communicate with "
                             + peerType + s);
      return false;
    }

    try {
      sock.setSoTimeout(timeout * 1000);
    }
    catch(SocketException e) {
      logError("Cannot set timeout of " + peerType + " socket: "
               + e.getMessage() + " -- closing socket");
      _req.sendHtmlErrorPage(HTTPStatusCode.SRVCE_NOT_AVAILABLE,
                             "I/O error: cannot set timeout on " + peerType
                             + " connection");

      try {
        sock.close();
      }
      catch(IOException e2) {
        logError("Cannot close " + peerType + " socket: " + e2.getMessage());
      }
      return false;
    }

    try {
      conn.init(sock);
      return true;
    }
    catch(Exception e) {
      String s = getExceptionMessage(e);

      logError("Cannot create " + peerType + " connection" + s);
      _req.sendHtmlErrorPage(HTTPStatusCode.SRVCE_NOT_AVAILABLE,
                             "I/O error: cannot communicate with "
                             + peerType + s);
      try {
        sock.close();
      }
      catch(IOException e2) {
        logError("Cannot close " + peerType + " socket"
                 + getExceptionMessage(e2));
      }
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
   * Write a message to the error log.
   *
   * @param  msg  error message
   */
  private void logError(String msg)
  {
    GlobeTranslator.errorLog.write(msg);
  }


  /**
   * Return the detail message of an exception.
   *
   * @param  e  the exception
   * @return the detail message;
   *         <code>""</code> if there is no detail message
   */
  private String getExceptionMessage(Exception e)
  {
    String s = e.getMessage();

    return (s == null) ? "" : ": " + s;
  }


  /**
   * Print a debug message to standard output.
   */
  private void debugPrint(String msg)
  {
    System.out.print(";; [TranslatorPoolThread" + _id + "] " + msg);
  }


  /**
   * Print a debug message to standard output.
   */
  private void debugPrintLn(String msg)
  {
    System.out.println(";; [TranslatorPoolThread:" + _id + "] " + msg);
  }
}
