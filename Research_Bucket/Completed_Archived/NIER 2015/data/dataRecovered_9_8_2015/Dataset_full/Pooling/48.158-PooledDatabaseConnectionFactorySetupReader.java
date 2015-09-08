/*
 * Copyright (c) 2003 - 2009 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: PooledDatabaseConnectionFactorySetupReader.java,v 1.1 2009/04/22 05:05:00 bastafidli Exp $
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

package org.opensubsystems.core.persist.db.connectionpool.impl;

import java.util.HashMap;
import java.util.Map;

import org.opensubsystems.core.persist.db.impl.DatabaseConnectionFactorySetupReader;
import org.opensubsystems.core.util.MultiSetupReader;
import org.opensubsystems.core.util.SetupReader;

/**
 * Class that reads setup for database connection pools from properties. Each 
 * pool has unique name. This class will find all properties for that name or 
 * will use the default values if property with such name is not present.
 * 
 * Each property name consist from three parts. 
 * 1. base path, for example oss.datasource.pool
 * 2. reader name, for example mypool
 * 3. parameter name, for example initialsize
 *  
 * Property name looks like <basepath>.<readername>.<parametername> for example
 * oss.datasource.mydatasource.pool.initialsize
 * 
 * The DBCP configuration is available at 
 * http://jakarta.apache.org/commons/dbcp/configuration.html
 * The XAPool configuration is available at
 * file:///e:/Development/OpenSubsystems/external/xapool/jdoc/index.html
 *    see classes StandardXADataSource, StandardXAPoolDataSource
 * The C3P0 configuration is available at 
 * http://www.mchange.com/projects/c3p0/index.html#appendix_a
 * The Proxool configuraion is available at 
 * http://proxool.sourceforge.net/properties.html
 * 
 * @version $Id: PooledDatabaseConnectionFactorySetupReader.java,v 1.1 2009/04/22 05:05:00 bastafidli Exp $
 * @author Miro Halas
 * @code.reviewer Miro Halas
 * @code.reviewed 1.2 2006/04/27 13:31:23 bastafidli
 */
public class PooledDatabaseConnectionFactorySetupReader extends MultiSetupReader
{
   // Configuration settings ///////////////////////////////////////////////////
   
   /**
    * Base path for all properties. Use the same base path as the regular data 
    * source since these properties just augment the data source properties to
    * allow pooling of connections to that datasource. 
    */
   public static final String DATABASE_POOL_BASE_PATH 
      = DatabaseConnectionFactorySetupReader.DATABASE_CONNECTION_BASE_PATH;

   // Configuration parameters names 
   
   /**
    * Initial size of the connection pool. How many connections are created
    * in the pool when the pool is started.
    * 
    * Connection pool specific terminology:
    * DBCP:    initialSize - if 0 - no initial connections created
    * XAPool:  StandardXAPoolDataSource(iInitialPoolSize)
    * C3P0:    initialPoolSize
    * Proxool: No support 
    */
   public static final String DBPOOL_INITIAL_SIZE = "pool.initialsize";

   /**
    * Minimal size of the connection pool. Number of connections in the pool 
    * should not fall under this threshold regardless if they are used or not. 
    * This is not the same as the initial/optimal size.
    * 
    * Connection pool specific terminology:
    * DBCP:    minIdle - if 0 - pool can become empty
    * XAPool:  StandardXAPoolDataSource.setMinSize
    * C3P0:    minPoolSize 
    * Proxool: minimum-connection-count
    */
   public static final String DBPOOL_MIN_SIZE = "pool.minsize";

   /**
    * Maximal size of the connection pool. How many connections can be taken out
    * of the pool or can exist in the pool idle.
    * 
    * Connection pool specific terminology:
    * DBCP:    maxActive, maxIdle - if 0 - no limit
    * XAPool:  StandardXAPoolDataSource.setMaxSize
    * C3P0:    maxPoolSize 
    * Proxool: maximum-connection-count    
    */
   public static final String DBPOOL_MAX_SIZE = "pool.maxsize";
   
   /**
    * Can the connection pool grow above maximal size or should it block. This 
    * can be useful if we want to have soft max limit but still allow to satisfy
    * request when load increases and then maybe in the future readjust the 
    * settings. If we do not allow growing, then the caller will be blocked until
    * connection becomes available.
    * 
    * Connection pool specific terminology:
    * DBCP:    GenericObjectPool(...whenExhaustedAction...)
    * XAPool:  No support
    * C3P0:    No support 
    * Proxool: No support 
    */
   public static final String DBPOOL_CAN_GROW = "pool.cangrow";

