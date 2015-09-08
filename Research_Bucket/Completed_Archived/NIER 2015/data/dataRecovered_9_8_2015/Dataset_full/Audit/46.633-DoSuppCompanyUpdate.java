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

public class DoSuppCompanyUpdate implements Action
{
	String strClassName = "DoSuppCompanyUpdate";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// update suppCompany
		fnUpdateSuppCompany(servlet, req, res);
		// propagate suppAccPKID
		String suppAccPKID = req.getParameter("suppAccPKID");
		req.setAttribute("suppAccPKID", suppAccPKID);
		return new ActionRouter("supp-setup-updated-company-page");
	}

	protected void fnUpdateSuppCompany(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateSuppCompany()";
		// Get the request paramaters
		String suppAccPKID = req.getParameter("suppAccPKID");
		String suppCompanyPkid = req.getParameter("suppCompanyPkid");
		String suppCompanyCode = req.getParameter("suppCompanyCode");
		String suppCompanyRegNo = req.getParameter("suppCompanyRegNo");
		String suppCompanyName = req.getParameter("suppCompanyName");
		String suppCompanyDesc = req.getParameter("suppCompanyDesc");
		String suppCompanyAddr1 = req.getParameter("suppCompanyAddr1");
		String suppCompanyAddr2 = req.getParameter("suppCompanyAddr2");
		String suppCompanyAddr3 = req.getParameter("suppCompanyAddr3");
		String suppCompanyZip = req.getParameter("suppCompanyZip");
		String suppCompanyState = req.getParameter("suppCompanyState");
		String suppCompanyCountryCode = req.getParameter("suppCompanyCountryCode");
		String suppCompanyWebUrl = req.getParameter("suppCompanyWebUrl");
		// Update the last modified and user_edit fields
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		Integer usrid = null;
		if (lUsr == null)
		{
			Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
			return;
		}
		try
		{
			usrid = lUsr.getUserId();
		} catch (Exception ex)
		{
			Log.printAudit("User does not exist: " + ex.getMessage());
		}
		// get the current timestamp
		java.util.Date ldt = new java.util.Date();
		Timestamp tsCreate = new Timestamp(ldt.getTime());
		/*
		 * if (suppAccPKID == null) return; else Log.printVerbose(strClassName +
		 * ":" + funcName + " - suppAccPKID = " + suppAccPKID); if (suppUserPkid ==
		 * null) return; else Log.printVerbose(strClassName + ":" + funcName + " -
		 * suppUserPkid = " + suppUserPkid); if (suppUserName == null) return;
		 * else Log.printVerbose(strClassName + ":" + funcName + " -
		 * suppUserNewPasswd = " + suppUserNewPasswd); if (suppUserNewPasswd ==
		 * null) return; else Log.printVerbose(strClassName + ":" + funcName + " -
		 * suppUserNewPasswd = " + suppUserNewPasswd); if (suppUserNewPasswd2 ==
		 * null) return; else Log.printVerbose(strClassName + ":" + funcName + " -
		 * suppUserNewPasswd2 = " + suppUserNewPasswd2);
		 */
		try
		{
			// Get the suppAccId first
			Integer suppAccId = new Integer(suppAccPKID);
			// SuppAccount suppAcc =
			// SuppAccountNut.getObjectByCode(suppAccPKID);
			SuppAccount suppAcc = SuppAccountNut.getHandle(suppAccId);
			// Integer suppAccId = suppAcc.getPkid();
			SuppCompany lSuppCompany = SuppCompanyNut.getHandle(new Integer(suppCompanyPkid));
			if (lSuppCompany != null)
			{
				lSuppCompany.setCode(suppCompanyCode);
				lSuppCompany.setRegNo(suppCompanyRegNo);
				lSuppCompany.setName(suppCompanyName);
				lSuppCompany.setDescription(suppCompanyDesc);
				lSuppCompany.setAddr1(suppCompanyAddr1);
				lSuppCompany.setAddr2(suppCompanyAddr2);
				lSuppCompany.setAddr3(suppCompanyAddr3);
				lSuppCompany.setZip(suppCompanyZip);
				lSuppCompany.setState(suppCompanyState);
				lSuppCompany.setCountryCode(suppCompanyCountryCode);
				lSuppCompany.setWebUrl(suppCompanyWebUrl);
				// and then update the lastModified and userIdUpdate fields
				lSuppCompany.setLastUpdate(tsCreate);
				lSuppCompany.setUserIdUpdate(usrid);
				// populate the "editSuppCompany" attribute so re-display the
				// edited fields
				// req.setAttribute("suppAccPKID", suppAccPKID);
				req.setAttribute("editSuppCompany", lSuppCompany);
			} // end if
		} catch (Exception ex)
		{
			Log.printDebug("Update SuppCompany for SuppAccCode = " + suppAccPKID + " -  Failed" + ex.getMessage());
		} // end try-catch
	} // end fnUpdateSuppCompany

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
} // end class DoSuppCompanyUpdate
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

