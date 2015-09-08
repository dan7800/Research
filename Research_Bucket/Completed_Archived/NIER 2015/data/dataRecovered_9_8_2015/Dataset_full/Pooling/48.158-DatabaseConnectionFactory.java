/*
 * Copyright (c) 2003 - 2009 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: DatabaseConnectionFactory.java,v 1.18 2009/04/22 05:38:41 bastafidli Exp $
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

package org.opensubsystems.core.persist.db;

import java.sql.Connection;

import org.opensubsystems.core.error.OSSException;

/**
 * Interface to encapsulate retrieving and returning of database connections.
 * Data connection factory is here to implement the Abstract Factory pattern 
 * as described by GoF95. The main reason is that database connections can be 
 * managed in different ways: they may be always opened when requested or pooled 
 * and retrieved from the pool, they might be created by the driver or retrieved 
 * from the data source. This interface provides unified way how to access the 
 * database connection regardless of implementation. 
 * 
 * @version $Id: DatabaseConnectionFactory.java,v 1.18 2009/04/22 05:38:41 bastafidli Exp $
 * @author Miro Halas
 * @code.reviewer Miro Halas
 * @code.reviewed 1.17 2008/06/26 04:13:29 bastafidli
 */
public interface DatabaseConnectionFactory
{
   // Constants ////////////////////////////////////////////////////////////////
   
   /**
    * Name that will be used for default datasource when the connection factory
    * is started and no default datasource is defined. Make this name somehow 
    * meaningful because for example for J2EE CF this name has to be configured
    * in external configuration file or using administration gui.
    */
   String DEFAULT_DATASOURCE_NAME = "OSSDS"; 

   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * Get connection to a database as configured by the default data source. 
    * The connection has to be explicitly returned using returnConnection most 
    * likely in the finally clause. 
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
   Connection requestConnection(
      boolean bAutoCommit
   ) throws OSSException;

   /**
    * Get connection to a database as configured by the default data source but 
    * using explicit user credentials. The connection has to be explicitly 
    * returned using returnConnection most likely in the finally clause.  
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
   Connection requestConnection(
      boolean bAutoCommit,
      String  strUser, 
      String  strPassword
   ) throws OSSException;

   /**
    * Get connection to a database as configured by the specified data source. 
    * The connection has to be explicitly returned using returnConnection most 
    * likely in the finally clause. 
    *
    * @param bAutoCommit - The desired autocommit state of the connection. If 
    *                      this connection is invoked in global (JTA) transaction
    *                      then the autocommit is false regardless of what
    *                      value is specified here. Use true here if the client
    *                      only reads the data and false if the client also 
    *                      modifies the data. 
    *                      Use DatabaseTransactionFactory.commitTransaction
    *                      to commit the transaction.
    * @param strDataSourceName - data source which will be used to get the 
    *                            connection
    * @return Connection - connection to a database, never null
    * @see #returnConnection
    * @throws OSSException - an error has occurred
    */
   Connection requestConnection(
      boolean bAutoCommit,
      String  strDataSourceName
   ) throws OSSException;

   /**
    * Get connection to a database as configured by the specified source but 
    * using explicit user credential. The connection has to be explicitly 
    * returned using returnConnection most likely in the finally clause. 
    *
    * @param bAutoCommit - The desired autocommit state of the connection. If 
    *                      this connection is invoked in global (JTA) transaction
    *                      then the autocommit is false regardless of what
    *                      value is specified here. Use true here if the client
    *                      only reads the data and false if the client also 
    *                      modifies the data. 
    *                      Use DatabaseTransactionFactory.commitTransaction
    *                      to commit the transaction.
    * @param strDataSourceName - data source which will be used to get the 
    *                            connection
    * @param strUser - user name to connect to the database
    * @param strPassword - password to the database
    * @return Connection - connection to a database, never null
    * @see #returnConnection
    * @throws OSSException - an error has occurred
    */
   Connection requestConnection(
      boolean bAutoCommit,
      String  strDataSourceName,
      String  strUser, 
      String  strPassword
   ) throws OSSException;

   
   /**
    * Get connection to a database as configured by the default data source but 
    * using administrative credentials. The connection has to be explicitly 
    * returned using returnConnection most likely in the finally clause. 
    *
    * @param bAutoCommit - The desired autocommit state of the connection. If 
    *                      this connection is invoked in global (JTA) transaction
    *                      then the autocommit is false regardless of what
    *                      value is specified here. Use true here if the client
    *                      only reads the data and false if the client also 
    *                      modifies the data. 
    *                      Use DatabaseTransactionFactory.commitTransaction
    *                      to commit the transaction.
    * @return Connection - connection with administrator privileges, never null
    * @see #returnConnection
    * @throws OSSException - an error has occurred
    */
   Connection requestAdminConnection(
      boolean bAutoCommit
   ) throws OSSException;
   
