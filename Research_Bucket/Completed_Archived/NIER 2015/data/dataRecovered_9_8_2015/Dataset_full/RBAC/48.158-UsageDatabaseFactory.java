/*
 * Copyright (c) 2005 - 2009 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: UsageDatabaseFactory.java,v 1.24 2009/04/22 06:29:39 bastafidli Exp $
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

package org.opensubsystems.preferences.persist.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.opensubsystems.core.data.DataObject;
import org.opensubsystems.core.error.OSSDatabaseAccessException;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.persist.db.BasicDatabaseFactory;
import org.opensubsystems.core.persist.db.DatabaseSchemaManager;
import org.opensubsystems.core.persist.db.impl.DatabaseFactoryImpl;
import org.opensubsystems.core.persist.db.operation.DatabaseCreateSingleDataObjectOperation;
import org.opensubsystems.core.persist.db.operation.DatabaseReadMultipleOperation;
import org.opensubsystems.core.persist.db.operation.DatabaseReadOperation;
import org.opensubsystems.core.persist.db.operation.DatabaseReadSingleDataObjectOperation;
import org.opensubsystems.core.persist.db.operation.DatabaseUpdateOperation;
import org.opensubsystems.core.util.CallContext;
import org.opensubsystems.core.util.GlobalConstants;
import org.opensubsystems.core.util.TwoObjectStruct;
import org.opensubsystems.core.util.jdbc.DatabaseUtils;
import org.opensubsystems.preferences.data.Usage;
import org.opensubsystems.preferences.data.UsageConstant;
import org.opensubsystems.preferences.data.UsageDataDescriptor;
import org.opensubsystems.preferences.persist.UsageFactory;

/**
 * Data factory to retrieve and manipulate usages in persistence store.  
 *
 * @version $Id: UsageDatabaseFactory.java,v 1.24 2009/04/22 06:29:39 bastafidli Exp $
 * @author Julian Legeny
 * @code.reviewer Miro Halas 
 * @code.reviewed 1.10 2005/11/10 06:34:52 bastafidli
 */