public class DoSuppCompanyUpdate implements Action
{
	String strClassName = "DoSuppCompanyUpdate";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// update suppCompany
		fnUpdateSuppCompany(servlet, req, res);
		// propagate suppAccPKID
		String suppAccPKID = req.getParameter("suppAccPKID");
		req.setAttribute("suppAccPKID", suppAccPKID);
		return new ActionRouter("supp-setup-updated-company-page");
	}

	protected void fnUpdateSuppCompany(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateSuppCompany()";
		// Get the request paramaters
		String suppAccPKID = req.getParameter("suppAccPKID");
		String suppCompanyPkid = req.getParameter("suppCompanyPkid");
		String suppCompanyCode = req.getParameter("suppCompanyCode");
		String suppCompanyRegNo = req.getParameter("suppCompanyRegNo");
		String suppCompanyName = req.getParameter("suppCompanyName");
		String suppCompanyDesc = req.getParameter("suppCompanyDesc");
		String suppCompanyAddr1 = req.getParameter("suppCompanyAddr1");
		String suppCompanyAddr2 = req.getParameter("suppCompanyAddr2");
		String suppCompanyAddr3 = req.getParameter("suppCompanyAddr3");
		String suppCompanyZip = req.getParameter("suppCompanyZip");
		String suppCompanyState = req.getParameter("suppCompanyState");
		String suppCompanyCountryCode = req.getParameter("suppCompanyCountryCode");
		String suppCompanyWebUrl = req.getParameter("suppCompanyWebUrl");
		// Update the last modified and user_edit fields
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		Integer usrid = null;
		if (lUsr == null)
		{
			Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
			return;
		}
		try
		{
			usrid = lUsr.getUserId();
		} catch (Exception ex)
		{
			Log.printAudit("User does not exist: " + ex.getMessage());
		}
		// get the current timestamp
		java.util.Date ldt = new java.util.Date();
		Timestamp tsCreate = new Timestamp(ldt.getTime());
		/*
		 * if (suppAccPKID == null) return; else Log.printVerbose(strClassName +
		 * ":" + funcName + " - suppAccPKID = " + suppAccPKID); if (suppUserPkid ==
		 * null) return; else Log.printVerbose(strClassName + ":" + funcName + " -
		 * suppUserPkid = " + suppUserPkid); if (suppUserName == null) return;
		 * else Log.printVerbose(strClassName + ":" + funcName + " -
		 * suppUserNewPasswd = " + suppUserNewPasswd); if (suppUserNewPasswd ==
		 * null) return; else Log.printVerbose(strClassName + ":" + funcName + " -
		 * suppUserNewPasswd = " + suppUserNewPasswd); if (suppUserNewPasswd2 ==
		 * null) return; else Log.printVerbose(strClassName + ":" + funcName + " -
		 * suppUserNewPasswd2 = " + suppUserNewPasswd2);
		 */
		try
		{
			// Get the suppAccId first
			Integer suppAccId = new Integer(suppAccPKID);
			// SuppAccount suppAcc =
			// SuppAccountNut.getObjectByCode(suppAccPKID);
			SuppAccount suppAcc = SuppAccountNut.getHandle(suppAccId);
			// Integer suppAccId = suppAcc.getPkid();
			SuppCompany lSuppCompany = SuppCompanyNut.getHandle(new Integer(suppCompanyPkid));
			if (lSuppCompany != null)
			{
				lSuppCompany.setCode(suppCompanyCode);
				lSuppCompany.setRegNo(suppCompanyRegNo);
				lSuppCompany.setName(suppCompanyName);
				lSuppCompany.setDescription(suppCompanyDesc);
				lSuppCompany.setAddr1(suppCompanyAddr1);
				lSuppCompany.setAddr2(suppCompanyAddr2);
				lSuppCompany.setAddr3(suppCompanyAddr3);
				lSuppCompany.setZip(suppCompanyZip);
				lSuppCompany.setState(suppCompanyState);
				lSuppCompany.setCountryCode(suppCompanyCountryCode);
				lSuppCompany.setWebUrl(suppCompanyWebUrl);
				// and then update the lastModified and userIdUpdate fields
				lSuppCompany.setLastUpdate(tsCreate);
				lSuppCompany.setUserIdUpdate(usrid);
				// populate the "editSuppCompany" attribute so re-display the
				// edited fields
				// req.setAttribute("suppAccPKID", suppAccPKID);
				req.setAttribute("editSuppCompany", lSuppCompany);
			} // end if
		} catch (Exception ex)
		{
			Log.printDebug("Update SuppCompany for SuppAccCode = " + suppAccPKID + " -  Failed" + ex.getMessage());
		} // end try-catch
	} // end fnUpdateSuppCompany

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
} // end class DoSuppCompanyUpdate
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

