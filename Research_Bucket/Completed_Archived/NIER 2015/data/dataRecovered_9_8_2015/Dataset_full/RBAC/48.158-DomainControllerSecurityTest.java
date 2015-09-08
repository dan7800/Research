/*
 * Copyright (c) 2004 - 2009 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: DomainControllerSecurityTest.java,v 1.16 2009/09/20 05:32:57 bastafidli Exp $
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
import org.opensubsystems.security.application.SecurityBackendModule;
import org.opensubsystems.security.data.AccessRight;
import org.opensubsystems.security.data.Domain;
import org.opensubsystems.security.data.DomainDataDescriptor;
import org.opensubsystems.security.data.Role;
import org.opensubsystems.security.data.User;
import org.opensubsystems.security.patterns.listdata.logic.impl.SecureListControllerTest;
import org.opensubsystems.security.persist.AccessRightFactory;
import org.opensubsystems.security.persist.DomainFactory;
import org.opensubsystems.security.persist.RoleFactory;
import org.opensubsystems.security.persist.db.AccessRightDatabaseFactory;
import org.opensubsystems.security.persist.db.UserListDatabaseTestUtils;
import org.opensubsystems.security.util.ActionConstants;

/**
 * Test for security facade above the persistence layer. Each method of domain
 * controller check if user was granted access rights required to perform any
 * given operation. We do not test, if the security checks are performed correctly 
 * and if they work at all. 
 * 
 * @version $Id: DomainControllerSecurityTest.java,v 1.16 2009/09/20 05:32:57 bastafidli Exp $
 * @author Julo Legeny
 */
