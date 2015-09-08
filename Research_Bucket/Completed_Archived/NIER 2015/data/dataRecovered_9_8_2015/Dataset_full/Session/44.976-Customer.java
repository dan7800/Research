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
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import java.sql.*;
import javax.sql.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.apache.log4j.Logger;

import org.compiere.www.*;
import org.compiere.interfaces.*;

/**
 *  Web Store Customer Maintenance
 *  <pre>
 *  - User is asked for email address and password or to fill out the customer info
 *  - if email & password is OK, information is displayed to confirm
 *  </pre>
 *  @author Jorg Janke
 *  @version  $Id: Customer.java,v 1.5 2002/11/11 07:03:36 jjanke Exp $
 */
public class Customer extends HttpServlet
{
	/**	Logging						*/
	private Logger			log = Logger.getLogger(getClass());
	/**	Client						*/
	private int				m_AD_Client_ID = -1;

	/**
	 * 	Initialize global variables
	 *  @param config servlet configuration
	 *  @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		if (!WEnv.initWeb(config))
			throw new ServletException("Customer.init");
		//	Get Client
		m_AD_Client_ID = WEnv.getAD_Client_ID (config);
		String dbUID = WEnv.getDB_UID (config);
		String dbPWD = WEnv.getDB_PWD (config);
		log.info("init - AD_Client_ID=" + m_AD_Client_ID + ", DB=" + dbUID + "/" + dbPWD);
		//
		try
		{
			Context context = new InitialContext();
			DataSource ds = (DataSource)context.lookup("java:OracleDS");
			log.info ("OracleDS=" + ds.toString());
			Connection con = ds.getConnection(dbUID, dbPWD);
			log.info("Connection AutoCommit=" + con.getAutoCommit());
			//
			Object obj = context.lookup(BPartnerHome.JNDI_NAME);
			log.info("Obj=" + obj + " - " + obj.getClass());

			BPartnerHome bpHome = (BPartnerHome)obj;
		//	BPartnerHome bpHome = (BPartnerHome)PortableRemoteObject.narrow(obj, BPartnerHome.class);

			BPartner bp = bpHome.create();
			log.info("BPartner 2=" + bp.process2());
			log.info("BPartner 1=" + bp.process());
			bp.remove();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		//	throw new ServletException (ex);
		}
	}	//	init

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.info("destroy");
	}   //  destroy


	private static final String FORMNAME =      "customer";
	//  Parameter Names
	private static final String P_EMAIL =       "email";
	private static final String P_PASSWORD =    "password";
	private static final String P_LOOKUP =      "lookup";
	//
	private static final String P_COMPANY =     "company";
	private static final String P_NAME =        "name";
	private static final String P_ADDRESS1 =    "address1";
	private static final String P_ADDRESS2 =    "address2";
	private static final String P_CITY =        "city";
	private static final String P_ZIP =         "zip";
	private static final String P_STATE =       "state";
	private static final String P_COUNTRY =     "country";
	private static final String P_PHONE =       "phone";
	//
	private static final String P_SUBMIT =      "SUBMIT";
	private static final String P_MESSAGE =     "MESSAGE";


	/**
	 *  Process the initial HTTP Get request
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		doPost (request, response);
	}   //  doGet


	/**
	 *  Process the HTTP Post request
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		log.info("do from " + request.getRemoteHost() + " - " + request.getRemoteAddr());

		//  Get Session
		HttpSession sess = request.getSession (true);
		//  Get context
		Properties ctx = (Properties)sess.getAttribute(WEnv.SA_CONTEXT);
		//  Get Cookie
		Properties cProp = WUtil.getCookieProprties(request);

		//	Get Data
		Properties data = new Properties();
		data.setProperty(P_COUNTRY, "US");		//	Default
		Enumeration en = request.getParameterNames();
		while (en.hasMoreElements())
		{
			String name = (String)en.nextElement();
			data.setProperty (name, request.getParameter(name));
		}

		//	Lookup Pressed
		if (request.getParameter(P_LOOKUP) != null)
		{
			boolean ok = lookupCustomer (data);
			String email = data.getProperty(P_EMAIL);
			if (email != null)
				cProp.setProperty(P_EMAIL, email);
		}
		//	Submit Pressed
		else if (request.getParameter(P_SUBMIT) != null)
		{
			boolean ok = createCustomer (data);
			String email = data.getProperty(P_EMAIL);
			if (email != null)
				cProp.setProperty(P_EMAIL, email);

			//	Next Page
			if (ok)
			{

			}
		}
		//	Initial request
		else
		{
			data.setProperty (P_EMAIL, cProp.getProperty(P_EMAIL,""));
		}

		//	Create Page
		WDoc doc = createPage (request, data);
		WUtil.createResponse(request, response, this, cProp, doc, true);
	}   //  doPost

	/*************************************************************************/

