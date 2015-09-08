/*
 * Copyright (c) 2003 - 2007 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: MySQLDatabaseTestSchema.java,v 1.20 2009/04/22 06:17:44 bastafidli Exp $
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

package org.opensubsystems.core.persist.db.driver.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.persist.db.driver.DatabaseTestSchema;
import org.opensubsystems.core.util.Log;
import org.opensubsystems.core.util.jdbc.DatabaseUtils;

/**
 * This class encapsulates details about creation and upgrade
 * of database schema required to test database driver functionality
 * for tables which are MySQL database specific 
 *
 * @version $Id: MySQLDatabaseTestSchema.java,v 1.20 2009/04/22 06:17:44 bastafidli Exp $
 * @author Julo Legeny
 * @code.reviewer Miro Halas
 * @code.reviewed Initial revision
 */
public class MySQLDatabaseTestSchema extends DatabaseTestSchema
{   
   /*
   These tables are database specific 
   
      CREATE TABLE GENERATEDKEY_TEST
      (
         TEST_KEY INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY, 
         TEST_VALUE VARCHAR(50) NOT NULL
      ) TYPE=INNODB

      CREATE TABLE RESULTSET_TEST
      (
         RESULTSET_TEST VARCHAR(20) NOT NULL
      ) TYPE=INNODB
      
      CREATE TABLE DATE_TEST 
      (
         DATE_TEST DATE NOT NULL
      ) TYPE=INNODB
      
      CREATE TABLE TIME_TEST 
      (
         TIME_TEST TIME NOT NULL
      ) TYPE=INNODB
      
      CREATE TABLE TIMESTAMP_TEST 
      (
         TIMESTAMP_TEST TIMESTAMP NOT NULL
      ) TYPE=INNODB

     
      CREATE TABLE TRANSACTION_TEST
      (
         TEST_ID INTEGER NOT NULL,
         TEST_VALUE VARCHAR(50) NOT NULL,
         CONSTRAINT TEST_ID_PK PRIMARY KEY (TEST_ID)         
      ) TYPE=INNODB

      CREATE TABLE TRANSACTION_RELATED_TEST
      (
         TEST_REL_ID   INTEGER NOT NULL,
         TEST_ID   INTEGER NOT NULL,
         TEST_VALUE VARCHAR(50),
         INDEX IND_TRAN_TEST_ID (TEST_ID),
         CONSTRAINT TEST_ID_FK FOREIGN KEY (TEST_ID)
         REFERENCES TRANSACTION_TEST (TEST_ID) ON DELETE CASCADE         
      ) TYPE=INNODB
      
      CREATE TABLE DELETE_TEST
      (
         TEST_ID   INTEGER NOT NULL,
         TEST_VALUE VARCHAR(50) NOT NULL,
         CONSTRAINT DTEST_ID_PK PRIMARY KEY (TEST_ID)         
      ) TYPE=INNODB

      CREATE TABLE DELETE_RELATED_TEST
      (
         TEST_REL_ID   INTEGER NOT NULL,
         TEST_ID   INTEGER NOT NULL,
         TEST_VALUE VARCHAR(50),
         INDEX IND_DEL_TEST_ID (TEST_ID),
         CONSTRAINT DTEST_ID_FK FOREIGN KEY (TEST_ID)
         REFERENCES DELETE_TEST (TEST_ID) ON DELETE CASCADE         
      ) TYPE=INNODB

      CREATE TABLE OWN_FK_TEST
      (
         TEST_ID INTEGER NOT NULL,
         FK_ID INTEGER,
         INDEX IND_OWN_FK_ID (FK_ID),
         CONSTRAINT OWN_FK_TEST_PK PRIMARY KEY (TEST_ID),
         CONSTRAINT OWN_FK_TEST_FK FOREIGN KEY (FK_ID)
         REFERENCES OWN_FK_TEST (TEST_ID) ON DELETE CASCADE
      ) TYPE=INNODB

      CREATE TABLE GROUP_BASE_TEST
      (
         TEST_BASE_ID   INTEGER NOT NULL,
         TEST_BASE_VALUE INTEGER,
         CONSTRAINT GROUP_BASE_TEST_PK PRIMARY KEY (TEST_BASE_ID)
      ) TYPE=INNODB

      CREATE TABLE GROUP_CHILD_TEST
      (
         TEST_CHILD_ID   INTEGER NOT NULL,
         TEST_BASE_FK_ID INTEGER NOT NULL,
         TEST_CHILD_VALUE INTEGER,
         INDEX IND_CHILD_TEST_BASE_FK_ID (TEST_CHILD_ID),
         CONSTRAINT GROUP_CHILD_TEST_PK PRIMARY KEY (TEST_CHILD_ID),
         CONSTRAINT TEST_BASE_ID_FK FOREIGN KEY (TEST_BASE_FK_ID)
         REFERENCES GROUP_BASE_TEST (TEST_BASE_ID) ON DELETE CASCADE
      ) TYPE=INNODB

      CREATE TABLE SAME_TEST1
      (
         ID   INTEGER NOT NULL
      ) TYPE=INNODB

      CREATE TABLE SAME_TEST2
      (
         ID   INTEGER NOT NULL
      ) TYPE=INNODB

      CREATE TABLE POOL_TEST
      (
         TEST_VALUE INTEGER NOT NULL,
         CONSTRAINT TEST_VALUE_PK PRIMARY KEY (TEST_VALUE)         
      ) TYPE=INNODB
      
      CREATE TABLE POOL_TEST2
      (
         TEST_VALUE INTEGER NOT NULL,
         CONSTRAINT TEST_VALUE_PK PRIMARY KEY (TEST_VALUE)         
      ) TYPE=INNODB

      CREATE TABLE QUERY_TEST
      (
         VALUE_1 INTEGER NOT NULL,
         VALUE_2 INTEGER NOT NULL
      ) TYPE=INNODB

      CREATE table QUERY_TEST_EXCEPT (
         VALUE_1 INTEGER NOT NULL
      ) TYPE=INNODB

      CREATE TABLE UNIQUE_COLUMN_TEST 
      (
         TEST_ID INTEGER,
         CONSTRAINT TEST_ID_UQ UNIQUE (TEST_ID)
      ) TYPE=INNODB

      CREATE TABLE NULL_COLUMN_TEST (
         NAME VARCHAR(50)
      ) TYPE=INNODB

   */
   
   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(MySQLDatabaseTestSchema.class);

