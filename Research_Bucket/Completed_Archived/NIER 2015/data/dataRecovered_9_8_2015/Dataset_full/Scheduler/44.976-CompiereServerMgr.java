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

import java.util.*;
import java.sql.*;

import org.compiere.*;
import org.compiere.wf.*;
import org.compiere.model.*;
import org.compiere.util.*;

/**
 *	Compiere Server Manager
 *	
 *  @author Jorg Janke
 *  @version $Id: CompiereServerMgr.java,v 1.3 2004/09/09 14:21:05 jjanke Exp $
 */
public class CompiereServerMgr
{
	/**
	 * 	Get Compiere Server Manager
	 *	@return mgr
	 * @throws InterruptedException
	 */
	public static CompiereServerMgr get()
	{
		if (m_serverMgr == null)
		{
			//	for faster subsequent calls
			m_serverMgr = new CompiereServerMgr();
			m_serverMgr.startServers();
			m_serverMgr.log.info(m_serverMgr);
		}
		return m_serverMgr;
	}	//	get
	
	/**	Singleton					*/
	private static	CompiereServerMgr	m_serverMgr = null;
	/**	Logger			*/
	protected Logger	log = Logger.getCLogger(getClass());
	
	/**************************************************************************
	 * 	Compiere Server Manager
	 */
	private CompiereServerMgr ()
	{
		super();
		startEnvironment();
	//	m_serverMgr.startServers();
	}	//	CompiereServerMgr

	/**	The Servers				*/
	private ArrayList		m_servers = new ArrayList();
	/** Context					*/
	private Properties		m_ctx = Env.getCtx();
	/** Start					*/
	private Timestamp		m_start = new Timestamp(System.currentTimeMillis());

	/**
	 * 	Start Environment
	 *	@return true if started
	 */
	private boolean startEnvironment()
	{
		Compiere.startupServer();
		log.info("startEnvironment");
		PO.setDocWorkflowMgr (DocWorkflowManager.get());

		//	Set Environment
		
		//
		return true;
	}	//	startEnvironment
	
	/**
	 * 	Start Environment
	 *	@return true if started
	 */
	private boolean startServers()
	{
		log.info("startServers");
		int noServers = 0;
		//	Accounting
		MAcctProcessor[] acctModels = MAcctProcessor.getActive(m_ctx);
		for (int i = 0; i < acctModels.length; i++)
		{
			MAcctProcessor pModel = acctModels[i];
			CompiereServer server = CompiereServer.create(pModel);
			server.start();
			server.setPriority(Thread.NORM_PRIORITY-2);
			m_servers.add(server);
		}		
		//	Request
		MRequestProcessor[] requestModels = MRequestProcessor.getActive(m_ctx);
		for (int i = 0; i < requestModels.length; i++)
		{
			MRequestProcessor pModel = requestModels[i];
			CompiereServer server = CompiereServer.create(pModel);
			server.start();
			server.setPriority(Thread.NORM_PRIORITY-2);
			m_servers.add(server);
		}
		//	Workflow
		MWorkflowProcessor[] workflowModels = MWorkflowProcessor.getActive(m_ctx);
		for (int i = 0; i < workflowModels.length; i++)
		{
			MWorkflowProcessor pModel = workflowModels[i];
			CompiereServer server = CompiereServer.create(pModel);
			server.start();
			server.setPriority(Thread.NORM_PRIORITY-2);
			m_servers.add(server);
		}		
		//	Alert
		MAlertProcessor[] alertModels = MAlertProcessor.getActive(m_ctx);
		for (int i = 0; i < alertModels.length; i++)
		{
			MAlertProcessor pModel = alertModels[i];
			CompiereServer server = CompiereServer.create(pModel);
			server.start();
			server.setPriority(Thread.NORM_PRIORITY-2);
			m_servers.add(server);
		}		
		//	Scheduler
		MScheduler[] schedulerModels = MScheduler.getActive(m_ctx);
		for (int i = 0; i < schedulerModels.length; i++)
		{
			MScheduler pModel = schedulerModels[i];
			CompiereServer server = CompiereServer.create(pModel);
			server.start();
			server.setPriority(Thread.NORM_PRIORITY-2);
			m_servers.add(server);
		}		
		
		log.debug ("startServers - #" + noServers);
		return startAll();
	}	//	startEnvironment

