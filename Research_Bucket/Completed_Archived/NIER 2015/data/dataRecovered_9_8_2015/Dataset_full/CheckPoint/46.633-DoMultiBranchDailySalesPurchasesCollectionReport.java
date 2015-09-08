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

public class DoMultiBranchDailySalesPurchasesCollectionReport implements Action
{
	String strClassName = "DoMgtDailySalesReportDetails";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = (String) req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("mgt-multi-branch-daily-sales-purchases-collection-report-page");
		}
		if(formName.equals("popupPrint"))
		{
			return new ActionRouter("mgt-multi-branch-daily-sales-purchases-collection-report-printable-page");
		}
		if(formName.equals("generateReport"))
		{
			fnGenerateReport(servlet, req, res);
		}
		return new ActionRouter("mgt-multi-branch-daily-sales-purchases-collection-report-page");
	}


	private void fnGenerateReport(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		String [] selectedBranches = req.getParameterValues("selectedBranches");
		String dateFrom = req.getParameter("dateFrom");
		String dateTo = req.getParameter("dateTo");

		MultiBranchDailySalesPurchasesCollectionReport mbReport = 
				(MultiBranchDailySalesPurchasesCollectionReport) 
						session.getAttribute("mgt-multi-branch-daily-sales-purchases-collection-report");
		mbReport.setDateRange(dateFrom,dateTo);

		Vector vecSelectedBranches = new Vector();
		for(int cnt1=0;selectedBranches!=null && cnt1<selectedBranches.length;cnt1++)
		{
			try
			{
				if(selectedBranches[cnt1].length()>0)
				{
					Log.printVerbose(" checkpoint 1: selectedBranches["+cnt1+"] = "+selectedBranches[cnt1]);
					Integer iBranch = new Integer(selectedBranches[cnt1]);
					BranchObject branchObj = BranchNut.getObject(iBranch);
					if(branchObj!=null)
					{
						vecSelectedBranches.add(branchObj);
					}
				}
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
		mbReport.setSelectedBranches(vecSelectedBranches);
		Log.printVerbose(" CSV VALUES for SELECTED BRANCHES = "+mbReport.getSelectedBranchesCSV());
		mbReport.generateReport("");
	}

} //






 
