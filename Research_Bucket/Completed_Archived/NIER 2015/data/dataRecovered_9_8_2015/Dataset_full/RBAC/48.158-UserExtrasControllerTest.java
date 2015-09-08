/*
 * Copyright (c) 2003 - 2009 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: UserExtrasControllerTest.java,v 1.19 2009/09/20 05:32:57 bastafidli Exp $
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

package org.opensubsystems.security.logic;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.opensubsystems.core.application.Application;
import org.opensubsystems.security.application.SecureApplicationTestSetup;
import org.opensubsystems.core.application.ModuleManager;
import org.opensubsystems.core.application.impl.ApplicationImpl;
import org.opensubsystems.core.data.DataObject;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.logic.ControllerManager;
import org.opensubsystems.core.persist.DataFactoryManager;
import org.opensubsystems.core.persist.db.DatabaseTestSuite;
import org.opensubsystems.core.util.CallContext;
import org.opensubsystems.core.util.Tests;
import org.opensubsystems.core.util.jdbc.DatabaseUtils;
import org.opensubsystems.security.application.SecurityBackendModule;
import org.opensubsystems.security.data.InternalSession;
import org.opensubsystems.security.data.Role;
import org.opensubsystems.security.data.User;
import org.opensubsystems.security.logic.impl.UserExtrasControllerImpl;
import org.opensubsystems.security.persist.InternalSessionFactory;
import org.opensubsystems.security.persist.RoleFactory;
import org.opensubsystems.security.persist.UserFactory;
import org.opensubsystems.security.persist.db.SecureDatabaseTest;
import org.opensubsystems.security.persist.db.UserDatabaseSchema;
import org.opensubsystems.security.utils.TestUserDatabaseFactoryUtils;

/**
 * Test for UserExtrasController interface implementation class. 
 * 
 * @version $Id: UserExtrasControllerTest.java,v 1.19 2009/09/20 05:32:57 bastafidli Exp $
 * @author Julo Legeny
 * @code.reviewer Miro Halas
 * @code.reviewed Initial revision
 */
