/*
 * Copyright (c) 2003 - 2009 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: SecureGuiApplication.java,v 1.5 2009/04/22 06:29:19 bastafidli Exp $
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

package org.opensubsystems.security.gui;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.transaction.UserTransaction;

import org.opensubsystems.core.application.Application;
import org.opensubsystems.core.application.BackendModule;
import org.opensubsystems.core.application.Module;
import org.opensubsystems.core.application.ModuleManager;
import org.opensubsystems.core.application.ProductInfo;
import org.opensubsystems.core.application.impl.ApplicationImpl;
import org.opensubsystems.core.application.impl.InstanceInfoImpl;
import org.opensubsystems.core.application.impl.ProductInfoImpl;
import org.opensubsystems.core.data.DataObject;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.error.OSSInternalErrorException;
import org.opensubsystems.core.gui.ApplicationGui;
import org.opensubsystems.core.gui.impl.GuiApplicationImpl;
import org.opensubsystems.core.logic.ControllerManager;
import org.opensubsystems.core.persist.db.Database;
import org.opensubsystems.core.persist.db.impl.DatabaseImpl;
import org.opensubsystems.core.persist.db.impl.DatabaseTransactionFactoryImpl;
import org.opensubsystems.core.util.CallContext;
import org.opensubsystems.core.util.GlobalConstants;
import org.opensubsystems.core.util.Log;
import org.opensubsystems.core.util.NetUtils;
import org.opensubsystems.core.util.TransactionUtils;
import org.opensubsystems.security.application.SecurityBackendModule;
import org.opensubsystems.security.data.Domain;
import org.opensubsystems.security.data.User;
import org.opensubsystems.security.logic.SecurityDefinitionManager;
import org.opensubsystems.security.logic.SessionController;
import org.opensubsystems.security.logic.UserController;
import org.opensubsystems.security.util.SecureCallContext;
import org.opensubsystems.security.util.SecurityUtils;

/**
 * Gui application providing security features such as authentication and 
 * authorization.
 * 
 * This class also ensures that correct CallContext is set after the user is 
 * logged in and therefore the user information is propagated with all calls
 * which needs to be done in a secure manner. It takes advantage of the fact
 * that Java GUI (regardless if it is constructed using SWT or AWT) runs in 
 * a single thread therefore if this class sets or resets the call context, 
 * all methods invoked by this class or any other class in the same thread will
 * share the same call context. 
 *  
 * TODO: For Everyone: Code in this class needs to be kept in sync with code
 *                     in SecureApplication
 *                       
 * @version $Id: SecureGuiApplication.java,v 1.5 2009/04/22 06:29:19 bastafidli Exp $
 * @author Miro Halas
 * @code.reviewer Miro Halas
 * @code.reviewed Initial revision
 */
public class SecureGuiApplication extends GuiApplicationImpl
{
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * This flag controls if the login screen should be displayed again and again
    * after user logged out or exited the application.
    */
   protected boolean m_bRepeatLogin = false;
   
   /**
    * Login name of user created with create user dialog (first user). Cached
    * so that user doesn't have to login after he/she has created its account.
    */
   protected String m_strCreatedUserLoginName = null;
   
   /**
    * This is the user, which should be used to execute background tasks.
    */
   protected User m_backgroundUser = null;

   /**
    * This is the session, which should be used to execute background tasks.
    */
   protected String m_strBackgroundSession = null;

   /**
    * After successful login this will contain user information for the 
    * foreground user, which is user using the gui.
    * This information is not really required here since once user is logged in,
    * it is already kept within call context, but by keeping a copy here we 
    * can double check that the calls are executed within the correct call 
    * context. 
    */
   protected User m_user; 
   
   /**
    * After successful login this will contain session information for the 
    * foreground user, which is user using the gui.
    * This information is not really required here since once user is logged in,
    * it is already kept within call context, but by keeping a copy here we 
    * can double check that the calls are executed within the correct call 
    * context. 
    */
   protected String m_session;

