/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.svcs.gls.config;

import vu.globe.rts.runtime.cfg.Settings;

/**
   <code>NodeProperties</code> defines a set of typed configuration
   properties. Java properties can override these properties. All properties
   defined here are specific to node implementations.
 
   @author Patrick Verkaik
   @author Egon Amade (various properties for the database added)
*/

// TODO: all properties should be read using Settings

public class NodeProperties
{
   // implementations
 
   /** Default AlgorithmFact implementation */
   public static final Class ALGO_DEF =
            vu.globe.svcs.gls.node.exec.algorithm.proto.AlgorithmFactImpl.class;
 
   /** Java property name of an AlgorithmFact implementation */
   public static final String ALGO_PROP = "global.potato.locator.algo";
 
   /** Default StoreAddressPolicy implementation */
   public static final Class STORE_POLICY_DEF =
                     vu.globe.svcs.gls.node.policy.StoreAddressPolicyImpl.class;
 
   /** Java property name of a StoreAddressPolicy implementation */
   public static final String STORE_POLICY_PROP =
                                 "global.potato.locator.policy.store";
 
   /** Default Databases implementation */
   public static final Class DATABASES_DEF = 
                  vu.globe.svcs.gls.node.contact.proto.DatabasesImpl.class;
 
   /** Java property name of a Databases implementation */
   public static final String DATABASES_PROP ="global.potato.locator.databases";
 
   /** Default RpcExecutor implementation */
   public static final Class RPCX_DEF =
                        vu.globe.svcs.gls.node.comm.rpcx.proto.NodeRpc.class;

   /** Java property name of an RpcExecutor implementation */
   public static final String RPCX_PROP = "global.potato.locator.rpcx.executor";

   /** Default value for 'tentative caching' property */ 
   public static final String TENTATIVE_CACHING_DEF = "true";

   /** Java property name of 'tentative caching' property */
   public static final String TENTATIVE_CACHING_PROP = "lsnode.tentative.caching";

   /** Default value for 'tentative contact record pool size' property */ 
   public static final String TENTATIVE_CR_POOL_SIZE_DEF = "500";

   /** Java property name of 'tentative contact record pool size' property */
   public static final String TENTATIVE_CR_POOL_SIZE_PROP = "lsndoe.tentative.crpoolsize";

   /** Default value for 'authoritative database type' property */ 
   public static final String AUTHORITATIVE_DBTYPE_DEF = "berkeleydb";

   /** Java property name of 'authoritative database type' property */
   public static final String AUTHORITATIVE_DBTYPE_PROP = "gids.schema.databaseBackend";
   
   /** Default value for 'authoritative disk writes' property */ 
   public static final String AUTHORITATIVE_DISKWRITES_DEF = "true";

   /** Java property name of 'authoritative synchronous writes' property */
   public static final String AUTHORITATIVE_DISKWRITES_PROP = "lsnode.authoritative.diskwrites";

   /** Default value for 'authoritative synchronous writes' property */ 
   public static final String AUTHORITATIVE_SYNCWRITES_DEF = "true";

   /** Java property name of 'authoritative synchronous writes' property */
   public static final String AUTHORITATIVE_SYNCWRITES_PROP = "lsnode.authoritative.syncwrites";

   /** Default value for 'authoritative database file name' property */ 
   public static final String AUTHORITATIVE_DB_FILENAME_DEF = "ls.contacts.db";

   /** Java property name of 'authoritative database file name' property */
   public static final String AUTHORITATIVE_DB_FILENAME_PROP = "lsnode.authoritative.db";

   /** Default value for 'check authoritative writes' property */ 
   public static final String AUTHORITATIVE_CHECKWRITES_DEF = "false";

   /** Java property name of 'check authoritative writes' property */
   public static final String AUTHORITATIVE_CHECKWRITES_PROP = "lsnode.authoritative.checkwrites";

   /** Default value for 'disable authoritative writes' property */ 
   public static final String AUTHORITATIVE_DISABLEWRITES_DEF = "false";

   /** Java property name of 'disable authoritative writes' property */
   public static final String AUTHORITATIVE_DISABLEWRITES_PROP = "lsnode.authoritative.disablewrites";

