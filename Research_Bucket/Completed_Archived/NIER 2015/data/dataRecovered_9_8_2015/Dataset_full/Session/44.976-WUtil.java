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
import java.util.*;
import java.sql.*;
import java.io.*;
import javax.mail.internet.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.apache.log4j.Logger;

import org.compiere.wstore.*;
import org.compiere.util.*;
import org.compiere.model.*;

/**
 *  Servlet Utilities
 *
 *  @author Jorg Janke
 *  @version  $Id: WUtil.java,v 1.12 2003/07/19 05:21:44 jjanke Exp $
 */
public final class WUtil
{
	/**	Logging								*/
	private static Logger			s_log = Logger.getLogger("org.compiere.www.WUtil");

	/**
	 *  Create Timeout Message
	 *
	 *  @param request request
	 *  @param response response
	 *  @param servlet servlet
	 *  @param ctx context
	 *  @param message - optional message
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public static void createTimeoutPage (HttpServletRequest request, HttpServletResponse response,
		HttpServlet servlet, Properties ctx, String message) throws ServletException, IOException
	{
		s_log.info("createTimeoutPage - " + message);
		String windowTitle = "Timeout";
		if (ctx != null)
			windowTitle = Msg.getMsg(ctx, "Timeout");

		WDoc doc = WDoc.create (windowTitle);

		//	Body
		body body = doc.getBody();
		//  optional message
		if (message != null && message.length() > 0)
			body.addElement(new p(message));

		//  login button
		body.addElement(getLoginButton(ctx));

		//
		body.addElement(new hr());
		body.addElement(new small(servlet.getClass().getName()));
		//	fini
		createResponse (request, response, servlet, null, doc, false);
	}   //  createTimeoutPage

	/**
	 *  Create Error Message
	 *
	 *  @param request request
	 *  @param response response
	 *  @param servlet servlet
	 *  @param ctx context
	 *  @param message message
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public static void createErrorPage (HttpServletRequest request, HttpServletResponse response,
		HttpServlet servlet, Properties ctx, String message) throws ServletException, IOException
	{
		s_log.info("createErrorPage - " + message);
		String windowTitle = "Error";
		if (ctx != null)
			windowTitle = Msg.getMsg(ctx, "Error");		//	JUST TO GET LANGUAGE ??
		if (message != null)
			windowTitle += ": " + message;

		WDoc doc = WDoc.create (windowTitle);

		//	Body
		body b = doc.getBody();

		b.addElement(new p(servlet.getServletName(), AlignType.center));
		b.addElement(new br());

		//	fini
		createResponse (request, response, servlet, null, doc, true);
	}   //  createErrorPage

	/**
	 *  Create Exit Page "Log-off".
	 *  <p>
	 *  - End Session
	 *  - Go to start page (e.g. /compiere/index.html)
	 *
	 *  @param request request
	 *  @param response response
	 *  @param servlet servlet
	 *  @param ctx context
	 *  @param AD_Message messahe
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public static void createLoginPage (HttpServletRequest request, HttpServletResponse response,
		HttpServlet servlet, Properties ctx, String AD_Message) throws ServletException, IOException
	{
		request.getSession().invalidate();
		String url = WEnv.getBaseDirectory("index.html");
		//
		WDoc doc = null;
		if (ctx != null && AD_Message != null && !AD_Message.equals(""))
			doc = WDoc.create (Msg.getMsg(ctx, AD_Message));
		else if (AD_Message != null)
			doc = WDoc.create (AD_Message);
		else
			doc = WDoc.create (false);
		script script = new script("window.top.location.replace('" + url + "');");
		doc.getBody().addElement(script);
		//
		createResponse (request, response, servlet, null, doc, false);
	}   //  createLoginPage

	/**
	 *  Create Login Button - replace Window
	 *
	 *  @param ctx context
	 *  @return Button
	 */
	public static button getLoginButton(Properties ctx)
	{
		String text = Msg.getMsg(ctx, "Login");
		button button = new button();
		button.setType("button").setName("Login").addElement(text);
		StringBuffer cmd = new StringBuffer ("window.top.location.replace('");
		cmd.append(WEnv.getBaseDirectory("index.html"));
		cmd.append("');");
		button.setOnClick(cmd.toString());
		return button;
	}   //  getLoginButton

	/*************************************************************************/

	/**
	 *  Get Cookie Properties
	 *
	 *  @param request request
	 *  @return Properties
	 */
	public static Properties getCookieProprties(HttpServletRequest request)
	{
		//  Get Properties
		Cookie[] cookies = request.getCookies();
		if (cookies != null)
		{
			for (int i = 0; i < cookies.length; i++)
			{
				if (cookies[i].getName().equals(WEnv.COOKIE_INFO))
					return propertiesDecode(cookies[i].getValue());
			}
		}
		return new Properties();
	}   //  getProperties

	/**
	 *  Get numeric Parameter - 0 if not defined
	 *
	 *  @param request request
	 *  @param parameter parameter
	 *  @return int result or 0
	 */
	public static int getParameterAsInt(HttpServletRequest request, String parameter)
	{
		if (request == null || parameter == null)
			return 0;
		String data = request.getParameter(parameter);
		if (data == null || data.length() == 0)
			return 0;
		try
		{
			return Integer.parseInt(data);
		}
		catch (Exception e)
		{
			s_log.debug("getParameterAsInt - " + parameter + "=" + data, e);
		}
		return 0;
	}   //  getParameter

	/*************************************************************************/

	/**
	 *  Create Standard Response Header with optional Cookie and print document.
	 *  D:\j2sdk1.4.0\docs\guide\intl\encoding.doc.html
	 *
	 *  @param request request
	 *  @param response response
	 *  @param servlet servlet
	 *  @param cookieProperties cookie properties
	 *  @param doc doc
	 *  @param debug debug
	 *  @throws IOException
	 */
	public static void createResponse (HttpServletRequest request, HttpServletResponse response,
		HttpServlet servlet, Properties cookieProperties, WDoc doc, boolean debug) throws IOException
	{
		response.setHeader("Cache-Control", "no-cache");
	//	response.setContentType("text/html; charset=ISO-8859-1");   //  default
		response.setContentType("text/html; charset=UTF-8");

		//
		//  Update Cookie - overwrite
		if (cookieProperties != null)
		{
			Cookie cookie = new Cookie (WEnv.COOKIE_INFO, propertiesEncode(cookieProperties));
			cookie.setComment("(c) ComPiere, Inc - Jorg Janke");
			cookie.setSecure(false);
			cookie.setPath("/");
			if (cookieProperties.size() == 0)
				cookie.setMaxAge(0);            //  delete cookie
			else
				cookie.setMaxAge(2592000);      //  30 days in seconds   60*60*24*30
			response.addCookie(cookie);
		}
		//  add diagnostics
		if (debug && WEnv.DEBUG)
		{
		//	doc.output(System.out);
			WEnv.addFooter(request, response, servlet, doc.getBody());
		//	doc.output(System.out);
		}
	//	String content = doc.toString();
	//  response.setContentLength(content.length());    //  causes problems at the end of the output

		//  print document
		PrintWriter out = response.getWriter();     //  with character encoding support
		doc.output(out);
		out.flush();
		if (out.checkError())
			s_log.error("createResponse - error writing");
		//  binary output (is faster but does not do character set conversion)
	//	OutputStream out = response.getOutputStream();
	//	byte[] data = doc.toString().getBytes();
	//	response.setContentLength(data.length);
	//	out.write(doc.toString().getBytes());
		//
		out.close();
	}   //  createResponse

	/*************************************************************************/

	/**
	 *  Create Java Script to clear Target frame and display message
	 *
	 *  @param target target
	 *  @return Clear Frame Script
	 */
	public static script getClearFrame (String target)
	{
		StringBuffer cmd = new StringBuffer();
		cmd.append("var d = parent.").append(target).append(".document;\n");
		cmd.append("d.open();\n");
		cmd.append("d.write('<link href=\"").append(WEnv.getStylesheetURL()).append("\" rel=\"stylesheet\">');\n");
		cmd.append("d.close();");
		//
		return new script(cmd.toString());
	}   //  getClearFrame


	/**
	 * 	Return a link and script with new location.
	 * 	Wait 2 seconds
	 * 	@param url forward url
	 * 	@return html
	 */
	public static HtmlCode getForward (String url)
	{
		HtmlCode retValue = new HtmlCode();
		//	Link
		a a = new a(url);
		a.addElement(url);
		retValue.addElement(a);
		//	Java Script	- document.location - 2.5 sec delay
		script script = new script("setTimeout(\"window.top.location.replace('" + url + "')\",2500);");
		retValue.addElement(script);
		//
		return retValue;
	}	//	getForward

	/**
	 * 	Create Forward Page
	 * 	@param response response
	 * 	@param title page title
	 * 	@param forwardURL url
	 * 	@throws ServletException
	 * 	@throws IOException
	 */
	public static void createForwardPage (HttpServletResponse response,
		String title, String forwardURL) throws ServletException, IOException
	{
		response.setContentType("text/html; charset=UTF-8");
		WDoc doc = WDoc.create(title);
		body b = doc.getBody();
		b.addElement(getForward(forwardURL));
		PrintWriter out = response.getWriter();
		doc.output(out);
		out.flush();
		if (out.checkError())
			s_log.error("createForwardPage - error writing");
		out.close();
		s_log.debug("createForwardPage - " + forwardURL + " - " + title);
	}	//	createForwardPage


	/**
	 * 	Does Test exist
	 *	@param test string
	 *	@return true if String with data
	 */
	public static boolean exists (String test)
	{
		if (test == null)
			return false;
		return test.length() > 0;
	}	//	exists

	/**
	 * 	Does Parameter exist
	 * 	@param request request
	 *	@param parameter string
	 *	@return true if String with data
	 */
	public static boolean exists (HttpServletRequest request, String parameter)
	{
		if (request == null || parameter == null)
			return false;
		return exists (request.getParameter(parameter));
	}	//	exists


	/**
	 *	Is EMail address valid
	 * 	@param email mail address
	 * 	@return true if valid
	 */
	public static boolean isEmailValid (String email)
	{
		if (email == null || email.length () == 0)
			return false;
		try
		{
			InternetAddress ia = new InternetAddress (email, true);
			return true;
		}
		catch (AddressException ex)
		{
			s_log.warn ("isEmailValid - " + email + " - "
				+ ex.getLocalizedMessage ());
		}
		return false;
	}	//	isEmailValid


	/*************************************************************************/

	/**
	 *  Decode Properties into String (URL encoded)
	 *
	 *  @param pp properties
	 *  @return Encoded String
	 */
	public static String propertiesEncode (Properties pp)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try
		{
			pp.store(bos, "Compiere");   //  Header
		}
		catch (IOException e)
		{
			s_log.error("propertiesEncode-store", e);
		}
		String result = new String (bos.toByteArray());
	//	System.out.println("String=" + result);
		String enc = "UTF-8";
		try
		{
			result = java.net.URLEncoder.encode(result, enc);
		}
		catch (UnsupportedEncodingException e)
		{
			s_log.error("propertiesEncode-encode-" + enc, e);
			enc = System.getProperty("file.encoding");      //  Windows default is Cp1252
			try
			{
				result = java.net.URLEncoder.encode(result, enc);
			}
			catch (Exception ex)
			{
				s_log.error("propertiesEncode-encode", ex);
			}
		}
	//	System.out.println("String-Encoded=" + result);
		return result;
	}   //  propertiesEncode

	/**
	 *  Decode data String (URL encoded) into Properties
	 *
	 *  @param data data
	 *  @return Properties
	 */
	public static Properties propertiesDecode (String data)
	{
		String result = null;
	//	System.out.println("String=" + data);
		String enc = "UTF-8";
		try
		{
			result = java.net.URLDecoder.decode(data, enc);
		}
		catch (UnsupportedEncodingException e)
		{
			s_log.error("propertiesDecode-decode-" + enc, e);
			enc = System.getProperty("file.encoding");      //  Windows default is Cp1252
			try
			{
				result = java.net.URLEncoder.encode(data, enc);
			}
			catch (Exception ex)
			{
				s_log.error("propertiesDecode-decode", ex);
			}
		}
	//	System.out.println("String-Decoded=" + result);

		ByteArrayInputStream bis = new ByteArrayInputStream(result.getBytes());
		Properties pp = new Properties();
		try
		{
			pp.load(bis);
		}
		catch (IOException e)
		{
			s_log.error("propertiesDecode-load", e);
		}
		return pp;
	}   //  propertiesDecode

	/*************************************************************************/

	/**
	 *  Convert Array of NamePair to HTTP Option Array.
	 *  <p>
	 *  If the ArrayList does not contain NamePairs, the String value is used
	 *  @see org.compiere.util.NamePair
	 *  @param  list    ArrayList containing NamePair values
	 *  @param  default_ID  Sets the default if the key/ID value is found.
	 *      If the value is null or empty, the first value is selected
	 *  @return Option Array
	 */
	public static option[] convertToOption (NamePair[] list, String default_ID)
	{
		int size = list.length;
		option[] retValue = new option[size];
		for (int i = 0; i < size; i++)
		{
			boolean selected = false;
			//  select first entry
			if (i == 0 && (default_ID == null || default_ID.length() == 0))
				selected = true;

			//  Create option
			retValue[i] = new option(list[i].getID()).addElement(list[i].getName());

			//  Select if ID/Key is same as default ID
			if (default_ID != null && default_ID.equals(list[i].getID()))
				selected = true;
			retValue[i].setSelected(selected);
		}
		return retValue;
	}   //  convertToOption

	/**
	 *  Create label/field table row
	 *
	 *  @param line - null for new line (table row)
	 *  @param FORMNAME form name
	 *  @param PARAMETER parameter name
	 *  @param labelText label
	 *  @param inputType HTML input type
	 *  @param value data value
	 *  @param sizeDisplay display size
	 *  @param size data size
	 *  @param longField field spanning two columns
	 *  @param mandatory mark as mandatory
	 *  @param onChange onChange call
	 *  @param script script
	 *  @return tr table row
	 */
	static public tr createField (tr line, String FORMNAME, String PARAMETER,
		String labelText, String inputType, Object value,
		int sizeDisplay, int size, boolean longField, boolean mandatory, String onChange, StringBuffer script)
	{
		if (line == null)
			line = new tr();
		String labelInfo = labelText;
		if (mandatory)
		{
			labelInfo += "&nbsp;<font color=\"red\">*</font>";
			String fName = "document." + FORMNAME + "." + PARAMETER;
			script.append(fName).append(".required=true; ");
		}

		label llabel = new label().setFor(PARAMETER).addElement(labelInfo);
		llabel.setID("ID_" + PARAMETER + "_Label");
	//	label.setTitle(description);
		line.addElement(new td().addElement(llabel).setAlign(AlignType.right));
		input iinput = new input(inputType, PARAMETER, value == null ? "" : value.toString());
		iinput.setSize(sizeDisplay).setMaxlength(size);
		iinput.setID("ID_" + PARAMETER);
		if (onChange != null && onChange.length() > 0)
			iinput.setOnChange(onChange);
		iinput.setTitle(labelText);
		td field = new td().addElement(iinput).setAlign(AlignType.LEFT);
		if (longField)
			field.setColSpan(3);
		line.addElement(field);
		return line;
	}   //  addField

}   //  WUtil
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
import java.util.*;
import java.sql.*;
import java.text.*;
import java.io.*;
import java.math.*;

import javax.mail.internet.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.apache.log4j.Logger;

import org.compiere.wstore.*;
import org.compiere.util.*;

/**
 *  Servlet Utilities
 *
 *  @author Jorg Janke
 *  @version  $Id: WUtil.java,v 1.15 2004/04/27 05:23:33 jjanke Exp $
 */
public final class WUtil
{
	/**	Logging								*/
	private static Logger			s_log = Logger.getLogger("org.compiere.www.WUtil");

	
	/**
	 *  Create Timeout Message
	 *
	 *  @param request request
	 *  @param response response
	 *  @param servlet servlet
	 *  @param ctx context
	 *  @param message - optional message
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public static void createTimeoutPage (HttpServletRequest request, HttpServletResponse response,
		HttpServlet servlet, Properties ctx, String message) throws ServletException, IOException
	{
		s_log.info("createTimeoutPage - " + message);
		String windowTitle = "Timeout";
		if (ctx != null)
			windowTitle = Msg.getMsg(ctx, "Timeout");

		WDoc doc = WDoc.create (windowTitle);

		//	Body
		body body = doc.getBody();
		//  optional message
		if (message != null && message.length() > 0)
			body.addElement(new p(message));

		//  login button
		body.addElement(getLoginButton(ctx));

		//
		body.addElement(new hr());
		body.addElement(new small(servlet.getClass().getName()));
		//	fini
		createResponse (request, response, servlet, null, doc, false);
	}   //  createTimeoutPage

	/**
	 *  Create Error Message
	 *
	 *  @param request request
	 *  @param response response
	 *  @param servlet servlet
	 *  @param ctx context
	 *  @param message message
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public static void createErrorPage (HttpServletRequest request, HttpServletResponse response,
		HttpServlet servlet, Properties ctx, String message) throws ServletException, IOException
	{
		s_log.info("createErrorPage - " + message);
		String windowTitle = "Error";
		if (ctx != null)
			windowTitle = Msg.getMsg(ctx, "Error");		//	JUST TO GET LANGUAGE ??
		if (message != null)
			windowTitle += ": " + message;

		WDoc doc = WDoc.create (windowTitle);

		//	Body
		body b = doc.getBody();

		b.addElement(new p(servlet.getServletName(), AlignType.center));
		b.addElement(new br());

		//	fini
		createResponse (request, response, servlet, null, doc, true);
	}   //  createErrorPage

	/**
	 *  Create Exit Page "Log-off".
	 *  <p>
	 *  - End Session
	 *  - Go to start page (e.g. /compiere/index.html)
	 *
	 *  @param request request
	 *  @param response response
	 *  @param servlet servlet
	 *  @param ctx context
	 *  @param AD_Message messahe
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public static void createLoginPage (HttpServletRequest request, HttpServletResponse response,
		HttpServlet servlet, Properties ctx, String AD_Message) throws ServletException, IOException
	{
		request.getSession().invalidate();
		String url = WEnv.getBaseDirectory("index.html");
		//
		WDoc doc = null;
		if (ctx != null && AD_Message != null && !AD_Message.equals(""))
			doc = WDoc.create (Msg.getMsg(ctx, AD_Message));
		else if (AD_Message != null)
			doc = WDoc.create (AD_Message);
		else
			doc = WDoc.create (false);
		script script = new script("window.top.location.replace('" + url + "');");
		doc.getBody().addElement(script);
		//
		createResponse (request, response, servlet, null, doc, false);
	}   //  createLoginPage

	/**
	 *  Create Login Button - replace Window
	 *
	 *  @param ctx context
	 *  @return Button
	 */
	public static button getLoginButton(Properties ctx)
	{
		String text = Msg.getMsg(ctx, "Login");
		button button = new button();
		button.setType("button").setName("Login").addElement(text);
		StringBuffer cmd = new StringBuffer ("window.top.location.replace('");
		cmd.append(WEnv.getBaseDirectory("index.html"));
		cmd.append("');");
		button.setOnClick(cmd.toString());
		return button;
	}   //  getLoginButton

	
	/**************************************************************************
	 *  Get Cookie Properties
	 *
	 *  @param request request
	 *  @return Properties
	 */
	public static Properties getCookieProprties(HttpServletRequest request)
	{
		//  Get Properties
		Cookie[] cookies = request.getCookies();
		if (cookies != null)
		{
			for (int i = 0; i < cookies.length; i++)
			{
				if (cookies[i].getName().equals(WEnv.COOKIE_INFO))
					return propertiesDecode(cookies[i].getValue());
			}
		}
		return new Properties();
	}   //  getProperties

	
	/**
	 *  Get integer Parameter - 0 if not defined.
	 *
	 *  @param request request
	 *  @param parameter parameter
	 *  @return int result or 0
	 */
	public static int getParameterAsInt (HttpServletRequest request, String parameter)
	{
		if (request == null || parameter == null)
			return 0;
		String data = request.getParameter(parameter);
		if (data == null || data.length() == 0)
			return 0;
		try
		{
			return Integer.parseInt(data);
		}
		catch (Exception e)
		{
			s_log.debug("getParameterAsInt - " + parameter + "=" + data, e);
		}
		return 0;
	}   //  getParameterAsInt

