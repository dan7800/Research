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
import org.compiere.model.*;
import org.compiere.www.*;


/**
 *  Web Order.
 *
 *  @author Jorg Janke
 *  @version $Id: OrderServlet.java,v 1.20 2004/05/04 04:50:08 jjanke Exp $
 */
public class OrderServlet extends HttpServlet
{
	/**	Logging						*/
	private Logger			log = Logger.getCLogger(getClass());
	private static Logger	s_log = Logger.getCLogger(OrderServlet.class);

	/** Name						*/
	static public final String			NAME = "orderServlet";

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
			throw new ServletException("OrderServlet.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Order Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
	}   //  destroy

	/*************************************************************************/

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
		doPost (request, response);
	}	//	doGet

	/**
	 *  Process the HTTP Post request
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		log.info("doPost from " + request.getRemoteHost() + " - " + request.getRemoteAddr());
		Properties ctx = JSPEnv.getCtx(request);
		HttpSession session = request.getSession(true);
		session.removeAttribute(JSPEnv.HDR_MESSAGE);

		//	Web User/Basket
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
		WebBasket wb = (WebBasket)session.getAttribute(WebBasket.NAME);

		String url = "/paymentInfo.jsp";
		//	Not logged in
		if (wu == null || !wu.isLoggedIn())
		{
			session.setAttribute("CheckOut", "Y");	//	indicate checkout
			url = "/login.jsp";
		}
		//	Process Existing Order
		else if (processOrder(request, ctx, wu))
			url = "/orders.jsp";
		//	Nothing in basket
		else if (wb == null || wb.getLineCount() == 0)
			url = "/basket.jsp";
		//	Create Order & Payment Info
		else
		{
			WebOrder wo = new WebOrder(wu, wb, ctx);
			//	We have an order - do delete basket & checkout indicator
			if (wo.isInProgress() || wo.isProcessed())
			{
				session.removeAttribute(CheckOutServlet.ATTR_CHECKOUT);
				session.removeAttribute(WebBasket.NAME);
				sendEMail(request, ctx, wo, wu);
			}
			//	If the Order is negative, don't create a payment
			if (wo.getGrandTotal().compareTo(Env.ZERO) > 0)
			{
				session.setAttribute(WebOrder.NAME, wo);
				MPayment p = createPayment (session, ctx, wu, wo);
				if (p == null)
				{
					WUtil.createForwardPage(response, "Payment could not be created", "orders.jsp", 5);
					return;
				}
				else
					session.setAttribute (PaymentServlet.ATTR_PAYMENT, p);
			}
			else
			{
				url = "/orders.jsp";
			}
		}

		log.info ("doPost - Forward to " + url);
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher (url);
		dispatcher.forward (request, response);
	}	//	doPost

	/*************************************************************************/

	/**
	 * 	Create Payment, but don't save it
	 * 	@param session session
	 * 	@param ctx context
	 * 	@param wu web user
	 * 	@param wo Order
	 * 	@return Payment
	 */
	private MPayment createPayment(HttpSession session, Properties ctx,
		WebUser wu,	WebOrder wo)
	{
		//	See PaymentServlet.doGet
		MPayment p = new MPayment(ctx, 0);
		p.setIsSelfService(true);
		p.setAmount (wo.getC_Currency_ID(), wo.getGrandTotal ()); //	for CC selection
		p.setIsOnline (true);
		//	Sales CC Trx
		p.setC_DocType_ID(true);
		p.setTrxType(MPayment.TRXTYPE_Sales);
		p.setTenderType(MPayment.TENDERTYPE_CreditCard);
		//	Order Info
	//	p.setC_Invoice_ID(wo.getInvoice_ID());
		//	BP Info
		p.setBP_BankAccount(wu.getBankAccount());
		//
		return p;
	}	//	createPayment

	/**
	 *	Process Order
	 *	@param request request
	 * 	@param ctx context
	 *	@param wu web user
	 *	@return true if processed
	 */
	private boolean processOrder (HttpServletRequest request, Properties ctx, WebUser wu)
	{
		String para = request.getParameter("C_Order_ID");
		String DocAction = request.getParameter("DocAction");
		if (para == null || para.length() == 0 || DocAction == null || DocAction.length() == 0)
			return false;
		int C_Order_ID = 0;
		try
		{
			C_Order_ID = Integer.parseInt (para);
		}
		catch (NumberFormatException ex)
		{
		}
		if (C_Order_ID == 0)
			return false;

		//	We have a Order No & DocAction
		log.debug("processOrder - C_Order_ID=" + C_Order_ID + ", DocAction=" + DocAction);
		if (!(MOrder.DOCACTION_Complete.equals(DocAction) || MOrder.DOCACTION_Void.equals(DocAction)))
		{
			log.warn("processOrder - C_Order_ID=" + C_Order_ID + ", Invalid DocAction=" + DocAction);
			return true;
		}
		MOrder order = new MOrder (ctx, C_Order_ID);
	//	if (order.isSelfService())
		return order.processOrder (DocAction);
	}	//	processOrder


	/**
	 * 	Send Order EMail.
	 * 	@param request request
	 * 	@param ctx context
	 * 	@param wo web order
	 * 	@param wu web user
	 */
	private void sendEMail (HttpServletRequest request, Properties ctx, WebOrder wo, WebUser wu)
	{
		String subject = "Compiere Web - Order " + wo.getDocumentNo();
		String message = "Thank you for your purchase."
			+ "\nYou can view your order, invoices and open amounts at"
			+ "\nhttp://" + request.getServerName() + request.getContextPath() + "/"
			+ "\n\nOrder " + wo.getDocumentNo() + " - Amount " + wo.getGrandTotal()
			+ "\n";
		//
		MOrder mo = wo.getOrder();
		if (mo != null)
		{
			MOrderLine[] ol = mo.getLines(true);
			for (int i = 0; i < ol.length; i++)
			{
				message += "\n" + ol[i].getQtyOrdered() + " * " + ol[i].getName();
				if (ol[i].getDescription() != null)
					message += " - " + ol[i].getDescription();
				message += " (" + ol[i].getPriceActual() + ") = " + ol[i].getLineNetAmt();
			}	//	line
		}	//	order

		JSPEnv.sendEMail(ctx, wu.getEmail(), subject, message);
	}	//	sendEMail


}	//	OrderServlet
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
import org.compiere.model.*;
import org.compiere.www.*;


/**
 *  Web Order.
 *
 *  @author Jorg Janke
 *  @version $Id: OrderServlet.java,v 1.14 2003/07/19 05:21:44 jjanke Exp $
 */
public class OrderServlet extends HttpServlet
{
	/**	Logging						*/
	private Logger			log = Logger.getLogger(getClass());
	private static Logger	s_log = Logger.getLogger(OrderServlet.class);

	/** Name						*/
	static public final String			NAME = "orderServlet";

	public static final String 			BANKACCOUNT_ATTR = "bankAccount";


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
			throw new ServletException("OrderServlet.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Order Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
	}   //  destroy

	/*************************************************************************/

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
		doPost (request, response);
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

		//	Web User/Basket
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
		WebBasket wb = (WebBasket)session.getAttribute(WebBasket.NAME);

		String url = "paymentInfo.jsp";
		//	Nothing in basket
		if (wb == null || wb.getLineCount() == 0)
			url = "basket.jsp";
		//	Not logged in
		else if (wu == null || !wu.isLoggedIn())
		{
			session.setAttribute("CheckOut", "Y");	//	indicate checkout
			url = "login.jsp";
		}
		//	Create Order & Payment Info
		else
		{
			WebOrder wo = new WebOrder(wu, wb, ctx);
			//	We have an order - do delete basket & checkout indicator
			if (wo.isInProgress() || wo.isProcessed())
			{
				session.removeAttribute(CheckOutServlet.ATTR_CHECKOUT);
				session.removeAttribute(WebBasket.NAME);
				sendEMail(request, ctx, wo, wu);
			}
			//	If the Order is negative, don't create a payment
			if (wo.getGrandTotal().compareTo(Env.ZERO) > 0)
			{
				session.setAttribute(WebOrder.NAME, wo);
				MPayment p = createPayment (session, ctx, wu, wo);
				if (p == null)
				{
					WUtil.createForwardPage(response, "Payment could not be created", "orders.jsp");
					return;
				}
				else
					session.setAttribute (PaymentServlet.ATTR_PAYMENT, p);
			}
			else
			{
				url = "orders.jsp";
			}
		}

