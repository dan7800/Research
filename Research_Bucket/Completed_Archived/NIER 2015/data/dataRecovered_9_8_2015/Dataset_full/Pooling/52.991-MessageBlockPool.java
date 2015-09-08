/*-*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 0 -*-*/
/*.IH,MessageBlockPool,======================================*/
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
/*.IA,	PUBLIC Include File MessageBlockPool.java			*/
package org.openebxml.comp.util;

/************************************************
	Includes
\************************************************/

/**
 *  MessageBlockPool for fixed size Blocks
 *
 * @author Anders W. Tell   Financial Toolsmiths AB
 * @version $Id: MessageBlockPool.java,v 1.1 2001/08/27 11:40:22 awtopensource Exp $
 */

public interface MessageBlockPool {

 	/****/
	public MessageBlock	makeMessageBlock();

	/****/
	public void	returnMessageBlock(MessageBlock mb);

    /****/
    public int getAvailable();
    
}


/*.IEnd,MessageBlockPool,====================================*/
