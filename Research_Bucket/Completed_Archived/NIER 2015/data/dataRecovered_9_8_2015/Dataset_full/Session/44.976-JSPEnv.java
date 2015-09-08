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

import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.log4j.Logger;

import org.compiere.util.*;
import org.compiere.model.*;

/**
 *  JSP Environment Utilities
 *
 *  @author Jorg Janke
 *  @version $Id: JSPEnv.java,v 1.23 2004/05/02 02:12:18 jjanke Exp $
 */
public class JSPEnv
{
	/**	Logger							*/
	static private Logger			s_log = Logger.getLogger (JSPEnv.class);

	public final static String		CONTEXT_NAME = "ctx";
	public final static String		CTX_SERVER_CONTEXT = "context";
	public final static String		CTX_DOCUMENT_DIR = "documentDir";
	/** Header (Error) Message			*/
	public final static String		HDR_MESSAGE = "hdrMessage";
	/** Header Info Message				*/
	public final static String		HDR_INFO = "hdrInfo";
	/**	Context							*/
	private static CCache			s_cacheCtx = new CCache("JSPEnvCtx", 2, 60);	//	60 minute refresh


	/**
	 * 	Get Context from Session
	 *	@param request request
	 * 	@return properties
	 */
	public static Properties getCtx (HttpServletRequest request)
	{
		//	Session
		HttpSession session = request.getSession(true);
		Properties ctx = (Properties)session.getAttribute(CONTEXT_NAME);

		//	New Context
		if (ctx == null)
		{
			s_log.info ("getCtx - new (" + request.getRemoteAddr() + ")");
			ctx = new Properties();
			//	Add Servlet Init Parameters
			ServletContext sc = session.getServletContext();
			Enumeration en = sc.getInitParameterNames();
			while (en.hasMoreElements())
			{
				String key = (String)en.nextElement();
				String value = sc.getInitParameter(key);
				ctx.setProperty(key, value);
			}
			//	Default Client
			int AD_Client_ID = Env.getContextAsInt(ctx, "#AD_Client_ID");
			if (AD_Client_ID == 0)
			{
				AD_Client_ID = DB.getSQLValue("SELECT AD_Client_ID FROM AD_Client WHERE AD_Client_ID > 11 AND IsActive='Y'");
				if (AD_Client_ID < 0)
					AD_Client_ID = 11;	//	GardenWorld
				Env.setContext (ctx, "#AD_Client_ID", AD_Client_ID);
			}
			//	Add Defaults
			ctx = getDefaults (ctx, AD_Client_ID);
			//	ServerContext	- dev2/wstore
			ctx.put(CTX_SERVER_CONTEXT, request.getServerName() + request.getContextPath());

			//	save it
			session.setAttribute(CONTEXT_NAME, ctx);
			s_log.debug ("getCtx - new #" + ctx.size());
		//	s_log.debug ("getCtx - " + ctx);
		}

		//	Add/set current user
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
		if (wu != null)
		{
			int AD_User_ID = wu.getAD_User_ID();
			Env.setContext(ctx, "#AD_User_ID", AD_User_ID);		//	security
		}

		//	Finish
		session.setMaxInactiveInterval(1800);	//	30 Min	HARDCODED
		String info = (String)ctx.get(HDR_INFO);
		if (info != null)
			session.setAttribute(HDR_INFO, info);
		return ctx;
	}	//	getCtx

	/**
	 * 	Get Defaults
	 * 	@param ctx context
	 * 	@param AD_Client_ID client
	 * 	@return context
	 */
	private static Properties getDefaults (Properties ctx, int AD_Client_ID)
	{
		Integer key = new Integer (AD_Client_ID);
		Properties newCtx = (Properties)s_cacheCtx.get(key);
		
		/**	Create New Context		*/
		if (newCtx == null)
		{
			s_log.info("getDefaults - AD_Client_ID=" + AD_Client_ID);
			newCtx = new Properties();
			//	copy explicitly
			Enumeration e = ctx.keys();
			while (e.hasMoreElements())
			{
				String pKey = (String)e.nextElement();
				newCtx.setProperty(pKey, ctx.getProperty(pKey));
			}
			
			//	Default Trx! Org
			if (Env.getContextAsInt(newCtx, "#AD_Org_ID") == 0)
			{
				int AD_Org_ID = DB.getSQLValue("SELECT AD_Org_ID FROM AD_Org WHERE AD_Client_ID=? AND IsActive='Y' AND IsSummary='N' ORDER BY 1", AD_Client_ID);
				Env.setContext(newCtx, "#AD_Org_ID", AD_Org_ID);
			}
			//	Default User
			if (Env.getContextAsInt(newCtx, "#AD_User_ID") == 0)
			{
				int AD_User_ID = 0;		//	HARDCODED - System
				Env.setContext(newCtx, "#AD_User_ID", AD_User_ID);
			}
			//	Default Role for access
			if (Env.getContextAsInt(newCtx, "#AD_Role_ID") == 0)
			{
				int AD_Role_ID = 0;		//	HARDCODED - System
				Env.setContext(newCtx, "#AD_Role_ID", AD_Role_ID);
			}

			//	Warehouse
			if (Env.getContextAsInt(newCtx, "#M_Warehouse_ID") == 0)
			{
				int M_Warehouse_ID = DB.getSQLValue("SELECT M_Warehouse_ID FROM M_Warehouse WHERE AD_Client_ID=? AND IsActive='Y' ORDER BY 1", AD_Client_ID);
				Env.setContext(newCtx, "#M_Warehouse_ID", M_Warehouse_ID);
			}
			//	Sales Rep
			if (Env.getContextAsInt(newCtx, "#SalesRep_ID") == 0)
			{
				int SalesRep_ID = 0;	//	HARDCODED - Syatem
				Env.setContext(newCtx, "#SalesRep_ID", SalesRep_ID);
			}
			//	Payment Term
			if (Env.getContextAsInt(newCtx, "#C_PaymentTerm_ID") == 0)
			{
				int C_PaymentTerm_ID = DB.getSQLValue("SELECT C_PaymentTerm_ID FROM C_PaymentTerm WHERE AD_Client_ID=? AND IsDefault='Y' ORDER BY NetDays", AD_Client_ID);
				Env.setContext(newCtx, "#C_PaymentTerm_ID", C_PaymentTerm_ID);
			}

			/****************************************/

			//	Read from disk
			MClient client = MClient.get (newCtx, AD_Client_ID);
			//	Name,Description, SMTPHost,RequestEMail,RequestUser, RequestUserPw
			Env.setContext(newCtx, "name", client.getName());
			Env.setContext(newCtx, "description", client.getDescription());
			Env.setContext(newCtx, "SMTPHost", client.getSMTPHost());
			Env.setContext(newCtx, EMail.CTX_REQUEST_EMAIL, client.getRequestEMail());
			Env.setContext(newCtx, EMail.CTX_REQUEST_EMAIL_USER, client.getRequestUser());
			Env.setContext(newCtx, EMail.CTX_REQUEST_EMAIL_USERPW, client.getRequestUserPW());
			
			//	AD_Language, WebDir, WebParam1,WebParam2,WebParam3,WebParam4, WebOrderEMail
			if (newCtx.getProperty("#AD_Language") == null && client.getAD_Language() != null)
				Env.setContext(newCtx, "#AD_Language", client.getAD_Language());
			Env.setContext(newCtx, "webDir", client.getWebDir());
			String s = client.getWebParam1();
			Env.setContext(newCtx, "webParam1", s == null ? "" : s);
			s = client.getWebParam2();
			Env.setContext(newCtx, "webParam2", s == null ? "" : s);
			s = client.getWebParam3();
			Env.setContext(newCtx, "webParam3", s == null ? "" : s);
			s = client.getWebParam4();
			Env.setContext(newCtx, "webParam4", s == null ? "" : s);
			s = client.getWebParam5();
			Env.setContext(newCtx, "webParam5", s == null ? "" : s);
			s = client.getWebParam6();
			Env.setContext(newCtx, "webParam6", s == null ? "" : s);
			s = client.getWebOrderEMail();
			Env.setContext(newCtx, "webOrderEMail", s == null ? "" : s);
			s = client.getWebInfo();
			if (s != null && s.length() > 0)
				Env.setContext(newCtx, HDR_INFO, s);
			//	M_PriceList_ID, DocumentDir
			Env.setContext(newCtx, "#M_PriceList_ID", client.getInfo().getM_PriceList_ID());
			s = client.getDocumentDir();
			Env.setContext(newCtx, CTX_DOCUMENT_DIR, s == null ? "" : s);

			//	Default Language
			if (newCtx.getProperty("#AD_Language") == null)
				Env.setContext(newCtx, "#AD_Language", "en_US");

			//	Save - Key is AD_Client_ID
			s_cacheCtx.put(key, newCtx);
		}
		//	return new Properties (pp);	seems not to work with JSP
		Enumeration e = newCtx.keys();
		while (e.hasMoreElements())
		{
			String pKey = (String)e.nextElement();
			ctx.setProperty(pKey, newCtx.getProperty(pKey));
		}
		return ctx;
	}	//	getDefaults

	/*************************************************************************/

	private final static String		COOKIE_NAME = "CompiereWebUser";

	/**
	 * 	Get Web User from Cookie
	 * 	@param request request with cookie
	 * 	@return web user or null
	 */
	public static String getCookieWebUser (HttpServletRequest request)
	{
		Cookie[] cookies = request.getCookies();
		if (cookies == null)
			return null;
		for (int i = 0; i < cookies.length; i++)
		{
			if (COOKIE_NAME.equals(cookies[i].getName()))
				return cookies[i].getValue();
		}
		return null;
	}

	/**
	 * 	Add Cookie with web user
	 * 	@param request request (for context path)
	 * 	@param response response to add cookie
	 * 	@param webUser email address
	 */
	public static void addCookieWebUser (HttpServletRequest request, HttpServletResponse response, String webUser)
	{
		Cookie cookie = new Cookie(COOKIE_NAME, webUser);
		cookie.setComment("Compiere Web User");
		cookie.setPath(request.getContextPath());
		cookie.setMaxAge(2592000);      //  30 days in seconds   60*60*24*30
		response.addCookie(cookie);
	}	//	setCookieWebUser

	/**
	 * 	Remove Cookie with web user by setting user to _
	 * 	@param request request (for context path)
	 * 	@param response response to add cookie
	 */
	public static void deleteCookieWebUser (HttpServletRequest request, HttpServletResponse response)
	{
		Cookie cookie = new Cookie(COOKIE_NAME, " ");
		cookie.setComment("Compiere Web User");
		cookie.setPath(request.getContextPath());
		cookie.setMaxAge(1);      //  second
		response.addCookie(cookie);
	}	//	deleteCookieWebUser

	/**************************************************************************
	 * 	Send EMail
	 * 	@param ctx	context
	 * 	@param to	email to address
	 * 	@param subject	subject
	 * 	@param message	message
	 * 	@return mail EMail.SENT_OK or error message 
	 */
	public static String sendEMail (Properties ctx, String to, String subject, String message)
	{
		MClient client = MClient.get(ctx);
		//
		EMail em = new EMail(client, null, to, subject, message);
		//
		String webOrderEMail = ctx.getProperty("webOrderEMail");
		if (webOrderEMail != null && webOrderEMail.length() > 0)
			em.addBcc(webOrderEMail);
		//
		return em.send();
	}	//	sendEMail
}	//	JSPEnv
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
import org.apache.log4j.Logger;

import org.compiere.*;
import org.compiere.util.*;
import org.compiere.model.*;

/**
 *  JSP Environment Utilities
 *
 *  @author Jorg Janke
 *  @version $Id: JSPEnv.java,v 1.14 2003/07/16 19:12:12 jjanke Exp $
 */
public class JSPEnv
{
	/**	Logger							*/
	static private Logger			s_log = Logger.getLogger (JSPEnv.class);

	public final static String		CONTEXT_NAME = "ctx";
	public final static String		CTX_SERVER_CONTEXT = "context";
	public final static String		CTX_DOCUMENT_DIR = "documentDir";
	/** Header Message					*/
	public final static String		HDR_MESSAGE = "hdrMessage";

	/**
	 * 	Get Context from Session
	 *	@param request request
	 * 	@return properties
	 */
	public static Properties getCtx (HttpServletRequest request)
	{
		//	Session
		HttpSession session = request.getSession(true);
		Properties ctx = (Properties)session.getAttribute(CONTEXT_NAME);
		if (ctx != null)
		{
			session.setMaxInactiveInterval(1800);	//	30 Min
			return ctx;
		}

		//
		ctx = new Properties();
		//	Create New
		ServletContext sc = session.getServletContext();
		Enumeration en = sc.getInitParameterNames();
		while (en.hasMoreElements())
		{
			String key = (String)en.nextElement();
			String value = sc.getInitParameter(key);
			ctx.setProperty(key, value);
		}
		//	Default Client
		int AD_Client_ID = Env.getContextAsInt(ctx, "#AD_Client_ID");
		if (AD_Client_ID == 0)
		{
			AD_Client_ID = DB.getSQLValue("SELECT AD_Client_ID FROM AD_Client WHERE AD_Client_ID > 11 AND IsActive='Y'");
			if (AD_Client_ID < 0)
				AD_Client_ID = 11;	//	GardenWorld
			Env.setContext (ctx, "#AD_Client_ID", AD_Client_ID);
		}

		ctx = getDefaults(ctx, AD_Client_ID);
		//	ServerContext	- dev2/wstore
		ctx.put(CTX_SERVER_CONTEXT, request.getServerName() + request.getContextPath());

		//	save it
		s_log.debug("getCtx #" + ctx.size());
		session.setAttribute(CONTEXT_NAME, ctx);
		return ctx;
	}	//	getCtx