   /** Default value for 'rpc log disk writes' property */ 
   public static final String RPCLOG_DISKWRITES_DEF = "true";

   /** Java property name of 'rpc log disk writes' property */
   public static final String RPCLOG_DISKWRITES_PROP = "lsnode.rpclog.diskwrites";

   /** Default value for 'rpc log synchronous writes' property */ 
   public static final String RPCLOG_SYNCWRITES_DEF = "true";

   /** Java property name of 'rpc log synchronous writes' property */
   public static final String RPCLOG_SYNCWRITES_PROP = "lsnode.rpclog.syncwrites";

   /** Default value for 'rpc log file name' property */ 
   public static final String RPCLOG_FILENAME_DEF = "ls.rpclog.db";

   /** Java property name of 'rpc log file name' property */
   public static final String RPCLOG_FILENAME_PROP = "lsnode.rpclog";

   /** Default value for 'disable rpc log writes' property */
   public static final String RPCLOG_DISABLEWRITES_DEF = "false";

   /** Java property name of 'disable rpc log writes' property */
   public static final String RPCLOG_DISABLEWRITES_PROP = "lsnode.rpclog.disablewrites";
   
   /** Default value for 'check rpc log writes' property */ 
   public static final String RPCLOG_CHECKWRITES_DEF = "false";

   /** Java property name of 'check rpc log writes' property */
   public static final String RPCLOG_CHECKWRITES_PROP = "global.potato.locator.rpclog.checkwrites";

   /** Default value for 'statistics watcher interval' property. */ 
   public static final String STATS_INTERVAL_DEF = "-1";

   /** Java property name of 'statistics watcher interval' property */
   public static final String STATS_INTERVAL_PROP = "lsnode.statistics.interval";

   /** Default value for 'node lookup timeout' property (millis). */ 
   public static final String NODE_LOOKUP_TIMEOUT_DEF = "10000";

   /** Setting name of 'node lookup timeout' property */
   public static final String NODE_LOOKUP_TIMEOUT_PROP = 
     "gids.schema.lsLookupTimeout";

//   /** Java property name of 'node lookup timeout' property */
//public static final String NODE_LOOKUP_TIMEOUT_PROP = "global.potato.timeout.
//node.lookup";

   public static Class algoClass ()
   {
      Class cl = ConfigLib.createPropClass (ALGO_PROP);
      return cl == null ? ALGO_DEF : cl;
   }

   public static Class storePolicyClass ()
   {
      Class cl = ConfigLib.createPropClass (STORE_POLICY_PROP);
      return cl == null ? STORE_POLICY_DEF : cl;
   }

   public static Class databasesClass ()
   {
      Class cl = ConfigLib.createPropClass (DATABASES_PROP);
      return cl == null ? DATABASES_DEF : cl;
   }

   public static Class rpcExecutorClass ()
   {
      Class cl = ConfigLib.createPropClass (RPCX_PROP);
      return cl == null ? RPCX_DEF : cl;
   }

   /**
      Reads the 'tentative caching' property.
   */
   public static boolean tentativeCaching ()
   {
      String str = System.getProperty (TENTATIVE_CACHING_PROP,
                                 TENTATIVE_CACHING_DEF);
      return Boolean.valueOf (str).booleanValue ();
   }


   /**
      Reads the 'tentative contact record pool size' property.
   */
   public static int tentativeContactRecordPoolSize()
   {
      String str = System.getProperty (TENTATIVE_CR_POOL_SIZE_PROP,
                                 TENTATIVE_CR_POOL_SIZE_DEF);
      return Integer.valueOf (str).intValue ();
   }


   /**
      Reads the 'authoritative database type' property.
   */
   public static String authoritativeDatabaseType()
   {
      String str = Settings.getInstance().getValue(AUTHORITATIVE_DBTYPE_PROP);
      if (str == null) {
	str = AUTHORITATIVE_DBTYPE_DEF;
      }
      // dbtype is a setting that must be retrieved from Settings
      //String str = System.getProperty (AUTHORITATIVE_DBTYPE_PROP,
      //                           AUTHORITATIVE_DBTYPE_DEF);
      return str;
   }

  
   /**
      Reads the 'authoritative disk writes' property.
   */
   public static boolean authoritativeDiskWrites ()
   {
      String str = System.getProperty (AUTHORITATIVE_DISKWRITES_PROP,
                                 AUTHORITATIVE_DISKWRITES_DEF);
      return Boolean.valueOf (str).booleanValue ();
   }

