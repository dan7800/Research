/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2004 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.server;

import java.sql.*;
import java.util.*;

import org.compiere.model.*;
import org.compiere.wf.*;
import org.compiere.util.*;

/**
 *	Compiere Server Base
 *	
 *  @author Jorg Janke
 *  @version $Id: CompiereServer.java,v 1.3 2004/09/09 14:21:04 jjanke Exp $
 */
public abstract class CompiereServer extends Thread
{
	/**
	 * 	Create New Server Thead
	 *	@param model model
	 *	@return server tread or null
	 */
	public static CompiereServer create (CompiereProcessor model)
	{
		if (model instanceof MRequestProcessor)
			return new RequestProcessor ((MRequestProcessor)model);
		if (model instanceof MWorkflowProcessor)
			return new WorkflowProcessor ((MWorkflowProcessor)model);
		if (model instanceof MAcctProcessor)
			return new AcctProcessor ((MAcctProcessor)model);
		if (model instanceof MAlertProcessor)
			return new AlertProcessor ((MAlertProcessor)model);
		if (model instanceof MScheduler)
			return new Scheduler ((MScheduler)model);
		throw new IllegalArgumentException("Unknown Processor");
	}	//	 create

	
	/**************************************************************************
	 * 	Server Base Class
	 * 	@param name name
	 *	@param initialNap delay time running in sec
	 */
	protected CompiereServer (CompiereProcessor model, int initialNap)
	{
		super (CompiereServerGroup.get(), null, model.getName(), 0);
		p_model = model;
		m_ctx = model.getCtx();
		m_initialNap = initialNap;
	//	log.info(model.getName() + " - " + getThreadGroup());
	}	//	ServerBase

	/**	The Processor Model						*/
	protected					CompiereProcessor 	p_model;
	/** Initial nap is seconds		*/
	private int					m_initialNap = 0;
	
	/**	Miliseconds to sleep - 10 Min default	*/
	private long				m_sleepMS = 600000;
	/** Sleeping					*/
	private volatile boolean	m_sleeping = false;
	/** Server start time					*/
	private long				m_start = 0;
	/** Number of Work executions	*/
	protected int 				p_runCount = 0;
	/** Tine start of work				*/
	protected long				p_startWork = 0;
	/** Number MS of last Run		*/
	private long 				m_runLastMS = 0;
	/** Number of MS total			*/
	private long 				m_runTotalMS = 0;
	/** When to run next			*/
	private long 				m_nextWork = 0;
	
	/**	Logger						*/
	protected Logger	log = Logger.getCLogger(getClass());
	/**	Context						*/
	private Properties	m_ctx = null;
	
	/**
	 * 	Get Server Context
	 *	@return context
	 */
	public Properties getCtx()
	{
		return m_ctx;
	}	//	getCtx
	
	/**
	 * @return Returns the sleepMS.
	 */
	public long getSleepMS ()
	{
		return m_sleepMS;
	}	//	getSleepMS
	
	
	/**
	 * 	Sleep for set time
	 *	@return true if not interrupted
	 */
	public boolean sleep()
	{
		if (isInterrupted())
		{
			log.info ("sleep - interrupted");
			return false;
		}
		log.debug ("sleep - " + TimeUtil.formatElapsed(m_sleepMS));
		m_sleeping = true;
		try
		{
			sleep (m_sleepMS);
		}
		catch (InterruptedException e)
		{
			log.info ("sleep - interrupted");
			m_sleeping = false;
			return false;
		}
		m_sleeping = false;
		return true;
	}	//	sleep

	
	/**************************************************************************
	 * 	Run
	 */
	public void run ()
	{
		try
		{
			log.debug ("run - pre-nap - " + m_initialNap);
			sleep (m_initialNap * 1000);
		}
		catch (InterruptedException e)
		{
			log.error("run - pre-nap interrupted", e);
			return;
		}
		
		m_start = System.currentTimeMillis();
		while (true)
		{
			if (m_nextWork == 0)
			{
				Timestamp dateNextRun = getDateNextRun(true);
				if (dateNextRun != null)
					m_nextWork = dateNextRun.getTime();
			}
			long now = System.currentTimeMillis();
			if (m_nextWork > now)
			{
				m_sleepMS = m_nextWork - now;
				if (!sleep ())
					break;
			}
			if (isInterrupted())
			{
				log.info ("run - interrupted");
				break;
			}
			
			//	---------------
			p_startWork = System.currentTimeMillis();
			doWork();
			now = System.currentTimeMillis();
			//	---------------
			
			p_runCount++;
			m_runLastMS = now - p_startWork;
			m_runTotalMS += m_runLastMS;
			//
			m_sleepMS = calculateSleep();
			m_nextWork = now + m_sleepMS;
			//
			p_model.setDateLastRun(new Timestamp(now));
			p_model.setDateNextRun(new Timestamp(m_nextWork));
			p_model.save();
			//
			log.debug("run - " + getName() + ": " + getStatistics());
			if (!sleep())
				break;
		}
		m_start = 0;
	}	//	run

