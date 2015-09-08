/*-*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 0 -*-*/
/*.IH,DefaultMessageBlockPool,======================================*/
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
/*.IA,	PUBLIC Include File DefaultMessageBlockPool.java			*/
package org.openebxml.comp.util;

/************************************************
	Includes
\************************************************/

/**
 *  Simple implementation of a MessageBlockPool.
 *
 * @author Anders W. Tell   Financial Toolsmiths AB
 * @version $Id: DefaultMessageBlockPool.java,v 1.2 2001/10/21 09:47:52 awtopensource Exp $
 */

public final class DefaultMessageBlockPool  implements MessageBlockPool {

	/****/
	MessageBlock		fFirst;

	/****/
	MessageBlock		fLast;

	/*****/
	int					fAvailable;

	/****/
	int					fSize;

    /****/
    public DefaultMessageBlockPool()
    {
		fFirst 		= null;
		fLast		= null;
		fAvailable	= 0;
		fSize		= MessageBlock.DEFAULT_SIZE;
    }

    /****/
    public DefaultMessageBlockPool(int size)
    {
		fFirst 		= null;
		fLast		= null;
		fAvailable	= 0;
		fSize		= size;
    }

	/****/
	public final MessageBlock	makeMessageBlock()
    {
		if( fFirst != null )
			{
			MessageBlock lOld = fFirst;
			fFirst = lOld.getContinued();
			if(fFirst == null )
				{
				fLast = null;
				}
			lOld.Reset();
			fAvailable--;
			return lOld;
			}
		return new MessageBlock(fSize);
	}

	/****/
	public final void	returnMessageBlock(MessageBlock mb)
    {
		if(mb == null)
			return;
		mb.setContinued(null);
		if( fLast == null )
			{
			fFirst = fLast = mb;
			}
		else
			{
			fLast.setContinued(mb);
			fLast = mb;
			}
		fAvailable++;
	}

    /****/
    public final int getAvailable()
    {
		return fAvailable;
	}

}


/*.IEnd,DefaultMessageBlockPool,====================================*/
