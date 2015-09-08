/*
 * Copyright (c) 2003 - 2009 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: DomainControllerTest.java,v 1.24 2009/09/20 05:32:57 bastafidli Exp $
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
import java.sql.ResultSet;
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
import org.opensubsystems.security.data.Domain;
import org.opensubsystems.security.data.Role;
import org.opensubsystems.security.patterns.listdata.logic.impl.SecureListControllerTest;
import org.opensubsystems.security.persist.DomainFactory;
import org.opensubsystems.security.persist.RoleFactory;
import org.opensubsystems.security.persist.db.DomainDatabaseSchema;
import org.opensubsystems.security.persist.db.DomainListDatabaseTestUtils;
import org.opensubsystems.security.util.RoleUtils;
import org.opensubsystems.security.utils.TestRoleDatabaseFactoryUtils;


/**
 * Test for DomainController interface implementation class.
 * 
 * @version $Id: DomainControllerTest.java,v 1.24 2009/09/20 05:32:57 bastafidli Exp $
 * @author Julo Legeny
 * @code.reviewer Miro Halas
 * @code.reviewed Initial revision
 */
public final class DomainControllerTest extends Tests
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private DomainControllerTest(
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
      TestSuite suite = new DatabaseTestSuite("DomainControllerTest");
      suite.addTestSuite(DomainControllerTestInternal.class);
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
   public static class DomainControllerTestInternal extends SecureListControllerTest
   {
      // Cached values ////////////////////////////////////////////////////////////

      /**
       * Factory to manage roles.
       */
      protected RoleFactory m_roleFactory;

      /**
       * Factory to manage domains.
       */
      protected DomainFactory m_domainFactory;
   
      /**
       * Controller used to manipulate domains.
       */
      protected DomainController m_domainControl;

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
      public DomainControllerTestInternal(
         String strTestName
      ) throws Exception
      {
         super(strTestName, new DomainListDatabaseTestUtils());
   
         m_domainFactory = (DomainFactory)DataFactoryManager.getInstance(
                                             DomainFactory.class);
         m_roleFactory = (RoleFactory)DataFactoryManager.getInstance(
                                             RoleFactory.class);      
         m_domainControl = (DomainController)ControllerManager.getInstance(
                                             DomainController.class);
         m_roleFactoryUtils = new TestRoleDatabaseFactoryUtils();
      }
   
      // Tests ////////////////////////////////////////////////////////////////////
   
      /**
       * Test for getting of the domain by id
       * 
       * @throws Exception - error occurred
       */
      public void testGet(
      ) throws Exception 
      {
         Domain domain    = null;
         Domain testDomain = null;
   
         try
         {
            m_transaction.begin();
            try
            {
               // create test domain first
               domain = new Domain(DataObject.NEW_ID,
                                    "test_domain_name",
                                    "test_domain_description",
                                    true, true, false, false, false, false, false,
                                    "test_phone", "test_domain_address",
                                    null, null, null
                                   );
            
               domain = (Domain)m_domainFactory.create(domain);
            
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            // try to get domain with specific ID
            testDomain = (Domain)m_domainControl.get(domain.getId());
   
            assertNotNull("Domain should be not null", testDomain);
            assertEquals("Domain is not the same", domain, testDomain);
         }
         finally
         {
            m_transaction.begin();
            // delete inserted records
            try
            {
               if ((domain != null) && (domain.getId() != DataObject.NEW_ID))
               {
                  // delete role
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
       * Test for getting flag signaling existence of an domain within the database    
       * 
       * @throws Exception - error occurred during getting domain
       */
      public void testExistDomain(
      ) throws Exception
      {
         boolean bExist = false;
         bExist = m_domainControl.existDomain();
         
         // there should exist an domain
         assertTrue("Domain should exist within the database", bExist);
      }

      /**
       * Test for creating domain
       * 
       * @throws Exception - an error has occurred
       */
      public void testCreate(
      ) throws Exception
      {
         Domain domain    = null;
         Domain testDomain = null;
         Domain selectedDomain = null;
   
         try
         {
            m_transaction.begin();
            try
            {
               domain = new Domain(DataObject.NEW_ID,
                                    "test_domain_name",
                                    "test_domain_description",
                                    true, true, false, false, false, false, false,
                                    "test_phone", "test_domain_address",
                                    null, null, null
                                   );
               // try to create domain         
               testDomain = (Domain)m_domainControl.create(domain);
            
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            assertNotNull("Domain should be created but was not", testDomain);
            assertTrue("Id was not generated", testDomain.getId() != DataObject.NEW_ID);
            assertNotNull("Creation timestamp was not generated", 
                          testDomain.getCreationTimestamp());
            assertNotNull("Modification timestamp was not generated", 
                          testDomain.getModificationTimestamp());
            assertTrue("Domain is not the same", domain.isSame(testDomain));
   
            // Use factory to perform the verification operation since we relay
            // that the factory was tested
            selectedDomain = (Domain)m_domainFactory.get(
                                testDomain.getId(),
                                CallContext.getInstance().getCurrentDomainId());
            assertNotNull("Domain should be created but was not", selectedDomain);
            assertEquals("Domain is not the same", testDomain, selectedDomain);
         }
         finally
         {
            m_transaction.begin();
            // delete inserted records
            try
            {
               if ((domain != null) && (domain.getId() != DataObject.NEW_ID))
               {
                  // delete role
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
       * Test for saving Domain 
       * 
       *  @throws Exception - an error has occurred
       */
      public void testSave(
      ) throws Exception
      {
         Domain domain1        = null;
         Domain domain2        = null;
         Domain testDomain     = null;
         Domain selectedDomain = null;
   
         try
         {
            m_transaction.begin();
            try
            {
               domain1 = new Domain(DataObject.NEW_ID,
                                     "test_domain_name_1",
                                     "test_domain_description_1",
                                     true, true, false, false, false, false, false,
                                     "test_phone_1", "test_domain_address_1",
                                     null, null, null
                                    );
               domain1 = (Domain)m_domainFactory.create(domain1);
            
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            domain2 = new Domain(domain1.getId(),
                                  "test_domain_name_2",
                                  "test_domain_description_2",
                                  false, false, true, true, true, true, true,
                                  "test_phone_2", "test_domain_address_2",
                                  domain1.getCreationTimestamp(),
                                  domain1.getModificationTimestamp(),
                                  null
                                 );
   
            m_transaction.begin();
            try
            {
               // try to modify domain
               testDomain = (Domain)m_domainControl.save(domain2);         
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            assertNotNull("Domain should not be null", testDomain);
            assertTrue("Modified date not changed", testDomain.getModificationTimestamp() 
                                                    != domain1.getModificationTimestamp());
            assertTrue("Domain is not the same", domain2.isSame(testDomain));
            
            // Use factory to perform the verification operation since we relay
            // that the factory was tested
            selectedDomain = (Domain)m_domainFactory.get(
                                        testDomain.getId(),
                                        CallContext.getInstance().getCurrentDomainId());
            assertNotNull("Domain should be created but was not", selectedDomain);
            assertEquals("Domain is not the same", testDomain, selectedDomain);
         }
         finally
         {
            m_transaction.begin();
            // delete inserted records
            try
            {
               if ((domain1 != null) && (domain1.getId() != DataObject.NEW_ID))
               {
                  // delete role
                  m_domainFactory.delete(
                     domain1.getId(),
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
       * Test for deleting Domain
       * 
       *  @throws Exception - an error has occurred
       */
      public void testDelete(
      ) throws Exception 
      {
         Domain domain = null;
         Domain selectedDomain = null;
   
         try
         {
            m_transaction.begin();
            try
            {
               domain = new Domain(DataObject.NEW_ID,
                                    "test_domain_name_1",
                                    "test_domain_description_1",
                                    true, true, false, false, false, false, false,
                                    "test_phone_1", "test_domain_address_1",
                                    null, null, null
                                   );
               domain = (Domain)m_domainFactory.create(domain);
            
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
               // try to delete domain
               m_domainControl.delete(domain.getId());         
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            // Use factory to perform the verification operation since we relay
            // that the factory was tested
            selectedDomain = (Domain)m_domainFactory.get(
                                        domain.getId(),
                                        CallContext.getInstance().getCurrentDomainId());
            assertNull("Domain should be deleted but was not", selectedDomain);
            domain = null;
         }
         finally
         {
            m_transaction.begin();
            // delete inserted records
            try
            {
               if ((domain != null) && (domain.getId() != DataObject.NEW_ID))
               {
                  // delete role
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
       * Test for deleting domains knowing array of their IDs  
       * 
       * @throws Exception - error occurred while deleting more domains
       */
      public void testDeleteByIds(
      ) throws Exception
      {
         PreparedStatement statement = null;
         ResultSet         results = null;     
         String            strQuery;
         
         Domain domain1 = null;
         Domain domain2 = null;
         Domain domain3 = null;
         
         int  iDeleted = 0;
         boolean bDeleted = false;
   
         try
         {
            m_transaction.begin();      
            try
            {
               domain1 = new Domain(DataObject.NEW_ID,
                                    "test_domain_name_1",
                                    "test_domain_name_1",
                                    null, null);
               domain1 = (Domain)m_domainFactory.create(domain1);
               
               domain2 = new Domain(DataObject.NEW_ID,
                                    "test_domain_name_2",
                                    "test_domain_name_2",
                                    null, null);
               domain2 = (Domain)m_domainFactory.create(domain2);
   
               domain3 = new Domain(DataObject.NEW_ID,
                                    "test_domain_name_3",
                                    "test_domain_name_3",
                                    null, null);
               domain3 = (Domain)m_domainFactory.create(domain3);

               m_transaction.commit();         
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
      
            String strDomainIds = domain1.getId() + "," + domain3.getId();
            m_transaction.begin();
            try
            {
               // try to delete domains
               iDeleted = m_domainControl.delete(strDomainIds);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            assertEquals("Number of deleted domains is incorrect", 2, iDeleted);
            try
            {
               // try to get deleted domains
               strQuery = "select ID from " + DomainDatabaseSchema.DOMAIN_TABLE_NAME + " where ID IN (?, ?)";
               statement = m_connection.prepareStatement(strQuery);
               statement.setInt(1, domain1.getId());
               statement.setInt(2, domain3.getId());
               results = statement.executeQuery();
         
               assertFalse("Domains should be deleted but were not", results.next());
               bDeleted = true;
            }
            finally
            {
               DatabaseUtils.closeResultSetAndStatement(results, statement);
            }

            try
            {
               // try to get not deleted domains
               strQuery = "select ID from " + DomainDatabaseSchema.DOMAIN_TABLE_NAME + " where ID IN (?)";
               statement = m_connection.prepareStatement(strQuery);
               statement.setInt(1, domain2.getId());
               results = statement.executeQuery();
         
               assertTrue("Domains should not be deleted but it was", results.next());
            }
            finally
            {
               DatabaseUtils.closeResultSetAndStatement(results, statement);
            }
         }
         finally
         {
            if (bDeleted)
            {
               domain1 = null;
               domain3 = null;
            }

            m_transaction.begin();
            try
            {
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
               if ((domain3 != null) && (domain3.getId() != DataObject.NEW_ID))
               {
                  m_domainFactory.delete(
                     domain3.getId(),
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
       * Test for deleting domains knowing array of their IDs and 
       * there will be also current domain ID  
       * 
       * @throws Exception - error occurred while deleting more domains
       */
      public void testDeleteByIdsWithCurrentDomainId(
      ) throws Exception
      {
         PreparedStatement statement = null;
         ResultSet         results = null;     
         String            strQuery;
         
         Domain domain1 = null;
         Domain domain2 = null;
         Domain domain3 = null;
         
         int  iDeleted = 0;
         int iCurrentDomainId = CallContext.getInstance().getCurrentDomainId();
   
         try
         {
            m_transaction.begin();      
            try
            {
               domain1 = new Domain(DataObject.NEW_ID,
                                    "test_domain_name_1",
                                    "test_domain_name_1",
                                    null, null);
               domain1 = (Domain)m_domainFactory.create(domain1);
               
               domain2 = new Domain(DataObject.NEW_ID,
                                    "test_domain_name_2",
                                    "test_domain_name_2",
                                    null, null);
               domain2 = (Domain)m_domainFactory.create(domain2);
   
               domain3 = new Domain(DataObject.NEW_ID,
                                    "test_domain_name_3",
                                    "test_domain_name_3",
                                    null, null);
               domain3 = (Domain)m_domainFactory.create(domain3);

               m_transaction.commit();         
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
      
            String strDomainIds = domain1.getId() + "," + domain3.getId() +
                                 "," + iCurrentDomainId;
            m_transaction.begin();
            try
            {
               // try to delete domains
               iDeleted = m_domainControl.delete(strDomainIds);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            assertEquals("Number of deleted domains is incorrect", 0, iDeleted);
            try
            {
               // try to get not deleted domains
               strQuery = "select count(ID) from " + DomainDatabaseSchema.DOMAIN_TABLE_NAME + " where ID IN (?, ?, ?, ?)";
               statement = m_connection.prepareStatement(strQuery);
               statement.setInt(1, domain1.getId());
               statement.setInt(2, domain2.getId());
               statement.setInt(3, domain3.getId());
               statement.setInt(4, iCurrentDomainId);
               results = statement.executeQuery();
         
               assertTrue("Domains should not be deleted but were", results.next());
               assertEquals("Number of domains is incorrect", 4, results.getInt(1));
            }
            finally
            {
               DatabaseUtils.closeResultSetAndStatement(results, statement);
            }
         }
         finally
         {
            m_transaction.begin();
            try
            {
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
               if ((domain3 != null) && (domain3.getId() != DataObject.NEW_ID))
               {
                  m_domainFactory.delete(
                     domain3.getId(),
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
       * Test of updateEnable method
       * 
       * @throws Exception - an error has occurred
       */
      public void testUpdateEnable(
      ) throws Exception
      {
         Domain domain1 = null;
         Domain domain2 = null;
         Domain domain3 = null;
         Domain domain4 = null;
         Domain domain5 = null;
         Domain testDomain = null;
   
         try
         {
            m_transaction.begin();      
            try
            {
               domain1 = new Domain(DataObject.NEW_ID,
                                    "test_domain_name_1",
                                    "test_domain_name_1",
                                    null, null);
               domain1 = (Domain)m_domainFactory.create(domain1);
               
               domain2 = new Domain(DataObject.NEW_ID,
                                    "test_domain_name_2",
                                    "test_domain_name_2",
                                    null, null);
               domain2 = (Domain)m_domainFactory.create(domain2);
   
               domain3 = new Domain(DataObject.NEW_ID,
                                    "test_domain_name_3",
                                    "test_domain_name_3",
                                    null, null);
               domain3 = (Domain)m_domainFactory.create(domain3);

               domain4 = new Domain(DataObject.NEW_ID,
                                    "test_domain_name_4",
                                    "test_domain_name_4",
                                    null, null);
               domain4 = (Domain)m_domainFactory.create(domain4);
               
               domain5 = new Domain(DataObject.NEW_ID,
                                    "test_domain_name_5",
                                    "test_domain_name_5",
                                    null, null);
               domain5 = (Domain)m_domainFactory.create(domain5);

               m_transaction.commit();         
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
      
            Thread.sleep(1000);
   
            int iDisabled = 0;
            
            m_transaction.begin();      
            try
            {
               iDisabled = m_domainControl.updateEnable(Integer.toString(domain1.getId()) 
                              + "," + Integer.toString(domain2.getId()) 
                              + "," + Integer.toString(domain5.getId()) + ", 987654321", false);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            
            assertEquals("Number of enabled Domains is incorect", 3, iDisabled);
            testDomain = (Domain)m_domainFactory.get(
                            domain1.getId(),
                            CallContext.getInstance().getCurrentDomainId());
            assertNotNull("Domain should still exist", testDomain);
            assertFalse("Domain should be disabled", testDomain.isEnabled());
            assertTrue("Modified date should be changed",
                       testDomain.getModificationTimestamp().getTime()
                       != domain1.getModificationTimestamp().getTime());
            testDomain = (Domain)m_domainFactory.get(
                            domain2.getId(),
                            CallContext.getInstance().getCurrentDomainId());
            assertNotNull("Domain should still exist", testDomain);
            assertFalse("Domain should be disabled", testDomain.isEnabled());
            assertTrue("Modified date should be changed",
                       testDomain.getModificationTimestamp().getTime()
                       != domain2.getModificationTimestamp().getTime());
            testDomain = (Domain)m_domainFactory.get(
                            domain5.getId(),
                            CallContext.getInstance().getCurrentDomainId());
            assertNotNull("Domain should still exist", testDomain);
            assertFalse("Domain should be disabled", testDomain.isEnabled());
            assertTrue("Modified date should be changed",
                       testDomain.getModificationTimestamp().getTime()
                       != domain5.getModificationTimestamp().getTime());         

         }
         finally
         {
            m_transaction.begin();
            try
            {
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
               if ((domain3 != null) && (domain3.getId() != DataObject.NEW_ID))
               {
                  m_domainFactory.delete(
                     domain3.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((domain4 != null) && (domain4.getId() != DataObject.NEW_ID))
               {
                  m_domainFactory.delete(
                     domain4.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((domain5 != null) && (domain5.getId() != DataObject.NEW_ID))
               {
                  m_domainFactory.delete(
                     domain5.getId(),
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
       * Test of updateEnable method when there has to be disabled current domain
       * 
       * @throws Exception - an error has occurred
       */
      public void testUpdateEnableCurrentDomain(
      ) throws Exception
      {
         Domain domain1 = null;
         Domain domain2 = null;
   
         try
         {
            m_transaction.begin();      
            try
            {
               domain1 = new Domain(DataObject.NEW_ID,
                                    "test_domain_name_1",
                                    "test_domain_name_1",
                                    null, null);
               domain1 = (Domain)m_domainFactory.create(domain1);
               
               domain2 = new Domain(DataObject.NEW_ID,
                                    "test_domain_name_2",
                                    "test_domain_name_2",
                                    null, null);
               domain2 = (Domain)m_domainFactory.create(domain2);
   
               m_transaction.commit();         
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
      
            Thread.sleep(1000);
   
            int iDisabled = 0;
            int iCurrentDomainId = CallContext.getInstance().getCurrentDomainId();
            
            m_transaction.begin();      
            try
            {
               iDisabled = m_domainControl.updateEnable(Integer.toString(domain1.getId()) 
                              + "," + Integer.toString(domain1.getId()) 
                              + "," + Integer.toString(domain2.getId()) 
                              + "," + Integer.toString(iCurrentDomainId), false);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            
            assertEquals("Number of enabled Domains is incorect", 0, iDisabled);
         }
         finally
         {
            m_transaction.begin();
            try
            {
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

      /**
       * Test for getting current domain with assigned roles
       * 
       *  @throws Exception - an error has occurred
       */
      public void testGetCurrentDomainWithRoles(
      ) throws Exception 
      {
         Domain currentDomain = null;
         Domain testDomain    = null;
         Role   role1 = null;
         Role   role2 = null;
         List   roles = null;
         
         try
         {
            m_transaction.begin();
            try
            {
               role1 = new Role(DataObject.NEW_ID, 
                                 CallContext.getInstance().getCurrentDomainId(), 
                                 "test_name_1", "test_description_1",
                                 true, DataObject.NEW_ID,
                                 false, null, null, null);
               role1 = (Role)m_roleFactory.create(role1);
            
               role2 = new Role(DataObject.NEW_ID, 
                                 CallContext.getInstance().getCurrentDomainId(), 
                                 "test_name_2", "test_description_2",
                                 true, DataObject.NEW_ID,
                                 false, null, null, null);
               role2 = (Role)m_roleFactory.create(role2);
   
               m_roleFactory.assignToDomain(CallContext.getInstance().getCurrentDomainId(), 
                                            role1.getId() + "," + role2.getId());
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }

            currentDomain = (Domain)m_domainControl.get(
                                CallContext.getInstance().getCurrentDomainId());

            // try to get domain with roles
            testDomain = m_domainControl.getCurrentDomainWithRoles();         
            roles = testDomain.getDefaultRoles();
         
            assertNotNull("Domain should not be null", testDomain);
            assertEquals("Domain is not the same", currentDomain, testDomain);
            assertNotNull("List of default user roles should not be null", roles);
            assertEquals("Number of assigned roles is incorrect", 2, roles.size());
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
       * Test for getting domain with assigned roles
       * 
       *  @throws Exception - an error has occurred
       */
      public void testGetDomainWithRoles(
      ) throws Exception 
      {
         Domain domain     = null;
         Domain testDomain = null;
         Role     role1    = null;
         Role     role2    = null;
         List     roles  = null;
         
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
                                    null, null, null);
               domain = (Domain)m_domainControl.create(domain);
   
               role1 = new Role(DataObject.NEW_ID, 
                                 domain.getId(), 
                                 "test_name_1", "test_description_1",
                                 true, DataObject.NEW_ID,
                                 false, null, null, null);
               role1 = (Role)m_roleFactory.create(role1);
            
               role2 = new Role(DataObject.NEW_ID, 
                                 domain.getId(), 
                                 "test_name_2", "test_description_2",
                                 true, DataObject.NEW_ID,
                                 false, null, null, null);
               role2 = (Role)m_roleFactory.create(role2);
   
               m_roleFactory.assignToDomain(domain.getId(), role1.getId() + "," + role2.getId());
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }

            // try to get domain with roles
            testDomain = m_domainControl.getDomainWithAssociatedData(domain.getId());         
            roles = testDomain.getDefaultRoles();
         
            assertNotNull("Domain should not be null", testDomain);
            assertEquals("Domain is not the same", domain, testDomain);
            assertNotNull("List of default user roles should not be null", roles);
            assertEquals("Number of assigned roles is incorrect", 2, roles.size());
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
                  m_roleFactoryUtils.deleteRoleCascadeManual(role1.getId(), domain.getId());
               }
               if ((role2 != null) && (role2.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(role2.getId(), domain.getId());
               }
               if ((domain != null) && (domain.getId() != DataObject.NEW_ID))
               {
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
       * Test for getting domain with assigned roles when there already exist
       * self registered user role
       * 
       *  @throws Exception - an error has occurred
       */
      public void testGetDomainWithRolesSelfreg(
      ) throws Exception 
      {
         Domain domain         = null;
         Domain testDomain     = null;
         Role     role1        = null;
         Role     role2        = null;
         Role     selfregRole  = null;
         List     roles        = null;
         
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
                                    null, null, null);
               domain = (Domain)m_domainControl.create(domain);
   
               role1 = new Role(DataObject.NEW_ID, 
                                 domain.getId(), 
                                 "test_name_1", "test_description_1",
                                 true, DataObject.NEW_ID,
                                 false, null, null, null);
               role1 = (Role)m_roleFactory.create(role1);
            
               role2 = new Role(DataObject.NEW_ID, 
                                 domain.getId(), 
                                 "test_name_2", "test_description_2",
                                 true, DataObject.NEW_ID,
                                 false, null, null, null);
               role2 = (Role)m_roleFactory.create(role2);
   
               selfregRole = new Role(DataObject.NEW_ID, 
                                 CallContext.getInstance().getCurrentDomainId(), 
                                 RoleUtils.SELFREG_ROLE_NAME, 
                                 RoleUtils.SELFREG_ROLE_DESCRIPTION,
                                 true, DataObject.NEW_ID,
                                 false, null, null, null);
               selfregRole = (Role)m_roleFactory.create(selfregRole);

               m_roleFactory.assignToDomain(domain.getId(), role1.getId() + "," + role2.getId());
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }

            // try to get domain with roles
            testDomain = m_domainControl.getDomainWithAssociatedData(DataObject.NEW_ID);         
            roles = testDomain.getDefaultRoles();
         
            assertNotNull("Domain should not be null", testDomain);
            assertNotNull("List of default user roles should not be null", roles);
            assertEquals("Number of assigned roles is incorrect", 1, roles.size());
            assertTrue("Self registered user role should be assigned", 
                       selfregRole.equals(roles.get(0)));
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
                  m_roleFactoryUtils.deleteRoleCascadeManual(role1.getId(), domain.getId());
               }
               if ((role2 != null) && (role2.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(role2.getId(), domain.getId());
               }
               if ((selfregRole != null) && (selfregRole.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(selfregRole.getId());
               }
               if ((domain != null) && (domain.getId() != DataObject.NEW_ID))
               {
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
                                   "testname1", "testdescription1", true, DataObject.NEW_ID, 
                                   false, null, null, null);
               role1 = (Role)m_roleFactory.create(role1);
         
               role2 = new Role(DataObject.NEW_ID, 
                                   CallContext.getInstance().getCurrentDomainId(), 
                                   "testname2", "testdescription2", true, DataObject.NEW_ID, 
                                   false, null, null, null);
               role2 = (Role)m_roleFactory.create(role2);
         
               role3 = new Role(DataObject.NEW_ID, 
                                   CallContext.getInstance().getCurrentDomainId(), 
                                   "testname3", "testdescription3", true, DataObject.NEW_ID, 
                                   false, null, null, null);
               role3 = (Role)m_roleFactory.create(role3);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }         
            // assignt default roles to domain
            m_roleFactory.assignToDomain(
                  CallContext.getInstance().getCurrentDomainId(),
                  role1.getId() + "," + role2.getId());
            
            List roles = m_domainControl.getRoles(CallContext.getInstance().getCurrentDomainId());
            
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
       * Test for creating domain without roles
       * 
       *  @throws Exception - an error has occurred
       */
      public void testCreateWithNoRoles(
      ) throws Exception 
      {
         Domain newDomain      = null;
         Domain domain         = null;
         Domain selectedDomain = null;
         Role role1            = null;
         Role role2            = null;
         Role selectedRole     = null;
         List roles;
   
         newDomain = new Domain(DataObject.NEW_ID,
                                "test_domain_name_1",
                                "test_domain_name_1",
                                null, null);
         try
         {
            m_transaction.begin();
            try
            {
               // create 2 new roles
               role1 = new Role(DataObject.NEW_ID, 
                                 CallContext.getInstance().getCurrentDomainId(), 
                                 "test_name_1", "no_role_test_description_1",
                                 true, DataObject.NEW_ID,
                                 false, null, null, null);
               role1 = (Role)m_roleFactory.create(role1);
            
               role2 = new Role(DataObject.NEW_ID, 
                                 CallContext.getInstance().getCurrentDomainId(), 
                                 "test_name_2", "no_role_test_description_2",
                                 true, DataObject.NEW_ID,
                                 false, null, null, null);
               role2 = (Role)m_roleFactory.create(role2);
   
               // try to create domain without roles
               domain = m_domainControl.create(newDomain, "");
            
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
    
            assertNotNull("Domain should be not null", domain);
            
            assertTrue("Id was not generated", domain.getId() != DataObject.NEW_ID);
            assertNotNull("Creation timestamp was not generated", 
                          domain.getCreationTimestamp());
            assertNotNull("Modification timestamp was not generated", 
                          domain.getModificationTimestamp());
            assertTrue("Domain is not the same", newDomain.isSame(domain));

            assertNotNull("Domain should have assigned self registered user role.", 
                          domain.getDefaultRoles());
            assertEquals("Number of assigned roles is incorrect.", 
                         1, domain.getDefaultRoles().size());
            assertEquals("Incorrect name of the selfreg role.", 
                         RoleUtils.SELFREG_ROLE_NAME, 
                         ((Role)domain.getDefaultRoles().get(0)).getName());
            assertEquals("Incorrect description of the selfreg role.", 
                         RoleUtils.SELFREG_ROLE_DESCRIPTION, 
                         ((Role)domain.getDefaultRoles().get(0)).getDescription());

            // Use factory to perform the verification operation since we relay
            // that the factory was tested
            selectedDomain = (Domain)m_domainFactory.get(
                                domain.getId(),
                                CallContext.getInstance().getCurrentDomainId());
            assertNotNull("Domain should be created but was not", selectedDomain);
            assertTrue("Domain is not the same", domain.isSame(selectedDomain));
            
            assertNotNull("Domain should be created but was not", selectedDomain);
            assertTrue("Domain is not the same", domain.isSame(selectedDomain));
            
            assertNull("Domain should not have assigned role.", 
                       selectedDomain.getDefaultRoles());

            // try to get roles assigned to the domain
            roles = m_roleFactory.getAllForDomain(domain.getId(), SimpleRule.ALL_DATA);
            assertNotNull("Domain should have assigned selfreg role.", roles);
            assertEquals("Number of assigned roles is incorrect.", 
                         1, roles.size());
            
            selectedRole = (Role)roles.get(0);
            assertEquals("Incorrect name of the selfreg role.", 
                         RoleUtils.SELFREG_ROLE_NAME, 
                         selectedRole.getName());
            assertEquals("Incorrect description of the selfreg role.", 
                         RoleUtils.SELFREG_ROLE_DESCRIPTION, 
                         selectedRole.getDescription());
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
                  m_roleFactoryUtils.deleteRoleCascadeManual(selectedRole.getId(), domain.getId());
               }
               if ((role1 != null) && (role1.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(role1.getId());
               }
               if ((role2 != null) && (role2.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(role2.getId());
               }
               if ((domain != null) && (domain.getId() != DataObject.NEW_ID))
               {
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
       * Test for creating domain with roles
       * 
       *  @throws Exception - an error has occurred
       */
      public void testCreateWithRoles(
      ) throws Exception 
      {
         Domain newDomain      = null;
         Domain domain         = null;
         Domain selectedDomain = null;
         Role role1            = null;
         Role role2            = null;
         Role selectedRole1    = null;
         Role selectedRole2    = null;
         List roles;
   
         newDomain = new Domain(DataObject.NEW_ID,
                                "test_domain_name_1",
                                "test_domain_name_1",
                                null, null);
         try
         {
            m_transaction.begin();
            try
            {
               // create 2 new roles
               role1 = new Role(DataObject.NEW_ID, 
                                 CallContext.getInstance().getCurrentDomainId(), 
                                 "test_name_1", "create_with_role_test_description_1",
                                 true, DataObject.NEW_ID,
                                 false, null, null, null);
               role1 = (Role)m_roleFactory.create(role1);
            
               role2 = new Role(DataObject.NEW_ID, 
                                 CallContext.getInstance().getCurrentDomainId(), 
                                 "test_name_2", "create_with_role_test_description_2",
                                 true, DataObject.NEW_ID,
                                 false, null, null, null);
               role2 = (Role)m_roleFactory.create(role2);
   
               // try to create domain with roles
               domain = m_domainControl.create(newDomain, role1.getId() + "," + role2.getId());

               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
    
            assertNotNull("Domain should be not null", domain);
            
            assertTrue("Id was not generated", domain.getId() != DataObject.NEW_ID);
            assertNotNull("Creation timestamp was not generated", 
                          domain.getCreationTimestamp());
            assertNotNull("Modification timestamp was not generated", 
                          domain.getModificationTimestamp());
            assertTrue("Domain is not the same", newDomain.isSame(domain));
   
            // Use factory to perform the verification operation since we relay
            // that the factory was tested
            selectedDomain = (Domain)m_domainFactory.get(
                                domain.getId(),
                                CallContext.getInstance().getCurrentDomainId());
            assertNotNull("Domain should be created but was not", selectedDomain);
            assertEquals("Domain is not the same", domain, selectedDomain);
            
            assertNotNull("Domain should have assigned roles.", 
                          domain.getDefaultRoles());
            assertEquals("Domain should have exactly 2 assigned roles", 
                         2, domain.getDefaultRoles().size());
               
             // try to get default roles assigned to the domain
            roles = m_roleFactory.getAllForDomain(domain.getId(), SimpleRule.ALL_DATA);
            assertNotNull("Domain should have assigned role.", roles);
            assertEquals("Domain should have exactly 2 assigned roles", 
                         2, roles.size());
            
            selectedRole1 = (Role)roles.get(0);
            selectedRole2 = (Role)roles.get(1);
            
            // equals roles assigned to the new domain with roles returned from
            // create domain method
            assertTrue("Domain should have assigned role 1.", 
                     domain.getDefaultRoles().get(0).equals(selectedRole1)
                       || domain.getDefaultRoles().get(0).equals(selectedRole2));
            assertTrue("Domain should have assigned role 2.", 
                       domain.getDefaultRoles().get(1).equals(selectedRole1)
                       || domain.getDefaultRoles().get(1).equals(selectedRole2));
         }
         finally
         {
            m_transaction.begin();
            try
            {
               // delete roles for original domain 
               if ((role1 != null) && (role1.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(role1.getId());
               }
               if ((role2 != null) && (role2.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(role2.getId());
               }
               // delete roles for newly created domain
               if ((selectedRole1 != null) && (selectedRole1.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(selectedRole1.getId(), 
                                                             domain.getId());
               }
               if ((selectedRole2 != null) && (selectedRole2.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(selectedRole2.getId(), 
                                                             domain.getId());
               }
               if ((domain != null) && (domain.getId() != DataObject.NEW_ID))
               {
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
       * Test for saving user with roles assigned 
       * 
       *  @throws Exception - an error has occurred
       */
      public void testSaveWithRoles(
      ) throws Exception 
      {
         Domain domain1     = null;
         Domain domain2     = null;
         Domain testDomain1 = null;
         Domain testDomain2 = null;
         Role role1 = null;
         Role role2 = null;
         Role role3 = null;
         Role role4 = null;
         Role role5 = null;
         Role role6 = null;
         List roles;
   
         try
         {
            m_transaction.begin();
            try
            {
               domain1 = new Domain(DataObject.NEW_ID,
                                    "test_domain_name1",
                                    "test_domain_description1",
                                    true, true, false, false, false, false, false,
                                    "test_phone1", "test_domain_address1",
                                    null, null, null);
               domain1 = (Domain)m_domainFactory.create(domain1);
               
               role1 = new Role(DataObject.NEW_ID, 
                                 domain1.getId(), 
                                 "test_name_1", "test_description_1",
                                 true, DataObject.NEW_ID,
                                 false, null, null, null);
               role1 = (Role)m_roleFactory.create(role1);
            
               role2 = new Role(DataObject.NEW_ID, 
                                 domain1.getId(),
                                 "test_name_2", "test_description_2",
                                 true, DataObject.NEW_ID,
                                 false, null, null, null);
               role2 = (Role)m_roleFactory.create(role2);
   
               role3 = new Role(DataObject.NEW_ID, 
                                 domain1.getId(),
                                 "test_name_3", "test_description_3",
                                 true, DataObject.NEW_ID,
                                 false, null, null, null);
               role3 = (Role)m_roleFactory.create(role3);

               role4 = new Role(DataObject.NEW_ID, 
                                 domain1.getId(),
                                 "test_name_4", "test_description_4",
                                 true, DataObject.NEW_ID,
                                 false, null, null, null);
               role4 = (Role)m_roleFactory.create(role4);

               role5 = new Role(DataObject.NEW_ID, 
                                 domain1.getId(),
                                 "test_name_5", "test_description_5",
                                 true, DataObject.NEW_ID,
                                 false, null, null, null);
               role5 = (Role)m_roleFactory.create(role5);

               role6 = new Role(DataObject.NEW_ID, 
                                 domain1.getId(),
                                 "test_name_6", "test_description_6",
                                 true, DataObject.NEW_ID,
                                 false, null, null, null);
               role6 = (Role)m_roleFactory.create(role6);
               
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }         
   
            domain2 = new Domain(domain1.getId(),
                                 "test_domain_name2",
                                 "test_domain_description2",
                                 false, false, true, true, true, true, true,
                                 "test_phone2", "test_domain_address2",
                                 domain1.getCreationTimestamp(),
                                 domain1.getModificationTimestamp(), null);
   
            m_transaction.begin();
            try
            {
               // try to modify domain without assigned default roles
               testDomain1 = m_domainControl.save(domain2, "", "");
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            compareDomains(testDomain1, domain2);
            // Now we need to test if in the database is what we have wrote there
            testDomain2 = (Domain)m_domainFactory.get(
                                     testDomain1.getId(),
                                     CallContext.getInstance().getCurrentDomainId());
            compareDomains(testDomain2, domain2);
            
            roles = m_roleFactory.getAllForDomain(testDomain1.getId(), SimpleRule.ALL_DATA);
            //assertNull("No role should be assigned", roles);
            
            // ----------------------------------------------------------------
            // assign role1, role2, role3 to domain1
            String strDomainRoles = role1.getId() + "," + role2.getId() + "," + role3.getId();
            m_roleFactory.assignToDomain(domain1.getId(), strDomainRoles);
            
            // Now try to modify domain with roles:
            // - remove role1 and role3
            // - add role4, role5, role6 
            String strRolesRemove = role1.getId() + "," + role3.getId();
            String strRolesAdd = role4.getId() + "," + role5.getId() + "," + role6.getId();

            testDomain2 = testDomain1;
            m_transaction.begin();
            try
            {
               // try to modify domain with roles
               testDomain2 = m_domainControl.save(testDomain2, strRolesRemove, strRolesAdd);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            compareDomains(testDomain1, domain2);
            // Now we need to test if in the database is what we have wrote there
            testDomain1 = (Domain)m_domainFactory.get(
                                     testDomain1.getId(),
                                     CallContext.getInstance().getCurrentDomainId());
            compareDomains(testDomain1, testDomain2);
   
            roles = m_roleFactory.getAllForDomain(testDomain1.getId(), SimpleRule.ALL_DATA);
            assertNotNull("Role should be assigned", roles);
            assertEquals("Multiple roles should be assigned", 4, roles.size());
            assertTrue("Role1 should be assigned", 
                       role2.equals(roles.get(0))
                       || role2.equals(roles.get(1))
                       || role2.equals(roles.get(2))
                       || role2.equals(roles.get(3)));
            assertTrue("Role4 should be assigned", 
                       role4.equals(roles.get(0))
                       || role4.equals(roles.get(1))
                       || role4.equals(roles.get(2))
                       || role4.equals(roles.get(3)));
            assertTrue("Role5 should be assigned", 
                       role5.equals(roles.get(0))
                       || role5.equals(roles.get(1))
                       || role5.equals(roles.get(2))
                       || role5.equals(roles.get(3)));
            assertTrue("Role6 should be assigned", 
                       role6.equals(roles.get(0))
                       || role6.equals(roles.get(1))
                       || role6.equals(roles.get(2))
                       || role6.equals(roles.get(3)));
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
                  m_roleFactoryUtils.deleteRoleCascadeManual(role1.getId(), domain1.getId());
               }
               if ((role2 != null) && (role2.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(role2.getId(), domain1.getId());
               }
               if ((role3 != null) && (role3.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(role3.getId(), domain1.getId());
               }
               if ((role4 != null) && (role4.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(role4.getId(), domain1.getId());
               }
               if ((role5 != null) && (role5.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(role5.getId(), domain1.getId());
               }
               if ((role6 != null) && (role6.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(role6.getId(), domain1.getId());
               }
               if ((domain1 != null) && (domain1.getId() != DataObject.NEW_ID))
               {
                  m_domainFactory.delete(
                                     domain1.getId(),
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
       * Compare two domains. 
       * 
       * @param testDomain - domain which should be tested
       * @param templateDomain - domain which serves as template
       */
      protected void compareDomains(
         Domain testDomain,
         Domain templateDomain
      )
      {
         assertNotNull("Domain should not be null", testDomain);
         assertEquals("Domain name is incorrect", 
                      templateDomain.getName(), testDomain.getName());
         assertEquals("Domain description is incorrect", 
                      templateDomain.getDescription(), testDomain.getDescription());
         assertEquals("Domain status is incorrect", 
                      templateDomain.isEnabled(), testDomain.isEnabled());
         assertEquals("Domain administration flag is incorrect", 
                      templateDomain.isAdministration(), testDomain.isAdministration());
         assertEquals("Domain allow self registration flag is incorrect", 
                      templateDomain.isAllowSelfRegistration(), 
                      testDomain.isAllowSelfRegistration());
         assertEquals("Domain default phone number is incorrect", 
                      templateDomain.getDefaultPhone(), testDomain.getDefaultPhone());
         assertEquals("Domain default address is incorrect", 
                      templateDomain.getDefaultAddress(), testDomain.getDefaultAddress());
         assertEquals("Domain deafult login enabled status is incorrect", 
                      templateDomain.isDefaultLoginEnabled(), 
                      testDomain.isDefaultLoginEnabled());
         assertEquals("Domain dafault superuser flag is incorrect", 
                      templateDomain.isDefaultSuperUser(),
                      testDomain.isDefaultSuperUser());
         assertEquals("Domain default internal user flag is incorrect", 
                      templateDomain.isDefaultInternalUser(), 
                      testDomain.isDefaultInternalUser());
      }
   }
}
