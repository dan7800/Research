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

import org.compiere.util.*;
import org.compiere.www.*;

/**
 *  Web User Login.
 * 	<pre>
 * 	User posts Login
 * 	- OK = forward
 *  - Did not find user
 * 	- Invalid Password
 *	</pre>
 *  @author     Jorg Janke
 *  @version    $Id: LoginServlet.java,v 1.26 2004/04/20 13:50:59 jjanke Exp $
 */
public class LoginServlet extends HttpServlet
{
	/**	Logging						*/
	private Logger				log = Logger.getCLogger(getClass());
	/** Name						*/
	static public final String	NAME = "loginServlet";

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
			throw new ServletException("LoginServlet.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Login Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
	}   //  destroy

	public static final String		P_ForwardTo = "ForwardTo";
	public static final String		LOGIN_JSP = "/login.jsp";

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
		//
	//	WEnv.dump(request);

		//	save forward parameter
		String forward = request.getParameter(P_ForwardTo);			//	get forward from request
		if (forward != null)
			session.setAttribute(P_ForwardTo, forward);
		//
		String url = LOGIN_JSP;
		//	Mode
		String mode = request.getParameter("mode");
		boolean deleteCookie = "deleteCookie".equals(mode);
		boolean logout = "logout".equals(mode);
		if (deleteCookie)
		{
			log.debug("** deleteCookie");
			JSPEnv.deleteCookieWebUser (request, response);
		}
		if (logout || deleteCookie)
		{
			log.debug("** logout");
			if (session != null)
			{
				WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
				if (wu != null)
					wu.logout();
				session.setMaxInactiveInterval(1);
				session.invalidate ();
			}
			//	Forward to unsecure /
			WUtil.createForwardPage(response, "Logout", "http://" + request.getServerName() + "/", 2);
			return;
		}

		if (!url.startsWith("/"))
			url = "/" + url;
		log.info ("doGet - Forward to " + url);
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher (url);
		dispatcher.forward (request, response);
		return;
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
		Properties ctx = JSPEnv.getCtx(request);
		HttpSession session = request.getSession(true);
		session.removeAttribute(JSPEnv.HDR_MESSAGE);
	//	WEnv.dump(session);
	//	WEnv.dump(request);

		int AD_Client_ID = 0;
		String s = request.getParameter("AD_Client_ID");
		if (s != null)
			AD_Client_ID = Integer.parseInt(s);

		//	Forward URL
		String url = request.getParameter(P_ForwardTo);			//	get forward from request
		boolean checkOut = "Y".equals(session.getAttribute(CheckOutServlet.ATTR_CHECKOUT));
		//	Set in login.jsp & addressInfo.jsp
		boolean addressConfirm = "Y".equals(request.getParameter("AddressConfirm"));
		if (checkOut)
		{
			if (addressConfirm)
				url = "/orderServlet";
			else
				url = "/addressInfo.jsp";
		}
		else
			addressConfirm = false;
		if (url == null || url.length() == 0)
		{
			url = (String)session.getAttribute(P_ForwardTo);	//	get from session
			if (url == null || url.length() == 0)
				url = "/index.jsp";
		}
		else
		{
			if (!url.startsWith("/"))
				url = "/" + url;
			session.setAttribute(P_ForwardTo, url);				//	save for log in issues
		}
		//
		String mode = request.getParameter("Mode");
		log.debug("- targeting url=" + url + " - mode=" + mode);

		//	Web User
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);

		//	Get Base Info
		String email = request.getParameter("EMail");
		if (email == null)
			email = "";
		email = email.trim();
		String password = request.getParameter("Password");
		if (password == null)
			password = "";	//	null loads w/o check
		password = password.trim();

		//	Send EMail				***	Send Password EMail Request
		if ("SendEMail".equals(mode))
		{
			log.info("** send mail");
			wu = WebUser.get (ctx, email);			//	find it
			if (!wu.isEMailValid())
				wu.setPasswordMessage("EMail not found in system");
			else
			{
				wu.setPassword();		//	set password to current
				String context = request.getServerName() + request.getContextPath() + "/";
				StringBuffer sb = new StringBuffer("http://").append(context)
					.append(" received a Send Password request from\n" + request.getRemoteHost() + " - " + request.getRemoteAddr()
					+ ".\n\nYour password is: ").append(wu.getPassword())
					.append("\n\nThank you for using ")
					.append(context);
				//
				String msg = JSPEnv.sendEMail (ctx, email, 
					context + " Password request", sb.toString());
				if (EMail.SENT_OK.equals(msg))
					wu.setPasswordMessage ("EMail sent");
				else
					wu.setPasswordMessage ("Problem sending EMail: " + msg);
			}
			url = LOGIN_JSP;
		}	//	SendEMail

		//	Login
		else if ("Login".equals(mode))
		{
			log.info("** login " + email + "/" + password);
			//	add Cookie
			JSPEnv.addCookieWebUser(request, response, email);

			//	Always re-query
			wu = WebUser.get (ctx, email, password, false);
			wu.login(password);
			//	Password valid
			if (wu.isLoggedIn())
			{
				if (url.equals(LOGIN_JSP))
					url = "/index.jsp";
			}
			else
			{
				url = LOGIN_JSP;
				log.debug("- PasswordMessage=" + wu.getPasswordMessage());
			}
			session.setAttribute (WebUser.NAME, wu);
			session.setAttribute (Info.NAME, new Info (ctx, wu));
		}	//	Login

		//	Login New
		else if ("LoginNew".equals(mode))
		{
			log.info("** loginNew");
			JSPEnv.addCookieWebUser(request, response, "");
			wu =  WebUser.get (ctx, "");
			session.setAttribute(WebUser.NAME, wu);
			url = LOGIN_JSP;
		}

		//	Submit - update/new Contact
		else if ("Submit".equals(mode))
		{
			log.info("** submit " + email + "/" + password + " - AddrConf=" + addressConfirm);
			//	we have a record for address update
			if (wu != null && wu.isLoggedIn() && addressConfirm)	//	address update
				;
			else	//	Submit - always re-load user record
				wu = WebUser.get (ctx, email, null, false); //	load w/o password check direct
			//
			if (wu.getAD_User_ID() != 0)		//	existing BPC
			{
				String passwordNew = request.getParameter("PasswordNew");
				if (passwordNew == null)
					passwordNew = "";
				boolean passwordChange = passwordNew.length() > 0 && !passwordNew.equals(password);
				if (addressConfirm || wu.login (password))
				{
					if (passwordChange)
						log.debug("- update Pwd " + email + ", Old=" + password + ", DB=" + wu.getPassword() + ", New=" + passwordNew);
					if (updateFields(request, wu, passwordChange))
					{
						if (passwordChange)
							session.setAttribute(JSPEnv.HDR_MESSAGE, "Password changed");
						session.setAttribute (WebUser.NAME, wu);
						session.setAttribute (Info.NAME, new Info (ctx, wu));
					}
					else
					{
						url = LOGIN_JSP;
						log.warn(" - update not done");
					}
				}
				else
				{
					url = LOGIN_JSP;
					session.setAttribute(JSPEnv.HDR_MESSAGE, "Email/Password not correct");
					log.warn(" - update not confirmed");
				}
			}
			else	//	new
			{
				log.debug("- new " + email + "/" + password);
				wu.setEmail (email);
				wu.setPassword (password);
				if (updateFields (request, wu, true))
				{
					if (wu.login(password))
					{
						session.setAttribute (WebUser.NAME, wu);
						session.setAttribute (Info.NAME, new Info (ctx, wu));
					}
					else
						url = LOGIN_JSP;
				}
				else
				{
					log.debug("- failed - " + wu.getSaveErrorMessage() + " - " + wu.getPasswordMessage());
					url = LOGIN_JSP;
				}
			}	//	new

		}	//	Submit
		else
			log.error("doPost - Unknown request - " + mode);

		if (!url.startsWith("/"))
			url = "/" + url;
		log.info("doPost - Forward to " + url);
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(url);
		dispatcher.forward(request, response);
	}	//	doPost


	/**
	 * 	Update Web User
	 * 	@param request request
	 * 	@param wu user
	 * 	@param updateEMailPwd if true, change email/password
	 * 	@return true if saved
	 */
	private boolean updateFields (HttpServletRequest request, WebUser wu, boolean updateEMailPwd)
	{
		if (updateEMailPwd)
		{
			String s = request.getParameter ("PasswordNew");
			wu.setPasswordMessage (null);
			wu.setPassword (s);
			if (wu.getPasswordMessage () != null)
				return false;
			//
			s = request.getParameter ("EMail");
			if (!WUtil.isEmailValid (s))
			{
				wu.setPasswordMessage ("EMail Invalid");
				return false;
			}
			wu.setEmail (s.trim());
		}
		//
		StringBuffer mandatory = new StringBuffer();
		String s = request.getParameter("Name");
		if (s != null && s.length() != 0)
			wu.setName(s.trim());
		else
			mandatory.append(" - Name");
		s = request.getParameter("Company");
		if (s != null && s.length() != 0)
			wu.setCompany(s);
		s = request.getParameter("Title");
		if (s != null && s.length() != 0)
			wu.setTitle(s);
		//
		s = request.getParameter("Address");
		if (s != null && s.length() != 0)
			wu.setAddress(s);
		else
			mandatory.append(" - Address");
		s = request.getParameter("Address2");
		if (s != null && s.length() != 0)
			wu.setAddress2(s);
		//
		s = request.getParameter("City");
		if (s != null && s.length() != 0)
			wu.setCity(s);
		else
			mandatory.append(" - City");
		s = request.getParameter("Postal");
		if (s != null && s.length() != 0)
			wu.setPostal(s);
		else
			mandatory.append(" - Postal");
		//
		s = request.getParameter("C_Country_ID");
		if (s != null && s.length() != 0)
			wu.setC_Country_ID(s);
		s = request.getParameter("C_Region_ID");
		if (s != null && s.length() != 0)
			wu.setC_Region_ID(s);
		s = request.getParameter("RegionName");
		if (s != null && s.length() != 0)
			wu.setRegionName(s);
		//
		s = request.getParameter("Phone");
		if (s != null && s.length() != 0)
			wu.setPhone(s);
		s = request.getParameter("Phone2");
		if (s != null && s.length() != 0)
			wu.setPhone2(s);
		s = request.getParameter("Fax");
		if (s != null && s.length() != 0)
			wu.setFax(s);
		//
		if (mandatory.length() > 0)
		{
			mandatory.insert(0, "Enter Mandatory");
			wu.setSaveErrorMessage(mandatory.toString());
			return false;
		}
		return wu.save();
	}	//	updateFields

	/**
	 * 	Send Account EMail.
	 * 	@param request request
	 * 	@param ctx context
	 * 	@param wu web user
	 */
	private void sendEMail (HttpServletRequest request, Properties ctx, WebUser wu)
	{
		String subject = "Compiere Web - Account " + wu.getEmail();
		String message = "Thank you for your setting up an account at http://"
			+ request.getServerName()
			+ request.getContextPath() + "/";

		JSPEnv.sendEMail(ctx, wu.getEmail(), subject, message);

		/**
		Name=GardenWorld
		webDir=compiere,
		Description=GardenWorld
		**/

	}	//	sendEMail

}	//	LoginServlet
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

import org.compiere.util.EMail;
import org.compiere.www.*;

/**
 *  Web User Login.
 * 	<pre>
 * 	User posts Login
 * 	- OK = forward
 *  - Did not find user
 * 	- Invalid Password
 *	</pre>
 *  @author     Jorg Janke
 *  @version    $Id: LoginServlet.java,v 1.20 2003/08/15 18:05:15 jjanke Exp $
 */
public class LoginServlet extends HttpServlet
{
	/**	Logging						*/
	private Logger			log = Logger.getLogger(getClass());
	/** Name						*/
	static public final String	NAME = "loginServlet";

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
			throw new ServletException("LoginServlet.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Login Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
	}   //  destroy

	public static final String		P_ForwardTo = "ForwardTo";
	public static final String		LOGIN_JSP = "login.jsp";

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
		//
	//	WEnv.dump(request);

		//	save forward parameter
		String forward = request.getParameter(P_ForwardTo);			//	get forward from request
		if (forward != null)
			session.setAttribute(P_ForwardTo, forward);
		//
		String url = LOGIN_JSP;
		//	Mode
		String mode = request.getParameter("mode");
		boolean deleteCookie = "deleteCookie".equals(mode);
		boolean logout = "logout".equals(mode);
		if (deleteCookie)
		{
			log.debug("** deleteCookie");
			JSPEnv.deleteCookieWebUser (request, response);
		}
		if (logout || deleteCookie)
		{
			log.debug("** logout");
			if (session != null)
			{
				WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
				if (wu != null)
					wu.logout();
				session.setMaxInactiveInterval(1);
				session.invalidate ();
			}
			//	Forward to unsecure /
			WUtil.createForwardPage(response, "Logout", "http://" + request.getServerName() + "/");
			return;
		}

		log.info ("doGet - Forward to " + url);
		RequestDispatcher dispatcher = getServletContext ().getRequestDispatcher (url);
		dispatcher.forward (request, response);
		return;
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
		Properties ctx = JSPEnv.getCtx(request);
		HttpSession session = request.getSession(true);
		session.removeAttribute(JSPEnv.HDR_MESSAGE);
	//	WEnv.dump(session);
	//	WEnv.dump(request);

		int AD_Client_ID = 0;
		String s = request.getParameter("AD_Client_ID");
		if (s != null)
			AD_Client_ID = Integer.parseInt(s);

		//	Forward URL
		String url = request.getParameter(P_ForwardTo);			//	get forward from request
		boolean checkOut = "Y".equals(session.getAttribute(CheckOutServlet.ATTR_CHECKOUT));
		//	Set in login.jsp & addressInfo.jsp
		boolean addressConfirm = "Y".equals(request.getParameter("AddressConfirm"));
		if (checkOut)
		{
			if (addressConfirm)
				url = "orderServlet";
			else
				url = "addressInfo.jsp";
		}
		else
			addressConfirm = false;
		if (url == null || url.length() == 0)
		{
			url = (String)session.getAttribute(P_ForwardTo);	//	get from session
			if (url == null || url.length() == 0)
				url = "index.jsp";
		}
		else
			session.setAttribute(P_ForwardTo, url);				//	save for log in issues
		//
		String mode = request.getParameter("Mode");
		log.debug("- targeting url=" + url + " - mode=" + mode);

