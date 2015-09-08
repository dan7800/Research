/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.util;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;

import org.compiere.Compiere;
import org.compiere.model.*;
import org.compiere.wf.*;

/**
 *  Web Environment and debugging
 *
 *  @author Jorg Janke
 *  @version $Id: WebEnv.java,v 1.2 2004/09/09 14:14:33 jjanke Exp $
 */
public class WebEnv
{
	/** Add HTML Debug Info                     */
	public static boolean DEBUG                 = true;
	/**	Logging									*/
	private static Logger			s_log = Logger.getCLogger(WebEnv.class);

	/**
	 *  Base Directory links <b>http://localhost:8080/compiere</b>
	 *  to the physical <i>%COMPIERE_HOME%/tomcat/webroot/compiere</i> directory
	 */
	public static final String   	DIR_BASE    = "/compiere";      //  /compiere
	/** Image Sub-Directory under BASE          */
	private static final String     DIR_IMAGE   = "images";         //  /compiere/images
	/** Stylesheet Name                         */
	private static final String     STYLE_STD   = "standard.css";   //  /compiere/standard.css
	/** Small Logo                              */
	private static final String     LOGO        = "LogoSmall.gif";  //  /compiere/LogoSmall.gif
	/** Store Sub-Directory under BASE          */
	private static final String     DIR_STORE   = "store";          //  /compiere/store

	/**  Frame name for Commands   */
	public static final String      TARGET_CMD  = "WCmd";
	/**  Frame name for Menu       */
	public static final String      TARGET_MENU = "WMenu";
	/**  Frame name for Apps Window */
	public static final String      TARGET_WINDOW = "WWindow";

	/** Character Set (iso-8859-1)              */
	public static final String      CHARACTERSET = "iso-8859-8";     //  Default: UNKNOWN
	/** Cookie Name                             */
	public static final String      COOKIE_INFO = "CompiereInfo";

	/** Timeout - 15 Minutes                    */
	public static final int         TIMEOUT     = 15*60;

	/** Session Attribute - Context             */
	public static final String      SA_CONTEXT  = "SA_Context";
	/** Session Attribute - Window Status              */
	public static final String      SA_WINDOW   = "SA_Window";
	/** Session Attribute - Login Info          */
	public static final String      SA_LOGININFO = "SA_LoginInfo";
	/** Session Locale                          */
	public static final String      SA_LANGUAGE  = "SA_Language";

	/** Initialization OK?                      */
	private static boolean          s_initOK    = false;
	/** Not Braking Space						*/
	public static String			NBSP = "&nbsp;";

	/**
	 *  Init Web Environment.
	 *  <p>
	 *  To be called from every Servlet in the init method
	 *  or any other Web resource to make sure that the
	 *  environment is properly set.
	 *  @param config config
	 *  @return false if initialization problems
	 */
	public static boolean initWeb (ServletConfig config)
	{
		if (s_initOK)
		{
			s_log.info("initWeb " + config.getServletName());
			return true;
		}
		s_log.info("initWeb - Initial Call - " + config.getServletName());

		//  Load Environment Variables
		s_log.info("Web Context Init Parameter for " + config.getServletContext().getServletContextName());
		Enumeration en = config.getServletContext().getInitParameterNames();
		while (en.hasMoreElements())
		{
			String name = en.nextElement().toString();
			String value = config.getServletContext().getInitParameter(name);
			s_log.info(" - " + name + "=" + value);
			System.setProperty(name, value);
		}

		s_log.info("Servlet Init Parameter for " + config.getServletName());
		en = config.getInitParameterNames();
		while (en.hasMoreElements())
		{
			String name = en.nextElement().toString();
			String value = config.getInitParameter(name);
			s_log.info(" - " + name + "=" + value);
			System.setProperty(name, value);
		}

		try
		{
			s_initOK = Compiere.startupServer ();
			PO.setDocWorkflowMgr (DocWorkflowManager.get());
		}
		catch (Exception ex)
		{
			s_log.error("initWeb", ex);
		}
		if (!s_initOK)
			return false;

		s_log.info("initWeb complete");
		return s_initOK;
	}   //  initWeb


	/**
	 * 	Get AD_Client_ID from Servlet Context or Web Context
	 * 	@param config servlet config
	 * 	@return AD_Client_ID if found or 0
	 */
	public static int getAD_Client_ID (ServletConfig config)
	{
		//	Get Client from Servlet init
		String oo = config.getInitParameter("AD_Client_ID");
		//	Get Client from Web Context
		if (oo == null)
			oo = config.getServletContext().getInitParameter("AD_Client_ID");
		if (oo == null)
		{
			s_log.error("getAD_Client_ID is null");
			return 0;
		}
		int AD_Client_ID = 0;
		try
		{
			AD_Client_ID = Integer.parseInt(oo);
		}
		catch (NumberFormatException ex)
		{
			s_log.error("getAD_Client_ID - " + oo, ex);
		}
		return AD_Client_ID;
	}	//	getAD_Client_ID

	/**
	 * 	Get database user from Servlet Context or Web Context
	 * 	@param servlet servlet config
	 * 	@return db User ID or compiere if not found
	 */
	public static Connection getConnection (HttpServlet servlet)
	{
		//	Get Info from Servlet Context
		String user = servlet.getInitParameter("dbUID");
		String pwd = servlet.getInitParameter("dbPWD");
		//	Get Client Web Context
		if (user == null)
			user = servlet.getServletContext().getInitParameter("dbUID");
		if (pwd == null)
			pwd = servlet.getServletContext().getInitParameter("dbPWD");
		//	Defaults
		if (user == null)
			user = "compiere";
		if (user == null)
			user = "compiere";
/**		//
		try
		{
			Context context = new InitialContext();
			DataSource ds = (DataSource)context.lookup("java:OracleDS");
			log.info ("OracleDS=" + ds.toString());
			Connection con = ds.getConnection(dbUID, dbPWD);
			log.info("Connection AutoCommit=" + con.getAutoCommit());
			//
			BPartnerHome bpHome = (BPartnerHome)context.lookup(BPartnerHome.JNDI_NAME);
			BPartner bp = bpHome.create();
			log.info("BPartner=" + bp.process2());
			bp.remove();
		}
		catch (Exception ex)
		{
			throw new ServletException (ex);
		}
**/
		return null;

	}	//	getDB_UID

	/**
	 * 	Get database user from Servlet Context or Web Context
	 * 	@param config servlet config
	 * 	@return db User ID or compiere if not found
	 */
	public static String getDB_UID (ServletConfig config)
	{
		//	Get DB User from Servlet init
		String user = config.getInitParameter("dbUID");
		//	Get Client from Web Context
		if (user == null)
			user = config.getServletContext().getInitParameter("dbUID");
		if (user == null)
		{
			s_log.error("getDB_UID is null");
			return "compiere";
		}
		return user;
	}	//	getDB_UID

