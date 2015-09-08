/*
 * Copyright (c) 2003 - 2009 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: PooledDatabaseConnectionFactoryImpl.java,v 1.1 2009/04/22 05:05:00 bastafidli Exp $
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 2 of the License. 
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA 
 */

package org.opensubsystems.core.persist.db.connectionpool.impl;

import java.sql.Connection;

import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.persist.db.Database;
import org.opensubsystems.core.persist.db.impl.DatabaseConnectionFactoryImpl;
import org.opensubsystems.core.persist.db.impl.DatabaseTransactionFactoryImpl;

/**
 * Base class for implementation of factories for retrieving and returning of 
 * database connections, which are maintained in a pool of always ready 
 * connections. 
 *  
 * @version $Id: PooledDatabaseConnectionFactoryImpl.java,v 1.1 2009/04/22 05:05:00 bastafidli Exp $
 * @author Miro Halas
 * @code.reviewer Miro Halas
 * @code.reviewed 1.11 2008/06/26 04:14:33 bastafidli
 */
public abstract class PooledDatabaseConnectionFactoryImpl extends DatabaseConnectionFactoryImpl
{
   /**
    * Simple structure collecting all information about the connection pool that 
    * is defined for a specific data source in order to improve the efficiency
    * of connecting to the database. Connection pool corresponds to a pool of 
    * connections.
    */
   protected class ConnectionPoolDefinition extends DatabaseConnectionDefinition
   {
      /**
       * The connection pool however it is implemented. 
       */
      protected Object m_connectionPool;

      /**
       * @param strDataSourceName - name of the data source
       * @param database - database for this this data source is being created 
       * @param strDriverName - JDBC driver user by this data source
       * @param strUrl - URL to connect to the database
       * @param strUser - User name to use to connect to the database
       * @param strPassword - Password to use to connect to the database
       * @param connectionPool - connection pool however it is implemented
       * @param iTransactionIsolation - transaction isolation that should be 
       *                                used for connections
       */
      public ConnectionPoolDefinition(
         String   strDataSourceName,
         Database database,
         String   strDriverName,
         String   strUrl,
         String   strUser,
         String   strPassword,
         int      iTransactionIsolation,
         Object connectionPool
      )
      {
         super(strDataSourceName, database, strDriverName, strUrl, strUser, 
               strPassword, iTransactionIsolation);
         
         m_connectionPool = connectionPool;
      }
      
      /**
       * @return Object
       */
      public Object getConnectionPool()
      {
         return m_connectionPool;
      }
     
      /**
       * {@inheritDoc}
       */
      public String toString()
      {
         StringBuffer dump = new StringBuffer();
         
         dump.append("ConnectionPoolDefinition[");
         dump.append("\n   ");
         dump.append(super.toString());
         dump.append("\n   m_connectionPool = ");
         if (m_connectionPool != null)
         {
            dump.append(m_connectionPool.toString());
         }
         else
         {
            dump.append("null");            
         }
         dump.append("]");
         
         return dump.toString();
      }      
   }

   // Constants ////////////////////////////////////////////////////////////////
   
   // Attributes ///////////////////////////////////////////////////////////////
   
   // Cached values ////////////////////////////////////////////////////////////

   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Default constructor.
    */
   public PooledDatabaseConnectionFactoryImpl(
   )
   {
      this(null);
   }

   /**
    * Default constructor.
    *  
    * @param transactionFactory - transaction factory to use for this 
    *                             connection factory, can be null
    */
   public PooledDatabaseConnectionFactoryImpl(
      DatabaseTransactionFactoryImpl transactionFactory
   )
   {
      super(transactionFactory);
   }

   // Logic ////////////////////////////////////////////////////////////////////

   // Helper methods ///////////////////////////////////////////////////////////
   
   /**
    * {@inheritDoc} 
    */
   protected final DatabaseConnectionDefinition createDataSource(
      String   strDataSourceName,
      Database database,
      String   strDriverName,
      String   strUrl,
      String   strUser,
      String   strPassword,
      int      iTransactionIsolation
   ) throws OSSException
   {
      // Data source with this name doesn't exists yet
      Object connectionPool;
      
      connectionPool = createConnectionPool(strDataSourceName, database,
                                            strDriverName, strUrl, strUser, 
                                            strPassword, iTransactionIsolation);
      
      return new ConnectionPoolDefinition(strDataSourceName, database,
                                          strDriverName, strUrl, strUser, 
                                          strPassword, iTransactionIsolation, 
                                          connectionPool);
   }

   /**
    * {@inheritDoc} 
    */ 
   protected final void destroyDataSource(
      DatabaseConnectionDefinition dataSource
   ) throws OSSException
   {
      ConnectionPoolDefinition connectionPool;
         
      connectionPool = (ConnectionPoolDefinition)dataSource;
      destroyConnectionPool(connectionPool);
      connectionPool.m_connectionPool = null;

      // Now let the base class to the cleanup
      super.destroyDataSource(dataSource);
   }
   
   /**
    * {@inheritDoc}
    */
   protected final Connection getConnection(
      DatabaseConnectionDefinition dataSource
   ) throws OSSException
   {
      Connection cntDBConnection;
      
      // Instead of directly getting connection, ask connection pool for one
      // This will make it more friendly for the derived classes since they
      // just have to worry about connection pools and not what conversions are
      // required
      cntDBConnection = getPooledConnection(
                           (ConnectionPoolDefinition)dataSource);
      
      return cntDBConnection;
   }
   