		//	Web User
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);

		//	Get Base Info
		String email = request.getParameter("EMail");
		if (email == null)
			email = "";
		email = email.trim();
		String password = request.getParameter("Password");
		if (password == null)
			password = "";	//	null loads w/o check
		password = password.trim();

		//	Send EMail				***	Send Password EMail Request
		if ("SendEMail".equals(mode))
		{
			log.info("** send mail");
			wu = WebUser.get (ctx, email);			//	find it
			if (!wu.isEMailValid())
				wu.setPasswordMessage("EMail not found in system");
			else
			{
				wu.setPassword();		//	set password to current
				String context = request.getServerName() + request.getContextPath() + "/";
				StringBuffer sb = new StringBuffer("http://").append(context)
					.append(" received a Send Password request from\n" + request.getRemoteHost() + " - " + request.getRemoteAddr()
					+ ".\n\nYour password is: ").append(wu.getPassword())
					.append("\n\nThank you for using ")
					.append(context);
				EMail em = new EMail (ctx, true,	//	fromCurrentOrRequest
					  email, context + " Password request", sb.toString());
				String msg = em.send();
				if (EMail.SENT_OK.equals(msg))
					wu.setPasswordMessage ("EMail sent");
				else
					wu.setPasswordMessage ("Problem sending EMail: " + msg);
			}
			url = LOGIN_JSP;
		}	//	SendEMail

		//	Login
		else if ("Login".equals(mode))
		{
			log.info("** login " + email + "/" + password);
			//	add Cookie
			JSPEnv.addCookieWebUser(request, response, email);

			//	Always re-query
			wu = WebUser.get (ctx, email, password, false);
			wu.login(password);
			//	Password valid
			if (wu.isLoggedIn())
			{
				if (url.equals(LOGIN_JSP))
					url = "index.jsp";
			}
			else
			{
				url = LOGIN_JSP;
				log.debug("- PasswordMessage=" + wu.getPasswordMessage());
			}
			session.setAttribute (WebUser.NAME, wu);
			session.setAttribute (Info.NAME, new Info (ctx, wu.getC_BPartner_ID(), wu.getAD_User_ID()));
		}	//	Login

		//	Login New
		else if ("LoginNew".equals(mode))
		{
			log.info("** loginNew");
			JSPEnv.addCookieWebUser(request, response, "");
			wu =  WebUser.get (ctx, "");
			session.setAttribute(WebUser.NAME, wu);
			url = LOGIN_JSP;
		}

		//	Submit - update/new Contact
		else if ("Submit".equals(mode))
		{
			log.info("** submit " + email + "/" + password + " - LoggedIn=" + wu.isLoggedIn() + " - AddrConf=" + addressConfirm);
			//	we have a record for address update
			if (wu != null && wu.isLoggedIn() && addressConfirm)	//	address update
				;
			else	//	Submit - always re-load user record
				wu = WebUser.get (ctx, email); //	load w/o password check
			//
			if (wu.getAD_User_ID() != 0)		//	existing BPC
			{
				String passwordNew = request.getParameter("PasswordNew");
				if (passwordNew == null)
					passwordNew = "";
				boolean passwordChange = passwordNew.length() > 0 && !passwordNew.equals(password);
				if (addressConfirm || wu.login (password))
				{
					if (passwordChange)
						log.debug("- update Pwd " + email + ", Old=" + password + ", DB=" + wu.getPassword() + ", New=" + passwordNew);
					if (updateFields(request, wu, passwordChange))
					{
						if (passwordChange)
							session.setAttribute(JSPEnv.HDR_MESSAGE, "Password changed");
						session.setAttribute (WebUser.NAME, wu);
						session.setAttribute (Info.NAME, new Info (ctx, wu.getC_BPartner_ID(), wu.getAD_User_ID()));
					}
					else
					{
						url = LOGIN_JSP;
						log.warn(" - update not done");
					}
				}
				else
				{
					url = LOGIN_JSP;
					session.setAttribute(JSPEnv.HDR_MESSAGE, "Email/Password not correct");
					log.warn(" - update not confirmed");
				}
			}
			else	//	new
			{
				log.debug("- new " + email + "/" + password);
				wu.setEmail (email);
				wu.setPassword (password);
				if (updateFields (request, wu, true))
				{
					if (wu.login(password))
					{
						session.setAttribute (WebUser.NAME, wu);
						session.setAttribute (Info.NAME, new Info (ctx, wu.getC_BPartner_ID(), wu.getAD_User_ID()));
					}
					else
						url = LOGIN_JSP;
				}
				else
				{
					log.debug("- failed - " + wu.getSaveErrorMessage() + " - " + wu.getPasswordMessage());
					url = LOGIN_JSP;
				}
			}	//	new

		}	//	Submit
		else
			log.error("doPost - Unknown request - " + mode);

		log.info("doPost - Forward to " + url);
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(url);
		dispatcher.forward(request, response);
	}	//	doPost


	/**
	 * 	Update Web User
	 * 	@param request request
	 * 	@param wu user
	 * 	@param updateEMailPwd if true, change email/password
	 * 	@return true if saved
	 */
	private boolean updateFields (HttpServletRequest request, WebUser wu, boolean updateEMailPwd)
	{
		if (updateEMailPwd)
		{
			String s = request.getParameter ("PasswordNew");
			wu.setPasswordMessage (null);
			wu.setPassword (s);
			if (wu.getPasswordMessage () != null)
				return false;
			//
			s = request.getParameter ("EMail");
			if (!WUtil.isEmailValid (s))
			{
				wu.setPasswordMessage ("EMail Invalid");
				return false;
			}
			wu.setEmail (s.trim());
		}
		//
		StringBuffer mandatory = new StringBuffer();
		String s = request.getParameter("Name");
		if (s != null && s.length() != 0)
			wu.setName(s.trim());
		else
			mandatory.append(" - Name");
		s = request.getParameter("Company");
		if (s != null && s.length() != 0)
			wu.setCompany(s);
		s = request.getParameter("Title");
		if (s != null && s.length() != 0)
			wu.setTitle(s);
		//
		s = request.getParameter("Address");
		if (s != null && s.length() != 0)
			wu.setAddress(s);
		else
			mandatory.append(" - Address");
		s = request.getParameter("Address2");
		if (s != null && s.length() != 0)
			wu.setAddress2(s);
		//
		s = request.getParameter("City");
		if (s != null && s.length() != 0)
			wu.setCity(s);
		else
			mandatory.append(" - City");
		s = request.getParameter("Postal");
		if (s != null && s.length() != 0)
			wu.setPostal(s);
		else
			mandatory.append(" - Postal");
		//
		s = request.getParameter("C_Country_ID");
		if (s != null && s.length() != 0)
			wu.setC_Country_ID(s);
		s = request.getParameter("C_Region_ID");
		if (s != null && s.length() != 0)
			wu.setC_Region_ID(s);
		s = request.getParameter("RegionName");
		if (s != null && s.length() != 0)
			wu.setRegionName(s);
		//
		s = request.getParameter("Phone");
		if (s != null && s.length() != 0)
			wu.setPhone(s);
		s = request.getParameter("Phone2");
		if (s != null && s.length() != 0)
			wu.setPhone2(s);
		s = request.getParameter("Fax");
		if (s != null && s.length() != 0)
			wu.setFax(s);
		//
		if (mandatory.length() > 0)
		{
			mandatory.insert(0, "Enter Mandatory");
			wu.setSaveErrorMessage(mandatory.toString());
			return false;
		}
		return wu.save();
	}	//	updateFields

	/**
	 * 	Send Account EMail.
	 * 	@param request request
	 * 	@param ctx context
	 * 	@param wu web user
	 */
	private void sendEMail (HttpServletRequest request, Properties ctx, WebUser wu)
	{
		String subject = "Compiere Web - Account " + wu.getEmail();
		String message = "Thank you for your setting up an account at http://"
			+ request.getServerName()
			+ request.getContextPath() + "/";

		String SMTPHost = ctx.getProperty("SMTPHost", "localhost");
		String RequestEMail = ctx.getProperty("RequestEMail");
		String RequestUser = ctx.getProperty("RequestUser");
		String RequestUserPw = ctx.getProperty("RequestUserPw");
		//
		EMail em = new EMail(SMTPHost, RequestEMail, wu.getEmail(), subject, message);
		em.setEMailUser(RequestUser, RequestUserPw);
		//
	//	String webOrderEMail = ctx.getProperty("webOrderEMail");
	//	em.addBcc(webOrderEMail);
		//
		em.send();

		/**
		Name=GardenWorld
		webDir=compiere,
		Description=GardenWorld
		**/

	}	//	sendEMail

}	//	LoginServlet
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

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.compiere.util.*;

/**
 *  Web User Login.
 * 	<pre>
 * 	User posts Login
 * 	- OK = forward
 *  - Did not find user
 * 	- Invalid Password
 *	</pre>
 *  @author     Jorg Janke
 *  @version    $Id: LoginServlet.java,v 1.28 2004/08/30 06:02:38 jjanke Exp $
 */
public class LoginServlet extends HttpServlet
{
	/**	Logging						*/
	private Logger				log = Logger.getCLogger(getClass());
	/** Name						*/
	static public final String	NAME = "loginServlet";

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
			throw new ServletException("LoginServlet.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Login Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
	}   //  destroy

	public static final String		P_ForwardTo = "ForwardTo";
	public static final String		LOGIN_JSP = "/login.jsp";

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
		//
	//	WEnv.dump(request);

		//	save forward parameter
		String forward = request.getParameter(P_ForwardTo);			//	get forward from request
		if (forward != null)
			session.setAttribute(P_ForwardTo, forward);
		//
		String url = LOGIN_JSP;
		//	Mode
		String mode = request.getParameter("mode");
		boolean deleteCookie = "deleteCookie".equals(mode);
		if (deleteCookie)
		{
			log.debug("** deleteCookie");
			JSPEnv.deleteCookieWebUser (request, response);
		}
		boolean logout = "logout".equals(mode);
		if (logout || deleteCookie)
		{
			log.debug("** logout");
			if (session != null)
			{
				WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
				if (wu != null)
					wu.logout();
				session.setMaxInactiveInterval(1);
				session.invalidate ();
			}
			//	Forward to unsecure /
			WebUtil.createForwardPage(response, "Logout", "http://" + request.getServerName() + "/", 2);
			return;
		}

		if (!url.startsWith("/"))
			url = "/" + url;
		log.info ("doGet - Forward to " + url);
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher (url);
		dispatcher.forward (request, response);
		return;
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
		Properties ctx = JSPEnv.getCtx(request);
		HttpSession session = request.getSession(true);
		session.removeAttribute(JSPEnv.HDR_MESSAGE);
	//	WEnv.dump(session);
	//	WEnv.dump(request);

		int AD_Client_ID = 0;
		String s = request.getParameter("AD_Client_ID");
		if (s != null)
			AD_Client_ID = Integer.parseInt(s);

		//	Forward URL
		String url = request.getParameter(P_ForwardTo);			//	get forward from request
		boolean checkOut = "Y".equals(session.getAttribute(CheckOutServlet.ATTR_CHECKOUT));
		//	Set in login.jsp & addressInfo.jsp
		boolean addressConfirm = "Y".equals(request.getParameter("AddressConfirm"));
		if (checkOut)
		{
			if (addressConfirm)
				url = "/orderServlet";
			else
				url = "/addressInfo.jsp";
		}
		else
			addressConfirm = false;
		if (url == null || url.length() == 0)
		{
			url = (String)session.getAttribute(P_ForwardTo);	//	get from session
			if (url == null || url.length() == 0)
				url = "/index.jsp";
		}
		else
		{
			if (!url.startsWith("/"))
				url = "/" + url;
			session.setAttribute(P_ForwardTo, url);				//	save for log in issues
		}
		//
		String mode = request.getParameter("Mode");
		log.debug("- targeting url=" + url + " - mode=" + mode);

