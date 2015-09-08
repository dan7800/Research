/*
 * Copyright (c) 2003 - 2008 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: DatabaseTestSchema.java,v 1.26 2009/04/22 06:17:44 bastafidli Exp $
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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opensubsystems.core.error.OSSDatabaseAccessException;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.persist.db.DatabaseConnectionFactory;
import org.opensubsystems.core.persist.db.impl.DatabaseImpl;
import org.opensubsystems.core.persist.db.impl.ModifiableDatabaseSchemaImpl;
import org.opensubsystems.core.util.Log;
import org.opensubsystems.core.util.jdbc.DatabaseUtils;

/**
 * This class encapsulates details about creation and upgrade
 * of database schema required to test database driver functionality.
 *
 * This database schema already contains some database specific tables and
 * thats why it was declared abstract.
 *
 * @version $Id: DatabaseTestSchema.java,v 1.26 2009/04/22 06:17:44 bastafidli Exp $
 * @author Miro Halas
 * @code.reviewer Miro Halas
 * @code.reviewed Initial revision
 */
public abstract class DatabaseTestSchema extends ModifiableDatabaseSchemaImpl
{   
   /*
      CREATE TABLE RESULTSET_TEST
      (
         RESULTSET_TEST VARCHAR(20) NOT NULL
      )
      
      CREATE TABLE DATE_TEST 
      (
         DATE_TEST DATE NOT NULL
      )
      
      CREATE TABLE TIME_TEST 
      (
         TIME_TEST TIME NOT NULL
      )
      
      CREATE TABLE TIMESTAMP_TEST 
      (
         TIMESTAMP_TEST TIMESTAMP NOT NULL
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

      CREATE TABLE ROLLBACK_TEST
      (
         TEST_COLUMN VARCHAR(50) NOT NULL,
         CONSTRAINT TEST_COLUMN_UQ UNIQUE (TEST_COLUMN)
      )

      CREATE TABLE OWN_FK_TEST
      (
         TEST_ID INTEGER NOT NULL,
         FK_ID INTEGER,
         CONSTRAINT OWN_FK_TEST_PK PRIMARY KEY (TEST_ID),
         CONSTRAINT OWN_FK_TEST_FK FOREIGN KEY (FK_ID)
         REFERENCES OWN_FK_TEST (TEST_ID) ON DELETE CASCADE
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
      
      CREATE TABLE UNIQUE_COLUMN_TEST (
         TEST_ID INTEGER,
         CONSTRAINT TEST_ID_UQ UNIQUE (TEST_ID)
      )

      CREATE TABLE NULL_COLUMN_TEST (
         NAME VARCHAR(50)
      )

    */
   
   // Constants ////////////////////////////////////////////////////////////////
   
   /**
    * Name identifies this schema in the database. 
    */
   public static final String DBTEST_SCHEMA_NAME = "DBTEST";
   
   /**
    * Version of this schema in the database.
    */
   public static final int DBTEST_SCHEMA_VERSION = 1;

   /**
    * List of table names belonging to this schema.
    */
   public static final Map TABLE_NAMES;

   // Cached variables /////////////////////////////////////////////////////////
   
   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(DatabaseTestSchema.class);

   // Constructors /////////////////////////////////////////////////////////////