public class DoSuppCompanyUpdate implements Action
{
	String strClassName = "DoSuppCompanyUpdate";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// update suppCompany
		fnUpdateSuppCompany(servlet, req, res);
		// propagate suppAccPKID
		String suppAccPKID = req.getParameter("suppAccPKID");
		req.setAttribute("suppAccPKID", suppAccPKID);
		return new ActionRouter("supp-setup-updated-company-page");
	}

	protected void fnUpdateSuppCompany(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateSuppCompany()";
		// Get the request paramaters
		String suppAccPKID = req.getParameter("suppAccPKID");
		String suppCompanyPkid = req.getParameter("suppCompanyPkid");
		String suppCompanyCode = req.getParameter("suppCompanyCode");
		String suppCompanyRegNo = req.getParameter("suppCompanyRegNo");
		String suppCompanyName = req.getParameter("suppCompanyName");
		String suppCompanyDesc = req.getParameter("suppCompanyDesc");
		String suppCompanyAddr1 = req.getParameter("suppCompanyAddr1");
		String suppCompanyAddr2 = req.getParameter("suppCompanyAddr2");
		String suppCompanyAddr3 = req.getParameter("suppCompanyAddr3");
		String suppCompanyZip = req.getParameter("suppCompanyZip");
		String suppCompanyState = req.getParameter("suppCompanyState");
		String suppCompanyCountryCode = req.getParameter("suppCompanyCountryCode");
		String suppCompanyWebUrl = req.getParameter("suppCompanyWebUrl");
		// Update the last modified and user_edit fields
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		Integer usrid = null;
		if (lUsr == null)
		{
			Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
			return;
		}
		try
		{
			usrid = lUsr.getUserId();
		} catch (Exception ex)
		{
			Log.printAudit("User does not exist: " + ex.getMessage());
		}
		// get the current timestamp
		java.util.Date ldt = new java.util.Date();
		Timestamp tsCreate = new Timestamp(ldt.getTime());
		/*
		 * if (suppAccPKID == null) return; else Log.printVerbose(strClassName +
		 * ":" + funcName + " - suppAccPKID = " + suppAccPKID); if (suppUserPkid ==
		 * null) return; else Log.printVerbose(strClassName + ":" + funcName + " -
		 * suppUserPkid = " + suppUserPkid); if (suppUserName == null) return;
		 * else Log.printVerbose(strClassName + ":" + funcName + " -
		 * suppUserNewPasswd = " + suppUserNewPasswd); if (suppUserNewPasswd ==
		 * null) return; else Log.printVerbose(strClassName + ":" + funcName + " -
		 * suppUserNewPasswd = " + suppUserNewPasswd); if (suppUserNewPasswd2 ==
		 * null) return; else Log.printVerbose(strClassName + ":" + funcName + " -
		 * suppUserNewPasswd2 = " + suppUserNewPasswd2);
		 */
		try
		{
			// Get the suppAccId first
			Integer suppAccId = new Integer(suppAccPKID);
			// SuppAccount suppAcc =
			// SuppAccountNut.getObjectByCode(suppAccPKID);
			SuppAccount suppAcc = SuppAccountNut.getHandle(suppAccId);
			// Integer suppAccId = suppAcc.getPkid();
			SuppCompany lSuppCompany = SuppCompanyNut.getHandle(new Integer(suppCompanyPkid));
			if (lSuppCompany != null)
			{
				lSuppCompany.setCode(suppCompanyCode);
				lSuppCompany.setRegNo(suppCompanyRegNo);
				lSuppCompany.setName(suppCompanyName);
				lSuppCompany.setDescription(suppCompanyDesc);
				lSuppCompany.setAddr1(suppCompanyAddr1);
				lSuppCompany.setAddr2(suppCompanyAddr2);
				lSuppCompany.setAddr3(suppCompanyAddr3);
				lSuppCompany.setZip(suppCompanyZip);
				lSuppCompany.setState(suppCompanyState);
				lSuppCompany.setCountryCode(suppCompanyCountryCode);
				lSuppCompany.setWebUrl(suppCompanyWebUrl);
				// and then update the lastModified and userIdUpdate fields
				lSuppCompany.setLastUpdate(tsCreate);
				lSuppCompany.setUserIdUpdate(usrid);
				// populate the "editSuppCompany" attribute so re-display the
				// edited fields
				// req.setAttribute("suppAccPKID", suppAccPKID);
				req.setAttribute("editSuppCompany", lSuppCompany);
			} // end if
		} catch (Exception ex)
		{
			Log.printDebug("Update SuppCompany for SuppAccCode = " + suppAccPKID + " -  Failed" + ex.getMessage());
		} // end try-catch
	} // end fnUpdateSuppCompany

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
} // end class DoSuppCompanyUpdate
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

