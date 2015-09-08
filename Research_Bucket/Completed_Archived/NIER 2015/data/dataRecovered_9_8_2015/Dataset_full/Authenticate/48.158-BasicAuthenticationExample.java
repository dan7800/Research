/*
 * $Header: /home/cvs/jakarta-commons/httpclient/src/examples/BasicAuthenticationExample.java,v 1.1.2.3 2004/02/22 18:21:12 olegk Exp $
 * $Revision: 1.1.2.3 $
 * $Date: 2004/02/22 18:21:12 $
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
 import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 * A simple example that uses HttpClient to perform a GET using Basic
 * Authentication. Can be run standalone without parameters.
 *
 * You need to have JSSE on your classpath for JDK prior to 1.4
 *
 * @author Michael Becke
 */
public class BasicAuthenticationExample {

    /**
     * Constructor for BasicAuthenticatonExample.
     */
    public BasicAuthenticationExample() {
    }

    public static void main(String[] args) throws Exception {
        HttpClient client = new HttpClient();

        // pass our credentials to HttpClient, they will only be used for
        // authenticating to servers with realm "realm", to authenticate agains
        // an arbitrary realm change this to null.
        client.getState().setCredentials(
            "realm",
            "www.verisign.com",
            new UsernamePasswordCredentials("username", "password")
        );

        // create a GET method that reads a file over HTTPS, we're assuming
        // that this file requires basic authentication using the realm above.
        GetMethod get = new GetMethod("https://www.verisign.com/products/index.html");

        // Tell the GET method to automatically handle authentication. The
        // method will use any appropriate credentials to handle basic
        // authentication requests.  Setting this value to false will cause
        // any request for authentication to return with a status of 401.
        // It will then be up to the client to handle the authentication.
        get.setDoAuthentication( true );

        // execute the GET
        int status = client.executeMethod( get );

        // print the status and response
        System.out.println(status + "\n" + get.getResponseBodyAsString());

        // release any connection resources used by the method
        get.releaseConnection();
    }
}
