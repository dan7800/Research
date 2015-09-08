/*
 * Copyright (c) 2003 - 2007 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: TestUserDatabaseFactoryUtils.java,v 1.16 2009/04/22 06:29:42 bastafidli Exp $
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

package org.opensubsystems.security.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.opensubsystems.core.data.DataObject;
import org.opensubsystems.core.error.OSSDataDeleteException;
import org.opensubsystems.core.error.OSSDataNotFoundException;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.error.OSSInconsistentDataException;
import org.opensubsystems.core.error.OSSMultiException;
import org.opensubsystems.core.persist.db.DatabaseConnectionFactory;
import org.opensubsystems.core.persist.db.DatabaseSchemaManager;
import org.opensubsystems.core.persist.db.DatabaseTransactionFactory;
import org.opensubsystems.core.persist.db.impl.DatabaseConnectionFactoryImpl;
import org.opensubsystems.core.persist.db.impl.DatabaseTransactionFactoryImpl;
import org.opensubsystems.core.util.CallContext;
import org.opensubsystems.core.util.GlobalConstants;
import org.opensubsystems.core.util.StringUtils;
import org.opensubsystems.core.util.jdbc.DatabaseUtils;
import org.opensubsystems.security.persist.db.RoleDatabaseSchema;
import org.opensubsystems.security.persist.db.SessionDatabaseSchema;
import org.opensubsystems.security.persist.db.UserDatabaseSchema;

/**
 * This class is used as utilities for jUnit tests to provide manual cascade 
 * deleting capability.  
 *
 * @version $Id: TestUserDatabaseFactoryUtils.java,v 1.16 2009/04/22 06:29:42 bastafidli Exp $
 * @author Julo Legeny
 * @code.reviewer 
 * @code.reviewer TODO: Review this code
 */
public class TestUserDatabaseFactoryUtils 
{
   // Public methods //////////////////////////////////////////////////////////

   /**
    * Method for deleting user and all his sessions, roles and access rights.
    * It is necessary to be include all security db tables. 
    *
    * @param iId - ID of the user
    * @throws OSSException - error occurred during deleting
    */
   public void deleteUserCascadeManual(
      int iId
   ) throws OSSException
   {
      DatabaseConnectionFactory connectionFactory;
      Connection                cntConnection = null;
   
      connectionFactory = DatabaseConnectionFactoryImpl.getInstance();
      try
      {
         // Request autocommit false since we are modifying database
         cntConnection = connectionFactory.requestConnection(false);
         deleteUserCascadeManual(cntConnection, iId);
      }
      finally
      {
         connectionFactory.returnConnection(cntConnection);         
      }
   }
   
