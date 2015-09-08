/*
 * $Header: /home/cvs/jakarta-commons/httpclient/src/java/org/apache/commons/httpclient/cookie/CookiePolicy.java,v 1.7.2.2 2004/02/22 18:21:15 olegk Exp $
 * $Revision: 1.7.2.2 $
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Cookie management policy class. The cookie policy provides corresponding
 * cookie management interfrace for a given type or version of cookie. 
 * <p>RFC 2109 specification is used per default. Other supported specification
 * can be  chosen when appropriate or set default when desired
 * <p>The following specifications are provided:
 *  <ul>
 *   <li><tt>COMPATIBILITY</tt>: compatible with the common cookie management
 *   practices *  (even if they are not 100% standards compliant)
 *   <li><tt>NETSCAPE_DRAFT</tt>: Netscape cookie draft compliant
 *   <li><tt>RFC2109</tt>: RFC2109 compliant (default)
 *  </ul>
 * <p>Default policy can be set on JVM start-up through the system property 
 *  <tt>"apache.commons.httpclient.cookiespec"</tt>. Recognized values: 
 *  <tt>COMPATIBILITY</tt>, <tt>NETSCAPE_DRAFT</tt>, <tt>RFC2109</tt>.
 * 
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 *
 * @since 2.0
 */
public abstract class CookiePolicy {

    /** cookiespec system property. */
    private static final String SYSTEM_PROPERTY =
        "apache.commons.httpclient.cookiespec";

    /**
     * The <tt>COMPATIBILITY</tt> policy provides high compatibilty 
     * with common cookie management of popular HTTP agents.
     */
    public static final int COMPATIBILITY = 0;

    /** The <tt>NETSCAPE_DRAFT</tt> Netscape draft compliant policy. */
    public static final int NETSCAPE_DRAFT = 1;

    /** The <tt>RFC2109</tt> RFC 2109 compliant policy. */
    public static final int RFC2109 = 2;

    /** The default cookie policy. */
    private static int defaultPolicy = RFC2109;

    /** Log object. */
    protected static final Log LOG = LogFactory.getLog(CookiePolicy.class);

    static {
        String s = null;
        try {
            s = System.getProperty(SYSTEM_PROPERTY);
        } catch (SecurityException e) {
        }
        if ("COMPATIBILITY".equalsIgnoreCase(s)) {
            setDefaultPolicy(COMPATIBILITY);
        } else if ("NETSCAPE_DRAFT".equalsIgnoreCase(s)) {
            setDefaultPolicy(NETSCAPE_DRAFT);
        } else if ("RFC2109".equalsIgnoreCase(s)) {
            setDefaultPolicy(RFC2109);
        } else {
            if (s != null) {
                LOG.warn("Unrecognized cookiespec property '" + s
                    + "' - using default");
            }
            setDefaultPolicy(defaultPolicy);
        }
    }

    /**
     * @return default cookie policy
     *  <tt>(COMPATIBILITY | NETSCAPE_DRAFT | RFC2109)</tt>
     */
    public static int getDefaultPolicy() {
        return defaultPolicy;
    }
    

    /**
     * @param policy new default cookie policy
     *  <tt>(COMPATIBILITY | NETSCAPE_DRAFT | RFC2109)</tt>
     */
    public static void setDefaultPolicy(int policy) {
        defaultPolicy = policy;
    }
    

    /**
     * @param policy cookie policy to get the CookieSpec for
     * @return cookie specification interface for the given policy
     *  <tt>(COMPATIBILITY | NETSCAPE_DRAFT | RFC2109)</tt>
     */
    public static CookieSpec getSpecByPolicy(int policy) {
        switch(policy) {
            case COMPATIBILITY: 
                return new CookieSpecBase(); 
            case NETSCAPE_DRAFT: 
                return new NetscapeDraftSpec(); 
            case RFC2109:
                return new RFC2109Spec();
            default:
                return getSpecByPolicy(defaultPolicy); 
        }
    }


    /**
     * @return default cookie specification interface
     */
    public static CookieSpec getDefaultSpec() {
        return getSpecByPolicy(defaultPolicy);
    }
    

    /**
     * Gets the CookieSpec for a particular cookie version.
     * 
     * <p>Supported versions:
     * <ul>
     *  <li><tt>version 0</tt> corresponds to the NETSCAPE_DRAFT
     *  <li><tt>version 1</tt> corresponds to the RFC2109
     *  <li>Any other cookie value coresponds to the default spec
     * <ul>
     *
     * @param ver the cookie version to get the spec for
     * @return cookie specification interface intended for processing 
     *  cookies with the given version 
     */
    public static CookieSpec getSpecByVersion(int ver) {
        switch(ver) {
            case 0: 
                return new NetscapeDraftSpec(); 
            case 1:
                return new RFC2109Spec();
            default:
                return getDefaultSpec(); 
        }
    }

    /**
     * @return cookie specification interface that provides high compatibilty 
     * with common cookie management of popular HTTP agents
     */
    public static CookieSpec getCompatibilitySpec() {
        return getSpecByPolicy(COMPATIBILITY);
    }
}
