/*
 * Copyright (c) 2003 - 2009 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: Role.java,v 1.22 2009/04/22 06:29:19 bastafidli Exp $
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.opensubsystems.core.data.DataObject;
import org.opensubsystems.core.data.impl.ModifiableDataObjectImpl;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.patterns.ordereddata.data.OrderedData;

/**
 * Role represents group of authorization rights, which can be assigned to users 
 * to grant some permissions to it. Role acts just as a container but it is 
 * the access rights which signify what actions are granted for what data.
 * 
 * For each user there is single special role which collects access rights
 * granted only to this user. This type of role is called personal role.
 * Personal role has the same name as is the login name of the user and collects 
 * access rights assigned to a particular user. This way we can always display 
 * list of roles whenever we want to assign user or group of user to something.
 * 
 * @version $Id: Role.java,v 1.22 2009/04/22 06:29:19 bastafidli Exp $
 * @author Peter Satury
 * @code.reviewer Miro Halas
 * @code.reviewed Initial revision
 */
public class Role extends    ModifiableDataObjectImpl 
                  implements OrderedData
{
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Generated serial version id for this class.
    */
   private static final long serialVersionUID = -6678154288030117493L;

   /**
    * Name of the role
    */
   protected String m_strName;
   
   /**
    * Text description of the role function and usage
    */
   protected String m_strDescription;
   
   /**
    * This specify if the role should be use in authorization proccess.
    * If true then consider access rights granted by this role during 
    * authorization, if false, then ignore these access rights. This is easy way
    * how to temporarily take away some rights from user without removing the 
    * role to it.
    */
   protected boolean m_bEnabled;
   
   /**
    * Reference to user for personal roles. Personal role has the same name as 
    * is the login name of the user and collects access rights assigned to 
    * a particular user. This way we can always display list of roles whenever
    * we want to assign user or group of user to something.
    */
   protected int m_iUserId;
   
   /**
    * This role should not be modified for example because it was generated 
    * by the system.
    */
   protected boolean m_bUnmodifiable;

   /**
    * List of access rights associated with this role
    */
   protected List m_accessRights;
 
   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Default constructor
    * 
    * @throws OSSException - an error has occurred 
    */
   public Role(
   ) throws OSSException
   {
      this(DataObject.NEW_ID, DataObject.NEW_ID, "", "", true, DataObject.NEW_ID, 
           false, null, null, null
      );
   }

   /**
    * Empty role for a specified domain initialized to default parameters
    *
    * @param iDomainId - Id of the domain this role belongs to
    * @throws OSSException - an error has occurred 
    */
   public Role(
      int iDomainId
   ) throws OSSException
   {
      this(DataObject.NEW_ID, iDomainId, "", "", true, DataObject.NEW_ID, 
           false, null, null, null);
   }

   /**
    * Full constructor
    * 
    * @param roleId - ID of new Role
    * @param domainId - ID of Domain
    * @param name - unique name of Role
    * @param description - text description of Role
    * @param enabled - enable flag for Role
    * @param userId - ID of User for Personal Role 
    *                 when equals DataObject.NEW_ID, then stored in database 
    *                 like null
    * @param unmodifiable - flag if Role is modifiable 
    * @param accessRights - list of accessRights of Role
    * @param creationTimestamp - Timestamp when the Role was created
    * @param modificationTimestamp - Timestamp when the Role was last time modified
    * @throws OSSException - an error has occurred
    */
   public Role(
      int         roleId,
      int         domainId,
      String      name,
      String      description,
      boolean     enabled,
      int         userId,
      boolean     unmodifiable,
      List        accessRights,
      Timestamp   creationTimestamp,
      Timestamp   modificationTimestamp
   ) throws OSSException
   {
      super(roleId, RoleDataDescriptor.class, domainId, creationTimestamp, 
            modificationTimestamp);
      
      m_strName        = name;
      m_strDescription = (description == null) ? "" : description;
      m_bEnabled       = enabled;
      m_bUnmodifiable  = unmodifiable;
      m_iUserId        = userId;
      m_accessRights   = accessRights;
   }
   
   /**
    * @return List - list of accessRights
    */
   public List getAccessRights() 
   {
      return m_accessRights;
   }

   /**
    * @return List - list of accessRights if it is not null
    *              - EMPTY_LIST - if the list is null 
    */
   public List getAccessRightsSafely() 
   {
      List lstReturn = Collections.EMPTY_LIST;
      if (m_accessRights != null)
      {
         lstReturn = m_accessRights; 
      }
      return lstReturn;
   }

   /**
    * @return String - string representation of separated ID and 
    *                  access right parameters for each access right 
    *                  from the list  
    */
   public String getAccessRightsAsString() 
   {
      StringBuffer buffer = new StringBuffer();
      AccessRight objItem;

      if ((m_accessRights != null) && (m_accessRights.size() > 0))
      {
         for (Iterator itRights = m_accessRights.iterator(); itRights.hasNext();)
         {
            objItem = (AccessRight)itRights.next();
            buffer.append(";");
            buffer.append(objItem.getId());
            buffer.append(":");
            buffer.append(objItem.getDataType());
            buffer.append(",");
            buffer.append(objItem.getAction());
            buffer.append(",");
            buffer.append(objItem.getCategory());
            buffer.append(",");
            buffer.append(objItem.getIdentifier());
         }
         buffer.append(";");

      }
      
      return buffer.toString();
   }

   /**
    * @return String - Role description
    */
   public String getDescription() 
   {
      return m_strDescription;
   }

   /**
    * @return String - Role name
    */
   public String getName() 
   {
      return m_strName;
   }

   /**
    * @return int - user ID for personal role
    */
   public int getUserId()
   {
      return m_iUserId;
   }

   /**
    * @return boolean - true if this role is a personal role for some user
    *                   (and therefore user id is specified)
    */
   public boolean isPersonalRole()
   {
      return m_iUserId != DataObject.NEW_ID;
   }

   /**
    * @param accessRights - access rights
    */
   public void setAccessRights(
      List accessRights
   ) 
   {
      m_accessRights = accessRights;
   }
   
   /**
    * @param name - changing name
    */
   public void setName(
      String name
   )
   {
      m_strName = name;
   }
   
   /**
    * @param description - changing description
    */
   public void setDescription(
      String description
   )
   {
      m_strDescription = (description == null) ? "" : description;
   }
   

   /**
    * @return boolean - flag if Role usage is enabled 
    */
   public boolean isEnabled() 
   {
      return m_bEnabled;
   }
   
   /**
    * @return boolean - flag if Role is modifiable in database
    */
   public boolean isUnmodifiable()
   {
      return m_bUnmodifiable;
   }

   /**
    * method to add list of access rights
    *  
    * @param addedRights - List of rights to add
    */
   public void addAccessRights(
      List addedRights
   )
   {
      if ((addedRights != null) && (addedRights.size() > 0))
      {
         if (m_accessRights == null)
         {
            m_accessRights = new ArrayList();
         }
         for (Iterator itRights = addedRights.iterator(); itRights.hasNext();)
         {
            m_accessRights.add(itRights.next());
         }
      }
   }
   
   /**
    * method to delete rights by array of their IDs
    * 
    * @param deletedRightIds - array of Rights IDs
    */
   public void removeAccessRights(
      int[] deletedRightIds
   )
   {
      if ((m_accessRights != null) && (m_accessRights.size() > 0)
           && (deletedRightIds != null) && (deletedRightIds.length > 0))
      {         
         AccessRight arRight;
         int         iCount;
       
         // TODO: Performance: This can be speed up by first sorting the array
         // or keeping the array in sorted order and then using Arrays.binarySearch 
         for (Iterator itRights = m_accessRights.iterator(); itRights.hasNext();)
         {
            arRight = (AccessRight) itRights.next();
            for (iCount = 0; iCount < deletedRightIds.length; iCount++)
            {
               if (deletedRightIds[iCount] == arRight.getId())
               {
                  itRights.remove();
               }
            }
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public boolean isSame(
      Object oObject
   )
   {
      boolean bReturn = false;
      Role rHelp;

      if (oObject == this)
      {
         bReturn = true;
      }
      else
      {
         if ((oObject != null) && (oObject instanceof Role))
         {
            // cast Object to Role
            rHelp = (Role) oObject;
            // check all data attributes for equals
            bReturn = (((m_strName == null) && (rHelp.m_strName == null))
                         || ((m_strName != null) 
                            && (m_strName.equals(rHelp.m_strName))))
                      && (((m_strDescription == null) && (rHelp.m_strDescription == null))
                         || ((m_strDescription != null) 
                            && (m_strDescription.equals(rHelp.m_strDescription)))) 
                     && (rHelp.m_bEnabled == m_bEnabled)
                     && (rHelp.m_iUserId == m_iUserId)
                     && (rHelp.m_bUnmodifiable == m_bUnmodifiable);
           // not used for now
           // && ((rHelp.getAccessRights() == null && m_accessRights == null)
           //      || rHelp.getAccessRights().equals(m_accessRights));
         }
      }
      return bReturn;
   }

   /**
    * {@inheritDoc}
    */
   public int getOrderNumber()
   {
      return getId();
   }

   /**
    * {@inheritDoc}
    */
   public void setOrderNumber(
      int iOrderNumber
   )
   {
      // nothing for now
   }
}
