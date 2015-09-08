/*
 * Copyright (c) 2003 - 2009 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: SessionControllerTest.java,v 1.31 2009/09/20 05:32:57 bastafidli Exp $
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

import java.sql.PreparedStatement;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.opensubsystems.core.application.Application;
import org.opensubsystems.security.application.SecureApplicationTestSetup;
import org.opensubsystems.core.application.ModuleManager;
import org.opensubsystems.core.application.impl.ApplicationImpl;
import org.opensubsystems.core.application.impl.InstanceInfoImpl;
import org.opensubsystems.core.data.DataDescriptorManager;
import org.opensubsystems.core.data.DataObject;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.logic.ControllerManager;
import org.opensubsystems.core.persist.DataFactoryManager;
import org.opensubsystems.core.persist.db.DatabaseTestSuite;
import org.opensubsystems.core.util.CallContext;
import org.opensubsystems.core.util.Tests;
import org.opensubsystems.core.util.jdbc.DatabaseUtils;
import org.opensubsystems.patterns.listdata.data.SimpleRule;
import org.opensubsystems.security.application.SecurityBackendModule;
import org.opensubsystems.security.data.AccessRight;
import org.opensubsystems.security.data.Domain;
import org.opensubsystems.security.data.ExternalSession;
import org.opensubsystems.security.data.InternalSession;
import org.opensubsystems.security.data.InternalSessionDataDescriptor;
import org.opensubsystems.security.data.Role;
import org.opensubsystems.security.data.SessionView;
import org.opensubsystems.security.data.User;
import org.opensubsystems.security.patterns.listdata.logic.impl.SecureListControllerTest;
import org.opensubsystems.security.persist.AccessRightFactory;
import org.opensubsystems.security.persist.DomainFactory;
import org.opensubsystems.security.persist.ExternalSessionFactory;
import org.opensubsystems.security.persist.InternalSessionFactory;
import org.opensubsystems.security.persist.RoleFactory;
import org.opensubsystems.security.persist.UserFactory;
import org.opensubsystems.security.persist.db.AccessRightDatabaseFactory;
import org.opensubsystems.security.persist.db.DomainDatabaseFactory;
import org.opensubsystems.security.persist.db.ExternalSessionDatabaseFactory;
import org.opensubsystems.security.persist.db.InternalSessionDatabaseFactory;
import org.opensubsystems.security.persist.db.InternalSessionListDatabaseTestUtils;
import org.opensubsystems.security.persist.db.RoleDatabaseFactory;
import org.opensubsystems.security.persist.db.SessionDatabaseSchema;
import org.opensubsystems.security.persist.db.UserDatabaseSchema;
import org.opensubsystems.security.util.ActionConstants;
import org.opensubsystems.security.util.LoginConstants;
import org.opensubsystems.security.utils.TestUserDatabaseFactoryUtils;

/**
 * Test for SessionController interface implementation class. 
 * 
 * @version $Id: SessionControllerTest.java,v 1.31 2009/09/20 05:32:57 bastafidli Exp $
 * @author Julo Legeny
 * @code.reviewer Miro Halas
 * @code.reviewed Initial revision
 */
