/*==========================================================
 *
 * Copyright  of Vincent Lee (vlee@vlee.net,
 *      vincent@wavelet.biz). All Rights Reserved.
 * (http://www.wavelet.biz)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.trading;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Vector;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.vlee.bean.inventory.*;
import com.vlee.bean.pos.*;
import com.vlee.bean.user.PermissionManager;
import com.vlee.ejb.accounting.BranchBean;
import com.vlee.ejb.accounting.BranchNut;
import com.vlee.ejb.accounting.BranchObject;
import com.vlee.ejb.customer.InvoiceNut;
import com.vlee.ejb.customer.InvoiceObject;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.user.AuditTrailBean;
import com.vlee.ejb.user.AuditTrailNut;
import com.vlee.ejb.user.AuditTrailObject;
import com.vlee.servlet.main.*;
import com.vlee.util.*;

public class DoCancelCashsale implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("trading-pos-cashsale-cancel-page");
		}

		if(formName.equals("getCashsale"))
		{
			try{ fnGetCashsale(servlet,req,res);}
			catch(Exception ex){ req.setAttribute("errMsg",ex.getMessage());}
		}

		if(formName.equals("cancelCashsale"))
		{
			try{ fnCancelCashsale(servlet,req,res);}
			catch(Exception ex){ req.setAttribute("errMsg",ex.getMessage());}
		}
		return new ActionRouter("trading-pos-cashsale-cancel-page");
	}

	private synchronized void fnCancelCashsale(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
		throws Exception
	{
		HttpSession session = req.getSession();
		
		CancelCashsaleForm ccForm = (CancelCashsaleForm) session.getAttribute("trading-pos-cashsale-cancel-form");

		String salesReturnDate = req.getParameter("salesReturnDate");
		String remarksReverse = req.getParameter("remarksReverse");
		Timestamp tsSalesReturn = TimeFormat.createTimestamp(salesReturnDate);

		Log.printVerbose("WHERE IT BEGINS: " + remarksReverse);
		ccForm.remarksReverse = remarksReverse;
		ccForm.cancelCashsale(ccForm.getInvoice(),ccForm.getReceipt(), ccForm.getUserId(), tsSalesReturn);
		String successNotify = "Successfully reversed CASHSALE:"+ccForm.getInvoice().mPkid.toString();
		
		System.out.println("Inside fnCancelCashsale : "+successNotify);
		
		{
			System.out.println("Inside fnCancelCashsale, Invoice Number : "+ccForm.getInvoice().mPkid.toString());
			
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = (Integer) session.getAttribute("userId");
			atObj.auditType = AuditTrailBean.TYPE_CONFIG;
			atObj.time = TimeFormat.getTimestamp();
			atObj.remarks = "cancel-cashsale: INVOICE :" + ccForm.getInvoice().mPkid.toString()+" SR Date:"+TimeFormat.strDisplayDate(tsSalesReturn);
			AuditTrailNut.fnCreate(atObj);
			
			System.out.println("Inside fnCancelCashsale, recorded into audit trail");
		}
		
		ccForm.reset();
		req.setAttribute("successNotify",successNotify);				
		
		System.out.println("Leaving fnCancelCashsale");
	}

	private void fnGetCashsale(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		Integer userId = (Integer) session.getAttribute("userId");
		CancelCashsaleForm ccForm = (CancelCashsaleForm) session.getAttribute("trading-pos-cashsale-cancel-form");
		String cashsaleNo = req.getParameter("cashsaleNo");	

		///////////////////////////////////////////////////////////////////////////////
		//Assigning the permitted branches to a vector for further validation with SO #
		
		System.out.println("Assigning the permitted branches to a vector for further validation with SO #");
		
		PermissionManager permMgr =  (PermissionManager) session.getAttribute("permission-manager");
		String thePermissionName = req.getParameter("thePermissionName");
		String webUtilOptionsBranch = "web-util-inc-options-branch";	
		Vector vecBranch= (Vector) session.getAttribute(webUtilOptionsBranch);
		Vector vecPermittedBranch = new Vector();

		if(vecBranch== null)
		{				
			vecBranch= BranchNut.getValueObjectsGiven(
						BranchBean.STATUS,
						BranchBean.STATUS_ACTIVE,
						(String)null, (String)null);
			session.setAttribute(webUtilOptionsBranch, vecBranch);				
		}
		
		for(int cnt1=0; cnt1<vecBranch.size();cnt1++)
		{
			BranchObject theObj = (BranchObject) vecBranch.get(cnt1);
			if(permMgr.hasPermission(thePermissionName,BranchBean.TABLENAME,theObj.pkid.toString()))
			{								
				System.out.println("Branch description : "+theObj.description);
				
				vecPermittedBranch.add(theObj); 					
			}
		}		
		
		System.out.println("Finished assigning the permitted branches to a vector for further validation with SO #");
		///////////////////////////////////////////////////////////////
		
		try
		{
			Long lCashsale = new Long(cashsaleNo);
			
			//Checking whether the invoice belong to a permitted branch
			System.out.println("Checking whether the invoice belong to a permitted branch");
			
			boolean canCancel = false;
			String errBranch = "";
			
			InvoiceObject invoiceObj = InvoiceNut.getObject(lCashsale);
			
			for(int cnt1=0; cnt1<vecPermittedBranch.size();cnt1++)
			{
				BranchObject theObj = (BranchObject) vecPermittedBranch.get(cnt1);
				
				System.out.println("Branch ID : "+theObj.pkid.toString());
				System.out.println("Invoice Branch ID : "+invoiceObj.mCustSvcCtrId.toString());
				
				if(theObj.pkid.toString().equals(invoiceObj.mCustSvcCtrId.toString()))
				{
					System.out.println("The location IS permitted");
					canCancel = true;
				}
				else
				{
					System.out.println("The location NOT permitted");
				}
			}	
			
			System.out.println("Finished checking whether the invoice belong to a permitted branch");
			//////////////////////////////////////////////////////////////
			
			if(canCancel)
			{
				System.out.println("Can Cancel");
				req.setAttribute("canCancel", "true");
				
				ccForm.loadCashsale(lCashsale);
			}
			else
			{
				System.out.println("Cannot Cancel");
				
				req.setAttribute("errBranch", invoiceObj.mCustSvcCtrId.toString());
				req.setAttribute("canCancel", "false");
			}
			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new Exception(" Invalid Cashsale No! "+ ex.getMessage());
		}
	}

}