	private static HashMap		s_cache = new HashMap();

	/**
	 * 	Get Defaults
	 * 	@param ctx context
	 * 	@param AD_Client_ID client
	 * 	@return context
	 */
	private static Properties getDefaults (Properties ctx, int AD_Client_ID)
	{
		Integer key = new Integer (AD_Client_ID);
		Properties pp = (Properties)s_cache.get(key);
		if (pp != null)
		{
		//	return new Properties (pp);	seems not to work with JSP
			Enumeration e = pp.keys();
			while (e.hasMoreElements())
			{
				String pKey = (String)e.nextElement();
				ctx.setProperty(pKey, pp.getProperty(pKey));
			}
			return ctx;
		}

		/**	Create New Context		*/
		s_log.info("getDefaults - AD_Client_ID=" + AD_Client_ID);

		//	Default Trx! Org
		if (Env.getContextAsInt(ctx, "#AD_Org_ID") == 0)
		{
			int AD_Org_ID = DB.getSQLValue("SELECT AD_Org_ID FROM AD_Org WHERE AD_Client_ID=? AND IsActive='Y' AND IsSummary='N' ORDER BY 1", AD_Client_ID);
			Env.setContext(ctx, "#AD_Org_ID", AD_Org_ID);
		}
		//	Default User
		if (Env.getContextAsInt(ctx, "#AD_User_ID") == 0)
			ctx.setProperty("#AD_User_ID", "0");	//	System

		//	Warehouse
		if (Env.getContextAsInt(ctx, "#M_Warehouse_ID") == 0)
		{
			int M_Warehouse_ID = DB.getSQLValue("SELECT M_Warehouse_ID FROM M_Warehouse WHERE AD_Client_ID=? AND IsActive='Y' ORDER BY 1", AD_Client_ID);
			Env.setContext(ctx, "#M_Warehouse_ID", M_Warehouse_ID);
		}
		//	Sales Rep
		if (Env.getContextAsInt(ctx, "#SalesRep_ID") == 0)
		{
			int SalesRep_ID = 0;
			Env.setContext(ctx, "#SalesRep_ID", SalesRep_ID);
		}
		//	Payment Term
		if (Env.getContextAsInt(ctx, "#C_PaymentTerm_ID") == 0)
		{
			int C_PaymentTerm_ID = DB.getSQLValue("SELECT C_PaymentTerm_ID FROM C_PaymentTerm WHERE AD_Client_ID=? AND IsDefault='Y' ORDER BY NetDays", AD_Client_ID);
			Env.setContext(ctx, "#C_PaymentTerm_ID", C_PaymentTerm_ID);
		}

		/****************************************/

		String sql = "SELECT c.Name,c.Description, c.SMTPHost,c.RequestEMail,c.RequestUser,c.RequestUserPw,"	//	1..6
			+ " c.AD_Language, c.WebDir, c.WebParam1,c.WebParam2,c.WebParam3,c.WebParam4, c.WebOrderEMail,"		//	7..13
			+ " ci.M_PriceList_ID, c.DocumentDir "
			+ "FROM AD_Client c"
			+ " INNER JOIN AD_ClientInfo ci ON (c.AD_Client_ID=ci.AD_Client_ID) "
			+ "WHERE c.AD_Client_ID=?";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				//	Name,Description, SMTPHost,RequestEMail,RequestUser, RequestUserPw
				/** @todo catch null values in db */
				ctx.setProperty("name", rs.getString(1));
				ctx.setProperty("description", rs.getString(2));
				String s = rs.getString(3);
				if (s == null)
					s = "localhost";
				ctx.setProperty("SMTPHost", s);
				ctx.setProperty("RequestEMail", rs.getString(4));
				ctx.setProperty("RequestUser", rs.getString(5));
				ctx.setProperty("RequestUserPw", rs.getString(6));
				//	AD_Language, WebDir, WebParam1,WebParam2,WebParam3,WebParam4, WebOrderEMail
				if (ctx.getProperty("#AD_Language") == null)
					ctx.setProperty("#AD_Language", rs.getString(7));
				ctx.setProperty("webDir", rs.getString(8));
				s = rs.getString(9);
				ctx.setProperty("webParam1", s == null ? "" : s);
				s = rs.getString(10);
				ctx.setProperty("webParam2", s == null ? "" : s);
				s = rs.getString(11);
				ctx.setProperty("webParam3", s == null ? "" : s);
				s = rs.getString(12);
				ctx.setProperty("webParam4", s == null ? "" : s);
				s = rs.getString(13);
				ctx.setProperty("webOrderEMail", s == null ? "" : s);
				//	M_PriceList_ID, DocumentDir
				Env.setContext(ctx, "#M_PriceList_ID", rs.getInt(14));
				s = rs.getString(15);
				Env.setContext(ctx, CTX_DOCUMENT_DIR, s == null ? "" : s);
			}
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("setDefaults", e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}

		//	Default Language
		if (ctx.getProperty("#AD_Language") == null)
			ctx.setProperty("#AD_Language", "en_US");

		s_cache.put(key, ctx);
		return ctx;
	}	//	getDefaults

	/*************************************************************************/

	private final static String		COOKIE_NAME = "CompiereWebUser";

	/**
	 * 	Get Web User from Cookie
	 * 	@param request request with cookie
	 * 	@return web user or null
	 */
	public static String getCookieWebUser (HttpServletRequest request)
	{
		Cookie[] cookies = request.getCookies();
		if (cookies == null)
			return null;
		for (int i = 0; i < cookies.length; i++)
		{
			if (COOKIE_NAME.equals(cookies[i].getName()))
				return cookies[i].getValue();
		}
		return null;
	}

	/**
	 * 	Add Cookie with web user
	 * 	@param request request (for context path)
	 * 	@param response response to add cookie
	 * 	@param webUser email address
	 */
	public static void addCookieWebUser (HttpServletRequest request, HttpServletResponse response, String webUser)
	{
		Cookie cookie = new Cookie(COOKIE_NAME, webUser);
		cookie.setComment("Compiere Web User");
		cookie.setPath(request.getContextPath());
		cookie.setMaxAge(2592000);      //  30 days in seconds   60*60*24*30
		response.addCookie(cookie);
	}	//	setCookieWebUser

	/**
	 * 	Remove Cookie with web user by setting user to _
	 * 	@param request request (for context path)
	 * 	@param response response to add cookie
	 */
	public static void deleteCookieWebUser (HttpServletRequest request, HttpServletResponse response)
	{
		Cookie cookie = new Cookie(COOKIE_NAME, " ");
		cookie.setComment("Compiere Web User");
		cookie.setPath(request.getContextPath());
		cookie.setMaxAge(1);      //  second
		response.addCookie(cookie);
	}	//	deleteCookieWebUser

	/*************************************************************************/

	/**
	 * 	Get Bank Account
	 * 	@param session session
	 *	@param ctx context
	 * 	@return bank account
	 */
	static protected MBankAccount getBankAccount(HttpSession session, Properties ctx)
	{
		MBankAccount ba = (MBankAccount)session.getAttribute(OrderServlet.BANKACCOUNT_ATTR);
		if (ba != null)
			return ba;

		int AD_Client_ID = Env.getContextAsInt(ctx, "#AD_Client_ID");
		int C_BankAccount_ID = Env.getContextAsInt(ctx, "#C_BankAccount_ID");
		if (C_BankAccount_ID != 0)
			ba = new MBankAccount (ctx, C_BankAccount_ID);
		else
		{
			String sql = "SELECT * FROM C_BankAccount ba "
				+ "WHERE ba.AD_Client_ID=?"
				+ " AND EXISTS (SELECT * FROM C_PaymentProcessor pp WHERE ba.C_BankAccount_ID=pp.C_BankAccount_ID)"
				+ " AND ba.IsActive='Y'";
			PreparedStatement pstmt = null;
			try
			{
				pstmt = DB.prepareStatement(sql);
				pstmt.setInt(1, AD_Client_ID);
				ResultSet rs = pstmt.executeQuery();
				if (rs.next())
					ba = new MBankAccount (ctx, rs);
				else
					s_log.error("getBankAccount - No Payment Processor defined");
				rs.close();
				pstmt.close();
				pstmt = null;
			}
			catch (Exception e)
			{
				s_log.error("setBankAccount", e);
			}
			finally
			{
				try
				{
					if (pstmt != null)
						pstmt.close ();
				}
				catch (Exception e)
				{}
				pstmt = null;
			}
		}
		s_log.debug("getBankAccount - " + ba + " - AD_Client_ID=" + AD_Client_ID);
		session.setAttribute (OrderServlet.BANKACCOUNT_ATTR, ba);
		return ba;
	}	//	setBankAccount


}	//	JSPEnv
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

import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.log4j.Logger;

import org.compiere.util.*;
import org.compiere.model.*;

/**
 *  JSP Environment Utilities
 *
 *  @author Jorg Janke
 *  @version $Id: JSPEnv.java,v 1.23 2004/05/02 02:12:18 jjanke Exp $
 */
public class JSPEnv
{
	/**	Logger							*/
	static private Logger			s_log = Logger.getLogger (JSPEnv.class);

	public final static String		CONTEXT_NAME = "ctx";
	public final static String		CTX_SERVER_CONTEXT = "context";
	public final static String		CTX_DOCUMENT_DIR = "documentDir";
	/** Header (Error) Message			*/
	public final static String		HDR_MESSAGE = "hdrMessage";
	/** Header Info Message				*/
	public final static String		HDR_INFO = "hdrInfo";
	/**	Context							*/
	private static CCache			s_cacheCtx = new CCache("JSPEnvCtx", 2, 60);	//	60 minute refresh


	/**
	 * 	Get Context from Session
	 *	@param request request
	 * 	@return properties
	 */
	public static Properties getCtx (HttpServletRequest request)
	{
		//	Session
		HttpSession session = request.getSession(true);
		Properties ctx = (Properties)session.getAttribute(CONTEXT_NAME);

		//	New Context
		if (ctx == null)
		{
			s_log.info ("getCtx - new (" + request.getRemoteAddr() + ")");
			ctx = new Properties();
			//	Add Servlet Init Parameters
			ServletContext sc = session.getServletContext();
			Enumeration en = sc.getInitParameterNames();
			while (en.hasMoreElements())
			{
				String key = (String)en.nextElement();
				String value = sc.getInitParameter(key);
				ctx.setProperty(key, value);
			}
			//	Default Client
			int AD_Client_ID = Env.getContextAsInt(ctx, "#AD_Client_ID");
			if (AD_Client_ID == 0)
			{
				AD_Client_ID = DB.getSQLValue("SELECT AD_Client_ID FROM AD_Client WHERE AD_Client_ID > 11 AND IsActive='Y'");
				if (AD_Client_ID < 0)
					AD_Client_ID = 11;	//	GardenWorld
				Env.setContext (ctx, "#AD_Client_ID", AD_Client_ID);
			}
			//	Add Defaults
			ctx = getDefaults (ctx, AD_Client_ID);
			//	ServerContext	- dev2/wstore
			ctx.put(CTX_SERVER_CONTEXT, request.getServerName() + request.getContextPath());

			//	save it
			session.setAttribute(CONTEXT_NAME, ctx);
			s_log.debug ("getCtx - new #" + ctx.size());
		//	s_log.debug ("getCtx - " + ctx);
		}

		//	Add/set current user
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
		if (wu != null)
		{
			int AD_User_ID = wu.getAD_User_ID();
			Env.setContext(ctx, "#AD_User_ID", AD_User_ID);		//	security
		}

		//	Finish
		session.setMaxInactiveInterval(1800);	//	30 Min	HARDCODED
		String info = (String)ctx.get(HDR_INFO);
		if (info != null)
			session.setAttribute(HDR_INFO, info);
		return ctx;
	}	//	getCtx