	/**
	 * 	Get database user password from Servlet Context or Web Context
	 * 	@param config servlet config
	 * 	@return db Password or compiere if not found
	 */
	public static String getDB_PWD (ServletConfig config)
	{
		//	Get DB User from Servlet init
		String pwd = config.getInitParameter("dbPWD");
		//	Get Client from Web Context
		if (pwd == null)
			pwd = config.getServletContext().getInitParameter("dbPWD");
		if (pwd == null)
		{
			s_log.error("getDB_PWD is null");
			return "compiere";
		}
		return pwd;
	}	//	getDB_PWD

	/*************************************************************************/

	/**
	 *  Get Base Directory entrry.
	 *  <br>
	 *  /compiere/
	 *  @param entry file entry or path
	 *  @return url to entry in base directory
	 */
	public static String getBaseDirectory (String entry)
	{
		StringBuffer sb = new StringBuffer (DIR_BASE);
		if (!entry.startsWith("/"))
			sb.append("/");
		sb.append(entry);
		return sb.toString();
	}   //  getBaseDirectory

	/**
	 *  Get Image Directory entry.
	 *  <br>
	 *  /compiere/images
	 *  @param entry file entry or path
	 *  @return url to entry in image directory
	 */
	public static String getImageDirectory(String entry)
	{
		StringBuffer sb = new StringBuffer (DIR_BASE);
		sb.append("/").append(DIR_IMAGE);
		if (!entry.startsWith("/"))
			sb.append("/");
		sb.append(entry);
		return sb.toString();
	}   //  getImageDirectory

	/**
	 *  Get Store Directory entry.
	 *  <br>
	 *  /compiere/store
	 *  @param entry file entry or path
	 *  @return url to entry in store directory
	 */
	public static String getStoreDirectory(String entry)
	{
		StringBuffer sb = new StringBuffer (DIR_BASE);
		sb.append("/").append(DIR_STORE);
		if (!entry.startsWith("/"))
			sb.append("/");
		sb.append(entry);
		return sb.toString();
	}   //  getStoreDirectory

	/**
	 *  Get Logo Path.
	 *  <p>
	 *  /compiere/LogoSmall.gif
	 *  @return url to logo
	 */
	public static String getLogoURL()
	{
		return getBaseDirectory(LOGO);
	}   //  getLogoPath

	/**
	 *  Get Logo Image HTML tag
	 *  @return Image
	 */
	public static img getLogo()
	{
		return new img(getLogoURL()).setAlign(AlignType.right).setAlt(Compiere.COPYRIGHT);
	}   //  getLogo

	/**
	 *  Get Stylesheet Path.
	 *  <p>
	 *  /compiere/standard.css
	 *  @return url of Stylesheet
	 */
	public static String getStylesheetURL()
	{
		return getBaseDirectory(STYLE_STD);
	}   //  getStylesheetURL

	/**
	 * 	Get Cell Content
	 *	@param content optional content
	 *	@return string content or non breaking space
	 */
	public static String getCellContent (Object content)
	{
		if (content == null)
			return NBSP;
		String str = content.toString();
		if (str.length() == 0)
			return NBSP;
		return str;
	}	//	getCellContent

	/**
	 * 	Get Cell Content
	 *	@param content optional content
	 *	@return string content
	 */
	public static String getCellContent (int content)
	{
		return String.valueOf(content);
	}	//	getCellContent

	/**************************************************************************
	 * 	Dump Servlet Config
	 * 	@param config config
	 */
	public static void dump (ServletConfig config)
	{
		System.out.println("ServletConfig " + config.getServletName());
		System.out.println("- Context=" + config.getServletContext());
		if (!Log.isTraceLevel(9))
			return;
		boolean first = true;
		Enumeration e = config.getInitParameterNames();
		while (e.hasMoreElements())
		{
			if (first)
				System.out.println("- InitParameter:");
			first = false;
			String key = (String)e.nextElement();
			Object value = config.getInitParameter(key);
			System.out.println("  - " + key + " = " + value);
		}
	}	//	dump (ServletConfig)

	/**
	 * 	Dump Session
	 * 	@param ctx servlet context
	 */
	public static void dump (ServletContext ctx)
	{
		System.out.println("ServletContext " + ctx.getServletContextName());
		System.out.println("- ServerInfo=" + ctx.getServerInfo());
		if (!Log.isTraceLevel(9))
			return;
		boolean first = true;
		Enumeration e = ctx.getInitParameterNames();
		while (e.hasMoreElements())
		{
			if (first)
				System.out.println("- InitParameter:");
			first = false;
			String key = (String)e.nextElement();
			Object value = ctx.getInitParameter(key);
			System.out.println("  - " + key + " = " + value);
		}
		first = true;
		e = ctx.getAttributeNames();
		while (e.hasMoreElements())
		{
			if (first)
				System.out.println("- Attributes:");
			first = false;
			String key = (String)e.nextElement();
			Object value = ctx.getAttribute(key);
			System.out.println("  - " + key + " = " + value);
		}
	}	//	dump

	/**
	 * 	Dump Session
	 * 	@param session session
	 */
	public static void dump (HttpSession session)
	{
		System.out.println("Session " + session.getId());
		System.out.println("- Created=" + new Timestamp(session.getCreationTime()));
		if (!Log.isTraceLevel(9))
			return;
		boolean first = true;
		Enumeration e = session.getAttributeNames();
		while (e.hasMoreElements())
		{
			if (first)
				System.out.println("- Attributes:");
			first = false;
			String key = (String)e.nextElement();
			Object value = session.getAttribute(key);
			System.out.println("  - " + key + " = " + value);
		}
	}	//	dump (session)

