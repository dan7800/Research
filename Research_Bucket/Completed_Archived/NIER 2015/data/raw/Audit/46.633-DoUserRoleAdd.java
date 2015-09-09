package com.vlee.servlet.sysadmin;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.lang.*;
import com.vlee.ejb.user.*;
import com.vlee.util.*;

public class DoUserRoleAdd extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		UserRole bufUR = null;
		String editUserName = req.getParameter("editUserName");
		String newRoleName = req.getParameter("newRoleName");
		String strDate = req.getParameter("expiryDate");
		if (editUserName == null || newRoleName == null)
		{
			return new ActionRouter("sa-redirect-addrem-userrole-page");
		}
		User user = getUserHandle(editUserName);
		Role role = getRoleHandle(newRoleName);
		Calendar cal = Calendar.getInstance();
		Collection colUserRoles = null;
		UserRoleHome lUserRoleHome = getUserRoleHome();
		try
		{
			colUserRoles = (Collection) lUserRoleHome.findUserRolesGiven(new String("userid"), user.getUserId()
					.toString());
		} catch (Exception ex)
		{
			Log.printDebug("DoUserRoleAddRem:" + ex.getMessage());
		}
		Iterator itr = colUserRoles.iterator();
		if (itr.hasNext())
		{
			bufUR = (UserRole) itr.next();
			bufUR.setRoleId(role.getRoleId());
			bufUR.setTheDate(cal);
		} else
		{
			try
			{
				UserRole userrole = (UserRole) lUserRoleHome.create(role.getRoleId(), user.getUserId(), cal);
				fnAuditTrail(servlet, req, res);
			} catch (Exception ex)
			{
				Log.printDebug("DoUserRoleAdd:ejbCreate" + ex.getMessage());
			}
		}
		return new ActionRouter("sa-redirect-addrem-userrole-page");
	}

	// ////////////////////////////////////////////////////////
	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		if (iUserId != null)
		{
			String editUserName = req.getParameter("editUserName");
			String newRoleName = req.getParameter("newRoleName");
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_SYSADMIN;
			atObj.remarks = "sa: user-role-set " + editUserName + " to " + newRoleName;
			AuditTrailNut.fnCreate(atObj);
		}
	}
}
package com.vlee.servlet.sysadmin;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.lang.*;
import com.vlee.ejb.user.*;
import com.vlee.util.*;

public class DoUserRoleAdd extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		UserRole bufUR = null;
		String editUserName = req.getParameter("editUserName");
		String newRoleName = req.getParameter("newRoleName");
		String strDate = req.getParameter("expiryDate");
		if (editUserName == null || newRoleName == null)
		{
			return new ActionRouter("sa-redirect-addrem-userrole-page");
		}
		User user = getUserHandle(editUserName);
		Role role = getRoleHandle(newRoleName);
		Calendar cal = Calendar.getInstance();
		Collection colUserRoles = null;
		UserRoleHome lUserRoleHome = getUserRoleHome();
		try
		{
			colUserRoles = (Collection) lUserRoleHome.findUserRolesGiven(new String("userid"), user.getUserId()
					.toString());
		} catch (Exception ex)
		{
			Log.printDebug("DoUserRoleAddRem:" + ex.getMessage());
		}
		Iterator itr = colUserRoles.iterator();
		if (itr.hasNext())
		{
			bufUR = (UserRole) itr.next();
			bufUR.setRoleId(role.getRoleId());
			bufUR.setTheDate(cal);
		} else
		{
			try
			{
				UserRole userrole = (UserRole) lUserRoleHome.create(role.getRoleId(), user.getUserId(), cal);
				fnAuditTrail(servlet, req, res);
			} catch (Exception ex)
			{
				Log.printDebug("DoUserRoleAdd:ejbCreate" + ex.getMessage());
			}
		}
		return new ActionRouter("sa-redirect-addrem-userrole-page");
	}

	// ////////////////////////////////////////////////////////
	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		if (iUserId != null)
		{
			String editUserName = req.getParameter("editUserName");
			String newRoleName = req.getParameter("newRoleName");
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_SYSADMIN;
			atObj.remarks = "sa: user-role-set " + editUserName + " to " + newRoleName;
			AuditTrailNut.fnCreate(atObj);
		}
	}
}
package com.vlee.servlet.sysadmin;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.lang.*;
import com.vlee.ejb.user.*;
import com.vlee.util.*;

