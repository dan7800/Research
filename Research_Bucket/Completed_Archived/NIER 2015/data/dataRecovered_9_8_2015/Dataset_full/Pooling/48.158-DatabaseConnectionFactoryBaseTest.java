/*
 * Copyright (c) 2003 - 2008 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: DatabaseConnectionFactoryBaseTest.java,v 1.14 2009/04/22 06:17:43 bastafidli Exp $
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

package org.opensubsystems.core.persist.db.connectionpool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opensubsystems.core.error.OSSDatabaseAccessException;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.persist.db.Database;
import org.opensubsystems.core.persist.db.DatabaseConnectionFactory;
import org.opensubsystems.core.persist.db.DatabaseSchemaManager;
import org.opensubsystems.core.persist.db.DatabaseSourceDefinition;
import org.opensubsystems.core.persist.db.DatabaseTest;
import org.opensubsystems.core.persist.db.connectionpool.proxool.ProxoolDatabaseConnectionFactoryTest.ProxoolDatabaseConnectionFactoryTestInternal;
import org.opensubsystems.core.persist.db.db2.DB2DatabaseImpl;
import org.opensubsystems.core.persist.db.driver.DatabaseTestSchema;
import org.opensubsystems.core.persist.db.impl.DatabaseConnectionFactoryImpl;
import org.opensubsystems.core.persist.db.impl.DatabaseConnectionFactorySetupReader;
import org.opensubsystems.core.persist.db.impl.DatabaseImpl;
import org.opensubsystems.core.persist.db.impl.DatabaseTransactionFactoryImpl;
import org.opensubsystems.core.persist.db.mysql.MySQLDatabaseImpl;
import org.opensubsystems.core.util.Config;
import org.opensubsystems.core.util.Log;
import org.opensubsystems.core.util.PropertyUtils;
import org.opensubsystems.core.util.ThrowableUtils;
import org.opensubsystems.core.util.jdbc.DatabaseUtils;

/**
 * Base class containing tests for general concepts of  database connection 
 * factories, which every database connection factory shoudl support.
 * 
 * @version $Id: DatabaseConnectionFactoryBaseTest.java,v 1.14 2009/04/22 06:17:43 bastafidli Exp $
 * @author Julian Legeny
 * @code.reviewer Miro Halas
 * @code.reviewed 1.4 2005/09/01 05:22:03 bastafidli
 */
public abstract class DatabaseConnectionFactoryBaseTest extends DatabaseTest
{
   // Configuration settings ///////////////////////////////////////////////////
   
   /** 
    * Name of the property containing second user name to connect to the  
    * database during tests, 
    * e.g. "bastatest1"
    */   
   public static final String DATABASE_TESTUSER_PROPERTY_NAME 
                                 = "oss.databaseconnectionfactorytest.user";

   // Constants ////////////////////////////////////////////////////////////////

   /**
    * Name of the admin DataSource. 
    */
   public static final String DATASOURCE_NAME_ADMIN = "DATASOURCE_TEST_ADMIN";

   /**
    * Name of the DataSource 1. 
    */
   public static final String DATASOURCE_NAME_1 = "DATASOURCE_TEST_1";

   /**
    * Name of the DataSource 2 
    */
   public static final String DATASOURCE_NAME_2 = "DATASOURCE_TEST_2";

   /**
    * Default name for second database user used to run the tests. Make this  
    * name different from the name used by the real code so that tests execute  
    * in tables owned by different user.
    */
   public static final String DEFAULT_DB_TESTUSER2 = "testbasta2";

   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Database URL
    */
   private String m_strDatabaseURL;

   /**
    * User password
    */
   private String m_strPassword;

   /**
    * User password for datasources 1 and 2
    */
   private String m_strPasswordDS;

   /**
    * Default username from the data source
    */
   private String m_strUsername;

   /**
    * Default username for datasource 1
    */
   private String m_strUsernameDS1;

   /**
    * Default username for datasource 2
    */
   private String m_strUsernameDS2;

   /**
    * Database transaction isolation
    */
   private int m_iTransactionIsolation;

   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Database connection factory which can be used by test. This connection object
    * is instantiated before every test is started by the concrete derived class.
    */
   protected DatabaseConnectionFactory m_connectionFactory = null;

   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(DatabaseConnectionFactoryBaseTest.class);

   /**
    * User name of the second user used to connect to the database during tests.
    */
   protected String m_strDatabaseTestUser;
   
   // Constructors /////////////////////////////////////////////////////////////

