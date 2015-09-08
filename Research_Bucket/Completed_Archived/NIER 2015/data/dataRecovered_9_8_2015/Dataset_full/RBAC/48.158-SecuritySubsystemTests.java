/*
 * Copyright (c) 2003 - 2008 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: SecuritySubsystemTests.java,v 1.5 2009/09/20 05:32:57 bastafidli Exp $
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
 
package org.opensubsystems.security;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.opensubsystems.security.application.SecureApplicationTestSetup;
import org.opensubsystems.core.persist.db.DatabaseTestSuite;
import org.opensubsystems.core.util.Tests;
import org.opensubsystems.security.logic.AuthenticationControllerTest.AuthenticationControllerTestInternal;
import org.opensubsystems.security.logic.AuthorizationControllerTest.AuthorizationControllerTestInternal;
import org.opensubsystems.security.logic.XAPoolSapDBDeadlockTest.XAPoolSapDBDeadlockTestInternal;

/**
 * Test for classes included in security package.
 *
 * @version $Id: SecuritySubsystemTests.java,v 1.5 2009/09/20 05:32:57 bastafidli Exp $
 * @author Miro Halas
 * @code.reviewer Miro Halas
 * @code.reviewed 1.9 2005/11/04 00:26:23 jlegeny
 */
public class SecuritySubsystemTests extends Tests
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Constructor.
    */
   protected SecuritySubsystemTests(
   )
   {
      super();
   }

   // Logic ////////////////////////////////////////////////////////////////////

   /**
    * Create suite of all security tests.
    * 
    * @return Test - suite of tests to run
    */
   public static Test suite(
   )
   {
      TestSuite suite = new DatabaseTestSuite("Test for security");
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

      // Use ApplicationTestSetup instead of DatabaseTestSetup since the 
      // tested classes are part of application module specified in the tests 
      // below. This setup ensures that all the required related classes are 
      // correctly initialized
      TestSetup wrapper = new SecureApplicationTestSetup(suite);

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
      DomainTests.addGenericTests(suite);
      UserTests.addGenericTests(suite);
      RoleTests.addGenericTests(suite);
      SessionTests.addGenericTests(suite);
      suite.addTestSuite(AuthenticationControllerTestInternal.class);
      suite.addTestSuite(AuthorizationControllerTestInternal.class);
      suite.addTestSuite(XAPoolSapDBDeadlockTestInternal.class);
   }   
}
