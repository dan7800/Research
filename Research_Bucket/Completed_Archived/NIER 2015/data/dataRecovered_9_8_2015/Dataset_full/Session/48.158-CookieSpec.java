/*
 * $Header: /home/cvs/jakarta-commons/httpclient/src/java/org/apache/commons/httpclient/cookie/CookieSpec.java,v 1.6.2.2 2004/02/22 18:21:15 olegk Exp $
 * $Revision: 1.6.2.2 $
 * $Date: 2004/02/22 18:21:15 $
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

package org.apache.commons.httpclient.cookie;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.Cookie;

/**
 * Defines the cookie management specification.
 * <p>Cookie management specification must define
 * <ul>
 *   <li> rules of parsing "Set-Cookie" header
 *   <li> rules of validation of parsed cookies
 *   <li>  formatting of "Cookie" header 
 * </ul>
 * for a given host, port and path of origin
 * 
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * @author <a href="mailto:jsdever@apache.org">Jeff Dever</a>
 *
 * @since 2.0
 */
public interface CookieSpec {    

    /** Path delimiter */
    String PATH_DELIM = "/";

    /** Path delimiting charachter */
    char PATH_DELIM_CHAR = PATH_DELIM.charAt(0);

    /**
      * Parse the <tt>"Set-Cookie"</tt> header value into Cookie array.
      *
      * @param host the host which sent the <tt>Set-Cookie</tt> header
      * @param port the port which sent the <tt>Set-Cookie</tt> header
      * @param path the path which sent the <tt>Set-Cookie</tt> header
      * @param secure <tt>true</tt> when the <tt>Set-Cookie</tt> header 
      *  was received over secure conection
      * @param header the <tt>Set-Cookie</tt> received from the server
      * @return an array of <tt>Cookie</tt>s parsed from the Set-Cookie value
      * @throws MalformedCookieException if an exception occurs during parsing
      * @throws IllegalArgumentException if an input parameter is illegal
      */
    Cookie[] parse(String host, int port, String path, boolean secure,
      final String header)
      throws MalformedCookieException, IllegalArgumentException;

    /**
      * Parse the <tt>"Set-Cookie"</tt> Header into an array of Cookies.
      *
      * @param host the host which sent the <tt>Set-Cookie</tt> header
      * @param port the port which sent the <tt>Set-Cookie</tt> header
      * @param path the path which sent the <tt>Set-Cookie</tt> header
      * @param secure <tt>true</tt> when the <tt>Set-Cookie</tt> header 
      *  was received over secure conection
      * @param header the <tt>Set-Cookie</tt> received from the server
      * @return an array of <tt>Cookie</tt>s parsed from the header
      * @throws MalformedCookieException if an exception occurs during parsing
      * @throws IllegalArgumentException if an input parameter is illegal
      */
    Cookie[] parse(String host, int port, String path, boolean secure, 
      final Header header)
      throws MalformedCookieException, IllegalArgumentException;

    /**
      * Parse the cookie attribute and update the corresponsing Cookie 
      *  properties.
      *
      * @param attribute cookie attribute from the <tt>Set-Cookie</tt>
      * @param cookie the to be updated
      * @throws MalformedCookieException if an exception occurs during parsing
      * @throws IllegalArgumentException if an input parameter is illegal
      */
    void parseAttribute(final NameValuePair attribute, final Cookie cookie)
      throws MalformedCookieException, IllegalArgumentException;

    /**
      * Validate the cookie according to validation rules defined by the 
      *  cookie specification.
      *
      * @param host the host from which the {@link Cookie} was received
      * @param port the port from which the {@link Cookie} was received
      * @param path the path from which the {@link Cookie} was received
      * @param secure <tt>true</tt> when the {@link Cookie} was received 
      *  using a secure connection
      * @param cookie the Cookie to validate
      * @throws MalformedCookieException if the cookie is invalid
      * @throws IllegalArgumentException if an input parameter is illegal
      */
    void validate(String host, int port, String path, boolean secure, 
      final Cookie cookie) 
      throws MalformedCookieException, IllegalArgumentException;
    
    /**
     * Determines if a Cookie matches a location.
     *
     * @param host the host to which the request is being submitted
     * @param port the port to which the request is being submitted
     * @param path the path to which the request is being submitted
     * @param secure <tt>true</tt> if the request is using a secure connection
     * @param cookie the Cookie to be matched
     *
     * @return <tt>true</tt> if the cookie should be submitted with a request 
     *  with given attributes, <tt>false</tt> otherwise.
     */
    boolean match(String host, int port, String path, boolean secure,
        final Cookie cookie);

    /**
     * Determines which of an array of Cookies matches a location.
     *
     * @param host the host to which the request is being submitted
     * @param port the port to which the request is being submitted 
     *  (currenlty ignored)
     * @param path the path to which the request is being submitted
     * @param secure <tt>true</tt> if the request is using a secure protocol
     * @param cookies an array of <tt>Cookie</tt>s to be matched
     *
     * @return <tt>true</tt> if the cookie should be submitted with a request 
     *  with given attributes, <tt>false</tt> otherwise.
     */
    Cookie[] match(String host, int port, String path, boolean secure, 
        final Cookie cookies[]);

    /**
     * Create a <tt>"Cookie"</tt> header value for an array of cookies.
     *
     * @param cookie the cookie to be formatted as string
     * @return a string suitable for sending in a <tt>"Cookie"</tt> header.
     */
    String formatCookie(Cookie cookie);

    /**
     * Create a <tt>"Cookie"</tt> header value for an array of cookies.
     *
     * @param cookies the Cookies to be formatted
     * @return a string suitable for sending in a Cookie header.
     * @throws IllegalArgumentException if an input parameter is illegal
     */
    String formatCookies(Cookie[] cookies) throws IllegalArgumentException;
    
    /**
     * Create a <tt>"Cookie"</tt> Header for an array of Cookies.
     *
     * @param cookies the Cookies format into a Cookie header
     * @return a Header for the given Cookies.
     * @throws IllegalArgumentException if an input parameter is illegal
     */
    Header formatCookieHeader(Cookie[] cookies) throws IllegalArgumentException;

    /**
     * Create a <tt>"Cookie"</tt> Header for single Cookie.
     *
     * @param cookie the Cookie format as a <tt>Cookie</tt> header
     * @return a Cookie header.
     * @throws IllegalArgumentException if an input parameter is illegal
     */
    Header formatCookieHeader(Cookie cookie) throws IllegalArgumentException;

}
