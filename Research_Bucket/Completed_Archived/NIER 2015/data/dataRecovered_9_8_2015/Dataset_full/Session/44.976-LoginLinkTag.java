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

import java.util.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

import org.apache.ecs.xhtml.*;

import org.compiere.util.*;

/**
 *  Login Link.
 * 	Creates Login/Logout Link
 *  <pre>
 *  <cws:loginLink />
 *  Variable used - "webUser"
 *	</pre>
 *
 *  @author Jorg Janke
 *  @version $Id: LoginLinkTag.java,v 1.20 2004/03/05 06:01:27 jjanke Exp $
 */
public class LoginLinkTag extends TagSupport
{
	/**	Logger							*/
	protected Logger			log = Logger.getCLogger (getClass());

	/**
	 *  Start Tag
	 *  @return SKIP_BODY
	 * 	@throws JspException
	 */
	public int doStartTag() throws JspException
	{
		Properties ctx = JSPEnv.getCtx((HttpServletRequest)pageContext.getRequest());
		//
		WebUser wu = getWebUser(ctx);
		if (wu == null)
			pageContext.getSession().removeAttribute(WebUser.NAME);
		else
			pageContext.getSession().setAttribute (WebUser.NAME, wu);
		//
		String serverContext = ctx.getProperty(JSPEnv.CTX_SERVER_CONTEXT);
	//	log.debug ("doStartTag - ServerContext=" + serverContext);
		HtmlCode html = null;
		if (wu != null && wu.isValid())
			html = getWelcomeLink (serverContext, wu);
		else
			html = getLoginLink (serverContext);
		//
		JspWriter out = pageContext.getOut();
		/**
		//	Delete Cookie Call
		if (cookieUser != null && !cookieUser.equals(" "))
		{
			log.debug("- Cookie=" + cookieUser);
			html.addElement(" ");
			a a = new a("loginServlet?mode=deleteCookie");
			a.setClass("menuDetail");
			a.addElement("(Delete&nbsp;Cookie)");
			html.addElement(a);
		}
		**/
		html.output(out);
		//
		//
		return (SKIP_BODY);
	}   //  doStartTag

	/**
	 * 	End Tag
	 * 	@return EVAL_PAGE
	 * 	@throws JspException
	 */
	public int doEndTag() throws JspException
	{
		return EVAL_PAGE;
	}	//	doEndTag


	/**
	 *	Get WebUser.
	 * 	@param ctx context
	 * 	@return Web User or null
	 */
	private WebUser getWebUser (Properties ctx)
	{
		String address = pageContext.getRequest().getRemoteAddr();
		//	Get stored User
		WebUser wu = (WebUser)pageContext.getSession().getAttribute (WebUser.NAME);
		if (wu != null)
		{
			if (Log.isTraceLevel(8))
				log.debug("getWebUser (" + address + ") - SessionContext: " + wu);
		}
		else
		{
			wu = (WebUser)pageContext.getAttribute(WebUser.NAME);
			if (wu != null && Log.isTraceLevel(8))
				log.debug ("getWebUser (" + address + ") - Context: " + wu);
		}
		if (wu != null)
			return wu;

		//	Check Coockie
		String cookieUser = JSPEnv.getCookieWebUser ((HttpServletRequest)pageContext.getRequest());
		if (cookieUser == null || cookieUser.trim().length() == 0)
			log.debug ("getWebUser (" + address + ") - no cookie");
		else
		{
			//	Try to Load
			wu = WebUser.get (ctx, cookieUser);
			log.debug ("getWebUser (" + address + ") - Cookie: " + wu);
		}
		if (wu != null)
			return wu;
		//
		return null;
	}	//	getWebUser

	/*************************************************************************/

	/**
	 * 	Get Login Link
	 * 	@param	serverContext server context
	 * 	@return link
	 */
	private HtmlCode getLoginLink(String serverContext)
	{
		HtmlCode retValue = new HtmlCode();

		input button = new input(input.button, "Login", "Login");
		button.setOnClick("window.top.location.replace('https://" + serverContext + "/loginServlet');");
		retValue.addElement(button);

		/**	Link
		a a = new a("https://" + serverContext + "/login.jsp");
		a.setClass("menuMain");
		a.addElement("Login");
		retValue.addElement(a);
		**/

		retValue.addElement(" ");
		return retValue;
	}	//	getLoginLink

	/**
	 * 	Get Welcome Link
	 * 	@param	serverContext server Context
	 * 	@param wu web user
	 * 	@return link
	 */
	private HtmlCode getWelcomeLink(String serverContext, WebUser wu)
	{
		HtmlCode retValue = new HtmlCode();
		//
		a a = new a("https://" + serverContext + "/login.jsp");
		a.setClass("menuMain");
		String msg = "Welcome " + wu.getName();
		a.addElement(msg);
		retValue.addElement(a);
		//
		retValue.addElement(" &nbsp; ");
		if (wu.isLoggedIn())
		{
			input button = new input(input.button, "Update", "Update");
			button.setOnClick("window.top.location.replace('login.jsp');");
			retValue.addElement(button);

			retValue.addElement(" ");

			button = new input(input.button, "Logout", "Logout");
			button.setOnClick("window.top.location.replace('loginServlet?mode=logout');");
			retValue.addElement(button);

			/** Link
			a = new a ("loginServlet?mode=logout");
			a.setClass ("menuMain");
			a.addElement ("Logout");
			retValue.addElement (a);
			**/
		}
		else
		{
			input button = new input (input.button, "Login", "Login");
			button.setOnClick ("window.top.location.replace('https://" + serverContext + "/login.jsp');");
			retValue.addElement (button);
		}
		retValue.addElement (" ");
		//
		return retValue;
	}	//	getWelcomeLink

}	//	LoginLinkTag
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

import java.util.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.apache.log4j.Logger;


/**
 *  Login Link.
 * 	Creates Login/Logout Link
 *  <pre>
 *  <cws:loginLink />
 *  Variable used - "webUser"
 *	</pre>
 *
 *  @author Jorg Janke
 *  @version $Id: LoginLinkTag.java,v 1.17 2003/08/15 18:05:15 jjanke Exp $
 */
public class LoginLinkTag extends TagSupport
{
	/**	Logger							*/
	protected Logger			log = Logger.getLogger (getClass());

