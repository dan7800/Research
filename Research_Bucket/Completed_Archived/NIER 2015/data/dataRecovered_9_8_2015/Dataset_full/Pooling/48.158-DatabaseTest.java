/*
 * Copyright (c) 2003 - 2007 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: DatabaseTest.java,v 1.15 2009/04/22 06:17:44 bastafidli Exp $
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

package org.opensubsystems.core.persist.db;

import java.sql.Connection;

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.opensubsystems.core.error.OSSConfigException;
import org.opensubsystems.core.persist.db.impl.DatabaseConnectionFactoryImpl;
import org.opensubsystems.core.persist.db.impl.DatabaseTransactionFactoryImpl;
import org.opensubsystems.core.util.Config;

/**
 * Base class for all tests that access the database. Test cases derived from 
 * this class should use DatabaseTestSetup adapter and follow comments in this 
 * class to properly initialize and shutdown database.
 *
 * @version $Id: DatabaseTest.java,v 1.15 2009/04/22 06:17:44 bastafidli Exp $
 * @author Miro Halas
 * @code.reviewer Miro Halas
 * @code.reviewed 1.5 2004/12/18 06:18:22 bastafidli
 */
public abstract class DatabaseTest extends TestCase
{
   // Constants ////////////////////////////////////////////////////////////////

   /**
    * Default property file used to run tests.
    */
   public static final String DEFAULT_PROPERTY_FILE = "osstest.properties";

   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Database transaction which can be used by test. This transaction object
    * is instantiated before every test is started and checked if the 
    * transaction was properly finished after the test is done. 
    */
   protected UserTransaction m_transaction;
   
   /**
    * Connection to the database which can be used by test. This connection object
    * is instantiated before every test is started and returned after the test is 
    * done. This connection may be used to quickly access the database in the test
    * without worrying about its creation and destruction.
    */
   protected Connection m_connection;

   /**
    * How many connections were requested at the beginning of the test from 
    * the pool. This should must the number of requested connection after the 
    * test. 
    */
   protected int m_iRequestedConnectionCount;
   
   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Static initializer 
    */
   static
   {
      if (Config.getInstance().getRequestedConfigFile() == null)
      {
         Config.getInstance().setPropertyFileName(DEFAULT_PROPERTY_FILE);
      }
   }

   /**
    * Create new DatabaseTest.
    * 
    * @param strTestName - name of the test
    * @throws OSSConfigException - an error has occurred
    */
   public DatabaseTest(
      String strTestName
   ) 
   {
      super(strTestName);
    
      m_transaction = null;
      m_connection = null;
   }   
   
   /**
    * Set up environment for the test case.
    * 
    * @throws Exception - an error has occurred while setting up test
    */
   protected void setUp(
   ) throws Exception
   {
      super.setUp();

      // Get connection after the transaction so that we do not have to worry
      // about returning it in case it fails
      m_transaction = DatabaseTransactionFactoryImpl.getInstance().requestTransaction();
      // Request autocommit false since we might be modifying database
      m_connection = DatabaseConnectionFactoryImpl.getInstance().requestConnection(false);
      
      // Remember how many connections should be out of the pool after the test
      m_iRequestedConnectionCount 
         = DatabaseConnectionFactoryImpl.getInstance().getTotalRequestedConnectionCount();
   }

   /**
    * Restore original environment after the test case.
    * 
    * @throws Exception - an error has occurred while tearing down test
    */
   protected void tearDown(
   ) throws Exception
   {
      try
      {
         // Remember how many connections should be out of the pool after the test
         assertEquals("Somebody forgot to return connection to the pool.",
            m_iRequestedConnectionCount,  
            DatabaseConnectionFactoryImpl.getInstance().getTotalRequestedConnectionCount());

         int iStatus;
         
         iStatus = m_transaction.getStatus();
         
         if ((iStatus != Status.STATUS_NO_TRANSACTION)
             && (iStatus != Status.STATUS_COMMITTED)
             && (iStatus != Status.STATUS_ROLLEDBACK))
         {
            fail("Transaction wasn't commited or rollbacked. Status is " + iStatus);
         }
      }
      finally
      {
         if (m_connection != null)
         {
            if (!m_connection.isClosed())
            {
               DatabaseConnectionFactoryImpl.getInstance().returnConnection(m_connection);
            }
         }
      }

      super.tearDown();
   }
}
