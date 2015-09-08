/*
 * Copyright (c) 2003 - 2007 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: AsyncDataLoader.java,v 1.15 2009/04/22 06:29:19 bastafidli Exp $
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
 
package org.opensubsystems.patterns.listdata.util;

import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.logic.ControllerManager;
import org.opensubsystems.core.util.Config;
import org.opensubsystems.core.util.GlobalConstants;
import org.opensubsystems.core.util.Log;
import org.opensubsystems.core.util.PropertyUtils;
import org.opensubsystems.patterns.listdata.data.ListOptions;
import org.opensubsystems.patterns.listdata.logic.ListController;

/**
 * Thread used to load data asynchronously using list controller interface.
 * 
 * The class, which wants to perform async data processing will usually  derive 
 * new class inline and define method createAddItemsRunnable.
 * 
 * TODO: Performance: We may want to convert this to Runnable and the user pool
 * of threads to run them instead of creating new thread all the time.
 *  
 * @version $Id: AsyncDataLoader.java,v 1.15 2009/04/22 06:29:19 bastafidli Exp $
 * @author Miro Halas
 * @code.reviewer Miro Halas
 * @code.reviewed 1.9 2007/01/07 06:14:10 bastafidli
 */
public class AsyncDataLoader extends Thread
{
   // Configuration settings ///////////////////////////////////////////////////
   
   /**
    * Configuration setting to specify how many items to load at once when 
    * loading data asynchronously. If this is equal to ListOptions.PAGE_SIZE_ALL 
    * then all items will be loaded at once. 
    */
   public static final String ASYNC_LOADER_PAGE_SIZE 
                                 = "oss.listdata.asyncloader.pagesize";
   
   // Constants ////////////////////////////////////////////////////////////////
   
   /**
    * Default value for how many items to load at once when loading data 
    * asynchronously. 
    */
   // TODO: Performance: Tune this size to provide optimal performance.
   public static final int ASYNC_LOADER_PAGE_SIZE_DEFAULT = 50;
   
   /**
    * This object should be used as a placeholder when there is nothing to do
    * to prevent creation of new objects and exhausting memory.
    */
   public static final Runnable NOTHING_TO_DO = new Runnable()
                                                {
                                                   public void run()
                                                   {
                                                   }
                                                };
   
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Identification of this loader which is send back to owner to distinguish 
    * if there are multiple loaders present.
    */
   protected int m_iLoaderIdentification;
                                                
   /**
    * Owner which is loading the data using this thread.
    */
   protected AsyncDataLoaderOwner m_owner;
   
   /**
    * Identifier of the data type that should be loaded by the loader, since the 
    * same type of data objects can be retrieved and presented to clients in 
    * multiple type of views. One of the constants defined in DataConstant class.  
    */
   protected String m_strDataTypeView;
   
   /**
    * Value uniquely identifying the current state of the object to which 
    * the data should be added. This is used to detect when to stop loading
    * by increasing the value of the original variable to be different from
    * this value to prevent modifying the data then the object to which the data
    * are loaded has changed meanwhile.
    */
   protected int m_iCurrentObjectVersion;

   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(AsyncDataLoader.class);

   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Construct new loader.
    * 
    * @param iLoaderIdentification - identification of this loader which is send
    *                                back to owner to distinguish if there are
    *                                multiple loader present
    * @param owner - owner of this data loader
    * @param strDataTypeView - identifier of the data type that should be loaded 
    *                          by this loader, since the same type of data 
    *                          objects can be retrieved and presented to clients 
    *                          in multiple type of views. One of the constants 
    *                          defined in DataConstant class.
    * @param iObjectVersion - value uniquely identifying the current state of 
    *                         the object to which the data should be added 
    */
   public AsyncDataLoader(
      int                  iLoaderIdentification,
      AsyncDataLoaderOwner owner,
      String               strDataTypeView,
      int                  iObjectVersion
   )
   {
      if (GlobalConstants.ERROR_CHECKING)
      {   
         assert owner != null : "Owner of the data loader cannot be null.";
         assert strDataTypeView != null : "Data type view cannot be null."; 
      }

      m_iLoaderIdentification = iLoaderIdentification;
      m_owner                 = owner;
      m_strDataTypeView       = strDataTypeView;
      m_iCurrentObjectVersion = iObjectVersion;
   }
   
   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * {@inheritDoc}
    */
   public void run(
   )
   {
      ListController listLoader;
      ListOptions    listData = getInitialListOptions();
      Object[]       data;
      List           lstData;
          
      try
      {  
         listLoader = getListController();
         data = listLoader.getInitialPage(listData);
         if (data != null)
         {
            listData = (ListOptions)data[0];
            lstData = (List)data[1];                           
                                                              
            while ((listData != null) && (lstData != null) && (!lstData.isEmpty()))
            {
               // TODO: Feature: Add ability to pause loading of data.
               // As soon as we load the first data we know the total amount 
               // of data and therefore we can on a screen adjust a scrollbar
               // then we load/show only as much data (or maybe little more) as
               // the screen allows to see. Only when user scrolls up and down
               // we would load additional data
               if (shouldStopLoading())
               {
                  break;
               }
               processData(lstData, m_iCurrentObjectVersion);
               
               if (shouldStopLoading())
               {
                  break;
               }
               else
               {
                  if (listData.getEndPosition() != listData.getActualListSize())
                  {
                     data = listLoader.getNextPage(listData);
                     if (data == null)
                     {
                        break;
                     }
                     else
                     {
                        listData = (ListOptions)data[0];
                        lstData = (List)data[1];                           
                     }
                  }
                  else
                  {
                     // We are done
                     break;
                  }
               }
            }

            allDataLoaded();
         }
      }
      catch (Throwable thr)
      {
         s_logger.log(Level.WARNING, 
                      "Unexpected error has occurred while loading data.", thr);
      }
   }

