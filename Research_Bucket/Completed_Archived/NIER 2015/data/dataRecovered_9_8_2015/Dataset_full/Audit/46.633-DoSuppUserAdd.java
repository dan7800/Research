/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.supplier;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.text.*;
import java.sql.*;
import com.vlee.servlet.main.*;
import com.vlee.util.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.user.*;

public class DoSuppUserAdd implements Action
{
	String strClassName = "DoSuppUserAdd";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String suppAccPKID = req.getParameter("suppAccPKID");
		String suppUserName = req.getParameter("suppUserName");
		if (suppAccPKID == null)
		{
			Log.printDebug("NULL suppAccPKID !!");
		}
		if (suppUserName != null)
		{
			// add suppUser
			fnAddSuppUser(servlet, req, res);
			fnGetParams(servlet, req, res);
			return new ActionRouter("supp-setup-added-user-page");
		}
		fnGetParams(servlet, req, res);
		// propagate suppAccCode
		// String suppAccCode = req.getParameter("suppAccCode");
		// req.setAttribute("suppAccCode", suppAccCode);
		return new ActionRouter("supp-setup-add-user-page");
	}

	protected void fnGetParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String suppAccPKID = req.getParameter("suppAccPKID");
		req.setAttribute("suppAccPKID", suppAccPKID);
	}

	protected void fnAddSuppUser(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAddSuppUser()";
		// Get the request paramaters
		// String suppAccCode = req.getParameter("suppAccCode");
		String suppAccPKID = req.getParameter("suppAccPKID");
		String suppUserName = req.getParameter("suppUserName");
		String suppUserPasswd = req.getParameter("suppUserPasswd");
		String suppUserPasswd2 = req.getParameter("suppUserPasswd2");
		String suppUserNameFirst = req.getParameter("suppUserNameFirst");
		String suppUserNameLast = req.getParameter("suppUserNameLast");
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		try
		{
			if (lUsr == null)
			{
				Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
				// return;
				throw new Exception(
						"Invalid Username. Your session may have expired or you may not be authorised to access the system. Please login again");
			}
			// if (suppAccCode == null)
			// {
			// return;
			// throw new Exception("Invalid Supplier Account");
			// }
			else
				Log.printVerbose(strClassName + ":" + funcName + " - suppAccCode = ");
			if (suppUserName == null)
			{
				// return;
				throw new Exception("Invalid Supplier User Name");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - suppUserPasswd = " + suppUserPasswd);
			if (suppUserPasswd == null)
			{
				// return;
				throw new Exception("Invalid Password entered");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - suppUserPasswd = " + suppUserPasswd);
			if (suppUserPasswd2 == null)
			{
				// return;
				throw new Exception("Invalid Password entered");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - suppUserPasswd2 = " + suppUserPasswd2);
			Log.printVerbose("Adding new SuppUser");
			java.util.Date ldt = new java.util.Date();
			Timestamp tsCreate = new Timestamp(ldt.getTime());
			Integer usrid = null;
			try
			{
				usrid = lUsr.getUserId();
			} catch (Exception ex)
			{
				Log.printAudit("User does not exist: " + ex.getMessage());
			}
			// Add the user, ensure it doesn't already exist
			SuppUser lSuppUser = SuppUserNut.getObjectByName(suppUserName);
			if (lSuppUser == null)
			{
				// 1. Validate the input params first
				if ((suppUserPasswd.compareTo("") != 0) && (suppUserPasswd2.compareTo("") != 0))
				{
					// Check password ... TO_DO: more needs to be done here
					if (fnCheckPassword(suppUserPasswd, suppUserPasswd2) == 0)
					{
						Log.printDebug(strClassName + ":" + funcName + " - failed password check.");
						throw new Exception("failed password check");
					}
				}
				// 2. Create the SuppUser object
				SuppUserHome suppUserHome = SuppUserNut.getHome();
				SuppUser newSuppUser = suppUserHome.create(suppUserName, suppUserPasswd, suppUserNameFirst,
						suppUserNameLast, tsCreate, usrid);
				// 3. Create the suppAccountUser link
				Integer iSuppAcc = new Integer(suppAccPKID);
				SuppAccount suppAcc = SuppAccountNut.getHandle(iSuppAcc);
				Integer suppAccId = suppAcc.getPkid();
				Integer newSuppUserPkid = newSuppUser.getPkid();
				SuppAccountUserHome suppAccUserHome = SuppAccountUserNut.getHome();
				SuppAccountUser newAccUserLink = suppAccUserHome
						.create(suppAccId, newSuppUserPkid, "", tsCreate, usrid);
				// 4. Create an empty SuppUserDetails object
				Log.printVerbose("Creating SuppUserDetails ...");
				SuppUserDetailsHome suppUserDetailsHome = SuppUserDetailsNut.getHome();
				SuppUserDetails newSuppUserDetails = suppUserDetailsHome.create(newSuppUserPkid, tsCreate, usrid);
				Log.printVerbose("SuppUserDetails successfully created.");
				// If we reach this stage, we're successful !
				// populate the "addSuppUser" attribute so re-display the added
				// fields
				// req.setAttribute("suppAccCode", suppAccCode);
				req.setAttribute("addSuppUser", newSuppUser);
			} // end if
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("Add SuppUser for SuppAccCode = " + " -  Failed" + ex.getMessage());
			req.setAttribute("addSuppUserErrMsg", ex.getMessage());
		} // end try-catch
	} // end fnAddSuppUser

	protected int fnCheckPassword(String passwd1, String passwd2)
	{
		// Assume password has been validated by javascript first
		// Actually even the routine below should be validated by javascript
		// But this routine is to perform whatever password check that
		// javascript cannot possibly do.
		if (passwd1.compareTo(passwd2) == 0)
			return 1;
		else
			return 0;
	}
} // end class DoSuppUserAdd
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.supplier;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.text.*;
import java.sql.*;
import com.vlee.servlet.main.*;
import com.vlee.util.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.user.*;

public class DoSuppUserAdd implements Action
{
	String strClassName = "DoSuppUserAdd";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String suppAccPKID = req.getParameter("suppAccPKID");
		String suppUserName = req.getParameter("suppUserName");
		if (suppAccPKID == null)
		{
			Log.printDebug("NULL suppAccPKID !!");
		}
		if (suppUserName != null)
		{
			// add suppUser
			fnAddSuppUser(servlet, req, res);
			fnGetParams(servlet, req, res);
			return new ActionRouter("supp-setup-added-user-page");
		}
		fnGetParams(servlet, req, res);
		// propagate suppAccCode
		// String suppAccCode = req.getParameter("suppAccCode");
		// req.setAttribute("suppAccCode", suppAccCode);
		return new ActionRouter("supp-setup-add-user-page");
	}

	protected void fnGetParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String suppAccPKID = req.getParameter("suppAccPKID");
		req.setAttribute("suppAccPKID", suppAccPKID);
	}

	protected void fnAddSuppUser(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAddSuppUser()";
		// Get the request paramaters
		// String suppAccCode = req.getParameter("suppAccCode");
		String suppAccPKID = req.getParameter("suppAccPKID");
		String suppUserName = req.getParameter("suppUserName");
		String suppUserPasswd = req.getParameter("suppUserPasswd");
		String suppUserPasswd2 = req.getParameter("suppUserPasswd2");
		String suppUserNameFirst = req.getParameter("suppUserNameFirst");
		String suppUserNameLast = req.getParameter("suppUserNameLast");
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		try
		{
			if (lUsr == null)
			{
				Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
				// return;
				throw new Exception(
						"Invalid Username. Your session may have expired or you may not be authorised to access the system. Please login again");
			}
			// if (suppAccCode == null)
			// {
			// return;
			// throw new Exception("Invalid Supplier Account");
			// }
			else
				Log.printVerbose(strClassName + ":" + funcName + " - suppAccCode = ");
			if (suppUserName == null)
			{
				// return;
				throw new Exception("Invalid Supplier User Name");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - suppUserPasswd = " + suppUserPasswd);
			if (suppUserPasswd == null)
			{
				// return;
				throw new Exception("Invalid Password entered");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - suppUserPasswd = " + suppUserPasswd);
			if (suppUserPasswd2 == null)
			{
				// return;
				throw new Exception("Invalid Password entered");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - suppUserPasswd2 = " + suppUserPasswd2);
			Log.printVerbose("Adding new SuppUser");
			java.util.Date ldt = new java.util.Date();
			Timestamp tsCreate = new Timestamp(ldt.getTime());
			Integer usrid = null;
			try
			{
				usrid = lUsr.getUserId();
			} catch (Exception ex)
			{
				Log.printAudit("User does not exist: " + ex.getMessage());
			}
			// Add the user, ensure it doesn't already exist
			SuppUser lSuppUser = SuppUserNut.getObjectByName(suppUserName);
			if (lSuppUser == null)
			{
				// 1. Validate the input params first
				if ((suppUserPasswd.compareTo("") != 0) && (suppUserPasswd2.compareTo("") != 0))
				{
					// Check password ... TO_DO: more needs to be done here
					if (fnCheckPassword(suppUserPasswd, suppUserPasswd2) == 0)
					{
						Log.printDebug(strClassName + ":" + funcName + " - failed password check.");
						throw new Exception("failed password check");
					}
				}
				// 2. Create the SuppUser object
				SuppUserHome suppUserHome = SuppUserNut.getHome();
				SuppUser newSuppUser = suppUserHome.create(suppUserName, suppUserPasswd, suppUserNameFirst,
						suppUserNameLast, tsCreate, usrid);
				// 3. Create the suppAccountUser link
				Integer iSuppAcc = new Integer(suppAccPKID);
				SuppAccount suppAcc = SuppAccountNut.getHandle(iSuppAcc);
				Integer suppAccId = suppAcc.getPkid();
				Integer newSuppUserPkid = newSuppUser.getPkid();
				SuppAccountUserHome suppAccUserHome = SuppAccountUserNut.getHome();
				SuppAccountUser newAccUserLink = suppAccUserHome
						.create(suppAccId, newSuppUserPkid, "", tsCreate, usrid);
				// 4. Create an empty SuppUserDetails object
				Log.printVerbose("Creating SuppUserDetails ...");
				SuppUserDetailsHome suppUserDetailsHome = SuppUserDetailsNut.getHome();
				SuppUserDetails newSuppUserDetails = suppUserDetailsHome.create(newSuppUserPkid, tsCreate, usrid);
				Log.printVerbose("SuppUserDetails successfully created.");
				// If we reach this stage, we're successful !
				// populate the "addSuppUser" attribute so re-display the added
				// fields
				// req.setAttribute("suppAccCode", suppAccCode);
				req.setAttribute("addSuppUser", newSuppUser);
			} // end if
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("Add SuppUser for SuppAccCode = " + " -  Failed" + ex.getMessage());
			req.setAttribute("addSuppUserErrMsg", ex.getMessage());
		} // end try-catch
	} // end fnAddSuppUser

	protected int fnCheckPassword(String passwd1, String passwd2)
	{
		// Assume password has been validated by javascript first
		// Actually even the routine below should be validated by javascript
		// But this routine is to perform whatever password check that
		// javascript cannot possibly do.
		if (passwd1.compareTo(passwd2) == 0)
			return 1;
		else
			return 0;
	}
} // end class DoSuppUserAdd
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.supplier;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.text.*;
import java.sql.*;
import com.vlee.servlet.main.*;
import com.vlee.util.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.user.*;

public class DoSuppUserAdd implements Action
{
	String strClassName = "DoSuppUserAdd";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String suppAccPKID = req.getParameter("suppAccPKID");
		String suppUserName = req.getParameter("suppUserName");
		if (suppAccPKID == null)
		{
			Log.printDebug("NULL suppAccPKID !!");
		}
		if (suppUserName != null)
		{
			// add suppUser
			fnAddSuppUser(servlet, req, res);
			fnGetParams(servlet, req, res);
			return new ActionRouter("supp-setup-added-user-page");
		}
		fnGetParams(servlet, req, res);
		// propagate suppAccCode
		// String suppAccCode = req.getParameter("suppAccCode");
		// req.setAttribute("suppAccCode", suppAccCode);
		return new ActionRouter("supp-setup-add-user-page");
	}

	protected void fnGetParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String suppAccPKID = req.getParameter("suppAccPKID");
		req.setAttribute("suppAccPKID", suppAccPKID);
	}

	protected void fnAddSuppUser(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAddSuppUser()";
		// Get the request paramaters
		// String suppAccCode = req.getParameter("suppAccCode");
		String suppAccPKID = req.getParameter("suppAccPKID");
		String suppUserName = req.getParameter("suppUserName");
		String suppUserPasswd = req.getParameter("suppUserPasswd");
		String suppUserPasswd2 = req.getParameter("suppUserPasswd2");
		String suppUserNameFirst = req.getParameter("suppUserNameFirst");
		String suppUserNameLast = req.getParameter("suppUserNameLast");
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		try
		{
			if (lUsr == null)
			{
				Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
				// return;
				throw new Exception(
						"Invalid Username. Your session may have expired or you may not be authorised to access the system. Please login again");
			}
			// if (suppAccCode == null)
			// {
			// return;
			// throw new Exception("Invalid Supplier Account");
			// }
			else
				Log.printVerbose(strClassName + ":" + funcName + " - suppAccCode = ");
			if (suppUserName == null)
			{
				// return;
				throw new Exception("Invalid Supplier User Name");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - suppUserPasswd = " + suppUserPasswd);
			if (suppUserPasswd == null)
			{
				// return;
				throw new Exception("Invalid Password entered");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - suppUserPasswd = " + suppUserPasswd);
			if (suppUserPasswd2 == null)
			{
				// return;
				throw new Exception("Invalid Password entered");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - suppUserPasswd2 = " + suppUserPasswd2);
			Log.printVerbose("Adding new SuppUser");
			java.util.Date ldt = new java.util.Date();
			Timestamp tsCreate = new Timestamp(ldt.getTime());
			Integer usrid = null;
			try
			{
				usrid = lUsr.getUserId();
			} catch (Exception ex)
			{
				Log.printAudit("User does not exist: " + ex.getMessage());
			}
			// Add the user, ensure it doesn't already exist
			SuppUser lSuppUser = SuppUserNut.getObjectByName(suppUserName);
			if (lSuppUser == null)
			{
				// 1. Validate the input params first
				if ((suppUserPasswd.compareTo("") != 0) && (suppUserPasswd2.compareTo("") != 0))
				{
					// Check password ... TO_DO: more needs to be done here
					if (fnCheckPassword(suppUserPasswd, suppUserPasswd2) == 0)
					{
						Log.printDebug(strClassName + ":" + funcName + " - failed password check.");
						throw new Exception("failed password check");
					}
				}
				// 2. Create the SuppUser object
				SuppUserHome suppUserHome = SuppUserNut.getHome();
				SuppUser newSuppUser = suppUserHome.create(suppUserName, suppUserPasswd, suppUserNameFirst,
						suppUserNameLast, tsCreate, usrid);
				// 3. Create the suppAccountUser link
				Integer iSuppAcc = new Integer(suppAccPKID);
				SuppAccount suppAcc = SuppAccountNut.getHandle(iSuppAcc);
				Integer suppAccId = suppAcc.getPkid();
				Integer newSuppUserPkid = newSuppUser.getPkid();
				SuppAccountUserHome suppAccUserHome = SuppAccountUserNut.getHome();
				SuppAccountUser newAccUserLink = suppAccUserHome
						.create(suppAccId, newSuppUserPkid, "", tsCreate, usrid);
				// 4. Create an empty SuppUserDetails object
				Log.printVerbose("Creating SuppUserDetails ...");
				SuppUserDetailsHome suppUserDetailsHome = SuppUserDetailsNut.getHome();
				SuppUserDetails newSuppUserDetails = suppUserDetailsHome.create(newSuppUserPkid, tsCreate, usrid);
				Log.printVerbose("SuppUserDetails successfully created.");
				// If we reach this stage, we're successful !
				// populate the "addSuppUser" attribute so re-display the added
				// fields
				// req.setAttribute("suppAccCode", suppAccCode);
				req.setAttribute("addSuppUser", newSuppUser);
			} // end if
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("Add SuppUser for SuppAccCode = " + " -  Failed" + ex.getMessage());
			req.setAttribute("addSuppUserErrMsg", ex.getMessage());
		} // end try-catch
	} // end fnAddSuppUser

	protected int fnCheckPassword(String passwd1, String passwd2)
	{
		// Assume password has been validated by javascript first
		// Actually even the routine below should be validated by javascript
		// But this routine is to perform whatever password check that
		// javascript cannot possibly do.
		if (passwd1.compareTo(passwd2) == 0)
			return 1;
		else
			return 0;
	}
} // end class DoSuppUserAdd
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.supplier;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.text.*;
import java.sql.*;
import com.vlee.servlet.main.*;
import com.vlee.util.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.user.*;

public class DoSuppUserAdd implements Action
{
	String strClassName = "DoSuppUserAdd";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String suppAccPKID = req.getParameter("suppAccPKID");
		String suppUserName = req.getParameter("suppUserName");
		if (suppAccPKID == null)
		{
			Log.printDebug("NULL suppAccPKID !!");
		}
		if (suppUserName != null)
		{
			// add suppUser
			fnAddSuppUser(servlet, req, res);
			fnGetParams(servlet, req, res);
			return new ActionRouter("supp-setup-added-user-page");
		}
		fnGetParams(servlet, req, res);
		// propagate suppAccCode
		// String suppAccCode = req.getParameter("suppAccCode");
		// req.setAttribute("suppAccCode", suppAccCode);
		return new ActionRouter("supp-setup-add-user-page");
	}

	protected void fnGetParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String suppAccPKID = req.getParameter("suppAccPKID");
		req.setAttribute("suppAccPKID", suppAccPKID);
	}

	protected void fnAddSuppUser(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAddSuppUser()";
		// Get the request paramaters
		// String suppAccCode = req.getParameter("suppAccCode");
		String suppAccPKID = req.getParameter("suppAccPKID");
		String suppUserName = req.getParameter("suppUserName");
		String suppUserPasswd = req.getParameter("suppUserPasswd");
		String suppUserPasswd2 = req.getParameter("suppUserPasswd2");
		String suppUserNameFirst = req.getParameter("suppUserNameFirst");
		String suppUserNameLast = req.getParameter("suppUserNameLast");
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		try
		{
			if (lUsr == null)
			{
				Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
				// return;
				throw new Exception(
						"Invalid Username. Your session may have expired or you may not be authorised to access the system. Please login again");
			}
			// if (suppAccCode == null)
			// {
			// return;
			// throw new Exception("Invalid Supplier Account");
			// }
			else
				Log.printVerbose(strClassName + ":" + funcName + " - suppAccCode = ");
			if (suppUserName == null)
			{
				// return;
				throw new Exception("Invalid Supplier User Name");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - suppUserPasswd = " + suppUserPasswd);
			if (suppUserPasswd == null)
			{
				// return;
				throw new Exception("Invalid Password entered");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - suppUserPasswd = " + suppUserPasswd);
			if (suppUserPasswd2 == null)
			{
				// return;
				throw new Exception("Invalid Password entered");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - suppUserPasswd2 = " + suppUserPasswd2);
			Log.printVerbose("Adding new SuppUser");
			java.util.Date ldt = new java.util.Date();
			Timestamp tsCreate = new Timestamp(ldt.getTime());
			Integer usrid = null;
			try
			{
				usrid = lUsr.getUserId();
			} catch (Exception ex)
			{
				Log.printAudit("User does not exist: " + ex.getMessage());
			}
			// Add the user, ensure it doesn't already exist
			SuppUser lSuppUser = SuppUserNut.getObjectByName(suppUserName);
			if (lSuppUser == null)
			{
				// 1. Validate the input params first
				if ((suppUserPasswd.compareTo("") != 0) && (suppUserPasswd2.compareTo("") != 0))
				{
					// Check password ... TO_DO: more needs to be done here
					if (fnCheckPassword(suppUserPasswd, suppUserPasswd2) == 0)
					{
						Log.printDebug(strClassName + ":" + funcName + " - failed password check.");
						throw new Exception("failed password check");
					}
				}
				// 2. Create the SuppUser object
				SuppUserHome suppUserHome = SuppUserNut.getHome();
				SuppUser newSuppUser = suppUserHome.create(suppUserName, suppUserPasswd, suppUserNameFirst,
						suppUserNameLast, tsCreate, usrid);
				// 3. Create the suppAccountUser link
				Integer iSuppAcc = new Integer(suppAccPKID);
				SuppAccount suppAcc = SuppAccountNut.getHandle(iSuppAcc);
				Integer suppAccId = suppAcc.getPkid();
				Integer newSuppUserPkid = newSuppUser.getPkid();
				SuppAccountUserHome suppAccUserHome = SuppAccountUserNut.getHome();
				SuppAccountUser newAccUserLink = suppAccUserHome
						.create(suppAccId, newSuppUserPkid, "", tsCreate, usrid);
				// 4. Create an empty SuppUserDetails object
				Log.printVerbose("Creating SuppUserDetails ...");
				SuppUserDetailsHome suppUserDetailsHome = SuppUserDetailsNut.getHome();
				SuppUserDetails newSuppUserDetails = suppUserDetailsHome.create(newSuppUserPkid, tsCreate, usrid);
				Log.printVerbose("SuppUserDetails successfully created.");
				// If we reach this stage, we're successful !
				// populate the "addSuppUser" attribute so re-display the added
				// fields
				// req.setAttribute("suppAccCode", suppAccCode);
				req.setAttribute("addSuppUser", newSuppUser);
			} // end if
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("Add SuppUser for SuppAccCode = " + " -  Failed" + ex.getMessage());
			req.setAttribute("addSuppUserErrMsg", ex.getMessage());
		} // end try-catch
	} // end fnAddSuppUser

	protected int fnCheckPassword(String passwd1, String passwd2)
	{
		// Assume password has been validated by javascript first
		// Actually even the routine below should be validated by javascript
		// But this routine is to perform whatever password check that
		// javascript cannot possibly do.
		if (passwd1.compareTo(passwd2) == 0)
			return 1;
		else
			return 0;
	}
} // end class DoSuppUserAdd
