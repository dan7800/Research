/*
 * Copyright (c) 2003 - 2009 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: DatabaseConnectionFactoryImpl.java,v 1.1 2009/04/22 05:40:47 bastafidli Exp $
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

package org.opensubsystems.core.persist.db.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.transaction.SystemException;

import org.opensubsystems.core.error.OSSDatabaseAccessException;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.persist.db.Database;
import org.opensubsystems.core.persist.db.DatabaseConnectionFactory;
import org.opensubsystems.core.persist.db.DatabaseSourceDefinition;
import org.opensubsystems.core.persist.db.DatabaseTransactionFactory;
import org.opensubsystems.core.persist.db.connectionpool.dbcp.DBCPDatabaseConnectionFactoryImpl;
import org.opensubsystems.core.persist.db.connectionpool.j2ee.J2EEDatabaseConnectionFactoryImpl;
import org.opensubsystems.core.util.ClassFactory;
import org.opensubsystems.core.util.Config;
import org.opensubsystems.core.util.GlobalConstants;
import org.opensubsystems.core.util.Log;
import org.opensubsystems.core.util.PropertyUtils;
import org.opensubsystems.core.util.j2ee.J2EEUtils;
import org.opensubsystems.core.util.jdbc.DatabaseUtils;

/**
 * Base class for implementation of factories for retrieving and returning of 
 * database connections.
 * 
 * Most likely one project will use only one implementation of connection factory
 * therefore this class contains factory methods to instantiate and return
 * default instance of connection factory even though it still allows multiple
 * instances of connection factory to exist since all attributes are non-static.
 * 
 * @version $Id: DatabaseConnectionFactoryImpl.java,v 1.1 2009/04/22 05:40:47 bastafidli Exp $
 * @author Miro Halas
 * @code.reviewer Miro Halas
 * @code.reviewed 1.29 2008/11/14 00:03:24 bastafidli
 */
public class DatabaseConnectionFactoryImpl implements DatabaseConnectionFactory
{
   /**
    * Simple structure collecting all information about the data source.
    * DataSource corresponds to a set of connections to a database.
    */
   public class DatabaseConnectionDefinition extends DatabaseSourceDefinition
   {
      /**
       * Number of requested connections for particular data source.
       */
      protected int m_iRequestedConnectionCount;

      /**
       * @param strName - name of the data source
       * @param database - database for this this data source is being created 
       * @param strDriver - JDBC driver user by this data source
       * @param strUrl - URL to connect to the database
       * @param strUser - user name to use to connect to the database
       * @param strPassword - password to use to connect to the database
       * @param iTransactionIsolation - transaction isolation that should be 
       *                                used for connections

       */
      public DatabaseConnectionDefinition(
         String   strName,
         Database database,
         String   strDriver,
         String   strUrl,
         String   strUser,
         String   strPassword,
         int      iTransactionIsolation
      )
      {
         super(strName, database, strDriver, strUrl, strUser, strPassword, 
               iTransactionIsolation);
         
         m_iRequestedConnectionCount = 0;
      }
      
      /**
       * @return int
       */
      public int getRequestedConnectionCount()
      {
         return m_iRequestedConnectionCount;
      }

      /**
       * Method increments number of requested connections
       * 
       * @return int
       */
      public synchronized int connectionRequested()
      {
         return m_iRequestedConnectionCount++;
      }

      /**
       * Method decrements number of requested connections
       * 
       * @return int
       */
      public synchronized int connectionReturned()
      {
         if (GlobalConstants.ERROR_CHECKING)
         {
            assert m_iRequestedConnectionCount > 0 
                   : "Cannot return connection that wasn't requested.";
         }
         return m_iRequestedConnectionCount--;
      }
      
      /**
       * {@inheritDoc}
       */
      public String toString()
      {
         StringBuffer dump = new StringBuffer();
         
         dump.append("DatabaseConnectionDefinition[");
         dump.append("\n   ");
         dump.append(super.toString());
         dump.append("\n   m_iRequestedConnectionCount = ");
         dump.append(m_iRequestedConnectionCount);
         dump.append("]");
         
         return dump.toString();
      }      
   }

   // Configuration settings ///////////////////////////////////////////////////
   
   /** 
    * Name of the property specifying if to use a separate datasource name to 
    * obtain administration connection instead of just using the default data 
    * source with separate credentials. This have to be set to true when using 
    * J2EE capabilities in WebLogic 9.x. In this case the datasource doesn't 
    * have to have defined properties DATABASE_ADMIN_USER and 
    * DATABASE_ADMIN_PASSWORD since the properties identifying the regular user 
    * DATABASE_USER and DATABASE_PASSWORD will be used to get administration 
    * connection to the database.
    */   
   public static final String DATABASE_USE_ADMIN_DATAROURCE = "oss.dbaccess.admindatasource";
   
   // Constants ////////////////////////////////////////////////////////////////

   /**
    * Lock used in synchronized sections.
    */
   private static final String IMPL_LOCK = "IMPL_LOCK";

   /**
    * Default value for flag specifying if to use separate datasource for admin 
    * connections.
    */
   public static final Boolean DATABASE_USE_ADMIN_DATAROURCE_DEFAULT = Boolean.FALSE; 
   
   /**
    * Suffix that  will be appended to the regular datasource names if we are 
    * asked to use admin data source to get connection with administrative 
    * rights.
    */
   public static final String ADMIN_DATASOURCE_SUFFIX = "ADMIN"; 

   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(DatabaseConnectionFactoryImpl.class);
  
   /**
    * Reference to the default instance
    */
   private static DatabaseConnectionFactory s_defaultInstance = null;

   /**
    * This is concrete implementation of database transaction factory, which
    * will be used to manage transactions across connections allocated
    * by this database connection factory. This is a concrete class and not
    * and interface because these two classes are tightly coupled together
    * to make connections transaction aware.  
    */
   protected DatabaseTransactionFactoryImpl m_transactionFactory = null; 
   
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * How many connections were requested and not returned from all data sources.
    * This is here for debugging purposes to check if somebody is not returning
    * connection.
    */
   protected int m_iTotalRequestedConnectionCount;
   
