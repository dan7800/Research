/*
 * Copyright (c) 2003 - 2007 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: MSSQLDatabaseTestSchema.java,v 1.19 2009/04/22 06:17:43 bastafidli Exp $
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

package org.opensubsystems.core.persist.db.driver.mssql;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.persist.db.driver.DatabaseTestSchema;
import org.opensubsystems.core.util.Log;
import org.opensubsystems.core.util.jdbc.DatabaseUtils;

/**
 * This class encapsulates details about creation and upgrade
 * of database schema required to test database driver functionality
 * for tables which are MS SQL Application database specific 
 *
 * @version $Id: MSSQLDatabaseTestSchema.java,v 1.19 2009/04/22 06:17:43 bastafidli Exp $
 * @author Julo Legeny
 * @code.reviewer Miro Halas
 * @code.reviewed 1.14 2005/02/26 21:13:42 bastafidli
 */
public class MSSQLDatabaseTestSchema extends DatabaseTestSchema
{   
   /*
      These tables are database specific 
      
      CREATE TABLE GENERATEDKEY_TEST 
      (
         TEST_KEY INTEGER IDENTITY(1,1) NOT NULL PRIMARY KEY,
         TEST_VALUE VARCHAR(50) NOT NULL
      )

      CREATE TABLE RESULTSET_TEST
      (
         RESULTSET_TEST VARCHAR(20) NOT NULL
      )
      
      CREATE TABLE DATE_TEST 
      (
         DATE_TEST SMALLDATETIME NOT NULL
      )
      
      CREATE TABLE TIME_TEST 
      (
         TIME_TEST SMALLDATETIME NOT NULL
      )
      
      CREATE TABLE TIMESTAMP_TEST 
      (
         TIMESTAMP_TEST DATETIME NOT NULL
      )

      CREATE TABLE GENERATEDKEY_TEST 
      (
         TEST_KEY   INTEGER IDENTITY,
         TEST_VALUE VARCHAR(50) NOT NULL
      )
      
      CREATE TABLE TRANSACTION_TEST
      (
         TEST_ID   INTEGER NOT NULL,
         TEST_VALUE VARCHAR(50) NOT NULL,
         CONSTRAINT TEST_ID_PK PRIMARY KEY (TEST_ID)         
      )

      CREATE TABLE TRANSACTION_RELATED_TEST
      (
         TEST_REL_ID   INTEGER NOT NULL,
         TEST_ID   INTEGER NOT NULL,
         CONSTRAINT TEST_ID_FK FOREIGN KEY (TEST_ID)
         REFERENCES TRANSACTION_TEST (TEST_ID) ON DELETE CASCADE         
      )
      
      CREATE TABLE DELETE_TEST
      (
         TEST_ID   INTEGER NOT NULL,
         TEST_VALUE VARCHAR(50) NOT NULL,
         CONSTRAINT DTEST_ID_PK PRIMARY KEY (TEST_ID)         
      )

      CREATE TABLE DELETE_RELATED_TEST
      (
         TEST_REL_ID   INTEGER NOT NULL,
         TEST_ID   INTEGER NOT NULL,
         TEST_VALUE VARCHAR(50),
         CONSTRAINT DTEST_ID_FK FOREIGN KEY (TEST_ID)
         REFERENCES DELETE_TEST (TEST_ID) ON DELETE CASCADE         
      )

      CREATE TABLE OWN_FK_TEST
      (
         TEST_ID INTEGER NOT NULL,
         FK_ID INTEGER,
         CONSTRAINT OWN_FK_TEST_PK PRIMARY KEY (TEST_ID),
         CONSTRAINT OWN_FK_TEST_FK FOREIGN KEY (FK_ID)
         REFERENCES OWN_FK_TEST (TEST_ID) ON DELETE NO ACTION
      )

      CREATE TABLE GROUP_BASE_TEST
      (
         TEST_BASE_ID   INTEGER NOT NULL,
         TEST_BASE_VALUE INTEGER,
         CONSTRAINT GROUP_BASE_TEST_PK PRIMARY KEY (TEST_BASE_ID)
      )

      CREATE TABLE GROUP_CHILD_TEST
      (
         TEST_CHILD_ID   INTEGER NOT NULL,
         TEST_BASE_FK_ID INTEGER NOT NULL,
         TEST_CHILD_VALUE INTEGER,
         CONSTRAINT GROUP_CHILD_TEST_PK PRIMARY KEY (TEST_CHILD_ID),
         CONSTRAINT TEST_BASE_ID_FK FOREIGN KEY (TEST_BASE_FK_ID)
         REFERENCES GROUP_BASE_TEST (TEST_BASE_ID) ON DELETE CASCADE
      )

      CREATE TABLE SAME_TEST1
      (
         ID   INTEGER NOT NULL
      )

      CREATE TABLE SAME_TEST2
      (
         ID   INTEGER NOT NULL
      )

      CREATE TABLE POOL_TEST
      (
         TEST_VALUE INTEGER NOT NULL,
         CONSTRAINT TEST_VALUE_PK PRIMARY KEY (TEST_VALUE)         
      )
      
      CREATE TABLE POOL_TEST2
      (
         TEST_VALUE INTEGER NOT NULL,
         CONSTRAINT TEST_VALUE_PK PRIMARY KEY (TEST_VALUE)         
      )

      CREATE TABLE QUERY_TEST
      (
         VALUE_1 INTEGER NOT NULL,
         VALUE_2 INTEGER NOT NULL
      )

      CREATE table QUERY_TEST_EXCEPT (
         VALUE_1 INTEGER NOT NULL
      )

      CREATE TABLE UNIQUE_COLUMN_TEST 
      (
         TEST_ID INTEGER,
         CONSTRAINT TEST_ID_UQ UNIQUE (TEST_ID)
      )

      CREATE TABLE NULL_COLUMN_TEST (
         NAME VARCHAR(50)
      )

      CREATE PROCEDURE INSERT_GENERATEDKEY_TEST
         @IN_VALUE VARCHAR(50),
         @OUT_KEY INTEGER OUTPUT
      AS
         INSERT INTO GENERATEDKEY_TEST(TEST_VALUE) VALUES (@IN_VALUE)
         SET @OUT_KEY = @@IDENTITY 
      RETURN @OUT_KEY
   
      CREATE PROCEDURE INSERT_ROW_COUNT_TEST
         @IN_VALUE VARCHAR(50),
         @OUT_ROWS INTEGER OUTPUT
      AS
         INSERT INTO GENERATEDKEY_TEST(TEST_VALUE) VALUES (@IN_VALUE)
         SET @OUT_ROWS = @@ROWCOUNT  
      RETURN @OUT_ROWS
   
      CREATE PROCEDURE UPDATE_TRANSACTION_TEST_VALUE
         @IN_OLD_TEST_VALUE VARCHAR(50),
         @IN_NEW_TEST_VALUE VARCHAR(50)
      AS
         UPDATE TRANSACTION_TEST SET
                TEST_VALUE = @IN_NEW_TEST_VALUE 
                WHERE TEST_VALUE = @IN_OLD_TEST_VALUE;
      RETURN @@IDENTITY

   */
   
   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(MSSQLDatabaseTestSchema.class);

