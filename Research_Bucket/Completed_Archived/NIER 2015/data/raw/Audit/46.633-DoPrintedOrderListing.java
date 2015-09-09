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
package com.vlee.servlet.distribution;

import com.vlee.servlet.main.*;

import javax.servlet.*;
import javax.servlet.http.*;

import java.util.*;
import java.math.*;
import java.sql.*;

import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.util.*;
import com.vlee.bean.distribution.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.inventory.*;

public class DoPrintedOrderListing extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		String date = req.getParameter("date");
		
		String btnPrintForms = req.getParameter("printForms");
		String btnPrintForms_OdrNo = req.getParameter("printForms_OdrNo");

		if(formName==null)
		{
			if(date != null)
			{
				req.setAttribute("date",date);
			}
			return new ActionRouter("dist-printed-order-listing-page");
		}
		else if(formName.equals("setLogic"))
		{
			fnSetLogic(servlet,req,res);
		}
		else if(formName.equals("getList"))
		{
			fnGetList(servlet,req,res);
		}
		else if(formName.equals("printOrders") && btnPrintForms!=null)
		{
			fnGetSelectedOrders(servlet,req,res);
			return new ActionRouter("dist-sales-order-print-as-forms-multiple-page");
		}
		else if(formName.equals("printOrders") && btnPrintForms_OdrNo!=null)
		{
			req.setAttribute("printOrderNo", "true");
			fnGetSelectedOrders(servlet,req,res);
			return new ActionRouter("dist-sales-order-print-as-forms-multiple-page");
		}
				
		if(date != null)
		{
			req.setAttribute("date",date);
		}
		
		return new ActionRouter("dist-printed-order-listing-page");
	}

	private void fnSetLogic(HttpServlet servlet, HttpServletRequest req,
											HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		PrintedOrderListingForm polForm = (PrintedOrderListingForm) session.getAttribute("dist-printed-order-listing-form");
		if(polForm==null)
	   	{
			Integer userId = (Integer) session.getAttribute("userId");
	      	polForm = new PrintedOrderListingForm(userId);
	      	session.setAttribute("dist-printed-order-listing-form",polForm);
	   	}

		String logic = req.getParameter("logic");
		polForm.setLogic(logic);

	}

	private void fnGetList(HttpServlet servlet, HttpServletRequest req,
											HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		PrintedOrderListingForm polForm = (PrintedOrderListingForm) session.getAttribute("dist-printed-order-listing-form");
		
		if(polForm==null)
	   	{
			Integer userId = (Integer) session.getAttribute("userId");
	      	polForm = new PrintedOrderListingForm(userId);
	      	session.setAttribute("dist-printed-order-listing-form",polForm);
	   	}

		try
		{
			String dateFilter = req.getParameter("dateFilter");
			String dateFrom = req.getParameter("dateFrom");
			String dateTo = req.getParameter("dateTo");
			String logic = req.getParameter("logic");
			String paymentStatus = req.getParameter("paymentStatus");
			String floristDriver = req.getParameter("floristDriver");
			
			String[] receiverRegion = (String[]) req.getParameterValues("receiverRegion");
			if (receiverRegion != null)
			{
				Vector vecSelected = new Vector();
				for (int i = 0; i < receiverRegion.length; i++)
				{
					vecSelected.add((String) receiverRegion[i]);
				}
				req.setAttribute("vecSelected", vecSelected);

				polForm.setReceiverRegion(receiverRegion);
			}
			else
			{
				polForm.setReceiverRegion(null);
				req.setAttribute("vecSelected", null);

			}

			String sortBy = req.getParameter("sortBy");

			polForm.setDateFilter(dateFilter);
			polForm.setDateRange(dateFrom, dateTo);
			polForm.setLogic(logic);
			polForm.setPaymentStatus(paymentStatus);
			polForm.setFloristDriver(floristDriver);
			
			polForm.setSortBy(sortBy);
			polForm.searchRecords();

		}
		catch(Exception ex)
		{ 
			req.setAttribute("errMsg",ex.getMessage());
		}
	}
	
	private void fnGetSelectedOrders(HttpServlet servlet,HttpServletRequest req,HttpServletResponse res)
	{
		Vector vecOrder = new Vector();
		String[] soPkid = req.getParameterValues("soPkid");
		if(soPkid==null){ return;}
		
		String docTrail = "";
		HttpSession session = (HttpSession) req.getSession();
		Integer userId = (Integer) session.getAttribute("userId");
		
		for(int cnt1=0;cnt1<soPkid.length;cnt1++)
		{
			try
			{
				Long lPkid = new Long(soPkid[cnt1]);
				SalesOrderIndexObject soObj = SalesOrderIndexNut.getObjectTree(lPkid);
				vecOrder.add(soObj);
				
				docTrail = "";
				docTrail = DocumentProcessingItemNut.appendDocTrail(
						"PRINT-OF-DO-WO", "", TimeFormat.strDisplayTime(TimeFormat.getTimestamp()), docTrail);
				fnRecordInDocTrail(userId, soObj.pkid, docTrail);
			}
			catch(Exception ex)
			{ ex.printStackTrace();}
		}	
		req.setAttribute("vecOrder", vecOrder);
	}
	
	private static synchronized void fnRecordInDocTrail(Integer userId, Long soPkid, String docTrail)
	{		
		if(docTrail.length()>4)
		{
			System.out.println("Inside fnRecordInDocTrail : "+docTrail);
			
			DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
			dpiObj.processType = "ORDER-UPDATE";
			dpiObj.category = "UPDATE-DETAILS";
			dpiObj.auditLevel = new Integer(0);
			dpiObj.userid = userId;
			dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
			dpiObj.docId = soPkid;
			dpiObj.description1 = docTrail;
			dpiObj.time = TimeFormat.getTimestamp();
			dpiObj.state = DocumentProcessingItemBean.STATE_CREATED;
			dpiObj.status = DocumentProcessingItemBean.STATUS_ACTIVE;
			DocumentProcessingItemNut.fnCreate(dpiObj);
		}
	}
}



