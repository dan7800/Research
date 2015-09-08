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

public class DoSuppCompanyAdd implements Action
{
	String strClassName = "DoSuppCompanyAdd";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String suppAccPKID = req.getParameter("suppAccPKID");
		String suppCompanyCode = req.getParameter("suppCompanyCode");
		if (suppAccPKID == null)
		{
			Log.printDebug("NULL suppAccPKID !!");
		}
		if (suppCompanyCode != null)
		{
			// add suppCompany
			fnAddSuppCompany(servlet, req, res);
			fnGetParams(servlet, req, res);
			return new ActionRouter("supp-setup-added-company-page");
		}
		fnGetParams(servlet, req, res);
		// propagate suppAccPKID
		// String suppAccPKID = req.getParameter("suppAccPKID");
		// req.setAttribute("suppAccPKID", suppAccPKID);
		return new ActionRouter("supp-setup-add-company-page");
	}

	protected void fnGetParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String suppAccPKID = req.getParameter("suppAccPKID");
		req.setAttribute("suppAccPKID", suppAccPKID);
	}

	protected void fnAddSuppCompany(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAddSuppCompany()";
		// Get the request paramaters
		String suppAccPKID = req.getParameter("suppAccPKID");
		String suppCompanyCode = req.getParameter("suppCompanyName");
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
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		try
		{
			if (lUsr == null)
			{
				Log.printDebug(strClassName + ": " + "WARNING - NULL companyName");
				// return;
				throw new Exception(
						"Invalid Companyname. Your session may have expired or you may not be authorised to access the system. Please login again");
			}
			/*
			 * if (suppAccPKID == null) { //return; throw new Exception("Invalid
			 * Supplier Account"); } else Log.printVerbose(strClassName + ":" +
			 * funcName + " - suppAccPKID = " + suppAccPKID); if
			 * (suppCompanyName == null) { //return; throw new
			 * Exception("Invalid Supplier Company Name"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " -
			 * suppCompanyPasswd = " + suppCompanyPasswd); if (suppCompanyPasswd ==
			 * null) { //return; throw new Exception("Invalid Password
			 * entered"); } else Log.printVerbose(strClassName + ":" + funcName + " -
			 * suppCompanyPasswd = " + suppCompanyPasswd); if
			 * (suppCompanyPasswd2 == null) { //return; throw new
			 * Exception("Invalid Password entered"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " -
			 * suppCompanyPasswd2 = " + suppCompanyPasswd2);
			 */
			Log.printVerbose("Adding new SuppCompany");
			java.util.Date ldt = new java.util.Date();
			Timestamp tsCreate = new Timestamp(ldt.getTime());
			Integer usrid = null;
			try
			{
				usrid = lUsr.getUserId();
			} catch (Exception ex)
			{
				Log.printAudit("Company does not exist: " + ex.getMessage());
			}
			// Add the company, ensure it doesn't already exist
			SuppCompany lSuppCompany = SuppCompanyNut.getObjectByCode(suppCompanyCode);
			if (lSuppCompany == null)
			{
				// 1. Validate the input params first
				/*
				 * if((suppCompanyPasswd.compareTo("") != 0) &&
				 * (suppCompanyPasswd2.compareTo("") != 0)) { // Check password
				 * ... TO_DO: more needs to be done here
				 * if(fnCheckPassword(suppCompanyPasswd,suppCompanyPasswd2) ==
				 * 0) { Log.printDebug(strClassName + ":" + funcName + " -
				 * failed password check."); throw new Exception("failed
				 * password check"); } }
				 */
				// 2. Create the SuppCompany object
				Integer iSuppAcc = new Integer(suppAccPKID);
				SuppAccount suppAcc = SuppAccountNut.getHandle(iSuppAcc);
				Integer suppAccId = suppAcc.getPkid();
				SuppCompanyHome suppCompanyHome = SuppCompanyNut.getHome();
				SuppCompany newSuppCompany = suppCompanyHome.create(suppCompanyCode, suppCompanyRegNo, suppCompanyName,
						suppCompanyDesc, suppCompanyAddr1, suppCompanyAddr2, suppCompanyAddr3, suppCompanyZip,
						suppCompanyState, suppCompanyCountryCode, suppCompanyWebUrl, suppAccId, tsCreate, usrid);
				// If we reach this stage, we're successful !
				// populate the "addSuppCompany" attribute so re-display the
				// added fields
				// req.setAttribute("suppAccPKID", suppAccPKID);
				req.setAttribute("addSuppCompany", newSuppCompany);
			} // end if
		} catch (Exception ex)
		{
			Log.printDebug("Add SuppCompany for SuppAccCode = " + suppAccPKID + " -  Failed" + ex.getMessage());
			req.setAttribute("addSuppCompanyErrMsg", ex.getMessage());
		} // end try-catch
	} // end fnAddSuppCompany

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
} // end class DoSuppCompanyAdd
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

