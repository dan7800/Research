/*
 * Copyright (c) 2003 - 2007 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: BackgroundTaskListener.java,v 1.13 2007/11/06 03:39:43 bastafidli Exp $
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

package org.opensubsystems.patterns.backgroundtask.www;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.util.Config;
import org.opensubsystems.core.util.DateUtils;
import org.opensubsystems.core.util.Log;
import org.opensubsystems.core.util.PropertyUtils;
import org.opensubsystems.patterns.backgroundtask.util.BackgroundTask;

/**
 * BackgroundTaskListener is responsible for initialization and starting of 
 * tasks which in background, that is without user interaction, periodically 
 * execute some functionality. This class is useful for starting such tasks in 
 * web environment when the application is running as managed application on the
 * application server and therefore it's lifecycle is controlled by the 
 * application server.
 * 
 * @version $Id: BackgroundTaskListener.java,v 1.13 2007/11/06 03:39:43 bastafidli Exp $
 * @author Miro Halas
 * @code.reviewer Miro Halas
 * @code.reviewed 1.6 2006/04/20 13:59:45 bastafidli
 */
public abstract class BackgroundTaskListener implements ServletContextListener
{
   // Attributes ///////////////////////////////////////////////////////////////   
   
   /**
    * Map with all tasks created by this listener. Key is task name read from 
    * properties, value is BackgroundTask derived object for this name.
    */
   protected Map m_tasks;
   
   /**
    * Timer which periodically execute receiver tasks threads. Key is task name, 
    * value is task timer.
    */
   protected Map m_tasksTimers;

   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(BackgroundTaskListener.class);

   // Constructor //////////////////////////////////////////////////////////////
   
   /** 
    * Creates a new instance of FileProcessorListener
    */
   public BackgroundTaskListener()
   {
      super();
   }
   
   /**
    * {@inheritDoc}
    */
   public void contextInitialized(
      ServletContextEvent servletContextEvent
   )
   {
      s_logger.entering(this.getClass().getName(), "contextInitialized");
      try
      {
         Properties     prpSettings;
         BackgroundTask currentTask = null;
         Timer          currentTimer = null;
         List           lstTaskIds = new ArrayList();
         Boolean        bTaskState;
         StringBuffer   sbTaskIdentifier = new StringBuffer();
         int            iDefaultTaskIdentifierLength;
         int            iIndex = 0;
           
         prpSettings = Config.getInstance().getProperties();
         
         sbTaskIdentifier.append(getBaseProperty());
         sbTaskIdentifier.append(".");
         iDefaultTaskIdentifierLength = sbTaskIdentifier.length();
         
         // Find out what tasks are configured in the system and which ones of
         // them are enabled
         do
         {
            // Remove the previous number
            sbTaskIdentifier.delete(iDefaultTaskIdentifierLength, 
                                    sbTaskIdentifier.length());
            sbTaskIdentifier.append(iIndex);
            // And check if this task exists and if it is enabled or disabled
            
            bTaskState = PropertyUtils.getBooleanProperty(
                            prpSettings, sbTaskIdentifier.toString(), null, 
                            "Enabled state of background task " 
                            + getBaseProperty() + " #" + iIndex);
            
            if (bTaskState != null)
            {
               s_logger.fine("Read background task " + sbTaskIdentifier
                             + " state " + bTaskState);
               if (bTaskState.booleanValue())
               {
                  // remember this id
                  lstTaskIds.add(new Integer(iIndex));
               }
               iIndex++;               
            }
         }
         while (bTaskState != null);
      
         // And now create and start those tasks
         m_tasks = new HashMap(lstTaskIds.size());
         m_tasksTimers = new HashMap(lstTaskIds.size());
      
         Iterator tasksIterator = lstTaskIds.iterator();
         Integer  iTaskId;
         
         while (tasksIterator.hasNext())
         {
            try
            {
               iTaskId = (Integer)tasksIterator.next();
   
               currentTask  = createTask(iTaskId.intValue());
               currentTimer = new Timer(true);
               m_tasks.put(iTaskId, currentTask);
               m_tasksTimers.put(iTaskId, currentTimer);
               
               // This method will start thread every runEveryValue seconds, if 
               // thread is after runEveryValue still running, new thread will  
               // not be started
               currentTimer.schedule(
                  currentTask,             
                  currentTask.getStartDelay() * DateUtils.ONE_SECOND,
                  (long) (currentTask.getRunEvery() * DateUtils.ONE_SECOND));
            }
            catch (Throwable thr)
            {
               // TODO: Improve: Is this the correct course of actions? We may
               // actually want to abort the application
               s_logger.log(Level.SEVERE, 
                            "Problem while creating background task", thr);
            }
         }
      }
      // This is here just so we get a log about the exception since the 
      // web server may not print it out
      catch (Throwable thr)
      {
         // No way to throw checked exception so convert it to unchecked 
         s_logger.log(Level.SEVERE, "Unexpected exception.", thr);
         throw new RuntimeException("Unexpected exception.", thr);         
      }
      finally
      {
         s_logger.exiting(this.getClass().getName(), "contextInitialized");
      }
   }

   /**
    * {@inheritDoc}
    */
   public void contextDestroyed(
       ServletContextEvent servletContextEvent
   )
   {
      s_logger.entering(this.getClass().getName(), "contextDestroyed");
      try
      {
         if (m_tasksTimers != null)
         {
            Iterator       timersIterator = m_tasksTimers.entrySet().iterator();
            Timer          currentTaskTimer;
            Integer        currentTaskID;
            BackgroundTask currentTask;
            Map.Entry      currentEntry;
   
            while (timersIterator.hasNext())
            {
               currentEntry = (Map.Entry)timersIterator.next();
               currentTaskID = (Integer)currentEntry.getKey();
               currentTaskTimer = (Timer)currentEntry.getValue();
               // stop thread
               currentTaskTimer.cancel();
               // Cancel task which will logout the user if needed
               currentTask = (BackgroundTask)m_tasks.get(currentTaskID);
               currentTask.cancel();
            }
         }
      }
      finally
      {
         s_logger.exiting(this.getClass().getName(), "contextDestroyed");
      }
   }
   
   /**
    * Create task with given id. Derived class will implement this by creation
    * of specific task based on the business logic of the derived class. 
    * 
    * @param iId - id of the task to create
    * @return BackgroundTask - newly created task
    * @throws OSSException - error during creation of task 
    */
   protected abstract BackgroundTask createTask(
      int iId
   ) throws OSSException;

   /**
    * Get base property which will be used to construct identification of the task.
    * The identification of task will be property.X where X goes sequentially from
    * 0 to x and can have value 1 for enabled task or 0 for disabled task. 
    * 
    * @return String - name of the base property for task 
    */
   // TODO: Improve: Implement this method in this class by returning value 
   // passed into constructor as a new parameter
   protected abstract String getBaseProperty(
   );
}
