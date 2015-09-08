/*
 * Copyright (c) 2008  2009 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: DatabaseConnectionFactorySetupReader.java,v 1.1 2009/04/22 05:40:47 bastafidli Exp $
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

package org.opensubsystems.core.persist.db.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.opensubsystems.core.error.OSSConfigException;
import org.opensubsystems.core.util.Config;
import org.opensubsystems.core.util.MultiSetupReader;
import org.opensubsystems.core.util.PropertyUtils;
import org.opensubsystems.core.util.SetupReader;

/**
 * Class that reads setup for database connections from properties. Each 
 * database has unique name. This class will find all properties for that name 
 * or will use the default values if property with such name is not present.
 * 
 * Each property name consist from three parts. 
 * 1. base path, for example oss.datasource.
 * 2. reader name, for example mydatabase
 * 3. parameter name, for example url
 *  
 * Property name looks like <basepath>.<readername>.<parametername> for example
 * oss.datasource.mydatasource.url
 * 
 * In order to allow support for multiple dbms and allow settings for multiple
 * dbms to be defined at the same time, the reader name is qualified with the 
 * dbms identification to find the best possible settings. Assuming the above
 * values and the oracle dbms, this setup reader will try to find value for
 * settings in this order
 * 1. oss.datasource.oracle.mydatasource.url 
 * 2. oss.datasource.mydatasource.url
 * 3. oss.datasource.oracle.url
 * 4. oss.datasource.url
 * 
 * @version $Id: DatabaseConnectionFactorySetupReader.java,v 1.1 2009/04/22 05:40:47 bastafidli Exp $
 * @author Miro Halas
 * @code.reviewer Miro Halas
 * @code.reviewed 1.2 2008/11/14 00:03:38 bastafidli
 */
public class DatabaseConnectionFactorySetupReader extends MultiSetupReader
{
   // Configuration settings ///////////////////////////////////////////////////
   
   /**
    * Base path for all properties
    */
   public static final String DATABASE_CONNECTION_BASE_PATH = "oss.datasource";

   // Configuration parameters names 
   
   /** 
    * Name of the property containing driver to use to connect to the database, 
    * e.g. "oracle.jdbc.driver.OracleDriver"
    */   
   public static final String DATABASE_DRIVER = "driver";

   /** 
    * Name of the property containing URL to connect to the database, 
    * e.g. "jdbc:oracle:thin:@server:1521:database"
    */   
   public static final String DATABASE_URL = "url";

   /** 
    * Name of the property containing user name to connect to the database, 
    * e.g. "bastafidli"
    */   
   public static final String DATABASE_USER = "user";

   /** 
    * Name of the property containing user password to connect to the database, 
    * e.g. "password"
    */   
   public static final String DATABASE_PASSWORD = "password";

   /** 
    * Name of the property containing user name to connect to the database, 
    * as an administrator e.g. "bastafidli"
    */   
   public static final String DATABASE_ADMIN_USER = "adminuser";

   /** 
    * Name of the property containing user password to connect to the database, 
    * as administrator e.g. "password"
    */   
   public static final String DATABASE_ADMIN_PASSWORD = "adminpassword";

   /**
    * Default transaction isolation level. Settings are commited, uncommited, 
    * repeatable, serializable and none. None means to do not set transaction
    * isolation because dbms may for example not support it.
    * 
    * Connection pool specific terminology:
    * DBCP:    defaultTransactionIsolation
    * XAPool:  StandardXADataSource.setTransactionIsolation
    * C3P0:    No support
    * Proxool: No support
    */
   public static final String DATABASE_TRANSACTION_ISOLATION = "transaction.isolation";         
   
   // Configuration default values
   
   /**
    * Default database driver to use is HSQLDB driver since HSQLDB since it is 
    * just a library that can be shipped with application and doesn't require
    * any  specific installation or configuration.
    */
   protected static String DATABASE_DRIVER_DEFAULT = "org.hsqldb.jdbcDriver";

   /**
    * URL to connect to connect to the default database.
    * Note: This cannot be jdbc:hsqldb:OSS because HSQLDB produces file 
    * OSS.properties which in some scenarios leads to conflict with the default
    * configuration file oss.properties and failing code.
    */
   protected static String DATABASE_URL_DEFAULT = "jdbc:hsqldb:data/hsqldb/OSSDATA";

   /**
    * Default user name to use to connect to the database.
    */
   protected static String DATABASE_USER_DEFAULT = "basta";

   /**
    * Default password to use to connect to the database.
    */
   protected static String DATABASE_PASSWORD_DEFAULT = "fidli";

   /**
    * Default administration user name to use to connect to the database when 
    * it needs to be configured.
    */
   protected static String DATABASE_ADMIN_USER_DEFAULT = "sa";

   /**
    * Default administration password to use to connect to the database when 
    * it needs to be configured.
    */
   protected static String DATABASE_ADMIN_PASSWORD_DEFAULT = "";

   /**
    * Default transaction isolation level. Settings are committed, uncommitted, 
    * repeatable, serializable.
    */
   public static final String DATABASE_TRANSACTION_ISOLATION_DEFAULT = "serializable";         
   
   // Constants ////////////////////////////////////////////////////////////////
   
