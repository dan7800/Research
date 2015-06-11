/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.info)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.sysadmin;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import com.vlee.ejb.user.*;
import com.vlee.util.*;

public class DoUserResetPassword extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String uname = req.getParameter("userName");
		String pwd = req.getParameter("password");
		User user = null;
		user = (User) getUserHandle(uname);
		if (user != null)
		{
			// reset the password here....
			user.setPassword(pwd);
			fnAuditTrail(servlet, req, res);
			return new ActionRouter("sa-redirect-addrem-user-page");
		} else
		{
			String errorMsg = "<p> Sorry, username does not exist!!</p>";
			req.setAttribute("errorMessage", errorMsg);
			return new ActionRouter("error-message-page");
		}
	}

	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		if (iUserId != null)
		{
			String uname = req.getParameter("userName");
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_SYSADMIN;
			atObj.remarks = "sa: user-passwd-reset " + uname;
			AuditTrailNut.fnCreate(atObj);
		}
	}
}
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.info)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.sysadmin;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import com.vlee.ejb.user.*;
import com.vlee.util.*;

public class DoUserResetPassword extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String uname = req.getParameter("userName");
		String pwd = req.getParameter("password");
		User user = null;
		user = (User) getUserHandle(uname);
		if (user != null)
		{
			// reset the password here....
			user.setPassword(pwd);
			fnAuditTrail(servlet, req, res);
			return new ActionRouter("sa-redirect-addrem-user-page");
		} else
		{
			String errorMsg = "<p> Sorry, username does not exist!!</p>";
			req.setAttribute("errorMessage", errorMsg);
			return new ActionRouter("error-message-page");
		}
	}

	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		if (iUserId != null)
		{
			String uname = req.getParameter("userName");
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_SYSADMIN;
			atObj.remarks = "sa: user-passwd-reset " + uname;
			AuditTrailNut.fnCreate(atObj);
		}
	}
}
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.info)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.sysadmin;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import com.vlee.ejb.user.*;
import com.vlee.util.*;

public class DoUserResetPassword extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String uname = req.getParameter("userName");
		String pwd = req.getParameter("password");
		User user = null;
		user = (User) getUserHandle(uname);
		if (user != null)
		{
			// reset the password here....
			user.setPassword(pwd);
			fnAuditTrail(servlet, req, res);
			return new ActionRouter("sa-redirect-addrem-user-page");
		} else
		{
			String errorMsg = "<p> Sorry, username does not exist!!</p>";
			req.setAttribute("errorMessage", errorMsg);
			return new ActionRouter("error-message-page");
		}
	}

	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		if (iUserId != null)
		{
			String uname = req.getParameter("userName");
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_SYSADMIN;
			atObj.remarks = "sa: user-passwd-reset " + uname;
			AuditTrailNut.fnCreate(atObj);
		}
	}
}
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.info)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.sysadmin;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import com.vlee.ejb.user.*;
import com.vlee.util.*;

public class DoUserResetPassword extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String uname = req.getParameter("userName");
		String pwd = req.getParameter("password");
		User user = null;
		user = (User) getUserHandle(uname);
		if (user != null)
		{
			// reset the password here....
			user.setPassword(pwd);
			fnAuditTrail(servlet, req, res);
			return new ActionRouter("sa-redirect-addrem-user-page");
		} else
		{
			String errorMsg = "<p> Sorry, username does not exist!!</p>";
			req.setAttribute("errorMessage", errorMsg);
			return new ActionRouter("error-message-page");
		}
	}

	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		if (iUserId != null)
		{
			String uname = req.getParameter("userName");
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_SYSADMIN;
			atObj.remarks = "sa: user-passwd-reset " + uname;
			AuditTrailNut.fnCreate(atObj);
		}
	}
}
