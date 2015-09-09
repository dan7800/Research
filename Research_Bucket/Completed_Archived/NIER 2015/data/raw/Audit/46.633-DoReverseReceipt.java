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

public class DoReverseReceipt extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("finance-reverse-receipt-page");
		}
		if (formName.equals("selectReceipt"))
		{
			try
			{
				fnSelectReceipt(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("reverseReceipt"))
		{
			try
			{
				fnReverseReceipt(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		return new ActionRouter("finance-reverse-receipt-page");
	}

	private void fnSelectReceipt(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		Long receiptPkid = new Long(req.getParameter("receiptPkid"));
		OfficialReceiptObject orObj = OfficialReceiptNut.getObject(receiptPkid);
		if (orObj == null)
		{
			throw new Exception("Invalid Receipt Number!");
		}
		req.setAttribute("orObj", orObj);
	}

	private void fnReverseReceipt(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		Long receiptPkid = new Long(req.getParameter("receiptPkid"));
		
		String remarkReverse = "";
		String strTmp = req.getParameter("remarkReverse");
		if(strTmp != null)
			remarkReverse = strTmp;
		
		HttpSession session = req.getSession();
		Integer userId = (Integer) session.getAttribute("userId");
		OfficialReceiptNut.fnReverseReceipt(receiptPkid, userId, remarkReverse);
		OfficialReceiptObject orObj = OfficialReceiptNut.getObject(receiptPkid);
		req.setAttribute("orObj", orObj);
		
		System.out.println("Inside fnReverseReceipt");
		
		{
			System.out.println("Inside fnReverseReceipt, Official Receipt : "+orObj.pkid.toString());
			
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = (Integer) session.getAttribute("userId");
			atObj.auditType = AuditTrailBean.TYPE_CONFIG;
			atObj.time = TimeFormat.getTimestamp();
			atObj.remarks = "reverse-receipt: RECEIPT :" + orObj.pkid.toString();
			AuditTrailNut.fnCreate(atObj);
			
			System.out.println("Inside fnReverseReceipt, recorded into audit trail");
		}
	}
}
