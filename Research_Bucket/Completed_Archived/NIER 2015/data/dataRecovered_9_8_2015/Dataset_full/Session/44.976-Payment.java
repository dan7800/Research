/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2002 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.wstore;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;

import org.apache.log4j.Logger;
import org.compiere.www.*;

/**
 *	Web Store Payment Entry & Confirmation
 *
 * 	@author 	Jorg Janke
 * 	@version 	$Id: Payment.java,v 1.1 2002/10/30 05:05:16 jjanke Exp $
 */
public class Payment  extends HttpServlet
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
			throw new ServletException("Payment.init");
		//	Get Client
		m_AD_Client_ID = WEnv.getAD_Client_ID(config);
		log.info("init - AD_Client_ID=" + m_AD_Client_ID);
	}	//	init

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.info("destroy");
	}   //  destroy

	private static final String FORMNAME =      "payment";
	//  Parameter Names
	private static final String P_TENDER =      "TENDER";
	private static final String P_ACCT =        "ACCT";
	private static final String P_EXPDATE =     "EXPDATE";
	private static final String P_ABA =         "ABA";
	private static final String P_BACCT =       "BACCT";
	private static final String P_CHKNUM =      "CHKNUM";
	private static final String P_DL =          "DL";
	//
	private static final String P_SUBMIT =      "SUBMIT";

	/*************************************************************************/

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
		log.info("doGet from " + request.getRemoteHost() + " - " + request.getRemoteAddr());

		//  Create New Session, if required
		HttpSession sess = request.getSession(true);
		sess.setMaxInactiveInterval(60*15);     //  15 Minute Timeout
		//  Create Empty Context
		sess.setAttribute(WEnv.SA_CONTEXT, new Properties());
		//  Get Cookie
		Properties cProp = WUtil.getCookieProprties(request);

		//	Create Page
		WDoc doc = createPage(request);
		WUtil.createResponse(request, response, this, cProp, doc, true);
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
		log.info("doPost from " + request.getRemoteHost() + " - " + request.getRemoteAddr());

		//  Get Session
		HttpSession sess = request.getSession();
		//  Get context
		Properties ctx = (Properties)sess.getAttribute(WEnv.SA_CONTEXT);
		//  Get Cookie
		Properties cProp = WUtil.getCookieProprties(request);

		//	Create Page
		WDoc doc = createPage(request);
		WUtil.createResponse(request, response, this, cProp, doc, true);
	}   //  doPost


	/*************************************************************************/

	/**
	 *  Create Page
	 *  @param request request
	 *  @return  Document
	 */
	private WDoc createPage (HttpServletRequest request)
	{
		String title = "Enter Customer Information";
		//
		String payText = "Select Payment Type";
		String ccText = "Credit Card";
		String ecText = "Electronic Check (US only)";
		String achText = "Automated Clearing House (US only)";
		String acctText = "Credit Card Number";
		String expDateText = "Exp (MM/YY)";
		String abaText = "Bank Transit Number (ABA)";
		String bacctText = "Bank Account Number";
		String chknumText = "Unused Check Number";
		String dlText = "Driver License No";
		String cancelText = "Reset";
		String okText = "Submit Information";

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
		table table = new table().setAlign(AlignType.CENTER);
		form.addElement(table);
		body.addElement(form);

		//  Payment Type
		option[] payTypes = new option[3];
		payTypes[0] = new option("C").addElement(ccText).setSelected(true);
		payTypes[1] = new option("K").addElement(ecText);
		payTypes[2] = new option("A").addElement(achText);
		select payType = new select(P_TENDER, payTypes);
		//
		tr line = new tr();
		line.addElement(new td().addElement(payText).setAlign(AlignType.right));
		line.addElement(new td().addElement(payType).setColSpan(3));
		table.addElement(line);

		//  CreditCard          TENDER=C
		//  ACCT    EXPDATE
		line = WUtil.createField (null, FORMNAME, P_ACCT, acctText, input.text, "", 20, 19, false, true, "checkCreditCard(this);", script);
		table.addElement(WUtil.createField (line, FORMNAME, P_EXPDATE, expDateText, input.text, "", 8, 5, false, true, "checkExpDate(this);", script));

		//  Electronic Check    TENDER=K
		//  MICR (ABA ACCT CHKNUM)
		line = WUtil.createField (null, FORMNAME, P_ABA, abaText, input.text, "", 10, 9, false, true, "checkABA(this);", script);
		table.addElement(WUtil.createField (line, FORMNAME, P_BACCT, bacctText, input.text, "", 12, 12, false, true, "checkBAcct(this);", script));
		//  CHKNUM DL
		line = WUtil.createField (null, FORMNAME, P_CHKNUM, chknumText, input.text, "", 5, 4, false, true, "checkChknum(this);", script);
		table.addElement(WUtil.createField (line, FORMNAME, P_DL, dlText, input.text, "", 10, 9, false, true, "checkDL(this);", script));

		//  ACH                 TENDER=A
		//  ABA ACCT


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
	}	//	createPage


}	//	Payment/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2002 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.wstore;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;

