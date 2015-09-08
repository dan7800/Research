/*
 * Copyright (c) 2003 - 2009 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: RoleDatabaseFactory.java,v 1.48 2009/04/22 06:29:13 bastafidli Exp $
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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.opensubsystems.core.data.DataObject;
import org.opensubsystems.core.error.OSSDatabaseAccessException;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.error.OSSInconsistentDataException;
import org.opensubsystems.core.error.OSSInvalidContextException;
import org.opensubsystems.core.persist.db.Database;
import org.opensubsystems.core.persist.db.ModifiableDatabaseFactory;
import org.opensubsystems.core.persist.db.impl.DatabaseFactoryImpl;
import org.opensubsystems.core.persist.db.impl.DatabaseImpl;
import org.opensubsystems.core.persist.db.operation.DatabaseCreateMultipleDataObjectsOperation;
import org.opensubsystems.core.persist.db.operation.DatabaseCreateSingleDataObjectOperation;
import org.opensubsystems.core.persist.db.operation.DatabaseDeleteSingleDataObjectOperation;
import org.opensubsystems.core.persist.db.operation.DatabaseReadMultipleOperation;
import org.opensubsystems.core.persist.db.operation.DatabaseReadOperation;
import org.opensubsystems.core.persist.db.operation.DatabaseReadSingleDataObjectOperation;
import org.opensubsystems.core.persist.db.operation.DatabaseUpdateOperation;
import org.opensubsystems.core.persist.db.operation.DatabaseUpdateSingleDataObjectOperation;
import org.opensubsystems.core.util.CallContext;
import org.opensubsystems.core.util.GlobalConstants;
import org.opensubsystems.core.util.StringUtils;
import org.opensubsystems.core.util.jdbc.DatabaseUtils;
import org.opensubsystems.patterns.listdata.data.SimpleRule;
import org.opensubsystems.patterns.listdata.persist.db.ListDatabaseFactoryImpl;
import org.opensubsystems.security.data.Role;
import org.opensubsystems.security.data.RoleDataDescriptor;
import org.opensubsystems.security.data.User;
import org.opensubsystems.security.persist.RoleFactory;
import org.opensubsystems.security.util.RoleConstants;

/**
 * Data factory to retrieve and manipulate users in persistence store.  
 *
 * @version $Id: RoleDatabaseFactory.java,v 1.48 2009/04/22 06:29:13 bastafidli Exp $
 * @author Peter Satury
 * @code.reviewer Miro Halas
 * @code.reviewed 1.39 2006/08/11 22:07:32 jlegeny
 */
