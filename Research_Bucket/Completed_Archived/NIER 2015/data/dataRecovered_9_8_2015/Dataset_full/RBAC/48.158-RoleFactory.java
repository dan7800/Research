/*
 * Copyright (c) 2003 - 2007 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: RoleFactory.java,v 1.11 2007/09/13 06:37:45 bastafidli Exp $
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
import org.opensubsystems.core.persist.BasicDataFactory;
import org.opensubsystems.patterns.listdata.data.SimpleRule;
import org.opensubsystems.patterns.listdata.persist.ListFactory;
import org.opensubsystems.security.data.Role;
import org.opensubsystems.security.data.User;

/**
 * Methods to create, retrieve and manipulate roles in the persistence 
 * store.
 * 
 * @version $Id: RoleFactory.java,v 1.11 2007/09/13 06:37:45 bastafidli Exp $
 * @author Peter Satury
 * @code.reviewer Miro Halas
 * @code.reviewed Initial revision
 */
public interface RoleFactory extends BasicDataFactory, ListFactory 
{   
   /**
    * Update enable flag of list of data objects.
    *
    * @param arrIds - array of data IDs
    * @param bNewEnableValue - new enabled value
    * @param listSecurityData - security data describing what data can be modified
    * @return int - number of updated data items
    * @throws OSSException - error during update
    */
   int updateEnable(
      int[]      arrIds,
      boolean    bNewEnableValue,
      SimpleRule listSecurityData
   ) throws OSSException;

   /**
    * Get IDs of the data objects which still exists and can be accessed according 
    * to specified security constraints.
    * 
    * @param strIds - string of role IDs separated by ','
    * @param listSecurityData - security data describing what data can be retrieved
    * @return int[] - array of role Ids
    * @throws OSSException - an error has occurred
    */
   int[] getActualIds(
      String     strIds,
      SimpleRule listSecurityData
   ) throws OSSException;
   
   /**
    * Method to get actual number of data objects identified by the IDs
    * ignoring those which do not exist anymore.
    *
    * @param strIds - string of data IDs separated by ','
    * @return int - number of Ids which really exists
    * @throws OSSException - an error has occurred
    */
   int getActualCount(
      String strIds
   ) throws OSSException;
   
   /**
    * Get specified list of roles.
    *
    * @param  strIds - string representation of the IDs to retrieve
    * @param  lsdSecurity - security data describing what data can be retrieved
    * @return List - list of roles or null if none exists or can be retrieved
    * @throws OSSException - an error has occurred
    */
   List get(
      String     strIds,
      SimpleRule lsdSecurity
   ) throws OSSException;

   /**
    * Update role in the persistence store but only if the data in the 
    * persistence store match the expectation about type of role.
    * 
    * @param data - Role to updated
    * @param roleType - what type of role to update
    *                   (@see org.opensubsystems.security.util.RoleConstants)
    * @return - Role after update
    * @throws OSSException - an error has occurred
    */
   Role save(
      Role data,
      int  roleType
   ) throws OSSException;
   
   /**
    * Create personal role for specified user.
    * 
    * @param userData - user to create personal rolefor 
    * @return Role - created personal role
    * @throws OSSException - an error has occurred
    */
   Role createPersonal(
      User userData
   ) throws OSSException;
   
   /**
    * Update personal role according to changes in user object.
    * 
    * @param userData - user to update personal role for
    * @return Role - updated personal role
    * @throws OSSException - an error has occurred
    */
   Role savePersonal(
      User userData
   ) throws OSSException;
   
   /**
    * Delete personal roles for specified users.
    * 
    * @param arrUserIds - array of user IDs
    * @return int - number of deleted roles 
    * @throws OSSException - an error has occurred
    */
   int deletePersonal(
      int[] arrUserIds
   ) throws OSSException;

   
   /**
    * Get personal role for specified user.
    * 
    * @param iUserId - user ID
    * @return Role - personal role that belongs to user specified by iUserId
    * @throws OSSException - an error has occurred
    */
   Role getPersonal(
      int iUserId
   ) throws OSSException;

   /**
    * Check if any of the specified IDs identifies personal role.
    *  
    * @param colIds - set of role IDs
    * @return boolean - true if at least one role ID represents personal role 
    * @throws OSSException - an error has occurred
    */
   boolean isAnyOfSpecifiedPersonal(
      Collection colIds
   ) throws OSSException;
   