public class DoSuppCompanyAdd implements Action
{
	String strClassName = "DoSuppCompanyAdd";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String suppAccPKID = req.getParameter("suppAccPKID");
		String suppCompanyCode = req.getParameter("suppCompanyCode");
		if (suppAccPKID == null)
		{
			Log.printDebug("NULL suppAccPKID !!");
		}
		if (suppCompanyCode != null)
		{
			// add suppCompany
			fnAddSuppCompany(servlet, req, res);
			fnGetParams(servlet, req, res);
			return new ActionRouter("supp-setup-added-company-page");
		}
		fnGetParams(servlet, req, res);
		// propagate suppAccPKID
		// String suppAccPKID = req.getParameter("suppAccPKID");
		// req.setAttribute("suppAccPKID", suppAccPKID);
		return new ActionRouter("supp-setup-add-company-page");
	}

	protected void fnGetParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String suppAccPKID = req.getParameter("suppAccPKID");
		req.setAttribute("suppAccPKID", suppAccPKID);
	}

	protected void fnAddSuppCompany(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAddSuppCompany()";
		// Get the request paramaters
		String suppAccPKID = req.getParameter("suppAccPKID");
		String suppCompanyCode = req.getParameter("suppCompanyName");
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
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		try
		{
			if (lUsr == null)
			{
				Log.printDebug(strClassName + ": " + "WARNING - NULL companyName");
				// return;
				throw new Exception(
						"Invalid Companyname. Your session may have expired or you may not be authorised to access the system. Please login again");
			}
			/*
			 * if (suppAccPKID == null) { //return; throw new Exception("Invalid
			 * Supplier Account"); } else Log.printVerbose(strClassName + ":" +
			 * funcName + " - suppAccPKID = " + suppAccPKID); if
			 * (suppCompanyName == null) { //return; throw new
			 * Exception("Invalid Supplier Company Name"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " -
			 * suppCompanyPasswd = " + suppCompanyPasswd); if (suppCompanyPasswd ==
			 * null) { //return; throw new Exception("Invalid Password
			 * entered"); } else Log.printVerbose(strClassName + ":" + funcName + " -
			 * suppCompanyPasswd = " + suppCompanyPasswd); if
			 * (suppCompanyPasswd2 == null) { //return; throw new
			 * Exception("Invalid Password entered"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " -
			 * suppCompanyPasswd2 = " + suppCompanyPasswd2);
			 */
			Log.printVerbose("Adding new SuppCompany");
			java.util.Date ldt = new java.util.Date();
			Timestamp tsCreate = new Timestamp(ldt.getTime());
			Integer usrid = null;
			try
			{
				usrid = lUsr.getUserId();
			} catch (Exception ex)
			{
				Log.printAudit("Company does not exist: " + ex.getMessage());
			}
			// Add the company, ensure it doesn't already exist
			SuppCompany lSuppCompany = SuppCompanyNut.getObjectByCode(suppCompanyCode);
			if (lSuppCompany == null)
			{
				// 1. Validate the input params first
				/*
				 * if((suppCompanyPasswd.compareTo("") != 0) &&
				 * (suppCompanyPasswd2.compareTo("") != 0)) { // Check password
				 * ... TO_DO: more needs to be done here
				 * if(fnCheckPassword(suppCompanyPasswd,suppCompanyPasswd2) ==
				 * 0) { Log.printDebug(strClassName + ":" + funcName + " -
				 * failed password check."); throw new Exception("failed
				 * password check"); } }
				 */
				// 2. Create the SuppCompany object
				Integer iSuppAcc = new Integer(suppAccPKID);
				SuppAccount suppAcc = SuppAccountNut.getHandle(iSuppAcc);
				Integer suppAccId = suppAcc.getPkid();
				SuppCompanyHome suppCompanyHome = SuppCompanyNut.getHome();
				SuppCompany newSuppCompany = suppCompanyHome.create(suppCompanyCode, suppCompanyRegNo, suppCompanyName,
						suppCompanyDesc, suppCompanyAddr1, suppCompanyAddr2, suppCompanyAddr3, suppCompanyZip,
						suppCompanyState, suppCompanyCountryCode, suppCompanyWebUrl, suppAccId, tsCreate, usrid);
				// If we reach this stage, we're successful !
				// populate the "addSuppCompany" attribute so re-display the
				// added fields
				// req.setAttribute("suppAccPKID", suppAccPKID);
				req.setAttribute("addSuppCompany", newSuppCompany);
			} // end if
		} catch (Exception ex)
		{
			Log.printDebug("Add SuppCompany for SuppAccCode = " + suppAccPKID + " -  Failed" + ex.getMessage());
			req.setAttribute("addSuppCompanyErrMsg", ex.getMessage());
		} // end try-catch
	} // end fnAddSuppCompany

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
} // end class DoSuppCompanyAdd
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

