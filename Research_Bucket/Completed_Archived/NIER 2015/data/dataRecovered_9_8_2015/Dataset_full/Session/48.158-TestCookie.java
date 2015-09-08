/*
 * $Header: /home/cvs/jakarta-commons/httpclient/src/test/org/apache/commons/httpclient/Attic/TestCookie.java,v 1.22.2.4 2004/06/05 16:32:01 olegk Exp $
 * $Revision: 1.22.2.4 $
 * $Date: 2004/06/05 16:32:01 $
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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.util.Date;
import java.util.Vector;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Iterator;
import org.apache.commons.httpclient.cookie.*;


/**
 * Test cases for Cookie
 *
 * @author BC Holmes
 * @author Rod Waldhoff
 * @author dIon Gillard
 * @author <a href="mailto:JEvans@Cyveillance.com">John Evans</a>
 * @author Marc A. Saegesser
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * @version $Revision: 1.22.2.4 $
 */
public class TestCookie extends TestCase {


    // -------------------------------------------------------------- Constants

    private static final String DOMAIN_NAME = "www.apache.org";
    private static final String TEST_COOKIE = "cookie-name=cookie-value";
    private static final String OLD_EXPIRY = "Expires=Thu, 01-Jan-1970 00:00:10 GMT";
    private static final String SEP = ";";
    private static final String ROOT_PATH = "/";
    private static final int DEFAULT_PORT = 80;

    private String[] testName = { "custno", "name", "name" };
    private String[] testValue = { "12345", "John", "Doe, John" };
    private String[] testDomain = { "www.apache.org", ".apache.org",
        ".apache.org" };

    // ------------------------------------------------------------ Constructor


    public TestCookie(String name) {
        super(name);
    }


    // ------------------------------------------------------- TestCase Methods


    public static Test suite() {
        return new TestSuite(TestCookie.class);
    }


    // ------------------------------------------------------- Helper Methods

    private static Cookie[] cookieParse(int policy, String host, String path, boolean isSecure, Header setHeader)
      throws MalformedCookieException 
    {
        CookieSpec parser = CookiePolicy.getSpecByPolicy(policy);
        Cookie[] cookies = parser.parse(host, DEFAULT_PORT, path, isSecure, setHeader);
        if (cookies != null)
        {
            for(int i = 0; i < cookies.length; i++)
            {
                parser.validate(host, DEFAULT_PORT, path, isSecure, cookies[i]);
            }
        }
        return cookies;
    }


    private static Cookie[] cookieParse(String host, String path, boolean isSecure, Header setHeader)
      throws MalformedCookieException 
    {
        return cookieParse(CookiePolicy.RFC2109, host, path, isSecure, setHeader);
    }


    private static Cookie[] cookieParse(String host, String path, Header setHeader)
      throws MalformedCookieException 
    {
        return cookieParse(CookiePolicy.RFC2109, host, path, false, setHeader);
    }


    private static Cookie[] netscapeCcookieParse(String host, String path, Header setHeader)
      throws MalformedCookieException 
    {
        return cookieParse(CookiePolicy.NETSCAPE_DRAFT, host, path, false, setHeader);
    }


    public static Header cookieCreateHeader(int policy, String domain, int port, String path, boolean secure, Cookie[] cookies)
    {
        CookieSpec matcher = CookiePolicy.getSpecByPolicy(policy);
        cookies = matcher.match(domain, port, path, secure, cookies);
        if ((cookies != null) && (cookies.length > 0))
        {
            return matcher.formatCookieHeader(cookies);
        }
        else
        {
            return null;
        } 
    }

    public static Header cookieCreateHeader(String domain, int port, String path, boolean secure, Cookie[] cookies)
    {
        return cookieCreateHeader(CookiePolicy.RFC2109, domain, port, path, secure, cookies);
    }


    public boolean cookieMatch(int policy, String domain, int port, String path, boolean secure, Cookie cookie)
    {
        CookieSpec matcher = CookiePolicy.getSpecByPolicy(policy);
        return matcher.match(domain, port, path, secure, cookie);
    }


    public boolean cookieMatch(String domain, int port, String path, boolean secure, Cookie cookie)
    {
        return cookieMatch(CookiePolicy.RFC2109, domain, port, path, secure, cookie);
    }

    // ------------------------------------------------------------ Parse1 Test


    /**
     * Test basic parse (with various spacings
     */
    public void testParse1() throws Exception {
        String headerValue = "custno = 12345; comment=test; version=1," +
            " name=John; version=1; max-age=600; secure; domain=.apache.org";
        Cookie[] cookies = cookieParse(DOMAIN_NAME,"/", true, new Header(
            "set-cookie", headerValue));
        checkResultsOfParse(cookies, 2, 0);
    }