public class UsageDatabaseFactory extends DatabaseFactoryImpl 
                                  implements UsageFactory,
                                             BasicDatabaseFactory
{
   // Cached values ////////////////////////////////////////////////////////////
   
   /**
    * Schema to use to execute database dependent operations.
    */
   protected UsageDatabaseSchema m_schema;
   
   // Constructors /////////////////////////////////////////////////////////////

   /**
    * Default constructor.
    * 
    * @throws OSSException - an error has occurred
    */
   public UsageDatabaseFactory(
   ) throws OSSException 
   {
      super(UsageDataDescriptor.class);
      
      m_schema = ((UsageDatabaseSchema)DatabaseSchemaManager.getInstance(
                                         UsageDatabaseSchema.class));
   }

   // List operations //////////////////////////////////////////////////////////

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
         data = new Usage(iDomainId);
      }
      else
      {
         int[] arrColumnCodes =  ((UsageDataDescriptor)getDataDescriptor())
                                    .getAllColumnCodes();

         DatabaseReadOperation dbop = new DatabaseReadSingleDataObjectOperation(
                  this, m_schema.getSelectUsageById(arrColumnCodes),
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
      int initialIndex
   ) throws OSSException
   {
      Usage data;

      try
      {
         // The order must exactly match the order in COLUMNS constant
         data = new Usage(rsQueryResults.getInt(initialIndex),
                          rsQueryResults.getInt(initialIndex + 1),
                          rsQueryResults.getInt(initialIndex + 2),
                          rsQueryResults.getInt(initialIndex + 3),
                          rsQueryResults.getInt(initialIndex + 4),
                          rsQueryResults.getInt(initialIndex + 5),
                          rsQueryResults.getInt(initialIndex + 6),
                          rsQueryResults.getTimestamp(initialIndex + 7),
                          rsQueryResults.getTimestamp(initialIndex + 8));
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
      Usage usage = (Usage)data;

      // Here you must pass the domain id sent to you in usage object
      // If you want to check if this id is the same as current domain id
      // do it at the controller level. 
      insertStatement.setInt(iIndex++, usage.getDomainId());
      insertStatement.setInt(iIndex++, usage.getUserId());
      insertStatement.setInt(iIndex++, usage.getDataId());
      insertStatement.setInt(iIndex++, usage.getDataType());
      insertStatement.setInt(iIndex++, usage.getAction());
      insertStatement.setInt(iIndex++, usage.getCustomNumber());
      
      return iIndex;
   }

   // Operations specific to this factory //////////////////////////////////////

   /**
    * {@inheritDoc}
    */
   public void create(
      final Usage data,
      final int   iMaxCounter
   ) throws OSSException
   {
      if (GlobalConstants.ERROR_CHECKING)
      {
         assert data != null : "Data cannot be null";
         assert data.getId() == DataObject.NEW_ID 
                : "Cannot create already created data.";
      }

      // First try to update particular record
      DatabaseUpdateOperation updateOp = new DatabaseUpdateOperation(
         this, m_schema.getUpdateUsage(), m_schema,
         DatabaseUpdateOperation.DBOP_UPDATE, data)
      {
         protected void performOperation(
            DatabaseFactoryImpl dbfactory,
            Connection          cntConnection, 
            PreparedStatement   pstmUpdate
         ) throws OSSException,
                  SQLException
         {
            int iUpdated;
            pstmUpdate.setInt(1, CallContext.getInstance().getCurrentDomainId());
            pstmUpdate.setInt(2, CallContext.getInstance().getCurrentUserId());
            pstmUpdate.setInt(3, data.getDataId());
            pstmUpdate.setInt(4, data.getDataType());
            pstmUpdate.setInt(5, data.getAction());
            pstmUpdate.setInt(6, data.getCustomNumber());
            iUpdated = pstmUpdate.executeUpdate();
            setReturnData(new Integer(iUpdated));
         }         
      };
      updateOp.executeUpdate();

      // If we have updated the existing record we are done otherwise continue 
      // to create one
      int iUpdated = ((Integer)updateOp.getReturnData()).intValue();
      if (iUpdated == 0)
      {
         // Record was not updated it means that there is no usage record for 
         // this data stored yet and we have to add it. To do so we need to 
         // check number of stored records for particular datatype, action and 
         // custom number.
         
         // Find out how many usage records are there and if there is more than 
         // the threshold then get the ones we should keep. 
         // Use read multiple operation since we can potentially read multiple
         // records and it will also prepare statement in such a way that we
         // can do absolute positioning
         DatabaseReadOperation selectOp = new DatabaseReadMultipleOperation(
            this, m_schema.getSelectAllUsages(), m_schema)
         {
            protected Object performOperation(
               DatabaseFactoryImpl dbfactory,
               Connection          cntConnection,
               PreparedStatement   pstmQuery
            ) throws OSSException,
                     SQLException
            {
               ResultSet       rsQueryResults = null;
               TwoObjectStruct results = null;

               try
               {
                  int   iTotalRecordCount = 0;
                  int[] arrIDs = null;
                  
                  pstmQuery.setInt(1, CallContext.getInstance().getCurrentDomainId());
                  pstmQuery.setInt(2, CallContext.getInstance().getCurrentUserId());
                  pstmQuery.setInt(3, data.getDataType());
                  pstmQuery.setInt(4, data.getAction());
                  pstmQuery.setInt(5, data.getCustomNumber());
            
                  rsQueryResults = pstmQuery.executeQuery();
                  if (rsQueryResults.last())
                  {
                     // store number of total rows
                     iTotalRecordCount = rsQueryResults.getRow();
                  }
                  
                  if (iTotalRecordCount >= iMaxCounter)
                  {
                     int    iIndex;
                     
                     rsQueryResults.beforeFirst();
                     // fill array of all used IDs
                     arrIDs = new int[iMaxCounter];
                     for (iIndex = 0; iIndex < iMaxCounter; iIndex++)
                     {
                        rsQueryResults.next();
                        arrIDs[iIndex] = rsQueryResults.getInt(1);
                     }
                  }
                  
                  results = new TwoObjectStruct(new Integer(iTotalRecordCount),
                                                arrIDs);
               }
               finally
               {
                  DatabaseUtils.closeResultSet(rsQueryResults);
               }
               
               return results;
            }         
         };

         TwoObjectStruct results;
         final int       iTotalRecordCount;
         final int[]     arrIDs;
          
         results = (TwoObjectStruct)selectOp.executeRead();
         iTotalRecordCount = ((Integer)results.getFirst()).intValue();
         arrIDs = (int[])results.getSecond();
         
         // There is less usage records than our threshold so just insert new 
         // record
         if (iTotalRecordCount < iMaxCounter)
         {
            Usage createData;
            
            if (GlobalConstants.ERROR_CHECKING)
            {
               assert data.getId() == DataObject.NEW_ID 
                      : "Cannot create already created data.";
            }

            DatabaseUpdateOperation dbop = new DatabaseCreateSingleDataObjectOperation(
                     this, m_schema.getInsertUsageAndFetchGeneratedValues(), 
                     m_schema, data);
            dbop.executeUpdate();

            createData = (Usage)dbop.getReturnData();
            
            // Include assert to avoid checkstyle warning about not user variable
            if (GlobalConstants.ERROR_CHECKING)
            {
               assert createData != null : "Created data cannot be null";
            }
         }
            
         // There is at least or more usage records than our threshold so 
         // overwrite the oldest one (which would be deleted otherwise since it
         // is the oldest) to represent our new record 
         if (iTotalRecordCount >= iMaxCounter)
         {
            DatabaseUpdateOperation dbop3 = new DatabaseUpdateOperation(
               this, m_schema.getUpdateUsageByID(), m_schema,
               DatabaseUpdateOperation.DBOP_UPDATE, data)
            {
               protected void performOperation(
                  DatabaseFactoryImpl dbfactory,
                  Connection          cntConnection, 
                  PreparedStatement   pstmUpdate
               ) throws OSSException,
                        SQLException
               {
                  int iUpdated;
                  
                  pstmUpdate.setInt(1, data.getDataId());
                  pstmUpdate.setInt(2, CallContext.getInstance().getCurrentDomainId());
                  pstmUpdate.setInt(3, arrIDs[iMaxCounter - 1]);
                  iUpdated = pstmUpdate.executeUpdate();
                  if (GlobalConstants.ERROR_CHECKING)
                  {
                     assert iUpdated > 0 
                            : "Usage has to be updated for case when number"
                              + " of records >= to max counter.";
                  }
               }         
            };
            dbop3.executeUpdate();
         }
            
         // There is at least or more usage records than our threshold (for 
         // example because the threshold was decreased and we kept more records 
         // before) so delete all the ones which exceed the threshold 
         if (iTotalRecordCount > iMaxCounter)
         {
            DatabaseUpdateOperation dbop3 = new DatabaseUpdateOperation(
               this, m_schema.getDeleteUnusedUsages(arrIDs), m_schema,
               DatabaseUpdateOperation.DBOP_DELETE, data)
            {
               protected void performOperation(
                  DatabaseFactoryImpl dbfactory,
                  Connection          cntConnection, 
                  PreparedStatement   pstmUpdate
               ) throws OSSException,
                        SQLException
               {
                  int iDeleted;
                  
                  pstmUpdate.setInt(1, CallContext.getInstance().getCurrentDomainId());
                  pstmUpdate.setInt(2, CallContext.getInstance().getCurrentUserId());
                  pstmUpdate.setInt(3, data.getDataType());
                  pstmUpdate.setInt(4, data.getAction());
                  pstmUpdate.setInt(5, data.getCustomNumber());

                  iDeleted = pstmUpdate.executeUpdate();

                  if (GlobalConstants.ERROR_CHECKING)
                  {
                     assert iDeleted == (iTotalRecordCount - iMaxCounter) 
                            : "Incorrect number of deleted usages in " 
                               + "case when number of records is greater " 
                               + "than max counter.";
                  }
               }         
            };
            dbop3.executeUpdate();
         }
      }
   }   

   /**
    * {@inheritDoc}
    */
   public void create(
      int iDataId,
      int iObjectDataType, 
      int iAction, 
      int iCustomNumber,
      int iMaxCounter
   ) throws OSSException
   {
      if (GlobalConstants.ERROR_CHECKING)
      {
         assert iObjectDataType != DataObject.NO_DATA_TYPE 
                : "Datatype has to be specified.";
         assert iAction != UsageConstant.NO_ACTION 
                : "Action has to be specified.";
      }
      // construct usage data object
      Usage uUsage = new Usage(DataObject.NEW_ID,
                               CallContext.getInstance().getCurrentDomainId(),
                               CallContext.getInstance().getCurrentUserId(),
                               iDataId,
                               iObjectDataType,
                               iAction,
                               iCustomNumber,
                               null,
                               null
                              );
      // call method that will create usage data object 
      create(uUsage, iMaxCounter);
   } 

   /**
    * {@inheritDoc}
    */
   public int delete(
      final int iObjectDataType, 
      final int iAction,
      final int iCustomNumber,
      boolean bCheckUserID
   ) throws OSSException
   {
      int iUserId = DataObject.NEW_ID;
      
      // set up user id to current user if we want check also user id 
      // and generate particular condition to the sql command
      if (bCheckUserID)
      {
         iUserId = CallContext.getInstance().getCurrentUserId();
      }

      final int iUserIdFinal = iUserId;

      DatabaseUpdateOperation dbop = new DatabaseUpdateOperation(
         this, m_schema.getDeleteUsage(iUserIdFinal, iObjectDataType, 
                                       iAction, iCustomNumber),
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
            int iPosition = 1;  
            
            pstmQuery.setInt(iPosition++, CallContext.getInstance().getCurrentDomainId());
      
            // prepare statement for user ID if it is specified  
            if (iUserIdFinal != DataObject.NEW_ID)
            {
               pstmQuery.setInt(iPosition++, iUserIdFinal);
            }
            // prepare statement for object ID if it is specified  
            if (iObjectDataType != DataObject.NO_DATA_TYPE)
            {
               pstmQuery.setInt(iPosition++, iObjectDataType);
            }
            // append condition for action if it is specified  
            if (iAction != UsageConstant.NO_ACTION)
            {
               pstmQuery.setInt(iPosition++, iAction);
            }
            // append condition for custom number if it is specified  
            if (iCustomNumber != DataObject.NEW_ID)
            {
               pstmQuery.setInt(iPosition++, iCustomNumber);
            }

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
   public List getUsage(
      int iObjectDataType,
      int iAction,
      int iCustomNumber
   ) throws OSSException 
   {
      return getUsage(CallContext.getInstance().getCurrentUserId(),
                      iObjectDataType,
                      iAction,
                      iCustomNumber);
   }

   /**
    * {@inheritDoc}
    * 
    * !!! IMPORTANT: This method is used only for junit test purposes and
    *                shouldn't be used for another purposes. Use getUsage()
    *                method above instead of this one. !!!
    */
   public List getUsage(
      final int iUserId,
      final int iObjectDataType,
      final int iAction,
      final int iCustomNumber
   ) throws OSSException 
   {
      DatabaseReadOperation dbop = new DatabaseReadMultipleOperation(
         this, m_schema.getSelectUsage(iUserId, iObjectDataType,
                                       iAction, iCustomNumber), m_schema)
      {
         protected Object performOperation(
            DatabaseFactoryImpl dbfactory,
            Connection          cntConnection,
            PreparedStatement   pstmQuery
         ) throws OSSException,
                  SQLException
         {
            int iPosition = 1;
            pstmQuery.setInt(iPosition++, CallContext.getInstance().getCurrentDomainId());
            // prepare statement for user ID if it is specified  
            if (iUserId != DataObject.NEW_ID)
            {
               pstmQuery.setInt(iPosition++, iUserId);
            }
            // prepare statement for object ID if it is specified  
            if (iObjectDataType != DataObject.NO_DATA_TYPE)
            {
               pstmQuery.setInt(iPosition++, iObjectDataType);
            }
            // append condition for action if it is specified  
            if (iAction != UsageConstant.NO_ACTION)
            {
               pstmQuery.setInt(iPosition++, iAction);
            }
            // append condition for custom number if it is specified  
            if (iCustomNumber != DataObject.NEW_ID)
            {
               pstmQuery.setInt(iPosition++, iCustomNumber);
            }
   
            return loadMultipleData(dbfactory, pstmQuery);
         }         
      };
      return (List)dbop.executeRead();
   }
}
