/*
 * Copyright (c) 2003 - 2007 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: RoleSimpleRightsTableTag.java,v 1.15 2009/04/22 06:29:14 bastafidli Exp $
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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;

import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.util.HashCodeUtils;
import org.opensubsystems.core.util.jsp.TagUtils;
import org.opensubsystems.core.www.jsp.PageElementCacheTag;
import org.opensubsystems.security.data.AccessRight;
import org.opensubsystems.security.data.Role;
import org.opensubsystems.security.data.SecurityDefinition;
import org.opensubsystems.security.logic.SecurityDefinitionManager;

/**
 * Custom tag to construct table of simple rights on create or edit role page 
 * 
 * @version $Id: RoleSimpleRightsTableTag.java,v 1.15 2009/04/22 06:29:14 bastafidli Exp $
 * @author Peter Satury
 * @code.reviewer Miro Halas
 * @code.reviewed Initial revision.
 */
public class RoleSimpleRightsTableTag extends PageElementCacheTag
{
   // Helper classes ///////////////////////////////////////////////////////////
   
   /**
    * Simple internal class to store actions, their counts to sort them
    */
   public static class ActionCountData
   {
      /**
       * Count of the actions
       */
      protected int m_iActionCount;
      
      /**
       * Name of the action
       */
      protected String m_strActionName;
      
      /**
       * Id of the action
       */
      protected Integer m_intActionCode;
      
      /**
       * Constructor
       * 
       * @param iActionCount - action count to store
       * @param strActionName - action name to store
       * @param intActionCode - action code to store
       */
      public ActionCountData(
         int     iActionCount,
         String  strActionName,
         Integer intActionCode
      )
      {
         m_iActionCount = iActionCount;
         m_strActionName = strActionName;
         m_intActionCode = intActionCode;
      }
      
      /**
       * @return String - action name
       */
      public String getActionName(
      )
      {
         return m_strActionName;
      }
      
      /**
       * @return int - action count
       */
      public int getActionCount(
      )
      {
         return m_iActionCount;
      }
      
      /**
       * @return int - action code
       */
      public int getActionCode(
      )
      {
         return m_intActionCode.intValue();
      }
      
      /**
       * @return Integer - action code
       */
      public Integer getActionCodeAsObject(
      )
      {
         return m_intActionCode;
      }

      /**
       * Increments action count
       */
      public void incrementCount()
      {
         m_iActionCount++;
      }
      
      /**
       * {@inheritDoc}
       */
      public boolean equals(
         Object oObject
      )
      {
         boolean bReturn = false;
      
         if (oObject == this)
         {
            // Here we compared memory locations and found out that it is the same object
            bReturn = true;
         }
         else if (oObject != null)
         {
            if (oObject instanceof ActionCountData)
            {
               ActionCountData data;
               
               data = (ActionCountData)oObject;
               
               // We need to compare object for nulls. Do it this way either 
               // both are nulls or the one we are going to call equals on
               // is not null and the objects are equals
               bReturn  = (m_iActionCount == data.m_iActionCount) 
                           && (((m_strActionName == null) && (data.m_strActionName == null))
                               || ((m_strActionName != null) 
                                  && (m_strActionName.equals(data.m_strActionName))))
                           && (((m_intActionCode == null) && (data.m_intActionCode == null))
                               || ((m_intActionCode != null) 
                                  && (m_intActionCode.equals(data.m_intActionCode))));
            }
         }
         return bReturn;
      }

      /**
       * {@inheritDoc}
       */
      public int hashCode()
      {
         int iResult = HashCodeUtils.SEED;
         iResult = HashCodeUtils.hash(iResult, m_iActionCount);
         iResult = HashCodeUtils.hash(iResult, m_strActionName);
         iResult = HashCodeUtils.hash(iResult, m_intActionCode);
         return iResult;
      }
   }
   
   /**
    * Comparator to compare two object of ActionCountData type by sorting them
    * by their id.
    */
   public static class ActionsCountDataByIdComparator implements Comparator
   {
      /**
       * {@inheritDoc}
       */
      public int compare(
         Object o1, 
         Object o2
      )
      {
         int iReturn = 0;
         
         if ((o1 != null) || (o2 != null))
         {
            if (o1 == null) // o2 != null
            {
               iReturn = 1;
            }
            else if (o2 == null) // o1 != null
            {
               iReturn = -1;
            }
            else 
            {
               iReturn = (((ActionCountData) o1).getActionCode() 
                         > ((ActionCountData) o2).getActionCode()) ? 1 : -1;
            }
         }
         return iReturn;
      }
   }

