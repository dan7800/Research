/*
 * Copyright (c) 2003 - 2008 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: UserControllerImpl.java,v 1.51 2009/04/22 06:29:13 bastafidli Exp $
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
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.opensubsystems.core.util.Messages;
import org.opensubsystems.core.util.StringUtils;
import org.opensubsystems.patterns.listdata.data.SimpleRule;
import org.opensubsystems.security.data.AccessRight;
import org.opensubsystems.security.data.Domain;
import org.opensubsystems.security.data.SecurityDefinition;
import org.opensubsystems.security.data.User;
import org.opensubsystems.security.data.UserDataDescriptor;
import org.opensubsystems.security.logic.AuthorizationController;
import org.opensubsystems.security.logic.DomainController;
import org.opensubsystems.security.logic.RoleController;
import org.opensubsystems.security.logic.UserController;
import org.opensubsystems.security.persist.DomainFactory;
import org.opensubsystems.security.persist.RoleFactory;
import org.opensubsystems.security.persist.UserFactory;
import org.opensubsystems.security.util.ActionConstants;

/**
 * Implementation of UserController interface to manage users.
 *
 * View-type has to be set to local due to bug XDT-867 affecting WebSphere
 * Refs has to be set to local JNDI name since we do not want to use remote objects.
 * 
 * @ejb.bean type="Stateless"
 *           name="UserController"
 *           view-type="local" 
 *           jndi-name="org.opensubsystems.security.logic.UserControllerRemote"
 *           local-jndi-name="org.opensubsystems.security.logic.UserController"
 * @ejb.interface 
 *     local-extends="javax.ejb.EJBLocalObject, org.opensubsystems.security.logic.UserController"
 *     extends="javax.ejb.EJBObject, org.opensubsystems.security.logic.UserController"
 *
 * @ejb.ejb-ref ejb-name="AuthorizationController"
 *              ref-name="org.opensubsystems.security.logic.AuthorizationController"
 * @ejb.ejb-ref ejb-name="DomainController"
 *              ref-name="org.opensubsystems.security.logic.DomainController"
 * @ejb.ejb-ref ejb-name="RoleController"
 *              ref-name="org.opensubsystems.security.logic.RoleController"
 * 
 * @jonas.bean ejb-name="UserController"
 *             jndi-name="org.opensubsystems.security.logic.UserControllerRemote"
 *
 * @jonas.ejb-ref ejb-ref-name="org.opensubsystems.security.logic.AuthorizationController"
 *                jndi-name="org.opensubsystems.security.logic.AuthorizationControllerRemote"
 * @jonas.ejb-ref ejb-ref-name="org.opensubsystems.security.logic.DomainController"
 *                jndi-name="org.opensubsystems.security.logic.DomainControllerRemote"
 * @jonas.ejb-ref ejb-ref-name="org.opensubsystems.security.logic.RoleController"
 *                jndi-name="org.opensubsystems.security.logic.RoleControllerRemote"
 * 
 * @jboss.ejb-ref-jndi ref-name="org.opensubsystems.security.logic.AuthorizationController"
 *                     jndi-name="org.opensubsystems.security.logic.AuthorizationController"
 * @jboss.ejb-local-ref ref-name="org.opensubsystems.security.logic.AuthorizationController"
 *                     jndi-name="org.opensubsystems.security.logic.AuthorizationController"
 * @jboss.ejb-ref-jndi ref-name="org.opensubsystems.security.logic.DomainController"
 *                     jndi-name="org.opensubsystems.security.logic.DomainController"
 * @jboss.ejb-local-ref ref-name="org.opensubsystems.security.logic.DomainController"
 *                     jndi-name="org.opensubsystems.security.logic.DomainController"
 * @jboss.ejb-ref-jndi ref-name="org.opensubsystems.security.logic.RoleController"
 *                     jndi-name="org.opensubsystems.security.logic.RoleController"
 * @jboss.ejb-local-ref ref-name="org.opensubsystems.security.logic.RoleController"
 *                     jndi-name="org.opensubsystems.security.logic.RoleController"
 *
 * @weblogic.ejb-reference-description 
 *               ejb-ref-name="org.opensubsystems.security.logic.AuthorizationController"
 *               jndi-name="org.opensubsystems.security.logic.AuthorizationController"
 * @weblogic.ejb-local-reference-description 
 *               ejb-ref-name="org.opensubsystems.security.logic.AuthorizationController"
 *               jndi-name="org.opensubsystems.security.logic.AuthorizationController"
 * @weblogic.ejb-reference-description 
 *               ejb-ref-name="org.opensubsystems.security.logic.DomainController"
 *               jndi-name="org.opensubsystems.security.logic.DomainController"
 * @weblogic.ejb-local-reference-description 
 *               ejb-ref-name="org.opensubsystems.security.logic.DomainController"
 *               jndi-name="org.opensubsystems.security.logic.DomainController"
 * @weblogic.ejb-reference-description 
 *               ejb-ref-name="org.opensubsystems.security.logic.RoleController"
 *               jndi-name="org.opensubsystems.security.logic.RoleController"
 * @weblogic.ejb-local-reference-description 
 *               ejb-ref-name="org.opensubsystems.security.logic.RoleController"
 *               jndi-name="org.opensubsystems.security.logic.RoleController"
 *
 * @version $Id: UserControllerImpl.java,v 1.51 2009/04/22 06:29:13 bastafidli Exp $
 * @author Julo Legeny
 * @code.reviewer Miro Halas
 * @code.reviewed 1.26 2006/02/09 23:56:32 jlegeny
 */