public class DoSuppCompanyAdd implements Action
{
	String strClassName = "DoSuppCompanyAdd";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String suppAccPKID = req.getParameter("suppAccPKID");
		String suppCompanyCode = req.getParameter("suppCompanyCode");
		if (suppAccPKID == null)
		{
			Log.printDebug("NULL suppAccPKID !!");
		}
		if (suppCompanyCode != null)
		{
			// add suppCompany
			fnAddSuppCompany(servlet, req, res);
			fnGetParams(servlet, req, res);
			return new ActionRouter("supp-setup-added-company-page");
		}
		fnGetParams(servlet, req, res);
		// propagate suppAccPKID
		// String suppAccPKID = req.getParameter("suppAccPKID");
		// req.setAttribute("suppAccPKID", suppAccPKID);
		return new ActionRouter("supp-setup-add-company-page");
	}

	protected void fnGetParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String suppAccPKID = req.getParameter("suppAccPKID");
		req.setAttribute("suppAccPKID", suppAccPKID);
	}

	protected void fnAddSuppCompany(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAddSuppCompany()";
		// Get the request paramaters
		String suppAccPKID = req.getParameter("suppAccPKID");
		String suppCompanyCode = req.getParameter("suppCompanyName");
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
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		try
		{
			if (lUsr == null)
			{
				Log.printDebug(strClassName + ": " + "WARNING - NULL companyName");
				// return;
				throw new Exception(
						"Invalid Companyname. Your session may have expired or you may not be authorised to access the system. Please login again");
			}
			/*
			 * if (suppAccPKID == null) { //return; throw new Exception("Invalid
			 * Supplier Account"); } else Log.printVerbose(strClassName + ":" +
			 * funcName + " - suppAccPKID = " + suppAccPKID); if
			 * (suppCompanyName == null) { //return; throw new
			 * Exception("Invalid Supplier Company Name"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " -
			 * suppCompanyPasswd = " + suppCompanyPasswd); if (suppCompanyPasswd ==
			 * null) { //return; throw new Exception("Invalid Password
			 * entered"); } else Log.printVerbose(strClassName + ":" + funcName + " -
			 * suppCompanyPasswd = " + suppCompanyPasswd); if
			 * (suppCompanyPasswd2 == null) { //return; throw new
			 * Exception("Invalid Password entered"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " -
			 * suppCompanyPasswd2 = " + suppCompanyPasswd2);
			 */
			Log.printVerbose("Adding new SuppCompany");
			java.util.Date ldt = new java.util.Date();
			Timestamp tsCreate = new Timestamp(ldt.getTime());
			Integer usrid = null;
			try
			{
				usrid = lUsr.getUserId();
			} catch (Exception ex)
			{
				Log.printAudit("Company does not exist: " + ex.getMessage());
			}
			// Add the company, ensure it doesn't already exist
			SuppCompany lSuppCompany = SuppCompanyNut.getObjectByCode(suppCompanyCode);
			if (lSuppCompany == null)
			{
				// 1. Validate the input params first
				/*
				 * if((suppCompanyPasswd.compareTo("") != 0) &&
				 * (suppCompanyPasswd2.compareTo("") != 0)) { // Check password
				 * ... TO_DO: more needs to be done here
				 * if(fnCheckPassword(suppCompanyPasswd,suppCompanyPasswd2) ==
				 * 0) { Log.printDebug(strClassName + ":" + funcName + " -
				 * failed password check."); throw new Exception("failed
				 * password check"); } }
				 */
				// 2. Create the SuppCompany object
				Integer iSuppAcc = new Integer(suppAccPKID);
				SuppAccount suppAcc = SuppAccountNut.getHandle(iSuppAcc);
				Integer suppAccId = suppAcc.getPkid();
				SuppCompanyHome suppCompanyHome = SuppCompanyNut.getHome();
				SuppCompany newSuppCompany = suppCompanyHome.create(suppCompanyCode, suppCompanyRegNo, suppCompanyName,
						suppCompanyDesc, suppCompanyAddr1, suppCompanyAddr2, suppCompanyAddr3, suppCompanyZip,
						suppCompanyState, suppCompanyCountryCode, suppCompanyWebUrl, suppAccId, tsCreate, usrid);
				// If we reach this stage, we're successful !
				// populate the "addSuppCompany" attribute so re-display the
				// added fields
				// req.setAttribute("suppAccPKID", suppAccPKID);
				req.setAttribute("addSuppCompany", newSuppCompany);
			} // end if
		} catch (Exception ex)
		{
			Log.printDebug("Add SuppCompany for SuppAccCode = " + suppAccPKID + " -  Failed" + ex.getMessage());
			req.setAttribute("addSuppCompanyErrMsg", ex.getMessage());
		} // end try-catch
	} // end fnAddSuppCompany

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
} // end class DoSuppCompanyAdd
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

