/*
 * Copyright (c) 2003 - 2008 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: RoleServlet.java,v 1.21 2009/04/22 06:29:14 bastafidli Exp $
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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
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
import org.opensubsystems.core.util.Log;
import org.opensubsystems.core.util.Messages;
import org.opensubsystems.core.util.StringUtils;
import org.opensubsystems.core.util.TransactionUtils;
import org.opensubsystems.core.www.servlet.WebUIServlet;
import org.opensubsystems.security.data.AccessRight;
import org.opensubsystems.security.data.Role;
import org.opensubsystems.security.logic.RoleController;
import org.opensubsystems.security.logic.SecurityDefinitionManager;
import org.opensubsystems.security.util.ActionConstants;

/**
 * This servlet handles viewing and editing of roles.
 *  
 * @version $Id: RoleServlet.java,v 1.21 2009/04/22 06:29:14 bastafidli Exp $
 * @author Miro Halas
 * @code.reviewer Miro Halas
 * @code.reviewed Initial revision
 */
public class RoleServlet extends WebUIServlet
{
   // Configuration settings ///////////////////////////////////////////////////

   /** 
    * Name of the property for page to create new role.
    */   
   public static final String NEW_ROLE_PAGE = "oss.role.create.page";
   
   /** 
    * Name of the property for page to edit existing role.
    */   
   public static final String EDIT_ROLE_PAGE = "oss.role.edit.page";
   
   /**
    * Name of the property for page to view existing role.
    */
   public static final String VIEW_ROLE_PAGE = "oss.role.view.page";

   // Constants ////////////////////////////////////////////////////////////////

   /**
    * Names of the form and action to display new role dialog.
    */
   public static final String FORM_NEW_ROLE_NAME = "create";

   /**
    * Names of the form and action to display edit role dialog.
    */
   public static final String FORM_EDIT_ROLE_NAME = "edit";

   /**
    * Names of the form and action to display new role dialog.
    */
   public static final String FORM_CREATE_ROLE_NAME = "FORM_CREATE_ROLE";

   /**
    * Names of the form and action to display edit role dialog.
    */
   public static final String FORM_MODIFY_ROLE_NAME = "FORM_MODIFY_ROLE";

   /**
    * Names of the form field 
    */
   public static final String FORM_ADDED_RIGHTS = "ADDED_RIGHTS";
   
   /**
    * Names of the form field 
    */
   public static final String FORM_DELETED_RIGHTS = "DELETED_RIGHTS";
   
   /**
    * Constants for action to display new role dialog. 
    */
   public static final int FORM_NEW_ROLE_ID = FORM_COUNT_WEBUI + 1;

   /**
    * Constants for action to display edit role dialog. 
    */
   public static final int FORM_EDIT_ROLE_ID = FORM_COUNT_WEBUI + 2;

   /**
    * Constants for action to create new role. 
    */
   public static final int FORM_CREATE_ROLE_ID = FORM_COUNT_WEBUI + 3;

   /**
    * Constants for action to modify existing role. 
    */
   public static final int FORM_MODIFY_ROLE_ID = FORM_COUNT_WEBUI + 4;

   /**
    * Constants for action to view existing role. 
    */
   public static final int FORM_VIEW_ROLE_ID = FORM_COUNT_WEBUI + 5;

   /**
    * How many forms this servlet recongizes. 
    */
   public static final int FORM_COUNT_ROLE = FORM_COUNT_WEBUI + 6;
   
   // Parameter Constants //////////////////////////////////////////////////////

   /**
    * Parameter representing data of the role 
    */
   public static final String PARAMETER_ROLE_NAME = "roledata";

   /**
    * Parameter representing security definitions 
    */
   public static final String PARAMETER_SECURITY_DEFINITIONS = "securitydefinitions";
   
   /**
    * Parameter signaling that the list of items if present should be reloaded.
    */
   public static final String PARAMETER_RELOAD_LIST = "reloadlist";
   
   /**
    * Name of the list of items that should be reloaded using PARAMETER_RELOAD_LIST.
    */
   public static final String VALUE_RELOAD_LIST = "rolelist";

   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Generated serial version id for this class.
    */
   private static final long serialVersionUID = 481687993757986840L;

   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(RoleServlet.class);

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
      
