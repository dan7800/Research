/*
 * Copyright (c) 2003 - 2009 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: DatabaseImpl.java,v 1.2 2009/07/18 04:57:54 bastafidli Exp $
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opensubsystems.core.error.OSSDatabaseAccessException;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.persist.db.Database;
import org.opensubsystems.core.persist.db.DatabaseConnectionFactory;
import org.opensubsystems.core.persist.db.DatabaseSchema;
import org.opensubsystems.core.persist.db.DatabaseSchemaManager;
import org.opensubsystems.core.persist.db.DatabaseSourceDefinition;
import org.opensubsystems.core.persist.db.VersionedDatabaseSchema;
import org.opensubsystems.core.persist.db.hsqldb.HsqlDBDatabaseImpl;
import org.opensubsystems.core.util.ClassFactory;
import org.opensubsystems.core.util.GlobalConstants;
import org.opensubsystems.core.util.Log;

/**
 * Base class for all classes representing various database management systems.
 * The main purpose for this and the derived classes is to encapsulate  
 * differences in behavior of these systems so that the applications working 
 * with them can be written in a generic fashion and delegate any operations
 * that differ between them back to the specific class containing custom 
 * implementation.
 * 
 * Most likely one project will use only one database therefore this class
 * contains factory methods to instantiate and return default instance of a
 * database even though it still allows multiple instances of database to exist
 * since all attributes are non-static.
 * 
 * @version $Id: DatabaseImpl.java,v 1.2 2009/07/18 04:57:54 bastafidli Exp $
 * @author Miro Halas
 * @code.reviewer Miro Halas
 * @code.reviewed 1.38 2008/06/26 04:12:07 bastafidli
 */
public abstract class DatabaseImpl implements Database
{
   // Constants ////////////////////////////////////////////////////////////////

   /** 
    * Number specifying maximal safe length for members within the IN () clause
    * Because there is limitation for sql statement length (in sap db is default 
    * 64 kb) and in() expression can contain lot of members.
    * 
    * TODO: Improve: Based on the comment above this variable cannot be just 
    * simple number and therefore we may somehow encapsulate behavior which
    * uses it to differ between databases. One splits the list by number of
    * items, other splits it by size of items.
    */   
   public static final int IN_CLAUSE_MAX_SAFE_LENGTH = 1000;

   /** 
    * Lock used in synchronization blocks. 
    */   
   private static final String IMPL_LOCK = "IMPL_LOCK";

   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(DatabaseImpl.class);

   /**
    * Reference to the default database.
    */
   private static Database s_defaultInstance = null;
   
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Flag signaling that the database schema was initialized;
    */
   protected boolean m_bDatabaseSchemaInitialized;
   
   /**
    * Flag signaling that the database start is in progress.
    */
   protected boolean m_bDatabaseStartInProgress;
   
   /**
    * Flag if database was started.
    */
   protected boolean m_bDatabaseStarted;
   
   /**
    * Database schema for this database. By default it is versioned database 
    * schema which manages all other schemas in the database.
    */
   protected VersionedDatabaseSchema m_vdsSchema;

   /**
    * Identifier for the type of database represented by this instance. This 
    * identifier can be used to construct package and class names for classes 
    * that implements database specific behavior. 
    */
   protected String m_strDatabaseTypeIdentifier;
   
   /**
    * Default string which can be used in SQL queries to retrieve timestamp 
    * representing the current date and time.
    */
   protected String m_strSQLCurrentTimestampFunctionCall;
   
   /**
    * Default string which can be used in SQL queries to retrieve record count.
    */
   protected String m_strSQLCountFunctionCall;
   
   /**
    * Flag specifying if when trying to find out size of the result set we should 
    * use rather count(*)/count(1)/count(id) instead of using last() if the
    * hasAbsolutePositioningSupport() indicates the database has support for it.
    */
   protected boolean m_bDefaultPreferCountToLast;
   
   /**
    * Flag specifying if database support limiting the range of rows retrieved 
    * by a query. This means that database has to provide a way how to construct 
    * EFFICIENT SQL that allows us to retrieve items starting from row X and 
    * ending at row Y from the result set containing further criteria.
    */
   protected boolean m_bDefaultRangeSupport;
   
   /**
    * Default value for connection test statement.
    */
   protected String m_strConnectionTestStatement;
   
