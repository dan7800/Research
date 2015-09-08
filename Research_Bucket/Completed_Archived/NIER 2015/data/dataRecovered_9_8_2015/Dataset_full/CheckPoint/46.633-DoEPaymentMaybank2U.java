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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.vlee.bean.application.AppConfigManager;
import com.vlee.bean.distribution.CreateSalesOrderSession;
import com.vlee.bean.distribution.EditSalesOrderSession;
import com.vlee.ejb.customer.SalesOrderIndex;
import com.vlee.ejb.customer.SalesOrderIndexBean;
import com.vlee.ejb.customer.SalesOrderIndexNut;
import com.vlee.ejb.customer.SalesOrderIndexObject;
import com.vlee.ejb.ecommerce.EPaymentConfigNut;
import com.vlee.ejb.ecommerce.EPaymentConfigObject;
import com.vlee.ejb.ecommerce.EPaymentInboxBean;
import com.vlee.ejb.ecommerce.EPaymentInboxHome;
import com.vlee.ejb.ecommerce.EPaymentInboxNut;
import com.vlee.ejb.ecommerce.EPaymentInboxObject;
import com.vlee.ejb.ecommerce.EStoreObject;

import com.vlee.util.TimeFormat;
import com.vlee.webservice.maybank2u.databinding.XmlQueryClient;


import com.vlee.util.Log;
import com.vlee.util.QueryObject;

public class DoEPaymentMaybank2U extends HttpServlet
{
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
		doPost(req, res);
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
//		String status = (String) req.getParameter("status");
//		if (status.equals("00"))
//		{
//			String paymentStatus = queryStatusMaybank2u(req);
//			if(paymentStatus.equals("00"))
//			{
//				updatePaymentInbox(req, paymentStatus);
//				updatePayment(req);
//			} else 
//			{
//				updatePaymentInbox(req, paymentStatus);
//			}
//			req.setAttribute("status", "success");
//		} else
//		{
//			req.setAttribute("status", "fail");
//		}
		
		System.out.println("DoEPaymentMaybank2u called by Maybank2u");       
		
		String paymentStatus = queryStatusMaybank2u(req);
		String guid = req.getParameter("referenceNo");
		
		if(paymentStatus.equals("00"))
		{
			System.out.println("DoEPaymentMaybank2u success");       
			
			paymentStatus = "success";
			updatePaymentInbox(req, paymentStatus);
			updatePayment(req, guid, "10017");

			System.out.println("Updated SO");
			
			req.setAttribute("status", "success");

			try{ fnSalesOrderAmountTotal(req,res,guid);}
                        catch(Exception ex){ ex.printStackTrace();}

		} else 
		{
			System.out.println("DoEPaymentMaybank2u fail");       
			
			paymentStatus = "fail";
			req.setAttribute("status", "fail");
		}

		String nextPage = "/member_checkout_receipt.jsp";

		getServletContext().getRequestDispatcher(res.encodeRedirectURL(nextPage)).forward(req, res);

		//RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/checkout-receipt-page");
		//System.out.println(dispatcher.toString());

