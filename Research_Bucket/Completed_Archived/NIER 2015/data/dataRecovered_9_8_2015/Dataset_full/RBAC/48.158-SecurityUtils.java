/*
 * Copyright (c) 2006 - 2009 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: SecurityUtils.java,v 1.13 2009/04/22 06:29:41 bastafidli Exp $
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

package org.opensubsystems.security.util;

import java.rmi.RemoteException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.transaction.UserTransaction;

import org.opensubsystems.core.application.impl.InstanceInfoImpl;
import org.opensubsystems.core.data.DataObject;
import org.opensubsystems.core.error.OSSDataCreateException;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.error.OSSInternalErrorException;
import org.opensubsystems.core.logic.ControllerManager;
import org.opensubsystems.core.persist.db.impl.DatabaseTransactionFactoryImpl;
import org.opensubsystems.core.util.Config;
import org.opensubsystems.core.util.Log;
import org.opensubsystems.core.util.PropertyUtils;
import org.opensubsystems.core.util.TransactionUtils;
import org.opensubsystems.security.data.Domain;
import org.opensubsystems.security.data.DomainDataDescriptor;
import org.opensubsystems.security.data.User;
import org.opensubsystems.security.logic.DomainController;
import org.opensubsystems.security.logic.SessionController;
import org.opensubsystems.security.logic.UserController;

/**
 * Collection of useful methods and constants used with security subsystem.
 *
 * @version $Id: SecurityUtils.java,v 1.13 2009/04/22 06:29:41 bastafidli Exp $
 * @author Julian Legeny
 * @code.reviewer Miro Halas
 * @code.reviewed Initial revision 
 */
public final class SecurityUtils
{
   // Configuration parameters /////////////////////////////////////////////////

   /** 
    * Name of the property to configure if user creating their accounts are able
    * to create their own domains at the same time. If set to true, it allows 
    * not logged in users to create domains at the time of registration. 
    * 
    * @see #DEFAULT_ALLOW_CREATE_DOMAIN
    */   
   public static final String ALLOW_CREATE_DOMAIN_PROPERTY_NAME 
                                 = "oss.domain.create";   

   /** 
    * Name of the property to configure flag signaling if system or not logged 
    * in user can create first domain if no domain exists in the persistence 
    * store.
    * 
    * @see #DEFAULT_ALLOW_CREATE_FIRST_DOMAIN
    */   
   public static final String ALLOW_CREATE_FIRST_DOMAIN_PROPERTY_NAME 
                                 = "oss.domain.createfirst";   

   /** 
    * Name of the property to configure flag specifying if newly created domain
    * is enabled by default or not.
    * 
    * @see #DEFAULT_CREATE_ENABLED_DOMAIN
    */   
   public static final String CREATE_ENABLED_DOMAIN_PROPERTY_NAME 
                                 = "oss.domain.enabled";   

   /**
    * Name of the property to configure default user name.
    */   
   public static final String FIRST_USER_LOGIN_NAME_PROPERTY_NAME 
                                 = "oss.security.defaultuser";

   /**
    * Name of the property to configure default password.
    */   
   public static final String FIRST_USER_PASSWORD_PROPERTY_NAME 
                                 = "oss.security.defaultpassword";

   /**
    * Name of the property to configure the login name for user used by the 
    * background processes. 
    */   
   public static final String BACKGROUND_USER_LOGIN_NAME 
                                 = "oss.security.backgrounduser";
   
   /**
    * Name of the property to configure the password for user used by the 
    * background processes.
    */   
   public static final String BACKGROUND_USER_PASSWORD 
                                 = "oss.security.backgroundpassword";

   /**
    * Name of the property to configure if users are required to enter 
    * password to access the system or if just knowing a valid login name is 
    * enough.
    */   
   public static final String REQUIRE_PASSWORD 
                                 = "oss.security.usepassword";

   // Constants ////////////////////////////////////////////////////////////////

   /**
    * Default value for ALLOW_CREATE_DOMAIN.
    * @see #ALLOW_CREATE_DOMAIN_PROPERTY_NAME
    */
   public static final Boolean DEFAULT_ALLOW_CREATE_DOMAIN = Boolean.TRUE;

