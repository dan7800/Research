/*
 * Copyright (c) 2003 - 2007 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: AccessRightFactory.java,v 1.9 2007/09/13 06:37:45 bastafidli Exp $
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

package org.opensubsystems.security.persist;

import java.util.Collection;
import java.util.List;

import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.persist.DataFactory;
import org.opensubsystems.security.data.AccessRight;

/**
 * Methods to create, retrieve and manipulate access rights in the persistence 
 * store.
 * 
 * @version $Id: AccessRightFactory.java,v 1.9 2007/09/13 06:37:45 bastafidli Exp $
 * @author Peter Satury
 * @code.reviewer Miro Halas
 * @code.reviewed Initial revision
 */
public interface AccessRightFactory extends DataFactory 
{
   /**
    * Create access right.
    * 
    * @param data - access right to create
    * @return AccessRight - created access right 
    * @throws OSSException - an error has occurred
    */
   AccessRight create(
      AccessRight data
   ) throws OSSException;
   
   /**
    * Create access rights.
    * 
    * @param lstRights - list of access rights to create
    * @param iRoleId - id of role to which the access rights belong
    * @return List - create access rights, this will be the same list as it
    *                was sent in, just all the access rights will have generated
    *                Ids and timestamps
    * @throws OSSException - an error has occurred
    */
   List create(
      List lstRights,
      int  iRoleId
   ) throws OSSException;

   /**
    * Create collection of data objects.
    *
    * @param colDataObject - collection of data objects that will be created
    * @return int - number of inserted data items
    * @throws OSSException - error during create
    */
   int create(
      Collection  colDataObject
   ) throws OSSException;

   /**
    * Get access rights for specified role  
    * 
    * @param iRoleId - ID of the role 
    * @return List - list of access rights for specified role or null if no
    *                rights were granted
    * @throws OSSException - an error has occurred
    */   
   List getAllForRole(
      int iRoleId
   ) throws OSSException;
   
   /**
    * Get access rights for all specified roles
    * 
    * @param strRoleIds - string of role IDs separated by separator 
    * @return List - list of access rights for specified roles or null if no
    *                rights were granted
    * @throws OSSException - an error has occurred
    */   
   List getAllForRoles(
      String strRoleIds
   ) throws OSSException;

   /**
    * Delete list of access rights
    *
    * @param roleId - id of role to which the rights has to belong to be deleted  
    * @param rightIds - comma separated list of IDs of access rights to delete
    * @return int - number of deleted access rights
    * @throws OSSException - an error has occurred
    */ 
   int delete(
      int    roleId,
      String rightIds
   ) throws OSSException;
      
   /**
    * Delete all access rights for specified role  
    * 
    * @param iRoleId - ID of the role 
    * @return int - number of deleted access rights
    * @throws OSSException - an error has occurred
    */   
   int deleteAllForRole(
      int iRoleId
   ) throws OSSException;
   
   /**
    * Check if specified action is granted to data objects of given data type.
    * The action can be granted three different ways. User can have action
    * granted for all data objects of given type (no id and no categories
    * specified when granting access). User can have access granted to specific
    * data object (identified by identifier) of given data type. User can have
    * access granted to a group of objects based on categories the objects belong 
    * to.
    * 
    * Therefore if identifier or category is not specified then this method returns 
    * ACCESS_GRANTED only if access is granted to all data objects of given type. 
    * 
    * If identifier is specified and category is not then this method returns 
    * ACCESS_GRANTED if access is granted to all data objects of given type or to 
    * the specified data object. For example access to modify user 3 granted
    * if there exists access right granted to modify all users or access right
    * to modify user 3.
    * 
    * If category is specified but identifier is not then this method returns 
    * ACCCESS_GRANTED if access is granted to all data objects of given type or
    * access is granted to AT LEAST one category and its value specified as an
    * argument. For example access to modify user is granted if there exists 
    * access right granter to modify user whose category "is login enabled"
    * is true and the user for which we are checking this access has value true
    * for the "is login enabled" attribute.
    * 
    * If both identifier and category are specied then this method returns 
    * ACCESS_GRANTED if access is granted to all data objects of given type or
    * or to the specified data object or access is granted to AT LEAST one 
    * category and its value specified as an argument. 
    * 
    * @param dataType - data type code, one of the DataConstants.XXX_DATA_TYPE values
    * @param action - action code, one of the ActionConstants.RIGHTS_XXX values
    * @param identifier - identification of the data object of specified data type
    *                     to check if specific action is granted for. This
    *                     should be the id of the data object if want to see 
    *                     if user has access granted to given data object.
    * @param categories - array of category-identifier combinations, it is array
    *                     where each element is array of two elements, the first
    *                     is category id and the second is identifier for the
    *                     data in the category
    * @return int - access right type constant ACCESS_GRANTED or ACCESS_DENIED
    * @throws OSSException - error during get right type
    */
   int checkAccess(
      int     dataType,
      int     action,
      int     identifier,
      int[][] categories
   ) throws OSSException;
   
   /**
    * Get list of access rights for data type and action granted to current user.
    * 
    * @param dataType - data type code
    * @param action - action code
    * @return List - list of access rights associated to current user or null if
    *                no rights were granted 
    * @throws OSSException - an error has occurred
    */
   List getForCurrentUser(
      int dataType,
      int action
   ) throws OSSException;   
}
