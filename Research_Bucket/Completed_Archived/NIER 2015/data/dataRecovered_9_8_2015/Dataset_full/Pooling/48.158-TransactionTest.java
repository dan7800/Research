/*
 * Copyright (c) 2003 - 2008 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: TransactionTest.java,v 1.16 2009/07/18 05:09:17 bastafidli Exp $
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

package org.opensubsystems.core.persist.db.driver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.persist.db.Database;
import org.opensubsystems.core.persist.db.DatabaseTest;
import org.opensubsystems.core.persist.db.DatabaseTestSetup;
import org.opensubsystems.core.persist.db.DatabaseTestSuite;
import org.opensubsystems.core.persist.db.impl.DatabaseConnectionFactoryImpl;
import org.opensubsystems.core.persist.db.impl.DatabaseImpl;
import org.opensubsystems.core.persist.db.impl.DatabaseTransactionFactoryImpl;
import org.opensubsystems.core.util.Log;
import org.opensubsystems.core.util.Tests;
import org.opensubsystems.core.util.jdbc.DatabaseUtils;

/**
 * Test of transaction (update, insert, delete ans select in one transaction).
 * 
 * @version $Id: TransactionTest.java,v 1.16 2009/07/18 05:09:17 bastafidli Exp $
 * @author Julo Legeny
 * @code.reviewer Miro Halas
 * @code.reviewed Initial revision
 */