	/**
	 * 	Get Run Statistics
	 *	@return Statistic info
	 */
	public String getStatistics()
	{
		return "Run #" + p_runCount 
			+ " - Last=" + TimeUtil.formatElapsed(m_runLastMS)
			+ " - Total=" + TimeUtil.formatElapsed(m_runTotalMS)
			+ " - Next " + TimeUtil.formatElapsed(m_nextWork - System.currentTimeMillis());
	}	//	getStatistics
	
	/**
	 * 	Do the actual Work
	 */
	protected abstract void doWork();

	/**
	 * 	Get Server Info
	 *	@return info
	 */
	public abstract String getServerInfo();

	/**
	 * 	Get Unique ID
	 *	@return Unique ID
	 */
	public String getServerID()
	{
		return p_model.getServerID();
	}	//	getServerID

	/**
	 * 	Get the date Next run
	 * 	@param requery requery database
	 * 	@return date next run
	 */
	public Timestamp getDateNextRun (boolean requery)
	{
		return p_model.getDateNextRun(requery);
	}	//	getDateNextRun
	
	/**
	 * 	Get the date Last run
	 * 	@return date lext run
	 */
	public Timestamp getDateLastRun ()
	{
		return p_model.getDateLastRun();
	}	//	getDateLastRun

	/**
	 * 	Get Description
	 *	@return Description
	 */
	public String getDescription()
	{
		return p_model.getDescription();
	}	//	getDescription
	
	/**
	 * 	Get Model
	 *	@return Model
	 */
	public CompiereProcessor getModel()
	{
		return p_model;
	}	//	getModel
	
	/**
	 * 	Calculate Sleep ms
	 *	@return miliseconds
	 */
	private long calculateSleep ()
	{
		String frequencyType = p_model.getFrequencyType();
		int frequency = p_model.getFrequency();
		if (frequency < 1)
			frequency = 1;
		//
		long typeSec = 600;			//	10 minutes
		if (frequencyType == null)
			typeSec = 300;			//	5 minutes
		else if (MRequestProcessor.FREQUENCYTYPE_Minute.equals(frequencyType))
			typeSec = 60;
		else if (MRequestProcessor.FREQUENCYTYPE_Hour.equals(frequencyType))
			typeSec = 3600;
		else if (MRequestProcessor.FREQUENCYTYPE_Day.equals(frequencyType))
			typeSec = 86400;
		//
		return typeSec * 1000 * frequency;		//	ms
	}	//	calculateSleep

	/**
	 * 	Is Sleeping
	 *	@return sleeping
	 */
	public boolean isSleeping()
	{
		return m_sleeping;
	}	//	isSleeping
	
	/**
	 * 	String Representation
	 *	@return info
	 */
	public String toString ()
	{
		StringBuffer sb = new StringBuffer (getName())
			.append (",Prio=").append(getPriority())
			.append (",").append (getThreadGroup())
			.append (",Alive=").append(isAlive())
			.append (",Sleeping=").append(m_sleeping)
			.append (",Last=").append(getDateLastRun());
		if (m_sleeping)
			sb.append (",Next=").append(getDateNextRun(false));
		return sb.toString ();
	}	//	toString

	/**
	 * 	Get Seconds Alive
	 *	@return seconds alive
	 */
	public int getSecondsAlive()
	{
		if (m_start == 0)
			return 0;
		long now = System.currentTimeMillis();
		long ms = (now-m_start) / 1000;
		return (int)ms;
	}	//	getSecondsAlive
	
	/**
	 * 	Get Start Time
	 *	@return start time
	 */
	public Timestamp getStartTime()
	{
		if (m_start == 0)
			return null;
		return new Timestamp (m_start);
	}	//	getStartTime
	
	/**
	 * 	Get Processor Logs
	 *	@return logs
	 */
	public CompiereProcessorLog[] getLogs()
	{
		return p_model.getLogs();
	}	//	getLogs

}	//	CompiereServer
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2004 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.server;

