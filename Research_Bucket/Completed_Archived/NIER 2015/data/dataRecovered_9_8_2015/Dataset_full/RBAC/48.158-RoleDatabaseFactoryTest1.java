/*
 * Copyright (c) 2003 - 2009 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: RoleDatabaseFactoryTest1.java,v 1.13 2009/09/20 05:32:57 bastafidli Exp $
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.opensubsystems.security.utils.TestRoleDatabaseFactoryUtils;
import org.opensubsystems.security.utils.TestUserDatabaseFactoryUtils;

/**
 * Test of RoleDatabaseFactory
 * 
 * @version $Id: RoleDatabaseFactoryTest1.java,v 1.13 2009/09/20 05:32:57 bastafidli Exp $
 * @author Peter Satury
 * @code.reviewer Miro Halas
 * @code.reviewed Initial revision
 */
public final class RoleDatabaseFactoryTest1 extends Tests
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private RoleDatabaseFactoryTest1(
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
      TestSuite suite = new DatabaseTestSuite("RoleDatabaseFactoryTest1");
      suite.addTestSuite(RoleDatabaseFactoryTestInternal1.class);
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
   public static class RoleDatabaseFactoryTestInternal1 extends SecureListDatabaseFactoryTest
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
       * Data descriptor for the user data types.
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
      public RoleDatabaseFactoryTestInternal1(
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
       * Test of getRole method by unique ID
       * 
       * @throws Exception - an error has occurred during test
       */
      public void testGetById(
      ) throws Exception
      {
         Role data1 = null; 
         Role data2 = null; 
         try
         {
            data1 = new Role(
                     DataObject.NEW_ID, 
                     CallContext.getInstance().getCurrentDomainId(), 
                     "testname", "testdescription",
                     true, DataObject.NEW_ID, true, null, null, null
            );
            
            data2 = new Role(
                     DataObject.NEW_ID, 
                     CallContext.getInstance().getCurrentDomainId(), 
                     "testname2", "testdescription2",
                     false, DataObject.NEW_ID, false, null, null, 
                     data1.getModificationTimestamp()
            );
   
            // Create two roles to make sure we select the correct one
            m_transaction.begin();
            try
            {
               data1 = (Role)m_roleFactory.create(data1);
               data2 = (Role)m_roleFactory.create(data2);
               m_transaction.commit();           
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
         
            Role testData = (Role)m_roleFactory.get(
                                     data2.getId(),
                                     CallContext.getInstance().getCurrentDomainId());
                  
            assertNotNull("Role should not be null", testData);
            assertEquals("Role is not the same", data2, testData);
         }
         finally
         {
            m_transaction.begin();
            // delete inserted records
            try
            {
               if ((data1 != null) && (data1.getId() != DataObject.NEW_ID))
               {
                  // delete role
                  m_roleFactory.delete(
                                   data1.getId(),
                                   CallContext.getInstance().getCurrentDomainId());
               }
               if ((data2 != null) && (data2.getId() != DataObject.NEW_ID))
               {
                  // delete user
                  m_roleFactory.delete(
                                   data2.getId(),
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
       * Test of getPersonalRole method by unique ID
       * @throws Exception - an error has occurred during test
       */
      public void testGetPersonalById(
      )  throws Exception
      {
         User user1         = null;
         User user2         = null;
         Role personalRole1 = null; 
         Role personalRole2 = null;
         try
         {
            // Create two roles to make sure we select the correct one
            m_transaction.begin();
            try
            {
               user1 = new User(
                        DataObject.NEW_ID,
                        CallContext.getInstance().getCurrentDomainId(),
                        "test_first_name_1",
                        "test_last_name_1",
                        "test_phone_1",
                        "test_fax_1",
                        "test_address_1",
                        "test_email_1",
                        "test_login_name_1",
                        "test_password_1",
                        true, true, true, true, null, null, true
               );
               user1 = (User)m_userFactory.create(user1);
               // create personal role
               personalRole1 = m_roleFactory.createPersonal(user1);
      
               user2 = new User(
                        DataObject.NEW_ID,
                        CallContext.getInstance().getCurrentDomainId(),
                        "test_first_name_2",
                        "test_last_name_2",
                        "test_phone_2",
                        "test_fax_2",
                        "test_address_2",
                        "test_email_2",
                        "test_login_name_2",
                        "test_password_2",
                        true, true, true, true, null, null, true
               );
               user2 = (User)m_userFactory.create(user2);
               // create personal role
               personalRole2 = m_roleFactory.createPersonal(user2);
               m_transaction.commit();           
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            Role testData = (new RoleDatabaseFactory()).getPersonal(user2.getId());
                  
            assertNotNull("Role should not be null", testData);
            assertEquals("Role is not the same", personalRole2, testData);
            assertEquals("Loaded Name not equal to user LoginName", 
                     user2.getLoginName(), testData.getName());
            assertEquals("Loaded Description is invalid", 
                     user2.getFullNameLastFirst(), testData.getDescription());
            assertTrue("Loaded Enabled should be allways enabled", 
                       testData.isEnabled());
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
               if ((personalRole2 != null) && (personalRole2.getId() != DataObject.NEW_ID))
               {
                  // delete role
                  m_roleFactoryUtils.deleteRoleCascadeManual(personalRole2.getId());
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
       * Test of get method using incorrect ID
       * 
       * @throws Exception - an error has occurred
       */
      public void testGetByIncorrectId(
      )  throws Exception
      {
         Role data       = null;
         Role returnedData = null;
         try
         {
            data = new Role(
                     DataObject.NEW_ID, 
                     CallContext.getInstance().getCurrentDomainId(), 
                     "testname", "testdescription",
                     true, DataObject.NEW_ID, false, null, null, null
            );
            
            m_transaction.begin();
            try
            {
               data = (Role)m_roleFactory.create(data);
               m_transaction.commit();           
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            
            // using incorrect ID
            returnedData = (Role)m_roleFactory.get(
                                    (data.getId() * 2) + 1,
                                    CallContext.getInstance().getCurrentDomainId());
            
            assertNull("Role should not have been retrieved", returnedData);
         }
         finally
         {
            m_transaction.begin();
            // delete inserted records
            try
            {
               if ((data != null) && (data.getId() != DataObject.NEW_ID))
               {
                  // delete role
                  m_roleFactoryUtils.deleteRoleCascadeManual(data.getId());
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
       * Test of getPersonalRole method by incorrect ID
       * 
       * @throws Exception - an error has occurred during test
       */
      public void testGetPersonalByIncorrectId(
      )  throws Exception
      {
         User user               = null;
         Role personalRole       = null;
         Role returnPersonalRole = null;
         
         try
         {
            user = new User(
                     DataObject.NEW_ID,
                     CallContext.getInstance().getCurrentDomainId(),
                     "test_first_name_1",
                     "test_last_name_1",
                     "test_phone_1",
                     "test_fax_1",
                     "test_address_1",
                     "test_email_1",
                     "test_login_name_1",
                     "test_password_1",
                     true, true, true, true, null, null, true
            );
            m_transaction.begin();
            try 
            {
               user = (User)m_userFactory.create(user);
               // create personal role
               personalRole = m_roleFactory.createPersonal(user);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            // using incorrect ID
            returnPersonalRole = m_roleFactory.getPersonal((user.getId() * 2) + 1);
            
            assertNull("Role should not have been retrieved", returnPersonalRole);
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
       * Test of isAnyOfSpecifiedPersonalRole method where roles are only non personal 
       *  
       * @throws Exception - an error has occurred
       */
      public void testIsAnyOfSpecifiedPersonalRoleNegative(
      ) throws Exception
      {
         Role data1 = null; 
         Role data2 = null;
         boolean isAtLeastOnePersonal = true; // true because it should be false in the test  
         try
         {
            data1 = new Role(
                     DataObject.NEW_ID, 
                     CallContext.getInstance().getCurrentDomainId(), 
                     "testname", "testdescription",
                     true, DataObject.NEW_ID, false, null, null, null);
            
            data2 = new Role(
                        DataObject.NEW_ID, CallContext.getInstance().getCurrentDomainId(), 
                        "testname2", "testdescription2",
                        false, DataObject.NEW_ID, false, null, null, 
                        data1.getModificationTimestamp());
   
            m_transaction.begin();
            try
            {
               data1 = (Role)m_roleFactory.create(data1);
               data2 = (Role)m_roleFactory.create(data2);
               m_transaction.commit();           
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
         
            Set uniqueSetOfIds = new HashSet();
            uniqueSetOfIds.add(Integer.toString(data1.getId()));
            uniqueSetOfIds.add(Integer.toString(data2.getId()));
   
            isAtLeastOnePersonal = m_roleFactory.isAnyOfSpecifiedPersonal(
                        uniqueSetOfIds);
            
            assertFalse("None of specified roles is personal", isAtLeastOnePersonal);               
         }
         finally
         {
            m_transaction.begin();
            // delete inserted records
            try
            {
               if ((data1 != null) && (data1.getId() != DataObject.NEW_ID))
               {
                  // delete role
                  m_roleFactoryUtils.deleteRoleCascadeManual(data1.getId());
               }
               if ((data2 != null) && (data2.getId() != DataObject.NEW_ID))
               {
                  // delete role
                  m_roleFactoryUtils.deleteRoleCascadeManual(data2.getId());
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
       * Test of isAnyOfSpecifiedPersonalRole method where one role is personal  
       * 
       * @throws Exception - an error has occurred
       */
      public void testIsAnyOfSpecifiedPersonalRolePositive(
      ) throws Exception
      {
         User user     = null;
         Role data     = null; 
         Role personalRole = null;
         boolean isAtLeastOnePersonal = false; // false because it should be true in the test  
         try
         {
            user = new User(
                     DataObject.NEW_ID,
                     CallContext.getInstance().getCurrentDomainId(),
                     "test_first_name_1",
                     "test_last_name_1",
                     "test_phone_1",
                     "test_fax_1",
                     "test_address_1",
                     "test_email_1",
                     "test_login_name_1",
                     "test_password_1",
                     true, true, true, true, null, null, true
            );
            
            data = new Role(
                     DataObject.NEW_ID, 
                     CallContext.getInstance().getCurrentDomainId(), 
                     "testname", "testdescription",
                     true, DataObject.NEW_ID, false, null, null, null);
   
            m_transaction.begin();
            try
            {
               // create user
               user = (User)m_userFactory.create(user);
               // create personal Role
               personalRole = m_roleFactory.createPersonal(user);
               // create role
               data = (Role)m_roleFactory.create(data);
               m_transaction.commit();           
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
         
            Set uniqueSetOfIds = new HashSet();
            uniqueSetOfIds.add(Integer.toString(personalRole.getId()));
            uniqueSetOfIds.add(Integer.toString(data.getId()));
            
            isAtLeastOnePersonal = m_roleFactory.isAnyOfSpecifiedPersonal(
                                                    uniqueSetOfIds);
            
            assertTrue("At least one of specified roles is personal", isAtLeastOnePersonal);
         }
         finally
         {
            m_transaction.begin();
            // delete inserted records
            try
            {
               if ((data != null) && (data.getId() != DataObject.NEW_ID))
               {
                  // delete role
                  m_roleFactoryUtils.deleteRoleCascadeManual(data.getId());
               }
               if ((personalRole != null) && (personalRole.getId() != DataObject.NEW_ID))
               {
                  // delete role
                  m_roleFactoryUtils.deleteRoleCascadeManual(personalRole.getId());
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
       * Test of getRole method by default NEW_ID
       * 
       * @throws Exception - an error has occurred 
       */
      public void testGetRoleByNewId(
      ) throws Exception
      {
         Role testData = (Role)m_roleFactory.get(
                                  DataObject.NEW_ID,
                                  CallContext.getInstance().getCurrentDomainId());
         
         assertNotNull("Role called by NEW_ID is null", testData);
         assertEquals("ID is not initialized to NEW_ID", 
                      DataObject.NEW_ID, testData.getId());
      }   
   
      /**
       * Test of loadRole utility 
       * @throws Exception - an error has occurred during test
       */
      public void testLoadRole(
      )  throws Exception
      {
          
         PreparedStatement statement = null;
         ResultSet results = null;      
         
         Role data = null;
         
         try
         {       
            data = new Role(DataObject.NEW_ID, CallContext.getInstance().getCurrentDomainId(), 
                            "testname", "testdescription", true, DataObject.NEW_ID, 
                            false, null, null, null);
            
            m_transaction.begin();
            try
            {
               data = (Role)m_roleFactory.create(data);
               m_transaction.commit();           
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
         
            try
            {
               StringBuffer sbBuffer = new StringBuffer();
               
               sbBuffer.append("select ");
               m_roleSchema.getColumns(false, 
                                       m_roleDescriptor.getAllColumnCodes(), 
                                       null, null, sbBuffer);
               sbBuffer.append(" from " + RoleDatabaseSchema.ROLE_TABLE_NAME 
                               + " order by CREATION_DATE desc");
               statement = m_connection.prepareStatement(sbBuffer.toString());
               results = statement.executeQuery();
            
               assertTrue("Inserted item not found", results.next());   
            
               Role loadData;
               
               loadData = (Role)m_roleFactory.load(results, 1);
   
               int iUserId = results.getInt(m_roleSchema.getColumns(
                                            false,
                                            new int[] {RoleDataDescriptor.COL_ROLE_USER_ID}, null, null,
                                            null
                                            ).toString());
               if (results.wasNull())
               {
                  iUserId = DataObject.NEW_ID;
               }
               
               Role testdata = new Role(
                                 results.getInt(m_roleSchema.getColumns(
                                          false, 
                                          new int[] {RoleDataDescriptor.COL_ROLE_ID}, null, null, 
                                          null
                                       ).toString()),
                                 results.getInt(m_roleSchema.getColumns(
                                          false, 
                                          new int[] {RoleDataDescriptor.COL_ROLE_DOMAIN_ID}, null, null, 
                                          null
                                       ).toString()),
                                 results.getString(m_roleSchema.getColumns(
                                          false, new int[] {RoleDataDescriptor.COL_ROLE_NAME}, null, null,
                                          null
                                       ).toString()),
                                 results.getString(m_roleSchema.getColumns(
                                          false, new int[] {RoleDataDescriptor.COL_ROLE_DESCRIPTION}, null, null,
                                          null
                                       ).toString()),
                                 results.getInt(m_roleSchema.getColumns(
                                          false, 
                                          new int[] {RoleDataDescriptor.COL_ROLE_ENABLED}, null, null,
                                          null
                                       ).toString()) == 1,
                                 // has to be read first because of null value
                                 iUserId,
                                 results.getInt(m_roleSchema.getColumns(
                                          false, 
                                          new int[] {RoleDataDescriptor.COL_ROLE_UNMODIFIABLE}, null, null,
                                          null
                                       ).toString()) == 1,
                                 null,                  
                                 results.getTimestamp(m_roleSchema.getColumns(
                                          false, 
                                          new int[] {RoleDataDescriptor.COL_ROLE_CREATION_DATE}, null, null,
                                          null
                                       ).toString()),
                                 results.getTimestamp(m_roleSchema.getColumns(
                                          false, 
                                          new int[] {RoleDataDescriptor.COL_ROLE_MODIFICATION_DATE}, null, null,
                                          null
                                       ).toString())      
                                 );
            
               assertEquals("Role is not the same", testdata, loadData); 
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
               if ((data != null) && (data.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(data.getId());
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
       * Test of get methods in factory
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
                                "testname1", "testdescription1", true, DataObject.NEW_ID, false, 
                                null, null, null);
               role1 = (Role)m_roleFactory.create(role1);
         
               role2 = new Role(DataObject.NEW_ID, 
                                CallContext.getInstance().getCurrentDomainId(), 
                                "testname2", "testdescription2", false, DataObject.NEW_ID, false, 
                                null, null, null);
               role2 = (Role)m_roleFactory.create(role2);
         
               role3 = new Role(DataObject.NEW_ID, 
                                CallContext.getInstance().getCurrentDomainId(), 
                                "testname3", "testdescription3", false, DataObject.NEW_ID, false, 
                                null, null, null);
               role3 = (Role)m_roleFactory.create(role3);
               
               role4 = new Role(DataObject.NEW_ID, 
                                CallContext.getInstance().getCurrentDomainId(), 
                                "testname4", "testdescription4", true, DataObject.NEW_ID, false, 
                                null, null, null);
               role4 = (Role)m_roleFactory.create(role4);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            
            List lstRoles = m_roleFactory.get(
               role1.getId() + ","
               + role3.getId() + ","
               // not existing ID
               + (role1.getId() + role2.getId() + role3.getId() + role4.getId()),
               SimpleRule.ALL_DATA
            );
            
            assertNotNull("List of returned Roles should not be null", lstRoles);
            
            assertEquals("Size of returned list of Roles is not correct", 2, lstRoles.size());
            
            Role testRole = (Role) lstRoles.get(0);
            
            assertNotNull("Returned Role is null", testRole);
            assertEquals("Role is not the same", role1,  testRole);
      
            testRole = (Role) lstRoles.get(1);
            assertNotNull("Returned Role is null", testRole);
            assertEquals("Role is not the same", role3,  testRole);
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
       * test of getActualCount method
       * 
       * @throws Exception - error during test
       */
      public void testGetActualCount(
      ) throws Exception
      {
         Role role1 = null;
         Role role2 = null;
         Role role3 = null;
         Role role4 = null;
         Role role5 = null;
   
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
      
               role4 = new Role(DataObject.NEW_ID, 
                                CallContext.getInstance().getCurrentDomainId(), 
                                "testname4", "testdescription4", true, DataObject.NEW_ID, false, 
                                null, null, null);
               role4 = (Role)m_roleFactory.create(role4);
               m_transaction.commit();         
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
      
            int iActual = m_roleFactory.getActualCount(
                     role1.getId() + "," +  role3.getId() + "," + 
                     (role1.getId() + role2.getId() + role3.getId() + role4.getId())
            );
            
            assertEquals("Number of actual Roles is incorect", 2, iActual);
   
            // Now we will delete one role and add additional one
            m_transaction.begin();      
            try
            {
               role5 = new Role(DataObject.NEW_ID, 
                                CallContext.getInstance().getCurrentDomainId(), 
                                "testname5", "testdescription5", true, DataObject.NEW_ID, false, 
                                null, null, null);
               role5 = (Role)m_roleFactory.create(role5);
   
               if ((role1 != null) && (role1.getId() != DataObject.NEW_ID))
               {
                  m_roleFactory.delete(
                                   role1.getId(),
                                   CallContext.getInstance().getCurrentDomainId());
               }
   
               m_transaction.commit();         
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
      
            iActual = m_roleFactory.getActualCount(
                     role1.getId() + "," +  role3.getId() + "," + 
                     (role1.getId() + role2.getId() + role3.getId() + role4.getId())
            );
            
            assertEquals("Number of actual Roles is incorect", 1, iActual);
         }
         finally
         {
            m_transaction.begin();
            try
            {
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
               if ((role5 != null) && (role5.getId() != DataObject.NEW_ID))
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(role5.getId());
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
       * test of getAllForUser method
       * 
       * @throws Exception - an error has occurred
       */
      public void testGetAllForUser(
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
            m_roleFactory.assignToUser(
               CallContext.getInstance().getCurrentUserId(),
               role1.getId() + "," + role2.getId()
               );
            
            List lstRoles = m_roleFactory.getAllForUser(
               CallContext.getInstance().getCurrentUserId(),
               SimpleRule.ALL_DATA);
            
            assertNotNull("Returned Role list is null", lstRoles);
            
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
       * test of getAllForDomain method
       * 
       * @throws Exception - an error has occurred
       */
      public void testGetAllForDomain(
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
            
            List lstRoles = m_roleFactory.getAllForDomain(
                                domain1.getId(), SimpleRule.ALL_DATA);
            
            assertNotNull("Returned Role list is null", lstRoles);
            
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
                  strQuery = "delete from BF_DOMAIN_ROLE_MAP where DOMAIN_ID in (?, ?)" +
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
                  // delete access rights
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
            List lstRoles = m_roleFactory.getAllRolesInDomain(
                                domain1.getId(), SimpleRule.ALL_DATA);
            
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
            lstRoles = m_roleFactory.getAllRolesInDomain(
                                domain2.getId(), SimpleRule.ALL_DATA);
            
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
                  // delete access rights
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
