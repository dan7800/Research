// ========================================================================
// $Id: Authenticator.java,v 1.2 2004/05/09 20:31:40 gregwilkins Exp $
// Copyright 2003-2004 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at 
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================

package org.mortbay.http;

import java.io.IOException;
import java.io.Serializable;
import java.security.Principal;

/** Authenticator Interface.
 * This is the interface that must be implemented to provide authentication implementations to the HttpContext.
 */
public interface Authenticator extends Serializable
{
    /** Authenticate.
     * @param realm an <code>UserRealm</code> value
     * @param pathInContext a <code>String</code> value
     * @param request a <code>HttpRequest</code> value
     * @param response a <code>HttpResponse</code> value. If non-null response is passed, 
     *              then a failed authentication will result in a challenge response being 
     *              set in the response.
     * @return User <code>Principal</code> if authenticated. Null if Authentication
     * failed. If the SecurityConstraint.__NOBODY instance is returned,
     * the request is considered as part of the authentication process.
     * @exception IOException if an error occurs
     */
    public Principal authenticate(
        UserRealm realm,
        String pathInContext,
        HttpRequest request,
        HttpResponse response)
        throws IOException;

    /* ------------------------------------------------------------ */
    public String getAuthMethod();
}
/*
 * $Header: /home/cvs/jakarta-commons/httpclient/src/java/org/apache/commons/httpclient/Attic/Authenticator.java,v 1.46.2.1 2004/02/22 18:21:12 olegk Exp $
 * $Revision: 1.46.2.1 $
 * $Date: 2004/02/22 18:21:12 $
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

import java.util.ArrayList;
import org.apache.commons.httpclient.auth.HttpAuthenticator; 
import org.apache.commons.httpclient.auth.AuthScheme; 
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility methods for HTTP authorization and authentication.  This class
 * provides utility methods for generating responses to HTTP www and proxy
 * authentication challenges.
 * 
 * <blockquote>
 * A client SHOULD assume that all paths at or deeper than the depth of the
 * last symbolic element in the path field of the Request-URI also are within
 * the protection space specified by the BasicScheme realm value of the current
 * challenge. A client MAY preemptively send the corresponding Authorization
 * header with requests for resources in that space without receipt of another
 * challenge from the server. Similarly, when a client sends a request to a
 * proxy, it may reuse a userid and password in the Proxy-Authorization header
 * field without receiving another challenge from the proxy server.
 * </blockquote>
 * </p>
 * 
 * @deprecated use {@link org.apache.commons.httpclient.auth.HttpAuthenticator}
 * 
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 * @author Rodney Waldhoff
 * @author <a href="mailto:jsdever@apache.org">Jeff Dever</a>
 * @author Ortwin Glück
 * @author Sean C. Sullivan
 * @author <a href="mailto:adrian@ephox.com">Adrian Sutton</a>
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 */
public class Authenticator {

    // -------------------------------------- Static variables and initializers

    /**
     * <tt>org.apache.commons.httpclient.Authenticator</tt> LOG.
     */
    private static final Log LOG = LogFactory.getLog(Authenticator.class);

    /**
     * The www authenticate challange header.
     */
    public static final String WWW_AUTH = "WWW-Authenticate";


    /**
     * The www authenticate response header.
     */
    public static final String WWW_AUTH_RESP = "Authorization";


    /**
     * The proxy authenticate challange header.
     */
    public static final String PROXY_AUTH = "Proxy-Authenticate";


    /**
     * The proxy authenticate response header.
     */
    public static final String PROXY_AUTH_RESP = "Proxy-Authorization";


    // ---------------------------------------------------------------- Methods

