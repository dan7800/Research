/*
 * Copyright (c) 2003 - 2007 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: DomainController.java,v 1.20 2007/10/14 04:10:28 bastafidli Exp $
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
import org.opensubsystems.security.data.Domain;

/**
 * Business logic related to managing domains.
 *
 * @version $Id: DomainController.java,v 1.20 2007/10/14 04:10:28 bastafidli Exp $
 * @author Julo Legeny
 * @code.reviewer Miro Halas
 * @code.reviewed 1.9 2006/06/03 01:10:42 jlegeny
 */
public interface DomainController extends ModifiableDataController, 
                                          SecureController
{
   /**
    * Create domain and assign default roles.
    *
    * @param  data - domain to create
    * @param  strRoleIds - comma separated list of IDs of roles assigned to the 
    *                      domain
    * @return Domain - newly created domain object with assigned default roles, 
    *                  null if user doesn't have access to that data object granted
    * @throws OSSException - an error has occurred creating domain
    * @throws RemoteException - required since this method can be called remotely
    */
   Domain create(
      Domain data,
      String strRoleIds
   ) throws OSSException,
            RemoteException;

   /**
    * Save domain with assigned default roles.
    *
    * @param  data - domain to save
    * @param  strRemoveRoleIds - comma separated list of role IDs to remove 
    *                            (unassign) from the domain
    * @param  strAddRoleIds - comma separated list of role IDs to add (assign) 
    *                         to the domain
    * @return Domain - saved domain object, null if user doesn't have access 
    *                  to that data object granted
    * @throws OSSException - an error has occurred
    * @throws RemoteException - required since this method can be called remotely
    */
   Domain save(
      Domain data,
      String strRemoveRoleIds,
      String strAddRoleIds
   ) throws OSSException,
            RemoteException;

   /**
    * Delete domains
    * 
    * @param strIds - comma separated list of domain IDs
    * @return int - number of deleted domains
    * @throws OSSException - an error has occurred
    * @throws RemoteException - required since this method can be called remotely
    */
   int delete(
      String strIds
   ) throws OSSException,
            RemoteException;
   
   /**
    * Enable or disable domains
    * 
    * @param strIds - comma separated list of domain IDs
    * @param bNewEnableValue - new enabled value, if true domain will be enabled
    *                          if false, domain will be disabled
    * @return int - number of modified  domains
    * @throws OSSException - an error has occurred
    * @throws RemoteException - required since this method can be called remotely
    */
   int updateEnable(
      String  strIds,
      boolean bNewEnableValue
   ) throws OSSException,
            RemoteException;

   /**
    * Get current domain information with belonging default roles 
    * 
    * @return Domain - current domain with default roles
    * @throws OSSException - an error has occurred
    * @throws RemoteException - required since this method can be called remotely
    */
   Domain getCurrentDomainWithRoles(
   ) throws OSSException,
            RemoteException;

   /**
    * Get default roles assigned to the domain
    * 
    * @param iDomainId - domain ID
    * @return List - list of assigned default Roles to Domain
    * @throws OSSException - an error has occurred 
    * @throws RemoteException - required since this method can be called remotely
    */
   List getRoles(
      int iDomainId
   ) throws OSSException,
            RemoteException; 

   /**
    * Get domain information with all associated data that are part of this 
    * domain. 
    * 
    * @param iDomainId - domain ID, can be DataObject.NEW_ID if you want to get
    *                    current domain with its associated data (e.g. default roles)
    * @return Domain - domain with associated data (e.g. default roles)
    * @throws OSSException - an error has occurred
    * @throws RemoteException - required since this method can be called remotely
    */
   Domain getDomainWithAssociatedData(
      int iDomainId
   ) throws OSSException,
            RemoteException;

   /**
    * Get list of selfregistration domains data object knowing just the name. 
    * Don't check access rights here.
    * 
    * @param strIDs - string representation of IDs of the domain data objects to retrieve
    * @param strName - name of the domain data object to retrieve
    * @return Domain - retrieved domain data object, null if the data object 
    *                  doesn't exists
    * @throws OSSException - an error has occurred 
    * @throws RemoteException - required since this method can be called remotely
    */
   List getSelfregistrationDomains(
      String strIDs,
      String strName
   ) throws OSSException,
            RemoteException;
   
   /**
    * Check if there exist any domain in the system.
    * 
    * @return boolean - True if at least one domain exists in the system, 
    *                   otherwise false
    * @throws OSSException - an error has occurred
    * @throws RemoteException - required since this method can be called remotely
    */
   boolean existDomain(
   ) throws OSSException,
            RemoteException;
}
