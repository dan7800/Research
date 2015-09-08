/*
 * Copyright (c) 2003 - 2009 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: InternalSessionDatabaseFactoryTest.java,v 1.35 2009/09/20 05:32:57 bastafidli Exp $
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

package org.opensubsystems.security.persist.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.opensubsystems.core.application.Application;
import org.opensubsystems.security.application.SecureApplicationTestSetup;
import org.opensubsystems.core.application.ModuleManager;
import org.opensubsystems.core.application.impl.ApplicationImpl;
import org.opensubsystems.core.data.DataDescriptorManager;
import org.opensubsystems.core.data.DataObject;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.error.OSSInvalidDataException;
import org.opensubsystems.core.logic.ControllerManager;
import org.opensubsystems.core.persist.DataFactoryManager;
import org.opensubsystems.core.persist.db.DatabaseSchemaManager;
import org.opensubsystems.core.persist.db.DatabaseTestSuite;
import org.opensubsystems.core.util.ArrayUtils;
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
import org.opensubsystems.security.data.User;
import org.opensubsystems.security.logic.AuthorizationController;
import org.opensubsystems.security.patterns.listdata.persist.db.SecureListDatabaseFactoryTest;
import org.opensubsystems.security.persist.AccessRightFactory;
import org.opensubsystems.security.persist.DomainFactory;
import org.opensubsystems.security.persist.ExternalSessionFactory;
import org.opensubsystems.security.persist.InternalSessionFactory;
import org.opensubsystems.security.persist.RoleFactory;
import org.opensubsystems.security.persist.UserFactory;
import org.opensubsystems.security.util.ActionConstants;

/**
 * Test of InternalSessionDatabaseFactory
 * 
 * @version $Id: InternalSessionDatabaseFactoryTest.java,v 1.35 2009/09/20 05:32:57 bastafidli Exp $
 * @author Julo Legeny
 * @code.reviewer Miro Halas
 * @code.reviewed Initial revision
 */