	/**
	 * 	Get Server Context
	 *	@return ctx
	 */
	public Properties getCtx()
	{
		return m_ctx;
	}	//	getCtx
	
	/**
	 * 	Start all servers
	 *	@return true if started
	 */
	public boolean startAll()
	{
		log.info ("startAll");
		CompiereServer[] servers = getInActive();
		for (int i = 0; i < servers.length; i++)
		{
			CompiereServer server = servers[i];
			try
			{
				if (server.isAlive())
					continue;
				//	Wait until dead
				if (server.isInterrupted())
				{
					int maxWait = 10;	//	10 iterations = 1 sec
					while (server.isAlive())
					{
						if (maxWait-- == 0)
						{
							log.error ("startAll - Wait timeout for interruped " + server);
							break;
						}
						try
						{
							Thread.sleep(100);		//	1/10 sec
						}
						catch (InterruptedException e)
						{
							log.error("startAll - while sleeping", e);
						}
					}
				}
				//	Do start
				if (!server.isAlive())
				{
					//	replace
					server = CompiereServer.create (server.getModel());
					if (server == null)
						m_servers.remove(i);
					else
						m_servers.set(i, server);
					server.start();
					server.setPriority(Thread.NORM_PRIORITY-2);
				}
			}
			catch (Exception e)
			{
				log.error("startAll - " + server, e);
			}
		}	//	for all servers
		
		//	Final Check
		int noRunning = 0;
		int noStopped = 0;
		for (int i = 0; i < servers.length; i++)
		{
			CompiereServer server = servers[i];
			try
			{
				if (server.isAlive())
				{
					log.info("startAll - " + server);
					noRunning++;
				}
				else
				{
					log.warn("startAll - " + server);
					noStopped++;
				}
			}
			catch (Exception e)
			{
				log.error("startAll (checking) - " + server, e);
				noStopped++;
			}
		}
		log.debug ("startAll - Running=" + noRunning + ", Stopped=" + noStopped);
		CompiereServerGroup.get().dump();
		return noStopped == 0;
	}	//	startAll

	/**
	 * 	Start Server if not started yet
	 *	@return true if started
	 */
	public boolean start (String serverID)
	{
		CompiereServer server = getServer(serverID);
		if (server == null)
			return false;
		if (server.isAlive())
			return true;
		
		try
		{
			//	replace
			int index = m_servers.indexOf(server);
			server = CompiereServer.create (server.getModel());
			if (server == null)
				m_servers.remove(index);
			else
				m_servers.set(index, server);
			server.start();
			server.setPriority(Thread.NORM_PRIORITY-2);
			Thread.yield();
		}
		catch (Exception e)
		{
			log.error("start - " + serverID, e);
			return false;
		}
		log.info("start - " + server);
		CompiereServerGroup.get().dump();
		if (server == null)
			return false;
		return server.isAlive();
	}	//	startIt
	
	/**
	 * 	Stop all Servers
	 *	@return true if stopped
	 */
	public boolean stopAll()
	{
		log.info ("stop");
		CompiereServer[] servers = getActive();
		//	Interrupt
		for (int i = 0; i < servers.length; i++)
		{
			CompiereServer server = servers[i];
			try
			{
				if (server.isAlive() && !server.isInterrupted())
				{
					server.setPriority(Thread.MAX_PRIORITY-1);
					server.interrupt();
				}
			}
			catch (Exception e)
			{
				log.error("stopAll (interrupting) - " + server, e);
			}
		}	//	for all servers
		Thread.yield();
		
		//	Wait for death
		for (int i = 0; i < servers.length; i++)
		{
			CompiereServer server = servers[i];
			try
			{
				int maxWait = 10;	//	10 iterations = 1 sec
				while (server.isAlive())
				{
					if (maxWait-- == 0)
					{
						log.error ("stopAll - Wait timeout for interruped " + server);
						break;
					}
					Thread.sleep(100);		//	1/10
				}
			}
			catch (Exception e)
			{
				log.error("stopAll (waiting) - " + server, e);
			}
		}	//	for all servers
		
		//	Final Check
		int noRunning = 0;
		int noStopped = 0;
		for (int i = 0; i < servers.length; i++)
		{
			CompiereServer server = servers[i];
			try
			{
				if (server.isAlive())
				{
					log.warn ("stopAll - " + server);
					noRunning++;
				}
				else
				{
					log.info ("stopAll - " + server);
					noStopped++;
				}
			}
			catch (Exception e)
			{
				log.error("stop (checking) - " + server, e);
				noRunning++;
			}
		}
		log.debug ("stopAll - Running=" + noRunning + ", Stopped=" + noStopped);
		CompiereServerGroup.get().dump();
		return noRunning == 0;
	}	//	stopAll