   // Constructors /////////////////////////////////////////////////////////////

   /**
    * Default constructor.
    * 
    * @throws OSSException - error occurred.
    */
   public MySQLDatabaseTestSchema(
   ) throws OSSException
   {
      super();
   }     

   // Lifecycle events /////////////////////////////////////////////////////////

   /**
    * {@inheritDoc}
    */
   public void create(
      Connection cntDBConnection,
      String     strUserName
   ) throws SQLException
   {
      // There will be not called super for creating generic tables, because
      // MySQL has to have created tables using TYPE=INNODB 
      // super.create(cntDBConnection, strUserName);

      // Now try to create any database specific tables      
      Statement stmQuery = null;
      try
      {        
         stmQuery = cntDBConnection.createStatement();

         ///////////////////////////////////////////////////////////////////////

         if (stmQuery.execute("CREATE TABLE GENERATEDKEY_TEST" + NL +
                              "(" + NL +
                              "   TEST_KEY INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY," + NL + 
                              "   TEST_VALUE VARCHAR(50) NOT NULL" + NL +
                              ") TYPE=INNODB"))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         s_logger.log(Level.FINEST, "Table GENERATEDKEY_TEST created.");
         /*
         if (stmQuery.execute("grant all on GENERATEDKEY_TEST to " + strUserName))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         s_logger.log(Level.FINEST, 
                      "Access for table GENERATEDKEY_TEST set for user " 
                      + strUserName);
         */

         ///////////////////////////////////////////////////////////////////////

         if (stmQuery.execute("CREATE TABLE RESULTSET_TEST" + NL +
                              "(" + NL +
                              "   RESULTSET_TEST VARCHAR(20) NOT NULL" + NL + 
                              ") TYPE=INNODB"))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         s_logger.log(Level.FINEST, "Table RESULTSET_TEST created.");
         /*
         if (stmQuery.execute("grant all on RESULTSET_TEST to " + strUserName))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         Log.getLogger().log(Level.FINEST, 
                             "Access for table RESULTSET_TEST set for user " 
                             + strUserName);
         */                             

         ///////////////////////////////////////////////////////////////////////

         if (stmQuery.execute("CREATE TABLE DATE_TEST" + NL +
                              "(" + NL +
                              "   DATE_TEST DATE NOT NULL" + NL + 
                              ") TYPE=INNODB"))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         s_logger.log(Level.FINEST, "Table DATE_TEST created.");
         /*
         if (stmQuery.execute("grant all on DATE_TEST to " + strUserName))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         Log.getLogger().log(Level.FINEST, 
                             "Access for table DATE_TEST set for user " 
                             + strUserName);
         */

         ///////////////////////////////////////////////////////////////////////

         if (stmQuery.execute("CREATE TABLE TIME_TEST" + NL +
                              "(" + NL +
                              "   TIME_TEST TIME NOT NULL" + NL + 
                              ") TYPE=INNODB"))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         s_logger.log(Level.FINEST, "Table TIME_TEST created.");
         /*
         if (stmQuery.execute("grant all on TIME_TEST to " + strUserName))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         Log.getLogger().log(Level.FINEST, 
                             "Access for table TIME_TEST set for user " 
                             + strUserName);
         */

         ///////////////////////////////////////////////////////////////////////

         if (stmQuery.execute("CREATE TABLE TIMESTAMP_TEST" + NL + 
                              "(" + NL +
                              "   TIMESTAMP_TEST TIMESTAMP NOT NULL" + NL + 
                              ") TYPE=INNODB"))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         s_logger.log(Level.FINEST, "Table TIMESTAMP_TEST created.");
         /*
         if (stmQuery.execute("grant all on TIMESTAMP_TEST to " + strUserName))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         Log.getLogger().log(Level.FINEST, 
                             "Access for table TIMESTAMP_TEST set for user " 
                             + strUserName);
         */

         ///////////////////////////////////////////////////////////////////////

         if (stmQuery.execute("CREATE TABLE TRANSACTION_TEST" + NL +
                              "(" + NL +
                              "   TEST_ID INTEGER UNSIGNED UNIQUE NOT NULL," + NL +
                              "   TEST_VALUE VARCHAR(50) NOT NULL," + NL + 
                              "   CONSTRAINT TEST_ID_PK PRIMARY KEY (TEST_ID)" + NL +
                              ") TYPE=INNODB"))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         s_logger.log(Level.FINEST, "Table TRANSACTION_TEST created.");
         /*
         if (stmQuery.execute("grant all on TRANSACTION_TEST to " + strUserName))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         Log.getLogger().log(Level.FINEST, 
                             "Access for table TRANSACTION_TEST set for user " 
                             + strUserName);
         */

         ///////////////////////////////////////////////////////////////////////

         if (stmQuery.execute("CREATE TABLE TRANSACTION_RELATED_TEST" + NL +
                              "(" + NL +
                              "   TEST_REL_ID INTEGER NOT NULL," + NL +
                              "   TEST_ID  INTEGER UNSIGNED NOT NULL," + NL +
                              "   TEST_VALUE VARCHAR(50)," + NL +
                              "   INDEX IND_TRAN_TEST_ID (TEST_ID)," + NL +
                              "   CONSTRAINT TEST_ID_FK FOREIGN KEY (TEST_ID)" + NL +
                              "   REFERENCES TRANSACTION_TEST (TEST_ID) ON DELETE CASCADE" + NL +
                              ") TYPE=INNODB"))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         s_logger.log(Level.FINEST, "Table TRANSACTION_RELATED_TEST created.");
         /*
         if (stmQuery.execute("grant all on TRANSACTION_RELATED_TEST to " + strUserName))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         Log.getLogger().log(Level.FINEST, 
                             "Access for table TRANSACTION_RELATED_TEST set for user " 
                             + strUserName);
         */
         
         ///////////////////////////////////////////////////////////////////////

         if (stmQuery.execute("CREATE TABLE DELETE_TEST" + NL +
                              "(" + NL +
                              "   TEST_ID INTEGER NOT NULL," + NL +
                              "   TEST_VALUE VARCHAR(50) NOT NULL," + NL +
                              "   CONSTRAINT DTEST_ID_PK PRIMARY KEY (TEST_ID)" + NL +
                              ") TYPE=INNODB"))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         s_logger.log(Level.FINEST, "Table DELETE_TEST created.");
         /*
         if (stmQuery.execute("grant all on DELETE_TEST to " + strUserName))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         Log.getLogger().log(Level.FINEST, 
                             "Access for table DELETE_TEST set for user " 
                             + strUserName);
         */

         ///////////////////////////////////////////////////////////////////////

         if (stmQuery.execute("CREATE TABLE DELETE_RELATED_TEST" + NL +
                              "(" + NL +
                              "   TEST_REL_ID INTEGER NOT NULL," + NL +
                              "   TEST_ID INTEGER NOT NULL," + NL +
                              "   TEST_VALUE VARCHAR(50)," + NL +
                              "   INDEX IND_DEL_TEST_ID (TEST_ID)," + NL +
                              "   CONSTRAINT DTEST_ID_FK FOREIGN KEY (TEST_ID)" + NL +
                              "   REFERENCES DELETE_TEST (TEST_ID) ON DELETE CASCADE" + NL +
                              ") TYPE=INNODB"))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         s_logger.log(Level.FINEST, "Table DELETE_RELATED_TEST created.");
         /*
         if (stmQuery.execute("grant all on DELETE_RELATED_TEST to " + strUserName))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         Log.getLogger().log(Level.FINEST, 
                             "Access for table DELETE_RELATED_TEST set for user " 
                             + strUserName);
         */
         ///////////////////////////////////////////////////////////////////////

         if (stmQuery.execute("CREATE TABLE ROLLBACK_TEST" + NL +
                              "(" + NL +
                              "   TEST_COLUMN VARCHAR(50) NOT NULL," + NL +
                              "   CONSTRAINT TEST_COLUMN_UQ UNIQUE (TEST_COLUMN)" + NL +
                              ") TYPE=INNODB"))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         s_logger.log(Level.FINEST, "Table TRANSACTION_TEST created.");
         /*
         if (stmQuery.execute("grant all on TRANSACTION_TEST to " + strUserName))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         Log.getLogger().log(Level.FINEST, 
                             "Access for table TRANSACTION_TEST set for user " 
                             + strUserName);
         */

         ///////////////////////////////////////////////////////////////////////

         if (stmQuery.execute("CREATE TABLE OWN_FK_TEST " + NL +
                              "( " + NL +
                              "   TEST_ID INTEGER NOT NULL, " + NL +
                              "   FK_ID INTEGER," + NL +
                              "   INDEX IND_OWN_FK_ID (FK_ID)," + NL +
                              "   CONSTRAINT OWN_FK_TEST_PK PRIMARY KEY (TEST_ID), " + NL +
                              "   CONSTRAINT OWN_FK_TEST_FK FOREIGN KEY (FK_ID) " +  NL +
                              "   REFERENCES OWN_FK_TEST (TEST_ID) ON DELETE CASCADE" + NL +
                              ") TYPE=INNODB"))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         s_logger.log(Level.FINEST, "Table OWN_FK_TEST created.");
         /*
         if (stmQuery.execute("grant all on TRANSACTION_TEST to " + strUserName))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         Log.getLogger().log(Level.FINEST, 
                             "Access for table TRANSACTION_TEST set for user " 
                             + strUserName);
         */   
         
         /////////////////////////////////////////////////////////////////////// 

         if (stmQuery.execute("CREATE TABLE GROUP_BASE_TEST" + NL +
                              "(" + NL +
                              "   TEST_BASE_ID INTEGER NOT NULL, " + NL +
                              "   TEST_BASE_VALUE INTEGER," + NL +
                              "   CONSTRAINT GROUP_BASE_TEST_PK PRIMARY KEY (TEST_BASE_ID)" + NL +
                              ") TYPE=INNODB"))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         s_logger.log(Level.FINEST, "Table GROUP_BASE_TEST created.");
         /*
         if (stmQuery.execute("grant all on GROUP_BASE_TEST to " + strUserName))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         Log.getLogger().log(Level.FINEST, 
                             "Access for table GROUP_BASE_TEST set for user " 
                             + strUserName);
         */

         ///////////////////////////////////////////////////////////////////////

         if (stmQuery.execute(
                "CREATE TABLE GROUP_CHILD_TEST" + NL +
                "(" + NL +
                "   TEST_CHILD_ID INTEGER NOT NULL, " + NL +
                "   TEST_BASE_FK_ID INTEGER NOT NULL, " + NL +
                "   TEST_CHILD_VALUE INTEGER," + NL +
                "   INDEX IND_CHILD_TEST_BASE_FK_ID (TEST_BASE_FK_ID)," + NL +
                "   CONSTRAINT GROUP_CHILD_TEST_PK PRIMARY KEY (TEST_CHILD_ID)," + NL +
                "   CONSTRAINT TEST_BASE_ID_FK FOREIGN KEY (TEST_BASE_FK_ID) " + NL +
                "   REFERENCES GROUP_BASE_TEST (TEST_BASE_ID) ON DELETE CASCADE" + NL +
                ") TYPE=INNODB"))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         s_logger.log(Level.FINEST, "Table GROUP_CHILD_TEST created.");
         /*
         if (stmQuery.execute("grant all on GROUP_CHILD_TEST to " + strUserName))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         Log.getLogger().log(Level.FINEST, 
                             "Access for table GROUP_CHILD_TEST set for user " 
                             + strUserName);
         */

         ///////////////////////////////////////////////////////////////////////

         if (stmQuery.execute("CREATE TABLE SAME_TEST1" + NL +
                              "(" + NL +
                              "   ID INTEGER NOT NULL" + NL +
                              ") TYPE=INNODB"))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         s_logger.log(Level.FINEST, "Table SAME_TEST1 created.");
         /*
         if (stmQuery.execute("grant all on SAME_TEST1 to " + strUserName))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         Log.getLogger().log(Level.FINEST, 
                             "Access for table SAME_TEST1 set for user " 
                             + strUserName);
         */

         ///////////////////////////////////////////////////////////////////////

         if (stmQuery.execute("CREATE TABLE SAME_TEST2 " + NL +
                              "(" + NL +
                              "   ID INTEGER NOT NULL" + NL +
                              ") TYPE=INNODB"))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         s_logger.log(Level.FINEST, "Table SAME_TEST2 created.");
         /*
         if (stmQuery.execute("grant all on SAME_TEST2 to " + strUserName))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         Log.getLogger().log(Level.FINEST, 
                             "Access for table SAME_TEST2 set for user " 
                             + strUserName);
         */

         ///////////////////////////////////////////////////////////////////////

         if (stmQuery.execute("CREATE TABLE POOL_TEST " + NL +
                              "(" + NL +
                              "   TEST_VALUE INTEGER NOT NULL," + NL +
                              "   CONSTRAINT TEST_VALUE1_PK PRIMARY KEY (TEST_VALUE)" + NL +
                              ") TYPE=INNODB"))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         s_logger.log(Level.FINEST, "Table POOL_TEST created.");
         /*
         if (stmQuery.execute("grant all on POOL_TEST to " + strUserName))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         Log.getLogger().log(Level.FINEST, 
                             "Access for table POOL_TEST set for user " 
                             + strUserName);
         */

         ///////////////////////////////////////////////////////////////////////

         if (stmQuery.execute("CREATE TABLE POOL_TEST2 " + NL +
                              "(" + NL +
                              "   TEST_VALUE INTEGER NOT NULL," + NL +
                              "   CONSTRAINT TEST_VALUE2_PK PRIMARY KEY (TEST_VALUE)" + NL +
                              ") TYPE=INNODB"))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         s_logger.log(Level.FINEST, "Table POOL_TEST2 created.");
         /*
         if (stmQuery.execute("grant all on POOL_TEST2 to " + strUserName))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         Log.getLogger().log(Level.FINEST, 
                             "Access for table POOL_TEST set for user " 
                             + strUserName);
         */
         
         ///////////////////////////////////////////////////////////////////////

         if (stmQuery.execute("CREATE TABLE QUERY_TEST " + NL +
                              "(" + NL +
                              "   VALUE_1 INTEGER NOT NULL," + NL +
                              "   VALUE_2 INTEGER NOT NULL" + NL +
                              ") TYPE=INNODB"))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         s_logger.log(Level.FINEST, "Table QUERY_TEST created.");
         /*
         if (stmQuery.execute("grant all on QUERY_TEST to " + strUserName))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         Log.getLogger().log(Level.FINEST, 
                             "Access for table QUERY_TEST set for user " 
                             + strUserName);
         */

         ///////////////////////////////////////////////////////////////////////

         if (stmQuery.execute("CREATE TABLE QUERY_TEST_EXCEPT " + NL +
                              "(" + NL +
                              "   VALUE_1 INTEGER NOT NULL" + NL +
                              ") TYPE=INNODB"))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         s_logger.log(Level.FINEST, "Table QUERY_TEST_EXCEPT created.");
         /*
         if (stmQuery.execute("grant all on QUERY_TEST to " + strUserName))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         Log.getLogger().log(Level.FINEST, 
                             "Access for table QUERY_TEST set for user " 
                             + strUserName);
         */

         ///////////////////////////////////////////////////////////////////////

         if (stmQuery.execute("CREATE TABLE UNIQUE_COLUMN_TEST " + NL +
                              "(" + NL +
                              "   TEST_ID INTEGER, " + NL +
                              "   CONSTRAINT TEST_ID_UQ UNIQUE (TEST_ID) " + NL +
                              ") TYPE=INNODB"))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         s_logger.log(Level.FINEST, "Table UNIQUE_COLUMN_TEST created.");
         /*
         if (stmQuery.execute("grant all on UNIQUE_COLUMN_TEST to " + strUserName))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         Log.getLogger().log(Level.FINEST, 
                             "Access for table UNIQUE_COLUMN_TEST set for user " 
                             + strUserName);
         */

         ///////////////////////////////////////////////////////////////////////         

         if (stmQuery.execute("CREATE TABLE NULL_COLUMN_TEST " + NL +
                              "(" + NL +
                              "   NAME VARCHAR(50)" + NL +
                              ") TYPE=INNODB"))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         s_logger.log(Level.FINEST, "Table NULL_COLUMN_TEST created.");
         /*
         if (stmQuery.execute("grant all on NULL_COLUMN_TEST to " + strUserName))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         Log.getLogger().log(Level.FINEST, 
                             "Access for table NULL_COLUMN_TEST set for user " 
                             + strUserName);
         */         


      }
      catch (SQLException sqleExc)
      {
         s_logger.log(Level.WARNING, "Failed to create database test schema.", 
                             sqleExc);
         throw sqleExc;
      }
      finally
      {
         DatabaseUtils.closeStatement(stmQuery);
      }
   }