   /**
    * Get connection to a database as configured by the specified source but 
    * using administrative credentials. The connection has to be explicitly 
    * returned using returnConnection most likely in the finally clause. 
    *
    * @param bAutoCommit - The desired autocommit state of the connection. If 
    *                      this connection is invoked in global (JTA) transaction
    *                      then the autocommit is false regardless of what
    *                      value is specified here. Use true here if the client
    *                      only reads the data and false if the client also 
    *                      modifies the data. 
    *                      Use DatabaseTransactionFactory.commitTransaction
    *                      to commit the transaction.
    * @param strDataSourceName - data source which will be used to get the 
    *                            connection
    * @return Connection - connection with administrator privileges, never null
    * @see #returnConnection
    * @throws OSSException - an error has occurred
    */
   Connection requestAdminConnection(
      boolean bAutoCommit,
      String  strDataSourceName
   ) throws OSSException;
   
   /**
    * Release connection to a database. 
    *
    * @param cntDBConnection - connection to a database to release, may be null
    * @see #requestConnection
    */
   void returnConnection(
      Connection cntDBConnection
   );

   // Configuration methods ////////////////////////////////////////////////////
   
   /**
    * Define new data source initialized from the default properties for the 
    * default database.
    * 
    * @param strDataSourceName - data source name 
    * @return DatabaseSourceDefinition - data source definition
    * @throws OSSException - an error has occurred
    */
   DatabaseSourceDefinition addDataSource(
      String strDataSourceName
   ) throws OSSException;
   
   /**
    * Define new data source initialized from the default properties for the 
    * specified database.
    * 
    * @param strDataSourceName - data source name 
    * @param database - database for which the datasource is being created. 
    *                   This can represent different DBMS types (e.g. Oracle, 
    *                   DB2, etc.) or different groups of settings for the same 
    *                   database (e.g. Oracle Production, Oracle Development, 
    *                   etc.).
    * @return DatabaseSourceDefinition - data source definition
    * @throws OSSException - an error has occurred
    */
   DatabaseSourceDefinition addDataSource(
      String   strDataSourceName,
      Database database
   ) throws OSSException;
   
   /**
    * Define new data source. If it doesn't exist, it will be created.
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
    * @return DatabaseSourceDefinition - data source definition
    * @throws OSSException - an error has occurred
    */
   DatabaseSourceDefinition addDataSource(
      String   strDataSourceName,
      Database database,
      String   strDriverName,
      String   strUrl,
      String   strUser,
      String   strPassword,
      int      iTransactionIsolation
   ) throws OSSException;
   
   /**
    * Unregister data source initialized from the default properties. All the 
    * connections from this data source should be returned by the time this 
    * method is called but this method will try to do as much cleaning as it can
    * in case it detects some orphans.
    * 
    * @param strDataSourceName - data source name 
    * @throws OSSException - an error has occurred
    */
   void removeDataSource(
      String strDataSourceName
   ) throws OSSException;
   
   /**
    * Set the default data source. 
    * 
    * @param strDataSourceName - data source which will be used to get connections
    */
   void setDefaultDataSource(
      String strDataSourceName
   );

   /**
    * Get the name of the default data source.
    * 
    * @return String
    */
   String getDefaultDataSourceName(
   );
   
   /**
    * Get the default data source definition.
    * 
    * @return DatabaseSourceDefinition - default data source definition, never null 
    * @throws OSSException - an error has occurred                                    
    */
   DatabaseSourceDefinition getDefaultDataSource(
   ) throws OSSException;

   /**
    * Get the specified data source definition.
    * 
    * @param strDataSourceName - data source for which to get definition
    * @return DatabaseSourceDefinition - data source definition or null if none
    *                                    is defined yet for the specified name
    * @throws OSSException - an error has occurred                                    
    */
   DatabaseSourceDefinition getDataSource(
      String strDataSourceName
   ) throws OSSException;

   /**
    * Get the data source definition based on which the specified connection was
    * created.
    * 
    * @param cntDBConnection - database connection
    * @return DatabaseSourceDefinition - data source definition which was used 
    *                                    to construct the database connection
    *                                    or null if no such definition cannot be 
    *                                    found
    * @throws OSSException - an error has occurred                                    
    */
   DatabaseSourceDefinition getDataSource(
      Connection cntDBConnection
   ) throws OSSException;

   /**
    * Check if the specified data has been already defined
    * 
    * @param strDataSourceName - name of the data source to check for
    * @return boolean - true if the data source is already defined, false otherwise
    */
   boolean isDataSourceDefined(
      String strDataSourceName
   );
   
   /**
    * Stop the connection factory. After the connection factory is stopped
    * all the connections should be released and they cannot be retrieved
    * unless the connection factory is reinitialized in some way.
    * 
    * @throws OSSException - problem stopping connection factory.
    */
   void stop(
   ) throws OSSException;

   // Debug interface //////////////////////////////////////////////////////////
   
   /**
    * Get total number for connections (for all data sources) which are currently 
    * requested and were not returned.
    * 
    * @return int - how many connections were currently requested from pool
    *               and were not returned yet.
    */
   int getTotalRequestedConnectionCount(
   );

   /**
    * Get number for connections which are currently requested and were not returned 
    * for default data source.
    * 
    * @return int - how many connections were currently requested from pool
    *               and were not returned yet.
    */
   int getRequestedConnectionCount(
   );

   /**
    * Get number for connections which are currently requested and were not 
    * returned.
    * 
    * @param strDataSourceName - data source which will be used to get connections
    * @return int - how many connections were currently requested from pool
    * and were not returned yet.
    */
   int getRequestedConnectionCount(
      String strDataSourceName
   );
}