import java.sql.*;
import java.util.*;

import org.compiere.model.*;
import org.compiere.wf.*;
import org.compiere.util.*;

/**
 *	Compiere Server Base
 *	
 *  @author Jorg Janke
 *  @version $Id: CompiereServer.java,v 1.3 2004/09/09 14:21:04 jjanke Exp $
 */
public abstract class CompiereServer extends Thread
{
	/**
	 * 	Create New Server Thead
	 *	@param model model
	 *	@return server tread or null
	 */
	public static CompiereServer create (CompiereProcessor model)
	{
		if (model instanceof MRequestProcessor)
			return new RequestProcessor ((MRequestProcessor)model);
		if (model instanceof MWorkflowProcessor)
			return new WorkflowProcessor ((MWorkflowProcessor)model);
		if (model instanceof MAcctProcessor)
			return new AcctProcessor ((MAcctProcessor)model);
		if (model instanceof MAlertProcessor)
			return new AlertProcessor ((MAlertProcessor)model);
		if (model instanceof MScheduler)
			return new Scheduler ((MScheduler)model);
		throw new IllegalArgumentException("Unknown Processor");
	}	//	 create

	
	/**************************************************************************
	 * 	Server Base Class
	 * 	@param name name
	 *	@param initialNap delay time running in sec
	 */
	protected CompiereServer (CompiereProcessor model, int initialNap)
	{
		super (CompiereServerGroup.get(), null, model.getName(), 0);
		p_model = model;
		m_ctx = model.getCtx();
		m_initialNap = initialNap;
	//	log.info(model.getName() + " - " + getThreadGroup());
	}	//	ServerBase

	/**	The Processor Model						*/
	protected					CompiereProcessor 	p_model;
	/** Initial nap is seconds		*/
	private int					m_initialNap = 0;
	
	/**	Miliseconds to sleep - 10 Min default	*/
	private long				m_sleepMS = 600000;
	/** Sleeping					*/
	private volatile boolean	m_sleeping = false;
	/** Server start time					*/
	private long				m_start = 0;
	/** Number of Work executions	*/
	protected int 				p_runCount = 0;
	/** Tine start of work				*/
	protected long				p_startWork = 0;
	/** Number MS of last Run		*/
	private long 				m_runLastMS = 0;
	/** Number of MS total			*/
	private long 				m_runTotalMS = 0;
	/** When to run next			*/
	private long 				m_nextWork = 0;
	
	/**	Logger						*/
	protected Logger	log = Logger.getCLogger(getClass());
	/**	Context						*/
	private Properties	m_ctx = null;
	
	/**
	 * 	Get Server Context
	 *	@return context
	 */
	public Properties getCtx()
	{
		return m_ctx;
	}	//	getCtx
	
	/**
	 * @return Returns the sleepMS.
	 */
	public long getSleepMS ()
	{
		return m_sleepMS;
	}	//	getSleepMS
	
	
	/**
	 * 	Sleep for set time
	 *	@return true if not interrupted
	 */
	public boolean sleep()
	{
		if (isInterrupted())
		{
			log.info ("sleep - interrupted");
			return false;
		}
		log.debug ("sleep - " + TimeUtil.formatElapsed(m_sleepMS));
		m_sleeping = true;
		try
		{
			sleep (m_sleepMS);
		}
		catch (InterruptedException e)
		{
			log.info ("sleep - interrupted");
			m_sleeping = false;
			return false;
		}
		m_sleeping = false;
		return true;
	}	//	sleep

	
	/**************************************************************************
	 * 	Run
	 */
	public void run ()
	{
		try
		{
			log.debug ("run - pre-nap - " + m_initialNap);
			sleep (m_initialNap * 1000);
		}
		catch (InterruptedException e)
		{
			log.error("run - pre-nap interrupted", e);
			return;
		}
		
		m_start = System.currentTimeMillis();
		while (true)
		{
			if (m_nextWork == 0)
			{
				Timestamp dateNextRun = getDateNextRun(true);
				if (dateNextRun != null)
					m_nextWork = dateNextRun.getTime();
			}
			long now = System.currentTimeMillis();
			if (m_nextWork > now)
			{
				m_sleepMS = m_nextWork - now;
				if (!sleep ())
					break;
			}
			if (isInterrupted())
			{
				log.info ("run - interrupted");
				break;
			}
			
			//	---------------
			p_startWork = System.currentTimeMillis();
			doWork();
			now = System.currentTimeMillis();
			//	---------------
			
			p_runCount++;
			m_runLastMS = now - p_startWork;
			m_runTotalMS += m_runLastMS;
			//
			m_sleepMS = calculateSleep();
			m_nextWork = now + m_sleepMS;
			//
			p_model.setDateLastRun(new Timestamp(now));
			p_model.setDateNextRun(new Timestamp(m_nextWork));
			p_model.save();
			//
			log.debug("run - " + getName() + ": " + getStatistics());
			if (!sleep())
				break;
		}
		m_start = 0;
	}	//	run

