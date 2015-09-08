/*
 * Copyright (c) 2004 - 2009 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: SessionControllerSecurityTest.java,v 1.18 2009/09/20 05:32:57 bastafidli Exp $
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

import java.util.ArrayList;
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
import org.opensubsystems.patterns.listdata.data.DataCondition;
import org.opensubsystems.security.application.SecurityBackendModule;
import org.opensubsystems.security.data.AccessRight;
import org.opensubsystems.security.data.ExternalSession;
import org.opensubsystems.security.data.ExternalSessionDataDescriptor;
import org.opensubsystems.security.data.InternalSession;
import org.opensubsystems.security.data.InternalSessionDataDescriptor;
import org.opensubsystems.security.data.Role;
import org.opensubsystems.security.data.SessionView;
import org.opensubsystems.security.data.User;
import org.opensubsystems.security.data.UserDataDescriptor;
import org.opensubsystems.security.patterns.listdata.logic.impl.SecureListControllerTest;
import org.opensubsystems.security.persist.AccessRightFactory;
import org.opensubsystems.security.persist.ExternalSessionFactory;
import org.opensubsystems.security.persist.InternalSessionFactory;
import org.opensubsystems.security.persist.RoleFactory;
import org.opensubsystems.security.persist.SessionViewFactory;
import org.opensubsystems.security.persist.UserFactory;
import org.opensubsystems.security.persist.db.AccessRightDatabaseFactory;
import org.opensubsystems.security.persist.db.UserListDatabaseTestUtils;
import org.opensubsystems.security.util.ActionConstants;

/**
 * Test for security facade above the persistence layer. Each method of session
 * controller check if user was granted access rights required to perform any
 * given operation. We do not test, if the security checks are performed correctly 
 * and if they work at all. 
 * 
 * @version $Id: SessionControllerSecurityTest.java,v 1.18 2009/09/20 05:32:57 bastafidli Exp $
 * @author Julo Legeny
 */
