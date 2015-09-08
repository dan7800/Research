/*-*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 0 -*-*/
/*.IH,ThreadPool,======================================*/
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
/*.IA,	PUBLIC Include File ThreadPool.java			*/
package org.openebxml.comp.util;

/************************************************
	Includes
\************************************************/

/**
 *  Container for a number of ThreadPoolLanes, forming a ThreadPool.
 *
 * @author Anders W. Tell   Financial Toolsmiths AB
 * @version $Id: ThreadPool.java,v 1.2 2001/10/21 09:47:52 awtopensource Exp $
 */

public class ThreadPool {

 

	/*--------------------------------------------------------------------*/
	/****/
	ThreadPoolManager 	fManager;

    /**
     * Thread group for the Pool
     */
    protected ThreadGroup fThreadGroup;

	/**
	 * Forms an intrusive linked list maintained by owned ThreadPoolManager.
	 * Reference to next Thread
	 */
    ThreadPool 		fNext;

	/**
	 * Forms an intrusive linked list maintained by owned ThreadPoolManager.
	 * Reference to previous Thread
	 *
	 */
    ThreadPool 		fPrev;

    /****/
    ThreadPoolLane  fLanes[] = new ThreadPoolLane[Thread.MAX_PRIORITY];


    /**
     * Constructor for a named ThreadPool with a preallocated ThreadGroup
     * @param mgr  ThreadPoolManager
     * @param group Preallocated ThreadGroup
     */

    public ThreadPool(ThreadPoolManager mgr,ThreadGroup group) 
    {
		fManager		= mgr;
		fThreadGroup    = group;
        fNext           = null;
        fPrev           = null;

        /* Allocate all Lanes */
        for(int i = 0; i < fLanes.length; i++)
            {
            fLanes[i] = new ThreadPoolLane(this, group, i+1, i+1, 
                                     ThreadPoolLane.DEFAULT_THREADS_MINIMUM, 
                                     ThreadPoolLane.DEFAULT_THREADS_MAXIMUM);
            }/*for*/

    }

    /**
     * Constructor for a named ThreadPool
     * @param mgr  ThreadPoolManager
     * @param name Name of ThreadGroup that binds all threads together.
     */

    public ThreadPool(ThreadPoolManager mgr,String name) 
    {
		this(mgr, new ThreadGroup(name));
    }

	/**
	 * Get the value of Manager.
	 * @return value of Manager.
	 */
	public ThreadPoolManager getManager() {
		return fManager;
	}


    /**
	 * Wait until all assigned work has terminated in all Lanes
     */
    public synchronized void WaitForAllWork() 
    {
        for(int i = 0; i < fLanes.length; i++)
            {
            if( fLanes[i] != null)
                {
                fLanes[i].WaitForAllWork();
                }
            }/*for*/
    }

    /**
     * Spawns of a Thread to handle the assigned work.
     *
     * @param priority Priority for the work.
     * @param work
     * @param waitForRelease 
     * @return Spawned Thread, NULL otherwise.
     */
    public CThread Spawn(int priority,
                         Runnable work, 
                         boolean waitForRelease) 
    {
        ThreadPoolLane  lLane = fLanes[priority-1];
        if(lLane != null )
            {
            return lLane.Spawn(work, waitForRelease);
            }
        if( lLane == null )
            {
            /* try lower priorities */
            for(int i = priority-2;  0 <= i; i--)
                {
                lLane = fLanes[i];
                if(lLane != null )
                    {
                    return lLane.Spawn(work, waitForRelease);
                    }
                }/*for*/
            }/*if*/
        return null;
    }

    /**
     * Spawns of a Thread to handle the assigned work with NORMAL priority.
     *
     * @param priority Priority for the work.
     * @param work
     * @param waitForRelease 
     * @return Spawned Thread, NULL otherwise.
     */
    public CThread Spawn(Runnable work, 
                         boolean waitForRelease) 
    {
        return Spawn(Thread.NORM_PRIORITY, work, waitForRelease);
    }
}


/*.IEnd,ThreadPool,====================================*/
