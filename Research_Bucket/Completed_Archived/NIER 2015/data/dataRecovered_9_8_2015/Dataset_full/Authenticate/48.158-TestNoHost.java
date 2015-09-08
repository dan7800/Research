/*
 * $Header: /home/cvs/jakarta-commons/httpclient/src/test/org/apache/commons/httpclient/TestNoHost.java,v 1.22.2.2 2004/02/22 18:21:16 olegk Exp $
 * $Revision: 1.22.2.2 $
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
 * Tests that don't require any external host.
 * I.e., that run entirely within this JVM.
 *
 * (True unit tests, by some definitions.)
 *
 * @author Rodney Waldhoff
 * @author <a href="mailto:jsdever@apache.org">Jeff Dever</a>
 * @version $Revision: 1.22.2.2 $ $Date: 2004/02/22 18:21:16 $
 */
public class TestNoHost extends TestCase {

    public TestNoHost(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(TestHttpStatus.suite());
        suite.addTest(TestBase64.suite());
        suite.addTest(TestCookie.suite());
        suite.addTest(TestNVP.suite());
        suite.addTest(TestHeader.suite());
        suite.addTest(TestHeaderElement.suite());
        suite.addTest(TestChallengeParser.suite());
        suite.addTest(TestAuthenticator.suite());
        suite.addTest(TestHttpUrlMethod.suite());
        suite.addTest(TestURI.suite());
        suite.addTest(TestURIUtil.suite());
        suite.addTest(TestURIUtil2.suite());
        suite.addTest(TestMethodsNoHost.suite());
        suite.addTest(TestMethodsRedirectNoHost.suite());
        suite.addTest(TestHttpState.suite());
        suite.addTest(TestResponseHeaders.suite());
        suite.addTest(TestRequestHeaders.suite());
        suite.addTest(TestStreams.suite());
        suite.addTest(TestStatusLine.suite());
        suite.addTest(TestRequestLine.suite());
        suite.addTest(TestPartsNoHost.suite());
        suite.addTest(TestMethodCharEncoding.suite());
        suite.addTest(TestEquals.suite());
        return suite;
    }

    public static void main(String args[]) {
        String[] testCaseName = { TestNoHost.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

}
