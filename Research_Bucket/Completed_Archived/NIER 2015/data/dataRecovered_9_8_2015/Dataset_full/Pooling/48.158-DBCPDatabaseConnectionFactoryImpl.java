/*
 * Copyright (c) 2003 - 2009 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: DBCPDatabaseConnectionFactoryImpl.java,v 1.1 2009/04/22 05:03:00 bastafidli Exp $
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

package org.opensubsystems.core.persist.db.connectionpool.dbcp;

import java.sql.Connection;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.pool.KeyedObjectPoolFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericKeyedObjectPoolFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.opensubsystems.core.error.OSSDatabaseAccessException;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.persist.db.Database;
import org.opensubsystems.core.persist.db.connectionpool.impl.PooledDatabaseConnectionFactoryImpl;
import org.opensubsystems.core.persist.db.connectionpool.impl.PooledDatabaseConnectionFactorySetupReader;
import org.opensubsystems.core.persist.db.impl.DatabaseImpl;
import org.opensubsystems.core.persist.db.impl.DatabaseTransactionFactoryImpl;
import org.opensubsystems.core.util.GlobalConstants;

/**
 * Implementation of connection pool using Jakarta Commons DBCP package.
 * 
 * @version $Id: DBCPDatabaseConnectionFactoryImpl.java,v 1.1 2009/04/22 05:03:00 bastafidli Exp $
 * @author Miro Halas
 * @code.reviewer Miro Halas
 * @code.reviewed 1.9 2008/06/26 04:14:33 bastafidli
 */
