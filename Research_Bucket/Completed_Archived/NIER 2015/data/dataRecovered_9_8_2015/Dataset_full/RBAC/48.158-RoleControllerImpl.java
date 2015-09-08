/*
 * Copyright (c) 2003 - 2008 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: RoleControllerImpl.java,v 1.43 2009/04/22 06:29:13 bastafidli Exp $
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

package org.opensubsystems.security.logic.impl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opensubsystems.core.data.DataDescriptorManager;
import org.opensubsystems.core.data.DataObject;
import org.opensubsystems.core.data.ModifiableDataObject;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.error.OSSInconsistentDataException;
import org.opensubsystems.core.error.OSSInternalErrorException;
import org.opensubsystems.core.error.OSSInvalidDataException;
import org.opensubsystems.core.logic.ControllerManager;
import org.opensubsystems.core.logic.impl.StatelessControllerImpl;
import org.opensubsystems.core.persist.DataFactoryManager;
import org.opensubsystems.core.util.CallContext;
import org.opensubsystems.core.util.DataObjectOrderingComparator;
import org.opensubsystems.core.util.Messages;
import org.opensubsystems.core.util.StringUtils;
import org.opensubsystems.patterns.listdata.data.SimpleRule;
import org.opensubsystems.security.data.AccessRight;
import org.opensubsystems.security.data.Role;
import org.opensubsystems.security.data.RoleDataDescriptor;
import org.opensubsystems.security.data.SecurityDefinition;
import org.opensubsystems.security.logic.AuthorizationController;
import org.opensubsystems.security.logic.RoleController;
import org.opensubsystems.security.persist.AccessRightFactory;
import org.opensubsystems.security.persist.RoleFactory;
import org.opensubsystems.security.util.ActionConstants;
import org.opensubsystems.security.util.RoleConstants;
import org.opensubsystems.security.util.RoleUtils;

/**
 * Implementation of RoleController interface to manage roles.
 * 
 * View-type has to be set to local due to bug XDT-867 affecting WebSphere
 * Refs has to be set to local JNDI name since we do not want to use remote objects.
 * 
 * @ejb.bean type="Stateless"
 *           name="RoleController"
 *           view-type="local" 
 *           jndi-name="org.opensubsystems.security.logic.RoleControllerRemote"
 *           local-jndi-name="org.opensubsystems.security.logic.RoleController"
 * @ejb.interface 
 *     local-extends="javax.ejb.EJBLocalObject, org.opensubsystems.security.logic.RoleController"
 *     extends="javax.ejb.EJBObject, org.opensubsystems.security.logic.RoleController"
 *
 * @ejb.ejb-ref ejb-name="AuthorizationController"
 *              ref-name="org.opensubsystems.security.logic.AuthorizationController"
 * 
 * @jonas.bean ejb-name="RoleController"
 *             jndi-name="org.opensubsystems.security.logic.RoleControllerRemote"
 *
 * @jonas.ejb-ref ejb-ref-name="org.opensubsystems.security.logic.AuthorizationController"
 *                jndi-name="org.opensubsystems.security.logic.AuthorizationController"
 * 
 * @jboss.ejb-ref-jndi ref-name="org.opensubsystems.security.logic.AuthorizationController"
 *                     jndi-name="org.opensubsystems.security.logic.AuthorizationControllerRemote"
 * @jboss.ejb-local-ref ref-name="org.opensubsystems.security.logic.AuthorizationController"
 *                     jndi-name="org.opensubsystems.security.logic.AuthorizationController"
 *
 * @weblogic.ejb-reference-description 
 *               ejb-ref-name="org.opensubsystems.security.logic.AuthorizationController"
 *               jndi-name="org.opensubsystems.security.logic.AuthorizationControllerRemote"
 * @weblogic.ejb-local-reference-description 
 *               ejb-ref-name="org.opensubsystems.security.logic.AuthorizationController"
 *               jndi-name="org.opensubsystems.security.logic.AuthorizationController"
 *
 * @version $Id: RoleControllerImpl.java,v 1.43 2009/04/22 06:29:13 bastafidli Exp $
 * @author Peter Satury
 * @code.reviewer Miro Halas
 * @code.reviewed Initial revision
 */
