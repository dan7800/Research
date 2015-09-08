/*
 * Copyright (c) 2003 - 2009 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: TransactionalConnection.java,v 1.2 2009/07/18 04:58:16 bastafidli Exp $
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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.Map;

import org.opensubsystems.core.util.GlobalConstants;

/**
 * Transactional connection is wrapper around real database connection to ensure
 * that the connection can be made part of global transaction spanning several
 * code components which do not know about each other.
 * 
 * @version $Id: TransactionalConnection.java,v 1.2 2009/07/18 04:58:16 bastafidli Exp $
 * @author Miro Halas
 * @code.reviewer Miro Halas
 * @code.reviewed 1.5 2007/09/11 06:24:55 bastafidli
 */
public class TransactionalConnection implements Connection
{
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Underlying database connection to which all method calls are delegated.
    */
   private Connection m_privateConnection;

   /**
    * Name of the data source this connection belongs to.
    */
   private String m_strDataSourceName;
   
   /**
    * User name under which this connection was created.
    */
   private String m_strUser;

   /**
    * Password under which this connection was created.
    */
   private String m_strPassword;
   
   /**
    * This flag is used by transaction to see if this connection was used 
    * while it was associated with the transaction to see if it should be 
    * committed or not. 
    */
   private boolean m_bUsed;
   
   /**
    * This counter is incremented when the connection is requested from the pool
    * and decremented when it is returned. This is used in case the connection
    * is returned in the transaction to mark it as returned since it cannot be
    * really returned while it is associated with transaction.
    */
   private int m_iActiveCount;
   
   /**
    * This flag will be set to true if the connection is associated with 
    * transaction so that the database connection factory knows it cannot
    * be returned to the pool.
    */
   private boolean m_bInTransaction;
   
   /**
    * Connection factory from which the private connection was acquired.
    */
   private DatabaseConnectionFactoryImpl m_connectionFactory;
   
   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Protected constructor so that only classes from this package can create it.
    * 
    * @param privateConnection - real connection to the database
    * @param strDataSourceName - name of the data source this connection belongs t
    *                            o
    * @param strUser - user name under which this connection was created
    * @param strPassword - password under which this connection was created
    * @param inTransaction - is the connection already in the transaction
    * @param connectionFactory - connection factory from which the private 
    *                            connection was acquired
    */
   protected TransactionalConnection(
      Connection                    privateConnection, 
      String                        strDataSourceName,
      String                        strUser,
      String                        strPassword,
      boolean                       inTransaction,
      DatabaseConnectionFactoryImpl connectionFactory
   ) 
   {
      super();
      
      m_privateConnection = privateConnection;
      m_strDataSourceName = strDataSourceName;
      m_strUser = strUser;
      m_strPassword = strPassword;
      m_bInTransaction = inTransaction;
      m_connectionFactory = connectionFactory;
      m_bUsed = false; // the connection wasn't used yet
      m_iActiveCount = 1; // the connection was just constructed so it has to be 
                          // active
   }   
   
   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * This method is not public so that it can be used only in this package.
    * 
    * @return boolean - if true then the connection is active, it wasn't returned 
    *                   to the pool yet.
    */
   boolean isActive()
   {
      return (m_iActiveCount > 0);
   }
   
   /**
    * This method is not public so that it can be used only in this package.
    * 
    * @param active - if true then the connection was requested from the pool
    *                 if false it was already returned to the pool
    */
   void setActive(
      boolean active
   )
   {
      if (active)
      {
         m_iActiveCount++;
      }
      else
      {
         m_iActiveCount--;
         if (GlobalConstants.ERROR_CHECKING)
         {
            assert m_iActiveCount >= 0 
                   : "Connection was returned too many times.";
         }
      }
   }
   
   /**
    * This method is not public so that it can be used only in this package.
    * 
    * @return boolean - if true then the connection any use of connection at
    *                   this time will be inside of transaction.
    */
   boolean isInTransaction()
   {
      return m_bInTransaction;
   }
   
   /**
    * This method is not public so that it can be used only in this package.
    * 
    * @param inTransaction - if in true then this connection is now associated
    *                        with the transaction
    * @throws SQLException - an error has occurred while associating connection
    *                        with transaction
    */
   void setInTransaction(
      boolean inTransaction
   ) throws SQLException
   {
      if ((inTransaction) && (m_privateConnection.getAutoCommit())) 
      {
         // Since the connection is now in transaction we need to disable
         // the autocommit
         m_privateConnection.setAutoCommit(false);
      }
      m_bInTransaction = inTransaction;
   }
   