   /**
      Reads the 'authoritative synchronous writes' property.
   */
   public static boolean authoritativeSyncWrites ()
   {
      String str = System.getProperty (AUTHORITATIVE_SYNCWRITES_PROP,
                                 AUTHORITATIVE_SYNCWRITES_DEF);
      return Boolean.valueOf (str).booleanValue ();
   }


   /**
      Reads the 'authoritative database file name' property.
   */
   public static String authoritativeDBFilename ()
   {
      String str = System.getProperty (AUTHORITATIVE_DB_FILENAME_PROP,
                                 AUTHORITATIVE_DB_FILENAME_DEF);
      return str;
   }


   /**
      Reads the 'check authoritative writes' property.
   */
   public static boolean authoritativeWritesCheck ()
   {
      String str = System.getProperty (AUTHORITATIVE_CHECKWRITES_PROP,
                                 AUTHORITATIVE_CHECKWRITES_DEF);
      return Boolean.valueOf (str).booleanValue ();
   }

   /**
      Reads the 'disable authoritative writes' property.
   */
   public static boolean authoritativeDisableWrites ()
   {
      String str = System.getProperty (AUTHORITATIVE_DISABLEWRITES_PROP,
                                 AUTHORITATIVE_DISABLEWRITES_DEF);
      return Boolean.valueOf (str).booleanValue ();
   }
   
   /**
      Reads the 'rpc log disk writes' property.
   */
   public static boolean rpclogDiskWrites ()
   {
      String str = System.getProperty (RPCLOG_DISKWRITES_PROP,
                                 RPCLOG_DISKWRITES_DEF);
      return Boolean.valueOf (str).booleanValue ();
   }

   /**
      Reads the 'rpc log synchronous writes' property.
   */
   public static boolean rpclogSyncWrites ()
   {
      String str = System.getProperty (RPCLOG_SYNCWRITES_PROP,
                                 RPCLOG_SYNCWRITES_DEF);
      return Boolean.valueOf (str).booleanValue ();
   }

   /**
      Reads the 'rpc log disable writes' property.
   */
   public static boolean rpclogDisableWrites ()
   {
      String str = System.getProperty (RPCLOG_DISABLEWRITES_PROP,
                                 RPCLOG_DISABLEWRITES_DEF);
      return Boolean.valueOf (str).booleanValue ();
   }
   
   /**
      Reads the 'check rpclog writes' property.
   */
   public static boolean rpclogWritesCheck ()
   {
      String str = System.getProperty (RPCLOG_CHECKWRITES_PROP,
                                 RPCLOG_CHECKWRITES_DEF);
      return Boolean.valueOf (str).booleanValue ();
   }

   /**
      Reads the 'rpc log file name' property.
   */
   public static String rpclogFilename()
   {
      String str = System.getProperty (RPCLOG_FILENAME_PROP,
                                 RPCLOG_FILENAME_DEF);
      return str;
   }

   /**
      Reads the 'statistics watcher interval' property. This is the rate at
      which statistics are dumped by the node.

      @return 	the time in milliseconds between successive dumps of
      		statistics, -1 if no statistics should be dumped at all

      @see StatisticsWatcher
      @see Statistics
   */
   public static int statisticsWatcherInterval()
   {
      String str = System.getProperty (STATS_INTERVAL_PROP,
                                 STATS_INTERVAL_DEF);
      return Integer.valueOf (str).intValue ();
   }

   /**
      Reads the 'node lookup timeout' property. This is the number of
      milliseconds a look-up issued by an LS node is allowed to take. Returns
      RpcRequest.TIMEOUT_NONE if no timout should be set.
   */
   public static long nodeLookupTimeout()
   {
      String str = Settings.getInstance().getValue(NODE_LOOKUP_TIMEOUT_PROP);
      if (str == null) {
	str = NODE_LOOKUP_TIMEOUT_DEF;
      }
      return Long.valueOf (str).longValue ();
   }
}