public final class TransactionTest extends Tests
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private TransactionTest(
   )
   {
      // Do nothing
   }
   
   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * Create the suite for this test since this is the only way how to create
    * test setup which can initialize and shutdown the database for us
    * 
    * @return Test - suite of tests to run for this database
    */
   public static Test suite(
   )
   {
      TestSuite suite = new DatabaseTestSuite("TransactionTest");
      suite.addTestSuite(TransactionTestInternal.class);
      // Here we are using DatabaseTestSetup instead of ApplicationTestSetup
      // since we are just directly testing  database functionality without
      // accessing any business logic functionality packaged into application 
      // modules
      TestSetup wrapper = new DatabaseTestSetup(suite);

      return wrapper;
   }

   /**
    * Internal class which can be included in other test suites directly without
    * including the above suite. This allows us to group multiple tests 
    * together and the execute the DatabaseTestSetup only once 
    */
   public static class TransactionTestInternal extends DatabaseTest
   {
      // Cached values ////////////////////////////////////////////////////////////
   
      /**
       * Logger for this class
       */
      private static Logger s_logger = Log.getInstance(TransactionTestInternal.class);
   
      /**
       * Static initializer
       */
      static
      { // This test use special database schema so make the database aware of it
         Database dbDatabase;
         try
         { // Add schema database tests needs to the database
            dbDatabase = DatabaseImpl.getInstance();
            dbDatabase.add(DatabaseTestSchema.class);
         }
         catch (OSSException bfeExc)
         {
            throw new RuntimeException("Unexpected exception.", bfeExc);
         }
      }
      /**
       * Create new test.
       * 
       * @param strTestName - name of the test
       */
      public TransactionTestInternal(
         String strTestName
      )
      {
         super(strTestName);
      }
   
      /**
       * Test if the database driver support rollback. 
       * 
       * Uses the already setup connection and transaction. 
       * No need to close the connection since base class is doing it for us.
       * 
       * @throws Throwable - an error has occurred during test
       */
      public void testRollbackUniqueWithJTA(
      ) throws Throwable
      {
         final String INSERT_VALUE = "insert into ROLLBACK_TEST (TEST_COLUMN) values (?)";
         final String DELETE_ALL = "delete from ROLLBACK_TEST";      
         final String UPDATE_VALUE 
                          = "update ROLLBACK_TEST set TEST_COLUMN = ? where TEST_COLUMN = ?";
         final String VALUE_TEST_1 = "value one 1";
         final String VALUE_TEST_2 = "value two 2";
       
         PreparedStatement insertStatement = null;
         int               iUpdateCount;
         try
         {
            m_transaction.begin();
      
            try
            {
               insertStatement = m_connection.prepareStatement(INSERT_VALUE);
               insertStatement.setString(1, VALUE_TEST_1);
      
               iUpdateCount = DatabaseUtils.executeUpdateAndClose(insertStatement);
      
               m_transaction.commit();
            }
            catch (Throwable throwable)
            {
               m_transaction.rollback();
               throw throwable;
            }
            finally
            {
               DatabaseUtils.closeStatement(insertStatement);
               insertStatement = null;            
            }
         
            assertEquals("Exactly one record have been inserted.", 
                                iUpdateCount, 1);
      
            m_transaction.begin();
      
            try
            {
               insertStatement = m_connection.prepareStatement(INSERT_VALUE);
               insertStatement.setString(1, VALUE_TEST_2);
      
               iUpdateCount = DatabaseUtils.executeUpdateAndClose(insertStatement);
      
               m_transaction.commit();
            }
            catch (Throwable throwable)
            {
               m_transaction.rollback();
               throw throwable;
            }
            finally
            {
               DatabaseUtils.closeStatement(insertStatement);
               insertStatement = null;            
            }
               
            // Now select it back to be sure it is there so we cna delete it
            PreparedStatement updateStatement = null;
      
            m_transaction.begin();
            try
            {
               updateStatement = m_connection.prepareStatement(UPDATE_VALUE);
               updateStatement.setString(1, VALUE_TEST_1);
               updateStatement.setString(2, VALUE_TEST_2);
               iUpdateCount = updateStatement.executeUpdate();
               
               m_transaction.commit();
               
               fail("Updating the unique column with the same value" +
                    " didn't generated exception.");
            }
            catch (Throwable throwable)
            {
               s_logger.log(Level.FINEST, 
                            "Update of unique column with the same value" +
                            " generated exception as expected.", 
                            throwable);
               m_transaction.rollback();
               // Don't throw the exception since it was expected
            }
            finally
            {
               DatabaseUtils.closeStatement(updateStatement);
               updateStatement = null;
            }
         }
         finally
         {
            PreparedStatement deleteStatement = null;
            
            m_transaction.begin();
            try
            {
               deleteStatement = m_connection.prepareStatement(DELETE_ALL);
   
               DatabaseUtils.executeUpdateAndClose(deleteStatement);
   
               m_transaction.commit();
            }
            catch (Throwable throwable)
            {
               m_transaction.rollback();
               throw throwable;
            }
            finally
            {
               DatabaseUtils.closeStatement(deleteStatement);
               deleteStatement = null;            
            }
         }
      } 
   
      /**
       * Test if the database driver support rollback. 
       * 
       * Uses the already setup connection and transaction. 
       * No need to close the connection since base class is doing it for us.
       * 
       * @throws Throwable - an error has occurred during test
       */
      public void testRollbackUniqueWithJDBC(
      ) throws Throwable
      {
         final String INSERT_VALUE = "insert into ROLLBACK_TEST (TEST_COLUMN) values (?)";
         final String DELETE_ALL = "delete from ROLLBACK_TEST";      
         // See OracleTests class why we need to select tablename.* 
         final String UPDATE_VALUE 
                          = "update ROLLBACK_TEST set TEST_COLUMN = ? where TEST_COLUMN = ?";
         final String VALUE_TEST_1 = "value one 1";
         final String VALUE_TEST_2 = "value two 2";
       
         PreparedStatement insertStatement = null;
         int               iUpdateCount;
         try
         {
            m_transaction.begin();
            try
            {
               insertStatement = m_connection.prepareStatement(INSERT_VALUE);
               insertStatement.setString(1, VALUE_TEST_1);
      
               iUpdateCount = DatabaseUtils.executeUpdateAndClose(insertStatement);
      
               m_transaction.commit();
            }
            catch (Throwable throwable)
            {
               m_transaction.rollback();
               throw throwable;
            }
            finally
            {
               DatabaseUtils.closeStatement(insertStatement);
               insertStatement = null;            
            }
         
            assertEquals("Exactly one record have been inserted.", 
                                iUpdateCount, 1);
      
            m_transaction.begin();
            try
            {
               insertStatement = m_connection.prepareStatement(INSERT_VALUE);
               insertStatement.setString(1, VALUE_TEST_2);
      
               iUpdateCount = DatabaseUtils.executeUpdateAndClose(insertStatement);
      
               m_transaction.commit();
            }
            catch (Throwable throwable)
            {
               m_transaction.rollback();
               throw throwable;
            }
            finally
            {
               DatabaseUtils.closeStatement(insertStatement);
               insertStatement = null;            
            }
               
            // Now select it back to be sure it is there so we cna delete it
            PreparedStatement updateStatement = null;
      
            // Instead of JTA we will use JDBC to commit
            // m_transaction.begin();
            boolean bOriginalAutoCommit = m_connection.getAutoCommit();
            try
            {
               if (bOriginalAutoCommit)
               {
                  m_connection.setAutoCommit(false);
               }
               try
               {
                  updateStatement = m_connection.prepareStatement(UPDATE_VALUE);
                  updateStatement.setString(1, VALUE_TEST_1);
                  updateStatement.setString(2, VALUE_TEST_2);
                  iUpdateCount = updateStatement.executeUpdate();
                  
                  DatabaseTransactionFactoryImpl.getInstance().commitTransaction(m_connection);
                  
                  fail("Updating the unique column with the same value" +
                       " didn't generated exception.");
               }
               catch (Throwable throwable)
               {
                  s_logger.log(Level.FINEST, 
                               "Update of unique column with the same value" +
                               " generated exception as expected.", 
                                        throwable);
                  // Instead of JTA we will use JDBC to commit
                  // m_transaction.rollback();
                  DatabaseTransactionFactoryImpl.getInstance().rollbackTransaction(m_connection);
                  // Don't throw the exception since it was expected
               }
               finally
               {
                  DatabaseUtils.closeStatement(updateStatement);
                  updateStatement = null;
               }
            }
            finally
            {
               if (bOriginalAutoCommit)
               {
                  m_connection.setAutoCommit(bOriginalAutoCommit);
               }
            }
         }
         finally
         {
            PreparedStatement deleteStatement = null;
            
            m_transaction.begin();
            try
            {
               deleteStatement = m_connection.prepareStatement(DELETE_ALL);
   
               DatabaseUtils.executeUpdateAndClose(deleteStatement);
   
               m_transaction.commit();
            }
            catch (Throwable throwable)
            {
               m_transaction.rollback();
               throw throwable;
            }
            finally
            {
               DatabaseUtils.closeStatement(deleteStatement);
               deleteStatement = null;            
            }
         }
      }
   
      /**
       * Update, delete, insert and select record in the same transaction 
       * 
       * @throws Throwable - an error has occurred during test
       */
      public void testCommitAfterUpdateSelect(
      ) throws Throwable
      {
         final String DELETE_ALL = "delete from TRANSACTION_TEST";
         final String UPDATE_VALUE = "update TRANSACTION_TEST set TEST_VALUE = ? where TEST_ID = ?";
         final String INSERT_VALUE = "insert into TRANSACTION_TEST values (?, ?)";      
         final String SELECT_VALUE = "select TEST_VALUE from TRANSACTION_TEST where TEST_ID = 1";
         final String VALUE_TEST = "test_value";
         final String VALUE_TEST1 = "test_value_updated_1";
         
         Connection connection = null;
         PreparedStatement updateStatement = null;
         PreparedStatement deleteStatement = null;
         PreparedStatement selectStatement = null;
         PreparedStatement insertStatement = null;
         ResultSet         rsResults = null;
         int               iUpdatedCount = 0;
         int               iDeletedCount = 0;
         int               iSelectedCount = 0;
         int               iInsertCount = 0;
         String            strUpdatedValue = "";
   
         try
         {
            //******************************************************************
            // Try to select original record to verify that the database is in OK state
            try 
            {
               // Request autocommit false since we are modifying database
               connection = DatabaseConnectionFactoryImpl.getInstance().requestConnection(false);
      
               m_transaction.begin();
               try
               {
                  deleteStatement = connection.prepareStatement(DELETE_ALL);
      
                  iDeletedCount = DatabaseUtils.executeUpdateAndClose(deleteStatement);
      
                  try
                  {
                     insertStatement = connection.prepareStatement(INSERT_VALUE);
                     insertStatement.setInt(1, 1); // ID
                     insertStatement.setString(2, VALUE_TEST);
                     iInsertCount = insertStatement.executeUpdate();
                  }
                  finally
                  {   
                     DatabaseUtils.closeStatement(insertStatement);
                  }
   
                  m_transaction.commit();
               }
               catch (Throwable throwable)
               {
                  m_transaction.rollback();
                  throw throwable;
               }
            }
            finally
            {   
               DatabaseConnectionFactoryImpl.getInstance().returnConnection(connection);
               connection = null;
            }
            
            assertEquals("No records should be initially in the database.", 
                                0, iDeletedCount);
            assertEquals("Exactly one record should be inserted.", 1, iInsertCount);
                                
            //******************************************************************
            // Try to select original record to verify that the database is in OK state
            try 
            {
               // Request autocommit true since we are just reading data from the database
               connection = DatabaseConnectionFactoryImpl.getInstance().requestConnection(true);
               try
               {
                  selectStatement = connection.prepareStatement(SELECT_VALUE,
                                                                ResultSet.TYPE_SCROLL_INSENSITIVE, 
                                                                ResultSet.CONCUR_READ_ONLY);
                  rsResults = selectStatement.executeQuery();
                  if (rsResults.last())
                  { // The last row number will tell us the row count
                     iSelectedCount = rsResults.getRow();
                     rsResults.beforeFirst();
                  }
                  rsResults.beforeFirst();
                  if (rsResults.next())
                  {
                     strUpdatedValue = rsResults.getString(1);
                  }
               }
               finally
               {   
                  DatabaseUtils.closeResultSetAndStatement(rsResults, selectStatement);
               }
            }
            finally
            {   
               DatabaseConnectionFactoryImpl.getInstance().returnConnection(connection);
               connection = null;
            }
            assertEquals("Exactly one record have been selected.", 1, iSelectedCount);
            assertEquals("Selected items should not have postfix '_updated_1'.", 
                                VALUE_TEST, strUpdatedValue);
   
            // Now in transaction access the database and try to confuse JOTM/XAPool
            m_transaction.begin();
            try
            {            
               //******************************************************************
               try // try to update record 
               {
                  // Request autocommit false since we are modifying database
                  connection = DatabaseConnectionFactoryImpl.getInstance().requestConnection(false);
                  try
                  {
                     updateStatement = connection.prepareStatement(UPDATE_VALUE);
                     updateStatement.setString(1, VALUE_TEST1);
                     updateStatement.setInt(2, 1);
                     iUpdatedCount = DatabaseUtils.executeUpdateAndClose(updateStatement);
                  }
                  finally
                  {   
                     DatabaseUtils.closeStatement(updateStatement);
                  }
               }
               finally
               {   
                  DatabaseConnectionFactoryImpl.getInstance().returnConnection(connection);
                  connection = null;
               }
               assertEquals("Exactly one record should have been updated.", 1, iUpdatedCount);
               //******************************************************************
               try // try to select some record
               {
                  // Request autocommit true since we are just reading data from the database
                  connection = DatabaseConnectionFactoryImpl.getInstance().requestConnection(true);
                  try
                  {
                     // HERE IS BUG IN JOTM 1.4.2 / XAPOOL 1.2.2
                     // Connection which is used to execute this statement
                     // will never become part of the transaction and for whatever reason
                     // what was done before is rollbacked
                     selectStatement = connection.prepareStatement(
                                                       SELECT_VALUE,
                                                       ResultSet.TYPE_SCROLL_INSENSITIVE, 
                                                       ResultSet.CONCUR_READ_ONLY);
                     rsResults = selectStatement.executeQuery();
                     if (rsResults.last())
                     { // The last row number will tell us the row count
                        iSelectedCount = rsResults.getRow();
                        rsResults.beforeFirst();
                     }
                     rsResults.first();
                     strUpdatedValue = rsResults.getString(1);
                  }
                  finally
                  {   
                     DatabaseUtils.closeResultSetAndStatement(rsResults, selectStatement);
                  }
               }
               finally
               {   
                  DatabaseConnectionFactoryImpl.getInstance().returnConnection(connection);
                  connection = null;
               }
               assertEquals("Exactly one record should have been selected.", 1, iSelectedCount);
               assertEquals("Selected items should have updated value.", 
                                   VALUE_TEST1, strUpdatedValue);
               m_transaction.commit();
            }
            catch (Throwable throwable)
            {
               m_transaction.rollback();
               throw throwable;
            }
               
            // Now try to verify if the result is in there
            //******************************************************************
            try // try to select updated record
            {
               // Request autocommit true since we are just reading data from the database
               connection = DatabaseConnectionFactoryImpl.getInstance().requestConnection(true);
               try
               {
                  selectStatement = connection.prepareStatement(SELECT_VALUE,
                                                                ResultSet.TYPE_SCROLL_INSENSITIVE, 
                                                                ResultSet.CONCUR_READ_ONLY);
                  rsResults = selectStatement.executeQuery();
                  if (rsResults.last())
                  { // The last row number will tell us the row count
                     iSelectedCount = rsResults.getRow();
                     rsResults.beforeFirst();
                  }
                  rsResults.first();
                  strUpdatedValue = rsResults.getString(1);
               }
               finally
               {   
                  DatabaseUtils.closeResultSetAndStatement(rsResults, selectStatement);
               }
            }
            finally
            {   
               DatabaseConnectionFactoryImpl.getInstance().returnConnection(connection);
               connection = null;
            }
            // In JOTM 1.4.2 with XAPool 1.2.2 this will fail since the select
            // before rollbacked the update. In JOTM 1.4.3 this is passing
            assertEquals("Exactly one record should have been selected.", 
                                1, iSelectedCount);
            assertEquals("Selected items should have updated value.", 
                                VALUE_TEST1, strUpdatedValue);
                                
         }
         finally
         {
            m_transaction.begin();
            try
            {
               deleteStatement = m_connection.prepareStatement(DELETE_ALL);
   
               DatabaseUtils.executeUpdateAndClose(deleteStatement);
   
               m_transaction.commit();
            }
            catch (Throwable throwable)
            {
               m_transaction.rollback();
               throw throwable;
            }
         }
      }          
   
      /**
       * Test implemented to try to detect bug in JOTM which turned up to be 
       * bug in testCommitAfterUpdateSelect. Now the testCommitAfterUpdateSelect
       * and it still detects the bug in JOTM 1.4.2 and passing in JOTM 1.4.3 
       * 
       * This test case also detects defect in WebLogic 9.1 which doesn't enroll
       * connections acquired before the transaction is started in the transaction.
       * 
       * @throws Throwable - an error has occurred during test
       */
      public void testCommitAfterUpdateDelete(
      ) throws Throwable
      {
         final String DELETE_ALL = "delete from TRANSACTION_TEST";
         final String UPDATE_VALUE = "update TRANSACTION_TEST set TEST_VALUE = ? where TEST_ID = ?";
         final String INSERT_VALUE = "insert into TRANSACTION_TEST values (?, ?)";      
         final String SELECT_VALUE = "select TEST_VALUE from TRANSACTION_TEST where TEST_ID = 1";
         final String VALUE_TEST = "test_value";
         final String VALUE_TEST1 = "test_value_updated_1";
         
         Connection connection = null;
         PreparedStatement updateStatement = null;
         PreparedStatement deleteStatement = null;
         PreparedStatement selectStatement = null;
         PreparedStatement insertStatement = null;
         ResultSet         rsResults = null;
         int               iUpdatedCount = 0;
         int               iDeletedCount = 0;
         int               iSelectedCount = 0;
         int               iInsertCount = 0;
   
         try
         {
            //******************************************************************
            // Insert record to prepare it for the test
            try 
            {
               // Request autocommit false since we are modifying database
               // Notice that we are requesting the connection before we begin
               // the transaction. The transaction manager should handle this
               // and once the connection is used in the transaction it should
               // enroll it in the transaction
               connection = DatabaseConnectionFactoryImpl.getInstance().requestConnection(false);
      
               m_transaction.begin();
               try
               {
                  deleteStatement = connection.prepareStatement(DELETE_ALL);
                  iDeletedCount = DatabaseUtils.executeUpdateAndClose(deleteStatement);
                  try
                  {
                     insertStatement = connection.prepareStatement(INSERT_VALUE);
                     insertStatement.setInt(1, 1); // ID
                     insertStatement.setString(2, VALUE_TEST1);
                     iInsertCount = insertStatement.executeUpdate();
                  }
                  finally
                  {   
                     DatabaseUtils.closeStatement(insertStatement);
                  }
      
                  m_transaction.commit();
               }
               catch (Throwable throwable)
               {
                  m_transaction.rollback();
                  throw throwable;
               }
            }
            finally
            {   
               DatabaseConnectionFactoryImpl.getInstance().returnConnection(connection);
               connection = null;
            }
            
            assertEquals("No records should be initially in the database.", 
                                0, iDeletedCount);
            assertEquals("Exactly one record should be inserted.", 1, iInsertCount);
                                
            m_transaction.begin();
            try
            {
               try // try to update the record back to it's original form 
               {
                  // Notice that we are requesting the connection withing the 
                  // transaction. This connection should be enrolled in the 
                  // transaction automatically 
                  
                  // Request autocommit false since we are modifying database
                  connection = DatabaseConnectionFactoryImpl.getInstance().requestConnection(false);
                  try
                  {
                     updateStatement = connection.prepareStatement(UPDATE_VALUE);
                     updateStatement.setString(1, VALUE_TEST);
                     updateStatement.setInt(2, 1);
                     iUpdatedCount = DatabaseUtils.executeUpdateAndClose(updateStatement);
                  }
                  finally
                  {   
                     DatabaseUtils.closeStatement(updateStatement);
                  }
               }
               finally
               {   
                  DatabaseConnectionFactoryImpl.getInstance().returnConnection(connection);
                  connection = null;
               }
               assertEquals("Exactly one record should have been updated.", 1, iUpdatedCount);
               m_transaction.commit();
            }
            catch (Throwable throwable)
            {
               m_transaction.rollback();
               throw throwable;
            }
         }
         catch (Throwable throwable)
         {
            // Print the error message here since if some error occurs 
            // below in finally we will never see this one 
            s_logger.log(Level.SEVERE, 
                                  "Unexpected error has occurred during test.", 
                                  throwable);
         }
         finally
         {
            m_transaction.begin();
            try
            {
               // Notice here that we are using connection which we requested
               // before the transaction has started. This connection should
               // be automatically enrolled in the transaction
               deleteStatement = m_connection.prepareStatement(DELETE_ALL);
   
               DatabaseUtils.executeUpdateAndClose(deleteStatement);
   
               m_transaction.commit();
            }
            catch (Throwable throwable)
            {
               m_transaction.rollback();
               throw throwable;
            }
   
            //******************************************************************
            // Now use new connection to see if all records were deleted
            iSelectedCount = 0;
            try 
            {
               // Request autocommit true since we are just reading data from the database
               connection = DatabaseConnectionFactoryImpl.getInstance().requestConnection(true);
               try
               {
                  selectStatement = connection.prepareStatement(SELECT_VALUE);
                  // TODO: Bug: WebLogic 9.1: The thread blocks here because it 
                  // didn't enroll the m_connection to execute previous delete 
                  // in the transaction and therefore it didn't committed
                  // that delete and therefore this select will be blocked
                  rsResults = selectStatement.executeQuery();
                  if (rsResults.next())
                  {
                     iSelectedCount = 1;
                  }
               }
               finally
               {   
                  DatabaseUtils.closeResultSetAndStatement(rsResults, selectStatement);
               }
            }
            finally
            {   
               DatabaseConnectionFactoryImpl.getInstance().returnConnection(connection);
               connection = null;
            }
            assertEquals("No record should exists in the database but there is a record", 
                         0, iSelectedCount);
   
            //******************************************************************
            // And now use the same connection to see if all records were delected
            iSelectedCount = 0;
            try 
            {
               selectStatement = m_connection.prepareStatement(SELECT_VALUE);
               rsResults = selectStatement.executeQuery();
               if (rsResults.next())
               {
                  iSelectedCount = 1;
               }
            }
            finally
            {   
               DatabaseUtils.closeResultSetAndStatement(rsResults, selectStatement);
            }
            assertEquals("No record should exists in the database but there is a record", 
                         0, iSelectedCount);
         }
      }
   
      /**
       * Test implemented to try to detect bug in JOTM which turned up to be 
       * bug in testCommitAfterUpdateSelect. Now the testCommitAfterUpdateSelect
       * and it still detects the bug in JOTM 1.4.2 and passing in JOTM 1.4.3 
       * 
       * @throws Throwable - an error has occurred during test
       */
      public void testCommitAfterUpdateDeleteWithAutoCommit(
      ) throws Throwable
      {
         final String DELETE_ALL = "delete from TRANSACTION_TEST";
         final String UPDATE_VALUE = "update TRANSACTION_TEST set TEST_VALUE = ? where TEST_ID = ?";
         final String INSERT_VALUE = "insert into TRANSACTION_TEST values (?, ?)";      
         final String SELECT_VALUE = "select TEST_VALUE from TRANSACTION_TEST where TEST_ID = 1";
         final String VALUE_TEST = "test_value";
         final String VALUE_TEST1 = "test_value_updated_1";
         
         Connection connection = null;
         PreparedStatement updateStatement = null;
         PreparedStatement deleteStatement = null;
         PreparedStatement selectStatement = null;
         PreparedStatement insertStatement = null;
         ResultSet         rsResults = null;
         int               iUpdatedCount = 0;
         int               iDeletedCount = 0;
         int               iSelectedCount = 0;
         int               iInsertCount = 0;
         String            strUpdatedValue = "";
   
         try
         {
            //******************************************************************
            // Try to select original record to verify that the database is in OK state
            try 
            {
               // This sequence is correct, don't change it. 
               // 1. First get the connection from the pool
               // Request autocommit false since we are modifying database
               // Notice that we are requesting the connection before we begin
               // the transaction. The transaction manager should handle this
               // and once the connection is used in the transaction it should
               // enroll it in the transaction
               connection = DatabaseConnectionFactoryImpl.getInstance().requestConnection(false);
      
               // 2. Now start transaction
               m_transaction.begin();
               try
               {
                  // 3. Not prepare statement with the global connection
                  // It should become part of the transaction
                  deleteStatement = connection.prepareStatement(DELETE_ALL);
                  iDeletedCount = DatabaseUtils.executeUpdateAndClose(deleteStatement);
      
                  // 4. Now commit or rollback
                  m_transaction.commit();
               }
               catch (Throwable throwable)
               {
                  m_transaction.rollback();
                  throw throwable;
               }
   
               // 5. And now try to execute insert outside of transaction
               // The connection has autocommit as true and therefore it should 
               // commit immidiately. In Weblogic 9.1 this commits the previous
               // transaction since it wasn't commited because Weblogic due to bug
               // #1491821  doesn't enroll that connection in transaction
               connection.setAutoCommit(true);
               try
               {
                  insertStatement = connection.prepareStatement(INSERT_VALUE);
                  insertStatement.setInt(1, 1); // ID
                  insertStatement.setString(2, VALUE_TEST1);
                  iInsertCount = insertStatement.executeUpdate();
               }
               finally
               {   
                  DatabaseUtils.closeStatement(insertStatement);
               }
            }
            finally
            {   
               DatabaseConnectionFactoryImpl.getInstance().returnConnection(connection);
               connection = null;
            }
            
            assertEquals("No records should be initially in the database.", 
                         0, iDeletedCount);
                                
            assertEquals("Exactly one record should be inserted.", 1, iInsertCount);
                                
            m_transaction.begin();
            try
            {
               // try to update the record back to it's original form
               // Request autocommit false since we are modifying database
               // Notice that we are requesting the connection withing the 
               // transaction. This connection should be enrolled in the 
               // transaction automatically 
               connection = DatabaseConnectionFactoryImpl.getInstance().requestConnection(false);
               try
               {
                  updateStatement = connection.prepareStatement(UPDATE_VALUE);
                  updateStatement.setString(1, VALUE_TEST1);
                  updateStatement.setInt(2, 1);
                  iUpdatedCount = DatabaseUtils.executeUpdateAndClose(updateStatement);
               }
               finally
               {   
                  DatabaseUtils.closeStatement(updateStatement);
                  DatabaseConnectionFactoryImpl.getInstance().returnConnection(connection);
                  connection = null;
               }
               m_transaction.commit();
            }
            catch (Throwable throwable)
            {
               m_transaction.rollback();
               throw throwable;
            }
   
            assertEquals("Exactly one record should have been updated.", 
                                1, iUpdatedCount);
   
            // Now try to verify if the result is in there using different connection
            //******************************************************************
            // try to select updated record
            iSelectedCount = 0;
            strUpdatedValue = "";
            try
            {
               // Set autocommit true since we are just reading data from the database
               m_connection.setAutoCommit(true);
               selectStatement = m_connection.prepareStatement(SELECT_VALUE,
                                                               ResultSet.TYPE_SCROLL_INSENSITIVE, 
                                                               ResultSet.CONCUR_READ_ONLY);
               rsResults = selectStatement.executeQuery();
               if (rsResults.last())
               { // The last row number will tell us the row count
                  iSelectedCount = rsResults.getRow();
                  rsResults.beforeFirst();
               }
               rsResults.first();
               strUpdatedValue = rsResults.getString(1);
            }
            finally
            {   
               DatabaseUtils.closeResultSetAndStatement(rsResults, selectStatement);
               selectStatement = null;
               DatabaseConnectionFactoryImpl.getInstance().returnConnection(connection);
               connection = null;
            }
            assertEquals("Exactly one record should have been selected.", 
                                1, iSelectedCount);
            assertEquals("Selected items should have updated value.", 
                                VALUE_TEST1, strUpdatedValue);
                                
            // Now try to update it back but WITHOUT transaction to see if autocommit works
            m_connection.setAutoCommit(true);
            try
            {
               // Use the global connection. It was part of transaction above
               // I want to see if it will become able to act standalone
               // and autocommit
               updateStatement = m_connection.prepareStatement(UPDATE_VALUE);
               updateStatement.setString(1, VALUE_TEST);
               updateStatement.setInt(2, 1);
               iUpdatedCount = DatabaseUtils.executeUpdateAndClose(updateStatement);
            }
            finally
            {   
               DatabaseUtils.closeStatement(updateStatement);
            }
            
            // Now try to verify if the result is in there using different connection
            //******************************************************************
            // try to select updated record but use a different connection than
            // the one above
            iSelectedCount = 0;
            strUpdatedValue = "";
            try
            {
               // Request autocommit true since we are just reading data from the database
               connection = DatabaseConnectionFactoryImpl.getInstance().requestConnection(true);
               selectStatement = connection.prepareStatement(SELECT_VALUE,
                                                             ResultSet.TYPE_SCROLL_INSENSITIVE, 
                                                             ResultSet.CONCUR_READ_ONLY);
               rsResults = selectStatement.executeQuery();
               if (rsResults.last())
               { // The last row number will tell us the row count
                  iSelectedCount = rsResults.getRow();
                  rsResults.beforeFirst();
               }
               rsResults.first();
               strUpdatedValue = rsResults.getString(1);
            }
            finally
            {   
               DatabaseUtils.closeResultSetAndStatement(rsResults, selectStatement);
               selectStatement = null;
               DatabaseConnectionFactoryImpl.getInstance().returnConnection(connection);
               connection = null;
            }
            assertEquals("Exactly one record should have been selected.", 
                                1, iSelectedCount);
            assertEquals("Selected items should have updated value.", 
                                VALUE_TEST, strUpdatedValue);
         }
         catch (Throwable throwable)
         {
            // Print the error message here since if some error occurs 
            // below in finally we will never see this one 
            s_logger.log(Level.SEVERE, 
                                  "Unexpected error has occurred during test.", 
                                  throwable);
         }
         finally
         {
            try
            {
               // Get the connection before transaction
               // It should be automatically enrolled in the transaction
               // Request autocommit false since we are modifying data
               connection = DatabaseConnectionFactoryImpl.getInstance().requestConnection(false);
               
               // Now delete all the records
               m_transaction.begin();
               try
               {
                  deleteStatement = connection.prepareStatement(DELETE_ALL);
                  DatabaseUtils.executeUpdateAndClose(deleteStatement);
      
                  m_transaction.commit();
               }
               catch (Throwable throwable2)
               {
                  m_transaction.rollback();
                  throw throwable2;
               }
      
               //******************************************************************
               // Now use new connection to see if all records were delected
               iSelectedCount = 0;
               // Request autocommit true since we are just reading data from the database
               m_connection.setAutoCommit(true);
               try
               {
                  selectStatement = m_connection.prepareStatement(SELECT_VALUE);
                  rsResults = selectStatement.executeQuery();
                  if (rsResults.next())
                  {
                     iSelectedCount = 1;
                  }
               }
               finally
               {   
                  DatabaseUtils.closeResultSetAndStatement(rsResults, selectStatement);
                  selectStatement = null;
               }
               assertEquals("No record should exists in the database but there is a record", 
                             0, iSelectedCount);
      
               //******************************************************************
               // And now use the same connection to see if all records were delected
               iSelectedCount = 0;
               try 
               {
                  connection.setAutoCommit(true);
                  selectStatement = connection.prepareStatement(SELECT_VALUE);
                  rsResults = selectStatement.executeQuery();
                  if (rsResults.next())
                  {
                     iSelectedCount = 1;
                  }
               }
               finally
               {   
                  DatabaseUtils.closeResultSetAndStatement(rsResults, selectStatement);
                  selectStatement = null;
               }
               assertEquals("No record should exists in the database but there is a record", 
                            0, iSelectedCount);
            }
            finally
            {   
               DatabaseConnectionFactoryImpl.getInstance().returnConnection(connection);
               connection = null;
            }
         }
      }
   
      /**
       * Test implemented to try to detect bug in JDBC Transaction manager which 
       * was causing the transaction to stay active if nothing was done during 
       * the transaction 
       * 
       * @throws Throwable - an error has occurred during test
       */
      public void testEmptyCommit(
      ) throws Throwable
      {
         // The bug discovered by this test was visible only if the first transaction
         // was empty so reset the transaction factory so that we do not depend
         // on what the other tests did. Return the globally allocated connection
         // and reset it to null so that is is not returned again as well otherwise
         // we will try to return the connection about which the DatabaseTransactionFactory
         // doesn't know since it was reset
         DatabaseConnectionFactoryImpl.getInstance().returnConnection(m_connection);
         m_connection = null;
         m_iRequestedConnectionCount--;
         DatabaseTransactionFactoryImpl.getInstance().reset();
         
         m_transaction.begin();
         try
         {
            // Do nothing here
            m_transaction.commit();
         }
         catch (Throwable throwable)
         {
            m_transaction.rollback();
            throw throwable;
         }
         // Now do it again and it should still work (it wasn't)
         m_transaction.begin();
         try
         {
            // Do nothing here
            m_transaction.commit();
         }
         catch (Throwable throwable)
         {
            m_transaction.rollback();
            throw throwable;
         }
      }
   
      /**
       * Test implemented to try to detect bug in JDBC Transaction manager which 
       * was causing the transaction to stay active if nothing was done during 
       * the transaction 
       * 
       * @throws Throwable - an error has occurred during test
       */
      public void testEmptyRollback(
      ) throws Throwable
      {
         // The bug discovered by this test was visible only if the first transaction
         // was empty so reset the transaction factory so that we do not depend
         // on what the other tests did. Return the globally allocated connection
         // and reset it to null so that is is not returned again as well otherwise
         // we will try to return the connection about which the DatabaseTransactionFactory
         // doesn't know since it was reset
         DatabaseConnectionFactoryImpl.getInstance().returnConnection(m_connection);
         m_connection = null;
         m_iRequestedConnectionCount--;
         DatabaseTransactionFactoryImpl.getInstance().reset();
         
         m_transaction.begin();
         // Do nothing here
         m_transaction.rollback();
         // Now do it again and it should still work (it wasn't)
         m_transaction.begin();
         // Do nothing here
         m_transaction.rollback();
      }
   }
}
