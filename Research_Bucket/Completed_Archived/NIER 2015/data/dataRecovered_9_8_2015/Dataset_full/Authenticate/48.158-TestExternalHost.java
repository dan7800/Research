/*
 * $Header: /home/cvs/jakarta-commons/httpclient/src/test/org/apache/commons/httpclient/TestExternalHost.java,v 1.4.2.1 2004/02/22 18:21:16 olegk Exp $
 * $Revision: 1.4.2.1 $
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
 * A suite composed of only those tests which
 * require an external Internet connection.
 *
 * Optionally this test can be run using a proxy. If the proxy is not
 * authenticating do not set user / password.
 *
 * System properties:
 * httpclient.test.proxyHost - proxy host name
 * httpclient.test.proxyPort - proxy port
 * httpclient.test.proxyUser - proxy auth username
 * httpclient.test.proxyPass - proxy auth password
 *
 * @author Rodney Waldhoff
 * @author Ortwin Glück
 * @version $Id: TestExternalHost.java,v 1.4.2.1 2004/02/22 18:21:16 olegk Exp $
 */
public class TestExternalHost extends TestCase {

    public TestExternalHost(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(TestHttps.suite());
        suite.addTest(TestMethodsExternalHost.suite());
        return suite;
    }

    public static void main(String args[]) {
        String[] testCaseName = { TestExternalHost.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

}