public class DBCPDatabaseConnectionFactoryImpl extends PooledDatabaseConnectionFactoryImpl
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Constructor for new instance using default database properties.
    */
   public DBCPDatabaseConnectionFactoryImpl(
   )  
   {
      this(null);
   }

   /**
    * Constructor for new instance using default database properties.
    * 
    * @param transactionFactory - transaction factory to use for this 
    *                               connection factory, can be null
    */
   public DBCPDatabaseConnectionFactoryImpl(
      DatabaseTransactionFactoryImpl transactionFactory
   ) 
   {
      super(transactionFactory);
   }
   
   // Helper methods ///////////////////////////////////////////////////////////

   /**
    * {@inheritDoc}
    */
   protected Connection getPooledConnection(
      ConnectionPoolDefinition connectionpool
   ) throws OSSException
   {
      Connection conReturn;
      
      try
      {
         conReturn = (Connection)((ObjectPool)connectionpool.getConnectionPool())
                                                                .borrowObject();
      }
      catch (Exception eExc)
      {
         // ObjectPool throws Exception so convert it to something more 
         // meaningful here
         throw new OSSDatabaseAccessException(
                      "Cannot get database connection from pool.", eExc);
      }
      
      return conReturn;
   }

   /**
    * {@inheritDoc}
    */
   protected Connection getPooledConnection(
      ConnectionPoolDefinition connectionpool,
      String                   strUser,
      String                   strPassword
   ) throws OSSException
   {
      // DBCP doesn't provide any way how to get a connection for a specific 
      // user so just get a nonpooled connection
      return getNonPooledConnection(connectionpool, strUser, strPassword);
   }

   /**
    * {@inheritDoc}
    */
   protected Object createConnectionPool(
      String   strConnectionPoolName,
      Database database,
      String   strDriverName,
      String   strUrl,
      String   strUser,
      String   strPassword,
      int      iTransactionIsolation
   ) throws OSSException
   {
      // I am using here the PoolingDriver instead of PoolingDataSource because
      // in DBCP version 1.1 the PoolingDriver has clear way how to shutdown
      // the pool and PoolingDataSource doesn't.
      // This code was inspired by method setupDriver from 
      // ManualPoolingDriverExample.java in commons-dbcp package v 1.6
      ObjectPool                connectionPool;
      ConnectionFactory         connectionFactory;
      PoolableConnectionFactory poolableConnectionFactory;
      
      PooledDatabaseConnectionFactorySetupReader setupReader 
          = new PooledDatabaseConnectionFactorySetupReader(
                   strConnectionPoolName, database.getDatabaseTypeIdentifier());

      int iInitialPoolSize = setupReader.getIntegerParameterValue(
               PooledDatabaseConnectionFactorySetupReader.DBPOOL_INITIAL_SIZE).intValue();
      int iMinimalPoolSize = setupReader.getIntegerParameterValue(
               PooledDatabaseConnectionFactorySetupReader.DBPOOL_MIN_SIZE).intValue();
      int iMaximalPoolSize = setupReader.getIntegerParameterValue(
               PooledDatabaseConnectionFactorySetupReader.DBPOOL_MAX_SIZE).intValue();
      boolean bCanGrow = setupReader.getBooleanParameterValue(
               PooledDatabaseConnectionFactorySetupReader.DBPOOL_CAN_GROW).booleanValue();
      long lMaxWaitTimeForConnection = setupReader.getLongParameterValue(
               PooledDatabaseConnectionFactorySetupReader.DBPOOL_WAIT_PERIOD).longValue();
      boolean bValidateOnBorrow = setupReader.getBooleanParameterValue(
               PooledDatabaseConnectionFactorySetupReader.DBPOOL_VALIDATE_BORROW).booleanValue();
      boolean bValidateOnReturn = setupReader.getBooleanParameterValue(
               PooledDatabaseConnectionFactorySetupReader.DBPOOL_VALIDATE_RETURN).booleanValue();
      boolean bValidateOnIdle = setupReader.getBooleanParameterValue(
               PooledDatabaseConnectionFactorySetupReader.DBPOOL_VALIDATE_IDLE).booleanValue();
      long lTimeBetweenEvictionRunsMillis = setupReader.getLongParameterValue(
               PooledDatabaseConnectionFactorySetupReader.DBPOOL_IDLE_CHECK_PERIOD).longValue();
      int iNumTestsPerEvictionRun = setupReader.getIntegerParameterValue(
               PooledDatabaseConnectionFactorySetupReader.DBPOOL_IDLE_CHECK_SIZE).intValue();
      long lMinEvictableIdleTimeMillis = setupReader.getLongParameterValue(
               PooledDatabaseConnectionFactorySetupReader.DBPOOL_IDLE_PERIOD).longValue();
      int iPreparedStatementCacheSize = setupReader.getIntegerParameterValue(
               PooledDatabaseConnectionFactorySetupReader.DBPOOL_PREPSTATEMENT_CACHE_SIZE
                  ).intValue();
      
      // First, we'll need a ObjectPool that serves as the actual pool of 
      // connections. We'll use a GenericObjectPool instance, although
      // any ObjectPool implementation will suffice.
      connectionPool = new GenericObjectPool(
                              null, // factory will be specified below
                              iMaximalPoolSize,
                              bCanGrow ? GenericObjectPool.WHEN_EXHAUSTED_GROW
                                       : GenericObjectPool.WHEN_EXHAUSTED_BLOCK, 
                              lMaxWaitTimeForConnection,
                              iMaximalPoolSize, // max idle - if no connections are used
                                                // the pool should not fall under this size
                              iMinimalPoolSize, // min idle - if connection count falls 
                                                // under this limit (e.g. closed connections)
                                                // new connections will be created
                              bValidateOnBorrow,
                              bValidateOnReturn,
                              lTimeBetweenEvictionRunsMillis,
                              iNumTestsPerEvictionRun,
                              lMinEvictableIdleTimeMillis,
                              bValidateOnIdle);

      
      // Next, we'll create a ConnectionFactory that the pool will use to 
      // create Connections. I am using DriverManagerConnectionFactory instead 
      // of DriverConnectionFactory because it allows me to specify user name 
      // and password directly
      connectionFactory = new DriverManagerConnectionFactory(
                                 strUrl, strUser, strPassword);

      // This configuration of prepared statement caching is inspired by
      // Commons-DBCP Wiki avaiabe at http://wiki.apache.org/jakarta-commons/DBCP
      // null can be used as parameter because this parameter is set in 
      // PoolableConnectionFactory when creating a new PoolableConnection
      // 0 according to documentation should mean no limit
      KeyedObjectPoolFactory statementPool = null;
      if (iPreparedStatementCacheSize >= 0)
      {
         statementPool = new GenericKeyedObjectPoolFactory(
                                null, iPreparedStatementCacheSize);
      }
      
      // Now we'll create the PoolableConnectionFactory, which wraps
      // the "real" Connections created by the ConnectionFactory with
      // the classes that implement the pooling functionality.
      poolableConnectionFactory = new PoolableConnectionFactory(
                                         connectionFactory,
                                         connectionPool,
                                         statementPool,
                                         DatabaseImpl.getInstance().getConnectionTestStatement(),
                                         false, // not read-only connection
                                         false, // Default auto commit is false
                                         iTransactionIsolation);

      // PoolableConnectionFactory doesn't support the initialSize attribute of
      // DBCP so I have replicated the code from BasicDataSource v1.37 here
      try 
      {
         for (int iIndex = 0; iIndex < iInitialPoolSize; iIndex++) 
         {
            connectionPool.addObject();
         }
      } 
      catch (Exception e) 
      {
         throw new OSSDatabaseAccessException("Error preloading the connection pool", e);
      }
      
      if (GlobalConstants.ERROR_CHECKING)
      {
         // Put this check here to trick checkstyle
         assert poolableConnectionFactory != null 
                : "Poolable connection factory cannot be null.";
      }
      
      return connectionPool;
   }

   /**
    * {@inheritDoc}
    */
   protected void destroyConnectionPool(
      ConnectionPoolDefinition connectionpool
   ) throws OSSException
   {
      try
      {
         ((ObjectPool)connectionpool.getConnectionPool()).close();
      }
      catch (Exception eExc)
      {
         // ObjectPool throws Exception so convert it to something more 
         // meaningful here
         throw new OSSDatabaseAccessException("Cannot close connection pool.", 
                                              eExc);
      }      
   }
}
