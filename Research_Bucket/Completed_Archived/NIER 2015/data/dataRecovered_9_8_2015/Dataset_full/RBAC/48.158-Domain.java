/*
 * Copyright (c) 2003 - 2009 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: Domain.java,v 1.27 2009/04/22 06:29:19 bastafidli Exp $
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

package org.opensubsystems.security.data;

import java.sql.Timestamp;
import java.util.List;

import org.opensubsystems.core.data.DataObject;
import org.opensubsystems.core.data.impl.ModifiableDataObjectImpl;
import org.opensubsystems.core.error.OSSException;

/**
 * Domain represents a name space or partition where other data objects live. 
 * This way we can separate for example multiple customers living in the same 
 * persistence store by creating all their data in their own domain or we can 
 * use domain to represent geographical location such as states inside of 
 * country.
 *
 * @version $Id: Domain.java,v 1.27 2009/04/22 06:29:19 bastafidli Exp $
 * @author Julo Legeny
 * @code.reviewer Miro Halas
 * @code.reviewed 1.16 2006/06/03 01:10:30 jlegeny
 */
public class Domain extends ModifiableDataObjectImpl
{
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Generated serial version id for this class.
    */
   private static final long serialVersionUID = -8965542193682927601L;

   /**
    * Domain name.
    */
   protected String m_strName;

   /**
    * Domain description.
    */
   protected String m_strDescription;

   /**
    * This specify if the domain should be accessible. If domain is enabled, 
    * users can log in. If domain is disabled, users cannot log in.
    * Default value is false. 
    */
   protected boolean m_bEnabled;

   /**
    * This attribute will specify domains, which can administer other domains. 
    * The first domain created in the schema will have this attribute set to true.
    * Default value is false. First domain within the database schema should. 
    * have this value true.
    */
   protected boolean m_bAdministration;

   /**
    * This attribute specifies if users can self register (create user accounts 
    * for themselves) in this domain. If false, user cannot create user account
    * for himself in the domain and somebody else (already existing user with 
    * the privileges) has to create the account for him. If true, user can self 
    * register himself in this domain.
    * Default value is true. 
    */
   protected boolean m_bAllowSelfRegistration;

   /**
    * This attribute specifies if the domain is public. If domain is specified as 
    * public, it can be listed and visible for outside usage (e.g. create account).
    * This value has to be changed only in case when Allow self registration flag 
    * is true.
    * Default value is false. 
    */
   protected boolean m_bPublicDomain;

   /**
    * Flag specifying if the user has by default enabled login or he has to wait 
    * until somebody enables his login. This is default predefined value that 
    * is used when new user account is created.
    * Default value is true.
    */
   protected boolean m_bDefaultLoginEnabled;

   /**
    * Flag specifies if the user is by default super user or not. This is default 
    * predefined value that is used when new user account is created.
    * Default value is true.
    */
   protected boolean m_bDefaultSuperUser;

   /**
    * Flag specifies if the user is by default internal user or not. This is 
    * default predefined value that is used when new user account is created.
    * Default value is true. 
    */
   protected boolean m_bDefaultInternalUser;

   /**
    * Default phone number of the user.
    */
   protected String m_strPhone;

   /**
    * Default postal address of the user.
    */
   protected String m_strAddress;

   /**
    * List of default user roles that will be assigned to the user when 
    * new domain is created. 
    */
   protected List m_lstDefaultRoles;

   // Constructors /////////////////////////////////////////////////////////////

   /**
    * Empty domain initialized to default parameters
    * 
    * @throws OSSException - an error has occurred
    */
   public Domain(
   ) throws OSSException
   {
      this(NEW_ID, "", "", 
           DomainDataDescriptor.DOMAIN_DEFAULT_ENABLED, 
           DomainDataDescriptor.DOMAIN_DEFAULT_ADMINISTRATION, 
           DomainDataDescriptor.DOMAIN_DEFAULT_ALLOW_SELFREG, 
           DomainDataDescriptor.DOMAIN_DEFAULT_PUBLIC, 
           DomainDataDescriptor.DOMAIN_DEFAULT_LOGIN_ENABLED, 
           DomainDataDescriptor.DOMAIN_DEFAULT_SUPER_USER, 
           DomainDataDescriptor.DOMAIN_DEFAULT_INTERNAL_USER, 
           DomainDataDescriptor.DOMAIN_DEFAULT_PHONE,
           DomainDataDescriptor.DOMAIN_DEFAULT_ADDRESS, 
           null, null, null);
   }