	/**
	 *  Create Page
	 *  @param request request
	 *  @param data initial data
	 *  @return  Document
	 */
	private WDoc createPage (HttpServletRequest request, Properties data)
	{
		String title 		= "Enter Customer Information";
		String emailText 	= "Enter your email address";
		String passwordText = "Password";
		String lookupText 	= "Lookup Account";
		String choiceText 	= "or enter details below";
		String companyText 	= "Company";
		String nameText 	= "Name (as on credit card)";
		String address1Text = "Street (as on credit card)";
		String address2Text = "Suite/PO Box/Department";
		String cityText 	= "City";
		String zipText 		= "Zip or Postal Code";
		String stateText 	= "State or Province";
		String countryText 	= "Country";
		String phoneText 	= "Phone";
		//
		String cancelText 	= "Reset";
		String okText 		= "Submit Information";
		//
		StringBuffer script = new StringBuffer();
		script.append("mandatory='Enter mandatory information:'; ");

		WDoc doc = WDoc.create (title);
		head head = doc.getHead();
		head.addElement(new script("", WEnv.getBaseDirectory("wstore.js"), "text/javascript", "JavaScript1.2"));
		body body = doc.getBody();

		//  Post to same servlet
		form form = null;
		form = new form(request.getRequestURI(), form.post, form.ENC_DEFAULT).setName(FORMNAME);
		form.setAcceptCharset(WEnv.CHARACTERSET);

		//	Hidden (pass-through) Fields
		form.addElement(new input(input.hidden, "myHideKey", "myHideValue"));
		Iterator it = data.keySet().iterator();
		while (it.hasNext())
		{
			String key = (String)it.next();
			if (key.startsWith("HIDE"))
			{
				String value = data.getProperty(key);
				form.addElement(new input(input.hidden, key, value));
			}
		}	//	for all data iteems


		//	Table
		table table = new table().setAlign(AlignType.CENTER);
		form.addElement(table);
		body.addElement(form);
		tr line = null;

		//	Message (optional)
		String message = data.getProperty(P_MESSAGE);
		if (message != null)
		{
			td ttd = new td().setColSpan(4);
			ttd.addElement(new p().addElement(new strong().addElement(message)));
			table.addElement(new tr().addElement(ttd));
		}

		//  EMail & Password
		line = WUtil.createField (null, FORMNAME, P_EMAIL, emailText, input.text, data.getProperty(P_EMAIL), 20, 40, false, true, null, script);
		table.addElement(WUtil.createField (line, FORMNAME, P_PASSWORD, passwordText, input.password, "", 10, 20, false, true, null, script));

		//  Choice
		line = new tr();
		input lookup = new input(input.submit, P_LOOKUP, lookupText);
		lookup.setOnClick("return checkLookup(this);");
		line.addElement(new td().addElement(lookup ));
		line.addElement(new td().addElement(choiceText));
		table.addElement(line);

		//  Company
		table.addElement(WUtil.createField (null, FORMNAME, P_COMPANY, companyText, input.text, data.getProperty(P_COMPANY), 40, 60, true, false, null, script));
		//  Name
		table.addElement(WUtil.createField (null, FORMNAME, P_NAME, nameText, input.text, data.getProperty(P_NAME,""), 40, 60, true, true, null, script));
		//  Address /2
		table.addElement(WUtil.createField (null, FORMNAME, P_ADDRESS1, address1Text, input.text, data.getProperty(P_ADDRESS1), 40, 60, true, true, null, script));
		table.addElement(WUtil.createField (null, FORMNAME, P_ADDRESS2, address2Text, input.text, data.getProperty(P_ADDRESS2), 40, 60, true, true, null, script));
		//  City    ZIP
		line = WUtil.createField (null, FORMNAME, P_CITY, cityText, input.text, data.getProperty(P_CITY), 20, 40, false, true, null, script);
		table.addElement(WUtil.createField (line, FORMNAME, P_ZIP, zipText, input.text, data.getProperty(P_ZIP), 12, 20, false, true, null, script));
		//  State   Country
		line = WUtil.createField (null, FORMNAME, P_STATE, stateText, input.text, data.getProperty(P_STATE), 10, 20, false, true, null, script);
		table.addElement(WUtil.createField (line, FORMNAME, P_COUNTRY, countryText, input.text, data.getProperty(P_COUNTRY), 20, 40, false, true, null, script));
		//  Phone
		table.addElement(WUtil.createField (null, FORMNAME, P_PHONE, phoneText, input.text, data.getProperty(P_PHONE), 20, 60, true, true, null, script));

		//  Submit
		line = new tr();
		input cancel = new input(input.reset, "Reset", cancelText);
		line.addElement(new td().addElement(cancel ));
		input submit = new input(input.submit, P_SUBMIT, okText);
		submit.setOnClick("return checkForm(this);");
		line.addElement(new td().addElement(submit ));
		table.addElement(line);
		//
		body.addElement(new script(script.toString()));

		return doc;
	}   //  createPage


	/*************************************************************************/

	/**
	 * 	Lookup customer.
	 *  Response Messages:
	 *  - EMail not found
	 *  - Password not correct
	 *  - Welcome back
	 * 	@param data data to be filled
	 * 	@return true if OK
	 */
	private boolean lookupCustomer (Properties data)
	{
		data.setProperty (P_MESSAGE, "Welcome back ...");
		return true;
	}	//	lookupCustomer

