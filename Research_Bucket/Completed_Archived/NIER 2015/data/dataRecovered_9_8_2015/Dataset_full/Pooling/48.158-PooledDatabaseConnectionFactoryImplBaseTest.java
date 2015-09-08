/*
 * Copyright (c) 2003 - 2007 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: PooledDatabaseConnectionFactoryImplBaseTest.java,v 1.10 2009/04/22 06:17:43 bastafidli Exp $
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

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opensubsystems.core.error.OSSDatabaseAccessException;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.error.OSSInternalErrorException;
import org.opensubsystems.core.persist.db.connectionpool.c3p0.C3P0DatabaseConnectionFactoryTest.C3P0DatabaseConnectionFactoryTestInternal;
import org.opensubsystems.core.persist.db.connectionpool.impl.PooledDatabaseConnectionFactorySetupReader;
import org.opensubsystems.core.persist.db.connectionpool.proxool.ProxoolDatabaseConnectionFactoryTest.ProxoolDatabaseConnectionFactoryTestInternal;
import org.opensubsystems.core.persist.db.connectionpool.xapool.XAPoolDatabaseConnectionFactoryTest.XAPoolDatabaseConnectionFactoryTestInternal;
import org.opensubsystems.core.util.Config;
import org.opensubsystems.core.util.GlobalConstants;
import org.opensubsystems.core.util.Log;

/**
 * Base class containing tests for general concepts of  pooled database connection
 *  factories, which every pooled database connection factory should support.
 * 
 * @version $Id: PooledDatabaseConnectionFactoryImplBaseTest.java,v 1.10 2009/04/22 06:17:43 bastafidli Exp $
 * @author Julian Legeny
 * @code.reviewer Miro Halas
 * @code.reviewed 1.14 2004/10/05 07:39:53 bastafidli
 */
