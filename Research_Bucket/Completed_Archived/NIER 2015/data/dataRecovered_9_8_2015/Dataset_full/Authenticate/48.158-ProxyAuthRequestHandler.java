/*
 * $Header: /home/cvs/jakarta-commons/httpclient/src/test/org/apache/commons/httpclient/server/ProxyAuthRequestHandler.java,v 1.1.2.2 2004/02/22 18:21:18 olegk Exp $
 * $Revision: 1.1.2.2 $
 * $Date: 2004/02/22 18:21:18 $
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

package org.apache.commons.httpclient.server;

import java.io.IOException;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.auth.AuthenticationException;
import org.apache.commons.httpclient.auth.BasicScheme;
import org.apache.commons.httpclient.auth.HttpAuthenticator;
import org.apache.commons.httpclient.auth.MalformedChallengeException;

/**
 * This request handler guards access to a proxy when used in a 
 * request handler chain. It checks the headers for valid credentials
 * and performs the authentication handshake if necessary.
 * 
 * @author Ortwin Glueck
 */
public class ProxyAuthRequestHandler implements HttpRequestHandler {
    private Credentials credentials;
    
    /**
     * TODO replace creds parameter with a class specific to an auth scheme encapsulating all required information for a specific scheme
     * @param creds
     */
    public ProxyAuthRequestHandler(Credentials creds)  {
        if (creds == null) throw new IllegalArgumentException("Credentials can not be null");
        this.credentials = creds;
    }

    public boolean processRequest(SimpleHttpServerConnection conn)
        throws IOException {
        Header[] headers = conn.getHeaders();
        Header clientAuth = findHeader(headers, HttpAuthenticator.PROXY_AUTH_RESP);
        if (clientAuth != null) {
            boolean ok = checkAuthorization(clientAuth);
            if (ok) conn.connectionKeepAlive();
            return !ok;
        } else {
            performHandshake(conn);
        }
        return true;
    }
    
    /**
     * @param conn
     */
    private void performHandshake(SimpleHttpServerConnection conn) throws IOException {
        Header challenge = createChallenge();
        ResponseWriter out = conn.getWriter();
        out.println("HTTP/1.1 407 Proxy Authentication Required");
        out.print(challenge.toExternalForm());
        out.print(new Header("Proxy-Connection", "Keep-Alive").toExternalForm());
        out.print(new Header("Content-Length", "0").toExternalForm());
        out.println();
        out.flush();
        conn.connectionKeepAlive();
    }

    /**
     * 
     * @return
     */
    private Header createChallenge() {
        Header header = new Header();
        header.setName(HttpAuthenticator.PROXY_AUTH);
        //TODO add more auth schemes
        String challenge = "basic realm=test";
        header.setValue(challenge);
        return header;
    }

    /**
     * Checks if the credentials provided by the client match the required credentials
     * @return true if the client is authorized, false if not.
     * @param clientAuth
     */
    private boolean checkAuthorization(Header clientAuth) {
        // TODO Auto-generated method stub
        BasicScheme scheme;
        try {
            scheme = new BasicScheme("basic realm=test");
            String expectedAuthString = scheme.authenticate(credentials, null, null);
            return expectedAuthString.equals(clientAuth.getValue());
        } catch (MalformedChallengeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (AuthenticationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    private Header findHeader(Header[] headers, String name) {
        for(int i=0; i<headers.length; i++) {
            Header header = headers[i];
            if (header.getName().equalsIgnoreCase(name)) return header;
        }
        return null;
    }

}
