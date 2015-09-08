/*
 * Copyright (c) 2003 - 2009 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: ProxoolDatabaseConnectionFactoryImpl.java,v 1.1 2009/04/22 05:04:00 bastafidli Exp $
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

package org.opensubsystems.core.persist.db.connectionpool.proxool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.logicalcobwebs.proxool.ProxoolConstants;
import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.ProxoolFacade;
import org.opensubsystems.core.error.OSSDatabaseAccessException;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.persist.db.Database;
import org.opensubsystems.core.persist.db.connectionpool.impl.PooledDatabaseConnectionFactoryImpl;
import org.opensubsystems.core.persist.db.connectionpool.impl.PooledDatabaseConnectionFactorySetupReader;
import org.opensubsystems.core.persist.db.impl.DatabaseImpl;
import org.opensubsystems.core.persist.db.impl.DatabaseTransactionFactoryImpl;

/**
 * Implementation of connection pool using Objectweb Proxool package 
 * (http://proxool.sourceforge.net/).
 * 
 * Design of Proxool doesn't allow us to use separate instance of Proxool
 * for each database connection factory. This is because ConnectionPoolManager
 * singleton accessible only through ProxoolFacade. This is causing multiple
 * connection factories to underneath share the same connection pools, which
 * can result in bugs such as #1275551 when multiple factories cannot create
 * pool with the same name. The solution to this problem would be for Proxool
 * ConnectionPoolManager to do not be a singleton so each 
 * ProxoolDatabaseConnectionFactoryImpl can have its own connection pool manager.
 *
 * @version $Id: ProxoolDatabaseConnectionFactoryImpl.java,v 1.1 2009/04/22 05:04:00 bastafidli Exp $
 * @author Julo Legeny
 * @code.reviewer Miro Halas
 * @code.reviewed 1.7 2008/06/26 04:14:33 bastafidli
 */