		log.info ("doPost - Forward to " + url);
		RequestDispatcher dispatcher = getServletContext ().getRequestDispatcher (url);
		dispatcher.forward (request, response);
	}	//	doPost


	/**
	 * 	Create Payment, but don't save it
	 * 	@param session session
	 * 	@param ctx context
	 * 	@param wu web user
	 * 	@param wo Order
	 * 	@return Payment
	 */
	private MPayment createPayment(HttpSession session, Properties ctx, WebUser wu,
		WebOrder wo)
	{
		//	See PaymentServlet.doGet
		MPayment p = new MPayment(ctx, 0);
		//	Bank Account
		MBankAccount ba = JSPEnv.getBankAccount(session, ctx);
		if (ba == null)			//	may not be defined defined
			return null;
		//
		ba.setPayAmt (wo.getGrandTotal ()); //	for CC selection
		p.setC_BankAccount_ID (ba.getC_BankAccount_ID ());
		p.setC_Currency_ID (ba.getC_Currency_ID ());
		p.setIsOnline (true);
		//	Sales CC Trx
		p.setC_DocType_ID(true);
		p.setTrxType(MPayment.TRXTYPE_Sales);
		p.setTenderType(MPayment.TENDERTYPE_CreditCard);
		//	Order Info
		p.setPayAmt(wo.getGrandTotal());
	//	p.setC_Invoice_ID(wo.getInvoice_ID());
		//	BP Info
		p.setBP_BankAccount(wu.getBankAccount());
		//
		return p;
	}	//	createPayment

	/**
	 * 	Send Order EMail.
	 * 	@param request request
	 * 	@param ctx context
	 * 	@param wo web order
	 * 	@param wu web user
	 */
	private void sendEMail (HttpServletRequest request, Properties ctx, WebOrder wo, WebUser wu)
	{
		String subject = "Compiere Web - Order " + wo.getDocumentNo();
		String message = "Thank you for your purchase."
			+ "\nYou can view your order, invoices and open amounts at"
			+ "\nhttp://" + request.getServerName() + request.getContextPath() + "/"
			+ "\n\nOrder " + wo.getDocumentNo() + " - Amount " + wo.getGrandTotal()
			+ "\n";
		//
		MOrder mo = wo.getOrder();
		if (mo != null)
		{
			MOrderLine[] ol = mo.getLines();
			if (ol != null)
			{
				for (int i = 0; i < ol.length; i++)
				{
					message += "\n" + ol[i].getQtyOrdered() + " * " + ol[i].getName();
					if (ol[i].getDescription() != null)
						message += " - " + ol[i].getDescription();
					message += " (" + ol[i].getPriceActual() + ") = " + ol[i].getLineNetAmt();
				}	//	line
			}	//	lines
		}	//	order

		String SMTPHost = ctx.getProperty("SMTPHost", "localhost");
		String RequestEMail = ctx.getProperty("RequestEMail");
		String RequestUser = ctx.getProperty("RequestUser");
		String RequestUserPw = ctx.getProperty("RequestUserPw");
		//
		EMail em = new EMail(SMTPHost, RequestEMail, wu.getEmail(), subject, message.toString());
		em.setEMailUser(RequestUser, RequestUserPw);
		//
		String webOrderEMail = ctx.getProperty("webOrderEMail");
		em.addBcc(webOrderEMail);
		//
		em.send();
	}	//	sendEMail


}	//	OrderServlet
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

import org.compiere.model.*;
import org.compiere.util.*;


/**
 *  Web Order.
 *
 *  @author Jorg Janke
 *  @version $Id: OrderServlet.java,v 1.24 2004/09/10 02:54:23 jjanke Exp $
 */
public class OrderServlet extends HttpServlet
{
	/**	Logging						*/
	private Logger			log = Logger.getCLogger(getClass());
//	private static Logger	s_log = Logger.getCLogger(OrderServlet.class);

	/** Name						*/
	static public final String			NAME = "orderServlet";

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
			throw new ServletException("OrderServlet.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Order Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
	}   //  destroy

	
	/**************************************************************************
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
		doPost (request, response);
	}	//	doGet

	/**
	 *  Process the HTTP Post request
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		log.info("doPost from " + request.getRemoteHost() + " - " + request.getRemoteAddr());
		Properties ctx = JSPEnv.getCtx(request);
		HttpSession session = request.getSession(true);
		session.removeAttribute(JSPEnv.HDR_MESSAGE);

		//	Web User/Basket
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
		WebBasket wb = (WebBasket)session.getAttribute(WebBasket.NAME);
		MOrder order = null;
		
		boolean done = false;
		String url = "/paymentInfo.jsp";
		//	Not logged in
		if (wu == null || !wu.isLoggedIn())
		{
			session.setAttribute("CheckOut", "Y");	//	indicate checkout
			url = "/login.jsp";
			done = true;
		}
		else
			order = getOrder(request, ctx);
		
		//	We have an Order
		if (!done && order != null)
		{
			if (processOrder(request, order))
				url = "/orders.jsp";
			else
			{
				WebOrder wo = new WebOrder (order);
				MPayment p = createPayment (session, ctx, wu, wo);
				if (p != null)
				{
					session.setAttribute (PaymentServlet.ATTR_PAYMENT, p);
					session.setAttribute(WebOrder.NAME, wo);
				}
				else
					url = "/orders.jsp";
			}
			done = true;
		}
		
		//	Nothing in basket
		if (!done && (wb == null || wb.getLineCount() == 0))
		{
			url = "/basket.jsp";
			done = true;
		}
		//	Create Order & Payment Info
		if (!done)
		{
			WebOrder wo = new WebOrder(wu, wb, ctx);
			//	We have an order - do delete basket & checkout indicator
			if (wo.isInProgress() || wo.isCompleted())
			{
				session.removeAttribute(CheckOutServlet.ATTR_CHECKOUT);
				session.removeAttribute(WebBasket.NAME);
				sendEMail(request, ctx, wo, wu);
			}
			//	If the Order is negative, don't create a payment
			if (wo.getGrandTotal().compareTo(Env.ZERO) > 0)
			{
				session.setAttribute(WebOrder.NAME, wo);
				MPayment p = createPayment (session, ctx, wu, wo);
				if (p == null)
				{
					WebUtil.createForwardPage(response, "Payment could not be created", "orders.jsp", 5);
					return;
				}
				else
					session.setAttribute (PaymentServlet.ATTR_PAYMENT, p);
			}
			else
			{
				url = "/orders.jsp";
			}
		}

		log.info ("doPost - Forward to " + url);
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher (url);
		dispatcher.forward (request, response);
	}	//	doPost

	
	/**************************************************************************
	 * 	Create Payment, but don't save it
	 * 	@param session session
	 * 	@param ctx context
	 * 	@param wu web user
	 * 	@param wo Order
	 * 	@return Payment
	 */
	private MPayment createPayment(HttpSession session, Properties ctx,
		WebUser wu,	WebOrder wo)
	{
		//	See PaymentServlet.doGet
		MPayment p = new MPayment(ctx, 0);
		p.setIsSelfService(true);
		p.setAmount (wo.getC_Currency_ID(), wo.getGrandTotal ()); //	for CC selection
		p.setIsOnline (true);
		//	Sales CC Trx
		p.setC_DocType_ID(true);
		p.setTrxType(MPayment.TRXTYPE_Sales);
		p.setTenderType(MPayment.TENDERTYPE_CreditCard);
		//	Order Info
		p.setC_Order_ID(wo.getC_Order_ID());
		//	BP Info
		p.setBP_BankAccount(wu.getBankAccount());
		//
		return p;
	}	//	createPayment