    protected void checkResultsOfParse(
        Cookie[] cookies, int length, int offset) throws Exception {

        assertTrue("number of cookies should be " + length + ", but is " +
               cookies.length + " instead.", cookies.length == length);

        for (int i = 0; i < cookies.length; i++) {

            assertTrue("Name of cookie " + i + " should be \"" +
                   testName[i+offset] + "\", but is " + cookies[i].getName() +
                   " instead.",
                   testName[i+offset].equals(cookies[i].getName()));
            assertTrue("Value of cookie " + i + " should be \"" +
                   testValue[i+offset] + "\", but is " +
                   cookies[i].getValue() + " instead.",
                   testValue[i+offset].equals(cookies[i].getValue()));
            assertTrue("Domain of cookie " + i + " should be \"" +
                   testDomain[i+offset] + "\", but is " +
                   cookies[i].getDomain() + " instead.",
                   testDomain[i+offset].equalsIgnoreCase(
                       cookies[i].getDomain()));
        }
    }


    // ------------------------------------------------------------ Parse2 Test


    /**
     * Test no spaces
     */
    public void testParse2() throws Exception {
        String headerValue = "custno=12345;comment=test; version=1," +
            "name=John;version=1;max-age=600;secure;domain=.apache.org";
        Cookie[] cookies = cookieParse(DOMAIN_NAME, "/", true, new Header(
            "set-cookie", headerValue));
        checkResultsOfParse(cookies, 2, 0);
    }


    // ------------------------------------------------------------ Parse3 Test


    /**
     * Test parse with quoted text
     */
    public void testParse3() throws Exception {
        String headerValue =
            "name=\"Doe, John\";version=1;max-age=600;secure;domain=.apache.org";
        Cookie[] cookies = cookieParse(DOMAIN_NAME,"/", true, new Header(
            "set-cookie", headerValue));
        checkResultsOfParse(cookies, 1, 2);
    }

    // ------------------------------------------------------------- More Tests

    // see http://nagoya.apache.org/bugzilla/show_bug.cgi?id=5279
    public void testQuotedExpiresAttribute() throws Exception {
        String headerValue = "custno=12345;Expires='Thu, 01-Jan-2070 00:00:10 GMT'";
        Cookie[] cookies = cookieParse(DOMAIN_NAME,"/",true,new Header(
            "set-cookie", headerValue));
        assertNotNull("Expected some cookies",cookies);
        assertEquals("Expected 1 cookie",1,cookies.length);
        assertNotNull("Expected cookie to have getExpiryDate",cookies[0].getExpiryDate());
    }

    public void testSecurityError() throws Exception {
        String headerValue = "custno=12345;comment=test; version=1," +
            "name=John;version=1;max-age=600;secure;domain=jakarta.apache.org";
        try {
            Cookie[] cookies = cookieParse(DOMAIN_NAME, "/", new Header(
                "set-cookie", headerValue));
            fail("HttpException exception should have been thrown");
        } catch (HttpException e) {
            // expected
        }
    }

    public void testParseSimple() throws Exception {
        Header setCookie = new Header("Set-Cookie","cookie-name=cookie-value");
        Cookie[] parsed = cookieParse("127.0.0.1","/path/path",setCookie);
        assertEquals("Found 1 cookie.",1,parsed.length);
        assertEquals("Name","cookie-name",parsed[0].getName());
        assertEquals("Value","cookie-value",parsed[0].getValue());
        assertTrue("Comment",null == parsed[0].getComment());
        assertTrue("ExpiryDate",null == parsed[0].getExpiryDate());
        //assertTrue("isToBeDiscarded",parsed[0].isToBeDiscarded());
        assertTrue("isPersistent",!parsed[0].isPersistent());
        assertEquals("Domain","127.0.0.1",parsed[0].getDomain());
        assertEquals("Path","/path",parsed[0].getPath());
        assertTrue("Secure",!parsed[0].getSecure());
        assertEquals("Version",0,parsed[0].getVersion());
    }
 
 
    public void testParseSimple2() throws Exception {
        Header setCookie = new Header("Set-Cookie","cookie-name=cookie-value");
        Cookie[] parsed = cookieParse("127.0.0.1","/path",setCookie);
        assertEquals("Found 1 cookie.",1,parsed.length);
        assertEquals("Name","cookie-name",parsed[0].getName());
        assertEquals("Value","cookie-value",parsed[0].getValue());
        assertTrue("Comment",null == parsed[0].getComment());
        assertTrue("ExpiryDate",null == parsed[0].getExpiryDate());
        //assertTrue("isToBeDiscarded",parsed[0].isToBeDiscarded());
        assertTrue("isPersistent",!parsed[0].isPersistent());
        assertEquals("Domain","127.0.0.1",parsed[0].getDomain());
        assertEquals("Path","/",parsed[0].getPath());
        assertTrue("Secure",!parsed[0].getSecure());
        assertEquals("Version",0,parsed[0].getVersion());
    }
 
 
    public void testParseNoValue() throws Exception {
        Header setCookie = new Header("Set-Cookie","cookie-name=");
        Cookie[] parsed = cookieParse("127.0.0.1","/",setCookie);
        assertEquals("Found 1 cookie.",1,parsed.length);
        assertEquals("Name","cookie-name",parsed[0].getName());
        assertTrue("Value",null == parsed[0].getValue());
        assertTrue("Comment",null == parsed[0].getComment());
        assertTrue("ExpiryDate",null == parsed[0].getExpiryDate());
        //assertTrue("isToBeDiscarded",parsed[0].isToBeDiscarded());
        assertTrue("isPersistent",!parsed[0].isPersistent());
        assertEquals("Domain","127.0.0.1",parsed[0].getDomain());
        assertEquals("Path","/",parsed[0].getPath());
        assertTrue("Secure",!parsed[0].getSecure());
        assertEquals("Version",0,parsed[0].getVersion());
    }

