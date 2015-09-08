/*
 * Copyright (c) 2006 - 2008 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: DomainServlet.java,v 1.20 2009/04/22 06:29:14 bastafidli Exp $
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
import org.opensubsystems.security.data.Domain;
import org.opensubsystems.security.data.DomainDataDescriptor;
import org.opensubsystems.security.logic.DomainController;
import org.opensubsystems.security.logic.RoleController;

/**
 * This servlet handles viewing and editing of domains.
 *  
 * @version $Id: DomainServlet.java,v 1.20 2009/04/22 06:29:14 bastafidli Exp $
 * @author Julian Legeny
 * @code.reviewer TODO: Review this code
 * @code.reviewed
 */
public class DomainServlet extends WebUIServlet
{
   // Configuration settings ///////////////////////////////////////////////////

   /** 
    * Name of the property for page to create new domain.
    */   
   public static final String NEW_DOMAIN_PAGE = "oss.domain.create.page";
   
   /** 
    * Name of the property for page to edit existing domain.
    */   
   public static final String EDIT_DOMAIN_PAGE = "oss.domain.edit.page";
   
   // Constants ////////////////////////////////////////////////////////////////

   /**
    * Names of the form and action to display new domain dialog.
    */
   public static final String FORM_NEW_DOMAIN_NAME = "create";

   /**
    * Names of the form and action to display edit domain dialog.
    */
   public static final String FORM_EDIT_DOMAIN_NAME = "edit";

   /**
    * Names of the form and action to display new domain dialog.
    */
   public static final String FORM_CREATE_DOMAIN_NAME = "FORM_CREATE_DOMAIN";

   /**
    * Names of the form and action to display edit domain dialog.
    */
   public static final String FORM_MODIFY_DOMAIN_NAME = "FORM_MODIFY_DOMAIN";

   /**
    * Constants for action to display new domain dialog. 
    */
   public static final int FORM_NEW_DOMAIN_ID = FORM_COUNT_WEBUI + 1;

   /**
    * Constants for action to display edit domain dialog. 
    */
   public static final int FORM_EDIT_DOMAIN_ID = FORM_COUNT_WEBUI + 2;

   /**
    * Constants for action to create new domain. 
    */
   public static final int FORM_CREATE_DOMAIN_ID = FORM_COUNT_WEBUI + 3;

   /**
    * Constants for action to modify existing domain. 
    */
   public static final int FORM_MODIFY_DOMAIN_ID = FORM_COUNT_WEBUI + 4;

   /**
    * Constants for action to view existing domain. 
    */
   public static final int FORM_VIEW_DOMAIN_ID = FORM_COUNT_WEBUI + 5;

   /**
    * How many forms this servlet recongizes. 
    */
   public static final int FORM_COUNT_DOMAIN = FORM_COUNT_WEBUI + 6;
   
   // Parameter Constants //////////////////////////////////////////////////////

   /**
    * Parameter representing data of the domain 
    */
   public static final String PARAMETER_DOMAIN_NAME = "domaindata";

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
   public static final String VALUE_RELOAD_LIST = "domainlist";

   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Generated serial version id for this class.
    */
   private static final long serialVersionUID = 8852354988270224327L;

   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(DomainServlet.class);

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
      cacheUIPath(prpSettings, NEW_DOMAIN_PAGE, "Page to create domain");
      cacheUIPath(prpSettings, EDIT_DOMAIN_PAGE, "Page to edit domain");
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
         case (FORM_NEW_DOMAIN_ID):
         {
            processNewDomainForm(hsrqRequest, hsrpResponse);
            break;
         }
         
