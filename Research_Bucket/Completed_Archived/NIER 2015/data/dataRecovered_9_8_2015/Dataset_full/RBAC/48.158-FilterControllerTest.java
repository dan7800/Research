/*
 * Copyright (c) 2003 - 2009 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: FilterControllerTest.java,v 1.22 2009/09/20 05:32:59 bastafidli Exp $
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

package org.opensubsystems.search.logic;

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
import org.opensubsystems.patterns.listdata.data.DataCondition;
import org.opensubsystems.search.application.SearchBackendModule;
import org.opensubsystems.search.data.Filter;
import org.opensubsystems.search.data.FilterCondition;
import org.opensubsystems.search.persist.FilterConditionFactory;
import org.opensubsystems.search.persist.FilterFactory;
import org.opensubsystems.search.persist.db.FilterConditionDatabaseFactory;
import org.opensubsystems.search.persist.db.FilterDatabaseFactory;
import org.opensubsystems.search.persist.db.FilterListDatabaseTestUtils;
import org.opensubsystems.security.data.User;
import org.opensubsystems.security.patterns.listdata.persist.db.SecureListDatabaseFactoryTest;
import org.opensubsystems.security.persist.UserFactory;
import org.opensubsystems.security.persist.db.UserDatabaseFactory;

/**
 * Test for FilterController interface implementation class. 
 * 
 * @version $Id: FilterControllerTest.java,v 1.22 2009/09/20 05:32:59 bastafidli Exp $
 * @author Julian Legeny
 * @code.reviewer 
 * @code.reviewed TODO: review this code
 */
