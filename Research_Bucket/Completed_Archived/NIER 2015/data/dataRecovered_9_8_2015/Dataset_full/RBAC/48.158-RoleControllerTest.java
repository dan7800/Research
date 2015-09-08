/*
 * Copyright (c) 2003 - 2009 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: RoleControllerTest.java,v 1.34 2009/09/20 05:32:57 bastafidli Exp $
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
import java.util.ArrayList;
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
import org.opensubsystems.core.util.jdbc.DatabaseUtils;
import org.opensubsystems.patterns.listdata.data.SimpleRule;
import org.opensubsystems.security.application.SecurityBackendModule;
import org.opensubsystems.security.data.AccessRight;
import org.opensubsystems.security.data.Domain;
import org.opensubsystems.security.data.Role;
import org.opensubsystems.security.patterns.listdata.persist.db.SecureListDatabaseFactoryTest;
import org.opensubsystems.security.persist.AccessRightFactory;
import org.opensubsystems.security.persist.DomainFactory;
import org.opensubsystems.security.persist.RoleFactory;
import org.opensubsystems.security.persist.UserFactory;
import org.opensubsystems.security.persist.db.AccessRightDatabaseFactory;
import org.opensubsystems.security.persist.db.DomainDatabaseSchema;
import org.opensubsystems.security.persist.db.RoleDatabaseFactory;
import org.opensubsystems.security.persist.db.RoleDatabaseSchema;
import org.opensubsystems.security.persist.db.RoleListDatabaseTestUtils;
import org.opensubsystems.security.persist.db.UserDatabaseFactory;
import org.opensubsystems.security.util.RoleUtils;
import org.opensubsystems.security.utils.TestRoleDatabaseFactoryUtils;

/**
 * Test for RoleController interface implementation class. 
 * 
 * @version $Id: RoleControllerTest.java,v 1.34 2009/09/20 05:32:57 bastafidli Exp $
 * @author Peter Satury
 * @code.reviewer Miro Halas
 * @code.reviewed Initial revision
 */
