/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//

package javax.servlet.http;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Creates a cookie, a small amount of information sent by a servlet to
 * a Web browser, saved by the browser, and later sent back to the server.
 * A cookie's value can uniquely
 * identify a client, so cookies are commonly used for session management.
 *
 * <p>A cookie has a name, a single value, and optional attributes
 * such as a comment, path and domain qualifiers, a maximum age, and a
 * version number. Some Web browsers have bugs in how they handle the
 * optional attributes, so use them sparingly to improve the interoperability
 * of your servlets.
 *
 * <p>The servlet sends cookies to the browser by using the
 * {@link HttpServletResponse#addCookie} method, which adds
 * fields to HTTP response headers to send cookies to the
 * browser, one at a time. The browser is expected to
 * support 20 cookies for each Web server, 300 cookies total, and
 * may limit cookie size to 4 KB each.
 *
 * <p>The browser returns cookies to the servlet by adding
 * fields to HTTP request headers. Cookies can be retrieved
 * from a request by using the {@link HttpServletRequest#getCookies} method.
 * Several cookies might have the same name but different path attributes.
 *
 * <p>Cookies affect the caching of the Web pages that use them.
 * HTTP 1.0 does not cache pages that use cookies created with
 * this class. This class does not support the cache control
 * defined with HTTP 1.1.
 *
 * <p>This class supports both the Version 0 (by Netscape) and Version 1
 * (by RFC 2109) cookie specifications. By default, cookies are
 * created using Version 0 to ensure the best interoperability.
 *
 * @version $Revision: 1.8 $ $Date: 2004/09/23 08:05:29 $
 */

// XXX would implement java.io.Serializable too, but can't do that
// so long as sun.servlet.* must run on older JDK 1.02 JVMs which
// don't include that support.

public class Cookie implements Cloneable {
    private static final String LSTRING_FILE = "javax.servlet.http.LocalStrings";
    private static ResourceBundle lStrings = ResourceBundle.getBundle(LSTRING_FILE);

    //
    // The value of the cookie itself.
    //

    private String name; // NAME= ... "$Name" style is reserved
    private String value; // value of NAME

    //
    // Attributes encoded in the header's cookie fields.
    //

    private String comment; // ;Comment=VALUE ... describes cookie's use
    // ;Discard ... implied by maxAge < 0
    private String domain; // ;Domain=VALUE ... domain that sees cookie
    private int maxAge = -1; // ;Max-Age=VALUE ... cookies auto-expire
    private String path; // ;Path=VALUE ... URLs that see the cookie
    private boolean secure; // ;Secure ... e.g. use SSL
    private int version = 0; // ;Version=1 ... means RFC 2109++ style


    /**
     * Constructs a cookie with a specified name and value.
     *
     * <p>The name must conform to RFC 2109. That means it can contain
     * only ASCII alphanumeric characters and cannot contain commas,
     * semicolons, or white space or begin with a $ character. The cookie's
     * name cannot be changed after creation.
     *
     * <p>The value can be anything the server chooses to send. Its
     * value is probably of interest only to the server. The cookie's
     * value can be changed after creation with the
     * <code>setValue</code> method.
     *
     * <p>By default, cookies are created according to the Netscape
     * cookie specification. The version can be changed with the
     * <code>setVersion</code> method.
     *
     *
     * @param name a <code>String</code> specifying the name of the cookie
     *
     * @param value a <code>String</code> specifying the value of the cookie
     *
     * @throws IllegalArgumentException if the cookie name contains illegal characters
     * (for example, a comma, space, or semicolon) or it is one of the tokens reserved for use
     * by the cookie protocol
     *
     * @see #setValue
     * @see #setVersion
     */
    public Cookie(String name, String value) {
        if (!isToken(name)
                || name.equalsIgnoreCase("Comment") // rfc2019
                || name.equalsIgnoreCase("Discard") // 2019++
                || name.equalsIgnoreCase("Domain")
                || name.equalsIgnoreCase("Expires") // (old cookies)
                || name.equalsIgnoreCase("Max-Age") // rfc2019
                || name.equalsIgnoreCase("Path")
                || name.equalsIgnoreCase("Secure")
                || name.equalsIgnoreCase("Version")
                || name.startsWith("$")
        ) {
            String errMsg = lStrings.getString("err.cookie_name_is_token");
            Object[] errArgs = new Object[1];
            errArgs[0] = name;
            errMsg = MessageFormat.format(errMsg, errArgs);
            throw new IllegalArgumentException(errMsg);
        }

        this.name = name;
        this.value = value;
    }

    /**
     * Specifies a comment that describes a cookie's purpose.
     * The comment is useful if the browser presents the cookie
     * to the user. Comments
     * are not supported by Netscape Version 0 cookies.
     *
     * @param purpose a <code>String</code> specifying the comment
     * to display to the user
     *
     * @see #getComment
     */
    public void setComment(String purpose) {
        comment = purpose;
    }

    /**
     * Returns the comment describing the purpose of this cookie, or
     * <code>null</code> if the cookie has no comment.
     *
     * @return a <code>String</code> containing the comment,
     * or <code>null</code> if none
     *
     * @see #setComment
     */
    public String getComment() {
        return comment;
    }

    /**
     * Specifies the domain within which this cookie should be presented.
     *
     * <p>The form of the domain name is specified by RFC 2109. A domain
     * name begins with a dot (<code>.foo.com</code>) and means that
     * the cookie is visible to servers in a specified Domain Name System
     * (DNS) zone (for example, <code>www.foo.com</code>, but not
     * <code>a.b.foo.com</code>). By default, cookies are only returned
     * to the server that sent them.
     *
     * @param pattern a <code>String</code> containing the domain name
     * within which this cookie is visible; form is according to RFC 2109
     *
     * @see #getDomain
     */
    public void setDomain(String pattern) {
        domain = pattern.toLowerCase(); // IE allegedly needs this
    }

    /**
     * Returns the domain name set for this cookie. The form of
     * the domain name is set by RFC 2109.
     *
     * @return a <code>String</code> containing the domain name
     *
     * @see #setDomain
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Sets the maximum age of the cookie in seconds.
     *
     * <p>A positive value indicates that the cookie will expire
     * after that many seconds have passed. Note that the value is
     * the <i>maximum</i> age when the cookie will expire, not the cookie's
     * current age.
     *
     * <p>A negative value means
     * that the cookie is not stored persistently and will be deleted
     * when the Web browser exits. A zero value causes the cookie
     * to be deleted.
     *
     * @param expiry an integer specifying the maximum age of the
     * cookie in seconds; if negative, means the cookie is not stored;
     * if zero, deletes the cookie
     *
     * @see #getMaxAge
     */
    public void setMaxAge(int expiry) {
        maxAge = expiry;
    }

    /**
     * Returns the maximum age of the cookie, specified in seconds,
     * By default, <code>-1</code> indicating the cookie will persist
     * until browser shutdown.
     *
     * @return an integer specifying the maximum age of the
     * cookie in seconds; if negative, means the cookie persists
     * until browser shutdown
     *
     * @see #setMaxAge
     */
    public int getMaxAge() {
        return maxAge;
    }

    /**
     * Specifies a path for the cookie
     * to which the client should return the cookie.
     *
     * <p>The cookie is visible to all the pages in the directory
     * you specify, and all the pages in that directory's subdirectories.
     * A cookie's path must include the servlet that set the cookie,
     * for example, <i>/catalog</i>, which makes the cookie
     * visible to all directories on the server under <i>/catalog</i>.
     *
     * <p>Consult RFC 2109 (available on the Internet) for more
     * information on setting path names for cookies.
     *
     * @param uri a <code>String</code> specifying a path
     *
     * @see #getPath
     */
    public void setPath(String uri) {
        path = uri;
    }

    /**
     * Returns the path on the server
     * to which the browser returns this cookie. The
     * cookie is visible to all subpaths on the server.
     *
     * @return a <code>String</code> specifying a path that contains
     * a servlet name, for example, <i>/catalog</i>
     *
     * @see #setPath
     */
    public String getPath() {
        return path;
    }

    /**
     * Indicates to the browser whether the cookie should only be sent
     * using a secure protocol, such as HTTPS or SSL.
     *
     * <p>The default value is <code>false</code>.
     *
     * @param flag if <code>true</code>, sends the cookie from the browser
     * to the server only when using a secure protocol; if <code>false</code>,
     * sent on any protocol
     *
     * @see #getSecure
     */
    public void setSecure(boolean flag) {
        secure = flag;
    }

    /**
     * Returns <code>true</code> if the browser is sending cookies
     * only over a secure protocol, or <code>false</code> if the
     * browser can send cookies using any protocol.
     *
     * @return <code>true</code> if the browser uses a secure protocol;
     * otherwise, <code>true</code>
     *
     * @see #setSecure
     */
    public boolean getSecure() {
        return secure;
    }

    /**
     * Returns the name of the cookie. The name cannot be changed after
     * creation.
     *
     * @return a <code>String</code> specifying the cookie's name
     */
    public String getName() {
        return name;
    }

    /**
     * Assigns a new value to a cookie after the cookie is created.
     * If you use a binary value, you may want to use BASE64 encoding.
     *
     * <p>With Version 0 cookies, values should not contain white
     * space, brackets, parentheses, equals signs, commas,
     * double quotes, slashes, question marks, at signs, colons,
     * and semicolons. Empty values may not behave the same way
     * on all browsers.
     *
     * @param newValue a <code>String</code> specifying the new value
     *
     * @see #getValue
     * @see Cookie
     */
    public void setValue(String newValue) {
        value = newValue;
    }

    /**
     * Returns the value of the cookie.
     *
     * @return a <code>String</code> containing the cookie's
     * present value
     *
     * @see #setValue
     * @see Cookie
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the version of the protocol this cookie complies
     * with. Version 1 complies with RFC 2109,
     * and version 0 complies with the original
     * cookie specification drafted by Netscape. Cookies provided
     * by a browser use and identify the browser's cookie version.
     *
     * @return 0 if the cookie complies with the original Netscape
     * specification; 1 if the cookie complies with RFC 2109
     *
     * @see #setVersion
     */
    public int getVersion() {
        return version;
    }

    /**
     * Sets the version of the cookie protocol this cookie complies
     * with. Version 0 complies with the original Netscape cookie
     * specification. Version 1 complies with RFC 2109.
     *
     * <p>Since RFC 2109 is still somewhat new, consider
     * version 1 as experimental; do not use it yet on production sites.
     *
     * @param v 0 if the cookie should comply with the original Netscape
     * specification; 1 if the cookie should comply with RFC 2109
     *
     * @see #getVersion
     */
    public void setVersion(int v) {
        version = v;
    }

    // Note -- disabled for now to allow full Netscape compatibility
    // from RFC 2068, token special case characters
    //
    // private static final String tspecials = "()<>@,;:\\\"/[]?={} \t";

    private static final String tspecials = ",; ";

    /**
     * Tests a string and returns true if the string counts as a
     * reserved token in the Java language.
     *
     * @param value the <code>String</code> to be tested
     *
     * @return <code>true</code> if the <code>String</code> is
     * a reserved token; <code>false</code> if it is not
     */
    private boolean isToken(String value) {
        int len = value.length();

        for (int i = 0; i < len; i++) {
            char c = value.charAt(i);

            if (c < 0x20 || c >= 0x7f || tspecials.indexOf(c) != -1)
                return false;
        }
        return true;
    }

    /**
     * Overrides the standard <code>java.lang.Object.clone</code>
     * method to return a copy of this cookie.
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}

package org.omg.PortableServer.ServantLocatorPackage;

public interface Cookie {

}
/*
 * $Header: /home/cvs/jakarta-commons/httpclient/src/java/org/apache/commons/httpclient/Cookie.java,v 1.38.2.4 2004/06/05 16:32:01 olegk Exp $
 * $Revision: 1.38.2.4 $
 * $Date: 2004/06/05 16:32:01 $
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

import java.io.Serializable;
import java.text.RuleBasedCollator;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.cookie.CookieSpec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <p>
 * HTTP "magic-cookie" represents a piece of state information
 * that the HTTP agent and the target server can exchange to maintain 
 * a session.
 * </p>
 * 
 * @author B.C. Holmes
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
 * @version $Revision: 1.38.2.4 $ $Date: 2004/06/05 16:32:01 $
 */

public class Cookie extends NameValuePair implements Serializable, Comparator {

    // ----------------------------------------------------------- Constructors

    /**
     * Default constructor. Creates a blank cookie 
     */

    public Cookie() {
        this(null, "noname", null, null, null, false);
    }

    /**
     * Creates a cookie with the given name, value and domain attribute.
     *
     * @param name    the cookie name
     * @param value   the cookie value
     * @param domain  the domain this cookie can be sent to
     */
    public Cookie(String domain, String name, String value) {
        this(domain, name, value, null, null, false);
    }

    /**
     * Creates a cookie with the given name, value, domain attribute,
     * path attribute, expiration attribute, and secure attribute 
     *
     * @param name    the cookie name
     * @param value   the cookie value
     * @param domain  the domain this cookie can be sent to
     * @param path    the path prefix for which this cookie can be sent
     * @param expires the {@link Date} at which this cookie expires,
     *                or <tt>null</tt> if the cookie expires at the end
     *                of the session
     * @param secure if true this cookie can only be sent over secure
     * connections
     * @throws IllegalArgumentException If cookie name is null or blank,
     *   cookie name contains a blank, or cookie name starts with character $
     *   
     */
    public Cookie(String domain, String name, String value, 
        String path, Date expires, boolean secure) {
            
        super(name, value);
        LOG.trace("enter Cookie(String, String, String, String, Date, boolean)");
        if (name == null) {
            throw new IllegalArgumentException("Cookie name may not be null");
        }
        if (name.trim().equals("")) {
            throw new IllegalArgumentException("Cookie name may not be blank");
        }
        this.setPath(path);
        this.setDomain(domain);
        this.setExpiryDate(expires);
        this.setSecure(secure);
    }

    /**
     * Creates a cookie with the given name, value, domain attribute,
     * path attribute, maximum age attribute, and secure attribute 
     *
     * @param name   the cookie name
     * @param value  the cookie value
     * @param domain the domain this cookie can be sent to
     * @param path   the path prefix for which this cookie can be sent
     * @param maxAge the number of seconds for which this cookie is valid.
     *               maxAge is expected to be a non-negative number. 
     *               <tt>-1</tt> signifies that the cookie should never expire.
     * @param secure if <tt>true</tt> this cookie can only be sent over secure
     * connections
     */
    public Cookie(String domain, String name, String value, String path, 
        int maxAge, boolean secure) {
            
        this(domain, name, value, path, null, secure);
        if (maxAge < -1) {
            throw new IllegalArgumentException("Invalid max age:  " + Integer.toString(maxAge));
        }            
        if (maxAge >= 0) {
            setExpiryDate(new Date(System.currentTimeMillis() + maxAge * 1000L));
        }
    }

    /**
     * Returns the comment describing the purpose of this cookie, or
     * <tt>null</tt> if no such comment has been defined.
     * 
     * @return comment 
     *
     * @see #setComment(String)
     */
    public String getComment() {
        return cookieComment;
    }

    /**
     * If a user agent (web browser) presents this cookie to a user, the
     * cookie's purpose will be described using this comment.
     * 
     * @param comment
     *  
     * @see #getComment()
     */
    public void setComment(String comment) {
        cookieComment = comment;
    }

    /**
     * Returns the expiration {@link Date} of the cookie, or <tt>null</tt>
     * if none exists.
     * <p><strong>Note:</strong> the object returned by this method is 
     * considered immutable. Changing it (e.g. using setTime()) could result
     * in undefined behaviour. Do so at your peril. </p>
     * @return Expiration {@link Date}, or <tt>null</tt>.
     *
     * @see #setExpiryDate(java.util.Date)
     *
     */
    public Date getExpiryDate() {
        return cookieExpiryDate;
    }

    /**
     * Sets expiration date.
     * <p><strong>Note:</strong> the object returned by this method is considered
     * immutable. Changing it (e.g. using setTime()) could result in undefined 
     * behaviour. Do so at your peril.</p>
     *
     * @param expiryDate the {@link Date} after which this cookie is no longer valid.
     *
     * @see #getExpiryDate
     *
     */
    public void setExpiryDate (Date expiryDate) {
        cookieExpiryDate = expiryDate;
    }


    /**
     * Returns <tt>false</tt> if the cookie should be discarded at the end
     * of the "session"; <tt>true</tt> otherwise.
     *
     * @return <tt>false</tt> if the cookie should be discarded at the end
     *         of the "session"; <tt>true</tt> otherwise
     */
    public boolean isPersistent() {
        return (null != cookieExpiryDate);
    }


    /**
     * Returns domain attribute of the cookie.
     * 
     * @return the value of the domain attribute
     *
     * @see #setDomain(java.lang.String)
     */
    public String getDomain() {
        return cookieDomain;
    }

    /**
     * Sets the domain attribute.
     * 
     * @param domain The value of the domain attribute
     *
     * @see #getDomain
     */
    public void setDomain(String domain) {
        if (domain != null) {
            int ndx = domain.indexOf(":");
            if (ndx != -1) {
              domain = domain.substring(0, ndx);
            }
            cookieDomain = domain.toLowerCase();
        }
    }


    /**
     * Returns the path attribute of the cookie
     * 
     * @return The value of the path attribute.
     * 
     * @see #setPath(java.lang.String)
     */
    public String getPath() {
        return cookiePath;
    }

    /**
     * Sets the path attribute.
     *
     * @param path The value of the path attribute
     *
     * @see #getPath
     *
     */
    public void setPath(String path) {
        cookiePath = path;
    }

    /**
     * @return <code>true</code> if this cookie should only be sent over secure connections.
     * @see #setSecure(boolean)
     */
    public boolean getSecure() {
        return isSecure;
    }

    /**
     * Sets the secure attribute of the cookie.
     * <p>
     * When <tt>true</tt> the cookie should only be sent
     * using a secure protocol (https).  This should only be set when
     * the cookie's originating server used a secure protocol to set the
     * cookie's value.
     *
     * @param secure The value of the secure attribute
     * 
     * @see #getSecure()
     */
    public void setSecure (boolean secure) {
        isSecure = secure;
    }

    /**
     * Returns the version of the cookie specification to which this
     * cookie conforms.
     *
     * @return the version of the cookie.
     * 
     * @see #setVersion(int)
     *
     */
    public int getVersion() {
        return cookieVersion;
    }

    /**
     * Sets the version of the cookie specification to which this
     * cookie conforms. 
     *
     * @param version the version of the cookie.
     * 
     * @see #getVersion
     */
    public void setVersion(int version) {
        cookieVersion = version;
    }

    /**
     * Returns true if this cookie has expired.
     * 
     * @return <tt>true</tt> if the cookie has expired.
     */
    public boolean isExpired() {
        return (cookieExpiryDate != null  
            && cookieExpiryDate.getTime() <= System.currentTimeMillis());
    }

    /**
     * Returns true if this cookie has expired according to the time passed in.
     * 
     * @param now The current time.
     * 
     * @return <tt>true</tt> if the cookie expired.
     */
    public boolean isExpired(Date now) {
        return (cookieExpiryDate != null  
            && cookieExpiryDate.getTime() <= now.getTime());
    }


    /**
     * Indicates whether the cookie had a path specified in a 
     * path attribute of the <tt>Set-Cookie</tt> header. This value
     * is important for generating the <tt>Cookie</tt> header because 
     * some cookie specifications require that the <tt>Cookie</tt> header 
     * should only include a path attribute if the cookie's path 
     * was specified in the <tt>Set-Cookie</tt> header.
     *
     * @param value <tt>true</tt> if the cookie's path was explicitly 
     * set, <tt>false</tt> otherwise.
     * 
     * @see #isPathAttributeSpecified
     */
    public void setPathAttributeSpecified(boolean value) {
        hasPathAttribute = value;
    }

    /**
     * Returns <tt>true</tt> if cookie's path was set via a path attribute
     * in the <tt>Set-Cookie</tt> header.
     *
     * @return value <tt>true</tt> if the cookie's path was explicitly 
     * set, <tt>false</tt> otherwise.
     * 
     * @see #setPathAttributeSpecified
     */
    public boolean isPathAttributeSpecified() {
        return hasPathAttribute;
    }

    /**
     * Indicates whether the cookie had a domain specified in a 
     * domain attribute of the <tt>Set-Cookie</tt> header. This value
     * is important for generating the <tt>Cookie</tt> header because 
     * some cookie specifications require that the <tt>Cookie</tt> header 
     * should only include a domain attribute if the cookie's domain 
     * was specified in the <tt>Set-Cookie</tt> header.
     *
     * @param value <tt>true</tt> if the cookie's domain was explicitly 
     * set, <tt>false</tt> otherwise.
     *
     * @see #isDomainAttributeSpecified
     */
    public void setDomainAttributeSpecified(boolean value) {
        hasDomainAttribute = value;
    }

    /**
     * Returns <tt>true</tt> if cookie's domain was set via a domain 
     * attribute in the <tt>Set-Cookie</tt> header.
     *
     * @return value <tt>true</tt> if the cookie's domain was explicitly 
     * set, <tt>false</tt> otherwise.
     *
     * @see #setDomainAttributeSpecified
     */
    public boolean isDomainAttributeSpecified() {
        return hasDomainAttribute;
    }

    /**
     * Returns a hash code in keeping with the
     * {@link Object#hashCode} general hashCode contract.
     * @return A hash code
     */
    public int hashCode() {
        return super.hashCode()
            ^ (null == cookiePath ? 0 : cookiePath.hashCode())
            ^ (null == cookieDomain ? 0 : cookieDomain.hashCode());
    }


    /**
     * Two cookies are equal if the name, path and domain match.
     * @param obj The object to compare against.
     * @return true if the two objects are equal.
     */
    public boolean equals(Object obj) {
        LOG.trace("enter Cookie.equals(Object)");
        
        if ((obj != null) && (obj instanceof Cookie)) {
            Cookie that = (Cookie) obj;
            return 
                (null == this.getName() 
                    ? null == that.getName() 
                    : this.getName().equals(that.getName())) 
                && (null == this.getPath() 
                    ? null == that.getPath() 
                    : this.getPath().equals(that.getPath())) 
                && (null == this.getDomain() 
                    ? null == that.getDomain() 
                    : this.getDomain().equals(that.getDomain()));
        } else {
            return false;
        }
    }


    /**
     * Returns a textual representation of the cookie.
     * 
     * @return string .
     */
    public String toExternalForm() {
        return CookiePolicy.getSpecByVersion(
            getVersion()).formatCookie(this);
    }

    /**
     * Return <tt>true</tt> if I should be submitted with a request with given
     * attributes, <tt>false</tt> otherwise.
     * @param domain the host to which the request is being submitted
     * @param port the port to which the request is being submitted (currently
     * ignored)
     * @param path the path to which the request is being submitted
     * @param secure <tt>true</tt> if the request is using the HTTPS protocol
     * @param date the time at which the request is submitted
     * @return true if the cookie matches
     * 
     * @deprecated use {@link CookieSpec} interface
     */
    public boolean matches(
        String domain, int port, String path, boolean secure, Date date) {
            
        LOG.trace("enter Cookie.matches(Strinng, int, String, boolean, Date");
        CookieSpec matcher = CookiePolicy.getDefaultSpec();
        return matcher.match(domain, port, path, secure, this);
    }

    /**
     * Return <tt>true</tt> if I should be submitted with a request with given
     * attributes, <tt>false</tt> otherwise.
     * @param domain the host to which the request is being submitted
     * @param port the port to which the request is being submitted (currently
     * ignored)
     * @param path the path to which the request is being submitted
     * @param secure True if this cookie has the secure flag set
     * @return true if I should be submitted as above.
     * @deprecated use {@link CookieSpec} interface
     */
    public boolean matches(
        String domain, int port, String path, boolean secure) {
        LOG.trace("enter Cookie.matches(String, int, String, boolean");
        return matches(domain, port, path, secure, new Date());
    }

    /**
     * Create a <tt>Cookie</tt> header containing
     * all non-expired cookies in <i>cookies</i>,
     * associated with the given <i>domain</i> and
     * <i>path</i>, assuming the connection is not
     * secure.
     * <p>
     * If no cookies match, returns null.
     * 
     * @param domain The domain
     * @param path The path
     * @param cookies The cookies to use
     * @return The new header.
     * @deprecated use {@link CookieSpec} interface
     */
    public static Header createCookieHeader(String domain, String path, 
        Cookie[] cookies) {
            
        LOG.trace("enter Cookie.createCookieHeader(String,String,Cookie[])");
        return Cookie.createCookieHeader(domain, path, false, cookies);
    }

    /**
     * Create a <tt>Cookie</tt> header containing
     * all non-expired cookies in <i>cookies</i>,
     * associated with the given <i>domain</i>, <i>path</i> and
     * <i>https</i> setting.
     * <p>
     * If no cookies match, returns null.
     * 
     * @param domain The domain
     * @param path The path
     * @param secure True if this cookie has the secure flag set
     * @param cookies The cookies to use.
     * @return The new header
     * @exception IllegalArgumentException if domain or path is null
     * 
     * @deprecated use {@link CookieSpec} interface
     */
    public static Header createCookieHeader(String domain, String path, 
        boolean secure, Cookie[] cookies)
        throws IllegalArgumentException {
            
        LOG.trace("enter Cookie.createCookieHeader("
            + "String, String, boolean, Cookie[])");

        // Make sure domain isn't null here.  Path will be validated in 
        // subsequent call to createCookieHeader
        if (domain == null) {
            throw new IllegalArgumentException("null domain in "
                + "createCookieHeader.");
        }
        // parse port from domain, if any
        int port = secure ? 443 : 80;
        int ndx = domain.indexOf(":");
        if (ndx != -1) {
            try {
                port = Integer.parseInt(domain.substring(ndx + 1, 
                    domain.length()));
            } catch (NumberFormatException e) {
                // ignore?, but at least LOG
                LOG.warn("Cookie.createCookieHeader():  "
                    + "Invalid port number in domain " + domain);
            }
        }
        return Cookie.createCookieHeader(domain, port, path, secure, cookies);
    }

    /**
     * Create a <tt>Cookie</tt> header containing
     * all non-expired cookies in <i>cookies</i>,
     * associated with the given <i>domain</i>, <i>port</i>,
     * <i>path</i> and <i>https</i> setting.
     * <p>
     * If no cookies match, returns null.
     * 
     * @param domain The domain
     * @param port The port
     * @param path The path
     * @param secure True if this cookie has the secure flag set
     * @param cookies The cookies to use.
     * @return The new header
     * @throws IllegalArgumentException if domain or path is null
     * 
     * @deprecated use {@link CookieSpec} interface
     */
    public static Header createCookieHeader(String domain, int port, 
        String path, boolean secure, Cookie[] cookies) 
        throws IllegalArgumentException {
        LOG.trace("enter Cookie.createCookieHeader(String, int, String, boolean, Cookie[])");
        return Cookie.createCookieHeader(domain, port, path, secure, new Date(), cookies);
    }

    /**
     * Create a <tt>Cookie</tt> header containing all cookies in <i>cookies</i>,
     * associated with the given <i>domain</i>, <i>port</i>, <i>path</i> and
     * <i>https</i> setting, and which are not expired according to the given
     * <i>date</i>.
     * <p>
     * If no cookies match, returns null.
     * 
     * @param domain The domain
     * @param port The port
     * @param path The path
     * @param secure True if this cookie has the secure flag set
     * @param now The date to check for expiry
     * @param cookies The cookies to use.
     * @return The new header
     * @throws IllegalArgumentException if domain or path is null
     * 
     * @deprecated use {@link CookieSpec} interface
     */

    public static Header createCookieHeader(
        String domain, int port, String path, boolean secure, 
        Date now, Cookie[] cookies) 
        throws IllegalArgumentException {
            
        LOG.trace("enter Cookie.createCookieHeader(String, int, String, boolean, Date, Cookie[])");
        CookieSpec matcher = CookiePolicy.getDefaultSpec();
        cookies = matcher.match(domain, port, path, secure, cookies);
        if ((cookies != null) && (cookies.length > 0)) {
            return matcher.formatCookieHeader(cookies);
        } else {
            return null;
        } 
    }

    /**
     * <p>Compares two cookies to determine order for cookie header.</p>
     * <p>Most specific should be first. </p>
     * <p>This method is implemented so a cookie can be used as a comparator for
     * a SortedSet of cookies. Specifically it's used above in the 
     * createCookieHeader method.</p>
     * @param o1 The first object to be compared
     * @param o2 The second object to be compared
     * @return See {@link java.util.Comparator#compare(Object,Object)}
     */
    public int compare(Object o1, Object o2) {
        LOG.trace("enter Cookie.compare(Object, Object)");

        if (!(o1 instanceof Cookie)) {
            throw new ClassCastException(o1.getClass().getName());
        }
        if (!(o2 instanceof Cookie)) {
            throw new ClassCastException(o2.getClass().getName());
        }
        Cookie c1 = (Cookie) o1;
        Cookie c2 = (Cookie) o2;
        if (c1.getPath() == null && c2.getPath() == null) {
            return 0;
        } else if (c1.getPath() == null) {
            // null is assumed to be "/"
            if (c2.getPath().equals(CookieSpec.PATH_DELIM)) {
                return 0;
            } else {
                return -1;
            }
        } else if (c2.getPath() == null) {
            // null is assumed to be "/"
            if (c1.getPath().equals(CookieSpec.PATH_DELIM)) {
                return 0;
            } else {
                return 1;
            }
        } else {
            return STRING_COLLATOR.compare(c1.getPath(), c2.getPath());
        }
    }

    /**
     * Return a textual representation of the cookie.
     * @see #toExternalForm
     */
    public String toString() {
        return toExternalForm();
    }

    /**
     * Parses the Set-Cookie {@link Header} into an array of
     * <tt>Cookie</tt>s, assuming that the cookies were recieved
     * on an insecure channel.
     *
     * @param domain the domain from which the {@link Header} was received
     * @param port the port from which the {@link Header} was received
     * (currently ignored)
     * @param path the path from which the {@link Header} was received
     * @param setCookie the <tt>Set-Cookie</tt> {@link Header} received from the
     * server
     * @return an array of <tt>Cookie</tt>s parsed from the Set-Cookie {@link
     * Header}
     * @throws HttpException if an exception occurs during parsing
     * @throws IllegalArgumentException if domain or path are null
     * 
     * @deprecated use {@link CookieSpec} interface
     */
    public static Cookie[] parse(
        String domain, int port, String path, Header setCookie) 
        throws HttpException, IllegalArgumentException {
            
        LOG.trace("enter Cookie.parse(String, int, String, Header)");
        return Cookie.parse(domain, port, path, false, setCookie);
    }

    /**
     * Parses the Set-Cookie {@link Header} into an array of
     * <tt>Cookie</tt>s, assuming that the cookies were recieved
     * on an insecure channel.
     *
     * @param domain the domain from which the {@link Header} was received
     * @param path the path from which the {@link Header} was received
     * @param setCookie the <tt>Set-Cookie</tt> {@link Header} received from the
     * server
     * @return an array of <tt>Cookie</tt>s parsed from the Set-Cookie {@link
     * Header}
     * @throws HttpException if an exception occurs during parsing
     * @throws IllegalArgumentException if domain or path are null
     * 
     * @deprecated use {@link CookieSpec} interface
     */
    public static Cookie[] parse(String domain, String path, Header setCookie) 
    throws HttpException, IllegalArgumentException {
        LOG.trace("enter Cookie.parse(String, String, Header)");
        return Cookie.parse (domain, 80, path, false, setCookie);
    }

    /**
     * Parses the Set-Cookie {@link Header} into an array of
     * <tt>Cookie</tt>s.
     *
     * @param domain the domain from which the {@link Header} was received
     * @param path the path from which the {@link Header} was received
     * @param secure <tt>true</tt> when the header was recieved over a secure
     * channel
     * @param setCookie the <tt>Set-Cookie</tt> {@link Header} received from the
     * server
     * @return an array of <tt>Cookie</tt>s parsed from the Set-Cookie {@link
     * Header}
     * @throws HttpException if an exception occurs during parsing
     * @throws IllegalArgumentException if domain or path are null
     * 
     * @deprecated use {@link CookieSpec} interface
     */
    public static Cookie[] parse(String domain, String path, 
        boolean secure, Header setCookie) 
        throws HttpException, IllegalArgumentException {
            
        LOG.trace ("enter Cookie.parse(String, String, boolean, Header)");
        return Cookie.parse (
            domain, (secure ? 443 : 80), path, secure, setCookie);
    }

    /**
      * Parses the Set-Cookie {@link Header} into an array of
      * <tt>Cookie</tt>s.
      *
      * <P>The syntax for the Set-Cookie response header is:
      *
      * <PRE>
      * set-cookie      =    "Set-Cookie:" cookies
      * cookies         =    1#cookie
      * cookie          =    NAME "=" VALUE * (";" cookie-av)
      * NAME            =    attr
      * VALUE           =    value
      * cookie-av       =    "Comment" "=" value
      *                 |    "Domain" "=" value
      *                 |    "Max-Age" "=" value
      *                 |    "Path" "=" value
      *                 |    "Secure"
      *                 |    "Version" "=" 1*DIGIT
      * </PRE>
      *
      * @param domain the domain from which the {@link Header} was received
      * @param port The port from which the {@link Header} was received.
      * @param path the path from which the {@link Header} was received
      * @param secure <tt>true</tt> when the {@link Header} was received over
      * HTTPS
      * @param setCookie the <tt>Set-Cookie</tt> {@link Header} received from
      * the server
      * @return an array of <tt>Cookie</tt>s parsed from the Set-Cookie {@link
      * Header}
      * @throws HttpException if an exception occurs during parsing
      * 
      * @deprecated use {@link CookieSpec} interface
      */
    public static Cookie[] parse(String domain, int port, String path, 
        boolean secure, Header setCookie) 
        throws HttpException {
            
        LOG.trace("enter Cookie.parse(String, int, String, boolean, Header)");

        CookieSpec parser = CookiePolicy.getDefaultSpec();
        Cookie[] cookies = parser.parse(domain, port, path, secure, setCookie);

        for (int i = 0; i < cookies.length; i++) {
            final Cookie cookie = cookies[i];
            final CookieSpec validator 
                = CookiePolicy.getSpecByVersion(cookie.getVersion());
            validator.validate(domain, port, path, secure, cookie);
        }
        return cookies;
    }

   // ----------------------------------------------------- Instance Variables

   /** Comment attribute. */
   private String  cookieComment;

   /** Domain attribute. */
   private String  cookieDomain;

   /** Expiration {@link Date}. */
   private Date    cookieExpiryDate;

   /** Path attribute. */
   private String  cookiePath;

   /** My secure flag. */
   private boolean isSecure;

   /**
    * Specifies if the set-cookie header included a Path attribute for this
    * cookie
    */
   private boolean hasPathAttribute = false;

   /**
    * Specifies if the set-cookie header included a Domain attribute for this
    * cookie
    */
   private boolean hasDomainAttribute = false;

   /** The version of the cookie specification I was created from. */
   private int     cookieVersion = 0;

   // -------------------------------------------------------------- Constants

   /** 
    * Collator for Cookie comparisons.  Could be replaced with references to
    * specific Locales.
    */
   private static final RuleBasedCollator STRING_COLLATOR =
        (RuleBasedCollator) RuleBasedCollator.getInstance(
                                                new Locale("en", "US", ""));

   /** Log object for this class */
   private static final Log LOG = LogFactory.getLog(Cookie.class);

}

/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//

package javax.servlet.http;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Creates a cookie, a small amount of information sent by a servlet to
 * a Web browser, saved by the browser, and later sent back to the server.
 * A cookie's value can uniquely
 * identify a client, so cookies are commonly used for session management.
 *
 * <p>A cookie has a name, a single value, and optional attributes
 * such as a comment, path and domain qualifiers, a maximum age, and a
 * version number. Some Web browsers have bugs in how they handle the
 * optional attributes, so use them sparingly to improve the interoperability
 * of your servlets.
 *
 * <p>The servlet sends cookies to the browser by using the
 * {@link HttpServletResponse#addCookie} method, which adds
 * fields to HTTP response headers to send cookies to the
 * browser, one at a time. The browser is expected to
 * support 20 cookies for each Web server, 300 cookies total, and
 * may limit cookie size to 4 KB each.
 *
 * <p>The browser returns cookies to the servlet by adding
 * fields to HTTP request headers. Cookies can be retrieved
 * from a request by using the {@link HttpServletRequest#getCookies} method.
 * Several cookies might have the same name but different path attributes.
 *
 * <p>Cookies affect the caching of the Web pages that use them.
 * HTTP 1.0 does not cache pages that use cookies created with
 * this class. This class does not support the cache control
 * defined with HTTP 1.1.
 *
 * <p>This class supports both the Version 0 (by Netscape) and Version 1
 * (by RFC 2109) cookie specifications. By default, cookies are
 * created using Version 0 to ensure the best interoperability.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 04:56:06 -0500 (Tue, 14 Sep 2004) $
 */

// XXX would implement java.io.Serializable too, but can't do that
// so long as sun.servlet.* must run on older JDK 1.02 JVMs which
// don't include that support.

public class Cookie implements Cloneable {
    private static final String LSTRING_FILE = "javax.servlet.http.LocalStrings";
    private static ResourceBundle lStrings = ResourceBundle.getBundle(LSTRING_FILE);

    //
    // The value of the cookie itself.
    //

    private String name; // NAME= ... "$Name" style is reserved
    private String value; // value of NAME

    //
    // Attributes encoded in the header's cookie fields.
    //

    private String comment; // ;Comment=VALUE ... describes cookie's use
    // ;Discard ... implied by maxAge < 0
    private String domain; // ;Domain=VALUE ... domain that sees cookie
    private int maxAge = -1; // ;Max-Age=VALUE ... cookies auto-expire
    private String path; // ;Path=VALUE ... URLs that see the cookie
    private boolean secure; // ;Secure ... e.g. use SSL
    private int version = 0; // ;Version=1 ... means RFC 2109++ style


    /**
     * Constructs a cookie with a specified name and value.
     *
     * <p>The name must conform to RFC 2109. That means it can contain
     * only ASCII alphanumeric characters and cannot contain commas,
     * semicolons, or white space or begin with a $ character. The cookie's
     * name cannot be changed after creation.
     *
     * <p>The value can be anything the server chooses to send. Its
     * value is probably of interest only to the server. The cookie's
     * value can be changed after creation with the
     * <code>setValue</code> method.
     *
     * <p>By default, cookies are created according to the Netscape
     * cookie specification. The version can be changed with the
     * <code>setVersion</code> method.
     *
     *
     * @param name a <code>String</code> specifying the name of the cookie
     *
     * @param value a <code>String</code> specifying the value of the cookie
     *
     * @throws IllegalArgumentException if the cookie name contains illegal characters
     * (for example, a comma, space, or semicolon) or it is one of the tokens reserved for use
     * by the cookie protocol
     *
     * @see #setValue
     * @see #setVersion
     */
    public Cookie(String name, String value) {
        if (!isToken(name)
                || name.equalsIgnoreCase("Comment") // rfc2019
                || name.equalsIgnoreCase("Discard") // 2019++
                || name.equalsIgnoreCase("Domain")
                || name.equalsIgnoreCase("Expires") // (old cookies)
                || name.equalsIgnoreCase("Max-Age") // rfc2019
                || name.equalsIgnoreCase("Path")
                || name.equalsIgnoreCase("Secure")
                || name.equalsIgnoreCase("Version")
                || name.startsWith("$")
        ) {
            String errMsg = lStrings.getString("err.cookie_name_is_token");
            Object[] errArgs = new Object[1];
            errArgs[0] = name;
            errMsg = MessageFormat.format(errMsg, errArgs);
            throw new IllegalArgumentException(errMsg);
        }

        this.name = name;
        this.value = value;
    }

    /**
     * Specifies a comment that describes a cookie's purpose.
     * The comment is useful if the browser presents the cookie
     * to the user. Comments
     * are not supported by Netscape Version 0 cookies.
     *
     * @param purpose a <code>String</code> specifying the comment
     * to display to the user
     *
     * @see #getComment
     */
    public void setComment(String purpose) {
        comment = purpose;
    }

    /**
     * Returns the comment describing the purpose of this cookie, or
     * <code>null</code> if the cookie has no comment.
     *
     * @return a <code>String</code> containing the comment,
     * or <code>null</code> if none
     *
     * @see #setComment
     */
    public String getComment() {
        return comment;
    }

    /**
     * Specifies the domain within which this cookie should be presented.
     *
     * <p>The form of the domain name is specified by RFC 2109. A domain
     * name begins with a dot (<code>.foo.com</code>) and means that
     * the cookie is visible to servers in a specified Domain Name System
     * (DNS) zone (for example, <code>www.foo.com</code>, but not
     * <code>a.b.foo.com</code>). By default, cookies are only returned
     * to the server that sent them.
     *
     * @param pattern a <code>String</code> containing the domain name
     * within which this cookie is visible; form is according to RFC 2109
     *
     * @see #getDomain
     */
    public void setDomain(String pattern) {
        domain = pattern.toLowerCase(); // IE allegedly needs this
    }

    /**
     * Returns the domain name set for this cookie. The form of
     * the domain name is set by RFC 2109.
     *
     * @return a <code>String</code> containing the domain name
     *
     * @see #setDomain
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Sets the maximum age of the cookie in seconds.
     *
     * <p>A positive value indicates that the cookie will expire
     * after that many seconds have passed. Note that the value is
     * the <i>maximum</i> age when the cookie will expire, not the cookie's
     * current age.
     *
     * <p>A negative value means
     * that the cookie is not stored persistently and will be deleted
     * when the Web browser exits. A zero value causes the cookie
     * to be deleted.
     *
     * @param expiry an integer specifying the maximum age of the
     * cookie in seconds; if negative, means the cookie is not stored;
     * if zero, deletes the cookie
     *
     * @see #getMaxAge
     */
    public void setMaxAge(int expiry) {
        maxAge = expiry;
    }

    /**
     * Returns the maximum age of the cookie, specified in seconds,
     * By default, <code>-1</code> indicating the cookie will persist
     * until browser shutdown.
     *
     * @return an integer specifying the maximum age of the
     * cookie in seconds; if negative, means the cookie persists
     * until browser shutdown
     *
     * @see #setMaxAge
     */
    public int getMaxAge() {
        return maxAge;
    }

    /**
     * Specifies a path for the cookie
     * to which the client should return the cookie.
     *
     * <p>The cookie is visible to all the pages in the directory
     * you specify, and all the pages in that directory's subdirectories.
     * A cookie's path must include the servlet that set the cookie,
     * for example, <i>/catalog</i>, which makes the cookie
     * visible to all directories on the server under <i>/catalog</i>.
     *
     * <p>Consult RFC 2109 (available on the Internet) for more
     * information on setting path names for cookies.
     *
     * @param uri a <code>String</code> specifying a path
     *
     * @see #getPath
     */
    public void setPath(String uri) {
        path = uri;
    }

    /**
     * Returns the path on the server
     * to which the browser returns this cookie. The
     * cookie is visible to all subpaths on the server.
     *
     * @return a <code>String</code> specifying a path that contains
     * a servlet name, for example, <i>/catalog</i>
     *
     * @see #setPath
     */
    public String getPath() {
        return path;
    }

    /**
     * Indicates to the browser whether the cookie should only be sent
     * using a secure protocol, such as HTTPS or SSL.
     *
     * <p>The default value is <code>false</code>.
     *
     * @param flag if <code>true</code>, sends the cookie from the browser
     * to the server only when using a secure protocol; if <code>false</code>,
     * sent on any protocol
     *
     * @see #getSecure
     */
    public void setSecure(boolean flag) {
        secure = flag;
    }

    /**
     * Returns <code>true</code> if the browser is sending cookies
     * only over a secure protocol, or <code>false</code> if the
     * browser can send cookies using any protocol.
     *
     * @return <code>true</code> if the browser uses a secure protocol;
     * otherwise, <code>true</code>
     *
     * @see #setSecure
     */
    public boolean getSecure() {
        return secure;
    }

    /**
     * Returns the name of the cookie. The name cannot be changed after
     * creation.
     *
     * @return a <code>String</code> specifying the cookie's name
     */
    public String getName() {
        return name;
    }

    /**
     * Assigns a new value to a cookie after the cookie is created.
     * If you use a binary value, you may want to use BASE64 encoding.
     *
     * <p>With Version 0 cookies, values should not contain white
     * space, brackets, parentheses, equals signs, commas,
     * double quotes, slashes, question marks, at signs, colons,
     * and semicolons. Empty values may not behave the same way
     * on all browsers.
     *
     * @param newValue a <code>String</code> specifying the new value
     *
     * @see #getValue
     * @see Cookie
     */
    public void setValue(String newValue) {
        value = newValue;
    }

    /**
     * Returns the value of the cookie.
     *
     * @return a <code>String</code> containing the cookie's
     * present value
     *
     * @see #setValue
     * @see Cookie
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the version of the protocol this cookie complies
     * with. Version 1 complies with RFC 2109,
     * and version 0 complies with the original
     * cookie specification drafted by Netscape. Cookies provided
     * by a browser use and identify the browser's cookie version.
     *
     * @return 0 if the cookie complies with the original Netscape
     * specification; 1 if the cookie complies with RFC 2109
     *
     * @see #setVersion
     */
    public int getVersion() {
        return version;
    }

    /**
     * Sets the version of the cookie protocol this cookie complies
     * with. Version 0 complies with the original Netscape cookie
     * specification. Version 1 complies with RFC 2109.
     *
     * <p>Since RFC 2109 is still somewhat new, consider
     * version 1 as experimental; do not use it yet on production sites.
     *
     * @param v 0 if the cookie should comply with the original Netscape
     * specification; 1 if the cookie should comply with RFC 2109
     *
     * @see #getVersion
     */
    public void setVersion(int v) {
        version = v;
    }

    // Note -- disabled for now to allow full Netscape compatibility
    // from RFC 2068, token special case characters
    //
    // private static final String tspecials = "()<>@,;:\\\"/[]?={} \t";

    private static final String tspecials = ",; ";

    /**
     * Tests a string and returns true if the string counts as a
     * reserved token in the Java language.
     *
     * @param value the <code>String</code> to be tested
     *
     * @return <code>true</code> if the <code>String</code> is
     * a reserved token; <code>false</code> if it is not
     */
    private boolean isToken(String value) {
        int len = value.length();

        for (int i = 0; i < len; i++) {
            char c = value.charAt(i);

            if (c < 0x20 || c >= 0x7f || tspecials.indexOf(c) != -1)
                return false;
        }
        return true;
    }

    /**
     * Overrides the standard <code>java.lang.Object.clone</code>
     * method to return a copy of this cookie.
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}