	/**
	 *	Get Order
	 *	@param request request
	 * 	@param ctx context
	 *	@return true if processed
	 */
	private MOrder getOrder (HttpServletRequest request, Properties ctx)
	{
		//	Order
		String para = request.getParameter("C_Order_ID");
		if (para == null || para.length() == 0)
			return null;
		int C_Order_ID = 0;
		try
		{
			C_Order_ID = Integer.parseInt (para);
		}
		catch (NumberFormatException ex)
		{
		}
		if (C_Order_ID == 0)
			return null;

		log.debug("getOrder - C_Order_ID=" + C_Order_ID);
		return new MOrder (ctx, C_Order_ID);
	}	//	getOrder
	
	
	/**
	 *	Process Order
	 *	@param request request
	 * 	@param ctx context
	 *	@param wu web user
	 *	@return true if processed/ok
	 */
	private boolean processOrder (HttpServletRequest request, MOrder order)
	{
		//	Doc Action
		String DocAction = request.getParameter("DocAction");
		if (DocAction == null || DocAction.length() == 0)
			return false;

		MDocType dt = MDocType.get(order.getCtx(), order.getC_DocType_ID());
		if (!order.isSOTrx() 
			|| order.getGrandTotal().compareTo(Env.ZERO) <= 0
			|| !MDocType.DOCBASETYPE_SalesOrder.equals(dt.getDocBaseType()))
		{
			log.warn("processOrder - Not a valid Sales Order " + order);
			return true;
		}

		//	We have a Order No & DocAction
		log.debug("processOrder - DocAction=" + DocAction);
		if (!MOrder.DOCACTION_Void.equals(DocAction))
		{
			//	Do not complete Prepayment
			if (MOrder.STATUS_WaitingPayment.equals(order.getDocStatus()))
				return false;
			if (MDocType.DOCSUBTYPESO_PrepayOrder.equals(dt.getDocSubTypeSO()))
				return false;
			if (!MOrder.DOCACTION_Complete.equals(DocAction))
			{
				log.warn("processOrder - Invalid DocAction=" + DocAction);
				return true;
			}
		}
		order.setDocAction (DocAction, true);	//	force creation
		boolean ok = order.processIt (DocAction);
		order.save();
		return ok;
	}	//	processOrder


	/**
	 * 	Send Order EMail.
	 * 	@param request request
	 * 	@param ctx context
	 * 	@param wo web order
	 * 	@param wu web user
	 */
	private void sendEMail (HttpServletRequest request, Properties ctx, WebOrder wo, WebUser wu)
	{
		String subject = "Compiere Web - Order " + wo.getDocumentNo();
		String message = "Thank you for your purchase."
			+ "\nYou can view your order, invoices and open amounts at"
			+ "\nhttp://" + request.getServerName() + request.getContextPath() + "/"
			+ "\n\nOrder " + wo.getDocumentNo() + " - Amount " + wo.getGrandTotal()
			+ "\n";
		//
		MOrder mo = wo.getOrder();
		if (mo != null)
		{
			MOrderLine[] ol = mo.getLines(true);
			for (int i = 0; i < ol.length; i++)
			{
				message += "\n" + ol[i].getQtyOrdered() + " * " + ol[i].getName();
				if (ol[i].getDescription() != null)
					message += " - " + ol[i].getDescription();
				message += " (" + ol[i].getPriceActual() + ") = " + ol[i].getLineNetAmt();
			}	//	line
		}	//	order

		JSPEnv.sendEMail(ctx, wu.getEmail(), subject, message);
	}	//	sendEMail


}	//	OrderServlet
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
import org.compiere.model.*;
import org.compiere.www.*;


/**
 *  Web Order.
 *
 *  @author Jorg Janke
 *  @version $Id: OrderServlet.java,v 1.16 2003/09/05 04:55:42 jjanke Exp $
 */
public class OrderServlet extends HttpServlet
{
	/**	Logging						*/
	private Logger			log = Logger.getLogger(getClass());
	private static Logger	s_log = Logger.getLogger(OrderServlet.class);

	/** Name						*/
	static public final String			NAME = "orderServlet";

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
			throw new ServletException("OrderServlet.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Order Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
	}   //  destroy

	/*************************************************************************/

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
		doPost (request, response);
	}	//	doGet

	/**
	 *  Process the HTTP Post request
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		log.info("doPost from " + request.getRemoteHost() + " - " + request.getRemoteAddr());
		Properties ctx = JSPEnv.getCtx(request);
		HttpSession session = request.getSession(true);
		session.removeAttribute(JSPEnv.HDR_MESSAGE);

		//	Web User/Basket
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
		WebBasket wb = (WebBasket)session.getAttribute(WebBasket.NAME);

		String url = "paymentInfo.jsp";
		//	Not logged in
		if (wu == null || !wu.isLoggedIn())
		{
			session.setAttribute("CheckOut", "Y");	//	indicate checkout
			url = "login.jsp";
		}
		//	Process Existing Order
		else if (processOrder(request, ctx, wu))
			url = "orders.jsp";
		//	Nothing in basket
		else if (wb == null || wb.getLineCount() == 0)
			url = "basket.jsp";
		//	Create Order & Payment Info
		else
		{
			WebOrder wo = new WebOrder(wu, wb, ctx);
			//	We have an order - do delete basket & checkout indicator
			if (wo.isInProgress() || wo.isProcessed())
			{
				session.removeAttribute(CheckOutServlet.ATTR_CHECKOUT);
				session.removeAttribute(WebBasket.NAME);
				sendEMail(request, ctx, wo, wu);
			}
			//	If the Order is negative, don't create a payment
			if (wo.getGrandTotal().compareTo(Env.ZERO) > 0)
			{
				session.setAttribute(WebOrder.NAME, wo);
				MPayment p = createPayment (session, ctx, wu, wo);
				if (p == null)
				{
					WUtil.createForwardPage(response, "Payment could not be created", "orders.jsp");
					return;
				}
				else
					session.setAttribute (PaymentServlet.ATTR_PAYMENT, p);
			}
			else
			{
				url = "orders.jsp";
			}
		}

		log.info ("doPost - Forward to " + url);
		RequestDispatcher dispatcher = getServletContext ().getRequestDispatcher (url);
		dispatcher.forward (request, response);
	}	//	doPost

	/*************************************************************************/

	/**
	 * 	Create Payment, but don't save it
	 * 	@param session session
	 * 	@param ctx context
	 * 	@param wu web user
	 * 	@param wo Order
	 * 	@return Payment
	 */
	private MPayment createPayment(HttpSession session, Properties ctx,
		WebUser wu,	WebOrder wo)
	{
		//	See PaymentServlet.doGet
		MPayment p = new MPayment(ctx, 0);
		p.setIsSelfService(true);
		p.setAmount (wo.getC_Currency_ID(), wo.getGrandTotal ()); //	for CC selection
		p.setIsOnline (true);
		//	Sales CC Trx
		p.setC_DocType_ID(true);
		p.setTrxType(MPayment.TRXTYPE_Sales);
		p.setTenderType(MPayment.TENDERTYPE_CreditCard);
		//	Order Info
	//	p.setC_Invoice_ID(wo.getInvoice_ID());
		//	BP Info
		p.setBP_BankAccount(wu.getBankAccount());
		//
		return p;
	}	//	createPayment

	/**
	 *	Process Order
	 *	@param request request
	 * 	@param ctx context
	 *	@param wu web user
	 *	@return true if processed
	 */
	private boolean processOrder (HttpServletRequest request, Properties ctx, WebUser wu)
	{
		String para = request.getParameter("C_Order_ID");
		String DocAction = request.getParameter("DocAction");
		if (para == null || para.length() == 0 || DocAction == null || DocAction.length() == 0)
			return false;
		int C_Order_ID = 0;
		try
		{
			C_Order_ID = Integer.parseInt (para);
		}
		catch (NumberFormatException ex)
		{
		}
		if (C_Order_ID == 0)
			return false;

		//	We have a Order No & DocAction
		log.debug("processOrder - C_Order_ID=" + C_Order_ID + ", DocAction=" + DocAction);
		if (!(MOrder.DOCACTION_Complete.equals(DocAction) || MOrder.DOCACTION_Void.equals(DocAction)))
		{
			log.warn("processOrder - C_Order_ID=" + C_Order_ID + ", Invalid DocAction=" + DocAction);
			return true;
		}
		MOrder order = new MOrder (ctx, C_Order_ID);
	//	if (order.isSelfService())
		order.process(DocAction);

		return true;
	}	//	processOrder


	/**
	 * 	Send Order EMail.
	 * 	@param request request
	 * 	@param ctx context
	 * 	@param wo web order
	 * 	@param wu web user
	 */
	private void sendEMail (HttpServletRequest request, Properties ctx, WebOrder wo, WebUser wu)
	{
		String subject = "Compiere Web - Order " + wo.getDocumentNo();
		String message = "Thank you for your purchase."
			+ "\nYou can view your order, invoices and open amounts at"
			+ "\nhttp://" + request.getServerName() + request.getContextPath() + "/"
			+ "\n\nOrder " + wo.getDocumentNo() + " - Amount " + wo.getGrandTotal()
			+ "\n";
		//
		MOrder mo = wo.getOrder();
		if (mo != null)
		{
			MOrderLine[] ol = mo.getLines();
			if (ol != null)
			{
				for (int i = 0; i < ol.length; i++)
				{
					message += "\n" + ol[i].getQtyOrdered() + " * " + ol[i].getName();
					if (ol[i].getDescription() != null)
						message += " - " + ol[i].getDescription();
					message += " (" + ol[i].getPriceActual() + ") = " + ol[i].getLineNetAmt();
				}	//	line
			}	//	lines
		}	//	order

		String SMTPHost = ctx.getProperty("SMTPHost", "localhost");
		String RequestEMail = ctx.getProperty("RequestEMail");
		String RequestUser = ctx.getProperty("RequestUser");
		String RequestUserPw = ctx.getProperty("RequestUserPw");
		//
		EMail em = new EMail(SMTPHost, RequestEMail, wu.getEmail(), subject, message.toString());
		em.setEMailUser(RequestUser, RequestUserPw);
		//
		String webOrderEMail = ctx.getProperty("webOrderEMail");
		em.addBcc(webOrderEMail);
		//
		em.send();
	}	//	sendEMail


}	//	OrderServlet
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
import org.compiere.model.*;
import org.compiere.www.*;