   /**
    * {@inheritDoc}
    */
   public String getInsertGeneratedKey(
   )
   {
      return "INSERT INTO generatedkey_test(test_key, test_value) " +
              "VALUES (null, ?)";
   }

   /**
    * {@inheritDoc}
    */
   public int[] executeInsertGeneratedKey2(
      Connection dbConnection,
      String     strValue
   ) throws SQLException
   {
      PreparedStatement insertStatement = null;
      Statement selectStatement = null;
      // CallableStatement callStatement = null;
      ResultSet         rsResults = null;
      int               iInsertCount = 0;
      int               iGeneratedKey = 0;
      int[]             returnValues = null;
      
      try
      {
         insertStatement = dbConnection.prepareStatement(getInsertGeneratedKey());
         insertStatement.setString(1, strValue);
         iInsertCount = insertStatement.executeUpdate();

         selectStatement = dbConnection.createStatement();

         rsResults = selectStatement.executeQuery("SELECT LAST_INSERT_ID() FROM generatedkey_test");
         if (rsResults.next())
         {
            iGeneratedKey = rsResults.getInt(1);

            returnValues = new int[2];
            returnValues[0] = iInsertCount;
            returnValues[1] = iGeneratedKey;
         }
      }
      finally
      {
         DatabaseUtils.closeStatement(insertStatement);
         DatabaseUtils.closeResultSetAndStatement(rsResults, selectStatement);
      }
      
      return returnValues;
   }


