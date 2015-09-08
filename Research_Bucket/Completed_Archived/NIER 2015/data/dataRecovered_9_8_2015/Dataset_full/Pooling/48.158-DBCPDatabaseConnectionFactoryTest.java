/*
 * Copyright (c) 2003 - 2009 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: DBCPDatabaseConnectionFactoryTest.java,v 1.1 2009/04/22 06:16:28 bastafidli Exp $
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

package org.opensubsystems.core.persist.db.connectionpool.dbcp;

import java.sql.Connection;
import java.util.Iterator;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.opensubsystems.core.persist.db.DatabaseTestSetup;
import org.opensubsystems.core.persist.db.DatabaseTestSuite;
import org.opensubsystems.core.persist.db.connectionpool.PooledDatabaseConnectionFactoryImplBaseTest;
import org.opensubsystems.core.util.Tests;

/**
 * Test of DBCP database connection factory.
 * 
 * @version $Id: DBCPDatabaseConnectionFactoryTest.java,v 1.1 2009/04/22 06:16:28 bastafidli Exp $
 * @author Julian Legeny
 * @code.reviewer Miro Halas
 * @code.reviewed 1.7 2004/10/05 07:39:53 bastafidli
 */
public final class DBCPDatabaseConnectionFactoryTest extends Tests 
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private DBCPDatabaseConnectionFactoryTest(
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
      TestSuite suite = new DatabaseTestSuite("DBCPDatabaseConnectionFactoryTest");
      suite.addTestSuite(DBCPDatabaseConnectionFactoryTestInternal.class);
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
   public static class DBCPDatabaseConnectionFactoryTestInternal 
                          extends PooledDatabaseConnectionFactoryImplBaseTest
   {
      /**
       * Create new  DBCPDatabaseConnectionFactoryTest.
       * 
       * @param strTestName - name of the test
       */
      public DBCPDatabaseConnectionFactoryTestInternal(
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
         m_connectionFactory = new DBCPDatabaseConnectionFactoryImpl();
         
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
         Connection cConnectionFromList = null;
         Iterator                   itHelp;
         boolean                    bReturn = false;
   
         // for each connection from the list test if new requested connection is 
         // contained in the list
         itHelp = lstConnections.iterator();
         while (itHelp.hasNext())
         {
            cConnectionFromList = (Connection) itHelp.next();
            bReturn = bReturn || (cConnectionFromList == newConnection);         
         }
   
         return bReturn;   
      }
   }
}