		//	Web User
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);

		//	Get Base Info
		String email = request.getParameter("EMail");
		if (email == null)
			email = "";
		email = email.trim();
		String password = request.getParameter("Password");
		if (password == null)
			password = "";	//	null loads w/o check
		password = password.trim();

		//	Send EMail				***	Send Password EMail Request
		if ("SendEMail".equals(mode))
		{
			log.info("** send mail");
			wu = WebUser.get (ctx, email);			//	find it
			if (!wu.isEMailValid())
				wu.setPasswordMessage("EMail not found in system");
			else
			{
				wu.setPassword();		//	set password to current
				String context = request.getServerName() + request.getContextPath() + "/";
				StringBuffer sb = new StringBuffer("http://").append(context)
					.append(" received a Send Password request from\n" + request.getRemoteHost() + " - " + request.getRemoteAddr()
					+ ".\n\nYour password is: ").append(wu.getPassword())
					.append("\n\nThank you for using ")
					.append(context);
				//
				String msg = JSPEnv.sendEMail (ctx, email, 
					context + " Password request", sb.toString());
				if (EMail.SENT_OK.equals(msg))
					wu.setPasswordMessage ("EMail sent");
				else
					wu.setPasswordMessage ("Problem sending EMail: " + msg);
			}
			url = LOGIN_JSP;
		}	//	SendEMail

		//	Login
		else if ("Login".equals(mode))
		{
			log.info("** login " + email + "/" + password);
			//	add Cookie
			JSPEnv.addCookieWebUser(request, response, email);

			//	Always re-query
			wu = WebUser.get (ctx, email, password, false);
			wu.login(password);
			//	Password valid
			if (wu.isLoggedIn())
			{
				if (url.equals(LOGIN_JSP))
					url = "/index.jsp";
			}
			else
			{
				url = LOGIN_JSP;
				log.debug("- PasswordMessage=" + wu.getPasswordMessage());
			}
			session.setAttribute (WebUser.NAME, wu);
			session.setAttribute (Info.NAME, new Info (ctx, wu));
		}	//	Login

		//	Login New
		else if ("LoginNew".equals(mode))
		{
			log.info("** loginNew");
			JSPEnv.addCookieWebUser(request, response, "");
			wu =  WebUser.get (ctx, "");
			session.setAttribute(WebUser.NAME, wu);
			url = LOGIN_JSP;
		}

		//	Submit - update/new Contact
		else if ("Submit".equals(mode))
		{
			log.info("** submit " + email + "/" + password + " - AddrConf=" + addressConfirm);
			//	we have a record for address update
			if (wu != null && wu.isLoggedIn() && addressConfirm)	//	address update
				;
			else	//	Submit - always re-load user record
				wu = WebUser.get (ctx, email, null, false); //	load w/o password check direct
			//
			if (wu.getAD_User_ID() != 0)		//	existing BPC
			{
				String passwordNew = request.getParameter("PasswordNew");
				if (passwordNew == null)
					passwordNew = "";
				boolean passwordChange = passwordNew.length() > 0 && !passwordNew.equals(password);
				if (addressConfirm || wu.login (password))
				{
					if (passwordChange)
						log.debug("- update Pwd " + email + ", Old=" + password + ", DB=" + wu.getPassword() + ", New=" + passwordNew);
					if (updateFields(request, wu, passwordChange))
					{
						if (passwordChange)
							session.setAttribute(JSPEnv.HDR_MESSAGE, "Password changed");
						session.setAttribute (WebUser.NAME, wu);
						session.setAttribute (Info.NAME, new Info (ctx, wu));
					}
					else
					{
						url = LOGIN_JSP;
						log.warn(" - update not done");
					}
				}
				else
				{
					url = LOGIN_JSP;
					session.setAttribute(JSPEnv.HDR_MESSAGE, "Email/Password not correct");
					log.warn(" - update not confirmed");
				}
			}
			else	//	new
			{
				log.debug("- new " + email + "/" + password);
				wu.setEmail (email);
				wu.setPassword (password);
				if (updateFields (request, wu, true))
				{
					if (wu.login(password))
					{
						session.setAttribute (WebUser.NAME, wu);
						session.setAttribute (Info.NAME, new Info (ctx, wu));
					}
					else
						url = LOGIN_JSP;
				}
				else
				{
					log.debug("- failed - " + wu.getSaveErrorMessage() + " - " + wu.getPasswordMessage());
					url = LOGIN_JSP;
				}
			}	//	new

		}	//	Submit
		else
			log.error("doPost - Unknown request - " + mode);

		if (!url.startsWith("/"))
			url = "/" + url;
		log.info("doPost - Forward to " + url);
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(url);
		dispatcher.forward(request, response);
	}	//	doPost


	/**
	 * 	Update Web User
	 * 	@param request request
	 * 	@param wu user
	 * 	@param updateEMailPwd if true, change email/password
	 * 	@return true if saved
	 */
	private boolean updateFields (HttpServletRequest request, WebUser wu, boolean updateEMailPwd)
	{
		if (updateEMailPwd)
		{
			String s = request.getParameter ("PasswordNew");
			wu.setPasswordMessage (null);
			wu.setPassword (s);
			if (wu.getPasswordMessage () != null)
				return false;
			//
			s = request.getParameter ("EMail");
			if (!WebUtil.isEmailValid (s))
			{
				wu.setPasswordMessage ("EMail Invalid");
				return false;
			}
			wu.setEmail (s.trim());
		}
		//
		StringBuffer mandatory = new StringBuffer();
		String s = request.getParameter("Name");
		if (s != null && s.length() != 0)
			wu.setName(s.trim());
		else
			mandatory.append(" - Name");
		s = request.getParameter("Company");
		if (s != null && s.length() != 0)
			wu.setCompany(s);
		s = request.getParameter("Title");
		if (s != null && s.length() != 0)
			wu.setTitle(s);
		//
		s = request.getParameter("Address");
		if (s != null && s.length() != 0)
			wu.setAddress(s);
		else
			mandatory.append(" - Address");
		s = request.getParameter("Address2");
		if (s != null && s.length() != 0)
			wu.setAddress2(s);
		//
		s = request.getParameter("City");
		if (s != null && s.length() != 0)
			wu.setCity(s);
		else
			mandatory.append(" - City");
		s = request.getParameter("Postal");
		if (s != null && s.length() != 0)
			wu.setPostal(s);
		else
			mandatory.append(" - Postal");
		//
		s = request.getParameter("C_Country_ID");
		if (s != null && s.length() != 0)
			wu.setC_Country_ID(s);
		s = request.getParameter("C_Region_ID");
		if (s != null && s.length() != 0)
			wu.setC_Region_ID(s);
		s = request.getParameter("RegionName");
		if (s != null && s.length() != 0)
			wu.setRegionName(s);
		//
		s = request.getParameter("Phone");
		if (s != null && s.length() != 0)
			wu.setPhone(s);
		s = request.getParameter("Phone2");
		if (s != null && s.length() != 0)
			wu.setPhone2(s);
		s = request.getParameter("Fax");
		if (s != null && s.length() != 0)
			wu.setFax(s);
		//
		if (mandatory.length() > 0)
		{
			mandatory.insert(0, "Enter Mandatory");
			wu.setSaveErrorMessage(mandatory.toString());
			return false;
		}
		return wu.save();
	}	//	updateFields

	/**
	 * 	Send Account EMail.
	 * 	@param request request
	 * 	@param ctx context
	 * 	@param wu web user
	 */
	private void sendEMail (HttpServletRequest request, Properties ctx, WebUser wu)
	{
		String subject = "Compiere Web - Account " + wu.getEmail();
		String message = "Thank you for your setting up an account at http://"
			+ request.getServerName()
			+ request.getContextPath() + "/";

		JSPEnv.sendEMail(ctx, wu.getEmail(), subject, message);

		/**
		Name=GardenWorld
		webDir=compiere,
		Description=GardenWorld
		**/

	}	//	sendEMail

}	//	LoginServlet
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

import org.compiere.util.EMail;
import org.compiere.www.*;

/**
 *  Web User Login.
 * 	<pre>
 * 	User posts Login
 * 	- OK = forward
 *  - Did not find user
 * 	- Invalid Password
 *	</pre>
 *  @author     Jorg Janke
 *  @version    $Id: LoginServlet.java,v 1.22 2003/08/31 06:51:26 jjanke Exp $
 */
public class LoginServlet extends HttpServlet
{
	/**	Logging						*/
	private Logger				log = Logger.getLogger(getClass());
	/** Name						*/
	static public final String	NAME = "loginServlet";

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
			throw new ServletException("LoginServlet.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Login Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
	}   //  destroy

	public static final String		P_ForwardTo = "ForwardTo";
	public static final String		LOGIN_JSP = "login.jsp";

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
		//
	//	WEnv.dump(request);

		//	save forward parameter
		String forward = request.getParameter(P_ForwardTo);			//	get forward from request
		if (forward != null)
			session.setAttribute(P_ForwardTo, forward);
		//
		String url = LOGIN_JSP;
		//	Mode
		String mode = request.getParameter("mode");
		boolean deleteCookie = "deleteCookie".equals(mode);
		boolean logout = "logout".equals(mode);
		if (deleteCookie)
		{
			log.debug("** deleteCookie");
			JSPEnv.deleteCookieWebUser (request, response);
		}
		if (logout || deleteCookie)
		{
			log.debug("** logout");
			if (session != null)
			{
				WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
				if (wu != null)
					wu.logout();
				session.setMaxInactiveInterval(1);
				session.invalidate ();
			}
			//	Forward to unsecure /
			WUtil.createForwardPage(response, "Logout", "http://" + request.getServerName() + "/");
			return;
		}

		log.info ("doGet - Forward to " + url);
		RequestDispatcher dispatcher = getServletContext ().getRequestDispatcher (url);
		dispatcher.forward (request, response);
		return;
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
		Properties ctx = JSPEnv.getCtx(request);
		HttpSession session = request.getSession(true);
		session.removeAttribute(JSPEnv.HDR_MESSAGE);
	//	WEnv.dump(session);
	//	WEnv.dump(request);

		int AD_Client_ID = 0;
		String s = request.getParameter("AD_Client_ID");
		if (s != null)
			AD_Client_ID = Integer.parseInt(s);

		//	Forward URL
		String url = request.getParameter(P_ForwardTo);			//	get forward from request
		boolean checkOut = "Y".equals(session.getAttribute(CheckOutServlet.ATTR_CHECKOUT));
		//	Set in login.jsp & addressInfo.jsp
		boolean addressConfirm = "Y".equals(request.getParameter("AddressConfirm"));
		if (checkOut)
		{
			if (addressConfirm)
				url = "orderServlet";
			else
				url = "addressInfo.jsp";
		}
		else
			addressConfirm = false;
		if (url == null || url.length() == 0)
		{
			url = (String)session.getAttribute(P_ForwardTo);	//	get from session
			if (url == null || url.length() == 0)
				url = "index.jsp";
		}
		else
			session.setAttribute(P_ForwardTo, url);				//	save for log in issues
		//
		String mode = request.getParameter("Mode");
		log.debug("- targeting url=" + url + " - mode=" + mode);

		//	Web User
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);

		//	Get Base Info
		String email = request.getParameter("EMail");
		if (email == null)
			email = "";
		email = email.trim();
		String password = request.getParameter("Password");
		if (password == null)
			password = "";	//	null loads w/o check
		password = password.trim();

		//	Send EMail				***	Send Password EMail Request
		if ("SendEMail".equals(mode))
		{
			log.info("** send mail");
			wu = WebUser.get (ctx, email);			//	find it
			if (!wu.isEMailValid())
				wu.setPasswordMessage("EMail not found in system");
			else
			{
				wu.setPassword();		//	set password to current
				String context = request.getServerName() + request.getContextPath() + "/";
				StringBuffer sb = new StringBuffer("http://").append(context)
					.append(" received a Send Password request from\n" + request.getRemoteHost() + " - " + request.getRemoteAddr()
					+ ".\n\nYour password is: ").append(wu.getPassword())
					.append("\n\nThank you for using ")
					.append(context);
				EMail em = new EMail (ctx, true,	//	fromCurrentOrRequest
					  email, context + " Password request", sb.toString());
				String msg = em.send();
				if (EMail.SENT_OK.equals(msg))
					wu.setPasswordMessage ("EMail sent");
				else
					wu.setPasswordMessage ("Problem sending EMail: " + msg);
			}
			url = LOGIN_JSP;
		}	//	SendEMail

		//	Login
		else if ("Login".equals(mode))
		{
			log.info("** login " + email + "/" + password);
			//	add Cookie
			JSPEnv.addCookieWebUser(request, response, email);

			//	Always re-query
			wu = WebUser.get (ctx, email, password, false);
			wu.login(password);
			//	Password valid
			if (wu.isLoggedIn())
			{
				if (url.equals(LOGIN_JSP))
					url = "index.jsp";
			}
			else
			{
				url = LOGIN_JSP;
				log.debug("- PasswordMessage=" + wu.getPasswordMessage());
			}
			session.setAttribute (WebUser.NAME, wu);
			session.setAttribute (Info.NAME, new Info (ctx, wu.getC_BPartner_ID(), wu.getAD_User_ID()));
		}	//	Login

		//	Login New
		else if ("LoginNew".equals(mode))
		{
			log.info("** loginNew");
			JSPEnv.addCookieWebUser(request, response, "");
			wu =  WebUser.get (ctx, "");
			session.setAttribute(WebUser.NAME, wu);
			url = LOGIN_JSP;
		}

		//	Submit - update/new Contact
		else if ("Submit".equals(mode))
		{
			log.info("** submit " + email + "/" + password + " - AddrConf=" + addressConfirm);
			//	we have a record for address update
			if (wu != null && wu.isLoggedIn() && addressConfirm)	//	address update
				;
			else	//	Submit - always re-load user record
				wu = WebUser.get (ctx, email, null, false); //	load w/o password check direct
			//
			if (wu.getAD_User_ID() != 0)		//	existing BPC
			{
				String passwordNew = request.getParameter("PasswordNew");
				if (passwordNew == null)
					passwordNew = "";
				boolean passwordChange = passwordNew.length() > 0 && !passwordNew.equals(password);
				if (addressConfirm || wu.login (password))
				{
					if (passwordChange)
						log.debug("- update Pwd " + email + ", Old=" + password + ", DB=" + wu.getPassword() + ", New=" + passwordNew);
					if (updateFields(request, wu, passwordChange))
					{
						if (passwordChange)
							session.setAttribute(JSPEnv.HDR_MESSAGE, "Password changed");
						session.setAttribute (WebUser.NAME, wu);
						session.setAttribute (Info.NAME, new Info (ctx, wu.getC_BPartner_ID(), wu.getAD_User_ID()));
					}
					else
					{
						url = LOGIN_JSP;
						log.warn(" - update not done");
					}
				}
				else
				{
					url = LOGIN_JSP;
					session.setAttribute(JSPEnv.HDR_MESSAGE, "Email/Password not correct");
					log.warn(" - update not confirmed");
				}
			}
			else	//	new
			{
				log.debug("- new " + email + "/" + password);
				wu.setEmail (email);
				wu.setPassword (password);
				if (updateFields (request, wu, true))
				{
					if (wu.login(password))
					{
						session.setAttribute (WebUser.NAME, wu);
						session.setAttribute (Info.NAME, new Info (ctx, wu.getC_BPartner_ID(), wu.getAD_User_ID()));
					}
					else
						url = LOGIN_JSP;
				}
				else
				{
					log.debug("- failed - " + wu.getSaveErrorMessage() + " - " + wu.getPasswordMessage());
					url = LOGIN_JSP;
				}
			}	//	new

		}	//	Submit
		else
			log.error("doPost - Unknown request - " + mode);

		log.info("doPost - Forward to " + url);
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(url);
		dispatcher.forward(request, response);
	}	//	doPost


	/**
	 * 	Update Web User
	 * 	@param request request
	 * 	@param wu user
	 * 	@param updateEMailPwd if true, change email/password
	 * 	@return true if saved
	 */
	private boolean updateFields (HttpServletRequest request, WebUser wu, boolean updateEMailPwd)
	{
		if (updateEMailPwd)
		{
			String s = request.getParameter ("PasswordNew");
			wu.setPasswordMessage (null);
			wu.setPassword (s);
			if (wu.getPasswordMessage () != null)
				return false;
			//
			s = request.getParameter ("EMail");
			if (!WUtil.isEmailValid (s))
			{
				wu.setPasswordMessage ("EMail Invalid");
				return false;
			}
			wu.setEmail (s.trim());
		}
		//
		StringBuffer mandatory = new StringBuffer();
		String s = request.getParameter("Name");
		if (s != null && s.length() != 0)
			wu.setName(s.trim());
		else
			mandatory.append(" - Name");
		s = request.getParameter("Company");
		if (s != null && s.length() != 0)
			wu.setCompany(s);
		s = request.getParameter("Title");
		if (s != null && s.length() != 0)
			wu.setTitle(s);
		//
		s = request.getParameter("Address");
		if (s != null && s.length() != 0)
			wu.setAddress(s);
		else
			mandatory.append(" - Address");
		s = request.getParameter("Address2");
		if (s != null && s.length() != 0)
			wu.setAddress2(s);
		//
		s = request.getParameter("City");
		if (s != null && s.length() != 0)
			wu.setCity(s);
		else
			mandatory.append(" - City");
		s = request.getParameter("Postal");
		if (s != null && s.length() != 0)
			wu.setPostal(s);
		else
			mandatory.append(" - Postal");
		//
		s = request.getParameter("C_Country_ID");
		if (s != null && s.length() != 0)
			wu.setC_Country_ID(s);
		s = request.getParameter("C_Region_ID");
		if (s != null && s.length() != 0)
			wu.setC_Region_ID(s);
		s = request.getParameter("RegionName");
		if (s != null && s.length() != 0)
			wu.setRegionName(s);
		//
		s = request.getParameter("Phone");
		if (s != null && s.length() != 0)
			wu.setPhone(s);
		s = request.getParameter("Phone2");
		if (s != null && s.length() != 0)
			wu.setPhone2(s);
		s = request.getParameter("Fax");
		if (s != null && s.length() != 0)
			wu.setFax(s);
		//
		if (mandatory.length() > 0)
		{
			mandatory.insert(0, "Enter Mandatory");
			wu.setSaveErrorMessage(mandatory.toString());
			return false;
		}
		return wu.save();
	}	//	updateFields

	/**
	 * 	Send Account EMail.
	 * 	@param request request
	 * 	@param ctx context
	 * 	@param wu web user
	 */
	private void sendEMail (HttpServletRequest request, Properties ctx, WebUser wu)
	{
		String subject = "Compiere Web - Account " + wu.getEmail();
		String message = "Thank you for your setting up an account at http://"
			+ request.getServerName()
			+ request.getContextPath() + "/";

		String SMTPHost = ctx.getProperty("SMTPHost", "localhost");
		String RequestEMail = ctx.getProperty("RequestEMail");
		String RequestUser = ctx.getProperty("RequestUser");
		String RequestUserPw = ctx.getProperty("RequestUserPw");
		//
		EMail em = new EMail(SMTPHost, RequestEMail, wu.getEmail(), subject, message);
		em.setEMailUser(RequestUser, RequestUserPw);
		//
	//	String webOrderEMail = ctx.getProperty("webOrderEMail");
	//	em.addBcc(webOrderEMail);
		//
		em.send();

		/**
		Name=GardenWorld
		webDir=compiere,
		Description=GardenWorld
		**/

	}	//	sendEMail

}	//	LoginServlet
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