   /**
    * Domain created by the system if no other exists.
    */
   protected Domain m_domain;

   // Cached values ////////////////////////////////////////////////////////////
   
   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(SecureGuiApplication.class);

   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Construct instance of the secure gui application.
    */
   public SecureGuiApplication(
   )
   {
      this(new ProductInfoImpl("Generic OpenSubsystems Secure GUI application", 
                               "1.0", "OpenSubsystems s.r.o.",
                               "Copyright (c) 2003 - 2008 OpenSubsystems s.r.o."
                               + " Slovak Republic. All rights reserved.")
          );
   }
   
   /**
    * Construct instance of the secure gui application.
    * 
    * @param product - information about product which is running this 
    *                  application, this way we will force every application 
    *                  to create and publish this information in uniform way
    */
   public SecureGuiApplication(
      ProductInfo product
   )
   {
      super(product);

      // Since the developer is using SecureGuiApplication class it is required 
      // that the SecureCallContext is used to correctly store and translate 
      // values 
      if (!(CallContext.getInstance() instanceof SecureCallContext))
      {
         CallContext.setInstance(new SecureCallContext());
      }
   }
   
   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * {@inheritDoc}
    */
   public void init(
   ) throws OSSException, 
            IOException
   {
      super.init();

      // Secure application will always require security subsystem (or some
      // customized version of it) so add it to the application
      ApplicationImpl.getInstance().add(
         ModuleManager.getInstance(SecurityBackendModule.class));
   }   
   
   /**
    * {@inheritDoc}
    */
   public boolean prepareToStopGui(
   )
   {
      boolean  bReturn;
      
      bReturn = super.prepareToStopGui(); 
      if (bReturn)
      {
         try
         {
            // Logout the user using this application
            // We were asked to stop the gui so no need to repeat the login
            // Do not change value of bReturn since we don't know what has 
            // happened there so we don't know if it is save to stop logout
            logout(false);
         }
         catch (OSSException exc)
         {
            m_gui.displayMessage(ApplicationGui.MESSAGE_TITLE_ERROR, 
                                 "Unexpected error has occurred during logout: "
                                 + exc.getMessage(),
                                 ApplicationGui.MESSAGE_STYLE_ERROR);
         }
      }
      
      return bReturn;
   }
   
   /**
    * {@inheritDoc}
    */
   public void stop(
   ) throws OSSException
   {
      s_logger.entering(this.getClass().getName(), "stop");
      try
      {
         // Logout the user for background tasks. We don't need gui for this
         // since user doesn't need to be aware that there is such user so
         // even if there is a problem, we wouldn't communicate this to user.
         logoutBackgroundUser();         
      }
      finally
      {
         try
         {
            // Cleanup any leftover data that may have remain in the system
            cleanOrphanSessions();
         }
         finally
         {
            try
            {
               // Stop the underlying application
               super.stop();
            }
            finally
            {
               s_logger.exiting(this.getClass().getName(), "stop");
            }
         }
      }
   }
   