public class RoleControllerImpl extends    StatelessControllerImpl 
                                implements RoleController
{
   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Factory to use to execute persistence operations.
    */
   protected RoleFactory m_roleFactory;

   /**
    * Factory to use to execute persistence operations.
    */
   protected AccessRightFactory m_accessRightFactory;

   /**
    * Authorization controller used to check access rights.
    */
   protected AuthorizationController m_authorityControl;

   /**
    * Data descriptor for the role data types.
    */
   protected RoleDataDescriptor m_roleDescriptor;

   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Generated serial version id for this class.
    */
   private static final long serialVersionUID = 481687993757986840L;

   // Constructors /////////////////////////////////////////////////////////////

   /**
    * Default constructor.
    */
   public RoleControllerImpl(
   ) 
   {
      super();
      
      // Do not cache anything here since if this controller is run as a stateless
      // session bean the referenced objects may not be ready
      m_roleFactory = null;
      m_accessRightFactory = null;
      m_authorityControl = null;
   }
   
   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * {@inheritDoc}
    * 
    * @ejb.interface-method
    * @ejb.transaction type="Supports"
    */
   public DataObject get(
      int iId
   ) throws OSSException 
   {
      Role data = null;
      
      // We need to get the data before we can check if the user has access
      // granted based on the attributes of the data
      data = (Role)m_roleFactory.get(
                      iId, CallContext.getInstance().getCurrentDomainId());
      if (data != null)
      {
         if (checkAccess(data, ActionConstants.RIGHT_ACTION_VIEW) 
            != AccessRight.ACCESS_GRANTED)
         {
            data = null;
            CallContext.getInstance().getMessages().addMessage(
               Messages.ACCESSRIGHT_ERRORS, "No rights to view role.");
         }
         else
         {
            data.setAccessRights(m_accessRightFactory.getAllForRole(iId));
         }
      }

      return data;
   }
   
   /**
    * {@inheritDoc}
    * 
    * @ejb.interface-method
    * @ejb.transaction type="Required"
    */
   public DataObject create(
      DataObject data
   ) throws OSSException 
   {
      Role newdata = (Role)data;
      Role createddata = null;

      // Validate data before we hit the database to improve performance
      validate(newdata);
      if (checkAccess(newdata, ActionConstants.RIGHT_ACTION_CREATE) 
         == AccessRight.ACCESS_GRANTED)
      {
         createddata = (Role)m_roleFactory.create(newdata);
         // create and assign access rights
         List addedRights = null;

         addedRights = newdata.getAccessRights();  
         if ((addedRights != null) && (addedRights.size() > 0))
         {
            createddata.setAccessRights(m_accessRightFactory.create(addedRights, 
                                                                    createddata.getId()));
         }   
      }
      else
      {
         CallContext.getInstance().getMessages().addMessage(
            Messages.ACCESSRIGHT_ERRORS, "No rights to create role.");
      }

      return createddata;
   }
   
   /**
    * {@inheritDoc}
    * 
    * @ejb.interface-method
    * @ejb.transaction type="Required"
    */
   public void delete(
      int iId
   ) throws OSSException
   {
      // Security check performed in the called method
      delete(Integer.toString(iId));
   }

   /**
    * {@inheritDoc}
    * 
    * @ejb.interface-method
    * @ejb.transaction type="Required"
    */
   public ModifiableDataObject save(
      ModifiableDataObject data
   ) throws OSSException
   {
      Role newdata = (Role)data;
      Role olddata = null;
      Role saveddata = null;
      
      // Validate data before we hit the database to improve performance
      validate(newdata);
      // Since we are modifying and the security is based on the categories, 
      // we need check that we have rights to modify the data in the old 
      // categories as well as the data in the new categories (if they were changed)
      olddata = (Role)m_roleFactory.get(
                         data.getId(),
                         CallContext.getInstance().getCurrentDomainId());
      if (olddata != null)
      {
         if ((checkAccess(olddata, ActionConstants.RIGHT_ACTION_MODIFY) 
            == AccessRight.ACCESS_GRANTED)
            && (checkAccess(newdata, ActionConstants.RIGHT_ACTION_MODIFY) 
             == AccessRight.ACCESS_GRANTED))
         {
            // Save role, but we cannot trust client what kind of data is sending
            // us so we need to save it only if the reference to user wasn't changed
            saveddata = (Role)m_roleFactory.save(newdata,
                                                 // If it is not personal now, then save
                                                 // only if the original wasn't personal
                                                 // and if user id is set (it is personal)
                                                 // then save only if it was personal
                                                 olddata.getUserId() == DataObject.NEW_ID 
                                                    ? RoleConstants.SAVE_ROLE_NONPERSONAL 
                                                    : RoleConstants.SAVE_ROLE_PERSONAL
                     
                     );
            if (!olddata.isUnmodifiable())
            {
               // First delete all
               m_accessRightFactory.deleteAllForRole(newdata.getId());
               
               // And now create the ones we should have
               List addedRights = null;
      
               addedRights = newdata.getAccessRights();  
               if ((addedRights != null) && (addedRights.size() > 0))
               {
                  saveddata.setAccessRights(m_accessRightFactory.create(addedRights, 
                                                                        newdata.getId()));
               }
            }
         }
         else
         {
            saveddata = null;      
            CallContext.getInstance().getMessages().addMessage(
               Messages.ACCESSRIGHT_ERRORS, "No rights to modify role.");
   
         }
      }
            
      return saveddata;
   }

   /**
    * {@inheritDoc}
    * 
    * @ejb.interface-method
    * @ejb.transaction type="Supports"
    */   
   public List get(
      String strRoleIds
   ) throws OSSException
   {
      List lstReturn = null;
      
      SimpleRule lsdSecurity = null;
      try
      {
         lsdSecurity = m_authorityControl.getRightsForCurrentUser(
                                            m_roleDescriptor.getDataType(),
                                            ActionConstants.RIGHT_ACTION_VIEW);
      }
      catch (RemoteException rExc)
      {
         // We cannot propagate this exception otherwise XDoclet would generate 
         // the local interface incorrectly since it would include the declared
         // RemoteException in it (to propagate we would have to declare it)
         throw new OSSInternalErrorException("Remote error occurred", rExc);
      }

      if (lsdSecurity == null)
      {
         CallContext.getInstance().getMessages().addMessage(
            Messages.ACCESSRIGHT_ERRORS, "No rights to view roles");
      }
      else
      {
         if ((strRoleIds != null) && (strRoleIds.trim().length() > 0))
         {
            // Return list of roles 
            lstReturn = m_roleFactory.get(strRoleIds, lsdSecurity);
         }
      }
      
      return lstReturn;
   }

   /**
    * {@inheritDoc}
    * 
    * @ejb.interface-method
    * @ejb.transaction type="Supports"
    */
   public List getAllForUser(
      int iUserId
   ) throws OSSException
   {
      // Test for this method is included within RoleControllerSecurityTest class 
      List lstReturn = null;
      
      SimpleRule lsdSecurity = null;
      try
      {
         lsdSecurity = m_authorityControl.getRightsForCurrentUser(
                                            m_roleDescriptor.getDataType(),
                                            ActionConstants.RIGHT_ACTION_VIEW);
      }
      catch (RemoteException rExc)
      {
         // We cannot propagate this exception otherwise XDoclet would generate 
         // the local interface incorrectly since it would include the declared
         // RemoteException in it (to propagate we would have to declare it)
         throw new OSSInternalErrorException("Remote error occurred", rExc);
      }

      if (lsdSecurity == null)
      {
         CallContext.getInstance().getMessages().addMessage(
            Messages.ACCESSRIGHT_ERRORS, "No rights to view roles");
      }
      else
      {
         // Return list of roles 
         lstReturn = m_roleFactory.getAllForUser(iUserId, lsdSecurity);
      }
      
      return lstReturn;
   }

   /**
    * {@inheritDoc}
    * 
    * @ejb.interface-method
    * @ejb.transaction type="Supports"
    */
   public List getAllForDomain(
      int iDomainId
   ) throws OSSException
   {
      // Test for this method is included in the testDomains() method 
      // within the DomainControllerTest class 
      List lstReturn = null;
      
      SimpleRule lsdSecurity = null;
      
      if (iDomainId == CallContext.getInstance().getCurrentDomainId())
      {
         // if user from current domain wants to modify current domain, get
         // security conditions that will be used
         try
         {
            lsdSecurity = m_authorityControl.getRightsForCurrentUser(
                                               m_roleDescriptor.getDataType(),
                                               ActionConstants.RIGHT_ACTION_VIEW);
         }
         catch (RemoteException rExc)
         {
            // We cannot propagate this exception otherwise XDoclet would generate 
            // the local interface incorrectly since it would include the declared
            // RemoteException in it (to propagate we would have to declare it)
            throw new OSSInternalErrorException("Remote error occurred", rExc);
         }
      }
      else
      {
         // for new (current) domain or not current domain don't perform 
         // security check and load all data
         lsdSecurity = SimpleRule.ALL_DATA;
      }

      // if user from current domain wants to modify current domain, get
      // security conditions that will be used
      if (lsdSecurity == null)
      {
         CallContext.getInstance().getMessages().addMessage(
            Messages.ACCESSRIGHT_ERRORS, "No rights to view roles");
      }
      else
      {
         // Return list of roles 
         lstReturn = m_roleFactory.getAllForDomain(iDomainId, lsdSecurity);
      }
      
      return lstReturn;
   }

   /**
    * {@inheritDoc}
    * 
    * @ejb.interface-method
    * @ejb.transaction type="Supports"
    */
   public List getAllRolesInDomain(
      int iDomainId
   ) throws OSSException
   {
      List lstReturn = null;
      
      SimpleRule lsdSecurity = null;
      
      if (iDomainId == CallContext.getInstance().getCurrentDomainId())
      {
         // if user from current domain wants to modify current domain, get
         // security conditions that will be used
         try
         {
            lsdSecurity = m_authorityControl.getRightsForCurrentUser(
                                               m_roleDescriptor.getDataType(),
                                               ActionConstants.RIGHT_ACTION_VIEW);
         }
         catch (RemoteException rExc)
         {
            // We cannot propagate this exception otherwise XDoclet would generate 
            // the local interface incorrectly since it would include the declared
            // RemoteException in it (to propagate we would have to declare it)
            throw new OSSInternalErrorException("Remote error occurred", rExc);
         }
      }
      else
      {
         // for new (current) domain or not current domain don't perform 
         // security check and load all data
         lsdSecurity = SimpleRule.ALL_DATA;
      }

      // if user from current domain wants to modify current domain, get
      // security conditions that will be used
      if (lsdSecurity == null)
      {
         CallContext.getInstance().getMessages().addMessage(
            Messages.ACCESSRIGHT_ERRORS, "No rights to view roles");
      }
      else
      {
         // Return list of roles 
         lstReturn = m_roleFactory.getAllRolesInDomain(iDomainId, lsdSecurity);
      }
      
      return lstReturn;
   }

   /**
    * {@inheritDoc}
    * 
    * @ejb.interface-method
    * @ejb.transaction type="Supports"
    */
   public List[] getOrderedLists(
      String strRoleIds1,
      String strRoleIds2,
      String strRoleIds3
   ) throws OSSException
   {
      List[] arrListReturn = new List[] {null, null, null};

      Iterator itHelp;

      StringBuffer sbHelp;
      String strHelp1 = "";
      String strHelp2 = "";
      String strHelp3 = "";
      Role rHelp;
      List lstRoles;
      
      sbHelp = new StringBuffer();
      if ((strRoleIds1 != null) && (strRoleIds1.length() > 0))
      {
         sbHelp.append(strRoleIds1);
      }
      if ((strRoleIds2 != null) && (strRoleIds2.length() > 0))
      {
         if (sbHelp.length() > 0)
         {
            sbHelp.append(",");
         }
         sbHelp.append(strRoleIds2);
      }
      if ((strRoleIds3 != null) && (strRoleIds3.length() > 0))
      {
         if (sbHelp.length() > 0)
         {
            sbHelp.append(",");
         }
         sbHelp.append(strRoleIds3);
      }
               
      if (sbHelp.length() > 0)
      {
         lstRoles = get(sbHelp.toString());
         if ((lstRoles != null) && (lstRoles.size() > 0))
         {
            if ((strRoleIds1 != null) && (strRoleIds1.length() > 0))
            {
               // clear help buffer that will be used for constructing of strHelp1 strings
               sbHelp.delete(0, sbHelp.length());
               sbHelp.append(",");
               sbHelp.append(strRoleIds1);
               sbHelp.append(",");
               strHelp1 = sbHelp.toString();
            }
            if ((strRoleIds2 != null) && (strRoleIds2.length() > 0))
            {
               // clear help buffer that will be used for constructing of strHelp2 strings
               sbHelp.delete(0, sbHelp.length());
               sbHelp.append(",");
               sbHelp.append(strRoleIds2);
               sbHelp.append(",");
               strHelp2 = sbHelp.toString();
            }
            if ((strRoleIds3 != null) && (strRoleIds3.length() > 0))
            {
               // clear help buffer that will be used for constructing of strHelp3 strings
               sbHelp.delete(0, sbHelp.length());
               sbHelp.append(",");
               sbHelp.append(strRoleIds3);
               sbHelp.append(",");
               strHelp3 = sbHelp.toString();
            }
   
            for (itHelp = lstRoles.iterator(); itHelp.hasNext();)
            {
               rHelp = (Role) itHelp.next();
               // clear help buffer that will be used for constructing of temporary strings
               sbHelp.delete(0, sbHelp.length());
               sbHelp.append(",");
               sbHelp.append(rHelp.getId());
               sbHelp.append(",");

               if (strHelp1.indexOf(sbHelp.toString()) > -1)
               {
                  if (arrListReturn[0] == null)
                  {
                     arrListReturn[0] = new ArrayList();
                  }
                  arrListReturn[0].add(rHelp);
               }
               if (strHelp2.indexOf(sbHelp.toString()) > -1)
               {
                  if (arrListReturn[1] == null)
                  {
                     arrListReturn[1] = new ArrayList();
                  }
                  arrListReturn[1].add(rHelp);
               }
               if (strHelp3.indexOf(sbHelp.toString()) > -1)
               {
                  if (arrListReturn[2] == null)
                  {
                     arrListReturn[2] = new ArrayList();
                  }
                  arrListReturn[2].add(rHelp);
               }
            }
   
            // Sort first list
            if (arrListReturn[0] != null && arrListReturn[0].size() > 1)
            {
               Collections.sort(arrListReturn[0], new DataObjectOrderingComparator(
                                             StringUtils.parseStringToIntArray(strRoleIds1, ",")));
            }
            
            // Sort second list
            if (arrListReturn[1] != null && arrListReturn[1].size() > 1)
            {
               // order Maintainers
               Collections.sort(arrListReturn[1], new DataObjectOrderingComparator(
                                             StringUtils.parseStringToIntArray(strRoleIds2, ",")));
            }
         
            // Sort third list
            if (arrListReturn[2] != null && arrListReturn[2].size() > 1)
            {
               Collections.sort(arrListReturn[2], new DataObjectOrderingComparator(
                                             StringUtils.parseStringToIntArray(strRoleIds3, ",")));
            }
         }
      }
      return arrListReturn;
   }
   
   /**
    * {@inheritDoc}
    * 
    * @ejb.interface-method
    * @ejb.transaction type="Required"
    */
   public Role save(
      Role   data, 
      String deletedRightIds, 
      List   addedRights
   ) throws OSSException 
   {
      Role newdata = (Role)data;
      Role olddata = null;
      Role saveddata = null;
      
      // Validate data before we hit the database to improve performance
      validate(newdata);
      // Since we are modifying and the security is based on the categories, 
      // we need check that we have rights to modify the data in the old 
      // categories as well as the data in the new categories (if they were changed)
      olddata = (Role)m_roleFactory.get(
                         data.getId(),
                         CallContext.getInstance().getCurrentDomainId());
      if (olddata != null)
      {
         if ((checkAccess(olddata, ActionConstants.RIGHT_ACTION_MODIFY) 
            == AccessRight.ACCESS_GRANTED)
            && (checkAccess(newdata, ActionConstants.RIGHT_ACTION_MODIFY) 
             == AccessRight.ACCESS_GRANTED))
         {
            // Save role, but we cannot trust client what kind of data is sending
            // us so we need to save it only if the reference to user wasn't changed
            saveddata = (Role)m_roleFactory.save(newdata,
                                                 // If it is not personal now, then save
                                                 // only if the original wasn't personal
                                                 // and if user id is set (it is personal)
                                                 // then save only if it was personal
                                                 olddata.getUserId() == DataObject.NEW_ID 
                                                    ? RoleConstants.SAVE_ROLE_NONPERSONAL 
                                                    : RoleConstants.SAVE_ROLE_PERSONAL);
            if (!olddata.isUnmodifiable())
            {
               if ((deletedRightIds != null) && (deletedRightIds.length() > 0))
               {
                  m_accessRightFactory.delete(newdata.getId(), deletedRightIds);
               }
               if ((addedRights != null) && (addedRights.size() > 0))
               {
                  m_accessRightFactory.create(addedRights, newdata.getId());
               }
               // Since there could be some existing rights, which were not
               // deleted, we must load all access rights from the database
               saveddata.setAccessRights(m_accessRightFactory.getAllForRole(newdata.getId()));
            }
         }
         else
         {
            CallContext.getInstance().getMessages().addMessage(
               Messages.ACCESSRIGHT_ERRORS, "No rights to modify role.");
         }
      }
       
      return saveddata;
   }

   /**
    * {@inheritDoc}
    * 
    * @ejb.interface-method
    * @ejb.transaction type="Required"
    */
   public int delete(
      String strIds
   ) throws OSSException
   {
      int        iDeleted    = 0;
      SimpleRule lsdSecurity = null;

      OSSInvalidDataException ideException = null;
      
      if ((strIds == null) || (strIds.length() == 0))
      {
         ideException = OSSInvalidDataException.addException(ideException,
                           RoleDataDescriptor.COL_ROLE_ID, 
                           "You have not specified any roles to delete.");
      }

      if (ideException != null)
      {               
         throw ideException;
      }

      try
      {
         lsdSecurity = m_authorityControl.getRightsForCurrentUser(
                                           m_roleDescriptor.getDataType(),
                                           ActionConstants.RIGHT_ACTION_DELETE);
      }
      catch (RemoteException rExc)
      {
         // We cannot propagate this exception otherwise XDoclet would generate 
         // the local interface incorrectly since it would include the declared
         // RemoteException in it (to propagate we would have to declare it)
         throw new OSSInternalErrorException("Remote error occurred", rExc);
      }

      if (lsdSecurity == null)
      {
         CallContext.getInstance().getMessages().addMessage(
            Messages.ACCESSRIGHT_ERRORS, "No rights to delete roles");
      }
      else
      {
         int iAttempted = 0;
         int iActual    = 0;
         int[] arrActualIds  = null;

         // Not all of the ids may still exist or can be accessed due to 
         // security restrictions. Figure out first what ids do exists
         // and can be accesses so that we can produce nice error message
         arrActualIds = m_roleFactory.getActualIds(strIds, lsdSecurity);
         
         if ((arrActualIds != null) && (arrActualIds.length > 0))
         {
            iActual = arrActualIds.length;
            // Delete only modifiable since only those can be deleted
            iDeleted = m_roleFactory.delete(arrActualIds, lsdSecurity, 
                                            RoleConstants.COUNT_WITH_MODIFIABLE_ONLY);
            if (iActual != iDeleted)
            {
               throw new OSSInconsistentDataException(
                  "Deleted role count not equal to determined actual role count.");
            }               
         }
         iAttempted = StringUtils.count(strIds, ',') + 1;

         if (iAttempted != iDeleted)
         {
            if (iDeleted == 0)
            {
               CallContext.getInstance().getMessages().addErrorMessage(
                  " No roles have been deleted either because some roles were already" +
                  " deleted or because some of the roles are system generated and cannot " +
                  " be deleted directly or you don't have rights to delete all the specified roles."
               );
            }
            else
            {
               CallContext.getInstance().getMessages().addErrorMessage(
                  "Only " + iDeleted + " role(s) have been deleted either" +
                  " because some roles were already deleted or because some of" +
                  " the roles are system generated and cannot be deleted directly" +
                  " or because you don't have rights to delete all the specified roles."
               );
            }
         }            
      }

      return iDeleted;
   }   

   /**
    * {@inheritDoc}
    * 
    * @ejb.interface-method
    * @ejb.transaction type="Required"
    */
   public int updateEnable(
      String  strIds,
      boolean newEnableValue
   ) throws OSSException
   {
      int        iModified   = 0;
      SimpleRule lsdSecurity = null;

      OSSInvalidDataException ideException = null;
      
      if ((strIds == null) || (strIds.length() == 0))
      {
         ideException = OSSInvalidDataException.addException(ideException,
                           RoleDataDescriptor.COL_ROLE_ID, 
                           "You have not specified any roles to update.");
      }

      if (ideException != null)
      {               
         throw ideException;
      }

      try
      {
         lsdSecurity = m_authorityControl.getRightsForCurrentUser(
                                              m_roleDescriptor.getDataType(),
                                              ActionConstants.RIGHT_ACTION_MODIFY);
      }
      catch (RemoteException rExc)
      {
         // We cannot propagate this exception otherwise XDoclet would generate 
         // the local interface incorrectly since it would include the declared
         // RemoteException in it (to propagate we would have to declare it)
         throw new OSSInternalErrorException("Remote error occurred", rExc);
      }

      if (lsdSecurity == null)
      {
         CallContext.getInstance().getMessages().addMessage(
            Messages.ACCESSRIGHT_ERRORS, "No rights to modify roles");
      }
      else
      {
         int iAttempted = 0;
         int iActual    = 0;
         int[] arrActualIds  = null;

         // Not all of the ids may still exist or can be accessed due to 
         // security restrictions. Figure out first what ids do exists
         // and can be accesses so that we can produce nice error message
         arrActualIds = m_roleFactory.getActualIds(strIds, lsdSecurity);
         
         if ((arrActualIds != null) && (arrActualIds.length > 0))
         {
            iActual = arrActualIds.length;
            iModified = m_roleFactory.updateEnable(arrActualIds, 
                                                   newEnableValue, 
                                                   lsdSecurity);
            if (iActual != iModified)
            {
               throw new OSSInconsistentDataException(
                  "Modified roles count not equal to determined actual role count.");
            }               
         }
         iAttempted = StringUtils.count(strIds, ',') + 1;

         if (iAttempted != iModified)
         {
            if (iModified == 0)
            {
               CallContext.getInstance().getMessages().addErrorMessage(
                  " No roles have been modified either because some roles were already" +
                  " deleted or because you don't have rights to modify all the specified roles."
               );
            }
            else
            {
               CallContext.getInstance().getMessages().addErrorMessage(
                  "Only " + iModified + " role(s) have been modified either" +
                  " because some roles were already deleted or because you don't" +
                  " have rights to modify all the specified roles."
               );
            }
         }            
      }

      return iModified;
   }

   /**
    * {@inheritDoc}
    * 
    * @ejb.interface-method
    * @ejb.transaction type="Required"
    */
   public Role createSelfRegistrationRole(
      int iDomainId
   ) throws OSSException
   {
      // create default role for a specified domain initialized to 
      // self registered user parameters.
      // There will be assigned rights to view all data objects.
      Role role = null;
      List lstAccessRights = null;
      
      // get role with assigned access rights
      role = RoleUtils.getSelfregisteredRole(iDomainId);
      lstAccessRights = role.getAccessRights();
      
      // create role
      role = (Role)m_roleFactory.create(role);

      // create and assign access rights
      if ((lstAccessRights != null) && (lstAccessRights.size() > 0))
      {
         role.setAccessRights(m_accessRightFactory.create(lstAccessRights, 
                                                          role.getId()));
      }
      
      // automatically assign self registration role to domain default user
      m_roleFactory.assignToDomain(iDomainId, Integer.toString(role.getId()));

      return role;
   }

   /**
    * {@inheritDoc}
    * 
    * @ejb.interface-method
    * @ejb.transaction type="Required"
    */
   public List copyRoles(
      int    iDomainId, 
      String strRoleIds
   ) throws OSSException
   {
      List lstReturn = null;
      List lstRoles = null;

      // get list of roles that have to be copied 
      // Security check performed in the called method
      lstRoles = get(strRoleIds);

      if (lstRoles != null && lstRoles.size() > 0)
      {
         lstReturn = new ArrayList(lstRoles.size());
         
         Map hmNameID = new HashMap(lstRoles.size()); 
         Role currentRole = null;
         AccessRight currentRight = null;
         List lstRights = null;

         StringBuffer sbOldRoleIDs = new StringBuffer();
         StringBuffer sbNewRoleIDs = new StringBuffer();

         Iterator itRoles;
         Iterator itRights;

         // in the loop construct hashmap of roles
         for (itRoles = lstRoles.iterator(); itRoles.hasNext();)
         {
            // get current role object
            currentRole = (Role)itRoles.next();
            // create list of new role objects that will be created as batch. 
            // Copy attributes from old one and assignt it to the new domain
            lstReturn.add(new Role(DataObject.NEW_ID,
                                   iDomainId,
                                   currentRole.getName(),
                                   currentRole.getDescription(),
                                   currentRole.isEnabled(),
                                   currentRole.getUserId(),
                                   currentRole.isUnmodifiable(),
                                   null, null, null));
            
            // add old role ID as key and name as value to hashmap - this map 
            // will be used for assignment access rights and new created roles
            hmNameID.put(new Integer(currentRole.getId()), currentRole.getName());
            
            // create string buffer of original role IDs that will be used for
            // selecting access rights for these roles
            if (sbOldRoleIDs.length() > 0)
            {
               sbOldRoleIDs.append(",");
            }
            sbOldRoleIDs.append(currentRole.getId());
         }
         // at this moment we have created list of new roles and we will create
         // them as batch
         m_roleFactory.create(lstReturn);

         // get list of all new created roles
         lstReturn = getAllRolesInDomain(iDomainId);
         
         if (lstReturn != null && lstReturn.size() > 0)
         {
            Map hmRoleName = new HashMap(lstReturn.size());
            int iIndex;
            
            // create map role -> role name ... map of new created roles
            for (iIndex = 0; iIndex < lstReturn.size(); iIndex++)
            {
               hmRoleName.put(((Role)lstReturn.get(iIndex)).getName(), 
                              (Role)lstReturn.get(iIndex));
               // create string buffer of new role IDs that will be used for
               // assignment default roles to domain (domain-role map table)
               if (sbNewRoleIDs.length() > 0)
               {
                  sbNewRoleIDs.append(",");
               }
               sbNewRoleIDs.append(((Role)lstReturn.get(iIndex)).getId());
            }
            
            // get ordered access rights for all roles
            lstRights = m_accessRightFactory.getAllForRoles(
                             sbOldRoleIDs.toString());

            if (lstRights != null && lstRights.size() > 0)
            {
               List lstRightsForRole = new ArrayList(lstRights.size());
               String strName;
               // get all access rights belonging to the current domain 
               for (itRights = lstRights.iterator(); itRights.hasNext();)
               {
                  // get current access right object
                  currentRight = (AccessRight)itRights.next();
                  
                  // get name from hash map of old roles
                  strName = (String)hmNameID.get(new Integer(currentRight.getRoleId()));
                  // get role object from hash map of new roles
                  currentRole = (Role)hmRoleName.get(strName);
                  
                  // add new access right to the list and assign role ID
                  lstRightsForRole.add(new AccessRight(
                                              DataObject.NEW_ID,
                                              currentRole.getId(),
                                              iDomainId,
                                              currentRight.getAction(),
                                              currentRight.getDataType(),
                                              currentRight.getRightType(),
                                              currentRight.getCategory(),
                                              currentRight.getIdentifier(),
                                              null, null)
                                       );
               }
               // at this moment we have created list of new access rights 
               // and we will create them as batch
               m_accessRightFactory.create(lstRightsForRole);
            }
         }
         // assign all copied roles to the new domain (domain-role map table)
         m_roleFactory.assignToDomain(iDomainId, sbNewRoleIDs.toString());
      }
      
      return lstReturn;
   }

   /**
    * {@inheritDoc}
    * 
    * @ejb.interface-method
    * @ejb.transaction type="Supports"
    */
   public SecurityDefinition[] getSecurityDefinitions()
   {
      SecurityDefinition secDef = new SecurityDefinition(
                                         m_roleDescriptor.getDataTypeAsObject(),
                                         m_roleDescriptor.getDisplayableViewName(),
                                         ActionConstants.ACTIONS_MODIFIABLE_DATA);

      secDef.addDataCategory(RoleDataDescriptor.COL_ROLE_ENABLED, "Enabled");
      secDef.addDataCategory(RoleDataDescriptor.COL_ROLE_USER_ID, "Personal");

      secDef.addDataCategoryValueMap(RoleDataDescriptor.COL_ROLE_ENABLED,
                                     SecurityDefinition.YES_NO_CATEGORY_VALUES);
      secDef.addDataCategoryValueMap(RoleDataDescriptor.COL_ROLE_USER_ID,
                                     SecurityDefinition.YES_NO_CATEGORY_VALUES);

      return new SecurityDefinition[] {secDef};
   }

   /**
    * {@inheritDoc}
    * 
    * @ejb.interface-method
    * @ejb.transaction type="Supports"
    */
   public void constructor(
   ) throws OSSException
   {
      m_roleFactory = (RoleFactory)DataFactoryManager.getInstance(
                                      RoleFactory.class);
      m_accessRightFactory = (AccessRightFactory)DataFactoryManager.getInstance(
                                AccessRightFactory.class);
      m_authorityControl = (AuthorizationController)ControllerManager.getInstance(
                              AuthorizationController.class);
      m_roleDescriptor = (RoleDataDescriptor)DataDescriptorManager.getInstance(
                            RoleDataDescriptor.class);
   } 
   
   // Helper methods ///////////////////////////////////////////////////////////

   /**
    * Check access for given data object based on categories on which rights can
    * be enforces.
    * 
    * @param data - data object to check
    * @param action - action to check for, one of the ActionConstants.RIGHT_XXX 
    *                 constants
    * @return int - AccessRight.ACCESS_GRANTED or AccessRight.ACCESS_DENIED
    * @throws OSSException - an error has occurred
    */
   protected int checkAccess(
      Role data,
      int  action
   ) throws OSSException
   {
      try
      {
         return m_authorityControl.checkAccess(
                  m_roleDescriptor.getDataType(),
                  action,
                  data.getId(),
                  new int[][] {
                     new int[] {RoleDataDescriptor.COL_ROLE_ENABLED,
                                data.isEnabled() ? 1 : 0,
                               },
                     new int[] {RoleDataDescriptor.COL_ROLE_USER_ID,
                                // If there is user id then it is personal
                                (data.getUserId() != DataObject.NEW_ID) ? 1 : 0,
                               },
                              }
               );
      }
      catch (RemoteException rExc)
      {
         // We cannot propagate this exception otherwise XDoclet would generate 
         // the local interface incorrectly since it would include the declared
         // RemoteException in it (to propagate we would have to declare it)
         throw new OSSInternalErrorException("Remote error occurred", rExc);
      }
   }

   /**
    * Validate data in the specified object.
    * 
    * @param role - data to validate
    * @throws OSSInvalidDataException - the passed data were not valid
    */
   protected void validate(
      Role role
   ) throws OSSInvalidDataException
   {
      if (role.getUserId() == DataObject.NEW_ID)
      {
         // only if role is not personal
         OSSInvalidDataException ideException = null;
         if ((role.getName() == null) || (role.getName().trim().length() == 0))
         {
            if (ideException == null)
            {
               ideException = new OSSInvalidDataException();
            }
            ideException.getErrorMessages().addMessage(
               RoleDataDescriptor.COL_ROLE_NAME,
               "Name cannot be empty." 
            );
         }
         if (role.getName().length() > m_roleDescriptor.getNameMaxLength())
         {
            if (ideException == null)
            {
               ideException = new OSSInvalidDataException();
            }
            ideException.getErrorMessages().addMessage(
               RoleDataDescriptor.COL_ROLE_NAME,
               "Name should not be longer then " 
               + m_roleDescriptor.getNameMaxLength() 
               + " characters."
            );
         }
         if (role.getDescription().length() > m_roleDescriptor.getDescriptionMaxLength())
         {
            if (ideException == null)
            {
               ideException = new OSSInvalidDataException();
            }
            ideException.getErrorMessages().addMessage(
               RoleDataDescriptor.COL_ROLE_DESCRIPTION, 
               "Description should not be longer then " 
               + m_roleDescriptor.getDescriptionMaxLength() 
               + " characters."
            );
         }      
         if (ideException != null)
         {               
            throw ideException;
         }     
      }
   }
}