   /**
    * Simplest constructor.
    *
    * @param iId - Unique ID identifying this domain.
    * @param strName - Domain name.
    * @param strDescription - Domain description.
    * @param creationTimestamp - Timestamp when the domain was created
    * @param modificationTimestamp - Timestamp when the domain was last time modified
    * @throws OSSException - an error has occurred
    */
   public Domain(
      int       iId,
      String    strName,
      String    strDescription,
      Timestamp creationTimestamp,
      Timestamp modificationTimestamp
   ) throws OSSException   
   {
      this(iId, strName, strDescription,
           DomainDataDescriptor.DOMAIN_DEFAULT_ENABLED, 
           DomainDataDescriptor.DOMAIN_DEFAULT_ADMINISTRATION, 
           DomainDataDescriptor.DOMAIN_DEFAULT_ALLOW_SELFREG, 
           DomainDataDescriptor.DOMAIN_DEFAULT_PUBLIC, 
           DomainDataDescriptor.DOMAIN_DEFAULT_LOGIN_ENABLED, 
           DomainDataDescriptor.DOMAIN_DEFAULT_SUPER_USER, 
           DomainDataDescriptor.DOMAIN_DEFAULT_INTERNAL_USER, 
           DomainDataDescriptor.DOMAIN_DEFAULT_PHONE,
           DomainDataDescriptor.DOMAIN_DEFAULT_ADDRESS, 
           creationTimestamp, modificationTimestamp, null);
   }

   /**
    * Copy constructor.
    *
    * @param domain - domain data object.
    * @throws OSSException - an error has occurred
    */
   public Domain(
      Domain domain
   ) throws OSSException
   {
      this(domain.getId(), 
           domain.m_strName,
           domain.m_strDescription,
           domain.m_bEnabled,
           domain.m_bAdministration,
           domain.m_bAllowSelfRegistration,
           domain.m_bPublicDomain,
           domain.m_bDefaultLoginEnabled,
           domain.m_bDefaultSuperUser,
           domain.m_bDefaultInternalUser,
           domain.m_strPhone,
           domain.m_strAddress,
           domain.m_creationTimestamp, 
           domain.m_modificationTimestamp, 
           domain.m_lstDefaultRoles);
   }

   /**
    * Create domain from a given parameters.
    *
    * @param iId - Unique ID identifying this domain.
    * @param strName - Domain name.
    * @param strDescription - Domain description.
    * @param bEnabled - Flag specifies if the domain should be accessible
    * @param bAdministration - Flag specifies domains, which can administer 
    *                          other domains.
    * @param bAllowSelfRegistration - Flag specifies if users can self register 
    *                                 (create user accounts for themselves) in 
    *                                 this domain.
    * @param bPublicDomain - Flag specifies if the domain is public or not
    * @param bDefaultLoginEnabled - Flag specifies if the user has by default  
    *                               enabled login or he has to wait until 
    *                               somebody enables his login.
    * @param bDefaultSuperUser - Flag specifies if the user is by default super 
    *                            user.
    * @param bDefaultInternalUser - Flag specifies if the user is by default 
    *                               internal user.
    * @param strDefaultPhone - Default value for phone number.
    * @param strDefaultAddress - Default value for postal address.
    * @param lstDefaultRoles - list of deafult user Roles
    * @param creationTimestamp - Timestamp when the domain was created
    * @param modificationTimestamp - Timestamp when the domain was last time 
    *                                modified
    * throws OSSException - an error has occurred
    */
   public Domain(
      int       iId,
      String    strName,
      String    strDescription,
      boolean   bEnabled,
      boolean   bAdministration,
      boolean   bAllowSelfRegistration,
      boolean   bPublicDomain,
      boolean   bDefaultLoginEnabled,
      boolean   bDefaultSuperUser,
      boolean   bDefaultInternalUser,
      String    strDefaultPhone,
      String    strDefaultAddress,
      Timestamp creationTimestamp,
      Timestamp modificationTimestamp,
      List      lstDefaultRoles
   ) throws OSSException
   {
      this(iId,
           DomainDataDescriptor.class,
           strName,
           strDescription,
           bEnabled,
           bAdministration,
           bAllowSelfRegistration,
           bPublicDomain,
           bDefaultLoginEnabled,
           bDefaultSuperUser,
           bDefaultInternalUser,
           strDefaultPhone,
           strDefaultAddress,
           creationTimestamp,
           modificationTimestamp,
           lstDefaultRoles);
   }