	/**
	 *  Start Tag
	 *  @return SKIP_BODY
	 * 	@throws JspException
	 */
	public int doStartTag() throws JspException
	{
		Properties ctx = JSPEnv.getCtx((HttpServletRequest)pageContext.getRequest());
		//
		WebUser wu = getWebUser(ctx);
		if (wu == null)
			pageContext.getSession().removeAttribute(WebUser.NAME);
		else
			pageContext.getSession().setAttribute (WebUser.NAME, wu);
		//
		String serverContext = ctx.getProperty(JSPEnv.CTX_SERVER_CONTEXT);
	//	log.debug ("doStartTag - ServerContext=" + serverContext);
		HtmlCode html = null;
		if (wu != null && wu.isValid())
			html = getWelcomeLink (serverContext, wu);
		else
			html = getLoginLink (serverContext);
		//
		JspWriter out = pageContext.getOut();
		/**
		//	Delete Cookie Call
		if (cookieUser != null && !cookieUser.equals(" "))
		{
			log.debug("- Cookie=" + cookieUser);
			html.addElement(" ");
			a a = new a("loginServlet?mode=deleteCookie");
			a.setClass("menuDetail");
			a.addElement("(Delete&nbsp;Cookie)");
			html.addElement(a);
		}
		**/
		html.output(out);
		//
		//
		return (SKIP_BODY);
	}   //  doStartTag

	/**
	 * 	End Tag
	 * 	@return EVAL_PAGE
	 * 	@throws JspException
	 */
	public int doEndTag() throws JspException
	{
		return EVAL_PAGE;
	}	//	doEndTag


	/**
	 *	Get WebUser.
	 * 	@param ctx context
	 * 	@return Web User or null
	 */
	private WebUser getWebUser (Properties ctx)
	{
		//	Get stored User
		WebUser wu = (WebUser)pageContext.getSession().getAttribute (WebUser.NAME);
		if (wu != null)
			log.debug("getWebUser - SessionContext:found " + wu);
		else
		{
			wu = (WebUser)pageContext.getAttribute(WebUser.NAME);
			if (wu != null)
				log.debug ("getWebUser - Context:found " + wu);
		}
		if (wu != null)
			return wu;

		//	Check Coockie
		String cookieUser = JSPEnv.getCookieWebUser ((HttpServletRequest)pageContext.getRequest());
		if (cookieUser == null || cookieUser.trim().length() == 0)
			log.debug ("getWebUser - no cookie");
		else
		{
			//	Try to Load
			wu = WebUser.get (ctx, cookieUser);
			log.debug ("getWebUser - got " + wu);
		}
		if (wu != null)
			return wu;
		//
		return null;
	}	//	getWebUser

	/*************************************************************************/

	/**
	 * 	Get Login Link
	 * 	@param	serverContext server context
	 * 	@return link
	 */
	private HtmlCode getLoginLink(String serverContext)
	{
		HtmlCode retValue = new HtmlCode();

		input button = new input(input.button, "Login", "Login");
		button.setOnClick("window.top.location.replace('https://" + serverContext + "/loginServlet');");
		retValue.addElement(button);

		/**	Link
		a a = new a("https://" + serverContext + "/login.jsp");
		a.setClass("menuMain");
		a.addElement("Login");
		retValue.addElement(a);
		**/

		retValue.addElement(" ");
		return retValue;
	}	//	getLoginLink

	/**
	 * 	Get Welcome Link
	 * 	@param	serverContext server Context
	 * 	@param wu web user
	 * 	@return link
	 */
	private HtmlCode getWelcomeLink(String serverContext, WebUser wu)
	{
		HtmlCode retValue = new HtmlCode();
		//
		a a = new a("https://" + serverContext + "/login.jsp");
		a.setClass("menuMain");
		String msg = "Welcome " + wu.getName();
		a.addElement(msg);
		retValue.addElement(a);
		//
		retValue.addElement(" &nbsp; ");
		if (wu.isLoggedIn())
		{
			input button = new input(input.button, "Update", "Update");
			button.setOnClick("window.top.location.replace('login.jsp');");
			retValue.addElement(button);

			retValue.addElement(" ");

			button = new input(input.button, "Logout", "Logout");
			button.setOnClick("window.top.location.replace('loginServlet?mode=logout');");
			retValue.addElement(button);

			/** Link
			a = new a ("loginServlet?mode=logout");
			a.setClass ("menuMain");
			a.addElement ("Logout");
			retValue.addElement (a);
			**/
		}
		else
		{
			input button = new input (input.button, "Login", "Login");
			button.setOnClick ("window.top.location.replace('https://" + serverContext + "/login.jsp');");
			retValue.addElement (button);
		}
		retValue.addElement (" ");
		//
		return retValue;
	}	//	getWelcomeLink

}	//	LoginLinkTag
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

import java.util.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

import org.apache.ecs.xhtml.*;

import org.compiere.util.*;

/**
 *  Login Link.
 * 	Creates Login/Logout Link
 *  <pre>
 *  <cws:loginLink />
 *  Variable used - "webUser"
 *	</pre>
 *
 *  @author Jorg Janke
 *  @version $Id: LoginLinkTag.java,v 1.20 2004/03/05 06:01:27 jjanke Exp $
 */
public class LoginLinkTag extends TagSupport
{
	/**	Logger							*/
	protected Logger			log = Logger.getCLogger (getClass());

	/**
	 *  Start Tag
	 *  @return SKIP_BODY
	 * 	@throws JspException
	 */
	public int doStartTag() throws JspException
	{
		Properties ctx = JSPEnv.getCtx((HttpServletRequest)pageContext.getRequest());
		//
		WebUser wu = getWebUser(ctx);
		if (wu == null)
			pageContext.getSession().removeAttribute(WebUser.NAME);
		else
			pageContext.getSession().setAttribute (WebUser.NAME, wu);
		//
		String serverContext = ctx.getProperty(JSPEnv.CTX_SERVER_CONTEXT);
	//	log.debug ("doStartTag - ServerContext=" + serverContext);
		HtmlCode html = null;
		if (wu != null && wu.isValid())
			html = getWelcomeLink (serverContext, wu);
		else
			html = getLoginLink (serverContext);
		//
		JspWriter out = pageContext.getOut();
		/**
		//	Delete Cookie Call
		if (cookieUser != null && !cookieUser.equals(" "))
		{
			log.debug("- Cookie=" + cookieUser);
			html.addElement(" ");
			a a = new a("loginServlet?mode=deleteCookie");
			a.setClass("menuDetail");
			a.addElement("(Delete&nbsp;Cookie)");
			html.addElement(a);
		}
		**/
		html.output(out);
		//
		//
		return (SKIP_BODY);
	}   //  doStartTag

	/**
	 * 	End Tag
	 * 	@return EVAL_PAGE
	 * 	@throws JspException
	 */
	public int doEndTag() throws JspException
	{
		return EVAL_PAGE;
	}	//	doEndTag


