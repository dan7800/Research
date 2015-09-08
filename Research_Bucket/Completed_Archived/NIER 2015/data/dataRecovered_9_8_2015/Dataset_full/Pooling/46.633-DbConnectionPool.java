package com.vlee.beans.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.vlee.beans.util.ResourcePool;
import com.vlee.beans.util.ResourceException;

public class DbConnectionPool extends ResourcePool 
{
   private final String driver, url, user, pwd;
   private final int initialConnections = 5;
   private boolean driverLoaded = false;

   public DbConnectionPool(String driver, String url) 
   {
      this(driver, url, null, null);
   }

   public DbConnectionPool(String driver, String url, 
                           String user, String pwd) 
   {
      this.driver = driver;
      this.url    = url;
      this.user   = user;
      this.pwd    = pwd;

      try 
      {
         for(int i=0; i < initialConnections; ++i) 
            availableResources.addElement(createResource());
      }
      catch(Exception ex) {}
   }

   public Object createResource() 
	throws ResourceException 
   {
      Connection connection = null;
      try 
         {
         if(!driverLoaded) 
            {
            Class.forName(driver);
            driverLoaded = true;
            }
         if(user == null || pwd == null)
            connection = DriverManager.getConnection(url);
         else
            connection = DriverManager.getConnection(url, user, 
                                                          pwd);
         }
      catch(Exception ex) 
         {
         // ex is ClassNotFoundException or SQLException
         throw new ResourceException(ex.getMessage());
         }
      return connection;
   }



   public void closeResource(Object connection) 
   {
      try 
      {
         ((Connection)connection).close();
      }
      catch(SQLException ex) 
      {
         // ignore exception
      }
   }

   public boolean isResourceValid(Object object) 
   {
      Connection connection = (Connection)object;      
      boolean valid = false;

      try 
      {
         valid = ! connection.isClosed();
      }
      catch(SQLException ex) 
      {
         valid = false;
      }
      return valid;
   }
}