import org.compiere.util.EMail;
import org.compiere.www.*;

/**
 *  Web User Login.
 * 	<pre>
 * 	User posts Login
 * 	- OK = forward
 *  - Did not find user
 * 	- Invalid Password
 *	</pre>
 *  @author     Jorg Janke
 *  @version    $Id: LoginServlet.java,v 1.19 2003/07/24 03:36:41 jjanke Exp $
 */
public class LoginServlet extends HttpServlet
{
	/**	Logging						*/
	private Logger			log = Logger.getLogger(getClass());
	/** Name						*/
	static public final String	NAME = "loginServlet";

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
			throw new ServletException("LoginServlet.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Login Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
	}   //  destroy

	public static final String		P_ForwardTo = "ForwardTo";
	public static final String		LOGIN_JSP = "login.jsp";

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
		//
	//	WEnv.dump(request);

		//	save forward parameter
		String forward = request.getParameter(P_ForwardTo);			//	get forward from request
		if (forward != null)
			session.setAttribute(P_ForwardTo, forward);
		//
		String url = LOGIN_JSP;
		//	Mode
		String mode = request.getParameter("mode");
		boolean deleteCookie = "deleteCookie".equals(mode);
		boolean logout = "logout".equals(mode);
		if (deleteCookie)
		{
			log.debug("** deleteCookie");
			JSPEnv.deleteCookieWebUser (request, response);
		}
		if (logout || deleteCookie)
		{
			log.debug("** logout");
			if (session != null)
			{
				WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
				if (wu != null)
					wu.logout();
				session.setMaxInactiveInterval(1);
				session.invalidate ();
			}
			//	Forward to unsecure /
			WUtil.createForwardPage(response, "Logout", "http://" + request.getServerName() + "/");
			return;
		}

		log.info ("doGet - Forward to " + url);
		RequestDispatcher dispatcher = getServletContext ().getRequestDispatcher (url);
		dispatcher.forward (request, response);
		return;
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
		Properties ctx = JSPEnv.getCtx(request);
		HttpSession session = request.getSession(true);
		session.removeAttribute(JSPEnv.HDR_MESSAGE);
	//	WEnv.dump(session);
	//	WEnv.dump(request);

		int AD_Client_ID = 0;
		String s = request.getParameter("AD_Client_ID");
		if (s != null)
			AD_Client_ID = Integer.parseInt(s);

		//	Forward URL
		String url = request.getParameter(P_ForwardTo);			//	get forward from request
		boolean checkOut = "Y".equals(session.getAttribute(CheckOutServlet.ATTR_CHECKOUT));
		//	Set in login.jsp & addressInfo.jsp
		boolean addressConfirmed = "Y".equals(request.getParameter("AddressConfirmed"));
		if (checkOut)
		{
			if (addressConfirmed)
				url = "orderServlet";
			else
				url = "addressInfo.jsp";
		}
		if (url == null || url.length() == 0)
		{
			url = (String)session.getAttribute(P_ForwardTo);	//	get from session
			if (url == null || url.length() == 0)
				url = "index.jsp";
		}
		else
			session.setAttribute(P_ForwardTo, url);				//	save for log in issues
		//
		String mode = request.getParameter("Mode");
		log.debug("- targeting url=" + url + " - mode=" + mode);

		//	Web User
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);

		//	Get Base Info
		String email = request.getParameter("EMail");
		if (email == null)
			email = "";
		email = email.trim();
		String password = request.getParameter("Password");
		if (password == null)
			password = "";	//	null loads w/o check
		password = password.trim();

		//	Send EMail				***	Send Password EMail Request
		if ("SendEMail".equals(mode))
		{
			log.info("** send mail");
			wu = WebUser.get (ctx, email, null);			//	find it
			if (!wu.isEMailValid())
				wu.setPasswordMessage("EMail not found in system");
			else
			{
				wu.setPassword();		//	set password to current
				String context = request.getServerName() + request.getContextPath() + "/";
				StringBuffer sb = new StringBuffer("http://").append(context)
					.append(" received a Send Password request.\n\n"
					+ "Your password is: ").append(wu.getPassword())
					.append("\n\nThank you for using ")
					.append(context);
				EMail em = new EMail (ctx, true,	//	fromCurrentOrRequest
					  email, context + " Password request", sb.toString());
				String msg = em.send();
				if (EMail.SENT_OK.equals(msg))
					wu.setPasswordMessage ("EMail sent");
				else
					wu.setPasswordMessage ("Problem sending EMail: " + msg);
			}
			url = LOGIN_JSP;
		}	//	SendEMail

		//	Login
		else if ("Login".equals(mode))
		{
			log.info("** login " + email + "/" + password + " - wu=" + wu);
			//	add Cookie
			JSPEnv.addCookieWebUser(request, response, email);

			//	we have a record
			if (wu != null && wu.getEmail().equals(email))
			{
				wu.login(password);
			}
			else	//	Find user with password
			{
				log.debug("- Search for " + email);
				wu = WebUser.get (ctx, email, password);
				wu.login(password);
			}

			//	Password valid
			if (wu.isLoggedIn())
			{
				if (url.equals(LOGIN_JSP))
					url = "index.jsp";
			}
			else
			{
				url = LOGIN_JSP;
				log.debug("- PasswordMessage=" + wu.getPasswordMessage());
			}
			session.setAttribute (WebUser.NAME, wu);
			session.setAttribute (Info.NAME, new Info (ctx, wu.getC_BPartner_ID(), wu.getAD_User_ID()));
		}	//	Login

		//	Login New
		else if ("LoginNew".equals(mode))
		{
			log.info("** loginNew");
			JSPEnv.addCookieWebUser(request, response, "");
			wu =  WebUser.get (ctx, "");
			session.setAttribute(WebUser.NAME, wu);
			url = LOGIN_JSP;
		}

		//	Submit - update/new Contact
		else if ("Submit".equals(mode))
		{
			log.info("** submit " + email + "/" + password + " - LoggedIn=" + wu.isLoggedIn() + " - AddrConf=" + addressConfirmed);
			//	we have a record
			if (wu != null
				&& (	(wu.isLoggedIn() && addressConfirmed)	//	address update
				|| wu.getEmail().equals(email)) )				//	normal screen
				;
			else	//	Find user record
				wu = WebUser.get (ctx, email, null);	//	load w/o password check
			//
			if (wu.getAD_User_ID() != 0)		//	existing BPC
			{
				String passwordNew = request.getParameter("PasswordNew");
				if (passwordNew == null)
					passwordNew = "";
				boolean passwordChange = passwordNew.length() > 0 && !passwordNew.equals(password);
				if (addressConfirmed || wu.login (password))
				{
					if (passwordChange)
						log.debug("- update Pwd " + email + ", Old=" + password + ", DB=" + wu.getPassword() + ", New=" + passwordNew);
					if (updateFields(request, wu, passwordChange))
					{
						if (passwordChange)
							session.setAttribute(JSPEnv.HDR_MESSAGE, "Password changed");
						session.setAttribute (WebUser.NAME, wu);
						session.setAttribute (Info.NAME, new Info (ctx, wu.getC_BPartner_ID(), wu.getAD_User_ID()));
					}
					else
					{
						url = LOGIN_JSP;
						log.warn(" - update not done");
					}
				}
				else
				{
					url = LOGIN_JSP;
					log.warn(" - update not confirmed");
				}
			}
			else	//	new
			{
				log.debug("- new " + email + "/" + password);
				wu.setEmail (email);
				wu.setPassword (password);
				if (updateFields (request, wu, true))
				{
					if (wu.login(password))
					{
						session.setAttribute (WebUser.NAME, wu);
						session.setAttribute (Info.NAME, new Info (ctx, wu.getC_BPartner_ID(), wu.getAD_User_ID()));
					}
					else
						url = LOGIN_JSP;
				}
				else
				{
					log.debug("- failed - " + wu.getSaveErrorMessage() + " - " + wu.getPasswordMessage());
					url = LOGIN_JSP;
				}
			}	//	new

		}	//	Submit
		else
			log.error("doPost - Unknown request - " + mode);

		log.info("doPost - Forward to " + url);
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(url);
		dispatcher.forward(request, response);
	}	//	doPost


	/**
	 * 	Update Web User
	 * 	@param request request
	 * 	@param wu user
	 * 	@param updateEMailPwd if true, change email/password
	 * 	@return true if saved
	 */
	private boolean updateFields (HttpServletRequest request, WebUser wu, boolean updateEMailPwd)
	{
		if (updateEMailPwd)
		{
			String s = request.getParameter ("PasswordNew");
			wu.setPasswordMessage (null);
			wu.setPassword (s);
			if (wu.getPasswordMessage () != null)
				return false;
			//
			s = request.getParameter ("EMail");
			if (!WUtil.isEmailValid (s))
			{
				wu.setPasswordMessage ("EMail Invalid");
				return false;
			}
			wu.setEmail (s.trim());
		}
		//
		StringBuffer mandatory = new StringBuffer();
		String s = request.getParameter("Name");
		if (s != null && s.length() != 0)
			wu.setName(s.trim());
		else
			mandatory.append(" - Name");
		s = request.getParameter("Company");
		if (s != null && s.length() != 0)
			wu.setCompany(s);
		s = request.getParameter("Title");
		if (s != null && s.length() != 0)
			wu.setTitle(s);
		//
		s = request.getParameter("Address");
		if (s != null && s.length() != 0)
			wu.setAddress(s);
		else
			mandatory.append(" - Address");
		s = request.getParameter("Address2");
		if (s != null && s.length() != 0)
			wu.setAddress2(s);
		//
		s = request.getParameter("City");
		if (s != null && s.length() != 0)
			wu.setCity(s);
		else
			mandatory.append(" - City");
		s = request.getParameter("Postal");
		if (s != null && s.length() != 0)
			wu.setPostal(s);
		else
			mandatory.append(" - Postal");
		//
		s = request.getParameter("C_Country_ID");
		if (s != null && s.length() != 0)
			wu.setC_Country_ID(s);
		s = request.getParameter("C_Region_ID");
		if (s != null && s.length() != 0)
			wu.setC_Region_ID(s);
		s = request.getParameter("RegionName");
		if (s != null && s.length() != 0)
			wu.setRegionName(s);
		//
		s = request.getParameter("Phone");
		if (s != null && s.length() != 0)
			wu.setPhone(s);
		s = request.getParameter("Phone2");
		if (s != null && s.length() != 0)
			wu.setPhone2(s);
		s = request.getParameter("Fax");
		if (s != null && s.length() != 0)
			wu.setFax(s);
		//
		if (mandatory.length() > 0)
		{
			mandatory.insert(0, "Enter Mandatory");
			wu.setSaveErrorMessage(mandatory.toString());
			return false;
		}
		return wu.save();
	}	//	updateFields

	/**
	 * 	Send Account EMail.
	 * 	@param request request
	 * 	@param ctx context
	 * 	@param wu web user
	 */
	private void sendEMail (HttpServletRequest request, Properties ctx, WebUser wu)
	{
		String subject = "Compiere Web - Account " + wu.getEmail();
		String message = "Thank you for your setting up an account at http://"
			+ request.getServerName()
			+ request.getContextPath() + "/";

		String SMTPHost = ctx.getProperty("SMTPHost", "localhost");
		String RequestEMail = ctx.getProperty("RequestEMail");
		String RequestUser = ctx.getProperty("RequestUser");
		String RequestUserPw = ctx.getProperty("RequestUserPw");
		//
		EMail em = new EMail(SMTPHost, RequestEMail, wu.getEmail(), subject, message);
		em.setEMailUser(RequestUser, RequestUserPw);
		//
	//	String webOrderEMail = ctx.getProperty("webOrderEMail");
	//	em.addBcc(webOrderEMail);
		//
		em.send();

		/**
		Name=GardenWorld
		webDir=compiere,
		Description=GardenWorld
		**/

	}	//	sendEMail

}	//	LoginServlet
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

