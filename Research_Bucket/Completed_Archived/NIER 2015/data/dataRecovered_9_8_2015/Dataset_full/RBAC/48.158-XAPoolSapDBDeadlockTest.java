/*
 * Copyright (c) 2003 - 2009 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: XAPoolSapDBDeadlockTest.java,v 1.15 2009/09/20 05:32:57 bastafidli Exp $
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
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.opensubsystems.core.application.Application;
import org.opensubsystems.security.application.SecureApplicationTestSetup;
import org.opensubsystems.core.application.ModuleManager;
import org.opensubsystems.core.application.impl.ApplicationImpl;
import org.opensubsystems.core.application.impl.InstanceInfoImpl;
import org.opensubsystems.core.data.DataObject;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.logic.ControllerManager;
import org.opensubsystems.core.persist.DataFactoryManager;
import org.opensubsystems.core.persist.db.DatabaseTest;
import org.opensubsystems.core.persist.db.DatabaseTestSuite;
import org.opensubsystems.core.util.CallContext;
import org.opensubsystems.core.util.Log;
import org.opensubsystems.core.util.Tests;
import org.opensubsystems.core.util.jdbc.DatabaseUtils;
import org.opensubsystems.security.application.SecurityBackendModule;
import org.opensubsystems.security.data.Domain;
import org.opensubsystems.security.data.ExternalSession;
import org.opensubsystems.security.data.InternalSession;
import org.opensubsystems.security.data.User;
import org.opensubsystems.security.persist.DomainFactory;
import org.opensubsystems.security.persist.ExternalSessionFactory;
import org.opensubsystems.security.persist.InternalSessionFactory;
import org.opensubsystems.security.persist.UserFactory;
import org.opensubsystems.security.persist.db.ExternalSessionDatabaseFactory;
import org.opensubsystems.security.persist.db.InternalSessionDatabaseFactory;
import org.opensubsystems.security.persist.db.SessionDatabaseSchema;
import org.opensubsystems.security.persist.db.UserDatabaseSchema;
import org.opensubsystems.security.util.SecureCallContext;
import org.opensubsystems.security.utils.TestUserDatabaseFactoryUtils;

/**
 * Test to cause dealock in Sapdb 7.4.3.27 with JOTM 1.4.3 and XAPool 1.3.1. 
 * This deadlock was actually caused by XAPool packaged with that version of 
 * JOTM.
 * 
 * @version $Id: XAPoolSapDBDeadlockTest.java,v 1.15 2009/09/20 05:32:57 bastafidli Exp $
 * @author MiroHalas
 * @code.reviewer
 * @code.reviewed TODO: Review this code
 */
