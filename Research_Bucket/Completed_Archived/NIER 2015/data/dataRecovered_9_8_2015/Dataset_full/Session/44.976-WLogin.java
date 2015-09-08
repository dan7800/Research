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
package org.compiere.www;

import javax.servlet.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import java.text.*;
import java.security.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;

import org.compiere.util.*;

/**
 *  Web Login Page.
 *  <p>
 *  Page request:
 *  <pre>
 *  - Check database connection
 *  - LoginInfo from request?
 *      - Yes: DoLogin success ?
 *          - Yes: return (second) preferences page
 *          - No: return (first) user/password page
 *      - No: User Principal ?
 *          - Yes: DoLogin success ?
 *              - Yes: return (second) preferences page
 *              - No: return (first) user/password page
 *          - No: return (first) user/password page
 *  </pre>
 *
 *  @author Jorg Janke
 *  @version  $Id: WLogin.java,v 1.5 2004/03/06 07:16:14 jjanke Exp $
 */
public class WLogin extends HttpServlet
{
	/**
	 *	Initialize
	 *  @param config confif
	 *  @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		if (!WEnv.initWeb(config))
			throw new ServletException("WLogin.init");
	}	//	init

	/**
	 * Get Servlet information
	 * @return servlet info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Login";
	}	//	getServletInfo

	/**
	 *	Clean up
	 */
	public void destroy()
	{
		Log.trace(Log.l1_User, "WLogin.destroy");
		super.destroy();
	}	//	destroy


	/**
	 *	Process the HTTP Get request - forward to Post
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		Log.trace(Log.l1_User, "WLogin.doGet");
		doPost (request, response);
	}	//	doGet


	/**
	 *	Process the HTTP Post request.
	 *  <pre>
	 *  - Optionally create Session
	 *  - Check database connection
	 *  - LoginInfo from request?
	 *      - Yes: DoLogin success ?
	 *          - Yes: return (second) preferences page
	 *          - No: return (first) user/password page
	 *      - No: User Principal ?
	 *          - Yes: DoLogin success ?
	 *              - Yes: return (second) preferences page
	 *              - No: return (first) user/password page
	 *          - No: return (first) user/password page
	 *  </pre>
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		Log.trace(Log.l1_User, "WLogin.doPost");
		//  Create New Session
		HttpSession sess = request.getSession(true);
		sess.setMaxInactiveInterval(WEnv.TIMEOUT);

		//  Get/set Context
		Properties ctx = (Properties)sess.getAttribute(WEnv.SA_CONTEXT);
		if (ctx == null)
			ctx = new Properties();
		sess.setAttribute(WEnv.SA_CONTEXT, ctx);

		//  Get Cookie
		Properties cProp = WUtil.getCookieProprties(request);
		//  Language
		checkLanguage (request, sess, ctx, cProp);
		//  Page
		WDoc doc = null;

		//  Check DB connection
		if (!DB.isConnected())
		{
			String msg = Msg.getMsg(ctx, "WLoginNoDB");
			doc = WDoc.create (msg);
			if (msg.equals("WLoginNoDB"))
				msg = "No Database Connection";
			doc.getBody().addElement(new h1(msg));
		}

		//  Login Info from request?
		else
		{
			//  Get Parameters:     UserName/Password
			String usr = request.getParameter(P_USERNAME);
			String pwd = request.getParameter(P_PASSWORD);
			//  Get Principle
			Principal userPr = request.getUserPrincipal();
			Log.trace(Log.l4_Data, "Principal=" + userPr + "; User=" + usr);

			//  Login info not from request and not pre-authorized
			if (userPr == null && (usr == null || pwd == null))
				doc = createFirstPage (cProp, request, "");
			//  Login info from request or authorized
			else
			{
				KeyNamePair[] roles = null;
				//  Pre-authorized
				if (userPr != null)
				{
					roles = DB.login(ctx, userPr);
					usr = userPr.getName();
				}
				else
					roles = DB.login(ctx, usr, pwd);
				//
				if (roles == null)
					doc = createFirstPage(cProp, request, Msg.getMsg(ctx, "UserPwdError"));
				else
					doc = createSecondPage(cProp, request, WUtil.convertToOption(roles, null), "");
				//  Can we save Cookie ?
				if (request.getParameter(P_STORE) == null)
				{
					cProp.clear();                          //  erase all
				}
				else    //  Save Cookie Parameter
				{
					cProp.setProperty(P_USERNAME, usr);
					cProp.setProperty(P_STORE, "Y");
					cProp.setProperty(P_PASSWORD, pwd);     //  For test only
				}
			}
		}

		//
		WUtil.createResponse (request, response, this, cProp, doc, true);
	}	//	doPost

	//  Variable Names
	public static final String      P_USERNAME      = "User";
	private static final String     P_PASSWORD      = "Password";
	protected static final String   P_LANGUAGE      = Env.LANG;
	private static final String     P_SUBMIT        = "Submit";
	//  WMenu picks it up
	protected static final String   P_ROLE          = "AD_Role_ID";
	protected static final String   P_CLIENT        = "AD_Client_ID";
	protected static final String   P_ORG           = "AD_Org_ID";
	protected static final String   P_DATE          = "Date";
	protected static final String   P_WAREHOUSE     = "M_Warehouse_ID";
	protected static final String   P_ERRORMSG      = "ErrorMessage";
	protected static final String   P_STORE         = "SaveCookie";

	/*************************************************************************/

	/**
	 *  Set Language from request or session in.
	 *  - Properties
	 *  - Cookie
	 *  - Session
	 *  @param request request
	 *  @param sess sess
	 *  @param ctx context
	 *  @param cProp properties
	 */
	private static void checkLanguage (HttpServletRequest request,
		HttpSession sess, Properties ctx, Properties cProp)
	{
		//  Get/set Parameter:      Language
		String AD_Language = request.getParameter(P_LANGUAGE);
		if (AD_Language == null)
		{
			//  Check Cookie
			AD_Language = cProp.getProperty(P_LANGUAGE);
			if (AD_Language == null)
			{
				//  Check Request Locale
				Locale locale = request.getLocale();
				AD_Language = Language.getAD_Language (locale);
			}
		}
		if (AD_Language != null)
		{
			Language language = Language.getLanguage(AD_Language);
			Env.verifyLanguage(ctx, language);
			Env.setContext(ctx, Env.LANG, language.getAD_Language());
			cProp.setProperty(P_LANGUAGE, language.getAD_Language());
			sess.setAttribute(WEnv.SA_LANGUAGE, language);
		}
	}   //  checkLanguage

	/*************************************************************************/


	/**
	 *  First Login Page
	 *  @param cProp Login Cookie information for defaults
	 *  @param request request
	 *  @param errorMessage error message
	 *  @return WDoc page
	 */
	private WDoc createFirstPage(Properties cProp, HttpServletRequest request,
		String errorMessage)
	{
		Log.trace(Log.l4_Data, "WLogin.createFirstPage", errorMessage);
		String AD_Language = (cProp.getProperty(P_LANGUAGE, Language.getAD_Language(request.getLocale())));
		//
		String windowTitle = Msg.getMsg(AD_Language, "Login");
		String usrText = Msg.getMsg(AD_Language, "User");
		String pwdText = Msg.getMsg(AD_Language, "Password");
		String lngText = Msg.translate(AD_Language, "AD_Language");
		String okText = Msg.getMsg(AD_Language, "OK");
		String cancelText = Msg.getMsg(AD_Language, "Cancel");
		String storeTxt = Msg.getMsg(AD_Language, "SaveCookie");

		//  Document
		WDoc doc = WDoc.create (windowTitle);
		body b = doc.getBody();
		//  Clear Menu Frame
		b.addElement(WUtil.getClearFrame(WEnv.TARGET_MENU));

		//	Form - post to same URL
		String action = request.getRequestURI();
		form myForm = null;
		myForm = new form(action, form.post, form.ENC_DEFAULT).setName("Login1");
		myForm.setAcceptCharset(WEnv.CHARACTERSET);
		table table = new table().setAlign(AlignType.center);

		//	Username
		String userData = cProp.getProperty(P_USERNAME, "");
		tr line = new tr();
		label usrLabel = new label().setFor(P_USERNAME).addElement(usrText);
		usrLabel.setID("ID_"+P_USERNAME);
		line.addElement(new td().addElement(usrLabel).setAlign(AlignType.right));
		input usr = new input(input.text, P_USERNAME, userData).setSize(20).setMaxlength(30);
		usr.setID("ID_"+P_USERNAME);
		line.addElement(new td().addElement(usr).setAlign(AlignType.left));
		table.addElement(line);

		//  Password
		String pwdData = cProp.getProperty(P_PASSWORD, "");
		line = new tr();
		label pwdLabel = new label().setFor(P_PASSWORD).addElement(pwdText);
		pwdLabel.setID("ID_"+P_PASSWORD);
		line.addElement(new td().addElement(pwdLabel).setAlign(AlignType.right));
		input pwd = new input(input.password, P_PASSWORD, pwdData).setSize(20).setMaxlength(30);
		pwd.setID("ID_"+P_PASSWORD);
		line.addElement(new td().addElement(pwd).setAlign(AlignType.left));
		table.addElement(line);

		//	Language Pick
		String langData = cProp.getProperty(AD_Language);
		line = new tr();
		label langLabel = new label().setFor(P_LANGUAGE).addElement(lngText);
		langLabel.setID("ID_"+P_LANGUAGE);
		line.addElement(new td().addElement(langLabel).setAlign(AlignType.right));
		option options[] = new option[Language.getLanguageCount()];
		for (int i = 0; i < Language.getLanguageCount(); i++)
		{
			Language language = Language.getLanguage(i);
			options[i] = new option(language.getAD_Language()).addElement(language.getName());
			if (language.getAD_Language().equals(langData))
				options[i].setSelected(true);
			else
				options[i].setSelected(false);
		}
		line.addElement(new td().addElement(new select(P_LANGUAGE, options).setID("ID_"+P_LANGUAGE) ));
		table.addElement(line);

		//  Store Cookie
		String storeData = cProp.getProperty(P_STORE, "N");
		line = new tr();
		line.addElement(new td());
		input store = new input(input.checkbox, P_STORE, "Y").addElement(storeTxt).setChecked(storeData.equals("Y"));
		store.setID("ID_"+P_STORE);
		line.addElement(new td().addElement(store).setAlign(AlignType.left));
		table.addElement(line);

		//  ErrorMessage
		if (errorMessage != null && errorMessage.length() > 0)
		{
			line = new tr();
		//	line.addElement(new td());
			line.addElement(new td().setColSpan(2)
				.addElement(new font(HtmlColor.red, 4).addElement(new b(errorMessage))));   //  color, size
			table.addElement(line);
		}

		//  Finish
		line = new tr();
		input cancel = new input(input.reset, "Reset", cancelText);
		line.addElement(new td().addElement(cancel ));
		line.addElement(new td().addElement(new input(input.submit, P_SUBMIT, okText) ));
		table.addElement(line);
		//
		myForm.addElement(table);
		b.addElement(myForm);

		return doc;
	}   //  getUserPasswordPage


	/**
	 *  Create Second Page
	 *  @param cProp clinet properties
	 *  @param request request
	 *  @param roleOptions role options
	 *  @param errorMessage error message
	 *  @return WDoc page
	 */
	private WDoc createSecondPage(Properties cProp, HttpServletRequest request,
		option[] roleOptions, String errorMessage)
	{
		Log.trace(Log.l4_Data, "WLogin.createSecondPage", errorMessage);
		String AD_Language = cProp.getProperty(P_LANGUAGE, Language.getAD_Language(request.getLocale()));
		String windowTitle = Msg.getMsg(AD_Language, "LoginSuccess");
		//  Create Document
		WDoc doc = WDoc.create (windowTitle);
		body b = doc.getBody();

		//	Form - Get Menu
		String action = WEnv.getBaseDirectory("WMenu");
		form myForm = null;
		myForm = new form(action, form.post, form.ENC_DEFAULT).setName("Login2");
		myForm.setTarget(WEnv.TARGET_MENU);
		myForm.setAcceptCharset(WEnv.CHARACTERSET);
		table table = new table().setAlign(AlignType.center);

		//	Role Pick
		tr line = new tr();
		label roleLabel = new label().setFor(P_ROLE).addElement(Msg.translate(AD_Language, "AD_Role_ID"));
		line.addElement(new td().addElement(roleLabel).setAlign(AlignType.right));
		select role = new select(P_ROLE, roleOptions);
		role.setOnClick("fieldUpdate(this);");        		//  WFieldUpdate sets Client & Org
		line.addElement(new td().addElement(role));
		table.addElement(line);

		//	Client Pick
		line = new tr();
		label clientLabel = new label().setFor(P_CLIENT).addElement(Msg.translate(AD_Language, "AD_Client_ID"));
		line.addElement(new td().addElement(clientLabel).setAlign(AlignType.right));
		select client = new select(P_CLIENT);
		client.setOnClick("fieldUpdate(this);");          	//  WFieldUpdate sets Warehouse
		line.addElement(new td().addElement(client));
		table.addElement(line);

		//	Org Pick
		line = new tr();
		label orgLabel = new label().setFor(P_ORG).addElement(Msg.translate(AD_Language, "AD_Org_ID"));
		line.addElement(new td().addElement(orgLabel).setAlign(AlignType.right));
		line.addElement(new td().addElement(new select(P_ORG) ));
		table.addElement(line);

		//  Warehouse
		line = new tr();
		label whLabel = new label().setFor(P_WAREHOUSE).addElement(Msg.translate(AD_Language, "M_Warehouse_ID"));
		line.addElement(new td().addElement(whLabel).setAlign(AlignType.right));
		line.addElement(new td().addElement(new select(P_WAREHOUSE) ));
		table.addElement(line);

		//  Date
		Language language = (Language)request.getSession().getAttribute(WEnv.SA_LANGUAGE);
		DateFormat df = DisplayType.getDateFormat(DisplayType.Date, language);
		String dateData = df.format(new java.util.Date());
		line = new tr();
		label dateLabel = new label().setFor(P_DATE).addElement(Msg.getMsg(AD_Language, "Date"));
		line.addElement(new td().addElement(dateLabel).setAlign(AlignType.right));
		input date = new input(input.text, P_DATE, dateData).setSize(10).setMaxlength(10);
		date.setID("ID_Date");
		line.addElement(new td().addElement(date).setAlign(AlignType.left));
		table.addElement(line);

		//  ErrorMessage
		if (errorMessage != null && errorMessage.length() > 0)
		{
			line = new tr();
			line.addElement(new td().addElement(new strong(errorMessage)).setColSpan(2).setAlign(AlignType.center));
			table.addElement(line);
		}

		//  Finish
		line = new tr();
		input cancel = new input(input.reset, "Reset", Msg.getMsg(AD_Language, "Cancel"));
		line.addElement(new td().addElement(cancel ));
		input submit = new input(input.submit, "Submit", Msg.getMsg(AD_Language, "OK"));
		submit.setOnClick("showLoadingMenu('" + WEnv.getBaseDirectory("") + "');");
		line.addElement(new td().addElement(submit));
		table.addElement(line);

		//  Note
		line = new tr();
		String note = Msg.getMsg(AD_Language, "WLoginBrowserNote");
		line.addElement(new td().addElement(note).setColSpan(2).setAlign(AlignType.center));
		table.addElement(line);
		//
		myForm.addElement(table);
		b.addElement(myForm);
		//
		String script = "fieldUpdate(document.Login2." + P_ROLE + ");";	//  init dependency updates
		b.addElement(new script(script));

		return doc;
	}   //  getSecondPage

}	//	WLogin
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
package org.compiere.www;

import javax.servlet.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import java.sql.*;
import java.text.*;
import java.security.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;

import org.compiere.util.*;

/**
 *  Web Login Page.
 *  <p>
 *  Page request:
 *  <pre>
 *  - Check database connection
 *  - LoginInfo from request?
 *      - Yes: DoLogin success ?
 *          - Yes: return (second) preferences page
 *          - No: return (first) user/password page
 *      - No: User Principal ?
 *          - Yes: DoLogin success ?
 *              - Yes: return (second) preferences page
 *              - No: return (first) user/password page
 *          - No: return (first) user/password page
 *  </pre>
 *
 *  @author Jorg Janke
 *  @version  $Id: WLogin.java,v 1.2 2003/01/20 05:42:07 jjanke Exp $
 */
public class WLogin extends HttpServlet
{
	/**
	 *	Initialize
	 *  @param config confif
	 *  @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		if (!WEnv.initWeb(config))
			throw new ServletException("WLogin.init");
	}	//	init

	/**
	 * Get Servlet information
	 * @return servlet info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Login";
	}	//	getServletInfo

	/**
	 *	Clean up
	 */
	public void destroy()
	{
		Log.trace(Log.l1_User, "WLogin.destroy");
		super.destroy();
	}	//	destroy


	/**
	 *	Process the HTTP Get request - forward to Post
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		Log.trace(Log.l1_User, "WLogin.doGet");
		doPost (request, response);
	}	//	doGet


	/**
	 *	Process the HTTP Post request.
	 *  <pre>
	 *  - Optionally create Session
	 *  - Check database connection
	 *  - LoginInfo from request?
	 *      - Yes: DoLogin success ?
	 *          - Yes: return (second) preferences page
	 *          - No: return (first) user/password page
	 *      - No: User Principal ?
	 *          - Yes: DoLogin success ?
	 *              - Yes: return (second) preferences page
	 *              - No: return (first) user/password page
	 *          - No: return (first) user/password page
	 *  </pre>
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		Log.trace(Log.l1_User, "WLogin.doPost");
		//  Create New Session
		HttpSession sess = request.getSession(true);
		sess.setMaxInactiveInterval(WEnv.TIMEOUT);

		//  Get/set Context
		Properties ctx = (Properties)sess.getAttribute(WEnv.SA_CONTEXT);
		if (ctx == null)
			ctx = new Properties();
		sess.setAttribute(WEnv.SA_CONTEXT, ctx);

		//  Get Cookie
		Properties cProp = WUtil.getCookieProprties(request);
		//  Language
		checkLanguage (request, sess, ctx, cProp);
		//  Page
		WDoc doc = null;

		//  Check DB connection
		if (!DB.isConnected())
		{
			String msg = Msg.getMsg(ctx, "WLoginNoDB");
			doc = WDoc.create (msg);
			if (msg.equals("WLoginNoDB"))
				msg = "No Database Connection";
			doc.getBody().addElement(new h1(msg));
		}

		//  Login Info from request?
		else
		{
			//  Get Parameters:     UserName/Password
			String usr = request.getParameter(P_USERNAME);
			String pwd = request.getParameter(P_PASSWORD);
			//  Get Principle
			Principal userPr = request.getUserPrincipal();
			Log.trace(Log.l4_Data, "Principal=" + userPr + "; User=" + usr);

			//  Login info not from request and not pre-authorized
			if (userPr == null && (usr == null || pwd == null))
				doc = createFirstPage (cProp, request, "");
			//  Login info from request or authorized
			else
			{
				KeyNamePair[] roles = null;
				//  Pre-authorized
				if (userPr != null)
				{
					roles = DB.login(ctx, userPr);
					usr = userPr.getName();
				}
				else
					roles = DB.login(ctx, usr, pwd);
				//
				if (roles == null)
					doc = createFirstPage(cProp, request, Msg.getMsg(ctx, "UserPwdError"));
				else
					doc = createSecondPage(cProp, request, WUtil.convertToOption(roles, null), "");
				//  Can we save Cookie ?
				if (request.getParameter(P_STORE) == null)
				{
					cProp.clear();                          //  erase all
				}
				else    //  Save Cookie Parameter
				{
					cProp.setProperty(P_USERNAME, usr);
					cProp.setProperty(P_STORE, "Y");
					cProp.setProperty(P_PASSWORD, pwd);     //  For test only
				}
			}
		}

		//
		WUtil.createResponse (request, response, this, cProp, doc, true);
	}	//	doPost

	//  Variable Names
	public static final String      P_USERNAME      = "User";
	private static final String     P_PASSWORD      = "Password";
	protected static final String   P_LANGUAGE      = Env.LANG;
	private static final String     P_SUBMIT        = "Submit";
	//  WMenu picks it up
	protected static final String   P_ROLE          = "AD_Role_ID";
	protected static final String   P_CLIENT        = "AD_Client_ID";
	protected static final String   P_ORG           = "AD_Org_ID";
	protected static final String   P_DATE          = "Date";
	protected static final String   P_WAREHOUSE     = "M_Warehouse_ID";
	protected static final String   P_ERRORMSG      = "ErrorMessage";
	protected static final String   P_STORE         = "SaveCookie";

	/*************************************************************************/

	/**
	 *  Set Language from request or session in.
	 *  - Properties
	 *  - Cookie
	 *  - Session
	 *  @param request request
	 *  @param sess sess
	 *  @param ctx context
	 *  @param cProp properties
	 */
	private static void checkLanguage (HttpServletRequest request,
		HttpSession sess, Properties ctx, Properties cProp)
	{
		//  Get/set Parameter:      Language
		String AD_Language = request.getParameter(P_LANGUAGE);
		if (AD_Language == null)
		{
			//  Check Cookie
			AD_Language = cProp.getProperty(P_LANGUAGE);
			if (AD_Language == null)
			{
				//  Check Request Locale
				Locale locale = request.getLocale();
				AD_Language = Language.getAD_Language (locale);
			}
		}
		if (AD_Language != null)
		{
			Language language = Language.getLanguage(AD_Language);
			Env.verifyLanguage(ctx, language);
			Env.setContext(ctx, Env.LANG, language.getAD_Language());
			cProp.setProperty(P_LANGUAGE, language.getAD_Language());
			sess.setAttribute(WEnv.SA_LANGUAGE, language);
		}
	}   //  checkLanguage

	/*************************************************************************/


