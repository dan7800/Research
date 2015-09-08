/*
 * Copyright (c) 2003 - 2009 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: AccessRightDatabaseFactory.java,v 1.31 2009/04/22 06:29:13 bastafidli Exp $
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

package org.opensubsystems.security.persist.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.opensubsystems.core.data.DataObject;
import org.opensubsystems.core.error.OSSDatabaseAccessException;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.persist.db.BasicDatabaseFactory;
import org.opensubsystems.core.persist.db.DatabaseSchemaManager;
import org.opensubsystems.core.persist.db.impl.DatabaseFactoryImpl;
import org.opensubsystems.core.persist.db.operation.DatabaseCreateMultipleDataObjectsOperation;
import org.opensubsystems.core.persist.db.operation.DatabaseCreateSingleDataObjectOperation;
import org.opensubsystems.core.persist.db.operation.DatabaseReadMultipleOperation;
import org.opensubsystems.core.persist.db.operation.DatabaseReadOperation;
import org.opensubsystems.core.persist.db.operation.DatabaseReadSingleDataObjectOperation;
import org.opensubsystems.core.persist.db.operation.DatabaseUpdateOperation;
import org.opensubsystems.core.util.CallContext;
import org.opensubsystems.core.util.GlobalConstants;
import org.opensubsystems.core.util.jdbc.DatabaseUtils;
import org.opensubsystems.security.data.AccessRight;
import org.opensubsystems.security.data.AccessRightDataDescriptor;
import org.opensubsystems.security.persist.AccessRightFactory;

/**
 * Data factory to retrieve and manipulate access rights in persistence store.  
 *
 * @version $Id: AccessRightDatabaseFactory.java,v 1.31 2009/04/22 06:29:13 bastafidli Exp $
 * @author Peter Satury
 * @code.reviewer Miro Halas
 * @code.reviewed 1.12 2005/11/02 00:46:56 jlegeny
 */
