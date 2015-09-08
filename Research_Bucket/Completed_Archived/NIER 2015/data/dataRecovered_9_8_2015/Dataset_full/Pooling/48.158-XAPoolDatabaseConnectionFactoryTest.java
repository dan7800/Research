/*
 * Copyright (c) 2003 - 2008 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: XAPoolDatabaseConnectionFactoryTest.java,v 1.1 2009/04/22 06:17:05 bastafidli Exp $
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

package org.opensubsystems.core.persist.db.connectionpool.xapool;

import java.sql.Connection;
import java.util.Iterator;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.enhydra.jdbc.standard.StandardXAConnectionHandle;
import org.opensubsystems.core.persist.db.DatabaseTestSetup;
import org.opensubsystems.core.persist.db.DatabaseTestSuite;
import org.opensubsystems.core.persist.db.connectionpool.PooledDatabaseConnectionFactoryImplBaseTest;
import org.opensubsystems.core.util.Tests;

/**
 * Test of XAPool database connection factory.
 * 
 * @version $Id: XAPoolDatabaseConnectionFactoryTest.java,v 1.1 2009/04/22 06:17:05 bastafidli Exp $
 * @author Julian Legeny
 * @code.reviewer Miro Halas
 * @code.reviewed 1.10 2004/10/07 07:31:07 bastafidli
 */
public final class XAPoolDatabaseConnectionFactoryTest extends Tests 
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private XAPoolDatabaseConnectionFactoryTest(
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
      TestSuite suite = new DatabaseTestSuite("XAPoolDatabaseConnectionFactoryTest");
      suite.addTestSuite(XAPoolDatabaseConnectionFactoryTestInternal.class);
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
   public static class XAPoolDatabaseConnectionFactoryTestInternal
                          extends PooledDatabaseConnectionFactoryImplBaseTest
   {
      /**
       * Create new XAPoolDatabaseConnectionFactoryTest.
       * 
       * @param strTestName - name of the test
       */
      public XAPoolDatabaseConnectionFactoryTestInternal(
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
         m_connectionFactory = new XAPoolDatabaseConnectionFactoryImpl();
         
         super.setUp();
      }
   
      /**
       * {@inheritDoc}
       */
      protected boolean containsConnection(
         List lstConnections, 
         Connection newConnection
      )
      {
         StandardXAConnectionHandle cConnectionFromList = null;
         Iterator                   itHelp;
         boolean                    bReturn = false;
   
         // for each connection from the list test if new requested connection is 
         // contained in the list
         itHelp = lstConnections.iterator();
         while (itHelp.hasNext())
         {
            cConnectionFromList = (StandardXAConnectionHandle) itHelp.next();
            bReturn = bReturn || ((cConnectionFromList).con 
                                 == ((StandardXAConnectionHandle) newConnection).con);         
         }
   
         return bReturn;
      }
   }
}