   /**
    * How long in millisecons to wait for a connection when the pool reaches 
    * maximal size. Time is in milliseconds.
    * 
    * Connection pool specific terminology:
    * DBCP:    maxWait - if -1 - wait indefinitely
    * XAPool:  StandardXAPoolDataSource.setDeadLockMaxWait
    * C3P0:    checkoutTimeout - if 0 - wait indefinitely
    * Proxool: No support
    */
   public static final String DBPOOL_WAIT_PERIOD = "pool.waitperiod";

   /**
    * How long in milliseconds to wait until the pool tries to acquire another 
    * connection, if getting connection from the pool fails. For example in case 
    * of C3P0 even for some other reason than the pool is exhausted, such as 
    * temporary database outage. 
    * 
    * Connection pool specific terminology:
    * DBCP:    No support
    * XAPool:  StandardXAPoolDataSource.setDeadLockRetryWait
    * C3P0:    acquireRetryDelay 
    * Proxool: No support
    * TODO: Feature: Implement this for pools that do not support it since this  
    *                should be easy to implement.
    */
   public static final String DBPOOL_RETRY_PERIOD = "pool.retryperiod";

   /**
    * How many times to try to acquire another connection if getting connection 
    * from the pool fails. For example in case of C3P0 even for some other reason 
    * than the pool is exhausted, such as temporary database outage.  
    * 
    * Connection pool specific terminology:
    * DBCP:    No support
    * XAPool:  No support
    * C3P0:    acquireRetryAttempts - if less or equal to 0 - try indefinitely 
    * Proxool: No support
    * TODO: Feature: Implement this for pools that do not support it since this  
    *                should be easy to implement.
    */
   public static final String DBPOOL_RETRY_COUNT = "pool.retrycount";

   /**
    * Should the pool validate connection immediately before it is borrowed 
    * from the pool. This is quite expensive to do by default and often it is
    * better to let pool check on idle connection if such option is available.
    * 
    * Connection pool specific terminology:
    * DBCP:    testOnBorrow
    * XAPool:  No support
    * C3P0:    testConnectionOnCheckout 
    * Proxool: test-before-use
    */
   public static final String DBPOOL_VALIDATE_BORROW = "pool.validate.borrow";

   /**
    * Should the pool validate connection when it is returned to the pool.
    * 
    * Connection pool specific terminology:
    * DBCP:    testOnReturn
    * XAPool:  No support
    * C3P0:    testConnectionOnCheckin  
    * Proxool: test-after-use
    */
   public static final String DBPOOL_VALIDATE_RETURN = "pool.validate.return";

   /**
    * Should the pool validate connection when it is idle. This is the most
    * efficient way of connection testing and if you do decide to test connections
    * you may consider doing so while they are idle.
    * 
    * Connection pool specific terminology:
    * DBCP:    testWhileIdle
    * XAPool:  No support
    * C3P0:    just set value for idleConnectionTestPeriod  
    * Proxool: Just set value for idlecheckperiod
    */
   public static final String DBPOOL_VALIDATE_IDLE = "pool.validate.idle";

   /**
    * How thoroughly to validate the objects in the pool. Since this is the only
    * way provided by XAPool to configure connection testing, we provide this
    * attribute, even though it has no meaning for other pools.
    * 
    * Connection pool specific terminology:
    * DBCP:    No support
    * XAPool:  StandardXAPoolDataSource.setCheckLevelObject
    * C3P0:    No support  
    * Proxool: No support
    * 
    * From XAPool JavaDoc:
    * 0 = no special checking 
    * 1 = just a check on an object 
    * 2 = test the object 
    * 3 = just a check on an object (for all the objects) 
    * 4 = test the object (for all the objects)
    */
   public static final String DBPOOL_CHECK_LEVEL = "pool.validate.level";         
   