public class AccessRightDatabaseFactory extends    DatabaseFactoryImpl
                                        implements AccessRightFactory,
                                                   BasicDatabaseFactory
{
   // Cached values ////////////////////////////////////////////////////////////
   
   /**
    * Schema to use to execute database dependent operations.
    */
   protected RoleDatabaseSchema m_schema;
   
   // Constructor //////////////////////////////////////////////////////////////
   
   /**
    * Default constructor
    * 
    * @throws OSSException - an error has occurred
    */
   public AccessRightDatabaseFactory(
   ) throws OSSException 
   {
      super(AccessRightDataDescriptor.class);
      
      m_schema = ((RoleDatabaseSchema)DatabaseSchemaManager.getInstance(
                                         RoleDatabaseSchema.class));
   }
   
   // Basic operations /////////////////////////////////////////////////////////

   /**
    * {@inheritDoc}
    */
   public DataObject get(
      final int iId,
      final int iDomainId
   ) throws OSSException
   {
      DataObject data = null;

      // If the ID is supplied try to read the data from the database, 
      // if it is not, it is new data which doesn't have ID yet
      if (iId == DataObject.NEW_ID)
      {
         // These values are used as default values for new data object
         data = new AccessRight(iDomainId);
      }
      else
      {
         int[] arrColumnCodes =  ((AccessRightDataDescriptor)getDataDescriptor())
                                    .getAllColumnCodes();

         DatabaseReadOperation dbop = new DatabaseReadSingleDataObjectOperation(
            this, 
            m_schema.getSelectAccessRightById(arrColumnCodes),
            m_schema, iId, iDomainId);
         data = (DataObject)dbop.executeRead();
      }

      return data;
   }

   /**
    * {@inheritDoc}
    */
   public DataObject load(
      ResultSet rsQueryResults,
      int       initialIndex
   ) throws OSSException
   {
      AccessRight arReturn;
      
      try
      {
         // The order must exactly match the order in COLUMNS constant
         arReturn = new AccessRight(rsQueryResults.getInt(initialIndex),
                                    rsQueryResults.getInt(initialIndex + 1),
                                    rsQueryResults.getInt(initialIndex + 2),
                                    rsQueryResults.getInt(initialIndex + 3),
                                    rsQueryResults.getInt(initialIndex + 4),
                                    rsQueryResults.getInt(initialIndex + 5),
                                    rsQueryResults.getInt(initialIndex + 6),
                                    rsQueryResults.getInt(initialIndex + 7),
                                    rsQueryResults.getTimestamp(initialIndex + 8),
                                    rsQueryResults.getTimestamp(initialIndex + 9)
                                    );
         arReturn.setFromPersistenceStore();
      }
      catch (SQLException sqleExc)
      {
         throw new OSSDatabaseAccessException("Failed to load data from the database.",
                                             sqleExc);
      }
      
      return arReturn;
   }

   /**
    * {@inheritDoc}
    */
   public int setValuesForInsert(
      PreparedStatement insertStatement,
      DataObject        data,
      int               iIndex
   ) throws OSSException, 
            SQLException
   {
      AccessRight accessRight = (AccessRight)data;

      // Here you must pass the domain id sent to you in role object
      // If you want to check if this id is the same as current domain id
      // do it at the controller level. 
      insertStatement.setInt(iIndex++, accessRight.getRoleId());
      insertStatement.setInt(iIndex++, accessRight.getDomainId());
      insertStatement.setInt(iIndex++, accessRight.getAction());
      insertStatement.setInt(iIndex++, accessRight.getDataType());
      insertStatement.setInt(iIndex++, accessRight.getRightType());
      insertStatement.setInt(iIndex++, accessRight.getCategory());
      insertStatement.setInt(iIndex++, accessRight.getIdentifier());
      
      return iIndex;
   }

   // Operations specific to this factory //////////////////////////////////////

   /**
    * {@inheritDoc}
    */
   public AccessRight create(
      final AccessRight data
   ) throws OSSException 
   {
      if (GlobalConstants.ERROR_CHECKING)
      {
         assert data != null : "Data cannot be null";
         assert data.getId() == DataObject.NEW_ID 
         : "Cannot create already created data.";
      }
      DatabaseUpdateOperation dbop = new DatabaseCreateSingleDataObjectOperation(
               this, m_schema.getInsertAccessRightAndFetchGeneratedValues(), m_schema, data);
      dbop.executeUpdate();

      return (AccessRight)dbop.getReturnData();
   }

   /**
    * {@inheritDoc}
    */
   public List create(
      final List lstRights,
      final int  iRoleId
   ) throws OSSException
   {
      if (GlobalConstants.ERROR_CHECKING)
      {
         assert lstRights != null
                : "List of access rights cannot be null";
         assert lstRights.size() > 0
                : "List of access rights cannot be empty";
      }

      DatabaseUpdateOperation dbop = new DatabaseCreateMultipleDataObjectsOperation(
               this, m_schema.getInsertAccessRightAndFetchGeneratedValues(), 
               m_schema, lstRights, true)
      {
         protected void prepareData(
            DataObject data 
         )
         {
            ((AccessRight)data).setRoleId(iRoleId);
         }
      };
      dbop.executeUpdate();
      
      return (List)dbop.getReturnData();
   }

   /**
    * {@inheritDoc}
    */
   public int create(
      Collection colDataObject
   ) throws OSSException
   {
      if (GlobalConstants.ERROR_CHECKING)
      {
         assert colDataObject != null && !colDataObject.isEmpty() 
                : "Cannot create empty data list.";
      }

      DatabaseUpdateOperation dbop = new DatabaseCreateMultipleDataObjectsOperation(
         this, m_schema.getInsertAccessRight(), m_schema, colDataObject, false);
      dbop.executeUpdate();
      
      return ((Integer)dbop.getReturnData()).intValue();
   }

   /**
    * {@inheritDoc}
    */
   public List getAllForRole(
      final int roleId
   ) throws OSSException 
   {
      List lstRights = null;
      
      if (roleId != DataObject.NEW_ID)
      {
         DatabaseReadOperation dbop = new DatabaseReadMultipleOperation(
            this, m_schema.getSelectAccessRightsByRoleId(), m_schema)
         {
            protected Object performOperation(
               DatabaseFactoryImpl dbfactory,
               Connection          cntConnection,
               PreparedStatement   pstmQuery
            ) throws OSSException,
                     SQLException
            {
               pstmQuery.setInt(1, roleId);
               pstmQuery.setInt(2, CallContext.getInstance().getCurrentDomainId());
               return loadMultipleData(dbfactory, pstmQuery);
            }         
         };
         lstRights = (List)dbop.executeRead();
      }
      
      return lstRights;
   }

   /**
    * {@inheritDoc}
    */
   public List getAllForRoles(
      String strRoleIds
   ) throws OSSException
   {
      DatabaseReadOperation dbop = new DatabaseReadMultipleOperation(
         this, m_schema.getSelectAllForRoles(strRoleIds), m_schema)
      {
         protected Object performOperation(
            DatabaseFactoryImpl dbfactory,
            Connection          cntConnection,
            PreparedStatement   pstmQuery
         ) throws OSSException,
                  SQLException
         {
            pstmQuery.setInt(1, CallContext.getInstance().getCurrentDomainId());
            return loadMultipleData(dbfactory, pstmQuery);
         }         
      };
      return (List)dbop.executeRead();
   }

   /**
    * {@inheritDoc}
    */
   public int delete(
      final int roleId,
      String    rightIds
   ) throws OSSException 
   {
      DatabaseUpdateOperation dbop = new DatabaseUpdateOperation(
         this, m_schema.getDeleteAccessRights(rightIds), m_schema,
         DatabaseUpdateOperation.DBOP_DELETE, null)
      {
         protected void performOperation(
            DatabaseFactoryImpl dbfactory,
            Connection          cntConnection, 
            PreparedStatement   pstmQuery
         ) throws OSSException,
                  SQLException
         {
            int iDeleted;
            
            pstmQuery.setInt(1, roleId);
            pstmQuery.setInt(2, CallContext.getInstance().getCurrentDomainId());
            iDeleted = pstmQuery.executeUpdate();
            setReturnData(new Integer(iDeleted));
         }         
      };
      dbop.executeUpdate();
      
      return ((Integer)dbop.getReturnData()).intValue();
   }
   
   /**
    * {@inheritDoc}
    */
   public int deleteAllForRole(
      final int roleId
   ) throws OSSException 
   {
      DatabaseUpdateOperation dbop = new DatabaseUpdateOperation(
         this, m_schema.getDeleteAllAccessRightsForRoleId(), 
         m_schema, DatabaseUpdateOperation.DBOP_DELETE, null)
      {
         protected void performOperation(
            DatabaseFactoryImpl dbfactory,
            Connection          cntConnection, 
            PreparedStatement   pstmQuery
         ) throws OSSException,
                  SQLException
         {
            int iDeleted;
            
            pstmQuery.setInt(1, roleId);
            pstmQuery.setInt(2, CallContext.getInstance().getCurrentDomainId());
            iDeleted = pstmQuery.executeUpdate();
            setReturnData(new Integer(iDeleted));
         }         
      };
      dbop.executeUpdate();
      
      return ((Integer)dbop.getReturnData()).intValue();
   }

   /**
    * {@inheritDoc}
    */
   public int checkAccess(
      final int     dataType,
      final int     action,
      final int     identifier,
      final int[][] categories      
   ) throws OSSException 
   {
      final boolean validIdentifier = (identifier != AccessRight.NO_RIGHT_IDENTIFIER); 
      final int     iDomainId = CallContext.getInstance().getCurrentDomainId();

      DatabaseReadOperation dbop = new DatabaseReadOperation(
         this, m_schema.getSelectDataRightTypes(validIdentifier, 
                   (categories != null ? categories.length : 0)), m_schema)
      {
         protected Object performOperation(
            DatabaseFactoryImpl dbfactory,
            Connection          cntConnection,
            PreparedStatement   pstmQuery
         ) throws OSSException,
                  SQLException
         {
            ResultSet rsQueryResults = null;
            int       rightType;
            int       iIndex = 1;

            pstmQuery.setInt(iIndex++, CallContext.getInstance().getCurrentUserId());
            pstmQuery.setInt(iIndex++, iDomainId);
            pstmQuery.setInt(iIndex++, iDomainId);
            pstmQuery.setInt(iIndex++, dataType);
            pstmQuery.setInt(iIndex++, action);

            if (validIdentifier)
            {
               // This is for access rights assigned to particular data object when
               // the identifier is its id
               pstmQuery.setInt(iIndex++, identifier);
            }   
            
            if (categories != null)
            {
               for (int iCount = 0; iCount < categories.length; iCount++)
               {
                  pstmQuery.setInt((iCount * 2) + iIndex, categories[iCount][0]);
                  pstmQuery.setInt((iCount * 2) + iIndex + 1, categories[iCount][1]);
               }
            }   

            try
            {
               rsQueryResults = pstmQuery.executeQuery();
               // we use ordered result so we just need first right type
               if (rsQueryResults.next())
               {
                  rightType = rsQueryResults.getInt(1);            
               }
               else
               {
                  rightType = AccessRight.ACCESS_DENIED;
               }
            }
            finally
            {
               DatabaseUtils.closeResultSet(rsQueryResults);
            }
            return new Integer(rightType);
         }         
      };

      return ((Integer)dbop.executeRead()).intValue();
   }

   /**
    * {@inheritDoc}
    */
   public List getForCurrentUser(
      final int dataType, 
      final int action
   ) throws OSSException 
   {
      DatabaseReadOperation dbop = new DatabaseReadMultipleOperation(
         this, m_schema.getSelectAccessRightsForUserTypeAndAction(), m_schema)
      {
         protected Object performOperation(
            DatabaseFactoryImpl dbfactory,
            Connection          cntConnection,
            PreparedStatement   pstmQuery
         ) throws OSSException,
                  SQLException
         {
            int iIndex = 1;
            int iDomainId = CallContext.getInstance().getCurrentDomainId();
            pstmQuery.setInt(iIndex++, CallContext.getInstance().getCurrentUserId());
            pstmQuery.setInt(iIndex++, iDomainId);
            pstmQuery.setInt(iIndex++, iDomainId);
            pstmQuery.setInt(iIndex++, dataType);
            pstmQuery.setInt(iIndex++, action);
            return loadMultipleData(dbfactory, pstmQuery);
         }         
      };

      return (List)dbop.executeRead();
   }
}