   /**
    * This method is not public so that it can be used only in this package.
    * 
    * @return boolean - if true then some method was called on connection since
    *                   the last time this flag was reset 
    */
   public boolean isUsed()
   {
      return m_bUsed;
   }
   
   /**
    * @param used - if true then some method was called on connection since
    *               the last time this flag was reset
    * @throws SQLException - an error probably because the connection should
    *                        no longer be used 
    */
   void setUsed(
      boolean used
   ) throws SQLException
   {
      if ((m_iActiveCount == 0) && (used))
      {
         throw new SQLException("This connection was already returned to the pool" 
                                + " and shouldn't be used anymore."); 
      }
      m_bUsed = used;
   }
   
   /**
    * @return String
    */
   public String getDataSourceName()
   {
      return m_strDataSourceName;
   }

   /**
    * @return String
    */
   public String getPassword()
   {
      return m_strPassword;
   }
   
   /**
    * @return String
    */
   public String getUser()
   {
      return m_strUser;
   }
   
   /**
    * Verify if the specified connection is the one which is used by this wrapper.
    * 
    * @param verifiableConnection - connection to verify
    * @return boolean - true if the connections are the same
    */
   public boolean verifyConnection(
      Connection verifiableConnection
   )
   {
      // Verify if they are the same instance using ==
      return verifiableConnection == m_privateConnection;
   }   

   /**
    * @return DatabaseConnectionFactoryImpl
    */
   public DatabaseConnectionFactoryImpl getConnectionFactory()
   {
      return m_connectionFactory;
   }
   
   // java.sql.Connection delegating methods ///////////////////////////////////
   
   /**
    * {@inheritDoc}
    */
   public void clearWarnings(
   ) throws SQLException
   {
      setUsed(true);
      m_privateConnection.clearWarnings();
   }

   /**
    * {@inheritDoc}
    */
   public void close(
   ) throws SQLException
   {
      throw new SQLException("Connection is not supposed to be closed. It should" 
                             + " be returned to database connection factory.");
      // setUsed(true);
      // m_privateConnection.close();
   }

   /**
    * {@inheritDoc}
    */
   public void commit(
   ) throws SQLException
   {
      if (m_bInTransaction)
      {
         throw new SQLException("Connection in transaction cannot be commited.");
      }
      // We are commiting so that means that this connection is clean and not 
      // used anymore
      setUsed(false);
      m_privateConnection.commit();
   }

   /**
    * {@inheritDoc}
    */
   public Statement createStatement(
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.createStatement();
   }

   /**
    * {@inheritDoc}
    */
   public Statement createStatement(
      int resultSetType, 
      int resultSetConcurrency
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.createStatement(resultSetType, 
                                                 resultSetConcurrency);
   }

   /**
    * {@inheritDoc}
    */
   public Statement createStatement(
      int resultSetType,
      int resultSetConcurrency,
      int resultSetHoldability
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.createStatement(resultSetType, 
                                                 resultSetConcurrency,
                                                 resultSetHoldability);
   }

   /**
    * {@inheritDoc}
    */
   public boolean equals(
      Object obj
   )
   {
      // This is not really a use of connection
      // setUsed(true);
      return m_privateConnection.equals(obj);
   }

   /**
    * {@inheritDoc}
    */
   public boolean getAutoCommit(
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.getAutoCommit();
   }

   /**
    * {@inheritDoc}
    */
   public String getCatalog(
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.getCatalog();
   }

   /**
    * {@inheritDoc}
    */
   public int getHoldability(
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.getHoldability();
   }

   /**
    * {@inheritDoc}
    */
   public DatabaseMetaData getMetaData(
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.getMetaData();
   }

   /**
    * {@inheritDoc}
    */
   public int getTransactionIsolation(
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.getTransactionIsolation();
   }

   /**
    * {@inheritDoc}
    */
   public Map getTypeMap(
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.getTypeMap();
   }