   /**
    * Static initializer
    */
   static
   {
      // Create map that stores table names. The key is object data type
      // and value is table name
      TABLE_NAMES = new HashMap();
      TABLE_NAMES.put(new Integer(1), "RESULTSET_TEST");
      TABLE_NAMES.put(new Integer(2), "DATE_TEST");
      TABLE_NAMES.put(new Integer(3), "TIME_TEST");
      TABLE_NAMES.put(new Integer(4), "TIMESTAMP_TEST");
      TABLE_NAMES.put(new Integer(5), "TRANSACTION_TEST");
      TABLE_NAMES.put(new Integer(6), "TRANSACTION_RELATED_TEST");
      TABLE_NAMES.put(new Integer(7), "DELETE_TEST");
      TABLE_NAMES.put(new Integer(8), "DELETE_RELATED_TEST");
      TABLE_NAMES.put(new Integer(9), "OWN_FK_TEST");
      TABLE_NAMES.put(new Integer(10), "GROUP_BASE_TEST");
      TABLE_NAMES.put(new Integer(11), "GROUP_CHILD_TEST");
      TABLE_NAMES.put(new Integer(12), "SAME_TEST1");
      TABLE_NAMES.put(new Integer(13), "SAME_TEST2");
      TABLE_NAMES.put(new Integer(14), "POOL_TEST");
      TABLE_NAMES.put(new Integer(15), "POOL_TEST2");
      TABLE_NAMES.put(new Integer(16), "QUERY_TEST");
      TABLE_NAMES.put(new Integer(17), "QUERY_TEST_EXCEPT");
      TABLE_NAMES.put(new Integer(18), "UNIQUE_COLUMN_TEST");
      TABLE_NAMES.put(new Integer(19), "NULL_COLUMN_TEST");
      TABLE_NAMES.put(new Integer(20), "ROLLBACK_TEST");
   }