public final class InternalSessionDatabaseFactoryTest extends Tests
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private InternalSessionDatabaseFactoryTest(
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
      TestSuite suite = new DatabaseTestSuite("InternalSessionDatabaseFactoryTest");
      suite.addTestSuite(InternalSessionDatabaseFactoryTestInternal.class);
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
   public static class InternalSessionDatabaseFactoryTestInternal 
                       extends SecureListDatabaseFactoryTest
   {
      // Cached values ////////////////////////////////////////////////////////////
   
      /**
       * Schema for database dependent operations.
       */
      protected SessionDatabaseSchema m_sessionSchema;
   
      /**
       * Factory to manage domains.
       */
      protected DomainDatabaseFactory m_domainFactory;

      /**
       * Factory to manage users.
       */
      protected UserDatabaseFactory m_userFactory;

      /**
       * Factory to manage roles.
       */
      protected RoleDatabaseFactory m_roleFactory;

      /**
       * Factory to manage access rights.
       */
      protected AccessRightDatabaseFactory m_accessRightFactory;
   
      /**
       * Factory to manage internal sessions.
       */
      protected InternalSessionDatabaseFactory m_sessionFactory;
   
      /**
       * Factory to manage external sessions.
       */
      protected ExternalSessionDatabaseFactory m_externalSessionFactory;

      /**
       * Controller to perform authorization.
       */
      protected AuthorizationController m_authorizeControl;

      /**
       * Data descriptor for the internal session data types.
       */
      protected InternalSessionDataDescriptor m_intSessionDescriptor;

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
      public InternalSessionDatabaseFactoryTestInternal(
         String strTestName
      ) throws Exception
      {
         super(strTestName, new InternalSessionListDatabaseTestUtils());
   
         m_sessionSchema = (SessionDatabaseSchema)DatabaseSchemaManager.getInstance(
                                                     SessionDatabaseSchema.class);
         m_domainFactory = (DomainDatabaseFactory)DataFactoryManager.getInstance(
                                                     DomainFactory.class);
         m_userFactory = (UserDatabaseFactory)DataFactoryManager.getInstance(
                                                     UserFactory.class);
         m_sessionFactory = (InternalSessionDatabaseFactory)DataFactoryManager.getInstance(
                                                     InternalSessionFactory.class);      
         m_roleFactory = (RoleDatabaseFactory)DataFactoryManager.getInstance(
                                                     RoleFactory.class);
         m_accessRightFactory = (AccessRightDatabaseFactory)DataFactoryManager.getInstance(
                                                     AccessRightFactory.class);
         m_externalSessionFactory = (ExternalSessionDatabaseFactory)DataFactoryManager.getInstance(
                                                     ExternalSessionFactory.class);
         m_authorizeControl = (AuthorizationController)ControllerManager.getInstance(
                                                     AuthorizationController.class);
         m_intSessionDescriptor = (InternalSessionDataDescriptor)DataDescriptorManager
                                     .getInstance(InternalSessionDataDescriptor.class);
      }
   
      // Tests ////////////////////////////////////////////////////////////////////
   
      /**
       * Test for get method with id parameter  
       * 
       * @throws Exception - an error has occurred
       */
      public void testGetById(
      ) throws Exception 
      {
         User            user         = null;
         InternalSession session  = null;
         InternalSession testSession = null;
   
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
                                true, false, true, true, null, null,
                                true
                               );
               
               user = (User)m_userFactory.create(user);
               
               // create test internal session
               session = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            user.getId(),
                                            "test_generated_code",
                                            "test_c_IP",
                                            "test_client_type",
                                            null, true
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
            testSession = (InternalSession)m_sessionFactory.get(
                             session.getId(),
                             CallContext.getInstance().getCurrentDomainId());
      
            assertNotNull("Internal session should not be null", testSession);
            assertEquals("Internal session is not the same", session, testSession);
         }
         finally
         {
            m_transaction.begin();
            // delete inserted records
            try
            {
               if ((session != null) && (session.getId() != DataObject.NEW_ID))
               {
                  // delete session
                  m_sessionFactory.delete(
                                      session.getId(),
                                      CallContext.getInstance().getCurrentDomainId());
               }
               if ((user != null) && (user.getId() != DataObject.NEW_ID))
               {
                  // delete user
                  m_userFactory.delete(
                                   user.getId(),
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
       * Test for get method with internal session string parameter  
       * 
       * @throws Exception - an error has occurred
       */
      public void testGetByInternalSession(
      ) throws Exception 
      {
         User            user         = null;
         InternalSession session  = null;
         InternalSession testSession = null;
   
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
                                true, false, true, true, null, null, true
                               );
               
               user = (User)m_userFactory.create(user);
               
               // create test internal session
               session = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            user.getId(),
                                            "test_generated_code",
                                            "test_c_IP",
                                            "test_client_type",
                                            null, true
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
            testSession = m_sessionFactory.get(session.getInternalSession());
      
            assertNotNull("Internal session should not be null", testSession);
            assertEquals("Internal session is not the same", session, testSession);
         }
         finally
         {
            m_transaction.begin();
            // delete inserted records
            try
            {
               if ((session != null) && (session.getId() != DataObject.NEW_ID))
               {
                  // delete session
                  m_sessionFactory.delete(
                                      session.getId(),
                                      CallContext.getInstance().getCurrentDomainId());
               }
               if ((user != null) && (user.getId() != DataObject.NEW_ID))
               {
                  // delete user
                  m_userFactory.delete(
                                   user.getId(),
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
       * test for  getting actual internal session IDs (method getActualIds)
       * 
       * @throws Exception - error during test
       */
      public void testGetActualIds(
      ) throws Exception 
      {
         User            user     = null;
         InternalSession session1 = null;
         InternalSession session2 = null;
         InternalSession session3 = null;
         InternalSession session4 = null;
   
         int[] arrActualBFSessionIds = null;
   
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
                                true, false, true, true, null, null, true
                               );
               
               user = (User)m_userFactory.create(user);
               
               // create 3 test internal sessions
               session1 = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            user.getId(),
                                            "test_generated_code_1",
                                            "test_c_IP_1",
                                            "test_client_type_1",
                                            null, true
                                            );
               
               session1 = (InternalSession)m_sessionFactory.create(session1);
               
               session2 = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            user.getId(),
                                            "test_generated_code_2",
                                            "test_c_IP_2",
                                            "test_client_type_2",
                                            null, true
                                            );
               
               session2 = (InternalSession)m_sessionFactory.create(session2);
   
               session3 = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            user.getId(),
                                            "test_generated_code_3",
                                            "test_c_IP_3",
                                            "test_client_type_3",
                                            null, true
                                            );
               
               session3 = (InternalSession)m_sessionFactory.create(session3);
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }   
            // string representation of the selected internal sessions
            int[] arSelectedSessions = new int[] {session1.getId(), 
                                                  session2.getId(),
                                                  session3.getId(),
                                                 };
            
            // try to get actual number of internal sessions
            arrActualBFSessionIds = m_sessionFactory.getActualIds(
                                       arSelectedSessions, SimpleRule.ALL_DATA);
   
            assertNotNull("BF session IDs not found", arrActualBFSessionIds);
            assertEquals("Actual BF session IDs count is incorrect", 3, 
                         arrActualBFSessionIds.length);
            assertTrue("BF session ID not found", 
                       ArrayUtils.contains(arrActualBFSessionIds, session1.getId()) != -1);
            assertTrue("BF session ID not found", 
                       ArrayUtils.contains(arrActualBFSessionIds, session2.getId()) != -1);
            assertTrue("BF session ID not found", 
                       ArrayUtils.contains(arrActualBFSessionIds, session3.getId()) != -1);
   
            // Now we will delete one session and add additional one. Then we will try to 
            // retrieve actual number of sessions
            m_transaction.begin();
            try
            {
               if ((session1 != null) && (session1.getId() != DataObject.NEW_ID))
               {
                  m_sessionFactory.delete(
                                      session1.getId(),
                                      CallContext.getInstance().getCurrentDomainId());
               }
   
               // create additional test internal sessions
               session4 = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            user.getId(),
                                            "test_generated_code_4",
                                            "test_c_IP_4",
                                            "test_client_type_4",
                                            null, true
                                            );
               
               session4 = (InternalSession)m_sessionFactory.create(session4);
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }   
   
            //try to get actual number of sessions
            arrActualBFSessionIds = m_sessionFactory.getActualIds(
                                       arSelectedSessions, SimpleRule.ALL_DATA);
   
            assertNotNull("BF session IDs not found", arrActualBFSessionIds);
            assertEquals("Actual BF session IDs count is incorrect", 2, 
                         arrActualBFSessionIds.length);
            assertFalse("BF session ID was found but it was already deleted", 
                       ArrayUtils.contains(arrActualBFSessionIds, session1.getId()) != -1);
            assertTrue("BF session ID not found", 
                       ArrayUtils.contains(arrActualBFSessionIds, session2.getId()) != -1);
            assertTrue("BF session ID not found", 
                       ArrayUtils.contains(arrActualBFSessionIds, session3.getId()) != -1);
         }
         finally
         {
            m_transaction.begin();
            try
            {
               if ((session2 != null) && (session2.getId() != DataObject.NEW_ID))
               {
                  m_sessionFactory.delete(
                                      session2.getId(),
                                      CallContext.getInstance().getCurrentDomainId());
               }
               if ((session3 != null) && (session3.getId() != DataObject.NEW_ID))
               {
                  m_sessionFactory.delete(
                                      session3.getId(),
                                      CallContext.getInstance().getCurrentDomainId());
               }
               if ((session4 != null) && (session4.getId() != DataObject.NEW_ID))
               {
                  m_sessionFactory.delete(
                                      session4.getId(),
                                      CallContext.getInstance().getCurrentDomainId());
               }
               if ((user != null) && (user.getId() != DataObject.NEW_ID))
               {
                  m_userFactory.delete(
                                   user.getId(),
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
       * test of getActualCount method
       * 
       * @throws Exception - error during test
       */
      public void testGetActualCount(
      ) throws Exception 
      {
         User            user     = null;
         InternalSession session1 = null;
         InternalSession session2 = null;
         InternalSession session3 = null;
         InternalSession session4 = null;
   
         int iActualCount = 0;
   
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
                                true, false, true, true, null, null, true
                               );
               
               user = (User)m_userFactory.create(user);
               
               // create 3 test internal sessions
               session1 = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            user.getId(),
                                            "test_generated_code_1",
                                            "test_c_IP_1",
                                            "test_client_type_1",
                                            null, true
                                            );
               
               session1 = (InternalSession)m_sessionFactory.create(session1);
               
               session2 = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            user.getId(),
                                            "test_generated_code_2",
                                            "test_c_IP_2",
                                            "test_client_type_2",
                                            null, true
                                            );
               
               session2 = (InternalSession)m_sessionFactory.create(session2);
   
               session3 = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            user.getId(),
                                            "test_generated_code_3",
                                            "test_c_IP_3",
                                            "test_client_type_3",
                                            null,
                                            true
                                            );
               
               session3 = (InternalSession)m_sessionFactory.create(session3);
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }   
            // string representation of the selected internal sessions
            int[] arSelectedSessions = new int[] {session1.getId(), 
                                                  session2.getId(),
                                                  session3.getId(),
                                                 };
            
            // try to get actual number of internal sessions
            iActualCount = m_sessionFactory.getActualCount(arSelectedSessions);
      
            assertEquals("Actual count of internal sessions is incorrect", 3, iActualCount);
   
            // Now we will delete one session and add additional one. Then we will try to 
            // retrieve actual number of sessions
            m_transaction.begin();
            try
            {
               if ((session1 != null) && (session1.getId() != DataObject.NEW_ID))
               {
                  m_sessionFactory.delete(
                                      session1.getId(),
                                      CallContext.getInstance().getCurrentDomainId());
               }
   
               // create additional test internal sessions
               session4 = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            user.getId(),
                                            "test_generated_code_4",
                                            "test_c_IP_4",
                                            "test_client_type_4",
                                            null, true
                                            );
               
               session4 = (InternalSession)m_sessionFactory.create(session4);
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }   
   
            // try to get actual number of internal sessions
            iActualCount = m_sessionFactory.getActualCount(arSelectedSessions);
      
            assertEquals("Actual count of internal sessions is incorrect", 2, iActualCount);
         }
         finally
         {
            m_transaction.begin();
            try
            {
               if ((session2 != null) && (session2.getId() != DataObject.NEW_ID))
               {
                  m_sessionFactory.delete(
                                      session2.getId(),
                                      CallContext.getInstance().getCurrentDomainId());
               }
               if ((session3 != null) && (session3.getId() != DataObject.NEW_ID))
               {
                  m_sessionFactory.delete(
                                      session3.getId(),
                                      CallContext.getInstance().getCurrentDomainId());
               }
               if ((session4 != null) && (session4.getId() != DataObject.NEW_ID))
               {
                  m_sessionFactory.delete(
                                      session4.getId(),
                                      CallContext.getInstance().getCurrentDomainId());
               }
               if ((user != null) && (user.getId() != DataObject.NEW_ID))
               {
                  m_userFactory.delete(
                                   user.getId(),
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
       * test of getActualUserCount method
       * 
       * @throws Exception - error during test
       */
      public void testGetActualUserCount(
      ) throws Exception 
      {
         User            user1    = null;
         User            user2    = null;
         User            user3    = null;
         
         InternalSession session1 = null;
         InternalSession session2 = null;
         InternalSession session3 = null;
         InternalSession session4 = null;
   
         int iActualUserCount = 0;
   
         try
         {
            m_transaction.begin();
            try
            {
               // create 2 test users first
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
                                true, false, true, true, null, null, true
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
                                true, false, true, true, null, null, true
                               );
               
               user2 = (User)m_userFactory.create(user2);
   
               // create 3 test internal sessions
               session1 = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            user1.getId(),
                                            "test_generated_code_1",
                                            "test_c_IP_1",
                                            "test_client_type_1",
                                            null, true
                                            );
               
               session1 = (InternalSession)m_sessionFactory.create(session1);
               
               session2 = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            user2.getId(),
                                            "test_generated_code_2",
                                            "test_c_IP_2",
                                            "test_client_type_2",
                                            null, true
                                            );
               
               session2 = (InternalSession)m_sessionFactory.create(session2);
   
               session3 = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            user2.getId(),
                                            "test_generated_code_3",
                                            "test_c_IP_3",
                                            "test_client_type_3",
                                            null, true
                                            );
               
               session3 = (InternalSession)m_sessionFactory.create(session3);
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }   
            // string representation of the selected internal sessions
            int[] arSelectedSessions = new int[] {session1.getId(), 
                                                  session2.getId(),
                                                  session3.getId(),
                                                 };
            
            // try to get actual number of users belonging to specified internal sessions
            iActualUserCount = m_sessionFactory.getActualCount(arSelectedSessions);
      
            assertEquals("Actual count of users is incorrect", 3, iActualUserCount);
   
            // Now we will delete one user (with belonging BF Sessions) and add additional one. 
            // Then we will try to retrieve actual number of users again
            m_transaction.begin();
            try
            {
               if ((session2 != null) && (session2.getId() != DataObject.NEW_ID))
               {
                  m_sessionFactory.delete(
                                      session2.getId(),
                                      CallContext.getInstance().getCurrentDomainId());
               }
               if ((session3 != null) && (session3.getId() != DataObject.NEW_ID))
               {
                  m_sessionFactory.delete(
                                      session3.getId(),
                                      CallContext.getInstance().getCurrentDomainId());
               }
               if ((user2 != null) && (user2.getId() != DataObject.NEW_ID))
               {
                  m_userFactory.delete(
                                   user2.getId(),
                                   CallContext.getInstance().getCurrentDomainId());
               }
   
               user3 = new User(DataObject.NEW_ID,
                                CallContext.getInstance().getCurrentDomainId(),
                                "test_first_name_3",
                                "test_last_name_3",
                                "test_phone_3",
                                "test_fax_3",
                                "test_address_3",
                                "test_email_3",
                                "test_login_name_3",
                                "test_password_3",
                                true, false, true, true, null, null, true
                               );
               
               user3 = (User)m_userFactory.create(user3);
   
               // create additional test internal sessions
               session4 = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            user3.getId(),
                                            "test_generated_code_4",
                                            "test_c_IP_4",
                                            "test_client_type_4",
                                            null, true
                                            );
               
               session4 = (InternalSession)m_sessionFactory.create(session4);
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }   
   
            // try to get actual number of users belonging to specified internal sessions
            iActualUserCount = m_sessionFactory.getActualCount(arSelectedSessions);
      
            assertEquals("Actual count of users is incorrect", 1, iActualUserCount);
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
               if ((session4 != null) && (session4.getId() != DataObject.NEW_ID))
               {
                  m_sessionFactory.delete(
                                      session4.getId(),
                                      CallContext.getInstance().getCurrentDomainId());
               }
               if ((user1 != null) && (user1.getId() != DataObject.NEW_ID))
               {
                  m_userFactory.delete(
                                   user1.getId(),
                                   CallContext.getInstance().getCurrentDomainId());
               }
               if ((user3 != null) && (user3.getId() != DataObject.NEW_ID))
               {
                  m_userFactory.delete(
                                   user3.getId(),
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
       * Test for getActualInternalSessions method  
       * 
       * @throws Exception - an error has occurred
       */
      public void testGetActualInternalSessions(
      ) throws Exception 
      {
         User            user     = null;
         InternalSession session1 = null;
         InternalSession session2 = null;
         InternalSession session3 = null;
   
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
                                true, false, true, true, null, null, true
                               );
               
               user = (User)m_userFactory.create(user);
               
               // create 3 test internal sessions
               session1 = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            user.getId(),
                                            "test_generated_code_1",
                                            "test_c_IP_1",
                                            "test_client_type_1",
                                            null, true
                                            );
               
               session1 = (InternalSession)m_sessionFactory.create(session1);
               
               session2 = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            user.getId(),
                                            "test_generated_code_2",
                                            "test_c_IP_2",
                                            "test_client_type_2",
                                            null, true
                                            );
               
               session2 = (InternalSession)m_sessionFactory.create(session2);
   
               session3 = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            user.getId(),
                                            "test_generated_code_3",
                                            "test_c_IP_3",
                                            "test_client_type_3",
                                            null, true
                                            );
               
               session3 = (InternalSession)m_sessionFactory.create(session3);
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }   
            // string representation of the selected internal sessions
            int[] arSelectedSessions = new int[] {session1.getId(), 
                                                  session2.getId(),
                                                  session3.getId(),
                                                  (session1.getId() +  // not existing ID
                                                   session1.getId() + 
                                                   session1.getId()),
                                                 }; 
            
            // try to get actual number of internal sessions
            List lstActualSessionGenCodes;
            
            lstActualSessionGenCodes = m_sessionFactory.getActualInternalSessions(
                                          arSelectedSessions, SimpleRule.ALL_DATA);
      
            assertNotNull("No sessions were retrieved", lstActualSessionGenCodes);
            assertEquals("Retrieved actual count of internal sessions is incorrect", 
                         3, lstActualSessionGenCodes.size());
            assertTrue("Session code wasn't retrieved.",
                       lstActualSessionGenCodes.contains(session1.getInternalSession()));
            assertTrue("Session code wasn't retrieved.",
                       lstActualSessionGenCodes.contains(session2.getInternalSession()));
            assertTrue("Session code wasn't retrieved.",
                       lstActualSessionGenCodes.contains(session3.getInternalSession()));
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
               if ((session3 != null) && (session3.getId() != DataObject.NEW_ID))
               {
                  m_sessionFactory.delete(
                                      session3.getId(),
                                      CallContext.getInstance().getCurrentDomainId());
               }
               if ((user != null) && (user.getId() != DataObject.NEW_ID))
               {
                  m_userFactory.delete(
                                   user.getId(),
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
       * Test for getting number of superuser IDs that are not contained 
       * within the selected IDs.   
       * 
       * @throws Exception - an error has occurred
       */
      public void testGetCountOfNotContainedSuperUsers(
      ) throws Exception
      {
         User user1 = null;
         User user2 = null;
         User user3 = null;
         User user4 = null;
   
         InternalSession session1 = null;
         InternalSession session2 = null;
         InternalSession session3 = null;
         InternalSession session4 = null;
   
         int iSuperUserCount = 0;
   
         try
         {
            m_transaction.begin();
            try
            {
               // create 4 test users and belonging sessions first
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
                                true, true, true, true, null, null, true
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
                                true, true, false, true, null, null, true
                               );
            
               user2 = (User)m_userFactory.create(user2);
   
               user3 = new User(DataObject.NEW_ID,
                                CallContext.getInstance().getCurrentDomainId(),
                                "test_first_name_3",
                                "test_last_name_3",
                                "test_phone_3",
                                "test_fax_3",
                                "test_address_3",
                                "test_email_3",
                                "test_login_name_3",
                                "test_password_3",
                                true, true, true, true, null, null, true
                               );
            
               user3 = (User)m_userFactory.create(user3);
   
               user4 = new User(DataObject.NEW_ID,
                                CallContext.getInstance().getCurrentDomainId(),
                                "test_first_name_4",
                                "test_last_name_4",
                                "test_phone_4",
                                "test_fax_4",
                                "test_address_4",
                                "test_email_4",
                                "test_login_name_4",
                                "test_password_4",
                                true, true, false, true, null, null, true
                               );
            
               user4 = (User)m_userFactory.create(user4);
   
               session1 = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            user1.getId(),
                                            "test_generated_code_1",
                                            "test_c_IP_1",
                                            "test_client_type_1",
                                            null, true
                                            );
               
               session1 = (InternalSession)m_sessionFactory.create(session1);
               
               session2 = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            user2.getId(),
                                            "test_generated_code_2",
                                            "test_c_IP_2",
                                            "test_client_type_2",
                                            null, true
                                            );
               
               session2 = (InternalSession)m_sessionFactory.create(session2);
   
               session3 = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            user3.getId(),
                                            "test_generated_code_3",
                                            "test_c_IP_3",
                                            "test_client_type_3",
                                            null, true
                                            );
               
               session3 = (InternalSession)m_sessionFactory.create(session3);
   
               session4 = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            user4.getId(),
                                            "test_generated_code_4",
                                            "test_c_IP_4",
                                            "test_client_type_4",
                                            null, true
                                            );
               
               session4 = (InternalSession)m_sessionFactory.create(session4);
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // String representation of the selected sessions
            int[] arSelectedSessions = new int[] {session1.getId(), 
                                                  session2.getId(),
                                                  session4.getId(),
                                                 };
            
            // try to get count of superuser IDs that are not contained within the selected IDs
            iSuperUserCount = m_sessionFactory.getCountOfNotContainedSuperUsers(
                                                  arSelectedSessions);
   
            assertEquals("Superuser count is incorrect", 2, iSuperUserCount);
         }
         finally
         {
            m_transaction.begin();
            try
            {
               // delete sessions
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
               if ((session3 != null) && (session3.getId() != DataObject.NEW_ID))
               {
                  m_sessionFactory.delete(
                                      session3.getId(),
                                      CallContext.getInstance().getCurrentDomainId());
               }
               if ((session4 != null) && (session4.getId() != DataObject.NEW_ID))
               {
                  m_sessionFactory.delete(
                                      session4.getId(),
                                      CallContext.getInstance().getCurrentDomainId());
               }
               // delete users
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
               if ((user3 != null) && (user3.getId() != DataObject.NEW_ID))
               {
                  m_userFactory.delete(
                                   user3.getId(),
                                   CallContext.getInstance().getCurrentDomainId());
               }
               if ((user4 != null) && (user4.getId() != DataObject.NEW_ID))
               {
                  m_userFactory.delete(
                                   user4.getId(),
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
       * Test for creating internal session  
       * 
       * @throws Exception - an error has occurred
       */
      public void testCreate(
      ) throws Exception
      {
         User            user         = null;
         InternalSession data         = null;
         InternalSession testData     = null;
         InternalSession selectedData = null;
         
         PreparedStatement statement = null;
         ResultSet         results   = null;     
         String            strQuery;
   
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
                            true, false, true, true, null, null, true
                           );
         
            m_transaction.begin();
            try
            {
               user = (User)m_userFactory.create(user);
            
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            data = new InternalSession(DataObject.NEW_ID,
                                         CallContext.getInstance().getCurrentDomainId(),
                                         user.getId(),
                                         "test_generated_code",
                                         "test_c_IP",
                                         "test_client_type",
                                         null, true
                                         );
   
            m_transaction.begin();
            try 
            {
               testData = (InternalSession)m_sessionFactory.create(data);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            assertNotNull("Internal session should not be null", testData);
            assertTrue("New id wasn't generated.", testData.getId() != DataObject.NEW_ID); 
            assertNotNull("Creation timestamp was not generated", 
                     testData.getCreationTimestamp());
            assertTrue("Internal session is not the same", data.isSame(testData));
   
            // try to test created internal session from the DB 
            strQuery = m_sessionSchema.getSelectInternalSessionByCode(
                          m_intSessionDescriptor.getAllColumnCodes());
            try
            {                    
               statement = m_connection.prepareStatement(strQuery);
               statement.setString(1, testData.getInternalSession());
               results = statement.executeQuery();
               
               assertTrue("Inserted data were not found.", results.next());
               selectedData = (InternalSession)m_sessionFactory.load(results, 1);
               assertNotNull("Internal session should not be null", selectedData);
               assertEquals("Internal session is not the same", selectedData, testData);
               assertFalse("Only one data should have been created.", results.next());
            }
            finally
            {
               DatabaseUtils.closeResultSetAndStatement(results, statement);
            }
         }
         finally
         {
            m_transaction.begin();
            // delete inserted records
            try
            {
               if ((data != null) && (data.getId() != DataObject.NEW_ID))
               {
                  // delete session
                  m_sessionFactory.delete(
                                      data.getId(),
                                      CallContext.getInstance().getCurrentDomainId());
               }
               if ((user != null) && (user.getId() != DataObject.NEW_ID))
               {
                  // delete user
                  m_userFactory.delete(
                                   user.getId(),
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
       * Test for checking if there will be thrown unique constraint exception  
       * 
       * @throws Throwable - an error has occurred
       */
      public void testCheckUniqueConstraint(
      ) throws Throwable
      {
         User            user1      = null;
         User            user2      = null;
         InternalSession data1      = null;
         InternalSession data2      = null;
         InternalSession testData1  = null;
         InternalSession testData2  = null;
   
         try
         {
            // create 2 test users first
            user1 = new User(DataObject.NEW_ID,
                             CallContext.getInstance().getCurrentDomainId(),
                             "test_first_name",
                             "test_last_name",
                             "test_phone",
                             "test_fax",
                             "test_address",
                             "test_email",
                             "test_login_name",
                             "test_password",
                             true, false, true, true, null, null, true);
            user2 = new User(DataObject.NEW_ID,
                             CallContext.getInstance().getCurrentDomainId(),
                             "test_first_name2",
                             "test_last_name2",
                             "test_phone2",
                             "test_fax2",
                             "test_address2",
                             "test_email2",
                             "test_login_name2",
                             "test_password2",
                             true, false, true, true, null, null, true);
         
            m_transaction.begin();
            try
            {
               user1 = (User)m_userFactory.create(user1);
               user2 = (User)m_userFactory.create(user2);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw thr;
            }
            
            data1 = new InternalSession(DataObject.NEW_ID,
                                         CallContext.getInstance().getCurrentDomainId(),
                                         user1.getId(),
                                         "test_generated_code",
                                         "test_c_IP",
                                         "test_client_type",
                                         null, true
                                         );
   
            m_transaction.begin();
            try 
            {
               // try to create internal session
               testData1 = (InternalSession)m_sessionFactory.create(data1);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw thr;
            }
            
            assertNotNull("Create data  should not be null", testData1);
            assertTrue("Id was not generated", 
                       testData1.getId() != DataObject.NEW_ID);
            assertNotNull("Creation timestamp was not generated", 
                          testData1.getCreationTimestamp());
            assertTrue("Create data is not the same", data1.isSame(testData1));
   
            // At this point we have created data 1. Now we will try to create 
            // another data with the same internal session code. This should  
            // throw an exception due to constraint validation
            data2 = new InternalSession(DataObject.NEW_ID,
                                        CallContext.getInstance().getCurrentDomainId(),
                                        user2.getId(),
                                        testData1.getInternalSession(),
                                        "test_c_IP2",
                                        "test_client_type2",
                                        null, true);
            
            m_transaction.begin();
            try
            {
               testData2 = (InternalSession)m_sessionFactory.create(data2);
               m_transaction.commit();
               fail("It should not be possible to create two internal sessions"
                    + " with the same internal session code");
               assertNull("It should not be possible to create two internal"
                          + " sessions with the same internal session code", 
                          testData2);
            }
            catch (OSSInvalidDataException ideExc)
            {
               // This exception is expected, test if correct error is generated
               m_transaction.rollback();
               
               assertTrue("There should be message identifying violation of"
                          + " unique constraint for internal session code",
                          ideExc.getErrorMessages().containsMessage(
                             InternalSessionDataDescriptor.COL_INTSESSION_GEN_CODE,
                             "System generated nonunique session code. Try again."));
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw thr;
            }
         }
         finally
         {
            m_transaction.begin();
            // delete inserted records
            try
            {
               if ((testData1 != null) && (testData1.getId() != DataObject.NEW_ID))
               {
                  m_sessionFactory.delete(
                     testData1.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((testData2 != null) && (testData2.getId() != DataObject.NEW_ID))
               {
                  m_sessionFactory.delete(
                     testData2.getId(),
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
               throw thr;
            }
         }
      }
   
   
      /**
       * Test for deleting internal session by id  
       * 
       * @throws Exception - an error has occurred
       */
      public void testDeleteById(
      ) throws Exception
      {
         User            user         = null;
         InternalSession session  = null;
         
         PreparedStatement statement = null;
         ResultSet         results   = null;     
         String            strQuery;
   
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
                                true, false, true, true, null, null, true
                               );
            
               user = (User)m_userFactory.create(user);
            
               // create test internal session
               session = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            user.getId(),
                                            "test_generated_code",
                                            "test_c_IP",
                                            "test_client_type",
                                            null, true
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
               // try to delete internal session
               m_sessionFactory.delete(
                                   session.getId(),
                                   CallContext.getInstance().getCurrentDomainId());
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // try to get deleted internal session
            strQuery = "select GEN_CODE from " + SessionDatabaseSchema.INTSESSION_TABLE_NAME + " where GEN_CODE = ?";
            statement = m_connection.prepareStatement(strQuery);
            statement.setString(1, session.getInternalSession());
            results = statement.executeQuery();
         
            session = null;
             
            assertFalse("internal session should be deleted but was not", results.next());
         }
         finally
         {
            m_transaction.begin();
            // delete inserted records
            try
            {
               if ((session != null) && (session.getId() != DataObject.NEW_ID))
               {
                  // delete session
                  m_sessionFactory.delete(
                                      session.getId(),
                                      CallContext.getInstance().getCurrentDomainId());
               }
               if ((user != null) && (user.getId() != DataObject.NEW_ID))
               {
                  // delete user
                  m_userFactory.delete(
                                   user.getId(),
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
       * Test for deleting internal session by session code  
       * 
       * @throws Exception - an error has occurred
       */
      public void testDeleteByInternalSession(
      ) throws Exception
      {
         User            user         = null;
         InternalSession session  = null;
         
         PreparedStatement statement = null;
         ResultSet         results   = null;     
         String            strQuery;
   
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
                                true, false, true, true, null, null, true
                               );
            
               user = (User)m_userFactory.create(user);
            
               // create test internal session
               session = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            user.getId(),
                                            "test_generated_code",
                                            "test_c_IP",
                                            "test_client_type",
                                            null, true
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
               // try to delete internal session
               m_sessionFactory.delete(session.getInternalSession());
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // try to get deleted internal session
            strQuery = "select GEN_CODE from " + SessionDatabaseSchema.INTSESSION_TABLE_NAME + " where GEN_CODE = ?";
            statement = m_connection.prepareStatement(strQuery);
            statement.setString(1, session.getInternalSession());
            results = statement.executeQuery();
         
            session = null;
             
            assertFalse("internal session should be deleted but was not", results.next());
         }
         finally
         {
            m_transaction.begin();
            // delete inserted records
            try
            {
               if ((session != null) && (session.getId() != DataObject.NEW_ID))
               {
                  // delete session
                  m_sessionFactory.delete(
                                      session.getId(),
                                      CallContext.getInstance().getCurrentDomainId());
               }
               if ((user != null) && (user.getId() != DataObject.NEW_ID))
               {
                  // delete user
                  m_userFactory.delete(
                                   user.getId(),
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
       * Test all orphaned internal sessions  
       * 
       * @throws Exception - and error has occurred
       */
      public void testDeleteAllOrphans(
      ) throws Exception 
      {
         InternalSession session1     = null;
         InternalSession session2     = null;
         InternalSession testSession  = null;
         ExternalSession extSession1  = null;
         ExternalSession extSession2  = null;
   
         PreparedStatement statement = null;
         ResultSet         results   = null;     
         String            strQuery;
   
         try
         {
            m_transaction.begin();
            try
            {
               // create 2 test internal sessions
               session1 = new InternalSession(DataObject.NEW_ID,
                                             CallContext.getInstance().getCurrentDomainId(),
                                             CallContext.getInstance().getCurrentUserId(),
                                             "test_generated_code_1",
                                             "test_c_IP_1",
                                             "test_client_type_1",
                                             null, false
                                            );
               session1 = (InternalSession)m_sessionFactory.create(session1);
            
               session2 = new InternalSession(DataObject.NEW_ID,
                                             CallContext.getInstance().getCurrentDomainId(),
                                             CallContext.getInstance().getCurrentUserId(),
                                             "test_generated_code_2",
                                             "test_c_IP_2",
                                             "test_client_type_2",
                                             null, false
                                            );
               session2 = (InternalSession)m_sessionFactory.create(session2);
   
               // create 2 server sessions belonging to the internal session
               extSession1 = new ExternalSession(DataObject.NEW_ID,
                                                    CallContext.getInstance().getCurrentDomainId(),
                                                    session1.getId(),
                                                    "test_server_session_gen_code_1",
                                                    "test_srv_IP",
                                                    null
                                                   );
               extSession1 = (ExternalSession)m_externalSessionFactory.create(extSession1);
            
               extSession2 = new ExternalSession(DataObject.NEW_ID,
                                                    CallContext.getInstance().getCurrentDomainId(),
                                                    session1.getId(),
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
               // try to delete all empty internal sessions
               m_sessionFactory.deleteAllOrphans();
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            try
            {
               // try to get number of internal session after clean
               strQuery = "select count(ID) from " + SessionDatabaseSchema.INTSESSION_TABLE_NAME + " where ID in (?, ?)";
               statement = m_connection.prepareStatement(strQuery);
               statement.setInt(1, session1.getId());
               statement.setInt(2, session2.getId());
               results = statement.executeQuery();
               results.next();
   
               assertEquals("Incorrect number of internal sessions after clean",
                            1, results.getInt(1));
            }
            finally
            {
               DatabaseUtils.closeResultSetAndStatement(results, statement);
            }
            
            // try to get internal session
            testSession = (InternalSession)m_sessionFactory.get(
                             session2.getId(),
                             CallContext.getInstance().getCurrentDomainId());
      
            session2 = null;
            
            assertNull("Internal session should  be delete", testSession);
   
            // try to get internal session
            testSession = (InternalSession)m_sessionFactory.get(
                             session1.getId(),
                             CallContext.getInstance().getCurrentDomainId());
      
            assertNotNull("Internal session should not be null", testSession);
            assertEquals("Internal session is not the same", session1, testSession);
            
         }
         finally
         {
            m_transaction.begin();
            // delete inserted records
            try
            {
               if ((extSession1 != null) && (extSession1.getId() != DataObject.NEW_ID))
               {
                  // delete session
                  m_externalSessionFactory.delete(
                     extSession1.getId(), 
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((extSession2 != null) && (extSession2.getId() != DataObject.NEW_ID))
               {
                  // delete session
                  m_externalSessionFactory.delete(
                     extSession2.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((session1 != null) && (session1.getId() != DataObject.NEW_ID))
               {
                  // delete session
                  m_sessionFactory.delete(
                     session1.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((session2 != null) && (session2.getId() != DataObject.NEW_ID))
               {
                  // delete session
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
       * Test for delete of the list of internal sessions  
       * 
       * @throws Exception - an error has occurred
       */
      public void testDeleteList(
      ) throws Exception 
      {
         User            user         = null;
         InternalSession session1 = null;
         InternalSession session2 = null;
         int             iLoggedOut    = 0;
         
         PreparedStatement statement = null;
         ResultSet         results   = null;     
         String            strQuery;
   
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
                                true, false, true, true, null, null, true
                               );
            
               user = (User)m_userFactory.create(user);
            
               // create two test internal sessions
               session1 = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            user.getId(),
                                            "test_generated_code_1",
                                            "test_c_IP_1",
                                            "test_client_type_1",
                                            null, true
                                            );
               session1 = (InternalSession)m_sessionFactory.create(session1);
            
               session2 = new InternalSession(DataObject.NEW_ID,
                                            CallContext.getInstance().getCurrentDomainId(),
                                            user.getId(),
                                            "test_generated_code_2",
                                            "test_c_IP_2",
                                            "test_client_type_2",
                                            null, false
                                            );
               session2 = (InternalSession)m_sessionFactory.create(session2);
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            int[] arrIds = new int[] {session1.getId(), session2.getId()};
   
            m_transaction.begin();
            try
            {
               // try to logout internal sessions
               iLoggedOut = m_sessionFactory.delete(arrIds, SimpleRule.ALL_DATA);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            assertEquals("Number of session logged out is incorrect", 2, iLoggedOut);
            try
            {
               // try to get logged out sessions
               strQuery = "select ID from " + SessionDatabaseSchema.INTSESSION_TABLE_NAME + " where ID IN (?, ?)";
               statement = m_connection.prepareStatement(strQuery);
               statement.setInt(1, session1.getId());
               statement.setInt(2, session2.getId());
               results = statement.executeQuery();
               
               session1 = null;
               session2 = null;
         
               assertFalse("internal session should be deleted", results.next());
            }
            finally
            {
               DatabaseUtils.closeResultSetAndStatement(results, statement);
            }
         }
         finally
         {
            m_transaction.begin();
            // delete inserted records
            try
            {
               if ((session1 != null) && (session1.getId() != DataObject.NEW_ID))
               {
                  // delete session
                  m_sessionFactory.delete(
                     session1.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((session2 != null) && (session2.getId() != DataObject.NEW_ID))
               {
                  // delete session
                  m_sessionFactory.delete(
                     session2.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((user != null) && (user.getId() != DataObject.NEW_ID))
               {
                  // delete user
                  m_userFactory.delete(
                     user.getId(),
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
       * Test for delete list of internal sessions for all specified domains  
       * 
       * @throws Exception - an error has occurred
       */
      public void testDeleteListForDomains(
      ) throws Exception 
      {
         Domain          domain1   = null;
         Domain          domain2   = null;
         User            user1     = null;
         User            user2     = null;
         User            user3     = null;
         InternalSession session11 = null;
         InternalSession session12 = null;
         InternalSession session21 = null;
         InternalSession session22 = null;
         InternalSession session31 = null;
         InternalSession session32 = null;
         int             iLoggedOut    = 0;
         
         PreparedStatement statement = null;
         ResultSet         results   = null;     
         String            strQuery;
   
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
                                true, false, true, true, null, null, true
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
                                true, false, true, true, null, null, true
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
                                true, false, true, true, null, null, true
                               );
               user3 = (User)m_userFactory.create(user3);

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
               iLoggedOut = m_sessionFactory.deleteAllForDomains(arrDomainIds, 
                                                                 SimpleRule.ALL_DATA);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            assertEquals("Number of session logged out is incorrect", 4, iLoggedOut);
            try
            {
               // try to get logged out sessions
               strQuery = "select ID from " + SessionDatabaseSchema.INTSESSION_TABLE_NAME + " where ID IN (?, ?, ?, ?)";
               statement = m_connection.prepareStatement(strQuery);
               statement.setInt(1, session21.getId());
               statement.setInt(2, session22.getId());
               statement.setInt(3, session31.getId());
               statement.setInt(4, session32.getId());
               results = statement.executeQuery();
               
               assertFalse("Internal sessions should be deleted", results.next());
               session21 = null;
               session22 = null;
               session31 = null;
               session32 = null;
            }
            finally
            {
               DatabaseUtils.closeResultSetAndStatement(results, statement);
            }
            try
            {
               // try to get not logged out sessions
               strQuery = "select ID from " + SessionDatabaseSchema.INTSESSION_TABLE_NAME + " where ID IN (?, ?)";
               statement = m_connection.prepareStatement(strQuery);
               statement.setInt(1, session11.getId());
               statement.setInt(2, session12.getId());
               results = statement.executeQuery();
               
               assertTrue("Internal session should not be deleted", results.next());
            }
            finally
            {
               DatabaseUtils.closeResultSetAndStatement(results, statement);
            }
         }
         finally
         {
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
               if ((session21 != null) && (session21.getId() != DataObject.NEW_ID))
               {
                  // delete session21
                  m_sessionFactory.delete(
                     session21.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((session22 != null) && (session22.getId() != DataObject.NEW_ID))
               {
                  // delete session22
                  m_sessionFactory.delete(
                     session22.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((session31 != null) && (session31.getId() != DataObject.NEW_ID))
               {
                  // delete session31
                  m_sessionFactory.delete(
                     session31.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((session32 != null) && (session32.getId() != DataObject.NEW_ID))
               {
                  // delete session32
                  m_sessionFactory.delete(
                     session32.getId(),
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
                  strQuery = "delete from " + UserDatabaseSchema.USER_TABLE_NAME + " where ID in (?, ?)";
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
       * Test check if all sessions can be logged out from all specfied domains  
       * 
       * @throws Exception - an error has occurred
       */
      public void testCheckLogoutDomains(
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
         boolean         bCanLogout = false;

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
                           m_intSessionDescriptor.getDataType(), 
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
   
            // try to check if internal sessions for domain1 and domain2 can be logged out
            bCanLogout = m_sessionFactory.checkLogoutDomains(arrDomainIds, 
                                                             SimpleRule.ALL_DATA);
            assertTrue("Incorrect value for 1st check logout domains", bCanLogout);
            
            // set new user and session info
            CallContext.getInstance().setCurrentUserAndSession(user1, 
                                                session11.getInternalSession());
            
            SimpleRule securityData = m_authorizeControl.getRightsForCurrentUser(
                          m_intSessionDescriptor.getDataType(),
                          ActionConstants.RIGHT_ACTION_VIEW);

            // try to check if internal sessions for domain1 and domain2 can be logged out
            bCanLogout = m_sessionFactory.checkLogoutDomains(arrDomainIds, securityData);
            assertFalse("Incorrect value for 2nd check logout domains", bCanLogout);

            CallContext.getInstance().resetCurrentUserAndSession();
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
                  strQuery = "delete from " + SessionDatabaseSchema.INTSESSION_TABLE_NAME + " where ID in (?, ?, ?, ?)";
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
                  // delete access rights
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
                  strQuery = "delete from " + UserDatabaseSchema.USER_TABLE_NAME + " where ID in (?, ?)";
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
       * Test for updating and enabling users by session id
       * 
       * @throws Exception - an error has occurred
       */
      public void testUpdateEnable(
      ) throws Exception 
      {
         User            user1        = null;
         User            user2        = null;
         User            user3        = null;
         InternalSession session1 = null;
         InternalSession session2 = null;
         InternalSession session3 = null;
         int             iUpdated      = 0;
         
         PreparedStatement statement = null;
         ResultSet         results   = null;     
         String            strQuery;
   
         Timestamp updatedUserTmstp1;
         Timestamp updatedUserTmstp2;
         Timestamp updatedUserTmstp3;
   
         Timestamp updated2UserTmstp1;
         Timestamp updated2UserTmstp2;
         Timestamp updated2UserTmstp3;
   
         try
         {
            m_transaction.begin();
            try
            {
               // create 3 test users first
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
                                 false, // disabled
                                 false, true, true, null, null, true
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
                                 true, // enabled
                                 false, true, true, null, null, true
                                );
            
               user2 = (User)m_userFactory.create(user2);
   
               user3 = new User(DataObject.NEW_ID,
                                 CallContext.getInstance().getCurrentDomainId(),
                                 "test_first_name_3",
                                 "test_last_name_3",
                                 "test_phone_3",
                                 "test_fax_3",
                                 "test_address_3",
                                 "test_email_3",
                                 "test_login_name_3",
                                 "test_password_3",
                                 false, // disabled
                                 false, true, true, null, null, true
                                );
            
               user3 = (User)m_userFactory.create(user3);
   
               // create 3 test internal sessions
               session1 = new InternalSession(DataObject.NEW_ID,
                                              CallContext.getInstance().getCurrentDomainId(),
                                              user1.getId(),
                                              "test_generated_code_1",
                                              "test_c_IP_1",
                                              "test_client_type_1",
                                              null, true
                                             );
               session1 = (InternalSession)m_sessionFactory.create(session1);
   
               session2 = new InternalSession(DataObject.NEW_ID,
                                              CallContext.getInstance().getCurrentDomainId(),
                                              user2.getId(),
                                              "test_generated_code_2",
                                              "test_c_IP_2",
                                              "test_client_type_2",
                                              null, false
                                             );
               session2 = (InternalSession)m_sessionFactory.create(session2);
   
               session3 = new InternalSession(DataObject.NEW_ID,
                                              CallContext.getInstance().getCurrentDomainId(),
                                              user3.getId(),
                                              "test_generated_code_3",
                                              "test_c_IP_3",
                                              "test_client_type_3",
                                              null, false
                                             );
               session3 = (InternalSession)m_sessionFactory.create(session3);
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            int[] arrIds = new int[] {session1.getId(), session2.getId()};
            
            // sleep for some time the modification date can be updated
            Thread.sleep(1000);
   
            m_transaction.begin();
            try
            {
               // try to enable users
               iUpdated = m_sessionFactory.updateEnable(arrIds, true, 
                                                        SimpleRule.ALL_DATA);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            assertEquals("Number of enabled users is incorrect", 2, iUpdated);
            try
            {
               // try to get enabled users from the DB
               strQuery = "select count(ID) from " 
                          + UserDatabaseSchema.USER_TABLE_NAME 
                          + " where ID IN (?, ?, ?) " 
                          + "and LOGIN_ENABLED = 1";
               statement = m_connection.prepareStatement(strQuery);
               statement.setInt(1, user1.getId());
               statement.setInt(2, user2.getId());
               statement.setInt(3, user3.getId());
               results = statement.executeQuery();
               results.next();
         
               assertEquals("Two users should be enabled but were not", 2, 
                            results.getInt(1));
               DatabaseUtils.closeResultSetAndStatement(results, statement);
               results = null;
               statement = null;
   
               // try to verify all enabled user flags
               strQuery = "select ID, LOGIN_NAME, LOGIN_ENABLED,"
                          + " MODIFICATION_DATE from " 
                          + UserDatabaseSchema.USER_TABLE_NAME 
                          + " where ID in (?, ?, ?) order by ID";
               statement = m_connection.prepareStatement(strQuery);
               statement.setInt(1, user1.getId());
               statement.setInt(2, user2.getId());
               statement.setInt(3, user3.getId());
               results = statement.executeQuery();
               
               assertTrue("User 1 should be found in the DB but was not", 
                          results.next());
               assertEquals("User 1 ID is incorrect", user1.getId(), 
                            results.getInt("ID"));
               assertEquals("User 1 login should not be changed", 
                            user1.getName(), results.getString("LOGIN_NAME"));
               assertEquals("User 1 login was not enabled", 
                            1, results.getInt("LOGIN_ENABLED"));
               updatedUserTmstp1 = results.getTimestamp("MODIFICATION_DATE");
               assertTrue("User 1 modification time was not changed. Before "
                          + user1.getModificationTimestamp().toString()
                          + " after " + updatedUserTmstp1.toString(),
                          user1.getModificationTimestamp().before(updatedUserTmstp1));
   
               assertTrue("User 2 should be found in the DB but was not", 
                          results.next());
               assertEquals("User 2 ID is incorrect", 
                            user2.getId(), results.getInt("ID"));
               assertEquals("User 2 login should not be changed", 
                            user2.getName(), results.getString("LOGIN_NAME"));
               assertEquals("User 2 login should be enabled", 
                            1, results.getInt("LOGIN_ENABLED"));
               updatedUserTmstp2 = results.getTimestamp("MODIFICATION_DATE");
               assertTrue("User 2 modification time was not changed. Before "
                          + user2.getModificationTimestamp().toString()
                          + " after " + updatedUserTmstp2.toString(),
                          user2.getModificationTimestamp().before(
                                   updatedUserTmstp2));
   
               assertTrue("User 3 should be found in the DB but was not", 
                          results.next());
               assertEquals("User 3 ID is incorrect", 
                            user3.getId(), results.getInt("ID"));
               assertEquals("User 3 login should not be changed", 
                            user3.getName(), results.getString("LOGIN_NAME"));
               assertEquals("User 3 login shoul be disabled", 
                            0, results.getInt("LOGIN_ENABLED"));
               updatedUserTmstp3 = results.getTimestamp("MODIFICATION_DATE");
               assertTrue("User 3 modification should be not changed. Before "
                          + user3.getModificationTimestamp().toString()
                          + " after " + updatedUserTmstp3.toString(),
                          user3.getModificationTimestamp().equals(
                                   updatedUserTmstp3));
            }
            finally
            {
               DatabaseUtils.closeResultSetAndStatement(results, statement);
            }
   
            // sleep before the 2nd update
            Thread.sleep(1000);
   
            m_transaction.begin();
            try
            {
               // try to disable users
               iUpdated = m_sessionFactory.updateEnable(arrIds, false, 
                                                        SimpleRule.ALL_DATA);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            assertEquals("Number of disabled users is incorrect", 2, iUpdated);
            try
            {
               // try to get enabled users from the DB
               strQuery = "select count(ID) from " 
                          + UserDatabaseSchema.USER_TABLE_NAME 
                          + " where ID IN (?, ?, ?) " 
                          + "and LOGIN_ENABLED = 0";
               statement = m_connection.prepareStatement(strQuery);
               statement.setInt(1, user1.getId());
               statement.setInt(2, user2.getId());
               statement.setInt(3, user3.getId());
               results = statement.executeQuery();
               results.next();
         
               assertEquals("Three users should be disabled but are not", 
                            3, results.getInt(1));
               DatabaseUtils.closeResultSetAndStatement(results, statement);
               results = null;
               statement = null;
   
               // try to verify all enabled user flags
               strQuery = "select ID, LOGIN_NAME, LOGIN_ENABLED,"
                          + " MODIFICATION_DATE from " 
                          + UserDatabaseSchema.USER_TABLE_NAME 
                          + " where ID in (?, ?, ?) order by ID";
               statement = m_connection.prepareStatement(strQuery);
               statement.setInt(1, user1.getId());
               statement.setInt(2, user2.getId());
               statement.setInt(3, user3.getId());
               results = statement.executeQuery();
               
               assertTrue("User 1 should be found in the DB but was not", 
                          results.next());
               assertEquals("User 1 ID is incorrect", 
                            user1.getId(), results.getInt("ID"));
               assertEquals("User 1 login should not be changed", 
                            user1.getName(), results.getString("LOGIN_NAME"));
               assertEquals("User 1 login was not disabled", 
                            0, results.getInt("LOGIN_ENABLED"));
               // Check if there was changed modification timestamp and it's 
               // time is after the last enabled user time
               updated2UserTmstp1 = results.getTimestamp("MODIFICATION_DATE");
               assertTrue("User 1 modification time was not changed. Before "
                          + updatedUserTmstp1.toString()
                          + " after " + updated2UserTmstp1.toString(),
                          updatedUserTmstp1.before(updated2UserTmstp1));
   
               assertTrue("User 2 should be found in the DB but was not", 
                          results.next());
               assertEquals("User 2 ID is incorrect", 
                            user2.getId(), results.getInt("ID"));
               assertEquals("User 2 login should not be changed", 
                            user2.getName(), results.getString("LOGIN_NAME"));
               assertEquals("User 2 login was not disabled", 
                            0, results.getInt("LOGIN_ENABLED"));
               // Check if there was changed modification timestamp and it's 
               // time is after the last enabled user time
               updated2UserTmstp2 = results.getTimestamp("MODIFICATION_DATE");
               assertTrue("User 2 modification time was not changed. Before "
                          + updatedUserTmstp2.toString()
                          + " after " + updated2UserTmstp2.toString(),
                          updatedUserTmstp2.before(updated2UserTmstp2));
   
               assertTrue("User 3 should be found in the DB but was not", 
                          results.next());
               assertEquals("User 3 ID is incorrect", 
                            user3.getId(), results.getInt("ID"));
               assertEquals("User 3 login should not be changed", 
                            user3.getName(), results.getString("LOGIN_NAME"));
               assertEquals("User 3 login shoul be disabled", 
                            0, results.getInt("LOGIN_ENABLED"));
               // Check if there was not changed modification timestamp (this 
               // user was not disabled)
               updated2UserTmstp3 = results.getTimestamp("MODIFICATION_DATE");
               assertTrue("User 3 modification should be not changed. Before "
                          + updatedUserTmstp3
                          + " after " + updated2UserTmstp3.toString(),
                          user3.getModificationTimestamp().equals(
                                   updated2UserTmstp3));
            }
            finally
            {
               DatabaseUtils.closeResultSetAndStatement(results, statement);
            }
         }
         finally
         {
            m_transaction.begin();
            // delete inserted records
            try
            {
               if ((session1 != null) && (session1.getId() != DataObject.NEW_ID))
               {
                  // delete session
                  m_sessionFactory.delete(
                     session1.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((session2 != null) && (session2.getId() != DataObject.NEW_ID))
               {
                  // delete session
                  m_sessionFactory.delete(
                     session2.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((session3 != null) && (session3.getId() != DataObject.NEW_ID))
               {
                  // delete session
                  m_sessionFactory.delete(
                     session3.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((user1 != null) && (user1.getId() != DataObject.NEW_ID))
               {
                  // delete user
                  m_userFactory.delete(
                     user1.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((user2 != null) && (user2.getId() != DataObject.NEW_ID))
               {
                  // delete user
                  m_userFactory.delete(
                     user2.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((user3 != null) && (user3.getId() != DataObject.NEW_ID))
               {
                  // delete user
                  m_userFactory.delete(
                     user3.getId(),
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
