/*
 * Copyright (c) 2003 - 2009 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: AccessRight.java,v 1.16 2009/04/22 06:29:19 bastafidli Exp $
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

import org.opensubsystems.core.data.DataObject;
import org.opensubsystems.core.data.impl.ModifiableDataObjectImpl;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.util.GlobalConstants;
import org.opensubsystems.security.util.ActionConstants;

/**
 * Access right represents authorization which can be granted to user to access 
 * or manipulate specific data in a specific way. Each access right is 
 * represented by Data type (User, File, ...) and Action type (View, Delete, ...). 
 * The Right type specified what type of access is assigned (for example the 
 * access is granted or denied). At this time only positive access rights are 
 * supported (access is granted and not access is denied) For some types of 
 * data there exist also additional Categories which specify what data the 
 * authorization applies to in more details (only for Active Files, only for 
 * Internal Users, only for Documents of specific Document template ...)
 * 
 * @version $Id: AccessRight.java,v 1.16 2009/04/22 06:29:19 bastafidli Exp $
 * @author Peter Satury
 * @code.reviewer Miro Halas
 * @code.reviewed Initial revision
 */
public class AccessRight extends ModifiableDataObjectImpl 
{
   // Constants ////////////////////////////////////////////////////////////////
   
   /**
    * Code for access rights that have no category set.
    * This constant is hardcoded in role.js so do not change the value.
    */
   public static final int NO_RIGHT_CATEGORY =  0;

   /**
    * Code for access rights that have no category set.
    */
   public static final Integer NO_RIGHT_CATEGORY_OBJ =  new Integer(NO_RIGHT_CATEGORY);

   /**
    * Code for access rights that have no identifier set. No surprise since
    * the identifier is ID of the data object, the identifier is equal to
    * value for uninitialized id.
    * This constant is hardcoded in role.js so do not change the value.
    */
   public static final int NO_RIGHT_IDENTIFIER = DataObject.NEW_ID;

   /**
    * Code for access rights that have no identifier set.
    */
   public static final Integer NO_RIGHT_IDENTIFIER_OBJ =  new Integer(NO_RIGHT_IDENTIFIER);
   
   /**
    * Access right type to signal that access was granted.
    */
   public static final int ACCESS_GRANTED =  1;

   /**
    * Access right type to signal that access was granted.
    */
   public static final Integer ACCESS_GRANTED_OBJ =  new Integer(ACCESS_GRANTED);
   
   /**
    * Access right type to signal that access was denied.
    */
   public static final int ACCESS_DENIED =  2;

   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Generated serial version id for this class.
    */
   private static final long serialVersionUID = -82108061176310081L;

   /**
    * Id of the role that this access right belongs to.
    */
   protected int m_iRoleId;
   
   /**
    * Code of the action that this right is for. See ActionConstants values
    */
   protected int m_iAction;
   
   /**
    * Data type that this right is tied to. See DataConstant values.
    */
   protected int m_iDataType;
   
   /**
    * Code, which will specify the type of right, for example granted or denied.
    * See AccessRight.ACCESS_XXX constants.
    */
   protected int m_iRightType;
   
   /**
    * Code for group of data objects. This can be code for attribute of data
    * object which has to have certain value matching the value specified in 
    * identifier.
    * Examples: For user it can be code which for user uniquely identifies 
    * IsSuperUser attribute. 
    */
   protected int m_iCategory;
   
   /**
    * Identifier relates the access right to specific data object or group
    * of data objects. If category is NO_RIGHT_CATEGORY then identifier is
    * ID of exact data object, which this right is for. If category is specified
    * then identifier is ID of data object associated with group of other data 
    * objects (such as if category is document owner then identifier is id the 
    * role (and if it is personal role then it represents user), which is 
    * assigned as owner of the document. If category is just some constant
    * representing some attribute of data object, then identifier can be any
    * value which represents the value of the parameter for category.
    */
   protected int m_iIdentifier;

   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Default constructor
    * 
    * @throws OSSException - an error has occurred
    */
   public AccessRight(
   ) throws OSSException
   {
      this(DataObject.NEW_ID);
   }
   