/**
 *  Web Order.
 *
 *  @author Jorg Janke
 *  @version $Id: OrderServlet.java,v 1.14 2003/07/19 05:21:44 jjanke Exp $
 */
public class OrderServlet extends HttpServlet
{
	/**	Logging						*/
	private Logger			log = Logger.getLogger(getClass());
	private static Logger	s_log = Logger.getLogger(OrderServlet.class);

	/** Name						*/
	static public final String			NAME = "orderServlet";

	public static final String 			BANKACCOUNT_ATTR = "bankAccount";


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
			throw new ServletException("OrderServlet.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Order Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
	}   //  destroy

	/*************************************************************************/

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
		doPost (request, response);
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

		//	Web User/Basket
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
		WebBasket wb = (WebBasket)session.getAttribute(WebBasket.NAME);

		String url = "paymentInfo.jsp";
		//	Nothing in basket
		if (wb == null || wb.getLineCount() == 0)
			url = "basket.jsp";
		//	Not logged in
		else if (wu == null || !wu.isLoggedIn())
		{
			session.setAttribute("CheckOut", "Y");	//	indicate checkout
			url = "login.jsp";
		}
		//	Create Order & Payment Info
		else
		{
			WebOrder wo = new WebOrder(wu, wb, ctx);
			//	We have an order - do delete basket & checkout indicator
			if (wo.isInProgress() || wo.isProcessed())
			{
				session.removeAttribute(CheckOutServlet.ATTR_CHECKOUT);
				session.removeAttribute(WebBasket.NAME);
				sendEMail(request, ctx, wo, wu);
			}
			//	If the Order is negative, don't create a payment
			if (wo.getGrandTotal().compareTo(Env.ZERO) > 0)
			{
				session.setAttribute(WebOrder.NAME, wo);
				MPayment p = createPayment (session, ctx, wu, wo);
				if (p == null)
				{
					WUtil.createForwardPage(response, "Payment could not be created", "orders.jsp");
					return;
				}
				else
					session.setAttribute (PaymentServlet.ATTR_PAYMENT, p);
			}
			else
			{
				url = "orders.jsp";
			}
		}

		log.info ("doPost - Forward to " + url);
		RequestDispatcher dispatcher = getServletContext ().getRequestDispatcher (url);
		dispatcher.forward (request, response);
	}	//	doPost


	/**
	 * 	Create Payment, but don't save it
	 * 	@param session session
	 * 	@param ctx context
	 * 	@param wu web user
	 * 	@param wo Order
	 * 	@return Payment
	 */
	private MPayment createPayment(HttpSession session, Properties ctx, WebUser wu,
		WebOrder wo)
	{
		//	See PaymentServlet.doGet
		MPayment p = new MPayment(ctx, 0);
		//	Bank Account
		MBankAccount ba = JSPEnv.getBankAccount(session, ctx);
		if (ba == null)			//	may not be defined defined
			return null;
		//
		ba.setPayAmt (wo.getGrandTotal ()); //	for CC selection
		p.setC_BankAccount_ID (ba.getC_BankAccount_ID ());
		p.setC_Currency_ID (ba.getC_Currency_ID ());
		p.setIsOnline (true);
		//	Sales CC Trx
		p.setC_DocType_ID(true);
		p.setTrxType(MPayment.TRXTYPE_Sales);
		p.setTenderType(MPayment.TENDERTYPE_CreditCard);
		//	Order Info
		p.setPayAmt(wo.getGrandTotal());
	//	p.setC_Invoice_ID(wo.getInvoice_ID());
		//	BP Info
		p.setBP_BankAccount(wu.getBankAccount());
		//
		return p;
	}	//	createPayment

	/**
	 * 	Send Order EMail.
	 * 	@param request request
	 * 	@param ctx context
	 * 	@param wo web order
	 * 	@param wu web user
	 */
	private void sendEMail (HttpServletRequest request, Properties ctx, WebOrder wo, WebUser wu)
	{
		String subject = "Compiere Web - Order " + wo.getDocumentNo();
		String message = "Thank you for your purchase."
			+ "\nYou can view your order, invoices and open amounts at"
			+ "\nhttp://" + request.getServerName() + request.getContextPath() + "/"
			+ "\n\nOrder " + wo.getDocumentNo() + " - Amount " + wo.getGrandTotal()
			+ "\n";
		//
		MOrder mo = wo.getOrder();
		if (mo != null)
		{
			MOrderLine[] ol = mo.getLines();
			if (ol != null)
			{
				for (int i = 0; i < ol.length; i++)
				{
					message += "\n" + ol[i].getQtyOrdered() + " * " + ol[i].getName();
					if (ol[i].getDescription() != null)
						message += " - " + ol[i].getDescription();
					message += " (" + ol[i].getPriceActual() + ") = " + ol[i].getLineNetAmt();
				}	//	line
			}	//	lines
		}	//	order

		String SMTPHost = ctx.getProperty("SMTPHost", "localhost");
		String RequestEMail = ctx.getProperty("RequestEMail");
		String RequestUser = ctx.getProperty("RequestUser");
		String RequestUserPw = ctx.getProperty("RequestUserPw");
		//
		EMail em = new EMail(SMTPHost, RequestEMail, wu.getEmail(), subject, message.toString());
		em.setEMailUser(RequestUser, RequestUserPw);
		//
		String webOrderEMail = ctx.getProperty("webOrderEMail");
		em.addBcc(webOrderEMail);
		//
		em.send();
	}	//	sendEMail


}	//	OrderServlet
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
import org.compiere.model.*;
import org.compiere.www.*;