	/**
	 * 	Get Defaults
	 * 	@param ctx context
	 * 	@param AD_Client_ID client
	 * 	@return context
	 */
	private static Properties getDefaults (Properties ctx, int AD_Client_ID)
	{
		Integer key = new Integer (AD_Client_ID);
		Properties newCtx = (Properties)s_cacheCtx.get(key);
		
		/**	Create New Context		*/
		if (newCtx == null)
		{
			s_log.info("getDefaults - AD_Client_ID=" + AD_Client_ID);
			newCtx = new Properties();
			//	copy explicitly
			Enumeration e = ctx.keys();
			while (e.hasMoreElements())
			{
				String pKey = (String)e.nextElement();
				newCtx.setProperty(pKey, ctx.getProperty(pKey));
			}
			
			//	Default Trx! Org
			if (Env.getContextAsInt(newCtx, "#AD_Org_ID") == 0)
			{
				int AD_Org_ID = DB.getSQLValue("SELECT AD_Org_ID FROM AD_Org WHERE AD_Client_ID=? AND IsActive='Y' AND IsSummary='N' ORDER BY 1", AD_Client_ID);
				Env.setContext(newCtx, "#AD_Org_ID", AD_Org_ID);
			}
			//	Default User
			if (Env.getContextAsInt(newCtx, "#AD_User_ID") == 0)
			{
				int AD_User_ID = 0;		//	HARDCODED - System
				Env.setContext(newCtx, "#AD_User_ID", AD_User_ID);
			}
			//	Default Role for access
			if (Env.getContextAsInt(newCtx, "#AD_Role_ID") == 0)
			{
				int AD_Role_ID = 0;		//	HARDCODED - System
				Env.setContext(newCtx, "#AD_Role_ID", AD_Role_ID);
			}

			//	Warehouse
			if (Env.getContextAsInt(newCtx, "#M_Warehouse_ID") == 0)
			{
				int M_Warehouse_ID = DB.getSQLValue("SELECT M_Warehouse_ID FROM M_Warehouse WHERE AD_Client_ID=? AND IsActive='Y' ORDER BY 1", AD_Client_ID);
				Env.setContext(newCtx, "#M_Warehouse_ID", M_Warehouse_ID);
			}
			//	Sales Rep
			if (Env.getContextAsInt(newCtx, "#SalesRep_ID") == 0)
			{
				int SalesRep_ID = 0;	//	HARDCODED - Syatem
				Env.setContext(newCtx, "#SalesRep_ID", SalesRep_ID);
			}
			//	Payment Term
			if (Env.getContextAsInt(newCtx, "#C_PaymentTerm_ID") == 0)
			{
				int C_PaymentTerm_ID = DB.getSQLValue("SELECT C_PaymentTerm_ID FROM C_PaymentTerm WHERE AD_Client_ID=? AND IsDefault='Y' ORDER BY NetDays", AD_Client_ID);
				Env.setContext(newCtx, "#C_PaymentTerm_ID", C_PaymentTerm_ID);
			}

			/****************************************/

			//	Read from disk
			MClient client = MClient.get (newCtx, AD_Client_ID);
			//	Name,Description, SMTPHost,RequestEMail,RequestUser, RequestUserPw
			Env.setContext(newCtx, "name", client.getName());
			Env.setContext(newCtx, "description", client.getDescription());
			Env.setContext(newCtx, "SMTPHost", client.getSMTPHost());
			Env.setContext(newCtx, EMail.CTX_REQUEST_EMAIL, client.getRequestEMail());
			Env.setContext(newCtx, EMail.CTX_REQUEST_EMAIL_USER, client.getRequestUser());
			Env.setContext(newCtx, EMail.CTX_REQUEST_EMAIL_USERPW, client.getRequestUserPW());
			
			//	AD_Language, WebDir, WebParam1,WebParam2,WebParam3,WebParam4, WebOrderEMail
			if (newCtx.getProperty("#AD_Language") == null && client.getAD_Language() != null)
				Env.setContext(newCtx, "#AD_Language", client.getAD_Language());
			Env.setContext(newCtx, "webDir", client.getWebDir());
			String s = client.getWebParam1();
			Env.setContext(newCtx, "webParam1", s == null ? "" : s);
			s = client.getWebParam2();
			Env.setContext(newCtx, "webParam2", s == null ? "" : s);
			s = client.getWebParam3();
			Env.setContext(newCtx, "webParam3", s == null ? "" : s);
			s = client.getWebParam4();
			Env.setContext(newCtx, "webParam4", s == null ? "" : s);
			s = client.getWebParam5();
			Env.setContext(newCtx, "webParam5", s == null ? "" : s);
			s = client.getWebParam6();
			Env.setContext(newCtx, "webParam6", s == null ? "" : s);
			s = client.getWebOrderEMail();
			Env.setContext(newCtx, "webOrderEMail", s == null ? "" : s);
			s = client.getWebInfo();
			if (s != null && s.length() > 0)
				Env.setContext(newCtx, HDR_INFO, s);
			//	M_PriceList_ID, DocumentDir
			Env.setContext(newCtx, "#M_PriceList_ID", client.getInfo().getM_PriceList_ID());
			s = client.getDocumentDir();
			Env.setContext(newCtx, CTX_DOCUMENT_DIR, s == null ? "" : s);

			//	Default Language
			if (newCtx.getProperty("#AD_Language") == null)
				Env.setContext(newCtx, "#AD_Language", "en_US");

			//	Save - Key is AD_Client_ID
			s_cacheCtx.put(key, newCtx);
		}
		//	return new Properties (pp);	seems not to work with JSP
		Enumeration e = newCtx.keys();
		while (e.hasMoreElements())
		{
			String pKey = (String)e.nextElement();
			ctx.setProperty(pKey, newCtx.getProperty(pKey));
		}
		return ctx;
	}	//	getDefaults

	/*************************************************************************/

	private final static String		COOKIE_NAME = "CompiereWebUser";

	/**
	 * 	Get Web User from Cookie
	 * 	@param request request with cookie
	 * 	@return web user or null
	 */
	public static String getCookieWebUser (HttpServletRequest request)
	{
		Cookie[] cookies = request.getCookies();
		if (cookies == null)
			return null;
		for (int i = 0; i < cookies.length; i++)
		{
			if (COOKIE_NAME.equals(cookies[i].getName()))
				return cookies[i].getValue();
		}
		return null;
	}

	/**
	 * 	Add Cookie with web user
	 * 	@param request request (for context path)
	 * 	@param response response to add cookie
	 * 	@param webUser email address
	 */
	public static void addCookieWebUser (HttpServletRequest request, HttpServletResponse response, String webUser)
	{
		Cookie cookie = new Cookie(COOKIE_NAME, webUser);
		cookie.setComment("Compiere Web User");
		cookie.setPath(request.getContextPath());
		cookie.setMaxAge(2592000);      //  30 days in seconds   60*60*24*30
		response.addCookie(cookie);
	}	//	setCookieWebUser

	/**
	 * 	Remove Cookie with web user by setting user to _
	 * 	@param request request (for context path)
	 * 	@param response response to add cookie
	 */
	public static void deleteCookieWebUser (HttpServletRequest request, HttpServletResponse response)
	{
		Cookie cookie = new Cookie(COOKIE_NAME, " ");
		cookie.setComment("Compiere Web User");
		cookie.setPath(request.getContextPath());
		cookie.setMaxAge(1);      //  second
		response.addCookie(cookie);
	}	//	deleteCookieWebUser

	/**************************************************************************
	 * 	Send EMail
	 * 	@param ctx	context
	 * 	@param to	email to address
	 * 	@param subject	subject
	 * 	@param message	message
	 * 	@return mail EMail.SENT_OK or error message 
	 */
	public static String sendEMail (Properties ctx, String to, String subject, String message)
	{
		MClient client = MClient.get(ctx);
		//
		EMail em = new EMail(client, null, to, subject, message);
		//
		String webOrderEMail = ctx.getProperty("webOrderEMail");
		if (webOrderEMail != null && webOrderEMail.length() > 0)
			em.addBcc(webOrderEMail);
		//
		return em.send();
	}	//	sendEMail
}	//	JSPEnv
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
import org.apache.log4j.Logger;

import org.compiere.*;
import org.compiere.util.*;
import org.compiere.model.*;

/**
 *  JSP Environment Utilities
 *
 *  @author Jorg Janke
 *  @version $Id: JSPEnv.java,v 1.17 2003/09/07 06:15:16 jjanke Exp $
 */
public class JSPEnv
{
	/**	Logger							*/
	static private Logger			s_log = Logger.getLogger (JSPEnv.class);

	public final static String		CONTEXT_NAME = "ctx";
	public final static String		CTX_SERVER_CONTEXT = "context";
	public final static String		CTX_DOCUMENT_DIR = "documentDir";
	/** Header (Error) Message			*/
	public final static String		HDR_MESSAGE = "hdrMessage";
	/** Header Info Message				*/
	public final static String		HDR_INFO = "hdrInfo";
	/** Next Refresh					*/
	public static long				s_refresh;
	public static final int			REFRESH_MS = 360000;		//	1 min = 60,000 - 1 Std = 360,000
	/**	Context							*/
	private static CCache			s_cacheCtx = new CCache("JSPEnvCtx", 2);


	/**
	 * 	Get Context from Session
	 *	@param request request
	 * 	@return properties
	 */
	public static Properties getCtx (HttpServletRequest request)
	{
		//	Session
		HttpSession session = request.getSession(true);
		Properties ctx = (Properties)session.getAttribute(CONTEXT_NAME);
		if (ctx != null && System.currentTimeMillis() < s_refresh)
		{
			session.setMaxInactiveInterval(1800);	//	30 Min
			String info = (String)ctx.get(HDR_INFO);
			if (info != null)
				session.setAttribute(HDR_INFO, info);
			return ctx;
		}

		//
		s_log.info("getCtx - refresh");
		s_refresh = System.currentTimeMillis() + REFRESH_MS;
		ctx = new Properties();
		//	Create New
		ServletContext sc = session.getServletContext();
		Enumeration en = sc.getInitParameterNames();
		while (en.hasMoreElements())
		{
			String key = (String)en.nextElement();
			String value = sc.getInitParameter(key);
			ctx.setProperty(key, value);
		}
		//	Default Client
		int AD_Client_ID = Env.getContextAsInt(ctx, "#AD_Client_ID");
		if (AD_Client_ID == 0)
		{
			AD_Client_ID = DB.getSQLValue("SELECT AD_Client_ID FROM AD_Client WHERE AD_Client_ID > 11 AND IsActive='Y'");
			if (AD_Client_ID < 0)
				AD_Client_ID = 11;	//	GardenWorld
			Env.setContext (ctx, "#AD_Client_ID", AD_Client_ID);
		}

		ctx = getDefaults(ctx, AD_Client_ID);
		//	ServerContext	- dev2/wstore
		ctx.put(CTX_SERVER_CONTEXT, request.getServerName() + request.getContextPath());

		//	save it
		s_log.debug("getCtx #" + ctx.size());
		session.setAttribute(CONTEXT_NAME, ctx);
		return ctx;
	}	//	getCtx

	/**
	 * 	Get Defaults
	 * 	@param ctx context
	 * 	@param AD_Client_ID client
	 * 	@return context
	 */
	private static Properties getDefaults (Properties ctx, int AD_Client_ID)
	{
		Integer key = new Integer (AD_Client_ID);
		Properties pp = (Properties)s_cacheCtx.get(key);
		if (pp != null)
		{
		//	return new Properties (pp);	seems not to work with JSP
			Enumeration e = pp.keys();
			while (e.hasMoreElements())
			{
				String pKey = (String)e.nextElement();
				ctx.setProperty(pKey, pp.getProperty(pKey));
			}
			return ctx;
		}

		/**	Create New Context		*/
		s_log.info("getDefaults - AD_Client_ID=" + AD_Client_ID);

		//	Default Trx! Org
		if (Env.getContextAsInt(ctx, "#AD_Org_ID") == 0)
		{
			int AD_Org_ID = DB.getSQLValue("SELECT AD_Org_ID FROM AD_Org WHERE AD_Client_ID=? AND IsActive='Y' AND IsSummary='N' ORDER BY 1", AD_Client_ID);
			Env.setContext(ctx, "#AD_Org_ID", AD_Org_ID);
		}
		//	Default User
		if (Env.getContextAsInt(ctx, "#AD_User_ID") == 0)
			ctx.setProperty("#AD_User_ID", "0");	//	System

		//	Warehouse
		if (Env.getContextAsInt(ctx, "#M_Warehouse_ID") == 0)
		{
			int M_Warehouse_ID = DB.getSQLValue("SELECT M_Warehouse_ID FROM M_Warehouse WHERE AD_Client_ID=? AND IsActive='Y' ORDER BY 1", AD_Client_ID);
			Env.setContext(ctx, "#M_Warehouse_ID", M_Warehouse_ID);
		}
		//	Sales Rep
		if (Env.getContextAsInt(ctx, "#SalesRep_ID") == 0)
		{
			int SalesRep_ID = 0;
			Env.setContext(ctx, "#SalesRep_ID", SalesRep_ID);
		}
		//	Payment Term
		if (Env.getContextAsInt(ctx, "#C_PaymentTerm_ID") == 0)
		{
			int C_PaymentTerm_ID = DB.getSQLValue("SELECT C_PaymentTerm_ID FROM C_PaymentTerm WHERE AD_Client_ID=? AND IsDefault='Y' ORDER BY NetDays", AD_Client_ID);
			Env.setContext(ctx, "#C_PaymentTerm_ID", C_PaymentTerm_ID);
		}

		/****************************************/

		//	Read from disk
		MClient client = MClient.get (ctx, AD_Client_ID);
		//	Name,Description, SMTPHost,RequestEMail,RequestUser, RequestUserPw
		Env.setContext(ctx, "name", client.getName());
		Env.setContext(ctx, "description", client.getDescription());
		Env.setContext(ctx, "SMTPHost", client.getSMTPHost());
		Env.setContext(ctx, "RequestEMail", client.getRequestEMail());
		Env.setContext(ctx, "RequestUser", client.getRequestUser());
		Env.setContext(ctx, "RequestUserPw", client.getRequestUserPW());
		//	AD_Language, WebDir, WebParam1,WebParam2,WebParam3,WebParam4, WebOrderEMail
		if (ctx.getProperty("#AD_Language") == null && client.getAD_Language() != null)
			Env.setContext(ctx, "#AD_Language", client.getAD_Language());
		Env.setContext(ctx, "webDir", client.getWebDir());
		String s = client.getWebParam1();
		Env.setContext(ctx, "webParam1", s == null ? "" : s);
		s = client.getWebParam2();
		Env.setContext(ctx, "webParam2", s == null ? "" : s);
		s = client.getWebParam3();
		Env.setContext(ctx, "webParam3", s == null ? "" : s);
		s = client.getWebParam4();
		Env.setContext(ctx, "webParam4", s == null ? "" : s);
		s = client.getWebParam5();
		Env.setContext(ctx, "webParam5", s == null ? "" : s);
		s = client.getWebParam6();
		Env.setContext(ctx, "webParam6", s == null ? "" : s);
		s = client.getWebOrderEMail();
		Env.setContext(ctx, "webOrderEMail", s == null ? "" : s);
		s = client.getWebInfo();
		if (s != null && s.length() > 0)
			Env.setContext(ctx, HDR_INFO, s);
		//	M_PriceList_ID, DocumentDir
		Env.setContext(ctx, "#M_PriceList_ID", client.getMClientInfo().getM_PriceList_ID());
		s = client.getDocumentDir();
		Env.setContext(ctx, CTX_DOCUMENT_DIR, s == null ? "" : s);

		//	Default Language
		if (ctx.getProperty("#AD_Language") == null)
			Env.setContext(ctx, "#AD_Language", "en_US");

		s_cacheCtx.put(key, ctx);	//	Key is AD_Client_ID
		return ctx;
	}	//	getDefaults

