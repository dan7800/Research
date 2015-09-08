/*
 * Copyright (c) 2004 - 2009 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: RoleControllerSecurityTest.java,v 1.14 2009/09/20 05:32:57 bastafidli Exp $
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

/**
 * Test for security facade above the persistence layer. Each method of role
 * controller check if user was granted access rights required to perform any
 * given operation. We do not test, if the security checks are performed correctly 
 * and if they work at all. 
 * 
 * @version $Id: RoleControllerSecurityTest.java,v 1.14 2009/09/20 05:32:57 bastafidli Exp $
 * @author Julo Legeny
 * @code.reviewer
 * @code.reviewed TODO: Review this code
 */
public final class RoleControllerSecurityTest extends Tests
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private RoleControllerSecurityTest(
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
      TestSuite suite = new DatabaseTestSuite("RoleControllerSecurityTest");
      suite.addTestSuite(RoleControllerSecurityTestInternal.class);
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
   public static class RoleControllerSecurityTestInternal extends SecureListControllerTest
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
       * Controller used to manipulate roles.
       */
      protected RoleController m_roleControl;
   
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
      public RoleControllerSecurityTestInternal(
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
         m_roleControl = (RoleController)ControllerManager.getInstance(
                                         RoleController.class);
         
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
       * Test for getting of the role. User have action granted for all data objects 
       * of given type (no id and no categories specified when granting access).
       * 
       *  @throws Exception - an error has occurred
       */
      public void testGetCheck(
      ) throws Exception 
      {
         User     curUser      = null;
         Role     role1        = null;
         Role     role2        = null;
         Role     testRole     = null;
         Role     userRole     = null;
         Object[] arrTestRoles = null;
         
         AccessRight accessRightView   = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_roleFactory.createPersonal(curUser);
   
            m_transaction.begin();
            try
            {
               arrTestRoles = constructTestRoles();
               role1 = (Role)arrTestRoles[0];
               role2 = (Role)arrTestRoles[1];
            
               role1 = (Role)m_roleFactory.create(role1);
               role2 = (Role)m_roleFactory.create(role2);
   
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
            // 1. try to get role1
            //------------------------
            // role1 has to be retrieved 
            testRole = (Role)m_roleControl.get(role1.getId());
            assertNotNull("Role 1 should not be null", testRole);
            assertEquals("Retrieved role 1 is incorrect", role1, testRole);
            
            //------------------------
            // 2. try to get role2
            //------------------------
            // role2 has to be retrieved 
            testRole = (Role)m_roleControl.get(role2.getId());
            assertNotNull("Role 2 should not be null", testRole);
            assertEquals("Retrieved role 2 is incorrect", role2, testRole);
         }
         finally
         {
            deleteTestData(userRole, role1, role2, null, null);
         }
      }
   
      /**
       * Test for getting of the role. User have access granted to specific
       * data object (identified by identifier) of given data type.
       * 
       *  @throws Exception - an error has occurred
       */
      public void testGetCheckId(
      ) throws Exception 
      {
         User     curUser      = null;
         Role     role1        = null;
         Role     role2        = null;
         Role     testRole     = null;
         Role     userRole     = null;
         Object[] arrTestRoles = null;
         
         AccessRight accessRightView   = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_roleFactory.createPersonal(curUser);
   
            m_transaction.begin();
            try
            {
               arrTestRoles = constructTestRoles();
               role1 = (Role)arrTestRoles[0];
               role2 = (Role)arrTestRoles[1];
            
               role1 = (Role)m_roleFactory.create(role1);
               role2 = (Role)m_roleFactory.create(role2);
   
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
   
               // create access right that curUser can view only role1 object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iRoleDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             role1.getId(),
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
            // 1. try to get role1
            //------------------------
            // role1 has to be retrieved 
            testRole = (Role)m_roleControl.get(role1.getId());
            assertNotNull("Role 1 should not be null", testRole);
            assertEquals("Retrieved role 1 is incorrect", role1, testRole);
            
            //------------------------
            // 2. try to get role2
            //------------------------
            // role2 has to be retrieved 
            testRole = (Role)m_roleControl.get(role2.getId());
            assertNull("Role 2 should not be retrieved " +
                       "because of access right limitation", testRole);
         }
         finally
         {
            deleteTestData(userRole, role1, role2, null, null);
         }
      }
   
      /**
       * Test for getting of the role. User have access granted to a group 
       * of objects based on categories the objects belong to.
       * 
       *  @throws Exception - an error has occurred
       */
      public void testGetCheckCategoryEnabled(
      ) throws Exception 
      {
         User     curUser      = null;
         Role     role1        = null;
         Role     role2        = null;
         Role     testRole     = null;
         Role     userRole     = null;
         Object[] arrTestRoles = null;
         
         AccessRight accessRightView   = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_roleFactory.createPersonal(curUser);
   
            m_transaction.begin();
            try
            {
               arrTestRoles = constructTestRoles();
               role1 = (Role)arrTestRoles[0];
               role2 = (Role)arrTestRoles[1];
            
               role1 = (Role)m_roleFactory.create(role1);
               role2 = (Role)m_roleFactory.create(role2);
   
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
   
               // create access right that curUser can view only role1 object that is not enabled
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iRoleDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             RoleDataDescriptor.COL_ROLE_ENABLED,
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
            // 1. try to get role1
            //------------------------
            // role1 has to be retrieved 
            testRole = (Role)m_roleControl.get(role1.getId());
            assertNotNull("Role 1 should not be null", testRole);
            assertEquals("Retrieved role 1 is incorrect", role1, testRole);
            
            //------------------------
            // 2. try to get role2
            //------------------------
            // role2 has to be retrieved 
            testRole = (Role)m_roleControl.get(role2.getId());
            assertNull("Role 2 should not be retrieved " +
                       "because of access right limitation", testRole);
         }
         finally
         {
            deleteTestData(userRole, role1, role2, null, null);
         }
      }
   
      /**
       * Test for getting of the list roles. User have action granted for all data objects 
       * of given type (no id and no categories specified when granting access).
       * 
       *  @throws Exception - an error has occurred
       */
      public void testGetListRolesCheck(
      ) throws Exception 
      {
         User     curUser      = null;
         Role     role1        = null;
         Role     role2        = null;
         Role     userRole     = null;
         Object[] arrTestRoles = null;
         List     lstRoles     = null;
         String   strIDs       = null;
         
         AccessRight accessRightView   = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_roleFactory.createPersonal(curUser);
   
            m_transaction.begin();
            try
            {
               arrTestRoles = constructTestRoles();
               role1 = (Role)arrTestRoles[0];
               role2 = (Role)arrTestRoles[1];
            
               role1 = (Role)m_roleFactory.create(role1);
               role2 = (Role)m_roleFactory.create(role2);
   
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
            // 1. try to get all roles
            //------------------------
            strIDs = role1.getId() + "," + role2.getId();
            // roles has to be retrieved 
            lstRoles = m_roleControl.get(strIDs);
            assertNotNull("List of roles should not be null", lstRoles);
            assertEquals("List of roles has incorrect size", 2, lstRoles.size());
            
         }
         finally
         {
            deleteTestData(userRole, role1, role2, null, null);
         }
      }
   
      /**
       * Test for getting of the list roles. User have access granted to specific
       * data object (identified by identifier) of given data type.
       * 
       * @throws Exception - an error has occurred
       */
      public void testGetListRolesCheckId(
      ) throws Exception 
      {
         User     curUser      = null;
         Role     role1        = null;
         Role     role2        = null;
         Role     userRole     = null;
         Object[] arrTestRoles = null;
         List     lstRoles     = null;
         String   strIDs       = null;
         
         AccessRight accessRightView   = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_roleFactory.createPersonal(curUser);
   
            m_transaction.begin();
            try
            {
               arrTestRoles = constructTestRoles();
               role1 = (Role)arrTestRoles[0];
               role2 = (Role)arrTestRoles[1];
            
               role1 = (Role)m_roleFactory.create(role1);
               role2 = (Role)m_roleFactory.create(role2);
   
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
   
               // create access right that curUser can view only role1 object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iRoleDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             role1.getId(),
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
            // 1. try to get all roles
            //------------------------
            strIDs = role1.getId() + "," + role2.getId();
            // roles has to be retrieved 
            lstRoles = m_roleControl.get(strIDs);
            assertNotNull("List of roles should not be null", lstRoles);
            assertEquals("List of roles has incorrect size", 1, lstRoles.size());
            assertEquals("Retrieved role is incorrect", role1, lstRoles.get(0));
         }
         finally
         {
            deleteTestData(userRole, role1, role2, null, null);
         }
      }
   
      /**
       * Test for getting of the list roles. User have access granted to a group 
       * of objects based on categories the objects belong to.
       * 
       *  @throws Exception - an error has occurred
       */
      public void testGetListRolesCheckCategoryEnabled(
      ) throws Exception 
      {
         User     curUser      = null;
         Role     role1        = null;
         Role     role2        = null;
         Role     userRole     = null;
         Object[] arrTestRoles = null;
         List     lstRoles     = null;
         String   strIDs       = null;
         
         AccessRight accessRightView   = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_roleFactory.createPersonal(curUser);
   
            m_transaction.begin();
            try
            {
               arrTestRoles = constructTestRoles();
               role1 = (Role)arrTestRoles[0];
               role2 = (Role)arrTestRoles[1];
            
               role1 = (Role)m_roleFactory.create(role1);
               role2 = (Role)m_roleFactory.create(role2);
   
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
   
               // create access right that curUser can view only role object 
               // that is not unmodifiable
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iRoleDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             RoleDataDescriptor.COL_ROLE_ENABLED,
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
            // 1. try to get all roles
            //------------------------
            strIDs = role1.getId() + "," + role2.getId();
            // roles has to be retrieved 
            lstRoles = m_roleControl.get(strIDs);
            assertNotNull("List of roles should not be null", lstRoles);
            assertEquals("List of roles has incorrect size", 1, lstRoles.size());
            assertEquals("Retrieved role is incorrect", role1, lstRoles.get(0));
         }
         finally
         {
            deleteTestData(userRole, role1, role2, null, null);
         }
      }
   
      /**
       * Test for getting of all roles for specified user. User have action granted for all 
       * data objects of given type (no id and no categories specified when granting access).
       * 
       *  @throws Exception - an error has occurred
       */
      public void testGetAllForUserCheck(
      ) throws Exception 
      {
         User        curUser      = null;
         User        user1        = null;
         User        user2        = null;
         Role        role1        = null;
         Role        role2        = null;
         List        lstRoles     = null;
         Role        userRole     = null;
         Object[]    arrTestUsers = null;
         Object[]    arrTestRoles = null;
   
         AccessRight accessRightView   = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_roleFactory.createPersonal(curUser);
   
            m_transaction.begin();
            try
            {
               
               // create 2 test users
               arrTestUsers = constructTestUsers();
               user1 = (User)arrTestUsers[0];
               user2 = (User)arrTestUsers[1];
   
               user1 = (User)m_userFactory.create(user1);
               m_roleFactory.createPersonal(user1);
               
               user2 = (User)m_userFactory.create(user2);
               m_roleFactory.createPersonal(user2);
   
               // create 2 test roles
               arrTestRoles = constructTestRoles();
               role1 = (Role)arrTestRoles[0];
               role2 = (Role)arrTestRoles[1];
            
               role1 = (Role)m_roleFactory.create(role1);
               role2 = (Role)m_roleFactory.create(role2);
               
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
               // assign test roles to the user 1
               m_roleFactory.assignToUser(user1.getId(),
                                          Integer.toString(role1.getId()));
               m_roleFactory.assignToUser(user1.getId(),
                                          Integer.toString(role2.getId()));
   
   
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
            // 1. try to get all roles for user1
            //------------------------
            // list of roles have to be retrieved 
            lstRoles = m_roleControl.getAllForUser(user1.getId());
            assertNotNull("List of roles for user 1 should not be null", lstRoles);
            assertEquals("List of roles for user 1 has incorrect size", 3, lstRoles.size());
            
            //------------------------
            // 2. try to get all roles for user2
            //------------------------
            // list of roles have to be retrieved 
            lstRoles = m_roleControl.getAllForUser(user2.getId());
            assertNotNull("List of roles for user 2 should not be null", lstRoles);
            assertEquals("List of roles for user 2 has incorrect size", 1, lstRoles.size());
         }
         finally
         {
            deleteTestData(userRole, role1, role2, user1, user2);
         }
      }
   
      /**
       * Test for getting of all roles for specified user. User have access granted to specific
       * data object (identified by identifier) of given data type.
       * 
       *  @throws Exception - an error has occurred
       */
      public void testGetAllForUserCheckId(
      ) throws Exception 
      {
         User        curUser      = null;
         User        user1        = null;
         User        user2        = null;
         Role        role1        = null;
         Role        role2        = null;
         List        lstRoles     = null;
         Role        userRole     = null;
         Object[]    arrTestUsers = null;
         Object[]    arrTestRoles = null;
   
         AccessRight accessRightView   = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_roleFactory.createPersonal(curUser);
   
            m_transaction.begin();
            try
            {
               
               // create 2 test users
               arrTestUsers = constructTestUsers();
               user1 = (User)arrTestUsers[0];
               user2 = (User)arrTestUsers[1];
   
               user1 = (User)m_userFactory.create(user1);
               m_roleFactory.createPersonal(user1);
               
               user2 = (User)m_userFactory.create(user2);
               m_roleFactory.createPersonal(user2);
   
               // create 2 test roles
               arrTestRoles = constructTestRoles();
               role1 = (Role)arrTestRoles[0];
               role2 = (Role)arrTestRoles[1];
            
               role1 = (Role)m_roleFactory.create(role1);
               role2 = (Role)m_roleFactory.create(role2);
               
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
               // assign test roles to the user 1
               m_roleFactory.assignToUser(user1.getId(),
                                          Integer.toString(role1.getId()));
               m_roleFactory.assignToUser(user1.getId(),
                                          Integer.toString(role2.getId()));
   
   
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
   
               // create access right that curUser can view only role2 object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iRoleDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             role2.getId(),
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
            // 1. try to get all roles for user1
            //------------------------
            // there have to be retrieved only role1 
            lstRoles = m_roleControl.getAllForUser(user1.getId());
            assertNotNull("List of roles for user 1 should not be null", lstRoles);
            assertEquals("List of roles for user 1 has incorrect size", 1, lstRoles.size());
            assertEquals("Incorrect role retrieved", role2, lstRoles.get(0));
            
            //------------------------
            // 2. try to get all roles for user2
            //------------------------
            // list of roles have not be retrieved 
            lstRoles = m_roleControl.getAllForUser(user2.getId());
            assertTrue("List of roles for user 2 should not be retrieved because of " +
                       "access right limitation", (lstRoles == null || lstRoles.isEmpty()));
         }
         finally
         {
            deleteTestData(userRole, role1, role2, user1, user2);
         }
      }
   
      /**
       * Test for getting of all roles for specified user. User have access granted to a group 
       * of objects based on categories the objects belong to.
       * 
       *  @throws Exception - an error has occurred
       */
      public void testGetAllForUserCheckCategoryEnabled(
      ) throws Exception 
      {
         User        curUser      = null;
         User        user1        = null;
         User        user2        = null;
         Role        role1        = null;
         Role        role2        = null;
         List        lstRoles     = null;
         Role        userRole     = null;
         Object[]    arrTestUsers = null;
         Object[]    arrTestRoles = null;
   
         AccessRight accessRightView   = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_roleFactory.createPersonal(curUser);
   
            m_transaction.begin();
            try
            {
               
               // create 2 test users
               arrTestUsers = constructTestUsers();
               user1 = (User)arrTestUsers[0];
               user2 = (User)arrTestUsers[1];
   
               user1 = (User)m_userFactory.create(user1);
               m_roleFactory.createPersonal(user1);
               
               user2 = (User)m_userFactory.create(user2);
               m_roleFactory.createPersonal(user2);
   
               // create 2 test roles
               arrTestRoles = constructTestRoles();
               role1 = (Role)arrTestRoles[0];
               role2 = (Role)arrTestRoles[1];
            
               role1 = (Role)m_roleFactory.create(role1);
               role2 = (Role)m_roleFactory.create(role2);
               
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
               // assign test roles to the user 1
               m_roleFactory.assignToUser(user1.getId(),
                                          Integer.toString(role1.getId()));
               m_roleFactory.assignToUser(user1.getId(),
                                          Integer.toString(role2.getId()));
               // assign test roles to the user 2
               m_roleFactory.assignToUser(user2.getId(),
                                          Integer.toString(role2.getId()));
   
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
   
               // create access right that curUser can view only role object, that is not enabled
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iRoleDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             RoleDataDescriptor.COL_ROLE_ENABLED,
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
            // 1. try to get all roles for user1
            //------------------------
            // there have to be retrieved only role1 
            lstRoles = m_roleControl.getAllForUser(user1.getId());
            assertNotNull("List of roles for user 1 should not be null", lstRoles);
            assertEquals("List of roles for user 1 has incorrect size", 1, lstRoles.size());
            assertEquals("Incorrect role retrieved", role1, lstRoles.get(0));
            
            //------------------------
            // 2. try to get all roles for user2
            //------------------------
            // list of roles have not be retrieved 
            lstRoles = m_roleControl.getAllForUser(user2.getId());
            assertTrue("List of roles for user 2 should not be retrieved because of " +
                       "access right limitation", (lstRoles == null || lstRoles.isEmpty()));
         }
         finally
         {
            deleteTestData(userRole, role1, role2, user1, user2);
         }
      }
   
      /**
       * Test for saving of the role. User have action granted for all data objects 
       * of given type (no id and no categories specified when granting access).
       * 
       * @throws Exception - an error has occurred
       */
      public void testSaveCheck(
      ) throws Exception 
      {
         User     curUser      = null;
         Role     role1        = null;
         Role     role2        = null;
         Role     role3        = null;
         Role     testRole     = null;
         Role     userRole     = null;
         Object[] arrTestRoles = null;
         
         AccessRight accessRightView   = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_roleFactory.createPersonal(curUser);
   
            m_transaction.begin();
            try
            {
               arrTestRoles = constructTestRoles();
               role1 = (Role)arrTestRoles[0];
               role2 = (Role)arrTestRoles[1];
            
               role1 = (Role)m_roleFactory.create(role1);
               role2 = (Role)m_roleFactory.create(role2);
   
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
               m_roleFactory.assignToUser(curUser.getId(),
                                          Integer.toString(role1.getId()));
               m_roleFactory.assignToUser(curUser.getId(),
                                          Integer.toString(role2.getId()));
   
               // create access right that curUser can modify all role object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_MODIFY,
                                             m_iRoleDataType,
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
            // 1. try to save role1
            //------------------------
            role3 = new Role(role1.getId(), 
                             role1.getDomainId(), 
                             "testname3", 
                             "testdescription3", 
                             role1.isEnabled(), 
                             DataObject.NEW_ID, 
                             false, null, 
                             role1.getCreationTimestamp(),
                             role1.getModificationTimestamp()
                            );
            m_transaction.begin();
            try
            {
               // try to modify role 
               m_roleControl.save(role3);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // role1 has to be updated
            testRole = (Role)m_roleControl.get(role1.getId());
            assertNotNull("Role 1 should not be null", testRole);
            assertEquals("Role 1 should be updated but wasn't", 
                         role3.getName(), testRole.getName());
            assertEquals("Role 1 should be updated but wasn't", 
                         role3.getDescription(), testRole.getDescription());
            
            //------------------------
            // 2. try to save role2
            //------------------------
            role3 = new Role(role2.getId(), 
                             role2.getDomainId(), 
                             "testname4", 
                             "testdescription4", 
                             role2.isEnabled(), 
                             DataObject.NEW_ID, 
                             false, null, 
                             role2.getCreationTimestamp(),
                             role2.getModificationTimestamp()
                            );
            m_transaction.begin();
            try
            {
               // try to modify role 
               m_roleControl.save(role3);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // role2 has to be updated 
            testRole = (Role)m_roleControl.get(role3.getId());
            assertNotNull("Role 2 should not be null", testRole);
            assertEquals("Role 2 should be updated but wasn't", 
                         role3.getName(), testRole.getName());
            assertEquals("Role 2 should be updated but wasn't", 
                         role3.getDescription(), testRole.getDescription());
         }
         finally
         {
            deleteTestData(userRole, role1, role2, null, null);
         }
      }
   
      /**
       * Test for saving of the role. User have access granted to specific
       * data object (identified by identifier) of given data type.
       * 
       * @throws Exception - an error has occurred
       */
      public void testSaveCheckId(
      ) throws Exception 
      {
         User     curUser      = null;
         Role     role1        = null;
         Role     role2        = null;
         Role     role3        = null;
         Role     testRole     = null;
         Role     userRole     = null;
         Object[] arrTestRoles = null;
         
         AccessRight accessRightView   = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_roleFactory.createPersonal(curUser);
   
            m_transaction.begin();
            try
            {
               arrTestRoles = constructTestRoles();
               role1 = (Role)arrTestRoles[0];
               role2 = (Role)arrTestRoles[1];
            
               role1 = (Role)m_roleFactory.create(role1);
               role2 = (Role)m_roleFactory.create(role2);
   
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
               m_roleFactory.assignToUser(curUser.getId(),
                                          Integer.toString(role1.getId()));
               m_roleFactory.assignToUser(curUser.getId(),
                                          Integer.toString(role2.getId()));
   
               // create access right that curUser can modify only role1 object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_MODIFY,
                                             m_iRoleDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             role1.getId(),
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
            // 1. try to save role1
            //------------------------
            role3 = new Role(role1.getId(), 
                             role1.getDomainId(), 
                             "testname3", 
                             "testdescription3", 
                             role1.isEnabled(), 
                             DataObject.NEW_ID, 
                             false, null, 
                             role1.getCreationTimestamp(),
                             role1.getModificationTimestamp()
                            );
            m_transaction.begin();
            try
            {
               // try to modify role 
               m_roleControl.save(role3);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // role1 has to be updated
            testRole = (Role)m_roleControl.get(role1.getId());
            assertNotNull("Role 1 should not be null", testRole);
            assertEquals("Role 1 should be updated but wasn't", 
                         role3.getName(), testRole.getName());
            assertEquals("Role 1 should be updated but wasn't", 
                         role3.getDescription(), testRole.getDescription());
            
            //------------------------
            // 2. try to save role2
            //------------------------
            role3 = new Role(role2.getId(), 
                             role2.getDomainId(), 
                             "testname4", 
                             "testdescription4", 
                             role2.isEnabled(), 
                             DataObject.NEW_ID, 
                             false, null, 
                             role2.getCreationTimestamp(),
                             role2.getModificationTimestamp()
                            );
            m_transaction.begin();
            try
            {
               // try to modify role 
               m_roleControl.save(role3);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // role2 has not be updated 
            testRole = (Role)m_roleControl.get(role3.getId());
            assertNotNull("Role 2 should not be null", testRole);
            assertEquals("Role 2 should not be updated because of access right limitation",
                         role2, testRole);
         }
         finally
         {
            deleteTestData(userRole, role1, role2, null, null);
         }
      }
   
      /**
       * Test for saving of the role. User have access granted to a group 
       * of objects based on categories the objects belong to.
       * 
       * @throws Exception - an error has occurred
       */
      public void testSaveCheckCategoryEnabled(
      ) throws Exception 
      {
         User     curUser      = null;
         Role     role1        = null;
         Role     role2        = null;
         Role     role3        = null;
         Role     testRole     = null;
         Role     userRole     = null;
         Object[] arrTestRoles = null;
         
         AccessRight accessRightView   = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_roleFactory.createPersonal(curUser);
   
            m_transaction.begin();
            try
            {
               arrTestRoles = constructTestRoles();
               role1 = (Role)arrTestRoles[0];
               role2 = (Role)arrTestRoles[1];
            
               role1 = (Role)m_roleFactory.create(role1);
               role2 = (Role)m_roleFactory.create(role2);
   
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
               m_roleFactory.assignToUser(curUser.getId(),
                                          Integer.toString(role1.getId()));
               m_roleFactory.assignToUser(curUser.getId(),
                                          Integer.toString(role2.getId()));
   
               // create access right that curUser can modify only role object thet is not enabled
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_MODIFY,
                                             m_iRoleDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             RoleDataDescriptor.COL_ROLE_ENABLED,
                                             DataCondition.DATA_CODE_FLAG_NO,
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
            // 1. try to save role1
            //------------------------
            role3 = new Role(role1.getId(), 
                             role1.getDomainId(), 
                             "testname3", 
                             "testdescription3", 
                             role1.isEnabled(), 
                             DataObject.NEW_ID, 
                             false, null, 
                             role1.getCreationTimestamp(),
                             role1.getModificationTimestamp()
                            );
            m_transaction.begin();
            try
            {
               // try to modify role 
               m_roleControl.save(role3);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // role1 has to be updated
            testRole = (Role)m_roleControl.get(role1.getId());
            assertNotNull("Role 1 should not be null", testRole);
            assertEquals("Role 1 should be updated but wasn't", 
                         role3.getName(), testRole.getName());
            assertEquals("Role 1 should be updated but wasn't", 
                         role3.getDescription(), testRole.getDescription());
            
            //------------------------
            // 2. try to save role2
            //------------------------
            role3 = new Role(role2.getId(), 
                             role2.getDomainId(), 
                             "testname4", 
                             "testdescription4", 
                             role2.isEnabled(), 
                             DataObject.NEW_ID, 
                             false, null, 
                             role2.getCreationTimestamp(),
                             role2.getModificationTimestamp()
                            );
            m_transaction.begin();
            try
            {
               // try to modify role 
               m_roleControl.save(role3);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // role2 has not be updated 
            testRole = (Role)m_roleControl.get(role3.getId());
            assertNotNull("Role 2 should not be null", testRole);
            assertEquals("Role 2 should not be updated because of access right limitation",
                         role2, testRole);
         }
         finally
         {
            deleteTestData(userRole, role1, role2, null, null);
         }
      }
   
      /**
       * Test for deleting of the roles specified by IDs. User have action granted for all 
       * data objects of given type (no id and no categories specified when granting access).
       * 
       *  @throws Exception - an error has occurred
       */
      public void testDeleteMoreCheck(
      ) throws Exception 
      {
         User     curUser      = null;
         Role     role1        = null;
         Role     role2        = null;
         Role     testRole     = null;
         Role     userRole     = null;
         Object[] arrTestRoles = null;
         int      iDeleted     = 0;
         String   strIDs       = null;       
         
         AccessRight accessRightView   = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_roleFactory.createPersonal(curUser);
   
            m_transaction.begin();
            try
            {
               arrTestRoles = constructTestRoles();
               role1 = (Role)arrTestRoles[0];
               role2 = (Role)arrTestRoles[1];
            
               role1 = (Role)m_roleFactory.create(role1);
               role2 = (Role)m_roleFactory.create(role2);
   
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
               m_roleFactory.assignToUser(curUser.getId(),
                                          Integer.toString(role1.getId()));
               m_roleFactory.assignToUser(curUser.getId(),
                                          Integer.toString(role2.getId()));
   
               // create access right that curUser can delete all role object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_DELETE,
                                             m_iRoleDataType,
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
            // 1. try to delete role1 and role2
            //------------------------
            strIDs = role1.getId() + "," + role2.getId(); 
            m_transaction.begin();
            try
            {
               // try to delete both roles 
               iDeleted = m_roleControl.delete(strIDs);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            
            // role1 has to be deleted
            assertEquals("Number of deleted roles is incorrect", 2, iDeleted);
            testRole = (Role)m_roleControl.get(role1.getId());
            assertNull("Role 1 should be deleted but was not", testRole);
            role1 = null;
            testRole = (Role)m_roleControl.get(role2.getId());
            assertNull("Role 2 should be deleted but was not", testRole);
            role2 = null;
         }
         finally
         {
            deleteTestData(userRole, role1, role2, null, null);
         }
      }
   
      /**
       * Test for deleting of the roles specified by IDs. User have access granted to specific
       * data object (identified by identifier) of given data type.
       * 
       *  @throws Exception - an error has occurred
       */
      public void testDeleteMoreCheckId(
      ) throws Exception 
      {
         User     curUser      = null;
         Role     role1        = null;
         Role     role2        = null;
         Role     testRole     = null;
         Role     userRole     = null;
         Object[] arrTestRoles = null;
         int      iDeleted     = 0;
         String   strIDs       = null;       
         
         AccessRight accessRightView   = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_roleFactory.createPersonal(curUser);
   
            m_transaction.begin();
            try
            {
               arrTestRoles = constructTestRoles();
               role1 = (Role)arrTestRoles[0];
               role2 = (Role)arrTestRoles[1];
            
               role1 = (Role)m_roleFactory.create(role1);
               role2 = (Role)m_roleFactory.create(role2);
   
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
               m_roleFactory.assignToUser(curUser.getId(),
                                          Integer.toString(role1.getId()));
               m_roleFactory.assignToUser(curUser.getId(),
                                          Integer.toString(role2.getId()));
   
               // create access right that curUser can delete only role1 object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_DELETE,
                                             m_iRoleDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             role1.getId(),
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
            // 1. try to delete role1 and role2
            //------------------------
            strIDs = role1.getId() + "," + role2.getId(); 
            m_transaction.begin();
            try
            {
               // try to delete both roles 
               iDeleted = m_roleControl.delete(strIDs);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            
            // role1 has to be deleted
            assertEquals("Number of deleted roles is incorrect", 1, iDeleted);
            testRole = (Role)m_roleControl.get(role1.getId());
            assertNull("Role 1 should be deleted but was not", testRole);
            role1 = null;
            testRole = (Role)m_roleControl.get(role2.getId());
            assertNotNull("Role 2 should not be deleted", testRole);
         }
         finally
         {
            deleteTestData(userRole, role1, role2, null, null);
         }
      }
   
      /**
       * Test for deleting of the roles specified by IDs. User have access granted to a group 
       * of objects based on categories the objects belong to.
       * 
       *  @throws Exception - an error has occurred
       */
      public void testDeleteMoreCheckCategoryEnabled(
      ) throws Exception 
      {
         User     curUser      = null;
         Role     role1        = null;
         Role     role2        = null;
         Role     testRole     = null;
         Role     userRole     = null;
         Object[] arrTestRoles = null;
         int      iDeleted     = 0;
         String   strIDs       = null;       
         
         AccessRight accessRightView   = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_roleFactory.createPersonal(curUser);
   
            m_transaction.begin();
            try
            {
               arrTestRoles = constructTestRoles();
               role1 = (Role)arrTestRoles[0];
               role2 = (Role)arrTestRoles[1];
            
               role1 = (Role)m_roleFactory.create(role1);
               role2 = (Role)m_roleFactory.create(role2);
   
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
               m_roleFactory.assignToUser(curUser.getId(),
                                          Integer.toString(role1.getId()));
               m_roleFactory.assignToUser(curUser.getId(),
                                          Integer.toString(role2.getId()));
   
               // create access right that curUser can delete only role object that is not enabled
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_DELETE,
                                             m_iRoleDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             RoleDataDescriptor.COL_ROLE_ENABLED,
                                             DataCondition.DATA_CODE_FLAG_NO,
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
            // 1. try to enable role1 and role2
            //------------------------
            strIDs = role1.getId() + "," + role2.getId(); 
            m_transaction.begin();
            try
            {
               // try to delete both roles 
               iDeleted = m_roleControl.delete(strIDs);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            
            // role1 has to be deleted
            assertEquals("Number of deleted roles is incorrect", 1, iDeleted);
            testRole = (Role)m_roleControl.get(role1.getId());
            assertNull("Role 1 should be deleted but was not", testRole);
            role1 = null;
            testRole = (Role)m_roleControl.get(role2.getId());
            assertNotNull("Role 2 should not be deleted", testRole);
         }
         finally
         {
            deleteTestData(userRole, role1, role2, null, null);
         }
      }
   
      /**
       * Test for update enable of the roles. User have action granted for all data objects 
       * of given type (no id and no categories specified when granting access).
       * 
       * @throws Exception - an error has occurred
       */
      public void testUpdateEnableCheck(
      ) throws Exception 
      {
         User     curUser      = null;
         Role     role1        = null;
         Role     role2        = null;
         Role     testRole     = null;
         Role     userRole     = null;
         Object[] arrTestRoles = null;
         int      iEnabled     = 0;
         String   strIDs       = null;       
         
         AccessRight accessRightView   = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_roleFactory.createPersonal(curUser);
   
            m_transaction.begin();
            try
            {
               arrTestRoles = constructTestRoles();
               role1 = (Role)arrTestRoles[0];
               role2 = (Role)arrTestRoles[1];
            
               role1 = (Role)m_roleFactory.create(role1);
               role2 = (Role)m_roleFactory.create(role2);
   
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
               m_roleFactory.assignToUser(curUser.getId(),
                                          Integer.toString(role1.getId()));
               m_roleFactory.assignToUser(curUser.getId(),
                                          Integer.toString(role2.getId()));
   
               // create access right that curUser can enable all role objects
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_MODIFY,
                                             m_iRoleDataType,
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
            // 1. try to enable role1 and role2
            //------------------------
            strIDs = role1.getId() + "," + role2.getId(); 
            m_transaction.begin();
            try
            {
               // try to delete both roles 
               iEnabled = m_roleControl.updateEnable(strIDs, false);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            
            // role1 has to be enabled
            assertEquals("Number of enabled roles is incorrect", 2, iEnabled);
            testRole = (Role)m_roleControl.get(role1.getId());
            assertEquals("Role 1 should be enabled but was not", 
                         false, testRole.isEnabled());
            testRole = (Role)m_roleControl.get(role2.getId());
            assertEquals("Role 2 should be enabled but was not", 
                         false, testRole.isEnabled());
         }
         finally
         {
            deleteTestData(userRole, role1, role2, null, null);
         }
      }
   
      /**
       * Test for update enable of the roles. User have access granted to specific
       * data object (identified by identifier) of given data type.
       * 
       * @throws Exception - an error has occurred
       */
      public void testUpdateEnableCheckId(
      ) throws Exception 
      {
         User     curUser      = null;
         Role     role1        = null;
         Role     role2        = null;
         Role     testRole     = null;
         Role     userRole     = null;
         Object[] arrTestRoles = null;
         int      iEnabled     = 0;
         String   strIDs       = null;       
         
         AccessRight accessRightView   = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_roleFactory.createPersonal(curUser);
   
            m_transaction.begin();
            try
            {
               arrTestRoles = constructTestRoles();
               role1 = (Role)arrTestRoles[0];
               role2 = (Role)arrTestRoles[1];
            
               role1 = (Role)m_roleFactory.create(role1);
               role2 = (Role)m_roleFactory.create(role2);
   
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
               m_roleFactory.assignToUser(curUser.getId(),
                                          Integer.toString(role1.getId()));
               m_roleFactory.assignToUser(curUser.getId(),
                                          Integer.toString(role2.getId()));
   
               // create access right that curUser can enable only role1 objects
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_MODIFY,
                                             m_iRoleDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             role1.getId(),
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
            // 1. try to enable role1 and role2
            //------------------------
            strIDs = role1.getId() + "," + role2.getId(); 
            m_transaction.begin();
            try
            {
               // try to delete both roles 
               iEnabled = m_roleControl.updateEnable(strIDs, false);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            
            // role1 has to be enabled
            assertEquals("Number of enabled roles is incorrect", 1, iEnabled);
            testRole = (Role)m_roleControl.get(role1.getId());
            assertEquals("Role 1 should be enabled but was not", 
                         false, testRole.isEnabled());
            // role1 has not be enabled
            testRole = (Role)m_roleControl.get(role2.getId());
            assertEquals("Role 2 should not be enabled ", true, testRole.isEnabled());
         }
         finally
         {
            deleteTestData(userRole, role1, role2, null, null);
         }
      }
   
      /**
       * Test for update enable of the roles. User have access granted to a group 
       * of objects based on categories the objects belong to.
       * 
       * @throws Exception - an error has occurred
       */
      public void testUpdateEnableCheckCategoryEnabled(
      ) throws Exception 
      {
         User     curUser      = null;
         Role     role1        = null;
         Role     role2        = null;
         Role     testRole     = null;
         Role     userRole     = null;
         Object[] arrTestRoles = null;
         int      iEnabled     = 0;
         String   strIDs       = null;       
         
         AccessRight accessRightView   = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_roleFactory.createPersonal(curUser);
   
            m_transaction.begin();
            try
            {
               arrTestRoles = constructTestRoles();
               role1 = (Role)arrTestRoles[0];
               role2 = (Role)arrTestRoles[1];
            
               role1 = (Role)m_roleFactory.create(role1);
               role2 = (Role)m_roleFactory.create(role2);
   
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
               m_roleFactory.assignToUser(curUser.getId(),
                                          Integer.toString(role1.getId()));
               m_roleFactory.assignToUser(curUser.getId(),
                                          Integer.toString(role2.getId()));
   
               // create access right that curUser can enable only role object that is not enabled
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_MODIFY,
                                             m_iRoleDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             RoleDataDescriptor.COL_ROLE_ENABLED,
                                             DataCondition.DATA_CODE_FLAG_NO,
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
            // 1. try to enable role1 and role2
            //------------------------
            strIDs = role1.getId() + "," + role2.getId(); 
            m_transaction.begin();
            try
            {
               // try to delete both roles 
               iEnabled = m_roleControl.updateEnable(strIDs, false);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            
            // role1 has to be enabled
            assertEquals("Number of enabled roles is incorrect", 1, iEnabled);
            testRole = (Role)m_roleControl.get(role1.getId());
            assertEquals("Role 1 should be enabled but was not", 
                         false, testRole.isEnabled());
            // role1 has not be enabled
            testRole = (Role)m_roleControl.get(role2.getId());
            assertEquals("Role 2 should not be enabled ", true, testRole.isEnabled());
         }
         finally
         {
            deleteTestData(userRole, role1, role2, null, null);
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
       * Method construct 2 test roles that will be used for all tests
       * 
       * @return Object[] - array of created roles
       *                  - index 0 = role1 object; index 1 = role2 object
       * @throws OSSException - error occurred during of test roles creating
       */
      private Object[] constructTestRoles(
      ) throws OSSException
      {
         Role role1 = null;
         Role role2 = null;
         int iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
   
         // create 2 test roles
         role1 = new Role(DataObject.NEW_ID, 
                          iCurrentDomainID, 
                          "testname1", 
                          "testdescription1", 
                          false, // role enabled 
                          DataObject.NEW_ID, 
                          false, // role unmodifiable 
                          null, null, null);
      
         role2 =  new Role(DataObject.NEW_ID, 
                           iCurrentDomainID, 
                           "testname2", 
                           "testdescription2", 
                           true,  // role enabled
                           DataObject.NEW_ID, 
                           false, // role unmodifiable
                           null, null, null);
   
         return new Object[] {role1, role2};  
      }
   
      /**
       * Method for deleting test data - it will be used for all tests
       * 
       * @param userRole - user role that will be deleted
       * @param role1 - role 1 that will be deleted
       * @param role2 - role 2 that will be deleted
       * @param user1 - user 1 that will be deleted
       * @param user2 - user 2 that will be deleted
       * @throws Exception - error occurred during deleting of test data 
       */
      private void deleteTestData(
         Role userRole,
         Role role1,
         Role role2,
         User user1,
         User user2
      ) throws Exception
      {
         m_transaction.begin();
         try
         {
            if ((userRole != null) && (userRole.getId() != DataObject.NEW_ID))
            {
               StringBuffer sbUserIDs = new StringBuffer();

               // delete role and also data from user-role map table (necessary 
               // for Sybase ASE)
               m_rightFactory.deleteAllForRole(userRole.getId());

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
            }
            if ((role1 != null) && (role1.getId() != DataObject.NEW_ID))
            {
               m_roleFactory.delete(
                  role1.getId(),
                  CallContext.getInstance().getCurrentDomainId());
            }
            if ((role2 != null) && (role2.getId() != DataObject.NEW_ID))
            {
               m_roleFactory.delete(
                  role2.getId(),
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
}