   /**
    * How often in milliseconds to validate idle connections. Validating idles 
    * connections is the most efficient way of validating connections since it 
    * doesn't affect requesting of returning of connections and therefore 
    * performance of the application. Time is in milliseconds (even though some 
    * pools expect it in seconds, it will be converted from milliseconds as 
    * needed). Alternative is to set idlechecksize if the connection pool 
    * supports it.
    * 
    * Connection pool specific terminology:
    * DBCP:    timeBetweenEvictionRunsMillis - if less or equal to 0 - do not check
    * XAPool:  No support
    * C3P0:    idleConnectionTestPeriod - if less or equal to 0 - do not check
    * Proxool: house-keeping-sleep-time
    */
   public static final String DBPOOL_IDLE_CHECK_PERIOD = "pool.idlecheckperiod";   

   /**
    * How many of the idle connections to test during one test run. This is
    * an alternative of idlecheckperiod.
    * 
    * Connection pool specific terminology:
    * DBCP:    numTestsPerEvictionRun
    * XAPool:  No support
    * C3P0:    No support
    * Proxool: No support
    */
   public static final String DBPOOL_IDLE_CHECK_SIZE = "pool.idlechecksize";   

   /**
    * How long in milliseconds can connection sit in the pool before it is 
    * considered idle. Time is in milliseconds (even though some pools expect it 
    * in seconds, it will be converted from milliseconds as needed). 
    * 
    * Connection pool specific terminology:
    * DBCP:    minEvictableIdleTimeMillis
    * XAPool:  No support
    * C3P0:    maxIdleTime - 0 means connections will never expire
    * Proxool: No support
    */
   public static final String DBPOOL_IDLE_PERIOD = "pool.idleperiod";      
   
   /**
    * Should the connection pool cache prepared statements and if so how many.
    * 
    * Connection pool specific terminology:
    * DBCP:    maxOpenPreparedStatements - if 0 - cache and do not impose any limit
    *                                    - if less than 0 - do no cache 
    * XAPool:  StandardXADataSource.setPreparedStmtCacheSize - if less or equal 
    *                                                          to 0 - do not cache
    * C3P0:    maxStatements - if less or equal to 0 - do not cache
    * Proxool: No support
    */
   public static final String DBPOOL_PREPSTATEMENT_CACHE_SIZE = "pool.pstmtcachesize";         
   
   // Configuration default values
   
   /**
    * Initial size of the connection pool. Make this 0 since if the database
    * doesn't exist or is not started we do not want to create unnecessary error
    * conditions due to creation of initial connections.
    */
   public static final int DBPOOL_INITIAL_SIZE_DEFAULT = 0;

   /**
    * Minimal size of the connection pool. Make this 0 since if the database
    * doesn't exist or is not started we do not want to create unnecessary error
    * conditions due to creation of initial connections.
    */
   public static final int DBPOOL_MIN_SIZE_DEFAULT = 0;

   /**
    * Maximal size of the connection pool. This is reasonable amount for most
    * not high volume applications.
    */
   public static final int DBPOOL_MAX_SIZE_DEFAULT = 20;
   
   /**
    * Can the connection pool grow above maximal size or should it block.
    */
   public static final boolean DBPOOL_CAN_GROW_DEFAULT = true;

   /**
    * If the pool should block when it reaches maximal size, how long can wait
    * at most for connection.
    */
   public static final long DBPOOL_WAIT_PERIOD_DEFAULT = 10000L; // 10 seconds

   /**
    * How long to wait until the pool tries to acquire another connection, if 
    * getting connection from the pool fails. For example in case of C3P0 even 
    * for some other reason than the pool is exhaused, such as temporary database 
    * outage. Time is in milliseconds.
    */
   public static final long DBPOOL_RETRY_PERIOD_DEFAULT = 2000L; // 20 seconds

   /**
    * How many times to try to acquire another connection if getting connection 
    * from the pool fails. For example in case of C3P0 even for some other reason 
    * than the pool is exhaused, such as temporary database outage.  
    */
   public static final long DBPOOL_RETRY_COUNT_DEFAULT = 3; // 3 times

   /**
    * Should the pool validate connection immmediately before it is borrowed 
    * from the pool. This is quite expensive to do by default and often it is
    * better to let pool check on idle connection if such option is available.
    */
   public static final boolean DBPOOL_VALIDATE_BORROW_DEFAULT = false; // no validation

   /**
    * Should the pool validate connection when it is returned to the pool.
    */
   public static final boolean DBPOOL_VALIDATE_RETURN_DEFAULT = false; // no validation