   /**
    * Delete list of roles.
    * 
    * @param arrIds - array of IDs to delete
    * @param listSecurityData - security data describing what data can be accessed
    * @param roleType - what type of Roles should be only deleted 
    *                   (@see org.opensubsystems.security.util.RoleConstants)
    * @return int - number of deleted records
    * @throws OSSException - error during delete
    */ 
   int delete(
      int[]      arrIds,
      SimpleRule listSecurityData,
      int        roleType
   ) throws OSSException;
   
   /**
    * Get all user roles assigned to the user just knowing user ID.
    *
    * @param iUserId - user ID
    * @param listSecurityData - security data describing what data can be accessed
    * @return List - list of user roles or null if none exists
    * @throws OSSException - an error while getting user roles
    */
   List getAllForUser(
      int        iUserId,
      SimpleRule listSecurityData
   ) throws OSSException;
   
   /**
    * Get all default user roles assigned to the domain just knowing domain ID.
    *
    * @param iDomainId - domain ID
    * @param listSecurityData - security data describing what data can be accessed
    * @return List - list of deafult user roles or null if none exists
    * @throws OSSException - an error while getting default user roles
    */
   List getAllForDomain(
      int        iDomainId,
      SimpleRule listSecurityData
   ) throws OSSException;
   
   /**
    * Get all roles within the domain just knowing domain ID.
    *
    * @param iDomainId - domain ID
    * @param listSecurityData - security data describing what data can be accessed
    * @return List - list of roles or null if none exists
    * @throws OSSException - an error while getting roles
    */
   List getAllRolesInDomain(
      int        iDomainId,
      SimpleRule listSecurityData
   ) throws OSSException;
   
   /**
    * Get default selfregistered user role within the specified domain.
    *
    * @param iDomainId - domain ID
    * @return Role - default selfregistered user role roles or null if none exists
    * @throws OSSException - an error while getting roles
    */
   Role getDefaultSelfregisteredRole(
      int iDomainId
   ) throws OSSException;

   /**
    * Assign new roles to user.
    * 
    * @param iUserId - ID of the user
    * @param strRoleIds - string of roles IDs separated by ',' that should be
    *                     assigned to user
    * @return int - number of assigned items
    * @throws OSSException - error during insert
    */ 
   int assignToUser(
      int    iUserId,
      String strRoleIds
   ) throws OSSException;

   
   /**
    * Assign new default roles to domain.
    * 
    * @param iDomainId - ID of the domain
    * @param strRoleIds - string of roles IDs separated by ',' that should be
    *                     assigned to domain
    * @return int - number of assigned items
    * @throws OSSException - error during insert
    */ 
   int assignToDomain(
      int    iDomainId,
      String strRoleIds
   ) throws OSSException;

   /**
    * Remove assigned roles from user
    * 
    * @param iUserId - ID of the user
    * @param strRoleIds - list of roles IDs separated by ',' that should be
    *                     removed from user
    * @return int - number of removed items
    * @throws OSSException - error during delete
    */ 
   int removeFromUser(
      int    iUserId,
      String strRoleIds
   ) throws OSSException;

   /**
    * Remove assigned default roles from domain
    * 
    * @param iDomainId - ID of the domain
    * @param strRoleIds - list of roles IDs separated by ',' that should be
    *                     removed from domain
    * @return int - number of removed items
    * @throws OSSException - error during delete
    */ 
   int removeFromDomain(
      int    iDomainId,
      String strRoleIds
   ) throws OSSException;

   /**
    * Remove assigned roles from user
    * 
    * @param strUserIds - string of user IDs the roles will be deleted for
    * @param bRemoveExceptSpecified - flag signaling if there will be 
    *        removed all records except specified or not
    *        true - remove all user roles except specified
    *        false - remove specified user roles
    * @return int - number of removed items
    * @throws OSSException - error during delete
    */ 
   int removeFromUsers(
      String strUserIds,
      boolean bRemoveExceptSpecified
   ) throws OSSException;

   /**
    * Remove assigned default roles from domain
    * 
    * @param strDomainIds - string of domain IDs the roles will be deleted for
    * @param bRemoveExceptSpecified - flag signaling if there will be 
    *        removed all records except specified or not
    *        true - remove all user roles except specified
    *        false - remove specified user roles
    * @return int - number of removed items
    * @throws OSSException - error during delete
    */ 
   int removeFromDomains(
      String strDomainIds,
      boolean bRemoveExceptSpecified
   ) throws OSSException;
}
