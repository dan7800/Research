/*
 * $Header: /home/cvs/jakarta-commons/httpclient/src/java/org/apache/commons/httpclient/auth/HttpAuthRealm.java,v 1.3.2.2 2004/02/22 18:21:15 olegk Exp $
 * $Revision: 1.3.2.2 $
 * $Date: 2004/02/22 18:21:15 $
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

package org.apache.commons.httpclient.auth;

/** The key used to look up authentication credentials.
 * 
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * @author <a href="mailto:adrian@intencha.com">Adrian Sutton</a>
 */
public class HttpAuthRealm {
    
    /** The realm the credentials apply to. */
    private String realm = null;
    
    /** The domain the credentials apply to. */
    private String domain = null;
        
    /** Creates a new HttpAuthRealm for the given <tt>domain</tt> and 
     * <tt>realm</tt>.
     * 
     * @param domain the domain the credentials apply to. May be set
     *   to <tt>null</tt> if credenticals are applicable to
     *   any domain. 
     * @param realm the realm the credentials apply to. May be set 
     *   to <tt>null</tt> if credenticals are applicable to
     *   any realm. 
     *   
     */
    public HttpAuthRealm(final String domain, final String realm) {
        this.domain = domain;
        this.realm = realm;
    }
    
    /** Determines if the given domains match.  Note that <tt>null</tt> acts as a
     * wildcard so if either of the domains are <tt>null</tt>, it is considered a match.
     * 
     * @param d1 the domain
     * @param d2 the other domain
     * @return boolean true if the domains match, otherwise false.
     */
    private static boolean domainAttribMatch(final String d1, final String d2) {
        return d1 == null || d2 == null || d1.equalsIgnoreCase(d2);
    }

    /** Determines if the given realms match.  Note that <tt>null</tt> acts as a
     * wildcard so if either realm is <tt>null</tt>, this function will return <tt>true</tt>.
     * 
     * @param r1 the realm
     * @param r2 the other realm
     * @return boolean true if the realms match, otherwise false.
     */ 
    private static boolean realmAttribMatch(final String r1, final String r2) {
        return r1 == null || r2 == null || r1.equals(r2);
    }


    /**
     * @see java.lang.Object#equals(Object)
     */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (!(o instanceof HttpAuthRealm)) {
            return super.equals(o);
        }
        HttpAuthRealm that = (HttpAuthRealm) o;
        return 
          domainAttribMatch(this.domain, that.domain) 
          && realmAttribMatch(this.realm, that.realm);
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Authentication domain: '");
        buffer.append(this.domain);
        buffer.append("', authentication realm: '");
        buffer.append(this.realm);
        buffer.append("'");
        return buffer.toString();
    }
    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(this.domain);
        buffer.append(this.realm);
        return buffer.toString().hashCode();
    }

}