   /**
    * Create domain from a given parameters. This constructor is here for derived
    * classes so that they can specify their own data descriptors.
    *
    * @param iId - Unique ID identifying this domain.
    * @param clsDataDescriptor - class identifying data descriptor for the object
    * @param strName - Domain name.
    * @param strDescription - Domain description.
    * @param bEnabled - Flag specifies if the domain should be accessible
    * @param bAdministration - Flag specifies domains, which can administer
    *                          other domains.
    * @param bAllowSelfRegistration - Flag specifies if users can self register 
    *                                 (create user accounts for themselves) in 
    *                                 this domain.
    * @param bPublicDomain - Flag specifies if the domain is public or not
    * @param bDefaultLoginEnabled - Flag specifies if the user has by default  
    *                               enabled login or he has to wait until 
    *                               somebody enables his login.
    * @param bDefaultSuperUser - Flag specifies if the user is by default super 
    *                            user.
    * @param bDefaultInternalUser - Flag specifies if the user is by default 
    *                               internal user.
    * @param strDefaultPhone - Default value for phone number.
    * @param strDefaultAddress - Default value for postal address.
    * @param lstDefaultRoles - list of deafult user Roles
    * @param creationTimestamp - Timestamp when the domain was created
    * @param modificationTimestamp - Timestamp when the domain was last time 
    *                                modified
    * @throws OSSException - an error has occurred
    */
   protected Domain(
      int       iId,
      Class     clsDataDescriptor,
      String    strName,
      String    strDescription,
      boolean   bEnabled,
      boolean   bAdministration,
      boolean   bAllowSelfRegistration,
      boolean   bPublicDomain,
      boolean   bDefaultLoginEnabled,
      boolean   bDefaultSuperUser,
      boolean   bDefaultInternalUser,
      String    strDefaultPhone,
      String    strDefaultAddress,
      Timestamp creationTimestamp,
      Timestamp modificationTimestamp,
      List      lstDefaultRoles
   ) throws OSSException
   {
      super(iId, clsDataDescriptor, DataObject.NEW_ID, 
            creationTimestamp, modificationTimestamp);

      m_strName                = strName;
      m_strDescription         = (strDescription == null) ? "" : strDescription;
      m_bEnabled               = bEnabled;
      m_bAdministration        = bAdministration;
      m_bAllowSelfRegistration = bAllowSelfRegistration;
      m_bPublicDomain          = bPublicDomain;
      m_bDefaultLoginEnabled   = bDefaultLoginEnabled;
      m_bDefaultSuperUser      = bDefaultSuperUser;
      m_bDefaultInternalUser   = bDefaultInternalUser;
      m_strPhone               = (strDefaultPhone == null) ? "" : strDefaultPhone;
      m_strAddress             = (strDefaultAddress == null) ? "" : strDefaultAddress;
      m_lstDefaultRoles        = lstDefaultRoles;
   }

   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * {@inheritDoc}
    */
   public int getDomainId(
   ) 
   {
      // For domain, domain it is the same as id 
      return getId();
   }

   /**
    * @return String
    */
   public String getName()
   {
      return m_strName;
   }

   /**
    * @param name - new domain name
    */
   public void setName(
      String name
   )
   {
      m_strName = name;
   }

   /**
    * @return String
    */
   public String getDescription()
   {
      return m_strDescription;
   }

   /**
    * @param description - new domain description
    */
   public void setDescription(
      String description
   )
   {
      m_strDescription = (description == null) ? "" : description;
   }

   /**
    * @return boolean
    */
   public boolean isEnabled()
   {
      return m_bEnabled;
   }

   /**
    * @param enabled - new enabled flag
    */
   public void setEnabled(
      boolean enabled
   )
   {
      m_bEnabled = enabled;
   }

   /**
    * @return boolean
    */
   public boolean isAdministration()
   {
      return m_bAdministration;
   }

   /**
    * @param administration - new administration flag
    */
   public void setAdministration(
      boolean administration
   )
   {
      m_bAdministration = administration;
   }