   // Helper methods ///////////////////////////////////////////////////////////
   
   /**
    * Return the initial ListOptions structure used to load the list of data 
    * objects. This method may be overridden if some parameters should be preset.
    * 
    * @return ListOptions - initialized list options
    */
   protected ListOptions getInitialListOptions(
   )
   {
      // Read configuration parameters how many items to load at once when 
      // loading data in the background. If this is equal to 
      // ListOptions.PAGE_SIZE_ALL then all items will be loaded at once.
      // Read it here instead of in static block since if this code is executed
      // in different execution context, it might have different configuration
      // settings.
      int        iPageSize;
      Properties prpSettings;

      if (GlobalConstants.ERROR_CHECKING)
      {
         assert ListOptions.PAGE_SIZE_ALL == 0
                : "Constant value has changed, review the code";
      }
      
      prpSettings = Config.getInstance().getProperties();
      iPageSize = PropertyUtils.getIntPropertyInRange(
                     prpSettings, ASYNC_LOADER_PAGE_SIZE, 
                     ASYNC_LOADER_PAGE_SIZE_DEFAULT, 
                     "Asynchronous data loader page size",
                     // 0 is allowed since it is ListOptions.PAGE_SIZE_ALL
                     0, Integer.MAX_VALUE);
      
      ListOptions options = new ListOptions(m_strDataTypeView); 
      options.setPageSize(iPageSize);
      // Give owner chance to modify the default settings
      options = m_owner.getInitialListOptions(this, options);
      
      return options;
   }
   
   /**
    * This method should return true if the async thread should stop loading 
    * data.
    * 
    * @return boolean - true if the async thread should stop loading data, false
    *                   to continue loading   
    */
   protected boolean shouldStopLoading(
   )
   {
      return m_owner.shouldStopLoading(this);
   }
   
   /**
    * Process data which were asynchronously loaded.
    * 
    * @param lstDataToAdd - list of data items to add to the container
    * @param iOriginalObjectVersion - version of the object by which the data 
    *        should be processed, which is the version which was used to 
    *        construct this object. If the version of the object at the time 
    *        when the data are being processed is different from this version, 
    *        then the data will not be processed or the processing of the data 
    *        stops.
    */
   protected void processData(
      final List lstDataToAdd,
      final int  iOriginalObjectVersion
   )
   {
      m_owner.processData(this, lstDataToAdd, iOriginalObjectVersion);
   }
   
   /**
    * This function is called where data loading finished. It is useful if you 
    * want do same changes or ask for information relevant to the whole dataset.
    */
   protected void allDataLoaded()
   {
      m_owner.allDataLoaded(this);
   }
   
   /**
    * Get list controller to use.
    * 
    * @return ListController
    * @throws OSSException - an error has occurred
    */
   protected ListController getListController(
   ) throws OSSException
   {
      ListController controller;
      
      controller = (ListController)ControllerManager.getInstance(
                                                        ListController.class);
      
      return controller;
   }
}
