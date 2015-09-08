/*
 * Copyright (c) 2003 - 2008 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: C3P0DatabaseConnectionFactoryTest.java,v 1.1 2009/04/22 06:16:14 bastafidli Exp $
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

package org.opensubsystems.core.persist.db.connectionpool.c3p0;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.opensubsystems.core.error.OSSInternalErrorException;
import org.opensubsystems.core.persist.db.DatabaseTestSetup;
import org.opensubsystems.core.persist.db.DatabaseTestSuite;
import org.opensubsystems.core.persist.db.connectionpool.PooledDatabaseConnectionFactoryImplBaseTest;
import org.opensubsystems.core.util.Tests;

import com.mchange.v2.c3p0.C3P0ProxyConnection;
import com.mchange.v2.c3p0.util.TestUtils;

/**
 * Test of C3P0 database connection factory.
 * 
 * @version $Id: C3P0DatabaseConnectionFactoryTest.java,v 1.1 2009/04/22 06:16:14 bastafidli Exp $
 * @author Julian Legeny
 * @code.reviewer Miro Halas
 * @code.reviewed 1.8 2004/11/22 09:47:07 jlegeny
 */
public final class C3P0DatabaseConnectionFactoryTest extends Tests 
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private C3P0DatabaseConnectionFactoryTest(
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
      TestSuite suite = new DatabaseTestSuite("C3P0DatabaseConnectionFactoryTest");
      suite.addTestSuite(C3P0DatabaseConnectionFactoryTestInternal.class);
      // Here we are using DatabaseTestSetup instead of ApplicationTestSetup
      // since we are just directly testing  database functionality without
      // accessing any business logic functionality packaged into application 
      // modules
      TestSetup wrapper = new DatabaseTestSetup(suite);

      return wrapper;
   }

   /**
    * Internal class which can be included in other test suites directly without
    * including the above suite. This allows us to group multiple tests 
    * together and the execute the DatabaseTestSetup only once 
    */
   public static class C3P0DatabaseConnectionFactoryTestInternal 
                          extends PooledDatabaseConnectionFactoryImplBaseTest
   {
      /**
       * Create new C3P0DatabaseConnectionFactoryTest.
       * 
       * @param strTestName - name of the test
       */
      public C3P0DatabaseConnectionFactoryTestInternal(
         String strTestName
      )
      {
         super(strTestName);
      }
   
      /**
       * {@inheritDoc}
       */
      protected void setUp(
      ) throws Exception
      {
         // initialize connection factory
         m_connectionFactory = new C3P0DatabaseConnectionFactoryImpl();
         
         super.setUp();
      }
   
      /**
       * {@inheritDoc}
       */
      protected Object addItem(
         Connection newConnection
      ) throws OSSInternalErrorException
      {
         try
         {
            return new Integer(TestUtils.physicalConnectionIdentityHashCode(
                          (C3P0ProxyConnection) newConnection));
         } 
         catch (SQLException sqlExc)
         {
            throw new OSSInternalErrorException("Error occurred during getting of physical " +
                                                "connection identity hash code.", sqlExc);
         }
      }
   
      /**
       * {@inheritDoc}
       */
      protected boolean containsConnection(
         List lstConnections, 
         Connection newConnection
      ) throws OSSInternalErrorException
      {
         Integer  iHashCodeFromList = null;
         Iterator itHelp;
         boolean  bReturn = false;
   
         // for each connection from the list test if new requested connection is 
         // contained in the list
         itHelp = lstConnections.iterator();
         while (itHelp.hasNext())
         {
            
            iHashCodeFromList = (Integer) itHelp.next();
            try
            {
               bReturn = bReturn || iHashCodeFromList.intValue() 
                         == TestUtils.physicalConnectionIdentityHashCode(
                                 (C3P0ProxyConnection) newConnection);
            }
            catch (SQLException sqlExc)
            {
               throw new OSSInternalErrorException("Error occurred during getting of physical " +
                                                   "connection identity hash code.", sqlExc);
            }
         }
   
         return bReturn;
      }
   }
}