   // Constants ////////////////////////////////////////////////////////////////
   
   /**
    * Percentage which is used to determine when to create new column for 
    * a shared action or add into custom column. 
    */ 
   public static final int SHARED_ACTION_THRESHOLD = 10;
   
   // Cached values ////////////////////////////////////////////////////////////
   
   /**
    * Collection of definitions describing security of all components.
    * Key is data type, value is SecurityDefinition
    */
   protected static Map s_mpDefinitions = null;
   
   /**
    * Collection of actions which are shared between most data types.
    * Key is action name, value is ActionCountData 
    */
   protected static Map s_mpSharedActions = null;
   
   /**
    * How many columns in the table will be used by custom actions.
    */
   protected static int s_iCustomColumnCount = 0;

   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Generated serial version id for this class.
    */
   private static final long serialVersionUID = -2044770459009673L;

   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Constructor for custom tag.
    */
   public RoleSimpleRightsTableTag() 
   {
      super();
   }
   
   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * {@inheritDoc}
    */
   public int doStartTag(
   ) throws JspException 
   {
      if (s_mpDefinitions == null)
      {
         try
         {
            calculateStaticValues();
         }
         catch (OSSException osseExc)
         {
            throw new JspException("An unexpected exception has occurred.", osseExc);
         }
      }
      
      // Create map that contains access rights specified in the role hashed 
      // for easy access. The key is combination of data type and action id 
      // (dataType + '_' + actionId) and value is AccessRight object
      Role data;
      Map  mpAccessRights = Collections.EMPTY_MAP; 
   
      data = (Role)pageContext.getRequest().getAttribute(RoleServlet.PARAMETER_ROLE_NAME);
      if (data != null)
      {
         List lRoleAccessRights = data.getAccessRights();
         
         if ((lRoleAccessRights != null) && (!lRoleAccessRights.isEmpty()))
         {
            Iterator     itRights;
            AccessRight  right;
            StringBuffer sbBuffer = new StringBuffer();
            
            mpAccessRights = new HashMap(lRoleAccessRights.size()); 
            for (itRights = lRoleAccessRights.iterator(); itRights.hasNext();)
            {
               right = (AccessRight)itRights.next();
               sbBuffer.delete(0, sbBuffer.length());
               sbBuffer.append(right.getDataType());
               sbBuffer.append('_');
               sbBuffer.append(right.getAction());
               mpAccessRights.put(sbBuffer.toString(), right);
            }
         }
      }
      
      Iterator           itActionCounts;
      Iterator           itDefinitions;
      ActionCountData    countData;
      SecurityDefinition definition;
      String             strActionName;
      Collection         colDataActionNames;
      
      StringBuffer sbHtml = new StringBuffer();
      
      // Generate table header together with checkbox to select all access rights
      // in the entire table
      sbHtml.append("<table width=\"100%\" cellspacing=\"0\" cellpadding=\"1\"\n" +
                    "       border=\"1\" id=\"simplerightstable\"\n" + 
                    "       summary=\"Simple way to define access rights\"\n" +
                    "       frame=\"void\" rules=\"cols\" class=\"clsSimpleRightsTable\">\n" +
                    "   <thead>\n" +
                    "      <tr id=\"simpleheader\">\n" +
                    "         <th><a href=\"#\" onclick=\"grantAllCheckBox();\">" +
                    "<img id=\"table_grant_all\" border=\"0\" " +
                    "src=\"/patterns/images/checkboxunchecked.gif\" width=\"12\" " +
                    "height=\"12\" alt=\"Grant all\"/></a>&nbsp;Grant all</th>\n");
      
      // For each action, which is shared between large number of data types
      // define separate column heading with the name of the action in it
      // and checkbox allowing us to select all rights in the column
      // that is select that action for all data types (e.g. view everything)
      for (itActionCounts = s_mpSharedActions.values().iterator(); 
           itActionCounts.hasNext();)
      {
         countData = (ActionCountData)itActionCounts.next();
         sbHtml.append("         <th>" +
                  "<a href=\"#\" onclick=\"checkColumns(event); return false;\">" +
                  "<img id=\"headercolumn_");
         // TODO: For Julo: What if action name contains space, it cannot be used 
         // in id. This should be really replaced with action code
         sbHtml.append(countData.getActionName());
         sbHtml.append("\" border=\"0\" src=\"/patterns/images/checkboxunchecked.gif\" " +
                  "width=\"12\" height=\"12\" alt=\"Grant action to all\"/></a>&nbsp;");
         sbHtml.append(countData.getActionName());
         sbHtml.append("</th>\n");
      }

      // Now generate single heading which will span as many columns as there is
      // custom actions for the most customized data type and checkbox allowing
      // us to select all custom rights
      if (s_iCustomColumnCount > 0)
      {
         sbHtml.append("         <th colspan=\"");
         sbHtml.append(s_iCustomColumnCount);
         sbHtml.append("\">" +
                  "<a href=\"#\" onclick=\"checkColumns(event); return false;\">" +
                  "<img id=\"headercolumn_Custom\" border=\"0\" " +
                  "src=\"/patterns/images/checkboxunchecked.gif\" " +
                  "width=\"12\" height=\"12\" " +
                  "alt=\"Grant all custom actions\"/></a>&nbsp;Custom</th>\n");
      }
      sbHtml.append("      </tr>\n" + 
                    "   </thead>\n");
      
      // Now generate footer of the table
      // TODO: For Miro: Do we need footer
      sbHtml.append("   <tfoot>\n" +
               "      <tr>\n" +
               "         <th colspan=\"");
      sbHtml.append((s_mpSharedActions.size() + s_iCustomColumnCount + 1));
      sbHtml.append("\">&nbsp;</th>\n" + 
                    "      </tr>\n" +
                    "   </tfoot>\n");
      
      // Now generate body of the table which will contain checkbox for every 
      // right that can be assigned
      sbHtml.append("   <tbody>\n");
      
      int       iRowIndex;
      int       iCount;
      Integer   intActionCode;
      Iterator  itDataAction;
      Map.Entry meDataAction;
      
      // The counting of rows has to start from 1
      for (itDefinitions = s_mpDefinitions.values().iterator(), iRowIndex = 1; 
           itDefinitions.hasNext(); iRowIndex++)
      {
         definition = (SecurityDefinition)itDefinitions.next();
         // Get all codes (Integers) for the actions in this definition
         colDataActionNames = definition.getDataActions().values();
         
         // Start new row for the data type
         sbHtml.append("      <tr id=\"simplerow_");
         sbHtml.append(iRowIndex);
         sbHtml.append("\" class=\"");
         sbHtml.append(iRowIndex % 2 == 1 ? "clsOdd" : "clsEven");
         sbHtml.append("\">\n");

         // The first cell in the row will be name of the data type together
         // with checkbox allowing us to select all rights in the row
         // meaning can do anything with e.g. User
         sbHtml.append("         <td class=\"clsSimpleRightsRowHeader\">");
         sbHtml.append("<a href=\"#\" onclick=\"checkRows(event); return false;\">" +
                       "<img id=\"headerrow_");
         sbHtml.append(definition.getDataType());
         sbHtml.append("\" border=\"0\" src=\"/patterns/images/checkboxunchecked.gif\"" +
                       " width=\"12\" height=\"12\" alt=\"Grant all actions\"/></a>&nbsp;\n");
         sbHtml.append(definition.getDisplayableViewName());
         sbHtml.append("</td>\n");
         
         // The following cells will be cells for shared actions and will contain
         // only checkboxes and no names
         for (itActionCounts = s_mpSharedActions.values().iterator();
              itActionCounts.hasNext();)
         {
            countData = (ActionCountData)itActionCounts.next();
            strActionName = countData.getActionName();               
            if (colDataActionNames.contains(strActionName))
            {
               // The current data type support shared action 
               writeCheckBox(mpAccessRights, sbHtml, definition.getDataType().intValue(),
                             countData.getActionName(), countData.getActionCode(), false);
            }
            else
            {
               // The current data type does not support shared action
               // therefore just leave the space empty
               sbHtml.append("         <td>&nbsp;</td>\n");
            }
         }
         
         // The following cells will be any custom actions left out for current
         // data type
         if (s_iCustomColumnCount > 0)
         {
            iCount = 0;
            for (itDataAction = definition.getDataActions().entrySet().iterator(); 
                 itDataAction.hasNext();)
            {
               meDataAction = (Map.Entry)itDataAction.next();
               intActionCode = (Integer)meDataAction.getKey();
               strActionName = (String)meDataAction.getValue();
               if (!s_mpSharedActions.containsKey(strActionName))
               {
                  // The action was not found between shared and therefore 
                  // is considered custom 
                  writeCheckBox(mpAccessRights, sbHtml, definition.getDataType().intValue(),
                                strActionName, intActionCode.intValue(), true);
                  iCount++;
               }
            }
            
            // Fill the rest of the row with emtpy cells
            for (; iCount < s_iCustomColumnCount; iCount++)
            { 
               // No more custome actions for this data type left
               sbHtml.append("         <td>&nbsp;</td>\n");
            }
         }
         sbHtml.append("      </tr>\n");
      }
      sbHtml.append("   </tbody>\n" +
                    "</table>\n");
      
      TagUtils.write(pageContext, sbHtml.toString());
      
      return (SKIP_BODY);
   }

