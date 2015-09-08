/*
 * Copyright (c) 2003 - 2008 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: RoleDatabaseSchema.java,v 1.55 2009/04/22 06:29:14 bastafidli Exp $
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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opensubsystems.core.data.DataDescriptorManager;
import org.opensubsystems.core.data.DataObject;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.error.OSSInvalidContextException;
import org.opensubsystems.core.error.OSSInvalidDataException;
import org.opensubsystems.core.persist.db.DatabaseSchema;
import org.opensubsystems.core.persist.db.DatabaseSchemaManager;
import org.opensubsystems.core.persist.db.impl.DatabaseImpl;
import org.opensubsystems.core.util.GlobalConstants;
import org.opensubsystems.core.util.Log;
import org.opensubsystems.core.util.StringUtils;
import org.opensubsystems.core.util.jdbc.DatabaseUtils;
import org.opensubsystems.patterns.listdata.data.SimpleRule;
import org.opensubsystems.patterns.listdata.persist.db.ListDatabaseUtils;
import org.opensubsystems.patterns.listdata.persist.db.ModifiableListDatabaseSchemaImpl;
import org.opensubsystems.security.data.AccessRight;
import org.opensubsystems.security.data.AccessRightDataDescriptor;
import org.opensubsystems.security.data.Role;
import org.opensubsystems.security.data.RoleDataDescriptor;
import org.opensubsystems.security.util.RoleConstants;
import org.opensubsystems.security.util.RoleUtils;

/**
 * Database specific operations related to persistence of roles.
 * 
 * @version $Id: RoleDatabaseSchema.java,v 1.55 2009/04/22 06:29:14 bastafidli Exp $
 * @author Peter Satury
 * @code.reviewer Miro Halas
 * @code.reviewed Initial revision
 */
public abstract class RoleDatabaseSchema extends ModifiableListDatabaseSchemaImpl
{
   // Constants ////////////////////////////////////////////////////////////////
   
   /**
    * Name identifies this schema in the database. 
    */
   public static final String ROLE_SCHEMA_NAME = "ROLE";
   
   /**
    * Version of this schema in the database.
    * The version number is 2 since 
    * - version 1 was released to customer in release 2.0.3
    */
   public static final int ROLE_SCHEMA_VERSION = 3;   
   
   /**
    * Columns which always have to be retrieved from the database
    * We always need data object id and domain id to correctly identify the 
    * object and modification date to provide support for optimistic locking.
    * The user id is required to distinguish if the role is generic or personal 
    * (and therefore for example display correct icon). Name is the unique item 
    * used to display hyperlink to access details of the role. 
    */
   public static final int[] ROLE_MANDATORY_RETRIEVE_COLUMNS 
                                =  {RoleDataDescriptor.COL_ROLE_ID, 
                                    RoleDataDescriptor.COL_ROLE_DOMAIN_ID,
                                    RoleDataDescriptor.COL_ROLE_MODIFICATION_DATE,
                                    RoleDataDescriptor.COL_ROLE_USER_ID,
                                    RoleDataDescriptor.COL_ROLE_NAME,
                                   };

   /**
    * Static variable for array of all columns codes that can be used for sorting.
    */
   public static final int[] ROLE_SORT_COLUMNS = {RoleDataDescriptor.COL_ROLE_NAME,
                                                  RoleDataDescriptor.COL_ROLE_ENABLED,
                                                  RoleDataDescriptor.COL_ROLE_CREATION_DATE,
                                                  RoleDataDescriptor.COL_ROLE_MODIFICATION_DATE,
                                                 };

   /**
    * Table name where role informations are stored.
    */
   public static final String ROLE_TABLE_NAME = "BF_ROLE";

   /**
    * Table name where access right informations are stored.
    */
   public static final String ACCESSRIGHT_TABLE_NAME = "BF_ACCESS_RIGHT";

   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(UserDatabaseSchema.class);

   /**
    * Data descriptor for the role data types.
    */
   protected RoleDataDescriptor m_roleDescriptor;
   
   /**
    * Data descriptor for the access right data types.
    */
   protected AccessRightDataDescriptor m_accessRightDescriptor;
   
   // Constructors /////////////////////////////////////////////////////////////

   /**
    * Default constructor.
    * 
    * @throws OSSException - error occurred.
    */
   public RoleDatabaseSchema(
   ) throws OSSException
   {
      // Only the role data objects are scrollable at this time since the gui
      // displays all access rights at the same time
      super(new DatabaseSchema[] 
               {DatabaseSchemaManager.getInstance(DomainDatabaseSchema.class),
                DatabaseSchemaManager.getInstance(UserDatabaseSchema.class),
               }, 
            ROLE_SCHEMA_NAME, ROLE_SCHEMA_VERSION, true, 
            createModifiableTableNameMap(),  
            RoleDataDescriptor.class, ROLE_TABLE_NAME,
            ROLE_MANDATORY_RETRIEVE_COLUMNS, ROLE_SORT_COLUMNS);
      
      m_roleDescriptor = (RoleDataDescriptor)DataDescriptorManager
                            .getInstance(RoleDataDescriptor.class);
      m_accessRightDescriptor = (AccessRightDataDescriptor)DataDescriptorManager
                                   .getInstance(AccessRightDataDescriptor.class);
   }

   // Logic ////////////////////////////////////////////////////////////////////

   /**
    * Method returns simple insert role query. This method is common for all
    * databases and can be overwritten for each specific database schema.
    *
    * @return String - simple insert role query
    * @throws OSSException - exception during getting query
    */
   public String getInsertRole(
   ) throws OSSException
   {
      StringBuffer buffer = new StringBuffer();
      
      buffer.append("insert into " + ROLE_TABLE_NAME + "(DOMAIN_ID, NAME, DESCRIPTION, " +
                    "ENABLED, USER_ID, UNMODIFIABLE, CREATION_DATE, MODIFICATION_DATE) " +
                    "values (?, ?, ?, ?, ?, ?, ");
      buffer.append(DatabaseImpl.getInstance().getSQLCurrentTimestampFunctionCall());
      buffer.append(",");
      buffer.append(DatabaseImpl.getInstance().getSQLCurrentTimestampFunctionCall());
      buffer.append(")");

      return buffer.toString();
   }