	/**
	 * 	Dump Request
	 * 	@param request request
	 */
	public static void dump (HttpServletRequest request)
	{
		System.out.println("Request " + request.getProtocol() + " " + request.getMethod());
		if (!Log.isTraceLevel(9))
			return;
		System.out.println("- Server="  + request.getServerName() + ", Port=" + request.getServerPort());
		System.out.println("- ContextPath=" + request.getContextPath()
			+ ", ServletPath=" + request.getServletPath()
			+ ", Query=" + request.getQueryString());
		System.out.println("- URI=" + request.getRequestURI() + ", URL=" + request.getRequestURL());
		System.out.println("- AuthType=" + request.getAuthType());
		System.out.println("- Secure=" + request.isSecure());
		System.out.println("- PathInfo=" + request.getPathInfo() + " - " + request.getPathTranslated());
		System.out.println("- UserPrincipal=" + request.getUserPrincipal());
		//
		boolean first = true;
		Enumeration e = request.getHeaderNames();
		/** Header Names */
		while (e.hasMoreElements())
		{
			if (first)
				System.out.println("- Header:");
			first = false;
			String key = (String)e.nextElement();
			Object value = request.getHeader(key);
			System.out.println("  - " + key + " = " + value);
		}
		/** **/
		first = true;
		/** Parameter	*/
		e = request.getParameterNames();
		while (e.hasMoreElements())
		{
			if (first)
				System.out.println("- Parameter:");
			first = false;
			String key = (String)e.nextElement();
			String value = request.getParameter(key);
			System.out.println("  - " + key + " = " + value);
		}
		first = true;
		/** Attributes	*/
		e = request.getAttributeNames();
		while (e.hasMoreElements())
		{
			if (first)
				System.out.println("- Attributes:");
			first = false;
			String key = (String)e.nextElement();
			Object value = request.getAttribute(key);
			System.out.println("  - " + key + " = " + value);
		}
		/** Cookies	*/
		Cookie[] ccc = request.getCookies();
		if (ccc != null)
		{
			for (int i = 0; i < ccc.length; i++)
			{
				if (i == 0)
					System.out.println("- Cookies:");
				System.out.println ("  - " + ccc[i].getName ()
					+ ", Domain=" + ccc[i].getDomain ()
					+ ", Path=" + ccc[i].getPath ()
					+ ", MaxAge=" + ccc[i].getMaxAge ());
			}
		}
		System.out.println("- Encoding=" + request.getCharacterEncoding());
		System.out.println("- Locale=" + request.getLocale());
		first = true;
		e = request.getLocales();
		while (e.hasMoreElements())
		{
			if (first)
				System.out.println("- Locales:");
			first = false;
			System.out.println("  - " + e.nextElement());
		}
	}	//	dump (Request)

	/*************************************************************************/

	/**
	 *  Add Footer (with diagnostics)
	 *  @param request request
	 *  @param response response
	 *  @param servlet servlet
	 *  @param body - Body to add footer
	 */
	public static void addFooter(HttpServletRequest request, HttpServletResponse response,
		HttpServlet servlet, body body)
	{
		body.addElement(new hr());
		body.addElement(new comment(" --- Footer Start --- "));
		//  Command Line
		p footer = new p();
		footer.addElement(org.compiere.Compiere.DATE_VERSION + ": ");
		footer.addElement(new a("javascript:diag_window();", "Window Info"));
		footer.addElement(" - ");
		footer.addElement(new a("javascript:diag_navigator();", "Browser Info"));
		footer.addElement(" - ");
		footer.addElement(new a("javascript:diag_request();", "Request Info"));
		footer.addElement(" - ");
		footer.addElement(new a("javascript:diag_document();", "Document Info"));
		footer.addElement(" - ");
		footer.addElement(new a("javascript:diag_form();", "Form Info"));
		footer.addElement(" - ");
		footer.addElement(new a("javascript:toggle('DEBUG');", "Servlet Info"));
		footer.addElement(" - ");
		footer.addElement(new a("javascript:diag_source();", "Show Source"));
		footer.addElement("\n");
		body.addElement(footer);

		//  Add ServletInfo
		body.addElement(new br());
		body.addElement(getServletInfo(request, response, servlet));
		body.addElement(new script("hide('DEBUG');"));
		body.addElement(new comment(" --- Footer End --- "));
	}   //  getFooter