	/**
	 * 	Get Run Statistics
	 *	@return Statistic info
	 */
	public String getStatistics()
	{
		return "Run #" + p_runCount 
			+ " - Last=" + TimeUtil.formatElapsed(m_runLastMS)
			+ " - Total=" + TimeUtil.formatElapsed(m_runTotalMS)
			+ " - Next " + TimeUtil.formatElapsed(m_nextWork - System.currentTimeMillis());
	}	//	getStatistics
	
	/**
	 * 	Do the actual Work
	 */
	protected abstract void doWork();

	/**
	 * 	Get Server Info
	 *	@return info
	 */
	public abstract String getServerInfo();

	/**
	 * 	Get Unique ID
	 *	@return Unique ID
	 */
	public String getServerID()
	{
		return p_model.getServerID();
	}	//	getServerID

	/**
	 * 	Get the date Next run
	 * 	@param requery requery database
	 * 	@return date next run
	 */
	public Timestamp getDateNextRun (boolean requery)
	{
		return p_model.getDateNextRun(requery);
	}	//	getDateNextRun
	
	/**
	 * 	Get the date Last run
	 * 	@return date lext run
	 */
	public Timestamp getDateLastRun ()
	{
		return p_model.getDateLastRun();
	}	//	getDateLastRun

	/**
	 * 	Get Description
	 *	@return Description
	 */
	public String getDescription()
	{
		return p_model.getDescription();
	}	//	getDescription
	
	/**
	 * 	Get Model
	 *	@return Model
	 */
	public CompiereProcessor getModel()
	{
		return p_model;
	}	//	getModel
	
	/**
	 * 	Calculate Sleep ms
	 *	@return miliseconds
	 */
	private long calculateSleep ()
	{
		String frequencyType = p_model.getFrequencyType();
		int frequency = p_model.getFrequency();
		if (frequency < 1)
			frequency = 1;
		//
		long typeSec = 600;			//	10 minutes
		if (frequencyType == null)
			typeSec = 300;			//	5 minutes
		else if (MRequestProcessor.FREQUENCYTYPE_Minute.equals(frequencyType))
			typeSec = 60;
		else if (MRequestProcessor.FREQUENCYTYPE_Hour.equals(frequencyType))
			typeSec = 3600;
		else if (MRequestProcessor.FREQUENCYTYPE_Day.equals(frequencyType))
			typeSec = 86400;
		//
		return typeSec * 1000 * frequency;		//	ms
	}	//	calculateSleep

	/**
	 * 	Is Sleeping
	 *	@return sleeping
	 */
	public boolean isSleeping()
	{
		return m_sleeping;
	}	//	isSleeping
	
	/**
	 * 	String Representation
	 *	@return info
	 */
	public String toString ()
	{
		StringBuffer sb = new StringBuffer (getName())
			.append (",Prio=").append(getPriority())
			.append (",").append (getThreadGroup())
			.append (",Alive=").append(isAlive())
			.append (",Sleeping=").append(m_sleeping)
			.append (",Last=").append(getDateLastRun());
		if (m_sleeping)
			sb.append (",Next=").append(getDateNextRun(false));
		return sb.toString ();
	}	//	toString

	/**
	 * 	Get Seconds Alive
	 *	@return seconds alive
	 */
	public int getSecondsAlive()
	{
		if (m_start == 0)
			return 0;
		long now = System.currentTimeMillis();
		long ms = (now-m_start) / 1000;
		return (int)ms;
	}	//	getSecondsAlive
	
	/**
	 * 	Get Start Time
	 *	@return start time
	 */
	public Timestamp getStartTime()
	{
		if (m_start == 0)
			return null;
		return new Timestamp (m_start);
	}	//	getStartTime
	
	/**
	 * 	Get Processor Logs
	 *	@return logs
	 */
	public CompiereProcessorLog[] getLogs()
	{
		return p_model.getLogs();
	}	//	getLogs

}	//	CompiereServer