	/**
	 *  First Login Page
	 *  @param cProp Login Cookie information for defaults
	 *  @param request request
	 *  @param errorMessage error message
	 *  @return WDoc page
	 */
	private WDoc createFirstPage(Properties cProp, HttpServletRequest request,
		String errorMessage)
	{
		Log.trace(Log.l4_Data, "WLogin.createFirstPage", errorMessage);
		String AD_Language = (cProp.getProperty(P_LANGUAGE, Language.getAD_Language(request.getLocale())));
		//
		String windowTitle = Msg.getMsg(AD_Language, "Login");
		String usrText = Msg.getMsg(AD_Language, "User");
		String pwdText = Msg.getMsg(AD_Language, "Password");
		String lngText = Msg.translate(AD_Language, "AD_Language");
		String okText = Msg.getMsg(AD_Language, "OK");
		String cancelText = Msg.getMsg(AD_Language, "Cancel");
		String storeTxt = Msg.getMsg(AD_Language, "SaveCookie");

		//  Document
		WDoc doc = WDoc.create (windowTitle);
		body b = doc.getBody();
		//  Clear Menu Frame
		b.addElement(WUtil.getClearFrame(WEnv.TARGET_MENU));

		//	Form - post to same URL
		String action = request.getRequestURI();
		form form = null;
		form = new form(action, form.post, form.ENC_DEFAULT).setName("Login1");
		form.setAcceptCharset(WEnv.CHARACTERSET);
		table table = new table().setAlign(AlignType.center);

		//	Username
		String userData = cProp.getProperty(P_USERNAME, "");
		tr line = new tr();
		label usrLabel = new label().setFor(P_USERNAME).addElement(usrText);
		usrLabel.setID("ID_"+P_USERNAME);
		line.addElement(new td().addElement(usrLabel).setAlign(AlignType.right));
		input usr = new input(input.text, P_USERNAME, userData).setSize(20).setMaxlength(30);
		usr.setID("ID_"+P_USERNAME);
		line.addElement(new td().addElement(usr).setAlign(AlignType.left));
		table.addElement(line);

		//  Password
		String pwdData = cProp.getProperty(P_PASSWORD, "");
		line = new tr();
		label pwdLabel = new label().setFor(P_PASSWORD).addElement(pwdText);
		pwdLabel.setID("ID_"+P_PASSWORD);
		line.addElement(new td().addElement(pwdLabel).setAlign(AlignType.right));
		input pwd = new input(input.password, P_PASSWORD, pwdData).setSize(20).setMaxlength(30);
		pwd.setID("ID_"+P_PASSWORD);
		line.addElement(new td().addElement(pwd).setAlign(AlignType.left));
		table.addElement(line);

		//	Language Pick
		String langData = cProp.getProperty(AD_Language);
		line = new tr();
		label langLabel = new label().setFor(P_LANGUAGE).addElement(lngText);
		langLabel.setID("ID_"+P_LANGUAGE);
		line.addElement(new td().addElement(langLabel).setAlign(AlignType.right));
		option options[] = new option[Language.getLanguageCount()];
		for (int i = 0; i < Language.getLanguageCount(); i++)
		{
			Language language = Language.getLanguage(i);
			options[i] = new option(language.getAD_Language()).addElement(language.getName());
			if (language.getAD_Language().equals(langData))
				options[i].setSelected(true);
			else
				options[i].setSelected(false);
		}
		line.addElement(new td().addElement(new select(P_LANGUAGE, options).setID("ID_"+P_LANGUAGE) ));
		table.addElement(line);

		//  Store Cookie
		String storeData = cProp.getProperty(P_STORE, "N");
		line = new tr();
		line.addElement(new td());
		input store = new input(input.checkbox, P_STORE, "Y").addElement(storeTxt).setChecked(storeData.equals("Y"));
		store.setID("ID_"+P_STORE);
		line.addElement(new td().addElement(store).setAlign(AlignType.left));
		table.addElement(line);

		//  ErrorMessage
		if (errorMessage != null && errorMessage.length() > 0)
		{
			line = new tr();
		//	line.addElement(new td());
			line.addElement(new td().setColSpan(2)
				.addElement(new font(HtmlColor.red, 4).addElement(new b(errorMessage))));   //  color, size
			table.addElement(line);
		}

		//  Finish
		line = new tr();
		input cancel = new input(input.reset, "Reset", cancelText);
		line.addElement(new td().addElement(cancel ));
		line.addElement(new td().addElement(new input(input.submit, P_SUBMIT, okText) ));
		table.addElement(line);
		//
		form.addElement(table);
		b.addElement(form);

		return doc;
	}   //  getUserPasswordPage


	/**
	 *  Create Second Page
	 *  @param cProp clinet properties
	 *  @param request request
	 *  @param roleOptions role options
	 *  @param errorMessage error message
	 *  @return WDoc page
	 */
	private WDoc createSecondPage(Properties cProp, HttpServletRequest request,
		option[] roleOptions, String errorMessage)
	{
		Log.trace(Log.l4_Data, "WLogin.createSecondPage", errorMessage);
		String AD_Language = cProp.getProperty(P_LANGUAGE, Language.getAD_Language(request.getLocale()));
		String windowTitle = Msg.getMsg(AD_Language, "LoginSuccess");
		//  Create Document
		WDoc doc = WDoc.create (windowTitle);
		body b = doc.getBody();

		//	Form - Get Menu
		String action = WEnv.getBaseDirectory("WMenu");
		form form = null;
		form = new form(action, form.post, form.ENC_DEFAULT).setName("Login2");
		form.setTarget(WEnv.TARGET_MENU);
		form.setAcceptCharset(WEnv.CHARACTERSET);
		table table = new table().setAlign(AlignType.center);

		//	Role Pick
		tr line = new tr();
		label roleLabel = new label().setFor(P_ROLE).addElement(Msg.translate(AD_Language, "AD_Role_ID"));
		line.addElement(new td().addElement(roleLabel).setAlign(AlignType.right));
		select role = new select(P_ROLE, roleOptions);
		role.setOnClick("fieldUpdate();");                  //  WFieldUpdate sets Client & Org
		line.addElement(new td().addElement(role));
		table.addElement(line);

		//	Client Pick
		line = new tr();
		label clientLabel = new label().setFor(P_CLIENT).addElement(Msg.translate(AD_Language, "AD_Client_ID"));
		line.addElement(new td().addElement(clientLabel).setAlign(AlignType.right));
		select client = new select(P_CLIENT);
		client.setOnClick("fieldUpdate();");                //  WFieldUpdate sets Warehouse
		line.addElement(new td().addElement(client));
		table.addElement(line);

		//	Org Pick
		line = new tr();
		label orgLabel = new label().setFor(P_ORG).addElement(Msg.translate(AD_Language, "AD_Org_ID"));
		line.addElement(new td().addElement(orgLabel).setAlign(AlignType.right));
		line.addElement(new td().addElement(new select(P_ORG) ));
		table.addElement(line);

		//  Warehouse
		line = new tr();
		label whLabel = new label().setFor(P_WAREHOUSE).addElement(Msg.translate(AD_Language, "M_Warehouse_ID"));
		line.addElement(new td().addElement(whLabel).setAlign(AlignType.right));
		line.addElement(new td().addElement(new select(P_WAREHOUSE) ));
		table.addElement(line);

		//  Date
		Language language = (Language)request.getSession().getAttribute(WEnv.SA_LANGUAGE);
		DateFormat df = DisplayType.getDateFormat(DisplayType.Date, language);
		String dateData = df.format(new java.util.Date());
		line = new tr();
		label dateLabel = new label().setFor(P_DATE).addElement(Msg.getMsg(AD_Language, "Date"));
		line.addElement(new td().addElement(dateLabel).setAlign(AlignType.right));
		input date = new input(input.text, P_DATE, dateData).setSize(10).setMaxlength(10);
		date.setID("ID_Date");
		line.addElement(new td().addElement(date).setAlign(AlignType.left));
		table.addElement(line);

		//  ErrorMessage
		if (errorMessage != null && errorMessage.length() > 0)
		{
			line = new tr();
			line.addElement(new td().addElement(new strong(errorMessage)).setColSpan(2).setAlign(AlignType.center));
			table.addElement(line);
		}

		//  Finish
		line = new tr();
		input cancel = new input(input.reset, "Reset", Msg.getMsg(AD_Language, "Cancel"));
		line.addElement(new td().addElement(cancel ));
		input submit = new input(input.submit, "Submit", Msg.getMsg(AD_Language, "OK"));
		submit.setOnClick("showLoadingMenu('" + WEnv.getBaseDirectory("") + "');");
		line.addElement(new td().addElement(submit));
		table.addElement(line);

		//  Note
		line = new tr();
		String note = Msg.getMsg(AD_Language, "WLoginBrowserNote");
		line.addElement(new td().addElement(note).setColSpan(2).setAlign(AlignType.center));
		table.addElement(line);
		//
		form.addElement(table);
		b.addElement(form);
		//
		String script = "document.Login2." + P_ROLE + ".click();";         //  init dependency updates
		b.addElement(new script(script));

		return doc;
	}   //  getSecondPage

}	//	WLogin
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
package org.compiere.www;

import javax.servlet.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import java.sql.*;
import java.text.*;
import java.security.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;

import org.compiere.util.*;

/**
 *  Web Login Page.
 *  <p>
 *  Page request:
 *  <pre>
 *  - Check database connection
 *  - LoginInfo from request?
 *      - Yes: DoLogin success ?
 *          - Yes: return (second) preferences page
 *          - No: return (first) user/password page
 *      - No: User Principal ?
 *          - Yes: DoLogin success ?
 *              - Yes: return (second) preferences page
 *              - No: return (first) user/password page
 *          - No: return (first) user/password page
 *  </pre>
 *
 *  @author Jorg Janke
 *  @version  $Id: WLogin.java,v 1.2 2003/01/20 05:42:07 jjanke Exp $
 */
public class WLogin extends HttpServlet
{
	/**
	 *	Initialize
	 *  @param config confif
	 *  @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		if (!WEnv.initWeb(config))
			throw new ServletException("WLogin.init");
	}	//	init

	/**
	 * Get Servlet information
	 * @return servlet info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Login";
	}	//	getServletInfo

	/**
	 *	Clean up
	 */
	public void destroy()
	{
		Log.trace(Log.l1_User, "WLogin.destroy");
		super.destroy();
	}	//	destroy


	/**
	 *	Process the HTTP Get request - forward to Post
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		Log.trace(Log.l1_User, "WLogin.doGet");
		doPost (request, response);
	}	//	doGet


	/**
	 *	Process the HTTP Post request.
	 *  <pre>
	 *  - Optionally create Session
	 *  - Check database connection
	 *  - LoginInfo from request?
	 *      - Yes: DoLogin success ?
	 *          - Yes: return (second) preferences page
	 *          - No: return (first) user/password page
	 *      - No: User Principal ?
	 *          - Yes: DoLogin success ?
	 *              - Yes: return (second) preferences page
	 *              - No: return (first) user/password page
	 *          - No: return (first) user/password page
	 *  </pre>
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		Log.trace(Log.l1_User, "WLogin.doPost");
		//  Create New Session
		HttpSession sess = request.getSession(true);
		sess.setMaxInactiveInterval(WEnv.TIMEOUT);

		//  Get/set Context
		Properties ctx = (Properties)sess.getAttribute(WEnv.SA_CONTEXT);
		if (ctx == null)
			ctx = new Properties();
		sess.setAttribute(WEnv.SA_CONTEXT, ctx);

		//  Get Cookie
		Properties cProp = WUtil.getCookieProprties(request);
		//  Language
		checkLanguage (request, sess, ctx, cProp);
		//  Page
		WDoc doc = null;

		//  Check DB connection
		if (!DB.isConnected())
		{
			String msg = Msg.getMsg(ctx, "WLoginNoDB");
			doc = WDoc.create (msg);
			if (msg.equals("WLoginNoDB"))
				msg = "No Database Connection";
			doc.getBody().addElement(new h1(msg));
		}

		//  Login Info from request?
		else
		{
			//  Get Parameters:     UserName/Password
			String usr = request.getParameter(P_USERNAME);
			String pwd = request.getParameter(P_PASSWORD);
			//  Get Principle
			Principal userPr = request.getUserPrincipal();
			Log.trace(Log.l4_Data, "Principal=" + userPr + "; User=" + usr);

			//  Login info not from request and not pre-authorized
			if (userPr == null && (usr == null || pwd == null))
				doc = createFirstPage (cProp, request, "");
			//  Login info from request or authorized
			else
			{
				KeyNamePair[] roles = null;
				//  Pre-authorized
				if (userPr != null)
				{
					roles = DB.login(ctx, userPr);
					usr = userPr.getName();
				}
				else
					roles = DB.login(ctx, usr, pwd);
				//
				if (roles == null)
					doc = createFirstPage(cProp, request, Msg.getMsg(ctx, "UserPwdError"));
				else
					doc = createSecondPage(cProp, request, WUtil.convertToOption(roles, null), "");
				//  Can we save Cookie ?
				if (request.getParameter(P_STORE) == null)
				{
					cProp.clear();                          //  erase all
				}
				else    //  Save Cookie Parameter
				{
					cProp.setProperty(P_USERNAME, usr);
					cProp.setProperty(P_STORE, "Y");
					cProp.setProperty(P_PASSWORD, pwd);     //  For test only
				}
			}
		}

		//
		WUtil.createResponse (request, response, this, cProp, doc, true);
	}	//	doPost

	//  Variable Names
	public static final String      P_USERNAME      = "User";
	private static final String     P_PASSWORD      = "Password";
	protected static final String   P_LANGUAGE      = Env.LANG;
	private static final String     P_SUBMIT        = "Submit";
	//  WMenu picks it up
	protected static final String   P_ROLE          = "AD_Role_ID";
	protected static final String   P_CLIENT        = "AD_Client_ID";
	protected static final String   P_ORG           = "AD_Org_ID";
	protected static final String   P_DATE          = "Date";
	protected static final String   P_WAREHOUSE     = "M_Warehouse_ID";
	protected static final String   P_ERRORMSG      = "ErrorMessage";
	protected static final String   P_STORE         = "SaveCookie";

	/*************************************************************************/

	/**
	 *  Set Language from request or session in.
	 *  - Properties
	 *  - Cookie
	 *  - Session
	 *  @param request request
	 *  @param sess sess
	 *  @param ctx context
	 *  @param cProp properties
	 */
	private static void checkLanguage (HttpServletRequest request,
		HttpSession sess, Properties ctx, Properties cProp)
	{
		//  Get/set Parameter:      Language
		String AD_Language = request.getParameter(P_LANGUAGE);
		if (AD_Language == null)
		{
			//  Check Cookie
			AD_Language = cProp.getProperty(P_LANGUAGE);
			if (AD_Language == null)
			{
				//  Check Request Locale
				Locale locale = request.getLocale();
				AD_Language = Language.getAD_Language (locale);
			}
		}
		if (AD_Language != null)
		{
			Language language = Language.getLanguage(AD_Language);
			Env.verifyLanguage(ctx, language);
			Env.setContext(ctx, Env.LANG, language.getAD_Language());
			cProp.setProperty(P_LANGUAGE, language.getAD_Language());
			sess.setAttribute(WEnv.SA_LANGUAGE, language);
		}
	}   //  checkLanguage

	/*************************************************************************/


	/**
	 *  First Login Page
	 *  @param cProp Login Cookie information for defaults
	 *  @param request request
	 *  @param errorMessage error message
	 *  @return WDoc page
	 */
	private WDoc createFirstPage(Properties cProp, HttpServletRequest request,
		String errorMessage)
	{
		Log.trace(Log.l4_Data, "WLogin.createFirstPage", errorMessage);
		String AD_Language = (cProp.getProperty(P_LANGUAGE, Language.getAD_Language(request.getLocale())));
		//
		String windowTitle = Msg.getMsg(AD_Language, "Login");
		String usrText = Msg.getMsg(AD_Language, "User");
		String pwdText = Msg.getMsg(AD_Language, "Password");
		String lngText = Msg.translate(AD_Language, "AD_Language");
		String okText = Msg.getMsg(AD_Language, "OK");
		String cancelText = Msg.getMsg(AD_Language, "Cancel");
		String storeTxt = Msg.getMsg(AD_Language, "SaveCookie");

		//  Document
		WDoc doc = WDoc.create (windowTitle);
		body b = doc.getBody();
		//  Clear Menu Frame
		b.addElement(WUtil.getClearFrame(WEnv.TARGET_MENU));

		//	Form - post to same URL
		String action = request.getRequestURI();
		form form = null;
		form = new form(action, form.post, form.ENC_DEFAULT).setName("Login1");
		form.setAcceptCharset(WEnv.CHARACTERSET);
		table table = new table().setAlign(AlignType.center);

		//	Username
		String userData = cProp.getProperty(P_USERNAME, "");
		tr line = new tr();
		label usrLabel = new label().setFor(P_USERNAME).addElement(usrText);
		usrLabel.setID("ID_"+P_USERNAME);
		line.addElement(new td().addElement(usrLabel).setAlign(AlignType.right));
		input usr = new input(input.text, P_USERNAME, userData).setSize(20).setMaxlength(30);
		usr.setID("ID_"+P_USERNAME);
		line.addElement(new td().addElement(usr).setAlign(AlignType.left));
		table.addElement(line);

		//  Password
		String pwdData = cProp.getProperty(P_PASSWORD, "");
		line = new tr();
		label pwdLabel = new label().setFor(P_PASSWORD).addElement(pwdText);
		pwdLabel.setID("ID_"+P_PASSWORD);
		line.addElement(new td().addElement(pwdLabel).setAlign(AlignType.right));
		input pwd = new input(input.password, P_PASSWORD, pwdData).setSize(20).setMaxlength(30);
		pwd.setID("ID_"+P_PASSWORD);
		line.addElement(new td().addElement(pwd).setAlign(AlignType.left));
		table.addElement(line);

		//	Language Pick
		String langData = cProp.getProperty(AD_Language);
		line = new tr();
		label langLabel = new label().setFor(P_LANGUAGE).addElement(lngText);
		langLabel.setID("ID_"+P_LANGUAGE);
		line.addElement(new td().addElement(langLabel).setAlign(AlignType.right));
		option options[] = new option[Language.getLanguageCount()];
		for (int i = 0; i < Language.getLanguageCount(); i++)
		{
			Language language = Language.getLanguage(i);
			options[i] = new option(language.getAD_Language()).addElement(language.getName());
			if (language.getAD_Language().equals(langData))
				options[i].setSelected(true);
			else
				options[i].setSelected(false);
		}
		line.addElement(new td().addElement(new select(P_LANGUAGE, options).setID("ID_"+P_LANGUAGE) ));
		table.addElement(line);

		//  Store Cookie
		String storeData = cProp.getProperty(P_STORE, "N");
		line = new tr();
		line.addElement(new td());
		input store = new input(input.checkbox, P_STORE, "Y").addElement(storeTxt).setChecked(storeData.equals("Y"));
		store.setID("ID_"+P_STORE);
		line.addElement(new td().addElement(store).setAlign(AlignType.left));
		table.addElement(line);

		//  ErrorMessage
		if (errorMessage != null && errorMessage.length() > 0)
		{
			line = new tr();
		//	line.addElement(new td());
			line.addElement(new td().setColSpan(2)
				.addElement(new font(HtmlColor.red, 4).addElement(new b(errorMessage))));   //  color, size
			table.addElement(line);
		}

		//  Finish
		line = new tr();
		input cancel = new input(input.reset, "Reset", cancelText);
		line.addElement(new td().addElement(cancel ));
		line.addElement(new td().addElement(new input(input.submit, P_SUBMIT, okText) ));
		table.addElement(line);
		//
		form.addElement(table);
		b.addElement(form);

		return doc;
	}   //  getUserPasswordPage


	/**
	 *  Create Second Page
	 *  @param cProp clinet properties
	 *  @param request request
	 *  @param roleOptions role options
	 *  @param errorMessage error message
	 *  @return WDoc page
	 */
	private WDoc createSecondPage(Properties cProp, HttpServletRequest request,
		option[] roleOptions, String errorMessage)
	{
		Log.trace(Log.l4_Data, "WLogin.createSecondPage", errorMessage);
		String AD_Language = cProp.getProperty(P_LANGUAGE, Language.getAD_Language(request.getLocale()));
		String windowTitle = Msg.getMsg(AD_Language, "LoginSuccess");
		//  Create Document
		WDoc doc = WDoc.create (windowTitle);
		body b = doc.getBody();

		//	Form - Get Menu
		String action = WEnv.getBaseDirectory("WMenu");
		form form = null;
		form = new form(action, form.post, form.ENC_DEFAULT).setName("Login2");
		form.setTarget(WEnv.TARGET_MENU);
		form.setAcceptCharset(WEnv.CHARACTERSET);
		table table = new table().setAlign(AlignType.center);

		//	Role Pick
		tr line = new tr();
		label roleLabel = new label().setFor(P_ROLE).addElement(Msg.translate(AD_Language, "AD_Role_ID"));
		line.addElement(new td().addElement(roleLabel).setAlign(AlignType.right));
		select role = new select(P_ROLE, roleOptions);
		role.setOnClick("fieldUpdate();");                  //  WFieldUpdate sets Client & Org
		line.addElement(new td().addElement(role));
		table.addElement(line);

		//	Client Pick
		line = new tr();
		label clientLabel = new label().setFor(P_CLIENT).addElement(Msg.translate(AD_Language, "AD_Client_ID"));
		line.addElement(new td().addElement(clientLabel).setAlign(AlignType.right));
		select client = new select(P_CLIENT);
		client.setOnClick("fieldUpdate();");                //  WFieldUpdate sets Warehouse
		line.addElement(new td().addElement(client));
		table.addElement(line);

		//	Org Pick
		line = new tr();
		label orgLabel = new label().setFor(P_ORG).addElement(Msg.translate(AD_Language, "AD_Org_ID"));
		line.addElement(new td().addElement(orgLabel).setAlign(AlignType.right));
		line.addElement(new td().addElement(new select(P_ORG) ));
		table.addElement(line);

		//  Warehouse
		line = new tr();
		label whLabel = new label().setFor(P_WAREHOUSE).addElement(Msg.translate(AD_Language, "M_Warehouse_ID"));
		line.addElement(new td().addElement(whLabel).setAlign(AlignType.right));
		line.addElement(new td().addElement(new select(P_WAREHOUSE) ));
		table.addElement(line);

		//  Date
		Language language = (Language)request.getSession().getAttribute(WEnv.SA_LANGUAGE);
		DateFormat df = DisplayType.getDateFormat(DisplayType.Date, language);
		String dateData = df.format(new java.util.Date());
		line = new tr();
		label dateLabel = new label().setFor(P_DATE).addElement(Msg.getMsg(AD_Language, "Date"));
		line.addElement(new td().addElement(dateLabel).setAlign(AlignType.right));
		input date = new input(input.text, P_DATE, dateData).setSize(10).setMaxlength(10);
		date.setID("ID_Date");
		line.addElement(new td().addElement(date).setAlign(AlignType.left));
		table.addElement(line);

		//  ErrorMessage
		if (errorMessage != null && errorMessage.length() > 0)
		{
			line = new tr();
			line.addElement(new td().addElement(new strong(errorMessage)).setColSpan(2).setAlign(AlignType.center));
			table.addElement(line);
		}

		//  Finish
		line = new tr();
		input cancel = new input(input.reset, "Reset", Msg.getMsg(AD_Language, "Cancel"));
		line.addElement(new td().addElement(cancel ));
		input submit = new input(input.submit, "Submit", Msg.getMsg(AD_Language, "OK"));
		submit.setOnClick("showLoadingMenu('" + WEnv.getBaseDirectory("") + "');");
		line.addElement(new td().addElement(submit));
		table.addElement(line);

		//  Note
		line = new tr();
		String note = Msg.getMsg(AD_Language, "WLoginBrowserNote");
		line.addElement(new td().addElement(note).setColSpan(2).setAlign(AlignType.center));
		table.addElement(line);
		//
		form.addElement(table);
		b.addElement(form);
		//
		String script = "document.Login2." + P_ROLE + ".click();";         //  init dependency updates
		b.addElement(new script(script));

		return doc;
	}   //  getSecondPage

}	//	WLogin
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
package org.compiere.www;

