/*-*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 0 -*-*/
/*.IH,ThreadPoolManager,======================================*/
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
/*.IA,	PUBLIC Include File ThreadPoolManager.java			*/
package org.openebxml.comp.util;


/************************************************
	Includes
\************************************************/

/**
 *  Manager for a number ThreadPools.
 *
 * @author Anders W. Tell   Financial Toolsmiths AB
 * @version $Id: ThreadPoolManager.java,v 1.2 2001/10/21 09:47:52 awtopensource Exp $
 */

public class ThreadPoolManager  {

	/**
	 * Forms an intrusive linked list.
	 * Reference to Head ThreadPool
	 */
    ThreadPool 		fHead;

	/**
	 * Forms an intrusive linked list 
	 * Reference to Tail ThreadPool
	 *
	 */
    ThreadPool 		fTail;

    /****/
    public ThreadPoolManager()
    {
		fHead	= null;
		fTail	= null;
    }

	/****/
	public ThreadPool makeThreadPool(String name)
    {
		ThreadPool lTP = new ThreadPool(this, name);
		
		if(fHead == null)
			{
			fHead = fTail = lTP;
			}
		else
			{
			fTail.fNext = lTP;
			lTP.fPrev	= fTail;
			fTail		= lTP;
			}
		return lTP;
	}
}


/*.IEnd,ThreadPoolManager,====================================*/