   // Constructors /////////////////////////////////////////////////////////////

   /**
    * Default constructor.
    * 
    * @throws OSSException - error occurred.
    */
   public MSSQLDatabaseTestSchema(
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
      // MS SQL Application uses own specific data types 
      // super.create(cntDBConnection, strUserName);

      // Now try to create any database specific tables      
      Statement stmQuery = null;
      try
      {        
         stmQuery = cntDBConnection.createStatement();

         ///////////////////////////////////////////////////////////////////////

         if (stmQuery.execute("CREATE TABLE GENERATEDKEY_TEST" + NL +
                              "(" + NL +
                              "   TEST_KEY INTEGER IDENTITY(1,1) NOT NULL PRIMARY KEY," + NL + 
                              "   TEST_VALUE VARCHAR(50) NOT NULL" + NL +
                              ")"))
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
                              ")"))
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
                              "   DATE_TEST SMALLDATETIME NOT NULL" + NL + 
                              ")"))
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
                              "   TIME_TEST SMALLDATETIME NOT NULL" + NL + 
                              ")"))
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
                              "   TIMESTAMP_TEST DATETIME NOT NULL" + NL + 
                              ")"))
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
                              "   TEST_ID INTEGER NOT NULL," + NL +
                              "   TEST_VALUE VARCHAR(50) NOT NULL," + NL + 
                              "   CONSTRAINT TEST_ID_PK PRIMARY KEY (TEST_ID)" + NL +
                              ")"))
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
                              "   TEST_ID INTEGER NOT NULL," + NL +
                              "   TEST_VALUE VARCHAR(50)," + NL +
                              "   CONSTRAINT TEST_ID_FK FOREIGN KEY (TEST_ID)" + NL +
                              "   REFERENCES TRANSACTION_TEST (TEST_ID) ON DELETE CASCADE" + NL +
                              ")"))
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
                              "(" + 
                              "   TEST_ID INTEGER NOT NULL," + NL +
                              "   TEST_VALUE VARCHAR(50) NOT NULL," + NL +
                              "   CONSTRAINT DTEST_ID_PK PRIMARY KEY (TEST_ID)" + NL +
                              ")"))
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
                              "   CONSTRAINT DTEST_ID_FK FOREIGN KEY (TEST_ID)" + NL +
                              "   REFERENCES DELETE_TEST (TEST_ID) ON DELETE CASCADE" + NL +
                              ")"))
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
                              ")"))
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
                              "   CONSTRAINT OWN_FK_TEST_PK PRIMARY KEY (TEST_ID), " + NL +
                              "   CONSTRAINT OWN_FK_TEST_FK FOREIGN KEY (FK_ID) " +  NL +
                              "   REFERENCES OWN_FK_TEST (TEST_ID) ON DELETE NO ACTION" + NL +
                              ")"))
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
                              ")"))
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
                "   CONSTRAINT GROUP_CHILD_TEST_PK PRIMARY KEY (TEST_CHILD_ID)," + NL +
                "   CONSTRAINT TEST_BASE_ID_FK FOREIGN KEY (TEST_BASE_FK_ID) " + NL +
                "   REFERENCES GROUP_BASE_TEST (TEST_BASE_ID) ON DELETE CASCADE" + NL +
                ")"))
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
                              ")"))
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
                              ")"))
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
                              ")"))
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
                              ")"))
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
                              ")"))
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
                              ")"))
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
                              ")"))
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
                              ")"))
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

         ///////////////////////////////////////////////////////////////////////

         if (stmQuery.execute("CREATE PROCEDURE INSERT_GENERATEDKEY_TEST " + NL +
                  "@IN_VALUE VARCHAR(50), " + NL +
                  "@OUT_KEY INTEGER OUTPUT " + NL +
                  "AS " + NL +
                  "   INSERT INTO GENERATEDKEY_TEST(TEST_VALUE) VALUES (@IN_VALUE) " + NL +
                  "   SET @OUT_KEY = @@IDENTITY " + NL +
                  "   RETURN @OUT_KEY"))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         s_logger.log(Level.FINEST, "Procedure INSERT_GENERATEDKEY_TEST created.");
         /*
         if (stmQuery.execute("grant execute on INSERT_GENERATEDKEY_TEST to " + strUserName))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         Log.getLogger().log(Level.FINEST, 
                          "Access for procedure INSERT_GENERATEDKEY_TEST set for user " 
                          + strUserName);
         */                             

         ///////////////////////////////////////////////////////////////////////

         if (stmQuery.execute("CREATE PROCEDURE INSERT_ROW_COUNT_TEST " + NL +
                  "@IN_VALUE VARCHAR(50), " + NL +
                  "@OUT_ROWS INTEGER OUTPUT " + NL +
                  "AS " + NL +
                  "   INSERT INTO GENERATEDKEY_TEST(TEST_VALUE) VALUES (@IN_VALUE) " + NL +
                  "   SET @OUT_ROWS = @@ROWCOUNT " + NL +
                  "   RETURN @OUT_ROWS"))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         s_logger.log(Level.FINEST, "Procedure INSERT_ROW_COUNT_TEST created.");
         /*
         if (stmQuery.execute("grant execute on INSERT_ROW_COUNT_TEST to " + strUserName))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         Log.getLogger().log(Level.FINEST, 
                          "Access for procedure INSERT_ROW_COUNT_TEST set for user " 
                          + strUserName);
         */                             

         ///////////////////////////////////////////////////////////////////////

         if (stmQuery.execute("CREATE PROCEDURE UPDATE_TRANSACTION_TEST_VALUE " + NL +
                              "@IN_OLD_TEST_VALUE VARCHAR(50), " + NL +
                              "@IN_NEW_TEST_VALUE VARCHAR(50) " + NL +
                              "AS " + NL +
                              "   UPDATE " + strUserName + ".TRANSACTION_TEST SET " + NL +
                              "      TEST_VALUE = @IN_NEW_TEST_VALUE " + NL +
                              "      WHERE TEST_VALUE = @IN_OLD_TEST_VALUE " + NL +
                              "   RETURN @@IDENTITY"))

         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         s_logger.log(Level.FINEST, "Procedure UPDATE_TRANSACTION_TEST_VALUE created.");
         /*
         if (stmQuery.execute("grant all on UPDATE_TRANSACTION_TEST_VALUE to " + strUserName))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         Log.getLogger().log(Level.FINEST, 
                             "Access for procedure UPDATE_TRANSACTION_TEST_VALUE set for user " 
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
      return "INSERT INTO generatedkey_test(test_value) " +
              "VALUES (?)";
   }

   /**
    * {@inheritDoc}
    */
   public int[] executeInsertGeneratedKey2(
      Connection dbConnection,
      String     strValue
   ) throws SQLException
   {
      CallableStatement insertStatement = null;
      int               iInsertCount = 0;
      int               iGeneratedKey = 0;
      int[]             returnValues = null;
      
      try
      {
         insertStatement = dbConnection.prepareCall(
                              "EXEC INSERT_GENERATEDKEY_TEST ?, ?");
         insertStatement.setString(1, strValue);
         insertStatement.registerOutParameter(2, Types.INTEGER);

         iInsertCount = insertStatement.executeUpdate();
         iGeneratedKey = insertStatement.getInt(2);

         returnValues = new int[2];
         returnValues[0] = iInsertCount;
         returnValues[1] = iGeneratedKey;
      }
      finally
      {
         DatabaseUtils.closeStatement(insertStatement);
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
      CallableStatement updateStatement = null;
      int               iUpdateCount   = 0;

      try
      {
         updateStatement = dbConnection.prepareCall("EXEC UPDATE_TRANSACTION_TEST_VALUE ?, ?");
         updateStatement.setString(1, strOldValue);
         updateStatement.setString(2, strNewValue);
            
         // here is the bug in SAP DB, if there is called stored procedure without
         // output parameters, there is not returned number of updated records   
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
      CallableStatement insertStatement = null;
      int               iInsertCount = 0;
      int               iInsertCountReturnedFromSP = 0;
      int[]             returnValues = null;
      
      try
      {
         insertStatement = dbConnection.prepareCall(
                              "EXEC INSERT_ROW_COUNT_TEST ?, ?");
         insertStatement.setString(1, strValue);
         insertStatement.registerOutParameter(2, Types.INTEGER);

         iInsertCount = insertStatement.executeUpdate();
         iInsertCountReturnedFromSP = insertStatement.getInt(2);

         returnValues = new int[2];

         // value (number of affected rows) returned from insertStatement.executeUpdate(); 
         returnValues[0] = iInsertCount;

         // value (number of inserted rows) returned from stored procedure. 
         returnValues[1] = iInsertCountReturnedFromSP;
      }
      finally
      {
         DatabaseUtils.closeStatement(insertStatement);
      }
      
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
         String strDatabaseName = "";
         // There can be used various JDBC drivers: original MS driver, 
         // open source jTDS driver, BEA WebLogic drivers, DataDirect drivers, 
         // I-NET driver and J-NETDirect driver. URL is different for particular
         // drivers so we have to retrieve database name by diffrent ways.
         if (strDatabaseURL.indexOf(":jtds:") > 0)
         {
            // There is used open source jTDS driver. Parse DB name.
            strDatabaseName = strDatabaseURL.substring(
                                 strDatabaseURL.lastIndexOf("/") + 1, strDatabaseURL.length()); 
         }
         else if (strDatabaseURL.indexOf(":microsoft:") > 0)
         {
            // There is used original MS driver. Parse DB name.
            strDatabaseName = strDatabaseURL.substring(
                                 strDatabaseURL.lastIndexOf("DatabaseName=") + 
                                 "DatabaseName=".length(), strDatabaseURL.indexOf(";", 
                                 strDatabaseURL.lastIndexOf("DatabaseName=")));
         }
         else if (strDatabaseURL.indexOf(":bea:") > 0 
                  || strDatabaseURL.indexOf(":datadirect:") > 0
                  || strDatabaseURL.indexOf(":JSQLConnect:") > 0)
         {
            // There is used BEA WebLogic, DataDirect or J-NETDirect driver. Parse DB name.
            strDatabaseName = strDatabaseURL.substring(
                                 strDatabaseURL.lastIndexOf("atabaseName=") + 
                                 "atabaseName=".length(), strDatabaseURL.length());
         }
         else if (strDatabaseURL.indexOf(":inetdae7:") > 0)
         {
            // There is used I-NET driver. Parse DB name.
            strDatabaseName = strDatabaseURL.substring(
                                 strDatabaseURL.lastIndexOf("database=") + 
                                 "database=".length(), strDatabaseURL.length());
         }
         else
         {
            throw new SQLException("Unsupported URL: " + strDatabaseURL);
         }

         StringBuffer buffer = new StringBuffer();
         buffer.append("USE ");
         buffer.append(strDatabaseName);
         buffer.append("; EXEC sp_addlogin '");
         buffer.append(strUserName);
         buffer.append("', '");
         buffer.append(strUserPassword);
         buffer.append("', '");
         buffer.append(strDatabaseName);
         buffer.append("', 'us_english'");
         buffer.append("; EXEC sp_adduser '");
         buffer.append(strUserName);
         buffer.append("' ; GRANT ALL TO ");
         buffer.append(strUserName);

         stmQuery = cntAdminDBConnection.createStatement();

         if (stmQuery.execute(buffer.toString()))
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
         String strDatabaseName = "";
         // There can be used 2 various JDBC drivers: original MS driver and 
         // open source jTDS driver. URL is different for both drivers so we 
         // have to retrieve database name by 2 diffrent ways
         if (strDatabaseURL.indexOf(":jtds:") > 0)
         {
            // There is used open source jTDS driver. Parse DB name.
            strDatabaseName = strDatabaseURL.substring(
                                 strDatabaseURL.lastIndexOf("/") + 1, strDatabaseURL.length()); 
         }
         else if (strDatabaseURL.indexOf(":microsoft:") > 0)
         {
            // There is used original MS driver. Parse DB name.
            strDatabaseName = strDatabaseURL.substring(
                                 strDatabaseURL.lastIndexOf("DatabaseName=") + 
                                 "DatabaseName=".length(), strDatabaseURL.indexOf(";", 
                                 strDatabaseURL.lastIndexOf("DatabaseName=")));
         }
         else
         {
            throw new SQLException("Unsupported URL: " + strDatabaseURL);
         }

         StringBuffer buffer = new StringBuffer();
         buffer.append("USE ");
         buffer.append(strDatabaseName);
         buffer.append("; EXEC sp_revokedbaccess '");
         buffer.append(strUserName);
         buffer.append("'; EXEC sp_droplogin '");
         buffer.append(strUserName);
         buffer.append("'");

         stmQuery = cntAdminDBConnection.createStatement();

         if (stmQuery.execute(buffer.toString()))
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