   /**
    * Actual value for result set type when selecting lists.
    */
   protected Integer m_iSelectListResultSetType;
   
   /**
    * Actual value for result set concurrency when selecting lists.
    */
   protected Integer m_iSelectListResultSetConcurrency;
   
   /**
    * Actual value for how many database operations to batch together
    */
   protected Integer m_iBatchSize;
   
   /**
    * Actual value for flag specifying range support when selecting lists.
    */
   protected Boolean m_bRangeSupport;
   
   /**
    * Actual value for flag specifying absolute positioning support.
    */
   protected Boolean m_bAbsolutePositioningSupport;
   
   /**
    * Actual value for flag specifying if to prefer count() to last().
    */
   protected Boolean m_bPreferCountToLast;
   
   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Default constructor. .
    * 
    * @param strDatabaseTypeIdentifier - the identifier for the type of database 
    *                                    represented by this instance.
    * @param strSQLCurrentTimestampFunctionCall - string which can be used in 
    *                                             SQL queries to retrieve 
    *                                             timestamp representing the 
    *                                             current date and time
    * @param strSQLCountFunctionCall - string which can be used in SQL queries 
    *                                  to retrieve record count
    * @param bPreferCountToLast - flag specifying if when trying to find out 
    *                             size of the result set we should use rather 
    *                             count(*)/count(1)/count(id) instead of using 
    *                             last() of the JDBC driver
    * @param bRangeSupport - flag specifying if database support limiting the 
    *                        range of rows retrieved by a query.
    * @throws OSSException - an error has occurred
    */
   public DatabaseImpl(
      String  strDatabaseTypeIdentifier,
      String  strSQLCurrentTimestampFunctionCall,
      String  strSQLCountFunctionCall,
      boolean bPreferCountToLast,
      boolean bRangeSupport
   ) throws OSSException
   {
      m_strDatabaseTypeIdentifier = strDatabaseTypeIdentifier;
      
      m_strSQLCurrentTimestampFunctionCall = strSQLCurrentTimestampFunctionCall;
      m_strSQLCountFunctionCall = strSQLCountFunctionCall;
      // These are just default hints based on how the code prefers to function
      // but we can let user to modify these
      m_bDefaultPreferCountToLast = bPreferCountToLast;
      m_bDefaultRangeSupport = bRangeSupport;
      
      // We cannot initialize schema here because we would have circular 
      // dependency so wait and initialize it when it is needed
      m_vdsSchema = null;            
      m_bDatabaseSchemaInitialized = false;
      m_bDatabaseStartInProgress = false;
      m_bDatabaseStarted = false;
      
      DatabaseSetupReader setupReader = new DatabaseSetupReader(
         strDatabaseTypeIdentifier.toLowerCase(),
         // This table is very static, doesn't change and it is small so this 
         // query should be super fast
         "select count(*) from " + VersionedDatabaseSchemaImpl.SCHEMA_TABLE_NAME,
         // HSQLDB requires the statement to be ResultSet.TYPE_SCROLL_INSENSITIVE
         // and ResultSet.CONCUR_READ_ONLY to be able call last, etc. so lets
         // assume that is the default behavior for all databases for now
         ResultSet.TYPE_SCROLL_INSENSITIVE,
         // HSQL requires the statement to be ResultSet.TYPE_SCROLL_INSENSITIVE
         // and ResultSet.CONCUR_READ_ONLY to be able call last, etc. so lets
         // assume that is the default behavior for all databases for now
         ResultSet.CONCUR_READ_ONLY,
         m_bDefaultRangeSupport,
         // By default lets assume that database and database driver supports 
         // absolute positioning since it is part of the JDBC specification so 
         // that not every implementation has to implement this method even 
         // though it may not be the optimal way to manipulate the result set
         true,
         m_bDefaultPreferCountToLast
         );
      
      m_strConnectionTestStatement = setupReader.getStringParameterValue(
         DatabaseSetupReader.CONNECTION_TEST_STATEMENT);
      m_iSelectListResultSetType = setupReader.getIntegerParameterValue(
         DatabaseSetupReader.SELECT_LIST_RESULT_SET_TYPE);
      m_iSelectListResultSetConcurrency = setupReader.getIntegerParameterValue(
         DatabaseSetupReader.SELECT_LIST_RESULT_SET_CONCURRENCY);
      m_iBatchSize = setupReader.getIntegerParameterValue(
         DatabaseSetupReader.BATCH_SIZE);
      m_bRangeSupport = setupReader.getBooleanParameterValue(
         DatabaseSetupReader.RANGE_SUPPORT);
      m_bAbsolutePositioningSupport = setupReader.getBooleanParameterValue(
         DatabaseSetupReader.ABSOLUTE_POSITIONING_SUPPORT);
      m_bPreferCountToLast = setupReader.getBooleanParameterValue(
         DatabaseSetupReader.PREFER_COUNT_TO_LAST);
   }

