/*==========================================================
*
* Copyright  of Vincent Lee. All Rights Reserved.
* (http://www.vlee.net)
*
* This software is the proprietary information of VLEE,
* Use is subject to license terms.
*
==========================================================*/
package com.vlee.servlet.management;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;

import java.util.*;
import java.sql.*;
import java.math.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.accounting.*;
import com.vlee.bean.reports.*;

public class DoMgtGrossProfitBySalesmanByInvoice implements Action
{
	String strClassName = "DoMgtGrossProfitBySalesmanByInvoice";
	String sessionName = "mgt-gross-profit-by-salesman-by-invoice-report";
	
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = (String) req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("mgt-gross-profit-by-salesman-by-invoice-page");
		}
		
		if (formName.equals("popupPrint"))
		{
			return new ActionRouter("mgt-gross-profit-by-salesman-by-invoice-printable-page");
		}
		
		if (formName.equals("generateReport"))
		{
			try
			{
				fnGenerateReport(servlet, req, res);
			}
			catch(Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
			
		}
		return new ActionRouter("mgt-gross-profit-by-salesman-by-invoice-page");
	}



	public void fnGenerateReport(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		String branchId = req.getParameter("branch");
		String dateFrom = req.getParameter("dateFrom");
		String dateTo = req.getParameter("dateTo");
		String salesmanId = req.getParameter("salesmanId");
				
		HttpSession session = req.getSession();
		GrossProfitBySalesmanByInvoiceReport gpsi = (GrossProfitBySalesmanByInvoiceReport) session.getAttribute(sessionName);
		
		try
		{
			if (gpsi == null)
			{
				gpsi = new GrossProfitBySalesmanByInvoiceReport();
				session.setAttribute(sessionName,gpsi);
			}
						
			Integer intBranchId = new Integer(-1);
			try
			{
				intBranchId = new Integer(branchId);
			} catch (Exception ex)
			{		
			}
			
			if (intBranchId.compareTo(new Integer(-1)) != 0)
			{
				gpsi.setBranch(intBranchId);
			}
				
			Integer intSalesmanId = new Integer(-1);
			if (salesmanId != null)
			{
				intSalesmanId = new Integer(salesmanId);
			}
			
			if (intSalesmanId.compareTo(new Integer(-1)) != 0)
			{
				gpsi.setSalesmanId(intSalesmanId);
			}
			
			gpsi.setDateRange(dateFrom, dateTo);	
			gpsi = gpsi.generateReport(gpsi);
			
			session.setAttribute(sessionName,gpsi);
		} 
		catch (Exception ex)
		{
			throw ex;
		}		
			
		fnAuditTrail(servlet, req, res);
	}
	
	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		System.out.println("Inside fnAuditTrail");
		
		HttpSession session = req.getSession();
		GrossProfitByDepartmentDetailsForm gpdd = (GrossProfitByDepartmentDetailsForm) 
												session.getAttribute(sessionName);
		
		Integer iUserId = (Integer) session.getAttribute("userId");	
		
		if (iUserId != null)
		{
			Log.printVerbose("userid : "+iUserId.toString());
			
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_USAGE;
			atObj.remarks = "generate report: Gross Profit by Salesman By Invoice";
			
			if (gpdd.getBranchId().compareTo(new Integer(-1)) > 0)
			{
				BranchObject branchObj = BranchNut.getObject(gpdd.getBranchId());
				atObj.remarks += ", Branch: " + branchObj.description;
			}
			else
			{
				atObj.remarks += ", Branch: All ";
			}
			
			atObj.remarks += ", Date: " + gpdd.getDateFrom("") + " to " + gpdd.getDateTo("");
			
			AuditTrailNut.fnCreate(atObj);
		}
		
		System.out.println("Leaving fnAuditTrail");
	}
}
