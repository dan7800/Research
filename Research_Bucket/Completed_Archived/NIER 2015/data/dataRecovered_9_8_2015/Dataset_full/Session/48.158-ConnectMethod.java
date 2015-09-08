/*
 * $Header: /home/cvs/jakarta-commons/httpclient/src/java/org/apache/commons/httpclient/ConnectMethod.java,v 1.18.2.3 2004/06/25 03:27:40 mbecke Exp $
 * $Revision: 1.18.2.3 $
 * $Date: 2004/06/25 03:27:40 $
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

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>Wraps another method to tunnel through a proxy.</p>
 *
 * @author Ortwin Gl�ck
 * @author dIon Gillard
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * @since 2.0
 * @version $Revision: 1.18.2.3 $ $Date: 2004/06/25 03:27:40 $
 */
public class ConnectMethod extends HttpMethodBase {
    /** the name of this method */
    public static final String NAME = "CONNECT";

    /**
     * Create a connect method wrapping the existing method
     *
     * @param method the {@link HttpMethod method} to execute after connecting
     *      to the server
     */
    public ConnectMethod(HttpMethod method) {
        LOG.trace("enter ConnectMethod(HttpMethod)");
        this.method = method;
    }

    /**
     * Provide the {@link #NAME name} of this method.
     *
     * @return the String "CONNECT"
     */
    public String getName() {
        return NAME;
    }


    /**
     * This method does nothing. <tt>CONNECT</tt> request is not supposed 
     * to contain <tt>Authorization</tt> request header.
     *
     * @param state current state of http requests
     * @param conn the connection to use for I/O
     *
     * @throws IOException when errors occur reading or writing to/from the
     *         connection
     * @throws HttpException when a recoverable error occurs
     *  
     * @see HttpMethodBase#addAuthorizationRequestHeader(HttpState, HttpConnection)
     */
    protected void addAuthorizationRequestHeader(HttpState state, HttpConnection conn)
        throws IOException, HttpException {
        // Do nothing. Not applicable to CONNECT method
    }

    /**
     * This method does nothing. <tt>CONNECT</tt> request is not supposed 
     * to contain <tt>Content-Length</tt> request header.
     *
     * @param state current state of http requests
     * @param conn the connection to use for I/O
     *
     * @throws IOException when errors occur reading or writing to/from the
     *         connection
     * @throws HttpException when a recoverable error occurs
     *  
     * @see HttpMethodBase#addContentLengthRequestHeader(HttpState, HttpConnection)
     */
    protected void addContentLengthRequestHeader(HttpState state, HttpConnection conn)
        throws IOException, HttpException {
        // Do nothing. Not applicable to CONNECT method
    }

    /**
     * This method does nothing. <tt>CONNECT</tt> request is not supposed 
     * to contain <tt>Cookie</tt> request header.
     *
     * @param state current state of http requests
     * @param conn the connection to use for I/O
     *
     * @throws IOException when errors occur reading or writing to/from the
     *         connection
     * @throws HttpException when a recoverable error occurs
     *  
     * @see HttpMethodBase#addCookieRequestHeader(HttpState, HttpConnection)
     */
    protected void addCookieRequestHeader(HttpState state, HttpConnection conn)
        throws IOException, HttpException {
        // Do nothing. Not applicable to CONNECT method
    }


    /**
     * Populates the request headers map to with additional {@link Header
     * headers} to be submitted to the given {@link HttpConnection}.
     *
     * <p>
     * This implementation adds <tt>User-Agent</tt>, <tt>Host</tt>,
     * and <tt>Proxy-Authorization</tt> headers, when appropriate.
     * </p>
     *
     * @param state the client state
     * @param conn the {@link HttpConnection} the headers will eventually be
     *        written to
     * @throws IOException when an error occurs writing the request
     * @throws HttpException when a HTTP protocol error occurs
     *
     * @see #writeRequestHeaders
     */
    protected void addRequestHeaders(HttpState state, HttpConnection conn)
        throws IOException, HttpException {
        LOG.trace("enter ConnectMethod.addRequestHeaders(HttpState, "
            + "HttpConnection)");
        addUserAgentRequestHeader(state, conn);
        addHostRequestHeader(state, conn);
        addProxyAuthorizationRequestHeader(state, conn);
        addProxyConnectionHeader(state, conn);
    }

