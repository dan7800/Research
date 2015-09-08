/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.info)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/

package com.vlee.servlet.ecommerce;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

import com.vlee.bean.distribution.EditSalesOrderSession;
import com.vlee.ejb.customer.SalesOrderIndexBean;
import com.vlee.ejb.customer.SalesOrderIndexNut;
import com.vlee.ejb.customer.SalesOrderIndexObject;
import com.vlee.ejb.ecommerce.EPaymentInboxBean;
import com.vlee.ejb.ecommerce.EPaymentInboxHome;
import com.vlee.ejb.ecommerce.EPaymentInboxNut;
import com.vlee.ejb.ecommerce.EPaymentInboxObject;
import com.vlee.ejb.ecommerce.EStoreObject;
import com.vlee.util.Log;
import com.vlee.util.QueryObject;

public class DoEPaymentMobileMoneyRequest extends HttpServlet
{
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException
	{
		doPost(req, res);
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException
	{
		mobileMoney(req, res);
		
		//fnNextPage(req, res);
	}
	
	private boolean mobileMoney(HttpServletRequest req, HttpServletResponse res)
	{
		String mmResponseBody = "";

		try
		{
			String mm_cmd = (String) req.getParameter("mm_cmd");
			String mm_merchantepursendo = (String) req.getParameter("mm_merchantepursendo");
			String mmm_tran_id = (String) req.getParameter("mmm_tran_id");
			String TranType = (String) req.getParameter("TranType");
			String MobileNo = (String) req.getParameter("MobileNo");
			String Amt = (String) req.getParameter("Amt");
			String icNumber = (String) req.getParameter("icNumber");

			String MobileNoPlus = "+6" + MobileNo;

			Log.printVerbose("mm_cmd :"+mm_cmd);
			Log.printVerbose("mm_merchantepursendo :"+mm_merchantepursendo);
			Log.printVerbose("mmm_tran_id :"+mmm_tran_id);
			Log.printVerbose("TranType :"+TranType);
			Log.printVerbose("MobileNo :"+MobileNoPlus);
			Log.printVerbose("Amt :"+Amt);
			Log.printVerbose("icNumber :"+icNumber);


			NameValuePair form_data[] = new NameValuePair[10];
			form_data[0] = new NameValuePair("mm_cmd", mm_cmd);
			form_data[1] = new NameValuePair("mmm_merchantepurseno", mm_merchantepursendo);
			form_data[2] = new NameValuePair("mmm_tran_id", mmm_tran_id);
			form_data[3] = new NameValuePair("Trantype", TranType);
			form_data[4] = new NameValuePair("MobileNo", MobileNoPlus);
			form_data[5] = new NameValuePair("Amt", Amt);
			form_data[6] = new NameValuePair("mmm_desc", "Testing");
			form_data[7] = new NameValuePair("mmm_rev_tran_id", "");
			form_data[8] = new NameValuePair("IC", icNumber);
			form_data[9] = new NameValuePair("ECashNo", "");
		
			// Insert into inbox
			EPaymentInboxHome inboxHome = EPaymentInboxNut.getHome();
			EPaymentInboxObject inboxObj = new EPaymentInboxObject();
			inboxObj.merchant_id = mm_merchantepursendo;
			inboxObj.merchant_tranx_id = new Integer(mmm_tran_id);
			inboxObj.payment_mode = EPaymentInboxBean.PAYMENT_MOBILEMONEY;
			inboxObj.property1 = mm_cmd;
			inboxObj.property2 = TranType;
			inboxObj.property3 = MobileNoPlus;
			inboxObj.property4 = icNumber;

			inboxHome.create(inboxObj);

			//String url = "http://60.51.164.2/mm_weblink/WEBLINK_Tran_Request.aspx";
			String url = "https://mmweb.mobile-money.com/mm_weblink/WEBLINK_Tran_Request.aspx";

			System.setProperty("javax.net.ssl.trustStore","/usr/java/j2sdk/jre/lib/security/MobileMoneyCacerts");

			HttpMethod method = new GetMethod();
			method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
			method.setRequestHeader("Cookie", "special-cookie=value");
			HttpClient client = new HttpClient();
			PostMethod post = new PostMethod(url);
			post.setRequestBody(form_data);
			post.setContentChunked(true);

			Log.printVerbose("Posted to Mobile Money server");

			try {
				client.executeMethod(post);
				
				if (post.getStatusCode() < 300)
				{
					Log.printVerbose("Http Success");

					String mmResponse = post.getStatusText();
			
					mmResponseBody = post.getResponseBodyAsString();

					Log.printVerbose("mmResponse: " + mmResponse);
		
					Log.printVerbose("mmResponseBody: " + mmResponseBody);

					fnNextPage(req, res, mmResponseBody);

					return true;
					
				} else
				{
					Log.printVerbose("HTTP Error");
					// out.println("HTTP Error");
				}

			} finally {

				Log.printVerbose("Checkpoint 1");
				post.releaseConnection();
			}
			
		} catch (Exception e)
		{
			Log.printVerbose("Checkpoint 2");

			e.printStackTrace();
		}

		fnNextPage(req, res, mmResponseBody);
		return false;
	}
	
	private void fnNextPage(HttpServletRequest request, HttpServletResponse response, String mmResponseBody)
	{
		try
		{
			Log.printVerbose("In fnNextPage: mmResponseBody "+mmResponseBody);

			HttpSession session = request.getSession(true);
			
			String SOPkid = "0";
			String tempStr = (String) request.getParameter("mmm_tran_id");
			if (tempStr != null) SOPkid = tempStr;
			
			EStoreObject objStore = (EStoreObject) session.getAttribute("objStore");
			EditSalesOrderSession csos = new EditSalesOrderSession(objStore.ecom_userId);
			csos.loadSalesOrder(new Long(SOPkid));
			session.setAttribute("dist-sales-order-session", csos);

			fnSalesOrderAmountTotal(request, response, SOPkid);

			String nextPage = "/member_checkout_receipt.jsp";

			if(mmResponseBody.equals("0000 : OK"))
			{
				Log.printVerbose("mmResponseBody OK");

				request.setAttribute("status", "pending");
				request.setAttribute("transError", "");
			}
			else
			{
				Log.printVerbose("mmResponseBody not OK");

				request.setAttribute("status", "fail");

				String mmResponseCode = mmResponseBody.substring(0, 4);

				Log.printVerbose("mmResponseBody.substring "+mmResponseCode);

				if(mmResponseCode.equals("5214"))
				{
                                	request.setAttribute("transError", "Mobile Money Out of Service");
				}
				else if (mmResponseCode.equals("5231"))
				{
					request.setAttribute("transError", "Mobile Money Internal Error");
				}
				else if (mmResponseCode.equals("5321"))
                                {
                                        request.setAttribute("transError", "Duplicate Order ID with Mobile Money");
                                }
				else 
                                {
                                        request.setAttribute("transError", "Invalid Mobile Number or I/C Number");
                                }
			}

			getServletContext().getRequestDispatcher(response.encodeRedirectURL(nextPage)).forward(request, response);
			
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void fnSalesOrderAmountTotal(HttpServletRequest req,
			HttpServletResponse res, String SOGuid) throws Exception
	{
		
			HttpSession session = req.getSession(true);
			
			QueryObject query = new QueryObject(new String[] { SalesOrderIndexBean.GUID + " = '" + SOGuid + "' " });
			Vector vecSalesOrder = new Vector(SalesOrderIndexNut.getObjects(query));
			BigDecimal AmtTotal = new BigDecimal(0);
			for (int cnt1 = 0; cnt1 < vecSalesOrder.size(); cnt1++)
			{				
				SalesOrderIndexObject soObj = (SalesOrderIndexObject) vecSalesOrder.get(cnt1);
				EStoreObject objStore = (EStoreObject) session.getAttribute("objStore");
				EditSalesOrderSession csos = new EditSalesOrderSession(objStore.ecom_userId);
				csos.loadSalesOrder(soObj.pkid);

				AmtTotal = AmtTotal.add(csos.getBillAmount());

			}

			session.setAttribute("AmtTotal", String.valueOf(AmtTotal));

			session.setAttribute("vecSalesOrder", null);
			session.setAttribute("vecSalesOrder", vecSalesOrder);	
	}
}
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.info)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/

package com.vlee.servlet.ecommerce;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

import com.vlee.bean.distribution.EditSalesOrderSession;
import com.vlee.ejb.customer.SalesOrderIndexBean;
import com.vlee.ejb.customer.SalesOrderIndexNut;
import com.vlee.ejb.customer.SalesOrderIndexObject;
import com.vlee.ejb.ecommerce.EPaymentInboxBean;
import com.vlee.ejb.ecommerce.EPaymentInboxHome;
import com.vlee.ejb.ecommerce.EPaymentInboxNut;
import com.vlee.ejb.ecommerce.EPaymentInboxObject;
import com.vlee.ejb.ecommerce.EStoreObject;
import com.vlee.util.Log;
import com.vlee.util.QueryObject;

public class DoEPaymentMobileMoneyRequest extends HttpServlet
{
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException
	{
		doPost(req, res);
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException
	{
		mobileMoney(req, res);
		
		fnNextPage(req, res);
	}
	
	private boolean mobileMoney(HttpServletRequest req, HttpServletResponse res)
	{
		try
		{
			String mm_cmd = (String) req.getParameter("mm_cmd");
			String mm_merchantepursendo = (String) req.getParameter("mm_merchantepursendo");
			String mmm_tran_id = (String) req.getParameter("mmm_tran_id");
			String TranType = (String) req.getParameter("TranType");
			String MobileNo = (String) req.getParameter("MobileNo");
			String Amt = (String) req.getParameter("Amt");

			String MobileNoPlus = "+" + MobileNo;

			NameValuePair form_data[] = new NameValuePair[10];
			form_data[0] = new NameValuePair("mm_cmd", mm_cmd);
			form_data[1] = new NameValuePair("mmm_merchantepurseno", mm_merchantepursendo);
			form_data[2] = new NameValuePair("mmm_tran_id", mmm_tran_id);
			form_data[3] = new NameValuePair("Trantype", TranType);
			form_data[4] = new NameValuePair("MobileNo", MobileNoPlus);
			form_data[5] = new NameValuePair("Amt", Amt);
			form_data[6] = new NameValuePair("mmm_desc", "Testing");
			form_data[7] = new NameValuePair("mmm_rev_tran_id", "");
			form_data[8] = new NameValuePair("IC", "");
			form_data[9] = new NameValuePair("ECashNo", "");

			// Insert into inbox
			EPaymentInboxHome inboxHome = EPaymentInboxNut.getHome();
			EPaymentInboxObject inboxObj = new EPaymentInboxObject();
			inboxObj.merchant_id = new Integer(mm_merchantepursendo);
			inboxObj.merchant_tranx_id = new Integer(mmm_tran_id);
			inboxObj.payment_mode = EPaymentInboxBean.PAYMENT_MOBILEMONEY;
			inboxObj.property1 = mm_cmd;
			inboxObj.property2 = TranType;
			inboxObj.property3 = MobileNoPlus;

			inboxHome.create(inboxObj);

			//String url = "http://60.51.164.2/mm_weblink/WEBLINK_Tran_Request.aspx";
			String url = "https://mmweb.mobile-money.com/mm_weblink/WEBLINK_Tran_Request.aspx";

			System.setProperty("javax.net.ssl.trustStore","/usr/java/j2sdk/jre/lib/security/MobileMoneyCacerts");

			HttpMethod method = new GetMethod();
			method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
			method.setRequestHeader("Cookie", "special-cookie=value");
			HttpClient client = new HttpClient();
			PostMethod post = new PostMethod(url);
			post.setRequestBody(form_data);
			post.setContentChunked(true);

			Log.printVerbose("Posted to Mobile Money server");

			try {
				client.executeMethod(post);
				
				if (post.getStatusCode() < 300)
				{
					Log.printVerbose("Http Success");

					String mmResponse = post.getStatusText();

					Log.printVerbose("mmResponse: " + mmResponse);

					return true;
				} else
				{
					Log.printVerbose("HTTP Error");
					// out.println("HTTP Error");
				}

			} finally {
				post.releaseConnection();
			}
			
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}
	
	private void fnNextPage(HttpServletRequest request, HttpServletResponse response)
	{
		try
		{
			HttpSession session = request.getSession(true);
			
			String SOPkid = "0";
			String tempStr = (String) request.getParameter("mmm_tran_id");
			if (tempStr != null) SOPkid = tempStr;
			
			EStoreObject objStore = (EStoreObject) session.getAttribute("objStore");
			EditSalesOrderSession csos = new EditSalesOrderSession(objStore.ecom_userId);
			csos.loadSalesOrder(new Long(SOPkid));
			session.setAttribute("dist-sales-order-session", csos);

			fnSalesOrderAmountTotal(request, response, SOPkid);

			String nextPage = "/member_checkout_receipt.jsp";
			
			request.setAttribute("status", "pending");
			
			getServletContext().getRequestDispatcher(response.encodeRedirectURL(nextPage)).forward(request, response);
			
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void fnSalesOrderAmountTotal(HttpServletRequest req,
			HttpServletResponse res, String SOGuid) throws Exception
	{
		
			HttpSession session = req.getSession(true);
			
			QueryObject query = new QueryObject(new String[] { SalesOrderIndexBean.GUID + " = '" + SOGuid + "' " });
			Vector vecSalesOrder = new Vector(SalesOrderIndexNut.getObjects(query));
			BigDecimal AmtTotal = new BigDecimal(0);
			for (int cnt1 = 0; cnt1 < vecSalesOrder.size(); cnt1++)
			{				
				SalesOrderIndexObject soObj = (SalesOrderIndexObject) vecSalesOrder.get(cnt1);
				EStoreObject objStore = (EStoreObject) session.getAttribute("objStore");
				EditSalesOrderSession csos = new EditSalesOrderSession(objStore.ecom_userId);
				csos.loadSalesOrder(soObj.pkid);

				AmtTotal = AmtTotal.add(csos.getBillAmount());

			}

			session.setAttribute("AmtTotal", String.valueOf(AmtTotal));

			session.setAttribute("vecSalesOrder", null);
			session.setAttribute("vecSalesOrder", vecSalesOrder);	
	}
}
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.info)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/

package com.vlee.servlet.ecommerce;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

import com.vlee.bean.distribution.EditSalesOrderSession;
import com.vlee.ejb.customer.SalesOrderIndexBean;
import com.vlee.ejb.customer.SalesOrderIndexNut;
import com.vlee.ejb.customer.SalesOrderIndexObject;
import com.vlee.ejb.ecommerce.EPaymentInboxBean;
import com.vlee.ejb.ecommerce.EPaymentInboxHome;
import com.vlee.ejb.ecommerce.EPaymentInboxNut;
import com.vlee.ejb.ecommerce.EPaymentInboxObject;
import com.vlee.ejb.ecommerce.EStoreObject;
import com.vlee.util.Log;
import com.vlee.util.QueryObject;

public class DoEPaymentMobileMoneyRequest extends HttpServlet
{
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException
	{
		doPost(req, res);
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException
	{
		mobileMoney(req, res);
		
		fnNextPage(req, res);
	}
	
	private boolean mobileMoney(HttpServletRequest req, HttpServletResponse res)
	{
		try
		{
			String mm_cmd = (String) req.getParameter("mm_cmd");
			String mm_merchantepursendo = (String) req.getParameter("mm_merchantepursendo");
			String mmm_tran_id = (String) req.getParameter("mmm_tran_id");
			String TranType = (String) req.getParameter("TranType");
			String MobileNo = (String) req.getParameter("MobileNo");
			String Amt = (String) req.getParameter("Amt");

			String MobileNoPlus = "+" + MobileNo;

			NameValuePair form_data[] = new NameValuePair[10];
			form_data[0] = new NameValuePair("mm_cmd", mm_cmd);
			form_data[1] = new NameValuePair("mmm_merchantepurseno", mm_merchantepursendo);
			form_data[2] = new NameValuePair("mmm_tran_id", mmm_tran_id);
			form_data[3] = new NameValuePair("Trantype", TranType);
			form_data[4] = new NameValuePair("MobileNo", MobileNoPlus);
			form_data[5] = new NameValuePair("Amt", Amt);
			form_data[6] = new NameValuePair("mmm_desc", "Testing");
			form_data[7] = new NameValuePair("mmm_rev_tran_id", "");
			form_data[8] = new NameValuePair("IC", "");
			form_data[9] = new NameValuePair("ECashNo", "");

			// Insert into inbox
			EPaymentInboxHome inboxHome = EPaymentInboxNut.getHome();
			EPaymentInboxObject inboxObj = new EPaymentInboxObject();
			inboxObj.merchant_id = new Integer(mm_merchantepursendo);
			inboxObj.merchant_tranx_id = new Integer(mmm_tran_id);
			inboxObj.payment_mode = EPaymentInboxBean.PAYMENT_MOBILEMONEY;
			inboxObj.property1 = mm_cmd;
			inboxObj.property2 = TranType;
			inboxObj.property3 = MobileNoPlus;

			inboxHome.create(inboxObj);

			//String url = "http://60.51.164.2/mm_weblink/WEBLINK_Tran_Request.aspx";
			String url = "https://mmweb.mobile-money.com/mm_weblink/WEBLINK_Tran_Request.aspx";

			System.setProperty("javax.net.ssl.trustStore","/usr/java/j2sdk/jre/lib/security/MobileMoneyCacerts");

			HttpMethod method = new GetMethod();
			method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
			method.setRequestHeader("Cookie", "special-cookie=value");
			HttpClient client = new HttpClient();
			PostMethod post = new PostMethod(url);
			post.setRequestBody(form_data);
			post.setContentChunked(true);

			Log.printVerbose("Posted to Mobile Money server");

			try {
				client.executeMethod(post);
				
				if (post.getStatusCode() < 300)
				{
					Log.printVerbose("Http Success");

					String mmResponse = post.getStatusText();

					Log.printVerbose("mmResponse: " + mmResponse);

					return true;
				} else
				{
					Log.printVerbose("HTTP Error");
					// out.println("HTTP Error");
				}

			} finally {
				post.releaseConnection();
			}
			
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}
	
	private void fnNextPage(HttpServletRequest request, HttpServletResponse response)
	{
		try
		{
			HttpSession session = request.getSession(true);
			
			String SOPkid = "0";
			String tempStr = (String) request.getParameter("mmm_tran_id");
			if (tempStr != null) SOPkid = tempStr;
			
			EStoreObject objStore = (EStoreObject) session.getAttribute("objStore");
			EditSalesOrderSession csos = new EditSalesOrderSession(objStore.ecom_userId);
			csos.loadSalesOrder(new Long(SOPkid));
			session.setAttribute("dist-sales-order-session", csos);

			fnSalesOrderAmountTotal(request, response, SOPkid);

			String nextPage = "/member_checkout_receipt.jsp";
			
			request.setAttribute("status", "pending");
			
			getServletContext().getRequestDispatcher(response.encodeRedirectURL(nextPage)).forward(request, response);
			
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void fnSalesOrderAmountTotal(HttpServletRequest req,
			HttpServletResponse res, String SOGuid) throws Exception
	{
		
			HttpSession session = req.getSession(true);
			
			QueryObject query = new QueryObject(new String[] { SalesOrderIndexBean.GUID + " = '" + SOGuid + "' " });
			Vector vecSalesOrder = new Vector(SalesOrderIndexNut.getObjects(query));
			BigDecimal AmtTotal = new BigDecimal(0);
			for (int cnt1 = 0; cnt1 < vecSalesOrder.size(); cnt1++)
			{				
				SalesOrderIndexObject soObj = (SalesOrderIndexObject) vecSalesOrder.get(cnt1);
				EStoreObject objStore = (EStoreObject) session.getAttribute("objStore");
				EditSalesOrderSession csos = new EditSalesOrderSession(objStore.ecom_userId);
				csos.loadSalesOrder(soObj.pkid);

				AmtTotal = AmtTotal.add(csos.getBillAmount());

			}

			session.setAttribute("AmtTotal", String.valueOf(AmtTotal));

			session.setAttribute("vecSalesOrder", null);
			session.setAttribute("vecSalesOrder", vecSalesOrder);	
	}
}
