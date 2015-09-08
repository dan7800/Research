/*
 * Copyright (c) 2003 - 2008 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: UserServlet.java,v 1.17 2009/04/22 06:29:14 bastafidli Exp $
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
 
package org.opensubsystems.security.www;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;

import org.opensubsystems.core.data.DataObject;
import org.opensubsystems.core.error.OSSConcurentModifyException;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.error.OSSInvalidContextException;
import org.opensubsystems.core.error.OSSInvalidDataException;
import org.opensubsystems.core.logic.ControllerManager;
import org.opensubsystems.core.persist.db.impl.DatabaseTransactionFactoryImpl;
import org.opensubsystems.core.util.CallContext;
import org.opensubsystems.core.util.DateUtils;
import org.opensubsystems.core.util.GlobalConstants;
import org.opensubsystems.core.util.Log;
import org.opensubsystems.core.util.Messages;
import org.opensubsystems.core.util.TransactionUtils;
import org.opensubsystems.core.www.servlet.WebUIServlet;
import org.opensubsystems.security.data.User;
import org.opensubsystems.security.logic.RoleController;
import org.opensubsystems.security.logic.UserController;

/**
 * This servlet handles viewing and editing of users.
 *  
 * @version $Id: UserServlet.java,v 1.17 2009/04/22 06:29:14 bastafidli Exp $
 * @author Miro Halas
 * @code.reviewer Miro Halas
 * @code.reviewed Initial revision
 */
public class UserServlet extends WebUIServlet
{
   // Configuration settings ///////////////////////////////////////////////////

   /** 
    * Name of the property for page to create new user.
    */   
   public static final String NEW_USER_PAGE = "oss.user.create.page";
   
   /** 
    * Name of the property for page to edit existing user.
    */   
   public static final String EDIT_USER_PAGE = "oss.user.edit.page";

   // Constants ////////////////////////////////////////////////////////////////

   /**
    * Names of the form and action to create new user.
    */
   public static final String FORM_CREATE_USER_NAME = "FORM_CREATE_USER";

   /**
    * Names of the form and action to edit existing user.
    */
   public static final String FORM_MODIFY_USER_NAME = "FORM_MODIFY_USER";

   /**
    * Names of the form and action to display new user dialog.
    */
   public static final String FORM_NEW_USER_NAME_CREATE = "create";

   /**
    * Names of the form and action to display edit user dialog.
    */
   public static final String FORM_MODIFY_USER_NAME_EDIT = "edit";

   /**
    * Constants for action to display new user dialog. 
    */
   public static final int FORM_NEW_USER_ID = FORM_COUNT_WEBUI + 1;

   /**
    * Constants for action to display edit user dialog. 
    */
   public static final int FORM_MODIFY_USER_ID = FORM_COUNT_WEBUI + 2;

   /**
    * Constants for action to create new user. 
    */
   public static final int FORM_CREATE_USER_ID = FORM_COUNT_WEBUI + 3;

   /**
    * Constants for action to modify existing user. 
    */
   public static final int FORM_SAVE_USER_ID = FORM_COUNT_WEBUI + 4;

   /**
    * How many forms this servlet recongizes. 
    */
   public static final int FORM_COUNT_USER = FORM_COUNT_WEBUI + 5;
   
   // Parameter Constants //////////////////////////////////////////////////////

   /**
    * Parameter representing data of the user 
    */
   public static final String PARAMETER_USER_NAME = "userdata";

   /**
    * Parameter representing roles of the user 
    */
   public static final String PARAMETER_ROLES_NAME = "rolelist";
   
   /**
    * Parameter signaling that the list of items if present should be reloaded.
    */
   public static final String PARAMETER_RELOAD_LIST = "reloadlist";
   
   /**
    * Name of the list of items that should be reloaded using PARAMETER_RELOAD_LIST.
    */
   public static final String VALUE_RELOAD_LIST = "userlist";

   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(UserServlet.class);

   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Generated serial version id for this class.
    */
   private static final long serialVersionUID = 322547463691937622L;

   // Logic ////////////////////////////////////////////////////////////////////

   /**
    * {@inheritDoc}
    */
   public String getServletInfo(
   )
   {
      return this.getClass().getName();
   }

   // Helper methods ///////////////////////////////////////////////////////////

