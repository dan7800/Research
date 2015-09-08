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

public class DoMgtGrossProfitBySupervisorTechnicianDetails implements Action
{
	String strClassName = "DoMgtGrossProfitBySupervisorTechnicianDetails";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = (String) req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("mgt-gross-profit-by-supervisor-details-page");
		}
		
		if (formName.equals("popupPrint"))
		{
			return new ActionRouter("mgt-printable-gross-profit-by-supervisor-details-page");
		}
		
		if (formName.equals("popupPrint2"))
		{
			return new ActionRouter("mgt-printable2-gross-profit-by-supervisor-details-page");
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
		return new ActionRouter("mgt-gross-profit-by-supervisor-details-page");
	}

	public void fnGenerateReport(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		String branchId = req.getParameter("branch");
		String dateFrom = req.getParameter("dateFrom");
		String dateTo = req.getParameter("dateTo");
		String salesmanId = req.getParameter("salesmanId");
		String salesType = req.getParameter("salesType");
		
		HttpSession session = req.getSession();
		GrossProfitBySupervisorTechnicianDetailsForm gpstd = (GrossProfitBySupervisorTechnicianDetailsForm) session .getAttribute("mgt-gross-profit-by-supervisor-details-report");

		try
		{
			if (gpstd == null)
			{
				gpstd = new GrossProfitBySupervisorTechnicianDetailsForm();
				session.setAttribute("mgt-gross-profit-by-supervisor-details-report",gpstd);
			}
						
			Integer intBranchId = new Integer(-1);
			try
			{
				intBranchId = new Integer(branchId);				
			}
			catch(Exception ex){}
			
			Integer intSalesmanId = new Integer(-1);
			try
			{
				intSalesmanId = new Integer(salesmanId);				
			}
			catch(Exception ex){}
			
			gpstd.setBranch(intBranchId);
			gpstd.setDateRange(dateFrom, dateTo);	
			gpstd.setSalesmanId(intSalesmanId);
			gpstd.setSalesType(salesType);
			gpstd = gpstd.generateReport(gpstd);
			
			session.setAttribute("mgt-gross-profit-by-supervisor-details-report",gpstd);
		} 
		catch (Exception ex)
		{
			throw ex;
		}		
		
		fnAuditTrail(servlet, req, res);
	}
	
	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Log.printVerbose("Inside fnAuditTrail");
		
		HttpSession session = req.getSession();
		GrossProfitBySupervisorTechnicianDetailsForm gpstd = (GrossProfitBySupervisorTechnicianDetailsForm) 
												session.getAttribute("mgt-gross-profit-by-supervisor-details-report");
		
		Integer iUserId = (Integer) session.getAttribute("userId");	
				
		if (iUserId != null)
		{
			Log.printVerbose("userid : "+iUserId.toString());
			
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_USAGE;
			atObj.remarks = "generate-rpt: Gross Profit by Supervisor/Technician Details";
			
			if (gpstd.getBranchId().compareTo(new Integer(-1)) > 0)
			{
				BranchObject branchObj = BranchNut.getObject(gpstd.getBranchId());
				atObj.remarks += ", Branch: " + branchObj.description;
			}
			else
			{
				atObj.remarks += ", Branch: All ";
			}
			
			atObj.remarks += ", Date: " + gpstd.getDateFrom("") + " to " + gpstd.getDateTo("");
			
			AuditTrailNut.fnCreate(atObj);
		}
		
		Log.printVerbose("Leaving fnAuditTrail");
	}
}