      // Load UI page for the main screen display
      cacheUIPath(prpSettings, NEW_ROLE_PAGE, "Page to create role");
      cacheUIPath(prpSettings, EDIT_ROLE_PAGE, "Page to edit role");
      cacheUIPath(prpSettings, VIEW_ROLE_PAGE, "Page to view role");
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
         case (FORM_NEW_ROLE_ID):
         {
            processNewRoleForm(hsrqRequest, hsrpResponse);
            break;
         }
         
         case (FORM_EDIT_ROLE_ID):
         {
            processEditOrViewRoleForm(hsrqRequest, hsrpResponse);
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
         case FORM_CREATE_ROLE_ID :
         {
            processCreateRoleForm(hsrqRequest, hsrpResponse);
            break;
         }
         
         case FORM_MODIFY_ROLE_ID :
         {
            processSaveRoleForm(hsrqRequest, hsrpResponse);
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
    * Examine request and find out what form needs to be processed
    *
    * @param  hsrqRequest - the servlet request, which is used to find out
    *                       what form needs to be processed
    * @return int - one of the FORM_XXX constants
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
      else if (strFormName.equals(FORM_NEW_ROLE_NAME))
      {
         // Display new role page
         iReturn = FORM_NEW_ROLE_ID;
      }
      else if (strFormName.equals(FORM_EDIT_ROLE_NAME))
      {
         // Display edit role page
         iReturn = FORM_EDIT_ROLE_ID;
      }
      else if (strFormName.equals(FORM_CREATE_ROLE_NAME))
      {
         // Create role
         iReturn = FORM_CREATE_ROLE_ID;
      }
      else if (strFormName.equals(FORM_MODIFY_ROLE_NAME))
      {
         // Modify role
         iReturn = FORM_MODIFY_ROLE_ID;
      }
      else
      {
         iReturn = super.getFormToProcess(hsrqRequest);
      }

      return iReturn;
   }

   /**
    * Process form to display dialog to create new role. 
    * It supplies template object with default information for new role.
    *
    * @param  hsrqRequest  - the servlet request.
    * @param  hsrpResponse - the servlet response.
    * @throws ServletException - an error while serving request
    * @throws IOException - an error while writing response
    */
   protected void processNewRoleForm(
      HttpServletRequest  hsrqRequest,
      HttpServletResponse hsrpResponse
   ) throws IOException,
            ServletException
   {
      s_logger.entering("RoleServlet", "processNewRoleForm");
      try
      {
         Role data = (Role)getController().get(DataObject.NEW_ID);
         
         if (data != null)
         {
            hsrqRequest.setAttribute(PARAMETER_ROLE_NAME, data);
            hsrqRequest.setAttribute(PARAMETER_SECURITY_DEFINITIONS, 
               SecurityDefinitionManager.getInstance().getDataTypeSecurityDefinitions());
            displayUI(NEW_ROLE_PAGE, hsrqRequest, hsrpResponse);
         }
         else
         {
            messageBoxPage(hsrqRequest, hsrpResponse, "Error", 
               "An error has occurred while retrieving new role information.",
               hsrqRequest.getContextPath(), null);
         }
      }
      catch (Exception eExc)
      {
         s_logger.log(Level.WARNING, 
                      "An error has occurred while retrieving new role information", 
                      eExc);
         messageBoxPage(hsrqRequest, hsrpResponse, "Error", eExc.getMessage(),
                        hsrqRequest.getContextPath(), eExc.getCause());
      }
      finally
      {
         s_logger.exiting("RoleServlet", "processNewRoleForm");
      }
   }

   /**
    * Process form to display dialog to edit existing role. 
    * It supplies object with information for specified role.
    *
    * @param  hsrqRequest  - the servlet request.
    * @param  hsrpResponse - the servlet response.
    * @throws ServletException - an error while serving request
    * @throws IOException - an error while writing response
    */
   protected void processEditOrViewRoleForm(
      HttpServletRequest  hsrqRequest,
      HttpServletResponse hsrpResponse
   ) throws IOException,
            ServletException
   {
      s_logger.entering("RoleServlet", "processEditRoleForm");
      try
      {
         int           roleId = Integer.parseInt(hsrqRequest.getParameter("id"));
         Role            data = (Role)getController().get(roleId);

         if (data != null)
         {
            hsrqRequest.setAttribute(PARAMETER_ROLE_NAME, data);
            hsrqRequest.setAttribute(PARAMETER_SECURITY_DEFINITIONS, 
               SecurityDefinitionManager.getInstance().getDataTypeSecurityDefinitions());

            if (data.isUnmodifiable())
            {
               displayUI(VIEW_ROLE_PAGE, hsrqRequest, hsrpResponse);
            }
            else
            {
               displayUI(EDIT_ROLE_PAGE, hsrqRequest, hsrpResponse);
            }
         }
         else
         {
            hsrqRequest.setAttribute(PARAMETER_RELOAD_LIST, VALUE_RELOAD_LIST);            
            messageBoxPage(hsrqRequest, hsrpResponse, "Error", 
                           "Selected role does not exist.",
                           hsrqRequest.getContextPath(), null);
         }
      }
      catch (Exception eExc)
      {
         s_logger.log(Level.WARNING, 
                      "An error has occurred while retrieving existing role information.",
                      eExc);
         messageBoxPage(hsrqRequest, hsrpResponse, "Error", eExc.getMessage(),
                        hsrqRequest.getContextPath(), eExc.getCause());
      }
      finally
      {
         s_logger.exiting("RoleServlet", "processEditRoleForm");
      }
   }
   
   /**
    * Process form to create specified role.
    *
    * @param  hsrqRequest  - the servlet request.
    * @param  hsrpResponse - the servlet response.
    * @throws ServletException - an error while serving request
    * @throws IOException - an error while writing response
    */
   protected void processCreateRoleForm(
      HttpServletRequest  hsrqRequest,
      HttpServletResponse hsrpResponse
   ) throws IOException,
            ServletException
   {
      s_logger.entering("RoleServlet", "processCreateRoleForm");
      try
      {
         UserTransaction transaction = DatabaseTransactionFactoryImpl.getInstance()
                                          .requestTransaction();
         Role            role = parseRoleFromRequest(hsrqRequest);
         Role            roleUpdated = null;
         String          redirectUIId;
         List            addedRights = parseAdded(hsrqRequest, role.getId());
         boolean         bOK = false;

         role.setAccessRights(addedRights);
         transaction.begin();
         try
         {
            roleUpdated = (Role)getController().create(role);
            if (roleUpdated != null)
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

         // Execute this outside of try catch since we do not want to confuse
         // the exception handling
         if (bOK)
         {
            redirectUIId = EDIT_ROLE_PAGE;
            // TODO: Performance: Why are we retrieving the role here again
            // when the save already returned it to us
            roleUpdated = (Role)getController().get(roleUpdated.getId());
            // Since new role was created, reload the list
            hsrqRequest.setAttribute(PARAMETER_RELOAD_LIST, VALUE_RELOAD_LIST);                     
         }
         else
         {
            // Insufficient rights prevented from creation of the role
            // Start again from the new role page but prefill it with data
            redirectUIId = NEW_ROLE_PAGE;
            roleUpdated = parseRoleFromRequest(hsrqRequest);
            roleUpdated.setAccessRights(addedRights);
            hsrqRequest.setAttribute(FORM_ADDED_RIGHTS, 
                                     hsrqRequest.getParameter(FORM_ADDED_RIGHTS));
         }
         
         hsrqRequest.setAttribute(PARAMETER_ROLE_NAME, roleUpdated);
         hsrqRequest.setAttribute(PARAMETER_SECURITY_DEFINITIONS, 
            SecurityDefinitionManager.getInstance().getDataTypeSecurityDefinitions());
         displayUI(redirectUIId, hsrqRequest, hsrpResponse);
      }
      catch (Exception eExc)
      {
         s_logger.log(Level.WARNING, 
                      "An error has occurred while creating new role.", eExc);
         messageBoxPage(hsrqRequest, hsrpResponse, "Error", eExc.getMessage(),
                        hsrqRequest.getContextPath(), eExc.getCause());
      }
      finally
      {
         s_logger.exiting("RoleServlet", "processCreateRoleForm");
      }
   }
   
   /**
    * Process form to modify specified role.
    *
    * @param  hsrqRequest  - the servlet request.
    * @param  hsrpResponse - the servlet response.
    * @throws ServletException - an error while serving request
    * @throws IOException - an error while writing response
    */
   protected void processSaveRoleForm(
      HttpServletRequest  hsrqRequest,
      HttpServletResponse hsrpResponse
   ) throws IOException,
            ServletException
   {
      s_logger.entering("RoleServlet", "processSaveRoleForm");
      try
      {
         UserTransaction transaction = DatabaseTransactionFactoryImpl.getInstance()
                                          .requestTransaction();
         Role            role = parseRoleFromRequest(hsrqRequest);
         Role            roleUpdated = null;
         Role            actualRole = null;
         String          deletedRights = hsrqRequest.getParameter(FORM_DELETED_RIGHTS);
         List            addedRights = parseAdded(hsrqRequest, role.getId());
         boolean         bOK = false;
         boolean         bKeepModifications = true;
         
         transaction.begin();
         try
         {
            roleUpdated = getController().save(role, deletedRights, addedRights);
            if (roleUpdated != null)
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
         
         // Execute this outside of try catch since we do not want to confuse
         // the exception handling
         if (bOK)
         {
            // TODO: Performance: Why are we retrieving the role here again
            // when the save already returned it to us
            roleUpdated = (Role)getController().get(role.getId());
            // Since new role was created, reload the list
            hsrqRequest.setAttribute(PARAMETER_RELOAD_LIST, VALUE_RELOAD_LIST);               
         }
         else
         {
            // Start again from the edit role page but prefill it with data
            actualRole = (Role)getController().get(role.getId());
            if (bKeepModifications)
            {
               roleUpdated = parseRoleFromRequest(hsrqRequest);
               if (actualRole == null)
               {
                  roleUpdated.setAccessRights(new ArrayList());
               }                        
               else
               {
                  roleUpdated.setAccessRights(actualRole.getAccessRights());
                  roleUpdated.removeAccessRights(
                                 StringUtils.parseStringToIntArray(deletedRights, ","));
               }
               roleUpdated.addAccessRights(addedRights);
               hsrqRequest.setAttribute(FORM_ADDED_RIGHTS, 
                                        hsrqRequest.getParameter(FORM_ADDED_RIGHTS));
               hsrqRequest.setAttribute(FORM_DELETED_RIGHTS, 
                                        hsrqRequest.getParameter(FORM_DELETED_RIGHTS));
            }
            else
            {
               // TODO: For Julo: Handle if actualRole = null if it was deleted meanwhile  
               roleUpdated = actualRole;
            }
         }
         
         hsrqRequest.setAttribute(PARAMETER_ROLE_NAME, roleUpdated);
         hsrqRequest.setAttribute(PARAMETER_SECURITY_DEFINITIONS, 
            SecurityDefinitionManager.getInstance().getDataTypeSecurityDefinitions());
         displayUI(EDIT_ROLE_PAGE, hsrqRequest, hsrpResponse);
      }
      catch (Exception eExc)
      {
         s_logger.log(Level.WARNING, 
                      "An error has occurred while updating existing role.", eExc);
         hsrqRequest.setAttribute(PARAMETER_RELOAD_LIST, VALUE_RELOAD_LIST);
         messageBoxPage(hsrqRequest, hsrpResponse, "Error", eExc.getMessage(),
                        hsrqRequest.getContextPath(), eExc.getCause());
      }
      finally
      {
         s_logger.exiting("RoleServlet", "processSaveRoleForm");
      }
   }
      
   /**
    * Create new instance of role from a HTTP request.
    *
    * @param  hsrqRequest - HTTP request from HTML form
    * @return Role
    * @throws  OSSException - an error has occurred
    */
   protected Role parseRoleFromRequest(
      HttpServletRequest hsrqRequest
   ) throws OSSException
   {
      String    strTemp;
      int       iRoleId        = DataObject.NEW_ID;
      int       iDomainId      = DataObject.NEW_ID;
      String    strName        = "";
      String    strDescription = "";
      boolean   bEnabled       = false;
      boolean   bUnmodifiable  = false;
      Timestamp tsModify       = null;
      int       iUserId        = DataObject.NEW_ID;
      
      iDomainId = CallContext.getInstance().getCurrentDomainId();
      
      strTemp = hsrqRequest.getParameter("ROLE_ID");
      if (strTemp != null)
      {
         iRoleId = Integer.parseInt(strTemp);   
      }
      strTemp = hsrqRequest.getParameter("ROLE_NAME");
      if (strTemp != null)
      {
         strName = strTemp;   
      }
      strTemp = hsrqRequest.getParameter("ROLE_DESCRIPTION");
      if (strTemp != null)
      {
         strDescription = strTemp;   
      }
      strTemp = hsrqRequest.getParameter("ROLE_ENABLED");
      if (strTemp != null)
      {
         bEnabled = true;   
      }
      strTemp = hsrqRequest.getParameter("ROLE_UNMODIFIABLE");
      if (strTemp != null)
      {
         bUnmodifiable = true;
      }
      strTemp = hsrqRequest.getParameter("MODIFICATION_DATE");
      if (strTemp != null)
      {
         tsModify = DateUtils.parseTimestamp(strTemp);   
      }
      strTemp = hsrqRequest.getParameter("USER_ID");
      if (strTemp != null) 
      {
         iUserId = Integer.parseInt(strTemp);
      }
      
      return new Role(iRoleId, iDomainId, strName, strDescription,
                      bEnabled, iUserId, bUnmodifiable, null, null, tsModify);
   }  
   
   /**
    * Parse added access right parameter to the list of Access Rights
    * 
    * @param hsrqRequest - request with FORM_ADDED_RIGHTS parameter
    * @param roleId - id of role these parameters belong to 
    * @return List - list of parsed AccessRight objects 
    * @throws OSSException - an error has occurred
    */
   public List parseAdded(
      HttpServletRequest  hsrqRequest,
      int                 roleId
   ) throws OSSException
   {
      String strAdded = hsrqRequest.getParameter(FORM_ADDED_RIGHTS);
      if (strAdded == null)
      {
         return null;
      }
      
      StringTokenizer strTokAdded = new StringTokenizer(strAdded, ";");
      StringTokenizer strTokRight;
      List lstRights = new ArrayList();
      
      int iId = DataObject.NEW_ID;
      int iDomainId = CallContext.getInstance().getCurrentDomainId();
      int iAction = ActionConstants.NO_RIGHT_ACTION;
      int iDataType = DataObject.NO_DATA_TYPE;
      int iCategory = AccessRight.NO_RIGHT_CATEGORY;
      int iIdentifier = AccessRight.NO_RIGHT_IDENTIFIER;
      
      String strHelp;
      int indexOfDots = 0;
      while (strTokAdded.hasMoreTokens())
      {
         strHelp = strTokAdded.nextToken();
         indexOfDots = strHelp.indexOf(":");

         // TODO: Performance: Recode this to use StringBuffer instead of 
         // + and indexOf instead of creating new instance of StringTokenized
         // every time
         strHelp = strHelp.substring(0, indexOfDots) + 
                   "," + strHelp.substring(indexOfDots + 1, 
                                           strHelp.length());
         strTokRight = new StringTokenizer(strHelp, ",");
         
         if (strTokRight.hasMoreElements())
         {
            //iId = Integer.parseInt(strTokRight.nextToken());
            strTokRight.nextToken();
         }
         if (strTokRight.hasMoreElements())
         {
            iDataType = Integer.parseInt(strTokRight.nextToken());
         }
         if (strTokRight.hasMoreElements())
         {
            iAction = Integer.parseInt(strTokRight.nextToken());
         }
         if (strTokRight.hasMoreElements())
         {
            iCategory = Integer.parseInt(strTokRight.nextToken());
         }
         if (strTokRight.hasMoreElements())
         {
            iIdentifier = Integer.parseInt(strTokRight.nextToken());
         }
         else
         {
            throw new IllegalArgumentException("Not enough elements for Access Right parsing");
         }
         lstRights.add(new AccessRight(iId,
                                       roleId,
                                       iDomainId,
                                       iAction,
                                       iDataType,
                                       AccessRight.ACCESS_GRANTED,
                                       iCategory,
                                       iIdentifier,
                                       null,
                                       null));
      }
      
      return lstRights;  
   }      

   /**
    * Get controller to invoke business logic.
    * 
    * @return RoleController
    * @throws OSSException - an error has occurred
    */
   protected RoleController getController(
   ) throws OSSException
   {
      RoleController controller;
      
      controller = (RoleController)ControllerManager.getInstance(RoleController.class);
      
      return controller;
   }
}