   /**
    * Should the pool validate connection when it is idle. This is the most
    * efficient way of conection testing and if you do decide to test connections
    * you may consider doing so while they are idle.
    */
   public static final boolean DBPOOL_VALIDATE_IDLE_DEFAULT = false; // no validation

   /**
    * How thoroughly to validate the objects in the pool. Since this is the only
    * way provided by XAPool to configure connection testing, we provide this
    * attribute, even though it has no meaning for other pools.
    * 
    * From XAPool JavaDoc:
    * 0 = no special checking 
    * 1 = just a check on an object 
    * 2 = test the object 
    * 3 = just a check on an object (for all the objects) 
    * 4 = test the object (for all the objects)
    */
   public static final int DBPOOL_CHECK_LEVEL_DEFAULT = 0; // no validation         
   
   /**
    * How often to validate idle connections. Validating idles connections is 
    * the most efficient way of validating connections since it doesn't affect
    * requesting of returning of connections and therefore performance of the
    * application. Time is in milliseconds (even though some pools expect it in 
    * seconds, it will be converted from milliseconds as needed).
    */
   public static final long DBPOOL_IDLE_CHECK_PERIOD_DEFAULT = 0; // do not check   

   /**
    * How much of the idle connections to test during one test run.
    */
   public static final int DBPOOL_IDLE_CHECK_SIZE_DEFAULT = 0; // do not check   

   /**
    * How long can connection sit in the pool before it is considered idle. Time 
    * is in milliseconds (even though some pools expect it in seconds, it will be 
    * converted from milliseconds as needed).
    */
   public static final long DBPOOL_IDLE_PERIOD_DEFAULT = 0; // never idle      
   
   /**
    * Should the connection pool cache prepared statements and if so how many.
    * 
    * In XAPool 1.4.1 0 triggers NullPointerException in XAPool therefore 
    * we have patched StandardConnectionHandle.java:145 in 1.4.1 ourselves. This 
    * was fixed in subsequent releases of XAPool.
    */
   public static final int DBPOOL_PREPSTATEMENT_CACHE_SIZE_DEFAULT = -1; // do not cache         

   // Cached values ////////////////////////////////////////////////////////////

   /**
   /**
    * Map with all registered parameters. The key is String parameter name and 
    * the value is an a ThreeObjectStruct where the element 
    * 1. is Integer constant (one of the PARAMETER_TYPE_XXX constants) 
    *    representing the type. If this map is null of empty, the constructor 
    *    will invoke the registerParameters method to register the parameters 
    *    that will inserted to this map. This allows you to pass in static 
    *    variable that can be shared between all instances.
    * 2. is Object representing the default value. This can be static variable 
    *    that can be shared between all instances.
    * 3. is String representing user friendly name of the property
    */
   protected  static Map s_mpRegisteredParameters = new HashMap();

   // Constructor //////////////////////////////////////////////////////////////
   
   /**
    * Constructor.
    * 
    * @param strDatabaseIdentification - name of reader usually representing the 
    *                                    dbms for which to read the settings. 
    *                                    This can represent different DBMS types 
    *                                    (e.g. Oracle, DB2, etc.) or different 
    *                                    groups of settings for the same database 
    *                                    (e.g. Oracle Production, Oracle 
    *                                    Development, etc.).
    * @param strReaderName - name of reader usually representing the logical 
    *                     database name used in the application, for which to 
    *                     read the settings
    */
   public PooledDatabaseConnectionFactorySetupReader(
      String strReaderName,
      String strDatabaseIdentification
   )
   {
      // We can pass the static variables here since once the types are 
      // registered they are the same for all instances so it is enough to 
      // register them only once
      super(DATABASE_POOL_BASE_PATH, 
            DatabaseConnectionFactorySetupReader.createReaderNames(
                        strReaderName, strDatabaseIdentification), 
            s_mpRegisteredParameters);
   }

   // Helper methods //////////////////////////////////////////////////////////
   