	/**
	 *	Get Information and put it in a HTML table
	 *  @param request request
	 *  @param response response
	 *  @param servlet servlet
	 *  @return Table
	 */
	private static table getServletInfo (HttpServletRequest request,
		HttpServletResponse response, HttpServlet servlet)
	{
		table table = new table();
		table.setID("DEBUG");
		Enumeration e;

		tr space = new tr().addElement(new td().addElement("."));
		//	Request Info
		table.addElement(space);
		table.addElement(new tr().addElement(new td().addElement(new h3("Request Info")) ));
		table.addElement(new tr().addElement(new td().addElement("Method"))
									.addElement(new td().addElement(request.getMethod() )));
		table.addElement(new tr().addElement(new td().addElement("Protocol"))
									.addElement(new td().addElement(request.getProtocol() )));
		table.addElement(new tr().addElement(new td().addElement("URI"))
									.addElement(new td().addElement(request.getRequestURI() )));
		table.addElement(new tr().addElement(new td().addElement("Context Path"))
									.addElement(new td().addElement(request.getContextPath() )));
		table.addElement(new tr().addElement(new td().addElement("Servlet Path"))
									.addElement(new td().addElement(request.getServletPath() )));
		table.addElement(new tr().addElement(new td().addElement("Path Info"))
									.addElement(new td().addElement(request.getPathInfo() )));
		table.addElement(new tr().addElement(new td().addElement("Path Translated"))
									.addElement(new td().addElement(request.getPathTranslated() )));
		table.addElement(new tr().addElement(new td().addElement("Query String"))
									.addElement(new td().addElement(request.getQueryString() )));
		table.addElement(new tr().addElement(new td().addElement("Content Length"))
									.addElement(new td().addElement("" + request.getContentLength() )));
		table.addElement(new tr().addElement(new td().addElement("Content Type"))
									.addElement(new td().addElement(request.getContentType() )));
		table.addElement(new tr().addElement(new td().addElement("Character Encoding"))
									.addElement(new td().addElement(request.getCharacterEncoding() )));
		table.addElement(new tr().addElement(new td().addElement("Locale"))
									.addElement(new td().addElement(request.getLocale().toString() )));
		table.addElement(new tr().addElement(new td().addElement("Schema"))
									.addElement(new td().addElement(request.getScheme() )));
		table.addElement(new tr().addElement(new td().addElement("Server Name"))
									.addElement(new td().addElement(request.getServerName() )));
		table.addElement(new tr().addElement(new td().addElement("Server Port"))
									.addElement(new td().addElement("" + request.getServerPort() )));
		table.addElement(new tr().addElement(new td().addElement("Remote User"))
									.addElement(new td().addElement(request.getRemoteUser() )));
		table.addElement(new tr().addElement(new td().addElement("Remote Address"))
									.addElement(new td().addElement(request.getRemoteAddr() )));
		table.addElement(new tr().addElement(new td().addElement("Remote Host"))
									.addElement(new td().addElement(request.getRemoteHost() )));
		table.addElement(new tr().addElement(new td().addElement("Authorization Type"))
									.addElement(new td().addElement(request.getAuthType() )));
		table.addElement(new tr().addElement(new td().addElement("User Principal"))
									.addElement(new td().addElement(request.getUserPrincipal()==null ? "" : request.getUserPrincipal().toString())));
		table.addElement(new tr().addElement(new td().addElement("IsSecure"))
									.addElement(new td().addElement(request.isSecure() ? "true" : "false" )));

		//	Request Attributes
		table.addElement(space);
		table.addElement(new tr().addElement(new td().addElement(new h3("Request Attributes")) ));
		e = request.getAttributeNames();
		while (e.hasMoreElements())
		{
			String name = e.nextElement().toString();
			String attrib = request.getAttribute(name).toString();
			table.addElement(new tr().addElement(new td().addElement(name))
										.addElement(new td().addElement(attrib)));
		}

		//	Request Parameter
		table.addElement(space);
		table.addElement(new tr().addElement(new td().addElement(new h3("Req Parameters")) ));
		e = request.getParameterNames();
		while (e.hasMoreElements())
		{
			String name = (String)e.nextElement();
			String para = request.getParameter(name);
			table.addElement(new tr().addElement(new td().addElement(name))
										.addElement(new td().addElement(para)));
		}

		//	Request Header
		table.addElement(space);
		table.addElement(new tr().addElement(new td().addElement(new h3("Req Header")) ));
		e = request.getHeaderNames();
		while (e.hasMoreElements())
		{
			String name = (String)e.nextElement();
			if (!name.equals("Cockie"))
			{
				String hdr = request.getHeader(name);
				table.addElement(new tr().addElement(new td().addElement(name))
											.addElement(new td().addElement(hdr)));
			}
		}

		//  Request Cookies
		table.addElement(space);
		table.addElement(new tr().addElement(new td().addElement(new h3("Req Cookies")) ));
		Cookie[] cc = request.getCookies();
		if (cc != null)
		{
			for (int i = 0; i < cc.length; i++)
			{
				//	Name and Comment
				table.addElement(new tr().addElement(new td().addElement(cc[i].getName() ))
											.addElement(new td().addElement(cc[i].getValue()) ));
				table.addElement(new tr().addElement(new td().addElement(cc[i].getName()+": Comment" ))
											.addElement(new td().addElement(cc[i].getComment()) ));
				table.addElement(new tr().addElement(new td().addElement(cc[i].getName()+": Domain" ))
											.addElement(new td().addElement(cc[i].getDomain()) ));
				table.addElement(new tr().addElement(new td().addElement(cc[i].getName()+": Max Age" ))
											.addElement(new td().addElement(""+ cc[i].getMaxAge()) ));
				table.addElement(new tr().addElement(new td().addElement(cc[i].getName()+": Path" ))
											.addElement(new td().addElement(cc[i].getPath()) ));
				table.addElement(new tr().addElement(new td().addElement(cc[i].getName()+": Is Secure" ))
											.addElement(new td().addElement(cc[i].getSecure() ? "true" : "false") ));
				table.addElement(new tr().addElement(new td().addElement(cc[i].getName()+": Version" ))
											.addElement(new td().addElement("" + cc[i].getVersion()) ));
			}
		}	//	Cookies

		//  Request Session Info
		table.addElement(space);
		table.addElement(new tr().addElement(new td().addElement(new h3("Req Session")) ));
		HttpSession session = request.getSession(true);
		table.addElement(new tr().addElement(new td().addElement("Session ID"))
									.addElement(new td().addElement(session.getId() )));
		Timestamp ts = new Timestamp(session.getCreationTime());
		table.addElement(new tr().addElement(new td().addElement("Created"))
									.addElement(new td().addElement(ts.toString() )));
		ts = new Timestamp(session.getLastAccessedTime());
		table.addElement(new tr().addElement(new td().addElement("Accessed"))
									.addElement(new td().addElement(ts.toString() )));
		table.addElement(new tr().addElement(new td().addElement("Request Session ID"))
									.addElement(new td().addElement(request.getRequestedSessionId() )));
		table.addElement(new tr().addElement(new td().addElement(".. via Cookie"))
									.addElement(new td().addElement("" + request.isRequestedSessionIdFromCookie() )));
		table.addElement(new tr().addElement(new td().addElement(".. via URL"))
									.addElement(new td().addElement("" + request.isRequestedSessionIdFromURL() )));
		table.addElement(new tr().addElement(new td().addElement("Valid"))
									.addElement(new td().addElement("" + request.isRequestedSessionIdValid() )));

		//	Request Session Attributes
		table.addElement(space);
		table.addElement(new tr().addElement(new td().addElement(new h3("Session Attributes")) ));
		e = session.getAttributeNames();
		while (e.hasMoreElements())
		{
			String name = (String)e.nextElement();
			String attrib = session.getAttribute(name).toString();
			table.addElement(new tr().addElement(new td().addElement(name))
										.addElement(new td().addElement(attrib)));
		}

		//	Response Info
		table.addElement(space);
		table.addElement(new tr().addElement(new td().addElement(new h3("Response")) ));
		table.addElement(new tr().addElement(new td().addElement("Buffer Size"))
									.addElement(new td().addElement(String.valueOf(response.getBufferSize()) )));
		table.addElement(new tr().addElement(new td().addElement("Character Encoding"))
									.addElement(new td().addElement(response.getCharacterEncoding() )));
		table.addElement(new tr().addElement(new td().addElement("Locale"))
									.addElement(new td().addElement(response.getLocale()==null ? "null" : response.getLocale().toString())));

		//  Servlet
		table.addElement(space);
		table.addElement(new tr().addElement(new td().addElement(new h3("Servlet")) ));
		table.addElement(new tr().addElement(new td().addElement("Name"))
										.addElement(new td().addElement(servlet.getServletName())));
		table.addElement(new tr().addElement(new td().addElement("Info"))
										.addElement(new td().addElement(servlet.getServletInfo())));

		//  Servlet Init
		table.addElement(space);
		table.addElement(new tr().addElement(new td().addElement(new h3("Servlet Init Parameter")) ));
		e = servlet.getInitParameterNames();
		//  same as:  servlet.getServletConfig().getInitParameterNames();
		while (e.hasMoreElements())
		{
			String name = (String)e.nextElement();
			String para = servlet.getInitParameter(name);
			table.addElement(new tr().addElement(new td().addElement(name))
										.addElement(new td().addElement(para)));
		}

		//  Servlet Context
		table.addElement(space);
		table.addElement(new tr().addElement(new td().addElement(new h3("Servlet Context")) ));
		ServletContext servCtx = servlet.getServletContext();
		e = servCtx.getAttributeNames();
		while (e.hasMoreElements())
		{
			String name = (String)e.nextElement();
			String attrib = servCtx.getAttribute(name).toString();
			table.addElement(new tr().addElement(new td().addElement(name))
										.addElement(new td().addElement(attrib)));
		}

		//  Servlet Context
		table.addElement(space);
		table.addElement(new tr().addElement(new td().addElement(new h3("Servlet Context Init Parameter")) ));
		e = servCtx.getInitParameterNames();
		while (e.hasMoreElements())
		{
			String name = (String)e.nextElement();
			String attrib = servCtx.getInitParameter(name).toString();
			table.addElement(new tr().addElement(new td().addElement(name))
										.addElement(new td().addElement(attrib)));
		}

		/*	*/
		return table;
	}	//	getServletInfo

}   //  WEnv
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.util;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;

