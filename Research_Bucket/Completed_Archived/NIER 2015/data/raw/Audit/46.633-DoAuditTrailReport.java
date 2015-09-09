/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.sysadmin;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.application.*;

public class DoAuditTrailReport implements Action
{
	private String strClassName = "DoAuditTrailReport";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Get the formName
		String formName = req.getParameter("formName");
		String fwdPage = req.getParameter("fwdPage");
		String userId = req.getParameter("userId");
		req.setAttribute("userId", userId);
		String namespace = req.getParameter("namespace");
		req.setAttribute("namespace", namespace);
		String auditType = req.getParameter("auditType");
		req.setAttribute("auditType", auditType);
		String auditLevel = req.getParameter("auditLevel");
		req.setAttribute("auditLevel", auditLevel);
		String keyword = req.getParameter("keyword");
		req.setAttribute("keyword", keyword);
		String dateFrom = req.getParameter("dateFrom");
		req.setAttribute("dateFrom", dateFrom);
		String dateTo = req.getParameter("dateTo");
		req.setAttribute("dateTo", dateTo);
		String state = req.getParameter("state");
		req.setAttribute("state", state);
		String status = req.getParameter("status");
		req.setAttribute("status", status);
		Vector vecUser = UserNut.getValueObjectsGiven((String) null, (String) null, (String) null, (String) null,
				(String) null, (String) null);
			
		req.setAttribute("vecUser", vecUser);
		
		
		if (fwdPage == null)
		{
			fwdPage = "sa-audit-trail-report-page";
		}
		if (formName == null)
		{
			return new ActionRouter(fwdPage);
		}
		if (formName.equals("getAuditTrailReport"))
		{
			fnGetAuditTrailReport(servlet, req, res);
			return new ActionRouter(fwdPage);
		}
		return new ActionRouter(fwdPage);
	}

	protected void fnGetAuditTrailReport(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String userId = req.getParameter("userId");
		String namespace = req.getParameter("namespace");
		String auditType = req.getParameter("auditType");
		String auditLevel = req.getParameter("auditLevel");
		String keyword = req.getParameter("keyword");
		String dateFrom = req.getParameter("dateFrom");
		String dateTo = req.getParameter("dateTo");
		String state = req.getParameter("state");
		String status = req.getParameter("status");
		
		// 20080109 Jimmy - add branch and filter
		String userBranch = req.getParameter("userBranch");
						
		Integer iBranch = null;
		try
		{ 
			iBranch = new Integer(userBranch);
		} catch (Exception ex)
		{ /* do nothing */
		}
		
		req.setAttribute("userBranch", userBranch);
		
		String checkUserBranch = (req.getParameter("checkBranch") != null)? req.getParameter("checkBranch"):req.getParameter("checkUser");
		req.setAttribute("checkUserBranch", checkUserBranch);
		
		// end
		
		
		Integer iUserId = null;
		try
		{
			iUserId = new Integer(userId);
		} catch (Exception ex)
		{ /* do nothing */
		}
		
		// 20080109 Jimmy
		if (checkUserBranch != null )
		{
			
			if (checkUserBranch.equals("User"))
				iBranch = null;
			else
				iUserId = null;
		}
		
		// end
				
		Timestamp tsFrom = TimeFormat.createTimestamp(dateFrom);
		Timestamp tsTo = TimeFormat.createTimestamp(dateTo);
		tsTo = TimeFormat.add(tsTo, 0, 0, 1);
		if (keyword != null && keyword.length() < 1)
		{
			keyword = null;
		}
		Integer iAuditType = null;
		try
		{
			iAuditType = new Integer(auditType);
		} catch (Exception ex)
		{ /* do nothing */
		}
		Integer iAuditLevel = null;
		try
		{
			iAuditLevel = new Integer(auditLevel);
		} catch (Exception ex)
		{ /* do nothing */
		}
		
		Vector vecAuditTrail = AuditTrailNut.getAuditTrailReport(iUserId, namespace, iAuditType, iAuditLevel, keyword, tsFrom, tsTo, state, status, checkUserBranch, iBranch);
		 
		req.setAttribute("vecAuditTrail", vecAuditTrail);
	}
}
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.sysadmin;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.application.*;