public final class DomainControllerSecurityTest extends Tests
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private DomainControllerSecurityTest(
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
      TestSuite suite = new DatabaseTestSuite("DomainControllerSecurityTest");
      suite.addTestSuite(DomainControllerSecurityTestInternal.class);
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
   public static class DomainControllerSecurityTestInternal extends SecureListControllerTest
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
       * Factory to manage domanis.
       */
      protected DomainFactory m_domainFactory;
   
      /**
       * Controller used to manipulate domains.
       */
      protected DomainController m_domainControl;

      /**
       * Data type assigned to domain data types.
       */
      protected int m_iDomainDataType;
      
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
      public DomainControllerSecurityTestInternal(
         String strTestName
      ) throws Exception
      {
         super(strTestName, new UserListDatabaseTestUtils());
   
         m_rightFactory = (AccessRightDatabaseFactory)DataFactoryManager.getInstance(
                                         AccessRightFactory.class);
         m_roleFactory = (RoleFactory)DataFactoryManager.getInstance(
                                         RoleFactory.class);      
         m_domainFactory = (DomainFactory)DataFactoryManager.getInstance(
                                         DomainFactory.class);
         m_domainControl = (DomainController)ControllerManager.getInstance(
                                         DomainController.class);
         
         DomainDataDescriptor descriptor;
         
         descriptor = (DomainDataDescriptor)DataDescriptorManager.getInstance(
                                               DomainDataDescriptor.class);
         
         m_iDomainDataType = descriptor.getDataType();
      }
   
      // Tests ////////////////////////////////////////////////////////////////////
   
      /**
       * Test for getting of the domain. User have action granted for all data objects 
       * of given type (no id and no categories specified when granting access).
       * 
       *  @throws Exception - an error has occurred
       */
      public void testGetCheck(
      ) throws Exception 
      {
         User        curUser        = null;
         Domain      domain1        = null;
         Domain      domain2        = null;
         Domain      testDomain     = null;
         Role        userRole       = null;
         Object[]    arrTestDomains = null;
         
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
               arrTestDomains = constructTestDomains();
               domain1 = (Domain)arrTestDomains[0];
               domain2 = (Domain)arrTestDomains[1];
            
               domain1 = (Domain)m_domainFactory.create(domain1);
               domain2 = (Domain)m_domainFactory.create(domain2);
   
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
   
               // create access right that curUser can view all domain object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iDomainDataType,
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
            // 1. try to get domain1
            //------------------------
            // domain has to be retrieved 
            testDomain = (Domain)m_domainControl.get(domain1.getId());
            assertNotNull("Domain 1 should not be null", testDomain);
            assertEquals("Domain 1 should be the same but wasn't", domain1, testDomain);
            
            //------------------------
            // 2. try to get domain2
            //------------------------
            // domain has to be retrieved
            testDomain = (Domain)m_domainControl.get(domain2.getId());
            assertNotNull("Domain 2 should not be null", testDomain);
            assertEquals("Domain 2 should be the same but wasn't", domain2, testDomain);
         }
         finally
         {
            deleteTestData(userRole, domain1, domain2);
         }
      }
   
      /**
       * Test for getting of the domain. User have access granted to specific
       * data object (identified by identifier) of given data type.
       * 
       *  @throws Exception - an error has occurred
       */
      public void testGetCheckId(
      ) throws Exception 
      {
         User        curUser        = null;
         Domain      domain1        = null;
         Domain      domain2        = null;
         Domain      testDomain     = null;
         Role        userRole       = null;
         Object[]    arrTestDomains = null;
         
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
               arrTestDomains = constructTestDomains();
               domain1 = (Domain)arrTestDomains[0];
               domain2 = (Domain)arrTestDomains[1];
            
               domain1 = (Domain)m_domainFactory.create(domain1);
               domain2 = (Domain)m_domainFactory.create(domain2);
   
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
   
               // create access right that curUser can view only domain1 object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iDomainDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             domain1.getId(),
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
            // 1. try to get domain1
            //------------------------
            // domain has to be retrieved 
            testDomain = (Domain)m_domainControl.get(domain1.getId());
            assertNotNull("Domain 1 should not be null", testDomain);
            assertEquals("Domain 1 should be the same but wasn't", domain1, testDomain);
            
            //------------------------
            // 2. try to get domain2
            //------------------------
            // domain has not be retrieved
            testDomain = (Domain)m_domainControl.get(domain2.getId());
            assertNull("Domain 2 should not be retrieved because of access right limitation",
                       testDomain);
         }
         finally
         {
            deleteTestData(userRole, domain1, domain2);
         }
      }
   
      /**
       * Test for creating of the domain. User have action granted for all data objects 
       * of given type (no id and no categories specified when granting access).
       * 
       *  @throws Exception - an error has occurred
       */
      public void testCreateCheck(
      ) throws Exception 
      {
         User        curUser        = null;
         Domain      domain1        = null;
         Domain      domain2        = null;
         Domain      testDomain     = null;
         Role        userRole       = null;
         Object[]    arrTestDomains = null;
         
         AccessRight accessRightView   = null;
         AccessRight accessRightCreate = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_roleFactory.createPersonal(curUser);
   
            m_transaction.begin();
            try
            {
               arrTestDomains = constructTestDomains();
               domain1 = (Domain)arrTestDomains[0];
               domain2 = (Domain)arrTestDomains[1];
            
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
   
               // create access right that curUser can create all domain objects
               accessRightCreate = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_CREATE,
                                             m_iDomainDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             AccessRight.NO_RIGHT_IDENTIFIER,
                                             null, null);
               m_rightFactory.create(accessRightCreate); 
   
               // create access right that curUser can view all domain object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iDomainDataType,
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
            // 1. try to create domain1
            //------------------------
            m_transaction.begin();
            try
            {
               // try to create domain 
               testDomain = (Domain)m_domainControl.create(domain1);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            // domain has to be created 
            assertNotNull("Domain 1 should not be null", testDomain);
            assertEquals("Domain 1 should be created but wasn't", domain1, testDomain);
            
            //------------------------
            // 2. try to create domain2
            //------------------------
            m_transaction.begin();
            try
            {
               // try to create domain 
               testDomain = (Domain)m_domainControl.create(domain2);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // domain has to be created
            assertNotNull("Domain 2 should not be null", testDomain);
            assertEquals("Domain 2 should be created but wasn't", domain2, testDomain);
         }
         finally
         {
            deleteTestData(userRole, domain1, domain2);
         }
      }
   
      /**
       * Test for updating of the domain. User have action granted for all data objects 
       * of given type (no id and no categories specified when granting access).
       * 
       *  @throws Exception - an error has occurred
       */
      public void testSaveCheck(
      ) throws Exception 
      {
         User        curUser        = null;
         Domain      domain1        = null;
         Domain      domain2        = null;
         Domain      domain3        = null;
         Domain      testDomain     = null;
         Role        userRole       = null;
         Object[]    arrTestDomains = null;
         
         AccessRight accessRightView   = null;
         AccessRight accessRightCreate = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_roleFactory.createPersonal(curUser);
   
            m_transaction.begin();
            try
            {
               arrTestDomains = constructTestDomains();
               domain1 = (Domain)arrTestDomains[0];
               domain2 = (Domain)arrTestDomains[1];
   
               domain1 = (Domain)m_domainFactory.create(domain1);
               domain2 = (Domain)m_domainFactory.create(domain2);
   
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
   
               // create access right that curUser can modify all domain objects
               accessRightCreate = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_MODIFY,
                                             m_iDomainDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             AccessRight.NO_RIGHT_IDENTIFIER,
                                             null, null);
               m_rightFactory.create(accessRightCreate); 
   
               // create access right that curUser can view all domain object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iDomainDataType,
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
   
            domain3 = new Domain(domain1.getId(),
                                 "test_domain_name3",
                                 "test_domain_description3",
                                 domain1.getCreationTimestamp(),
                                 domain1.getModificationTimestamp()
                                 ); 
            //------------------------
            // 1. try to update domain1
            //------------------------
            m_transaction.begin();
            try
            {
               // try to modify domain
               testDomain = (Domain)m_domainControl.save(domain3);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // domain has to be updated 
            assertNotNull("Domain 1 should not be null", testDomain);
            assertEquals("Domain 1 should be updated but wasn't", domain3, testDomain);
            
            domain3 = new Domain(domain2.getId(),
                                 "test_domain_name4",
                                 "test_domain_description4",
                                 domain2.getCreationTimestamp(),
                                 domain2.getModificationTimestamp()
                                 ); 
            //------------------------
            // 2. try to update domain2
            //------------------------
            m_transaction.begin();
            try
            {
               // try to modify domain
               testDomain = (Domain)m_domainControl.save(domain3);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // domain has to be updated
            assertNotNull("Domain 2 should not be null", testDomain);
            assertEquals("Domain 2 should be updated but wasn't", domain3, testDomain);
         }
         finally
         {
            deleteTestData(userRole, domain1, domain2);
         }
      }
   
      /**
       * Test for updating of the domain. User have access granted to specific
       * data object (identified by identifier) of given data type.
       * 
       *  @throws Exception - an error has occurred
       */
      public void testSaveCheckId(
      ) throws Exception 
      {
         User        curUser        = null;
         Domain      domain1        = null;
         Domain      domain2        = null;
         Domain      domain3        = null;
         Domain      testDomain     = null;
         Role        userRole       = null;
         Object[]    arrTestDomains = null;
         
         AccessRight accessRightView   = null;
         AccessRight accessRightCreate = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_roleFactory.createPersonal(curUser);
   
            m_transaction.begin();
            try
            {
               arrTestDomains = constructTestDomains();
               domain1 = (Domain)arrTestDomains[0];
               domain2 = (Domain)arrTestDomains[1];
   
               domain1 = (Domain)m_domainFactory.create(domain1);
               domain2 = (Domain)m_domainFactory.create(domain2);
   
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
   
               // create access right that curUser can modify only domain1 objects
               accessRightCreate = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_MODIFY,
                                             m_iDomainDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             domain1.getId(),
                                             null, null);
               m_rightFactory.create(accessRightCreate); 
   
               // create access right that curUser can view all domain object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iDomainDataType,
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
   
            domain3 = new Domain(domain1.getId(),
                                 "test_domain_name3",
                                 "test_domain_description3",
                                 domain1.getCreationTimestamp(),
                                 domain1.getModificationTimestamp()
                                 ); 
            //------------------------
            // 1. try to update domain1
            //------------------------
            m_transaction.begin();
            try
            {
               // try to modify domain
               testDomain = (Domain)m_domainControl.save(domain3);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // domain has to be updated 
            assertNotNull("Domain 1 should not be null", testDomain);
            assertEquals("Domain 1 should be updated but wasn't", domain3, testDomain);
            
            domain3 = new Domain(domain2.getId(),
                                 "test_domain_name4",
                                 "test_domain_description4",
                                 domain2.getCreationTimestamp(),
                                 domain2.getModificationTimestamp()
                                 ); 
            //------------------------
            // 2. try to update domain2
            //------------------------
            m_transaction.begin();
            try
            {
               // try to modify domain
               testDomain = (Domain)m_domainControl.save(domain3);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // domain has not be updated
            assertNull("Domain 2 should not be updated because of access right limitation",
                       testDomain);
         }
         finally
         {
            deleteTestData(userRole, domain1, domain2);
         }
      }
   
      /**
       * Test for deleting of the domain. User have action granted for all data objects 
       * of given type (no id and no categories specified when granting access).
       * 
       *  @throws Exception - an error has occurred
       */
      public void testDeleteCheck(
      ) throws Exception 
      {
         User        curUser        = null;
         Domain      domain1        = null;
         Domain      domain2        = null;
         Domain      testDomain     = null;
         Role        userRole       = null;
         Object[]    arrTestDomains = null;
         
         AccessRight accessRightView   = null;
         AccessRight accessRightCreate = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_roleFactory.createPersonal(curUser);
   
            m_transaction.begin();
            try
            {
               arrTestDomains = constructTestDomains();
               domain1 = (Domain)arrTestDomains[0];
               domain2 = (Domain)arrTestDomains[1];
   
               domain1 = (Domain)m_domainFactory.create(domain1);
               domain2 = (Domain)m_domainFactory.create(domain2);
   
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
   
               // create access right that curUser can delete all domain objects
               accessRightCreate = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_DELETE,
                                             m_iDomainDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             AccessRight.NO_RIGHT_IDENTIFIER,
                                             null, null);
               m_rightFactory.create(accessRightCreate); 
   
               // create access right that curUser can view all domain object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iDomainDataType,
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
            // 1. try to delete domain1
            //------------------------
            m_transaction.begin();
            try
            {
               // try to delete domain
               m_domainControl.delete(domain1.getId());
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // domain has to be deleted
            testDomain = (Domain)m_domainControl.get(domain1.getId());
            assertNull("Domain 1 should be deleted but wasn't", testDomain);
            domain1 = null;
            
            //------------------------
            // 2. try to delete domain2
            //------------------------
            m_transaction.begin();
            try
            {
               // try to delete domain
               m_domainControl.delete(domain2.getId());
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // domain has to be deleted
            testDomain = (Domain)m_domainControl.get(domain2.getId());
            assertNull("Domain 2 should be deleted but wasn't", testDomain);
            domain2 = null;
         }
         finally
         {
            deleteTestData(userRole, domain1, domain2);
         }
      }
   
      /**
       * Test for deleting of the domain. User have access granted to specific
       * data object (identified by identifier) of given data type.
       * 
       *  @throws Exception - an error has occurred
       */
      public void testDeleteCheckId(
      ) throws Exception 
      {
         User        curUser        = null;
         Domain      domain1        = null;
         Domain      domain2        = null;
         Domain      testDomain     = null;
         Role        userRole       = null;
         Object[]    arrTestDomains = null;
         
         AccessRight accessRightView   = null;
         AccessRight accessRightCreate = null;
         
         int       iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
         // first change the current user that will be not super user
         curUser = changeCurrentUser();
   
         try
         {
            m_roleFactory.createPersonal(curUser);
   
            m_transaction.begin();
            try
            {
               arrTestDomains = constructTestDomains();
               domain1 = (Domain)arrTestDomains[0];
               domain2 = (Domain)arrTestDomains[1];
   
               domain1 = (Domain)m_domainFactory.create(domain1);
               domain2 = (Domain)m_domainFactory.create(domain2);
   
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
   
               // create access right that curUser can delete only domain1 objects
               accessRightCreate = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_DELETE,
                                             m_iDomainDataType,
                                             AccessRight.ACCESS_GRANTED,
                                             AccessRight.NO_RIGHT_CATEGORY,
                                             domain1.getId(),
                                             null, null);
               m_rightFactory.create(accessRightCreate); 
   
               // create access right that curUser can view all domain object
               accessRightView = new AccessRight(DataObject.NEW_ID,
                                             userRole.getId(),
                                             iCurrentDomainID,
                                             ActionConstants.RIGHT_ACTION_VIEW,
                                             m_iDomainDataType,
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
            // 1. try to delete domain1
            //------------------------
            m_transaction.begin();
            try
            {
               // try to delete domain
               m_domainControl.delete(domain1.getId());
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // domain has to be deleted
            testDomain = (Domain)m_domainControl.get(domain1.getId());
            assertNull("Domain 1 should be deleted but wasn't", testDomain);
            domain1 = null;
            
            //------------------------
            // 2. try to delete domain2
            //------------------------
            m_transaction.begin();
            try
            {
               // try to delete domain
               m_domainControl.delete(domain2.getId());
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // domain has not be deleted
            testDomain = (Domain)m_domainControl.get(domain2.getId());
            assertNotNull("Domain 2 should not be null", testDomain);
            assertEquals("Domain 2 should not be deleted because of access right limitation",
                         domain2.getId(), testDomain.getId());
         }
         finally
         {
            deleteTestData(userRole, domain1, domain2);
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
       * Method construct 2 test domains that will be used for all tests
       * 
       * @return Object[] - array of created domains
       *                  - index 0 = domain1 object; index 1 = domain2 object
       * @throws OSSException - error occurred during of test domains creating
       */
      private Object[] constructTestDomains(
      ) throws OSSException
      {
         Domain domain1 = null;
         Domain domain2 = null;
   
         // create 2 test domains
         domain1 = new Domain(DataObject.NEW_ID,
                              "test_domain_name1",
                              "test_domain_description1",
                              true, true, false, false, false, false, false,
                              "test_phone1", "test_domain_address1",
                              null, null, null
                             );
      
         domain2 = new Domain(DataObject.NEW_ID,
                              "test_domain_name2",
                              "test_domain_description2",
                              true, true, false, false, false, false, false,
                              "test_phone2", "test_domain_address2",
                              null, null, null
                             );
   
         return new Object[] {domain1, domain2};  
      }
   
      /**
       * Method for deleting test data - it will be used for all tests
       * 
       * @param userRole - user role that will be deleted
       * @param domain1 - domain 1 that will be deleted
       * @param domain2 - domain 2 that will be deleted
       * @throws Exception - error occurred during deleting of test data 
       */
      private void deleteTestData(
         Role userRole,
         Domain domain1,
         Domain domain2
      ) throws Exception
      {
         m_transaction.begin();
         try
         {
            if ((userRole != null) && (userRole.getId() != DataObject.NEW_ID))
            {
               m_rightFactory.deleteAllForRole(userRole.getId());
               m_roleFactory.removeFromUsers(String.valueOf(userRole.getUserId()), false);
               //m_roleFactory.delete(userRole.getId());
            }
            if ((domain1 != null) && (domain1.getId() != DataObject.NEW_ID))
            {
               m_domainFactory.delete(
                  domain1.getId(),
                  CallContext.getInstance().getCurrentDomainId());
            }
            if ((domain2 != null) && (domain2.getId() != DataObject.NEW_ID))
            {
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