public final class RoleControllerTest extends Tests
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private RoleControllerTest(
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
      TestSuite suite = new DatabaseTestSuite("RoleControllerTest");
      suite.addTestSuite(RoleControllerTestInternal.class);
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
   public static class RoleControllerTestInternal extends SecureListDatabaseFactoryTest 
   {
      // Cached values ////////////////////////////////////////////////////////////
   
      /**
       * Factory to manage domains.
       */
      protected DomainFactory m_domainFactory;

      /**
       * Controller to manage roles.
       */
      protected RoleController m_roleControl;
   
      /**
       * Factory to manage roles.
       */
      protected RoleDatabaseFactory m_roleFactory;
      
      /**
       * Factory to manage access rights.
       */
      protected AccessRightDatabaseFactory m_rightFactory;
      
      /**
       * Factory to manage users.
       */
      protected UserDatabaseFactory m_userFactory;
   
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
      public RoleControllerTestInternal(
         String strTestName
      ) throws Exception
      {
         super(strTestName, new RoleListDatabaseTestUtils());         

         m_domainFactory = (DomainFactory)DataFactoryManager.getInstance(
                                                   DomainFactory.class);
         m_roleControl = (RoleController)ControllerManager.getInstance(
                                                   RoleController.class);
         m_roleFactory = (RoleDatabaseFactory)DataFactoryManager.getInstance(
                                                   RoleFactory.class);
         m_rightFactory = (AccessRightDatabaseFactory)DataFactoryManager.getInstance(
                                                   AccessRightFactory.class);
         m_userFactory = (UserDatabaseFactory)DataFactoryManager.getInstance(
                                                   UserFactory.class);
         m_roleFactoryUtils = new TestRoleDatabaseFactoryUtils();
      }
      
      // Tests ////////////////////////////////////////////////////////////////////
   
      /**
       * Test of get methods in controller
       * 
       * @throws Exception - an error has occurred
       */
      public void testGet(
      ) throws Exception
      {
         Role role1 = null;
         Role role2 = null;
         AccessRight arRight11 = null;
         AccessRight arRight12 = null;
         AccessRight arRight21 = null;
         
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
               
               arRight11 = new AccessRight(DataObject.NEW_ID, role1.getId(), 
                                           CallContext.getInstance().getCurrentDomainId(),
                                           1, 1, 1, 1, 1, null, null);
                  
               arRight12 = new AccessRight(DataObject.NEW_ID, role1.getId(), 
                                           CallContext.getInstance().getCurrentDomainId(),
                                           2, 2, 1, 2, 2, null, null);
         
               arRight11 = m_rightFactory.create(arRight11);
               arRight12 = m_rightFactory.create(arRight12);
               
               role2 = new Role(DataObject.NEW_ID, 
                                CallContext.getInstance().getCurrentDomainId(), 
                                "testname2", "testdescription2", true, DataObject.NEW_ID, false, 
                                null, null, null);
                                   
               role2 = (Role)m_roleFactory.create(role2);
               
               arRight21 = new AccessRight(DataObject.NEW_ID, role2.getId(), 
                                           CallContext.getInstance().getCurrentDomainId(),
                                           1, 1, 1, 1, 1, null, null);
               
               arRight21 = m_rightFactory.create(arRight21);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
      
            Role testRole = (Role)m_roleControl.get(role1.getId());
            
            assertNotNull("Role is null", testRole);
            assertEquals("Role is not the same", role1, testRole);
            assertNotNull("Role do not have access rights", testRole.getAccessRights());
            assertEquals("Number of access rights is not correct", 2, 
                         testRole.getAccessRights().size());
            assertTrue("Access right is not loaded",
                       arRight11.equals(testRole.getAccessRights().get(0))
                       || arRight11.equals(testRole.getAccessRights().get(1)));
            assertTrue("Access right is not loaded",
                       arRight12.equals(testRole.getAccessRights().get(0))
                       || arRight12.equals(testRole.getAccessRights().get(1)));
         }
         finally
         {
            m_transaction.begin();
            // delete inserted records
            try
            {
               if ((arRight11 != null) && (arRight11.getId() != DataObject.NEW_ID))
               {
                  // delete access right
                  m_rightFactory.delete(role1.getId(), Integer.toString(arRight11.getId()));
               }
               if ((arRight12 != null) && (arRight12.getId() != DataObject.NEW_ID))
               {
                  // delete access right
                  m_rightFactory.delete(role1.getId(), Integer.toString(arRight12.getId()));
               }
               if ((role1 != null) && (role1.getId() != DataObject.NEW_ID))
               {
                  // delete role
                  m_roleFactory.delete(
                     role1.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((arRight21 != null) && (arRight21.getId() != DataObject.NEW_ID))
               {
                  // delete access right
                  m_rightFactory.delete(role2.getId(), Integer.toString(arRight21.getId()));
               }
               if ((role2 != null) && (role2.getId() != DataObject.NEW_ID))
               {
                  // delete role
                  m_roleFactory.delete(
                     role2.getId(),
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
       * Test of create methods in controller
       * 
       * @throws Exception - an error has occurred
       */
      public void testCreate(
      ) throws Exception
      {
         Role        role = null;
         Role        testRole = null;
         AccessRight arRight1 = null;
         AccessRight arRight2 = null;
   
         try
         {

            role = new Role(DataObject.NEW_ID, 
                     CallContext.getInstance().getCurrentDomainId(), 
                     "testname", "testdescription", true, DataObject.NEW_ID, false, 
                     null, null, null);
            arRight1 = new AccessRight(
                DataObject.NEW_ID, role.getId(), CallContext.getInstance().getCurrentDomainId(),
                1, 1, 1, 1, 1, null, null);
             
            arRight2 = new AccessRight(
                DataObject.NEW_ID, role.getId(), CallContext.getInstance().getCurrentDomainId(),
                2, 2, 1, 2, 2, null, null);
            
            List lstRights = new ArrayList();
            lstRights.add(arRight1);
            lstRights.add(arRight2);
            role.setAccessRights(lstRights);
            
            m_transaction.begin();
            try 
            {
               testRole = (Role)m_roleControl.create(role);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
      
            assertNotNull("Role is null", testRole);
            assertTrue("Role is not the same", role.isSame(testRole));
            assertNotNull("Role do not have access rights", testRole.getAccessRights());
            assertEquals("Number of access rights is not correct", 2, 
                         testRole.getAccessRights().size());
            assertTrue("Access right is not loaded",
                       arRight1.equals(testRole.getAccessRights().get(0))
                       || arRight1.equals(testRole.getAccessRights().get(1)));
            assertTrue("Access right is not loaded",
                       arRight2.equals(testRole.getAccessRights().get(0))
                       || arRight2.equals(testRole.getAccessRights().get(1)));
   
            assertNotNull("Returned Role is null", testRole);
         
            Role testRole2 = (Role)m_roleFactory.get(
                                testRole.getId(),
                                CallContext.getInstance().getCurrentDomainId());
         
            assertNotNull("Role is null", testRole2);
            assertTrue("Role is not the same", testRole.isSame(testRole2));
            
            List rights = m_rightFactory.getAllForRole(testRole.getId());
            assertNotNull("Role do not have access rights", rights);
            assertEquals("Number of access rights is not correct", 2, 
                         rights.size());
            assertTrue("Access right is not loaded",
                       arRight1.equals(rights.get(0))
                       || arRight1.equals(rights.get(1)));
            assertTrue("Access right is not loaded",
                       arRight2.equals(rights.get(0))
                       || arRight2.equals(rights.get(1)));
         }
         finally
         {
            m_transaction.begin();
            // delete inserted records
            try
            {
               if ((arRight1 != null) && (arRight1.getId() != DataObject.NEW_ID))
               {
                  // delete access right
                  m_rightFactory.delete(role.getId(), Integer.toString(arRight1.getId()));
               }
               if ((arRight2 != null) && (arRight2.getId() != DataObject.NEW_ID))
               {
                  // delete access right
                  m_rightFactory.delete(role.getId(), Integer.toString(arRight2.getId()));
               }
               if ((role != null) && (role.getId() != DataObject.NEW_ID))
               {
                  // delete role
                  m_roleFactory.delete(
                     role.getId(),
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
       * Test of save role with differences methods in controller
       * 
       * @throws Exception - an error has occurred
       */
      public void testSave(
      ) throws Exception
      {
         Role        role1 = null;
         Role        role11 = null;
         Role        role2 = null;
         Role        testRole = null;
         AccessRight arRight11 = null;
         AccessRight arRight12 = null;
         AccessRight arRight13 = null;
         AccessRight arRight14 = null;
         AccessRight arRight21 = null;
         AccessRight testRight = null;
         List        lstRights = new ArrayList();
         
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
               arRight11 = new AccessRight(
                  DataObject.NEW_ID, role1.getId(), CallContext.getInstance().getCurrentDomainId(),
                  1, 1, 1, 1, 1, null, null);
                  
               arRight12 = new AccessRight(
                  DataObject.NEW_ID, role1.getId(), CallContext.getInstance().getCurrentDomainId(),
                  2, 2, 1, 2, 2, null, null);
         
               arRight11 = m_rightFactory.create(arRight11);
               arRight12 = m_rightFactory.create(arRight12);
               
               role2 = new Role(DataObject.NEW_ID, 
                                   CallContext.getInstance().getCurrentDomainId(), 
                                   "testname2", "testdescription2", true, DataObject.NEW_ID, false, 
                                   null, null, null);
                                   
               role2 = (Role)m_roleFactory.create(role2);
               
               arRight21 = new AccessRight(
                  DataObject.NEW_ID, role2.getId(), CallContext.getInstance().getCurrentDomainId(),
                  0, 0, 1, 0, 0, null, null);
                  
               arRight21 = m_rightFactory.create(arRight21);
               
               arRight13 = new AccessRight(
                  DataObject.NEW_ID, role1.getId(), CallContext.getInstance().getCurrentDomainId(),
                  3, 3, 1, 3, 3, null, null);
                  
               arRight14 = new AccessRight(
                  DataObject.NEW_ID, role1.getId(), CallContext.getInstance().getCurrentDomainId(),
                  4, 4, 1, 4, 4, null, null);
               lstRights.add(arRight13);
               lstRights.add(arRight14);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            Thread.sleep(100);
            
            role11 = new Role(role1.getId(),
                             role1.getDomainId(),
                             "testnameXXX",
                             role1.getDescription(),
                             false,
                             role1.getUserId(),
                             role1.isUnmodifiable(),
                             role1.getAccessRights(),
                             role1.getCreationTimestamp(),
                             role1.getModificationTimestamp());
            role11.setAccessRights(lstRights);
   
            m_transaction.begin();
            try 
            {
               // THis should remove the two original rights and replace them with 
               // two new ones
               testRole = (Role)m_roleControl.save(role11);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            Thread.sleep(100);
            
            assertNotNull("Role is null", testRole);
            assertEquals("Role is not the same", role11, testRole);
            assertNotNull("Role do not have access rights", testRole.getAccessRights());
            assertEquals("Number of access rights is not correct", 2, 
                         testRole.getAccessRights().size());
            assertTrue("Access right is not loaded",
                       arRight13.equals(testRole.getAccessRights().get(0))
                       || arRight13.equals(testRole.getAccessRights().get(1))
                       || arRight13.equals(testRole.getAccessRights().get(2)));
            assertTrue("Access right is not loaded",
                       arRight14.equals(testRole.getAccessRights().get(0))
                       || arRight14.equals(testRole.getAccessRights().get(1))
                       || arRight14.equals(testRole.getAccessRights().get(2)));
            
            testRight = (AccessRight)m_rightFactory.get(
                           arRight11.getId(),
                           CallContext.getInstance().getCurrentDomainId());
            assertNull("Right should be deleted", testRight);
            arRight11 = null;
            testRight = (AccessRight)m_rightFactory.get(
                           arRight12.getId(),
                           CallContext.getInstance().getCurrentDomainId());
            assertNull("Right should be deleted", testRight);
            arRight12 = null;
   
            // Make sure the other role didn't changed
            Role testRole2 = (Role)m_roleControl.get(role2.getId());
            
            assertNotNull("Role is null", testRole2);
            assertEquals("Role is not the same", role2, testRole2);
            assertNotNull("Role do not have access rights", testRole2.getAccessRights());
            assertEquals("Number of access rights is not correct", 1, 
                         testRole2.getAccessRights().size());
            assertTrue("Access right is not loaded",
                       arRight21.equals(testRole2.getAccessRights().get(0)));
         }
         finally
         {
            m_transaction.begin();
            // delete inserted records
            try
            {
               if ((arRight14 != null) && (arRight14.getId() != DataObject.NEW_ID))
               {
                  // delete access right
                  m_rightFactory.delete(role1.getId(), Integer.toString(arRight14.getId()));
               }
               if ((arRight12 != null) && (arRight12.getId() != DataObject.NEW_ID))
               {
                  // delete access right
                  m_rightFactory.delete(role1.getId(), Integer.toString(arRight12.getId()));
               }
               if ((arRight13 != null) && (arRight13.getId() != DataObject.NEW_ID))
               {
                  // delete access right
                  m_rightFactory.delete(role1.getId(), Integer.toString(arRight13.getId()));
               }
               if ((role1 != null) && (role1.getId() != DataObject.NEW_ID))
               {
                  // delete role
                  m_roleFactoryUtils.deleteRoleCascadeManual(role1.getId());
               }
               if ((arRight21 != null) && (arRight21.getId() != DataObject.NEW_ID))
               {
                  // delete access right
                  m_rightFactory.delete(role2.getId(), Integer.toString(arRight21.getId()));
               }
               if ((role2 != null) && (role2.getId() != DataObject.NEW_ID))
               {
                  // delete role
                  m_roleFactoryUtils.deleteRoleCascadeManual(role2.getId());
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
       * Test of save with differences methods in controller
       * 
       * @throws Exception - an error has occurred
       */
      public void testSaveWithDifferences(
      ) throws Exception
      {
         Role        role1 = null;
         Role        role2 = null;
         Role        testRole = null;
         AccessRight arRight11 = null;
         AccessRight arRight12 = null;
         AccessRight arRight13 = null;
         AccessRight arRight14 = null;
         AccessRight arRight21 = null;
         AccessRight testRight = null;
         List        lstRights = new ArrayList();
         
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
               arRight11 = new AccessRight(
                  DataObject.NEW_ID, role1.getId(), CallContext.getInstance().getCurrentDomainId(),
                  1, 1, 1, 1, 1, null, null);
                  
               arRight12 = new AccessRight(
                  DataObject.NEW_ID, role1.getId(), CallContext.getInstance().getCurrentDomainId(),
                  2, 2, 1, 2, 2, null, null);
         
               arRight11 = m_rightFactory.create(arRight11);
               arRight12 = m_rightFactory.create(arRight12);
               
               role2 = new Role(DataObject.NEW_ID, 
                                   CallContext.getInstance().getCurrentDomainId(), 
                                   "testname2", "testdescription2", true, DataObject.NEW_ID, false, 
                                   null, null, null);
                                   
               role2 = (Role)m_roleFactory.create(role2);
               
               arRight21 = new AccessRight(
                  DataObject.NEW_ID, role2.getId(), CallContext.getInstance().getCurrentDomainId(),
                  0, 0, 1, 0, 0, null, null);
                  
               arRight21 = m_rightFactory.create(arRight21);
               
               arRight13 = new AccessRight(
                  DataObject.NEW_ID, role1.getId(), CallContext.getInstance().getCurrentDomainId(),
                  3, 3, 1, 3, 3, null, null);
                  
               arRight14 = new AccessRight(
                  DataObject.NEW_ID, role1.getId(), CallContext.getInstance().getCurrentDomainId(),
                  4, 4, 1, 4, 4, null, null);
               lstRights.add(arRight13);
               lstRights.add(arRight14);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            Thread.sleep(100);
            
            role1 = new Role(role1.getId(),
                              role1.getDomainId(),
                              "testnameXXX",
                              role1.getDescription(),
                              false,
                              role1.getUserId(),
                              role1.isUnmodifiable(),
                              role1.getAccessRights(),
                              role1.getCreationTimestamp(),
                              role1.getModificationTimestamp());
   
            m_transaction.begin();
            try 
            {
               // This should keep the one right, which we are not deleting
               // and add two more
               testRole = m_roleControl.save(role1, Integer.toString(arRight11.getId()),
                                             lstRights);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            Thread.sleep(100);
            
            assertNotNull("Role is null", testRole);
            assertEquals("Role is not the same", role1, testRole);
            assertNotNull("Role do not have access rights", testRole.getAccessRights());
            assertEquals("Number of access rights is not correct", 3, 
                         testRole.getAccessRights().size());
            assertTrue("Access right is not loaded",
                       arRight12.equals(testRole.getAccessRights().get(0))
                       || arRight12.equals(testRole.getAccessRights().get(1))
                       || arRight12.equals(testRole.getAccessRights().get(2)));
            assertTrue("Access right is not loaded",
                       arRight13.equals(testRole.getAccessRights().get(0))
                       || arRight13.equals(testRole.getAccessRights().get(1))
                       || arRight13.equals(testRole.getAccessRights().get(2)));
            assertTrue("Access right is not loaded",
                       arRight14.equals(testRole.getAccessRights().get(0))
                       || arRight14.equals(testRole.getAccessRights().get(1))
                       || arRight14.equals(testRole.getAccessRights().get(2)));
            testRight = (AccessRight)m_rightFactory.get(
                           arRight11.getId(),
                           CallContext.getInstance().getCurrentDomainId());
            assertNull("Right should be deleted", testRight);
            arRight11 = null;
   
            // Make sure the other role didn't changed
            Role testRole2 = (Role)m_roleControl.get(role2.getId());
            
            assertNotNull("Role is null", testRole2);
            assertEquals("Role is not the same", role2, testRole2);
            assertNotNull("Role do not have access rights", testRole2.getAccessRights());
            assertEquals("Number of access rights is not correct", 1, 
                         testRole2.getAccessRights().size());
            assertTrue("Access right is not loaded",
                       arRight21.equals(testRole2.getAccessRights().get(0)));
         }
         finally
         {
            m_transaction.begin();
            // delete inserted records
            try
            {
               if ((arRight12 != null) && (arRight12.getId() != DataObject.NEW_ID))
               {
                  // delete access right
                  m_rightFactory.delete(role1.getId(), Integer.toString(arRight12.getId()));
               }
               if ((arRight12 != null) && (arRight12.getId() != DataObject.NEW_ID))
               {
                  // delete access right
                  m_rightFactory.delete(role1.getId(), Integer.toString(arRight12.getId()));
               }
               if ((arRight13 != null) && (arRight13.getId() != DataObject.NEW_ID))
               {
                  // delete access right
                  m_rightFactory.delete(role1.getId(), Integer.toString(arRight13.getId()));
               }
               if ((role1 != null) && (role1.getId() != DataObject.NEW_ID))
               {
                  // delete role
                  m_roleFactoryUtils.deleteRoleCascadeManual(role1.getId());
               }
               if ((arRight21 != null) && (arRight21.getId() != DataObject.NEW_ID))
               {
                  // delete access right
                  m_rightFactory.delete(role2.getId(), Integer.toString(arRight21.getId()));
               }
               if ((role2 != null) && (role2.getId() != DataObject.NEW_ID))
               {
                  // delete role
                  m_roleFactoryUtils.deleteRoleCascadeManual(role2.getId());
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
       * Test of delete role methods in controller
       * 
       * @throws Exception - an error has occurred
       */
      public void testDelete(
      ) throws Exception
      {
         Role role1 = null;
         Role testRole = null;
         AccessRight arRight1 = null;
         AccessRight arRight2 = null;
         List        lstRights = new ArrayList(2);
         
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
   
               arRight1 = new AccessRight(
                  DataObject.NEW_ID, role1.getId(), CallContext.getInstance().getCurrentDomainId(),
                  1, 1, 1, 1, 1, null, null);
                  
               arRight2 = new AccessRight(
                  DataObject.NEW_ID, role1.getId(), CallContext.getInstance().getCurrentDomainId(),
                  2, 2, 1, 2, 2, null, null);
         
               arRight1 = m_rightFactory.create(arRight1);
               arRight2 = m_rightFactory.create(arRight2);
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            lstRights = m_rightFactory.getAllForRole(role1.getId());
            assertEquals("Incorrect number of access rights assigned to the role", 
                         2, lstRights.size());
            
            m_roleControl.delete(role1.getId());
            testRole = m_roleFactory.getPersonal(role1.getId());
            assertNull("Role should have been deleted", testRole);
            
            lstRights = m_rightFactory.getAllForRole(role1.getId());
   
            assertNull("There should be no access rights assigned to the role", lstRights);
   
            role1 = null;
            arRight1 = (AccessRight) m_rightFactory.get(
                          arRight1.getId(),
                          CallContext.getInstance().getCurrentDomainId());
            assertNull("There should not assigned Access Right 1 to the role", arRight1);
            arRight2 = (AccessRight) m_rightFactory.get(
                          arRight2.getId(),
                          CallContext.getInstance().getCurrentDomainId());
            assertNull("There should not assigned Access Right 2 to the role", arRight2);
         }
         finally
         {
            m_transaction.begin();
            // delete inserted records
            try
            {
               if ((arRight1 != null) && (arRight1.getId() != DataObject.NEW_ID))
               {
                  // delete access right
                  m_rightFactory.delete(role1.getId(), Integer.toString(arRight1.getId()));
               }
               if ((arRight2 != null) && (arRight2.getId() != DataObject.NEW_ID))
               {
                  // delete access right
                  m_rightFactory.delete(role1.getId(), Integer.toString(arRight2.getId()));
               }
               if ((role1 != null) && (role1.getId() != DataObject.NEW_ID))
               {
                  // delete role
                  m_roleFactoryUtils.deleteRoleCascadeManual(role1.getId());
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
       * Test of delete multiple roles methods in controller
       * 
       * @throws Exception - an error has occurred
       */
      public void testDeleteMultiple(
      ) throws Exception
      {
         Role role1 = null;
         Role role2 = null;
         Role role3 = null;
         Role testRole = null;
   
         AccessRight arRight11 = null;
         AccessRight arRight12 = null;
         AccessRight arRight21 = null;
         AccessRight arRight22 = null;
         AccessRight arRight31 = null;
         AccessRight arRight32 = null;
   
         AccessRight arRight31Test = null;
         AccessRight arRight32Test = null;
   
         List        lstRights = new ArrayList(6);
   
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
   
               // create 2 access rights for role 1
               arRight11 = new AccessRight(
                  DataObject.NEW_ID, role1.getId(), CallContext.getInstance().getCurrentDomainId(),
                  1, 1, 1, 1, 1, null, null);
                  
               arRight12 = new AccessRight(
                  DataObject.NEW_ID, role1.getId(), CallContext.getInstance().getCurrentDomainId(),
                  2, 2, 1, 2, 2, null, null);
         
               arRight11 = m_rightFactory.create(arRight11);
               arRight12 = m_rightFactory.create(arRight12);
   
               // create 2 access rights for role 2
               arRight21 = new AccessRight(
                  DataObject.NEW_ID, role2.getId(), CallContext.getInstance().getCurrentDomainId(),
                  1, 1, 1, 1, 1, null, null);
                  
               arRight22 = new AccessRight(
                  DataObject.NEW_ID, role2.getId(), CallContext.getInstance().getCurrentDomainId(),
                  2, 2, 1, 2, 2, null, null);
         
               arRight21 = m_rightFactory.create(arRight21);
               arRight22 = m_rightFactory.create(arRight22);
   
               // create 2 access rights for role 3
               arRight31 = new AccessRight(
                  DataObject.NEW_ID, role3.getId(), CallContext.getInstance().getCurrentDomainId(),
                  1, 1, 1, 1, 1, null, null);
                  
               arRight32 = new AccessRight(
                  DataObject.NEW_ID, role3.getId(), CallContext.getInstance().getCurrentDomainId(),
                  2, 2, 1, 2, 2, null, null);
         
               arRight31 = m_rightFactory.create(arRight31);
               arRight32 = m_rightFactory.create(arRight32);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            lstRights = m_rightFactory.getAllForRole(role1.getId());
            assertEquals("Incorrect number of access rights assigned to the role 1", 
                         2, lstRights.size());
   
            lstRights = m_rightFactory.getAllForRole(role2.getId());
            assertEquals("Incorrect number of access rights assigned to the role 2", 
                         2, lstRights.size());
   
            lstRights = m_rightFactory.getAllForRole(role3.getId());
            assertEquals("Incorrect number of access rights assigned to the role 3", 
                         2, lstRights.size());
   
            int iDeleted = m_roleControl.delete(Integer.toString(role1.getId()) + "," 
                                                +  Integer.toString(role2.getId()) + ",987654321");
            assertEquals("Number of deleted Roles is incorect", 2, iDeleted);
            testRole = m_roleFactory.getPersonal(role1.getId());
            assertNull("Role should have been deleted", testRole);
            testRole = m_roleFactory.getPersonal(role2.getId());
            assertNull("Role should have been deleted", testRole);
   
            lstRights = m_rightFactory.getAllForRole(role1.getId());
            assertNull("There should be no access rights assigned to the role 1", lstRights);
   
            lstRights = m_rightFactory.getAllForRole(role2.getId());
            assertNull("There should be no access rights assigned to the role 2", lstRights);
   
            role1 = null;
            role2 = null;
   
            lstRights = m_rightFactory.getAllForRole(role3.getId());
            assertEquals("Incorrect number of access rights assigned to the role 3", 
                         2, lstRights.size());
   
            arRight11 = (AccessRight) m_rightFactory.get(
                           arRight11.getId(),
                           CallContext.getInstance().getCurrentDomainId());
            assertNull("There should not assigned Access Right 11 to the role", arRight11);
            arRight12 = (AccessRight) m_rightFactory.get(
                           arRight12.getId(),
                           CallContext.getInstance().getCurrentDomainId());
            assertNull("There should not assigned Access Right 12 to the role", arRight12);
   
            arRight21 = (AccessRight) m_rightFactory.get(
                           arRight21.getId(),
                           CallContext.getInstance().getCurrentDomainId());
            assertNull("There should not assigned Access Right 21 to the role", arRight21);
            arRight22 = (AccessRight) m_rightFactory.get(
                           arRight22.getId(),
                           CallContext.getInstance().getCurrentDomainId());
            assertNull("There should not assigned Access Right 22 to the role", arRight22);
   
            arRight31Test = (AccessRight) m_rightFactory.get(
                               arRight31.getId(),
                               CallContext.getInstance().getCurrentDomainId());
            assertTrue("Incorrect access right 31 assigned to the role 3", 
                         arRight31.isSame(arRight31Test));
            arRight32Test = (AccessRight) m_rightFactory.get(
                               arRight32.getId(),
                               CallContext.getInstance().getCurrentDomainId());
            assertTrue("Incorrect access right 32 assigned to the role 3", 
                         arRight32.isSame(arRight32Test));
         }
         finally
         {
            m_transaction.begin();
            // delete inserted records
            try
            {
               if ((arRight11 != null) && (arRight11.getId() != DataObject.NEW_ID))
               {
                  // delete access right
                  m_rightFactory.delete(role1.getId(), Integer.toString(arRight11.getId()));
               }
               if ((arRight12 != null) && (arRight12.getId() != DataObject.NEW_ID))
               {
                  // delete access right
                  m_rightFactory.delete(role1.getId(), Integer.toString(arRight12.getId()));
               }
               if ((arRight21 != null) && (arRight21.getId() != DataObject.NEW_ID))
               {
                  // delete access right
                  m_rightFactory.delete(role2.getId(), Integer.toString(arRight21.getId()));
               }
               if ((arRight22 != null) && (arRight22.getId() != DataObject.NEW_ID))
               {
                  // delete access right
                  m_rightFactory.delete(role2.getId(), Integer.toString(arRight22.getId()));
               }
               if ((arRight31 != null) && (arRight31.getId() != DataObject.NEW_ID))
               {
                  // delete access right
                  m_rightFactory.delete(role3.getId(), Integer.toString(arRight31.getId()));
               }
               if ((arRight32 != null) && (arRight32.getId() != DataObject.NEW_ID))
               {
                  // delete access right
                  m_rightFactory.delete(role3.getId(), Integer.toString(arRight32.getId()));
               }
               if ((role1 != null) && (role1.getId() != DataObject.NEW_ID))
               {
                  // delete role
                  m_roleFactoryUtils.deleteRoleCascadeManual(role1.getId());
               }
               if ((role2 != null) && (role2.getId() != DataObject.NEW_ID))
               {
                  // delete role
                  m_roleFactoryUtils.deleteRoleCascadeManual(role2.getId());
               }
               if ((role3 != null) && (role3.getId() != DataObject.NEW_ID))
               {
                  // delete role
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
       * Test of updateEnable methods in controller
       * 
       * @throws Exception - an error has occurred
       */
      public void testUpdateTrue(
      ) throws Exception
      {
         Role role1 = null;
         Role role2 = null;
         Role role3 = null;
         Role testRole = null;
         
         try
         {      
            m_transaction.begin();      
            try
            {
               role1 = new Role(DataObject.NEW_ID, 
                                   CallContext.getInstance().getCurrentDomainId(), 
                                   "testname1", "testdescription1", true, 
                                   DataObject.NEW_ID, false, null, null, null);
               role1 = (Role)m_roleFactory.create(role1);
         
               role2 = new Role(DataObject.NEW_ID, 
                                   CallContext.getInstance().getCurrentDomainId(), 
                                   "testname2", "testdescription2", false, 
                                   DataObject.NEW_ID, false, null, null, null);
               role2 = (Role)m_roleFactory.create(role2);
         
               role3 = new Role(DataObject.NEW_ID, 
                                   CallContext.getInstance().getCurrentDomainId(), 
                                   "testname3", "testdescription3", false, 
                                   DataObject.NEW_ID, false, null, null, null);
               role3 = (Role)m_roleFactory.create(role3);
               
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            
            Thread.sleep(1000);
            
            int iEnabled;
            
            m_transaction.begin();      
            try
            {
               iEnabled = m_roleControl.updateEnable(Integer.toString(role1.getId()) 
                              + "," + Integer.toString(role2.getId()) 
                              + "," + Integer.toString(role3.getId()) + ", 987654321", true);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            
            assertEquals("Number of enabled Roles is incorect", 3, iEnabled);
            testRole = (Role)m_roleFactory.get(
                                role1.getId(),
                                CallContext.getInstance().getCurrentDomainId());
            assertNotNull("Role should still exist", testRole);
            assertTrue("Role should be enabled", testRole.isEnabled());
            assertTrue("Modified date should be changed",
                       testRole.getModificationTimestamp().getTime()
                       != role1.getModificationTimestamp().getTime());
            testRole = (Role)m_roleFactory.get(
                                role2.getId(),
                                CallContext.getInstance().getCurrentDomainId());
            assertNotNull("Role should still exist", testRole);
            assertTrue("Role should be enabled", testRole.isEnabled());
            assertTrue("Modified date should be changed",
                       testRole.getModificationTimestamp().getTime()
                       != role2.getModificationTimestamp().getTime());
            testRole = (Role)m_roleFactory.get(
                                role3.getId(),
                                CallContext.getInstance().getCurrentDomainId());
            assertNotNull("Role should still exist", testRole);
            assertTrue("Role should be enabled", testRole.isEnabled());
            assertTrue("Modified date should be changed",
                       testRole.getModificationTimestamp().getTime()
                       != role3.getModificationTimestamp().getTime());         
         }
         finally
         {
            m_transaction.begin();
            // delete inserted records
            try
            {
               if ((role1 != null) && (role1.getId() != DataObject.NEW_ID))
               {
                  // delete role
                  m_roleFactoryUtils.deleteRoleCascadeManual(role1.getId());
               }
               if ((role2 != null) && (role2.getId() != DataObject.NEW_ID))
               {
                  // delete role
                  m_roleFactoryUtils.deleteRoleCascadeManual(role2.getId());
               }
               if ((role3 != null) && (role3.getId() != DataObject.NEW_ID))
               {
                  // delete role
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
       * Test of updateEnable methods in controller
       * 
       * @throws Exception - an error has occurred
       */
      public void testUpdateEnableFalse(
      ) throws Exception
      {
         Role role1 = null;
         Role role2 = null;
         Role role3 = null;
         Role testRole = null;
         
         try
         {      
            m_transaction.begin();      
            try
            {
               role1 = new Role(DataObject.NEW_ID, 
                                CallContext.getInstance().getCurrentDomainId(), 
                                "testname1", "testdescription1", false, DataObject.NEW_ID, false, 
                                null, null, null);
               role1 = (Role)m_roleFactory.create(role1);
         
               role2 = new Role(DataObject.NEW_ID, 
                                CallContext.getInstance().getCurrentDomainId(), 
                                "testname2", "testdescription2", false, DataObject.NEW_ID, false, 
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
            
            Thread.sleep(1000);
            
            int iDisabled;
            
            m_transaction.begin();      
            try
            {
               iDisabled = m_roleControl.updateEnable(Integer.toString(role1.getId()) 
                              + "," + Integer.toString(role2.getId()) 
                              + "," + Integer.toString(role3.getId()) + ", 987654321", false);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            
            assertEquals("Number of enabled Roles is incorect", 3, iDisabled);
            testRole = (Role)m_roleFactory.get(
                                role1.getId(),
                                CallContext.getInstance().getCurrentDomainId());
            assertNotNull("Role should still exist", testRole);
            assertFalse("Role should be disabled", testRole.isEnabled());
            assertTrue("Modified date should be changed",
                       testRole.getModificationTimestamp().getTime()
                       != role1.getModificationTimestamp().getTime());
            testRole = (Role)m_roleFactory.get(
                                role2.getId(),
                                CallContext.getInstance().getCurrentDomainId());
            assertNotNull("Role should still exist", testRole);
            assertFalse("Role should be disabled", testRole.isEnabled());
            assertTrue("Modified date should be changed",
                       testRole.getModificationTimestamp().getTime()
                       != role2.getModificationTimestamp().getTime());
            testRole = (Role)m_roleFactory.get(
                                role3.getId(),
                                CallContext.getInstance().getCurrentDomainId());
            assertNotNull("Role should still exist", testRole);
            assertFalse("Role should be disabled", testRole.isEnabled());
            assertTrue("Modified date should be changed",
                       testRole.getModificationTimestamp().getTime()
                       != role3.getModificationTimestamp().getTime());         
         }
         finally
         {
            m_transaction.begin();
            // delete inserted records
            try
            {
               if ((role1 != null) && (role1.getId() != DataObject.NEW_ID))
               {
                  // delete role
                  m_roleFactoryUtils.deleteRoleCascadeManual(role1.getId());
               }
               if ((role2 != null) && (role2.getId() != DataObject.NEW_ID))
               {
                  // delete role
                  m_roleFactoryUtils.deleteRoleCascadeManual(role2.getId());
               }
               if ((role3 != null) && (role3.getId() != DataObject.NEW_ID))
               {
                  // delete role
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
       * Test of get multiple roles methods in controller
       * 
       * @throws Exception - an error has occurred
       */
      public void testGetList(
      ) throws Exception
      {
         Role role1 = null;
         Role role2 = null;
         Role role3 = null;
         Role role4 = null;
         
         try
         {
            m_transaction.begin();
            try
            {
               role1 = new Role(DataObject.NEW_ID, 
                                   CallContext.getInstance().getCurrentDomainId(), 
                                   "testname1", "testdescription1", true, 
                                   DataObject.NEW_ID, false, null, null, null);
               role1 = (Role)m_roleFactory.create(role1);
         
               role2 = new Role(DataObject.NEW_ID, 
                                   CallContext.getInstance().getCurrentDomainId(), 
                                   "testname2", "testdescription2", false, 
                                   DataObject.NEW_ID, false, null, null, null);
               role2 = (Role)m_roleFactory.create(role2);
         
               role3 = new Role(DataObject.NEW_ID, 
                                   CallContext.getInstance().getCurrentDomainId(), 
                                   "testname3", "testdescription3", false, 
                                   DataObject.NEW_ID, false, null, null, null);
               role3 = (Role)m_roleFactory.create(role3);
               
               role4 = new Role(DataObject.NEW_ID, 
                                   CallContext.getInstance().getCurrentDomainId(), 
                                   "testname4", "testdescription4", true, 
                                   DataObject.NEW_ID, false, null, null, null);
               role4 = (Role)m_roleFactory.create(role4);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            
            List lstRoles = m_roleControl.get(role1.getId() + "," + role3.getId() + ","
                                              + role4.getId() + ","
                                              // not existing ID
                                              + (role1.getId() + role2.getId() 
                                                 + role3.getId() + role4.getId()));
            
            assertNotNull("List of returned Roles should not be null", lstRoles);
            assertEquals("Size of returned list of Roles is not correct", 3, lstRoles.size());
            
            assertTrue("Role should exist",
                       role1.equals(lstRoles.get(0))
                       || role1.equals(lstRoles.get(1))
                       || role1.equals(lstRoles.get(2)));
            assertTrue("Role should exist",
                       role3.equals(lstRoles.get(0))
                       || role3.equals(lstRoles.get(1))
                       || role3.equals(lstRoles.get(2)));
            assertTrue("Role should exist",
                       role4.equals(lstRoles.get(0))
                       || role4.equals(lstRoles.get(1))
                       || role4.equals(lstRoles.get(2)));
         }
         finally
         {
            m_transaction.begin();
            // delete inserted records
            try
            {
               if ((role1 != null) && (role1.getId() != DataObject.NEW_ID))
               {
                  // delete role
                  m_roleFactoryUtils.deleteRoleCascadeManual(role1.getId());
               }
               if ((role2 != null) && (role2.getId() != DataObject.NEW_ID))
               {
                  // delete role
                  m_roleFactoryUtils.deleteRoleCascadeManual(role2.getId());
               }
               if ((role3 != null) && (role3.getId() != DataObject.NEW_ID))
               {
                  // delete role
                  m_roleFactoryUtils.deleteRoleCascadeManual(role3.getId());
               }
               if ((role4 != null) && (role4.getId() != DataObject.NEW_ID))
               {
                  // delete role
                  m_roleFactoryUtils.deleteRoleCascadeManual(role4.getId());
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
       * Test of getOrderedLists methods in controller
       * 
       * @throws Exception - an error has occurred
       */
      public void testGetOrderedLists(
      ) throws Exception
      {
         Role role1 = null;
         Role role2 = null;
         Role role3 = null;
         Role role4 = null;
   
         try
         {
            m_transaction.begin();
            try
            {
               role1 = new Role(DataObject.NEW_ID, 
                                   CallContext.getInstance().getCurrentDomainId(), 
                                   "testname1", "testdescription1", true, 
                                   DataObject.NEW_ID, false, null, null, null);
               role1 = (Role)m_roleFactory.create(role1);
         
               role2 = new Role(DataObject.NEW_ID, 
                                   CallContext.getInstance().getCurrentDomainId(), 
                                   "testname2", "testdescription2", false, 
                                   DataObject.NEW_ID, false, null, null, null);
               role2 = (Role)m_roleFactory.create(role2);
         
               role3 = new Role(DataObject.NEW_ID, 
                                   CallContext.getInstance().getCurrentDomainId(), 
                                   "testname3", "testdescription3", false, 
                                   DataObject.NEW_ID, false, null, null, null);
               role3 = (Role)m_roleFactory.create(role3);
               
               role4 = new Role(DataObject.NEW_ID, 
                                   CallContext.getInstance().getCurrentDomainId(), 
                                   "testname4", "testdescription4", true, 
                                   DataObject.NEW_ID, false, null, null, null);
               role4 = (Role)m_roleFactory.create(role4);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            
            List[] lstRoles = m_roleControl.getOrderedLists(
                     role4.getId() + "," + role3.getId(),
                     role1.getId() + "," + role3.getId() + "," + role2.getId(),            
                     "");
            
            assertNotNull("Array of lists of returned roles should not be null", lstRoles);
            assertEquals("Size of returned array of lists of roles is not correct", 
                         3, lstRoles.length);
   
            List lstHelp;
            lstHelp = lstRoles[0];
   
            assertNotNull("List 1 of returned roles should not be null", lstHelp);
            assertEquals("Size of list 1 of roles is not correct", 2, lstHelp.size());
            assertEquals("Role is not the same", role4, lstHelp.get(0));
            assertEquals("Role is not the same", role3, lstHelp.get(1));
   
            lstHelp = lstRoles[1];
            assertNotNull("List 2 of returned roles should not be null", lstHelp);
            assertEquals("Size of list 2 of roles is not correct", 3, lstHelp.size());
            assertEquals("Role is not the same", role1, lstHelp.get(0));
            assertEquals("Role is not the same", role3, lstHelp.get(1));
            assertEquals("Role is not the same", role2, lstHelp.get(2));
   
            lstHelp = lstRoles[2];
   
            assertNull("List 3 of returned roles should be null", lstHelp);
   
         }
         finally
         {
            m_transaction.begin();
            // delete inserted records
            try
            {
               if ((role1 != null) && (role1.getId() != DataObject.NEW_ID))
               {
                  // delete role
                  m_roleFactoryUtils.deleteRoleCascadeManual(role1.getId());
               }
               if ((role2 != null) && (role2.getId() != DataObject.NEW_ID))
               {
                  // delete role
                  m_roleFactoryUtils.deleteRoleCascadeManual(role2.getId());
               }
               if ((role3 != null) && (role3.getId() != DataObject.NEW_ID))
               {
                  // delete role
                  m_roleFactoryUtils.deleteRoleCascadeManual(role3.getId());
               }
               if ((role4 != null) && (role4.getId() != DataObject.NEW_ID))
               {
                  // delete role
                  m_roleFactoryUtils.deleteRoleCascadeManual(role4.getId());
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
       * Test of createSelfRegistrationRole methods in controller
       * 
       * @throws Exception - an error has occurred
       */
      public void testCreateSelfRegistrationRole(
      ) throws Exception
      {
         Domain domain1   = null;
         Domain domain2   = null;
         Role   testRole1 = null;
         Role   testRole2 = null;
         
         try
         {
            m_transaction.begin();
            try
            {
               // try to create 2 domains         
               domain1 = new Domain(DataObject.NEW_ID,
                                    "test_domain_name1",
                                    "test_domain_description1",
                                    true, true, false, false, false, false, false,
                                    "test_phone1", "test_domain_address1",
                                    null, null, null
                                   );
               domain1 = (Domain)m_domainFactory.create(domain1);

               domain2 = new Domain(DataObject.NEW_ID,
                                    "test_domain_name2",
                                    "test_domain_description2",
                                    true, true, false, false, false, false, false,
                                    "test_phone2", "test_domain_address2",
                                    null, null, null
                                   );
               domain2 = (Domain)m_domainFactory.create(domain2);

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
               // try to create self registration role for domain 1
               testRole1 = (Role)m_roleControl.createSelfRegistrationRole(domain1.getId());
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
      
            assertNotNull("Self registration role 1 should not be null", testRole1);
            assertNotNull("Role 1 do not have access rights", testRole1.getAccessRights());
            assertEquals("Role 1 name is incorrect", 
                         RoleUtils.SELFREG_ROLE_NAME, testRole1.getName());
            assertEquals("Role 1 description is incorrect", 
                         "Default role for self registered user", testRole1.getDescription());
            assertEquals("Role 1 domain ID is incorrect", 
                         domain1.getId(), testRole1.getDomainId());
            
            m_transaction.begin();
            try 
            {
               // try to create self registration role for domain 2
               testRole2 = (Role)m_roleControl.createSelfRegistrationRole(domain2.getId());
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
      
            assertNotNull("Self registration role 2 should not be null", testRole2);
            assertNotNull("Role 2 do not have access rights", testRole2.getAccessRights());
            assertEquals("Role 2 name is incorrect", 
                         RoleUtils.SELFREG_ROLE_NAME, testRole2.getName());
            assertEquals("Role 2 description is incorrect", 
                         "Default role for self registered user", testRole2.getDescription());
            assertEquals("Role 2 domain ID is incorrect", 
                         domain2.getId(), testRole2.getDomainId());
         }
         finally
         {
            m_transaction.begin();
            // delete inserted records
            try
            {
               PreparedStatement statement = null;
               String            strQuery;
               try
               {
                  // delete mapped data
                  strQuery = "delete from " + DomainDatabaseSchema.DOMAIN_TABLE_NAME + "_ROLE_MAP where DOMAIN_ID in (?, ?)" +
                             " and ROLE_ID in (?, ?)";
                  statement = m_connection.prepareStatement(strQuery);
                  statement.setInt(1, domain1.getId());
                  statement.setInt(2, domain2.getId());
                  statement.setInt(3, testRole1.getId());
                  statement.setInt(4, testRole2.getId());
                  statement.executeUpdate();
               }
               finally
               {
                  DatabaseUtils.closeStatement(statement);
               }
               try
               {
                  // delete access rights
                  strQuery = "delete from " + RoleDatabaseSchema.ACCESSRIGHT_TABLE_NAME + " where DOMAIN_ID in (?, ?)" +
                             " and ROLE_ID in (?, ?)";
                  statement = m_connection.prepareStatement(strQuery);
                  statement.setInt(1, domain1.getId());
                  statement.setInt(2, domain2.getId());
                  statement.setInt(3, testRole1.getId());
                  statement.setInt(4, testRole2.getId());
                  statement.executeUpdate();
               }
               finally
               {
                  DatabaseUtils.closeStatement(statement);
               }
               try
               {
                  // delete roles belonging to specified domains
                  strQuery = "delete from " + RoleDatabaseSchema.ROLE_TABLE_NAME + " where DOMAIN_ID in (?, ?)";
                  statement = m_connection.prepareStatement(strQuery);
                  statement.setInt(1, domain1.getId());
                  statement.setInt(2, domain2.getId());
                  statement.executeUpdate();
               }
               finally
               {
                  DatabaseUtils.closeStatement(statement);
               }
               if ((domain1 != null) && (domain1.getId() != DataObject.NEW_ID))
               {
                  // delete domain 1
                  m_domainFactory.delete(
                     domain1.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((domain2 != null) && (domain2.getId() != DataObject.NEW_ID))
               {
                  // delete domain 2
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
       * Test for copyRoles method in controller
       * 
       * @throws Exception - an error has occurred
       */
      public void testCopyRoles(
      ) throws Exception
      {
         Domain domain = null; 
         Role role1 = null;
         Role role2 = null;
         Role newRole1 = null;
         Role newRole2 = null;
         Role testRole = null;
         AccessRight arRight11 = null;
         AccessRight arRight12 = null;
         AccessRight arRight21 = null;
         
         List lstTestRoles = null;
         List lstNewRoles = null;
         
         try
         {
            m_transaction.begin();
            try 
            {
               domain = new Domain(DataObject.NEW_ID,
                                    "test_domain_name1",
                                    "test_domain_description1",
                                    true, true, false, false, false, false, false,
                                    "test_phone1", "test_domain_address1",
                                    null, null, null
                                   );
               domain = (Domain)m_domainFactory.create(domain);

               role1 = new Role(DataObject.NEW_ID, 
                                CallContext.getInstance().getCurrentDomainId(), 
                                "testname1", "testdescription1", true, DataObject.NEW_ID, false, 
                                null, null, null);
               role1 = (Role)m_roleFactory.create(role1);
               
               arRight11 = new AccessRight(DataObject.NEW_ID, role1.getId(), 
                                           CallContext.getInstance().getCurrentDomainId(),
                                           1, 1, 1, 1, 1, null, null);
               arRight11 = m_rightFactory.create(arRight11);
                  
               arRight12 = new AccessRight(DataObject.NEW_ID, role1.getId(), 
                                           CallContext.getInstance().getCurrentDomainId(),
                                           2, 2, 1, 2, 2, null, null);
               arRight12 = m_rightFactory.create(arRight12);
               
               role2 = new Role(DataObject.NEW_ID, 
                                CallContext.getInstance().getCurrentDomainId(), 
                                "testname2", "testdescription2", true, DataObject.NEW_ID, false, 
                                null, null, null);
               role2 = (Role)m_roleFactory.create(role2);
               
               arRight21 = new AccessRight(DataObject.NEW_ID, role2.getId(), 
                                           CallContext.getInstance().getCurrentDomainId(),
                                           1, 1, 1, 1, 1, null, null);
               arRight21 = m_rightFactory.create(arRight21);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }

            String strRoleIDs = role1.getId() + "," + role2.getId();
            
            m_transaction.begin();
            try 
            {
               // try to copy roles with belonging access rights to new domain
               lstTestRoles = m_roleControl.copyRoles(domain.getId(), strRoleIDs);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            
            assertNotNull("List of copied roles should not be null", lstTestRoles);
            assertEquals("List of copied roles has incorrect size", 2, lstTestRoles.size());
            
            // get all roles for new domain
            lstNewRoles = m_roleFactory.getAllForDomain(domain.getId(), SimpleRule.ALL_DATA);
            assertNotNull("List of new roles should not be null", lstNewRoles);
            assertEquals("List of new roles has incorrect size", 2, lstNewRoles.size());
            
            newRole1 = (Role)lstNewRoles.get(0);
            newRole2 = (Role)lstNewRoles.get(1);
            
            // get and equal 1st role from the list
            testRole = (Role)lstTestRoles.get(0);
            assertNotNull("Role is null", testRole);
            assertTrue("Role should be assigned", 
                       testRole.equals(newRole1) || testRole.equals(newRole2));

            // get and equal 2nd role from the list
            testRole = (Role)lstTestRoles.get(1);
            assertNotNull("Role is null", testRole);
            assertTrue("Role should be assigned", 
                       testRole.equals(newRole1) || testRole.equals(newRole2));

            assertNull("Role shoul not have access rights", testRole.getAccessRights());
         }
         finally
         {
            m_transaction.begin();
            // delete inserted records
            try
            {
               if ((arRight11 != null) && (arRight11.getId() != DataObject.NEW_ID))
               {
                  // delete access right
                  m_rightFactory.delete(role1.getId(), Integer.toString(arRight11.getId()));
               }
               if ((arRight12 != null) && (arRight12.getId() != DataObject.NEW_ID))
               {
                  // delete access right
                  m_rightFactory.delete(role1.getId(), Integer.toString(arRight12.getId()));
               }
               if ((role1 != null) && (role1.getId() != DataObject.NEW_ID))
               {
                  // delete role
                  m_roleFactory.delete(
                     role1.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((arRight21 != null) && (arRight21.getId() != DataObject.NEW_ID))
               {
                  // delete access right
                  m_rightFactory.delete(role2.getId(), Integer.toString(arRight21.getId()));
               }
               if ((role2 != null) && (role2.getId() != DataObject.NEW_ID))
               {
                  // delete role
                  m_roleFactory.delete(
                     role2.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((newRole1 != null) && (newRole1.getId() != DataObject.NEW_ID))
               {
                  // delete role
                  m_roleFactoryUtils.deleteRoleCascadeManual(newRole1.getId(), domain.getId()); 
               }
               if ((newRole2 != null) && (newRole2.getId() != DataObject.NEW_ID))
               {
                  // delete role
                  m_roleFactoryUtils.deleteRoleCascadeManual(newRole2.getId(), domain.getId()); 
               }
               if ((domain != null) && (domain.getId() != DataObject.NEW_ID))
               {
                  // delete domain
                  m_domainFactory.delete(
                     domain.getId(),
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
       * test of getAllRolesInDomain method
       * 
       * @throws Exception - an error has occurred
       */
      public void testGetAllRolesInDomain(
      ) throws Exception
      {
         Domain domain1 = null;
         Domain domain2 = null;
         Role role1 = null;
         Role role2 = null;
         Role role3 = null;
         
         try
         {
            m_transaction.begin();      
            try
            {
               // try to create 2 domains         
               domain1 = new Domain(DataObject.NEW_ID,
                                    "test_domain_name1",
                                    "test_domain_description1",
                                    true, true, false, false, false, false, false,
                                    "test_phone1", "test_domain_address1",
                                    null, null, null
                                   );
               domain1 = (Domain)m_domainFactory.create(domain1);

               domain2 = new Domain(DataObject.NEW_ID,
                                    "test_domain_name2",
                                    "test_domain_description2",
                                    true, true, false, false, false, false, false,
                                    "test_phone2", "test_domain_address2",
                                    null, null, null
                                   );
               domain2 = (Domain)m_domainFactory.create(domain2);

               role1 = new Role(DataObject.NEW_ID, 
                                domain1.getId(), 
                                "testname1", "testdescription1", true, DataObject.NEW_ID, false, 
                                null, null, null);
               role1 = (Role)m_roleFactory.create(role1);
         
               role2 = new Role(DataObject.NEW_ID, 
                                domain1.getId(), 
                                "testname2", "testdescription2", true, DataObject.NEW_ID, false, 
                                null, null, null);
               role2 = (Role)m_roleFactory.create(role2);
         
               role3 = new Role(DataObject.NEW_ID, 
                                domain2.getId(), 
                                "testname3", "testdescription3", true, DataObject.NEW_ID, false, 
                                null, null, null);
               role3 = (Role)m_roleFactory.create(role3);

               m_roleFactory.assignToDomain(domain1.getId(), 
                                            role1.getId() + "," + role2.getId());

               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }         
            
            // try to get all roles for domain1
            List lstRoles = m_roleControl.getAllRolesInDomain(domain1.getId());
            
            assertNotNull("Returned Role list should not be null", lstRoles);
            assertEquals("Returned Role list size is not correct", 2, lstRoles.size());
            
            Role testRole = (Role) lstRoles.get(0);
            
            assertTrue("Returned Role is not correct", 
               testRole.getId() == role1.getId() 
               || testRole.getId() == role2.getId());
            
            if (testRole.getId() == role1.getId())
            {
               assertEquals("Role is not the same", role1, testRole);
               testRole = (Role) lstRoles.get(1);
               assertEquals("Role is not the same", role2, testRole);
            }
            else
            {
               assertEquals("Role is not the same", role2, testRole);
               testRole = (Role) lstRoles.get(1);
               assertEquals("Role is not the same", role1, testRole);
            }
            
            // try to get all roles for domain2
            lstRoles = m_roleControl.getAllRolesInDomain(domain2.getId());
            
            assertNotNull("Returned Role list should not be null", lstRoles);
            assertEquals("Returned Role list size is not correct", 1, lstRoles.size());
            
            testRole = (Role) lstRoles.get(0);
            assertTrue("Returned Role is not correct", 
               testRole.getId() == role3.getId());
            assertEquals("Role is not the same", role3, testRole);
         }
         finally
         {
            m_transaction.begin();
            try
            {
               PreparedStatement statement = null;
               String            strQuery;
               try
               {
                  // delete mapped data
                  strQuery = "delete from " + DomainDatabaseSchema.DOMAIN_TABLE_NAME + "_ROLE_MAP where DOMAIN_ID in (?, ?)" +
                             " and ROLE_ID in (?, ?, ?)";
                  statement = m_connection.prepareStatement(strQuery);
                  statement.setInt(1, domain1.getId());
                  statement.setInt(2, domain2.getId());
                  statement.setInt(3, role1.getId());
                  statement.setInt(4, role2.getId());
                  statement.setInt(5, role3.getId());
                  statement.executeUpdate();
               }
               finally
               {
                  DatabaseUtils.closeStatement(statement);
               }
               try
               {
                  // delete roles
                  strQuery = "delete from " + RoleDatabaseSchema.ROLE_TABLE_NAME + " where DOMAIN_ID in (?, ?)" +
                             " and ID in (?, ?, ?)";
                  statement = m_connection.prepareStatement(strQuery);
                  statement.setInt(1, domain1.getId());
                  statement.setInt(2, domain2.getId());
                  statement.setInt(3, role1.getId());
                  statement.setInt(4, role2.getId());
                  statement.setInt(5, role3.getId());
                  statement.executeUpdate();
               }
               finally
               {
                  DatabaseUtils.closeStatement(statement);
               }
               if ((domain1 != null) && (domain1.getId() != DataObject.NEW_ID))
               {
                  // delete domain 1
                  m_domainFactory.delete(
                     domain1.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((domain2 != null) && (domain2.getId() != DataObject.NEW_ID))
               {
                  // delete domain 2
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