   /**
    * Method returns simple insert access right query. This method is common for all
    * databases and can be overwritten for each specific database schema.
    *
    * @return String - simple insert access right query
    * @throws OSSException - exception during getting query
    */
   public String getInsertAccessRight(
   ) throws OSSException
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append("insert into " + ACCESSRIGHT_TABLE_NAME + " (ROLE_ID, DOMAIN_ID, ACTION, " +
                    "DATA_TYPE, RIGHT_TYPE, CATEGORY, IDENTIFIER, " +
                    "CREATION_DATE, MODIFICATION_DATE) " +
                    "values (?, ?, ?, ?, ?, ?, ?, ");
      buffer.append(DatabaseImpl.getInstance().getSQLCurrentTimestampFunctionCall());
      buffer.append(",");
      buffer.append(DatabaseImpl.getInstance().getSQLCurrentTimestampFunctionCall());
      buffer.append(")");

      return buffer.toString();
   }

   /**
    * Method creates specified LST indexes used for speeding up retrieving data into the list.
    *  
    * @param cntDBConnection - valid connection to database
    * @throws SQLException - problem creating the database schema
    * @throws OSSException - problem creating the database schema
    */
   public void createListIndexes(
      Connection cntDBConnection
   ) throws SQLException, OSSException
   {
      Statement stmQuery = null;
      String strIndexName = null;

      try
      {
         stmQuery = cntDBConnection.createStatement();

         strIndexName = ListDatabaseUtils.getInstance().generateListIndexName(
                           ROLE_TABLE_NAME, "ENABLED");
         if (stmQuery.execute("CREATE INDEX " + strIndexName + " ON " + ROLE_TABLE_NAME + " (DOMAIN_ID, ENABLED)"))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         s_logger.log(Level.FINEST, "Index " + strIndexName + " created.");

         ///////////////////////////////////////////////////////////////////////

         strIndexName = ListDatabaseUtils.getInstance().generateListIndexName(
                           ROLE_TABLE_NAME, "CREATION_DATE");
         if (stmQuery.execute("CREATE INDEX " + strIndexName +
                              " ON " + ROLE_TABLE_NAME + " (DOMAIN_ID, CREATION_DATE)"))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         s_logger.log(Level.FINEST, "Index " + strIndexName + " created.");

         ///////////////////////////////////////////////////////////////////////

         strIndexName = ListDatabaseUtils.getInstance().generateListIndexName(
                           ROLE_TABLE_NAME, "MODIFICATION_DATE");
         if (stmQuery.execute("CREATE INDEX " + strIndexName +
                              " ON " + ROLE_TABLE_NAME + " (DOMAIN_ID, MODIFICATION_DATE)"))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         s_logger.log(Level.FINEST, "Index " + strIndexName + " created.");
      }
      catch (SQLException sqleExc)
      {
         s_logger.log(Level.WARNING, 
                      "Failed to create schema LST indexes for Role data object", sqleExc);
         throw sqleExc;
      }
      finally
      {
         DatabaseUtils.closeStatement(stmQuery);
      }
   }

   /**
    * Get query to select role by ID.
    * 
    * @param columns - columns to retrieve
    * @return String - query
    * @throws OSSException - and error has occurred
    */
   public String getSelectRoleById(
      int[] columns   
   ) throws OSSException
   {
      StringBuffer buffer = new StringBuffer();
      
      buffer.append("select ");
      getColumns(true, columns, null, null, buffer);
      buffer.append(" from " + ROLE_TABLE_NAME + " where " + ROLE_TABLE_NAME + ".ID = ? and DOMAIN_ID = ?");

      return buffer.toString();
   }

   /**
    * Get query to retrieve role by specified ids.
    *  
    * @param columns - columns to retrieve
    * @param strIds - comma separated list of role ids
    * @param iDomainId - id of domain where the selected data should exist
    * @param secData - security data describing what data can be accessed
    * @param lstPrepStmtArgumentBuffer - buffer which will collect arguments 
    *                                    that should be used to populate 
    *                                    prepared statement constructed from 
    *                                    this query
    * @return String - query
    * @throws OSSException - an error has occurred
    */   
   public String getSelectList(
      int[]      columns,
      String     strIds,
      int        iDomainId,
      SimpleRule secData,
      List       lstPrepStmtArgumentBuffer
   ) throws OSSException
   {
      StringBuffer buffer = new StringBuffer();

      buffer.append("select ");
      getColumns(true, columns, null, null, buffer);
      buffer.append(" from " + ROLE_TABLE_NAME + " ");
      buffer.append(ListDatabaseUtils.getInstance().getWhereClause(
                        ROLE_TABLE_NAME, 
                        StringUtils.parseStringToIntArray(strIds, ","), 
                        iDomainId, secData, this, lstPrepStmtArgumentBuffer));
      buffer.append(" order by " + ROLE_TABLE_NAME + ".NAME");

      return buffer.toString();   
   }

   /**
    * Get query that inserts a Role to the database and fetches database 
    * generated values such as the generated id and creation timestamp 
    * 
    * @return String - query for simple insert or stored procedure call
    * @throws OSSException - an error has occurred
    */
   public abstract String getInsertRoleAndFetchGeneratedValues(
   ) throws OSSException;
   
   /**
    * Get query that updates Role in the database and fetches database 
    * generated values such as the updated modification timestamp 
    * 
    * @param bUnmodifiable - flag signaling if role is unmodifiable
    * @param iRoleType - type of the role
    * 
    * @return String - query for simple update or stored procedure call
    * @throws OSSException - an error has occurred
    */
   public abstract String getUpdateRoleAndFetchGeneratedValues(
      boolean bUnmodifiable,
      int     iRoleType
   ) throws OSSException;

   /**
    * Get query that inserts a AccessRight to the database and fetches database 
    * generated values such as the generated id and creation timestamp 
    * 
    * @return String - query for simple insert or stored procedure call
    * @throws OSSException - an error has occurred
    */
   public abstract String getInsertAccessRightAndFetchGeneratedValues(
   ) throws OSSException;
   
   /**
    * Get query to select access rights belonging to specified role.
    * 
    * @return String - query
    */
   public String getSelectAccessRightsByRoleId()
   {
      StringBuffer buffer = new StringBuffer();
   
      buffer.append("select ");
      getAccessRightColumns(false, m_accessRightDescriptor.getAllColumnCodes(), 
                            null, null, buffer);
      buffer.append(" from " + ACCESSRIGHT_TABLE_NAME 
                    + " where ROLE_ID = ? and DOMAIN_ID = ?");

      return buffer.toString();
   }

   /**
    * Get query to select access rights belonging to specified roles.
    * 
    * @param strRoleIds - string of role IDs 
    * @return String - query
    */
   public String getSelectAllForRoles(
      String strRoleIds
   )
   {
      StringBuffer buffer = new StringBuffer();
   
      buffer.append("select ");
      getAccessRightColumns(false, m_accessRightDescriptor.getAllColumnCodes(), 
                            null, null, buffer);
      buffer.append(" from " + ACCESSRIGHT_TABLE_NAME + " where ROLE_ID in ( ");
      buffer.append(strRoleIds);
      buffer.append(") and DOMAIN_ID = ? order by ROLE_ID");

      return buffer.toString();
   }

   /**
    * Get query to select access rights for specified user, access right type
    * and action.
    * 
    * @return String - query
    */   
   public String getSelectAccessRightsForUserTypeAndAction()
   {
      StringBuffer strB = new StringBuffer();
      
      strB.append("select ");
      getAccessRightColumns(true, m_accessRightDescriptor.getAllColumnCodes(), 
                            null, null, strB);
      strB.append(" from " + ACCESSRIGHT_TABLE_NAME + ", " + ROLE_TABLE_NAME + ", " + UserDatabaseSchema.USER_TABLE_NAME + "_ROLE_MAP" +
                  " where " + ACCESSRIGHT_TABLE_NAME + ".ROLE_ID=" + ROLE_TABLE_NAME + ".ID and" +
                  " " + ROLE_TABLE_NAME + ".ID=" + UserDatabaseSchema.USER_TABLE_NAME + "_ROLE_MAP.ROLE_ID and" +
                  " " + UserDatabaseSchema.USER_TABLE_NAME + "_ROLE_MAP.USER_ID = ? and" +
                  " " + ROLE_TABLE_NAME + ".DOMAIN_ID = ? and " + ACCESSRIGHT_TABLE_NAME + ".DOMAIN_ID = ? and" +
                  " " + ACCESSRIGHT_TABLE_NAME + ".DATA_TYPE = ? and " + ACCESSRIGHT_TABLE_NAME + ".ACTION = ? and" +
                  " " + ROLE_TABLE_NAME + ".ENABLED = 1 " +
                  " order by " + ACCESSRIGHT_TABLE_NAME + ".RIGHT_TYPE asc," +
                  " " + ACCESSRIGHT_TABLE_NAME + ".CATEGORY asc, " + ACCESSRIGHT_TABLE_NAME + ".IDENTIFIER asc");
   
      return strB.toString();
   }
   
   /**
    * Get query to select distinct right types for exact data object
    * which is categorized into specific number of categories. The right types will
    * tell us what group of users can access the data objects (e.g. all, the 
    * ones who own it, etc.)
    * 
    * @param validIdentifier - if identifier part will be used
    * @param categoryNumber - number of data object categories to test
    * @return String - query
    */
   public String getSelectDataRightTypes(
      boolean validIdentifier,
      int     categoryNumber
   )
   {
      StringBuffer strB = new StringBuffer();
      // TODO: Performance: Since at this time we support only one right type 
      // (AccessRight.ACCESS_GRANTED) there should be only one so this query should
      // be optimized (something like EXISTS)
      strB.append("select distinct " + ACCESSRIGHT_TABLE_NAME + ".RIGHT_TYPE " +
                  " from " + ACCESSRIGHT_TABLE_NAME + ", " + ROLE_TABLE_NAME + ", " + UserDatabaseSchema.USER_TABLE_NAME + "_ROLE_MAP" +
                  " where " + ACCESSRIGHT_TABLE_NAME + ".ROLE_ID=" + ROLE_TABLE_NAME + ".ID and " +
                  " " + ROLE_TABLE_NAME + ".ID=" + UserDatabaseSchema.USER_TABLE_NAME + "_ROLE_MAP.ROLE_ID and" +
                  " " + UserDatabaseSchema.USER_TABLE_NAME + "_ROLE_MAP.USER_ID=? and " +
                  " " + ROLE_TABLE_NAME + ".DOMAIN_ID=? and " + ACCESSRIGHT_TABLE_NAME + ".DOMAIN_ID=? and" +
                  " " + ACCESSRIGHT_TABLE_NAME + ".DATA_TYPE=? and " + ACCESSRIGHT_TABLE_NAME + ".ACTION=? and" +
                  " " + ROLE_TABLE_NAME + ".ENABLED=1 and (" +
                  // This takes care of the access right asigned to data objects without
                  // specifying any category
                  "(" + ACCESSRIGHT_TABLE_NAME + ".CATEGORY=" + AccessRight.NO_RIGHT_CATEGORY +
                  " and " + ACCESSRIGHT_TABLE_NAME + ".IDENTIFIER=" + AccessRight.NO_RIGHT_IDENTIFIER + ")");
      if (validIdentifier)
      {
         // This is for access rights assigned to particular data object when
         // the identifier is its id
         strB.append("or (" + ACCESSRIGHT_TABLE_NAME + ".CATEGORY=" + AccessRight.NO_RIGHT_CATEGORY
                     + " and " + ACCESSRIGHT_TABLE_NAME + ".IDENTIFIER=?)");
      }
            
      for (int iCount = 0; iCount < categoryNumber; iCount++)
      {
         strB.append(" or (" + ACCESSRIGHT_TABLE_NAME + ".CATEGORY=? and " + ACCESSRIGHT_TABLE_NAME + ".IDENTIFIER=?)");
      }
      
      strB.append(")");
      
      return strB.toString();
   }

   /**
    * Method for deleting personal roles
    * 
    * @param dbConnection - database connection
    * @param arrUserIds - array of user IDs that will be deleted
    * @throws OSSException - error occurred during delete
    * @throws SQLException - error occurred during delete
    * @return int - number of deleted users
    */
   public int deletePersonalRoles(
      Connection dbConnection,
      int[] arrUserIds
   ) throws OSSException, SQLException
   {
      PreparedStatement pstmDelete = null;
      int iDeleted = 0;

      try
      {
         pstmDelete = dbConnection.prepareStatement(getDeletePersonalRoles(arrUserIds));
         iDeleted = pstmDelete.executeUpdate();
      }
      finally
      {
         DatabaseUtils.closeStatement(pstmDelete);
      }
      return iDeleted;
   }

   /**
    * Method for deleting role
    * 
    * @param dbConnection - database connection
    * @param arrIds - role ids
    * @param iDomainId - id of domain where the selected data should exist
    * @param secData - security data describing what data can be accessed
    * @param iRoleType - type of the role
    * @throws OSSException - error occurred during delete
    * @throws SQLException - error occurred during delete
    * @return int - number of deleted roles
    */
   public int deleteRoles(
      Connection dbConnection,
      int[]      arrIds,
      int        iDomainId,
      SimpleRule secData,
      int        iRoleType
   ) throws OSSException, 
            SQLException
   {
      PreparedStatement pstmDelete = null;
      List              lstPrepStmtArguments = new ArrayList();
      int               iDeleted = 0;

      try
      {
         String strQuery;
         
         strQuery = getDeleteRoles(arrIds, iDomainId, secData, iRoleType,
                                   lstPrepStmtArguments);
         pstmDelete = dbConnection.prepareStatement(strQuery);         
         DatabaseUtils.populatePreparedStatementPlaceholders(
                          pstmDelete, lstPrepStmtArguments);
         iDeleted = pstmDelete.executeUpdate();
      }
      finally
      {
         DatabaseUtils.closeStatement(pstmDelete);
      }
      return iDeleted;
   }

   /**
    * Get query to delete roles
    * 
    * @param arrIds - role ids
    * @param iDomainId - id of domain where the selected data should exist
    * @param secData - security data describing what data can be accessed
    * @param iDeleteFlag - flag to determine what type of delete will be used
    *                      one of RoleConstants.COUNT_XXX values
    * @param lstPrepStmtArgumentBuffer - buffer which will collect arguments 
    *                                    that should be used to populate 
    *                                    prepared statement constructed from 
    *                                    this query
    * @return String - query
    * @throws OSSException - an error has occurred
    */
   public String getDeleteRoles(
      int[]      arrIds,
      int        iDomainId,
      SimpleRule secData,
      int        iDeleteFlag,
      List       lstPrepStmtArgumentBuffer
   ) throws OSSException
   {     
      StringBuffer buffer = new StringBuffer();
      
      buffer.append("delete from " + ROLE_TABLE_NAME + " ");
      buffer.append(ListDatabaseUtils.getInstance().getWhereClause(
                       ROLE_TABLE_NAME, arrIds, iDomainId, secData, this,
                       lstPrepStmtArgumentBuffer));
      buffer.append(" and " + ROLE_TABLE_NAME + ".USER_ID is null");
      switch (iDeleteFlag) 
      {
         case (RoleConstants.COUNT_WITH_MODIFIABLE_ONLY) :
         {
            buffer.append(" and " + ROLE_TABLE_NAME + ".UNMODIFIABLE = 0");
            break;
         }
         case (RoleConstants.COUNT_WITH_UNMODIFIABLE_ONLY) :
         {
            buffer.append(" and " + ROLE_TABLE_NAME + ".UNMODIFIABLE = 1");
            break;
         }
         case RoleConstants.COUNT_WITH_ALL :
         {
            break;
         }
         default :
         {
            assert false : "Undefined type of role delete.";
            break;
         }
      }
      
      return buffer.toString();
   }

   /**
    * Get query to delete personal roles for specified user ird.
    * 
    * @param arrUserIds - user ids for which to delete roles
    * @return String - query
    * @throws OSSException - an error has occurred
    */
   public String getDeletePersonalRoles(
      int[] arrUserIds
   ) throws OSSException
   {
      StringBuffer buffer = new StringBuffer();
      
      buffer.append("delete from " + ROLE_TABLE_NAME + " where " + ROLE_TABLE_NAME + ".USER_ID in (");
      buffer.append(StringUtils.parseIntArrayToString(arrUserIds, ","));
      buffer.append(") and " + ROLE_TABLE_NAME + ".USER_ID != ");
      buffer.append(DataObject.NEW_ID);
      
      return buffer.toString();
   }

   /**
    * Get query to update Enabled flag for specified roles
    * 
    * @param arrIds - role Ids
    * @param bNewEnableValue - new enabled value
    * @param iDomainId - id of domain where the selected data should exist
    * @param secData - security data describing what data can be accessed
    * @param lstPrepStmtArgumentBuffer - buffer which will collect arguments 
    *                                    that should be used to populate 
    *                                    prepared statement constructed from 
    *                                    this query
    * @return String - query
    * @throws OSSException - an error has occurred
    */
   public String getUpdateEnable(
      int[]      arrIds,
      boolean    bNewEnableValue,
      int        iDomainId,
      SimpleRule secData,
      List       lstPrepStmtArgumentBuffer
   ) throws OSSException
   {      
      StringBuffer buffer = new StringBuffer();
      
      lstPrepStmtArgumentBuffer.add(bNewEnableValue ? GlobalConstants.INTEGER_1 
                                                    : GlobalConstants.INTEGER_0);

      buffer.append("update " + ROLE_TABLE_NAME + " set ENABLED = ?, MODIFICATION_DATE = ");
      buffer.append(DatabaseImpl.getInstance().getSQLCurrentTimestampFunctionCall());
      buffer.append(ListDatabaseUtils.getInstance().getWhereClause(
                       ROLE_TABLE_NAME, arrIds, iDomainId, secData, this, 
                       lstPrepStmtArgumentBuffer));
      buffer.append(" and UNMODIFIABLE = 0 and USER_ID is null");
      
      return buffer.toString();
   }   
   
   /**
    * Get query to select role ids, which still exists and can be accessed based 
    * on security data.  
    * 
    * @param strIds - selected ids
    * @param iDomainId - id of domain where the selected data should exist
    * @param secData - security data
    * @param lstPrepStmtArgumentBuffer - buffer which will collect arguments 
    *                                    that should be used to populate 
    *                                    prepared statement constructed from 
    *                                    this query
    * @return String - query 
    * @throws OSSException - error during select
    */
   public String getSelectActualRoleIds(
      String     strIds,
      int        iDomainId,
      SimpleRule secData,
      List       lstPrepStmtArgumentBuffer
   ) throws OSSException
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append("select ID from " + ROLE_TABLE_NAME + " ");
      buffer.append(ListDatabaseUtils.getInstance().getWhereClause(
                        ROLE_TABLE_NAME, 
                        StringUtils.parseStringToIntArray(strIds, ","), 
                        iDomainId, secData, this, lstPrepStmtArgumentBuffer));

      return buffer.toString();
   }

   /**
    * Get query to retrieve count of roles which still exists.
    * 
    * @param strIds - comma separated role IDs
    * @return String - query 
    * @throws OSSException - an error has occurred
    */
   public String getSelectActualRoleCount(
      String strIds
   ) throws OSSException 
   {
      StringBuffer buffer = new StringBuffer();
      
      buffer.append("select ");
      buffer.append(DatabaseImpl.getInstance().getSQLCountFunctionCall());
      buffer.append(" from " + ROLE_TABLE_NAME + " where ID in (");
      buffer.append(strIds);
      buffer.append(") and DOMAIN_ID = ?");
      
      return buffer.toString();
   }

   /**
    * Get query to delete particular role.
    * 
    * @return String - query
    */
   public String getDeleteRoleById(
   )
   {
      return "delete from " + ROLE_TABLE_NAME + " where ID = ? and DOMAIN_ID = ?";
   }

   /**
    * Get query to delete all access rights that belongs to a role role
    * 
    * @return String - query
    * @throws OSSException - an error has occurred
    */
   public String getDeleteAllAccessRightsForRoleId(
   ) throws OSSException
   {
      return "delete from " + ACCESSRIGHT_TABLE_NAME + " where ROLE_ID = ? and DOMAIN_ID = ?";
   }   

   /**
    * Get query to delete specified access rights
    * 
    * @param arrIds - comma separated list of access rights ids
    * @return String - query
    * @throws OSSException - an error has occurred
    */
   public String getDeleteAccessRights(
      String arrIds
   ) throws OSSException
   {
      StringBuffer buffer = new StringBuffer();

      buffer.append("delete from " + ACCESSRIGHT_TABLE_NAME + " where ID in (");
      buffer.append(arrIds);
      buffer.append(") and ROLE_ID = ? and DOMAIN_ID = ?");
      
      return buffer.toString();  
   }   

   /**
    * Get query to select roles assigned to a user.
    * 
    * @param columns - columns to retrieve
    * @param iUserId - user ID
    * @param iDomainId - id of domain where the selected data should exist
    * @param secData - security data describing what data can be accessed
    * @param lstPrepStmtArgumentBuffer - buffer which will collect arguments 
    *                                    that should be used to populate 
    *                                    prepared statement constructed from 
    *                                    this query
    * @return String - query
    * @throws OSSException - an error has occurred
    */   
   public String geSelectRolesForUserId(
      int[]      columns,
      int        iUserId,
      int        iDomainId,
      SimpleRule secData,
      List       lstPrepStmtArgumentBuffer
   ) throws OSSException
   {
      StringBuffer buffer = new StringBuffer();

      buffer.append("select ");
      getColumns(true, columns, null, null, buffer);
      buffer.append(" from " + ROLE_TABLE_NAME + ", " + UserDatabaseSchema.USER_TABLE_NAME + "_ROLE_MAP, " + 
                    UserDatabaseSchema.USER_TABLE_NAME + " ");
      buffer.append(ListDatabaseUtils.getInstance().getWhereClause(
                       ROLE_TABLE_NAME, null, iDomainId, secData, this,
                       lstPrepStmtArgumentBuffer));
      buffer.append(" and " + ROLE_TABLE_NAME + ".ID = " 
                    + UserDatabaseSchema.USER_TABLE_NAME + "_ROLE_MAP.ROLE_ID" 
                    + " and " + UserDatabaseSchema.USER_TABLE_NAME + "_ROLE_MAP.USER_ID = " 
                    + UserDatabaseSchema.USER_TABLE_NAME + ".ID" 
                    + " and " + UserDatabaseSchema.USER_TABLE_NAME + ".ID = ?"
                    + " and " + UserDatabaseSchema.USER_TABLE_NAME + ".DOMAIN_ID = ?" 
                    + " order by " + ROLE_TABLE_NAME + ".NAME");

      lstPrepStmtArgumentBuffer.add(new Integer(iUserId));
      lstPrepStmtArgumentBuffer.add(new Integer(iDomainId));
      
      return buffer.toString();   
   }

   /**
    * Get query to select default roles assigned to a domain
    * 
    * @param columns - columns to retrieve
    * @param iDomainId - id of domain where the selected data should exist
    * @param secData - security data describing what data can be accessed
    * @param lstPrepStmtArgumentBuffer - buffer which will collect arguments 
    *                                    that should be used to populate 
    *                                    prepared statement constructed from 
    *                                    this query
    * @return String - query
    * @throws OSSException - an error has occurred
    */   
   public String geSelectRolesForDomainId(
      int[]      columns,
      int        iDomainId,
      SimpleRule secData,
      List       lstPrepStmtArgumentBuffer
   ) throws OSSException
   {
      StringBuffer buffer = new StringBuffer();

      buffer.append("select ");
      getColumns(true, columns, null, null, buffer);
      buffer.append(" from " + ROLE_TABLE_NAME + ", " + DomainDatabaseSchema.DOMAIN_TABLE_NAME + "_ROLE_MAP, " + DomainDatabaseSchema.DOMAIN_TABLE_NAME + " ");
      buffer.append(ListDatabaseUtils.getInstance().getWhereClause(
                       ROLE_TABLE_NAME, null, iDomainId, secData, this,
                       lstPrepStmtArgumentBuffer));
      buffer.append(" and " + ROLE_TABLE_NAME + ".ID = " 
                     + DomainDatabaseSchema.DOMAIN_TABLE_NAME + "_ROLE_MAP.ROLE_ID" 
                     + " and " + DomainDatabaseSchema.DOMAIN_TABLE_NAME 
                     + "_ROLE_MAP.DOMAIN_ID = " + DomainDatabaseSchema.DOMAIN_TABLE_NAME + ".ID" 
                     + " and " + ROLE_TABLE_NAME + ".DOMAIN_ID = " 
                     + DomainDatabaseSchema.DOMAIN_TABLE_NAME + ".ID" 
                     + " order by " + ROLE_TABLE_NAME + ".NAME");

      return buffer.toString();   
   }

   /**
    * Get query to select all roles in specified domain
    * 
    * @param columns - columns to retrieve
    * @param iDomainId - id of domain where the selected data should exist
    * @param secData - security data describing what data can be accessed
    * @param lstPrepStmtArgumentBuffer - buffer which will collect arguments 
    *                                    that should be used to populate 
    *                                    prepared statement constructed from 
    *                                    this query
    * @return String - query
    * @throws OSSException - an error has occurred
    */   
   public String geSelectAllRolesInDomain(
      int[]      columns,
      int        iDomainId,
      SimpleRule secData,
      List       lstPrepStmtArgumentBuffer
   ) throws OSSException
   {
      StringBuffer buffer = new StringBuffer();

      buffer.append("select ");
      getColumns(true, columns, null, null, buffer);
      buffer.append(" from " + ROLE_TABLE_NAME + " ");
      buffer.append(ListDatabaseUtils.getInstance().getWhereClause(
                       ROLE_TABLE_NAME, null, iDomainId, secData, this,
                       lstPrepStmtArgumentBuffer));

      return buffer.toString();   
   }

   /**
    * Get query to select default selfregistered user role 
    * 
    * @param columns - columns to retrieve
    * @return String - query
    * @throws OSSException - an error has occurred
    */   
   public String geSelectSelfregisteredRole(
      int[]      columns
   ) throws OSSException
   {
      StringBuffer buffer = new StringBuffer();

      buffer.append("select ");
      getColumns(true, columns, null, null, buffer);
      buffer.append(" from " + ROLE_TABLE_NAME + " where " + ROLE_TABLE_NAME + ".NAME = '");
      buffer.append(RoleUtils.SELFREG_ROLE_NAME);
      buffer.append("' and DOMAIN_ID = ?");

      return buffer.toString();   
   }

   /**
    * Get query to select personal role for user.
    * 
    * @param columns - columns to retrieve
    * @return String - query 
    * @throws OSSException - an error has occurred
    */
   public String getPersonalRoleByUserId(
      int[] columns
   ) throws OSSException
   {
      StringBuffer buffer = new StringBuffer();

      buffer.append("select ");
      getColumns(true, columns, null, null, buffer);
      buffer.append(" from " + ROLE_TABLE_NAME + " where " + ROLE_TABLE_NAME + ".USER_ID = ?" +
                    " and " + ROLE_TABLE_NAME + ".DOMAIN_ID = ? order by " + ROLE_TABLE_NAME + ".NAME");

      return buffer.toString();   
   }

   /**
    * Get personal roles for specified ids.
    *  
    * @param colIds - collection of role IDs
    * @return String - query
    */
   public String getPersonalRoleIds(
      Collection colIds
   )
   {
      if (GlobalConstants.ERROR_CHECKING)
      {
         assert (colIds != null) && (!colIds.isEmpty()) 
                : "Role IDs have to be specified.";
      }   

      StringBuffer buffer = new StringBuffer();
      buffer.append("select ID from " + ROLE_TABLE_NAME + " where ID in (");
      buffer.append(StringUtils.parseCollectionToString(colIds, ","));
      buffer.append(") and " + ROLE_TABLE_NAME + ".DOMAIN_ID = ? and USER_ID is not NULL");
      
      return buffer.toString();
   }

   /**
    * Get query to remove assigned roles from user.
    * 
    * @param strIds - comma separated list of role ids 
    * @return String - query
    * @throws OSSException - and error has occurred
    */ 
   public String getRemoveRolesFromUser(
      String strIds
   ) throws OSSException
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append("delete from " + UserDatabaseSchema.USER_TABLE_NAME + "_ROLE_MAP where USER_ID=? and ROLE_ID in (");
      buffer.append(strIds);
      buffer.append(") ");
      
      return buffer.toString();
   }

   /**
    * Get query to remove assigned default roles from domain.
    * 
    * @param strIds - comma separated list of role ids 
    * @return String - query
    * @throws OSSException - and error has occurred
    */ 
   public String getRemoveRolesFromDomain(
      String strIds
   ) throws OSSException
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append("delete from " + DomainDatabaseSchema.DOMAIN_TABLE_NAME + "_ROLE_MAP where DOMAIN_ID=? and ROLE_ID in (");
      buffer.append(strIds);
      buffer.append(") ");
      
      return buffer.toString();
   }

   /**
    * Get query to remove assigned roles from all specified 
    * users - within the user-role map table.
    * 
    * @param strUserIds - string of user ids separated by ',' 
    * @param bRemoveExceptSpecified - flag signaling if there will be 
    *        removed all records except specified or not
    *        true - remove all user roles except specified
    *        false - remove specified user roles
    * @return String - query
    * @throws OSSException - and error has occurred
    */ 
   public String getRemoveMappedRolesFromUsers(
      String strUserIds,
      boolean bRemoveExceptSpecified
   ) throws OSSException
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append("delete from " + UserDatabaseSchema.USER_TABLE_NAME + "_ROLE_MAP where USER_ID ");
      buffer.append(bRemoveExceptSpecified ? " not " : "");
      buffer.append(" in (");
      buffer.append(strUserIds);
      buffer.append(") ");
      
      return buffer.toString();
   }

   /**
    * Get query to remove assigned default roles from all specified 
    * domains - within the domain-role map table.
    * 
    * @param strDomainIds - string of domain ids separated by ',' 
    * @param bRemoveExceptSpecified - flag signaling if there will be 
    *        removed all records except specified or not
    *        true - remove all user roles except specified
    *        false - remove specified user roles
    * @return String - query
    * @throws OSSException - and error has occurred
    */ 
   public String getRemoveMappedRolesFromDomains(
      String strDomainIds,
      boolean bRemoveExceptSpecified
   ) throws OSSException
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append("delete from " + DomainDatabaseSchema.DOMAIN_TABLE_NAME + "_ROLE_MAP where DOMAIN_ID ");
      buffer.append(bRemoveExceptSpecified ? " not " : "");
      buffer.append(" in (");
      buffer.append(strDomainIds);
      buffer.append(") ");
      
      return buffer.toString();
   }

   /**
    * Get query to remove assigned roles from all specified users - within the role table.
    * 
    * @param strUserIds - string of user ids separated by ','
    * @param bRemoveExceptSpecified - flag signaling if there will be 
    *        removed all records except specified or not
    *        true - remove all user roles except specified
    *        false - remove specified user roles
    * @return String - query
    * @throws OSSException - and error has occurred
    */ 
   public String getRemoveRolesFromUsers(
      String strUserIds,
      boolean bRemoveExceptSpecified
   ) throws OSSException
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append("delete from " + ROLE_TABLE_NAME + " where USER_ID ");
      buffer.append(bRemoveExceptSpecified ? " not " : "");
      buffer.append(" in (");
      buffer.append(strUserIds);
      buffer.append(") ");
      
      return buffer.toString();
   }

   /**
    * Get query to remove assigned default roles from all specified 
    * domains - within the role table.
    * 
    * @param strDomainIds - string of domain ids separated by ','
    * @param bRemoveExceptSpecified - flag signaling if there will be 
    *        removed all records except specified or not
    *        true - remove all user roles except specified
    *        false - remove specified user roles
    * @return String - query
    * @throws OSSException - and error has occurred
    */ 
   public String getRemoveRolesFromDomains(
      String strDomainIds,
      boolean bRemoveExceptSpecified
   ) throws OSSException
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append("delete from " + ROLE_TABLE_NAME + " where DOMAIN_ID ");
      buffer.append(bRemoveExceptSpecified ? " not " : "");
      buffer.append(" in (");
      buffer.append(strDomainIds);
      buffer.append(") ");
      
      return buffer.toString();
   }

   /**
    * Get query to assign role to user.
    * 
    * @return String - query
    */ 
   public String getAssignRoleToUser(
   )
   {
      return "insert into " + UserDatabaseSchema.USER_TABLE_NAME + "_ROLE_MAP (USER_ID,ROLE_ID) values (?,?)";
   }

   /**
    * Get query to assign role to domain.
    * 
    * @return String - query
    */ 
   public String getAssignRoleToDomain(
   )
   {
      return "insert into " + DomainDatabaseSchema.DOMAIN_TABLE_NAME + "_ROLE_MAP (DOMAIN_ID,ROLE_ID) values (?,?)";
   }

   /**
    * Get query to select access right by id.
    * 
    * @param columns - columns to retrieve
    * @return String - query
    * @throws OSSException - an error has occurred
    */
   public String getSelectAccessRightById(
      int[] columns
   ) throws OSSException
   {
      StringBuffer buffer = new StringBuffer();

      buffer.append("select ");
      getAccessRightColumns(false, columns, null, null, buffer);
      buffer.append(" from " + ACCESSRIGHT_TABLE_NAME + " where ID = ? and DOMAIN_ID = ?");
 
      return buffer.toString();
   }

   /**
    * {@inheritDoc}
    */
   public StringBuffer getColumns(
      final boolean  bSpecific,   
      final int[]    columns,
      final Object[] prefixes, 
      final Object[] postfixes, 
      StringBuffer   buffer      
   ) throws OSSException
   {
      if (buffer == null)
      {
         buffer = new StringBuffer();
      }        
      for (int iIndex = 0; iIndex < columns.length; iIndex++)
      {
         if (iIndex > 0)
         {
            buffer.append(",");
         }

         if ((prefixes != null) && (prefixes.length > 0) && (prefixes[iIndex] != null))
         {
            buffer.append(prefixes[iIndex]);            
         }

         if (bSpecific)
         {
            buffer.append(ROLE_TABLE_NAME + ".");
         }
         
         switch(columns[iIndex])
         {
            case (RoleDataDescriptor.COL_ROLE_ID):
            {
               buffer.append("ID");
               break;
            }
            case (RoleDataDescriptor.COL_ROLE_DOMAIN_ID):
            {
               buffer.append("DOMAIN_ID");
               break;
            }
            case (RoleDataDescriptor.COL_ROLE_NAME):
            {
               buffer.append("NAME");
               break;
            }
            case (RoleDataDescriptor.COL_ROLE_DESCRIPTION):
            {
               buffer.append("DESCRIPTION");
               break;
            }
            case (RoleDataDescriptor.COL_ROLE_ENABLED):
            {
               buffer.append("ENABLED");
               break;
            }
            case (RoleDataDescriptor.COL_ROLE_USER_ID) :
            {
               buffer.append("USER_ID");
               break;
            }
            case (RoleDataDescriptor.COL_ROLE_UNMODIFIABLE) :
            {
               buffer.append("UNMODIFIABLE");
               break;
            }
            case (RoleDataDescriptor.COL_ROLE_CREATION_DATE):
            {
               buffer.append("CREATION_DATE");
               break;
            }
            case (RoleDataDescriptor.COL_ROLE_MODIFICATION_DATE):
            {
               buffer.append("MODIFICATION_DATE");
               break;
            }
            default:
            {
               assert false : "Unknown column ID " + columns[iIndex]; 
            }
         }

         if ((postfixes != null) && (postfixes.length > 0) && (postfixes[iIndex] != null))
         {
            buffer.append(postfixes[iIndex]);            
         }
      }
   
      return buffer;
   }

   /**
    * This method can convert any attribute regardless if it is in the main table
    * or in join table into database column name.
    * 
    * @param specific - if true - table_name.table_column, if false only table_column 
    * @param columns - specific array of columns codes
    * @param prefixes - specific array of prefixes to prepend before each column,
    *                   each prefix will be mapped with column and the same index
    *                   they will be directly prepended or appended to the column
    *                   name and therefore they need to contain any extra space if
    *                   necessary, 
    *                   specify null, if no prefixes should be prepended
    * @param postfixes - specific array of postfixes to prepend before each column,
    *                    each prefix will be mapped with column and the same index
    *                    they will be directly prepended or appended to the column
    *                    name and therefore they need to contain any extra space if
    *                    necessary, 
    *                    specify null, if no prefixes should be prepended
    * @param buffer - buffer to use for column codes construction, it may
    *                 already contain some value
    * @return StringBuffer - specific columns of table divided by comma
    *                        with specified prefixes and postfixes
    */
   public StringBuffer getAccessRightColumns(
      boolean        specific,   
      int[]          columns,
      final Object[] prefixes, 
      final Object[] postfixes, 
      StringBuffer   buffer      
   )
   {
      if (buffer == null)
      {
         buffer = new StringBuffer();
      }        
      for (int iIndex = 0; iIndex < columns.length; iIndex++)
      {
         if (iIndex > 0)
         {
            buffer.append(",");
         }

         if ((prefixes != null) && (prefixes.length > 0) && (prefixes[iIndex] != null))
         {
            buffer.append(prefixes[iIndex]);            
         }

         if (specific)
         {
            buffer.append(ACCESSRIGHT_TABLE_NAME + ".");
         }
         switch(columns[iIndex])
         {
            case (AccessRightDataDescriptor.COL_ACCESSRIGHT_ID):
            {
               buffer.append("ID");
               break;
            }
            case (AccessRightDataDescriptor.COL_ACCESSRIGHT_ROLE_ID):
            {
               buffer.append("ROLE_ID");
               break;
            }
            case (AccessRightDataDescriptor.COL_ACCESSRIGHT_DOMAIN_ID):
            {
               buffer.append("DOMAIN_ID");
               break;
            }
            case (AccessRightDataDescriptor.COL_ACCESSRIGHT_ACTION):
            {
               buffer.append("ACTION");
               break;
            }
            case (AccessRightDataDescriptor.COL_ACCESSRIGHT_DATA_TYPE):
            {
               buffer.append("DATA_TYPE");
               break;
            }
            case (AccessRightDataDescriptor.COL_ACCESSRIGHT_RIGHT_TYPE):
            {
               buffer.append("RIGHT_TYPE");
               break;
            }
            case (AccessRightDataDescriptor.COL_ACCESSRIGHT_CATEGORY):
            {
               buffer.append("CATEGORY");
               break;
            }
            case (AccessRightDataDescriptor.COL_ACCESSRIGHT_IDENTIFIER):
            {
               buffer.append("IDENTIFIER");
               break;
            }
            case (AccessRightDataDescriptor.COL_ACCESSRIGHT_CREATION_DATE):
            {
               buffer.append("CREATION_DATE");
               break;
            }
            case (AccessRightDataDescriptor.COL_ACCESSRIGHT_MODIFICATION_DATE):
            {
               buffer.append("MODIFICATION_DATE");
               break;
            }
            default:
            {
               assert false : "Unknown column ID " + columns[iIndex];
            }
         }

         if ((postfixes != null) && (postfixes.length > 0) && (postfixes[iIndex] != null))
         {
            buffer.append(postfixes[iIndex]);            
         }
      }
   
      return buffer;
   }

   /**
    * {@inheritDoc}
    */
   public void handleSQLException(
      SQLException exc, 
      Connection   dbConnection, 
      int          iOperationType, 
      int          iDataType,
      String       strDisplayableViewName,
      Object       data
   ) throws OSSException
   {
      // TODO: For Miro: Use strDisplayableViewName rather than hardcoded name
      OSSInvalidDataException ideException = null;
      
      switch(iOperationType)
      {
         case DBOP_INSERT:
         {
            if (iDataType == m_roleDescriptor.getDataType())
            {
               if (exc.getMessage().toUpperCase().indexOf(
                     ROLE_TABLE_NAME + "_FK") > -1)
               {
                  throw new OSSInvalidContextException(
                        "Domain to create role in does not exist.", 
                        exc);
               }
               
               if (exc.getMessage().toUpperCase().indexOf(
                     ROLE_TABLE_NAME + "_USER_FK") > -1)
               {
                  throw new OSSInvalidDataException(
                        "User to create role for does not exist.", 
                        exc);
               }

               if ((exc.getMessage().toUpperCase().indexOf(
                      ROLE_TABLE_NAME + "_UQ") > -1)
                  // MySQL handles role name unique constraint exception as 
                  // 'KEY 2'
                  || ((exc.getMessage().toUpperCase()).endsWith("KEY 2"))
                  // IBM DB2 handles role name unique constraint exception as 
                  // "SQLERRMC: 2"
                  || (exc.getMessage().toUpperCase().indexOf("SQLERRMC: 2") > -1))
               {
                  ideException = OSSInvalidDataException.addException(ideException,
                     RoleDataDescriptor.COL_ROLE_NAME,
                     // If the user id is specified then the failure was caused
                     // while renaming user since when this is personal role 
                     // there should not be a way how to just rename role
                     data == null 
                        ? "Role name has to be unique." 
                        : (((Role)data).getUserId() > DataObject.NEW_ID  
                          ? "There already exists role \"" + ((Role)data).getName() 
                            + "\" therefore you cannot create user with the same name."
                          : (((Role)data).getUserId() 
                            ==  RoleConstants.ERROR_RENAMING_CATEGORY_WHILE_ROLE_NAME_HAS_TO_BE_UNIQUE 
                            ? "There already exists role \"" + ((Role)data).getName() + 
                              "\" therefore you cannot create category with the same name." 
                            : "Role name has to be unique.")),
                     exc
                  );
               }
            }
            else if (iDataType == m_accessRightDescriptor.getDataType())
            {
               if (exc.getMessage().toUpperCase().indexOf(
                     ACCESSRIGHT_TABLE_NAME + "_DOMAIN_FK") > -1)
               {
                  throw new OSSInvalidContextException(
                        "Domain to create access right in does not exist.", 
                        exc);
               }
               
               if (exc.getMessage().toUpperCase().indexOf(
                     ACCESSRIGHT_TABLE_NAME + "_ROLE_FK") > -1)
               {
                  throw new OSSInvalidDataException(
                        "Role to create access right for does not exist.", 
                        exc);
               }
            }
            break;
         }
         case DBOP_UPDATE:
         {
            if (iDataType == m_roleDescriptor.getDataType())
            {
               if ((exc.getMessage().toUpperCase().indexOf(ROLE_TABLE_NAME + "_UQ") > -1)
                  // MySQL handles role name unique constraint exception as 'KEY 2'
                  || ((exc.getMessage().toUpperCase()).endsWith("KEY 2"))
                  // IBM DB2 handles role name unique constraint exception as 
                  // "SQLERRMC: 2"
                  || (exc.getMessage().toUpperCase().indexOf("SQLERRMC: 2") > -1))
               {
                  ideException = OSSInvalidDataException.addException(ideException, 
                     RoleDataDescriptor.COL_ROLE_NAME, 
                     ((Role)data).getUserId() != DataObject.NEW_ID  
                        ? "There already exists role \"" + ((Role)data).getName() 
                          + "\" therefore you cannot save user with the same name."
                        : "Role name has to be unique.",
                     exc
                  );
               }
            }
            else if (iDataType == m_accessRightDescriptor.getDataType())
            {
               // No special error checking
            }
            break;
         }
         default:
         {
            // Some operations do not need any specific handling
         }
      }
      if (ideException != null)
      {
         // If a specific exception was created throw it back to client
         throw ideException;
      }
      else
      {
         // No special handling was needed so execute the default exception 
         // handling
         super.handleSQLException(exc, dbConnection, iOperationType, iDataType,
                                  strDisplayableViewName, data);
      }   
   }
   
   // Helper methods ///////////////////////////////////////////////////////////
   
   /**
    * Construct map of table names belonging to this schema that store data 
    * objects that can be modified.
    * 
    * @return Map - map where key is the data type and value is the table name
    *               that stores modifiable objects of this data type
    * @throws OSSException - an error has occurred
    */
   protected static Map createModifiableTableNameMap(
   ) throws OSSException
   {
      // Create map that stores table names with data objects that can be modified. 
      Map                       mpTableNames;
      RoleDataDescriptor        roleDescriptor;
      AccessRightDataDescriptor accessRightDescriptor;
      
      roleDescriptor = (RoleDataDescriptor)DataDescriptorManager
                          .getInstance(RoleDataDescriptor.class);
      accessRightDescriptor = (AccessRightDataDescriptor)DataDescriptorManager
                                 .getInstance(AccessRightDataDescriptor.class);
      
      mpTableNames = new HashMap();
      mpTableNames.put(roleDescriptor.getDataTypeAsObject(), 
                       ROLE_TABLE_NAME);
      mpTableNames.put(accessRightDescriptor.getDataTypeAsObject(), 
                       ACCESSRIGHT_TABLE_NAME);
      
      return mpTableNames;
   }
}