/**
 *  Web Order.
 *
 *  @author Jorg Janke
 *  @version $Id: OrderServlet.java,v 1.14 2003/07/19 05:21:44 jjanke Exp $
 */
public class OrderServlet extends HttpServlet
{
	/**	Logging						*/
	private Logger			log = Logger.getLogger(getClass());
	private static Logger	s_log = Logger.getLogger(OrderServlet.class);

	/** Name						*/
	static public final String			NAME = "orderServlet";

	public static final String 			BANKACCOUNT_ATTR = "bankAccount";


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
			throw new ServletException("OrderServlet.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Order Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
	}   //  destroy

	/*************************************************************************/

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
		doPost (request, response);
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

		//	Web User/Basket
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
		WebBasket wb = (WebBasket)session.getAttribute(WebBasket.NAME);

		String url = "paymentInfo.jsp";
		//	Nothing in basket
		if (wb == null || wb.getLineCount() == 0)
			url = "basket.jsp";
		//	Not logged in
		else if (wu == null || !wu.isLoggedIn())
		{
			session.setAttribute("CheckOut", "Y");	//	indicate checkout
			url = "login.jsp";
		}
		//	Create Order & Payment Info
		else
		{
			WebOrder wo = new WebOrder(wu, wb, ctx);
			//	We have an order - do delete basket & checkout indicator
			if (wo.isInProgress() || wo.isProcessed())
			{
				session.removeAttribute(CheckOutServlet.ATTR_CHECKOUT);
				session.removeAttribute(WebBasket.NAME);
				sendEMail(request, ctx, wo, wu);
			}
			//	If the Order is negative, don't create a payment
			if (wo.getGrandTotal().compareTo(Env.ZERO) > 0)
			{
				session.setAttribute(WebOrder.NAME, wo);
				MPayment p = createPayment (session, ctx, wu, wo);
				if (p == null)
				{
					WUtil.createForwardPage(response, "Payment could not be created", "orders.jsp");
					return;
				}
				else
					session.setAttribute (PaymentServlet.ATTR_PAYMENT, p);
			}
			else
			{
				url = "orders.jsp";
			}
		}

		log.info ("doPost - Forward to " + url);
		RequestDispatcher dispatcher = getServletContext ().getRequestDispatcher (url);
		dispatcher.forward (request, response);
	}	//	doPost


	/**
	 * 	Create Payment, but don't save it
	 * 	@param session session
	 * 	@param ctx context
	 * 	@param wu web user
	 * 	@param wo Order
	 * 	@return Payment
	 */
	private MPayment createPayment(HttpSession session, Properties ctx, WebUser wu,
		WebOrder wo)
	{
		//	See PaymentServlet.doGet
		MPayment p = new MPayment(ctx, 0);
		//	Bank Account
		MBankAccount ba = JSPEnv.getBankAccount(session, ctx);
		if (ba == null)			//	may not be defined defined
			return null;
		//
		ba.setPayAmt (wo.getGrandTotal ()); //	for CC selection
		p.setC_BankAccount_ID (ba.getC_BankAccount_ID ());
		p.setC_Currency_ID (ba.getC_Currency_ID ());
		p.setIsOnline (true);
		//	Sales CC Trx
		p.setC_DocType_ID(true);
		p.setTrxType(MPayment.TRXTYPE_Sales);
		p.setTenderType(MPayment.TENDERTYPE_CreditCard);
		//	Order Info
		p.setPayAmt(wo.getGrandTotal());
	//	p.setC_Invoice_ID(wo.getInvoice_ID());
		//	BP Info
		p.setBP_BankAccount(wu.getBankAccount());
		//
		return p;
	}	//	createPayment

	/**
	 * 	Send Order EMail.
	 * 	@param request request
	 * 	@param ctx context
	 * 	@param wo web order
	 * 	@param wu web user
	 */
	private void sendEMail (HttpServletRequest request, Properties ctx, WebOrder wo, WebUser wu)
	{
		String subject = "Compiere Web - Order " + wo.getDocumentNo();
		String message = "Thank you for your purchase."
			+ "\nYou can view your order, invoices and open amounts at"
			+ "\nhttp://" + request.getServerName() + request.getContextPath() + "/"
			+ "\n\nOrder " + wo.getDocumentNo() + " - Amount " + wo.getGrandTotal()
			+ "\n";
		//
		MOrder mo = wo.getOrder();
		if (mo != null)
		{
			MOrderLine[] ol = mo.getLines();
			if (ol != null)
			{
				for (int i = 0; i < ol.length; i++)
				{
					message += "\n" + ol[i].getQtyOrdered() + " * " + ol[i].getName();
					if (ol[i].getDescription() != null)
						message += " - " + ol[i].getDescription();
					message += " (" + ol[i].getPriceActual() + ") = " + ol[i].getLineNetAmt();
				}	//	line
			}	//	lines
		}	//	order

		String SMTPHost = ctx.getProperty("SMTPHost", "localhost");
		String RequestEMail = ctx.getProperty("RequestEMail");
		String RequestUser = ctx.getProperty("RequestUser");
		String RequestUserPw = ctx.getProperty("RequestUserPw");
		//
		EMail em = new EMail(SMTPHost, RequestEMail, wu.getEmail(), subject, message.toString());
		em.setEMailUser(RequestUser, RequestUserPw);
		//
		String webOrderEMail = ctx.getProperty("webOrderEMail");
		em.addBcc(webOrderEMail);
		//
		em.send();
	}	//	sendEMail


}	//	OrderServlet
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
import org.compiere.model.*;
import org.compiere.www.*;


/**
 *  Web Order.
 *
 *  @author Jorg Janke
 *  @version $Id: OrderServlet.java,v 1.20 2004/05/04 04:50:08 jjanke Exp $
 */
public class OrderServlet extends HttpServlet
{
	/**	Logging						*/
	private Logger			log = Logger.getCLogger(getClass());
	private static Logger	s_log = Logger.getCLogger(OrderServlet.class);

	/** Name						*/
	static public final String			NAME = "orderServlet";

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
			throw new ServletException("OrderServlet.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Order Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
	}   //  destroy

