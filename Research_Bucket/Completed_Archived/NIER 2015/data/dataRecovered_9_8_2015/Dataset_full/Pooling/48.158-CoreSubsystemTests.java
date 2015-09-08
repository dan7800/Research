/*
 * Copyright (c) 2003 - 2008 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: CoreSubsystemTests.java,v 1.5 2009/04/22 06:17:44 bastafidli Exp $
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

package org.opensubsystems.core;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.opensubsystems.core.persist.DataFactoryManagerTest;
import org.opensubsystems.core.persist.db.DatabaseFactoryClassFactoryTest;
import org.opensubsystems.core.persist.db.DatabaseSchemaClassFactoryTest;
import org.opensubsystems.core.persist.db.DatabaseSchemaManagerTest;
import org.opensubsystems.core.persist.db.DatabaseTestSetup;
import org.opensubsystems.core.persist.db.DatabaseTestSuite;
import org.opensubsystems.core.persist.db.connectionpool.ConnectionPoolTests;
import org.opensubsystems.core.persist.db.driver.DriverTests;
import org.opensubsystems.core.util.ClassFactoryTest;
import org.opensubsystems.core.util.ClassUtilsTest;
import org.opensubsystems.core.util.ConfigTest;
import org.opensubsystems.core.util.CryptoUtilsTest;
import org.opensubsystems.core.util.DateUtilsTest;
import org.opensubsystems.core.util.FileCommitUtilsTest;
import org.opensubsystems.core.util.FileUtilsTest;
import org.opensubsystems.core.util.MultiConfigTest;
import org.opensubsystems.core.util.StringUtilsTest;
import org.opensubsystems.core.util.Tests;

/**
 * Test for classes included in Core package.
 *
 * @version $Id: CoreSubsystemTests.java,v 1.5 2009/04/22 06:17:44 bastafidli Exp $
 * @author Miro Halas
 * @code.reviewer Miro Halas
 * @code.reviewed 1.14 2005/09/09 06:50:44 bastafidli
 */
public final class CoreSubsystemTests extends Tests
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Constructor
    */
   public CoreSubsystemTests(
   )
   {
      // Do nothing
   }
   
   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * Create suite of all core tests.
    * 
    * @return Test - suite of tests to run
    */
   public static Test suite(
   )
   {
      TestSuite suite = new DatabaseTestSuite("Test for core");
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
    * @param suite - suite of tests to run
    */
   public static void addGenericTests(
      TestSuite suite
   ) 
   {
      suite.addTestSuite(ClassFactoryTest.class);
      suite.addTestSuite(ClassUtilsTest.class);
      suite.addTestSuite(ConfigTest.class);
      suite.addTestSuite(MultiConfigTest.class);
      suite.addTestSuite(DateUtilsTest.class);
      suite.addTestSuite(FileCommitUtilsTest.class);
      suite.addTestSuite(FileUtilsTest.class);
      suite.addTestSuite(CryptoUtilsTest.class);
      suite.addTestSuite(StringUtilsTest.class);
      suite.addTestSuite(DataFactoryManagerTest.class);
      suite.addTestSuite(DatabaseFactoryClassFactoryTest.class);
      suite.addTestSuite(DatabaseSchemaClassFactoryTest.class);
      suite.addTestSuite(DatabaseSchemaManagerTest.class);
      ConnectionPoolTests.addGenericTests(suite);
      DriverTests.addGenericTests(suite); 
   }   
}