	/**
	 *  Get numeric Parameter - 0 if not defined
	 *
	 *  @param request request
	 *  @param parameter parameter
	 *  @return big decimal result or 0
	 */
	public static BigDecimal getParameterAsBD (HttpServletRequest request, String parameter)
	{
		if (request == null || parameter == null)
			return Env.ZERO;
		String data = request.getParameter(parameter);
		if (data == null || data.length() == 0)
			return Env.ZERO;
		try
		{
			return new BigDecimal (data);
		}
		catch (Exception e)
		{
		}
		try
		{
			DecimalFormat format = DisplayType.getNumberFormat(DisplayType.Number);
			Object oo = format.parseObject(data);
			if (oo instanceof BigDecimal)
				return (BigDecimal)oo;
			else if (oo instanceof Number)
				return new BigDecimal (((Number)oo).doubleValue());
			return new BigDecimal (oo.toString());
		}
		catch (Exception e)
		{
			s_log.debug("getParameterAsBD - " + parameter + "=" + data, e);
		}
		return Env.ZERO;
	}   //  getParameterAsBD

	/**
	 *  Get date Parameter - null if not defined.
	 *	Date portion only
	 *  @param request request
	 *  @param parameter parameter
	 *  @return timestamp result or null
	 */
	public static Timestamp getParameterAsDate(HttpServletRequest request, String parameter)
	{
		if (request == null || parameter == null)
			return null;
		String data = request.getParameter(parameter);
		if (data == null || data.length() == 0)
			return null;
		try
		{
			SimpleDateFormat format = DisplayType.getDateFormat(DisplayType.Date);
			java.util.Date date = format.parse(data);
			if (date != null)
				return new Timestamp (date.getTime());
		}
		catch (Exception e)
		{
			s_log.warn("getParameterAsDate - " + parameter + "=" + data, e);
		}
		return null;
	}   //  getParameterAsDate

	/**
	 *  Get boolean Parameter - false if not defined.
	 *  @param request request
	 *  @param parameter parameter
	 *  @return true if found
	 */
	public static boolean getParameterAsBoolean(HttpServletRequest request, String parameter)
	{
		if (request == null || parameter == null)
			return false;
		String data = request.getParameter(parameter);
		if (data == null || data.length() == 0)
			return false;
		return true;
	}   //  getParameterAsBoolean
	
	/**************************************************************************
	 *  Create Standard Response Header with optional Cookie and print document.
	 *  D:\j2sdk1.4.0\docs\guide\intl\encoding.doc.html
	 *
	 *  @param request request
	 *  @param response response
	 *  @param servlet servlet
	 *  @param cookieProperties cookie properties
	 *  @param doc doc
	 *  @param debug debug
	 *  @throws IOException
	 */
	public static void createResponse (HttpServletRequest request, HttpServletResponse response,
		HttpServlet servlet, Properties cookieProperties, WDoc doc, boolean debug) throws IOException
	{
		response.setHeader("Cache-Control", "no-cache");
	//	response.setContentType("text/html; charset=ISO-8859-1");   //  default
		response.setContentType("text/html; charset=UTF-8");

		//
		//  Update Cookie - overwrite
		if (cookieProperties != null)
		{
			Cookie cookie = new Cookie (WEnv.COOKIE_INFO, propertiesEncode(cookieProperties));
			cookie.setComment("(c) ComPiere, Inc - Jorg Janke");
			cookie.setSecure(false);
			cookie.setPath("/");
			if (cookieProperties.size() == 0)
				cookie.setMaxAge(0);            //  delete cookie
			else
				cookie.setMaxAge(2592000);      //  30 days in seconds   60*60*24*30
			response.addCookie(cookie);
		}
		//  add diagnostics
		if (debug && WEnv.DEBUG)
		{
		//	doc.output(System.out);
			WEnv.addFooter(request, response, servlet, doc.getBody());
		//	doc.output(System.out);
		}
	//	String content = doc.toString();
	//  response.setContentLength(content.length());    //  causes problems at the end of the output

		//  print document
		PrintWriter out = response.getWriter();     //  with character encoding support
		doc.output(out);
		out.flush();
		if (out.checkError())
			s_log.error("createResponse - error writing");
		//  binary output (is faster but does not do character set conversion)
	//	OutputStream out = response.getOutputStream();
	//	byte[] data = doc.toString().getBytes();
	//	response.setContentLength(data.length);
	//	out.write(doc.toString().getBytes());
		//
		out.close();
	}   //  createResponse

	
	/**************************************************************************
	 *  Create Java Script to clear Target frame and display message
	 *
	 *  @param target target
	 *  @return Clear Frame Script
	 */
	public static script getClearFrame (String target)
	{
		StringBuffer cmd = new StringBuffer();
		cmd.append("var d = parent.").append(target).append(".document;\n");
		cmd.append("d.open();\n");
		cmd.append("d.write('<link href=\"").append(WEnv.getStylesheetURL()).append("\" rel=\"stylesheet\">');\n");
		cmd.append("d.close();");
		//
		return new script(cmd.toString());
	}   //  getClearFrame


	/**
	 * 	Return a link and script with new location.
	 * 	@param url forward url
	 * 	@param delaySec delay in seconds (default 3)
	 * 	@return html
	 */
	public static HtmlCode getForward (String url, int delaySec)
	{
		if (delaySec <= 0)
			delaySec = 3;
		HtmlCode retValue = new HtmlCode();
		//	Link
		a a = new a(url);
		a.addElement(url);
		retValue.addElement(a);
		//	Java Script	- document.location - 
		script script = new script("setTimeout(\"window.top.location.replace('" + url 
			+ "')\"," + (delaySec+1000) + ");");
		retValue.addElement(script);
		//
		return retValue;
	}	//	getForward

	/**
	 * 	Create Forward Page
	 * 	@param response response
	 * 	@param title page title
	 * 	@param forwardURL url
	 * 	@param delaySec delay in seconds (default 3)
	 * 	@throws ServletException
	 * 	@throws IOException
	 */
	public static void createForwardPage (HttpServletResponse response,
		String title, String forwardURL, int delaySec) throws ServletException, IOException
	{
		response.setContentType("text/html; charset=UTF-8");
		WDoc doc = WDoc.create(title);
		body b = doc.getBody();
		b.addElement(getForward(forwardURL, delaySec));
		PrintWriter out = response.getWriter();
		doc.output(out);
		out.flush();
		if (out.checkError())
			s_log.error("createForwardPage - error writing");
		out.close();
		s_log.debug("createForwardPage - " + forwardURL + " - " + title);
	}	//	createForwardPage


	/**
	 * 	Does Test exist
	 *	@param test string
	 *	@return true if String with data
	 */
	public static boolean exists (String test)
	{
		if (test == null)
			return false;
		return test.length() > 0;
	}	//	exists

	/**
	 * 	Does Parameter exist
	 * 	@param request request
	 *	@param parameter string
	 *	@return true if String with data
	 */
	public static boolean exists (HttpServletRequest request, String parameter)
	{
		if (request == null || parameter == null)
			return false;
		return exists (request.getParameter(parameter));
	}	//	exists


	/**
	 *	Is EMail address valid
	 * 	@param email mail address
	 * 	@return true if valid
	 */
	public static boolean isEmailValid (String email)
	{
		if (email == null || email.length () == 0)
			return false;
		try
		{
			InternetAddress ia = new InternetAddress (email, true);
			return true;
		}
		catch (AddressException ex)
		{
			s_log.warn ("isEmailValid - " + email + " - "
				+ ex.getLocalizedMessage ());
		}
		return false;
	}	//	isEmailValid


	/**************************************************************************
	 *  Decode Properties into String (URL encoded)
	 *
	 *  @param pp properties
	 *  @return Encoded String
	 */
	public static String propertiesEncode (Properties pp)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try
		{
			pp.store(bos, "Compiere");   //  Header
		}
		catch (IOException e)
		{
			s_log.error("propertiesEncode-store", e);
		}
		String result = new String (bos.toByteArray());
	//	System.out.println("String=" + result);
		String enc = "UTF-8";
		try
		{
			result = java.net.URLEncoder.encode(result, enc);
		}
		catch (UnsupportedEncodingException e)
		{
			s_log.error("propertiesEncode-encode-" + enc, e);
			enc = System.getProperty("file.encoding");      //  Windows default is Cp1252
			try
			{
				result = java.net.URLEncoder.encode(result, enc);
			}
			catch (Exception ex)
			{
				s_log.error("propertiesEncode-encode", ex);
			}
		}
	//	System.out.println("String-Encoded=" + result);
		return result;
	}   //  propertiesEncode

	/**
	 *  Decode data String (URL encoded) into Properties
	 *
	 *  @param data data
	 *  @return Properties
	 */
	public static Properties propertiesDecode (String data)
	{
		String result = null;
	//	System.out.println("String=" + data);
		String enc = "UTF-8";
		try
		{
			result = java.net.URLDecoder.decode(data, enc);
		}
		catch (UnsupportedEncodingException e)
		{
			s_log.error("propertiesDecode-decode-" + enc, e);
			enc = System.getProperty("file.encoding");      //  Windows default is Cp1252
			try
			{
				result = java.net.URLEncoder.encode(data, enc);
			}
			catch (Exception ex)
			{
				s_log.error("propertiesDecode-decode", ex);
			}
		}
	//	System.out.println("String-Decoded=" + result);

		ByteArrayInputStream bis = new ByteArrayInputStream(result.getBytes());
		Properties pp = new Properties();
		try
		{
			pp.load(bis);
		}
		catch (IOException e)
		{
			s_log.error("propertiesDecode-load", e);
		}
		return pp;
	}   //  propertiesDecode

	
	/**************************************************************************
	 *  Convert Array of NamePair to HTTP Option Array.
	 *  <p>
	 *  If the ArrayList does not contain NamePairs, the String value is used
	 *  @see org.compiere.util.NamePair
	 *  @param  list    ArrayList containing NamePair values
	 *  @param  default_ID  Sets the default if the key/ID value is found.
	 *      If the value is null or empty, the first value is selected
	 *  @return Option Array
	 */
	public static option[] convertToOption (NamePair[] list, String default_ID)
	{
		int size = list.length;
		option[] retValue = new option[size];
		for (int i = 0; i < size; i++)
		{
			boolean selected = false;
			//  select first entry
			if (i == 0 && (default_ID == null || default_ID.length() == 0))
				selected = true;

			//  Create option
			retValue[i] = new option(list[i].getID()).addElement(list[i].getName());

			//  Select if ID/Key is same as default ID
			if (default_ID != null && default_ID.equals(list[i].getID()))
				selected = true;
			retValue[i].setSelected(selected);
		}
		return retValue;
	}   //  convertToOption

	/**
	 *  Create label/field table row
	 *
	 *  @param line - null for new line (table row)
	 *  @param FORMNAME form name
	 *  @param PARAMETER parameter name
	 *  @param labelText label
	 *  @param inputType HTML input type
	 *  @param value data value
	 *  @param sizeDisplay display size
	 *  @param size data size
	 *  @param longField field spanning two columns
	 *  @param mandatory mark as mandatory
	 *  @param onChange onChange call
	 *  @param script script
	 *  @return tr table row
	 */
	static public tr createField (tr line, String FORMNAME, String PARAMETER,
		String labelText, String inputType, Object value,
		int sizeDisplay, int size, boolean longField, boolean mandatory, String onChange, StringBuffer script)
	{
		if (line == null)
			line = new tr();
		String labelInfo = labelText;
		if (mandatory)
		{
			labelInfo += "&nbsp;<font color=\"red\">*</font>";
			String fName = "document." + FORMNAME + "." + PARAMETER;
			script.append(fName).append(".required=true; ");
		}

		label llabel = new label().setFor(PARAMETER).addElement(labelInfo);
		llabel.setID("ID_" + PARAMETER + "_Label");
	//	label.setTitle(description);
		line.addElement(new td().addElement(llabel).setAlign(AlignType.right));
		input iinput = new input(inputType, PARAMETER, value == null ? "" : value.toString());
		iinput.setSize(sizeDisplay).setMaxlength(size);
		iinput.setID("ID_" + PARAMETER);
		if (onChange != null && onChange.length() > 0)
			iinput.setOnChange(onChange);
		iinput.setTitle(labelText);
		td field = new td().addElement(iinput).setAlign(AlignType.LEFT);
		if (longField)
			field.setColSpan(3);
		line.addElement(field);
		return line;
	}   //  addField

}   //  WUtil
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
import java.util.*;
import java.sql.*;
import java.io.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.apache.log4j.Logger;

import org.compiere.util.*;
import org.compiere.model.*;

/**
 *  Servlet Utilities
 *
 *  @author Jorg Janke
 *  @version  $Id: WUtil.java,v 1.3 2002/10/30 05:05:17 jjanke Exp $
 */
public final class WUtil
{
	/**	Logging								*/
	private static Logger			s_log = Logger.getLogger("org.compiere.www.WUtil");

	/**
	 *  Create Timeout Message
	 *
	 *  @param request request
	 *  @param response response
	 *  @param servlet servlet
	 *  @param ctx context
	 *  @param message - optional message
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public static void createTimeoutPage (HttpServletRequest request, HttpServletResponse response,
		HttpServlet servlet, Properties ctx, String message) throws ServletException, IOException
	{
		s_log.info("createTimeoutPage - " + message);
		String windowTitle = "Timeout";
		if (ctx != null)
			windowTitle = Msg.getMsg(ctx, "Timeout");

		WDoc doc = WDoc.create (windowTitle);

		//	Body
		body body = doc.getBody();
		//  optional message
		if (message != null && message.length() > 0)
			body.addElement(new p(message));

		//  login button
		body.addElement(getLoginButton(ctx));

		//
		body.addElement(new hr());
		body.addElement(new small(servlet.getClass().getName()));
		//	fini
		createResponse (request, response, servlet, null, doc, false);
	}   //  createTimeoutPage

	/**
	 *  Create Error Message
	 *
	 *  @param request request
	 *  @param response response
	 *  @param servlet servlet
	 *  @param ctx context
	 *  @param message message
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public static void createErrorPage (HttpServletRequest request, HttpServletResponse response,
		HttpServlet servlet, Properties ctx, String message) throws ServletException, IOException
	{
		s_log.info("createErrorPage - " + message);
		String windowTitle = "Error";
		if (ctx != null)
			windowTitle = Msg.getMsg(ctx, "Error");		//	JUST TO GET LANGUAGE ??
		if (message != null)
			windowTitle += ": " + message;

		WDoc doc = WDoc.create (windowTitle);

		//	Body
		body b = doc.getBody();

		b.addElement(new p(servlet.getServletName(), AlignType.center));
		b.addElement(new br());

		//	fini
		createResponse (request, response, servlet, null, doc, true);
	}   //  createErrorPage

	/**
	 *  Create Exit Page "Log-off".
	 *  <p>
	 *  - End Session
	 *  - Go to start page (e.g. /compiere/index.html)
	 *
	 *  @param request request
	 *  @param response response
	 *  @param servlet servlet
	 *  @param ctx context
	 *  @param AD_Message messahe
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public static void createLoginPage (HttpServletRequest request, HttpServletResponse response,
		HttpServlet servlet, Properties ctx, String AD_Message) throws ServletException, IOException
	{
		request.getSession().invalidate();
		String url = WEnv.getBaseDirectory("index.html");
		//
		WDoc doc = null;
		if (ctx != null && AD_Message != null && !AD_Message.equals(""))
			doc = WDoc.create (Msg.getMsg(ctx, AD_Message));
		else if (AD_Message != null)
			doc = WDoc.create (AD_Message);
		else
			doc = WDoc.create (false);
		script script = new script("window.top.location.replace('" + url + "');");
		doc.getBody().addElement(script);
		//
		createResponse (request, response, servlet, null, doc, false);
	}   //  exit

	/**
	 *  Create Login Button - replace Window
	 *
	 *  @param ctx context
	 *  @return Button
	 */
	public static button getLoginButton(Properties ctx)
	{
		String text = Msg.getMsg(ctx, "Login");
		button button = new button();
		button.setType("button").setName("Login").addElement(text);
		StringBuffer cmd = new StringBuffer ("window.top.location.replace('");
		cmd.append(WEnv.getBaseDirectory("index.html"));
		cmd.append("');");
		button.setOnClick(cmd.toString());
		return button;
	}   //  getLoginButton

	/*************************************************************************/

	/**
	 *  Get Cookie Properties
	 *
	 *  @param request request
	 *  @return Properties
	 */
	public static Properties getCookieProprties(HttpServletRequest request)
	{
		//  Get Properties
		Cookie[] cookies = request.getCookies();
		if (cookies != null)
		{
			for (int i = 0; i < cookies.length; i++)
			{
				if (cookies[i].getName().equals(WEnv.COOKIE_INFO))
					return propertiesDecode(cookies[i].getValue());
			}
		}
		return new Properties();
	}   //  getProperties

	/**
	 *  Get numeric Parameter - 0 if not defined
	 *
	 *  @param request request
	 *  @param parameter parameter
	 *  @return int result
	 */
	public static int getParameterAsInt(HttpServletRequest request, String parameter)
	{
		if (request == null || parameter == null)
			return 0;
		String data = request.getParameter(parameter);
		if (data == null || data.length() == 0)
			return 0;
		try
		{
			return Integer.parseInt(data);
		}
		catch (Exception e)
		{
			s_log.debug("getParameterAsInt - " + parameter + "=" + data, e);
		}
		return 0;
	}   //  getParameter

