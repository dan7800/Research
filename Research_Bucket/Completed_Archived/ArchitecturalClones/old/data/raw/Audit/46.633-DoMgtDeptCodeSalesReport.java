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

public class DoMgtDeptCodeSalesReport implements Action
{
	String strClassName = "DoMgtDeptCodeSalesReport";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = (String) req.getParameter("formName");
		String fwdPage = (String) req.getParameter("fwdPage");
		if (fwdPage == null)
		{
			fwdPage = "mgt-deptcode-sales-report-page";
		}
		fnPreserveOptions(servlet, req, res);
		if (formName == null)
		{
			return new ActionRouter(fwdPage);
		}
		if (formName.equals("getReport"))
		{
			fnGetReport(servlet, req, res);
		}
		return new ActionRouter(fwdPage);
	}

	public void fnGetReport(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String custSvcCtr = req.getParameter("custSvcCtr");
		Integer iSvcCtr = null;
		try
		{
			iSvcCtr = new Integer(custSvcCtr);
		} catch (Exception ex)
		{ /* do nothing */
		}
		String strDateFrom = req.getParameter("dateFrom");
		Timestamp dateFrom = TimeFormat.createTimestamp(strDateFrom);
		String strDateTo = req.getParameter("dateTo");
		Timestamp dateTo = TimeFormat.createTimestamp(strDateTo);
		dateTo = TimeFormat.add(dateTo, 0, 0, 1);
		DeptCodeReport dcr = new DeptCodeReport(iSvcCtr, dateFrom, dateTo);
		dcr.reportTitle = " Dept Code Sales Report ";
		dcr = InvoiceItemNut.getDeptCodeReport(dcr);
		req.setAttribute("deptCodeReport", dcr);
	}

	private void fnPreserveOptions(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String custSvcCtr = req.getParameter("custSvcCtr");
		req.setAttribute("custSvcCtr", custSvcCtr);
		String dateFrom = req.getParameter("dateFrom");
		req.setAttribute("dateFrom", dateFrom);
		String dateTo = req.getParameter("dateTo");
		req.setAttribute("dateTo", dateTo);
	}

	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		if (iUserId != null)
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_USAGE;
			atObj.remarks = "mgt_report: deptcode-sales-report";
			AuditTrailNut.fnCreate(atObj);
		}
	}
} // end of class DoMgtDeptCodeSalesReport
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

public class DoMgtDeptCodeSalesReport implements Action
{
	String strClassName = "DoMgtDeptCodeSalesReport";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = (String) req.getParameter("formName");
		String fwdPage = (String) req.getParameter("fwdPage");
		if (fwdPage == null)
		{
			fwdPage = "mgt-deptcode-sales-report-page";
		}
		fnPreserveOptions(servlet, req, res);
		if (formName == null)
		{
			return new ActionRouter(fwdPage);
		}
		if (formName.equals("getReport"))
		{
			fnGetReport(servlet, req, res);
		}
		return new ActionRouter(fwdPage);
	}

	public void fnGetReport(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String custSvcCtr = req.getParameter("custSvcCtr");
		Integer iSvcCtr = null;
		try
		{
			iSvcCtr = new Integer(custSvcCtr);
		} catch (Exception ex)
		{ /* do nothing */
		}
		String strDateFrom = req.getParameter("dateFrom");
		Timestamp dateFrom = TimeFormat.createTimestamp(strDateFrom);
		String strDateTo = req.getParameter("dateTo");
		Timestamp dateTo = TimeFormat.createTimestamp(strDateTo);
		dateTo = TimeFormat.add(dateTo, 0, 0, 1);
		DeptCodeReport dcr = new DeptCodeReport(iSvcCtr, dateFrom, dateTo);
		dcr.reportTitle = " Dept Code Sales Report ";
		dcr = InvoiceItemNut.getDeptCodeReport(dcr);
		req.setAttribute("deptCodeReport", dcr);
	}

	private void fnPreserveOptions(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String custSvcCtr = req.getParameter("custSvcCtr");
		req.setAttribute("custSvcCtr", custSvcCtr);
		String dateFrom = req.getParameter("dateFrom");
		req.setAttribute("dateFrom", dateFrom);
		String dateTo = req.getParameter("dateTo");
		req.setAttribute("dateTo", dateTo);
	}

	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		if (iUserId != null)
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_USAGE;
			atObj.remarks = "mgt_report: deptcode-sales-report";
			AuditTrailNut.fnCreate(atObj);
		}
	}
} // end of class DoMgtDeptCodeSalesReport
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

public class DoMgtDeptCodeSalesReport implements Action
{
	String strClassName = "DoMgtDeptCodeSalesReport";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = (String) req.getParameter("formName");
		String fwdPage = (String) req.getParameter("fwdPage");
		if (fwdPage == null)
		{
			fwdPage = "mgt-deptcode-sales-report-page";
		}
		fnPreserveOptions(servlet, req, res);
		if (formName == null)
		{
			return new ActionRouter(fwdPage);
		}
		if (formName.equals("getReport"))
		{
			fnGetReport(servlet, req, res);
		}
		return new ActionRouter(fwdPage);
	}

	public void fnGetReport(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String custSvcCtr = req.getParameter("custSvcCtr");
		Integer iSvcCtr = null;
		try
		{
			iSvcCtr = new Integer(custSvcCtr);
		} catch (Exception ex)
		{ /* do nothing */
		}
		String strDateFrom = req.getParameter("dateFrom");
		Timestamp dateFrom = TimeFormat.createTimestamp(strDateFrom);
		String strDateTo = req.getParameter("dateTo");
		Timestamp dateTo = TimeFormat.createTimestamp(strDateTo);
		dateTo = TimeFormat.add(dateTo, 0, 0, 1);
		DeptCodeReport dcr = new DeptCodeReport(iSvcCtr, dateFrom, dateTo);
		dcr.reportTitle = " Dept Code Sales Report ";
		dcr = InvoiceItemNut.getDeptCodeReport(dcr);
		req.setAttribute("deptCodeReport", dcr);
	}

	private void fnPreserveOptions(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String custSvcCtr = req.getParameter("custSvcCtr");
		req.setAttribute("custSvcCtr", custSvcCtr);
		String dateFrom = req.getParameter("dateFrom");
		req.setAttribute("dateFrom", dateFrom);
		String dateTo = req.getParameter("dateTo");
		req.setAttribute("dateTo", dateTo);
	}

	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		if (iUserId != null)
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_USAGE;
			atObj.remarks = "mgt_report: deptcode-sales-report";
			AuditTrailNut.fnCreate(atObj);
		}
	}
} // end of class DoMgtDeptCodeSalesReport
