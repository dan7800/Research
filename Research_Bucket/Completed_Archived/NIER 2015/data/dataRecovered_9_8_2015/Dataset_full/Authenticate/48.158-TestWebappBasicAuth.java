/*
 * $Header: /home/cvs/jakarta-commons/httpclient/src/test/org/apache/commons/httpclient/TestWebappBasicAuth.java,v 1.12.2.1 2004/02/22 18:21:16 olegk Exp $
 * $Revision: 1.12.2.1 $
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
import junit.framework.TestSuite;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;

/**
 * This suite of tests depends upon the httpclienttest webapp,
 * which is available in the httpclient/src/test-webapp
 * directory in the CVS tree.
 * <p>
 * The webapp should be deployed in the context "httpclienttest"
 * on a servlet engine running on port 8080 on the localhost
 * (IP 127.0.0.1).
 * <p>
 * You can change the assumed port by setting the
 * "httpclient.test.localPort" property.
 * You can change the assumed host by setting the
 * "httpclient.test.localHost" property.
 * You can change the assumed context by setting the
 * "httpclient.test.webappContext" property.
 *
 * @author Rodney Waldhoff
 * @version $Id: TestWebappBasicAuth.java,v 1.12.2.1 2004/02/22 18:21:16 olegk Exp $
 */
public class TestWebappBasicAuth extends TestWebappBase {

    public TestWebappBasicAuth(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(TestWebappBasicAuth.class);
        return suite;
    }

    public static void main(String args[]) {
        String[] testCaseName = { TestWebappBasicAuth.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------------------ Tests

    public void testSimpleAuthGet() throws Exception {
        HttpClient client = createHttpClient();
        client.getState().setCredentials("BasicAuthServlet",new UsernamePasswordCredentials("jakarta","commons"));
        GetMethod method = new GetMethod("/" + getWebappContext() + "/auth/basic");
        
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("<title>BasicAuth Servlet: GET</title>") >= 0);
        assertTrue(method.getResponseBodyAsString().indexOf("<p>You have authenticated as \"jakarta:commons\"</p>") >= 0);

        method.recycle();
        method.setPath("/" + getWebappContext() + "/auth/basic");
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("<title>BasicAuth Servlet: GET</title>") >= 0);
        assertTrue(method.getResponseBodyAsString().indexOf("<p>You have authenticated as \"jakarta:commons\"</p>") >= 0);
    }

    public void testSimpleAuthPost() throws Exception {
        HttpClient client = createHttpClient();
        client.getState().setCredentials("BasicAuthServlet",new UsernamePasswordCredentials("jakarta","commons"));
        PostMethod method = new PostMethod("/" + getWebappContext() + "/auth/basic");
        method.setRequestBody(new NameValuePair[] { new NameValuePair("testing","one") } );
        
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("<title>BasicAuth Servlet: POST</title>") >= 0);
        assertTrue(method.getResponseBodyAsString().indexOf("<p>You have authenticated as \"jakarta:commons\"</p>") >= 0);

        method.recycle();
        method.setPath("/" + getWebappContext() + "/auth/basic");
        method.setRequestBody(new NameValuePair[] { new NameValuePair("testing","one") } );
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("<title>BasicAuth Servlet: POST</title>") >= 0);
        assertTrue(method.getResponseBodyAsString().indexOf("<p>You have authenticated as \"jakarta:commons\"</p>") >= 0);
    }

    public void testSimpleAuthPut() throws Exception {
        HttpClient client = createHttpClient();
        client.getState().setCredentials("BasicAuthServlet",new UsernamePasswordCredentials("jakarta","commons"));
        PutMethod method = new PutMethod("/" + getWebappContext() + "/auth/basic");
        method.setRequestBody("testing one two three");
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("<title>BasicAuth Servlet: PUT</title>") >= 0);
        assertTrue(method.getResponseBodyAsString().indexOf("<p>You have authenticated as \"jakarta:commons\"</p>") >= 0);

        method.recycle();
        method.setPath("/" + getWebappContext() + "/auth/basic");
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("<title>BasicAuth Servlet: PUT</title>") >= 0);
        assertTrue(method.getResponseBodyAsString().indexOf("<p>You have authenticated as \"jakarta:commons\"</p>") >= 0);
    }

    public void testNoCredAuthRetry() throws Exception {
        HttpClient client = createHttpClient();
        GetMethod method = new GetMethod("/" + getWebappContext() + "/auth/basic");
        
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(401,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("<title>BasicAuth Servlet: GET</title>") >= 0);
        assertTrue(method.getResponseBodyAsString().indexOf("<p>Not authorized.</p>") >= 0);

        client.getState().setCredentials("BasicAuthServlet",new UsernamePasswordCredentials("jakarta","commons"));

        method.recycle();
        method.setPath("/" + getWebappContext() + "/auth/basic");
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("<title>BasicAuth Servlet: GET</title>") >= 0);
        assertTrue(method.getResponseBodyAsString().indexOf("<p>You have authenticated as \"jakarta:commons\"</p>") >= 0);
    }

    public void testBadCredFails() throws Exception {
        HttpClient client = createHttpClient();
        GetMethod method = new GetMethod("/" + getWebappContext() + "/auth/basic");
        
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(HttpStatus.SC_UNAUTHORIZED,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("<title>BasicAuth Servlet: GET</title>") >= 0);
        assertTrue(method.getResponseBodyAsString().indexOf("<p>Not authorized.</p>") >= 0);

        client.getState().setCredentials("BasicAuthServlet",new UsernamePasswordCredentials("bad","creds"));

        method.recycle();
        method.setPath("/" + getWebappContext() + "/auth/basic");
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(HttpStatus.SC_UNAUTHORIZED,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("<title>BasicAuth Servlet: GET</title>") >= 0);
        assertTrue(method.getResponseBodyAsString().indexOf("<p>Not authorized. \"Basic YmFkOmNyZWRz\" not recognized.</p>") >= 0);
    }
    
    public void testHeadAuth() throws Exception {
        HttpClient client = new HttpClient();
        HttpState state = client.getState();
        Credentials cred = new UsernamePasswordCredentials("jakarta", "commons");
        state.setCredentials(null, cred);
        HostConfiguration hc = new HostConfiguration();
        hc.setHost(getHost(), getPort(), getProtocol());
        client.setHostConfiguration(hc);
        client.setState(state);
        HeadMethod method = new HeadMethod("/"+ getWebappContext() +"/auth/basic");
        client.executeMethod(method);
        method.releaseConnection();
        assertEquals(200, method.getStatusCode());
    }
    
}




