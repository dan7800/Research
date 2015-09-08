/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.footwear;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import java.math.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.accounting.BranchNut;
import com.vlee.ejb.accounting.BranchObject;
import com.vlee.ejb.customer.*;
import com.vlee.bean.customer.*;
import com.vlee.bean.footwear.WholesaleSalesByCategory;
import com.vlee.bean.reports.*;

public class DoFootwearWholesaleSalesByCategoryReport implements Action
{
	private String strClassName = "DoFootwearWholesaleSalesByCategoryReport";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("footwear-wholesale-sales-by-category-report-page");
		}
		if (formName.equals("popupPrint"))
		{
			return new ActionRouter("footwear-wholesale-sales-by-category-report-printable-page");
		}
		if (formName.equals("generateReport"))
		{
			try
			{
				fnGenerateReport(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}

		return new ActionRouter("footwear-wholesale-sales-by-category-report-page");
	}

	private void fnGenerateReport(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		String dateFrom = req.getParameter("dateFrom");
		String dateTo = req.getParameter("dateTo");
		String [] selectedBranches = req.getParameterValues("selectedBranches");
		String groupByCategoryA = req.getParameter("groupByCategoryA");
		
		HttpSession session = req.getSession();		
		WholesaleSalesByCategory wssbc = (WholesaleSalesByCategory) 
												session.getAttribute("footwear-wholesale-sales-by-category-report");
							
		if(dateFrom == null)
		{
			throw new Exception("Date variable is null!!");
		}
		if(dateTo == null)
		{
			throw new Exception("Date variable is null!!");
		}			
						
		try
		{
			if (wssbc == null)
			{
				wssbc = new WholesaleSalesByCategory();
				session.setAttribute("footwear-wholesale-sales-by-category-report", wssbc);
			}
						
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
			wssbc.setSelectedBranches(vecSelectedBranches);
			
			wssbc.setDateRange(dateFrom, dateTo);				
			wssbc.setGroupByCategoryA(groupByCategoryA);
			wssbc = wssbc.generateReport(wssbc);
			
			session.setAttribute("footwear-wholesale-sales-by-category-report", wssbc);
		} 
		catch (Exception ex)
		{
			throw ex;
		}		
	}
}