	/**
	 * 	Stop Server if not stopped
	 *	@return true if interrupted
	 */
	public boolean stop (String serverID)
	{
		CompiereServer server = getServer(serverID);
		if (server == null)
			return false;
		if (!server.isAlive())
			return true;

		try
		{
			server.interrupt();
			Thread.sleep(10);	//	1/100 sec
		}
		catch (Exception e)
		{
			log.error("stop", e);
			return false;
		}
		log.info("stop - " + server);
		CompiereServerGroup.get().dump();
		return !server.isAlive();
	}	//	stop

	
	/**
	 * 	Destroy
	 */
	public void destroy ()
	{
		log.info ("destroy");
		stopAll();
		m_servers.clear();
	}	//	destroy

	/**
	 * 	Get Active Servers
	 *	@return array of active servers
	 */
	protected CompiereServer[] getActive()
	{
		ArrayList list = new ArrayList();
		for (int i = 0; i < m_servers.size(); i++)
		{
			CompiereServer server = (CompiereServer)m_servers.get(i);
			if (server != null && server.isAlive() && !server.isInterrupted())
				list.add (server);
		}
		CompiereServer[] retValue = new CompiereServer[list.size ()];
		list.toArray (retValue);
		return retValue;
	}	//	getActive
	
	/**
	 * 	Get InActive Servers
	 *	@return array of inactive servers
	 */
	protected CompiereServer[] getInActive()
	{
		ArrayList list = new ArrayList();
		for (int i = 0; i < m_servers.size(); i++)
		{
			CompiereServer server = (CompiereServer)m_servers.get(i);
			if (server != null && (!server.isAlive() || !server.isInterrupted()))
				list.add (server);
		}
		CompiereServer[] retValue = new CompiereServer[list.size ()];
		list.toArray (retValue);
		return retValue;
	}	//	getInActive

	/**
	 * 	Get all Servers
	 *	@return array of servers
	 */
	public CompiereServer[] getAll()
	{
		CompiereServer[] retValue = new CompiereServer[m_servers.size ()];
		m_servers.toArray (retValue);
		return retValue;
	}	//	getAll
	
	/**
	 * 	Get Server with ID
	 *	@param serverID server id
	 *	@return server or null
	 */
	public CompiereServer getServer (String serverID)
	{
		if (serverID == null)
			return null;
		for (int i = 0; i < m_servers.size(); i++)
		{
			CompiereServer server = (CompiereServer)m_servers.get(i);
			if (serverID.equals(server.getServerID()))
				return server;
		}
		return null;
	}	//	getServer
	
	/**
	 * 	String Representation
	 *	@return info
	 */
	public String toString ()
	{
		StringBuffer sb = new StringBuffer ("CompiereServerMgr[");
		sb.append("Servers=").append(m_servers.size())
			.append(",ContextSize=").append(m_ctx.size())
			.append(",Started=").append(m_start)
			.append ("]");
		return sb.toString ();
	}	//	toString
	
	/**
	 * 	Get Description
	 *	@return description
	 */
	public String getDescription()
	{
		return "$Revision: 1.3 $";
	}	//	getDescription
	
	/**
	 * 	Get Number Servers
	 *	@return no of servers
	 */
	public String getServerCount()
	{
		int noRunning = 0;
		int noStopped = 0;
		for (int i = 0; i < m_servers.size(); i++)
		{
			CompiereServer server = (CompiereServer)m_servers.get(i);
			if (server.isAlive())
				noRunning++;
			else
				noStopped++;
		}
		String info = String.valueOf(m_servers.size())
			+ " - Running=" + noRunning
			+ " - Stopped=" + noStopped;
		return info;
	}	//	getServerCount
	
	/**
	 * 	Get start date
	 *	@return start date
	 */
	public Timestamp getStartTime()
	{
		return m_start;
	}	//	getStartTime

}	//	CompiereServerMgr
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