   // Factory methods //////////////////////////////////////////////////////////
   
   /**
    * Get the default database instance.
    *
    * @return Database
    * @throws OSSException - problem accessing the database
    */
   public static Database getInstance(
   ) throws OSSException 
   {
      if (s_defaultInstance == null)
      {
         // Only if the default database wasn't set by other means, create a new 
         // one. Synchronize just for the creation
         synchronized (IMPL_LOCK)
         {
            if (s_defaultInstance == null)
            {
               // Use HSQLDB as a default database since it can be easily
               // bundled with the application
               setInstance((Database)ClassFactory.getInstance().createInstance(
                              Database.class, HsqlDBDatabaseImpl.class));
            }
         }   
      }
      
      return s_defaultInstance;
   }
   
   /**
    * Get the default database instance if it was started otherwise return null.
    *
    * @return Database - null if it wasn't started otherwise instance of the 
    *                    database
    * @throws OSSException - problem accessing the database
    */
   public static Database getInstanceIfStarted(
   ) throws OSSException
   {
      return ((s_defaultInstance != null) && (s_defaultInstance.isStarted())) 
             ? s_defaultInstance : null;  
   } 
   
   /**
    * Set default database instance. This instance will be returned by 
    * getInstance method until reset.
    *
    * @param dbDatabase - new default database instance
    * @see #getInstance
    */
   public static void setInstance(
      Database dbDatabase
   )
   {
      if (GlobalConstants.ERROR_CHECKING)
      {
         assert dbDatabase != null : "Default database instance cannot be null";
      }   
      
      synchronized (IMPL_LOCK)
      {
         s_defaultInstance = dbDatabase;
         s_logger.fine("Default database is " 
                       + s_defaultInstance.getClass().getName());
      }   
   }

   // Database administration //////////////////////////////////////////////////