		//dispatcher.forward(req, res);
	}

	private void updatePaymentInbox(HttpServletRequest req, String status)
	{
		try
		{
			String payeeCode = (String) req.getParameter("payeeCode");
			String referenceNo = (String) req.getParameter("referenceNo");
			String accountNo = (String) req.getParameter("accountNo");
			String transAmount = (String) req.getParameter("transAmount");
			String approvalCode = (String) req.getParameter("approvalCode");
			String bankRefNo = (String) req.getParameter("bankRefNo");
			String transDate = (String) req.getParameter("transDate");
			
			EPaymentInboxHome inboxHome = EPaymentInboxNut.getHome();
			EPaymentInboxObject inboxObj = new EPaymentInboxObject();

			System.out.println("payeeCode : "+payeeCode);       
			System.out.println("referenceNo : " +referenceNo);          
			System.out.println("accountNo : "+accountNo);         
			System.out.println("transAmount : "+transAmount);         
			System.out.println("approvalCode : "+approvalCode);          
			System.out.println("bankRefNO : "+bankRefNo);         
            System.out.println("transDate : "+transDate);	
			System.out.println("status :"+status);
			
			inboxObj.payment_mode = EPaymentInboxBean.PAYMENT_MAYBANK2U;
			inboxObj.merchant_id = payeeCode;

			if(!referenceNo.equals(""))
                 inboxObj.merchant_tranx_id = new Integer(referenceNo);

			System.out.println("Checkpoint 1");

			if(!accountNo.equals(""))
				inboxObj.merchant_tranx_id = new Integer(accountNo);

			System.out.println("Checkpoint 2");

			if(!transAmount.equals(""))
				inboxObj.tranx_amt = new BigDecimal(transAmount.trim()); 

			System.out.println("Checkpoint 3");

			inboxObj.tranx_status = status;
			
			System.out.println("Checkpoint 4");

			inboxObj.tranx_date = TimeFormat.createTimestamp(transDate);
		
			System.out.println("Checkpoint 5");		

			if(!approvalCode.equals(""))
				inboxObj.tranx_appr_code = approvalCode;

			System.out.println("Checkpoint 6");

			if(!bankRefNo.equals(""))
				inboxObj.bank_ref_no = bankRefNo;
			
			System.out.println("Checkpoint 7");

			inboxHome.create(inboxObj);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private String queryStatusMaybank2u(HttpServletRequest req)
	{
		try
		{
			String userid = AppConfigManager.getProperty("ECOM-M2U-USRID");
			String password = AppConfigManager.getProperty("ECOM-M2U-PASS");
			String payeecode = AppConfigManager.getProperty("ECOM-M2U-MERID");
			String accountNo = (String) req.getParameter("accountNo");
			String referenceNo = (String) req.getParameter("referenceNo");
			String transAmount = (String) req.getParameter("transAmount");

			String billno = "";
			
			System.out.println("userid : "+userid);
			System.out.println("password : "+password);
			System.out.println("payeecode : "+payeecode);
			System.out.println("accountNo : "+accountNo);
			System.out.println("referenceNo : "+referenceNo);
			System.out.println("transAmount : "+transAmount);

            if(accountNo != null)
            	billno = accountNo;

			if(referenceNo != null)
				billno = referenceNo;			

			XmlQueryClient client = XmlQueryClient.getInstance();		
			String paymentStatus = client.requestPayeeInfo(userid, password, payeecode, billno, transAmount);

			if(paymentStatus.equals("00"))
				req.setAttribute("status", "Successful");
			else if(paymentStatus.equals("01"))
				req.setAttribute("status", "Unsuccessful");
			else
				req.setAttribute("status", "Unathourised");
			
			return paymentStatus;
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
        private void updatePayment(HttpServletRequest req, String pkid, String paymentConfigPkid)
        {
                try
                {
                        System.out.println("Checkpoint yyyy");

                        Integer so_pkid = new Integer(pkid);
                        QueryObject query = new QueryObject(new String[] { SalesOrderIndexBean.GUID + " = '" + so_pkid.toString() + "' " });

                        Vector vecOrder = new Vector(SalesOrderIndexNut.getObjects(query));
                        for (int cnt1 = 0; cnt1 < vecOrder.size(); cnt1++)
                        {
                                SalesOrderIndexObject soObj = (SalesOrderIndexObject) vecOrder.get(cnt1);
                                SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soObj.pkid);

                                EPaymentConfigObject obj = EPaymentConfigNut.getObject(new Long(paymentConfigPkid));
                                soObj.statusPayment = obj.payment_status;
                                soObj.receiptRemarks = obj.payment_remarks;

                                if (req.getParameter("approvalCode") != null)
                					soObj.receiptApprovalCode = req.getParameter("approvalCode");
                				
                                if (req.getParameter("bankRefNo") != null)
                					soObj.etxnCode = req.getParameter("bankRefNo");
                                
                                soEJB.setObject(soObj);
                        }

                        System.out.println("Checkpoint zzzz");

                } catch (Exception e)
                {
                        e.printStackTrace();
                }
        }

	public void fnSalesOrderAmountTotal(HttpServletRequest req, HttpServletResponse res, String SOGuid)
                        throws Exception
        {

                Log.printVerbose("Checkpoint ZAAAAAA");

                HttpSession session = req.getSession(true);

                QueryObject query = new QueryObject(new String[] { SalesOrderIndexBean.GUID + " = '" + SOGuid + "' " });
                Vector vecSalesOrder = new Vector(SalesOrderIndexNut.getObjects(query));
                BigDecimal AmtTotal = new BigDecimal(0);
                for (int cnt1 = 0; cnt1 < vecSalesOrder.size(); cnt1++)
                {
                		Log.printVerbose("There's order inside vecSalesOrder");
                	
                        SalesOrderIndexObject soObj = (SalesOrderIndexObject) vecSalesOrder.get(cnt1);
                        EStoreObject objStore = (EStoreObject) session.getAttribute("objStore");
                        EditSalesOrderSession csos = new EditSalesOrderSession(objStore.ecom_userId);
                        csos.loadSalesOrder(soObj.pkid);

                        Log.printVerbose("csos getBillAmount :"+csos.getBillAmount().toString());
                        
                        AmtTotal = AmtTotal.add(csos.getBillAmount());
                        
                        Log.printVerbose("AmtTotal :"+AmtTotal.toString());

                }

                Log.printVerbose("vecSalesOrder "+ vecSalesOrder.size());

                session.setAttribute("AmtTotal", String.valueOf(AmtTotal));

                session.setAttribute("vecSalesOrder", null);
                session.setAttribute("vecSalesOrder", vecSalesOrder);

                Log.printVerbose("Assigned to verSalesOrder session");
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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.vlee.bean.distribution.CreateSalesOrderSession;
import com.vlee.bean.distribution.EditSalesOrderSession;
import com.vlee.ejb.customer.SalesOrderIndex;
import com.vlee.ejb.customer.SalesOrderIndexBean;
import com.vlee.ejb.customer.SalesOrderIndexNut;
import com.vlee.ejb.customer.SalesOrderIndexObject;
import com.vlee.ejb.ecommerce.EPaymentConfigNut;
import com.vlee.ejb.ecommerce.EPaymentConfigObject;
import com.vlee.ejb.ecommerce.EPaymentInboxBean;
import com.vlee.ejb.ecommerce.EPaymentInboxHome;
import com.vlee.ejb.ecommerce.EPaymentInboxNut;
import com.vlee.ejb.ecommerce.EPaymentInboxObject;
import com.vlee.ejb.ecommerce.EStoreObject;

import com.vlee.util.TimeFormat;
import com.vlee.webservice.maybank2u.databinding.XmlQueryClient;


import com.vlee.util.Log;
import com.vlee.util.QueryObject;

public class DoEPaymentMaybank2U extends HttpServlet
{
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
		doPost(req, res);
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
//		String status = (String) req.getParameter("status");
//		if (status.equals("00"))
//		{
//			String paymentStatus = queryStatusMaybank2u(req);
//			if(paymentStatus.equals("00"))
//			{
//				updatePaymentInbox(req, paymentStatus);
//				updatePayment(req);
//			} else 
//			{
//				updatePaymentInbox(req, paymentStatus);
//			}
//			req.setAttribute("status", "success");
//		} else
//		{
//			req.setAttribute("status", "fail");
//		}
		
		String paymentStatus = queryStatusMaybank2u(req);
		String guid = req.getParameter("referenceNo");
		
		if(paymentStatus.equals("00"))
		{
			paymentStatus = "success";
			updatePaymentInbox(req, paymentStatus);
			updatePayment(req, guid, "10017");

			System.out.println("Updated SO");
			
			req.setAttribute("status", "success");

			try{ fnSalesOrderAmountTotal(req,res,guid);}
                        catch(Exception ex){ ex.printStackTrace();}

		} else 
		{
			paymentStatus = "fail";
			req.setAttribute("status", "fail");
		}

		String nextPage = "/member_checkout_receipt.jsp";

		getServletContext().getRequestDispatcher(res.encodeRedirectURL(nextPage)).forward(req, res);

		//RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/checkout-receipt-page");
		//System.out.println(dispatcher.toString());

		//dispatcher.forward(req, res);
	}

	private void updatePaymentInbox(HttpServletRequest req, String status)
	{
		try
		{
			String payeeCode = (String) req.getParameter("payeeCode");
			String referenceNo = (String) req.getParameter("referenceNo");
			String accountNo = (String) req.getParameter("accountNo");
			String transAmount = (String) req.getParameter("transAmount");
			String approvalCode = (String) req.getParameter("approvalCode");
			String bankRefNo = (String) req.getParameter("bankRefNo");
			String transDate = (String) req.getParameter("transDate");
			
			EPaymentInboxHome inboxHome = EPaymentInboxNut.getHome();
			EPaymentInboxObject inboxObj = new EPaymentInboxObject();

			System.out.println("payeeCode : "+payeeCode);
                        System.out.println("referenceNo : " +referenceNo);
                        System.out.println("accountNo : "+accountNo);
                        System.out.println("transAmount : "+transAmount);
                        System.out.println("approvalCode : "+approvalCode);
                        System.out.println("bankRefNO : "+bankRefNo);
                        System.out.println("transDate : "+transDate);	
			System.out.println("status :"+status);
			
			inboxObj.payment_mode = EPaymentInboxBean.PAYMENT_MAYBANK2U;
			inboxObj.merchant_id = new Integer(payeeCode);

			if(!referenceNo.equals(""))
                                inboxObj.merchant_tranx_id = new Integer(referenceNo);

			System.out.println("Checkpoint 1");

			if(!accountNo.equals(""))
				inboxObj.merchant_tranx_id = new Integer(accountNo);

			System.out.println("Checkpoint 2");

			if(!transAmount.equals(""))
				inboxObj.tranx_amt = new BigDecimal(transAmount.trim()); 

			System.out.println("Checkpoint 3");

			inboxObj.tranx_status = status;
			
			System.out.println("Checkpoint 4");

			inboxObj.tranx_date = TimeFormat.createTimestamp(transDate);
		
			System.out.println("Checkpoint 5");		

			if(!approvalCode.equals(""))
				inboxObj.tranx_appr_code = approvalCode;

			System.out.println("Checkpoint 6");

			if(!bankRefNo.equals(""))
				inboxObj.bank_ref_no = bankRefNo;
			
			System.out.println("Checkpoint 7");

			inboxHome.create(inboxObj);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private String queryStatusMaybank2u(HttpServletRequest req)
	{
		try
		{
			String userid = "mws486";// (String) req.getParameter("userid");
			String password = "s4ss251097";// (String) req.getParameter("password");
			String payeecode = "486";// (String) req.getParameter("payeecode");
			String accountNo = (String) req.getParameter("accountNo");
			String referenceNo = (String) req.getParameter("transAmount");
			String transAmount = (String) req.getParameter("transAmount");

			String billno = "";

			if(referenceNo != null)
                                billno = referenceNo;

                        if(accountNo != null)
                                billno = accountNo;
			

			XmlQueryClient client = XmlQueryClient.getInstance();		

			String paymentStatus = client.requestPayeeInfo(userid, password, payeecode, billno, transAmount);

			if(paymentStatus.equals("00"))
				req.setAttribute("status", "Successful");
			else if(paymentStatus.equals("01"))
				req.setAttribute("status", "Unsuccessful");
			else
				req.setAttribute("status", "Unathourised");
			
			return paymentStatus;
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

/*
	private void updatePayment(HttpServletRequest req)
	{
		System.out.println("Inside update payment");

		try
		{
			HttpSession session = req.getSession();
			Vector v = (Vector) session.getAttribute("vecSalesOrder");

			for (int x = 0; x < v.size(); x++)
			{
				System.out.println("Inside for loop");

				CreateSalesOrderSession csos = (CreateSalesOrderSession) v.get(x);
				SalesOrderIndexObject salesObj = csos.getSalesOrderIndex();

				EPaymentConfigObject configObj = EPaymentConfigNut.getObject(new Long(10017));
				salesObj.statusPayment = configObj.payment_status;
				salesObj.receiptRemarks = configObj.payment_remarks;

				String approvalCode = (String) req.getParameter("approvalCode");

				if(!approvalCode.equals(""))
					salesObj.receiptApprovalCode = approvalCode;

				SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(salesObj.pkid);
				soEJB.setObject(salesObj);
			}
			session.removeAttribute("vecSalesOrder");
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
*/

        private void updatePayment(HttpServletRequest req, String pkid, String paymentConfigPkid)
        {
                try
                {
                        System.out.println("Checkpoint yyyy");

                        Integer so_pkid = new Integer(pkid);
                        QueryObject query = new QueryObject(new String[] { SalesOrderIndexBean.GUID + " = '" + so_pkid.toString() + "' " });

                        Vector vecOrder = new Vector(SalesOrderIndexNut.getObjects(query));
                        for (int cnt1 = 0; cnt1 < vecOrder.size(); cnt1++)
                        {
                                SalesOrderIndexObject soObj = (SalesOrderIndexObject) vecOrder.get(cnt1);
                                SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soObj.pkid);

                                EPaymentConfigObject obj = EPaymentConfigNut.getObject(new Long(paymentConfigPkid));
                                soObj.statusPayment = obj.payment_status;
                                soObj.receiptRemarks = obj.payment_remarks;

                                soEJB.setObject(soObj);
                        }

                        System.out.println("Checkpoint zzzz");

                } catch (Exception e)
                {
                        e.printStackTrace();
                }
        }

	public void fnSalesOrderAmountTotal(HttpServletRequest req, HttpServletResponse res, String SOGuid)
                        throws Exception
        {

                Log.printVerbose("Checkpoint ZAAAAAA");

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

                Log.printVerbose("vecSalesOrder "+ vecSalesOrder.size());

                session.setAttribute("AmtTotal", String.valueOf(AmtTotal));

                session.setAttribute("vecSalesOrder", null);
                session.setAttribute("vecSalesOrder", vecSalesOrder);

                Log.printVerbose("Assigned to verSalesOrder session");
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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.vlee.bean.distribution.CreateSalesOrderSession;
import com.vlee.bean.distribution.EditSalesOrderSession;
import com.vlee.ejb.customer.SalesOrderIndex;
import com.vlee.ejb.customer.SalesOrderIndexBean;
import com.vlee.ejb.customer.SalesOrderIndexNut;
import com.vlee.ejb.customer.SalesOrderIndexObject;
import com.vlee.ejb.ecommerce.EPaymentConfigNut;
import com.vlee.ejb.ecommerce.EPaymentConfigObject;
import com.vlee.ejb.ecommerce.EPaymentInboxBean;
import com.vlee.ejb.ecommerce.EPaymentInboxHome;
import com.vlee.ejb.ecommerce.EPaymentInboxNut;
import com.vlee.ejb.ecommerce.EPaymentInboxObject;
import com.vlee.ejb.ecommerce.EStoreObject;

import com.vlee.util.TimeFormat;
import com.vlee.webservice.maybank2u.databinding.XmlQueryClient;


import com.vlee.util.Log;
import com.vlee.util.QueryObject;

public class DoEPaymentMaybank2U extends HttpServlet
{
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
		doPost(req, res);
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
//		String status = (String) req.getParameter("status");
//		if (status.equals("00"))
//		{
//			String paymentStatus = queryStatusMaybank2u(req);
//			if(paymentStatus.equals("00"))
//			{
//				updatePaymentInbox(req, paymentStatus);
//				updatePayment(req);
//			} else 
//			{
//				updatePaymentInbox(req, paymentStatus);
//			}
//			req.setAttribute("status", "success");
//		} else
//		{
//			req.setAttribute("status", "fail");
//		}
		
		String paymentStatus = queryStatusMaybank2u(req);
		String guid = req.getParameter("referenceNo");
		
		if(paymentStatus.equals("00"))
		{
			paymentStatus = "success";
			updatePaymentInbox(req, paymentStatus);
			updatePayment(req, guid, "10017");

			System.out.println("Updated SO");
			
			req.setAttribute("status", "success");

			try{ fnSalesOrderAmountTotal(req,res,guid);}
                        catch(Exception ex){ ex.printStackTrace();}

		} else 
		{
			paymentStatus = "fail";
			req.setAttribute("status", "fail");
		}

		String nextPage = "/member_checkout_receipt.jsp";

		getServletContext().getRequestDispatcher(res.encodeRedirectURL(nextPage)).forward(req, res);

		//RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/checkout-receipt-page");
		//System.out.println(dispatcher.toString());

		//dispatcher.forward(req, res);
	}

	private void updatePaymentInbox(HttpServletRequest req, String status)
	{
		try
		{
			String payeeCode = (String) req.getParameter("payeeCode");
			String referenceNo = (String) req.getParameter("referenceNo");
			String accountNo = (String) req.getParameter("accountNo");
			String transAmount = (String) req.getParameter("transAmount");
			String approvalCode = (String) req.getParameter("approvalCode");
			String bankRefNo = (String) req.getParameter("bankRefNo");
			String transDate = (String) req.getParameter("transDate");
			
			EPaymentInboxHome inboxHome = EPaymentInboxNut.getHome();
			EPaymentInboxObject inboxObj = new EPaymentInboxObject();

			System.out.println("payeeCode : "+payeeCode);
                        System.out.println("referenceNo : " +referenceNo);
                        System.out.println("accountNo : "+accountNo);
                        System.out.println("transAmount : "+transAmount);
                        System.out.println("approvalCode : "+approvalCode);
                        System.out.println("bankRefNO : "+bankRefNo);
                        System.out.println("transDate : "+transDate);	
			System.out.println("status :"+status);
			
			inboxObj.payment_mode = EPaymentInboxBean.PAYMENT_MAYBANK2U;
			inboxObj.merchant_id = new Integer(payeeCode);

			if(!referenceNo.equals(""))
                                inboxObj.merchant_tranx_id = new Integer(referenceNo);

			System.out.println("Checkpoint 1");

			if(!accountNo.equals(""))
				inboxObj.merchant_tranx_id = new Integer(accountNo);

			System.out.println("Checkpoint 2");

			if(!transAmount.equals(""))
				inboxObj.tranx_amt = new BigDecimal(transAmount.trim()); 

			System.out.println("Checkpoint 3");

			inboxObj.tranx_status = status;
			
			System.out.println("Checkpoint 4");

			inboxObj.tranx_date = TimeFormat.createTimestamp(transDate);
		
			System.out.println("Checkpoint 5");		

			if(!approvalCode.equals(""))
				inboxObj.tranx_appr_code = approvalCode;

			System.out.println("Checkpoint 6");

			if(!bankRefNo.equals(""))
				inboxObj.bank_ref_no = bankRefNo;
			
			System.out.println("Checkpoint 7");

			inboxHome.create(inboxObj);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private String queryStatusMaybank2u(HttpServletRequest req)
	{
		try
		{
			String userid = "mws486";// (String) req.getParameter("userid");
			String password = "s4ss251097";// (String) req.getParameter("password");
			String payeecode = "486";// (String) req.getParameter("payeecode");
			String accountNo = (String) req.getParameter("accountNo");
			String referenceNo = (String) req.getParameter("transAmount");
			String transAmount = (String) req.getParameter("transAmount");

			String billno = "";

			if(referenceNo != null)
                                billno = referenceNo;

                        if(accountNo != null)
                                billno = accountNo;
			

			XmlQueryClient client = XmlQueryClient.getInstance();		

			String paymentStatus = client.requestPayeeInfo(userid, password, payeecode, billno, transAmount);

			if(paymentStatus.equals("00"))
				req.setAttribute("status", "Successful");
			else if(paymentStatus.equals("01"))
				req.setAttribute("status", "Unsuccessful");
			else
				req.setAttribute("status", "Unathourised");
			
			return paymentStatus;
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

/*
	private void updatePayment(HttpServletRequest req)
	{
		System.out.println("Inside update payment");

		try
		{
			HttpSession session = req.getSession();
			Vector v = (Vector) session.getAttribute("vecSalesOrder");

			for (int x = 0; x < v.size(); x++)
			{
				System.out.println("Inside for loop");

				CreateSalesOrderSession csos = (CreateSalesOrderSession) v.get(x);
				SalesOrderIndexObject salesObj = csos.getSalesOrderIndex();

				EPaymentConfigObject configObj = EPaymentConfigNut.getObject(new Long(10017));
				salesObj.statusPayment = configObj.payment_status;
				salesObj.receiptRemarks = configObj.payment_remarks;

				String approvalCode = (String) req.getParameter("approvalCode");

				if(!approvalCode.equals(""))
					salesObj.receiptApprovalCode = approvalCode;

				SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(salesObj.pkid);
				soEJB.setObject(salesObj);
			}
			session.removeAttribute("vecSalesOrder");
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
*/

        private void updatePayment(HttpServletRequest req, String pkid, String paymentConfigPkid)
        {
                try
                {
                        System.out.println("Checkpoint yyyy");

                        Integer so_pkid = new Integer(pkid);
                        QueryObject query = new QueryObject(new String[] { SalesOrderIndexBean.GUID + " = '" + so_pkid.toString() + "' " });

                        Vector vecOrder = new Vector(SalesOrderIndexNut.getObjects(query));
                        for (int cnt1 = 0; cnt1 < vecOrder.size(); cnt1++)
                        {
                                SalesOrderIndexObject soObj = (SalesOrderIndexObject) vecOrder.get(cnt1);
                                SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soObj.pkid);

                                EPaymentConfigObject obj = EPaymentConfigNut.getObject(new Long(paymentConfigPkid));
                                soObj.statusPayment = obj.payment_status;
                                soObj.receiptRemarks = obj.payment_remarks;

                                soEJB.setObject(soObj);
                        }

                        System.out.println("Checkpoint zzzz");

                } catch (Exception e)
                {
                        e.printStackTrace();
                }
        }

	public void fnSalesOrderAmountTotal(HttpServletRequest req, HttpServletResponse res, String SOGuid)
                        throws Exception
        {

                Log.printVerbose("Checkpoint ZAAAAAA");

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

                Log.printVerbose("vecSalesOrder "+ vecSalesOrder.size());

                session.setAttribute("AmtTotal", String.valueOf(AmtTotal));

                session.setAttribute("vecSalesOrder", null);
                session.setAttribute("vecSalesOrder", vecSalesOrder);

                Log.printVerbose("Assigned to verSalesOrder session");
        }

}