	/*************************************************************************/

	/**
	 *  Create Standard Response Header with optional Cookie and print document.
	 *  D:\j2sdk1.4.0\docs\guide\intl\encoding.doc.html
	 *
	 *  @param request request
	 *  @param response response
	 *  @param servlet servlet
	 *  @param cookieProperties cookie properties
	 *  @param doc doc
	 *  @param debug debug
	 *  @throws IOException
	 */
	public static void createResponse (HttpServletRequest request, HttpServletResponse response,
		HttpServlet servlet, Properties cookieProperties, WDoc doc, boolean debug) throws IOException
	{
		response.setHeader("Cache-Control", "no-cache");
	//	response.setContentType("text/html; charset=ISO-8859-1");   //  default
		response.setContentType("text/html; charset=UTF-8");

		//
		//  Update Cookie - overwrite
		if (cookieProperties != null)
		{
			Cookie cookie = new Cookie (WEnv.COOKIE_INFO, propertiesEncode(cookieProperties));
			cookie.setComment("(c) ComPiere, Inc - Jorg Janke");
			cookie.setSecure(false);
			cookie.setPath("/");
			if (cookieProperties.size() == 0)
				cookie.setMaxAge(0);            //  delete cookie
			else
				cookie.setMaxAge(2592000);      //  30 days in seconds   60*60*24*30
			response.addCookie(cookie);
		}
		//  add diagnostics
		if (debug && WEnv.DEBUG)
		{
		//	doc.output(System.out);
			WEnv.addFooter(request, response, servlet, doc.getBody());
		//	doc.output(System.out);
		}
	//	String content = doc.toString();
	//  response.setContentLength(content.length());    //  causes problems at the end of the output

		//  print document
		PrintWriter out = response.getWriter();     //  with character encoding support
		doc.output(out);
		out.flush();
		if (out.checkError())
			s_log.error("createResponse - error writing");
		//  binary output (is faster but does not do character set conversion)
	//	OutputStream out = response.getOutputStream();
	//	byte[] data = doc.toString().getBytes();
	//	response.setContentLength(data.length);
	//	out.write(doc.toString().getBytes());
		//
		out.close();
	}   //  createResponse

	/*************************************************************************/

	/**
	 *  Create Java Script to clear Target frame and display message
	 *
	 *  @param target target
	 *  @return Clear Frame Script
	 */
	public static script getClearFrame (String target)
	{
		StringBuffer cmd = new StringBuffer();
		cmd.append("var d = parent.").append(target).append(".document;\n");
		cmd.append("d.open();\n");
		cmd.append("d.write('<link href=\"").append(WEnv.getStylesheetURL()).append("\" rel=\"stylesheet\">');\n");
		cmd.append("d.close();");
		//
		return new script(cmd.toString());
	}   //  getClearFrame

	/*************************************************************************/

	/**
	 *  Decode Properties into String (URL encoded)
	 *
	 *  @param pp properties
	 *  @return Encoded String
	 */
	public static String propertiesEncode (Properties pp)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try
		{
			pp.store(bos, "Compiere");   //  Header
		}
		catch (IOException e)
		{
			s_log.error("propertiesEncode-store", e);
		}
		String result = new String (bos.toByteArray());
	//	System.out.println("String=" + result);
		String enc = "UTF-8";
		try
		{
			result = java.net.URLEncoder.encode(result, enc);
		}
		catch (UnsupportedEncodingException e)
		{
			s_log.error("propertiesEncode-encode-" + enc, e);
			enc = System.getProperty("file.encoding");      //  Windows default is Cp1252
			try
			{
				result = java.net.URLEncoder.encode(result, enc);
			}
			catch (Exception ex)
			{
				s_log.error("propertiesEncode-encode", ex);
			}
		}
	//	System.out.println("String-Encoded=" + result);
		return result;
	}   //  propertiesEncode

	/**
	 *  Decode data String (URL encoded) into Properties
	 *
	 *  @param data data
	 *  @return Properties
	 */
	public static Properties propertiesDecode (String data)
	{
		String result = null;
	//	System.out.println("String=" + data);
		String enc = "UTF-8";
		try
		{
			result = java.net.URLDecoder.decode(data, enc);
		}
		catch (UnsupportedEncodingException e)
		{
			s_log.error("propertiesDecode-decode-" + enc, e);
			enc = System.getProperty("file.encoding");      //  Windows default is Cp1252
			try
			{
				result = java.net.URLEncoder.encode(data, enc);
			}
			catch (Exception ex)
			{
				s_log.error("propertiesDecode-decode", ex);
			}
		}
	//	System.out.println("String-Decoded=" + result);

		ByteArrayInputStream bis = new ByteArrayInputStream(result.getBytes());
		Properties pp = new Properties();
		try
		{
			pp.load(bis);
		}
		catch (IOException e)
		{
			s_log.error("propertiesDecode-load", e);
		}
		return pp;
	}   //  propertiesDecode

	/*************************************************************************/

	/**
	 *  Convert Array of NamePair to HTTP Option Array.
	 *  <p>
	 *  If the ArrayList does not contain NamePairs, the String value is used
	 *  @see org.compiere.util.NamePair
	 *  @param  list    ArrayList containing NamePair values
	 *  @param  default_ID  Sets the default if the key/ID value is found.
	 *      If the value is null or empty, the first value is selected
	 *  @return Option Array
	 */
	public static option[] convertToOption (NamePair[] list, String default_ID)
	{
		int size = list.length;
		option[] retValue = new option[size];
		for (int i = 0; i < size; i++)
		{
			boolean selected = false;
			//  select first entry
			if (i == 0 && (default_ID == null || default_ID.length() == 0))
				selected = true;

			//  Create option
			retValue[i] = new option(list[i].getID()).addElement(list[i].getName());

			//  Select if ID/Key is same as default ID
			if (default_ID != null && default_ID.equals(list[i].getID()))
				selected = true;
			retValue[i].setSelected(selected);
		}
		return retValue;
	}   //  convertToOption

	/**
	 *  Create label/field table row
	 *
	 *  @param line - null for new line (table row)
	 *  @param FORMNAME form name
	 *  @param PARAMETER parameter name
	 *  @param labelText label
	 *  @param inputType HTML input type
	 *  @param value data value
	 *  @param sizeDisplay display size
	 *  @param size data size
	 *  @param longField field spanning two columns
	 *  @param mandatory mark as mandatory
	 *  @param onChange onChange call
	 *  @param script script
	 *  @return tr table row
	 */
	static public tr createField (tr line, String FORMNAME, String PARAMETER,
		String labelText, String inputType, Object value,
		int sizeDisplay, int size, boolean longField, boolean mandatory, String onChange, StringBuffer script)
	{
		if (line == null)
			line = new tr();
		String labelInfo = labelText;
		if (mandatory)
		{
			labelInfo += "&nbsp;<font color=\"red\">*</font>";
			String fName = "document." + FORMNAME + "." + PARAMETER;
			script.append(fName).append(".required=true; ");
		}

		label llabel = new label().setFor(PARAMETER).addElement(labelInfo);
		llabel.setID("ID_" + PARAMETER + "_Label");
	//	label.setTitle(description);
		line.addElement(new td().addElement(llabel).setAlign(AlignType.right));
		input iinput = new input(inputType, PARAMETER, value == null ? "" : value.toString());
		iinput.setSize(sizeDisplay).setMaxlength(size);
		iinput.setID("ID_" + PARAMETER);
		if (onChange != null && onChange.length() > 0)
			iinput.setOnChange(onChange);
		iinput.setTitle(labelText);
		td field = new td().addElement(iinput).setAlign(AlignType.LEFT);
		if (longField)
			field.setColSpan(3);
		line.addElement(field);
		return line;
	}   //  addField


}   //  WUtil
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
import java.util.*;
import java.sql.*;
import java.io.*;
import javax.mail.internet.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.apache.log4j.Logger;

import org.compiere.wstore.*;
import org.compiere.util.*;
import org.compiere.model.*;

/**
 *  Servlet Utilities
 *
 *  @author Jorg Janke
 *  @version  $Id: WUtil.java,v 1.12 2003/07/19 05:21:44 jjanke Exp $
 */
public final class WUtil
{
	/**	Logging								*/
	private static Logger			s_log = Logger.getLogger("org.compiere.www.WUtil");

	/**
	 *  Create Timeout Message
	 *
	 *  @param request request
	 *  @param response response
	 *  @param servlet servlet
	 *  @param ctx context
	 *  @param message - optional message
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public static void createTimeoutPage (HttpServletRequest request, HttpServletResponse response,
		HttpServlet servlet, Properties ctx, String message) throws ServletException, IOException
	{
		s_log.info("createTimeoutPage - " + message);
		String windowTitle = "Timeout";
		if (ctx != null)
			windowTitle = Msg.getMsg(ctx, "Timeout");

		WDoc doc = WDoc.create (windowTitle);

		//	Body
		body body = doc.getBody();
		//  optional message
		if (message != null && message.length() > 0)
			body.addElement(new p(message));

		//  login button
		body.addElement(getLoginButton(ctx));

		//
		body.addElement(new hr());
		body.addElement(new small(servlet.getClass().getName()));
		//	fini
		createResponse (request, response, servlet, null, doc, false);
	}   //  createTimeoutPage

	/**
	 *  Create Error Message
	 *
	 *  @param request request
	 *  @param response response
	 *  @param servlet servlet
	 *  @param ctx context
	 *  @param message message
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public static void createErrorPage (HttpServletRequest request, HttpServletResponse response,
		HttpServlet servlet, Properties ctx, String message) throws ServletException, IOException
	{
		s_log.info("createErrorPage - " + message);
		String windowTitle = "Error";
		if (ctx != null)
			windowTitle = Msg.getMsg(ctx, "Error");		//	JUST TO GET LANGUAGE ??
		if (message != null)
			windowTitle += ": " + message;

		WDoc doc = WDoc.create (windowTitle);

		//	Body
		body b = doc.getBody();

		b.addElement(new p(servlet.getServletName(), AlignType.center));
		b.addElement(new br());

		//	fini
		createResponse (request, response, servlet, null, doc, true);
	}   //  createErrorPage

	/**
	 *  Create Exit Page "Log-off".
	 *  <p>
	 *  - End Session
	 *  - Go to start page (e.g. /compiere/index.html)
	 *
	 *  @param request request
	 *  @param response response
	 *  @param servlet servlet
	 *  @param ctx context
	 *  @param AD_Message messahe
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public static void createLoginPage (HttpServletRequest request, HttpServletResponse response,
		HttpServlet servlet, Properties ctx, String AD_Message) throws ServletException, IOException
	{
		request.getSession().invalidate();
		String url = WEnv.getBaseDirectory("index.html");
		//
		WDoc doc = null;
		if (ctx != null && AD_Message != null && !AD_Message.equals(""))
			doc = WDoc.create (Msg.getMsg(ctx, AD_Message));
		else if (AD_Message != null)
			doc = WDoc.create (AD_Message);
		else
			doc = WDoc.create (false);
		script script = new script("window.top.location.replace('" + url + "');");
		doc.getBody().addElement(script);
		//
		createResponse (request, response, servlet, null, doc, false);
	}   //  createLoginPage

	/**
	 *  Create Login Button - replace Window
	 *
	 *  @param ctx context
	 *  @return Button
	 */
	public static button getLoginButton(Properties ctx)
	{
		String text = Msg.getMsg(ctx, "Login");
		button button = new button();
		button.setType("button").setName("Login").addElement(text);
		StringBuffer cmd = new StringBuffer ("window.top.location.replace('");
		cmd.append(WEnv.getBaseDirectory("index.html"));
		cmd.append("');");
		button.setOnClick(cmd.toString());
		return button;
	}   //  getLoginButton

	/*************************************************************************/

	/**
	 *  Get Cookie Properties
	 *
	 *  @param request request
	 *  @return Properties
	 */
	public static Properties getCookieProprties(HttpServletRequest request)
	{
		//  Get Properties
		Cookie[] cookies = request.getCookies();
		if (cookies != null)
		{
			for (int i = 0; i < cookies.length; i++)
			{
				if (cookies[i].getName().equals(WEnv.COOKIE_INFO))
					return propertiesDecode(cookies[i].getValue());
			}
		}
		return new Properties();
	}   //  getProperties

	/**
	 *  Get numeric Parameter - 0 if not defined
	 *
	 *  @param request request
	 *  @param parameter parameter
	 *  @return int result or 0
	 */
	public static int getParameterAsInt(HttpServletRequest request, String parameter)
	{
		if (request == null || parameter == null)
			return 0;
		String data = request.getParameter(parameter);
		if (data == null || data.length() == 0)
			return 0;
		try
		{
			return Integer.parseInt(data);
		}
		catch (Exception e)
		{
			s_log.debug("getParameterAsInt - " + parameter + "=" + data, e);
		}
		return 0;
	}   //  getParameter

	/*************************************************************************/

	/**
	 *  Create Standard Response Header with optional Cookie and print document.
	 *  D:\j2sdk1.4.0\docs\guide\intl\encoding.doc.html
	 *
	 *  @param request request
	 *  @param response response
	 *  @param servlet servlet
	 *  @param cookieProperties cookie properties
	 *  @param doc doc
	 *  @param debug debug
	 *  @throws IOException
	 */
	public static void createResponse (HttpServletRequest request, HttpServletResponse response,
		HttpServlet servlet, Properties cookieProperties, WDoc doc, boolean debug) throws IOException
	{
		response.setHeader("Cache-Control", "no-cache");
	//	response.setContentType("text/html; charset=ISO-8859-1");   //  default
		response.setContentType("text/html; charset=UTF-8");

		//
		//  Update Cookie - overwrite
		if (cookieProperties != null)
		{
			Cookie cookie = new Cookie (WEnv.COOKIE_INFO, propertiesEncode(cookieProperties));
			cookie.setComment("(c) ComPiere, Inc - Jorg Janke");
			cookie.setSecure(false);
			cookie.setPath("/");
			if (cookieProperties.size() == 0)
				cookie.setMaxAge(0);            //  delete cookie
			else
				cookie.setMaxAge(2592000);      //  30 days in seconds   60*60*24*30
			response.addCookie(cookie);
		}
		//  add diagnostics
		if (debug && WEnv.DEBUG)
		{
		//	doc.output(System.out);
			WEnv.addFooter(request, response, servlet, doc.getBody());
		//	doc.output(System.out);
		}
	//	String content = doc.toString();
	//  response.setContentLength(content.length());    //  causes problems at the end of the output

		//  print document
		PrintWriter out = response.getWriter();     //  with character encoding support
		doc.output(out);
		out.flush();
		if (out.checkError())
			s_log.error("createResponse - error writing");
		//  binary output (is faster but does not do character set conversion)
	//	OutputStream out = response.getOutputStream();
	//	byte[] data = doc.toString().getBytes();
	//	response.setContentLength(data.length);
	//	out.write(doc.toString().getBytes());
		//
		out.close();
	}   //  createResponse

	/*************************************************************************/

	/**
	 *  Create Java Script to clear Target frame and display message
	 *
	 *  @param target target
	 *  @return Clear Frame Script
	 */
	public static script getClearFrame (String target)
	{
		StringBuffer cmd = new StringBuffer();
		cmd.append("var d = parent.").append(target).append(".document;\n");
		cmd.append("d.open();\n");
		cmd.append("d.write('<link href=\"").append(WEnv.getStylesheetURL()).append("\" rel=\"stylesheet\">');\n");
		cmd.append("d.close();");
		//
		return new script(cmd.toString());
	}   //  getClearFrame


	/**
	 * 	Return a link and script with new location.
	 * 	Wait 2 seconds
	 * 	@param url forward url
	 * 	@return html
	 */
	public static HtmlCode getForward (String url)
	{
		HtmlCode retValue = new HtmlCode();
		//	Link
		a a = new a(url);
		a.addElement(url);
		retValue.addElement(a);
		//	Java Script	- document.location - 2.5 sec delay
		script script = new script("setTimeout(\"window.top.location.replace('" + url + "')\",2500);");
		retValue.addElement(script);
		//
		return retValue;
	}	//	getForward

	/**
	 * 	Create Forward Page
	 * 	@param response response
	 * 	@param title page title
	 * 	@param forwardURL url
	 * 	@throws ServletException
	 * 	@throws IOException
	 */
	public static void createForwardPage (HttpServletResponse response,
		String title, String forwardURL) throws ServletException, IOException
	{
		response.setContentType("text/html; charset=UTF-8");
		WDoc doc = WDoc.create(title);
		body b = doc.getBody();
		b.addElement(getForward(forwardURL));
		PrintWriter out = response.getWriter();
		doc.output(out);
		out.flush();
		if (out.checkError())
			s_log.error("createForwardPage - error writing");
		out.close();
		s_log.debug("createForwardPage - " + forwardURL + " - " + title);
	}	//	createForwardPage


	/**
	 * 	Does Test exist
	 *	@param test string
	 *	@return true if String with data
	 */
	public static boolean exists (String test)
	{
		if (test == null)
			return false;
		return test.length() > 0;
	}	//	exists

	/**
	 * 	Does Parameter exist
	 * 	@param request request
	 *	@param parameter string
	 *	@return true if String with data
	 */
	public static boolean exists (HttpServletRequest request, String parameter)
	{
		if (request == null || parameter == null)
			return false;
		return exists (request.getParameter(parameter));
	}	//	exists


	/**
	 *	Is EMail address valid
	 * 	@param email mail address
	 * 	@return true if valid
	 */
	public static boolean isEmailValid (String email)
	{
		if (email == null || email.length () == 0)
			return false;
		try
		{
			InternetAddress ia = new InternetAddress (email, true);
			return true;
		}
		catch (AddressException ex)
		{
			s_log.warn ("isEmailValid - " + email + " - "
				+ ex.getLocalizedMessage ());
		}
		return false;
	}	//	isEmailValid


	/*************************************************************************/

