/*
 * $Header: /home/cvs/jakarta-commons/httpclient/src/test/org/apache/commons/httpclient/TestHttpState.java,v 1.3.2.1 2004/02/22 18:21:16 olegk Exp $
 * $Revision: 1.3.2.1 $
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

import junit.framework.*;

/**
 * 
 * Simple tests for {@link HttpState}.
 *
 * @author Rodney Waldhoff
 * @author <a href="mailto:jsdever@apache.org">Jeff Dever</a>
 * @author Sean C. Sullivan
 * 
 * @version $Id: TestHttpState.java,v 1.3.2.1 2004/02/22 18:21:16 olegk Exp $
 * 
 */
public class TestHttpState extends TestCase {

    public final Credentials creds1 = new UsernamePasswordCredentials("user1", "pass1");
    public final Credentials creds2 = new UsernamePasswordCredentials("user2", "pass2");

    public final String realm1 = "realm1";
    public final String realm2 = "realm2";


    // ------------------------------------------------------------ Constructor
    public TestHttpState(String testName) {
        super(testName);
    }

    // ------------------------------------------------------------------- Main
    public static void main(String args[]) {
        String[] testCaseName = { TestHttpState.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------- TestCase Methods

    public static Test suite() {
        return new TestSuite(TestHttpState.class);
    }


    // ----------------------------------------------------------- Test Methods

    public void testHttpStateCredentials() {
        HttpState state = new HttpState();
    state.setCredentials(realm1, creds1);
    state.setCredentials(realm2, creds2);
        assertEquals(creds1, state.getCredentials(realm1));
        assertEquals(creds2, state.getCredentials(realm2));
    }

    public void testToString()
    {
        HttpState state = new HttpState();
        assertNotNull(state.toString());
        
        state.addCookie(new Cookie("foo", "bar", "yeah"));
        assertNotNull(state.toString());

        state.addCookie(new Cookie("flub", "duck", "yuck"));
        assertNotNull(state.toString());

        state.setCredentials(realm1, creds1);
        assertNotNull(state.toString());
        
        state.setProxyCredentials(realm2, creds2);
        assertNotNull(state.toString());
    }

    public void testHttpStateNoCredentials() {
        HttpState state = new HttpState();
        assertEquals(null, state.getCredentials("bogus"));
    }

    public void testHttpStateDefaultCredentials() {
        HttpState state = new HttpState();
    state.setCredentials(null, creds1);
    state.setCredentials(realm2, creds2);
        assertEquals(creds1, state.getCredentials("bogus"));
    }


    public void testHttpStateProxyCredentials() {
        HttpState state = new HttpState();
    state.setProxyCredentials(realm1, creds1);
    state.setProxyCredentials(realm2, creds2);
        assertEquals(creds1, state.getProxyCredentials(realm1));
        assertEquals(creds2, state.getProxyCredentials(realm2));
    }

    public void testHttpStateProxyNoCredentials() {
        HttpState state = new HttpState();
        assertEquals(null, state.getProxyCredentials("bogus"));
    }

    public void testHttpStateProxyDefaultCredentials() {
        HttpState state = new HttpState();
    state.setProxyCredentials(null, creds1);
    state.setProxyCredentials(realm2, creds2);
        assertEquals(creds1, state.getProxyCredentials("bogus"));
    }

}