   /**
    * This string is used to identify JDBC spy driver. If this driver is
    * setup to be used, then we need to lookup real driver as well.
    */   
   public static final String SPY_DRIVER_IDENTIFICATION = "p6spy";
   
   /**
    * Name of the configuration file for the spy driver.
    */
   public static final String SPY_CONFIG_FILE_NAME = "spy.properties";
   
   /**
    * Name of the property which contains identification of real driver user
    * by the spy driver.
    */
   public static final String SPY_REALDRIVER = "realdriver";
   
   // Cached values ////////////////////////////////////////////////////////////

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
   public DatabaseConnectionFactorySetupReader(
      String strReaderName,
      String strDatabaseIdentification
   )
   {
      // We can pass the static variables here since once the types are 
      // registered they are the same for all instances so it is enough to 
      // register them only once
      super(DATABASE_CONNECTION_BASE_PATH, 
            createReaderNames(strReaderName, strDatabaseIdentification), 
            s_mpRegisteredParameters);
   }

   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * Test if the database driver is a real database driver or if it is a spy
    * database driver which just proxies requests to a real database driver.
    */
   public boolean isDatabaseDriverReal(
   )
   {
      String  strDatabaseDriver;
      boolean bReturn;
      
      strDatabaseDriver = getStringParameterValue(
         DatabaseConnectionFactorySetupReader.DATABASE_DRIVER); 

      bReturn = (strDatabaseDriver.indexOf(SPY_DRIVER_IDENTIFICATION) != -1);

      return bReturn;
   }
   
   /**
    * If there is a spy driver setup as a database driver, return a real 
    * database driver which will be used by the spy driver to actually access
    * the database. If there is no spy driver, return the regular driver.
    * 
    * @return String - name of the JDBC driver that really connects to the database
    * @throws OSSConfigException
    */
   public String getRealDatabaseDriver(
   ) throws OSSConfigException
   {
      String strRealDatabaseDriver;
      
      if (isDatabaseDriverReal())
      {
         Config     spyConfig = new Config(SPY_CONFIG_FILE_NAME);
         Properties prpSettings = spyConfig.getProperties();
      
         strRealDatabaseDriver = PropertyUtils.getStringProperty(
                                    prpSettings, SPY_REALDRIVER, 
                                    "Real JDBC driver class name");
      }
      else
      {
         strRealDatabaseDriver = getStringParameterValue(
            DatabaseConnectionFactorySetupReader.DATABASE_DRIVER); 
      }
      
      return strRealDatabaseDriver;
   }
   
   /**
    * Create list of reader names in priority order that will be used by this
    * reader to get values for properties. The priority logix is described in 
    * the header of the class.
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
   public static List createReaderNames(
      String strReaderName,
      String strDatabaseIdentification
   )
   {
      List lstReaderNames = new ArrayList(3);
      
      // Do not change the case of the reader name since developers control this
      // and may prefer certain case. Do change the database identifier since
      // that is controlled in general by this code and it will make this uniform  
      // wit the rest of the properties
      // strReaderName = strReaderName.toLowerCase();
      strDatabaseIdentification = strDatabaseIdentification.toLowerCase();
      lstReaderNames.add(strDatabaseIdentification + "." + strReaderName);
      lstReaderNames.add(strReaderName);
      lstReaderNames.add(strDatabaseIdentification);
      
      return lstReaderNames;
   }
   
   // Helper methods ///////////////////////////////////////////////////////////
   
   /**
    * {@inheritDoc}
    */
   protected void registerParameters(
   )
   {
      registerParameter(DATABASE_DRIVER, 
                        SetupReader.PARAMETER_TYPE_STRING_OBJ,
                        DATABASE_DRIVER_DEFAULT,
                        "JDBC driver class name");
      
      registerParameter(DATABASE_URL,
                        SetupReader.PARAMETER_TYPE_STRING_OBJ,
                        DATABASE_URL_DEFAULT,
                        "Database URL");
      
      registerParameter(DATABASE_USER, 
                        SetupReader.PARAMETER_TYPE_STRING_OBJ,
                        DATABASE_USER_DEFAULT,
                        "User name of the database user to connect to the database");
      
      registerParameter(DATABASE_PASSWORD, 
                        SetupReader.PARAMETER_TYPE_STRING_OBJ,
                        DATABASE_PASSWORD_DEFAULT,
                        "Password of the database user to connect to the database");

      registerParameter(DATABASE_ADMIN_USER,
                        SetupReader.PARAMETER_TYPE_STRING_OBJ,
                        DATABASE_ADMIN_USER_DEFAULT,
                        "User name of the administrator database user to configure the database");
      
      registerParameter(DATABASE_ADMIN_PASSWORD, 
                        SetupReader.PARAMETER_TYPE_STRING_OBJ,
                        DATABASE_ADMIN_PASSWORD_DEFAULT,
                        "Password of the administrator database user to configure the database");

      registerParameter(DATABASE_TRANSACTION_ISOLATION,
                        SetupReader.PARAMETER_TYPE_STRING_OBJ,
                        DATABASE_TRANSACTION_ISOLATION_DEFAULT,
                        "Default transaction isolation level");
   }
}
