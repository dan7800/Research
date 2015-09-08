/*
 * Copyright (c) 2003 - 2007 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: UserController.java,v 1.16 2007/09/13 06:18:54 bastafidli Exp $
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
import java.sql.Timestamp;
import java.util.List;

import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.logic.ModifiableDataController;
import org.opensubsystems.security.data.Domain;
import org.opensubsystems.security.data.User;

/**
 * Business logic related to managing users.
 *
 * @version $Id: UserController.java,v 1.16 2007/09/13 06:18:54 bastafidli Exp $
 * @author Julo Legeny
 * @code.reviewer Miro Halas
 * @code.reviewed 1.11 2006/06/03 06:11:07 bastafidli
 */
public interface UserController extends ModifiableDataController,
                                        SecureController
{
   /**
    * Get user knowing just the login name.
    *
    * @param strLoginName - login name of the user to get
    * @return User - specified user, null if the data object  doesn't exists 
    *                or if user doesn't have access to that data object granted
    * @throws OSSException - an error has occurred 
    * @throws RemoteException - required since this method can be called remotely
    */
   User get(
      String strLoginName
   ) throws OSSException,
            RemoteException;
   
   /**
    * Get default user data object template which is initialized to default 
    * values specified on a domain object.
    *
    * @param domain - domain data object the default values will be used from
    * @return User - retrieved user data object
    * @throws OSSException - an error has occurred 
    * @throws RemoteException - required since this method can be called remotely
    */
   User getDefaultUserForDomain(
      Domain domain
   ) throws OSSException,
            RemoteException;

   /**
    * Get user and all his assigned roles knowing just the user id.
    *
    * @param iUserId - id of user to retrieve, DataObject.NEW_ID if to retrieve
    *                  template for new user
    * @return Object[] - index 0 is user data and index 1 is list of his roles
    * @throws OSSException - an error has occurred 
    * @throws RemoteException - required since this method can be called remotely
    */
   Object[] getUserWithRoles(
      int iUserId
   ) throws OSSException,
            RemoteException;

   /**
    * Get roles assigned to the user
    * 
    * @param iUserId - user ID
    * @return List - list of assigned Roles of User
    * @throws OSSException - an error has occurred 
    * @throws RemoteException - required since this method can be called remotely
    */
   List getRoles(
      int iUserId
   ) throws OSSException,
            RemoteException; 

   /**
    * Create user.
    *
    * @param  data - user to create
    * @param  strRoleIds - IDs of roles assigned to the user
    * @return User - newly created user object, null if user doesn't have access 
    *                to that data object granted
    * @throws OSSException - an error has occurred creating user
    * @throws RemoteException - required since this method can be called remotely
    */
   User create(
      User   data,
      String strRoleIds
   ) throws OSSException,
            RemoteException;

   /**
    * Create domain with user account. This method is used for creating new account.
    * 
    * @param dDomain - domain that has to be created as new or new user 
    *           account has to be joined for
    * @param uUser - user the account will be created for
    * @return Object[] - index 0 is created domain data and index 1 is created user data
    * @throws OSSException - an error has occurred
    * @throws RemoteException - required since this method can be called remotely
    */
   Object[] create(
      Domain dDomain,
      User   uUser
   ) throws OSSException,
            RemoteException;

   /**
    * Save user.
    *
    * @param  data - user to save
    * @param  strRemoveRoleIds - user role IDs to remove (unassign) from the user
    * @param  strAddRoleIds - user role IDs to add (assign) to the user
    * @return User - saved user object, null if user doesn't have access 
    *                to that data object granted
    * @throws OSSException - an error has occurred
    * @throws RemoteException - required since this method can be called remotely
    */
   User save(
      User   data,
      String strRemoveRoleIds,
      String strAddRoleIds
   ) throws OSSException,
            RemoteException;

   /**
    * Delete users
    *
    * @param strIds - comma separated list of user IDs
    * @return int - number of deleted users
    * @throws OSSException - an error has occurred
    * @throws RemoteException - required since this method can be called remotely
    */
   int delete(
      String strIds
   ) throws OSSException,
            RemoteException;

   /**
    * Enable or disable users
    * 
    * @param strIds - comma separated list of user IDs
    * @param newEnableValue - new enabled value
    * @return int - number of modified  users
    * @throws OSSException - an error has occurred
    * @throws RemoteException - required since this method can be called remotely
    */
   int updateEnable(
      String  strIds,
      boolean newEnableValue
   ) throws OSSException,
            RemoteException;
   
   /**
    * Change users password
    * 
    * @param strLoginName - login name of user which password will be changed
    * @param strCurrentPassword - current password of that user
    * @param strNewPassword - new password
    * @throws OSSException - an error has occurred
    * @throws RemoteException - required since this method can be called remotely
    */
   void changePassword(
      String strLoginName,
      String strCurrentPassword,
      String strNewPassword
   ) throws OSSException,
            RemoteException;

   /**
    * Change login name and the email for the user
    *
    * @param  iUserId - id of the user to change
    * @param  tsLastModified - time when it was last modified
    * @param  strLoginName - new login name
    * @param  strEmail - new email for the user (since it is unique as well)
    * @return User - saved user object, null if user doesn't have access 
    *                to that data object granted
    * @throws OSSException - an error has occurred
    * @throws RemoteException - required since this method can be called remotely
    */
   User changeLoginNameAndEmail(
      int       iUserId,
      Timestamp tsLastModified,
      String    strLoginName,
      String    strEmail
   ) throws OSSException,
            RemoteException;

   /**
    * Check if there exist any enabled internal users.
    * 
    * @param iDomain - domain to check in, this is specified since this check
    *                  can be performend during initialization when nobody is
    *                  logged in to determine if default user should be created
    * @param ignoreLoginNames - list of login names to ignore during check
    * @return boolean - true if there are any enabled internal users
    * @throws OSSException - an error has occurred
    * @throws RemoteException - required since this method can be called remotely
    */
   boolean checkForInternalEnabledUsers(
      int      iDomain,
      String[] ignoreLoginNames
   ) throws OSSException,
            RemoteException;
}