    public void testParseWithWhiteSpace() throws Exception {
        Header setCookie = new Header("Set-Cookie"," cookie-name  =    cookie-value  ");
        Cookie[] parsed = cookieParse("127.0.0.1","/",setCookie);
        assertEquals("Found 1 cookie.",1,parsed.length);
        assertEquals("Name","cookie-name",parsed[0].getName());
        assertEquals("Value","cookie-value",parsed[0].getValue());
        assertEquals("Domain","127.0.0.1",parsed[0].getDomain());
        assertEquals("Path","/",parsed[0].getPath());
        assertTrue("Secure",!parsed[0].getSecure());
        assertTrue("ExpiryDate",null == parsed[0].getExpiryDate());
        assertTrue("Comment",null == parsed[0].getComment());
    }

    public void testParseWithQuotes() throws Exception {
        Header setCookie = new Header("Set-Cookie"," cookie-name  =  \" cookie-value \" ;path=/");
        Cookie[] parsed = cookieParse("127.0.0.1","/",setCookie);
        assertEquals("Found 1 cookie.",1,parsed.length);
        assertEquals("Name","cookie-name",parsed[0].getName());
        assertEquals("Value"," cookie-value ",parsed[0].getValue());
        assertEquals("Domain","127.0.0.1",parsed[0].getDomain());
        assertEquals("Path","/",parsed[0].getPath());
        assertTrue("Secure",!parsed[0].getSecure());
        assertTrue("ExpiryDate",null == parsed[0].getExpiryDate());
        assertTrue("Comment",null == parsed[0].getComment());
    }

    public void testParseWithPath() throws Exception {
        Header setCookie = new Header("Set-Cookie","cookie-name=cookie-value; Path=/path/");
        Cookie[] parsed = cookieParse("127.0.0.1","/path/path",setCookie);
        assertEquals("Found 1 cookie.",1,parsed.length);
        assertEquals("Name","cookie-name",parsed[0].getName());
        assertEquals("Value","cookie-value",parsed[0].getValue());
        assertEquals("Domain","127.0.0.1",parsed[0].getDomain());
        assertEquals("Path","/path/",parsed[0].getPath());
        assertTrue("Secure",!parsed[0].getSecure());
        assertTrue("ExpiryDate",null == parsed[0].getExpiryDate());
        assertTrue("Comment",null == parsed[0].getComment());
    }

    public void testParseWithDomain() throws Exception {
        Header setCookie = new Header("Set-Cookie","cookie-name=cookie-value; Domain=127.0.0.1");
        Cookie[] parsed = cookieParse("127.0.0.1","/",setCookie);
        assertEquals("Found 1 cookie.",1,parsed.length);
        assertEquals("Name","cookie-name",parsed[0].getName());
        assertEquals("Value","cookie-value",parsed[0].getValue());
        assertEquals("Domain","127.0.0.1",parsed[0].getDomain());
        assertEquals("Path","/",parsed[0].getPath());
        assertTrue("Secure",!parsed[0].getSecure());
        assertTrue("ExpiryDate",null == parsed[0].getExpiryDate());
        assertTrue("Comment",null == parsed[0].getComment());
    }

    public void testParseWithSecure() throws Exception {
        Header setCookie = new Header("Set-Cookie","cookie-name=cookie-value; secure");
        Cookie[] parsed = cookieParse("127.0.0.1","/",true,setCookie);
        assertEquals("Found 1 cookie.",1,parsed.length);
        assertEquals("Name","cookie-name",parsed[0].getName());
        assertEquals("Value","cookie-value",parsed[0].getValue());
        assertEquals("Domain","127.0.0.1",parsed[0].getDomain());
        assertEquals("Path","/",parsed[0].getPath());
        assertTrue("Secure",parsed[0].getSecure());
        assertTrue("ExpiryDate",null == parsed[0].getExpiryDate());
        assertTrue("Comment",null == parsed[0].getComment());
    }