   /**
    * {@inheritDoc}
    */
   public int doEndTag(
   ) throws JspException 
   {
      return (EVAL_PAGE);
   }

   // Helper methods ///////////////////////////////////////////////////////////
   
   /**
    * Internal method to write three state checkbox to the output
    *
    * @param mpAccessRights - map that contains actual access rights specified in 
    *                       the role. The key is combination of data type and 
    *                       action id (dataType + '_' + actionId) and value is 
    *                       AccessRight object
    * @param sbHtml - StringBuffer output
    * @param dataType - int data type of checkbox
    * @param actionName - string that defines name of action
    * @param actionCode - action code identifying action
    * @param bIsCustom - boolean that defines if the checkbox is in the custom 
    *                    column, which is columns where each cell can represent
    *                    different action 
    */
   protected void writeCheckBox(
      Map          mpAccessRights,
      StringBuffer sbHtml,
      int          dataType,
      String       actionName,
      int          actionCode,
      boolean      bIsCustom
   )
   {
      AccessRight  right;
      StringBuffer sbBuffer = new StringBuffer();

      // Check if current role contains right for the specified data type and action
      sbBuffer.delete(0, sbBuffer.length());
      sbBuffer.append(dataType);
      sbBuffer.append('_');
      sbBuffer.append(actionCode);
      right = (AccessRight)mpAccessRights.get(sbBuffer.toString());
      
      sbHtml.append("         <td>");
      // TODO: Improve: In the future investigate if it would not be more efficient
      // to use css background image for this 
      sbHtml.append("<a href=\"#\" onclick=\"simpleTableCheckBoxClick(event); return false;\">" +
                    "<img id=\"checkbox_");
      sbHtml.append(dataType);
      sbHtml.append('_');
      if (bIsCustom)
      {
         sbHtml.append("Custom_");
      }
      sbHtml.append(actionName);
      sbHtml.append("\" border=\"0\" src=\"/patterns/images/checkbox");
      if (right != null)
      {
         // Yes it does 
         if ((right.getRightType() == AccessRight.ACCESS_GRANTED) 
            && (right.getCategory() == AccessRight.NO_RIGHT_CATEGORY))
         {
            // And it is simple
            sbHtml.append("checked");
         }
         else
         {
            // And it is complex
            sbHtml.append("dimmed");
         }
      }
      else 
      {
         // No it doesn't
         sbHtml.append("unchecked");
      }
      sbHtml.append(".gif\" width=\"12\" height=\"12\" alt=\"Grant\" actionId=\"");
      sbHtml.append(actionCode);
      sbHtml.append("\"/></a>");
      // If this is custom column, we need to add name since each action
      // in this column can be potentially different
      if (bIsCustom)
      {
         sbHtml.append("&nbsp;");
         sbHtml.append(actionName);
      }
      sbHtml.append("</td>\n");
   }