    /**
     * Execute this method by tunnelling and then executing the wrapped method.
     *
     * @param state the current http state
     * @param conn the connection to write to
     * @return the http status code from execution
     * @throws HttpException when an error occurs writing the headers
     * @throws IOException when an error occurs writing the headers
     */
    public int execute(HttpState state, HttpConnection conn) 
    throws IOException, HttpException {

        LOG.trace("enter ConnectMethod.execute(HttpState, HttpConnection)");
        int code = super.execute(state, conn);
        LOG.debug("CONNECT status code " + code);
        if ((code >= 200) && (code < 300)) {
            conn.tunnelCreated();
            code = method.execute(state, conn);
        } else {
            // What is to follow is an ugly hack.
            // I REALLY hate having to resort to such
            // an appalling trick
            // TODO: Connect method must be redesigned.
            // The only feasible solution is to split monolithic
            // HttpMethod into HttpRequest/HttpResponse pair.
            // That would allow to execute CONNECT method 
            // behind the scene and return CONNECT HttpResponse 
            // object in response to the original request that 
            // contains the correct status line, headers & 
            // response body.
            
            LOG.debug("CONNECT failed, fake the response for the original method");
            if (method instanceof HttpMethodBase) {
                // Pass the status, headers and response stream to the wrapped
                // method.
                // To ensure that the connection is not released more than once
                // this method is still responsible for releasing the connection. 
                // This will happen when the response body is consumed, or when
                // the wrapped method closes the response connection in 
                // releaseConnection().
                ((HttpMethodBase) method).fakeResponse(
                    getStatusLine(), 
                    getResponseHeaderGroup(),
                    getResponseStream()
                );
            } else {
                releaseConnection();
            }
        }
        return code;
    }

    /**
     * Special Connect request.
     *
     * @param state the current http state
     * @param conn the connection to write to
     * @throws IOException when an error occurs writing the request
     * @throws HttpException when an error occurs writing the request
     */
    protected void writeRequestLine(HttpState state, HttpConnection conn)
    throws IOException, HttpException {
        int port = conn.getPort();
        if (port == -1) {
            port = conn.getProtocol().getDefaultPort();  
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append(getName()); 
        buffer.append(' '); 
        buffer.append(conn.getHost()); 
        if (port > -1) {
            buffer.append(':'); 
            buffer.append(port); 
        }
        buffer.append(" HTTP/1.1"); 
        String line = buffer.toString();
        conn.printLine(line);
        if (Wire.HEADER_WIRE.enabled()) {
            Wire.HEADER_WIRE.output(line);
        }
    }

    /**
     * Returns <code>true</code> if the status code is anything other than
     * SC_OK, <code>false</code> otherwise.
     * 
     * @param conn the connection to test
     * 
     * @return <code>true</code> if the connection should be closed
     * 
     * @see HttpMethodBase#shouldCloseConnection(HttpConnection)
     * @see HttpStatus#SC_OK
     */
    protected boolean shouldCloseConnection(HttpConnection conn) {
        if (getStatusCode() == HttpStatus.SC_OK) {
            Header connectionHeader = null;
            if (!conn.isTransparent()) {
                connectionHeader = getResponseHeader("proxy-connection");
            }
            if (connectionHeader == null) {
                connectionHeader = getResponseHeader("connection");
            }
            if (connectionHeader != null) {
                if (connectionHeader.getValue().equalsIgnoreCase("close")) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("Invalid header encountered '" + connectionHeader.toExternalForm() 
                            + "' in response " + getStatusLine().toString());
                    }
                }
            }
            return false;
        } else {
            return super.shouldCloseConnection(conn);
        }
    }
    
    /** Log object for this class. */
    private static final Log LOG = LogFactory.getLog(ConnectMethod.class);

    /** The wrapped method */
    private HttpMethod method;

}
