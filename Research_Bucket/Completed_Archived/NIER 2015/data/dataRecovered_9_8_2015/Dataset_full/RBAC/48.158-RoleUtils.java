/*
 * Copyright (c) 2006 - 2007 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: RoleUtils.java,v 1.6 2009/04/22 06:29:41 bastafidli Exp $
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opensubsystems.core.data.DataObject;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.security.data.AccessRight;
import org.opensubsystems.security.data.Role;
import org.opensubsystems.security.data.SecurityDefinition;
import org.opensubsystems.security.logic.SecurityDefinitionManager;

/**
 * Collection of useful methods and constants used for roles and access rights.
 *
 * @version $Id: RoleUtils.java,v 1.6 2009/04/22 06:29:41 bastafidli Exp $
 * @author Julian Legeny
 * @code.reviewer Miro Halas 
 * @code.reviewed 1.1 2006/03/15 00:01:56 jlegeny
 */
public final class RoleUtils
{
   // Constants used to create default self registered user role 

   /**
    * Name of the self registered user role.
    */
   public static final String SELFREG_ROLE_NAME = "Self registered user";

   /**
    * Description of the self registered user role.
    */
   public static final String SELFREG_ROLE_DESCRIPTION 
                                 = "Default role for self registered user";
   
   // Constructor //////////////////////////////////////////////////////////////

   /** 
    * Private constructor since this class cannot be instantiated
    * @throws OSSException - error occurred 
    */
   private RoleUtils(
   ) throws OSSException
   {
      // Do nothing
   }
   
   // Logic ////////////////////////////////////////////////////////////////////

   /**
    * Method to get default self registered user role with belonging access rights. 
    * Used for 1st created domain within the database schema.
    * 
    * @param iDomainId - ID of the domain
    * @return Role - created self registered role
    * @throws OSSException - error during insert
    */
   public static Role getSelfregisteredRole(
      int iDomainId
   ) throws OSSException
   {
      List               lstAccessRights = new ArrayList();
      Map.Entry          entry;
      Iterator           entries;
      Integer            actualDataType;
      SecurityDefinition secDef;

      // get map of all security definitions and find all data types that have
      // action view - these data types will be used for creating access rights
      Map secDefMap = SecurityDefinitionManager.getInstance().getDataTypeSecurityDefinitions();
      
      for (entries = secDefMap.entrySet().iterator(); entries.hasNext();)
      {
         entry = (Map.Entry)entries.next();
         actualDataType = (Integer)entry.getKey();
         secDef = (SecurityDefinition)entry.getValue();
         if ((actualDataType != null) 
              && secDef.getDataActions().containsKey(ActionConstants.RIGHT_ACTION_VIEW_OBJ))
         {
            // use retrieved data type to access right and add it 
            // into the list of access rights
            lstAccessRights.add(new AccessRight(
                                       DataObject.NEW_ID, DataObject.NEW_ID, 
                                       iDomainId, ActionConstants.RIGHT_ACTION_VIEW, 
                                       actualDataType.intValue(), 
                                       AccessRight.ACCESS_GRANTED, 
                                       AccessRight.NO_RIGHT_CATEGORY, 
                                       AccessRight.NO_RIGHT_IDENTIFIER, 
                                       null, null));
         }
      }

      // --------------------------------------------------------------------
      // return self registered role with belonging access rights
      return new Role(DataObject.NEW_ID, iDomainId, 
                      RoleUtils.SELFREG_ROLE_NAME, 
                      RoleUtils.SELFREG_ROLE_DESCRIPTION, 
                      true, DataObject.NEW_ID, false, 
                      lstAccessRights, null, null);
   }
}