	/*************************************************************************/

	private final static String		COOKIE_NAME = "CompiereWebUser";

	/**
	 * 	Get Web User from Cookie
	 * 	@param request request with cookie
	 * 	@return web user or null
	 */
	public static String getCookieWebUser (HttpServletRequest request)
	{
		Cookie[] cookies = request.getCookies();
		if (cookies == null)
			return null;
		for (int i = 0; i < cookies.length; i++)
		{
			if (COOKIE_NAME.equals(cookies[i].getName()))
				return cookies[i].getValue();
		}
		return null;
	}

	/**
	 * 	Add Cookie with web user
	 * 	@param request request (for context path)
	 * 	@param response response to add cookie
	 * 	@param webUser email address
	 */
	public static void addCookieWebUser (HttpServletRequest request, HttpServletResponse response, String webUser)
	{
		Cookie cookie = new Cookie(COOKIE_NAME, webUser);
		cookie.setComment("Compiere Web User");
		cookie.setPath(request.getContextPath());
		cookie.setMaxAge(2592000);      //  30 days in seconds   60*60*24*30
		response.addCookie(cookie);
	}	//	setCookieWebUser

	/**
	 * 	Remove Cookie with web user by setting user to _
	 * 	@param request request (for context path)
	 * 	@param response response to add cookie
	 */
	public static void deleteCookieWebUser (HttpServletRequest request, HttpServletResponse response)
	{
		Cookie cookie = new Cookie(COOKIE_NAME, " ");
		cookie.setComment("Compiere Web User");
		cookie.setPath(request.getContextPath());
		cookie.setMaxAge(1);      //  second
		response.addCookie(cookie);
	}	//	deleteCookieWebUser

}	//	JSPEnv
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
import org.apache.log4j.Logger;

import org.compiere.*;
import org.compiere.util.*;
import org.compiere.model.*;

/**
 *  JSP Environment Utilities
 *
 *  @author Jorg Janke
 *  @version $Id: JSPEnv.java,v 1.14 2003/07/16 19:12:12 jjanke Exp $
 */
public class JSPEnv
{
	/**	Logger							*/
	static private Logger			s_log = Logger.getLogger (JSPEnv.class);

	public final static String		CONTEXT_NAME = "ctx";
	public final static String		CTX_SERVER_CONTEXT = "context";
	public final static String		CTX_DOCUMENT_DIR = "documentDir";
	/** Header Message					*/
	public final static String		HDR_MESSAGE = "hdrMessage";

	/**
	 * 	Get Context from Session
	 *	@param request request
	 * 	@return properties
	 */
	public static Properties getCtx (HttpServletRequest request)
	{
		//	Session
		HttpSession session = request.getSession(true);
		Properties ctx = (Properties)session.getAttribute(CONTEXT_NAME);
		if (ctx != null)
		{
			session.setMaxInactiveInterval(1800);	//	30 Min
			return ctx;
		}

		//
		ctx = new Properties();
		//	Create New
		ServletContext sc = session.getServletContext();
		Enumeration en = sc.getInitParameterNames();
		while (en.hasMoreElements())
		{
			String key = (String)en.nextElement();
			String value = sc.getInitParameter(key);
			ctx.setProperty(key, value);
		}
		//	Default Client
		int AD_Client_ID = Env.getContextAsInt(ctx, "#AD_Client_ID");
		if (AD_Client_ID == 0)
		{
			AD_Client_ID = DB.getSQLValue("SELECT AD_Client_ID FROM AD_Client WHERE AD_Client_ID > 11 AND IsActive='Y'");
			if (AD_Client_ID < 0)
				AD_Client_ID = 11;	//	GardenWorld
			Env.setContext (ctx, "#AD_Client_ID", AD_Client_ID);
		}

		ctx = getDefaults(ctx, AD_Client_ID);
		//	ServerContext	- dev2/wstore
		ctx.put(CTX_SERVER_CONTEXT, request.getServerName() + request.getContextPath());

		//	save it
		s_log.debug("getCtx #" + ctx.size());
		session.setAttribute(CONTEXT_NAME, ctx);
		return ctx;
	}	//	getCtx

	private static HashMap		s_cache = new HashMap();

	/**
	 * 	Get Defaults
	 * 	@param ctx context
	 * 	@param AD_Client_ID client
	 * 	@return context
	 */
	private static Properties getDefaults (Properties ctx, int AD_Client_ID)
	{
		Integer key = new Integer (AD_Client_ID);
		Properties pp = (Properties)s_cache.get(key);
		if (pp != null)
		{
		//	return new Properties (pp);	seems not to work with JSP
			Enumeration e = pp.keys();
			while (e.hasMoreElements())
			{
				String pKey = (String)e.nextElement();
				ctx.setProperty(pKey, pp.getProperty(pKey));
			}
			return ctx;
		}

		/**	Create New Context		*/
		s_log.info("getDefaults - AD_Client_ID=" + AD_Client_ID);

		//	Default Trx! Org
		if (Env.getContextAsInt(ctx, "#AD_Org_ID") == 0)
		{
			int AD_Org_ID = DB.getSQLValue("SELECT AD_Org_ID FROM AD_Org WHERE AD_Client_ID=? AND IsActive='Y' AND IsSummary='N' ORDER BY 1", AD_Client_ID);
			Env.setContext(ctx, "#AD_Org_ID", AD_Org_ID);
		}
		//	Default User
		if (Env.getContextAsInt(ctx, "#AD_User_ID") == 0)
			ctx.setProperty("#AD_User_ID", "0");	//	System

		//	Warehouse
		if (Env.getContextAsInt(ctx, "#M_Warehouse_ID") == 0)
		{
			int M_Warehouse_ID = DB.getSQLValue("SELECT M_Warehouse_ID FROM M_Warehouse WHERE AD_Client_ID=? AND IsActive='Y' ORDER BY 1", AD_Client_ID);
			Env.setContext(ctx, "#M_Warehouse_ID", M_Warehouse_ID);
		}
		//	Sales Rep
		if (Env.getContextAsInt(ctx, "#SalesRep_ID") == 0)
		{
			int SalesRep_ID = 0;
			Env.setContext(ctx, "#SalesRep_ID", SalesRep_ID);
		}
		//	Payment Term
		if (Env.getContextAsInt(ctx, "#C_PaymentTerm_ID") == 0)
		{
			int C_PaymentTerm_ID = DB.getSQLValue("SELECT C_PaymentTerm_ID FROM C_PaymentTerm WHERE AD_Client_ID=? AND IsDefault='Y' ORDER BY NetDays", AD_Client_ID);
			Env.setContext(ctx, "#C_PaymentTerm_ID", C_PaymentTerm_ID);
		}

		/****************************************/

		String sql = "SELECT c.Name,c.Description, c.SMTPHost,c.RequestEMail,c.RequestUser,c.RequestUserPw,"	//	1..6
			+ " c.AD_Language, c.WebDir, c.WebParam1,c.WebParam2,c.WebParam3,c.WebParam4, c.WebOrderEMail,"		//	7..13
			+ " ci.M_PriceList_ID, c.DocumentDir "
			+ "FROM AD_Client c"
			+ " INNER JOIN AD_ClientInfo ci ON (c.AD_Client_ID=ci.AD_Client_ID) "
			+ "WHERE c.AD_Client_ID=?";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				//	Name,Description, SMTPHost,RequestEMail,RequestUser, RequestUserPw
				/** @todo catch null values in db */
				ctx.setProperty("name", rs.getString(1));
				ctx.setProperty("description", rs.getString(2));
				String s = rs.getString(3);
				if (s == null)
					s = "localhost";
				ctx.setProperty("SMTPHost", s);
				ctx.setProperty("RequestEMail", rs.getString(4));
				ctx.setProperty("RequestUser", rs.getString(5));
				ctx.setProperty("RequestUserPw", rs.getString(6));
				//	AD_Language, WebDir, WebParam1,WebParam2,WebParam3,WebParam4, WebOrderEMail
				if (ctx.getProperty("#AD_Language") == null)
					ctx.setProperty("#AD_Language", rs.getString(7));
				ctx.setProperty("webDir", rs.getString(8));
				s = rs.getString(9);
				ctx.setProperty("webParam1", s == null ? "" : s);
				s = rs.getString(10);
				ctx.setProperty("webParam2", s == null ? "" : s);
				s = rs.getString(11);
				ctx.setProperty("webParam3", s == null ? "" : s);
				s = rs.getString(12);
				ctx.setProperty("webParam4", s == null ? "" : s);
				s = rs.getString(13);
				ctx.setProperty("webOrderEMail", s == null ? "" : s);
				//	M_PriceList_ID, DocumentDir
				Env.setContext(ctx, "#M_PriceList_ID", rs.getInt(14));
				s = rs.getString(15);
				Env.setContext(ctx, CTX_DOCUMENT_DIR, s == null ? "" : s);
			}
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("setDefaults", e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}

		//	Default Language
		if (ctx.getProperty("#AD_Language") == null)
			ctx.setProperty("#AD_Language", "en_US");

		s_cache.put(key, ctx);
		return ctx;
	}	//	getDefaults

	/*************************************************************************/

	private final static String		COOKIE_NAME = "CompiereWebUser";

	/**
	 * 	Get Web User from Cookie
	 * 	@param request request with cookie
	 * 	@return web user or null
	 */
	public static String getCookieWebUser (HttpServletRequest request)
	{
		Cookie[] cookies = request.getCookies();
		if (cookies == null)
			return null;
		for (int i = 0; i < cookies.length; i++)
		{
			if (COOKIE_NAME.equals(cookies[i].getName()))
				return cookies[i].getValue();
		}
		return null;
	}

	/**
	 * 	Add Cookie with web user
	 * 	@param request request (for context path)
	 * 	@param response response to add cookie
	 * 	@param webUser email address
	 */
	public static void addCookieWebUser (HttpServletRequest request, HttpServletResponse response, String webUser)
	{
		Cookie cookie = new Cookie(COOKIE_NAME, webUser);
		cookie.setComment("Compiere Web User");
		cookie.setPath(request.getContextPath());
		cookie.setMaxAge(2592000);      //  30 days in seconds   60*60*24*30
		response.addCookie(cookie);
	}	//	setCookieWebUser

	/**
	 * 	Remove Cookie with web user by setting user to _
	 * 	@param request request (for context path)
	 * 	@param response response to add cookie
	 */
	public static void deleteCookieWebUser (HttpServletRequest request, HttpServletResponse response)
	{
		Cookie cookie = new Cookie(COOKIE_NAME, " ");
		cookie.setComment("Compiere Web User");
		cookie.setPath(request.getContextPath());
		cookie.setMaxAge(1);      //  second
		response.addCookie(cookie);
	}	//	deleteCookieWebUser

	/*************************************************************************/

	/**
	 * 	Get Bank Account
	 * 	@param session session
	 *	@param ctx context
	 * 	@return bank account
	 */
	static protected MBankAccount getBankAccount(HttpSession session, Properties ctx)
	{
		MBankAccount ba = (MBankAccount)session.getAttribute(OrderServlet.BANKACCOUNT_ATTR);
		if (ba != null)
			return ba;

		int AD_Client_ID = Env.getContextAsInt(ctx, "#AD_Client_ID");
		int C_BankAccount_ID = Env.getContextAsInt(ctx, "#C_BankAccount_ID");
		if (C_BankAccount_ID != 0)
			ba = new MBankAccount (ctx, C_BankAccount_ID);
		else
		{
			String sql = "SELECT * FROM C_BankAccount ba "
				+ "WHERE ba.AD_Client_ID=?"
				+ " AND EXISTS (SELECT * FROM C_PaymentProcessor pp WHERE ba.C_BankAccount_ID=pp.C_BankAccount_ID)"
				+ " AND ba.IsActive='Y'";
			PreparedStatement pstmt = null;
			try
			{
				pstmt = DB.prepareStatement(sql);
				pstmt.setInt(1, AD_Client_ID);
				ResultSet rs = pstmt.executeQuery();
				if (rs.next())
					ba = new MBankAccount (ctx, rs);
				else
					s_log.error("getBankAccount - No Payment Processor defined");
				rs.close();
				pstmt.close();
				pstmt = null;
			}
			catch (Exception e)
			{
				s_log.error("setBankAccount", e);
			}
			finally
			{
				try
				{
					if (pstmt != null)
						pstmt.close ();
				}
				catch (Exception e)
				{}
				pstmt = null;
			}
		}
		s_log.debug("getBankAccount - " + ba + " - AD_Client_ID=" + AD_Client_ID);
		session.setAttribute (OrderServlet.BANKACCOUNT_ATTR, ba);
		return ba;
	}	//	setBankAccount


}	//	JSPEnv
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

