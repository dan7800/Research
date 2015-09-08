/*
 * Copyright (c) 2003 - 2009 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: AuthenticationControllerTest.java,v 1.10 2009/09/20 05:32:57 bastafidli Exp $
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
import org.opensubsystems.security.application.SecurityBackendModule;
import org.opensubsystems.security.data.User;
import org.opensubsystems.security.persist.RoleFactory;
import org.opensubsystems.security.persist.db.SecureDatabaseTest;
import org.opensubsystems.security.persist.db.SecureDatabaseTestUtils;
import org.opensubsystems.security.util.LoginConstants;

/**
 * Test for AuthenticationController interface implementation class.
 * 
 * @version $Id: AuthenticationControllerTest.java,v 1.10 2009/09/20 05:32:57 bastafidli Exp $
 * @author Julo Legeny
 * @code.reviewer Miro Halas
 * @code.reviewed Initial revision
 */
public final class AuthenticationControllerTest extends Tests
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private AuthenticationControllerTest(
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
      TestSuite suite = new DatabaseTestSuite("AuthenticationControllerTest");
      suite.addTestSuite(AuthenticationControllerTestInternal.class);
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
   public static class AuthenticationControllerTestInternal extends SecureDatabaseTest
   {
      // Cached values ////////////////////////////////////////////////////////////
   
      /**
       * Factory to manage roles.
       */
      protected RoleFactory m_roleFactory;
   
      /**
       * Controller used to manipulate users.
       */
      protected UserController m_userControl;
   
      /**
       * Controller used to authenticate users.
       */
      protected AuthenticationController m_authenticateControl;
   
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
      public AuthenticationControllerTestInternal(
         String strTestName
      ) throws Exception
      {
         super(strTestName);
   
         m_roleFactory = (RoleFactory)DataFactoryManager.getInstance(
                                         RoleFactory.class);      
         m_userControl = (UserController)ControllerManager.getInstance(
                                            UserController.class);
         m_authenticateControl = (AuthenticationController)ControllerManager.getInstance(
                                    AuthenticationController.class);
      }
   
      // Tests ////////////////////////////////////////////////////////////////////
   
   
      /**
       * Test for verifying and returning of user
       * 
       *  @throws Exception - an error has occurred
       */
      public void testVerifyAndReturnUserByNameAndPassword(
      ) throws Exception 
      {
         User     user           = null;
         Object[] objVerifyResult = {null, null};
         String   strLoginName    = "test_login_name";
         String   strPassword     = "test_password";
   
         try
         {
            m_transaction.begin();
            try
            {
               // create two users
               user = new User(DataObject.NEW_ID,
                                CallContext.getInstance().getCurrentDomainId(),
                                "test_first_name",
                                "test_last_name",
                                "test_phone",
                                "test_fax",
                                "test_address",
                                "test_email",
                                strLoginName,
                                strPassword,
                                true,
                                true,
                                true,
                                true,
                                null,
                                null, true
                               );
               user = m_userControl.create(user, "");
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // test for correct login and password
            objVerifyResult = m_authenticateControl.verifyAndReturnUser(strLoginName,
                                                                        strPassword);
            assertNotNull("User information cannot be null", objVerifyResult[0]);
            assertEquals("Return value is incorrect", LoginConstants.LOGIN_OK_OBJ,
                                                      objVerifyResult[1]);
            // test for incorrect password
            strPassword = "test_password_wrong";
            objVerifyResult = m_authenticateControl.verifyAndReturnUser(strLoginName,
                                                                        strPassword);
            assertNull("User information should be null", objVerifyResult[0]);
            assertEquals("Return value is incorrect", LoginConstants.LOGIN_INCORRECT_PASSWORD_OBJ,
                                                     objVerifyResult[1]);
            // test for incorrect login
            strLoginName = "test_login_name_wrong";
            objVerifyResult = m_authenticateControl.verifyAndReturnUser(strLoginName,
                                                                        strPassword);
            assertNull("User information should be null", objVerifyResult[0]);
            assertEquals("Return value is incorrect", LoginConstants.LOGIN_NAME_NOT_VALID_OBJ,
                                                     objVerifyResult[1]);
            // test for enabled login with correct login and password
            // since login enabled should have no effect on verification of credentials
            // disable login to the user
            m_transaction.begin();
            try
            {
               m_userControl.updateEnable(Integer.toString(user.getId()), false);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }  
   
            strLoginName = "test_login_name";
            strPassword  = "test_password";
            objVerifyResult = m_authenticateControl.verifyAndReturnUser(strLoginName,
                                                                                   strPassword
                                                                                   );
            assertNotNull("User information should not be null", objVerifyResult[0]);
            assertEquals("Return value is incorrect", LoginConstants.LOGIN_OK_OBJ,
                                                      objVerifyResult[1]);
         }
         finally
         {
            if ((user != null) && (user.getId() != DataObject.NEW_ID))
            {
               m_transaction.begin();
               // delete inserted records
               try
               {
                  // delete user
                  m_userControl.delete(user.getId());
                  m_transaction.commit();
               }
               catch (Throwable thr)
               {
                  m_transaction.rollback();
                  throw new Exception(thr);
               }
            }
         }
      }
   
      /**
       * Test for verifying and returning of user
       * 
       *  @throws Exception - an error has occurred
       */
      public void testVerifyAndReturnUserById(
      ) throws Exception 
      {
         User     user           = null;
         Object[] objVerifyResult = {null, null};
         String   strLoginName    = "test_login_name";
         String   strPassword     = "test_password";
   
         try
         {
            m_transaction.begin();
            try
            {
               // create two users
               user = new User(DataObject.NEW_ID,
                                CallContext.getInstance().getCurrentDomainId(),
                                "test_first_name",
                                "test_last_name",
                                "test_phone",
                                "test_fax",
                                "test_address",
                                "test_email",
                                strLoginName,
                                strPassword,
                                true,
                                true,
                                true,
                                true,
                                null,
                                null, true
                               );
               user = m_userControl.create(user, "");
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }         
            // test for correct login and password
            objVerifyResult = m_authenticateControl.verifyAndReturnUser(
                                 user.getId());
                                  
            assertNotNull("User information cannot be null", objVerifyResult[0]);
            assertEquals("Return value is incorrect", LoginConstants.LOGIN_USER_EXISTS_OBJ,
                                                      objVerifyResult[1]);
   
            // test for incorrect id
            objVerifyResult = m_authenticateControl.verifyAndReturnUser(
                                 user.getId() + 1);
   
            assertNull("User information should be null", objVerifyResult[0]);
            assertEquals("Return value is incorrect", LoginConstants.LOGIN_NAME_NOT_VALID_OBJ,
                                                      objVerifyResult[1]);
                                                     
            // test for enabled login with correct login and password since login 
            // enabled should have no effect on verification of credentials
            
            m_transaction.begin();
            try
            {
               m_userControl.updateEnable(Integer.toString(user.getId()), false);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }  
   
            objVerifyResult = m_authenticateControl.verifyAndReturnUser(
                                 user.getId());
   
            assertNotNull("User information cannot be null", objVerifyResult[0]);
            assertEquals("Return value is incorrect", LoginConstants.LOGIN_USER_EXISTS_OBJ,
                                                      objVerifyResult[1]);
         }
         finally
         {
            if ((user != null) && (user.getId() != DataObject.NEW_ID))
            {
               m_transaction.begin();
               // delete inserted records
               try
               {
                  // delete user
                  m_userControl.delete(user.getId());
                  m_transaction.commit();
               }
               catch (Throwable thr)
               {
                  m_transaction.rollback();
                  throw new Exception(thr);
               }
            }
         }
      }
   
      /**
       * Test for verifying and returning of current user
       * 
       *  @throws Exception - an error has occurred
       */
      public void testVerifyAndCurrentUser(
      ) throws Exception 
      {
         User     user           = null;
         Object[] objVerifyResult = {null, null};
         String   strLoginName    = "test_login_name";
         String   strPassword     = SecureDatabaseTestUtils.DEFAULT_USER_PASSWORD + "aaa";
   
         try
         {
            m_transaction.begin();
            try
            {
               // create dummy user different than the one created 
               // by SecureDatabaseTestUtils
               user = new User(DataObject.NEW_ID,
                                CallContext.getInstance().getCurrentDomainId(),
                                "test_first_name",
                                "test_last_name",
                                "test_phone",
                                "test_fax",
                                "test_address",
                                "test_email",
                                strLoginName,
                                strPassword,
                                true,
                                true,
                                true,
                                true,
                                null,
                                null, true
                               );
               user = m_userControl.create(user, "");
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }         
            // test for correct password
            objVerifyResult = m_authenticateControl.verifyAndReturnCurrentUser(
                                 SecureDatabaseTestUtils.DEFAULT_USER_PASSWORD);
            assertNotNull("User information cannot be null", objVerifyResult[0]);
            assertEquals("Return value is incorrect", LoginConstants.LOGIN_OK_OBJ,
                                                      objVerifyResult[1]);
            // test for incorrect password
            objVerifyResult = m_authenticateControl.verifyAndReturnCurrentUser(
                                                                  strPassword);
            assertNull("User information should be null", objVerifyResult[0]);
            assertEquals("Return value is incorrect", LoginConstants.LOGIN_INCORRECT_PASSWORD_OBJ,
                                                      objVerifyResult[1]);
            // test for enabled login with correct login and password
            // since login enabled should have no effect on verification of credentials
            m_transaction.begin();
            try
            {
               m_userControl.updateEnable(Integer.toString(user.getId()), false);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }  
   
            strLoginName = "test_login_name";
            strPassword  = "test_password";
            objVerifyResult = m_authenticateControl.verifyAndReturnCurrentUser(
                                 SecureDatabaseTestUtils.DEFAULT_USER_PASSWORD);
            assertNotNull("User information should not be null", objVerifyResult[0]);
            assertEquals("Return value is incorrect", LoginConstants.LOGIN_OK_OBJ,
                                                     objVerifyResult[1]);
         }
         finally
         {
            if ((user != null) && (user.getId() != DataObject.NEW_ID))
            {
               m_transaction.begin();
               // delete inserted records
               try
               {
                  // delete user
                  m_userControl.delete(user.getId());
                  m_transaction.commit();
               }
               catch (Throwable thr)
               {
                  m_transaction.rollback();
                  throw new Exception(thr);
               }
            }
         }
      }
   
      /**
       * Test for verifying and returning of guest user
       * 
       *  @throws Exception - an error has occurred
       */
      public void testVerifyAndReturnGuestUser(
      ) throws Exception 
      {
         User     user           = null;
         Object[] objVerifyResult = {null, null};
         String   strLoginName    = "test_login_name";
         String   strPassword     = "test_password";
   
         try
         {
            m_transaction.begin();
            try
            {
               // create no guest user
               user = new User(DataObject.NEW_ID,
                                CallContext.getInstance().getCurrentDomainId(),
                                "test_first_name",
                                "test_last_name",
                                "test_phone",
                                "test_fax",
                                "test_address",
                                "test_email",
                                strLoginName,
                                strPassword,
                                true,
                                true,
                                true,
                                true,
                                null,
                                null, true
                               );
               user = m_userControl.create(user, "");
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // test for correct login should result in disabled access 
            objVerifyResult = m_authenticateControl.verifyAndReturnGuestUser(
                                                                  strLoginName);
            assertNotNull("User information cannot be null", objVerifyResult[0]);
            assertEquals("Return value is incorrect", LoginConstants.LOGIN_OK_OBJ,
                                                      objVerifyResult[1]);
            // test for incorrect login
            strLoginName = "test_login_name_wrong";
            objVerifyResult = m_authenticateControl.verifyAndReturnGuestUser(
                                                                  strLoginName);
            assertNull("User information should be null", objVerifyResult[0]);
            assertEquals("Return value is incorrect", LoginConstants.LOGIN_NAME_NOT_VALID_OBJ,
                                                      objVerifyResult[1]);
            // test for enabled login with correct login and password
            m_transaction.begin();
            try
            {
               m_userControl.updateEnable(Integer.toString(user.getId()), false);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }  
   
            strLoginName = "test_login_name";
            strPassword  = "test_password";
            objVerifyResult = m_authenticateControl.verifyAndReturnUser(strLoginName,
                                                                        strPassword);
            assertNotNull("User information should not be null", objVerifyResult[0]);
            assertEquals("Return value is incorrect", LoginConstants.LOGIN_OK_OBJ,
                                                      objVerifyResult[1]);
         }
         finally
         {
            if ((user != null) && (user.getId() != DataObject.NEW_ID))
            {
               m_transaction.begin();
               // delete inserted records
               try
               {
                  // delete user
                  m_userControl.delete(user.getId());
                  m_transaction.commit();
               }
               catch (Throwable thr)
               {
                  m_transaction.rollback();
                  throw new Exception(thr);
               }
            }
         }
      }
   
      /**
       * Test for verifying and returning of guest user
       * 
       *  @throws Exception - an error has occurred
       */
      public void testVerifyAndReturnGuestUserWithNoGuest(
      ) throws Exception 
      {
         User     user           = null;
         Object[] objVerifyResult = {null, null};
         String   strLoginName    = "test_login_name";
         String   strPassword     = "test_password";
   
         try
         {
            m_transaction.begin();
            try
            {
               // create no guest user
               user = new User(DataObject.NEW_ID,
                                CallContext.getInstance().getCurrentDomainId(),
                                "test_first_name",
                                "test_last_name",
                                "test_phone",
                                "test_fax",
                                "test_address",
                                "test_email",
                                strLoginName,
                                strPassword,
                                true,
                                false,
                                true,
                                true,
                                null,
                                null, true
                               );
               user = m_userControl.create(user, "");
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // test for correct login should result in disabled access 
            objVerifyResult = m_authenticateControl.verifyAndReturnGuestUser(
                                                                  strLoginName);
            assertNull("User information should be null", objVerifyResult[0]);
            assertEquals("Return value is incorrect", LoginConstants.LOGIN_GUEST_NOT_ENABLED_OBJ,
                                                      objVerifyResult[1]);
            // test for incorrect login
            strLoginName = "test_login_name_wrong";
            objVerifyResult = m_authenticateControl.verifyAndReturnGuestUser(
                                                                  strLoginName);
            assertNull("User information should be null", objVerifyResult[0]);
            assertEquals("Return value is incorrect", LoginConstants.LOGIN_NAME_NOT_VALID_OBJ,
                                                      objVerifyResult[1]);
            // test for enabled login with correct login and password
            m_transaction.begin();
            try
            {
               m_userControl.updateEnable(Integer.toString(user.getId()), false);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }  
   
            strLoginName = "test_login_name";
            strPassword  = "test_password";
            objVerifyResult = m_authenticateControl.verifyAndReturnUser(strLoginName,
                                                                        strPassword);
            assertNotNull("User information should not be null", objVerifyResult[0]);
            assertEquals("Return value is incorrect", LoginConstants.LOGIN_OK_OBJ,
                                                      objVerifyResult[1]);
         }
         finally
         {
            if ((user != null) && (user.getId() != DataObject.NEW_ID))
            {
               m_transaction.begin();
               // delete inserted records
               try
               {
                  // delete user
                  m_userControl.delete(user.getId());
                  m_transaction.commit();
               }
               catch (Throwable thr)
               {
                  m_transaction.rollback();
                  throw new Exception(thr);
               }
            }
         }
      }
   
      /**
       * Test for verifying and returning of guest user, when we know just user ID
       * 
       *  @throws Exception - an error has occurred
       */
      public void testVerifyAndReturnGuestUserByID(
      ) throws Exception 
      {
         User     user           = null;
         Object[] objVerifyResult = {null, null};
         String   strLoginName    = "test_login_name";
         String   strPassword     = "test_password";
         int      iUserID         = DataObject.NEW_ID;
   
         try
         {
            m_transaction.begin();
            try
            {
               // create no guest user
               user = new User(DataObject.NEW_ID,
                                CallContext.getInstance().getCurrentDomainId(),
                                "test_first_name",
                                "test_last_name",
                                "test_phone",
                                "test_fax",
                                "test_address",
                                "test_email",
                                strLoginName,
                                strPassword,
                                true,
                                true,
                                true,
                                true,
                                null,
                                null, true
                               );
               user = m_userControl.create(user, "");
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            iUserID = user.getId();
            // test for correct user ID should result in disabled access 
            objVerifyResult = m_authenticateControl.verifyAndReturnGuestUser(iUserID);
            assertNotNull("User information cannot be null", objVerifyResult[0]);
            assertEquals("Return value is incorrect", LoginConstants.LOGIN_OK_OBJ,
                                                      objVerifyResult[1]);
            // test for incorrect user ID
            iUserID = DataObject.NEW_ID;
            objVerifyResult = m_authenticateControl.verifyAndReturnGuestUser(iUserID);
            assertNull("User information should be null", objVerifyResult[0]);
            assertEquals("Return value is incorrect", LoginConstants.LOGIN_GUEST_NOT_ENABLED_OBJ,
                                                      objVerifyResult[1]);
            // test for enabled login with correct login and password
            m_transaction.begin();
            try
            {
               m_userControl.updateEnable(Integer.toString(user.getId()), false);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }  
   
            strLoginName = "test_login_name";
            strPassword  = "test_password";
            objVerifyResult = m_authenticateControl.verifyAndReturnUser(strLoginName,
                                                                        strPassword);
            assertNotNull("User information should not be null", objVerifyResult[0]);
            assertEquals("Return value is incorrect", LoginConstants.LOGIN_OK_OBJ,
                                                      objVerifyResult[1]);
         }
         finally
         {
            if ((user != null) && (user.getId() != DataObject.NEW_ID))
            {
               m_transaction.begin();
               // delete inserted records
               try
               {
                  // delete user
                  m_userControl.delete(user.getId());
                  m_transaction.commit();
               }
               catch (Throwable thr)
               {
                  m_transaction.rollback();
                  throw new Exception(thr);
               }
            }
         }
      }
   
      /**
       * Test for verifying and returning of guest user knowing just user ID
       * 
       *  @throws Exception - an error has occurred
       */
      public void testVerifyAndReturnGuestUserByIDWithNoGuest(
      ) throws Exception 
      {
         User     user           = null;
         Object[] objVerifyResult = {null, null};
         String   strLoginName    = "test_login_name";
         String   strPassword     = "test_password";
         int      iUserID         = DataObject.NEW_ID;
   
         try
         {
            m_transaction.begin();
            try
            {
               // create no guest user
               user = new User(DataObject.NEW_ID,
                                CallContext.getInstance().getCurrentDomainId(),
                                "test_first_name",
                                "test_last_name",
                                "test_phone",
                                "test_fax",
                                "test_address",
                                "test_email",
                                strLoginName,
                                strPassword,
                                true,
                                false,
                                true,
                                true,
                                null,
                                null, true
                               );
               user = m_userControl.create(user, "");
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            iUserID = user.getId();
            // test for correct user ID should result in disabled access 
            objVerifyResult = m_authenticateControl.verifyAndReturnGuestUser(iUserID);
            assertNull("User information should be null", objVerifyResult[0]);
            assertEquals("Return value is incorrect", LoginConstants.LOGIN_GUEST_NOT_ENABLED_OBJ,
                                                      objVerifyResult[1]);
            // test for incorrect user ID
            iUserID = DataObject.NEW_ID;
            objVerifyResult = m_authenticateControl.verifyAndReturnGuestUser(iUserID);
            assertNull("User information should be null", objVerifyResult[0]);
            assertEquals("Return value is incorrect", LoginConstants.LOGIN_GUEST_NOT_ENABLED_OBJ,
                                                      objVerifyResult[1]);
            // test for enabled login with correct login and password
            m_transaction.begin();
            try
            {
               m_userControl.updateEnable(Integer.toString(user.getId()), false);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }  
   
            strLoginName = "test_login_name";
            strPassword  = "test_password";
            objVerifyResult = m_authenticateControl.verifyAndReturnUser(strLoginName,
                                                                        strPassword);
            assertNotNull("User information should not be null", objVerifyResult[0]);
            assertEquals("Return value is incorrect", LoginConstants.LOGIN_OK_OBJ,
                                                      objVerifyResult[1]);
         }
         finally
         {
            if ((user != null) && (user.getId() != DataObject.NEW_ID))
            {
               m_transaction.begin();
               // delete inserted records
               try
               {
                  // delete user
                  m_userControl.delete(user.getId());
                  m_transaction.commit();
               }
               catch (Throwable thr)
               {
                  m_transaction.rollback();
                  throw new Exception(thr);
               }
            }
         }
      }
   }
}
