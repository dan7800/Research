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

import org.compiere.util.*;
import org.compiere.www.*;


/**
 *  Check Out.
 *
 *  @author Jorg Janke
 *  @version $Id: CheckOutServlet.java,v 1.9 2004/02/15 01:55:47 jjanke Exp $
 */
public class CheckOutServlet extends HttpServlet
{
	/**	Logging						*/
	private Logger			log = Logger.getCLogger(getClass());
	/** Name						*/
	static public final String		NAME = "checkOutServlet";
	/** Attribute					*/
	static public final String		ATTR_CHECKOUT = "CheckOut";

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
			throw new ServletException("CheckOutServlet.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Compiere Web CheckOut Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
	}   //  destroy


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
		log.info("doGet from " + request.getRemoteHost() + " - " + request.getRemoteAddr());
		HttpSession session = request.getSession(true);
		session.removeAttribute(JSPEnv.HDR_MESSAGE);

		//	Web User/Basket
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
		WebBasket wb = (WebBasket)session.getAttribute(WebBasket.NAME);

		String url = "/login.jsp";
		//	Nothing in basket
		if (wb == null || wb.getLineCount() == 0)
			url = "/basket.jsp";
		else
		{
			session.setAttribute(ATTR_CHECKOUT, "Y");	//	indicate checkout
			if (wu != null && wu.isLoggedIn ())
				url = "/addressInfo.jsp";
		}

	//	if (request.isSecure())
	//	{
			log.info ("doGet - Forward to " + url);
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher (url);
			dispatcher.forward (request, response);
	//	}
	//	else
		//	Switch to secure
	//	{
	//		url = "https://" + request.getServerName() + request.getContextPath() + "/" + url;
	//		log.info ("doGet - Secure Forward to " + url);
	//		WUtil.createForwardPage(response, "Secure Access", url);
	//	}
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
		log.info("doPost from " + request.getRemoteHost() + " - " + request.getRemoteAddr());
		HttpSession session = request.getSession(false);
	}	//	doPost

}	//	CheckOutServlet
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

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.apache.log4j.Logger;

import org.compiere.util.*;
import org.compiere.www.*;


/**
 *  Check Out.
 *
 *  @author Jorg Janke
 *  @version $Id: CheckOutServlet.java,v 1.7 2003/07/24 03:36:41 jjanke Exp $
 */
public class CheckOutServlet extends HttpServlet
{
	/**	Logging						*/
	private Logger			log = Logger.getLogger(getClass());
	/** Name						*/
	static public final String		NAME = "checkOutServlet";
	/** Attribute					*/
	static public final String		ATTR_CHECKOUT = "CheckOut";

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
			throw new ServletException("CheckOutServlet.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Compiere Web CheckOut Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
	}   //  destroy


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
		log.info("doGet from " + request.getRemoteHost() + " - " + request.getRemoteAddr());
		HttpSession session = request.getSession(true);
		session.removeAttribute(JSPEnv.HDR_MESSAGE);

		//	Web User/Basket
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
		WebBasket wb = (WebBasket)session.getAttribute(WebBasket.NAME);

		String url = "login.jsp";
		//	Nothing in basket
		if (wb == null || wb.getLineCount() == 0)
			url = "basket.jsp";
		else
		{
			session.setAttribute(ATTR_CHECKOUT, "Y");	//	indicate checkout
			if (wu != null && wu.isLoggedIn ())
				url = "addressInfo.jsp";
		}

	//	if (request.isSecure())
	//	{
			log.info ("doGet - Forward to " + url);
			RequestDispatcher dispatcher = getServletContext ().getRequestDispatcher (url);
			dispatcher.forward (request, response);
	//	}
	//	else
		//	Switch to secure
	//	{
	//		url = "https://" + request.getServerName() + request.getContextPath() + "/" + url;
	//		log.info ("doGet - Secure Forward to " + url);
	//		WUtil.createForwardPage(response, "Secure Access", url);
	//	}
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
		log.info("doPost from " + request.getRemoteHost() + " - " + request.getRemoteAddr());
		HttpSession session = request.getSession(false);
	}	//	doPost

}	//	CheckOutServlet
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