import java.util.*;
import java.sql.*;

import org.compiere.*;
import org.compiere.wf.*;
import org.compiere.model.*;
import org.compiere.util.*;

/**
 *	Compiere Server Manager
 *	
 *  @author Jorg Janke
 *  @version $Id: CompiereServerMgr.java,v 1.3 2004/09/09 14:21:05 jjanke Exp $
 */
public class CompiereServerMgr
{
	/**
	 * 	Get Compiere Server Manager
	 *	@return mgr
	 * @throws InterruptedException
	 */
	public static CompiereServerMgr get()
	{
		if (m_serverMgr == null)
		{
			//	for faster subsequent calls
			m_serverMgr = new CompiereServerMgr();
			m_serverMgr.startServers();
			m_serverMgr.log.info(m_serverMgr);
		}
		return m_serverMgr;
	}	//	get
	
	/**	Singleton					*/
	private static	CompiereServerMgr	m_serverMgr = null;
	/**	Logger			*/
	protected Logger	log = Logger.getCLogger(getClass());
	
	/**************************************************************************
	 * 	Compiere Server Manager
	 */
	private CompiereServerMgr ()
	{
		super();
		startEnvironment();
	//	m_serverMgr.startServers();
	}	//	CompiereServerMgr

	/**	The Servers				*/
	private ArrayList		m_servers = new ArrayList();
	/** Context					*/
	private Properties		m_ctx = Env.getCtx();
	/** Start					*/
	private Timestamp		m_start = new Timestamp(System.currentTimeMillis());

	/**
	 * 	Start Environment
	 *	@return true if started
	 */
	private boolean startEnvironment()
	{
		Compiere.startupServer();
		log.info("startEnvironment");
		PO.setDocWorkflowMgr (DocWorkflowManager.get());

		//	Set Environment
		
		//
		return true;
	}	//	startEnvironment
	
	/**
	 * 	Start Environment
	 *	@return true if started
	 */
	private boolean startServers()
	{
		log.info("startServers");
		int noServers = 0;
		//	Accounting
		MAcctProcessor[] acctModels = MAcctProcessor.getActive(m_ctx);
		for (int i = 0; i < acctModels.length; i++)
		{
			MAcctProcessor pModel = acctModels[i];
			CompiereServer server = CompiereServer.create(pModel);
			server.start();
			server.setPriority(Thread.NORM_PRIORITY-2);
			m_servers.add(server);
		}		
		//	Request
		MRequestProcessor[] requestModels = MRequestProcessor.getActive(m_ctx);
		for (int i = 0; i < requestModels.length; i++)
		{
			MRequestProcessor pModel = requestModels[i];
			CompiereServer server = CompiereServer.create(pModel);
			server.start();
			server.setPriority(Thread.NORM_PRIORITY-2);
			m_servers.add(server);
		}
		//	Workflow
		MWorkflowProcessor[] workflowModels = MWorkflowProcessor.getActive(m_ctx);
		for (int i = 0; i < workflowModels.length; i++)
		{
			MWorkflowProcessor pModel = workflowModels[i];
			CompiereServer server = CompiereServer.create(pModel);
			server.start();
			server.setPriority(Thread.NORM_PRIORITY-2);
			m_servers.add(server);
		}		
		//	Alert
		MAlertProcessor[] alertModels = MAlertProcessor.getActive(m_ctx);
		for (int i = 0; i < alertModels.length; i++)
		{
			MAlertProcessor pModel = alertModels[i];
			CompiereServer server = CompiereServer.create(pModel);
			server.start();
			server.setPriority(Thread.NORM_PRIORITY-2);
			m_servers.add(server);
		}		
		//	Scheduler
		MScheduler[] schedulerModels = MScheduler.getActive(m_ctx);
		for (int i = 0; i < schedulerModels.length; i++)
		{
			MScheduler pModel = schedulerModels[i];
			CompiereServer server = CompiereServer.create(pModel);
			server.start();
			server.setPriority(Thread.NORM_PRIORITY-2);
			m_servers.add(server);
		}		
		
		log.debug ("startServers - #" + noServers);
		return startAll();
	}	//	startEnvironment