public final class SessionControllerSecurityTest extends Tests
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private SessionControllerSecurityTest(
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
      TestSuite suite = new DatabaseTestSuite("SessionControllerSecurityTest");
      suite.addTestSuite(SessionControllerSecurityTestInternal.class);
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
   public static class SessionControllerSecurityTestInternal 
                 extends SecureListControllerTest
   {
      // Cached values ////////////////////////////////////////////////////////////
   
      /**
       * Factory to manage access rights.
       */
      protected AccessRightDatabaseFactory m_rightFactory;
   
      /**
       * Factory to manage roles.
       */
      protected RoleFactory m_roleFactory;
   
      /**
       * Factory to manage users.
       */
      protected UserFactory m_userFactory;
   
      /**
       * Factory to use to execute persistence operations.
       */
      protected InternalSessionFactory m_internalSessionFactory;
   
      /**
       * Factory to use to execute persistence operations.
       */
      protected ExternalSessionFactory m_externalSessionFactory;
   
      /**
       * Factory to use to execute persistence operations.
       */
      protected SessionViewFactory m_sessionViewFactory;
   
      /**
       * Controller to manage sessions.
       */
      protected SessionController m_sessionControl;
   
      /**
       * Data type assigned to user data types.
       */
      protected int m_iUserDataType;
      
      /**
       * Data type assigned to internal session data types.
       */
      protected int m_iIntSessionDataType;
   
      /**
       * Data type assigned to external session data types.
       */
      protected int m_iExtSessionDataType;
   
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
      public SessionControllerSecurityTestInternal(
         String strTestName
      ) throws Exception
      {
         super(strTestName, new UserListDatabaseTestUtils());
   
         m_rightFactory = (AccessRightDatabaseFactory)DataFactoryManager.getInstance(
                                         AccessRightFactory.class);
         m_roleFactory = (RoleFactory)DataFactoryManager.getInstance(
                                         RoleFactory.class);      
         m_userFactory = (UserFactory)DataFactoryManager.getInstance(
                                         UserFactory.class);
         m_internalSessionFactory = (InternalSessionFactory)DataFactoryManager.getInstance(
                                                               InternalSessionFactory.class);
         m_externalSessionFactory = (ExternalSessionFactory)DataFactoryManager.getInstance(
                                                               ExternalSessionFactory.class);
         m_sessionViewFactory = (SessionViewFactory)DataFactoryManager.getInstance(
                                                       SessionViewFactory.class);
         m_sessionControl = (SessionController)ControllerManager.getInstance(
                                                   SessionController.class);
         
         UserDataDescriptor userDescriptor;
         
         userDescriptor = (UserDataDescriptor)DataDescriptorManager.getInstance(
                                                 UserDataDescriptor.class);
         
         m_iUserDataType = userDescriptor.getDataType();
         
         InternalSessionDataDescriptor intSessionDescriptor;
         
         intSessionDescriptor = (InternalSessionDataDescriptor)DataDescriptorManager
                                   .getInstance(
                                      InternalSessionDataDescriptor.class);
         
         m_iIntSessionDataType = intSessionDescriptor.getDataType();
         
         ExternalSessionDataDescriptor extSessionDescriptor;
         
         extSessionDescriptor = (ExternalSessionDataDescriptor)DataDescriptorManager
                                   .getInstance(
                                      ExternalSessionDataDescriptor.class);
         
         m_iExtSessionDataType = extSessionDescriptor.getDataType();
      }
   
      // Tests ////////////////////////////////////////////////////////////////////
   
   
      /**
       * Test for getting of the role. User have action granted for all data objects 
       * of given type (no id and no categories specified when granting access).
       * 
       * @throws Exception - an error has occurred
       */
      public void testGetSessionsCheck(
      ) throws Exception 
      {
         User            curUser         = null;
         Role            userRole        = null;
         User            user1           = null;
         User            user2           = null;
         InternalSession session1        = null;
         InternalSession session2        = null;
         ExternalSession extsession1     = null;
         ExternalSession extsession2     = null;
         ExternalSession extsession3     = null;
         ExternalSession extsession4     = null;
         Object[]        arrTestData     = null;
         Object[]        arrTestUserData = null;
         Object[]        arrTestSessData = null;
         Object[]        arrReturned     = null;
         List            lstExtSessions  = null;
         AccessRight     accessRightView = null;
         SessionView     sesView         = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_roleFactory.createPersonal(curUser);
   
            m_transaction.begin();
            try
            {
               // create 2 test users first
               arrTestUserData = constructTestUsers();
               user1 = (User)arrTestUserData[0];
               user2 = (User)arrTestUserData[1];
               user1 = (User)m_userFactory.create(user1);
               user2 = (User)m_userFactory.create(user2);
               // create 2 int. sessions belonging to each user one session
               arrTestSessData = constructTestSessions(user1.getId(), user2.getId());
               session1 = (InternalSession)arrTestSessData[0];
               session2 = (InternalSession)arrTestSessData[1];
               session1 = (InternalSession)m_internalSessionFactory.create(session1);
               session2 = (InternalSession)m_internalSessionFactory.create(session2);
               // create 4 external sessions belonging to each int. session 2 ext. sessions
               arrTestData = constructTestData(session1.getId(), session2.getId());
               extsession1 = (ExternalSession)arrTestData[0];
               extsession2 = (ExternalSession)arrTestData[1];
               extsession3 = (ExternalSession)arrTestData[2];
               extsession4 = (ExternalSession)arrTestData[3];
               extsession1 = (ExternalSession)m_externalSessionFactory.create(extsession1);
               extsession2 = (ExternalSession)m_externalSessionFactory.create(extsession2);
               extsession3 = (ExternalSession)m_externalSessionFactory.create(extsession3);
               extsession4 = (ExternalSession)m_externalSessionFactory.create(extsession4);
   
               // create role to the curUser
               userRole = new Role(DataObject.NEW_ID, 
                                   iCurrentDomainID, 
                                   "testname", 
                                   "testdescription", 
                                   true, 
                                   curUser.getId(), 
                                   false, null, null, null);
               userRole = (Role)m_roleFactory.create(userRole);
   
               m_roleFactory.assignToUser(curUser.getId(),
                                          Integer.toString(userRole.getId()));
   
               // create access right that curUser can view all session object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iIntSessionDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             AccessRight.NO_RIGHT_IDENTIFIER,
                                             null, null);
               m_rightFactory.create(accessRightView);          
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            //------------------------
            // 1. try to get sessions for int. session 1
            //------------------------
            // sessions have to be retrieved 
            arrReturned = m_sessionControl.getSessions(session1.getId());
            sesView = (SessionView)arrReturned[0];
            lstExtSessions = (ArrayList)arrReturned[1];
            assertNotNull("Session view should be not null", sesView);
            assertNotNull("List of external sessions has to be not null", lstExtSessions);
            assertEquals("List of external sessions has incorrect size ", 2, lstExtSessions.size());
   
            //------------------------
            // 2. try to get sessions for int. session 2
            //------------------------
            // sessions have to be retrieved 
            arrReturned = m_sessionControl.getSessions(session2.getId());
            sesView = (SessionView)arrReturned[0];
            lstExtSessions = (ArrayList)arrReturned[1];
            assertNotNull("Session view should be not null", sesView);
            assertNotNull("List of external sessions has to be not null", lstExtSessions);
            assertEquals("List of external sessions has incorrect size ", 2, lstExtSessions.size());
   
         }
         finally
         {
            deleteTestData(user1, user2, userRole, session1, session2, 
                           extsession1, extsession2, extsession3, extsession4);
         }
      }
   
      /**
       * Test for getting of the role. User have access granted to specific
       * data object (identified by identifier) of given data type.
       * 
       * @throws Exception - an error has occurred
       */
      public void testGetSessionsCheckId(
      ) throws Exception 
      {
         User            curUser         = null;
         Role            userRole        = null;
         User            user1           = null;
         User            user2           = null;
         InternalSession session1        = null;
         InternalSession session2        = null;
         ExternalSession extsession1     = null;
         ExternalSession extsession2     = null;
         ExternalSession extsession3     = null;
         ExternalSession extsession4     = null;
         Object[]        arrTestData     = null;
         Object[]        arrTestUserData = null;
         Object[]        arrTestSessData = null;
         Object[]        arrReturned     = null;
         List            lstExtSessions  = null;
         AccessRight     accessRightView = null;
         SessionView     sesView         = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_roleFactory.createPersonal(curUser);
   
            m_transaction.begin();
            try
            {
               // create 2 test users first
               arrTestUserData = constructTestUsers();
               user1 = (User)arrTestUserData[0];
               user2 = (User)arrTestUserData[1];
               user1 = (User)m_userFactory.create(user1);
               user2 = (User)m_userFactory.create(user2);
               // create 2 int. sessions belonging to each user one session
               arrTestSessData = constructTestSessions(user1.getId(), user2.getId());
               session1 = (InternalSession)arrTestSessData[0];
               session2 = (InternalSession)arrTestSessData[1];
               session1 = (InternalSession)m_internalSessionFactory.create(session1);
               session2 = (InternalSession)m_internalSessionFactory.create(session2);
               // create 4 external sessions belonging to each int. session 2 ext. sessions
               arrTestData = constructTestData(session1.getId(), session2.getId());
               extsession1 = (ExternalSession)arrTestData[0];
               extsession2 = (ExternalSession)arrTestData[1];
               extsession3 = (ExternalSession)arrTestData[2];
               extsession4 = (ExternalSession)arrTestData[3];
               extsession1 = (ExternalSession)m_externalSessionFactory.create(extsession1);
               extsession2 = (ExternalSession)m_externalSessionFactory.create(extsession2);
               extsession3 = (ExternalSession)m_externalSessionFactory.create(extsession3);
               extsession4 = (ExternalSession)m_externalSessionFactory.create(extsession4);
   
               // create role to the curUser
               userRole = new Role(DataObject.NEW_ID, 
                                   iCurrentDomainID, 
                                   "testname", 
                                   "testdescription", 
                                   true, 
                                   curUser.getId(), 
                                   false, null, null, null);
               userRole = (Role)m_roleFactory.create(userRole);
   
               m_roleFactory.assignToUser(curUser.getId(),
                                          Integer.toString(userRole.getId()));
   
               // create access right that curUser can view only session1 object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iIntSessionDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             session1.getId(),
                                             null, null);
               m_rightFactory.create(accessRightView);          
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            //------------------------
            // 1. try to get sessions for int. session 1
            //------------------------
            // sessions have to be retrieved 
            arrReturned = m_sessionControl.getSessions(session1.getId());
            sesView = (SessionView)arrReturned[0];
            lstExtSessions = (ArrayList)arrReturned[1];
            assertNotNull("Session view for int. session 1 should be not null", sesView);
            assertNotNull("List of external sessions for int. session 1 has to be not null", 
                          lstExtSessions);
            assertEquals("List of external sessions for int. session 2 has incorrect size ", 
                          2, lstExtSessions.size());
   
            //------------------------
            // 2. try to get sessions for int. session 2
            //------------------------
            // sessions have not be retrieved 
            arrReturned = m_sessionControl.getSessions(session2.getId());
            sesView = (SessionView)arrReturned[0];
            lstExtSessions = (ArrayList)arrReturned[1];
            assertNull("Session view for int. session 2 should not be retrieved because " +
                       "of access right limitation", sesView);
            assertNull("List of external sessions for int. session 2 has not be retrieved " +
                       "because of access right limitation", lstExtSessions);
         }
         finally
         {
            deleteTestData(user1, user2, userRole, session1, session2, 
                           extsession1, extsession2, extsession3, extsession4);
         }
      }
   
      /**
       * Test for logout of the user (deleting of user's int.session and all related ext.sessions).
       * User have action granted for all data objects of given type (no id and no categories 
       * specified when granting access).
       * 
       * @throws Exception - an error has occurred
       */
      public void testLogoutCheck(
      ) throws Exception 
      {
         User            curUser         = null;
         Role            userRole        = null;
         User            user1           = null;
         User            user2           = null;
         InternalSession session1        = null;
         InternalSession session2        = null;
         InternalSession testSession     = null;
         ExternalSession extsession1     = null;
         ExternalSession extsession2     = null;
         ExternalSession extsession3     = null;
         ExternalSession extsession4     = null;
         Object[]        arrTestData     = null;
         Object[]        arrTestUserData = null;
         Object[]        arrTestSessData = null;
         List            lstExtSessions  = null;
         AccessRight     accessRightView = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_roleFactory.createPersonal(curUser);
   
            m_transaction.begin();
            try
            {
               // create 2 test users first
               arrTestUserData = constructTestUsers();
               user1 = (User)arrTestUserData[0];
               user2 = (User)arrTestUserData[1];
               user1 = (User)m_userFactory.create(user1);
               user2 = (User)m_userFactory.create(user2);
               // create 2 int. sessions belonging to each user one session
               arrTestSessData = constructTestSessions(user1.getId(), user2.getId());
               session1 = (InternalSession)arrTestSessData[0];
               session2 = (InternalSession)arrTestSessData[1];
               session1 = (InternalSession)m_internalSessionFactory.create(session1);
               session2 = (InternalSession)m_internalSessionFactory.create(session2);
               // create 4 external sessions belonging to each int. session 2 ext. sessions
               arrTestData = constructTestData(session1.getId(), session2.getId());
               extsession1 = (ExternalSession)arrTestData[0];
               extsession2 = (ExternalSession)arrTestData[1];
               extsession3 = (ExternalSession)arrTestData[2];
               extsession4 = (ExternalSession)arrTestData[3];
               extsession1 = (ExternalSession)m_externalSessionFactory.create(extsession1);
               extsession2 = (ExternalSession)m_externalSessionFactory.create(extsession2);
               extsession3 = (ExternalSession)m_externalSessionFactory.create(extsession3);
               extsession4 = (ExternalSession)m_externalSessionFactory.create(extsession4);
   
               // create role to the curUser
               userRole = new Role(DataObject.NEW_ID, 
                                   iCurrentDomainID, 
                                   "testname", 
                                   "testdescription", 
                                   true, 
                                   curUser.getId(), 
                                   false, null, null, null);
               userRole = (Role)m_roleFactory.create(userRole);
   
               m_roleFactory.assignToUser(curUser.getId(),
                                          Integer.toString(userRole.getId()));
   
               // create access right that curUser can delete all int.session objects
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_DELETE,
                                             m_iIntSessionDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             AccessRight.NO_RIGHT_IDENTIFIER,
                                             null, null);
               m_rightFactory.create(accessRightView);          
   
               // create access right that curUser can view all int.session objects
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iIntSessionDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             session1.getId(),
                                             null, null);
               m_rightFactory.create(accessRightView);  
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            //------------------------
            // 1. try to logout for int. session 1
            //------------------------
            m_transaction.begin();
            try
            {
               m_sessionControl.logout(session1.getInternalSession());
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // sessions have to be logged out
            testSession = m_internalSessionFactory.get(session1.getInternalSession());
            assertNull("Internal session 1 should be logged out", testSession);
            lstExtSessions = m_externalSessionFactory.getAll(session1.getId());
            assertTrue("External sessions belonging int.session 1 should be logged out", 
                       (lstExtSessions == null || lstExtSessions.isEmpty()));
            session1 = null;
            extsession1 = null;
            extsession2 = null;
   
            //------------------------
            // 2. try to logout for int. session 2
            //------------------------
            m_transaction.begin();
            try
            {
               m_sessionControl.logout(session2.getInternalSession());
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // sessions have to be logged out 
            testSession = m_internalSessionFactory.get(session2.getInternalSession());
            assertNull("Internal session 2 should be logged out", testSession);
            lstExtSessions = m_externalSessionFactory.getAll(session2.getId());
            assertTrue("External sessions belonging int.session 2 should be logged out", 
                       (lstExtSessions == null || lstExtSessions.isEmpty()));
            session2 = null;
            extsession3 = null;
            extsession4 = null;
         }
         finally
         {
            deleteTestData(user1, user2, userRole, session1, session2, 
                           extsession1, extsession2, extsession3, extsession4);
         }
      }
   
      /**
       * Test for logout of the user (deleting of user's int.session and all related ext.sessions).
       * User have access granted to specific data object (identified by identifier) of given 
       * data type.
       * 
       * @throws Exception - an error has occurred
       */
      public void testLogoutCheckId(
      ) throws Exception 
      {
         User            curUser         = null;
         Role            userRole        = null;
         User            user1           = null;
         User            user2           = null;
         InternalSession session1        = null;
         InternalSession session2        = null;
         InternalSession testSession     = null;
         ExternalSession extsession1     = null;
         ExternalSession extsession2     = null;
         ExternalSession extsession3     = null;
         ExternalSession extsession4     = null;
         Object[]        arrTestData     = null;
         Object[]        arrTestUserData = null;
         Object[]        arrTestSessData = null;
         List            lstExtSessions  = null;
         AccessRight     accessRightView = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_roleFactory.createPersonal(curUser);
   
            m_transaction.begin();
            try
            {
               // create 2 test users first
               arrTestUserData = constructTestUsers();
               user1 = (User)arrTestUserData[0];
               user2 = (User)arrTestUserData[1];
               user1 = (User)m_userFactory.create(user1);
               user2 = (User)m_userFactory.create(user2);
               // create 2 int. sessions belonging to each user one session
               arrTestSessData = constructTestSessions(user1.getId(), user2.getId());
               session1 = (InternalSession)arrTestSessData[0];
               session2 = (InternalSession)arrTestSessData[1];
               session1 = (InternalSession)m_internalSessionFactory.create(session1);
               session2 = (InternalSession)m_internalSessionFactory.create(session2);
               // create 4 external sessions belonging to each int. session 2 ext. sessions
               arrTestData = constructTestData(session1.getId(), session2.getId());
               extsession1 = (ExternalSession)arrTestData[0];
               extsession2 = (ExternalSession)arrTestData[1];
               extsession3 = (ExternalSession)arrTestData[2];
               extsession4 = (ExternalSession)arrTestData[3];
               extsession1 = (ExternalSession)m_externalSessionFactory.create(extsession1);
               extsession2 = (ExternalSession)m_externalSessionFactory.create(extsession2);
               extsession3 = (ExternalSession)m_externalSessionFactory.create(extsession3);
               extsession4 = (ExternalSession)m_externalSessionFactory.create(extsession4);
   
               // create role to the curUser
               userRole = new Role(DataObject.NEW_ID, 
                                   iCurrentDomainID, 
                                   "testname", 
                                   "testdescription", 
                                   true, 
                                   curUser.getId(), 
                                   false, null, null, null);
               userRole = (Role)m_roleFactory.create(userRole);
   
               m_roleFactory.assignToUser(curUser.getId(),
                                          Integer.toString(userRole.getId()));
   
               // create access right that curUser can delete only int.session1 object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_DELETE,
                                             m_iIntSessionDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             session1.getId(),
                                             null, null);
               m_rightFactory.create(accessRightView);          
   
               // create access right that curUser can view all int.session objects
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iIntSessionDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             session1.getId(),
                                             null, null);
               m_rightFactory.create(accessRightView);  
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            //------------------------
            // 1. try to logout for int. session 1
            //------------------------
            m_transaction.begin();
            try
            {
               m_sessionControl.logout(session1.getInternalSession());
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // sessions have to be logged out
            testSession = m_internalSessionFactory.get(session1.getInternalSession());
            assertNull("Internal session 1 should be logged out", testSession);
            lstExtSessions = m_externalSessionFactory.getAll(session1.getId());
            assertTrue("External sessions belonging int.session 1 should be logged out", 
                       (lstExtSessions == null || lstExtSessions.isEmpty()));
            session1 = null;
            extsession1 = null;
            extsession2 = null;
   
            //------------------------
            // 2. try to logout for int. session 2
            //------------------------
            // This method does not check access rights, so it doesn't matter if there are
            // set up access rights. Logout should be possible to call for all users.
            m_transaction.begin();
            try
            {
               m_sessionControl.logout(session2.getInternalSession());
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // sessions have not be logged out 
            testSession = m_internalSessionFactory.get(session2.getInternalSession());
            assertNull("Internal session 1 should be logged out", testSession);
            lstExtSessions = m_externalSessionFactory.getAll(session2.getId());
            assertTrue("External sessions belonging int.session 2 should be logged out", 
                       (lstExtSessions == null || lstExtSessions.isEmpty()));
            session2 = null;
            extsession3 = null;
            extsession4 = null;
         }
         finally
         {
            deleteTestData(user1, user2, userRole, session1, session2, 
                           extsession1, extsession2, extsession3, extsession4);
         }
      }
   
      /**
       * Test for logout of the user sessions (deleting of user's int.sessions and all related 
       * ext.sessions). User have action granted for all data objects of given type (no id and 
       * no categories specified when granting access).
       * 
       * @throws Exception - an error has occurred
       */
      public void testLogoutSessionsCheck(
      ) throws Exception 
      {
         User            curUser         = null;
         Role            userRole        = null;
         User            user1           = null;
         User            user2           = null;
         InternalSession session1        = null;
         InternalSession session2        = null;
         InternalSession testSession     = null;
         ExternalSession extsession1     = null;
         ExternalSession extsession2     = null;
         ExternalSession extsession3     = null;
         ExternalSession extsession4     = null;
         Object[]        arrTestData     = null;
         Object[]        arrTestUserData = null;
         Object[]        arrTestSessData = null;
         List            lstExtSessions  = null;
         AccessRight     accessRightView = null;
         int[]           arIDs           = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_roleFactory.createPersonal(curUser);
   
            m_transaction.begin();
            try
            {
               // create 2 test users first
               arrTestUserData = constructTestUsers();
               user1 = (User)arrTestUserData[0];
               user2 = (User)arrTestUserData[1];
               user1 = (User)m_userFactory.create(user1);
               user2 = (User)m_userFactory.create(user2);
               // create 2 int. sessions belonging to each user one session
               arrTestSessData = constructTestSessions(user1.getId(), user2.getId());
               session1 = (InternalSession)arrTestSessData[0];
               session2 = (InternalSession)arrTestSessData[1];
               session1 = (InternalSession)m_internalSessionFactory.create(session1);
               session2 = (InternalSession)m_internalSessionFactory.create(session2);
               // create 4 external sessions belonging to each int. session 2 ext. sessions
               arrTestData = constructTestData(session1.getId(), session2.getId());
               extsession1 = (ExternalSession)arrTestData[0];
               extsession2 = (ExternalSession)arrTestData[1];
               extsession3 = (ExternalSession)arrTestData[2];
               extsession4 = (ExternalSession)arrTestData[3];
               extsession1 = (ExternalSession)m_externalSessionFactory.create(extsession1);
               extsession2 = (ExternalSession)m_externalSessionFactory.create(extsession2);
               extsession3 = (ExternalSession)m_externalSessionFactory.create(extsession3);
               extsession4 = (ExternalSession)m_externalSessionFactory.create(extsession4);
   
               // create role to the curUser
               userRole = new Role(DataObject.NEW_ID, 
                                   iCurrentDomainID, 
                                   "testname", 
                                   "testdescription", 
                                   true, 
                                   curUser.getId(), 
                                   false, null, null, null);
               userRole = (Role)m_roleFactory.create(userRole);
   
               m_roleFactory.assignToUser(curUser.getId(),
                                          Integer.toString(userRole.getId()));
   
               // create access right that curUser can delete all int.session objects
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_DELETE,
                                             m_iIntSessionDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             AccessRight.NO_RIGHT_IDENTIFIER,
                                             null, null);
               m_rightFactory.create(accessRightView);          
   
               // create access right that curUser can view all int.session objects
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iIntSessionDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             session1.getId(),
                                             null, null);
               m_rightFactory.create(accessRightView);  
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            //------------------------
            // 1. try to logout session1 and session2
            //------------------------
            arIDs = new int[] {session1.getId(), session2.getId()};
            m_transaction.begin();
            try
            {
               m_sessionControl.logoutSessions(arIDs, true);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // all sessions have to be logged out
            testSession = m_internalSessionFactory.get(session1.getInternalSession());
            assertNull("Internal session 1 should be logged out", testSession);
            lstExtSessions = m_externalSessionFactory.getAll(session1.getId());
            assertTrue("External sessions belonging int.session 1 should be logged out", 
                       (lstExtSessions == null || lstExtSessions.isEmpty()));
            session1 = null;
            extsession1 = null; extsession2 = null; 
            testSession = m_internalSessionFactory.get(session2.getInternalSession());
            assertNull("Internal session 2 should be logged out", testSession);
            lstExtSessions = m_externalSessionFactory.getAll(session2.getId());
            assertTrue("External sessions belonging int.session 2 should be logged out", 
                       (lstExtSessions == null || lstExtSessions.isEmpty()));
            session2 = null;
            extsession3 = null; extsession4 = null;
         }
         finally
         {
            deleteTestData(user1, user2, userRole, session1, session2, 
                           extsession1, extsession2, extsession3, extsession4);
         }
      }
   
      /**
       * Test for logout of the user sessions (deleting of user's int.sessions and all related 
       * ext.sessions). User have access granted to specific data object (identified by identifier) 
       * of given data type.
       * 
       * @throws Exception - an error has occurred
       */
      public void testLogoutSessionsCheckId(
      ) throws Exception 
      {
         User            curUser         = null;
         Role            userRole        = null;
         User            user1           = null;
         User            user2           = null;
         InternalSession session1        = null;
         InternalSession session2        = null;
         InternalSession testSession     = null;
         ExternalSession extsession1     = null;
         ExternalSession extsession2     = null;
         ExternalSession extsession3     = null;
         ExternalSession extsession4     = null;
         Object[]        arrTestData     = null;
         Object[]        arrTestUserData = null;
         Object[]        arrTestSessData = null;
         List            lstExtSessions  = null;
         AccessRight     accessRightView = null;
         int[]           arIDs           = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_roleFactory.createPersonal(curUser);
   
            m_transaction.begin();
            try
            {
               // create 2 test users first
               arrTestUserData = constructTestUsers();
               user1 = (User)arrTestUserData[0];
               user2 = (User)arrTestUserData[1];
               user1 = (User)m_userFactory.create(user1);
               user2 = (User)m_userFactory.create(user2);
               // create 2 int. sessions belonging to each user one session
               arrTestSessData = constructTestSessions(user1.getId(), user2.getId());
               session1 = (InternalSession)arrTestSessData[0];
               session2 = (InternalSession)arrTestSessData[1];
               session1 = (InternalSession)m_internalSessionFactory.create(session1);
               session2 = (InternalSession)m_internalSessionFactory.create(session2);
               // create 4 external sessions belonging to each int. session 2 ext. sessions
               arrTestData = constructTestData(session1.getId(), session2.getId());
               extsession1 = (ExternalSession)arrTestData[0];
               extsession2 = (ExternalSession)arrTestData[1];
               extsession3 = (ExternalSession)arrTestData[2];
               extsession4 = (ExternalSession)arrTestData[3];
               extsession1 = (ExternalSession)m_externalSessionFactory.create(extsession1);
               extsession2 = (ExternalSession)m_externalSessionFactory.create(extsession2);
               extsession3 = (ExternalSession)m_externalSessionFactory.create(extsession3);
               extsession4 = (ExternalSession)m_externalSessionFactory.create(extsession4);
   
               // create role to the curUser
               userRole = new Role(DataObject.NEW_ID, 
                                   iCurrentDomainID, 
                                   "testname", 
                                   "testdescription", 
                                   true, 
                                   curUser.getId(), 
                                   false, null, null, null);
               userRole = (Role)m_roleFactory.create(userRole);
   
               m_roleFactory.assignToUser(curUser.getId(),
                                          Integer.toString(userRole.getId()));
   
               // create access right that curUser can delete only int.session1 object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_DELETE,
                                             m_iIntSessionDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             session1.getId(),
                                             null, null);
               m_rightFactory.create(accessRightView);          
   
               // create access right that curUser can view all int.session objects
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iIntSessionDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             AccessRight.NO_RIGHT_IDENTIFIER,
                                             null, null);
               m_rightFactory.create(accessRightView);  
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            //------------------------
            // 1. try to logout session1 and session2
            //------------------------
            arIDs = new int[] {session1.getId(), session2.getId()};
            m_transaction.begin();
            try
            {
               m_sessionControl.logoutSessions(arIDs, true);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // only session1 have to be logged out
            testSession = m_internalSessionFactory.get(session1.getInternalSession());
            assertNull("Internal session 1 should be logged out", testSession);
            lstExtSessions = m_externalSessionFactory.getAll(session1.getId());
            assertTrue("External sessions belonging int.session 1 should be logged out", 
                       (lstExtSessions == null || lstExtSessions.isEmpty()));
            session1 = null;
            extsession1 = null; extsession2 = null; 
            testSession = m_internalSessionFactory.get(session2.getInternalSession());
            assertEquals("Internal session 2 should not be logged out because of access " +
                         "right limitation", session2, testSession);
            lstExtSessions = m_externalSessionFactory.getAll(session2.getId());
            assertEquals("External sessions belonging int.session 2 should not be logged out " +
                         "because of access right limitation", 2, lstExtSessions.size());
         }
         finally
         {
            deleteTestData(user1, user2, userRole, session1, session2, 
                           extsession1, extsession2, extsession3, extsession4);
         }
      }
   
      /**
       * Test for detach.
       * User have action granted for all data objects of given type (no id and no categories 
       * specified when granting access).
       * 
       * @throws Exception - an error has occurred
       */
      public void testDetachCheck(
      ) throws Exception 
      {
         User            curUser         = null;
         Role            userRole        = null;
         User            user1           = null;
         User            user2           = null;
         InternalSession session1        = null;
         InternalSession session2        = null;
         InternalSession testSession     = null;
         ExternalSession extsession1     = null;
         ExternalSession extsession2     = null;
         ExternalSession extsession3     = null;
         ExternalSession extsession4     = null;
         Object[]        arrTestData     = null;
         Object[]        arrTestUserData = null;
         Object[]        arrTestSessData = null;
         List            lstExtSessions  = null;
         AccessRight     accessRightView = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_roleFactory.createPersonal(curUser);
   
            m_transaction.begin();
            try
            {
               // create 2 test users first
               arrTestUserData = constructTestUsers();
               user1 = (User)arrTestUserData[0];
               user2 = (User)arrTestUserData[1];
               user1 = (User)m_userFactory.create(user1);
               user2 = (User)m_userFactory.create(user2);
               // create 2 int. sessions belonging to each user one session
               arrTestSessData = constructTestSessions(user1.getId(), user2.getId());
               session1 = (InternalSession)arrTestSessData[0];
               session2 = (InternalSession)arrTestSessData[1];
               session1 = (InternalSession)m_internalSessionFactory.create(session1);
               session2 = (InternalSession)m_internalSessionFactory.create(session2);
               // create 4 external sessions belonging to each int. session 2 ext. sessions
               arrTestData = constructTestData(session1.getId(), session2.getId());
               extsession1 = (ExternalSession)arrTestData[0];
               extsession2 = (ExternalSession)arrTestData[1];
               extsession3 = (ExternalSession)arrTestData[2];
               extsession4 = (ExternalSession)arrTestData[3];
               extsession1 = (ExternalSession)m_externalSessionFactory.create(extsession1);
               extsession2 = (ExternalSession)m_externalSessionFactory.create(extsession2);
               extsession3 = (ExternalSession)m_externalSessionFactory.create(extsession3);
               extsession4 = (ExternalSession)m_externalSessionFactory.create(extsession4);
   
               // create role to the curUser
               userRole = new Role(DataObject.NEW_ID, 
                                   iCurrentDomainID, 
                                   "testname", 
                                   "testdescription", 
                                   true, 
                                   curUser.getId(), 
                                   false, null, null, null);
               userRole = (Role)m_roleFactory.create(userRole);
   
               m_roleFactory.assignToUser(curUser.getId(),
                                          Integer.toString(userRole.getId()));
   
               // create access right that curUser can delete all int.session objects
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_DELETE,
                                             m_iIntSessionDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             AccessRight.NO_RIGHT_IDENTIFIER,
                                             null, null);
               m_rightFactory.create(accessRightView);          
   
               // create access right that curUser can delete all ext.session objects
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_DELETE,
                                             m_iExtSessionDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             AccessRight.NO_RIGHT_IDENTIFIER,
                                             null, null);
               m_rightFactory.create(accessRightView);          
   
               // create access right that curUser can view all int.session objects
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iIntSessionDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             session1.getId(),
                                             null, null);
               m_rightFactory.create(accessRightView);  
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            //------------------------
            // 1. try to detach for int.session 1 ext.session 1
            //------------------------
            m_transaction.begin();
            try
            {
               m_sessionControl.detach(session1.getInternalSession(), 
                                       extsession1.getExternalSession(), 
                                       new InstanceInfoImpl(extsession1.getServer()));
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // only ext.sessions 1 should be detached
            testSession = m_internalSessionFactory.get(session1.getInternalSession());
            assertNotNull("Internal session 1 should not be null", testSession);
            lstExtSessions = m_externalSessionFactory.getAll(session1.getId());
            assertEquals("Number of external sessions belonging int.session 1 is incorrect", 
                         1, lstExtSessions.size());
            assertEquals("Incorrect session was detached", extsession2, lstExtSessions.get(0));
            extsession1 = null;
            testSession = m_internalSessionFactory.get(session2.getInternalSession());
            assertNotNull("Internal session 2 should not be null", testSession);
            lstExtSessions = m_externalSessionFactory.getAll(session2.getId());
            assertEquals("Number of external sessions belonging int.session 2 is incorrect", 
                         2, lstExtSessions.size());
   
            //------------------------
            // 2. try to detach for int.session 2 ext.session 3
            //------------------------
            m_transaction.begin();
            try
            {
               m_sessionControl.detach(session2.getInternalSession(), 
                                       extsession3.getExternalSession(), 
                                       new InstanceInfoImpl(extsession3.getServer()));
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // only ext.sessions 3 should be detached
            testSession = m_internalSessionFactory.get(session1.getInternalSession());
            assertNotNull("Internal session 1 should not be null", testSession);
            lstExtSessions = m_externalSessionFactory.getAll(session1.getId());
            assertEquals("Number of external sessions belonging int.session 1 is incorrect", 
                         1, lstExtSessions.size());
            assertEquals("Incorrect session was detached", extsession2, lstExtSessions.get(0));
            extsession3 = null;
            testSession = m_internalSessionFactory.get(session2.getInternalSession());
            assertNotNull("Internal session 2 should not be null", testSession);
            lstExtSessions = m_externalSessionFactory.getAll(session2.getId());
            assertEquals("Number of external sessions belonging int.session 2 is incorrect", 
                         1, lstExtSessions.size());
            assertEquals("Incorrect session was detached", extsession4, lstExtSessions.get(0));
         }
         finally
         {
            deleteTestData(user1, user2, userRole, session1, session2, 
                           extsession1, extsession2, extsession3, extsession4);
         }
      }
   
      /**
       * Test for detach. User have access granted to specific
       * data object (identified by identifier) of given data type.
       * 
       * @throws Exception - an error has occurred
       */
      public void testDetachCheckId(
      ) throws Exception 
      {
         User            curUser         = null;
         Role            userRole        = null;
         User            user1           = null;
         User            user2           = null;
         InternalSession session1        = null;
         InternalSession session2        = null;
         InternalSession testSession     = null;
         ExternalSession extsession1     = null;
         ExternalSession extsession2     = null;
         ExternalSession extsession3     = null;
         ExternalSession extsession4     = null;
         Object[]        arrTestData     = null;
         Object[]        arrTestUserData = null;
         Object[]        arrTestSessData = null;
         List            lstExtSessions  = null;
         AccessRight     accessRightView = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_roleFactory.createPersonal(curUser);
   
            m_transaction.begin();
            try
            {
               // create 2 test users first
               arrTestUserData = constructTestUsers();
               user1 = (User)arrTestUserData[0];
               user2 = (User)arrTestUserData[1];
               user1 = (User)m_userFactory.create(user1);
               user2 = (User)m_userFactory.create(user2);
               // create 2 int. sessions belonging to each user one session
               arrTestSessData = constructTestSessions(user1.getId(), user2.getId());
               session1 = (InternalSession)arrTestSessData[0];
               session2 = (InternalSession)arrTestSessData[1];
               session1 = (InternalSession)m_internalSessionFactory.create(session1);
               session2 = (InternalSession)m_internalSessionFactory.create(session2);
               // create 4 external sessions belonging to each int. session 2 ext. sessions
               arrTestData = constructTestData(session1.getId(), session2.getId());
               extsession1 = (ExternalSession)arrTestData[0];
               extsession2 = (ExternalSession)arrTestData[1];
               extsession3 = (ExternalSession)arrTestData[2];
               extsession4 = (ExternalSession)arrTestData[3];
               extsession1 = (ExternalSession)m_externalSessionFactory.create(extsession1);
               extsession2 = (ExternalSession)m_externalSessionFactory.create(extsession2);
               extsession3 = (ExternalSession)m_externalSessionFactory.create(extsession3);
               extsession4 = (ExternalSession)m_externalSessionFactory.create(extsession4);
   
               // create role to the curUser
               userRole = new Role(DataObject.NEW_ID, 
                                   iCurrentDomainID, 
                                   "testname", 
                                   "testdescription", 
                                   true, 
                                   curUser.getId(), 
                                   false, null, null, null);
               userRole = (Role)m_roleFactory.create(userRole);
   
               m_roleFactory.assignToUser(curUser.getId(),
                                          Integer.toString(userRole.getId()));
   
               // create access right that curUser can delete only ext.session1 objects
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_DELETE,
                                             m_iExtSessionDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             AccessRight.NO_RIGHT_IDENTIFIER,
                                             null, null);
               m_rightFactory.create(accessRightView);          
   
               // create access right that curUser can view all int.session objects
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iIntSessionDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             session1.getId(),
                                             null, null);
               m_rightFactory.create(accessRightView);  
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            //------------------------
            // 1. try to detach for int.session 1 ext.session 1
            //------------------------
            m_transaction.begin();
            try
            {
               m_sessionControl.detach(session1.getInternalSession(), 
                                       extsession1.getExternalSession(), 
                                       new InstanceInfoImpl(extsession1.getServer()));
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // only ext.sessions 1 should be detached
            // there are not checked access right within the controller method
            testSession = m_internalSessionFactory.get(session1.getInternalSession());
            assertNotNull("Internal session 1 should not be null", testSession);
            lstExtSessions = m_externalSessionFactory.getAll(session1.getId());
            assertEquals("Number of external sessions belonging int.session 1 is incorrect", 
                         1, lstExtSessions.size());
            assertEquals("Incorrect session was detached", extsession2, lstExtSessions.get(0));
            extsession1 = null;
            testSession = m_internalSessionFactory.get(session2.getInternalSession());
            assertNotNull("Internal session 2 should not be null", testSession);
            lstExtSessions = m_externalSessionFactory.getAll(session2.getId());
            assertEquals("Number of external sessions belonging int.session 2 is incorrect", 
                         2, lstExtSessions.size());
   
            //------------------------
            // 2. try to detach for int.session 2 ext.session 3
            //------------------------
            m_transaction.begin();
            try
            {
               m_sessionControl.detach(session2.getInternalSession(), 
                                       extsession3.getExternalSession(), 
                                       new InstanceInfoImpl(extsession3.getServer()));
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // only ext.sessions 3 should be detached
            // there are not checked access right within the controller method
            testSession = m_internalSessionFactory.get(session1.getInternalSession());
            assertNotNull("Internal session 1 should not be null", testSession);
            lstExtSessions = m_externalSessionFactory.getAll(session1.getId());
            assertEquals("Number of external sessions belonging int.session 1 is incorrect", 
                         1, lstExtSessions.size());
            assertEquals("Incorrect session was detached", extsession2, lstExtSessions.get(0));
            extsession3 = null;
            testSession = m_internalSessionFactory.get(session2.getInternalSession());
            assertNotNull("Internal session 2 should not be null", testSession);
            lstExtSessions = m_externalSessionFactory.getAll(session2.getId());
            assertEquals("Number of external sessions belonging int.session 2 is incorrect", 
                         1, lstExtSessions.size());
            assertEquals("Incorrect session was detached", extsession4, lstExtSessions.get(0));
         }
         finally
         {
            deleteTestData(user1, user2, userRole, session1, session2, 
                           extsession1, extsession2, extsession3, extsession4);
         }
      }
   
      /**
       * Test for cleaning of orphan sessions.
       * User have action granted for all data objects of given type (no id and no categories 
       * specified when granting access).
       * 
       * @throws Exception - an error has occurred
       */
      public void testCleanOrphanSessionCheck(
      ) throws Exception 
      {
         User            curUser         = null;
         Role            userRole        = null;
         User            user1           = null;
         User            user2           = null;
         InternalSession session1        = null;
         InternalSession session2        = null;
         InternalSession testSession     = null;
         ExternalSession extsession1     = null;
         ExternalSession extsession2     = null;
         ExternalSession extsession3     = null;
         ExternalSession extsession4     = null;
         Object[]        arrTestData     = null;
         Object[]        arrTestUserData = null;
         Object[]        arrTestSessData = null;
         List            lstExtSessions  = null;
         AccessRight     accessRightView = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_roleFactory.createPersonal(curUser);
   
            m_transaction.begin();
            try
            {
               // create 2 test users first
               arrTestUserData = constructTestUsers();
               user1 = (User)arrTestUserData[0];
               user2 = (User)arrTestUserData[1];
               user1 = (User)m_userFactory.create(user1);
               user2 = (User)m_userFactory.create(user2);
               // create 2 int. sessions belonging to each user one session
               arrTestSessData = constructTestSessions(user1.getId(), user2.getId());
               session1 = (InternalSession)arrTestSessData[0];
               session2 = (InternalSession)arrTestSessData[1];
               session1 = (InternalSession)m_internalSessionFactory.create(session1);
               session2 = (InternalSession)m_internalSessionFactory.create(session2);
               // create 4 external sessions belonging to each int. session 2 ext. sessions
               arrTestData = constructTestData(session1.getId(), session2.getId());
               extsession1 = (ExternalSession)arrTestData[0];
               extsession2 = (ExternalSession)arrTestData[1];
               extsession3 = (ExternalSession)arrTestData[2];
               extsession4 = (ExternalSession)arrTestData[3];
               extsession1 = (ExternalSession)m_externalSessionFactory.create(extsession1);
               extsession2 = (ExternalSession)m_externalSessionFactory.create(extsession2);
               extsession3 = (ExternalSession)m_externalSessionFactory.create(extsession3);
               extsession4 = (ExternalSession)m_externalSessionFactory.create(extsession4);
   
               // create role to the curUser
               userRole = new Role(DataObject.NEW_ID, 
                                   iCurrentDomainID, 
                                   "testname", 
                                   "testdescription", 
                                   true, 
                                   curUser.getId(), 
                                   false, null, null, null);
               userRole = (Role)m_roleFactory.create(userRole);
   
               m_roleFactory.assignToUser(curUser.getId(),
                                          Integer.toString(userRole.getId()));
   
               // create access right that curUser can delete all int.session objects
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_DELETE,
                                             m_iIntSessionDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             AccessRight.NO_RIGHT_IDENTIFIER,
                                             null, null);
               m_rightFactory.create(accessRightView);          
   
               // create access right that curUser can delete all ext.session objects
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_DELETE,
                                             m_iExtSessionDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             AccessRight.NO_RIGHT_IDENTIFIER,
                                             null, null);
               m_rightFactory.create(accessRightView);          
   
               // create access right that curUser can view all int.session objects
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iIntSessionDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             session1.getId(),
                                             null, null);
               m_rightFactory.create(accessRightView);  
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            //------------------------
            // 1. try to clean orphan sessions for ext. session 1 server
            //------------------------
            m_transaction.begin();
            try
            {
               m_sessionControl.cleanOrphanSession(
                                   new InstanceInfoImpl(extsession1.getServer()));
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // only ext.sessions 1 and 3 should be deleted
            testSession = m_internalSessionFactory.get(session1.getInternalSession());
            assertNotNull("Internal session 1 should not be null", testSession);
            lstExtSessions = m_externalSessionFactory.getAll(session1.getId());
            assertEquals("Number of external sessions belonging int.session 1 is incorrect", 
                       1, lstExtSessions.size());
            extsession1 = null;
            testSession = m_internalSessionFactory.get(session2.getInternalSession());
            assertNotNull("Internal session 2 should not be null", testSession);
            lstExtSessions = m_externalSessionFactory.getAll(session2.getId());
            assertEquals("Number of external sessions belonging int.session 2 is incorrect", 
                       1, lstExtSessions.size());
            extsession3 = null;
   
            //------------------------
            // 2. try to clean orphan sessions for ext. session 2 server
            //------------------------
            m_transaction.begin();
            try
            {
               m_sessionControl.cleanOrphanSession(
                                   new InstanceInfoImpl(extsession2.getServer()));
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // int.sessions 1 and 2 with belonging exterrnal sessions should be deleted 
            testSession = m_internalSessionFactory.get(session1.getInternalSession());
            assertNull("Internal session 1 should be null", testSession);
            lstExtSessions = m_externalSessionFactory.getAll(session1.getId());
            assertTrue("External sessions belonging int.session 1 should be cleaned", 
                       (lstExtSessions == null || lstExtSessions.isEmpty()));
            testSession = m_internalSessionFactory.get(session2.getInternalSession());
            assertNull("Internal session 2 should be null", testSession);
            lstExtSessions = m_externalSessionFactory.getAll(session2.getId());
            assertTrue("External sessions belonging int.session 2 should be cleanedt", 
                       (lstExtSessions == null || lstExtSessions.isEmpty()));
            session1 = null;
            session2 = null;
            extsession2 = null;
            extsession4 = null;
         }
         finally
         {
            deleteTestData(user1, user2, userRole, session1, session2, 
                           extsession1, extsession2, extsession3, extsession4);
         }
      }
   
      /**
       * Test for cleaning of orphan sessions. User have access granted to specific data object
       * (identified by identifier) of given data type.
       * 
       * @throws Exception - an error has occurred
       */
      public void testCleanOrphanSessionCheckId(
      ) throws Exception 
      {
         User            curUser         = null;
         Role            userRole        = null;
         User            user1           = null;
         User            user2           = null;
         InternalSession session1        = null;
         InternalSession session2        = null;
         InternalSession testSession     = null;
         ExternalSession extsession1     = null;
         ExternalSession extsession2     = null;
         ExternalSession extsession3     = null;
         ExternalSession extsession4     = null;
         Object[]        arrTestData     = null;
         Object[]        arrTestUserData = null;
         Object[]        arrTestSessData = null;
         List            lstExtSessions  = null;
         AccessRight     accessRightView = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_roleFactory.createPersonal(curUser);
   
            m_transaction.begin();
            try
            {
               // create 2 test users first
               arrTestUserData = constructTestUsers();
               user1 = (User)arrTestUserData[0];
               user2 = (User)arrTestUserData[1];
               user1 = (User)m_userFactory.create(user1);
               user2 = (User)m_userFactory.create(user2);
               // create 2 int. sessions belonging to each user one session
               arrTestSessData = constructTestSessions(user1.getId(), user2.getId());
               session1 = (InternalSession)arrTestSessData[0];
               session2 = (InternalSession)arrTestSessData[1];
               session1 = (InternalSession)m_internalSessionFactory.create(session1);
               session2 = (InternalSession)m_internalSessionFactory.create(session2);
               // create 4 external sessions belonging to each int. session 2 ext. sessions
               arrTestData = constructTestData(session1.getId(), session2.getId());
               extsession1 = (ExternalSession)arrTestData[0];
               extsession2 = (ExternalSession)arrTestData[1];
               extsession3 = (ExternalSession)arrTestData[2];
               extsession4 = (ExternalSession)arrTestData[3];
               extsession1 = (ExternalSession)m_externalSessionFactory.create(extsession1);
               extsession2 = (ExternalSession)m_externalSessionFactory.create(extsession2);
               extsession3 = (ExternalSession)m_externalSessionFactory.create(extsession3);
               extsession4 = (ExternalSession)m_externalSessionFactory.create(extsession4);
   
               // create role to the curUser
               userRole = new Role(DataObject.NEW_ID, 
                                   iCurrentDomainID, 
                                   "testname", 
                                   "testdescription", 
                                   true, 
                                   curUser.getId(), 
                                   false, null, null, null);
               userRole = (Role)m_roleFactory.create(userRole);
   
               m_roleFactory.assignToUser(curUser.getId(),
                                          Integer.toString(userRole.getId()));
   
               // create access right that curUser can delete only int.session1 object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_DELETE,
                                             m_iIntSessionDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             session1.getId(),
                                             null, null);
               m_rightFactory.create(accessRightView);          
   
               // create access right that curUser can delete only ext.session1 objects
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_DELETE,
                                             m_iExtSessionDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             extsession1.getId(),
                                             null, null);
               m_rightFactory.create(accessRightView);          
   
               // create access right that curUser can view all int.session objects
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iIntSessionDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             session1.getId(),
                                             null, null);
               m_rightFactory.create(accessRightView);  
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            //------------------------
            // 1. try to clean orphan sessions for ext. session 1 server
            //------------------------
            m_transaction.begin();
            try
            {
               m_sessionControl.cleanOrphanSession(
                                   new InstanceInfoImpl(extsession1.getServer()));
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // only ext.sessions 1 and 3 should be deleted
            // there is not security check in particular controller method
            testSession = m_internalSessionFactory.get(session1.getInternalSession());
            assertNotNull("Internal session 1 should not be null", testSession);
            lstExtSessions = m_externalSessionFactory.getAll(session1.getId());
            assertEquals("Number of external sessions belonging int.session 1 is incorrect", 
                       1, lstExtSessions.size());
            extsession1 = null;
            testSession = m_internalSessionFactory.get(session2.getInternalSession());
            assertNotNull("Internal session 2 should not be null", testSession);
            lstExtSessions = m_externalSessionFactory.getAll(session2.getId());
            assertEquals("Number of external sessions belonging int.session 2 is incorrect", 
                       1, lstExtSessions.size());
            extsession3 = null;
   
            //------------------------
            // 2. try to clean orphan sessions for ext. session 2 server
            //------------------------
            m_transaction.begin();
            try
            {
               m_sessionControl.cleanOrphanSession(
                                   new InstanceInfoImpl(extsession2.getServer()));
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // int.sessions 1 and 2 with belonging exterrnal sessions should be deleted 
            // there is not security check in particular controller method
            testSession = m_internalSessionFactory.get(session1.getInternalSession());
            assertNull("Internal session 1 should be null", testSession);
            lstExtSessions = m_externalSessionFactory.getAll(session1.getId());
            assertTrue("External sessions belonging int.session 1 should be cleaned", 
                       (lstExtSessions == null || lstExtSessions.isEmpty()));
            testSession = m_internalSessionFactory.get(session2.getInternalSession());
            assertNull("Internal session 2 should be null", testSession);
            lstExtSessions = m_externalSessionFactory.getAll(session2.getId());
            assertTrue("External sessions belonging int.session 2 should be cleanedt", 
                       (lstExtSessions == null || lstExtSessions.isEmpty()));
            session1 = null;
            session2 = null;
            extsession2 = null;
            extsession4 = null;
         }
         finally
         {
            deleteTestData(user1, user2, userRole, session1, session2, 
                           extsession1, extsession2, extsession3, extsession4);
         }
      }
   
      /**
       * Test checking if the user is logged in. User have action granted for all data 
       * objects of given type (no id and no categories specified when granting access).
       * 
       * @throws Exception - an error has occurred
       */
      public void testUpdateEnableCheck(
      ) throws Exception 
      {
         User            curUser         = null;
         Role            userRole        = null;
         User            user1           = null;
         User            user2           = null;
         User            testUser        = null;
         InternalSession session1        = null;
         InternalSession session2        = null;
         ExternalSession extsession1     = null;
         ExternalSession extsession2     = null;
         ExternalSession extsession3     = null;
         ExternalSession extsession4     = null;
         Object[]        arrTestData     = null;
         Object[]        arrTestUserData = null;
         Object[]        arrTestSessData = null;
         AccessRight     accessRightView = null;
         int             iEnabled        = 0;
         int[]           arIDs           = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_roleFactory.createPersonal(curUser);
   
            m_transaction.begin();
            try
            {
               // create 2 test users first
               arrTestUserData = constructTestUsers();
               user1 = (User)arrTestUserData[0];
               user2 = (User)arrTestUserData[1];
               user1 = (User)m_userFactory.create(user1);
               user2 = (User)m_userFactory.create(user2);
               // create 2 int. sessions belonging to each user one session
               arrTestSessData = constructTestSessions(user1.getId(), user2.getId());
               session1 = (InternalSession)arrTestSessData[0];
               session2 = (InternalSession)arrTestSessData[1];
               session1 = (InternalSession)m_internalSessionFactory.create(session1);
               session2 = (InternalSession)m_internalSessionFactory.create(session2);
               // create 4 external sessions belonging to each int. session 2 ext. sessions
               arrTestData = constructTestData(session1.getId(), session2.getId());
               extsession1 = (ExternalSession)arrTestData[0];
               extsession2 = (ExternalSession)arrTestData[1];
               extsession3 = (ExternalSession)arrTestData[2];
               extsession4 = (ExternalSession)arrTestData[3];
               extsession1 = (ExternalSession)m_externalSessionFactory.create(extsession1);
               extsession2 = (ExternalSession)m_externalSessionFactory.create(extsession2);
               extsession3 = (ExternalSession)m_externalSessionFactory.create(extsession3);
               extsession4 = (ExternalSession)m_externalSessionFactory.create(extsession4);
   
               // create role to the curUser
               userRole = new Role(DataObject.NEW_ID, 
                                   iCurrentDomainID, 
                                   "testname", 
                                   "testdescription", 
                                   true, 
                                   curUser.getId(), 
                                   false, null, null, null);
               userRole = (Role)m_roleFactory.create(userRole);
   
               m_roleFactory.assignToUser(curUser.getId(),
                                          Integer.toString(userRole.getId()));
   
               // create access right that curUser can modify all user objects
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_MODIFY,
                                             m_iUserDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             AccessRight.NO_RIGHT_IDENTIFIER,
                                             null, null);
               m_rightFactory.create(accessRightView);          
   
               // create access right that curUser can view all user objects
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iIntSessionDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             AccessRight.NO_RIGHT_IDENTIFIER,
                                             null, null);
               m_rightFactory.create(accessRightView);          
   
               // create access right that curUser can view all session objects
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iIntSessionDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             AccessRight.NO_RIGHT_IDENTIFIER,
                                             null, null);
               m_rightFactory.create(accessRightView);          
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            //------------------------
            // 1. try to update enable users for int. session1 and int. session2 code
            //------------------------
            arIDs = new int[] {session1.getId(), session2.getId()};
            m_transaction.begin();
            try
            {
               iEnabled = m_sessionControl.updateEnable(arIDs, true);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // users has to be enabled
            testUser = (User)m_userFactory.get(
                                user1.getId(),
                                CallContext.getInstance().getCurrentDomainId());
            assertEquals("User 1 should be enabled", true, testUser.isLoginEnabled());
            testUser = (User)m_userFactory.get(
                                user2.getId(),
                                CallContext.getInstance().getCurrentDomainId());
            assertEquals("User 2 should be enabled", true, testUser.isLoginEnabled());
            assertEquals("Incorrect number of enabled users", 2, iEnabled);
         }
         finally
         {
            deleteTestData(user1, user2, userRole, session1, session2, 
                           extsession1, extsession2, extsession3, extsession4);
         }
      }
   
      /**
       * Test checking if the user is logged in. User have access granted to specific data 
       * object (identified by identifier) of given data type.
       * 
       * @throws Exception - an error has occurred
       */
      public void testUpdateEnableCheckId(
      ) throws Exception 
      {
         User            curUser         = null;
         Role            userRole        = null;
         User            user1           = null;
         User            user2           = null;
         User            testUser        = null;
         InternalSession session1        = null;
         InternalSession session2        = null;
         ExternalSession extsession1     = null;
         ExternalSession extsession2     = null;
         ExternalSession extsession3     = null;
         ExternalSession extsession4     = null;
         Object[]        arrTestData     = null;
         Object[]        arrTestUserData = null;
         Object[]        arrTestSessData = null;
         AccessRight     accessRightView = null;
         int             iEnabled        = 0;
         int[]           arIDs           = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_roleFactory.createPersonal(curUser);
   
            m_transaction.begin();
            try
            {
               // create 2 test users first
               arrTestUserData = constructTestUsers();
               user1 = (User)arrTestUserData[0];
               user2 = (User)arrTestUserData[1];
               user1 = (User)m_userFactory.create(user1);
               user2 = (User)m_userFactory.create(user2);
               // create 2 int. sessions belonging to each user one session
               arrTestSessData = constructTestSessions(user1.getId(), user2.getId());
               session1 = (InternalSession)arrTestSessData[0];
               session2 = (InternalSession)arrTestSessData[1];
               session1 = (InternalSession)m_internalSessionFactory.create(session1);
               session2 = (InternalSession)m_internalSessionFactory.create(session2);
               // create 4 external sessions belonging to each int. session 2 ext. sessions
               arrTestData = constructTestData(session1.getId(), session2.getId());
               extsession1 = (ExternalSession)arrTestData[0];
               extsession2 = (ExternalSession)arrTestData[1];
               extsession3 = (ExternalSession)arrTestData[2];
               extsession4 = (ExternalSession)arrTestData[3];
               extsession1 = (ExternalSession)m_externalSessionFactory.create(extsession1);
               extsession2 = (ExternalSession)m_externalSessionFactory.create(extsession2);
               extsession3 = (ExternalSession)m_externalSessionFactory.create(extsession3);
               extsession4 = (ExternalSession)m_externalSessionFactory.create(extsession4);
   
               // create role to the curUser
               userRole = new Role(DataObject.NEW_ID, 
                                   iCurrentDomainID, 
                                   "testname", 
                                   "testdescription", 
                                   true, 
                                   curUser.getId(), 
                                   false, null, null, null);
               userRole = (Role)m_roleFactory.create(userRole);
   
               m_roleFactory.assignToUser(curUser.getId(),
                                          Integer.toString(userRole.getId()));
   
               // create access right that curUser can modify only user1 objects
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_MODIFY,
                                             m_iUserDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             user1.getId(),
                                             null, null);
               m_rightFactory.create(accessRightView);          
   
               // create access right that curUser can view all user objects
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iIntSessionDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             AccessRight.NO_RIGHT_IDENTIFIER,
                                             null, null);
               m_rightFactory.create(accessRightView);          
   
               // create access right that curUser can view only session2 objects
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iIntSessionDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             session2.getId(),
                                             null, null);
               m_rightFactory.create(accessRightView);          
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            //------------------------
            // 1. try to update enable users for int. session1 and int. session2 code
            //------------------------
            arIDs = new int[] {session1.getId(), session2.getId()};
            m_transaction.begin();
            try
            {
               iEnabled = m_sessionControl.updateEnable(arIDs, true);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // user1 has to be enabled
            testUser = (User)m_userFactory.get(
                                user1.getId(),
                                CallContext.getInstance().getCurrentDomainId());
            assertEquals("User 1 should be enabled", true, testUser.isLoginEnabled());
            // user1 has not be enabled
            testUser = (User)m_userFactory.get(
                                user2.getId(),
                                CallContext.getInstance().getCurrentDomainId());
            assertEquals("User 2 should not be enabled because of access right limitation",
                         false, testUser.isLoginEnabled());
            assertEquals("Incorrect number of enabled users", 1, iEnabled);
         }
         finally
         {
            deleteTestData(user1, user2, userRole, session1, session2, 
                           extsession1, extsession2, extsession3, extsession4);
         }
      }
   
      /**
       * Test checking if the user is logged in. User have access granted to a group 
       * of objects based on categories the objects belong to.
       * 
       * @throws Exception - an error has occurred
       */
      public void testUpdateEnableCheckCategorySuperUser(
      ) throws Exception 
      {
         User            curUser         = null;
         Role            userRole        = null;
         User            user1           = null;
         User            user2           = null;
         User            testUser        = null;
         InternalSession session1        = null;
         InternalSession session2        = null;
         ExternalSession extsession1     = null;
         ExternalSession extsession2     = null;
         ExternalSession extsession3     = null;
         ExternalSession extsession4     = null;
         Object[]        arrTestData     = null;
         Object[]        arrTestUserData = null;
         Object[]        arrTestSessData = null;
         AccessRight     accessRightView = null;
         int             iEnabled        = 0;
         int[]           arIDs           = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_roleFactory.createPersonal(curUser);
   
            m_transaction.begin();
            try
            {
               // create 2 test users first
               arrTestUserData = constructTestUsers();
               user1 = (User)arrTestUserData[0];
               user2 = (User)arrTestUserData[1];
               user1 = (User)m_userFactory.create(user1);
               user2 = (User)m_userFactory.create(user2);
               // create 2 int. sessions belonging to each user one session
               arrTestSessData = constructTestSessions(user1.getId(), user2.getId());
               session1 = (InternalSession)arrTestSessData[0];
               session2 = (InternalSession)arrTestSessData[1];
               session1 = (InternalSession)m_internalSessionFactory.create(session1);
               session2 = (InternalSession)m_internalSessionFactory.create(session2);
               // create 4 external sessions belonging to each int. session 2 ext. sessions
               arrTestData = constructTestData(session1.getId(), session2.getId());
               extsession1 = (ExternalSession)arrTestData[0];
               extsession2 = (ExternalSession)arrTestData[1];
               extsession3 = (ExternalSession)arrTestData[2];
               extsession4 = (ExternalSession)arrTestData[3];
               extsession1 = (ExternalSession)m_externalSessionFactory.create(extsession1);
               extsession2 = (ExternalSession)m_externalSessionFactory.create(extsession2);
               extsession3 = (ExternalSession)m_externalSessionFactory.create(extsession3);
               extsession4 = (ExternalSession)m_externalSessionFactory.create(extsession4);
   
               // create role to the curUser
               userRole = new Role(DataObject.NEW_ID, 
                                   iCurrentDomainID, 
                                   "testname", 
                                   "testdescription", 
                                   true, 
                                   curUser.getId(), 
                                   false, null, null, null);
               userRole = (Role)m_roleFactory.create(userRole);
   
               m_roleFactory.assignToUser(curUser.getId(),
                                          Integer.toString(userRole.getId()));
   
               // create access right that curUser can modify only user objects 
               // that are not superuser
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_MODIFY,
                                             m_iUserDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             UserDataDescriptor.COL_USER_SUPER_USER,
                                             DataCondition.DATA_CODE_FLAG_NO,
                                             null, null);
               m_rightFactory.create(accessRightView);          
   
               // create access right that curUser can view all user objects
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iIntSessionDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             AccessRight.NO_RIGHT_IDENTIFIER,
                                             null, null);
               m_rightFactory.create(accessRightView);          
   
               // create access right that curUser can view only session2 objects
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iIntSessionDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             session2.getId(),
                                             null, null);
               m_rightFactory.create(accessRightView);          
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            //------------------------
            // 1. try to update enable users for int. session1 and int. session2 code
            //------------------------
            arIDs = new int[] {session1.getId(), session2.getId()};
            m_transaction.begin();
            try
            {
               iEnabled = m_sessionControl.updateEnable(arIDs, true);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // user1 has to be enabled
            testUser = (User)m_userFactory.get(
                                user1.getId(),
                                CallContext.getInstance().getCurrentDomainId());
            assertEquals("User 1 should be enabled", true, testUser.isLoginEnabled());
            // user1 has not be enabled
            testUser = (User)m_userFactory.get(
                                user2.getId(),
                                CallContext.getInstance().getCurrentDomainId());
            assertEquals("User 2 should not be enabled because of access right limitation",
                         false, testUser.isLoginEnabled());
            assertEquals("Incorrect number of enabled users", 1, iEnabled);
            
         }
         finally
         {
            deleteTestData(user1, user2, userRole, session1, session2, 
                           extsession1, extsession2, extsession3, extsession4);
         }
      }
   
      /**
       * Test checking if the user is logged in. User have action granted for all data 
       * objects of given type (no id and no categories specified when granting access).
       * 
       * @throws Exception - an error has occurred
       */
      public void testCheckLoggedInUserCheck(
      ) throws Exception 
      {
         User            curUser         = null;
         Role            userRole        = null;
         User            user1           = null;
         User            user2           = null;
         InternalSession session1        = null;
         InternalSession session2        = null;
         ExternalSession extsession1     = null;
         ExternalSession extsession2     = null;
         ExternalSession extsession3     = null;
         ExternalSession extsession4     = null;
         Object[]        arrTestData     = null;
         Object[]        arrTestUserData = null;
         Object[]        arrTestSessData = null;
         AccessRight     accessRightView = null;
         boolean         bLoggedInUser   = false;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_roleFactory.createPersonal(curUser);
   
            m_transaction.begin();
            try
            {
               // create 2 test users first
               arrTestUserData = constructTestUsers();
               user1 = (User)arrTestUserData[0];
               user2 = (User)arrTestUserData[1];
               user1 = (User)m_userFactory.create(user1);
               user2 = (User)m_userFactory.create(user2);
               // create 2 int. sessions belonging to each user one session
               arrTestSessData = constructTestSessions(user1.getId(), user2.getId());
               session1 = (InternalSession)arrTestSessData[0];
               session2 = (InternalSession)arrTestSessData[1];
               session1 = (InternalSession)m_internalSessionFactory.create(session1);
               session2 = (InternalSession)m_internalSessionFactory.create(session2);
               // create 4 external sessions belonging to each int. session 2 ext. sessions
               arrTestData = constructTestData(session1.getId(), session2.getId());
               extsession1 = (ExternalSession)arrTestData[0];
               extsession2 = (ExternalSession)arrTestData[1];
               extsession3 = (ExternalSession)arrTestData[2];
               extsession4 = (ExternalSession)arrTestData[3];
               extsession1 = (ExternalSession)m_externalSessionFactory.create(extsession1);
               extsession2 = (ExternalSession)m_externalSessionFactory.create(extsession2);
               extsession3 = (ExternalSession)m_externalSessionFactory.create(extsession3);
               extsession4 = (ExternalSession)m_externalSessionFactory.create(extsession4);
   
               // create role to the curUser
               userRole = new Role(DataObject.NEW_ID, 
                                   iCurrentDomainID, 
                                   "testname", 
                                   "testdescription", 
                                   true, 
                                   curUser.getId(), 
                                   false, null, null, null);
               userRole = (Role)m_roleFactory.create(userRole);
   
               m_roleFactory.assignToUser(curUser.getId(),
                                          Integer.toString(userRole.getId()));
   
               // create access right that curUser can view all session object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iIntSessionDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             AccessRight.NO_RIGHT_IDENTIFIER,
                                             null, null);
               m_rightFactory.create(accessRightView);          
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            //------------------------
            // 1. try to check if user 1 is logged in for int. session1 code
            //------------------------
            // user has to be logged in 
            bLoggedInUser = m_sessionControl.checkLoggedInUser(session1.getInternalSession());
            assertEquals("User 1 should be logged in", true, bLoggedInUser);
   
            //------------------------
            // 2. try to check if user 2 is logged in for int. session2 code
            //------------------------
            // user has to be logged in 
            bLoggedInUser = m_sessionControl.checkLoggedInUser(session2.getInternalSession());
            assertEquals("User 2 should be logged in", true, bLoggedInUser);
         }
         finally
         {
            deleteTestData(user1, user2, userRole, session1, session2, 
                           extsession1, extsession2, extsession3, extsession4);
         }
      }
   
      /**
       * Test checking if the user is logged in. User have access granted to specific data object
       * (identified by identifier) of given data type.
       * 
       * @throws Exception - an error has occurred
       */
      public void testCheckLoggedInUserCheckId(
      ) throws Exception 
      {
         User            curUser         = null;
         Role            userRole        = null;
         User            user1           = null;
         User            user2           = null;
         InternalSession session1        = null;
         InternalSession session2        = null;
         ExternalSession extsession1     = null;
         ExternalSession extsession2     = null;
         ExternalSession extsession3     = null;
         ExternalSession extsession4     = null;
         Object[]        arrTestData     = null;
         Object[]        arrTestUserData = null;
         Object[]        arrTestSessData = null;
         AccessRight     accessRightView = null;
         boolean         bLoggedInUser   = false;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_roleFactory.createPersonal(curUser);
   
            m_transaction.begin();
            try
            {
               // create 2 test users first
               arrTestUserData = constructTestUsers();
               user1 = (User)arrTestUserData[0];
               user2 = (User)arrTestUserData[1];
               user1 = (User)m_userFactory.create(user1);
               user2 = (User)m_userFactory.create(user2);
               // create 2 int. sessions belonging to each user one session
               arrTestSessData = constructTestSessions(user1.getId(), user2.getId());
               session1 = (InternalSession)arrTestSessData[0];
               session2 = (InternalSession)arrTestSessData[1];
               session1 = (InternalSession)m_internalSessionFactory.create(session1);
               session2 = (InternalSession)m_internalSessionFactory.create(session2);
               // create 4 external sessions belonging to each int. session 2 ext. sessions
               arrTestData = constructTestData(session1.getId(), session2.getId());
               extsession1 = (ExternalSession)arrTestData[0];
               extsession2 = (ExternalSession)arrTestData[1];
               extsession3 = (ExternalSession)arrTestData[2];
               extsession4 = (ExternalSession)arrTestData[3];
               extsession1 = (ExternalSession)m_externalSessionFactory.create(extsession1);
               extsession2 = (ExternalSession)m_externalSessionFactory.create(extsession2);
               extsession3 = (ExternalSession)m_externalSessionFactory.create(extsession3);
               extsession4 = (ExternalSession)m_externalSessionFactory.create(extsession4);
   
               // create role to the curUser
               userRole = new Role(DataObject.NEW_ID, 
                                   iCurrentDomainID, 
                                   "testname", 
                                   "testdescription", 
                                   true, 
                                   curUser.getId(), 
                                   false, null, null, null);
               userRole = (Role)m_roleFactory.create(userRole);
   
               m_roleFactory.assignToUser(curUser.getId(),
                                          Integer.toString(userRole.getId()));
   
               // create access right that curUser can view ony session1 object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iIntSessionDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             session1.getId(),
                                             null, null);
               m_rightFactory.create(accessRightView);          
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            //------------------------
            // 1. try to check if user 1 is logged in for int. session1 code
            //------------------------
            // user has to be logged in 
            bLoggedInUser = m_sessionControl.checkLoggedInUser(session1.getInternalSession());
            assertEquals("User 1 should be logged in", true, bLoggedInUser);
   
            //------------------------
            // 2. try to check if user 2 is logged in for int. session2 code
            //------------------------
            // user has to be logged in
            // There are not checked access rights for session in particular controller method
            bLoggedInUser = m_sessionControl.checkLoggedInUser(session2.getInternalSession());
            assertEquals("User 2 should be logged in", true, bLoggedInUser);
         }
         finally
         {
            deleteTestData(user1, user2, userRole, session1, session2, 
                           extsession1, extsession2, extsession3, extsession4);
         }
      }
   
      /**
       * Test for getting of the user ID knowing just internal session code. User 
       * has to have action granted for all data objects of given type (no id and 
       * no categories specified when granting access).
       * 
       * @throws Exception - an error has occurred
       */
      public void testGetUserIdCheck(
      ) throws Exception 
      {
         User            curUser         = null;
         Role            userRole        = null;
         User            user1           = null;
         User            user2           = null;
         InternalSession session1        = null;
         InternalSession session2        = null;
         ExternalSession extsession1     = null;
         ExternalSession extsession2     = null;
         ExternalSession extsession3     = null;
         ExternalSession extsession4     = null;
         Object[]        arrTestData     = null;
         Object[]        arrTestUserData = null;
         Object[]        arrTestSessData = null;
         AccessRight     accessRightView = null;
         int             iUserID         = DataObject.NEW_ID;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_roleFactory.createPersonal(curUser);
   
            m_transaction.begin();
            try
            {
               // create 2 test users first
               arrTestUserData = constructTestUsers();
               user1 = (User)arrTestUserData[0];
               user2 = (User)arrTestUserData[1];
               user1 = (User)m_userFactory.create(user1);
               user2 = (User)m_userFactory.create(user2);
               // create 2 int. sessions belonging to each user one session
               arrTestSessData = constructTestSessions(user1.getId(), user2.getId());
               session1 = (InternalSession)arrTestSessData[0];
               session2 = (InternalSession)arrTestSessData[1];
               session1 = (InternalSession)m_internalSessionFactory.create(session1);
               session2 = (InternalSession)m_internalSessionFactory.create(session2);
               // create 4 external sessions belonging to each int. session 2 ext. sessions
               arrTestData = constructTestData(session1.getId(), session2.getId());
               extsession1 = (ExternalSession)arrTestData[0];
               extsession2 = (ExternalSession)arrTestData[1];
               extsession3 = (ExternalSession)arrTestData[2];
               extsession4 = (ExternalSession)arrTestData[3];
               extsession1 = (ExternalSession)m_externalSessionFactory.create(extsession1);
               extsession2 = (ExternalSession)m_externalSessionFactory.create(extsession2);
               extsession3 = (ExternalSession)m_externalSessionFactory.create(extsession3);
               extsession4 = (ExternalSession)m_externalSessionFactory.create(extsession4);
   
               // create role to the curUser
               userRole = new Role(DataObject.NEW_ID, 
                                   iCurrentDomainID, 
                                   "testname", 
                                   "testdescription", 
                                   true, 
                                   curUser.getId(), 
                                   false, null, null, null);
               userRole = (Role)m_roleFactory.create(userRole);
   
               m_roleFactory.assignToUser(curUser.getId(),
                                          Integer.toString(userRole.getId()));
   
               // create access right that curUser can view all session object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iIntSessionDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             AccessRight.NO_RIGHT_IDENTIFIER,
                                             null, null);
               m_rightFactory.create(accessRightView);          
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            //------------------------
            // 1. try to get user ID for int. session1 code
            //------------------------
            // user ID have to be retrieved 
            iUserID = m_sessionControl.getUserId(session1.getInternalSession());
            assertEquals("User ID 1 should be retrieved", user1.getId(), iUserID);
   
            //------------------------
            // 2. try to get sessions for int. session2 code
            //------------------------
            // user ID have to be retrieved 
            iUserID = m_sessionControl.getUserId(session2.getInternalSession());
            assertEquals("User ID 2 should be retrieved", user2.getId(), iUserID);
         }
         finally
         {
            deleteTestData(user1, user2, userRole, session1, session2, 
                           extsession1, extsession2, extsession3, extsession4);
         }
      }
   
      /**
       * Test for getting of the user ID knowing just internal session code.  User 
       * has to have access granted to specific data object (identified by identifier) 
       * of given data type.
       * @throws Exception - an error has occurred
       */
      public void testGetUserIdCheckId(
      ) throws Exception 
      {
         User            curUser         = null;
         Role            userRole        = null;
         User            user1           = null;
         User            user2           = null;
         InternalSession session1        = null;
         InternalSession session2        = null;
         ExternalSession extsession1     = null;
         ExternalSession extsession2     = null;
         ExternalSession extsession3     = null;
         ExternalSession extsession4     = null;
         Object[]        arrTestData     = null;
         Object[]        arrTestUserData = null;
         Object[]        arrTestSessData = null;
         AccessRight     accessRightView = null;
         int             iUserID         = DataObject.NEW_ID;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_roleFactory.createPersonal(curUser);
   
            m_transaction.begin();
            try
            {
               // create 2 test users first
               arrTestUserData = constructTestUsers();
               user1 = (User)arrTestUserData[0];
               user2 = (User)arrTestUserData[1];
               user1 = (User)m_userFactory.create(user1);
               user2 = (User)m_userFactory.create(user2);
               // create 2 int. sessions belonging to each user one session
               arrTestSessData = constructTestSessions(user1.getId(), user2.getId());
               session1 = (InternalSession)arrTestSessData[0];
               session2 = (InternalSession)arrTestSessData[1];
               session1 = (InternalSession)m_internalSessionFactory.create(session1);
               session2 = (InternalSession)m_internalSessionFactory.create(session2);
               // create 4 external sessions belonging to each int. session 2 ext. sessions
               arrTestData = constructTestData(session1.getId(), session2.getId());
               extsession1 = (ExternalSession)arrTestData[0];
               extsession2 = (ExternalSession)arrTestData[1];
               extsession3 = (ExternalSession)arrTestData[2];
               extsession4 = (ExternalSession)arrTestData[3];
               extsession1 = (ExternalSession)m_externalSessionFactory.create(extsession1);
               extsession2 = (ExternalSession)m_externalSessionFactory.create(extsession2);
               extsession3 = (ExternalSession)m_externalSessionFactory.create(extsession3);
               extsession4 = (ExternalSession)m_externalSessionFactory.create(extsession4);
   
               // create role to the curUser
               userRole = new Role(DataObject.NEW_ID, 
                                   iCurrentDomainID, 
                                   "testname", 
                                   "testdescription", 
                                   true, 
                                   curUser.getId(), 
                                   false, null, null, null);
               userRole = (Role)m_roleFactory.create(userRole);
   
               m_roleFactory.assignToUser(curUser.getId(),
                                          Integer.toString(userRole.getId()));
   
               // create access right that curUser can view only session1 object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iIntSessionDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             session1.getId(),
                                             null, null);
               m_rightFactory.create(accessRightView);
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            //------------------------
            // 1. try to get user ID for int. session1 code
            //------------------------
            // user ID have to be retrieved 
            iUserID = m_sessionControl.getUserId(session1.getInternalSession());
            assertEquals("User ID 1 should be retrieved", user1.getId(), iUserID);
   
            //------------------------
            // 2. try to get sessions for int. session2 code
            //------------------------
            // There is not checking of the access rights in that method within 
            // the controller, so there has be retrieved user ID  
            // user ID have to be retrieved 
            iUserID = m_sessionControl.getUserId(session2.getInternalSession());
            assertEquals("User ID 2 should be retrieved", user2.getId(), iUserID);
         }
         finally
         {
            deleteTestData(user1, user2, userRole, session1, session2, 
                           extsession1, extsession2, extsession3, extsession4);
         }
      }
   
      // helper methods -----------------------------------------------------------------
      
      /**
       * Method changes current user setting (super user flag to false) - it will be used 
       * for all tests
       * 
       * @return User - changed cutrrent user
       * @throws OSSException - error occurred during changing of the current user
       */ 
      private User changeCurrentUser(
      ) throws OSSException
      {
          User originalCurrentUser = (User) CallContext.getInstance().getCurrentUser();
          User curUser = new User(originalCurrentUser.getId(),
                            originalCurrentUser.getDomainId(),
                            originalCurrentUser.getFirstName(),
                            originalCurrentUser.getLastName(),
                            originalCurrentUser.getPhone(),
                            originalCurrentUser.getFax(),
                            originalCurrentUser.getAddress(),
                            originalCurrentUser.getEmail(),
                            originalCurrentUser.getLoginName(),
                            originalCurrentUser.getPassword(),
                            true,
                            originalCurrentUser.isGuestAccessEnabled(),
                            false, // set super user flag to false
                            originalCurrentUser.isInternalUser(),
                            originalCurrentUser.getCreationTimestamp(),
                            originalCurrentUser.getModificationTimestamp(),
                            true
                           );
   
          CallContext.getInstance().reset();
          CallContext.getInstance().setCurrentUserAndSession(curUser, null);
   
          return curUser;
      }
   
      /**
       * Method construct 2 test users that will be used for all tests
       * 
       * @return Object[] - array of created users
       *                  - index 0 = user1 object; index 1 = user2 object
       * @throws OSSException - error occurred during of test users creating
       */
      private Object[] constructTestUsers(
      ) throws OSSException
      {
         User user1 = null;
         User user2 = null;
         int iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
   
         // create 2 test users
         user1 = new User(DataObject.NEW_ID,
                          iCurrentDomainID,
                          "test_first_name1",
                          "test_last_name1",
                          "test_phone1",
                          "test_fax1",
                          "test_address1",
                          "test_email1",
                          "test_login_name1",
                          "test_password1",
                          false, // login enabled 
                          true, false, // super user
                          true, null, null, true
                         );
      
         user2 = new User(DataObject.NEW_ID,
                          iCurrentDomainID,
                          "test_first_name2",
                          "test_last_name2",
                          "test_phone2",
                          "test_fax2",
                          "test_address2",
                          "test_email2",
                          "test_login_name2",
                          "test_password2",
                          false, // login enabled 
                          true, true, // super user
                          true, null, null, true
                         );
   
         return new Object[] {user1, user2}; 
      }
   
      /**
       * Method construct InternalSession that will be used for all tests
       *
       * @param iUser1Id - ID of the user1 the internal session will belong to
       * @param iUser2Id - ID of the user2 the internal session will belong to
       * @return Object[] - constructed InternalSession objects
       *                  - index 0 = session1; index 1 = session2
       * @throws OSSException - error occurred during of test InternalSessions data creating
       */
      private Object[] constructTestSessions(
         int iUser1Id,
         int iUser2Id
      ) throws OSSException
      {
         InternalSession session1 = null;
         InternalSession session2 = null;
   
         // create test internal session belonging to the user1      
         session1 = new InternalSession(DataObject.NEW_ID,
                                        CallContext.getInstance().getCurrentDomainId(),
                                        iUser1Id,
                                        "test_generated_code1",
                                        "test_c_IP1",
                                        "test_client_type1",
                                        null,
                                        false
                                        );
         // create test internal session belonging to the user2
         session2 = new InternalSession(DataObject.NEW_ID,
                                        CallContext.getInstance().getCurrentDomainId(),
                                        iUser2Id,
                                        "test_generated_code2",
                                        "test_c_IP2",
                                        "test_client_type2",
                                        null,
                                        false
                                        );
         return new Object[] {session1, session2};
      }
   
      /**
       * Method construct 4 external sessions that will be used for all tests
       * 
       * @param iSession1Id - ID of the session 1 the external sessions will belong to
       * @param iSession2Id - ID of the session 2 the external sessions will belong to
       * @return Object[] - array of created external sessions
       *                  - index 0 = external session1 object
       *                  - index 1 = external session2 object
       * @throws OSSException - error occurred during of test data creating
       */
      private Object[] constructTestData(
        int iSession1Id,
        int iSession2Id
      ) throws OSSException
      {
         ExternalSession extsession1 = null;
         ExternalSession extsession2 = null;
         ExternalSession extsession3 = null;
         ExternalSession extsession4 = null;
   
         int iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
   
         // create 24 server sessions belonging to the internal session 1
         extsession1 = new ExternalSession(DataObject.NEW_ID,
                                           iCurrentDomainID,
                                           iSession1Id,
                                           "test_server_session_gen_code_1",
                                           "test_srv_IP_1",
                                           null
                                          );
      
         extsession2 = new ExternalSession(DataObject.NEW_ID,
                                           iCurrentDomainID,
                                           iSession1Id,
                                           "test_server_session_gen_code_2",
                                           "test_srv_IP_2",
                                           null
                                          );
   
         // create 2 server sessions belonging to the internal session 2
         extsession3 = new ExternalSession(DataObject.NEW_ID,
                                           iCurrentDomainID,
                                           iSession2Id,
                                           "test_server_session_gen_code_3",
                                           "test_srv_IP_1",
                                           null
                                          );
      
         extsession4 = new ExternalSession(DataObject.NEW_ID,
                                           iCurrentDomainID,
                                           iSession2Id,
                                           "test_server_session_gen_code_4",
                                           "test_srv_IP_2",
                                           null
                                          );
   
         return new Object[] {extsession1, extsession2, extsession3, extsession4};  
      }
   
      /**
       * Method for deleting test data - it will be used for all tests
       * 
       * @param user1 - user1 that will be deleted
       * @param user2 - user2 that will be deleted
       * @param userRole - user session that will be deleted
       * @param session1 - internal session 1 that will be deleted
       * @param session2 - internal session 2 that will be deleted
       * @param extsession1 - external session 1 that will be deleted
       * @param extsession2 - external session 2 that will be deleted
       * @param extsession3 - external session 3 that will be deleted
       * @param extsession4 - external session 4 that will be deleted
       * @throws Exception - error occurred during deleting of test data 
       */
      private void deleteTestData(
         User user1,
         User user2,
         Role userRole,
         InternalSession session1,
         InternalSession session2,
         ExternalSession extsession1,
         ExternalSession extsession2,
         ExternalSession extsession3,
         ExternalSession extsession4
      ) throws Exception
      {
         m_transaction.begin();
         try
         {
            if ((extsession1 != null) && (extsession1.getId() != DataObject.NEW_ID))
            {
               m_externalSessionFactory.delete(
                  extsession1.getId(),
                  CallContext.getInstance().getCurrentDomainId());
            }
            if ((extsession2 != null) && (extsession2.getId() != DataObject.NEW_ID))
            {
               m_externalSessionFactory.delete(
                  extsession2.getId(),
                  CallContext.getInstance().getCurrentDomainId());
            }
            if ((extsession3 != null) && (extsession3.getId() != DataObject.NEW_ID))
            {
               m_externalSessionFactory.delete(
                  extsession3.getId(),
                  CallContext.getInstance().getCurrentDomainId());
            }
            if ((extsession4 != null) && (extsession4.getId() != DataObject.NEW_ID))
            {
               m_externalSessionFactory.delete(
                  extsession4.getId(),
                  CallContext.getInstance().getCurrentDomainId());
            }
            if ((session1 != null) && (session1.getId() != DataObject.NEW_ID))
            {
               m_internalSessionFactory.delete(
                  session1.getId(),
                  CallContext.getInstance().getCurrentDomainId());
            }
            if ((session2 != null) && (session2.getId() != DataObject.NEW_ID))
            {
               m_internalSessionFactory.delete(
                  session2.getId(),
                  CallContext.getInstance().getCurrentDomainId());
            }
            if ((userRole != null) && (userRole.getId() != DataObject.NEW_ID))
            {
               m_rightFactory.deleteAllForRole(userRole.getId());
               m_roleFactory.removeFromUsers(String.valueOf(userRole.getUserId()), false);
               //m_roleFactory.delete(userRole.getId());
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
}