   /**
    * {@inheritDoc}
    */
   public SQLWarning getWarnings(
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.getWarnings();
   }

   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      // This is not really a use of connection
      // setUsed(true);
      return m_privateConnection.hashCode();
   }

   /**
    * {@inheritDoc}
    */
   public boolean isClosed(
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.isClosed();
   }

   /**
    * {@inheritDoc}
    */
   public boolean isReadOnly(
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.isReadOnly();
   }

   /**
    * {@inheritDoc}
    */
   public String nativeSQL(
      String sql
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.nativeSQL(sql);
   }

   /**
    * {@inheritDoc}
    */
   public CallableStatement prepareCall(
      String sql
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.prepareCall(sql);
   }

   /**
    * {@inheritDoc}
    */
   public CallableStatement prepareCall(
      String sql, 
      int resultSetType, 
      int resultSetConcurrency
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.prepareCall(sql, resultSetType, 
                                             resultSetConcurrency);
   }

   /**
    * {@inheritDoc}
    */
   public CallableStatement prepareCall(
      String sql,
      int resultSetType,
      int resultSetConcurrency,
      int resultSetHoldability
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.prepareCall(sql, resultSetType, 
                                             resultSetConcurrency,
                                             resultSetHoldability);
   }

   /**
    * {@inheritDoc}
    */
   public PreparedStatement prepareStatement(
      String sql
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.prepareStatement(sql);
   }

   /**
    * {@inheritDoc}
    */
   public PreparedStatement prepareStatement(
      String sql, 
      int autoGeneratedKeys
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.prepareStatement(sql, autoGeneratedKeys);
   }

   /**
    * {@inheritDoc}
    */
   public PreparedStatement prepareStatement(
      String sql, 
      int resultSetType, 
      int resultSetConcurrency
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.prepareStatement(sql, resultSetType, 
                                                  resultSetConcurrency);
   }

   /**
    * {@inheritDoc}
    */
   public PreparedStatement prepareStatement(
      String sql,
      int resultSetType,
      int resultSetConcurrency,
      int resultSetHoldability
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.prepareStatement(sql, resultSetType, 
                                                  resultSetConcurrency,
                                                  resultSetHoldability);
   }

   /**
    * {@inheritDoc}
    */
   public PreparedStatement prepareStatement(
      String sql, 
      int[] columnIndexes
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.prepareStatement(sql, columnIndexes);
   }

   /**
    * {@inheritDoc}
    */
   public PreparedStatement prepareStatement(
      String sql, String[] columnNames
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.prepareStatement(sql, columnNames);
   }

   /**
    * {@inheritDoc}
    */
   public void releaseSavepoint(
      Savepoint savepoint
   ) throws SQLException
   {
      setUsed(true);
      m_privateConnection.releaseSavepoint(savepoint);
   }

   /**
    * {@inheritDoc}
    */
   public void rollback(
   ) throws SQLException
   {
      if (m_bInTransaction)
      {
         throw new SQLException("Connection in transaction cannot be rollbacked.");
      }
      // We are rolling back so that means that this connection is clean and not 
      // used anymore
      setUsed(false);
      m_privateConnection.rollback();
   }

   /**
    * {@inheritDoc}
    */
   public void rollback(
      Savepoint savepoint
   ) throws SQLException
   {
      setUsed(true);
      m_privateConnection.rollback(savepoint);
   }

   /**
    * {@inheritDoc}
    */
   public void setAutoCommit(
      boolean autoCommit
   ) throws SQLException
   {
      if ((m_bInTransaction) && (autoCommit))
      {
         throw new SQLException(
                      "Connection in transaction cannot be set to autocommit.");
      }
      setUsed(true);
      m_privateConnection.setAutoCommit(autoCommit);
   }

   /**
    * {@inheritDoc}
    */
   public void setCatalog(
      String catalog
   ) throws SQLException
   {
      setUsed(true);
      m_privateConnection.setCatalog(catalog);
   }

   /**
    * {@inheritDoc}
    */
   public void setHoldability(
      int holdability
   ) throws SQLException
   {
      setUsed(true);
      m_privateConnection.setHoldability(holdability);
   }

   /**
    * {@inheritDoc}
    */
   public void setReadOnly(
      boolean readOnly
   ) throws SQLException
   {
      setUsed(true);
      m_privateConnection.setReadOnly(readOnly);
   }

   /**
    * {@inheritDoc}
    */
   public Savepoint setSavepoint(
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.setSavepoint();
   }

   /**
    * {@inheritDoc}
    */
   public Savepoint setSavepoint(
      String name
   ) throws SQLException
   {
      setUsed(true);
      return m_privateConnection.setSavepoint(name);
   }

   /**
    * {@inheritDoc}
    */
   public void setTransactionIsolation(
      int level
   ) throws SQLException
   {
      setUsed(true);
      m_privateConnection.setTransactionIsolation(level);
   }

   /**
    * {@inheritDoc}
    */
   public void setTypeMap(
      Map map
   ) throws SQLException
   {
      setUsed(true);
      m_privateConnection.setTypeMap(map);
   }

   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      // This is not really a use of connection
      // setUsed(true);
      return m_privateConnection.toString();
   }

   // These methods were added in Java 1.6 /////////////////////////////////////

