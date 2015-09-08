/*
 * Copyright (c) 2004 - 2009 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: UserControllerSecurityTest.java,v 1.22 2009/09/20 05:32:57 bastafidli Exp $
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
import org.opensubsystems.security.data.RoleDataDescriptor;
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
 * There are contained only tests for get methods.
 * 
 * @version $Id: UserControllerSecurityTest.java,v 1.22 2009/09/20 05:32:57 bastafidli Exp $
 * @author Julo Legeny
 * @code.reviewed 1.3 2004/12/17 12:21:36 jlegeny
 * @code.reviewer Miro Halas
 */
public final class UserControllerSecurityTest extends Tests
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private UserControllerSecurityTest(
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
      TestSuite suite = new DatabaseTestSuite("UserControllerSecurityTest");
      suite.addTestSuite(UserControllerSecurityTestInternal.class);
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
   public static class UserControllerSecurityTestInternal extends SecureListControllerTest
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
   
      /**
       * Data type assigned to role data types.
       */
      protected int m_iRoleDataType;
   
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
      public UserControllerSecurityTestInternal(
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
         
         RoleDataDescriptor roleDescriptor;
         
         roleDescriptor = (RoleDataDescriptor)DataDescriptorManager.getInstance(
                                                 RoleDataDescriptor.class);
         
         m_iRoleDataType = roleDescriptor.getDataType();
      }
   
      // Tests ////////////////////////////////////////////////////////////////////
   
      /**
       * Test for getting of the user. User have action granted for all data objects 
       * of given type (no id and no categories specified when granting access).
       * 
       *  @throws Exception - an error has occurred
       */
      public void testGetCheck(
      ) throws Exception 
      {
         User        curUser      = null;
         User        user1        = null;
         User        user2        = null;
         User        testUser     = null;
         Role        userRole     = null;
         Object[]    arrTestUsers = null;
         
         AccessRight accessRightView   = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_transaction.begin();
            try
            {
               m_roleFactory.createPersonal(curUser);
               
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
            // 1. try to get user1
            //------------------------
            // user has to be retrieved 
            testUser = (User)m_userControl.get(user1.getId());
            assertNotNull("User 1 should not be null", testUser);
            assertEquals("User 1 should be the same but wasn't", user1, testUser);
            
            //------------------------
            // 2. try to get user2
            //------------------------
            // user has to be retrieved
            testUser = (User)m_userControl.get(user2.getId());
            assertNotNull("User 2 should not be null", testUser);
            assertEquals("User 2 should be the same but wasn't", user2, testUser);
         }
         finally
         {
            deleteTestData(userRole, user1, user2);
         }
      }
   
      /**
       * Test for getting of the user. User have access granted to specific
       * data object (identified by identifier) of given data type.
       * 
       *  @throws Exception - an error has occurred
       */
      public void testGetCheckId(
      ) throws Exception 
      {
         User        curUser      = null;
         User        user1        = null;
         User        user2        = null;
         User        testUser     = null;
         Role        userRole     = null;
         Object[]    arrTestUsers = null;
         
         AccessRight accessRightView   = null;
         
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
   
               // create access right that curUser can view anly 'user2' object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iUserDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             user2.getId(),
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
            // 1. try to get user1
            //------------------------
            // user has not be retrieved 
            testUser = (User)m_userControl.get(user1.getId());
            assertNull("User 1 should not be retrieved because of access rights limitation", 
                       testUser);
            
            //------------------------
            // 2. try to get user2
            //------------------------
            // user has to be retrieved
            testUser = (User)m_userControl.get(user2.getId());
            assertNotNull("User 2 should not be null", testUser);
            assertEquals("User 2 should be the same but wasn't", user2, testUser);
         }
         finally
         {
            deleteTestData(userRole, user1, user2);
         }
      }
   
      /**
       * Test for getting of the user. User have access granted to a group 
       * of objects based on categories the objects belong to.
       * 
       *  @throws Exception - an error has occurred
       */
      public void testGetCheckCategorySuperUser(
      ) throws Exception 
      {
         User        curUser      = null;
         User        user1        = null;
         User        user2        = null;
         User        testUser     = null;
         Role        userRole     = null;
         Object[]    arrTestUsers = null;
         
         AccessRight accessRightView   = null;
         
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
   
               // create access right that curUser can view only users that are not super users
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iUserDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             UserDataDescriptor.COL_USER_SUPER_USER,
                                             DataCondition.DATA_CODE_FLAG_NO,
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
            // 1. try to get user1
            //------------------------
            // user has not be retrieved 
            testUser = (User)m_userControl.get(user1.getId());
            assertEquals("User 1 should be retrieved but wasn't", user1, testUser);
   
            //------------------------
            // 2. try to get user2
            //------------------------
            // user has to be retrieved
            testUser = (User)m_userControl.get(user2.getId());
            assertNull("User 2 should not be retrieved because of access rights limitation", 
                       testUser);
         }
         finally
         {
            deleteTestData(userRole, user1, user2);
         }
      }
   
      /**
       * Test for getting of the user with roles. User have action granted for all data objects 
       * of given type (no id and no categories specified when granting access).
       * 
       *  @throws Exception - an error has occurred
       */
      public void testGetUserWithRolesCheck(
      ) throws Exception 
      {
         User        curUser      = null;
         User        user1        = null;
         User        user2        = null;
         User        testUser     = null;
         List        lstRoles     = null;
         Role        userRole     = null;
         Object[]    arrObject    = null;
         Object[]    arrTestUsers = null;
   
         AccessRight accessRightView   = null;
         
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
               m_roleFactory.assignToUser(user1.getId(),
                                          Integer.toString(userRole.getId()));
   
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
   
               // create access right that curUser can view all role object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iRoleDataType,
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
            // 1. try to get user1
            //------------------------
            // user and his belonging roles have to be retrieved 
            arrObject = m_userControl.getUserWithRoles(user1.getId());
            testUser = (User)arrObject[0];
            assertNotNull("User 1 should not be null", testUser);
            assertEquals("User 1 should be the same but wasn't", user1, testUser);
            lstRoles = (ArrayList)arrObject[1];
            assertNotNull("List of User 1 roles should not be null", lstRoles);
            assertEquals("List of User 1 roles has incorrect size", 2, lstRoles.size());
            
            //------------------------
            // 2. try to get user2
            //------------------------
            // user and his belonging roles have to be retrieved
            arrObject = m_userControl.getUserWithRoles(user2.getId());
            testUser = (User)arrObject[0];
            assertNotNull("User 2 should not be null", testUser);
            assertEquals("User 2 should be the same but wasn't", user2, testUser);
            lstRoles = (ArrayList)arrObject[1];
            assertNotNull("List of User 2 roles should not be null", lstRoles);
            assertEquals("List of User 2 roles has incorrect size", 1, lstRoles.size());
         }
         finally
         {
            deleteTestData(userRole, user1, user2);
         }
      }
   
      /**
       * Test for getting of the user with roles. User have access granted to specific
       * data object (identified by identifier) of given data type.
       * 
       *  @throws Exception - an error has occurred
       */
      public void testGetUserWithRolesCheckId(
      ) throws Exception 
      {
         User        curUser      = null;
         User        user1        = null;
         User        user2        = null;
         User        testUser     = null;
         List        lstRoles     = null;
         Role        userRole     = null;
         Object[]    arrObject    = null;
         Object[]    arrTestUsers = null;
         
         AccessRight accessRightView   = null;
         
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
               m_roleFactory.assignToUser(user1.getId(),
                                          Integer.toString(userRole.getId()));
   
               // create access right that curUser can view only 'user1' object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iUserDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             user1.getId(),
                                             null, null);
               m_rightFactory.create(accessRightView);          
   
               // create access right that curUser can view only 'userRole' object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iRoleDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             userRole.getId(),
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
            // 1. try to get user1
            //------------------------
            // user and his belonging roles have to be retrieved 
            arrObject = m_userControl.getUserWithRoles(user1.getId());
            testUser = (User)arrObject[0];
            assertNotNull("User 1 should not be null", testUser);
            assertEquals("User 1 should be the same but wasn't", user1, testUser);
            lstRoles = (ArrayList)arrObject[1];
            assertNotNull("List of User 1 roles should not be null", lstRoles);
            assertEquals("List of User 1 roles has incorrect size", 1, lstRoles.size());
            
            //------------------------
            // 2. try to get user2
            //------------------------
            // user and his belonging roles have not be retrieved
            arrObject = m_userControl.getUserWithRoles(user2.getId());
            assertNull("User 2 should not be retrieved because of access rights limitation",
                       arrObject[0]);
            assertNull("List of User 2 roles should not be retrieved because of " +
                       "access rights limitation", arrObject[1]);
         }
         finally
         {
            deleteTestData(userRole, user1, user2);
         }
      }
   
      /**
       * Test for getting of the user with roles. User have access granted to a group 
       * of objects based on categories the objects belong to.
       * 
       *  @throws Exception - an error has occurred
       */
      public void testGetUserWithRolesCheckCategorySuperUser(
      ) throws Exception 
      {
         User        curUser      = null;
         User        user1        = null;
         User        user2        = null;
         User        testUser     = null;
         List        lstRoles     = null;
         Role        userRole     = null;
         Object[]    arrObject    = null;
         Object[]    arrTestUsers = null;
         
         AccessRight accessRightView   = null;
         
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
               m_roleFactory.assignToUser(user1.getId(),
                                          Integer.toString(userRole.getId()));
   
               // create access right that curUser can view only user1 object, that is not superuser
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iUserDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             UserDataDescriptor.COL_USER_SUPER_USER,
                                             DataCondition.DATA_CODE_FLAG_NO,
                                             null, null);
               m_rightFactory.create(accessRightView);          
   
               // create access right that curUser can view all role objects
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iRoleDataType,
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
            // 1. try to get user1
            //------------------------
            // user and his belonging roles have to be retrieved 
            arrObject = m_userControl.getUserWithRoles(user1.getId());
            testUser = (User)arrObject[0];
            assertNotNull("User 1 should not be null", testUser);
            assertEquals("User 1 should be the same but wasn't", user1, testUser);
            lstRoles = (ArrayList)arrObject[1];
            assertNotNull("List of User 1 roles should not be null", lstRoles);
            assertEquals("List of User 1 roles has incorrect size", 2, lstRoles.size());
            
            //------------------------
            // 2. try to get user2
            //------------------------
            // user and his belonging roles have not be retrieved
            arrObject = m_userControl.getUserWithRoles(user2.getId());
            assertNull("User 2 should not be retrieved because of access rights limitation",
                       arrObject[0]);
            assertNull("List of User 2 roles should not be retrieved because of " +
                       "access rights limitation", arrObject[1]);
         }
         finally
         {
            deleteTestData(userRole, user1, user2);
         }
      }
   
      /**
       * Test for getting of the user roles. User have action granted for all data objects 
       * of given type (no id and no categories specified when granting access).
       * 
       *  @throws Exception - an error has occurred
       */
      public void testGetRolesCheck(
      ) throws Exception 
      {
         User     curUser   = null;
         User     user1     = null;
         User     user2     = null;
         List     lstRoles  = null;
         Role     userRole  = null;
         Object[] arrTestUsers = null;
         
         AccessRight accessRightView   = null;
         
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
               m_roleFactory.assignToUser(user1.getId(),
                                          Integer.toString(userRole.getId()));
   
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
   
               // create access right that curUser can view all role object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iRoleDataType,
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
   
            //--------------------------
            // 1. try to get user1 roles
            //--------------------------
            // user roles have to be retrieved 
            lstRoles = m_userControl.getRoles(user1.getId());
            assertNotNull("List of User 1 roles should not be null", lstRoles);
            assertEquals("List of User 1 roles has incorrect size", 2, lstRoles.size());
            
            //--------------------------
            // 2. try to get user2 roles
            //--------------------------
            // user roles have to be retrieved
            lstRoles = m_userControl.getRoles(user2.getId());
            assertNotNull("List of User 2 roles should not be null", lstRoles);
            assertEquals("List of User 2 roles has incorrect size", 1, lstRoles.size());
         }
         finally
         {
            deleteTestData(userRole, user1, user2);
         }
      }
   
      /**
       * Test for getting of the user roles. User have access granted to specific
       * data object (identified by identifier) of given data type.
       * 
       *  @throws Exception - an error has occurred
       */
      public void testGetRolesCheckId(
      ) throws Exception 
      {
         User     curUser   = null;
         User     user1     = null;
         User     user2     = null;
         List     lstRoles  = null;
         Role     userRole  = null;
         Object[] arrTestUsers = null;
         
         AccessRight accessRightView   = null;
         
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
               m_roleFactory.assignToUser(user1.getId(),
                                          Integer.toString(userRole.getId()));
   
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
   
               // create access right that curUser can view only role object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iRoleDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             userRole.getId(),
                                             null, null);
               m_rightFactory.create(accessRightView);          
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            //--------------------------
            // 1. try to get user1 roles
            //--------------------------
            // user roles have to be retrieved 
            lstRoles = m_userControl.getRoles(user1.getId());
            assertNotNull("List of User 1 roles should not be null", lstRoles);
            assertEquals("List of User 1 roles has incorrect size", 1, lstRoles.size());
            assertEquals("Retrieved role is incorrect", userRole, (Role)lstRoles.get(0));
            
            //--------------------------
            // 2. try to get user2 roles
            //--------------------------
            // user roles have not to be retrieved
            lstRoles = null;
            lstRoles = m_userControl.getRoles(user2.getId());
            assertNull("List of User 2 roles should not be retrieved " +
                       "because of access right limitations", lstRoles);
         }
         finally
         {
            deleteTestData(userRole, user1, user2);
         }
      }
   
      /**
       * Test for getting of the user roles. User have access granted to a group 
       * of objects based on categories the objects belong to.
       * 
       *  @throws Exception - an error has occurred
       */
      public void testGetRolesCheckCategorySuperUser(
      ) throws Exception 
      {
         User     curUser   = null;
         User     user1     = null;
         User     user2     = null;
         List     lstRoles  = null;
         Role     userRole  = null;
         Object[] arrTestUsers = null;
         
         AccessRight accessRightView   = null;
         
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
               m_roleFactory.assignToUser(user1.getId(),
                                          Integer.toString(userRole.getId()));
   
               // create access right that curUser can view only super user object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iUserDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             UserDataDescriptor.COL_USER_SUPER_USER,
                                             DataCondition.DATA_CODE_FLAG_NO,
                                             null, null);
               m_rightFactory.create(accessRightView);          
   
               // create access right that curUser can view only user role object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iRoleDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             userRole.getId(),
                                             null, null);
               m_rightFactory.create(accessRightView);          
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            //--------------------------
            // 1. try to get user1 roles
            //--------------------------
            // user roles have to be retrieved 
            lstRoles = m_userControl.getRoles(user1.getId());
            assertNotNull("List of User 1 roles should not be null", lstRoles);
            assertEquals("List of User 1 roles has incorrect size", 1, lstRoles.size());
            assertEquals("Retrieved role is incorrect", userRole, (Role)lstRoles.get(0));
   
            //--------------------------
            // 2. try to get user2 roles
            //--------------------------
            // user roles have not to be retrieved
            lstRoles = m_userControl.getRoles(user2.getId());
            assertNull("List of User 2 roles should not be retrieved " +
                       "because of access right limitations", lstRoles);
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
