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
 *  Web Request.
 *
 *  @author     Jorg Janke
 *  @version    $Id: Request.java,v 1.3 2002/10/30 05:05:16 jjanke Exp $
 */
public class Request extends HttpServlet
{
	/**	Logging						*/
	private Logger			log = Logger.getLogger(getClass());
	/**	Client						*/
	private int				m_AD_Client_ID = -1;

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
			throw new ServletException("Request.init");

		//	Get Client
		m_AD_Client_ID = WEnv.getAD_Client_ID(config);
		log.info("init - AD_Client_ID=" + m_AD_Client_ID);
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Request";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
	}   //  destroy

	/*************************************************************************/

	public static final String  P_FORWARDTO     = "ForwardTo";
	public static final String  P_SOURCE        = "Source";
	public static final String  P_INFO          = "Info";
	public static final String  P_CLIENT        = "AD_Client_ID";

	public static final String  P_REQUESTTYPE   = "RequestType";
	public static final String  P_NAME          = "Name";
	public static final String  P_COMPANY       = "Company";
	public static final String  P_ADDRESS       = "Address";
	public static final String  P_CITY          = "City";
	public static final String  P_POSTAL        = "Postal";
	public static final String  P_REGION        = "Region";
	public static final String  P_COUNTRY       = "Country";
	public static final String  P_EMAIL         = "EMail";
	public static final String  P_PHONE         = "Phone";
	public static final String  P_QUESTION      = "Question";

	/**
	 *  Process the HTTP Get request
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

		WDoc doc = WDoc.create ("Web Request");

		//  Form posting to same servlet
		form form = null;
		form = new form (request.getRequestURI(), form.post, form.ENC_DEFAULT).setName("Request");
		form.addElement(new input (input.hidden, P_FORWARDTO, "http://www.compiere.org"));
		form.addElement(new input (input.hidden, P_SOURCE, ""));
		form.addElement(new input (input.hidden, P_INFO, ""));
		form.addElement(new input (input.hidden, P_CLIENT, String.valueOf(m_AD_Client_ID)));
		String script = "document.Request." + P_SOURCE + ".value=document.referrer; "
			+ "document.Request." + P_INFO + ".value=document.lastModified;";
		form.addElement(new script().addElement(script));

		table table = new table().setAlign(AlignType.center);
		//  RequestType
		tr line = new tr();
		label label = new label().setFor(P_REQUESTTYPE).addElement("Request Type");
		label.setID("ID_"+P_REQUESTTYPE);
		line.addElement(new td().addElement(label).setAlign(AlignType.right));
		option options[] = getRequestTypes();
		line.addElement(new td().addElement(new select(P_REQUESTTYPE, options).setID("ID_"+P_REQUESTTYPE)));
		table.addElement(line);
		//  Name
		line = new tr();
		label = new label().setFor(P_NAME).addElement("Name");
		label.setID("ID_"+P_NAME);
		line.addElement(new td().addElement(label).setAlign(AlignType.right));
		input input = null;
		input = new input(input.text, P_NAME, "").setSize(30).setMaxlength(60);
		input.setID("ID_"+P_NAME);
		line.addElement(new td().addElement(input).setAlign(AlignType.left));
		table.addElement(line);
		//  Company
		line = new tr();
		label = new label().setFor(P_COMPANY).addElement("Company");
		label.setID("ID_"+P_COMPANY);
		line.addElement(new td().addElement(label).setAlign(AlignType.right));
		input = new input(input.text, P_COMPANY, "").setSize(30).setMaxlength(60);
		input.setID("ID_"+P_COMPANY);
		line.addElement(new td().addElement(input).setAlign(AlignType.left));
		table.addElement(line);
		//  Address
		line = new tr();
		label = new label().setFor(P_ADDRESS).addElement("Address");
		label.setID("ID_"+P_ADDRESS);
		line.addElement(new td().addElement(label).setAlign(AlignType.right));
		input = new input(input.text, P_ADDRESS, "").setSize(30).setMaxlength(60);
		input.setID("ID_"+P_ADDRESS);
		line.addElement(new td().addElement(input).setAlign(AlignType.left));
		table.addElement(line);
		//  City
		line = new tr();
		label = new label().setFor(P_CITY).addElement("City");
		label.setID("ID_"+P_CITY);
		line.addElement(new td().addElement(label).setAlign(AlignType.right));
		input = new input(input.text, P_CITY, "").setSize(30).setMaxlength(60);
		input.setID("ID_"+P_CITY);
		line.addElement(new td().addElement(input).setAlign(AlignType.left));
		table.addElement(line);
		//  Postal
		line = new tr();
		label = new label().setFor(P_POSTAL).addElement("Zip");
		label.setID("ID_"+P_POSTAL);
		line.addElement(new td().addElement(label).setAlign(AlignType.right));
		input = new input(input.text, P_POSTAL, "").setSize(10).setMaxlength(20);
		input.setID("ID_"+P_POSTAL);
		line.addElement(new td().addElement(input).setAlign(AlignType.left));
		table.addElement(line);
		//  Region
		line = new tr();
		label = new label().setFor(P_REGION).addElement("State");
		label.setID("ID_"+P_REGION);
		line.addElement(new td().addElement(label).setAlign(AlignType.right));
		input = new input(input.text, P_REGION, "").setSize(20).setMaxlength(20);
		input.setID("ID_"+P_REGION);
		line.addElement(new td().addElement(input).setAlign(AlignType.left));
		table.addElement(line);
		//  Country
		line = new tr();
		label = new label().setFor(P_COUNTRY).addElement("Country");
		label.setID("ID_"+P_COUNTRY);
		line.addElement(new td().addElement(label).setAlign(AlignType.right));
		input = new input(input.text, P_COUNTRY, "").setSize(20).setMaxlength(20);
		input.setID("ID_"+P_COUNTRY);
		line.addElement(new td().addElement(input).setAlign(AlignType.left));
		table.addElement(line);
		//  EMail
		line = new tr();
		label = new label().setFor(P_EMAIL).addElement("EMail");
		label.setID("ID_"+P_EMAIL);
		line.addElement(new td().addElement(label).setAlign(AlignType.right));
		input = new input(input.text, P_EMAIL, "").setSize(30).setMaxlength(60);
		input.setID("ID_"+P_EMAIL);
		line.addElement(new td().addElement(input).setAlign(AlignType.left));
		table.addElement(line);
		//  Phone
		line = new tr();
		label = new label().setFor(P_PHONE).addElement("Phone");
		label.setID("ID_"+P_PHONE);
		line.addElement(new td().addElement(label).setAlign(AlignType.right));
		input = new input(input.text, P_PHONE, "").setSize(20).setMaxlength(20);
		input.setID("ID_"+P_PHONE);
		line.addElement(new td().addElement(input).setAlign(AlignType.left));
		table.addElement(line);
		//  Question
		line = new tr();
		label = new label().setFor(P_QUESTION).addElement("Question, Request");
		label.setID("ID_"+P_QUESTION);
		line.addElement(new td().addElement(label).setAlign(AlignType.right));
		table.addElement(line);
		line = new tr();
		textarea text = new textarea(P_QUESTION, 6, 40).addElement("");
		text.setID("ID_" + P_QUESTION);
//		text.setClass(C_MANDATORY);
		line.addElement(new td().addElement(text).setAlign(AlignType.left).setColSpan(2));
		table.addElement(line);
		//  Submit
		line = new tr();
		line.addElement(new td().addElement(new input(input.reset, "Reset", "Clear") ));
		line.addElement(new td().addElement(new input(input.submit, "Submit", "Send")).setAlign(AlignType.right));
		table.addElement(line);
		//
		form.addElement(table);
		doc.getBody().addElement(form);
		//
		WUtil.createResponse(request, response, this, null, doc, true);
	}   //  doGet

	/**
	 *  Get Request Types
	 *  @return Options with Request Types
	 */
	private option[] getRequestTypes()
	{
		ArrayList list = new ArrayList();
		String sql = "SELECT R_RequestType_ID, Name, IsDefault "
			+ "FROM R_RequestType WHERE AD_Client_ID=? ORDER BY Name";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, m_AD_Client_ID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				String id = rs.getString(1);
				String name = rs.getString(2);
				boolean isDefault = "Y".equals(rs.getString(3));
				//
				option o = new option(id).addElement(name);
				if (isDefault)
					o.setSelected(true);
				list.add(o);
			}
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			log.error("getRequestType", e);
		}
		option[] op = new option[list.size()];
		list.toArray(op);
		return op;
	}   //  getRequestType


	/*************************************************************************/

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

		//  Get Session attributes
		HttpSession sess = request.getSession(false);
		if (sess != null)
		{
			Properties ctx = (Properties)sess.getAttribute(WEnv.SA_CONTEXT);
			Properties cProp = WUtil.getCookieProprties(request);
		}

		StringBuffer sql = new StringBuffer ("INSERT INTO W_Request "
			+ "(W_Request_ID,AD_Client_ID,AD_Org_ID, "
			+ "IsActive,Created,CreatedBy,Updated,UpdatedBy, "
			+ "R_RequestType_ID,Question, "
			+ "Name,Company, "
			+ "Address,City,Postal,Region,Country, "
			+ "EMail,Phone, "
			+ "PageURL,Referrer, "
			+ "WebUserID,AcceptLanguage, "
			+ "Remote_Addr,UserAgent, "
			+ "FindBPartner,Processing,Processed) VALUES (");
		//
		//  W_Request_ID,AD_Client_ID,AD_Org_ID,
		int AD_Client_ID = m_AD_Client_ID;
		try
		{
			String s = request.getParameter(P_CLIENT);
			if (s != null && s.length() > 0)
				AD_Client_ID = Integer.parseInt(s);
		}
		catch (NumberFormatException ex)
		{
		}
		int W_Request_ID = DB.getKeyNextNo(AD_Client_ID, "N", "W_Request");
		sql.append(W_Request_ID).append(",").append(AD_Client_ID).append(",0, ");

		//  IsActive,Created,CreatedBy,Updated,UpdatedBy,
		sql.append("'Y',SysDate,0,SysDate,0, ");

		//  RequestType,Question,
		String RequestType = request.getParameter(P_REQUESTTYPE);
		if (RequestType == null)
			RequestType = "null";
		sql.append(RequestType).append(",")
			.append(DB.TO_STRING(request.getParameter(P_QUESTION), 2000)).append(", ");

		//  Name,Company,
		sql.append(DB.TO_STRING(request.getParameter(P_NAME), 60)).append(",")
			.append(DB.TO_STRING(request.getParameter(P_COMPANY), 60)).append(", ");

		//  Address,City,Postal,Region,Country,
		sql.append(DB.TO_STRING(request.getParameter(P_ADDRESS), 60)).append(",")
			.append(DB.TO_STRING(request.getParameter(P_CITY), 60)).append(",")
			.append(DB.TO_STRING(request.getParameter(P_POSTAL), 20)).append(",")
			.append(DB.TO_STRING(request.getParameter(P_REGION), 20)).append(",")
			.append(DB.TO_STRING(request.getParameter(P_COUNTRY), 20)).append(", ");

		//  EMail,Phone,
		sql.append(DB.TO_STRING(request.getParameter(P_EMAIL), 60)).append(",")
			.append(DB.TO_STRING(request.getParameter(P_PHONE), 20)).append(", ");

		//  PageURL,Referrer,
		String requestURL = request.getRequestURL().toString();
		String requestRef = request.getHeader("referer");
		String source = request.getParameter(P_SOURCE);
		String info = request.getParameter(P_INFO);
		if (requestURL == null)
			requestURL = "";
		if (requestURL.equals(requestRef))      //  if URL and Referrer are the same, get source
		{
			requestRef = source;
			source = null;
		}
		sql.append(DB.TO_STRING(requestURL, 255)).append(",")
			.append(DB.TO_STRING(requestRef, 255)).append(", ");

		//  WebUserID,AcceptLanguage,
		String web = source;
		if (web == null)
			web = info;
		else if (info != null)
			web += " - " + info;
		sql.append(DB.TO_STRING(web, 60)).append(",")
			.append(DB.TO_STRING(request.getHeader("accept-language"), 60)).append(", ");

		//  Remote_Addr,UserAgent,
		sql.append(DB.TO_STRING(request.getRemoteAddr(), 60)).append(",")
			.append(DB.TO_STRING(request.getHeader("user-agent"), 60)).append(", ");

		//  FindBPartner,Processing,Processed)
		sql.append("'N','N','N')");
		int no = DB.executeUpdate(sql.toString());
		if (no != 1)
		{
			log.error("post - Request NOT saved - #=" + no);
			WUtil.createErrorPage(request, response, this, Env.getCtx(), "Error");		//	Env??
			return;
		}

		//  --  Fini
		WDoc doc = WDoc.create ("Web Request Received");
		String ForwardTo = request.getParameter(P_FORWARDTO);
		if (ForwardTo == null || ForwardTo.length() == 0)
			ForwardTo = "http://www.compiere.org";

		script script = new script("window.top.location.replace('" + ForwardTo + "');");
		doc.getBody().addElement(script);
		doc.getBody().addElement(new a (ForwardTo, "Thanks"));
		//
		WUtil.createResponse(request, response, this, null, doc, false);
	}   //  doPost

}   //  Request
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
 *  Web Request.
 *
 *  @author     Jorg Janke
 *  @version    $Id: Request.java,v 1.3 2002/10/30 05:05:16 jjanke Exp $
 */
