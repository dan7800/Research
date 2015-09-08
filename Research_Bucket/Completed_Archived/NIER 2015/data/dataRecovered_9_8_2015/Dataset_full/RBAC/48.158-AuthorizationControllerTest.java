/*
 * Copyright (c) 2003 - 2009 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: AuthorizationControllerTest.java,v 1.15 2009/09/20 05:32:57 bastafidli Exp $
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
import org.opensubsystems.patterns.listdata.data.SimpleRule;
import org.opensubsystems.security.application.SecurityBackendModule;
import org.opensubsystems.security.data.AccessRight;
import org.opensubsystems.security.data.DomainDataDescriptor;
import org.opensubsystems.security.data.Role;
import org.opensubsystems.security.data.RoleDataDescriptor;
import org.opensubsystems.security.data.User;
import org.opensubsystems.security.data.UserDataDescriptor;
import org.opensubsystems.security.persist.AccessRightFactory;
import org.opensubsystems.security.persist.RoleFactory;
import org.opensubsystems.security.persist.db.AccessRightDatabaseFactory;
import org.opensubsystems.security.persist.db.RoleDatabaseFactory;
import org.opensubsystems.security.persist.db.SecureDatabaseTest;
import org.opensubsystems.security.util.ActionConstants;
import org.opensubsystems.security.utils.TestRoleDatabaseFactoryUtils;

/**
 * Test for AuthorizationController interface implementation class. 
 * 
 * @version $Id: AuthorizationControllerTest.java,v 1.15 2009/09/20 05:32:57 bastafidli Exp $
 * @author Peter Satury
 * @code.reviewer Miro Halas
 * @code.reviewed Initial revision
 */