	/**
	 *  Decode Properties into String (URL encoded)
	 *
	 *  @param pp properties
	 *  @return Encoded String
	 */
	public static String propertiesEncode (Properties pp)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try
		{
			pp.store(bos, "Compiere");   //  Header
		}
		catch (IOException e)
		{
			s_log.error("propertiesEncode-store", e);
		}
		String result = new String (bos.toByteArray());
	//	System.out.println("String=" + result);
		String enc = "UTF-8";
		try
		{
			result = java.net.URLEncoder.encode(result, enc);
		}
		catch (UnsupportedEncodingException e)
		{
			s_log.error("propertiesEncode-encode-" + enc, e);
			enc = System.getProperty("file.encoding");      //  Windows default is Cp1252
			try
			{
				result = java.net.URLEncoder.encode(result, enc);
			}
			catch (Exception ex)
			{
				s_log.error("propertiesEncode-encode", ex);
			}
		}
	//	System.out.println("String-Encoded=" + result);
		return result;
	}   //  propertiesEncode

	/**
	 *  Decode data String (URL encoded) into Properties
	 *
	 *  @param data data
	 *  @return Properties
	 */
	public static Properties propertiesDecode (String data)
	{
		String result = null;
	//	System.out.println("String=" + data);
		String enc = "UTF-8";
		try
		{
			result = java.net.URLDecoder.decode(data, enc);
		}
		catch (UnsupportedEncodingException e)
		{
			s_log.error("propertiesDecode-decode-" + enc, e);
			enc = System.getProperty("file.encoding");      //  Windows default is Cp1252
			try
			{
				result = java.net.URLEncoder.encode(data, enc);
			}
			catch (Exception ex)
			{
				s_log.error("propertiesDecode-decode", ex);
			}
		}
	//	System.out.println("String-Decoded=" + result);

		ByteArrayInputStream bis = new ByteArrayInputStream(result.getBytes());
		Properties pp = new Properties();
		try
		{
			pp.load(bis);
		}
		catch (IOException e)
		{
			s_log.error("propertiesDecode-load", e);
		}
		return pp;
	}   //  propertiesDecode

	/*************************************************************************/

	/**
	 *  Convert Array of NamePair to HTTP Option Array.
	 *  <p>
	 *  If the ArrayList does not contain NamePairs, the String value is used
	 *  @see org.compiere.util.NamePair
	 *  @param  list    ArrayList containing NamePair values
	 *  @param  default_ID  Sets the default if the key/ID value is found.
	 *      If the value is null or empty, the first value is selected
	 *  @return Option Array
	 */
	public static option[] convertToOption (NamePair[] list, String default_ID)
	{
		int size = list.length;
		option[] retValue = new option[size];
		for (int i = 0; i < size; i++)
		{
			boolean selected = false;
			//  select first entry
			if (i == 0 && (default_ID == null || default_ID.length() == 0))
				selected = true;

			//  Create option
			retValue[i] = new option(list[i].getID()).addElement(list[i].getName());

			//  Select if ID/Key is same as default ID
			if (default_ID != null && default_ID.equals(list[i].getID()))
				selected = true;
			retValue[i].setSelected(selected);
		}
		return retValue;
	}   //  convertToOption

	/**
	 *  Create label/field table row
	 *
	 *  @param line - null for new line (table row)
	 *  @param FORMNAME form name
	 *  @param PARAMETER parameter name
	 *  @param labelText label
	 *  @param inputType HTML input type
	 *  @param value data value
	 *  @param sizeDisplay display size
	 *  @param size data size
	 *  @param longField field spanning two columns
	 *  @param mandatory mark as mandatory
	 *  @param onChange onChange call
	 *  @param script script
	 *  @return tr table row
	 */
	static public tr createField (tr line, String FORMNAME, String PARAMETER,
		String labelText, String inputType, Object value,
		int sizeDisplay, int size, boolean longField, boolean mandatory, String onChange, StringBuffer script)
	{
		if (line == null)
			line = new tr();
		String labelInfo = labelText;
		if (mandatory)
		{
			labelInfo += "&nbsp;<font color=\"red\">*</font>";
			String fName = "document." + FORMNAME + "." + PARAMETER;
			script.append(fName).append(".required=true; ");
		}

		label llabel = new label().setFor(PARAMETER).addElement(labelInfo);
		llabel.setID("ID_" + PARAMETER + "_Label");
	//	label.setTitle(description);
		line.addElement(new td().addElement(llabel).setAlign(AlignType.right));
		input iinput = new input(inputType, PARAMETER, value == null ? "" : value.toString());
		iinput.setSize(sizeDisplay).setMaxlength(size);
		iinput.setID("ID_" + PARAMETER);
		if (onChange != null && onChange.length() > 0)
			iinput.setOnChange(onChange);
		iinput.setTitle(labelText);
		td field = new td().addElement(iinput).setAlign(AlignType.LEFT);
		if (longField)
			field.setColSpan(3);
		line.addElement(field);
		return line;
	}   //  addField

}   //  WUtil
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
import java.util.*;
import java.sql.*;
import java.io.*;
import javax.mail.internet.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.apache.log4j.Logger;

import org.compiere.wstore.*;
import org.compiere.util.*;
import org.compiere.model.*;

/**
 *  Servlet Utilities
 *
 *  @author Jorg Janke
 *  @version  $Id: WUtil.java,v 1.12 2003/07/19 05:21:44 jjanke Exp $
 */
public final class WUtil
{
	/**	Logging								*/
	private static Logger			s_log = Logger.getLogger("org.compiere.www.WUtil");

	/**
	 *  Create Timeout Message
	 *
	 *  @param request request
	 *  @param response response
	 *  @param servlet servlet
	 *  @param ctx context
	 *  @param message - optional message
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public static void createTimeoutPage (HttpServletRequest request, HttpServletResponse response,
		HttpServlet servlet, Properties ctx, String message) throws ServletException, IOException
	{
		s_log.info("createTimeoutPage - " + message);
		String windowTitle = "Timeout";
		if (ctx != null)
			windowTitle = Msg.getMsg(ctx, "Timeout");

		WDoc doc = WDoc.create (windowTitle);

		//	Body
		body body = doc.getBody();
		//  optional message
		if (message != null && message.length() > 0)
			body.addElement(new p(message));

		//  login button
		body.addElement(getLoginButton(ctx));

		//
		body.addElement(new hr());
		body.addElement(new small(servlet.getClass().getName()));
		//	fini
		createResponse (request, response, servlet, null, doc, false);
	}   //  createTimeoutPage

	/**
	 *  Create Error Message
	 *
	 *  @param request request
	 *  @param response response
	 *  @param servlet servlet
	 *  @param ctx context
	 *  @param message message
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public static void createErrorPage (HttpServletRequest request, HttpServletResponse response,
		HttpServlet servlet, Properties ctx, String message) throws ServletException, IOException
	{
		s_log.info("createErrorPage - " + message);
		String windowTitle = "Error";
		if (ctx != null)
			windowTitle = Msg.getMsg(ctx, "Error");		//	JUST TO GET LANGUAGE ??
		if (message != null)
			windowTitle += ": " + message;

		WDoc doc = WDoc.create (windowTitle);

		//	Body
		body b = doc.getBody();

		b.addElement(new p(servlet.getServletName(), AlignType.center));
		b.addElement(new br());

		//	fini
		createResponse (request, response, servlet, null, doc, true);
	}   //  createErrorPage

	/**
	 *  Create Exit Page "Log-off".
	 *  <p>
	 *  - End Session
	 *  - Go to start page (e.g. /compiere/index.html)
	 *
	 *  @param request request
	 *  @param response response
	 *  @param servlet servlet
	 *  @param ctx context
	 *  @param AD_Message messahe
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public static void createLoginPage (HttpServletRequest request, HttpServletResponse response,
		HttpServlet servlet, Properties ctx, String AD_Message) throws ServletException, IOException
	{
		request.getSession().invalidate();
		String url = WEnv.getBaseDirectory("index.html");
		//
		WDoc doc = null;
		if (ctx != null && AD_Message != null && !AD_Message.equals(""))
			doc = WDoc.create (Msg.getMsg(ctx, AD_Message));
		else if (AD_Message != null)
			doc = WDoc.create (AD_Message);
		else
			doc = WDoc.create (false);
		script script = new script("window.top.location.replace('" + url + "');");
		doc.getBody().addElement(script);
		//
		createResponse (request, response, servlet, null, doc, false);
	}   //  createLoginPage

	/**
	 *  Create Login Button - replace Window
	 *
	 *  @param ctx context
	 *  @return Button
	 */
	public static button getLoginButton(Properties ctx)
	{
		String text = Msg.getMsg(ctx, "Login");
		button button = new button();
		button.setType("button").setName("Login").addElement(text);
		StringBuffer cmd = new StringBuffer ("window.top.location.replace('");
		cmd.append(WEnv.getBaseDirectory("index.html"));
		cmd.append("');");
		button.setOnClick(cmd.toString());
		return button;
	}   //  getLoginButton

	/*************************************************************************/

	/**
	 *  Get Cookie Properties
	 *
	 *  @param request request
	 *  @return Properties
	 */
	public static Properties getCookieProprties(HttpServletRequest request)
	{
		//  Get Properties
		Cookie[] cookies = request.getCookies();
		if (cookies != null)
		{
			for (int i = 0; i < cookies.length; i++)
			{
				if (cookies[i].getName().equals(WEnv.COOKIE_INFO))
					return propertiesDecode(cookies[i].getValue());
			}
		}
		return new Properties();
	}   //  getProperties

	/**
	 *  Get numeric Parameter - 0 if not defined
	 *
	 *  @param request request
	 *  @param parameter parameter
	 *  @return int result or 0
	 */
	public static int getParameterAsInt(HttpServletRequest request, String parameter)
	{
		if (request == null || parameter == null)
			return 0;
		String data = request.getParameter(parameter);
		if (data == null || data.length() == 0)
			return 0;
		try
		{
			return Integer.parseInt(data);
		}
		catch (Exception e)
		{
			s_log.debug("getParameterAsInt - " + parameter + "=" + data, e);
		}
		return 0;
	}   //  getParameter

	/*************************************************************************/

	/**
	 *  Create Standard Response Header with optional Cookie and print document.
	 *  D:\j2sdk1.4.0\docs\guide\intl\encoding.doc.html
	 *
	 *  @param request request
	 *  @param response response
	 *  @param servlet servlet
	 *  @param cookieProperties cookie properties
	 *  @param doc doc
	 *  @param debug debug
	 *  @throws IOException
	 */
	public static void createResponse (HttpServletRequest request, HttpServletResponse response,
		HttpServlet servlet, Properties cookieProperties, WDoc doc, boolean debug) throws IOException
	{
		response.setHeader("Cache-Control", "no-cache");
	//	response.setContentType("text/html; charset=ISO-8859-1");   //  default
		response.setContentType("text/html; charset=UTF-8");

		//
		//  Update Cookie - overwrite
		if (cookieProperties != null)
		{
			Cookie cookie = new Cookie (WEnv.COOKIE_INFO, propertiesEncode(cookieProperties));
			cookie.setComment("(c) ComPiere, Inc - Jorg Janke");
			cookie.setSecure(false);
			cookie.setPath("/");
			if (cookieProperties.size() == 0)
				cookie.setMaxAge(0);            //  delete cookie
			else
				cookie.setMaxAge(2592000);      //  30 days in seconds   60*60*24*30
			response.addCookie(cookie);
		}
		//  add diagnostics
		if (debug && WEnv.DEBUG)
		{
		//	doc.output(System.out);
			WEnv.addFooter(request, response, servlet, doc.getBody());
		//	doc.output(System.out);
		}
	//	String content = doc.toString();
	//  response.setContentLength(content.length());    //  causes problems at the end of the output

		//  print document
		PrintWriter out = response.getWriter();     //  with character encoding support
		doc.output(out);
		out.flush();
		if (out.checkError())
			s_log.error("createResponse - error writing");
		//  binary output (is faster but does not do character set conversion)
	//	OutputStream out = response.getOutputStream();
	//	byte[] data = doc.toString().getBytes();
	//	response.setContentLength(data.length);
	//	out.write(doc.toString().getBytes());
		//
		out.close();
	}   //  createResponse

	/*************************************************************************/

	/**
	 *  Create Java Script to clear Target frame and display message
	 *
	 *  @param target target
	 *  @return Clear Frame Script
	 */
	public static script getClearFrame (String target)
	{
		StringBuffer cmd = new StringBuffer();
		cmd.append("var d = parent.").append(target).append(".document;\n");
		cmd.append("d.open();\n");
		cmd.append("d.write('<link href=\"").append(WEnv.getStylesheetURL()).append("\" rel=\"stylesheet\">');\n");
		cmd.append("d.close();");
		//
		return new script(cmd.toString());
	}   //  getClearFrame


	/**
	 * 	Return a link and script with new location.
	 * 	Wait 2 seconds
	 * 	@param url forward url
	 * 	@return html
	 */
	public static HtmlCode getForward (String url)
	{
		HtmlCode retValue = new HtmlCode();
		//	Link
		a a = new a(url);
		a.addElement(url);
		retValue.addElement(a);
		//	Java Script	- document.location - 2.5 sec delay
		script script = new script("setTimeout(\"window.top.location.replace('" + url + "')\",2500);");
		retValue.addElement(script);
		//
		return retValue;
	}	//	getForward

	/**
	 * 	Create Forward Page
	 * 	@param response response
	 * 	@param title page title
	 * 	@param forwardURL url
	 * 	@throws ServletException
	 * 	@throws IOException
	 */
	public static void createForwardPage (HttpServletResponse response,
		String title, String forwardURL) throws ServletException, IOException
	{
		response.setContentType("text/html; charset=UTF-8");
		WDoc doc = WDoc.create(title);
		body b = doc.getBody();
		b.addElement(getForward(forwardURL));
		PrintWriter out = response.getWriter();
		doc.output(out);
		out.flush();
		if (out.checkError())
			s_log.error("createForwardPage - error writing");
		out.close();
		s_log.debug("createForwardPage - " + forwardURL + " - " + title);
	}	//	createForwardPage


	/**
	 * 	Does Test exist
	 *	@param test string
	 *	@return true if String with data
	 */
	public static boolean exists (String test)
	{
		if (test == null)
			return false;
		return test.length() > 0;
	}	//	exists

	/**
	 * 	Does Parameter exist
	 * 	@param request request
	 *	@param parameter string
	 *	@return true if String with data
	 */
	public static boolean exists (HttpServletRequest request, String parameter)
	{
		if (request == null || parameter == null)
			return false;
		return exists (request.getParameter(parameter));
	}	//	exists


	/**
	 *	Is EMail address valid
	 * 	@param email mail address
	 * 	@return true if valid
	 */
	public static boolean isEmailValid (String email)
	{
		if (email == null || email.length () == 0)
			return false;
		try
		{
			InternetAddress ia = new InternetAddress (email, true);
			return true;
		}
		catch (AddressException ex)
		{
			s_log.warn ("isEmailValid - " + email + " - "
				+ ex.getLocalizedMessage ());
		}
		return false;
	}	//	isEmailValid


	/*************************************************************************/

	/**
	 *  Decode Properties into String (URL encoded)
	 *
	 *  @param pp properties
	 *  @return Encoded String
	 */
	public static String propertiesEncode (Properties pp)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try
		{
			pp.store(bos, "Compiere");   //  Header
		}
		catch (IOException e)
		{
			s_log.error("propertiesEncode-store", e);
		}
		String result = new String (bos.toByteArray());
	//	System.out.println("String=" + result);
		String enc = "UTF-8";
		try
		{
			result = java.net.URLEncoder.encode(result, enc);
		}
		catch (UnsupportedEncodingException e)
		{
			s_log.error("propertiesEncode-encode-" + enc, e);
			enc = System.getProperty("file.encoding");      //  Windows default is Cp1252
			try
			{
				result = java.net.URLEncoder.encode(result, enc);
			}
			catch (Exception ex)
			{
				s_log.error("propertiesEncode-encode", ex);
			}
		}
	//	System.out.println("String-Encoded=" + result);
		return result;
	}   //  propertiesEncode

	/**
	 *  Decode data String (URL encoded) into Properties
	 *
	 *  @param data data
	 *  @return Properties
	 */
	public static Properties propertiesDecode (String data)
	{
		String result = null;
	//	System.out.println("String=" + data);
		String enc = "UTF-8";
		try
		{
			result = java.net.URLDecoder.decode(data, enc);
		}
		catch (UnsupportedEncodingException e)
		{
			s_log.error("propertiesDecode-decode-" + enc, e);
			enc = System.getProperty("file.encoding");      //  Windows default is Cp1252
			try
			{
				result = java.net.URLEncoder.encode(data, enc);
			}
			catch (Exception ex)
			{
				s_log.error("propertiesDecode-decode", ex);
			}
		}
	//	System.out.println("String-Decoded=" + result);

		ByteArrayInputStream bis = new ByteArrayInputStream(result.getBytes());
		Properties pp = new Properties();
		try
		{
			pp.load(bis);
		}
		catch (IOException e)
		{
			s_log.error("propertiesDecode-load", e);
		}
		return pp;
	}   //  propertiesDecode

	/*************************************************************************/

	/**
	 *  Convert Array of NamePair to HTTP Option Array.
	 *  <p>
	 *  If the ArrayList does not contain NamePairs, the String value is used
	 *  @see org.compiere.util.NamePair
	 *  @param  list    ArrayList containing NamePair values
	 *  @param  default_ID  Sets the default if the key/ID value is found.
	 *      If the value is null or empty, the first value is selected
	 *  @return Option Array
	 */
	public static option[] convertToOption (NamePair[] list, String default_ID)
	{
		int size = list.length;
		option[] retValue = new option[size];
		for (int i = 0; i < size; i++)
		{
			boolean selected = false;
			//  select first entry
			if (i == 0 && (default_ID == null || default_ID.length() == 0))
				selected = true;

			//  Create option
			retValue[i] = new option(list[i].getID()).addElement(list[i].getName());

			//  Select if ID/Key is same as default ID
			if (default_ID != null && default_ID.equals(list[i].getID()))
				selected = true;
			retValue[i].setSelected(selected);
		}
		return retValue;
	}   //  convertToOption

	/**
	 *  Create label/field table row
	 *
	 *  @param line - null for new line (table row)
	 *  @param FORMNAME form name
	 *  @param PARAMETER parameter name
	 *  @param labelText label
	 *  @param inputType HTML input type
	 *  @param value data value
	 *  @param sizeDisplay display size
	 *  @param size data size
	 *  @param longField field spanning two columns
	 *  @param mandatory mark as mandatory
	 *  @param onChange onChange call
	 *  @param script script
	 *  @return tr table row
	 */
	static public tr createField (tr line, String FORMNAME, String PARAMETER,
		String labelText, String inputType, Object value,
		int sizeDisplay, int size, boolean longField, boolean mandatory, String onChange, StringBuffer script)
	{
		if (line == null)
			line = new tr();
		String labelInfo = labelText;
		if (mandatory)
		{
			labelInfo += "&nbsp;<font color=\"red\">*</font>";
			String fName = "document." + FORMNAME + "." + PARAMETER;
			script.append(fName).append(".required=true; ");
		}

		label llabel = new label().setFor(PARAMETER).addElement(labelInfo);
		llabel.setID("ID_" + PARAMETER + "_Label");
	//	label.setTitle(description);
		line.addElement(new td().addElement(llabel).setAlign(AlignType.right));
		input iinput = new input(inputType, PARAMETER, value == null ? "" : value.toString());
		iinput.setSize(sizeDisplay).setMaxlength(size);
		iinput.setID("ID_" + PARAMETER);
		if (onChange != null && onChange.length() > 0)
			iinput.setOnChange(onChange);
		iinput.setTitle(labelText);
		td field = new td().addElement(iinput).setAlign(AlignType.LEFT);
		if (longField)
			field.setColSpan(3);
		line.addElement(field);
		return line;
	}   //  addField

}   //  WUtil
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
import java.util.*;
import java.sql.*;
import java.io.*;
import javax.mail.internet.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.apache.log4j.Logger;