	/**
	 * 	Create or update customer.
	 *  Response Messages:
	 *  - EMail already used
	 *  - Account created
	 *  - Account updated
	 * 	@param data data
	 * 	@return true if OK
	 */
	private boolean createCustomer (Properties data)
	{
		return true;
	}	//	createCustomer

}   //  Customer
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
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import java.sql.*;
import javax.sql.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.apache.log4j.Logger;

import org.compiere.www.*;
import org.compiere.interfaces.*;

/**
 *  Web Store Customer Maintenance
 *  <pre>
 *  - User is asked for email address and password or to fill out the customer info
 *  - if email & password is OK, information is displayed to confirm
 *  </pre>
 *  @author Jorg Janke
 *  @version  $Id: Customer.java,v 1.5 2002/11/11 07:03:36 jjanke Exp $
 */
public class Customer extends HttpServlet
{
	/**	Logging						*/
	private Logger			log = Logger.getLogger(getClass());
	/**	Client						*/
	private int				m_AD_Client_ID = -1;

	/**
	 * 	Initialize global variables
	 *  @param config servlet configuration
	 *  @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		if (!WEnv.initWeb(config))
			throw new ServletException("Customer.init");
		//	Get Client
		m_AD_Client_ID = WEnv.getAD_Client_ID (config);
		String dbUID = WEnv.getDB_UID (config);
		String dbPWD = WEnv.getDB_PWD (config);
		log.info("init - AD_Client_ID=" + m_AD_Client_ID + ", DB=" + dbUID + "/" + dbPWD);
		//
		try
		{
			Context context = new InitialContext();
			DataSource ds = (DataSource)context.lookup("java:OracleDS");
			log.info ("OracleDS=" + ds.toString());
			Connection con = ds.getConnection(dbUID, dbPWD);
			log.info("Connection AutoCommit=" + con.getAutoCommit());
			//
			Object obj = context.lookup(BPartnerHome.JNDI_NAME);
			log.info("Obj=" + obj + " - " + obj.getClass());

			BPartnerHome bpHome = (BPartnerHome)obj;
		//	BPartnerHome bpHome = (BPartnerHome)PortableRemoteObject.narrow(obj, BPartnerHome.class);

			BPartner bp = bpHome.create();
			log.info("BPartner 2=" + bp.process2());
			log.info("BPartner 1=" + bp.process());
			bp.remove();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		//	throw new ServletException (ex);
		}
	}	//	init

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.info("destroy");
	}   //  destroy


	private static final String FORMNAME =      "customer";
	//  Parameter Names
	private static final String P_EMAIL =       "email";
	private static final String P_PASSWORD =    "password";
	private static final String P_LOOKUP =      "lookup";
	//
	private static final String P_COMPANY =     "company";
	private static final String P_NAME =        "name";
	private static final String P_ADDRESS1 =    "address1";
	private static final String P_ADDRESS2 =    "address2";
	private static final String P_CITY =        "city";
	private static final String P_ZIP =         "zip";
	private static final String P_STATE =       "state";
	private static final String P_COUNTRY =     "country";
	private static final String P_PHONE =       "phone";
	//
	private static final String P_SUBMIT =      "SUBMIT";
	private static final String P_MESSAGE =     "MESSAGE";


	/**
	 *  Process the initial HTTP Get request
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		doPost (request, response);
	}   //  doGet


	/**
	 *  Process the HTTP Post request
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		log.info("do from " + request.getRemoteHost() + " - " + request.getRemoteAddr());

		//  Get Session
		HttpSession sess = request.getSession (true);
		//  Get context
		Properties ctx = (Properties)sess.getAttribute(WEnv.SA_CONTEXT);
		//  Get Cookie
		Properties cProp = WUtil.getCookieProprties(request);

		//	Get Data
		Properties data = new Properties();
		data.setProperty(P_COUNTRY, "US");		//	Default
		Enumeration en = request.getParameterNames();
		while (en.hasMoreElements())
		{
			String name = (String)en.nextElement();
			data.setProperty (name, request.getParameter(name));
		}

		//	Lookup Pressed
		if (request.getParameter(P_LOOKUP) != null)
		{
			boolean ok = lookupCustomer (data);
			String email = data.getProperty(P_EMAIL);
			if (email != null)
				cProp.setProperty(P_EMAIL, email);
		}
		//	Submit Pressed
		else if (request.getParameter(P_SUBMIT) != null)
		{
			boolean ok = createCustomer (data);
			String email = data.getProperty(P_EMAIL);
			if (email != null)
				cProp.setProperty(P_EMAIL, email);

			//	Next Page
			if (ok)
			{

			}
		}
		//	Initial request
		else
		{
			data.setProperty (P_EMAIL, cProp.getProperty(P_EMAIL,""));
		}

		//	Create Page
		WDoc doc = createPage (request, data);
		WUtil.createResponse(request, response, this, cProp, doc, true);
	}   //  doPost

	/*************************************************************************/

