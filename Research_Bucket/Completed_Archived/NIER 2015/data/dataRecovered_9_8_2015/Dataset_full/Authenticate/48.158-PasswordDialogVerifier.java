/*
 * Copyright (c) 2005 - 2007 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: PasswordDialogVerifier.java,v 1.2 2009/04/22 06:29:19 bastafidli Exp $
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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.gui.ApplicationGui;
import org.opensubsystems.core.logic.ControllerManager;
import org.opensubsystems.core.util.Log;
import org.opensubsystems.security.data.User;
import org.opensubsystems.security.logic.AuthenticationController;
import org.opensubsystems.security.util.LoginConstants;

/**
 * Dialog listener allowing to verify password of a user.
 *  
 * @version $Id: PasswordDialogVerifier.java,v 1.2 2009/04/22 06:29:19 bastafidli Exp $
 * @author Miro Halas
 * @code.reviewer Miro Halas
 * @code.reviewed Initial revision.
 */
public class PasswordDialogVerifier implements PasswordVerifyDialogListener
{
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Dialog which is being processed by this class.
    */
   protected PasswordVerifyDialog m_dialog;
   
   /**
    * Flag which signals if the password was verified or not.
    */
   protected boolean m_bPasswordVerified;
   
   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(PasswordDialogVerifier.class);

   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Constructor
    * 
    * @param dialog - dialog being processed.
    * @throws OSSException - an error has occurred
    */
   public PasswordDialogVerifier(
      PasswordVerifyDialog dialog
   ) throws OSSException
   {
      m_dialog = dialog;
      
      m_bPasswordVerified = false;
   }
   
   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * Get result of the password verification operation.
    * 
    * @return boolean - if true then password was verified, false otherwise
    */
   public boolean getResult(
   )
   {
      return m_bPasswordVerified;
   }
   
   /**
    * {@inheritDoc}
    */
   public boolean takeAction(
      String strPassword
   )
   {
      // TODO: ThickClient: Here is our MAJOR challenge. This can be executed
      // on a client, which also contains server (and therefore can create
      // local user transaction and create local controller) or it can be 
      // executed on a remote client, which is separate from server. The
      // controllers created here must be able to execute the functionality 
      // on the server and not on the client (effectively remote call).
      try
      {
         Object[] userInfo;
         
         userInfo = getController().verifyAndReturnCurrentUser(strPassword);
         if (userInfo[0] != null)
         {
            // Password was verified
            m_bPasswordVerified = true;
            s_logger.finer("Password of " + ((User)userInfo[0]).getLoginName() 
                           + " successfully verified.");
         }
         else
         {
            String strErrorMessage;
            
            s_logger.finer("Login of current user with password " 
                                  + strPassword + " failed.");
            switch (((Integer)userInfo[1]).intValue()) 
            {
               case LoginConstants.LOGIN_NAME_NOT_VALID:
               {
                  strErrorMessage = "Specified login name is not valid.";
                  break;
               }
               case LoginConstants.LOGIN_INCORRECT_PASSWORD:
               {
                  strErrorMessage = "The password is incorrect.";
                  break;
               }
               default :
               {
                  strErrorMessage = "Unspecified login error.";
                  break;                  
               }
            }

            m_dialog.getGui().displayMessage(ApplicationGui.MESSAGE_TITLE_INFO,
                           strErrorMessage, ApplicationGui.MESSAGE_STYLE_INFO);
         }
      }
      catch (Exception exc)
      {
         s_logger.log(Level.WARNING, 
                      "Unexpected error has occurred during login name validation.",
                      exc);
         
         m_dialog.getGui().displayMessage(ApplicationGui.MESSAGE_TITLE_ERROR,
                       "Unexpected error has occurred during login: "
                       + exc.getMessage(),
                       ApplicationGui.MESSAGE_STYLE_ERROR);
      }
      
      return m_bPasswordVerified;
   }

   // Helper methods ///////////////////////////////////////////////////////////
   
   /**
    * Get controller to invoke business logic.
    * 
    * @return AuthenticationController
    * @throws OSSException - an error has occurred
    */
   protected AuthenticationController getController(
   ) throws OSSException
   {
      AuthenticationController controller;
      
      controller = (AuthenticationController)ControllerManager.getInstance(
                                         AuthenticationController.class);
      
      return controller;
   }
}
