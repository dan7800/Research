/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicemix.common.security;

import java.security.GeneralSecurityException;

import javax.security.auth.Subject;

import org.apache.servicemix.jbi.security.UserPrincipal;
import junit.framework.TestCase;

public class AuthenticationServiceTest extends TestCase {

    public void test() throws Exception {
        AuthenticationService svc = AuthenticationService.Proxy.create(new Svc());
        Subject s = new Subject();
        svc.authenticate(s, null, "user", null);
        assertEquals(1, s.getPrincipals().size());
    }

    public static class Svc {
        public void authenticate(Subject subject, String domain, String user, Object credentials) throws GeneralSecurityException {
            subject.getPrincipals().add(new UserPrincipal(user));
        }
    }
}
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicemix.common.security;

import java.security.GeneralSecurityException;

import javax.security.auth.Subject;

import org.apache.servicemix.jbi.security.UserPrincipal;
import junit.framework.TestCase;

public class AuthenticationServiceTest extends TestCase {

    public void test() throws Exception {
        AuthenticationService svc = AuthenticationService.Proxy.create(new Svc());
        Subject s = new Subject();
        svc.authenticate(s, null, "user", null);
        assertEquals(1, s.getPrincipals().size());
    }

    public static class Svc {
        public void authenticate(Subject subject, String domain, String user, Object credentials) throws GeneralSecurityException {
            subject.getPrincipals().add(new UserPrincipal(user));
        }
    }
}