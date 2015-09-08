/*
 * Copyright (c) 2003 - 2008 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: AccessRightDatabaseFactoryTest.java,v 1.31 2009/09/20 05:32:57 bastafidli Exp $
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.opensubsystems.core.application.Application;
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
import org.opensubsystems.patterns.listdata.data.DataCondition;
import org.opensubsystems.security.application.SecureApplicationTestSetup;
import org.opensubsystems.security.application.SecurityBackendModule;
import org.opensubsystems.security.data.AccessRight;
import org.opensubsystems.security.data.AccessRightDataDescriptor;
import org.opensubsystems.security.data.Role;
import org.opensubsystems.security.data.RoleDataDescriptor;
import org.opensubsystems.security.data.User;
import org.opensubsystems.security.data.UserDataDescriptor;
import org.opensubsystems.security.persist.AccessRightFactory;
import org.opensubsystems.security.persist.RoleFactory;
import org.opensubsystems.security.util.ActionConstants;
import org.opensubsystems.security.utils.TestRoleDatabaseFactoryUtils;

/**
 * Test of AccessRightDatabaseFactory
 * 
 * @version $Id: AccessRightDatabaseFactoryTest.java,v 1.31 2009/09/20 05:32:57 bastafidli Exp $
 * @author Peter Satury
 * @code.reviewer Miro Halas
 * @code.reviewed Initial revision
 */