    public void testParseWithComment() throws Exception {
        Header setCookie = new Header("Set-Cookie","cookie-name=cookie-value; comment=\"This is a comment.\"");
        Cookie[] parsed = cookieParse("127.0.0.1","/",true,setCookie);
        assertEquals("Found 1 cookie.",1,parsed.length);
        assertEquals("Name","cookie-name",parsed[0].getName());
        assertEquals("Value","cookie-value",parsed[0].getValue());
        assertEquals("Domain","127.0.0.1",parsed[0].getDomain());
        assertEquals("Path","/",parsed[0].getPath());
        assertTrue("Secure",!parsed[0].getSecure());
        assertTrue("ExpiryDate",null == parsed[0].getExpiryDate());
        assertEquals("Comment","This is a comment.",parsed[0].getComment());
    }

    public void testParseWithExpires() throws Exception {
        Header setCookie = new Header("Set-Cookie","cookie-name=cookie-value;Expires=Thu, 01-Jan-1970 00:00:10 GMT");
        Cookie[] parsed = cookieParse("127.0.0.1","/",true,setCookie);
        assertEquals("Found 1 cookie.",1,parsed.length);
        assertEquals("Name","cookie-name",parsed[0].getName());
        assertEquals("Value","cookie-value",parsed[0].getValue());
        assertEquals("Domain","127.0.0.1",parsed[0].getDomain());
        assertEquals("Path","/",parsed[0].getPath());
        assertTrue("Secure",!parsed[0].getSecure());
        assertEquals(new Date(10000L),parsed[0].getExpiryDate());
        assertTrue("Comment",null == parsed[0].getComment());
    }

    public void testParseWithAll() throws Exception {
        Header setCookie = new Header("Set-Cookie","cookie-name=cookie-value;Version=1;Path=/commons;Domain=.apache.org;Comment=This is a comment.;secure;Expires=Thu, 01-Jan-1970 00:00:10 GMT");
        Cookie[] parsed = cookieParse(".apache.org","/commons/httpclient",true,setCookie);
        assertEquals("Found 1 cookie.",1,parsed.length);
        assertEquals("Name","cookie-name",parsed[0].getName());
        assertEquals("Value","cookie-value",parsed[0].getValue());
        assertEquals("Domain",".apache.org",parsed[0].getDomain());
        assertEquals("Path","/commons",parsed[0].getPath());
        assertTrue("Secure",parsed[0].getSecure());
        assertEquals(new Date(10000L),parsed[0].getExpiryDate());
        assertEquals("Comment","This is a comment.",parsed[0].getComment());
        assertEquals("Version",1,parsed[0].getVersion());
    }

    public void testParseMultipleDifferentPaths() throws Exception {
        Header setCookie = new Header("Set-Cookie","name1=value1;Version=1;Path=/commons,name1=value2;Version=1;Path=/commons/httpclient;Version=1");
        Cookie[] parsed = cookieParse(".apache.org","/commons/httpclient",true,setCookie);
        HttpState state = new HttpState();
        state.addCookies(parsed);
        Cookie[] cookies = state.getCookies();
        assertEquals("Wrong number of cookies.",2,cookies.length);
        assertEquals("Name","name1",cookies[0].getName());
        assertEquals("Value","value1",cookies[0].getValue());
        assertEquals("Name","name1",cookies[1].getName());
        assertEquals("Value","value2",cookies[1].getValue());
    }

    public void testParseMultipleSamePaths() throws Exception {
        Header setCookie = new Header("Set-Cookie","name1=value1;Version=1;Path=/commons,name1=value2;Version=1;Path=/commons");
        Cookie[] parsed = cookieParse(".apache.org","/commons/httpclient",true,setCookie);
        HttpState state = new HttpState();
        state.addCookies(parsed);
        Cookie[] cookies = state.getCookies();
        assertEquals("Found 1 cookies.",1,cookies.length);
        assertEquals("Name","name1",cookies[0].getName());
        assertEquals("Value","value2",cookies[0].getValue());
    }

    public void testParseWithWrongDomain() throws Exception {
        Header setCookie = new Header("Set-Cookie","cookie-name=cookie-value; domain=127.0.0.1; version=1");
        try {
            Cookie[] parsed = cookieParse("127.0.0.2","/",setCookie);
            fail("HttpException exception should have been thrown");
        } catch (HttpException e) {
            // expected
        }
    }

    public void testParseWithWrongDomain2() throws Exception {
        Header setCookie = new Header("Set-Cookie","cookie-name=cookie-value; domain=.c.com; version=1");
        try {
            Cookie[] parsed = cookieParse("a.b.c.com","/",setCookie);
            fail("HttpException exception should have been thrown");
        } catch (HttpException e) {
            // expected
        }
    }

    /**
     * Domain has no embedded dots
     */
    public void testParseWithIllegalDomain() throws Exception {
        Header setCookie = new Header("Set-Cookie","cookie-name=cookie-value; domain=.com; version=1");
        try {
            Cookie[] parsed = cookieParse("b.com","/",setCookie);
            fail("HttpException exception should have been thrown");
        } catch (HttpException e) {
            // expected
        }
    }