public class DoAuditTrailReport implements Action
{
	private String strClassName = "DoAuditTrailReport";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Get the formName
		String formName = req.getParameter("formName");
		String fwdPage = req.getParameter("fwdPage");
		String userId = req.getParameter("userId");
		req.setAttribute("userId", userId);
		String namespace = req.getParameter("namespace");
		req.setAttribute("namespace", namespace);
		String auditType = req.getParameter("auditType");
		req.setAttribute("auditType", auditType);
		String auditLevel = req.getParameter("auditLevel");
		req.setAttribute("auditLevel", auditLevel);
		String keyword = req.getParameter("keyword");
		req.setAttribute("keyword", keyword);
		String dateFrom = req.getParameter("dateFrom");
		req.setAttribute("dateFrom", dateFrom);
		String dateTo = req.getParameter("dateTo");
		req.setAttribute("dateTo", dateTo);
		String state = req.getParameter("state");
		req.setAttribute("state", state);
		String status = req.getParameter("status");
		req.setAttribute("status", status);
		Vector vecUser = UserNut.getValueObjectsGiven((String) null, (String) null, (String) null, (String) null,
				(String) null, (String) null);
		req.setAttribute("vecUser", vecUser);
		if (fwdPage == null)
		{
			fwdPage = "sa-audit-trail-report-page";
		}
		if (formName == null)
		{
			return new ActionRouter(fwdPage);
		}
		if (formName.equals("getAuditTrailReport"))
		{
			fnGetAuditTrailReport(servlet, req, res);
			return new ActionRouter(fwdPage);
		}
		return new ActionRouter(fwdPage);
	}

	protected void fnGetAuditTrailReport(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String userId = req.getParameter("userId");
		String namespace = req.getParameter("namespace");
		String auditType = req.getParameter("auditType");
		String auditLevel = req.getParameter("auditLevel");
		String keyword = req.getParameter("keyword");
		String dateFrom = req.getParameter("dateFrom");
		String dateTo = req.getParameter("dateTo");
		String state = req.getParameter("state");
		String status = req.getParameter("status");
		Integer iUserId = null;
		try
		{
			iUserId = new Integer(userId);
		} catch (Exception ex)
		{ /* do nothing */
		}
		Timestamp tsFrom = TimeFormat.createTimestamp(dateFrom);
		Timestamp tsTo = TimeFormat.createTimestamp(dateTo);
		tsTo = TimeFormat.add(tsTo, 0, 0, 1);
		if (keyword != null && keyword.length() < 1)
		{
			keyword = null;
		}
		Integer iAuditType = null;
		try
		{
			iAuditType = new Integer(auditType);
		} catch (Exception ex)
		{ /* do nothing */
		}
		Integer iAuditLevel = null;
		try
		{
			iAuditLevel = new Integer(auditLevel);
		} catch (Exception ex)
		{ /* do nothing */
		}
		Vector vecAuditTrail = AuditTrailNut.getValueObjectsGiven(iUserId, namespace, iAuditType, iAuditLevel, keyword,
				tsFrom, tsTo, state, status);
		req.setAttribute("vecAuditTrail", vecAuditTrail);
	}
}
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.sysadmin;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.application.*;

public class DoAuditTrailReport implements Action
{
	private String strClassName = "DoAuditTrailReport";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Get the formName
		String formName = req.getParameter("formName");
		String fwdPage = req.getParameter("fwdPage");
		String userId = req.getParameter("userId");
		req.setAttribute("userId", userId);
		String namespace = req.getParameter("namespace");
		req.setAttribute("namespace", namespace);
		String auditType = req.getParameter("auditType");
		req.setAttribute("auditType", auditType);
		String auditLevel = req.getParameter("auditLevel");
		req.setAttribute("auditLevel", auditLevel);
		String keyword = req.getParameter("keyword");
		req.setAttribute("keyword", keyword);
		String dateFrom = req.getParameter("dateFrom");
		req.setAttribute("dateFrom", dateFrom);
		String dateTo = req.getParameter("dateTo");
		req.setAttribute("dateTo", dateTo);
		String state = req.getParameter("state");
		req.setAttribute("state", state);
		String status = req.getParameter("status");
		req.setAttribute("status", status);
		Vector vecUser = UserNut.getValueObjectsGiven((String) null, (String) null, (String) null, (String) null,
				(String) null, (String) null);
		req.setAttribute("vecUser", vecUser);
		if (fwdPage == null)
		{
			fwdPage = "sa-audit-trail-report-page";
		}
		if (formName == null)
		{
			return new ActionRouter(fwdPage);
		}
		if (formName.equals("getAuditTrailReport"))
		{
			fnGetAuditTrailReport(servlet, req, res);
			return new ActionRouter(fwdPage);
		}
		return new ActionRouter(fwdPage);
	}

	protected void fnGetAuditTrailReport(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String userId = req.getParameter("userId");
		String namespace = req.getParameter("namespace");
		String auditType = req.getParameter("auditType");
		String auditLevel = req.getParameter("auditLevel");
		String keyword = req.getParameter("keyword");
		String dateFrom = req.getParameter("dateFrom");
		String dateTo = req.getParameter("dateTo");
		String state = req.getParameter("state");
		String status = req.getParameter("status");
		Integer iUserId = null;
		try
		{
			iUserId = new Integer(userId);
		} catch (Exception ex)
		{ /* do nothing */
		}
		Timestamp tsFrom = TimeFormat.createTimestamp(dateFrom);
		Timestamp tsTo = TimeFormat.createTimestamp(dateTo);
		tsTo = TimeFormat.add(tsTo, 0, 0, 1);
		if (keyword != null && keyword.length() < 1)
		{
			keyword = null;
		}
		Integer iAuditType = null;
		try
		{
			iAuditType = new Integer(auditType);
		} catch (Exception ex)
		{ /* do nothing */
		}
		Integer iAuditLevel = null;
		try
		{
			iAuditLevel = new Integer(auditLevel);
		} catch (Exception ex)
		{ /* do nothing */
		}
		Vector vecAuditTrail = AuditTrailNut.getValueObjectsGiven(iUserId, namespace, iAuditType, iAuditLevel, keyword,
				tsFrom, tsTo, state, status);
		req.setAttribute("vecAuditTrail", vecAuditTrail);
	}
}
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.sysadmin;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.application.*;