import org.compiere.util.EMail;
import org.compiere.www.*;

/**
 *  Web User Login.
 * 	<pre>
 * 	User posts Login
 * 	- OK = forward
 *  - Did not find user
 * 	- Invalid Password
 *	</pre>
 *  @author     Jorg Janke
 *  @version    $Id: LoginServlet.java,v 1.20 2003/08/15 18:05:15 jjanke Exp $
 */
public class LoginServlet extends HttpServlet
{
	/**	Logging						*/
	private Logger			log = Logger.getLogger(getClass());
	/** Name						*/
	static public final String	NAME = "loginServlet";

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
			throw new ServletException("LoginServlet.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Login Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
	}   //  destroy

	public static final String		P_ForwardTo = "ForwardTo";
	public static final String		LOGIN_JSP = "login.jsp";

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
		//
	//	WEnv.dump(request);

		//	save forward parameter
		String forward = request.getParameter(P_ForwardTo);			//	get forward from request
		if (forward != null)
			session.setAttribute(P_ForwardTo, forward);
		//
		String url = LOGIN_JSP;
		//	Mode
		String mode = request.getParameter("mode");
		boolean deleteCookie = "deleteCookie".equals(mode);
		boolean logout = "logout".equals(mode);
		if (deleteCookie)
		{
			log.debug("** deleteCookie");
			JSPEnv.deleteCookieWebUser (request, response);
		}
		if (logout || deleteCookie)
		{
			log.debug("** logout");
			if (session != null)
			{
				WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
				if (wu != null)
					wu.logout();
				session.setMaxInactiveInterval(1);
				session.invalidate ();
			}
			//	Forward to unsecure /
			WUtil.createForwardPage(response, "Logout", "http://" + request.getServerName() + "/");
			return;
		}

		log.info ("doGet - Forward to " + url);
		RequestDispatcher dispatcher = getServletContext ().getRequestDispatcher (url);
		dispatcher.forward (request, response);
		return;
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
		Properties ctx = JSPEnv.getCtx(request);
		HttpSession session = request.getSession(true);
		session.removeAttribute(JSPEnv.HDR_MESSAGE);
	//	WEnv.dump(session);
	//	WEnv.dump(request);

		int AD_Client_ID = 0;
		String s = request.getParameter("AD_Client_ID");
		if (s != null)
			AD_Client_ID = Integer.parseInt(s);

		//	Forward URL
		String url = request.getParameter(P_ForwardTo);			//	get forward from request
		boolean checkOut = "Y".equals(session.getAttribute(CheckOutServlet.ATTR_CHECKOUT));
		//	Set in login.jsp & addressInfo.jsp
		boolean addressConfirm = "Y".equals(request.getParameter("AddressConfirm"));
		if (checkOut)
		{
			if (addressConfirm)
				url = "orderServlet";
			else
				url = "addressInfo.jsp";
		}
		else
			addressConfirm = false;
		if (url == null || url.length() == 0)
		{
			url = (String)session.getAttribute(P_ForwardTo);	//	get from session
			if (url == null || url.length() == 0)
				url = "index.jsp";
		}
		else
			session.setAttribute(P_ForwardTo, url);				//	save for log in issues
		//
		String mode = request.getParameter("Mode");
		log.debug("- targeting url=" + url + " - mode=" + mode);

		//	Web User
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);

		//	Get Base Info
		String email = request.getParameter("EMail");
		if (email == null)
			email = "";
		email = email.trim();
		String password = request.getParameter("Password");
		if (password == null)
			password = "";	//	null loads w/o check
		password = password.trim();

		//	Send EMail				***	Send Password EMail Request
		if ("SendEMail".equals(mode))
		{
			log.info("** send mail");
			wu = WebUser.get (ctx, email);			//	find it
			if (!wu.isEMailValid())
				wu.setPasswordMessage("EMail not found in system");
			else
			{
				wu.setPassword();		//	set password to current
				String context = request.getServerName() + request.getContextPath() + "/";
				StringBuffer sb = new StringBuffer("http://").append(context)
					.append(" received a Send Password request from\n" + request.getRemoteHost() + " - " + request.getRemoteAddr()
					+ ".\n\nYour password is: ").append(wu.getPassword())
					.append("\n\nThank you for using ")
					.append(context);
				EMail em = new EMail (ctx, true,	//	fromCurrentOrRequest
					  email, context + " Password request", sb.toString());
				String msg = em.send();
				if (EMail.SENT_OK.equals(msg))
					wu.setPasswordMessage ("EMail sent");
				else
					wu.setPasswordMessage ("Problem sending EMail: " + msg);
			}
			url = LOGIN_JSP;
		}	//	SendEMail

		//	Login
		else if ("Login".equals(mode))
		{
			log.info("** login " + email + "/" + password);
			//	add Cookie
			JSPEnv.addCookieWebUser(request, response, email);

			//	Always re-query
			wu = WebUser.get (ctx, email, password, false);
			wu.login(password);
			//	Password valid
			if (wu.isLoggedIn())
			{
				if (url.equals(LOGIN_JSP))
					url = "index.jsp";
			}
			else
			{
				url = LOGIN_JSP;
				log.debug("- PasswordMessage=" + wu.getPasswordMessage());
			}
			session.setAttribute (WebUser.NAME, wu);
			session.setAttribute (Info.NAME, new Info (ctx, wu.getC_BPartner_ID(), wu.getAD_User_ID()));
		}	//	Login

		//	Login New
		else if ("LoginNew".equals(mode))
		{
			log.info("** loginNew");
			JSPEnv.addCookieWebUser(request, response, "");
			wu =  WebUser.get (ctx, "");
			session.setAttribute(WebUser.NAME, wu);
			url = LOGIN_JSP;
		}

		//	Submit - update/new Contact
		else if ("Submit".equals(mode))
		{
			log.info("** submit " + email + "/" + password + " - LoggedIn=" + wu.isLoggedIn() + " - AddrConf=" + addressConfirm);
			//	we have a record for address update
			if (wu != null && wu.isLoggedIn() && addressConfirm)	//	address update
				;
			else	//	Submit - always re-load user record
				wu = WebUser.get (ctx, email); //	load w/o password check
			//
			if (wu.getAD_User_ID() != 0)		//	existing BPC
			{
				String passwordNew = request.getParameter("PasswordNew");
				if (passwordNew == null)
					passwordNew = "";
				boolean passwordChange = passwordNew.length() > 0 && !passwordNew.equals(password);
				if (addressConfirm || wu.login (password))
				{
					if (passwordChange)
						log.debug("- update Pwd " + email + ", Old=" + password + ", DB=" + wu.getPassword() + ", New=" + passwordNew);
					if (updateFields(request, wu, passwordChange))
					{
						if (passwordChange)
							session.setAttribute(JSPEnv.HDR_MESSAGE, "Password changed");
						session.setAttribute (WebUser.NAME, wu);
						session.setAttribute (Info.NAME, new Info (ctx, wu.getC_BPartner_ID(), wu.getAD_User_ID()));
					}
					else
					{
						url = LOGIN_JSP;
						log.warn(" - update not done");
					}
				}
				else
				{
					url = LOGIN_JSP;
					session.setAttribute(JSPEnv.HDR_MESSAGE, "Email/Password not correct");
					log.warn(" - update not confirmed");
				}
			}
			else	//	new
			{
				log.debug("- new " + email + "/" + password);
				wu.setEmail (email);
				wu.setPassword (password);
				if (updateFields (request, wu, true))
				{
					if (wu.login(password))
					{
						session.setAttribute (WebUser.NAME, wu);
						session.setAttribute (Info.NAME, new Info (ctx, wu.getC_BPartner_ID(), wu.getAD_User_ID()));
					}
					else
						url = LOGIN_JSP;
				}
				else
				{
					log.debug("- failed - " + wu.getSaveErrorMessage() + " - " + wu.getPasswordMessage());
					url = LOGIN_JSP;
				}
			}	//	new

		}	//	Submit
		else
			log.error("doPost - Unknown request - " + mode);

		log.info("doPost - Forward to " + url);
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(url);
		dispatcher.forward(request, response);
	}	//	doPost


	/**
	 * 	Update Web User
	 * 	@param request request
	 * 	@param wu user
	 * 	@param updateEMailPwd if true, change email/password
	 * 	@return true if saved
	 */
	private boolean updateFields (HttpServletRequest request, WebUser wu, boolean updateEMailPwd)
	{
		if (updateEMailPwd)
		{
			String s = request.getParameter ("PasswordNew");
			wu.setPasswordMessage (null);
			wu.setPassword (s);
			if (wu.getPasswordMessage () != null)
				return false;
			//
			s = request.getParameter ("EMail");
			if (!WUtil.isEmailValid (s))
			{
				wu.setPasswordMessage ("EMail Invalid");
				return false;
			}
			wu.setEmail (s.trim());
		}
		//
		StringBuffer mandatory = new StringBuffer();
		String s = request.getParameter("Name");
		if (s != null && s.length() != 0)
			wu.setName(s.trim());
		else
			mandatory.append(" - Name");
		s = request.getParameter("Company");
		if (s != null && s.length() != 0)
			wu.setCompany(s);
		s = request.getParameter("Title");
		if (s != null && s.length() != 0)
			wu.setTitle(s);
		//
		s = request.getParameter("Address");
		if (s != null && s.length() != 0)
			wu.setAddress(s);
		else
			mandatory.append(" - Address");
		s = request.getParameter("Address2");
		if (s != null && s.length() != 0)
			wu.setAddress2(s);
		//
		s = request.getParameter("City");
		if (s != null && s.length() != 0)
			wu.setCity(s);
		else
			mandatory.append(" - City");
		s = request.getParameter("Postal");
		if (s != null && s.length() != 0)
			wu.setPostal(s);
		else
			mandatory.append(" - Postal");
		//
		s = request.getParameter("C_Country_ID");
		if (s != null && s.length() != 0)
			wu.setC_Country_ID(s);
		s = request.getParameter("C_Region_ID");
		if (s != null && s.length() != 0)
			wu.setC_Region_ID(s);
		s = request.getParameter("RegionName");
		if (s != null && s.length() != 0)
			wu.setRegionName(s);
		//
		s = request.getParameter("Phone");
		if (s != null && s.length() != 0)
			wu.setPhone(s);
		s = request.getParameter("Phone2");
		if (s != null && s.length() != 0)
			wu.setPhone2(s);
		s = request.getParameter("Fax");
		if (s != null && s.length() != 0)
			wu.setFax(s);
		//
		if (mandatory.length() > 0)
		{
			mandatory.insert(0, "Enter Mandatory");
			wu.setSaveErrorMessage(mandatory.toString());
			return false;
		}
		return wu.save();
	}	//	updateFields

	/**
	 * 	Send Account EMail.
	 * 	@param request request
	 * 	@param ctx context
	 * 	@param wu web user
	 */
	private void sendEMail (HttpServletRequest request, Properties ctx, WebUser wu)
	{
		String subject = "Compiere Web - Account " + wu.getEmail();
		String message = "Thank you for your setting up an account at http://"
			+ request.getServerName()
			+ request.getContextPath() + "/";

		String SMTPHost = ctx.getProperty("SMTPHost", "localhost");
		String RequestEMail = ctx.getProperty("RequestEMail");
		String RequestUser = ctx.getProperty("RequestUser");
		String RequestUserPw = ctx.getProperty("RequestUserPw");
		//
		EMail em = new EMail(SMTPHost, RequestEMail, wu.getEmail(), subject, message);
		em.setEMailUser(RequestUser, RequestUserPw);
		//
	//	String webOrderEMail = ctx.getProperty("webOrderEMail");
	//	em.addBcc(webOrderEMail);
		//
		em.send();

		/**
		Name=GardenWorld
		webDir=compiere,
		Description=GardenWorld
		**/

	}	//	sendEMail

}	//	LoginServlet
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

import org.compiere.util.*;
import org.compiere.www.*;

/**
 *  Web User Login.
 * 	<pre>
 * 	User posts Login
 * 	- OK = forward
 *  - Did not find user
 * 	- Invalid Password
 *	</pre>
 *  @author     Jorg Janke
 *  @version    $Id: LoginServlet.java,v 1.26 2004/04/20 13:50:59 jjanke Exp $
 */
public class LoginServlet extends HttpServlet
{
	/**	Logging						*/
	private Logger				log = Logger.getCLogger(getClass());
	/** Name						*/
	static public final String	NAME = "loginServlet";

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
			throw new ServletException("LoginServlet.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Login Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
	}   //  destroy

	public static final String		P_ForwardTo = "ForwardTo";
	public static final String		LOGIN_JSP = "/login.jsp";

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
		//
	//	WEnv.dump(request);

		//	save forward parameter
		String forward = request.getParameter(P_ForwardTo);			//	get forward from request
		if (forward != null)
			session.setAttribute(P_ForwardTo, forward);
		//
		String url = LOGIN_JSP;
		//	Mode
		String mode = request.getParameter("mode");
		boolean deleteCookie = "deleteCookie".equals(mode);
		boolean logout = "logout".equals(mode);
		if (deleteCookie)
		{
			log.debug("** deleteCookie");
			JSPEnv.deleteCookieWebUser (request, response);
		}
		if (logout || deleteCookie)
		{
			log.debug("** logout");
			if (session != null)
			{
				WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
				if (wu != null)
					wu.logout();
				session.setMaxInactiveInterval(1);
				session.invalidate ();
			}
			//	Forward to unsecure /
			WUtil.createForwardPage(response, "Logout", "http://" + request.getServerName() + "/", 2);
			return;
		}

		if (!url.startsWith("/"))
			url = "/" + url;
		log.info ("doGet - Forward to " + url);
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher (url);
		dispatcher.forward (request, response);
		return;
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
		Properties ctx = JSPEnv.getCtx(request);
		HttpSession session = request.getSession(true);
		session.removeAttribute(JSPEnv.HDR_MESSAGE);
	//	WEnv.dump(session);
	//	WEnv.dump(request);

		int AD_Client_ID = 0;
		String s = request.getParameter("AD_Client_ID");
		if (s != null)
			AD_Client_ID = Integer.parseInt(s);