import org.compiere.util.*;


/**
 *  Check Out.
 *
 *  @author Jorg Janke
 *  @version $Id: CheckOutServlet.java,v 1.10 2004/08/30 06:02:38 jjanke Exp $
 */
public class CheckOutServlet extends HttpServlet
{
	/**	Logging						*/
	private Logger			log = Logger.getCLogger(getClass());
	/** Name						*/
	static public final String		NAME = "checkOutServlet";
	/** Attribute					*/
	static public final String		ATTR_CHECKOUT = "CheckOut";

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
		if (!WebEnv.initWeb(config))
			throw new ServletException("CheckOutServlet.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Compiere Web CheckOut Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
	}   //  destroy


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
		log.info("doGet from " + request.getRemoteHost() + " - " + request.getRemoteAddr());
		HttpSession session = request.getSession(true);
		session.removeAttribute(JSPEnv.HDR_MESSAGE);

		//	Web User/Basket
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
		WebBasket wb = (WebBasket)session.getAttribute(WebBasket.NAME);

		String url = "/login.jsp";
		//	Nothing in basket
		if (wb == null || wb.getLineCount() == 0)
			url = "/basket.jsp";
		else
		{
			session.setAttribute(ATTR_CHECKOUT, "Y");	//	indicate checkout
			if (wu != null && wu.isLoggedIn ())
				url = "/addressInfo.jsp";
		}

	//	if (request.isSecure())
	//	{
			log.info ("doGet - Forward to " + url);
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher (url);
			dispatcher.forward (request, response);
	//	}
	//	else
		//	Switch to secure
	//	{
	//		url = "https://" + request.getServerName() + request.getContextPath() + "/" + url;
	//		log.info ("doGet - Secure Forward to " + url);
	//		WUtil.createForwardPage(response, "Secure Access", url);
	//	}
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
		log.info("doPost from " + request.getRemoteHost() + " - " + request.getRemoteAddr());
		HttpSession session = request.getSession(false);
	}	//	doPost

}	//	CheckOutServlet
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

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;

import org.compiere.util.*;
import org.compiere.www.*;


/**
 *  Check Out.
 *
 *  @author Jorg Janke
 *  @version $Id: CheckOutServlet.java,v 1.8 2003/10/04 03:58:26 jjanke Exp $
 */
public class CheckOutServlet extends HttpServlet
{
	/**	Logging						*/
	private Logger			log = Logger.getCLogger(getClass());
	/** Name						*/
	static public final String		NAME = "checkOutServlet";
	/** Attribute					*/
	static public final String		ATTR_CHECKOUT = "CheckOut";

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
			throw new ServletException("CheckOutServlet.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Compiere Web CheckOut Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
	}   //  destroy


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
		log.info("doGet from " + request.getRemoteHost() + " - " + request.getRemoteAddr());
		HttpSession session = request.getSession(true);
		session.removeAttribute(JSPEnv.HDR_MESSAGE);

		//	Web User/Basket
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
		WebBasket wb = (WebBasket)session.getAttribute(WebBasket.NAME);

		String url = "login.jsp";
		//	Nothing in basket
		if (wb == null || wb.getLineCount() == 0)
			url = "basket.jsp";
		else
		{
			session.setAttribute(ATTR_CHECKOUT, "Y");	//	indicate checkout
			if (wu != null && wu.isLoggedIn ())
				url = "addressInfo.jsp";
		}

	//	if (request.isSecure())
	//	{
			log.info ("doGet - Forward to " + url);
			RequestDispatcher dispatcher = getServletContext ().getRequestDispatcher (url);
			dispatcher.forward (request, response);
	//	}
	//	else
		//	Switch to secure
	//	{
	//		url = "https://" + request.getServerName() + request.getContextPath() + "/" + url;
	//		log.info ("doGet - Secure Forward to " + url);
	//		WUtil.createForwardPage(response, "Secure Access", url);
	//	}
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
		log.info("doPost from " + request.getRemoteHost() + " - " + request.getRemoteAddr());
		HttpSession session = request.getSession(false);
	}	//	doPost

}	//	CheckOutServlet
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

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.apache.log4j.Logger;

