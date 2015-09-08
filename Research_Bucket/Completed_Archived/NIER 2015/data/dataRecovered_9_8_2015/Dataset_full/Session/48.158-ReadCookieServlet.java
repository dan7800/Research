/*
 * $Header: /home/cvs/jakarta-commons/httpclient/src/test-webapp/src/org/apache/commons/httpclient/ReadCookieServlet.java,v 1.4.2.2 2004/02/22 18:21:18 olegk Exp $
 * $Revision: 1.4.2.2 $
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
import javax.servlet.http.Cookie;
import java.util.*;

public class ReadCookieServlet extends MultiMethodServlet {
    protected void genericService(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head><title>ReadCookieServlet: " + request.getMethod() + "</title></head>");
        out.println("<body>");
        out.println("<p>This is a response to an HTTP " + request.getMethod() + " request.</p>");
        Enumeration enumeration = request.getHeaders("Cookie");
        while (enumeration.hasMoreElements()) {
            out.println("<p><tt>Cookie: " + (String)enumeration.nextElement() + "</tt></p>");
        }
        Cookie[] cookies = request.getCookies();
        if(null != cookies) {
            for(int i=0;i<cookies.length;i++) {
                out.println("<tt>" + cookies[i].getName() + "=" + cookies[i].getValue() + "</tt><br>");
            }
        }
        out.println("</body>");
        out.println("</html>");
    }
}