    /**
     * Domain has no embedded dots again
     */
    public void testParseWithIllegalDomain2() throws Exception {
        Header setCookie = new Header("Set-Cookie","cookie-name=cookie-value; domain=.com.; version=1");
        try {
            Cookie[] parsed = cookieParse("b.com","/",setCookie);
            fail("HttpException exception should have been thrown");
        } catch (HttpException e) {
            // expected
        }
    }

    public void testParseWithIllegalNetscapeDomain1() throws Exception {
        Header setCookie = new Header("Set-Cookie","cookie-name=cookie-value; domain=.com");
        try {
            Cookie[] parsed = netscapeCcookieParse("a.com","/",setCookie);
            fail("HttpException exception should have been thrown");
        } catch (HttpException e) {
            // expected
        }
    }

    public void testParseWithWrongNetscapeDomain2() throws Exception {
        Header setCookie = new Header("Set-Cookie","cookie-name=cookie-value; domain=.y.z");
        try {
            Cookie[] parsed = netscapeCcookieParse("x.y.z","/",setCookie);
            fail("HttpException exception should have been thrown");
        } catch (HttpException e) {
            // expected
        }
    }

    public void testParseWithWrongPath() throws Exception {
        Header setCookie = new Header("Set-Cookie","cookie-name=cookie-value; domain=127.0.0.1; path=/not/just/root");
        try {
            Cookie[] parsed = cookieParse("127.0.0.1","/",setCookie);
            fail("HttpException exception should have been thrown");
        } catch (HttpException e) {
            // expected
        }
    }

    public void testParseWithNullDomain() {
        Header setCookie = new Header("Set-Cookie","cookie-name=cookie-value; domain=127.0.0.1; path=/; secure");
        try {
            Cookie[] parsed = cookieParse(null,"/",false,setCookie);
            fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException e) {
            // expected
        } catch (Exception e){
            fail("Should have thrown IllegalArgumentException.");
        }
    }

    public void testParseWithNullPath() {
        Header setCookie = new Header("Set-Cookie","cookie-name=cookie-value; domain=127.0.0.1; path=/; secure");
        try {
            Cookie[] parsed = cookieParse("127.0.0.1",null,false,setCookie);
            fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException e) {
            // expected
        } catch (Exception e){
            fail("Should have thrown IllegalArgumentException.");
        }
    }

    public void testParseWithNullDomainAndPath() {
        Header setCookie = new Header("Set-Cookie","cookie-name=cookie-value; domain=127.0.0.1; path=/; secure");
        try {
            Cookie[] parsed = cookieParse(null,null,false,setCookie);
            fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException e) {
            // expected
        } catch (Exception e){
            fail("Should have thrown IllegalArgumentException.");
        }
    }
    
    public void testParseWithPathMismatch() {
        Header setCookie = new Header("Set-Cookie","cookie-name=cookie-value; path=/path/path/path");
        try {
            Cookie[] parsed = cookieParse("127.0.0.1","/path",false,setCookie);
            fail("HttpException should have been thrown.");
        } catch (HttpException e) {
            // expected
        } catch (Exception e){
            fail("Should have thrown HttpException.");
        }
    }
    
    public void testParseWithPathMismatch2() {
        Header setCookie = new Header("Set-Cookie","cookie-name=cookie-value; path=/foobar");
        try {
            Cookie[] parsed = cookieParse("127.0.0.1","/foo",false,setCookie);
            fail("HttpException should have been thrown.");
        } catch (HttpException e) {
            // expected
        } catch (Exception e){
            fail("Should have thrown HttpException.");
        }
    }
    