public final class AuthorizationControllerTest extends Tests
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private AuthorizationControllerTest(
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
      TestSuite suite = new DatabaseTestSuite("AuthorizationControllerTest");
      suite.addTestSuite(AuthorizationControllerTestInternal.class);
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
   public static class AuthorizationControllerTestInternal extends SecureDatabaseTest
   {
      // Cached values ////////////////////////////////////////////////////////////
   
      /**
       * Controller to perform authorization.
       */
      protected AuthorizationController m_authorizeControl;
   
      /**
       * Factory to manage roles.
       */
      protected RoleDatabaseFactory m_roleFactory;
      
      /**
       * Factory to manage access rights.
       */
      protected AccessRightDatabaseFactory m_rightFactory;
   
      /**
       * Factory utilities to manage roles.
       */
      protected TestRoleDatabaseFactoryUtils m_roleFactoryUtils;

      /**
       * Data type assigned to domain data types.
       */
      protected int m_iDomainDataType;

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
      public AuthorizationControllerTestInternal(
         String strTestName
      ) throws Exception
      {
         super(strTestName);
         
         m_authorizeControl = (AuthorizationController)ControllerManager.getInstance(
                                                   AuthorizationController.class);
         m_roleFactory = (RoleDatabaseFactory)DataFactoryManager.getInstance(
                                                     RoleFactory.class);
         m_rightFactory = (AccessRightDatabaseFactory)DataFactoryManager.getInstance(
                                                     AccessRightFactory.class);
         m_roleFactoryUtils = new TestRoleDatabaseFactoryUtils();
         
         DomainDataDescriptor domainDescriptor;
         
         domainDescriptor = (DomainDataDescriptor)DataDescriptorManager.getInstance(
                                                     DomainDataDescriptor.class);
         
         m_iDomainDataType = domainDescriptor.getDataType();
         
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
       * Test of checkAccess(int, int) method.
       * 
       * @throws Exception - an error has occurred
       */
      public void testCheckAccessWithIntInt(
      ) throws Exception
      {
         User user = (User) CallContext.getInstance().getCurrentUser();
         user = new User(user.getId(),
                        user.getDomainId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getPhone(),
                        user.getFax(),
                        user.getAddress(),
                        user.getEmail(),
                        user.getLoginName(),
                        user.getPassword(),
                        user.isLoginEnabled(),
                        user.isGuestAccessEnabled(),
                        false, // set super user flag to false
                        user.isInternalUser(),
                        user.getCreationTimestamp(),
                        user.getModificationTimestamp(),
                        true
                     );
         
         CallContext.getInstance().reset();
         CallContext.getInstance().setCurrentUserAndSession(user, null);
         
         Role role1 = null;
         Role role2 = null;
         Role personalRole = null;
         AccessRight arRight1 = null;
         AccessRight arRight2 = null;
         AccessRight arRight3 = null;
         AccessRight arRight4 = null;
         AccessRight arRight5 = null;
         AccessRight arRight6 = null;
         int iReturn = 0;
   
         try
         {            
            role1 = new Role(DataObject.NEW_ID, 
                        CallContext.getInstance().getCurrentDomainId(), 
                        "test role name 1", "test role description 1", true, 
                        DataObject.NEW_ID, false, null, null, null);
   
            role2 = new Role(DataObject.NEW_ID, 
                        CallContext.getInstance().getCurrentDomainId(), 
                        "test role name 2", "test role description 2", true, 
                        DataObject.NEW_ID, false, null, null, null);
            
            
            m_transaction.begin();
            try
            {                                            
               role1 = (Role)m_roleFactory.create(role1);
               role2 = (Role)m_roleFactory.create(role2);
               personalRole = m_roleFactory.createPersonal(user);
               
               m_roleFactory.assignToUser(user.getId(),
                                          Integer.toString(role1.getId()));
               
               // role 1
               // Create user in category 1, with identifier 1
               arRight1 = new AccessRight(DataObject.NEW_ID, role1.getId(),
                           CallContext.getInstance().getCurrentDomainId(),
                           ActionConstants.RIGHT_ACTION_CREATE, 
                           m_iUserDataType, 
                           AccessRight.ACCESS_GRANTED, 
                           1, 
                           1,
                           null, null);                  
   
               // Modify user with id 3
               arRight2 = new AccessRight(DataObject.NEW_ID, role1.getId(),
                           CallContext.getInstance().getCurrentDomainId(), 
                           ActionConstants.RIGHT_ACTION_MODIFY, 
                           m_iUserDataType, 
                           AccessRight.ACCESS_GRANTED, 
                           AccessRight.NO_RIGHT_CATEGORY, 
                           1,
                           null, null);                  
                                          
               // Create role in category 4 with identifier 3
               arRight3 = new AccessRight(DataObject.NEW_ID, role1.getId(),
                           CallContext.getInstance().getCurrentDomainId(), 
                           ActionConstants.RIGHT_ACTION_CREATE, 
                           m_iRoleDataType,
                           AccessRight.ACCESS_GRANTED, 
                           4, 
                           3,
                           null, null);                  
   
               // View any user
               arRight4 = new AccessRight(DataObject.NEW_ID, role1.getId(),
                           CallContext.getInstance().getCurrentDomainId(),
                           ActionConstants.RIGHT_ACTION_VIEW, 
                           m_iUserDataType, 
                           AccessRight.ACCESS_GRANTED, 
                           AccessRight.NO_RIGHT_CATEGORY, 
                           AccessRight.NO_RIGHT_IDENTIFIER,
                           null, null);                  
   
               // role 2
               // Create any user 
               arRight5 = new AccessRight(DataObject.NEW_ID, role2.getId(),
                           CallContext.getInstance().getCurrentDomainId(),
                           ActionConstants.RIGHT_ACTION_CREATE, 
                           m_iUserDataType, 
                           AccessRight.ACCESS_GRANTED, 
                           AccessRight.NO_RIGHT_CATEGORY, 
                           AccessRight.NO_RIGHT_IDENTIFIER,
                           null, null);
               
               // personal role
               // Import any user
               arRight6 = new AccessRight(DataObject.NEW_ID, personalRole.getId(),
                           CallContext.getInstance().getCurrentDomainId(),
                           ActionConstants.RIGHT_ACTION_IMPORT, 
                           m_iUserDataType, 
                           AccessRight.ACCESS_GRANTED, 
                           AccessRight.NO_RIGHT_CATEGORY, 
                           AccessRight.NO_RIGHT_IDENTIFIER,
                           null, null);                 
   
               arRight1 = m_rightFactory.create(arRight1);
               arRight2 = m_rightFactory.create(arRight2);
               arRight3 = m_rightFactory.create(arRight3);
               arRight4 = m_rightFactory.create(arRight4);
               arRight5 = m_rightFactory.create(arRight5);
               arRight6 = m_rightFactory.create(arRight6);
               
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            
            // At this point only role 1 and personal role is assigned with following
            // rights : Create user in category 1, with identifier 1
            //          Modify user with id 3
            //          Create role in category 4 with identifier 3
            //          View any user 
            //          Import any user
            iReturn = m_authorizeControl.checkAccess(
                                 m_iUserDataType,
                                 ActionConstants.RIGHT_ACTION_CREATE);
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_DENIED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iUserDataType,
                                 ActionConstants.RIGHT_ACTION_VIEW);
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_GRANTED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iUserDataType,
                                 ActionConstants.RIGHT_ACTION_DELETE);
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_DENIED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iRoleDataType,
                                 ActionConstants.RIGHT_ACTION_CREATE);
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_DENIED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iRoleDataType,
                                 ActionConstants.RIGHT_ACTION_VIEW);
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_DENIED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iDomainDataType,
                                 ActionConstants.RIGHT_ACTION_CREATE);
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_DENIED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iUserDataType,
                                 ActionConstants.RIGHT_ACTION_IMPORT);
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_GRANTED, iReturn);
   
            m_transaction.begin();
            try
            {                                            
               m_roleFactory.assignToUser(user.getId(),
                                          Integer.toString(role2.getId()));
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            // At this point roles 1 and 2 and personal role is assigned with following
            // rights : Create user in category 1, with identifier 1
            //          Modify user with id 3
            //          Create role in category 4 with identifier 3
            //          View any user 
            //          Import any user
            //          Create any user
            
            iReturn = m_authorizeControl.checkAccess(
                                 m_iUserDataType,
                                 ActionConstants.RIGHT_ACTION_CREATE);
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_GRANTED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iUserDataType,
                                 ActionConstants.RIGHT_ACTION_VIEW);
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_GRANTED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iUserDataType,
                                 ActionConstants.RIGHT_ACTION_DELETE);
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_DENIED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iRoleDataType,
                                 ActionConstants.RIGHT_ACTION_CREATE);
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_DENIED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iRoleDataType,
                                 ActionConstants.RIGHT_ACTION_VIEW);
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_DENIED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iDomainDataType,
                                 ActionConstants.RIGHT_ACTION_CREATE);
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_DENIED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iUserDataType,
                                 ActionConstants.RIGHT_ACTION_IMPORT);
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_GRANTED, iReturn);
         }
         finally
         {
            m_transaction.begin();
            try
            {
               if ((arRight1 != null) && (arRight1.getId() != DataObject.NEW_ID))
               {
                  m_rightFactory.delete(role1.getId(), Integer.toString(arRight1.getId()));
               }
               if ((arRight2 != null) && (arRight2.getId() != DataObject.NEW_ID))
               {
                  m_rightFactory.delete(role1.getId(), Integer.toString(arRight2.getId()));
               }
               if ((arRight3 != null) && (arRight3.getId() != DataObject.NEW_ID))
               {
                  m_rightFactory.delete(role1.getId(), Integer.toString(arRight3.getId()));
               }
               if ((arRight4 != null) && (arRight4.getId() != DataObject.NEW_ID))
               {
                  m_rightFactory.delete(role1.getId(), Integer.toString(arRight4.getId()));
               }
               if ((arRight5 != null) && (arRight5.getId() != DataObject.NEW_ID))
               {
                  m_rightFactory.delete(role2.getId(), Integer.toString(arRight5.getId()));
               }
               if ((arRight6 != null) && (arRight6.getId() != DataObject.NEW_ID))
               {
                  m_rightFactory.delete(personalRole.getId(), Integer.toString(arRight6.getId()));
               }
               if ((role1 != null) && (role1.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(role1.getId());
               }
               if ((role2 != null) && (role2.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(role2.getId());
               }
               if ((personalRole != null) && (personalRole.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(personalRole.getId());
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
       * Test of checkAccess(int, int, int) method.
       * 
       * @throws Exception - an error has occurred
       */
      public void testCheckAccessWithIntIntInt(
      ) throws Exception
      {
         User user = (User) CallContext.getInstance().getCurrentUser();
         user = new User(user.getId(),
                        user.getDomainId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getPhone(),
                        user.getFax(),
                        user.getAddress(),
                        user.getEmail(),
                        user.getLoginName(),
                        user.getPassword(),
                        user.isLoginEnabled(),
                        user.isGuestAccessEnabled(),
                        false, // set super user flag to false
                        user.isInternalUser(),
                        user.getCreationTimestamp(),
                        user.getModificationTimestamp(),
                        true
                     );
         
         CallContext.getInstance().reset();
         CallContext.getInstance().setCurrentUserAndSession(user, null);
         
         Role role1 = null;
         Role role2 = null;
         Role personalRole = null;
         AccessRight arRight1 = null;
         AccessRight arRight2 = null;
         AccessRight arRight3 = null;
         AccessRight arRight4 = null;
         AccessRight arRight5 = null;
         AccessRight arRight6 = null;
   
         int iReturn = 0;
   
         try
         {            
            role1 = new Role(DataObject.NEW_ID, 
                        CallContext.getInstance().getCurrentDomainId(), 
                        "test role name 1", "test role description 1", true, 
                        DataObject.NEW_ID, false, null, null, null);
   
            role2 = new Role(DataObject.NEW_ID, 
                        CallContext.getInstance().getCurrentDomainId(), 
                        "test role name 2", "test role description 2", true, 
                        DataObject.NEW_ID, false, null, null, null);
   
            personalRole = m_roleFactory.createPersonal(user);
            
            m_transaction.begin();
            try
            {                                            
               role1 = (Role)m_roleFactory.create(role1);
               role2 = (Role)m_roleFactory.create(role2);
   
               m_roleFactory.assignToUser(user.getId(),
                                          Integer.toString(role1.getId()));
               
               // Role 1
               // Create user in category 1 with identifier 1
               arRight1 = new AccessRight(DataObject.NEW_ID, role1.getId(),
                           CallContext.getInstance().getCurrentDomainId(),
                           ActionConstants.RIGHT_ACTION_CREATE, 
                           m_iUserDataType, 
                           AccessRight.ACCESS_GRANTED, 
                           1, 
                           1,
                           null, null);                  
   
               // Create user with id 2 
               arRight2 = new AccessRight(DataObject.NEW_ID, role1.getId(),
                           CallContext.getInstance().getCurrentDomainId(), 
                           ActionConstants.RIGHT_ACTION_CREATE, 
                           m_iUserDataType, 
                           AccessRight.ACCESS_GRANTED, 
                           AccessRight.NO_RIGHT_CATEGORY, 
                           2,
                           null, null);                  
                               
               // Create any role
               arRight3 = new AccessRight(DataObject.NEW_ID, role1.getId(),
                           CallContext.getInstance().getCurrentDomainId(), 
                           ActionConstants.RIGHT_ACTION_CREATE, 
                           m_iRoleDataType,
                           AccessRight.ACCESS_GRANTED, 
                           AccessRight.NO_RIGHT_CATEGORY, 
                           AccessRight.NO_RIGHT_IDENTIFIER,
                           null, null);                  
   
               // View any user
               arRight4 = new AccessRight(DataObject.NEW_ID, role1.getId(),
                           CallContext.getInstance().getCurrentDomainId(),
                           ActionConstants.RIGHT_ACTION_VIEW, 
                           m_iUserDataType, 
                           AccessRight.ACCESS_GRANTED, 
                           AccessRight.NO_RIGHT_CATEGORY, 
                           AccessRight.NO_RIGHT_IDENTIFIER,
                           null, null);                  
   
               // Role 2
               // Delete user with id 5
               arRight5 = new AccessRight(DataObject.NEW_ID, role2.getId(),
                           CallContext.getInstance().getCurrentDomainId(),
                           ActionConstants.RIGHT_ACTION_DELETE, 
                           m_iUserDataType, 
                           AccessRight.ACCESS_GRANTED, 
                           AccessRight.NO_RIGHT_CATEGORY, 
                           5,
                           null, null);                  
   
               // personal role
               // Import any user
               arRight6 = new AccessRight(DataObject.NEW_ID, personalRole.getId(),
                           CallContext.getInstance().getCurrentDomainId(),
                           ActionConstants.RIGHT_ACTION_IMPORT, 
                           m_iUserDataType, 
                           AccessRight.ACCESS_GRANTED, 
                           AccessRight.NO_RIGHT_CATEGORY, 
                           AccessRight.NO_RIGHT_IDENTIFIER,
                           null, null);                 
   
               arRight1 = m_rightFactory.create(arRight1);
               arRight2 = m_rightFactory.create(arRight2);
               arRight3 = m_rightFactory.create(arRight3);
               arRight4 = m_rightFactory.create(arRight4);
               arRight5 = m_rightFactory.create(arRight5);
               arRight6 = m_rightFactory.create(arRight6);
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            
            // At this point only role 1 and personal role is assigned with following
            // rights : Create user in category 1, with identifier 1
            //          Create user with id 2
            //          Create any role
            //          View any user 
            //          Import any user
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iUserDataType,
                                 ActionConstants.RIGHT_ACTION_CREATE,
                                 1);
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_DENIED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iUserDataType,
                                 ActionConstants.RIGHT_ACTION_CREATE,
                                 2);
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_GRANTED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iUserDataType,
                                 ActionConstants.RIGHT_ACTION_VIEW,
                                 3);
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_GRANTED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iUserDataType,
                                 ActionConstants.RIGHT_ACTION_DELETE,
                                 5);
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_DENIED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iRoleDataType,
                                 ActionConstants.RIGHT_ACTION_DELETE,
                                 5);
            assertEquals("Access was not determined correctly", 
                        AccessRight.ACCESS_DENIED, iReturn);
   
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iDomainDataType,
                                 ActionConstants.RIGHT_ACTION_VIEW,
                                 0);
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_DENIED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iUserDataType,
                                 ActionConstants.RIGHT_ACTION_IMPORT,
                                 3);
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_GRANTED, iReturn);
   
            m_transaction.begin();
            try
            {                                            
               m_roleFactory.assignToUser(user.getId(),
                                          Integer.toString(role2.getId()));
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            // At this point only role 1 and personal role is assigned with following
            // rights : Create user in category 1, with identifier 1
            //          Create user with id 2
            //          Create any role
            //          View any user 
            //          Delete user with id 5
            //          Import any user
            iReturn = m_authorizeControl.checkAccess(
                                 m_iUserDataType,
                                 ActionConstants.RIGHT_ACTION_CREATE,
                                 1);
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_DENIED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iUserDataType,
                                 ActionConstants.RIGHT_ACTION_CREATE,
                                 2);
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_GRANTED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iUserDataType,
                                 ActionConstants.RIGHT_ACTION_VIEW,
                                 3);
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_GRANTED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iUserDataType,
                                 ActionConstants.RIGHT_ACTION_DELETE,
                                 5);
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_GRANTED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iRoleDataType,
                                 ActionConstants.RIGHT_ACTION_DELETE,
                                 5);
            assertEquals("Access was not determined correctly", 
                        AccessRight.ACCESS_DENIED, iReturn);
   
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iDomainDataType,
                                 ActionConstants.RIGHT_ACTION_VIEW,
                                 0);
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_DENIED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iUserDataType,
                                 ActionConstants.RIGHT_ACTION_IMPORT,
                                 3);
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_GRANTED, iReturn);
         }
         finally
         {
            m_transaction.begin();
            try
            {
               if ((arRight1 != null) && (arRight1.getId() != DataObject.NEW_ID))
               {
                  m_rightFactory.delete(role1.getId(), Integer.toString(arRight1.getId()));
               }
               if ((arRight2 != null) && (arRight2.getId() != DataObject.NEW_ID))
               {
                  m_rightFactory.delete(role1.getId(), Integer.toString(arRight2.getId()));
               }
               if ((arRight3 != null) && (arRight3.getId() != DataObject.NEW_ID))
               {
                  m_rightFactory.delete(role1.getId(), Integer.toString(arRight3.getId()));
               }
               if ((arRight4 != null) && (arRight4.getId() != DataObject.NEW_ID))
               {
                  m_rightFactory.delete(role1.getId(), Integer.toString(arRight4.getId()));
               }
               if ((arRight5 != null) && (arRight5.getId() != DataObject.NEW_ID))
               {
                  m_rightFactory.delete(role2.getId(), Integer.toString(arRight5.getId()));
               }
               if ((arRight6 != null) && (arRight6.getId() != DataObject.NEW_ID))
               {
                  m_rightFactory.delete(personalRole.getId(), Integer.toString(arRight6.getId()));
               }
               if ((role1 != null) && (role1.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(role1.getId());
               }
               if ((role2 != null) && (role2.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(role2.getId());
               }
               if ((personalRole != null) && (personalRole.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(personalRole.getId());
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
       * Test of checkAccess(int, int, int, int[][]) method.
       * 
       * @throws Exception - an error has occurred
       */
      public void testCheckAccessWithIntIntIntIntArray(
      ) throws Exception
      {
         User user = (User) CallContext.getInstance().getCurrentUser();
         user = new User(user.getId(),
                        user.getDomainId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getPhone(),
                        user.getFax(),
                        user.getAddress(),
                        user.getEmail(),
                        user.getLoginName(),
                        user.getPassword(),
                        user.isLoginEnabled(),
                        user.isGuestAccessEnabled(),
                        false, // set super user flag to false
                        user.isInternalUser(),
                        user.getCreationTimestamp(),
                        user.getModificationTimestamp(),
                        true
                     );
         
         CallContext.getInstance().reset();
         CallContext.getInstance().setCurrentUserAndSession(user, null);
   
         Role role1 = null;
         Role role2 = null;
         Role personalRole = null;
         AccessRight arRight1 = null;
         AccessRight arRight2 = null;
         AccessRight arRight3 = null;
         AccessRight arRight4 = null;
         AccessRight arRight5 = null;
         AccessRight arRight6 = null;
         int iReturn = 0;
   
         try
         {            
            role1 = new Role(DataObject.NEW_ID, 
                        CallContext.getInstance().getCurrentDomainId(), 
                        "test role name 1", "test role description 1", true, 
                        DataObject.NEW_ID, false, null, null, null);
   
            role2 = new Role(DataObject.NEW_ID, 
                        CallContext.getInstance().getCurrentDomainId(), 
                        "test role name 2", "test role description 2", true, 
                        DataObject.NEW_ID, false, null, null, null);
            
            personalRole = m_roleFactory.createPersonal(user);
   
            m_transaction.begin();
            try
            {                                            
               role1 = (Role)m_roleFactory.create(role1);
               role2 = (Role)m_roleFactory.create(role2);
   
               m_roleFactory.assignToUser(user.getId(),
                                          Integer.toString(role1.getId()));
               
               // Role 1
               // Create user in category 1 with identifier 1
               arRight1 = new AccessRight(DataObject.NEW_ID, role1.getId(),
                           CallContext.getInstance().getCurrentDomainId(),
                           ActionConstants.RIGHT_ACTION_CREATE, 
                           m_iUserDataType, 
                           AccessRight.ACCESS_GRANTED, 
                           1, 
                           1,
                           null, null);                  
   
               // Create user with id 2
               arRight2 = new AccessRight(DataObject.NEW_ID, role1.getId(),
                           CallContext.getInstance().getCurrentDomainId(), 
                           ActionConstants.RIGHT_ACTION_CREATE, 
                           m_iUserDataType, 
                           AccessRight.ACCESS_GRANTED, 
                           AccessRight.NO_RIGHT_CATEGORY, 
                           2,
                           null, null);                  
                      
               // Create any role
               arRight3 = new AccessRight(DataObject.NEW_ID, role1.getId(),
                           CallContext.getInstance().getCurrentDomainId(), 
                           ActionConstants.RIGHT_ACTION_CREATE, 
                           m_iRoleDataType,
                           AccessRight.ACCESS_GRANTED, 
                           AccessRight.NO_RIGHT_CATEGORY, 
                           AccessRight.NO_RIGHT_IDENTIFIER,
                           null, null);                  
   
               // View user in category 2 with identifier 2
               arRight4 = new AccessRight(DataObject.NEW_ID, role1.getId(),
                           CallContext.getInstance().getCurrentDomainId(),
                           ActionConstants.RIGHT_ACTION_VIEW, 
                           m_iUserDataType, 
                           AccessRight.ACCESS_GRANTED, 
                           2, 
                           2,
                           null, null);                  
   
               // Delete user in category 5 with id 2
               arRight5 = new AccessRight(DataObject.NEW_ID, role2.getId(),
                           CallContext.getInstance().getCurrentDomainId(),
                           ActionConstants.RIGHT_ACTION_CREATE, 
                           m_iUserDataType, 
                           AccessRight.ACCESS_GRANTED, 
                           5, 
                           2,
                           null, null);                  
   
               // personal role
               // Import any user
               arRight6 = new AccessRight(DataObject.NEW_ID, personalRole.getId(),
                           CallContext.getInstance().getCurrentDomainId(),
                           ActionConstants.RIGHT_ACTION_IMPORT, 
                           m_iUserDataType, 
                           AccessRight.ACCESS_GRANTED, 
                           AccessRight.NO_RIGHT_CATEGORY, 
                           AccessRight.NO_RIGHT_IDENTIFIER,
                           null, null);                 
   
               arRight1 = m_rightFactory.create(arRight1);
               arRight2 = m_rightFactory.create(arRight2);
               arRight3 = m_rightFactory.create(arRight3);
               arRight4 = m_rightFactory.create(arRight4);
               arRight5 = m_rightFactory.create(arRight5);
               arRight6 = m_rightFactory.create(arRight6);
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            
            // At this point only role 1 and personal role is assigned with following
            // rights : Create user in category 1, with identifier 1
            //          Create user with id 2
            //          Create any role
            //          View user in category 2 with identifier 2 
            //          Import any user
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iUserDataType,
                                 ActionConstants.RIGHT_ACTION_CREATE,
                                 5,
                                 new int[][] {{1, 1}});
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_GRANTED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iUserDataType,
                                 ActionConstants.RIGHT_ACTION_CREATE,
                                 2, null);
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_GRANTED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iUserDataType,
                                 ActionConstants.RIGHT_ACTION_CREATE,
                                 4,
                                 new int[][] {{5, 2}});
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_DENIED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iUserDataType,
                                 ActionConstants.RIGHT_ACTION_CREATE,
                                 2,
                                 new int[][] {{1, 1}});
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_GRANTED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iUserDataType,
                                 ActionConstants.RIGHT_ACTION_CREATE,
                                 5,
                                 null);
            assertEquals("Access was not determined correctly", 
                     AccessRight.ACCESS_DENIED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iUserDataType,
                                 ActionConstants.RIGHT_ACTION_VIEW,
                                 2,
                                 new int[][] {{1, 1}}
                              );
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_DENIED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iUserDataType,
                                 ActionConstants.RIGHT_ACTION_VIEW,
                                 2,
                                 new int[][] {{2, 2}});
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_GRANTED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iUserDataType,
                                 ActionConstants.RIGHT_ACTION_VIEW,
                                 AccessRight.NO_RIGHT_IDENTIFIER,
                                 new int[][] {{1, 1}});
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_DENIED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iUserDataType,
                                 ActionConstants.RIGHT_ACTION_VIEW,
                                 AccessRight.NO_RIGHT_IDENTIFIER,
                                 null);
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_DENIED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iRoleDataType,
                                 ActionConstants.RIGHT_ACTION_VIEW,
                                 AccessRight.NO_RIGHT_IDENTIFIER,
                                 null);
            assertEquals("Access was not determined correctly", 
                        AccessRight.ACCESS_DENIED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iRoleDataType,
                                 ActionConstants.RIGHT_ACTION_VIEW,
                                 8,
                                 new int[][] {{1, 1}});
            assertEquals("Access was not determined correctly", 
                     AccessRight.ACCESS_DENIED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iRoleDataType,
                                 ActionConstants.RIGHT_ACTION_CREATE,
                                 8,
                                 null);
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_GRANTED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iUserDataType,
                                 ActionConstants.RIGHT_ACTION_IMPORT);
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_GRANTED, iReturn);
   
            m_transaction.begin();
            try
            {                                            
               m_roleFactory.assignToUser(user.getId(),
                                          Integer.toString(role2.getId()));
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            // At this point roles 1 and 2 and personal role is assigned with following
            // rights : Create user in category 1, with identifier 1
            //          Create user with id 2
            //          Create user in category 5, with identifier 2
            //          Create any role
            //          View user in category 2 with identifier 2 
            //          Import any user
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iUserDataType,
                                 ActionConstants.RIGHT_ACTION_CREATE,
                                 5,
                                 new int[][] {{1, 1}});
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_GRANTED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iUserDataType,
                                 ActionConstants.RIGHT_ACTION_CREATE,
                                 2, null);
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_GRANTED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iUserDataType,
                                 ActionConstants.RIGHT_ACTION_CREATE,
                                 4,
                                 new int[][] {{5, 2}});
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_GRANTED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iUserDataType,
                                 ActionConstants.RIGHT_ACTION_CREATE,
                                 2,
                                 new int[][] {{1, 1}});
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_GRANTED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iUserDataType,
                                 ActionConstants.RIGHT_ACTION_CREATE,
                                 5,
                                 null);
            assertEquals("Access was not determined correctly", 
                     AccessRight.ACCESS_DENIED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iUserDataType,
                                 ActionConstants.RIGHT_ACTION_VIEW,
                                 2,
                                 new int[][] {{1, 1}}
                              );
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_DENIED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iUserDataType,
                                 ActionConstants.RIGHT_ACTION_VIEW,
                                 2,
                                 new int[][] {{2, 2}}
                              );
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_GRANTED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iUserDataType,
                                 ActionConstants.RIGHT_ACTION_VIEW,
                                 AccessRight.NO_RIGHT_IDENTIFIER,
                                 new int[][] {{1, 1}}
                              );
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_DENIED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iUserDataType,
                                 ActionConstants.RIGHT_ACTION_VIEW,
                                 AccessRight.NO_RIGHT_IDENTIFIER,
                                 null);
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_DENIED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iRoleDataType,
                                 ActionConstants.RIGHT_ACTION_VIEW,
                                 AccessRight.NO_RIGHT_IDENTIFIER,
                                 null);
            assertEquals("Access was not determined correctly", 
                        AccessRight.ACCESS_DENIED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iRoleDataType,
                                 ActionConstants.RIGHT_ACTION_VIEW,
                                 8,
                                 new int[][] {{1, 1}});
            assertEquals("Access was not determined correctly", 
                     AccessRight.ACCESS_DENIED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iRoleDataType,
                                 ActionConstants.RIGHT_ACTION_CREATE,
                                 8,
                                 null);
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_GRANTED, iReturn);
   
            iReturn = m_authorizeControl.checkAccess(
                                 m_iUserDataType,
                                 ActionConstants.RIGHT_ACTION_IMPORT);
            assertEquals("Access was not determined correctly", 
                         AccessRight.ACCESS_GRANTED, iReturn);
         }
         finally
         {
            m_transaction.begin();
            try
            {
               if ((arRight1 != null) && (arRight1.getId() != DataObject.NEW_ID))
               {
                  m_rightFactory.delete(role1.getId(), Integer.toString(arRight1.getId()));
               }
               if ((arRight2 != null) && (arRight2.getId() != DataObject.NEW_ID))
               {
                  m_rightFactory.delete(role1.getId(), Integer.toString(arRight2.getId()));
               }
               if ((arRight3 != null) && (arRight3.getId() != DataObject.NEW_ID))
               {
                  m_rightFactory.delete(role1.getId(), Integer.toString(arRight3.getId()));
               }
               if ((arRight4 != null) && (arRight4.getId() != DataObject.NEW_ID))
               {
                  m_rightFactory.delete(role1.getId(), Integer.toString(arRight4.getId()));
               }
               if ((arRight5 != null) && (arRight5.getId() != DataObject.NEW_ID))
               {
                  m_rightFactory.delete(role2.getId(), Integer.toString(arRight5.getId()));
               }
               if ((arRight6 != null) && (arRight6.getId() != DataObject.NEW_ID))
               {
                  m_rightFactory.delete(personalRole.getId(), Integer.toString(arRight6.getId()));
               }
               if ((role1 != null) && (role1.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(role1.getId());
               }
               if ((role2 != null) && (role2.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(role2.getId());
               }
               if ((personalRole != null) && (personalRole.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(personalRole.getId());
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
       * Test of getRightsForDataType method.
       * 
       * @throws Exception - an error has occurred
       */
      public void testGetRightsForDataType(
      ) throws Exception
      {
         Role role1 = null;
         Role role2 = null;
         Role personalRole = null;
         AccessRight arRight1 = null;
         AccessRight arRight2 = null;
         AccessRight arRight3 = null;
         AccessRight arRight4 = null;
         AccessRight arRight5 = null;
         AccessRight arRight6 = null;
         
         DataCondition condition;
         SimpleRule securityData;
         List lstCondition = null; 
         Integer intHelp;
         int     iNextCondition;
         
         User user = (User) CallContext.getInstance().getCurrentUser();
         user = new User(user.getId(),
                        user.getDomainId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getPhone(),
                        user.getFax(),
                        user.getAddress(),
                        user.getEmail(),
                        user.getLoginName(),
                        user.getPassword(),
                        user.isLoginEnabled(),
                        user.isGuestAccessEnabled(),
                        false, // set super user flag to false
                        user.isInternalUser(),
                        user.getCreationTimestamp(),
                        user.getModificationTimestamp(),
                        true
                     );
         
         CallContext.getInstance().reset();
         CallContext.getInstance().setCurrentUserAndSession(user, null);
         
         try
         {            
            role1 = new Role(DataObject.NEW_ID, 
                        CallContext.getInstance().getCurrentDomainId(), 
                        "test role name 1", "test role description 1", true, 
                        DataObject.NEW_ID, false, null, null, null);
   
            role2 = new Role(DataObject.NEW_ID, 
                        CallContext.getInstance().getCurrentDomainId(), 
                        "test role name 2", "test role description 2", true, 
                        DataObject.NEW_ID, false, null, null, null);
            
            personalRole = m_roleFactory.createPersonal(user);
            
            m_transaction.begin();
            try
            {                                            
               role1 = (Role)m_roleFactory.create(role1);
               role2 = (Role)m_roleFactory.create(role2);
               
               m_roleFactory.assignToUser(user.getId(),
                                          Integer.toString(role1.getId()));
   
               // Role 1
               // Create user in category 1 with identification 5
               arRight1 = new AccessRight(DataObject.NEW_ID, role1.getId(),
                           CallContext.getInstance().getCurrentDomainId(), 
                           ActionConstants.RIGHT_ACTION_CREATE, 
                           m_iUserDataType, 
                           AccessRight.ACCESS_GRANTED, 
                           1, 
                           5,
                           null, null);                  
   
               // Create user with id 3
               arRight2 = new AccessRight(DataObject.NEW_ID, role1.getId(),
                           CallContext.getInstance().getCurrentDomainId(), 
                           ActionConstants.RIGHT_ACTION_CREATE, 
                           m_iUserDataType, 
                           AccessRight.ACCESS_GRANTED, 
                           AccessRight.NO_RIGHT_CATEGORY, 
                           3,
                           null, null);                  
                                               
               // Create user in category 1 with identification 6
               arRight3 = new AccessRight(DataObject.NEW_ID, role1.getId(),
                           CallContext.getInstance().getCurrentDomainId(), 
                           ActionConstants.RIGHT_ACTION_CREATE, 
                           m_iUserDataType, 
                           AccessRight.ACCESS_GRANTED, 
                           1, 
                           6,
                           null, null);                  
   
               // Create any role
               arRight4 = new AccessRight(DataObject.NEW_ID, role1.getId(),
                           CallContext.getInstance().getCurrentDomainId(), 
                           ActionConstants.RIGHT_ACTION_CREATE, 
                           m_iRoleDataType, 
                           AccessRight.ACCESS_GRANTED, 
                           AccessRight.NO_RIGHT_CATEGORY, 
                           AccessRight.NO_RIGHT_IDENTIFIER,
                           null, null);                  
   
               // Role 2
               // Create any user
               arRight5 = new AccessRight(DataObject.NEW_ID, role2.getId(),
                           CallContext.getInstance().getCurrentDomainId(), 
                           ActionConstants.RIGHT_ACTION_CREATE, 
                           m_iUserDataType, 
                           AccessRight.ACCESS_GRANTED, 
                           AccessRight.NO_RIGHT_CATEGORY, 
                           AccessRight.NO_RIGHT_IDENTIFIER,
                           null, null); 
                                       
               // personal role
               // Import any user
               arRight6 = new AccessRight(DataObject.NEW_ID, personalRole.getId(),
                           CallContext.getInstance().getCurrentDomainId(),
                           ActionConstants.RIGHT_ACTION_IMPORT, 
                           m_iUserDataType, 
                           AccessRight.ACCESS_GRANTED, 
                           AccessRight.NO_RIGHT_CATEGORY, 
                           AccessRight.NO_RIGHT_IDENTIFIER,
                           null, null);                 
   
               arRight1 = m_rightFactory.create(arRight1);
               arRight2 = m_rightFactory.create(arRight2);
               arRight3 = m_rightFactory.create(arRight3);
               arRight4 = m_rightFactory.create(arRight4);
               arRight5 = m_rightFactory.create(arRight5);
               arRight6 = m_rightFactory.create(arRight6);
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            
            securityData = m_authorizeControl.getRightsForCurrentUser(
                           m_iUserDataType,
                           ActionConstants.RIGHT_ACTION_CREATE);
            assertNotNull("Returned security data should not be null", securityData);         
            assertEquals("Returned security data operant is not correct",
                         SimpleRule.LOGICAL_OR, 
                         securityData.getInterConditionOperation());
            
            lstCondition = securityData.getConditions();
            assertNotNull("Returned list of conditions is null", lstCondition);
            assertEquals("Returned list of conditions size is not correct", 
                         2, lstCondition.size());
            condition = (DataCondition) lstCondition.get(0);
            if (condition.getOperation() != DataCondition.OPERATION_IN)
            {
               condition = (DataCondition) lstCondition.get(1);
               iNextCondition = 0;
            }
            else
            {
               iNextCondition = 1;
            }
            
            assertEquals("Returned condition attribute is not correct",
                         1, condition.getAttribute());
            assertEquals("Returned condition operation is not correct",
                         DataCondition.OPERATION_IN, condition.getOperation());         
            intHelp = (Integer) ((Object[]) condition.getValue())[0];
            if (intHelp.intValue() == 5)
            {
               intHelp = (Integer) ((Object[]) condition.getValue())[1];                        
               assertEquals("Returned condition value is not correct",
                            6, intHelp.intValue());
            }
            else if (intHelp.intValue() == 6)
            {
               intHelp = (Integer) ((Object[]) condition.getValue())[1];                        
               assertEquals("Returned condition value is not correct",
                            5, intHelp.intValue());
            } 
            else
            {
               assertTrue("Returned condition value is not correct", false);
            }
            assertEquals("Returned condition value type is not correct",
                         DataCondition.VALUE_TYPE_INTEGER, condition.getValueType());
   
            condition = (DataCondition) lstCondition.get(iNextCondition);
            assertEquals("Returned condition attribute is not correct",
                         UserDataDescriptor.COL_USER_ID, condition.getAttribute());
            assertEquals("Returned condition operation is not correct",
                         DataCondition.OPERATION_EQUALS, condition.getOperation());
            intHelp = (Integer) condition.getValue();
            assertEquals("Returned condition value is not correct",
                         3, intHelp.intValue());
            assertEquals("Returned condition value type is not correct",
                         DataCondition.VALUE_TYPE_INTEGER, condition.getValueType());
   
            securityData = m_authorizeControl.getRightsForCurrentUser(
                              m_iUserDataType,
                              ActionConstants.RIGHT_ACTION_VIEW);
            assertNull("Returned security data should be null", securityData);
            
            securityData = m_authorizeControl.getRightsForCurrentUser(
                              m_iRoleDataType,
                              ActionConstants.RIGHT_ACTION_CREATE);
            assertNotNull("Returned security data should be not null", securityData);
   
            assertEquals("Returned security data operant is not correct",
                         SimpleRule.LOGICAL_OR, 
                         securityData.getInterConditionOperation());
            
            lstCondition = securityData.getConditions();
            // Since we can create any role, there will not be any conditions
            assertNull("Returned list of conditions is not null", lstCondition);
            
            // personal role
            securityData = m_authorizeControl.getRightsForCurrentUser(
                                 m_iUserDataType,
                                 ActionConstants.RIGHT_ACTION_IMPORT);
            assertNotNull("Returned security data should not be null", securityData);
            assertEquals("Returned security data operant is not correct",
                         SimpleRule.LOGICAL_OR, 
                         securityData.getInterConditionOperation());
            
            lstCondition = securityData.getConditions();
            assertNull("Returned list of conditions is not null", lstCondition);
   
            securityData = m_authorizeControl.getRightsForCurrentUser(
                                 m_iUserDataType,
                                 ActionConstants.RIGHT_ACTION_DELETE);
            assertNull("Returned security data should be null", securityData);
         }
         finally
         {
            m_transaction.begin();
            try
            {
               if ((arRight1 != null) && (arRight1.getId() != DataObject.NEW_ID))
               {
                  m_rightFactory.delete(role1.getId(), Integer.toString(arRight1.getId()));
               }
               if ((arRight2 != null) && (arRight2.getId() != DataObject.NEW_ID))
               {
                  m_rightFactory.delete(role1.getId(), Integer.toString(arRight2.getId()));
               }
               if ((arRight3 != null) && (arRight3.getId() != DataObject.NEW_ID))
               {
                  m_rightFactory.delete(role1.getId(), Integer.toString(arRight3.getId()));
               }
               if ((arRight4 != null) && (arRight4.getId() != DataObject.NEW_ID))
               {
                  m_rightFactory.delete(role1.getId(), Integer.toString(arRight4.getId()));
               }
               if ((arRight5 != null) && (arRight5.getId() != DataObject.NEW_ID))
               {
                  m_rightFactory.delete(role2.getId(), Integer.toString(arRight5.getId()));
               }
               if ((arRight6 != null) && (arRight6.getId() != DataObject.NEW_ID))
               {
                  m_rightFactory.delete(personalRole.getId(), Integer.toString(arRight6.getId()));
               }
               if ((role1 != null) && (role1.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(role1.getId());
               }
               if ((role2 != null) && (role2.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(role2.getId());
               }
               if ((personalRole != null) && (personalRole.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(personalRole.getId());
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