import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.log4j.Logger;

import org.compiere.util.*;
import org.compiere.model.*;

/**
 *  JSP Environment Utilities
 *
 *  @author Jorg Janke
 *  @version $Id: JSPEnv.java,v 1.23 2004/05/02 02:12:18 jjanke Exp $
 */
public class JSPEnv
{
	/**	Logger							*/
	static private Logger			s_log = Logger.getLogger (JSPEnv.class);

	public final static String		CONTEXT_NAME = "ctx";
	public final static String		CTX_SERVER_CONTEXT = "context";
	public final static String		CTX_DOCUMENT_DIR = "documentDir";
	/** Header (Error) Message			*/
	public final static String		HDR_MESSAGE = "hdrMessage";
	/** Header Info Message				*/
	public final static String		HDR_INFO = "hdrInfo";
	/**	Context							*/
	private static CCache			s_cacheCtx = new CCache("JSPEnvCtx", 2, 60);	//	60 minute refresh


	/**
	 * 	Get Context from Session
	 *	@param request request
	 * 	@return properties
	 */
	public static Properties getCtx (HttpServletRequest request)
	{
		//	Session
		HttpSession session = request.getSession(true);
		Properties ctx = (Properties)session.getAttribute(CONTEXT_NAME);

		//	New Context
		if (ctx == null)
		{
			s_log.info ("getCtx - new (" + request.getRemoteAddr() + ")");
			ctx = new Properties();
			//	Add Servlet Init Parameters
			ServletContext sc = session.getServletContext();
			Enumeration en = sc.getInitParameterNames();
			while (en.hasMoreElements())
			{
				String key = (String)en.nextElement();
				String value = sc.getInitParameter(key);
				ctx.setProperty(key, value);
			}
			//	Default Client
			int AD_Client_ID = Env.getContextAsInt(ctx, "#AD_Client_ID");
			if (AD_Client_ID == 0)
			{
				AD_Client_ID = DB.getSQLValue("SELECT AD_Client_ID FROM AD_Client WHERE AD_Client_ID > 11 AND IsActive='Y'");
				if (AD_Client_ID < 0)
					AD_Client_ID = 11;	//	GardenWorld
				Env.setContext (ctx, "#AD_Client_ID", AD_Client_ID);
			}
			//	Add Defaults
			ctx = getDefaults (ctx, AD_Client_ID);
			//	ServerContext	- dev2/wstore
			ctx.put(CTX_SERVER_CONTEXT, request.getServerName() + request.getContextPath());

			//	save it
			session.setAttribute(CONTEXT_NAME, ctx);
			s_log.debug ("getCtx - new #" + ctx.size());
		//	s_log.debug ("getCtx - " + ctx);
		}

		//	Add/set current user
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
		if (wu != null)
		{
			int AD_User_ID = wu.getAD_User_ID();
			Env.setContext(ctx, "#AD_User_ID", AD_User_ID);		//	security
		}

		//	Finish
		session.setMaxInactiveInterval(1800);	//	30 Min	HARDCODED
		String info = (String)ctx.get(HDR_INFO);
		if (info != null)
			session.setAttribute(HDR_INFO, info);
		return ctx;
	}	//	getCtx

	/**
	 * 	Get Defaults
	 * 	@param ctx context
	 * 	@param AD_Client_ID client
	 * 	@return context
	 */
	private static Properties getDefaults (Properties ctx, int AD_Client_ID)
	{
		Integer key = new Integer (AD_Client_ID);
		Properties newCtx = (Properties)s_cacheCtx.get(key);
		
		/**	Create New Context		*/
		if (newCtx == null)
		{
			s_log.info("getDefaults - AD_Client_ID=" + AD_Client_ID);
			newCtx = new Properties();
			//	copy explicitly
			Enumeration e = ctx.keys();
			while (e.hasMoreElements())
			{
				String pKey = (String)e.nextElement();
				newCtx.setProperty(pKey, ctx.getProperty(pKey));
			}
			
			//	Default Trx! Org
			if (Env.getContextAsInt(newCtx, "#AD_Org_ID") == 0)
			{
				int AD_Org_ID = DB.getSQLValue("SELECT AD_Org_ID FROM AD_Org WHERE AD_Client_ID=? AND IsActive='Y' AND IsSummary='N' ORDER BY 1", AD_Client_ID);
				Env.setContext(newCtx, "#AD_Org_ID", AD_Org_ID);
			}
			//	Default User
			if (Env.getContextAsInt(newCtx, "#AD_User_ID") == 0)
			{
				int AD_User_ID = 0;		//	HARDCODED - System
				Env.setContext(newCtx, "#AD_User_ID", AD_User_ID);
			}
			//	Default Role for access
			if (Env.getContextAsInt(newCtx, "#AD_Role_ID") == 0)
			{
				int AD_Role_ID = 0;		//	HARDCODED - System
				Env.setContext(newCtx, "#AD_Role_ID", AD_Role_ID);
			}

			//	Warehouse
			if (Env.getContextAsInt(newCtx, "#M_Warehouse_ID") == 0)
			{
				int M_Warehouse_ID = DB.getSQLValue("SELECT M_Warehouse_ID FROM M_Warehouse WHERE AD_Client_ID=? AND IsActive='Y' ORDER BY 1", AD_Client_ID);
				Env.setContext(newCtx, "#M_Warehouse_ID", M_Warehouse_ID);
			}
			//	Sales Rep
			if (Env.getContextAsInt(newCtx, "#SalesRep_ID") == 0)
			{
				int SalesRep_ID = 0;	//	HARDCODED - Syatem
				Env.setContext(newCtx, "#SalesRep_ID", SalesRep_ID);
			}
			//	Payment Term
			if (Env.getContextAsInt(newCtx, "#C_PaymentTerm_ID") == 0)
			{
				int C_PaymentTerm_ID = DB.getSQLValue("SELECT C_PaymentTerm_ID FROM C_PaymentTerm WHERE AD_Client_ID=? AND IsDefault='Y' ORDER BY NetDays", AD_Client_ID);
				Env.setContext(newCtx, "#C_PaymentTerm_ID", C_PaymentTerm_ID);
			}

			/****************************************/

			//	Read from disk
			MClient client = MClient.get (newCtx, AD_Client_ID);
			//	Name,Description, SMTPHost,RequestEMail,RequestUser, RequestUserPw
			Env.setContext(newCtx, "name", client.getName());
			Env.setContext(newCtx, "description", client.getDescription());
			Env.setContext(newCtx, "SMTPHost", client.getSMTPHost());
			Env.setContext(newCtx, EMail.CTX_REQUEST_EMAIL, client.getRequestEMail());
			Env.setContext(newCtx, EMail.CTX_REQUEST_EMAIL_USER, client.getRequestUser());
			Env.setContext(newCtx, EMail.CTX_REQUEST_EMAIL_USERPW, client.getRequestUserPW());
			
			//	AD_Language, WebDir, WebParam1,WebParam2,WebParam3,WebParam4, WebOrderEMail
			if (newCtx.getProperty("#AD_Language") == null && client.getAD_Language() != null)
				Env.setContext(newCtx, "#AD_Language", client.getAD_Language());
			Env.setContext(newCtx, "webDir", client.getWebDir());
			String s = client.getWebParam1();
			Env.setContext(newCtx, "webParam1", s == null ? "" : s);
			s = client.getWebParam2();
			Env.setContext(newCtx, "webParam2", s == null ? "" : s);
			s = client.getWebParam3();
			Env.setContext(newCtx, "webParam3", s == null ? "" : s);
			s = client.getWebParam4();
			Env.setContext(newCtx, "webParam4", s == null ? "" : s);
			s = client.getWebParam5();
			Env.setContext(newCtx, "webParam5", s == null ? "" : s);
			s = client.getWebParam6();
			Env.setContext(newCtx, "webParam6", s == null ? "" : s);
			s = client.getWebOrderEMail();
			Env.setContext(newCtx, "webOrderEMail", s == null ? "" : s);
			s = client.getWebInfo();
			if (s != null && s.length() > 0)
				Env.setContext(newCtx, HDR_INFO, s);
			//	M_PriceList_ID, DocumentDir
			Env.setContext(newCtx, "#M_PriceList_ID", client.getInfo().getM_PriceList_ID());
			s = client.getDocumentDir();
			Env.setContext(newCtx, CTX_DOCUMENT_DIR, s == null ? "" : s);

			//	Default Language
			if (newCtx.getProperty("#AD_Language") == null)
				Env.setContext(newCtx, "#AD_Language", "en_US");

			//	Save - Key is AD_Client_ID
			s_cacheCtx.put(key, newCtx);
		}
		//	return new Properties (pp);	seems not to work with JSP
		Enumeration e = newCtx.keys();
		while (e.hasMoreElements())
		{
			String pKey = (String)e.nextElement();
			ctx.setProperty(pKey, newCtx.getProperty(pKey));
		}
		return ctx;
	}	//	getDefaults

	/*************************************************************************/

	private final static String		COOKIE_NAME = "CompiereWebUser";

	/**
	 * 	Get Web User from Cookie
	 * 	@param request request with cookie
	 * 	@return web user or null
	 */
	public static String getCookieWebUser (HttpServletRequest request)
	{
		Cookie[] cookies = request.getCookies();
		if (cookies == null)
			return null;
		for (int i = 0; i < cookies.length; i++)
		{
			if (COOKIE_NAME.equals(cookies[i].getName()))
				return cookies[i].getValue();
		}
		return null;
	}

	/**
	 * 	Add Cookie with web user
	 * 	@param request request (for context path)
	 * 	@param response response to add cookie
	 * 	@param webUser email address
	 */
	public static void addCookieWebUser (HttpServletRequest request, HttpServletResponse response, String webUser)
	{
		Cookie cookie = new Cookie(COOKIE_NAME, webUser);
		cookie.setComment("Compiere Web User");
		cookie.setPath(request.getContextPath());
		cookie.setMaxAge(2592000);      //  30 days in seconds   60*60*24*30
		response.addCookie(cookie);
	}	//	setCookieWebUser

	/**
	 * 	Remove Cookie with web user by setting user to _
	 * 	@param request request (for context path)
	 * 	@param response response to add cookie
	 */
	public static void deleteCookieWebUser (HttpServletRequest request, HttpServletResponse response)
	{
		Cookie cookie = new Cookie(COOKIE_NAME, " ");
		cookie.setComment("Compiere Web User");
		cookie.setPath(request.getContextPath());
		cookie.setMaxAge(1);      //  second
		response.addCookie(cookie);
	}	//	deleteCookieWebUser

	/**************************************************************************
	 * 	Send EMail
	 * 	@param ctx	context
	 * 	@param to	email to address
	 * 	@param subject	subject
	 * 	@param message	message
	 * 	@return mail EMail.SENT_OK or error message 
	 */
	public static String sendEMail (Properties ctx, String to, String subject, String message)
	{
		MClient client = MClient.get(ctx);
		//
		EMail em = new EMail(client, null, to, subject, message);
		//
		String webOrderEMail = ctx.getProperty("webOrderEMail");
		if (webOrderEMail != null && webOrderEMail.length() > 0)
			em.addBcc(webOrderEMail);
		//
		return em.send();
	}	//	sendEMail
}	//	JSPEnv
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
import org.apache.log4j.Logger;

import org.compiere.*;
import org.compiere.util.*;
import org.compiere.model.*;

/**
 *  JSP Environment Utilities
 *
 *  @author Jorg Janke
 *  @version $Id: JSPEnv.java,v 1.14 2003/07/16 19:12:12 jjanke Exp $
 */
public class JSPEnv
{
	/**	Logger							*/
	static private Logger			s_log = Logger.getLogger (JSPEnv.class);

	public final static String		CONTEXT_NAME = "ctx";
	public final static String		CTX_SERVER_CONTEXT = "context";
	public final static String		CTX_DOCUMENT_DIR = "documentDir";
	/** Header Message					*/
	public final static String		HDR_MESSAGE = "hdrMessage";

	/**
	 * 	Get Context from Session
	 *	@param request request
	 * 	@return properties
	 */
	public static Properties getCtx (HttpServletRequest request)
	{
		//	Session
		HttpSession session = request.getSession(true);
		Properties ctx = (Properties)session.getAttribute(CONTEXT_NAME);
		if (ctx != null)
		{
			session.setMaxInactiveInterval(1800);	//	30 Min
			return ctx;
		}

		//
		ctx = new Properties();
		//	Create New
		ServletContext sc = session.getServletContext();
		Enumeration en = sc.getInitParameterNames();
		while (en.hasMoreElements())
		{
			String key = (String)en.nextElement();
			String value = sc.getInitParameter(key);
			ctx.setProperty(key, value);
		}
		//	Default Client
		int AD_Client_ID = Env.getContextAsInt(ctx, "#AD_Client_ID");
		if (AD_Client_ID == 0)
		{
			AD_Client_ID = DB.getSQLValue("SELECT AD_Client_ID FROM AD_Client WHERE AD_Client_ID > 11 AND IsActive='Y'");
			if (AD_Client_ID < 0)
				AD_Client_ID = 11;	//	GardenWorld
			Env.setContext (ctx, "#AD_Client_ID", AD_Client_ID);
		}

		ctx = getDefaults(ctx, AD_Client_ID);
		//	ServerContext	- dev2/wstore
		ctx.put(CTX_SERVER_CONTEXT, request.getServerName() + request.getContextPath());

		//	save it
		s_log.debug("getCtx #" + ctx.size());
		session.setAttribute(CONTEXT_NAME, ctx);
		return ctx;
	}	//	getCtx

