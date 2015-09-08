/*
 * Copyright (c) 2003 - 2008 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: DomainControllerImpl.java,v 1.58 2009/04/22 06:29:13 bastafidli Exp $
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
import java.util.HashSet;
import java.util.Iterator;
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
import org.opensubsystems.security.data.DomainDataDescriptor;
import org.opensubsystems.security.data.Role;
import org.opensubsystems.security.data.SecurityDefinition;
import org.opensubsystems.security.logic.AuthorizationController;
import org.opensubsystems.security.logic.DomainController;
import org.opensubsystems.security.logic.RoleController;
import org.opensubsystems.security.logic.SessionController;
import org.opensubsystems.security.persist.DomainFactory;
import org.opensubsystems.security.persist.RoleFactory;
import org.opensubsystems.security.util.ActionConstants;
import org.opensubsystems.security.util.SecurityUtils;

/**
 * Implementation of DomainController interface to manage domains.
 *
 * View-type has to be set to local due to bug XDT-867 affecting WebSphere
 * Refs has to be set to local JNDI name since we do not want to use remote objects.
 * 
 * @ejb.bean type="Stateless"
 *           name="DomainController"
 *           view-type="local" 
 *           jndi-name="org.opensubsystems.security.logic.DomainControllerRemote"
 *           local-jndi-name="org.opensubsystems.security.logic.DomainController"
 * @ejb.interface 
 *     local-extends="javax.ejb.EJBLocalObject, org.opensubsystems.security.logic.DomainController"
 *     extends="javax.ejb.EJBObject, org.opensubsystems.security.logic.DomainController"
 * 
 * @ejb.ejb-ref ejb-name="AuthorizationController"
 *              ref-name="org.opensubsystems.security.logic.AuthorizationController"
 * @ejb.ejb-ref ejb-name="SessionController"
 *              ref-name="org.opensubsystems.security.logic.SessionController"
 * @ejb.ejb-ref ejb-name="RoleController"
 *              ref-name="org.opensubsystems.security.logic.RoleController"
 * 
 * @jonas.bean ejb-name="DomainController"
 *             jndi-name="org.opensubsystems.security.logic.DomainControllerRemote"
 * 
 * @jonas.ejb-ref ejb-ref-name="org.opensubsystems.security.logic.AuthorizationController"
 *                jndi-name="org.opensubsystems.security.logic.AuthorizationControllerRemote"
 * @jonas.ejb-ref ejb-ref-name="org.opensubsystems.security.logic.SessionController"
 *                jndi-name="org.opensubsystems.security.logic.SessionControllerRemote"
 * @jonas.ejb-ref ejb-ref-name="org.opensubsystems.security.logic.RoleController"
 *                jndi-name="org.opensubsystems.security.logic.RoleControllerRemote"
 *  
 * @jboss.ejb-ref-jndi ref-name="org.opensubsystems.security.logic.AuthorizationController"
 *                     jndi-name="org.opensubsystems.security.logic.AuthorizationController"
 * @jboss.ejb-local-ref ref-name="org.opensubsystems.security.logic.AuthorizationController"
 *                     jndi-name="org.opensubsystems.security.logic.AuthorizationController"
 * @jboss.ejb-ref-jndi ref-name="org.opensubsystems.security.logic.SessionController"
 *                     jndi-name="org.opensubsystems.security.logic.SessionController"
 * @jboss.ejb-local-ref ref-name="org.opensubsystems.security.logic.SessionController"
 *                     jndi-name="org.opensubsystems.security.logic.SessionController"
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
 *               ejb-ref-name="org.opensubsystems.security.logic.SessionController"
 *               jndi-name="org.opensubsystems.security.logic.SessionController"
 * @weblogic.ejb-local-reference-description 
 *               ejb-ref-name="org.opensubsystems.security.logic.SessionController"
 *               jndi-name="org.opensubsystems.security.logic.SessionController"
 * @weblogic.ejb-reference-description 
 *               ejb-ref-name="org.opensubsystems.security.logic.RoleController"
 *               jndi-name="org.opensubsystems.security.logic.RoleController"
 * @weblogic.ejb-local-reference-description 
 *               ejb-ref-name="org.opensubsystems.security.logic.RoleController"
 *               jndi-name="org.opensubsystems.security.logic.RoleController"
 *
 * @version $Id: DomainControllerImpl.java,v 1.58 2009/04/22 06:29:13 bastafidli Exp $
 * @author Julo Legeny
 * @code.reviewer Miro Halas
 * @code.reviewed Initial revision
 */