   /**
    * Calculate static values required to display the table. 
    * 
    * @throws OSSException - an error has occurred
    */
   protected static void calculateStaticValues(
   ) throws OSSException
   {
      // Figure out which actions are shared between enough data types to justify
      // whole column per action and which actions are rare and will be displayed
      // together in "custom" columns
      Map mpDefinitions;   // key is data type, value is SecurityDefinition

      // Key is name of the action which can be executed on data and value is
      // ActionCountData with count, how many data types support this action
      // Hashmap can be used since once constructed it will be only read
      // Name is used as key since some actions can share the same code
      // but have different name and therefore will look like different
      // actions to the user
      Map                mpActionCounts = new HashMap();
      Map                mpActions;
      Map.Entry          actionEntry;
      SecurityDefinition definition;
      ActionCountData    actionCountData;
      Integer            intActionCode;
      Iterator           itDefinitions;
      Iterator           itActions;
      String             strActionName;
      
      // Get all the security definitions
      mpDefinitions = SecurityDefinitionManager.getInstance()
                                   .getDataTypeSecurityDefinitions();
      
      // Go through all definitions
      for (itDefinitions = mpDefinitions.values().iterator();
          itDefinitions.hasNext();)
      {
         definition = (SecurityDefinition)itDefinitions.next();
         mpActions = definition.getDataActions();
         
         // And through all actions of the definition
         for (itActions = mpActions.entrySet().iterator();
             itActions.hasNext();)
         {
            actionEntry = (Map.Entry)itActions.next();
            intActionCode = (Integer)actionEntry.getKey();
            strActionName = (String)actionEntry.getValue();

            // And count number of occurences for each action
            actionCountData = (ActionCountData)mpActionCounts.get(strActionName);
            if (actionCountData == null)
            {
               actionCountData = new ActionCountData(1, strActionName, 
                                                     intActionCode); 
            }
            else
            {
               actionCountData.incrementCount();
            }
            mpActionCounts.put(strActionName, actionCountData);
         }
      }
               
      // First figure out haw many shared actions there are which will tell us
      // how many columns will be shared between all data types
      Map             mpSharedActions; // key is action name, value is ActionCountData
      Iterator        itActionCounts;
      ActionCountData countData;
      int             iThreshold;

      // Compute threshold of how many data types have to support given action
      // to be considered shared as
      // shared count >= total number of data types * threshold percentage / 100
      iThreshold = (int)((((float)mpDefinitions.size()) * ((float)SHARED_ACTION_THRESHOLD)) / 100f);
      if ((iThreshold == 0) && (mpDefinitions.size() > 1))
      {
         // If there is more than one data item but there is so few of them that
         // no action count is below threshold, consider all actions which occurs
         // only 1 time as custom
         iThreshold = 2;
      }
      
      
      // Now find out how many are above threshold and remember them separately
      // Since there can be many custom actions between different data types, 
      // start from empty and add only those few shared. We key them by action
      // name since some actions can share the same code since they do the same 
      // functionality behind the scene but are called differently and therefore
      // need to appear as different actions to the user
      mpSharedActions = new HashMap();
      for (itActionCounts = mpActionCounts.values().iterator(); 
           itActionCounts.hasNext();) 
      {
         countData = (ActionCountData)itActionCounts.next();
         if (countData.getActionCount() >= iThreshold)
         {
            mpSharedActions.put(countData.getActionName(), countData);
         }
      }
      
      // At this point 
      // mpSharedActions contains map of actions shared between most of the data types 
      
      // And now determine the maximum count of custom actions per data type
      // which will tell us, how many columns thre needs to be to fit all
      // custom actions per data type into the table
      Iterator   itActionNames;
      int        iCustomActionCounter;
      int        iCustomColumnCount = 0;
      Collection colDataActionNames;
            
      for (itDefinitions = mpDefinitions.values().iterator(); 
           itDefinitions.hasNext();)
      {
         definition = (SecurityDefinition)itDefinitions.next();
         // Get all names of the actions in this definition since differently
         // named actions are from user standpoint considered different even
         // when they share the same code underneath
         colDataActionNames = definition.getDataActions().values();
         
         // Go through all actions for this definition and see which ones are not shared 
         iCustomActionCounter = 0;
         for (itActionNames = colDataActionNames.iterator(); itActionNames.hasNext();)
         {
            strActionName = (String)itActionNames.next();
            if (!mpSharedActions.containsKey(strActionName))
            {
               iCustomActionCounter++;               
            }
         }
         
         if (iCustomActionCounter > iCustomColumnCount)
         {
            iCustomColumnCount = iCustomActionCounter;
         }
      }      

      s_mpDefinitions = mpDefinitions;
      s_mpSharedActions = mpSharedActions;
      s_iCustomColumnCount = iCustomColumnCount;
   }
}