	/**
	 *	Get WebUser.
	 * 	@param ctx context
	 * 	@return Web User or null
	 */
	private WebUser getWebUser (Properties ctx)
	{
		String address = pageContext.getRequest().getRemoteAddr();
		//	Get stored User
		WebUser wu = (WebUser)pageContext.getSession().getAttribute (WebUser.NAME);
		if (wu != null)
		{
			if (Log.isTraceLevel(8))
				log.debug("getWebUser (" + address + ") - SessionContext: " + wu);
		}
		else
		{
			wu = (WebUser)pageContext.getAttribute(WebUser.NAME);
			if (wu != null && Log.isTraceLevel(8))
				log.debug ("getWebUser (" + address + ") - Context: " + wu);
		}
		if (wu != null)
			return wu;

		//	Check Coockie
		String cookieUser = JSPEnv.getCookieWebUser ((HttpServletRequest)pageContext.getRequest());
		if (cookieUser == null || cookieUser.trim().length() == 0)
			log.debug ("getWebUser (" + address + ") - no cookie");
		else
		{
			//	Try to Load
			wu = WebUser.get (ctx, cookieUser);
			log.debug ("getWebUser (" + address + ") - Cookie: " + wu);
		}
		if (wu != null)
			return wu;
		//
		return null;
	}	//	getWebUser

	/*************************************************************************/

	/**
	 * 	Get Login Link
	 * 	@param	serverContext server context
	 * 	@return link
	 */
	private HtmlCode getLoginLink(String serverContext)
	{
		HtmlCode retValue = new HtmlCode();

		input button = new input(input.button, "Login", "Login");
		button.setOnClick("window.top.location.replace('https://" + serverContext + "/loginServlet');");
		retValue.addElement(button);

		/**	Link
		a a = new a("https://" + serverContext + "/login.jsp");
		a.setClass("menuMain");
		a.addElement("Login");
		retValue.addElement(a);
		**/

		retValue.addElement(" ");
		return retValue;
	}	//	getLoginLink

	/**
	 * 	Get Welcome Link
	 * 	@param	serverContext server Context
	 * 	@param wu web user
	 * 	@return link
	 */
	private HtmlCode getWelcomeLink(String serverContext, WebUser wu)
	{
		HtmlCode retValue = new HtmlCode();
		//
		a a = new a("https://" + serverContext + "/login.jsp");
		a.setClass("menuMain");
		String msg = "Welcome " + wu.getName();
		a.addElement(msg);
		retValue.addElement(a);
		//
		retValue.addElement(" &nbsp; ");
		if (wu.isLoggedIn())
		{
			input button = new input(input.button, "Update", "Update");
			button.setOnClick("window.top.location.replace('login.jsp');");
			retValue.addElement(button);

			retValue.addElement(" ");

			button = new input(input.button, "Logout", "Logout");
			button.setOnClick("window.top.location.replace('loginServlet?mode=logout');");
			retValue.addElement(button);

			/** Link
			a = new a ("loginServlet?mode=logout");
			a.setClass ("menuMain");
			a.addElement ("Logout");
			retValue.addElement (a);
			**/
		}
		else
		{
			input button = new input (input.button, "Login", "Login");
			button.setOnClick ("window.top.location.replace('https://" + serverContext + "/login.jsp');");
			retValue.addElement (button);
		}
		retValue.addElement (" ");
		//
		return retValue;
	}	//	getWelcomeLink

}	//	LoginLinkTag
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

import java.util.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.apache.log4j.Logger;


/**
 *  Login Link.
 * 	Creates Login/Logout Link
 *  <pre>
 *  <cws:loginLink />
 *  Variable used - "webUser"
 *	</pre>
 *
 *  @author Jorg Janke
 *  @version $Id: LoginLinkTag.java,v 1.17 2003/08/15 18:05:15 jjanke Exp $
 */
public class LoginLinkTag extends TagSupport
{
	/**	Logger							*/
	protected Logger			log = Logger.getLogger (getClass());

	/**
	 *  Start Tag
	 *  @return SKIP_BODY
	 * 	@throws JspException
	 */
	public int doStartTag() throws JspException
	{
		Properties ctx = JSPEnv.getCtx((HttpServletRequest)pageContext.getRequest());
		//
		WebUser wu = getWebUser(ctx);
		if (wu == null)
			pageContext.getSession().removeAttribute(WebUser.NAME);
		else
			pageContext.getSession().setAttribute (WebUser.NAME, wu);
		//
		String serverContext = ctx.getProperty(JSPEnv.CTX_SERVER_CONTEXT);
	//	log.debug ("doStartTag - ServerContext=" + serverContext);
		HtmlCode html = null;
		if (wu != null && wu.isValid())
			html = getWelcomeLink (serverContext, wu);
		else
			html = getLoginLink (serverContext);
		//
		JspWriter out = pageContext.getOut();
		/**
		//	Delete Cookie Call
		if (cookieUser != null && !cookieUser.equals(" "))
		{
			log.debug("- Cookie=" + cookieUser);
			html.addElement(" ");
			a a = new a("loginServlet?mode=deleteCookie");
			a.setClass("menuDetail");
			a.addElement("(Delete&nbsp;Cookie)");
			html.addElement(a);
		}
		**/
		html.output(out);
		//
		//
		return (SKIP_BODY);
	}   //  doStartTag

	/**
	 * 	End Tag
	 * 	@return EVAL_PAGE
	 * 	@throws JspException
	 */
	public int doEndTag() throws JspException
	{
		return EVAL_PAGE;
	}	//	doEndTag


	/**
	 *	Get WebUser.
	 * 	@param ctx context
	 * 	@return Web User or null
	 */
	private WebUser getWebUser (Properties ctx)
	{
		//	Get stored User
		WebUser wu = (WebUser)pageContext.getSession().getAttribute (WebUser.NAME);
		if (wu != null)
			log.debug("getWebUser - SessionContext:found " + wu);
		else
		{
			wu = (WebUser)pageContext.getAttribute(WebUser.NAME);
			if (wu != null)
				log.debug ("getWebUser - Context:found " + wu);
		}
		if (wu != null)
			return wu;

		//	Check Coockie
		String cookieUser = JSPEnv.getCookieWebUser ((HttpServletRequest)pageContext.getRequest());
		if (cookieUser == null || cookieUser.trim().length() == 0)
			log.debug ("getWebUser - no cookie");
		else
		{
			//	Try to Load
			wu = WebUser.get (ctx, cookieUser);
			log.debug ("getWebUser - got " + wu);
		}
		if (wu != null)
			return wu;
		//
		return null;
	}	//	getWebUser

	/*************************************************************************/

	/**
	 * 	Get Login Link
	 * 	@param	serverContext server context
	 * 	@return link
	 */
	private HtmlCode getLoginLink(String serverContext)
	{
		HtmlCode retValue = new HtmlCode();

		input button = new input(input.button, "Login", "Login");
		button.setOnClick("window.top.location.replace('https://" + serverContext + "/loginServlet');");
		retValue.addElement(button);

		/**	Link
		a a = new a("https://" + serverContext + "/login.jsp");
		a.setClass("menuMain");
		a.addElement("Login");
		retValue.addElement(a);
		**/

		retValue.addElement(" ");
		return retValue;
	}	//	getLoginLink