public final class SessionControllerTest extends Tests
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private SessionControllerTest(
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
      TestSuite suite = new DatabaseTestSuite("SessionControllerTest");
      suite.addTestSuite(SessionControllerTestInternal.class);
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
   public static class SessionControllerTestInternal extends SecureListControllerTest 
   {
      // Cached values ////////////////////////////////////////////////////////////
   
      /**
       * Factory to manage domains.
       */
      protected DomainDatabaseFactory m_domainFactory;

      /**
       * Factory to manage users.
       */
      protected UserFactory m_userFactory;
   
      /**
       * Factory to manage internal sessions.
       */
      protected InternalSessionDatabaseFactory m_sessionFactory;
   
      /**
       * Factory to manage external sessions.
       */
      protected ExternalSessionDatabaseFactory m_externalSessionFactory;


      /**
       * Factory to manage roles.
       */
      protected RoleDatabaseFactory m_roleFactory;

      /**
       * Factory to manage access rights.
       */
      protected AccessRightDatabaseFactory m_accessRightFactory;

      /**
       * Controller to manage sessions.
       */
      protected SessionController m_sessionControl;
   
      /**
       * Controller used to manipulate users.
       */
      protected UserController m_userControl;

      /**
       * Controller to perform authorization.
       */
      protected AuthorizationController m_authorizeControl;

      /**
       * Factory utilities to manage users.
       */
      protected TestUserDatabaseFactoryUtils m_userFactoryUtils;
   
      /**
       * Data type assigned to internal session data types.
       */
      protected int m_iIntSessionDataType;
   
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
      public SessionControllerTestInternal(
         String strTestName
      ) throws Exception
      {
         super(strTestName, new InternalSessionListDatabaseTestUtils());

         m_domainFactory = (DomainDatabaseFactory)DataFactoryManager.getInstance(
                                         DomainFactory.class);
         m_userFactory = (UserFactory)DataFactoryManager.getInstance(
                                         UserFactory.class);
         m_sessionFactory = (InternalSessionDatabaseFactory)DataFactoryManager.getInstance(
                                         InternalSessionFactory.class);      
         m_externalSessionFactory = (ExternalSessionDatabaseFactory)DataFactoryManager.getInstance(
                                         ExternalSessionFactory.class);
         m_roleFactory = (RoleDatabaseFactory)DataFactoryManager.getInstance(
                                         RoleFactory.class);
         m_accessRightFactory = (AccessRightDatabaseFactory)DataFactoryManager.getInstance(
                                         AccessRightFactory.class);
         m_sessionControl = (SessionController)ControllerManager.getInstance(
                                         SessionController.class);
         m_userControl = (UserController)ControllerManager.getInstance(
                                         UserController.class);
         m_authorizeControl = (AuthorizationController)ControllerManager.getInstance(
                                         AuthorizationController.class);
         m_userFactoryUtils = new TestUserDatabaseFactoryUtils();

         InternalSessionDataDescriptor intSessionDescriptor;
         
         intSessionDescriptor = (InternalSessionDataDescriptor)DataDescriptorManager
                                   .getInstance(
                                      InternalSessionDataDescriptor.class);
         
         m_iIntSessionDataType = intSessionDescriptor.getDataType();
      }
   
      // Tests ////////////////////////////////////////////////////////////////////
      
      /**
       * Test for getSessiones method  
       * 
       * @throws Exception - an error has occurred
       */
      public void testGetSessions(
      ) throws Exception
      {
         User            user             = null;
         InternalSession session      = null;
         ExternalSession extSession1  = null;
         ExternalSession extSession2  = null;
         SessionView     sessionView = null;
         Object[]        objReturn         = {null, null};
         List            lstServerSessions = null;
         
         try
         {
            m_transaction.begin();
            try
            {
               // create test user first
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
            
               user = (User)m_userFactory.create(user);
            
               // create test internal session
               session = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            user.getId(),
                                            "test_generated_code",
                                            "test_c_IP",
                                            "test_client_type",
                                            null,
                                            false
                                            );
            
               session = (InternalSession)m_sessionFactory.create(session);
            
               // create 2 server sessions belonging to the internal session
               extSession1 = new ExternalSession(DataObject.NEW_ID,
                                                    CallContext.getInstance().getCurrentDomainId(),
                                                    session.getId(),
                                                    "test_server_session_gen_code_1",
                                                    "test_srv_IP_1",
                                                    null
                                                   );
               extSession1 = (ExternalSession)m_externalSessionFactory.create(extSession1);
            
               extSession2 = new ExternalSession(DataObject.NEW_ID,
                                                    CallContext.getInstance().getCurrentDomainId(),
                                                    session.getId(),
                                                    "test_server_session_gen_code_2",
                                                    "test_srv_IP_2",
                                                    null
                                                   );
               extSession2 = (ExternalSession)m_externalSessionFactory.create(extSession2);
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
         
            // get internal session view with belonging server sessions
            objReturn = m_sessionControl.getSessions(session.getId());
         
            sessionView = (SessionView) objReturn[0];
            lstServerSessions = (List) objReturn[1];
         
            assertNotNull("Returned session view should not be null", sessionView);
            assertNotNull("Returned list of server sessions is null", lstServerSessions);
            assertNotNull("Session view should not be null", sessionView);
            assertEquals("User id of the view is incorrect", 
                         user.getId(), sessionView.getUserId());
            assertEquals("Login name of the view is incorrect", 
                         user.getLoginName(), sessionView.getLoginName());
            assertEquals("First name of the view is incorrect", 
                         user.getFirstName(), sessionView.getFirstName());
            assertEquals("Last name of the view is incorrect", 
                         user.getLastName(), sessionView.getLastName());
            assertEquals("Phone of the view is incorrect", 
                         user.getPhone(), sessionView.getPhone());
            assertEquals("Email of the view is incorrect", 
                         user.getEmail(), sessionView.getEmail());
            assertEquals("Fax of the view is incorrect", 
                         user.getFax(), sessionView.getFax());         
            assertEquals("Login status of the view is incorrect", 
                         user.isLoginEnabled(), sessionView.isLoginEnabled());
            assertEquals("Superuser status of the view is incorrect", 
                         user.isSuperUser(), sessionView.isSuperUser());
            assertEquals("Origin of the view is incorrect", 
                         session.getClientIP(), sessionView.getClientIP());
            assertEquals("Client type of the view is incorrect", 
                         session.getClientType(), sessionView.getClientType());
            assertEquals("Internal session of the view is incorrect", 
                         session.getInternalSession(), sessionView.getInternalSession());
            assertEquals("Id of the view is incorrect", 
                         session.getId(), sessionView.getId());
         
            assertEquals("Number of external sessions is incorrect", 
                         2, lstServerSessions.size());
         
            assertTrue("External session is not the same", 
                       extSession1.equals(lstServerSessions.get(0))
                       || extSession1.equals(lstServerSessions.get(1)));
            assertTrue("External session is not the same", 
                       extSession2.equals(lstServerSessions.get(0))
                       || extSession2.equals(lstServerSessions.get(1)));
         }
         finally
         {
            m_transaction.begin();
            try
            {
               if ((extSession1 != null) && (extSession1.getId() != DataObject.NEW_ID))
               {
                  m_externalSessionFactory.delete(
                     extSession1.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((extSession2 != null) && (extSession2.getId() != DataObject.NEW_ID))
               {
                  m_externalSessionFactory.delete(
                     extSession2.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((session != null) && (session.getId() != DataObject.NEW_ID))
               {
                  m_sessionFactory.delete(
                     session.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((user != null) && (user.getId() != DataObject.NEW_ID))
               {
                  m_userFactoryUtils.deleteUserCascadeManual(user.getId());
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
       * Test of the login method  
       * 
       * @throws Exception - an error has occurred
       */
      public void testLogin(
      ) throws Exception
      {
         User     user           = null;
         Object[] loginInfo       = {null, null, null};
         String   strLoginName    = "test_login_name";
         String   strPassword     = "test_password";
         String   strGenSessionCode = "";
         
         try
         {
            m_transaction.begin();
            try
            {
               // create test user first
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
   
            m_transaction.begin();
            try
            {
               strLoginName = "test_login_name_wrong";
               strPassword = "test_password";
               // try to login user with incorrect login
               loginInfo = m_sessionControl.login(strLoginName, strPassword,
                                                  "test_server_session",
                                                  "test_c_IP",
                                                  "test_client_type",
                                                  new InstanceInfoImpl("test_s_IP"));
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            
            assertNull("User information should be null", loginInfo[0]);
            assertEquals("Return value is incorrect", LoginConstants.LOGIN_NAME_NOT_VALID,
                                                      Integer.parseInt(loginInfo[1].toString()));
            assertNull("Generated code should be null", loginInfo[2]);
   
            m_transaction.begin();
            try
            {
               strLoginName = "test_login_name";
               strPassword = "test_password_wrong";
               // try to login user with incorrect password
               loginInfo = m_sessionControl.login(strLoginName, strPassword,
                                                  "test_server_session",
                                                  "test_c_IP",
                                                  "test_client_type",
                                                  new InstanceInfoImpl("test_s_IP"));
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            assertNull("User information should be null", loginInfo[0]);
            assertEquals("Return value is incorrect", LoginConstants.LOGIN_INCORRECT_PASSWORD,
                                                      Integer.parseInt(loginInfo[1].toString()));
            assertNull("Generated code should be null", loginInfo[2]);
   
            m_transaction.begin();
            try
            {
               strLoginName = "test_login_name";
               strPassword  = "test_password";
               // try to login user with correct login and password
               loginInfo = m_sessionControl.login(strLoginName, strPassword,
                                                  "test_server_session",
                                                  "test_c_IP",
                                                  "test_client_type",
                                                  new InstanceInfoImpl("test_s_IP"));
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            assertNotNull("User information cannot be null", loginInfo[0]);
            assertEquals("Return value is incorrect", LoginConstants.LOGIN_OK,
                                                      Integer.parseInt(loginInfo[1].toString()));
            assertNotNull("Generated code code cannot be null", loginInfo[2]);
            
            strGenSessionCode = loginInfo[2].toString();
   
            // Now check if the generated session has a good guest access
            InternalSession generatedSession;
            
            generatedSession = m_sessionFactory.get(strGenSessionCode);
            assertNotNull("Generated session cannot be null.", generatedSession);
            assertFalse("Guest access for generated session shouldn't be set to guest", 
                       generatedSession.isGuestAccess());
   
            try
            {
               // disable login to the user
               m_transaction.begin();
               m_userFactory.updateEnable(new int[] {user.getId()}, false, SimpleRule.ALL_DATA);
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
               strLoginName = "test_login_name";
               strPassword  = "test_password";
               // try to login disabled user with correct login and password
               loginInfo = m_sessionControl.login(strLoginName, strPassword,
                                                  "test_server_session",
                                                  "test_c_IP",
                                                  "test_client_type",
                                                  new InstanceInfoImpl("test_s_IP"));
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            assertNull("User information should be null", loginInfo[0]);
            assertEquals("Return value is incorrect", LoginConstants.LOGIN_NOT_ENABLED,
                                                      Integer.parseInt(loginInfo[1].toString()));
            assertNull("Generated code should be null", loginInfo[2]);
         }
         finally
         {
            m_transaction.begin();
            try
            {
               if ((user != null) && (user.getId() != DataObject.NEW_ID))
               {
                  m_userFactoryUtils.deleteUserCascadeManual(user.getId());
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
       * Test for loginGuest method  
       * 
       * @throws Exception - an error has occurred
       */
      public void testLoginGuest(
      ) throws Exception
      {
         User     user           = null;
         Object[] loginInfo       = {null, null, null};
         String   strLoginName    = "test_login_name";
         String   strPassword     = "test_password";
         String   strGenSessionCode = "";
         
         try
         {
            m_transaction.begin();
            try
            {
               // create test user first
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
   
            m_transaction.begin();
            try
            {
               strLoginName = "test_login_name_wrong";
               // try to login user with incorrect login
               loginInfo = m_sessionControl.loginGuest(strLoginName,
                                                       "test_server_session",
                                                       "test_c_IP",
                                                       "test_client_type",
                                                       new InstanceInfoImpl("test_s_IP"));
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            assertNull("User information should be null", loginInfo[0]);
            assertEquals("Return value is incorrect", LoginConstants.LOGIN_NAME_NOT_VALID,
                                                      Integer.parseInt(loginInfo[1].toString()));
            assertNull("Generated code should be null", loginInfo[2]);
   
            m_transaction.begin();
            try
            {
               strLoginName = "test_login_name";
               // try to login user with correct login and password
               loginInfo = m_sessionControl.loginGuest(strLoginName,
                                                       "test_server_session",
                                                       "test_c_IP",
                                                       "test_client_type",
                                                       new InstanceInfoImpl("test_s_IP"));
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            assertNotNull("User information cannot be null", loginInfo[0]);
            assertEquals("Return value is incorrect", LoginConstants.LOGIN_OK,
                                                      Integer.parseInt(loginInfo[1].toString()));
            assertNotNull("Generated code code cannot be null", loginInfo[2]);
            strGenSessionCode = loginInfo[2].toString();
            
            // Now check if the generated session has a good guest access
            InternalSession generatedSession;
            
            generatedSession = m_sessionFactory.get(strGenSessionCode);
            assertNotNull("Generated session cannot be null.", generatedSession);
            assertTrue("Guest access for generated session is not set to guest", 
                       generatedSession.isGuestAccess());
            
            try
            {
               // disable login to the user
               m_transaction.begin();
               m_userFactory.updateEnable(new int[] {user.getId()}, false, SimpleRule.ALL_DATA);
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
               strLoginName = "test_login_name";
               // try to login disabled user with correct login and password
               loginInfo = m_sessionControl.loginGuest(strLoginName,
                                                       "test_server_session",
                                                       "test_c_IP",
                                                       "test_client_type",
                                                       new InstanceInfoImpl("test_s_IP"));
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            assertNull("User information should be null", loginInfo[0]);
            assertEquals("Return value is incorrect", LoginConstants.LOGIN_NOT_ENABLED,
                                                      Integer.parseInt(loginInfo[1].toString()));
            assertNull("Generated code should be null", loginInfo[2]);
   
         }
         finally
         {
            m_transaction.begin();
            try
            {
               if ((user != null) && (user.getId() != DataObject.NEW_ID))
               {
                  m_userFactoryUtils.deleteUserCascadeManual(user.getId());
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
       * Test for loginGuest method without guest enabled  
       * 
       * @throws Exception - an error has occurred
       */
      public void testLoginGuestWithNoGuest(
      ) throws Exception
      {
         User     user           = null;
         Object[] loginInfo       = {null, null, null};
         String   strLoginName    = "test_login_name";
         String   strPassword     = "test_password";
         
         try
         {
            m_transaction.begin();
            try
            {
               // create test user first
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
   
            m_transaction.begin();
            try
            {
               strLoginName = "test_login_name_wrong";
               // try to login user with incorrect login
               loginInfo = m_sessionControl.loginGuest(strLoginName,
                                                       "test_server_session",
                                                       "test_c_IP",
                                                       "test_client_type",
                                                       new InstanceInfoImpl("test_s_IP"));
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            assertNull("User information should be null", loginInfo[0]);
            assertEquals("Return value is incorrect", LoginConstants.LOGIN_NAME_NOT_VALID,
                                                      Integer.parseInt(loginInfo[1].toString()));
            assertNull("Generated code should be null", loginInfo[2]);
   
            m_transaction.begin();
            try
            {
               strLoginName = "test_login_name";
               // try to login user with correct login and password
               loginInfo = m_sessionControl.loginGuest(strLoginName,
                                                       "test_server_session",
                                                       "test_c_IP",
                                                       "test_client_type",
                                                       new InstanceInfoImpl("test_s_IP"));
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            assertNull("User information should be null", loginInfo[0]);
            assertEquals("Return value is incorrect", LoginConstants.LOGIN_GUEST_NOT_ENABLED_OBJ,
                                                      loginInfo[1]);
            assertNull("Generated code code should be null", loginInfo[2]);
   
            try
            {
               // disable login to the user
               m_transaction.begin();
               m_userFactory.updateEnable(new int[] {user.getId()}, false, SimpleRule.ALL_DATA);
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
               strLoginName = "test_login_name";
               // try to login disabled user with correct login and password
               loginInfo = m_sessionControl.loginGuest(strLoginName,
                                                       "test_server_session",
                                                       "test_c_IP",
                                                       "test_client_type",
                                                       new InstanceInfoImpl("test_s_IP"));
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            assertNull("User information should be null", loginInfo[0]);
            assertEquals("Return value is incorrect", LoginConstants.LOGIN_GUEST_NOT_ENABLED_OBJ,
                                                      loginInfo[1]);
            assertNull("Generated code should be null", loginInfo[2]);
         }
         finally
         {
            m_transaction.begin();
            try
            {
               if ((user != null) && (user.getId() != DataObject.NEW_ID))
               {
                  m_userFactoryUtils.deleteUserCascadeManual(user.getId());
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
       * Test for logout method  
       * 
       * @throws Exception - an error has occurred
       */
      public void testLogout(
      ) throws Exception
      {
         InternalSession session      = null;
         InternalSession testSession  = null;
         ExternalSession extSession1  = null;
         ExternalSession extSession2  = null;
         ExternalSession testExtSession  = null;
   
         try
         {
            m_transaction.begin();
            try
            {
               // create test internal session
               session = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            CallContext.getInstance().getCurrentUserId(),
                                            "test_generated_code",
                                            "test_c_IP",
                                            "test_client_type",
                                            null,
                                            false
                                            );
            
               session = (InternalSession) m_sessionFactory.create(session);
            
               // create 2 external sessions belonging to the internal session
               extSession1 = new ExternalSession(DataObject.NEW_ID,
                                                    CallContext.getInstance().getCurrentDomainId(), 
                                                    session.getId(),
                                                    "test_server_session_gen_code_1",
                                                    "test_srv_IP_1",
                                                    null
                                                   );
               extSession1 = (ExternalSession)m_externalSessionFactory.create(extSession1);
            
               extSession2 = new ExternalSession(DataObject.NEW_ID,
                                                    CallContext.getInstance().getCurrentDomainId(),
                                                    session.getId(),
                                                    "test_server_session_gen_code_2",
                                                    "test_srv_IP_2",
                                                    null
                                                   );
               extSession2 = (ExternalSession)m_externalSessionFactory.create(extSession2);
   
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
               m_sessionControl.logout(session.getInternalSession());
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            testSession = m_sessionFactory.get(session.getInternalSession());
            assertNull("Internal session should have been removed.", testSession);
            session = null;
            testExtSession = (ExternalSession)m_externalSessionFactory.get(
                                extSession1.getId(),
                                CallContext.getInstance().getCurrentDomainId());
            assertNull("External session should have been removed.", testExtSession);
            extSession1 = null;
            testExtSession = (ExternalSession)m_externalSessionFactory.get(
                                extSession2.getId(),
                                CallContext.getInstance().getCurrentDomainId());
            assertNull("External session should have been removed.", testExtSession);
            extSession2 = null;
         }
         finally
         {
            m_transaction.begin();
            try
            {
               if ((extSession1 != null) && (extSession1.getId() != DataObject.NEW_ID))
               {
                  m_externalSessionFactory.delete(
                     extSession1.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((extSession2 != null) && (extSession2.getId() != DataObject.NEW_ID))
               {
                  m_externalSessionFactory.delete(
                     extSession2.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((session != null) && (session.getId() != DataObject.NEW_ID))
               {
                  m_sessionFactory.delete(
                     session.getId(),
                     CallContext.getInstance().getCurrentDomainId());
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
       * Test for attach method.  
       * 
       * @throws Exception - an error has occurred
       */
      public void testAttach(
      ) throws Exception
      {
         InternalSession session     = null;
         ExternalSession testExtSession1 = null;
         ExternalSession testExtSession2 = null;
         List            lstSessions;
         Object[]        attachedUserInfo;            
         
         try
         {
            m_transaction.begin();
            try
            {
               // create test internal session
               session = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            CallContext.getInstance().getCurrentUserId(),
                                            "test_generated_code",
                                            "test_c_IP",
                                            "test_client_type",
                                            null,
                                            false
                                            );
            
               session = (InternalSession)m_sessionFactory.create(session);
   
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
   
               // try to attach external session 1 to the existing internal session
               attachedUserInfo = m_sessionControl.attach(
                                     session.getInternalSession(),
                                     "test_server_session_gen_code_1",
                                     "test_c_IP", 
                                     new InstanceInfoImpl("test_srv_IP_1"));
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            assertNotNull("Result of the external session attach should not be null", 
                          attachedUserInfo);
            assertNotNull("Result of the external session attach should be user info", 
                          attachedUserInfo[0]);
            assertEquals("Attach returned incorrect attach code", 
                         LoginConstants.LOGIN_USER_EXISTS_OBJ, attachedUserInfo[1]);
   
            lstSessions = m_externalSessionFactory.getAll(session.getId());
            assertNotNull("External session should have been created.", lstSessions);
            assertEquals("External session should have been created.", 1, lstSessions.size());
            testExtSession1 = (ExternalSession)lstSessions.get(0);
            assertEquals("Session code is not correct.", 
                         testExtSession1.getExternalSession(), 
                         "test_server_session_gen_code_1");
            assertEquals("Application is not correct.", 
                         testExtSession1.getServer(), 
                         "test_srv_IP_1");
   
            m_transaction.begin();
            try
            {
               // try to attach external session to the existing internal session but from
               // a different client address which should not allow us to attach since
               // the client address should always match
               attachedUserInfo = m_sessionControl.attach(
                                     session.getInternalSession(),
                                     "test_server_session_gen_code_1",
                                     "test_c_IP1", 
                                     new InstanceInfoImpl("test_srv_IP_1"));
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            lstSessions = m_externalSessionFactory.getAll(session.getId());
            assertNotNull("External session should have been created.", lstSessions);
            assertEquals("External session should have been created.", 1, lstSessions.size());
            testExtSession1 = (ExternalSession)lstSessions.get(0);
            assertEquals("Session code is not correct.", 
                         testExtSession1.getExternalSession(), 
                         "test_server_session_gen_code_1");
            assertEquals("Application is not correct.", 
                         testExtSession1.getServer(), 
                         "test_srv_IP_1");
            
            int iSessionId = session.getId();
               
            m_transaction.begin();
            try
            {
               if ((testExtSession1 != null) && (testExtSession1.getId() != DataObject.NEW_ID))
               {
                  m_externalSessionFactory.delete(
                     testExtSession1.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((testExtSession2 != null) && (testExtSession2.getId() != DataObject.NEW_ID))
               {
                  m_externalSessionFactory.delete(
                     testExtSession2.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((session != null) && (session.getId() != DataObject.NEW_ID))
               {
                  m_sessionFactory.delete(
                     session.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               m_transaction.commit();
               testExtSession1 = null;
               testExtSession2 = null;
               session = null;
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            m_transaction.begin();
            try
            {
               // try to attach external session to the non existing internal session
               attachedUserInfo = m_sessionControl.attach(
                                     "test_BFsession_not_existing",
                                     "test_server_session_gen_code_1",
                                     "test_c_IP", 
                                     new InstanceInfoImpl("test_srv_IP_1"));
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            assertNotNull("Result of the external session attach should not be null", 
                          attachedUserInfo);
            assertNull("Result of the external session attach should NOT be user info", 
                       attachedUserInfo[0]);
            assertEquals("Attach returned incorrect attach code", 
                         LoginConstants.LOGIN_INVALID_OBJ, attachedUserInfo[1]);
   
            lstSessions = m_externalSessionFactory.getAll(iSessionId);
            assertNull("External session should not have been created.", lstSessions);
         }
         finally
         {
            m_transaction.begin();
            try
            {
               if ((testExtSession1 != null) && (testExtSession1.getId() != DataObject.NEW_ID))
               {
                  m_externalSessionFactory.delete(
                     testExtSession1.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((testExtSession2 != null) && (testExtSession2.getId() != DataObject.NEW_ID))
               {
                  m_externalSessionFactory.delete(
                     testExtSession2.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((session != null) && (session.getId() != DataObject.NEW_ID))
               {
                  m_sessionFactory.delete(
                     session.getId(),
                     CallContext.getInstance().getCurrentDomainId());
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
       * Test for detach method  
       * 
       * @throws Exception - an error has occurred
       */
      public void testDetach(
      ) throws Exception 
      {
         InternalSession session      = null;
         InternalSession testSession  = null;
         ExternalSession extSession1  = null;
         ExternalSession extSession2  = null;
         ExternalSession testExtSession = null;
   
         try
         {
            m_transaction.begin();
            try
            {
               // create test internal session
               session = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            CallContext.getInstance().getCurrentUserId(),
                                            "test_generated_code",
                                            "test_c_IP",
                                            "test_client_type",
                                            null,
                                            false
                                            );
            
               session = (InternalSession)m_sessionFactory.create(session);
            
               // create 2 external sessions belonging to the internal session
               extSession1 = new ExternalSession(DataObject.NEW_ID,
                                                    CallContext.getInstance().getCurrentDomainId(),
                                                    session.getId(),
                                                    "test_server_session_gen_code_1",
                                                    "test_srv_IP_1",
                                                    null
                                                   );
               extSession1 = (ExternalSession)m_externalSessionFactory.create(extSession1);
            
               extSession2 = new ExternalSession(DataObject.NEW_ID,
                                                    CallContext.getInstance().getCurrentDomainId(),
                                                    session.getId(),
                                                    "test_server_session_gen_code_2",
                                                    "test_srv_IP_2",
                                                    null
                                                   );
               extSession2 = (ExternalSession)m_externalSessionFactory.create(extSession2);
   
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
               // try to detach external session 1 (internal session will not be detached yet)
               m_sessionControl.detach(session.getInternalSession(),
                                       extSession1.getExternalSession(),
                                       new InstanceInfoImpl(extSession1.getServer()));
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            testSession = (InternalSession)m_sessionFactory.get(
                             session.getId(),
                             CallContext.getInstance().getCurrentDomainId());
            assertNotNull("Internal session should not be removed", testSession);
            assertEquals("Internal session is not the same", session, testSession);
   
            testExtSession = (ExternalSession)m_externalSessionFactory.get(
                                extSession1.getId(),
                                CallContext.getInstance().getCurrentDomainId());
            assertNull("External session should be removed", testExtSession);
            extSession1 = null;
   
            testExtSession = (ExternalSession)m_externalSessionFactory.get(
                                extSession2.getId(),
                                CallContext.getInstance().getCurrentDomainId());
            assertNotNull("External session should not be removed", testExtSession);
            assertEquals("External session is not the same", extSession2, testExtSession);
                     
            m_transaction.begin();
            try
            {
               // try to detach external session 2 (internal session will be detached too)
               m_sessionControl.detach(session.getInternalSession(),
                                       extSession2.getExternalSession(),
                                       new InstanceInfoImpl(extSession2.getServer()));
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            testSession = (InternalSession)m_sessionFactory.get(
                             session.getId(),
                             CallContext.getInstance().getCurrentDomainId());
            assertNull("Internal session should be removed", testSession);
            session = null;
            
            testExtSession = (ExternalSession)m_externalSessionFactory.get(
                                extSession2.getId(),
                                CallContext.getInstance().getCurrentDomainId());
            assertNull("External session should be removed", testExtSession);
            extSession2 = null;
         }
         finally
         {
            m_transaction.begin();
            try
            {
               if ((extSession1 != null) && (extSession1.getId() != DataObject.NEW_ID))
               {
                  m_externalSessionFactory.delete(
                     extSession1.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((extSession2 != null) && (extSession2.getId() != DataObject.NEW_ID))
               {
                  m_externalSessionFactory.delete(
                     extSession2.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((session != null) && (session.getId() != DataObject.NEW_ID))
               {
                  m_sessionFactory.delete(
                     session.getId(),
                     CallContext.getInstance().getCurrentDomainId());
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
       * Test for logoutSessions method.  
       * 
       * @throws Exception - an error has occurred
       */
      public void testLogoutSessions(
      ) throws Exception
      {
         InternalSession session1 = null;
         InternalSession session2 = null;
         InternalSession testSession = null;
         int             iLogoutedSessions = 0;
         
         try
         {
            m_transaction.begin();
            try
            {
               // create two internal sessions
               session1 = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            CallContext.getInstance().getCurrentUserId(),
                                            "test_generated_code_1",
                                            "test_c_IP_1",
                                            "test_client_type_1",
                                            null,
                                            false
                                            );
            
               session1 = (InternalSession)m_sessionFactory.create(session1);
            
               session2 = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            CallContext.getInstance().getCurrentUserId(),
                                            "test_generated_code_2",
                                            "test_c_IP_2",
                                            "test_client_type_2",
                                            null,
                                            false
                                            );
            
               session2 = (InternalSession)m_sessionFactory.create(session2);
   
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
               int[] arrIds = new int[] {session1.getId(), session2.getId()};
               
               // try to logout external sessions
               iLogoutedSessions = m_sessionControl.logoutSessions(arrIds, true);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            assertEquals("Number of logout internal sessions is incorrect", 
                         2, iLogoutedSessions);
            
            testSession = (InternalSession)m_sessionFactory.get(
                             session1.getId(),
                             CallContext.getInstance().getCurrentDomainId());
            assertNull("Internal session should be removed", testSession);
            session1 = null;
            testSession = (InternalSession)m_sessionFactory.get(
                             session2.getId(),
                             CallContext.getInstance().getCurrentDomainId());
            assertNull("Internal session should be removed", testSession);
            session2 = null;
         }
         finally
         {
            m_transaction.begin();
            try
            {
               if ((session1 != null) && (session1.getId() != DataObject.NEW_ID))
               {
                  m_sessionFactory.delete(
                     session1.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((session2 != null) && (session2.getId() != DataObject.NEW_ID))
               {
                  m_sessionFactory.delete(
                     session2.getId(),
                     CallContext.getInstance().getCurrentDomainId());
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
       * Test for updateEnable with true parameter. 
       * 
       * @throws Exception - an error has occurred
       */
      public void testUpdateEnableTrue(
      ) throws Exception
      {
         User            user1        = null;
         User            user2        = null;
         User            selectedUser = null;
         InternalSession session1 = null;
         InternalSession session2 = null;
         InternalSession testSession = null;
         int             iUsersEnabled = 0;
   
         try
         {
            m_transaction.begin();
            try
            {
               // create two users
               user1 = new User(DataObject.NEW_ID,
                                 CallContext.getInstance().getCurrentDomainId(),
                                 "test_first_name_1",
                                 "test_last_name_1",
                                 "test_phone_1",
                                 "test_fax_1",
                                 "test_address_1",
                                 "test_email_1",
                                 "test_login_name_1",
                                 "test_password_1",
                                 false,
                                 false,
                                 false,
                                 false,
                                 null,
                                 null, true
                                );
               user1 = (User)m_userFactory.create(user1);
   
               user2 = new User(DataObject.NEW_ID,
                                 CallContext.getInstance().getCurrentDomainId(),
                                 "test_first_name_2",
                                 "test_last_name_2",
                                 "test_phone_2",
                                 "test_fax_2",
                                 "test_address_2",
                                 "test_email_2",
                                 "test_login_name_2",
                                 "test_password_2",
                                 false,
                                 false,
                                 false,
                                 false,
                                 null,
                                 null, true
                                );
               user2 = (User)m_userFactory.create(user2);
   
               // create two internal sessions
               session1 = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            user1.getId(),
                                            "test_generated_code_1",
                                            "test_c_IP_1",
                                            "test_client_type_1",
                                            null,
                                            false
                                            );
            
               session1 = (InternalSession)m_sessionFactory.create(session1);
            
               session2 = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            user2.getId(),
                                            "test_generated_code_2",
                                            "test_c_IP_2",
                                            "test_client_type_2",
                                            null,
                                            false
                                            );
            
               session2 = (InternalSession)m_sessionFactory.create(session2);
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            Thread.sleep(1000);
            
            m_transaction.begin();
            try
            {
               int[] arrIds = new int[] {session1.getId(), session2.getId()};
               
               // try to enable selected users
               iUsersEnabled = m_sessionControl.updateEnable(arrIds, true);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            assertEquals("Number of enabled users is incorrect", 2, iUsersEnabled);
   
            selectedUser = (User)m_userFactory.get(
                                    user1.getId(),
                                    CallContext.getInstance().getCurrentDomainId());
            assertTrue("User should be enabled.", selectedUser.isLoginEnabled());
            assertTrue("Modified date should be changed.", 
                       selectedUser.getModificationTimestamp().getTime()
                       != user1.getModificationTimestamp().getTime());
            selectedUser = (User)m_userFactory.get(
                                    user2.getId(),
                                    CallContext.getInstance().getCurrentDomainId());
            assertTrue("User should be enabled.", selectedUser.isLoginEnabled());
            assertTrue("Modified date should be changed.", 
                       selectedUser.getModificationTimestamp().getTime()
                       != user2.getModificationTimestamp().getTime());
   
            testSession = (InternalSession)m_sessionFactory.get(
                             session1.getId(),
                             CallContext.getInstance().getCurrentDomainId());
            assertNotNull("Internal session should exist.", testSession);
            assertEquals("Internal session is not the same.", session1, testSession);
            testSession = (InternalSession)m_sessionFactory.get(
                             session1.getId(),
                             CallContext.getInstance().getCurrentDomainId());
            assertNotNull("Internal session should exist.", testSession);
            assertEquals("Internal session is not the same.", session1, testSession);
         }
         finally
         {
            m_transaction.begin();
            try
            {
               if ((session1 != null) && (session1.getId() != DataObject.NEW_ID))
               {
                  m_sessionFactory.delete(
                     session1.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((session2 != null) && (session2.getId() != DataObject.NEW_ID))
               {
                  m_sessionFactory.delete(
                     session2.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((user1 != null) && (user1.getId() != DataObject.NEW_ID))
               {
                  m_userFactory.delete(
                     user1.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((user2 != null) && (user2.getId() != DataObject.NEW_ID))
               {
                  m_userFactory.delete(
                     user2.getId(),
                     CallContext.getInstance().getCurrentDomainId());
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
       * Test for updateEnable with false parameter. 
       * 
       * @throws Exception - an error has occurred
       */
      public void testUpdateEnableFalse(
      ) throws Exception 
      {
         User            user1        = null;
         User            user2        = null;
         User            selectedUser = null;
         InternalSession session1 = null;
         InternalSession session2 = null;
         InternalSession testSession = null;
         int             iUsersEnabled = 0;
   
         try
         {
            m_transaction.begin();
            try
            {
               // create two users
               user1 = new User(DataObject.NEW_ID,
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
               user1 = (User)m_userFactory.create(user1);
   
               user2 = new User(DataObject.NEW_ID,
                                 CallContext.getInstance().getCurrentDomainId(),
                                 "test_first_name_2",
                                 "test_last_name_2",
                                 "test_phone_2",
                                 "test_fax_2",
                                 "test_address_2",
                                 "test_email_2",
                                 "test_login_name_2",
                                 "test_password_2",
                                 true,
                                 false,
                                 true,
                                 true,
                                 null,
                                 null, true
                                );
               user2 = (User)m_userFactory.create(user2);
   
               // create two internal sessions
               session1 = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            user1.getId(),
                                            "test_generated_code_1",
                                            "test_c_IP_1",
                                            "test_client_type_1",
                                            null,
                                            false
                                            );
            
               session1 = (InternalSession)m_sessionFactory.create(session1);
            
               session2 = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            user2.getId(),
                                            "test_generated_code_2",
                                            "test_c_IP_2",
                                            "test_client_type_2",
                                            null,
                                            false
                                            );
            
               session2 = (InternalSession)m_sessionFactory.create(session2);
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            Thread.sleep(1000);
            
            m_transaction.begin();
            try
            {
               int[] arrIds = new int[] {session1.getId(), session2.getId()};
               
               // try to enable selected users
               iUsersEnabled = m_sessionControl.updateEnable(arrIds, false);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            assertEquals("Number of enabled users is incorrect", 2, iUsersEnabled);
   
            selectedUser = (User)m_userFactory.get(
                                    user1.getId(),
                                    CallContext.getInstance().getCurrentDomainId());
            assertFalse("User should be disabled.", selectedUser.isLoginEnabled());
            assertTrue("Modified date should be changed.", 
                       selectedUser.getModificationTimestamp().getTime()
                       != user1.getModificationTimestamp().getTime());
            selectedUser = (User)m_userFactory.get(
                                    user2.getId(),
                                    CallContext.getInstance().getCurrentDomainId());
            assertFalse("User should be disabled.", selectedUser.isLoginEnabled());
            assertTrue("Modified date should be changed.", 
                       selectedUser.getModificationTimestamp().getTime()
                       != user2.getModificationTimestamp().getTime());
   
            testSession = (InternalSession)m_sessionFactory.get(
                             session1.getId(),
                             CallContext.getInstance().getCurrentDomainId());
            assertNotNull("Internal session should exist.", testSession);
            assertEquals("Internal session is not the same.", session1, testSession);
            testSession = (InternalSession)m_sessionFactory.get(
                             session1.getId(),
                             CallContext.getInstance().getCurrentDomainId());
            assertNotNull("Internal session should exist.", testSession);
            assertEquals("Internal session is not the same.", session1, testSession);
         }
         finally
         {
            m_transaction.begin();
            try
            {
               if ((session1 != null) && (session1.getId() != DataObject.NEW_ID))
               {
                  m_sessionFactory.delete(
                     session1.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((session2 != null) && (session2.getId() != DataObject.NEW_ID))
               {
                  m_sessionFactory.delete(
                     session2.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((user1 != null) && (user1.getId() != DataObject.NEW_ID))
               {
                  m_userFactory.delete(
                     user1.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((user2 != null) && (user2.getId() != DataObject.NEW_ID))
               {
                  m_userFactory.delete(
                     user2.getId(),
                     CallContext.getInstance().getCurrentDomainId());
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
       * Test for disabling all users. These shouldn't be disabled because 
       * at least 1 superuser has to remain.
       * 
       * @throws Exception - an error has occurred
       */
      public void testUpdateEnableFalseAllUsers(
      ) throws Exception 
      {
         User            user1        = null;
         User            user2        = null;
         User            selectedUser = null;
         InternalSession session1 = null;
         InternalSession session2 = null;
         InternalSession testSession = null;
         int             iUsersDisabled = 0;
   
         try
         {
            m_transaction.begin();
            try
            {
               // create two users
               user1 = new User(DataObject.NEW_ID,
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
               user1 = (User)m_userFactory.create(user1);
   
               user2 = new User(DataObject.NEW_ID,
                                 CallContext.getInstance().getCurrentDomainId(),
                                 "test_first_name_2",
                                 "test_last_name_2",
                                 "test_phone_2",
                                 "test_fax_2",
                                 "test_address_2",
                                 "test_email_2",
                                 "test_login_name_2",
                                 "test_password_2",
                                 true,
                                 false,
                                 true,
                                 true,
                                 null,
                                 null, true
                                );
               user2 = (User)m_userFactory.create(user2);
   
               // create two internal sessions
               session1 = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            user1.getId(),
                                            "test_generated_code_1",
                                            "test_c_IP_1",
                                            "test_client_type_1",
                                            null,
                                            false
                                            );
            
               session1 = (InternalSession)m_sessionFactory.create(session1);
            
               session2 = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            user2.getId(),
                                            "test_generated_code_2",
                                            "test_c_IP_2",
                                            "test_client_type_2",
                                            null,
                                            false
                                            );
            
               session2 = (InternalSession)m_sessionFactory.create(session2);
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            Thread.sleep(1000);
            
            // get current session object
            InternalSession currSession = m_sessionFactory.get(
                     CallContext.getInstance().getCurrentSession());
               
            m_transaction.begin();
            try
            {
               int[] arrIds = new int[] {session1.getId(), 
                                         session2.getId(),
                                         currSession.getId()};
               
               // try to disable all users
               iUsersDisabled = m_sessionControl.updateEnable(arrIds, false);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // there have to be disabled no users (no changes within the DB)
            assertEquals("Number of disabled users is incorrect", -1, iUsersDisabled);
   
            selectedUser = (User)m_userFactory.get(
                                    user1.getId(),
                                    CallContext.getInstance().getCurrentDomainId());
            assertTrue("User should be enabled.", selectedUser.isLoginEnabled());
            assertTrue("Modified date should not be changed.", 
                       selectedUser.getModificationTimestamp().getTime()
                       == user1.getModificationTimestamp().getTime());
            selectedUser = (User)m_userFactory.get(
                                    user2.getId(),
                                    CallContext.getInstance().getCurrentDomainId());
            assertTrue("User should be enabled.", selectedUser.isLoginEnabled());
            assertTrue("Modified date should not be changed.", 
                       selectedUser.getModificationTimestamp().getTime()
                       == user2.getModificationTimestamp().getTime());
   
            testSession = (InternalSession)m_sessionFactory.get(
                             session1.getId(),
                             CallContext.getInstance().getCurrentDomainId());
            assertNotNull("Internal session should exist.", testSession);
            assertEquals("Internal session is not the same.", session1, testSession);
            testSession = (InternalSession)m_sessionFactory.get(
                             session1.getId(),
                             CallContext.getInstance().getCurrentDomainId());
            assertNotNull("Internal session should exist.", testSession);
            assertEquals("Internal session is not the same.", session1, testSession);
         }
         finally
         {
            m_transaction.begin();
            try
            {
               if ((session1 != null) && (session1.getId() != DataObject.NEW_ID))
               {
                  m_sessionFactory.delete(
                     session1.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((session2 != null) && (session2.getId() != DataObject.NEW_ID))
               {
                  m_sessionFactory.delete(
                     session2.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((user1 != null) && (user1.getId() != DataObject.NEW_ID))
               {
                  m_userFactory.delete(
                     user1.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((user2 != null) && (user2.getId() != DataObject.NEW_ID))
               {
                  m_userFactory.delete(
                     user2.getId(),
                     CallContext.getInstance().getCurrentDomainId());
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
       * Test for checkLoggedInUser method  
       * 
       * @throws Exception - an error has occurred
       */
      public void testCheckLoggedInUser(
      ) throws Exception 
      {
         InternalSession session  = null;
   
         try
         {
            m_transaction.begin();
            try
            {
               // create internal sessions
               session = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            CallContext.getInstance().getCurrentUserId(),
                                            "test_generated_code_1",
                                            "test_c_IP_1",
                                            "test_client_type_1",
                                            null,
                                            false
                                            );
            
               session = (InternalSession)m_sessionFactory.create(session);
            
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            // try to get internal session
            assertTrue("internal session was not found", 
                       m_sessionControl.checkLoggedInUser(session.getInternalSession()));
         }
         finally
         {
            m_transaction.begin();
            try
            {
               if ((session != null) && (session.getId() != DataObject.NEW_ID))
               {
                  m_sessionFactory.delete(
                     session.getId(),
                     CallContext.getInstance().getCurrentDomainId());
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
       * Test for getUserId method  
       * 
       * @throws Exception - an error has occurred
       */
      public void testGetUserId(
      ) throws Exception 
      {
         User            user1        = null;
         User            user2        = null;
         InternalSession session  = null;
         
         String strInternalSession = "test_generated_code_1";
         int    iTestUserID = DataObject.NEW_ID;    
   
         try
         {
            m_transaction.begin();
            try
            {
               // create two users
               user1 = new User(DataObject.NEW_ID,
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
               user1 = (User)m_userFactory.create(user1);
   
               user2 = new User(DataObject.NEW_ID,
                                 CallContext.getInstance().getCurrentDomainId(),
                                 "test_first_name_2",
                                 "test_last_name_2",
                                 "test_phone_2",
                                 "test_fax_2",
                                 "test_address_2",
                                 "test_email_2",
                                 "test_login_name_2",
                                 "test_password_2",
                                 true,
                                 false,
                                 true,
                                 true,
                                 null,
                                 null, true
                                );
               user2 = (User)m_userFactory.create(user2);
   
               // create internal sessions
               session = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            user2.getId(),
                                            strInternalSession,
                                            "test_c_IP_1",
                                            "test_client_type_1",
                                            null,
                                            false
                                            );
            
               session = (InternalSession)m_sessionFactory.create(session);
            
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            // try to get user ID by internal session
            iTestUserID = m_sessionControl.getUserId(strInternalSession);
            
            assertEquals("Incorrect user ID", user2.getId(), iTestUserID);
         }
         finally
         {
            m_transaction.begin();
            try
            {
               
               if ((session != null) && (session.getId() != DataObject.NEW_ID))
               {
                  m_sessionFactory.delete(
                     session.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((user1 != null) && (user1.getId() != DataObject.NEW_ID))
               {
                  m_userFactory.delete(
                     user1.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((user2 != null) && (user2.getId() != DataObject.NEW_ID))
               {
                  m_userFactory.delete(
                     user2.getId(),
                     CallContext.getInstance().getCurrentDomainId());
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
       * Test for logout all sessions for all specfied domains - in this test 
       * we allow logout  
       * 
       * @throws Exception - an error has occurred
       */
      public void testLogoutDomainsAllow(
      ) throws Exception 
      {
         Domain          domain1   = null;
         Domain          domain2   = null;
         User            user1     = null;
         User            user2     = null;
         User            user3     = null;
         Role            role      = null;
         AccessRight     right     = null;
         InternalSession session11 = null;
         InternalSession session12 = null;
         InternalSession session21 = null;
         InternalSession session22 = null;
         InternalSession session31 = null;
         InternalSession session32 = null;
         int             iLoggedOut = DataObject.NEW_ID;

         try
         {
            m_transaction.begin();
            try
            {
               // create 2 test domains
               domain1 = new Domain(DataObject.NEW_ID,
                                    "test_domain_name_1",
                                    "test_domain_description_1",
                                    true, true, false, false, false, false, false,
                                    "test_phone_1", "test_domain_address_1",
                                    null, null, null
                                   );
               domain1 = (Domain)m_domainFactory.create(domain1);

               domain2 = new Domain(DataObject.NEW_ID,
                                    "test_domain_name_2",
                                    "test_domain_description_2",
                                    true, true, false, false, false, false, false,
                                    "test_phone_2", "test_domain_address_2",
                                    null, null, null
                                   );
               domain2 = (Domain)m_domainFactory.create(domain2);

               // create test user1 for actual domain
               user1 = new User(DataObject.NEW_ID,
                                CallContext.getInstance().getCurrentDomainId(),
                                "test_first_name_1",
                                "test_last_name_1",
                                "test_phone_1",
                                "test_fax_1",
                                "test_address_1",
                                "test_email_1",
                                "test_login_name_1",
                                "test_password_1",
                                true, false, false, true, null, null, true
                               );
               user1 = (User)m_userFactory.create(user1);
            
               // create test user2 for actual domain1
               user2 = new User(DataObject.NEW_ID,
                                domain1.getId(),
                                "test_first_name_2",
                                "test_last_name_2",
                                "test_phone_2",
                                "test_fax_2",
                                "test_address_2",
                                "test_email_2",
                                "test_login_name_2",
                                "test_password_2",
                                true, false, false, true, null, null, true
                               );
               user2 = (User)m_userFactory.create(user2);
            
               // create test user3 for actual domain2
               user3 = new User(DataObject.NEW_ID,
                                domain2.getId(),
                                "test_first_name_3",
                                "test_last_name_3",
                                "test_phone_3",
                                "test_fax_3",
                                "test_address_3",
                                "test_email_3",
                                "test_login_name_3",
                                "test_password_3",
                                true, false, false, true, null, null, true
                               );
               user3 = (User)m_userFactory.create(user3);

               role = new Role(DataObject.NEW_ID, 
                           CallContext.getInstance().getCurrentDomainId(), 
                           "test role name 1", "test role description 1", true, 
                           user1.getId(), false, null, null, null);
               role = (Role)m_roleFactory.create(role);

               right = new AccessRight(DataObject.NEW_ID, role.getId(),
                           CallContext.getInstance().getCurrentDomainId(), 
                           ActionConstants.RIGHT_ACTION_VIEW, 
                           m_iIntSessionDataType, 
                           1, InternalSessionDataDescriptor.COL_INTSESSION_ID, 3,
                           null, null);                  
               right = m_accessRightFactory.create(right);

               m_roleFactory.assignToUser(user1.getId(),
                                          Integer.toString(role.getId()));

               // create 2 test internal sessions belonging to user1
               session11 = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            user1.getId(),
                                            "test_generated_code_11",
                                            "test_c_IP_11",
                                            "test_client_type_11",
                                            null, true
                                            );
               session11 = (InternalSession)m_sessionFactory.create(session11);
            
               session12 = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            user1.getId(),
                                            "test_generated_code_12",
                                            "test_c_IP_12",
                                            "test_client_type_12",
                                            null, false
                                            );
               session12 = (InternalSession)m_sessionFactory.create(session12);

               // create 2 test internal sessions belonging to user2
               session21 = new InternalSession(DataObject.NEW_ID,
                                            domain1.getId(),
                                            user2.getId(),
                                            "test_generated_code_21",
                                            "test_c_IP_21",
                                            "test_client_type_21",
                                            null, true
                                            );
               session21 = (InternalSession)m_sessionFactory.create(session21);
            
               session22 = new InternalSession(DataObject.NEW_ID,
                                            domain1.getId(),
                                            user2.getId(),
                                            "test_generated_code_22",
                                            "test_c_IP_22",
                                            "test_client_type_22",
                                            null, false
                                            );
               session22 = (InternalSession)m_sessionFactory.create(session22);

               // create 3 test internal sessions belonging to user3
               session31 = new InternalSession(DataObject.NEW_ID,
                                            domain2.getId(),
                                            user3.getId(),
                                            "test_generated_code_31",
                                            "test_c_IP_31",
                                            "test_client_type_31",
                                            null, true
                                            );
               session31 = (InternalSession)m_sessionFactory.create(session31);
            
               session32 = new InternalSession(DataObject.NEW_ID,
                                            domain2.getId(),
                                            user3.getId(),
                                            "test_generated_code_32",
                                            "test_c_IP_32",
                                            "test_client_type_32",
                                            null, false
                                            );
               session32 = (InternalSession)m_sessionFactory.create(session32);
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            int[] arrDomainIds = new int[] {domain1.getId(), domain2.getId(), };
   
            m_transaction.begin();
            try
            {
               // try to logout internal sessions for current domain1 and domain2
               iLoggedOut = m_sessionControl.logoutDomains(arrDomainIds);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            assertEquals("Number of session logged out is incorrect", 4, iLoggedOut);
         }
         finally
         {
            PreparedStatement statement = null;
            String            strQuery;
   
            m_transaction.begin();
            // delete inserted records
            try
            {
               if ((session11 != null) && (session11.getId() != DataObject.NEW_ID))
               {
                  // delete session11
                  m_sessionFactory.delete(
                     session11.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((session12 != null) && (session12.getId() != DataObject.NEW_ID))
               {
                  // delete session12
                  m_sessionFactory.delete(
                     session12.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               try
               {
                  // delete internal sessions for not actual domains
                  strQuery = "delete from " + 
                             SessionDatabaseSchema.INTSESSION_TABLE_NAME + 
                             " where ID in (?, ?, ?, ?)";
                  statement = m_connection.prepareStatement(strQuery);
                  statement.setInt(1, session21.getId());
                  statement.setInt(2, session22.getId());
                  statement.setInt(3, session31.getId());
                  statement.setInt(4, session32.getId());
                  statement.executeUpdate();
               }
               finally
               {
                  DatabaseUtils.closeStatement(statement);
               }
               if ((right != null) && (right.getId() != DataObject.NEW_ID))
               {
                  // delete access right
                  m_accessRightFactory.deleteAllForRole(role.getId());
               }
               if ((role != null) && (role.getId() != DataObject.NEW_ID))
               {
                  // delete role
                  m_roleFactory.delete(
                     role.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((user1 != null) && (user1.getId() != DataObject.NEW_ID))
               {
                  // delete user1
                  m_userFactory.delete(
                     user1.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               try
               {
                  // delete users for not actual domains
                  strQuery = "delete from " + 
                             UserDatabaseSchema.USER_TABLE_NAME + 
                             " where ID in (?, ?)";
                  statement = m_connection.prepareStatement(strQuery);
                  statement.setInt(1, user2.getId());
                  statement.setInt(2, user3.getId());
                  statement.executeUpdate();
               }
               finally
               {
                  DatabaseUtils.closeStatement(statement);
               }
               if ((domain1 != null) && (domain1.getId() != DataObject.NEW_ID))
               {
                  // delete domain1
                  m_domainFactory.delete(
                     domain1.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((domain2 != null) && (domain2.getId() != DataObject.NEW_ID))
               {
                  // delete domain2
                  m_domainFactory.delete(
                     domain2.getId(),
                     CallContext.getInstance().getCurrentDomainId());
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
       * Test for logout all sessions for all specfied domains - in this test 
       * we don't allow logout  
       * 
       * @throws Exception - an error has occurred
       */
      public void testLogoutDomainsDeny(
      ) throws Exception 
      {
         Domain          domain1   = null;
         Domain          domain2   = null;
         User            user1     = null;
         User            user2     = null;
         User            user3     = null;
         Role            role      = null;
         AccessRight     right     = null;
         InternalSession session11 = null;
         InternalSession session12 = null;
         InternalSession session21 = null;
         InternalSession session22 = null;
         InternalSession session31 = null;
         InternalSession session32 = null;
         int             iLoggedOut = DataObject.NEW_ID;

         try
         {
            m_transaction.begin();
            try
            {
               // create 2 test domains
               domain1 = new Domain(DataObject.NEW_ID,
                                    "test_domain_name_1",
                                    "test_domain_description_1",
                                    true, true, false, false, false, false, false,
                                    "test_phone_1", "test_domain_address_1",
                                    null, null, null
                                   );
               domain1 = (Domain)m_domainFactory.create(domain1);

               domain2 = new Domain(DataObject.NEW_ID,
                                    "test_domain_name_2",
                                    "test_domain_description_2",
                                    true, true, false, false, false, false, false,
                                    "test_phone_2", "test_domain_address_2",
                                    null, null, null
                                   );
               domain2 = (Domain)m_domainFactory.create(domain2);

               // create test user1 for actual domain
               user1 = new User(DataObject.NEW_ID,
                                CallContext.getInstance().getCurrentDomainId(),
                                "test_first_name_1",
                                "test_last_name_1",
                                "test_phone_1",
                                "test_fax_1",
                                "test_address_1",
                                "test_email_1",
                                "test_login_name_1",
                                "test_password_1",
                                true, false, false, true, null, null, true
                               );
               user1 = (User)m_userFactory.create(user1);
            
               // create test user2 for actual domain1
               user2 = new User(DataObject.NEW_ID,
                                domain1.getId(),
                                "test_first_name_2",
                                "test_last_name_2",
                                "test_phone_2",
                                "test_fax_2",
                                "test_address_2",
                                "test_email_2",
                                "test_login_name_2",
                                "test_password_2",
                                true, false, false, true, null, null, true
                               );
               user2 = (User)m_userFactory.create(user2);
            
               // create test user3 for actual domain2
               user3 = new User(DataObject.NEW_ID,
                                domain2.getId(),
                                "test_first_name_3",
                                "test_last_name_3",
                                "test_phone_3",
                                "test_fax_3",
                                "test_address_3",
                                "test_email_3",
                                "test_login_name_3",
                                "test_password_3",
                                true, false, false, true, null, null, true
                               );
               user3 = (User)m_userFactory.create(user3);

               role = new Role(DataObject.NEW_ID, 
                           CallContext.getInstance().getCurrentDomainId(), 
                           "test role name 1", "test role description 1", true, 
                           user1.getId(), false, null, null, null);
               role = (Role)m_roleFactory.create(role);

               right = new AccessRight(DataObject.NEW_ID, role.getId(),
                           CallContext.getInstance().getCurrentDomainId(), 
                           ActionConstants.RIGHT_ACTION_VIEW, 
                           m_iIntSessionDataType, 
                           1, InternalSessionDataDescriptor.COL_INTSESSION_ID, 3,
                           null, null);                  
               right = m_accessRightFactory.create(right);

               m_roleFactory.assignToUser(user1.getId(),
                                          Integer.toString(role.getId()));

               // create 2 test internal sessions belonging to user1
               session11 = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            user1.getId(),
                                            "test_generated_code_11",
                                            "test_c_IP_11",
                                            "test_client_type_11",
                                            null, true
                                            );
               session11 = (InternalSession)m_sessionFactory.create(session11);
            
               session12 = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            user1.getId(),
                                            "test_generated_code_12",
                                            "test_c_IP_12",
                                            "test_client_type_12",
                                            null, false
                                            );
               session12 = (InternalSession)m_sessionFactory.create(session12);

               // create 2 test internal sessions belonging to user2
               session21 = new InternalSession(DataObject.NEW_ID,
                                            domain1.getId(),
                                            user2.getId(),
                                            "test_generated_code_21",
                                            "test_c_IP_21",
                                            "test_client_type_21",
                                            null, true
                                            );
               session21 = (InternalSession)m_sessionFactory.create(session21);
            
               session22 = new InternalSession(DataObject.NEW_ID,
                                            domain1.getId(),
                                            user2.getId(),
                                            "test_generated_code_22",
                                            "test_c_IP_22",
                                            "test_client_type_22",
                                            null, false
                                            );
               session22 = (InternalSession)m_sessionFactory.create(session22);

               // create 3 test internal sessions belonging to user3
               session31 = new InternalSession(DataObject.NEW_ID,
                                            domain2.getId(),
                                            user3.getId(),
                                            "test_generated_code_31",
                                            "test_c_IP_31",
                                            "test_client_type_31",
                                            null, true
                                            );
               session31 = (InternalSession)m_sessionFactory.create(session31);
            
               session32 = new InternalSession(DataObject.NEW_ID,
                                            domain2.getId(),
                                            user3.getId(),
                                            "test_generated_code_32",
                                            "test_c_IP_32",
                                            "test_client_type_32",
                                            null, false
                                            );
               session32 = (InternalSession)m_sessionFactory.create(session32);
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            int[] arrDomainIds = new int[] {domain1.getId(), domain2.getId(), };

            // set new user and session info
            CallContext.getInstance().setCurrentUserAndSession(user1, 
                                                session11.getInternalSession());

            m_transaction.begin();
            try
            {
               // try to logout internal sessions for current domain1 and domain2
               iLoggedOut = m_sessionControl.logoutDomains(arrDomainIds);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }

            // Reset up back original user and session info
            CallContext.getInstance().resetCurrentUserAndSession();

            assertEquals("Number of session logged out is incorrect", 0, iLoggedOut);
         }
         finally
         {
            PreparedStatement statement = null;
            String            strQuery;
   
            m_transaction.begin();
            // delete inserted records
            try
            {
               if ((session11 != null) && (session11.getId() != DataObject.NEW_ID))
               {
                  // delete session11
                  m_sessionFactory.delete(
                     session11.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((session12 != null) && (session12.getId() != DataObject.NEW_ID))
               {
                  // delete session12
                  m_sessionFactory.delete(
                     session12.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               try
               {
                  // delete internal sessions for not actual domains
                  strQuery = "delete from " + 
                             SessionDatabaseSchema.INTSESSION_TABLE_NAME + 
                             " where ID in (?, ?, ?, ?)";
                  statement = m_connection.prepareStatement(strQuery);
                  statement.setInt(1, session21.getId());
                  statement.setInt(2, session22.getId());
                  statement.setInt(3, session31.getId());
                  statement.setInt(4, session32.getId());
                  statement.executeUpdate();
               }
               finally
               {
                  DatabaseUtils.closeStatement(statement);
               }
               if ((right != null) && (right.getId() != DataObject.NEW_ID))
               {
                  // delete access right
                  m_accessRightFactory.deleteAllForRole(role.getId());
               }
               if ((role != null) && (role.getId() != DataObject.NEW_ID))
               {
                  // delete role
                  m_roleFactory.delete(
                     role.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((user1 != null) && (user1.getId() != DataObject.NEW_ID))
               {
                  // delete user1
                  m_userFactory.delete(
                     user1.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               try
               {
                  // delete users for not actual domains
                  strQuery = "delete from " + 
                             UserDatabaseSchema.USER_TABLE_NAME + 
                             " where ID in (?, ?)";
                  statement = m_connection.prepareStatement(strQuery);
                  statement.setInt(1, user2.getId());
                  statement.setInt(2, user3.getId());
                  statement.executeUpdate();
               }
               finally
               {
                  DatabaseUtils.closeStatement(statement);
               }
               if ((domain1 != null) && (domain1.getId() != DataObject.NEW_ID))
               {
                  // delete domain1
                  m_domainFactory.delete(
                     domain1.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((domain2 != null) && (domain2.getId() != DataObject.NEW_ID))
               {
                  // delete domain2
                  m_domainFactory.delete(
                     domain2.getId(),
                     CallContext.getInstance().getCurrentDomainId());
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
   }
}