import javax.servlet.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import java.sql.*;
import java.text.*;
import java.security.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;

import org.compiere.util.*;

/**
 *  Web Login Page.
 *  <p>
 *  Page request:
 *  <pre>
 *  - Check database connection
 *  - LoginInfo from request?
 *      - Yes: DoLogin success ?
 *          - Yes: return (second) preferences page
 *          - No: return (first) user/password page
 *      - No: User Principal ?
 *          - Yes: DoLogin success ?
 *              - Yes: return (second) preferences page
 *              - No: return (first) user/password page
 *          - No: return (first) user/password page
 *  </pre>
 *
 *  @author Jorg Janke
 *  @version  $Id: WLogin.java,v 1.3 2003/09/29 01:34:27 jjanke Exp $
 */
public class WLogin extends HttpServlet
{
	/**
	 *	Initialize
	 *  @param config confif
	 *  @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		if (!WEnv.initWeb(config))
			throw new ServletException("WLogin.init");
	}	//	init

	/**
	 * Get Servlet information
	 * @return servlet info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Login";
	}	//	getServletInfo

	/**
	 *	Clean up
	 */
	public void destroy()
	{
		Log.trace(Log.l1_User, "WLogin.destroy");
		super.destroy();
	}	//	destroy


	/**
	 *	Process the HTTP Get request - forward to Post
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		Log.trace(Log.l1_User, "WLogin.doGet");
		doPost (request, response);
	}	//	doGet


	/**
	 *	Process the HTTP Post request.
	 *  <pre>
	 *  - Optionally create Session
	 *  - Check database connection
	 *  - LoginInfo from request?
	 *      - Yes: DoLogin success ?
	 *          - Yes: return (second) preferences page
	 *          - No: return (first) user/password page
	 *      - No: User Principal ?
	 *          - Yes: DoLogin success ?
	 *              - Yes: return (second) preferences page
	 *              - No: return (first) user/password page
	 *          - No: return (first) user/password page
	 *  </pre>
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		Log.trace(Log.l1_User, "WLogin.doPost");
		//  Create New Session
		HttpSession sess = request.getSession(true);
		sess.setMaxInactiveInterval(WEnv.TIMEOUT);

		//  Get/set Context
		Properties ctx = (Properties)sess.getAttribute(WEnv.SA_CONTEXT);
		if (ctx == null)
			ctx = new Properties();
		sess.setAttribute(WEnv.SA_CONTEXT, ctx);

		//  Get Cookie
		Properties cProp = WUtil.getCookieProprties(request);
		//  Language
		checkLanguage (request, sess, ctx, cProp);
		//  Page
		WDoc doc = null;

		//  Check DB connection
		if (!DB.isConnected())
		{
			String msg = Msg.getMsg(ctx, "WLoginNoDB");
			doc = WDoc.create (msg);
			if (msg.equals("WLoginNoDB"))
				msg = "No Database Connection";
			doc.getBody().addElement(new h1(msg));
		}

		//  Login Info from request?
		else
		{
			//  Get Parameters:     UserName/Password
			String usr = request.getParameter(P_USERNAME);
			String pwd = request.getParameter(P_PASSWORD);
			//  Get Principle
			Principal userPr = request.getUserPrincipal();
			Log.trace(Log.l4_Data, "Principal=" + userPr + "; User=" + usr);

			//  Login info not from request and not pre-authorized
			if (userPr == null && (usr == null || pwd == null))
				doc = createFirstPage (cProp, request, "");
			//  Login info from request or authorized
			else
			{
				KeyNamePair[] roles = null;
				//  Pre-authorized
				if (userPr != null)
				{
					roles = DB.login(ctx, userPr);
					usr = userPr.getName();
				}
				else
					roles = DB.login(ctx, usr, pwd);
				//
				if (roles == null)
					doc = createFirstPage(cProp, request, Msg.getMsg(ctx, "UserPwdError"));
				else
					doc = createSecondPage(cProp, request, WUtil.convertToOption(roles, null), "");
				//  Can we save Cookie ?
				if (request.getParameter(P_STORE) == null)
				{
					cProp.clear();                          //  erase all
				}
				else    //  Save Cookie Parameter
				{
					cProp.setProperty(P_USERNAME, usr);
					cProp.setProperty(P_STORE, "Y");
					cProp.setProperty(P_PASSWORD, pwd);     //  For test only
				}
			}
		}

		//
		WUtil.createResponse (request, response, this, cProp, doc, true);
	}	//	doPost

	//  Variable Names
	public static final String      P_USERNAME      = "User";
	private static final String     P_PASSWORD      = "Password";
	protected static final String   P_LANGUAGE      = Env.LANG;
	private static final String     P_SUBMIT        = "Submit";
	//  WMenu picks it up
	protected static final String   P_ROLE          = "AD_Role_ID";
	protected static final String   P_CLIENT        = "AD_Client_ID";
	protected static final String   P_ORG           = "AD_Org_ID";
	protected static final String   P_DATE          = "Date";
	protected static final String   P_WAREHOUSE     = "M_Warehouse_ID";
	protected static final String   P_ERRORMSG      = "ErrorMessage";
	protected static final String   P_STORE         = "SaveCookie";

	/*************************************************************************/

	/**
	 *  Set Language from request or session in.
	 *  - Properties
	 *  - Cookie
	 *  - Session
	 *  @param request request
	 *  @param sess sess
	 *  @param ctx context
	 *  @param cProp properties
	 */
	private static void checkLanguage (HttpServletRequest request,
		HttpSession sess, Properties ctx, Properties cProp)
	{
		//  Get/set Parameter:      Language
		String AD_Language = request.getParameter(P_LANGUAGE);
		if (AD_Language == null)
		{
			//  Check Cookie
			AD_Language = cProp.getProperty(P_LANGUAGE);
			if (AD_Language == null)
			{
				//  Check Request Locale
				Locale locale = request.getLocale();
				AD_Language = Language.getAD_Language (locale);
			}
		}
		if (AD_Language != null)
		{
			Language language = Language.getLanguage(AD_Language);
			Env.verifyLanguage(ctx, language);
			Env.setContext(ctx, Env.LANG, language.getAD_Language());
			cProp.setProperty(P_LANGUAGE, language.getAD_Language());
			sess.setAttribute(WEnv.SA_LANGUAGE, language);
		}
	}   //  checkLanguage

	/*************************************************************************/


	/**
	 *  First Login Page
	 *  @param cProp Login Cookie information for defaults
	 *  @param request request
	 *  @param errorMessage error message
	 *  @return WDoc page
	 */
	private WDoc createFirstPage(Properties cProp, HttpServletRequest request,
		String errorMessage)
	{
		Log.trace(Log.l4_Data, "WLogin.createFirstPage", errorMessage);
		String AD_Language = (cProp.getProperty(P_LANGUAGE, Language.getAD_Language(request.getLocale())));
		//
		String windowTitle = Msg.getMsg(AD_Language, "Login");
		String usrText = Msg.getMsg(AD_Language, "User");
		String pwdText = Msg.getMsg(AD_Language, "Password");
		String lngText = Msg.translate(AD_Language, "AD_Language");
		String okText = Msg.getMsg(AD_Language, "OK");
		String cancelText = Msg.getMsg(AD_Language, "Cancel");
		String storeTxt = Msg.getMsg(AD_Language, "SaveCookie");

		//  Document
		WDoc doc = WDoc.create (windowTitle);
		body b = doc.getBody();
		//  Clear Menu Frame
		b.addElement(WUtil.getClearFrame(WEnv.TARGET_MENU));

		//	Form - post to same URL
		String action = request.getRequestURI();
		form myForm = null;
		myForm = new form(action, form.post, form.ENC_DEFAULT).setName("Login1");
		myForm.setAcceptCharset(WEnv.CHARACTERSET);
		table table = new table().setAlign(AlignType.center);

		//	Username
		String userData = cProp.getProperty(P_USERNAME, "");
		tr line = new tr();
		label usrLabel = new label().setFor(P_USERNAME).addElement(usrText);
		usrLabel.setID("ID_"+P_USERNAME);
		line.addElement(new td().addElement(usrLabel).setAlign(AlignType.right));
		input usr = new input(input.text, P_USERNAME, userData).setSize(20).setMaxlength(30);
		usr.setID("ID_"+P_USERNAME);
		line.addElement(new td().addElement(usr).setAlign(AlignType.left));
		table.addElement(line);

		//  Password
		String pwdData = cProp.getProperty(P_PASSWORD, "");
		line = new tr();
		label pwdLabel = new label().setFor(P_PASSWORD).addElement(pwdText);
		pwdLabel.setID("ID_"+P_PASSWORD);
		line.addElement(new td().addElement(pwdLabel).setAlign(AlignType.right));
		input pwd = new input(input.password, P_PASSWORD, pwdData).setSize(20).setMaxlength(30);
		pwd.setID("ID_"+P_PASSWORD);
		line.addElement(new td().addElement(pwd).setAlign(AlignType.left));
		table.addElement(line);

		//	Language Pick
		String langData = cProp.getProperty(AD_Language);
		line = new tr();
		label langLabel = new label().setFor(P_LANGUAGE).addElement(lngText);
		langLabel.setID("ID_"+P_LANGUAGE);
		line.addElement(new td().addElement(langLabel).setAlign(AlignType.right));
		option options[] = new option[Language.getLanguageCount()];
		for (int i = 0; i < Language.getLanguageCount(); i++)
		{
			Language language = Language.getLanguage(i);
			options[i] = new option(language.getAD_Language()).addElement(language.getName());
			if (language.getAD_Language().equals(langData))
				options[i].setSelected(true);
			else
				options[i].setSelected(false);
		}
		line.addElement(new td().addElement(new select(P_LANGUAGE, options).setID("ID_"+P_LANGUAGE) ));
		table.addElement(line);

		//  Store Cookie
		String storeData = cProp.getProperty(P_STORE, "N");
		line = new tr();
		line.addElement(new td());
		input store = new input(input.checkbox, P_STORE, "Y").addElement(storeTxt).setChecked(storeData.equals("Y"));
		store.setID("ID_"+P_STORE);
		line.addElement(new td().addElement(store).setAlign(AlignType.left));
		table.addElement(line);

		//  ErrorMessage
		if (errorMessage != null && errorMessage.length() > 0)
		{
			line = new tr();
		//	line.addElement(new td());
			line.addElement(new td().setColSpan(2)
				.addElement(new font(HtmlColor.red, 4).addElement(new b(errorMessage))));   //  color, size
			table.addElement(line);
		}

		//  Finish
		line = new tr();
		input cancel = new input(input.reset, "Reset", cancelText);
		line.addElement(new td().addElement(cancel ));
		line.addElement(new td().addElement(new input(input.submit, P_SUBMIT, okText) ));
		table.addElement(line);
		//
		myForm.addElement(table);
		b.addElement(myForm);

		return doc;
	}   //  getUserPasswordPage


	/**
	 *  Create Second Page
	 *  @param cProp clinet properties
	 *  @param request request
	 *  @param roleOptions role options
	 *  @param errorMessage error message
	 *  @return WDoc page
	 */
	private WDoc createSecondPage(Properties cProp, HttpServletRequest request,
		option[] roleOptions, String errorMessage)
	{
		Log.trace(Log.l4_Data, "WLogin.createSecondPage", errorMessage);
		String AD_Language = cProp.getProperty(P_LANGUAGE, Language.getAD_Language(request.getLocale()));
		String windowTitle = Msg.getMsg(AD_Language, "LoginSuccess");
		//  Create Document
		WDoc doc = WDoc.create (windowTitle);
		body b = doc.getBody();

		//	Form - Get Menu
		String action = WEnv.getBaseDirectory("WMenu");
		form myForm = null;
		myForm = new form(action, form.post, form.ENC_DEFAULT).setName("Login2");
		myForm.setTarget(WEnv.TARGET_MENU);
		myForm.setAcceptCharset(WEnv.CHARACTERSET);
		table table = new table().setAlign(AlignType.center);

		//	Role Pick
		tr line = new tr();
		label roleLabel = new label().setFor(P_ROLE).addElement(Msg.translate(AD_Language, "AD_Role_ID"));
		line.addElement(new td().addElement(roleLabel).setAlign(AlignType.right));
		select role = new select(P_ROLE, roleOptions);
		role.setOnClick("fieldUpdate();");                  //  WFieldUpdate sets Client & Org
		line.addElement(new td().addElement(role));
		table.addElement(line);

		//	Client Pick
		line = new tr();
		label clientLabel = new label().setFor(P_CLIENT).addElement(Msg.translate(AD_Language, "AD_Client_ID"));
		line.addElement(new td().addElement(clientLabel).setAlign(AlignType.right));
		select client = new select(P_CLIENT);
		client.setOnClick("fieldUpdate();");                //  WFieldUpdate sets Warehouse
		line.addElement(new td().addElement(client));
		table.addElement(line);

		//	Org Pick
		line = new tr();
		label orgLabel = new label().setFor(P_ORG).addElement(Msg.translate(AD_Language, "AD_Org_ID"));
		line.addElement(new td().addElement(orgLabel).setAlign(AlignType.right));
		line.addElement(new td().addElement(new select(P_ORG) ));
		table.addElement(line);

		//  Warehouse
		line = new tr();
		label whLabel = new label().setFor(P_WAREHOUSE).addElement(Msg.translate(AD_Language, "M_Warehouse_ID"));
		line.addElement(new td().addElement(whLabel).setAlign(AlignType.right));
		line.addElement(new td().addElement(new select(P_WAREHOUSE) ));
		table.addElement(line);

		//  Date
		Language language = (Language)request.getSession().getAttribute(WEnv.SA_LANGUAGE);
		DateFormat df = DisplayType.getDateFormat(DisplayType.Date, language);
		String dateData = df.format(new java.util.Date());
		line = new tr();
		label dateLabel = new label().setFor(P_DATE).addElement(Msg.getMsg(AD_Language, "Date"));
		line.addElement(new td().addElement(dateLabel).setAlign(AlignType.right));
		input date = new input(input.text, P_DATE, dateData).setSize(10).setMaxlength(10);
		date.setID("ID_Date");
		line.addElement(new td().addElement(date).setAlign(AlignType.left));
		table.addElement(line);

		//  ErrorMessage
		if (errorMessage != null && errorMessage.length() > 0)
		{
			line = new tr();
			line.addElement(new td().addElement(new strong(errorMessage)).setColSpan(2).setAlign(AlignType.center));
			table.addElement(line);
		}

		//  Finish
		line = new tr();
		input cancel = new input(input.reset, "Reset", Msg.getMsg(AD_Language, "Cancel"));
		line.addElement(new td().addElement(cancel ));
		input submit = new input(input.submit, "Submit", Msg.getMsg(AD_Language, "OK"));
		submit.setOnClick("showLoadingMenu('" + WEnv.getBaseDirectory("") + "');");
		line.addElement(new td().addElement(submit));
		table.addElement(line);

		//  Note
		line = new tr();
		String note = Msg.getMsg(AD_Language, "WLoginBrowserNote");
		line.addElement(new td().addElement(note).setColSpan(2).setAlign(AlignType.center));
		table.addElement(line);
		//
		myForm.addElement(table);
		b.addElement(myForm);
		//
		String script = "document.Login2." + P_ROLE + ".click();";         //  init dependency updates
		b.addElement(new script(script));

		return doc;
	}   //  getSecondPage

}	//	WLogin
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
package org.compiere.www;

import javax.servlet.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import java.sql.*;
import java.text.*;
import java.security.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;

import org.compiere.util.*;

/**
 *  Web Login Page.
 *  <p>
 *  Page request:
 *  <pre>
 *  - Check database connection
 *  - LoginInfo from request?
 *      - Yes: DoLogin success ?
 *          - Yes: return (second) preferences page
 *          - No: return (first) user/password page
 *      - No: User Principal ?
 *          - Yes: DoLogin success ?
 *              - Yes: return (second) preferences page
 *              - No: return (first) user/password page
 *          - No: return (first) user/password page
 *  </pre>
 *
 *  @author Jorg Janke
 *  @version  $Id: WLogin.java,v 1.2 2003/01/20 05:42:07 jjanke Exp $
 */
public class WLogin extends HttpServlet
{
	/**
	 *	Initialize
	 *  @param config confif
	 *  @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		if (!WEnv.initWeb(config))
			throw new ServletException("WLogin.init");
	}	//	init

	/**
	 * Get Servlet information
	 * @return servlet info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Login";
	}	//	getServletInfo

	/**
	 *	Clean up
	 */
	public void destroy()
	{
		Log.trace(Log.l1_User, "WLogin.destroy");
		super.destroy();
	}	//	destroy


	/**
	 *	Process the HTTP Get request - forward to Post
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		Log.trace(Log.l1_User, "WLogin.doGet");
		doPost (request, response);
	}	//	doGet


	/**
	 *	Process the HTTP Post request.
	 *  <pre>
	 *  - Optionally create Session
	 *  - Check database connection
	 *  - LoginInfo from request?
	 *      - Yes: DoLogin success ?
	 *          - Yes: return (second) preferences page
	 *          - No: return (first) user/password page
	 *      - No: User Principal ?
	 *          - Yes: DoLogin success ?
	 *              - Yes: return (second) preferences page
	 *              - No: return (first) user/password page
	 *          - No: return (first) user/password page
	 *  </pre>
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		Log.trace(Log.l1_User, "WLogin.doPost");
		//  Create New Session
		HttpSession sess = request.getSession(true);
		sess.setMaxInactiveInterval(WEnv.TIMEOUT);

		//  Get/set Context
		Properties ctx = (Properties)sess.getAttribute(WEnv.SA_CONTEXT);
		if (ctx == null)
			ctx = new Properties();
		sess.setAttribute(WEnv.SA_CONTEXT, ctx);

		//  Get Cookie
		Properties cProp = WUtil.getCookieProprties(request);
		//  Language
		checkLanguage (request, sess, ctx, cProp);
		//  Page
		WDoc doc = null;

		//  Check DB connection
		if (!DB.isConnected())
		{
			String msg = Msg.getMsg(ctx, "WLoginNoDB");
			doc = WDoc.create (msg);
			if (msg.equals("WLoginNoDB"))
				msg = "No Database Connection";
			doc.getBody().addElement(new h1(msg));
		}

		//  Login Info from request?
		else
		{
			//  Get Parameters:     UserName/Password
			String usr = request.getParameter(P_USERNAME);
			String pwd = request.getParameter(P_PASSWORD);
			//  Get Principle
			Principal userPr = request.getUserPrincipal();
			Log.trace(Log.l4_Data, "Principal=" + userPr + "; User=" + usr);

			//  Login info not from request and not pre-authorized
			if (userPr == null && (usr == null || pwd == null))
				doc = createFirstPage (cProp, request, "");
			//  Login info from request or authorized
			else
			{
				KeyNamePair[] roles = null;
				//  Pre-authorized
				if (userPr != null)
				{
					roles = DB.login(ctx, userPr);
					usr = userPr.getName();
				}
				else
					roles = DB.login(ctx, usr, pwd);
				//
				if (roles == null)
					doc = createFirstPage(cProp, request, Msg.getMsg(ctx, "UserPwdError"));
				else
					doc = createSecondPage(cProp, request, WUtil.convertToOption(roles, null), "");
				//  Can we save Cookie ?
				if (request.getParameter(P_STORE) == null)
				{
					cProp.clear();                          //  erase all
				}
				else    //  Save Cookie Parameter
				{
					cProp.setProperty(P_USERNAME, usr);
					cProp.setProperty(P_STORE, "Y");
					cProp.setProperty(P_PASSWORD, pwd);     //  For test only
				}
			}
		}

		//
		WUtil.createResponse (request, response, this, cProp, doc, true);
	}	//	doPost

	//  Variable Names
	public static final String      P_USERNAME      = "User";
	private static final String     P_PASSWORD      = "Password";
	protected static final String   P_LANGUAGE      = Env.LANG;
	private static final String     P_SUBMIT        = "Submit";
	//  WMenu picks it up
	protected static final String   P_ROLE          = "AD_Role_ID";
	protected static final String   P_CLIENT        = "AD_Client_ID";
	protected static final String   P_ORG           = "AD_Org_ID";
	protected static final String   P_DATE          = "Date";
	protected static final String   P_WAREHOUSE     = "M_Warehouse_ID";
	protected static final String   P_ERRORMSG      = "ErrorMessage";
	protected static final String   P_STORE         = "SaveCookie";

	/*************************************************************************/

	/**
	 *  Set Language from request or session in.
	 *  - Properties
	 *  - Cookie
	 *  - Session
	 *  @param request request
	 *  @param sess sess
	 *  @param ctx context
	 *  @param cProp properties
	 */
	private static void checkLanguage (HttpServletRequest request,
		HttpSession sess, Properties ctx, Properties cProp)
	{
		//  Get/set Parameter:      Language
		String AD_Language = request.getParameter(P_LANGUAGE);
		if (AD_Language == null)
		{
			//  Check Cookie
			AD_Language = cProp.getProperty(P_LANGUAGE);
			if (AD_Language == null)
			{
				//  Check Request Locale
				Locale locale = request.getLocale();
				AD_Language = Language.getAD_Language (locale);
			}
		}
		if (AD_Language != null)
		{
			Language language = Language.getLanguage(AD_Language);
			Env.verifyLanguage(ctx, language);
			Env.setContext(ctx, Env.LANG, language.getAD_Language());
			cProp.setProperty(P_LANGUAGE, language.getAD_Language());
			sess.setAttribute(WEnv.SA_LANGUAGE, language);
		}
	}   //  checkLanguage

	/*************************************************************************/