import org.compiere.Compiere;
import org.compiere.model.*;
import org.compiere.wf.*;

/**
 *  Web Environment and debugging
 *
 *  @author Jorg Janke
 *  @version $Id: WebEnv.java,v 1.2 2004/09/09 14:14:33 jjanke Exp $
 */
public class WebEnv
{
	/** Add HTML Debug Info                     */
	public static boolean DEBUG                 = true;
	/**	Logging									*/
	private static Logger			s_log = Logger.getCLogger(WebEnv.class);

	/**
	 *  Base Directory links <b>http://localhost:8080/compiere</b>
	 *  to the physical <i>%COMPIERE_HOME%/tomcat/webroot/compiere</i> directory
	 */
	public static final String   	DIR_BASE    = "/compiere";      //  /compiere
	/** Image Sub-Directory under BASE          */
	private static final String     DIR_IMAGE   = "images";         //  /compiere/images
	/** Stylesheet Name                         */
	private static final String     STYLE_STD   = "standard.css";   //  /compiere/standard.css
	/** Small Logo                              */
	private static final String     LOGO        = "LogoSmall.gif";  //  /compiere/LogoSmall.gif
	/** Store Sub-Directory under BASE          */
	private static final String     DIR_STORE   = "store";          //  /compiere/store

	/**  Frame name for Commands   */
	public static final String      TARGET_CMD  = "WCmd";
	/**  Frame name for Menu       */
	public static final String      TARGET_MENU = "WMenu";
	/**  Frame name for Apps Window */
	public static final String      TARGET_WINDOW = "WWindow";

	/** Character Set (iso-8859-1)              */
	public static final String      CHARACTERSET = "iso-8859-8";     //  Default: UNKNOWN
	/** Cookie Name                             */
	public static final String      COOKIE_INFO = "CompiereInfo";

	/** Timeout - 15 Minutes                    */
	public static final int         TIMEOUT     = 15*60;

	/** Session Attribute - Context             */
	public static final String      SA_CONTEXT  = "SA_Context";
	/** Session Attribute - Window Status              */
	public static final String      SA_WINDOW   = "SA_Window";
	/** Session Attribute - Login Info          */
	public static final String      SA_LOGININFO = "SA_LoginInfo";
	/** Session Locale                          */
	public static final String      SA_LANGUAGE  = "SA_Language";

	/** Initialization OK?                      */
	private static boolean          s_initOK    = false;
	/** Not Braking Space						*/
	public static String			NBSP = "&nbsp;";

	/**
	 *  Init Web Environment.
	 *  <p>
	 *  To be called from every Servlet in the init method
	 *  or any other Web resource to make sure that the
	 *  environment is properly set.
	 *  @param config config
	 *  @return false if initialization problems
	 */
	public static boolean initWeb (ServletConfig config)
	{
		if (s_initOK)
		{
			s_log.info("initWeb " + config.getServletName());
			return true;
		}
		s_log.info("initWeb - Initial Call - " + config.getServletName());

		//  Load Environment Variables
		s_log.info("Web Context Init Parameter for " + config.getServletContext().getServletContextName());
		Enumeration en = config.getServletContext().getInitParameterNames();
		while (en.hasMoreElements())
		{
			String name = en.nextElement().toString();
			String value = config.getServletContext().getInitParameter(name);
			s_log.info(" - " + name + "=" + value);
			System.setProperty(name, value);
		}

		s_log.info("Servlet Init Parameter for " + config.getServletName());
		en = config.getInitParameterNames();
		while (en.hasMoreElements())
		{
			String name = en.nextElement().toString();
			String value = config.getInitParameter(name);
			s_log.info(" - " + name + "=" + value);
			System.setProperty(name, value);
		}

		try
		{
			s_initOK = Compiere.startupServer ();
			PO.setDocWorkflowMgr (DocWorkflowManager.get());
		}
		catch (Exception ex)
		{
			s_log.error("initWeb", ex);
		}
		if (!s_initOK)
			return false;

		s_log.info("initWeb complete");
		return s_initOK;
	}   //  initWeb


	/**
	 * 	Get AD_Client_ID from Servlet Context or Web Context
	 * 	@param config servlet config
	 * 	@return AD_Client_ID if found or 0
	 */
	public static int getAD_Client_ID (ServletConfig config)
	{
		//	Get Client from Servlet init
		String oo = config.getInitParameter("AD_Client_ID");
		//	Get Client from Web Context
		if (oo == null)
			oo = config.getServletContext().getInitParameter("AD_Client_ID");
		if (oo == null)
		{
			s_log.error("getAD_Client_ID is null");
			return 0;
		}
		int AD_Client_ID = 0;
		try
		{
			AD_Client_ID = Integer.parseInt(oo);
		}
		catch (NumberFormatException ex)
		{
			s_log.error("getAD_Client_ID - " + oo, ex);
		}
		return AD_Client_ID;
	}	//	getAD_Client_ID

	/**
	 * 	Get database user from Servlet Context or Web Context
	 * 	@param servlet servlet config
	 * 	@return db User ID or compiere if not found
	 */
	public static Connection getConnection (HttpServlet servlet)
	{
		//	Get Info from Servlet Context
		String user = servlet.getInitParameter("dbUID");
		String pwd = servlet.getInitParameter("dbPWD");
		//	Get Client Web Context
		if (user == null)
			user = servlet.getServletContext().getInitParameter("dbUID");
		if (pwd == null)
			pwd = servlet.getServletContext().getInitParameter("dbPWD");
		//	Defaults
		if (user == null)
			user = "compiere";
		if (user == null)
			user = "compiere";
/**		//
		try
		{
			Context context = new InitialContext();
			DataSource ds = (DataSource)context.lookup("java:OracleDS");
			log.info ("OracleDS=" + ds.toString());
			Connection con = ds.getConnection(dbUID, dbPWD);
			log.info("Connection AutoCommit=" + con.getAutoCommit());
			//
			BPartnerHome bpHome = (BPartnerHome)context.lookup(BPartnerHome.JNDI_NAME);
			BPartner bp = bpHome.create();
			log.info("BPartner=" + bp.process2());
			bp.remove();
		}
		catch (Exception ex)
		{
			throw new ServletException (ex);
		}
**/
		return null;

	}	//	getDB_UID

	/**
	 * 	Get database user from Servlet Context or Web Context
	 * 	@param config servlet config
	 * 	@return db User ID or compiere if not found
	 */
	public static String getDB_UID (ServletConfig config)
	{
		//	Get DB User from Servlet init
		String user = config.getInitParameter("dbUID");
		//	Get Client from Web Context
		if (user == null)
			user = config.getServletContext().getInitParameter("dbUID");
		if (user == null)
		{
			s_log.error("getDB_UID is null");
			return "compiere";
		}
		return user;
	}	//	getDB_UID