   /**
    * Default value for ALLOW_CREATE_FIRST_DOMAIN.
    * @see #ALLOW_CREATE_FIRST_DOMAIN_PROPERTY_NAME
    */
   public static final Boolean DEFAULT_ALLOW_CREATE_FIRST_DOMAIN = Boolean.TRUE;

   /**
    * Default value for CREATE_ENABLED_DOMAIN.
    * @see #CREATE_ENABLED_DOMAIN_PROPERTY_NAME
    */
   public static final Boolean DEFAULT_CREATE_ENABLED_DOMAIN = Boolean.TRUE;

   /**
    * Default name of the first created domain.
    */
   public static final String DEFAULT_FIRST_DOMAIN_NAME = "OpenSubsystems";

   /**
    * Default description of the first created domain.
    */
   public static final String DEFAULT_FIRST_DOMAIN_DESCRIPTION = "Default domain";

   /**
    * Default login name of the first created user.
    */
   protected static final String DEFAULT_FIRST_USER_LOGIN_NAME = "basta";

   /**
    * Default password of the first created user.
    */
   protected static final String DEFAULT_FIRST_USER_PASSWORD = "fidli";

   /**
    * Default first name of the first created user.
    */
   public static final String DEFAULT_FIRST_USER_FIRST_NAME = "System";

   /**
    * Default last name of the first created user.
    */
   public static final String DEFAULT_FIRST_USER_LAST_NAME = "Administrator";

   /**
    * Default phone number of the first created user.
    */
   protected static final String DEFAULT_FIRST_USER_PHONE = "";

   /**
    * Default fax number of the first created user.
    */
   protected static final String DEFAULT_FIRST_USER_FAX = "";

   /**
    * Default postal address of the first created user.
    */
   protected static final String DEFAULT_FIRST_USER_ADDRESS = "";

   /**
    * Default e-mail address of the first created user.
    */
   protected static final String DEFAULT_FIRST_USER_EMAIL = "info@opensubsystems.org";

   /**
    * Default login name of user for background tasks. It is numeric so that it 
    * works also for clients, which allow only numeric entries.
    */
   public static final String DEFAULT_BACKGROUND_USER_LOGIN_NAME = "0000";

   /**
    * Default password of user for background tasks. It is numeric so that it 
    * works also for clients, which allow only numeric entries.
    */
   public static final String DEFAULT_BACKGROUND_USER_PASSWORD = "0000";

   /**
    * Default value of the property to configure if users are required to enter 
    * password to access the system or if just knowing a valid login name is 
    * enough.
    */   
   public static final Boolean DEFAULT_REQUIRE_PASSWORD = Boolean.TRUE;
   
   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(SecurityUtils.class);

   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private SecurityUtils(
   )
   {
      // Do nothing
   }
   
   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * Returns flag signaling if user creating their accounts are able to create 
    * their own domains at the same time. If set to true, it allows not logged 
    * in users to create domains at the time of registration.  
    * 
    * @return boolean - true if domains can be created is allowed by not logged 
    *                   in users
    */
   public static boolean isAllowCreateDomain()
   {
      Properties prpSettings;
      boolean    bAllowCreateDomain;
      
      prpSettings = Config.getInstance().getProperties();

      bAllowCreateDomain = PropertyUtils.getBooleanProperty(
         prpSettings, ALLOW_CREATE_DOMAIN_PROPERTY_NAME, 
         DEFAULT_ALLOW_CREATE_DOMAIN, 
         "Allow to create domains by unauthenticated clients.").booleanValue();  

      return bAllowCreateDomain;
   }

   /**
    * Returns flag signaling if system or not logged in user can create first 
    * domain if no domain exists in the persistence store.  
    * 
    * @return boolean - true if the first domain can be created by not logged in
    *                   user
    */
   public static boolean isAllowCreateFirstDomain()
   {
      Properties prpSettings;
      boolean    bAllowCreateFirstDomain;
      
      prpSettings = Config.getInstance().getProperties();

      bAllowCreateFirstDomain = PropertyUtils.getBooleanProperty(
         prpSettings, ALLOW_CREATE_FIRST_DOMAIN_PROPERTY_NAME, 
         DEFAULT_ALLOW_CREATE_FIRST_DOMAIN, 
         "Allow to create first domain in the system by unauthenticated client."
      ).booleanValue();
      
      return bAllowCreateFirstDomain;
   }