public class DomainControllerImpl extends    StatelessControllerImpl 
                                  implements DomainController
{
   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Factory to use to execute persistence operations.
    */
   protected DomainFactory m_domainFactory;

   /**
    * Factory to use to execute persistence operations.
    */
   protected RoleFactory m_roleFactory;

   /**
    * Role controller used to manipulate roles.
    */
   protected RoleController m_roleControl;

   /**
    * Authorization controller used to check access rights.
    */
   protected AuthorizationController m_authorityControl;

   /**
    * Session controller used to check access rights.
    */
   protected SessionController m_sessionControl;

   /**
    * Data descriptor for the domain data types.
    */
   protected DomainDataDescriptor m_domainDescriptor;
   
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Generated serial version id for this class.
    */
   private static final long serialVersionUID = 280588233176208149L;

   // Constructors /////////////////////////////////////////////////////////////

   /**
    * Default constructor.
    */
   public DomainControllerImpl(
   ) 
   {
      super();
      
      // Do not cache anything here since if this controller is run as a stateless
      // session bean the referenced objects may not be ready
      m_domainFactory = null;
      m_roleFactory = null;
      m_roleControl = null;
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
      DataObject data = null;
      
      // We should check if creation of domain is allowed and there is no user 
      // and the id requested is NEW_ID then and only then the method will not 
      // check access right
      data = m_domainFactory.get(iId, 
                                 CallContext.getInstance().getCurrentDomainId());
      
      if ((data != null) && (!(((Domain)data).isAllowSelfRegistration() 
               && iId == DataObject.NEW_ID
               && CallContext.getInstance().getCurrentUser() == null))
           && (!(checkAccess(iId, ActionConstants.RIGHT_ACTION_VIEW) 
               == AccessRight.ACCESS_GRANTED))
         )
      {
         data = null;
         CallContext.getInstance().getMessages().addMessage(
            Messages.ACCESSRIGHT_ERRORS, "No rights to view domain.");
      }
      
      return data;
   }
   
   /**
    * {@inheritDoc}
    * 
    * @ejb.interface-method
    * @ejb.transaction type="Supports"
    */
   public List getSelfregistrationDomains(
      String strIDs,
      String strName
   ) throws OSSException
   {
      // this method doesn't check access rights because it is used
      // for self registered domains
      List lstReturn = Collections.EMPTY_LIST;
      List lstDomains = Collections.EMPTY_LIST;
      Domain domain = null;

      if ((strIDs != null) && (strIDs.length() > 0))
      {
         // get list of domains with specified IDs
         lstDomains = m_domainFactory.getMultipleDomains(strIDs);
         if (lstDomains != null && !lstDomains.isEmpty())
         {
            lstReturn = new ArrayList(lstDomains.size());
            Iterator itDomain = null;
            for (itDomain = lstDomains.iterator(); itDomain.hasNext();)
            {
               // check actual domain from the list and add it into the return list
               // in case when check passes ok
               domain = checkDomainCondition((Domain)itDomain.next(), strName);
               if (domain != null)
               {
                  lstReturn.add(domain);
               }
            }
         }
      }
      else
      {
         CallContext.getInstance().getMessages().addMessage(Messages.ALL_ERRORS, 
            "Cannot join domain with unspecified ID.");
      }

      return lstReturn;
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
      Domain newdata = (Domain)data;
      Domain createddata = null;

      // Validate data before we hit the database to improve performance
      validate(newdata);
      // don't check access rights if there is allowed creating domain for not logged users 
      // and if user is currently not logged in
      // or if there does not exist domain within the database and also there is allowed
      // creating of first domain
      if ((CallContext.getInstance().getCurrentUser() == null
           && (SecurityUtils.isAllowCreateDomain() 
               || ((SecurityUtils.isAllowCreateFirstDomain()) 
                   && (!m_domainFactory.existDomain()))))
          || ((checkAccess(DataObject.NEW_ID, ActionConstants.RIGHT_ACTION_CREATE) 
               == AccessRight.ACCESS_GRANTED)))
      {
         createddata = (Domain)m_domainFactory.create(newdata);
      }
      else
      {
         CallContext.getInstance().getMessages().addMessage(
            Messages.ACCESSRIGHT_ERRORS, "No rights to create domain.");
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
      if (checkAccess(iId, ActionConstants.RIGHT_ACTION_DELETE) 
         == AccessRight.ACCESS_GRANTED)
      {
         m_domainFactory.delete(iId, 
                                CallContext.getInstance().getCurrentDomainId());
      }
      else
      {
         CallContext.getInstance().getMessages().addMessage(
            Messages.ACCESSRIGHT_ERRORS, "No rights to delete domain.");
      }
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
      Domain newdata   = (Domain)data;
      Domain olddata   = null;
      Domain saveddata = null;
      
      OSSInvalidDataException ideException = null;
      
      // Validate data before we hit the database to improve performance
      validate(newdata);
      // Since we are modifying and the security is based on the categories, 
      // we need check that we have rights to modify the data in the old 
      // categories as well as the data in the new categories (if they were changed)
      olddata = (Domain)m_domainFactory.get(
                           data.getId(), 
                           CallContext.getInstance().getCurrentDomainId());
      if (olddata != null)
      {
         if (checkAccess(data.getId(), ActionConstants.RIGHT_ACTION_MODIFY) 
            == AccessRight.ACCESS_GRANTED)
         {
            int iCurrentDomainID = CallContext.getInstance().getCurrentDomainId();
            
            if ((iCurrentDomainID != newdata.getId())
                || (newdata.isEnabled() && newdata.isAdministration())
                || (newdata.isEnabled() && !newdata.isAdministration()
                    && !olddata.isAdministration())
               ) 
            {
               if (!newdata.isEnabled())
               {
                  try
                  {
                     // if domain has to be disabled, first we must to logout 
                     // all belonging sessions
                     m_sessionControl.logoutDomains(new int[] {newdata.getId()});
                  }
                  catch (RemoteException rExc)
                  {
                     // We cannot propagate this exception otherwise XDoclet would generate 
                     // the local interface incorrectly since it would include the declared
                     // RemoteException in it (to propagate we would have to declare it)
                     throw new OSSInternalErrorException("Remote error occurred", rExc);
                  }
               }

               saveddata = (Domain)m_domainFactory.save(newdata);
            }
            else
            {
               // don't allow disable or change administration flag for own current domain
               // and construct particular message
               if (!newdata.isEnabled())
               {
                  ideException = OSSInvalidDataException.addException(ideException,
                           DomainDataDescriptor.COL_DOMAIN_ENABLED, 
                           "Cannot disable current domain.");
               }
               if (!newdata.isAdministration() && olddata.isAdministration())
               {
                  ideException = OSSInvalidDataException.addException(ideException,
                     DomainDataDescriptor.COL_DOMAIN_ADMINISTRATION,
                     "Cannot change administration flag for current domain.");
               }
               
               if (ideException != null)
               {               
                  throw ideException;
               }
            }
         }
         else
         {
            CallContext.getInstance().getMessages().addMessage(
               Messages.ACCESSRIGHT_ERRORS, "No rights to modify domain.");
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
                           DomainDataDescriptor.COL_DOMAIN_ID, 
                           "Domains to delete have not specified IDs.");
      }

      if (ideException != null)
      {               
         throw ideException;
      }

      if (allowManipulate(strIds))
      {
         try
         {
            lsdSecurity = m_authorityControl.getRightsForCurrentUser(
                                                m_domainDescriptor.getDataType(),
                                                ActionConstants.RIGHT_ACTION_DELETE);
         }
         catch (RemoteException rExc)
         {
            // We cannot propagate this exception otherwise XDoclet would generate 
            // the local interface incorrectly since it would include the declared
            // RemoteException in it (to propagate we would have to declare it)
            throw new OSSInternalErrorException("Remote error occurred", rExc);
         }
      }   

      if (lsdSecurity == null)
      {
         CallContext.getInstance().getMessages().addMessage(
            Messages.ACCESSRIGHT_ERRORS, "No rights to delete domains or" +
                                         "current domain cannot be deleted.");
      }
      else
      {
         int iAttempted = 0;
         int iActual    = 0;
         int[] arrActualIds  = null;

         // Not all of the ids may still exist or can be accessed due to 
         // security restrictions. Figure out first what ids do exists
         // and can be accesses so that we can produce nice error message
         arrActualIds = m_domainFactory.getActualIds(strIds, lsdSecurity);
         
         if ((arrActualIds != null) && (arrActualIds.length > 0))
         {
            iActual = arrActualIds.length;
            // Delete only modifiable since only those can be deleted
            iDeleted = m_domainFactory.delete(arrActualIds, lsdSecurity);
            if (iActual != iDeleted)
            {
               throw new OSSInconsistentDataException(
                  "Deleted domain count not equal to determined actual domain count.");
            }               
         }
         
         iAttempted = StringUtils.count(strIds, ',') + 1;

         if (iAttempted != iDeleted)
         {
            if (iDeleted == 0)
            {
               CallContext.getInstance().getMessages().addErrorMessage(
               " No domains have been deleted either because some domains were already" +
               " deleted or because some of the domains are system generated and cannot " +
               " be deleted directly or you don't have rights to delete all the specified domains."
               );
            }
            else
            {
               CallContext.getInstance().getMessages().addErrorMessage(
                  "Only " + iDeleted + " domain(s) have been deleted either" +
                  " because some domains were already deleted or because some of" +
                  " the domains are system generated and cannot be deleted directly" +
                  " or because you don't have rights to delete all the specified domains."
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
   public Domain create(
      Domain data, 
      String strRoleIds
   ) throws OSSException
   {
      Domain newdata;

      // Security check performed in the called method
      newdata = (Domain)create(data);
      // assign new roles
      if ((newdata != null))
      {
         try
         {
            List lstRoles = null;
            if ((strRoleIds != null) && (strRoleIds.length() > 0))
            {
               // Since we are creating new domain and assigned roles belong to 
               // the current domain, we have to copy all roles from current 
               // domain into the new created domain
               lstRoles = m_roleControl.copyRoles(newdata.getId(), strRoleIds);
            }
            else
            {
               // No roles are assigned - automatically create self registered 
               // user role
               lstRoles = new ArrayList(1);
               lstRoles.add(m_roleControl.createSelfRegistrationRole(newdata.getId()));
            }
            // set up default user roles to domain
            newdata.setDefaultRoles(lstRoles);
         }
         catch (RemoteException rExc)
         {
            // We cannot propagate this exception otherwise XDoclet would generate 
            // the local interface incorrectly since it would include the declared
            // RemoteException in it (to propagate we would have to declare it)
            throw new OSSInternalErrorException("Remote error occurred", rExc);
         }
      }

      return newdata;
   }

   /**
    * {@inheritDoc}
    * 
    * @ejb.interface-method
    * @ejb.transaction type="Required"
    */
   public Domain save(
      Domain data, 
      String strRemoveRoleIds, 
      String strAddRoleIds
   ) throws OSSException
   {
      Domain saveddata = null;

      // Validate data before we hit the database to improve performance
      validateUserRolesAssignment(strRemoveRoleIds, strAddRoleIds);
      // Security check performed in the called method
      saveddata = (Domain)save(data);
      if (saveddata != null)
      {
         // remove default roles from domain
         if (strRemoveRoleIds.length() > 0)
         {
            // Here we are not modifying nor accessing the roles so no security 
            // check beyond the one already done on user is not necessary
            m_roleFactory.removeFromDomain(data.getId(), strRemoveRoleIds);
         }
         
         // assign new default roles
         if (strAddRoleIds.length() > 0)
         {
            // Here we are not modifying nor accessing the roles so no security 
            // check beyond the one already done on user is necessary
            m_roleFactory.assignToDomain(data.getId(), strAddRoleIds);
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
                           DomainDataDescriptor.COL_DOMAIN_ID, 
                           "Domains to update have not specified IDs.");
      }

      if (ideException != null)
      {               
         throw ideException;
      }

      if (allowManipulate(strIds))
      {
         try
         {
            lsdSecurity = m_authorityControl.getRightsForCurrentUser(
                                                m_domainDescriptor.getDataType(),
                                                ActionConstants.RIGHT_ACTION_MODIFY);
         }
         catch (RemoteException rExc)
         {
            // We cannot propagate this exception otherwise XDoclet would generate 
            // the local interface incorrectly since it would include the declared
            // RemoteException in it (to propagate we would have to declare it)
            throw new OSSInternalErrorException("Remote error occurred", rExc);
         }
      }

      if (lsdSecurity == null)
      {
         CallContext.getInstance().getMessages().addMessage(
            Messages.ACCESSRIGHT_ERRORS, "No rights to modify domains or" +
                                         " current domain cannot be modified.");
      }
      else
      {
         int iAttempted = 0;
         int iActual    = 0;
         int[] arrActualIds  = null;

         // Not all of the ids may still exist or can be accessed due to 
         // security restrictions. Figure out first what ids do exists
         // and can be accesses so that we can produce nice error message
         arrActualIds = m_domainFactory.getActualIds(strIds, lsdSecurity);
         
         if ((arrActualIds != null) && (arrActualIds.length > 0))
         {
            if (!newEnableValue)
            {
               try
               {
                  // if domains have to be disabled, first we must to logout 
                  // all belonging sessions
                  m_sessionControl.logoutDomains(arrActualIds);
               }
               catch (RemoteException rExc)
               {
                  // We cannot propagate this exception otherwise XDoclet would generate 
                  // the local interface incorrectly since it would include the declared
                  // RemoteException in it (to propagate we would have to declare it)
                  throw new OSSInternalErrorException("Remote error occurred", rExc);
               }
            }
            
            iActual = arrActualIds.length;
            iModified = m_domainFactory.updateEnable(arrActualIds, 
                                                     newEnableValue, 
                                                     lsdSecurity);
            if (iActual != iModified)
            {
               throw new OSSInconsistentDataException(
                  "Modified domains count not equal to determined actual domain count.");
            }               
         }
         iAttempted = StringUtils.count(strIds, ',') + 1;

         if (iAttempted != iModified)
         {
            if (iModified == 0)
            {
               CallContext.getInstance().getMessages().addErrorMessage(
                  " No domains have been modified either because some domains were already" +
                  " deleted or because you don't have rights to modify all the specified domains."
               );
            }
            else
            {
               CallContext.getInstance().getMessages().addErrorMessage(
                  "Only " + iModified + " domain(s) have been modified either" +
                  " because some domains were already deleted or because you don't" +
                  " have rights to modify all the specified domains."
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
    * @ejb.transaction type="Supports"
    */
   public Domain getCurrentDomainWithRoles(
   ) throws OSSException
   {
      Domain domain   = null;
      List   lstRoles = null;

      // if there is requested new user, set up default user 
      // values specified within domain
      domain = (Domain)m_domainFactory.get(
                          CallContext.getInstance().getCurrentDomainId(),
                          CallContext.getInstance().getCurrentDomainId());
      // get default roles assignrd to the domain
      lstRoles = m_roleFactory.getAllForDomain(
                    CallContext.getInstance().getCurrentDomainId(),
                    SimpleRule.ALL_DATA);
      // set up roles to domain
      domain.setDefaultRoles(lstRoles);
      
      return domain;
   }

   /**
    * {@inheritDoc}
    * 
    * @ejb.interface-method
    * @ejb.transaction type="Supports"
    */
   public List getRoles(
      int iDomainId
   ) throws OSSException
   {
      // Convenience method, security check performed in the called method
      try
      {
         return m_roleControl.getAllForDomain(iDomainId);
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
    * @ejb.transaction type="Supports"
    */
   public Domain getDomainWithAssociatedData(
      int iDomainId
   ) throws OSSException
   {
      Domain data            = null;
      List   lstRoles        = null;
      List   lstDefaultRoles = new ArrayList();
      Role   selfregRole     = null;     
      
      data = (Domain)get(iDomainId);
      if (data != null)
      {
         // Security check performed in the called method
         try
         {
            if (iDomainId != DataObject.NEW_ID)
            {
               lstRoles = m_roleControl.getAllForDomain(data.getId());
               if (lstRoles != null && lstRoles.size() > 0)
               {
                  lstDefaultRoles.addAll(lstRoles);
               }
            }
            else
            {
               // for newly created domain try to get default selfregistered 
               // user role from current domain - if exists, add it automatically 
               // at the end of the list
               selfregRole = m_roleFactory.getDefaultSelfregisteredRole(
                                 CallContext.getInstance().getCurrentDomainId());
               if (selfregRole != null)
               {
                  lstDefaultRoles.add(selfregRole);
               }
            }
         }
         catch (RemoteException rExc)
         {
            // We cannot propagate this exception otherwise XDoclet would generate 
            // the local interface incorrectly since it would include the declared
            // RemoteException in it (to propagate we would have to declare it)
            throw new OSSInternalErrorException("Remote error occurred", rExc);
         }
      }

      // set up roles to domain
      data.setDefaultRoles(lstDefaultRoles);
      
      return data;
   }

   /**
    * {@inheritDoc}
    * 
    * @ejb.interface-method
    * @ejb.transaction type="Supports"
    */
   public boolean existDomain(
   ) throws OSSException
   {
      // do not perform security check becouse this method
      // is used for creating initial default data within empty
      // database
      return m_domainFactory.existDomain();
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
         m_domainDescriptor.getDataTypeAsObject(),
         m_domainDescriptor.getDisplayableViewName(),
         ActionConstants.ACTIONS_MODIFIABLE_DATA);

      return new SecurityDefinition[]{secDef};
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
      m_roleFactory = (RoleFactory)DataFactoryManager.getInstance(
                                          RoleFactory.class);
      m_roleControl = (RoleController)ControllerManager.getInstance(
                                          RoleController.class);
      m_authorityControl = (AuthorizationController)ControllerManager.getInstance(
                                          AuthorizationController.class);
      m_sessionControl = (SessionController)ControllerManager.getInstance(
                                          SessionController.class);
      
      m_domainDescriptor = (DomainDataDescriptor)DataDescriptorManager.getInstance(
                                                    DomainDataDescriptor.class);
   }
   
   // Helper methods ///////////////////////////////////////////////////////////

   /**
    * Check access for given data object based on categories on which rights can
    * be enforces. Since the security for this data object is not dependent on 
    * the actual data elements (categories) it is sufficient to specify id of 
    * the data object.
    * 
    * @param iId - id of data object to check
    * @param iAction - action to check for, one of the ActionConstants.RIGHT_XXX 
    *                 constants
    * @return int - AccessRight.ACCESS_GRANTED or AccessRight.ACCESS_DENIED
    * @throws OSSException - an error has occurred
    */
   protected int checkAccess(
      int iId,
      int iAction
   ) throws OSSException
   {
      
      try
      {
         int iCurrentDomainId = CallContext.getInstance().getCurrentDomainId();
         int iReturnCode      = -1;
   
         if ((iCurrentDomainId != iId) && (iId != DataObject.NEW_ID))
         {
            // We want to manipulate some another domain that is not current one. 
            // First load current domain from database and then do check
            // if we can manipulate domain (in case when current domain 
            // administration flag will be true).
            if (!((Domain)m_domainFactory.get(
                  iCurrentDomainId, iCurrentDomainId)).isAdministration())
            {
               iReturnCode = AccessRight.ACCESS_DENIED;
            }
         }

         if (iReturnCode != AccessRight.ACCESS_DENIED)
         {
            iReturnCode = m_authorityControl.checkAccess(
                             m_domainDescriptor.getDataType(), iAction, iId);
         }
         
         return iReturnCode;
      }
      catch (RemoteException rExc)
      {
         throw new OSSInternalErrorException("Remote error occurred", rExc);
      }
   }

   /**
    * Method returns decicion if there is allowed manipulation with domain.
    * 
    * @param strIds - string of domain IDs separated by separator
    * @return - decicion if there is allowed manipulation with domain
    * @throws OSSException - an error has occurred
    */
   protected boolean allowManipulate(
      String strIds
   ) throws OSSException
   {
      // Check current domain administration flag and make decision about 
      // manipulating domain(s)
      // a.) administration = false ... dont't allow manipulate
      // b.) administration = true ... user can't manipulate (disable/delete) own 
      //     domain, it means if there is current domain ID within 'strIds', there 
      //     will be not allowed manipulation
      int iCurrentDomainId = CallContext.getInstance().getCurrentDomainId();
      boolean bReturn = ((Domain)m_domainFactory.get(
                                    iCurrentDomainId, 
                                    iCurrentDomainId)).isAdministration();
      
      if (bReturn)
      {
         StringBuffer sbIDs = new StringBuffer();
         StringBuffer sbID  = new StringBuffer();
   
         sbIDs.append(",");
         sbIDs.append(strIds);
         sbIDs.append(",");
   
         sbID.append(",");
         sbID.append(iCurrentDomainId);
         sbID.append(",");
   
         if (sbIDs.indexOf(sbID.toString()) != -1)
         {
            // there was found current domain ID within the domain IDs that 
            // have to be updated - don't allow update
            bReturn = false;
         }
      }
      
      return bReturn;
   }
   
   /**
    * Validate data in the specified object.
    * 
    * @param data - data to validate
    * @throws OSSInvalidDataException - the passed data were not valid
    */
   protected void validate(
      Domain data
   ) throws OSSInvalidDataException
   {
      OSSInvalidDataException ideException = null;
            
      if (data.getName().length() > m_domainDescriptor.getNameMaxLength())
      {
         if (ideException == null)
         {
            ideException = new OSSInvalidDataException();
         }
         ideException.getErrorMessages().addMessage(
            DomainDataDescriptor.COL_DOMAIN_NAME, 
            "Domain name should not be longer then " 
            + m_domainDescriptor.getNameMaxLength() + " characters."
         );
      }
      if (data.getDescription().length() > m_domainDescriptor.getDescriptionMaxLength())
      {
         if (ideException == null)
         {
            ideException = new OSSInvalidDataException();
         }
         ideException.getErrorMessages().addMessage(
            DomainDataDescriptor.COL_DOMAIN_DESCRIPTION, 
            "Description should not be longer then " 
            + m_domainDescriptor.getDescriptionMaxLength() + " characters."
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
   
   /**
    * Check domain conditions
    * 
    * @param domain - domain object
    * @param strName - domain name
    * @return boolean - true = domain condition passed
    *                 - false = domain condition don't passed
    * @throws OSSException - an error has occurred
    */
   protected Domain checkDomainCondition(
      Domain domain,
      String strName
   ) throws OSSException
   {
      Domain domainReturn = null;
      
      // Check retrieved domain:
      // a.) (domain = selfreg) and (domain = public)  and (name like nameparam)
      // b.) (domain = selfreg) and (domain != public) and (name = nameparam)
      if (((domain.isAllowSelfRegistration()) && (domain.isPublicDomain()) 
              && (domain.getName().startsWith(strName)))
           || ((domain.isAllowSelfRegistration()) && (!domain.isPublicDomain()) 
              && (domain.getName().equals(strName))))
      {
         domainReturn = domain;
      }
      
      return domainReturn;
   }
}
