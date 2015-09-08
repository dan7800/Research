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

public class DoMgtGrossProfitByDepartmentSummary implements Action
{
	String strClassName = "DoMgtGrossProfitByDepartmentSummary";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = (String) req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("mgt-gross-profit-by-department-summary-page");
		}
		
		if (formName.equals("popupPrint"))
		{
			return new ActionRouter("mgt-gross-profit-by-department-summary-print-page");
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
		return new ActionRouter("mgt-gross-profit-by-department-summary-page");
	}



	public void fnGenerateReport(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		String branchId = req.getParameter("branch");
		String dateFrom = req.getParameter("dateFrom");
		String dateTo = req.getParameter("dateTo");
		String deptCode = req.getParameter("deptCode");
				
		HttpSession session = req.getSession();
		GrossProfitByDepartmentSummaryForm gpdd = (GrossProfitByDepartmentSummaryForm) 
						session.getAttribute("mgt-gross-profit-by-department-summary-report");
		
		try
		{
			if (gpdd == null)
			{
				gpdd = new GrossProfitByDepartmentSummaryForm();
				session.setAttribute("mgt-gross-profit-by-department-summary-report",gpdd);
			}
						
			Integer intBranchId = new Integer(-1);
			try
			{
				intBranchId = new Integer(branchId);				
			}
			catch(Exception ex){}
					
			gpdd.setBranch(intBranchId);
			gpdd.setDateRange(dateFrom, dateTo);	
			gpdd.setDeptCode(deptCode);
			gpdd = gpdd.generateReport(gpdd);
			
			session.setAttribute("mgt-gross-profit-by-department-summary-report",gpdd);
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
												session.getAttribute("mgt-gross-profit-by-department-summary-report");
		
		Integer iUserId = (Integer) session.getAttribute("userId");	
		
		if (iUserId != null)
		{
			Log.printVerbose("userid : "+iUserId.toString());
			
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_USAGE;
			atObj.remarks = "generate-rpt: Gross Profit by Department Summary";
			
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