   /**
    * Returns flag specifying if newly created domain is enabled by default or 
    * not. 
    * 
    * @return boolean - true if domain is enabled
    */
   public static boolean isDomainEnabled()
   {
      Properties prpSettings;
      boolean    bCreateEnabledDomain;
      
      prpSettings = Config.getInstance().getProperties();

      bCreateEnabledDomain = PropertyUtils.getBooleanProperty(
         prpSettings, CREATE_ENABLED_DOMAIN_PROPERTY_NAME, 
         DEFAULT_CREATE_ENABLED_DOMAIN, 
         "Create by default domains as enabled").booleanValue();
      
      return bCreateEnabledDomain;
      
   }
   
   /**
    * Are users required to enter password to access the system or if just 
    * knowing a valid login name is enough.
    * 
    * @return boolean
    */
   public static boolean isPasswordRequired(
   )
   {
      Properties prpSettings;
      boolean    bRequirePassword;
      
      prpSettings = Config.getInstance().getProperties();

      bRequirePassword = PropertyUtils.getBooleanProperty(
         prpSettings, REQUIRE_PASSWORD, DEFAULT_REQUIRE_PASSWORD, 
         "Allow to login just with a user name").booleanValue();
      
      return bRequirePassword;
   }
   
   /**
    * Get the login name of the user used for background tasks. 
    * 
    * @return String - login name of background user, this can be null or empty
    */
   public static String getBackgroundUserLoginName(
   )
   {
      Properties prpSettings;
      String     strBackgroundUserLoginName;
      
      prpSettings = Config.getInstance().getProperties();

      strBackgroundUserLoginName = PropertyUtils.getStringProperty(
         prpSettings, BACKGROUND_USER_LOGIN_NAME,
         DEFAULT_BACKGROUND_USER_LOGIN_NAME,
         "Login name of the user for background tasks");
      
      return strBackgroundUserLoginName;
   }
   
   /**
    * Get the password of the user used for background tasks.
    * 
    * @return String - password of background user, this can be null or empty
    */
   public static String getBackgroundUserPassword(
   )
   {
      Properties prpSettings;
      String     strBackgroundUserPassword;

      prpSettings = Config.getInstance().getProperties();

      strBackgroundUserPassword = PropertyUtils.getStringProperty(
         prpSettings, BACKGROUND_USER_PASSWORD,
         DEFAULT_BACKGROUND_USER_PASSWORD,
         "Password of the user for background tasks");
      
      return strBackgroundUserPassword;
   }
   