	/**
	 *  Create Page
	 *  @param request request
	 *  @param data initial data
	 *  @return  Document
	 */
	private WDoc createPage (HttpServletRequest request, Properties data)
	{
		String title 		= "Enter Customer Information";
		String emailText 	= "Enter your email address";
		String passwordText = "Password";
		String lookupText 	= "Lookup Account";
		String choiceText 	= "or enter details below";
		String companyText 	= "Company";
		String nameText 	= "Name (as on credit card)";
		String address1Text = "Street (as on credit card)";
		String address2Text = "Suite/PO Box/Department";
		String cityText 	= "City";
		String zipText 		= "Zip or Postal Code";
		String stateText 	= "State or Province";
		String countryText 	= "Country";
		String phoneText 	= "Phone";
		//
		String cancelText 	= "Reset";
		String okText 		= "Submit Information";
		//
		StringBuffer script = new StringBuffer();
		script.append("mandatory='Enter mandatory information:'; ");

		WDoc doc = WDoc.create (title);
		head head = doc.getHead();
		head.addElement(new script("", WEnv.getBaseDirectory("wstore.js"), "text/javascript", "JavaScript1.2"));
		body body = doc.getBody();

		//  Post to same servlet
		form form = null;
		form = new form(request.getRequestURI(), form.post, form.ENC_DEFAULT).setName(FORMNAME);
		form.setAcceptCharset(WEnv.CHARACTERSET);

		//	Hidden (pass-through) Fields
		form.addElement(new input(input.hidden, "myHideKey", "myHideValue"));
		Iterator it = data.keySet().iterator();
		while (it.hasNext())
		{
			String key = (String)it.next();
			if (key.startsWith("HIDE"))
			{
				String value = data.getProperty(key);
				form.addElement(new input(input.hidden, key, value));
			}
		}	//	for all data iteems


		//	Table
		table table = new table().setAlign(AlignType.CENTER);
		form.addElement(table);
		body.addElement(form);
		tr line = null;

		//	Message (optional)
		String message = data.getProperty(P_MESSAGE);
		if (message != null)
		{
			td ttd = new td().setColSpan(4);
			ttd.addElement(new p().addElement(new strong().addElement(message)));
			table.addElement(new tr().addElement(ttd));
		}

		//  EMail & Password
		line = WUtil.createField (null, FORMNAME, P_EMAIL, emailText, input.text, data.getProperty(P_EMAIL), 20, 40, false, true, null, script);
		table.addElement(WUtil.createField (line, FORMNAME, P_PASSWORD, passwordText, input.password, "", 10, 20, false, true, null, script));

		//  Choice
		line = new tr();
		input lookup = new input(input.submit, P_LOOKUP, lookupText);
		lookup.setOnClick("return checkLookup(this);");
		line.addElement(new td().addElement(lookup ));
		line.addElement(new td().addElement(choiceText));
		table.addElement(line);

		//  Company
		table.addElement(WUtil.createField (null, FORMNAME, P_COMPANY, companyText, input.text, data.getProperty(P_COMPANY), 40, 60, true, false, null, script));
		//  Name
		table.addElement(WUtil.createField (null, FORMNAME, P_NAME, nameText, input.text, data.getProperty(P_NAME,""), 40, 60, true, true, null, script));
		//  Address /2
		table.addElement(WUtil.createField (null, FORMNAME, P_ADDRESS1, address1Text, input.text, data.getProperty(P_ADDRESS1), 40, 60, true, true, null, script));
		table.addElement(WUtil.createField (null, FORMNAME, P_ADDRESS2, address2Text, input.text, data.getProperty(P_ADDRESS2), 40, 60, true, true, null, script));
		//  City    ZIP
		line = WUtil.createField (null, FORMNAME, P_CITY, cityText, input.text, data.getProperty(P_CITY), 20, 40, false, true, null, script);
		table.addElement(WUtil.createField (line, FORMNAME, P_ZIP, zipText, input.text, data.getProperty(P_ZIP), 12, 20, false, true, null, script));
		//  State   Country
		line = WUtil.createField (null, FORMNAME, P_STATE, stateText, input.text, data.getProperty(P_STATE), 10, 20, false, true, null, script);
		table.addElement(WUtil.createField (line, FORMNAME, P_COUNTRY, countryText, input.text, data.getProperty(P_COUNTRY), 20, 40, false, true, null, script));
		//  Phone
		table.addElement(WUtil.createField (null, FORMNAME, P_PHONE, phoneText, input.text, data.getProperty(P_PHONE), 20, 60, true, true, null, script));

		//  Submit
		line = new tr();
		input cancel = new input(input.reset, "Reset", cancelText);
		line.addElement(new td().addElement(cancel ));
		input submit = new input(input.submit, P_SUBMIT, okText);
		submit.setOnClick("return checkForm(this);");
		line.addElement(new td().addElement(submit ));
		table.addElement(line);
		//
		body.addElement(new script(script.toString()));

		return doc;
	}   //  createPage


	/*************************************************************************/

	/**
	 * 	Lookup customer.
	 *  Response Messages:
	 *  - EMail not found
	 *  - Password not correct
	 *  - Welcome back
	 * 	@param data data to be filled
	 * 	@return true if OK
	 */
	private boolean lookupCustomer (Properties data)
	{
		data.setProperty (P_MESSAGE, "Welcome back ...");
		return true;
	}	//	lookupCustomer

	/**
	 * 	Create or update customer.
	 *  Response Messages:
	 *  - EMail already used
	 *  - Account created
	 *  - Account updated
	 * 	@param data data
	 * 	@return true if OK
	 */
	private boolean createCustomer (Properties data)
	{
		return true;
	}	//	createCustomer

}   //  Customer
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
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import java.sql.*;
import javax.sql.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.apache.log4j.Logger;

