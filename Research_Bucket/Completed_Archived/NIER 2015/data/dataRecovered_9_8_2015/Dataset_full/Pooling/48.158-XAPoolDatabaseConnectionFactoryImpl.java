/*
 * Copyright (c) 2003 - 2009 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: XAPoolDatabaseConnectionFactoryImpl.java,v 1.1 2009/04/22 05:04:36 bastafidli Exp $
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

package org.opensubsystems.core.persist.db.connectionpool.xapool;

import java.sql.Connection;
import java.sql.SQLException;

import org.enhydra.jdbc.pool.StandardXAPoolDataSource;
import org.enhydra.jdbc.standard.StandardXADataSource;
import org.opensubsystems.core.error.OSSDatabaseAccessException;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.persist.db.Database;
import org.opensubsystems.core.persist.db.connectionpool.impl.PooledDatabaseConnectionFactoryImpl;
import org.opensubsystems.core.persist.db.connectionpool.impl.PooledDatabaseConnectionFactorySetupReader;
import org.opensubsystems.core.persist.db.impl.DatabaseImpl;
import org.opensubsystems.core.persist.db.impl.DatabaseTransactionFactoryImpl;

/**
 * Implementation of connection pool using Objectweb XAPool package 
 * (http://xapool.experlog.com/).
 *
 * @version $Id: XAPoolDatabaseConnectionFactoryImpl.java,v 1.1 2009/04/22 05:04:36 bastafidli Exp $
 * @author Miro Halas
 * @code.reviewer Miro Halas
 * @code.reviewed 1.10 2008/06/26 04:14:33 bastafidli
 */
public class XAPoolDatabaseConnectionFactoryImpl extends PooledDatabaseConnectionFactoryImpl
{   
   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Constructor for new instance using default database properties and default
    * transaction factory.
    */
   public XAPoolDatabaseConnectionFactoryImpl(
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
   public XAPoolDatabaseConnectionFactoryImpl(
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
         conReturn = (Connection)((StandardXAPoolDataSource)
                         connectionpool.getConnectionPool()).getConnection();
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
   ) throws OSSDatabaseAccessException
   {
      Connection conReturn;
      
      try
      {
         conReturn = (Connection)((StandardXAPoolDataSource)
                         connectionpool.getConnectionPool()).getConnection(
                                                                strUser, 
                                                                strPassword);
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
      long lRetryTimeForConnection = setupReader.getLongParameterValue(
               PooledDatabaseConnectionFactorySetupReader.DBPOOL_RETRY_PERIOD).longValue();
      int iCheckLevel = setupReader.getIntegerParameterValue(
               PooledDatabaseConnectionFactorySetupReader.DBPOOL_CHECK_LEVEL).intValue();
      int iPreparedStatementCacheSize = setupReader.getIntegerParameterValue(
               PooledDatabaseConnectionFactorySetupReader.DBPOOL_PREPSTATEMENT_CACHE_SIZE
                  ).intValue();

      StandardXADataSource xadsDataSource;

      // Fix for defect# 1630270 
      if (iPreparedStatementCacheSize < 0)
      {
         iPreparedStatementCacheSize = 0;
      }
      
      // Create regular XA data source
      xadsDataSource = new StandardXADataSource();
      try
      {
         xadsDataSource.setDriverName(strDriverName);
      }
      catch (SQLException sqleExc)
      {
         throw new OSSDatabaseAccessException("Unexpected error when creating" +
                                                 " connection pool.", sqleExc);
      }
      xadsDataSource.setUrl(strUrl);
      xadsDataSource.setUser(strUser);
      xadsDataSource.setPassword(strPassword);
      xadsDataSource.setPreparedStmtCacheSize(iPreparedStatementCacheSize);
      if (iTransactionIsolation >= 0)
      {
         // Set the transaction isolation only if driver supports it
         xadsDataSource.setTransactionIsolation(iTransactionIsolation);
      }
      
      if (m_transactionFactory != null)
      {
         xadsDataSource.setTransactionManager(m_transactionFactory.getTransactionManager());
      }

      StandardXAPoolDataSource xapdsPoolDataSource;
      
      // Create an XA pooled datasource 
      xapdsPoolDataSource = new StandardXAPoolDataSource(iInitialPoolSize);
      xapdsPoolDataSource.setDataSourceName("XA" + strConnectionPoolName);
      xapdsPoolDataSource.setDataSource(xadsDataSource);
      if (m_transactionFactory != null)
      {
         xapdsPoolDataSource.setTransactionManager(m_transactionFactory.getTransactionManager());
      }
      xapdsPoolDataSource.setUser(strUser);
      xapdsPoolDataSource.setPassword(strPassword);

      // bCanGrow is not supported, c3p0 pool size cannot grow when all connections 
      // are already requested.
      try
      {
         // Set max size first so that the min size is never greater than the 
         // max size
         xapdsPoolDataSource.setMaxSize(iMaximalPoolSize);
         xapdsPoolDataSource.setMinSize(iMinimalPoolSize);
      }
      catch (Exception eExc)
      {
         throw new OSSDatabaseAccessException("Unexpected error when creating" +
                                                 " connection pool.", eExc);
      }
      xapdsPoolDataSource.setDeadLockMaxWait(lMaxWaitTimeForConnection);
      xapdsPoolDataSource.setDeadLockRetryWait(lRetryTimeForConnection);
      xapdsPoolDataSource.setCheckLevelObject(iCheckLevel);
      xapdsPoolDataSource.setJdbcTestStmt(
         DatabaseImpl.getInstance().getConnectionTestStatement());

      return xapdsPoolDataSource;
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
         ((StandardXAPoolDataSource)connectionpool.getConnectionPool())
                                                      .shutdown(true);
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
