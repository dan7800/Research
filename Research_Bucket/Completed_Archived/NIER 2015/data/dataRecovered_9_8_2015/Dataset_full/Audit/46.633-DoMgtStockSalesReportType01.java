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

public class DoMgtStockSalesReportType01 implements Action
{
	String strClassName = "DoMgtStockSalesReportType01";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = (String) req.getParameter("formName");
		String fwdPage = (String) req.getParameter("fwdPage");
		if (fwdPage == null)
		{
			fwdPage = "mgt-stock-sales-report-type01-page";
		}
		Vector vecCustSvcCtr = CustServiceCenterNut.getValueObjectsGiven(CustServiceCenterBean.STATUS,
				CustServiceCenterBean.STATUS_ACTIVE, (String) null, (String) null);
		req.setAttribute("vecCustSvcCtr", vecCustSvcCtr);
		String svcCtr = req.getParameter("svcCtr");
		req.setAttribute("svcCtr", svcCtr);
		String dateFrom = req.getParameter("dateFrom");
		req.setAttribute("dateFrom", dateFrom);
		String dateTo = req.getParameter("dateTo");
		req.setAttribute("dateTo", dateTo);
		if (formName == null)
		{
			return new ActionRouter(fwdPage);
		}
		if (formName.equals("getStockSalesReport"))
		{
			fnGetReportType01Values(servlet, req, res);
			return new ActionRouter(fwdPage);
		}
		return new ActionRouter(fwdPage);
	}

	public void fnGetReportType01Values(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		try
		{
			String svcCtr = req.getParameter("svcCtr");
			req.setAttribute("svcCtr", svcCtr);
			String dateFrom = req.getParameter("dateFrom");
			req.setAttribute("dateFrom", dateFrom);
			String dateTo = req.getParameter("dateTo");
			req.setAttribute("dateTo", dateTo);
			if (svcCtr == null || dateFrom == null || dateTo == null)
			{
				return;
			}
			Integer iSvcCtr = null;
			try
			{
				iSvcCtr = new Integer(svcCtr);
			} catch (Exception ex)
			{ // do nothing, when "all" is selected
			}
			Timestamp tsFrom = TimeFormat.createTimestamp(dateFrom);
			Timestamp tsTo = TimeFormat.createTimestamp(dateTo);
			tsTo = TimeFormat.add(tsTo, 0, 0, 1);
			POSItemSalesReport posReport = POSItemNut.getStockSalesReport(iSvcCtr, tsFrom, tsTo);
			req.setAttribute("posReport", posReport);
			fnAuditTrail(servlet, req, res);
		}// end try
		catch (Exception ex)
		{
			ex.printStackTrace();
		}// end catch
	}// end of function

	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		if (iUserId != null)
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_USAGE;
			atObj.remarks = "mgt_report: stock-sales-report";
			AuditTrailNut.fnCreate(atObj);
		}
	}
} // end of DoMgtStockSalesReportType01
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