public class DoAuditTrailReport implements Action
{
	private String strClassName = "DoAuditTrailReport";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Get the formName
		String formName = req.getParameter("formName");
		String fwdPage = req.getParameter("fwdPage");
		String userId = req.getParameter("userId");
		req.setAttribute("userId", userId);
		String namespace = req.getParameter("namespace");
		req.setAttribute("namespace", namespace);
		String auditType = req.getParameter("auditType");
		req.setAttribute("auditType", auditType);
		String auditLevel = req.getParameter("auditLevel");
		req.setAttribute("auditLevel", auditLevel);
		String keyword = req.getParameter("keyword");
		req.setAttribute("keyword", keyword);
		String dateFrom = req.getParameter("dateFrom");
		req.setAttribute("dateFrom", dateFrom);
		String dateTo = req.getParameter("dateTo");
		req.setAttribute("dateTo", dateTo);
		String state = req.getParameter("state");
		req.setAttribute("state", state);
		String status = req.getParameter("status");
		req.setAttribute("status", status);
		Vector vecUser = UserNut.getValueObjectsGiven((String) null, (String) null, (String) null, (String) null,
				(String) null, (String) null);
		req.setAttribute("vecUser", vecUser);
		if (fwdPage == null)
		{
			fwdPage = "sa-audit-trail-report-page";
		}
		if (formName == null)
		{
			return new ActionRouter(fwdPage);
		}
		if (formName.equals("getAuditTrailReport"))
		{
			fnGetAuditTrailReport(servlet, req, res);
			return new ActionRouter(fwdPage);
		}
		return new ActionRouter(fwdPage);
	}

	protected void fnGetAuditTrailReport(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String userId = req.getParameter("userId");
		String namespace = req.getParameter("namespace");
		String auditType = req.getParameter("auditType");
		String auditLevel = req.getParameter("auditLevel");
		String keyword = req.getParameter("keyword");
		String dateFrom = req.getParameter("dateFrom");
		String dateTo = req.getParameter("dateTo");
		String state = req.getParameter("state");
		String status = req.getParameter("status");
		Integer iUserId = null;
		try
		{
			iUserId = new Integer(userId);
		} catch (Exception ex)
		{ /* do nothing */
		}
		Timestamp tsFrom = TimeFormat.createTimestamp(dateFrom);
		Timestamp tsTo = TimeFormat.createTimestamp(dateTo);
		tsTo = TimeFormat.add(tsTo, 0, 0, 1);
		if (keyword != null && keyword.length() < 1)
		{
			keyword = null;
		}
		Integer iAuditType = null;
		try
		{
			iAuditType = new Integer(auditType);
		} catch (Exception ex)
		{ /* do nothing */
		}
		Integer iAuditLevel = null;
		try
		{
			iAuditLevel = new Integer(auditLevel);
		} catch (Exception ex)
		{ /* do nothing */
		}
		Vector vecAuditTrail = AuditTrailNut.getValueObjectsGiven(iUserId, namespace, iAuditType, iAuditLevel, keyword,
				tsFrom, tsTo, state, status);
		req.setAttribute("vecAuditTrail", vecAuditTrail);
	}
}