public class DoSuppCompanyUpdate implements Action
{
	String strClassName = "DoSuppCompanyUpdate";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// update suppCompany
		fnUpdateSuppCompany(servlet, req, res);
		// propagate suppAccPKID
		String suppAccPKID = req.getParameter("suppAccPKID");
		req.setAttribute("suppAccPKID", suppAccPKID);
		return new ActionRouter("supp-setup-updated-company-page");
	}

	protected void fnUpdateSuppCompany(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateSuppCompany()";
		// Get the request paramaters
		String suppAccPKID = req.getParameter("suppAccPKID");
		String suppCompanyPkid = req.getParameter("suppCompanyPkid");
		String suppCompanyCode = req.getParameter("suppCompanyCode");
		String suppCompanyRegNo = req.getParameter("suppCompanyRegNo");
		String suppCompanyName = req.getParameter("suppCompanyName");
		String suppCompanyDesc = req.getParameter("suppCompanyDesc");
		String suppCompanyAddr1 = req.getParameter("suppCompanyAddr1");
		String suppCompanyAddr2 = req.getParameter("suppCompanyAddr2");
		String suppCompanyAddr3 = req.getParameter("suppCompanyAddr3");
		String suppCompanyZip = req.getParameter("suppCompanyZip");
		String suppCompanyState = req.getParameter("suppCompanyState");
		String suppCompanyCountryCode = req.getParameter("suppCompanyCountryCode");
		String suppCompanyWebUrl = req.getParameter("suppCompanyWebUrl");
		// Update the last modified and user_edit fields
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		Integer usrid = null;
		if (lUsr == null)
		{
			Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
			return;
		}
		try
		{
			usrid = lUsr.getUserId();
		} catch (Exception ex)
		{
			Log.printAudit("User does not exist: " + ex.getMessage());
		}
		// get the current timestamp
		java.util.Date ldt = new java.util.Date();
		Timestamp tsCreate = new Timestamp(ldt.getTime());
		/*
		 * if (suppAccPKID == null) return; else Log.printVerbose(strClassName +
		 * ":" + funcName + " - suppAccPKID = " + suppAccPKID); if (suppUserPkid ==
		 * null) return; else Log.printVerbose(strClassName + ":" + funcName + " -
		 * suppUserPkid = " + suppUserPkid); if (suppUserName == null) return;
		 * else Log.printVerbose(strClassName + ":" + funcName + " -
		 * suppUserNewPasswd = " + suppUserNewPasswd); if (suppUserNewPasswd ==
		 * null) return; else Log.printVerbose(strClassName + ":" + funcName + " -
		 * suppUserNewPasswd = " + suppUserNewPasswd); if (suppUserNewPasswd2 ==
		 * null) return; else Log.printVerbose(strClassName + ":" + funcName + " -
		 * suppUserNewPasswd2 = " + suppUserNewPasswd2);
		 */
		try
		{
			// Get the suppAccId first
			Integer suppAccId = new Integer(suppAccPKID);
			// SuppAccount suppAcc =
			// SuppAccountNut.getObjectByCode(suppAccPKID);
			SuppAccount suppAcc = SuppAccountNut.getHandle(suppAccId);
			// Integer suppAccId = suppAcc.getPkid();
			SuppCompany lSuppCompany = SuppCompanyNut.getHandle(new Integer(suppCompanyPkid));
			if (lSuppCompany != null)
			{
				lSuppCompany.setCode(suppCompanyCode);
				lSuppCompany.setRegNo(suppCompanyRegNo);
				lSuppCompany.setName(suppCompanyName);
				lSuppCompany.setDescription(suppCompanyDesc);
				lSuppCompany.setAddr1(suppCompanyAddr1);
				lSuppCompany.setAddr2(suppCompanyAddr2);
				lSuppCompany.setAddr3(suppCompanyAddr3);
				lSuppCompany.setZip(suppCompanyZip);
				lSuppCompany.setState(suppCompanyState);
				lSuppCompany.setCountryCode(suppCompanyCountryCode);
				lSuppCompany.setWebUrl(suppCompanyWebUrl);
				// and then update the lastModified and userIdUpdate fields
				lSuppCompany.setLastUpdate(tsCreate);
				lSuppCompany.setUserIdUpdate(usrid);
				// populate the "editSuppCompany" attribute so re-display the
				// edited fields
				// req.setAttribute("suppAccPKID", suppAccPKID);
				req.setAttribute("editSuppCompany", lSuppCompany);
			} // end if
		} catch (Exception ex)
		{
			Log.printDebug("Update SuppCompany for SuppAccCode = " + suppAccPKID + " -  Failed" + ex.getMessage());
		} // end try-catch
	} // end fnUpdateSuppCompany

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
} // end class DoSuppCompanyUpdate