   /**
    * {@inheritDoc}
    */
   public void start(
   ) throws OSSException
   {
      s_logger.entering(this.getClass().getName(), "start");
      
      try
      {
         if ((!m_bDatabaseStarted) || (!m_bDatabaseSchemaInitialized))
         {
            m_bDatabaseStartInProgress = true;
            
            Connection cntDBConnection = null;
   
            try
            {
               try
               {
                  startDatabaseServer();
               }
               catch (OSSException osseExc)
               {
                  s_logger.log(Level.WARNING, 
                               "Cannot start database server, trying to create.",
                               osseExc);
                  try
                  {
                     createDatabaseInstance();
                  }
                  catch (OSSException osseExc1)
                  {
                     s_logger.log(Level.SEVERE, 
                                  "Error while creating database instance.",
                                  osseExc1);
                  }
               }
               // Request connection as normal user. If the database doesn't 
               // exist or it wasn't initialized (e.g. user doesn't exists) this 
               // will fail
               DatabaseConnectionFactory connectionFactory;
               
               connectionFactory = DatabaseConnectionFactoryImpl.getInstance();
               try
               {
                  // Request autocommit false since we might be modifying database
                  cntDBConnection = connectionFactory.requestConnection(false);
               }
               catch (OSSDatabaseAccessException osseExc)
               {
                  Connection cntAdminDBConnection = null;
                  
                  // We couldn't connect to database by default. It might mean,  
                  // that the user and or database doesn't exists
                  // Try to connect to the database create as administrator
                  // and create the database (if it can be done, e.g. HSQLDB 
                  // will create the database if we try to connect as 
                  // administrator) and/or create the regular user that will 
                  // be used by the rest of the application
                  try
                  {
 
                     // Request autocommit false since we are modifying database
                     cntAdminDBConnection = connectionFactory.requestAdminConnection(
                                                                 false);
      
                     s_logger.log(Level.FINER, 
                                         "Database exists or was just created.");
      
                     // Use the existing administrator connection to create 
                     // regular user
                     createUser(connectionFactory, cntAdminDBConnection);
                  }
                  finally
                  {
                     // We have created our own connection therefore we need to 
                     // return it since we wont need it anymore
                     connectionFactory.returnConnection(cntAdminDBConnection);                     
                  }

                  // Some connections pools (PROXOOL) have problems if when the 
                  // first time we request connections the user doesn't exist
                  // and they cannot recover from it. Just in case the current
                  // connection pool is one of those, restart it at this time
                  // since the user was created above 
                  connectionFactory.stop();
                  // Once the connection pool is stopped, it should restart 
                  // automatically when connection is requested again
                  
                  // Now I should be able to get connection to database as 
                  // given user since we want to create everything as that user
                  // Request autocommit false since we might be modifying database
                  cntDBConnection = connectionFactory.requestConnection(false);
                  if (cntDBConnection == null)
                  {
                     throw new OSSDatabaseAccessException(
                        "Cannot get connection to database after user was"
                        + " created.");
                  }
               }

               // Now initialize the schema, this will create all tables,
               // stored procedures and indexes
               if (m_vdsSchema != null)
               {
                  DatabaseSourceDefinition definition;
                  
                  definition = connectionFactory.getDefaultDataSource();
                  m_vdsSchema.init(cntDBConnection, definition.getUser());
               }
   
               // At this point we don't know if this is just a single operation
               // and we need to commit or if it is a part of bigger transaction
               // and the commit is not desired until all operations proceed. 
               // Therefore let the DatabaseTransactionFactory resolve it 
               DatabaseTransactionFactoryImpl.getInstance()
                                             .commitTransaction(cntDBConnection);

               m_bDatabaseSchemaInitialized = true;            
               s_logger.log(Level.FINER, "Database is initialized.");
            }
            catch (Throwable thr)
            {
               s_logger.log(Level.SEVERE, "Failed to initialize database.", 
                                   thr);
               if (cntDBConnection != null)
               {
                  try
                  {
                     // At this point we don't know if this is just a single 
                     // operation and we need to commit or if it is a part of 
                     // bigger transaction and the commit is not desired until 
                     // all operations proceed. Therefore let the 
                     // DatabaseTransactionFactory resolve it 
                     DatabaseTransactionFactoryImpl.getInstance()
                        .rollbackTransaction(cntDBConnection);
                  }
                  catch (SQLException sqleExc)
                  {
                     // Ignore this
                     s_logger.log(
                        Level.WARNING, 
                        "Failed to rollback changes for creation of database.", 
                        sqleExc);
                  }
               }
               throw new OSSDatabaseAccessException(
                            "Failed to initialize database.", thr);
            }
            finally
            {
               DatabaseConnectionFactoryImpl.getInstance().returnConnection(
                  cntDBConnection);
               m_bDatabaseStartInProgress = false;
            }
            m_bDatabaseStarted = true;   
         }      
      }
      finally
      {
         s_logger.exiting(this.getClass().getName(), "start");
      }
   }

