/*
 * Copyright (c) 2003 - 2009 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: C3P0DatabaseConnectionFactoryImpl.java,v 1.1 2009/04/22 05:02:38 bastafidli Exp $
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

package org.opensubsystems.core.persist.db.connectionpool.c3p0;

import java.sql.Connection;

import org.opensubsystems.core.error.OSSDatabaseAccessException;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.persist.db.Database;
import org.opensubsystems.core.persist.db.connectionpool.impl.PooledDatabaseConnectionFactoryImpl;
import org.opensubsystems.core.persist.db.connectionpool.impl.PooledDatabaseConnectionFactorySetupReader;
import org.opensubsystems.core.persist.db.impl.DatabaseImpl;
import org.opensubsystems.core.persist.db.impl.DatabaseTransactionFactoryImpl;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.PooledDataSource;

/**
 * Implementation of connection pool using C3P0 connection pool.
 * (http://sourceforge.net/projects/c3p0/)
 *
 * @version $Id: C3P0DatabaseConnectionFactoryImpl.java,v 1.1 2009/04/22 05:02:38 bastafidli Exp $
 * @author Julo Legeny
 * @code.reviewer Miro Halas
 * @code.reviewed 1.8 2008/06/26 04:14:33 bastafidli
 */
public class C3P0DatabaseConnectionFactoryImpl extends PooledDatabaseConnectionFactoryImpl
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Constructor for new instance using default database properties.
    */
   public C3P0DatabaseConnectionFactoryImpl(
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
   public C3P0DatabaseConnectionFactoryImpl(
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
         conReturn = (Connection)((PooledDataSource) 
                         connectionpool.getConnectionPool()).getConnection();
      }
      catch (Exception eExc)
      {
         // ComboPooledDataSource throws Exception so convert it to something 
         // more meaningful here
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
      Connection conReturn;
      
      try
      {
         conReturn = (Connection)((PooledDataSource)
                         connectionpool.getConnectionPool()).getConnection(
                                                                strUser, 
                                                                strPassword);
      }
      catch (Exception eExc)
      {
         // ComboPooledDataSource throws Exception so convert it to something 
         // more meaningful here
         throw new OSSDatabaseAccessException(
                      "Cannot get database connection from pool.", eExc);
      }
      
      return conReturn;
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
      PooledDatabaseConnectionFactorySetupReader setupReader 
          = new PooledDatabaseConnectionFactorySetupReader(
                   strConnectionPoolName, database.getDatabaseTypeIdentifier());

      int iInitialPoolSize = setupReader.getIntegerParameterValue(
               PooledDatabaseConnectionFactorySetupReader.DBPOOL_INITIAL_SIZE).intValue();
      int iMinimalPoolSize = setupReader.getIntegerParameterValue(
               PooledDatabaseConnectionFactorySetupReader.DBPOOL_MIN_SIZE).intValue();
      int iMaximalPoolSize = setupReader.getIntegerParameterValue(
               PooledDatabaseConnectionFactorySetupReader.DBPOOL_MAX_SIZE).intValue();
//      boolean bCanGrow = setupReader.getBooleanParameterValue(
//               PooledDatabaseConnectionFactorySetupReader.DBPOOL_CAN_GROW).booleanValue();
      long lMaxWaitTimeForConnection = setupReader.getLongParameterValue(
               PooledDatabaseConnectionFactorySetupReader.DBPOOL_WAIT_PERIOD).longValue();
      boolean bValidateOnBorrow = setupReader.getBooleanParameterValue(
               PooledDatabaseConnectionFactorySetupReader.DBPOOL_VALIDATE_BORROW).booleanValue();
      boolean bValidateOnReturn = setupReader.getBooleanParameterValue(
               PooledDatabaseConnectionFactorySetupReader.DBPOOL_VALIDATE_RETURN).booleanValue();
      int iRetryCountForConnection = setupReader.getIntegerParameterValue(
               PooledDatabaseConnectionFactorySetupReader.DBPOOL_RETRY_COUNT).intValue();
      long lRetryTimeForConnection = setupReader.getLongParameterValue(
               PooledDatabaseConnectionFactorySetupReader.DBPOOL_RETRY_PERIOD).longValue();
      long lTimeBetweenEvictionRunsMillis = setupReader.getLongParameterValue(
               PooledDatabaseConnectionFactorySetupReader.DBPOOL_IDLE_CHECK_PERIOD).longValue();
      long lMinEvictableIdleTimeMillis = setupReader.getLongParameterValue(
               PooledDatabaseConnectionFactorySetupReader.DBPOOL_IDLE_PERIOD).longValue();
      int iPreparedStatementCacheSize = setupReader.getIntegerParameterValue(
               PooledDatabaseConnectionFactorySetupReader.DBPOOL_PREPSTATEMENT_CACHE_SIZE
                  ).intValue();

      ComboPooledDataSource pooledDS = null;

      // Description of configuration properties is available at
      // http://www.mchange.com/projects/c3p0/index.html#appendix_a
      pooledDS = new ComboPooledDataSource();
      pooledDS.setJdbcUrl(strUrl);
      pooledDS.setUser(strUser);
      pooledDS.setPassword(strPassword);
      
      // Create custom configured pooled data source
      pooledDS.setInitialPoolSize(iInitialPoolSize);
      pooledDS.setMinPoolSize(iMinimalPoolSize);
      pooledDS.setMaxPoolSize(iMaximalPoolSize);
      pooledDS.setMaxStatements(iPreparedStatementCacheSize);
      // pooledDS.setMaxStatementsPerConnection() - not used since we support only global cache 
      
      // bCanGrow is not supported, c3p0 pool size cannot grow when all connections 
      // are already requested.
      pooledDS.setAcquireIncrement(1);
      pooledDS.setAcquireRetryAttempts(iRetryCountForConnection);
      pooledDS.setAcquireRetryDelay((int)lRetryTimeForConnection);
      // Do not automatically comit since our database layer should take care of this
      pooledDS.setAutoCommitOnClose(false);
      // pooledDS.setAutomaticTestTable - not used since we are using setPreferredTestQuery
      pooledDS.setBreakAfterAcquireFailure(false);
      pooledDS.setCheckoutTimeout((int)lMaxWaitTimeForConnection);
      // pooledDS.setConnectionTesterClassName() - not used since we are using setPreferredTestQuery
      // pooledDS.setFactoryClassLocation() - not used since I do not understand what it is 
      // pooledDS.setForceIgnoreUnresolvedTransactions() - not used since it is no clear
      // C3P0 requires this time to be in seconds so convert it from milliseconds
      pooledDS.setIdleConnectionTestPeriod((int)lTimeBetweenEvictionRunsMillis / 1000);
      // C3P0 requires this time to be in seconds so convert it from milliseconds
      pooledDS.setMaxIdleTime((int)lMinEvictableIdleTimeMillis / 1000);
      // pooledDS.setNumHelperThreads() - left default value since it is hard to judge what to set 
      pooledDS.setPreferredTestQuery(DatabaseImpl.getInstance().getConnectionTestStatement());
      // pooledDS.setPropertyCycle() - not used since it is ignored by C3P0 
      pooledDS.setTestConnectionOnCheckin(bValidateOnReturn);
      pooledDS.setTestConnectionOnCheckout(bValidateOnBorrow);
      // pooledDS.setUsesTraditionalReflectiveProxies() - not used only for backward compatibility
      
      return pooledDS;
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
         ((PooledDataSource)connectionpool.getConnectionPool()).close();
         // We need to wait because c3p0 closes the connections asynchronously 
         // and we may get unexpected errors, e.g. when we want to drop user in 
         // test since the connection is still open for some time
         // TODO: Improve: Either make this configurable or figure out a way
         // how we can wait until the pool is really destroyed
         Thread.sleep(1000);
      }
      catch (Exception eExc)
      {
         // ComboPooledDataSource throws Exception so convert it to something 
         // more meaningful here
         throw new OSSDatabaseAccessException("Cannot close connection pool.", 
                                              eExc);
      }      
   }
}