	private static HashMap		s_cache = new HashMap();

	/**
	 * 	Get Defaults
	 * 	@param ctx context
	 * 	@param AD_Client_ID client
	 * 	@return context
	 */
	private static Properties getDefaults (Properties ctx, int AD_Client_ID)
	{
		Integer key = new Integer (AD_Client_ID);
		Properties pp = (Properties)s_cache.get(key);
		if (pp != null)
		{
		//	return new Properties (pp);	seems not to work with JSP
			Enumeration e = pp.keys();
			while (e.hasMoreElements())
			{
				String pKey = (String)e.nextElement();
				ctx.setProperty(pKey, pp.getProperty(pKey));
			}
			return ctx;
		}

		/**	Create New Context		*/
		s_log.info("getDefaults - AD_Client_ID=" + AD_Client_ID);

		//	Default Trx! Org
		if (Env.getContextAsInt(ctx, "#AD_Org_ID") == 0)
		{
			int AD_Org_ID = DB.getSQLValue("SELECT AD_Org_ID FROM AD_Org WHERE AD_Client_ID=? AND IsActive='Y' AND IsSummary='N' ORDER BY 1", AD_Client_ID);
			Env.setContext(ctx, "#AD_Org_ID", AD_Org_ID);
		}
		//	Default User
		if (Env.getContextAsInt(ctx, "#AD_User_ID") == 0)
			ctx.setProperty("#AD_User_ID", "0");	//	System

		//	Warehouse
		if (Env.getContextAsInt(ctx, "#M_Warehouse_ID") == 0)
		{
			int M_Warehouse_ID = DB.getSQLValue("SELECT M_Warehouse_ID FROM M_Warehouse WHERE AD_Client_ID=? AND IsActive='Y' ORDER BY 1", AD_Client_ID);
			Env.setContext(ctx, "#M_Warehouse_ID", M_Warehouse_ID);
		}
		//	Sales Rep
		if (Env.getContextAsInt(ctx, "#SalesRep_ID") == 0)
		{
			int SalesRep_ID = 0;
			Env.setContext(ctx, "#SalesRep_ID", SalesRep_ID);
		}
		//	Payment Term
		if (Env.getContextAsInt(ctx, "#C_PaymentTerm_ID") == 0)
		{
			int C_PaymentTerm_ID = DB.getSQLValue("SELECT C_PaymentTerm_ID FROM C_PaymentTerm WHERE AD_Client_ID=? AND IsDefault='Y' ORDER BY NetDays", AD_Client_ID);
			Env.setContext(ctx, "#C_PaymentTerm_ID", C_PaymentTerm_ID);
		}

		/****************************************/

		String sql = "SELECT c.Name,c.Description, c.SMTPHost,c.RequestEMail,c.RequestUser,c.RequestUserPw,"	//	1..6
			+ " c.AD_Language, c.WebDir, c.WebParam1,c.WebParam2,c.WebParam3,c.WebParam4, c.WebOrderEMail,"		//	7..13
			+ " ci.M_PriceList_ID, c.DocumentDir "
			+ "FROM AD_Client c"
			+ " INNER JOIN AD_ClientInfo ci ON (c.AD_Client_ID=ci.AD_Client_ID) "
			+ "WHERE c.AD_Client_ID=?";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				//	Name,Description, SMTPHost,RequestEMail,RequestUser, RequestUserPw
				/** @todo catch null values in db */
				ctx.setProperty("name", rs.getString(1));
				ctx.setProperty("description", rs.getString(2));
				String s = rs.getString(3);
				if (s == null)
					s = "localhost";
				ctx.setProperty("SMTPHost", s);
				ctx.setProperty("RequestEMail", rs.getString(4));
				ctx.setProperty("RequestUser", rs.getString(5));
				ctx.setProperty("RequestUserPw", rs.getString(6));
				//	AD_Language, WebDir, WebParam1,WebParam2,WebParam3,WebParam4, WebOrderEMail
				if (ctx.getProperty("#AD_Language") == null)
					ctx.setProperty("#AD_Language", rs.getString(7));
				ctx.setProperty("webDir", rs.getString(8));
				s = rs.getString(9);
				ctx.setProperty("webParam1", s == null ? "" : s);
				s = rs.getString(10);
				ctx.setProperty("webParam2", s == null ? "" : s);
				s = rs.getString(11);
				ctx.setProperty("webParam3", s == null ? "" : s);
				s = rs.getString(12);
				ctx.setProperty("webParam4", s == null ? "" : s);
				s = rs.getString(13);
				ctx.setProperty("webOrderEMail", s == null ? "" : s);
				//	M_PriceList_ID, DocumentDir
				Env.setContext(ctx, "#M_PriceList_ID", rs.getInt(14));
				s = rs.getString(15);
				Env.setContext(ctx, CTX_DOCUMENT_DIR, s == null ? "" : s);
			}
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("setDefaults", e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}

		//	Default Language
		if (ctx.getProperty("#AD_Language") == null)
			ctx.setProperty("#AD_Language", "en_US");

		s_cache.put(key, ctx);
		return ctx;
	}	//	getDefaults

	/*************************************************************************/

	private final static String		COOKIE_NAME = "CompiereWebUser";

	/**
	 * 	Get Web User from Cookie
	 * 	@param request request with cookie
	 * 	@return web user or null
	 */
	public static String getCookieWebUser (HttpServletRequest request)
	{
		Cookie[] cookies = request.getCookies();
		if (cookies == null)
			return null;
		for (int i = 0; i < cookies.length; i++)
		{
			if (COOKIE_NAME.equals(cookies[i].getName()))
				return cookies[i].getValue();
		}
		return null;
	}

	/**
	 * 	Add Cookie with web user
	 * 	@param request request (for context path)
	 * 	@param response response to add cookie
	 * 	@param webUser email address
	 */
	public static void addCookieWebUser (HttpServletRequest request, HttpServletResponse response, String webUser)
	{
		Cookie cookie = new Cookie(COOKIE_NAME, webUser);
		cookie.setComment("Compiere Web User");
		cookie.setPath(request.getContextPath());
		cookie.setMaxAge(2592000);      //  30 days in seconds   60*60*24*30
		response.addCookie(cookie);
	}	//	setCookieWebUser

	/**
	 * 	Remove Cookie with web user by setting user to _
	 * 	@param request request (for context path)
	 * 	@param response response to add cookie
	 */
	public static void deleteCookieWebUser (HttpServletRequest request, HttpServletResponse response)
	{
		Cookie cookie = new Cookie(COOKIE_NAME, " ");
		cookie.setComment("Compiere Web User");
		cookie.setPath(request.getContextPath());
		cookie.setMaxAge(1);      //  second
		response.addCookie(cookie);
	}	//	deleteCookieWebUser

	/*************************************************************************/

	/**
	 * 	Get Bank Account
	 * 	@param session session
	 *	@param ctx context
	 * 	@return bank account
	 */
	static protected MBankAccount getBankAccount(HttpSession session, Properties ctx)
	{
		MBankAccount ba = (MBankAccount)session.getAttribute(OrderServlet.BANKACCOUNT_ATTR);
		if (ba != null)
			return ba;

		int AD_Client_ID = Env.getContextAsInt(ctx, "#AD_Client_ID");
		int C_BankAccount_ID = Env.getContextAsInt(ctx, "#C_BankAccount_ID");
		if (C_BankAccount_ID != 0)
			ba = new MBankAccount (ctx, C_BankAccount_ID);
		else
		{
			String sql = "SELECT * FROM C_BankAccount ba "
				+ "WHERE ba.AD_Client_ID=?"
				+ " AND EXISTS (SELECT * FROM C_PaymentProcessor pp WHERE ba.C_BankAccount_ID=pp.C_BankAccount_ID)"
				+ " AND ba.IsActive='Y'";
			PreparedStatement pstmt = null;
			try
			{
				pstmt = DB.prepareStatement(sql);
				pstmt.setInt(1, AD_Client_ID);
				ResultSet rs = pstmt.executeQuery();
				if (rs.next())
					ba = new MBankAccount (ctx, rs);
				else
					s_log.error("getBankAccount - No Payment Processor defined");
				rs.close();
				pstmt.close();
				pstmt = null;
			}
			catch (Exception e)
			{
				s_log.error("setBankAccount", e);
			}
			finally
			{
				try
				{
					if (pstmt != null)
						pstmt.close ();
				}
				catch (Exception e)
				{}
				pstmt = null;
			}
		}
		s_log.debug("getBankAccount - " + ba + " - AD_Client_ID=" + AD_Client_ID);
		session.setAttribute (OrderServlet.BANKACCOUNT_ATTR, ba);
		return ba;
	}	//	setBankAccount


}	//	JSPEnv
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
import org.apache.log4j.Logger;

import org.compiere.*;
import org.compiere.util.*;
import org.compiere.model.*;

/**
 *  JSP Environment Utilities
 *
 *  @author Jorg Janke
 *  @version $Id: JSPEnv.java,v 1.17 2003/09/07 06:15:16 jjanke Exp $
 */
public class JSPEnv
{
	/**	Logger							*/
	static private Logger			s_log = Logger.getLogger (JSPEnv.class);

	public final static String		CONTEXT_NAME = "ctx";
	public final static String		CTX_SERVER_CONTEXT = "context";
	public final static String		CTX_DOCUMENT_DIR = "documentDir";
	/** Header (Error) Message			*/
	public final static String		HDR_MESSAGE = "hdrMessage";
	/** Header Info Message				*/
	public final static String		HDR_INFO = "hdrInfo";
	/** Next Refresh					*/
	public static long				s_refresh;
	public static final int			REFRESH_MS = 360000;		//	1 min = 60,000 - 1 Std = 360,000
	/**	Context							*/
	private static CCache			s_cacheCtx = new CCache("JSPEnvCtx", 2);


	/**
	 * 	Get Context from Session
	 *	@param request request
	 * 	@return properties
	 */
	public static Properties getCtx (HttpServletRequest request)
	{
		//	Session
		HttpSession session = request.getSession(true);
		Properties ctx = (Properties)session.getAttribute(CONTEXT_NAME);
		if (ctx != null && System.currentTimeMillis() < s_refresh)
		{
			session.setMaxInactiveInterval(1800);	//	30 Min
			String info = (String)ctx.get(HDR_INFO);
			if (info != null)
				session.setAttribute(HDR_INFO, info);
			return ctx;
		}

		//
		s_log.info("getCtx - refresh");
		s_refresh = System.currentTimeMillis() + REFRESH_MS;
		ctx = new Properties();
		//	Create New
		ServletContext sc = session.getServletContext();
		Enumeration en = sc.getInitParameterNames();
		while (en.hasMoreElements())
		{
			String key = (String)en.nextElement();
			String value = sc.getInitParameter(key);
			ctx.setProperty(key, value);
		}
		//	Default Client
		int AD_Client_ID = Env.getContextAsInt(ctx, "#AD_Client_ID");
		if (AD_Client_ID == 0)
		{
			AD_Client_ID = DB.getSQLValue("SELECT AD_Client_ID FROM AD_Client WHERE AD_Client_ID > 11 AND IsActive='Y'");
			if (AD_Client_ID < 0)
				AD_Client_ID = 11;	//	GardenWorld
			Env.setContext (ctx, "#AD_Client_ID", AD_Client_ID);
		}

		ctx = getDefaults(ctx, AD_Client_ID);
		//	ServerContext	- dev2/wstore
		ctx.put(CTX_SERVER_CONTEXT, request.getServerName() + request.getContextPath());

		//	save it
		s_log.debug("getCtx #" + ctx.size());
		session.setAttribute(CONTEXT_NAME, ctx);
		return ctx;
	}	//	getCtx

