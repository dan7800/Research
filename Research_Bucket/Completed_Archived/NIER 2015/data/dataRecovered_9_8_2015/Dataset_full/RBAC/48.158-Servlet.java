// ========================================================================
// $Id: Servlet.java,v 1.4 2004/07/01 19:12:06 janb Exp $
// Copyright 2003-2004 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at 
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================

package org.mortbay.webapps.jaas;

import java.io.IOException;
import java.io.Writer;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.SecurityPermission;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

import javax.security.auth.Subject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jaas.JAASUserPrincipal;
import org.mortbay.util.Loader;




/* ---------------------------------------------------- */
/** Servlet
 * <p>Demo of using a JAAS realm to authenticate, and then
 * to authorize using a policy file.
 *
 * <p><h4>Notes</h4>
 * <p>Minimal servlet just for demo.
 *
 * <p><h4>Usage</h4>
 * Use ant run.jaas.demo in jetty/extra/plus/build.xml
 */
/*
 * </pre>
 *
 * @see
 * @version 1.0 Wed Apr 30 2003
 * @author Jan Bartel (janb)
 */
public class Servlet extends HttpServlet
{

    public String dbUrl;
    public String dbUserName;
    public String dbPassword;
    public String dbDriver;
    public Connection connection;
    

    
    public void doGet (HttpServletRequest request,
                       HttpServletResponse response) 
        throws ServletException, IOException
    {
	String action = request.getParameter ("action");
	if ((action != null) && (action.equalsIgnoreCase ("logout")))
	{
            request.getSession().invalidate();
            response.setContentType ("text/html");
            Writer writer = response.getWriter();
            writer.write ("<HTML><TITLE>JAAS Authentication and Authorization Test</TITLE>");
            writer.write ("<BODY><H1>Logout successfull</H1>");
            writer.write ("You should now not be able to access any pages that require authentication unless you log back in again.");
            writer.write ("<A HREF=\"/jaas/auth.html\">Try it</A>");
            writer.write ("</BODY></HTML>");

	}
	else
	{
            response.setContentType ("text/html");
            Writer writer = response.getWriter();
            writer.write ("<HTML><TITLE>JAAS Authentication and Authorization Test</TITLE>");
            
            //must have been authenticated or we wouldn't get here
            JAASUserPrincipal userPrincipal = (JAASUserPrincipal)request.getUserPrincipal();
            writer.write ("<BODY><H1> Congratulations "+userPrincipal.getName()+" you are AUTHENTICATED</H1>");
            
            
            
            try
            {
                Object o = Subject.doAsPrivileged(userPrincipal.getSubject(),
                                                  new PrivilegedExceptionAction ()
                                                  {
                                                      public Object run ()
                                                      throws Exception
                                                      {
                                                          AccessController.checkPermission (new SecurityPermission("mySecurityPermission"));
                                                          return new Boolean(true);
                                                      }
                                                      
                                                  },
                                                  null);
                
                
                //authorization success if we get here
                writer.write ("<H1>Congratulations "+userPrincipal.getName()+" you are AUTHORIZED</h1>");
                
            }
            catch (PrivilegedActionException e)
            {
                writer.write ("<H1> Commiserations "+ userPrincipal.getName()+" you are NOT AUTHORIZED</H1>");
                writer.write (e.toString());
                
            }
            catch (SecurityException e)
            {
                writer.write ("<H1> Commiserations "+ userPrincipal.getName()+" you are NOT AUTHORIZED</H1>");
                writer.write(e.toString());
            }

            writer.write ("<A HREF=\"/jaas/doit?action=logout\">Logout</A>");
            
            writer.write ("</BODY></HTML>");
	}

        
    }
    


    /* ------------------------------------------------ */
    /** Create user for test
     * @exception ServletException 
     */
    public void init ()
        throws ServletException
    {
        dbDriver = getServletConfig().getInitParameter("dbDriver");
        dbUrl = getServletConfig().getInitParameter("dbUrl");
        dbUserName = getServletConfig().getInitParameter("dbUserName");
        dbPassword = getServletConfig().getInitParameter("dbPassword");
        
        if ((dbDriver == null)
            ||
            (dbUrl == null))
            throw new ServletException ("Configure servlet init params; dbDriver, dbUrl, dbUserName, dbPassword");

        try
        {
            Loader.loadClass(this.getClass(), dbDriver).newInstance();

            //keep connection around, as using hypersonic in
            //in-memory mode requires that the connection stay
            //open for the data to persist
            connection = DriverManager.getConnection (dbUrl,
                                                      dbUserName,
                                                      dbPassword);
            
            connection.setAutoCommit(true);
            
            
            //create tables
            String sql = "create table myusers (myuser varchar(32) PRIMARY KEY, mypassword varchar(32))";
            Statement createStatement = connection.createStatement();
            createStatement.executeUpdate (sql);
            
            sql = " create table myuserroles (myuser varchar(32), myrole varchar(32))";
            createStatement.executeUpdate (sql);
            createStatement.close();
            
            //insert test users and roles
            sql = "insert into myusers (myuser, mypassword) values (?, ?)";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString (1, "me");
            statement.setString (2, "me");
            
            statement.executeUpdate();
            
            sql = "insert into myuserroles (myuser, myrole) values ( ? , ? )";
            statement = connection.prepareStatement (sql);
            statement.setString (1, "me");
            statement.setString (2, "roleA");
            statement.executeUpdate();
            
            statement.setString(1, "me");
            statement.setString(2, "roleB");
            statement.executeUpdate();
            
            statement.close();

        }
        catch (Exception e)
        {
            throw new ServletException(e);
        }
        
    }


    /* ------------------------------------------------ */
    /** Destroy servlet, drop tables.
     */
    public void destroy ()
    {
        try
        {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate ("drop table myusers");
            stmt.executeUpdate ("drop table myuserroles");
            
            stmt.close();
            connection.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
    }
    
    
}