	/**
	 * 	Get Welcome Link
	 * 	@param	serverContext server Context
	 * 	@param wu web user
	 * 	@return link
	 */
	private HtmlCode getWelcomeLink(String serverContext, WebUser wu)
	{
		HtmlCode retValue = new HtmlCode();
		//
		a a = new a("https://" + serverContext + "/login.jsp");
		a.setClass("menuMain");
		String msg = "Welcome " + wu.getName();
		a.addElement(msg);
		retValue.addElement(a);
		//
		retValue.addElement(" &nbsp; ");
		if (wu.isLoggedIn())
		{
			input button = new input(input.button, "Update", "Update");
			button.setOnClick("window.top.location.replace('login.jsp');");
			retValue.addElement(button);

			retValue.addElement(" ");

			button = new input(input.button, "Logout", "Logout");
			button.setOnClick("window.top.location.replace('loginServlet?mode=logout');");
			retValue.addElement(button);

			/** Link
			a = new a ("loginServlet?mode=logout");
			a.setClass ("menuMain");
			a.addElement ("Logout");
			retValue.addElement (a);
			**/
		}
		else
		{
			input button = new input (input.button, "Login", "Login");
			button.setOnClick ("window.top.location.replace('https://" + serverContext + "/login.jsp');");
			retValue.addElement (button);
		}
		retValue.addElement (" ");
		//
		return retValue;
	}	//	getWelcomeLink

}	//	LoginLinkTag
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

import java.util.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.apache.log4j.Logger;


/**
 *  Login Link.
 * 	Creates Login/Logout Link
 *  <pre>
 *  <cws:loginLink />
 *  Variable used - "webUser"
 *	</pre>
 *
 *  @author Jorg Janke
 *  @version $Id: LoginLinkTag.java,v 1.17 2003/08/15 18:05:15 jjanke Exp $
 */
public class LoginLinkTag extends TagSupport
{
	/**	Logger							*/
	protected Logger			log = Logger.getLogger (getClass());

	/**
	 *  Start Tag
	 *  @return SKIP_BODY
	 * 	@throws JspException
	 */
	public int doStartTag() throws JspException
	{
		Properties ctx = JSPEnv.getCtx((HttpServletRequest)pageContext.getRequest());
		//
		WebUser wu = getWebUser(ctx);
		if (wu == null)
			pageContext.getSession().removeAttribute(WebUser.NAME);
		else
			pageContext.getSession().setAttribute (WebUser.NAME, wu);
		//
		String serverContext = ctx.getProperty(JSPEnv.CTX_SERVER_CONTEXT);
	//	log.debug ("doStartTag - ServerContext=" + serverContext);
		HtmlCode html = null;
		if (wu != null && wu.isValid())
			html = getWelcomeLink (serverContext, wu);
		else
			html = getLoginLink (serverContext);
		//
		JspWriter out = pageContext.getOut();
		/**
		//	Delete Cookie Call
		if (cookieUser != null && !cookieUser.equals(" "))
		{
			log.debug("- Cookie=" + cookieUser);
			html.addElement(" ");
			a a = new a("loginServlet?mode=deleteCookie");
			a.setClass("menuDetail");
			a.addElement("(Delete&nbsp;Cookie)");
			html.addElement(a);
		}
		**/
		html.output(out);
		//
		//
		return (SKIP_BODY);
	}   //  doStartTag

	/**
	 * 	End Tag
	 * 	@return EVAL_PAGE
	 * 	@throws JspException
	 */
	public int doEndTag() throws JspException
	{
		return EVAL_PAGE;
	}	//	doEndTag


	/**
	 *	Get WebUser.
	 * 	@param ctx context
	 * 	@return Web User or null
	 */
	private WebUser getWebUser (Properties ctx)
	{
		//	Get stored User
		WebUser wu = (WebUser)pageContext.getSession().getAttribute (WebUser.NAME);
		if (wu != null)
			log.debug("getWebUser - SessionContext:found " + wu);
		else
		{
			wu = (WebUser)pageContext.getAttribute(WebUser.NAME);
			if (wu != null)
				log.debug ("getWebUser - Context:found " + wu);
		}
		if (wu != null)
			return wu;

		//	Check Coockie
		String cookieUser = JSPEnv.getCookieWebUser ((HttpServletRequest)pageContext.getRequest());
		if (cookieUser == null || cookieUser.trim().length() == 0)
			log.debug ("getWebUser - no cookie");
		else
		{
			//	Try to Load
			wu = WebUser.get (ctx, cookieUser);
			log.debug ("getWebUser - got " + wu);
		}
		if (wu != null)
			return wu;
		//
		return null;
	}	//	getWebUser

	/*************************************************************************/

	/**
	 * 	Get Login Link
	 * 	@param	serverContext server context
	 * 	@return link
	 */
	private HtmlCode getLoginLink(String serverContext)
	{
		HtmlCode retValue = new HtmlCode();

		input button = new input(input.button, "Login", "Login");
		button.setOnClick("window.top.location.replace('https://" + serverContext + "/loginServlet');");
		retValue.addElement(button);

		/**	Link
		a a = new a("https://" + serverContext + "/login.jsp");
		a.setClass("menuMain");
		a.addElement("Login");
		retValue.addElement(a);
		**/

		retValue.addElement(" ");
		return retValue;
	}	//	getLoginLink

	/**
	 * 	Get Welcome Link
	 * 	@param	serverContext server Context
	 * 	@param wu web user
	 * 	@return link
	 */
	private HtmlCode getWelcomeLink(String serverContext, WebUser wu)
	{
		HtmlCode retValue = new HtmlCode();
		//
		a a = new a("https://" + serverContext + "/login.jsp");
		a.setClass("menuMain");
		String msg = "Welcome " + wu.getName();
		a.addElement(msg);
		retValue.addElement(a);
		//
		retValue.addElement(" &nbsp; ");
		if (wu.isLoggedIn())
		{
			input button = new input(input.button, "Update", "Update");
			button.setOnClick("window.top.location.replace('login.jsp');");
			retValue.addElement(button);

			retValue.addElement(" ");

			button = new input(input.button, "Logout", "Logout");
			button.setOnClick("window.top.location.replace('loginServlet?mode=logout');");
			retValue.addElement(button);

			/** Link
			a = new a ("loginServlet?mode=logout");
			a.setClass ("menuMain");
			a.addElement ("Logout");
			retValue.addElement (a);
			**/
		}
		else
		{
			input button = new input (input.button, "Login", "Login");
			button.setOnClick ("window.top.location.replace('https://" + serverContext + "/login.jsp');");
			retValue.addElement (button);
		}
		retValue.addElement (" ");
		//
		return retValue;
	}	//	getWelcomeLink

}	//	LoginLinkTag
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