	/*************************************************************************/

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
		doPost (request, response);
	}	//	doGet

	/**
	 *  Process the HTTP Post request
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		log.info("doPost from " + request.getRemoteHost() + " - " + request.getRemoteAddr());
		Properties ctx = JSPEnv.getCtx(request);
		HttpSession session = request.getSession(true);
		session.removeAttribute(JSPEnv.HDR_MESSAGE);

		//	Web User/Basket
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
		WebBasket wb = (WebBasket)session.getAttribute(WebBasket.NAME);

		String url = "/paymentInfo.jsp";
		//	Not logged in
		if (wu == null || !wu.isLoggedIn())
		{
			session.setAttribute("CheckOut", "Y");	//	indicate checkout
			url = "/login.jsp";
		}
		//	Process Existing Order
		else if (processOrder(request, ctx, wu))
			url = "/orders.jsp";
		//	Nothing in basket
		else if (wb == null || wb.getLineCount() == 0)
			url = "/basket.jsp";
		//	Create Order & Payment Info
		else
		{
			WebOrder wo = new WebOrder(wu, wb, ctx);
			//	We have an order - do delete basket & checkout indicator
			if (wo.isInProgress() || wo.isProcessed())
			{
				session.removeAttribute(CheckOutServlet.ATTR_CHECKOUT);
				session.removeAttribute(WebBasket.NAME);
				sendEMail(request, ctx, wo, wu);
			}
			//	If the Order is negative, don't create a payment
			if (wo.getGrandTotal().compareTo(Env.ZERO) > 0)
			{
				session.setAttribute(WebOrder.NAME, wo);
				MPayment p = createPayment (session, ctx, wu, wo);
				if (p == null)
				{
					WUtil.createForwardPage(response, "Payment could not be created", "orders.jsp", 5);
					return;
				}
				else
					session.setAttribute (PaymentServlet.ATTR_PAYMENT, p);
			}
			else
			{
				url = "/orders.jsp";
			}
		}

		log.info ("doPost - Forward to " + url);
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher (url);
		dispatcher.forward (request, response);
	}	//	doPost

	/*************************************************************************/

	/**
	 * 	Create Payment, but don't save it
	 * 	@param session session
	 * 	@param ctx context
	 * 	@param wu web user
	 * 	@param wo Order
	 * 	@return Payment
	 */
	private MPayment createPayment(HttpSession session, Properties ctx,
		WebUser wu,	WebOrder wo)
	{
		//	See PaymentServlet.doGet
		MPayment p = new MPayment(ctx, 0);
		p.setIsSelfService(true);
		p.setAmount (wo.getC_Currency_ID(), wo.getGrandTotal ()); //	for CC selection
		p.setIsOnline (true);
		//	Sales CC Trx
		p.setC_DocType_ID(true);
		p.setTrxType(MPayment.TRXTYPE_Sales);
		p.setTenderType(MPayment.TENDERTYPE_CreditCard);
		//	Order Info
	//	p.setC_Invoice_ID(wo.getInvoice_ID());
		//	BP Info
		p.setBP_BankAccount(wu.getBankAccount());
		//
		return p;
	}	//	createPayment

	/**
	 *	Process Order
	 *	@param request request
	 * 	@param ctx context
	 *	@param wu web user
	 *	@return true if processed
	 */
	private boolean processOrder (HttpServletRequest request, Properties ctx, WebUser wu)
	{
		String para = request.getParameter("C_Order_ID");
		String DocAction = request.getParameter("DocAction");
		if (para == null || para.length() == 0 || DocAction == null || DocAction.length() == 0)
			return false;
		int C_Order_ID = 0;
		try
		{
			C_Order_ID = Integer.parseInt (para);
		}
		catch (NumberFormatException ex)
		{
		}
		if (C_Order_ID == 0)
			return false;

		//	We have a Order No & DocAction
		log.debug("processOrder - C_Order_ID=" + C_Order_ID + ", DocAction=" + DocAction);
		if (!(MOrder.DOCACTION_Complete.equals(DocAction) || MOrder.DOCACTION_Void.equals(DocAction)))
		{
			log.warn("processOrder - C_Order_ID=" + C_Order_ID + ", Invalid DocAction=" + DocAction);
			return true;
		}
		MOrder order = new MOrder (ctx, C_Order_ID);
	//	if (order.isSelfService())
		return order.processOrder (DocAction);
	}	//	processOrder


	/**
	 * 	Send Order EMail.
	 * 	@param request request
	 * 	@param ctx context
	 * 	@param wo web order
	 * 	@param wu web user
	 */
	private void sendEMail (HttpServletRequest request, Properties ctx, WebOrder wo, WebUser wu)
	{
		String subject = "Compiere Web - Order " + wo.getDocumentNo();
		String message = "Thank you for your purchase."
			+ "\nYou can view your order, invoices and open amounts at"
			+ "\nhttp://" + request.getServerName() + request.getContextPath() + "/"
			+ "\n\nOrder " + wo.getDocumentNo() + " - Amount " + wo.getGrandTotal()
			+ "\n";
		//
		MOrder mo = wo.getOrder();
		if (mo != null)
		{
			MOrderLine[] ol = mo.getLines(true);
			for (int i = 0; i < ol.length; i++)
			{
				message += "\n" + ol[i].getQtyOrdered() + " * " + ol[i].getName();
				if (ol[i].getDescription() != null)
					message += " - " + ol[i].getDescription();
				message += " (" + ol[i].getPriceActual() + ") = " + ol[i].getLineNetAmt();
			}	//	line
		}	//	order

		JSPEnv.sendEMail(ctx, wu.getEmail(), subject, message);
	}	//	sendEMail


}	//	OrderServlet
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
import org.compiere.model.*;
import org.compiere.www.*;


/**
 *  Web Order.
 *
 *  @author Jorg Janke
 *  @version $Id: OrderServlet.java,v 1.16 2003/09/05 04:55:42 jjanke Exp $
 */
public class OrderServlet extends HttpServlet
{
	/**	Logging						*/
	private Logger			log = Logger.getLogger(getClass());
	private static Logger	s_log = Logger.getLogger(OrderServlet.class);

	/** Name						*/
	static public final String			NAME = "orderServlet";

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
			throw new ServletException("OrderServlet.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Order Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
	}   //  destroy

	/*************************************************************************/

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
		doPost (request, response);
	}	//	doGet

	/**
	 *  Process the HTTP Post request
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		log.info("doPost from " + request.getRemoteHost() + " - " + request.getRemoteAddr());
		Properties ctx = JSPEnv.getCtx(request);
		HttpSession session = request.getSession(true);
		session.removeAttribute(JSPEnv.HDR_MESSAGE);

		//	Web User/Basket
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
		WebBasket wb = (WebBasket)session.getAttribute(WebBasket.NAME);

		String url = "paymentInfo.jsp";
		//	Not logged in
		if (wu == null || !wu.isLoggedIn())
		{
			session.setAttribute("CheckOut", "Y");	//	indicate checkout
			url = "login.jsp";
		}
		//	Process Existing Order
		else if (processOrder(request, ctx, wu))
			url = "orders.jsp";
		//	Nothing in basket
		else if (wb == null || wb.getLineCount() == 0)
			url = "basket.jsp";
		//	Create Order & Payment Info
		else
		{
			WebOrder wo = new WebOrder(wu, wb, ctx);
			//	We have an order - do delete basket & checkout indicator
			if (wo.isInProgress() || wo.isProcessed())
			{
				session.removeAttribute(CheckOutServlet.ATTR_CHECKOUT);
				session.removeAttribute(WebBasket.NAME);
				sendEMail(request, ctx, wo, wu);
			}
			//	If the Order is negative, don't create a payment
			if (wo.getGrandTotal().compareTo(Env.ZERO) > 0)
			{
				session.setAttribute(WebOrder.NAME, wo);
				MPayment p = createPayment (session, ctx, wu, wo);
				if (p == null)
				{
					WUtil.createForwardPage(response, "Payment could not be created", "orders.jsp");
					return;
				}
				else
					session.setAttribute (PaymentServlet.ATTR_PAYMENT, p);
			}
			else
			{
				url = "orders.jsp";
			}
		}

		log.info ("doPost - Forward to " + url);
		RequestDispatcher dispatcher = getServletContext ().getRequestDispatcher (url);
		dispatcher.forward (request, response);
	}	//	doPost

	/*************************************************************************/

	/**
	 * 	Create Payment, but don't save it
	 * 	@param session session
	 * 	@param ctx context
	 * 	@param wu web user
	 * 	@param wo Order
	 * 	@return Payment
	 */
	private MPayment createPayment(HttpSession session, Properties ctx,
		WebUser wu,	WebOrder wo)
	{
		//	See PaymentServlet.doGet
		MPayment p = new MPayment(ctx, 0);
		p.setIsSelfService(true);
		p.setAmount (wo.getC_Currency_ID(), wo.getGrandTotal ()); //	for CC selection
		p.setIsOnline (true);
		//	Sales CC Trx
		p.setC_DocType_ID(true);
		p.setTrxType(MPayment.TRXTYPE_Sales);
		p.setTenderType(MPayment.TENDERTYPE_CreditCard);
		//	Order Info
	//	p.setC_Invoice_ID(wo.getInvoice_ID());
		//	BP Info
		p.setBP_BankAccount(wu.getBankAccount());
		//
		return p;
	}	//	createPayment

	/**
	 *	Process Order
	 *	@param request request
	 * 	@param ctx context
	 *	@param wu web user
	 *	@return true if processed
	 */
	private boolean processOrder (HttpServletRequest request, Properties ctx, WebUser wu)
	{
		String para = request.getParameter("C_Order_ID");
		String DocAction = request.getParameter("DocAction");
		if (para == null || para.length() == 0 || DocAction == null || DocAction.length() == 0)
			return false;
		int C_Order_ID = 0;
		try
		{
			C_Order_ID = Integer.parseInt (para);
		}
		catch (NumberFormatException ex)
		{
		}
		if (C_Order_ID == 0)
			return false;

		//	We have a Order No & DocAction
		log.debug("processOrder - C_Order_ID=" + C_Order_ID + ", DocAction=" + DocAction);
		if (!(MOrder.DOCACTION_Complete.equals(DocAction) || MOrder.DOCACTION_Void.equals(DocAction)))
		{
			log.warn("processOrder - C_Order_ID=" + C_Order_ID + ", Invalid DocAction=" + DocAction);
			return true;
		}
		MOrder order = new MOrder (ctx, C_Order_ID);
	//	if (order.isSelfService())
		order.process(DocAction);

		return true;
	}	//	processOrder


	/**
	 * 	Send Order EMail.
	 * 	@param request request
	 * 	@param ctx context
	 * 	@param wo web order
	 * 	@param wu web user
	 */
	private void sendEMail (HttpServletRequest request, Properties ctx, WebOrder wo, WebUser wu)
	{
		String subject = "Compiere Web - Order " + wo.getDocumentNo();
		String message = "Thank you for your purchase."
			+ "\nYou can view your order, invoices and open amounts at"
			+ "\nhttp://" + request.getServerName() + request.getContextPath() + "/"
			+ "\n\nOrder " + wo.getDocumentNo() + " - Amount " + wo.getGrandTotal()
			+ "\n";
		//
		MOrder mo = wo.getOrder();
		if (mo != null)
		{
			MOrderLine[] ol = mo.getLines();
			if (ol != null)
			{
				for (int i = 0; i < ol.length; i++)
				{
					message += "\n" + ol[i].getQtyOrdered() + " * " + ol[i].getName();
					if (ol[i].getDescription() != null)
						message += " - " + ol[i].getDescription();
					message += " (" + ol[i].getPriceActual() + ") = " + ol[i].getLineNetAmt();
				}	//	line
			}	//	lines
		}	//	order

		String SMTPHost = ctx.getProperty("SMTPHost", "localhost");
		String RequestEMail = ctx.getProperty("RequestEMail");
		String RequestUser = ctx.getProperty("RequestUser");
		String RequestUserPw = ctx.getProperty("RequestUserPw");
		//
		EMail em = new EMail(SMTPHost, RequestEMail, wu.getEmail(), subject, message.toString());
		em.setEMailUser(RequestUser, RequestUserPw);
		//
		String webOrderEMail = ctx.getProperty("webOrderEMail");
		em.addBcc(webOrderEMail);
		//
		em.send();
	}	//	sendEMail


}	//	OrderServlet
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

