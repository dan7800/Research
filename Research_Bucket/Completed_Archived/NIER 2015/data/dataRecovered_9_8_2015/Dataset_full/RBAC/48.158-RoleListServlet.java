/*
 * Copyright (c) 2003 - 2007 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: RoleListServlet.java,v 1.9 2009/04/22 06:29:14 bastafidli Exp $
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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;

import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.error.OSSInvalidDataException;
import org.opensubsystems.core.logic.ControllerManager;
import org.opensubsystems.core.persist.db.impl.DatabaseTransactionFactoryImpl;
import org.opensubsystems.core.util.CallContext;
import org.opensubsystems.core.util.GlobalConstants;
import org.opensubsystems.core.util.Log;
import org.opensubsystems.core.util.TransactionUtils;
import org.opensubsystems.patterns.listdata.www.ListBrowserServlet;
import org.opensubsystems.security.logic.RoleController;

/**
 * This servlet handles screen displaying list of users.
 *  
 * @version $Id: RoleListServlet.java,v 1.9 2009/04/22 06:29:14 bastafidli Exp $
 * @author Miro Halas
 * @code.reviewer Miro Halas
 * @code.reviewed Initial revision.
 */
public class RoleListServlet extends ListBrowserServlet
{
   // Constants ////////////////////////////////////////////////////////////////

   /**
    * Names of the form and action to delete roles.
    */
   public static final String FORM_DELETE_ROLE_NAME = "delete";

   /**
    * Names of the form and action to enable roles.
    */
   public static final String FORM_ENABLE_ROLE_NAME = "enable";

   /**
    * Names of the form and action to disable roles.
    */
   public static final String FORM_DISABLE_ROLE_NAME = "disable";

   /**
    * Constants for action to delete roles. 
    */
   public static final int FORM_DELETE_ROLE_ID = FORM_COUNT_LISTBROWSER + 1;

   /**
    * Constants for action to enable roles. 
    */
   public static final int FORM_ENABLE_ROLE_ID = FORM_COUNT_LISTBROWSER + 2;

   /**
    * Constants for action to disable roles. 
    */
   public static final int FORM_DISABLE_ROLE_ID = FORM_COUNT_LISTBROWSER + 3;

