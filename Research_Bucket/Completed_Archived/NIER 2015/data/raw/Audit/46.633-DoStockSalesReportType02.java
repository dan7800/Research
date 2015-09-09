/*==========================================================
 *
 * Copyright of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.info)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.inventory;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;

import java.util.*;
import java.sql.*;
import java.math.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;
import com.vlee.bean.reports.*;

public class DoStockSalesReportType02 implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = (String) req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("inv-stock-sales-report-type02-page");
		}
		if (formName.equals("popupPrint"))
		{
			return new ActionRouter("inv-print-sales-report-type02-page");
		}
//		if (formName.equals("setLocation"))
//		{
//			fnSetLocation(servlet, req, res);
//		}
		if (formName.equals("setDateRange"))
		{
			fnSetDateRange(servlet, req, res);
		}
		if (formName.equals("setCodeRange"))
		{
			fnSetCodeRange(servlet, req, res);
		}
		if (formName.equals("generateReport"))
		{
			fnGenerateReport(servlet, req, res);
		}
		if (formName.equals("getReport"))
		{
			fnGetReport(servlet, req, res);
		}
		return new ActionRouter("inv-stock-sales-report-type02-page");
	}

	// /////////////////////////////////////////////////////////////////////////
	private void fnGetReport(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		StockSalesReportType02Session ssrt2 = (StockSalesReportType02Session) session
				.getAttribute("inv-stock-sales-report-type02-session");
		String codeStart = req.getParameter("codeStart");
		String codeEnd = req.getParameter("codeEnd");
		boolean useCode = false;
		String useCodeRange = req.getParameter("useCodeRange");
		if (useCodeRange != null && useCodeRange.equals("true"))
		{ useCode = true; }
		ssrt2.setCodeRange(useCode, codeStart, codeEnd);

		Timestamp dateFrom = TimeFormat.createTimestamp(req.getParameter("dateFrom"));
		Timestamp dateTo = TimeFormat.createTimestamp(req.getParameter("dateTo"));
		ssrt2.setDateRange(dateFrom, dateTo);
		//Integer locationId = null;
		//try { locationId = new Integer(req.getParameter("locationId")); } catch (Exception ex) { }
		//try { ssrt2.setLocation(locationId); } catch (Exception ex) { }

		String filterSalesman = req.getParameter("filterSalesman");
		String salesman = req.getParameter("salesman");

		boolean bFilterSalesman = (filterSalesman!=null && filterSalesman.equals("true"))?true:false;
		Integer iSalesman = null;
		try{ iSalesman = new Integer(salesman);}catch(Exception ex){ }
		ssrt2.setFilterSalesman(bFilterSalesman);
		ssrt2.setSalesman(iSalesman);


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
		ssrt2.setCategoryFilter((filterCategory0 != null), new Integer(
            idCategory0), (filterCategory1 != null), category1, (filterCategory2 != null), category2,
            (filterCategory3 != null), category3, (filterCategory4 != null), category4, (filterCategory5 != null),
            category5);

		// 20080603 Jimmy
		ssrt2.vecLocation.clear();
		String selectedLocation[] = req.getParameterValues("selectedLocation");
		if (selectedLocation!= null) {
			for(int cnt1=0;cnt1<selectedLocation.length;cnt1++)
			{
				ssrt2.vecLocation.add(selectedLocation[cnt1]);
			}
		}
		String orderBy = req.getParameter("orderBy");
		ssrt2.setOrderBy(orderBy);
		ssrt2.generateReport("testing");
		
		fnAuditTrail(servlet, req, res);
	}

	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		System.out.println("Inside fnAuditTrail");
		
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
				
		//String locationId = req.getParameter("locationId");		
				
		String dateFrom =req.getParameter("dateFrom");
		String dateTo = req.getParameter("dateTo");
		
		String filterSalesman = req.getParameter("filterSalesman");
		String salesman = req.getParameter("salesman");
		boolean bFilterSalesman = (filterSalesman!=null && filterSalesman.equals("true"))?true:false;
		
		String codeStart = req.getParameter("codeStart");
		String codeEnd = req.getParameter("codeEnd");		
		String useCodeRange = req.getParameter("useCodeRange");
		boolean bFilterCode = (useCodeRange!=null && useCodeRange.equals("true"))?true:false;				
		
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
		boolean bFilterCategory0 = (filterCategory0!=null)?true:false;
		boolean bFilterCategory1 = (filterCategory1!=null)?true:false;
		boolean bFilterCategory2 = (filterCategory2!=null)?true:false;
		boolean bFilterCategory3 = (filterCategory3!=null)?true:false;
		boolean bFilterCategory4 = (filterCategory4!=null)?true:false;
		boolean bFilterCategory5 = (filterCategory5!=null)?true:false;
		
		if (iUserId != null)
		{
			System.out.println("userid : "+iUserId.toString());
			
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_USAGE;
			atObj.remarks = "generate-rpt: Stock Sales Report";
			
//			if(!"all".equals(locationId))
//			{
//				LocationObject locationObj = LocationNut.getObject(new Integer(locationId));
//				
//				atObj.remarks += ", Location: " + locationObj.description;
//			}
//			else
//			{
//				atObj.remarks += ", Location: " + locationId;
//			}
			
			atObj.remarks += ", Date: " + dateFrom + " to " + dateTo;
			
			if(bFilterSalesman)
			{
				try
				{
					atObj.remarks += ", Salesman: " + UserNut.getUserName(new Integer(salesman));
				}
				catch(Exception ex)
				{
					System.out.println("Exception : "+ex.toString());
				}
			}
			
			if(bFilterCode)
			{
				atObj.remarks += ", Item_Code: " + codeStart + " to " + codeEnd;
			}
			
			if(bFilterCategory0)
			{
				try
				{
					atObj.remarks += ", Category0: " + CategoryNut.getObject(new Integer(idCategory0)).categoryCode;
				}
				catch(Exception ex)
				{
					System.out.println("Exception : "+ex.toString());
				}
			}
			
			if(bFilterCategory1)
			{
				atObj.remarks += ", Cat1: " + category1;
			}
			
			if(bFilterCategory2)
			{
				atObj.remarks += ", Cat2: " + category2;
			}
			
			if(bFilterCategory3)
			{
				atObj.remarks += ", Cat3: " + category3;
			}
			
			if(bFilterCategory4)
			{
				atObj.remarks += ", Cat4: " + category4;
			}
			
			if(bFilterCategory5)
			{
				atObj.remarks += ", Cat5: " + category5;
			}
			
			
			AuditTrailNut.fnCreate(atObj);
		}
		
		System.out.println("Leaving fnAuditTrail");
	}
	
	private void fnGenerateReport(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		StockSalesReportType02Session ssrt2 = (StockSalesReportType02Session) session
				.getAttribute("inv-stock-sales-report-type02-session");
		ssrt2.generateReport("testing");
	}

	private void fnSetCodeRange(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		StockSalesReportType02Session ssrt2 = (StockSalesReportType02Session) session
				.getAttribute("inv-stock-sales-report-type02-session");
		String codeStart = req.getParameter("codeStart");
		String codeEnd = req.getParameter("codeEnd");
		boolean useCode = false;
		String useCodeRange = req.getParameter("useCodeRange");
		if (useCodeRange != null && useCodeRange.equals("true"))
		{
			useCode = true;
		}
		ssrt2.setCodeRange(useCode, codeStart, codeEnd);
	}

	private void fnSetDateRange(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		StockSalesReportType02Session ssrt2 = (StockSalesReportType02Session) session
				.getAttribute("inv-stock-sales-report-type02-session");
		Timestamp dateFrom = TimeFormat.createTimestamp(req.getParameter("dateFrom"));
		Timestamp dateTo = TimeFormat.createTimestamp(req.getParameter("dateTo"));
		ssrt2.setDateRange(dateFrom, dateTo);
	}

//	private void fnSetLocation(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
//	{
//		HttpSession session = req.getSession();
//		StockSalesReportType02Session ssrt2 = (StockSalesReportType02Session) session
//				.getAttribute("inv-stock-sales-report-type02-session");
//		Integer locationId = null;
//		try
//		{
//			locationId = new Integer(req.getParameter("locationId"));
//		} catch (Exception ex)
//		{
//		}
//		try
//		{
//			ssrt2.setLocation(locationId);
//		} catch (Exception ex)
//		{
//		}
//	}
}