public class UserControllerImpl extends    StatelessControllerImpl 
                                implements UserController
{
   // Cached values ////////////////////////////////////////////////////////////   
   
   /**
    * Factory to use to execute persistence operations.
    */
   protected DomainFactory m_domainFactory;

   /**
    * Factory to use to execute persistence operations.
    */
   protected UserFactory m_userFactory;

   /**
    * Factory to use to execute persistence operations.
    */
   protected RoleFactory m_roleFactory;

   /**
    * Authorization controller used to check access rights.
    */
   protected AuthorizationController m_authorityControl;

   /**
    * Domain controller used to manipulate domains.
    */
   protected DomainController m_domainControl;

   /**
    * Role controller used to manipulate roles.
    */
   protected RoleController m_roleControl;

   /**
    * Data descriptor for the user data types.
    */
   protected UserDataDescriptor m_userDescriptor;

   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Generated serial version id for this class.
    */
   private static final long serialVersionUID = 8667910163224155312L;

   // Constructors /////////////////////////////////////////////////////////////

   /**
    * Default constructor.
    */
   public UserControllerImpl(
   ) 
   {
      super();
      
      // Do not cache anything here since if this controller is run as a stateless
      // session bean the referenced objects may not be ready
      m_userFactory = null;
      m_roleFactory = null;
      m_authorityControl = null;
      m_domainControl = null;
      m_roleControl = null;
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
      User   data   = null;

      // We need to get the data before we can check if the user has access
      // granted based on the attributes of the data
      data = (User)m_userFactory.get(
                      iId, CallContext.getInstance().getCurrentDomainId());
      if (data != null)
      {
         if (checkAccess(data, ActionConstants.RIGHT_ACTION_VIEW) 
            != AccessRight.ACCESS_GRANTED)
         {
            data = null;
            CallContext.getInstance().getMessages().addMessage(
               Messages.ACCESSRIGHT_ERRORS, "No rights to view user.");
         }
      }

      return data;
   }
   
   /**
    * {@inheritDoc}
    * 
    * @ejb.interface-method
    * @ejb.transaction type="Supports"
    */
   public User getDefaultUserForDomain(
      Domain domain
   ) throws OSSException
   {
      User data = null;

      // this method doesn't check access rights because it is used
      // getting default user related to selfregistration domain
      data = (User)m_userFactory.get(
                      DataObject.NEW_ID, 
                      CallContext.getInstance().getCurrentDomainId());
      // set up default values from domain
      data.setPhone(domain.getDefaultPhone());
      data.setAddress(domain.getDefaultAddress());
      data.setInternalUser(domain.isDefaultInternalUser());
      data.setLoginEnabled(domain.isDefaultLoginEnabled());
      data.setSuperUser(domain.isDefaultSuperUser());

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
      Domain domain = null;
      User newdata = (User)data;
      User createddata = null;

      // Validate data before we hit the database to improve performance
      validate(newdata);

      if (CallContext.getInstance().getCurrentUser() == null)
      {
         // load domain data the user should be created for
         domain = (Domain)m_domainFactory.get(newdata.getDomainId(), 
                                              newdata.getDomainId());
         if (domain != null && domain.isAllowSelfRegistration())
         {
            // set up default values from retrieved domain object
            newdata.setLoginEnabled(domain.isDefaultLoginEnabled());
            newdata.setInternalUser(domain.isDefaultInternalUser());
            newdata.setSuperUser(domain.isDefaultSuperUser());
            // don't check access rights if there is allowed self registration 
            // domain and if user is currently not logged in
            createddata = (User)m_userFactory.create(newdata);
            // Create and assign personal role, this goes directly against
            // factory since personal roles are more part of user data than
            // roles on its own
            m_roleFactory.createPersonal(createddata);
         }
         else
         {
            CallContext.getInstance().getMessages().addMessage(
                     Messages.ACCESSRIGHT_ERRORS, "No rights to create user.");
         }
      }
      else
      {
         if ((checkAccess(newdata, ActionConstants.RIGHT_ACTION_CREATE) 
                  == AccessRight.ACCESS_GRANTED))
         {
            createddata = (User)m_userFactory.create(newdata);
            // Create and assign personal role, this goes directly against
            // factory since personal roles are more part of user data than
            // roles on its own
            m_roleFactory.createPersonal(createddata);
         }
         else
         {
            CallContext.getInstance().getMessages().addMessage(
               Messages.ACCESSRIGHT_ERRORS, "No rights to create user.");
         }
      }

      return createddata;
   }

   /**
    * {@inheritDoc}
    *    
    * @ejb.interface-method
    * @ejb.transaction type="Required"
    */
   public Object[] create(
      Domain dDomain,
      User   uUser
   ) throws OSSException
   {
      // Create new domain and particular user account
      Domain domain = null;
      User user = null;
      
      try
      {
         // first create domain
         domain = (Domain)m_domainControl.create(dDomain);
      }
      catch (RemoteException rExc)
      {
         // We cannot propagate this exception otherwise XDoclet would generate 
         // the local interface incorrectly since it would include the declared
         // RemoteException in it (to propagate we would have to declare it)
         throw new OSSInternalErrorException("Remote error occurred", rExc);
      }

      if (domain != null)
      {
         user = (User)m_userFactory.create(new User(domain.getId(), uUser));
         // Create and assign personal role, this goes directly against
         // factory since personal roles are more part of user data than
         // roles on its own
         m_roleFactory.createPersonal(user);

         // create default role for selfregistered user and assign it to the user
         try
         {
            m_roleControl.createSelfRegistrationRole(domain.getId());
         }
         catch (RemoteException rExc)
         {
            // We cannot propagate this exception otherwise XDoclet would generate 
            // the local interface incorrectly since it would include the declared
            // RemoteException in it (to propagate we would have to declare it)
            throw new OSSInternalErrorException("Remote error occurred", rExc);
         }
      }
      
      return new Object[] {domain, user};
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
      User newdata = (User)data;
      User olddata = null;
      User saveddata = null;
      
      // Validate data before we hit the database to improve performance
      validate(newdata);
      // Since we are modifying and the security is based on the categories, 
      // we need check that we have rights to modify the data in the old 
      // categories as well as the data in the new categories (if they were changed)
      olddata = (User)m_userFactory.get(
                         data.getId(), 
                         CallContext.getInstance().getCurrentDomainId());
      if (olddata != null)
      {
         if ((checkAccess(olddata, ActionConstants.RIGHT_ACTION_MODIFY) 
            == AccessRight.ACCESS_GRANTED)
            && (checkAccess(newdata, ActionConstants.RIGHT_ACTION_MODIFY) 
             == AccessRight.ACCESS_GRANTED))
         {
            // Save user
            saveddata = (User)m_userFactory.save(newdata);
   
            // And update pesonal role, this goes directly against
            // factory since personal roles are more part of user data than
            // roles on its own
            m_roleFactory.savePersonal(saveddata);
         }
         else
         {
            saveddata = null;      
            CallContext.getInstance().getMessages().addMessage(
               Messages.ACCESSRIGHT_ERRORS, "No rights to modify user.");
   
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
   public User get(
      String strLoginName
   ) throws OSSException
   {
      User data = null;

      // We need to get the data before we can check if the user has access
      // granted based on the attributes of the data
      data = (User)m_userFactory.getByLoginName(strLoginName);
      if (data != null)
      {
         if (checkAccess(data, ActionConstants.RIGHT_ACTION_VIEW) 
            != AccessRight.ACCESS_GRANTED)
         {
            data = null;
            CallContext.getInstance().getMessages().addMessage(
               Messages.ACCESSRIGHT_ERRORS, "No rights to view user.");
         }
      }

      return data;
   }
   
   /**
    * {@inheritDoc}
    * 
    * @ejb.interface-method
    * @ejb.transaction type="Supports"
    */
   public Object[] getUserWithRoles(
      int iUserId
   ) throws OSSException
   {
      User   data = null;
      List   lstUserRoles = null;

      // Security check performed in the called method
      data = (User)get(iUserId);
      if (iUserId == DataObject.NEW_ID)
      {
         // there is requested new user data - set up default user information
         // and default user roles (retrieved from current domain)
         Domain domain = null;
         try
         {
            domain = m_domainControl.getCurrentDomainWithRoles();  
         }
         catch (RemoteException rExc)
         {
            // We cannot propagate this exception otherwise XDoclet would generate 
            // the local interface incorrectly since it would include the declared
            // RemoteException in it (to propagate we would have to declare it)
            throw new OSSInternalErrorException("Remote error occurred", rExc);
         }
         
         // set up default user information
         data.setLoginEnabled(domain.isDefaultLoginEnabled());
         data.setSuperUser(domain.isDefaultSuperUser());
         data.setInternalUser(domain.isDefaultInternalUser());
         data.setAddress(domain.getDefaultAddress());
         data.setPhone(domain.getDefaultPhone());
         // set up default roles
         lstUserRoles = domain.getDefaultRoles();
      }
      else
      {
         if (data != null)
         {
            // Security check performed in the called method
            try
            {
               lstUserRoles = m_roleControl.getAllForUser(data.getId());
            }
            catch (RemoteException rExc)
            {
               // We cannot propagate this exception otherwise XDoclet would generate 
               // the local interface incorrectly since it would include the declared
               // RemoteException in it (to propagate we would have to declare it)
               throw new OSSInternalErrorException("Remote error occurred", rExc);
            }
         }
      }

      return new Object[] {data, lstUserRoles};
   }

   /**
    * {@inheritDoc}
    * 
    * @ejb.interface-method
    * @ejb.transaction type="Supports"
    */
   public List getRoles(
      int iUserId
   ) throws OSSException
   {
      // Convenience method, security check performed in the called method
      try
      {
         return m_roleControl.getAllForUser(iUserId);
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
    * {@inheritDoc}
    * 
    * @ejb.interface-method
    * @ejb.transaction type="Required"
    */
   public User create(
      User   data,
      String strRoleIds
   ) throws OSSException
   {
      User newdata;

      // Security check performed in the called method
      newdata = (User)create(data);
      // assign new roles
      if ((newdata != null) && (strRoleIds != null) && (strRoleIds.length() > 0))
      {
         // Here we are not modifying nor accessing the roles so no security check
         // beyond the one already done on user is necessary
         m_roleFactory.assignToUser(newdata.getId(), strRoleIds);
      }

      return newdata;
   }

   /**
    * {@inheritDoc}
    * 
    * @ejb.interface-method
    * @ejb.transaction type="Required"
    */
   public User save(
      User   data,
      String strRemoveRoleIds,
      String strAddRoleIds
   ) throws OSSException
   {
      User saveddata = null;

      // Validate data before we hit the database to improve performance
      validateUserRolesAssignment(strRemoveRoleIds, strAddRoleIds);
      // Security check performed in the called method
      saveddata = (User)save(data);
      if (saveddata != null)
      {
         // remove roles from user
         if (strRemoveRoleIds.length() > 0)
         {
            // Here we are not modifying nor accessing the roles so no security 
            // check beyond the one already done on user is not necessary
            m_roleFactory.removeFromUser(data.getId(), strRemoveRoleIds);
         }
         
         // assign new roles
         if (strAddRoleIds.length() > 0)
         {
            // Here we are not modifying nor accessing the roles so no security 
            // check beyond the one already done on user is necessary
            m_roleFactory.assignToUser(data.getId(), strAddRoleIds);
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
         ideException = OSSInvalidDataException.addException(
                           ideException,
                           UserDataDescriptor.COL_USER_ID, 
                           "You have not specified any users to delete.");
      }

      if (ideException != null)
      {               
         throw ideException;
      }

      try
      {
         lsdSecurity = m_authorityControl.getRightsForCurrentUser(
                                           m_userDescriptor.getDataType(),
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
            Messages.ACCESSRIGHT_ERRORS, "No rights to delete users");
      }
      else
      {
         int iAttempted = 0;
         int iActual    = 0;
         int iRolesDeleted   = 0;
         int[] arrActualIds  = null;

         // Not all of the ids may still exist or can be accessed due to 
         // security restrictions. Figure out first what ids do exists
         // and can be accesses so that we can produce nice error message
         arrActualIds = m_userFactory.getActualIds(strIds, lsdSecurity);
         
         if ((arrActualIds != null) && (arrActualIds.length > 0))
         {
            iActual = arrActualIds.length;
            
            // No security check here since this can be invoked only from this 
            // controller once users were checked
            // Delete personal roles first in case the database doesn't support
            // cascading deletes
            iRolesDeleted = m_roleFactory.deletePersonal(arrActualIds);

            iDeleted = m_userFactory.delete(arrActualIds, lsdSecurity);
            if (iActual != iDeleted)
            {
               // This will prevent us from deleting personal roles for users
               // which were not deleted
               throw new OSSInconsistentDataException(
                  "Deleted user count not equal to determined actual user count.");
            }               
            if (iRolesDeleted != iDeleted)
            {
               // By throwing exception this will rollback the transaction
               // so no harm will be done
               throw new OSSInconsistentDataException(
                  "Deleted personal role count is not equal to deleted user count: " +
                  " deleted users " + iDeleted + " vs deleted roles " + iRolesDeleted);
            }
         }
         iAttempted = StringUtils.count(strIds, ',') + 1;

         if (iAttempted != iDeleted)
         {
            if (iDeleted == 0)
            {
               CallContext.getInstance().getMessages().addErrorMessage(
                  " No user have been deleted either because some users were already" +
                  " deleted or because you don't have rights to delete all the specified users."
               );
            }
            else
            {
               CallContext.getInstance().getMessages().addErrorMessage(
                  "Only " + iDeleted + " user(s) have been deleted either" +
                  " because some users were already deleted or because you don't" +
                  " have rights to delete all the specified users."
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
         ideException = OSSInvalidDataException.addException(
                           ideException, UserDataDescriptor.COL_USER_ID, 
                           "You have not specified any users to update.");
      }

      if (ideException != null)
      {               
         throw ideException;
      }

      try
      {
         lsdSecurity = m_authorityControl.getRightsForCurrentUser(
                                              m_userDescriptor.getDataType(),
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
            Messages.ACCESSRIGHT_ERRORS, "No rights to modify users");
      }
      else
      {
         int iAttempted = 0;
         int iActual    = 0;
         int[] arrActualIds  = null;

         // Not all of the ids may still exist or can be accessed due to 
         // security restrictions. Figure out first what ids do exists
         // and can be accesses so that we can produce nice error message
         arrActualIds = m_userFactory.getActualIds(strIds, lsdSecurity);
         
         if ((arrActualIds != null) && (arrActualIds.length > 0))
         {
            // If users have to be disabled, check if there will remain 
            // enabled at least 1 superuser within the database after disabling
            if ((newEnableValue) || ((!newEnableValue) 
                && (m_userFactory.getCountOfNotContainedSuperUsers(
                       StringUtils.parseIntArrayToString(arrActualIds, ",")) > 0)))
            {
               iActual = arrActualIds.length;
               iModified = m_userFactory.updateEnable(arrActualIds, 
                                                      newEnableValue, 
                                                      lsdSecurity);
               if (iActual != iModified)
               {
                  throw new OSSInconsistentDataException(
                     "Modified user count not equal to determined actual user count.");
               }               
            }
            else
            {
               // In this case no superuser would remain enabled within the DB 
               // after disabling. Set iModified to -1 and we will add specific
               // error message for this specific case.
               iModified = -1;
            }
         }
         iAttempted = StringUtils.count(strIds, ',') + 1;

         if (iAttempted != iModified)
         {
            if (iModified == -1)
            {
               CallContext.getInstance().getMessages().addErrorMessage(
                  " No user has been modified since no active superuser would" +
                  " remain after modification."
               );
            }
            else if (iModified == 0)
            {
               CallContext.getInstance().getMessages().addErrorMessage(
                  " No user have been modified either because some users were already" +
                  " deleted or because you don't have rights to modify all the specified users."
               );
            }
            else
            {
               CallContext.getInstance().getMessages().addErrorMessage(
                  "Only " + iModified + " user(s) have been modified either" +
                  " because some users were already deleted or because you don't" +
                  " have rights to modify all the specified users."
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
   public void changePassword(
      String strLoginName,
      String strCurrentPassword,
      String strNewPassword
   ) throws OSSException
   {
      User data;
      
      data = m_userFactory.getByLoginName(strLoginName);
      if (data != null)
      {
         // We cannot check if user has right to change password since each user 
         // has right to change it's own password and change password can be done
         // without user being logged in 
         // if (checkAccess(data, RIGHT_ACTION_CHANGE_PASSWORD) 
         //   == AccessRight.ACCESS_GRANTED)
         String strNewEncryptedPassword = User.encryptPassword(strCurrentPassword);
         if (data.getPassword().equals(strNewEncryptedPassword))
         {
            data.setPassword(strNewPassword);
            m_userFactory.save(data);
         }
         else
         {
            throw new OSSInvalidDataException("The password you typed is incorrect." +
                                              " Please retype your current password.");
         }
      }
      else
      {
         throw new OSSInvalidDataException("User \"" + strLoginName + "\" doesn't exist.");
      }
   }

   /**
    * {@inheritDoc}
    * 
    * @ejb.interface-method
    * @ejb.transaction type="Required"
    */
   public User changeLoginNameAndEmail(
      int       iUserId,
      Timestamp tsLastModified,
      String    strLoginName,
      String    strEmail
   ) throws OSSException
   {
      User saveddata = null;
      User olddata;
      
      olddata = (User)m_userFactory.get(
                         iUserId, 
                         CallContext.getInstance().getCurrentDomainId());
      if (olddata != null)
      {
         // Modify the login name and email, since none of these is category
         // we can modify them without affecting access rights checking
         olddata.setLoginName(strLoginName);
         olddata.setEmail(strEmail);
         // Set the original modification time when user loaded the data
         olddata.setModificationTimestamp(tsLastModified);
         validateLoginNameAndEmail(olddata, null);
         
         if (checkAccess(olddata, ActionConstants.RIGHT_ACTION_MODIFY) 
            == AccessRight.ACCESS_GRANTED)
         {
            // Now save it         
            saveddata = (User)m_userFactory.save(olddata);
            // And update pesonal role, this goes directly against
            // factory since personal roles are more part of user data than
            // roles on its own
            m_roleFactory.savePersonal(saveddata);
         }
         else
         {
            CallContext.getInstance().getMessages().addMessage(
               Messages.ACCESSRIGHT_ERRORS, "No rights to modify User.");
   
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
   public boolean checkForInternalEnabledUsers(
      int      iDomain,
      String[] ignoreLoginNames
   ) throws OSSException
   {
      // No access check since this is used to check if there is at least
      // one user which can login
      return m_userFactory.checkForInternalEnabled(iDomain, ignoreLoginNames);
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
         m_userDescriptor.getDataTypeAsObject(),
         m_userDescriptor.getDisplayableViewName(),
         ActionConstants.ACTIONS_MODIFIABLE_DATA);

      secDef.addDataCategory(UserDataDescriptor.COL_USER_SUPER_USER, "Super user");
      secDef.addDataCategory(UserDataDescriptor.COL_USER_GUEST_ACCESS_ENABLED, "Guest");
      secDef.addDataCategory(UserDataDescriptor.COL_USER_LOGIN_ENABLED, "Login enabled");
      secDef.addDataCategory(UserDataDescriptor.COL_USER_INTERNAL_USER, "Internal user");

      secDef.addDataCategoryValueMap(UserDataDescriptor.COL_USER_SUPER_USER,
                                     SecurityDefinition.YES_NO_CATEGORY_VALUES);
      secDef.addDataCategoryValueMap(UserDataDescriptor.COL_USER_GUEST_ACCESS_ENABLED,
                                     SecurityDefinition.YES_NO_CATEGORY_VALUES);
      secDef.addDataCategoryValueMap(UserDataDescriptor.COL_USER_LOGIN_ENABLED,
                                     SecurityDefinition.YES_NO_CATEGORY_VALUES);
      secDef.addDataCategoryValueMap(UserDataDescriptor.COL_USER_INTERNAL_USER,
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
      m_domainFactory = (DomainFactory)DataFactoryManager.getInstance(
                                      DomainFactory.class);
      m_userFactory = (UserFactory)DataFactoryManager.getInstance(
                                      UserFactory.class);
      m_roleFactory = (RoleFactory)DataFactoryManager.getInstance(
                                      RoleFactory.class);
      m_authorityControl = (AuthorizationController)ControllerManager.getInstance(
                                      AuthorizationController.class);
      m_domainControl = (DomainController)ControllerManager.getInstance(
                                      DomainController.class);
      m_roleControl = (RoleController)ControllerManager.getInstance(
                                      RoleController.class);
      m_userDescriptor = (UserDataDescriptor)DataDescriptorManager.getInstance(
                                                UserDataDescriptor.class);
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
      User data,
      int  action
   ) throws OSSException
   {
      try
      {
         return m_authorityControl.checkAccess(
                  m_userDescriptor.getDataType(),
                  action,
                  data.getId(),
                  new int[][] {
                     new int[] {UserDataDescriptor.COL_USER_SUPER_USER,
                                data.isSuperUser() ? 1 : 0,
                                },
                     new int[] {UserDataDescriptor.COL_USER_GUEST_ACCESS_ENABLED,
                                data.isGuestAccessEnabled() ? 1 : 0,
                               },
                     new int[] {UserDataDescriptor.COL_USER_LOGIN_ENABLED,
                                data.isLoginEnabled() ? 1 : 0,
                               },
                     new int[] {UserDataDescriptor.COL_USER_INTERNAL_USER,
                                data.isInternalUser() ? 1 : 0,
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
    * @param data - data to validate
    * @throws OSSInvalidDataException - the passed data were not valid
    */
   protected void validate(
      User data
   ) throws OSSInvalidDataException
   {
      OSSInvalidDataException ideException = null;
            
      if (data.getFirstName().length() > m_userDescriptor.getFirstNameMaxLength())
      {
         if (ideException == null)
         {
            ideException = new OSSInvalidDataException();
         }
         ideException.getErrorMessages().addMessage(
            UserDataDescriptor.COL_USER_FIRST_NAME, 
            "First name should not be longer then " 
            + m_userDescriptor.getFirstNameMaxLength() 
            + " characters."
         );
      }
      if (data.getLastName().length() > m_userDescriptor.getLastNameMaxLength())
      {
         if (ideException == null)
         {
            ideException = new OSSInvalidDataException();
         }
         ideException.getErrorMessages().addMessage(
            UserDataDescriptor.COL_USER_LAST_NAME, 
            "Last name should not be longer then " 
            + m_userDescriptor.getLastNameMaxLength() 
            + " characters."
         );
      }
      if (data.getPhone().length() > m_userDescriptor.getPhoneMaxLength())
      {
         if (ideException == null)
         {
            ideException = new OSSInvalidDataException();
         }
         ideException.getErrorMessages().addMessage(
            UserDataDescriptor.COL_USER_PHONE, 
            "Phone number should not be longer then " 
            + m_userDescriptor.getPhoneMaxLength() 
            + " characters."
         );
      }
      if (data.getFax().length() > m_userDescriptor.getFaxMaxLength())
      {
         if (ideException == null)
         {
            ideException = new OSSInvalidDataException();
         }
         ideException.getErrorMessages().addMessage(
            UserDataDescriptor.COL_USER_FAX, 
            "Fax number should not be longer then " 
            + m_userDescriptor.getFaxMaxLength() 
            + " characters."
         );
      }
      if (data.getAddress().length() > m_userDescriptor.getAddressMaxLength())
      {
         if (ideException == null)
         {
            ideException = new OSSInvalidDataException();
         }
         ideException.getErrorMessages().addMessage(
            UserDataDescriptor.COL_USER_ADDRESS, 
            "Address should not be longer then " 
            + m_userDescriptor.getAddressMaxLength() 
            + " characters."
         );
      }
      // User data object contains already encrypted password and it's 
      // length does not correspond with the password the user was entered.
      // Password string is encoded by some message digest algorithm and there is 
      // returned XX characters string and after that each character is converted 
      // into the hexa-decimal number. Final password length should have at most
      // approximately 3*XX characters. Therefore we need to check encrypted password 
      // length for number at most 3 * passwordMaxLength. 
      if (data.getPassword().length() > 3 * m_userDescriptor.getPasswordMaxLength())
      {
         if (ideException == null)
         {
            ideException = new OSSInvalidDataException();
         }
         ideException.getErrorMessages().addMessage(
            UserDataDescriptor.COL_USER_PASSWORD, 
            "Password should not be longer then " 
            + m_userDescriptor.getPasswordMaxLength() 
            + " characters."
         );
      }    
      validateLoginNameAndEmail(data, ideException);
      if (ideException != null)
      {               
         throw ideException;
      }
   }

   /**
    * Validate login name and email of user
    * 
    * @param data - data to validate 
    * @param ideException - existing exception or null
    */
   protected void validateLoginNameAndEmail(
      User                    data,
      OSSInvalidDataException ideException
   )
   {
      if (data.getEmail().length() > m_userDescriptor.getEmailMaxLength())
      {
         if (ideException == null)
         {
            ideException = new OSSInvalidDataException();
         }
         ideException.getErrorMessages().addMessage(
            UserDataDescriptor.COL_USER_EMAIL, 
            "Email should not be longer then " 
            + m_userDescriptor.getEmailMaxLength() 
            + " characters."
         );
      }      
      if (data.getLoginName().length() > m_userDescriptor.getLoginNameMaxLength())
      {
         if (ideException == null)
         {
            ideException = new OSSInvalidDataException();
         }
         ideException.getErrorMessages().addMessage(
            UserDataDescriptor.COL_USER_LOGIN_NAME, 
            "Login name should not be longer then " 
            + m_userDescriptor.getLoginNameMaxLength() 
            + " characters."
         );
      }      
   }

   /**
    * Check if personal roles were not removed nor added  
    * 
    * @param strRemoveRoleIds - String of role IDs that should be removed
    * @param strAddRoleIds - String of role IDs that should be added
    * @throws OSSException - error during check
    */
   protected void validateUserRolesAssignment(
      String strRemoveRoleIds, 
      String strAddRoleIds
   ) throws OSSException
   {
      Set uniqueSetOfIds = new HashSet();
      StringUtils.parseStringToCollection(strRemoveRoleIds, ",", false, 
                                          StringUtils.CASE_ORIGINAL, 
                                          uniqueSetOfIds);
      StringUtils.parseStringToCollection(strAddRoleIds, ",", false, 
                                          StringUtils.CASE_ORIGINAL, 
                                          uniqueSetOfIds);
      
      if ((!uniqueSetOfIds.isEmpty()) 
         && (m_roleFactory.isAnyOfSpecifiedPersonal(uniqueSetOfIds)))
      {
         throw new OSSInvalidDataException(
                      "Personal user role cannot be assigned or removed.");
      }
   }
}