	/**
	 *  First Login Page
	 *  @param cProp Login Cookie information for defaults
	 *  @param request request
	 *  @param errorMessage error message
	 *  @return WDoc page
	 */
	private WDoc createFirstPage(Properties cProp, HttpServletRequest request,
		String errorMessage)
	{
		Log.trace(Log.l4_Data, "WLogin.createFirstPage", errorMessage);
		String AD_Language = (cProp.getProperty(P_LANGUAGE, Language.getAD_Language(request.getLocale())));
		//
		String windowTitle = Msg.getMsg(AD_Language, "Login");
		String usrText = Msg.getMsg(AD_Language, "User");
		String pwdText = Msg.getMsg(AD_Language, "Password");
		String lngText = Msg.translate(AD_Language, "AD_Language");
		String okText = Msg.getMsg(AD_Language, "OK");
		String cancelText = Msg.getMsg(AD_Language, "Cancel");
		String storeTxt = Msg.getMsg(AD_Language, "SaveCookie");

		//  Document
		WDoc doc = WDoc.create (windowTitle);
		body b = doc.getBody();
		//  Clear Menu Frame
		b.addElement(WUtil.getClearFrame(WEnv.TARGET_MENU));

		//	Form - post to same URL
		String action = request.getRequestURI();
		form form = null;
		form = new form(action, form.post, form.ENC_DEFAULT).setName("Login1");
		form.setAcceptCharset(WEnv.CHARACTERSET);
		table table = new table().setAlign(AlignType.center);

		//	Username
		String userData = cProp.getProperty(P_USERNAME, "");
		tr line = new tr();
		label usrLabel = new label().setFor(P_USERNAME).addElement(usrText);
		usrLabel.setID("ID_"+P_USERNAME);
		line.addElement(new td().addElement(usrLabel).setAlign(AlignType.right));
		input usr = new input(input.text, P_USERNAME, userData).setSize(20).setMaxlength(30);
		usr.setID("ID_"+P_USERNAME);
		line.addElement(new td().addElement(usr).setAlign(AlignType.left));
		table.addElement(line);

		//  Password
		String pwdData = cProp.getProperty(P_PASSWORD, "");
		line = new tr();
		label pwdLabel = new label().setFor(P_PASSWORD).addElement(pwdText);
		pwdLabel.setID("ID_"+P_PASSWORD);
		line.addElement(new td().addElement(pwdLabel).setAlign(AlignType.right));
		input pwd = new input(input.password, P_PASSWORD, pwdData).setSize(20).setMaxlength(30);
		pwd.setID("ID_"+P_PASSWORD);
		line.addElement(new td().addElement(pwd).setAlign(AlignType.left));
		table.addElement(line);

		//	Language Pick
		String langData = cProp.getProperty(AD_Language);
		line = new tr();
		label langLabel = new label().setFor(P_LANGUAGE).addElement(lngText);
		langLabel.setID("ID_"+P_LANGUAGE);
		line.addElement(new td().addElement(langLabel).setAlign(AlignType.right));
		option options[] = new option[Language.getLanguageCount()];
		for (int i = 0; i < Language.getLanguageCount(); i++)
		{
			Language language = Language.getLanguage(i);
			options[i] = new option(language.getAD_Language()).addElement(language.getName());
			if (language.getAD_Language().equals(langData))
				options[i].setSelected(true);
			else
				options[i].setSelected(false);
		}
		line.addElement(new td().addElement(new select(P_LANGUAGE, options).setID("ID_"+P_LANGUAGE) ));
		table.addElement(line);

		//  Store Cookie
		String storeData = cProp.getProperty(P_STORE, "N");
		line = new tr();
		line.addElement(new td());
		input store = new input(input.checkbox, P_STORE, "Y").addElement(storeTxt).setChecked(storeData.equals("Y"));
		store.setID("ID_"+P_STORE);
		line.addElement(new td().addElement(store).setAlign(AlignType.left));
		table.addElement(line);

		//  ErrorMessage
		if (errorMessage != null && errorMessage.length() > 0)
		{
			line = new tr();
		//	line.addElement(new td());
			line.addElement(new td().setColSpan(2)
				.addElement(new font(HtmlColor.red, 4).addElement(new b(errorMessage))));   //  color, size
			table.addElement(line);
		}

		//  Finish
		line = new tr();
		input cancel = new input(input.reset, "Reset", cancelText);
		line.addElement(new td().addElement(cancel ));
		line.addElement(new td().addElement(new input(input.submit, P_SUBMIT, okText) ));
		table.addElement(line);
		//
		form.addElement(table);
		b.addElement(form);

		return doc;
	}   //  getUserPasswordPage


	/**
	 *  Create Second Page
	 *  @param cProp clinet properties
	 *  @param request request
	 *  @param roleOptions role options
	 *  @param errorMessage error message
	 *  @return WDoc page
	 */
	private WDoc createSecondPage(Properties cProp, HttpServletRequest request,
		option[] roleOptions, String errorMessage)
	{
		Log.trace(Log.l4_Data, "WLogin.createSecondPage", errorMessage);
		String AD_Language = cProp.getProperty(P_LANGUAGE, Language.getAD_Language(request.getLocale()));
		String windowTitle = Msg.getMsg(AD_Language, "LoginSuccess");
		//  Create Document
		WDoc doc = WDoc.create (windowTitle);
		body b = doc.getBody();

		//	Form - Get Menu
		String action = WEnv.getBaseDirectory("WMenu");
		form form = null;
		form = new form(action, form.post, form.ENC_DEFAULT).setName("Login2");
		form.setTarget(WEnv.TARGET_MENU);
		form.setAcceptCharset(WEnv.CHARACTERSET);
		table table = new table().setAlign(AlignType.center);

		//	Role Pick
		tr line = new tr();
		label roleLabel = new label().setFor(P_ROLE).addElement(Msg.translate(AD_Language, "AD_Role_ID"));
		line.addElement(new td().addElement(roleLabel).setAlign(AlignType.right));
		select role = new select(P_ROLE, roleOptions);
		role.setOnClick("fieldUpdate();");                  //  WFieldUpdate sets Client & Org
		line.addElement(new td().addElement(role));
		table.addElement(line);

		//	Client Pick
		line = new tr();
		label clientLabel = new label().setFor(P_CLIENT).addElement(Msg.translate(AD_Language, "AD_Client_ID"));
		line.addElement(new td().addElement(clientLabel).setAlign(AlignType.right));
		select client = new select(P_CLIENT);
		client.setOnClick("fieldUpdate();");                //  WFieldUpdate sets Warehouse
		line.addElement(new td().addElement(client));
		table.addElement(line);

		//	Org Pick
		line = new tr();
		label orgLabel = new label().setFor(P_ORG).addElement(Msg.translate(AD_Language, "AD_Org_ID"));
		line.addElement(new td().addElement(orgLabel).setAlign(AlignType.right));
		line.addElement(new td().addElement(new select(P_ORG) ));
		table.addElement(line);

		//  Warehouse
		line = new tr();
		label whLabel = new label().setFor(P_WAREHOUSE).addElement(Msg.translate(AD_Language, "M_Warehouse_ID"));
		line.addElement(new td().addElement(whLabel).setAlign(AlignType.right));
		line.addElement(new td().addElement(new select(P_WAREHOUSE) ));
		table.addElement(line);

		//  Date
		Language language = (Language)request.getSession().getAttribute(WEnv.SA_LANGUAGE);
		DateFormat df = DisplayType.getDateFormat(DisplayType.Date, language);
		String dateData = df.format(new java.util.Date());
		line = new tr();
		label dateLabel = new label().setFor(P_DATE).addElement(Msg.getMsg(AD_Language, "Date"));
		line.addElement(new td().addElement(dateLabel).setAlign(AlignType.right));
		input date = new input(input.text, P_DATE, dateData).setSize(10).setMaxlength(10);
		date.setID("ID_Date");
		line.addElement(new td().addElement(date).setAlign(AlignType.left));
		table.addElement(line);

		//  ErrorMessage
		if (errorMessage != null && errorMessage.length() > 0)
		{
			line = new tr();
			line.addElement(new td().addElement(new strong(errorMessage)).setColSpan(2).setAlign(AlignType.center));
			table.addElement(line);
		}

		//  Finish
		line = new tr();
		input cancel = new input(input.reset, "Reset", Msg.getMsg(AD_Language, "Cancel"));
		line.addElement(new td().addElement(cancel ));
		input submit = new input(input.submit, "Submit", Msg.getMsg(AD_Language, "OK"));
		submit.setOnClick("showLoadingMenu('" + WEnv.getBaseDirectory("") + "');");
		line.addElement(new td().addElement(submit));
		table.addElement(line);

		//  Note
		line = new tr();
		String note = Msg.getMsg(AD_Language, "WLoginBrowserNote");
		line.addElement(new td().addElement(note).setColSpan(2).setAlign(AlignType.center));
		table.addElement(line);
		//
		form.addElement(table);
		b.addElement(form);
		//
		String script = "document.Login2." + P_ROLE + ".click();";         //  init dependency updates
		b.addElement(new script(script));

		return doc;
	}   //  getSecondPage

}	//	WLogin
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
package org.compiere.www;

import java.io.*;
import java.security.*;
import java.text.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.compiere.util.*;

/**
 *  Web Login Page.
 *  <p>
 *  Page request:
 *  <pre>
 *  - Check database connection
 *  - LoginInfo from request?
 *      - Yes: DoLogin success ?
 *          - Yes: return (second) preferences page
 *          - No: return (first) user/password page
 *      - No: User Principal ?
 *          - Yes: DoLogin success ?
 *              - Yes: return (second) preferences page
 *              - No: return (first) user/password page
 *          - No: return (first) user/password page
 *  </pre>
 *
 *  @author Jorg Janke
 *  @version  $Id: WLogin.java,v 1.8 2004/09/10 02:54:23 jjanke Exp $
 */
public class WLogin extends HttpServlet
{
	/**	Logger			*/
	protected Logger	log = Logger.getCLogger(getClass());
	
	/**
	 *	Initialize
	 *  @param config confif
	 *  @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		if (!WebEnv.initWeb(config))
			throw new ServletException("WLogin.init");
	}	//	init

	/**
	 * Get Servlet information
	 * @return servlet info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Login";
	}	//	getServletInfo

	/**
	 *	Clean up
	 */
	public void destroy()
	{
		log.debug("destroy");
		super.destroy();
	}	//	destroy


	/**
	 *	Process the HTTP Get request - forward to Post
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		log.debug("doGet");
		doPost (request, response);
	}	//	doGet


	/**
	 *	Process the HTTP Post request.
	 *  <pre>
	 *  - Optionally create Session
	 *  - Check database connection
	 *  - LoginInfo from request?
	 *      - Yes: DoLogin success ?
	 *          - Yes: return (second) preferences page
	 *          - No: return (first) user/password page
	 *      - No: User Principal ?
	 *          - Yes: DoLogin success ?
	 *              - Yes: return (second) preferences page
	 *              - No: return (first) user/password page
	 *          - No: return (first) user/password page
	 *  </pre>
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		log.debug("doPost");
		//  Create New Session
		HttpSession sess = request.getSession(true);
		sess.setMaxInactiveInterval(WebEnv.TIMEOUT);

		//  Get/set Context
		Properties ctx = (Properties)sess.getAttribute(WebEnv.SA_CONTEXT);
		if (ctx == null)
			ctx = new Properties();
		sess.setAttribute(WebEnv.SA_CONTEXT, ctx);

		//  Get Cookie
		Properties cProp = WebUtil.getCookieProprties(request);
		//  Language
		checkLanguage (request, sess, ctx, cProp);
		//  Page
		WebDoc doc = null;

		//  Check DB connection
		if (!DB.isConnected())
		{
			String msg = Msg.getMsg(ctx, "WLoginNoDB");
			doc = WebDoc.create (msg);
			if (msg.equals("WLoginNoDB"))
				msg = "No Database Connection";
			doc.getBody().addElement(new h1(msg));
		}

		//  Login Info from request?
		else
		{
			//  Get Parameters:     UserName/Password
			String usr = request.getParameter(P_USERNAME);
			String pwd = request.getParameter(P_PASSWORD);
			//  Get Principle
			Principal userPr = request.getUserPrincipal();
			log.debug("doPost - Principal=" + userPr + "; User=" + usr);

			//  Login info not from request and not pre-authorized
			if (userPr == null && (usr == null || pwd == null))
				doc = createFirstPage (cProp, request, "");
			//  Login info from request or authorized
			else
			{
				KeyNamePair[] roles = null;
				//  Pre-authorized
				if (userPr != null)
				{
					roles = DB.login(ctx, userPr);
					usr = userPr.getName();
				}
				else
					roles = DB.login(ctx, usr, pwd);
				//
				if (roles == null)
					doc = createFirstPage(cProp, request, Msg.getMsg(ctx, "UserPwdError"));
				else
					doc = createSecondPage(cProp, request, WebUtil.convertToOption(roles, null), "");
				//  Can we save Cookie ?
				if (request.getParameter(P_STORE) == null)
				{
					cProp.clear();                          //  erase all
				}
				else    //  Save Cookie Parameter
				{
					cProp.setProperty(P_USERNAME, usr);
					cProp.setProperty(P_STORE, "Y");
					cProp.setProperty(P_PASSWORD, pwd);     //  For test only
				}
			}
		}

		//
		WebUtil.createResponse (request, response, this, cProp, doc, true);
	}	//	doPost

	//  Variable Names
	public static final String      P_USERNAME      = "User";
	private static final String     P_PASSWORD      = "Password";
	protected static final String   P_LANGUAGE      = Env.LANGUAGE;
	private static final String     P_SUBMIT        = "Submit";
	//  WMenu picks it up
	protected static final String   P_ROLE          = "AD_Role_ID";
	protected static final String   P_CLIENT        = "AD_Client_ID";
	protected static final String   P_ORG           = "AD_Org_ID";
	protected static final String   P_DATE          = "Date";
	protected static final String   P_WAREHOUSE     = "M_Warehouse_ID";
	protected static final String   P_ERRORMSG      = "ErrorMessage";
	protected static final String   P_STORE         = "SaveCookie";

	
	/**************************************************************************
	 *  Set Language from request or session in.
	 *  - Properties
	 *  - Cookie
	 *  - Session
	 *  @param request request
	 *  @param sess sess
	 *  @param ctx context
	 *  @param cProp properties
	 */
	private static void checkLanguage (HttpServletRequest request,
		HttpSession sess, Properties ctx, Properties cProp)
	{
		//  Get/set Parameter:      Language
		String AD_Language = request.getParameter(P_LANGUAGE);
		if (AD_Language == null)
		{
			//  Check Cookie
			AD_Language = cProp.getProperty(P_LANGUAGE);
			if (AD_Language == null)
			{
				//  Check Request Locale
				Locale locale = request.getLocale();
				AD_Language = Language.getAD_Language (locale);
			}
		}
		if (AD_Language != null)
		{
			Language language = Language.getLanguage(AD_Language);
			Env.verifyLanguage (ctx, language);
			Env.setContext(ctx, Env.LANGUAGE, language.getAD_Language());
			Msg.getMsg(ctx, "0");
			cProp.setProperty(P_LANGUAGE, language.getAD_Language());
			sess.setAttribute(WebEnv.SA_LANGUAGE, language);
		}
	}   //  checkLanguage

	
	/**************************************************************************
	 *  First Login Page
	 *  @param cProp Login Cookie information for defaults
	 *  @param request request
	 *  @param errorMessage error message
	 *  @return WDoc page
	 */
	private WebDoc createFirstPage(Properties cProp, HttpServletRequest request,
		String errorMessage)
	{
		log.debug ("createFirstPage - " + errorMessage);
		String AD_Language = (cProp.getProperty(P_LANGUAGE, Language.getAD_Language(request.getLocale())));
		//
		String windowTitle = Msg.getMsg(AD_Language, "Login");
		String usrText = Msg.getMsg(AD_Language, "User");
		String pwdText = Msg.getMsg(AD_Language, "Password");
		String lngText = Msg.translate(AD_Language, "AD_Language");
		String okText = Msg.getMsg(AD_Language, "OK");
		String cancelText = Msg.getMsg(AD_Language, "Cancel");
		String storeTxt = Msg.getMsg(AD_Language, "SaveCookie");

		//  Document
		WebDoc doc = WebDoc.create (windowTitle);
		body b = doc.getBody();
		//  Clear Menu Frame
		b.addElement(WebUtil.getClearFrame(WebEnv.TARGET_MENU));

		//	Form - post to same URL
		String action = request.getRequestURI();
		form myForm = null;
		myForm = new form(action, form.post, form.ENC_DEFAULT).setName("Login1");
		myForm.setAcceptCharset(WebEnv.CHARACTERSET);
		table table = new table().setAlign(AlignType.center);

		//	Username
		String userData = cProp.getProperty(P_USERNAME, "");
		tr line = new tr();
		label usrLabel = new label().setFor(P_USERNAME).addElement(usrText);
		usrLabel.setID("ID_"+P_USERNAME);
		line.addElement(new td().addElement(usrLabel).setAlign(AlignType.right));
		input usr = new input(input.text, P_USERNAME, userData).setSize(20).setMaxlength(30);
		usr.setID("ID_"+P_USERNAME);
		line.addElement(new td().addElement(usr).setAlign(AlignType.left));
		table.addElement(line);

		//  Password
		String pwdData = cProp.getProperty(P_PASSWORD, "");
		line = new tr();
		label pwdLabel = new label().setFor(P_PASSWORD).addElement(pwdText);
		pwdLabel.setID("ID_"+P_PASSWORD);
		line.addElement(new td().addElement(pwdLabel).setAlign(AlignType.right));
		input pwd = new input(input.password, P_PASSWORD, pwdData).setSize(20).setMaxlength(30);
		pwd.setID("ID_"+P_PASSWORD);
		line.addElement(new td().addElement(pwd).setAlign(AlignType.left));
		table.addElement(line);

		//	Language Pick
		String langData = cProp.getProperty(AD_Language);
		line = new tr();
		label langLabel = new label().setFor(P_LANGUAGE).addElement(lngText);
		langLabel.setID("ID_"+P_LANGUAGE);
		line.addElement(new td().addElement(langLabel).setAlign(AlignType.right));
		option options[] = new option[Language.getLanguageCount()];
		for (int i = 0; i < Language.getLanguageCount(); i++)
		{
			Language language = Language.getLanguage(i);
			options[i] = new option(language.getAD_Language()).addElement(language.getName());
			if (language.getAD_Language().equals(langData))
				options[i].setSelected(true);
			else
				options[i].setSelected(false);
		}
		line.addElement(new td().addElement(new select(P_LANGUAGE, options).setID("ID_"+P_LANGUAGE) ));
		table.addElement(line);

		//  Store Cookie
		String storeData = cProp.getProperty(P_STORE, "N");
		line = new tr();
		line.addElement(new td());
		input store = new input(input.checkbox, P_STORE, "Y").addElement(storeTxt).setChecked(storeData.equals("Y"));
		store.setID("ID_"+P_STORE);
		line.addElement(new td().addElement(store).setAlign(AlignType.left));
		table.addElement(line);

		//  ErrorMessage
		if (errorMessage != null && errorMessage.length() > 0)
		{
			line = new tr();
		//	line.addElement(new td());
			line.addElement(new td().setColSpan(2)
				.addElement(new font(HtmlColor.red, 4).addElement(new b(errorMessage))));   //  color, size
			table.addElement(line);
		}

		//  Finish
		line = new tr();
		input cancel = new input(input.reset, "Reset", cancelText);
		line.addElement(new td().addElement(cancel ));
		line.addElement(new td().addElement(new input(input.submit, P_SUBMIT, okText) ));
		table.addElement(line);
		//
		myForm.addElement(table);
		b.addElement(myForm);

		return doc;
	}   //  getUserPasswordPage


	/**
	 *  Create Second Page
	 *  @param cProp clinet properties
	 *  @param request request
	 *  @param roleOptions role options
	 *  @param errorMessage error message
	 *  @return WDoc page
	 */
	private WebDoc createSecondPage(Properties cProp, HttpServletRequest request,
		option[] roleOptions, String errorMessage)
	{
		log.debug("createSecondPage - " + errorMessage);
		String AD_Language = cProp.getProperty(P_LANGUAGE, Language.getAD_Language(request.getLocale()));
		String windowTitle = Msg.getMsg(AD_Language, "LoginSuccess");
		//  Create Document
		WebDoc doc = WebDoc.create (windowTitle);
		body b = doc.getBody();

		//	Form - Get Menu
		String action = WebEnv.getBaseDirectory("WMenu");
		form myForm = null;
		myForm = new form(action, form.post, form.ENC_DEFAULT).setName("Login2");
		myForm.setTarget(WebEnv.TARGET_MENU);
		myForm.setAcceptCharset(WebEnv.CHARACTERSET);
		table table = new table().setAlign(AlignType.center);

		//	Role Pick
		tr line = new tr();
		label roleLabel = new label().setFor(P_ROLE).addElement(Msg.translate(AD_Language, "AD_Role_ID"));
		line.addElement(new td().addElement(roleLabel).setAlign(AlignType.right));
		select role = new select(P_ROLE, roleOptions);
		role.setOnClick("fieldUpdate(this);");        		//  WFieldUpdate sets Client & Org
		line.addElement(new td().addElement(role));
		table.addElement(line);

		//	Client Pick
		line = new tr();
		label clientLabel = new label().setFor(P_CLIENT).addElement(Msg.translate(AD_Language, "AD_Client_ID"));
		line.addElement(new td().addElement(clientLabel).setAlign(AlignType.right));
		select client = new select(P_CLIENT);
		client.setOnClick("fieldUpdate(this);");          	//  WFieldUpdate sets Warehouse
		line.addElement(new td().addElement(client));
		table.addElement(line);

		//	Org Pick
		line = new tr();
		label orgLabel = new label().setFor(P_ORG).addElement(Msg.translate(AD_Language, "AD_Org_ID"));
		line.addElement(new td().addElement(orgLabel).setAlign(AlignType.right));
		line.addElement(new td().addElement(new select(P_ORG) ));
		table.addElement(line);

		//  Warehouse
		line = new tr();
		label whLabel = new label().setFor(P_WAREHOUSE).addElement(Msg.translate(AD_Language, "M_Warehouse_ID"));
		line.addElement(new td().addElement(whLabel).setAlign(AlignType.right));
		line.addElement(new td().addElement(new select(P_WAREHOUSE) ));
		table.addElement(line);

		//  Date
		Language language = (Language)request.getSession().getAttribute(WebEnv.SA_LANGUAGE);
		DateFormat df = DisplayType.getDateFormat(DisplayType.Date, language);
		String dateData = df.format(new java.util.Date());
		line = new tr();
		label dateLabel = new label().setFor(P_DATE).addElement(Msg.getMsg(AD_Language, "Date"));
		line.addElement(new td().addElement(dateLabel).setAlign(AlignType.right));
		input date = new input(input.text, P_DATE, dateData).setSize(10).setMaxlength(10);
		date.setID("ID_Date");
		line.addElement(new td().addElement(date).setAlign(AlignType.left));
		table.addElement(line);

		//  ErrorMessage
		if (errorMessage != null && errorMessage.length() > 0)
		{
			line = new tr();
			line.addElement(new td().addElement(new strong(errorMessage)).setColSpan(2).setAlign(AlignType.center));
			table.addElement(line);
		}

		//  Finish
		line = new tr();
		input cancel = new input(input.reset, "Reset", Msg.getMsg(AD_Language, "Cancel"));
		line.addElement(new td().addElement(cancel ));
		input submit = new input(input.submit, "Submit", Msg.getMsg(AD_Language, "OK"));
		submit.setOnClick("showLoadingMenu('" + WebEnv.getBaseDirectory("") + "');");
		line.addElement(new td().addElement(submit));
		table.addElement(line);

		//  Note
		line = new tr();
		String note = Msg.getMsg(AD_Language, "WLoginBrowserNote");
		line.addElement(new td().addElement(note).setColSpan(2).setAlign(AlignType.center));
		table.addElement(line);
		//
		myForm.addElement(table);
		b.addElement(myForm);
		//
		String script = "fieldUpdate(document.Login2." + P_ROLE + ");";	//  init dependency updates
		b.addElement(new script(script));

		return doc;
	}   //  getSecondPage

}	//	WLogin
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
package org.compiere.www;

