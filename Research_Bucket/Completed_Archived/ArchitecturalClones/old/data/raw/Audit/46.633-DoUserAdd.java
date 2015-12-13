package com.vlee.servlet.sysadmin;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.lang.*;
import com.vlee.ejb.user.*;
import com.vlee.util.*;

public class DoUserAdd extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		fnAuditTrail(servlet, req, res);
		Collection colUsers = null;
		UserHome lUserHome = getUserHome();
		try
		{
			colUsers = (Collection) lUserHome.findAllUsers();
		} catch (Exception ex)
		{
			Log.printDebug("DoUserAddRem:" + ex.getMessage());
		}
		Iterator itr = colUsers.iterator();
		req.setAttribute("UsersIterator", itr);
		// The section above is essentially same as DoUserAddRem
		// Validation at servlet level
		String uname = req.getParameter("userName");
		String pwd = req.getParameter("password");
		String fname = req.getParameter("nameFirst");
		String lname = req.getParameter("nameLast");
		// ensure that the users don't click add user for fun
		if (uname == null || pwd == null || fname == null || lname == null)
		{
			return new ActionRouter("sa-redirect-addrem-user-page");
		}
		uname = filterString(uname, "username");
		fname = filterString(fname, "names");
		lname = filterString(lname, "names");
		// ///////////////////////////////////////////////////////////////
		// todo: Check if user has the right do to so
		boolean bCanAdd = true;
		if (!bCanAdd)
		{
			String errorMsg = "<p> Sorry, you do not have the" + " right to create new user... </p>";
			req.setAttribute("errorMessage", errorMsg);
			return new ActionRouter("error-message-page");
		}
		// ///////////////////////////////////////////////////////////////
		// Check username
		// 1. If it is unique
		// 2. If it is currently in the database
		String errMsg = checkUsername(uname, itr);
		if (errMsg != null)
		{
			req.setAttribute("errorMessage", errMsg);
			return new ActionRouter("error-message-page");
		}
		// ///////////////////////////////////////////////////////////////
		// Check username
		// 1. If it is unique
		// 2. If it is currently in the database
		errMsg = checkFirstLastNamePassword(fname, lname, pwd);
		if (errMsg != null)
		{
			req.setAttribute("errorMessage", errMsg);
			return new ActionRouter("error-message-page");
		}
		// /////////////////////////////
		// if everything is OK.. add user then...
		try
		{
			User usr = (User) lUserHome.create(uname, pwd, fname, lname);
		} catch (Exception ex)
		{
			Log.printDebug("DoUserAdd:ejbCreate" + ex.getMessage());
		}
		// res.encodeRedirectURL("sa-addrem-user.do");
		// res.encodeURL("sa-addrem-user.do");
		// res.sendRedirect("sa-addrem-user.do");
		// return new ActionRouter(res.encodeURL("sa-addremove-user"));
		return new ActionRouter("sa-redirect-addrem-user-page");
	}

	// ////////////////////////////////////////////////////////
	protected String filterString(String strbuf, String strtype)
	{
		strbuf = strbuf.trim();
		int ca = 1;
		if (strtype.equals("username"))
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

	protected String checkFirstLastNamePassword(String fname, String lname, String pwd)
	{
		if (fname.length() < 1 || fname.length() >= 50)
		{
			return new String("<p> The first name " + "must have at least 1 or not more than 50 characters."
					+ " <br> Please try again.....");
		}
		if (lname.length() < 1 || lname.length() >= 50)
		{
			return new String("<p> The last name " + "must have at least 1 or not more than 50 characters."
					+ " <br> Please try again.....");
		}
		if (pwd.length() < 4 || fname.length() >= 50)
		{
			return new String("<p> The password " + "must have at least 4 or not more than 50 characters."
					+ " <br> Please try again.....");
		}
		return null;
	}

	protected String checkUsername(String uname, Iterator itr)
	{
		Log.printVerbose(" Check Usernames : outside iter ");
		try
		{
			while (itr.hasNext())
			{
				Log.printVerbose(" Check Usernames ");
				User bufUser = (User) itr.next();
				if (uname.equals((String) bufUser.getUserName()))
				{
					return new String("<p> The username that you want exists in "
							+ " our database. Please try a different username..");
				}
			}
		} catch (Exception ex)
		{
			Log.printVerbose("Exception is loading all users" + ex.getMessage());
		}
		if (uname.length() < 3 || uname.length() > 20)
		{
			return new String("<p> The username that you want "
					+ "must have at least 3 or not more than 20 characters."
					+ " <br> Please try a different username..");
		}
		return null;
	}

	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		if (iUserId != null)
		{
			String uname = req.getParameter("userName");
			String fname = req.getParameter("nameFirst");
			String lname = req.getParameter("nameLast");
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_SYSADMIN;
			atObj.remarks = "sa: user-add " + uname + " (" + fname + " " + lname + ")";
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

public class DoUserAdd extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		fnAuditTrail(servlet, req, res);
		Collection colUsers = null;
		UserHome lUserHome = getUserHome();
		try
		{
			colUsers = (Collection) lUserHome.findAllUsers();
		} catch (Exception ex)
		{
			Log.printDebug("DoUserAddRem:" + ex.getMessage());
		}
		Iterator itr = colUsers.iterator();
		req.setAttribute("UsersIterator", itr);
		// The section above is essentially same as DoUserAddRem
		// Validation at servlet level
		String uname = req.getParameter("userName");
		String pwd = req.getParameter("password");
		String fname = req.getParameter("nameFirst");
		String lname = req.getParameter("nameLast");
		// ensure that the users don't click add user for fun
		if (uname == null || pwd == null || fname == null || lname == null)
		{
			return new ActionRouter("sa-redirect-addrem-user-page");
		}
		uname = filterString(uname, "username");
		fname = filterString(fname, "names");
		lname = filterString(lname, "names");
		// ///////////////////////////////////////////////////////////////
		// todo: Check if user has the right do to so
		boolean bCanAdd = true;
		if (!bCanAdd)
		{
			String errorMsg = "<p> Sorry, you do not have the" + " right to create new user... </p>";
			req.setAttribute("errorMessage", errorMsg);
			return new ActionRouter("error-message-page");
		}
		// ///////////////////////////////////////////////////////////////
		// Check username
		// 1. If it is unique
		// 2. If it is currently in the database
		String errMsg = checkUsername(uname, itr);
		if (errMsg != null)
		{
			req.setAttribute("errorMessage", errMsg);
			return new ActionRouter("error-message-page");
		}
		// ///////////////////////////////////////////////////////////////
		// Check username
		// 1. If it is unique
		// 2. If it is currently in the database
		errMsg = checkFirstLastNamePassword(fname, lname, pwd);
		if (errMsg != null)
		{
			req.setAttribute("errorMessage", errMsg);
			return new ActionRouter("error-message-page");
		}
		// /////////////////////////////
		// if everything is OK.. add user then...
		try
		{
			User usr = (User) lUserHome.create(uname, pwd, fname, lname);
		} catch (Exception ex)
		{
			Log.printDebug("DoUserAdd:ejbCreate" + ex.getMessage());
		}
		// res.encodeRedirectURL("sa-addrem-user.do");
		// res.encodeURL("sa-addrem-user.do");
		// res.sendRedirect("sa-addrem-user.do");
		// return new ActionRouter(res.encodeURL("sa-addremove-user"));
		return new ActionRouter("sa-redirect-addrem-user-page");
	}

	// ////////////////////////////////////////////////////////
	protected String filterString(String strbuf, String strtype)
	{
		strbuf = strbuf.trim();
		int ca = 1;
		if (strtype.equals("username"))
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

	protected String checkFirstLastNamePassword(String fname, String lname, String pwd)
	{
		if (fname.length() < 1 || fname.length() >= 50)
		{
			return new String("<p> The first name " + "must have at least 1 or not more than 50 characters."
					+ " <br> Please try again.....");
		}
		if (lname.length() < 1 || lname.length() >= 50)
		{
			return new String("<p> The last name " + "must have at least 1 or not more than 50 characters."
					+ " <br> Please try again.....");
		}
		if (pwd.length() < 4 || fname.length() >= 50)
		{
			return new String("<p> The password " + "must have at least 4 or not more than 50 characters."
					+ " <br> Please try again.....");
		}
		return null;
	}

	protected String checkUsername(String uname, Iterator itr)
	{
		Log.printVerbose(" Check Usernames : outside iter ");
		try
		{
			while (itr.hasNext())
			{
				Log.printVerbose(" Check Usernames ");
				User bufUser = (User) itr.next();
				if (uname.equals((String) bufUser.getUserName()))
				{
					return new String("<p> The username that you want exists in "
							+ " our database. Please try a different username..");
				}
			}
		} catch (Exception ex)
		{
			Log.printVerbose("Exception is loading all users" + ex.getMessage());
		}
		if (uname.length() < 3 || uname.length() > 20)
		{
			return new String("<p> The username that you want "
					+ "must have at least 3 or not more than 20 characters."
					+ " <br> Please try a different username..");
		}
		return null;
	}

	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		if (iUserId != null)
		{
			String uname = req.getParameter("userName");
			String fname = req.getParameter("nameFirst");
			String lname = req.getParameter("nameLast");
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_SYSADMIN;
			atObj.remarks = "sa: user-add " + uname + " (" + fname + " " + lname + ")";
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

public class DoUserAdd extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		fnAuditTrail(servlet, req, res);
		Collection colUsers = null;
		UserHome lUserHome = getUserHome();
		try
		{
			colUsers = (Collection) lUserHome.findAllUsers();
		} catch (Exception ex)
		{
			Log.printDebug("DoUserAddRem:" + ex.getMessage());
		}
		Iterator itr = colUsers.iterator();
		req.setAttribute("UsersIterator", itr);
		// The section above is essentially same as DoUserAddRem
		// Validation at servlet level
		String uname = req.getParameter("userName");
		String pwd = req.getParameter("password");
		String fname = req.getParameter("nameFirst");
		String lname = req.getParameter("nameLast");
		// ensure that the users don't click add user for fun
		if (uname == null || pwd == null || fname == null || lname == null)
		{
			return new ActionRouter("sa-redirect-addrem-user-page");
		}
		uname = filterString(uname, "username");
		fname = filterString(fname, "names");
		lname = filterString(lname, "names");
		// ///////////////////////////////////////////////////////////////
		// todo: Check if user has the right do to so
		boolean bCanAdd = true;
		if (!bCanAdd)
		{
			String errorMsg = "<p> Sorry, you do not have the" + " right to create new user... </p>";
			req.setAttribute("errorMessage", errorMsg);
			return new ActionRouter("error-message-page");
		}
		// ///////////////////////////////////////////////////////////////
		// Check username
		// 1. If it is unique
		// 2. If it is currently in the database
		String errMsg = checkUsername(uname, itr);
		if (errMsg != null)
		{
			req.setAttribute("errorMessage", errMsg);
			return new ActionRouter("error-message-page");
		}
		// ///////////////////////////////////////////////////////////////
		// Check username
		// 1. If it is unique
		// 2. If it is currently in the database
		errMsg = checkFirstLastNamePassword(fname, lname, pwd);
		if (errMsg != null)
		{
			req.setAttribute("errorMessage", errMsg);
			return new ActionRouter("error-message-page");
		}
		// /////////////////////////////
		// if everything is OK.. add user then...
		try
		{
			User usr = (User) lUserHome.create(uname, pwd, fname, lname);
		} catch (Exception ex)
		{
			Log.printDebug("DoUserAdd:ejbCreate" + ex.getMessage());
		}
		// res.encodeRedirectURL("sa-addrem-user.do");
		// res.encodeURL("sa-addrem-user.do");
		// res.sendRedirect("sa-addrem-user.do");
		// return new ActionRouter(res.encodeURL("sa-addremove-user"));
		return new ActionRouter("sa-redirect-addrem-user-page");
	}

	// ////////////////////////////////////////////////////////
	protected String filterString(String strbuf, String strtype)
	{
		strbuf = strbuf.trim();
		int ca = 1;
		if (strtype.equals("username"))
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

	protected String checkFirstLastNamePassword(String fname, String lname, String pwd)
	{
		if (fname.length() < 1 || fname.length() >= 50)
		{
			return new String("<p> The first name " + "must have at least 1 or not more than 50 characters."
					+ " <br> Please try again.....");
		}
		if (lname.length() < 1 || lname.length() >= 50)
		{
			return new String("<p> The last name " + "must have at least 1 or not more than 50 characters."
					+ " <br> Please try again.....");
		}
		if (pwd.length() < 4 || fname.length() >= 50)
		{
			return new String("<p> The password " + "must have at least 4 or not more than 50 characters."
					+ " <br> Please try again.....");
		}
		return null;
	}

	protected String checkUsername(String uname, Iterator itr)
	{
		Log.printVerbose(" Check Usernames : outside iter ");
		try
		{
			while (itr.hasNext())
			{
				Log.printVerbose(" Check Usernames ");
				User bufUser = (User) itr.next();
				if (uname.equals((String) bufUser.getUserName()))
				{
					return new String("<p> The username that you want exists in "
							+ " our database. Please try a different username..");
				}
			}
		} catch (Exception ex)
		{
			Log.printVerbose("Exception is loading all users" + ex.getMessage());
		}
		if (uname.length() < 3 || uname.length() > 20)
		{
			return new String("<p> The username that you want "
					+ "must have at least 3 or not more than 20 characters."
					+ " <br> Please try a different username..");
		}
		return null;
	}

	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		if (iUserId != null)
		{
			String uname = req.getParameter("userName");
			String fname = req.getParameter("nameFirst");
			String lname = req.getParameter("nameLast");
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_SYSADMIN;
			atObj.remarks = "sa: user-add " + uname + " (" + fname + " " + lname + ")";
			AuditTrailNut.fnCreate(atObj);
		}
	}
}
