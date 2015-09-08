/*
 * Copyright (c) 2003 - 2009 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: RoleDatabaseFactoryTest.java,v 1.36 2009/09/20 05:32:57 bastafidli Exp $
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

package org.opensubsystems.security.persist.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

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
import org.opensubsystems.core.error.OSSInternalErrorException;
import org.opensubsystems.core.error.OSSInvalidDataException;
import org.opensubsystems.core.persist.DataFactoryManager;
import org.opensubsystems.core.persist.db.DatabaseSchemaManager;
import org.opensubsystems.core.persist.db.DatabaseTestSuite;
import org.opensubsystems.core.util.CallContext;
import org.opensubsystems.core.util.Tests;
import org.opensubsystems.core.util.jdbc.DatabaseUtils;
import org.opensubsystems.patterns.listdata.data.SimpleRule;
import org.opensubsystems.security.application.SecurityBackendModule;
import org.opensubsystems.security.data.Domain;
import org.opensubsystems.security.data.Role;
import org.opensubsystems.security.data.RoleDataDescriptor;
import org.opensubsystems.security.data.User;
import org.opensubsystems.security.patterns.listdata.persist.db.SecureListDatabaseFactoryTest;
import org.opensubsystems.security.persist.DomainFactory;
import org.opensubsystems.security.persist.RoleFactory;
import org.opensubsystems.security.persist.UserFactory;
import org.opensubsystems.security.util.RoleConstants;
import org.opensubsystems.security.utils.TestRoleDatabaseFactoryUtils;
import org.opensubsystems.security.utils.TestUserDatabaseFactoryUtils;

/**
 * Test of RoleDatabaseFactory
 * 
 * @version $Id: RoleDatabaseFactoryTest.java,v 1.36 2009/09/20 05:32:57 bastafidli Exp $
 * @author Peter Satury
 * @code.reviewer Miro Halas
 * @code.reviewed Initial revision
 */