   /**
    * Basic constructor
    * 
    * @param iDomainId - id of domain in which this access right exists
    * @throws OSSException - an error has occurred
    */
   public AccessRight(
      int iDomainId
   ) throws OSSException
   {
      this(DataObject.NEW_ID, DataObject.NEW_ID, iDomainId, 
           ActionConstants.NO_RIGHT_ACTION, DataObject.NO_DATA_TYPE, 
           AccessRight.ACCESS_GRANTED, NO_RIGHT_CATEGORY, NO_RIGHT_IDENTIFIER, 
           null, null);
   }

   /**
    * Full constructor
    * 
    * @param id - id of the access right
    * @param roleId - if of the role this right belongs to
    * @param domainId - if of the domain this right is in
    * @param action - action code
    * @param dataType - data type code 
    * @param rightType - right type code - at this time we support only ACCESS_GRANTED
    * @param category - category code or NO_RIGHT_CATEGORY if the identifier 
    *                   represents ID of an object this ID is tied to
    * @param identifier - If category is NO_RIGHT_CATEGORY then identifier is
    *                     ID of exact data object, which this right is for. 
    *                     If category is specified then identifier is ID of data 
    *                     object associated with group of other data objects 
    *                     (such as if category is document owner then identifier 
    *                     is id the role (and if it is personal role then it 
    *                     represents user), which is assigned as owner of the 
    *                     document. If category is just some constant
    *                     representing some attribute of data object, then 
    *                     identifier can be any value which represents the value 
    *                     of the parameter for category.
    * @param creationTimestamp - timestamp when the access right was created
    * @param modificationTimestamp - timestamp when the access right was last time 
    *                                modified
    * @throws OSSException - an error has occurred
    */
   public AccessRight(
      int       id,
      int       roleId,
      int       domainId,
      int       action,
      int       dataType,
      int       rightType,
      int       category,
      int       identifier,
      Timestamp creationTimestamp,
      Timestamp modificationTimestamp
   ) throws OSSException
   {
      super(id, AccessRightDataDescriptor.class, domainId, creationTimestamp, 
            modificationTimestamp);
      
      if (GlobalConstants.ERROR_CHECKING)
      {
         assert rightType == ACCESS_GRANTED
                : "Only positive access rights supported at this time.";
      }
         
      m_iRoleId     = roleId;
      m_iAction     = action;
      m_iDataType   = dataType;
      m_iRightType  = rightType;
      m_iCategory   = category;
      m_iIdentifier = identifier;
   }

   /**
    * @return int - action code
    */
   public int getAction() 
   {
      return m_iAction;
   }

   /**
    * @return int - category code
    */
   public int getCategory() 
   {
      return m_iCategory;
   }

   /**
    * @return int - data type code
    */
   public int getDataType() 
   {
      return m_iDataType;
   }

   /**
    * @return int - identifier
    */
   public int getIdentifier() 
   {
      return m_iIdentifier;
   }

   /**
    * @return int - right type, always ACCESS_GRANTED since we don't support
    *               at this time negative access rights
    */
   public int getRightType() 
   {
      return m_iRightType;
   }

   /**
    * @return int - ID of the role which is parent of this access right
    */
   public int getRoleId() 
   {
      return m_iRoleId;
   }

   /**
    * @param roleId - Role ID of the role which is parent of this access right
    */
   public void setRoleId(
      int roleId
   ) 
   {
      m_iRoleId = roleId;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isSame(
      Object oObject
   )
   {
      boolean bReturn = false;
      AccessRight arHelp;

      if (oObject == this)
      {
         bReturn = true;
      }
      else
      {
         if (oObject != null && oObject instanceof AccessRight)
         {
            // cast Object to AccessRight
            arHelp = (AccessRight) oObject;
            // check all data attributes for equals
            bReturn = arHelp.m_iRoleId == m_iRoleId
                     && arHelp.m_iAction == m_iAction
                     && arHelp.m_iDataType == m_iDataType
                     && arHelp.m_iRightType == m_iRightType
                     && arHelp.m_iCategory == m_iCategory
                     && arHelp.m_iIdentifier == m_iIdentifier;
         }
      }
      return bReturn;
   }
}