   /**
    * How many forms this servlet recongizes. 
    */
   public static final int FORM_COUNT_ROLELIST = FORM_COUNT_LISTBROWSER + 4;
   
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Generated serial version id for this class.
    */
   private static final long serialVersionUID = 481687993757986840L;

   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(RoleListServlet.class);

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
   protected void doPost(
      HttpServletRequest  hsrqRequest,
      HttpServletResponse hsrpResponse
   ) throws ServletException, 
            IOException
   {
      switch(getFormToProcess(hsrqRequest))
      {
         case (FORM_DELETE_ROLE_ID):
         {
            processDeleteRoleForm(hsrqRequest, hsrpResponse);
            break;
         }

         case (FORM_DISABLE_ROLE_ID):
         {
            processUpdateEnableRoleForm(hsrqRequest, hsrpResponse, false);
            break;
         }
                  
         case (FORM_ENABLE_ROLE_ID):
         {
            processUpdateEnableRoleForm(hsrqRequest, hsrpResponse, true);
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
      else if (strFormName.equals(FORM_DELETE_ROLE_NAME))
      {
         // Disable selected roles
         iReturn = FORM_DELETE_ROLE_ID;
      }
      else if (strFormName.equals(FORM_DISABLE_ROLE_NAME))
      {
         // Display edit user page
         iReturn = FORM_DISABLE_ROLE_ID;
      }
      else if (strFormName.equals(FORM_ENABLE_ROLE_NAME))
      {
         // Enable selected roles
         iReturn = FORM_ENABLE_ROLE_ID;
      }
      else
      {
         iReturn = super.getFormToProcess(hsrqRequest);
      }

      return iReturn;
   }
   
   /**
    * Process form to delete roles. 
    *
    * @param  hsrqRequest  - the servlet request.
    * @param  hsrpResponse - the servlet response.
    * @throws ServletException - an error while serving request
    * @throws IOException - an error while writing response
    */
   protected void processDeleteRoleForm(
      HttpServletRequest  hsrqRequest,
      HttpServletResponse hsrpResponse
   ) throws IOException,
            ServletException
   {
      s_logger.entering("RoleListServlet", "processDeleteRoleForm");

      try
      {
         String selectedIds = hsrqRequest.getParameter(LIST_PARAM_SELECTED_ITEMS);
         UserTransaction utTransaction;

         utTransaction = DatabaseTransactionFactoryImpl.getInstance().requestTransaction();
         utTransaction.begin();
         try
         {
            int deleted = getController().delete(selectedIds);
            if (GlobalConstants.ERROR_CHECKING)
            {
               assert deleted > Integer.MIN_VALUE : "Check to avoid checkstyle.";
            }
            utTransaction.commit();
         }
         catch (OSSInvalidDataException ideExc)
         {
            TransactionUtils.rollback(utTransaction);
            CallContext.getInstance().getMessages().addMessages(
                                                       ideExc.getErrorMessages());
         }
         catch (Throwable thr)
         {
            TransactionUtils.rollback(utTransaction);
            throw new ServletException(thr);
         }

         // Return to the same page of the list we were called from
         processSetExactPage(hsrqRequest, hsrpResponse);
      }
      catch (Throwable eExc)
      {
         s_logger.log(Level.WARNING, 
                      "An error has occurred while delete roles.", eExc);
         messageBoxPage(hsrqRequest, hsrpResponse, "Error", eExc.getMessage(),
                        hsrqRequest.getContextPath(), eExc.getCause());
      }
      finally
      {
         s_logger.exiting("RoleListServlet", "processDeleteRoleForm");
      }
   }
   
   /**
    * Process form to update enable flag for the selected roles. 
    *
    * @param  hsrqRequest  - the servlet request.
    * @param  hsrpResponse - the servlet response.
    * @param  newEnableValue - new enabled value
    * @throws ServletException - an error while serving request
    * @throws IOException - an error while writing response
    */
   protected void processUpdateEnableRoleForm(
      HttpServletRequest  hsrqRequest,
      HttpServletResponse hsrpResponse,
      boolean             newEnableValue
   ) throws IOException,
            ServletException
   {
      s_logger.entering("RoleListServlet", "processUpdateEnableRoleForm");
      
      try
      {
         String selectedIds = hsrqRequest.getParameter(LIST_PARAM_SELECTED_ITEMS);
         UserTransaction utTransaction;
         
         utTransaction = DatabaseTransactionFactoryImpl.getInstance().requestTransaction();
         utTransaction.begin();
         try
         {
            int enabled = getController().updateEnable(selectedIds, newEnableValue);
            if (GlobalConstants.ERROR_CHECKING)
            {
               assert enabled > Integer.MIN_VALUE : "Check to avoid checkstyle.";
            }
            utTransaction.commit();
         }
         catch (OSSInvalidDataException ideExc)
         {
            TransactionUtils.rollback(utTransaction);
            CallContext.getInstance().getMessages().addMessages(
                                                       ideExc.getErrorMessages());
         }
         catch (Throwable thr)
         {
            TransactionUtils.rollback(utTransaction);
            throw new ServletException(thr);
         }
         
         // Return to the same page of the list we were called from
         processSetExactPage(hsrqRequest, hsrpResponse);
      }
      catch (Throwable eExc)
      {
         s_logger.log(Level.WARNING, 
                      "An error has occurred while enable roles.", eExc);
         messageBoxPage(hsrqRequest, hsrpResponse, "Error", eExc.getMessage(),
                        hsrqRequest.getContextPath(), eExc.getCause());
      }
      finally
      {
         s_logger.exiting("RoleListServlet", "processUpdateEnableRoleForm");
      }
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
