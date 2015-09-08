/*
 * Copyright (c) 2003 - 2007 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: TestRoleDatabaseFactoryUtils.java,v 1.15 2009/04/22 06:29:41 bastafidli Exp $
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
import java.sql.SQLException;

import org.opensubsystems.core.data.DataObject;
import org.opensubsystems.core.error.OSSDataDeleteException;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.error.OSSMultiException;
import org.opensubsystems.core.persist.db.DatabaseConnectionFactory;
import org.opensubsystems.core.persist.db.DatabaseSchemaManager;
import org.opensubsystems.core.persist.db.DatabaseTransactionFactory;
import org.opensubsystems.core.persist.db.impl.DatabaseConnectionFactoryImpl;
import org.opensubsystems.core.persist.db.impl.DatabaseTransactionFactoryImpl;
import org.opensubsystems.core.util.CallContext;
import org.opensubsystems.core.util.GlobalConstants;
import org.opensubsystems.core.util.jdbc.DatabaseUtils;
import org.opensubsystems.security.persist.db.DomainDatabaseSchema;
import org.opensubsystems.security.persist.db.RoleDatabaseSchema;
import org.opensubsystems.security.persist.db.UserDatabaseSchema;

/**
 * This class is used as utilities for jUnit tests. There will be provided 
 * manual cascade deleting.  
 *
 * @version $Id: TestRoleDatabaseFactoryUtils.java,v 1.15 2009/04/22 06:29:41 bastafidli Exp $
 * @author Julo Legeny
 * @code.reviewer 
 * @code.reviewer TODO: Review this code
 */
public class TestRoleDatabaseFactoryUtils 
{
   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * Method for deleting role and all his belonging references from the user-role map table.
    * 
    * @param iId - ID of the role
    * @throws OSSException - error occurred during deleting
    */
   public void deleteRoleCascadeManual(
      int iId
   ) throws OSSException
   {
      deleteRoleCascadeManual(iId, DataObject.NEW_ID);
   }
   
   
   /**
    * Method for deleting role and all his belonging references from the user-role map table.
    * 
    * @param iId - ID of the role
    * @param iDomainId - ID of the domain the role belongs to
    * @throws OSSException - error occurred during deleting
    */
   public void deleteRoleCascadeManual(
      int iId,
      int iDomainId
   ) throws OSSException
   {
      if (GlobalConstants.ERROR_CHECKING)
      {
         assert iId != DataObject.NEW_ID 
                : "Cannot delete data, which wasn't created yet.";
      }

      Connection                 cntConnection = null;
      PreparedStatement          pstmDelete = null;
      String                     strQuery = "";
      int                        iDeleted;
      RoleDatabaseSchema         roleSchema;
      DatabaseConnectionFactory  connectionFactory;
      DatabaseTransactionFactory transactionFactory;

      connectionFactory = DatabaseConnectionFactoryImpl.getInstance();
      transactionFactory = DatabaseTransactionFactoryImpl.getInstance();
      roleSchema = ((RoleDatabaseSchema)DatabaseSchemaManager.getInstance(
                    RoleDatabaseSchema.class));
 
      try
      {
         // Request autocommit false since we are modifying database
         cntConnection = connectionFactory.requestConnection(false);

         // 1. ---------------------------------------------------------------------------
         // delete all role references from user-role map table
         // ------------------------------------------------------------------------------
         strQuery = "delete from " + UserDatabaseSchema.USER_TABLE_NAME + "_ROLE_MAP where ROLE_ID = ?";

         pstmDelete = cntConnection.prepareStatement(strQuery);
         pstmDelete.setInt(1, iId);
         pstmDelete.executeUpdate();
   
         // 2. ---------------------------------------------------------------------------
         // delete all role references from domain-role map table
         // ------------------------------------------------------------------------------
         strQuery = "delete from " + DomainDatabaseSchema.DOMAIN_TABLE_NAME + "_ROLE_MAP where ROLE_ID = ?";

         pstmDelete = cntConnection.prepareStatement(strQuery);
         pstmDelete.setInt(1, iId);
         pstmDelete.executeUpdate();

         // 3. ---------------------------------------------------------------------------
         // delete all access rights belonging to the specified role
         // ------------------------------------------------------------------------------
         strQuery = "delete from " + RoleDatabaseSchema.ACCESSRIGHT_TABLE_NAME + " where ROLE_ID = ?";

         pstmDelete = cntConnection.prepareStatement(strQuery);
         pstmDelete.setInt(1, iId);
         pstmDelete.executeUpdate();

         // 4. ---------------------------------------------------------------------------
         // delete specified role from table
         // ------------------------------------------------------------------------------
         if (iDomainId == DataObject.NEW_ID)
         {
            iDomainId = CallContext.getInstance().getCurrentDomainId();
         }
         pstmDelete = cntConnection.prepareStatement(roleSchema.getDeleteRoleById());
         pstmDelete.setInt(1, iId);
         pstmDelete.setInt(2, iDomainId);
         iDeleted = pstmDelete.executeUpdate();
         if (GlobalConstants.ERROR_CHECKING)
         {
            assert iDeleted > Integer.MIN_VALUE : "Check to avoid checkstyle.";
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
         DatabaseUtils.closeStatement(pstmDelete);
         connectionFactory.returnConnection(cntConnection);
      }
   }
}