    /**
     * Add requisite authentication credentials to the given <i>method</i> in
     * the given <i>state</i> if possible.
     * 
     * @param method the HttpMethod which requires authentication
     * @param state the HttpState object providing Credentials
     * @return true if the Authenticate response header was added
     * @throws HttpException when a parsing or other error occurs
     * @throws UnsupportedOperationException when the challenge type is not
     *         supported
     * @see HttpState#setCredentials(String,Credentials)
     * 
     * @deprecated use {@link 
     * HttpAuthenticator#authenticate(AuthScheme, HttpMethod, HttpConnection, HttpState)}
     */
    public static boolean authenticate(HttpMethod method, HttpState state)
        throws HttpException, UnsupportedOperationException {

        LOG.trace("enter Authenticator.authenticate(HttpMethod, HttpState)");

        return authenticate(method, state, false);
    }


    /**
     * Add requisite proxy authentication credentials to the given
     * <i>method</i> in the given <i>state</i> if possible.
     * 
     * @param method the HttpMethod which requires authentication
     * @param state the HttpState object providing Credentials
     * @return true if the Authenticate response header was added
     * @throws HttpException when a parsing or other error occurs
     * @throws UnsupportedOperationException when the given challenge type is
     *         not supported
     * @see HttpState#setProxyCredentials(String,Credentials)
     * 
     * @deprecated use {@link 
     * HttpAuthenticator#authenticateProxy(AuthScheme, HttpMethod, HttpConnection, HttpState)}
     */
    public static boolean authenticateProxy(HttpMethod method, HttpState state)
        throws HttpException, UnsupportedOperationException {

        LOG.trace("enter Authenticator.authenticateProxy(HttpMethod, "
                  + "HttpState)");

        return authenticate(method, state, true);
    }


    /**
     * Add requisite authentication credentials to the given <i>method</i>
     * using the given the <i>challengeHeader</i>. Currently <b>BasicScheme</b> and
     * <b>DigestScheme</b> authentication are supported. If the challengeHeader is
     * null, the default authentication credentials will be sent.
     * 
     * @param method the http method to add the authentication header to
     * @param state the http state object providing {@link Credentials}
     * @param proxy a flag indicating if the authentication is against a proxy
     * 
     * @return true if a response header was added
     * 
     * @throws HttpException when an error occurs parsing the challenge
     * @throws UnsupportedOperationException when the given challenge type is
     *         not supported
     * @see #basic
     * @see #digest
     * @see HttpMethod#addRequestHeader
     */
    private static boolean authenticate(HttpMethod method, HttpState state, 
        boolean proxy)
        throws HttpException, UnsupportedOperationException {

        LOG.trace("enter Authenticator.authenticate(HttpMethod, HttpState, "
                  + "Header, String)");
        return authenticate(method, null, state, proxy);
   }

    private static boolean authenticate(HttpMethod method, HttpConnection conn,
            HttpState state, boolean proxy)
            throws HttpException, UnsupportedOperationException {
        String challengeheader = proxy ? PROXY_AUTH : WWW_AUTH;

        // I REALLY hate doing this, but I need to avoid multiple autorization
        // headers being condenced itno one. Currently HttpMethod interface
        // does not provide this kind of functionality
        Header[] headers = method.getResponseHeaders();
        ArrayList headerlist = new ArrayList();
        for (int i = 0; i < headers.length; i++) {
            Header header = headers[i];
            if (header.getName().equalsIgnoreCase(challengeheader)) {
                headerlist.add(header);
            }
        }        
        headers = (Header[]) headerlist.toArray(new Header[headerlist.size()]);        
        headerlist = null;

        //if there is no challenge, attempt to use preemptive authorization
        if (headers.length == 0) {
            if (state.isAuthenticationPreemptive()) {
                LOG.debug("Preemptively sending default basic credentials");
                if (proxy) {
                    return HttpAuthenticator.authenticateProxyDefault(method, conn, state);
                } else {
                    return HttpAuthenticator.authenticateDefault(method, conn, state);
                }
            }
            return false;
        }

        // parse the authenticate headers
        AuthScheme authscheme = HttpAuthenticator.selectAuthScheme(headers);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Using " + authscheme.getSchemeName() + " authentication scheme");
        }
        if (proxy) {
            return HttpAuthenticator.authenticateProxy(authscheme, method, conn, state);
        } else {
            return HttpAuthenticator.authenticate(authscheme, method, conn, state);
        }

    }
}