import org.compiere.util.*;
import org.compiere.www.*;


/**
 *  Check Out.
 *
 *  @author Jorg Janke
 *  @version $Id: CheckOutServlet.java,v 1.7 2003/07/24 03:36:41 jjanke Exp $
 */
public class CheckOutServlet extends HttpServlet
{
	/**	Logging						*/
	private Logger			log = Logger.getLogger(getClass());
	/** Name						*/
	static public final String		NAME = "checkOutServlet";
	/** Attribute					*/
	static public final String		ATTR_CHECKOUT = "CheckOut";

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
			throw new ServletException("CheckOutServlet.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Compiere Web CheckOut Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
	}   //  destroy


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
		log.info("doGet from " + request.getRemoteHost() + " - " + request.getRemoteAddr());
		HttpSession session = request.getSession(true);
		session.removeAttribute(JSPEnv.HDR_MESSAGE);

		//	Web User/Basket
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
		WebBasket wb = (WebBasket)session.getAttribute(WebBasket.NAME);

		String url = "login.jsp";
		//	Nothing in basket
		if (wb == null || wb.getLineCount() == 0)
			url = "basket.jsp";
		else
		{
			session.setAttribute(ATTR_CHECKOUT, "Y");	//	indicate checkout
			if (wu != null && wu.isLoggedIn ())
				url = "addressInfo.jsp";
		}

	//	if (request.isSecure())
	//	{
			log.info ("doGet - Forward to " + url);
			RequestDispatcher dispatcher = getServletContext ().getRequestDispatcher (url);
			dispatcher.forward (request, response);
	//	}
	//	else
		//	Switch to secure
	//	{
	//		url = "https://" + request.getServerName() + request.getContextPath() + "/" + url;
	//		log.info ("doGet - Secure Forward to " + url);
	//		WUtil.createForwardPage(response, "Secure Access", url);
	//	}
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
		log.info("doPost from " + request.getRemoteHost() + " - " + request.getRemoteAddr());
		HttpSession session = request.getSession(false);
	}	//	doPost

}	//	CheckOutServlet
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

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.apache.log4j.Logger;

import org.compiere.util.*;
import org.compiere.www.*;


/**
 *  Check Out.
 *
 *  @author Jorg Janke
 *  @version $Id: CheckOutServlet.java,v 1.7 2003/07/24 03:36:41 jjanke Exp $
 */
public class CheckOutServlet extends HttpServlet
{
	/**	Logging						*/
	private Logger			log = Logger.getLogger(getClass());
	/** Name						*/
	static public final String		NAME = "checkOutServlet";
	/** Attribute					*/
	static public final String		ATTR_CHECKOUT = "CheckOut";

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
			throw new ServletException("CheckOutServlet.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Compiere Web CheckOut Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
	}   //  destroy


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
		log.info("doGet from " + request.getRemoteHost() + " - " + request.getRemoteAddr());
		HttpSession session = request.getSession(true);
		session.removeAttribute(JSPEnv.HDR_MESSAGE);

		//	Web User/Basket
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
		WebBasket wb = (WebBasket)session.getAttribute(WebBasket.NAME);

		String url = "login.jsp";
		//	Nothing in basket
		if (wb == null || wb.getLineCount() == 0)
			url = "basket.jsp";
		else
		{
			session.setAttribute(ATTR_CHECKOUT, "Y");	//	indicate checkout
			if (wu != null && wu.isLoggedIn ())
				url = "addressInfo.jsp";
		}

	//	if (request.isSecure())
	//	{
			log.info ("doGet - Forward to " + url);
			RequestDispatcher dispatcher = getServletContext ().getRequestDispatcher (url);
			dispatcher.forward (request, response);
	//	}
	//	else
		//	Switch to secure
	//	{
	//		url = "https://" + request.getServerName() + request.getContextPath() + "/" + url;
	//		log.info ("doGet - Secure Forward to " + url);
	//		WUtil.createForwardPage(response, "Secure Access", url);
	//	}
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
		log.info("doPost from " + request.getRemoteHost() + " - " + request.getRemoteAddr());
		HttpSession session = request.getSession(false);
	}	//	doPost

}	//	CheckOutServlet
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