	/**
	 * 	Get Defaults
	 * 	@param ctx context
	 * 	@param AD_Client_ID client
	 * 	@return context
	 */
	private static Properties getDefaults (Properties ctx, int AD_Client_ID)
	{
		Integer key = new Integer (AD_Client_ID);
		Properties pp = (Properties)s_cacheCtx.get(key);
		if (pp != null)
		{
		//	return new Properties (pp);	seems not to work with JSP
			Enumeration e = pp.keys();
			while (e.hasMoreElements())
			{
				String pKey = (String)e.nextElement();
				ctx.setProperty(pKey, pp.getProperty(pKey));
			}
			return ctx;
		}

		/**	Create New Context		*/
		s_log.info("getDefaults - AD_Client_ID=" + AD_Client_ID);

		//	Default Trx! Org
		if (Env.getContextAsInt(ctx, "#AD_Org_ID") == 0)
		{
			int AD_Org_ID = DB.getSQLValue("SELECT AD_Org_ID FROM AD_Org WHERE AD_Client_ID=? AND IsActive='Y' AND IsSummary='N' ORDER BY 1", AD_Client_ID);
			Env.setContext(ctx, "#AD_Org_ID", AD_Org_ID);
		}
		//	Default User
		if (Env.getContextAsInt(ctx, "#AD_User_ID") == 0)
			ctx.setProperty("#AD_User_ID", "0");	//	System

		//	Warehouse
		if (Env.getContextAsInt(ctx, "#M_Warehouse_ID") == 0)
		{
			int M_Warehouse_ID = DB.getSQLValue("SELECT M_Warehouse_ID FROM M_Warehouse WHERE AD_Client_ID=? AND IsActive='Y' ORDER BY 1", AD_Client_ID);
			Env.setContext(ctx, "#M_Warehouse_ID", M_Warehouse_ID);
		}
		//	Sales Rep
		if (Env.getContextAsInt(ctx, "#SalesRep_ID") == 0)
		{
			int SalesRep_ID = 0;
			Env.setContext(ctx, "#SalesRep_ID", SalesRep_ID);
		}
		//	Payment Term
		if (Env.getContextAsInt(ctx, "#C_PaymentTerm_ID") == 0)
		{
			int C_PaymentTerm_ID = DB.getSQLValue("SELECT C_PaymentTerm_ID FROM C_PaymentTerm WHERE AD_Client_ID=? AND IsDefault='Y' ORDER BY NetDays", AD_Client_ID);
			Env.setContext(ctx, "#C_PaymentTerm_ID", C_PaymentTerm_ID);
		}

		/****************************************/

		//	Read from disk
		MClient client = MClient.get (ctx, AD_Client_ID);
		//	Name,Description, SMTPHost,RequestEMail,RequestUser, RequestUserPw
		Env.setContext(ctx, "name", client.getName());
		Env.setContext(ctx, "description", client.getDescription());
		Env.setContext(ctx, "SMTPHost", client.getSMTPHost());
		Env.setContext(ctx, "RequestEMail", client.getRequestEMail());
		Env.setContext(ctx, "RequestUser", client.getRequestUser());
		Env.setContext(ctx, "RequestUserPw", client.getRequestUserPW());
		//	AD_Language, WebDir, WebParam1,WebParam2,WebParam3,WebParam4, WebOrderEMail
		if (ctx.getProperty("#AD_Language") == null && client.getAD_Language() != null)
			Env.setContext(ctx, "#AD_Language", client.getAD_Language());
		Env.setContext(ctx, "webDir", client.getWebDir());
		String s = client.getWebParam1();
		Env.setContext(ctx, "webParam1", s == null ? "" : s);
		s = client.getWebParam2();
		Env.setContext(ctx, "webParam2", s == null ? "" : s);
		s = client.getWebParam3();
		Env.setContext(ctx, "webParam3", s == null ? "" : s);
		s = client.getWebParam4();
		Env.setContext(ctx, "webParam4", s == null ? "" : s);
		s = client.getWebParam5();
		Env.setContext(ctx, "webParam5", s == null ? "" : s);
		s = client.getWebParam6();
		Env.setContext(ctx, "webParam6", s == null ? "" : s);
		s = client.getWebOrderEMail();
		Env.setContext(ctx, "webOrderEMail", s == null ? "" : s);
		s = client.getWebInfo();
		if (s != null && s.length() > 0)
			Env.setContext(ctx, HDR_INFO, s);
		//	M_PriceList_ID, DocumentDir
		Env.setContext(ctx, "#M_PriceList_ID", client.getMClientInfo().getM_PriceList_ID());
		s = client.getDocumentDir();
		Env.setContext(ctx, CTX_DOCUMENT_DIR, s == null ? "" : s);

		//	Default Language
		if (ctx.getProperty("#AD_Language") == null)
			Env.setContext(ctx, "#AD_Language", "en_US");

		s_cacheCtx.put(key, ctx);	//	Key is AD_Client_ID
		return ctx;
	}	//	getDefaults

	/*************************************************************************/

	private final static String		COOKIE_NAME = "CompiereWebUser";

	/**
	 * 	Get Web User from Cookie
	 * 	@param request request with cookie
	 * 	@return web user or null
	 */
	public static String getCookieWebUser (HttpServletRequest request)
	{
		Cookie[] cookies = request.getCookies();
		if (cookies == null)
			return null;
		for (int i = 0; i < cookies.length; i++)
		{
			if (COOKIE_NAME.equals(cookies[i].getName()))
				return cookies[i].getValue();
		}
		return null;
	}

	/**
	 * 	Add Cookie with web user
	 * 	@param request request (for context path)
	 * 	@param response response to add cookie
	 * 	@param webUser email address
	 */
	public static void addCookieWebUser (HttpServletRequest request, HttpServletResponse response, String webUser)
	{
		Cookie cookie = new Cookie(COOKIE_NAME, webUser);
		cookie.setComment("Compiere Web User");
		cookie.setPath(request.getContextPath());
		cookie.setMaxAge(2592000);      //  30 days in seconds   60*60*24*30
		response.addCookie(cookie);
	}	//	setCookieWebUser

	/**
	 * 	Remove Cookie with web user by setting user to _
	 * 	@param request request (for context path)
	 * 	@param response response to add cookie
	 */
	public static void deleteCookieWebUser (HttpServletRequest request, HttpServletResponse response)
	{
		Cookie cookie = new Cookie(COOKIE_NAME, " ");
		cookie.setComment("Compiere Web User");
		cookie.setPath(request.getContextPath());
		cookie.setMaxAge(1);      //  second
		response.addCookie(cookie);
	}	//	deleteCookieWebUser

}	//	JSPEnv
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

import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.log4j.Logger;

import org.compiere.util.*;
import org.compiere.model.*;

/**
 *  JSP Environment Utilities
 *
 *  @author Jorg Janke
 *  @version $Id: JSPEnv.java,v 1.23 2004/05/02 02:12:18 jjanke Exp $
 */
public class JSPEnv
{
	/**	Logger							*/
	static private Logger			s_log = Logger.getLogger (JSPEnv.class);

	public final static String		CONTEXT_NAME = "ctx";
	public final static String		CTX_SERVER_CONTEXT = "context";
	public final static String		CTX_DOCUMENT_DIR = "documentDir";
	/** Header (Error) Message			*/
	public final static String		HDR_MESSAGE = "hdrMessage";
	/** Header Info Message				*/
	public final static String		HDR_INFO = "hdrInfo";
	/**	Context							*/
	private static CCache			s_cacheCtx = new CCache("JSPEnvCtx", 2, 60);	//	60 minute refresh


	/**
	 * 	Get Context from Session
	 *	@param request request
	 * 	@return properties
	 */
	public static Properties getCtx (HttpServletRequest request)
	{
		//	Session
		HttpSession session = request.getSession(true);
		Properties ctx = (Properties)session.getAttribute(CONTEXT_NAME);

		//	New Context
		if (ctx == null)
		{
			s_log.info ("getCtx - new (" + request.getRemoteAddr() + ")");
			ctx = new Properties();
			//	Add Servlet Init Parameters
			ServletContext sc = session.getServletContext();
			Enumeration en = sc.getInitParameterNames();
			while (en.hasMoreElements())
			{
				String key = (String)en.nextElement();
				String value = sc.getInitParameter(key);
				ctx.setProperty(key, value);
			}
			//	Default Client
			int AD_Client_ID = Env.getContextAsInt(ctx, "#AD_Client_ID");
			if (AD_Client_ID == 0)
			{
				AD_Client_ID = DB.getSQLValue("SELECT AD_Client_ID FROM AD_Client WHERE AD_Client_ID > 11 AND IsActive='Y'");
				if (AD_Client_ID < 0)
					AD_Client_ID = 11;	//	GardenWorld
				Env.setContext (ctx, "#AD_Client_ID", AD_Client_ID);
			}
			//	Add Defaults
			ctx = getDefaults (ctx, AD_Client_ID);
			//	ServerContext	- dev2/wstore
			ctx.put(CTX_SERVER_CONTEXT, request.getServerName() + request.getContextPath());

			//	save it
			session.setAttribute(CONTEXT_NAME, ctx);
			s_log.debug ("getCtx - new #" + ctx.size());
		//	s_log.debug ("getCtx - " + ctx);
		}

		//	Add/set current user
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
		if (wu != null)
		{
			int AD_User_ID = wu.getAD_User_ID();
			Env.setContext(ctx, "#AD_User_ID", AD_User_ID);		//	security
		}

		//	Finish
		session.setMaxInactiveInterval(1800);	//	30 Min	HARDCODED
		String info = (String)ctx.get(HDR_INFO);
		if (info != null)
			session.setAttribute(HDR_INFO, info);
		return ctx;
	}	//	getCtx

	/**
	 * 	Get Defaults
	 * 	@param ctx context
	 * 	@param AD_Client_ID client
	 * 	@return context
	 */
	private static Properties getDefaults (Properties ctx, int AD_Client_ID)
	{
		Integer key = new Integer (AD_Client_ID);
		Properties newCtx = (Properties)s_cacheCtx.get(key);
		
		/**	Create New Context		*/
		if (newCtx == null)
		{
			s_log.info("getDefaults - AD_Client_ID=" + AD_Client_ID);
			newCtx = new Properties();
			//	copy explicitly
			Enumeration e = ctx.keys();
			while (e.hasMoreElements())
			{
				String pKey = (String)e.nextElement();
				newCtx.setProperty(pKey, ctx.getProperty(pKey));
			}
			
			//	Default Trx! Org
			if (Env.getContextAsInt(newCtx, "#AD_Org_ID") == 0)
			{
				int AD_Org_ID = DB.getSQLValue("SELECT AD_Org_ID FROM AD_Org WHERE AD_Client_ID=? AND IsActive='Y' AND IsSummary='N' ORDER BY 1", AD_Client_ID);
				Env.setContext(newCtx, "#AD_Org_ID", AD_Org_ID);
			}
			//	Default User
			if (Env.getContextAsInt(newCtx, "#AD_User_ID") == 0)
			{
				int AD_User_ID = 0;		//	HARDCODED - System
				Env.setContext(newCtx, "#AD_User_ID", AD_User_ID);
			}
			//	Default Role for access
			if (Env.getContextAsInt(newCtx, "#AD_Role_ID") == 0)
			{
				int AD_Role_ID = 0;		//	HARDCODED - System
				Env.setContext(newCtx, "#AD_Role_ID", AD_Role_ID);
			}

			//	Warehouse
			if (Env.getContextAsInt(newCtx, "#M_Warehouse_ID") == 0)
			{
				int M_Warehouse_ID = DB.getSQLValue("SELECT M_Warehouse_ID FROM M_Warehouse WHERE AD_Client_ID=? AND IsActive='Y' ORDER BY 1", AD_Client_ID);
				Env.setContext(newCtx, "#M_Warehouse_ID", M_Warehouse_ID);
			}
			//	Sales Rep
			if (Env.getContextAsInt(newCtx, "#SalesRep_ID") == 0)
			{
				int SalesRep_ID = 0;	//	HARDCODED - Syatem
				Env.setContext(newCtx, "#SalesRep_ID", SalesRep_ID);
			}
			//	Payment Term
			if (Env.getContextAsInt(newCtx, "#C_PaymentTerm_ID") == 0)
			{
				int C_PaymentTerm_ID = DB.getSQLValue("SELECT C_PaymentTerm_ID FROM C_PaymentTerm WHERE AD_Client_ID=? AND IsDefault='Y' ORDER BY NetDays", AD_Client_ID);
				Env.setContext(newCtx, "#C_PaymentTerm_ID", C_PaymentTerm_ID);
			}

			/****************************************/

			//	Read from disk
			MClient client = MClient.get (newCtx, AD_Client_ID);
			//	Name,Description, SMTPHost,RequestEMail,RequestUser, RequestUserPw
			Env.setContext(newCtx, "name", client.getName());
			Env.setContext(newCtx, "description", client.getDescription());
			Env.setContext(newCtx, "SMTPHost", client.getSMTPHost());
			Env.setContext(newCtx, EMail.CTX_REQUEST_EMAIL, client.getRequestEMail());
			Env.setContext(newCtx, EMail.CTX_REQUEST_EMAIL_USER, client.getRequestUser());
			Env.setContext(newCtx, EMail.CTX_REQUEST_EMAIL_USERPW, client.getRequestUserPW());
			
			//	AD_Language, WebDir, WebParam1,WebParam2,WebParam3,WebParam4, WebOrderEMail
			if (newCtx.getProperty("#AD_Language") == null && client.getAD_Language() != null)
				Env.setContext(newCtx, "#AD_Language", client.getAD_Language());
			Env.setContext(newCtx, "webDir", client.getWebDir());
			String s = client.getWebParam1();
			Env.setContext(newCtx, "webParam1", s == null ? "" : s);
			s = client.getWebParam2();
			Env.setContext(newCtx, "webParam2", s == null ? "" : s);
			s = client.getWebParam3();
			Env.setContext(newCtx, "webParam3", s == null ? "" : s);
			s = client.getWebParam4();
			Env.setContext(newCtx, "webParam4", s == null ? "" : s);
			s = client.getWebParam5();
			Env.setContext(newCtx, "webParam5", s == null ? "" : s);
			s = client.getWebParam6();
			Env.setContext(newCtx, "webParam6", s == null ? "" : s);
			s = client.getWebOrderEMail();
			Env.setContext(newCtx, "webOrderEMail", s == null ? "" : s);
			s = client.getWebInfo();
			if (s != null && s.length() > 0)
				Env.setContext(newCtx, HDR_INFO, s);
			//	M_PriceList_ID, DocumentDir
			Env.setContext(newCtx, "#M_PriceList_ID", client.getInfo().getM_PriceList_ID());
			s = client.getDocumentDir();
			Env.setContext(newCtx, CTX_DOCUMENT_DIR, s == null ? "" : s);

			//	Default Language
			if (newCtx.getProperty("#AD_Language") == null)
				Env.setContext(newCtx, "#AD_Language", "en_US");

			//	Save - Key is AD_Client_ID
			s_cacheCtx.put(key, newCtx);
		}
		//	return new Properties (pp);	seems not to work with JSP
		Enumeration e = newCtx.keys();
		while (e.hasMoreElements())
		{
			String pKey = (String)e.nextElement();
			ctx.setProperty(pKey, newCtx.getProperty(pKey));
		}
		return ctx;
	}	//	getDefaults

