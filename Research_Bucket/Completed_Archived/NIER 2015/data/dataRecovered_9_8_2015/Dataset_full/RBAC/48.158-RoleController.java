/*
 * Copyright (c) 2003 - 2007 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: RoleController.java,v 1.13 2007/09/13 06:18:54 bastafidli Exp $
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

package org.opensubsystems.security.logic;

import java.rmi.RemoteException;
import java.util.List;

import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.logic.ModifiableDataController;
import org.opensubsystems.security.data.Role;

/**
 * Business logic related to managing roles.
 * 
 * @version $Id: RoleController.java,v 1.13 2007/09/13 06:18:54 bastafidli Exp $
 * @author Peter Satury
 * @code.reviewer Miro Halas
 * @code.reviewed Initial revision
 */
public interface RoleController extends ModifiableDataController,
                                        SecureController
{
   /**
    * Get all roles by list of IDs
    * 
    * @param strIds - comma separated string of the role IDs
    * @return List - list of Roles
    * @throws OSSException - an error has occurred
    * @throws RemoteException - required since this method can be called remotely
    */
   List get(
      String strIds
   ) throws OSSException,
            RemoteException; 

   /**
    * Get all roles assigned to the user knowing just the user ID.
    *
    * @param iUserId - user ID, can be DataObject.NEW_ID if you want to find out
    *                  roles for new user
    * @return List - list of user roles or null if none exists
    * @throws OSSException - an error has occurred
    * @throws RemoteException - required since this method can be called remotely
    */
   List getAllForUser(
      int iUserId
   ) throws OSSException,
            RemoteException;

   /**
    * Get all default roles assigned to the domain knowing just the domain ID.
    *
    * @param iDomainId - domain ID, can be DataObject.NEW_ID if you want to find out
    *                    default roles for new domain
    * @return List - list of default user roles or null if none exists
    * @throws OSSException - an error has occurred
    * @throws RemoteException - required since this method can be called remotely
    */
   List getAllForDomain(
      int iDomainId
   ) throws OSSException,
            RemoteException;

   /**
    * Get all roles belonging to the domain knowing just the domain ID.
    *
    * @param iDomainId - domain ID the belonging roles will be retrieved for
    * @return List - list of roles or null if none exists
    * @throws OSSException - an error has occurred
    * @throws RemoteException - required since this method can be called remotely
    */
   List getAllRolesInDomain(
      int iDomainId
   ) throws OSSException,
            RemoteException;

   /**
    * Efficiently get 3 ordered lists of roles if we known just 3 strings of 
    * role IDs. The same ID can be present in multiple lists
    * 
    * @param strRoleIds1 - comma separated list of the role IDs number 1
    * @param strRoleIds2 - comma separated list of the role IDs number 2
    * @param strRoleIds3 - comma separated list of the role IDs number 3
    * @return List[] - array of lists with Roles in the same order as they were
    *                  in the strings 
    * @throws OSSException - an error has occurred
    * @throws RemoteException - required since this method can be called remotely
    */
   List[] getOrderedLists(
      String strRoleIds1,
      String strRoleIds2,
      String strRoleIds3
   ) throws OSSException,
            RemoteException; 

   /**
    * Save role
    * 
    * @param data - role to save
    * @param deletedRightIds - comma separated list of IDs of access rights to delete
    * @param addedRights - List of added access rights
    * @return Role - updated Role, null if user doesn't have access 
    *                to that data object granted
    * @throws OSSException - an error has occurred
    * @throws RemoteException - required since this method can be called remotely
    */
   Role save(
      Role   data,
      String deletedRightIds,
      List   addedRights
   ) throws OSSException,
            RemoteException;

   /**
    * Delete roles
    * 
    * @param strIds - comma separated list of role IDs
    * @return int - number of deleted roles
    * @throws OSSException - an error has occurred
    * @throws RemoteException - required since this method can be called remotely
    */
   int delete(
      String strIds
   ) throws OSSException,
            RemoteException;
   
   /**
    * Enable or disable roles
    * 
    * @param strIds - comma separated list of role IDs
    * @param newEnableValue - new enabled value
    * @return int - number of modified  roles
    * @throws OSSException - an error has occurred
    * @throws RemoteException - required since this method can be called remotely
    */
   int updateEnable(
      String  strIds,
      boolean newEnableValue
   ) throws OSSException,
            RemoteException;

   /**
    * Create default role for self registration user
    *  
    * @param iDomainId - ID of the domain the role belongs to
    * @return Role - created self registration role
    * @throws OSSException - an error has occurred
    * @throws RemoteException - required since this method can be called remotely
    */
   Role createSelfRegistrationRole(
      int iDomainId
   ) throws OSSException,
            RemoteException;
   
   /**
    * Copy roles with belonging access rights from current domain to another.
    *  
    * @param iDomainId - domain ID the roles will be copied to
    * @param strRoleIds - comma separated list of role IDs that have to be copied
    * @return List - list of copied roles
    * @throws OSSException - an error has occurred
    * @throws RemoteException - required since this method can be called remotely
    */
   List copyRoles(
      int    iDomainId, 
      String strRoleIds
   ) throws OSSException,
            RemoteException;
}