import org.compiere.wstore.*;
import org.compiere.util.*;
import org.compiere.model.*;

/**
 *  Servlet Utilities
 *
 *  @author Jorg Janke
 *  @version  $Id: WUtil.java,v 1.12 2003/07/19 05:21:44 jjanke Exp $
 */
public final class WUtil
{
	/**	Logging								*/
	private static Logger			s_log = Logger.getLogger("org.compiere.www.WUtil");

	/**
	 *  Create Timeout Message
	 *
	 *  @param request request
	 *  @param response response
	 *  @param servlet servlet
	 *  @param ctx context
	 *  @param message - optional message
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public static void createTimeoutPage (HttpServletRequest request, HttpServletResponse response,
		HttpServlet servlet, Properties ctx, String message) throws ServletException, IOException
	{
		s_log.info("createTimeoutPage - " + message);
		String windowTitle = "Timeout";
		if (ctx != null)
			windowTitle = Msg.getMsg(ctx, "Timeout");

		WDoc doc = WDoc.create (windowTitle);

		//	Body
		body body = doc.getBody();
		//  optional message
		if (message != null && message.length() > 0)
			body.addElement(new p(message));

		//  login button
		body.addElement(getLoginButton(ctx));

		//
		body.addElement(new hr());
		body.addElement(new small(servlet.getClass().getName()));
		//	fini
		createResponse (request, response, servlet, null, doc, false);
	}   //  createTimeoutPage

	/**
	 *  Create Error Message
	 *
	 *  @param request request
	 *  @param response response
	 *  @param servlet servlet
	 *  @param ctx context
	 *  @param message message
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public static void createErrorPage (HttpServletRequest request, HttpServletResponse response,
		HttpServlet servlet, Properties ctx, String message) throws ServletException, IOException
	{
		s_log.info("createErrorPage - " + message);
		String windowTitle = "Error";
		if (ctx != null)
			windowTitle = Msg.getMsg(ctx, "Error");		//	JUST TO GET LANGUAGE ??
		if (message != null)
			windowTitle += ": " + message;

		WDoc doc = WDoc.create (windowTitle);

		//	Body
		body b = doc.getBody();

		b.addElement(new p(servlet.getServletName(), AlignType.center));
		b.addElement(new br());

		//	fini
		createResponse (request, response, servlet, null, doc, true);
	}   //  createErrorPage

	/**
	 *  Create Exit Page "Log-off".
	 *  <p>
	 *  - End Session
	 *  - Go to start page (e.g. /compiere/index.html)
	 *
	 *  @param request request
	 *  @param response response
	 *  @param servlet servlet
	 *  @param ctx context
	 *  @param AD_Message messahe
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public static void createLoginPage (HttpServletRequest request, HttpServletResponse response,
		HttpServlet servlet, Properties ctx, String AD_Message) throws ServletException, IOException
	{
		request.getSession().invalidate();
		String url = WEnv.getBaseDirectory("index.html");
		//
		WDoc doc = null;
		if (ctx != null && AD_Message != null && !AD_Message.equals(""))
			doc = WDoc.create (Msg.getMsg(ctx, AD_Message));
		else if (AD_Message != null)
			doc = WDoc.create (AD_Message);
		else
			doc = WDoc.create (false);
		script script = new script("window.top.location.replace('" + url + "');");
		doc.getBody().addElement(script);
		//
		createResponse (request, response, servlet, null, doc, false);
	}   //  createLoginPage

	/**
	 *  Create Login Button - replace Window
	 *
	 *  @param ctx context
	 *  @return Button
	 */
	public static button getLoginButton(Properties ctx)
	{
		String text = Msg.getMsg(ctx, "Login");
		button button = new button();
		button.setType("button").setName("Login").addElement(text);
		StringBuffer cmd = new StringBuffer ("window.top.location.replace('");
		cmd.append(WEnv.getBaseDirectory("index.html"));
		cmd.append("');");
		button.setOnClick(cmd.toString());
		return button;
	}   //  getLoginButton

	/*************************************************************************/

	/**
	 *  Get Cookie Properties
	 *
	 *  @param request request
	 *  @return Properties
	 */
	public static Properties getCookieProprties(HttpServletRequest request)
	{
		//  Get Properties
		Cookie[] cookies = request.getCookies();
		if (cookies != null)
		{
			for (int i = 0; i < cookies.length; i++)
			{
				if (cookies[i].getName().equals(WEnv.COOKIE_INFO))
					return propertiesDecode(cookies[i].getValue());
			}
		}
		return new Properties();
	}   //  getProperties

	/**
	 *  Get numeric Parameter - 0 if not defined
	 *
	 *  @param request request
	 *  @param parameter parameter
	 *  @return int result or 0
	 */
	public static int getParameterAsInt(HttpServletRequest request, String parameter)
	{
		if (request == null || parameter == null)
			return 0;
		String data = request.getParameter(parameter);
		if (data == null || data.length() == 0)
			return 0;
		try
		{
			return Integer.parseInt(data);
		}
		catch (Exception e)
		{
			s_log.debug("getParameterAsInt - " + parameter + "=" + data, e);
		}
		return 0;
	}   //  getParameter

	/*************************************************************************/

	/**
	 *  Create Standard Response Header with optional Cookie and print document.
	 *  D:\j2sdk1.4.0\docs\guide\intl\encoding.doc.html
	 *
	 *  @param request request
	 *  @param response response
	 *  @param servlet servlet
	 *  @param cookieProperties cookie properties
	 *  @param doc doc
	 *  @param debug debug
	 *  @throws IOException
	 */
	public static void createResponse (HttpServletRequest request, HttpServletResponse response,
		HttpServlet servlet, Properties cookieProperties, WDoc doc, boolean debug) throws IOException
	{
		response.setHeader("Cache-Control", "no-cache");
	//	response.setContentType("text/html; charset=ISO-8859-1");   //  default
		response.setContentType("text/html; charset=UTF-8");

		//
		//  Update Cookie - overwrite
		if (cookieProperties != null)
		{
			Cookie cookie = new Cookie (WEnv.COOKIE_INFO, propertiesEncode(cookieProperties));
			cookie.setComment("(c) ComPiere, Inc - Jorg Janke");
			cookie.setSecure(false);
			cookie.setPath("/");
			if (cookieProperties.size() == 0)
				cookie.setMaxAge(0);            //  delete cookie
			else
				cookie.setMaxAge(2592000);      //  30 days in seconds   60*60*24*30
			response.addCookie(cookie);
		}
		//  add diagnostics
		if (debug && WEnv.DEBUG)
		{
		//	doc.output(System.out);
			WEnv.addFooter(request, response, servlet, doc.getBody());
		//	doc.output(System.out);
		}
	//	String content = doc.toString();
	//  response.setContentLength(content.length());    //  causes problems at the end of the output

		//  print document
		PrintWriter out = response.getWriter();     //  with character encoding support
		doc.output(out);
		out.flush();
		if (out.checkError())
			s_log.error("createResponse - error writing");
		//  binary output (is faster but does not do character set conversion)
	//	OutputStream out = response.getOutputStream();
	//	byte[] data = doc.toString().getBytes();
	//	response.setContentLength(data.length);
	//	out.write(doc.toString().getBytes());
		//
		out.close();
	}   //  createResponse

	/*************************************************************************/

	/**
	 *  Create Java Script to clear Target frame and display message
	 *
	 *  @param target target
	 *  @return Clear Frame Script
	 */
	public static script getClearFrame (String target)
	{
		StringBuffer cmd = new StringBuffer();
		cmd.append("var d = parent.").append(target).append(".document;\n");
		cmd.append("d.open();\n");
		cmd.append("d.write('<link href=\"").append(WEnv.getStylesheetURL()).append("\" rel=\"stylesheet\">');\n");
		cmd.append("d.close();");
		//
		return new script(cmd.toString());
	}   //  getClearFrame


	/**
	 * 	Return a link and script with new location.
	 * 	Wait 2 seconds
	 * 	@param url forward url
	 * 	@return html
	 */
	public static HtmlCode getForward (String url)
	{
		HtmlCode retValue = new HtmlCode();
		//	Link
		a a = new a(url);
		a.addElement(url);
		retValue.addElement(a);
		//	Java Script	- document.location - 2.5 sec delay
		script script = new script("setTimeout(\"window.top.location.replace('" + url + "')\",2500);");
		retValue.addElement(script);
		//
		return retValue;
	}	//	getForward

	/**
	 * 	Create Forward Page
	 * 	@param response response
	 * 	@param title page title
	 * 	@param forwardURL url
	 * 	@throws ServletException
	 * 	@throws IOException
	 */
	public static void createForwardPage (HttpServletResponse response,
		String title, String forwardURL) throws ServletException, IOException
	{
		response.setContentType("text/html; charset=UTF-8");
		WDoc doc = WDoc.create(title);
		body b = doc.getBody();
		b.addElement(getForward(forwardURL));
		PrintWriter out = response.getWriter();
		doc.output(out);
		out.flush();
		if (out.checkError())
			s_log.error("createForwardPage - error writing");
		out.close();
		s_log.debug("createForwardPage - " + forwardURL + " - " + title);
	}	//	createForwardPage


	/**
	 * 	Does Test exist
	 *	@param test string
	 *	@return true if String with data
	 */
	public static boolean exists (String test)
	{
		if (test == null)
			return false;
		return test.length() > 0;
	}	//	exists

	/**
	 * 	Does Parameter exist
	 * 	@param request request
	 *	@param parameter string
	 *	@return true if String with data
	 */
	public static boolean exists (HttpServletRequest request, String parameter)
	{
		if (request == null || parameter == null)
			return false;
		return exists (request.getParameter(parameter));
	}	//	exists


	/**
	 *	Is EMail address valid
	 * 	@param email mail address
	 * 	@return true if valid
	 */
	public static boolean isEmailValid (String email)
	{
		if (email == null || email.length () == 0)
			return false;
		try
		{
			InternetAddress ia = new InternetAddress (email, true);
			return true;
		}
		catch (AddressException ex)
		{
			s_log.warn ("isEmailValid - " + email + " - "
				+ ex.getLocalizedMessage ());
		}
		return false;
	}	//	isEmailValid


	/*************************************************************************/

	/**
	 *  Decode Properties into String (URL encoded)
	 *
	 *  @param pp properties
	 *  @return Encoded String
	 */
	public static String propertiesEncode (Properties pp)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try
		{
			pp.store(bos, "Compiere");   //  Header
		}
		catch (IOException e)
		{
			s_log.error("propertiesEncode-store", e);
		}
		String result = new String (bos.toByteArray());
	//	System.out.println("String=" + result);
		String enc = "UTF-8";
		try
		{
			result = java.net.URLEncoder.encode(result, enc);
		}
		catch (UnsupportedEncodingException e)
		{
			s_log.error("propertiesEncode-encode-" + enc, e);
			enc = System.getProperty("file.encoding");      //  Windows default is Cp1252
			try
			{
				result = java.net.URLEncoder.encode(result, enc);
			}
			catch (Exception ex)
			{
				s_log.error("propertiesEncode-encode", ex);
			}
		}
	//	System.out.println("String-Encoded=" + result);
		return result;
	}   //  propertiesEncode

	/**
	 *  Decode data String (URL encoded) into Properties
	 *
	 *  @param data data
	 *  @return Properties
	 */
	public static Properties propertiesDecode (String data)
	{
		String result = null;
	//	System.out.println("String=" + data);
		String enc = "UTF-8";
		try
		{
			result = java.net.URLDecoder.decode(data, enc);
		}
		catch (UnsupportedEncodingException e)
		{
			s_log.error("propertiesDecode-decode-" + enc, e);
			enc = System.getProperty("file.encoding");      //  Windows default is Cp1252
			try
			{
				result = java.net.URLEncoder.encode(data, enc);
			}
			catch (Exception ex)
			{
				s_log.error("propertiesDecode-decode", ex);
			}
		}
	//	System.out.println("String-Decoded=" + result);

		ByteArrayInputStream bis = new ByteArrayInputStream(result.getBytes());
		Properties pp = new Properties();
		try
		{
			pp.load(bis);
		}
		catch (IOException e)
		{
			s_log.error("propertiesDecode-load", e);
		}
		return pp;
	}   //  propertiesDecode

	/*************************************************************************/

	/**
	 *  Convert Array of NamePair to HTTP Option Array.
	 *  <p>
	 *  If the ArrayList does not contain NamePairs, the String value is used
	 *  @see org.compiere.util.NamePair
	 *  @param  list    ArrayList containing NamePair values
	 *  @param  default_ID  Sets the default if the key/ID value is found.
	 *      If the value is null or empty, the first value is selected
	 *  @return Option Array
	 */
	public static option[] convertToOption (NamePair[] list, String default_ID)
	{
		int size = list.length;
		option[] retValue = new option[size];
		for (int i = 0; i < size; i++)
		{
			boolean selected = false;
			//  select first entry
			if (i == 0 && (default_ID == null || default_ID.length() == 0))
				selected = true;

			//  Create option
			retValue[i] = new option(list[i].getID()).addElement(list[i].getName());

			//  Select if ID/Key is same as default ID
			if (default_ID != null && default_ID.equals(list[i].getID()))
				selected = true;
			retValue[i].setSelected(selected);
		}
		return retValue;
	}   //  convertToOption

	/**
	 *  Create label/field table row
	 *
	 *  @param line - null for new line (table row)
	 *  @param FORMNAME form name
	 *  @param PARAMETER parameter name
	 *  @param labelText label
	 *  @param inputType HTML input type
	 *  @param value data value
	 *  @param sizeDisplay display size
	 *  @param size data size
	 *  @param longField field spanning two columns
	 *  @param mandatory mark as mandatory
	 *  @param onChange onChange call
	 *  @param script script
	 *  @return tr table row
	 */
	static public tr createField (tr line, String FORMNAME, String PARAMETER,
		String labelText, String inputType, Object value,
		int sizeDisplay, int size, boolean longField, boolean mandatory, String onChange, StringBuffer script)
	{
		if (line == null)
			line = new tr();
		String labelInfo = labelText;
		if (mandatory)
		{
			labelInfo += "&nbsp;<font color=\"red\">*</font>";
			String fName = "document." + FORMNAME + "." + PARAMETER;
			script.append(fName).append(".required=true; ");
		}

		label llabel = new label().setFor(PARAMETER).addElement(labelInfo);
		llabel.setID("ID_" + PARAMETER + "_Label");
	//	label.setTitle(description);
		line.addElement(new td().addElement(llabel).setAlign(AlignType.right));
		input iinput = new input(inputType, PARAMETER, value == null ? "" : value.toString());
		iinput.setSize(sizeDisplay).setMaxlength(size);
		iinput.setID("ID_" + PARAMETER);
		if (onChange != null && onChange.length() > 0)
			iinput.setOnChange(onChange);
		iinput.setTitle(labelText);
		td field = new td().addElement(iinput).setAlign(AlignType.LEFT);
		if (longField)
			field.setColSpan(3);
		line.addElement(field);
		return line;
	}   //  addField

}   //  WUtil
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
import java.util.*;
import java.sql.*;
import java.io.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.apache.log4j.Logger;

import org.compiere.util.*;
import org.compiere.model.*;

/**
 *  Servlet Utilities
 *
 *  @author Jorg Janke
 *  @version  $Id: WUtil.java,v 1.3 2002/10/30 05:05:17 jjanke Exp $
 */
public final class WUtil
{
	/**	Logging								*/
	private static Logger			s_log = Logger.getLogger("org.compiere.www.WUtil");

	/**
	 *  Create Timeout Message
	 *
	 *  @param request request
	 *  @param response response
	 *  @param servlet servlet
	 *  @param ctx context
	 *  @param message - optional message
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public static void createTimeoutPage (HttpServletRequest request, HttpServletResponse response,
		HttpServlet servlet, Properties ctx, String message) throws ServletException, IOException
	{
		s_log.info("createTimeoutPage - " + message);
		String windowTitle = "Timeout";
		if (ctx != null)
			windowTitle = Msg.getMsg(ctx, "Timeout");

		WDoc doc = WDoc.create (windowTitle);

		//	Body
		body body = doc.getBody();
		//  optional message
		if (message != null && message.length() > 0)
			body.addElement(new p(message));

		//  login button
		body.addElement(getLoginButton(ctx));

		//
		body.addElement(new hr());
		body.addElement(new small(servlet.getClass().getName()));
		//	fini
		createResponse (request, response, servlet, null, doc, false);
	}   //  createTimeoutPage

	/**
	 *  Create Error Message
	 *
	 *  @param request request
	 *  @param response response
	 *  @param servlet servlet
	 *  @param ctx context
	 *  @param message message
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public static void createErrorPage (HttpServletRequest request, HttpServletResponse response,
		HttpServlet servlet, Properties ctx, String message) throws ServletException, IOException
	{
		s_log.info("createErrorPage - " + message);
		String windowTitle = "Error";
		if (ctx != null)
			windowTitle = Msg.getMsg(ctx, "Error");		//	JUST TO GET LANGUAGE ??
		if (message != null)
			windowTitle += ": " + message;

		WDoc doc = WDoc.create (windowTitle);

		//	Body
		body b = doc.getBody();

		b.addElement(new p(servlet.getServletName(), AlignType.center));
		b.addElement(new br());

		//	fini
		createResponse (request, response, servlet, null, doc, true);
	}   //  createErrorPage

	/**
	 *  Create Exit Page "Log-off".
	 *  <p>
	 *  - End Session
	 *  - Go to start page (e.g. /compiere/index.html)
	 *
	 *  @param request request
	 *  @param response response
	 *  @param servlet servlet
	 *  @param ctx context
	 *  @param AD_Message messahe
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public static void createLoginPage (HttpServletRequest request, HttpServletResponse response,
		HttpServlet servlet, Properties ctx, String AD_Message) throws ServletException, IOException
	{
		request.getSession().invalidate();
		String url = WEnv.getBaseDirectory("index.html");
		//
		WDoc doc = null;
		if (ctx != null && AD_Message != null && !AD_Message.equals(""))
			doc = WDoc.create (Msg.getMsg(ctx, AD_Message));
		else if (AD_Message != null)
			doc = WDoc.create (AD_Message);
		else
			doc = WDoc.create (false);
		script script = new script("window.top.location.replace('" + url + "');");
		doc.getBody().addElement(script);
		//
		createResponse (request, response, servlet, null, doc, false);
	}   //  exit

	/**
	 *  Create Login Button - replace Window
	 *
	 *  @param ctx context
	 *  @return Button
	 */
	public static button getLoginButton(Properties ctx)
	{
		String text = Msg.getMsg(ctx, "Login");
		button button = new button();
		button.setType("button").setName("Login").addElement(text);
		StringBuffer cmd = new StringBuffer ("window.top.location.replace('");
		cmd.append(WEnv.getBaseDirectory("index.html"));
		cmd.append("');");
		button.setOnClick(cmd.toString());
		return button;
	}   //  getLoginButton

