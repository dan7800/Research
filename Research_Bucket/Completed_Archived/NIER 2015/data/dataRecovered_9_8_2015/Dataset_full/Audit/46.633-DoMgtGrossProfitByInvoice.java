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

public class DoMgtGrossProfitByInvoice implements Action
{
	String strClassName = "DoMgtGrossProfitByInvoice";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = (String) req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("mgt-gross-profit-by-invoice-page");
		}
		if (formName.equals("popupPrint"))
		{
			HttpSession session = req.getSession();
			GrossProfitByInvoiceReport gpReport = (GrossProfitByInvoiceReport) session .getAttribute("mgt-gross-profit-by-invoice-report");
			if(!gpReport.getBranch("").equals("all"))
			{
				BranchObject bchObj = BranchNut.getObject(new Integer(gpReport.getBranch("")));
				if(bchObj!=null)
				{
					req.setAttribute("strBranch",bchObj.name);
				}				
			}
			else
			{
				req.setAttribute("strBranch","ALL BRANCHES");
			}

			req.setAttribute("dateFrom", TimeFormat.strDisplayDate(gpReport.getDateFrom()));
			req.setAttribute("dateTo", TimeFormat.strDisplayDate(gpReport.getDateTo()));		
			return new ActionRouter("mgt-printable-gross-profit-by-invoice-page");
		}

      if(formName.equals("setCustomer"))
      {
         try
         {
            fnSetCustomer(servlet, req, res);
         }
         catch (Exception ex)
         {
            req.setAttribute("errMsg", ex.getMessage());
         }
      }

		
		
		if (formName.equals("generateReport"))
		{
			fnGenerateReport(servlet, req, res);
		}
		return new ActionRouter("mgt-gross-profit-by-invoice-page");
	}

   private void fnSetCustomer(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
   {
      Log.printVerbose("inside fnSetCustomer...");
      String option = req.getParameter("option");
      Log.printVerbose("option = "+option);
      if (option == null)
      {
         return;
      }
      HttpSession session = (HttpSession) req.getSession();
      GrossProfitByInvoiceReport gpReport = (GrossProfitByInvoiceReport) session .getAttribute("mgt-gross-profit-by-invoice-report");
      
      if(option.equals("setAcc"))
      {
         String accPkid = req.getParameter("accPkid");
         Log.printVerbose("accPkid = "+accPkid);
         try
         {
            Integer pkid = new Integer(accPkid);
            CustAccountObject custObj = CustAccountNut.getObject(pkid);
            if (custObj != null)
            {
               Log.printVerbose(" setting customer...............................");
               Log.printVerbose(" custObj.name = "+custObj.name);
               gpReport.setCustomer(custObj);
               Log.printVerbose(" done setting customer...............................");
            }
            else
            {
               throw new Exception("Invalid Account");
            }
         } catch (Exception ex)
         {
            throw new Exception("Invalid Account Number!");
         }
      }

      if(option.equals("setMember"))
      {
         String memPkid = req.getParameter("memPkid");
         try
         {
            Integer pkid = new Integer(memPkid);
            CustUserObject custUserObj = CustUserNut.getObject(pkid);
            if (custUserObj != null)
            {
               gpReport.setCustUser(custUserObj);
            }
            else
            {
               throw new Exception("Invalid Contact!");
            }
         }
         catch(Exception ex)
         {
            throw new Exception("Invalid Contact!");
         }
      }

   }

	public void fnGenerateReport(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String strBranch = req.getParameter("branch");
		String dateFrom = req.getParameter("dateFrom");
		String dateTo = req.getParameter("dateTo");
		String filterCustomer = req.getParameter("filterCustomer");
		boolean boolFC = (filterCustomer!=null && filterCustomer.equals("true"))?true:false;
		
		req.setAttribute("strBranch", strBranch);
		req.setAttribute("dateFrom", dateFrom);
		req.setAttribute("dateTo", dateTo);
		
		HttpSession session = req.getSession();
		GrossProfitByInvoiceReport gpReport = (GrossProfitByInvoiceReport) session .getAttribute("mgt-gross-profit-by-invoice-report");
		Log.printVerbose("inside generate report.......................................... 1");
		if (gpReport == null)
		{
			return;
		}
		Integer branchId = null;
		try
		{
			branchId = new Integer(strBranch);
		} catch (Exception ex)
		{
		}
		Log.printVerbose("inside generate report.......................................... 2");
		gpReport.setBranch(branchId);
		gpReport.setFilterCustomer(boolFC);
		gpReport.setDateRange(dateFrom, dateTo);
		gpReport.generateReport("");
		
		fnAuditTrail(servlet, req, res);
	}
	
	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		System.out.println("Inside fnAuditTrail");
		
		HttpSession session = req.getSession();
		GrossProfitByInvoiceReport gpReport = (GrossProfitByInvoiceReport) 
												session.getAttribute("mgt-gross-profit-by-invoice-report");
		
		Integer iUserId = (Integer) session.getAttribute("userId");
		
		String strBranch = gpReport.getBranch("");
		String dateFrom = gpReport.getDateFrom("");
		String dateTo = gpReport.getDateTo("");
		CustAccountObject custObj = gpReport.getCustomer();		
		boolean boolFC = gpReport.getFilterCustomer();		
				
		if (iUserId != null)
		{
			System.out.println("userid : "+iUserId.toString());
			
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_USAGE;
			atObj.remarks = "generate-rpt: Gross Profit by Invoice";
			
			if(!"all".equals(strBranch))
			{
				BranchObject branchObj = BranchNut.getObject(new Integer(strBranch));
				
				atObj.remarks += ", Branch: " + branchObj.description;
			}
			else
			{
				atObj.remarks += ", Branch: " + strBranch;
			}
			
			atObj.remarks += ", Date: " + dateFrom + " to " + dateTo;
			
			if(boolFC && custObj != null)
				atObj.remarks += ", Customer: " + custObj.name + "(" + custObj.pkid +")";
			
			AuditTrailNut.fnCreate(atObj);
		}
		
		System.out.println("Leaving fnAuditTrail");
	}
}