public final class XAPoolSapDBDeadlockTest extends Tests
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private XAPoolSapDBDeadlockTest(
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
      TestSuite suite = new DatabaseTestSuite("XAPoolSapDBDeadlockTest");
      suite.addTestSuite(XAPoolSapDBDeadlockTestInternal.class);
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
   public static class XAPoolSapDBDeadlockTestInternal extends DatabaseTest 
   {
      // Cached values ////////////////////////////////////////////////////////////
   
      /**
       * Logger for this class
       */
      private static Logger s_logger = Log.getInstance(XAPoolSapDBDeadlockTestInternal.class);
   
      /**
       * Factory to manage domains.
       */
      protected DomainFactory m_domainFactory;
   
      /**
       * Factory to manage users.
       */
      protected UserFactory m_userFactory;
   
      /**
       * Factory to manage internal sessions.
       */
      protected InternalSessionDatabaseFactory m_sessionFactory;
   
      /**
       * Factory to manage external sessions.
       */
      protected ExternalSessionDatabaseFactory m_externalSessionFactory;
   
      /**
       * Controller used to manipulate users.
       */
      protected UserController m_userControl;
   
      /**
       * Controller to manage sessions.
       */
      protected SessionController m_sessionControl;
   
      // Attributes ///////////////////////////////////////////////////////////////
      
      /**
       * Domain which is used for setUp() and tearDown() 
       */
      private Domain m_domain;
      
      /**
       * Domain which is used for setUpSafely() and tearDown() 
       */
      private Domain m_existingDomain;
   
      /**
       * User which is used for setUp() and tearDown()
       */
      private User m_user;
   
      /**
       * User which is used for setUpSafely() and tearDown()
       */
      private User m_existingUser;
      
      /**
       * This will simulate that the user is logged in.
       */
      private InternalSession m_session;
         
      /**
       * This will simulate that the user is logged in.
       */
      private ExternalSession m_externalSession;
         
      /**
       * This will simulate that the user is logged in for setUpSafely() and tearDown()
       */
      private InternalSession m_existingSession;
   
      /**
       * Factory utilities to manage roles.
       */
      protected TestUserDatabaseFactoryUtils m_userFactoryUtils;
   
      // Constructors /////////////////////////////////////////////////////////////
      
      /**
        * Static initializer.
        */
       static
       {
          // Since the developer is using SecureDatabaseTestUtil class
          // it is required that the SecureCallContext is used to correctly
          // store and translate values
          if (!(CallContext.getInstance() instanceof SecureCallContext))
          {
             CallContext.setInstance(new SecureCallContext());
          }
   
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
        * @param strTestName - name of the test
        * @throws Exception - an error has occurred
        */
       public XAPoolSapDBDeadlockTestInternal(
          String strTestName
       ) throws Exception
       {
          super(strTestName);
   
          m_domain = null;
          m_existingDomain = null;
          m_user = null;
          m_existingUser = null;
          m_session = null;
          m_existingSession = null;
          m_externalSession = null;
          
         m_domainFactory = (DomainFactory)DataFactoryManager.getInstance(
                                         DomainFactory.class);
         m_userFactory = (UserFactory)DataFactoryManager.getInstance(
                                         UserFactory.class);
         m_sessionFactory = (InternalSessionDatabaseFactory)DataFactoryManager.getInstance(
                                                     InternalSessionFactory.class);      
         m_externalSessionFactory = (ExternalSessionDatabaseFactory)DataFactoryManager.getInstance(
                                        ExternalSessionFactory.class);      
         m_userControl = (UserController)ControllerManager.getInstance(
                                            UserController.class);
         m_sessionControl = (SessionController)ControllerManager.getInstance(
                                                   SessionController.class);
         m_userFactoryUtils = new TestUserDatabaseFactoryUtils();
       }
   
       // Tests ///////////////////////////////////////////////////////////////////
   
      /**
       * This test is the minimal test to deadlock sapdb 7.4.3.27 with JOTM 1.4.3
       * and XAPool 1.3.1. Don't look at the correctness of the test but the
       * sequence of the SQL commands. If I remove anything, the deadlock will not
       * occur.   
       * 
       * @throws Exception - error occurred
       */
      public void testSapDeadlock(
      ) throws Exception
      {
         try
         {
            s_logger.info(" setup test data transaction ===============");
   
            m_transaction.begin();
            try
            {
               // To init domain and user for later usage
               // Init Call Context for security usage (Super User)
               m_domain = new Domain(DataObject.NEW_ID, "secdbtest_setup_domain", 
                                      "Created by " + getClass().getName() + "." 
                                      + getName(), 
                                      null, null);
               m_domain = (Domain)m_domainFactory.create(m_domain);         
               m_user = new User(DataObject.NEW_ID, m_domain.getId(), "firstname", 
                                 "lastname", "", "", "Created by " + getClass().getName() + 
                                 "." + getName(), "email", "secdbtest_setup_login_name", 
                                 "defaultpassword", true, false, true, true, null, null, true);
               // We need to set the context before we even call the create user
               // so that it is called in correct context 
               CallContext.getInstance().setCurrentUserAndSession(m_user, ""); 
               m_user = (User)m_userFactory.create(m_user);
   
               m_session = new InternalSession(DataObject.NEW_ID,
                                              m_domain.getId(),
                                              m_user.getId(),
                                              "secdbtest_setup_generated_code",
                                              "secdbtest_setup_ip",
                                              "secdbtest_setup_browser_type",
                                               null,
                                               true);
               m_session = (InternalSession)m_sessionFactory.create(m_session);
   
               // We need to also create the server session, otherwise will
               // the internal session look like orphan
   //            m_externalSession = new ExternalSession(m_domain.getId(),
   //                                                m_session.getId(),
   //                                                "secdbtest_setup_server_session",
   //                                                NetUtils.getServerIPAddress());
   //            m_externalSession = ServerSessionFactoryImpl.getInstance().create(
   //                                   m_externalSession);
   
               // Now when we have the correct user, try to reset it to correct value
               CallContext.getInstance().resetCurrentUserAndSession();               
               CallContext.getInstance().setCurrentUserAndSession(m_user, 
                                            m_session.getInternalSession());
   
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
      
            //////////////////////////////////////////////////////////////////////////
      
            User     user           = null;
            Object[] loginInfo       = {null, null, null};
            String   strLoginName    = "test_login_name";
            String   strPassword     = "test_password";
            String   strSessionCode = "";
      
            PreparedStatement statement = null;
            String            strQuery;
            
            InternalSession generatedSession = null;
   
            try
            {
               s_logger.info(" create user transaction ================");
   
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
                                   strLoginName,
                                   strPassword,
                                   true,
                                   false,
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
   
               s_logger.info(" login transaction ======================");
   
               // --------------------------------------------------------------------
               m_transaction.begin();
               try
               {
                  strLoginName = "test_login_name";
                  strPassword  = "test_password";
                  // try to login user with correct login and password
                  loginInfo = m_sessionControl.login(strLoginName, strPassword,
                                                     "test_server_session",
                                                     "test_c_IP",
                                                     "test_browser_type",
                                                     new InstanceInfoImpl("test_s_IP"));
                  m_transaction.commit();
               }
               catch (Throwable thr)
               {
                  m_transaction.rollback();
                  throw new Exception(thr);
               }
               strSessionCode = loginInfo[2].toString();
      
               s_logger.info(" read session no transaction ============");
   
               // -----------------------------------------------------------------------
               // Now check if the generated session has a good guest access
               
               generatedSession = m_sessionFactory.get(strSessionCode);
      
               s_logger.info(" update user transaction =================");
   
               // -----------------------------------------------------------------------
               m_transaction.begin();
               try
               {
                  // disable login to the user
                  strQuery = "update " + UserDatabaseSchema.USER_TABLE_NAME + 
                             " set LOGIN_ENABLED = 0 where ID=?";   
                  statement = m_connection.prepareStatement(strQuery);
                  statement.setInt(1, user.getId());
                  statement.executeUpdate();
                  m_transaction.commit();
               }
               catch (Throwable thr)
               {
                  m_transaction.rollback();
                  throw new Exception(thr);
               }
   
               s_logger.info(" login transaction ======================");
   
               m_transaction.begin();
               try
               {
                  strLoginName = "test_login_name";
                  strPassword  = "test_password";
                  // try to login disabled user with correct login and password
                  loginInfo = m_sessionControl.login(strLoginName, strPassword,
                                                     "test_server_session",
                                                     "test_c_IP",
                                                     "test_browser_type",
                                                     new InstanceInfoImpl("test_s_IP"));
                  m_transaction.commit();
               }
               catch (Throwable thr)
               {
                  s_logger.log(Level.SEVERE, "Login failed", thr);
                  m_transaction.rollback();
                  throw new Exception(thr);
               }
            }
            finally
            {
               m_transaction.begin();
               try
               {
                  // delete BF user
                  try
                  {
                     // delete all external sessions belonging to the internal one
                     strQuery = "delete from " + 
                                SessionDatabaseSchema.EXTSESSION_TABLE_NAME + 
                                " where INTERNAL_SESSION_ID=?";   
                     statement = m_connection.prepareStatement(strQuery);
                     statement.setInt(1, generatedSession.getId());
                     statement.executeUpdate();
                  }
                  finally
                  {
                     DatabaseUtils.closeStatement(statement);
                  }
   
                  try
                  {
                     // delete internal session
                     strQuery = "delete from " + 
                                SessionDatabaseSchema.INTSESSION_TABLE_NAME + 
                                " where GEN_CODE=?";   
                     statement = m_connection.prepareStatement(strQuery);
                     statement.setString(1, strSessionCode);
                     statement.executeUpdate();
                  }
                  finally
                  {
                     DatabaseUtils.closeStatement(statement);
                  }
   
                  // delete created user
                  if (user != null && user.getId() != DataObject.NEW_ID)
                  {
                     // Since we are already using connection pass it to the method
                     // so that we do not encounter deadlock when two connections
                     // are used (it would be ok to use two connections if before
                     // we get the second one, we return the first one to the pool
                     // but that is not the case).
                     m_userFactoryUtils.deleteUserCascadeManual(m_connection, user.getId());
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
         finally
         {
            s_logger.info(" delete test data transaction ==============");
               
            // Destruct everything properly in case even the setup has failed.
            try
            {
               m_transaction.begin();
   
               // Destruct everything properly in case even the setup has failed.
               try
               {
                  if ((m_externalSession != null) 
                     && (m_externalSession.getId() != DataObject.NEW_ID))
                  {
   //   ServerSessionFactoryImpl.getInstance().delete(m_externalSession.getServerSessionGen(),
   //                                                 m_externalSession.getServerIP());
                  }         
               }
               catch (Throwable thr)
               {
                  thr.printStackTrace();
                  throw new Exception(thr); 
               }
               finally
               {
                  try
                  {   
                     // Don't delete the user if it already existed there
                     if ((m_session != null) 
                        && (m_session.getId() != DataObject.NEW_ID)
                        && (m_existingSession == null))
                     {
   //                     s_logger.info(DatabaseConnectionFactoryImpl.getInstance().debug());
                        ///////////////////////////////////////////////////////////
                        // Sapdn 7.4.3.27 and JOTM 1.4.3 and XAPool 1.3.1 will
                        // deadlock the sapdb when executing this call                     
                        ///////////////////////////////////////////////////////////
                        m_sessionFactory.delete(m_session.getInternalSession());
                        s_logger.info(" session deleted ================");
                     }
                  }
                  catch (Throwable thr)
                  {
                     thr.printStackTrace();
                     throw new Exception(thr); 
                  }
                  finally
                  {
                     try
                     {   
                        // Don't delete the user if it already existed there
                        if ((m_user != null) && (m_user.getId() != DataObject.NEW_ID)
                           && (m_existingUser == null))
                        {
                           m_userFactoryUtils.deleteUserCascadeManual(m_user.getId());
                        }
                     }
                     catch (Throwable thr)
                     {
                        thr.printStackTrace();
                        throw new Exception(thr); 
                     }
                     
                     finally
                     {
                        try
                        {
                           // Don't delete the domain if it already existed there
                           if ((m_domain != null) 
                              && (m_domain.getId() != DataObject.NEW_ID)
                              && (m_existingDomain == null))
                           {
                              m_domainFactory.delete(
                                 m_domain.getId(),
                                 CallContext.getInstance().getCurrentDomainId());
                           }
                        }
                        catch (Throwable thr)
                        {
                           thr.printStackTrace();
                           throw new Exception(thr); 
                        }
                        finally
                        {
                           CallContext.getInstance().reset();
                           m_session = null;
                           m_user = null;
                           m_domain = null;
                        }
                     }      
                  }
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
