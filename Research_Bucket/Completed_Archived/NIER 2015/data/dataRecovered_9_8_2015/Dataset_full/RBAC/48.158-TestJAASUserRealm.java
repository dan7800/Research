// ========================================================================
// $Id: TestJAASUserRealm.java,v 1.4 2005/01/20 22:49:37 janb Exp $
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

package org.mortbay.jaas;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.security.AccessController;
import java.security.Principal;
import java.security.SecurityPermission;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.mortbay.jaas.callback.DefaultCallbackHandler;
import org.mortbay.http.HttpRequest;
import org.mortbay.util.Loader;


/* ---------------------------------------------------- */
/** TestJAASUserRealm
 * <p> Test JAAS in Jetty - relies on the JDBCUserRealm.
 *
 * <p><h4>Notes</h4>
 * <p>
 *
 * <p><h4>Usage</h4>
 * <pre>
 */
/*
 * </pre>
 *
 * @see
 * @version 1.0 Mon Apr 28 2003
 * @author Jan Bartel (janb)
 */
public class TestJAASUserRealm extends TestCase

{
    public Connection connection = null;
    
    public TestJAASUserRealm(String name)
    {
        super (name);
    }

    public static Test suite()
    {
        return new TestSuite(TestJAASUserRealm.class);
    }


    public void setUp ()
	throws Exception
    {
        //get a connection
        //NOTE: we don't close this connection because if we are
        //using Hypersonic in memory, if we close the first
        //connection we will close the entire database!
        Loader.loadClass(this.getClass(), System.getProperty("dbDriver")).newInstance();

        connection = DriverManager.getConnection (System.getProperty("dbUrl"),
                                                  System.getProperty("dbUserName"),
                                                  System.getProperty("dbPassword",""));

        connection.setAutoCommit(true);
    }

    public void testIt ()
        throws Exception
    {

       
        
        //set up config
        File configFile = File.createTempFile ("loginConf", null);
        PrintWriter writer = new PrintWriter(new FileWriter(configFile));
        writer.println ("jdbc {");
        writer.println ("org.mortbay.jaas.spi.JDBCLoginModule required");       
        writer.println ("debug=\"true\"");
        writer.println ("dbUrl=\""+System.getProperty("dbUrl")+"\"");
        writer.println ("dbUserName=\""+System.getProperty("dbUserName")+"\"");
        if ((System.getProperty("dbPassword") != null) && (!System.getProperty("dbPassword").equals("")))
            writer.println ("dbPassword=\""+System.getProperty("dbPassword")+"\"");
        writer.println ("dbDriver=\""+System.getProperty("dbDriver")+"\"");
        writer.println ("userTable=\"myusers\"");
        writer.println ("userField=\"myuser\"");
        writer.println ("credentialField=\"mypassword\"");
        writer.println ("userRoleTable=\"myuserroles\"");
        writer.println ("userRoleUserField=\"myuser\"");
        writer.println ("userRoleRoleField=\"myrole\";");
        writer.println ("};");
        writer.flush();
        writer.close();
        
        BufferedReader reader = new BufferedReader(new FileReader(configFile));
        String s = "";
        for (s = reader.readLine(); (s != null); s = reader.readLine())
        {
            System.out.println (s);
        }
        
        
        //create a login module config file
        System.setProperty ("java.security.auth.login.config", configFile.toURL().toExternalForm());

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
        //connection.close();
        
        
        //create a JAASUserRealm
        JAASUserRealm realm = new JAASUserRealm ("testRealm");

        realm.setLoginModuleName ("jdbc");
        realm.setCallbackHandlerClass ("org.mortbay.jaas.callback.DefaultCallbackHandler");
        realm.setRoleCheckPolicy (new StrictRoleCheckPolicy());
        

        JAASUserPrincipal userPrincipal = (JAASUserPrincipal)realm.authenticate ("me", "blah",(HttpRequest)null);
        assertNull (userPrincipal);
        
        userPrincipal = (JAASUserPrincipal)realm.authenticate ("me", "me", (HttpRequest)null);

        assertNotNull (userPrincipal);
        assertNotNull (userPrincipal.getName());
        assertTrue (userPrincipal.getName().equals("me"));

        assertTrue (userPrincipal.isUserInRole("roleA"));
        assertTrue (userPrincipal.isUserInRole("roleB"));
        assertTrue (!userPrincipal.isUserInRole("roleC"));

        realm.pushRole (userPrincipal, "roleC");
        assertTrue (userPrincipal.isUserInRole("roleC"));
        assertTrue (!userPrincipal.isUserInRole("roleA"));
        assertTrue (!userPrincipal.isUserInRole("roleB"));

        realm.pushRole (userPrincipal, "roleD");
        assertTrue (userPrincipal.isUserInRole("roleD"));
        assertTrue (!userPrincipal.isUserInRole("roleC"));
        assertTrue (!userPrincipal.isUserInRole("roleA"));
        assertTrue (!userPrincipal.isUserInRole("roleB"));

        realm.popRole(userPrincipal);
        assertTrue (userPrincipal.isUserInRole("roleC"));
        assertTrue (!userPrincipal.isUserInRole("roleA"));
        assertTrue (!userPrincipal.isUserInRole("roleB"));
        
        realm.popRole(userPrincipal);
        assertTrue (!userPrincipal.isUserInRole("roleC"));
        assertTrue (userPrincipal.isUserInRole("roleA"));
        assertTrue (userPrincipal.isUserInRole("roleB"));

        realm.popRole (userPrincipal);
        assertTrue (!userPrincipal.isUserInRole("roleC"));
        assertTrue (userPrincipal.isUserInRole("roleA"));
        assertTrue (userPrincipal.isUserInRole("roleB"));        

        //execute as privileged user
        System.out.println (((JAASUserPrincipal)userPrincipal).getSubject());
        

        Object o =
            javax.security.auth.Subject.doAsPrivileged
            (((JAASUserPrincipal)userPrincipal).getSubject(),
             new java.security.PrivilegedExceptionAction ()
             {
                 public Object run ()
                     throws Exception
                 {
                     AccessController.checkPermission (new SecurityPermission("mySecurityPermission"));
                     return new Boolean(true);
                 }
             },
             null);
        

        assertTrue (((Boolean)o).booleanValue());
        

        realm.disassociate (userPrincipal);
        
    }

    public void tearDown ()
        throws Exception
    {
        Statement stmt = connection.createStatement();
        stmt.executeUpdate ("drop table myusers");
        stmt.executeUpdate ("drop table myuserroles");
        
        stmt.close();
        connection.close();
    }
    
    
}