   /**
    * Logout current foreground user. 
    * 
    * @param bRepeatLogin - should the login screen come up again
    * @throws OSSException - an error has occurred
    */
   public void logout(
      boolean bRepeatLogin
   ) throws OSSException
   {
      if (m_session != null)
      {
         UserTransaction transaction = null;
         
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
            s_logger.fine("Session " + m_session + " will be logged out");
   
            if (GlobalConstants.ERROR_CHECKING)
            {
               // This method needs to be called with session context so make
               // sure it is still correctly configured
               assert CallContext.getInstance() != null 
                      : "Call context has to be present.";
               assert CallContext.getInstance() instanceof SecureCallContext 
                      : "Call context is not suitable for secure gui application.";
               assert m_session.equals(CallContext.getInstance().getCurrentSession()) 
                      : "Call context doesn't match current session.";
               assert m_user.equals(CallContext.getInstance().getCurrentUser()) 
                      : "Call context doesn't match current user.";
            }
            
            getSessionController().logout(m_session);
            transaction.commit();
            
            // Logout was successful therefore we need to reset the call context
            // and all other variables
            CallContext.getInstance().reset();
            s_logger.fine("Session "  + m_session + " was logged out");
            m_user = null;
            m_session = null;
            m_bRepeatLogin = bRepeatLogin;
         }
         catch (Throwable thr) 
         {
            s_logger.log(Level.SEVERE, 
                         "Unexpected error has occurred while logging out session " 
                         + m_session,
                         thr);
            TransactionUtils.rollback(transaction);
            throw new OSSInternalErrorException(thr);
         }
      }
   }

   // Helper methods ///////////////////////////////////////////////////////////

   /**
    * {@inheritDoc}
    */
   protected void addDirectly(
      Module module
   ) throws OSSException
   {
      super.addDirectly(module);
      
      if (module instanceof BackendModule)
      {
         SecurityDefinitionManager.getInstance().add((BackendModule)module);
      }
   }
   
   /**
    * {@inheritDoc}
    * 
    * This method overrides the default implementation and allows to start the
    * application only if user logs in to the system.
    * @throws OSSException {@inheritDoc} 
    */
   protected void startGui(
   ) throws OSSException
   {
      // Create the initial data needed by the application
      createInitialData();
      
      // Cleanup any leftover data from a previous session
      cleanOrphanSessions();
      
      // TODO: For Miro: Look at SecureApplication, which is also deleting
      // content of temporary directory and creating initial data
      
      // TODO: ThickClient: We should make this configurable since not every 
      // secure application requires background user, 
      // e.g. oss.secureapp.needsbackgrounduser
      // Make sure we have user session for execution of background tasks 
      loginBackgroundUser();
      
      // Only after the sessions were cleaned up and the user for background 
      // tasks is ready, it is possible to start the secure gui application
      // Do not call base class since this method is replacing the default
      // behavior to introduce requirement of login in
      // super.startGui();
      
      try
      {
         // Create display resources only once since for repeated login we do 
         // not need new resources. These might be also needed to login user
         // to the application
         m_gui.createDisplayResources(m_bHideCursor);
         
         do
         {
            m_bRepeatLogin = false;
            // Login user to the system
            if (login())
            {
               // User is successfully logged in so display gui 
               
               // Display the gui using previously initialized resources in 
               // separate method so that if application had a different way 
               // how to display gui then it can change this behavior
               displayGui();
            }
         }
         while (m_bRepeatLogin);
      }
      finally
      {
         try
         {
            try
            {
               logout(true);
            }
            catch (OSSException exc)
            {
               s_logger.log(Level.SEVERE, "Unexpected error", exc);
            }
         }
         finally
         {
            // We are done with the GUI so we can destroy the display resources
            m_gui.destroyDisplayResources();
         }
      }               
   }   

   /**
    * Cleanup orphan sessions in case the system crashed.
    */
   protected void createInitialData(
   )
   {
      if (isLocalDatabaseUsed())
      {
         // This application is using local database and therefore try to create
         // initial data if they do not exist yet
         Database dbDatabase;

         try
         {
            dbDatabase = DatabaseImpl.getInstanceIfStarted();
            if (dbDatabase != null)
            {
               SecurityUtils.createInitialData();
            }
         }
         catch(Throwable thr)
         {
            s_logger.log(Level.WARNING, 
               "Unexpected error has occurred while creating initial data.",
               thr);
         }
      }
   }
   
   /**
    * Cleanup orphan sessions in case the system crashed.
    */
   protected void cleanOrphanSessions(
   )
   {
      if (isLocalDatabaseUsed())
      {
         // This application is using local database and therefore clean
         // the orphan sessions that may have been left in it in case the 
         // application crashed
         Database dbDatabase;

         // We don't want to start database if the start() crashed and the 
         // database never started and we are just cleaning up   
         try
         {
            dbDatabase = DatabaseImpl.getInstanceIfStarted();
            if (dbDatabase != null)
            {
               SecurityUtils.cleanOrphanSessions();
            }
         }
         catch(Throwable thr)
         {
            s_logger.log(Level.WARNING, 
               "Unexpected error has occurred while cleaning orphan sessions.",
               thr);
         }
      }
   }
   
   /**
    * Login user to the system
    * 
    * @return boolean - true if user is logged in, false otherwise
    * @throws OSSException - an error has occurred
    */
   protected boolean login(
   ) throws OSSException
   {
      // Generate unique session id for this application, this is not the same
      // session as the application generates 
      String strApplicationSession = (new java.rmi.server.UID()).toString();

      // Reset all session based variables
      m_user = null;
      m_session = null;
      
      if (checkForInternalEnabledUsers())
      {
         // A new user was created so log him in automatically so he doesn't 
         // need to reenter the login name and password again
         String          strClientAddress = NetUtils.getServerIPAddress();
         UserTransaction transaction;
         
         // TODO: ThickClient: Here is our MAJOR challenge. This can be executed
         // on a client, which also contains server (and therefore can create
         // local user transaction and create local controller) or it can be 
         // executed on a remote client, which is separate from server and 
         // therefore if it creates transaction here, it has to somehow exist
         // also on the server (effectively distributed transaction) and the 
         // controllers created here must be able to execute the functionality 
         // on the server and not on the client (effectively remote call).
         transaction = DatabaseTransactionFactoryImpl.getInstance().requestTransaction();
         try
         {
            Object[]    loggedUserInfo;
            Application currentApp;
            ProductInfo currentProd;
            
            currentApp  = ApplicationImpl.getInstance();
            currentProd = currentApp.getCurrentProduct();
            transaction.begin();
            // TODO: ThickClient: Here we are login as guest even though we can have
            // a password if the user created it. Why are we doing it that way 
            // instead of normal login? We should make it dependent on s_bUsePassword
            loggedUserInfo = getSessionController().loginGuest(
                                m_strCreatedUserLoginName,
                                strApplicationSession,
                                strClientAddress,
                                currentProd.getName() + " " 
                                + currentProd.getVersion(), 
                                InstanceInfoImpl.getInstance());
            transaction.commit();
            if (loggedUserInfo[0] != null)
            {
               m_user = (User)loggedUserInfo[0];
               m_session = (String)loggedUserInfo[2];

               // Setup call context since all calls will be executed in the 
               // thread where the user interface runs
               CallContext.getInstance().setCurrentUserAndSession(m_user, 
                                                                  m_session);
               s_logger.finer("User " + m_strCreatedUserLoginName 
                              + " logged in as " + m_session);
            }
            else
            {
               // TODO: Improve: We should display more detailed information
               // why we coudn't login the user
               s_logger.fine("Problems to login created user.");
               m_gui.displayMessage(ApplicationGui.MESSAGE_TITLE_INFO,
                                    "Problems to login created user.",
                                    ApplicationGui.MESSAGE_STYLE_INFO);
            }
         }
         catch (Throwable thr)
         {
            s_logger.log(Level.SEVERE, 
                         "Unexpected error has occurred during silent login.",
                         thr);
            TransactionUtils.rollback(transaction);
            m_gui.displayMessage(ApplicationGui.MESSAGE_TITLE_ERROR,
                     "Unexpected error has occurred during login: "
                     + thr.getMessage(),
                     ApplicationGui.MESSAGE_STYLE_ERROR);
         }
         
         
         if (m_session == null)
         {
            m_bRepeatLogin = true;
         }
      }
      else
      {
         // No user was created to display login dialog to allow user to login
         Object[]    arLoginData;
         Application currentApp;
         ProductInfo currentProd;
         
         currentApp  = ApplicationImpl.getInstance();
         currentProd = currentApp.getCurrentProduct();
         
         // TODO: ThickClient: We need to propagate s_bUsePassword to the login 
         // dialog to allow either login with or without password
         // see use of SecureThickClient.userPassword() in SecurityModule
         LoginDialog dialog = new LoginDialog(this,
                                     currentProd.getName()  
                                     + " " + currentProd.getVersion()
                                     + " by " + currentProd.getCreator());
         LoginDialogListener loginMethod = new LoginDialogGuestLogin(dialog,
                                                  strApplicationSession); 
                  
         dialog.addLoginDialogGuiListener(loginMethod);
         dialog.displayDialog();
         arLoginData = ((LoginDialogGuestLogin)loginMethod).getResult();
         if (arLoginData != null)
         {
            // Get info from login dialog
            m_user = (User)arLoginData[0];
            m_session = (String)arLoginData[1];
            
            // Setup call context since all calls will be executed in the thread
            // where the user interface runs
            CallContext.getInstance().setCurrentUserAndSession(m_user, m_session);
            s_logger.finer("User " + m_user.getName() + " logged in as " 
                           + m_session);
         }
      }
      
      return (m_session != null);
   }
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   // TODO: For Miro: The methods below needs to be reworked since they are just
   // hacked together
   
   /**
    * This method will make sure that we have session can be used for execution 
    * of background tasks.
    * 
    * @throws OSSException - an error has occurred
    */
   protected void loginBackgroundUser(
   ) throws OSSException
   {
      User            backgroundUser;
      UserController  userControl;
      UserTransaction transaction = null;
      Object[]        atBackgroundLogin = null;

      userControl = getUserController();
      
      // TODO: ThickClient: Originally this used factories instead of controllers 
      // since we wanted to circumvent security but this may not be possible 
      // if we run this as remote server so I converted it to controllers and 
      // if there are issues with it we may need to revise it
      try
      {
         // TODO: ThickClient: Here we are calling it without session context
         // so it may fail. Investigate and resolve it. 
         // Miro: Since it doesn't work without session context, let me setup 
         // a bogus one for now to get it work, remove the bogus context when
         // fixing it for real
         CallContext.getInstance().setCurrentUserAndSession(
            new User(0, 0, "bogus", "bogus", "bous", "bogus", "bogus", "bogus", 
                     "bogus", "bogus", true, true, true, true, null, null, 
                     true), "bogus session");
         // end of workaround
         backgroundUser = userControl.get(SecurityUtils.getBackgroundUserLoginName());
         // TODO: ThickClient: Remove this code once the bogus context above is removed
         CallContext.getInstance().resetCurrentUserAndSession();
         // end of workaround
      }
      catch (RemoteException reExc)
      {
         throw new OSSInternalErrorException("Remote error occurred", reExc);
      }
      
      transaction = DatabaseTransactionFactoryImpl.getInstance().requestTransaction();

      // The specified user doesn't exist
      if (backgroundUser == null)
      {
         s_logger.fine("Background user doesn't exist, creating one.");         
         User newUser;
      
         newUser = new User(DataObject.NEW_ID, getDefaultDomain().getId(),
                            "System", "User", "", "", "", "",
                            SecurityUtils.getBackgroundUserLoginName(), 
                            SecurityUtils.getBackgroundUserPassword(),
                            // login enables, guest enabled, super user, 
                            // internal user 
                            true, true, true, true, null, null, true);
      
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

            transaction.begin(); 
            // TODO: ThickClient: Here we are calling it without session context
            // so it may fail. Investigate and resolve it. 
            // Miro: Since it doesn't work without session context, let me setup 
            // a bogus one for now to get it work, remove the bogus context when
            // fixing it for real
            CallContext.getInstance().setCurrentUserAndSession(
               new User(0, 0, "bogus", "bogus", "bous", "bogus", "bogus", "bogus", 
                        "bogus", "bogus", true, true, true, true, null, null, 
                        true), "bogus session");
            // end of workaround
            backgroundUser = (User)userControl.create(newUser);
            // TODO: ThickClient: Remove this code once the bogus context above is removed
            CallContext.getInstance().resetCurrentUserAndSession();
            // end of workaround
            
            transaction.commit();
            s_logger.fine("Background user created.");         
         }
         catch (Throwable thr)
         {
            s_logger.log(Level.SEVERE, 
                         "Unexpected error has occurred while creating" +
                         " background user.",
                         thr);
            TransactionUtils.rollback(transaction);
            throw new OSSInternalErrorException(thr);            
         }
      }
      else
      {
         s_logger.fine("Background user exists, no need to create.");         
      }
      
      // Now we have an background user so log him into the system
      s_logger.fine("Creating session for background tasks.");         
      try
      {
         String strServerSession = (new java.rmi.server.UID()).toString();
         String strClientIP = NetUtils.getServerIPAddress(); 

         // TODO: ThickClient: Here is our MAJOR challenge. This can be executed
         // on a client, which also contains server (and therefore can create
         // local user transaction and create local controller) or it can be 
         // executed on a remote client, which is separate from server and 
         // therefore if it creates transaction here, it has to somehow exist
         // also on the server (effectively distributed transaction) and the 
         // controllers created here must be able to execute the functionality 
         // on the server and not on the client (effectively remote call).
         transaction.begin();
         // TODO: ThickClient: Here we are login as guest even though we have
         // a password. Why are we doing it that way instead of normal login?
         // We should make it dependent on s_bUsePassword
         atBackgroundLogin = getSessionController().loginGuest(
                                SecurityUtils.getBackgroundUserLoginName(),
                                strServerSession,
                                strClientIP,
                                "Console background task",
                                InstanceInfoImpl.getInstance());
         // Do the rest after we commit the transaction to keep the 
         // database locked to minimum
                                 
         // We have to commit regardless what was the outcome of login                           
         transaction.commit();
      }
      catch (Throwable thr)
      {
         s_logger.log(Level.SEVERE, "Unexpected error has occurred during login", 
                      thr);
         TransactionUtils.rollback(transaction);
         throw new OSSInternalErrorException(thr);
      }

      if (atBackgroundLogin[0] != null)
      {
         // Do not set callcontext here since it would be overridden by the
         // callcontext for the foreground user once he logs in. The background
         // task will need to establish it's own call context once it is started
         m_backgroundUser = (User)atBackgroundLogin[0];
         m_strBackgroundSession = (String)atBackgroundLogin[2];            
         s_logger.fine("Session for background tasks logged in as user " 
                       + m_backgroundUser.getLoginName() + " created and is "
                       + m_strBackgroundSession);         
      }
      else
      {
         // TODO: Improve: We should print some more information why we couldn't
         // log user in
         s_logger.severe("Cannot login admin user to the system for execution" +
                         " of background tasks");
      }
   }
   
   /**
    * This method will logout background user and terminate the session used 
    * by background tasks if it was started. 
    * 
    * @throws OSSException - an error has occurred
    */
   protected void logoutBackgroundUser(
   ) throws OSSException
   {
      UserTransaction transaction = null;

      if (m_strBackgroundSession != null)
      {
         boolean bCallContextSwitched = false;
         
         s_logger.fine("Loging out session for background tasks.");
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
            
            // Temporarily replace the identify of the current user with the
            // identity of the background user
            CallContext.getInstance().setCurrentUserAndSession(
                                         m_backgroundUser, 
                                         m_strBackgroundSession);
            bCallContextSwitched = true;
            
            transaction.begin();
            getSessionController().logout(m_strBackgroundSession);
            // We have to commit regardless what was the outcome of login                           
            transaction.commit();
            s_logger.fine("Session " + m_strBackgroundSession 
                          + " for background tasks logged out.");
         }
         catch (Throwable thr)
         {
            s_logger.log(Level.SEVERE, "Unexpected error has occurred during" +
                         " logout of session " + m_strBackgroundSession + 
                         " for background tasks", thr);
            TransactionUtils.rollback(transaction);
            throw new OSSInternalErrorException(thr);
         }
         finally
         {
            if (bCallContextSwitched)
            {
               // This restores the identify of the currently logged in user
               CallContext.getInstance().reset();
            }
         }
      }
      else
      {
         s_logger.fine("No session for background tasks to logout.");
      }
   }

   /**
    * Get default domain which should be used by the user of this application
    * at this time.
    * 
    * @return Domain - domain which this user should use
    * @throws OSSException - an error has occurred
    */
   public Domain getDefaultDomain(
   ) throws OSSException
   {
// TODO: ThickClient: This method doesn't make sense in general application, figure
// out what is the generic way to do this. It may be that the domain is sent 
// from the application of we let user log in first to figure out the domain
// This is temporary workaround to make it work for now, remove this once fixed
      m_domain = new Domain(0, "TODO", "TODO", null, null);
// end of workaround      
      
      
//      if (m_domain == null)
//      {
//         synchronized (this)
//         {
//            // TODO: Improve: There is no need for the client to know about
//            // DomainFactory, figure out how to 
//            ListOptions    listData = new ListOptions(DomainFactory.class);
//            ListController listLoader;
//            Object[]       data;
//            List           lstData;
//            Domain         retDomain = null;
//          
//            // Use the first inventory we find
//            // Don't use SecureListControllerImpl just normal one
//            // to be able find inventory undercover
//            listLoader = ListControllerImpl.getInstance(DomainFactoryImpl.class.getName());
//            data = listLoader.getShowList(listData);
//            if (data != null)
//            {
//               DataObject tempData;
//                  
//               lstData = (List)data[1];                           
//                                                                    
//               if ((lstData != null) && (!lstData.isEmpty()))
//               {
//                  retDomain = (Domain)lstData.get(0);
//               }
//            }
//            
//            if (retDomain == null)
//            {
//               // TODO: Feature: Remove this once the schema checks that we really have
//               // domain and creates one
//               retDomain = new Domain(DataObject.NEW_ID, "Default domain", 
//                                      "Automatically created by Trailer System.",
//                                      null, null);
//               
//               UserTransaction transaction;
//         
//               transaction = DatabaseTransactionFactoryImpl.getInstance().requestTransaction();
//               try
//               {
//                  transaction.begin(); 
//                  // Use factories instead of controllers since we want to circumvent security
//                  retDomain = DomainFactoryImpl.getInstance().create(retDomain);
//                  transaction.commit();
//               }
//               catch (Throwable thr)
//               {
//                  s_logger.log(Level.WARNING, 
//                                        "Unexpected error has occurred while" +
//                                        " creating domain.",
//                                        thr);
//                  TransactionUtils.rollback(transaction);
//               }
//            }
//            m_domain = retDomain;
//         }         
//      }
      
      return m_domain;
   }

   /**
    * Check if there is at least one internal user with enabled login. If not, 
    * open create user dialog and let user create one. The assumption is that
    * there should be at least one internal user with enables login since that
    * would be the user which would be managing the application. We are not 
    * checking for administrator since administrator can be user which just
    * installed the application and has maximum privileges but it may not be 
    * the user which is actually using the application.
    * 
    * @return boolean - true - if a first user was created and should be logged
    *                          in automatically 
    *                   false - no user was created or user already exists
    * @throws OSSException - an error has occurred
    */
   protected boolean checkForInternalEnabledUsers(
   ) throws OSSException
   {
      int     iDomainId;
      boolean bReturn = false;
      
      try
      {
         iDomainId = getDefaultDomain().getId();
         
         // TODO: ThickClient: Here we are calling it without session context
         // so it may fail. Investigate and resolve it
         s_logger.fine("Checking existence of internal user with enabled login.");
         if (getUserController().checkForInternalEnabledUsers(
               iDomainId,
               // Ignore the background user since we shouldn't reuse it
               new String[] {SecurityUtils.getBackgroundUserLoginName()}))
         {
            // The user already exists
            s_logger.fine("Internal user with enabled login exists.");
         }
         else
         {
            m_strCreatedUserLoginName = null;
            
            User userData = null;
      
            s_logger.fine("Internal user with enabled login doesn't exist," +
                          " asking to create one.");
            
            // TODO: ThickClient: Here we are calling it without session context
            // so it may fail. Investigate and resolve it. 
            // Miro: Since it doesn't work without session context, let me setup 
            // a bogus one for now to get it work, remove the bogus context when
            // fixing it for real
            CallContext.getInstance().setCurrentUserAndSession(
               new User(0, 0, "bogus", "bogus", "bous", "bogus", "bogus", "bogus", 
                        "bogus", "bogus", true, true, true, true, null, null, 
                        true), "bogus session");
            // end of workaround
            // First we have to load template for the new user
            userData = (User)getUserController().get(DataObject.NEW_ID);
            // TODO: ThickClient: Remove this code once the bogus context above is removed
            CallContext.getInstance().resetCurrentUserAndSession();
            // end of workaround
            
            // TODO: ThickClient: Since for now we support only guest login we need
            // to allow guest
            userData.setGuestAccessEnabled(true);
            // TODO: ThickClient: SInce we do not have way to assign access rights
            // we have to create the user as superuser
            userData.setSuperUser(true);
            UserDialog dialog = new UserDialog(this, "Create first user", false, 
                                               userData, 
                                               SecurityUtils.isPasswordRequired(), 
                                               true,
                                               "This is the first time you are" +
                                               " using the system, please create" +
                                               " an account for yourself.");
            UserDialogListener dialogPersistor = new UserDialogPersistor(dialog);
            
            // TODO: ThickClient: Here we are calling it without session context
            // so it may fail. Investigate and resolve it. 
            // Miro: Since it doesn't work without session context, let me setup 
            // a bogus one for now to get it work, remove the bogus context when
            // fixing it for real
            CallContext.getInstance().setCurrentUserAndSession(
               new User(0, 0, "bogus", "bogus", "bous", "bogus", "bogus", "bogus", 
                        "bogus", "bogus", true, true, true, true, null, null, 
                        true), "bogus session");
            // end of workaround
            dialog.addUserDialogGuiListener(dialogPersistor);
            dialog.displayDialog();
            userData = ((UserDialogPersistor)dialogPersistor).getResults();
            // TODO: ThickClient: Remove this code once the bogus context above is removed
            CallContext.getInstance().resetCurrentUserAndSession();
            // end of workaround
            
            // TODO: ThickClient: Here we are calling it without session context
            // so it may fail. Investigate and resolve it
            s_logger.fine("Checking creation of internal user with enabled login.");
            if (getUserController().checkForInternalEnabledUsers(
                  iDomainId, 
                  // Ignore the background user since we shouldn't reuse it
                  new String[] {SecurityUtils.getBackgroundUserLoginName()}) 
                && (userData != null))
            {
               m_strCreatedUserLoginName = userData.getLoginName();
               s_logger.fine("Internal user with enabled login was created.");
               bReturn = true;
            }
            else
            {
               s_logger.fine("Internal user with enabled login wasn't created.");            
            }
         }
      }
      catch (RemoteException reExc)
      {
         s_logger.log(Level.SEVERE, 
                      "Unexpected error has occurred while creating first user.",
                      reExc);
         throw new OSSInternalErrorException("Remote error occurred", reExc);
      }
      
      return bReturn;
   }
   
   /**
    * Get controller to invoke business logic.
    * 
    * @return SessionController
    * @throws OSSException - an error has occurred
    */
   protected SessionController getSessionController(
   ) throws OSSException
   {
      SessionController controller;
      
      controller = (SessionController)ControllerManager.getInstance(
                                         SessionController.class);
      
      return controller;
   }

   /**
    * Get controller to invoke business logic.
    * 
    * @return UserController
    * @throws OSSException - an error has occurred
    */
   protected UserController getUserController(
   ) throws OSSException
   {
      UserController controller;
      
      controller = (UserController)ControllerManager.getInstance(
                                         UserController.class);
      
      return controller;
   }
}