import javax.servlet.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import java.sql.*;
import java.text.*;
import java.security.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;

import org.compiere.util.*;

/**
 *  Web Login Page.
 *  <p>
 *  Page request:
 *  <pre>
 *  - Check database connection
 *  - LoginInfo from request?
 *      - Yes: DoLogin success ?
 *          - Yes: return (second) preferences page
 *          - No: return (first) user/password page
 *      - No: User Principal ?
 *          - Yes: DoLogin success ?
 *              - Yes: return (second) preferences page
 *              - No: return (first) user/password page
 *          - No: return (first) user/password page
 *  </pre>
 *
 *  @author Jorg Janke
 *  @version  $Id: WLogin.java,v 1.2 2003/01/20 05:42:07 jjanke Exp $
 */
public class WLogin extends HttpServlet
{
	/**
	 *	Initialize
	 *  @param config confif
	 *  @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		if (!WEnv.initWeb(config))
			throw new ServletException("WLogin.init");
	}	//	init

	/**
	 * Get Servlet information
	 * @return servlet info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Login";
	}	//	getServletInfo

	/**
	 *	Clean up
	 */
	public void destroy()
	{
		Log.trace(Log.l1_User, "WLogin.destroy");
		super.destroy();
	}	//	destroy


	/**
	 *	Process the HTTP Get request - forward to Post
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		Log.trace(Log.l1_User, "WLogin.doGet");
		doPost (request, response);
	}	//	doGet


	/**
	 *	Process the HTTP Post request.
	 *  <pre>
	 *  - Optionally create Session
	 *  - Check database connection
	 *  - LoginInfo from request?
	 *      - Yes: DoLogin success ?
	 *          - Yes: return (second) preferences page
	 *          - No: return (first) user/password page
	 *      - No: User Principal ?
	 *          - Yes: DoLogin success ?
	 *              - Yes: return (second) preferences page
	 *              - No: return (first) user/password page
	 *          - No: return (first) user/password page
	 *  </pre>
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		Log.trace(Log.l1_User, "WLogin.doPost");
		//  Create New Session
		HttpSession sess = request.getSession(true);
		sess.setMaxInactiveInterval(WEnv.TIMEOUT);

		//  Get/set Context
		Properties ctx = (Properties)sess.getAttribute(WEnv.SA_CONTEXT);
		if (ctx == null)
			ctx = new Properties();
		sess.setAttribute(WEnv.SA_CONTEXT, ctx);

		//  Get Cookie
		Properties cProp = WUtil.getCookieProprties(request);
		//  Language
		checkLanguage (request, sess, ctx, cProp);
		//  Page
		WDoc doc = null;

		//  Check DB connection
		if (!DB.isConnected())
		{
			String msg = Msg.getMsg(ctx, "WLoginNoDB");
			doc = WDoc.create (msg);
			if (msg.equals("WLoginNoDB"))
				msg = "No Database Connection";
			doc.getBody().addElement(new h1(msg));
		}

		//  Login Info from request?
		else
		{
			//  Get Parameters:     UserName/Password
			String usr = request.getParameter(P_USERNAME);
			String pwd = request.getParameter(P_PASSWORD);
			//  Get Principle
			Principal userPr = request.getUserPrincipal();
			Log.trace(Log.l4_Data, "Principal=" + userPr + "; User=" + usr);

			//  Login info not from request and not pre-authorized
			if (userPr == null && (usr == null || pwd == null))
				doc = createFirstPage (cProp, request, "");
			//  Login info from request or authorized
			else
			{
				KeyNamePair[] roles = null;
				//  Pre-authorized
				if (userPr != null)
				{
					roles = DB.login(ctx, userPr);
					usr = userPr.getName();
				}
				else
					roles = DB.login(ctx, usr, pwd);
				//
				if (roles == null)
					doc = createFirstPage(cProp, request, Msg.getMsg(ctx, "UserPwdError"));
				else
					doc = createSecondPage(cProp, request, WUtil.convertToOption(roles, null), "");
				//  Can we save Cookie ?
				if (request.getParameter(P_STORE) == null)
				{
					cProp.clear();                          //  erase all
				}
				else    //  Save Cookie Parameter
				{
					cProp.setProperty(P_USERNAME, usr);
					cProp.setProperty(P_STORE, "Y");
					cProp.setProperty(P_PASSWORD, pwd);     //  For test only
				}
			}
		}

		//
		WUtil.createResponse (request, response, this, cProp, doc, true);
	}	//	doPost

	//  Variable Names
	public static final String      P_USERNAME      = "User";
	private static final String     P_PASSWORD      = "Password";
	protected static final String   P_LANGUAGE      = Env.LANG;
	private static final String     P_SUBMIT        = "Submit";
	//  WMenu picks it up
	protected static final String   P_ROLE          = "AD_Role_ID";
	protected static final String   P_CLIENT        = "AD_Client_ID";
	protected static final String   P_ORG           = "AD_Org_ID";
	protected static final String   P_DATE          = "Date";
	protected static final String   P_WAREHOUSE     = "M_Warehouse_ID";
	protected static final String   P_ERRORMSG      = "ErrorMessage";
	protected static final String   P_STORE         = "SaveCookie";

	/*************************************************************************/

	/**
	 *  Set Language from request or session in.
	 *  - Properties
	 *  - Cookie
	 *  - Session
	 *  @param request request
	 *  @param sess sess
	 *  @param ctx context
	 *  @param cProp properties
	 */
	private static void checkLanguage (HttpServletRequest request,
		HttpSession sess, Properties ctx, Properties cProp)
	{
		//  Get/set Parameter:      Language
		String AD_Language = request.getParameter(P_LANGUAGE);
		if (AD_Language == null)
		{
			//  Check Cookie
			AD_Language = cProp.getProperty(P_LANGUAGE);
			if (AD_Language == null)
			{
				//  Check Request Locale
				Locale locale = request.getLocale();
				AD_Language = Language.getAD_Language (locale);
			}
		}
		if (AD_Language != null)
		{
			Language language = Language.getLanguage(AD_Language);
			Env.verifyLanguage(ctx, language);
			Env.setContext(ctx, Env.LANG, language.getAD_Language());
			cProp.setProperty(P_LANGUAGE, language.getAD_Language());
			sess.setAttribute(WEnv.SA_LANGUAGE, language);
		}
	}   //  checkLanguage

	/*************************************************************************/


	/**
	 *  First Login Page
	 *  @param cProp Login Cookie information for defaults
	 *  @param request request
	 *  @param errorMessage error message
	 *  @return WDoc page
	 */
	private WDoc createFirstPage(Properties cProp, HttpServletRequest request,
		String errorMessage)
	{
		Log.trace(Log.l4_Data, "WLogin.createFirstPage", errorMessage);
		String AD_Language = (cProp.getProperty(P_LANGUAGE, Language.getAD_Language(request.getLocale())));
		//
		String windowTitle = Msg.getMsg(AD_Language, "Login");
		String usrText = Msg.getMsg(AD_Language, "User");
		String pwdText = Msg.getMsg(AD_Language, "Password");
		String lngText = Msg.translate(AD_Language, "AD_Language");
		String okText = Msg.getMsg(AD_Language, "OK");
		String cancelText = Msg.getMsg(AD_Language, "Cancel");
		String storeTxt = Msg.getMsg(AD_Language, "SaveCookie");

		//  Document
		WDoc doc = WDoc.create (windowTitle);
		body b = doc.getBody();
		//  Clear Menu Frame
		b.addElement(WUtil.getClearFrame(WEnv.TARGET_MENU));

		//	Form - post to same URL
		String action = request.getRequestURI();
		form form = null;
		form = new form(action, form.post, form.ENC_DEFAULT).setName("Login1");
		form.setAcceptCharset(WEnv.CHARACTERSET);
		table table = new table().setAlign(AlignType.center);

		//	Username
		String userData = cProp.getProperty(P_USERNAME, "");
		tr line = new tr();
		label usrLabel = new label().setFor(P_USERNAME).addElement(usrText);
		usrLabel.setID("ID_"+P_USERNAME);
		line.addElement(new td().addElement(usrLabel).setAlign(AlignType.right));
		input usr = new input(input.text, P_USERNAME, userData).setSize(20).setMaxlength(30);
		usr.setID("ID_"+P_USERNAME);
		line.addElement(new td().addElement(usr).setAlign(AlignType.left));
		table.addElement(line);

		//  Password
		String pwdData = cProp.getProperty(P_PASSWORD, "");
		line = new tr();
		label pwdLabel = new label().setFor(P_PASSWORD).addElement(pwdText);
		pwdLabel.setID("ID_"+P_PASSWORD);
		line.addElement(new td().addElement(pwdLabel).setAlign(AlignType.right));
		input pwd = new input(input.password, P_PASSWORD, pwdData).setSize(20).setMaxlength(30);
		pwd.setID("ID_"+P_PASSWORD);
		line.addElement(new td().addElement(pwd).setAlign(AlignType.left));
		table.addElement(line);

		//	Language Pick
		String langData = cProp.getProperty(AD_Language);
		line = new tr();
		label langLabel = new label().setFor(P_LANGUAGE).addElement(lngText);
		langLabel.setID("ID_"+P_LANGUAGE);
		line.addElement(new td().addElement(langLabel).setAlign(AlignType.right));
		option options[] = new option[Language.getLanguageCount()];
		for (int i = 0; i < Language.getLanguageCount(); i++)
		{
			Language language = Language.getLanguage(i);
			options[i] = new option(language.getAD_Language()).addElement(language.getName());
			if (language.getAD_Language().equals(langData))
				options[i].setSelected(true);
			else
				options[i].setSelected(false);
		}
		line.addElement(new td().addElement(new select(P_LANGUAGE, options).setID("ID_"+P_LANGUAGE) ));
		table.addElement(line);

		//  Store Cookie
		String storeData = cProp.getProperty(P_STORE, "N");
		line = new tr();
		line.addElement(new td());
		input store = new input(input.checkbox, P_STORE, "Y").addElement(storeTxt).setChecked(storeData.equals("Y"));
		store.setID("ID_"+P_STORE);
		line.addElement(new td().addElement(store).setAlign(AlignType.left));
		table.addElement(line);

		//  ErrorMessage
		if (errorMessage != null && errorMessage.length() > 0)
		{
			line = new tr();
		//	line.addElement(new td());
			line.addElement(new td().setColSpan(2)
				.addElement(new font(HtmlColor.red, 4).addElement(new b(errorMessage))));   //  color, size
			table.addElement(line);
		}

		//  Finish
		line = new tr();
		input cancel = new input(input.reset, "Reset", cancelText);
		line.addElement(new td().addElement(cancel ));
		line.addElement(new td().addElement(new input(input.submit, P_SUBMIT, okText) ));
		table.addElement(line);
		//
		form.addElement(table);
		b.addElement(form);

		return doc;
	}   //  getUserPasswordPage


	/**
	 *  Create Second Page
	 *  @param cProp clinet properties
	 *  @param request request
	 *  @param roleOptions role options
	 *  @param errorMessage error message
	 *  @return WDoc page
	 */
	private WDoc createSecondPage(Properties cProp, HttpServletRequest request,
		option[] roleOptions, String errorMessage)
	{
		Log.trace(Log.l4_Data, "WLogin.createSecondPage", errorMessage);
		String AD_Language = cProp.getProperty(P_LANGUAGE, Language.getAD_Language(request.getLocale()));
		String windowTitle = Msg.getMsg(AD_Language, "LoginSuccess");
		//  Create Document
		WDoc doc = WDoc.create (windowTitle);
		body b = doc.getBody();

		//	Form - Get Menu
		String action = WEnv.getBaseDirectory("WMenu");
		form form = null;
		form = new form(action, form.post, form.ENC_DEFAULT).setName("Login2");
		form.setTarget(WEnv.TARGET_MENU);
		form.setAcceptCharset(WEnv.CHARACTERSET);
		table table = new table().setAlign(AlignType.center);

		//	Role Pick
		tr line = new tr();
		label roleLabel = new label().setFor(P_ROLE).addElement(Msg.translate(AD_Language, "AD_Role_ID"));
		line.addElement(new td().addElement(roleLabel).setAlign(AlignType.right));
		select role = new select(P_ROLE, roleOptions);
		role.setOnClick("fieldUpdate();");                  //  WFieldUpdate sets Client & Org
		line.addElement(new td().addElement(role));
		table.addElement(line);

		//	Client Pick
		line = new tr();
		label clientLabel = new label().setFor(P_CLIENT).addElement(Msg.translate(AD_Language, "AD_Client_ID"));
		line.addElement(new td().addElement(clientLabel).setAlign(AlignType.right));
		select client = new select(P_CLIENT);
		client.setOnClick("fieldUpdate();");                //  WFieldUpdate sets Warehouse
		line.addElement(new td().addElement(client));
		table.addElement(line);

		//	Org Pick
		line = new tr();
		label orgLabel = new label().setFor(P_ORG).addElement(Msg.translate(AD_Language, "AD_Org_ID"));
		line.addElement(new td().addElement(orgLabel).setAlign(AlignType.right));
		line.addElement(new td().addElement(new select(P_ORG) ));
		table.addElement(line);

		//  Warehouse
		line = new tr();
		label whLabel = new label().setFor(P_WAREHOUSE).addElement(Msg.translate(AD_Language, "M_Warehouse_ID"));
		line.addElement(new td().addElement(whLabel).setAlign(AlignType.right));
		line.addElement(new td().addElement(new select(P_WAREHOUSE) ));
		table.addElement(line);

		//  Date
		Language language = (Language)request.getSession().getAttribute(WEnv.SA_LANGUAGE);
		DateFormat df = DisplayType.getDateFormat(DisplayType.Date, language);
		String dateData = df.format(new java.util.Date());
		line = new tr();
		label dateLabel = new label().setFor(P_DATE).addElement(Msg.getMsg(AD_Language, "Date"));
		line.addElement(new td().addElement(dateLabel).setAlign(AlignType.right));
		input date = new input(input.text, P_DATE, dateData).setSize(10).setMaxlength(10);
		date.setID("ID_Date");
		line.addElement(new td().addElement(date).setAlign(AlignType.left));
		table.addElement(line);

		//  ErrorMessage
		if (errorMessage != null && errorMessage.length() > 0)
		{
			line = new tr();
			line.addElement(new td().addElement(new strong(errorMessage)).setColSpan(2).setAlign(AlignType.center));
			table.addElement(line);
		}

		//  Finish
		line = new tr();
		input cancel = new input(input.reset, "Reset", Msg.getMsg(AD_Language, "Cancel"));
		line.addElement(new td().addElement(cancel ));
		input submit = new input(input.submit, "Submit", Msg.getMsg(AD_Language, "OK"));
		submit.setOnClick("showLoadingMenu('" + WEnv.getBaseDirectory("") + "');");
		line.addElement(new td().addElement(submit));
		table.addElement(line);

		//  Note
		line = new tr();
		String note = Msg.getMsg(AD_Language, "WLoginBrowserNote");
		line.addElement(new td().addElement(note).setColSpan(2).setAlign(AlignType.center));
		table.addElement(line);
		//
		form.addElement(table);
		b.addElement(form);
		//
		String script = "document.Login2." + P_ROLE + ".click();";         //  init dependency updates
		b.addElement(new script(script));

		return doc;
	}   //  getSecondPage

}	//	WLogin
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
package org.compiere.www;

import javax.servlet.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import java.sql.*;
import java.text.*;
import java.security.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;

import org.compiere.util.*;

/**
 *  Web Login Page.
 *  <p>
 *  Page request:
 *  <pre>
 *  - Check database connection
 *  - LoginInfo from request?
 *      - Yes: DoLogin success ?
 *          - Yes: return (second) preferences page
 *          - No: return (first) user/password page
 *      - No: User Principal ?
 *          - Yes: DoLogin success ?
 *              - Yes: return (second) preferences page
 *              - No: return (first) user/password page
 *          - No: return (first) user/password page
 *  </pre>
 *
 *  @author Jorg Janke
 *  @version  $Id: WLogin.java,v 1.2 2003/01/20 05:42:07 jjanke Exp $
 */
public class WLogin extends HttpServlet
{
	/**
	 *	Initialize
	 *  @param config confif
	 *  @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		if (!WEnv.initWeb(config))
			throw new ServletException("WLogin.init");
	}	//	init

	/**
	 * Get Servlet information
	 * @return servlet info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Login";
	}	//	getServletInfo

	/**
	 *	Clean up
	 */
	public void destroy()
	{
		Log.trace(Log.l1_User, "WLogin.destroy");
		super.destroy();
	}	//	destroy


	/**
	 *	Process the HTTP Get request - forward to Post
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		Log.trace(Log.l1_User, "WLogin.doGet");
		doPost (request, response);
	}	//	doGet


	/**
	 *	Process the HTTP Post request.
	 *  <pre>
	 *  - Optionally create Session
	 *  - Check database connection
	 *  - LoginInfo from request?
	 *      - Yes: DoLogin success ?
	 *          - Yes: return (second) preferences page
	 *          - No: return (first) user/password page
	 *      - No: User Principal ?
	 *          - Yes: DoLogin success ?
	 *              - Yes: return (second) preferences page
	 *              - No: return (first) user/password page
	 *          - No: return (first) user/password page
	 *  </pre>
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		Log.trace(Log.l1_User, "WLogin.doPost");
		//  Create New Session
		HttpSession sess = request.getSession(true);
		sess.setMaxInactiveInterval(WEnv.TIMEOUT);

		//  Get/set Context
		Properties ctx = (Properties)sess.getAttribute(WEnv.SA_CONTEXT);
		if (ctx == null)
			ctx = new Properties();
		sess.setAttribute(WEnv.SA_CONTEXT, ctx);

		//  Get Cookie
		Properties cProp = WUtil.getCookieProprties(request);
		//  Language
		checkLanguage (request, sess, ctx, cProp);
		//  Page
		WDoc doc = null;

		//  Check DB connection
		if (!DB.isConnected())
		{
			String msg = Msg.getMsg(ctx, "WLoginNoDB");
			doc = WDoc.create (msg);
			if (msg.equals("WLoginNoDB"))
				msg = "No Database Connection";
			doc.getBody().addElement(new h1(msg));
		}

		//  Login Info from request?
		else
		{
			//  Get Parameters:     UserName/Password
			String usr = request.getParameter(P_USERNAME);
			String pwd = request.getParameter(P_PASSWORD);
			//  Get Principle
			Principal userPr = request.getUserPrincipal();
			Log.trace(Log.l4_Data, "Principal=" + userPr + "; User=" + usr);

			//  Login info not from request and not pre-authorized
			if (userPr == null && (usr == null || pwd == null))
				doc = createFirstPage (cProp, request, "");
			//  Login info from request or authorized
			else
			{
				KeyNamePair[] roles = null;
				//  Pre-authorized
				if (userPr != null)
				{
					roles = DB.login(ctx, userPr);
					usr = userPr.getName();
				}
				else
					roles = DB.login(ctx, usr, pwd);
				//
				if (roles == null)
					doc = createFirstPage(cProp, request, Msg.getMsg(ctx, "UserPwdError"));
				else
					doc = createSecondPage(cProp, request, WUtil.convertToOption(roles, null), "");
				//  Can we save Cookie ?
				if (request.getParameter(P_STORE) == null)
				{
					cProp.clear();                          //  erase all
				}
				else    //  Save Cookie Parameter
				{
					cProp.setProperty(P_USERNAME, usr);
					cProp.setProperty(P_STORE, "Y");
					cProp.setProperty(P_PASSWORD, pwd);     //  For test only
				}
			}
		}

		//
		WUtil.createResponse (request, response, this, cProp, doc, true);
	}	//	doPost

	//  Variable Names
	public static final String      P_USERNAME      = "User";
	private static final String     P_PASSWORD      = "Password";
	protected static final String   P_LANGUAGE      = Env.LANG;
	private static final String     P_SUBMIT        = "Submit";
	//  WMenu picks it up
	protected static final String   P_ROLE          = "AD_Role_ID";
	protected static final String   P_CLIENT        = "AD_Client_ID";
	protected static final String   P_ORG           = "AD_Org_ID";
	protected static final String   P_DATE          = "Date";
	protected static final String   P_WAREHOUSE     = "M_Warehouse_ID";
	protected static final String   P_ERRORMSG      = "ErrorMessage";
	protected static final String   P_STORE         = "SaveCookie";

	/*************************************************************************/

	/**
	 *  Set Language from request or session in.
	 *  - Properties
	 *  - Cookie
	 *  - Session
	 *  @param request request
	 *  @param sess sess
	 *  @param ctx context
	 *  @param cProp properties
	 */
	private static void checkLanguage (HttpServletRequest request,
		HttpSession sess, Properties ctx, Properties cProp)
	{
		//  Get/set Parameter:      Language
		String AD_Language = request.getParameter(P_LANGUAGE);
		if (AD_Language == null)
		{
			//  Check Cookie
			AD_Language = cProp.getProperty(P_LANGUAGE);
			if (AD_Language == null)
			{
				//  Check Request Locale
				Locale locale = request.getLocale();
				AD_Language = Language.getAD_Language (locale);
			}
		}
		if (AD_Language != null)
		{
			Language language = Language.getLanguage(AD_Language);
			Env.verifyLanguage(ctx, language);
			Env.setContext(ctx, Env.LANG, language.getAD_Language());
			cProp.setProperty(P_LANGUAGE, language.getAD_Language());
			sess.setAttribute(WEnv.SA_LANGUAGE, language);
		}
	}   //  checkLanguage

	/*************************************************************************/