	/*************************************************************************/

	/**
	 *  Get Cookie Properties
	 *
	 *  @param request request
	 *  @return Properties
	 */
	public static Properties getCookieProprties(HttpServletRequest request)
	{
		//  Get Properties
		Cookie[] cookies = request.getCookies();
		if (cookies != null)
		{
			for (int i = 0; i < cookies.length; i++)
			{
				if (cookies[i].getName().equals(WEnv.COOKIE_INFO))
					return propertiesDecode(cookies[i].getValue());
			}
		}
		return new Properties();
	}   //  getProperties

	/**
	 *  Get numeric Parameter - 0 if not defined
	 *
	 *  @param request request
	 *  @param parameter parameter
	 *  @return int result
	 */
	public static int getParameterAsInt(HttpServletRequest request, String parameter)
	{
		if (request == null || parameter == null)
			return 0;
		String data = request.getParameter(parameter);
		if (data == null || data.length() == 0)
			return 0;
		try
		{
			return Integer.parseInt(data);
		}
		catch (Exception e)
		{
			s_log.debug("getParameterAsInt - " + parameter + "=" + data, e);
		}
		return 0;
	}   //  getParameter

	/*************************************************************************/

	/**
	 *  Create Standard Response Header with optional Cookie and print document.
	 *  D:\j2sdk1.4.0\docs\guide\intl\encoding.doc.html
	 *
	 *  @param request request
	 *  @param response response
	 *  @param servlet servlet
	 *  @param cookieProperties cookie properties
	 *  @param doc doc
	 *  @param debug debug
	 *  @throws IOException
	 */
	public static void createResponse (HttpServletRequest request, HttpServletResponse response,
		HttpServlet servlet, Properties cookieProperties, WDoc doc, boolean debug) throws IOException
	{
		response.setHeader("Cache-Control", "no-cache");
	//	response.setContentType("text/html; charset=ISO-8859-1");   //  default
		response.setContentType("text/html; charset=UTF-8");

		//
		//  Update Cookie - overwrite
		if (cookieProperties != null)
		{
			Cookie cookie = new Cookie (WEnv.COOKIE_INFO, propertiesEncode(cookieProperties));
			cookie.setComment("(c) ComPiere, Inc - Jorg Janke");
			cookie.setSecure(false);
			cookie.setPath("/");
			if (cookieProperties.size() == 0)
				cookie.setMaxAge(0);            //  delete cookie
			else
				cookie.setMaxAge(2592000);      //  30 days in seconds   60*60*24*30
			response.addCookie(cookie);
		}
		//  add diagnostics
		if (debug && WEnv.DEBUG)
		{
		//	doc.output(System.out);
			WEnv.addFooter(request, response, servlet, doc.getBody());
		//	doc.output(System.out);
		}
	//	String content = doc.toString();
	//  response.setContentLength(content.length());    //  causes problems at the end of the output

		//  print document
		PrintWriter out = response.getWriter();     //  with character encoding support
		doc.output(out);
		out.flush();
		if (out.checkError())
			s_log.error("createResponse - error writing");
		//  binary output (is faster but does not do character set conversion)
	//	OutputStream out = response.getOutputStream();
	//	byte[] data = doc.toString().getBytes();
	//	response.setContentLength(data.length);
	//	out.write(doc.toString().getBytes());
		//
		out.close();
	}   //  createResponse

	/*************************************************************************/

	/**
	 *  Create Java Script to clear Target frame and display message
	 *
	 *  @param target target
	 *  @return Clear Frame Script
	 */
	public static script getClearFrame (String target)
	{
		StringBuffer cmd = new StringBuffer();
		cmd.append("var d = parent.").append(target).append(".document;\n");
		cmd.append("d.open();\n");
		cmd.append("d.write('<link href=\"").append(WEnv.getStylesheetURL()).append("\" rel=\"stylesheet\">');\n");
		cmd.append("d.close();");
		//
		return new script(cmd.toString());
	}   //  getClearFrame

	/*************************************************************************/

	/**
	 *  Decode Properties into String (URL encoded)
	 *
	 *  @param pp properties
	 *  @return Encoded String
	 */
	public static String propertiesEncode (Properties pp)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try
		{
			pp.store(bos, "Compiere");   //  Header
		}
		catch (IOException e)
		{
			s_log.error("propertiesEncode-store", e);
		}
		String result = new String (bos.toByteArray());
	//	System.out.println("String=" + result);
		String enc = "UTF-8";
		try
		{
			result = java.net.URLEncoder.encode(result, enc);
		}
		catch (UnsupportedEncodingException e)
		{
			s_log.error("propertiesEncode-encode-" + enc, e);
			enc = System.getProperty("file.encoding");      //  Windows default is Cp1252
			try
			{
				result = java.net.URLEncoder.encode(result, enc);
			}
			catch (Exception ex)
			{
				s_log.error("propertiesEncode-encode", ex);
			}
		}
	//	System.out.println("String-Encoded=" + result);
		return result;
	}   //  propertiesEncode

	/**
	 *  Decode data String (URL encoded) into Properties
	 *
	 *  @param data data
	 *  @return Properties
	 */
	public static Properties propertiesDecode (String data)
	{
		String result = null;
	//	System.out.println("String=" + data);
		String enc = "UTF-8";
		try
		{
			result = java.net.URLDecoder.decode(data, enc);
		}
		catch (UnsupportedEncodingException e)
		{
			s_log.error("propertiesDecode-decode-" + enc, e);
			enc = System.getProperty("file.encoding");      //  Windows default is Cp1252
			try
			{
				result = java.net.URLEncoder.encode(data, enc);
			}
			catch (Exception ex)
			{
				s_log.error("propertiesDecode-decode", ex);
			}
		}
	//	System.out.println("String-Decoded=" + result);

		ByteArrayInputStream bis = new ByteArrayInputStream(result.getBytes());
		Properties pp = new Properties();
		try
		{
			pp.load(bis);
		}
		catch (IOException e)
		{
			s_log.error("propertiesDecode-load", e);
		}
		return pp;
	}   //  propertiesDecode

	/*************************************************************************/

	/**
	 *  Convert Array of NamePair to HTTP Option Array.
	 *  <p>
	 *  If the ArrayList does not contain NamePairs, the String value is used
	 *  @see org.compiere.util.NamePair
	 *  @param  list    ArrayList containing NamePair values
	 *  @param  default_ID  Sets the default if the key/ID value is found.
	 *      If the value is null or empty, the first value is selected
	 *  @return Option Array
	 */
	public static option[] convertToOption (NamePair[] list, String default_ID)
	{
		int size = list.length;
		option[] retValue = new option[size];
		for (int i = 0; i < size; i++)
		{
			boolean selected = false;
			//  select first entry
			if (i == 0 && (default_ID == null || default_ID.length() == 0))
				selected = true;

			//  Create option
			retValue[i] = new option(list[i].getID()).addElement(list[i].getName());

			//  Select if ID/Key is same as default ID
			if (default_ID != null && default_ID.equals(list[i].getID()))
				selected = true;
			retValue[i].setSelected(selected);
		}
		return retValue;
	}   //  convertToOption

	/**
	 *  Create label/field table row
	 *
	 *  @param line - null for new line (table row)
	 *  @param FORMNAME form name
	 *  @param PARAMETER parameter name
	 *  @param labelText label
	 *  @param inputType HTML input type
	 *  @param value data value
	 *  @param sizeDisplay display size
	 *  @param size data size
	 *  @param longField field spanning two columns
	 *  @param mandatory mark as mandatory
	 *  @param onChange onChange call
	 *  @param script script
	 *  @return tr table row
	 */
	static public tr createField (tr line, String FORMNAME, String PARAMETER,
		String labelText, String inputType, Object value,
		int sizeDisplay, int size, boolean longField, boolean mandatory, String onChange, StringBuffer script)
	{
		if (line == null)
			line = new tr();
		String labelInfo = labelText;
		if (mandatory)
		{
			labelInfo += "&nbsp;<font color=\"red\">*</font>";
			String fName = "document." + FORMNAME + "." + PARAMETER;
			script.append(fName).append(".required=true; ");
		}

		label llabel = new label().setFor(PARAMETER).addElement(labelInfo);
		llabel.setID("ID_" + PARAMETER + "_Label");
	//	label.setTitle(description);
		line.addElement(new td().addElement(llabel).setAlign(AlignType.right));
		input iinput = new input(inputType, PARAMETER, value == null ? "" : value.toString());
		iinput.setSize(sizeDisplay).setMaxlength(size);
		iinput.setID("ID_" + PARAMETER);
		if (onChange != null && onChange.length() > 0)
			iinput.setOnChange(onChange);
		iinput.setTitle(labelText);
		td field = new td().addElement(iinput).setAlign(AlignType.LEFT);
		if (longField)
			field.setColSpan(3);
		line.addElement(field);
		return line;
	}   //  addField


}   //  WUtil
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
import java.util.*;
import java.sql.*;
import java.text.*;
import java.io.*;
import java.math.*;

import javax.mail.internet.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.apache.log4j.Logger;

import org.compiere.wstore.*;
import org.compiere.util.*;

/**
 *  Servlet Utilities
 *
 *  @author Jorg Janke
 *  @version  $Id: WUtil.java,v 1.15 2004/04/27 05:23:33 jjanke Exp $
 */
public final class WUtil
{
	/**	Logging								*/
	private static Logger			s_log = Logger.getLogger("org.compiere.www.WUtil");

	
	/**
	 *  Create Timeout Message
	 *
	 *  @param request request
	 *  @param response response
	 *  @param servlet servlet
	 *  @param ctx context
	 *  @param message - optional message
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public static void createTimeoutPage (HttpServletRequest request, HttpServletResponse response,
		HttpServlet servlet, Properties ctx, String message) throws ServletException, IOException
	{
		s_log.info("createTimeoutPage - " + message);
		String windowTitle = "Timeout";
		if (ctx != null)
			windowTitle = Msg.getMsg(ctx, "Timeout");

		WDoc doc = WDoc.create (windowTitle);

		//	Body
		body body = doc.getBody();
		//  optional message
		if (message != null && message.length() > 0)
			body.addElement(new p(message));

		//  login button
		body.addElement(getLoginButton(ctx));

		//
		body.addElement(new hr());
		body.addElement(new small(servlet.getClass().getName()));
		//	fini
		createResponse (request, response, servlet, null, doc, false);
	}   //  createTimeoutPage

	/**
	 *  Create Error Message
	 *
	 *  @param request request
	 *  @param response response
	 *  @param servlet servlet
	 *  @param ctx context
	 *  @param message message
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public static void createErrorPage (HttpServletRequest request, HttpServletResponse response,
		HttpServlet servlet, Properties ctx, String message) throws ServletException, IOException
	{
		s_log.info("createErrorPage - " + message);
		String windowTitle = "Error";
		if (ctx != null)
			windowTitle = Msg.getMsg(ctx, "Error");		//	JUST TO GET LANGUAGE ??
		if (message != null)
			windowTitle += ": " + message;

		WDoc doc = WDoc.create (windowTitle);

		//	Body
		body b = doc.getBody();

		b.addElement(new p(servlet.getServletName(), AlignType.center));
		b.addElement(new br());

		//	fini
		createResponse (request, response, servlet, null, doc, true);
	}   //  createErrorPage

	/**
	 *  Create Exit Page "Log-off".
	 *  <p>
	 *  - End Session
	 *  - Go to start page (e.g. /compiere/index.html)
	 *
	 *  @param request request
	 *  @param response response
	 *  @param servlet servlet
	 *  @param ctx context
	 *  @param AD_Message messahe
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public static void createLoginPage (HttpServletRequest request, HttpServletResponse response,
		HttpServlet servlet, Properties ctx, String AD_Message) throws ServletException, IOException
	{
		request.getSession().invalidate();
		String url = WEnv.getBaseDirectory("index.html");
		//
		WDoc doc = null;
		if (ctx != null && AD_Message != null && !AD_Message.equals(""))
			doc = WDoc.create (Msg.getMsg(ctx, AD_Message));
		else if (AD_Message != null)
			doc = WDoc.create (AD_Message);
		else
			doc = WDoc.create (false);
		script script = new script("window.top.location.replace('" + url + "');");
		doc.getBody().addElement(script);
		//
		createResponse (request, response, servlet, null, doc, false);
	}   //  createLoginPage

	/**
	 *  Create Login Button - replace Window
	 *
	 *  @param ctx context
	 *  @return Button
	 */
	public static button getLoginButton(Properties ctx)
	{
		String text = Msg.getMsg(ctx, "Login");
		button button = new button();
		button.setType("button").setName("Login").addElement(text);
		StringBuffer cmd = new StringBuffer ("window.top.location.replace('");
		cmd.append(WEnv.getBaseDirectory("index.html"));
		cmd.append("');");
		button.setOnClick(cmd.toString());
		return button;
	}   //  getLoginButton

	
	/**************************************************************************
	 *  Get Cookie Properties
	 *
	 *  @param request request
	 *  @return Properties
	 */
	public static Properties getCookieProprties(HttpServletRequest request)
	{
		//  Get Properties
		Cookie[] cookies = request.getCookies();
		if (cookies != null)
		{
			for (int i = 0; i < cookies.length; i++)
			{
				if (cookies[i].getName().equals(WEnv.COOKIE_INFO))
					return propertiesDecode(cookies[i].getValue());
			}
		}
		return new Properties();
	}   //  getProperties

	
	/**
	 *  Get integer Parameter - 0 if not defined.
	 *
	 *  @param request request
	 *  @param parameter parameter
	 *  @return int result or 0
	 */
	public static int getParameterAsInt (HttpServletRequest request, String parameter)
	{
		if (request == null || parameter == null)
			return 0;
		String data = request.getParameter(parameter);
		if (data == null || data.length() == 0)
			return 0;
		try
		{
			return Integer.parseInt(data);
		}
		catch (Exception e)
		{
			s_log.debug("getParameterAsInt - " + parameter + "=" + data, e);
		}
		return 0;
	}   //  getParameterAsInt

	/**
	 *  Get numeric Parameter - 0 if not defined
	 *
	 *  @param request request
	 *  @param parameter parameter
	 *  @return big decimal result or 0
	 */
	public static BigDecimal getParameterAsBD (HttpServletRequest request, String parameter)
	{
		if (request == null || parameter == null)
			return Env.ZERO;
		String data = request.getParameter(parameter);
		if (data == null || data.length() == 0)
			return Env.ZERO;
		try
		{
			return new BigDecimal (data);
		}
		catch (Exception e)
		{
		}
		try
		{
			DecimalFormat format = DisplayType.getNumberFormat(DisplayType.Number);
			Object oo = format.parseObject(data);
			if (oo instanceof BigDecimal)
				return (BigDecimal)oo;
			else if (oo instanceof Number)
				return new BigDecimal (((Number)oo).doubleValue());
			return new BigDecimal (oo.toString());
		}
		catch (Exception e)
		{
			s_log.debug("getParameterAsBD - " + parameter + "=" + data, e);
		}
		return Env.ZERO;
	}   //  getParameterAsBD

	/**
	 *  Get date Parameter - null if not defined.
	 *	Date portion only
	 *  @param request request
	 *  @param parameter parameter
	 *  @return timestamp result or null
	 */
	public static Timestamp getParameterAsDate(HttpServletRequest request, String parameter)
	{
		if (request == null || parameter == null)
			return null;
		String data = request.getParameter(parameter);
		if (data == null || data.length() == 0)
			return null;
		try
		{
			SimpleDateFormat format = DisplayType.getDateFormat(DisplayType.Date);
			java.util.Date date = format.parse(data);
			if (date != null)
				return new Timestamp (date.getTime());
		}
		catch (Exception e)
		{
			s_log.warn("getParameterAsDate - " + parameter + "=" + data, e);
		}
		return null;
	}   //  getParameterAsDate

	/**
	 *  Get boolean Parameter - false if not defined.
	 *  @param request request
	 *  @param parameter parameter
	 *  @return true if found
	 */
	public static boolean getParameterAsBoolean(HttpServletRequest request, String parameter)
	{
		if (request == null || parameter == null)
			return false;
		String data = request.getParameter(parameter);
		if (data == null || data.length() == 0)
			return false;
		return true;
	}   //  getParameterAsBoolean
	
	/**************************************************************************
	 *  Create Standard Response Header with optional Cookie and print document.
	 *  D:\j2sdk1.4.0\docs\guide\intl\encoding.doc.html
	 *
	 *  @param request request
	 *  @param response response
	 *  @param servlet servlet
	 *  @param cookieProperties cookie properties
	 *  @param doc doc
	 *  @param debug debug
	 *  @throws IOException
	 */
	public static void createResponse (HttpServletRequest request, HttpServletResponse response,
		HttpServlet servlet, Properties cookieProperties, WDoc doc, boolean debug) throws IOException
	{
		response.setHeader("Cache-Control", "no-cache");
	//	response.setContentType("text/html; charset=ISO-8859-1");   //  default
		response.setContentType("text/html; charset=UTF-8");

		//
		//  Update Cookie - overwrite
		if (cookieProperties != null)
		{
			Cookie cookie = new Cookie (WEnv.COOKIE_INFO, propertiesEncode(cookieProperties));
			cookie.setComment("(c) ComPiere, Inc - Jorg Janke");
			cookie.setSecure(false);
			cookie.setPath("/");
			if (cookieProperties.size() == 0)
				cookie.setMaxAge(0);            //  delete cookie
			else
				cookie.setMaxAge(2592000);      //  30 days in seconds   60*60*24*30
			response.addCookie(cookie);
		}
		//  add diagnostics
		if (debug && WEnv.DEBUG)
		{
		//	doc.output(System.out);
			WEnv.addFooter(request, response, servlet, doc.getBody());
		//	doc.output(System.out);
		}
	//	String content = doc.toString();
	//  response.setContentLength(content.length());    //  causes problems at the end of the output

		//  print document
		PrintWriter out = response.getWriter();     //  with character encoding support
		doc.output(out);
		out.flush();
		if (out.checkError())
			s_log.error("createResponse - error writing");
		//  binary output (is faster but does not do character set conversion)
	//	OutputStream out = response.getOutputStream();
	//	byte[] data = doc.toString().getBytes();
	//	response.setContentLength(data.length);
	//	out.write(doc.toString().getBytes());
		//
		out.close();
	}   //  createResponse

	
	/**************************************************************************
	 *  Create Java Script to clear Target frame and display message
	 *
	 *  @param target target
	 *  @return Clear Frame Script
	 */
	public static script getClearFrame (String target)
	{
		StringBuffer cmd = new StringBuffer();
		cmd.append("var d = parent.").append(target).append(".document;\n");
		cmd.append("d.open();\n");
		cmd.append("d.write('<link href=\"").append(WEnv.getStylesheetURL()).append("\" rel=\"stylesheet\">');\n");
		cmd.append("d.close();");
		//
		return new script(cmd.toString());
	}   //  getClearFrame