import java.util.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

import org.apache.ecs.xhtml.*;

import org.compiere.util.*;

/**
 *  Login Link.
 * 	Creates Login/Logout Link
 *  <pre>
 *  <cws:loginLink />
 *  Variable used - "webUser"
 *	</pre>
 *
 *  @author Jorg Janke
 *  @version $Id: LoginLinkTag.java,v 1.20 2004/03/05 06:01:27 jjanke Exp $
 */
public class LoginLinkTag extends TagSupport
{
	/**	Logger							*/
	protected Logger			log = Logger.getCLogger (getClass());

	/**
	 *  Start Tag
	 *  @return SKIP_BODY
	 * 	@throws JspException
	 */
	public int doStartTag() throws JspException
	{
		Properties ctx = JSPEnv.getCtx((HttpServletRequest)pageContext.getRequest());
		//
		WebUser wu = getWebUser(ctx);
		if (wu == null)
			pageContext.getSession().removeAttribute(WebUser.NAME);
		else
			pageContext.getSession().setAttribute (WebUser.NAME, wu);
		//
		String serverContext = ctx.getProperty(JSPEnv.CTX_SERVER_CONTEXT);
	//	log.debug ("doStartTag - ServerContext=" + serverContext);
		HtmlCode html = null;
		if (wu != null && wu.isValid())
			html = getWelcomeLink (serverContext, wu);
		else
			html = getLoginLink (serverContext);
		//
		JspWriter out = pageContext.getOut();
		/**
		//	Delete Cookie Call
		if (cookieUser != null && !cookieUser.equals(" "))
		{
			log.debug("- Cookie=" + cookieUser);
			html.addElement(" ");
			a a = new a("loginServlet?mode=deleteCookie");
			a.setClass("menuDetail");
			a.addElement("(Delete&nbsp;Cookie)");
			html.addElement(a);
		}
		**/
		html.output(out);
		//
		//
		return (SKIP_BODY);
	}   //  doStartTag

	/**
	 * 	End Tag
	 * 	@return EVAL_PAGE
	 * 	@throws JspException
	 */
	public int doEndTag() throws JspException
	{
		return EVAL_PAGE;
	}	//	doEndTag


	/**
	 *	Get WebUser.
	 * 	@param ctx context
	 * 	@return Web User or null
	 */
	private WebUser getWebUser (Properties ctx)
	{
		String address = pageContext.getRequest().getRemoteAddr();
		//	Get stored User
		WebUser wu = (WebUser)pageContext.getSession().getAttribute (WebUser.NAME);
		if (wu != null)
		{
			if (Log.isTraceLevel(8))
				log.debug("getWebUser (" + address + ") - SessionContext: " + wu);
		}
		else
		{
			wu = (WebUser)pageContext.getAttribute(WebUser.NAME);
			if (wu != null && Log.isTraceLevel(8))
				log.debug ("getWebUser (" + address + ") - Context: " + wu);
		}
		if (wu != null)
			return wu;

		//	Check Coockie
		String cookieUser = JSPEnv.getCookieWebUser ((HttpServletRequest)pageContext.getRequest());
		if (cookieUser == null || cookieUser.trim().length() == 0)
			log.debug ("getWebUser (" + address + ") - no cookie");
		else
		{
			//	Try to Load
			wu = WebUser.get (ctx, cookieUser);
			log.debug ("getWebUser (" + address + ") - Cookie: " + wu);
		}
		if (wu != null)
			return wu;
		//
		return null;
	}	//	getWebUser

	/*************************************************************************/

	/**
	 * 	Get Login Link
	 * 	@param	serverContext server context
	 * 	@return link
	 */
	private HtmlCode getLoginLink(String serverContext)
	{
		HtmlCode retValue = new HtmlCode();

		input button = new input(input.button, "Login", "Login");
		button.setOnClick("window.top.location.replace('https://" + serverContext + "/loginServlet');");
		retValue.addElement(button);

		/**	Link
		a a = new a("https://" + serverContext + "/login.jsp");
		a.setClass("menuMain");
		a.addElement("Login");
		retValue.addElement(a);
		**/

		retValue.addElement(" ");
		return retValue;
	}	//	getLoginLink

	/**
	 * 	Get Welcome Link
	 * 	@param	serverContext server Context
	 * 	@param wu web user
	 * 	@return link
	 */
	private HtmlCode getWelcomeLink(String serverContext, WebUser wu)
	{
		HtmlCode retValue = new HtmlCode();
		//
		a a = new a("https://" + serverContext + "/login.jsp");
		a.setClass("menuMain");
		String msg = "Welcome " + wu.getName();
		a.addElement(msg);
		retValue.addElement(a);
		//
		retValue.addElement(" &nbsp; ");
		if (wu.isLoggedIn())
		{
			input button = new input(input.button, "Update", "Update");
			button.setOnClick("window.top.location.replace('login.jsp');");
			retValue.addElement(button);

			retValue.addElement(" ");

			button = new input(input.button, "Logout", "Logout");
			button.setOnClick("window.top.location.replace('loginServlet?mode=logout');");
			retValue.addElement(button);

			/** Link
			a = new a ("loginServlet?mode=logout");
			a.setClass ("menuMain");
			a.addElement ("Logout");
			retValue.addElement (a);
			**/
		}
		else
		{
			input button = new input (input.button, "Login", "Login");
			button.setOnClick ("window.top.location.replace('https://" + serverContext + "/login.jsp');");
			retValue.addElement (button);
		}
		retValue.addElement (" ");
		//
		return retValue;
	}	//	getWelcomeLink

}	//	LoginLinkTag
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

import java.util.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.apache.log4j.Logger;


/**
 *  Login Link.
 * 	Creates Login/Logout Link
 *  <pre>
 *  <cws:loginLink />
 *  Variable used - "webUser"
 *	</pre>
 *
 *  @author Jorg Janke
 *  @version $Id: LoginLinkTag.java,v 1.16 2003/07/16 19:12:12 jjanke Exp $
 */
public class LoginLinkTag extends TagSupport
{
	/**	Logger							*/
	protected Logger			log = Logger.getLogger (getClass());

