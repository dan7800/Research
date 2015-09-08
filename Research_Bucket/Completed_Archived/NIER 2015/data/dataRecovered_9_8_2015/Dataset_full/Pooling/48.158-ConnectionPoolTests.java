/*
 * Copyright (c) 2003 - 2008 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: ConnectionPoolTests.java,v 1.6 2009/04/22 06:17:43 bastafidli Exp $
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

package org.opensubsystems.core.persist.db.connectionpool;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.opensubsystems.core.persist.db.DatabaseTestSetup;
import org.opensubsystems.core.persist.db.DatabaseTestSuite;
import org.opensubsystems.core.persist.db.connectionpool.c3p0.C3P0DatabaseConnectionFactoryTest.C3P0DatabaseConnectionFactoryTestInternal;
import org.opensubsystems.core.persist.db.connectionpool.dbcp.DBCPDatabaseConnectionFactoryTest.DBCPDatabaseConnectionFactoryTestInternal;
import org.opensubsystems.core.persist.db.connectionpool.proxool.ProxoolDatabaseConnectionFactoryTest.ProxoolDatabaseConnectionFactoryTestInternal;
import org.opensubsystems.core.persist.db.connectionpool.xapool.XAPoolDatabaseConnectionFactoryTest.XAPoolDatabaseConnectionFactoryTestInternal;
import org.opensubsystems.core.util.Tests;

/**
 * Test suite containing all tests verifying behaviour of different connection
 * pools.
 * 
 * @version $Id: ConnectionPoolTests.java,v 1.6 2009/04/22 06:17:43 bastafidli Exp $
 * @author Miro Halas
 * @code.reviewer Miro Halas
 * @code.reviewed Initial revision
 */
public final class ConnectionPoolTests extends Tests
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private ConnectionPoolTests(
   )
   {
      // Do nothing
   }
   
   // Logic ////////////////////////////////////////////////////////////////////

   /**
    * Create suite of all database driver tests.
    * 
    * @return Test - suite of tests to run
    */
   public static Test suite(
   )
   {
      TestSuite suite = new DatabaseTestSuite("Test for connection pools");
      try
      {
         addGenericTests(suite);
      }
      catch (Throwable thr)
      {
         System.out.println(thr);
         System.out.println(thr.getCause());
         thr.getCause().printStackTrace(System.out);
      }

      // Here we are using DatabaseTestSetup instead of ApplicationTestSetup
      // since we are just directly testing  database functionality without
      // accessing any business logic functionality packaged into application 
      // modules
      TestSetup wrapper = new DatabaseTestSetup(suite);

      return wrapper;
   }

   /**
    * Add all generic database tests to given suite.
    * 
    * @param suite - suite to add tests to
    */
   public static void addGenericTests(
      TestSuite suite
   ) 
   {      
      suite.addTestSuite(C3P0DatabaseConnectionFactoryTestInternal.class);
      suite.addTestSuite(DBCPDatabaseConnectionFactoryTestInternal.class);
      suite.addTestSuite(ProxoolDatabaseConnectionFactoryTestInternal.class);
      suite.addTestSuite(XAPoolDatabaseConnectionFactoryTestInternal.class);
   }   
}
