/*
 * Copyright (c) 2003 - 2009 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: UserControllerTest.java,v 1.21 2009/09/20 05:32:57 bastafidli Exp $
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
import org.opensubsystems.core.data.DataObject;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.logic.ControllerManager;
import org.opensubsystems.core.persist.DataFactoryManager;
import org.opensubsystems.core.persist.db.DatabaseTestSuite;
import org.opensubsystems.core.util.CallContext;
import org.opensubsystems.core.util.Tests;
import org.opensubsystems.patterns.listdata.data.SimpleRule;
import org.opensubsystems.security.application.SecurityBackendModule;
import org.opensubsystems.security.data.Role;
import org.opensubsystems.security.data.User;
import org.opensubsystems.security.patterns.listdata.logic.impl.SecureListControllerTest;
import org.opensubsystems.security.persist.RoleFactory;
import org.opensubsystems.security.persist.UserFactory;
import org.opensubsystems.security.persist.db.UserListDatabaseTestUtils;
import org.opensubsystems.security.utils.TestRoleDatabaseFactoryUtils;

/**
 * Test for UserController interface implementation class. 
 * 
 * @version $Id: UserControllerTest.java,v 1.21 2009/09/20 05:32:57 bastafidli Exp $
 * @author Julo Legeny
 * @code.reviewer Miro Halas
 * @code.reviewed Initial revision
 */