   /**
    * Default data source used for all connections.
    */
   protected DatabaseConnectionDefinition m_defaultDataSource;
   
   /** 
    * Registered data sources. Key is String, data source name and value is 
    * DataSourceDefinition.
    */
   protected Map m_mpDataSources;

   /**
    * Map used to cross reference connection to their data sources so that we 
    * can correctly manage counts of requested connections per data source
    */
   protected Map m_mpConnectionDataSourceCrossRef;
   
   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Default constructor.
    */
   public DatabaseConnectionFactoryImpl(
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
   public DatabaseConnectionFactoryImpl(
      DatabaseTransactionFactoryImpl transactionFactory
   )
   {
      super();
      
      // Do not invoke loadDefaultDatabaseProperties here in case the derived 
      // class wants to use some different database properties. If it doesn't
      // it can call the method on it's own 
      m_iTotalRequestedConnectionCount = 0;
      m_transactionFactory = transactionFactory;
      
      // Use Hashtable so that it is synchronized
      m_mpDataSources = new Hashtable();
      m_mpConnectionDataSourceCrossRef = new Hashtable();
   }

   // Factory methods //////////////////////////////////////////////////////////

   /**
    * Get the default database connection factory. This method is here to make 
    * the database connection factory configurable. One can specify in a 
    * configuration file derived class to used instead of this one 
    * [DatabaseConnectionFactory.class]=new class to use
    *
    * @return DatabaseConnectionFactory
    * @throws OSSException - problem connecting to database 
    */
   public static DatabaseConnectionFactory getInstance(
   ) throws OSSException
   {
      if (s_defaultInstance == null)
      {
         // Only if the default connection factory wasn't set by other means, 
         // create a new one
         // Synchronize just for the creation
         synchronized (IMPL_LOCK)
         {
            if (s_defaultInstance == null)
            {
               DatabaseConnectionFactory  connectionFactory;
               DatabaseTransactionFactory transactionFactory; 

               transactionFactory = DatabaseTransactionFactoryImpl.getInstance();
               
               // Use DBCPDatabaseConnectionFactoryImpl as default data source 
               // since it seems simple and reliable passing all our tests.
               Class clsDefaultFactory;

               // TODO: Dependency: This creates compile time dependency on
               // connectionpool and j2ee packages. If we want to allow to build
               // and distribute without this packages this dependency needs to 
               // be removed
               
               clsDefaultFactory = DBCPDatabaseConnectionFactoryImpl.class;
               // Find out if we are running under a j2ee server. If yes, redefine
               // default variable defaultConnectionFactoryImpl to use 
               // j2ee database connection factory implementation.
               if (J2EEUtils.getJ2EEServerType() != J2EEUtils.J2EE_SERVER_NO)
               {
                  clsDefaultFactory = J2EEDatabaseConnectionFactoryImpl.class;
               }
               
               connectionFactory = (DatabaseConnectionFactory)ClassFactory
                                      .getInstance()
                                         .createInstance(
                                            DatabaseConnectionFactory.class,
                                            clsDefaultFactory);
               if ((connectionFactory instanceof DatabaseConnectionFactoryImpl)
                  && (transactionFactory instanceof DatabaseTransactionFactoryImpl))
               {
                  // Connect these two together so that the database connections
                  // can be properly managed by transaction factory
                  ((DatabaseConnectionFactoryImpl)connectionFactory)
                     .setTransactionFactory(
                        (DatabaseTransactionFactoryImpl)transactionFactory);
               }
               
               setInstance(connectionFactory);                  
            }
         }   
      }
      
      return s_defaultInstance;
   }
   
   /**
    * Set default database connection factory instance. This instance will be
    * returned by getInstance method until it is changed.
    *
    * @param defaultConnectionFactory - new default database
    * connection factory instance
    * @see #getInstance
    */
   public static void setInstance(
      DatabaseConnectionFactory defaultConnectionFactory
   )
   {
      if (GlobalConstants.ERROR_CHECKING)
      {         
         assert defaultConnectionFactory != null 
                : "Default database connection factory instance cannot be null";
      }   
      
      synchronized (IMPL_LOCK)
      {
         s_defaultInstance = defaultConnectionFactory;
         s_logger.fine("Default database connection factory is " 
                       + s_defaultInstance.getClass().getName());
      }   
   }

   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * {@inheritDoc}
    */
   public String toString(
   )
   {
      StringBuffer dump = new StringBuffer();
      
      dump.append("DatabaseConnectionFactoryImpl[");
      dump.append("\n   m_iRequestedConnectionCount = ");
      dump.append(m_iTotalRequestedConnectionCount);
      if (m_defaultDataSource != null)
      {
         dump.append("\n   m_defaultConnectionPool.getName() = ");
         dump.append(m_defaultDataSource.getName());
      }
      else
      {
         dump.append("\n   m_defaultConnectionPool = null");
      }
      dump.append("\n   m_mpDataSources = ");
      if (m_mpDataSources != null)
      {
         dump.append(m_mpDataSources);
      }
      else
      {
         dump.append("null");            
      }
      dump.append("]");

      return dump.toString();
   }   
   
   /**
    * {@inheritDoc}
    */
   public synchronized void stop(
   ) throws OSSException
   {
      // Now close all the data sources
      // Create a copy since the map will be modified as we close them
      List     lstDataSources = new ArrayList(m_mpDataSources.keySet());
      String   strDataSourceName;
      Iterator itrNames;
      for (itrNames = lstDataSources.iterator(); itrNames.hasNext();)
      {
         strDataSourceName = (String)itrNames.next();
         try
         {
            removeDataSource(strDataSourceName);
         }
         catch (Throwable thr)
         {
            // Catch throwable so that we can try to close others even if one fail 
            s_logger.log(Level.WARNING, "Cannot close data source " 
                         + strDataSourceName, thr);
         }      
      }
      
      if (GlobalConstants.ERROR_CHECKING)
      {
         assert m_mpConnectionDataSourceCrossRef.isEmpty()
                : "Not all connections from data sources were closed.";
         assert m_mpDataSources.isEmpty() 
                : "Not all data sources were closed.";
      }
      
      // Reset the default connection pool as well, if the pool is restarted next 
      // time, the default connection pool will have to be recreated
      m_iTotalRequestedConnectionCount = 0;
      m_defaultDataSource = null;
      // Closing data sources should have closed all connections that were 
      // retrieved from them, but just in case there is an issue and the asserts
      // are disabled, clear the buffers 
      m_mpConnectionDataSourceCrossRef.clear();
      m_mpDataSources.clear();
   }

   // Connection factory methods ///////////////////////////////////////////////
   // These methods ensure that the database connection factory is integrated
   // with database transaction factory so that the transaction factory can
   // properly manage connections created by this connection factory

   /**
    * {@inheritDoc}
    */
   public final Connection requestConnection(
      boolean bAutoCommit
   ) throws OSSException
   {
      Connection con;
      
      if (m_transactionFactory == null)
      {
         // This is not the preferred situation, the connection which will be 
         // returned will not be managed by transaction factory
         s_logger.finest("Requesting connection without transaction factory.");
         con = requestNonTransactionalConnection(bAutoCommit);         
      }
      else
      {
         // Let the transactional factory return transactional connection
         // by allowing it to call back us to get non transactional connection
         con = m_transactionFactory.requestTransactionalConnection(bAutoCommit, 
                                       null, null, null, this);
      }
      
      return con;
   }   
   
   /**
    * {@inheritDoc}
    */
   public final Connection requestConnection(
      boolean bAutoCommit,
      String  strUser, 
      String  strPassword
   ) throws OSSException
   {
      Connection con;
      
      if (m_transactionFactory == null)
      {
         // This is not the preferred situation, the connection which will be 
         // returned will not be managed by transaction factory
         s_logger.finest("Requesting connection without transaction factory.");
         con = requestNonTransactionalConnection(bAutoCommit, strUser, strPassword);
      }
      else
      {
         // Let the transactional factory return transactional connection
         // by allowing it to call back us to get non transactional connection
         con = m_transactionFactory.requestTransactionalConnection(bAutoCommit, 
                                       null, strUser, strPassword, this);
      }
      
      return con;
   }
      
   /**
    * {@inheritDoc}
    */
   public final Connection requestConnection(
      boolean bAutoCommit,
      String  strDataSourceName
   ) throws OSSException
   {
      Connection con;
      
      if (m_transactionFactory == null)
      {
         // This is not the preferred situation, the connection which will be 
         // returned will not be managed by transaction factory
         s_logger.finest("Requesting connection without transaction factory.");
         con = requestNonTransactionalConnection(bAutoCommit, strDataSourceName);
      }
      else
      {
         // Let the transactional factory return transactional connection
         // by allowing it to call back us to get non transactional connection
         con = m_transactionFactory.requestTransactionalConnection(bAutoCommit, 
                                       strDataSourceName, null, null, this);
      }
      
      return con;
   }
   
   /**
    * {@inheritDoc}
    */
   public final Connection requestConnection(
      boolean bAutoCommit,
      String  strDataSourceName, 
      String  strUser, 
      String  strPassword
   ) throws OSSException
   {
      Connection con;
      
      if (m_transactionFactory == null)
      {
         // This is not the preferred situation, the connection which will be 
         // returned will not be managed by transaction factory
         s_logger.finest("Requesting connection without transaction factory.");
         con = requestNonTransactionalConnection(bAutoCommit, strDataSourceName, 
                                                 strUser, strPassword);
      }
      else
      {
         // Let the transactional factory return transactional connection
         // by allowing it to call back us to get non transactional connection
         con = m_transactionFactory.requestTransactionalConnection(bAutoCommit, 
                                       strDataSourceName, strUser, strPassword, 
                                       this);
      }
      
      return con;
   }

   /**
    * {@inheritDoc}
    */
   public final void returnConnection(
      Connection cntDBConnection
   )
   {
      if (m_transactionFactory == null)
      {
         // This is not prefered situation, the connection which will be returned
         // will not be managed by transaction factory
         s_logger.finest("Returning connection without transaction factory.");
         returnNonTransactionalConnection(cntDBConnection);
      }
      else
      {
         // Let the transactional factory decide if the connection should be 
         // returned by allowing it to call us back to return any non transactional
         // connection
         m_transactionFactory.returnTransactionalConnection(cntDBConnection, this);
      }      
   }

   /**
    * Initialize connection to the default state so it can be used to work with 
    * the database. The most common setup is that we will set the autocommit
    * by default to true when transaction is not in progress and to false if
    * there is a pending transaction. This is mainly because if selects are 
    * executed without transaction and transaction isolation is set to for 
    * example serializable, it can come to a deadlock since the select may 
    * block table when different connection tries to modify it.
    *
    * @param bAutoCommit - The desired autocommit state of the connection. If 
    *                      this connection is invoked in global (JTA) transaction
    *                      then the autocommit is false regardless of what
    *                      value is specified here. Use true here if the client
    *                      only reads the data and false if the client also 
    *                      modifies the data. 
    *                      Use DatabaseTransactionFactory.commitTransaction
    *                      to commit the transaction.
    * @param cntDBConnection - valid connection to the database
    * @throws SQLException - problem initializing the connection
    */
   public void initializeConnection(
      Connection cntDBConnection,
      boolean    bAutoCommit      
   ) throws SQLException
   {
      try
      {
         DatabaseSourceDefinition definition;
         
         definition = getDataSource(cntDBConnection);
            
         if ((m_transactionFactory != null) 
            && (m_transactionFactory.isTransactionInProgress()))
         {
            // If we are in transaction disable autocommit.
            if (m_transactionFactory.isTransactionMonitored())
            {
               s_logger.finest("Setting autocommit to false. Requested autocommit"
                               + bAutoCommit + " and transaction is in progress.");
            }
            cntDBConnection.setAutoCommit(false);
         }
         else
         {
            // If we are not in transaction set the autocommit to the desired state
            if ((m_transactionFactory != null) 
                && (m_transactionFactory.isTransactionMonitored()))
            {
               s_logger.finest("Setting autocommit to requested autocommit "
                               + bAutoCommit + " since no transaction is in progress.");
            }
            cntDBConnection.setAutoCommit(bAutoCommit);
         }
         
         if (definition != null)
         {
            int iTransactionIsolation = definition.getTransactionIsolation();
            
            // -1 means do not set transaction isolation because dbms doesn't
            // support it. We also have to check that we are really changing
            // the transaction isolation since some databases
            if ((iTransactionIsolation != -1)
               && (cntDBConnection.getTransactionIsolation() != iTransactionIsolation))
            {
               cntDBConnection.setTransactionIsolation(iTransactionIsolation);
            }
         }
         else
         {
            s_logger.warning("Cannot set transaction isolation since no"
                             + " datasource definition could be found for the"
                             + " specified connection.");
         }
      }
      catch (SystemException sExc)
      {
         throw new SQLException("Error while getting transaction status.");
      }
      catch (OSSException ossExc)
      {
         throw new SQLException("Error while getting transaction status.");
      }
   }

   // Configuration methods ////////////////////////////////////////////////////
   
   /**
    * {@inheritDoc}
    */
   public final String getDefaultDataSourceName(
   )
   {
      String strName;
      
      if (m_defaultDataSource != null)
      {
         strName = m_defaultDataSource.getName();
      }
      else
      {
         strName = "";
      }
      
      return strName;
   }

   /**
    * {@inheritDoc}
    */
   public synchronized final void setDefaultDataSource(
      String strDataSourceName
   )
   {
      DatabaseConnectionDefinition definition;
      
      // Don't assign it to the member variable so that we don't override
      // existing data source if there is one
      definition = (DatabaseConnectionDefinition)m_mpDataSources.get(strDataSourceName);
      if (definition == null)
      {
         throw new IllegalArgumentException("Data source with name " 
                                            + strDataSourceName 
                                            + " doesn't exist.");         
      }      

      m_defaultDataSource = definition;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isDataSourceDefined(
      String strDataSourceName
   )
   {
      return m_mpDataSources.containsKey(strDataSourceName);
   }
   
   /**
    * {@inheritDoc}
    */
   public DatabaseSourceDefinition getDefaultDataSource(
   ) throws OSSException
   {
      if (m_defaultDataSource == null)
      {
         start();
      }
      
      return m_defaultDataSource;
   }

   /**
    * {@inheritDoc}
    */
   public DatabaseSourceDefinition getDataSource(
      String strDataSourceName
   ) throws OSSException
   {
      if (m_defaultDataSource == null)
      {
         start();
      }
      
      return (DatabaseSourceDefinition)m_mpDataSources.get(strDataSourceName);
   }

   /**
    * {@inheritDoc}
    */
   public DatabaseSourceDefinition getDataSource(
      Connection cntDBConnection
   ) throws OSSException
   {
      if (m_defaultDataSource == null)
      {
         start();
      }
      
      return (DatabaseSourceDefinition)m_mpConnectionDataSourceCrossRef.get(
                                          cntDBConnection);
   }
   
   /**
    * Set the transaction factory for this connection factory. This can be done
    * only if the transaction factory is not set yet (e.g. using constructor).
    * 
    * @param transactionFactory - the transaction factory to set.
    */
   public synchronized void setTransactionFactory(
      DatabaseTransactionFactoryImpl transactionFactory
   )
   {
      if (GlobalConstants.ERROR_CHECKING)
      {
         assert m_transactionFactory == null
                : "Transaction factory can be set only if it is not set.";
      }
      
      m_transactionFactory = transactionFactory;
   }   
   
   /**
    * Get number for connections which are currently requested and were not 
    * returned.
    * 
    * @return int - how many connections were currently requested from this 
    *               connection factory and were not returned yet.
    */
   public int getTotalRequestedConnectionCount(
   )
   {
      return m_iTotalRequestedConnectionCount;
   }
  
   /**
    * {@inheritDoc}
    */
   public int getRequestedConnectionCount()
   {
      return m_defaultDataSource.getRequestedConnectionCount();
   }

   /**
    * {@inheritDoc}
    */
   public int getRequestedConnectionCount(
      String strDataSourceName
   )
   {
      DatabaseConnectionDefinition definition;
      int                  iConnectionCountReturn = 0;

      // Don't assign it to the member variable so that we don't override
      // existing data source if there is one
      definition = (DatabaseConnectionDefinition)m_mpDataSources.get(strDataSourceName);
      if (definition == null)
      {
         throw new IllegalArgumentException("Data source with name " 
                                            + strDataSourceName 
                                            + " doesn't exist.");         
      }
      else
      {
         iConnectionCountReturn = definition.getRequestedConnectionCount();
      }

      return iConnectionCountReturn;
   }

   /**
    * {@inheritDoc}
    */
   public final DatabaseSourceDefinition addDataSource(
      String strDataSourceName
   ) throws OSSException
   {
      return addDataSource(strDataSourceName, DatabaseImpl.getInstance());
   }
   
   /**
    * {@inheritDoc}
    */
   public final DatabaseSourceDefinition addDataSource(
      String   strDataSourceName,
      Database database
   ) throws OSSException
   {
       String                               strDriverName;
       String                               strRealDriverName;
       String                               strUrl;
       String                               strUser;
       String                               strPassword;
       String                               strTransactionIsolation;
       int                                  iTransactionIsolation;
       DatabaseConnectionFactorySetupReader setupReader;
       DatabaseSourceDefinition             dataSource;
       
       setupReader = new DatabaseConnectionFactorySetupReader(strDataSourceName,
                            database.getDatabaseTypeIdentifier());
       
       strDriverName = setupReader.getStringParameterValue(
          DatabaseConnectionFactorySetupReader.DATABASE_DRIVER);
       strUrl = setupReader.getStringParameterValue(
          DatabaseConnectionFactorySetupReader.DATABASE_URL);
       strUser = setupReader.getStringParameterValue(
          DatabaseConnectionFactorySetupReader.DATABASE_USER);
       strPassword = setupReader.getStringParameterValue(
          DatabaseConnectionFactorySetupReader.DATABASE_PASSWORD);
       strRealDriverName = setupReader.getRealDatabaseDriver();
       strTransactionIsolation = setupReader.getStringParameterValue(
          DatabaseConnectionFactorySetupReader.DATABASE_TRANSACTION_ISOLATION);
       iTransactionIsolation = DatabaseUtils.convertTransactionIsolationToConstant(
                                                strTransactionIsolation);
       // Not every database supports every transaction isolation level so let
       // database tells us if this level us supported
       iTransactionIsolation = DatabaseImpl.getInstance().getTransactionIsolation(
                                  iTransactionIsolation);
       
       // Try load database driver and establish connection to the database
       initializeDriver(strDriverName);
       if (strRealDriverName != null)
       {
          initializeDriver(strRealDriverName);
       }
       
       dataSource = addDataSource(strDataSourceName, database, strDriverName, 
                                  strUrl, strUser, strPassword, 
                                  iTransactionIsolation);
       dataSource.setRealDriver(strRealDriverName);
       
       return dataSource;
   }
   
   /**
    * {@inheritDoc}
    */
   public synchronized final DatabaseSourceDefinition addDataSource(
      String   strDataSourceName,
      Database database,
      String   strDriverName,
      String   strUrl,
      String   strUser,
      String   strPassword,
      int      iTransactionIsolation
   ) throws OSSException
   {
      DatabaseConnectionDefinition dataSource;
      
      dataSource = (DatabaseConnectionDefinition)m_mpDataSources.get(
                                                    strDataSourceName); 
      if (dataSource == null)
      {
         // Data source with this name doesn't exists yet
         dataSource = createDataSource(strDataSourceName, database, 
                                       strDriverName, strUrl, strUser, 
                                       strPassword, iTransactionIsolation);      
         m_mpDataSources.put(strDataSourceName, dataSource);
         s_logger.fine("Data source " + strDataSourceName + " registered.");
      }
      else
      {
         s_logger.warning("Data source " + strDataSourceName + " already exists"
                          + " and it wasn't added second time.");
         throw new IllegalArgumentException("Data source " + strDataSourceName 
                                            + " already exists.");
      }
      
      return dataSource;
   }
   
   /**
    * {@inheritDoc}
    */
   public synchronized final void removeDataSource(
      String strDataSourceName
   ) throws OSSException
   {
      DatabaseConnectionDefinition dataSource;
      
      dataSource = (DatabaseConnectionDefinition)m_mpDataSources.remove(strDataSourceName);
      if (dataSource == null)
      {
         throw new IllegalArgumentException("Data source " + strDataSourceName 
                                            + " doesn't exist.");
      }
      else
      {
         destroyDataSource(dataSource);
         s_logger.fine("Data source " + strDataSourceName 
                       + " was unregistered.");
      }
   }
   
   /**
    * {@inheritDoc}
    */
   public final Connection requestAdminConnection(
      boolean bAutoCommit
   ) throws OSSException
   {
      Connection cntConnection;
      
      if (m_defaultDataSource == null)
      {
         start();
      }

      if (m_defaultDataSource != null)
      {
         cntConnection = requestAdminConnection(bAutoCommit, 
                                                m_defaultDataSource.getName()); 
      }
      else 
      {
         throw new OSSDatabaseAccessException("Cannot get database connection" 
                                              + " since no default data source"
                                              + " is defined.");
      }
      
      return cntConnection;
   }
   
   /**
    * {@inheritDoc}
    */
   public final Connection requestAdminConnection(
      boolean bAutoCommit,
      String  strDataSourceName
   ) throws OSSException
   {
      boolean                              bUseAdminDataSource;
      Connection                           cntAdminDBConnection;
      bUseAdminDataSource = useAdminDataSource();
      
      if (bUseAdminDataSource)
      {
         StringBuffer sbBuffer = new StringBuffer(strDataSourceName);
         
         // Modify the data source name to add suffix to it identifying one that 
         // should be configured to get us connection with administration tights
         sbBuffer.append(ADMIN_DATASOURCE_SUFFIX);
         // Since we are using separate data source just request a regular 
         // connection
         cntAdminDBConnection  = requestConnection(bAutoCommit, 
                                                   sbBuffer.toString());
      }
      else
      {
         DatabaseConnectionFactorySetupReader setupReader;
         Database                             database;
         DatabaseSourceDefinition             dataSource;
         String                               strAdminUser;
         String                               strAdminPassword;
         
         dataSource = getDataSource(strDataSourceName);
         if (dataSource == null)
         {
            throw new IllegalArgumentException("Data source " + strDataSourceName 
                                               + " doesn't exist.");         
         }
         
         database = dataSource.getDatabase();
         // Check the default datasource to see if we should be using 
         // administrative datasource instead
         setupReader = new DatabaseConnectionFactorySetupReader(
                              strDataSourceName,
                              database.getDatabaseTypeIdentifier());
         
         // It is OK to use the default datasource so just use different
         // credentials. Since these settings are not used very often if ever
         // read them only if a connection for these settings is requested.
         strAdminUser = setupReader.getStringParameterValue(
            DatabaseConnectionFactorySetupReader.DATABASE_ADMIN_USER);
         strAdminPassword = setupReader.getStringParameterValue(
            DatabaseConnectionFactorySetupReader.DATABASE_ADMIN_PASSWORD);
         
         cntAdminDBConnection  = requestConnection(bAutoCommit, 
                                                   strDataSourceName, 
                                                   strAdminUser, 
                                                   strAdminPassword);
      }
      
      return cntAdminDBConnection;         
   }
   
   // Helper methods ///////////////////////////////////////////////////////////

   /**
    * Start the connection factory.
    * 
    * @throws OSSException - an error has occurred
    */
   protected synchronized void start(
   ) throws OSSException
   {
      addDataSource(DEFAULT_DATASOURCE_NAME);
      setDefaultDataSource(DEFAULT_DATASOURCE_NAME);
   }
    
   /**
    * Create new data source with specified parameters. 
    * 
    * @param strDataSourceName - data source name 
    * @param database - database for which the datasource is being created. 
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
    * @return DataSourceDefinition - newly created data source
    * @throws OSSException - an error has occurred 
    */
   protected DatabaseConnectionDefinition createDataSource(
      String   strDataSourceName,
      Database database,
      String   strDriverName,
      String   strUrl,
      String   strUser,
      String   strPassword,
      int      iTransactionIsolation
   ) throws OSSException
   {
      return new DatabaseConnectionDefinition(strDataSourceName, database, 
                                              strDriverName, strUrl, strUser, 
                                              strPassword, iTransactionIsolation);
   }

   /**
    * Destroy the specified data source 
    * 
    * @param dataSource - data source to destroy
    * @throws OSSException - an error has occurred
    */ 
   protected void destroyDataSource(
      DatabaseConnectionDefinition dataSource
   ) throws OSSException
   {
      String               strName;
      Iterator             entries;
      Map.Entry            entry;
      DatabaseConnectionDefinition currentDataSource;
      Connection           currentConnection;        

      strName = dataSource.getName();
      // Close all connections for this data source for which we still have 
      // references to
      for (entries = m_mpConnectionDataSourceCrossRef.entrySet().iterator(); 
           entries.hasNext();)
      {
         entry = (Map.Entry)entries.next();
         
         currentDataSource = (DatabaseConnectionDefinition)entry.getValue();
         currentConnection = (Connection)entry.getKey(); 
         if (currentDataSource.getName().equals(strName))
         {
            try
            {
               if (!currentConnection.isClosed())
               {
                  try
                  {  
                     currentConnection.close();
                  }
                  catch (Throwable thr)
                  {
                     // Catch throwable so that we can try to close others even 
                     // if one fail
                     s_logger.log(Level.WARNING, 
                                  "Cannot close connection from data source " 
                                  + strName, thr);
                  }
               }
            }
            catch (Throwable thr)
            {
               // Catch the exception so that we can close the rest
               s_logger.log(Level.FINEST, 
                            "An unexpected error has encountered when checking"
                            + " if connection is closed.",
                            thr);
            }
            entries.remove();
         }
      }
   }
   
   /**
    * Initialize specified database driver.
    * 
    * @param strDatabaseDriver - class name of JDBC driver
    * @throws OSSDatabaseAccessException - error initializing the driver
    */
   protected void initializeDriver(
      String strDatabaseDriver
   ) throws OSSDatabaseAccessException
   {
      // Try load database driver and establish connection to the database
      try
      {
         Class.forName(strDatabaseDriver).newInstance();
      }
      catch (ClassNotFoundException cnfExc)
      {
          // A call to Class.forName() forces us to consider this exception :-)...
         throw new OSSDatabaseAccessException("Cannot load JDBC driver " 
                                             + strDatabaseDriver,
                                             cnfExc);
      }
      catch (IllegalAccessException iaeExc)
      {
          // A call to Class.forName() forces us to consider this exception :-)...
         throw new OSSDatabaseAccessException("Cannot access JDBC driver " 
                                             + strDatabaseDriver,
                                             iaeExc);
      }
      catch (InstantiationException ieExc)
      {
          // A call to Class.forName() forces us to consider this exception :-)...
         throw new OSSDatabaseAccessException("Cannot instantiate JDBC driver " 
                                             + strDatabaseDriver,
                                             ieExc);
      }
   }
   
   /**
    * This method is a delegation method for now final
    * org.opensubsystems.core.persist.db.DatabaseConnectionFactory#requestConnection()
    * to get connection which is not tied to the transactional manager.
    *
    * This method is protected so that only classes from this package can access 
    * it.
    *
    * @param bAutoCommit - The desired autocommit state of the connection. If 
    *                      this connection is invoked in global (JTA) transaction
    *                      then the autocommit is false regardless of what
    *                      value is specified here. Use true here if the client
    *                      only reads the data and false if the client also 
    *                      modifies the data. 
    *                      Use DatabaseTransactionFactory.commitTransaction
    *                      to commit the transaction.
    * @return Connection - connection to a database, never null
    * @see #returnConnection
    * @throws OSSException - an error has occurred
    */
   protected synchronized Connection requestNonTransactionalConnection(
      boolean bAutoCommit
   ) throws OSSException
   {
      Connection cntDBConnection;

      // If somebody is requesting connection the normal behavior is that
      // he will get it. If he doesn't get it, thats an exception. Therefore
      // I decide to don't return null since then everybody has to check
      // for the null value and throw an exception instead
      if (m_defaultDataSource == null)
      {
         start();
      }

      if (m_defaultDataSource != null)
      {
         // Instead of directly getting connection, delegate it to separate 
         // method so that we can implement different behaviors based on the 
         // type of the data source. This is not a method on a data source
         // so that whoever define new data connection factory doesn't have to
         // override two classes, this and the data source
         cntDBConnection = getConnection(m_defaultDataSource);
         connectionHouseKeepingOnRequest(cntDBConnection, m_defaultDataSource, 
                                         bAutoCommit);
      }
      else
      {
         throw new OSSDatabaseAccessException("Cannot get database connection" 
                                              + " since no default data source"
                                              + " is defined.");
      }         

      return cntDBConnection;
   }
   
   /**
    * This method is a delegation method for now final
    * org.opensubsystems.core.persist.db.DatabaseConnectionFactory
    * #requestConnection(String, String)
    * to get connection which is not tied to the transactional manager.
    *
    * This method is protected so that only classes from this package can access 
    * it.
    *
    * @param bAutoCommit - The desired autocommit state of the connection. If 
    *                      this connection is invoked in global (JTA) transaction
    *                      then the autocommit is false regardless of what
    *                      value is specified here. Use true here if the client
    *                      only reads the data and false if the client also 
    *                      modifies the data. 
    *                      Use DatabaseTransactionFactory.commitTransaction
    *                      to commit the transaction.
    * @param  strUser - user name to connect to the database
    * @param  strPassword - password to the database
    * @return Connection - connection to a database, never null
    * @see #returnConnection
    * @throws OSSException - an error has occurred
    */
   protected synchronized Connection requestNonTransactionalConnection(
      boolean bAutoCommit,
      String  strUser, 
      String  strPassword
   ) throws OSSException
   {
      Connection cntDBConnection;

      // If somebody is requesting connection the normal behavior is that
      // he will get it. If he doesn't get it, thats an exception. Therefore
      // I decide to don't return null since then everybody has to check
      // for the null value and throw an exception instead
      if (m_defaultDataSource == null)
      {
         start();
      }

      if (m_defaultDataSource != null)
      {
         if ((m_defaultDataSource.getUser().equals(strUser))
            && (m_defaultDataSource.getPassword().equals(strPassword)))
         {
            // The user name and password are the same as for regular data 
            // source so just use the regular method to get the connection
            cntDBConnection = requestNonTransactionalConnection(bAutoCommit);
         }
         else
         {
            // Instead of directly getting connection, delegate it to separate 
            // method so that we can implement different behaviors based on the 
            // type of the data source. This is not a method on a data source
            // so that whoever define new data connection factory doesn't have 
            // to override two classes, this and the data source
            cntDBConnection = getConnection(m_defaultDataSource,
                                            strUser, strPassword);
            connectionHouseKeepingOnRequest(cntDBConnection, m_defaultDataSource, 
                                            bAutoCommit);
         }
      }
      else
      {
         throw new OSSDatabaseAccessException("Cannot get database connection" 
                                              + " since no default data source"
                                              + " is defined.");
      }         

      return cntDBConnection;
   }
   
   /**
    * This method is a delegation method for now final
    * org.opensubsystems.core.persist.db.DatabaseConnectionFactory
    * #requestConnection(String)
    * to get connection which is not tied to the transactional manager.
    *
    * This method is protected so that only classes from this package can access it.
    *
    * @param bAutoCommit - The desired autocommit state of the connection. If 
    *                      this connection is invoked in global (JTA) transaction
    *                      then the autocommit is false regardless of what
    *                      value is specified here. Use true here if the client
    *                      only reads the data and false if the client also 
    *                      modifies the data. 
    *                      Use DatabaseTransactionFactory.commitTransaction
    *                      to commit the transaction.
    * @param strDataSourceName - data source which will be used to get connections
    * @return Connection - connection to a database, never null
    * @see #returnConnection
    * @throws OSSException - an error has occurred
    */
   protected synchronized Connection requestNonTransactionalConnection(
      boolean bAutoCommit,
      String  strDataSourceName
   ) throws OSSException
   {
      DatabaseConnectionDefinition dataSource;
      Connection           cntDBConnection;

      dataSource = (DatabaseConnectionDefinition)m_mpDataSources.get(strDataSourceName);
      if (dataSource == null)
      {
         throw new IllegalArgumentException("Data source " + strDataSourceName 
                                            + " doesn't exist.");         
      }
      else
      {
         cntDBConnection = getConnection(dataSource);
         connectionHouseKeepingOnRequest(cntDBConnection, dataSource, 
                                         bAutoCommit);
      }

      return cntDBConnection;
   }

   /**
    * This method is a delegation method for now final
    * org.opensubsystems.core.persist.db.DatabaseConnectionFactory
    * #requestConnection(String, String, String)
    * to get connection which is not tied to the transactional manager.
    *
    * This method is protected so that only classes from this package can access it.
    *
    * @param bAutoCommit - The desired autocommit state of the connection. If 
    *                      this connection is invoked in global (JTA) transaction
    *                      then the autocommit is false regardless of what
    *                      value is specified here. Use true here if the client
    *                      only reads the data and false if the client also 
    *                      modifies the data. 
    *                      Use DatabaseTransactionFactory.commitTransaction
    *                      to commit the transaction.
    * @param strDataSourceName - data source which will be used to get connections
    * @param strUser - user name to connect to the database
    * @param strPassword - password to the database
    * @return Connection - connection to a database, never null
    * @see #returnConnection
    * @throws OSSException - an error has occurred
    */
   protected synchronized Connection requestNonTransactionalConnection(
      boolean bAutoCommit,
      String  strDataSourceName, 
      String  strUser, 
      String  strPassword
   ) throws OSSException
   {
      DatabaseConnectionDefinition dataSource;
      Connection           cntDBConnection;

      dataSource = (DatabaseConnectionDefinition)m_mpDataSources.get(strDataSourceName);
      if (dataSource == null)
      {
         throw new IllegalArgumentException("Data source " + strDataSourceName 
                                            + " doesn't exist.");         
      }
      else
      {
         cntDBConnection = getConnection(dataSource, strUser, strPassword);
         connectionHouseKeepingOnRequest(cntDBConnection, dataSource, 
                                         bAutoCommit);
      }

      return cntDBConnection;
   }

   /**
    * This method is a delegation method for now final
    * org.opensubsystems.core.persist.db.DatabaseConnectionFactory
    * #returnConnection(java.sql.Connection)
    * to return connection which is not tied to the transactional manager.
    *
    * This method is protected so that only classes from this package can access it.
    * 
    * @param cntDBConnection - connection to return, can be null
    */
   public synchronized void returnNonTransactionalConnection(
      Connection cntDBConnection
   )
   {
      if (cntDBConnection != null)
      {
         if (GlobalConstants.ERROR_CHECKING)
         {
            try
            {
               if (cntDBConnection.isClosed())
               {
                  assert false : "Returning closed connection.";
               }
            }
            catch (SQLException sqleExc)
            {
               s_logger.log(Level.WARNING, 
                                   "Cannot check if database connection is closed", 
                                   sqleExc);
            }
         }

         /*
         Disabled, because it is not probably necessary to do this and we let the
         driver deal with this
         // Before we close the connection, let's rollback any changes we may have
         // made that are not committed
         try
         {
            cntDBConnection.rollback();
         }
         catch (SQLException sqleExc)
         {
            Log.getLogger().log(Level.WARNING, 
                                "Rollback before closing connection has failed.", 
                                sqleExc);
         }
         */

         // Decrease total number of requested connections   
         if (GlobalConstants.ERROR_CHECKING)
         {
            assert m_iTotalRequestedConnectionCount > 0 
                   : "Cannot return connection that wasn't requested.";
         }
         
         m_iTotalRequestedConnectionCount--;
         // Figure out from what data source was the connection retrieved and
         // adjust the count
         DatabaseConnectionDefinition dataSource;
         
         dataSource 
            = (DatabaseConnectionDefinition)m_mpConnectionDataSourceCrossRef.remove(
                                       cntDBConnection);
         if (dataSource != null)
         {
            // Decrease number of connections for particular data source
            dataSource.connectionReturned();
         }
         
         returnConnection(cntDBConnection, dataSource);
      }
   }
   
   /**
    * Get connection from the specified data source
    * 
    * @param dataSource - data source, from which to get connection
    * @return Connection - database connection from the specified data source
    * @throws OSSException - an error has occurred
    */
   protected Connection getConnection(
      DatabaseConnectionDefinition dataSource
   ) throws OSSException
   {
      Connection cntDBConnection;
      
      // Get the connection directly from the driver manager based on information
      // from the data source
      try
      {
         cntDBConnection = DriverManager.getConnection(dataSource.getUrl(),
                                                       dataSource.getUser(),
                                                       dataSource.getPassword());
      }
      catch (SQLException e)
      {
         throw new OSSDatabaseAccessException("Cannot get database connection" 
                                              + " from data source " 
                                              + dataSource.getName());
      }         
      
      return cntDBConnection;
   }
   
   /**
    * Get connection from the specified data source
    * 
    * @param dataSource - data source, from which to get connection
    * @param strUser - user name to connect to the database
    * @param strPassword - password to the database
    * @return Connection - database connection from the specified data source
    * @throws OSSException - an error has occurred
    */
   protected Connection getConnection(
      DatabaseConnectionDefinition dataSource,
      String                       strUser,
      String                       strPassword
   ) throws OSSException
   {
      Connection cntDBConnection;
      
      // Get the connection directly from the driver manager based on information
      // from the data source and the specified credentials
      try
      {
         cntDBConnection = DriverManager.getConnection(dataSource.getUrl(),
                                                       strUser, strPassword);
      }
      catch (SQLException sqlExc)
      {
         throw new OSSDatabaseAccessException("Cannot get database connection" 
                                              + " from data source " 
                                              + dataSource.getName()
                                              + " for explicitely specified user",
                                              sqlExc);
      }         
      
      return cntDBConnection;
   }
   
   /**
    * Return connection to the specified data source. This method is invoked
    * after all the house keeping was done so all what needs to be done
    * is take care of the connection.
    * 
    * @param cntDBConnection - connection that is being returned, it is safe to 
    *                          assume that the connection wasn't closed yet
    * @param dataSource - data source to which the connection belongs. It can be
    *                     null, if the data source can not be identified.
    */
   protected void returnConnection(
      Connection           cntDBConnection,
      DatabaseConnectionDefinition dataSource
   )
   {
      // We cannot do anything special other than just close the connection
      try
      {
         cntDBConnection.close();
      }
      catch (SQLException sqleExc)
      {
         s_logger.log(Level.WARNING, "Closing of connection has failed.", 
                      sqleExc);
      }
   }
   
   /**
    * Update internal state of this factory when connection was requesed for
    * a specified data source
    * 
    * @param cntDBConnection - newly requested connection
    * @param dataSource - data source from which the connection was requested
    * @param bAutoCommit - The desired autocommit state of the connection. If 
    *                      this connection is invoked in global (JTA) transaction
    *                      then the autocommit is false regardless of what
    *                      value is specified here. Use true here if the client
    *                      only reads the data and false if the client also 
    *                      modifies the data. 
    *                      Use DatabaseTransactionFactory.commitTransaction
    *                      to commit the transaction.
    * @throws OSSException - an error has occurred
    */
   protected synchronized void connectionHouseKeepingOnRequest(
      Connection                   cntDBConnection,
      DatabaseConnectionDefinition dataSource,
      boolean                      bAutoCommit
   ) throws OSSException
   {
      if (cntDBConnection == null)
      {
         throw new OSSDatabaseAccessException("Cannot get database connection" 
                                              + " for an unknown reason");            
      }

      // Increase total number of requested connections   
      m_iTotalRequestedConnectionCount++;

      // Increase number of connections for particular data source 
      // and associate them together so that when connection is returned
      // we can decrement count from correct data source
      m_mpConnectionDataSourceCrossRef.put(cntDBConnection, dataSource);
      dataSource.connectionRequested();

      try
      {
         initializeConnection(cntDBConnection, bAutoCommit);
      }
      catch (SQLException sqleExc)
      {
         // Do not return the connection here since user MUST call 
         // returnConnection even if exception occurs during requestConnection
         throw new OSSDatabaseAccessException(
                      "Cannot initialize database connection", sqleExc);            
      }
   }
   
   /**
    * Check if to use a separate datasource name to obtain administration 
    * connection instead of just using the default data source with separate 
    * admin credentials. This have to be set to true when using J2EE 
    * capabilities in WebLogic 9.x.
    * 
    * @return boolean - true if to use separate admin data source to get 
    *                   connection with administration rights, false otherwise
    */
   public static boolean useAdminDataSource(
   )
   {
      // TODO: Performance: Since we are reading it all the time this may impact
      // performance
      
      // Read it here instead of in static block or constructor since if this 
      // code is executed in different execution context, it might have 
      // different configuration settings.
      Properties prpSettings;
      Boolean    bUseAdminDataSource;
      
      prpSettings = Config.getInstance().getProperties();
      bUseAdminDataSource = PropertyUtils.getBooleanProperty(
                               prpSettings, DATABASE_USE_ADMIN_DATAROURCE, 
                               DATABASE_USE_ADMIN_DATAROURCE_DEFAULT, 
                               "Use separate datasource for admin connections");
      
      return bUseAdminDataSource.booleanValue();
   }
}