    public void testComparator() throws Exception {
        Header setCookie = null;
        Cookie[] parsed = null;
        Vector cookies = new Vector();
        // Cookie 0
        setCookie = new Header("Set-Cookie","cookie-name=cookie-value;Path=/commons;Domain=.apache.org;Expires=Thu, 01-Jan-1970 00:00:10 GMT");
        parsed = cookieParse(".apache.org", "/commons/httpclient", true,
                              setCookie);
        cookies.add(parsed[0]);
        // Cookie 1
        setCookie = new Header("Set-Cookie","cookie-name=cookie-value;Path=/commons/bif;Domain=.apache.org;Expires=Thu, 01-Jan-1970 00:00:10 GMT");
        parsed = cookieParse(".apache.org","/commons/bif/httpclient",true,setCookie);
        cookies.add(parsed[0]);
        // Cookie 2
        setCookie = new Header("Set-Cookie","cookie-name=cookie-value;Path=/commons;Domain=.baz.org;Expires=Thu, 01-Jan-1970 00:00:10 GMT");
        parsed = cookieParse(".baz.org","/commons/httpclient",true,setCookie);
        cookies.add(parsed[0]);
        // Cookie 3
        setCookie = new Header("Set-Cookie","cookie-name=cookie-value;Path=/commons/bif;Domain=.baz.org;Expires=Thu, 01-Jan-1970 00:00:10 GMT");
        parsed = cookieParse(".baz.org","/commons/bif/httpclient",true,setCookie);
        cookies.add(parsed[0]);
        // Cookie 4
        setCookie = new Header("Set-Cookie","cookie-name=cookie-value;Path=/commons;Domain=.baz.com;Expires=Thu, 01-Jan-1970 00:00:10 GMT");
        parsed = cookieParse(".baz.com","/commons/httpclient",true,setCookie);
        cookies.add(parsed[0]);
        // The order should be:
        // 1, 0, 3, 2, 4
        parsed = (Cookie[])cookies.toArray(new Cookie[0]);
        SortedSet set = new TreeSet(parsed[0]);
        int pass = 0;
        for (Iterator itr = set.iterator(); itr.hasNext();) {
            Cookie cookie = (Cookie)itr.next();
            switch (pass) {
                case 0:
                    assertTrue("0th cookie should be cookie[1]", cookie == parsed[1]);
                    break;
                case 1:
                    assertTrue("1st cookie should be cookie[0]", cookie == parsed[0]);
                    break;
                case 2:
                    assertTrue("2nd cookie should be cookie[3]", cookie == parsed[3]);
                    break;
                case 3:
                    assertTrue("3rd cookie should be cookie[2]", cookie == parsed[2]);
                    break;
                case 4:
                    assertTrue("4th cookie should be cookie[4]", cookie == parsed[4]);
                    break;
                default:
                    fail("This should never happen.");
            }
            pass++;
        }
        try {
            parsed[0].compare("foo", "bar");
            fail("Should have thrown an exception trying to compare non-cookies");
        }
        catch (ClassCastException ex) {
            // expected
        }
    }
    
    /** Call Cookie.createCookieHeader providing null for domain to match on
     */
    public void testCreateCookieHeaderWithNullDomain() throws Exception {
        Header setCookie = new Header("Set-Cookie",
                                      TEST_COOKIE + SEP + OLD_EXPIRY);
        Cookie[] parsed = cookieParse(DOMAIN_NAME, ROOT_PATH, true, setCookie);

        try{
            Header header = cookieCreateHeader(null, DEFAULT_PORT, ROOT_PATH, false, parsed);
            fail("IllegalArgumentException should have been thrown.");
        }catch(IllegalArgumentException e){
            // Expected
        }catch(Exception e){
            fail("Threw wrong type of exception.  Expected IllegalArgumentException.");
        }
    }
    
    /** Call Cookie.createCookieHeader providing null for path to match on
     */
    public void testCreateCookieHeaderWithNullPath() throws Exception{
        Header setCookie = new Header("Set-Cookie",
                                      TEST_COOKIE + SEP + OLD_EXPIRY);
        Cookie[] parsed = cookieParse(DOMAIN_NAME, ROOT_PATH, false, setCookie);

        try{
            Header header = cookieCreateHeader(DOMAIN_NAME, DEFAULT_PORT, null, false, parsed);
            fail("IllegalArgumentException should have been thrown.");
        }catch(IllegalArgumentException e){
            // Expected
        }catch(Exception e){
            fail("Threw wrong type of exception.  Expected IllegalArgumentException.");
        }
    }

    /**
     * Verify that cookies with no domain or path don't get added to a cookie header.
     */
    public void testCreateCookieHeaderWithUninitializedCookies() throws Exception {
        Cookie cookies[] = new Cookie[2];
        cookies[0] = new Cookie(null, "name0", "value0");
        cookies[1] = new Cookie(null, "name1", "value1", null, null, false);

        Header header = cookieCreateHeader(DOMAIN_NAME, DEFAULT_PORT, ROOT_PATH, false, cookies);
        assertEquals("createCookieHeader added cookies with null domains or paths", null, header);
    }

    /** Call Cookie.createCookieHeader providing null for domain and path to
     * match on
     */
    public void testCreateCookieHeaderWithNullDomainAndPath() throws Exception {
        Header setCookie = new Header("Set-Cookie",
                                      TEST_COOKIE + SEP + OLD_EXPIRY);
        Cookie[] parsed = cookieParse(DOMAIN_NAME, ROOT_PATH, true, setCookie);

        try{
            Header header = cookieCreateHeader(null, DEFAULT_PORT, null, false, parsed);
            fail("IllegalArgumentException should have been thrown.");
        }catch(IllegalArgumentException e){
            // Expected
        }catch(Exception e){
            fail("Threw wrong type of exception.  Expected IllegalArgumentException.");
        }
    }

    /**
     * Tests several date formats.
     */
    public void testDateFormats() throws Exception {
        //comma, dashes
        checkDate("Thu, 01-Jan-70 00:00:10 GMT");
        checkDate("Thu, 01-Jan-2070 00:00:10 GMT");
        //no comma, dashes
        checkDate("Thu 01-Jan-70 00:00:10 GMT");
        checkDate("Thu 01-Jan-2070 00:00:10 GMT");
        //comma, spaces
        checkDate("Thu, 01 Jan 70 00:00:10 GMT");
        checkDate("Thu, 01 Jan 2070 00:00:10 GMT");
        //no comma, spaces
        checkDate("Thu 01 Jan 70 00:00:10 GMT");
        checkDate("Thu 01 Jan 2070 00:00:10 GMT");
        //weird stuff
        checkDate("Wed, 20-Nov-2002 09-38-33 GMT");


        try {
            checkDate("this aint a date");
            fail("Date check is bogous");
        } catch(Exception e) {
            /* must fail */
        }
    }