public abstract class PooledDatabaseConnectionFactoryImplBaseTest extends
                         DatabaseConnectionFactoryBaseTest
{
   // Constants/////////////////////////////////////////////////////////////////
   
   /**
    * Constant defining the maximal number of connections in the pool for this
    * test.
    */
   protected static final int ALL_CONNECTION_COUNT = 5;
   
   /**
    * Constant defining how long the pool will be waiting when there all the 
    * connections in the pool are in use. Time is specified in milliseconds
    * c3p0 is executing lots of operations asynchronously and if this is too low
    * (e.g. 2000) you may get message that it cannot acquire connection even
    * though connection is available. Solution is to keep this high enough.
    */
   protected static final int POOL_WAIT_PERIOD = 5000;
 
   // Cached variables /////////////////////////////////////////////////////////
   
   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(
                            PooledDatabaseConnectionFactoryImplBaseTest.class);   
   
   // Constructors /////////////////////////////////////////////////////////////

   /**
    * Create new  PooledDatabaseConnectionFactoryImplBaseTest.
    * 
    * @param strTestName - name of the test
    */
   public PooledDatabaseConnectionFactoryImplBaseTest(
      String strTestName
   )
   {
      super(strTestName);
   }

   // Tests ////////////////////////////////////////////////////////////////////

   /**
    * Set up environment for the test case.
    * 
    * @throws Exception - an error has occurred during setting up test
    */
   protected void setUp(
   ) throws Exception
   {
      String strTestName;

      // set actual test name
      strTestName = getName();

      // get properties and predefine particular values
      Properties prpSettings = Config.getInstance().getProperties();

      // construct property name for MIN POOL SIZE
      String strMinPoolSize 
         = PooledDatabaseConnectionFactorySetupReader.DATABASE_POOL_BASE_PATH
           + "." + DATASOURCE_NAME_1 + "." 
           + PooledDatabaseConnectionFactorySetupReader.DBPOOL_MIN_SIZE;
      
      // set minimal size for connection pool
      // We have to allocate minimal count less then the max count since in the 
      // method testRequestConnectionByUserAndPassword we allocate connection 
      // using different user name and password and it fails to do it 
      // TODO: Bug: XAPool 1.4.1: Once XAPool fixes it behavior consider setting 
      // it back to MAX_SIZE 
      prpSettings.put(strMinPoolSize, 
                      Integer.toString(ALL_CONNECTION_COUNT - 1));

      // construct property name for INIT POOL SIZE
      String strInitPoolSize 
         = PooledDatabaseConnectionFactorySetupReader.DATABASE_POOL_BASE_PATH 
           + "." + DATASOURCE_NAME_1 + "." 
           + PooledDatabaseConnectionFactorySetupReader.DBPOOL_INITIAL_SIZE; 
      // set initial size for connection pool
      prpSettings.put(strInitPoolSize, 
                               (new Integer(ALL_CONNECTION_COUNT)).toString());

      // construct property name for MAX POOL SIZE
      String strMaxPoolSize 
         = PooledDatabaseConnectionFactorySetupReader.DATABASE_POOL_BASE_PATH 
           + "." + DATASOURCE_NAME_1 + "." 
           + PooledDatabaseConnectionFactorySetupReader.DBPOOL_MAX_SIZE; 
      // set maximal size for connection pool
      prpSettings.put(strMaxPoolSize, 
                               (new Integer(ALL_CONNECTION_COUNT)).toString());

      // construct property name for POOL WAIT PERIOD
      String strWaitPeriod 
         = PooledDatabaseConnectionFactorySetupReader.DATABASE_POOL_BASE_PATH 
           + "." + DATASOURCE_NAME_1 + "." 
           + PooledDatabaseConnectionFactorySetupReader.DBPOOL_WAIT_PERIOD; 
      // set wait period for pool
      prpSettings.put(strWaitPeriod, 
                               (new Integer(POOL_WAIT_PERIOD)).toString());

      // construct property name for CAN GROW
      String strCanGrow 
         = PooledDatabaseConnectionFactorySetupReader.DATABASE_POOL_BASE_PATH 
           + "." + DATASOURCE_NAME_1 + "." 
           + PooledDatabaseConnectionFactorySetupReader.DBPOOL_CAN_GROW; 

      // set CAN GROW flag - it depends on particular test
      if (strTestName.equals("testRequestOneMoreCannotGrow"))
      {
         prpSettings.put(strCanGrow, GlobalConstants.INTEGER_0.toString());
      }
      if (strTestName.equals("testRequestOneMoreCanGrow"))
      {
         prpSettings.put(strCanGrow, GlobalConstants.INTEGER_1.toString());
      }

      Config.setInstance(new Config(prpSettings));

      super.setUp();
   }

   /**
    * Test if I configure the factory to return at most X connections and block 
    * then I cannot retrieve more.
    * 
    * @throws Exception - an error has occurred during test
    */
   public void testRequestXReturnX(
   ) throws Exception
   {
      Connection con = null;
      int        iIndex;
      List       lstConIdentifiers = new ArrayList(ALL_CONNECTION_COUNT);
      List       lstConnections = new ArrayList(ALL_CONNECTION_COUNT);

      try
      {
         for (iIndex = 0; iIndex < ALL_CONNECTION_COUNT; iIndex++)
         {
            // request connection
            // Request autocommit true since we are just reading data from the 
            // database
            con = m_connectionFactory.requestConnection(true);
            
            // We need to keep the connection so that the next iteration forces 
            // the pool to return us new connection. We will return them all in 
            // finally
            lstConnections.add(con);

            // Test if just requested connection is in list.
            assertFalse("The pool should NOT return existing connection again" +
                       " since it wasn't returned yet.", 
                       containsConnection(lstConIdentifiers, con));

            // Add connection identifier it to the list, it will be used for 
            // testing if the connection is really pooled and can be requested 
            // again
            lstConIdentifiers.add(addItem(con));
         }
      }
      finally
      {
         for (iIndex = 0; (iIndex < ALL_CONNECTION_COUNT) 
             && (!lstConnections.isEmpty()); iIndex++)
         {
            // Return connection, we still remember it but it is already returned
            m_connectionFactory.returnConnection(
                (Connection)lstConnections.remove(0));
         }
      }
      
      assertTrue("Not all connections were returned.", lstConnections.isEmpty());

      // At this point we have stored identifiers for all connections that we 
      // already requested and returned.  Now we will request all connections 
      // again and test if they are the same as are stored in the list.
      try
      {
         boolean bContainsConnection;
         
         for (iIndex = 0; iIndex < ALL_CONNECTION_COUNT; iIndex++)
         {
            // request connection
            // Request autocommit true since we are just reading data from the 
            // database
            con = m_connectionFactory.requestConnection(true);
         
            // We need to keep the connection so that the next iteration forces 
            // the pool to return us new connection. We will return them all in 
            // finally
            lstConnections.add(con);

            // Test if just requested connection is in list.
            bContainsConnection = containsConnection(lstConIdentifiers, con);
            
            if (this instanceof ProxoolDatabaseConnectionFactoryTestInternal)
            {
               assertFalse("Defect #1468646 behavior of Proxool has changed."
                           + " Review it.", bContainsConnection);
            }
            else
            {
               assertTrue("The pool should return existing connection again" +
                          " since it should be pooled.", 
                          bContainsConnection);
            }
         }
      }
      finally
      {
         for (iIndex = 0; (iIndex < ALL_CONNECTION_COUNT) 
             && (!lstConnections.isEmpty()); iIndex++)
         {
            // Return connection, we still remember it but it is already returned
            m_connectionFactory.returnConnection(
                (Connection)lstConnections.remove(0));
         }
      }
      
      assertTrue("Not all connections were returned.", lstConnections.isEmpty());
   }

   /**
    * Test requesting X+1 connections when the pool shouldn't grow (CAN GROW 
    * flag = FALSE or particular pool doesn't support it).
    * 
    * @throws Exception - an error has occurred during test
    */
   public void testRequestOneMoreCannotGrow(
   ) throws Exception
   {
      requestOneMore(false);
   }
   
   /**
    * Test requesting X+1 connections when the pool should be able to grow (CAN 
    * GROW flag = TRUE or particular pool supports it).
    * 
    * @throws Exception - an error has occurred during test
    */
   public void testRequestOneMoreCanGrow(
   ) throws Exception
   {
      requestOneMore(true);
   }

   /**
    * Method results if new connection is contained within the list of 
    * first time requested connections.
    * 
    * @param lstConnections - list of first time requested connections
    * @param newConnection - just new requested connection (requested second time)
    * 
    * @return true  - if newConnection is contained in the list
    *         false - if newConnection is not contained in the list
    * 
    * @throws OSSException - exception occurred during comparing 2 connections
    */
   protected abstract boolean containsConnection(
      List lstConnections,
      Connection newConnection
   ) throws OSSException;
   
   // Helper methods ///////////////////////////////////////////////////////////
   
   /**
    * Test for request X+1 connections when there is not possible to grow pool 
    * (can grow flag = false or particular pool doesn't support it)
    * 
    * @param bCanGrow - can the pool grow
    * @throws Exception - an error has occurred during test
    */
   private void requestOneMore(
      boolean bCanGrow
   ) throws Exception
   {
      Connection con = null;
      List       lstConnections = new ArrayList(ALL_CONNECTION_COUNT);

      try
      {
         int iIndex;
         
         // Request maximal number of connection in the pool
         for (iIndex = 0; iIndex < ALL_CONNECTION_COUNT; iIndex++)
         {
            // Request autocommit true since we are just reading data from the 
            // database
            con = m_connectionFactory.requestConnection(true);

            // Add connection it to the list, it will be used for returning 
            // these connections
            lstConnections.add(con);
         }

         // At this point we have requested maximal connections in the pool.
         // Now we try to request one more connection (MAX + 1).
         Connection cOneMoreConnection = null;
         long       startTime = 0;
         long       endTime = 0;
   
         try
         {
            // At this point we have requested maximal connections in the pool.
            // Now we try to request one more connection (MAX + 1).
            try
            {
               startTime = System.currentTimeMillis(); 
               // Request autocommit true since we are just reading data from 
               // the database
               cOneMoreConnection = m_connectionFactory.requestConnection(true);
   
               if (bCanGrow)
               {
                  if (this instanceof C3P0DatabaseConnectionFactoryTestInternal)
                  {
                     // As of version 0.9.1-pre6 the C3P0 doesn't support 
                     // growing and something has changed so review the code
                     fail("C3P0 \"cangrow\" behaviour has changed. Review it.");
                  }
                  else if (this instanceof XAPoolDatabaseConnectionFactoryTestInternal)
                  {
                     // As of version 1.5.0 patched the XAPool doesn't support 
                     // growing and something has changed so review the code
                     fail("XAPool \"cangrow\" behaviour has changed. Review it.");
                  }
                  else if (this instanceof ProxoolDatabaseConnectionFactoryTestInternal)
                  {
                     // As of version 0.9.0 RC2 the Proxool doesn't support 
                     // growing and something has changed so review the code
                     fail("Proxool \"cangrow\" behaviour has changed. Review it.");
                  }
                  else
                  {
                     assertNotNull("Either the pool doesn't implement growing"
                                   + " beyond maximal size or it failed to grow.", 
                                   cOneMoreConnection);
                  }
               }
               else 
               {
                  // If pool cannot grow, it should throw an exception but never 
                  // null
                  fail("Pool is setup to do not grow so another connections" +
                       " shouldn't be returned.");
               }
            }
            catch (OSSDatabaseAccessException daeExc)
            {
               if (bCanGrow)
               {
                  s_logger.log(Level.WARNING, 
                           "Either the pool doesn't implement growing" +
                           " beyond maximal size or failed to grow.", 
                           daeExc);
                  if (this instanceof C3P0DatabaseConnectionFactoryTestInternal)
                  {
                     // As of version 0.9.1-pre6 the C3P0 doesn't support 
                     // growing which is OK since thats how we documented it so
                     // the test should not fail
                  }
                  else if (this instanceof XAPoolDatabaseConnectionFactoryTestInternal)
                  {
                     // As of version 1.5.0 patched the XAPool doesn't support 
                     // growing which is OK since thats how we documented it so
                     // the test should not fail
                  }
                  else if (this instanceof ProxoolDatabaseConnectionFactoryTestInternal)
                  {
                     // As of version 0.9.0 RC2 the Proxool doesn't support 
                     // growing which is OK since thats how we documented it so
                     // the test should not fail
                  }
                  else
                  {
                     fail("Either the pool doesn't implement growing beyond"
                          + " maximal size or it failed to grow.");
                  }
               }
               else
               {
                  // If the pool cannot grow it is expected that it can throw 
                  // an exception
                  // If the pool cannot grow it should wait
                  // how long does pool wait
                  endTime = System.currentTimeMillis() - startTime;
                  
                  if (this instanceof ProxoolDatabaseConnectionFactoryTestInternal)
                  {
                     // As of version 0.9.0 RC2 the Proxool doesn't support 
                     // waiting when the pool is exhausted so test if the 
                     // behavior is consistent
                     assertTrue("Proxool \"cannotgrow\" behavior changed." 
                                + " Review it.", endTime < 100);
                  }
                  else
                  {
                     assertTrue("Either the pool doesn't implement pool waiting"
                                + " period when exhausted or the pool waited"
                                + " less that expected. Expected wait = " 
                                + POOL_WAIT_PERIOD + " and waited = " + endTime, 
                                endTime >= POOL_WAIT_PERIOD);
                  }
               }
            }
         }
         finally
         {
            m_connectionFactory.returnConnection(cOneMoreConnection);
         }
      }
      finally
      {      
         int iIndex;
         
         for (iIndex = 0; (iIndex < ALL_CONNECTION_COUNT) 
             && (!lstConnections.isEmpty()); iIndex++)
         {
            // Return connection, we still remember it but it is already returned
            m_connectionFactory.returnConnection(
                (Connection)lstConnections.remove(0));
         }
      }
      
      assertTrue("Not all connections were returned.", lstConnections.isEmpty());
   }

   /**
    * Method returns item that will be added to the list. The item should 
    * uniquely represent the connection or the underlying connection so that
    * it can be compared with different connections.
    * 
    * @param cActualConnection - actual connection 
    * @throws OSSInternalErrorException - an error has occurred
    * @return Object - object that will be added to the list
    */
   protected Object addItem(
      Connection cActualConnection
   )  throws OSSInternalErrorException
   {
      return cActualConnection;
   }
}
