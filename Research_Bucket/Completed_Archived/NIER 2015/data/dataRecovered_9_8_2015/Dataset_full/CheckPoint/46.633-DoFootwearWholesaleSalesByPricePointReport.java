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
import com.vlee.bean.footwear.WholesaleSalesByPricePoint;
import com.vlee.bean.reports.*;

public class DoFootwearWholesaleSalesByPricePointReport implements Action
{
	private String strClassName = "DoFootwearWholesaleSalesByPricePointReport";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("footwear-wholesale-sales-by-price-point-report-page");
		}
		if(formName.equals("popupPrint"))
		{
			return new ActionRouter ("footwear-wholesale-sales-by-price-point-report-printable-page");
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

		return new ActionRouter("footwear-wholesale-sales-by-price-point-report-page");
	}

	private void fnGenerateReport(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		String dateFrom = req.getParameter("dateFrom");
		String dateTo = req.getParameter("dateTo");
		String [] selectedBranches = req.getParameterValues("selectedBranches");
		
		HttpSession session = req.getSession();		
		WholesaleSalesByPricePoint wsbpp = (WholesaleSalesByPricePoint) 
												session.getAttribute("footwear-wholesale-sales-by-price-point-report");
							
		if(dateFrom == null)
		{
			throw new Exception("Date variable is null!!");
		}
		if(dateTo == null)
		{
			throw new Exception("Date variable is null!!");
		}			
							
		String filterCategory0 = req.getParameter("filterCategory0");
		String idCategory0 = req.getParameter("idCategory0");
		String filterCategory1 = req.getParameter("filterCategory1");
		String category1 = req.getParameter("category1");
		String filterCategory2 = req.getParameter("filterCategory2");
		String category2 = req.getParameter("category2");
		String filterCategory3 = req.getParameter("filterCategory3");
		String category3 = req.getParameter("category3");
		String filterCategory4 = req.getParameter("filterCategory4");
		String category4 = req.getParameter("category4");
		String filterCategory5 = req.getParameter("filterCategory5");
		String category5 = req.getParameter("category5");
				
		try
		{
			if (wsbpp == null)
			{
				wsbpp = new WholesaleSalesByPricePoint();
				session.setAttribute("footwear-wholesale-sales-by-price-point-report", wsbpp);
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
			wsbpp.setSelectedBranches(vecSelectedBranches);
						
			wsbpp.setDateRange(dateFrom, dateTo);				
			wsbpp.setListingOptions(
					(filterCategory0 != null), new Integer(idCategory0), 
					(filterCategory1 != null), category1, 
					(filterCategory2 != null), category2,
					(filterCategory3 != null), category3, 
					(filterCategory4 != null), category4, 
					(filterCategory5 != null), category5);
			
			wsbpp = wsbpp.generateReport(wsbpp);
			
			session.setAttribute("footwear-wholesale-sales-by-price-point-report", wsbpp);
		} 
		catch (Exception ex)
		{
			throw ex;
		}		
	}
}
