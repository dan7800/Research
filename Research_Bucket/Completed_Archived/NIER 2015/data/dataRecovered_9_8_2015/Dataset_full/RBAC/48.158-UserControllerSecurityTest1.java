/*
 * Copyright (c) 2004 - 2009 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: UserControllerSecurityTest1.java,v 1.10 2009/09/20 05:32:57 bastafidli Exp $
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
import org.opensubsystems.security.data.Role;
import org.opensubsystems.security.data.User;
import org.opensubsystems.security.data.UserDataDescriptor;
import org.opensubsystems.security.patterns.listdata.logic.impl.SecureListControllerTest;
import org.opensubsystems.security.persist.AccessRightFactory;
import org.opensubsystems.security.persist.RoleFactory;
import org.opensubsystems.security.persist.UserFactory;
import org.opensubsystems.security.persist.db.AccessRightDatabaseFactory;
import org.opensubsystems.security.persist.db.UserListDatabaseTestUtils;
import org.opensubsystems.security.util.ActionConstants;
import org.opensubsystems.security.utils.TestRoleDatabaseFactoryUtils;

/**
 * Test for  security facade above the persistence layer. Each method of user
 * controller check if user was granted access rights required to perform any
 * given operation. We do not test, if the security checks are performed correctly 
 * and if they work at all. 
 * There are contained only tests for create/update methods.
 * 
 * @version $Id: UserControllerSecurityTest1.java,v 1.10 2009/09/20 05:32:57 bastafidli Exp $
 * @author Julo Legeny
 * @code.reviewed 1.3 2004/12/17 12:21:36 jlegeny
 * @code.reviewer Miro Halas
 */