public final class UserExtrasControllerTest extends Tests
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private UserExtrasControllerTest(
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
      TestSuite suite = new DatabaseTestSuite("UserExtrasControllerTest");
      suite.addTestSuite(UserExtrasControllerTestInternal.class);
      // Use SecureApplicationTestSetup instead of DatabaseTestSetup since the 
      // tested classes are part of secure application module specified in the 
      // tests below. This setup ensures that all the required related classes 
      // are correctly initialized
      TestSetup wrapper = new SecureApplicationTestSetup(suite);

      return wrapper;
   }

   /**
    * Internal class which can be included in other test suites directly without
    * including the above suite. This allows us to group multiple tests 
    * together and the execute the DatabaseTestSetup only once 
    */
   public static class UserExtrasControllerTestInternal extends SecureDatabaseTest
   {
      // Cached values ////////////////////////////////////////////////////////////
   
      /**
       * Factory to manage roles.
       */
      protected RoleFactory m_roleFactory;
   
      /**
       * Factory to manage internal sessions.
       */
      protected InternalSessionFactory m_sessionFactory;
   
      /**
       * Factory to manage users.
       */
      protected UserFactory m_userFactory;
   
      /**
       * Controller used to manipulate users.
       */
      protected UserController m_userControl;
   
      /**
       * Controller used to manipulate users.
       */
      protected UserExtrasController m_userExtrasControl;
      
      /**
       * Factory utilities to manage users.
       */
      protected TestUserDatabaseFactoryUtils m_userFactoryUtils;
   
      // Constructors /////////////////////////////////////////////////////////////
      
      /**
       * Static initializer.
       */
      static
      {
         try
         {
            // The tested class is part of this module. Make it part of the
            // application so that the SecureApplicationTestSetup above can ensure 
            // that all the required related classes are correctly initialized
            Application app;
            
            app = ApplicationImpl.getInstance();
            app.add(ModuleManager.getInstance(SecurityBackendModule.class));
         }
         catch (OSSException bfeExc)
         {
            throw new RuntimeException("Unexpected exception.", bfeExc);
         }
      }
   
      /**
       * Constructor
       * 
       * @param strTestName - name of the test
       * @throws Exception - an error has occurred
       */
      public UserExtrasControllerTestInternal(
         String strTestName
      ) throws Exception
      {
         super(strTestName);
   
         m_roleFactory = (RoleFactory)DataFactoryManager.getInstance(
                                         RoleFactory.class);      
         m_userFactory = (UserFactory)DataFactoryManager.getInstance(
                                         UserFactory.class);
         m_sessionFactory = (InternalSessionFactory)DataFactoryManager.getInstance(
                                         InternalSessionFactory.class);
         m_userControl = (UserController)ControllerManager.getInstance(
                                            UserController.class);
         m_userExtrasControl = (UserExtrasController)ControllerManager.getInstance(
                                            UserExtrasController.class);
         m_userFactoryUtils = new TestUserDatabaseFactoryUtils();
      }
   
      // Tests ////////////////////////////////////////////////////////////////////
   
      /**
       * Test for importing user data from the MS Excel file
       * 
       * @throws Exception - an error has occurred
       */
      public void testImportWithSomeIncorrectUserData() throws Exception 
      {
         PreparedStatement statement        = null;
         ResultSet         results          = null;     
         User              user            = null;
         User              userLoggedIn    = null;
         User              testUser        = null; 
         InternalSession   session         = null;
         Role              testPersonalRole  = null;
//         Role              testPersonalRole2 = null;
         String            strQuery;
         int[]             importResult     = null;
         boolean           bInsert          = true;
         boolean           bClean           = false;
         int               iUserCount       = 0;
         int               iCount;
    
         String strRootFolder = "sources/tests/data/security/";
         String strFilePath   = "test_import_user.xls";
         try
         {
            try
            {
               m_transaction.begin();
               // create user
               user = new User(DataObject.NEW_ID,
                                CallContext.getInstance().getCurrentDomainId(),
                                "test_first_name",
                                "test_last_name",
                                "test_phone",
                                "test_fax",
                                "test_address",
                                "test_email",
                                "test_login_name",
                                "test_password",
                                true,
                                false,
                                true,
                                true,
                                null,
                                null, true
                               );
               user = m_userControl.create(user, "");
      
               // create user that will be logged in
               userLoggedIn = new User(DataObject.NEW_ID,
                                        CallContext.getInstance().getCurrentDomainId(),
                                        "test_first_name_1",
                                        "test_last_name_1",
                                        "test_phone_1",
                                        "test_fax_1",
                                        "test_address_1",
                                        "test_email_1",
                                        "test_login_name_1",
                                        "test_password_1",
                                        true,
                                        false,
                                        true,
                                        true,
                                        null,
                                        null, true
                                       );
               userLoggedIn = m_userControl.create(userLoggedIn, "");
      
               // create test internal session
               session = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            userLoggedIn.getId(),
                                            "test_generated_code",
                                            "test_c_IP",
                                            "test_browser_type",
                                            null,
                                            true
                                            );
                  
               session = (InternalSession)m_sessionFactory.create(session);
      
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
      
            // Load personal roles so we can do cleanup
//            testPersonalRole = m_roleFactory.getPersonal(user.getId());
//            testPersonalRole2 = m_roleFactory.getPersonal(userLoggedIn.getId());
            
            // Try to get user number of all users within the DB
            iUserCount = getNumberOfUsers();
      
            // Test importing XLS file containing incorrect rows and bAddOnly = true (there 
            // will be imported new users and no user will be deleted)  
      
            m_transaction.begin();
            try
            {
               importResult = m_userExtrasControl.importUserData(strRootFolder + strFilePath, 
                                                           1, bInsert, bClean);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }  
   
            try
            {
               // try to get IDs of inserted users
               strQuery = "select ID from " + UserDatabaseSchema.USER_TABLE_NAME + " where FIRST_NAME like 'Fname%'";   
               statement = m_connection.prepareStatement(strQuery);
               results = statement.executeQuery();
   
               assertEquals("Number of inserted rows is incorrect", 8, importResult[0]);
               assertEquals("Number of incorrect rows is incorrect", 9, importResult[1]);
               assertEquals("Number of skipped rows is incorrect", 1, importResult[2]);
               assertEquals("Number of deleted rows is incorrect", 0, importResult[3]);
               
               int userId;
               // testing if created personal roles are correct;
               iCount = 0;
               while (results.next())
               {
                  iCount++;
                  userId = results.getInt(1);
                  testUser = (User)m_userFactory.get(
                                userId,
                                CallContext.getInstance().getCurrentDomainId());
                  
                  testPersonalRole = m_roleFactory.getPersonal(userId);
                  // testing created personal role 
                  comparePersonalRoleWithUser(testPersonalRole, testUser);
               }
               assertEquals("There should be inserted 8 users", 8, iCount);
            }
            finally
            {
               DatabaseUtils.closeResultSetAndStatement(results, statement);
               results = null; 
               statement = null;
            }
            iCount = getNumberOfUsers();
            assertEquals("Number of all users in DB is incorrect", iUserCount + 8,
                         iCount);
         }
         finally
         {
            File outFile = new File(strRootFolder + UserExtrasControllerImpl.OUTPUT_FILE_PREFIX 
                                    + strFilePath);
            if (outFile.exists())
            {
               outFile.delete();
            }
            
            // Cleanup
            m_transaction.begin();
            try
            {
               // delete users
               m_userFactoryUtils.deleteUserCascadeManualUsingLike("Fname%");
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }  
            m_transaction.begin();
            try
            {
               // delete users cascade (with all referenced stuff)
               if ((user != null) && (user.getId() != DataObject.NEW_ID))
               {
                  m_userFactoryUtils.deleteUserCascadeManual(user.getId());
               }
               if ((userLoggedIn != null) && (userLoggedIn.getId() != DataObject.NEW_ID))
               {
                  m_userFactoryUtils.deleteUserCascadeManual(userLoggedIn.getId());
               }
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
         }
      }
         
      /**
       * Test for importing user data from the MS Excel file
       * 
       * @throws Exception - an error has occurred
       */
      public void testImportWithAllCorrectUserData() throws Exception 
      {
         PreparedStatement statement        = null;
         ResultSet         results          = null;     
         User              user            = null;
         User              userLoggedIn    = null;
         User              testUser        = null; 
         InternalSession   session         = null;
         Role              testPersonalRole  = null;
//         Role              testPersonalRole2 = null;
         String            strQuery;
         int[]             importResult     = null;
         boolean           bInsert          = true;
         boolean           bClean           = false;
         int               iUserCount       = 0;
         int               iCount;
    
         String strRootFolder = "sources/tests/data/security/";
         String strOKFilePath = "test_import_user_ok.xls";
         try
         {
            try
            {
               m_transaction.begin();
               // create user
               user = new User(DataObject.NEW_ID,
                                CallContext.getInstance().getCurrentDomainId(),
                                "test_first_name",
                                "test_last_name",
                                "test_phone",
                                "test_fax",
                                "test_address",
                                "test_email",
                                "test_login_name",
                                "test_password",
                                true,
                                false,
                                true,
                                true,
                                null,
                                null, true
                               );
               user = m_userControl.create(user, "");
      
               // create user that will be logged in
               userLoggedIn = new User(DataObject.NEW_ID,
                                        CallContext.getInstance().getCurrentDomainId(),
                                        "test_first_name_1",
                                        "test_last_name_1",
                                        "test_phone_1",
                                        "test_fax_1",
                                        "test_address_1",
                                        "test_email_1",
                                        "test_login_name_1",
                                        "test_password_1",
                                        true,
                                        false,
                                        true,
                                        true,
                                        null,
                                        null, true
                                       );
               userLoggedIn = m_userControl.create(userLoggedIn, "");
      
               // create test internal session
               session = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            userLoggedIn.getId(),
                                            "test_generated_code",
                                            "test_c_IP",
                                            "test_browser_type",
                                            null,
                                            true
                                            );
                  
               session = (InternalSession)m_sessionFactory.create(session);
      
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
      
            // Load personal roles so we can do cleanup
//            testPersonalRole = m_roleFactory.getPersonal(user.getId());
//            testPersonalRole2 = m_roleFactory.getPersonal(userLoggedIn.getId());
            
            // Try to get user number of all users within the DB
            iUserCount = getNumberOfUsers();
      
             // Test importing XLS file with all correct rows and bAddOnly = true (there 
             // will be imported new users and no user will be deleted)
            m_transaction.begin();
            try
            {
               importResult = m_userExtrasControl.importUserData(strRootFolder + strOKFilePath, 
                                                           1, bInsert, bClean);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }  
   
            try
            {
               strQuery = "select ID from " + UserDatabaseSchema.USER_TABLE_NAME + " where FIRST_NAME like 'FnameOK%'";   
               statement = m_connection.prepareStatement(strQuery);
               results = statement.executeQuery();
   
               assertEquals("Number of inserted rows is incorrect", 4, importResult[0]);
               assertEquals("Number of incorrect rows is incorrect", 0, importResult[1]);
               assertEquals("Number of skipped rows is incorrect", 0, importResult[2]);
               assertEquals("Number of deleted rows is incorrect", 0, importResult[3]);
               
               int userId;
               iCount = 0;
               // testing if created personal roles are correct;
               while (results.next())
               {
                  iCount++;
                  userId = results.getInt(1);
                  testUser = (User)m_userFactory.get(
                                userId,
                                CallContext.getInstance().getCurrentDomainId());
                  
                  testPersonalRole = m_roleFactory.getPersonal(userId);
                  // testing created personal role 
                  comparePersonalRoleWithUser(testPersonalRole, testUser);
               }
   
               assertEquals("There should be inserted 4 users", 4, iCount);
            }
            finally
            {
               DatabaseUtils.closeResultSetAndStatement(results, statement);
               results = null; 
               statement = null;
            }         
            iCount = getNumberOfUsers();
            assertEquals("Number of all users in DB is incorrect", iUserCount + 4,
                         iCount);
         }
         finally
         {
            File outFile = new File(strRootFolder + UserExtrasControllerImpl.OUTPUT_FILE_PREFIX 
                                    + strOKFilePath);
            if (outFile.exists())
            {
               outFile.delete();
            }
            
            // Cleanup
            m_transaction.begin();
            try
            {
               // delete users
               m_userFactoryUtils.deleteUserCascadeManualUsingLike("FnameOK%");
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }  
            m_transaction.begin();
            try
            {
               // delete users cascade (with all referenced stuff)
               if ((user != null) && (user.getId() != DataObject.NEW_ID))
               {
                  m_userFactoryUtils.deleteUserCascadeManual(user.getId());
               }
               if ((userLoggedIn != null) && (userLoggedIn.getId() != DataObject.NEW_ID))
               {
                  m_userFactoryUtils.deleteUserCascadeManual(userLoggedIn.getId());
               }
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
         }
      }
   
      /**
       * Test for importing user data from the MS Excel file
       * 
       * @throws Exception - an error has occurred
       */
      public void testImportAndCleanWithSomeIncorrectUserData(
      ) throws Exception 
      {
         PreparedStatement statement        = null;
         ResultSet         results          = null;     
         User              user            = null;
         User              userLoggedIn    = null;
         User              testUser        = null; 
         InternalSession   session         = null;
         Role              testPersonalRole  = null;
//         Role              testPersonalRole2 = null;
         String            strQuery;
         int[]             importResult     = null;
         boolean           bInsert          = true;
         boolean           bClean           = false;
         int               iUserCount       = 0;
         int               iCount;
    
         String strRootFolder = "sources/tests/data/security/";
         String strFilePath   = "test_import_user.xls";
         try
         {
            try
            {
               m_transaction.begin();
               // create user
               user = new User(DataObject.NEW_ID,
                                CallContext.getInstance().getCurrentDomainId(),
                                "test_first_name",
                                "test_last_name",
                                "test_phone",
                                "test_fax",
                                "test_address",
                                "test_email",
                                "test_login_name",
                                "test_password",
                                true,
                                false,
                                true,
                                true,
                                null,
                                null, true
                               );
               user = m_userControl.create(user, "");
      
               // create user that will be logged in
               userLoggedIn = new User(DataObject.NEW_ID,
                                        CallContext.getInstance().getCurrentDomainId(),
                                        "test_first_name_1",
                                        "test_last_name_1",
                                        "test_phone_1",
                                        "test_fax_1",
                                        "test_address_1",
                                        "test_email_1",
                                        "test_login_name_1",
                                        "test_password_1",
                                        true,
                                        false,
                                        true,
                                        true,
                                        null,
                                        null, true
                                       );
               userLoggedIn = m_userControl.create(userLoggedIn, "");
      
               // create test internal session
               session = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            userLoggedIn.getId(),
                                            "test_generated_code",
                                            "test_c_IP",
                                            "test_browser_type",
                                            null,
                                            true
                                            );
                  
               session = (InternalSession)m_sessionFactory.create(session);
      
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
      
            // Load personal roles so we can do cleanup
//            testPersonalRole = m_roleFactory.getPersonal(user.getId());
//            testPersonalRole2 = m_roleFactory.getPersonal(userLoggedIn.getId());
            
            // Try to get user number of all users within the DB
            iUserCount = getNumberOfUsers();
            bClean = true;
      
            // test importing XLS file containing incorrect rows and bAddOnly = false (there 
            // will be imported new users and not imported users will be deleted from DB)  
      
            m_transaction.begin();
            try
            {
               importResult = m_userExtrasControl.importUserData(strRootFolder + strFilePath, 
                                                           1, bInsert, bClean);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }  
   
            try
            {
               // try to get inserted users
               strQuery = "select ID from " + UserDatabaseSchema.USER_TABLE_NAME + " where FIRST_NAME like 'Fname%'";   
               statement = m_connection.prepareStatement(strQuery);
               results = statement.executeQuery();
   
               assertEquals("Number of inserted rows is incorrect", 8, importResult[0]);
               assertEquals("Number of incorrect rows is incorrect", 9, importResult[1]);
               assertEquals("Number of skipped rows is incorrect", 1, importResult[2]);
               assertEquals("Number of deleted rows is incorrect", 1, importResult[3]);
               
               user = null;
               int userId;
               // testing if created personal roles are correct;
               iCount = 0;
               while (results.next())
               {
                  iCount++;
                  userId = results.getInt(1);
                  testUser = (User)m_userFactory.get(
                                userId,
                                CallContext.getInstance().getCurrentDomainId());
                  
                  testPersonalRole = m_roleFactory.getPersonal(userId);
                  // testing created personal role 
                  comparePersonalRoleWithUser(testPersonalRole, testUser);
               }
               assertEquals("There should be inserted 8 users", 8, iCount);
            }
            finally
            {
               DatabaseUtils.closeResultSetAndStatement(results, statement);
            }
            iCount = getNumberOfUsers();
            // One user was deleted
            assertEquals("Number of all users in DB is incorrect", 
                         iUserCount + importResult[0] - importResult[3],
                         iCount);
         }
         finally
         {
            File outFile = new File(strRootFolder + UserExtrasControllerImpl.OUTPUT_FILE_PREFIX 
                                    + strFilePath);
            if (outFile.exists())
            {
               outFile.delete();
            }
            
            // Cleanup
            m_transaction.begin();
            try
            {
               // delete users
               m_userFactoryUtils.deleteUserCascadeManualUsingLike("Fname%");
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }  
            m_transaction.begin();
            try
            {
               // delete users cascade (with all referenced stuff)
               if ((user != null) && (user.getId() != DataObject.NEW_ID))
               {
                  m_userFactoryUtils.deleteUserCascadeManual(user.getId());
               }
               if ((userLoggedIn != null) && (userLoggedIn.getId() != DataObject.NEW_ID))
               {
                  m_userFactoryUtils.deleteUserCascadeManual(userLoggedIn.getId());
               }
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
         }
      }
   
      /**
       * Test for importing user data from the MS Excel file
       * 
       * @throws Exception - an error has occurred
       */
      public void testImportAndCleanWithCorrectUserData(
      ) throws Exception 
      {
         PreparedStatement statement        = null;
         ResultSet         results          = null;     
         User              user            = null;
         User              userLoggedIn    = null;
         User              testUser        = null; 
         InternalSession   session         = null;
         Role              testPersonalRole  = null;
//         Role              testPersonalRole2 = null;
         String            strQuery;
         int[]             importResult     = null;
         boolean           bInsert          = true;
         boolean           bClean           = false;
         int               iUserCount       = 0;
         int               iCount;
    
         String strRootFolder = "sources/tests/data/security/";
         String strOKFilePath = "test_import_user_ok.xls";
         try
         {
            try
            {
               m_transaction.begin();
               // create user
               user = new User(DataObject.NEW_ID,
                                CallContext.getInstance().getCurrentDomainId(),
                                "test_first_name",
                                "test_last_name",
                                "test_phone",
                                "test_fax",
                                "test_address",
                                "test_email",
                                "test_login_name",
                                "test_password",
                                true,
                                false,
                                true,
                                true,
                                null,
                                null, true
                               );
               user = m_userControl.create(user, "");
      
               // create user that will be logged in
               userLoggedIn = new User(DataObject.NEW_ID,
                                        CallContext.getInstance().getCurrentDomainId(),
                                        "test_first_name_1",
                                        "test_last_name_1",
                                        "test_phone_1",
                                        "test_fax_1",
                                        "test_address_1",
                                        "test_email_1",
                                        "test_login_name_1",
                                        "test_password_1",
                                        true,
                                        false,
                                        true,
                                        true,
                                        null,
                                        null, true
                                       );
               userLoggedIn = m_userControl.create(userLoggedIn, "");
      
               // create test internal session
               session = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            userLoggedIn.getId(),
                                            "test_generated_code",
                                            "test_c_IP",
                                            "test_browser_type",
                                            null,
                                            true
                                            );
                  
               session = (InternalSession)m_sessionFactory.create(session);
      
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
      
            // Load personal roles so we can do cleanup
//            testPersonalRole = m_roleFactory.getPersonal(user.getId());
//            testPersonalRole2 = m_roleFactory.getPersonal(userLoggedIn.getId());
            
            // Try to get user number of all users within the DB
            iUserCount = getNumberOfUsers();
      
            // test importing XLS file containing all correct rows and bAddOnly = false (there 
            // will be imported new users and not imported users will be deleted from DB)
            bClean = true;
            m_transaction.begin();
            try
            {
               importResult = m_userExtrasControl.importUserData(strRootFolder + strOKFilePath, 
                                                           1, bInsert, bClean);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }  
   
            try
            {
               // try to get inserted users user
               strQuery = "select ID from " + UserDatabaseSchema.USER_TABLE_NAME + " where FIRST_NAME like 'FnameOK%'";   
               statement = m_connection.prepareStatement(strQuery);
               results = statement.executeQuery();
   
               assertEquals("Number of inserted rows is incorrect", 4, importResult[0]);
               assertEquals("Number of incorrect rows is incorrect", 0, importResult[1]);
               assertEquals("Number of skipped rows is incorrect", 0, importResult[2]);
               assertEquals("Number of deleted rows is incorrect", 1, importResult[3]);
               user = null;
               int userId;
               iCount = 0;
               // testing if created personal roles are correct;
               while (results.next())
               {
                  iCount++;
                  userId = results.getInt(1);
                  testUser = (User)m_userFactory.get(
                                userId,
                                CallContext.getInstance().getCurrentDomainId());
                  
                  testPersonalRole = m_roleFactory.getPersonal(userId);
                  // testing created personal role 
                  comparePersonalRoleWithUser(testPersonalRole, testUser);
               }
               assertEquals("There should be inserted 4 users", 4, iCount);
            }
            finally
            {
               DatabaseUtils.closeResultSetAndStatement(results, statement);
            }
            iCount = getNumberOfUsers();
            assertEquals("Number of all users in DB is incorrect", 
                         iUserCount + importResult[0] - importResult[3],
                         iCount);
         }
         finally
         {
            File outFile = new File(strRootFolder + UserExtrasControllerImpl.OUTPUT_FILE_PREFIX 
                                    + strOKFilePath);
            if (outFile.exists())
            {
               outFile.delete();
            }
            
            // Cleanup
            m_transaction.begin();
            try
            {
               // delete users
               m_userFactoryUtils.deleteUserCascadeManualUsingLike("Fname%");
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }  
            m_transaction.begin();
            try
            {
               // delete users cascade (with all referenced stuff)
               if ((user != null) && (user.getId() != DataObject.NEW_ID))
               {
                  m_userFactoryUtils.deleteUserCascadeManual(user.getId());
               }
               if ((userLoggedIn != null) && (userLoggedIn.getId() != DataObject.NEW_ID))
               {
                  m_userFactoryUtils.deleteUserCascadeManual(userLoggedIn.getId());
               }
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
         }
      }
   
      /**
       * Test for importing user data from the MS Excel file
       * 
       * @throws Exception - an error has occurred
       */
      public void testImportCheckAndCleanWithSomeIncorrectUserData(
      ) throws Exception 
      {
         PreparedStatement statement        = null;
         ResultSet         results          = null;     
         User              user            = null;
         User              userLoggedIn    = null;
         InternalSession   session         = null;
//         Role              testPersonalRole  = null;
//         Role              testPersonalRole2 = null;
         String            strQuery;
         int[]             importResult     = null;
         boolean           bInsert          = true;
         boolean           bClean           = false;
         int               iUserCount       = 0;
         int               iCount;
    
         String strRootFolder = "sources/tests/data/security/";
         String strFilePath   = "test_import_user.xls";
         try
         {
            try
            {
               m_transaction.begin();
               // create user
               user = new User(DataObject.NEW_ID,
                                CallContext.getInstance().getCurrentDomainId(),
                                "test_first_name",
                                "test_last_name",
                                "test_phone",
                                "test_fax",
                                "test_address",
                                "test_email",
                                "test_login_name",
                                "test_password",
                                true,
                                false,
                                true,
                                true,
                                null,
                                null, true
                               );
               user = m_userControl.create(user, "");
      
               // create user that will be logged in
               userLoggedIn = new User(DataObject.NEW_ID,
                                        CallContext.getInstance().getCurrentDomainId(),
                                        "test_first_name_1",
                                        "test_last_name_1",
                                        "test_phone_1",
                                        "test_fax_1",
                                        "test_address_1",
                                        "test_email_1",
                                        "test_login_name_1",
                                        "test_password_1",
                                        true,
                                        false,
                                        true,
                                        true,
                                        null,
                                        null, true
                                       );
               userLoggedIn = m_userControl.create(userLoggedIn, "");
      
               // create test internal session
               session = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            userLoggedIn.getId(),
                                            "test_generated_code",
                                            "test_c_IP",
                                            "test_browser_type",
                                            null,
                                            true
                                            );
                  
               session = (InternalSession)m_sessionFactory.create(session);
      
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
      
            // Load personal roles so we can do cleanup
//            testPersonalRole = m_roleFactory.getPersonal(user.getId());
//            testPersonalRole2 = m_roleFactory.getPersonal(userLoggedIn.getId());
            
            // Try to get user number of all users within the DB
            iUserCount = getNumberOfUsers();
            bClean = true;
            bInsert = false;
            // --------------------------------------------------------------------------
            // test importing XLS file containing incorrect rows and bClean = true and
            // bInsert = true (there will be only checked spreadsheet data without DB changes)
      
            try
            {     
               // try to get user number of all users within the DB
               strQuery = "select count(ID) from " + UserDatabaseSchema.USER_TABLE_NAME;
               statement = m_connection.prepareStatement(strQuery);
               results = statement.executeQuery();
               results.next();
               iUserCount = results.getInt(1);
            }
            finally
            {
               DatabaseUtils.closeResultSetAndStatement(results, statement);
            }
      
            m_transaction.begin();
            try
            {
               importResult = m_userExtrasControl.importUserData(strRootFolder + strFilePath, 
                                                           1, bInsert, bClean);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }  
   
            try
            {
               // try to get inserted users
               strQuery = "select count(ID) from " + UserDatabaseSchema.USER_TABLE_NAME + " where FIRST_NAME like 'Fname%'";   
               statement = m_connection.prepareStatement(strQuery);
               results = statement.executeQuery();
               results.next();
      
               assertEquals("There should be not inserted users", 0, results.getInt(1));
      
               assertEquals("Number of inserted rows is incorrect", 0, importResult[0]);
               assertEquals("Number of incorrect rows is incorrect", 9, importResult[1]);
               assertEquals("Number of skipped rows is incorrect", 0, importResult[2]);
               assertEquals("Number of deleted rows is incorrect", 0, importResult[3]);
            }
            finally
            {
               DatabaseUtils.closeResultSetAndStatement(results, statement);
            }
            iCount = getNumberOfUsers();
            assertEquals("Number of all users in DB is incorrect", 
                         iUserCount, iCount);
         }
         finally
         {
            File outFile = new File(strRootFolder + UserExtrasControllerImpl.OUTPUT_FILE_PREFIX 
                                    + strFilePath);
            if (outFile.exists())
            {
               outFile.delete();
            }
            
            // Cleanup
            m_transaction.begin();
            try
            {
               // delete users
               m_userFactoryUtils.deleteUserCascadeManualUsingLike("Fname%");
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }  
            m_transaction.begin();
            try
            {
               // delete users cascade (with all referenced stuff)
               if ((user != null) && (user.getId() != DataObject.NEW_ID))
               {
                  m_userFactoryUtils.deleteUserCascadeManual(user.getId());
               }
               if ((userLoggedIn != null) && (userLoggedIn.getId() != DataObject.NEW_ID))
               {
                  m_userFactoryUtils.deleteUserCascadeManual(userLoggedIn.getId());
               }
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
         }
      }
   
      // Helper methods ///////////////////////////////////////////////////////////
      
      /**
       * Get number of users in the database
       * 
       * @return int - number of users in the database
       * @throws Exception - an error has occurred
       */
      protected int getNumberOfUsers(
      ) throws Exception
      {
         PreparedStatement statement = null;
         ResultSet         results   = null;
         int               iUserCount;
         
         
         // Try to get user number of all users within the DB
         try
         {     
            statement = m_connection.prepareStatement("select count(ID) from " + UserDatabaseSchema.USER_TABLE_NAME);
            results = statement.executeQuery();
            results.next();
            iUserCount = results.getInt(1);
         }
         finally
         {
            DatabaseUtils.closeResultSetAndStatement(results, statement);
            results = null; 
            statement = null;
         }
         
         return iUserCount;
      }
   
      /**
       * Compare user with his personal role
       * 
       * @param testPersonalRole - personal role which should be tested
       * @param templateUser - user which serves as template
       */
      protected void comparePersonalRoleWithUser(
         Role testPersonalRole, 
         User templateUser
      )
      {
         assertNotNull("Personal role should not be null", testPersonalRole);
         assertEquals("Personal role name is incorrect", 
                      templateUser.getLoginName(), testPersonalRole.getName());
         assertEquals("Personal role description is incorrect", 
                      templateUser.getFullNameLastFirst(), 
                      testPersonalRole.getDescription());
         assertEquals("Personal role should belong to specific user", 
                      templateUser.getId(),
                      testPersonalRole.getUserId());
         assertTrue("Personal role shoul be allways enabled", 
                    testPersonalRole.isEnabled());
         assertFalse("Personal role shoul be allways modifiable", 
                    testPersonalRole.isUnmodifiable());      
      }
   }
}