	/*************************************************************************/

	private final static String		COOKIE_NAME = "CompiereWebUser";

	/**
	 * 	Get Web User from Cookie
	 * 	@param request request with cookie
	 * 	@return web user or null
	 */
	public static String getCookieWebUser (HttpServletRequest request)
	{
		Cookie[] cookies = request.getCookies();
		if (cookies == null)
			return null;
		for (int i = 0; i < cookies.length; i++)
		{
			if (COOKIE_NAME.equals(cookies[i].getName()))
				return cookies[i].getValue();
		}
		return null;
	}

	/**
	 * 	Add Cookie with web user
	 * 	@param request request (for context path)
	 * 	@param response response to add cookie
	 * 	@param webUser email address
	 */
	public static void addCookieWebUser (HttpServletRequest request, HttpServletResponse response, String webUser)
	{
		Cookie cookie = new Cookie(COOKIE_NAME, webUser);
		cookie.setComment("Compiere Web User");
		cookie.setPath(request.getContextPath());
		cookie.setMaxAge(2592000);      //  30 days in seconds   60*60*24*30
		response.addCookie(cookie);
	}	//	setCookieWebUser

	/**
	 * 	Remove Cookie with web user by setting user to _
	 * 	@param request request (for context path)
	 * 	@param response response to add cookie
	 */
	public static void deleteCookieWebUser (HttpServletRequest request, HttpServletResponse response)
	{
		Cookie cookie = new Cookie(COOKIE_NAME, " ");
		cookie.setComment("Compiere Web User");
		cookie.setPath(request.getContextPath());
		cookie.setMaxAge(1);      //  second
		response.addCookie(cookie);
	}	//	deleteCookieWebUser

	/**************************************************************************
	 * 	Send EMail
	 * 	@param ctx	context
	 * 	@param to	email to address
	 * 	@param subject	subject
	 * 	@param message	message
	 * 	@return mail EMail.SENT_OK or error message 
	 */
	public static String sendEMail (Properties ctx, String to, String subject, String message)
	{
		MClient client = MClient.get(ctx);
		//
		EMail em = new EMail(client, null, to, subject, message);
		//
		String webOrderEMail = ctx.getProperty("webOrderEMail");
		if (webOrderEMail != null && webOrderEMail.length() > 0)
			em.addBcc(webOrderEMail);
		//
		return em.send();
	}	//	sendEMail
}	//	JSPEnv
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
import org.apache.log4j.Logger;

import org.compiere.*;
import org.compiere.util.*;
import org.compiere.model.*;

/**
 *  JSP Environment Utilities
 *
 *  @author Jorg Janke
 *  @version $Id: JSPEnv.java,v 1.14 2003/07/16 19:12:12 jjanke Exp $
 */
public class JSPEnv
{
	/**	Logger							*/
	static private Logger			s_log = Logger.getLogger (JSPEnv.class);

	public final static String		CONTEXT_NAME = "ctx";
	public final static String		CTX_SERVER_CONTEXT = "context";
	public final static String		CTX_DOCUMENT_DIR = "documentDir";
	/** Header Message					*/
	public final static String		HDR_MESSAGE = "hdrMessage";

	/**
	 * 	Get Context from Session
	 *	@param request request
	 * 	@return properties
	 */
	public static Properties getCtx (HttpServletRequest request)
	{
		//	Session
		HttpSession session = request.getSession(true);
		Properties ctx = (Properties)session.getAttribute(CONTEXT_NAME);
		if (ctx != null)
		{
			session.setMaxInactiveInterval(1800);	//	30 Min
			return ctx;
		}

		//
		ctx = new Properties();
		//	Create New
		ServletContext sc = session.getServletContext();
		Enumeration en = sc.getInitParameterNames();
		while (en.hasMoreElements())
		{
			String key = (String)en.nextElement();
			String value = sc.getInitParameter(key);
			ctx.setProperty(key, value);
		}
		//	Default Client
		int AD_Client_ID = Env.getContextAsInt(ctx, "#AD_Client_ID");
		if (AD_Client_ID == 0)
		{
			AD_Client_ID = DB.getSQLValue("SELECT AD_Client_ID FROM AD_Client WHERE AD_Client_ID > 11 AND IsActive='Y'");
			if (AD_Client_ID < 0)
				AD_Client_ID = 11;	//	GardenWorld
			Env.setContext (ctx, "#AD_Client_ID", AD_Client_ID);
		}

		ctx = getDefaults(ctx, AD_Client_ID);
		//	ServerContext	- dev2/wstore
		ctx.put(CTX_SERVER_CONTEXT, request.getServerName() + request.getContextPath());

		//	save it
		s_log.debug("getCtx #" + ctx.size());
		session.setAttribute(CONTEXT_NAME, ctx);
		return ctx;
	}	//	getCtx

	private static HashMap		s_cache = new HashMap();

	/**
	 * 	Get Defaults
	 * 	@param ctx context
	 * 	@param AD_Client_ID client
	 * 	@return context
	 */
	private static Properties getDefaults (Properties ctx, int AD_Client_ID)
	{
		Integer key = new Integer (AD_Client_ID);
		Properties pp = (Properties)s_cache.get(key);
		if (pp != null)
		{
		//	return new Properties (pp);	seems not to work with JSP
			Enumeration e = pp.keys();
			while (e.hasMoreElements())
			{
				String pKey = (String)e.nextElement();
				ctx.setProperty(pKey, pp.getProperty(pKey));
			}
			return ctx;
		}

		/**	Create New Context		*/
		s_log.info("getDefaults - AD_Client_ID=" + AD_Client_ID);

		//	Default Trx! Org
		if (Env.getContextAsInt(ctx, "#AD_Org_ID") == 0)
		{
			int AD_Org_ID = DB.getSQLValue("SELECT AD_Org_ID FROM AD_Org WHERE AD_Client_ID=? AND IsActive='Y' AND IsSummary='N' ORDER BY 1", AD_Client_ID);
			Env.setContext(ctx, "#AD_Org_ID", AD_Org_ID);
		}
		//	Default User
		if (Env.getContextAsInt(ctx, "#AD_User_ID") == 0)
			ctx.setProperty("#AD_User_ID", "0");	//	System

		//	Warehouse
		if (Env.getContextAsInt(ctx, "#M_Warehouse_ID") == 0)
		{
			int M_Warehouse_ID = DB.getSQLValue("SELECT M_Warehouse_ID FROM M_Warehouse WHERE AD_Client_ID=? AND IsActive='Y' ORDER BY 1", AD_Client_ID);
			Env.setContext(ctx, "#M_Warehouse_ID", M_Warehouse_ID);
		}
		//	Sales Rep
		if (Env.getContextAsInt(ctx, "#SalesRep_ID") == 0)
		{
			int SalesRep_ID = 0;
			Env.setContext(ctx, "#SalesRep_ID", SalesRep_ID);
		}
		//	Payment Term
		if (Env.getContextAsInt(ctx, "#C_PaymentTerm_ID") == 0)
		{
			int C_PaymentTerm_ID = DB.getSQLValue("SELECT C_PaymentTerm_ID FROM C_PaymentTerm WHERE AD_Client_ID=? AND IsDefault='Y' ORDER BY NetDays", AD_Client_ID);
			Env.setContext(ctx, "#C_PaymentTerm_ID", C_PaymentTerm_ID);
		}

		/****************************************/

		String sql = "SELECT c.Name,c.Description, c.SMTPHost,c.RequestEMail,c.RequestUser,c.RequestUserPw,"	//	1..6
			+ " c.AD_Language, c.WebDir, c.WebParam1,c.WebParam2,c.WebParam3,c.WebParam4, c.WebOrderEMail,"		//	7..13
			+ " ci.M_PriceList_ID, c.DocumentDir "
			+ "FROM AD_Client c"
			+ " INNER JOIN AD_ClientInfo ci ON (c.AD_Client_ID=ci.AD_Client_ID) "
			+ "WHERE c.AD_Client_ID=?";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				//	Name,Description, SMTPHost,RequestEMail,RequestUser, RequestUserPw
				/** @todo catch null values in db */
				ctx.setProperty("name", rs.getString(1));
				ctx.setProperty("description", rs.getString(2));
				String s = rs.getString(3);
				if (s == null)
					s = "localhost";
				ctx.setProperty("SMTPHost", s);
				ctx.setProperty("RequestEMail", rs.getString(4));
				ctx.setProperty("RequestUser", rs.getString(5));
				ctx.setProperty("RequestUserPw", rs.getString(6));
				//	AD_Language, WebDir, WebParam1,WebParam2,WebParam3,WebParam4, WebOrderEMail
				if (ctx.getProperty("#AD_Language") == null)
					ctx.setProperty("#AD_Language", rs.getString(7));
				ctx.setProperty("webDir", rs.getString(8));
				s = rs.getString(9);
				ctx.setProperty("webParam1", s == null ? "" : s);
				s = rs.getString(10);
				ctx.setProperty("webParam2", s == null ? "" : s);
				s = rs.getString(11);
				ctx.setProperty("webParam3", s == null ? "" : s);
				s = rs.getString(12);
				ctx.setProperty("webParam4", s == null ? "" : s);
				s = rs.getString(13);
				ctx.setProperty("webOrderEMail", s == null ? "" : s);
				//	M_PriceList_ID, DocumentDir
				Env.setContext(ctx, "#M_PriceList_ID", rs.getInt(14));
				s = rs.getString(15);
				Env.setContext(ctx, CTX_DOCUMENT_DIR, s == null ? "" : s);
			}
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("setDefaults", e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}

		//	Default Language
		if (ctx.getProperty("#AD_Language") == null)
			ctx.setProperty("#AD_Language", "en_US");

		s_cache.put(key, ctx);
		return ctx;
	}	//	getDefaults

	/*************************************************************************/

	private final static String		COOKIE_NAME = "CompiereWebUser";

	/**
	 * 	Get Web User from Cookie
	 * 	@param request request with cookie
	 * 	@return web user or null
	 */
	public static String getCookieWebUser (HttpServletRequest request)
	{
		Cookie[] cookies = request.getCookies();
		if (cookies == null)
			return null;
		for (int i = 0; i < cookies.length; i++)
		{
			if (COOKIE_NAME.equals(cookies[i].getName()))
				return cookies[i].getValue();
		}
		return null;
	}

	/**
	 * 	Add Cookie with web user
	 * 	@param request request (for context path)
	 * 	@param response response to add cookie
	 * 	@param webUser email address
	 */
	public static void addCookieWebUser (HttpServletRequest request, HttpServletResponse response, String webUser)
	{
		Cookie cookie = new Cookie(COOKIE_NAME, webUser);
		cookie.setComment("Compiere Web User");
		cookie.setPath(request.getContextPath());
		cookie.setMaxAge(2592000);      //  30 days in seconds   60*60*24*30
		response.addCookie(cookie);
	}	//	setCookieWebUser

	/**
	 * 	Remove Cookie with web user by setting user to _
	 * 	@param request request (for context path)
	 * 	@param response response to add cookie
	 */
	public static void deleteCookieWebUser (HttpServletRequest request, HttpServletResponse response)
	{
		Cookie cookie = new Cookie(COOKIE_NAME, " ");
		cookie.setComment("Compiere Web User");
		cookie.setPath(request.getContextPath());
		cookie.setMaxAge(1);      //  second
		response.addCookie(cookie);
	}	//	deleteCookieWebUser

	/*************************************************************************/

	/**
	 * 	Get Bank Account
	 * 	@param session session
	 *	@param ctx context
	 * 	@return bank account
	 */
	static protected MBankAccount getBankAccount(HttpSession session, Properties ctx)
	{
		MBankAccount ba = (MBankAccount)session.getAttribute(OrderServlet.BANKACCOUNT_ATTR);
		if (ba != null)
			return ba;

		int AD_Client_ID = Env.getContextAsInt(ctx, "#AD_Client_ID");
		int C_BankAccount_ID = Env.getContextAsInt(ctx, "#C_BankAccount_ID");
		if (C_BankAccount_ID != 0)
			ba = new MBankAccount (ctx, C_BankAccount_ID);
		else
		{
			String sql = "SELECT * FROM C_BankAccount ba "
				+ "WHERE ba.AD_Client_ID=?"
				+ " AND EXISTS (SELECT * FROM C_PaymentProcessor pp WHERE ba.C_BankAccount_ID=pp.C_BankAccount_ID)"
				+ " AND ba.IsActive='Y'";
			PreparedStatement pstmt = null;
			try
			{
				pstmt = DB.prepareStatement(sql);
				pstmt.setInt(1, AD_Client_ID);
				ResultSet rs = pstmt.executeQuery();
				if (rs.next())
					ba = new MBankAccount (ctx, rs);
				else
					s_log.error("getBankAccount - No Payment Processor defined");
				rs.close();
				pstmt.close();
				pstmt = null;
			}
			catch (Exception e)
			{
				s_log.error("setBankAccount", e);
			}
			finally
			{
				try
				{
					if (pstmt != null)
						pstmt.close ();
				}
				catch (Exception e)
				{}
				pstmt = null;
			}
		}
		s_log.debug("getBankAccount - " + ba + " - AD_Client_ID=" + AD_Client_ID);
		session.setAttribute (OrderServlet.BANKACCOUNT_ATTR, ba);
		return ba;
	}	//	setBankAccount


}	//	JSPEnv