public class DoUserRoleAdd extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		UserRole bufUR = null;
		String editUserName = req.getParameter("editUserName");
		String newRoleName = req.getParameter("newRoleName");
		String strDate = req.getParameter("expiryDate");
		if (editUserName == null || newRoleName == null)
		{
			return new ActionRouter("sa-redirect-addrem-userrole-page");
		}
		User user = getUserHandle(editUserName);
		Role role = getRoleHandle(newRoleName);
		Calendar cal = Calendar.getInstance();
		Collection colUserRoles = null;
		UserRoleHome lUserRoleHome = getUserRoleHome();
		try
		{
			colUserRoles = (Collection) lUserRoleHome.findUserRolesGiven(new String("userid"), user.getUserId()
					.toString());
		} catch (Exception ex)
		{
			Log.printDebug("DoUserRoleAddRem:" + ex.getMessage());
		}
		Iterator itr = colUserRoles.iterator();
		if (itr.hasNext())
		{
			bufUR = (UserRole) itr.next();
			bufUR.setRoleId(role.getRoleId());
			bufUR.setTheDate(cal);
		} else
		{
			try
			{
				UserRole userrole = (UserRole) lUserRoleHome.create(role.getRoleId(), user.getUserId(), cal);
				fnAuditTrail(servlet, req, res);
			} catch (Exception ex)
			{
				Log.printDebug("DoUserRoleAdd:ejbCreate" + ex.getMessage());
			}
		}
		return new ActionRouter("sa-redirect-addrem-userrole-page");
	}

	// ////////////////////////////////////////////////////////
	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		if (iUserId != null)
		{
			String editUserName = req.getParameter("editUserName");
			String newRoleName = req.getParameter("newRoleName");
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_SYSADMIN;
			atObj.remarks = "sa: user-role-set " + editUserName + " to " + newRoleName;
			AuditTrailNut.fnCreate(atObj);
		}
	}
}
package com.vlee.servlet.sysadmin;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.lang.*;
import com.vlee.ejb.user.*;
import com.vlee.util.*;

public class DoUserRoleAdd extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		UserRole bufUR = null;
		String editUserName = req.getParameter("editUserName");
		String newRoleName = req.getParameter("newRoleName");
		String strDate = req.getParameter("expiryDate");
		if (editUserName == null || newRoleName == null)
		{
			return new ActionRouter("sa-redirect-addrem-userrole-page");
		}
		User user = getUserHandle(editUserName);
		Role role = getRoleHandle(newRoleName);
		Calendar cal = Calendar.getInstance();
		Collection colUserRoles = null;
		UserRoleHome lUserRoleHome = getUserRoleHome();
		try
		{
			colUserRoles = (Collection) lUserRoleHome.findUserRolesGiven(new String("userid"), user.getUserId()
					.toString());
		} catch (Exception ex)
		{
			Log.printDebug("DoUserRoleAddRem:" + ex.getMessage());
		}
		Iterator itr = colUserRoles.iterator();
		if (itr.hasNext())
		{
			bufUR = (UserRole) itr.next();
			bufUR.setRoleId(role.getRoleId());
			bufUR.setTheDate(cal);
		} else
		{
			try
			{
				UserRole userrole = (UserRole) lUserRoleHome.create(role.getRoleId(), user.getUserId(), cal);
				fnAuditTrail(servlet, req, res);
			} catch (Exception ex)
			{
				Log.printDebug("DoUserRoleAdd:ejbCreate" + ex.getMessage());
			}
		}
		return new ActionRouter("sa-redirect-addrem-userrole-page");
	}

	// ////////////////////////////////////////////////////////
	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		if (iUserId != null)
		{
			String editUserName = req.getParameter("editUserName");
			String newRoleName = req.getParameter("newRoleName");
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_SYSADMIN;
			atObj.remarks = "sa: user-role-set " + editUserName + " to " + newRoleName;
			AuditTrailNut.fnCreate(atObj);
		}
	}
}