         case (FORM_EDIT_DOMAIN_ID):
         {
            processEditOrViewDomainForm(hsrqRequest, hsrpResponse);
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
         case FORM_CREATE_DOMAIN_ID :
         {
            processCreateDomainForm(hsrqRequest, hsrpResponse);
            break;
         }
         
         case FORM_MODIFY_DOMAIN_ID :
         {
            processSaveDomainForm(hsrqRequest, hsrpResponse);
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
      else if (strFormName.equals(FORM_NEW_DOMAIN_NAME))
      {
         // Display new domain page
         iReturn = FORM_NEW_DOMAIN_ID;
      }
      else if (strFormName.equals(FORM_EDIT_DOMAIN_NAME))
      {
         // Display edit domain page
         iReturn = FORM_EDIT_DOMAIN_ID;
      }
      else if (strFormName.equals(FORM_CREATE_DOMAIN_NAME))
      {
         // Create domain
         iReturn = FORM_CREATE_DOMAIN_ID;
      }
      else if (strFormName.equals(FORM_MODIFY_DOMAIN_NAME))
      {
         // Modify domain
         iReturn = FORM_MODIFY_DOMAIN_ID;
      }
      else
      {
         iReturn = super.getFormToProcess(hsrqRequest);
      }

      return iReturn;
   }

   /**
    * Process form to display dialog to create new domain. 
    * It supplies template object with default information for new domain.
    *
    * @param  hsrqRequest  - the servlet request.
    * @param  hsrpResponse - the servlet response.
    * @throws ServletException - an error while serving request
    * @throws IOException - an error while writing response
    */
   protected void processNewDomainForm(
      HttpServletRequest  hsrqRequest,
      HttpServletResponse hsrpResponse
   ) throws IOException,
            ServletException
   {
      s_logger.entering("DomainServlet", "processNewDomainForm");
      try
      {
         Domain data = (Domain)getController().getDomainWithAssociatedData(DataObject.NEW_ID);
         
         if (data != null)
         {
            List lstDefaultUserRoles = null;
            lstDefaultUserRoles = data.getDefaultRoles();
            if (lstDefaultUserRoles == null)
            {
               lstDefaultUserRoles = Collections.EMPTY_LIST;
            }
            hsrqRequest.setAttribute(PARAMETER_DOMAIN_NAME, data);
            hsrqRequest.setAttribute(PARAMETER_ROLES_NAME, lstDefaultUserRoles);
            displayUI(NEW_DOMAIN_PAGE, hsrqRequest, hsrpResponse);
         }
         else
         {
            messageBoxPage(hsrqRequest, hsrpResponse, "Error", 
               "An error has occurred while retrieving new domain information.",
               hsrqRequest.getContextPath(), null);
         }
      }
      catch (Exception eExc)
      {
         s_logger.log(Level.WARNING, 
                      "An error has occurred while retrieving new domain information", 
                      eExc);
         messageBoxPage(hsrqRequest, hsrpResponse, "Error", eExc.getMessage(),
                        hsrqRequest.getContextPath(), eExc.getCause());
      }
      finally
      {
         s_logger.exiting("DomainServlet", "processNewDomainForm");
      }
   }

   /**
    * Process form to display dialog to edit existing domain. 
    * It supplies object with information for specified domain.
    *
    * @param  hsrqRequest  - the servlet request.
    * @param  hsrpResponse - the servlet response.
    * @throws ServletException - an error while serving request
    * @throws IOException - an error while writing response
    */
   protected void processEditOrViewDomainForm(
      HttpServletRequest  hsrqRequest,
      HttpServletResponse hsrpResponse
   ) throws IOException,
            ServletException
   {
      s_logger.entering("DomainServlet", "processEditDomainForm");
      try
      {
         int  domainId = Integer.parseInt(hsrqRequest.getParameter("id"));
         Domain data = (Domain)getController().getDomainWithAssociatedData(domainId);

         if (data != null)
         {
            List lstDefaultUserRoles = null;
            lstDefaultUserRoles = data.getDefaultRoles();
            if (lstDefaultUserRoles == null)
            {
               lstDefaultUserRoles = Collections.EMPTY_LIST;
            }
            hsrqRequest.setAttribute(PARAMETER_DOMAIN_NAME, data);
            hsrqRequest.setAttribute(PARAMETER_ROLES_NAME, lstDefaultUserRoles);
            displayUI(EDIT_DOMAIN_PAGE, hsrqRequest, hsrpResponse);
         }
         else
         {
            hsrqRequest.setAttribute(PARAMETER_RELOAD_LIST, VALUE_RELOAD_LIST);            
            messageBoxPage(hsrqRequest, hsrpResponse, "Error", 
                           "Selected domain does not exist.",
                           hsrqRequest.getContextPath(), null);
         }
      }
      catch (Exception eExc)
      {
         s_logger.log(Level.WARNING, 
                      "An error has occurred while retrieving existing domain information.",
                      eExc);
         messageBoxPage(hsrqRequest, hsrpResponse, "Error", eExc.getMessage(),
                        hsrqRequest.getContextPath(), eExc.getCause());
      }
      finally
      {
         s_logger.exiting("DomainServlet", "processEditDomainForm");
      }
   }
   
   /**
    * Process form to create specified domain.
    *
    * @param  hsrqRequest  - the servlet request.
    * @param  hsrpResponse - the servlet response.
    * @throws ServletException - an error while serving request
    * @throws IOException - an error while writing response
    */
   protected void processCreateDomainForm(
      HttpServletRequest  hsrqRequest,
      HttpServletResponse hsrpResponse
   ) throws IOException,
            ServletException
   {
      s_logger.entering("DomainServlet", "processCreateDomainForm");
      try
      {
         UserTransaction transaction = DatabaseTransactionFactoryImpl.getInstance()
                                          .requestTransaction();
         Domain  domain = parseDomainFromRequest(hsrqRequest);
         Domain  domainUpdated = null;
         List    lstDefaultUserRoles = null;
         String  redirectUIId;
         String  strIgnoredItems = hsrqRequest.getParameter("ROLE_IGNORED_ITEMS");
         boolean bOK = false;

         transaction.begin();
         try
         {
            domainUpdated = (Domain)getController().create(domain, strIgnoredItems);
            if (domainUpdated != null)
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
            redirectUIId = EDIT_DOMAIN_PAGE;
            // get list of default roles from created domain object (during
            // create process new roles were copied and set up to domain object) 
            lstDefaultUserRoles = domainUpdated.getDefaultRoles();
            // Since new domain was created, reload the list
            hsrqRequest.setAttribute(PARAMETER_RELOAD_LIST, VALUE_RELOAD_LIST);                     
         }
         else
         {
            // Insufficient rights prevented from creation of the domain
            // Start again from the new domain page but prefill it with data
            redirectUIId = NEW_DOMAIN_PAGE;
            domainUpdated = parseDomainFromRequest(hsrqRequest);
            lstDefaultUserRoles = getRoleController().get(strIgnoredItems);
         }

         if (lstDefaultUserRoles == null)
         {
            lstDefaultUserRoles = Collections.EMPTY_LIST;
         }

         hsrqRequest.setAttribute(PARAMETER_DOMAIN_NAME, domainUpdated);
         hsrqRequest.setAttribute(PARAMETER_ROLES_NAME, lstDefaultUserRoles);
         displayUI(redirectUIId, hsrqRequest, hsrpResponse);
      }
      catch (Exception eExc)
      {
         s_logger.log(Level.WARNING, 
                      "An error has occurred while creating new domain.", eExc);
         messageBoxPage(hsrqRequest, hsrpResponse, "Error", eExc.getMessage(),
                        hsrqRequest.getContextPath(), eExc.getCause());
      }
      finally
      {
         s_logger.exiting("DomainServlet", "processCreateDomainForm");
      }
   }
   
   /**
    * Process form to modify specified domain.
    *
    * @param  hsrqRequest  - the servlet request.
    * @param  hsrpResponse - the servlet response.
    * @throws ServletException - an error while serving request
    * @throws IOException - an error while writing response
    */
   protected void processSaveDomainForm(
      HttpServletRequest  hsrqRequest,
      HttpServletResponse hsrpResponse
   ) throws IOException,
            ServletException
   {
      s_logger.entering("DomainServlet", "processSaveDomainForm");
      try
      {
         UserTransaction transaction = DatabaseTransactionFactoryImpl.getInstance()
                                          .requestTransaction();
         Domain  domain = parseDomainFromRequest(hsrqRequest);
         Domain  domainUpdated = null;
         Domain  actualDomain = null;
         List    lstDefaultUserRoles = null;
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
            domainUpdated = (Domain)getController().save(
                                       domain, strRemoveRoleIds, strAddRoleIds);
            if (domainUpdated != null)
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
            // Somebody modified the domain at the same time, just reload the new
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
            lstDefaultUserRoles = getController().getRoles(domainUpdated.getId());
            // Since new domain was created, reload the list
            hsrqRequest.setAttribute(PARAMETER_RELOAD_LIST, VALUE_RELOAD_LIST);               
         }
         else
         {
            // Start again from the edit domain page but prefill it with data
            actualDomain = (Domain)getController().get(domain.getId());
            if (bKeepModifications)
            {
               domainUpdated = parseDomainFromRequest(hsrqRequest);
               // set up back administration for current domain using value from 
               // actualDomain object, because if this flag will be changed within 
               // the object to false value there will be hidden some administration
               // checkbox on the GUI
               domainUpdated.setAdministration(actualDomain.isAdministration());
               lstDefaultUserRoles = getRoleController().get(strIgnoredItems);
               hsrqRequest.setAttribute("REMOVED_ROLE_IDS", 
                                       hsrqRequest.getParameter("REMOVED_ROLE_IDS"));
               hsrqRequest.setAttribute("ADD_ROLE_IDS", 
                                       hsrqRequest.getParameter("ADD_ROLE_IDS"));
            }
            else
            {
               domainUpdated = actualDomain;
               hsrqRequest.setAttribute(PARAMETER_RELOAD_LIST, VALUE_RELOAD_LIST);
               if (domainUpdated == null)
               {
                  hsrqRequest.setAttribute(PARAMETER_RELOAD_LIST, VALUE_RELOAD_LIST);
                  messageBoxPage(hsrqRequest, hsrpResponse, "Error", 
                                 "An error has occurred while updating domain.",
                                 hsrqRequest.getContextPath(), null);
                  return;
               }
               else
               {
                  lstDefaultUserRoles = getController().getRoles(domainUpdated.getId());
               }
            }
         }
         
         if (lstDefaultUserRoles == null)
         {
            lstDefaultUserRoles = Collections.EMPTY_LIST;
         }
                  
         hsrqRequest.setAttribute(PARAMETER_DOMAIN_NAME, domainUpdated);
         hsrqRequest.setAttribute(PARAMETER_ROLES_NAME, lstDefaultUserRoles);
         displayUI(EDIT_DOMAIN_PAGE, hsrqRequest, hsrpResponse);
      }
      catch (Exception eExc)
      {
         s_logger.log(Level.WARNING, 
                      "An error has occurred while updating existing domain.", eExc);
         hsrqRequest.setAttribute(PARAMETER_RELOAD_LIST, VALUE_RELOAD_LIST);
         messageBoxPage(hsrqRequest, hsrpResponse, "Error", eExc.getMessage(),
                        hsrqRequest.getContextPath(), eExc.getCause());
      }
      finally
      {
         s_logger.exiting("DomainServlet", "processSaveDomainForm");
      }
   }
      