   /**
    * Method for deleting user and all his sessions, roles and access rights.
    * It is necessary to be include all security db tables. 
    * 
    * @param cntConnection - database connection to use
    * @param iId - ID of the user
    * @throws OSSException - error occurred during deleting
    */
   public void deleteUserCascadeManual(
      Connection cntConnection,
      int        iId
   ) throws OSSException
   {
      if (GlobalConstants.ERROR_CHECKING)
      {
         assert iId != DataObject.NEW_ID 
                : "Cannot delete data, which wasn't created yet.";
      }

      PreparedStatement          pstmtSelect = null;
      PreparedStatement          pstmDelete = null;
      ResultSet                  rsResults = null;      
      UserDatabaseSchema         userSchema;
      String                     strQuery = "";
      StringBuffer               buffer = new StringBuffer();
      List                       lstIntSessionIDs = new ArrayList();
      List                       lstRoleIDs = new ArrayList();
      int                        iDeleted;
      DatabaseTransactionFactory transactionFactory;

      transactionFactory = DatabaseTransactionFactoryImpl.getInstance();
      userSchema = ((UserDatabaseSchema)DatabaseSchemaManager.getInstance(
                      UserDatabaseSchema.class));
      
      try
      {
         lstRoleIDs.add(new Integer(-1));
         // a. ---------------------------------------------------------------------------
         // retrieve all role IDs belonging to the particular user
         // ------------------------------------------------------------------------------
         strQuery = "select ID from " + RoleDatabaseSchema.ROLE_TABLE_NAME + " where USER_ID = ? and DOMAIN_ID = ?";

         try
         {
            pstmtSelect = cntConnection.prepareStatement(strQuery);
            pstmtSelect.setInt(1, iId);
            pstmtSelect.setInt(2, CallContext.getInstance().getCurrentDomainId());
            rsResults = pstmtSelect.executeQuery();
   
            while (rsResults.next())
            {
               lstRoleIDs.add(new Integer(rsResults.getInt(1)));
            }
         }
         finally
         {
            DatabaseUtils.closeResultSetAndStatement(rsResults, pstmtSelect);
         }
         
         if (lstRoleIDs.size() > 1)
         {
            // b. ---------------------------------------------------------------------------
            // delete all role references within the user-role map table
            // ------------------------------------------------------------------------------
            buffer.delete(0, buffer.length());
            buffer.append("delete from " + UserDatabaseSchema.USER_TABLE_NAME + "_ROLE_MAP where ROLE_ID in (");
            buffer.append(StringUtils.parseCollectionToString(lstRoleIDs, ","));
            buffer.append(")");
   
            try
            {
               pstmDelete = cntConnection.prepareStatement(buffer.toString());
               pstmDelete.executeUpdate();
            }
            finally
            {
               DatabaseUtils.closeStatement(pstmDelete);
            }

            // c. ---------------------------------------------------------------------------
            // delete all roles belonging to the user
            // ------------------------------------------------------------------------------
            buffer.delete(0, buffer.length());
            buffer.append("delete from " + RoleDatabaseSchema.ROLE_TABLE_NAME + " where ID in (");
            buffer.append(StringUtils.parseCollectionToString(lstRoleIDs, ","));
            buffer.append(") and DOMAIN_ID = ?");

            try
            {
               pstmDelete = cntConnection.prepareStatement(buffer.toString());
               pstmDelete.setInt(1, CallContext.getInstance().getCurrentDomainId());
               pstmDelete.executeUpdate();
            }
            finally
            {
               DatabaseUtils.closeStatement(pstmDelete);
            }
         }

         lstIntSessionIDs.add(new Integer(-1));
         // 1. ---------------------------------------------------------------------------
         // retrieve all internal session IDs for particular user
         // ------------------------------------------------------------------------------
         strQuery = "select ID from " + 
                    SessionDatabaseSchema.INTSESSION_TABLE_NAME + 
                    " where USER_ID = ? and DOMAIN_ID = ?";

         try
         {
            pstmtSelect = cntConnection.prepareStatement(strQuery);
            pstmtSelect.setInt(1, iId);
            pstmtSelect.setInt(2, CallContext.getInstance().getCurrentDomainId());
            rsResults = pstmtSelect.executeQuery();
   
            while (rsResults.next())
            {
               lstIntSessionIDs.add(new Integer(rsResults.getInt(1)));
            }
         }
         finally
         {
            DatabaseUtils.closeResultSetAndStatement(rsResults, pstmtSelect);
         }
         
         if (lstIntSessionIDs.size() > 1)
         {
            // 2. ---------------------------------------------------------------------------
            // delete all external sessions belonging to the user's internal sessions
            // ------------------------------------------------------------------------------
            buffer.delete(0, buffer.length());
            buffer.append("delete from " + 
                          SessionDatabaseSchema.EXTSESSION_TABLE_NAME + 
                          " where INTERNAL_SESSION_ID in (");
            buffer.append(StringUtils.parseCollectionToString(lstIntSessionIDs, ","));
            buffer.append(") and DOMAIN_ID = ?");
   
            try
            {
               pstmDelete = cntConnection.prepareStatement(buffer.toString());
               pstmDelete.setInt(1, CallContext.getInstance().getCurrentDomainId());
               pstmDelete.executeUpdate();
            }
            finally
            {
               DatabaseUtils.closeStatement(pstmDelete);
            }
               
            // 3. ---------------------------------------------------------------------------
            // delete all internal sessions belonging to the user
            // ------------------------------------------------------------------------------
            buffer.delete(0, buffer.length());
            buffer.append("delete from " + 
                          SessionDatabaseSchema.INTSESSION_TABLE_NAME + 
                          " where ID in (");
            buffer.append(StringUtils.parseCollectionToString(lstIntSessionIDs, ","));
            buffer.append(") and DOMAIN_ID = ?");
   
            try
            {
               pstmDelete = cntConnection.prepareStatement(buffer.toString());
               pstmDelete.setInt(1, CallContext.getInstance().getCurrentDomainId());
               pstmDelete.executeUpdate();
            }
            finally
            {
               DatabaseUtils.closeStatement(pstmDelete);
            }
         }

         // 4. ---------------------------------------------------------------------------
         // delete specified user from table
         // ------------------------------------------------------------------------------
         try
         {
            pstmDelete = cntConnection.prepareStatement(userSchema.getDeleteUserById());
            pstmDelete.setInt(1, iId);
            pstmDelete.setInt(2, CallContext.getInstance().getCurrentDomainId());
            iDeleted = pstmDelete.executeUpdate();
   
            if (iDeleted == 0)
            {
               throw new OSSDataNotFoundException("Data to delete cannot be found in the database");
            }
            else if (iDeleted != 1)
            {
               throw new OSSInconsistentDataException(
                            "Inconsistent database contains multiple ("
                            + iDeleted + ") data object with the same ID");
            }
         }
         finally
         {
            DatabaseUtils.closeStatement(pstmDelete);
         }
         

         // At this point we don't know if this is just a single operation
         // and we need to commit or if it is a part of bigger transaction
         // and the commit is not desired until all operations proceed.
         // Therefore let the DatabaseTransactionFactory resolve it
         transactionFactory.commitTransaction(cntConnection);
      }
      catch (OSSException bfExc)
      {
         // At this point we don't know if this is just a single operation
         // and we need to commit or if it is a part of bigger transaction
         // and the commit is not desired until all operations proceed. 
         // Therefore let the DatabaseTransactionFactory resolve it 
         try
         {
            transactionFactory.rollbackTransaction(cntConnection);
         }
         catch (SQLException sqlExc)
         {
            bfExc = new OSSMultiException(bfExc, sqlExc);
         }
         throw bfExc;
      }
      // We must catch Throwable to rollback since assert throw Error and not 
      // Exception
      catch (Throwable thrThr)
      {
         // At this point we don't know if this is just a single operation
         // and we need to commit or if it is a part of bigger transaction
         // and the commit is not desired until all operations proceed.
         // Therefore let the DatabaseTransactionFactory resolve it
         try
         {
            transactionFactory.rollbackTransaction(cntConnection);
            throw new OSSDataDeleteException(
                         "Failed to delete data from the database.", thrThr);
         }
         catch (SQLException sqlExc)
         {
            throw new OSSMultiException(thrThr, sqlExc);
         }
      }
   }