	/**
	 * 	Get database user password from Servlet Context or Web Context
	 * 	@param config servlet config
	 * 	@return db Password or compiere if not found
	 */
	public static String getDB_PWD (ServletConfig config)
	{
		//	Get DB User from Servlet init
		String pwd = config.getInitParameter("dbPWD");
		//	Get Client from Web Context
		if (pwd == null)
			pwd = config.getServletContext().getInitParameter("dbPWD");
		if (pwd == null)
		{
			s_log.error("getDB_PWD is null");
			return "compiere";
		}
		return pwd;
	}	//	getDB_PWD

	/*************************************************************************/

	/**
	 *  Get Base Directory entrry.
	 *  <br>
	 *  /compiere/
	 *  @param entry file entry or path
	 *  @return url to entry in base directory
	 */
	public static String getBaseDirectory (String entry)
	{
		StringBuffer sb = new StringBuffer (DIR_BASE);
		if (!entry.startsWith("/"))
			sb.append("/");
		sb.append(entry);
		return sb.toString();
	}   //  getBaseDirectory

	/**
	 *  Get Image Directory entry.
	 *  <br>
	 *  /compiere/images
	 *  @param entry file entry or path
	 *  @return url to entry in image directory
	 */
	public static String getImageDirectory(String entry)
	{
		StringBuffer sb = new StringBuffer (DIR_BASE);
		sb.append("/").append(DIR_IMAGE);
		if (!entry.startsWith("/"))
			sb.append("/");
		sb.append(entry);
		return sb.toString();
	}   //  getImageDirectory

	/**
	 *  Get Store Directory entry.
	 *  <br>
	 *  /compiere/store
	 *  @param entry file entry or path
	 *  @return url to entry in store directory
	 */
	public static String getStoreDirectory(String entry)
	{
		StringBuffer sb = new StringBuffer (DIR_BASE);
		sb.append("/").append(DIR_STORE);
		if (!entry.startsWith("/"))
			sb.append("/");
		sb.append(entry);
		return sb.toString();
	}   //  getStoreDirectory

	/**
	 *  Get Logo Path.
	 *  <p>
	 *  /compiere/LogoSmall.gif
	 *  @return url to logo
	 */
	public static String getLogoURL()
	{
		return getBaseDirectory(LOGO);
	}   //  getLogoPath

	/**
	 *  Get Logo Image HTML tag
	 *  @return Image
	 */
	public static img getLogo()
	{
		return new img(getLogoURL()).setAlign(AlignType.right).setAlt(Compiere.COPYRIGHT);
	}   //  getLogo

	/**
	 *  Get Stylesheet Path.
	 *  <p>
	 *  /compiere/standard.css
	 *  @return url of Stylesheet
	 */
	public static String getStylesheetURL()
	{
		return getBaseDirectory(STYLE_STD);
	}   //  getStylesheetURL

	/**
	 * 	Get Cell Content
	 *	@param content optional content
	 *	@return string content or non breaking space
	 */
	public static String getCellContent (Object content)
	{
		if (content == null)
			return NBSP;
		String str = content.toString();
		if (str.length() == 0)
			return NBSP;
		return str;
	}	//	getCellContent

	/**
	 * 	Get Cell Content
	 *	@param content optional content
	 *	@return string content
	 */
	public static String getCellContent (int content)
	{
		return String.valueOf(content);
	}	//	getCellContent

	/**************************************************************************
	 * 	Dump Servlet Config
	 * 	@param config config
	 */
	public static void dump (ServletConfig config)
	{
		System.out.println("ServletConfig " + config.getServletName());
		System.out.println("- Context=" + config.getServletContext());
		if (!Log.isTraceLevel(9))
			return;
		boolean first = true;
		Enumeration e = config.getInitParameterNames();
		while (e.hasMoreElements())
		{
			if (first)
				System.out.println("- InitParameter:");
			first = false;
			String key = (String)e.nextElement();
			Object value = config.getInitParameter(key);
			System.out.println("  - " + key + " = " + value);
		}
	}	//	dump (ServletConfig)

	/**
	 * 	Dump Session
	 * 	@param ctx servlet context
	 */
	public static void dump (ServletContext ctx)
	{
		System.out.println("ServletContext " + ctx.getServletContextName());
		System.out.println("- ServerInfo=" + ctx.getServerInfo());
		if (!Log.isTraceLevel(9))
			return;
		boolean first = true;
		Enumeration e = ctx.getInitParameterNames();
		while (e.hasMoreElements())
		{
			if (first)
				System.out.println("- InitParameter:");
			first = false;
			String key = (String)e.nextElement();
			Object value = ctx.getInitParameter(key);
			System.out.println("  - " + key + " = " + value);
		}
		first = true;
		e = ctx.getAttributeNames();
		while (e.hasMoreElements())
		{
			if (first)
				System.out.println("- Attributes:");
			first = false;
			String key = (String)e.nextElement();
			Object value = ctx.getAttribute(key);
			System.out.println("  - " + key + " = " + value);
		}
	}	//	dump

	/**
	 * 	Dump Session
	 * 	@param session session
	 */
	public static void dump (HttpSession session)
	{
		System.out.println("Session " + session.getId());
		System.out.println("- Created=" + new Timestamp(session.getCreationTime()));
		if (!Log.isTraceLevel(9))
			return;
		boolean first = true;
		Enumeration e = session.getAttributeNames();
		while (e.hasMoreElements())
		{
			if (first)
				System.out.println("- Attributes:");
			first = false;
			String key = (String)e.nextElement();
			Object value = session.getAttribute(key);
			System.out.println("  - " + key + " = " + value);
		}
	}	//	dump (session)