public final class FilterControllerTest extends Tests
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private FilterControllerTest(
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
      TestSuite suite = new DatabaseTestSuite("FilterControllerTest");
      suite.addTestSuite(FilterControllerTestInternal.class);
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
   public static class FilterControllerTestInternal extends SecureListDatabaseFactoryTest 
   {
      // Cached values ////////////////////////////////////////////////////////////
   
      /**
       * Controller to manage filters.
       */
      protected FilterController m_filterControl;
   
      /**
       * Factory to manage filters.
       */
      protected FilterDatabaseFactory m_filterFactory;
      
      /**
       * Factory to manage filter conditions (criteria).
       */
      protected FilterConditionDatabaseFactory m_filterConditionFactory;
      
      /**
       * Factory to manage users.
       */
      protected UserDatabaseFactory m_userFactory;
   
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
            app.add(ModuleManager.getInstance(SearchBackendModule.class));
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
      public FilterControllerTestInternal(
         String strTestName
      ) throws Exception
      {
         super(strTestName, new FilterListDatabaseTestUtils());         
   
         m_filterControl = (FilterController)ControllerManager.getInstance(
                                                   FilterController.class);
         m_filterFactory = (FilterDatabaseFactory)DataFactoryManager.getInstance(
                                                     FilterFactory.class);
         m_filterConditionFactory = (FilterConditionDatabaseFactory)DataFactoryManager.getInstance(
                                                     FilterConditionFactory.class);
         m_userFactory = (UserDatabaseFactory)DataFactoryManager.getInstance(
                                                     UserFactory.class);
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
         User   user       = null;
         Filter filter1    = null;
         Filter filter2    = null;
         Filter testFilter = null;
         FilterCondition filterCondition11 = null;
         FilterCondition filterCondition12 = null;
         FilterCondition filterCondition21 = null;
   
         try
         {
            m_transaction.begin();
            try
            {
               // create test user
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
                                true, true, true, true, null, null, true
                               );
               user = (User)m_userFactory.create(user);
               
               // create 2 test filters
               filter1 = new Filter(DataObject.NEW_ID,
                                   CallContext.getInstance().getCurrentDomainId(),
                                   user.getId(),
                                   "test_filter_name_1",
                                   "test_filter_description_1",
                                   1, false, false, 10, false, false,
                                   "", "", null, null, null
                                  );
               filter1 = (Filter)m_filterFactory.create(filter1);
   
               filterCondition11 = new FilterCondition(
                                        DataObject.NEW_ID,
                                        CallContext.getInstance().getCurrentDomainId(),
                                        filter1.getId(),
                                        100, 1, "test_fc_value_11",
                                        DataCondition.VALUE_TYPE_INTEGER, 1,
                                        null, null
                                       );
               filterCondition11 = (FilterCondition)m_filterConditionFactory.create(
                                                        filterCondition11);
   
               filterCondition12 = new FilterCondition(
                                        DataObject.NEW_ID,
                                        CallContext.getInstance().getCurrentDomainId(),
                                        filter1.getId(),
                                        200, 2, "test_fc_value_12",
                                        DataCondition.VALUE_TYPE_BOOLEAN, 2,
                                        null, null
                                       );
               filterCondition12 = (FilterCondition)m_filterConditionFactory.create(
                                                        filterCondition12);
   
               filter2 = new Filter(DataObject.NEW_ID,
                                   CallContext.getInstance().getCurrentDomainId(),
                                   user.getId(),
                                   "test_filter_name_2",
                                   "test_filter_description_2",
                                   2, false, false, 20, false, false,
                                   "", "", null, null, null
                                  );
               filter2 = (Filter)m_filterFactory.create(filter2);
   
               filterCondition21 = new FilterCondition(
                                        DataObject.NEW_ID,
                                        CallContext.getInstance().getCurrentDomainId(),
                                        filter2.getId(),
                                        300, 3, "test_fc_value_21",
                                        DataCondition.VALUE_TYPE_STRING, 1,
                                        null, null
                                       );
               filterCondition21 = (FilterCondition)m_filterConditionFactory.create(
                                                        filterCondition21);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            //try to get filter by ID
            testFilter = (Filter)m_filterControl.get(filter1.getId());
   
            assertNotNull("Filter should not be null", testFilter);
            assertEquals("Filter is not the same", filter1, testFilter);
   
            assertNotNull("Filter do not have filter conditions", testFilter.getCriteria());
            assertEquals("Number of filter conditions is not correct", 2, 
                         testFilter.getCriteria().size());
            assertTrue("Filter condition 11 is not loaded",
                       filterCondition11.equals(testFilter.getCriteria().get(0))
                       || filterCondition11.equals(testFilter.getCriteria().get(1)));
            assertTrue("Filter Condition 12 is not loaded",
                       filterCondition12.equals(testFilter.getCriteria().get(0))
                       || filterCondition12.equals(testFilter.getCriteria().get(1)));
         }
         finally
         {
            // delete inserted records
            if ((filterCondition11 != null) && (filterCondition11.getId() != DataObject.NEW_ID))
            {
               // delete filter condition
               m_filterConditionFactory.delete(filter1.getId(), 
                                               Integer.toString(filterCondition11.getId()));
            }
            if ((filterCondition12 != null) && (filterCondition12.getId() != DataObject.NEW_ID))
            {
               // delete filter condition
               m_filterConditionFactory.delete(filter1.getId(), 
                                               Integer.toString(filterCondition12.getId()));
            }
            if ((filterCondition21 != null) && (filterCondition21.getId() != DataObject.NEW_ID))
            {
               // delete filter condition
               m_filterConditionFactory.delete(filter2.getId(), 
                                               Integer.toString(filterCondition21.getId()));
            }
            if ((filter1 != null) && (filter1.getId() != DataObject.NEW_ID))
            {
               m_transaction.begin();
               // delete inserted records
               try
               {
                  // delete filter 1
                  m_filterFactory.delete(
                     filter1.getId(),
                     CallContext.getInstance().getCurrentDomainId());
                  m_transaction.commit();
               }
               catch (Throwable thr)
               {
                  m_transaction.rollback();
                  throw new Exception(thr);
               }
            }
            if ((filter2 != null) && (filter2.getId() != DataObject.NEW_ID))
            {
               m_transaction.begin();
               // delete inserted records
               try
               {
                  // delete filter 2
                  m_filterFactory.delete(
                     filter2.getId(),
                     CallContext.getInstance().getCurrentDomainId());
                  m_transaction.commit();
               }
               catch (Throwable thr)
               {
                  m_transaction.rollback();
                  throw new Exception(thr);
               }
            }
            if ((user != null) && (user.getId() != DataObject.NEW_ID))
            {
               m_transaction.begin();
               // delete inserted records
               try
               {
                  // delete user
                  m_userFactory.delete(
                     user.getId(),
                     CallContext.getInstance().getCurrentDomainId());
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
      
      /**
       * Test of create methods in controller
       * 
       * @throws Exception - an error has occurred
       */
      public void testCreate(
      ) throws Exception
      {
         User   user       = null;
         Filter filter     = null;
         Filter testFilter = null;
         FilterCondition filterCondition11 = null;
         FilterCondition filterCondition12 = null;
         
         try
         {
            m_transaction.begin();
            try
            {
               // create test user
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
   
            m_transaction.begin();
            try
            {
               // create test filter
               filter = new Filter(DataObject.NEW_ID,
                                   CallContext.getInstance().getCurrentDomainId(),
                                   user.getId(),
                                   "test_filter_name_1",
                                   "test_filter_description_1",
                                   1, false, false, 10, false, false,
                                   "", "", null, null, null
                                  );
               // create 2 test filter conditions
               filterCondition11 = new FilterCondition(
                                        DataObject.NEW_ID,
                                        CallContext.getInstance().getCurrentDomainId(),
                                        filter.getId(),
                                        100, 1, "test_fc_value_11",
                                        DataCondition.VALUE_TYPE_INTEGER, 1,
                                        null, null
                                       );
            
               filterCondition12 = new FilterCondition(
                                        DataObject.NEW_ID,
                                        CallContext.getInstance().getCurrentDomainId(),
                                        filter.getId(),
                                        200, 2, "test_fc_value_12",
                                        DataCondition.VALUE_TYPE_BOOLEAN, 2,
                                        null, null
                                       );
               // create list of filter conditions and assign them to the filter
               List lstFilterConditions = new ArrayList();
               lstFilterConditions.add(filterCondition11);
               lstFilterConditions.add(filterCondition12);
               filter.setCriteria(lstFilterConditions);
               
               // try to create filter together with assigned filter conditions
               testFilter = (Filter)m_filterControl.create(filter);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            //try to get filter by ID
            testFilter = (Filter)m_filterControl.get(filter.getId());
   
            assertNotNull("Filter should not be null", testFilter);
            assertEquals("Filter is not the same", filter, testFilter);
   
            assertNotNull("Filter do not have filter conditions", testFilter.getCriteria());
            assertEquals("Number of filter conditions is not correct", 2, 
                         testFilter.getCriteria().size());
            assertTrue("Filter condition 11 is not loaded",
                       filterCondition11.equals(testFilter.getCriteria().get(0))
                       || filterCondition11.equals(testFilter.getCriteria().get(1)));
            assertTrue("Filter Condition 12 is not loaded",
                       filterCondition12.equals(testFilter.getCriteria().get(0))
                       || filterCondition12.equals(testFilter.getCriteria().get(1)));
         }
         finally
         {
            // delete inserted records
            if ((filterCondition11 != null) && (filterCondition11.getId() != DataObject.NEW_ID))
            {
               // delete filter condition
               m_filterConditionFactory.delete(filter.getId(), 
                                               Integer.toString(filterCondition11.getId()));
            }
            if ((filterCondition12 != null) && (filterCondition12.getId() != DataObject.NEW_ID))
            {
               // delete filter condition
               m_filterConditionFactory.delete(filter.getId(), 
                                               Integer.toString(filterCondition12.getId()));
            }
            if ((testFilter != null) && (testFilter.getId() != DataObject.NEW_ID))
            {
               m_transaction.begin();
               // delete inserted records
               try
               {
                  // delete filter 1
                  m_filterFactory.delete(
                     testFilter.getId(),
                     CallContext.getInstance().getCurrentDomainId());
                  m_transaction.commit();
               }
               catch (Throwable thr)
               {
                  m_transaction.rollback();
                  throw new Exception(thr);
               }
            }
            if ((user != null) && (user.getId() != DataObject.NEW_ID))
            {
               m_transaction.begin();
               // delete inserted records
               try
               {
                  // delete user
                  m_userFactory.delete(
                     user.getId(),
                     CallContext.getInstance().getCurrentDomainId());
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
      
      /**
       * Test of save role with differences methods in controller
       * 
       * @throws Exception - an error has occurred
       */
      public void testSave(
      ) throws Exception
      {
         User   user       = null;
         Filter filter1    = null;
         Filter filter11   = null;
         Filter filter2    = null;
         Filter testFilter = null;
         FilterCondition filterCondition11 = null;
         FilterCondition filterCondition12 = null;
         FilterCondition filterCondition13 = null;
         FilterCondition filterCondition14 = null;
         FilterCondition filterCondition21 = null;
         FilterCondition testFilterCondition = null;
         List lstFilterConditions  = new ArrayList();
         List lstFilter1Conditions = new ArrayList();
   
         try
         {
            m_transaction.begin();
            try
            {
               // create test user
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
                                true, true, true, true, null, null, true
                               );
               user = (User)m_userFactory.create(user);
               
               // create 2 test filters
               filter1 = new Filter(DataObject.NEW_ID,
                                   CallContext.getInstance().getCurrentDomainId(),
                                   user.getId(),
                                   "test_filter_name_1",
                                   "test_filter_description_1",
                                   1, false, false, 10, false, false,
                                   "", "", null, null, null
                                  );
               filter1 = (Filter)m_filterFactory.create(filter1);
   
               filterCondition11 = new FilterCondition(
                                        DataObject.NEW_ID,
                                        CallContext.getInstance().getCurrentDomainId(),
                                        filter1.getId(),
                                        100, 1, "test_fc_value_11",
                                        DataCondition.VALUE_TYPE_INTEGER, 1,
                                        null, null
                                       );
               filterCondition11 = (FilterCondition)m_filterConditionFactory.create(
                                      filterCondition11);
   
               filterCondition12 = new FilterCondition(
                                        DataObject.NEW_ID,
                                        CallContext.getInstance().getCurrentDomainId(),
                                        filter1.getId(),
                                        200, 2, "test_fc_value_12",
                                        DataCondition.VALUE_TYPE_BOOLEAN, 2,
                                        null, null
                                       );
               filterCondition12 = (FilterCondition)m_filterConditionFactory.create(
                                      filterCondition12);
   
               lstFilter1Conditions.add(filterCondition11);
               lstFilter1Conditions.add(filterCondition12);
               filter1.setCriteria(lstFilter1Conditions);
   
               filter2 = new Filter(DataObject.NEW_ID,
                                   CallContext.getInstance().getCurrentDomainId(),
                                   user.getId(),
                                   "test_filter_name_2",
                                   "test_filter_description_2",
                                   2, false, false, 20, false, false,
                                   "", "", null, null, null
                                  );
               filter2 = (Filter)m_filterFactory.create(filter2);
   
               filterCondition21 = new FilterCondition(
                                        DataObject.NEW_ID,
                                        CallContext.getInstance().getCurrentDomainId(),
                                        filter2.getId(),
                                        300, 3, "test_fc_value_21",
                                        DataCondition.VALUE_TYPE_STRING, 1,
                                        null, null
                                       );
               filterCondition21 = (FilterCondition)m_filterConditionFactory.create(
                                                        filterCondition21);
   
   
               filterCondition13 = new FilterCondition(
                                        DataObject.NEW_ID,
                                        CallContext.getInstance().getCurrentDomainId(),
                                        filter1.getId(),
                                        400, 4, "test_fc_value_13",
                                        DataCondition.VALUE_TYPE_DOUBLE, 3,
                                        null, null
                                       );
               filterCondition14 = new FilterCondition(
                                        DataObject.NEW_ID,
                                        CallContext.getInstance().getCurrentDomainId(),
                                        filter1.getId(),
                                        500, 5, "test_fc_value_14",
                                        DataCondition.VALUE_TYPE_TIMESTAMP, 4,
                                        null, null
                                       );
   
               lstFilterConditions.add(filterCondition13);
               lstFilterConditions.add(filterCondition14);
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            Thread.sleep(100);
            
            filter11 = new Filter(filter1.getId(),
                                  filter1.getDomainId(),
                                  filter1.getUserId(),
                                  "test_filter_name_11",
                                  "test_filter_description_11",
                                  1, false, false, 10, false, false,
                                  "", "",
                                  filter1.getCreationTimestamp(),
                                  filter1.getModificationTimestamp(),
                                  lstFilterConditions
                               );

            m_transaction.begin();
            try 
            {
               // This should remove the two original filter conditions and replace them with 
               // two new ones
               testFilter = (Filter)m_filterControl.save(filter11);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            Thread.sleep(100);
   
            assertNotNull("Filter should not be null", testFilter);
            assertEquals("Filter is not the same", filter11, testFilter);
   
            assertNotNull("Filter do not have filter conditions", testFilter.getCriteria());
            assertEquals("Number of filter conditions is not correct", 2, 
                         testFilter.getCriteria().size());
   
            assertTrue("Filter condition 13 is not loaded",
                       filterCondition13.equals(testFilter.getCriteria().get(0))
                       || filterCondition13.equals(testFilter.getCriteria().get(1)));
            assertTrue("Filter Condition 14 is not loaded",
                       filterCondition14.equals(testFilter.getCriteria().get(0))
                       || filterCondition14.equals(testFilter.getCriteria().get(1)));
            
            testFilterCondition = (FilterCondition)m_filterConditionFactory.get(
                                     filterCondition11.getId(), 
                                     CallContext.getInstance().getCurrentDomainId());
            assertNull("Filter condition should be deleted", testFilterCondition);
            filterCondition11 = null;
            testFilterCondition = (FilterCondition)m_filterConditionFactory.get(
                                     filterCondition12.getId(),
                                     CallContext.getInstance().getCurrentDomainId());
            assertNull("Filter condition should be deleted", testFilterCondition);
            filterCondition12 = null;
            
            // Make sure the other filter didn't changed
            Filter testFilter2 = (Filter)m_filterControl.get(filter2.getId());
            
            assertNotNull("Filter is null", testFilter2);
            assertEquals("Filter is not the same", filter2, testFilter2);
            assertNotNull("Filter do not have filter condition", testFilter2.getCriteria());
            assertEquals("Number of filter conditions is not correct", 1, 
                         testFilter2.getCriteria().size());
            assertTrue("Filter condition is not loaded",
                       filterCondition21.equals(testFilter2.getCriteria().get(0)));
         }
         finally
         {
            if ((filterCondition11 != null) && (filterCondition11.getId() != DataObject.NEW_ID))
            {
               // delete filter condition
               m_filterConditionFactory.delete(filter1.getId(), 
                                               Integer.toString(filterCondition11.getId()));
            }
            if ((filterCondition12 != null) && (filterCondition12.getId() != DataObject.NEW_ID))
            {
               // delete filter condition
               m_filterConditionFactory.delete(filter1.getId(), 
                                               Integer.toString(filterCondition12.getId()));
            }
            if ((filterCondition13 != null) && (filterCondition13.getId() != DataObject.NEW_ID))
            {
               // delete filter condition
               m_filterConditionFactory.delete(filter1.getId(), 
                                               Integer.toString(filterCondition13.getId()));
            }
            if ((filterCondition14 != null) && (filterCondition14.getId() != DataObject.NEW_ID))
            {
               // delete filter condition
               m_filterConditionFactory.delete(filter1.getId(), 
                                               Integer.toString(filterCondition14.getId()));
            }
            if ((filterCondition21 != null) && (filterCondition21.getId() != DataObject.NEW_ID))
            {
               // delete filter condition
               m_filterConditionFactory.delete(filter2.getId(), 
                                               Integer.toString(filterCondition21.getId()));
            }
            if ((filter1 != null) && (filter1.getId() != DataObject.NEW_ID))
            {
               m_transaction.begin();
               // delete inserted records
               try
               {
                  // delete filter 1
                  m_filterFactory.delete(
                                     filter1.getId(),
                                     CallContext.getInstance().getCurrentDomainId());
                  m_transaction.commit();
               }
               catch (Throwable thr)
               {
                  m_transaction.rollback();
                  throw new Exception(thr);
               }
            }
            if ((filter2 != null) && (filter2.getId() != DataObject.NEW_ID))
            {
               m_transaction.begin();
               // delete inserted records
               try
               {
                  // delete filter 2
                  m_filterFactory.delete(
                                     filter2.getId(),
                                     CallContext.getInstance().getCurrentDomainId());
                  m_transaction.commit();
               }
               catch (Throwable thr)
               {
                  m_transaction.rollback();
                  throw new Exception(thr);
               }
            }
            if ((user != null) && (user.getId() != DataObject.NEW_ID))
            {
               m_transaction.begin();
               // delete inserted records
               try
               {
                  // delete user
                  m_userFactory.delete(
                                   user.getId(),
                                   CallContext.getInstance().getCurrentDomainId());
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
   
      /**
       * Test of save with differences methods in controller
       * 
       * @throws Exception - an error has occurred
       */
      public void testSaveWithDifferences(
      ) throws Exception
      {
         User   user       = null;
         Filter filter1    = null;
         Filter filter2    = null;
         Filter testFilter = null;
         FilterCondition filterCondition11 = null;
         FilterCondition filterCondition12 = null;
         FilterCondition filterCondition13 = null;
         FilterCondition filterCondition14 = null;
         FilterCondition filterCondition15 = null;
         FilterCondition filterCondition12up = null;
         FilterCondition filterCondition21 = null;
         FilterCondition testFilterCondition = null;
         List lstFilterConditionsAdd    = new ArrayList();
         List lstFilterConditionsUpdate = new ArrayList();
   
         try
         {
            m_transaction.begin();
            try
            {
               // create test user
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
                                true, true, true, true, null, null, true
                               );
            
               user = (User)m_userFactory.create(user);
               
               // create 2 test filters
               filter1 = new Filter(DataObject.NEW_ID,
                                   CallContext.getInstance().getCurrentDomainId(),
                                   user.getId(),
                                   "test_filter_name_1",
                                   "test_filter_description_1",
                                   1, false, false, 10, false, false,
                                   "", "", null, null, null
                                  );
               filter1 = (Filter)m_filterFactory.create(filter1);
   
               filterCondition11 = new FilterCondition(
                                        DataObject.NEW_ID,
                                        CallContext.getInstance().getCurrentDomainId(),
                                        filter1.getId(),
                                        100, 1, "test_fc_value_11",
                                        DataCondition.VALUE_TYPE_INTEGER, 1,
                                        null, null
                                       );
               filterCondition11 = (FilterCondition)m_filterConditionFactory.create(
                                      filterCondition11);
   
               filterCondition12 = new FilterCondition(
                                        DataObject.NEW_ID,
                                        CallContext.getInstance().getCurrentDomainId(),
                                        filter1.getId(),
                                        200, 2, "test_fc_value_12",
                                        DataCondition.VALUE_TYPE_BOOLEAN, 2,
                                        null, null
                                       );
               filterCondition12 = (FilterCondition)m_filterConditionFactory.create(
                                      filterCondition12);
               filterCondition15 = new FilterCondition(
                                        DataObject.NEW_ID,
                                        CallContext.getInstance().getCurrentDomainId(),
                                        filter1.getId(),
                                        500, 5, "test_fc_value_15",
                                        DataCondition.VALUE_TYPE_BOOLEAN, 5,
                                        null, null
                                       );
               filterCondition15 = (FilterCondition)m_filterConditionFactory.create(
                                      filterCondition15);

               filter2 = new Filter(DataObject.NEW_ID,
                                   CallContext.getInstance().getCurrentDomainId(),
                                   user.getId(),
                                   "test_filter_name_2",
                                   "test_filter_description_2",
                                   2, false, false, 20, false, false,
                                   "", "", null, null, null
                                  );
               filter2 = (Filter)m_filterFactory.create(filter2);
   
               filterCondition21 = new FilterCondition(
                                        DataObject.NEW_ID,
                                        CallContext.getInstance().getCurrentDomainId(),
                                        filter2.getId(),
                                        300, 3, "test_fc_value_21",
                                        DataCondition.VALUE_TYPE_STRING, 1, 
                                        null, null
                                       );
               filterCondition21 = (FilterCondition)m_filterConditionFactory.create(
                                                        filterCondition21);
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            Thread.sleep(1000);
            
            // Update some attributes of the filter
            filter1 = new Filter(filter1.getId(),
                                 filter1.getDomainId(),
                                 filter1.getUserId(),
                                 "test_filter_name_11",
                                 "test_filter_description_11",
                                 1, false, false, 10, false, false,
                                 "", "",
                                 filter1.getCreationTimestamp(),
                                 filter1.getModificationTimestamp(),
                                 filter1.getCriteria()
                               );
            
            // Create condition that will update existing one
            filterCondition12up = new FilterCondition(
                                     filterCondition12.getId(),
                                     CallContext.getInstance().getCurrentDomainId(),
                                     filter1.getId(), 
                                     210, 21, "test_fc_value_12up",
                                     DataCondition.VALUE_TYPE_BOOLEAN, 2,
                                     null, null
                                    );
            // Create two new conditions 
            filterCondition13 = new FilterCondition(
                                     DataObject.NEW_ID,
                                     CallContext.getInstance().getCurrentDomainId(),
                                     filter1.getId(),
                                     400, 4, "test_fc_value_13",
                                     DataCondition.VALUE_TYPE_DOUBLE, 3,
                                     null, null
                                    );
            filterCondition14 = new FilterCondition(
                                     DataObject.NEW_ID,
                                     CallContext.getInstance().getCurrentDomainId(),
                                     filter1.getId(), 
                                     500, 5, "test_fc_value_14",
                                     DataCondition.VALUE_TYPE_TIMESTAMP, 4,
                                     null, null
                                    );

            lstFilterConditionsAdd.add(filterCondition13);
            lstFilterConditionsAdd.add(filterCondition14);
            lstFilterConditionsUpdate.add(filterCondition12up);
            lstFilterConditionsUpdate.add(filterCondition15);

            m_transaction.begin();
            try 
            {
               // delete one filter condition (fc11), 
               // add 2 new filter conditions (fc13, fc14),
               // modify 2 existing filter conditions (fc2 and fc5)
               testFilter = (Filter)m_filterControl.save(filter1, 
                                Integer.toString(filterCondition11.getId()),
                                lstFilterConditionsAdd, lstFilterConditionsUpdate);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            Thread.sleep(1000);
   
            assertNotNull("Filter should not be null", testFilter);
            assertEquals("Filter is not the same", filter1, testFilter);
   
            assertNotNull("Filter does not have any filter conditions", 
                          testFilter.getCriteria());
            assertEquals("Number of filter conditions is not correct", 4, 
                         testFilter.getCriteria().size());
   
            // TODO: For Miro: Some of this comparison uses isSame and some is
            // using equals. Review these and other tests to determine what 
            // should be used
            
            assertTrue("Filter condition 12 updated is not loaded",
                       filterCondition12up.isSame(testFilter.getCriteria().get(0))
                       || filterCondition12up.isSame(testFilter.getCriteria().get(1))
                       || filterCondition12up.isSame(testFilter.getCriteria().get(2))
                       || filterCondition12up.isSame(testFilter.getCriteria().get(3)));
            assertTrue("Filter condition 13 is not loaded",
                       filterCondition13.equals(testFilter.getCriteria().get(0))
                       || filterCondition13.equals(testFilter.getCriteria().get(1))
                       || filterCondition13.equals(testFilter.getCriteria().get(2))
                       || filterCondition13.equals(testFilter.getCriteria().get(3)));
            assertTrue("Filter Condition 14 is not loaded",
                       filterCondition14.equals(testFilter.getCriteria().get(0))
                       || filterCondition14.equals(testFilter.getCriteria().get(1))
                       || filterCondition14.equals(testFilter.getCriteria().get(2))
                       || filterCondition14.equals(testFilter.getCriteria().get(3)));
            assertTrue("Filter Condition 15 is not loaded",
                       filterCondition15.isSame(testFilter.getCriteria().get(0))
                       || filterCondition15.isSame(testFilter.getCriteria().get(1))
                       || filterCondition15.isSame(testFilter.getCriteria().get(2))
                       || filterCondition15.isSame(testFilter.getCriteria().get(3)));
            
            testFilterCondition = (FilterCondition)m_filterConditionFactory.get(
                                     filterCondition11.getId(),
                                     CallContext.getInstance().getCurrentDomainId());
            assertNull("Filter condition should be deleted", testFilterCondition);
            filterCondition11 = null;
            testFilterCondition = (FilterCondition)m_filterConditionFactory.get(
                                     filterCondition12.getId(),
                                     CallContext.getInstance().getCurrentDomainId());
   
            // Make sure the other filter didn't changed
            Filter testFilter2 = (Filter)m_filterControl.get(filter2.getId());
            
            assertNotNull("Filter is null", testFilter2);
            assertEquals("Filter is not the same", filter2, testFilter2);
            assertNotNull("Filter do not have filter condition", testFilter2.getCriteria());
            assertEquals("Number of filter conditions is not correct", 1, 
                         testFilter2.getCriteria().size());
            assertTrue("Filter condition is not loaded",
                       filterCondition21.equals(testFilter2.getCriteria().get(0)));
         }
         finally
         {
            if ((filterCondition11 != null) && (filterCondition11.getId() != DataObject.NEW_ID))
            {
               m_transaction.begin();
               // delete filter condition
               try
               {
                  m_filterConditionFactory.delete(filter1.getId(), 
                                                  Integer.toString(filterCondition11.getId()));
                  m_transaction.commit();
               }
               catch (Throwable thr)
               {
                  m_transaction.rollback();
                  throw new Exception(thr);
               }
            }
            if ((filterCondition12 != null) && (filterCondition12.getId() != DataObject.NEW_ID))
            {
               m_transaction.begin();
               // delete filter condition
               try
               {
                  m_filterConditionFactory.delete(filter1.getId(), 
                                                  Integer.toString(filterCondition12.getId()));
                  m_transaction.commit();
               }
               catch (Throwable thr)
               {
                  m_transaction.rollback();
                  throw new Exception(thr);
               }
            }
            if ((filterCondition13 != null) && (filterCondition13.getId() != DataObject.NEW_ID))
            {
               m_transaction.begin();
               // delete filter condition
               try
               {
                  m_filterConditionFactory.delete(filter1.getId(), 
                                                  Integer.toString(filterCondition13.getId()));
                  m_transaction.commit();
               }
               catch (Throwable thr)
               {
                  m_transaction.rollback();
                  throw new Exception(thr);
               }
            }
            if ((filterCondition14 != null) && (filterCondition14.getId() != DataObject.NEW_ID))
            {
               m_transaction.begin();
               // delete filter condition
               try
               {
                  m_filterConditionFactory.delete(filter1.getId(), 
                                                  Integer.toString(filterCondition14.getId()));
                  m_transaction.commit();
               }
               catch (Throwable thr)
               {
                  m_transaction.rollback();
                  throw new Exception(thr);
               }
            }
            if ((filterCondition21 != null) && (filterCondition21.getId() != DataObject.NEW_ID))
            {
               m_transaction.begin();
               // delete filter condition
               try
               {
                  m_filterConditionFactory.delete(filter2.getId(), 
                                                  Integer.toString(filterCondition21.getId()));
                  m_transaction.commit();
               }
               catch (Throwable thr)
               {
                  m_transaction.rollback();
                  throw new Exception(thr);
               }
            }
            if ((filter1 != null) && (filter1.getId() != DataObject.NEW_ID))
            {
               m_transaction.begin();
               // delete inserted records
               try
               {
                  // delete all filter conditions belonging to this filter
                  // this is necessary for Sybase ASE database
                  m_filterConditionFactory.deleteAllForFilter(filter1.getId());
                  // delete filter 1
                  m_filterFactory.delete(
                                     filter1.getId(),
                                     CallContext.getInstance().getCurrentDomainId());
                  m_transaction.commit();
               }
               catch (Throwable thr)
               {
                  m_transaction.rollback();
                  throw new Exception(thr);
               }
            }
            if ((filter2 != null) && (filter2.getId() != DataObject.NEW_ID))
            {
               m_transaction.begin();
               // delete inserted records
               try
               {
                  // delete all filter conditions belonging to this filter
                  // this is necessary for Sybase ASE database
                  m_filterConditionFactory.deleteAllForFilter(filter2.getId());
                  // delete filter 2
                  m_filterFactory.delete(
                                     filter2.getId(),
                                     CallContext.getInstance().getCurrentDomainId());
                  m_transaction.commit();
               }
               catch (Throwable thr)
               {
                  m_transaction.rollback();
                  throw new Exception(thr);
               }
            }
            if ((user != null) && (user.getId() != DataObject.NEW_ID))
            {
               m_transaction.begin();
               // delete inserted records
               try
               {
                  // delete user
                  m_userFactory.delete(
                                   user.getId(),
                                   CallContext.getInstance().getCurrentDomainId());
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
      
      /**
       * Test of delete role methods in controller
       * 
       * @throws Exception - an error has occurred
       */
      public void testDelete(
      ) throws Exception
      {
         User   user       = null;
         Filter filter     = null;
         Filter testFilter = null;
         FilterCondition filterCondition11 = null;
         FilterCondition filterCondition12 = null;
         List lstFilterConditions = new ArrayList();
         
         try
         {
            m_transaction.begin();
            try
            {
               // create test user
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
                                true, true, true, true, null, null, true
                               );
               user = (User)m_userFactory.create(user);
               
               // create test filter
               filter = new Filter(DataObject.NEW_ID,
                                   CallContext.getInstance().getCurrentDomainId(),
                                   user.getId(),
                                   "test_filter_name_1",
                                   "test_filter_description_1",
                                   1, false, false, 10, false, false,
                                   "", "", null, null, null
                                  );
               filter = (Filter)m_filterFactory.create(filter);
            
               filterCondition11 = new FilterCondition(
                                        DataObject.NEW_ID,
                                        CallContext.getInstance().getCurrentDomainId(),
                                        filter.getId(),
                                        100, 1, "test_fc_value_11",
                                        DataCondition.VALUE_TYPE_INTEGER, 1,
                                        null, null
                                       );
               filterCondition11 = (FilterCondition)m_filterConditionFactory.create(
                                      filterCondition11);
               
               filterCondition12 = new FilterCondition(
                                        DataObject.NEW_ID,
                                        CallContext.getInstance().getCurrentDomainId(),
                                        filter.getId(),
                                        200, 2, "test_fc_value_12",
                                        DataCondition.VALUE_TYPE_BOOLEAN, 2,
                                        null, null
                                       );
               filterCondition12 = (FilterCondition)m_filterConditionFactory.create(
                                      filterCondition12);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            
            lstFilterConditions = m_filterConditionFactory.getAllForFilter(filter.getId());
            assertEquals("Incorrect number of filter conditions assigned to the filter", 
                         2, lstFilterConditions.size());
            
            // try to delete filter by ID
            m_transaction.begin();
            try
            {
               m_filterControl.delete(filter.getId());
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            
            testFilter = (Filter)m_filterFactory.get(
                                    filter.getId(),
                                    CallContext.getInstance().getCurrentDomainId());
            assertNull("Filter should have been deleted", testFilter);
            
            lstFilterConditions = m_filterConditionFactory.getAllForFilter(filter.getId());
   
            assertNull("There should be no filter conditions assigned to the filter", 
                       lstFilterConditions);
   
            filter = null;
            filterCondition11 = (FilterCondition) m_filterConditionFactory.get(
                                   filterCondition11.getId(),
                                   CallContext.getInstance().getCurrentDomainId());
            assertNull("There should not assigned Filter Condition 1 to the filter", 
                       filterCondition11);
            filterCondition11 = null;
            filterCondition12 = (FilterCondition) m_filterConditionFactory.get(
                                   filterCondition12.getId(),
                                   CallContext.getInstance().getCurrentDomainId());
            assertNull("There should not assigned Filter Condition 2 to the filter", 
                       filterCondition12);
            filterCondition12 = null;
         }
         finally
         {
            // delete inserted records
            if ((filterCondition11 != null) && (filterCondition11.getId() != DataObject.NEW_ID))
            {
               // delete filter condition
               m_filterConditionFactory.delete(filter.getId(), 
                                               Integer.toString(filterCondition11.getId()));
            }
            if ((filterCondition12 != null) && (filterCondition12.getId() != DataObject.NEW_ID))
            {
               // delete filter condition
               m_filterConditionFactory.delete(filter.getId(), 
                                               Integer.toString(filterCondition12.getId()));
            }
            if ((filter != null) && (filter.getId() != DataObject.NEW_ID))
            {
               m_transaction.begin();
               // delete inserted records
               try
               {
                  // delete filter 1
                  m_filterFactory.delete(
                                     filter.getId(),
                                     CallContext.getInstance().getCurrentDomainId());
                  m_transaction.commit();
               }
               catch (Throwable thr)
               {
                  m_transaction.rollback();
                  throw new Exception(thr);
               }
            }
            if ((user != null) && (user.getId() != DataObject.NEW_ID))
            {
               m_transaction.begin();
               // delete inserted records
               try
               {
                  // delete user
                  m_userFactory.delete(
                                   user.getId(),
                                   CallContext.getInstance().getCurrentDomainId());
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
   
      /**
       * Test of delete multiple roles methods in controller
       * 
       * @throws Exception - an error has occurred
       */
      public void testDeleteMultiple(
      ) throws Exception
      {
         User   user       = null;
         Filter filter1    = null;
         Filter filter2    = null;
         Filter filter3    = null;
         Filter testFilter = null;
         FilterCondition filterCondition11 = null;
         FilterCondition filterCondition12 = null;
         FilterCondition filterCondition21 = null;
         FilterCondition filterCondition22 = null;
         FilterCondition filterCondition31 = null;
         FilterCondition filterCondition32 = null;
   
         FilterCondition filterConditionTest31 = null;
         FilterCondition filterConditionTest32 = null;
   
         List lstFilterConditions  = new ArrayList();
   
         try
         {
            m_transaction.begin();
            try
            {
               // create test user
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
                                true, true, true, true, null, null, true
                               );
            
               user = (User)m_userFactory.create(user);
               
               // create 3 test filters
               filter1 = new Filter(DataObject.NEW_ID,
                                   CallContext.getInstance().getCurrentDomainId(),
                                   user.getId(),
                                   "test_filter_name_1",
                                   "test_filter_description_1",
                                   1, false, false, 10, false, false,
                                   "", "", null, null, null
                                  );
               filter1 = (Filter)m_filterFactory.create(filter1);
   
               filterCondition11 = new FilterCondition(
                                        DataObject.NEW_ID,
                                        CallContext.getInstance().getCurrentDomainId(),
                                        filter1.getId(),
                                        100, 1, "test_fc_value_11",
                                        DataCondition.VALUE_TYPE_INTEGER, 1,
                                        null, null
                                       );
               filterCondition11 = (FilterCondition)m_filterConditionFactory.create(
                                      filterCondition11);
   
               filterCondition12 = new FilterCondition(
                                        DataObject.NEW_ID,
                                        CallContext.getInstance().getCurrentDomainId(),
                                        filter1.getId(),
                                        200, 2, "test_fc_value_12",
                                        DataCondition.VALUE_TYPE_BOOLEAN, 2,
                                        null, null
                                       );
               filterCondition12 = (FilterCondition)m_filterConditionFactory.create(
                                      filterCondition12);
   
               filter2 = new Filter(DataObject.NEW_ID,
                                   CallContext.getInstance().getCurrentDomainId(),
                                   user.getId(),
                                   "test_filter_name_2",
                                   "test_filter_description_2",
                                   2, false, false, 20, false, false,
                                   "", "", null, null, null
                                  );
               filter2 = (Filter)m_filterFactory.create(filter2);
   
               filterCondition21 = new FilterCondition(
                                        DataObject.NEW_ID,
                                        CallContext.getInstance().getCurrentDomainId(),
                                        filter2.getId(),
                                        300, 3, "test_fc_value_21",
                                        DataCondition.VALUE_TYPE_STRING, 1,
                                        null, null
                                       );
               filterCondition21 = (FilterCondition)m_filterConditionFactory.create(
                                       filterCondition21);
   
               filterCondition22 = new FilterCondition(
                                        DataObject.NEW_ID,
                                        CallContext.getInstance().getCurrentDomainId(),
                                        filter2.getId(),
                                        500, 5, "test_fc_value_22",
                                        DataCondition.VALUE_TYPE_TIMESTAMP, 2,
                                        null, null
                                       );
               filterCondition22 = (FilterCondition)m_filterConditionFactory.create(
                                       filterCondition22);
   
               
               filter3 = new Filter(DataObject.NEW_ID,
                                   CallContext.getInstance().getCurrentDomainId(),
                                   user.getId(),
                                   "test_filter_name_3",
                                   "test_filter_description_3",
                                   3, false, false, 50, false, false,
                                   "", "", null, null, null
                                  );
               filter3 = (Filter)m_filterFactory.create(filter3);
   
               filterCondition31 = new FilterCondition(
                                        DataObject.NEW_ID,
                                        CallContext.getInstance().getCurrentDomainId(),
                                        filter3.getId(),
                                        400, 4, "test_fc_value_31",
                                        DataCondition.VALUE_TYPE_DOUBLE, 1,
                                        null, null
                                       );
               filterCondition31 = (FilterCondition)m_filterConditionFactory.create(
                                       filterCondition31);
   
               filterCondition32 = new FilterCondition(
                                        DataObject.NEW_ID,
                                        CallContext.getInstance().getCurrentDomainId(),
                                        filter3.getId(),
                                        600, 6, "test_fc_value_32",
                                        DataCondition.VALUE_TYPE_INTEGER, 2,
                                        null, null
                                       );
               filterCondition32 = (FilterCondition)m_filterConditionFactory.create(
                                       filterCondition32);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            lstFilterConditions = m_filterConditionFactory.getAllForFilter(filter1.getId());
            assertEquals("Incorrect number of Filter Condition assigned to the filter 1", 
                         2, lstFilterConditions.size());
   
            lstFilterConditions = m_filterConditionFactory.getAllForFilter(filter2.getId());
            assertEquals("Incorrect number of Filter Condition assigned to the filter 2", 
                         2, lstFilterConditions.size());
   
            lstFilterConditions = m_filterConditionFactory.getAllForFilter(filter3.getId());
            assertEquals("Incorrect number of Filter Condition assigned to the filter 3", 
                         2, lstFilterConditions.size());
   
            int iDeleted = m_filterControl.delete(Integer.toString(filter1.getId()) + "," + 
                               Integer.toString(filter2.getId()) + ",987654321");
            assertEquals("Number of deleted Filters is incorect", 2, iDeleted);
            testFilter = (Filter)m_filterFactory.get(
                                    filter1.getId(),
                                    CallContext.getInstance().getCurrentDomainId());
            assertNull("Filter should have been deleted", testFilter);
            testFilter = (Filter)m_filterFactory.get(
                                    filter2.getId(),
                                    CallContext.getInstance().getCurrentDomainId());
            assertNull("Filter should have been deleted", testFilter);
   
            lstFilterConditions = m_filterConditionFactory.getAllForFilter(filter1.getId());
            assertNull("There should be no Filter Conditions assigned to the filter 1", 
                       lstFilterConditions);
            lstFilterConditions = m_filterConditionFactory.getAllForFilter(filter2.getId());
            assertNull("There should be no Filter Conditions assigned to the filter 2", 
                       lstFilterConditions);
   
            filter1 = null;
            filter2 = null;
   
            lstFilterConditions = m_filterConditionFactory.getAllForFilter(filter3.getId());
            assertEquals("Incorrect number of Filter Conditions assigned to the filter 3", 
                         2, lstFilterConditions.size());
   
            filterCondition11 = (FilterCondition)m_filterFactory.get(
                                   filterCondition11.getId(),
                                   CallContext.getInstance().getCurrentDomainId());
            assertNull("There should not be assigned Filter Condition 11 to the filter", 
                       filterCondition11);
            filterCondition12 = (FilterCondition)m_filterFactory.get(
                                   filterCondition12.getId(),
                                   CallContext.getInstance().getCurrentDomainId());
            assertNull("There should not be assigned Filter Condition 12 to the filter", 
                       filterCondition12);
   
            filterCondition21 = (FilterCondition)m_filterFactory.get(
                                   filterCondition21.getId(),
                                   CallContext.getInstance().getCurrentDomainId());
            assertNull("There should not be assigned Filter Condition 21 to the filter", 
                       filterCondition21);
            filterCondition22 = (FilterCondition)m_filterFactory.get(
                                   filterCondition22.getId(),
                                   CallContext.getInstance().getCurrentDomainId());
            assertNull("There should not be assigned Filter Condition 22 to the filter", 
                       filterCondition22);
   
            filterConditionTest31 = (FilterCondition)m_filterConditionFactory.get(
                                       filterCondition31.getId(),
                                       CallContext.getInstance().getCurrentDomainId());
            assertTrue("Incorrect Filter Condition 31 assigned to the filter 3", 
                         filterCondition31.isSame(filterConditionTest31));
            filterConditionTest32 = (FilterCondition)m_filterConditionFactory.get(
                                       filterCondition32.getId(),
                                       CallContext.getInstance().getCurrentDomainId());
            assertTrue("Incorrect Filter Condition 32 assigned to the filter 3", 
                         filterCondition32.isSame(filterConditionTest32));
         }
         finally
         {
            if ((filterCondition11 != null) && (filterCondition11.getId() != DataObject.NEW_ID))
            {
               // delete filter condition
               m_filterConditionFactory.delete(filter1.getId(), 
                                               Integer.toString(filterCondition11.getId()));
            }
            if ((filterCondition12 != null) && (filterCondition12.getId() != DataObject.NEW_ID))
            {
               // delete filter condition
               m_filterConditionFactory.delete(filter1.getId(), 
                                               Integer.toString(filterCondition12.getId()));
            }
            if ((filterCondition21 != null) && (filterCondition21.getId() != DataObject.NEW_ID))
            {
               // delete filter condition
               m_filterConditionFactory.delete(filter2.getId(), 
                                               Integer.toString(filterCondition21.getId()));
            }
            if ((filterCondition22 != null) && (filterCondition22.getId() != DataObject.NEW_ID))
            {
               // delete filter condition
               m_filterConditionFactory.delete(filter2.getId(), 
                                               Integer.toString(filterCondition22.getId()));
            }
            if ((filterCondition31 != null) && (filterCondition31.getId() != DataObject.NEW_ID))
            {
               // delete filter condition
               m_filterConditionFactory.delete(filter3.getId(), 
                                               Integer.toString(filterCondition31.getId()));
            }
            if ((filterCondition32 != null) && (filterCondition32.getId() != DataObject.NEW_ID))
            {
               // delete filter condition
               m_filterConditionFactory.delete(filter3.getId(), 
                                               Integer.toString(filterCondition32.getId()));
            }
            if ((filter1 != null) && (filter1.getId() != DataObject.NEW_ID))
            {
               m_transaction.begin();
               // delete inserted records
               try
               {
                  // delete all filter conditions belonging to this filter
                  // this is necessary for Sybase ASE database
                  m_filterConditionFactory.deleteAllForFilter(filter1.getId());
                  // delete filter 1
                  m_filterFactory.delete(
                                     filter1.getId(),
                                     CallContext.getInstance().getCurrentDomainId());
                  m_transaction.commit();
               }
               catch (Throwable thr)
               {
                  m_transaction.rollback();
                  throw new Exception(thr);
               }
            }
            if ((filter2 != null) && (filter2.getId() != DataObject.NEW_ID))
            {
               m_transaction.begin();
               // delete inserted records
               try
               {
                  // delete all filter conditions belonging to this filter
                  // this is necessary for Sybase ASE database
                  m_filterConditionFactory.deleteAllForFilter(filter2.getId());
                  // delete filter 2
                  m_filterFactory.delete(
                                     filter2.getId(),
                                     CallContext.getInstance().getCurrentDomainId());
                  m_transaction.commit();
               }
               catch (Throwable thr)
               {
                  m_transaction.rollback();
                  throw new Exception(thr);
               }
            }
            if ((filter3 != null) && (filter3.getId() != DataObject.NEW_ID))
            {
               m_transaction.begin();
               // delete inserted records
               try
               {
                  // delete all filter conditions belonging to this filter
                  // this is necessary for Sybase ASE database
                  m_filterConditionFactory.deleteAllForFilter(filter3.getId());
                  // delete filter 3
                  m_filterFactory.delete(
                                     filter3.getId(),
                                     CallContext.getInstance().getCurrentDomainId());
                  m_transaction.commit();
               }
               catch (Throwable thr)
               {
                  m_transaction.rollback();
                  throw new Exception(thr);
               }
            }
            if ((user != null) && (user.getId() != DataObject.NEW_ID))
            {
               m_transaction.begin();
               // delete inserted records
               try
               {
                  // delete user
                  m_userFactory.delete(
                                   user.getId(),
                                   CallContext.getInstance().getCurrentDomainId());
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
   
      /**
       * Test of updateShare methods in controller
       * 
       * @throws Exception - an error has occurred
       */
      public void testUpdateShareTrue(
      ) throws Exception
      {
         User   user       = null;
         Filter filter1    = null;
         Filter filter2   = null;
         Filter filter3    = null;
         Filter testFilter = null;
   
         try
         {
            m_transaction.begin();
            try
            {
               // create test user
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
                                true, true, true, true, null, null, true
                               );
            
               user = (User)m_userFactory.create(user);
               
               // create 2 test filters
               filter1 = new Filter(DataObject.NEW_ID,
                                   CallContext.getInstance().getCurrentDomainId(),
                                   user.getId(),
                                   "test_filter_name_1",
                                   "test_filter_description_1",
                                   1, false, false, 10, false, false,
                                   "", "", null, null, null
                                  );
               filter1 = (Filter)m_filterFactory.create(filter1);
   
               filter2 = new Filter(DataObject.NEW_ID,
                                   CallContext.getInstance().getCurrentDomainId(),
                                   user.getId(),
                                   "test_filter_name_2",
                                   "test_filter_description_2",
                                   2, false, false, 20, false, false,
                                   "", "", null, null, null
                                  );
               filter2 = (Filter)m_filterFactory.create(filter2);
   
               filter3 = new Filter(DataObject.NEW_ID,
                                   CallContext.getInstance().getCurrentDomainId(),
                                   user.getId(),
                                   "test_filter_name_3",
                                   "test_filter_description_3",
                                   3, false, false, 50, false, false,
                                   "", "", null, null, null
                                  );
               filter3 = (Filter)m_filterFactory.create(filter3);
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            Thread.sleep(1000);
            
            int iShared;
            
            m_transaction.begin();      
            try
            {
               iShared = m_filterControl.updateShare(Integer.toString(filter1.getId()) 
                              + "," + Integer.toString(filter2.getId()) 
                              + "," + Integer.toString(filter3.getId()) + ", 987654321", true);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            
            assertEquals("Number of shared Filters is incorect", 3, iShared);
            testFilter = (Filter)m_filterFactory.get(
                                    filter1.getId(),
                                    CallContext.getInstance().getCurrentDomainId());
            assertNotNull("Filter should still exist", testFilter);
            assertTrue("Filter should be shared", testFilter.isShared());
            assertTrue("Modified date should be changed",
                       testFilter.getModificationTimestamp().getTime()
                       != filter1.getModificationTimestamp().getTime());
            testFilter = (Filter)m_filterFactory.get(
                                    filter2.getId(),
                                    CallContext.getInstance().getCurrentDomainId());
            assertNotNull("Filter should still exist", testFilter);
            assertTrue("Filter should be shared", testFilter.isShared());
            assertTrue("Modified date should be changed",
                       testFilter.getModificationTimestamp().getTime()
                       != filter2.getModificationTimestamp().getTime());
            testFilter = (Filter)m_filterFactory.get(
                                    filter3.getId(),
                                    CallContext.getInstance().getCurrentDomainId());
            assertNotNull("Filter should still exist", testFilter);
            assertTrue("Filter should be shared", testFilter.isShared());
            assertTrue("Modified date should be changed",
                       testFilter.getModificationTimestamp().getTime()
                       != filter3.getModificationTimestamp().getTime());         
         }
         finally
         {
            if ((filter1 != null) && (filter1.getId() != DataObject.NEW_ID))
            {
               m_transaction.begin();
               // delete inserted records
               try
               {
                  // delete filter 1
                  m_filterFactory.delete(
                                     filter1.getId(),
                                     CallContext.getInstance().getCurrentDomainId());
                  m_transaction.commit();
               }
               catch (Throwable thr)
               {
                  m_transaction.rollback();
                  throw new Exception(thr);
               }
            }
            if ((filter2 != null) && (filter2.getId() != DataObject.NEW_ID))
            {
               m_transaction.begin();
               // delete inserted records
               try
               {
                  // delete filter 2
                  m_filterFactory.delete(
                                     filter2.getId(),
                                     CallContext.getInstance().getCurrentDomainId());
                  m_transaction.commit();
               }
               catch (Throwable thr)
               {
                  m_transaction.rollback();
                  throw new Exception(thr);
               }
            }
            if ((filter3 != null) && (filter3.getId() != DataObject.NEW_ID))
            {
               m_transaction.begin();
               // delete inserted records
               try
               {
                  // delete filter 3
                  m_filterFactory.delete(
                                     filter3.getId(),
                                     CallContext.getInstance().getCurrentDomainId());
                  m_transaction.commit();
               }
               catch (Throwable thr)
               {
                  m_transaction.rollback();
                  throw new Exception(thr);
               }
            }
            if ((user != null) && (user.getId() != DataObject.NEW_ID))
            {
               m_transaction.begin();
               // delete inserted records
               try
               {
                  // delete user
                  m_userFactory.delete(
                                   user.getId(),
                                   CallContext.getInstance().getCurrentDomainId());
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
      
      /**
       * Test of updateShare methods in controller
       * 
       * @throws Exception - an error has occurred
       */
      public void testUpdateShareFalse(
      ) throws Exception
      {
         User   user       = null;
         Filter filter1    = null;
         Filter filter2   = null;
         Filter filter3    = null;
         Filter testFilter = null;
   
         try
         {
            m_transaction.begin();
            try
            {
               // create test user
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
                                true, true, true, true, null, null, true
                               );
            
               user = (User)m_userFactory.create(user);
               
               // create 2 test filters
               filter1 = new Filter(DataObject.NEW_ID,
                                   CallContext.getInstance().getCurrentDomainId(),
                                   user.getId(),
                                   "test_filter_name_1",
                                   "test_filter_description_1",
                                   1, true, true, 10, false, false,
                                   "", "", null, null, null
                                  );
               filter1 = (Filter)m_filterFactory.create(filter1);
   
               filter2 = new Filter(DataObject.NEW_ID,
                                   CallContext.getInstance().getCurrentDomainId(),
                                   user.getId(),
                                   "test_filter_name_2",
                                   "test_filter_description_2",
                                   2, true, true, 20, false, false,
                                   "", "", null, null, null
                                  );
               filter2 = (Filter)m_filterFactory.create(filter2);
   
               filter3 = new Filter(DataObject.NEW_ID,
                                   CallContext.getInstance().getCurrentDomainId(),
                                   user.getId(),
                                   "test_filter_name_3",
                                   "test_filter_description_3",
                                   3, true, true, 50, false, false,
                                   "", "", null, null, null
                                  );
               filter3 = (Filter)m_filterFactory.create(filter3);
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            Thread.sleep(1000);
            
            int iNotShared;
            
            m_transaction.begin();      
            try
            {
               iNotShared = m_filterControl.updateShare(Integer.toString(filter1.getId()) 
                              + "," + Integer.toString(filter2.getId()) 
                              + "," + Integer.toString(filter3.getId()) + ", 987654321", false);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            
            assertEquals("Number of shared Filters is incorect", 3, iNotShared);
            testFilter = (Filter)m_filterFactory.get(
                                    filter1.getId(),
                                    CallContext.getInstance().getCurrentDomainId());
            assertNotNull("Filter should still exist", testFilter);
            assertFalse("Filter should be not shared", testFilter.isShared());
            assertTrue("Modified date should be changed",
                       testFilter.getModificationTimestamp().getTime()
                       != filter1.getModificationTimestamp().getTime());
            testFilter = (Filter)m_filterFactory.get(
                                    filter2.getId(),
                                    CallContext.getInstance().getCurrentDomainId());
            assertNotNull("Filter should still exist", testFilter);
            assertFalse("Filter should be not shared", testFilter.isShared());
            assertTrue("Modified date should be changed",
                       testFilter.getModificationTimestamp().getTime()
                       != filter2.getModificationTimestamp().getTime());
            testFilter = (Filter)m_filterFactory.get(
                                    filter3.getId(),
                                    CallContext.getInstance().getCurrentDomainId());
            assertNotNull("Filter should still exist", testFilter);
            assertFalse("Filter should be not shared", testFilter.isShared());
            assertTrue("Modified date should be changed",
                       testFilter.getModificationTimestamp().getTime()
                       != filter3.getModificationTimestamp().getTime());         
         }
         finally
         {
            if ((filter1 != null) && (filter1.getId() != DataObject.NEW_ID))
            {
               m_transaction.begin();
               // delete inserted records
               try
               {
                  // delete filter 1
                  m_filterFactory.delete(
                                     filter1.getId(),
                                     CallContext.getInstance().getCurrentDomainId());
                  m_transaction.commit();
               }
               catch (Throwable thr)
               {
                  m_transaction.rollback();
                  throw new Exception(thr);
               }
            }
            if ((filter2 != null) && (filter2.getId() != DataObject.NEW_ID))
            {
               m_transaction.begin();
               // delete inserted records
               try
               {
                  // delete filter 2
                  m_filterFactory.delete(
                                     filter2.getId(),
                                     CallContext.getInstance().getCurrentDomainId());
                  m_transaction.commit();
               }
               catch (Throwable thr)
               {
                  m_transaction.rollback();
                  throw new Exception(thr);
               }
            }
            if ((filter3 != null) && (filter3.getId() != DataObject.NEW_ID))
            {
               m_transaction.begin();
               // delete inserted records
               try
               {
                  // delete filter 3
                  m_filterFactory.delete(
                                     filter3.getId(),
                                     CallContext.getInstance().getCurrentDomainId());
                  m_transaction.commit();
               }
               catch (Throwable thr)
               {
                  m_transaction.rollback();
                  throw new Exception(thr);
               }
            }
            if ((user != null) && (user.getId() != DataObject.NEW_ID))
            {
               m_transaction.begin();
               // delete inserted records
               try
               {
                  // delete user
                  m_userFactory.delete(
                                   user.getId(),
                                   CallContext.getInstance().getCurrentDomainId());
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
}
