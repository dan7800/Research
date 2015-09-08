/*-*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 0 -*-*/
/*.IH,ThreadPoolLane,======================================*/
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
/*.IA,	PUBLIC Include File ThreadPoolLane.java			*/
package org.openebxml.comp.util;


/************************************************
	Includes
\************************************************/

/**
 *  Contains a pool threads for a given priority.
 *
 * @author Anders W. Tell   Financial Toolsmiths AB
 * @version $Id: ThreadPoolLane.java,v 1.2 2001/10/21 09:47:52 awtopensource Exp $
 */

public class ThreadPoolLane {

	/****/
    public static final int DEFAULT_THREADS_MINIMUM  = 1;
	
	/****/
    public static final int DEFAULT_THREADS_MAXIMUM  = 10;
	
	/****/
    public static final int DEFAULT_GRACEPERIOD		= 10000;
	
	/*--------------------------------------------------------------------*/

	/**
	 * Reference Pool that owns this Lane
	 */
	ThreadPool				fThreadPool;

    /**
     * ThreadGroup for this Lane.
     */
    protected ThreadGroup 	fThreadGroup;

	/****/
	int						fID;

    /**
     * Ever increasing CThread identifier.
     */
    protected int 			fIdentifierLast;


	/**
	 *	Priority for thread in this Lane.
	 */
	protected int 		fPriority;
	
	/**
     * References header of linked list with free and availiable threads
     */
    protected CThread fFreeList_Header;

	/**
     * References tail of linked list with free and availiable threads
     */
    protected CThread fFreeList_Tail;

    /**
     * Contains the actual number of created Threads in Lane.
     */
    protected int 			fCreatedThreadsNo;

    /**
     * Contains the number of threads with work assigned to them
     */
    protected int 			fAssignedThreads;


	/****/
    protected int 			fMinimumNoThreads;

	/**
	 * Maximum number of Threads in Lane
	 */
    protected int 			fMaximumNoThreads;

	/**
	 * Indicates if Lane has fixed size or can expand
	 */
	boolean 				fFixedSize;

	/**
	 * Grace period before idle threads are terminated. Measured in miliseconds
	 */
	int 					fGracePeriod;
	
    /****/
    public ThreadPoolLane(ThreadPool pool,ThreadGroup threadGroup,
						  int id, int priority, 
						  int mimimumThreads,int maximumThreads)
    {
		fThreadPool			= pool;
		fThreadGroup 		= threadGroup;
		fID					= id;
		fPriority			= priority;
		fIdentifierLast 	= 0;
		fCreatedThreadsNo 	= 0;
		fAssignedThreads 	= 0;
		fMinimumNoThreads	= mimimumThreads;
		fMaximumNoThreads	= maximumThreads;
		fFixedSize			= false;
		fGracePeriod		= DEFAULT_GRACEPERIOD;

		/* Preallocate minomum amount of CThreads */
		CThread lNew = makeCThread();

		fFreeList_Header 	= lNew;
		fFreeList_Tail 		= lNew;

		for (int i = 1 ; i < fMinimumNoThreads ; i++) 
			{
			lNew		    = makeCThread();

			/* put last in free list */
			lNew.fPrev      		= fFreeList_Tail;
			fFreeList_Tail.fNext	= lNew;
			fFreeList_Tail        	= lNew;
			}/*for*/
    }


 	
	/**
	 * Get the value of ThreadPool.
	 * @return value of ThreadPool.
	 */
	public synchronized ThreadPool getThreadPool() {
		return fThreadPool;
	}

	/**
	 * Get the value of ThreadGroup.
	 * @return value of ThreadGroup.
	 */
	public synchronized ThreadGroup getThreadGroup() {
		return fThreadGroup;
	}
	

	/**
	 * Get the value of Priority.
	 * @return value of Priority.
	 */
	public synchronized int getPriority() {
		return fPriority;
	}

	
	/**
	 * Get the value of MinimumNoThreads.
	 * @return value of MinimumNoThreads.
	 */
	public synchronized int getMinimumNoThreads() {
		return fMinimumNoThreads;
	}
	
	/**
	 * Set the value of MinimumNoThreads.
	 * @param v  Value to assign to MinimumNoThreads.
	 */
	public synchronized void setMinimumNoThreads(int  v) {
		this.fMinimumNoThreads = v;
	}
	
	
	/**
	 * Get the value of MaximumNoThreads.
	 * @return value of MaximumNoThreads.
	 */
	public synchronized int getMaximumNoThreads() {
		return fMaximumNoThreads;
	}
	
	/**
	 * Set the value of MaximumNoThreads.
	 * @param v  Value to assign to MaximumNoThreads.
	 */
	public synchronized void setMaximumNoThreads(int  v) {
		this.fMaximumNoThreads = v;
	}
	
	
	/**
	 * Get the value of FixedSize.
	 * @return value of FixedSize.
	 */
	public synchronized boolean isFixedSize() {
		return fFixedSize;
	}
	
