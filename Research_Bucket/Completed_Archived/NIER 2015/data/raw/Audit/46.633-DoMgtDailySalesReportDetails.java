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

public class DoMgtDailySalesReportDetails implements Action
{
	String strClassName = "DoMgtDailySalesReportDetails";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = (String) req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("mgt-daily-sales-report-details-page");
		}
		if (formName.equals("popupPrint"))
		{
			return new ActionRouter("mgt-printable-daily-sales-report-details-page");
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
			fnGetReportType02Values(servlet, req, res);
		}
		return new ActionRouter("mgt-daily-sales-report-details-page");
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
		DailySalesReportType02Session dsr02 = (DailySalesReportType02Session) session
				.getAttribute("mgt-daily-sales-report-details-session");
		if (dsr02 == null)
		{
			dsr02 = new DailySalesReportType02Session();
			session.setAttribute("mgt-daily-sales-report-details-session", dsr02);
		}
		dsr02.setDate(TimeFormat.createTimestamp(newDate));
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
		DailySalesReportType02Session dsr02 = (DailySalesReportType02Session) session
				.getAttribute("mgt-daily-sales-report-details-session");
		if (dsr02 == null)
		{
			dsr02 = new DailySalesReportType02Session();
			session.setAttribute("mgt-daily-sales-report-details-session", dsr02);
		}
		dsr02.setDate(TimeFormat.createTimestamp(dateFrom), TimeFormat.createTimestamp(dateTo));
		// dsr02.generateReport();
	}

	private void fnSetBranch(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		Integer branchId = null;
		try
		{
			branchId = new Integer(req.getParameter("branchId"));
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		HttpSession session = req.getSession();
		DailySalesReportType02Session dsr02 = (DailySalesReportType02Session) session
				.getAttribute("mgt-daily-sales-report-details-session");
		try
		{
			if (dsr02 == null)
			{
				dsr02 = new DailySalesReportType02Session();
				session.setAttribute("mgt-daily-sales-report-details-session", dsr02);
			}
			dsr02.setBranch(branchId);
		} catch (Exception ex)
		{
			throw ex;
		}
	}

	private void fnGetReportType02Values(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
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
			atObj.remarks = "mgt_rpt: daily-sales-report-t02";
			AuditTrailNut.fnCreate(atObj);
		}
	}
} // end of class DoMgtDailySalesReportType02
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

public class DoMgtDailySalesReportDetails implements Action
{
	String strClassName = "DoMgtDailySalesReportDetails";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = (String) req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("mgt-daily-sales-report-details-page");
		}
		if (formName.equals("popupPrint"))
		{
			return new ActionRouter("mgt-printable-daily-sales-report-details-page");
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
			fnGetReportType02Values(servlet, req, res);
		}
		return new ActionRouter("mgt-daily-sales-report-details-page");
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
		DailySalesReportType02Session dsr02 = (DailySalesReportType02Session) session
				.getAttribute("mgt-daily-sales-report-details-session");
		if (dsr02 == null)
		{
			dsr02 = new DailySalesReportType02Session();
			session.setAttribute("mgt-daily-sales-report-details-session", dsr02);
		}
		dsr02.setDate(TimeFormat.createTimestamp(newDate));
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
		DailySalesReportType02Session dsr02 = (DailySalesReportType02Session) session
				.getAttribute("mgt-daily-sales-report-details-session");
		if (dsr02 == null)
		{
			dsr02 = new DailySalesReportType02Session();
			session.setAttribute("mgt-daily-sales-report-details-session", dsr02);
		}
		dsr02.setDate(TimeFormat.createTimestamp(dateFrom), TimeFormat.createTimestamp(dateTo));
		// dsr02.generateReport();
	}

	private void fnSetBranch(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		Integer branchId = null;
		try
		{
			branchId = new Integer(req.getParameter("branchId"));
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		HttpSession session = req.getSession();
		DailySalesReportType02Session dsr02 = (DailySalesReportType02Session) session
				.getAttribute("mgt-daily-sales-report-details-session");
		try
		{
			if (dsr02 == null)
			{
				dsr02 = new DailySalesReportType02Session();
				session.setAttribute("mgt-daily-sales-report-details-session", dsr02);
			}
			dsr02.setBranch(branchId);
		} catch (Exception ex)
		{
			throw ex;
		}
	}

	private void fnGetReportType02Values(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
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
			atObj.remarks = "mgt_rpt: daily-sales-report-t02";
			AuditTrailNut.fnCreate(atObj);
		}
	}
} // end of class DoMgtDailySalesReportType02
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