	/**
	 * 	Return a link and script with new location.
	 * 	@param url forward url
	 * 	@param delaySec delay in seconds (default 3)
	 * 	@return html
	 */
	public static HtmlCode getForward (String url, int delaySec)
	{
		if (delaySec <= 0)
			delaySec = 3;
		HtmlCode retValue = new HtmlCode();
		//	Link
		a a = new a(url);
		a.addElement(url);
		retValue.addElement(a);
		//	Java Script	- document.location - 
		script script = new script("setTimeout(\"window.top.location.replace('" + url 
			+ "')\"," + (delaySec+1000) + ");");
		retValue.addElement(script);
		//
		return retValue;
	}	//	getForward

	/**
	 * 	Create Forward Page
	 * 	@param response response
	 * 	@param title page title
	 * 	@param forwardURL url
	 * 	@param delaySec delay in seconds (default 3)
	 * 	@throws ServletException
	 * 	@throws IOException
	 */
	public static void createForwardPage (HttpServletResponse response,
		String title, String forwardURL, int delaySec) throws ServletException, IOException
	{
		response.setContentType("text/html; charset=UTF-8");
		WDoc doc = WDoc.create(title);
		body b = doc.getBody();
		b.addElement(getForward(forwardURL, delaySec));
		PrintWriter out = response.getWriter();
		doc.output(out);
		out.flush();
		if (out.checkError())
			s_log.error("createForwardPage - error writing");
		out.close();
		s_log.debug("createForwardPage - " + forwardURL + " - " + title);
	}	//	createForwardPage


	/**
	 * 	Does Test exist
	 *	@param test string
	 *	@return true if String with data
	 */
	public static boolean exists (String test)
	{
		if (test == null)
			return false;
		return test.length() > 0;
	}	//	exists

	/**
	 * 	Does Parameter exist
	 * 	@param request request
	 *	@param parameter string
	 *	@return true if String with data
	 */
	public static boolean exists (HttpServletRequest request, String parameter)
	{
		if (request == null || parameter == null)
			return false;
		return exists (request.getParameter(parameter));
	}	//	exists


	/**
	 *	Is EMail address valid
	 * 	@param email mail address
	 * 	@return true if valid
	 */
	public static boolean isEmailValid (String email)
	{
		if (email == null || email.length () == 0)
			return false;
		try
		{
			InternetAddress ia = new InternetAddress (email, true);
			return true;
		}
		catch (AddressException ex)
		{
			s_log.warn ("isEmailValid - " + email + " - "
				+ ex.getLocalizedMessage ());
		}
		return false;
	}	//	isEmailValid


	/**************************************************************************
	 *  Decode Properties into String (URL encoded)
	 *
	 *  @param pp properties
	 *  @return Encoded String
	 */
	public static String propertiesEncode (Properties pp)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try
		{
			pp.store(bos, "Compiere");   //  Header
		}
		catch (IOException e)
		{
			s_log.error("propertiesEncode-store", e);
		}
		String result = new String (bos.toByteArray());
	//	System.out.println("String=" + result);
		String enc = "UTF-8";
		try
		{
			result = java.net.URLEncoder.encode(result, enc);
		}
		catch (UnsupportedEncodingException e)
		{
			s_log.error("propertiesEncode-encode-" + enc, e);
			enc = System.getProperty("file.encoding");      //  Windows default is Cp1252
			try
			{
				result = java.net.URLEncoder.encode(result, enc);
			}
			catch (Exception ex)
			{
				s_log.error("propertiesEncode-encode", ex);
			}
		}
	//	System.out.println("String-Encoded=" + result);
		return result;
	}   //  propertiesEncode

	/**
	 *  Decode data String (URL encoded) into Properties
	 *
	 *  @param data data
	 *  @return Properties
	 */
	public static Properties propertiesDecode (String data)
	{
		String result = null;
	//	System.out.println("String=" + data);
		String enc = "UTF-8";
		try
		{
			result = java.net.URLDecoder.decode(data, enc);
		}
		catch (UnsupportedEncodingException e)
		{
			s_log.error("propertiesDecode-decode-" + enc, e);
			enc = System.getProperty("file.encoding");      //  Windows default is Cp1252
			try
			{
				result = java.net.URLEncoder.encode(data, enc);
			}
			catch (Exception ex)
			{
				s_log.error("propertiesDecode-decode", ex);
			}
		}
	//	System.out.println("String-Decoded=" + result);

		ByteArrayInputStream bis = new ByteArrayInputStream(result.getBytes());
		Properties pp = new Properties();
		try
		{
			pp.load(bis);
		}
		catch (IOException e)
		{
			s_log.error("propertiesDecode-load", e);
		}
		return pp;
	}   //  propertiesDecode

	
	/**************************************************************************
	 *  Convert Array of NamePair to HTTP Option Array.
	 *  <p>
	 *  If the ArrayList does not contain NamePairs, the String value is used
	 *  @see org.compiere.util.NamePair
	 *  @param  list    ArrayList containing NamePair values
	 *  @param  default_ID  Sets the default if the key/ID value is found.
	 *      If the value is null or empty, the first value is selected
	 *  @return Option Array
	 */
	public static option[] convertToOption (NamePair[] list, String default_ID)
	{
		int size = list.length;
		option[] retValue = new option[size];
		for (int i = 0; i < size; i++)
		{
			boolean selected = false;
			//  select first entry
			if (i == 0 && (default_ID == null || default_ID.length() == 0))
				selected = true;

			//  Create option
			retValue[i] = new option(list[i].getID()).addElement(list[i].getName());

			//  Select if ID/Key is same as default ID
			if (default_ID != null && default_ID.equals(list[i].getID()))
				selected = true;
			retValue[i].setSelected(selected);
		}
		return retValue;
	}   //  convertToOption

	/**
	 *  Create label/field table row
	 *
	 *  @param line - null for new line (table row)
	 *  @param FORMNAME form name
	 *  @param PARAMETER parameter name
	 *  @param labelText label
	 *  @param inputType HTML input type
	 *  @param value data value
	 *  @param sizeDisplay display size
	 *  @param size data size
	 *  @param longField field spanning two columns
	 *  @param mandatory mark as mandatory
	 *  @param onChange onChange call
	 *  @param script script
	 *  @return tr table row
	 */
	static public tr createField (tr line, String FORMNAME, String PARAMETER,
		String labelText, String inputType, Object value,
		int sizeDisplay, int size, boolean longField, boolean mandatory, String onChange, StringBuffer script)
	{
		if (line == null)
			line = new tr();
		String labelInfo = labelText;
		if (mandatory)
		{
			labelInfo += "&nbsp;<font color=\"red\">*</font>";
			String fName = "document." + FORMNAME + "." + PARAMETER;
			script.append(fName).append(".required=true; ");
		}

		label llabel = new label().setFor(PARAMETER).addElement(labelInfo);
		llabel.setID("ID_" + PARAMETER + "_Label");
	//	label.setTitle(description);
		line.addElement(new td().addElement(llabel).setAlign(AlignType.right));
		input iinput = new input(inputType, PARAMETER, value == null ? "" : value.toString());
		iinput.setSize(sizeDisplay).setMaxlength(size);
		iinput.setID("ID_" + PARAMETER);
		if (onChange != null && onChange.length() > 0)
			iinput.setOnChange(onChange);
		iinput.setTitle(labelText);
		td field = new td().addElement(iinput).setAlign(AlignType.LEFT);
		if (longField)
			field.setColSpan(3);
		line.addElement(field);
		return line;
	}   //  addField

}   //  WUtil
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
import java.util.*;
import java.sql.*;
import java.io.*;
import javax.mail.internet.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.apache.log4j.Logger;

import org.compiere.wstore.*;
import org.compiere.util.*;
import org.compiere.model.*;

/**
 *  Servlet Utilities
 *
 *  @author Jorg Janke
 *  @version  $Id: WUtil.java,v 1.12 2003/07/19 05:21:44 jjanke Exp $
 */
public final class WUtil
{
	/**	Logging								*/
	private static Logger			s_log = Logger.getLogger("org.compiere.www.WUtil");

	/**
	 *  Create Timeout Message
	 *
	 *  @param request request
	 *  @param response response
	 *  @param servlet servlet
	 *  @param ctx context
	 *  @param message - optional message
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public static void createTimeoutPage (HttpServletRequest request, HttpServletResponse response,
		HttpServlet servlet, Properties ctx, String message) throws ServletException, IOException
	{
		s_log.info("createTimeoutPage - " + message);
		String windowTitle = "Timeout";
		if (ctx != null)
			windowTitle = Msg.getMsg(ctx, "Timeout");

		WDoc doc = WDoc.create (windowTitle);

		//	Body
		body body = doc.getBody();
		//  optional message
		if (message != null && message.length() > 0)
			body.addElement(new p(message));

		//  login button
		body.addElement(getLoginButton(ctx));

		//
		body.addElement(new hr());
		body.addElement(new small(servlet.getClass().getName()));
		//	fini
		createResponse (request, response, servlet, null, doc, false);
	}   //  createTimeoutPage

	/**
	 *  Create Error Message
	 *
	 *  @param request request
	 *  @param response response
	 *  @param servlet servlet
	 *  @param ctx context
	 *  @param message message
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public static void createErrorPage (HttpServletRequest request, HttpServletResponse response,
		HttpServlet servlet, Properties ctx, String message) throws ServletException, IOException
	{
		s_log.info("createErrorPage - " + message);
		String windowTitle = "Error";
		if (ctx != null)
			windowTitle = Msg.getMsg(ctx, "Error");		//	JUST TO GET LANGUAGE ??
		if (message != null)
			windowTitle += ": " + message;

		WDoc doc = WDoc.create (windowTitle);

		//	Body
		body b = doc.getBody();

		b.addElement(new p(servlet.getServletName(), AlignType.center));
		b.addElement(new br());

		//	fini
		createResponse (request, response, servlet, null, doc, true);
	}   //  createErrorPage

	/**
	 *  Create Exit Page "Log-off".
	 *  <p>
	 *  - End Session
	 *  - Go to start page (e.g. /compiere/index.html)
	 *
	 *  @param request request
	 *  @param response response
	 *  @param servlet servlet
	 *  @param ctx context
	 *  @param AD_Message messahe
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public static void createLoginPage (HttpServletRequest request, HttpServletResponse response,
		HttpServlet servlet, Properties ctx, String AD_Message) throws ServletException, IOException
	{
		request.getSession().invalidate();
		String url = WEnv.getBaseDirectory("index.html");
		//
		WDoc doc = null;
		if (ctx != null && AD_Message != null && !AD_Message.equals(""))
			doc = WDoc.create (Msg.getMsg(ctx, AD_Message));
		else if (AD_Message != null)
			doc = WDoc.create (AD_Message);
		else
			doc = WDoc.create (false);
		script script = new script("window.top.location.replace('" + url + "');");
		doc.getBody().addElement(script);
		//
		createResponse (request, response, servlet, null, doc, false);
	}   //  createLoginPage

	/**
	 *  Create Login Button - replace Window
	 *
	 *  @param ctx context
	 *  @return Button
	 */
	public static button getLoginButton(Properties ctx)
	{
		String text = Msg.getMsg(ctx, "Login");
		button button = new button();
		button.setType("button").setName("Login").addElement(text);
		StringBuffer cmd = new StringBuffer ("window.top.location.replace('");
		cmd.append(WEnv.getBaseDirectory("index.html"));
		cmd.append("');");
		button.setOnClick(cmd.toString());
		return button;
	}   //  getLoginButton

	/*************************************************************************/

	/**
	 *  Get Cookie Properties
	 *
	 *  @param request request
	 *  @return Properties
	 */
	public static Properties getCookieProprties(HttpServletRequest request)
	{
		//  Get Properties
		Cookie[] cookies = request.getCookies();
		if (cookies != null)
		{
			for (int i = 0; i < cookies.length; i++)
			{
				if (cookies[i].getName().equals(WEnv.COOKIE_INFO))
					return propertiesDecode(cookies[i].getValue());
			}
		}
		return new Properties();
	}   //  getProperties

	/**
	 *  Get numeric Parameter - 0 if not defined
	 *
	 *  @param request request
	 *  @param parameter parameter
	 *  @return int result or 0
	 */
	public static int getParameterAsInt(HttpServletRequest request, String parameter)
	{
		if (request == null || parameter == null)
			return 0;
		String data = request.getParameter(parameter);
		if (data == null || data.length() == 0)
			return 0;
		try
		{
			return Integer.parseInt(data);
		}
		catch (Exception e)
		{
			s_log.debug("getParameterAsInt - " + parameter + "=" + data, e);
		}
		return 0;
	}   //  getParameter

	/*************************************************************************/

	/**
	 *  Create Standard Response Header with optional Cookie and print document.
	 *  D:\j2sdk1.4.0\docs\guide\intl\encoding.doc.html
	 *
	 *  @param request request
	 *  @param response response
	 *  @param servlet servlet
	 *  @param cookieProperties cookie properties
	 *  @param doc doc
	 *  @param debug debug
	 *  @throws IOException
	 */
	public static void createResponse (HttpServletRequest request, HttpServletResponse response,
		HttpServlet servlet, Properties cookieProperties, WDoc doc, boolean debug) throws IOException
	{
		response.setHeader("Cache-Control", "no-cache");
	//	response.setContentType("text/html; charset=ISO-8859-1");   //  default
		response.setContentType("text/html; charset=UTF-8");

		//
		//  Update Cookie - overwrite
		if (cookieProperties != null)
		{
			Cookie cookie = new Cookie (WEnv.COOKIE_INFO, propertiesEncode(cookieProperties));
			cookie.setComment("(c) ComPiere, Inc - Jorg Janke");
			cookie.setSecure(false);
			cookie.setPath("/");
			if (cookieProperties.size() == 0)
				cookie.setMaxAge(0);            //  delete cookie
			else
				cookie.setMaxAge(2592000);      //  30 days in seconds   60*60*24*30
			response.addCookie(cookie);
		}
		//  add diagnostics
		if (debug && WEnv.DEBUG)
		{
		//	doc.output(System.out);
			WEnv.addFooter(request, response, servlet, doc.getBody());
		//	doc.output(System.out);
		}
	//	String content = doc.toString();
	//  response.setContentLength(content.length());    //  causes problems at the end of the output

		//  print document
		PrintWriter out = response.getWriter();     //  with character encoding support
		doc.output(out);
		out.flush();
		if (out.checkError())
			s_log.error("createResponse - error writing");
		//  binary output (is faster but does not do character set conversion)
	//	OutputStream out = response.getOutputStream();
	//	byte[] data = doc.toString().getBytes();
	//	response.setContentLength(data.length);
	//	out.write(doc.toString().getBytes());
		//
		out.close();
	}   //  createResponse

	/*************************************************************************/

	/**
	 *  Create Java Script to clear Target frame and display message
	 *
	 *  @param target target
	 *  @return Clear Frame Script
	 */
	public static script getClearFrame (String target)
	{
		StringBuffer cmd = new StringBuffer();
		cmd.append("var d = parent.").append(target).append(".document;\n");
		cmd.append("d.open();\n");
		cmd.append("d.write('<link href=\"").append(WEnv.getStylesheetURL()).append("\" rel=\"stylesheet\">');\n");
		cmd.append("d.close();");
		//
		return new script(cmd.toString());
	}   //  getClearFrame


	/**
	 * 	Return a link and script with new location.
	 * 	Wait 2 seconds
	 * 	@param url forward url
	 * 	@return html
	 */
	public static HtmlCode getForward (String url)
	{
		HtmlCode retValue = new HtmlCode();
		//	Link
		a a = new a(url);
		a.addElement(url);
		retValue.addElement(a);
		//	Java Script	- document.location - 2.5 sec delay
		script script = new script("setTimeout(\"window.top.location.replace('" + url + "')\",2500);");
		retValue.addElement(script);
		//
		return retValue;
	}	//	getForward

	/**
	 * 	Create Forward Page
	 * 	@param response response
	 * 	@param title page title
	 * 	@param forwardURL url
	 * 	@throws ServletException
	 * 	@throws IOException
	 */
	public static void createForwardPage (HttpServletResponse response,
		String title, String forwardURL) throws ServletException, IOException
	{
		response.setContentType("text/html; charset=UTF-8");
		WDoc doc = WDoc.create(title);
		body b = doc.getBody();
		b.addElement(getForward(forwardURL));
		PrintWriter out = response.getWriter();
		doc.output(out);
		out.flush();
		if (out.checkError())
			s_log.error("createForwardPage - error writing");
		out.close();
		s_log.debug("createForwardPage - " + forwardURL + " - " + title);
	}	//	createForwardPage


	/**
	 * 	Does Test exist
	 *	@param test string
	 *	@return true if String with data
	 */
	public static boolean exists (String test)
	{
		if (test == null)
			return false;
		return test.length() > 0;
	}	//	exists

	/**
	 * 	Does Parameter exist
	 * 	@param request request
	 *	@param parameter string
	 *	@return true if String with data
	 */
	public static boolean exists (HttpServletRequest request, String parameter)
	{
		if (request == null || parameter == null)
			return false;
		return exists (request.getParameter(parameter));
	}	//	exists


	/**
	 *	Is EMail address valid
	 * 	@param email mail address
	 * 	@return true if valid
	 */
	public static boolean isEmailValid (String email)
	{
		if (email == null || email.length () == 0)
			return false;
		try
		{
			InternetAddress ia = new InternetAddress (email, true);
			return true;
		}
		catch (AddressException ex)
		{
			s_log.warn ("isEmailValid - " + email + " - "
				+ ex.getLocalizedMessage ());
		}
		return false;
	}	//	isEmailValid


	/*************************************************************************/