   /**
    * {@inheritDoc}
    */
   protected void init(
      ServletConfig scConfig,
      Properties    prpSettings
   ) throws ServletException,
            OSSException
   {
      super.init(scConfig, prpSettings);

      cacheUIPath(prpSettings, NEW_USER_PAGE, "Page to create user");
      cacheUIPath(prpSettings, EDIT_USER_PAGE, "Page to edit user");
   }

   /**
    * {@inheritDoc}
    */
   protected void doGet(
      HttpServletRequest  hsrqRequest,
      HttpServletResponse hsrpResponse
   ) throws ServletException, 
            IOException
   {
      switch(getFormToProcess(hsrqRequest))
      {
         // Here will be actions when user only requests the data
         case (FORM_NEW_USER_ID):
         {
            processNewUserForm(hsrqRequest, hsrpResponse);
            break;
         }
         
         case (FORM_MODIFY_USER_ID):
         {
            processEditUserForm(hsrqRequest, hsrpResponse);
            break;
         }
         
         default :
         {
            super.doGet(hsrqRequest, hsrpResponse);
            break;
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   protected void doPost(
      HttpServletRequest  hsrqRequest,
      HttpServletResponse hsrpResponse
   ) throws ServletException, 
            IOException
   {
      switch(getFormToProcess(hsrqRequest))
      {
         // Here will be actions when user really modifies data         
         case FORM_CREATE_USER_ID :
         {
            processCreateUserForm(hsrqRequest, hsrpResponse);
            break;
         }
         
         case FORM_SAVE_USER_ID :
         {  
            processSaveUserForm(hsrqRequest, hsrpResponse);
            break;
         }
         
         default :
         {
            super.doPost(hsrqRequest, hsrpResponse);
            break;
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   protected int getFormToProcess(
      HttpServletRequest hsrqRequest
   )
   {
      String strFormName;
      int    iReturn = FORM_UNKNOWN_ID;

      strFormName = hsrqRequest.getParameter(FORM_NAME_REQUEST_PARAM);

      if (strFormName == null)
      {
         iReturn = super.getFormToProcess(hsrqRequest);
      }
      else if (strFormName.equals(FORM_NEW_USER_NAME_CREATE))
      {
         // Display new user page
         iReturn = FORM_NEW_USER_ID;
      }
      else if (strFormName.equals(FORM_MODIFY_USER_NAME_EDIT))
      {
         // Display edit user page
         iReturn = FORM_MODIFY_USER_ID;
      }
      else if (strFormName.equals(FORM_CREATE_USER_NAME))
      {
         // Create new user from already supplied details
         iReturn = FORM_CREATE_USER_ID;
      }
      else if (strFormName.equals(FORM_MODIFY_USER_NAME))
      {
         // Modify user
         iReturn = FORM_SAVE_USER_ID;
      }
      else 
      {
         iReturn = super.getFormToProcess(hsrqRequest);
      }

      return iReturn;
   }
   
   /**
    * Process form to display dialog to create new user. 
    * It supplies template object with default information for new user.
    *
    * @param  hsrqRequest  - the servlet request.
    * @param  hsrpResponse - the servlet response.
    * @throws ServletException - an error while serving request
    * @throws IOException - an error while writing response
    */
   protected void processNewUserForm(
      HttpServletRequest  hsrqRequest,
      HttpServletResponse hsrpResponse
   ) throws IOException,
            ServletException
   {
      s_logger.entering("UserServlet", "processNewUserForm");
      try
      {
         Object[] objUserWithRoles;
         User     data;

         objUserWithRoles = getController().getUserWithRoles(DataObject.NEW_ID);
         data = (User)objUserWithRoles[0];

         if (data != null)
         {
            List lstUserRoles;
         
            lstUserRoles = (List) objUserWithRoles[1];
            if (lstUserRoles == null)
            {
               lstUserRoles = Collections.EMPTY_LIST;
            }
   
            hsrqRequest.setAttribute(PARAMETER_USER_NAME, data);
            hsrqRequest.setAttribute(PARAMETER_ROLES_NAME, lstUserRoles);         
            displayUI(NEW_USER_PAGE, hsrqRequest, hsrpResponse);
         }
         else
         {
            messageBoxPage(hsrqRequest, hsrpResponse, "Error", 
               "An error has occurred while retrieving new user information.",
               hsrqRequest.getContextPath(), null);
         }
      }
      catch (Exception eExc)
      {
         s_logger.log(Level.WARNING, 
                      "An error has occurred while retrieving new user information.",
                      eExc);
         messageBoxPage(hsrqRequest, hsrpResponse, "Error", eExc.getMessage(),
                        hsrqRequest.getContextPath(), eExc.getCause());
      }
      finally
      {
         s_logger.exiting("UserServlet", "processNewUserForm");
      }
   }

   /**
    * Process form to display dialog to edit existing user. 
    * It supplies object with information for cpecified user.
    *
    * @param  hsrqRequest  - the servlet request.
    * @param  hsrpResponse - the servlet response.
    * @throws ServletException - an error while serving request
    * @throws IOException - an error while writing response
    */
   protected void processEditUserForm(
      HttpServletRequest  hsrqRequest,
      HttpServletResponse hsrpResponse
   ) throws IOException,
            ServletException
   {
      s_logger.entering("UserServlet", "processEditUserForm");
      try
      {
         int      userId = Integer.parseInt(hsrqRequest.getParameter("id"));
         Object[] objUserWithRoles;
         User     data;

         objUserWithRoles = getController().getUserWithRoles(userId);
         data = (User)objUserWithRoles[0];

         if (data != null)
         {
            List lstUserRoles;
         
            lstUserRoles = (List) objUserWithRoles[1];
            if (lstUserRoles == null)
            {
               lstUserRoles = Collections.EMPTY_LIST;
            }
   
            hsrqRequest.setAttribute(PARAMETER_USER_NAME, data);
            hsrqRequest.setAttribute(PARAMETER_ROLES_NAME, lstUserRoles);
            displayUI(EDIT_USER_PAGE, hsrqRequest, hsrpResponse);
         }
         else
         {
            hsrqRequest.setAttribute(PARAMETER_RELOAD_LIST, VALUE_RELOAD_LIST);            
            messageBoxPage(hsrqRequest, hsrpResponse, "Error", 
                           "Selected user already does not exist.",
                           hsrqRequest.getContextPath(), null);
         }
      }
      catch (Exception eExc)
      {
         s_logger.log(Level.WARNING, 
                      "An error has occurred while retrieving existing user information",
                      eExc);
         messageBoxPage(hsrqRequest, hsrpResponse, "Error", eExc.getMessage(),
                        hsrqRequest.getContextPath(), eExc.getCause());
      }
      finally
      {
         s_logger.exiting("UserServlet", "processEditUserForm");
      }
   }

   /**
    * Process form to create new user. 
    * It supplies object with newly created information for new user.
    *
    * @param  hsrqRequest  - the servlet request.
    * @param  hsrpResponse - the servlet response.
    * @throws ServletException - an error while serving request
    * @throws IOException - an error while writing response
    */
   protected void processCreateUserForm(
      HttpServletRequest  hsrqRequest,
      HttpServletResponse hsrpResponse
   ) throws IOException,
            ServletException
   {
      s_logger.entering("UserServlet", "processCreateUserForm");

      try
      {
         UserTransaction transaction = DatabaseTransactionFactoryImpl.getInstance().
                                          requestTransaction();
         User            user = parseUserFromRequest(hsrqRequest);
         User            userUpdated = null;
         List            lstUserRoles = null;
         String          redirectUIId;
         String          strIgnoredItems = hsrqRequest.getParameter("ROLE_IGNORED_ITEMS");
         boolean         bOK = false;
         
         transaction.begin();
         try
         {
            userUpdated = getController().create(user, strIgnoredItems);
            if (userUpdated != null)
            {
               transaction.commit();
               bOK = true;
            }
            else
            {
               TransactionUtils.rollback(transaction);
               bOK = false;
            }
         }
         catch (OSSInvalidDataException ideExc)
         {
            TransactionUtils.rollback(transaction);
            CallContext.getInstance().getMessages().addMessages(
                                                       ideExc.getErrorMessages());
            // Take the path of bOK = false
         }
         catch (OSSInvalidContextException iceExc)
         {
            TransactionUtils.rollback(transaction);
            CallContext.getInstance().getMessages().addMessage(
               Messages.NONSPECIFIC_ERRORS, iceExc.getMessage());
            // Since it looks like related data were deleted, reload the list
            // and since the state is undefined, just display the message box
            hsrqRequest.setAttribute(PARAMETER_RELOAD_LIST, VALUE_RELOAD_LIST);
            messageBoxPage(hsrqRequest, hsrpResponse, "Error", iceExc.getMessage(),
                           hsrqRequest.getContextPath(), iceExc.getCause());
            return;
         }
         catch (Throwable thr)
         {
            TransactionUtils.rollback(transaction);
            throw new ServletException(thr);            
         }
         
         if (bOK)
         {
            redirectUIId = EDIT_USER_PAGE;
            lstUserRoles = getController().getRoles(userUpdated.getId());
            // Since new role was created, reload the list
            hsrqRequest.setAttribute(PARAMETER_RELOAD_LIST, VALUE_RELOAD_LIST);                     
         }
         else
         {
            // Insufficient rights prevented from creation of the user
            // Start again from the new user page but prefill it with data
            redirectUIId = NEW_USER_PAGE;
            userUpdated = parseUserFromRequest(hsrqRequest);
            lstUserRoles = getRoleController().get(strIgnoredItems);
         }

         if (lstUserRoles == null)
         {
            lstUserRoles = Collections.EMPTY_LIST;
         }
         
         hsrqRequest.setAttribute(PARAMETER_USER_NAME, userUpdated);
         hsrqRequest.setAttribute(PARAMETER_ROLES_NAME, lstUserRoles);
         displayUI(redirectUIId, hsrqRequest, hsrpResponse);
      }
      catch (Exception eExc)
      {
         s_logger.log(Level.WARNING, 
                      "An error has occurred while creating new user.", eExc);
         messageBoxPage(hsrqRequest, hsrpResponse, "Error", eExc.getMessage(),
                        hsrqRequest.getContextPath(), eExc.getCause());
      }
      finally
      {
         s_logger.exiting("UserServlet", "processCreateUserForm");
      }
   }

   /**
    * Process form to modify existing user. 
    *
    * @param  hsrqRequest  - the servlet request.
    * @param  hsrpResponse - the servlet response.
    * @throws ServletException - an error while serving request
    * @throws IOException - an error while writing response
    */
   protected void processSaveUserForm(
      HttpServletRequest  hsrqRequest,
      HttpServletResponse hsrpResponse
   ) throws IOException,
            ServletException
   {
      s_logger.entering("UserServlet", "processSaveUserForm");
      try
      {
         UserTransaction transaction = DatabaseTransactionFactoryImpl.getInstance()
                                          .requestTransaction();
         User    user = parseUserFromRequest(hsrqRequest);
         User    userUpdated = null;
         List    lstUserRoles = null;
         String  strIgnoredItems = hsrqRequest.getParameter("ROLE_IGNORED_ITEMS");
         String  strRemoveRoleIds = hsrqRequest.getParameter("REMOVED_ROLE_IDS");
         String  strAddRoleIds = hsrqRequest.getParameter("ADD_ROLE_IDS");
         boolean bOK = false;
         boolean bKeepModifications = true;
         
         if (GlobalConstants.ERROR_CHECKING)
         {
            assert strAddRoleIds != null 
                     : "Add role IDs parameter can not be null";
            assert strAddRoleIds.startsWith(",") 
                     : "Add role IDs parameter should starts with ','";
            assert strAddRoleIds.endsWith(",") 
                     : "Add role IDs parameter should ends with ','";
            assert strRemoveRoleIds != null 
                     : "Remove role IDs parameter can not be null";
            assert strRemoveRoleIds.startsWith(",") 
                     : "Remove role IDs parameter should starts with ','";
            assert strRemoveRoleIds.endsWith(",") 
                     : "Remove role IDs parameter should ends with ','";
         }
         // We have to change String as ",11,67,3," to "11,67,3"
         // because then it can be used for SQL IN operator
         if (strRemoveRoleIds.length() > 2)
         {
            strRemoveRoleIds = strRemoveRoleIds.substring(1, strRemoveRoleIds.length() - 1);
         }
         else
         {
            strRemoveRoleIds = "";
         }

         if (strAddRoleIds.length() > 2)
         {
            strAddRoleIds = strAddRoleIds.substring(1, strAddRoleIds.length() - 1);
         }
         else
         {
            strAddRoleIds = "";
         }

         transaction.begin();
         try
         {
            userUpdated = getController().save(user, strRemoveRoleIds, strAddRoleIds);
            if (userUpdated != null)
            {
               transaction.commit();
               bOK = true;
            }
            else
            {
               TransactionUtils.rollback(transaction);
               bOK = false;               
            }
         }
         catch (OSSInvalidDataException ideExc)
         {
            TransactionUtils.rollback(transaction);
            CallContext.getInstance().getMessages().addMessages(
                                                       ideExc.getErrorMessages());
            // Take the path of bOK = false, bKeepModifications = true
         }
         catch (OSSConcurentModifyException cmeExc)
         {
            TransactionUtils.rollback(transaction);
            CallContext.getInstance().getMessages().addMessage(
               Messages.NONSPECIFIC_ERRORS, cmeExc.getMessage());
            // Somebody modified the role at the same time, just reload the new
            // data and at this time loose the old data
            bKeepModifications = false;
            // Reload list to see the data modified by somebody else
            hsrqRequest.setAttribute(PARAMETER_RELOAD_LIST, VALUE_RELOAD_LIST);
            // Take the path of bOK = false, bKeepModifications = false
         }
         catch (OSSInvalidContextException iceExc)
         {
            TransactionUtils.rollback(transaction);
            CallContext.getInstance().getMessages().addMessage(
               Messages.NONSPECIFIC_ERRORS, iceExc.getMessage());
            // Since it looks like related data were deleted, reload the list
            // and since the state is undefined, just display the message box
            hsrqRequest.setAttribute(PARAMETER_RELOAD_LIST, VALUE_RELOAD_LIST);
            messageBoxPage(hsrqRequest, hsrpResponse, "Error", iceExc.getMessage(),
                           hsrqRequest.getContextPath(), iceExc.getCause());
            return;
         }
         catch (Throwable thr)
         {
            TransactionUtils.rollback(transaction);
            throw new ServletException(thr);            
         }
         
         // Execute this outside of try catch since we do not want to confuse
         // the exception handling
         if (bOK)
         {
            lstUserRoles = getController().getRoles(userUpdated.getId());
            // Since new role was created, reload the list
            hsrqRequest.setAttribute(PARAMETER_RELOAD_LIST, VALUE_RELOAD_LIST);               
         }
         else
         {
            if (bKeepModifications)
            {
               userUpdated = parseUserFromRequest(hsrqRequest);
               lstUserRoles = getRoleController().get(strIgnoredItems);
               hsrqRequest.setAttribute("REMOVED_ROLE_IDS", 
                                       hsrqRequest.getParameter("REMOVED_ROLE_IDS"));
               hsrqRequest.setAttribute("ADD_ROLE_IDS", 
                                       hsrqRequest.getParameter("ADD_ROLE_IDS"));
            }
            else
            {
               userUpdated = (User)getController().get(user.getId());
               hsrqRequest.setAttribute(PARAMETER_RELOAD_LIST, VALUE_RELOAD_LIST);
               if (userUpdated == null)
               {
                  hsrqRequest.setAttribute(PARAMETER_RELOAD_LIST, VALUE_RELOAD_LIST);
                  messageBoxPage(hsrqRequest, hsrpResponse, "Error", 
                                 "An error has occurred while updating new user.",
                                 hsrqRequest.getContextPath(), null);
                  return;
               }
               else
               {
                  lstUserRoles = getController().getRoles(userUpdated.getId());
               }
            }
         }
         
         if (lstUserRoles == null)
         {
            lstUserRoles = Collections.EMPTY_LIST;
         }
                  
         hsrqRequest.setAttribute(PARAMETER_USER_NAME, userUpdated);
         hsrqRequest.setAttribute(PARAMETER_ROLES_NAME, lstUserRoles);
         displayUI(EDIT_USER_PAGE, hsrqRequest, hsrpResponse);
      }
      catch (Exception eExc)
      {
         s_logger.log(Level.WARNING, 
                      "An error has occurred while updating existing user.", eExc);
         hsrqRequest.setAttribute(PARAMETER_RELOAD_LIST, VALUE_RELOAD_LIST);
         messageBoxPage(hsrqRequest, hsrpResponse, "Error", eExc.getMessage(),
                        hsrqRequest.getContextPath(), eExc.getCause());
      }
      finally
      {
         s_logger.exiting("UserServlet", "processSaveUserForm");
      }
   }

   /**
    * Create new instance of user from a HTTP request.
    *
    * @param  hsrqRequest - HTTP request from HTML form
    * @return User
    * @throws  OSSException - an error has occurred
    */
   protected User parseUserFromRequest(
      HttpServletRequest hsrqRequest
   ) throws OSSException
   {
      String    strTemp;
      int       iUserId   = DataObject.NEW_ID;
      int       iDomainId = DataObject.NEW_ID;
      String    strFirstName = "";
      String    strLastName = "";
      String    strPhone = "";
      String    strFax = "";
      String    strEmail = "";
      String    strAddress = "";
      String    strLoginName = "";
      String    strPassword = "";
      boolean   bLoginEnabled = false;
      boolean   bSuperUser = false;
      boolean   bInternalUser = false;
      Timestamp modificationDate = null;
      
      iDomainId = CallContext.getInstance().getCurrentDomainId();

      strTemp = hsrqRequest.getParameter("USER_ID");
      if (strTemp != null)
      {
         iUserId = Integer.parseInt(strTemp);
      }
      strTemp = hsrqRequest.getParameter("FIRST_NAME");
      if (strTemp != null)
      {
         strFirstName = strTemp;
      }
      strTemp = hsrqRequest.getParameter("LAST_NAME");
      if (strTemp != null)
      {
         strLastName = strTemp;
      }
      strTemp = hsrqRequest.getParameter("PHONE");
      if (strTemp != null)
      {
         strPhone = strTemp;
      }
      strTemp = hsrqRequest.getParameter("FAX");
      if (strTemp != null)
      {
         strFax = strTemp;
      }
      strTemp = hsrqRequest.getParameter("EMAIL");
      if (strTemp != null)
      {
         strEmail = strTemp;
      }
      strTemp = hsrqRequest.getParameter("ADDRESS");
      if (strTemp != null)
      {
         strAddress = strTemp;
      }
      strTemp = hsrqRequest.getParameter("LOGIN_NAME");
      if (strTemp != null)
      {
         strLoginName = strTemp;
      }
      bLoginEnabled = hsrqRequest.getParameter("LOGIN_ENABLED") != null;
      bSuperUser = hsrqRequest.getParameter("SUPER_USER") != null;
      bInternalUser = hsrqRequest.getParameter("INTERNAL_USER")  != null;
      strTemp = hsrqRequest.getParameter("PASSWORD");
      if (strTemp != null)
      {
         strPassword = strTemp;
      }
      // User can never change modification date, therefore we require to store
      // it as the millisecond value since that is independent from the display format       
      strTemp = hsrqRequest.getParameter("MODIFICATION_DATE");
      if (strTemp != null)
      {
         modificationDate = DateUtils.parseTimestamp(strTemp);
      }

      return new User(iUserId, 
                      iDomainId,
                      strFirstName,
                      strLastName,
                      strPhone,
                      strFax,
                      strAddress,
                      strEmail,
                      strLoginName,
                      strPassword,
                      bLoginEnabled,
                      // TODO: Feature: Gui doesn't allow right now to create guest users
                      false,
                      bSuperUser,
                      bInternalUser,
                      null,
                      modificationDate,
                      true);
   }

   /**
    * Get controller to invoke business logic.
    * 
    * @return UserController
    * @throws OSSException - an error has occurred
    */
   protected UserController getController(
   ) throws OSSException
   {
      UserController controller;
      
      controller = (UserController)ControllerManager.getInstance(UserController.class);
      
      return controller;
   }

   /**
    * Get controller to invoke business logic.
    * 
    * @return RoleController
    * @throws OSSException - an error has occurred
    */
   protected RoleController getRoleController(
   ) throws OSSException
   {
      RoleController controller;
      
      controller = (RoleController)ControllerManager.getInstance(RoleController.class);
      
      return controller;
   }
}
