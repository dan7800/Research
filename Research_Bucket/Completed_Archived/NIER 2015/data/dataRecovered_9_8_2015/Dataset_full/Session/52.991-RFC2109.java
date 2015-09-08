/*-*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 0 -*-*/
/*.IH,RFC2109,======================================*/
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
/*.IA,	PUBLIC Include File RFC2109.java			*/
package org.openebxml.comp.rfc;


/************************************************
	Includes
\************************************************/

/**
 *  IETF RFC2109 related functionality.
 *
 * @author Anders W. Tell   Financial Toolsmiths AB
 * @version $Id: RFC2109.java,v 1.2 2003/08/19 14:51:35 awtopensource Exp $
 */

public class RFC2109  {

	/*****/
	public static final String  HEADER_COOKIE	= "Set-Cookie";
	/*****/
	public static final String  HEADER_COOKIE2	= "Set-Cookie2";

	/*****/
	public static final byte[]	BHEADER_COOKIE	= "Set-Cookie".getBytes();
	/*****/
	public static final byte[]	BHEADER_COOKIE_COLON	= "Set-Cookie:".getBytes();
	/*****/
	public static final byte[]	BHEADER_COOKIE2	= "Set-Cookie2".getBytes();
	/*****/
	public static final byte[]	BHEADER_COOKIE2_COLON	= "Set-Cookie2:".getBytes();

    /****/
    private RFC2109()
    {
    }

	/****/
	public static final boolean isTOKEN(String str)
    {
		int liLength = str.length();
		for(int i = 0; i < liLength; i++)
			{
			if( ! RFC2616.isTOKEN(str.charAt(i)) )
				return false;
			}
		return true;
	}

	/**
	 * @return 1-SetCookie 2-SetCookie2  negative not a valid header
	 */
	public static int isCookieHeader(String str)
    {
		if(str.equalsIgnoreCase(HEADER_COOKIE) )
			return 1;
		else if(str.equalsIgnoreCase(HEADER_COOKIE2) )
			return 2;
		else
			return -1;
	}
	/**
	 * @return 1-SetCookie 2-SetCookie2  negative not a valid header
	 */
	public static int isCookieHeader(byte[] str)
    {
		int	liLength = str.length;
		if(liLength == 10 )
			{
			for(int i = 0; i < liLength; i++)
			    {
				if(BHEADER_COOKIE[i] != str[i])
    				return -1;
				}/*for*/
			return 1;
			}
		else if(liLength == 11 )
			{	
			for(int i = 0; i < liLength; i++)
			    {
				if(BHEADER_COOKIE2[i] != str[i])
    				return -1;
				}/*for*/
			return 2;
			}
		return -1;
	}
}


/*.IEnd,RFC2109,====================================*/