	/**
	 *  Decode Properties into String (URL encoded)
	 *
	 *  @param pp properties
	 *  @return Encoded String
	 */
	public static String propertiesEncode (Properties pp)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try
		{
			pp.store(bos, "Compiere");   //  Header
		}
		catch (IOException e)
		{
			s_log.error("propertiesEncode-store", e);
		}
		String result = new String (bos.toByteArray());
	//	System.out.println("String=" + result);
		String enc = "UTF-8";
		try
		{
			result = java.net.URLEncoder.encode(result, enc);
		}
		catch (UnsupportedEncodingException e)
		{
			s_log.error("propertiesEncode-encode-" + enc, e);
			enc = System.getProperty("file.encoding");      //  Windows default is Cp1252
			try
			{
				result = java.net.URLEncoder.encode(result, enc);
			}
			catch (Exception ex)
			{
				s_log.error("propertiesEncode-encode", ex);
			}
		}
	//	System.out.println("String-Encoded=" + result);
		return result;
	}   //  propertiesEncode

	/**
	 *  Decode data String (URL encoded) into Properties
	 *
	 *  @param data data
	 *  @return Properties
	 */
	public static Properties propertiesDecode (String data)
	{
		String result = null;
	//	System.out.println("String=" + data);
		String enc = "UTF-8";
		try
		{
			result = java.net.URLDecoder.decode(data, enc);
		}
		catch (UnsupportedEncodingException e)
		{
			s_log.error("propertiesDecode-decode-" + enc, e);
			enc = System.getProperty("file.encoding");      //  Windows default is Cp1252
			try
			{
				result = java.net.URLEncoder.encode(data, enc);
			}
			catch (Exception ex)
			{
				s_log.error("propertiesDecode-decode", ex);
			}
		}
	//	System.out.println("String-Decoded=" + result);

		ByteArrayInputStream bis = new ByteArrayInputStream(result.getBytes());
		Properties pp = new Properties();
		try
		{
			pp.load(bis);
		}
		catch (IOException e)
		{
			s_log.error("propertiesDecode-load", e);
		}
		return pp;
	}   //  propertiesDecode

	/*************************************************************************/

	/**
	 *  Convert Array of NamePair to HTTP Option Array.
	 *  <p>
	 *  If the ArrayList does not contain NamePairs, the String value is used
	 *  @see org.compiere.util.NamePair
	 *  @param  list    ArrayList containing NamePair values
	 *  @param  default_ID  Sets the default if the key/ID value is found.
	 *      If the value is null or empty, the first value is selected
	 *  @return Option Array
	 */
	public static option[] convertToOption (NamePair[] list, String default_ID)
	{
		int size = list.length;
		option[] retValue = new option[size];
		for (int i = 0; i < size; i++)
		{
			boolean selected = false;
			//  select first entry
			if (i == 0 && (default_ID == null || default_ID.length() == 0))
				selected = true;

			//  Create option
			retValue[i] = new option(list[i].getID()).addElement(list[i].getName());

			//  Select if ID/Key is same as default ID
			if (default_ID != null && default_ID.equals(list[i].getID()))
				selected = true;
			retValue[i].setSelected(selected);
		}
		return retValue;
	}   //  convertToOption

	/**
	 *  Create label/field table row
	 *
	 *  @param line - null for new line (table row)
	 *  @param FORMNAME form name
	 *  @param PARAMETER parameter name
	 *  @param labelText label
	 *  @param inputType HTML input type
	 *  @param value data value
	 *  @param sizeDisplay display size
	 *  @param size data size
	 *  @param longField field spanning two columns
	 *  @param mandatory mark as mandatory
	 *  @param onChange onChange call
	 *  @param script script
	 *  @return tr table row
	 */
	static public tr createField (tr line, String FORMNAME, String PARAMETER,
		String labelText, String inputType, Object value,
		int sizeDisplay, int size, boolean longField, boolean mandatory, String onChange, StringBuffer script)
	{
		if (line == null)
			line = new tr();
		String labelInfo = labelText;
		if (mandatory)
		{
			labelInfo += "&nbsp;<font color=\"red\">*</font>";
			String fName = "document." + FORMNAME + "." + PARAMETER;
			script.append(fName).append(".required=true; ");
		}

		label llabel = new label().setFor(PARAMETER).addElement(labelInfo);
		llabel.setID("ID_" + PARAMETER + "_Label");
	//	label.setTitle(description);
		line.addElement(new td().addElement(llabel).setAlign(AlignType.right));
		input iinput = new input(inputType, PARAMETER, value == null ? "" : value.toString());
		iinput.setSize(sizeDisplay).setMaxlength(size);
		iinput.setID("ID_" + PARAMETER);
		if (onChange != null && onChange.length() > 0)
			iinput.setOnChange(onChange);
		iinput.setTitle(labelText);
		td field = new td().addElement(iinput).setAlign(AlignType.LEFT);
		if (longField)
			field.setColSpan(3);
		line.addElement(field);
		return line;
	}   //  addField

}   //  WUtil
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
import java.util.*;
import java.sql.*;
import java.io.*;
import javax.mail.internet.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.apache.log4j.Logger;

import org.compiere.wstore.*;
import org.compiere.util.*;
import org.compiere.model.*;

/**
 *  Servlet Utilities
 *
 *  @author Jorg Janke
 *  @version  $Id: WUtil.java,v 1.12 2003/07/19 05:21:44 jjanke Exp $
 */
public final class WUtil
{
	/**	Logging								*/
	private static Logger			s_log = Logger.getLogger("org.compiere.www.WUtil");

	/**
	 *  Create Timeout Message
	 *
	 *  @param request request
	 *  @param response response
	 *  @param servlet servlet
	 *  @param ctx context
	 *  @param message - optional message
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public static void createTimeoutPage (HttpServletRequest request, HttpServletResponse response,
		HttpServlet servlet, Properties ctx, String message) throws ServletException, IOException
	{
		s_log.info("createTimeoutPage - " + message);
		String windowTitle = "Timeout";
		if (ctx != null)
			windowTitle = Msg.getMsg(ctx, "Timeout");

		WDoc doc = WDoc.create (windowTitle);

		//	Body
		body body = doc.getBody();
		//  optional message
		if (message != null && message.length() > 0)
			body.addElement(new p(message));

		//  login button
		body.addElement(getLoginButton(ctx));

		//
		body.addElement(new hr());
		body.addElement(new small(servlet.getClass().getName()));
		//	fini
		createResponse (request, response, servlet, null, doc, false);
	}   //  createTimeoutPage

	/**
	 *  Create Error Message
	 *
	 *  @param request request
	 *  @param response response
	 *  @param servlet servlet
	 *  @param ctx context
	 *  @param message message
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public static void createErrorPage (HttpServletRequest request, HttpServletResponse response,
		HttpServlet servlet, Properties ctx, String message) throws ServletException, IOException
	{
		s_log.info("createErrorPage - " + message);
		String windowTitle = "Error";
		if (ctx != null)
			windowTitle = Msg.getMsg(ctx, "Error");		//	JUST TO GET LANGUAGE ??
		if (message != null)
			windowTitle += ": " + message;

		WDoc doc = WDoc.create (windowTitle);

		//	Body
		body b = doc.getBody();

		b.addElement(new p(servlet.getServletName(), AlignType.center));
		b.addElement(new br());

		//	fini
		createResponse (request, response, servlet, null, doc, true);
	}   //  createErrorPage

	/**
	 *  Create Exit Page "Log-off".
	 *  <p>
	 *  - End Session
	 *  - Go to start page (e.g. /compiere/index.html)
	 *
	 *  @param request request
	 *  @param response response
	 *  @param servlet servlet
	 *  @param ctx context
	 *  @param AD_Message messahe
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public static void createLoginPage (HttpServletRequest request, HttpServletResponse response,
		HttpServlet servlet, Properties ctx, String AD_Message) throws ServletException, IOException
	{
		request.getSession().invalidate();
		String url = WEnv.getBaseDirectory("index.html");
		//
		WDoc doc = null;
		if (ctx != null && AD_Message != null && !AD_Message.equals(""))
			doc = WDoc.create (Msg.getMsg(ctx, AD_Message));
		else if (AD_Message != null)
			doc = WDoc.create (AD_Message);
		else
			doc = WDoc.create (false);
		script script = new script("window.top.location.replace('" + url + "');");
		doc.getBody().addElement(script);
		//
		createResponse (request, response, servlet, null, doc, false);
	}   //  createLoginPage

	/**
	 *  Create Login Button - replace Window
	 *
	 *  @param ctx context
	 *  @return Button
	 */
	public static button getLoginButton(Properties ctx)
	{
		String text = Msg.getMsg(ctx, "Login");
		button button = new button();
		button.setType("button").setName("Login").addElement(text);
		StringBuffer cmd = new StringBuffer ("window.top.location.replace('");
		cmd.append(WEnv.getBaseDirectory("index.html"));
		cmd.append("');");
		button.setOnClick(cmd.toString());
		return button;
	}   //  getLoginButton

	/*************************************************************************/

	/**
	 *  Get Cookie Properties
	 *
	 *  @param request request
	 *  @return Properties
	 */
	public static Properties getCookieProprties(HttpServletRequest request)
	{
		//  Get Properties
		Cookie[] cookies = request.getCookies();
		if (cookies != null)
		{
			for (int i = 0; i < cookies.length; i++)
			{
				if (cookies[i].getName().equals(WEnv.COOKIE_INFO))
					return propertiesDecode(cookies[i].getValue());
			}
		}
		return new Properties();
	}   //  getProperties

	/**
	 *  Get numeric Parameter - 0 if not defined
	 *
	 *  @param request request
	 *  @param parameter parameter
	 *  @return int result or 0
	 */
	public static int getParameterAsInt(HttpServletRequest request, String parameter)
	{
		if (request == null || parameter == null)
			return 0;
		String data = request.getParameter(parameter);
		if (data == null || data.length() == 0)
			return 0;
		try
		{
			return Integer.parseInt(data);
		}
		catch (Exception e)
		{
			s_log.debug("getParameterAsInt - " + parameter + "=" + data, e);
		}
		return 0;
	}   //  getParameter

	/*************************************************************************/

	/**
	 *  Create Standard Response Header with optional Cookie and print document.
	 *  D:\j2sdk1.4.0\docs\guide\intl\encoding.doc.html
	 *
	 *  @param request request
	 *  @param response response
	 *  @param servlet servlet
	 *  @param cookieProperties cookie properties
	 *  @param doc doc
	 *  @param debug debug
	 *  @throws IOException
	 */
	public static void createResponse (HttpServletRequest request, HttpServletResponse response,
		HttpServlet servlet, Properties cookieProperties, WDoc doc, boolean debug) throws IOException
	{
		response.setHeader("Cache-Control", "no-cache");
	//	response.setContentType("text/html; charset=ISO-8859-1");   //  default
		response.setContentType("text/html; charset=UTF-8");

		//
		//  Update Cookie - overwrite
		if (cookieProperties != null)
		{
			Cookie cookie = new Cookie (WEnv.COOKIE_INFO, propertiesEncode(cookieProperties));
			cookie.setComment("(c) ComPiere, Inc - Jorg Janke");
			cookie.setSecure(false);
			cookie.setPath("/");
			if (cookieProperties.size() == 0)
				cookie.setMaxAge(0);            //  delete cookie
			else
				cookie.setMaxAge(2592000);      //  30 days in seconds   60*60*24*30
			response.addCookie(cookie);
		}
		//  add diagnostics
		if (debug && WEnv.DEBUG)
		{
		//	doc.output(System.out);
			WEnv.addFooter(request, response, servlet, doc.getBody());
		//	doc.output(System.out);
		}
	//	String content = doc.toString();
	//  response.setContentLength(content.length());    //  causes problems at the end of the output

		//  print document
		PrintWriter out = response.getWriter();     //  with character encoding support
		doc.output(out);
		out.flush();
		if (out.checkError())
			s_log.error("createResponse - error writing");
		//  binary output (is faster but does not do character set conversion)
	//	OutputStream out = response.getOutputStream();
	//	byte[] data = doc.toString().getBytes();
	//	response.setContentLength(data.length);
	//	out.write(doc.toString().getBytes());
		//
		out.close();
	}   //  createResponse

	/*************************************************************************/

	/**
	 *  Create Java Script to clear Target frame and display message
	 *
	 *  @param target target
	 *  @return Clear Frame Script
	 */
	public static script getClearFrame (String target)
	{
		StringBuffer cmd = new StringBuffer();
		cmd.append("var d = parent.").append(target).append(".document;\n");
		cmd.append("d.open();\n");
		cmd.append("d.write('<link href=\"").append(WEnv.getStylesheetURL()).append("\" rel=\"stylesheet\">');\n");
		cmd.append("d.close();");
		//
		return new script(cmd.toString());
	}   //  getClearFrame


	/**
	 * 	Return a link and script with new location.
	 * 	Wait 2 seconds
	 * 	@param url forward url
	 * 	@return html
	 */
	public static HtmlCode getForward (String url)
	{
		HtmlCode retValue = new HtmlCode();
		//	Link
		a a = new a(url);
		a.addElement(url);
		retValue.addElement(a);
		//	Java Script	- document.location - 2.5 sec delay
		script script = new script("setTimeout(\"window.top.location.replace('" + url + "')\",2500);");
		retValue.addElement(script);
		//
		return retValue;
	}	//	getForward

	/**
	 * 	Create Forward Page
	 * 	@param response response
	 * 	@param title page title
	 * 	@param forwardURL url
	 * 	@throws ServletException
	 * 	@throws IOException
	 */
	public static void createForwardPage (HttpServletResponse response,
		String title, String forwardURL) throws ServletException, IOException
	{
		response.setContentType("text/html; charset=UTF-8");
		WDoc doc = WDoc.create(title);
		body b = doc.getBody();
		b.addElement(getForward(forwardURL));
		PrintWriter out = response.getWriter();
		doc.output(out);
		out.flush();
		if (out.checkError())
			s_log.error("createForwardPage - error writing");
		out.close();
		s_log.debug("createForwardPage - " + forwardURL + " - " + title);
	}	//	createForwardPage


	/**
	 * 	Does Test exist
	 *	@param test string
	 *	@return true if String with data
	 */
	public static boolean exists (String test)
	{
		if (test == null)
			return false;
		return test.length() > 0;
	}	//	exists

	/**
	 * 	Does Parameter exist
	 * 	@param request request
	 *	@param parameter string
	 *	@return true if String with data
	 */
	public static boolean exists (HttpServletRequest request, String parameter)
	{
		if (request == null || parameter == null)
			return false;
		return exists (request.getParameter(parameter));
	}	//	exists


	/**
	 *	Is EMail address valid
	 * 	@param email mail address
	 * 	@return true if valid
	 */
	public static boolean isEmailValid (String email)
	{
		if (email == null || email.length () == 0)
			return false;
		try
		{
			InternetAddress ia = new InternetAddress (email, true);
			return true;
		}
		catch (AddressException ex)
		{
			s_log.warn ("isEmailValid - " + email + " - "
				+ ex.getLocalizedMessage ());
		}
		return false;
	}	//	isEmailValid


	/*************************************************************************/

	/**
	 *  Decode Properties into String (URL encoded)
	 *
	 *  @param pp properties
	 *  @return Encoded String
	 */
	public static String propertiesEncode (Properties pp)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try
		{
			pp.store(bos, "Compiere");   //  Header
		}
		catch (IOException e)
		{
			s_log.error("propertiesEncode-store", e);
		}
		String result = new String (bos.toByteArray());
	//	System.out.println("String=" + result);
		String enc = "UTF-8";
		try
		{
			result = java.net.URLEncoder.encode(result, enc);
		}
		catch (UnsupportedEncodingException e)
		{
			s_log.error("propertiesEncode-encode-" + enc, e);
			enc = System.getProperty("file.encoding");      //  Windows default is Cp1252
			try
			{
				result = java.net.URLEncoder.encode(result, enc);
			}
			catch (Exception ex)
			{
				s_log.error("propertiesEncode-encode", ex);
			}
		}
	//	System.out.println("String-Encoded=" + result);
		return result;
	}   //  propertiesEncode

	/**
	 *  Decode data String (URL encoded) into Properties
	 *
	 *  @param data data
	 *  @return Properties
	 */
	public static Properties propertiesDecode (String data)
	{
		String result = null;
	//	System.out.println("String=" + data);
		String enc = "UTF-8";
		try
		{
			result = java.net.URLDecoder.decode(data, enc);
		}
		catch (UnsupportedEncodingException e)
		{
			s_log.error("propertiesDecode-decode-" + enc, e);
			enc = System.getProperty("file.encoding");      //  Windows default is Cp1252
			try
			{
				result = java.net.URLEncoder.encode(data, enc);
			}
			catch (Exception ex)
			{
				s_log.error("propertiesDecode-decode", ex);
			}
		}
	//	System.out.println("String-Decoded=" + result);

		ByteArrayInputStream bis = new ByteArrayInputStream(result.getBytes());
		Properties pp = new Properties();
		try
		{
			pp.load(bis);
		}
		catch (IOException e)
		{
			s_log.error("propertiesDecode-load", e);
		}
		return pp;
	}   //  propertiesDecode

	/*************************************************************************/

	/**
	 *  Convert Array of NamePair to HTTP Option Array.
	 *  <p>
	 *  If the ArrayList does not contain NamePairs, the String value is used
	 *  @see org.compiere.util.NamePair
	 *  @param  list    ArrayList containing NamePair values
	 *  @param  default_ID  Sets the default if the key/ID value is found.
	 *      If the value is null or empty, the first value is selected
	 *  @return Option Array
	 */
	public static option[] convertToOption (NamePair[] list, String default_ID)
	{
		int size = list.length;
		option[] retValue = new option[size];
		for (int i = 0; i < size; i++)
		{
			boolean selected = false;
			//  select first entry
			if (i == 0 && (default_ID == null || default_ID.length() == 0))
				selected = true;

			//  Create option
			retValue[i] = new option(list[i].getID()).addElement(list[i].getName());

			//  Select if ID/Key is same as default ID
			if (default_ID != null && default_ID.equals(list[i].getID()))
				selected = true;
			retValue[i].setSelected(selected);
		}
		return retValue;
	}   //  convertToOption

	/**
	 *  Create label/field table row
	 *
	 *  @param line - null for new line (table row)
	 *  @param FORMNAME form name
	 *  @param PARAMETER parameter name
	 *  @param labelText label
	 *  @param inputType HTML input type
	 *  @param value data value
	 *  @param sizeDisplay display size
	 *  @param size data size
	 *  @param longField field spanning two columns
	 *  @param mandatory mark as mandatory
	 *  @param onChange onChange call
	 *  @param script script
	 *  @return tr table row
	 */
	static public tr createField (tr line, String FORMNAME, String PARAMETER,
		String labelText, String inputType, Object value,
		int sizeDisplay, int size, boolean longField, boolean mandatory, String onChange, StringBuffer script)
	{
		if (line == null)
			line = new tr();
		String labelInfo = labelText;
		if (mandatory)
		{
			labelInfo += "&nbsp;<font color=\"red\">*</font>";
			String fName = "document." + FORMNAME + "." + PARAMETER;
			script.append(fName).append(".required=true; ");
		}

		label llabel = new label().setFor(PARAMETER).addElement(labelInfo);
		llabel.setID("ID_" + PARAMETER + "_Label");
	//	label.setTitle(description);
		line.addElement(new td().addElement(llabel).setAlign(AlignType.right));
		input iinput = new input(inputType, PARAMETER, value == null ? "" : value.toString());
		iinput.setSize(sizeDisplay).setMaxlength(size);
		iinput.setID("ID_" + PARAMETER);
		if (onChange != null && onChange.length() > 0)
			iinput.setOnChange(onChange);
		iinput.setTitle(labelText);
		td field = new td().addElement(iinput).setAlign(AlignType.LEFT);
		if (longField)
			field.setColSpan(3);
		line.addElement(field);
		return line;
	}   //  addField

}   //  WUtil