	/**
	 *  First Login Page
	 *  @param cProp Login Cookie information for defaults
	 *  @param request request
	 *  @param errorMessage error message
	 *  @return WDoc page
	 */
	private WDoc createFirstPage(Properties cProp, HttpServletRequest request,
		String errorMessage)
	{
		Log.trace(Log.l4_Data, "WLogin.createFirstPage", errorMessage);
		String AD_Language = (cProp.getProperty(P_LANGUAGE, Language.getAD_Language(request.getLocale())));
		//
		String windowTitle = Msg.getMsg(AD_Language, "Login");
		String usrText = Msg.getMsg(AD_Language, "User");
		String pwdText = Msg.getMsg(AD_Language, "Password");
		String lngText = Msg.translate(AD_Language, "AD_Language");
		String okText = Msg.getMsg(AD_Language, "OK");
		String cancelText = Msg.getMsg(AD_Language, "Cancel");
		String storeTxt = Msg.getMsg(AD_Language, "SaveCookie");

		//  Document
		WDoc doc = WDoc.create (windowTitle);
		body b = doc.getBody();
		//  Clear Menu Frame
		b.addElement(WUtil.getClearFrame(WEnv.TARGET_MENU));

		//	Form - post to same URL
		String action = request.getRequestURI();
		form form = null;
		form = new form(action, form.post, form.ENC_DEFAULT).setName("Login1");
		form.setAcceptCharset(WEnv.CHARACTERSET);
		table table = new table().setAlign(AlignType.center);

		//	Username
		String userData = cProp.getProperty(P_USERNAME, "");
		tr line = new tr();
		label usrLabel = new label().setFor(P_USERNAME).addElement(usrText);
		usrLabel.setID("ID_"+P_USERNAME);
		line.addElement(new td().addElement(usrLabel).setAlign(AlignType.right));
		input usr = new input(input.text, P_USERNAME, userData).setSize(20).setMaxlength(30);
		usr.setID("ID_"+P_USERNAME);
		line.addElement(new td().addElement(usr).setAlign(AlignType.left));
		table.addElement(line);

		//  Password
		String pwdData = cProp.getProperty(P_PASSWORD, "");
		line = new tr();
		label pwdLabel = new label().setFor(P_PASSWORD).addElement(pwdText);
		pwdLabel.setID("ID_"+P_PASSWORD);
		line.addElement(new td().addElement(pwdLabel).setAlign(AlignType.right));
		input pwd = new input(input.password, P_PASSWORD, pwdData).setSize(20).setMaxlength(30);
		pwd.setID("ID_"+P_PASSWORD);
		line.addElement(new td().addElement(pwd).setAlign(AlignType.left));
		table.addElement(line);

		//	Language Pick
		String langData = cProp.getProperty(AD_Language);
		line = new tr();
		label langLabel = new label().setFor(P_LANGUAGE).addElement(lngText);
		langLabel.setID("ID_"+P_LANGUAGE);
		line.addElement(new td().addElement(langLabel).setAlign(AlignType.right));
		option options[] = new option[Language.getLanguageCount()];
		for (int i = 0; i < Language.getLanguageCount(); i++)
		{
			Language language = Language.getLanguage(i);
			options[i] = new option(language.getAD_Language()).addElement(language.getName());
			if (language.getAD_Language().equals(langData))
				options[i].setSelected(true);
			else
				options[i].setSelected(false);
		}
		line.addElement(new td().addElement(new select(P_LANGUAGE, options).setID("ID_"+P_LANGUAGE) ));
		table.addElement(line);

		//  Store Cookie
		String storeData = cProp.getProperty(P_STORE, "N");
		line = new tr();
		line.addElement(new td());
		input store = new input(input.checkbox, P_STORE, "Y").addElement(storeTxt).setChecked(storeData.equals("Y"));
		store.setID("ID_"+P_STORE);
		line.addElement(new td().addElement(store).setAlign(AlignType.left));
		table.addElement(line);

		//  ErrorMessage
		if (errorMessage != null && errorMessage.length() > 0)
		{
			line = new tr();
		//	line.addElement(new td());
			line.addElement(new td().setColSpan(2)
				.addElement(new font(HtmlColor.red, 4).addElement(new b(errorMessage))));   //  color, size
			table.addElement(line);
		}

		//  Finish
		line = new tr();
		input cancel = new input(input.reset, "Reset", cancelText);
		line.addElement(new td().addElement(cancel ));
		line.addElement(new td().addElement(new input(input.submit, P_SUBMIT, okText) ));
		table.addElement(line);
		//
		form.addElement(table);
		b.addElement(form);

		return doc;
	}   //  getUserPasswordPage


	/**
	 *  Create Second Page
	 *  @param cProp clinet properties
	 *  @param request request
	 *  @param roleOptions role options
	 *  @param errorMessage error message
	 *  @return WDoc page
	 */
	private WDoc createSecondPage(Properties cProp, HttpServletRequest request,
		option[] roleOptions, String errorMessage)
	{
		Log.trace(Log.l4_Data, "WLogin.createSecondPage", errorMessage);
		String AD_Language = cProp.getProperty(P_LANGUAGE, Language.getAD_Language(request.getLocale()));
		String windowTitle = Msg.getMsg(AD_Language, "LoginSuccess");
		//  Create Document
		WDoc doc = WDoc.create (windowTitle);
		body b = doc.getBody();

		//	Form - Get Menu
		String action = WEnv.getBaseDirectory("WMenu");
		form form = null;
		form = new form(action, form.post, form.ENC_DEFAULT).setName("Login2");
		form.setTarget(WEnv.TARGET_MENU);
		form.setAcceptCharset(WEnv.CHARACTERSET);
		table table = new table().setAlign(AlignType.center);

		//	Role Pick
		tr line = new tr();
		label roleLabel = new label().setFor(P_ROLE).addElement(Msg.translate(AD_Language, "AD_Role_ID"));
		line.addElement(new td().addElement(roleLabel).setAlign(AlignType.right));
		select role = new select(P_ROLE, roleOptions);
		role.setOnClick("fieldUpdate();");                  //  WFieldUpdate sets Client & Org
		line.addElement(new td().addElement(role));
		table.addElement(line);

		//	Client Pick
		line = new tr();
		label clientLabel = new label().setFor(P_CLIENT).addElement(Msg.translate(AD_Language, "AD_Client_ID"));
		line.addElement(new td().addElement(clientLabel).setAlign(AlignType.right));
		select client = new select(P_CLIENT);
		client.setOnClick("fieldUpdate();");                //  WFieldUpdate sets Warehouse
		line.addElement(new td().addElement(client));
		table.addElement(line);

		//	Org Pick
		line = new tr();
		label orgLabel = new label().setFor(P_ORG).addElement(Msg.translate(AD_Language, "AD_Org_ID"));
		line.addElement(new td().addElement(orgLabel).setAlign(AlignType.right));
		line.addElement(new td().addElement(new select(P_ORG) ));
		table.addElement(line);

		//  Warehouse
		line = new tr();
		label whLabel = new label().setFor(P_WAREHOUSE).addElement(Msg.translate(AD_Language, "M_Warehouse_ID"));
		line.addElement(new td().addElement(whLabel).setAlign(AlignType.right));
		line.addElement(new td().addElement(new select(P_WAREHOUSE) ));
		table.addElement(line);

		//  Date
		Language language = (Language)request.getSession().getAttribute(WEnv.SA_LANGUAGE);
		DateFormat df = DisplayType.getDateFormat(DisplayType.Date, language);
		String dateData = df.format(new java.util.Date());
		line = new tr();
		label dateLabel = new label().setFor(P_DATE).addElement(Msg.getMsg(AD_Language, "Date"));
		line.addElement(new td().addElement(dateLabel).setAlign(AlignType.right));
		input date = new input(input.text, P_DATE, dateData).setSize(10).setMaxlength(10);
		date.setID("ID_Date");
		line.addElement(new td().addElement(date).setAlign(AlignType.left));
		table.addElement(line);

		//  ErrorMessage
		if (errorMessage != null && errorMessage.length() > 0)
		{
			line = new tr();
			line.addElement(new td().addElement(new strong(errorMessage)).setColSpan(2).setAlign(AlignType.center));
			table.addElement(line);
		}

		//  Finish
		line = new tr();
		input cancel = new input(input.reset, "Reset", Msg.getMsg(AD_Language, "Cancel"));
		line.addElement(new td().addElement(cancel ));
		input submit = new input(input.submit, "Submit", Msg.getMsg(AD_Language, "OK"));
		submit.setOnClick("showLoadingMenu('" + WEnv.getBaseDirectory("") + "');");
		line.addElement(new td().addElement(submit));
		table.addElement(line);

		//  Note
		line = new tr();
		String note = Msg.getMsg(AD_Language, "WLoginBrowserNote");
		line.addElement(new td().addElement(note).setColSpan(2).setAlign(AlignType.center));
		table.addElement(line);
		//
		form.addElement(table);
		b.addElement(form);
		//
		String script = "document.Login2." + P_ROLE + ".click();";         //  init dependency updates
		b.addElement(new script(script));

		return doc;
	}   //  getSecondPage

}	//	WLogin
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
package org.compiere.www;

import javax.servlet.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import java.sql.*;
import java.text.*;
import java.security.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;

import org.compiere.util.*;

/**
 *  Web Login Page.
 *  <p>
 *  Page request:
 *  <pre>
 *  - Check database connection
 *  - LoginInfo from request?
 *      - Yes: DoLogin success ?
 *          - Yes: return (second) preferences page
 *          - No: return (first) user/password page
 *      - No: User Principal ?
 *          - Yes: DoLogin success ?
 *              - Yes: return (second) preferences page
 *              - No: return (first) user/password page
 *          - No: return (first) user/password page
 *  </pre>
 *
 *  @author Jorg Janke
 *  @version  $Id: WLogin.java,v 1.3 2003/09/29 01:34:27 jjanke Exp $
 */
public class WLogin extends HttpServlet
{
	/**
	 *	Initialize
	 *  @param config confif
	 *  @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		if (!WEnv.initWeb(config))
			throw new ServletException("WLogin.init");
	}	//	init

	/**
	 * Get Servlet information
	 * @return servlet info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Login";
	}	//	getServletInfo

	/**
	 *	Clean up
	 */
	public void destroy()
	{
		Log.trace(Log.l1_User, "WLogin.destroy");
		super.destroy();
	}	//	destroy


	/**
	 *	Process the HTTP Get request - forward to Post
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		Log.trace(Log.l1_User, "WLogin.doGet");
		doPost (request, response);
	}	//	doGet


	/**
	 *	Process the HTTP Post request.
	 *  <pre>
	 *  - Optionally create Session
	 *  - Check database connection
	 *  - LoginInfo from request?
	 *      - Yes: DoLogin success ?
	 *          - Yes: return (second) preferences page
	 *          - No: return (first) user/password page
	 *      - No: User Principal ?
	 *          - Yes: DoLogin success ?
	 *              - Yes: return (second) preferences page
	 *              - No: return (first) user/password page
	 *          - No: return (first) user/password page
	 *  </pre>
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		Log.trace(Log.l1_User, "WLogin.doPost");
		//  Create New Session
		HttpSession sess = request.getSession(true);
		sess.setMaxInactiveInterval(WEnv.TIMEOUT);

		//  Get/set Context
		Properties ctx = (Properties)sess.getAttribute(WEnv.SA_CONTEXT);
		if (ctx == null)
			ctx = new Properties();
		sess.setAttribute(WEnv.SA_CONTEXT, ctx);

		//  Get Cookie
		Properties cProp = WUtil.getCookieProprties(request);
		//  Language
		checkLanguage (request, sess, ctx, cProp);
		//  Page
		WDoc doc = null;

		//  Check DB connection
		if (!DB.isConnected())
		{
			String msg = Msg.getMsg(ctx, "WLoginNoDB");
			doc = WDoc.create (msg);
			if (msg.equals("WLoginNoDB"))
				msg = "No Database Connection";
			doc.getBody().addElement(new h1(msg));
		}

		//  Login Info from request?
		else
		{
			//  Get Parameters:     UserName/Password
			String usr = request.getParameter(P_USERNAME);
			String pwd = request.getParameter(P_PASSWORD);
			//  Get Principle
			Principal userPr = request.getUserPrincipal();
			Log.trace(Log.l4_Data, "Principal=" + userPr + "; User=" + usr);

			//  Login info not from request and not pre-authorized
			if (userPr == null && (usr == null || pwd == null))
				doc = createFirstPage (cProp, request, "");
			//  Login info from request or authorized
			else
			{
				KeyNamePair[] roles = null;
				//  Pre-authorized
				if (userPr != null)
				{
					roles = DB.login(ctx, userPr);
					usr = userPr.getName();
				}
				else
					roles = DB.login(ctx, usr, pwd);
				//
				if (roles == null)
					doc = createFirstPage(cProp, request, Msg.getMsg(ctx, "UserPwdError"));
				else
					doc = createSecondPage(cProp, request, WUtil.convertToOption(roles, null), "");
				//  Can we save Cookie ?
				if (request.getParameter(P_STORE) == null)
				{
					cProp.clear();                          //  erase all
				}
				else    //  Save Cookie Parameter
				{
					cProp.setProperty(P_USERNAME, usr);
					cProp.setProperty(P_STORE, "Y");
					cProp.setProperty(P_PASSWORD, pwd);     //  For test only
				}
			}
		}

		//
		WUtil.createResponse (request, response, this, cProp, doc, true);
	}	//	doPost

	//  Variable Names
	public static final String      P_USERNAME      = "User";
	private static final String     P_PASSWORD      = "Password";
	protected static final String   P_LANGUAGE      = Env.LANG;
	private static final String     P_SUBMIT        = "Submit";
	//  WMenu picks it up
	protected static final String   P_ROLE          = "AD_Role_ID";
	protected static final String   P_CLIENT        = "AD_Client_ID";
	protected static final String   P_ORG           = "AD_Org_ID";
	protected static final String   P_DATE          = "Date";
	protected static final String   P_WAREHOUSE     = "M_Warehouse_ID";
	protected static final String   P_ERRORMSG      = "ErrorMessage";
	protected static final String   P_STORE         = "SaveCookie";

	/*************************************************************************/

	/**
	 *  Set Language from request or session in.
	 *  - Properties
	 *  - Cookie
	 *  - Session
	 *  @param request request
	 *  @param sess sess
	 *  @param ctx context
	 *  @param cProp properties
	 */
	private static void checkLanguage (HttpServletRequest request,
		HttpSession sess, Properties ctx, Properties cProp)
	{
		//  Get/set Parameter:      Language
		String AD_Language = request.getParameter(P_LANGUAGE);
		if (AD_Language == null)
		{
			//  Check Cookie
			AD_Language = cProp.getProperty(P_LANGUAGE);
			if (AD_Language == null)
			{
				//  Check Request Locale
				Locale locale = request.getLocale();
				AD_Language = Language.getAD_Language (locale);
			}
		}
		if (AD_Language != null)
		{
			Language language = Language.getLanguage(AD_Language);
			Env.verifyLanguage(ctx, language);
			Env.setContext(ctx, Env.LANG, language.getAD_Language());
			cProp.setProperty(P_LANGUAGE, language.getAD_Language());
			sess.setAttribute(WEnv.SA_LANGUAGE, language);
		}
	}   //  checkLanguage

	/*************************************************************************/


	/**
	 *  First Login Page
	 *  @param cProp Login Cookie information for defaults
	 *  @param request request
	 *  @param errorMessage error message
	 *  @return WDoc page
	 */
	private WDoc createFirstPage(Properties cProp, HttpServletRequest request,
		String errorMessage)
	{
		Log.trace(Log.l4_Data, "WLogin.createFirstPage", errorMessage);
		String AD_Language = (cProp.getProperty(P_LANGUAGE, Language.getAD_Language(request.getLocale())));
		//
		String windowTitle = Msg.getMsg(AD_Language, "Login");
		String usrText = Msg.getMsg(AD_Language, "User");
		String pwdText = Msg.getMsg(AD_Language, "Password");
		String lngText = Msg.translate(AD_Language, "AD_Language");
		String okText = Msg.getMsg(AD_Language, "OK");
		String cancelText = Msg.getMsg(AD_Language, "Cancel");
		String storeTxt = Msg.getMsg(AD_Language, "SaveCookie");

		//  Document
		WDoc doc = WDoc.create (windowTitle);
		body b = doc.getBody();
		//  Clear Menu Frame
		b.addElement(WUtil.getClearFrame(WEnv.TARGET_MENU));

		//	Form - post to same URL
		String action = request.getRequestURI();
		form myForm = null;
		myForm = new form(action, form.post, form.ENC_DEFAULT).setName("Login1");
		myForm.setAcceptCharset(WEnv.CHARACTERSET);
		table table = new table().setAlign(AlignType.center);

		//	Username
		String userData = cProp.getProperty(P_USERNAME, "");
		tr line = new tr();
		label usrLabel = new label().setFor(P_USERNAME).addElement(usrText);
		usrLabel.setID("ID_"+P_USERNAME);
		line.addElement(new td().addElement(usrLabel).setAlign(AlignType.right));
		input usr = new input(input.text, P_USERNAME, userData).setSize(20).setMaxlength(30);
		usr.setID("ID_"+P_USERNAME);
		line.addElement(new td().addElement(usr).setAlign(AlignType.left));
		table.addElement(line);

		//  Password
		String pwdData = cProp.getProperty(P_PASSWORD, "");
		line = new tr();
		label pwdLabel = new label().setFor(P_PASSWORD).addElement(pwdText);
		pwdLabel.setID("ID_"+P_PASSWORD);
		line.addElement(new td().addElement(pwdLabel).setAlign(AlignType.right));
		input pwd = new input(input.password, P_PASSWORD, pwdData).setSize(20).setMaxlength(30);
		pwd.setID("ID_"+P_PASSWORD);
		line.addElement(new td().addElement(pwd).setAlign(AlignType.left));
		table.addElement(line);

		//	Language Pick
		String langData = cProp.getProperty(AD_Language);
		line = new tr();
		label langLabel = new label().setFor(P_LANGUAGE).addElement(lngText);
		langLabel.setID("ID_"+P_LANGUAGE);
		line.addElement(new td().addElement(langLabel).setAlign(AlignType.right));
		option options[] = new option[Language.getLanguageCount()];
		for (int i = 0; i < Language.getLanguageCount(); i++)
		{
			Language language = Language.getLanguage(i);
			options[i] = new option(language.getAD_Language()).addElement(language.getName());
			if (language.getAD_Language().equals(langData))
				options[i].setSelected(true);
			else
				options[i].setSelected(false);
		}
		line.addElement(new td().addElement(new select(P_LANGUAGE, options).setID("ID_"+P_LANGUAGE) ));
		table.addElement(line);

		//  Store Cookie
		String storeData = cProp.getProperty(P_STORE, "N");
		line = new tr();
		line.addElement(new td());
		input store = new input(input.checkbox, P_STORE, "Y").addElement(storeTxt).setChecked(storeData.equals("Y"));
		store.setID("ID_"+P_STORE);
		line.addElement(new td().addElement(store).setAlign(AlignType.left));
		table.addElement(line);

		//  ErrorMessage
		if (errorMessage != null && errorMessage.length() > 0)
		{
			line = new tr();
		//	line.addElement(new td());
			line.addElement(new td().setColSpan(2)
				.addElement(new font(HtmlColor.red, 4).addElement(new b(errorMessage))));   //  color, size
			table.addElement(line);
		}

		//  Finish
		line = new tr();
		input cancel = new input(input.reset, "Reset", cancelText);
		line.addElement(new td().addElement(cancel ));
		line.addElement(new td().addElement(new input(input.submit, P_SUBMIT, okText) ));
		table.addElement(line);
		//
		myForm.addElement(table);
		b.addElement(myForm);

		return doc;
	}   //  getUserPasswordPage


	/**
	 *  Create Second Page
	 *  @param cProp clinet properties
	 *  @param request request
	 *  @param roleOptions role options
	 *  @param errorMessage error message
	 *  @return WDoc page
	 */
	private WDoc createSecondPage(Properties cProp, HttpServletRequest request,
		option[] roleOptions, String errorMessage)
	{
		Log.trace(Log.l4_Data, "WLogin.createSecondPage", errorMessage);
		String AD_Language = cProp.getProperty(P_LANGUAGE, Language.getAD_Language(request.getLocale()));
		String windowTitle = Msg.getMsg(AD_Language, "LoginSuccess");
		//  Create Document
		WDoc doc = WDoc.create (windowTitle);
		body b = doc.getBody();

		//	Form - Get Menu
		String action = WEnv.getBaseDirectory("WMenu");
		form myForm = null;
		myForm = new form(action, form.post, form.ENC_DEFAULT).setName("Login2");
		myForm.setTarget(WEnv.TARGET_MENU);
		myForm.setAcceptCharset(WEnv.CHARACTERSET);
		table table = new table().setAlign(AlignType.center);

		//	Role Pick
		tr line = new tr();
		label roleLabel = new label().setFor(P_ROLE).addElement(Msg.translate(AD_Language, "AD_Role_ID"));
		line.addElement(new td().addElement(roleLabel).setAlign(AlignType.right));
		select role = new select(P_ROLE, roleOptions);
		role.setOnClick("fieldUpdate();");                  //  WFieldUpdate sets Client & Org
		line.addElement(new td().addElement(role));
		table.addElement(line);

		//	Client Pick
		line = new tr();
		label clientLabel = new label().setFor(P_CLIENT).addElement(Msg.translate(AD_Language, "AD_Client_ID"));
		line.addElement(new td().addElement(clientLabel).setAlign(AlignType.right));
		select client = new select(P_CLIENT);
		client.setOnClick("fieldUpdate();");                //  WFieldUpdate sets Warehouse
		line.addElement(new td().addElement(client));
		table.addElement(line);

		//	Org Pick
		line = new tr();
		label orgLabel = new label().setFor(P_ORG).addElement(Msg.translate(AD_Language, "AD_Org_ID"));
		line.addElement(new td().addElement(orgLabel).setAlign(AlignType.right));
		line.addElement(new td().addElement(new select(P_ORG) ));
		table.addElement(line);

		//  Warehouse
		line = new tr();
		label whLabel = new label().setFor(P_WAREHOUSE).addElement(Msg.translate(AD_Language, "M_Warehouse_ID"));
		line.addElement(new td().addElement(whLabel).setAlign(AlignType.right));
		line.addElement(new td().addElement(new select(P_WAREHOUSE) ));
		table.addElement(line);

		//  Date
		Language language = (Language)request.getSession().getAttribute(WEnv.SA_LANGUAGE);
		DateFormat df = DisplayType.getDateFormat(DisplayType.Date, language);
		String dateData = df.format(new java.util.Date());
		line = new tr();
		label dateLabel = new label().setFor(P_DATE).addElement(Msg.getMsg(AD_Language, "Date"));
		line.addElement(new td().addElement(dateLabel).setAlign(AlignType.right));
		input date = new input(input.text, P_DATE, dateData).setSize(10).setMaxlength(10);
		date.setID("ID_Date");
		line.addElement(new td().addElement(date).setAlign(AlignType.left));
		table.addElement(line);

		//  ErrorMessage
		if (errorMessage != null && errorMessage.length() > 0)
		{
			line = new tr();
			line.addElement(new td().addElement(new strong(errorMessage)).setColSpan(2).setAlign(AlignType.center));
			table.addElement(line);
		}

		//  Finish
		line = new tr();
		input cancel = new input(input.reset, "Reset", Msg.getMsg(AD_Language, "Cancel"));
		line.addElement(new td().addElement(cancel ));
		input submit = new input(input.submit, "Submit", Msg.getMsg(AD_Language, "OK"));
		submit.setOnClick("showLoadingMenu('" + WEnv.getBaseDirectory("") + "');");
		line.addElement(new td().addElement(submit));
		table.addElement(line);

		//  Note
		line = new tr();
		String note = Msg.getMsg(AD_Language, "WLoginBrowserNote");
		line.addElement(new td().addElement(note).setColSpan(2).setAlign(AlignType.center));
		table.addElement(line);
		//
		myForm.addElement(table);
		b.addElement(myForm);
		//
		String script = "document.Login2." + P_ROLE + ".click();";         //  init dependency updates
		b.addElement(new script(script));

		return doc;
	}   //  getSecondPage

}	//	WLogin
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
package org.compiere.www;