	/**
	 * 	Dump Request
	 * 	@param request request
	 */
	public static void dump (HttpServletRequest request)
	{
		System.out.println("Request " + request.getProtocol() + " " + request.getMethod());
		if (!Log.isTraceLevel(9))
			return;
		System.out.println("- Server="  + request.getServerName() + ", Port=" + request.getServerPort());
		System.out.println("- ContextPath=" + request.getContextPath()
			+ ", ServletPath=" + request.getServletPath()
			+ ", Query=" + request.getQueryString());
		System.out.println("- URI=" + request.getRequestURI() + ", URL=" + request.getRequestURL());
		System.out.println("- AuthType=" + request.getAuthType());
		System.out.println("- Secure=" + request.isSecure());
		System.out.println("- PathInfo=" + request.getPathInfo() + " - " + request.getPathTranslated());
		System.out.println("- UserPrincipal=" + request.getUserPrincipal());
		//
		boolean first = true;
		Enumeration e = request.getHeaderNames();
		/** Header Names */
		while (e.hasMoreElements())
		{
			if (first)
				System.out.println("- Header:");
			first = false;
			String key = (String)e.nextElement();
			Object value = request.getHeader(key);
			System.out.println("  - " + key + " = " + value);
		}
		/** **/
		first = true;
		/** Parameter	*/
		e = request.getParameterNames();
		while (e.hasMoreElements())
		{
			if (first)
				System.out.println("- Parameter:");
			first = false;
			String key = (String)e.nextElement();
			String value = request.getParameter(key);
			System.out.println("  - " + key + " = " + value);
		}
		first = true;
		/** Attributes	*/
		e = request.getAttributeNames();
		while (e.hasMoreElements())
		{
			if (first)
				System.out.println("- Attributes:");
			first = false;
			String key = (String)e.nextElement();
			Object value = request.getAttribute(key);
			System.out.println("  - " + key + " = " + value);
		}
		/** Cookies	*/
		Cookie[] ccc = request.getCookies();
		if (ccc != null)
		{
			for (int i = 0; i < ccc.length; i++)
			{
				if (i == 0)
					System.out.println("- Cookies:");
				System.out.println ("  - " + ccc[i].getName ()
					+ ", Domain=" + ccc[i].getDomain ()
					+ ", Path=" + ccc[i].getPath ()
					+ ", MaxAge=" + ccc[i].getMaxAge ());
			}
		}
		System.out.println("- Encoding=" + request.getCharacterEncoding());
		System.out.println("- Locale=" + request.getLocale());
		first = true;
		e = request.getLocales();
		while (e.hasMoreElements())
		{
			if (first)
				System.out.println("- Locales:");
			first = false;
			System.out.println("  - " + e.nextElement());
		}
	}	//	dump (Request)

	/*************************************************************************/

	/**
	 *  Add Footer (with diagnostics)
	 *  @param request request
	 *  @param response response
	 *  @param servlet servlet
	 *  @param body - Body to add footer
	 */
	public static void addFooter(HttpServletRequest request, HttpServletResponse response,
		HttpServlet servlet, body body)
	{
		body.addElement(new hr());
		body.addElement(new comment(" --- Footer Start --- "));
		//  Command Line
		p footer = new p();
		footer.addElement(org.compiere.Compiere.DATE_VERSION + ": ");
		footer.addElement(new a("javascript:diag_window();", "Window Info"));
		footer.addElement(" - ");
		footer.addElement(new a("javascript:diag_navigator();", "Browser Info"));
		footer.addElement(" - ");
		footer.addElement(new a("javascript:diag_request();", "Request Info"));
		footer.addElement(" - ");
		footer.addElement(new a("javascript:diag_document();", "Document Info"));
		footer.addElement(" - ");
		footer.addElement(new a("javascript:diag_form();", "Form Info"));
		footer.addElement(" - ");
		footer.addElement(new a("javascript:toggle('DEBUG');", "Servlet Info"));
		footer.addElement(" - ");
		footer.addElement(new a("javascript:diag_source();", "Show Source"));
		footer.addElement("\n");
		body.addElement(footer);

		//  Add ServletInfo
		body.addElement(new br());
		body.addElement(getServletInfo(request, response, servlet));
		body.addElement(new script("hide('DEBUG');"));
		body.addElement(new comment(" --- Footer End --- "));
	}   //  getFooter