	/**
	 *  Start Tag
	 *  @return SKIP_BODY
	 * 	@throws JspException
	 */
	public int doStartTag() throws JspException
	{
		Properties ctx = JSPEnv.getCtx((HttpServletRequest)pageContext.getRequest());
		//
		WebUser wu = getWebUser(ctx);
		if (wu == null)
			pageContext.getSession().removeAttribute(WebUser.NAME);
		else
			pageContext.getSession().setAttribute (WebUser.NAME, wu);
		//
		String serverContext = ctx.getProperty(JSPEnv.CTX_SERVER_CONTEXT);
	//	log.debug ("doStartTag - ServerContext=" + serverContext);
		HtmlCode html = null;
		if (wu != null && wu.isValid())
			html = getWelcomeLink (serverContext, wu);
		else
			html = getLoginLink (serverContext);
		//
		JspWriter out = pageContext.getOut();
		/**
		//	Delete Cookie Call
		if (cookieUser != null && !cookieUser.equals(" "))
		{
			log.debug("- Cookie=" + cookieUser);
			html.addElement(" ");
			a a = new a("loginServlet?mode=deleteCookie");
			a.setClass("menuDetail");
			a.addElement("(Delete&nbsp;Cookie)");
			html.addElement(a);
		}
		**/
		html.output(out);
		//
		//
		return (SKIP_BODY);
	}   //  doStartTag

	/**
	 * 	End Tag
	 * 	@return EVAL_PAGE
	 * 	@throws JspException
	 */
	public int doEndTag() throws JspException
	{
		return EVAL_PAGE;
	}	//	doEndTag


	/**
	 *	Get WebUser.
	 * 	@param ctx context
	 * 	@return Web User or null
	 */
	private WebUser getWebUser (Properties ctx)
	{
		//	Get stored User
		WebUser wu = (WebUser)pageContext.getSession().getAttribute (WebUser.NAME);
		if (wu != null)
			log.debug("getWebUser - SessionContext:found " + wu);
		else
		{
			wu = (WebUser)pageContext.getAttribute(WebUser.NAME);
			if (wu != null)
				log.debug ("getWebUser - Context:found " + wu);
		}
		if (wu != null)
			return wu;

		//	Check Coockie
		String cookieUser = JSPEnv.getCookieWebUser ((HttpServletRequest)pageContext.getRequest());
		if (cookieUser == null || cookieUser.trim().length() == 0)
			log.debug ("getWebUser - no cookie");
		else
		{
			//	Try to Load
			wu = WebUser.get (ctx, cookieUser, null);
			log.debug ("getWebUser - got " + wu);
		}
		if (wu != null)
			return wu;
		//
		return null;
	}	//	getWebUser

	/*************************************************************************/

	/**
	 * 	Get Login Link
	 * 	@param	serverContext server context
	 * 	@return link
	 */
	private HtmlCode getLoginLink(String serverContext)
	{
		HtmlCode retValue = new HtmlCode();

		input button = new input(input.button, "Login", "Login");
		button.setOnClick("window.top.location.replace('https://" + serverContext + "/loginServlet');");
		retValue.addElement(button);

		/**	Link
		a a = new a("https://" + serverContext + "/login.jsp");
		a.setClass("menuMain");
		a.addElement("Login");
		retValue.addElement(a);
		**/

		retValue.addElement(" ");
		return retValue;
	}	//	getLoginLink

	/**
	 * 	Get Welcome Link
	 * 	@param	serverContext server Context
	 * 	@param wu web user
	 * 	@return link
	 */
	private HtmlCode getWelcomeLink(String serverContext, WebUser wu)
	{
		HtmlCode retValue = new HtmlCode();
		//
		a a = new a("https://" + serverContext + "/login.jsp");
		a.setClass("menuMain");
		String msg = "Welcome " + wu.getName();
		a.addElement(msg);
		retValue.addElement(a);
		//
		retValue.addElement(" &nbsp; ");
		if (wu.isLoggedIn())
		{
			input button = new input(input.button, "Update", "Update");
			button.setOnClick("window.top.location.replace('login.jsp');");
			retValue.addElement(button);

			retValue.addElement(" ");

			button = new input(input.button, "Logout", "Logout");
			button.setOnClick("window.top.location.replace('loginServlet?mode=logout');");
			retValue.addElement(button);

			/** Link
			a = new a ("loginServlet?mode=logout");
			a.setClass ("menuMain");
			a.addElement ("Logout");
			retValue.addElement (a);
			**/
		}
		else
		{
			input button = new input (input.button, "Login", "Login");
			button.setOnClick ("window.top.location.replace('https://" + serverContext + "/login.jsp');");
			retValue.addElement (button);
		}
		retValue.addElement (" ");
		//
		return retValue;
	}	//	getWelcomeLink

}	//	LoginLinkTag
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

import java.util.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.apache.log4j.Logger;


/**
 *  Login Link.
 * 	Creates Login/Logout Link
 *  <pre>
 *  <cws:loginLink />
 *  Variable used - "webUser"
 *	</pre>
 *
 *  @author Jorg Janke
 *  @version $Id: LoginLinkTag.java,v 1.17 2003/08/15 18:05:15 jjanke Exp $
 */
public class LoginLinkTag extends TagSupport
{
	/**	Logger							*/
	protected Logger			log = Logger.getLogger (getClass());

	/**
	 *  Start Tag
	 *  @return SKIP_BODY
	 * 	@throws JspException
	 */
	public int doStartTag() throws JspException
	{
		Properties ctx = JSPEnv.getCtx((HttpServletRequest)pageContext.getRequest());
		//
		WebUser wu = getWebUser(ctx);
		if (wu == null)
			pageContext.getSession().removeAttribute(WebUser.NAME);
		else
			pageContext.getSession().setAttribute (WebUser.NAME, wu);
		//
		String serverContext = ctx.getProperty(JSPEnv.CTX_SERVER_CONTEXT);
	//	log.debug ("doStartTag - ServerContext=" + serverContext);
		HtmlCode html = null;
		if (wu != null && wu.isValid())
			html = getWelcomeLink (serverContext, wu);
		else
			html = getLoginLink (serverContext);
		//
		JspWriter out = pageContext.getOut();
		/**
		//	Delete Cookie Call
		if (cookieUser != null && !cookieUser.equals(" "))
		{
			log.debug("- Cookie=" + cookieUser);
			html.addElement(" ");
			a a = new a("loginServlet?mode=deleteCookie");
			a.setClass("menuDetail");
			a.addElement("(Delete&nbsp;Cookie)");
			html.addElement(a);
		}
		**/
		html.output(out);
		//
		//
		return (SKIP_BODY);
	}   //  doStartTag

	/**
	 * 	End Tag
	 * 	@return EVAL_PAGE
	 * 	@throws JspException
	 */
	public int doEndTag() throws JspException
	{
		return EVAL_PAGE;
	}	//	doEndTag