import org.compiere.www.*;
import org.compiere.interfaces.*;

/**
 *  Web Store Customer Maintenance
 *  <pre>
 *  - User is asked for email address and password or to fill out the customer info
 *  - if email & password is OK, information is displayed to confirm
 *  </pre>
 *  @author Jorg Janke
 *  @version  $Id: Customer.java,v 1.5 2002/11/11 07:03:36 jjanke Exp $
 */
public class Customer extends HttpServlet
{
	/**	Logging						*/
	private Logger			log = Logger.getLogger(getClass());
	/**	Client						*/
	private int				m_AD_Client_ID = -1;

	/**
	 * 	Initialize global variables
	 *  @param config servlet configuration
	 *  @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		if (!WEnv.initWeb(config))
			throw new ServletException("Customer.init");
		//	Get Client
		m_AD_Client_ID = WEnv.getAD_Client_ID (config);
		String dbUID = WEnv.getDB_UID (config);
		String dbPWD = WEnv.getDB_PWD (config);
		log.info("init - AD_Client_ID=" + m_AD_Client_ID + ", DB=" + dbUID + "/" + dbPWD);
		//
		try
		{
			Context context = new InitialContext();
			DataSource ds = (DataSource)context.lookup("java:OracleDS");
			log.info ("OracleDS=" + ds.toString());
			Connection con = ds.getConnection(dbUID, dbPWD);
			log.info("Connection AutoCommit=" + con.getAutoCommit());
			//
			Object obj = context.lookup(BPartnerHome.JNDI_NAME);
			log.info("Obj=" + obj + " - " + obj.getClass());

			BPartnerHome bpHome = (BPartnerHome)obj;
		//	BPartnerHome bpHome = (BPartnerHome)PortableRemoteObject.narrow(obj, BPartnerHome.class);

			BPartner bp = bpHome.create();
			log.info("BPartner 2=" + bp.process2());
			log.info("BPartner 1=" + bp.process());
			bp.remove();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		//	throw new ServletException (ex);
		}
	}	//	init

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.info("destroy");
	}   //  destroy


	private static final String FORMNAME =      "customer";
	//  Parameter Names
	private static final String P_EMAIL =       "email";
	private static final String P_PASSWORD =    "password";
	private static final String P_LOOKUP =      "lookup";
	//
	private static final String P_COMPANY =     "company";
	private static final String P_NAME =        "name";
	private static final String P_ADDRESS1 =    "address1";
	private static final String P_ADDRESS2 =    "address2";
	private static final String P_CITY =        "city";
	private static final String P_ZIP =         "zip";
	private static final String P_STATE =       "state";
	private static final String P_COUNTRY =     "country";
	private static final String P_PHONE =       "phone";
	//
	private static final String P_SUBMIT =      "SUBMIT";
	private static final String P_MESSAGE =     "MESSAGE";


	/**
	 *  Process the initial HTTP Get request
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		doPost (request, response);
	}   //  doGet


	/**
	 *  Process the HTTP Post request
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		log.info("do from " + request.getRemoteHost() + " - " + request.getRemoteAddr());

		//  Get Session
		HttpSession sess = request.getSession (true);
		//  Get context
		Properties ctx = (Properties)sess.getAttribute(WEnv.SA_CONTEXT);
		//  Get Cookie
		Properties cProp = WUtil.getCookieProprties(request);

		//	Get Data
		Properties data = new Properties();
		data.setProperty(P_COUNTRY, "US");		//	Default
		Enumeration en = request.getParameterNames();
		while (en.hasMoreElements())
		{
			String name = (String)en.nextElement();
			data.setProperty (name, request.getParameter(name));
		}

		//	Lookup Pressed
		if (request.getParameter(P_LOOKUP) != null)
		{
			boolean ok = lookupCustomer (data);
			String email = data.getProperty(P_EMAIL);
			if (email != null)
				cProp.setProperty(P_EMAIL, email);
		}
		//	Submit Pressed
		else if (request.getParameter(P_SUBMIT) != null)
		{
			boolean ok = createCustomer (data);
			String email = data.getProperty(P_EMAIL);
			if (email != null)
				cProp.setProperty(P_EMAIL, email);

			//	Next Page
			if (ok)
			{

			}
		}
		//	Initial request
		else
		{
			data.setProperty (P_EMAIL, cProp.getProperty(P_EMAIL,""));
		}

		//	Create Page
		WDoc doc = createPage (request, data);
		WUtil.createResponse(request, response, this, cProp, doc, true);
	}   //  doPost

	/*************************************************************************/