// TODO: JDK 1.6: Enable these methods for additional JDBC features 
//   /**
//    * {@inheritDoc}
//    */
//   public Array createArrayOf(
//      String   typeName, 
//      Object[] elements
//   ) throws SQLException
//   {
//      setUsed(true);
//      return m_privateConnection.createArrayOf(typeName, elements);
//   }
//
//   /**
//    * {@inheritDoc}
//    */
//   public Blob createBlob(
//   ) throws SQLException
//   {
//      setUsed(true);
//      return m_privateConnection.createBlob();
//   }
//
//   /**
//    * {@inheritDoc}
//    */
//   public Clob createClob(
//   ) throws SQLException
//   {
//      setUsed(true);
//      return m_privateConnection.createClob();
//   }
//
//   /**
//    * {@inheritDoc}
//    */
//   public NClob createNClob(
//   ) throws SQLException
//   {
//      setUsed(true);
//      return m_privateConnection.createNClob();
//   }
//
//   /**
//    * {@inheritDoc}
//    */
//   public SQLXML createSQLXML(
//   ) throws SQLException
//   {
//      setUsed(true);
//      return m_privateConnection.createSQLXML();
//   }
//
//   /**
//    * {@inheritDoc}
//    */
//   public Struct createStruct(
//      String   typeName, 
//      Object[] attributes
//   ) throws SQLException
//   {
//      setUsed(true);
//      return m_privateConnection.createStruct(typeName, attributes);
//   }
//
//   /**
//    * {@inheritDoc}
//    */
//   public Properties getClientInfo(
//   ) throws SQLException
//   {
//      setUsed(true);
//      return m_privateConnection.getClientInfo();
//   }
//
//   /**
//    * {@inheritDoc}
//    */
//   public String getClientInfo(
//      String name
//   ) throws SQLException
//   {
//      setUsed(true);
//      return m_privateConnection.getClientInfo(name);
//   }
//
//   /**
//    * {@inheritDoc}
//    */
//   public boolean isValid(
//      int timeout
//   ) throws SQLException
//   {
//      setUsed(true);
//      return m_privateConnection.isValid(timeout);
//   }
//
//   /**
//    * {@inheritDoc}
//    */
//   public void setClientInfo(
//      Properties properties
//   ) throws SQLClientInfoException
//   {
//      try
//      {
//         setUsed(true);
//      }
//      catch (SQLException exc)
//      {
//         SQLClientInfoException exc2 = new SQLClientInfoException();
//         exc2.setNextException(exc);
//         throw exc2;
//      }
//      m_privateConnection.setClientInfo(properties);
//   }
//
//   /**
//    * {@inheritDoc}
//    */
//   public void setClientInfo(
//      String name, 
//      String value
//   ) throws SQLClientInfoException
//   {
//      try
//      {
//         setUsed(true);
//      }
//      catch (SQLException exc)
//      {
//         SQLClientInfoException exc2 = new SQLClientInfoException();
//         exc2.setNextException(exc);
//         throw exc2;
//      }
//      m_privateConnection.setClientInfo(name, value);
//   }
//
//   /**
//    * {@inheritDoc}
//    */
//   public boolean isWrapperFor(
//      Class iface
//   ) throws SQLException
//   {
//      setUsed(true);
//      return m_privateConnection.isWrapperFor(iface);
//   }
//
//   /**
//    * {@inheritDoc}
//    */
//   public Object unwrap(
//      Class iface
//   ) throws SQLException
//   {
//      setUsed(true);
//      return m_privateConnection.unwrap(iface);
//   }
}