   /**
    * Static initializer
    */
   static
   { 
      if (Config.getInstance().getRequestedConfigFile() == null)
      {
         Config.getInstance().setPropertyFileName(DatabaseTest.DEFAULT_PROPERTY_FILE);
      }

      // This test use special database schema so make the database aware of it
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
    * Create new DatabaseConnectionFactoryBaseTest.
    * 
    * @param strTestName - name of the test
    */
   public DatabaseConnectionFactoryBaseTest(
      String strTestName
   ) 
   {
      super(strTestName);

      // Initialize in constructor to avoid checkstyle warnings
      m_strDatabaseURL = "";
      m_strPassword = "";
      m_strPasswordDS = "";
      m_strUsername = "";
      m_strUsernameDS1 = "";
      m_strUsernameDS2 = "";
      m_strDatabaseTestUser = "";
   }

   /**
    * Set up environment for the test case.
    * 
    * @throws Exception - an error has occurred during setting up test
    */
   protected void setUp(
   ) throws Exception
   {
      String                               strTestName;
      DatabaseConnectionFactory            defaultFactory;
      DatabaseSourceDefinition             defaultSource;       
      Properties                           prpSettings;
      DatabaseConnectionFactorySetupReader setupReader;
      String                               strAdminUser;
      String                               strAdminPassword;
      
      strTestName = getName();
      defaultFactory = DatabaseConnectionFactoryImpl.getInstance();
      defaultSource = defaultFactory.getDefaultDataSource();
      prpSettings = Config.getInstance().getProperties();
      
      // Check the default datasource to see if we should be using 
      // administrative datasource instead
      setupReader = new DatabaseConnectionFactorySetupReader(
                           defaultSource.getName(),
                           defaultSource.getDatabase().getDatabaseTypeIdentifier());
      strAdminUser = setupReader.getStringParameterValue(
               DatabaseConnectionFactorySetupReader.DATABASE_ADMIN_USER);
      strAdminPassword = setupReader.getStringParameterValue(
               DatabaseConnectionFactorySetupReader.DATABASE_ADMIN_PASSWORD);

      m_strDatabaseTestUser = PropertyUtils.getStringProperty(
                                  prpSettings, DATABASE_TESTUSER_PROPERTY_NAME,
                                  DEFAULT_DB_TESTUSER2, 
                                  "User name of second test database user");
      m_strDatabaseURL = defaultSource.getUrl();
      
      // For testing 'Request Connection By User And Password' we add data 
      // sources 1 and 2 with another user/password as will be used for 
      // requesting connection in the test.
      if (strTestName.equals(
             "testRequestConnectionFromTwoDataSourcesByUserAndPassword"))
      {
         m_strUsernameDS1 = strAdminUser;
         m_strUsernameDS2 = m_strUsernameDS1; 
         m_strPasswordDS = strAdminPassword;
      }
      else
      {
         m_strUsernameDS1 = defaultSource.getUser();
         m_strUsernameDS2 = m_strDatabaseTestUser;
         m_strPasswordDS = defaultSource.getPassword();
      }

      m_strPassword = defaultSource.getPassword();
      m_strUsername = defaultSource.getUser();
      m_iTransactionIsolation = defaultSource.getTransactionIsolation();

      // add data sources
      if (!strTestName.equals("testCreatingDefaultDataSource"))
      {
         m_connectionFactory.addDataSource(DATASOURCE_NAME_1,
                                           DatabaseImpl.getInstance(),
                                           defaultSource.getDriver(),
                                           m_strDatabaseURL,
                                           m_strUsernameDS1,
                                           m_strPasswordDS,
                                           m_iTransactionIsolation);
      }

      if (strTestName.equals("testRequestConnectionFromTwoDataSources")
          || strTestName.equals("testConnectionCountForTwoDataSources")
          || strTestName.equals("testRequestConnectionCountByDataSource")
          || strTestName.equals(
                "testRequestConnectionCountByDataSourceWithUserAndPasswd")
          || strTestName.equals(
                "testRequestConnectionCountForSpecifiedDataSource")
          || strTestName.equals(
                "testRequestConnectionFromTwoDataSourcesByUserAndPassword")
          || strTestName.equals("testRequestConnectionByUserAndPassword"))
      {
         Connection cAdminConnection = null;
         
         // add data source for admin user
         ((DatabaseTestSchema) DatabaseSchemaManager.getInstance(
            DatabaseTestSchema.class)).createDataSource(
               m_connectionFactory, DATASOURCE_NAME_ADMIN,
               defaultSource.getDriver(), m_strDatabaseURL,
               strAdminUser, strAdminPassword, m_iTransactionIsolation);
         
         m_connectionFactory.setDefaultDataSource(DATASOURCE_NAME_ADMIN);

         try
         {
            // Request autocommit false since we are modifying database
            cAdminConnection = m_connectionFactory.requestConnection(false);
   
            // create user for this specific test
            ((DatabaseTestSchema) DatabaseSchemaManager.getInstance(
                DatabaseTestSchema.class)).createTestUser(cAdminConnection,
                                                          m_strDatabaseURL,
                                                          m_strDatabaseTestUser,
                                                          m_strPassword);

            DatabaseTransactionFactoryImpl.getInstance().commitTransaction(
                                                            cAdminConnection);
         }
         catch (Throwable thr)
         {
            DatabaseTransactionFactoryImpl.getInstance().rollbackTransaction(
                                                            cAdminConnection);
            throw new Exception(thr);
         }
         finally
         {
            m_connectionFactory.returnConnection(cAdminConnection);
         }
         // add data source 2
         ((DatabaseTestSchema) DatabaseSchemaManager.getInstance(
            DatabaseTestSchema.class)).createDataSource(
               m_connectionFactory, DATASOURCE_NAME_2,
               defaultSource.getDriver(), m_strDatabaseURL,
               m_strUsernameDS2, m_strPasswordDS, m_iTransactionIsolation);
      }

      if (!strTestName.equals("testCreatingDefaultDataSource"))
      {
         m_connectionFactory.setDefaultDataSource(DATASOURCE_NAME_1);
      }

      super.setUp();
   }

   /**
    * Restore original environment after the test case.
    * 
    * @throws Exception - an error has occurred during tearing down up test
    */
   protected void tearDown() throws Exception
   {
      String strTestName = getName();
      
      if (strTestName.equals("testRequestConnectionFromTwoDataSources")
          || strTestName.equals("testConnectionCountForTwoDataSources")
          || strTestName.equals("testRequestConnectionCountByDataSource")
          || strTestName.equals(
                "testRequestConnectionCountByDataSourceWithUserAndPasswd")
          || strTestName.equals(
                "testRequestConnectionCountForSpecifiedDataSource")
          || strTestName.equals(
                "testRequestConnectionFromTwoDataSourcesByUserAndPassword")
          || strTestName.equals("testRequestConnectionByUserAndPassword"))
      {
         // set admin datasource and drop test user
         Connection cAdminConnection = null;

         try
         {
            // We need to set correct default data source to get admin 
            // connection to the same data source where we have made changes
            // in the setup
            m_connectionFactory.setDefaultDataSource(DATASOURCE_NAME_ADMIN);
            
            // There has to be here autocommit = true, because there is problem 
            // to execute some stored procedure while in transaction
            cAdminConnection = m_connectionFactory.requestAdminConnection(true);
            // drop user for this specific test
            ((DatabaseTestSchema) DatabaseSchemaManager.getInstance(
                DatabaseTestSchema.class)).dropTestUser(cAdminConnection,
                                                        m_strDatabaseURL,
                                                        m_strDatabaseTestUser);
         }
         finally
         {
            m_connectionFactory.returnConnection(cAdminConnection);
         }
      }

      m_connectionFactory.stop();

      super.tearDown();
   }

   // Tests ////////////////////////////////////////////////////////////////////

   /**
    * Test for request connection will create default data source if there 
    * wasn't specified.
    * 
    * @throws Exception - an error has occurred during test
    */
   public void testCreatingDefaultDataSource(
   ) throws Exception
   {
      Connection con = null;
      String strDefaultDS = "";

      try
      {
         strDefaultDS = m_connectionFactory.getDefaultDataSourceName();

         assertEquals("There should be not specified default data source yet.",
                      "", strDefaultDS);

         // Request autocommit true since we are just reading data from the database
         con = m_connectionFactory.requestConnection(true);

         strDefaultDS = m_connectionFactory.getDefaultDataSourceName();

         assertTrue("There should be already specified default data source.",
                    strDefaultDS.length() > 0);
      }
      catch (IllegalArgumentException iaeExc)
      {
         s_logger.log(Level.WARNING, 
                      "Proxool doesn't support creation of multiple connection pools" +
                      " with the same name, see bug 1275551",
                      iaeExc);
         fail("Proxool doesn't support creation of multiple connection pools" +
              " with the same name, see bug 1275551");
      }
      finally
      {
         m_connectionFactory.returnConnection(con);
      }
   }

   /**
    * Test for request connection - if there is connection requested, request 
    * counter has to increase.
    * 
    * @throws Exception - an error has occurred during test
    */
   public void testRequestConnectionCount(
   ) throws Exception
   {
      Connection con = null;
      int testReqConCount = -1;

      try
      {
         // Request autocommit true since we are just reading data from the database
         con = m_connectionFactory.requestConnection(true);
         assertTrue("Autocommit should be true for pooled connections.", con.getAutoCommit());
         
         // get number of connections
         testReqConCount = m_connectionFactory.getTotalRequestedConnectionCount();
         
         assertEquals("Incorrect number of requested connections", 1, testReqConCount);
      }
      finally
      {
         m_connectionFactory.returnConnection(con);
      }
   }

   /**
    * Test for connection count that is specified by data source
    * 
    * @throws Exception - an error has occurred during test
    */
   public void testRequestConnectionCountForSpecifiedDataSource(
   ) throws Exception
   {
      Connection con1 = null;
      Connection con2 = null;
      int testReqConCount = -1;
      int testTotalReqConCount = -1;

      try
      {
         // Check if there is set default data source 1.
         assertEquals("Incorrect default data source is specified",
                      DATASOURCE_NAME_1, m_connectionFactory.getDefaultDataSourceName());

         // Check for total connection count
         testTotalReqConCount = m_connectionFactory.getTotalRequestedConnectionCount();
         assertEquals("Incorrect initial total connection count.",
                      0, testTotalReqConCount);

         // Check for connection count for data source 1
         testReqConCount = m_connectionFactory.getRequestedConnectionCount(DATASOURCE_NAME_1);
         assertEquals("Incorrect initial connection count for data source 1.",
                      0, testReqConCount);

         // Check for connection count for data source 2
         testReqConCount = m_connectionFactory.getRequestedConnectionCount(DATASOURCE_NAME_2);
         assertEquals("Incorrect initial connection count for data source 2.",
                      0, testReqConCount);

         // Request autocommit true since we are just reading data from the database
         con1 = m_connectionFactory.requestConnection(true);
         
         // Check if there is still default data source 1.
         assertEquals("There should be still datasource 1 specified as default",
                      DATASOURCE_NAME_1, m_connectionFactory.getDefaultDataSourceName());

         // Check for total connection count
         testTotalReqConCount = m_connectionFactory.getTotalRequestedConnectionCount();
         assertEquals("Incorrect total connection count after requesting connection " +
                      "from data source 2.", 1, testTotalReqConCount);

         // Check for connection count of data source 1
         testReqConCount = m_connectionFactory.getRequestedConnectionCount(DATASOURCE_NAME_1);
         assertEquals("Incorrect connection count for data source 1.",
                      1, testReqConCount);

         // Check for connection count of data source 2
         testReqConCount = m_connectionFactory.getRequestedConnectionCount(DATASOURCE_NAME_2);
         assertEquals("Incorrect connection count for data source 2.",
                      0, testReqConCount);

         // Now set datasource 2 as default
         m_connectionFactory.setDefaultDataSource(DATASOURCE_NAME_2);

         // Request connection from data source 2
         // Request autocommit true since we are just reading data from the database
         con2 = m_connectionFactory.requestConnection(true);

         // Check if there is still default data source 2.
         assertEquals("There should be still datasource 2 specified as default",
                      DATASOURCE_NAME_2, m_connectionFactory.getDefaultDataSourceName());

         // Check for total connection count
         testTotalReqConCount = m_connectionFactory.getTotalRequestedConnectionCount();
         assertEquals("Incorrect total connection count after requesting connection " +
                      "from data source 1.", 2, testTotalReqConCount);

         // Check for connection count of data source 1
         testReqConCount = m_connectionFactory.getRequestedConnectionCount(DATASOURCE_NAME_1);
         assertEquals("Incorrect connection count for data source 1.",
                      1, testReqConCount);
         
         // Check for connection count of data source 2
         testReqConCount = m_connectionFactory.getRequestedConnectionCount(DATASOURCE_NAME_2);
         assertEquals("Incorrect connection count for data source 2.",
                      1, testReqConCount);
      }
      finally
      {
         // return connections
         m_connectionFactory.returnConnection(con1);
         m_connectionFactory.returnConnection(con2);
      }

   }

   /**
    * Test for return connection - if there is connection returned, request 
    * counter has to decrease.
    * 
    * @throws Exception - an error has occurred during test
    */
   public void testReturnConnectionCount(
   ) throws Exception
   {
      Connection con = null;
      int testReqConCount = -1;

      try
      {
         // Request autocommit true since we are just reading data from the database
         con = m_connectionFactory.requestConnection(true);
      }
      finally
      {
         // return connection
         m_connectionFactory.returnConnection(con);
      }
      // get number of connections
      testReqConCount = m_connectionFactory.getTotalRequestedConnectionCount();

      assertEquals("Incorrect number of connections after returning connection", 
                   0, testReqConCount);
   }

   /**
    * Test for adding data source with the same name as already has been added.
    * 
    * @throws Exception - an error has occurred during test
    */
   public void testAddDataSourceWithTheSameName(
   ) throws Exception
   {
      try
      {
         DatabaseConnectionFactory defaultFactory;
         DatabaseSourceDefinition  defaultSource;
         
         defaultFactory = DatabaseConnectionFactoryImpl.getInstance();
         defaultSource = defaultFactory.getDefaultDataSource(); 
         
         // add data sources
         m_connectionFactory.addDataSource(DATASOURCE_NAME_1,
                                           DatabaseImpl.getInstance(),
                                           defaultSource.getDriver(),
                                           defaultSource.getUrl(),
                                           defaultSource.getUser(),
                                           defaultSource.getPassword(),
                                           defaultSource.getTransactionIsolation());

         fail("It should be not permitted to add DataSource with the same name.");
      }
      catch (IllegalArgumentException iaeExc)
      {
         // There should be thrown exception when DataSource with the same name 
         // already exists. This exception is expected.
         assertNotNull("The exception didn't contain message.",
                       iaeExc.getMessage());
         assertTrue("The exception message didn't contain expected datasource name",
                    iaeExc.getMessage().contains(DATASOURCE_NAME_1));
      }
   }

   /**
    * Test for 2 data sources. When there was requested connection from DS1, then set to DS2,
    * then return conection (for DS1 when active is DS2), connection count will be still 1 
    * instead of 0. 
    * 
    * @throws Exception - an error has occurred during test
    */
   public void testConnectionCountForTwoDataSources(
   ) throws Exception
   {
      Connection con1 = null;
      Connection con2 = null;
      int testConCountDS1 = -1;
      int testConCountDS2 = -1;

      try
      {
         // at this moment there are added 2 data sources
         
         // set to DS1, request connection from DS1 and get connection count for DS1
         m_connectionFactory.setDefaultDataSource(DATASOURCE_NAME_1);
         // Request autocommit true since we are just reading data from the database
         con1 = m_connectionFactory.requestConnection(true);
         testConCountDS1 = m_connectionFactory.getRequestedConnectionCount();
         
         assertEquals("Incorrect number of connections for DataSource 1", 1, testConCountDS1);

         // set to DS2 and get connection count for DS2
         m_connectionFactory.setDefaultDataSource(DATASOURCE_NAME_2);
         testConCountDS2 = m_connectionFactory.getRequestedConnectionCount();
         
         assertEquals("Incorrect number of connections for DataSource 2", 0, testConCountDS2);
         
         // return connection (it's still active DS2) and here should be returned connection for
         // DS1 because there was not yet requested connection for DS2
         m_connectionFactory.returnConnection(con1);
         // reset so that we don't return it again below
         con1 = null;
         
         // get connection count - it's still active DS2 and there should be 
         // returned connection count for DS2
         testConCountDS2 = m_connectionFactory.getRequestedConnectionCount();
         assertEquals("Incorrect number of connections for DataSource 2", 0, testConCountDS2);
         
         // set to DS1 and get connection count for DS1
         m_connectionFactory.setDefaultDataSource(DATASOURCE_NAME_1);
         testConCountDS1 = m_connectionFactory.getRequestedConnectionCount();

         assertEquals("Incorrect number of connections for DataSource 1", 0, testConCountDS1);
      }
      finally
      {
         // return con1 only if there was error above
         m_connectionFactory.returnConnection(con1);
         m_connectionFactory.returnConnection(con2);
      }
   }

   /**
    * Test for requesting connection implicitly without specifying user name and 
    * password. There are 2 data sources. It should be possible to switch them 
    * and retrieve connections from each one of them. Each data source has different 
    * user which creates different table and inserts some data to it and selects 
    * some data from it. These data should be different for both connections and 
    * it should not be possible to access the other table using incorrect connection.
    * 
    * @throws Exception - an error has occurred during test
    */
   public void testRequestConnectionFromTwoDataSources(
   ) throws Exception
   {
      Connection con1 = null;
      Connection con2 = null;
      int testConCountDS1 = -1;
      int testConCountDS2 = -1;
      int iInsertCount = 0;
      String testValueDS1 = "";
      String testValueDS2 = "";

      Statement         createStatement = null;
      PreparedStatement insertStatement = null;
      Statement         selectStatement = null;
      ResultSet         rsResults = null;


      final String CREATE_TABLE1 = "create table REQ_CONN_TEST1 (TEST_VALUE varchar(20))";
      final String DROP_TABLE1 = "drop table REQ_CONN_TEST1";
      final String INSERT_VALUE1 = "insert into REQ_CONN_TEST1 (TEST_VALUE) values (?)";
      final String SELECT_VALUE1 = "select * from REQ_CONN_TEST1";
      final String CREATE_TABLE2 = "create table REQ_CONN_TEST2 (TEST_VALUE varchar(20))";
      final String DROP_TABLE2 = "drop table REQ_CONN_TEST2";
      final String INSERT_VALUE2 = "insert into REQ_CONN_TEST2 (TEST_VALUE) values (?)";
      final String SELECT_VALUE2 = "select * from REQ_CONN_TEST2";
      final String VALUE_DS1 = "test_value_DS1";
      final String VALUE_DS2 = "test_value_DS2";

      try
      {
         // at this moment there are added 2 data sources
         
         // set to DS1, request connection from DS1 and get connection count for DS1
         m_connectionFactory.setDefaultDataSource(DATASOURCE_NAME_1);
         // Request autocommit false since we are modifying database
         con1 = m_connectionFactory.requestConnection(false);
         testConCountDS1 = m_connectionFactory.getRequestedConnectionCount();
         
         assertEquals("Incorrect number of connections for DataSource 1", 1, testConCountDS1);

         // ==========================================
         // 1. Create table for DS1 and insert record
         // ==========================================
         try
         {
            try
            {
                // create table for DS1
               createStatement = con1.createStatement();
               createStatement.execute(CREATE_TABLE1);
               
               // insert value to the table 
               insertStatement = con1.prepareStatement(INSERT_VALUE1);
               insertStatement.setString(1, VALUE_DS1);
               iInsertCount = insertStatement.executeUpdate();
               assertEquals("One record should have been inserted.", 
                                   1, iInsertCount);
            }
            finally
            {
               DatabaseUtils.closeStatement(createStatement);
               DatabaseUtils.closeStatement(insertStatement);
               createStatement = null;
               insertStatement = null;
            }
            // We are using 2 connections so we need to make commitTransaction for particular
            // connection. There will be better to have distributed connection manager.
            DatabaseTransactionFactoryImpl.getInstance().commitTransaction(con1);         
         }
         catch (Throwable thr)
         {
            // We are using 2 connections so we need to do rollbackTransaction for particular 
            // connection.There will be better to have distributed connection manager.
            DatabaseTransactionFactoryImpl.getInstance().rollbackTransaction(con1);
            throw new Exception(thr);
         }

         // set to DS2, request connection from DS2 and get connection count for DS2
         m_connectionFactory.setDefaultDataSource(DATASOURCE_NAME_2);
         // Request autocommit false since we are modifying database
         con2 = m_connectionFactory.requestConnection(false);
         testConCountDS2 = m_connectionFactory.getRequestedConnectionCount();
         
         assertEquals("Incorrect number of connections for DataSource 1", 1, testConCountDS2);

         // ==========================================
         // 1. Create table for DS2 and insert record
         // ==========================================
         try
         {
            try
            {
               // create table for DS1
               createStatement = con2.createStatement();
               createStatement.execute(CREATE_TABLE2);
               
               // insert value to the table 
               insertStatement = con2.prepareStatement(INSERT_VALUE2);
               insertStatement.setString(1, VALUE_DS2);
               iInsertCount = insertStatement.executeUpdate();
               assertEquals("One record should have been inserted.", 
                                   1, iInsertCount);
            }
            finally
            {
               DatabaseUtils.closeStatement(createStatement);
               DatabaseUtils.closeStatement(insertStatement);
               createStatement = null;
               insertStatement = null;            
            }
            // We are using 2 connections so we need to make commitTransaction for particular
            // connection. There will be better to have distributed connection manager.
            DatabaseTransactionFactoryImpl.getInstance().commitTransaction(con2);         
         }
         catch (Throwable thr)
         {
            // We are using 2 connections so we need to do rollbackTransaction for particular 
            // connection.There will be better to have distributed connection manager.
            DatabaseTransactionFactoryImpl.getInstance().rollbackTransaction(con2);
            throw new Exception(thr);
         }

         // Now try to select from DS2 using this connection
         try
         {
            selectStatement = con2.createStatement();
            rsResults = selectStatement.executeQuery(SELECT_VALUE2);
            assertTrue("There should be selected value for DS2", rsResults.next());
            testValueDS2 = rsResults.getString(1);
            assertEquals("Incorrect value for data source 2", VALUE_DS2, testValueDS2);

         }
         finally
         {
            DatabaseUtils.closeResultSetAndStatement(rsResults, selectStatement);
            rsResults = null;
            selectStatement = null;
         }

         // set to DS1
         m_connectionFactory.setDefaultDataSource(DATASOURCE_NAME_1);

         // Now try to select from DS1 using this connection
         try
         {
            selectStatement = con1.createStatement();
            rsResults = selectStatement.executeQuery(SELECT_VALUE1);
            assertTrue("There should be selected value for DS1", rsResults.next());
            testValueDS1 = rsResults.getString(1);
            assertEquals("Incorrect value for data source 1", VALUE_DS1, testValueDS1);
         }
         finally
         {
            DatabaseUtils.closeResultSetAndStatement(rsResults, selectStatement);
            rsResults = null;
            selectStatement = null;
         }

         // At this point we know there are inserted correct records in particular 
         // tables for particular data sources. Now try to retrieve data from TABLE 2
         // using data source 1 and retrieve data from TABLE 1 using data source 2.
         // Now try to select from DS1 using this connection
         try
         {
            selectStatement = con1.createStatement();
            rsResults = selectStatement.executeQuery(SELECT_VALUE2);
            if (rsResults.next())
            {
              testValueDS1 = rsResults.getString(1);  
            }
            
            if (DatabaseImpl.getInstance() instanceof MySQLDatabaseImpl)
            {
               // MySQL as of version 4.1.14 doesn't isolate user tables of 
               // different users from each other
               assertNotNull("MySQL user isolation behavior has changed."
                             + " Review it", rsResults);
            }
            else
            {
               assertNull("The database by default doesn't isolate tables" 
                          + " created by one user from tables created by other" 
                          + " user since it should not be possible to select"
                          + " value from Table 2 for DS1", rsResults);
            }
         }
         catch (SQLException sqlExc)
         {
            DatabaseTransactionFactoryImpl.getInstance().rollbackTransaction(con1);
            // This exception is expected, because there is not Table 2 for DS1 
            // (Table 2 belongs to DS2)
            assertNull("There should not be selected value from Table 2 for DS1", 
                       rsResults);
         }
         finally
         {
            DatabaseUtils.closeResultSetAndStatement(rsResults, selectStatement);
            rsResults = null;
            selectStatement = null;
         }
         
         // set to DS2
         m_connectionFactory.setDefaultDataSource(DATASOURCE_NAME_2);

         try
         {
            selectStatement = con2.createStatement();
            rsResults = selectStatement.executeQuery(SELECT_VALUE1);
            if (rsResults.next())
            {
              testValueDS2 = rsResults.getString(1);  
            }
            
            if (DatabaseImpl.getInstance() instanceof MySQLDatabaseImpl)
            {
               // MySQL as of version 4.1.14 doesn't isolate user tables of 
               // different users from each other
               assertNotNull("MySQL user isolation behavior has changed."
                             + " Review it", rsResults);
            }
            else
            {
               assertNull("The database by default doesn't isolate tables" 
                          + " created by one user from tables created by other" 
                          + " user since it should not be possible to select"
                          + " value from Table 1 for DS2", rsResults);
            }
         }
         catch (SQLException sqlExc)
         {
            DatabaseTransactionFactoryImpl.getInstance().rollbackTransaction(con2);

            // This exception is expected, because there is not Table 1 for DS2 
            // (Table 1 belongs to DS1)
            assertNull("The database by default doesn't isolate tables created by" +
                       " one user from tables created by other user since it should" +
                       " not be possible to select value from Table 1 for DS2", 
                       rsResults);
         }
         finally
         {
            DatabaseUtils.closeResultSetAndStatement(rsResults, selectStatement);
            rsResults = null;
            selectStatement = null;
         }
      }
      finally
      {
         Statement stmQuery = null;

         try
         {
            // If connection wasn't created due to some error, there is no
            // need to delete this table
            if (con1 != null)
            {
               // set to DS1
               m_connectionFactory.setDefaultDataSource(DATASOURCE_NAME_1);
            
               try
               {
                  stmQuery = con1.createStatement();
                  // drop table 1
                  if (stmQuery.execute(DROP_TABLE1))
                  {
                     // Close any results
                     stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
                  }
                  // We are using 2 connections so we need to make commitTransaction for particular
                  // connection. There will be better to have distributed connection manager.
                  DatabaseTransactionFactoryImpl.getInstance().commitTransaction(con1);         
               }
               finally
               {
                   DatabaseUtils.closeStatement(stmQuery);            
               }
            }
         }
         catch (Throwable thr)
         {
            // We are using 2 connections so we need to do rollbackTransaction for particular 
            // connection.There will be better to have distributed connection manager.
            DatabaseTransactionFactoryImpl.getInstance().rollbackTransaction(con1);
            throw new Exception(thr);
         }
         finally
         {
            try
            {
               // If connection wasn't created due to some error, there is no
               // need to delete this table
               if (con2 != null)
               {
                  // set to DS1
                  m_connectionFactory.setDefaultDataSource(DATASOURCE_NAME_2);
               
                  try
                  {
                     stmQuery = con2.createStatement();
                     // drop table 2
                     if (stmQuery.execute(DROP_TABLE2))
                     {
                        // Close any results
                        stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
                     }
                  }
                  finally
                  {
                      DatabaseUtils.closeStatement(stmQuery);            
                  }
                  // We are using 2 connections so we need to make commitTransaction for particular
                  // connection. There will be better to have distributed connection manager.
                  DatabaseTransactionFactoryImpl.getInstance().commitTransaction(con2);
               }
            }
            catch (Throwable thr)
            {
               // We are using 2 connections so we need to do rollbackTransaction for particular 
               // connection.There will be better to have distributed connection manager.
               DatabaseTransactionFactoryImpl.getInstance().rollbackTransaction(con2);
               throw new Exception(thr);
            }
            finally
            {
               m_connectionFactory.returnConnection(con1);
               m_connectionFactory.returnConnection(con2);
            }
         }
      }
   }

   /**
    * Test for requesting connection explictly by user and password. There are 
    * 2 data sources. It should be possible to switch them and retrieve connections 
    * from each one of them. Each data source has different user which creates
    * different table and inserts some data to it and selects some data from it. 
    * These data should be different for both connections and it should not be
    * possible to access the other table using incorrect connection.
    * 
    * @throws Exception - an error has occurred during test
    */
   public void testRequestConnectionFromTwoDataSourcesByUserAndPassword(
   ) throws Exception
   {
      Connection con1 = null;
      Connection con2 = null;
      int testConCountDS1 = -1;
      int testConCountDS2 = -1;
      int iInsertCount = 0;
      String testValueDS1 = "";
      String testValueDS2 = "";

      Statement         createStatement = null;
      PreparedStatement insertStatement = null;
      Statement         selectStatement = null;
      ResultSet         rsResults = null;


      final String CREATE_TABLE1 = "create table REQ_CONN_TEST1 (TEST_VALUE varchar(20))";
      final String DROP_TABLE1 = "drop table REQ_CONN_TEST1";
      final String INSERT_VALUE1 = "insert into REQ_CONN_TEST1 (TEST_VALUE) values (?)";
      final String SELECT_VALUE1 = "select * from REQ_CONN_TEST1";
      final String CREATE_TABLE2 = "create table REQ_CONN_TEST2 (TEST_VALUE varchar(20))";
      final String DROP_TABLE2 = "drop table REQ_CONN_TEST2";
      final String INSERT_VALUE2 = "insert into REQ_CONN_TEST2 (TEST_VALUE) values (?)";
      final String SELECT_VALUE2 = "select * from REQ_CONN_TEST2";
      final String VALUE_DS1 = "test_value_DS1";
      final String VALUE_DS2 = "test_value_DS2";

      // at this moment there are added 2 data sources
      
      // set to DS1, request connection from DS1 and get connection count for DS1
      m_connectionFactory.setDefaultDataSource(DATASOURCE_NAME_1);
      try
      {
         // Try to request connection using different user name only
         // Request autocommit true since we are just reading data from the database
         con1 = m_connectionFactory.requestConnection(true, 
                                       m_strUsername + "_diff", m_strPassword);
         fail("The connection should have not been allocated.");
      } 
      catch (OSSDatabaseAccessException daeExc)
      {
         // This exception is expected
         assertNull("There should be not requested connection for incorrect username", con1);
      }
      finally
      {
         // In case the connection was retrieved, return it, this will also 
         // work if it is null
         m_connectionFactory.returnConnection(con1);
         con1 = null;
      }

      if (!(DatabaseImpl.getInstance() instanceof DB2DatabaseImpl))
      {
         // Do not test this for DB2 because DB2 is using real OS users and 
         // depending on policy this may cause the user to be locked out (e.g.
         // for C3P0 database pool which implements automatic retry) causing all
         // subsequent tests and test runs to fail until the user is unlocked
         try
         {
            // Try to request connection using different password only
            // Request autocommit true since we are just reading data from the database
            con1 = m_connectionFactory.requestConnection(true, 
                                          m_strUsername, m_strPassword + "_diff");
            fail("The connection should have not been allocated.");
         } 
         catch (OSSDatabaseAccessException daeExc)
         {
            // This exception is expected
            assertNull("There should be not requested connection for incorrect password", con1);
         }
         finally
         {
            // In case the connection was retrieved, return it, this will also 
            // work if it is null
            m_connectionFactory.returnConnection(con1);
            con1 = null;
         }
      }
      
      try
      {
         // Try to request connection using different usename and password
         // Request autocommit true since we are just reading data from the database
         con1 = m_connectionFactory.requestConnection(true, 
                                                      m_strUsername + "_diff", 
                                                      m_strPassword + "_diff");
         fail("The connection should have not been allocated.");
      } 
      catch (OSSDatabaseAccessException daeExc)
      {
         // This exception is expected
         assertNull("There should be not requested connection for incorrect usernane " +
                    "and password", con1);
      }
      finally
      {
         // In case the connection was retrieved, return it, this will also 
         // work if it is null
         m_connectionFactory.returnConnection(con1);
         con1 = null;
      }

      try
      {
         // We have another user on the datasource ()there is used ADMIN for adding 
         // datasources 1 and 2. For requesting connections in this test we are using
         // another 2 users.
         // Request autocommit false since we are modifying database
         con1 = m_connectionFactory.requestConnection(false, m_strUsername, m_strPassword);
         testConCountDS1 = m_connectionFactory.getRequestedConnectionCount();
         
         assertEquals("Incorrect number of connections for DataSource 1", 1, testConCountDS1);

         // ==========================================
         // 1. Create table for DS1 and insert record
         // ==========================================
         try
         {
            try
            {
                // create table for DS1
               createStatement = con1.createStatement();
               createStatement.execute(CREATE_TABLE1);
               
               // insert value to the table 
               insertStatement = con1.prepareStatement(INSERT_VALUE1);
               insertStatement.setString(1, VALUE_DS1);
               iInsertCount = insertStatement.executeUpdate();
               assertEquals("One record should have been inserted.", 
                                   1, iInsertCount);
            }
            finally
            {
               DatabaseUtils.closeStatement(createStatement);
               DatabaseUtils.closeStatement(insertStatement);
               createStatement = null;
               insertStatement = null;
            }
            // We are using 2 connections so we need to make commitTransaction for particular
            // connection. There will be better to have distributed connection manager.
            DatabaseTransactionFactoryImpl.getInstance().commitTransaction(con1);         
         }
         catch (Throwable thr)
         {
            // We are using 2 connections so we need to do rollbackTransaction for particular 
            // connection.There will be better to have distributed connection manager.
            DatabaseTransactionFactoryImpl.getInstance().rollbackTransaction(con1);
            throw new Exception(thr);
         }

         // set to DS2, request connection from DS2 and get connection count for 
         // DS2
         m_connectionFactory.setDefaultDataSource(DATASOURCE_NAME_2);
         // request connection by username and password
         // Request autocommit false since we are modifying database
         con2 = m_connectionFactory.requestConnection(false, 
                                                      m_strDatabaseTestUser, 
                                                      m_strPassword);
         testConCountDS2 = m_connectionFactory.getRequestedConnectionCount();
         
         assertEquals("Incorrect number of connections for DataSource 1", 1, 
                      testConCountDS2);

         // ==========================================
         // 1. Create table for DS2 and insert record
         // ==========================================
         try
         {
            try
            {
               // create table for DS1
               createStatement = con2.createStatement();
               createStatement.execute(CREATE_TABLE2);
               
               // insert value to the table 
               insertStatement = con2.prepareStatement(INSERT_VALUE2);
               insertStatement.setString(1, VALUE_DS2);
               iInsertCount = insertStatement.executeUpdate();
               assertEquals("One record should have been inserted.", 
                                   1, iInsertCount);
            }
            finally
            {
               DatabaseUtils.closeStatement(createStatement);
               DatabaseUtils.closeStatement(insertStatement);
               createStatement = null;
               insertStatement = null;            
            }
            // We are using 2 connections so we need to make commitTransaction for particular
            // connection. There will be better to have distributed connection manager.
            DatabaseTransactionFactoryImpl.getInstance().commitTransaction(con2);         
         }
         catch (Throwable thr)
         {
            // We are using 2 connections so we need to do rollbackTransaction for particular 
            // connection.There will be better to have distributed connection manager.
            DatabaseTransactionFactoryImpl.getInstance().rollbackTransaction(con2);
            throw new Exception(thr);
         }

         // Now try to select from DS2 using this connection
         try
         {
            selectStatement = con2.createStatement();
            rsResults = selectStatement.executeQuery(SELECT_VALUE2);
            assertTrue("There should be selected value for DS2", rsResults.next());
            testValueDS2 = rsResults.getString(1);
            assertEquals("Incorrect value for data source 2", VALUE_DS2, testValueDS2);

         }
         finally
         {
            DatabaseUtils.closeResultSetAndStatement(rsResults, selectStatement);
            rsResults = null;
            selectStatement = null;
         }

         // set to DS1
         m_connectionFactory.setDefaultDataSource(DATASOURCE_NAME_1);

         // Now try to select from DS1 using this connection
         try
         {
            selectStatement = con1.createStatement();
            rsResults = selectStatement.executeQuery(SELECT_VALUE1);
            assertTrue("There should be selected value for DS1", rsResults.next());
            testValueDS1 = rsResults.getString(1);
            assertEquals("Incorrect value for data source 1", VALUE_DS1, testValueDS1);
         }
         finally
         {
            DatabaseUtils.closeResultSetAndStatement(rsResults, selectStatement);
            rsResults = null;
            selectStatement = null;
         }

         // At this point we know we have inserted correct records in particular 
         // tables for particular data sources. Now try to retrieve data from TABLE 2
         // using data source 1 and retrieve data from TABLE 1 using data source 2.
         // Now try to select from DS1 using this connection
         try
         {
            selectStatement = con1.createStatement();
            // TODO: Bug: c3p0 if an SQL Exception occurred, c3p0 will 
            // explicitly close connection:
            // [c3p0] A PooledConnection died due to the following error!
            // PSQLException: ERROR: permission denied for relation req_conn_test2
            rsResults = selectStatement.executeQuery(SELECT_VALUE2);
            // we are expecting that rsResults will be null. Code retrieved data from
            // rsResults is for testing (debug) purposes
            if (rsResults.next())
            {
              testValueDS1 = rsResults.getString(1);  
            }
            
            if (DatabaseImpl.getInstance() instanceof MySQLDatabaseImpl)
            {
               // MySQL as of version 4.1.14 doesn't isolate user tables of 
               // different users from each other
               assertNotNull("MySQL user isolation behavior has changed."
                             + " Review it", rsResults);
            }
            else
            {
               assertNull("The database by default doesn't isolate tables" 
                          + " created by one user from tables created by other" 
                          + " user since it should not be possible to select"
                          + " value from Table 2 for DS1", rsResults);
            }
         }
         catch (SQLException sqlExc)
         {
            DatabaseTransactionFactoryImpl.getInstance().rollbackTransaction(con1);
            
            // This exception is expected, because there is not Table 2 for DS1 
            // (Table 2 belongs to DS2)
            assertNull("There should not be selected value from Table 2 for DS1", 
                       rsResults);
         }
         finally
         {
            DatabaseUtils.closeResultSetAndStatement(rsResults, selectStatement);
            rsResults = null;
            selectStatement = null;
         }
         
         // set to DS2
         m_connectionFactory.setDefaultDataSource(DATASOURCE_NAME_2);

         try
         {
            selectStatement = con2.createStatement();
            rsResults = selectStatement.executeQuery(SELECT_VALUE1);
            if (rsResults.next())
            {
              testValueDS2 = rsResults.getString(1);  
            }

            if (DatabaseImpl.getInstance() instanceof MySQLDatabaseImpl)
            {
               // MySQL as of version 4.1.14 doesn't isolate user tables of 
               // different users from each other
               assertNotNull("MySQL user isolation behavior has changed."
                             + " Review it", rsResults);
            }
            else
            {
               assertNull("The database doesn't isolate tables created by one" 
                          + " user from tables created by other user since it" 
                          + " should not be possible to select value from Table"
                          + " 1 for DS2", rsResults);
            }
         }
         catch (SQLException sqlExc)
         {
            DatabaseTransactionFactoryImpl.getInstance().rollbackTransaction(con2);

            // This exception is expected, because there is not Table 1 for DS2 
            // (Table 1 belongs to DS1)
            assertNull("There should not be selected value from Table 1 for DS2", 
                        rsResults);
         }
         finally
         {
            DatabaseUtils.closeResultSetAndStatement(rsResults, selectStatement);
            rsResults = null;
            selectStatement = null;
         }
      }
      finally
      {
         Statement stmQuery = null;

         try
         {
            // Execute this only if we allocated connection
            if (con1 != null)
            {
               // set to DS1
               m_connectionFactory.setDefaultDataSource(DATASOURCE_NAME_1);
            
               try
               {
                  stmQuery = con1.createStatement();
                  // drop table 1
                  if (stmQuery.execute(DROP_TABLE1))
                  {
                     // Close any results
                     stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
                  }
                  // We are using 2 connections so we need to make commitTransaction for particular
                  // connection. There will be better to have distributed connection manager.
                  DatabaseTransactionFactoryImpl.getInstance().commitTransaction(con1);         
               }
               finally
               {
                   DatabaseUtils.closeStatement(stmQuery);            
               }
            }
         }
         catch (Throwable thr)
         {
            // We are using 2 connections so we need to do rollbackTransaction for particular 
            // connection.There will be better to have distributed connection manager.
               DatabaseTransactionFactoryImpl.getInstance().rollbackTransaction(con1);
            throw new Exception(thr);
         }
         finally
         {
            try
            {
               if (con2 != null)
               {
                  // set to DS2
                  m_connectionFactory.setDefaultDataSource(DATASOURCE_NAME_2);
               
                  try
                  {
                     stmQuery = con2.createStatement();
                     // drop table 2
                     if (stmQuery.execute(DROP_TABLE2))
                     {
                        // Close any results
                        stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
                     }
                  }
                  finally
                  {
                      DatabaseUtils.closeStatement(stmQuery);            
                  }
                  // We are using 2 connections so we need to make commitTransaction for particular
                  // connection. There will be better to have distributed connection manager.
                  DatabaseTransactionFactoryImpl.getInstance().commitTransaction(con2);
               }
            }
            catch (Throwable thr)
            {
               // We are using 2 connections so we need to do rollbackTransaction for particular 
               // connection.There will be better to have distributed connection manager.
               DatabaseTransactionFactoryImpl.getInstance().rollbackTransaction(con2);
               throw new Exception(thr);
            }
            finally
            {
               m_connectionFactory.returnConnection(con1);
               m_connectionFactory.returnConnection(con2);
            }
         }
      }
   }

   /**
    * Test for requesting connection by user and password. There are 2 data sources. 
    * There shoul be possible to switch them and retrieve connections from each one of them.
    * And select some data drom DB. These data should be different for both connections.
    * 
    * @throws Exception - an error has occurred during test
    */
   public void testRequestConnectionByUserAndPassword(
   ) throws Exception
   {
      Connection con1 = null;
      int testConCountDS1 = -1;

      // at this moment there are added 2 data sources
      
      // set to DS1, request connection from DS1 and get connection count for DS1
      m_connectionFactory.setDefaultDataSource(DATASOURCE_NAME_1);
      try
      {
         // Try to trequest connection using different user name only
         // Request autocommit true since we are just reading data from the database
         con1 = m_connectionFactory.requestConnection(true, 
                                       m_strUsername + "_diff", m_strPassword);
         fail("The connection should have not been allocated.");
      } 
      catch (OSSDatabaseAccessException daeExc)
      {
         // This exception is expected
      }
      finally
      {
         // In case the connection was retrieved, return it, this will also 
         // work if it is null
         m_connectionFactory.returnConnection(con1);
         con1 = null;
      }
      try
      {
         // TODO: Bug: Proxool: Here we request connection for user = bastatest
         // but an error will occur: User not found: BASTATEST_DIFF which shows as
         // SQLException below
         // Request autocommit true since we are just reading data from the database
         con1 = m_connectionFactory.requestConnection(true, m_strUsername, m_strPassword);
         testConCountDS1 = m_connectionFactory.getRequestedConnectionCount();
         
         assertEquals("Incorrect number of connections for DataSource 1", 1, testConCountDS1);
      }
      catch (OSSDatabaseAccessException ossExc)
      {
         // See the bug description above
         s_logger.log(Level.WARNING, "Unexpected exception has occurred", ossExc);

         if (this instanceof ProxoolDatabaseConnectionFactoryTestInternal)
         {
            // This was logged as defect #1110440. Since this is not our bug
            // but a Proxool bug make it not throw an exception
            assertTrue("Defect #1110440 behavior of Proxool has changed."
                        + " Review it.", 
                        ThrowableUtils.toString(ossExc).indexOf(
                           "User not found: BASTATEST_DIFF") != 1);
         }
         else
         {
            fail("Unexpected exception has occurred: " + ossExc.getMessage());
         }
      }
      finally
      {
         m_connectionFactory.returnConnection(con1);
      }
   }


   /**
    * Test for getting data source specified by name. There should be not changed to 
    * default data source, only thing that will change will be maximal connection 
    * count and connection count for specified data source.
    * 
    * @throws Exception - an error has occurred during test
    */
   
   public void testRequestConnectionCountByDataSource(
   ) throws Exception
   {
      Connection con1 = null;
      Connection con2 = null;
      int testReqConCount = -1;
      int testTotalReqConCount = -1;

      try
      {
         // Check if there is set default data source 1.
         assertEquals("Incorrect default data source is specified",
                      DATASOURCE_NAME_1, m_connectionFactory.getDefaultDataSourceName());

         // Check for total connection count
         testTotalReqConCount = m_connectionFactory.getTotalRequestedConnectionCount();
         assertEquals("Incorrect initial total connection count.",
                      0, testTotalReqConCount);

         // Check for connection count
         testReqConCount = m_connectionFactory.getRequestedConnectionCount();
         assertEquals("Incorrect initial connection count for data source 1.",
                      0, testReqConCount);

         // Request connection from data source 2 (there should not be changed default data source)
         // Request autocommit true since we are just reading data from the database
         con2 = m_connectionFactory.requestConnection(true, DATASOURCE_NAME_2);
         assertTrue("Autocommit should be true for pooled connections.", con2.getAutoCommit());
         
         // Check if there is still default data source 1.
         assertEquals("There should be still datasource 1 specified as default",
                      DATASOURCE_NAME_1, m_connectionFactory.getDefaultDataSourceName());

         // Check for total connection count
         testTotalReqConCount = m_connectionFactory.getTotalRequestedConnectionCount();
         assertEquals("Incorrect total connection count after requesting connection " +
                      "from data source 2.", 1, testTotalReqConCount);

         // Check for connection count of data source 1
         testReqConCount = m_connectionFactory.getRequestedConnectionCount();
         assertEquals("Incorrect connection count for data source 1.",
                      0, testReqConCount);

         // Now set datasource 2 as default
         m_connectionFactory.setDefaultDataSource(DATASOURCE_NAME_2);

         // Request connection from data source 1 (there should not be changed default data source)
         // Request autocommit true since we are just reading data from the database
         con1 = m_connectionFactory.requestConnection(true, DATASOURCE_NAME_1);
         assertTrue("Autocommit should be true for pooled connections.", con1.getAutoCommit());

         // Check if there is still default data source 2.
         assertEquals("There should be still datasource 2 specified as default",
                      DATASOURCE_NAME_2, m_connectionFactory.getDefaultDataSourceName());

         // Check for total connection count
         testTotalReqConCount = m_connectionFactory.getTotalRequestedConnectionCount();
         assertEquals("Incorrect total connection count after requesting connection " +
                      "from data source 1.", 2, testTotalReqConCount);

         // Check for connection count of data source 2
         testReqConCount = m_connectionFactory.getRequestedConnectionCount();
         assertEquals("Incorrect connection count for data source 2.",
                      1, testReqConCount);

         // Now set again datasource 1 as default
         m_connectionFactory.setDefaultDataSource(DATASOURCE_NAME_1);

         // Check for connection count of data source 1
         testReqConCount = m_connectionFactory.getRequestedConnectionCount();
         assertEquals("Incorrect connection count for data source 1.",
                      1, testReqConCount);

      }
      finally
      {
         // return connections
         m_connectionFactory.returnConnection(con1);
         m_connectionFactory.returnConnection(con2);
      }
   }

   /**
    * Test for getting data source specified by name. There should be not changed to 
    * default data source, only thing that will change will be maximal connection 
    * count and connection count for specified data source.
    * 
    * @throws Exception - an error has occurred during test
    */
   public void testRequestConnectionCountByDataSourceWithUserAndPasswd(
   ) throws Exception
   {
      Connection con1 = null;
      Connection con2 = null;
      int testReqConCount = -1;
      int testTotalReqConCount = -1;

      try
      {
         // Check if there is set default data source 1.
         assertEquals("Incorrect default data source is specified",
                      DATASOURCE_NAME_1, m_connectionFactory.getDefaultDataSourceName());

         // Check for total connection count
         testTotalReqConCount = m_connectionFactory.getTotalRequestedConnectionCount();
         assertEquals("Incorrect initial total connection count.",
                      0, testTotalReqConCount);

         // Check for connection count
         testReqConCount = m_connectionFactory.getRequestedConnectionCount();
         assertEquals("Incorrect initial connection count for data source 1.",
                      0, testReqConCount);

         // Request connection from data source 2 (there should not be changed 
         // default data source)
         // Request autocommit true since we are just reading data from the database
         con2 = m_connectionFactory.requestConnection(true, DATASOURCE_NAME_2,
                                                      m_strDatabaseTestUser,
                                                      m_strPassword);
         assertTrue("Autocommit should be true for pooled connections.", con2.getAutoCommit());
         
         // Check if there is still default data source 1.
         assertEquals("There should be still datasource 1 specified as default",
                      DATASOURCE_NAME_1, m_connectionFactory.getDefaultDataSourceName());

         // Check for total connection count
         testTotalReqConCount = m_connectionFactory.getTotalRequestedConnectionCount();
         assertEquals("Incorrect total connection count after requesting connection " +
                      "from data source 2.", 1, testTotalReqConCount);

         // Check for connection count of data source 1
         testReqConCount = m_connectionFactory.getRequestedConnectionCount();
         assertEquals("Incorrect connection count for data source 1.",
                      0, testReqConCount);

         // Now set datasource 2 as default
         m_connectionFactory.setDefaultDataSource(DATASOURCE_NAME_2);

         // Request connection from data source 1 (there should not be changed default data source)
         // Request autocommit true since we are just reading data from the database
         con1 = m_connectionFactory.requestConnection(true, DATASOURCE_NAME_1,
                                                      m_strUsername,
                                                      m_strPassword);
         assertTrue("Autocommit should be true for pooled connections.", con1.getAutoCommit());

         // Check if there is still default data source 2.
         assertEquals("There should be still datasource 2 specified as default",
                      DATASOURCE_NAME_2, m_connectionFactory.getDefaultDataSourceName());

         // Check for total connection count
         testTotalReqConCount = m_connectionFactory.getTotalRequestedConnectionCount();
         assertEquals("Incorrect total connection count after requesting connection " +
                      "from data source 1.", 2, testTotalReqConCount);

         // Check for connection count of data source 2
         testReqConCount = m_connectionFactory.getRequestedConnectionCount();
         assertEquals("Incorrect connection count for data source 2.",
                      1, testReqConCount);

         // Now set again datasource 1 as default
         m_connectionFactory.setDefaultDataSource(DATASOURCE_NAME_1);

         // Check for connection count of data source 1
         testReqConCount = m_connectionFactory.getRequestedConnectionCount();
         assertEquals("Incorrect connection count for data source 1.",
                      1, testReqConCount);
      }
      finally
      {
         // return connections
         m_connectionFactory.returnConnection(con1);
         m_connectionFactory.returnConnection(con2);
      }
   }
}