	/**
	 *  Create Page
	 *  @param request request
	 *  @param data initial data
	 *  @return  Document
	 */
	private WDoc createPage (HttpServletRequest request, Properties data)
	{
		String title 		= "Enter Customer Information";
		String emailText 	= "Enter your email address";
		String passwordText = "Password";
		String lookupText 	= "Lookup Account";
		String choiceText 	= "or enter details below";
		String companyText 	= "Company";
		String nameText 	= "Name (as on credit card)";
		String address1Text = "Street (as on credit card)";
		String address2Text = "Suite/PO Box/Department";
		String cityText 	= "City";
		String zipText 		= "Zip or Postal Code";
		String stateText 	= "State or Province";
		String countryText 	= "Country";
		String phoneText 	= "Phone";
		//
		String cancelText 	= "Reset";
		String okText 		= "Submit Information";
		//
		StringBuffer script = new StringBuffer();
		script.append("mandatory='Enter mandatory information:'; ");

		WDoc doc = WDoc.create (title);
		head head = doc.getHead();
		head.addElement(new script("", WEnv.getBaseDirectory("wstore.js"), "text/javascript", "JavaScript1.2"));
		body body = doc.getBody();

		//  Post to same servlet
		form form = null;
		form = new form(request.getRequestURI(), form.post, form.ENC_DEFAULT).setName(FORMNAME);
		form.setAcceptCharset(WEnv.CHARACTERSET);

		//	Hidden (pass-through) Fields
		form.addElement(new input(input.hidden, "myHideKey", "myHideValue"));
		Iterator it = data.keySet().iterator();
		while (it.hasNext())
		{
			String key = (String)it.next();
			if (key.startsWith("HIDE"))
			{
				String value = data.getProperty(key);
				form.addElement(new input(input.hidden, key, value));
			}
		}	//	for all data iteems


		//	Table
		table table = new table().setAlign(AlignType.CENTER);
		form.addElement(table);
		body.addElement(form);
		tr line = null;

		//	Message (optional)
		String message = data.getProperty(P_MESSAGE);
		if (message != null)
		{
			td ttd = new td().setColSpan(4);
			ttd.addElement(new p().addElement(new strong().addElement(message)));
			table.addElement(new tr().addElement(ttd));
		}

		//  EMail & Password
		line = WUtil.createField (null, FORMNAME, P_EMAIL, emailText, input.text, data.getProperty(P_EMAIL), 20, 40, false, true, null, script);
		table.addElement(WUtil.createField (line, FORMNAME, P_PASSWORD, passwordText, input.password, "", 10, 20, false, true, null, script));

		//  Choice
		line = new tr();
		input lookup = new input(input.submit, P_LOOKUP, lookupText);
		lookup.setOnClick("return checkLookup(this);");
		line.addElement(new td().addElement(lookup ));
		line.addElement(new td().addElement(choiceText));
		table.addElement(line);

		//  Company
		table.addElement(WUtil.createField (null, FORMNAME, P_COMPANY, companyText, input.text, data.getProperty(P_COMPANY), 40, 60, true, false, null, script));
		//  Name
		table.addElement(WUtil.createField (null, FORMNAME, P_NAME, nameText, input.text, data.getProperty(P_NAME,""), 40, 60, true, true, null, script));
		//  Address /2
		table.addElement(WUtil.createField (null, FORMNAME, P_ADDRESS1, address1Text, input.text, data.getProperty(P_ADDRESS1), 40, 60, true, true, null, script));
		table.addElement(WUtil.createField (null, FORMNAME, P_ADDRESS2, address2Text, input.text, data.getProperty(P_ADDRESS2), 40, 60, true, true, null, script));
		//  City    ZIP
		line = WUtil.createField (null, FORMNAME, P_CITY, cityText, input.text, data.getProperty(P_CITY), 20, 40, false, true, null, script);
		table.addElement(WUtil.createField (line, FORMNAME, P_ZIP, zipText, input.text, data.getProperty(P_ZIP), 12, 20, false, true, null, script));
		//  State   Country
		line = WUtil.createField (null, FORMNAME, P_STATE, stateText, input.text, data.getProperty(P_STATE), 10, 20, false, true, null, script);
		table.addElement(WUtil.createField (line, FORMNAME, P_COUNTRY, countryText, input.text, data.getProperty(P_COUNTRY), 20, 40, false, true, null, script));
		//  Phone
		table.addElement(WUtil.createField (null, FORMNAME, P_PHONE, phoneText, input.text, data.getProperty(P_PHONE), 20, 60, true, true, null, script));

		//  Submit
		line = new tr();
		input cancel = new input(input.reset, "Reset", cancelText);
		line.addElement(new td().addElement(cancel ));
		input submit = new input(input.submit, P_SUBMIT, okText);
		submit.setOnClick("return checkForm(this);");
		line.addElement(new td().addElement(submit ));
		table.addElement(line);
		//
		body.addElement(new script(script.toString()));

		return doc;
	}   //  createPage


	/*************************************************************************/

	/**
	 * 	Lookup customer.
	 *  Response Messages:
	 *  - EMail not found
	 *  - Password not correct
	 *  - Welcome back
	 * 	@param data data to be filled
	 * 	@return true if OK
	 */
	private boolean lookupCustomer (Properties data)
	{
		data.setProperty (P_MESSAGE, "Welcome back ...");
		return true;
	}	//	lookupCustomer

	/**
	 * 	Create or update customer.
	 *  Response Messages:
	 *  - EMail already used
	 *  - Account created
	 *  - Account updated
	 * 	@param data data
	 * 	@return true if OK
	 */
	private boolean createCustomer (Properties data)
	{
		return true;
	}	//	createCustomer

}   //  Customer
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
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import java.sql.*;
import javax.sql.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.apache.log4j.Logger;