public class ProxoolDatabaseConnectionFactoryImpl extends PooledDatabaseConnectionFactoryImpl
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Constructor for new instance using default database properties.
    */
   public ProxoolDatabaseConnectionFactoryImpl(
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
   public ProxoolDatabaseConnectionFactoryImpl(
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
         conReturn = DriverManager.getConnection(
                        connectionpool.getConnectionPool().toString());
      }
      catch (SQLException sqleExc)
      {
         throw new OSSDatabaseAccessException(
                      "Cannot get database connection from pool.", sqleExc);
      }
      
      return conReturn;
   }

   /**
    * {@inheritDoc}
    */
   protected Connection getPooledConnection(
      ConnectionPoolDefinition connectionpool,
      String strUser,
      String strPassword
   ) throws OSSDatabaseAccessException
   {
      Connection cntDBConnection;
      
      try
      {
         cntDBConnection = DriverManager.getConnection(
                              connectionpool.getConnectionPool().toString(),
                              strUser, strPassword);
      }
      catch (SQLException sqleExc)
      {
         throw new OSSDatabaseAccessException("Cannot get database connection" 
                                              + " for explicitly specified user.",
                                                 sqleExc); 
      }   
      
      return cntDBConnection;
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

      int iMinimalPoolSize = setupReader.getIntegerParameterValue(
               PooledDatabaseConnectionFactorySetupReader.DBPOOL_MIN_SIZE).intValue();
      int iMaximalPoolSize = setupReader.getIntegerParameterValue(
               PooledDatabaseConnectionFactorySetupReader.DBPOOL_MAX_SIZE).intValue();
// DBPOOL_CAN_GROW and DBPOOL_WAIT_PERIOD is not supported Proxool at this time 
// As soon as connection are exhausted, proxool throws an exception
// See mailing list at:
// http://sourceforge.net/mailarchive/forum.php?thread_id=2232720&forum_id=31278 
// http://sourceforge.net/mailarchive/forum.php?thread_id=4827193&forum_id=31278
//      boolean bCanGrow = setupReader.getBooleanParameterValue(
//               PooledDatabaseConnectionFactorySetupReader.DBPOOL_CAN_GROW).booleanValue();
//      long lMaxWaitTimeForConnection = setupReader.getLongParameterValue(
//               PooledDatabaseConnectionFactorySetupReader.DBPOOL_WAIT_PERIOD).longValue();
      boolean bValidateOnBorrow = setupReader.getBooleanParameterValue(
               PooledDatabaseConnectionFactorySetupReader.DBPOOL_VALIDATE_BORROW).booleanValue();
      boolean bValidateOnReturn = setupReader.getBooleanParameterValue(
               PooledDatabaseConnectionFactorySetupReader.DBPOOL_VALIDATE_RETURN).booleanValue();
      long lTimeBetweenEvictionRunsMillis = setupReader.getLongParameterValue(
               PooledDatabaseConnectionFactorySetupReader.DBPOOL_IDLE_CHECK_PERIOD).longValue();
//    int iPreparedStatementCacheSize = setupReader.getIntegerParameterValue(
//             PooledDatabaseConnectionFactorySetupReader.DBPOOL_PREPSTATEMENT_CACHE_SIZE)
//                .intValue();

      try
      {
         Class.forName("org.logicalcobwebs.proxool.ProxoolDriver");
      }
      catch (ClassNotFoundException cnfeExc)
      {
         throw new OSSDatabaseAccessException("Cannot found proxool driver " +
                                              "'org.logicalcobwebs.proxool.ProxoolDriver'.",
                                              cnfeExc);
      }

      Properties info = new Properties();

      // bCanGrow is not supported, c3p0 pool size cannot grow when all connections 
      // are already requested.
      // lMaxWaitTimeForConnection is not supported, the pool returns error immediately
      // FATAL_SQL_EXCEPTION - not used
      // FATAL_SQL_EXCEPTION_WRAPPER_CLASS - not used
      info.setProperty(ProxoolConstants.HOUSE_KEEPING_SLEEP_TIME_PROPERTY,
                       Integer.toString((int)lTimeBetweenEvictionRunsMillis));
      info.setProperty(ProxoolConstants.HOUSE_KEEPING_TEST_SQL_PROPERTY,
                       DatabaseImpl.getInstance().getConnectionTestStatement());
      // INJECTABLE_CONNECTION_INTERFACE_NAME - not used
      // INJECTABLE_STATEMENT_INTERFACE_NAME - not used
      // INJECTABLE_PREPARED_STATEMENT_INTERFACE_NAME - not used
      // INJECTABLE_CALLABLE_STATEMENT_INTERFACE_NAME - not used
      // JMX_PROPERTY - not used
      // JMX_AGENT_ID - not used
      // JNDI_NAME - not used
      // Set this to maximal value since we do not want connection pool killing
      // our threads
      info.setProperty(ProxoolConstants.MAXIMUM_ACTIVE_TIME_PROPERTY, 
                       Integer.toString(Integer.MAX_VALUE));
      info.setProperty(ProxoolConstants.MAXIMUM_CONNECTION_COUNT_PROPERTY, 
                       Integer.toString(iMaximalPoolSize));
      // Set this to maximal value since we do not want connection pool killing
      // our connections
      info.setProperty(ProxoolConstants.MAXIMUM_CONNECTION_LIFETIME_PROPERTY, 
                       Integer.toString(Integer.MAX_VALUE));
      info.setProperty(ProxoolConstants.MINIMUM_CONNECTION_COUNT_PROPERTY,
                       Integer.toString(iMinimalPoolSize));
      // OVERLOAD_WITHOUT_REFUSAL_LIFETIME - not used
      // PROTOTYPE_COUNT - not used
      // RECENTLY_STARTED_THRESHOLD - not used
      // SIMULTANEOUS_BUILD_THROTTLE - not used
      // STATISTICS - not used
      // STATISTICS_LOG_LEVEL - not used
      info.setProperty(ProxoolConstants.TEST_BEFORE_USE_PROPERTY,
                       Boolean.toString(bValidateOnBorrow));
      info.setProperty(ProxoolConstants.TEST_AFTER_USE_PROPERTY,
                       Boolean.toString(bValidateOnReturn));
      info.setProperty(ProxoolConstants.USER_PROPERTY, strUser);
      info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, strPassword);
      // TRACE - not used
      // VERBOSE - not used

      StringBuffer sbProxoolURL = new StringBuffer();
      
      sbProxoolURL.append("proxool.");
      sbProxoolURL.append(strConnectionPoolName);
      sbProxoolURL.append(":");
      sbProxoolURL.append(strDriverName);
      sbProxoolURL.append(":");
      sbProxoolURL.append(strUrl);
      
      try
      {
         ProxoolFacade.registerConnectionPool(sbProxoolURL.toString(), info);
      }
      catch (ProxoolException peExc)
      {
         throw new OSSDatabaseAccessException("Unexpected error when registering" 
                                              + " connection pool.", peExc);
      }
      
      return sbProxoolURL.toString();
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
         ProxoolFacade.removeConnectionPool(connectionpool.getName());
      }
      catch (ProxoolException peExc)
      {
         throw new OSSDatabaseAccessException("Cannot close connection pool.", 
                                              peExc);
      }      
   }
}