import javax.servlet.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import java.text.*;
import java.security.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;

import org.compiere.util.*;

/**
 *  Web Login Page.
 *  <p>
 *  Page request:
 *  <pre>
 *  - Check database connection
 *  - LoginInfo from request?
 *      - Yes: DoLogin success ?
 *          - Yes: return (second) preferences page
 *          - No: return (first) user/password page
 *      - No: User Principal ?
 *          - Yes: DoLogin success ?
 *              - Yes: return (second) preferences page
 *              - No: return (first) user/password page
 *          - No: return (first) user/password page
 *  </pre>
 *
 *  @author Jorg Janke
 *  @version  $Id: WLogin.java,v 1.5 2004/03/06 07:16:14 jjanke Exp $
 */
public class WLogin extends HttpServlet
{
	/**
	 *	Initialize
	 *  @param config confif
	 *  @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		if (!WEnv.initWeb(config))
			throw new ServletException("WLogin.init");
	}	//	init

	/**
	 * Get Servlet information
	 * @return servlet info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Login";
	}	//	getServletInfo

	/**
	 *	Clean up
	 */
	public void destroy()
	{
		Log.trace(Log.l1_User, "WLogin.destroy");
		super.destroy();
	}	//	destroy


	/**
	 *	Process the HTTP Get request - forward to Post
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		Log.trace(Log.l1_User, "WLogin.doGet");
		doPost (request, response);
	}	//	doGet


	/**
	 *	Process the HTTP Post request.
	 *  <pre>
	 *  - Optionally create Session
	 *  - Check database connection
	 *  - LoginInfo from request?
	 *      - Yes: DoLogin success ?
	 *          - Yes: return (second) preferences page
	 *          - No: return (first) user/password page
	 *      - No: User Principal ?
	 *          - Yes: DoLogin success ?
	 *              - Yes: return (second) preferences page
	 *              - No: return (first) user/password page
	 *          - No: return (first) user/password page
	 *  </pre>
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		Log.trace(Log.l1_User, "WLogin.doPost");
		//  Create New Session
		HttpSession sess = request.getSession(true);
		sess.setMaxInactiveInterval(WEnv.TIMEOUT);

		//  Get/set Context
		Properties ctx = (Properties)sess.getAttribute(WEnv.SA_CONTEXT);
		if (ctx == null)
			ctx = new Properties();
		sess.setAttribute(WEnv.SA_CONTEXT, ctx);

		//  Get Cookie
		Properties cProp = WUtil.getCookieProprties(request);
		//  Language
		checkLanguage (request, sess, ctx, cProp);
		//  Page
		WDoc doc = null;

		//  Check DB connection
		if (!DB.isConnected())
		{
			String msg = Msg.getMsg(ctx, "WLoginNoDB");
			doc = WDoc.create (msg);
			if (msg.equals("WLoginNoDB"))
				msg = "No Database Connection";
			doc.getBody().addElement(new h1(msg));
		}

		//  Login Info from request?
		else
		{
			//  Get Parameters:     UserName/Password
			String usr = request.getParameter(P_USERNAME);
			String pwd = request.getParameter(P_PASSWORD);
			//  Get Principle
			Principal userPr = request.getUserPrincipal();
			Log.trace(Log.l4_Data, "Principal=" + userPr + "; User=" + usr);

			//  Login info not from request and not pre-authorized
			if (userPr == null && (usr == null || pwd == null))
				doc = createFirstPage (cProp, request, "");
			//  Login info from request or authorized
			else
			{
				KeyNamePair[] roles = null;
				//  Pre-authorized
				if (userPr != null)
				{
					roles = DB.login(ctx, userPr);
					usr = userPr.getName();
				}
				else
					roles = DB.login(ctx, usr, pwd);
				//
				if (roles == null)
					doc = createFirstPage(cProp, request, Msg.getMsg(ctx, "UserPwdError"));
				else
					doc = createSecondPage(cProp, request, WUtil.convertToOption(roles, null), "");
				//  Can we save Cookie ?
				if (request.getParameter(P_STORE) == null)
				{
					cProp.clear();                          //  erase all
				}
				else    //  Save Cookie Parameter
				{
					cProp.setProperty(P_USERNAME, usr);
					cProp.setProperty(P_STORE, "Y");
					cProp.setProperty(P_PASSWORD, pwd);     //  For test only
				}
			}
		}

		//
		WUtil.createResponse (request, response, this, cProp, doc, true);
	}	//	doPost

	//  Variable Names
	public static final String      P_USERNAME      = "User";
	private static final String     P_PASSWORD      = "Password";
	protected static final String   P_LANGUAGE      = Env.LANG;
	private static final String     P_SUBMIT        = "Submit";
	//  WMenu picks it up
	protected static final String   P_ROLE          = "AD_Role_ID";
	protected static final String   P_CLIENT        = "AD_Client_ID";
	protected static final String   P_ORG           = "AD_Org_ID";
	protected static final String   P_DATE          = "Date";
	protected static final String   P_WAREHOUSE     = "M_Warehouse_ID";
	protected static final String   P_ERRORMSG      = "ErrorMessage";
	protected static final String   P_STORE         = "SaveCookie";

	/*************************************************************************/

	/**
	 *  Set Language from request or session in.
	 *  - Properties
	 *  - Cookie
	 *  - Session
	 *  @param request request
	 *  @param sess sess
	 *  @param ctx context
	 *  @param cProp properties
	 */
	private static void checkLanguage (HttpServletRequest request,
		HttpSession sess, Properties ctx, Properties cProp)
	{
		//  Get/set Parameter:      Language
		String AD_Language = request.getParameter(P_LANGUAGE);
		if (AD_Language == null)
		{
			//  Check Cookie
			AD_Language = cProp.getProperty(P_LANGUAGE);
			if (AD_Language == null)
			{
				//  Check Request Locale
				Locale locale = request.getLocale();
				AD_Language = Language.getAD_Language (locale);
			}
		}
		if (AD_Language != null)
		{
			Language language = Language.getLanguage(AD_Language);
			Env.verifyLanguage(ctx, language);
			Env.setContext(ctx, Env.LANG, language.getAD_Language());
			cProp.setProperty(P_LANGUAGE, language.getAD_Language());
			sess.setAttribute(WEnv.SA_LANGUAGE, language);
		}
	}   //  checkLanguage

	/*************************************************************************/


	/**
	 *  First Login Page
	 *  @param cProp Login Cookie information for defaults
	 *  @param request request
	 *  @param errorMessage error message
	 *  @return WDoc page
	 */
	private WDoc createFirstPage(Properties cProp, HttpServletRequest request,
		String errorMessage)
	{
		Log.trace(Log.l4_Data, "WLogin.createFirstPage", errorMessage);
		String AD_Language = (cProp.getProperty(P_LANGUAGE, Language.getAD_Language(request.getLocale())));
		//
		String windowTitle = Msg.getMsg(AD_Language, "Login");
		String usrText = Msg.getMsg(AD_Language, "User");
		String pwdText = Msg.getMsg(AD_Language, "Password");
		String lngText = Msg.translate(AD_Language, "AD_Language");
		String okText = Msg.getMsg(AD_Language, "OK");
		String cancelText = Msg.getMsg(AD_Language, "Cancel");
		String storeTxt = Msg.getMsg(AD_Language, "SaveCookie");

		//  Document
		WDoc doc = WDoc.create (windowTitle);
		body b = doc.getBody();
		//  Clear Menu Frame
		b.addElement(WUtil.getClearFrame(WEnv.TARGET_MENU));

		//	Form - post to same URL
		String action = request.getRequestURI();
		form myForm = null;
		myForm = new form(action, form.post, form.ENC_DEFAULT).setName("Login1");
		myForm.setAcceptCharset(WEnv.CHARACTERSET);
		table table = new table().setAlign(AlignType.center);

		//	Username
		String userData = cProp.getProperty(P_USERNAME, "");
		tr line = new tr();
		label usrLabel = new label().setFor(P_USERNAME).addElement(usrText);
		usrLabel.setID("ID_"+P_USERNAME);
		line.addElement(new td().addElement(usrLabel).setAlign(AlignType.right));
		input usr = new input(input.text, P_USERNAME, userData).setSize(20).setMaxlength(30);
		usr.setID("ID_"+P_USERNAME);
		line.addElement(new td().addElement(usr).setAlign(AlignType.left));
		table.addElement(line);

		//  Password
		String pwdData = cProp.getProperty(P_PASSWORD, "");
		line = new tr();
		label pwdLabel = new label().setFor(P_PASSWORD).addElement(pwdText);
		pwdLabel.setID("ID_"+P_PASSWORD);
		line.addElement(new td().addElement(pwdLabel).setAlign(AlignType.right));
		input pwd = new input(input.password, P_PASSWORD, pwdData).setSize(20).setMaxlength(30);
		pwd.setID("ID_"+P_PASSWORD);
		line.addElement(new td().addElement(pwd).setAlign(AlignType.left));
		table.addElement(line);

		//	Language Pick
		String langData = cProp.getProperty(AD_Language);
		line = new tr();
		label langLabel = new label().setFor(P_LANGUAGE).addElement(lngText);
		langLabel.setID("ID_"+P_LANGUAGE);
		line.addElement(new td().addElement(langLabel).setAlign(AlignType.right));
		option options[] = new option[Language.getLanguageCount()];
		for (int i = 0; i < Language.getLanguageCount(); i++)
		{
			Language language = Language.getLanguage(i);
			options[i] = new option(language.getAD_Language()).addElement(language.getName());
			if (language.getAD_Language().equals(langData))
				options[i].setSelected(true);
			else
				options[i].setSelected(false);
		}
		line.addElement(new td().addElement(new select(P_LANGUAGE, options).setID("ID_"+P_LANGUAGE) ));
		table.addElement(line);

		//  Store Cookie
		String storeData = cProp.getProperty(P_STORE, "N");
		line = new tr();
		line.addElement(new td());
		input store = new input(input.checkbox, P_STORE, "Y").addElement(storeTxt).setChecked(storeData.equals("Y"));
		store.setID("ID_"+P_STORE);
		line.addElement(new td().addElement(store).setAlign(AlignType.left));
		table.addElement(line);

		//  ErrorMessage
		if (errorMessage != null && errorMessage.length() > 0)
		{
			line = new tr();
		//	line.addElement(new td());
			line.addElement(new td().setColSpan(2)
				.addElement(new font(HtmlColor.red, 4).addElement(new b(errorMessage))));   //  color, size
			table.addElement(line);
		}

		//  Finish
		line = new tr();
		input cancel = new input(input.reset, "Reset", cancelText);
		line.addElement(new td().addElement(cancel ));
		line.addElement(new td().addElement(new input(input.submit, P_SUBMIT, okText) ));
		table.addElement(line);
		//
		myForm.addElement(table);
		b.addElement(myForm);

		return doc;
	}   //  getUserPasswordPage


	/**
	 *  Create Second Page
	 *  @param cProp clinet properties
	 *  @param request request
	 *  @param roleOptions role options
	 *  @param errorMessage error message
	 *  @return WDoc page
	 */
	private WDoc createSecondPage(Properties cProp, HttpServletRequest request,
		option[] roleOptions, String errorMessage)
	{
		Log.trace(Log.l4_Data, "WLogin.createSecondPage", errorMessage);
		String AD_Language = cProp.getProperty(P_LANGUAGE, Language.getAD_Language(request.getLocale()));
		String windowTitle = Msg.getMsg(AD_Language, "LoginSuccess");
		//  Create Document
		WDoc doc = WDoc.create (windowTitle);
		body b = doc.getBody();

		//	Form - Get Menu
		String action = WEnv.getBaseDirectory("WMenu");
		form myForm = null;
		myForm = new form(action, form.post, form.ENC_DEFAULT).setName("Login2");
		myForm.setTarget(WEnv.TARGET_MENU);
		myForm.setAcceptCharset(WEnv.CHARACTERSET);
		table table = new table().setAlign(AlignType.center);

		//	Role Pick
		tr line = new tr();
		label roleLabel = new label().setFor(P_ROLE).addElement(Msg.translate(AD_Language, "AD_Role_ID"));
		line.addElement(new td().addElement(roleLabel).setAlign(AlignType.right));
		select role = new select(P_ROLE, roleOptions);
		role.setOnClick("fieldUpdate(this);");        		//  WFieldUpdate sets Client & Org
		line.addElement(new td().addElement(role));
		table.addElement(line);

		//	Client Pick
		line = new tr();
		label clientLabel = new label().setFor(P_CLIENT).addElement(Msg.translate(AD_Language, "AD_Client_ID"));
		line.addElement(new td().addElement(clientLabel).setAlign(AlignType.right));
		select client = new select(P_CLIENT);
		client.setOnClick("fieldUpdate(this);");          	//  WFieldUpdate sets Warehouse
		line.addElement(new td().addElement(client));
		table.addElement(line);

		//	Org Pick
		line = new tr();
		label orgLabel = new label().setFor(P_ORG).addElement(Msg.translate(AD_Language, "AD_Org_ID"));
		line.addElement(new td().addElement(orgLabel).setAlign(AlignType.right));
		line.addElement(new td().addElement(new select(P_ORG) ));
		table.addElement(line);

		//  Warehouse
		line = new tr();
		label whLabel = new label().setFor(P_WAREHOUSE).addElement(Msg.translate(AD_Language, "M_Warehouse_ID"));
		line.addElement(new td().addElement(whLabel).setAlign(AlignType.right));
		line.addElement(new td().addElement(new select(P_WAREHOUSE) ));
		table.addElement(line);

		//  Date
		Language language = (Language)request.getSession().getAttribute(WEnv.SA_LANGUAGE);
		DateFormat df = DisplayType.getDateFormat(DisplayType.Date, language);
		String dateData = df.format(new java.util.Date());
		line = new tr();
		label dateLabel = new label().setFor(P_DATE).addElement(Msg.getMsg(AD_Language, "Date"));
		line.addElement(new td().addElement(dateLabel).setAlign(AlignType.right));
		input date = new input(input.text, P_DATE, dateData).setSize(10).setMaxlength(10);
		date.setID("ID_Date");
		line.addElement(new td().addElement(date).setAlign(AlignType.left));
		table.addElement(line);

		//  ErrorMessage
		if (errorMessage != null && errorMessage.length() > 0)
		{
			line = new tr();
			line.addElement(new td().addElement(new strong(errorMessage)).setColSpan(2).setAlign(AlignType.center));
			table.addElement(line);
		}

		//  Finish
		line = new tr();
		input cancel = new input(input.reset, "Reset", Msg.getMsg(AD_Language, "Cancel"));
		line.addElement(new td().addElement(cancel ));
		input submit = new input(input.submit, "Submit", Msg.getMsg(AD_Language, "OK"));
		submit.setOnClick("showLoadingMenu('" + WEnv.getBaseDirectory("") + "');");
		line.addElement(new td().addElement(submit));
		table.addElement(line);

		//  Note
		line = new tr();
		String note = Msg.getMsg(AD_Language, "WLoginBrowserNote");
		line.addElement(new td().addElement(note).setColSpan(2).setAlign(AlignType.center));
		table.addElement(line);
		//
		myForm.addElement(table);
		b.addElement(myForm);
		//
		String script = "fieldUpdate(document.Login2." + P_ROLE + ");";	//  init dependency updates
		b.addElement(new script(script));

		return doc;
	}   //  getSecondPage

}	//	WLogin
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
package org.compiere.www;

import javax.servlet.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import java.sql.*;
import java.text.*;
import java.security.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;

import org.compiere.util.*;

/**
 *  Web Login Page.
 *  <p>
 *  Page request:
 *  <pre>
 *  - Check database connection
 *  - LoginInfo from request?
 *      - Yes: DoLogin success ?
 *          - Yes: return (second) preferences page
 *          - No: return (first) user/password page
 *      - No: User Principal ?
 *          - Yes: DoLogin success ?
 *              - Yes: return (second) preferences page
 *              - No: return (first) user/password page
 *          - No: return (first) user/password page
 *  </pre>
 *
 *  @author Jorg Janke
 *  @version  $Id: WLogin.java,v 1.2 2003/01/20 05:42:07 jjanke Exp $
 */
public class WLogin extends HttpServlet
{
	/**
	 *	Initialize
	 *  @param config confif
	 *  @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		if (!WEnv.initWeb(config))
			throw new ServletException("WLogin.init");
	}	//	init

	/**
	 * Get Servlet information
	 * @return servlet info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Login";
	}	//	getServletInfo

	/**
	 *	Clean up
	 */
	public void destroy()
	{
		Log.trace(Log.l1_User, "WLogin.destroy");
		super.destroy();
	}	//	destroy


	/**
	 *	Process the HTTP Get request - forward to Post
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		Log.trace(Log.l1_User, "WLogin.doGet");
		doPost (request, response);
	}	//	doGet


	/**
	 *	Process the HTTP Post request.
	 *  <pre>
	 *  - Optionally create Session
	 *  - Check database connection
	 *  - LoginInfo from request?
	 *      - Yes: DoLogin success ?
	 *          - Yes: return (second) preferences page
	 *          - No: return (first) user/password page
	 *      - No: User Principal ?
	 *          - Yes: DoLogin success ?
	 *              - Yes: return (second) preferences page
	 *              - No: return (first) user/password page
	 *          - No: return (first) user/password page
	 *  </pre>
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		Log.trace(Log.l1_User, "WLogin.doPost");
		//  Create New Session
		HttpSession sess = request.getSession(true);
		sess.setMaxInactiveInterval(WEnv.TIMEOUT);

		//  Get/set Context
		Properties ctx = (Properties)sess.getAttribute(WEnv.SA_CONTEXT);
		if (ctx == null)
			ctx = new Properties();
		sess.setAttribute(WEnv.SA_CONTEXT, ctx);

		//  Get Cookie
		Properties cProp = WUtil.getCookieProprties(request);
		//  Language
		checkLanguage (request, sess, ctx, cProp);
		//  Page
		WDoc doc = null;

		//  Check DB connection
		if (!DB.isConnected())
		{
			String msg = Msg.getMsg(ctx, "WLoginNoDB");
			doc = WDoc.create (msg);
			if (msg.equals("WLoginNoDB"))
				msg = "No Database Connection";
			doc.getBody().addElement(new h1(msg));
		}

		//  Login Info from request?
		else
		{
			//  Get Parameters:     UserName/Password
			String usr = request.getParameter(P_USERNAME);
			String pwd = request.getParameter(P_PASSWORD);
			//  Get Principle
			Principal userPr = request.getUserPrincipal();
			Log.trace(Log.l4_Data, "Principal=" + userPr + "; User=" + usr);

			//  Login info not from request and not pre-authorized
			if (userPr == null && (usr == null || pwd == null))
				doc = createFirstPage (cProp, request, "");
			//  Login info from request or authorized
			else
			{
				KeyNamePair[] roles = null;
				//  Pre-authorized
				if (userPr != null)
				{
					roles = DB.login(ctx, userPr);
					usr = userPr.getName();
				}
				else
					roles = DB.login(ctx, usr, pwd);
				//
				if (roles == null)
					doc = createFirstPage(cProp, request, Msg.getMsg(ctx, "UserPwdError"));
				else
					doc = createSecondPage(cProp, request, WUtil.convertToOption(roles, null), "");
				//  Can we save Cookie ?
				if (request.getParameter(P_STORE) == null)
				{
					cProp.clear();                          //  erase all
				}
				else    //  Save Cookie Parameter
				{
					cProp.setProperty(P_USERNAME, usr);
					cProp.setProperty(P_STORE, "Y");
					cProp.setProperty(P_PASSWORD, pwd);     //  For test only
				}
			}
		}

		//
		WUtil.createResponse (request, response, this, cProp, doc, true);
	}	//	doPost

	//  Variable Names
	public static final String      P_USERNAME      = "User";
	private static final String     P_PASSWORD      = "Password";
	protected static final String   P_LANGUAGE      = Env.LANG;
	private static final String     P_SUBMIT        = "Submit";
	//  WMenu picks it up
	protected static final String   P_ROLE          = "AD_Role_ID";
	protected static final String   P_CLIENT        = "AD_Client_ID";
	protected static final String   P_ORG           = "AD_Org_ID";
	protected static final String   P_DATE          = "Date";
	protected static final String   P_WAREHOUSE     = "M_Warehouse_ID";
	protected static final String   P_ERRORMSG      = "ErrorMessage";
	protected static final String   P_STORE         = "SaveCookie";

	/*************************************************************************/

	/**
	 *  Set Language from request or session in.
	 *  - Properties
	 *  - Cookie
	 *  - Session
	 *  @param request request
	 *  @param sess sess
	 *  @param ctx context
	 *  @param cProp properties
	 */
	private static void checkLanguage (HttpServletRequest request,
		HttpSession sess, Properties ctx, Properties cProp)
	{
		//  Get/set Parameter:      Language
		String AD_Language = request.getParameter(P_LANGUAGE);
		if (AD_Language == null)
		{
			//  Check Cookie
			AD_Language = cProp.getProperty(P_LANGUAGE);
			if (AD_Language == null)
			{
				//  Check Request Locale
				Locale locale = request.getLocale();
				AD_Language = Language.getAD_Language (locale);
			}
		}
		if (AD_Language != null)
		{
			Language language = Language.getLanguage(AD_Language);
			Env.verifyLanguage(ctx, language);
			Env.setContext(ctx, Env.LANG, language.getAD_Language());
			cProp.setProperty(P_LANGUAGE, language.getAD_Language());
			sess.setAttribute(WEnv.SA_LANGUAGE, language);
		}
	}   //  checkLanguage

	/*************************************************************************/