   /**
    * Default constructor.
    * 
    * @throws OSSException - error occurred.
    */
   public DatabaseTestSchema(
   ) throws OSSException
   {
      super(null, DBTEST_SCHEMA_NAME, DBTEST_SCHEMA_VERSION, true, TABLE_NAMES);
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
      Statement stmQuery = null;
      try
      {        
         stmQuery = cntDBConnection.createStatement();
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
                              "   DATE_TEST DATE NOT NULL" + NL + 
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
                              "   TIME_TEST TIME NOT NULL" + NL + 
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
                              "   TIMESTAMP_TEST TIMESTAMP NOT NULL" + NL + 
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
                              "   REFERENCES OWN_FK_TEST (TEST_ID) ON DELETE CASCADE" + NL +
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
    * Returns insert-select query
    * 
    *  @return String - SQL query for insert-select .
    */
   public String getInsertSelectQuery()
   {
      return "insert into QUERY_TEST (VALUE_1, VALUE_2) " +
             "select ?, VALUE_2 from QUERY_TEST where VALUE_2=1";
   }

   /**
    * Returns select except query
    * 
    *  @return String - SQL query for select-except.
    */
   public String getSelectExceptQuery()
   {
      return "select value_1 from query_test_except where value_1 in (?, ?, ?) " +
             "except select value_1 from query_test_except where value_1 in (?, ?) " +
             "except select value_1 from query_test_except where value_1 in (?, ?)";
   }

   /**
    * Returns select-except-union query
    * 
    *  @return String - SQL query for select-except-union.
    */
   public String getSelectExceptUnionQuery()
   {
      return "select value_1 from query_test_except where value_1 in (?, ?, ?) " +
             "except select value_1 from query_test_except where value_1 in (?, ?) " +
             "union select value_1 from query_test_except where value_1 in (?, ?)";
   }

   /**
    * Returns select-exist query
    * 
    *  @return String - SQL query for select-exist.
    */
   public String getSelectExistQuery()
   {
      return "select VALUE_1 from QUERY_TEST AS QT where exists " +
             "(select 1 from QUERY_TEST AS QT1 where QT1.VALUE_2 = QT.VALUE_1)";
   }

   /**
    * Query to insert to table with generated keys is database specific.
    * 
    * @return String - SQL query to insert to table with generated keys.
    */
   public abstract String getInsertGeneratedKey(
   );

  /**
   * Execute another query to insert to table with generated keys 
   * which guarantees return 
   * 
   * @param  dbConnection - database connection
   * @param  strValue - value to insert
   * @return int[] - index 0 - how many records were inserted
   *               - index 1 - the value of generated key
   * @throws SQLException - error during insert execute
   */
  public abstract int[] executeInsertGeneratedKey2(
     Connection dbConnection,
     String     strValue
  ) throws SQLException;

  /**
   * Execute another query to insert to table. There will be test if calling of 
   * stored procedure will return number of inserted rows.
   * 
   * @param  dbConnection - database connection
   * @param  strValue - value to insert
   * @return int[] - index 0 - how many records were inserted, Use -1 if the 
   *                           driver doesn't support returning number of 
   *                           affected rows
   *               - index 1 - the value of inserted records retrieved from 
   *                           stored procedure, Use -1 if stored procedure is
   *                           not supported
   * @throws SQLException - error during insert execute
   */
  public abstract int[] executeInsertRow(
     Connection dbConnection,
     String     strValue
  ) throws SQLException;

  /**
   * Execute query to update test value using stored procedure without parameters
   * 
   * @param  dbConnection - database connection
   * @param  strOldValue - old value to be updated
   * @param  strNewValue - new value for update
   * @return int - number of updated records
   * @throws SQLException - error during insert execute 
   */
  public abstract int executeUpdateTestValue(
     Connection dbConnection,
     String     strOldValue,
     String     strNewValue
  ) throws SQLException;

  /**
   * Execute insert that will try to insert the same value twice and therefore
   * generate SQLException due to violation of unique constraint.
   * 
   * @param dbConnection - database connection
   * @param strQuery - query user to insert original and duplicate value
   * @return boolean - true if the database allowed to insert duplicate value
   *                   false otherwise
   * @throws SQLException - an error has occurred 
   */
  public boolean executeDuplicateInsert(
     Connection dbConnection,
     String     strQuery
  ) throws SQLException
  {
     boolean           bReturn = false;
     PreparedStatement insertStatement = null;
     int               iCounter;
     
     try
     {
        // Try to insert 5 records
        for (iCounter = 1; iCounter < 6; iCounter++)
        {
           insertStatement = dbConnection.prepareStatement(strQuery);
           insertStatement.setInt(1, 100 * iCounter);
   
           insertStatement.executeUpdate();
        }
        
        // Insert duplicate value into unique column
        try
        {
           insertStatement.setInt(1, 100);
           insertStatement.executeUpdate();
           bReturn = true;
        }
        catch (SQLException sqlExc)
        {
           // We are expecting exception here due to duplicate so just ignore it
        }
     }
     finally
     {   
        DatabaseUtils.closeStatement(insertStatement);
     }
     
     return bReturn;
  }

  /**
   * Create datasource with specified parameters.
   * 
   * @param  dbConnectionFactory - database connection factory
   * @param  strDataSourceName - name of the data source
   * @param  strDatabaseDriver - database driver
   * @param  strDatabaseURL - database URL
   * @param  strUserName - name of the user that will be created
   * @param  strUserPassword - user password
   * @param  iTransactionIsolation - transaction isolation to use for connections
   * @throws OSSDatabaseAccessException - error during data source creation
   */
  public void createDataSource(
     DatabaseConnectionFactory dbConnectionFactory,
     String                    strDataSourceName,
     String                    strDatabaseDriver,
     String                    strDatabaseURL,
     String                    strUserName,
     String                    strUserPassword,
     int                       iTransactionIsolation
  ) throws OSSException
  {
      dbConnectionFactory.addDataSource(strDataSourceName,
                                        DatabaseImpl.getInstance(),
                                        strDatabaseDriver,
                                        strDatabaseURL,
                                        strUserName,
                                        strUserPassword,
                                        iTransactionIsolation);
  }

  /**
   * Create test user in the specified database.
   * 
   * @param  cntAdminDBConnection - database connection
   * @param  strDatabaseURL - database URL
   * @param  strUserName - name of the user that will be created
   * @param  strUserPassword - user password
   * @throws SQLException - error during user creation 
   */
  public abstract void createTestUser(
     Connection cntAdminDBConnection,
     String     strDatabaseURL,
     String     strUserName,
     String     strUserPassword
  ) throws SQLException;

  /**
   * Drop test user in the specified database.
   * 
   * @param  cntAdminDBConnection - database connection
   * @param  strDatabaseURL - database URL
   * @param  strUserName - name of the user that will be created
   * @throws SQLException - error during user deletion
   */
  public abstract void dropTestUser(
     Connection cntAdminDBConnection,
     String     strDatabaseURL,
     String     strUserName
  ) throws SQLException;
}
