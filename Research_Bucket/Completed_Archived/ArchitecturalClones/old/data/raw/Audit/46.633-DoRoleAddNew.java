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
import java.util.*;
import java.lang.*;
import com.vlee.ejb.user.*;
import com.vlee.util.*;

public class DoRoleAddNew extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		Collection colRoles = null;
		RoleHome lRoleHome = getRoleHome();
		try
		{
			colRoles = (Collection) lRoleHome.findAllRoleNames();
		} catch (Exception ex)
		{
			Log.printDebug("DoRoleAddRem:" + ex.getMessage());
		}
		Iterator itr = colRoles.iterator();
		req.setAttribute("RolesIterator", itr);
		// The section above is essentially same as DoRoleAddRem
		// Validation at servlet level
		String rname = req.getParameter("roleName");
		String title = req.getParameter("title");
		String description = req.getParameter("description");
		// When the users click the button for fun!!
		if (rname == null || title == null || description == null)
		{
			return new ActionRouter("sa-new-redirect-addrem-role-page");
		}
		rname = filterString(rname, "rolename");
		title = filterString(title, "names");
		// lname = filterString(lname, "names");
		// ///////////////////////////////////////////////////////////////
		// todo: Check if role has the right do to so
		boolean bCanAdd = true;
		if (!bCanAdd)
		{
			String errorMsg = "<p> Sorry, you do not have the" + " right to create new role... </p>";
			req.setAttribute("errorMessage", errorMsg);
			return new ActionRouter("error-message-page");
		}
		// ///////////////////////////////////////////////////////////////
		// Check rolename
		// 1. If it is unique
		// 2. If it is currently in the database
		String errMsg = checkRolename(rname, itr);
		if (errMsg != null)
		{
			Log.printDebug("DoRoleAddNew:errMsg" + errMsg);
			req.setAttribute("errorMessage", errMsg);
			return new ActionRouter("error-message-page");
		}
		// ///////////////////////////////////////////////////////////////
		// /////////////////////////////
		// if everything is OK.. add role then...
		try
		{
			Log.printDebug("DoRoleAddNew: b4 Create");
			Role role = (Role) lRoleHome.create(rname, title, description);
			fnAuditTrail(servlet, req, res, role.getRoleId());
		} catch (Exception ex)
		{
			Log.printDebug("DoRoleAdd:ejbCreate" + ex.getMessage());
		}
		// res.encodeRedirectURL("sa-addrem-role.do");
		// res.encodeURL("sa-addrem-role.do");
		// res.sendRedirect("sa-addrem-role.do");
		// return new ActionRouter(res.encodeURL("sa-addremove-role"));
		return new ActionRouter("sa-new-redirect-addrem-role-page");
	}

	// ////////////////////////////////////////////////////////
	protected String filterString(String strbuf, String strtype)
	{
		strbuf = strbuf.trim();
		int ca = 1;
		if (strtype.equals("rolename"))
		{
			strbuf = strbuf.toLowerCase();
			while (ca < strbuf.length())
			{
				char cha = strbuf.charAt(ca);
				if (Character.isLetter(cha) || cha == '.' || cha== '-')
				{
					ca = ca + 1;
				} else
				{
					strbuf = strbuf.substring(0, ca) + strbuf.substring(ca + 1);
				}
			}
		}
		if (strtype.equals("names"))
		{
			while (ca < strbuf.length())
			{
				char cha = strbuf.charAt(ca);
				if (Character.isSpaceChar(cha) || Character.isLetter(cha) || cha == '.' || cha== '-')
				{
					ca = ca + 1;
				} else
				{
					strbuf = strbuf.substring(0, ca) + strbuf.substring(ca + 1);
				}
			}
		}
		return new String(strbuf);
	}

	protected String checkRolename(String uname, Iterator itr)
	{
		Log.printVerbose(" Check Rolenames : outside iter ");
		try
		{
			while (itr.hasNext())
			{
				Log.printVerbose(" Check Rolenames ");
				Role bufRole = (Role) itr.next();
				if (uname.equals((String) bufRole.getRoleName()))
				{
					return new String("<p> The rolename that you want exists in "
							+ " our database. Please try a different rolename..");
				}
			}
		} catch (Exception ex)
		{
			Log.printVerbose("Exception in loading all roles" + ex.getMessage());
		}
		if (uname.length() < 3 || uname.length() > 20)
		{
			return new String("<p> The rolename that you want "
					+ "must have at least 3 or not more than 20 characters."
					+ " <br> Please try a different rolename..");
		}
		return null;
	}

	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res, Integer pkid)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		if (iUserId != null)
		{
			String rname = req.getParameter("roleName");
			String title = req.getParameter("title");
			String description = req.getParameter("description");
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_SYSADMIN;
			atObj.remarks = "sa: role-add " + "name=" + rname + ", title=" + title + ", desc=" + description;
			atObj.tc_entity_table = RoleBean.TABLENAME;
			atObj.tc_entity_id = pkid;
			atObj.tc_action = AuditTrailBean.TC_ACTION_DELETE;
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
import java.util.*;
import java.lang.*;
import com.vlee.ejb.user.*;
import com.vlee.util.*;

public class DoRoleAddNew extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		Collection colRoles = null;
		RoleHome lRoleHome = getRoleHome();
		try
		{
			colRoles = (Collection) lRoleHome.findAllRoleNames();
		} catch (Exception ex)
		{
			Log.printDebug("DoRoleAddRem:" + ex.getMessage());
		}
		Iterator itr = colRoles.iterator();
		req.setAttribute("RolesIterator", itr);
		// The section above is essentially same as DoRoleAddRem
		// Validation at servlet level
		String rname = req.getParameter("roleName");
		String title = req.getParameter("title");
		String description = req.getParameter("description");
		// When the users click the button for fun!!
		if (rname == null || title == null || description == null)
		{
			return new ActionRouter("sa-new-redirect-addrem-role-page");
		}
		rname = filterString(rname, "rolename");
		title = filterString(title, "names");
		// lname = filterString(lname, "names");
		// ///////////////////////////////////////////////////////////////
		// todo: Check if role has the right do to so
		boolean bCanAdd = true;
		if (!bCanAdd)
		{
			String errorMsg = "<p> Sorry, you do not have the" + " right to create new role... </p>";
			req.setAttribute("errorMessage", errorMsg);
			return new ActionRouter("error-message-page");
		}
		// ///////////////////////////////////////////////////////////////
		// Check rolename
		// 1. If it is unique
		// 2. If it is currently in the database
		String errMsg = checkRolename(rname, itr);
		if (errMsg != null)
		{
			Log.printDebug("DoRoleAddNew:errMsg" + errMsg);
			req.setAttribute("errorMessage", errMsg);
			return new ActionRouter("error-message-page");
		}
		// ///////////////////////////////////////////////////////////////
		// /////////////////////////////
		// if everything is OK.. add role then...
		try
		{
			Log.printDebug("DoRoleAddNew: b4 Create");
			Role role = (Role) lRoleHome.create(rname, title, description);
			fnAuditTrail(servlet, req, res);
		} catch (Exception ex)
		{
			Log.printDebug("DoRoleAdd:ejbCreate" + ex.getMessage());
		}
		// res.encodeRedirectURL("sa-addrem-role.do");
		// res.encodeURL("sa-addrem-role.do");
		// res.sendRedirect("sa-addrem-role.do");
		// return new ActionRouter(res.encodeURL("sa-addremove-role"));
		return new ActionRouter("sa-new-redirect-addrem-role-page");
	}

	// ////////////////////////////////////////////////////////
	protected String filterString(String strbuf, String strtype)
	{
		strbuf = strbuf.trim();
		int ca = 1;
		if (strtype.equals("rolename"))
		{
			strbuf = strbuf.toLowerCase();
			while (ca < strbuf.length())
			{
				char cha = strbuf.charAt(ca);
				if (Character.isLetter(cha))
				{
					ca = ca + 1;
				} else
				{
					strbuf = strbuf.substring(0, ca) + strbuf.substring(ca + 1);
				}
			}
		}
		if (strtype.equals("names"))
		{
			while (ca < strbuf.length())
			{
				char cha = strbuf.charAt(ca);
				if (Character.isSpaceChar(cha) || Character.isLetter(cha))
				{
					ca = ca + 1;
				} else
				{
					strbuf = strbuf.substring(0, ca) + strbuf.substring(ca + 1);
				}
			}
		}
		return new String(strbuf);
	}

	protected String checkRolename(String uname, Iterator itr)
	{
		Log.printVerbose(" Check Rolenames : outside iter ");
		try
		{
			while (itr.hasNext())
			{
				Log.printVerbose(" Check Rolenames ");
				Role bufRole = (Role) itr.next();
				if (uname.equals((String) bufRole.getRoleName()))
				{
					return new String("<p> The rolename that you want exists in "
							+ " our database. Please try a different rolename..");
				}
			}
		} catch (Exception ex)
		{
			Log.printVerbose("Exception in loading all roles" + ex.getMessage());
		}
		if (uname.length() < 3 || uname.length() > 20)
		{
			return new String("<p> The rolename that you want "
					+ "must have at least 3 or not more than 20 characters."
					+ " <br> Please try a different rolename..");
		}
		return null;
	}

	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		if (iUserId != null)
		{
			String rname = req.getParameter("roleName");
			String title = req.getParameter("title");
			String description = req.getParameter("description");
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_SYSADMIN;
			atObj.remarks = "sa: role-add " + "name=" + rname + ", title=" + title + ", desc=" + description;
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
import java.util.*;
import java.lang.*;
import com.vlee.ejb.user.*;
import com.vlee.util.*;

public class DoRoleAddNew extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		Collection colRoles = null;
		RoleHome lRoleHome = getRoleHome();
		try
		{
			colRoles = (Collection) lRoleHome.findAllRoleNames();
		} catch (Exception ex)
		{
			Log.printDebug("DoRoleAddRem:" + ex.getMessage());
		}
		Iterator itr = colRoles.iterator();
		req.setAttribute("RolesIterator", itr);
		// The section above is essentially same as DoRoleAddRem
		// Validation at servlet level
		String rname = req.getParameter("roleName");
		String title = req.getParameter("title");
		String description = req.getParameter("description");
		// When the users click the button for fun!!
		if (rname == null || title == null || description == null)
		{
			return new ActionRouter("sa-new-redirect-addrem-role-page");
		}
		rname = filterString(rname, "rolename");
		title = filterString(title, "names");
		// lname = filterString(lname, "names");
		// ///////////////////////////////////////////////////////////////
		// todo: Check if role has the right do to so
		boolean bCanAdd = true;
		if (!bCanAdd)
		{
			String errorMsg = "<p> Sorry, you do not have the" + " right to create new role... </p>";
			req.setAttribute("errorMessage", errorMsg);
			return new ActionRouter("error-message-page");
		}
		// ///////////////////////////////////////////////////////////////
		// Check rolename
		// 1. If it is unique
		// 2. If it is currently in the database
		String errMsg = checkRolename(rname, itr);
		if (errMsg != null)
		{
			Log.printDebug("DoRoleAddNew:errMsg" + errMsg);
			req.setAttribute("errorMessage", errMsg);
			return new ActionRouter("error-message-page");
		}
		// ///////////////////////////////////////////////////////////////
		// /////////////////////////////
		// if everything is OK.. add role then...
		try
		{
			Log.printDebug("DoRoleAddNew: b4 Create");
			Role role = (Role) lRoleHome.create(rname, title, description);
			fnAuditTrail(servlet, req, res);
		} catch (Exception ex)
		{
			Log.printDebug("DoRoleAdd:ejbCreate" + ex.getMessage());
		}
		// res.encodeRedirectURL("sa-addrem-role.do");
		// res.encodeURL("sa-addrem-role.do");
		// res.sendRedirect("sa-addrem-role.do");
		// return new ActionRouter(res.encodeURL("sa-addremove-role"));
		return new ActionRouter("sa-new-redirect-addrem-role-page");
	}

	// ////////////////////////////////////////////////////////
	protected String filterString(String strbuf, String strtype)
	{
		strbuf = strbuf.trim();
		int ca = 1;
		if (strtype.equals("rolename"))
		{
			strbuf = strbuf.toLowerCase();
			while (ca < strbuf.length())
			{
				char cha = strbuf.charAt(ca);
				if (Character.isLetter(cha))
				{
					ca = ca + 1;
				} else
				{
					strbuf = strbuf.substring(0, ca) + strbuf.substring(ca + 1);
				}
			}
		}
		if (strtype.equals("names"))
		{
			while (ca < strbuf.length())
			{
				char cha = strbuf.charAt(ca);
				if (Character.isSpaceChar(cha) || Character.isLetter(cha))
				{
					ca = ca + 1;
				} else
				{
					strbuf = strbuf.substring(0, ca) + strbuf.substring(ca + 1);
				}
			}
		}
		return new String(strbuf);
	}

	protected String checkRolename(String uname, Iterator itr)
	{
		Log.printVerbose(" Check Rolenames : outside iter ");
		try
		{
			while (itr.hasNext())
			{
				Log.printVerbose(" Check Rolenames ");
				Role bufRole = (Role) itr.next();
				if (uname.equals((String) bufRole.getRoleName()))
				{
					return new String("<p> The rolename that you want exists in "
							+ " our database. Please try a different rolename..");
				}
			}
		} catch (Exception ex)
		{
			Log.printVerbose("Exception in loading all roles" + ex.getMessage());
		}
		if (uname.length() < 3 || uname.length() > 20)
		{
			return new String("<p> The rolename that you want "
					+ "must have at least 3 or not more than 20 characters."
					+ " <br> Please try a different rolename..");
		}
		return null;
	}

	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		if (iUserId != null)
		{
			String rname = req.getParameter("roleName");
			String title = req.getParameter("title");
			String description = req.getParameter("description");
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_SYSADMIN;
			atObj.remarks = "sa: role-add " + "name=" + rname + ", title=" + title + ", desc=" + description;
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
import java.util.*;
import java.lang.*;
import com.vlee.ejb.user.*;
import com.vlee.util.*;

public class DoRoleAddNew extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		Collection colRoles = null;
		RoleHome lRoleHome = getRoleHome();
		try
		{
			colRoles = (Collection) lRoleHome.findAllRoleNames();
		} catch (Exception ex)
		{
			Log.printDebug("DoRoleAddRem:" + ex.getMessage());
		}
		Iterator itr = colRoles.iterator();
		req.setAttribute("RolesIterator", itr);
		// The section above is essentially same as DoRoleAddRem
		// Validation at servlet level
		String rname = req.getParameter("roleName");
		String title = req.getParameter("title");
		String description = req.getParameter("description");
		// When the users click the button for fun!!
		if (rname == null || title == null || description == null)
		{
			return new ActionRouter("sa-new-redirect-addrem-role-page");
		}
		rname = filterString(rname, "rolename");
		title = filterString(title, "names");
		// lname = filterString(lname, "names");
		// ///////////////////////////////////////////////////////////////
		// todo: Check if role has the right do to so
		boolean bCanAdd = true;
		if (!bCanAdd)
		{
			String errorMsg = "<p> Sorry, you do not have the" + " right to create new role... </p>";
			req.setAttribute("errorMessage", errorMsg);
			return new ActionRouter("error-message-page");
		}
		// ///////////////////////////////////////////////////////////////
		// Check rolename
		// 1. If it is unique
		// 2. If it is currently in the database
		String errMsg = checkRolename(rname, itr);
		if (errMsg != null)
		{
			Log.printDebug("DoRoleAddNew:errMsg" + errMsg);
			req.setAttribute("errorMessage", errMsg);
			return new ActionRouter("error-message-page");
		}
		// ///////////////////////////////////////////////////////////////
		// /////////////////////////////
		// if everything is OK.. add role then...
		try
		{
			Log.printDebug("DoRoleAddNew: b4 Create");
			Role role = (Role) lRoleHome.create(rname, title, description);
			fnAuditTrail(servlet, req, res);
		} catch (Exception ex)
		{
			Log.printDebug("DoRoleAdd:ejbCreate" + ex.getMessage());
		}
		// res.encodeRedirectURL("sa-addrem-role.do");
		// res.encodeURL("sa-addrem-role.do");
		// res.sendRedirect("sa-addrem-role.do");
		// return new ActionRouter(res.encodeURL("sa-addremove-role"));
		return new ActionRouter("sa-new-redirect-addrem-role-page");
	}

	// ////////////////////////////////////////////////////////
	protected String filterString(String strbuf, String strtype)
	{
		strbuf = strbuf.trim();
		int ca = 1;
		if (strtype.equals("rolename"))
		{
			strbuf = strbuf.toLowerCase();
			while (ca < strbuf.length())
			{
				char cha = strbuf.charAt(ca);
				if (Character.isLetter(cha))
				{
					ca = ca + 1;
				} else
				{
					strbuf = strbuf.substring(0, ca) + strbuf.substring(ca + 1);
				}
			}
		}
		if (strtype.equals("names"))
		{
			while (ca < strbuf.length())
			{
				char cha = strbuf.charAt(ca);
				if (Character.isSpaceChar(cha) || Character.isLetter(cha))
				{
					ca = ca + 1;
				} else
				{
					strbuf = strbuf.substring(0, ca) + strbuf.substring(ca + 1);
				}
			}
		}
		return new String(strbuf);
	}

	protected String checkRolename(String uname, Iterator itr)
	{
		Log.printVerbose(" Check Rolenames : outside iter ");
		try
		{
			while (itr.hasNext())
			{
				Log.printVerbose(" Check Rolenames ");
				Role bufRole = (Role) itr.next();
				if (uname.equals((String) bufRole.getRoleName()))
				{
					return new String("<p> The rolename that you want exists in "
							+ " our database. Please try a different rolename..");
				}
			}
		} catch (Exception ex)
		{
			Log.printVerbose("Exception in loading all roles" + ex.getMessage());
		}
		if (uname.length() < 3 || uname.length() > 20)
		{
			return new String("<p> The rolename that you want "
					+ "must have at least 3 or not more than 20 characters."
					+ " <br> Please try a different rolename..");
		}
		return null;
	}

	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		if (iUserId != null)
		{
			String rname = req.getParameter("roleName");
			String title = req.getParameter("title");
			String description = req.getParameter("description");
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_SYSADMIN;
			atObj.remarks = "sa: role-add " + "name=" + rname + ", title=" + title + ", desc=" + description;
			AuditTrailNut.fnCreate(atObj);
		}
	}
}