public class DoSuppCompanyAdd implements Action
{
	String strClassName = "DoSuppCompanyAdd";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String suppAccPKID = req.getParameter("suppAccPKID");
		String suppCompanyCode = req.getParameter("suppCompanyCode");
		if (suppAccPKID == null)
		{
			Log.printDebug("NULL suppAccPKID !!");
		}
		if (suppCompanyCode != null)
		{
			// add suppCompany
			fnAddSuppCompany(servlet, req, res);
			fnGetParams(servlet, req, res);
			return new ActionRouter("supp-setup-added-company-page");
		}
		fnGetParams(servlet, req, res);
		// propagate suppAccPKID
		// String suppAccPKID = req.getParameter("suppAccPKID");
		// req.setAttribute("suppAccPKID", suppAccPKID);
		return new ActionRouter("supp-setup-add-company-page");
	}

	protected void fnGetParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String suppAccPKID = req.getParameter("suppAccPKID");
		req.setAttribute("suppAccPKID", suppAccPKID);
	}

	protected void fnAddSuppCompany(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAddSuppCompany()";
		// Get the request paramaters
		String suppAccPKID = req.getParameter("suppAccPKID");
		String suppCompanyCode = req.getParameter("suppCompanyName");
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
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		try
		{
			if (lUsr == null)
			{
				Log.printDebug(strClassName + ": " + "WARNING - NULL companyName");
				// return;
				throw new Exception(
						"Invalid Companyname. Your session may have expired or you may not be authorised to access the system. Please login again");
			}
			/*
			 * if (suppAccPKID == null) { //return; throw new Exception("Invalid
			 * Supplier Account"); } else Log.printVerbose(strClassName + ":" +
			 * funcName + " - suppAccPKID = " + suppAccPKID); if
			 * (suppCompanyName == null) { //return; throw new
			 * Exception("Invalid Supplier Company Name"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " -
			 * suppCompanyPasswd = " + suppCompanyPasswd); if (suppCompanyPasswd ==
			 * null) { //return; throw new Exception("Invalid Password
			 * entered"); } else Log.printVerbose(strClassName + ":" + funcName + " -
			 * suppCompanyPasswd = " + suppCompanyPasswd); if
			 * (suppCompanyPasswd2 == null) { //return; throw new
			 * Exception("Invalid Password entered"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " -
			 * suppCompanyPasswd2 = " + suppCompanyPasswd2);
			 */
			Log.printVerbose("Adding new SuppCompany");
			java.util.Date ldt = new java.util.Date();
			Timestamp tsCreate = new Timestamp(ldt.getTime());
			Integer usrid = null;
			try
			{
				usrid = lUsr.getUserId();
			} catch (Exception ex)
			{
				Log.printAudit("Company does not exist: " + ex.getMessage());
			}
			// Add the company, ensure it doesn't already exist
			SuppCompany lSuppCompany = SuppCompanyNut.getObjectByCode(suppCompanyCode);
			if (lSuppCompany == null)
			{
				// 1. Validate the input params first
				/*
				 * if((suppCompanyPasswd.compareTo("") != 0) &&
				 * (suppCompanyPasswd2.compareTo("") != 0)) { // Check password
				 * ... TO_DO: more needs to be done here
				 * if(fnCheckPassword(suppCompanyPasswd,suppCompanyPasswd2) ==
				 * 0) { Log.printDebug(strClassName + ":" + funcName + " -
				 * failed password check."); throw new Exception("failed
				 * password check"); } }
				 */
				// 2. Create the SuppCompany object
				Integer iSuppAcc = new Integer(suppAccPKID);
				SuppAccount suppAcc = SuppAccountNut.getHandle(iSuppAcc);
				Integer suppAccId = suppAcc.getPkid();
				SuppCompanyHome suppCompanyHome = SuppCompanyNut.getHome();
				SuppCompany newSuppCompany = suppCompanyHome.create(suppCompanyCode, suppCompanyRegNo, suppCompanyName,
						suppCompanyDesc, suppCompanyAddr1, suppCompanyAddr2, suppCompanyAddr3, suppCompanyZip,
						suppCompanyState, suppCompanyCountryCode, suppCompanyWebUrl, suppAccId, tsCreate, usrid);
				// If we reach this stage, we're successful !
				// populate the "addSuppCompany" attribute so re-display the
				// added fields
				// req.setAttribute("suppAccPKID", suppAccPKID);
				req.setAttribute("addSuppCompany", newSuppCompany);
			} // end if
		} catch (Exception ex)
		{
			Log.printDebug("Add SuppCompany for SuppAccCode = " + suppAccPKID + " -  Failed" + ex.getMessage());
			req.setAttribute("addSuppCompanyErrMsg", ex.getMessage());
		} // end try-catch
	} // end fnAddSuppCompany

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
} // end class DoSuppCompanyAdd
