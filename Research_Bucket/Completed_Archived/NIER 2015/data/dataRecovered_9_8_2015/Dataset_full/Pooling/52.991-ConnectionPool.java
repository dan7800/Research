/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/server/persistence/rdb/ConnectionPool.java,v 1.6 2003/10/31 20:17:07 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.persistence.rdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/*
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/server/persistence/rdb/ConnectionPool.java,v 1.6 2003/10/31 20:17:07 farrukh_najmi Exp $
 *
*/
class ConnectionPool {
    private Log log = LogFactory.getLog(this.getClass());
    private String name;
    private String URL;
    private String user;
    private String password;
    private int initConns;
    private int maxConns;
    private int timeOut;
    private SQLException sqlException;
    private int checkedOut;
    private Vector freeConnections = new Vector();

    /**
    @param timeOut is the time in seconds after this has been elasped but the
    connection cannot be returned, getConnection() will return SQLException
    */
    public ConnectionPool(String name, String URL, String user,
        String password, int maxConns, int initConns, int timeOut) {
        this.name = name;
        this.URL = URL;
        this.user = user;
        this.password = password;
        this.initConns = initConns;
        this.maxConns = maxConns;
        this.timeOut = (timeOut > 0) ? timeOut : 5;

        // initialise the pool
        for (int i = 0; i < initConns; i++) {
            try {
                Connection pc = newConnection();
                freeConnections.addElement(pc);
            } catch (SQLException e) {
                sqlException = e;
            }
        }

        System.err.println("Database connection pooling enabled");
        System.err.println(getStats());
    }

    public Connection getConnection() throws SQLException {
        try {
            if ((maxConns < initConns) || (maxConns <= 0)) {
                throw new SQLException(
                    "Invalid initial or maximum size of connection pool");
            }

            if ((freeConnections.size() == 0) && (initConns != 0)) {
                // for some reasons the pool cannot be initialised
                throw sqlException;
            }

            Connection conn = getConnection(timeOut * 1000);

            return conn;
        } catch (SQLException e) {
            throw e;
        }
    }

    private synchronized Connection getConnection(long timeout)
        throws SQLException {
        // Get a pooled Connection from the cache or a new one.
        // Wait if all are checked out and the max limit has
        // been reached.
        long startTime = System.currentTimeMillis();
        long remaining = timeout;
        Connection conn = null;

        while ((conn = getPooledConnection()) == null) {
            try {
                wait(remaining);
            } catch (InterruptedException e) {
            }

            remaining = timeout - (System.currentTimeMillis() - startTime);

            if (remaining <= 0) {
                // Timeout has expired
                throw new SQLException("Database connection timed-out");
            }
        }

        // Check if the Connection is still OK
        if (!isConnectionOK(conn)) {
            // It was bad. Try again with the remaining timeout
            return getConnection(remaining);
        }

        checkedOut++;

        return conn;
    }

    private boolean isConnectionOK(Connection connection) {
        Statement testStmt = null;

        try {
            if (!connection.isClosed()) {
                // Try to createStatement to see if it's really alive
                testStmt = connection.createStatement();
                testStmt.close();
            } else {
                return false;
            }
        } catch (SQLException e) {
            if (testStmt != null) {
                try {
                    testStmt.close();
                } catch (SQLException se) {
                }
            }

            return false;
        }

        return true;
    }

    private Connection getPooledConnection() throws SQLException {
        Connection conn = null;

        if (freeConnections.size() > 0) {
            // Pick the first Connection in the Vector
            // to get round-robin usage
            conn = (Connection) freeConnections.firstElement();
            freeConnections.removeElementAt(0);
        } else if (checkedOut < maxConns) {
            conn = newConnection();
        }

        return conn;
    }

    private Connection newConnection() throws SQLException {
        Connection conn = null;

        if (user == null) {
            conn = DriverManager.getConnection(URL);
        } else {
            conn = DriverManager.getConnection(URL, user, password);
        }

        return conn;
    }

    public synchronized void freeConnection(Connection conn)
        throws SQLException {
        if ((freeConnections.size() == 0) && (checkedOut == 0)) {
            // for some reasons the pool cannot be initialised
            throw sqlException;
        }

        // Put the connection at the end of the Vector
        freeConnections.addElement(conn);
        checkedOut--;
        notifyAll();
    }

    /**
    Close all connections in the pool
    */
    public synchronized void release() throws SQLException {
        if ((freeConnections.size() == 0) && (checkedOut == 0)) {
            // for some reasons the pool cannot be initialised
            throw sqlException;
        }

        Enumeration allConnections = freeConnections.elements();

        while (allConnections.hasMoreElements()) {
            Connection con = (Connection) allConnections.nextElement();
            con.close();
        }

        freeConnections.removeAllElements();
    }

    private String getStats() {
        return "Total connections: " + (freeConnections.size() + checkedOut) +
        " Available: " + freeConnections.size() + " Checked-out: " +
        checkedOut;
    }
}
package com.sun.ebxml.registry.persistence.rdb;