import org.compiere.model.*;
import org.compiere.util.*;


/**
 *  Web Order.
 *
 *  @author Jorg Janke
 *  @version $Id: OrderServlet.java,v 1.24 2004/09/10 02:54:23 jjanke Exp $
 */
public class OrderServlet extends HttpServlet
{
	/**	Logging						*/
	private Logger			log = Logger.getCLogger(getClass());
//	private static Logger	s_log = Logger.getCLogger(OrderServlet.class);

	/** Name						*/
	static public final String			NAME = "orderServlet";

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
			throw new ServletException("OrderServlet.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Order Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
	}   //  destroy

	
	/**************************************************************************
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
		doPost (request, response);
	}	//	doGet

	/**
	 *  Process the HTTP Post request
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		log.info("doPost from " + request.getRemoteHost() + " - " + request.getRemoteAddr());
		Properties ctx = JSPEnv.getCtx(request);
		HttpSession session = request.getSession(true);
		session.removeAttribute(JSPEnv.HDR_MESSAGE);

		//	Web User/Basket
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
		WebBasket wb = (WebBasket)session.getAttribute(WebBasket.NAME);
		MOrder order = null;
		
		boolean done = false;
		String url = "/paymentInfo.jsp";
		//	Not logged in
		if (wu == null || !wu.isLoggedIn())
		{
			session.setAttribute("CheckOut", "Y");	//	indicate checkout
			url = "/login.jsp";
			done = true;
		}
		else
			order = getOrder(request, ctx);
		
		//	We have an Order
		if (!done && order != null)
		{
			if (processOrder(request, order))
				url = "/orders.jsp";
			else
			{
				WebOrder wo = new WebOrder (order);
				MPayment p = createPayment (session, ctx, wu, wo);
				if (p != null)
				{
					session.setAttribute (PaymentServlet.ATTR_PAYMENT, p);
					session.setAttribute(WebOrder.NAME, wo);
				}
				else
					url = "/orders.jsp";
			}
			done = true;
		}
		
		//	Nothing in basket
		if (!done && (wb == null || wb.getLineCount() == 0))
		{
			url = "/basket.jsp";
			done = true;
		}
		//	Create Order & Payment Info
		if (!done)
		{
			WebOrder wo = new WebOrder(wu, wb, ctx);
			//	We have an order - do delete basket & checkout indicator
			if (wo.isInProgress() || wo.isCompleted())
			{
				session.removeAttribute(CheckOutServlet.ATTR_CHECKOUT);
				session.removeAttribute(WebBasket.NAME);
				sendEMail(request, ctx, wo, wu);
			}
			//	If the Order is negative, don't create a payment
			if (wo.getGrandTotal().compareTo(Env.ZERO) > 0)
			{
				session.setAttribute(WebOrder.NAME, wo);
				MPayment p = createPayment (session, ctx, wu, wo);
				if (p == null)
				{
					WebUtil.createForwardPage(response, "Payment could not be created", "orders.jsp", 5);
					return;
				}
				else
					session.setAttribute (PaymentServlet.ATTR_PAYMENT, p);
			}
			else
			{
				url = "/orders.jsp";
			}
		}

		log.info ("doPost - Forward to " + url);
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher (url);
		dispatcher.forward (request, response);
	}	//	doPost

	
	/**************************************************************************
	 * 	Create Payment, but don't save it
	 * 	@param session session
	 * 	@param ctx context
	 * 	@param wu web user
	 * 	@param wo Order
	 * 	@return Payment
	 */
	private MPayment createPayment(HttpSession session, Properties ctx,
		WebUser wu,	WebOrder wo)
	{
		//	See PaymentServlet.doGet
		MPayment p = new MPayment(ctx, 0);
		p.setIsSelfService(true);
		p.setAmount (wo.getC_Currency_ID(), wo.getGrandTotal ()); //	for CC selection
		p.setIsOnline (true);
		//	Sales CC Trx
		p.setC_DocType_ID(true);
		p.setTrxType(MPayment.TRXTYPE_Sales);
		p.setTenderType(MPayment.TENDERTYPE_CreditCard);
		//	Order Info
		p.setC_Order_ID(wo.getC_Order_ID());
		//	BP Info
		p.setBP_BankAccount(wu.getBankAccount());
		//
		return p;
	}	//	createPayment