public final class UserControllerSecurityTest1 extends Tests
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private UserControllerSecurityTest1(
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
      TestSuite suite = new DatabaseTestSuite("UserControllerSecurityTest1");
      suite.addTestSuite(UserControllerSecurityTestInternal1.class);
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
   public static class UserControllerSecurityTestInternal1 extends SecureListControllerTest
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
       * Controller used to manipulate users.
       */
      protected UserController m_userControl;
   
      /**
       * Factory utilities to manage roles.
       */
      protected TestRoleDatabaseFactoryUtils m_roleFactoryUtils;
   
      /**
       * Data type assigned to user data types.
       */
      protected int m_iUserDataType;
   
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
      public UserControllerSecurityTestInternal1(
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
         m_userControl = (UserController)ControllerManager.getInstance(
                                         UserController.class);
         m_roleFactoryUtils = new TestRoleDatabaseFactoryUtils();
         
         UserDataDescriptor userDescriptor;
         
         userDescriptor = (UserDataDescriptor)DataDescriptorManager.getInstance(
                                                 UserDataDescriptor.class);
         
         m_iUserDataType = userDescriptor.getDataType();
      }
   
      // Tests ////////////////////////////////////////////////////////////////////
   
      /**
       * Test for creating of the user. User have action granted for all data objects 
       * of given type (no id and no categories specified when granting access).
       * 
       *  @throws Exception - an error has occurred
       */
      public void testCreateCheck(
      ) throws Exception 
      {
         User        curUser      = null;
         User        user1        = null;
         User        user2        = null;
         User        testUser     = null;
         Role        userRole     = null;
         Object[]    arrTestUsers = null;
         
         AccessRight accessRightModify  = null;
         AccessRight accessRightView  = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_transaction.begin();
            try
            {
               m_roleFactory.createPersonal(curUser);

               // create 2 test users
               arrTestUsers = constructTestUsers();
               user1 = (User)arrTestUsers[0];
               user2 = (User)arrTestUsers[1];
   
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
   
               // create access right that curUser can create only user objects 
               // that are not superusers
               accessRightModify = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_CREATE,
                                             m_iUserDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             AccessRight.NO_RIGHT_IDENTIFIER,
                                             null, null);
               m_rightFactory.create(accessRightModify);          
   
               // create access right that curUser can view all user object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iUserDataType,
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
            // 1. try to create user1
            //------------------------
            m_transaction.begin();
            try
            {
               // try to create user 1
               testUser = (User)m_userControl.create(user1);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            // user has to be created 
            assertNotNull("User 1 should not be null", testUser);
            assertTrue("User 1 should be created but wasn't", testUser.getId() > DataObject.NEW_ID);
   
            //------------------------
            // 2. try to create user2
            //------------------------
            m_transaction.begin();
            try
            {
               // try to create user without roles (role1)
               testUser = (User)m_userControl.create(user2);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            // user has to be created 
            assertNotNull("User 2 should not be null", testUser);
            assertTrue("User 2 should be created but wasn't", testUser.getId() > DataObject.NEW_ID);
         }
         finally
         {
            deleteTestData(userRole, user1, user2);
         }
      }
   
      /**
       * Test for creating of the user. User have access granted to a group 
       * of objects based on categories the objects belong to.
       * 
       *  @throws Exception - an error has occurred
       */
      public void testCreateCheckCategorySuperUser(
      ) throws Exception 
      {
         User        curUser      = null;
         User        user1        = null;
         User        user2        = null;
         User        testUser     = null;
         Role        userRole     = null;
         Object[]    arrTestUsers = null;
         
         AccessRight accessRightModify  = null;
         AccessRight accessRightView  = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_transaction.begin();
            try
            {
               m_roleFactory.createPersonal(curUser);

               // create 2 test users
               arrTestUsers = constructTestUsers();
               user1 = (User)arrTestUsers[0];
               user2 = (User)arrTestUsers[1];
   
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
   
               // create access right that curUser can create only user objects 
               // that are not superusers
               accessRightModify = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_CREATE,
                                             m_iUserDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             UserDataDescriptor.COL_USER_SUPER_USER,
                                             DataCondition.DATA_CODE_FLAG_NO,
                                             null, null);
               m_rightFactory.create(accessRightModify);          
   
               // create access right that curUser can view all user object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iUserDataType,
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
            // 1. try to create user1
            //------------------------
            m_transaction.begin();
            try
            {
               // try to create user without roles (role1)
               testUser = (User)m_userControl.create(user1);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            // user has to be updated 
            assertNotNull("User 1 should not be null", testUser);
            assertTrue("User 1 should be created but wasn't", testUser.getId() > DataObject.NEW_ID);
   
            //------------------------
            // 2. try to create user2
            //------------------------
            m_transaction.begin();
            try
            {
               // try to create user 2
               testUser = (User)m_userControl.create(user2);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            // user has not be created 
            assertNull("User 2 should not be created", testUser);
         }
         finally
         {
            deleteTestData(userRole, user1, user2);
         }
      }
   
      /**
       * Test for saving of the user. User have action granted for all data objects 
       * of given type (no id and no categories specified when granting access).
       * 
       *  @throws Exception - an error has occurred
       */
      public void testSaveCheck(
      ) throws Exception 
      {
         User        curUser      = null;
         User        user1        = null;
         User        user2        = null;
         User        user3        = null;
         User        testUser     = null;
         Role        userRole     = null;
         Object[]    arrTestUsers = null;
         
         AccessRight accessRightModify  = null;
         AccessRight accessRightView  = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_transaction.begin();
            try
            {
               m_roleFactory.createPersonal(curUser);

               // create 2 test users
               arrTestUsers = constructTestUsers();
               user1 = (User)arrTestUsers[0];
               user2 = (User)arrTestUsers[1];
   
               user1 = (User)m_userFactory.create(user1);
               m_roleFactory.createPersonal(user1);
               
               user2 = (User)m_userFactory.create(user2);
               m_roleFactory.createPersonal(user2);
   
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
   
               // create access right that curUser can modify only 'user1' object
               accessRightModify = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_MODIFY,
                                             m_iUserDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             AccessRight.NO_RIGHT_IDENTIFIER,
                                             null, null);
               m_rightFactory.create(accessRightModify);          
   
               // create access right that curUser can view all user object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iUserDataType,
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
            // 1. try to modify user1
            //------------------------
            user3 = new User(user1.getId(),
                             iCurrentDomainID,
                             "test_first_name_3",
                             "test_last_name_3",
                             "test_phone_3",
                             "test_fax_3",
                             "test_address_3",
                             user1.getEmail(),
                             user1.getLoginName(),
                             user1.getPassword(),
                             user1.isLoginEnabled(),
                             user1.isGuestAccessEnabled(),
                             user1.isSuperUser(),
                             user1.isInternalUser(),
                             user1.getCreationTimestamp(),
                             user1.getModificationTimestamp(), true
                             );
   
            m_transaction.begin();
            try
            {
               // try to modify user 
               m_userControl.save(user3);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            // user has to be updated 
            testUser = (User)m_userControl.get(user1.getId());
            assertNotNull("User 1 should not be null", testUser);
            assertEquals("User 1 should be changed but wasn't", user3, testUser);
            
            //------------------------
            // 2. try to modify user2
            //------------------------
            user3 = new User(user2.getId(),
                             iCurrentDomainID,
                             "test_first_name_3",
                             "test_last_name_3",
                             "test_phone_3",
                             "test_fax_3",
                             "test_address_3",
                             user2.getEmail(),
                             user2.getLoginName(),
                             user2.getPassword(),
                             user2.isLoginEnabled(),
                             user2.isGuestAccessEnabled(),
                             user2.isSuperUser(),
                             user2.isInternalUser(),
                             user2.getCreationTimestamp(),
                             user2.getModificationTimestamp(), true
                             );
   
            m_transaction.begin();
            try
            {
               // try to modify user 
               m_userControl.save(user3);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            // user has to be updated 
            testUser = (User)m_userControl.get(user2.getId());
            assertNotNull("User 2 should not be null", testUser);
            assertEquals("User 2 should be changed but wasn't", user3, testUser);
         }
         finally
         {
            deleteTestData(userRole, user1, user2);
         }
      }
   
      /**
       * Test for saving of the user. User have access granted to specific
       * data object (identified by identifier) of given data type.
       * 
       *  @throws Exception - an error has occurred
       */
      public void testSaveCheckId(
      ) throws Exception 
      {
         User        curUser      = null;
         User        user1        = null;
         User        user2        = null;
         User        user3        = null;
         User        testUser     = null;
         Role        userRole     = null;
         Object[]    arrTestUsers = null;
         
         AccessRight accessRightModify  = null;
         AccessRight accessRightView  = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_transaction.begin();
            try
            {
               m_roleFactory.createPersonal(curUser);

               // create 2 test users
               arrTestUsers = constructTestUsers();
               user1 = (User)arrTestUsers[0];
               user2 = (User)arrTestUsers[1];
   
               user1 = (User)m_userFactory.create(user1);
               m_roleFactory.createPersonal(user1);
               
               user2 = (User)m_userFactory.create(user2);
   
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
   
               // create access right that curUser can modify only 'user1' object
               accessRightModify = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_MODIFY,
                                             m_iUserDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             user1.getId(),
                                             null, null);
               m_rightFactory.create(accessRightModify);          
   
               // create access right that curUser can view all user object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iUserDataType,
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
            // 1. try to modify user1
            //------------------------
            user3 = new User(user1.getId(),
                             iCurrentDomainID,
                             "test_first_name_3",
                             "test_last_name_3",
                             "test_phone_3",
                             "test_fax_3",
                             "test_address_3",
                             user1.getEmail(),
                             user1.getLoginName(),
                             user1.getPassword(),
                             user1.isLoginEnabled(),
                             user1.isGuestAccessEnabled(),
                             user1.isSuperUser(),
                             user1.isInternalUser(),
                             user1.getCreationTimestamp(),
                             user1.getModificationTimestamp(), true
                             );
   
            m_transaction.begin();
            try
            {
               // try to modify user 
               m_userControl.save(user3);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            // user has to be updated 
            testUser = (User)m_userControl.get(user1.getId());
            assertNotNull("User 1 should not be null", testUser);
            assertEquals("User 1 should be changed but wasn't", user3, testUser);
            
            //------------------------
            // 2. try to modify user2
            //------------------------
            user3 = new User(user2.getId(),
                             iCurrentDomainID,
                             "test_first_name_3",
                             "test_last_name_3",
                             "test_phone_3",
                             "test_fax_3",
                             "test_address_3",
                             user2.getEmail(),
                             user2.getLoginName(),
                             user2.getPassword(),
                             user2.isLoginEnabled(),
                             user2.isGuestAccessEnabled(),
                             user2.isSuperUser(),
                             user2.isInternalUser(),
                             user2.getCreationTimestamp(),
                             user2.getModificationTimestamp(), true
                             );
   
            m_transaction.begin();
            try
            {
               // try to modify user 
               m_userControl.save(user3);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            // user has not be updated 
            testUser = (User)m_userControl.get(user2.getId());
            assertNotNull("User 2 should not be null", testUser);
            assertEquals("User 2 should not be changed because of access rights limitation",
                         user2, testUser);
         }
         finally
         {
            deleteTestData(userRole, user1, user2);
         }
      }
   
      /**
       * Test for saving of the user. User have access granted to a group 
       * of objects based on categories the objects belong to.
       * 
       *  @throws Exception - an error has occurred
       */
      public void testSaveCheckCategorySuperUser(
      ) throws Exception 
      {
         User        curUser      = null;
         User        user1        = null;
         User        user2        = null;
         User        user3        = null;
         User        testUser     = null;
         Role        userRole     = null;
         Object[]    arrTestUsers = null;
         
         AccessRight accessRightModify  = null;
         AccessRight accessRightView  = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_transaction.begin();
            try
            {
               m_roleFactory.createPersonal(curUser);

               // create 2 test users
               arrTestUsers = constructTestUsers();
               user1 = (User)arrTestUsers[0];
               user2 = (User)arrTestUsers[1];
   
               user1 = (User)m_userFactory.create(user1);
               m_roleFactory.createPersonal(user1);
               
               user2 = (User)m_userFactory.create(user2);
               m_roleFactory.createPersonal(user2);
   
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
   
               // create access right that curUser can modify only 'user1' object
               accessRightModify = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_MODIFY,
                                             m_iUserDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             UserDataDescriptor.COL_USER_SUPER_USER,
                                             DataCondition.DATA_CODE_FLAG_NO,
                                             null, null);
               m_rightFactory.create(accessRightModify);          
   
               // create access right that curUser can view all user object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iUserDataType,
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
            // 1. try to modify user1
            //------------------------
            user3 = new User(user1.getId(),
                             iCurrentDomainID,
                             "test_first_name_3",
                             "test_last_name_3",
                             "test_phone_3",
                             "test_fax_3",
                             "test_address_3",
                             user1.getEmail(),
                             user1.getLoginName(),
                             user1.getPassword(),
                             user1.isLoginEnabled(),
                             user1.isGuestAccessEnabled(),
                             user1.isSuperUser(),
                             user1.isInternalUser(),
                             user1.getCreationTimestamp(),
                             user1.getModificationTimestamp(), true
                             );
   
            m_transaction.begin();
            try
            {
               // try to modify user without roles (role1)
               m_userControl.save(user3);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            // user has to be updated 
            testUser = (User)m_userControl.get(user1.getId());
            assertNotNull("User 1 should not be null", testUser);
            assertEquals("User 1 should be changed but wasn't", user3, testUser);
   
            //------------------------
            // 2. try to modify user2
            //------------------------
            user3 = new User(user2.getId(),
                             iCurrentDomainID,
                             "test_first_name_3",
                             "test_last_name_3",
                             "test_phone_3",
                             "test_fax_3",
                             "test_address_3",
                             user2.getEmail(),
                             user2.getLoginName(),
                             user2.getPassword(),
                             user2.isLoginEnabled(),
                             user2.isGuestAccessEnabled(),
                             user2.isSuperUser(),
                             user2.isInternalUser(),
                             user2.getCreationTimestamp(),
                             user2.getModificationTimestamp(), true
                             );
   
            m_transaction.begin();
            try
            {
               // try to modify user 1
               m_userControl.save(user3);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            // user has not be updated 
            testUser = (User)m_userControl.get(user2.getId());
            assertNotNull("User 2 should not be null", testUser);
            assertEquals("User 21 should not be changed because of access rights limitation",
                         user2, testUser);
         }
         finally
         {
            deleteTestData(userRole, user1, user2);
         }
      }
   
      /**
       * Test for deleting of the user. User have action granted for all data objects 
       * of given type (no id and no categories specified when granting access).
       * 
       *  @throws Exception - an error has occurred
       */
      public void testDeleteCheck(
      ) throws Exception 
      {
         User        curUser      = null;
         User        user1        = null;
         User        user2        = null;
         User        testUser     = null;
         Role        userRole     = null;
         Object[]    arrTestUsers = null;
         
         AccessRight accessRightModify  = null;
         AccessRight accessRightView  = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_transaction.begin();
            try
            {
               m_roleFactory.createPersonal(curUser);

               // create 2 test users
               arrTestUsers = constructTestUsers();
               user1 = (User)arrTestUsers[0];
               user2 = (User)arrTestUsers[1];
               
               user1 = (User)m_userFactory.create(user1);
               m_roleFactory.createPersonal(user1);
   
               user2 = (User)m_userFactory.create(user2);
               m_roleFactory.createPersonal(user2);
            
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
   
               // create access right that curUser can create only user objects 
               // that are not superusers
               accessRightModify = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_DELETE,
                                             m_iUserDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             AccessRight.NO_RIGHT_IDENTIFIER,
                                             null, null);
               m_rightFactory.create(accessRightModify);          
   
               // create access right that curUser can view all user object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iUserDataType,
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
            // 1. try to delete user1
            //------------------------
            m_transaction.begin();
            try
            {
               // try to delete user 1
               m_userControl.delete(user1.getId());
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            // user 1 has to be deleted
            testUser = (User)m_userControl.get(user1.getId());
            assertNull("User 1 should be deleted but wasn't", testUser);
            user1 = null;
   
            //------------------------
            // 2. try to delete user2
            //------------------------
            m_transaction.begin();
            try
            {
               // try to delete user 2
               m_userControl.delete(user2.getId());
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            // user 2 has to be deleted
            testUser = (User)m_userControl.get(user2.getId());
            assertNull("User 2 should be deleted but wasn't", testUser);
            user2 = null;
         }
         finally
         {
            deleteTestData(userRole, user1, user2);
         }
      }
   
      /**
       * Test for deleting of the user. User have access granted to specific
       * data object (identified by identifier) of given data type.
       * 
       *  @throws Exception - an error has occurred
       */
      public void testDeleteCheckId(
      ) throws Exception 
      {
         User        curUser      = null;
         User        user1        = null;
         User        user2        = null;
         User        testUser     = null;
         Role        userRole     = null;
         Object[]    arrTestUsers = null;
         
         AccessRight accessRightModify  = null;
         AccessRight accessRightView  = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_transaction.begin();
            try
            {
               m_roleFactory.createPersonal(curUser);

               // create 2 test users
               arrTestUsers = constructTestUsers();
               user1 = (User)arrTestUsers[0];
               user2 = (User)arrTestUsers[1];
               
               user1 = (User)m_userFactory.create(user1);
               m_roleFactory.createPersonal(user1);
   
               user2 = (User)m_userFactory.create(user2);
               m_roleFactory.createPersonal(user2);
   
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
   
               // create access right that curUser can create only user objects 
               // that are not superusers
               accessRightModify = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_DELETE,
                                             m_iUserDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             user2.getId(),
                                             null, null);
               m_rightFactory.create(accessRightModify);          
   
               // create access right that curUser can view all user object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iUserDataType,
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
            // 1. try to delete user1
            //------------------------
            m_transaction.begin();
            try
            {
               // try to delete user 1
               m_userControl.delete(user1.getId());
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            // user has not be deleted 
            testUser = (User)m_userControl.get(user1.getId());
            assertNotNull("User 1 should not be deleted because of access right limitation", 
                          testUser);
   
            //------------------------
            // 2. try to delete user2
            //------------------------
            m_transaction.begin();
            try
            {
               // try to delete user 2
               m_userControl.delete(user2.getId());
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            // user 2 has to be deleted
            testUser = (User)m_userControl.get(user2.getId());
            assertNull("User 2 should be deleted but wasn't", testUser);
            user2 = null;
         }
         finally
         {
            deleteTestData(userRole, user1, user2);
         }
      }
   
      /**
       * Test for deleting of the user. User have access granted to a group 
       * of objects based on categories the objects belong to.
       * 
       *  @throws Exception - an error has occurred
       */
      public void testDeleteCheckCategorySuperUser(
      ) throws Exception 
      {
         User        curUser      = null;
         User        user1        = null;
         User        user2        = null;
         User        testUser     = null;
         Role        userRole     = null;
         Object[]    arrTestUsers = null;
         
         AccessRight accessRightModify  = null;
         AccessRight accessRightView  = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_transaction.begin();
            try
            {
               m_roleFactory.createPersonal(curUser);

               // create 2 test users
               arrTestUsers = constructTestUsers();
               user1 = (User)arrTestUsers[0];
               user2 = (User)arrTestUsers[1];
               
               user1 = (User)m_userFactory.create(user1);
               m_roleFactory.createPersonal(user1);
   
               user2 = (User)m_userFactory.create(user2);
               m_roleFactory.createPersonal(user2);
   
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
   
               // create access right that curUser can create only user objects 
               // that are not superusers
               accessRightModify = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_DELETE,
                                             m_iUserDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             UserDataDescriptor.COL_USER_SUPER_USER,
                                             DataCondition.DATA_CODE_FLAG_NO,
                                             null, null);
               m_rightFactory.create(accessRightModify);          
   
               // create access right that curUser can view all user object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iUserDataType,
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
            // 1. try to delete user1
            //------------------------
            m_transaction.begin();
            try
            {
               // try to delete user 1
               m_userControl.delete(user1.getId());
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            // user 1 has to be deleted
            testUser = (User)m_userControl.get(user1.getId());
            assertNull("User 1 should be deleted but wasn't", testUser);
            user1 = null;
   
            //------------------------
            // 2. try to delete user2
            //------------------------
            m_transaction.begin();
            try
            {
               // try to delete user 2
               m_userControl.delete(user2.getId());
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            // user has not be deleted 
            testUser = (User)m_userControl.get(user2.getId());
            assertNotNull("User 2 should not be deleted because of access right limitation", 
                          testUser);
         }
         finally
         {
            deleteTestData(userRole, user1, user2);
         }
      }
   
      /**
       * Test for update enable of the user. User have action granted for all data objects 
       * of given type (no id and no categories specified when granting access).
       * 
       *  @throws Exception - an error has occurred
       */
      public void testUpdateEnableCheck(
      ) throws Exception 
      {
         User        curUser      = null;
         User        user1        = null;
         User        user2        = null;
         User        testUser     = null;
         Role        userRole     = null;
         Object[]    arrTestUsers = null;
         
         String strIDs = "";
         int    iUpdated = 0;
         
         AccessRight accessRightModify  = null;
         AccessRight accessRightView  = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_transaction.begin();
            try
            {
               m_roleFactory.createPersonal(curUser);

               // create 2 test users
               arrTestUsers = constructTestUsers();
               user1 = (User)arrTestUsers[0];
               user2 = (User)arrTestUsers[1];
            
               user1 = (User)m_userFactory.create(user1);
               m_roleFactory.createPersonal(user1);
               
               user2 = (User)m_userFactory.create(user2);
               m_roleFactory.createPersonal(user2);
   
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
               accessRightModify = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_MODIFY,
                                             m_iUserDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             AccessRight.NO_RIGHT_IDENTIFIER,
                                             null, null);
               m_rightFactory.create(accessRightModify);          
   
               // create access right that curUser can view all user object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iUserDataType,
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
            // 1. try to change update enable flag for user1 and user2
            //------------------------
   
            strIDs = user1.getId() + "," + user2.getId();
            m_transaction.begin();
            try
            {
               // try to modify users - update enable flag
               iUpdated = m_userControl.updateEnable(strIDs, false);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            // both users have to be updated
            assertEquals("Incorrect number of updated users", 2, iUpdated);
            testUser = (User)m_userControl.get(user1.getId());
            assertNotNull("User 1 should not be null", testUser);
            assertEquals("User 1 should be updated but wasn't", false, testUser.isLoginEnabled());
            testUser = (User)m_userControl.get(user2.getId());
            assertNotNull("User 2 should not be null", testUser);
            assertEquals("User 2 should be updated but wasn't", false, testUser.isLoginEnabled());
         }
         finally
         {
            deleteTestData(userRole, user1, user2);
         }
      }
   
      /**
       * Test for update enable of the user. User have access granted to specific
       * data object (identified by identifier) of given data type.
       *  
       * 
       *  @throws Exception - an error has occurred
       */
      public void testUpdateEnableCheckId(
      ) throws Exception 
      {
         User        curUser      = null;
         User        user1        = null;
         User        user2        = null;
         User        testUser     = null;
         Role        userRole     = null;
         Object[]    arrTestUsers = null;
         
         String strIDs = "";
         int    iUpdated = 0;
         
         AccessRight accessRightModify  = null;
         AccessRight accessRightView  = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_transaction.begin();
            try
            {
               m_roleFactory.createPersonal(curUser);

               // create 2 test users
               arrTestUsers = constructTestUsers();
               user1 = (User)arrTestUsers[0];
               user2 = (User)arrTestUsers[1];
               
               user1 = (User)m_userFactory.create(user1);
               m_roleFactory.createPersonal(user1);
               
               user2 = (User)m_userFactory.create(user2);
               m_roleFactory.createPersonal(user2);
   
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
   
               // create access right that curUser can modify only 'user1' object
               accessRightModify = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_MODIFY,
                                             m_iUserDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             user1.getId(),
                                             null, null);
               m_rightFactory.create(accessRightModify);          
   
               // create access right that curUser can view all user object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iUserDataType,
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
            // 1. try to change update enable flag for user1 and user2
            //------------------------
            strIDs = user1.getId() + "," + user2.getId();
            m_transaction.begin();
            try
            {
               // try to modify users - update enable flag
               iUpdated = m_userControl.updateEnable(strIDs, false);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            // both users have to be updated
            assertEquals("Incorrect number of updated users", 1, iUpdated);
            testUser = (User)m_userControl.get(user1.getId());
            assertNotNull("User 1 should not be null", testUser);
            assertEquals("User 1 should be updated but wasn't", false, testUser.isLoginEnabled());
            testUser = (User)m_userControl.get(user2.getId());
            assertNotNull("User 2 should not be null", testUser);
            assertEquals("User 2 should be updated but wasn't", true, testUser.isLoginEnabled());
         }
         finally
         {
            deleteTestData(userRole, user1, user2);
         }
      }
   
      /**
       * Test for update enable of the user. User have access granted to a group 
       * of objects based on categories the objects belong to.
       * 
       *  @throws Exception - an error has occurred
       */
      public void testUpdateEnableCheckCategorySuperUser(
      ) throws Exception 
      {
         User        curUser      = null;
         User        user1        = null;
         User        user2        = null;
         User        testUser     = null;
         Role        userRole     = null;
         Object[]    arrTestUsers = null;
   
         String strIDs = "";
         int    iUpdated = 0;
   
         AccessRight accessRightModify  = null;
         AccessRight accessRightView  = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_transaction.begin();
            try
            {
               m_roleFactory.createPersonal(curUser);

               // create 2 test users
               arrTestUsers = constructTestUsers();
               user1 = (User)arrTestUsers[0];
               user2 = (User)arrTestUsers[1];
               
               user1 = (User)m_userFactory.create(user1);
               m_roleFactory.createPersonal(user1);
               
               user2 = (User)m_userFactory.create(user2);
               m_roleFactory.createPersonal(user2);
   
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
   
               // create access right that curUser can modify only 'user1' object
               accessRightModify = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_MODIFY,
                                             m_iUserDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             UserDataDescriptor.COL_USER_SUPER_USER,
                                             DataCondition.DATA_CODE_FLAG_NO,
                                             null, null);
               m_rightFactory.create(accessRightModify);          
   
               // create access right that curUser can view all user object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iUserDataType,
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
            // 1. try to change update enable flag for user1 and user2
            //------------------------
            strIDs = user1.getId() + "," + user2.getId();
            m_transaction.begin();
            try
            {
               // try to modify users - update enable flag
               iUpdated = m_userControl.updateEnable(strIDs, false);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            // both users have to be updated
            assertEquals("Incorrect number of updated users", 1, iUpdated);
            testUser = (User)m_userControl.get(user1.getId());
            assertNotNull("User 1 should not be null", testUser);
            assertEquals("User 1 should be updated but wasn't", false, testUser.isLoginEnabled());
            testUser = (User)m_userControl.get(user2.getId());
            assertNotNull("User 2 should not be null", testUser);
            assertEquals("User 2 should be updated but wasn't", true, testUser.isLoginEnabled());
         }
         finally
         {
            deleteTestData(userRole, user1, user2);
         }
      }
   
      /**
       * Test for changing password of the user. User have action granted for all data objects 
       * of given type (no id and no categories specified when granting access).
       * 
       *  @throws Exception - an error has occurred
       */
      public void testChangePasswordCheck(
      ) throws Exception 
      {
         User        curUser      = null;
         User        user1        = null;
         User        user2        = null;
         User        testUser     = null;
         Role        userRole     = null;
         Object[]    arrTestUsers = null;
         
         String strNewEncryptedPassword = null;
         String strNewPassword = "test_new_password";
         
         AccessRight accessRightModify  = null;
         AccessRight accessRightView  = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_transaction.begin();
            try
            {
               m_roleFactory.createPersonal(curUser);

               // create 2 test users
               arrTestUsers = constructTestUsers();
               user1 = (User)arrTestUsers[0];
               user2 = (User)arrTestUsers[1];
            
               user1 = (User)m_userFactory.create(user1);
               m_roleFactory.createPersonal(user1);
               
               user2 = (User)m_userFactory.create(user2);
               m_roleFactory.createPersonal(user2);
   
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
   
               // create access right that curUser can modify only user password
               accessRightModify = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.LAST_RIGHT_ACTION * 2,
                                             m_iUserDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             AccessRight.NO_RIGHT_IDENTIFIER,
                                             null, null);
               m_rightFactory.create(accessRightModify);          
   
               // create access right that curUser can view all user object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iUserDataType,
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
   
            m_transaction.begin();
            try
            {
               // try to change user1 password
               m_userControl.changePassword(user1.getLoginName(), 
                                            "test_password1", strNewPassword);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            // user1 password has to be changed
            testUser = (User)m_userControl.get(user1.getId());
            assertNotNull("User 1 should not be null", testUser);
            strNewEncryptedPassword = User.encryptPassword(strNewPassword);
            assertEquals("User 1 should have updated password but hasn't", 
                         strNewEncryptedPassword, testUser.getPassword());
   
            //------------------------
            // 2. try to change password for user2
            //------------------------
            m_transaction.begin();
            try
            {
               // try to change user1 password
               m_userControl.changePassword(user2.getLoginName(), 
                                            "test_password2", strNewPassword);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            // user2 password has to be changed
            testUser = (User)m_userControl.get(user2.getId());
            assertNotNull("User 2 should not be null", testUser);
            strNewEncryptedPassword = User.encryptPassword(strNewPassword);
            assertEquals("User 2 should have updated password but hasn't", 
                         strNewEncryptedPassword, testUser.getPassword());
         }
         finally
         {
            deleteTestData(userRole, user1, user2);
         }
      }
   
      /**
       * Test for changing password of the user. User have access granted to specific
       * data object (identified by identifier) of given data type.
       * 
       * @throws Exception - an error has occurred
       */
      public void testChangePasswordCheckId(
      ) throws Exception 
      {
         User        curUser      = null;
         User        user1        = null;
         User        user2        = null;
         User        testUser     = null;
         Role        userRole     = null;
         Object[]    arrTestUsers = null;
         
         String strNewEncryptedPassword = null;
         String strNewPassword = "test_new_password";
         
         AccessRight accessRightModify  = null;
         AccessRight accessRightView  = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_transaction.begin();
            try
            {
               m_roleFactory.createPersonal(curUser);

               // create 2 test users
               arrTestUsers = constructTestUsers();
               user1 = (User)arrTestUsers[0];
               user2 = (User)arrTestUsers[1];
            
               user1 = (User)m_userFactory.create(user1);
               m_roleFactory.createPersonal(user1);
               
               user2 = (User)m_userFactory.create(user2);
               m_roleFactory.createPersonal(user2);
   
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
   
               // create access right that curUser can modify only user1 password
               accessRightModify = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.LAST_RIGHT_ACTION * 2,
                                             m_iUserDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             user1.getId(),
                                             null, null);
               m_rightFactory.create(accessRightModify);          
   
               // create access right that curUser can view all user object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iUserDataType,
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
   
            m_transaction.begin();
            try
            {
               // try to change user1 password
               m_userControl.changePassword(user1.getLoginName(), 
                                            "test_password1", strNewPassword);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            // user1 password has to be changed
            testUser = (User)m_userControl.get(user1.getId());
            assertNotNull("User 1 should not be null", testUser);
            strNewEncryptedPassword = User.encryptPassword(strNewPassword);
            assertEquals("User 1 should have updated password but hasn't", 
                         strNewEncryptedPassword, testUser.getPassword());
   
            //------------------------
            // 2. try to change password for user2
            //------------------------
            m_transaction.begin();
            try
            {
               // try to change user1 password
               m_userControl.changePassword(user2.getLoginName(), 
                                            "test_password2", strNewPassword);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
            }
            // user2 password has to be changed also (inspite of there are not
            // acces rights for user modify) - but we are NOT CHECK ACCESS RIGHTS
            // since each user has right to change it's own password and change 
            // password can be done without user being logged in 
            testUser = (User)m_userControl.get(user2.getId());
            assertNotNull("User 2 should not be null", testUser);
            assertEquals("User 2 password should be changed inspite of access right " +
                         "limitation", strNewEncryptedPassword, testUser.getPassword());
         }
         finally
         {
            deleteTestData(userRole, user1, user2);
         }
      }
   
      /**
       * Test for changing password of the user. User have access granted to a group 
       * of objects based on categories the objects belong to.
       * 
       *  @throws Exception - an error has occurred
       */
      public void testChangePasswordCheckCategorySuperUser(
      ) throws Exception 
      {
         User        curUser      = null;
         User        user1        = null;
         User        user2        = null;
         User        testUser     = null;
         Role        userRole     = null;
         Object[]    arrTestUsers = null;
         
         String strNewEncryptedPassword = null;
         String strNewPassword = "test_new_password";
         
         AccessRight accessRightModify  = null;
         AccessRight accessRightView  = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_transaction.begin();
            try
            {
               m_roleFactory.createPersonal(curUser);

               // create 2 test users
               arrTestUsers = constructTestUsers();
               user1 = (User)arrTestUsers[0];
               user2 = (User)arrTestUsers[1];
            
               user1 = (User)m_userFactory.create(user1);
               m_roleFactory.createPersonal(user1);
               
               user2 = (User)m_userFactory.create(user2);
               m_roleFactory.createPersonal(user2);
   
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
   
               // create access right that curUser can modify only 
               // password for user that is not superuser
               accessRightModify = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.LAST_RIGHT_ACTION * 2,
                                             m_iUserDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             UserDataDescriptor.COL_USER_SUPER_USER,
                                             DataCondition.DATA_CODE_FLAG_NO,
                                             null, null);
               m_rightFactory.create(accessRightModify);          
   
               // create access right that curUser can view all user object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iUserDataType,
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
   
            m_transaction.begin();
            try
            {
               // try to change user1 password
               m_userControl.changePassword(user1.getLoginName(), 
                                            "test_password1", strNewPassword);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            // user1 password has to be changed
            testUser = (User)m_userControl.get(user1.getId());
            assertNotNull("User 1 should not be null", testUser);
            strNewEncryptedPassword = User.encryptPassword(strNewPassword);
            assertEquals("User 1 should have updated password but hasn't", 
                         strNewEncryptedPassword, testUser.getPassword());
   
            //------------------------
            // 2. try to change password for user2
            //------------------------
            m_transaction.begin();
            try
            {
               // try to change user1 password
               m_userControl.changePassword(user2.getLoginName(), 
                                            "test_password2", strNewPassword);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
            }
            // user2 password has to be changed also (inspite of there are not
            // acces rights for user modify) - but we are NOT CHECK ACCESS RIGHTS
            // since each user has right to change it's own password and change 
            // password can be done without user being logged in 
            testUser = (User)m_userControl.get(user2.getId());
            assertNotNull("User 2 should not be null", testUser);
            assertEquals("User 2 password should not be changed because of access right " +
                         "limitation", strNewEncryptedPassword, testUser.getPassword());
         }
         finally
         {
            deleteTestData(userRole, user1, user2);
         }
      }
   
      /**
       * Test for changing login name and email of the user. User have action granted for 
       * all data objects of given type (no id and no categories specified when granting access).
       * 
       *  @throws Exception - an error has occurred
       */
      public void testChangeLoginNameAndEmailCheck(
      ) throws Exception 
      {
         User        curUser      = null;
         User        user1        = null;
         User        user2        = null;
         User        testUser     = null;
         Role        userRole     = null;
         Object[]    arrTestUsers = null;
         
         String strNewLoginName = "test_new_login_name";
         String strNewEmail = "test_new_email";
         
         AccessRight accessRightModify  = null;
         AccessRight accessRightView  = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_transaction.begin();
            try
            {
               m_roleFactory.createPersonal(curUser);

               // create 2 test users
               arrTestUsers = constructTestUsers();
               user1 = (User)arrTestUsers[0];
               user2 = (User)arrTestUsers[1];
            
               user1 = (User)m_userFactory.create(user1);
               m_roleFactory.createPersonal(user1);
               
               user2 = (User)m_userFactory.create(user2);
               m_roleFactory.createPersonal(user2);
   
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
   
               // create access right that curUser can modify all users login names and emails
               accessRightModify = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_MODIFY,
                                             m_iUserDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             AccessRight.NO_RIGHT_IDENTIFIER,
                                             null, null);
               m_rightFactory.create(accessRightModify);          
   
               // create access right that curUser can view all user object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iUserDataType,
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
            // 1. try to change login name and email for user1
            //------------------------
            m_transaction.begin();
            try
            {
               // try to change user1 login name and email
               m_userControl.changeLoginNameAndEmail(user1.getId(), 
                   user1.getModificationTimestamp(), strNewLoginName + "1", strNewEmail + "1");
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            // user1 login name and email has to be changed
            testUser = (User)m_userControl.get(user1.getId());
            assertNotNull("User 1 should not be null", testUser);
            assertEquals("User 1 should have updated login name but hasn't", 
                         strNewLoginName + "1", testUser.getLoginName());
            assertEquals("User 1 should have updated email but hasn't", 
                         strNewEmail + "1", testUser.getEmail());
   
            //------------------------
            // 2. try to change login name and email for user2
            //------------------------
            m_transaction.begin();
            try
            {
               // try to change user2 login name and email
               m_userControl.changeLoginNameAndEmail(user2.getId(), 
                   user2.getModificationTimestamp(), strNewLoginName + "2", strNewEmail + "2");
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            // user2 login name and email has to be changed
            testUser = (User)m_userControl.get(user2.getId());
            assertNotNull("User 2 should not be null", testUser);
            assertEquals("User 2 should have updated login name but hasn't", 
                         strNewLoginName + "2", testUser.getLoginName());
            assertEquals("User 2 should have updated email but hasn't", 
                         strNewEmail + "2", testUser.getEmail());
         }
         finally
         {
            deleteTestData(userRole, user1, user2);
         }
      }
   
      /**
       * Test for changing login name and email of the user. User have access granted 
       * to specific data object (identified by identifier) of given data type.
       * 
       *  @throws Exception - an error has occurred
       */
      public void testChangeLoginNameAndEmailCheckId(
      ) throws Exception 
      {
         User        curUser      = null;
         User        user1        = null;
         User        user2        = null;
         User        testUser     = null;
         Role        userRole     = null;
         Object[]    arrTestUsers = null;
         
         String strNewLoginName = "test_new_login_name";
         String strNewEmail = "test_new_email";
         
         AccessRight accessRightModify  = null;
         AccessRight accessRightView  = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_transaction.begin();
            try
            {
               m_roleFactory.createPersonal(curUser);

               // create 2 test users
               arrTestUsers = constructTestUsers();
               user1 = (User)arrTestUsers[0];
               user2 = (User)arrTestUsers[1];
            
               user1 = (User)m_userFactory.create(user1);
               m_roleFactory.createPersonal(user1);
               
               user2 = (User)m_userFactory.create(user2);
               m_roleFactory.createPersonal(user2);
   
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
   
               // create access right that curUser can modify only user1 login name and email
               accessRightModify = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_MODIFY,
                                             m_iUserDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             user1.getId(),
                                             null, null);
               m_rightFactory.create(accessRightModify);          
   
               // create access right that curUser can view all user object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iUserDataType,
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
            // 1. try to change login name and email for user1
            //------------------------
            m_transaction.begin();
            try
            {
               // try to change user1 login name and email
               m_userControl.changeLoginNameAndEmail(user1.getId(), 
                   user1.getModificationTimestamp(), strNewLoginName + "1", strNewEmail + "1");
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            // user1 login name and email has to be changed
            testUser = (User)m_userControl.get(user1.getId());
            assertNotNull("User 1 should not be null", testUser);
            assertEquals("User 1 should have updated login name but hasn't", 
                         strNewLoginName + "1", testUser.getLoginName());
            assertEquals("User 1 should have updated email but hasn't", 
                         strNewEmail + "1", testUser.getEmail());
   
            //------------------------
            // 2. try to change login name and email for user2
            //------------------------
            m_transaction.begin();
            try
            {
               // try to change user2 login name and email
               m_userControl.changeLoginNameAndEmail(user2.getId(), 
                   user2.getModificationTimestamp(), strNewLoginName + "2", strNewEmail + "2");
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            // user2 login name and email has to be changed
            testUser = (User)m_userControl.get(user2.getId());
            assertNotNull("User 2 should not be null", testUser);
            assertEquals("User 2 should have not updated login name because of access right" +
                         "limitation", user2.getLoginName(), testUser.getLoginName());
            assertEquals("User 2 should have not updated email because of access right" +
                         "limitation", user2.getEmail(), testUser.getEmail());
         }
         finally
         {
            deleteTestData(userRole, user1, user2);
         }
      }
   
      /**
       * Test for changing login name and email of the user. User have access granted 
       * to a group of objects based on categories the objects belong to.
       * 
       *  @throws Exception - an error has occurred
       */
      public void testChangeLoginNameAndEmailCheckCategorySuperUser(
      ) throws Exception 
      {
         User        curUser      = null;
         User        user1        = null;
         User        user2        = null;
         User        testUser     = null;
         Role        userRole     = null;
         Object[]    arrTestUsers = null;
         
         String strNewLoginName = "test_new_login_name";
         String strNewEmail = "test_new_email";
         
         AccessRight accessRightModify  = null;
         AccessRight accessRightView  = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_transaction.begin();
            try
            {
               m_roleFactory.createPersonal(curUser);

               // create 2 test users
               arrTestUsers = constructTestUsers();
               user1 = (User)arrTestUsers[0];
               user2 = (User)arrTestUsers[1];
            
               user1 = (User)m_userFactory.create(user1);
               m_roleFactory.createPersonal(user1);
               
               user2 = (User)m_userFactory.create(user2);
               m_roleFactory.createPersonal(user2);
   
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
   
               // create access right that curUser can modify only user1 login name and email
               accessRightModify = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_MODIFY,
                                             m_iUserDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             UserDataDescriptor.COL_USER_SUPER_USER,
                                             DataCondition.DATA_CODE_FLAG_NO,
                                             null, null);
               m_rightFactory.create(accessRightModify);          
   
               // create access right that curUser can view all user object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iUserDataType,
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
            // 1. try to change login name and email for user1
            //------------------------
            m_transaction.begin();
            try
            {
               // try to change user1 login name and email
               m_userControl.changeLoginNameAndEmail(user1.getId(), 
                   user1.getModificationTimestamp(), strNewLoginName + "1", strNewEmail + "1");
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            // user1 login name and email has to be changed
            testUser = (User)m_userControl.get(user1.getId());
            assertNotNull("User 1 should not be null", testUser);
            assertEquals("User 1 should have updated login name but hasn't", 
                         strNewLoginName + "1", testUser.getLoginName());
            assertEquals("User 1 should have updated email but hasn't", 
                         strNewEmail + "1", testUser.getEmail());
   
            //------------------------
            // 2. try to change login name and email for user2
            //------------------------
            m_transaction.begin();
            try
            {
               // try to change user2 login name and email
               m_userControl.changeLoginNameAndEmail(user2.getId(), 
                   user2.getModificationTimestamp(), strNewLoginName + "2", strNewEmail + "2");
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            // user2 login name and email has to be changed
            testUser = (User)m_userControl.get(user2.getId());
            assertNotNull("User 2 should not be null", testUser);
            assertEquals("User 2 should have not updated login name because of access right" +
                         "limitation", user2.getLoginName(), testUser.getLoginName());
            assertEquals("User 2 should have not updated email because of access right" +
                         "limitation", user2.getEmail(), testUser.getEmail());
         }
         finally
         {
            deleteTestData(userRole, user1, user2);
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
                          true, true, false, // super user
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
                          true, true, true, // super user
                          true, null, null, true
                         );
   
         return new Object[] {user1, user2};  
      }
   
      /**
       * Method for deleting test data - it will be used for all tests
       * 
       * @param userRole - user role that will be deleted
       * @param user1 - user 1 that will be deleted
       * @param user2 - user 2 that will be deleted
       * @throws Exception - error occurred during deleting of test data 
       */
      private void deleteTestData(
         Role userRole,
         User user1,
         User user2
      ) throws Exception
      {
         m_transaction.begin();
         try
         {
            StringBuffer sbUserIDs = new StringBuffer();
            if ((userRole != null) && (userRole.getId() != DataObject.NEW_ID))
            {
               m_rightFactory.deleteAllForRole(userRole.getId());
               // construct string of user IDs the belonging roles will be deleted for 
               sbUserIDs.append(userRole.getUserId());
               if ((user1 != null) && (user1.getId() != DataObject.NEW_ID))
               {
                  sbUserIDs.append(",");
                  sbUserIDs.append(user1.getId());
               }
               if ((user2 != null) && (user2.getId() != DataObject.NEW_ID))
               {
                  sbUserIDs.append(",");
                  sbUserIDs.append(user2.getId());
               }
               m_roleFactory.removeFromUsers(sbUserIDs.toString(), false);
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