	/**
	 *	Get WebUser.
	 * 	@param ctx context
	 * 	@return Web User or null
	 */
	private WebUser getWebUser (Properties ctx)
	{
		//	Get stored User
		WebUser wu = (WebUser)pageContext.getSession().getAttribute (WebUser.NAME);
		if (wu != null)
			log.debug("getWebUser - SessionContext:found " + wu);
		else
		{
			wu = (WebUser)pageContext.getAttribute(WebUser.NAME);
			if (wu != null)
				log.debug ("getWebUser - Context:found " + wu);
		}
		if (wu != null)
			return wu;

		//	Check Coockie
		String cookieUser = JSPEnv.getCookieWebUser ((HttpServletRequest)pageContext.getRequest());
		if (cookieUser == null || cookieUser.trim().length() == 0)
			log.debug ("getWebUser - no cookie");
		else
		{
			//	Try to Load
			wu = WebUser.get (ctx, cookieUser);
			log.debug ("getWebUser - got " + wu);
		}
		if (wu != null)
			return wu;
		//
		return null;
	}	//	getWebUser

	/*************************************************************************/

	/**
	 * 	Get Login Link
	 * 	@param	serverContext server context
	 * 	@return link
	 */
	private HtmlCode getLoginLink(String serverContext)
	{
		HtmlCode retValue = new HtmlCode();

		input button = new input(input.button, "Login", "Login");
		button.setOnClick("window.top.location.replace('https://" + serverContext + "/loginServlet');");
		retValue.addElement(button);

		/**	Link
		a a = new a("https://" + serverContext + "/login.jsp");
		a.setClass("menuMain");
		a.addElement("Login");
		retValue.addElement(a);
		**/

		retValue.addElement(" ");
		return retValue;
	}	//	getLoginLink

	/**
	 * 	Get Welcome Link
	 * 	@param	serverContext server Context
	 * 	@param wu web user
	 * 	@return link
	 */
	private HtmlCode getWelcomeLink(String serverContext, WebUser wu)
	{
		HtmlCode retValue = new HtmlCode();
		//
		a a = new a("https://" + serverContext + "/login.jsp");
		a.setClass("menuMain");
		String msg = "Welcome " + wu.getName();
		a.addElement(msg);
		retValue.addElement(a);
		//
		retValue.addElement(" &nbsp; ");
		if (wu.isLoggedIn())
		{
			input button = new input(input.button, "Update", "Update");
			button.setOnClick("window.top.location.replace('login.jsp');");
			retValue.addElement(button);

			retValue.addElement(" ");

			button = new input(input.button, "Logout", "Logout");
			button.setOnClick("window.top.location.replace('loginServlet?mode=logout');");
			retValue.addElement(button);

			/** Link
			a = new a ("loginServlet?mode=logout");
			a.setClass ("menuMain");
			a.addElement ("Logout");
			retValue.addElement (a);
			**/
		}
		else
		{
			input button = new input (input.button, "Login", "Login");
			button.setOnClick ("window.top.location.replace('https://" + serverContext + "/login.jsp');");
			retValue.addElement (button);
		}
		retValue.addElement (" ");
		//
		return retValue;
	}	//	getWelcomeLink

}	//	LoginLinkTag
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

import java.util.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

import org.apache.ecs.xhtml.*;

import org.compiere.util.*;

/**
 *  Login Link.
 * 	Creates Login/Logout Link
 *  <pre>
 *  <cws:loginLink />
 *  Variable used - "webUser"
 *	</pre>
 *
 *  @author Jorg Janke
 *  @version $Id: LoginLinkTag.java,v 1.20 2004/03/05 06:01:27 jjanke Exp $
 */
public class LoginLinkTag extends TagSupport
{
	/**	Logger							*/
	protected Logger			log = Logger.getCLogger (getClass());

	/**
	 *  Start Tag
	 *  @return SKIP_BODY
	 * 	@throws JspException
	 */
	public int doStartTag() throws JspException
	{
		Properties ctx = JSPEnv.getCtx((HttpServletRequest)pageContext.getRequest());
		//
		WebUser wu = getWebUser(ctx);
		if (wu == null)
			pageContext.getSession().removeAttribute(WebUser.NAME);
		else
			pageContext.getSession().setAttribute (WebUser.NAME, wu);
		//
		String serverContext = ctx.getProperty(JSPEnv.CTX_SERVER_CONTEXT);
	//	log.debug ("doStartTag - ServerContext=" + serverContext);
		HtmlCode html = null;
		if (wu != null && wu.isValid())
			html = getWelcomeLink (serverContext, wu);
		else
			html = getLoginLink (serverContext);
		//
		JspWriter out = pageContext.getOut();
		/**
		//	Delete Cookie Call
		if (cookieUser != null && !cookieUser.equals(" "))
		{
			log.debug("- Cookie=" + cookieUser);
			html.addElement(" ");
			a a = new a("loginServlet?mode=deleteCookie");
			a.setClass("menuDetail");
			a.addElement("(Delete&nbsp;Cookie)");
			html.addElement(a);
		}
		**/
		html.output(out);
		//
		//
		return (SKIP_BODY);
	}   //  doStartTag

	/**
	 * 	End Tag
	 * 	@return EVAL_PAGE
	 * 	@throws JspException
	 */
	public int doEndTag() throws JspException
	{
		return EVAL_PAGE;
	}	//	doEndTag


	/**
	 *	Get WebUser.
	 * 	@param ctx context
	 * 	@return Web User or null
	 */
	private WebUser getWebUser (Properties ctx)
	{
		String address = pageContext.getRequest().getRemoteAddr();
		//	Get stored User
		WebUser wu = (WebUser)pageContext.getSession().getAttribute (WebUser.NAME);
		if (wu != null)
		{
			if (Log.isTraceLevel(8))
				log.debug("getWebUser (" + address + ") - SessionContext: " + wu);
		}
		else
		{
			wu = (WebUser)pageContext.getAttribute(WebUser.NAME);
			if (wu != null && Log.isTraceLevel(8))
				log.debug ("getWebUser (" + address + ") - Context: " + wu);
		}
		if (wu != null)
			return wu;

		//	Check Coockie
		String cookieUser = JSPEnv.getCookieWebUser ((HttpServletRequest)pageContext.getRequest());
		if (cookieUser == null || cookieUser.trim().length() == 0)
			log.debug ("getWebUser (" + address + ") - no cookie");
		else
		{
			//	Try to Load
			wu = WebUser.get (ctx, cookieUser);
			log.debug ("getWebUser (" + address + ") - Cookie: " + wu);
		}
		if (wu != null)
			return wu;
		//
		return null;
	}	//	getWebUser

	/*************************************************************************/

	/**
	 * 	Get Login Link
	 * 	@param	serverContext server context
	 * 	@return link
	 */
	private HtmlCode getLoginLink(String serverContext)
	{
		HtmlCode retValue = new HtmlCode();

		input button = new input(input.button, "Login", "Login");
		button.setOnClick("window.top.location.replace('https://" + serverContext + "/loginServlet');");
		retValue.addElement(button);

		/**	Link
		a a = new a("https://" + serverContext + "/login.jsp");
		a.setClass("menuMain");
		a.addElement("Login");
		retValue.addElement(a);
		**/

		retValue.addElement(" ");
		return retValue;
	}	//	getLoginLink