public class DoMgtDailySalesReportDetails implements Action
{
	String strClassName = "DoMgtDailySalesReportDetails";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = (String) req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("mgt-daily-sales-report-details-page");
		}
		if (formName.equals("popupPrint"))
		{
			return new ActionRouter("mgt-printable-daily-sales-report-details-page");
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
			fnGetReportType02Values(servlet, req, res);
		}
		return new ActionRouter("mgt-daily-sales-report-details-page");
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
		DailySalesReportType02Session dsr02 = (DailySalesReportType02Session) session
				.getAttribute("mgt-daily-sales-report-details-session");
		if (dsr02 == null)
		{
			dsr02 = new DailySalesReportType02Session();
			session.setAttribute("mgt-daily-sales-report-details-session", dsr02);
		}
		dsr02.setDate(TimeFormat.createTimestamp(newDate));
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
		DailySalesReportType02Session dsr02 = (DailySalesReportType02Session) session
				.getAttribute("mgt-daily-sales-report-details-session");
		if (dsr02 == null)
		{
			dsr02 = new DailySalesReportType02Session();
			session.setAttribute("mgt-daily-sales-report-details-session", dsr02);
		}
		dsr02.setDate(TimeFormat.createTimestamp(dateFrom), TimeFormat.createTimestamp(dateTo));
		// dsr02.generateReport();
	}

	private void fnSetBranch(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		Integer branchId = null;
		try
		{
			branchId = new Integer(req.getParameter("branchId"));
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		HttpSession session = req.getSession();
		DailySalesReportType02Session dsr02 = (DailySalesReportType02Session) session
				.getAttribute("mgt-daily-sales-report-details-session");
		try
		{
			if (dsr02 == null)
			{
				dsr02 = new DailySalesReportType02Session();
				session.setAttribute("mgt-daily-sales-report-details-session", dsr02);
			}
			dsr02.setBranch(branchId);
		} catch (Exception ex)
		{
			throw ex;
		}
	}

	private void fnGetReportType02Values(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
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
			atObj.remarks = "mgt_rpt: daily-sales-report-t02";
			AuditTrailNut.fnCreate(atObj);
		}
	}
} // end of class DoMgtDailySalesReportType02
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

public class DoMgtDailySalesReportDetails implements Action
{
	String strClassName = "DoMgtDailySalesReportDetails";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = (String) req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("mgt-daily-sales-report-details-page");
		}
		if (formName.equals("popupPrint"))
		{
			return new ActionRouter("mgt-printable-daily-sales-report-details-page");
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
			fnGetReportType02Values(servlet, req, res);
		}
		return new ActionRouter("mgt-daily-sales-report-details-page");
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
		DailySalesReportType02Session dsr02 = (DailySalesReportType02Session) session
				.getAttribute("mgt-daily-sales-report-details-session");
		if (dsr02 == null)
		{
			dsr02 = new DailySalesReportType02Session();
			session.setAttribute("mgt-daily-sales-report-details-session", dsr02);
		}
		dsr02.setDate(TimeFormat.createTimestamp(newDate));
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
		DailySalesReportType02Session dsr02 = (DailySalesReportType02Session) session
				.getAttribute("mgt-daily-sales-report-details-session");
		if (dsr02 == null)
		{
			dsr02 = new DailySalesReportType02Session();
			session.setAttribute("mgt-daily-sales-report-details-session", dsr02);
		}
		dsr02.setDate(TimeFormat.createTimestamp(dateFrom), TimeFormat.createTimestamp(dateTo));
		// dsr02.generateReport();
	}

	private void fnSetBranch(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		Integer branchId = null;
		try
		{
			branchId = new Integer(req.getParameter("branchId"));
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		HttpSession session = req.getSession();
		DailySalesReportType02Session dsr02 = (DailySalesReportType02Session) session
				.getAttribute("mgt-daily-sales-report-details-session");
		try
		{
			if (dsr02 == null)
			{
				dsr02 = new DailySalesReportType02Session();
				session.setAttribute("mgt-daily-sales-report-details-session", dsr02);
			}
			dsr02.setBranch(branchId);
		} catch (Exception ex)
		{
			throw ex;
		}
	}

	private void fnGetReportType02Values(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
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
			atObj.remarks = "mgt_rpt: daily-sales-report-t02";
			AuditTrailNut.fnCreate(atObj);
		}
	}
} // end of class DoMgtDailySalesReportType02
