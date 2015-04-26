/*==========================================================
 *
 * Copyright © of Vincent Lee (vlee@vlee.net,
 *      vincent@wavelet.biz). All Rights Reserved.
 * (http://www.wavelet.biz)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.finance;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.accounting.*;
import com.vlee.util.*;

public class DoReversePayment extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("finance-reverse-payment-page");
		}
		if (formName.equals("selectPayment"))
		{
			try
			{
				fnSelectPayment(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("reversePayment"))
		{
			try
			{
				fnReversePayment(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		return new ActionRouter("finance-reverse-payment-page");
	}

	private void fnSelectPayment(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		Long paymentPkid = new Long(req.getParameter("paymentPkid"));
		PaymentVoucherIndexObject pvObj = PaymentVoucherIndexNut.getObject(paymentPkid);
		if (pvObj == null)
		{
			throw new Exception("Invalid Payment Number!");
		}
		req.setAttribute("pvObj", pvObj);
	}

	private void fnReversePayment(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		Long paymentPkid = new Long(req.getParameter("paymentPkid"));
		HttpSession session = req.getSession();
		Integer userId = (Integer) session.getAttribute("userId");
		String remarkReverse = "";
		String strTmp = req.getParameter("remarkReverse");
		if(strTmp != null)
			remarkReverse = strTmp;
		PaymentVoucherIndexNut.fnReversePayment(paymentPkid, userId, remarkReverse);
		PaymentVoucherIndexObject pvObj = PaymentVoucherIndexNut.getObject(paymentPkid);
		req.setAttribute("pvObj", pvObj);
		
		System.out.println("Inside fnReverseReceipt");
		
		{
			System.out.println("Inside fnReversePayment, Payment Voucher : "+pvObj.pkid.toString());
			
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = (Integer) session.getAttribute("userId");
			atObj.auditType = AuditTrailBean.TYPE_CONFIG;
			atObj.time = TimeFormat.getTimestamp();
			atObj.remarks = "reverse-payment: Payment Voucher :" + pvObj.pkid.toString();
			AuditTrailNut.fnCreate(atObj);
			
			System.out.println("Inside fnReversePayment, recorded into audit trail");
		}
	}
}