   /**
    * @return boolean
    */
   public boolean isAllowSelfRegistration()
   {
      return m_bAllowSelfRegistration;
   }

   /**
    * @param allowSelfRegistration - new allowSelfRegistration flag
    */
   public void setAllowSelfRegistration(
      boolean allowSelfRegistration
   )
   {
      m_bAllowSelfRegistration = allowSelfRegistration;
   }

   /**
    * @return boolean
    */
   public boolean isPublicDomain()
   {
      return m_bPublicDomain;
   }

   /**
    * @param publicDomain - new public domain flag
    */
   public void setPublicDomain(
      boolean publicDomain
   )
   {
      m_bPublicDomain = publicDomain;
   }

   /**
    * @return boolean
    */
   public boolean isDefaultInternalUser()
   {
      return m_bDefaultInternalUser;
   }

   /**
    * @param defaultInternalUser - new defaultInternalUser flag
    */
   public void setDefaultInternalUser(
      boolean defaultInternalUser
   )
   {
      m_bDefaultInternalUser = defaultInternalUser;
   }

   /**
    * @return boolean
    */
   public boolean isDefaultLoginEnabled()
   {
      return m_bDefaultLoginEnabled;
   }

   /**
    * @param defaultLoginEnabled - new defaultLoginEnabled flag
    */
   public void setDefaultLoginEnabled(
      boolean defaultLoginEnabled
   )
   {
      m_bDefaultLoginEnabled = defaultLoginEnabled;
   }

   /**
    * @return boolean
    */
   public boolean isDefaultSuperUser()
   {
      return m_bDefaultSuperUser;
   }

   /**
    * @param defaultSuperUser - new defaultSuperUser flag
    */
   public void setDefaultSuperUser(
      boolean defaultSuperUser
   )
   {
      m_bDefaultSuperUser = defaultSuperUser;
   }

   /**
    * @return String
    */
   public String getDefaultPhone()
   {
      return m_strPhone;
   }

   /**
    * @param phone - new default phone number
    */
   public void setDefaultPhone(
      String phone
   )
   {
      m_strPhone = (phone == null) ? "" : phone;
   }

   /**
    * @return String
    */
   public String getDefaultAddress()
   {
      return m_strAddress;
   }

   /**
    * @param address - new default postal address
    */
   public void setDefaultAddress(
      String address
   )
   {
      m_strAddress = (address == null) ? "" : address;
   }

   /**
    * @return List
    */
   public List getDefaultRoles()
   {
      return m_lstDefaultRoles;
   }

   /**
    * @param defaultRoles - list of default user roles
    */
   public void setDefaultRoles(
      List defaultRoles
   )
   {
      m_lstDefaultRoles = defaultRoles;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isSame(
      Object oObject
   )
   {
      boolean bReturn = false;
      Domain ssHelp;

      if (oObject == this)
      {
         bReturn = true;
      }
      else
      {
         if (oObject != null && oObject instanceof Domain)
         {
            // cast Object to Domain
            ssHelp = (Domain) oObject;
            // check all data attributes for equals
            bReturn = (((m_strDescription == null) && (ssHelp.m_strDescription == null))
                        || ((m_strDescription != null) 
                              && (m_strDescription.equals(ssHelp.m_strDescription))))
                      && (((m_strName == null) && (ssHelp.m_strName == null))
                            || ((m_strName != null) 
                                  && (m_strName.equals(ssHelp.m_strName))))
                      && (((m_strPhone == null) && (ssHelp.m_strPhone == null))
                            || ((m_strPhone != null) 
                                  && (m_strPhone.equals(ssHelp.m_strPhone))))
                      && (((m_strAddress == null) && (ssHelp.m_strAddress == null))
                            || ((m_strAddress != null) 
                                  && (m_strAddress.equals(ssHelp.m_strAddress))))
                      && ssHelp.isEnabled() == m_bEnabled
                      && ssHelp.isAdministration() == m_bAdministration
                      && ssHelp.isAllowSelfRegistration() == m_bAllowSelfRegistration
                      && ssHelp.isPublicDomain() == m_bPublicDomain
                      && ssHelp.isDefaultLoginEnabled() == m_bDefaultLoginEnabled
                      && ssHelp.isDefaultSuperUser() == m_bDefaultSuperUser
                      && ssHelp.isDefaultInternalUser() == m_bDefaultInternalUser;
         }
      }
      return bReturn;
   }
}
