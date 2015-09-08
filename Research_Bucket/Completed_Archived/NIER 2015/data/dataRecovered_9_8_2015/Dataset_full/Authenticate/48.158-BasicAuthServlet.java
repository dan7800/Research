/*
 * $Header: /home/cvs/jakarta-commons/httpclient/src/test-webapp/src/org/apache/commons/httpclient/BasicAuthServlet.java,v 1.4.2.1 2004/02/22 18:21:18 olegk Exp $
 * $Revision: 1.4.2.1 $
 * $Date: 2004/02/22 18:21:18 $
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

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;

public class BasicAuthServlet extends MultiMethodServlet {
    // rather then make this servlet depend upon a base64 impl,
    // we'll just hard code some base64 encodings
    private static final HashMap creds = new HashMap();
    static {
        creds.put("dW5hbWU6cGFzc3dk","uname:passwd");
        creds.put("amFrYXJ0YTpjb21tb25z","jakarta:commons");
        creds.put("amFrYXJ0YS5hcGFjaGUub3JnL2NvbW1vbnM6aHR0cGNsaWVudA==","jakarta.apache.org/commons:httpclient");
    }

    protected void genericService(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String auth = request.getHeader("authorization");
        if(null == auth) {
            sendUnauthenticated(request, response);
        } else {
            String role = (String)(creds.get(auth.substring("basic ".length(),auth.length())));
            if(null == role) {
                sendUnauthorized(request, response, auth);
            } else {
                sendAuthorized(request, response, role);
            }
        }
    }

    protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String auth = request.getHeader("authorization");
        if(null == auth) {
            sendUnauthenticated(request, response);
        } else {
            String role = (String)(creds.get(auth.substring("basic ".length(),auth.length())));
            if(null == role) {
                sendUnauthorized(request, response, auth);
            }
        }
    }

    private void sendUnauthenticated(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(response.SC_UNAUTHORIZED);
        response.addHeader("www-authenticate","Basic realm=\"BasicAuthServlet\"");
        PrintWriter out = response.getWriter();
        response.setContentType("text/html");
        out.println("<html>");
        out.println("<head><title>BasicAuth Servlet: " + request.getMethod() + "</title></head>");
        out.println("<body>");
        out.println("<p>This is a response to an HTTP " + request.getMethod() + " request.</p>");
        out.println("<p>Not authorized.</p>");
        out.println("</body>");
        out.println("</html>");
    }

    private void sendUnauthorized(HttpServletRequest request, HttpServletResponse response, String auth) throws IOException {
        response.setStatus(response.SC_UNAUTHORIZED);
        response.addHeader("www-authenticate","Basic realm=\"BasicAuthServlet\"");
        PrintWriter out = response.getWriter();
        response.setContentType("text/html");
        out.println("<html>");
        out.println("<head><title>BasicAuth Servlet: " + request.getMethod() + "</title></head>");
        out.println("<body>");
        out.println("<p>This is a response to an HTTP " + request.getMethod() + " request.</p>");
        out.println("<p>Not authorized. \"" + auth + "\" not recognized.</p>");
        out.println("</body>");
        out.println("</html>");
    }

    private void sendAuthorized(HttpServletRequest request, HttpServletResponse response, String role) throws IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head><title>BasicAuth Servlet: " + request.getMethod() + "</title></head>");
        out.println("<body>");
        out.println("<p>This is a response to an HTTP " + request.getMethod() + " request.</p>");
        out.println("<p>You have authenticated as \"" + role + "\"</p>");
        out.println("</body>");
        out.println("</html>");
    }
}

