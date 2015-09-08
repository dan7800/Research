/*-*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 0 -*-*/
/*.IH,ObjectPool,======================================*/
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
/*.IA,	PUBLIC Include File ObjectPool.java			*/
package org.openebxml.comp.util;


/************************************************
	Includes
\************************************************/

/**
 *  Generic interface for Pools-of-Object classes.
 *
 * @author Anders W. Tell   Financial Toolsmiths AB
 * @version $Id: ObjectPool.java,v 1.2 2001/10/21 09:47:52 awtopensource Exp $
 */

public interface ObjectPool  {

	/**
	 * Factory Method for new Objects
	 */
	public Object newObject();

	/****/
	public Object makeObject();

	/****/
	public Object makeObject(TimeValue wait);

	/****/
	public void returnObject(Object obj);


	/****/
	public int getFree();

	/****/
	public int getMade();
	
	/****/
	public int getMaximum();
	
	/**
	 * Get the value of Timeout.
	 * @return value of Timeout.
	 */
	public TimeValue getTimeout();
	/**
	 * Set the value of Timeout.
	 * @param v  Value to assign to Timeout.
	 */
	public void setTimeout(TimeValue  v);


	
}


/*.IEnd,ObjectPool,====================================*/
