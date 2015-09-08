/*
 * $Header: /home/cvs/jakarta-commons/httpclient/src/java/org/apache/commons/httpclient/cookie/NetscapeDraftSpec.java,v 1.7.2.1 2004/02/22 18:21:15 olegk Exp $
 * $Revision: 1.7.2.1 $
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

import java.util.StringTokenizer;
import java.util.Date;
import java.util.Locale;   
import java.text.DateFormat; 
import java.text.SimpleDateFormat;  
import java.text.ParseException; 
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.Cookie;

/**
 * <P>Netscape cookie draft specific cookie management functions
 *
 * @author  B.C. Holmes
 * @author <a href="mailto:jericho@thinkfree.com">Park, Sung-Gu</a>
 * @author <a href="mailto:dsale@us.britannica.com">Doug Sale</a>
 * @author Rod Waldhoff
 * @author dIon Gillard
 * @author Sean C. Sullivan
 * @author <a href="mailto:JEvans@Cyveillance.com">John Evans</a>
 * @author Marc A. Saegesser
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * 
 * @since 2.0 
 */

public class NetscapeDraftSpec extends CookieSpecBase {

    /** Default constructor */
    public NetscapeDraftSpec() {
        super();
    }


    /**
      * Parse the cookie attribute and update the corresponsing {@link Cookie}
      * properties as defined by the Netscape draft specification
      *
      * @param attribute {@link NameValuePair} cookie attribute from the
      * <tt>Set- Cookie</tt>
      * @param cookie {@link Cookie} to be updated
      * @throws MalformedCookieException if an exception occurs during parsing
      */
    public void parseAttribute(
        final NameValuePair attribute, final Cookie cookie)
        throws MalformedCookieException {
            
        if (attribute == null) {
            throw new IllegalArgumentException("Attribute may not be null.");
        }
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie may not be null.");
        }
        final String paramName = attribute.getName().toLowerCase();
        final String paramValue = attribute.getValue();

        if (paramName.equals("expires")) {

            if (paramValue == null) {
                throw new MalformedCookieException(
                    "Missing value for expires attribute");
            }
            try {
                DateFormat expiryFormat = new SimpleDateFormat(
                    "EEE, dd-MMM-yyyy HH:mm:ss z", Locale.US);
                Date date = expiryFormat.parse(paramValue);
                cookie.setExpiryDate(date);
            } catch (ParseException e) {
                throw new MalformedCookieException("Invalid expires "
                    + "attribute: " + e.getMessage());
            }
        } else {
            super.parseAttribute(attribute, cookie);
        }
    }

    /**
      * Performs Netscape draft compliant {@link Cookie} validation
      *
      * @param host the host from which the {@link Cookie} was received
      * @param port the port from which the {@link Cookie} was received
      * @param path the path from which the {@link Cookie} was received
      * @param secure <tt>true</tt> when the {@link Cookie} was received 
      * using a secure connection
      * @param cookie The cookie to validate.
      * @throws MalformedCookieException if an exception occurs during
      * validation
      */
    public void validate(String host, int port, String path, 
        boolean secure, final Cookie cookie) 
        throws MalformedCookieException {
            
        LOG.trace("enterNetscapeDraftCookieProcessor "
            + "RCF2109CookieProcessor.validate(Cookie)");
        // Perform generic validation
        super.validate(host, port, path, secure, cookie);
        // Perform Netscape Cookie draft specific validation
        if (host.indexOf(".") >= 0) {
            int domainParts = new StringTokenizer(cookie.getDomain(), ".")
                .countTokens();

            if (isSpecialDomain(cookie.getDomain())) {
                if (domainParts < 2) {
                    throw new MalformedCookieException("Domain attribute \""
                        + cookie.getDomain() 
                        + "\" violates the Netscape cookie specification for "
                        + "special domains");
                }
            } else {
                if (domainParts < 3) {
                    throw new MalformedCookieException("Domain attribute \""
                        + cookie.getDomain() 
                        + "\" violates the Netscape cookie specification");
                }            
            }
        }
    }
    
    /**
     * Checks if the given domain is in one of the seven special
     * top level domains defined by the Netscape cookie specification.
     * @param domain The domain.
     * @return True if the specified domain is "special"
     */
    private static boolean isSpecialDomain(final String domain) {
        final String ucDomain = domain.toUpperCase();
        if (ucDomain.endsWith(".COM") 
           || ucDomain.endsWith(".EDU")
           || ucDomain.endsWith(".NET")
           || ucDomain.endsWith(".GOV")
           || ucDomain.endsWith(".MIL")
           || ucDomain.endsWith(".ORG")
           || ucDomain.endsWith(".INT")) {
            return true;
        }
        return false;
    }
}
