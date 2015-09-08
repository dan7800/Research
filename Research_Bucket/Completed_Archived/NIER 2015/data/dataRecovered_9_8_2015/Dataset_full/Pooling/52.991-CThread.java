/*-*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 0 -*-*/
/*.IH,CThread,======================================*/
/*.IC,--- COPYRIGHT (c) --  Open ebXML - 2001 ---

     The contents of this file are subject to the Open ebXML Public License
     Version 1.0 (the "License"); you may not use this file except in
     compliance with the License. You may obtain a copy of the License at
     'http://www.openebxml.org/LICENSE/OpenebXML-LICENSE-1.0.txt'

     Software distributed under the License is distributed on an "AS IS"
     basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
     License for the specific language governing rights and limitations
     under the License.

     The Initial Developer of the Original Code is Anders W. Tell.
     Portions created by Financial Toolsmiths AB are Copyright (C) 
     Financial Toolsmiths AB 1993-2001. All Rights Reserved.

     Contributor(s): see author tag.

---------------------------------------------------------------------*/
/*.IA,	PUBLIC Include File CThread.java			*/
package org.openebxml.comp.util;


/************************************************
	Includes
\************************************************/

/**
 *  Low level thread maintained by a ThreadPoolLane.
 *
 * @author Anders W. Tell   Financial Toolsmiths AB
 * @version $Id: CThread.java,v 1.2 2001/10/21 09:47:52 awtopensource Exp $
 */

public class CThread  extends Thread {

	/**
	 * Reference to owner ThreadPoolLane
	 */
    protected ThreadPoolLane  		fThreadPoolLane;

	/**
	 * Lane indentifier
	 */
	protected int				fLaneID;

	/**
	 * Thread identifier
	 */
	protected int				fID;

	/**
	 * Forms an intrusive linked list maintained by owned ThreadPoolLane.
	 * Reference to next Thread
	 */
    protected CThread 			fNext;

	/**
	 * Forms an intrusive linked list maintained by owned ThreadPoolLane.
	 * Reference to previous Thread
	 *
	 */
    protected CThread 			fPrev;

	/**
	 * Reference to Workload object as asigned through Lane and Pool.While running the work this variable is set no null and the referese is kept on stack..
	 */
    protected Runnable     		fWorkAssigned;

	/**
	 * Total number of assigned workloads to this thread
	 */
    protected int      			fWorkAssignedNo;

	/**
	 * TRUE if Thread is actually running.
	 */
    protected boolean      		fRunning;

	/**
	 * TRUE if Thread is marked as Terminated. For use as flag by Lane and Pool
	 */
    protected boolean      		fIsTerminated;

	/**
	 * Flag to facilitate start of Thread only when work has been assigned or otherwise explicitly started.
	 */
    protected boolean      		fIsStarted;


    /**
	 * Contructor
	 */
    public CThread(ThreadPoolLane lane, int laneID,int id, int priority) 
    {
		super(lane.getThreadGroup(), 
			  lane.getThreadGroup().getName()  +":"+laneID  +":"+id);

		fThreadPoolLane = lane;
		setPriority(priority);
		fLaneID	= laneID;
		fID		= id;



		fNext       	= null;
		fPrev       	= null;

		fRunning      	= true;
		fIsTerminated 	= false;
		fIsStarted    	= false;

		fWorkAssigned	= null;
		fWorkAssignedNo	 = 0;

		setDaemon(true);
    }


    /**
	 * Get the value of Running.
	 * @return value of Running.
	 */
	public synchronized boolean isRunning() {
		return fRunning;
	}
	

	/**
	 * Marks current Thread as terminated. Called by Lane and Pool.
	 * @return TRUE is Thread was terminated earlier, FALSE if already terminated
	 */
    public synchronized boolean MarkTerminate() 
    {
		boolean lbT 	= fIsTerminated;
		fIsTerminated 	= true;
		return lbT;
    }

	/**
	 * Collects work assigned through Lane and Pool
	 */
    protected synchronized Runnable WaitForWork() 
    {
		boolean lbGraceExpired = false;
		
		while ( fRunning ) 
			{
			/* check if any work has been assigned  */
			if ( fWorkAssigned != null ) 
				{
				Runnable lNew = fWorkAssigned;
				fWorkAssigned   = null;
				fWorkAssignedNo++;
				return lNew;
				} 

			/* Perform work at least once */
			else if ( fWorkAssignedNo == 0 ) 
				{
				/*.TODO add timeout */
				try {wait();} catch (InterruptedException ex) {	}
				} 

			/* Checkin with lane and Pool to see if Thread should continue */
			else if ( fRunning = fThreadPoolLane.Available(this,lbGraceExpired) ) 
				{
				int liWait = fThreadPoolLane.getGracePeriod();
				lbGraceExpired = false;
				try 
					{
					if ( liWait > 0 ) 
						{
						wait(liWait);

						/* Check if work was assigned */
						if( fWorkAssigned == null )
							lbGraceExpired = true;
						else
							lbGraceExpired = false;
						} 
					else 
						{
						wait();
						}
					} 
				catch (InterruptedException ex) {}
				}/*if handlegrace*/
			}
		return null;
    }


	/**
	 * Starts execution of Thread, with or without and work to do. Calls superclass start() method.
	 */
    public synchronized void start() 
    {
		super.start();
		fIsStarted = true;
    }

	/**
	 *	Performes an ordely close of this Thread.
	 */
    public synchronized void close() 
    {
		fRunning = false;
		notify();
    }


	/**
	 *  Primary Thread loop.
	 */
    public void run() 
    {
		try 
			{
			while ( fRunning ) 
				{
				/* Allocate work for the Thread */
				Runnable lWork = WaitForWork();

				if ( lWork != null ) 
					{
					lWork.run();
					}
				}
			} 
		finally 
			{
			/* Notify owner Lane and Pool that Thread has died */
			fThreadPoolLane.NotifyThreadDied(this);
			}
    }


	/**
	 * Assign some work to this Thread. Called by managing Lane and Pool.
	 */
    public synchronized boolean Assign(Runnable work) 
    {
		if ( fRunning ) 
			{
			fWorkAssigned = work;
			if ( ! fIsStarted ) 
				{
				start();
				}
			notify();
			return true;
			} 
		return false;
    }


}


/*.IEnd,CThread,====================================*/