import org.compiere.www.*;
import org.compiere.interfaces.*;

/**
 *  Web Store Customer Maintenance
 *  <pre>
 *  - User is asked for email address and password or to fill out the customer info
 *  - if email & password is OK, information is displayed to confirm
 *  </pre>
 *  @author Jorg Janke
 *  @version  $Id: Customer.java,v 1.5 2002/11/11 07:03:36 jjanke Exp $
 */
public class Customer extends HttpServlet
{
	/**	Logging						*/
	private Logger			log = Logger.getLogger(getClass());
	/**	Client						*/
	private int				m_AD_Client_ID = -1;

	/**
	 * 	Initialize global variables
	 *  @param config servlet configuration
	 *  @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		if (!WEnv.initWeb(config))
			throw new ServletException("Customer.init");
		//	Get Client
		m_AD_Client_ID = WEnv.getAD_Client_ID (config);
		String dbUID = WEnv.getDB_UID (config);
		String dbPWD = WEnv.getDB_PWD (config);
		log.info("init - AD_Client_ID=" + m_AD_Client_ID + ", DB=" + dbUID + "/" + dbPWD);
		//
		try
		{
			Context context = new InitialContext();
			DataSource ds = (DataSource)context.lookup("java:OracleDS");
			log.info ("OracleDS=" + ds.toString());
			Connection con = ds.getConnection(dbUID, dbPWD);
			log.info("Connection AutoCommit=" + con.getAutoCommit());
			//
			Object obj = context.lookup(BPartnerHome.JNDI_NAME);
			log.info("Obj=" + obj + " - " + obj.getClass());

			BPartnerHome bpHome = (BPartnerHome)obj;
		//	BPartnerHome bpHome = (BPartnerHome)PortableRemoteObject.narrow(obj, BPartnerHome.class);

			BPartner bp = bpHome.create();
			log.info("BPartner 2=" + bp.process2());
			log.info("BPartner 1=" + bp.process());
			bp.remove();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		//	throw new ServletException (ex);
		}
	}	//	init

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.info("destroy");
	}   //  destroy


	private static final String FORMNAME =      "customer";
	//  Parameter Names
	private static final String P_EMAIL =       "email";
	private static final String P_PASSWORD =    "password";
	private static final String P_LOOKUP =      "lookup";
	//
	private static final String P_COMPANY =     "company";
	private static final String P_NAME =        "name";
	private static final String P_ADDRESS1 =    "address1";
	private static final String P_ADDRESS2 =    "address2";
	private static final String P_CITY =        "city";
	private static final String P_ZIP =         "zip";
	private static final String P_STATE =       "state";
	private static final String P_COUNTRY =     "country";
	private static final String P_PHONE =       "phone";
	//
	private static final String P_SUBMIT =      "SUBMIT";
	private static final String P_MESSAGE =     "MESSAGE";


	/**
	 *  Process the initial HTTP Get request
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		doPost (request, response);
	}   //  doGet


	/**
	 *  Process the HTTP Post request
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		log.info("do from " + request.getRemoteHost() + " - " + request.getRemoteAddr());

		//  Get Session
		HttpSession sess = request.getSession (true);
		//  Get context
		Properties ctx = (Properties)sess.getAttribute(WEnv.SA_CONTEXT);
		//  Get Cookie
		Properties cProp = WUtil.getCookieProprties(request);

		//	Get Data
		Properties data = new Properties();
		data.setProperty(P_COUNTRY, "US");		//	Default
		Enumeration en = request.getParameterNames();
		while (en.hasMoreElements())
		{
			String name = (String)en.nextElement();
			data.setProperty (name, request.getParameter(name));
		}

		//	Lookup Pressed
		if (request.getParameter(P_LOOKUP) != null)
		{
			boolean ok = lookupCustomer (data);
			String email = data.getProperty(P_EMAIL);
			if (email != null)
				cProp.setProperty(P_EMAIL, email);
		}
		//	Submit Pressed
		else if (request.getParameter(P_SUBMIT) != null)
		{
			boolean ok = createCustomer (data);
			String email = data.getProperty(P_EMAIL);
			if (email != null)
				cProp.setProperty(P_EMAIL, email);

			//	Next Page
			if (ok)
			{

			}
		}
		//	Initial request
		else
		{
			data.setProperty (P_EMAIL, cProp.getProperty(P_EMAIL,""));
		}

		//	Create Page
		WDoc doc = createPage (request, data);
		WUtil.createResponse(request, response, this, cProp, doc, true);
	}   //  doPost

	/*************************************************************************/

