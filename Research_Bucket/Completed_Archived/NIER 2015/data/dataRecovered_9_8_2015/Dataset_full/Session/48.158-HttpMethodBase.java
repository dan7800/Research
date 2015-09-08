/*
 * $Header: /home/cvs/jakarta-commons/httpclient/src/java/org/apache/commons/httpclient/HttpMethodBase.java,v 1.159.2.33 2004/10/10 00:00:35 mbecke Exp $
 * $Revision: 1.159.2.33 $
 * $Date: 2004/10/10 00:00:35 $
 *
 * ====================================================================
 *
 *  Copyright 1999-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 */

package org.apache.commons.httpclient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.httpclient.auth.AuthScheme;
import org.apache.commons.httpclient.auth.AuthenticationException;
import org.apache.commons.httpclient.auth.HttpAuthenticator;
import org.apache.commons.httpclient.auth.MalformedChallengeException;
import org.apache.commons.httpclient.auth.NTLMScheme;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.cookie.CookieSpec;
import org.apache.commons.httpclient.cookie.MalformedCookieException;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An abstract base implementation of HttpMethod.
 * <p>
 * At minimum, subclasses will need to override:
 * <ul>
 *   <li>{@link #getName} to return the approriate name for this method
 *   </li>
 * </ul>
 *
 * <p>
 * When a method's request may contain a body, subclasses will typically want
 * to override:
 * <ul>
 *   <li>{@link #getRequestContentLength} to indicate the length (in bytes)
 *     of that body</li>
 *   <li>{@link #writeRequestBody writeRequestBody(HttpState,HttpConnection)}
 *     to write the body</li>
 * </ul>
 * </p>
 *
 * <p>
 * When a method requires additional request headers, subclasses will typically
 * want to override:
 * <ul>
 *   <li>{@link #addRequestHeaders addRequestHeaders(HttpState,HttpConnection)}
 *      to write those headers
 *   </li>
 * </ul>
 * </p>
 *
 * <p>
 * When a method expects specific response headers, subclasses may want to
 * override:
 * <ul>
 *   <li>{@link #processResponseHeaders processResponseHeaders(HttpState,HttpConnection)}
 *     to handle those headers
 *   </li>
 * </ul>
 * </p>
 *
 *
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 * @author Rodney Waldhoff
 * @author Sean C. Sullivan
 * @author <a href="mailto:dion@apache.org">dIon Gillard</a>
 * @author <a href="mailto:jsdever@apache.org">Jeff Dever</a>
 * @author <a href="mailto:dims@apache.org">Davanum Srinivas</a>
 * @author Ortwin Glueck
 * @author Eric Johnson
 * @author Michael Becke
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author <a href="mailto:ggregory@seagullsw.com">Gary Gregory</a>
 *
 * @version $Revision: 1.159.2.33 $ $Date: 2004/10/10 00:00:35 $
 */
public abstract class HttpMethodBase implements HttpMethod {

    /** Maximum number of redirects and authentications that will be followed */
    private static final int MAX_FORWARDS = 100;

    // -------------------------------------------------------------- Constants

    /** Log object for this class. */
    private static final Log LOG = LogFactory.getLog(HttpMethodBase.class);

    /** The User-Agent header sent on every request. */
    protected static final Header USER_AGENT;

    static {
        String agent = null;
        try {
            agent = System.getProperty("httpclient.useragent");
        } catch (SecurityException ignore) {
        }
        if (agent == null) {
            agent = "Jakarta Commons-HttpClient/2.0.2";
        }
        USER_AGENT = new Header("User-Agent", agent);
    }

    // ----------------------------------------------------- Instance variables 

    /** Request headers, if any. */
    private HeaderGroup requestHeaders = new HeaderGroup();

    /** The Status-Line from the response. */
    private StatusLine statusLine = null;

    /** Response headers, if any. */
    private HeaderGroup responseHeaders = new HeaderGroup();

    /** Response trailer headers, if any. */
    private HeaderGroup responseTrailerHeaders = new HeaderGroup();

    /** Authentication scheme used to authenticate againt the target server */
    private AuthScheme authScheme = null;

    /** Realms this method tried to authenticate to */
    private Set realms = null;

    /** Actual authentication realm */
    private String realm = null;

    /** Authentication scheme used to authenticate againt the proxy server */
    private AuthScheme proxyAuthScheme = null;

    /** Proxy Realms this method tried to authenticate to */
    private Set proxyRealms = null;

    /** Actual proxy authentication realm */
    private String proxyRealm = null;

    /** Path of the HTTP method. */
    private String path = null;

    /** Query string of the HTTP method, if any. */
    private String queryString = null;

    /** The response body of the HTTP method, assuming it has not be 
     * intercepted by a sub-class. */
    private InputStream responseStream = null;

    /** The connection that the response stream was read from. */
    private HttpConnection responseConnection = null;

    /** Buffer for the response */
    private byte[] responseBody = null;

    /** True if the HTTP method should automatically follow
     *  HTTP redirects. */
    private boolean followRedirects = false;

    /** True if the HTTP method should automatically handle
     *  HTTP authentication challenges. */
    private boolean doAuthentication = true;

    /** True if version 1.1 of the HTTP protocol should be used per default. */
    private boolean http11 = true;

    /** True if this HTTP method should strictly follow the HTTP protocol
     * specification. */
    private boolean strictMode = false;

    /** True if this method has already been executed. */
    private boolean used = false;

    /** Count of how many times did this HTTP method transparently handle 
     * a recoverable exception. */
    private int recoverableExceptionCount = 0;

    /** The host configuration for this HTTP method, can be null */
    private HostConfiguration hostConfiguration;

    /**
     * Handles method retries
     */
    private MethodRetryHandler methodRetryHandler;

    /** True if this method is currently being executed. */
    private boolean inExecute = false;

    /** True if this HTTP method is finished with the connection */
    private boolean doneWithConnection = false;

    /** True if the connection must be closed when no longer needed */
    private boolean connectionCloseForced = false;

    /** Number of milliseconds to wait for 100-contunue response. */
    private static final int RESPONSE_WAIT_TIME_MS = 3000;

    /** Maximum buffered response size (in bytes) that triggers no warning. */
    private static final int BUFFER_WARN_TRIGGER_LIMIT = 1024*1024; //1 MB

    /** Default initial size of the response buffer if content length is unknown. */
    private static final int DEFAULT_INITIAL_BUFFER_SIZE = 4*1024; // 4 kB

    // ----------------------------------------------------------- Constructors

    /**
     * No-arg constructor.
     */
    public HttpMethodBase() {
    }

    /**
     * Constructor specifying a URI.
     * It is responsibility of the caller to ensure that URI elements
     * (path & query parameters) are properly encoded (URL safe).
     *
     * @param uri either an absolute or relative URI. The URI is expected
     *            to be URL-encoded
     * 
     * @throws IllegalArgumentException when URI is invalid
     * @throws IllegalStateException when protocol of the absolute URI is not recognised
     */
    public HttpMethodBase(String uri) 
        throws IllegalArgumentException, IllegalStateException {

        try {

            // create a URI and allow for null/empty uri values
            if (uri == null || uri.equals("")) {
                uri = "/";
            }
            URI parsedURI = new URI(uri.toCharArray());
            
            // only set the host if specified by the URI
            if (parsedURI.isAbsoluteURI()) {
                hostConfiguration = new HostConfiguration();
                hostConfiguration.setHost(
                    parsedURI.getHost(),
                    parsedURI.getPort(),
                    parsedURI.getScheme()
                ); 
            }
            
            // set the path, defaulting to root
            setPath(
                parsedURI.getPath() == null
                ? "/"
                : parsedURI.getEscapedPath()
            );
            setQueryString(parsedURI.getEscapedQuery());

        } catch (URIException e) {
            throw new IllegalArgumentException("Invalid uri '" 
                + uri + "': " + e.getMessage() 
            );
        }
    }

    // ------------------------------------------- Property Setters and Getters

    /**
     * Obtains the name of the HTTP method as used in the HTTP request line,
     * for example <tt>"GET"</tt> or <tt>"POST"</tt>.
     * 
     * @return the name of this method
     */
    public abstract String getName();

    /**
     * Returns the URI of the HTTP method
     * 
     * @return The URI
     * 
     * @throws URIException If the URI cannot be created.
     * 
     * @see org.apache.commons.httpclient.HttpMethod#getURI()
     */
    public URI getURI() throws URIException {

        if (hostConfiguration == null) {
            // just use a relative URI, the host hasn't been set
            URI tmpUri = new URI(null, null, path, null, null);
            tmpUri.setEscapedQuery(queryString);
            return tmpUri;
        } else {

            // we only want to include the port if it's not the default
            int port = hostConfiguration.getPort();
            if (port == hostConfiguration.getProtocol().getDefaultPort()) {
                port = -1;
            }

            URI tmpUri = new URI(
                hostConfiguration.getProtocol().getScheme(),
                null,
                hostConfiguration.getHost(),
                port,
                path,
                null // to set an escaped form
            );
            tmpUri.setEscapedQuery(queryString);
            return tmpUri;

        }

    }

    /**
     * Sets whether or not the HTTP method should automatically follow HTTP redirects 
     * (status code 302, etc.)
     * 
     * @param followRedirects <tt>true</tt> if the method will automatically follow redirects,
     * <tt>false</tt> otherwise.
     */
    public void setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    /**
     * Returns <tt>true</tt> if the HTTP method should automatically follow HTTP redirects 
     * (status code 302, etc.), <tt>false</tt> otherwise.
     * 
     * @return <tt>true</tt> if the method will automatically follow HTTP redirects, 
     * <tt>false</tt> otherwise.
     */
    public boolean getFollowRedirects() {
        return this.followRedirects;
    }

    /**
    /** Sets whether version 1.1 of the HTTP protocol should be used per default.
     *
     * @param http11 <tt>true</tt> to use HTTP/1.1, <tt>false</tt> to use 1.0
     */
    public void setHttp11(boolean http11) {
        this.http11 = http11;
    }

    /**
     * Returns <tt>true</tt> if the HTTP method should automatically handle HTTP 
     * authentication challenges (status code 401, etc.), <tt>false</tt> otherwise
     *
     * @return <tt>true</tt> if authentication challenges will be processed 
     * automatically, <tt>false</tt> otherwise.
     * 
     * @since 2.0
     */
    public boolean getDoAuthentication() {
        return doAuthentication;
    }

    /**
     * Sets whether or not the HTTP method should automatically handle HTTP 
     * authentication challenges (status code 401, etc.)
     *
     * @param doAuthentication <tt>true</tt> to process authentication challenges
     * authomatically, <tt>false</tt> otherwise.
     * 
     * @since 2.0
     */
    public void setDoAuthentication(boolean doAuthentication) {
        this.doAuthentication = doAuthentication;
    }

    // ---------------------------------------------- Protected Utility Methods

    /**
     * Returns <tt>true</tt> if version 1.1 of the HTTP protocol should be 
     * used per default, <tt>false</tt> if version 1.0 should be used.
     *
     * @return <tt>true</tt> to use HTTP/1.1, <tt>false</tt> to use 1.0
     */
    public boolean isHttp11() {
        return http11;
    }

    /**
     * Sets the path of the HTTP method.
     * It is responsibility of the caller to ensure that the path is
     * properly encoded (URL safe).
     *
     * @param path the path of the HTTP method. The path is expected
     *        to be URL-encoded
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Adds the specified request header, NOT overwriting any previous value.
     * Note that header-name matching is case insensitive.
     *
     * @param header the header to add to the request
     */
    public void addRequestHeader(Header header) {
        LOG.trace("HttpMethodBase.addRequestHeader(Header)");

        if (header == null) {
            LOG.debug("null header value ignored");
        } else {
            getRequestHeaderGroup().addHeader(header);
        }
    }

    /**
     * Use this method internally to add footers.
     * 
     * @param footer The footer to add.
     */
    public void addResponseFooter(Header footer) {
        getResponseTrailerHeaderGroup().addHeader(footer);
    }

    /**
     * Gets the path of this HTTP method.
     * Calling this method <em>after</em> the request has been executed will 
     * return the <em>actual</em> path, following any redirects automatically
     * handled by this HTTP method.
     *
     * @return the path to request or "/" if the path is blank.
     */
    public String getPath() {
        return (path == null || path.equals("")) ? "/" : path;
    }

    /**
     * Sets the query string of this HTTP method. The caller must ensure that the string 
     * is properly URL encoded. The query string should not start with the question 
     * mark character.
     *
     * @param queryString the query string
     * 
     * @see EncodingUtil#formUrlEncode(NameValuePair[], String)
     */
    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    /**
     * Sets the query string of this HTTP method.  The pairs are encoded as UTF-8 characters.  
     * To use a different charset the parameters can be encoded manually using EncodingUtil 
     * and set as a single String.
     *
     * @param params an array of {@link NameValuePair}s to add as query string
     *        parameters. The name/value pairs will be automcatically 
     *        URL encoded
     * 
     * @see EncodingUtil#formUrlEncode(NameValuePair[], String)
     * @see #setQueryString(String)
     */
    public void setQueryString(NameValuePair[] params) {
        LOG.trace("enter HttpMethodBase.setQueryString(NameValuePair[])");
        queryString = EncodingUtil.formUrlEncode(params, "UTF-8");
    }

    /**
     * Gets the query string of this HTTP method.
     *
     * @return The query string
     */
    public String getQueryString() {
        return queryString;
    }

    /**
     * Set the specified request header, overwriting any previous value. Note
     * that header-name matching is case-insensitive.
     *
     * @param headerName the header's name
     * @param headerValue the header's value
     */
    public void setRequestHeader(String headerName, String headerValue) {
        Header header = new Header(headerName, headerValue);
        setRequestHeader(header);
    }

    /**
     * Sets the specified request header, overwriting any previous value.
     * Note that header-name matching is case insensitive.
     * 
     * @param header the header
     */
    public void setRequestHeader(Header header) {
        
        Header[] headers = getRequestHeaderGroup().getHeaders(header.getName());
        
        for (int i = 0; i < headers.length; i++) {
            getRequestHeaderGroup().removeHeader(headers[i]);
        }
        
        getRequestHeaderGroup().addHeader(header);
        
    }

    /**
     * Returns the specified request header. Note that header-name matching is
     * case insensitive. <tt>null</tt> will be returned if either
     * <i>headerName</i> is <tt>null</tt> or there is no matching header for
     * <i>headerName</i>.
     * 
     * @param headerName The name of the header to be returned.
     *
     * @return The specified request header.
     */
    public Header getRequestHeader(String headerName) {
        if (headerName == null) {
            return null;
        } else {
            return getRequestHeaderGroup().getCondensedHeader(headerName);
        }
    }

    /**
     * Returns an array of the requests headers that the HTTP method currently has
     *
     * @return an array of my request headers.
     */
    public Header[] getRequestHeaders() {
        return getRequestHeaderGroup().getAllHeaders();
    }

    /**
     * Gets the {@link HeaderGroup header group} storing the request headers.
     * 
     * @return a HeaderGroup
     * 
     * @since 2.0beta1
     */
    protected HeaderGroup getRequestHeaderGroup() {
        return requestHeaders;
    }

    /**
     * Gets the {@link HeaderGroup header group} storing the response trailer headers 
     * as per RFC 2616 section 3.6.1.
     * 
     * @return a HeaderGroup
     * 
     * @since 2.0beta1
     */
    protected HeaderGroup getResponseTrailerHeaderGroup() {
        return responseTrailerHeaders;
    }

    /**
     * Gets the {@link HeaderGroup header group} storing the response headers.
     * 
     * @return a HeaderGroup
     * 
     * @since 2.0beta1
     */
    protected HeaderGroup getResponseHeaderGroup() {
        return responseHeaders;
    }
    
    /**
     * Returns the response status code.
     *
     * @return the status code associated with the latest response.
     */
    public int getStatusCode() {
        return statusLine.getStatusCode();
    }

    /**
     * Provides access to the response status line.
     *
     * @return the status line object from the latest response.
     * @since 2.0
     */
    public StatusLine getStatusLine() {
        return statusLine;
    }

    /**
     * Checks if response data is available.
     * @return <tt>true</tt> if response data is available, <tt>false</tt> otherwise.
     */
    private boolean responseAvailable() {
        return (responseBody != null) || (responseStream != null);
    }

    /**
     * Returns an array of the response headers that the HTTP method currently has
     * in the order in which they were read.
     *
     * @return an array of response headers.
     */
    public Header[] getResponseHeaders() {
        return getResponseHeaderGroup().getAllHeaders();
    }

    /**
     * Gets the response header associated with the given name. Header name
     * matching is case insensitive. <tt>null</tt> will be returned if either
     * <i>headerName</i> is <tt>null</tt> or there is no matching header for
     * <i>headerName</i>.
     *
     * @param headerName the header name to match
     *
     * @return the matching header
     */
    public Header getResponseHeader(String headerName) {        
        if (headerName == null) {
            return null;
        } else {
            return getResponseHeaderGroup().getCondensedHeader(headerName);
        }        
    }


    /**
     * Return the length (in bytes) of the response body, as specified in a
     * <tt>Content-Length</tt> header.
     *
     * <p>
     * Return <tt>-1</tt> when the content-length is unknown.
     * </p>
     *
     * @return content length, if <tt>Content-Length</tt> header is available. 
     *          <tt>0</tt> indicates that the request has no body.
     *          If <tt>Content-Length</tt> header is not present, the method 
     *          returns  <tt>-1</tt>.
     */
    protected int getResponseContentLength() {
        Header[] headers = getResponseHeaderGroup().getHeaders("Content-Length");
        if (headers.length == 0) {
            return -1;
        }
        if (headers.length > 1) {
            LOG.warn("Multiple content-length headers detected");
        }
        for (int i = headers.length - 1; i >= 0; i--) {
            Header header = headers[i];
            try {
                return Integer.parseInt(header.getValue());
            } catch (NumberFormatException e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Invalid content-length value: " + e.getMessage());
                }
            }
            // See if we can have better luck with another header, if present
        }
        return -1;
    }


    /**
     * Returns the response body of the HTTP method, if any, as an array of bytes.
     * If response body is not available or cannot be read, returns <tt>null</tt>.
     * 
     * Note: This will cause the entire response body to be buffered in memory. A
     * malicious server may easily exhaust all the VM memory. It is strongly
     * recommended, to use getResponseAsStream if the content length of the response
     * is unknown or resonably large.
     * 
     * @return The response body.
     */
    public byte[] getResponseBody() {
        if (this.responseBody == null) {
            try {
                InputStream instream = getResponseBodyAsStream();
                if (instream != null) {
                    int contentLength = getResponseContentLength();
                    if ((contentLength == -1) || (contentLength > BUFFER_WARN_TRIGGER_LIMIT)) {
                        LOG.warn("Going to buffer response body of large or unknown size. "
                                +"Using getResponseAsStream instead is recommended.");
                    }
                    LOG.debug("Buffering response body");
                    ByteArrayOutputStream outstream = new ByteArrayOutputStream(
                            contentLength > 0 ? contentLength : DEFAULT_INITIAL_BUFFER_SIZE);
                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = instream.read(buffer)) > 0) {
                        outstream.write(buffer, 0, len);
                    }
                    outstream.close();
                    setResponseStream(null);
                    this.responseBody = outstream.toByteArray();
                }
            } catch (IOException e) {
                LOG.error("I/O failure reading response body", e);
                this.responseBody = null;
            }
        }
        return this.responseBody;
    }

    /**
     * Returns the response body of the HTTP method, if any, as an {@link InputStream}. 
     * If response body is not available, returns <tt>null</tt>
     * 
     * @return The response body
     * 
     * @throws IOException If an I/O (transport) problem occurs while obtaining the 
     * response body.
     */
    public InputStream getResponseBodyAsStream() throws IOException {
        if (responseStream != null) {
            return responseStream;
        }
        if (responseBody != null) {
            InputStream byteResponseStream = new ByteArrayInputStream(responseBody);
            LOG.debug("re-creating response stream from byte array");
            return byteResponseStream;
        }
        return null;
    }

    /**
     * Returns the response body of the HTTP method, if any, as a {@link String}. 
     * If response body is not available or cannot be read, returns <tt>null</tt>
     * The string conversion on the data is done using the character encoding specified
     * in <tt>Content-Type</tt> header.
     * 
     * Note: This will cause the entire response body to be buffered in memory. A
     * malicious server may easily exhaust all the VM memory. It is strongly
     * recommended, to use getResponseAsStream if the content length of the response
     * is unknown or resonably large.
     * 
     * @return The response body.
     */
    public String getResponseBodyAsString() {
        byte[] rawdata = null;
        if (responseAvailable()) {
            rawdata = getResponseBody();
        }
        if (rawdata != null) {
            return HttpConstants.getContentString(rawdata, getResponseCharSet());
        } else {
            return null;
        }
    }

    /**
     * Returns an array of the response footers that the HTTP method currently has
     * in the order in which they were read.
     *
     * @return an array of footers
     */
    public Header[] getResponseFooters() {
        return getResponseTrailerHeaderGroup().getAllHeaders();
    }

    /**
     * Gets the response footer associated with the given name.
     * Footer name matching is case insensitive.
     * <tt>null</tt> will be returned if either <i>footerName</i> is
     * <tt>null</tt> or there is no matching footer for <i>footerName</i>
     * or there are no footers available.  If there are multiple footers
     * with the same name, there values will be combined with the ',' separator
     * as specified by RFC2616.
     * 
     * @param footerName the footer name to match
     * @return the matching footer
     */
    public Header getResponseFooter(String footerName) {
        if (footerName == null) {
            return null;
        } else {
            return getResponseTrailerHeaderGroup().getCondensedHeader(footerName);
        }
    }

    /**
     * Sets the response stream.
     * @param responseStream The new response stream.
     */
    protected void setResponseStream(InputStream responseStream) {
        this.responseStream = responseStream;
    }

    /**
     * Returns a stream from which the body of the current response may be read.
     * If the method has not yet been executed, if <code>responseBodyConsumed</code>
     * has been called, or if the stream returned by a previous call has been closed,
     * <code>null</code> will be returned.
     *
     * @return the current response stream
     */
    protected InputStream getResponseStream() {
        return responseStream;
    }
    
    /**
     * Returns the status text (or "reason phrase") associated with the latest
     * response.
     * 
     * @return The status text.
     */
    public String getStatusText() {
        return statusLine.getReasonPhrase();
    }

    /**
     * Defines how strictly HttpClient follows the HTTP protocol specification  
     * (RFC 2616 and other relevant RFCs). In the strict mode HttpClient precisely
     * implements the requirements of the specification, whereas in non-strict mode 
     * it attempts to mimic the exact behaviour of commonly used HTTP agents, 
     * which many HTTP servers expect.
     * 
     * @param strictMode <tt>true</tt> for strict mode, <tt>false</tt> otherwise
     */
    public void setStrictMode(boolean strictMode) {
        this.strictMode = strictMode;
    }

    /**
     * Returns the value of the strict mode flag.
     *
     * @return <tt>true</tt> if strict mode is enabled, <tt>false</tt> otherwise
     */
    public boolean isStrictMode() {
        return strictMode;
    }

    /**
     * Adds the specified request header, NOT overwriting any previous value.
     * Note that header-name matching is case insensitive.
     *
     * @param headerName the header's name
     * @param headerValue the header's value
     */
    public void addRequestHeader(String headerName, String headerValue) {
        addRequestHeader(new Header(headerName, headerValue));
    }

    /**
     * Tests if the connection should be force-closed when no longer needed.
     * 
     * @return <code>true</code> if the connection must be closed
     */
    protected boolean isConnectionCloseForced() {
        return this.connectionCloseForced;
    }

    /**
     * Sets whether or not the connection should be force-closed when no longer 
     * needed. This value should only be set to <code>true</code> in abnormal 
     * circumstances, such as HTTP protocol violations. 
     * 
     * @param b <code>true</code> if the connection must be closed, <code>false</code>
     * otherwise.
     */
    protected void setConnectionCloseForced(boolean b) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Force-close connection: " + b);
        }
        this.connectionCloseForced = b;
    }

    /**
     * Tests if the connection should be closed after the method has been executed.
     * The connection will be left open when using HTTP/1.1 or if <tt>Connection: 
     * keep-alive</tt> header was sent.
     * 
     * @param conn the connection in question
     * 
     * @return boolean true if we should close the connection.
     */
    protected boolean shouldCloseConnection(HttpConnection conn) {

        // Connection must be closed due to an abnormal circumstance 
        if (isConnectionCloseForced()) {
            LOG.debug("Should force-close connection.");
            return true;
        }

        Header connectionHeader = null;
        // In case being connected via a proxy server
        if (!conn.isTransparent()) {
            // Check for 'proxy-connection' directive
            connectionHeader = responseHeaders.getFirstHeader("proxy-connection");
        }
        // In all cases Check for 'connection' directive
        // some non-complaint proxy servers send it instread of
        // expected 'proxy-connection' directive
        if (connectionHeader == null) {
            connectionHeader = responseHeaders.getFirstHeader("connection");
        }
        if (connectionHeader != null) {
            if (connectionHeader.getValue().equalsIgnoreCase("close")) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Should close connection in response to " 
                        + connectionHeader.toExternalForm());
                }
                return true;
            } else if (connectionHeader.getValue().equalsIgnoreCase("keep-alive")) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Should NOT close connection in response to " 
                        + connectionHeader.toExternalForm());
                }
                return false;
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Unknown directive: " + connectionHeader.toExternalForm());
                }
            }
        }
        LOG.debug("Resorting to protocol version default close connection policy");
        // missing or invalid connection header, do the default
        if (http11) {
            LOG.debug("Should NOT close connection, using HTTP/1.1.");
        } else {
            LOG.debug("Should close connection, using HTTP/1.0.");
        }
        return !http11;
    }
    
    /**
     * Tests if the method needs to be retried.
     * @param statusCode The status code
     * @param state the {@link HttpState state} information associated with this method
     * @param conn the {@link HttpConnection connection} to be used
     * @return boolean true if a retry is needed.
     */
    private boolean isRetryNeeded(int statusCode, HttpState state, HttpConnection conn) {
        switch (statusCode) {
            case HttpStatus.SC_UNAUTHORIZED:
            case HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED:
                LOG.debug("Authorization required");
                if (doAuthentication) { //process authentication response
                    //if the authentication is successful, return the statusCode
                    //otherwise, drop through the switch and try again.
                    if (processAuthenticationResponse(state, conn)) {
                        return false;
                    }
                } else { //let the client handle the authenticaiton
                    return false;
                }
                break;

            case HttpStatus.SC_MOVED_TEMPORARILY:
            case HttpStatus.SC_MOVED_PERMANENTLY:
            case HttpStatus.SC_SEE_OTHER:
            case HttpStatus.SC_TEMPORARY_REDIRECT:
                LOG.debug("Redirect required");

                if (!processRedirectResponse(conn)) {
                    return false;
                }
                break;

            default:
                // neither an unauthorized nor a redirect response
                return false;
        } //end of switch

        return true;
    }

    /**
     * Tests if the this method is ready to be executed.
     * 
     * @param state the {@link HttpState state} information associated with this method
     * @param conn the {@link HttpConnection connection} to be used
     * @throws HttpException If the method is in invalid state.
     */
    private void checkExecuteConditions(HttpState state, HttpConnection conn)
    throws HttpException {

        if (state == null) {
            throw new IllegalArgumentException("HttpState parameter may not be null");
        }
        if (conn == null) {
            throw new IllegalArgumentException("HttpConnection parameter may not be null");
        }
        if (hasBeenUsed()) {
            throw new HttpException("Already used, but not recycled.");
        }
        if (!validate()) {
            throw new HttpException("Not valid");
        }
        if (inExecute) {
            throw new IllegalStateException("Execute invoked recursively, or exited abnormally.");
        }
    }

    /**
     * Execute this HTTP method. Note that we cannot currently support redirects
     * that change  the connection parameters (host, port, protocol) because
     * we  don't yet have a good way to get the new connection.  For  the time
     * being, we just return the redirect response code,  and allow the user
     * agent to resubmit if desired.
     *
     * @param state {@link HttpState state} information to associate with this
     *        request. Must be non-null.
     * @param conn the {@link HttpConnection connection} to used to execute
     *        this HTTP method. Must be non-null.
     *        Note that we cannot currently support redirects that
     *        change the HttpConnection parameters (host, port, protocol)
     *        because we don't yet have a good way to get the new connection.
     *        For the time being, we just return the 302 response, and allow
     *        the user agent to resubmit if desired.
     *
     * @return the integer status code if one was obtained, or <tt>-1</tt>
     *
     * @throws IOException if an I/O (transport) error occurs
     * @throws HttpException  if a protocol exception occurs.
     * @throws HttpRecoverableException if a recoverable transport error occurs. 
     *                    Usually this kind of exceptions can be recovered from by
     *                    retrying the HTTP method 
     */
    public int execute(HttpState state, HttpConnection conn)
        throws HttpException, HttpRecoverableException, 
            IOException {
                
        LOG.trace("enter HttpMethodBase.execute(HttpState, HttpConnection)");

        // this is our connection now, assign it to a local variable so 
        // that it can be released later
        this.responseConnection = conn;

        checkExecuteConditions(state, conn);
        inExecute = true;

        try {
            //pre-emptively add the authorization header, if required.
            if (state.isAuthenticationPreemptive()) {

                LOG.debug("Preemptively sending default basic credentials");

                try {
                    if (HttpAuthenticator.authenticateDefault(this, conn, state)) {
                        LOG.debug("Default basic credentials applied");
                    } else {
                        LOG.warn("Preemptive authentication failed");
                    }
                    if (conn.isProxied()) {
                        if (HttpAuthenticator.authenticateProxyDefault(this, conn, state)) {
                            LOG.debug("Default basic proxy credentials applied");
                        } else {
                            LOG.warn("Preemptive proxy authentication failed");
                        }
                    }
                } catch (AuthenticationException e) {
                    // Log error and move on
                    LOG.error(e.getMessage(), e);
                }
            }

            realms = new HashSet();
            proxyRealms = new HashSet();
            int forwardCount = 0; //protect from an infinite loop

            while (forwardCount++ < MAX_FORWARDS) {
                // on every retry, reset this state information.
                conn.setLastResponseInputStream(null);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Execute loop try " + forwardCount);
                }

                // Discard status line
                this.statusLine = null;
                this.connectionCloseForced = false;

                //write the request and read the response, will retry
                processRequest(state, conn);

                if (!isRetryNeeded(statusLine.getStatusCode(), state, conn)) {
                    // nope, no retry needed, exit loop.
                    break;
                }

                // retry - close previous stream.  Caution - this causes
                // responseBodyConsumed to be called, which may also close the
                // connection.
                if (responseStream != null) {
                    responseStream.close();
                }

            } //end of retry loop

            if (forwardCount >= MAX_FORWARDS) {
                LOG.error("Narrowly avoided an infinite loop in execute");
                throw new HttpRecoverableException("Maximum redirects ("
                    + MAX_FORWARDS + ") exceeded");
            }

        } finally {
            inExecute = false;
            // If the response has been fully processed, return the connection
            // to the pool.  Use this flag, rather than other tests (like
            // responseStream == null), as subclasses, might reset the stream,
            // for example, reading the entire response into a file and then
            // setting the file as the stream.
            if (doneWithConnection) {
                ensureConnectionRelease();
            }
        }

        return statusLine.getStatusCode();
    }

    /**
     * Process the redirect response.
     * @param conn the {@link HttpConnection connection} used to execute
     *        this HTTP method
     * @return boolean <tt>true</tt> if the redirect was successful, <tt>false</tt>
     *        otherwise.
     */
    private boolean processRedirectResponse(HttpConnection conn) {

        if (!getFollowRedirects()) {
            LOG.info("Redirect requested but followRedirects is "
                    + "disabled");
            return false;
        }

        //get the location header to find out where to redirect to
        Header locationHeader = getResponseHeader("location");
        if (locationHeader == null) {
            // got a redirect response, but no location header
            LOG.error("Received redirect response " + getStatusCode()
                    + " but no location header");
            return false;
        }
        String location = locationHeader.getValue();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Redirect requested to location '" + location
                    + "'");
        }

        //rfc2616 demands the location value be a complete URI
        //Location       = "Location" ":" absoluteURI
        URI redirectUri = null;
        URI currentUri = null;

        try {
            currentUri = new URI(
                conn.getProtocol().getScheme(),
                null,
                conn.getHost(), 
                conn.getPort(), 
                this.getPath()
            );
            redirectUri = new URI(location.toCharArray());
            if (redirectUri.isRelativeURI()) {
                if (isStrictMode()) {
                    LOG.warn("Redirected location '" + location 
                        + "' is not acceptable in strict mode");
                    return false;
                } else { 
                    //location is incomplete, use current values for defaults
                    LOG.debug("Redirect URI is not absolute - parsing as relative");
                    redirectUri = new URI(currentUri, redirectUri);
                }
            }
        } catch (URIException e) {
            LOG.warn("Redirected location '" + location + "' is malformed");
            return false;
        }

        //check for redirect to a different protocol, host or port
        try {
            checkValidRedirect(currentUri, redirectUri);
        } catch (HttpException ex) {
            //LOG the error and let the client handle the redirect
            LOG.warn(ex.getMessage());
            return false;
        }

        //invalidate the list of authentication attempts
        this.realms.clear();
        //remove exisitng authentication headers
        if (this.proxyAuthScheme instanceof NTLMScheme) {
            removeRequestHeader(HttpAuthenticator.PROXY_AUTH_RESP);
        }
        removeRequestHeader(HttpAuthenticator.WWW_AUTH_RESP); 
        //update the current location with the redirect location.
        //avoiding use of URL.getPath() and URL.getQuery() to keep
        //jdk1.2 comliance.
        setPath(redirectUri.getEscapedPath());
        setQueryString(redirectUri.getEscapedQuery());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Redirecting from '" + currentUri.getEscapedURI()
                + "' to '" + redirectUri.getEscapedURI());
        }

        return true;
    }

    /**
     * Check for a valid redirect given the current connection and new URI.
     * Redirect to a different protocol, host or port are checked for validity.
     *
     * @param currentUri The current URI (redirecting from)
     * @param redirectUri The new URI to redirect to
     * @throws HttpException if the redirect is invalid
     * @since 2.0
     */
    private static void checkValidRedirect(URI currentUri, URI redirectUri)
    throws HttpException {
        LOG.trace("enter HttpMethodBase.checkValidRedirect(HttpConnection, URL)");

        String oldProtocol = currentUri.getScheme();
        String newProtocol = redirectUri.getScheme();
        if (!oldProtocol.equals(newProtocol)) {
            throw new HttpException("Redirect from protocol " + oldProtocol
                    + " to " + newProtocol + " is not supported");
        }

        try {
            String oldHost = currentUri.getHost();
            String newHost = redirectUri.getHost();
            if (!oldHost.equalsIgnoreCase(newHost)) {
                throw new HttpException("Redirect from host " + oldHost
                        + " to " + newHost + " is not supported");
            }
        } catch (URIException e) {
            LOG.warn("Error getting URI host", e);
            throw new HttpException("Invalid Redirect URI from: " 
                + currentUri.getEscapedURI() + " to: " + redirectUri.getEscapedURI()
            );
        }

        int oldPort = currentUri.getPort();
        if (oldPort < 0) {
            oldPort = getDefaultPort(oldProtocol);
        }
        int newPort = redirectUri.getPort();
        if (newPort < 0) {
            newPort = getDefaultPort(newProtocol);
        }
        if (oldPort != newPort) {
            throw new HttpException("Redirect from port " + oldPort
                    + " to " + newPort + " is not supported");
        }
    }

    /**
     * Returns the default port for the given protocol.
     *
     * @param protocol the given protocol.
     * @return the default port of the given protocol or -1 if the
     * protocol is not recognized.
     *
     * @since 2.0
     *
     */
    private static int getDefaultPort(String protocol) {
        String proto = protocol.toLowerCase().trim();
        if (proto.equals("http")) {
            return 80;
        } else if (proto.equals("https")) {
            return 443;
        }
        return -1;
    }

    /**
     * Returns <tt>true</tt> if the HTTP method has been already {@link #execute executed},
     * but not {@link #recycle recycled}.
     * 
     * @return <tt>true</tt> if the method has been executed, <tt>false</tt> otherwise
     */
    public boolean hasBeenUsed() {
        return used;
    }

    /**
     * Recycles the HTTP method so that it can be used again.
     * Note that all of the instance variables will be reset
     * once this method has been called. This method will also
     * release the connection being used by this HTTP method.
     * 
     * @see #releaseConnection()
     * 
     * @deprecated no longer supported and will be removed in the future
     *             version of HttpClient
     */
    public void recycle() {
        LOG.trace("enter HttpMethodBase.recycle()");

        releaseConnection();

        path = null;
        followRedirects = false;
        doAuthentication = true;
        authScheme = null;
        realm = null;
        proxyAuthScheme = null;
        proxyRealm = null;
        queryString = null;
        getRequestHeaderGroup().clear();
        getResponseHeaderGroup().clear();
        getResponseTrailerHeaderGroup().clear();
        statusLine = null;
        used = false;
        http11 = true;
        responseBody = null;
        recoverableExceptionCount = 0;
        inExecute = false;
        doneWithConnection = false;
        connectionCloseForced = false;
    }

    /**
     * Releases the connection being used by this HTTP method. In particular the
     * connection is used to read the response(if there is one) and will be held
     * until the response has been read. If the connection can be reused by other 
     * HTTP methods it is NOT closed at this point.
     *
     * @since 2.0
     */
    public void releaseConnection() {

        if (responseStream != null) {
            try {
                // FYI - this may indirectly invoke responseBodyConsumed.
                responseStream.close();
            } catch (IOException e) {
                // the connection may not have been released, let's make sure
                ensureConnectionRelease();
            }
        } else {
            // Make sure the connection has been released. If the response 
            // stream has not been set, this is the only way to release the 
            // connection. 
            ensureConnectionRelease();
        }
    }

    /**
     * Remove the request header associated with the given name. Note that
     * header-name matching is case insensitive.
     *
     * @param headerName the header name
     */
    public void removeRequestHeader(String headerName) {
        
        Header[] headers = getRequestHeaderGroup().getHeaders(headerName);
        for (int i = 0; i < headers.length; i++) {
            getRequestHeaderGroup().removeHeader(headers[i]);
        }
        
    }

    // ---------------------------------------------------------------- Queries

    /**
     * Returns <tt>true</tt> the method is ready to execute, <tt>false</tt> otherwise.
     * 
     * @return This implementation always returns <tt>true</tt>.
     */
    public boolean validate() {
        return true;
    }

    /**
     * Return the length (in bytes) of my request body, suitable for use in a
     * <tt>Content-Length</tt> header.
     *
     * <p>
     * Return <tt>-1</tt> when the content-length is unknown.
     * </p>
     *
     * <p>
     * This implementation returns <tt>0</tt>, indicating that the request has
     * no body.
     * </p>
     *
     * @return <tt>0</tt>, indicating that the request has no body.
     */
    protected int getRequestContentLength() {
        return 0;
    }

    /**
     * Generates <tt>Authorization</tt> request header if needed, as long as no
     * <tt>Authorization</tt> request header already exists.
     *
     * @param state the {@link HttpState state} information associated with this method
     * @param conn the {@link HttpConnection connection} used to execute
     *        this HTTP method
     *
     * @throws IOException if an I/O (transport) error occurs
     * @throws HttpException  if a protocol exception occurs.
     * @throws HttpRecoverableException if a recoverable transport error occurs. 
     *                    Usually this kind of exceptions can be recovered from by
     *                    retrying the HTTP method 
     */
    protected void addAuthorizationRequestHeader(HttpState state,
                                                 HttpConnection conn)
    throws IOException, HttpException {
        LOG.trace("enter HttpMethodBase.addAuthorizationRequestHeader("
                  + "HttpState, HttpConnection)");

        // add authorization header, if needed
        if (getRequestHeader(HttpAuthenticator.WWW_AUTH_RESP) == null) {
            Header[] challenges = getResponseHeaderGroup().getHeaders(
                                               HttpAuthenticator.WWW_AUTH);
            if (challenges.length > 0) {
                try {
                    this.authScheme = HttpAuthenticator.selectAuthScheme(challenges);
                    HttpAuthenticator.authenticate(this.authScheme, this, conn, state);
                } catch (HttpException e) {
                    // log and move on
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            }
        }
    }

    /**
     * Generates <tt>Content-Length</tt> or <tt>Transfer-Encoding: Chunked</tt>
     * request header, as long as no <tt>Content-Length</tt> request header
     * already exists.
     * 
     * @param state the {@link HttpState state} information associated with this method
     * @param conn the {@link HttpConnection connection} used to execute
     *        this HTTP method
     *
     * @throws IOException if an I/O (transport) error occurs
     * @throws HttpException  if a protocol exception occurs.
     * @throws HttpRecoverableException if a recoverable transport error occurs. 
     *                    Usually this kind of exceptions can be recovered from by
     *                    retrying the HTTP method 
     */
    protected void addContentLengthRequestHeader(HttpState state,
                                                 HttpConnection conn)
    throws IOException, HttpException {
        LOG.trace("enter HttpMethodBase.addContentLengthRequestHeader("
                  + "HttpState, HttpConnection)");

        // add content length or chunking
        int len = getRequestContentLength();
        if (getRequestHeader("content-length") == null) {
            if (0 < len) {
                setRequestHeader("Content-Length", String.valueOf(len));
            } else if (http11 && (len < 0)) {
                setRequestHeader("Transfer-Encoding", "chunked");
            }
        }
    }

    /**
     * Generates <tt>Cookie</tt> request headers for those {@link Cookie cookie}s
     * that match the given host, port and path.
     *
     * @param state the {@link HttpState state} information associated with this method
     * @param conn the {@link HttpConnection connection} used to execute
     *        this HTTP method
     *
     * @throws IOException if an I/O (transport) error occurs
     * @throws HttpException  if a protocol exception occurs.
     * @throws HttpRecoverableException if a recoverable transport error occurs. 
     *                    Usually this kind of exceptions can be recovered from by
     *                    retrying the HTTP method 
     */
    protected void addCookieRequestHeader(HttpState state, HttpConnection conn)
        throws IOException, HttpException {

        LOG.trace("enter HttpMethodBase.addCookieRequestHeader(HttpState, "
                  + "HttpConnection)");

        // Clean up the cookie headers
        removeRequestHeader("cookie");

        CookieSpec matcher = CookiePolicy.getSpecByPolicy(state.getCookiePolicy());
        Cookie[] cookies = matcher.match(conn.getHost(), conn.getPort(),
            getPath(), conn.isSecure(), state.getCookies());
        if ((cookies != null) && (cookies.length > 0)) {
            if (this.isStrictMode()) {
                // In strict mode put all cookies on the same header
                getRequestHeaderGroup().addHeader(
                  matcher.formatCookieHeader(cookies));
            } else {
                // In non-strict mode put each cookie on a separate header
                for (int i = 0; i < cookies.length; i++) {
                    getRequestHeaderGroup().addHeader(
                      matcher.formatCookieHeader(cookies[i]));
                }
            }
        }
    }

    /**
     * Generates <tt>Host</tt> request header, as long as no <tt>Host</tt> request
     * header already exists.
     *
     * @param state the {@link HttpState state} information associated with this method
     * @param conn the {@link HttpConnection connection} used to execute
     *        this HTTP method
     *
     * @throws IOException if an I/O (transport) error occurs
     * @throws HttpException  if a protocol exception occurs.
     * @throws HttpRecoverableException if a recoverable transport error occurs. 
     *                    Usually this kind of exceptions can be recovered from by
     *                    retrying the HTTP method 
     */
    protected void addHostRequestHeader(HttpState state, HttpConnection conn)
    throws IOException, HttpException {
        LOG.trace("enter HttpMethodBase.addHostRequestHeader(HttpState, "
                  + "HttpConnection)");

        // Per 19.6.1.1 of RFC 2616, it is legal for HTTP/1.0 based
        // applications to send the Host request-header.
        // TODO: Add the ability to disable the sending of this header for
        //       HTTP/1.0 requests.
        String host = conn.getVirtualHost();
        if (host != null) {
            LOG.debug("Using virtual host name: " + host);
        } else {
            host = conn.getHost();
        }
        int port = conn.getPort();

        if (getRequestHeader("host") != null) {
            LOG.debug(
                "Request to add Host header ignored: header already added");
            return;
        }

        // Note: RFC 2616 uses the term "internet host name" for what goes on the
        // host line.  It would seem to imply that host should be blank if the
        // host is a number instead of an name.  Based on the behavior of web
        // browsers, and the fact that RFC 2616 never defines the phrase "internet
        // host name", and the bad behavior of HttpClient that follows if we
        // send blank, I interpret this as a small misstatement in the RFC, where
        // they meant to say "internet host".  So IP numbers get sent as host
        // entries too. -- Eric Johnson 12/13/2002
        if (LOG.isDebugEnabled()) {
            LOG.debug("Adding Host request header");
        }

        //appends the port only if not using the default port for the protocol
        if (conn.getProtocol().getDefaultPort() != port) {
            host += (":" + port);
        }

        setRequestHeader("Host", host);
    }

    /**
     * Generates <tt>Proxy-Authorization</tt> request header if needed, as long as no
     * <tt>Proxy-Authorization</tt> request header already exists.
     *
     * @param state the {@link HttpState state} information associated with this method
     * @param conn the {@link HttpConnection connection} used to execute
     *        this HTTP method
     *
     * @throws IOException if an I/O (transport) error occurs
     * @throws HttpException  if a protocol exception occurs.
     * @throws HttpRecoverableException if a recoverable transport error occurs. 
     *                    Usually this kind of exceptions can be recovered from by
     *                    retrying the HTTP method 
     */
    protected void addProxyAuthorizationRequestHeader(HttpState state,
                                                      HttpConnection conn)
    throws IOException, HttpException {
        LOG.trace("enter HttpMethodBase.addProxyAuthorizationRequestHeader("
                  + "HttpState, HttpConnection)");

        // add proxy authorization header, if needed
        if (getRequestHeader(HttpAuthenticator.PROXY_AUTH_RESP) == null) {
            Header[] challenges = getResponseHeaderGroup().getHeaders(
                                               HttpAuthenticator.PROXY_AUTH);
            if (challenges.length > 0) {
                try {
                    this.proxyAuthScheme = HttpAuthenticator.selectAuthScheme(challenges);
                    HttpAuthenticator.authenticateProxy(this.proxyAuthScheme, this, conn, state);
                } catch (HttpException e) {
                    // log and move on
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            }
        }
    }

    /**
     * Generates <tt>Proxy-Connection: Keep-Alive</tt> request header when 
     * communicating via a proxy server.
     *
     * @param state the {@link HttpState state} information associated with this method
     * @param conn the {@link HttpConnection connection} used to execute
     *        this HTTP method
     *
     * @throws IOException if an I/O (transport) error occurs
     * @throws HttpException  if a protocol exception occurs.
     * @throws HttpRecoverableException if a recoverable transport error occurs. 
     *                    Usually this kind of exceptions can be recovered from by
     *                    retrying the HTTP method 
     */
    protected void addProxyConnectionHeader(HttpState state,
                                            HttpConnection conn)
    throws IOException, HttpException {
        LOG.trace("enter HttpMethodBase.addProxyConnectionHeader("
                  + "HttpState, HttpConnection)");
        if (!conn.isTransparent()) {
            setRequestHeader("Proxy-Connection", "Keep-Alive");
        }
    }

    /**
     * Generates all the required request {@link Header header}s 
     * to be submitted via the given {@link HttpConnection connection}.
     *
     * <p>
     * This implementation adds <tt>User-Agent</tt>, <tt>Host</tt>,
     * <tt>Cookie</tt>, <tt>Content-Length</tt>, <tt>Transfer-Encoding</tt>,
     * and <tt>Authorization</tt> headers, when appropriate.
     * </p>
     *
     * <p>
     * Subclasses may want to override this method to to add additional
     * headers, and may choose to invoke this implementation (via
     * <tt>super</tt>) to add the "standard" headers.
     * </p>
     *
     * @param state the {@link HttpState state} information associated with this method
     * @param conn the {@link HttpConnection connection} used to execute
     *        this HTTP method
     *
     * @throws IOException if an I/O (transport) error occurs
     * @throws HttpException  if a protocol exception occurs.
     * @throws HttpRecoverableException if a recoverable transport error occurs. 
     *                    Usually this kind of exceptions can be recovered from by
     *                    retrying the HTTP method 
     *
     * @see #writeRequestHeaders
     */
    protected void addRequestHeaders(HttpState state, HttpConnection conn)
    throws IOException, HttpException {
        LOG.trace("enter HttpMethodBase.addRequestHeaders(HttpState, "
            + "HttpConnection)");

        addUserAgentRequestHeader(state, conn);
        addHostRequestHeader(state, conn);
        addCookieRequestHeader(state, conn);
        addAuthorizationRequestHeader(state, conn);
        addProxyAuthorizationRequestHeader(state, conn);
        addProxyConnectionHeader(state, conn);
        addContentLengthRequestHeader(state, conn);
    }

    /**
     * Generates default <tt>User-Agent</tt> request header, as long as no
     * <tt>User-Agent</tt> request header already exists.
     *
     * @param state the {@link HttpState state} information associated with this method
     * @param conn the {@link HttpConnection connection} used to execute
     *        this HTTP method
     *
     * @throws IOException if an I/O (transport) error occurs
     * @throws HttpException  if a protocol exception occurs.
     * @throws HttpRecoverableException if a recoverable transport error occurs. 
     *                    Usually this kind of exceptions can be recovered from by
     *                    retrying the HTTP method 
     */
    protected void addUserAgentRequestHeader(HttpState state,
                                             HttpConnection conn)
    throws IOException, HttpException {
        LOG.trace("enter HttpMethodBase.addUserAgentRequestHeaders(HttpState, "
            + "HttpConnection)");

        if (getRequestHeader("user-agent") == null) {
            setRequestHeader(HttpMethodBase.USER_AGENT);
        }
    }

    /**
     * Throws an {@link IllegalStateException} if the HTTP method has been already
     * {@link #execute executed}, but not {@link #recycle recycled}.
     *
     * @throws IllegalStateException if the method has been used and not
     *      recycled
     */
    protected void checkNotUsed() throws IllegalStateException {
        if (used) {
            throw new IllegalStateException("Already used.");
        }
    }

    /**
     * Throws an {@link IllegalStateException} if the HTTP method has not been
     * {@link #execute executed} since last {@link #recycle recycle}.
     *
     *
     * @throws IllegalStateException if not used
     */
    protected void checkUsed()  throws IllegalStateException {
        if (!used) {
            throw new IllegalStateException("Not Used.");
        }
    }

    // ------------------------------------------------- Static Utility Methods

    /**
     * Generates HTTP request line according to the specified attributes.
     *
     * @param connection the {@link HttpConnection connection} used to execute
     *        this HTTP method
     * @param name the method name generate a request for
     * @param requestPath the path string for the request
     * @param query the query string for the request
     * @param version the protocol version to use (e.g. HTTP/1.0)
     *
     * @return HTTP request line
     */
    protected static String generateRequestLine(HttpConnection connection,
        String name, String requestPath, String query, String version) {
        LOG.trace("enter HttpMethodBase.generateRequestLine(HttpConnection, "
            + "String, String, String, String)");

        StringBuffer buf = new StringBuffer();
        // Append method name
        buf.append(name);
        buf.append(" ");
        // Absolute or relative URL?
        if (!connection.isTransparent()) {
            Protocol protocol = connection.getProtocol();
            buf.append(protocol.getScheme().toLowerCase());
            buf.append("://");
            buf.append(connection.getHost());
            if ((connection.getPort() != -1) 
                && (connection.getPort() != protocol.getDefaultPort())
            ) {
                buf.append(":");
                buf.append(connection.getPort());
            }
        }
        // Append path, if any
        if (requestPath == null) {
            buf.append("/");
        } else {
            if (!connection.isTransparent() && !requestPath.startsWith("/")) {
                buf.append("/");
            }
            buf.append(requestPath);
        }
        // Append query, if any
        if (query != null) {
            if (query.indexOf("?") != 0) {
                buf.append("?");
            }
            buf.append(query);
        }
        // Append protocol
        buf.append(" ");
        buf.append(version);
        buf.append("\r\n");
        
        return buf.toString();
    }
    
    /**
     * This method is invoked immediately after 
     * {@link #readResponseBody(HttpState,HttpConnection)} and can be overridden by
     * sub-classes in order to provide custom body processing.
     *
     * <p>
     * This implementation does nothing.
     * </p>
     *
     * @param state the {@link HttpState state} information associated with this method
     * @param conn the {@link HttpConnection connection} used to execute
     *        this HTTP method
     *
     * @see #readResponse
     * @see #readResponseBody
     */
    protected void processResponseBody(HttpState state, HttpConnection conn) {
    }

    /**
     * This method is invoked immediately after 
     * {@link #readResponseHeaders(HttpState,HttpConnection)} and can be overridden by
     * sub-classes in order to provide custom response headers processing.

     * <p>
     * This implementation will handle the <tt>Set-Cookie</tt> and
     * <tt>Set-Cookie2</tt> headers, if any, adding the relevant cookies to
     * the given {@link HttpState}.
     * </p>
     *
     * @param state the {@link HttpState state} information associated with this method
     * @param conn the {@link HttpConnection connection} used to execute
     *        this HTTP method
     *
     * @see #readResponse
     * @see #readResponseHeaders
     */
    protected void processResponseHeaders(HttpState state,
        HttpConnection conn) {
        LOG.trace("enter HttpMethodBase.processResponseHeaders(HttpState, "
            + "HttpConnection)");

        Header[] headers = getResponseHeaderGroup().getHeaders("set-cookie2");
        //Only process old style set-cookie headers if new style headres
        //are not present
        if (headers.length == 0) { 
            headers = getResponseHeaderGroup().getHeaders("set-cookie");
        }
        
        CookieSpec parser = CookiePolicy.getSpecByPolicy(state.getCookiePolicy());
        for (int i = 0; i < headers.length; i++) {
            Header header = headers[i];
            Cookie[] cookies = null;
            try {
                cookies = parser.parse(
                  conn.getHost(),
                  conn.getPort(),
                  getPath(),
                  conn.isSecure(),
                  header);
            } catch (MalformedCookieException e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Invalid cookie header: \"" 
                        + header.getValue() 
                        + "\". " + e.getMessage());
                }
            }
            if (cookies != null) {
                for (int j = 0; j < cookies.length; j++) {
                    Cookie cookie = cookies[j];
                    try {
                        parser.validate(
                          conn.getHost(),
                          conn.getPort(),
                          getPath(),
                          conn.isSecure(),
                          cookie);
                        state.addCookie(cookie);
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Cookie accepted: \"" 
                                + parser.formatCookie(cookie) + "\"");
                        }
                    } catch (MalformedCookieException e) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("Cookie rejected: \"" + parser.formatCookie(cookie) 
                                + "\". " + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    /**
     * This method is invoked immediately after 
     * {@link #readStatusLine(HttpState,HttpConnection)} and can be overridden by
     * sub-classes in order to provide custom response status line processing.
     *
     * @param state the {@link HttpState state} information associated with this method
     * @param conn the {@link HttpConnection connection} used to execute
     *        this HTTP method
     *
     * @see #readResponse
     * @see #readStatusLine
     */
    protected void processStatusLine(HttpState state, HttpConnection conn) {
    }

    /**
     * Reads the response from the given {@link HttpConnection connection}.
     *
     * <p>
     * The response is processed as the following sequence of actions:
     *
     * <ol>
     * <li>
     * {@link #readStatusLine(HttpState,HttpConnection)} is
     * invoked to read the request line.
     * </li>
     * <li>
     * {@link #processStatusLine(HttpState,HttpConnection)}
     * is invoked, allowing the method to process the status line if
     * desired.
     * </li>
     * <li>
     * {@link #readResponseHeaders(HttpState,HttpConnection)} is invoked to read
     * the associated headers.
     * </li>
     * <li>
     * {@link #processResponseHeaders(HttpState,HttpConnection)} is invoked, allowing
     * the method to process the headers if desired.
     * </li>
     * <li>
     * {@link #readResponseBody(HttpState,HttpConnection)} is
     * invoked to read the associated body (if any).
     * </li>
     * <li>
     * {@link #processResponseBody(HttpState,HttpConnection)} is invoked, allowing the
     * method to process the response body if desired.
     * </li>
     * </ol>
     *
     * Subclasses may want to override one or more of the above methods to to
     * customize the processing. (Or they may choose to override this method
     * if dramatically different processing is required.)
     * </p>
     *
     * @param state the {@link HttpState state} information associated with this method
     * @param conn the {@link HttpConnection connection} used to execute
     *        this HTTP method
     *
     * @throws HttpException  if a protocol exception occurs.
     * @throws HttpRecoverableException if a recoverable transport error occurs. 
     *                    Usually this kind of exceptions can be recovered from by
     *                    retrying the HTTP method 
     */
    protected void readResponse(HttpState state, HttpConnection conn)
    throws HttpException {
        LOG.trace(
            "enter HttpMethodBase.readResponse(HttpState, HttpConnection)");
        try {
            // Status line & line may have already been received
            // if 'expect - continue' handshake has been used
            while (this.statusLine == null) {
                readStatusLine(state, conn);
                processStatusLine(state, conn);
                readResponseHeaders(state, conn);
                processResponseHeaders(state, conn);
                
                int status = this.statusLine.getStatusCode();
                if ((status >= 100) && (status < 200)) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Discarding unexpected response: " + this.statusLine.toString()); 
                    }
                    this.statusLine = null;
                }
            }
            readResponseBody(state, conn);
            processResponseBody(state, conn);
        } catch (IOException e) {
            throw new HttpRecoverableException(e.toString());
        }
    }

    /**
     * Read the response body from the given {@link HttpConnection}.
     *
     * <p>
     * The current implementation wraps the socket level stream with
     * an appropriate stream for the type of response (chunked, content-length,
     * or auto-close).  If there is no response body, the connection associated
     * with the request will be returned to the connection manager.
     * </p>
     *
     * <p>
     * Subclasses may want to override this method to to customize the
     * processing.
     * </p>
     *
     * @param state the {@link HttpState state} information associated with this method
     * @param conn the {@link HttpConnection connection} used to execute
     *        this HTTP method
     *
     * @throws IOException if an I/O (transport) error occurs
     * @throws HttpException  if a protocol exception occurs.
     * @throws HttpRecoverableException if a recoverable transport error occurs. 
     *                    Usually this kind of exceptions can be recovered from by
     *                    retrying the HTTP method 
     *
     * @see #readResponse
     * @see #processResponseBody
     */
    protected void readResponseBody(HttpState state, HttpConnection conn)
    throws IOException, HttpException {
        LOG.trace(
            "enter HttpMethodBase.readResponseBody(HttpState, HttpConnection)");

        // assume we are not done with the connection if we get a stream
        doneWithConnection = false;
        InputStream stream = readResponseBody(conn);
        if (stream == null) {
            // done using the connection!
            responseBodyConsumed();
        } else {
            conn.setLastResponseInputStream(stream);
            setResponseStream(stream);
        }
    }

    /**
     * Returns the response body as an {@link InputStream input stream}
     * corresponding to the values of the <tt>Content-Length</tt> and 
     * <tt>Transfer-Encoding</tt> headers. If no response body is available
     * returns <tt>null</tt>.
     * <p>
     *
     * @see #readResponse
     * @see #processResponseBody
     *
     * @param conn the {@link HttpConnection connection} used to execute
     *        this HTTP method
     *
     * @throws IOException if an I/O (transport) error occurs
     * @throws HttpException  if a protocol exception occurs.
     * @throws HttpRecoverableException if a recoverable transport error occurs. 
     *                    Usually this kind of exceptions can be recovered from by
     *                    retrying the HTTP method 
     */
    private InputStream readResponseBody(HttpConnection conn)
        throws IOException {

        LOG.trace("enter HttpMethodBase.readResponseBody(HttpConnection)");

        responseBody = null;
        InputStream is = conn.getResponseInputStream();
        if (Wire.CONTENT_WIRE.enabled()) {
            is = new WireLogInputStream(is, Wire.CONTENT_WIRE);
        }
        InputStream result = null;
        Header transferEncodingHeader = responseHeaders.getFirstHeader("Transfer-Encoding");
        // We use Transfer-Encoding if present and ignore Content-Length.
        // RFC2616, 4.4 item number 3
        if (transferEncodingHeader != null) {

            String transferEncoding = transferEncodingHeader.getValue();
            if (!"chunked".equalsIgnoreCase(transferEncoding) 
                && !"identity".equalsIgnoreCase(transferEncoding)) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Unsupported transfer encoding: " + transferEncoding);
                }
            }
            HeaderElement[] encodings = transferEncodingHeader.getValues();
            // The chunck encoding must be the last one applied
            // RFC2616, 14.41
            int len = encodings.length;            
            if ((len > 0) && ("chunked".equalsIgnoreCase(encodings[len - 1].getName()))) { 
                // if response body is empty
                if (conn.isResponseAvailable(conn.getSoTimeout())) {
                    result = new ChunkedInputStream(is, this);
                } else {
                    if (isStrictMode()) {
                        throw new HttpException("Chunk-encoded body declared but not sent");
                    } else {
                        LOG.warn("Chunk-encoded body missing");
                    }
                }
            } else {
                LOG.info("Response content is not chunk-encoded");
                // The connection must be terminated by closing 
                // the socket as per RFC 2616, 3.6
                setConnectionCloseForced(true);
                result = is;  
            }
        } else {
            int expectedLength = getResponseContentLength();
            if (expectedLength == -1) {
                if (canResponseHaveBody(statusLine.getStatusCode())) {
                    Header connectionHeader = responseHeaders.getFirstHeader("Connection");
                    String connectionDirective = null;
                    if (connectionHeader != null) {
                        connectionDirective = connectionHeader.getValue();
                    }
                    if (isHttp11() && !"close".equalsIgnoreCase(connectionDirective)) {
                        LOG.info("Response content length is not known");
                        setConnectionCloseForced(true);
                    }
                    result = is;            
                }
            } else {
                result = new ContentLengthInputStream(is, expectedLength);
            }
        } 
        // if there is a result - ALWAYS wrap it in an observer which will
        // close the underlying stream as soon as it is consumed, and notify
        // the watcher that the stream has been consumed.
        if (result != null) {

            result = new AutoCloseInputStream(
                result,
                new ResponseConsumedWatcher() {
                    public void responseConsumed() {
                        responseBodyConsumed();
                    }
                }
            );
        }

        return result;
    }

    /**
     * Reads the response headers from the given {@link HttpConnection connection}.
     *
     * <p>
     * Subclasses may want to override this method to to customize the
     * processing.
     * </p>
     *
     * <p>
     * "It must be possible to combine the multiple header fields into one
     * "field-name: field-value" pair, without changing the semantics of the
     * message, by appending each subsequent field-value to the first, each
     * separated by a comma." - HTTP/1.0 (4.3)
     * </p>
     *
     * @param state the {@link HttpState state} information associated with this method
     * @param conn the {@link HttpConnection connection} used to execute
     *        this HTTP method
     *
     * @throws IOException if an I/O (transport) error occurs
     * @throws HttpException  if a protocol exception occurs.
     * @throws HttpRecoverableException if a recoverable transport error occurs. 
     *                    Usually this kind of exceptions can be recovered from by
     *                    retrying the HTTP method 
     *
     * @see #readResponse
     * @see #processResponseHeaders
     */
    protected void readResponseHeaders(HttpState state, HttpConnection conn)
    throws IOException, HttpException {
        LOG.trace("enter HttpMethodBase.readResponseHeaders(HttpState,"
            + "HttpConnection)");

        getResponseHeaderGroup().clear();
        Header[] headers = HttpParser.parseHeaders(conn.getResponseInputStream());
        if (Wire.HEADER_WIRE.enabled()) {
            for (int i = 0; i < headers.length; i++) {
                Wire.HEADER_WIRE.input(headers[i].toExternalForm());
            }
        }
        getResponseHeaderGroup().setHeaders(headers);
    }

    /**
     * Read the status line from the given {@link HttpConnection}, setting my
     * {@link #getStatusCode status code} and {@link #getStatusText status
     * text}.
     *
     * <p>
     * Subclasses may want to override this method to to customize the
     * processing.
     * </p>
     *
     * @param state the {@link HttpState state} information associated with this method
     * @param conn the {@link HttpConnection connection} used to execute
     *        this HTTP method
     *
     * @throws IOException if an I/O (transport) error occurs
     * @throws HttpException  if a protocol exception occurs.
     * @throws HttpRecoverableException if a recoverable transport error occurs. 
     *                    Usually this kind of exceptions can be recovered from by
     *                    retrying the HTTP method 
     *
     * @see StatusLine
     */
    protected void readStatusLine(HttpState state, HttpConnection conn)
    throws IOException, HttpRecoverableException, HttpException {
        LOG.trace("enter HttpMethodBase.readStatusLine(HttpState, HttpConnection)");

        //read out the HTTP status string
        String s = conn.readLine();
        while ((s != null) && !StatusLine.startsWithHTTP(s)) {
            if (Wire.HEADER_WIRE.enabled()) {
                Wire.HEADER_WIRE.input(s + "\r\n");
            }
            s = conn.readLine();
        }
        if (s == null) {
            // A null statusString means the connection was lost before we got a
            // response.  Try again.
            throw new HttpRecoverableException("Error in parsing the status "
                + " line from the response: unable to find line starting with"
                + " \"HTTP\"");
        }
        if (Wire.HEADER_WIRE.enabled()) {
            Wire.HEADER_WIRE.input(s + "\r\n");
        }
        //create the status line from the status string
        statusLine = new StatusLine(s);

        //check for a valid HTTP-Version
        String httpVersion = statusLine.getHttpVersion();
        if (httpVersion.equals("HTTP/1.0")) {
            http11 = false;
        } else if (httpVersion.equals("HTTP/1.1")) {
            http11 = true;
        } else if (httpVersion.equals("HTTP")) {
            // some servers do not specify the version correctly, we will just assume 1.0
            http11 = false;
        } else {
            throw new HttpException("Unrecognized server protocol: '"
                                    + httpVersion + "'");
        }

    }

    // ------------------------------------------------------ Protected Methods

    /**
     * <p>
     * Sends the request via the given {@link HttpConnection connection}.
     * </p>
     *
     * <p>
     * The request is written as the following sequence of actions:
     * </p>
     *
     * <ol>
     * <li>
     * {@link #writeRequestLine(HttpState, HttpConnection)} is invoked to 
     * write the request line.
     * </li>
     * <li>
     * {@link #writeRequestHeaders(HttpState, HttpConnection)} is invoked 
     * to write the associated headers.
     * </li>
     * <li>
     * <tt>\r\n</tt> is sent to close the head part of the request.
     * </li>
     * <li>
     * {@link #writeRequestBody(HttpState, HttpConnection)} is invoked to 
     * write the body part of the request.
     * </li>
     * </ol>
     *
     * <p>
     * Subclasses may want to override one or more of the above methods to to
     * customize the processing. (Or they may choose to override this method
     * if dramatically different processing is required.)
     * </p>
     *
     * @param state the {@link HttpState state} information associated with this method
     * @param conn the {@link HttpConnection connection} used to execute
     *        this HTTP method
     *
     * @throws IOException if an I/O (transport) error occurs
     * @throws HttpException  if a protocol exception occurs.
     * @throws HttpRecoverableException if a recoverable transport error occurs. 
     *                    Usually this kind of exceptions can be recovered from by
     *                    retrying the HTTP method 
     */
    protected void writeRequest(HttpState state, HttpConnection conn)
    throws IOException, HttpException {
        LOG.trace(
            "enter HttpMethodBase.writeRequest(HttpState, HttpConnection)");
        writeRequestLine(state, conn);
        writeRequestHeaders(state, conn);
        conn.writeLine(); // close head
        // make sure the status line and headers have been sent
        conn.flushRequestOutputStream();
        if (Wire.HEADER_WIRE.enabled()) {
            Wire.HEADER_WIRE.output("\r\n");
        }

        Header expectheader = getRequestHeader("Expect");
        String expectvalue = null;
        if (expectheader != null) {
            expectvalue = expectheader.getValue();
        }
        if ((expectvalue != null) 
         && (expectvalue.compareToIgnoreCase("100-continue") == 0)) {
            if (this.isHttp11()) {
                int readTimeout = conn.getSoTimeout();
                try {
                    conn.setSoTimeout(RESPONSE_WAIT_TIME_MS);
                    readStatusLine(state, conn);
                    processStatusLine(state, conn);
                    readResponseHeaders(state, conn);
                    processResponseHeaders(state, conn);

                    if (this.statusLine.getStatusCode() == HttpStatus.SC_CONTINUE) {
                        // Discard status line
                        this.statusLine = null;
                        LOG.debug("OK to continue received");
                    } else {
                        return;
                    }
                } catch (InterruptedIOException e) {
                    // Most probably Expect header is not recongnized
                    // Remove the header to signal the method 
                    // that it's okay to go ahead with sending data
                    removeRequestHeader("Expect");
                    LOG.info("100 (continue) read timeout. Resume sending the request");
                } finally {
                    conn.setSoTimeout(readTimeout);
                }
                
            } else {
                removeRequestHeader("Expect");
                LOG.info("'Expect: 100-continue' handshake is only supported by "
                    + "HTTP/1.1 or higher");
            }
        }

        writeRequestBody(state, conn);
        // make sure the entire request body has been sent
        conn.flushRequestOutputStream();
    }

    /**
     * Writes the request body to the given {@link HttpConnection connection}.
     *
     * <p>
     * This method should return <tt>true</tt> if the request body was actually
     * sent (or is empty), or <tt>false</tt> if it could not be sent for some
     * reason.
     * </p>
     *
     * <p>
     * This implementation writes nothing and returns <tt>true</tt>.
     * </p>
     *
     * @param state the {@link HttpState state} information associated with this method
     * @param conn the {@link HttpConnection connection} used to execute
     *        this HTTP method
     *
     * @return <tt>true</tt>
     *
     * @throws IOException if an I/O (transport) error occurs
     * @throws HttpException  if a protocol exception occurs.
     * @throws HttpRecoverableException if a recoverable transport error occurs. 
     *                    Usually this kind of exceptions can be recovered from by
     *                    retrying the HTTP method 
     */
    protected boolean writeRequestBody(HttpState state, HttpConnection conn)
    throws IOException, HttpException {
        return true;
    }

    /**
     * Writes the request headers to the given {@link HttpConnection connection}.
     *
     * <p>
     * This implementation invokes {@link #addRequestHeaders(HttpState,HttpConnection)},
     * and then writes each header to the request stream.
     * </p>
     *
     * <p>
     * Subclasses may want to override this method to to customize the
     * processing.
     * </p>
     *
     * @param state the {@link HttpState state} information associated with this method
     * @param conn the {@link HttpConnection connection} used to execute
     *        this HTTP method
     *
     * @throws IOException if an I/O (transport) error occurs
     * @throws HttpException  if a protocol exception occurs.
     * @throws HttpRecoverableException if a recoverable transport error occurs. 
     *                    Usually this kind of exceptions can be recovered from by
     *                    retrying the HTTP method 
     *
     * @see #addRequestHeaders
     * @see #getRequestHeaders
     */
    protected void writeRequestHeaders(HttpState state, HttpConnection conn)
    throws IOException, HttpException {
        LOG.trace("enter HttpMethodBase.writeRequestHeaders(HttpState,"
            + "HttpConnection)");
        addRequestHeaders(state, conn);

        Header[] headers = getRequestHeaders();
        for (int i = 0; i < headers.length; i++) {
            String s = headers[i].toExternalForm();
            if (Wire.HEADER_WIRE.enabled()) {
                Wire.HEADER_WIRE.output(s);
            }
            conn.print(s);
        }
    }

    /**
     * Writes the request line to the given {@link HttpConnection connection}.
     *
     * <p>
     * Subclasses may want to override this method to to customize the
     * processing.
     * </p>
     *
     * @param state the {@link HttpState state} information associated with this method
     * @param conn the {@link HttpConnection connection} used to execute
     *        this HTTP method
     *
     * @throws IOException if an I/O (transport) error occurs
     * @throws HttpException  if a protocol exception occurs.
     * @throws HttpRecoverableException if a recoverable transport error occurs. 
     *                    Usually this kind of exceptions can be recovered from by
     *                    retrying the HTTP method 
     *
     * @see #generateRequestLine
     */
    protected void writeRequestLine(HttpState state, HttpConnection conn)
    throws IOException, HttpException {
        LOG.trace(
            "enter HttpMethodBase.writeRequestLine(HttpState, HttpConnection)");
        String requestLine = getRequestLine(conn);
        if (Wire.HEADER_WIRE.enabled()) {
            Wire.HEADER_WIRE.output(requestLine);
        }
        conn.print(requestLine);
    }

    /**
     * Returns the request line.
     * 
     * @param conn the {@link HttpConnection connection} used to execute
     *        this HTTP method
     * 
     * @return The request line.
     */
    private String getRequestLine(HttpConnection conn) {
        return  HttpMethodBase.generateRequestLine(conn, getName(),
                getPath(), getQueryString(), getHttpVersion());
    }

    /**
     * Get the HTTP version.
     *
     * @return HTTP/1.1 if version 1.1 of HTTP protocol is used, HTTP/1.0 otherwise
     *
     * @since 2.0
     */
    private String getHttpVersion() {
        return (http11 ? "HTTP/1.1" : "HTTP/1.0");
    }

    /**
     * Per RFC 2616 section 4.3, some response can never contain a message
     * body.
     *
     * @param status - the HTTP status code
     *
     * @return <tt>true</tt> if the message may contain a body, <tt>false</tt> if it can not
     *         contain a message body
     */
    private static boolean canResponseHaveBody(int status) {
        LOG.trace("enter HttpMethodBase.canResponseHaveBody(int)");

        boolean result = true;

        if ((status >= 100 && status <= 199) || (status == 204)
            || (status == 304)) { // NOT MODIFIED
            result = false;
        }

        return result;
    }

    /**
     * Processes a response that requires authentication
     *
     * @param state the {@link HttpState state} information associated with this method
     * @param conn the {@link HttpConnection connection} used to execute
     *        this HTTP method
     *
     * @return true if the request has completed process, false if more
     *         attempts are needed
     */
    private boolean processAuthenticationResponse(HttpState state, HttpConnection conn) {
        LOG.trace("enter HttpMethodBase.processAuthenticationResponse("
            + "HttpState, HttpConnection)");

        if (this.proxyAuthScheme instanceof NTLMScheme) {
            removeRequestHeader(HttpAuthenticator.PROXY_AUTH_RESP);
        }
        if (this.authScheme instanceof NTLMScheme) {
            removeRequestHeader(HttpAuthenticator.WWW_AUTH_RESP);
        }
        int statusCode = statusLine.getStatusCode();
        // handle authentication required
        Header[] challenges = null;
        Set realmsUsed = null;
        String host = null;
        switch (statusCode) {
            case HttpStatus.SC_UNAUTHORIZED:
                challenges = getResponseHeaderGroup().getHeaders(HttpAuthenticator.WWW_AUTH);
                realmsUsed = realms;
                host = conn.getVirtualHost();
                if (host == null) {
                    host = conn.getHost();
                }
                break;
            case HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED:
                challenges = getResponseHeaderGroup().getHeaders(HttpAuthenticator.PROXY_AUTH);
                realmsUsed = proxyRealms;
                host = conn.getProxyHost();
                break;
        }
        boolean authenticated = false;
        // if there was a header requesting authentication
        if (challenges.length > 0) {
            AuthScheme authscheme = null;
            try {
                authscheme = HttpAuthenticator.selectAuthScheme(challenges);
            } catch (MalformedChallengeException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getMessage(), e);
                }
                return true;
            } catch (UnsupportedOperationException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getMessage(), e);
                }
                return true;
            }
        
            StringBuffer buffer = new StringBuffer();
            buffer.append(host);
            buffer.append('#');
            buffer.append(authscheme.getID());
            String realm = buffer.toString();

            if (realmsUsed.contains(realm)) {
                if (LOG.isInfoEnabled()) {
                    buffer = new StringBuffer();
                    buffer.append("Already tried to authenticate with '");
                    buffer.append(authscheme.getRealm());
                    buffer.append("' authentication realm at ");
                    buffer.append(host);
                    buffer.append(", but still receiving: ");
                    buffer.append(statusLine.toString());
                    LOG.info(buffer.toString());
                }
                return true;
            } else {
                realmsUsed.add(realm);
            }

            try {
                //remove preemptive header and reauthenticate
                switch (statusCode) {
                    case HttpStatus.SC_UNAUTHORIZED:
                        removeRequestHeader(HttpAuthenticator.WWW_AUTH_RESP);
                        authenticated = HttpAuthenticator.authenticate(
                            authscheme, this, conn, state);
                        this.realm = authscheme.getRealm();
                        this.authScheme = authscheme;
                        break;
                    case HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED:
                        removeRequestHeader(HttpAuthenticator.PROXY_AUTH_RESP);
                        authenticated = HttpAuthenticator.authenticateProxy(
                            authscheme, this, conn, state);
                        this.proxyRealm = authscheme.getRealm();
                        this.proxyAuthScheme = authscheme;
                        break;
                }
            } catch (AuthenticationException e) {
                LOG.warn(e.getMessage());
                return true; // finished request
            }
            if (!authenticated) {
                // won't be able to authenticate to this challenge
                // without additional information
                LOG.debug("HttpMethodBase.execute(): Server demands "
                          + "authentication credentials, but none are "
                          + "available, so aborting.");
            } else {
                LOG.debug("HttpMethodBase.execute(): Server demanded "
                          + "authentication credentials, will try again.");
                // let's try it again, using the credentials
            }
        }

        return !authenticated; // finished processing if we aren't authenticated
    }

    /**
     * Returns proxy authentication realm, if it has been used during authentication process. 
     * Otherwise returns <tt>null</tt>.
     * 
     * @return proxy authentication realm
     */
    public String getProxyAuthenticationRealm() {
        return this.proxyRealm;
    }

    /**
     * Returns authentication realm, if it has been used during authentication process. 
     * Otherwise returns <tt>null</tt>.
     * 
     * @return authentication realm
     */
    public String getAuthenticationRealm() {
        return this.realm;
    }

    /**
     * Sends the request and reads the response. The request will be retried 
     * {@link #maxRetries} times if the operation fails with a
     * {@link HttpRecoverableException}.
     *
     * <p>
     * The {@link #isUsed()} is set to true if the write succeeds.
     * </p>
     *
     * @param state the {@link HttpState state} information associated with this method
     * @param conn the {@link HttpConnection connection} used to execute
     *        this HTTP method
     *
     * @throws IOException if an I/O (transport) error occurs
     * @throws HttpException  if a protocol exception occurs.
     * @throws HttpRecoverableException if a recoverable transport error occurs. 
     *                    Usually this kind of exceptions can be recovered from by
     *                    retrying the HTTP method 
     *
     * @see #writeRequest(HttpState,HttpConnection)
     * @see #readResponse(HttpState,HttpConnection)
     */
    private void processRequest(HttpState state, HttpConnection connection)
    throws HttpException, IOException {
        LOG.trace("enter HttpMethodBase.processRequest(HttpState, HttpConnection)");

        int execCount = 0;
        boolean requestSent = false;
        
        // loop until the method is successfully processed, the retryHandler 
        // returns false or a non-recoverable exception is thrown
        while (true) {
            execCount++;
            requestSent = false;
            
            if (LOG.isTraceEnabled()) {
                LOG.trace("Attempt number " + execCount + " to process request");
            }
            try {
                if (!connection.isOpen()) {
                    LOG.debug("Opening the connection.");
                    connection.open();
                }
                writeRequest(state, connection);
                requestSent = true;
                readResponse(state, connection);
                // the method has successfully executed
                used = true; 
                break;
            } catch (HttpRecoverableException httpre) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Closing the connection.");
                }
                connection.close();
                LOG.info("Recoverable exception caught when processing request");
                // update the recoverable exception count.
                recoverableExceptionCount++;
                
                // test if this method should be retried                
                if (!getMethodRetryHandler().retryMethod(
                        this, 
                        connection, 
                        httpre, 
                        execCount, 
                        requestSent)
                ) {
                    LOG.warn(
                        "Recoverable exception caught but MethodRetryHandler.retryMethod() "
                        + "returned false, rethrowing exception"
                    );
                    // this connection can no longer be used, it has been closed
                    doneWithConnection = true;
                    throw httpre;
                }
            } catch (IOException e) {
                connection.close();
                doneWithConnection = true;
                throw e;
            } catch (RuntimeException e) {
                connection.close();
                doneWithConnection = true;
                throw e;
            }
        }
    }

    /**
     * Returns the character set from the <tt>Content-Type</tt> header.
     * @param contentheader The content header.
     * @return String The character set.
     */
    protected static String getContentCharSet(Header contentheader) {
        LOG.trace("enter getContentCharSet( Header contentheader )");
        String charset = null;
        if (contentheader != null) {
            try {
                HeaderElement values[] = contentheader.getValues();
                // I expect only one header element to be there
                // No more. no less
                if (values.length == 1) {
                    NameValuePair param = values[0].getParameterByName("charset");
                    if (param != null) {
                        // If I get anything "funny" 
                        // UnsupportedEncondingException will result
                        charset = param.getValue();
                    }
                }
            } catch (HttpException e) {
                LOG.error(e);
            }
        }
        if (charset == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Default charset used: " + HttpConstants.DEFAULT_CONTENT_CHARSET);
            }
            charset = HttpConstants.DEFAULT_CONTENT_CHARSET;
        }
        return charset;
    }


    /**
     * Returns the character encoding of the request from the <tt>Content-Type</tt> header.
     * 
     * @return String The character set.
     */
    public String getRequestCharSet() {
        return getContentCharSet(getRequestHeader("Content-Type"));
    }


    /**  
     * Returns the character encoding of the response from the <tt>Content-Type</tt> header.
     * 
     * @return String The character set.
     */
    public String getResponseCharSet() {
        return getContentCharSet(getResponseHeader("Content-Type"));
    }

    /**
     * Returns the number of "recoverable" exceptions thrown and handled, to
     * allow for monitoring the quality of the connection.
     *
     * @return The number of recoverable exceptions handled by the method.
     */
    public int getRecoverableExceptionCount() {
        return recoverableExceptionCount;
    }

    /**
     * A response has been consumed.
     *
     * <p>The default behavior for this class is to check to see if the connection
     * should be closed, and close if need be, and to ensure that the connection
     * is returned to the connection manager - if and only if we are not still
     * inside the execute call.</p>
     *
     */
    protected void responseBodyConsumed() {

        // make sure this is the initial invocation of the notification,
        // ignore subsequent ones.
        responseStream = null;
        if (responseConnection != null) {
            responseConnection.setLastResponseInputStream(null);

            if (shouldCloseConnection(responseConnection)) {
                responseConnection.close();
            }
        }
        this.connectionCloseForced = false;
        doneWithConnection = true;
        if (!inExecute) {
            ensureConnectionRelease();
        }
    }

    /**
     * Insure that the connection is released back to the pool.
     */
    private void ensureConnectionRelease() {
        if (responseConnection != null) {
            responseConnection.releaseConnection();
            responseConnection = null;
        }
    }

    /**
     * Returns the {@link HostConfiguration host configuration}.
     * 
     * @return the host configuration
     */
    public HostConfiguration getHostConfiguration() {
        return hostConfiguration;
    }

    /**
     * Sets the {@link HostConfiguration host configuration}.
     * 
     * @param hostConfiguration The hostConfiguration to set
     */
    public void setHostConfiguration(HostConfiguration hostConfiguration) {
        this.hostConfiguration = hostConfiguration;
    }

    /**
     * Returns the {@link MethodRetryHandler retry handler} for this HTTP method
     * 
     * @return the methodRetryHandler
     */
    public MethodRetryHandler getMethodRetryHandler() {
        
        if (methodRetryHandler == null) {
            methodRetryHandler = new DefaultMethodRetryHandler();
        }

        return methodRetryHandler;
    }

    /**
     * Sets the {@link MethodRetryHandler retry handler} for this HTTP method
     * 
     * @param handler the methodRetryHandler to use when this method executed
     */
    public void setMethodRetryHandler(MethodRetryHandler handler) {
        methodRetryHandler = handler;
    }

    /**
     * This method is a dirty hack intended to work around 
     * current (2.0) design flaw that prevents the user from
     * obtaining correct status code, headers and response body from the 
     * preceding HTTP CONNECT method.
     * 
     * TODO: Remove this crap as soon as possible
     */
    protected void fakeResponse(
        StatusLine statusline, 
        HeaderGroup responseheaders,
        InputStream responseStream
    ) {
        // set used so that the response can be read
        this.used = true;
        this.statusLine = statusline;
        this.responseHeaders = responseheaders;
        this.responseBody = null;
        this.responseStream = responseStream;
    }
}