import org.compiere.util.*;
import org.compiere.www.*;


/**
 *  Check Out.
 *
 *  @author Jorg Janke
 *  @version $Id: CheckOutServlet.java,v 1.9 2004/02/15 01:55:47 jjanke Exp $
 */
public class CheckOutServlet extends HttpServlet
{
	/**	Logging						*/
	private Logger			log = Logger.getCLogger(getClass());
	/** Name						*/
	static public final String		NAME = "checkOutServlet";
	/** Attribute					*/
	static public final String		ATTR_CHECKOUT = "CheckOut";

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
			throw new ServletException("CheckOutServlet.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Compiere Web CheckOut Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
	}   //  destroy


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
		log.info("doGet from " + request.getRemoteHost() + " - " + request.getRemoteAddr());
		HttpSession session = request.getSession(true);
		session.removeAttribute(JSPEnv.HDR_MESSAGE);

		//	Web User/Basket
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
		WebBasket wb = (WebBasket)session.getAttribute(WebBasket.NAME);

		String url = "/login.jsp";
		//	Nothing in basket
		if (wb == null || wb.getLineCount() == 0)
			url = "/basket.jsp";
		else
		{
			session.setAttribute(ATTR_CHECKOUT, "Y");	//	indicate checkout
			if (wu != null && wu.isLoggedIn ())
				url = "/addressInfo.jsp";
		}

	//	if (request.isSecure())
	//	{
			log.info ("doGet - Forward to " + url);
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher (url);
			dispatcher.forward (request, response);
	//	}
	//	else
		//	Switch to secure
	//	{
	//		url = "https://" + request.getServerName() + request.getContextPath() + "/" + url;
	//		log.info ("doGet - Secure Forward to " + url);
	//		WUtil.createForwardPage(response, "Secure Access", url);
	//	}
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
		log.info("doPost from " + request.getRemoteHost() + " - " + request.getRemoteAddr());
		HttpSession session = request.getSession(false);
	}	//	doPost

}	//	CheckOutServlet
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

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;

import org.compiere.util.*;
import org.compiere.www.*;


/**
 *  Check Out.
 *
 *  @author Jorg Janke
 *  @version $Id: CheckOutServlet.java,v 1.8 2003/10/04 03:58:26 jjanke Exp $
 */
public class CheckOutServlet extends HttpServlet
{
	/**	Logging						*/
	private Logger			log = Logger.getCLogger(getClass());
	/** Name						*/
	static public final String		NAME = "checkOutServlet";
	/** Attribute					*/
	static public final String		ATTR_CHECKOUT = "CheckOut";

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
			throw new ServletException("CheckOutServlet.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Compiere Web CheckOut Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
	}   //  destroy


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
		log.info("doGet from " + request.getRemoteHost() + " - " + request.getRemoteAddr());
		HttpSession session = request.getSession(true);
		session.removeAttribute(JSPEnv.HDR_MESSAGE);

		//	Web User/Basket
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
		WebBasket wb = (WebBasket)session.getAttribute(WebBasket.NAME);

		String url = "login.jsp";
		//	Nothing in basket
		if (wb == null || wb.getLineCount() == 0)
			url = "basket.jsp";
		else
		{
			session.setAttribute(ATTR_CHECKOUT, "Y");	//	indicate checkout
			if (wu != null && wu.isLoggedIn ())
				url = "addressInfo.jsp";
		}

	//	if (request.isSecure())
	//	{
			log.info ("doGet - Forward to " + url);
			RequestDispatcher dispatcher = getServletContext ().getRequestDispatcher (url);
			dispatcher.forward (request, response);
	//	}
	//	else
		//	Switch to secure
	//	{
	//		url = "https://" + request.getServerName() + request.getContextPath() + "/" + url;
	//		log.info ("doGet - Secure Forward to " + url);
	//		WUtil.createForwardPage(response, "Secure Access", url);
	//	}
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
		log.info("doPost from " + request.getRemoteHost() + " - " + request.getRemoteAddr());
		HttpSession session = request.getSession(false);
	}	//	doPost

}	//	CheckOutServlet
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