	/**
	 * Set the value of FixedSize.
	 * @param v  Value to assign to FixedSize.
	 */
	public synchronized void setFixedSize(boolean  v) {
		this.fFixedSize = v;
	}
	
	/**
	 * Get the value of GracePeriod.
	 * @return value of GracePeriod. If negative then graceperiod does not apply and should not be considered
	 */
	public synchronized int getGracePeriod() {
		return (fCreatedThreadsNo <= fMinimumNoThreads) ? -1 :fGracePeriod;
	}
	
	/**
	 * Set the value of GracePeriod.
	 * @param v  Value to assign to GracePeriod.
	 */
	public synchronized void setGracePeriod(int  v) {
		this.fGracePeriod = v;
	}
	
	
   /**
     * Lifcycle factory method for CThreads.
     * @return Newly instantiated CThread
     */
    private synchronized CThread makeCThread() 
    {
		fIdentifierLast++;
		CThread lNew = new CThread(this, fID, fIdentifierLast, fPriority);
		fCreatedThreadsNo++;
		return lNew;
    }

    /**
	 * Lifecycle notification sent from CThread that Thread's primary :run method has stopped executing.
     * @param thr Thread that terminated
     */
    public final synchronized void NotifyThreadDied(CThread thr) 
    {
		if ( ! thr.MarkTerminate() ) 
			{
			fCreatedThreadsNo--;
			notifyAll();
			}
    }

    /**
     * When Threads are free and available the should  call this method in order to return to free list.
     *
     * @param gracePeriod 
     * @return TRUE if Thread should continue to run and FALSE otherwise.
     */

    final synchronized boolean Available(CThread thr, boolean gracePeriod) 
    {
		if ( gracePeriod && (fCreatedThreadsNo > fMinimumNoThreads) ) 
			{
			if ( ! thr.MarkTerminate() ) 
				{
				fCreatedThreadsNo--;
				fAssignedThreads--;
				notifyAll();
				} 
			return false;
			} 

		else if ( fCreatedThreadsNo <= fMaximumNoThreads ) 
			{
			thr.fPrev   = fFreeList_Tail;
			if (fFreeList_Tail != null)
				{
				fFreeList_Tail.fNext = thr;
				}
			fFreeList_Tail = thr;
			if (fFreeList_Header == null)
				{
				fFreeList_Header = thr;
				}
			fAssignedThreads--;
			notifyAll();
			return true;
			}

		else 
			{
			if ( ! thr.MarkTerminate() ) 
				{
				fCreatedThreadsNo--;
				fAssignedThreads--;
				notifyAll();
				}
			return false;
			}
    }


    /**
	 * Wait until all assigned work has terminated.
     */
    public synchronized void WaitForAllWork() 
    {
		while ( fAssignedThreads > 0) 
			{
			try {wait();} catch (InterruptedException ex) {	}
			}/*while*/
    }

    /**
     * Allocates new Threads from list of free Threads or creates new ones.
     *
     * @param waitForRelease calling thread should wait for freed threads if none was avaliable.
     * @return Allocated Thread or NULL if allocation failed.
     */

    protected synchronized CThread allocateThread(boolean waitForRelease) 
    {
		CThread lNew = null;

		while ( true ) 
			{
            /* Check for free in list */
			if ( fFreeList_Header != null ) 
				{
                /* Get first Thread from list */
				lNew                = fFreeList_Header;
				fFreeList_Header    = fFreeList_Header.fNext;
				if (fFreeList_Header != null) 
					{
					fFreeList_Header.fPrev = null;
					} 
				else 
					{
					fFreeList_Tail = null;
					}
				lNew.fNext = null;
				return lNew;
				} 

            /* Check if there is more room in pool  */
			else if ( fCreatedThreadsNo < fMaximumNoThreads ) 
				{
				return  makeCThread();
				} 

            /* check if it is possible to expand Lane*/
			else if ( !fFixedSize ) 
				{
				return makeCThread();
				}
            
            /* should we wait for another CThread to be released */
			else if ( waitForRelease ) 
				{
				try {wait();} 	catch (InterruptedException ex) {}
				} 

            /* no strategy worked !! */
			else 
				{
				return null;
				}
			}/*while*/
    }

 

    /**
     * Spawns of a Thread to handle the assigned work.
	 *
     * @param work
     * @param waitForRelease 
     * @return Spawned Thread, NULL otherwise.
     */
    public CThread Spawn(Runnable work, 
                         boolean waitForRelease) 
    {
		while ( true ) 
			{
			CThread thr = allocateThread(/*priority, */waitForRelease);
			if ( thr != null ) 
				{
                if( thr.Assign(work) )
                    {
                    synchronized (this) 
                        {
                        fAssignedThreads++;
                        }
                    return thr;
                    }
				} /*if*/
			else 
				{
				return null;
				}
			}/*while*/
    }

	
}


/*.IEnd,ThreadPoolLane,====================================*/
