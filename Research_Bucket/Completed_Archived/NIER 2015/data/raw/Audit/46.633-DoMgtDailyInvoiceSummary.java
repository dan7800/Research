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

public class DoMgtDailyInvoiceSummary implements Action
{
	String strClassName = "DoMgtDailyInvoiceSummary";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = (String) req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("mgt-daily-invoice-summary-report-page");
		}
		if (formName.equals("popupPrint"))
		{
			return new ActionRouter("mgt-printable-daily-invoice-summary-report-page");
		}
		if (formName.equals("setBranch"))
		{
			try
			{
				fnSetBranch(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("setDate"))
		{
			try
			{
				fnSetDate(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("setDateRange"))
		{
			try
			{
				fnSetDateRange(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("generateReport"))
		{
			try
			{
				fnGenerateReport(servlet,req,res);
				fnGetReportInvSummaryValues(servlet, req, res);
			}
			catch(Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		return new ActionRouter("mgt-daily-invoice-summary-report-page");
	}

	// ///////////////////////////////////////////////////////////////////
	private void fnSetDate(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		String newDate = req.getParameter("theDate");
		if (newDate == null)
		{
			throw new Exception("Date variable is null!!");
		}
		HttpSession session = req.getSession();
		DailyInvoiceSummarySession dis = (DailyInvoiceSummarySession) session
				.getAttribute("mgt-daily-invoice-summary-session");
		if (dis == null)
		{
			dis = new DailyInvoiceSummarySession();
			session.setAttribute("mgt-daily-invoice-summary-session", dis);
		}
		dis.setDate(TimeFormat.createTimestamp(newDate));
	}


	private void fnGenerateReport(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{



      HttpSession session = req.getSession();
      DailyInvoiceSummarySession dis = (DailyInvoiceSummarySession) session
            .getAttribute("mgt-daily-invoice-summary-session");
      try
      {
         if (dis == null)
         {
            dis = new DailyInvoiceSummarySession();
            session.setAttribute("mgt-daily-invoice-summary-session", dis);
         }
      } catch (Exception ex)
      {
         throw ex;
      }

      String dateFrom = req.getParameter("dateFrom");
	  String dateTo = req.getParameter("dateTo");
	  String userId = req.getParameter("userId");
      String branchStr = req.getParameter("branchId");
      
      Integer branchId = null;
      try { branchId = new Integer(branchStr); } catch (Exception ex) { }
		dis.setBranch(branchId);

		if (dateFrom == null)
		{
			throw new Exception("Date variable is null!!");
		}
		if (dateTo == null)
		{
			throw new Exception("Date variable is null!!");
		}
		
		
		dis.setDate(TimeFormat.createTimestamp(dateFrom), TimeFormat.createTimestamp(dateTo));
		dis.setUserId(new Integer(userId));
		dis = DailyInvoiceSummarySession.generateReport(dis);
		session.setAttribute("mgt-daily-invoice-summary-session", dis);
	}




	private void fnSetDateRange(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		String dateFrom = req.getParameter("dateFrom");
		String dateTo = req.getParameter("dateTo");
		if (dateFrom == null)
		{
			throw new Exception("Date variable is null!!");
		}
		if (dateTo == null)
		{
			throw new Exception("Date variable is null!!");
		}
		HttpSession session = req.getSession();
		DailyInvoiceSummarySession dis = (DailyInvoiceSummarySession) session
				.getAttribute("mgt-daily-invoice-summary-session");
		if (dis == null)
		{
			dis = new DailyInvoiceSummarySession();
			session.setAttribute("mgt-daily-invoice-summary-session", dis);
		}
		dis.setDate(TimeFormat.createTimestamp(dateFrom), TimeFormat.createTimestamp(dateTo));
		dis = DailyInvoiceSummarySession.generateReport(dis);
		session.setAttribute("mgt-daily-invoice-summary-session", dis);
	}

	private void fnSetBranch(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		String branchStr = req.getParameter("branchId");
		Integer branchId = null;
		try
		{
			branchId = new Integer(branchStr);
		} catch (Exception ex)
		{
		}
		HttpSession session = req.getSession();
		DailyInvoiceSummarySession dis = (DailyInvoiceSummarySession) session
				.getAttribute("mgt-daily-invoice-summary-session");
		try
		{
			if (dis == null)
			{
				dis = new DailyInvoiceSummarySession();
				session.setAttribute("mgt-daily-invoice-summary-session", dis);
			}
			dis.setBranch(branchId);
		} catch (Exception ex)
		{
			throw ex;
		}
	}

	private void fnGetReportInvSummaryValues(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		fnAuditTrail(servlet, req, res);
	}

	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		if (iUserId != null && iUserId.intValue()>501)
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_USAGE;
			atObj.remarks = "mgt_rpt: daily-invoice-summary";
			AuditTrailNut.fnCreate(atObj);
		}
	}
} // end of class DoMgtDailyInvoiceSummary
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

public class DoMgtDailyInvoiceSummary implements Action
{
	String strClassName = "DoMgtDailyInvoiceSummary";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = (String) req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("mgt-daily-invoice-summary-report-page");
		}
		if (formName.equals("popupPrint"))
		{
			return new ActionRouter("mgt-printable-daily-invoice-summary-page");
		}
		if (formName.equals("setBranch"))
		{
			try
			{
				fnSetBranch(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("setDate"))
		{
			try
			{
				fnSetDate(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("setDateRange"))
		{
			try
			{
				fnSetDateRange(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("generateReport"))
		{
			fnGetReportInvSummaryValues(servlet, req, res);
		}
		return new ActionRouter("mgt-daily-invoice-summary-report-page");
	}

	// ///////////////////////////////////////////////////////////////////
	private void fnSetDate(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		String newDate = req.getParameter("theDate");
		if (newDate == null)
		{
			throw new Exception("Date variable is null!!");
		}
		HttpSession session = req.getSession();
		DailyInvoiceSummarySession dis = (DailyInvoiceSummarySession) session
				.getAttribute("mgt-daily-invoice-summary-session");
		if (dis == null)
		{
			dis = new DailyInvoiceSummarySession();
			session.setAttribute("mgt-daily-invoice-summary-session", dis);
		}
		dis.setDate(TimeFormat.createTimestamp(newDate));
	}

	private void fnSetDateRange(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		String dateFrom = req.getParameter("dateFrom");
		String dateTo = req.getParameter("dateTo");
		if (dateFrom == null)
		{
			throw new Exception("Date variable is null!!");
		}
		if (dateTo == null)
		{
			throw new Exception("Date variable is null!!");
		}
		HttpSession session = req.getSession();
		DailyInvoiceSummarySession dis = (DailyInvoiceSummarySession) session
				.getAttribute("mgt-daily-invoice-summary-session");
		if (dis == null)
		{
			dis = new DailyInvoiceSummarySession();
			session.setAttribute("mgt-daily-invoice-summary-session", dis);
		}
		dis.setDate(TimeFormat.createTimestamp(dateFrom), TimeFormat.createTimestamp(dateTo));
		dis = DailyInvoiceSummarySession.generateReport(dis);
		session.setAttribute("mgt-daily-invoice-summary-session", dis);
	}

	private void fnSetBranch(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		String branchStr = req.getParameter("branchId");
		Integer branchId = null;
		try
		{
			branchId = new Integer(branchStr);
		} catch (Exception ex)
		{
		}
		HttpSession session = req.getSession();
		DailyInvoiceSummarySession dis = (DailyInvoiceSummarySession) session
				.getAttribute("mgt-daily-invoice-summary-session");
		try
		{
			if (dis == null)
			{
				dis = new DailyInvoiceSummarySession();
				session.setAttribute("mgt-daily-invoice-summary-session", dis);
			}
			dis.setBranch(branchId);
		} catch (Exception ex)
		{
			throw ex;
		}
	}

	private void fnGetReportInvSummaryValues(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		fnAuditTrail(servlet, req, res);
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
			atObj.remarks = "mgt_rpt: daily-invoice-summary";
			AuditTrailNut.fnCreate(atObj);
		}
	}
} // end of class DoMgtDailyInvoiceSummary
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

public class DoMgtDailyInvoiceSummary implements Action
{
	String strClassName = "DoMgtDailyInvoiceSummary";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = (String) req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("mgt-daily-invoice-summary-report-page");
		}
		if (formName.equals("popupPrint"))
		{
			return new ActionRouter("mgt-printable-daily-invoice-summary-page");
		}
		if (formName.equals("setBranch"))
		{
			try
			{
				fnSetBranch(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("setDate"))
		{
			try
			{
				fnSetDate(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("setDateRange"))
		{
			try
			{
				fnSetDateRange(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("generateReport"))
		{
			fnGetReportInvSummaryValues(servlet, req, res);
		}
		return new ActionRouter("mgt-daily-invoice-summary-report-page");
	}

	// ///////////////////////////////////////////////////////////////////
	private void fnSetDate(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		String newDate = req.getParameter("theDate");
		if (newDate == null)
		{
			throw new Exception("Date variable is null!!");
		}
		HttpSession session = req.getSession();
		DailyInvoiceSummarySession dis = (DailyInvoiceSummarySession) session
				.getAttribute("mgt-daily-invoice-summary-session");
		if (dis == null)
		{
			dis = new DailyInvoiceSummarySession();
			session.setAttribute("mgt-daily-invoice-summary-session", dis);
		}
		dis.setDate(TimeFormat.createTimestamp(newDate));
	}

	private void fnSetDateRange(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		String dateFrom = req.getParameter("dateFrom");
		String dateTo = req.getParameter("dateTo");
		if (dateFrom == null)
		{
			throw new Exception("Date variable is null!!");
		}
		if (dateTo == null)
		{
			throw new Exception("Date variable is null!!");
		}
		HttpSession session = req.getSession();
		DailyInvoiceSummarySession dis = (DailyInvoiceSummarySession) session
				.getAttribute("mgt-daily-invoice-summary-session");
		if (dis == null)
		{
			dis = new DailyInvoiceSummarySession();
			session.setAttribute("mgt-daily-invoice-summary-session", dis);
		}
		dis.setDate(TimeFormat.createTimestamp(dateFrom), TimeFormat.createTimestamp(dateTo));
		dis = DailyInvoiceSummarySession.generateReport(dis);
		session.setAttribute("mgt-daily-invoice-summary-session", dis);
	}

	private void fnSetBranch(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		String branchStr = req.getParameter("branchId");
		Integer branchId = null;
		try
		{
			branchId = new Integer(branchStr);
		} catch (Exception ex)
		{
		}
		HttpSession session = req.getSession();
		DailyInvoiceSummarySession dis = (DailyInvoiceSummarySession) session
				.getAttribute("mgt-daily-invoice-summary-session");
		try
		{
			if (dis == null)
			{
				dis = new DailyInvoiceSummarySession();
				session.setAttribute("mgt-daily-invoice-summary-session", dis);
			}
			dis.setBranch(branchId);
		} catch (Exception ex)
		{
			throw ex;
		}
	}

	private void fnGetReportInvSummaryValues(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		fnAuditTrail(servlet, req, res);
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
			atObj.remarks = "mgt_rpt: daily-invoice-summary";
			AuditTrailNut.fnCreate(atObj);
		}
	}
} // end of class DoMgtDailyInvoiceSummary
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

public class DoMgtDailyInvoiceSummary implements Action
{
	String strClassName = "DoMgtDailyInvoiceSummary";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = (String) req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("mgt-daily-invoice-summary-report-page");
		}
		if (formName.equals("popupPrint"))
		{
			return new ActionRouter("mgt-printable-daily-invoice-summary-page");
		}
		if (formName.equals("setBranch"))
		{
			try
			{
				fnSetBranch(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("setDate"))
		{
			try
			{
				fnSetDate(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("setDateRange"))
		{
			try
			{
				fnSetDateRange(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("generateReport"))
		{
			fnGetReportInvSummaryValues(servlet, req, res);
		}
		return new ActionRouter("mgt-daily-invoice-summary-report-page");
	}

	// ///////////////////////////////////////////////////////////////////
	private void fnSetDate(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		String newDate = req.getParameter("theDate");
		if (newDate == null)
		{
			throw new Exception("Date variable is null!!");
		}
		HttpSession session = req.getSession();
		DailyInvoiceSummarySession dis = (DailyInvoiceSummarySession) session
				.getAttribute("mgt-daily-invoice-summary-session");
		if (dis == null)
		{
			dis = new DailyInvoiceSummarySession();
			session.setAttribute("mgt-daily-invoice-summary-session", dis);
		}
		dis.setDate(TimeFormat.createTimestamp(newDate));
	}

	private void fnSetDateRange(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		String dateFrom = req.getParameter("dateFrom");
		String dateTo = req.getParameter("dateTo");
		if (dateFrom == null)
		{
			throw new Exception("Date variable is null!!");
		}
		if (dateTo == null)
		{
			throw new Exception("Date variable is null!!");
		}
		HttpSession session = req.getSession();
		DailyInvoiceSummarySession dis = (DailyInvoiceSummarySession) session
				.getAttribute("mgt-daily-invoice-summary-session");
		if (dis == null)
		{
			dis = new DailyInvoiceSummarySession();
			session.setAttribute("mgt-daily-invoice-summary-session", dis);
		}
		dis.setDate(TimeFormat.createTimestamp(dateFrom), TimeFormat.createTimestamp(dateTo));
		dis = DailyInvoiceSummarySession.generateReport(dis);
		session.setAttribute("mgt-daily-invoice-summary-session", dis);
	}

	private void fnSetBranch(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		String branchStr = req.getParameter("branchId");
		Integer branchId = null;
		try
		{
			branchId = new Integer(branchStr);
		} catch (Exception ex)
		{
		}
		HttpSession session = req.getSession();
		DailyInvoiceSummarySession dis = (DailyInvoiceSummarySession) session
				.getAttribute("mgt-daily-invoice-summary-session");
		try
		{
			if (dis == null)
			{
				dis = new DailyInvoiceSummarySession();
				session.setAttribute("mgt-daily-invoice-summary-session", dis);
			}
			dis.setBranch(branchId);
		} catch (Exception ex)
		{
			throw ex;
		}
	}

	private void fnGetReportInvSummaryValues(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		fnAuditTrail(servlet, req, res);
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
			atObj.remarks = "mgt_rpt: daily-invoice-summary";
			AuditTrailNut.fnCreate(atObj);
		}
	}
} // end of class DoMgtDailyInvoiceSummary
