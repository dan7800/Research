/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.distribution;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;

import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.accounting.DocumentProcessingItemBean;
import com.vlee.ejb.accounting.DocumentProcessingItemNut;
import com.vlee.ejb.accounting.DocumentProcessingItemObject;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.inventory.*;

public class DoSalesOrderPrintPanel implements Action
{
	private String strClassName = "DoSalesOrderPrint";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		Long pkid = new Long(req.getParameter("pkid"));
		SalesOrderIndexObject soObj = SalesOrderIndexNut.getObjectTree(pkid);
		
		String docTrail = "";
		HttpSession session = (HttpSession) req.getSession();
		Integer userId = (Integer) session.getAttribute("userId");
		
		if (soObj != null)
		{
			req.setAttribute("soObj", soObj);
		}
		if (formName == null)
		{
			return new ActionRouter("dist-so-print-panel-page");
		}
		if (formName.equals("printInvoice"))
		{
			docTrail = DocumentProcessingItemNut.appendDocTrail(
					"PRINT-INVOICE", "", TimeFormat.strDisplayTime(TimeFormat.getTimestamp()), docTrail);			
			fnRecordInDocTrail(userId, soObj.pkid, docTrail);
			
			return new ActionRouter("dist-so-print-as-invoice-page");
		}
		if (formName.equals("printReceipt"))
		{
			docTrail = DocumentProcessingItemNut.appendDocTrail(
					"PRINT-RECEIPT", "", TimeFormat.strDisplayTime(TimeFormat.getTimestamp()), docTrail);			
			fnRecordInDocTrail(userId, soObj.pkid, docTrail);
			
			return new ActionRouter("dist-so-print-as-receipt-page");
		}
		try
		{
			if (formName.equals("printDelSoWorkshop"))
			{
				String printDelOrder = req.getParameter("delOrderPrintSel");
				String printOrderForm = req.getParameter("orderFormPrintSel");
				String printWorkshopOrder = req.getParameter("workshopOrderPrintSel");
				Log.printVerbose(req.getParameter("delOrderPrintSel"));
				Log.printVerbose(printDelOrder);
				if (printDelOrder == null && printOrderForm == null && printWorkshopOrder == null)
				{
					fnMsgBox(servlet, req, res);
				} else
				{
					if(printDelOrder != null)
					{
						docTrail = DocumentProcessingItemNut.appendDocTrail(
								"PRINT-DO", "", TimeFormat.strDisplayTime(TimeFormat.getTimestamp()), docTrail);
					}
					if(printOrderForm != null)
					{
						docTrail = DocumentProcessingItemNut.appendDocTrail(
								"PRINT-OF", "", TimeFormat.strDisplayTime(TimeFormat.getTimestamp()), docTrail);
					}
					if(printWorkshopOrder != null)
					{
						docTrail = DocumentProcessingItemNut.appendDocTrail(
								"PRINT-WO", "", TimeFormat.strDisplayTime(TimeFormat.getTimestamp()), docTrail);
					}								
					fnRecordInDocTrail(userId, soObj.pkid, docTrail);
					
					req.setAttribute("delOrderPrintSel", printDelOrder);
					req.setAttribute("orderFormPrintSel", printOrderForm);
					req.setAttribute("workshopOrderPrintSel", printWorkshopOrder);
					return new ActionRouter("dist-so-print-as-delSoWorkshop-page");
				}
			}
		} catch (Exception ex)
		{
			req.setAttribute("errMsg", ex.getMessage());
			return new ActionRouter("dist-so-print-panel-page");
		}
		return new ActionRouter("dist-so-print-panel-page");
	}

	private void fnMsgBox(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		throw new Exception("Please select at least 1 option");
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