public final class UserControllerTest extends Tests
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private UserControllerTest(
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
      TestSuite suite = new DatabaseTestSuite("UserControllerTest");
      suite.addTestSuite(UserControllerTestInternal.class);
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
   public static class UserControllerTestInternal extends SecureListControllerTest
   {
      // Cached values ////////////////////////////////////////////////////////////
   
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
      public UserControllerTestInternal(
         String strTestName
      ) throws Exception
      {
         super(strTestName, new UserListDatabaseTestUtils());
   
         m_roleFactory = (RoleFactory)DataFactoryManager.getInstance(
                                         RoleFactory.class);      
         m_userFactory = (UserFactory)DataFactoryManager.getInstance(
                                         UserFactory.class);
         m_userControl = (UserController)ControllerManager.getInstance(
                                            UserController.class);
         m_roleFactoryUtils = new TestRoleDatabaseFactoryUtils();
      }
   
      // Tests ////////////////////////////////////////////////////////////////////
   
      /**
       * Test for getting of the user
       * 
       *  @throws Exception - an error has occurred
       */
      public void testGetById(
      ) throws Exception 
      {
         User user    = null;
         User testUser = null;
         Role testPersonalRole = null;
   
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
                                true,
                                true,
                                true,
                                null,
                                null, true
                               );
            
               user = (User)m_userControl.create(user);
            
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // Now we need to test if personal role was changed according to user changes
            testPersonalRole = m_roleFactory.getPersonal(user.getId());
   
            // try to get user with specific ID
            testUser = (User)m_userControl.get(user.getId());
            assertNotNull("User should not be null", testUser);
            assertEquals("User is not the same", user, testUser);
         }
         finally
         {
            m_transaction.begin();
            try
            {
               // delete personal role first (we delete it explicitly in case
               // something goes wrong with deleting user :-)
               if ((testPersonalRole != null) && (testPersonalRole.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(testPersonalRole.getId());
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
       * Test for getting of the user using login name.
       * 
       *  @throws Exception - an error has occurred
       */
      public void testGetByLoginName(
      ) throws Exception 
      {
         User user    = null;
         User testUser = null;
         Role testPersonalRole = null;
   
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
                                true,
                                true,
                                true,
                                null,
                                null, true
                               );
            
               user = (User)m_userControl.create(user);
            
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // Now we need to test if personal role was changed according to user changes
            testPersonalRole = m_roleFactory.getPersonal(user.getId());
   
            // try to get user with specific login name
            testUser = m_userControl.get(user.getLoginName());
            assertNotNull("User should not be null", testUser);
            assertEquals("User is not the same", user, testUser);
         }
         finally
         {
            m_transaction.begin();
            try
            {
               // delete personal role first (we delete it explicitly in case
               // something goes wrong with deleting user :-)
               if ((testPersonalRole != null) && (testPersonalRole.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(testPersonalRole.getId());
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
       * Test for getting user with assigned roles
       * 
       *  @throws Exception - an error has occurred
       */
      public void testGetUserWithRoles(
      ) throws Exception 
      {
         User     user     = null;
         User     testUser  = null;
         Role     testPersonalRole = null;
         Role     role1    = null;
         Role     role2    = null;
         Object[] objReturn = {null, null};
         List     roles  = null;
         
         try
         {
            m_transaction.begin();
            try
            {
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
                                true,
                                true,
                                true,
                                null,
                                null, true
                               );
               user = m_userControl.create(user, "");
   
               role1 = new Role(DataObject.NEW_ID, 
                                 CallContext.getInstance().getCurrentDomainId(), 
                                 "test_name_1",
                                 "test_description_1",
                                 true,
                                 DataObject.NEW_ID,
                                 false,
                                 null,
                                 null,
                                 null);
               role1 = (Role)m_roleFactory.create(role1);
            
               role2 = new Role(DataObject.NEW_ID, 
                                 CallContext.getInstance().getCurrentDomainId(), 
                                 "test_name_2",
                                 "test_description_2",
                                 true,
                                 DataObject.NEW_ID,
                                 false,
                                 null,
                                 null,
                                 null);
               role2 = (Role)m_roleFactory.create(role2);
   
               m_roleFactory.assignToUser(user.getId(), role1.getId() + "," + role2.getId());
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // Now we need to test if personal role was changed according to user changes
            testPersonalRole = m_roleFactory.getPersonal(user.getId());
   
            // try to get user with roles
            objReturn = m_userControl.getUserWithRoles(user.getId());         
            testUser = (User) objReturn[0];
            roles = (List) objReturn[1];
         
            assertNotNull("User should not be null", testUser);
            assertEquals("User is not the same", user, testUser);
            assertNotNull("List of user roles should not be null", roles);
            assertEquals("Number of assigned roles is incorrect", 
                         3, // plus 1 for personal 
                         roles.size());
            assertTrue("Personal role should be assigned", 
                       testPersonalRole.equals(roles.get(0))
                       || testPersonalRole.equals(roles.get(1))
                       || testPersonalRole.equals(roles.get(2)));
            assertTrue("Role1 should be assigned", 
                       role1.equals(roles.get(0))
                       || role1.equals(roles.get(1))
                       || role1.equals(roles.get(2)));
            assertTrue("Role2 should be assigned", 
                       role2.equals(roles.get(0))
                       || role2.equals(roles.get(1))
                       || role2.equals(roles.get(2)));
         }
         finally
         {
            m_transaction.begin();
            try
            {
               // delete personal role first (we delete it explicitly in case
               // something goes wrong with deleting user :-)
               if ((testPersonalRole != null) && (testPersonalRole.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(testPersonalRole.getId());
               }
               if ((role1 != null) && (role1.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(role1.getId());
               }
               if ((role2 != null) && (role2.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(role2.getId());
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
       * Test of getRoles methods in controller
       * 
       * @throws Exception - an error has occurred
       */
      public void testRoles(
      ) throws Exception
      {
         Role role1 = null;
         Role role2 = null;
         Role role3 = null;
         
         try
         {      
            m_transaction.begin();      
            try
            {
               role1 = new Role(DataObject.NEW_ID, 
                                   CallContext.getInstance().getCurrentDomainId(), 
                                   "testname1", "testdescription1", true, DataObject.NEW_ID, false, 
                                   null, null, null);
               role1 = (Role)m_roleFactory.create(role1);
         
               role2 = new Role(DataObject.NEW_ID, 
                                   CallContext.getInstance().getCurrentDomainId(), 
                                   "testname2", "testdescription2", true, DataObject.NEW_ID, false, 
                                   null, null, null);
               role2 = (Role)m_roleFactory.create(role2);
         
               role3 = new Role(DataObject.NEW_ID, 
                                   CallContext.getInstance().getCurrentDomainId(), 
                                   "testname3", "testdescription3", true, DataObject.NEW_ID, false, 
                                   null, null, null);
               role3 = (Role)m_roleFactory.create(role3);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }         
            // new user will be created without personal role
            m_roleFactory.assignToUser(
                  CallContext.getInstance().getCurrentUserId(),
                  role1.getId() + "," + role2.getId());
            
            List roles = m_userControl.getRoles(CallContext.getInstance().getCurrentUserId());
            
            assertNotNull("Returned Role list is null", roles);
            assertEquals("Returned Role list size is not correct", 2, roles.size());  
            assertTrue("Role1 should be assigned", 
                       role1.equals(roles.get(0))
                       || role1.equals(roles.get(1))
                       || role1.equals(roles.get(2)));
            assertTrue("Role2 should be assigned", 
                       role2.equals(roles.get(0))
                       || role2.equals(roles.get(1))
                       || role2.equals(roles.get(2)));
         }
         finally
         {
            m_transaction.begin();
            try
            {
               // delete personal role first (we delete it explicitly in case
               // something goes wrong with deleting user :-)
               if ((role1 != null) && (role1.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(role1.getId());
               }
               if ((role2 != null) && (role2.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(role2.getId());
               }
               if ((role3 != null) && (role3.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(role3.getId());
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
       * Test for creating user
       * 
       *  @throws Exception - an error has occurred
       */
      public void testCreate(
      ) throws Exception 
      {
         User newUser  = null;
         User user    = null;
         User selectedUser = null;
         Role role1   = null;
         Role role2   = null;
         Role selectedRole = null;
         List roles;
   
         newUser = new User(DataObject.NEW_ID,
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
                            true,
                            true,
                            true,
                            null,
                            null, true
                           );
   
         try
         {
            m_transaction.begin();
            try
            {
               // create 2 new roles
               role1 = new Role(DataObject.NEW_ID, 
                                 CallContext.getInstance().getCurrentDomainId(), 
                                 "test_name_1",
                                 "test_description_1",
                                 true,
                                 DataObject.NEW_ID,
                                 false,
                                 null,
                                 null,
                                 null);
               role1 = (Role)m_roleFactory.create(role1);
            
               role2 = new Role(DataObject.NEW_ID, 
                                 CallContext.getInstance().getCurrentDomainId(), 
                                 "test_name_2",
                                 "test_description_2",
                                 true,
                                 DataObject.NEW_ID,
                                 false,
                                 null,
                                 null,
                                 null);
               role2 = (Role)m_roleFactory.create(role2);
   
               // try to create user without roles
               user = (User)m_userControl.create(newUser);
            
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
    
            assertNotNull("User should be not null", user);
            
            assertTrue("Id was not generated", user.getId() != DataObject.NEW_ID);
            assertNotNull("Creation timestamp was not generated", 
                          user.getCreationTimestamp());
            assertNotNull("Modification timestamp was not generated", 
                          user.getModificationTimestamp());
            assertTrue("Domain is not the same", newUser.isSame(user));
   
            // Use factory to perform the verification operation since we relay
            // that the factory was tested
            selectedUser = (User)m_userFactory.get(
                                    user.getId(),
                                    CallContext.getInstance().getCurrentDomainId());
            assertNotNull("Domain should be created but was not", selectedUser);
            assertEquals("User is not the same", user, selectedUser);
               
            selectedRole = m_roleFactory.getPersonal(user.getId());
            comparePersonalRoleWithUser(selectedRole, user);
            
             // try to get roles assigned to the user
            roles = m_roleFactory.getAllForUser(user.getId(), SimpleRule.ALL_DATA);
            assertNotNull("User should have assigned personal role.", roles);
            assertEquals("User should have exactly one assigned role", 
                         1, roles.size());
            assertEquals("User should have assigned personal role.", selectedRole,
                         roles.get(0));
         }
         finally
         {
            m_transaction.begin();
            try
            {
               // delete personal role first (we delete it explicitly in case
               // something goes wrong with deleting user :-)
               if ((selectedRole != null) && (selectedRole.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(selectedRole.getId());
               }
               if ((role1 != null) && (role1.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(role1.getId());
               }
               if ((role2 != null) && (role2.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(role2.getId());
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
       * Test for creating user
       * 
       *  @throws Exception - an error has occurred
       */
      public void testCreateWithNoRoles(
      ) throws Exception 
      {
         User newUser  = null;
         User user    = null;
         User selectedUser = null;
         Role role1   = null;
         Role role2   = null;
         Role selectedRole = null;
         List roles;
   
         newUser = new User(DataObject.NEW_ID,
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
                            true,
                            true,
                            true,
                            null,
                            null, true
                           );
   
         try
         {
            m_transaction.begin();
            try
            {
               // create 2 new roles
               role1 = new Role(DataObject.NEW_ID, 
                                 CallContext.getInstance().getCurrentDomainId(), 
                                 "test_name_1",
                                 "test_description_1",
                                 true,
                                 DataObject.NEW_ID,
                                 false,
                                 null,
                                 null,
                                 null);
               role1 = (Role)m_roleFactory.create(role1);
            
               role2 = new Role(DataObject.NEW_ID, 
                                 CallContext.getInstance().getCurrentDomainId(), 
                                 "test_name_2",
                                 "test_description_2",
                                 true,
                                 DataObject.NEW_ID,
                                 false,
                                 null,
                                 null,
                                 null);
               role2 = (Role)m_roleFactory.create(role2);
   
               // try to create user without roles
               user = m_userControl.create(newUser, "");
            
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
    
            assertNotNull("User should be not null", user);
            
            assertTrue("Id was not generated", user.getId() != DataObject.NEW_ID);
            assertNotNull("Creation timestamp was not generated", 
                          user.getCreationTimestamp());
            assertNotNull("Modification timestamp was not generated", 
                          user.getModificationTimestamp());
            assertTrue("Domain is not the same", newUser.isSame(user));
   
            // Use factory to perform the verification operation since we relay
            // that the factory was tested
            selectedUser = (User)m_userFactory.get(
                                    user.getId(),
                                    CallContext.getInstance().getCurrentDomainId());
            assertNotNull("Domain should be created but was not", selectedUser);
            assertEquals("User is not the same", user, selectedUser);
               
            selectedRole = m_roleFactory.getPersonal(user.getId());
            comparePersonalRoleWithUser(selectedRole, user);
            
             // try to get roles assigned to the user
            roles = m_roleFactory.getAllForUser(user.getId(), SimpleRule.ALL_DATA);
            assertNotNull("User should have assigned personal role.", roles);
            assertEquals("User should have exactly one assigned role", 
                         1, roles.size());
            assertEquals("User should have assigned personal role.", selectedRole,
                         roles.get(0));
         }
         finally
         {
            m_transaction.begin();
            try
            {
               // delete personal role first (we delete it explicitly in case
               // something goes wrong with deleting user :-)
               if ((selectedRole != null) && (selectedRole.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(selectedRole.getId());
               }
               if ((role1 != null) && (role1.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(role1.getId());
               }
               if ((role2 != null) && (role2.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(role2.getId());
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
       * Test for creating user
       * 
       *  @throws Exception - an error has occurred
       */
      public void testCreateWithRoles(
      ) throws Exception 
      {
         User newUser  = null;
         User user    = null;
         User selectedUser = null;
         Role role1   = null;
         Role role2   = null;
         Role selectedRole = null;
         List roles;
         
         newUser = new User(DataObject.NEW_ID,
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
                            true,
                            true,
                            true,
                            null,
                            null, true
                           );
   
         try
         {
            m_transaction.begin();
            try
            {
               // create 2 new roles
               role1 = new Role(DataObject.NEW_ID, 
                                 CallContext.getInstance().getCurrentDomainId(), 
                                 "test_name_1",
                                 "test_description_1",
                                 true,
                                 DataObject.NEW_ID,
                                 false,
                                 null,
                                 null,
                                 null);
               role1 = (Role)m_roleFactory.create(role1);
            
               role2 = new Role(DataObject.NEW_ID, 
                                 CallContext.getInstance().getCurrentDomainId(), 
                                 "test_name_2",
                                 "test_description_2",
                                 true,
                                 DataObject.NEW_ID,
                                 false,
                                 null,
                                 null,
                                 null);
               role2 = (Role)m_roleFactory.create(role2);
   
               // try to create user without roles
               user = m_userControl.create(newUser, role1.getId() + "," + role2.getId());
            
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            assertNotNull("User should be not null", user);
            
            assertTrue("Id was not generated", user.getId() != DataObject.NEW_ID);
            assertNotNull("Creation timestamp was not generated", 
                          user.getCreationTimestamp());
            assertNotNull("Modification timestamp was not generated", 
                          user.getModificationTimestamp());
            assertTrue("Domain is not the same", newUser.isSame(user));
   
            // Use factory to perform the verification operation since we relay
            // that the factory was tested
            selectedUser = (User)m_userFactory.get(
                                    user.getId(),
                                    CallContext.getInstance().getCurrentDomainId());
            assertNotNull("Domain should be created but was not", selectedUser);
            assertEquals("User is not the same", user, selectedUser);
               
            selectedRole = m_roleFactory.getPersonal(user.getId());
            comparePersonalRoleWithUser(selectedRole, user);
            
             // try to get roles assigned to the user
            roles = m_roleFactory.getAllForUser(user.getId(), SimpleRule.ALL_DATA);
            assertNotNull("User should have assigned roles.", roles);
            assertEquals("User should have exactly 3 assigned roles", 
                         3, roles.size());
            assertTrue("User should have assigned personal role.", 
                       selectedRole.equals(roles.get(0))
                       || selectedRole.equals(roles.get(1))
                       || selectedRole.equals(roles.get(2)));
            assertTrue("User should have assigned role 1.", 
                       role1.equals(roles.get(0))
                       || role1.equals(roles.get(1))
                       || role1.equals(roles.get(2)));
            assertTrue("User should have assigned role 2.", 
                       role2.equals(roles.get(0))
                       || role2.equals(roles.get(1))
                       || role2.equals(roles.get(2)));
         }
         finally
         {
            m_transaction.begin();
            try
            {
               // delete personal role first (we delete it explicitly in case
               // something goes wrong with deleting user :-)
               if ((selectedRole != null) && (selectedRole.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(selectedRole.getId());
               }
               if ((role1 != null) && (role1.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(role1.getId());
               }
               if ((role2 != null) && (role2.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(role2.getId());
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
       * Test for saving user
       * 
       *  @throws Exception - error occurred
       */
      public void testSave(
      ) throws Exception 
      {
         User user1   = null;
         User user2   = null;
         User testUser = null;
         Role testPersonalRole = null;
         
         try
         {
            m_transaction.begin();
            try
            {
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
                                 true,
                                 true,
                                 true,
                                 null,
                                 null, true
                                );
               user1 = m_userControl.create(user1, "");
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }         
            user2 = new User(user1.getId(),
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
                              user1.getCreationTimestamp(),
                              user1.getModificationTimestamp(), true
                             );
   
            m_transaction.begin();
            try
            {
               // try to modify user without roles (role1)
               testUser = (User)m_userControl.save(user2);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // Here we just test if the method returns what we have passed into it
            compareUsers(testUser, user2);
                                                     
            // Now we need to test if in the database is what we have wrote there
            testUser = (User)m_userFactory.get(
                                testUser.getId(),
                                CallContext.getInstance().getCurrentDomainId());
            compareUsers(testUser, user2);
            
            // Now we need to test if personal role was changed according to user changes
            testPersonalRole = m_roleFactory.getPersonal(testUser.getId());
            comparePersonalRoleWithUser(testPersonalRole, user2);
         }
         finally
         {
            m_transaction.begin();
            try
            {
               // delete personal role first (we delete it explicitly in case
               // something goes wrong with deleting user :-)
               if ((testPersonalRole != null) && (testPersonalRole.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(testPersonalRole.getId());
               }
               if ((user1 != null) && (user1.getId() != DataObject.NEW_ID))
               {
                  m_userFactory.delete(
                                   user1.getId(),
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
       * Test for saving user without changing password
       * 
       *  @throws Exception - an error has occurred
       */
      public void testSaveWithNoPasswordChange(
      ) throws Exception 
      {
         User user1    = null;
         User user2    = null;
         User testUser  = null;
         User testUser2 = null;
         Role testPersonalRole = null;
         
         try
         {
            m_transaction.begin();
            try
            {
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
                                 true,
                                 true,
                                 true,
                                 null,
                                 null, true
                                );
               user1 = m_userControl.create(user1, "");
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }         
            user2 = new User(user1.getId(),
                              CallContext.getInstance().getCurrentDomainId(),
                              "test_first_name_2",
                              "test_last_name_2",
                              "test_phone_2",
                              "test_fax_2",
                              "test_address_2",
                              "test_email_2",
                              "test_login_name_2",
                              "", // no password change
                              false,
                              false,
                              false,
                              false,
                              user1.getCreationTimestamp(),
                              user1.getModificationTimestamp(), true
                             );
   
            m_transaction.begin();
            try
            {
               // try to modify user without roles (role1)
               testUser = (User)m_userControl.save(user2);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // Here we just test if the method returns what we have passed into it
            compareUsers(testUser, user2);
                                                     
            // Now load the user straight from database and the password must remain the same
            testUser2 = (User)m_userFactory.get(
                                 user1.getId(),
                                 CallContext.getInstance().getCurrentDomainId());
            compareUsersWithoutPassword(testUser2, user2);
            assertEquals("User password is incorrect", user1.getPassword(),
                                                     testUser2.getPassword());
            assertFalse("User password in memory must be different than the one in the database",
                       testUser.getPassword().equals(testUser2.getPassword()));
   
            // Now we need to test if personal role was changed according to user changes
            testPersonalRole = m_roleFactory.getPersonal(testUser.getId());
            comparePersonalRoleWithUser(testPersonalRole, user2);
         }
         finally
         {
            m_transaction.begin();
            try
            {
               // delete personal role first (we delete it explicitly in case
               // something goes wrong with deleting user :-)
               if ((testPersonalRole != null) && (testPersonalRole.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(testPersonalRole.getId());
               }
               if ((user1 != null) && (user1.getId() != DataObject.NEW_ID))
               {
                  m_userFactory.delete(
                                   user1.getId(),
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
       * Test for changing of login name for user
       * 
       *  @throws Exception - an error has occurred
       */
      public void testChangeLoginName(
      ) throws Exception 
      {
         User user1   = null;
         User testUser = null;
         Role testPersonalRole = null;
   
         String strNewLoginName = "new_login_name";
         String strNewEmail = "new_email";
         
         try
         {
            m_transaction.begin();
            try
            {
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
                                 true,
                                 true,
                                 true,
                                 null,
                                 null, true
                                );
               user1 = m_userControl.create(user1, "");
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }         
            testPersonalRole = m_roleFactory.getPersonal(user1.getId());
            m_transaction.begin();
            try
            {
               // try to modify user without roles (role1)
               testUser = m_userControl.changeLoginNameAndEmail(
                             user1.getId(), user1.getModificationTimestamp(),
                             strNewLoginName, strNewEmail);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // Here we just test if the method returns what we have passed into it
            assertNotNull("User should not be null", testUser);
            assertEquals("The login name doesn't match", strNewLoginName, 
                         testUser.getLoginName());
            assertEquals("The email doesn't match", strNewEmail, 
                         testUser.getEmail());
            compareUsersWithoutLoginNameAndEmail(testUser, user1);
                                                     
            // Now we need to test if in the database is what we have wrote there
            testUser = (User)m_userFactory.get(
                                testUser.getId(),
                                CallContext.getInstance().getCurrentDomainId());
            assertNotNull("User should not be null", testUser);
            assertEquals("The login name doesn't match", strNewLoginName, 
                         testUser.getLoginName());
            assertEquals("The email doesn't match", strNewEmail, 
                         testUser.getEmail());
            compareUsersWithoutLoginNameAndEmail(testUser, user1);
                                                              
            // Now we need to test if personal role was changed according to user changes
            testPersonalRole = m_roleFactory.getPersonal(testUser.getId());
            comparePersonalRoleWithUser(testPersonalRole, testUser);
         }
         finally
         {
            m_transaction.begin();
            try
            {
               // delete personal role first (we delete it explicitly in case
               // something goes wrong with deleting user :-)
               if ((testPersonalRole != null) && (testPersonalRole.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(testPersonalRole.getId());
               }
               if ((user1 != null) && (user1.getId() != DataObject.NEW_ID))
               {
                  m_userFactory.delete(
                                   user1.getId(),
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
       * Test for changing password for user
       * 
       *  @throws Exception - an error has occurred
       */
      public void testChangePassword(
      ) throws Exception 
      {
         User user   = null;
         User testUser = null;
         
         String strNewPassword = "new_test_password_1";
         String oldPassword = "test_password_1";
         
         try
         {
            m_transaction.begin();
            try
            {
               user = new User(
                        DataObject.NEW_ID,
                        CallContext.getInstance().getCurrentDomainId(),
                        "test_first_name_1", "test_last_name_1",
                        "test_phone_1", "test_fax_1",
                        "test_address_1", "test_email_1",
                        "test_login_name_1",
                        oldPassword,
                        true, true, true, true, null, null, true
               );
               user = (User)m_userFactory.create(user);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }         
            // Try incorrect user name and password 
            m_transaction.begin();
            try
            {
               // try to change password on non existing user
               m_userControl.changePassword("unknown", "wrong_password",
                                            strNewPassword);
               fail("Change of password with invalid credential should not be allowed.");
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               // This is expected error
               m_transaction.rollback();
            }
            // Now we need to test if in the database is what we have wrote there
            testUser = (User)m_userFactory.get(
                                user.getId(),
                                CallContext.getInstance().getCurrentDomainId());
            compareUsers(testUser, user);
            
            // Try incorrect password 
            m_transaction.begin();
            try
            {
               // try to change password on non existing user
               m_userControl.changePassword(user.getLoginName(), "wrong_password",
                                            strNewPassword);
               fail("Change of password with invalid credential should not be allowed.");
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               // This is expected error
               m_transaction.rollback();
            }
            // Check if nothing was changed
            testUser = (User)m_userFactory.get(
                                user.getId(),
                                CallContext.getInstance().getCurrentDomainId());
            compareUsers(testUser, user);
            
            // Try correct change 
            Thread.sleep(1000);
            
            m_transaction.begin();
            try
            {
               // try to change password on non existing user
               m_userControl.changePassword(user.getLoginName(), oldPassword,
                                            strNewPassword);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // Check if nothing was changed
            testUser = (User)m_userFactory.get(
                                user.getId(),
                                CallContext.getInstance().getCurrentDomainId());
            compareUsersWithoutPassword(testUser, user);
            assertTrue("Record was not changed.", 
                       user.getModificationTimestamp().getTime() 
                          < testUser.getModificationTimestamp().getTime());
            assertNotNull("Password should have been retrieved", testUser);
            assertEquals("Changed password doesn't match.", 
                         User.encryptPassword(strNewPassword),
                         testUser.getPassword());
            assertEquals("The email doesn't match", user.getEmail(), 
                         testUser.getEmail());
         }
         finally
         {
            m_transaction.begin();
            try
            {
               // delete users
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
       * Test for saving user with roles assigned 
       * 
       *  @throws Exception - an error has occurred
       */
      public void testSaveWithRoles(
      ) throws Exception 
      {
         User user1    = null;
         User user2    = null;
         User testUser  = null;
         User testUser2 = null;
         Role role1    = null;
         Role role2    = null;
         Role testPersonalRole = null;
         List roles;
   
         try
         {
            m_transaction.begin();
            try
            {
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
                                 true,
                                 true,
                                 true,
                                 null,
                                 null, true
                                );
               user1 = m_userControl.create(user1, "");
               
               role1 = new Role(DataObject.NEW_ID, 
                                 CallContext.getInstance().getCurrentDomainId(), 
                                 "test_name_1",
                                 "test_description_1",
                                 true,
                                 DataObject.NEW_ID,
                                 false,
                                 null,
                                 null,
                                 null);
               role1 = (Role)m_roleFactory.create(role1);
            
               role2 = new Role(DataObject.NEW_ID, 
                                 CallContext.getInstance().getCurrentDomainId(), 
                                 "test_name_2",
                                 "test_description_2",
                                 true,
                                 DataObject.NEW_ID,
                                 false,
                                 null,
                                 null,
                                 null);
               role2 = (Role)m_roleFactory.create(role2);
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }         
            testPersonalRole = m_roleFactory.getPersonal(user1.getId());
   
            user2 = new User(user1.getId(),
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
                              user1.getCreationTimestamp(),
                              user1.getModificationTimestamp(), true
                             );
   
            m_transaction.begin();
            try
            {
               // try to modify user without roles (role1)
               testUser = m_userControl.save(user2, "", "");
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            compareUsers(testUser, user2);
            // Now we need to test if in the database is what we have wrote there
            testUser2 = (User)m_userFactory.get(
                                 testUser.getId(),
                                 CallContext.getInstance().getCurrentDomainId());
            compareUsers(testUser2, user2);
            
            // test personal role for changes
            testPersonalRole = m_roleFactory.getPersonal(testUser2.getId());
            comparePersonalRoleWithUser(testPersonalRole, testUser2);
   
            roles = m_roleFactory.getAllForUser(testUser.getId(), SimpleRule.ALL_DATA);
            assertNotNull("Personal role should be assigned", roles);
            assertEquals("Only personal role should be assigned", 1, roles.size());
            assertEquals("Only personal role should be assigned", testPersonalRole, roles.get(0));
            
            // Now try to modify roles
            String strUserRoles = role1.getId() + "," + role2.getId();
   
            testUser2 = testUser;
            m_transaction.begin();
            try
            {
               // try to modify user with roles
               testUser = m_userControl.save(testUser2, "", strUserRoles);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            compareUsers(testUser, user2);
            // Now we need to test if in the database is what we have wrote there
            testUser = (User)m_userFactory.get(
                                testUser.getId(),
                                CallContext.getInstance().getCurrentDomainId());
            compareUsers(testUser, testUser2);
            testPersonalRole = m_roleFactory.getPersonal(testUser2.getId());
   
            roles = m_roleFactory.getAllForUser(testUser.getId(), SimpleRule.ALL_DATA);
            assertNotNull("Role should be assigned", roles);
            assertEquals("Multiple roles should be assigned", 3, roles.size());
            assertTrue("Personal role should be assigned", 
                       testPersonalRole.equals(roles.get(0))
                       || testPersonalRole.equals(roles.get(1))
                       || testPersonalRole.equals(roles.get(2)));
            assertTrue("Role1 should be assigned", 
                       role1.equals(roles.get(0))
                       || role1.equals(roles.get(1))
                       || role1.equals(roles.get(2)));
            assertTrue("Role2 should be assigned", 
                       role2.equals(roles.get(0))
                       || role2.equals(roles.get(1))
                       || role2.equals(roles.get(2)));
         }
         finally
         {
            m_transaction.begin();
            try
            {
               // delete personal role first (we delete it explicitly in case
               // something goes wrong with deleting user :-)
               if ((testPersonalRole != null) && (testPersonalRole.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(testPersonalRole.getId());
               }
               if ((role1 != null) && (role1.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(role1.getId());
               }
               if ((role2 != null) && (role2.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(role2.getId());
               }
               if ((user1 != null) && (user1.getId() != DataObject.NEW_ID))
               {
                  m_userFactory.delete(
                                   user1.getId(),
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
       * Test for saving user with roles without changing his password.
       * 
       *  @throws Exception - an error has occurred
       */
      public void testSaveWithRolesWithNoPasswordChange(
      ) throws Exception 
      {
         User user1   = null;
         User user2   = null;
         User testUser = null;
         
         Role role1   = null;
         Role role2   = null;
         Role testPersonalRole = null;
         List roles;
         
         try
         {
            m_transaction.begin();
            try
            {
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
                                 true,
                                 true,
                                 true,
                                 null,
                                 null, true
                                );
               user1 = m_userControl.create(user1, "");
   
               role1 = new Role(DataObject.NEW_ID, 
                                 CallContext.getInstance().getCurrentDomainId(), 
                                 "test_name_1",
                                 "test_description_1",
                                 true,
                                 DataObject.NEW_ID,
                                 false,
                                 null,
                                 null,
                                 null);
               role1 = (Role)m_roleFactory.create(role1);
            
               role2 = new Role(DataObject.NEW_ID, 
                                 CallContext.getInstance().getCurrentDomainId(), 
                                 "test_name_2",
                                 "test_description_2",
                                 true,
                                 DataObject.NEW_ID,
                                 false,
                                 null,
                                 null,
                                 null);
               role2 = (Role)m_roleFactory.create(role2);
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            testPersonalRole = m_roleFactory.getPersonal(user1.getId());
            
            user2 = new User(user1.getId(),
                              CallContext.getInstance().getCurrentDomainId(),
                              "test_first_name_2",
                              "test_last_name_2",
                              "test_phone_2",
                              "test_fax_2",
                              "test_address_2",
                              "test_email_2",
                              "test_login_name_2",
                              "", // empty password means no change
                              false,
                              false,
                              false,
                              false,
                              user1.getCreationTimestamp(),
                              user1.getModificationTimestamp(), true
                             );
   
            m_transaction.begin();
            try
            {
               // try to modify user without roles (role1)
               testUser = m_userControl.save(user2, "", "");
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            compareUsers(testUser, user2);
            // The password in memory remains the same (empty)                      
            assertFalse("User password in memory must be different than the one in the database",
                       user1.getPassword().equals(testUser.getPassword()));
            
            testPersonalRole = m_roleFactory.getPersonal(testUser.getId());
            // testing modified personal role 
            comparePersonalRoleWithUser(testPersonalRole, testUser);
   
            roles = m_roleFactory.getAllForUser(testUser.getId(), SimpleRule.ALL_DATA);
            assertNotNull("Personal role should be assigned", roles);
            assertEquals("Only personal role should be assigned", 1, roles.size());
            assertEquals("Only personal role should be assigned", testPersonalRole, roles.get(0));
   
            // Now load the user straight from database and the password must remain the same
            User testUser2;
            
            testUser2 = (User)m_userFactory.get(
                                 user1.getId(),
                                 CallContext.getInstance().getCurrentDomainId());
   
            assertNotNull("User should not be null", testUser2);
            assertEquals("User password is incorrect", user1.getPassword(),
                                                     testUser2.getPassword());
            assertFalse("User password in memory must be different than the one in the database",
                       testUser.getPassword().equals(testUser2.getPassword()));
   
            // Now try to modify roles
            String strUseroles = role1.getId() + "," + role2.getId();
   
            testUser2 = testUser;
            m_transaction.begin();
            try
            {
               // try to modify user with roles
               testUser = m_userControl.save(testUser2, "", strUseroles);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // The password in this case doesn't change since it is empty after the
            // previous save
            compareUsers(testUser, testUser2);
   
            testPersonalRole = m_roleFactory.getPersonal(testUser.getId());
            // testing modified personal role 
            comparePersonalRoleWithUser(testPersonalRole, testUser);
            
            roles = m_roleFactory.getAllForUser(testUser.getId(), SimpleRule.ALL_DATA);
            assertNotNull("Role should be assigned", roles);
            assertEquals("Multiple roles should be assigned", 3, roles.size());
            assertTrue("Personal role should be assigned", 
                       testPersonalRole.equals(roles.get(0))
                       || testPersonalRole.equals(roles.get(1))
                       || testPersonalRole.equals(roles.get(2)));
            assertTrue("Role1 should be assigned", 
                       role1.equals(roles.get(0))
                       || role1.equals(roles.get(1))
                       || role1.equals(roles.get(2)));
            assertTrue("Role2 should be assigned", 
                       role2.equals(roles.get(0))
                       || role2.equals(roles.get(1))
                       || role2.equals(roles.get(2)));
   
            // Now load the user straight from database and the password must remain the same
            testUser2 = (User)m_userFactory.get(
                                 user1.getId(),
                                 CallContext.getInstance().getCurrentDomainId());
   
            assertNotNull("User should not be null", testUser2);
            assertEquals("User password is incorrect", user1.getPassword(),
                                                     testUser2.getPassword());
            assertFalse("User password in memory must be different than the one in the database",
                       testUser.getPassword().equals(testUser2.getPassword()));
         }
         finally
         {
            m_transaction.begin();
            try
            {
               // delete personal role first (we delete it explicitly in case
               // something goes wrong with deleting user :-)
               if ((testPersonalRole != null) && (testPersonalRole.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(testPersonalRole.getId());
               }
               if ((role1 != null) && (role1.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(role1.getId());
               }
               if ((role2 != null) && (role2.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(role2.getId());
               }
               if ((user1 != null) && (user1.getId() != DataObject.NEW_ID))
               {
                  m_userFactory.delete(
                                   user1.getId(),
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
       * Test for deleting user
       * 
       *  @throws Exception - an error has occurred
       */
      public void testDelete(
      ) throws Exception 
      {
         User user = null;
         Role testPersonalRole = null;
   
         try
         {
            m_transaction.begin();
            try
            {
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
            testPersonalRole = m_roleFactory.getPersonal(user.getId());
            
            m_transaction.begin();
            try
            {
               // try to delete user
               m_userControl.delete(user.getId());
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            Role selectedRole;
            User selectedUser;
            
            selectedUser = (User)m_userFactory.get(
                                    user.getId(),
                                    CallContext.getInstance().getCurrentDomainId());
            selectedRole = (Role)m_roleFactory.getPersonal(user.getId());
            assertNull("User should be deleted.", selectedUser);
            user = null;
            assertNull("Role should be deleted.", selectedRole);
            testPersonalRole = null;
         }
         finally
         {
            m_transaction.begin();
            try
            {
               // delete personal role first (we delete it explicitly in case
               // something goes wrong with deleting user :-)
               if ((testPersonalRole != null) && (testPersonalRole.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(testPersonalRole.getId());
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
       * Test for deleting multiple users
       * 
       *  @throws Exception - an error has occurred
       */
      public void testDeleteMultiple(
      ) throws Exception 
      {
         User user1   = null;
         User user2   = null;
         int  iDeleted = 0;
         Role testPersonalRole1 = null;
         Role testPersonalRole2 = null;
         
         try
         {
            m_transaction.begin();
            try
            {
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
                                 true,
                                 true,
                                 true,
                                 null,
                                 null, true
                                );
               user1 = m_userControl.create(user1, "");
            
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
                                 true,
                                 true,
                                 true,
                                 null,
                                 null, true
                                );
               user2 = m_userControl.create(user2, "");
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            String strUserIds = user1.getId() + "," + user2.getId();
            m_transaction.begin();
            try
            {
               // try to delete user
               iDeleted = m_userControl.delete(strUserIds);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            assertEquals("Two users should have been deleted.", 2, iDeleted);
            
            Role selectedRole;
            User selectedUser;
            
            selectedUser = (User)m_userFactory.get(
                                    user1.getId(),
                                    CallContext.getInstance().getCurrentDomainId());
            selectedRole = (Role)m_roleFactory.getPersonal(user1.getId());
            user1 = null;
            testPersonalRole1 = null;
            assertNull("User should be deleted.", selectedUser);
            assertNull("Role should be deleted.", selectedRole);
            selectedUser = (User)m_userFactory.get(
                                    user2.getId(),
                                    CallContext.getInstance().getCurrentDomainId());
            selectedRole = (Role)m_roleFactory.getPersonal(user2.getId());
            user2 = null;
            testPersonalRole2 = null;
            assertNull("User should be deleted.", selectedUser);
            assertNull("Role should be deleted.", selectedRole);
         }
         finally
         {
            m_transaction.begin();
            try
            {
               // delete personal role first (we delete it explicitly in case
               // something goes wrong with deleting user :-)
               if ((testPersonalRole1 != null) && (testPersonalRole1.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(testPersonalRole1.getId());
               }
               if ((testPersonalRole2 != null) && (testPersonalRole2.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(testPersonalRole2.getId());
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
       * Test for enabling Users 
       * 
       *  @throws Exception - an error has occurred
       */
      public void testUpdateEnableTrue(
      ) throws Exception 
      {
         User user1        = null;
         User user2        = null;
         User selectedUser = null;
         int  iUsersEnabled = 0;
   
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
   
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            
            Thread.sleep(1000);
            
            String strUserIds = user1.getId() + "," + user2.getId();
            m_transaction.begin();
            try
            {
               // try to enable selected users
               iUsersEnabled = m_userControl.updateEnable(strUserIds, true);
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
         }
         finally
         {
            m_transaction.begin();
            try
            {
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
       * Test for disabling users 
       * 
       *  @throws Exception - an error has occurred
       */
      public void testUpdateEnableFalse(
      ) throws Exception 
      {
         User user1        = null;
         User user2        = null;
         User selectedUser = null;
         int  iUsersEnabled = 0;
   
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
                                 true,
                                 false,
                                 false,
                                 false,
                                 null,
                                 null, true
                                );
               user2 = (User)m_userFactory.create(user2);
   
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            String strUserIds = user1.getId() + "," + user2.getId();
            m_transaction.begin();
            try
            {
               // try to enable selected users
               iUsersEnabled = m_userControl.updateEnable(strUserIds, false);
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
            assertFalse("User should be enabled.", selectedUser.isLoginEnabled());
            selectedUser = (User)m_userFactory.get(
                                    user2.getId(),
                                    CallContext.getInstance().getCurrentDomainId());
            assertFalse("User should be enabled.", selectedUser.isLoginEnabled());
         }
         finally
         {
            m_transaction.begin();
            try
            {
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
       *  @throws Exception - an error has occurred
       */
      public void testUpdateEnableFalseAllUsers(
      ) throws Exception 
      {
         User user1        = null;
         User user2        = null;
         User selectedUser = null;
         int  iUsersEnabled = 0;
   
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
                                 true,
                                 false,
                                 true,
                                 false,
                                 null,
                                 null, true
                                );
               user2 = (User)m_userFactory.create(user2);
   
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            String strUserIds = user1.getId() + "," + user2.getId() + "," +
                                CallContext.getInstance().getCurrentUserId();
            m_transaction.begin();
            try
            {
               // try to disable all users
               iUsersEnabled = m_userControl.updateEnable(strUserIds, false);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // there have to be disabled no users (no changes within the DB)
            assertEquals("Number of disabled users is incorrect", -1, iUsersEnabled);
   
            selectedUser = (User)m_userFactory.get(
                                    user1.getId(),
                                    CallContext.getInstance().getCurrentDomainId());
            assertTrue("User should be disabled.", selectedUser.isLoginEnabled());
            selectedUser = (User)m_userFactory.get(
                                    user2.getId(),
                                    CallContext.getInstance().getCurrentDomainId());
            assertTrue("User should be disabled.", selectedUser.isLoginEnabled());
         }
         finally
         {
            m_transaction.begin();
            try
            {
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
       * Test for checkForInternalEnabledUsers method
       * 
       *  @throws Exception - an error has occurred
       */
      public void testCheckForInternalEnabledUsers(
      ) throws Exception 
      {
         User user1        = null;
         User user2        = null;
         User user3        = null;
         User user4        = null;
         User user5        = null;
   
         int      iCurrentUserId = CallContext.getInstance().getCurrentUserId();
         String   strCurrentLoginName = ((User)m_userFactory.get(
                     iCurrentUserId,
                     CallContext.getInstance().getCurrentDomainId())).getLoginName();
   
         try
         {
            m_transaction.begin();
            try
            {
               // create 5 users
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
                                 true, // login enabled
                                 false,
                                 false,
                                 true, // internal user
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
                                 true, // login enabled
                                 false,
                                 false,
                                 false, // internal user
                                 null,
                                 null, true
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
                                 "test_login_name_32",
                                 "test_password_3",
                                 false, // login enabled
                                 false,
                                 false,
                                 true, // internal user
                                 null,
                                 null, true
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
                                 false, // login enabled
                                 false,
                                 false,
                                 false, // internal user
                                 null,
                                 null, true
                                );
               user4 = (User)m_userFactory.create(user4);
   
               user5 = new User(DataObject.NEW_ID,
                                 CallContext.getInstance().getCurrentDomainId(),
                                 "test_first_name_5",
                                 "test_last_name_5",
                                 "test_phone_5",
                                 "test_fax_5",
                                 "test_address_5",
                                 "test_email_5",
                                 "test_login_name_5",
                                 "test_password_5",
                                 true, // login enabled
                                 false,
                                 false,
                                 true, // internal user
                                 null,
                                 null, true
                                );
               user5 = (User)m_userFactory.create(user5);
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            
            // -------------------------------------------------------------------------
            // 1. Check for internal enabled users. All users are available for check.
            // -------------------------------------------------------------------------
            assertEquals("Incorrect check for internal enabled users", 
                         true, m_userControl.checkForInternalEnabledUsers(
                                  CallContext.getInstance().getCurrentDomainId(), 
                                  new String[] {strCurrentLoginName}));
   
            // -------------------------------------------------------------------------
            // 2. Check for internal enabled users. All users except 'user1' are available 
            //    for check.
            // -------------------------------------------------------------------------
            assertEquals("Incorrect check for internal enabled users", 
                         true, m_userControl.checkForInternalEnabledUsers(
                                  CallContext.getInstance().getCurrentDomainId(),
                                  new String[] {strCurrentLoginName, "test_login_name_1"}));
   
            // -------------------------------------------------------------------------
            // 3. Check for internal enabled users. All users except 'user1' and 'user5' 
            //    are available for check.
            // -------------------------------------------------------------------------
            assertEquals("Incorrect check for internal enabled users", 
                         false, m_userControl.checkForInternalEnabledUsers(
                                  CallContext.getInstance().getCurrentDomainId(),
                                  new String[] {strCurrentLoginName, 
                                                "test_login_name_1", 
                                                "test_login_name_5",
                                               }));
         }
         finally
         {
            m_transaction.begin();
            try
            {
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
               if ((user5 != null) && (user5.getId() != DataObject.NEW_ID))
               {
                  m_userFactory.delete(
                     user5.getId(),
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
   
      // Helper methods ///////////////////////////////////////////////////////////
      
      /**
       * Compare two users. 
       * 
       * @param testUser - user which should be tested
       * @param templateUser - user which serves as template
       */
      protected void compareUsers(
         User testUser,
         User templateUser
      )
      {
         compareUsersWithoutPassword(testUser, templateUser);
         assertEquals("User password is incorrect", 
                      templateUser.getPassword(), testUser.getPassword());
      }
   
      /**
       * Compare two users. 
       * 
       * @param testUser - user which should be tested
       * @param templateUser - user which serves as template
       */
      protected void compareUsersWithoutPassword(
         User testUser,
         User templateUser
      )
      {
         compareUsersWithoutLoginNameEmailPassword(testUser, templateUser);
         assertEquals("User login name is incorrect", 
                      templateUser.getLoginName(), testUser.getLoginName());
         assertEquals("User e-mail address is incorrect", 
                      templateUser.getEmail(), testUser.getEmail());
      }
      
      /**
       * Compare two users. 
       * 
       * @param testUser - user which should be tested
       * @param templateUser - user which serves as template
       */
      protected void compareUsersWithoutLoginNameAndEmail(
         User testUser,
         User templateUser
      )
      {
         compareUsersWithoutLoginNameEmailPassword(testUser, templateUser);
         assertEquals("User password is incorrect", 
                      templateUser.getPassword(), testUser.getPassword());
      }
      
      /**
       * Compare two users. 
       * 
       * @param testUser - user which should be tested
       * @param templateUser - user which serves as template
       */
      protected void compareUsersWithoutLoginNameEmailPassword(
         User testUser,
         User templateUser
      )
      {
         assertNotNull("User should not be null", testUser);
         assertEquals("User first name name is incorrect", 
                      templateUser.getFirstName(), testUser.getFirstName());
         assertEquals("User last name is incorrect", 
                      templateUser.getLastName(), testUser.getLastName());
         assertEquals("User phone number is incorrect", 
                      templateUser.getPhone(), testUser.getPhone());
         assertEquals("User fax number is incorrect", 
                      templateUser.getFax(), testUser.getFax());
         assertEquals("User address is incorrect", 
                      templateUser.getAddress(), testUser.getAddress());
         assertEquals("User status is incorrect", 
                      templateUser.isLoginEnabled(), testUser.isLoginEnabled());
         assertEquals("User guest access status is incorrect", 
                      templateUser.isGuestAccessEnabled(),
                      testUser.isGuestAccessEnabled());
         assertEquals("User superuser flag is incorrect", 
                      templateUser.isSuperUser(),
                      testUser.isSuperUser());
         assertEquals("User internal user flag is incorrect", 
                      templateUser.isInternalUser(), testUser.isInternalUser());
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