import org.compiere.util.*;


/**
 *  Check Out.
 *
 *  @author Jorg Janke
 *  @version $Id: CheckOutServlet.java,v 1.10 2004/08/30 06:02:38 jjanke Exp $
 */
public class CheckOutServlet extends HttpServlet
{
	/**	Logging						*/
	private Logger			log = Logger.getCLogger(getClass());
	/** Name						*/
	static public final String		NAME = "checkOutServlet";
	/** Attribute					*/
	static public final String		ATTR_CHECKOUT = "CheckOut";

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
		if (!WebEnv.initWeb(config))
			throw new ServletException("CheckOutServlet.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Compiere Web CheckOut Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
	}   //  destroy


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
		log.info("doGet from " + request.getRemoteHost() + " - " + request.getRemoteAddr());
		HttpSession session = request.getSession(true);
		session.removeAttribute(JSPEnv.HDR_MESSAGE);

		//	Web User/Basket
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
		WebBasket wb = (WebBasket)session.getAttribute(WebBasket.NAME);

		String url = "/login.jsp";
		//	Nothing in basket
		if (wb == null || wb.getLineCount() == 0)
			url = "/basket.jsp";
		else
		{
			session.setAttribute(ATTR_CHECKOUT, "Y");	//	indicate checkout
			if (wu != null && wu.isLoggedIn ())
				url = "/addressInfo.jsp";
		}

	//	if (request.isSecure())
	//	{
			log.info ("doGet - Forward to " + url);
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher (url);
			dispatcher.forward (request, response);
	//	}
	//	else
		//	Switch to secure
	//	{
	//		url = "https://" + request.getServerName() + request.getContextPath() + "/" + url;
	//		log.info ("doGet - Secure Forward to " + url);
	//		WUtil.createForwardPage(response, "Secure Access", url);
	//	}
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
		log.info("doPost from " + request.getRemoteHost() + " - " + request.getRemoteAddr());
		HttpSession session = request.getSession(false);
	}	//	doPost

}	//	CheckOutServlet
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

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.apache.log4j.Logger;

import org.compiere.util.*;
import org.compiere.www.*;


/**
 *  Check Out.
 *
 *  @author Jorg Janke
 *  @version $Id: CheckOutServlet.java,v 1.7 2003/07/24 03:36:41 jjanke Exp $
 */
public class CheckOutServlet extends HttpServlet
{
	/**	Logging						*/
	private Logger			log = Logger.getLogger(getClass());
	/** Name						*/
	static public final String		NAME = "checkOutServlet";
	/** Attribute					*/
	static public final String		ATTR_CHECKOUT = "CheckOut";

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
			throw new ServletException("CheckOutServlet.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Compiere Web CheckOut Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
	}   //  destroy


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
		log.info("doGet from " + request.getRemoteHost() + " - " + request.getRemoteAddr());
		HttpSession session = request.getSession(true);
		session.removeAttribute(JSPEnv.HDR_MESSAGE);

		//	Web User/Basket
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
		WebBasket wb = (WebBasket)session.getAttribute(WebBasket.NAME);

		String url = "login.jsp";
		//	Nothing in basket
		if (wb == null || wb.getLineCount() == 0)
			url = "basket.jsp";
		else
		{
			session.setAttribute(ATTR_CHECKOUT, "Y");	//	indicate checkout
			if (wu != null && wu.isLoggedIn ())
				url = "addressInfo.jsp";
		}

	//	if (request.isSecure())
	//	{
			log.info ("doGet - Forward to " + url);
			RequestDispatcher dispatcher = getServletContext ().getRequestDispatcher (url);
			dispatcher.forward (request, response);
	//	}
	//	else
		//	Switch to secure
	//	{
	//		url = "https://" + request.getServerName() + request.getContextPath() + "/" + url;
	//		log.info ("doGet - Secure Forward to " + url);
	//		WUtil.createForwardPage(response, "Secure Access", url);
	//	}
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
		log.info("doPost from " + request.getRemoteHost() + " - " + request.getRemoteAddr());
		HttpSession session = request.getSession(false);
	}	//	doPost

}	//	CheckOutServlet
