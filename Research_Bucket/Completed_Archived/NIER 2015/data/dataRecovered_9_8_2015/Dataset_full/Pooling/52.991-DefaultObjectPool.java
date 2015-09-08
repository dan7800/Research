/*-*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 0 -*-*/
/*.IH,DefaultObjectPool,======================================*/
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
/*.IA,	PUBLIC Include File DefaultObjectPool.java			*/
package org.openebxml.comp.util;

/************************************************
	Includes
\************************************************/

/**
 *  Simple implementation of an ObjectPool.
 *
 * @author Anders W. Tell   Financial Toolsmiths AB
 * @version $Id: DefaultObjectPool.java,v 1.2 2001/10/21 09:47:52 awtopensource Exp $
 */

public abstract class DefaultObjectPool  implements  ObjectPool {

	/****/
	public static final TimeValue	DEFAULT_TIMEOUT = new TimeValue(15,0);

	/****/
	public static final int			DEFAULT_INCREMENT	= 16;

	/****/
	public static final int			DEFAULT_MAXIMUM		= 32;

	/*------------------------------------------------------------*/
	/****/
	TimeValue		fTimeout;

	/****/
	int 			fMaximum;
	
	/****/
	int 			fFreeNo;
	
	/****/
	int 			fMadeNo;
	
	/****/
	Object[]		fFree;

	/****/
	int				fFreeOffset;

	/****/
	WaitObject		fLock;

    /****/
    public DefaultObjectPool()
    {
		this(DEFAULT_MAXIMUM);
    }

    /****/
    public DefaultObjectPool(int max)
    {
		fTimeout	= DEFAULT_TIMEOUT;
		fLock		= new WaitObject();
		fMaximum	= max;
		fFree		= new Object[max];
		fFreeNo		= 0;
		fFreeOffset	= 0;
		fMadeNo		= 0;
    }


	/****/
	public Object makeObject()
    {
		return makeObject(fTimeout);
	}

	/**
	 *
     *@param wait Absolute time to wait until before throwing an exception if the condition isn't satisfied
	 */
	public Object makeObject(TimeValue wait)
    {
		/* try to aquire free or create new one */
		synchronized(fLock) 
			{
			if( 0 < fFreeNo)				/* FREE */
				{
				fMadeNo++;
				fFreeNo--;
				return fFree[fFreeOffset+fFreeNo];
				}
			else if( fMadeNo < fMaximum )	/* NEW */
				{
				Object lNew = newObject();	
				fMadeNo++;
				return lNew;
				}
			}
		/* Try to wait for returned objects */
		try
			{
			fLock.timedWait(wait);
			synchronized(fLock) 
				{
				if( 0 < fFreeNo )
					{
					fMadeNo++;
					fFreeNo--;
					return fFree[fFreeOffset+fFreeNo];
					}
				}
			}	
		catch (InterruptedException  ex)
			{
			}
		catch ( TimeoutException  ex)
			{
			}
		return null;
	}

	/****/
	public void returnObject(Object obj)
    {
		synchronized(fLock) 
			{
			/* Check if need to expand free vector */
			if(fFreeNo == fFree.length )
				{
				int liSize = fFree.length + DEFAULT_INCREMENT;
				if( liSize > fMaximum )
					liSize = fMaximum;
				Object[] lNew = new Object[liSize];
				System.arraycopy(fFree, 0, lNew, 0, fFree.length);
				fFree = lNew;
				}

			if(fFreeOffset == 0)
				{
				fFree[fFreeNo] = obj;
				}
			else
				{
				fFreeOffset--;
				fFree[fFreeOffset] = obj;
				}
			fMadeNo++;
			fFreeNo++;
			fLock.condition( 0 < fFreeNo );
			fLock.signal();
			}
	}


	/****/
	public int getFree()
    {
		return fFreeNo;
	}

	/****/
	public int getMade()
    {
		return fMadeNo;
	}

	
	/**
	 * Get the value of Maximum.
	 * @return value of Maximum.
	 */
	public int getMaximum() {
		return fMaximum;
	}
	
	/**
	 * Set the value of Maximum.
	 * @param v  Value to assign to Maximum.
	 */
	public void setMaximum(int  v) {
		this.fMaximum = v;
	}

	
	/**
	 * Get the value of Timeout.
	 * @return value of Timeout.
	 */
	public TimeValue getTimeout() {
		return fTimeout;
	}
	/**
	 * Set the value of Timeout.
	 * @param v  Value to assign to Timeout.
	 */
	public void setTimeout(TimeValue  v){
		fTimeout = v;
	}
	

}


/*.IEnd,DefaultObjectPool,====================================*/