		//	Forward URL
		String url = request.getParameter(P_ForwardTo);			//	get forward from request
		boolean checkOut = "Y".equals(session.getAttribute(CheckOutServlet.ATTR_CHECKOUT));
		//	Set in login.jsp & addressInfo.jsp
		boolean addressConfirm = "Y".equals(request.getParameter("AddressConfirm"));
		if (checkOut)
		{
			if (addressConfirm)
				url = "/orderServlet";
			else
				url = "/addressInfo.jsp";
		}
		else
			addressConfirm = false;
		if (url == null || url.length() == 0)
		{
			url = (String)session.getAttribute(P_ForwardTo);	//	get from session
			if (url == null || url.length() == 0)
				url = "/index.jsp";
		}
		else
		{
			if (!url.startsWith("/"))
				url = "/" + url;
			session.setAttribute(P_ForwardTo, url);				//	save for log in issues
		}
		//
		String mode = request.getParameter("Mode");
		log.debug("- targeting url=" + url + " - mode=" + mode);

		//	Web User
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);

		//	Get Base Info
		String email = request.getParameter("EMail");
		if (email == null)
			email = "";
		email = email.trim();
		String password = request.getParameter("Password");
		if (password == null)
			password = "";	//	null loads w/o check
		password = password.trim();

		//	Send EMail				***	Send Password EMail Request
		if ("SendEMail".equals(mode))
		{
			log.info("** send mail");
			wu = WebUser.get (ctx, email);			//	find it
			if (!wu.isEMailValid())
				wu.setPasswordMessage("EMail not found in system");
			else
			{
				wu.setPassword();		//	set password to current
				String context = request.getServerName() + request.getContextPath() + "/";
				StringBuffer sb = new StringBuffer("http://").append(context)
					.append(" received a Send Password request from\n" + request.getRemoteHost() + " - " + request.getRemoteAddr()
					+ ".\n\nYour password is: ").append(wu.getPassword())
					.append("\n\nThank you for using ")
					.append(context);
				//
				String msg = JSPEnv.sendEMail (ctx, email, 
					context + " Password request", sb.toString());
				if (EMail.SENT_OK.equals(msg))
					wu.setPasswordMessage ("EMail sent");
				else
					wu.setPasswordMessage ("Problem sending EMail: " + msg);
			}
			url = LOGIN_JSP;
		}	//	SendEMail

		//	Login
		else if ("Login".equals(mode))
		{
			log.info("** login " + email + "/" + password);
			//	add Cookie
			JSPEnv.addCookieWebUser(request, response, email);

			//	Always re-query
			wu = WebUser.get (ctx, email, password, false);
			wu.login(password);
			//	Password valid
			if (wu.isLoggedIn())
			{
				if (url.equals(LOGIN_JSP))
					url = "/index.jsp";
			}
			else
			{
				url = LOGIN_JSP;
				log.debug("- PasswordMessage=" + wu.getPasswordMessage());
			}
			session.setAttribute (WebUser.NAME, wu);
			session.setAttribute (Info.NAME, new Info (ctx, wu));
		}	//	Login

		//	Login New
		else if ("LoginNew".equals(mode))
		{
			log.info("** loginNew");
			JSPEnv.addCookieWebUser(request, response, "");
			wu =  WebUser.get (ctx, "");
			session.setAttribute(WebUser.NAME, wu);
			url = LOGIN_JSP;
		}

		//	Submit - update/new Contact
		else if ("Submit".equals(mode))
		{
			log.info("** submit " + email + "/" + password + " - AddrConf=" + addressConfirm);
			//	we have a record for address update
			if (wu != null && wu.isLoggedIn() && addressConfirm)	//	address update
				;
			else	//	Submit - always re-load user record
				wu = WebUser.get (ctx, email, null, false); //	load w/o password check direct
			//
			if (wu.getAD_User_ID() != 0)		//	existing BPC
			{
				String passwordNew = request.getParameter("PasswordNew");
				if (passwordNew == null)
					passwordNew = "";
				boolean passwordChange = passwordNew.length() > 0 && !passwordNew.equals(password);
				if (addressConfirm || wu.login (password))
				{
					if (passwordChange)
						log.debug("- update Pwd " + email + ", Old=" + password + ", DB=" + wu.getPassword() + ", New=" + passwordNew);
					if (updateFields(request, wu, passwordChange))
					{
						if (passwordChange)
							session.setAttribute(JSPEnv.HDR_MESSAGE, "Password changed");
						session.setAttribute (WebUser.NAME, wu);
						session.setAttribute (Info.NAME, new Info (ctx, wu));
					}
					else
					{
						url = LOGIN_JSP;
						log.warn(" - update not done");
					}
				}
				else
				{
					url = LOGIN_JSP;
					session.setAttribute(JSPEnv.HDR_MESSAGE, "Email/Password not correct");
					log.warn(" - update not confirmed");
				}
			}
			else	//	new
			{
				log.debug("- new " + email + "/" + password);
				wu.setEmail (email);
				wu.setPassword (password);
				if (updateFields (request, wu, true))
				{
					if (wu.login(password))
					{
						session.setAttribute (WebUser.NAME, wu);
						session.setAttribute (Info.NAME, new Info (ctx, wu));
					}
					else
						url = LOGIN_JSP;
				}
				else
				{
					log.debug("- failed - " + wu.getSaveErrorMessage() + " - " + wu.getPasswordMessage());
					url = LOGIN_JSP;
				}
			}	//	new

		}	//	Submit
		else
			log.error("doPost - Unknown request - " + mode);

		if (!url.startsWith("/"))
			url = "/" + url;
		log.info("doPost - Forward to " + url);
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(url);
		dispatcher.forward(request, response);
	}	//	doPost


	/**
	 * 	Update Web User
	 * 	@param request request
	 * 	@param wu user
	 * 	@param updateEMailPwd if true, change email/password
	 * 	@return true if saved
	 */
	private boolean updateFields (HttpServletRequest request, WebUser wu, boolean updateEMailPwd)
	{
		if (updateEMailPwd)
		{
			String s = request.getParameter ("PasswordNew");
			wu.setPasswordMessage (null);
			wu.setPassword (s);
			if (wu.getPasswordMessage () != null)
				return false;
			//
			s = request.getParameter ("EMail");
			if (!WUtil.isEmailValid (s))
			{
				wu.setPasswordMessage ("EMail Invalid");
				return false;
			}
			wu.setEmail (s.trim());
		}
		//
		StringBuffer mandatory = new StringBuffer();
		String s = request.getParameter("Name");
		if (s != null && s.length() != 0)
			wu.setName(s.trim());
		else
			mandatory.append(" - Name");
		s = request.getParameter("Company");
		if (s != null && s.length() != 0)
			wu.setCompany(s);
		s = request.getParameter("Title");
		if (s != null && s.length() != 0)
			wu.setTitle(s);
		//
		s = request.getParameter("Address");
		if (s != null && s.length() != 0)
			wu.setAddress(s);
		else
			mandatory.append(" - Address");
		s = request.getParameter("Address2");
		if (s != null && s.length() != 0)
			wu.setAddress2(s);
		//
		s = request.getParameter("City");
		if (s != null && s.length() != 0)
			wu.setCity(s);
		else
			mandatory.append(" - City");
		s = request.getParameter("Postal");
		if (s != null && s.length() != 0)
			wu.setPostal(s);
		else
			mandatory.append(" - Postal");
		//
		s = request.getParameter("C_Country_ID");
		if (s != null && s.length() != 0)
			wu.setC_Country_ID(s);
		s = request.getParameter("C_Region_ID");
		if (s != null && s.length() != 0)
			wu.setC_Region_ID(s);
		s = request.getParameter("RegionName");
		if (s != null && s.length() != 0)
			wu.setRegionName(s);
		//
		s = request.getParameter("Phone");
		if (s != null && s.length() != 0)
			wu.setPhone(s);
		s = request.getParameter("Phone2");
		if (s != null && s.length() != 0)
			wu.setPhone2(s);
		s = request.getParameter("Fax");
		if (s != null && s.length() != 0)
			wu.setFax(s);
		//
		if (mandatory.length() > 0)
		{
			mandatory.insert(0, "Enter Mandatory");
			wu.setSaveErrorMessage(mandatory.toString());
			return false;
		}
		return wu.save();
	}	//	updateFields

	/**
	 * 	Send Account EMail.
	 * 	@param request request
	 * 	@param ctx context
	 * 	@param wu web user
	 */
	private void sendEMail (HttpServletRequest request, Properties ctx, WebUser wu)
	{
		String subject = "Compiere Web - Account " + wu.getEmail();
		String message = "Thank you for your setting up an account at http://"
			+ request.getServerName()
			+ request.getContextPath() + "/";

		JSPEnv.sendEMail(ctx, wu.getEmail(), subject, message);

		/**
		Name=GardenWorld
		webDir=compiere,
		Description=GardenWorld
		**/

	}	//	sendEMail

}	//	LoginServlet
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

import org.compiere.util.EMail;
import org.compiere.www.*;

/**
 *  Web User Login.
 * 	<pre>
 * 	User posts Login
 * 	- OK = forward
 *  - Did not find user
 * 	- Invalid Password
 *	</pre>
 *  @author     Jorg Janke
 *  @version    $Id: LoginServlet.java,v 1.22 2003/08/31 06:51:26 jjanke Exp $
 */
public class LoginServlet extends HttpServlet
{
	/**	Logging						*/
	private Logger				log = Logger.getLogger(getClass());
	/** Name						*/
	static public final String	NAME = "loginServlet";

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
			throw new ServletException("LoginServlet.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Login Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
	}   //  destroy

	public static final String		P_ForwardTo = "ForwardTo";
	public static final String		LOGIN_JSP = "login.jsp";

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
		//
	//	WEnv.dump(request);

		//	save forward parameter
		String forward = request.getParameter(P_ForwardTo);			//	get forward from request
		if (forward != null)
			session.setAttribute(P_ForwardTo, forward);
		//
		String url = LOGIN_JSP;
		//	Mode
		String mode = request.getParameter("mode");
		boolean deleteCookie = "deleteCookie".equals(mode);
		boolean logout = "logout".equals(mode);
		if (deleteCookie)
		{
			log.debug("** deleteCookie");
			JSPEnv.deleteCookieWebUser (request, response);
		}
		if (logout || deleteCookie)
		{
			log.debug("** logout");
			if (session != null)
			{
				WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
				if (wu != null)
					wu.logout();
				session.setMaxInactiveInterval(1);
				session.invalidate ();
			}
			//	Forward to unsecure /
			WUtil.createForwardPage(response, "Logout", "http://" + request.getServerName() + "/");
			return;
		}

		log.info ("doGet - Forward to " + url);
		RequestDispatcher dispatcher = getServletContext ().getRequestDispatcher (url);
		dispatcher.forward (request, response);
		return;
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
		Properties ctx = JSPEnv.getCtx(request);
		HttpSession session = request.getSession(true);
		session.removeAttribute(JSPEnv.HDR_MESSAGE);
	//	WEnv.dump(session);
	//	WEnv.dump(request);

		int AD_Client_ID = 0;
		String s = request.getParameter("AD_Client_ID");
		if (s != null)
			AD_Client_ID = Integer.parseInt(s);

		//	Forward URL
		String url = request.getParameter(P_ForwardTo);			//	get forward from request
		boolean checkOut = "Y".equals(session.getAttribute(CheckOutServlet.ATTR_CHECKOUT));
		//	Set in login.jsp & addressInfo.jsp
		boolean addressConfirm = "Y".equals(request.getParameter("AddressConfirm"));
		if (checkOut)
		{
			if (addressConfirm)
				url = "orderServlet";
			else
				url = "addressInfo.jsp";
		}
		else
			addressConfirm = false;
		if (url == null || url.length() == 0)
		{
			url = (String)session.getAttribute(P_ForwardTo);	//	get from session
			if (url == null || url.length() == 0)
				url = "index.jsp";
		}
		else
			session.setAttribute(P_ForwardTo, url);				//	save for log in issues
		//
		String mode = request.getParameter("Mode");
		log.debug("- targeting url=" + url + " - mode=" + mode);