public class RoleDatabaseFactory extends    ListDatabaseFactoryImpl 
                                 implements RoleFactory,
                                            ModifiableDatabaseFactory
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
   public RoleDatabaseFactory(
   ) throws OSSException 
   {
      super(RoleDataDescriptor.class, RoleDatabaseSchema.class);
      
      // As a convenience we cast it to correct type  
      m_schema = ((RoleDatabaseSchema)getListDatabaseSchema());
   }

   // List operations //////////////////////////////////////////////////////////

   /**
    * {@inheritDoc}
    */
   public DataObject load(
      ResultSet rsQueryResults,
      int[]     selectedColumns,
      int       initialIndex
   ) throws OSSException
   {
      Role data;
      
      try
      {
         int         id            = DataObject.NEW_ID;
         int         domainId      = DataObject.NEW_ID;
         String      name          = "";
         String      description   = "";
         boolean     enabled       = false;
         int         userId        = DataObject.NEW_ID;
         boolean     unmodifiable  = false;
         Timestamp   created       = null;
         Timestamp   modified      = null;
         
         for (int columnCount = initialIndex; 
              columnCount < (initialIndex + selectedColumns.length); 
              columnCount++)
         {
            switch (selectedColumns[columnCount - 1]) 
            {
               case (RoleDataDescriptor.COL_ROLE_ID) :
               { 
                  id = rsQueryResults.getInt(columnCount);
                  break;
               }
               case (RoleDataDescriptor.COL_ROLE_DOMAIN_ID) :
               { 
                  domainId = rsQueryResults.getInt(columnCount);
                  break;
               }
               case (RoleDataDescriptor.COL_ROLE_NAME) :
               { 
                  name = rsQueryResults.getString(columnCount);
                  break;
               }                                                           
               case (RoleDataDescriptor.COL_ROLE_DESCRIPTION) :
               { 
                  description = rsQueryResults.getString(columnCount);
                  break;
               }                                                           
               case (RoleDataDescriptor.COL_ROLE_ENABLED) :
               { 
                  enabled = rsQueryResults.getInt(columnCount) == 1;
                  break;
               }
               case (RoleDataDescriptor.COL_ROLE_UNMODIFIABLE) :
               {
                  unmodifiable = rsQueryResults.getInt(columnCount) == 1;
                  break;
               }
               case (RoleDataDescriptor.COL_ROLE_USER_ID) :
               {
                  userId = rsQueryResults.getInt(columnCount);
                  if (rsQueryResults.wasNull())
                  {
                     userId = DataObject.NEW_ID;
                  }
                  break;
               }
               case (RoleDataDescriptor.COL_ROLE_CREATION_DATE) :
               { 
                  created = rsQueryResults.getTimestamp(columnCount);
                  break;
               }                                                           
               case (RoleDataDescriptor.COL_ROLE_MODIFICATION_DATE) :
               { 
                  modified = rsQueryResults.getTimestamp(columnCount);
                  break;
               }                                                           
               default :
               {
                  assert false : "Unknown column ID " + selectedColumns[columnCount - 1];
               }
            }
         }        
         
         data = new Role(id,
                         domainId,
                         name,
                         description,
                         enabled,
                         userId,
                         unmodifiable,
                         null,
                         created,
                         modified);
         data.setFromPersistenceStore();
      }
      catch (SQLException sqleExc)
      {
         throw new OSSDatabaseAccessException("Failed to load data from the database.",
                                             sqleExc);
      }
   
      return data;
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
      Role role = (Role)data;

      // Here you must pass the domain id sent to you in role object
      // If you want to check if this id is the same as current domain id
      // do it at the controller level. 
      insertStatement.setInt(iIndex++, role.getDomainId());
      insertStatement.setString(iIndex++, role.getName());
      insertStatement.setString(iIndex++, role.getDescription());
      insertStatement.setInt(iIndex++, role.isEnabled() ? 1 : 0);
      if (role.getUserId() <= DataObject.NEW_ID)
      {
         insertStatement.setNull(iIndex++, Types.INTEGER);
      }
      else
      {
         insertStatement.setInt(iIndex++, role.getUserId());
      }
      insertStatement.setInt(iIndex++, role.isUnmodifiable() ? 1 : 0);
      
      return iIndex;
   }

   /**
    * {@inheritDoc}
    */
   public int setValuesForUpdate(
      PreparedStatement updateStatement, 
      DataObject        data,
      int               iIndex
   ) throws OSSException, SQLException
   {
      Role role = (Role)data;

      updateStatement.setInt(iIndex++, role.getId());
      // Here you must pass the domain id sent to you in role object
      // If you want to check if this id is the same as current domain id
      // do it at the controller level. 
      updateStatement.setInt(iIndex++, role.getDomainId());
      updateStatement.setTimestamp(iIndex++, role.getModificationTimestamp());
      updateStatement.setInt(iIndex++, role.isUnmodifiable() ? 1 : 0);

      return iIndex;
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
      
      // If the ID is supplied try to read the data from the database, if it is not,
      // it is new Role which doesn't have ID yet
      if (iId == DataObject.NEW_ID)
      {
         // These values are used as default values for new data object
         data = new Role(iDomainId);
      }
      else
      {
         int[] arrColumnCodes = getListDataDescriptor().getAllColumnCodes();
         
         DatabaseReadOperation dbop = new DatabaseReadSingleDataObjectOperation(
                  this, m_schema.getSelectRoleById(arrColumnCodes),
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
      Role data;
      
      try
      {
         int userId = rsQueryResults.getInt(initialIndex + 5);
         if (rsQueryResults.wasNull())
         {
            userId = DataObject.NEW_ID;
         }
         // The order must exactly match the order in COLUMNS constant
         data = new Role(
                  rsQueryResults.getInt(initialIndex),
                  rsQueryResults.getInt(initialIndex + 1),
                  rsQueryResults.getString(initialIndex + 2),
                  rsQueryResults.getString(initialIndex + 3),
                  rsQueryResults.getInt(initialIndex + 4) == 1,
                  userId,
                  rsQueryResults.getInt(initialIndex + 6) == 1,
                  null, // for access rights
                  rsQueryResults.getTimestamp(initialIndex + 7),
                  rsQueryResults.getTimestamp(initialIndex + 8)
         );
                           
         data.setFromPersistenceStore();
      }
      catch (SQLException sqleExc)
      {
         throw new OSSDatabaseAccessException("Failed to load data from the database.",
                                              sqleExc);
      }
      
      return data;
   }

   /**
    * {@inheritDoc}
    */
   public DataObject create(
      final DataObject data
   ) throws OSSException
   {
      if (GlobalConstants.ERROR_CHECKING)
      {
         assert data.getId() == DataObject.NEW_ID 
                : "Cannot create already created data.";
      }

      DatabaseUpdateOperation dbop = new DatabaseCreateSingleDataObjectOperation(
               this, m_schema.getInsertRoleAndFetchGeneratedValues(), m_schema, data);
      dbop.executeUpdate();
     
      return (DataObject)dbop.getReturnData();
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
         this, m_schema.getInsertRole(), m_schema, colDataObject, false);
      dbop.executeUpdate();
      
      return ((Integer)dbop.getReturnData()).intValue();
   }

   /**
    * {@inheritDoc}
    */
   public void delete(
      final int iId,
      final int iDomainId
   ) throws OSSException
   {
      if (GlobalConstants.ERROR_CHECKING)
      {
         assert iId != DataObject.NEW_ID 
                : "Cannot delete data, which wasn't created yet.";
      }

      DatabaseUpdateOperation dbop = new DatabaseDeleteSingleDataObjectOperation(
         this, m_schema.getDeleteRoleById(), m_schema, iId, iDomainId);
      dbop.executeUpdate();
   }

   // Operations specific to this factory //////////////////////////////////////

   /**
    * {@inheritDoc}
    */
   public int updateEnable(
      int[]      arrIds,
      boolean    bNewEnableValue,
      SimpleRule listSecurityData
   ) throws OSSException 
   {
      final List lstPrepStmtArguments = new ArrayList();
      
      DatabaseUpdateOperation dbop = new DatabaseUpdateOperation(
         this, m_schema.getUpdateEnable(
                  arrIds, bNewEnableValue,
                  CallContext.getInstance().getCurrentDomainId(),
                  listSecurityData, lstPrepStmtArguments), 
         m_schema, DatabaseUpdateOperation.DBOP_UPDATE, null)
      {
         protected void performOperation(
            DatabaseFactoryImpl dbfactory,
            Connection          cntConnection, 
            PreparedStatement   pstmQuery
         ) throws OSSException,
                  SQLException
         {
            final int iUpdated;
            
            DatabaseUtils.populatePreparedStatementPlaceholders(
                             pstmQuery, lstPrepStmtArguments);
            iUpdated = pstmQuery.executeUpdate();
            // set up number of updated roles
            setReturnData(new Integer(iUpdated));
         }         
      };
      dbop.executeUpdate();
     
      return ((Integer)dbop.getReturnData()).intValue();    
   }

   /**
    * {@inheritDoc}
    */
   public int[] getActualIds(
      String     strIds, 
      SimpleRule listSecurityData
   ) throws OSSException
   {
      int[]       arrActual = null;
      final List lstPrepStmtArguments = new ArrayList();

      if (GlobalConstants.ERROR_CHECKING)
      {
         assert strIds != null
                 : "String of selected Role IDs can not be null.";
         assert strIds.length() > 0
                 : "String of selected Role IDs can not be empty.";
         assert listSecurityData != null
                 : "Security data can not be null.";
      }

      if (strIds != null && strIds.length() > 0)
      {
         DatabaseReadOperation dbop = new DatabaseReadMultipleOperation(
            this, m_schema.getSelectActualRoleIds(
                     strIds,
                     CallContext.getInstance().getCurrentDomainId(),
                     listSecurityData, lstPrepStmtArguments), 
            m_schema)
         {
            protected Object performOperation(
               DatabaseFactoryImpl dbfactory,
               Connection          cntConnection,
               PreparedStatement   pstmQuery
            ) throws OSSException,
                     SQLException
            {
               DatabaseUtils.populatePreparedStatementPlaceholders(
                                pstmQuery, lstPrepStmtArguments);
               return loadMultipleIntsAsArray(pstmQuery);
            }         
         };
         arrActual = (int[])dbop.executeRead();         
      }
      
      return arrActual;
   }

   /**
    * {@inheritDoc}
    */
   public int getActualCount(
      String strIds
   ) throws OSSException
   {
      int iActual = 0;
      
      if ((strIds != null) && (strIds.length() > 0))
      {
         DatabaseReadOperation dbop = new DatabaseReadMultipleOperation(
            this, m_schema.getSelectActualRoleCount(strIds), m_schema)
         {
            protected Object performOperation(
               DatabaseFactoryImpl dbfactory,
               Connection          cntConnection,
               PreparedStatement   pstmQuery
            ) throws OSSException,
                     SQLException
            {
               pstmQuery.setInt(1, CallContext.getInstance().getCurrentDomainId());
               return new Integer(DatabaseUtils.loadAtMostOneInt(pstmQuery, 0,
                                     "Unexpected error loading count."));
            }         
         };
         iActual = ((Integer)dbop.executeRead()).intValue();   
      }
      
      return iActual;
   }

   /**
    * {@inheritDoc}
    */
   public List get(
      String     strIds,
      SimpleRule lsdSecurity
   ) throws OSSException 
   {
      final List lstPrepStmtArguments = new ArrayList();
      int[]       arrColumnCodes = getListDataDescriptor().getAllColumnCodes();
      
      DatabaseReadOperation dbop = new DatabaseReadMultipleOperation(
         this, m_schema.getSelectList(arrColumnCodes, strIds,
                  CallContext.getInstance().getCurrentDomainId(),
                  lsdSecurity, lstPrepStmtArguments), 
         m_schema)
      {
         protected Object performOperation(
            DatabaseFactoryImpl dbfactory,
            Connection          cntConnection,
            PreparedStatement   pstmQuery
         ) throws OSSException,
                  SQLException
         {
            DatabaseUtils.populatePreparedStatementPlaceholders(
                             pstmQuery, lstPrepStmtArguments);
            return loadMultipleData(dbfactory, pstmQuery);
         }         
      };
      return (List)dbop.executeRead();
   }

   /**
    * {@inheritDoc}
    */
   public Role save(
      final Role data,
      final int  roleType
   ) throws OSSException 
   {
      if (GlobalConstants.ERROR_CHECKING)
      {
         assert data.getId() != DataObject.NEW_ID
                : "Cannot save data which wasn't created yet.";
         assert (roleType >= RoleConstants.SAVE_ROLE_NONPERSONAL)
                && (roleType <= RoleConstants.SAVE_ROLE_ALWAYS) 
                : "Personal Flag can not have this value"; 
      }

      DatabaseUpdateOperation dbop = new DatabaseUpdateSingleDataObjectOperation(
               this, m_schema.getUpdateRoleAndFetchGeneratedValues(
                        data.isUnmodifiable(), roleType), m_schema, data)
      {
         protected int setValuesForUpdate(
            PreparedStatement updateStatement,
            DataObject        data,
            int               iIndex
         ) throws OSSException, 
                  SQLException
         {
            // add set up new parameters in dependancy on
            // roleType or modification flag
            if (updateStatement instanceof CallableStatement)
            {
               // if there is used CallableStatement it means there is called
               // stored procedure and we need to set up 3 parameters
               updateStatement.setString(iIndex++, ((Role)data).getName());
               updateStatement.setString(iIndex++, ((Role)data).getDescription());
               updateStatement.setInt(iIndex++, ((Role)data).isEnabled() ? 1 : 0);
            }
            else 
            {
               // there is used PreparedStatement and it means sql query is processed
               // and there can be different number of parameters to set up
               if (((Role)data).isUnmodifiable())
               {
                  updateStatement.setString(iIndex++, ((Role)data).getName());
               }
               else if (roleType != RoleConstants.SAVE_ROLE_PERSONAL)
               {
                  // there has to be updated no personal role - we need 
                  // to set up 3 parameters
                  updateStatement.setString(iIndex++, ((Role)data).getName());
                  updateStatement.setString(iIndex++, ((Role)data).getDescription());
                  updateStatement.setInt(iIndex++, ((Role)data).isEnabled() ? 1 : 0);
               }
            }

            iIndex = super.setValuesForUpdate(updateStatement, data, iIndex);
            
            if (updateStatement instanceof CallableStatement)
            {
               updateStatement.setInt(iIndex++, roleType);
            }
             
            return iIndex;
         }
      };
      dbop.executeUpdate();
     
      return (Role)dbop.getReturnData();
   }

   /**
    * {@inheritDoc}
    */
   public Role createPersonal(
      final User userData
   ) throws OSSException
   {
      if (GlobalConstants.ERROR_CHECKING)
      {
         assert userData != null
                 : "Source user object can not be null";
         assert userData.getId() != DataObject.NEW_ID
                 : "Cannot create personal role of user which is not created yet.";
      }

      // create personalRole
      Role personalRole = new Role(DataObject.NEW_ID, userData.getDomainId(), 
                              userData.getName(),
                              userData.getFullNameLastFirst(), //description
                              true, userData.getId(), false, null, null, null);

      DatabaseUpdateOperation dbop = new DatabaseCreateSingleDataObjectOperation(
               this, m_schema.getInsertRoleAndFetchGeneratedValues(), m_schema, 
               personalRole)
      {
         protected void performOperation(
            DatabaseFactoryImpl dbfactory,
            Connection          cntConnection, 
            PreparedStatement   pstmQuery
         ) throws OSSException,
                  SQLException
         {
            super.performOperation(dbfactory, cntConnection, pstmQuery);
            Role data = (Role)getReturnData();
            assignToUser(cntConnection, userData.getId(), 
                         Integer.toString(data.getId()));
         }
         
         protected void handleSQLException(
            SQLException sqleExc,
            Connection   cntConnection,
            int          operationType,
            int          dataType,
            String       strDisplayableViewName,
            Object       data
         ) throws OSSException
         {
            if (sqleExc.getMessage().indexOf(UserDatabaseSchema.USER_TABLE_NAME 
                  + "_ROLE_MAP_FK1") > -1)
            {
               throw new OSSInvalidContextException(
                         "User to assign the roles to does not exist anymore.", 
                         sqleExc);
            }
            else if (sqleExc.getMessage().indexOf(
                       UserDatabaseSchema.USER_TABLE_NAME + "_ROLE_MAP_FK2") > -1)
            {
               throw new OSSInvalidContextException(
                         "Role to assign to user does not exist anymore.", 
                         sqleExc);
            }
            else
            {
               super.handleSQLException(sqleExc, cntConnection, operationType, 
                        dataType, strDisplayableViewName,
                        data);
            }
         }
      };
      dbop.executeUpdate();
     
      return (Role)dbop.getReturnData();
   }

   /**
    * {@inheritDoc}
    */
   public Role savePersonal(
      User userData
   ) throws OSSException
   {
      Role returnPersonalRole = null;
      if (GlobalConstants.ERROR_CHECKING)
      {
         assert userData != null
                 : "Source user object can not be null";
         assert userData.getId() != DataObject.NEW_ID
                 : "Cannot update personal role of user which is not created yet.";
      }

      Role personalRole = getPersonal(userData.getId());
      if (personalRole != null)
      {
         personalRole.setName(userData.getLoginName());
         personalRole.setDescription(userData.getFullNameLastFirst());      
         returnPersonalRole = save(personalRole, RoleConstants.SAVE_ROLE_ALWAYS);
      }
      else
      {
         throw new OSSInconsistentDataException(
                  "There is no personal role created for user with id = " + userData.getId());
      }
      
      return returnPersonalRole;
   }

   /**
    * {@inheritDoc}
    */
   public int deletePersonal(
      final int[] arrUserIds
   ) throws OSSException
   {
      DatabaseUpdateOperation dbop = new DatabaseUpdateOperation(
         this, null, m_schema, DatabaseUpdateOperation.DBOP_DELETE, null)
      {
         protected void performOperation(
            DatabaseFactoryImpl dbfactory,
            Connection          cntConnection, 
            PreparedStatement   pstmQuery
         ) throws OSSException,
                  SQLException
         {
            // set up number of deleted users
            setReturnData(new Integer(m_schema.deletePersonalRoles(cntConnection, arrUserIds)));
         }         
      };
      dbop.executeUpdate();

      return ((Integer)dbop.getReturnData()).intValue();
   }

   /**
    * {@inheritDoc}
    */
   public Role getPersonal(
      final int iUserId
   ) throws OSSException
   {
      if (GlobalConstants.ERROR_CHECKING)
      {
         assert iUserId != DataObject.NEW_ID
                 : "Cannot get role from database for user, which wasn't created yet.";
      }

      int[] arrColumnCodes = getListDataDescriptor().getAllColumnCodes();
      
      DatabaseReadOperation dbop = new DatabaseReadOperation(
         this, m_schema.getPersonalRoleByUserId(arrColumnCodes), m_schema)
      {
         protected Object performOperation(
            DatabaseFactoryImpl dbfactory,
            Connection          cntConnection,
            PreparedStatement   pstmQuery
         ) throws OSSException,
                  SQLException
         {
            pstmQuery.setInt(1, iUserId);
            pstmQuery.setInt(2, CallContext.getInstance().getCurrentDomainId());
            return (Role)loadAtMostOneData(dbfactory, pstmQuery,
                           "There is more than one personal role assigned to user " 
                     + iUserId);
         }         
      };
      return (Role)dbop.executeRead();
   }

   /**
    * {@inheritDoc}
    */
   public boolean isAnyOfSpecifiedPersonal(
      Collection colIds
   ) throws OSSException
   {
      if (GlobalConstants.ERROR_CHECKING)
      {
         assert (colIds != null) && (!colIds.isEmpty())
                : "There are no role IDs specified";
      }

      DatabaseReadOperation dbop = new DatabaseReadOperation(
         this, m_schema.getPersonalRoleIds(colIds), m_schema)
      {
         protected Object performOperation(
            DatabaseFactoryImpl dbfactory,
            Connection          cntConnection,
            PreparedStatement   pstmQuery
         ) throws OSSException,
                  SQLException
         {
            ResultSet rsQueryResults = null;
            Boolean bReturn = Boolean.FALSE;

            try
            {
               pstmQuery.setInt(1, CallContext.getInstance().getCurrentDomainId());
               rsQueryResults = pstmQuery.executeQuery();
               
               if (rsQueryResults.next())
               {
                  bReturn = Boolean.TRUE; 
               }
            }
            finally
            {
               DatabaseUtils.closeResultSet(rsQueryResults);
            }
            return bReturn;
         }         
      };
      return ((Boolean)dbop.executeRead()).booleanValue();
   }

   /**
    * {@inheritDoc}
    */
   public int delete(
      final int[]      arrIds,
      final SimpleRule listSecurityData,
      final int        roleType
   ) throws OSSException 
   {
      DatabaseUpdateOperation dbop = new DatabaseUpdateOperation(
         this, null, m_schema, DatabaseUpdateOperation.DBOP_DELETE, null)
      {
         protected void performOperation(
            DatabaseFactoryImpl dbfactory,
            Connection          cntConnection, 
            PreparedStatement   pstmQuery
         ) throws OSSException,
                  SQLException
         {
            setReturnData(new Integer(m_schema.deleteRoles(
                                 cntConnection, arrIds,
                                 CallContext.getInstance().getCurrentDomainId(),
                                 listSecurityData, roleType)));
         }         
      };
      dbop.executeUpdate();

     return ((Integer)dbop.getReturnData()).intValue();
   }

   /**
    * {@inheritDoc}
    */
   public List getAllForUser(
      final int  iUserId, 
      SimpleRule lsdSecurity
   ) throws OSSException
   {
      List       lstUserRoles = null;
      final List lstPrepStmtArguments = new ArrayList();
      
      // This is valid check in case when we want to retrieve roles for a template 
      // for new user we do not return anything 
      if (iUserId != DataObject.NEW_ID)
      {
         int[] arrColumnCodes = getListDataDescriptor().getAllColumnCodes();
         
         DatabaseReadOperation dbop = new DatabaseReadMultipleOperation(
            this, m_schema.geSelectRolesForUserId(
                     arrColumnCodes, iUserId, 
                     CallContext.getInstance().getCurrentDomainId(),
                     lsdSecurity, lstPrepStmtArguments),
            m_schema)
         {
            protected Object performOperation(
               DatabaseFactoryImpl dbfactory,
               Connection          cntConnection,
               PreparedStatement   pstmQuery
            ) throws OSSException,
                     SQLException
            {
               DatabaseUtils.populatePreparedStatementPlaceholders(
                                pstmQuery, lstPrepStmtArguments);
               return loadMultipleData(dbfactory, pstmQuery);
            }         
         };
         lstUserRoles = (List)dbop.executeRead();
      }

      return lstUserRoles;
   }

   /**
    * {@inheritDoc}
    */
   public List getAllForDomain(
      final int iDomainId, 
      SimpleRule lsdSecurity
   ) throws OSSException
   {
      List       lstDefaultUserRoles = null;
      final List lstPrepStmtArguments = new ArrayList();
      
      // This is valid check in case when we want to retrieve roles for a template 
      // for new user we do not return anything 
      if (iDomainId != DataObject.NEW_ID)
      {
         int[] arrColumnCodes = getListDataDescriptor().getAllColumnCodes();
         
         DatabaseReadOperation dbop = new DatabaseReadMultipleOperation(
            this, m_schema.geSelectRolesForDomainId(
                     arrColumnCodes, iDomainId, lsdSecurity,
                     lstPrepStmtArguments),
            m_schema)
         {
            protected Object performOperation(
               DatabaseFactoryImpl dbfactory,
               Connection          cntConnection,
               PreparedStatement   pstmQuery
            ) throws OSSException,
                     SQLException
            {
               DatabaseUtils.populatePreparedStatementPlaceholders(
                                pstmQuery, lstPrepStmtArguments);
               return loadMultipleData(dbfactory, pstmQuery);
            }         
         };
         lstDefaultUserRoles = (List)dbop.executeRead();
      }

      return lstDefaultUserRoles;
   }

   /**
    * {@inheritDoc}
    */
   public List getAllRolesInDomain(
      final int iDomainId, 
      SimpleRule lsdSecurity
   ) throws OSSException
   {
      List       lstAllRoles = null;
      final List lstPrepStmtArguments = new ArrayList();
      
      // This is valid check in case when we want to retrieve roles for a template 
      // for new user we do not return anything 
      if (iDomainId != DataObject.NEW_ID)
      {
         int[] arrColumnCodes = getListDataDescriptor().getAllColumnCodes();
         
         DatabaseReadOperation dbop = new DatabaseReadMultipleOperation(
            this, m_schema.geSelectAllRolesInDomain(
                     arrColumnCodes, iDomainId, lsdSecurity,
                     lstPrepStmtArguments),
            m_schema)
         {
            protected Object performOperation(
               DatabaseFactoryImpl dbfactory,
               Connection          cntConnection,
               PreparedStatement   pstmQuery
            ) throws OSSException,
                     SQLException
            {
               DatabaseUtils.populatePreparedStatementPlaceholders(
                                pstmQuery, lstPrepStmtArguments);
               return loadMultipleData(dbfactory, pstmQuery);
            }         
         };
         lstAllRoles = (List)dbop.executeRead();
      }

      return lstAllRoles;
   }

   /**
    * {@inheritDoc}
    */
   public Role getDefaultSelfregisteredRole(
      final int iId
   ) throws OSSException 
   {
      Role data = null;
      final int  iDomainId = CallContext.getInstance().getCurrentDomainId();
      
      // If the ID is supplied try to read the data from the database, if it is not,
      // it is new Role which doesn't have ID yet
      if (iId == DataObject.NEW_ID)
      {
         // These values are used as default values for new data object
         data = new Role(iDomainId);
      }
      else
      {
         int[] arrColumnCodes = getListDataDescriptor().getAllColumnCodes();
         
         DatabaseReadOperation dbop = new DatabaseReadOperation(
            this, m_schema.geSelectSelfregisteredRole(arrColumnCodes), m_schema)
         {
            protected Object performOperation(
               DatabaseFactoryImpl dbfactory,
               Connection          cntConnection,
               PreparedStatement   pstmQuery
            ) throws OSSException,
                     SQLException
            {
               pstmQuery.setInt(1, iDomainId);
               return loadAtMostOneData(dbfactory, pstmQuery,
                         "Multiple records loaded from database for domain ID"
                         + iDomainId);
            }         
         };
         data = (Role)dbop.executeRead();
      }
      
      return data;
   }

   /**
    * {@inheritDoc}
    */
   public int assignToUser(
      final int    iUserId, 
      final String strUserRoleIDs
   ) throws OSSException
   {
      DatabaseUpdateOperation dbop = new DatabaseUpdateOperation(
         this, DatabaseUpdateOperation.DBOP_INSERT)
      {
         protected void performOperation(
            DatabaseFactoryImpl dbfactory,
            Connection          cntConnection, 
            PreparedStatement   pstmQuery
         ) throws OSSException,
                  SQLException
         {
            setReturnData(new Integer(assignToUser(cntConnection, iUserId, 
                                                   strUserRoleIDs)));
         }         

         protected void handleSQLException(
            SQLException sqleExc,
            Connection   cntConnection,
            int          operationType,
            int          dataType,
            String       strDisplayableViewName,
            Object       data
         ) throws OSSException
         {
            if (sqleExc.getMessage().indexOf(UserDatabaseSchema.USER_TABLE_NAME 
                  + "_ROLE_MAP_FK1") > -1)
            {
               throw new OSSInvalidContextException(
                         "User to assign the roles to does not exist anymore.", 
                         sqleExc);
            }
            else if (sqleExc.getMessage().indexOf(
                       UserDatabaseSchema.USER_TABLE_NAME + "_ROLE_MAP_FK2") > -1)
            {
               throw new OSSInvalidContextException(
                         "Role to assign to user does not exist anymore.", 
                         sqleExc);
            }
            else
            {
               super.handleSQLException(sqleExc, cntConnection, operationType, 
                        dataType, strDisplayableViewName,
                        data);
            }
         }
      };
      dbop.executeUpdate();

      return ((Integer)dbop.getReturnData()).intValue();
   }

   /**
    * {@inheritDoc}
    */
   public int assignToDomain(
      final int iDomainId, 
      final String strRoleIds
   ) throws OSSException
   {
      DatabaseUpdateOperation dbop = new DatabaseUpdateOperation(
         this, DatabaseUpdateOperation.DBOP_INSERT)
      {
         protected void performOperation(
            DatabaseFactoryImpl dbfactory,
            Connection          cntConnection, 
            PreparedStatement   pstmQuery
         ) throws OSSException,
                  SQLException
         {
            setReturnData(new Integer(assignToDomain(cntConnection, 
                                                     iDomainId, strRoleIds)));
         }

         protected void handleSQLException(
            SQLException sqleExc,
            Connection   cntConnection,
            int          operationType,
            int          dataType,
            String       strDisplayableViewName,
            Object       data
         ) throws OSSException
         {
            if (sqleExc.getMessage().indexOf("BF_DOM_ROL_MAP_FK1") > -1)
            {
               throw new OSSInvalidContextException(
                  "Domain the default roles have to be assigned to does not"
                  + " exist anymore.", sqleExc);
            }
            else if (sqleExc.getMessage().indexOf("BF_DOM_ROL_MAP_FK2") > -1)
            {
               throw new OSSInvalidContextException(
                  "Role to assign to domain does not exist anymore.", sqleExc);
            }
            else
            {
               super.handleSQLException(sqleExc, cntConnection, operationType, 
                                        dataType, strDisplayableViewName,
                                        data);
            }
         }         
      };
      dbop.executeUpdate();

      return ((Integer)dbop.getReturnData()).intValue();
   }

   /**
    * {@inheritDoc}
    */
   public int removeFromUser(
      final int iUserId, 
      String    strRoleIds
   ) throws OSSException
   {
      DatabaseUpdateOperation dbop = new DatabaseUpdateOperation(
         this, m_schema.getRemoveRolesFromUser(strRoleIds),
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
            
            pstmQuery.setInt(1, iUserId);
            iDeleted = pstmQuery.executeUpdate();
            // set up number of deleted roles
            setReturnData(new Integer(iDeleted));
         }         
      };
      dbop.executeUpdate();

      return ((Integer)dbop.getReturnData()).intValue();
   }

   /**
    * {@inheritDoc}
    */
   public int removeFromDomain(
      final int iDomainId, 
      String strRoleIds
   ) throws OSSException
   {
      DatabaseUpdateOperation dbop = new DatabaseUpdateOperation(
         this, m_schema.getRemoveRolesFromDomain(strRoleIds),
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
            
            pstmQuery.setInt(1, iDomainId);
            iDeleted = pstmQuery.executeUpdate();
            // set up number of deleted roles
            setReturnData(new Integer(iDeleted));
         }         
      };
      dbop.executeUpdate();

      return ((Integer)dbop.getReturnData()).intValue();
   }

   /**
    * {@inheritDoc}
    */
   public int removeFromUsers(
      final String  strUserIds,
      final boolean bRemoveExceptSpecified
   ) throws OSSException
   {
      // We will send as parameter 2nd statement (for deleting items from role table)
      // the 1st statement (for deleting items from user-role map table) will be
      // created inside performOperation() and also there will be closed statement 
      // for it. In the opposite order there will be problem with closing statement.
      DatabaseUpdateOperation dbop = new DatabaseUpdateOperation(
         this, m_schema.getRemoveRolesFromUsers(strUserIds, bRemoveExceptSpecified),
         m_schema, DatabaseUpdateOperation.DBOP_DELETE, null)
      {
         protected void performOperation(
            DatabaseFactoryImpl dbfactory,
            Connection          cntConnection, 
            PreparedStatement   pstmQuery
         ) throws OSSException,
                  SQLException
         {
            PreparedStatement pstmQuery1 = null;
            try
            {
               // first remove all references from user-role map table
               pstmQuery1 = cntConnection.prepareStatement(
                  m_schema.getRemoveMappedRolesFromUsers(strUserIds, bRemoveExceptSpecified)
               );
               pstmQuery1.executeUpdate();
            }
            finally
            {
               DatabaseUtils.closeStatement(pstmQuery1);
            }
            // remove all roles for specified users from the role table
            // and set up number of deleted roles
            setReturnData(new Integer(pstmQuery.executeUpdate()));
         }         
      };
      dbop.executeUpdate();

      return ((Integer)dbop.getReturnData()).intValue();
   }

   /**
    * {@inheritDoc}
    */
   public int removeFromDomains(
      final String strDomainIds, 
      final boolean bRemoveExceptSpecified
   ) throws OSSException
   {
      // We will send as parameter 2nd statement (for deleting items from role table)
      // the 1st statement (for deleting items from domain-role map table) will be
      // created inside performOperation() and also there will be closed statement 
      // for it. In the opposite order there will be problem with closing statement.
      DatabaseUpdateOperation dbop = new DatabaseUpdateOperation(
         this, m_schema.getRemoveRolesFromDomains(strDomainIds, bRemoveExceptSpecified),
         m_schema, DatabaseUpdateOperation.DBOP_DELETE, null)
      {
         protected void performOperation(
            DatabaseFactoryImpl dbfactory,
            Connection          cntConnection, 
            PreparedStatement   pstmQuery
         ) throws OSSException,
                  SQLException
         {
            PreparedStatement pstmQuery1 = null;
            try
            {
               // first remove all references from domain-role map table
               pstmQuery1 = cntConnection.prepareStatement(
                  m_schema.getRemoveMappedRolesFromDomains(strDomainIds, bRemoveExceptSpecified)
               );
               pstmQuery1.executeUpdate();
            }
            finally
            {
               DatabaseUtils.closeStatement(pstmQuery1);
            }
            // remove all roles for specified users from the role table
            // and set up number of deleted roles
            setReturnData(new Integer(pstmQuery.executeUpdate()));
         }         
      };
      dbop.executeUpdate();

      return ((Integer)dbop.getReturnData()).intValue();
   }

   // Helper methods ///////////////////////////////////////////////////////////

   /**
    * Assign new roles to user. This method is shared so that the calling method
    * can control the transaction per connection.
    * 
    * @param cntConnection - connection to use
    * @param iUserId - ID of the user
    * @param strUserRoleIds - string of roles IDs separated by ',' that should be
    *                         assigned to user
    * @return int - number of assigned items
    * @throws SQLException - an error has occurred
    * @throws OSSException - an error has occurred
    */ 
   protected int assignToUser(
      Connection cntConnection,
      int        iUserId, 
      String     strUserRoleIds
   ) throws SQLException,
            OSSException
   {
      PreparedStatement pstmInsert = null;
      int               iInserted = 0;
         
      try
      {
         int[] insertedId = StringUtils.parseStringToIntArray(strUserRoleIds, ",");
         int[] arrInsertedReturn;
         int   iBatchedCount = 0;

         if (insertedId != null)
         {
            int        iBatchSize;
            Database   database;
            
            database = DatabaseImpl.getInstance();
            iBatchSize = database.getBatchSize();

            pstmInsert = cntConnection.prepareStatement(
               m_schema.getAssignRoleToUser());

            for (int iCount = 0; iCount < insertedId.length; iCount++)
            {
               // set values for prepared statement
               pstmInsert.setInt(1, iUserId);
               pstmInsert.setInt(2, insertedId[iCount]);
               pstmInsert.addBatch();
               iBatchedCount++;
      
               // test if there is time to execute batch
               if (((iBatchedCount % iBatchSize) == 0) 
                  || (iBatchedCount == insertedId.length))
               {
                  arrInsertedReturn = pstmInsert.executeBatch();
                  iInserted += arrInsertedReturn.length; 
               }
            }
         }
      }
      finally
      {
         DatabaseUtils.closeStatement(pstmInsert);
      }
      
      return iInserted;
   }

   /**
    * Assign new default roles to domain. This method is shared so that the calling method
    * can control the transaction per connection.
    * 
    * @param cntConnection - connection to use
    * @param iDomainId - ID of the domain
    * @param strRoleIds - string of role IDs separated by ',' that should be
    *                     assigned to domain
    * @return int - number of assigned items
    * @throws SQLException - an error has occurred
    * @throws OSSException - an error has occurred
    */ 
   protected int assignToDomain(
      Connection cntConnection,
      int        iDomainId, 
      String     strRoleIds
   ) throws SQLException,
            OSSException
   {
      PreparedStatement pstmInsert = null;
      int               iInserted = 0;
         
      try
      {
         int[] insertedId = StringUtils.parseStringToIntArray(strRoleIds, ",");
         int[] arrInsertedReturn;
         int   iBatchedCount = 0;

         if (insertedId != null)
         {
            int        iBatchSize;
            Database   database;
            
            database = DatabaseImpl.getInstance();
            iBatchSize = database.getBatchSize();

            pstmInsert = cntConnection.prepareStatement(
                                          m_schema.getAssignRoleToDomain());

            for (int iCount = 0; iCount < insertedId.length; iCount++)
            {
               // set values for prepared statement
               pstmInsert.setInt(1, iDomainId);
               pstmInsert.setInt(2, insertedId[iCount]);
               pstmInsert.addBatch();
               iBatchedCount++;
      
               // test if there is time to execute batch
               if (((iBatchedCount % iBatchSize) == 0) 
                  || (iBatchedCount == insertedId.length))
               {
                  arrInsertedReturn = pstmInsert.executeBatch();
                  iInserted += arrInsertedReturn.length; 
               }
            }
         }
      }
      finally
      {
         DatabaseUtils.closeStatement(pstmInsert);
      }
      
      return iInserted;
   }
}