	/**
	 *	Get Order
	 *	@param request request
	 * 	@param ctx context
	 *	@return true if processed
	 */
	private MOrder getOrder (HttpServletRequest request, Properties ctx)
	{
		//	Order
		String para = request.getParameter("C_Order_ID");
		if (para == null || para.length() == 0)
			return null;
		int C_Order_ID = 0;
		try
		{
			C_Order_ID = Integer.parseInt (para);
		}
		catch (NumberFormatException ex)
		{
		}
		if (C_Order_ID == 0)
			return null;

		log.debug("getOrder - C_Order_ID=" + C_Order_ID);
		return new MOrder (ctx, C_Order_ID);
	}	//	getOrder
	
	
	/**
	 *	Process Order
	 *	@param request request
	 * 	@param ctx context
	 *	@param wu web user
	 *	@return true if processed/ok
	 */
	private boolean processOrder (HttpServletRequest request, MOrder order)
	{
		//	Doc Action
		String DocAction = request.getParameter("DocAction");
		if (DocAction == null || DocAction.length() == 0)
			return false;

		MDocType dt = MDocType.get(order.getCtx(), order.getC_DocType_ID());
		if (!order.isSOTrx() 
			|| order.getGrandTotal().compareTo(Env.ZERO) <= 0
			|| !MDocType.DOCBASETYPE_SalesOrder.equals(dt.getDocBaseType()))
		{
			log.warn("processOrder - Not a valid Sales Order " + order);
			return true;
		}

		//	We have a Order No & DocAction
		log.debug("processOrder - DocAction=" + DocAction);
		if (!MOrder.DOCACTION_Void.equals(DocAction))
		{
			//	Do not complete Prepayment
			if (MOrder.STATUS_WaitingPayment.equals(order.getDocStatus()))
				return false;
			if (MDocType.DOCSUBTYPESO_PrepayOrder.equals(dt.getDocSubTypeSO()))
				return false;
			if (!MOrder.DOCACTION_Complete.equals(DocAction))
			{
				log.warn("processOrder - Invalid DocAction=" + DocAction);
				return true;
			}
		}
		order.setDocAction (DocAction, true);	//	force creation
		boolean ok = order.processIt (DocAction);
		order.save();
		return ok;
	}	//	processOrder


	/**
	 * 	Send Order EMail.
	 * 	@param request request
	 * 	@param ctx context
	 * 	@param wo web order
	 * 	@param wu web user
	 */
	private void sendEMail (HttpServletRequest request, Properties ctx, WebOrder wo, WebUser wu)
	{
		String subject = "Compiere Web - Order " + wo.getDocumentNo();
		String message = "Thank you for your purchase."
			+ "\nYou can view your order, invoices and open amounts at"
			+ "\nhttp://" + request.getServerName() + request.getContextPath() + "/"
			+ "\n\nOrder " + wo.getDocumentNo() + " - Amount " + wo.getGrandTotal()
			+ "\n";
		//
		MOrder mo = wo.getOrder();
		if (mo != null)
		{
			MOrderLine[] ol = mo.getLines(true);
			for (int i = 0; i < ol.length; i++)
			{
				message += "\n" + ol[i].getQtyOrdered() + " * " + ol[i].getName();
				if (ol[i].getDescription() != null)
					message += " - " + ol[i].getDescription();
				message += " (" + ol[i].getPriceActual() + ") = " + ol[i].getLineNetAmt();
			}	//	line
		}	//	order

		JSPEnv.sendEMail(ctx, wu.getEmail(), subject, message);
	}	//	sendEMail


}	//	OrderServlet
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
import org.compiere.model.*;
import org.compiere.www.*;


/**
 *  Web Order.
 *
 *  @author Jorg Janke
 *  @version $Id: OrderServlet.java,v 1.14 2003/07/19 05:21:44 jjanke Exp $
 */
public class OrderServlet extends HttpServlet
{
	/**	Logging						*/
	private Logger			log = Logger.getLogger(getClass());
	private static Logger	s_log = Logger.getLogger(OrderServlet.class);

	/** Name						*/
	static public final String			NAME = "orderServlet";

	public static final String 			BANKACCOUNT_ATTR = "bankAccount";


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
			throw new ServletException("OrderServlet.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Order Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
	}   //  destroy

	/*************************************************************************/

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
		doPost (request, response);
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

		//	Web User/Basket
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
		WebBasket wb = (WebBasket)session.getAttribute(WebBasket.NAME);

		String url = "paymentInfo.jsp";
		//	Nothing in basket
		if (wb == null || wb.getLineCount() == 0)
			url = "basket.jsp";
		//	Not logged in
		else if (wu == null || !wu.isLoggedIn())
		{
			session.setAttribute("CheckOut", "Y");	//	indicate checkout
			url = "login.jsp";
		}
		//	Create Order & Payment Info
		else
		{
			WebOrder wo = new WebOrder(wu, wb, ctx);
			//	We have an order - do delete basket & checkout indicator
			if (wo.isInProgress() || wo.isProcessed())
			{
				session.removeAttribute(CheckOutServlet.ATTR_CHECKOUT);
				session.removeAttribute(WebBasket.NAME);
				sendEMail(request, ctx, wo, wu);
			}
			//	If the Order is negative, don't create a payment
			if (wo.getGrandTotal().compareTo(Env.ZERO) > 0)
			{
				session.setAttribute(WebOrder.NAME, wo);
				MPayment p = createPayment (session, ctx, wu, wo);
				if (p == null)
				{
					WUtil.createForwardPage(response, "Payment could not be created", "orders.jsp");
					return;
				}
				else
					session.setAttribute (PaymentServlet.ATTR_PAYMENT, p);
			}
			else
			{
				url = "orders.jsp";
			}
		}

		log.info ("doPost - Forward to " + url);
		RequestDispatcher dispatcher = getServletContext ().getRequestDispatcher (url);
		dispatcher.forward (request, response);
	}	//	doPost


	/**
	 * 	Create Payment, but don't save it
	 * 	@param session session
	 * 	@param ctx context
	 * 	@param wu web user
	 * 	@param wo Order
	 * 	@return Payment
	 */
	private MPayment createPayment(HttpSession session, Properties ctx, WebUser wu,
		WebOrder wo)
	{
		//	See PaymentServlet.doGet
		MPayment p = new MPayment(ctx, 0);
		//	Bank Account
		MBankAccount ba = JSPEnv.getBankAccount(session, ctx);
		if (ba == null)			//	may not be defined defined
			return null;
		//
		ba.setPayAmt (wo.getGrandTotal ()); //	for CC selection
		p.setC_BankAccount_ID (ba.getC_BankAccount_ID ());
		p.setC_Currency_ID (ba.getC_Currency_ID ());
		p.setIsOnline (true);
		//	Sales CC Trx
		p.setC_DocType_ID(true);
		p.setTrxType(MPayment.TRXTYPE_Sales);
		p.setTenderType(MPayment.TENDERTYPE_CreditCard);
		//	Order Info
		p.setPayAmt(wo.getGrandTotal());
	//	p.setC_Invoice_ID(wo.getInvoice_ID());
		//	BP Info
		p.setBP_BankAccount(wu.getBankAccount());
		//
		return p;
	}	//	createPayment

	/**
	 * 	Send Order EMail.
	 * 	@param request request
	 * 	@param ctx context
	 * 	@param wo web order
	 * 	@param wu web user
	 */
	private void sendEMail (HttpServletRequest request, Properties ctx, WebOrder wo, WebUser wu)
	{
		String subject = "Compiere Web - Order " + wo.getDocumentNo();
		String message = "Thank you for your purchase."
			+ "\nYou can view your order, invoices and open amounts at"
			+ "\nhttp://" + request.getServerName() + request.getContextPath() + "/"
			+ "\n\nOrder " + wo.getDocumentNo() + " - Amount " + wo.getGrandTotal()
			+ "\n";
		//
		MOrder mo = wo.getOrder();
		if (mo != null)
		{
			MOrderLine[] ol = mo.getLines();
			if (ol != null)
			{
				for (int i = 0; i < ol.length; i++)
				{
					message += "\n" + ol[i].getQtyOrdered() + " * " + ol[i].getName();
					if (ol[i].getDescription() != null)
						message += " - " + ol[i].getDescription();
					message += " (" + ol[i].getPriceActual() + ") = " + ol[i].getLineNetAmt();
				}	//	line
			}	//	lines
		}	//	order

		String SMTPHost = ctx.getProperty("SMTPHost", "localhost");
		String RequestEMail = ctx.getProperty("RequestEMail");
		String RequestUser = ctx.getProperty("RequestUser");
		String RequestUserPw = ctx.getProperty("RequestUserPw");
		//
		EMail em = new EMail(SMTPHost, RequestEMail, wu.getEmail(), subject, message.toString());
		em.setEMailUser(RequestUser, RequestUserPw);
		//
		String webOrderEMail = ctx.getProperty("webOrderEMail");
		em.addBcc(webOrderEMail);
		//
		em.send();
	}	//	sendEMail


}	//	OrderServlet