	/**
	 *  First Login Page
	 *  @param cProp Login Cookie information for defaults
	 *  @param request request
	 *  @param errorMessage error message
	 *  @return WDoc page
	 */
	private WDoc createFirstPage(Properties cProp, HttpServletRequest request,
		String errorMessage)
	{
		Log.trace(Log.l4_Data, "WLogin.createFirstPage", errorMessage);
		String AD_Language = (cProp.getProperty(P_LANGUAGE, Language.getAD_Language(request.getLocale())));
		//
		String windowTitle = Msg.getMsg(AD_Language, "Login");
		String usrText = Msg.getMsg(AD_Language, "User");
		String pwdText = Msg.getMsg(AD_Language, "Password");
		String lngText = Msg.translate(AD_Language, "AD_Language");
		String okText = Msg.getMsg(AD_Language, "OK");
		String cancelText = Msg.getMsg(AD_Language, "Cancel");
		String storeTxt = Msg.getMsg(AD_Language, "SaveCookie");

		//  Document
		WDoc doc = WDoc.create (windowTitle);
		body b = doc.getBody();
		//  Clear Menu Frame
		b.addElement(WUtil.getClearFrame(WEnv.TARGET_MENU));

		//	Form - post to same URL
		String action = request.getRequestURI();
		form form = null;
		form = new form(action, form.post, form.ENC_DEFAULT).setName("Login1");
		form.setAcceptCharset(WEnv.CHARACTERSET);
		table table = new table().setAlign(AlignType.center);

		//	Username
		String userData = cProp.getProperty(P_USERNAME, "");
		tr line = new tr();
		label usrLabel = new label().setFor(P_USERNAME).addElement(usrText);
		usrLabel.setID("ID_"+P_USERNAME);
		line.addElement(new td().addElement(usrLabel).setAlign(AlignType.right));
		input usr = new input(input.text, P_USERNAME, userData).setSize(20).setMaxlength(30);
		usr.setID("ID_"+P_USERNAME);
		line.addElement(new td().addElement(usr).setAlign(AlignType.left));
		table.addElement(line);

		//  Password
		String pwdData = cProp.getProperty(P_PASSWORD, "");
		line = new tr();
		label pwdLabel = new label().setFor(P_PASSWORD).addElement(pwdText);
		pwdLabel.setID("ID_"+P_PASSWORD);
		line.addElement(new td().addElement(pwdLabel).setAlign(AlignType.right));
		input pwd = new input(input.password, P_PASSWORD, pwdData).setSize(20).setMaxlength(30);
		pwd.setID("ID_"+P_PASSWORD);
		line.addElement(new td().addElement(pwd).setAlign(AlignType.left));
		table.addElement(line);

		//	Language Pick
		String langData = cProp.getProperty(AD_Language);
		line = new tr();
		label langLabel = new label().setFor(P_LANGUAGE).addElement(lngText);
		langLabel.setID("ID_"+P_LANGUAGE);
		line.addElement(new td().addElement(langLabel).setAlign(AlignType.right));
		option options[] = new option[Language.getLanguageCount()];
		for (int i = 0; i < Language.getLanguageCount(); i++)
		{
			Language language = Language.getLanguage(i);
			options[i] = new option(language.getAD_Language()).addElement(language.getName());
			if (language.getAD_Language().equals(langData))
				options[i].setSelected(true);
			else
				options[i].setSelected(false);
		}
		line.addElement(new td().addElement(new select(P_LANGUAGE, options).setID("ID_"+P_LANGUAGE) ));
		table.addElement(line);

		//  Store Cookie
		String storeData = cProp.getProperty(P_STORE, "N");
		line = new tr();
		line.addElement(new td());
		input store = new input(input.checkbox, P_STORE, "Y").addElement(storeTxt).setChecked(storeData.equals("Y"));
		store.setID("ID_"+P_STORE);
		line.addElement(new td().addElement(store).setAlign(AlignType.left));
		table.addElement(line);

		//  ErrorMessage
		if (errorMessage != null && errorMessage.length() > 0)
		{
			line = new tr();
		//	line.addElement(new td());
			line.addElement(new td().setColSpan(2)
				.addElement(new font(HtmlColor.red, 4).addElement(new b(errorMessage))));   //  color, size
			table.addElement(line);
		}

		//  Finish
		line = new tr();
		input cancel = new input(input.reset, "Reset", cancelText);
		line.addElement(new td().addElement(cancel ));
		line.addElement(new td().addElement(new input(input.submit, P_SUBMIT, okText) ));
		table.addElement(line);
		//
		form.addElement(table);
		b.addElement(form);

		return doc;
	}   //  getUserPasswordPage


	/**
	 *  Create Second Page
	 *  @param cProp clinet properties
	 *  @param request request
	 *  @param roleOptions role options
	 *  @param errorMessage error message
	 *  @return WDoc page
	 */
	private WDoc createSecondPage(Properties cProp, HttpServletRequest request,
		option[] roleOptions, String errorMessage)
	{
		Log.trace(Log.l4_Data, "WLogin.createSecondPage", errorMessage);
		String AD_Language = cProp.getProperty(P_LANGUAGE, Language.getAD_Language(request.getLocale()));
		String windowTitle = Msg.getMsg(AD_Language, "LoginSuccess");
		//  Create Document
		WDoc doc = WDoc.create (windowTitle);
		body b = doc.getBody();

		//	Form - Get Menu
		String action = WEnv.getBaseDirectory("WMenu");
		form form = null;
		form = new form(action, form.post, form.ENC_DEFAULT).setName("Login2");
		form.setTarget(WEnv.TARGET_MENU);
		form.setAcceptCharset(WEnv.CHARACTERSET);
		table table = new table().setAlign(AlignType.center);

		//	Role Pick
		tr line = new tr();
		label roleLabel = new label().setFor(P_ROLE).addElement(Msg.translate(AD_Language, "AD_Role_ID"));
		line.addElement(new td().addElement(roleLabel).setAlign(AlignType.right));
		select role = new select(P_ROLE, roleOptions);
		role.setOnClick("fieldUpdate();");                  //  WFieldUpdate sets Client & Org
		line.addElement(new td().addElement(role));
		table.addElement(line);

		//	Client Pick
		line = new tr();
		label clientLabel = new label().setFor(P_CLIENT).addElement(Msg.translate(AD_Language, "AD_Client_ID"));
		line.addElement(new td().addElement(clientLabel).setAlign(AlignType.right));
		select client = new select(P_CLIENT);
		client.setOnClick("fieldUpdate();");                //  WFieldUpdate sets Warehouse
		line.addElement(new td().addElement(client));
		table.addElement(line);

		//	Org Pick
		line = new tr();
		label orgLabel = new label().setFor(P_ORG).addElement(Msg.translate(AD_Language, "AD_Org_ID"));
		line.addElement(new td().addElement(orgLabel).setAlign(AlignType.right));
		line.addElement(new td().addElement(new select(P_ORG) ));
		table.addElement(line);

		//  Warehouse
		line = new tr();
		label whLabel = new label().setFor(P_WAREHOUSE).addElement(Msg.translate(AD_Language, "M_Warehouse_ID"));
		line.addElement(new td().addElement(whLabel).setAlign(AlignType.right));
		line.addElement(new td().addElement(new select(P_WAREHOUSE) ));
		table.addElement(line);

		//  Date
		Language language = (Language)request.getSession().getAttribute(WEnv.SA_LANGUAGE);
		DateFormat df = DisplayType.getDateFormat(DisplayType.Date, language);
		String dateData = df.format(new java.util.Date());
		line = new tr();
		label dateLabel = new label().setFor(P_DATE).addElement(Msg.getMsg(AD_Language, "Date"));
		line.addElement(new td().addElement(dateLabel).setAlign(AlignType.right));
		input date = new input(input.text, P_DATE, dateData).setSize(10).setMaxlength(10);
		date.setID("ID_Date");
		line.addElement(new td().addElement(date).setAlign(AlignType.left));
		table.addElement(line);

		//  ErrorMessage
		if (errorMessage != null && errorMessage.length() > 0)
		{
			line = new tr();
			line.addElement(new td().addElement(new strong(errorMessage)).setColSpan(2).setAlign(AlignType.center));
			table.addElement(line);
		}

		//  Finish
		line = new tr();
		input cancel = new input(input.reset, "Reset", Msg.getMsg(AD_Language, "Cancel"));
		line.addElement(new td().addElement(cancel ));
		input submit = new input(input.submit, "Submit", Msg.getMsg(AD_Language, "OK"));
		submit.setOnClick("showLoadingMenu('" + WEnv.getBaseDirectory("") + "');");
		line.addElement(new td().addElement(submit));
		table.addElement(line);

		//  Note
		line = new tr();
		String note = Msg.getMsg(AD_Language, "WLoginBrowserNote");
		line.addElement(new td().addElement(note).setColSpan(2).setAlign(AlignType.center));
		table.addElement(line);
		//
		form.addElement(table);
		b.addElement(form);
		//
		String script = "document.Login2." + P_ROLE + ".click();";         //  init dependency updates
		b.addElement(new script(script));

		return doc;
	}   //  getSecondPage

}	//	WLogin
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
package org.compiere.www;

import java.io.*;
import java.security.*;
import java.text.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.compiere.util.*;

/**
 *  Web Login Page.
 *  <p>
 *  Page request:
 *  <pre>
 *  - Check database connection
 *  - LoginInfo from request?
 *      - Yes: DoLogin success ?
 *          - Yes: return (second) preferences page
 *          - No: return (first) user/password page
 *      - No: User Principal ?
 *          - Yes: DoLogin success ?
 *              - Yes: return (second) preferences page
 *              - No: return (first) user/password page
 *          - No: return (first) user/password page
 *  </pre>
 *
 *  @author Jorg Janke
 *  @version  $Id: WLogin.java,v 1.8 2004/09/10 02:54:23 jjanke Exp $
 */
public class WLogin extends HttpServlet
{
	/**	Logger			*/
	protected Logger	log = Logger.getCLogger(getClass());
	
	/**
	 *	Initialize
	 *  @param config confif
	 *  @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		if (!WebEnv.initWeb(config))
			throw new ServletException("WLogin.init");
	}	//	init

	/**
	 * Get Servlet information
	 * @return servlet info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Login";
	}	//	getServletInfo

	/**
	 *	Clean up
	 */
	public void destroy()
	{
		log.debug("destroy");
		super.destroy();
	}	//	destroy


	/**
	 *	Process the HTTP Get request - forward to Post
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		log.debug("doGet");
		doPost (request, response);
	}	//	doGet


	/**
	 *	Process the HTTP Post request.
	 *  <pre>
	 *  - Optionally create Session
	 *  - Check database connection
	 *  - LoginInfo from request?
	 *      - Yes: DoLogin success ?
	 *          - Yes: return (second) preferences page
	 *          - No: return (first) user/password page
	 *      - No: User Principal ?
	 *          - Yes: DoLogin success ?
	 *              - Yes: return (second) preferences page
	 *              - No: return (first) user/password page
	 *          - No: return (first) user/password page
	 *  </pre>
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		log.debug("doPost");
		//  Create New Session
		HttpSession sess = request.getSession(true);
		sess.setMaxInactiveInterval(WebEnv.TIMEOUT);

		//  Get/set Context
		Properties ctx = (Properties)sess.getAttribute(WebEnv.SA_CONTEXT);
		if (ctx == null)
			ctx = new Properties();
		sess.setAttribute(WebEnv.SA_CONTEXT, ctx);

		//  Get Cookie
		Properties cProp = WebUtil.getCookieProprties(request);
		//  Language
		checkLanguage (request, sess, ctx, cProp);
		//  Page
		WebDoc doc = null;

		//  Check DB connection
		if (!DB.isConnected())
		{
			String msg = Msg.getMsg(ctx, "WLoginNoDB");
			doc = WebDoc.create (msg);
			if (msg.equals("WLoginNoDB"))
				msg = "No Database Connection";
			doc.getBody().addElement(new h1(msg));
		}

		//  Login Info from request?
		else
		{
			//  Get Parameters:     UserName/Password
			String usr = request.getParameter(P_USERNAME);
			String pwd = request.getParameter(P_PASSWORD);
			//  Get Principle
			Principal userPr = request.getUserPrincipal();
			log.debug("doPost - Principal=" + userPr + "; User=" + usr);

			//  Login info not from request and not pre-authorized
			if (userPr == null && (usr == null || pwd == null))
				doc = createFirstPage (cProp, request, "");
			//  Login info from request or authorized
			else
			{
				KeyNamePair[] roles = null;
				//  Pre-authorized
				if (userPr != null)
				{
					roles = DB.login(ctx, userPr);
					usr = userPr.getName();
				}
				else
					roles = DB.login(ctx, usr, pwd);
				//
				if (roles == null)
					doc = createFirstPage(cProp, request, Msg.getMsg(ctx, "UserPwdError"));
				else
					doc = createSecondPage(cProp, request, WebUtil.convertToOption(roles, null), "");
				//  Can we save Cookie ?
				if (request.getParameter(P_STORE) == null)
				{
					cProp.clear();                          //  erase all
				}
				else    //  Save Cookie Parameter
				{
					cProp.setProperty(P_USERNAME, usr);
					cProp.setProperty(P_STORE, "Y");
					cProp.setProperty(P_PASSWORD, pwd);     //  For test only
				}
			}
		}

		//
		WebUtil.createResponse (request, response, this, cProp, doc, true);
	}	//	doPost

	//  Variable Names
	public static final String      P_USERNAME      = "User";
	private static final String     P_PASSWORD      = "Password";
	protected static final String   P_LANGUAGE      = Env.LANGUAGE;
	private static final String     P_SUBMIT        = "Submit";
	//  WMenu picks it up
	protected static final String   P_ROLE          = "AD_Role_ID";
	protected static final String   P_CLIENT        = "AD_Client_ID";
	protected static final String   P_ORG           = "AD_Org_ID";
	protected static final String   P_DATE          = "Date";
	protected static final String   P_WAREHOUSE     = "M_Warehouse_ID";
	protected static final String   P_ERRORMSG      = "ErrorMessage";
	protected static final String   P_STORE         = "SaveCookie";

	
	/**************************************************************************
	 *  Set Language from request or session in.
	 *  - Properties
	 *  - Cookie
	 *  - Session
	 *  @param request request
	 *  @param sess sess
	 *  @param ctx context
	 *  @param cProp properties
	 */
	private static void checkLanguage (HttpServletRequest request,
		HttpSession sess, Properties ctx, Properties cProp)
	{
		//  Get/set Parameter:      Language
		String AD_Language = request.getParameter(P_LANGUAGE);
		if (AD_Language == null)
		{
			//  Check Cookie
			AD_Language = cProp.getProperty(P_LANGUAGE);
			if (AD_Language == null)
			{
				//  Check Request Locale
				Locale locale = request.getLocale();
				AD_Language = Language.getAD_Language (locale);
			}
		}
		if (AD_Language != null)
		{
			Language language = Language.getLanguage(AD_Language);
			Env.verifyLanguage (ctx, language);
			Env.setContext(ctx, Env.LANGUAGE, language.getAD_Language());
			Msg.getMsg(ctx, "0");
			cProp.setProperty(P_LANGUAGE, language.getAD_Language());
			sess.setAttribute(WebEnv.SA_LANGUAGE, language);
		}
	}   //  checkLanguage

	
	/**************************************************************************
	 *  First Login Page
	 *  @param cProp Login Cookie information for defaults
	 *  @param request request
	 *  @param errorMessage error message
	 *  @return WDoc page
	 */
	private WebDoc createFirstPage(Properties cProp, HttpServletRequest request,
		String errorMessage)
	{
		log.debug ("createFirstPage - " + errorMessage);
		String AD_Language = (cProp.getProperty(P_LANGUAGE, Language.getAD_Language(request.getLocale())));
		//
		String windowTitle = Msg.getMsg(AD_Language, "Login");
		String usrText = Msg.getMsg(AD_Language, "User");
		String pwdText = Msg.getMsg(AD_Language, "Password");
		String lngText = Msg.translate(AD_Language, "AD_Language");
		String okText = Msg.getMsg(AD_Language, "OK");
		String cancelText = Msg.getMsg(AD_Language, "Cancel");
		String storeTxt = Msg.getMsg(AD_Language, "SaveCookie");

		//  Document
		WebDoc doc = WebDoc.create (windowTitle);
		body b = doc.getBody();
		//  Clear Menu Frame
		b.addElement(WebUtil.getClearFrame(WebEnv.TARGET_MENU));

		//	Form - post to same URL
		String action = request.getRequestURI();
		form myForm = null;
		myForm = new form(action, form.post, form.ENC_DEFAULT).setName("Login1");
		myForm.setAcceptCharset(WebEnv.CHARACTERSET);
		table table = new table().setAlign(AlignType.center);

		//	Username
		String userData = cProp.getProperty(P_USERNAME, "");
		tr line = new tr();
		label usrLabel = new label().setFor(P_USERNAME).addElement(usrText);
		usrLabel.setID("ID_"+P_USERNAME);
		line.addElement(new td().addElement(usrLabel).setAlign(AlignType.right));
		input usr = new input(input.text, P_USERNAME, userData).setSize(20).setMaxlength(30);
		usr.setID("ID_"+P_USERNAME);
		line.addElement(new td().addElement(usr).setAlign(AlignType.left));
		table.addElement(line);

		//  Password
		String pwdData = cProp.getProperty(P_PASSWORD, "");
		line = new tr();
		label pwdLabel = new label().setFor(P_PASSWORD).addElement(pwdText);
		pwdLabel.setID("ID_"+P_PASSWORD);
		line.addElement(new td().addElement(pwdLabel).setAlign(AlignType.right));
		input pwd = new input(input.password, P_PASSWORD, pwdData).setSize(20).setMaxlength(30);
		pwd.setID("ID_"+P_PASSWORD);
		line.addElement(new td().addElement(pwd).setAlign(AlignType.left));
		table.addElement(line);

		//	Language Pick
		String langData = cProp.getProperty(AD_Language);
		line = new tr();
		label langLabel = new label().setFor(P_LANGUAGE).addElement(lngText);
		langLabel.setID("ID_"+P_LANGUAGE);
		line.addElement(new td().addElement(langLabel).setAlign(AlignType.right));
		option options[] = new option[Language.getLanguageCount()];
		for (int i = 0; i < Language.getLanguageCount(); i++)
		{
			Language language = Language.getLanguage(i);
			options[i] = new option(language.getAD_Language()).addElement(language.getName());
			if (language.getAD_Language().equals(langData))
				options[i].setSelected(true);
			else
				options[i].setSelected(false);
		}
		line.addElement(new td().addElement(new select(P_LANGUAGE, options).setID("ID_"+P_LANGUAGE) ));
		table.addElement(line);

		//  Store Cookie
		String storeData = cProp.getProperty(P_STORE, "N");
		line = new tr();
		line.addElement(new td());
		input store = new input(input.checkbox, P_STORE, "Y").addElement(storeTxt).setChecked(storeData.equals("Y"));
		store.setID("ID_"+P_STORE);
		line.addElement(new td().addElement(store).setAlign(AlignType.left));
		table.addElement(line);

		//  ErrorMessage
		if (errorMessage != null && errorMessage.length() > 0)
		{
			line = new tr();
		//	line.addElement(new td());
			line.addElement(new td().setColSpan(2)
				.addElement(new font(HtmlColor.red, 4).addElement(new b(errorMessage))));   //  color, size
			table.addElement(line);
		}

		//  Finish
		line = new tr();
		input cancel = new input(input.reset, "Reset", cancelText);
		line.addElement(new td().addElement(cancel ));
		line.addElement(new td().addElement(new input(input.submit, P_SUBMIT, okText) ));
		table.addElement(line);
		//
		myForm.addElement(table);
		b.addElement(myForm);

		return doc;
	}   //  getUserPasswordPage


	/**
	 *  Create Second Page
	 *  @param cProp clinet properties
	 *  @param request request
	 *  @param roleOptions role options
	 *  @param errorMessage error message
	 *  @return WDoc page
	 */
	private WebDoc createSecondPage(Properties cProp, HttpServletRequest request,
		option[] roleOptions, String errorMessage)
	{
		log.debug("createSecondPage - " + errorMessage);
		String AD_Language = cProp.getProperty(P_LANGUAGE, Language.getAD_Language(request.getLocale()));
		String windowTitle = Msg.getMsg(AD_Language, "LoginSuccess");
		//  Create Document
		WebDoc doc = WebDoc.create (windowTitle);
		body b = doc.getBody();

		//	Form - Get Menu
		String action = WebEnv.getBaseDirectory("WMenu");
		form myForm = null;
		myForm = new form(action, form.post, form.ENC_DEFAULT).setName("Login2");
		myForm.setTarget(WebEnv.TARGET_MENU);
		myForm.setAcceptCharset(WebEnv.CHARACTERSET);
		table table = new table().setAlign(AlignType.center);

		//	Role Pick
		tr line = new tr();
		label roleLabel = new label().setFor(P_ROLE).addElement(Msg.translate(AD_Language, "AD_Role_ID"));
		line.addElement(new td().addElement(roleLabel).setAlign(AlignType.right));
		select role = new select(P_ROLE, roleOptions);
		role.setOnClick("fieldUpdate(this);");        		//  WFieldUpdate sets Client & Org
		line.addElement(new td().addElement(role));
		table.addElement(line);

		//	Client Pick
		line = new tr();
		label clientLabel = new label().setFor(P_CLIENT).addElement(Msg.translate(AD_Language, "AD_Client_ID"));
		line.addElement(new td().addElement(clientLabel).setAlign(AlignType.right));
		select client = new select(P_CLIENT);
		client.setOnClick("fieldUpdate(this);");          	//  WFieldUpdate sets Warehouse
		line.addElement(new td().addElement(client));
		table.addElement(line);

		//	Org Pick
		line = new tr();
		label orgLabel = new label().setFor(P_ORG).addElement(Msg.translate(AD_Language, "AD_Org_ID"));
		line.addElement(new td().addElement(orgLabel).setAlign(AlignType.right));
		line.addElement(new td().addElement(new select(P_ORG) ));
		table.addElement(line);

		//  Warehouse
		line = new tr();
		label whLabel = new label().setFor(P_WAREHOUSE).addElement(Msg.translate(AD_Language, "M_Warehouse_ID"));
		line.addElement(new td().addElement(whLabel).setAlign(AlignType.right));
		line.addElement(new td().addElement(new select(P_WAREHOUSE) ));
		table.addElement(line);

		//  Date
		Language language = (Language)request.getSession().getAttribute(WebEnv.SA_LANGUAGE);
		DateFormat df = DisplayType.getDateFormat(DisplayType.Date, language);
		String dateData = df.format(new java.util.Date());
		line = new tr();
		label dateLabel = new label().setFor(P_DATE).addElement(Msg.getMsg(AD_Language, "Date"));
		line.addElement(new td().addElement(dateLabel).setAlign(AlignType.right));
		input date = new input(input.text, P_DATE, dateData).setSize(10).setMaxlength(10);
		date.setID("ID_Date");
		line.addElement(new td().addElement(date).setAlign(AlignType.left));
		table.addElement(line);

		//  ErrorMessage
		if (errorMessage != null && errorMessage.length() > 0)
		{
			line = new tr();
			line.addElement(new td().addElement(new strong(errorMessage)).setColSpan(2).setAlign(AlignType.center));
			table.addElement(line);
		}

		//  Finish
		line = new tr();
		input cancel = new input(input.reset, "Reset", Msg.getMsg(AD_Language, "Cancel"));
		line.addElement(new td().addElement(cancel ));
		input submit = new input(input.submit, "Submit", Msg.getMsg(AD_Language, "OK"));
		submit.setOnClick("showLoadingMenu('" + WebEnv.getBaseDirectory("") + "');");
		line.addElement(new td().addElement(submit));
		table.addElement(line);

		//  Note
		line = new tr();
		String note = Msg.getMsg(AD_Language, "WLoginBrowserNote");
		line.addElement(new td().addElement(note).setColSpan(2).setAlign(AlignType.center));
		table.addElement(line);
		//
		myForm.addElement(table);
		b.addElement(myForm);
		//
		String script = "fieldUpdate(document.Login2." + P_ROLE + ");";	//  init dependency updates
		b.addElement(new script(script));

		return doc;
	}   //  getSecondPage

}	//	WLogin