public final class RoleDatabaseFactoryTest extends Tests
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private RoleDatabaseFactoryTest(
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
      TestSuite suite = new DatabaseTestSuite("RoleDatabaseFactoryTest");
      suite.addTestSuite(RoleDatabaseFactoryTestInternal.class);
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
   public static class RoleDatabaseFactoryTestInternal extends SecureListDatabaseFactoryTest
   {
      // Cached values ////////////////////////////////////////////////////////////
   
      /**
       * Factory to manage domains.
       */
      protected DomainFactory m_domainFactory;

      /**
       * Schema for database dependent operations.
       */
      protected RoleDatabaseSchema m_roleSchema;
   
      /**
       * Factory to manage roles.
       */
      protected RoleDatabaseFactory m_roleFactory;
      
      /**
       * Factory to manage users.
       */
      protected UserDatabaseFactory m_userFactory;
      
      /**
       * Data descriptor for the role data types.
       */
      protected RoleDataDescriptor m_roleDescriptor;

      /**
       * Factory utilities to manage roles.
       */
      protected TestRoleDatabaseFactoryUtils m_roleFactoryUtils;
   
      /**
       * Factory utilities to manage users.
       */
      protected TestUserDatabaseFactoryUtils m_userFactoryUtils;
   
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
      public RoleDatabaseFactoryTestInternal(
         String strTestName
      ) throws Exception
      {
         super(strTestName, new RoleListDatabaseTestUtils());
         
         m_domainFactory = (DomainFactory)DataFactoryManager.getInstance(
                                                     DomainFactory.class);
         m_roleSchema = (RoleDatabaseSchema)DatabaseSchemaManager.getInstance(
                                                     RoleDatabaseSchema.class);
         m_roleFactory = (RoleDatabaseFactory)DataFactoryManager.getInstance(
                                                     RoleFactory.class);
         m_userFactory = (UserDatabaseFactory)DataFactoryManager.getInstance(
                                                     UserFactory.class);
         m_roleDescriptor = (RoleDataDescriptor)DataDescriptorManager.getInstance(
                               RoleDataDescriptor.class);
         m_roleFactoryUtils = new TestRoleDatabaseFactoryUtils();
         m_userFactoryUtils = new TestUserDatabaseFactoryUtils();
      }
      
      // Tests ////////////////////////////////////////////////////////////////////
   
      /**
       * Test creation of regular role.
       * 
       * @throws Exception - an error has occurred during test
       */
      public void testCreateRegular(
      ) throws Exception
      {
         Role data = null;
         Role testData = null;
         Role selectedData;     
   
         PreparedStatement statement = null;
         ResultSet         results = null;      
         String            strQuery;
   
         try
         {            
            data = new Role(DataObject.NEW_ID, CallContext.getInstance().getCurrentDomainId(), 
                     "testname1", "testdescription1", true, DataObject.NEW_ID, 
                     false, null, null, null);
   
            m_transaction.begin();
            try
            {                                            
               testData = (Role)m_roleFactory.create(data);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            assertNotNull("Role should not be null", testData);
            assertTrue("Id was not generated.", testData.getId() != DataObject.NEW_ID); 
            assertNotNull("Creation timestamp was not generated", 
                          testData.getCreationTimestamp());
            assertNotNull("Modification timestamp was not generated", 
                          testData.getModificationTimestamp());
            assertTrue("Role is not the same", data.isSame(testData));
   
            strQuery = m_roleSchema.getSelectRoleById(
                                       m_roleDescriptor.getAllColumnCodes());
            try
            {                    
               statement = m_connection.prepareStatement(strQuery);
   
               // test modifiable
               statement.setInt(1, testData.getId());
               statement.setInt(2, CallContext.getInstance().getCurrentDomainId());
               results = statement.executeQuery();
   
               assertTrue("Inserted data were not found.", results.next());
               selectedData = (Role)m_roleFactory.load(results, 1);
               assertNotNull("Role should not be null", selectedData);
               assertEquals("Role is not the same", testData, selectedData);
               assertEquals("Role should be personal", 
                            DataObject.NEW_ID, selectedData.getUserId());
               assertFalse("Only one data should have been created.", results.next());
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
               if ((testData != null) && (testData.getId() != DataObject.NEW_ID))
               {
                  m_roleFactory.delete(
                                   testData.getId(),
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
       * Test creation of unmodifiable role.
       * 
       * @throws Exception - an error has occurred during test
       */
      public void testCreateUnmodifiable(
      ) throws Exception
      {
         Role data = null;
         Role testData = null;
         Role selectedData;     
   
         PreparedStatement statement = null;
         ResultSet         results = null;      
         String            strQuery;
         
         try
         {            
            // creating unmodifiable role
            data = new Role(DataObject.NEW_ID, CallContext.getInstance().getCurrentDomainId(), 
                            "testname2", "testdescription2", true, DataObject.NEW_ID, 
                            true, null, null, null);
   
            m_transaction.begin();
            try
            {                                            
               testData = (Role)m_roleFactory.create(data);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
                  
            // Here the create is completed
            // We need to test role
            assertNotNull("Role should not be null", testData);
            assertTrue("New id wasn't generated.", 
                       testData.getId() != DataObject.NEW_ID); 
            assertNotNull("Creation timestamp was not generated", 
                     testData.getCreationTimestamp());
            assertNotNull("Modification timestamp was not generated", 
                     testData.getModificationTimestamp());
            assertTrue("Role is not the same", data.isSame(testData));
   
            strQuery = m_roleSchema.getSelectRoleById(
                                       m_roleDescriptor.getAllColumnCodes());
            try
            {                    
               statement = m_connection.prepareStatement(strQuery);
               
               // test unmodifiable
               statement.clearParameters();
               statement.setInt(1, testData.getId());
               statement.setInt(2, CallContext.getInstance().getCurrentDomainId());
               results = statement.executeQuery();
               
               assertTrue("Inserted data were not found.", results.next());
               selectedData = (Role)m_roleFactory.load(results, 1);
               assertNotNull("Role should not be null", selectedData);
               assertEquals("Role is not the same", testData, selectedData);
               assertEquals("Role should not be personal", 
                            DataObject.NEW_ID, selectedData.getUserId());
               assertFalse("Only one data should have been created.", results.next());
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
               // delete roles
               if ((testData != null) && (testData.getId() != DataObject.NEW_ID))
               {
                  m_roleFactory.delete(
                                   testData.getId(),
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
       * Test of create personal role
       * 
       * @throws Exception - an error has occurred
       */
      public void testCreatePersonal(
      ) throws Exception
      {
         User user = null;
         Role testData = null;
         Role selectedData;     
   
         PreparedStatement statement = null;
         ResultSet results = null;      
         String strQuery;
   
         try
         {            
            user = new User(DataObject.NEW_ID,
                     CallContext.getInstance().getCurrentDomainId(),
                     "test_first_name_1", "test_last_name_1",
                     "test_phone_1", "test_fax_1", "test_address_1",
                     "test_email_1", "test_login_name_1", "test_password_1",
                     true, true, true, true, null, null, true);
   
            m_transaction.begin();
            try
            {
               // create user
               user = (User)m_userFactory.create(user);
               // create personal role for user
               testData = m_roleFactory.createPersonal(user);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            
            assertNotNull("Role should not be null", testData);
            assertTrue("New id wasn't generated.", testData.getId() != DataObject.NEW_ID); 
            assertNotNull("Creation timestamp was not generated", 
                     testData.getCreationTimestamp());
            assertNotNull("Modification timestamp was not generated", 
                     testData.getModificationTimestamp());
            
            strQuery = m_roleSchema.getSelectRoleById(
                                       m_roleDescriptor.getAllColumnCodes());
            try
            {                    
               statement = m_connection.prepareStatement(strQuery);
               statement.setInt(1, testData.getId());
               statement.setInt(2, CallContext.getInstance().getCurrentDomainId());
               results = statement.executeQuery();
               
               assertTrue("Inserted data were not found.", results.next());
               selectedData = (Role)m_roleFactory.load(results, 1);
               assertNotNull("Role should not be null", selectedData);
               assertEquals("Role is not the same", testData, selectedData);
               assertEquals("Role name is not the same as user name", 
                            user.getLoginName(), selectedData.getName());
               assertEquals("Role description is not the same as user name", 
                            user.getFullNameLastFirst(), selectedData.getDescription());
               assertEquals("Role is not personal", 
                            user.getId(), selectedData.getUserId());
               assertFalse("Role should not be unmodifiable", 
                           selectedData.isUnmodifiable());
               assertFalse("Only one data should have been created.", results.next());
            }
            finally
            {
               DatabaseUtils.closeResultSetAndStatement(results, statement);
            }
         }
         finally
         {
            m_transaction.begin();
            // delete inserted records
            try
            {
               if ((testData != null) && (testData.getId() != DataObject.NEW_ID))
               {
                  // delete role
                  m_roleFactoryUtils.deleteRoleCascadeManual(testData.getId());
               }
               if ((user != null) && (user.getId() != DataObject.NEW_ID))
               {
                  // delete user
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
            finally
            {
               DatabaseUtils.closeStatement(statement);
            }
         }  
      }
   
      /**
       * Test to check if the violations of unique constraints are correctly 
       * detected.
       * 
       * @throws Throwable - an error has occurred
       */
      public void testCheckUniqueConstraint(
      ) throws Throwable
      {
         Role data1      = null;
         Role data2      = null;
         Role testData1  = null;
         Role testData2  = null;
         Role testData22 = null;
   
         try
         {            
            data1 = new Role(DataObject.NEW_ID, 
                             CallContext.getInstance().getCurrentDomainId(), 
                             "testname1", "testdescription1", true, 
                             DataObject.NEW_ID, false, null, null, null);
            
            m_transaction.begin();
            try
            {                                            
               testData1 = (Role)m_roleFactory.create(data1);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw thr;
            }
            
            assertNotNull("Create data  should not be null", testData1);
            assertTrue("Id was not generated", 
                       testData1.getId() != DataObject.NEW_ID);
            assertNotNull("Creation timestamp was not generated", 
                          testData1.getCreationTimestamp());
            assertNotNull("Modification timestamp was not generated", 
                          testData1.getModificationTimestamp());
            assertTrue("Create data is not the same", data1.isSame(testData1));
            
            // At this point we have created data 1. Now we will try to create 
            // another data with the same name. This should throw an exception
            // due to constraint validation
            data2 = new Role(DataObject.NEW_ID, 
                             CallContext.getInstance().getCurrentDomainId(), 
                             testData1.getName(), "testdescription2", true, 
                             DataObject.NEW_ID, false, null, null, null);
            
            m_transaction.begin();
            try
            {
               testData2 = (Role)m_roleFactory.create(data2);
               m_transaction.commit();
               fail("It should not be possible to create two roles with the"
                        + " same name");
            }
            catch (OSSInvalidDataException ideExc)
            {
               // this exception is expected, there should be not inserted role with the same name
               // as already exists within the DB, while role name is unique
               m_transaction.rollback();
               
               assertTrue("There should be message identifying violation of"
                          + " unique constraint for role name",
                          ideExc.getErrorMessages().containsMessage(
                                    RoleDataDescriptor.COL_ROLE_NAME,
                                    "Role name has to be unique."));
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw thr;
            }
            
            // Now create role with different name and then try to rename it
            data2 = new Role(DataObject.NEW_ID, 
                             CallContext.getInstance().getCurrentDomainId(), 
                             testData1.getName() + "diff", "testdescription2", 
                             true, DataObject.NEW_ID, false, null, null, null);
            
            m_transaction.begin();
            try
            {                                            
               testData2 = (Role)m_roleFactory.create(data2);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw thr;
            }
            
            assertNotNull("Create data  should not be null", testData2);
            assertTrue("Id was not generated", 
                       testData2.getId() != DataObject.NEW_ID);
            assertNotNull("Creation timestamp was not generated", 
                          testData2.getCreationTimestamp());
            assertNotNull("Modification timestamp was not generated", 
                          testData2.getModificationTimestamp());
            assertTrue("Create data is not the same", data2.isSame(testData2));
            
            testData2.setName(testData1.getName());
            
            m_transaction.begin();
            try
            {
               // try to create domain 2
               testData22 = (Role)m_roleFactory.save(
                                     testData2, 
                                     RoleConstants.SAVE_ROLE_NONPERSONAL);
            
               m_transaction.commit();
               fail("It should not be possible to create two roles with the"
                    + " same name");
               assertNull("It should not be possible to create two roles with"
                          + " the same name", testData22);
            }
            catch (OSSInvalidDataException ideExc)
            {
               // This exception is expected, test if correct error is generated
               m_transaction.rollback();
               
               assertTrue("There should be message identifying violation of"
                        + " unique constraint for role name",
                        ideExc.getErrorMessages().containsMessage(
                                  RoleDataDescriptor.COL_ROLE_NAME,
                                  "Role name has to be unique."));
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw thr;
            }
         }
         finally
         {
            m_transaction.begin();
            try
            {
               if ((testData1 != null) 
                  && (testData1.getId() != DataObject.NEW_ID))
               {
                  m_roleFactory.delete(
                     testData1.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw thr;
            }
            finally
            {
               m_transaction.begin();
               try
               {
                  if ((testData2 != null) 
                     && (testData2.getId() != DataObject.NEW_ID))
                  {
                     m_roleFactory.delete(
                        testData2.getId(),
                        CallContext.getInstance().getCurrentDomainId());
                  }
                  m_transaction.commit();
               }
               catch (Throwable thr)
               {
                  m_transaction.rollback();
                  throw thr;
               }
            }
         }  
      }
   
      /**
       * Test of save regular role method
       * 
       * @throws Exception - an error has occurred during test
       */
      public void testSaveRegularRole(
      ) throws Exception
      {
         User user1 = null;
         Role regularRole1 = null; 
         Role regularRole2 = null; 
         Role testRole;
         
         try
         {  
            m_transaction.begin();
            try
            {
               user1 = new User(
                        DataObject.NEW_ID,
                        CallContext.getInstance().getCurrentDomainId(),
                        "test_first_name_1", "test_last_name_1",
                        "test_phone_1", "test_fax_1", "test_address_1", 
                        "test_email_1", "test_login_name_1", "test_password_1",
                        true, true, true, true, null, null, true);
               user1 = (User)m_userFactory.create(user1);
               
               regularRole1 = new Role(
                        DataObject.NEW_ID, 
                        CallContext.getInstance().getCurrentDomainId(), 
                        "testname", "testdescription",
                        true, DataObject.NEW_ID, false, null, null, null);
               regularRole1 = (Role)m_roleFactory.create(regularRole1);
   
               m_transaction.commit();           
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
           
            // wait to change modification timestamp 
            Thread.sleep(100);
   
            regularRole2 = new Role(
                     regularRole1.getId(), 
                     CallContext.getInstance().getCurrentDomainId(), 
                     "testname2", "testdescription2",
                     false, DataObject.NEW_ID, false, null, 
                     regularRole1.getCreationTimestamp(), 
                     regularRole1.getModificationTimestamp());
      
            m_transaction.begin();
            try
            {
               testRole = m_roleFactory.save(regularRole2, RoleConstants.SAVE_ROLE_ALWAYS);
               m_transaction.commit();                    
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            
            assertNotNull("Role should not be null", testRole);
            assertTrue("Modified date not changed", testRole.getModificationTimestamp() 
                                                    != regularRole1.getModificationTimestamp());
            assertEquals("Role is not the same", regularRole2, testRole);
   
            Role selectedData;
            // Here the save - update is completed
            // We need to test
            selectedData = (Role)m_roleFactory.get(
                                    regularRole1.getId(),
                                    CallContext.getInstance().getCurrentDomainId());
            assertNotNull("Role should not be null", selectedData);
            assertEquals("Role is not the same", selectedData, testRole);
         }
         finally
         {
            m_transaction.begin();
            // delete inserted records
            try
            {
               if ((regularRole1 != null) && (regularRole1.getId() != DataObject.NEW_ID))
               {
                  // delete role
                  m_roleFactory.delete(
                                   regularRole1.getId(),
                                   CallContext.getInstance().getCurrentDomainId());
               }
               if ((user1 != null) && (user1.getId() != DataObject.NEW_ID))
               {
                  // delete user
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
       * Test of save regular role method - there will be within the DB specified regular role,
       * that is unmodifiable (DB attribute UNMODIFIABLE = 1). This role should not be updated.
       * 
       * @throws Exception - an error has occurred during test
       */
      public void testSaveRegularRoleUnmodifiable(
      ) throws Exception
      {
         User user1 = null;
         Role regularRole1Unmodifiable = null;
         Role regularRole2 = null; 
   
         try
         {  
            m_transaction.begin();
            try
            {
               user1 = new User(
                        DataObject.NEW_ID,
                        CallContext.getInstance().getCurrentDomainId(),
                        "test_first_name_1", "test_last_name_1",
                        "test_phone_1", "test_fax_1", "test_address_1",
                        "test_email_1", "test_login_name_1", "test_password_1",
                        true, true, true, true, null, null, true);
               user1 = (User)m_userFactory.create(user1);
               
               regularRole1Unmodifiable = new Role(
                        DataObject.NEW_ID, 
                        CallContext.getInstance().getCurrentDomainId(), 
                        "testname", "testdescription",
                        true, DataObject.NEW_ID, true, null, null, null);
               regularRole1Unmodifiable = (Role)m_roleFactory.create(regularRole1Unmodifiable);
   
               m_transaction.commit();           
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
           
            // wait to change modification timestamp 
            Thread.sleep(100);
   
            regularRole2 = new Role(
                     regularRole1Unmodifiable.getId(), 
                     CallContext.getInstance().getCurrentDomainId(), 
                     "testname2", "testdescription2",
                     false, DataObject.NEW_ID, false, null, 
                     regularRole1Unmodifiable.getCreationTimestamp(), 
                     regularRole1Unmodifiable.getModificationTimestamp());
   
            Role selectedData;
   
            m_transaction.begin();
            try
            {
               // now try to save the same regular with role type ALWAYS
               m_roleFactory.save(regularRole2, RoleConstants.SAVE_ROLE_ALWAYS);
               // here we can continue, if there was not error occurred. Postgre SQL don't throw
               // error message if there was not record updated (or it was not found)
               m_transaction.commit();                    
            }
            catch (OSSInternalErrorException ieeExc)
            {
               // this exception is expected
               m_transaction.rollback();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            // Here the save - update is completed
            // We need to test
            selectedData = (Role)m_roleFactory.get(
                                    regularRole1Unmodifiable.getId(),
                                    CallContext.getInstance().getCurrentDomainId());
            assertNotNull("Role should not be null", selectedData);
            assertEquals("Role is not the same", selectedData, regularRole1Unmodifiable);
   
            m_transaction.begin();
            try
            {
               // now try to save the same regular with role type NONPERSONAL
               m_roleFactory.save(regularRole2, RoleConstants.SAVE_ROLE_NONPERSONAL);
               // here we can continue, if there was not error occurred. Postgre SQL don't throw
               // error message if there was not record updated (or it was not found)
               m_transaction.commit();                    
            }
            catch (OSSInternalErrorException ieeExc)
            {
               // this exception is expected
               m_transaction.rollback();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            // Here the save - update is completed
            // We need to test
            selectedData = (Role)m_roleFactory.get(
                                    regularRole1Unmodifiable.getId(),
                                    CallContext.getInstance().getCurrentDomainId());
            assertNotNull("Role should not be null", selectedData);
            assertEquals("Role is not the same", selectedData, regularRole1Unmodifiable);
         }
         finally
         {
            m_transaction.begin();
            // delete inserted records
            try
            {
               if ((regularRole1Unmodifiable != null) && (regularRole1Unmodifiable.getId() 
                   != DataObject.NEW_ID))
               {
                  // delete role
                  m_roleFactory.delete(
                                   regularRole1Unmodifiable.getId(),
                                   CallContext.getInstance().getCurrentDomainId());
               }
               if ((user1 != null) && (user1.getId() != DataObject.NEW_ID))
               {
                  // delete user
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
       * Test of save personal role method
       * 
       * @throws Exception - an error has occurred during test
       */
      public void testSavePersonalRole(
      ) throws Exception
      {
         User user1 = null;
         Role personalRole1 = null;
         Role personalRole2 = null;
         Role testRole;
         
         try
         {  
            m_transaction.begin();
            try
            {
               user1 = new User(
                        DataObject.NEW_ID,
                        CallContext.getInstance().getCurrentDomainId(),
                        "test_first_name_1", "test_last_name_1",
                        "test_phone_1", "test_fax_1", "test_address_1",
                        "test_email_1", "test_login_name_1", "test_password_1",
                        true, true, true, true, null, null, true);
               user1 = (User)m_userFactory.create(user1);
               // create personal role
               personalRole1 = m_roleFactory.createPersonal(user1);
               
               m_transaction.commit();           
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
           
            // wait to change modification timestamp 
            Thread.sleep(1000);
            
            personalRole2 = new Role(
                     personalRole1.getId(), 
                     CallContext.getInstance().getCurrentDomainId(), 
                     "testname2", "testdescription2",      // changing name and description
                     false, DataObject.NEW_ID, false,      // trying to turn off enabled flag and
                     null,                                 // set personal role to non personal
                     personalRole1.getCreationTimestamp(),
                     personalRole1.getModificationTimestamp());
   
            m_transaction.begin();
            try
            {
               // trying to save personal role as non personal
               m_roleFactory.save(personalRole2, RoleConstants.SAVE_ROLE_NONPERSONAL);
               personalRole2.setModificationTimestamp(personalRole1.getModificationTimestamp());
               fail("Cannot save personal role as not personal");
               m_transaction.commit();                    
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               // This exception is expected
            }
   
            Role selectedData;
            // Here the save - update is completed
            // We need to test
            selectedData = (Role)m_roleFactory.get(
                                    personalRole1.getId(),
                                    CallContext.getInstance().getCurrentDomainId());
            assertNotNull("Role should not be null", selectedData);
            assertEquals("Role is not the same", selectedData, personalRole1);
   
            m_transaction.begin();
            try
            {
               // now try to save the same personal role as personal
               testRole = m_roleFactory.save(personalRole2, RoleConstants.SAVE_ROLE_PERSONAL);
               m_transaction.commit();                    
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            
            assertNotNull("Role should not be null", testRole);
            assertTrue("Modified date not changed", testRole.getModificationTimestamp() 
                                                    != personalRole1.getModificationTimestamp());
            assertEquals("Role is not the same", personalRole2, testRole);
   
            selectedData = (Role)m_roleFactory.get(
                                    personalRole1.getId(),
                                    CallContext.getInstance().getCurrentDomainId());
            assertNotNull("Role should not be null", selectedData);
            assertEquals("ID from database not equal to inserted personal role ID", 
                     personalRole1.getId(), selectedData.getId());                              
            assertEquals("Name from database should not be updated", 
                     personalRole1.getName(), selectedData.getName());
            assertEquals("Description from database should not be updated", 
                     personalRole1.getDescription(), selectedData.getDescription());
            assertTrue("Personal role should be allways enabled", 
                     selectedData.isEnabled());
            assertEquals("Unmodifiable flag from database should not be updated", 
                     personalRole1.isUnmodifiable(), selectedData.isUnmodifiable());
            assertEquals("Creation timestamp not equals", 
                     personalRole1.getCreationTimestamp(), selectedData.getCreationTimestamp());
            assertNotNull("Modification timestamp is null", 
                     selectedData.getModificationTimestamp());
            assertTrue("Modification timestamp not changed", 
                     selectedData.getModificationTimestamp().getTime() 
                        > personalRole1.getModificationTimestamp().getTime());
         }
         finally
         {
            m_transaction.begin();
            // delete inserted records
            try
            {
               if ((personalRole1 != null) && (personalRole1.getId() != DataObject.NEW_ID))
               {
                  // delete role
                  m_roleFactoryUtils.deleteRoleCascadeManual(personalRole1.getId());
               }
               if ((user1 != null) && (user1.getId() != DataObject.NEW_ID))
               {
                  // delete user
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
       * Test of save personal role method - there will be within the DB 
       * specified personal role, that is unmodifiable (DB attribute 
       * UNMODIFIABLE = 1). This role should not be updated.
       * 
       * @throws Throwable - an error has occurred
       */
      public void testSavePersonalRoleUnmodifiable(
      ) throws Throwable
      {
         User user1 = null;
         Role personalRole1 = null;
         Role personalRole1Unmodifiable = null;
         Role personalRole2 = null;
   
         int iUpdated = 0;
   
         PreparedStatement statement = null;
         String            strQuery;
   
         try
         {  
            m_transaction.begin();
            try
            {
               user1 = new User(
                        DataObject.NEW_ID,
                        CallContext.getInstance().getCurrentDomainId(),
                        "test_first_name_1", "test_last_name_1",
                        "test_phone_1", "test_fax_1", "test_address_1",
                        "test_email_1", "test_login_name_1", "test_password_1",
                        true, true, true, true, null, null, true);
               user1 = (User)m_userFactory.create(user1);
               // create personal role
               personalRole1 = m_roleFactory.createPersonal(user1);
               
               m_transaction.commit();           
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw thr;
            }
   
            // wait to change modification timestamp 
            Thread.sleep(500);
   
            m_transaction.begin();
            try
            {
               // update flag UNMODIFIABLE = 1 within the DB 
               strQuery = "update " + RoleDatabaseSchema.ROLE_TABLE_NAME 
                          + " set UNMODIFIABLE = 1 where ID = ?";
               statement = m_connection.prepareStatement(strQuery);
               statement.setInt(1, personalRole1.getId());
               iUpdated = statement.executeUpdate();
         
               m_transaction.commit();           
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw thr;
            }
            finally
            {
               DatabaseUtils.closeStatement(statement);
            }
   
            assertEquals("Incorrect number of updated roles", 1, iUpdated);
   
            // construct role identical with just created role in the DB, 
            // but change UNMODIFIABLE flag
            personalRole1Unmodifiable = new Role(
                     personalRole1.getId(), 
                     CallContext.getInstance().getCurrentDomainId(),
                     personalRole1.getName(),
                     personalRole1.getDescription(),
                     personalRole1.isEnabled(),
                     personalRole1.getUserId(),
                     true, // set UNMODIFIABLE flag to true
                     personalRole1.getAccessRights(),
                     personalRole1.getCreationTimestamp(),
                     personalRole1.getModificationTimestamp());
   
            personalRole2 = new Role(
                     personalRole1.getId(), 
                     CallContext.getInstance().getCurrentDomainId(),
                     // changing name and description
                     "testname2", "testdescription2",      
                     // trying to turn off enabled flag and
                     false, DataObject.NEW_ID, false,      
                     // set personal role to non personal
                     null,                                 
                     personalRole1.getCreationTimestamp(),
                     personalRole1.getModificationTimestamp());
   
            Role selectedData;
   
            Thread.sleep(1000);
            
            m_transaction.begin();
            try
            {
               // now try to save the same personal role as personal
               m_roleFactory.save(personalRole2, 
                                  RoleConstants.SAVE_ROLE_PERSONAL);
               // Here we can continue, if there was not error occurred. 
               // PostgreSQL don't throw error message if there was not record 
               // updated (or it was not found)
               m_transaction.commit();                    
            }
            catch (OSSInternalErrorException ieeExc)
            {
               // this exception is expected
               m_transaction.rollback();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw thr;
            }
   
            // Here the save - update is completed
            // We need to test
            selectedData = (Role)m_roleFactory.get(
               personalRole1.getId(),
               CallContext.getInstance().getCurrentDomainId());
            assertNotNull("Role should not be null", selectedData);
            assertEquals("Role is not the same", 
                         selectedData, personalRole1Unmodifiable);
         }
         finally
         {
            m_transaction.begin();
            // delete inserted records
            try
            {
               if ((personalRole1 != null) 
                  && (personalRole1.getId() != DataObject.NEW_ID))
               {
                  // delete role
                  m_roleFactoryUtils.deleteRoleCascadeManual(
                                        personalRole1.getId());
               }
               if ((user1 != null) && (user1.getId() != DataObject.NEW_ID))
               {
                  // delete user
                  m_userFactory.delete(
                     user1.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw thr;
            }
         }  
      }
   
      /**
       * Test of save unmodifiable role method
       * 
       * @throws Exception - an error has occurred during test
       */
      public void testSaveUnmodifiableRole(
      ) throws Exception
      {
         User user1 = null;
         Role testRole;
         Role unmodifiable1 = null;
         Role unmodifiable2 = null;
         
         try
         {  
            m_transaction.begin();
            try
            {
               user1 = new User(
                        DataObject.NEW_ID,
                        CallContext.getInstance().getCurrentDomainId(),
                        "test_first_name_1", "test_last_name_1",
                        "test_phone_1", "test_fax_1", "test_address_1",
                        "test_email_1", "test_login_name_1", "test_password_1",
                        true, true, true, true, null, null, true);
               user1 = (User)m_userFactory.create(user1);
   
               // create unmodifiable role
               unmodifiable1 = new Role(
                        DataObject.NEW_ID, 
                        CallContext.getInstance().getCurrentDomainId(), 
                        "testnameUN", "testdescriptionUN",
                        true, DataObject.NEW_ID, true, null, null, null);
               unmodifiable1 = (Role)m_roleFactory.create(unmodifiable1);
               m_transaction.commit();           
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
           
            // wait to change modification timestamp 
            Thread.sleep(1000);
   
            unmodifiable2 = new Role(
                     unmodifiable1.getId(), 
                     CallContext.getInstance().getCurrentDomainId(), 
                     "testnameUN2", "testdescriptionUN2",
                     false, CallContext.getInstance().getCurrentDomainId(), 
                     true, // this has to be true = unmodifiable 
                     null, 
                     unmodifiable1.getCreationTimestamp(), 
                     unmodifiable1.getModificationTimestamp());
      
            m_transaction.begin();
            try
            {
               testRole = m_roleFactory.save(unmodifiable2, RoleConstants.SAVE_ROLE_ALWAYS);
               m_transaction.commit();                    
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            assertNotNull("Role should not be null", testRole);
            
            Role selectedData;
            // Here the save - update is completed
            // We need to test
            selectedData = (Role)m_roleFactory.get(
                                    unmodifiable1.getId(),
                                    CallContext.getInstance().getCurrentDomainId());
            // Compare with the original data since the roles hould not changed
            assertNotNull("Role should not be null", selectedData);
            // When role is unmodifiable, only name can be updated
            assertEquals("ID from database not equal to inserted personal role ID", 
                     selectedData.getId(), unmodifiable1.getId());          
            // Name should have been changed
            assertEquals("Name from database was not updated", 
                        selectedData.getName(), unmodifiable2.getName());
            assertEquals("Description from database should not be updated", 
                     selectedData.getDescription(), unmodifiable1.getDescription());
            assertEquals("Enabled should not be updated", 
                     selectedData.isEnabled(), unmodifiable1.isEnabled());
            assertEquals("Unmodifiable should not be updated", 
                     selectedData.isUnmodifiable(), unmodifiable1.isUnmodifiable());
            assertEquals("Creation timestamp not equals", 
                     selectedData.getCreationTimestamp(), unmodifiable1.getCreationTimestamp());
            assertNotNull("Modification timestamp is null", 
                     selectedData.getModificationTimestamp());
            assertTrue("Modification timestamp not changed", 
                     selectedData.getModificationTimestamp().getTime() 
                        > unmodifiable1.getModificationTimestamp().getTime());
         }
         finally
         {
            m_transaction.begin();
            // delete inserted records
            try
            {
               if ((unmodifiable1 != null) && (unmodifiable1.getId() != DataObject.NEW_ID))
               {
                  // delete role
                  m_roleFactory.delete(
                                   unmodifiable1.getId(),
                                   CallContext.getInstance().getCurrentDomainId());
               }
               if ((user1 != null) && (user1.getId() != DataObject.NEW_ID))
               {
                  // delete user
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
       * Test of savePersonal method
       * 
       * @throws Exception - an error has occurred 
       */
      public void testSavePersonal(
      ) throws Exception
      {
         User user1           = null;
         User user2           = null;
         Role personalRole    = null;
         Role newPersonalRole = null;
         try
         {      
            user1 = new User(
                     DataObject.NEW_ID,
                     CallContext.getInstance().getCurrentDomainId(),
                     "test_first_name_1", "test_last_name_1",
                     "test_phone_1", "test_fax_1", "test_address_1",
                     "test_email_1", "test_login_name_1", "test_password_1",
                     true, true, true, true, null, null, true);
            m_transaction.begin();
            try
            {
               user1 = (User)m_userFactory.create(user1);
               // create personal role
               personalRole = m_roleFactory.createPersonal(user1);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            
            Thread.sleep(100);
            
            // Change the name of the user
            user2 = new User(user1.getId(),
                              CallContext.getInstance().getCurrentDomainId(),
                              "test_first_name_2", "test_last_name_2",
                              "test_phone_2", "test_fax_2", "test_address_2",
                              "test_email_2", "test_login_name_2", "test_password_2",
                              true, true, true, true,
                              user1.getCreationTimestamp(),
                              user1.getModificationTimestamp(), true);
            
            m_transaction.begin();
            try
            {
               // now save role
               newPersonalRole = m_roleFactory.savePersonal(user2);
               m_transaction.commit();           
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            assertNotNull("Role should not be null", newPersonalRole);
            assertTrue("Modified date not changed", newPersonalRole.getModificationTimestamp() 
                                                    != personalRole.getModificationTimestamp());
            assertEquals("Role name is not the same as user name", 
                         user2.getLoginName(), newPersonalRole.getName());
            assertEquals("Role description is not the same as user name", 
                         user2.getFullNameLastFirst(), newPersonalRole.getDescription());
            
            Role selectedData;
            
            selectedData = (Role)m_roleFactory.get(
                                    personalRole.getId(),
                                    CallContext.getInstance().getCurrentDomainId());
            assertNotNull("Role should not be null", selectedData);
            assertEquals("Role is not the same", selectedData, newPersonalRole);
         }
         finally
         {
            m_transaction.begin();
            // delete inserted records
            try
            {
               if ((personalRole != null) && (personalRole.getId() != DataObject.NEW_ID))
               {
                  // delete role
                  m_roleFactoryUtils.deleteRoleCascadeManual(personalRole.getId());
               }
               if ((user1 != null) && (user1.getId() != DataObject.NEW_ID))
               {
                  // delete user
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
       * test of delete method
       * 
       * @throws Exception - an error has occure
       */
      public void testDeleteMultiple(
      ) throws Exception
      {
         PreparedStatement statement = null;
         ResultSet         results = null;     
         String            strQuery;
            
         Role role1 = null;
         Role role2 = null;
         Role role3 = null;
         Role role4 = null;
         Role unmodifiable = null;
         
         User user = null;
         
         try
         {
            m_transaction.begin();      
            try
            {
               // create 3 roles
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
               
               // create one personal role (first user)
               user = new User(DataObject.NEW_ID,
                  CallContext.getInstance().getCurrentDomainId(),
                  "test_first_name_1", "test_last_name_1",
                  "test_phone_1", "test_fax_1", "test_address_1",
                  "test_email_1", "test_login_name_1", "test_password_1",
                  true, true, true, true, null, null, true);
               user = (User)m_userFactory.create(user);
               // create personal role
               role4 = m_roleFactory.createPersonal(user);
               
               unmodifiable = new Role(DataObject.NEW_ID, 
                                   CallContext.getInstance().getCurrentDomainId(), 
                                   "unmodifiable", "unmodifiabledescription", true, 
                                   DataObject.NEW_ID, true, 
                                   null, null, null);
               unmodifiable = (Role)m_roleFactory.create(unmodifiable);
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            
            int iDeleted;
            
            m_transaction.begin();      
            try
            {
               iDeleted =  m_roleFactory.delete(
                  new int[] {
                     role1.getId(), 
                     role2.getId(),
                     role4.getId(),                 // try to delete personal role
                     unmodifiable.getId(),          // try to delete unmodifiable role 
                     role1.getId() + role2.getId(), // try to delete not existing role
                  },
                  new SimpleRule(SimpleRule.LOGICAL_OR, null),
                  RoleConstants.COUNT_WITH_MODIFIABLE_ONLY
               );
               m_transaction.commit();
               role1 = null;
               role2 = null;
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            
            assertEquals("Number of deleted Roles is incorect", 2, iDeleted);
            
            try 
            {
               strQuery = "select ID, NAME from " + RoleDatabaseSchema.ROLE_TABLE_NAME + " where NAME like 'testname%'";
               statement = m_connection.prepareStatement(strQuery);
               results = statement.executeQuery();
               
               assertTrue("No Role in DB", results.next());
               assertEquals("Role should have not been deleted", role3.getId(), 
                            results.getInt("ID"));
               assertEquals("Role should have not been deleted", role3.getName(), 
                            results.getString("NAME"));
               assertFalse("Deleted Roles in DB", results.next());
            }
            finally
            {
               DatabaseUtils.closeResultSetAndStatement(results, statement);
               results = null;
               statement = null;
            }
   
            //----------------------------------------------------
            
            m_transaction.begin();      
            try
            {
               iDeleted =  m_roleFactory.delete(
                  new int[] {
                     role4.getId(),                 // try to delete personal role
                     unmodifiable.getId(),          // try to delete unmodifiable role 
                     unmodifiable.getId() + 
                     role4.getId(),                  // try to delete not existing role
                  },
                  new SimpleRule(SimpleRule.LOGICAL_OR, null),
                  RoleConstants.COUNT_WITH_UNMODIFIABLE_ONLY
               );
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            
            assertEquals("Number of deleted Roles is incorect", 1, iDeleted);
            
            try 
            {
               strQuery = "select ID, NAME from " + RoleDatabaseSchema.ROLE_TABLE_NAME + " where ID in (?, ?)";
               statement = m_connection.prepareStatement(strQuery);
               statement.setInt(1, role4.getId());
               statement.setInt(2, unmodifiable.getId());
               results = statement.executeQuery();
   
               unmodifiable = null;
               
               assertTrue("No Role in DB", results.next());
               assertEquals("Role should have not been deleted", 
                            role4.getId(), results.getInt("ID"));
               assertEquals("Role should have not been deleted", 
                           role4.getName(), results.getString("NAME"));
               assertFalse("Deleted Roles in DB", results.next());
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
               if ((role4 != null) && (role4.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(role4.getId());
               }
               if ((unmodifiable != null) && (unmodifiable.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(unmodifiable.getId());
               }
               if ((user != null) && (user.getId() != DataObject.NEW_ID))
               {
                  // delete user
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
       * Test of delete method
       * 
       * @throws Exception - an error has occurred
       */
      public void testDelete(
      ) throws Exception
      {
         PreparedStatement statement = null;
         ResultSet         results = null;     
         String            strQuery;
            
         Role role1 = null;
         Role role2 = null;
         Role role3 = null;
         
         int iActual = 0;
         
         try
         {
            m_transaction.begin();      
            try
            {
               // create 3 roles
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
            
            // try to get number of all inserted roles - before delete
            iActual = m_roleFactory.getActualCount(
                          role1.getId() + "," +  role2.getId() + "," + role3.getId());
            
            assertEquals("Number of actual Roles is incorect", 3, iActual);
            
            m_transaction.begin();      
            try
            {
               // delete role 2
               m_roleFactory.delete(
                                role2.getId(),
                                CallContext.getInstance().getCurrentDomainId());
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
               
            // try to get number of all inserted roles - before delete
            iActual = m_roleFactory.getActualCount(
                          role1.getId() + "," +  role2.getId() + "," + role3.getId());
            
            assertEquals("Number of actual Roles is incorect", 2, iActual);
            
            try 
            {
               strQuery = "select ID, NAME from " + RoleDatabaseSchema.ROLE_TABLE_NAME + " where NAME = 'testname2'";
               statement = m_connection.prepareStatement(strQuery);
               results = statement.executeQuery();
               
               assertFalse("There should not be found role 2 because it was already deleted.",
                           results.next());
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
               if ((role1 != null) && (role1.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(role1.getId());
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
       * Test of deletePersonal method
       * 
       * @throws Exception - an error has occurred
       */
      public void testDeletePersonal(
      ) throws Exception
      {
         PreparedStatement statement = null;
         ResultSet         results = null;     
         String            strQuery;
            
         User user1 = null;
         User user2 = null;
         User user3 = null;
         Role role1 = null;
         Role role2 = null;
         Role role3 = null;
         
         try
         {
            m_transaction.begin();      
            try
            {
               // create 3 test users first because personal roles should not be without users...
               user1 = new User(DataObject.NEW_ID,
                                CallContext.getInstance().getCurrentDomainId(),
                                "test_first_name_1", "test_last_name_1",
                                "test_phone_1", "test_fax_1", "test_address_1",
                                "test_email_1", "test_login_name_1", "test_password_1",
                                true, true, true, true, null, null, true);
               user1 = (User)m_userFactory.create(user1);
            
               user2 = new User(DataObject.NEW_ID,
                                CallContext.getInstance().getCurrentDomainId(),
                                "test_first_name_2", "test_last_name_2",
                                "test_phone_2", "test_fax_2", "test_address_2",
                                "test_email_2", "test_login_name_2", "test_password_2",
                                true, true, true, true, null, null, true);
               user2 = (User)m_userFactory.create(user2);
   
               user3 = new User(DataObject.NEW_ID,
                                CallContext.getInstance().getCurrentDomainId(),
                                "test_first_name_3", "test_last_name_3",
                                "test_phone_3", "test_fax_3", "test_address_3", 
                                "test_email_3", "test_login_name_3", "test_password_3",
                                true, true, true, true, null, null, true);
               user3 = (User)m_userFactory.create(user3);
               
               // create personal Roles
               role1 = m_roleFactory.createPersonal(user1);
               role2 = m_roleFactory.createPersonal(user2);
               role3 = m_roleFactory.createPersonal(user3);
               
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            
            int iDeleted;
            
            m_transaction.begin();      
            try
            {
               iDeleted =  m_roleFactory.deletePersonal(
                  new int[] {
                     user1.getId(), // personal role 
                     user2.getId(), // personal role
                     DataObject.NEW_ID, // try to delete all other roles through this method
                     (user1.getId() + user2.getId() + user3.getId()) * 2 + 1, // not existing ID
                  } 
               );
               m_transaction.commit();
               role1 = null;
               role2 = null;
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
               
            assertEquals("Number of deleted Personal Roles is incorect", 2, iDeleted);
            
            try 
            {
               strQuery = "select ID, NAME from " + RoleDatabaseSchema.ROLE_TABLE_NAME + " where NAME = 'test_login_name_3'";
               statement = m_connection.prepareStatement(strQuery);
               results = statement.executeQuery();
               
               assertTrue("No Personal Role in DB", results.next());
               
               assertEquals("Role should have not been deleted", 
                            role3.getId(), results.getInt("ID"));
               assertEquals("Role should have not been deleted", 
                            role3.getName(), results.getString("NAME"));
               assertFalse("Deleted Personal Roles still in DB", results.next());
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
               if ((user1 != null) && (user1.getId() != DataObject.NEW_ID))
               {
                  // delete user
                  m_userFactory.delete(
                                   user1.getId(),
                                   CallContext.getInstance().getCurrentDomainId());
               }
               if ((user2 != null) && (user2.getId() != DataObject.NEW_ID))
               {
                  // delete user
                  m_userFactory.delete(
                                   user2.getId(),
                                   CallContext.getInstance().getCurrentDomainId());
               }
               if ((user3 != null) && (user3.getId() != DataObject.NEW_ID))
               {
                  // delete user
                  m_userFactory.delete(
                                   user3.getId(),
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
       * test of assignRoleToUser method
       * 
       * @throws Exception - an error has occurred
       */
      public void testAssignRoleToUser(
      ) throws Exception
      {
         PreparedStatement statement = null;
         ResultSet         results = null;     
         String            strQuery;
               
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
            catch (Exception exc)
            {
               m_transaction.rollback();
               throw exc;
            }
      
            try 
            {
               int iAdded = m_roleFactory.assignToUser(
                  CallContext.getInstance().getCurrentUserId(),
                  role1.getId() + "," + role2.getId());
               
               assertEquals("Added Roles number is not correct", 2, iAdded);
               
               strQuery = "select ROLE_ID from " + UserDatabaseSchema.USER_TABLE_NAME + "_ROLE_MAP where USER_ID=?";
               statement = m_connection.prepareStatement(strQuery);
               statement.setInt(1, CallContext.getInstance().getCurrentUserId());
               results = statement.executeQuery();
               
               assertTrue("No Roles are assigned to User", results.next());
               
               assertTrue("No Correct Role is assigned to User", 
                  results.getInt("ROLE_ID") == role1.getId()
                  || results.getInt("ROLE_ID") == role2.getId());
               
               if (results.getInt("ROLE_ID") == role1.getId())
               {
                  assertTrue("Role is not assigned to User", results.next());
                  assertTrue("Incorrect role is assigned to User", 
                             results.getInt("ROLE_ID") == role2.getId());
               }
               else
               {
                  assertTrue("Role is not assigned to User", results.next());
                  assertTrue("Incorrect role is assigned to User", 
                             results.getInt("ROLE_ID") == role1.getId());
               }
               
               assertFalse("Some more Roles are assigned to User", results.next());
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
       * test of assignRoleToDomain method
       * 
       * @throws Exception - an error has occurred
       */
      public void testAssignRoleToDomain(
      ) throws Exception
      {
         PreparedStatement statement = null;
         ResultSet         results = null;     
         String            strQuery;
               
         Domain domain1 = null;
         Role role1 = null;
         Role role2 = null;
         Role role3 = null;
         
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
                                "testname1", "testdescription1", true, DataObject.NEW_ID, 
                                false, null, null, null);
               role1 = (Role)m_roleFactory.create(role1);
         
               role2 = new Role(DataObject.NEW_ID, 
                                domain1.getId(), 
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
            catch (Exception exc)
            {
               m_transaction.rollback();
               throw exc;
            }
      
            try 
            {
               int iAdded = m_roleFactory.assignToDomain(domain1.getId(), 
                                             role1.getId() + "," + role2.getId());
               
               assertEquals("Added Roles number is not correct", 2, iAdded);
               
               strQuery = "select ROLE_ID from " + DomainDatabaseSchema.DOMAIN_TABLE_NAME + "_ROLE_MAP where DOMAIN_ID=?";
               statement = m_connection.prepareStatement(strQuery);
               statement.setInt(1, domain1.getId());
               results = statement.executeQuery();
               
               assertTrue("No Roles are assigned to Domain 1", results.next());
               
               assertTrue("Incorrect Role is assigned to Domain 1", 
                  results.getInt("ROLE_ID") == role1.getId()
                  || results.getInt("ROLE_ID") == role2.getId());
               
               if (results.getInt("ROLE_ID") == role1.getId())
               {
                  assertTrue("Role is not assigned to Domain", results.next());
                  assertTrue("Incorrect role is assigned to Domain", 
                             results.getInt("ROLE_ID") == role2.getId());
               }
               else
               {
                  assertTrue("Role is not assigned to Domain", results.next());
                  assertTrue("Incorrect role is assigned to Domain", 
                             results.getInt("ROLE_ID") == role1.getId());
               }
               
               assertFalse("Some more Roles are assigned to Domain", results.next());
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
               try
               {
                  // delete mapped data
                  strQuery = "delete from " + DomainDatabaseSchema.DOMAIN_TABLE_NAME + "_ROLE_MAP where DOMAIN_ID in (?, ?)" +
                             " and ROLE_ID in (?, ?, ?)";
                  statement = m_connection.prepareStatement(strQuery);
                  statement.setInt(1, domain1.getId());
                  statement.setInt(2, CallContext.getInstance().getCurrentDomainId());
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
                  strQuery = "delete from " + RoleDatabaseSchema.ROLE_TABLE_NAME + " where DOMAIN_ID in (?, ?)" +
                             " and ID in (?, ?, ?)";
                  statement = m_connection.prepareStatement(strQuery);
                  statement.setInt(1, domain1.getId());
                  statement.setInt(2, CallContext.getInstance().getCurrentDomainId());
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
       * test of removeFromUser method
       * 
       * @throws Exception - error during test
       */
      public void testRemoveFromUser(
      ) throws Exception
      {
         PreparedStatement statement = null;
         ResultSet         results = null;     
         String            strQuery;
               
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
      
            m_transaction.begin();
            try
            {
               try
               {
                  strQuery = m_roleSchema.getAssignRoleToUser();
                  statement = m_connection.prepareStatement(strQuery);
                  statement.setInt(1, CallContext.getInstance().getCurrentUserId());
                  statement.setInt(2, role1.getId());
                  statement.executeUpdate();
               }
               finally
               {
                  DatabaseUtils.closeResultSetAndStatement(results, statement);
               }
               try
               {
                  strQuery = m_roleSchema.getAssignRoleToUser();
                  statement = m_connection.prepareStatement(strQuery);
                  statement.setInt(1, CallContext.getInstance().getCurrentUserId());
                  statement.setInt(2, role2.getId());
                  statement.executeUpdate();
               }
               finally
               {
                  DatabaseUtils.closeResultSetAndStatement(results, statement);
               }
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
         
            int iRemoved = 0;
      
            m_transaction.begin();
            try
            {
               iRemoved = m_roleFactory.removeFromUser(
                     CallContext.getInstance().getCurrentUserId(),
                     role1.getId() + "," + role2.getId());
               
               assertEquals("Removed Roles number is not correct", 2, iRemoved);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
               
            try 
            {
               strQuery = "select ROLE_ID from " + UserDatabaseSchema.USER_TABLE_NAME + "_ROLE_MAP where USER_ID=?";
               statement = m_connection.prepareStatement(strQuery);
               statement.setInt(1, CallContext.getInstance().getCurrentUserId());
               results = statement.executeQuery();
               
               assertFalse("Some Roles are still assigned to User", results.next());
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
               if ((role3 != null) && (role3.getId() != DataObject.NEW_ID))
               {
                  m_roleFactory.delete(
                                   role3.getId(),
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
       * test of removeFromDomain method
       * 
       * @throws Exception - error during test
       */
      public void testRemoveFromDomain(
      ) throws Exception
      {
         PreparedStatement statement = null;
         ResultSet         results = null;     
         String            strQuery;
               
         Domain domain1 = null;
         Role role1 = null;
         Role role2 = null;
         Role role3 = null;
         
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
                                "testname1", "testdescription1", true, DataObject.NEW_ID, 
                                false, null, null, null);
               role1 = (Role)m_roleFactory.create(role1);
         
               role2 = new Role(DataObject.NEW_ID, 
                                domain1.getId(), 
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
      
            m_transaction.begin();
            try
            {
               try
               {
                  strQuery = m_roleSchema.getAssignRoleToDomain();
                  statement = m_connection.prepareStatement(strQuery);
                  statement.setInt(1, domain1.getId());
                  statement.setInt(2, role1.getId());
                  statement.executeUpdate();
               }
               finally
               {
                  DatabaseUtils.closeResultSetAndStatement(results, statement);
               }
               try
               {
                  strQuery = m_roleSchema.getAssignRoleToDomain();
                  statement = m_connection.prepareStatement(strQuery);
                  statement.setInt(1, domain1.getId());
                  statement.setInt(2, role2.getId());
                  statement.executeUpdate();
               }
               finally
               {
                  DatabaseUtils.closeResultSetAndStatement(results, statement);
               }
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
         
            int iRemoved = 0;
      
            m_transaction.begin();
            try
            {
               iRemoved = m_roleFactory.removeFromDomain(domain1.getId(),
                                           role1.getId() + "," + role2.getId());
               
               assertEquals("Removed Roles number is not correct", 2, iRemoved);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
               
            try 
            {
               strQuery = "select ROLE_ID from " + DomainDatabaseSchema.DOMAIN_TABLE_NAME + "_ROLE_MAP where DOMAIN_ID=?";
               statement = m_connection.prepareStatement(strQuery);
               statement.setInt(1, domain1.getId());
               results = statement.executeQuery();
               
               assertFalse("Some Roles are still assigned to User", results.next());
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
               try
               {
                  strQuery = "delete from " + RoleDatabaseSchema.ROLE_TABLE_NAME + " where DOMAIN_ID in (?, ?)" +
                             " and ID in (?, ?, ?)";
                  statement = m_connection.prepareStatement(strQuery);
                  statement.setInt(1, domain1.getId());
                  statement.setInt(2, CallContext.getInstance().getCurrentDomainId());
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
       * Test of removeFromUsers method
       * 
       * @throws Exception - error during test
       */
      public void testRemoveFromUsers(
      ) throws Exception
      {
         PreparedStatement statement = null;
         ResultSet         results = null;     
         String            strQuery;
               
         User user1 = null;
         User user2 = null;
         User user3 = null;
   
         try
         {
            m_transaction.begin();      
            try
            {
               // create 3 test users first because personal roles should not be without users...
               user1 = new User(DataObject.NEW_ID,
                                CallContext.getInstance().getCurrentDomainId(),
                                "test_first_name_1", "test_last_name_1",
                                "test_phone_1", "test_fax_1", "test_address_1",
                                "test_email_1", "test_login_name_1", "test_password_1",
                                true, true, true, true, null, null, true);
               user1 = (User)m_userFactory.create(user1);
            
               user2 = new User(DataObject.NEW_ID,
                                CallContext.getInstance().getCurrentDomainId(),
                                "test_first_name_2", "test_last_name_2",
                                "test_phone_2", "test_fax_2", "test_address_2",
                                "test_email_2", "test_login_name_2", "test_password_2",
                                true, true, true, true, null, null, true);
               user2 = (User)m_userFactory.create(user2);
   
               user3 = new User(DataObject.NEW_ID,
                                CallContext.getInstance().getCurrentDomainId(),
                                "test_first_name_3", "test_last_name_3",
                                "test_phone_3", "test_fax_3", "test_address_3",
                                "test_email_3", "test_login_name_3", "test_password_3",
                                true, true, true, true, null, null, true);
               user3 = (User)m_userFactory.create(user3);
               
               // create personal Roles
               m_roleFactory.createPersonal(user1);
               m_roleFactory.createPersonal(user2);
               m_roleFactory.createPersonal(user3);
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
      
            int iRemoved = 0;
      
            m_transaction.begin();
            try
            {
               // try to remove all except specified roles from users (it means 
               // there will be deleted only role from user 2) 
               iRemoved = m_roleFactory.removeFromUsers(
                     String.valueOf(user2.getId()), false);
               
               assertEquals("Removed Roles number is not correct", 1, iRemoved);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
               
            try 
            {
               strQuery = "select ROLE_ID from " + UserDatabaseSchema.USER_TABLE_NAME + "_ROLE_MAP where USER_ID=?";
               statement = m_connection.prepareStatement(strQuery);
               statement.setInt(1, user2.getId());
               results = statement.executeQuery();
               
               assertFalse("Some Roles are still assigned to User", results.next());
            }
            finally
            {
               DatabaseUtils.closeResultSetAndStatement(results, statement);
            }
   
            m_transaction.begin();
            try
            {
               // try to remove all specified roles from users (it means 
               // there will be deleted roles from user 1 and 3) 
               iRemoved = m_roleFactory.removeFromUsers(
                     user1.getId() + "," + user3.getId(), false);
               
               assertEquals("Removed Roles number is not correct", 2, iRemoved);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
               
            try 
            {
               strQuery = "select ROLE_ID from " + UserDatabaseSchema.USER_TABLE_NAME + "_ROLE_MAP where USER_ID in (?, ?)";
               statement = m_connection.prepareStatement(strQuery);
               statement.setInt(1, user1.getId());
               statement.setInt(2, user3.getId());
               results = statement.executeQuery();
               
               assertFalse("Some Roles are still assigned to User", results.next());
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
               if ((user1 != null) && (user1.getId() != DataObject.NEW_ID))
               {
                  m_userFactoryUtils.deleteUserCascadeManual(user1.getId());
               }
               if ((user2 != null) && (user2.getId() != DataObject.NEW_ID))
               {
                  m_userFactoryUtils.deleteUserCascadeManual(user2.getId());
               }
               if ((user3 != null) && (user3.getId() != DataObject.NEW_ID))
               {
                  m_userFactoryUtils.deleteUserCascadeManual(user3.getId());
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
       * Test of removeFromDomains method
       * 
       * @throws Exception - error during test
       */
      public void testRemoveFromDomains(
      ) throws Exception
      {
         PreparedStatement statement = null;
         ResultSet         results = null;     
         String            strQuery;
               
         Domain domain1 = null;
         Domain domain2 = null;
         Domain domain3 = null;
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

               domain3 = new Domain(DataObject.NEW_ID,
                                    "test_domain_name3",
                                    "test_domain_description3",
                                    true, true, false, false, false, false, false,
                                    "test_phone3", "test_domain_address3",
                                    null, null, null
                                   );
               domain3 = (Domain)m_domainFactory.create(domain3);
               
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
      
            int iRemoved = 0;
      
            m_transaction.begin();
            try
            {
               // try to remove all except specified roles from domains (it means 
               // there will be deleted only role from domain 2) 
               iRemoved = m_roleFactory.removeFromDomains(
                     String.valueOf(domain2.getId()), false);
               
               assertEquals("Removed Roles number is not correct", 1, iRemoved);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
               
            try 
            {
               strQuery = "select ROLE_ID from " + DomainDatabaseSchema.DOMAIN_TABLE_NAME + "_ROLE_MAP where DOMAIN_ID=?";
               statement = m_connection.prepareStatement(strQuery);
               statement.setInt(1, domain2.getId());
               results = statement.executeQuery();
               
               assertFalse("Some Roles are still assigned to Domain", results.next());
            }
            finally
            {
               DatabaseUtils.closeResultSetAndStatement(results, statement);
            }
   
            m_transaction.begin();
            try
            {
               // try to remove all specified roles from domains (it means 
               // there will be deleted roles from domain 1 and 3) 
               iRemoved = m_roleFactory.removeFromDomains(
                     domain1.getId() + "," + domain3.getId(), false);
               
               assertEquals("Removed Roles number is not correct", 2, iRemoved);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
               
            try 
            {
               strQuery = "select ROLE_ID from " + DomainDatabaseSchema.DOMAIN_TABLE_NAME + "_ROLE_MAP where DOMAIN_ID in (?, ?)";
               statement = m_connection.prepareStatement(strQuery);
               statement.setInt(1, domain1.getId());
               statement.setInt(2, domain3.getId());
               results = statement.executeQuery();
               
               assertFalse("Some Roles are still assigned to Domain", results.next());
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
         PreparedStatement statement = null;
         ResultSet         results = null;     
         String            strQuery;
         
         User user  = null;
         Role role1 = null;
         Role role2 = null;
         Role role3 = null;
         Role role4 = null;
         Role personalRole = null;
         Role unmodifiable = null;
   
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
      
               role4 = new Role(DataObject.NEW_ID, 
                                CallContext.getInstance().getCurrentDomainId(), 
                                "testname4", "testdescription4", true, DataObject.NEW_ID, 
                                false, null, null, null);
               role4 = (Role)m_roleFactory.create(role4);
               
               // create one personal role (first user)
               user = new User(DataObject.NEW_ID,
                  CallContext.getInstance().getCurrentDomainId(),
                  "test_first_name_1", "test_last_name_1",
                  "test_phone_1", "test_fax_1", "test_address_1",
                  "test_email_1", "test_login_name_1", "test_password_1",
                  true, true, true, true, null, null, true);
               user = (User)m_userFactory.create(user);
               // create personal role
               personalRole = m_roleFactory.createPersonal(user);
   
               unmodifiable = new Role(
                        DataObject.NEW_ID,
                        CallContext.getInstance().getCurrentDomainId(), 
                        "unmodifiable", "unmodifiabledescription", true, 
                        DataObject.NEW_ID, true, null, null, null);
               unmodifiable = (Role)m_roleFactory.create(unmodifiable);
   
               m_transaction.commit();         
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
      
            Thread.sleep(1000);
   
            int iEnabled = m_roleFactory.updateEnable(
                  new int[] {
                           role1.getId(), 
                           role2.getId(),
                           personalRole.getId(),          // try to disable personal role
                           unmodifiable.getId(),          // try to disable unmodifiable role
                           (role2.getId() + role1.getId() + 
                            role3.getId() + role4.getId() +
                            personalRole.getId() +
                            unmodifiable.getId()),         // non existing ID
                           },
                  false,
                  new SimpleRule(SimpleRule.LOGICAL_OR, null)
                  );
            
            assertEquals("Number of disabled Roles is incorect", 2, iEnabled);
            
            try 
            {
               strQuery = "select ID, NAME, ENABLED, MODIFICATION_DATE from " + RoleDatabaseSchema.ROLE_TABLE_NAME + " " +
                          "where ID in (?, ?, ?, ?, ?, ?) order by ID";
               statement = m_connection.prepareStatement(strQuery);
               statement.setInt(1, role1.getId());
               statement.setInt(2, role2.getId());
               statement.setInt(3, role3.getId());
               statement.setInt(4, role4.getId());
               statement.setInt(5, personalRole.getId());
               statement.setInt(6, unmodifiable.getId());
               results = statement.executeQuery();
               
               assertTrue("Role1 not in DB", results.next());
               assertEquals("Role1 ID not correct", role1.getId(), results.getInt("ID"));
               assertEquals("Role1 name changed", role1.getName(), results.getString("NAME"));
               assertEquals("Role1 not disabled", 0, results.getInt("ENABLED"));
               assertTrue("Role1 modification date was not changed",
                          role1.getModificationTimestamp().before(
                                   results.getTimestamp("MODIFICATION_DATE")));
                  
               assertTrue("Role2 not in DB", results.next());
               assertEquals("Role2 ID not correct", role2.getId(), results.getInt("ID"));
               assertEquals("Role2 name changed", role2.getName(), results.getString("NAME"));
               assertEquals("Role2 not disabled", 0, results.getInt("ENABLED"));
               assertTrue("Role2 modification date was not changed",
                          role2.getModificationTimestamp().before(
                                   results.getTimestamp("MODIFICATION_DATE")));
      
               assertTrue("Role3 not in DB", results.next());
               assertEquals("Role3 ID not correct", role3.getId(), results.getInt("ID"));
               assertEquals("Role3 name changed", role3.getName(), results.getString("NAME"));
               assertEquals("Role3 not enabled", role3.isEnabled() ? 1 : 0,
                                                 results.getInt("ENABLED"));
               assertTrue("Role3 modification date should not be changed",
                          role3.getModificationTimestamp().equals(
                                   results.getTimestamp("MODIFICATION_DATE")));
   
               assertTrue("Role4 not in DB", results.next());
               assertEquals("Role4 ID not correct", role4.getId(), results.getInt("ID"));
               assertEquals("Role4 name changed", role4.getName(), results.getString("NAME"));
               assertEquals("Role4 not enabled", role4.isEnabled() ? 1 : 0, 
                                                 results.getInt("ENABLED"));
               assertTrue("Role4 modification date should not be changed",
                          role4.getModificationTimestamp().equals(
                                   results.getTimestamp("MODIFICATION_DATE")));
   
               assertTrue("Personal Role not in DB", results.next());
               assertEquals("Personal Role ID not correct", personalRole.getId(), 
                                                            results.getInt("ID"));
               assertEquals("Personal Role name changed", personalRole.getName(), 
                                                          results.getString("NAME"));
               assertEquals("Personal Role not enabled", personalRole.isEnabled() ? 1 : 0, 
                                                         results.getInt("ENABLED"));
               assertTrue("Personal Role modification date should not be changed",
                          personalRole.getModificationTimestamp().equals(
                                   results.getTimestamp("MODIFICATION_DATE")));
   
               assertTrue("Unmodifiable Role not in DB", results.next());
               assertEquals("Unmodifiable Role ID not correct", unmodifiable.getId(), 
                                                                results.getInt("ID"));
               assertEquals("Unmodifiable Role name changed", unmodifiable.getName(), 
                                                              results.getString("NAME"));
               assertEquals("Unmodifiable Role not enabled", unmodifiable.isEnabled() ? 1 : 0, 
                                                             results.getInt("ENABLED"));
               assertTrue("Unmodifiable Role modification date should not be changed",
                          unmodifiable.getModificationTimestamp().equals(
                                   results.getTimestamp("MODIFICATION_DATE")));
   
               assertFalse("More Roles in DB", results.next());
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
               if ((role4 != null) && (role4.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(role4.getId());
               }
               if ((personalRole != null) && (personalRole.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(personalRole.getId());
               }
               if ((unmodifiable != null) && (unmodifiable.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(unmodifiable.getId());
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
   }
}