	/**
	 * 	Get Welcome Link
	 * 	@param	serverContext server Context
	 * 	@param wu web user
	 * 	@return link
	 */
	private HtmlCode getWelcomeLink(String serverContext, WebUser wu)
	{
		HtmlCode retValue = new HtmlCode();
		//
		a a = new a("https://" + serverContext + "/login.jsp");
		a.setClass("menuMain");
		String msg = "Welcome " + wu.getName();
		a.addElement(msg);
		retValue.addElement(a);
		//
		retValue.addElement(" &nbsp; ");
		if (wu.isLoggedIn())
		{
			input button = new input(input.button, "Update", "Update");
			button.setOnClick("window.top.location.replace('login.jsp');");
			retValue.addElement(button);

			retValue.addElement(" ");

			button = new input(input.button, "Logout", "Logout");
			button.setOnClick("window.top.location.replace('loginServlet?mode=logout');");
			retValue.addElement(button);

			/** Link
			a = new a ("loginServlet?mode=logout");
			a.setClass ("menuMain");
			a.addElement ("Logout");
			retValue.addElement (a);
			**/
		}
		else
		{
			input button = new input (input.button, "Login", "Login");
			button.setOnClick ("window.top.location.replace('https://" + serverContext + "/login.jsp');");
			retValue.addElement (button);
		}
		retValue.addElement (" ");
		//
		return retValue;
	}	//	getWelcomeLink

}	//	LoginLinkTag
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

import java.util.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.apache.log4j.Logger;


/**
 *  Login Link.
 * 	Creates Login/Logout Link
 *  <pre>
 *  <cws:loginLink />
 *  Variable used - "webUser"
 *	</pre>
 *
 *  @author Jorg Janke
 *  @version $Id: LoginLinkTag.java,v 1.16 2003/07/16 19:12:12 jjanke Exp $
 */
public class LoginLinkTag extends TagSupport
{
	/**	Logger							*/
	protected Logger			log = Logger.getLogger (getClass());

	/**
	 *  Start Tag
	 *  @return SKIP_BODY
	 * 	@throws JspException
	 */
	public int doStartTag() throws JspException
	{
		Properties ctx = JSPEnv.getCtx((HttpServletRequest)pageContext.getRequest());
		//
		WebUser wu = getWebUser(ctx);
		if (wu == null)
			pageContext.getSession().removeAttribute(WebUser.NAME);
		else
			pageContext.getSession().setAttribute (WebUser.NAME, wu);
		//
		String serverContext = ctx.getProperty(JSPEnv.CTX_SERVER_CONTEXT);
	//	log.debug ("doStartTag - ServerContext=" + serverContext);
		HtmlCode html = null;
		if (wu != null && wu.isValid())
			html = getWelcomeLink (serverContext, wu);
		else
			html = getLoginLink (serverContext);
		//
		JspWriter out = pageContext.getOut();
		/**
		//	Delete Cookie Call
		if (cookieUser != null && !cookieUser.equals(" "))
		{
			log.debug("- Cookie=" + cookieUser);
			html.addElement(" ");
			a a = new a("loginServlet?mode=deleteCookie");
			a.setClass("menuDetail");
			a.addElement("(Delete&nbsp;Cookie)");
			html.addElement(a);
		}
		**/
		html.output(out);
		//
		//
		return (SKIP_BODY);
	}   //  doStartTag

	/**
	 * 	End Tag
	 * 	@return EVAL_PAGE
	 * 	@throws JspException
	 */
	public int doEndTag() throws JspException
	{
		return EVAL_PAGE;
	}	//	doEndTag


	/**
	 *	Get WebUser.
	 * 	@param ctx context
	 * 	@return Web User or null
	 */
	private WebUser getWebUser (Properties ctx)
	{
		//	Get stored User
		WebUser wu = (WebUser)pageContext.getSession().getAttribute (WebUser.NAME);
		if (wu != null)
			log.debug("getWebUser - SessionContext:found " + wu);
		else
		{
			wu = (WebUser)pageContext.getAttribute(WebUser.NAME);
			if (wu != null)
				log.debug ("getWebUser - Context:found " + wu);
		}
		if (wu != null)
			return wu;

		//	Check Coockie
		String cookieUser = JSPEnv.getCookieWebUser ((HttpServletRequest)pageContext.getRequest());
		if (cookieUser == null || cookieUser.trim().length() == 0)
			log.debug ("getWebUser - no cookie");
		else
		{
			//	Try to Load
			wu = WebUser.get (ctx, cookieUser, null);
			log.debug ("getWebUser - got " + wu);
		}
		if (wu != null)
			return wu;
		//
		return null;
	}	//	getWebUser

	/*************************************************************************/

	/**
	 * 	Get Login Link
	 * 	@param	serverContext server context
	 * 	@return link
	 */
	private HtmlCode getLoginLink(String serverContext)
	{
		HtmlCode retValue = new HtmlCode();

		input button = new input(input.button, "Login", "Login");
		button.setOnClick("window.top.location.replace('https://" + serverContext + "/loginServlet');");
		retValue.addElement(button);

		/**	Link
		a a = new a("https://" + serverContext + "/login.jsp");
		a.setClass("menuMain");
		a.addElement("Login");
		retValue.addElement(a);
		**/

		retValue.addElement(" ");
		return retValue;
	}	//	getLoginLink

	/**
	 * 	Get Welcome Link
	 * 	@param	serverContext server Context
	 * 	@param wu web user
	 * 	@return link
	 */
	private HtmlCode getWelcomeLink(String serverContext, WebUser wu)
	{
		HtmlCode retValue = new HtmlCode();
		//
		a a = new a("https://" + serverContext + "/login.jsp");
		a.setClass("menuMain");
		String msg = "Welcome " + wu.getName();
		a.addElement(msg);
		retValue.addElement(a);
		//
		retValue.addElement(" &nbsp; ");
		if (wu.isLoggedIn())
		{
			input button = new input(input.button, "Update", "Update");
			button.setOnClick("window.top.location.replace('login.jsp');");
			retValue.addElement(button);

			retValue.addElement(" ");

			button = new input(input.button, "Logout", "Logout");
			button.setOnClick("window.top.location.replace('loginServlet?mode=logout');");
			retValue.addElement(button);

			/** Link
			a = new a ("loginServlet?mode=logout");
			a.setClass ("menuMain");
			a.addElement ("Logout");
			retValue.addElement (a);
			**/
		}
		else
		{
			input button = new input (input.button, "Login", "Login");
			button.setOnClick ("window.top.location.replace('https://" + serverContext + "/login.jsp');");
			retValue.addElement (button);
		}
		retValue.addElement (" ");
		//
		return retValue;
	}	//	getWelcomeLink

}	//	LoginLinkTag
