/*
 * $Header: /home/cvs/jakarta-commons/httpclient/src/java/org/apache/commons/httpclient/auth/BasicScheme.java,v 1.4.2.3 2004/02/22 18:21:14 olegk Exp $
 * $Revision: 1.4.2.3 $
 * $Date: 2004/02/22 18:21:14 $
 *
 * ====================================================================
 *
 *  Copyright 2002-2004 The Apache Software Foundation
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

package org.apache.commons.httpclient.auth;

import org.apache.commons.httpclient.HttpConstants; 
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.util.Base64; 
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * Basic authentication scheme as defined in RFC 2617.
 * </p>
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

public class BasicScheme extends RFC2617Scheme {
    
    /** Log object for this class. */
    private static final Log LOG = LogFactory.getLog(BasicScheme.class);
    
    /**
     * Constructor for the basic authetication scheme.
     * 
     * @param challenge authentication challenge
     * 
     * @throws MalformedChallengeException is thrown if the authentication challenge
     * is malformed
     */
    public BasicScheme(final String challenge) throws MalformedChallengeException {
        super(challenge);
    }


    /**
     * Returns textual designation of the basic authentication scheme.
     * 
     * @return <code>basic</code>
     */
    public String getSchemeName() {
        return "basic";
    }

    /**
     * Produces basic authorization string for the given set of 
     * {@link Credentials}.
     * 
     * @param credentials The set of credentials to be used for athentication
     * @param method Method name is ignored by the basic authentication scheme
     * @param uri URI is ignored by the basic authentication scheme
     * @throws AuthenticationException if authorization string cannot 
     *   be generated due to an authentication failure
     * 
     * @return a basic authorization string
     */
    public String authenticate(Credentials credentials, String method, String uri)
      throws AuthenticationException {

        LOG.trace("enter BasicScheme.authenticate(Credentials, String, String)");

        UsernamePasswordCredentials usernamepassword = null;
        try {
            usernamepassword = (UsernamePasswordCredentials) credentials;
        } catch (ClassCastException e) {
            throw new AuthenticationException(
             "Credentials cannot be used for basic authentication: " 
              + credentials.getClass().getName());
        }
        return BasicScheme.authenticate(usernamepassword);
    }

    /**
     * Return a basic <tt>Authorization</tt> header value for the given 
     * {@link UsernamePasswordCredentials}.
     * 
     * @param credentials The credentials to encode.
     * 
     * @return a basic authorization string
     */
    public static String authenticate(UsernamePasswordCredentials credentials) {

        LOG.trace("enter BasicScheme.authenticate(UsernamePasswordCredentials)");

        if (credentials == null) {
            throw new IllegalArgumentException("Credentials may not be null"); 
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append(credentials.getUserName());
        buffer.append(":");
        buffer.append(credentials.getPassword());
        
        return "Basic " + HttpConstants.getAsciiString(
            Base64.encode(HttpConstants.getContentBytes(buffer.toString())));
    }
}