import java.sql.*;
import java.util.*;

import java.io.*;

/*
 * $Header: /cvsroot/sino/ebxmlrr/src/share/com/sun/ebxml/registry/persistence/rdb/ConnectionPool.java,v 1.3 2002/11/09 04:47:08 jasilva Exp $
 *
*/

public class ConnectionPool
{

   private String name;
   private String URL;
   private String user;
   private String password;
   private int initConns;
   private int maxConns;
   private int timeOut;
   private SQLException sqlException; 
   private int checkedOut;
   private Vector freeConnections = new Vector();

   /**
   @param timeOut is the time in seconds after this has been elasped but the 
   connection cannot be returned, getConnection() will return SQLException
   */
   public ConnectionPool(String name, String URL, String user, 
      String password, int maxConns, int initConns, int timeOut) 
   {
        this.name = name;
        this.URL = URL;
        this.user = user;
        this.password = password;
        this.initConns = initConns;
        this.maxConns = maxConns;
        this.timeOut = timeOut > 0 ? timeOut : 5;
        
        // initialise the pool
        for (int i = 0; i < initConns; i++)
        {
            try {
                Connection pc = newConnection();
                freeConnections.addElement(pc);
            }
            catch (SQLException e) {
                sqlException = e;
            } 
        }
        System.err.println("Database connection pooling enabled");
        System.err.println(getStats());
   }

   public Connection getConnection() throws SQLException {
      try
      {
          if (maxConns < initConns || maxConns <=0) {
              throw new SQLException ("Invalid initial or maximum size of connection pool");                      
          }
          if (freeConnections.size()==0 && initConns != 0) {
              // for some reasons the pool cannot be initialised
              throw sqlException;
          }
          
          Connection conn = getConnection(timeOut * 1000);
         return conn;
      }
      catch (SQLException e)
      {
         throw e;
      }
   }

   private synchronized Connection getConnection(long timeout) 
                      throws SQLException
   {
      // Get a pooled Connection from the cache or a new one.
      // Wait if all are checked out and the max limit has
      // been reached.
      long startTime = System.currentTimeMillis();
      long remaining = timeout;
      Connection conn = null;
      while ((conn = getPooledConnection()) == null)
      {
         try
         {
            wait(remaining);
         }
         catch (InterruptedException e)
         { }
         remaining = timeout - (System.currentTimeMillis() - startTime);
         if (remaining <= 0)
         {
            // Timeout has expired
            throw new SQLException("Database connection timed-out");
         }
      }

      // Check if the Connection is still OK
      if (!isConnectionOK(conn))
      {
         // It was bad. Try again with the remaining timeout
         return getConnection(remaining);
      }
      checkedOut++;
      return conn;
   }

   private boolean isConnectionOK(Connection conn)
   {
      Statement testStmt = null;
      try
      {
         if (!conn.isClosed())
         {
            // Try to createStatement to see if it's really alive
            testStmt = conn.createStatement();
            testStmt.close();
         }
         else
         {
            return false;
         }
      }
      catch (SQLException e)
      {
         if (testStmt != null)
         {
            try
            {
               testStmt.close();
            }
            catch (SQLException se)
            { }
         }
         return false;
      }
      return true;
   }

   private Connection getPooledConnection() throws SQLException
   {
      Connection conn = null;
      if (freeConnections.size() > 0)
      {
         // Pick the first Connection in the Vector
         // to get round-robin usage
         conn = (Connection) freeConnections.firstElement();
         freeConnections.removeElementAt(0);
      }
      else if (checkedOut < maxConns)
      {
         conn = newConnection();
      }
      return conn;
   }

   private Connection newConnection() throws SQLException
   {
      Connection conn = null;
      if (user == null) {
         conn = DriverManager.getConnection(URL);
      }
      else {
         conn = DriverManager.getConnection(URL, user, password);
      }
      return conn;
   }

   public synchronized void freeConnection(Connection conn) throws SQLException
   {
      if (freeConnections.size()==0 && checkedOut == 0) {
        // for some reasons the pool cannot be initialised
        throw sqlException;
      }
      // Put the connection at the end of the Vector
      freeConnections.addElement(conn);
      checkedOut--;
      notifyAll();
   }

   /**
   Close all connections in the pool
   */
   public synchronized void release() throws SQLException
   {
      if (freeConnections.size()==0 && checkedOut == 0) {
            // for some reasons the pool cannot be initialised
            throw sqlException;
      }
      Enumeration allConnections = freeConnections.elements();
      while (allConnections.hasMoreElements())
      {
         Connection con = (Connection) allConnections.nextElement();
         con.close();
      }
      freeConnections.removeAllElements();
   }

   private String getStats() {
      return "Total connections: " + 
         (freeConnections.size() + checkedOut) +
         " Available: " + freeConnections.size() +
         " Checked-out: " + checkedOut;
   }
}