	/**
	 * 	Get Server Context
	 *	@return ctx
	 */
	public Properties getCtx()
	{
		return m_ctx;
	}	//	getCtx
	
	/**
	 * 	Start all servers
	 *	@return true if started
	 */
	public boolean startAll()
	{
		log.info ("startAll");
		CompiereServer[] servers = getInActive();
		for (int i = 0; i < servers.length; i++)
		{
			CompiereServer server = servers[i];
			try
			{
				if (server.isAlive())
					continue;
				//	Wait until dead
				if (server.isInterrupted())
				{
					int maxWait = 10;	//	10 iterations = 1 sec
					while (server.isAlive())
					{
						if (maxWait-- == 0)
						{
							log.error ("startAll - Wait timeout for interruped " + server);
							break;
						}
						try
						{
							Thread.sleep(100);		//	1/10 sec
						}
						catch (InterruptedException e)
						{
							log.error("startAll - while sleeping", e);
						}
					}
				}
				//	Do start
				if (!server.isAlive())
				{
					//	replace
					server = CompiereServer.create (server.getModel());
					if (server == null)
						m_servers.remove(i);
					else
						m_servers.set(i, server);
					server.start();
					server.setPriority(Thread.NORM_PRIORITY-2);
				}
			}
			catch (Exception e)
			{
				log.error("startAll - " + server, e);
			}
		}	//	for all servers
		
		//	Final Check
		int noRunning = 0;
		int noStopped = 0;
		for (int i = 0; i < servers.length; i++)
		{
			CompiereServer server = servers[i];
			try
			{
				if (server.isAlive())
				{
					log.info("startAll - " + server);
					noRunning++;
				}
				else
				{
					log.warn("startAll - " + server);
					noStopped++;
				}
			}
			catch (Exception e)
			{
				log.error("startAll (checking) - " + server, e);
				noStopped++;
			}
		}
		log.debug ("startAll - Running=" + noRunning + ", Stopped=" + noStopped);
		CompiereServerGroup.get().dump();
		return noStopped == 0;
	}	//	startAll

	/**
	 * 	Start Server if not started yet
	 *	@return true if started
	 */
	public boolean start (String serverID)
	{
		CompiereServer server = getServer(serverID);
		if (server == null)
			return false;
		if (server.isAlive())
			return true;
		
		try
		{
			//	replace
			int index = m_servers.indexOf(server);
			server = CompiereServer.create (server.getModel());
			if (server == null)
				m_servers.remove(index);
			else
				m_servers.set(index, server);
			server.start();
			server.setPriority(Thread.NORM_PRIORITY-2);
			Thread.yield();
		}
		catch (Exception e)
		{
			log.error("start - " + serverID, e);
			return false;
		}
		log.info("start - " + server);
		CompiereServerGroup.get().dump();
		if (server == null)
			return false;
		return server.isAlive();
	}	//	startIt
	
	/**
	 * 	Stop all Servers
	 *	@return true if stopped
	 */
	public boolean stopAll()
	{
		log.info ("stop");
		CompiereServer[] servers = getActive();
		//	Interrupt
		for (int i = 0; i < servers.length; i++)
		{
			CompiereServer server = servers[i];
			try
			{
				if (server.isAlive() && !server.isInterrupted())
				{
					server.setPriority(Thread.MAX_PRIORITY-1);
					server.interrupt();
				}
			}
			catch (Exception e)
			{
				log.error("stopAll (interrupting) - " + server, e);
			}
		}	//	for all servers
		Thread.yield();
		
		//	Wait for death
		for (int i = 0; i < servers.length; i++)
		{
			CompiereServer server = servers[i];
			try
			{
				int maxWait = 10;	//	10 iterations = 1 sec
				while (server.isAlive())
				{
					if (maxWait-- == 0)
					{
						log.error ("stopAll - Wait timeout for interruped " + server);
						break;
					}
					Thread.sleep(100);		//	1/10
				}
			}
			catch (Exception e)
			{
				log.error("stopAll (waiting) - " + server, e);
			}
		}	//	for all servers
		
		//	Final Check
		int noRunning = 0;
		int noStopped = 0;
		for (int i = 0; i < servers.length; i++)
		{
			CompiereServer server = servers[i];
			try
			{
				if (server.isAlive())
				{
					log.warn ("stopAll - " + server);
					noRunning++;
				}
				else
				{
					log.info ("stopAll - " + server);
					noStopped++;
				}
			}
			catch (Exception e)
			{
				log.error("stop (checking) - " + server, e);
				noRunning++;
			}
		}
		log.debug ("stopAll - Running=" + noRunning + ", Stopped=" + noStopped);
		CompiereServerGroup.get().dump();
		return noRunning == 0;
	}	//	stopAll