public final class AccessRightDatabaseFactoryTest extends Tests
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private AccessRightDatabaseFactoryTest(
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
      TestSuite suite = new DatabaseTestSuite("AccessRightDatabaseFactoryTest");
      suite.addTestSuite(AccessRightDatabaseFactoryTestInternal.class);
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
   public static class AccessRightDatabaseFactoryTestInternal extends SecureDatabaseTest
   {
      // Cached values ////////////////////////////////////////////////////////////
   
      /**
       * Schema for database dependent operations.
       */
      protected RoleDatabaseSchema m_roleSchema;
   
      /**
       * Factory to manage roles.
       */
      protected RoleDatabaseFactory m_roleFactory;
   
      /**
       * Factory to manage access rights.
       */
      protected AccessRightDatabaseFactory m_accessRightFactory;
   
      /**
       * Factory utilities to manage roles.
       */
      protected TestRoleDatabaseFactoryUtils m_roleFactoryUtils;
   
      /**
       * Data descriptor for the access right data types.
       */
      protected AccessRightDataDescriptor m_accessRightDescriptor;

      /**
       * Data type assigned to user data types.
       */
      protected int m_iUserDataType;
   
      /**
       * Data type assigned to user data types.
       */
      protected int m_iRoleDataType;
   
      // Constructors //////////////////////////////////////////////////////////
      
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
       * Constructor.
       * 
       * @param strTestName - name of the test
       * @throws Exception - an error has occurred
       */
      public AccessRightDatabaseFactoryTestInternal(
         String strTestName
      ) throws Exception
      {
         super(strTestName);
   
         m_roleSchema = (RoleDatabaseSchema)DatabaseSchemaManager.getInstance(
                                                   RoleDatabaseSchema.class);
         m_roleFactory = (RoleDatabaseFactory)DataFactoryManager.getInstance(
                                                     RoleFactory.class);
         m_accessRightFactory = (AccessRightDatabaseFactory)DataFactoryManager.getInstance(
                                                     AccessRightFactory.class);
         m_roleFactoryUtils = new TestRoleDatabaseFactoryUtils();
         m_accessRightDescriptor = (AccessRightDataDescriptor)DataDescriptorManager
                                      .getInstance(AccessRightDataDescriptor.class);
         
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
       * Test creation of AccessRight.
       * 
       * @throws Exception - an error has occurred during test
       */
      public void testCreate(
      ) throws Exception
      {
         Role        role    = null;
         AccessRight data = null;
         AccessRight testData = null;
         AccessRight selectedData = null; 
   
         PreparedStatement statement = null;
         ResultSet         results = null; 
         
         try
         {            
            role = new Role(DataObject.NEW_ID, 
                        CallContext.getInstance().getCurrentDomainId(), 
                        "test role name", "test role description", true, DataObject.NEW_ID, false, 
                        null, null, null);
            m_transaction.begin();
            try
            {                                            
               role = (Role)m_roleFactory.create(role);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            
            data = new AccessRight(DataObject.NEW_ID, role.getId(),
                        CallContext.getInstance().getCurrentDomainId(), 
                        3, 5, 1, 10, 4, null, null);                  
                                                  
            m_transaction.begin();
            try
            {                                            
               testData = m_accessRightFactory.create(data);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
                  
            // Here the create is completed
            // We need to test
            assertNotNull("Access right should not be null", testData);
            assertTrue("New id wasn't generated.", testData.getId() != DataObject.NEW_ID); 
            assertNotNull("Creation timestamp was not generated", 
                     testData.getCreationTimestamp());
            assertNotNull("Modification timestamp was not generated", 
                     testData.getModificationTimestamp());
            assertTrue("Access right is not the same", data.isSame(testData));
            
            StringBuffer buffer = new StringBuffer();
            buffer.append("select ");
            m_roleSchema.getAccessRightColumns(false, 
                                               m_accessRightDescriptor.getAllColumnCodes(),
                                               null, null, buffer);
            buffer.append(" from " + RoleDatabaseSchema.ACCESSRIGHT_TABLE_NAME + " where ID=?");
   
            try
            {                    
               statement = m_connection.prepareStatement(buffer.toString());
               statement.setInt(1, testData.getId());
               results = statement.executeQuery();
               
               assertTrue("Inserted data were not found.", results.next());
               
               selectedData = (AccessRight)m_accessRightFactory.load(results, 1);
               assertNotNull("Access right should not be null", selectedData);
               assertEquals("Access right is not the same", testData, selectedData);
               assertFalse("Only one data should have been created.", results.next());
            }
            finally
            {
               DatabaseUtils.closeResultSetAndStatement(results, statement);
            }
         }
         finally
         {
            // Use sql to delete since in case there was a failure and data were
            // not created the sql will just ignore them unlike the delete() method
            m_transaction.begin();
            try
            {
               if ((role != null) && (role.getId() != DataObject.NEW_ID))
               {
                  // delete access right assigned to the particular role
                  m_accessRightFactory.deleteAllForRole(role.getId());
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
       * Test creation of AccessRights list.
       * 
       * @throws Exception - an error has occurred during test
       */
      public void testCreateList(
      ) throws Exception
      {
         PreparedStatement statement = null;
         ResultSet         results = null;      
         String            strQuery;
         
         Role        role    = null;
         AccessRight right1 = null;
         AccessRight right2 = null;
         AccessRight rightTest;
         List lstRights = null;
         List lstRightsReturned = null; 
   
         try
         {            
            role = new Role(DataObject.NEW_ID, 
                        CallContext.getInstance().getCurrentDomainId(), 
                        "test role name", "test role description", true, DataObject.NEW_ID, false, 
                        null, null, null);
            m_transaction.begin();
            try
            {                                            
               role = (Role)m_roleFactory.create(role);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            
            right1 = new AccessRight(DataObject.NEW_ID, DataObject.NEW_ID,
                        CallContext.getInstance().getCurrentDomainId(), 
                        3, 5, 1, 10, 4, null, null);                  
   
            right2 = new AccessRight(DataObject.NEW_ID, DataObject.NEW_ID,
                        CallContext.getInstance().getCurrentDomainId(), 
                        2, 1, 1, 9, 5, null, null);                  
   
            lstRights = new ArrayList();
            lstRights.add(0, right1);
            lstRights.add(1, right2);
   
            m_transaction.begin();
            try
            {                                            
               lstRightsReturned = m_accessRightFactory.create(lstRights, role.getId());
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            // Here the create is completed
            // We need to test
            assertNotNull("Returned list of rights is null", lstRightsReturned);
            assertEquals("Returned list of rights size is not correct", 
                         2, lstRightsReturned.size());
            
            rightTest = (AccessRight) lstRightsReturned.get(0); 
            assertNotNull("Access right should not be null", rightTest);
            assertTrue("New id wasn't generated.", rightTest.getId() != DataObject.NEW_ID); 
            assertNotNull("Creation timestamp was not generated", 
                     rightTest.getCreationTimestamp());
            assertNotNull("Modification timestamp was not generated", 
                     rightTest.getModificationTimestamp());
            assertTrue("Access right is not the same", right1.isSame(rightTest));
            
            right1 = rightTest;
            
            rightTest = (AccessRight) lstRightsReturned.get(1); 
            
            assertNotNull("Access right should not be null", rightTest);
            assertTrue("New id wasn't generated.", rightTest.getId() != DataObject.NEW_ID); 
            assertNotNull("Creation timestamp was not generated", 
                     rightTest.getCreationTimestamp());
            assertNotNull("Modification timestamp was not generated", 
                     rightTest.getModificationTimestamp());
            assertTrue("Access right is not the same", right2.isSame(rightTest));
            
            right2 = rightTest;
            
            try
            {                    
               StringBuffer buffer = new StringBuffer();
               buffer.append("select ");
               m_roleSchema.getAccessRightColumns(
                              false, 
                              m_accessRightDescriptor.getAllColumnCodes(), 
                              null, null, buffer);
               buffer.append(" from " + RoleDatabaseSchema.ACCESSRIGHT_TABLE_NAME + " where ROLE_ID=? order by ID");
   
               strQuery = buffer.toString();
   
               statement = m_connection.prepareStatement(strQuery);
               statement.setInt(1, role.getId());
               results = statement.executeQuery();
               
               assertTrue("Inserted data were not found.", results.next());
               
               rightTest = (AccessRight)m_accessRightFactory.load(results, 1);
               assertNotNull("Access right should not be null", rightTest);
               assertTrue("New id wasn't generated.", rightTest.getId() != DataObject.NEW_ID); 
               assertNotNull("Creation timestamp was not generated", 
                        rightTest.getCreationTimestamp());
               assertNotNull("Modification timestamp was not generated", 
                        rightTest.getModificationTimestamp());
               assertTrue("Access right is not the same", right1.isSame(rightTest));
                                            
               assertTrue("Inserted data were not found.", results.next());
               
               rightTest = (AccessRight)m_accessRightFactory.load(results, 1);
               assertNotNull("Access right should not be null", rightTest);
               assertTrue("New id wasn't generated.", rightTest.getId() != DataObject.NEW_ID); 
               assertNotNull("Creation timestamp was not generated", 
                        rightTest.getCreationTimestamp());
               assertNotNull("Modification timestamp was not generated", 
                        rightTest.getModificationTimestamp());
               assertTrue("Access right is not the same", right2.isSame(rightTest));
   
               assertFalse("Only two data should have been created.", results.next());
            }
            finally
            {
               DatabaseUtils.closeResultSetAndStatement(results, statement);
            }
         }
         finally
         {
            // Use sql to delete since in case there was a failure and data were
            // not created the sql will just ignore them unlike the delete() method
            m_transaction.begin();
            try
            {
               if ((role != null) && (role.getId() != DataObject.NEW_ID))
               {
                  // delete all access rights assigned to particular role 
                  m_accessRightFactory.deleteAllForRole(role.getId());
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
       * Test for creating collection of access rights
       * 
       * @throws Exception - an error has occurred
       */
      public void testCreateCollection(
      ) throws Exception
      {
         PreparedStatement statement = null;
         ResultSet         results = null;
   
         Role        role = null;
         List        lstData = new ArrayList();
         AccessRight testData = null;
         AccessRight selectedData = null;
         
         int      iIndex;
         int      iInsertedCount = 0;
         Iterator items;
         
         role = new Role(DataObject.NEW_ID, 
                     CallContext.getInstance().getCurrentDomainId(), 
                     "test role name", "test role description", true, DataObject.NEW_ID, false, 
                     null, null, null);
         m_transaction.begin();
         try
         {                                            
            role = (Role)m_roleFactory.create(role);
            m_transaction.commit();
         }
         catch (Throwable thr)
         {
            m_transaction.rollback();
            throw new Exception(thr);
         }
         
         for (iIndex = 1; iIndex < 234; iIndex++)
         {
            lstData.add(new AccessRight(DataObject.NEW_ID, role.getId(),
                        CallContext.getInstance().getCurrentDomainId(), 
                        iIndex, iIndex, iIndex, iIndex, iIndex, null, null));
         }
   
         try
         {
            m_transaction.begin();
            try
            {
               // try to create list of access rights
               iInsertedCount = m_accessRightFactory.create(lstData);
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            
            assertEquals("Number of inserted items is incorrect", lstData.size(), iInsertedCount);
   
            // try to test inserted data from the DB 
            StringBuffer buffer = new StringBuffer();
   
            buffer.append("select ");
            m_roleSchema.getAccessRightColumns(false, 
                                               m_accessRightDescriptor.getAllColumnCodes(), 
                                               null, null, buffer);
            buffer.append(" from " + RoleDatabaseSchema.ACCESSRIGHT_TABLE_NAME + " where DOMAIN_ID = ?" +
                          " and ROLE_ID = ? order by ID");
                       
            try
            {                    
               statement = m_connection.prepareStatement(buffer.toString());
               statement.setInt(1, CallContext.getInstance().getCurrentDomainId());
               statement.setInt(2, role.getId());
               results = statement.executeQuery();
               
               for (items = lstData.iterator(), iIndex = 1; items.hasNext(); iIndex++)
               {
                  testData = (AccessRight)items.next();
                  assertTrue("Inserted data were not found.", results.next());
                  selectedData = (AccessRight)m_accessRightFactory.load(results, 1);
                  assertNotNull("AccessRight should not be null", selectedData);
                  // Cannot use equals here since batch create doesn't fetch id and dates
                  assertTrue("AccessRight " + iIndex + " is not the same", 
                             testData.isSame(selectedData));
               }
               assertFalse("Only one data should have been created.", results.next());
            }
            finally
            {
               DatabaseUtils.closeResultSetAndStatement(results, statement);
            }
         }
         finally
         {
            if (iInsertedCount > 1)
            {
               m_transaction.begin();
               // delete inserted records
               try
               {
                  m_roleFactoryUtils.deleteRoleCascadeManual(role.getId());
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
       * Test of get method by ID
       * 
       * @throws Exception - an error has occurred during test
       */
      public void testGetById(
      ) throws Exception
      {
         Role role1 = null;
         Role role2 = null;
         AccessRight right1 = null;
         AccessRight right2 = null;
   
         AccessRight rightTest;
   
         try
         {            
            role1 = new Role(DataObject.NEW_ID, 
                        CallContext.getInstance().getCurrentDomainId(), 
                        "test role name 1", "test role description 1", true, 
                        DataObject.NEW_ID, false, null, null, null);
   
            role2 = new Role(DataObject.NEW_ID, 
                        CallContext.getInstance().getCurrentDomainId(), 
                        "test role name 2", "test role descripion 2", true, 
                        DataObject.NEW_ID, false, null, null, null);
   
            right1 = new AccessRight(DataObject.NEW_ID, DataObject.NEW_ID,
                        CallContext.getInstance().getCurrentDomainId(), 
                        3, 5, 1, 10, 4, null, null);                  
   
            right2 = new AccessRight(DataObject.NEW_ID, DataObject.NEW_ID,
                        CallContext.getInstance().getCurrentDomainId(), 
                        2, 1, 1, 9, 5, null, null);                  
                                                  
            m_transaction.begin();
            try
            {                                            
               role1 = (Role)m_roleFactory.create(role1);
               role2 = (Role)m_roleFactory.create(role2);
   
               // Create two access rights to make sure we select the correct one            
               right1.setRoleId(role1.getId());
               right1 = m_accessRightFactory.create(right1);
               
               right2.setRoleId(role2.getId());
               right2 = m_accessRightFactory.create(right2);
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
         
            rightTest = (AccessRight)m_accessRightFactory.get(
                           right2.getId(),
                           CallContext.getInstance().getCurrentDomainId());
                  
            assertNotNull("Access right should not be null", rightTest);
            assertEquals("Access Right is not the same", right2, rightTest);
         }
         finally
         {
            m_transaction.begin();
            // delete inserted records
            try
            {
               if ((right1 != null) && (right1.getId() != DataObject.NEW_ID))
               {
                  // delete access right for role 1
                  m_accessRightFactory.deleteAllForRole(role1.getId());
               }
               if ((right2 != null) && (right2.getId() != DataObject.NEW_ID))
               {
                  // delete access right for role 2
                  m_accessRightFactory.deleteAllForRole(role2.getId());
               }
               if ((role1 != null) && (role1.getId() != DataObject.NEW_ID))
               {
                  // delete role 1
                  m_roleFactory.delete(
                     role1.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((role2 != null) && (role2.getId() != DataObject.NEW_ID))
               {
                  // delete role 2
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
       * Test of getAllForRole method.
       * 
       * @throws Exception - an error has occurred 
       */
      public void testGetAllForRole(
      ) throws Exception
      {
         Role role1 = null;
         Role role2 = null;
         AccessRight right1 = null;
         AccessRight right2 = null;
         AccessRight right3 = null;
         AccessRight rightTest;
         List lstRights = null; 
   
         try
         {            
            role1 = new Role(DataObject.NEW_ID, 
                        CallContext.getInstance().getCurrentDomainId(), 
                        "test role name 1", "test role description 1", true, 
                        DataObject.NEW_ID, false, null, null, null);
   
            role2 = new Role(DataObject.NEW_ID, 
                        CallContext.getInstance().getCurrentDomainId(), 
                        "test role name 2", "test role descripion 2", true, 
                        DataObject.NEW_ID, false, null, null, null);
   
            right1 = new AccessRight(DataObject.NEW_ID, DataObject.NEW_ID,
                        CallContext.getInstance().getCurrentDomainId(), 
                        3, 5, 1, 10, 4, null, null);                  
   
            right2 = new AccessRight(DataObject.NEW_ID, DataObject.NEW_ID,
                        CallContext.getInstance().getCurrentDomainId(), 
                        2, 1, 1, 9, 5, null, null);                  
                                                  
            right3 = new AccessRight(DataObject.NEW_ID, DataObject.NEW_ID,
                        CallContext.getInstance().getCurrentDomainId(), 
                        3, 5, 1, 10, 4, null, null);                  
   
            m_transaction.begin();
            try
            {                                            
               role1 = (Role)m_roleFactory.create(role1);
               role2 = (Role)m_roleFactory.create(role2);
               
               right1.setRoleId(role1.getId());
               right1 = m_accessRightFactory.create(right1);
               
               right2.setRoleId(role1.getId());
               right2 = m_accessRightFactory.create(right2);
   
               right3.setRoleId(role2.getId());
               right3 = m_accessRightFactory.create(right3);
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            
            lstRights = m_accessRightFactory.getAllForRole(role1.getId());
                  
            assertNotNull("Returned list of rights is null", lstRights);
            assertEquals("Returned list of rights size is not correct", 2, lstRights.size());
            
            rightTest = (AccessRight) lstRights.get(0); 
            
            if (rightTest.getId() == right1.getId())
            {
               assertEquals("AccessRight is not the same", right1, rightTest);
               rightTest = (AccessRight) lstRights.get(1);
               assertEquals("AccessRight is not the same", right2, rightTest);
            }
            else if (rightTest.getId() == right2.getId())
            {
               assertEquals("AccessRight is not the same", right2, rightTest);
               rightTest = (AccessRight) lstRights.get(1);
               assertEquals("AccessRight is not the same", right1, rightTest);
            }
            else
            {
               assertTrue("Returned AccessRight in the list is not correct", false);
            }
            
            lstRights = m_accessRightFactory.getAllForRole(role2.getId());
                  
            assertNotNull("Returned list of rights is null", lstRights);
            assertEquals("Returned list of rights size is not correct", 1, lstRights.size());
            
            rightTest = (AccessRight) lstRights.get(0); 
            
            assertEquals("AccessRight is not the same", right3, rightTest);
         }
         finally
         {
            // Use sql to delete since in case there was a failure and data were
            // not created the sql will just ignore them unlike the delete() method
            m_transaction.begin();
            try
            {
               if ((role1 != null) && (role1.getId() != DataObject.NEW_ID))
               {
                  m_accessRightFactory.deleteAllForRole(role1.getId());
                  m_roleFactory.delete(
                     role1.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((role2 != null) && (role2.getId() != DataObject.NEW_ID))
               {
                  m_accessRightFactory.deleteAllForRole(role2.getId());
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
       * Test of getAllForRoles method.
       * 
       * @throws Exception - an error has occurred 
       */
      public void testGetAllForRoles(
      ) throws Exception
      {
         Role role1 = null;
         Role role2 = null;
         Role role3 = null;
         AccessRight right1 = null;
         AccessRight right2 = null;
         AccessRight right3 = null;
         AccessRight right4 = null;
         AccessRight rightTest;
         List lstRights = null; 
   
         try
         {            
            role1 = new Role(DataObject.NEW_ID, 
                        CallContext.getInstance().getCurrentDomainId(), 
                        "test role name 1", "test role description 1", true, 
                        DataObject.NEW_ID, false, null, null, null);
   
            role2 = new Role(DataObject.NEW_ID, 
                        CallContext.getInstance().getCurrentDomainId(), 
                        "test role name 2", "test role descripion 2", true, 
                        DataObject.NEW_ID, false, null, null, null);

            role3 = new Role(DataObject.NEW_ID, 
                        CallContext.getInstance().getCurrentDomainId(), 
                        "test role name 3", "test role descripion 3", true, 
                        DataObject.NEW_ID, false, null, null, null);
   
            right1 = new AccessRight(DataObject.NEW_ID, DataObject.NEW_ID,
                        CallContext.getInstance().getCurrentDomainId(), 
                        3, 5, 1, 10, 4, null, null);                  
   
            right2 = new AccessRight(DataObject.NEW_ID, DataObject.NEW_ID,
                        CallContext.getInstance().getCurrentDomainId(), 
                        2, 1, 1, 9, 5, null, null);                  
                                                  
            right3 = new AccessRight(DataObject.NEW_ID, DataObject.NEW_ID,
                        CallContext.getInstance().getCurrentDomainId(), 
                        3, 5, 1, 10, 4, null, null);                  

            right4 = new AccessRight(DataObject.NEW_ID, DataObject.NEW_ID,
                        CallContext.getInstance().getCurrentDomainId(), 
                        3, 5, 1, 9, 4, null, null);                  

            m_transaction.begin();
            try
            {                                            
               role1 = (Role)m_roleFactory.create(role1);
               role2 = (Role)m_roleFactory.create(role2);
               role3 = (Role)m_roleFactory.create(role3);
               
               right1.setRoleId(role1.getId());
               right1 = m_accessRightFactory.create(right1);
               
               right2.setRoleId(role1.getId());
               right2 = m_accessRightFactory.create(right2);
   
               right3.setRoleId(role2.getId());
               right3 = m_accessRightFactory.create(right3);
   
               right4.setRoleId(role3.getId());
               right4 = m_accessRightFactory.create(right4);

               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            
            String strRoleIDs = role1.getId() + "," + role2.getId() + ",999999999"; 
            
            lstRights = m_accessRightFactory.getAllForRoles(strRoleIDs);
                  
            assertNotNull("Returned list of rights is null", lstRights);
            assertEquals("Returned list of rights size is not correct", 3, lstRights.size());
            
            rightTest = (AccessRight) lstRights.get(0);
            
            Iterator itRight;
            
            for (itRight = lstRights.iterator(); itRight.hasNext();)
            {
               rightTest = (AccessRight)itRight.next();
               if (rightTest.getId() == right1.getId())
               {
                  assertEquals("AccessRight 1 is not the same", right1, rightTest);
               }
               else if (rightTest.getId() == right2.getId())
               {
                  assertEquals("AccessRight 2 is not the same", right2, rightTest);
               }
               else if (rightTest.getId() == right3.getId())
               {
                  assertEquals("AccessRight 3 is not the same", right3, rightTest);
               }
               else
               {
                  assertTrue("Returned AccessRight in the list is not correct", false);
               }
            }
         }
         finally
         {
            // Use sql to delete since in case there was a failure and data were
            // not created the sql will just ignore them unlike the delete() method
            m_transaction.begin();
            try
            {
               if ((role1 != null) && (role1.getId() != DataObject.NEW_ID))
               {
                  m_accessRightFactory.deleteAllForRole(role1.getId());
                  m_roleFactory.delete(
                     role1.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((role2 != null) && (role2.getId() != DataObject.NEW_ID))
               {
                  m_accessRightFactory.deleteAllForRole(role2.getId());
                  m_roleFactory.delete(
                     role2.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((role3 != null) && (role3.getId() != DataObject.NEW_ID))
               {
                  m_accessRightFactory.deleteAllForRole(role3.getId());
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
       * Test of deleteAllForRole method.
       * 
       * @throws Exception - an error has occurred 
       */
      public void testDeleteAllForRole(
      ) throws Exception
      {
         Role role1 = null;
         Role role2 = null;
         AccessRight right1 = null;
         AccessRight right2 = null;
         AccessRight right3 = null;
         AccessRight rightTest;
         List lstRights = null;
         
         int iDeletedAccessRights = 0;
   
         try
         {            
            role1 = new Role(DataObject.NEW_ID, 
                        CallContext.getInstance().getCurrentDomainId(), 
                        "test role name 1", "test role description 1", true, 
                        DataObject.NEW_ID, false, null, null, null);
   
            role2 = new Role(DataObject.NEW_ID, 
                        CallContext.getInstance().getCurrentDomainId(), 
                        "test role name 2", "test role descripion 2", true, 
                        DataObject.NEW_ID, false, null, null, null);
   
            right1 = new AccessRight(DataObject.NEW_ID, DataObject.NEW_ID,
                        CallContext.getInstance().getCurrentDomainId(), 
                        3, 5, 1, 10, 4, null, null);                  
   
            right2 = new AccessRight(DataObject.NEW_ID, DataObject.NEW_ID,
                        CallContext.getInstance().getCurrentDomainId(), 
                        2, 1, 1, 9, 5, null, null);                  
                                                  
            right3 = new AccessRight(DataObject.NEW_ID, DataObject.NEW_ID,
                        CallContext.getInstance().getCurrentDomainId(), 
                        3, 5, 1, 10, 4, null, null);                  
   
            m_transaction.begin();
            try
            {
               // create 2 roles
               role1 = (Role)m_roleFactory.create(role1);
               role2 = (Role)m_roleFactory.create(role2);
   
               // assign 2 access rights to role 1
               right1.setRoleId(role1.getId());
               right1 = m_accessRightFactory.create(right1);
   
               right2.setRoleId(role1.getId());
               right2 = m_accessRightFactory.create(right2);
   
               // assign access right to role 2
               right3.setRoleId(role2.getId());
               right3 = m_accessRightFactory.create(right3);
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            
            // check if there are assigned specified access rights for specified roles
            lstRights = m_accessRightFactory.getAllForRole(role1.getId());
                  
            assertNotNull("Returned list of rights is null", lstRights);
            assertEquals("Returned list of rights size is not correct", 2, lstRights.size());
            
            rightTest = (AccessRight) lstRights.get(0); 
            
            if (rightTest.getId() == right1.getId())
            {
               assertEquals("AccessRight is not the same", right1, rightTest);
               rightTest = (AccessRight) lstRights.get(1);
               assertEquals("AccessRight is not the same", right2, rightTest);
            }
            else if (rightTest.getId() == right2.getId())
            {
               assertEquals("AccessRight is not the same", right2, rightTest);
               rightTest = (AccessRight) lstRights.get(1);
               assertEquals("AccessRight is not the same", right1, rightTest);
            }
            else
            {
               assertTrue("Returned AccessRight in the list is not correct", false);
            }
            
            lstRights = m_accessRightFactory.getAllForRole(role2.getId());
                  
            assertNotNull("Returned list of rights is null", lstRights);
            assertEquals("Returned list of rights size is not correct", 1, lstRights.size());
            
            rightTest = (AccessRight) lstRights.get(0); 
            
            assertEquals("AccessRight is not the same", right3, rightTest);
            
            // now delete all access rights for role 1 and check if everything within the DB is ok
            m_transaction.begin();
            try
            {
               iDeletedAccessRights = m_accessRightFactory.deleteAllForRole(role1.getId());
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            assertEquals("Number of deleted access rights for role 1 is incorrect", 
                         2, iDeletedAccessRights);
   
            // check if there are assigned specified access rights for specified roles after delete
            
            // for role 1 there should be not assigned access rights
            lstRights = m_accessRightFactory.getAllForRole(role1.getId());
   
            assertNull("There should be no access rights assigned to the role", lstRights);
            
            // for role 2 there should be still assigned 1 access right
            lstRights = m_accessRightFactory.getAllForRole(role2.getId());
                  
            assertNotNull("Returned list of rights is null", lstRights);
            assertEquals("Returned list of rights size is not correct", 1, lstRights.size());
            
            rightTest = (AccessRight) lstRights.get(0); 
            
            assertEquals("AccessRight is not the same", right3, rightTest);
         }
         finally
         {
            // Use sql to delete since in case there was a failure and data were
            // not created the sql will just ignore them unlike the delete() method
            m_transaction.begin();
            try
            {
               if ((role1 != null) && (role1.getId() != DataObject.NEW_ID))
               {
                  m_accessRightFactory.deleteAllForRole(role1.getId());
                  m_roleFactory.delete(
                     role1.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((role2 != null) && (role2.getId() != DataObject.NEW_ID))
               {
                  m_accessRightFactory.deleteAllForRole(role2.getId());
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
       * Test of delete method.
       * 
       * @throws Exception - an error has occurred
       */
      public void testDeleteRights(
      ) throws Exception
      {
         PreparedStatement statement = null;
         ResultSet         results = null;      
         String            strQuery;
         
         Role role1 = null;
         Role role2 = null;
         AccessRight right1 = null;
         AccessRight right2 = null;
         AccessRight right3 = null;
         AccessRight rightTest;
         int iDeleted = 0;
   
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
   
            right1 = new AccessRight(DataObject.NEW_ID, DataObject.NEW_ID,
                        CallContext.getInstance().getCurrentDomainId(), 
                        3, 5, 1, 10, 4, null, null);                  
   
            right2 = new AccessRight(DataObject.NEW_ID, DataObject.NEW_ID,
                        CallContext.getInstance().getCurrentDomainId(), 
                        2, 1, 1, 9, 5, null, null);                  
                                                  
            right3 = new AccessRight(DataObject.NEW_ID, DataObject.NEW_ID,
                        CallContext.getInstance().getCurrentDomainId(), 
                        3, 5, 1, 10, 4, null, null);                  
   
            m_transaction.begin();
            try
            {                                            
               role1 = (Role)m_roleFactory.create(role1);
               role2 = (Role)m_roleFactory.create(role2);
               
               right1.setRoleId(role1.getId());
               right1 = m_accessRightFactory.create(right1);
               
               right2.setRoleId(role1.getId());
               right2 = m_accessRightFactory.create(right2);
   
               right3.setRoleId(role2.getId());
               right3 = m_accessRightFactory.create(right3);
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            
            iDeleted = m_accessRightFactory.delete(
                              role1.getId(), 
                              Integer.toString(right1.getId()) 
                              + "," + Integer.toString(right3.getId()));
                  
            assertEquals("Number of deleted AccessRights is not correct", 1, iDeleted);
            
            StringBuffer buffer = new StringBuffer();
            buffer.append("select ");
            m_roleSchema.getAccessRightColumns(
                           false, 
                           m_accessRightDescriptor.getAllColumnCodes(),
                           null, null, buffer);
            buffer.append(" from " + RoleDatabaseSchema.ACCESSRIGHT_TABLE_NAME + " where ROLE_ID=? order by ID");
            strQuery = buffer.toString();
   
            statement = m_connection.prepareStatement(strQuery);
            statement.setInt(1, role1.getId());
            results = statement.executeQuery();
               
            assertTrue("All rights of Role are deleted.", results.next());
               
            rightTest = (AccessRight)m_accessRightFactory.load(results, 1);
            assertEquals("Access right are not the same", right2, rightTest);
                                            
            assertFalse("Deleted data still in database", results.next());
            
            statement = m_connection.prepareStatement(strQuery);
            statement.setInt(1, role2.getId());
            results = statement.executeQuery();
               
            assertTrue("Rights of Role are deleted.", results.next());
               
            rightTest = (AccessRight) m_accessRightFactory.load(results, 1);
            assertEquals("Access right are not the same", right3, rightTest);
                                            
            assertFalse("Other Rights in database", results.next());
         }
         finally
         {
            // Use sql to delete since in case there was a failure and data were
            // not created the sql will just ignore them unlike the delete() method
            m_transaction.begin();
            try
            {
               if ((role1 != null) && (role1.getId() != DataObject.NEW_ID))
               {
                  m_accessRightFactory.deleteAllForRole(role1.getId());
                  m_roleFactory.delete(
                     role1.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((role2 != null) && (role2.getId() != DataObject.NEW_ID))
               {
                  m_accessRightFactory.deleteAllForRole(role2.getId());
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
       * Test of getForCurrentUser method.
       * 
       * @throws Exception - an error has occurred
       */
      public void testGetForCurrentUser(
      ) throws Exception
      {
         Role role1 = null;
         Role role2 = null;
         AccessRight right1 = null;
         AccessRight right2 = null;
         AccessRight right3 = null;
         AccessRight right4 = null;
         AccessRight right5 = null;
         AccessRight rightTest;
         List lstRights = null; 
   
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
   
            right1 = new AccessRight(DataObject.NEW_ID, DataObject.NEW_ID,
                        CallContext.getInstance().getCurrentDomainId(), 
                        ActionConstants.RIGHT_ACTION_CREATE, 
                        m_iUserDataType, 
                        1, 1, 3,
                        null, null);                  
   
            right2 = new AccessRight(DataObject.NEW_ID, DataObject.NEW_ID,
                        CallContext.getInstance().getCurrentDomainId(), 
                        ActionConstants.RIGHT_ACTION_CREATE, 
                        m_iUserDataType, 
                        1, 2, 7,
                        null, null);                  
                                                  
            right3 = new AccessRight(DataObject.NEW_ID, DataObject.NEW_ID,
                        CallContext.getInstance().getCurrentDomainId(), 
                        ActionConstants.RIGHT_ACTION_VIEW, 
                        m_iUserDataType, 
                        1, 9, 10,
                        null, null);                  
   
            right4 = new AccessRight(DataObject.NEW_ID, DataObject.NEW_ID,
                        CallContext.getInstance().getCurrentDomainId(), 
                        ActionConstants.RIGHT_ACTION_CREATE, 
                        m_iRoleDataType, 
                        1, 12, 13,
                        null, null);                  
                                                  
            right5 = new AccessRight(DataObject.NEW_ID, DataObject.NEW_ID,
                        CallContext.getInstance().getCurrentDomainId(), 
                        ActionConstants.RIGHT_ACTION_CREATE, 
                        m_iUserDataType, 
                        1, 0, 0,
                        null, null);                  
   
            m_transaction.begin();
            try
            {                                            
               role1 = (Role)m_roleFactory.create(role1);
               role2 = (Role)m_roleFactory.create(role2);
               
               m_roleFactory.assignToUser(
                                 user.getId(),
                                 Integer.toString(role1.getId())                              
                              );
               
               right1.setRoleId(role1.getId());
               right1 = m_accessRightFactory.create(right1);
               
               right2.setRoleId(role1.getId());
               right2 = m_accessRightFactory.create(right2);
   
               right3.setRoleId(role1.getId());
               right3 = m_accessRightFactory.create(right3);
   
               right4.setRoleId(role1.getId());
               right4 = m_accessRightFactory.create(right4);
   
               right5.setRoleId(role2.getId());
               right5 = m_accessRightFactory.create(right5);
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            
            lstRights = m_accessRightFactory.getForCurrentUser(
                           m_iUserDataType,
                           ActionConstants.RIGHT_ACTION_CREATE
                        );
                  
            assertNotNull("Returned list of rights is null", lstRights);
            assertEquals("Returned list of rights size is not correct", 2, lstRights.size());
            rightTest = (AccessRight) lstRights.get(0); 
            assertEquals("Returned AccessRight in the list is not correct",
                              right1.getId(),
                              rightTest.getId() 
                           );
            rightTest = (AccessRight) lstRights.get(1);
            assertEquals("AccessRight is not the same", right2, rightTest);
   
            lstRights = m_accessRightFactory.getForCurrentUser(
                           m_iUserDataType,
                           ActionConstants.RIGHT_ACTION_VIEW
                        );
                  
            assertNotNull("Returned list of rights is null", lstRights);
            assertEquals("Returned list of rights size is not correct", 1, lstRights.size());
            rightTest = (AccessRight) lstRights.get(0); 
            assertEquals("AccessRight is not the same", right3, rightTest);
   
            lstRights = m_accessRightFactory.getForCurrentUser(
                           m_iRoleDataType,
                           ActionConstants.RIGHT_ACTION_CREATE
                        );
                  
            assertNotNull("Returned list of rights is null", lstRights);
            assertEquals("Returned list of rights size is not correct", 1, lstRights.size());
            rightTest = (AccessRight) lstRights.get(0); 
            assertEquals("AccessRight is not the same", right4, rightTest);
   
            lstRights = m_accessRightFactory.getForCurrentUser(
                           m_iUserDataType,
                           ActionConstants.RIGHT_ACTION_DELETE
                        );
   
            assertNull("There should be no access rights assigned to the role", lstRights);
   
            lstRights = m_accessRightFactory.getForCurrentUser(
                           m_iRoleDataType,
                           ActionConstants.RIGHT_ACTION_VIEW
                        );
                  
            assertNull("There should be no access rights assigned to the role", lstRights);
         }
         finally
         {
            // Use sql to delete since in case there was a failure and data were
            // not created the sql will just ignore them unlike the delete() method
            m_transaction.begin();
            try
            {
               if ((role1 != null) && (role1.getId() != DataObject.NEW_ID))
               {
                   m_accessRightFactory.deleteAllForRole(role1.getId());
                   m_roleFactoryUtils.deleteRoleCascadeManual(role1.getId());
               }
               if ((role2 != null) && (role2.getId() != DataObject.NEW_ID))
               {
                   m_accessRightFactory.deleteAllForRole(role2.getId());
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
       * Test of checkAccess method.
       * 
       * @throws Exception - an error has occurred
       */
      public void testCheckAccess(
      ) throws Exception
      {
         Role role1 = null;
         Role role2 = null;
         Role role3 = null;
         AccessRight right1 = null;
         AccessRight right2 = null;
         AccessRight right3 = null;
   
         int iRightType = -1;
   
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
                        "test role name 2", "test role descripion 2", true, 
                        DataObject.NEW_ID, false, null, null, null);
   
            role3 = new Role(DataObject.NEW_ID, 
                        CallContext.getInstance().getCurrentDomainId(), 
                        "test role name 3", "test role descripion 3", false, 
                        DataObject.NEW_ID, false, null, null, null);
   
            m_transaction.begin();
            try
            {                                            
               role1 = (Role)m_roleFactory.create(role1);
               role2 = (Role)m_roleFactory.create(role2);
   
               // set right to view only role1 object
               right1 = new AccessRight(DataObject.NEW_ID, 
                                        DataObject.NEW_ID,
                                        CallContext.getInstance().getCurrentDomainId(), 
                                        ActionConstants.RIGHT_ACTION_VIEW,
                                        m_iRoleDataType,
                                        AccessRight.ACCESS_GRANTED,
                                        AccessRight.NO_RIGHT_CATEGORY,
                                        role1.getId(),
                                        null, null);
      
               // set right to delete only role2 object
               right2 = new AccessRight(DataObject.NEW_ID, 
                                        DataObject.NEW_ID,
                                        CallContext.getInstance().getCurrentDomainId(), 
                                        ActionConstants.RIGHT_ACTION_DELETE,
                                        m_iRoleDataType,
                                        AccessRight.ACCESS_GRANTED,
                                        AccessRight.NO_RIGHT_CATEGORY,
                                        role2.getId(),
                                        null, null);
   
               // set right to create only roles objects that are not enabled
               right3 = new AccessRight(DataObject.NEW_ID, 
                                        DataObject.NEW_ID,
                                        CallContext.getInstance().getCurrentDomainId(), 
                                        ActionConstants.RIGHT_ACTION_CREATE,
                                        m_iRoleDataType,
                                        AccessRight.ACCESS_GRANTED,
                                        RoleDataDescriptor.COL_ROLE_ENABLED,
                                        DataCondition.DATA_CODE_FLAG_NO,
                                        null, null);
   
               // Create two access rights to make sure we select the correct one            
               right1.setRoleId(role1.getId());
               right1 = m_accessRightFactory.create(right1);
               
               right2.setRoleId(role1.getId());
               right2 = m_accessRightFactory.create(right2);
   
               right3.setRoleId(role1.getId());
               right3 = m_accessRightFactory.create(right3);
   
               // assign roles to the current user
                m_roleFactory.assignToUser(user.getId(), Integer.toString(role1.getId()));
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
   
            // check access to view role 1
            iRightType = m_accessRightFactory.checkAccess(
                             m_iRoleDataType,
                             ActionConstants.RIGHT_ACTION_VIEW,
                             role1.getId(),
                                 new int[][] {
                                    new int[] {RoleDataDescriptor.COL_ROLE_ENABLED,
                                               role1.isEnabled() ? 1 : 0,
                                              },
                                    new int[] {RoleDataDescriptor.COL_ROLE_USER_ID,
                                               // If there is user id then it is personal
                                               (role1.getUserId() != DataObject.NEW_ID) ? 1 : 0,
                                              },
                                             }
            );
            assertEquals("Access Right type is incorrect", AccessRight.ACCESS_GRANTED, iRightType);
   
            // check access to view role 2
            iRightType = m_accessRightFactory.checkAccess(
                             m_iRoleDataType,
                             ActionConstants.RIGHT_ACTION_VIEW,
                             role2.getId(),
                                 new int[][] {
                                    new int[] {RoleDataDescriptor.COL_ROLE_ENABLED,
                                               role2.isEnabled() ? 1 : 0,
                                              },
                                    new int[] {RoleDataDescriptor.COL_ROLE_USER_ID,
                                               // If there is user id then it is personal
                                               (role2.getUserId() != DataObject.NEW_ID) ? 1 : 0,
                                              },
                                             }
            );
            assertEquals("Access Right type is incorrect", AccessRight.ACCESS_DENIED, iRightType);
   
            // check access to delete role 1
            iRightType = m_accessRightFactory.checkAccess(
                             m_iRoleDataType,
                             ActionConstants.RIGHT_ACTION_DELETE,
                             role1.getId(),
                                 new int[][] {
                                    new int[] {RoleDataDescriptor.COL_ROLE_ENABLED,
                                               role1.isEnabled() ? 1 : 0,
                                              },
                                    new int[] {RoleDataDescriptor.COL_ROLE_USER_ID,
                                               // If there is user id then it is personal
                                               (role1.getUserId() != DataObject.NEW_ID) ? 1 : 0,
                                              },
                                             }
            );
            assertEquals("Access Right type is incorrect", AccessRight.ACCESS_DENIED, iRightType);
   
            // check access to delete role 2
            iRightType = m_accessRightFactory.checkAccess(
                             m_iRoleDataType,
                             ActionConstants.RIGHT_ACTION_DELETE,
                             role2.getId(),
                                 new int[][] {
                                    new int[] {RoleDataDescriptor.COL_ROLE_ENABLED,
                                               role2.isEnabled() ? 1 : 0,
                                              },
                                    new int[] {RoleDataDescriptor.COL_ROLE_USER_ID,
                                               // If there is user id then it is personal
                                               (role2.getUserId() != DataObject.NEW_ID) ? 1 : 0,
                                              },
                                             }
            );
            assertEquals("Access Right type is incorrect", AccessRight.ACCESS_GRANTED, iRightType);
   
            // check access to create role 2
            iRightType = m_accessRightFactory.checkAccess(
                             m_iRoleDataType,
                             ActionConstants.RIGHT_ACTION_CREATE,
                             role2.getId(),
                                 new int[][] {
                                    new int[] {RoleDataDescriptor.COL_ROLE_ENABLED,
                                               role2.isEnabled() ? 1 : 0,
                                              },
                                    new int[] {RoleDataDescriptor.COL_ROLE_USER_ID,
                                               // If there is user id then it is personal
                                               (role2.getUserId() != DataObject.NEW_ID) ? 1 : 0,
                                              },
                                             }
            );
            assertEquals("Access Right type is incorrect", AccessRight.ACCESS_DENIED, iRightType);
   
            // check access to create role 3
            iRightType = m_accessRightFactory.checkAccess(
                             m_iRoleDataType,
                             ActionConstants.RIGHT_ACTION_CREATE,
                             role3.getId(),
                                 new int[][] {
                                    new int[] {RoleDataDescriptor.COL_ROLE_ENABLED,
                                               role3.isEnabled() ? 1 : 0,
                                              },
                                    new int[] {RoleDataDescriptor.COL_ROLE_USER_ID,
                                               // If there is user id then it is personal
                                               (role3.getUserId() != DataObject.NEW_ID) ? 1 : 0,
                                              },
                                             }
            );
            assertEquals("Access Right type is incorrect", AccessRight.ACCESS_GRANTED, iRightType);
   
         }
         finally
         {
            m_transaction.begin();
            // delete inserted records
            try
            {
               if ((right1 != null) && (right1.getId() != DataObject.NEW_ID))
               {
                  // delete access right for role 1
                  m_accessRightFactory.deleteAllForRole(role1.getId());
               }
               if ((right2 != null) && (right2.getId() != DataObject.NEW_ID))
               {
                  // delete access right for role 2
                  m_accessRightFactory.deleteAllForRole(role2.getId());
               }
               // we need to call method that will delete also data from 
               // user-role map table (necessary for Sybase ASE database)
               // therefore first construct string of user IDs the belonging
               // roles we want to delete
               // remove roles with specified user ID 
               m_roleFactory.removeFromUsers(String.valueOf(user.getId()), false);
               
               if ((role1 != null) && (role1.getId() != DataObject.NEW_ID))
               {
                  // delete role 1
                  m_roleFactory.delete(
                     role1.getId(),
                     CallContext.getInstance().getCurrentDomainId());
               }
               if ((role2 != null) && (role2.getId() != DataObject.NEW_ID))
               {
                  // delete role 2
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
   }
}