   /**
    * {@inheritDoc}
    */
   public void stop(
   ) throws OSSException
   {
      // Stop any transaction factory
      DatabaseTransactionFactoryImpl.getInstance().stop();
      // Stop any connection factory
      DatabaseConnectionFactoryImpl.getInstance().stop();
      m_vdsSchema = (VersionedDatabaseSchema)DatabaseSchemaManager.getInstance(
                       VersionedDatabaseSchema.class);            
      m_bDatabaseStarted = false;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isStarted(
   )
   {
      return m_bDatabaseStarted;
   }

   // Schema management methods ////////////////////////////////////////////////
   
   /**
    * {@inheritDoc}
    */
   public void add(
      DatabaseSchema dsSchema
   ) throws OSSException 
   {
      if (GlobalConstants.ERROR_CHECKING)
      {
         assert dsSchema != null : "Cannot add null schema.";
      }
      
      if (m_vdsSchema == null)
      {
         m_vdsSchema = (VersionedDatabaseSchema)DatabaseSchemaManager
                       .getInstance(VersionedDatabaseSchema.class);            
      }
      m_vdsSchema.add(dsSchema);
      m_bDatabaseSchemaInitialized = false;

      // We have new schema, it needs to be reinitialized
      s_logger.log(Level.FINEST, "Database schema " + dsSchema.getName() 
                   + " added to the database versioned schema.");
   }  

   /**
    * {@inheritDoc}
    */
   public void add(
      Class clsSchema
   ) throws OSSException 
   {
      add(DatabaseSchemaManager.getInstance(clsSchema));
   }

   /**
    * {@inheritDoc}
    */
   public String getDatabaseTypeIdentifier(
   )
   {
      return m_strDatabaseTypeIdentifier;
   }

   /**
    * {@inheritDoc}
    */
   public int getTransactionIsolation(
      int iTransactionIsolation
   )
   {
      // By default database should support all isolations level so provide 
      // simple default implementation
      return iTransactionIsolation;
   }

   /**
    * {@inheritDoc}
    */
   public String getSQLCountFunctionCall()
   {
      return m_strSQLCountFunctionCall;
   }

   /**
    * {@inheritDoc}
    */
   public String getSQLCurrentTimestampFunctionCall()
   {
      return m_strSQLCurrentTimestampFunctionCall;
   }
   
   /**
    * {@inheritDoc}
    */
   public String getConnectionTestStatement()
   {
      return m_strConnectionTestStatement;
   }
   
   /**
    * {@inheritDoc}
    */
   public int getSelectListResultSetType(
   )
   {
      return m_iSelectListResultSetType.intValue();
   }
   
   /**
    * {@inheritDoc}
    */
   public int getSelectListResultSetConcurrency(
   )   
   {
      return m_iSelectListResultSetConcurrency.intValue();
   }
   
   /**
    * {@inheritDoc}
    */
   public int getBatchSize()
   {
      return m_iBatchSize.intValue();
   }
   
   /**
    * {@inheritDoc}
    */
   public boolean hasAbsolutePositioningSupport(
   )
   {
      return m_bAbsolutePositioningSupport.booleanValue();
   }

   /**
    * {@inheritDoc}
    */
   public boolean preferCountToLast(
   )
   {
      return m_bPreferCountToLast.booleanValue();
   }

   /**
    * {@inheritDoc}
    */
   public boolean hasRangeSupport(
   )
   {
      return m_bRangeSupport.booleanValue();
   }

   /**
    * {@inheritDoc}
    */
   public List getInListWithSafeLength(
      Collection idList,
      boolean    bQuote
   )
   {
      List         lstRetList = new ArrayList(
                                       idList.size() 
                                       / IN_CLAUSE_MAX_SAFE_LENGTH 
                                       + 1);
      int          count = 0;
      Iterator     inputIterator = idList.iterator();
      String       currData = null;
      StringBuffer dataString = new StringBuffer();
      
      while (inputIterator.hasNext())
      {
         // Get next data
         currData = ((Object)inputIterator.next()).toString();
         if (count == 0)
         {
            // Clear buffer
            dataString.delete(0, dataString.length());
         }
         else
         {
            // Add comma before next item
            dataString.append(",");
         }
         
         if (bQuote)
         {
            // Append left quote if it is allowed
            dataString.append("'");
         }
   
         // Append item to buffer
         dataString.append(currData);
   
         if (bQuote)
         {
            // Append right quote if it is allowed
            dataString.append("'");
         }
   
         count++;
         
         if (count == IN_CLAUSE_MAX_SAFE_LENGTH)
         {
            // Put buffer with specified number of items to output list
            lstRetList.add(dataString.toString());
            // Reset count
            count = 0;
         }
      }
      if (count > 0)
      {
         // Add rest (if any) to output list
         lstRetList.add(dataString.toString());
      }
      
      return lstRetList;
   }
   
   // Helper methods ///////////////////////////////////////////////////////////

   /**
    * Start the database server used by this database instance if it is not 
    * running yet.
    * 
    * @throws OSSException - problem starting the server
    */
   protected abstract void startDatabaseServer(
   ) throws OSSException;

   /**
    * Create the database represented by this database instance if it doesn't 
    * exist yet.
    * 
    * @throws OSSException - problem creating the database instance
    */
   protected abstract void createDatabaseInstance(
   ) throws OSSException;

   /**
    * Create database user which will be used by the applications to access
    * the database.
    * 
    * @param connectionFactory - connection factory that was used to get the 
    *                            admin connection 
    * @param cntAdminDBConnection - connection with rights to create users
    * @throws OSSException - cannot create user
    */
   protected abstract void createUser(
      DatabaseConnectionFactory connectionFactory, 
      Connection                cntAdminDBConnection
   ) throws OSSException;
}