		//	Web User
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);

		//	Get Base Info
		String email = request.getParameter("EMail");
		if (email == null)
			email = "";
		email = email.trim();
		String password = request.getParameter("Password");
		if (password == null)
			password = "";	//	null loads w/o check
		password = password.trim();

		//	Send EMail				***	Send Password EMail Request
		if ("SendEMail".equals(mode))
		{
			log.info("** send mail");
			wu = WebUser.get (ctx, email);			//	find it
			if (!wu.isEMailValid())
				wu.setPasswordMessage("EMail not found in system");
			else
			{
				wu.setPassword();		//	set password to current
				String context = request.getServerName() + request.getContextPath() + "/";
				StringBuffer sb = new StringBuffer("http://").append(context)
					.append(" received a Send Password request from\n" + request.getRemoteHost() + " - " + request.getRemoteAddr()
					+ ".\n\nYour password is: ").append(wu.getPassword())
					.append("\n\nThank you for using ")
					.append(context);
				EMail em = new EMail (ctx, true,	//	fromCurrentOrRequest
					  email, context + " Password request", sb.toString());
				String msg = em.send();
				if (EMail.SENT_OK.equals(msg))
					wu.setPasswordMessage ("EMail sent");
				else
					wu.setPasswordMessage ("Problem sending EMail: " + msg);
			}
			url = LOGIN_JSP;
		}	//	SendEMail

		//	Login
		else if ("Login".equals(mode))
		{
			log.info("** login " + email + "/" + password);
			//	add Cookie
			JSPEnv.addCookieWebUser(request, response, email);

			//	Always re-query
			wu = WebUser.get (ctx, email, password, false);
			wu.login(password);
			//	Password valid
			if (wu.isLoggedIn())
			{
				if (url.equals(LOGIN_JSP))
					url = "index.jsp";
			}
			else
			{
				url = LOGIN_JSP;
				log.debug("- PasswordMessage=" + wu.getPasswordMessage());
			}
			session.setAttribute (WebUser.NAME, wu);
			session.setAttribute (Info.NAME, new Info (ctx, wu.getC_BPartner_ID(), wu.getAD_User_ID()));
		}	//	Login

		//	Login New
		else if ("LoginNew".equals(mode))
		{
			log.info("** loginNew");
			JSPEnv.addCookieWebUser(request, response, "");
			wu =  WebUser.get (ctx, "");
			session.setAttribute(WebUser.NAME, wu);
			url = LOGIN_JSP;
		}

		//	Submit - update/new Contact
		else if ("Submit".equals(mode))
		{
			log.info("** submit " + email + "/" + password + " - AddrConf=" + addressConfirm);
			//	we have a record for address update
			if (wu != null && wu.isLoggedIn() && addressConfirm)	//	address update
				;
			else	//	Submit - always re-load user record
				wu = WebUser.get (ctx, email, null, false); //	load w/o password check direct
			//
			if (wu.getAD_User_ID() != 0)		//	existing BPC
			{
				String passwordNew = request.getParameter("PasswordNew");
				if (passwordNew == null)
					passwordNew = "";
				boolean passwordChange = passwordNew.length() > 0 && !passwordNew.equals(password);
				if (addressConfirm || wu.login (password))
				{
					if (passwordChange)
						log.debug("- update Pwd " + email + ", Old=" + password + ", DB=" + wu.getPassword() + ", New=" + passwordNew);
					if (updateFields(request, wu, passwordChange))
					{
						if (passwordChange)
							session.setAttribute(JSPEnv.HDR_MESSAGE, "Password changed");
						session.setAttribute (WebUser.NAME, wu);
						session.setAttribute (Info.NAME, new Info (ctx, wu.getC_BPartner_ID(), wu.getAD_User_ID()));
					}
					else
					{
						url = LOGIN_JSP;
						log.warn(" - update not done");
					}
				}
				else
				{
					url = LOGIN_JSP;
					session.setAttribute(JSPEnv.HDR_MESSAGE, "Email/Password not correct");
					log.warn(" - update not confirmed");
				}
			}
			else	//	new
			{
				log.debug("- new " + email + "/" + password);
				wu.setEmail (email);
				wu.setPassword (password);
				if (updateFields (request, wu, true))
				{
					if (wu.login(password))
					{
						session.setAttribute (WebUser.NAME, wu);
						session.setAttribute (Info.NAME, new Info (ctx, wu.getC_BPartner_ID(), wu.getAD_User_ID()));
					}
					else
						url = LOGIN_JSP;
				}
				else
				{
					log.debug("- failed - " + wu.getSaveErrorMessage() + " - " + wu.getPasswordMessage());
					url = LOGIN_JSP;
				}
			}	//	new

		}	//	Submit
		else
			log.error("doPost - Unknown request - " + mode);

		log.info("doPost - Forward to " + url);
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(url);
		dispatcher.forward(request, response);
	}	//	doPost


	/**
	 * 	Update Web User
	 * 	@param request request
	 * 	@param wu user
	 * 	@param updateEMailPwd if true, change email/password
	 * 	@return true if saved
	 */
	private boolean updateFields (HttpServletRequest request, WebUser wu, boolean updateEMailPwd)
	{
		if (updateEMailPwd)
		{
			String s = request.getParameter ("PasswordNew");
			wu.setPasswordMessage (null);
			wu.setPassword (s);
			if (wu.getPasswordMessage () != null)
				return false;
			//
			s = request.getParameter ("EMail");
			if (!WUtil.isEmailValid (s))
			{
				wu.setPasswordMessage ("EMail Invalid");
				return false;
			}
			wu.setEmail (s.trim());
		}
		//
		StringBuffer mandatory = new StringBuffer();
		String s = request.getParameter("Name");
		if (s != null && s.length() != 0)
			wu.setName(s.trim());
		else
			mandatory.append(" - Name");
		s = request.getParameter("Company");
		if (s != null && s.length() != 0)
			wu.setCompany(s);
		s = request.getParameter("Title");
		if (s != null && s.length() != 0)
			wu.setTitle(s);
		//
		s = request.getParameter("Address");
		if (s != null && s.length() != 0)
			wu.setAddress(s);
		else
			mandatory.append(" - Address");
		s = request.getParameter("Address2");
		if (s != null && s.length() != 0)
			wu.setAddress2(s);
		//
		s = request.getParameter("City");
		if (s != null && s.length() != 0)
			wu.setCity(s);
		else
			mandatory.append(" - City");
		s = request.getParameter("Postal");
		if (s != null && s.length() != 0)
			wu.setPostal(s);
		else
			mandatory.append(" - Postal");
		//
		s = request.getParameter("C_Country_ID");
		if (s != null && s.length() != 0)
			wu.setC_Country_ID(s);
		s = request.getParameter("C_Region_ID");
		if (s != null && s.length() != 0)
			wu.setC_Region_ID(s);
		s = request.getParameter("RegionName");
		if (s != null && s.length() != 0)
			wu.setRegionName(s);
		//
		s = request.getParameter("Phone");
		if (s != null && s.length() != 0)
			wu.setPhone(s);
		s = request.getParameter("Phone2");
		if (s != null && s.length() != 0)
			wu.setPhone2(s);
		s = request.getParameter("Fax");
		if (s != null && s.length() != 0)
			wu.setFax(s);
		//
		if (mandatory.length() > 0)
		{
			mandatory.insert(0, "Enter Mandatory");
			wu.setSaveErrorMessage(mandatory.toString());
			return false;
		}
		return wu.save();
	}	//	updateFields

	/**
	 * 	Send Account EMail.
	 * 	@param request request
	 * 	@param ctx context
	 * 	@param wu web user
	 */
	private void sendEMail (HttpServletRequest request, Properties ctx, WebUser wu)
	{
		String subject = "Compiere Web - Account " + wu.getEmail();
		String message = "Thank you for your setting up an account at http://"
			+ request.getServerName()
			+ request.getContextPath() + "/";

		String SMTPHost = ctx.getProperty("SMTPHost", "localhost");
		String RequestEMail = ctx.getProperty("RequestEMail");
		String RequestUser = ctx.getProperty("RequestUser");
		String RequestUserPw = ctx.getProperty("RequestUserPw");
		//
		EMail em = new EMail(SMTPHost, RequestEMail, wu.getEmail(), subject, message);
		em.setEMailUser(RequestUser, RequestUserPw);
		//
	//	String webOrderEMail = ctx.getProperty("webOrderEMail");
	//	em.addBcc(webOrderEMail);
		//
		em.send();

		/**
		Name=GardenWorld
		webDir=compiere,
		Description=GardenWorld
		**/

	}	//	sendEMail

}	//	LoginServlet
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

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.compiere.util.*;

/**
 *  Web User Login.
 * 	<pre>
 * 	User posts Login
 * 	- OK = forward
 *  - Did not find user
 * 	- Invalid Password
 *	</pre>
 *  @author     Jorg Janke
 *  @version    $Id: LoginServlet.java,v 1.28 2004/08/30 06:02:38 jjanke Exp $
 */
public class LoginServlet extends HttpServlet
{
	/**	Logging						*/
	private Logger				log = Logger.getCLogger(getClass());
	/** Name						*/
	static public final String	NAME = "loginServlet";

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
			throw new ServletException("LoginServlet.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Login Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
	}   //  destroy

	public static final String		P_ForwardTo = "ForwardTo";
	public static final String		LOGIN_JSP = "/login.jsp";

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
		//
	//	WEnv.dump(request);

		//	save forward parameter
		String forward = request.getParameter(P_ForwardTo);			//	get forward from request
		if (forward != null)
			session.setAttribute(P_ForwardTo, forward);
		//
		String url = LOGIN_JSP;
		//	Mode
		String mode = request.getParameter("mode");
		boolean deleteCookie = "deleteCookie".equals(mode);
		if (deleteCookie)
		{
			log.debug("** deleteCookie");
			JSPEnv.deleteCookieWebUser (request, response);
		}
		boolean logout = "logout".equals(mode);
		if (logout || deleteCookie)
		{
			log.debug("** logout");
			if (session != null)
			{
				WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
				if (wu != null)
					wu.logout();
				session.setMaxInactiveInterval(1);
				session.invalidate ();
			}
			//	Forward to unsecure /
			WebUtil.createForwardPage(response, "Logout", "http://" + request.getServerName() + "/", 2);
			return;
		}

		if (!url.startsWith("/"))
			url = "/" + url;
		log.info ("doGet - Forward to " + url);
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher (url);
		dispatcher.forward (request, response);
		return;
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
		Properties ctx = JSPEnv.getCtx(request);
		HttpSession session = request.getSession(true);
		session.removeAttribute(JSPEnv.HDR_MESSAGE);
	//	WEnv.dump(session);
	//	WEnv.dump(request);

		int AD_Client_ID = 0;
		String s = request.getParameter("AD_Client_ID");
		if (s != null)
			AD_Client_ID = Integer.parseInt(s);

		//	Forward URL
		String url = request.getParameter(P_ForwardTo);			//	get forward from request
		boolean checkOut = "Y".equals(session.getAttribute(CheckOutServlet.ATTR_CHECKOUT));
		//	Set in login.jsp & addressInfo.jsp
		boolean addressConfirm = "Y".equals(request.getParameter("AddressConfirm"));
		if (checkOut)
		{
			if (addressConfirm)
				url = "/orderServlet";
			else
				url = "/addressInfo.jsp";
		}
		else
			addressConfirm = false;
		if (url == null || url.length() == 0)
		{
			url = (String)session.getAttribute(P_ForwardTo);	//	get from session
			if (url == null || url.length() == 0)
				url = "/index.jsp";
		}
		else
		{
			if (!url.startsWith("/"))
				url = "/" + url;
			session.setAttribute(P_ForwardTo, url);				//	save for log in issues
		}
		//
		String mode = request.getParameter("Mode");
		log.debug("- targeting url=" + url + " - mode=" + mode);

		//	Web User
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);

		//	Get Base Info
		String email = request.getParameter("EMail");
		if (email == null)
			email = "";
		email = email.trim();
		String password = request.getParameter("Password");
		if (password == null)
			password = "";	//	null loads w/o check
		password = password.trim();

		//	Send EMail				***	Send Password EMail Request
		if ("SendEMail".equals(mode))
		{
			log.info("** send mail");
			wu = WebUser.get (ctx, email);			//	find it
			if (!wu.isEMailValid())
				wu.setPasswordMessage("EMail not found in system");
			else
			{
				wu.setPassword();		//	set password to current
				String context = request.getServerName() + request.getContextPath() + "/";
				StringBuffer sb = new StringBuffer("http://").append(context)
					.append(" received a Send Password request from\n" + request.getRemoteHost() + " - " + request.getRemoteAddr()
					+ ".\n\nYour password is: ").append(wu.getPassword())
					.append("\n\nThank you for using ")
					.append(context);
				//
				String msg = JSPEnv.sendEMail (ctx, email, 
					context + " Password request", sb.toString());
				if (EMail.SENT_OK.equals(msg))
					wu.setPasswordMessage ("EMail sent");
				else
					wu.setPasswordMessage ("Problem sending EMail: " + msg);
			}
			url = LOGIN_JSP;
		}	//	SendEMail

		//	Login
		else if ("Login".equals(mode))
		{
			log.info("** login " + email + "/" + password);
			//	add Cookie
			JSPEnv.addCookieWebUser(request, response, email);

			//	Always re-query
			wu = WebUser.get (ctx, email, password, false);
			wu.login(password);
			//	Password valid
			if (wu.isLoggedIn())
			{
				if (url.equals(LOGIN_JSP))
					url = "/index.jsp";
			}
			else
			{
				url = LOGIN_JSP;
				log.debug("- PasswordMessage=" + wu.getPasswordMessage());
			}
			session.setAttribute (WebUser.NAME, wu);
			session.setAttribute (Info.NAME, new Info (ctx, wu));
		}	//	Login

		//	Login New
		else if ("LoginNew".equals(mode))
		{
			log.info("** loginNew");
			JSPEnv.addCookieWebUser(request, response, "");
			wu =  WebUser.get (ctx, "");
			session.setAttribute(WebUser.NAME, wu);
			url = LOGIN_JSP;
		}

		//	Submit - update/new Contact
		else if ("Submit".equals(mode))
		{
			log.info("** submit " + email + "/" + password + " - AddrConf=" + addressConfirm);
			//	we have a record for address update
			if (wu != null && wu.isLoggedIn() && addressConfirm)	//	address update
				;
			else	//	Submit - always re-load user record
				wu = WebUser.get (ctx, email, null, false); //	load w/o password check direct
			//
			if (wu.getAD_User_ID() != 0)		//	existing BPC
			{
				String passwordNew = request.getParameter("PasswordNew");
				if (passwordNew == null)
					passwordNew = "";
				boolean passwordChange = passwordNew.length() > 0 && !passwordNew.equals(password);
				if (addressConfirm || wu.login (password))
				{
					if (passwordChange)
						log.debug("- update Pwd " + email + ", Old=" + password + ", DB=" + wu.getPassword() + ", New=" + passwordNew);
					if (updateFields(request, wu, passwordChange))
					{
						if (passwordChange)
							session.setAttribute(JSPEnv.HDR_MESSAGE, "Password changed");
						session.setAttribute (WebUser.NAME, wu);
						session.setAttribute (Info.NAME, new Info (ctx, wu));
					}
					else
					{
						url = LOGIN_JSP;
						log.warn(" - update not done");
					}
				}
				else
				{
					url = LOGIN_JSP;
					session.setAttribute(JSPEnv.HDR_MESSAGE, "Email/Password not correct");
					log.warn(" - update not confirmed");
				}
			}
			else	//	new
			{
				log.debug("- new " + email + "/" + password);
				wu.setEmail (email);
				wu.setPassword (password);
				if (updateFields (request, wu, true))
				{
					if (wu.login(password))
					{
						session.setAttribute (WebUser.NAME, wu);
						session.setAttribute (Info.NAME, new Info (ctx, wu));
					}
					else
						url = LOGIN_JSP;
				}
				else
				{
					log.debug("- failed - " + wu.getSaveErrorMessage() + " - " + wu.getPasswordMessage());
					url = LOGIN_JSP;
				}
			}	//	new

		}	//	Submit
		else
			log.error("doPost - Unknown request - " + mode);

		if (!url.startsWith("/"))
			url = "/" + url;
		log.info("doPost - Forward to " + url);
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(url);
		dispatcher.forward(request, response);
	}	//	doPost


	/**
	 * 	Update Web User
	 * 	@param request request
	 * 	@param wu user
	 * 	@param updateEMailPwd if true, change email/password
	 * 	@return true if saved
	 */
	private boolean updateFields (HttpServletRequest request, WebUser wu, boolean updateEMailPwd)
	{
		if (updateEMailPwd)
		{
			String s = request.getParameter ("PasswordNew");
			wu.setPasswordMessage (null);
			wu.setPassword (s);
			if (wu.getPasswordMessage () != null)
				return false;
			//
			s = request.getParameter ("EMail");
			if (!WebUtil.isEmailValid (s))
			{
				wu.setPasswordMessage ("EMail Invalid");
				return false;
			}
			wu.setEmail (s.trim());
		}
		//
		StringBuffer mandatory = new StringBuffer();
		String s = request.getParameter("Name");
		if (s != null && s.length() != 0)
			wu.setName(s.trim());
		else
			mandatory.append(" - Name");
		s = request.getParameter("Company");
		if (s != null && s.length() != 0)
			wu.setCompany(s);
		s = request.getParameter("Title");
		if (s != null && s.length() != 0)
			wu.setTitle(s);
		//
		s = request.getParameter("Address");
		if (s != null && s.length() != 0)
			wu.setAddress(s);
		else
			mandatory.append(" - Address");
		s = request.getParameter("Address2");
		if (s != null && s.length() != 0)
			wu.setAddress2(s);
		//
		s = request.getParameter("City");
		if (s != null && s.length() != 0)
			wu.setCity(s);
		else
			mandatory.append(" - City");
		s = request.getParameter("Postal");
		if (s != null && s.length() != 0)
			wu.setPostal(s);
		else
			mandatory.append(" - Postal");
		//
		s = request.getParameter("C_Country_ID");
		if (s != null && s.length() != 0)
			wu.setC_Country_ID(s);
		s = request.getParameter("C_Region_ID");
		if (s != null && s.length() != 0)
			wu.setC_Region_ID(s);
		s = request.getParameter("RegionName");
		if (s != null && s.length() != 0)
			wu.setRegionName(s);
		//
		s = request.getParameter("Phone");
		if (s != null && s.length() != 0)
			wu.setPhone(s);
		s = request.getParameter("Phone2");
		if (s != null && s.length() != 0)
			wu.setPhone2(s);
		s = request.getParameter("Fax");
		if (s != null && s.length() != 0)
			wu.setFax(s);
		//
		if (mandatory.length() > 0)
		{
			mandatory.insert(0, "Enter Mandatory");
			wu.setSaveErrorMessage(mandatory.toString());
			return false;
		}
		return wu.save();
	}	//	updateFields

	/**
	 * 	Send Account EMail.
	 * 	@param request request
	 * 	@param ctx context
	 * 	@param wu web user
	 */
	private void sendEMail (HttpServletRequest request, Properties ctx, WebUser wu)
	{
		String subject = "Compiere Web - Account " + wu.getEmail();
		String message = "Thank you for your setting up an account at http://"
			+ request.getServerName()
			+ request.getContextPath() + "/";

		JSPEnv.sendEMail(ctx, wu.getEmail(), subject, message);

		/**
		Name=GardenWorld
		webDir=compiere,
		Description=GardenWorld
		**/

	}	//	sendEMail

}	//	LoginServlet
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

