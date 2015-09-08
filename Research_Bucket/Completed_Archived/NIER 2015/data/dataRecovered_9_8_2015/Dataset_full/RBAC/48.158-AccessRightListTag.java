/*
 * Copyright (c) 2003 - 2008 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: AccessRightListTag.java,v 1.16 2008/11/15 06:08:11 bastafidli Exp $
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

import java.util.Map;

import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.util.GlobalConstants;
import org.opensubsystems.patterns.listdata.www.ListTag;
import org.opensubsystems.security.data.AccessRight;
import org.opensubsystems.security.data.AccessRightDataDescriptor;
import org.opensubsystems.security.data.SecurityDefinition;

/**
 * Custom tag to create scrollable and browsable table of accessright data objects.
 * 
 * @version $Id: AccessRightListTag.java,v 1.16 2008/11/15 06:08:11 bastafidli Exp $
 * @author Miro Halas
 * @code.reviewer Miro Halas
 * @code.reviewed Initial revision
 */
public class AccessRightListTag extends ListTag 
{
   // Constants ////////////////////////////////////////////////////////////////
   
   /**
    * Generated serial version id for this class.
    */
   private static final long serialVersionUID = 851166521397791987L;

   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Default constructor
    * 
    * @throws OSSException - an error has occurred
    */
   public AccessRightListTag(
   ) throws OSSException
   {
      super (AccessRightDataDescriptor.class);
   }
   
   // Helper methods ///////////////////////////////////////////////////////////
   
   /**
    * {@inheritDoc}
    */
   protected void getBodyDefinition(
      int              iColumnId,
      int              iCounter,
      int              iItemCounter,
      Object           objListItem,
      StringBuffer     sbHtml
   )
   {
      AccessRight        right = (AccessRight)objListItem;
      Map                securitydefinitions;
      SecurityDefinition definition = null;
      String             strText;
      
      // This has to be sent by the backend
      securitydefinitions = (Map)pageContext.getRequest().getAttribute(
                                    RoleServlet.PARAMETER_SECURITY_DEFINITIONS);

      if (GlobalConstants.ERROR_CHECKING)
      {
         assert securitydefinitions != null 
                : "No security definitions available in the request";
      }
      definition = (SecurityDefinition)securitydefinitions.get(
                                          right.getDataTypeAsObject());
      if (GlobalConstants.ERROR_CHECKING)
      {
         assert definition != null 
                : "No security definition can be found for data type " 
                   + right.getDataType();
      }
      
      switch(iColumnId)
      {
         case(AccessRightDataDescriptor.COL_ACCESSRIGHT_ID):
         {
            sbHtml.append("<td>");
            sbHtml.append(right.getId());
            sbHtml.append("</td>\n");
            break;
         }
         case(AccessRightDataDescriptor.COL_ACCESSRIGHT_DATA_TYPE):
         {
            if (definition != null)
            {
               strText = definition.getDisplayableViewName(); 
            }
            else
            {
               strText = "Unknown";
            }
            generateTableCell(sbHtml, strText, 40, null, null);
            break;
         }
         case(AccessRightDataDescriptor.COL_ACCESSRIGHT_ACTION):
         {
            generateTableCell(sbHtml, 
                              (String)definition.getDataActions().get(
                                         new Integer(right.getAction())), 
                              40, 
                              null, 
                              null);
            break;
         }
         case(AccessRightDataDescriptor.COL_ACCESSRIGHT_CATEGORY):
         {
            // TODO: Performance: Here and below we are constructing object
            // for the category, we may decide to cache it inside of AccessRight
            // for smaller footprint
            generateTableCell(sbHtml, 
                              (String)definition.getDataCategories().get(
                                         new Integer(right.getCategory())), 
                              40, 
                              null, 
                              null);
            break;            
         }
         case(AccessRightDataDescriptor.COL_ACCESSRIGHT_IDENTIFIER):
         {
            generateTableCell(sbHtml, 
                              (String)((Map)definition.getDataCategoryValueMaps(
                                        ).get(new Integer(right.getCategory()))
                                           ).get(new Integer(right.getIdentifier())), 
                              40, 
                              null, 
                              null);
            break;            
         }
         default:
         {
            sbHtml.append("<td>Unknown value</td>\n");            
            break;
         }
      }
   }
}