	/**
	 *  Create Page
	 *  @param request request
	 *  @param data initial data
	 *  @return  Document
	 */
	private WDoc createPage (HttpServletRequest request, Properties data)
	{
		String title 		= "Enter Customer Information";
		String emailText 	= "Enter your email address";
		String passwordText = "Password";
		String lookupText 	= "Lookup Account";
		String choiceText 	= "or enter details below";
		String companyText 	= "Company";
		String nameText 	= "Name (as on credit card)";
		String address1Text = "Street (as on credit card)";
		String address2Text = "Suite/PO Box/Department";
		String cityText 	= "City";
		String zipText 		= "Zip or Postal Code";
		String stateText 	= "State or Province";
		String countryText 	= "Country";
		String phoneText 	= "Phone";
		//
		String cancelText 	= "Reset";
		String okText 		= "Submit Information";
		//
		StringBuffer script = new StringBuffer();
		script.append("mandatory='Enter mandatory information:'; ");

		WDoc doc = WDoc.create (title);
		head head = doc.getHead();
		head.addElement(new script("", WEnv.getBaseDirectory("wstore.js"), "text/javascript", "JavaScript1.2"));
		body body = doc.getBody();

		//  Post to same servlet
		form form = null;
		form = new form(request.getRequestURI(), form.post, form.ENC_DEFAULT).setName(FORMNAME);
		form.setAcceptCharset(WEnv.CHARACTERSET);

		//	Hidden (pass-through) Fields
		form.addElement(new input(input.hidden, "myHideKey", "myHideValue"));
		Iterator it = data.keySet().iterator();
		while (it.hasNext())
		{
			String key = (String)it.next();
			if (key.startsWith("HIDE"))
			{
				String value = data.getProperty(key);
				form.addElement(new input(input.hidden, key, value));
			}
		}	//	for all data iteems


		//	Table
		table table = new table().setAlign(AlignType.CENTER);
		form.addElement(table);
		body.addElement(form);
		tr line = null;

		//	Message (optional)
		String message = data.getProperty(P_MESSAGE);
		if (message != null)
		{
			td ttd = new td().setColSpan(4);
			ttd.addElement(new p().addElement(new strong().addElement(message)));
			table.addElement(new tr().addElement(ttd));
		}

		//  EMail & Password
		line = WUtil.createField (null, FORMNAME, P_EMAIL, emailText, input.text, data.getProperty(P_EMAIL), 20, 40, false, true, null, script);
		table.addElement(WUtil.createField (line, FORMNAME, P_PASSWORD, passwordText, input.password, "", 10, 20, false, true, null, script));

		//  Choice
		line = new tr();
		input lookup = new input(input.submit, P_LOOKUP, lookupText);
		lookup.setOnClick("return checkLookup(this);");
		line.addElement(new td().addElement(lookup ));
		line.addElement(new td().addElement(choiceText));
		table.addElement(line);

		//  Company
		table.addElement(WUtil.createField (null, FORMNAME, P_COMPANY, companyText, input.text, data.getProperty(P_COMPANY), 40, 60, true, false, null, script));
		//  Name
		table.addElement(WUtil.createField (null, FORMNAME, P_NAME, nameText, input.text, data.getProperty(P_NAME,""), 40, 60, true, true, null, script));
		//  Address /2
		table.addElement(WUtil.createField (null, FORMNAME, P_ADDRESS1, address1Text, input.text, data.getProperty(P_ADDRESS1), 40, 60, true, true, null, script));
		table.addElement(WUtil.createField (null, FORMNAME, P_ADDRESS2, address2Text, input.text, data.getProperty(P_ADDRESS2), 40, 60, true, true, null, script));
		//  City    ZIP
		line = WUtil.createField (null, FORMNAME, P_CITY, cityText, input.text, data.getProperty(P_CITY), 20, 40, false, true, null, script);
		table.addElement(WUtil.createField (line, FORMNAME, P_ZIP, zipText, input.text, data.getProperty(P_ZIP), 12, 20, false, true, null, script));
		//  State   Country
		line = WUtil.createField (null, FORMNAME, P_STATE, stateText, input.text, data.getProperty(P_STATE), 10, 20, false, true, null, script);
		table.addElement(WUtil.createField (line, FORMNAME, P_COUNTRY, countryText, input.text, data.getProperty(P_COUNTRY), 20, 40, false, true, null, script));
		//  Phone
		table.addElement(WUtil.createField (null, FORMNAME, P_PHONE, phoneText, input.text, data.getProperty(P_PHONE), 20, 60, true, true, null, script));

		//  Submit
		line = new tr();
		input cancel = new input(input.reset, "Reset", cancelText);
		line.addElement(new td().addElement(cancel ));
		input submit = new input(input.submit, P_SUBMIT, okText);
		submit.setOnClick("return checkForm(this);");
		line.addElement(new td().addElement(submit ));
		table.addElement(line);
		//
		body.addElement(new script(script.toString()));

		return doc;
	}   //  createPage


	/*************************************************************************/

	/**
	 * 	Lookup customer.
	 *  Response Messages:
	 *  - EMail not found
	 *  - Password not correct
	 *  - Welcome back
	 * 	@param data data to be filled
	 * 	@return true if OK
	 */
	private boolean lookupCustomer (Properties data)
	{
		data.setProperty (P_MESSAGE, "Welcome back ...");
		return true;
	}	//	lookupCustomer

	/**
	 * 	Create or update customer.
	 *  Response Messages:
	 *  - EMail already used
	 *  - Account created
	 *  - Account updated
	 * 	@param data data
	 * 	@return true if OK
	 */
	private boolean createCustomer (Properties data)
	{
		return true;
	}	//	createCustomer

}   //  Customer