	/**
	 *	Get Information and put it in a HTML table
	 *  @param request request
	 *  @param response response
	 *  @param servlet servlet
	 *  @return Table
	 */
	private static table getServletInfo (HttpServletRequest request,
		HttpServletResponse response, HttpServlet servlet)
	{
		table table = new table();
		table.setID("DEBUG");
		Enumeration e;

		tr space = new tr().addElement(new td().addElement("."));
		//	Request Info
		table.addElement(space);
		table.addElement(new tr().addElement(new td().addElement(new h3("Request Info")) ));
		table.addElement(new tr().addElement(new td().addElement("Method"))
									.addElement(new td().addElement(request.getMethod() )));
		table.addElement(new tr().addElement(new td().addElement("Protocol"))
									.addElement(new td().addElement(request.getProtocol() )));
		table.addElement(new tr().addElement(new td().addElement("URI"))
									.addElement(new td().addElement(request.getRequestURI() )));
		table.addElement(new tr().addElement(new td().addElement("Context Path"))
									.addElement(new td().addElement(request.getContextPath() )));
		table.addElement(new tr().addElement(new td().addElement("Servlet Path"))
									.addElement(new td().addElement(request.getServletPath() )));
		table.addElement(new tr().addElement(new td().addElement("Path Info"))
									.addElement(new td().addElement(request.getPathInfo() )));
		table.addElement(new tr().addElement(new td().addElement("Path Translated"))
									.addElement(new td().addElement(request.getPathTranslated() )));
		table.addElement(new tr().addElement(new td().addElement("Query String"))
									.addElement(new td().addElement(request.getQueryString() )));
		table.addElement(new tr().addElement(new td().addElement("Content Length"))
									.addElement(new td().addElement("" + request.getContentLength() )));
		table.addElement(new tr().addElement(new td().addElement("Content Type"))
									.addElement(new td().addElement(request.getContentType() )));
		table.addElement(new tr().addElement(new td().addElement("Character Encoding"))
									.addElement(new td().addElement(request.getCharacterEncoding() )));
		table.addElement(new tr().addElement(new td().addElement("Locale"))
									.addElement(new td().addElement(request.getLocale().toString() )));
		table.addElement(new tr().addElement(new td().addElement("Schema"))
									.addElement(new td().addElement(request.getScheme() )));
		table.addElement(new tr().addElement(new td().addElement("Server Name"))
									.addElement(new td().addElement(request.getServerName() )));
		table.addElement(new tr().addElement(new td().addElement("Server Port"))
									.addElement(new td().addElement("" + request.getServerPort() )));
		table.addElement(new tr().addElement(new td().addElement("Remote User"))
									.addElement(new td().addElement(request.getRemoteUser() )));
		table.addElement(new tr().addElement(new td().addElement("Remote Address"))
									.addElement(new td().addElement(request.getRemoteAddr() )));
		table.addElement(new tr().addElement(new td().addElement("Remote Host"))
									.addElement(new td().addElement(request.getRemoteHost() )));
		table.addElement(new tr().addElement(new td().addElement("Authorization Type"))
									.addElement(new td().addElement(request.getAuthType() )));
		table.addElement(new tr().addElement(new td().addElement("User Principal"))
									.addElement(new td().addElement(request.getUserPrincipal()==null ? "" : request.getUserPrincipal().toString())));
		table.addElement(new tr().addElement(new td().addElement("IsSecure"))
									.addElement(new td().addElement(request.isSecure() ? "true" : "false" )));

		//	Request Attributes
		table.addElement(space);
		table.addElement(new tr().addElement(new td().addElement(new h3("Request Attributes")) ));
		e = request.getAttributeNames();
		while (e.hasMoreElements())
		{
			String name = e.nextElement().toString();
			String attrib = request.getAttribute(name).toString();
			table.addElement(new tr().addElement(new td().addElement(name))
										.addElement(new td().addElement(attrib)));
		}

		//	Request Parameter
		table.addElement(space);
		table.addElement(new tr().addElement(new td().addElement(new h3("Req Parameters")) ));
		e = request.getParameterNames();
		while (e.hasMoreElements())
		{
			String name = (String)e.nextElement();
			String para = request.getParameter(name);
			table.addElement(new tr().addElement(new td().addElement(name))
										.addElement(new td().addElement(para)));
		}

		//	Request Header
		table.addElement(space);
		table.addElement(new tr().addElement(new td().addElement(new h3("Req Header")) ));
		e = request.getHeaderNames();
		while (e.hasMoreElements())
		{
			String name = (String)e.nextElement();
			if (!name.equals("Cockie"))
			{
				String hdr = request.getHeader(name);
				table.addElement(new tr().addElement(new td().addElement(name))
											.addElement(new td().addElement(hdr)));
			}
		}

		//  Request Cookies
		table.addElement(space);
		table.addElement(new tr().addElement(new td().addElement(new h3("Req Cookies")) ));
		Cookie[] cc = request.getCookies();
		if (cc != null)
		{
			for (int i = 0; i < cc.length; i++)
			{
				//	Name and Comment
				table.addElement(new tr().addElement(new td().addElement(cc[i].getName() ))
											.addElement(new td().addElement(cc[i].getValue()) ));
				table.addElement(new tr().addElement(new td().addElement(cc[i].getName()+": Comment" ))
											.addElement(new td().addElement(cc[i].getComment()) ));
				table.addElement(new tr().addElement(new td().addElement(cc[i].getName()+": Domain" ))
											.addElement(new td().addElement(cc[i].getDomain()) ));
				table.addElement(new tr().addElement(new td().addElement(cc[i].getName()+": Max Age" ))
											.addElement(new td().addElement(""+ cc[i].getMaxAge()) ));
				table.addElement(new tr().addElement(new td().addElement(cc[i].getName()+": Path" ))
											.addElement(new td().addElement(cc[i].getPath()) ));
				table.addElement(new tr().addElement(new td().addElement(cc[i].getName()+": Is Secure" ))
											.addElement(new td().addElement(cc[i].getSecure() ? "true" : "false") ));
				table.addElement(new tr().addElement(new td().addElement(cc[i].getName()+": Version" ))
											.addElement(new td().addElement("" + cc[i].getVersion()) ));
			}
		}	//	Cookies

		//  Request Session Info
		table.addElement(space);
		table.addElement(new tr().addElement(new td().addElement(new h3("Req Session")) ));
		HttpSession session = request.getSession(true);
		table.addElement(new tr().addElement(new td().addElement("Session ID"))
									.addElement(new td().addElement(session.getId() )));
		Timestamp ts = new Timestamp(session.getCreationTime());
		table.addElement(new tr().addElement(new td().addElement("Created"))
									.addElement(new td().addElement(ts.toString() )));
		ts = new Timestamp(session.getLastAccessedTime());
		table.addElement(new tr().addElement(new td().addElement("Accessed"))
									.addElement(new td().addElement(ts.toString() )));
		table.addElement(new tr().addElement(new td().addElement("Request Session ID"))
									.addElement(new td().addElement(request.getRequestedSessionId() )));
		table.addElement(new tr().addElement(new td().addElement(".. via Cookie"))
									.addElement(new td().addElement("" + request.isRequestedSessionIdFromCookie() )));
		table.addElement(new tr().addElement(new td().addElement(".. via URL"))
									.addElement(new td().addElement("" + request.isRequestedSessionIdFromURL() )));
		table.addElement(new tr().addElement(new td().addElement("Valid"))
									.addElement(new td().addElement("" + request.isRequestedSessionIdValid() )));

		//	Request Session Attributes
		table.addElement(space);
		table.addElement(new tr().addElement(new td().addElement(new h3("Session Attributes")) ));
		e = session.getAttributeNames();
		while (e.hasMoreElements())
		{
			String name = (String)e.nextElement();
			String attrib = session.getAttribute(name).toString();
			table.addElement(new tr().addElement(new td().addElement(name))
										.addElement(new td().addElement(attrib)));
		}

		//	Response Info
		table.addElement(space);
		table.addElement(new tr().addElement(new td().addElement(new h3("Response")) ));
		table.addElement(new tr().addElement(new td().addElement("Buffer Size"))
									.addElement(new td().addElement(String.valueOf(response.getBufferSize()) )));
		table.addElement(new tr().addElement(new td().addElement("Character Encoding"))
									.addElement(new td().addElement(response.getCharacterEncoding() )));
		table.addElement(new tr().addElement(new td().addElement("Locale"))
									.addElement(new td().addElement(response.getLocale()==null ? "null" : response.getLocale().toString())));

		//  Servlet
		table.addElement(space);
		table.addElement(new tr().addElement(new td().addElement(new h3("Servlet")) ));
		table.addElement(new tr().addElement(new td().addElement("Name"))
										.addElement(new td().addElement(servlet.getServletName())));
		table.addElement(new tr().addElement(new td().addElement("Info"))
										.addElement(new td().addElement(servlet.getServletInfo())));

		//  Servlet Init
		table.addElement(space);
		table.addElement(new tr().addElement(new td().addElement(new h3("Servlet Init Parameter")) ));
		e = servlet.getInitParameterNames();
		//  same as:  servlet.getServletConfig().getInitParameterNames();
		while (e.hasMoreElements())
		{
			String name = (String)e.nextElement();
			String para = servlet.getInitParameter(name);
			table.addElement(new tr().addElement(new td().addElement(name))
										.addElement(new td().addElement(para)));
		}

		//  Servlet Context
		table.addElement(space);
		table.addElement(new tr().addElement(new td().addElement(new h3("Servlet Context")) ));
		ServletContext servCtx = servlet.getServletContext();
		e = servCtx.getAttributeNames();
		while (e.hasMoreElements())
		{
			String name = (String)e.nextElement();
			String attrib = servCtx.getAttribute(name).toString();
			table.addElement(new tr().addElement(new td().addElement(name))
										.addElement(new td().addElement(attrib)));
		}

		//  Servlet Context
		table.addElement(space);
		table.addElement(new tr().addElement(new td().addElement(new h3("Servlet Context Init Parameter")) ));
		e = servCtx.getInitParameterNames();
		while (e.hasMoreElements())
		{
			String name = (String)e.nextElement();
			String attrib = servCtx.getInitParameter(name).toString();
			table.addElement(new tr().addElement(new td().addElement(name))
										.addElement(new td().addElement(attrib)));
		}

		/*	*/
		return table;
	}	//	getServletInfo

}   //  WEnv
