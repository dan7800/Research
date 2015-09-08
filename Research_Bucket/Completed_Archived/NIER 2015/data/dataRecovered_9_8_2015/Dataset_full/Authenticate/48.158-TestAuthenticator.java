/*
 * $Header: /home/cvs/jakarta-commons/httpclient/src/test/org/apache/commons/httpclient/TestAuthenticator.java,v 1.25.2.6 2004/02/22 18:21:16 olegk Exp $
 * $Revision: 1.25.2.6 $
 * $Date: 2004/02/22 18:21:16 $
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

import java.util.Hashtable;
import java.util.StringTokenizer;
import org.apache.commons.httpclient.auth.*;
import org.apache.commons.httpclient.util.Base64;

/**
 * Unit tests for {@link Authenticator}.
 *
 * @author Rodney Waldhoff
 * @author <a href="mailto:jsdever@apache.org">Jeff Dever</a>
 * @version $Id: TestAuthenticator.java,v 1.25.2.6 2004/02/22 18:21:16 olegk Exp $
 */
public class TestAuthenticator extends TestCase {

    // ------------------------------------------------------------ Constructor
    public TestAuthenticator(String testName) {
        super(testName);
    }

    // ------------------------------------------------------------------- Main
    public static void main(String args[]) {
        String[] testCaseName = { TestAuthenticator.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------- Utility Methods

    private void checkAuthorization(UsernamePasswordCredentials cred, String methodName, String auth) throws Exception {
        Hashtable table = new Hashtable();
        StringTokenizer tokenizer = new StringTokenizer(auth, ",=\"");
        while(tokenizer.hasMoreTokens()){
            String key = null;
            String value = null;
            if(tokenizer.hasMoreTokens())
                key = tokenizer.nextToken();
            if(tokenizer.hasMoreTokens()) {
                value = tokenizer.nextToken();
                assertFalse("Value of "+key+" was \"null\"", "null".equals(value));
            }
            if(key != null && value != null){
                table.put(key.trim(),value.trim());
            }
        }
        String response = (String) table.get("response");
        table.put( "methodname", methodName );
        //System.out.println(auth);
        String digest = DigestScheme.createDigest(cred.getUserName(),cred.getPassword(), table);
        assertEquals(response, digest);
    }


    // ------------------------------------------------------- TestCase Methods

    public static Test suite() {
        return new TestSuite(TestAuthenticator.class);
    }


    // ---------------------------------- Test Methods for BasicScheme Authentication

    public void testBasicAuthenticationWithNoCreds() {
        String challenge = "Basic realm=\"realm1\"";
        HttpState state = new HttpState();
        HttpMethod method = new SimpleHttpMethod(new Header("WWW-Authenticate", challenge));
        try {
            AuthScheme authscheme = new BasicScheme(challenge);
            HttpAuthenticator.authenticate(authscheme, method, null, state);
            fail("Should have thrown HttpException");
        } catch(HttpException e) {
            // expected
        }
    }

    public void testBasicAuthenticationWithNoRealm() {
        String challenge = "Basic";
        HttpState state = new HttpState();
        HttpMethod method = new SimpleHttpMethod(new Header("WWW-Authenticate", challenge));
        try {
            AuthScheme authscheme = new BasicScheme(challenge);
            HttpAuthenticator.authenticate(authscheme, method, null, state);
            fail("Should have thrown HttpException");
        } catch(HttpException e) {
            // expected
        }
    }

    public void testBasicAuthenticationWithNoRealm2() {
        String challenge = "Basic ";
        HttpState state = new HttpState();
        HttpMethod method = new SimpleHttpMethod(new Header("WWW-Authenticate", challenge));
        try {
            AuthScheme authscheme = new BasicScheme(challenge);
            HttpAuthenticator.authenticate(authscheme, method, null, state);
            fail("Should have thrown HttpException");
        } catch(HttpException e) {
            // expected
        }
    }

    public void testBasicAuthenticationWithNullHttpState() throws Exception {
        String challenge = "Basic realm=\"realm1\"";
        HttpMethod method = new SimpleHttpMethod(new Header("WWW-Authenticate", challenge));
        try {
            AuthScheme authscheme = new BasicScheme(challenge);
            HttpAuthenticator.authenticate(authscheme, method, null, null);
            fail("Should have thrown IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            // expected
        }
    }

    public void testInvalidAuthenticationScheme() throws Exception {
        String challenge = "invalid realm=\"realm1\"";
        try{
            HttpAuthenticator.selectAuthScheme(
              new Header[] { new Header("WWW-Authenticate", challenge) });
            fail("Should have thrown UnsupportedOperationException");
        }catch (UnsupportedOperationException uoe){
            // expected
        }
    }

    public void testBasicAuthenticationCaseInsensitivity() throws Exception {
        String challenge = "bAsIc ReAlM=\"realm1\"";
        HttpState state = new HttpState();
        state.setCredentials(null, null, new UsernamePasswordCredentials("username","password"));
        HttpMethod method = new SimpleHttpMethod(new Header("WwW-AuThEnTiCaTe", challenge));
        AuthScheme authscheme = new BasicScheme(challenge);
        assertTrue(HttpAuthenticator.authenticate(authscheme, method, null, state));
        assertTrue(null != method.getRequestHeader("Authorization"));
        String expected = "Basic " + HttpConstants.getString(Base64.encode(HttpConstants.getBytes("username:password")));
        assertEquals(expected,method.getRequestHeader("Authorization").getValue());
    }


    public void testBasicAuthenticationWithDefaultCreds() throws Exception {
        HttpState state = new HttpState();
        state.setCredentials(null, null, new UsernamePasswordCredentials("username","password"));
        HttpMethod method = new SimpleHttpMethod(new Header("WWW-Authenticate","Basic realm=\"realm1\""));
        assertTrue(HttpAuthenticator.authenticateDefault(method, null, state));
        assertTrue(null != method.getRequestHeader("Authorization"));
        String expected = "Basic " + HttpConstants.getString(Base64.encode(HttpConstants.getBytes("username:password")));
        assertEquals(expected,method.getRequestHeader("Authorization").getValue());
    }

    public void testBasicAuthentication() throws Exception {
        String challenge = "Basic realm=\"realm\"";
        HttpState state = new HttpState();
        state.setCredentials("realm", null, new UsernamePasswordCredentials("username","password"));
        HttpMethod method = new SimpleHttpMethod(new Header("WWW-Authenticate", challenge));
        AuthScheme authscheme = new BasicScheme(challenge);
        assertTrue(HttpAuthenticator.authenticate(authscheme, method, null, state));
        assertTrue(null != method.getRequestHeader("Authorization"));
        String expected = "Basic " + HttpConstants.getString(Base64.encode(HttpConstants.getBytes("username:password")));
        assertEquals(expected,method.getRequestHeader("Authorization").getValue());
    }
    
    public void testBasicAuthenticationWith88591Chars() throws Exception {
        int[] germanChars = { 0xE4, 0x2D, 0xF6, 0x2D, 0xFc };
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < germanChars.length; i++) {
            buffer.append((char)germanChars[i]); 
        }
        
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("dh", buffer.toString());
        assertEquals("Basic ZGg65C32Lfw=", BasicScheme.authenticate(credentials));
    }
    
    public void testBasicAuthenticationWithMutlipleRealms() throws Exception {
        String challenge1 = "Basic realm=\"realm1\"";
        String challenge2 = "Basic realm=\"realm2\"";
        HttpState state = new HttpState();
        state.setCredentials("realm1", null, new UsernamePasswordCredentials("username","password"));
        state.setCredentials("realm2", null, new UsernamePasswordCredentials("uname2","password2"));
        AuthScheme authscheme1 = new BasicScheme(challenge1);
        AuthScheme authscheme2 = new BasicScheme(challenge2);
        {
            HttpMethod method = new SimpleHttpMethod(new Header("WWW-Authenticate",challenge1));
            assertTrue(HttpAuthenticator.authenticate(authscheme1, method, null, state));
            assertTrue(null != method.getRequestHeader("Authorization"));
            String expected = "Basic " + HttpConstants.getString(Base64.encode(HttpConstants.getBytes("username:password")));
            assertEquals(expected,method.getRequestHeader("Authorization").getValue());
        }
        {
            HttpMethod method = new SimpleHttpMethod(new Header("WWW-Authenticate", challenge2));
            assertTrue(HttpAuthenticator.authenticate(authscheme2, method, null, state));
            assertTrue(null != method.getRequestHeader("Authorization"));
            String expected = "Basic " + HttpConstants.getString(Base64.encode(HttpConstants.getBytes("uname2:password2")));
            assertEquals(expected,method.getRequestHeader("Authorization").getValue());
        }
    }

    public void testPreemptiveAuthorizationTrueNoCreds() throws Exception {
        HttpState state = new HttpState();
        HttpMethod method = new SimpleHttpMethod();

        state.setAuthenticationPreemptive(true);
        assertTrue(!HttpAuthenticator.authenticateDefault(method, null, state));
        assertTrue(null == method.getRequestHeader("Authorization"));
    }

    public void testPreemptiveAuthorizationTrueWithCreds() throws Exception {
        HttpState state = new HttpState();
        HttpMethod method = new SimpleHttpMethod();
        state.setCredentials(null, null, new UsernamePasswordCredentials("username","password"));

        state.setAuthenticationPreemptive(true);
        assertTrue(HttpAuthenticator.authenticateDefault(method, null, state));
        assertTrue(null != method.getRequestHeader("Authorization"));
        String expected = "Basic " + HttpConstants.getString(Base64.encode(HttpConstants.getBytes("username:password")));
        assertEquals(expected, method.getRequestHeader("Authorization").getValue());
    }

    // --------------------------------- Test Methods for DigestScheme Authentication

    public void testDigestAuthenticationWithNoCreds() {
        String challenge = "Digest realm=\"realm1\", nonce=\"ABC123\"";
        HttpState state = new HttpState();
        HttpMethod method = new SimpleHttpMethod(new Header("WWW-Authenticate", challenge));
        try {
            AuthScheme authscheme = new DigestScheme(challenge);
            HttpAuthenticator.authenticate(authscheme, method, null, state);
            fail("Should have thrown HttpException");
        } catch(HttpException e) {
            // expected
        }
    }

    public void testDigestAuthenticationWithNoRealm() {
        String challenge = "Digest";
        try {
            AuthScheme authscheme = new DigestScheme(challenge);
            authscheme.hashCode(); //quiet Eclipse compiler
            fail("Should have thrown HttpException");
        } catch(MalformedChallengeException e) {
            // expected
        }
    }

    public void testDigestAuthenticationWithNoRealm2() {
        String challenge = "Digest ";
        try {
            AuthScheme authscheme = new DigestScheme(challenge);
            authscheme.hashCode(); //quiet Eclipse compiler
            fail("Should have thrown HttpException");
        } catch(MalformedChallengeException e) {
            // expected
        }
    }

    public void testDigestAuthenticationWithNullHttpState() throws Exception {
        String challenge = "Digest realm=\"realm1\", nonce=\"ABC123\"";
        HttpMethod method = new SimpleHttpMethod(new Header("WWW-Authenticate", challenge));
        try {
            AuthScheme authscheme = new DigestScheme(challenge);
            HttpAuthenticator.authenticate(authscheme, method, null, null);
            fail("Should have thrown IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            // expected
        }
    }

    public void testDigestAuthenticationCaseInsensitivity() throws Exception {
        String challenge = "dIgEsT ReAlM=\"realm1\", nONce=\"ABC123\"";
        HttpState state = new HttpState();
        UsernamePasswordCredentials cred = new UsernamePasswordCredentials("username","password");
        state.setCredentials(null, null, cred);
        HttpMethod method = new SimpleHttpMethod(new Header("WwW-AuThEnTiCaTe", challenge));
        AuthScheme authscheme = new DigestScheme(challenge);
        assertTrue(HttpAuthenticator.authenticate(authscheme, method, null, state));
        assertTrue(null != method.getRequestHeader("Authorization"));
    }


    public void testDigestAuthenticationWithDefaultCreds() throws Exception {
        String challenge = "Digest realm=\"realm1\", nonce=\"ABC123\"";
        HttpState state = new HttpState();
        UsernamePasswordCredentials cred = new UsernamePasswordCredentials("username","password");
        state.setCredentials(null, null, cred);
        HttpMethod method = new SimpleHttpMethod(new Header("WWW-Authenticate", challenge));
        AuthScheme authscheme = new DigestScheme(challenge);
        assertTrue(HttpAuthenticator.authenticate(authscheme, method, null, state));
        assertTrue(null != method.getRequestHeader("Authorization"));
        checkAuthorization(cred, method.getName(), method.getRequestHeader("Authorization").getValue());
    }

    public void testDigestAuthentication() throws Exception {
        String challenge = "Digest realm=\"realm1\", nonce=\"ABC123\"";
        HttpState state = new HttpState();
        UsernamePasswordCredentials cred = new UsernamePasswordCredentials("username","password");
        state.setCredentials(null, null, cred);
        HttpMethod method = new SimpleHttpMethod(new Header("WWW-Authenticate", challenge));
        AuthScheme authscheme = new DigestScheme(challenge);
        assertTrue(HttpAuthenticator.authenticate(authscheme, method, null, state));
        assertTrue(null != method.getRequestHeader("Authorization"));
        checkAuthorization(cred, method.getName(), method.getRequestHeader("Authorization").getValue());
    }

    public void testDigestAuthenticationWithStaleNonce() throws Exception {
        
        String headers =
            "HTTP/1.1 401 OK\r\n" +
            "Connection: close\r\n" +
            "Content-Length: 0\r\n" +
            "WWW-Authenticate: Digest realm=\"realm1\", nonce=\"ABC123\"\r\n";
        String headers2 =
            "HTTP/1.1 401 OK\r\n" +
            "Connection: close\r\n" +
            "Content-Length: 0\r\n" +
            "WWW-Authenticate: Digest realm=\"realm1\", nonce=\"321CBA\", stale=\"true\"\r\n";
        String headers3 = 
            "HTTP/1.1 200 OK\r\n" +
            "Connection: close\r\n" +
            "Server: HttpClient Test/2.0\r\n\r\n" +
            "stuff\r\n";
        
        SimpleHttpConnection conn = new SimpleHttpConnection();
        
        conn.addResponse(headers);
        conn.addResponse(headers2);
        conn.addResponse(headers3);
        HttpState state = new HttpState();
        UsernamePasswordCredentials cred = new UsernamePasswordCredentials("username","password");
        state.setCredentials(null, null, cred);

        SimpleHttpMethod method = new SimpleHttpMethod();
        method.setDoAuthentication(true);
        assertEquals("Authentication failed", 200, method.execute(state, conn));
        checkAuthorization(cred, method.getName(), method.getRequestHeader("Authorization").getValue());
    }

    public void testDigestAuthenticationWithMultipleRealms() throws Exception {
        String challenge1 = "Digest realm=\"realm1\", nonce=\"ABC123\"";
        String challenge2 = "Digest realm=\"realm2\", nonce=\"ABC123\"";
        HttpState state = new HttpState();
        UsernamePasswordCredentials cred = new UsernamePasswordCredentials("username","password");
        state.setCredentials("realm1", null, cred);
        UsernamePasswordCredentials cred2 = new UsernamePasswordCredentials("uname2","password2");
        state.setCredentials("realm2", null, cred2);
        AuthScheme authscheme1 = new DigestScheme(challenge1);
        AuthScheme authscheme2 = new DigestScheme(challenge2);
        {
            HttpMethod method = new SimpleHttpMethod(new Header("WWW-Authenticate",challenge1));
            assertTrue(HttpAuthenticator.authenticate(authscheme1, method, null, state));
            assertTrue(null != method.getRequestHeader("Authorization"));
            checkAuthorization(cred, method.getName(), method.getRequestHeader("Authorization").getValue());
        }
        {
            HttpMethod method = new SimpleHttpMethod(new Header("WWW-Authenticate",challenge2));
            assertTrue(HttpAuthenticator.authenticate(authscheme2, method, null, state));
            assertTrue(null != method.getRequestHeader("Authorization"));
            checkAuthorization(cred2, method.getName(), method.getRequestHeader("Authorization").getValue());
        }
    }

    /** 
     * Test digest authentication using the MD5-sess algorithm.
     */
    public void testDigestAuthenticationMD5Sess() throws Exception {
        // Example using Digest auth with MD5-sess

        String realm="realm";
        String username="username";
        String password="password";
        String nonce="e273f1776275974f1a120d8b92c5b3cb";

        String challenge="Digest realm=\"" + realm + "\", "
            + "nonce=\"" + nonce + "\", "
            + "opaque=\"SomeString\", "
            + "stale=false, "
            + "algorithm=MD5-sess, "
            + "qop=\"auth\"";

        HttpState state = new HttpState();
        UsernamePasswordCredentials cred =
            new UsernamePasswordCredentials(username, password);
        state.setCredentials(realm, null, cred);
        AuthScheme authscheme = new DigestScheme(challenge);
        HttpMethod method =
            new SimpleHttpMethod(new Header("WWW-Authenticate", challenge));
        assertTrue(HttpAuthenticator.authenticate(
            authscheme, method, null, state));
        assertTrue(null != method.getRequestHeader("Authorization"));
        checkAuthorization(cred, method.getName(),
            method.getRequestHeader("Authorization").getValue());
    }

    // --------------------------------- Test Methods for NTLM Authentication

    public void testNTLMAuthenticationWithNoCreds() {
        String challenge = "NTLM";
        HttpState state = new HttpState();
        HttpMethod method = new SimpleHttpMethod(new Header("WWW-Authenticate", challenge));
        method.addRequestHeader("Host", "host");
        try {
            AuthScheme authscheme = new NTLMScheme(challenge);
            HttpAuthenticator.authenticate(authscheme, method, null, state);
            fail("Should have thrown HttpException");
        } catch(HttpException e) {
            // expected
        }
    }

    public void testNTLMAuthenticationWithNullHttpState() throws Exception {
        String challenge = "NTLM";
        HttpMethod method = new SimpleHttpMethod(new Header("WWW-Authenticate", challenge));
        method.addRequestHeader("Host", "host");
        try {
            AuthScheme authscheme = new NTLMScheme(challenge);
            HttpAuthenticator.authenticate(authscheme, method, null, null);
            fail("Should have thrown IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            // expected
        }
    }

    public void testNTLMAuthenticationCaseInsensitivity() throws Exception {
        String challenge = "nTlM";
        HttpState state = new HttpState();
        NTCredentials cred = new NTCredentials("username","password", "host",
                "domain");
        state.setCredentials(null, null, cred);
        HttpMethod method = new SimpleHttpMethod(new Header("WwW-AuThEnTiCaTe", challenge));
        AuthScheme authscheme = new NTLMScheme(challenge);
        assertTrue(HttpAuthenticator.authenticate(authscheme, method, null, state));
        assertTrue(null != method.getRequestHeader("Authorization"));
    }

    public void testNTLMAuthenticationResponse1() throws Exception {
        String challenge = "NTLM";
        String expected = "NTLM TlRMTVNTUAABAAAABlIAAAYABgAkAAAABAAEACAAAABIT" +
            "1NURE9NQUlO";
        HttpState state = new HttpState();
        NTCredentials cred = new NTCredentials("username","password", "host",
                "domain");
        state.setCredentials(null, null, cred);
        HttpMethod method = new SimpleHttpMethod(new Header("WWW-Authenticate", challenge));
        AuthScheme authscheme = new NTLMScheme(challenge);
        assertTrue(HttpAuthenticator.authenticate(authscheme, method, null, state));
        assertTrue(null != method.getRequestHeader("Authorization"));
        assertEquals(expected,
                method.getRequestHeader("Authorization").getValue());
    }
    
    public void testNTLMAuthenticationResponse2() throws Exception {
        String challenge = 
            "NTLM TlRMTVNTUAACAAAACgAKADAAAAAGgoEAPc4kP4LtCV8AAAAAAAAAAJ4AngA" +
            "6AAAASU5UUkFFUEhPWAIAFABJAE4AVABSAEEARQBQAEgATwBYAAEAEgBCAE8AQQB" +
            "SAEQAUgBPAE8ATQAEACgAaQBuAHQAcgBhAGUAcABoAG8AeAAuAGUAcABoAG8AeAA" +
            "uAGMAbwBtAAMAPABCAG8AYQByAGQAcgBvAG8AbQAuAGkAbgB0AHIAYQBlAHAAaAB" +
            "vAHgALgBlAHAAaABvAHgALgBjAG8AbQAAAAAA";

        String expected = "NTLM TlRMTVNTUAADAAAAGAAYAFIAAAAAAAAAagAAAAYABgB" +
            "AAAAACAAIAEYAAAAEAAQATgAAAAAAAABqAAAABlIAAERPTUFJTlVTRVJOQU1FSE" +
            "9TVAaC+vLxUEHnUtpItj9Dp4kzwQfd61Lztg==";
        HttpState state = new HttpState();
        NTCredentials cred = new NTCredentials("username","password", "host",
                "domain");
        state.setCredentials(null, null, cred);
        HttpMethod method = new SimpleHttpMethod(new Header("WWW-Authenticate", challenge));
        AuthScheme authscheme = new NTLMScheme(challenge);
        assertTrue(HttpAuthenticator.authenticate(authscheme, method, null, state));
        assertTrue(null != method.getRequestHeader("Authorization"));
        assertEquals(expected,
                method.getRequestHeader("Authorization").getValue());
    }
    
    public void testNTLMAuthenticationWithDefaultCreds() throws Exception {
        String challenge = 
            "NTLM TlRMTVNTUAACAAAACgAKADAAAAAGgoEAPc4kP4LtCV8AAAAAAAAAAJ4AngA" +
            "6AAAASU5UUkFFUEhPWAIAFABJAE4AVABSAEEARQBQAEgATwBYAAEAEgBCAE8AQQB" +
            "SAEQAUgBPAE8ATQAEACgAaQBuAHQAcgBhAGUAcABoAG8AeAAuAGUAcABoAG8AeAA" +
            "uAGMAbwBtAAMAPABCAG8AYQByAGQAcgBvAG8AbQAuAGkAbgB0AHIAYQBlAHAAaAB" +
            "vAHgALgBlAHAAaABvAHgALgBjAG8AbQAAAAAA";
        String expected = "NTLM TlRMTVNTUAADAAAAGAAYAFIAAAAAAAAAagAAAAYABgB" +
            "AAAAACAAIAEYAAAAEAAQATgAAAAAAAABqAAAABlIAAERPTUFJTlVTRVJOQU1FSE" +
            "9TVAaC+vLxUEHnUtpItj9Dp4kzwQfd61Lztg==";
        HttpState state = new HttpState();
        NTCredentials cred = new NTCredentials("username","password", "host",
                "domain");
        state.setCredentials(null, null, cred);
        HttpMethod method = new SimpleHttpMethod(new Header("WWW-Authenticate", challenge));
        method.addRequestHeader("Host", "host");
        AuthScheme authscheme = new NTLMScheme(challenge);
        assertTrue(HttpAuthenticator.authenticate(authscheme, method, null, state));
        assertTrue(null != method.getRequestHeader("Authorization"));
        assertEquals(expected,
                method.getRequestHeader("Authorization").getValue());
    }
    
    public void testNTLMAuthenticationRetry() throws Exception {
        NTCredentials cred = new NTCredentials("username", "password", "host", "domain");
        HttpState state = new HttpState();
        state.setCredentials(null, null, cred);
        HttpMethod method = new SimpleHttpMethod();
        SimpleHttpConnection conn = new SimpleHttpConnection();
        conn.addResponse(
            "HTTP/1.1 401 Unauthorized\r\n" +
            "WWW-Authenticate: NTLM\r\n" +
            "Connection: close\r\n" +
            "Server: HttpClient Test/2.0\r\n");
        conn.addResponse(
            "HTTP/1.1 401 Unauthorized\r\n" +
            "WWW-Authenticate: NTLM TlRMTVNTUAACAAAAAAAAACgAAAABggAAU3J2Tm9uY2UAAAAAAAAAAA==\r\n" +
            "Connection: close\r\n" +
            "Server: HttpClient Test/2.0\r\n");
        conn.addResponse(
            "HTTP/1.1 200 OK\r\n" +
            "Connection: close\r\n" +
            "Server: HttpClient Test/2.0\r\n\r\n" +
            "stuff\r\n");
        method.execute(state, conn);
        assertNull(method.getResponseHeader("WWW-Authenticate"));
        assertEquals(200, method.getStatusCode());
    }

    /** 
     * Test that the Unauthorized response is returned when doAuthentication is false.
     */
    public void testDoAuthenticateFalse() throws Exception {
        HttpState state = new HttpState();
        state.setCredentials(null, "Protected",
                new UsernamePasswordCredentials("name", "pass"));
        HttpMethod method = new SimpleHttpMethod();
        method.setDoAuthentication(false);
        SimpleHttpConnection conn = new SimpleHttpConnection();
        conn.addResponse(
            "HTTP/1.1 401 Unauthorized\r\n" + 
            "WWW-Authenticate: Basic realm=\"Protected\"\r\n" +
            "Connection: close\r\n" +
            "Server: HttpClient Test/2.0\r\n");
        conn.addResponse( 
            "HTTP/1.1 200 OK\r\n" +
            "Connection: close\r\n" +
            "Server: HttpClient Test/2.0\r\n"); 
        method.execute(state, conn);
        assertNotNull(method.getResponseHeader("WWW-Authenticate"));
        assertNull(method.getRequestHeader("Authorization"));
        assertEquals(401, method.getStatusCode());

    }


    /** 
     */
    public void testInvalidCredentials() throws Exception {
        HttpState state = new HttpState();
        state.setCredentials(null, "Protected", new UsernamePasswordCredentials("name", "pass"));
        HttpMethod method = new SimpleHttpMethod();
        method.setDoAuthentication(false);
        SimpleHttpConnection conn = new SimpleHttpConnection();
        conn.addResponse(
            "HTTP/1.1 401 Unauthorized\r\n" + 
            "WWW-Authenticate: Basic realm=\"Protected\"\r\n" +
            "Connection: close\r\n" +
            "Server: HttpClient Test/2.0\r\n"
                );
        method.execute(state, conn);
        assertEquals(401, method.getStatusCode());
    }


    // --------------------------------- Test Methods for Multiple Authentication

    public void testMultipleChallengeBasic() throws Exception {
        HttpState state = new HttpState();
        state.setCredentials("Protected", null, new UsernamePasswordCredentials("name", "pass"));
        HttpMethod method = new SimpleHttpMethod();
        SimpleHttpConnection conn = new SimpleHttpConnection();
        conn.addResponse(
            "HTTP/1.1 401 Unauthorized\r\n" + 
            "WWW-Authenticate: Unsupported\r\n" +
            "WWW-Authenticate: Basic realm=\"Protected\"\r\n" +
            "Connection: close\r\n" +
            "Server: HttpClient Test/2.0\r\n"
                );
        conn.addResponse( 
            "HTTP/1.1 200 OK\r\n" +
            "Connection: close\r\n" +
            "Server: HttpClient Test/2.0\r\n"
                ); 
        method.execute(state, conn);
        Header authHeader = method.getRequestHeader("Authorization");
        assertNotNull(authHeader);

        String authValue = authHeader.getValue();
        assertTrue(authValue.startsWith("Basic"));
    }

    public void testMultipleChallengeBasicLongRealm() throws Exception {
        HttpState state = new HttpState();
        state.setCredentials(null, null, new UsernamePasswordCredentials("name", "pass"));
        HttpMethod method = new SimpleHttpMethod();
        SimpleHttpConnection conn = new SimpleHttpConnection();
        conn.addResponse(
            "HTTP/1.1 401 Unauthorized\r\n" + 
            "WWW-Authenticate: Unsupported\r\n" +
            "WWW-Authenticate: Basic realm=\"This site is protected.  We put this message into the realm string, against all reasonable rationale, so that users would see it in the authentication dialog generated by your browser.\"\r\n" +
            "Connection: close\r\n" +
            "Server: HttpClient Test/2.0\r\n"
                );
        conn.addResponse( 
            "HTTP/1.1 200 OK\r\n" +
            "Connection: close\r\n" +
            "Server: HttpClient Test/2.0\r\n"
                ); 
        method.execute(state, conn);
        Header authHeader = method.getRequestHeader("Authorization");
        assertNotNull(authHeader);

        String authValue = authHeader.getValue();
        assertTrue(authValue.startsWith("Basic"));
    }




    public void testMultipleChallengeDigest() throws Exception {
        HttpState state = new HttpState();
        state.setCredentials("Protected", null, new UsernamePasswordCredentials("name", "pass"));
        HttpMethod method = new SimpleHttpMethod();
        SimpleHttpConnection conn = new SimpleHttpConnection();
        conn.addResponse(
            "HTTP/1.1 401 Unauthorized\r\n" + 
            "WWW-Authenticate: Unsupported\r\n" +
            "WWW-Authenticate: Digest realm=\"Protected\", nonce=\"ABC123\"\r\n" +
            "WWW-Authenticate: Basic realm=\"Protected\"\r\n" +
            "Connection: close\r\n" +
            "Server: HttpClient Test/2.0\r\n"
                );
        conn.addResponse( 
            "HTTP/1.1 200 OK\r\n" +
            "Connection: close\r\n" +
            "Server: HttpClient Test/2.0\r\n"
                ); 
        method.execute(state, conn);
        Header authHeader = method.getRequestHeader("Authorization");
        assertNotNull(authHeader);

        String authValue = authHeader.getValue();
        assertTrue(authValue.startsWith("Digest"));
    }


    public void testMultipleProxyChallengeBasic() throws Exception {
        HttpState state = new HttpState();
        state.setProxyCredentials("Protected", new UsernamePasswordCredentials("name", "pass"));
        HttpMethod method = new SimpleHttpMethod();
        SimpleHttpConnection conn = new SimpleHttpConnection();
        conn.addResponse(
            "HTTP/1.1 407 Proxy Authentication Required\r\n" + 
            "Proxy-Authenticate: Basic realm=\"Protected\"\r\n" +
            "Proxy-Authenticate: Unsupported\r\n" +
            "Connection: close\r\n" +
            "Server: HttpClient Test/2.0\r\n"
                );
        conn.addResponse( 
            "HTTP/1.1 200 OK\r\n" +
            "Connection: close\r\n" +
            "Server: HttpClient Test/2.0\r\n"
                ); 
        method.execute(state, conn);
        Header authHeader = method.getRequestHeader("Proxy-Authorization");
        assertNotNull(authHeader);

        String authValue = authHeader.getValue();
        assertTrue(authValue.startsWith("Basic"));
    }


    public void testMultipleProxyChallengeDigest() throws Exception {
        HttpState state = new HttpState();
        state.setProxyCredentials("Protected", new UsernamePasswordCredentials("name", "pass"));
        HttpMethod method = new SimpleHttpMethod();
        SimpleHttpConnection conn = new SimpleHttpConnection();
        conn.addResponse(
            "HTTP/1.1 407 Proxy Authentication Required\r\n" + 
            "Proxy-Authenticate: Basic realm=\"Protected\"\r\n" +
            "Proxy-Authenticate: Digest realm=\"Protected\", nonce=\"ABC123\"\r\n" +
            "Proxy-Authenticate: Unsupported\r\n" +
            "Connection: close\r\n" +
            "Server: HttpClient Test/2.0\r\n"
                );
        conn.addResponse( 
            "HTTP/1.1 200 OK\r\n" +
            "Connection: close\r\n" +
            "Server: HttpClient Test/2.0\r\n"
                ); 
        method.execute(state, conn);
        Header authHeader = method.getRequestHeader("Proxy-Authorization");
        assertNotNull(authHeader);

        String authValue = authHeader.getValue();
        assertTrue(authValue.startsWith("Digest"));
    }


    // --------------------------------- Test Methods for Selecting Credentials
    
    public void testDefaultCredentials() throws Exception {
        HttpState state = new HttpState();
        Credentials expected = new UsernamePasswordCredentials("name", "pass");
        state.setCredentials(null, null, expected);
        Credentials got = state.getCredentials("realm", "host");
        assertEquals(got, expected);
    }
    
    public void testRealmCredentials() throws Exception {
        HttpState state = new HttpState();
        Credentials expected = new UsernamePasswordCredentials("name", "pass");
        state.setCredentials("realm", null, expected);
        Credentials got = state.getCredentials("realm", "host");
        assertEquals(expected, got);
    }
    
    public void testHostCredentials() throws Exception {
        HttpState state = new HttpState();
        Credentials expected = new UsernamePasswordCredentials("name", "pass");
        state.setCredentials(null, "host", expected);
        Credentials got = state.getCredentials("realm", "host");
        assertEquals(expected, got);
    }
    
    public void testBothCredentials() throws Exception {
        HttpState state = new HttpState();
        Credentials expected = new UsernamePasswordCredentials("name", "pass");
        state.setCredentials("realm", "host", expected);
        Credentials got = state.getCredentials("realm", "host");
        assertEquals(expected, got);
    }
    
    public void testWrongHostCredentials() throws Exception {
        HttpState state = new HttpState();
        Credentials expected = new UsernamePasswordCredentials("name", "pass");
        state.setCredentials(null, "host1", expected);
        Credentials got = state.getCredentials("realm", "host2");
        assertNotSame(expected, got);
    }
    
    public void testWrongRealmCredentials() throws Exception {
        HttpState state = new HttpState();
        Credentials cred = new UsernamePasswordCredentials("name", "pass");
        state.setCredentials("realm1", "host", cred);
        Credentials got = state.getCredentials("realm2", "host");
        assertNotSame(cred, got);
    }
    
    public void testRealmSpoof() throws Exception {
        HttpState state = new HttpState();
        Credentials cred = new UsernamePasswordCredentials("name", "pass");
        state.setCredentials(null, "admin.apache.org", cred);
        Credentials got = state.getCredentials("admin.apache.org", "myhost");
        assertNotSame(cred, got);
    }
    
    public void testRealmSpoof2() throws Exception {
        HttpState state = new HttpState();
        Credentials cred = new UsernamePasswordCredentials("name", "pass");
        state.setCredentials(null, "whatever", cred);
        Credentials got = state.getCredentials("nullwhatever", null);
        assertNotSame(cred, got);
    }
}