	/**
	 * 	Stop Server if not stopped
	 *	@return true if interrupted
	 */
	public boolean stop (String serverID)
	{
		CompiereServer server = getServer(serverID);
		if (server == null)
			return false;
		if (!server.isAlive())
			return true;

		try
		{
			server.interrupt();
			Thread.sleep(10);	//	1/100 sec
		}
		catch (Exception e)
		{
			log.error("stop", e);
			return false;
		}
		log.info("stop - " + server);
		CompiereServerGroup.get().dump();
		return !server.isAlive();
	}	//	stop

	
	/**
	 * 	Destroy
	 */
	public void destroy ()
	{
		log.info ("destroy");
		stopAll();
		m_servers.clear();
	}	//	destroy

	/**
	 * 	Get Active Servers
	 *	@return array of active servers
	 */
	protected CompiereServer[] getActive()
	{
		ArrayList list = new ArrayList();
		for (int i = 0; i < m_servers.size(); i++)
		{
			CompiereServer server = (CompiereServer)m_servers.get(i);
			if (server != null && server.isAlive() && !server.isInterrupted())
				list.add (server);
		}
		CompiereServer[] retValue = new CompiereServer[list.size ()];
		list.toArray (retValue);
		return retValue;
	}	//	getActive
	
	/**
	 * 	Get InActive Servers
	 *	@return array of inactive servers
	 */
	protected CompiereServer[] getInActive()
	{
		ArrayList list = new ArrayList();
		for (int i = 0; i < m_servers.size(); i++)
		{
			CompiereServer server = (CompiereServer)m_servers.get(i);
			if (server != null && (!server.isAlive() || !server.isInterrupted()))
				list.add (server);
		}
		CompiereServer[] retValue = new CompiereServer[list.size ()];
		list.toArray (retValue);
		return retValue;
	}	//	getInActive

	/**
	 * 	Get all Servers
	 *	@return array of servers
	 */
	public CompiereServer[] getAll()
	{
		CompiereServer[] retValue = new CompiereServer[m_servers.size ()];
		m_servers.toArray (retValue);
		return retValue;
	}	//	getAll
	
	/**
	 * 	Get Server with ID
	 *	@param serverID server id
	 *	@return server or null
	 */
	public CompiereServer getServer (String serverID)
	{
		if (serverID == null)
			return null;
		for (int i = 0; i < m_servers.size(); i++)
		{
			CompiereServer server = (CompiereServer)m_servers.get(i);
			if (serverID.equals(server.getServerID()))
				return server;
		}
		return null;
	}	//	getServer
	
	/**
	 * 	String Representation
	 *	@return info
	 */
	public String toString ()
	{
		StringBuffer sb = new StringBuffer ("CompiereServerMgr[");
		sb.append("Servers=").append(m_servers.size())
			.append(",ContextSize=").append(m_ctx.size())
			.append(",Started=").append(m_start)
			.append ("]");
		return sb.toString ();
	}	//	toString
	
	/**
	 * 	Get Description
	 *	@return description
	 */
	public String getDescription()
	{
		return "$Revision: 1.3 $";
	}	//	getDescription
	
	/**
	 * 	Get Number Servers
	 *	@return no of servers
	 */
	public String getServerCount()
	{
		int noRunning = 0;
		int noStopped = 0;
		for (int i = 0; i < m_servers.size(); i++)
		{
			CompiereServer server = (CompiereServer)m_servers.get(i);
			if (server.isAlive())
				noRunning++;
			else
				noStopped++;
		}
		String info = String.valueOf(m_servers.size())
			+ " - Running=" + noRunning
			+ " - Stopped=" + noStopped;
		return info;
	}	//	getServerCount
	
	/**
	 * 	Get start date
	 *	@return start date
	 */
	public Timestamp getStartTime()
	{
		return m_start;
	}	//	getStartTime

}	//	CompiereServerMgr