   /**
    * {@inheritDoc}
    */
   protected void registerParameters(
   )
   {
      registerParameter(DBPOOL_INITIAL_SIZE,
                        SetupReader.PARAMETER_TYPE_INTEGER_OBJ,
                        Integer.toString(DBPOOL_INITIAL_SIZE_DEFAULT),
                        "Initial size of the connection pool");

      registerParameter(DBPOOL_MIN_SIZE,
                        SetupReader.PARAMETER_TYPE_INTEGER_OBJ,
                        Integer.toString(DBPOOL_MIN_SIZE_DEFAULT),
                        "Minimal size of the connection pool");

      registerParameter(DBPOOL_MAX_SIZE,
                        SetupReader.PARAMETER_TYPE_INTEGER_OBJ,
                        Integer.toString(DBPOOL_MAX_SIZE_DEFAULT),
                        "Maximal size of the connection pool");

      registerParameter(DBPOOL_CAN_GROW,
                        SetupReader.PARAMETER_TYPE_BOOLEAN_OBJ,
                        Boolean.toString(DBPOOL_CAN_GROW_DEFAULT),
                        "Can the connection pool grow above maximal size");

      registerParameter(DBPOOL_WAIT_PERIOD,
                        SetupReader.PARAMETER_TYPE_LONG_OBJ,
                        Long.toString(DBPOOL_WAIT_PERIOD_DEFAULT),
                        "How long in milliseconds to wait for a connection when"
                        + " the pool reaches maximal size");

      registerParameter(DBPOOL_RETRY_PERIOD,
                        SetupReader.PARAMETER_TYPE_LONG_OBJ,
                        Long.toString(DBPOOL_RETRY_PERIOD_DEFAULT),
                        "How long in milliseconds to wait until the pool tries"
                        + " to acquire another connection, if getting connection"
                        + " from the pool fails");

      registerParameter(DBPOOL_RETRY_COUNT,
                        SetupReader.PARAMETER_TYPE_INTEGER_OBJ,
                        Long.toString(DBPOOL_RETRY_COUNT_DEFAULT),
                        "How many times to try to acquire another connection if"
                        + " getting connection from the pool fails");

      registerParameter(DBPOOL_VALIDATE_BORROW,
                        SetupReader.PARAMETER_TYPE_BOOLEAN_OBJ,
                        Boolean.toString(DBPOOL_VALIDATE_BORROW_DEFAULT),
                        "Should the pool validate connection immediately before"
                        + " it is borrowed from the pool");

      registerParameter(DBPOOL_VALIDATE_RETURN,
                        SetupReader.PARAMETER_TYPE_BOOLEAN_OBJ,
                        Boolean.toString(DBPOOL_VALIDATE_RETURN_DEFAULT),
                        "Should the pool validate connection when it is"
                        + " returned to the pool");

      registerParameter(DBPOOL_VALIDATE_IDLE,
                        SetupReader.PARAMETER_TYPE_BOOLEAN_OBJ,
                        Boolean.toString(DBPOOL_VALIDATE_IDLE_DEFAULT),
                        "Should the pool validate connection when it is idle");

      registerParameter(DBPOOL_CHECK_LEVEL,
                        SetupReader.PARAMETER_TYPE_INTEGER_OBJ,
                        Integer.toString(DBPOOL_CHECK_LEVEL_DEFAULT),
                        "How thoroughly to validate the objects in the pool");
   
      registerParameter(DBPOOL_IDLE_CHECK_PERIOD,
                        SetupReader.PARAMETER_TYPE_LONG_OBJ,
                        Long.toString(DBPOOL_IDLE_CHECK_PERIOD_DEFAULT),
                        "How often in milliseconds to validate idle connections");

      registerParameter(DBPOOL_IDLE_CHECK_SIZE,
                        SetupReader.PARAMETER_TYPE_INTEGER_OBJ,
                        Integer.toString(DBPOOL_IDLE_CHECK_SIZE_DEFAULT),
                        "How many of the idle connections to test during one"
                        + " test run");

      registerParameter(DBPOOL_IDLE_PERIOD,
                        SetupReader.PARAMETER_TYPE_LONG_OBJ,
                        Long.toString(DBPOOL_IDLE_PERIOD_DEFAULT),
                        "How long in milliseconds can connection sit in the"
                        + " pool before it is considered idle");

      registerParameter(DBPOOL_PREPSTATEMENT_CACHE_SIZE,
                        SetupReader.PARAMETER_TYPE_INTEGER_OBJ,
                        Integer.toString(DBPOOL_PREPSTATEMENT_CACHE_SIZE_DEFAULT),
                        "Should the connection pool cache prepared statements"
                        + " and if so how many");
   }
}