   /**
    * {@inheritDoc} 
    */
   public int executeUpdateTestValue(
      Connection dbConnection,
      String     strOldValue,
      String     strNewValue
   ) throws SQLException
   {
      PreparedStatement updateStatement = null;
      int               iUpdateCount   = 0;

      try
      {
         updateStatement = dbConnection.prepareStatement(
              "update TRANSACTION_TEST set TEST_VALUE = ? where TEST_VALUE = ?");
         updateStatement.setString(1, strNewValue);
         updateStatement.setString(2, strOldValue);
            
         // here is the bug in SAP DB which is not seen in HSQLDB, if there is 
         // called stored procedure without output parameters, there is not 
         // returned number of updated records   
         iUpdateCount = updateStatement.executeUpdate();
      }
      finally
      {
         DatabaseUtils.closeStatement(updateStatement);
      }
      
      return iUpdateCount;
   }

   /**
    * {@inheritDoc}
    */
   public int[] executeInsertRow(
      Connection dbConnection, 
      String strValue) 
   throws SQLException
   {
      PreparedStatement insertStatement = null;
      Statement selectStatement = null;
      ResultSet         rsResults = null;
      int               iInsertCount = 0;
      int               iInsertCountReturnedFromSP = -1;
      int[]             returnValues = null;
      
      try
      {
         insertStatement = dbConnection.prepareStatement(getInsertGeneratedKey());
         insertStatement.setString(1, strValue);
         iInsertCount = insertStatement.executeUpdate();

         selectStatement = dbConnection.createStatement();

         // Returning number of affected rows is supported from MySQL version 5
         // rsResults = selectStatement.executeQuery("SELECT ROW_COUNT();");
         // if (rsResults.next())
         // {
            // iGeneratedKey = rsResults.getInt(1);

            returnValues = new int[2];

            // value (number of affected rows) returned from insertStatement.executeUpdate();
            returnValues[0] = iInsertCount;

            // Value (number of inserted rows) returned from stored procedure. 
            // It will be always -1 here, because MySQL doesn't support stored 
            // procedures.
            returnValues[1] = iInsertCountReturnedFromSP;
         // }
      }
      finally
      {
         DatabaseUtils.closeStatement(insertStatement);
         DatabaseUtils.closeResultSetAndStatement(rsResults, selectStatement);
      }
      // MySQL doesn't support stored procedures 
      return returnValues;
   }

