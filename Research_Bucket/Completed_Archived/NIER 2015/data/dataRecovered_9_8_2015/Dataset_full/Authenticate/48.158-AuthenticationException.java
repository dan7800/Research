/*
 * $Header: /home/cvs/jakarta-commons/httpclient/src/java/org/apache/commons/httpclient/auth/AuthenticationException.java,v 1.2.2.1 2004/02/22 18:21:14 olegk Exp $
 * $Revision: 1.2.2.1 $
 * $Date: 2004/02/22 18:21:14 $
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

package org.apache.commons.httpclient.auth;

import org.apache.commons.httpclient.HttpException;

/**
 * Signals a failure in authentication process
 *
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * 
 * @since 2.0
 */
public class AuthenticationException extends HttpException {

    /**
     * @see HttpException#HttpException()
     */
    public AuthenticationException() {
        super();
    }

    /**
     * @see HttpException#HttpException(String)
     */
    public AuthenticationException(String message) {
        super(message);
    }
}