   /**
    * {@inheritDoc}
    */
   protected final Connection getConnection(
      DatabaseConnectionDefinition dataSource,
      String               strUser,
      String               strPassword
   ) throws OSSException
   {
      Connection cntDBConnection;
      
      // Instead of directly getting connection, ask connection pool for one
      // This will make it more friendly for the derived classes since they
      // just have to worry about connection pools and not what conversions are
      // required
      cntDBConnection = getPooledConnection(
                           (ConnectionPoolDefinition)dataSource,
                           strUser, strPassword);
      
      return cntDBConnection;
   }
   
   /**
    * {@inheritDoc}
    */
   protected final void returnConnection(
      Connection           cntDBConnection,
      DatabaseConnectionDefinition dataSource
   )
   {
      // This will make it more friendly for the derived classes since they
      // just have to worry about connection pools and not what conversions are
      // required
      returnPooledConnection(cntDBConnection, 
                             (ConnectionPoolDefinition)dataSource);            
   }
   
   /**
    * Get connection for given connection pool.
    * 
    * @param connectionpool - connection pool to get connection for
    * @return Connection - this can be null if connection is not available
    * @throws OSSException - an error has occurred
    */
   protected abstract Connection getPooledConnection(
      ConnectionPoolDefinition connectionpool
   ) throws OSSException;
   
   /**
    * Get connection using the same settings as given connection pool but
    * get the connection under different name and password. 
    * 
    * @param connectionpool - connection pool defining settings to get connection 
    *                         for
    * @param strUser - user name to get the connection for
    * @param strPassword - password to get the connection for
    * @return Connection - this can be null if connection is not available
    * @throws OSSException - an error has occurred
    */
   protected abstract Connection getPooledConnection(
      ConnectionPoolDefinition connectionpool,
      String                   strUser,
      String                   strPassword
   ) throws OSSException;

   /**
    * Get connection directly from a driver without requesting it from a 
    * connection pool. This is a convenience method, which delegates this call
    * to a base class and can be used by the derived classes in case they
    * cannot for some reason get connection from a pool.
    * 
    * @param connectionpool - connection pool to get connection for
    * @return Connection - this can be null if connection is not available
    * @throws OSSException - an error has occurred
    */
   protected Connection getNonPooledConnection(
      ConnectionPoolDefinition connectionpool
   ) throws OSSException
   {
      return super.getConnection(connectionpool);
   }
   
   /**
    * Get connection directly from a driver under specific name and password 
    * without requesting it from a connection pool. This is a convenience 
    * method, which delegates this call to a base class and can be used by the 
    * derived classes in case they cannot for some reason get connection from a 
    * pool.
    * 
    * @param connectionpool - connection pool defining settings to get 
    *                         connection for
    * @param strUser - user name to get the connection for
    * @param strPassword - password to get the connection for
    * @return Connection - this can be null if connection is not available
    * @throws OSSException - an error has occurred
    */
   protected Connection getNonPooledConnection(
      ConnectionPoolDefinition connectionpool,
      String                   strUser,
      String                   strPassword
   ) throws OSSException
   {
      return super.getConnection(connectionpool, strUser, strPassword);
   }

   /**
    * Return connection which was taken from the pool. Since most pool 
    * implementation will return connection to the pool as soon as it is closed
    * the default implementation is provided to just close the connection as it 
    * is done by the base class (that is also why there is no need for 
    * returnNonPooledConnection method). If the derived class needs to do 
    * something specific, it can do so by overriding this method.
    * 
    * @param cntDBConnection - connection taken from the pool
    * @param connectionPool - connection pool from which the connection was taken
    */
   protected void returnPooledConnection(
      Connection               cntDBConnection,
      ConnectionPoolDefinition connectionPool
   )
   {
      // Since most pool implementation will return connection to the pool as 
      // soon as it is closed the default implementation is provided to just 
      // close the connection by calling the base class method
      super.returnConnection(cntDBConnection, connectionPool);
   }   

   /**
    * Create new connection pool with specified parameters. 
    * 
    * @param strConnectionPoolName - connection pool name 
    * @param database - database for which the connection pool is being created. 
    *                   This can represent different DBMS types (e.g. Oracle, 
    *                   DB2, etc.) or different groups of settings for the same 
    *                   database (e.g. Oracle Production, Oracle Development, 
    *                   etc.).
    * @param strDriverName - name of the JDBC driver
    * @param strUrl - url by which data source connects to the database 
    * @param strUser - user name to connects to the database
    * @param strPassword - password to connects to the database
    * @param iTransactionIsolation - transaction isolation that should be used
    *                                for connections
    * @return Object - connection pool
    * @throws OSSException - an error has occurred 
    */
   protected abstract Object createConnectionPool(
      String   strConnectionPoolName,
      Database database,
      String   strDriverName,
      String   strUrl,
      String   strUser,
      String   strPassword,
      int      iTransactionIsolation
   ) throws OSSException;

   /**
    * Destroy the specified connection pool and free all connections maintained 
    * to database by this connection pool.
    * 
    * @param connectionpool - connection pool to close.
    * @throws OSSException - an error has occurred 
    */
   protected abstract void destroyConnectionPool(
      ConnectionPoolDefinition connectionpool
   ) throws OSSException;   
}