import org.apache.log4j.Logger;
import org.compiere.www.*;

/**
 *	Web Store Payment Entry & Confirmation
 *
 * 	@author 	Jorg Janke
 * 	@version 	$Id: Payment.java,v 1.1 2002/10/30 05:05:16 jjanke Exp $
 */
public class Payment  extends HttpServlet
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
			throw new ServletException("Payment.init");
		//	Get Client
		m_AD_Client_ID = WEnv.getAD_Client_ID(config);
		log.info("init - AD_Client_ID=" + m_AD_Client_ID);
	}	//	init

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.info("destroy");
	}   //  destroy

	private static final String FORMNAME =      "payment";
	//  Parameter Names
	private static final String P_TENDER =      "TENDER";
	private static final String P_ACCT =        "ACCT";
	private static final String P_EXPDATE =     "EXPDATE";
	private static final String P_ABA =         "ABA";
	private static final String P_BACCT =       "BACCT";
	private static final String P_CHKNUM =      "CHKNUM";
	private static final String P_DL =          "DL";
	//
	private static final String P_SUBMIT =      "SUBMIT";

	/*************************************************************************/

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
		log.info("doGet from " + request.getRemoteHost() + " - " + request.getRemoteAddr());

		//  Create New Session, if required
		HttpSession sess = request.getSession(true);
		sess.setMaxInactiveInterval(60*15);     //  15 Minute Timeout
		//  Create Empty Context
		sess.setAttribute(WEnv.SA_CONTEXT, new Properties());
		//  Get Cookie
		Properties cProp = WUtil.getCookieProprties(request);

		//	Create Page
		WDoc doc = createPage(request);
		WUtil.createResponse(request, response, this, cProp, doc, true);
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
		log.info("doPost from " + request.getRemoteHost() + " - " + request.getRemoteAddr());

		//  Get Session
		HttpSession sess = request.getSession();
		//  Get context
		Properties ctx = (Properties)sess.getAttribute(WEnv.SA_CONTEXT);
		//  Get Cookie
		Properties cProp = WUtil.getCookieProprties(request);

		//	Create Page
		WDoc doc = createPage(request);
		WUtil.createResponse(request, response, this, cProp, doc, true);
	}   //  doPost


	/*************************************************************************/

	/**
	 *  Create Page
	 *  @param request request
	 *  @return  Document
	 */
	private WDoc createPage (HttpServletRequest request)
	{
		String title = "Enter Customer Information";
		//
		String payText = "Select Payment Type";
		String ccText = "Credit Card";
		String ecText = "Electronic Check (US only)";
		String achText = "Automated Clearing House (US only)";
		String acctText = "Credit Card Number";
		String expDateText = "Exp (MM/YY)";
		String abaText = "Bank Transit Number (ABA)";
		String bacctText = "Bank Account Number";
		String chknumText = "Unused Check Number";
		String dlText = "Driver License No";
		String cancelText = "Reset";
		String okText = "Submit Information";

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
		table table = new table().setAlign(AlignType.CENTER);
		form.addElement(table);
		body.addElement(form);

		//  Payment Type
		option[] payTypes = new option[3];
		payTypes[0] = new option("C").addElement(ccText).setSelected(true);
		payTypes[1] = new option("K").addElement(ecText);
		payTypes[2] = new option("A").addElement(achText);
		select payType = new select(P_TENDER, payTypes);
		//
		tr line = new tr();
		line.addElement(new td().addElement(payText).setAlign(AlignType.right));
		line.addElement(new td().addElement(payType).setColSpan(3));
		table.addElement(line);

		//  CreditCard          TENDER=C
		//  ACCT    EXPDATE
		line = WUtil.createField (null, FORMNAME, P_ACCT, acctText, input.text, "", 20, 19, false, true, "checkCreditCard(this);", script);
		table.addElement(WUtil.createField (line, FORMNAME, P_EXPDATE, expDateText, input.text, "", 8, 5, false, true, "checkExpDate(this);", script));

		//  Electronic Check    TENDER=K
		//  MICR (ABA ACCT CHKNUM)
		line = WUtil.createField (null, FORMNAME, P_ABA, abaText, input.text, "", 10, 9, false, true, "checkABA(this);", script);
		table.addElement(WUtil.createField (line, FORMNAME, P_BACCT, bacctText, input.text, "", 12, 12, false, true, "checkBAcct(this);", script));
		//  CHKNUM DL
		line = WUtil.createField (null, FORMNAME, P_CHKNUM, chknumText, input.text, "", 5, 4, false, true, "checkChknum(this);", script);
		table.addElement(WUtil.createField (line, FORMNAME, P_DL, dlText, input.text, "", 10, 9, false, true, "checkDL(this);", script));

		//  ACH                 TENDER=A
		//  ABA ACCT


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
	}	//	createPage


}	//	Payment