   /**
    * Create initial data for the security subsystem.
    * 
    * @throws OSSException - an error has occurred
    */
   public static void createInitialData(
   ) throws OSSException
   {
      UserTransaction transaction = null;
      Domain          firstDomain = null;
      User            firstUser = null;
      boolean         bExistDomain;
      
      s_logger.fine("Creating initial security data.");

      try
      {
         // Get flag if any domain exists within the database
         bExistDomain = ((DomainController)ControllerManager.getInstance(
                           DomainController.class)).existDomain();
      }
      catch (RemoteException rExc)
      {
         // We cannot propagate this exception otherwise XDoclet would generate 
         // the local interface incorrectly since it would include the declared
         // RemoteException in it (to propagate we would have to declare it)
         throw new OSSInternalErrorException("Remote error occurred", rExc);
      }

      if ((SecurityUtils.isAllowCreateFirstDomain()) && (!bExistDomain))
      {
         Properties prpSettings;
         String     strFirstUserLoginName;
         String     strFirstUserPassword;

         prpSettings = Config.getInstance().getProperties();

         strFirstUserLoginName = PropertyUtils.getStringProperty(
            prpSettings, FIRST_USER_LOGIN_NAME_PROPERTY_NAME, 
            DEFAULT_FIRST_USER_LOGIN_NAME, 
            "Login name of the first automatically created user"); 
            
         strFirstUserPassword = PropertyUtils.getStringProperty(
            prpSettings, FIRST_USER_PASSWORD_PROPERTY_NAME, 
            DEFAULT_FIRST_USER_PASSWORD, 
            "Password of the first automatically created user");
         
         // allow creating initial first domain, user, role, ... only in case
         // when there is allowed flag for creating first domain and also there
         // was no domain inserted within the database
         firstDomain = new Domain(
            DataObject.NEW_ID,
            DEFAULT_FIRST_DOMAIN_NAME,
            DEFAULT_FIRST_DOMAIN_DESCRIPTION,
            DomainDataDescriptor.DOMAIN_DEFAULT_ENABLED,
            true, // first domain within the schema has administration flag always true 
            DomainDataDescriptor.DOMAIN_DEFAULT_ALLOW_SELFREG,
            DomainDataDescriptor.DOMAIN_DEFAULT_PUBLIC,
            DomainDataDescriptor.DOMAIN_DEFAULT_LOGIN_ENABLED,
            DomainDataDescriptor.DOMAIN_DEFAULT_SUPER_USER,
            DomainDataDescriptor.DOMAIN_DEFAULT_INTERNAL_USER,
            DomainDataDescriptor.DOMAIN_DEFAULT_PHONE,
            DomainDataDescriptor.DOMAIN_DEFAULT_ADDRESS,
            null, null, null
         );
         
         firstUser = new User(
                  DataObject.NEW_ID,
                  firstDomain.getId(),
                  DEFAULT_FIRST_USER_FIRST_NAME, 
                  DEFAULT_FIRST_USER_LAST_NAME,
                  DEFAULT_FIRST_USER_PHONE,
                  DEFAULT_FIRST_USER_FAX,
                  DEFAULT_FIRST_USER_ADDRESS,
                  DEFAULT_FIRST_USER_EMAIL,
                  strFirstUserLoginName,
                  strFirstUserPassword,
                  true, true, true, true,
                  null, null, true
         );

         // TODO: For Miro: Consider creation of the background user as well
         
         try
         {
            // Create default domain and user and role(s)
            transaction = DatabaseTransactionFactoryImpl.getInstance().requestTransaction();
            transaction.begin();
            ((UserController)ControllerManager.getInstance(
                  UserController.class)).create(firstDomain, firstUser);
            transaction.commit();
            s_logger.fine("Initial security data created.");
         }
         catch (OSSException ossExc)
         {
            TransactionUtils.rollback(transaction);
            throw ossExc;
         }
         catch (Throwable thr)
         {
            TransactionUtils.rollback(transaction);
            throw new OSSDataCreateException(
               "Unexpected error has occurred while creating first domain and user.", 
               thr);
         }
      }
   }

   /**
    * Delete any orphan sessions if they are left out from previous run for 
    * example after application crash, which prevented them to be cleaned out
    * the normal way.
    * 
    * @throws OSSException - an error has occurred
    */
   public static void cleanOrphanSessions(
   ) throws OSSException
   {
      UserTransaction transaction = null;
   
      s_logger.fine("Cleaning orphan sessions.");
      try
      {
         // TODO: ThickClient: Here is our MAJOR challenge. This can be executed
         // on a client, which also contains server (and therefore can create
         // local user transaction and create local controller) or it can be 
         // executed on a remote client, which is separate from server and 
         // therefore if it creates transaction here, it has to somehow exist
         // also on the server (effectively distributed transaction) and the 
         // controllers created here must be able to execute the functionality 
         // on the server and not on the client (effectively remote call).
         transaction = DatabaseTransactionFactoryImpl.getInstance().requestTransaction();
         
         transaction.begin();
         ((SessionController)ControllerManager.getInstance(
            SessionController.class)).cleanOrphanSession(
               InstanceInfoImpl.getInstance());
         transaction.commit();
         s_logger.fine("Orphan sessions cleaned.");
      }
      catch (OSSException ossExc)
      {
         TransactionUtils.rollback(transaction);
         throw ossExc;
      }
      catch (Throwable thr)
      {
         TransactionUtils.rollback(transaction);
         throw new OSSDataCreateException(
                  "Unexpected error has occurred while cleaning orphan session.", 
                  thr);
      }
   }
}