   /**
    * Create new instance of domain from a HTTP request.
    *
    * @param  hsrqRequest - HTTP request from HTML form
    * @return Domain
    * @throws OSSException - an error has occurred
    */
   protected Domain parseDomainFromRequest(
      HttpServletRequest hsrqRequest
   ) throws OSSException
   {
      String    strTemp;
      int       iDomainId              = DataObject.NEW_ID;
      String    strName                = "";
      String    strDescription         = "";
      boolean   bEnabled               = DomainDataDescriptor.DOMAIN_DEFAULT_ENABLED;
      boolean   bAdministration        = DomainDataDescriptor.DOMAIN_DEFAULT_ADMINISTRATION;
      boolean   bAllowSelfRegistration = DomainDataDescriptor.DOMAIN_DEFAULT_ALLOW_SELFREG;
      boolean   bPublicDomain          = DomainDataDescriptor.DOMAIN_DEFAULT_PUBLIC;
      boolean   bDefaultLoginEnabled   = DomainDataDescriptor.DOMAIN_DEFAULT_LOGIN_ENABLED;
      boolean   bDefaultSuperUser      = DomainDataDescriptor.DOMAIN_DEFAULT_SUPER_USER;
      boolean   bDefaultInternalUser   = DomainDataDescriptor.DOMAIN_DEFAULT_INTERNAL_USER;
      String    strDefaultPhone        = DomainDataDescriptor.DOMAIN_DEFAULT_PHONE;
      String    strDefaultAddress      = DomainDataDescriptor.DOMAIN_DEFAULT_ADDRESS;
      Timestamp modificationDate       = null;

      strTemp = hsrqRequest.getParameter("DOMAIN_ID");
      if (strTemp != null)
      {
         iDomainId = Integer.parseInt(strTemp);   
      }
      strTemp = hsrqRequest.getParameter("DOMAIN_NAME");
      if (strTemp != null)
      {
         strName = strTemp;   
      }
      strTemp = hsrqRequest.getParameter("DOMAIN_DESCRIPTION");
      if (strTemp != null)
      {
         strDescription = strTemp;   
      }
      strTemp = hsrqRequest.getParameter("DOMAIN_ENABLED");
      if (strTemp != null)
      {
         bEnabled = true;   
      }
      else
      {
         bEnabled = false;
      }
      strTemp = hsrqRequest.getParameter("DOMAIN_ADMINISTRATION");
      if (strTemp != null)
      {
         bAdministration = true;
      }
      else
      {
         bAdministration = false;
      }
      strTemp = hsrqRequest.getParameter("DOMAIN_ALLOW_SELFREG");
      if (strTemp != null)
      {
         bAllowSelfRegistration = true;
      }
      else
      {
         bAllowSelfRegistration = false;
      }
      strTemp = hsrqRequest.getParameter("DOMAIN_PUBLIC");
      if (strTemp != null)
      {
         bPublicDomain = true;   
      }
      else
      {
         bPublicDomain = false;
      }

      strTemp = hsrqRequest.getParameter("DOMAIN_LOGIN_ENABLED");
      if (strTemp != null)
      {
         bDefaultLoginEnabled = true;
      }
      else
      {
         bDefaultLoginEnabled = false;
      }
      strTemp = hsrqRequest.getParameter("DOMAIN_SUPER_USER");
      if (strTemp != null)
      {
         bDefaultSuperUser = true;
      }
      else
      {
         bDefaultSuperUser = false;
      }
      strTemp = hsrqRequest.getParameter("DOMAIN_INTERNAL_USER");
      if (strTemp != null)
      {
         bDefaultInternalUser = true;
      }
      else
      {
         bDefaultInternalUser = false;
      }
      strTemp = hsrqRequest.getParameter("DOMAIN_PHONE");
      if (strTemp != null)
      {
         strDefaultPhone = strTemp;   
      }
      strTemp = hsrqRequest.getParameter("DOMAIN_ADDRESS");
      if (strTemp != null)
      {
         strDefaultAddress = strTemp;   
      }

      strTemp = hsrqRequest.getParameter("MODIFICATION_DATE");
      if (strTemp != null)
      {
         modificationDate = DateUtils.parseTimestamp(strTemp);   
      }
      
      return new Domain(iDomainId, strName, strDescription, bEnabled, 
                        bAdministration, bAllowSelfRegistration, bPublicDomain,
                        bDefaultLoginEnabled, bDefaultSuperUser,
                        bDefaultInternalUser, strDefaultPhone, strDefaultAddress,
                        null, modificationDate, null);
   }  
   
   /**
    * Get controller to invoke business logic.
    * 
    * @return DomainController
    * @throws OSSException - an error has occurred
    */
   protected DomainController getController(
   ) throws OSSException
   {
      DomainController controller;
      
      controller = (DomainController)ControllerManager.getInstance(DomainController.class);
      
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
