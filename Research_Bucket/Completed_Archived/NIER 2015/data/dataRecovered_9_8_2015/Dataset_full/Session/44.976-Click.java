/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is             Compiere  ERP & CRM Smart Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2003 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.wstore;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.sql.*;

import org.apache.log4j.Logger;

import org.compiere.www.*;

/**
 *	Click Counter
 *
 *  @author Jorg Janke
 *  @version $Id: Click.java,v 1.1 2003/05/04 06:47:27 jjanke Exp $
 */
public class Click  extends HttpServlet implements Runnable
{
	/**	Logging						*/
	private Logger			log = Logger.getLogger(getClass());

	/** Name						*/
	static public final String			NAME = "click";

	/** Target Parameter			*/
	static public final String			PARA_TARGET = "target";
	static public final String			DEFAULT_TARGET = "http://www.compiere.org/";


	/**
	 *	Initialize global variables
	 *
	 *  @param config Configuration
	 *  @throws ServletException
	 */
	public void init(ServletConfig config)
		throws ServletException
	{
		super.init(config);
		if (!WEnv.initWeb(config))
			throw new ServletException("Click.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Compiere Click Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
	}   //  destroy

	/*************************************************************************/

	/**
	 *  Process the HTTP Get request.
	 * 	(logout, deleteCookie)
	 *  Sends Web Request Page
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		log.info("doGet from " + request.getRemoteHost()
			+ " - " + request.getRemoteAddr()
			+ " - " + request.getRemoteUser()
			+ " - " + request.getHeader("Referer")
			+ " - " + request.getHeader("Accept-Language")
			+ " - " + request.getHeader("User-Agent")
			);
	//	WEnv.dump(request);

		//	Get Named Parameter		-	/click?target=www...
		String url = request.getParameter(PARA_TARGET);
		//	Check parameters		-	/click?www...
		if (url == null || url.length() == 0)
		{
			Enumeration e = request.getParameterNames ();
			if (e.hasMoreElements ())
				url = (String)e.nextElement ();
		}
		//	Check Path				-	/click/www...
		if (url == null || url.length() == 0)
		{
			url = request.getPathInfo ();
			if (url != null)
				url = url.substring(1);		//	cut off initial /
		}
		//	Still nothing
		if (url == null || url.length() == 0)
			url = DEFAULT_TARGET;
		//	add protocol
		if (url.indexOf("://") == -1)
			url = "http://" + url;

		log.debug("redirect - " + url);
		response.sendRedirect(url);

		new Thread(this).start();
	}	//	doGet

	/**
	 *  Process the HTTP Post request
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		doGet (request, response);
	}	//	doPost

	/*************************************************************************/

	/**
	 * 	Async Process
	 */
	public void run()
	{
		log.debug("run " + this);
	}	//	run

}	//	Click
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is             Compiere  ERP & CRM Smart Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2003 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.wstore;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.sql.*;

import org.apache.log4j.Logger;

import org.compiere.www.*;

/**
 *	Click Counter
 *
 *  @author Jorg Janke
 *  @version $Id: Click.java,v 1.1 2003/05/04 06:47:27 jjanke Exp $
 */
public class Click  extends HttpServlet implements Runnable
{
	/**	Logging						*/
	private Logger			log = Logger.getLogger(getClass());

	/** Name						*/
	static public final String			NAME = "click";

	/** Target Parameter			*/
	static public final String			PARA_TARGET = "target";
	static public final String			DEFAULT_TARGET = "http://www.compiere.org/";


	/**
	 *	Initialize global variables
	 *
	 *  @param config Configuration
	 *  @throws ServletException
	 */
	public void init(ServletConfig config)
		throws ServletException
	{
		super.init(config);
		if (!WEnv.initWeb(config))
			throw new ServletException("Click.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Compiere Click Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
	}   //  destroy

	/*************************************************************************/

	/**
	 *  Process the HTTP Get request.
	 * 	(logout, deleteCookie)
	 *  Sends Web Request Page
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		log.info("doGet from " + request.getRemoteHost()
			+ " - " + request.getRemoteAddr()
			+ " - " + request.getRemoteUser()
			+ " - " + request.getHeader("Referer")
			+ " - " + request.getHeader("Accept-Language")
			+ " - " + request.getHeader("User-Agent")
			);
	//	WEnv.dump(request);

		//	Get Named Parameter		-	/click?target=www...
		String url = request.getParameter(PARA_TARGET);
		//	Check parameters		-	/click?www...
		if (url == null || url.length() == 0)
		{
			Enumeration e = request.getParameterNames ();
			if (e.hasMoreElements ())
				url = (String)e.nextElement ();
		}
		//	Check Path				-	/click/www...
		if (url == null || url.length() == 0)
		{
			url = request.getPathInfo ();
			if (url != null)
				url = url.substring(1);		//	cut off initial /
		}
		//	Still nothing
		if (url == null || url.length() == 0)
			url = DEFAULT_TARGET;
		//	add protocol
		if (url.indexOf("://") == -1)
			url = "http://" + url;

		log.debug("redirect - " + url);
		response.sendRedirect(url);

		new Thread(this).start();
	}	//	doGet

	/**
	 *  Process the HTTP Post request
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		doGet (request, response);
	}	//	doPost

	/*************************************************************************/

	/**
	 * 	Async Process
	 */
	public void run()
	{
		log.debug("run " + this);
	}	//	run

}	//	Click