public class DoMgtStockSalesReportType01 implements Action
{
	String strClassName = "DoMgtStockSalesReportType01";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = (String) req.getParameter("formName");
		String fwdPage = (String) req.getParameter("fwdPage");
		if (fwdPage == null)
		{
			fwdPage = "mgt-stock-sales-report-type01-page";
		}
		Vector vecCustSvcCtr = CustServiceCenterNut.getValueObjectsGiven(CustServiceCenterBean.STATUS,
				CustServiceCenterBean.STATUS_ACTIVE, (String) null, (String) null);
		req.setAttribute("vecCustSvcCtr", vecCustSvcCtr);
		String svcCtr = req.getParameter("svcCtr");
		req.setAttribute("svcCtr", svcCtr);
		String dateFrom = req.getParameter("dateFrom");
		req.setAttribute("dateFrom", dateFrom);
		String dateTo = req.getParameter("dateTo");
		req.setAttribute("dateTo", dateTo);
		if (formName == null)
		{
			return new ActionRouter(fwdPage);
		}
		if (formName.equals("getStockSalesReport"))
		{
			fnGetReportType01Values(servlet, req, res);
			return new ActionRouter(fwdPage);
		}
		return new ActionRouter(fwdPage);
	}

	public void fnGetReportType01Values(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		try
		{
			String svcCtr = req.getParameter("svcCtr");
			req.setAttribute("svcCtr", svcCtr);
			String dateFrom = req.getParameter("dateFrom");
			req.setAttribute("dateFrom", dateFrom);
			String dateTo = req.getParameter("dateTo");
			req.setAttribute("dateTo", dateTo);
			if (svcCtr == null || dateFrom == null || dateTo == null)
			{
				return;
			}
			Integer iSvcCtr = null;
			try
			{
				iSvcCtr = new Integer(svcCtr);
			} catch (Exception ex)
			{ // do nothing, when "all" is selected
			}
			Timestamp tsFrom = TimeFormat.createTimestamp(dateFrom);
			Timestamp tsTo = TimeFormat.createTimestamp(dateTo);
			tsTo = TimeFormat.add(tsTo, 0, 0, 1);
			POSItemSalesReport posReport = POSItemNut.getStockSalesReport(iSvcCtr, tsFrom, tsTo);
			req.setAttribute("posReport", posReport);
			fnAuditTrail(servlet, req, res);
		}// end try
		catch (Exception ex)
		{
			ex.printStackTrace();
		}// end catch
	}// end of function

	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		if (iUserId != null)
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_USAGE;
			atObj.remarks = "mgt_report: stock-sales-report";
			AuditTrailNut.fnCreate(atObj);
		}
	}
} // end of DoMgtStockSalesReportType01
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

public class DoMgtStockSalesReportType01 implements Action
{
	String strClassName = "DoMgtStockSalesReportType01";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = (String) req.getParameter("formName");
		String fwdPage = (String) req.getParameter("fwdPage");
		if (fwdPage == null)
		{
			fwdPage = "mgt-stock-sales-report-type01-page";
		}
		Vector vecCustSvcCtr = CustServiceCenterNut.getValueObjectsGiven(CustServiceCenterBean.STATUS,
				CustServiceCenterBean.STATUS_ACTIVE, (String) null, (String) null);
		req.setAttribute("vecCustSvcCtr", vecCustSvcCtr);
		String svcCtr = req.getParameter("svcCtr");
		req.setAttribute("svcCtr", svcCtr);
		String dateFrom = req.getParameter("dateFrom");
		req.setAttribute("dateFrom", dateFrom);
		String dateTo = req.getParameter("dateTo");
		req.setAttribute("dateTo", dateTo);
		if (formName == null)
		{
			return new ActionRouter(fwdPage);
		}
		if (formName.equals("getStockSalesReport"))
		{
			fnGetReportType01Values(servlet, req, res);
			return new ActionRouter(fwdPage);
		}
		return new ActionRouter(fwdPage);
	}

	public void fnGetReportType01Values(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		try
		{
			String svcCtr = req.getParameter("svcCtr");
			req.setAttribute("svcCtr", svcCtr);
			String dateFrom = req.getParameter("dateFrom");
			req.setAttribute("dateFrom", dateFrom);
			String dateTo = req.getParameter("dateTo");
			req.setAttribute("dateTo", dateTo);
			if (svcCtr == null || dateFrom == null || dateTo == null)
			{
				return;
			}
			Integer iSvcCtr = null;
			try
			{
				iSvcCtr = new Integer(svcCtr);
			} catch (Exception ex)
			{ // do nothing, when "all" is selected
			}
			Timestamp tsFrom = TimeFormat.createTimestamp(dateFrom);
			Timestamp tsTo = TimeFormat.createTimestamp(dateTo);
			tsTo = TimeFormat.add(tsTo, 0, 0, 1);
			POSItemSalesReport posReport = POSItemNut.getStockSalesReport(iSvcCtr, tsFrom, tsTo);
			req.setAttribute("posReport", posReport);
			fnAuditTrail(servlet, req, res);
		}// end try
		catch (Exception ex)
		{
			ex.printStackTrace();
		}// end catch
	}// end of function

	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		if (iUserId != null)
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_USAGE;
			atObj.remarks = "mgt_report: stock-sales-report";
			AuditTrailNut.fnCreate(atObj);
		}
	}
} // end of DoMgtStockSalesReportType01