    private void checkDate(String date) throws Exception {
        Header setCookie = new Header("Set-Cookie", "custno=12345;Expires='"+date+"'");
        cookieParse("localhost","/",setCookie);
    }
    
    
    /**
     * Tests default constructor.
     */
    public void testDefaultConsttuctor() {
        Cookie dummy = new Cookie();
        assertEquals( "noname=", dummy.toExternalForm() );
    }

    /**
     * Tests whether domain attribute check is case-insensitive.
     */
    public void testDomainCaseInsensitivity() throws Exception {
        Header setCookie = new Header(
          "Set-Cookie", "name=value; path=/; domain=.whatever.com");
        try {
            Cookie[] parsed = cookieParse("www.WhatEver.com", "/", false, setCookie );
        }
        catch(HttpException e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.toString());
        }
    }
    

    /**
     * Tests if cookie constructor rejects cookie name containing blanks.
     */
    public void testCookieNameWithBlanks() throws Exception {
        Header setcookie = new Header("Set-Cookie", "invalid name=");
        cookieParse(CookiePolicy.COMPATIBILITY, "localhost", "/", false, setcookie); 
        try {
            cookieParse(CookiePolicy.RFC2109, "localhost", "/", false, setcookie);
            fail("MalformedCookieException must have been thrown");
        }
        catch(MalformedCookieException e) {
            // Expected            
        }
    }


    /**
     * Tests if cookie constructor rejects cookie name starting with $.
     */
    public void testCookieNameStartingWithDollarSign() throws Exception {
        Header setcookie = new Header("Set-Cookie", "$invalid_name=");
        cookieParse(CookiePolicy.COMPATIBILITY, "localhost", "/", false, setcookie); 
        try {
            cookieParse(CookiePolicy.RFC2109, "localhost", "/", false, setcookie); 
            fail("MalformedCookieException must have been thrown");
        }
        catch(MalformedCookieException e) {
            // Expected            
        }
    }

    /**
     * Tests if default cookie validator rejects cookies originating from a host without domain
     * where domain attribute does not match the host of origin 
     */
    
    public void testInvalidDomainWithSimpleHostName() {    
        CookieSpec parser = CookiePolicy.getDefaultSpec();
        Header setCookie = null;
        Cookie[] cookies = null;
        try {
            setCookie = new Header(
            "Set-Cookie", "name=\"value\"; version=\"1\"; path=\"/\"; domain=\".mydomain.com\"");
            cookies = parser.parse("host", 80, "/", false, setCookie );
            try {
                parser.validate("host", 80, "/", false, cookies[0]);
                fail("MalformedCookieException must have thrown");
            }
            catch(MalformedCookieException expected) {
            }
        }
        catch(HttpException e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.toString());
        }
        try {
            setCookie = new Header(
            "Set-Cookie", "name=\"value\"; version=\"1\"; path=\"/\"; domain=\"host1\"");
            cookies = parser.parse("host2", 80, "/", false, setCookie );
            try {
                parser.validate("host2", 80, "/", false, cookies[0]);
                fail("MalformedCookieException must have thrown");
            }
            catch(MalformedCookieException expected) {
            }
        }
        catch(HttpException e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.toString());
        }
    }

    /**
     * Makes sure that a cookie matches with a path of the same value.
     */
    public void testMatchWithEqualPaths() {
        
        Cookie cookie = new Cookie(".test.com", "test", "1", "/test", null, false);
        
        try {
            boolean match = cookieMatch(
                "test.test.com", 
                80, 
                "/test", 
                false,
                cookie
            );
            
            assertTrue("Cookie paths did not match", match);
        } catch ( Exception e ) {
            e.printStackTrace();
            fail("Unexpected exception: " + e);
        }
                   
    }


    /**
     * Tests generic cookie formatting.
     */
    
    public void testGenericCookieFormatting() {
        Header setCookie = new Header(
          "Set-Cookie", "name=value; path=/; domain=.mydomain.com");
        try {
            CookieSpec parser = CookiePolicy.getSpecByPolicy(CookiePolicy.COMPATIBILITY);
            Cookie[] cookies = parser.parse("myhost.mydomain.com", 80, "/", false, setCookie );
            parser.validate("myhost.mydomain.com", 80, "/", false, cookies[0]);
            String s = parser.formatCookie(cookies[0]);
            assertEquals("name=value", s);
        }
        catch(HttpException e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.toString());
        }
    }    

    /**
     * Tests Netscape specific cookie formatting.
     */
    
    public void testNetscapeCookieFormatting() {
        Header setCookie = new Header(
          "Set-Cookie", "name=value; path=/; domain=.mydomain.com");
        try {
            CookieSpec parser = CookiePolicy.getSpecByPolicy(CookiePolicy.NETSCAPE_DRAFT);
            Cookie[] cookies = parser.parse("myhost.mydomain.com", 80, "/", false, setCookie );
            parser.validate("myhost.mydomain.com", 80, "/", false, cookies[0]);
            String s = parser.formatCookie(cookies[0]);
            assertEquals("name=value", s);
        }
        catch(HttpException e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.toString());
        }
    }
    

    /**
     * Tests RFC 2109 compiant cookie formatting.
     */
    
    public void testRFC2109CookieFormatting() {
        CookieSpec parser = CookiePolicy.getSpecByPolicy(CookiePolicy.RFC2109);
        Header setCookie = null;
        Cookie[] cookies = null;
        try {
            setCookie = new Header(
            "Set-Cookie", "name=\"value\"; version=\"1\"; path=\"/\"; domain=\".mydomain.com\"");
            cookies = parser.parse("myhost.mydomain.com", 80, "/", false, setCookie );
            parser.validate("myhost.mydomain.com", 80, "/", false, cookies[0]);
            String s1 = parser.formatCookie(cookies[0]);
            assertEquals(s1, "$Version=\"1\"; name=\"value\"; $Domain=\".mydomain.com\"; $Path=\"/\"");

            setCookie = new Header(
            "Set-Cookie", "name=value; path=/; domain=.mydomain.com");
            cookies = parser.parse("myhost.mydomain.com", 80, "/", false, setCookie );
            parser.validate("myhost.mydomain.com", 80, "/", false, cookies[0]);
            String s2 = parser.formatCookie(cookies[0]);
            assertEquals(s2, "$Version=0; name=value; $Domain=.mydomain.com; $Path=/");
        }
        catch(HttpException e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.toString());
        }
    }


    /**
     * Tests Netscape specific expire attribute parsing.
     */
    
    public void testNetscapeCookieExpireAttribute() {
        CookieSpec parser = CookiePolicy.getSpecByPolicy(CookiePolicy.NETSCAPE_DRAFT);
        Header setCookie = null;
        Cookie[] cookies = null;
        try {
            setCookie = new Header(
              "Set-Cookie", "name=value; path=/; domain=.mydomain.com; expires=Thu, 01-Jan-2070 00:00:10 GMT; comment=no_comment");
            cookies = parser.parse("myhost.mydomain.com", 80, "/", false, setCookie );
            parser.validate("myhost.mydomain.com", 80, "/", false, cookies[0]);
        }
        catch(MalformedCookieException e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.toString());
        }
        try {
            setCookie = new Header(
              "Set-Cookie", "name=value; path=/; domain=.mydomain.com; expires=Thu 01-Jan-2070 00:00:10 GMT; comment=no_comment");
            cookies = parser.parse("myhost.mydomain.com", 80, "/", false, setCookie );
            parser.validate("myhost.mydomain.com", 80, "/", false, cookies[0]);
            fail("MalformedCookieException must have been thrown");
        }
        catch(MalformedCookieException e) {
            //expected
        }
    }
    

    /**
     * Tests if null cookie values are handled correctly.
     */
    public void testNullCookieValueFormatting() {
        Cookie cookie = new Cookie(".whatever.com", "name", null, "/", null, false); 
        cookie.setDomainAttributeSpecified(true);
        cookie.setPathAttributeSpecified(true);

        CookieSpec parser = null;
        String s = null;

        parser = CookiePolicy.getSpecByPolicy(CookiePolicy.COMPATIBILITY);
        s = parser.formatCookie(cookie);
        assertEquals("name=", s);

        parser = CookiePolicy.getSpecByPolicy(CookiePolicy.RFC2109);
        s = parser.formatCookie(cookie);
        assertEquals("$Version=0; name=; $Domain=.whatever.com; $Path=/", s);
    }
    
    /**
     * Tests if that invalid second domain level cookie gets 
     * rejected in the strict mode, but gets accepted in the
     * browser compatibility mode.
     */
    public void testSecondDomainLevelCookie() throws Exception {
        Cookie cookie = new Cookie(".sourceforge.net", "name", null, "/", null, false); 
        cookie.setDomainAttributeSpecified(true);
        cookie.setPathAttributeSpecified(true);

        CookieSpec parser = null;

        parser = CookiePolicy.getSpecByPolicy(CookiePolicy.COMPATIBILITY);
        parser.validate("sourceforge.net", 80, "/", false, cookie);

        parser = CookiePolicy.getSpecByPolicy(CookiePolicy.RFC2109);
        try {
            parser.validate("sourceforge.net", 80, "/", false, cookie);
            fail("MalformedCookieException should have been thrown");
        } catch (MalformedCookieException e) {
            // Expected
        }
    }
}

