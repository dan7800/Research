/*
 * Copyright (c) 2003 - 2008 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: ProxoolDatabaseConnectionFactoryTest.java,v 1.1 2009/04/22 06:16:46 bastafidli Exp $
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

package org.opensubsystems.core.persist.db.connectionpool.proxool;

import java.sql.Connection;
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

/**
 * Test of Proxool database connection factory.
 * 
 * @version $Id: ProxoolDatabaseConnectionFactoryTest.java,v 1.1 2009/04/22 06:16:46 bastafidli Exp $
 * @author Julian Legeny
 * @code.reviewer Miro Halas
 * @code.reviewed 1.6 2004/10/05 07:39:53 bastafidli
 */
public final class ProxoolDatabaseConnectionFactoryTest extends Tests 
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private ProxoolDatabaseConnectionFactoryTest(
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
      TestSuite suite = new DatabaseTestSuite("ProxoolDatabaseConnectionFactoryTest");
      suite.addTestSuite(ProxoolDatabaseConnectionFactoryTestInternal.class);
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
   public static class ProxoolDatabaseConnectionFactoryTestInternal
                          extends PooledDatabaseConnectionFactoryImplBaseTest
   {
      /**
       * Create new ProxoolDatabaseConnectionFactoryTest.
       * 
       * @param strTestName - name of the test
       */
      public ProxoolDatabaseConnectionFactoryTestInternal(
         String strTestName
      )
      {
         super(strTestName);
      }
   
      /**
       * Set up environment for the test case.
       * 
       * @throws Exception - an error has occurred during setting up test
       */
      protected void setUp(
      ) throws Exception
      {
         // initialize connection factory
         m_connectionFactory = new ProxoolDatabaseConnectionFactoryImpl();
         
         super.setUp();
      }
   
      /**
       * {@inheritDoc}
       */
      protected boolean containsConnection(
         List       lstConnections, 
         Connection newConnection
      ) throws OSSInternalErrorException
      {
         Connection cConnectionFromList = null;
         Iterator                   itHelp;
         boolean                    bReturn = false;
   
//         Connection testConn1 = null;
//         Connection testConn2 = null;
   
         // for each connection from the list test if new requested connection is 
         // contained in the list
         itHelp = lstConnections.iterator();
         while (itHelp.hasNext())
         {
            cConnectionFromList = (Connection)itHelp.next();
// TODO: Bug: Proxool 0.9.0RC2: Reported as bug# 1468635. This method was 
// originally implemented as this but Proxool deprecated method 
// ProxoolFacade.getDelegateConnection saying we should be able to just use the 
// given connections. This seems to not work since test testRequestXReturnX is 
// failing for Proxool. Investigate this please.
//            try
//            {
//               testConn1 = ProxoolFacade.getDelegateConnection(cConnectionFromList);
//               testConn2 = ProxoolFacade.getDelegateConnection(newConnection);
//               bReturn = bReturn || (testConn1 == testConn2);
//            }
//            catch (ProxoolException peExc)
//            {
//               throw new OSSInternalErrorException("There was an error occurred during getting" +
//                     " the proxool connection.", peExc);
//            }
            bReturn = bReturn || (cConnectionFromList == newConnection);
         }
   
         return bReturn; 
      }
   }
}