   /**
    * {@inheritDoc}
    */
   public void createTestUser(
      Connection cntAdminDBConnection, 
      String strDatabaseURL,
      String strUserName,
      String strUserPassword
   ) throws SQLException
   {
      Statement stmQuery = null;
      try
      {
         // Parse database name from URL.
         String strDatabaseName = strDatabaseURL.substring(
                                     strDatabaseURL.lastIndexOf("/") + 1, 
                                     strDatabaseURL.length());

         String strCreateUserQuery = "GRANT Select, Insert, Update, Delete, Index, Alter, " + 
                                     "Create, Drop, References ON " + strDatabaseName + ".* TO '" + 
                                     strUserName + "'@'localhost' IDENTIFIED BY '"
                                     + strUserPassword + "'";

         stmQuery = cntAdminDBConnection.createStatement();

         if (stmQuery.execute(strCreateUserQuery))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
      }
      finally
      {
         DatabaseUtils.closeStatement(stmQuery);
      }                        
   }

   /**
    * {@inheritDoc}
    */
   public void dropTestUser(
      Connection cntAdminDBConnection,
      String strDatabaseURL,
      String strUserName
   ) throws SQLException
   {
      Statement stmQuery = null;
      try
      {
         // Parse host name from URL 
         String strHost = strDatabaseURL.substring(
                             strDatabaseURL.indexOf("://") + 3, 
                             strDatabaseURL.lastIndexOf("/")); 

         String strDropUserQuery = "DELETE FROM mysql.user WHERE User='" + strUserName + "' " +
                                   "AND Host='" + strHost + "'";

         stmQuery = cntAdminDBConnection.createStatement();

         if (stmQuery.execute(strDropUserQuery))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
      }
      finally
      {
         DatabaseUtils.closeStatement(stmQuery);
      }                        
   }
}