public class Request extends HttpServlet
{
	/**	Logging						*/
	private Logger			log = Logger.getLogger(getClass());
	/**	Client						*/
	private int				m_AD_Client_ID = -1;

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
			throw new ServletException("Request.init");

		//	Get Client
		m_AD_Client_ID = WEnv.getAD_Client_ID(config);
		log.info("init - AD_Client_ID=" + m_AD_Client_ID);
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Request";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
	}   //  destroy

	/*************************************************************************/

	public static final String  P_FORWARDTO     = "ForwardTo";
	public static final String  P_SOURCE        = "Source";
	public static final String  P_INFO          = "Info";
	public static final String  P_CLIENT        = "AD_Client_ID";

	public static final String  P_REQUESTTYPE   = "RequestType";
	public static final String  P_NAME          = "Name";
	public static final String  P_COMPANY       = "Company";
	public static final String  P_ADDRESS       = "Address";
	public static final String  P_CITY          = "City";
	public static final String  P_POSTAL        = "Postal";
	public static final String  P_REGION        = "Region";
	public static final String  P_COUNTRY       = "Country";
	public static final String  P_EMAIL         = "EMail";
	public static final String  P_PHONE         = "Phone";
	public static final String  P_QUESTION      = "Question";

	/**
	 *  Process the HTTP Get request
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

		WDoc doc = WDoc.create ("Web Request");

		//  Form posting to same servlet
		form form = null;
		form = new form (request.getRequestURI(), form.post, form.ENC_DEFAULT).setName("Request");
		form.addElement(new input (input.hidden, P_FORWARDTO, "http://www.compiere.org"));
		form.addElement(new input (input.hidden, P_SOURCE, ""));
		form.addElement(new input (input.hidden, P_INFO, ""));
		form.addElement(new input (input.hidden, P_CLIENT, String.valueOf(m_AD_Client_ID)));
		String script = "document.Request." + P_SOURCE + ".value=document.referrer; "
			+ "document.Request." + P_INFO + ".value=document.lastModified;";
		form.addElement(new script().addElement(script));

		table table = new table().setAlign(AlignType.center);
		//  RequestType
		tr line = new tr();
		label label = new label().setFor(P_REQUESTTYPE).addElement("Request Type");
		label.setID("ID_"+P_REQUESTTYPE);
		line.addElement(new td().addElement(label).setAlign(AlignType.right));
		option options[] = getRequestTypes();
		line.addElement(new td().addElement(new select(P_REQUESTTYPE, options).setID("ID_"+P_REQUESTTYPE)));
		table.addElement(line);
		//  Name
		line = new tr();
		label = new label().setFor(P_NAME).addElement("Name");
		label.setID("ID_"+P_NAME);
		line.addElement(new td().addElement(label).setAlign(AlignType.right));
		input input = null;
		input = new input(input.text, P_NAME, "").setSize(30).setMaxlength(60);
		input.setID("ID_"+P_NAME);
		line.addElement(new td().addElement(input).setAlign(AlignType.left));
		table.addElement(line);
		//  Company
		line = new tr();
		label = new label().setFor(P_COMPANY).addElement("Company");
		label.setID("ID_"+P_COMPANY);
		line.addElement(new td().addElement(label).setAlign(AlignType.right));
		input = new input(input.text, P_COMPANY, "").setSize(30).setMaxlength(60);
		input.setID("ID_"+P_COMPANY);
		line.addElement(new td().addElement(input).setAlign(AlignType.left));
		table.addElement(line);
		//  Address
		line = new tr();
		label = new label().setFor(P_ADDRESS).addElement("Address");
		label.setID("ID_"+P_ADDRESS);
		line.addElement(new td().addElement(label).setAlign(AlignType.right));
		input = new input(input.text, P_ADDRESS, "").setSize(30).setMaxlength(60);
		input.setID("ID_"+P_ADDRESS);
		line.addElement(new td().addElement(input).setAlign(AlignType.left));
		table.addElement(line);
		//  City
		line = new tr();
		label = new label().setFor(P_CITY).addElement("City");
		label.setID("ID_"+P_CITY);
		line.addElement(new td().addElement(label).setAlign(AlignType.right));
		input = new input(input.text, P_CITY, "").setSize(30).setMaxlength(60);
		input.setID("ID_"+P_CITY);
		line.addElement(new td().addElement(input).setAlign(AlignType.left));
		table.addElement(line);
		//  Postal
		line = new tr();
		label = new label().setFor(P_POSTAL).addElement("Zip");
		label.setID("ID_"+P_POSTAL);
		line.addElement(new td().addElement(label).setAlign(AlignType.right));
		input = new input(input.text, P_POSTAL, "").setSize(10).setMaxlength(20);
		input.setID("ID_"+P_POSTAL);
		line.addElement(new td().addElement(input).setAlign(AlignType.left));
		table.addElement(line);
		//  Region
		line = new tr();
		label = new label().setFor(P_REGION).addElement("State");
		label.setID("ID_"+P_REGION);
		line.addElement(new td().addElement(label).setAlign(AlignType.right));
		input = new input(input.text, P_REGION, "").setSize(20).setMaxlength(20);
		input.setID("ID_"+P_REGION);
		line.addElement(new td().addElement(input).setAlign(AlignType.left));
		table.addElement(line);
		//  Country
		line = new tr();
		label = new label().setFor(P_COUNTRY).addElement("Country");
		label.setID("ID_"+P_COUNTRY);
		line.addElement(new td().addElement(label).setAlign(AlignType.right));
		input = new input(input.text, P_COUNTRY, "").setSize(20).setMaxlength(20);
		input.setID("ID_"+P_COUNTRY);
		line.addElement(new td().addElement(input).setAlign(AlignType.left));
		table.addElement(line);
		//  EMail
		line = new tr();
		label = new label().setFor(P_EMAIL).addElement("EMail");
		label.setID("ID_"+P_EMAIL);
		line.addElement(new td().addElement(label).setAlign(AlignType.right));
		input = new input(input.text, P_EMAIL, "").setSize(30).setMaxlength(60);
		input.setID("ID_"+P_EMAIL);
		line.addElement(new td().addElement(input).setAlign(AlignType.left));
		table.addElement(line);
		//  Phone
		line = new tr();
		label = new label().setFor(P_PHONE).addElement("Phone");
		label.setID("ID_"+P_PHONE);
		line.addElement(new td().addElement(label).setAlign(AlignType.right));
		input = new input(input.text, P_PHONE, "").setSize(20).setMaxlength(20);
		input.setID("ID_"+P_PHONE);
		line.addElement(new td().addElement(input).setAlign(AlignType.left));
		table.addElement(line);
		//  Question
		line = new tr();
		label = new label().setFor(P_QUESTION).addElement("Question, Request");
		label.setID("ID_"+P_QUESTION);
		line.addElement(new td().addElement(label).setAlign(AlignType.right));
		table.addElement(line);
		line = new tr();
		textarea text = new textarea(P_QUESTION, 6, 40).addElement("");
		text.setID("ID_" + P_QUESTION);
//		text.setClass(C_MANDATORY);
		line.addElement(new td().addElement(text).setAlign(AlignType.left).setColSpan(2));
		table.addElement(line);
		//  Submit
		line = new tr();
		line.addElement(new td().addElement(new input(input.reset, "Reset", "Clear") ));
		line.addElement(new td().addElement(new input(input.submit, "Submit", "Send")).setAlign(AlignType.right));
		table.addElement(line);
		//
		form.addElement(table);
		doc.getBody().addElement(form);
		//
		WUtil.createResponse(request, response, this, null, doc, true);
	}   //  doGet

	/**
	 *  Get Request Types
	 *  @return Options with Request Types
	 */
	private option[] getRequestTypes()
	{
		ArrayList list = new ArrayList();
		String sql = "SELECT R_RequestType_ID, Name, IsDefault "
			+ "FROM R_RequestType WHERE AD_Client_ID=? ORDER BY Name";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, m_AD_Client_ID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				String id = rs.getString(1);
				String name = rs.getString(2);
				boolean isDefault = "Y".equals(rs.getString(3));
				//
				option o = new option(id).addElement(name);
				if (isDefault)
					o.setSelected(true);
				list.add(o);
			}
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			log.error("getRequestType", e);
		}
		option[] op = new option[list.size()];
		list.toArray(op);
		return op;
	}   //  getRequestType


	/*************************************************************************/

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

		//  Get Session attributes
		HttpSession sess = request.getSession(false);
		if (sess != null)
		{
			Properties ctx = (Properties)sess.getAttribute(WEnv.SA_CONTEXT);
			Properties cProp = WUtil.getCookieProprties(request);
		}

		StringBuffer sql = new StringBuffer ("INSERT INTO W_Request "
			+ "(W_Request_ID,AD_Client_ID,AD_Org_ID, "
			+ "IsActive,Created,CreatedBy,Updated,UpdatedBy, "
			+ "R_RequestType_ID,Question, "
			+ "Name,Company, "
			+ "Address,City,Postal,Region,Country, "
			+ "EMail,Phone, "
			+ "PageURL,Referrer, "
			+ "WebUserID,AcceptLanguage, "
			+ "Remote_Addr,UserAgent, "
			+ "FindBPartner,Processing,Processed) VALUES (");
		//
		//  W_Request_ID,AD_Client_ID,AD_Org_ID,
		int AD_Client_ID = m_AD_Client_ID;
		try
		{
			String s = request.getParameter(P_CLIENT);
			if (s != null && s.length() > 0)
				AD_Client_ID = Integer.parseInt(s);
		}
		catch (NumberFormatException ex)
		{
		}
		int W_Request_ID = DB.getKeyNextNo(AD_Client_ID, "N", "W_Request");
		sql.append(W_Request_ID).append(",").append(AD_Client_ID).append(",0, ");

		//  IsActive,Created,CreatedBy,Updated,UpdatedBy,
		sql.append("'Y',SysDate,0,SysDate,0, ");

		//  RequestType,Question,
		String RequestType = request.getParameter(P_REQUESTTYPE);
		if (RequestType == null)
			RequestType = "null";
		sql.append(RequestType).append(",")
			.append(DB.TO_STRING(request.getParameter(P_QUESTION), 2000)).append(", ");

		//  Name,Company,
		sql.append(DB.TO_STRING(request.getParameter(P_NAME), 60)).append(",")
			.append(DB.TO_STRING(request.getParameter(P_COMPANY), 60)).append(", ");

		//  Address,City,Postal,Region,Country,
		sql.append(DB.TO_STRING(request.getParameter(P_ADDRESS), 60)).append(",")
			.append(DB.TO_STRING(request.getParameter(P_CITY), 60)).append(",")
			.append(DB.TO_STRING(request.getParameter(P_POSTAL), 20)).append(",")
			.append(DB.TO_STRING(request.getParameter(P_REGION), 20)).append(",")
			.append(DB.TO_STRING(request.getParameter(P_COUNTRY), 20)).append(", ");

		//  EMail,Phone,
		sql.append(DB.TO_STRING(request.getParameter(P_EMAIL), 60)).append(",")
			.append(DB.TO_STRING(request.getParameter(P_PHONE), 20)).append(", ");

		//  PageURL,Referrer,
		String requestURL = request.getRequestURL().toString();
		String requestRef = request.getHeader("referer");
		String source = request.getParameter(P_SOURCE);
		String info = request.getParameter(P_INFO);
		if (requestURL == null)
			requestURL = "";
		if (requestURL.equals(requestRef))      //  if URL and Referrer are the same, get source
		{
			requestRef = source;
			source = null;
		}
		sql.append(DB.TO_STRING(requestURL, 255)).append(",")
			.append(DB.TO_STRING(requestRef, 255)).append(", ");

		//  WebUserID,AcceptLanguage,
		String web = source;
		if (web == null)
			web = info;
		else if (info != null)
			web += " - " + info;
		sql.append(DB.TO_STRING(web, 60)).append(",")
			.append(DB.TO_STRING(request.getHeader("accept-language"), 60)).append(", ");

		//  Remote_Addr,UserAgent,
		sql.append(DB.TO_STRING(request.getRemoteAddr(), 60)).append(",")
			.append(DB.TO_STRING(request.getHeader("user-agent"), 60)).append(", ");

		//  FindBPartner,Processing,Processed)
		sql.append("'N','N','N')");
		int no = DB.executeUpdate(sql.toString());
		if (no != 1)
		{
			log.error("post - Request NOT saved - #=" + no);
			WUtil.createErrorPage(request, response, this, Env.getCtx(), "Error");		//	Env??
			return;
		}

		//  --  Fini
		WDoc doc = WDoc.create ("Web Request Received");
		String ForwardTo = request.getParameter(P_FORWARDTO);
		if (ForwardTo == null || ForwardTo.length() == 0)
			ForwardTo = "http://www.compiere.org";

		script script = new script("window.top.location.replace('" + ForwardTo + "');");
		doc.getBody().addElement(script);
		doc.getBody().addElement(new a (ForwardTo, "Thanks"));
		//
		WUtil.createResponse(request, response, this, null, doc, false);
	}   //  doPost

}   //  Request