import org.compiere.util.EMail;
import org.compiere.www.*;

/**
 *  Web User Login.
 * 	<pre>
 * 	User posts Login
 * 	- OK = forward
 *  - Did not find user
 * 	- Invalid Password
 *	</pre>
 *  @author     Jorg Janke
 *  @version    $Id: LoginServlet.java,v 1.19 2003/07/24 03:36:41 jjanke Exp $
 */
public class LoginServlet extends HttpServlet
{
	/**	Logging						*/
	private Logger			log = Logger.getLogger(getClass());
	/** Name						*/
	static public final String	NAME = "loginServlet";

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
			throw new ServletException("LoginServlet.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Login Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
	}   //  destroy

	public static final String		P_ForwardTo = "ForwardTo";
	public static final String		LOGIN_JSP = "login.jsp";

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
		//
	//	WEnv.dump(request);

		//	save forward parameter
		String forward = request.getParameter(P_ForwardTo);			//	get forward from request
		if (forward != null)
			session.setAttribute(P_ForwardTo, forward);
		//
		String url = LOGIN_JSP;
		//	Mode
		String mode = request.getParameter("mode");
		boolean deleteCookie = "deleteCookie".equals(mode);
		boolean logout = "logout".equals(mode);
		if (deleteCookie)
		{
			log.debug("** deleteCookie");
			JSPEnv.deleteCookieWebUser (request, response);
		}
		if (logout || deleteCookie)
		{
			log.debug("** logout");
			if (session != null)
			{
				WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
				if (wu != null)
					wu.logout();
				session.setMaxInactiveInterval(1);
				session.invalidate ();
			}
			//	Forward to unsecure /
			WUtil.createForwardPage(response, "Logout", "http://" + request.getServerName() + "/");
			return;
		}

		log.info ("doGet - Forward to " + url);
		RequestDispatcher dispatcher = getServletContext ().getRequestDispatcher (url);
		dispatcher.forward (request, response);
		return;
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
		Properties ctx = JSPEnv.getCtx(request);
		HttpSession session = request.getSession(true);
		session.removeAttribute(JSPEnv.HDR_MESSAGE);
	//	WEnv.dump(session);
	//	WEnv.dump(request);

		int AD_Client_ID = 0;
		String s = request.getParameter("AD_Client_ID");
		if (s != null)
			AD_Client_ID = Integer.parseInt(s);

		//	Forward URL
		String url = request.getParameter(P_ForwardTo);			//	get forward from request
		boolean checkOut = "Y".equals(session.getAttribute(CheckOutServlet.ATTR_CHECKOUT));
		//	Set in login.jsp & addressInfo.jsp
		boolean addressConfirmed = "Y".equals(request.getParameter("AddressConfirmed"));
		if (checkOut)
		{
			if (addressConfirmed)
				url = "orderServlet";
			else
				url = "addressInfo.jsp";
		}
		if (url == null || url.length() == 0)
		{
			url = (String)session.getAttribute(P_ForwardTo);	//	get from session
			if (url == null || url.length() == 0)
				url = "index.jsp";
		}
		else
			session.setAttribute(P_ForwardTo, url);				//	save for log in issues
		//
		String mode = request.getParameter("Mode");
		log.debug("- targeting url=" + url + " - mode=" + mode);

		//	Web User
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);

		//	Get Base Info
		String email = request.getParameter("EMail");
		if (email == null)
			email = "";
		email = email.trim();
		String password = request.getParameter("Password");
		if (password == null)
			password = "";	//	null loads w/o check
		password = password.trim();

		//	Send EMail				***	Send Password EMail Request
		if ("SendEMail".equals(mode))
		{
			log.info("** send mail");
			wu = WebUser.get (ctx, email, null);			//	find it
			if (!wu.isEMailValid())
				wu.setPasswordMessage("EMail not found in system");
			else
			{
				wu.setPassword();		//	set password to current
				String context = request.getServerName() + request.getContextPath() + "/";
				StringBuffer sb = new StringBuffer("http://").append(context)
					.append(" received a Send Password request.\n\n"
					+ "Your password is: ").append(wu.getPassword())
					.append("\n\nThank you for using ")
					.append(context);
				EMail em = new EMail (ctx, true,	//	fromCurrentOrRequest
					  email, context + " Password request", sb.toString());
				String msg = em.send();
				if (EMail.SENT_OK.equals(msg))
					wu.setPasswordMessage ("EMail sent");
				else
					wu.setPasswordMessage ("Problem sending EMail: " + msg);
			}
			url = LOGIN_JSP;
		}	//	SendEMail

		//	Login
		else if ("Login".equals(mode))
		{
			log.info("** login " + email + "/" + password + " - wu=" + wu);
			//	add Cookie
			JSPEnv.addCookieWebUser(request, response, email);

			//	we have a record
			if (wu != null && wu.getEmail().equals(email))
			{
				wu.login(password);
			}
			else	//	Find user with password
			{
				log.debug("- Search for " + email);
				wu = WebUser.get (ctx, email, password);
				wu.login(password);
			}

			//	Password valid
			if (wu.isLoggedIn())
			{
				if (url.equals(LOGIN_JSP))
					url = "index.jsp";
			}
			else
			{
				url = LOGIN_JSP;
				log.debug("- PasswordMessage=" + wu.getPasswordMessage());
			}
			session.setAttribute (WebUser.NAME, wu);
			session.setAttribute (Info.NAME, new Info (ctx, wu.getC_BPartner_ID(), wu.getAD_User_ID()));
		}	//	Login

		//	Login New
		else if ("LoginNew".equals(mode))
		{
			log.info("** loginNew");
			JSPEnv.addCookieWebUser(request, response, "");
			wu =  WebUser.get (ctx, "");
			session.setAttribute(WebUser.NAME, wu);
			url = LOGIN_JSP;
		}

		//	Submit - update/new Contact
		else if ("Submit".equals(mode))
		{
			log.info("** submit " + email + "/" + password + " - LoggedIn=" + wu.isLoggedIn() + " - AddrConf=" + addressConfirmed);
			//	we have a record
			if (wu != null
				&& (	(wu.isLoggedIn() && addressConfirmed)	//	address update
				|| wu.getEmail().equals(email)) )				//	normal screen
				;
			else	//	Find user record
				wu = WebUser.get (ctx, email, null);	//	load w/o password check
			//
			if (wu.getAD_User_ID() != 0)		//	existing BPC
			{
				String passwordNew = request.getParameter("PasswordNew");
				if (passwordNew == null)
					passwordNew = "";
				boolean passwordChange = passwordNew.length() > 0 && !passwordNew.equals(password);
				if (addressConfirmed || wu.login (password))
				{
					if (passwordChange)
						log.debug("- update Pwd " + email + ", Old=" + password + ", DB=" + wu.getPassword() + ", New=" + passwordNew);
					if (updateFields(request, wu, passwordChange))
					{
						if (passwordChange)
							session.setAttribute(JSPEnv.HDR_MESSAGE, "Password changed");
						session.setAttribute (WebUser.NAME, wu);
						session.setAttribute (Info.NAME, new Info (ctx, wu.getC_BPartner_ID(), wu.getAD_User_ID()));
					}
					else
					{
						url = LOGIN_JSP;
						log.warn(" - update not done");
					}
				}
				else
				{
					url = LOGIN_JSP;
					log.warn(" - update not confirmed");
				}
			}
			else	//	new
			{
				log.debug("- new " + email + "/" + password);
				wu.setEmail (email);
				wu.setPassword (password);
				if (updateFields (request, wu, true))
				{
					if (wu.login(password))
					{
						session.setAttribute (WebUser.NAME, wu);
						session.setAttribute (Info.NAME, new Info (ctx, wu.getC_BPartner_ID(), wu.getAD_User_ID()));
					}
					else
						url = LOGIN_JSP;
				}
				else
				{
					log.debug("- failed - " + wu.getSaveErrorMessage() + " - " + wu.getPasswordMessage());
					url = LOGIN_JSP;
				}
			}	//	new

		}	//	Submit
		else
			log.error("doPost - Unknown request - " + mode);

		log.info("doPost - Forward to " + url);
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(url);
		dispatcher.forward(request, response);
	}	//	doPost


	/**
	 * 	Update Web User
	 * 	@param request request
	 * 	@param wu user
	 * 	@param updateEMailPwd if true, change email/password
	 * 	@return true if saved
	 */
	private boolean updateFields (HttpServletRequest request, WebUser wu, boolean updateEMailPwd)
	{
		if (updateEMailPwd)
		{
			String s = request.getParameter ("PasswordNew");
			wu.setPasswordMessage (null);
			wu.setPassword (s);
			if (wu.getPasswordMessage () != null)
				return false;
			//
			s = request.getParameter ("EMail");
			if (!WUtil.isEmailValid (s))
			{
				wu.setPasswordMessage ("EMail Invalid");
				return false;
			}
			wu.setEmail (s.trim());
		}
		//
		StringBuffer mandatory = new StringBuffer();
		String s = request.getParameter("Name");
		if (s != null && s.length() != 0)
			wu.setName(s.trim());
		else
			mandatory.append(" - Name");
		s = request.getParameter("Company");
		if (s != null && s.length() != 0)
			wu.setCompany(s);
		s = request.getParameter("Title");
		if (s != null && s.length() != 0)
			wu.setTitle(s);
		//
		s = request.getParameter("Address");
		if (s != null && s.length() != 0)
			wu.setAddress(s);
		else
			mandatory.append(" - Address");
		s = request.getParameter("Address2");
		if (s != null && s.length() != 0)
			wu.setAddress2(s);
		//
		s = request.getParameter("City");
		if (s != null && s.length() != 0)
			wu.setCity(s);
		else
			mandatory.append(" - City");
		s = request.getParameter("Postal");
		if (s != null && s.length() != 0)
			wu.setPostal(s);
		else
			mandatory.append(" - Postal");
		//
		s = request.getParameter("C_Country_ID");
		if (s != null && s.length() != 0)
			wu.setC_Country_ID(s);
		s = request.getParameter("C_Region_ID");
		if (s != null && s.length() != 0)
			wu.setC_Region_ID(s);
		s = request.getParameter("RegionName");
		if (s != null && s.length() != 0)
			wu.setRegionName(s);
		//
		s = request.getParameter("Phone");
		if (s != null && s.length() != 0)
			wu.setPhone(s);
		s = request.getParameter("Phone2");
		if (s != null && s.length() != 0)
			wu.setPhone2(s);
		s = request.getParameter("Fax");
		if (s != null && s.length() != 0)
			wu.setFax(s);
		//
		if (mandatory.length() > 0)
		{
			mandatory.insert(0, "Enter Mandatory");
			wu.setSaveErrorMessage(mandatory.toString());
			return false;
		}
		return wu.save();
	}	//	updateFields

	/**
	 * 	Send Account EMail.
	 * 	@param request request
	 * 	@param ctx context
	 * 	@param wu web user
	 */
	private void sendEMail (HttpServletRequest request, Properties ctx, WebUser wu)
	{
		String subject = "Compiere Web - Account " + wu.getEmail();
		String message = "Thank you for your setting up an account at http://"
			+ request.getServerName()
			+ request.getContextPath() + "/";

		String SMTPHost = ctx.getProperty("SMTPHost", "localhost");
		String RequestEMail = ctx.getProperty("RequestEMail");
		String RequestUser = ctx.getProperty("RequestUser");
		String RequestUserPw = ctx.getProperty("RequestUserPw");
		//
		EMail em = new EMail(SMTPHost, RequestEMail, wu.getEmail(), subject, message);
		em.setEMailUser(RequestUser, RequestUserPw);
		//
	//	String webOrderEMail = ctx.getProperty("webOrderEMail");
	//	em.addBcc(webOrderEMail);
		//
		em.send();

		/**
		Name=GardenWorld
		webDir=compiere,
		Description=GardenWorld
		**/

	}	//	sendEMail

}	//	LoginServlet