   /**
    * Method for deleting user and all his belonging sessions, roles and access rights.
    * It is necessary to be present all security db tables. 
    * 
    * @param strLikeClause - LIKE clause the users are specified by
    * @throws OSSException - error occurred during deleting
    */
   public void deleteUserCascadeManualUsingLike(
      String strLikeClause
   ) throws OSSException
   {
      if (GlobalConstants.ERROR_CHECKING)
      {
         assert strLikeClause.length() > 0 
                : "Like clause cannot be empty.";
      }

      Connection        cntConnection = null;
      PreparedStatement pstmtSelect = null;
      PreparedStatement pstmDelete = null;
      ResultSet         rsResults = null;      
      StringBuffer buffer = new StringBuffer();
      List   lstUserIDs = new ArrayList();
      List                       lstIntSessionIDs = new ArrayList();
      List                       lstRoleIDs = new ArrayList();
      int                        iDeleted = 0;
      DatabaseConnectionFactory  connectionFactory;
      DatabaseTransactionFactory transactionFactory;

      connectionFactory = DatabaseConnectionFactoryImpl.getInstance();
      transactionFactory = DatabaseTransactionFactoryImpl.getInstance();

      try
      {
         // Request autocommit false since we are modifying database
         cntConnection = connectionFactory.requestConnection(false);

         lstUserIDs.add(new Integer(-1));
         // 1. ---------------------------------------------------------------------------
         // retrieve all user IDs for specified LIKE clause 
         // ------------------------------------------------------------------------------
         buffer.delete(0, buffer.length());
         buffer.append("select ID from " + UserDatabaseSchema.USER_TABLE_NAME + " where FIRST_NAME like '");
         buffer.append(strLikeClause);
         buffer.append("'");

         try
         {
            pstmtSelect = cntConnection.prepareStatement(buffer.toString());
            rsResults = pstmtSelect.executeQuery();
   
            while (rsResults.next())
            {
               lstUserIDs.add(new Integer(rsResults.getInt(1)));
            }
            
            rsResults.close();
         }
         finally
         {
            DatabaseUtils.closeResultSetAndStatement(rsResults, pstmtSelect);
         }

         if (lstUserIDs.size() > 0)
         {
            lstRoleIDs.add(new Integer(-1));
            // 2. ---------------------------------------------------------------------------
            // retrieve all role IDs belonging to the particular user
            // ------------------------------------------------------------------------------
            buffer.delete(0, buffer.length());
            buffer.append("select ID from " + RoleDatabaseSchema.ROLE_TABLE_NAME + " where USER_ID in (");
            buffer.append(StringUtils.parseCollectionToString(lstUserIDs, ","));
            buffer.append(") and DOMAIN_ID = ?");
   
            try
            {
               pstmtSelect = cntConnection.prepareStatement(buffer.toString());
               pstmtSelect.setInt(1, CallContext.getInstance().getCurrentDomainId());
               rsResults = pstmtSelect.executeQuery();
      
               while (rsResults.next())
               {
                  lstRoleIDs.add(new Integer(rsResults.getInt(1)));
               }
            }
            finally
            {
               DatabaseUtils.closeResultSetAndStatement(rsResults, pstmtSelect);
            }
            
            if (lstRoleIDs.size() > 1)
            {
               // 3. ---------------------------------------------------------------------------
               // delete all role references within the user-role map table
               // ------------------------------------------------------------------------------
               buffer.delete(0, buffer.length());
               buffer.append("delete from " + UserDatabaseSchema.USER_TABLE_NAME + "_ROLE_MAP where ROLE_ID in (");
               buffer.append(StringUtils.parseCollectionToString(lstRoleIDs, ","));
               buffer.append(")");
      
               try
               {
                  pstmDelete = cntConnection.prepareStatement(buffer.toString());
                  pstmDelete.executeUpdate();
               }
               finally
               {
                  DatabaseUtils.closeStatement(pstmDelete);
               }
   
               // 4. ---------------------------------------------------------------------------
               // delete all roles belonging to the user
               // ------------------------------------------------------------------------------
               buffer.delete(0, buffer.length());
               buffer.append("delete from " + RoleDatabaseSchema.ROLE_TABLE_NAME + " where ID in (");
               buffer.append(StringUtils.parseCollectionToString(lstRoleIDs, ","));
               buffer.append(") and DOMAIN_ID = ?");
      
               try
               {
                  pstmDelete = cntConnection.prepareStatement(buffer.toString());
                  pstmDelete.setInt(1, CallContext.getInstance().getCurrentDomainId());
                  pstmDelete.executeUpdate();
               }
               finally
               {
                  DatabaseUtils.closeStatement(pstmDelete);
               }
            }
   
            lstIntSessionIDs.add(new Integer(-1));
            // 5. ---------------------------------------------------------------------------
            // retrieve all internal session IDs for particular user
            // ------------------------------------------------------------------------------
            buffer.delete(0, buffer.length());
            buffer.append("select ID from " + 
                          SessionDatabaseSchema.INTSESSION_TABLE_NAME + 
                          " where USER_ID in (");
            buffer.append(StringUtils.parseCollectionToString(lstUserIDs, ","));
            buffer.append(") and DOMAIN_ID = ?");

            try
            {
               pstmtSelect = cntConnection.prepareStatement(buffer.toString());
               pstmtSelect.setInt(1, CallContext.getInstance().getCurrentDomainId());
               rsResults = pstmtSelect.executeQuery();
      
               while (rsResults.next())
               {
                  lstIntSessionIDs.add(new Integer(rsResults.getInt(1)));
               }
            }
            finally
            {
               DatabaseUtils.closeResultSetAndStatement(rsResults, pstmtSelect);
            }
   
            if (lstIntSessionIDs.size() > 1)
            {
               // 6. ---------------------------------------------------------------------------
               // delete all external sessions belonging to the user's internal sessions
               // ------------------------------------------------------------------------------
               buffer.delete(0, buffer.length());
               buffer.append("delete from " + 
                             SessionDatabaseSchema.EXTSESSION_TABLE_NAME + 
                             " where INTERNAL_SESSION_ID in (");
               buffer.append(StringUtils.parseCollectionToString(lstIntSessionIDs, ","));
               buffer.append(") and DOMAIN_ID = ?");
      
               try
               {
                  pstmDelete = cntConnection.prepareStatement(buffer.toString());
                  pstmDelete.setInt(1, CallContext.getInstance().getCurrentDomainId());
                  pstmDelete.executeUpdate();
               }
               finally
               {
                  DatabaseUtils.closeStatement(pstmDelete);
               }
      
               // 7. ---------------------------------------------------------------------------
               // delete all internal sessions belonging to the user
               // ------------------------------------------------------------------------------
               buffer.delete(0, buffer.length());
               buffer.append("delete from " + 
                             SessionDatabaseSchema.INTSESSION_TABLE_NAME + 
                             " where ID in (");
               buffer.append(StringUtils.parseCollectionToString(lstIntSessionIDs, ","));
               buffer.append(") and DOMAIN_ID = ?");
      
               try
               {
                  pstmDelete = cntConnection.prepareStatement(buffer.toString());
                  pstmDelete.setInt(1, CallContext.getInstance().getCurrentDomainId());
                  pstmDelete.executeUpdate();
               }
               finally
               {
                  DatabaseUtils.closeStatement(pstmDelete);
               }
            }
   
            // 8. ---------------------------------------------------------------------------
            // delete specified users from table
            // ------------------------------------------------------------------------------
            buffer.delete(0, buffer.length());
            buffer.append("delete from " + UserDatabaseSchema.USER_TABLE_NAME + " where ID in (");
            buffer.append(StringUtils.parseCollectionToString(lstUserIDs, ","));
            buffer.append(") and DOMAIN_ID = ?");

            try
            {
               pstmDelete = cntConnection.prepareStatement(buffer.toString());
               pstmDelete.setInt(1, CallContext.getInstance().getCurrentDomainId());
               iDeleted = pstmDelete.executeUpdate();
               if (GlobalConstants.ERROR_CHECKING)
               {
                  assert iDeleted > Integer.MIN_VALUE : "Check to avoid checkstyle.";
               }
            }
            finally
            {
               DatabaseUtils.closeStatement(pstmDelete);
            }
         }

         // At this point we don't know if this is just a single operation
         // and we need to commit or if it is a part of bigger transaction
         // and the commit is not desired until all operations proceed.
         // Therefore let the DatabaseTransactionFactory resolve it
         transactionFactory.commitTransaction(cntConnection);
      }
      catch (OSSException bfExc)
      {
         // At this point we don't know if this is just a single operation
         // and we need to commit or if it is a part of bigger transaction
         // and the commit is not desired until all operations proceed. 
         // Therefore let the DatabaseTransactionFactory resolve it 
         try
         {
            transactionFactory.rollbackTransaction(cntConnection);
         }
         catch (SQLException sqlExc)
         {
            bfExc = new OSSMultiException(bfExc, sqlExc);
         }
         throw bfExc;
      }
      // We must catch Throwable to rollback since assert throw Error and not 
      // Exception
      catch (Throwable thrThr)
      {
         // At this point we don't know if this is just a single operation
         // and we need to commit or if it is a part of bigger transaction
         // and the commit is not desired until all operations proceed.
         // Therefore let the DatabaseTransactionFactory resolve it
         try
         {
            transactionFactory.rollbackTransaction(cntConnection);
            throw new OSSDataDeleteException(
                         "Failed to delete data from the database.", thrThr);
         }
         catch (SQLException sqlExc)
         {
            throw new OSSMultiException(thrThr, sqlExc);
         }
      }
      finally
      {
         connectionFactory.returnConnection(cntConnection);
      }
   }
}
