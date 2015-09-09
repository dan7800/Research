package com.vlee.servlet.customer;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.text.*;
import java.sql.*;
import com.vlee.servlet.main.*;
import com.vlee.util.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.user.*;

public class DoCustCompanyAdd implements Action
{
	String strClassName = "DoCustCompanyAdd";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String custAccId = req.getParameter("custAccId");
		String custCompanyCode = req.getParameter("custCompanyCode");
		if (custAccId == null)
		{
			Log.printDebug("NULL custAccId !!");
		}
		if (custCompanyCode != null)
		{
			// add custCompany
			fnAddCustCompany(servlet, req, res);
			fnGetParams(servlet, req, res);
			return new ActionRouter("cust-setup-added-company-page");
		}
		fnGetParams(servlet, req, res);
		// propagate custAccId
		// String custAccId = req.getParameter("custAccId");
		// req.setAttribute("custAccId", custAccId);
		return new ActionRouter("cust-setup-add-company-page");
	}

	protected void fnGetParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String custAccId = req.getParameter("custAccId");
		req.setAttribute("custAccId", custAccId);
	}

	protected void fnAddCustCompany(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAddCustCompany()";
		// Get the request paramaters
		String custAccId = req.getParameter("custAccId");
		String custCompanyCode = req.getParameter("custCompanyName");
		String custCompanyRegNo = req.getParameter("custCompanyRegNo");
		String custCompanyName = req.getParameter("custCompanyName");
		String custCompanyDesc = req.getParameter("custCompanyDesc");
		String custCompanyAddr1 = req.getParameter("custCompanyAddr1");
		String custCompanyAddr2 = req.getParameter("custCompanyAddr2");
		String custCompanyAddr3 = req.getParameter("custCompanyAddr3");
		String custCompanyZip = req.getParameter("custCompanyZip");
		String custCompanyState = req.getParameter("custCompanyState");
		String custCompanyCountryCode = req.getParameter("custCompanyCountryCode");
		String custCompanyWebUrl = req.getParameter("custCompanyWebUrl");
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
			 * if (custAccId == null) { //return; throw new Exception("Invalid
			 * Customer Account"); } else Log.printVerbose(strClassName + ":" +
			 * funcName + " - custAccId = " + custAccId); if (custCompanyName ==
			 * null) { //return; throw new Exception("Invalid Customer Company
			 * Name"); } else Log.printVerbose(strClassName + ":" + funcName + " -
			 * custCompanyPasswd = " + custCompanyPasswd); if (custCompanyPasswd ==
			 * null) { //return; throw new Exception("Invalid Password
			 * entered"); } else Log.printVerbose(strClassName + ":" + funcName + " -
			 * custCompanyPasswd = " + custCompanyPasswd); if
			 * (custCompanyPasswd2 == null) { //return; throw new
			 * Exception("Invalid Password entered"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " -
			 * custCompanyPasswd2 = " + custCompanyPasswd2);
			 */
			Log.printVerbose("Adding new CustCompany");
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
			CustCompany lCustCompany = CustCompanyNut.getObjectByCode(custCompanyCode);
			if (lCustCompany == null)
			{
				// 1. Validate the input params first
				/*
				 * if((custCompanyPasswd.compareTo("") != 0) &&
				 * (custCompanyPasswd2.compareTo("") != 0)) { // Check password
				 * ... TO_DO: more needs to be done here
				 * if(fnCheckPassword(custCompanyPasswd,custCompanyPasswd2) ==
				 * 0) { Log.printDebug(strClassName + ":" + funcName + " -
				 * failed password check."); throw new Exception("failed
				 * password check"); } }
				 */
				// 2. Create the CustCompany object
				// CustAccount custAcc =
				// CustAccountNut.getObjectByCode(custAccCode);
				// Integer custAccId = custAcc.getPkid();
				CustCompanyHome custCompanyHome = CustCompanyNut.getHome();
				CustCompany newCustCompany = custCompanyHome.create(custCompanyCode, custCompanyRegNo, custCompanyName,
						custCompanyDesc, custCompanyAddr1, custCompanyAddr2, custCompanyAddr3, custCompanyZip,
						custCompanyState, custCompanyCountryCode, custCompanyWebUrl, new Integer(custAccId), tsCreate,
						usrid);
				// If we reach this stage, we're successful !
				// populate the "addCustCompany" attribute so re-display the
				// added fields
				// req.setAttribute("custAccId", custAccId);
				req.setAttribute("addCustCompany", newCustCompany);
			} // end if
		} catch (Exception ex)
		{
			Log.printDebug("Add CustCompany for CustAccId = " + custAccId + " -  Failed" + ex.getMessage());
			req.setAttribute("addCustCompanyErrMsg", ex.getMessage());
		} // end try-catch
	} // end fnAddCustCompany

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
} // end class DoCustCompanyAdd
package com.vlee.servlet.customer;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.text.*;
import java.sql.*;
import com.vlee.servlet.main.*;
import com.vlee.util.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.user.*;

public class DoCustCompanyAdd implements Action
{
	String strClassName = "DoCustCompanyAdd";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String custAccId = req.getParameter("custAccId");
		String custCompanyCode = req.getParameter("custCompanyCode");
		if (custAccId == null)
		{
			Log.printDebug("NULL custAccId !!");
		}
		if (custCompanyCode != null)
		{
			// add custCompany
			fnAddCustCompany(servlet, req, res);
			fnGetParams(servlet, req, res);
			return new ActionRouter("cust-setup-added-company-page");
		}
		fnGetParams(servlet, req, res);
		// propagate custAccId
		// String custAccId = req.getParameter("custAccId");
		// req.setAttribute("custAccId", custAccId);
		return new ActionRouter("cust-setup-add-company-page");
	}

	protected void fnGetParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String custAccId = req.getParameter("custAccId");
		req.setAttribute("custAccId", custAccId);
	}

	protected void fnAddCustCompany(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAddCustCompany()";
		// Get the request paramaters
		String custAccId = req.getParameter("custAccId");
		String custCompanyCode = req.getParameter("custCompanyName");
		String custCompanyRegNo = req.getParameter("custCompanyRegNo");
		String custCompanyName = req.getParameter("custCompanyName");
		String custCompanyDesc = req.getParameter("custCompanyDesc");
		String custCompanyAddr1 = req.getParameter("custCompanyAddr1");
		String custCompanyAddr2 = req.getParameter("custCompanyAddr2");
		String custCompanyAddr3 = req.getParameter("custCompanyAddr3");
		String custCompanyZip = req.getParameter("custCompanyZip");
		String custCompanyState = req.getParameter("custCompanyState");
		String custCompanyCountryCode = req.getParameter("custCompanyCountryCode");
		String custCompanyWebUrl = req.getParameter("custCompanyWebUrl");
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
			 * if (custAccId == null) { //return; throw new Exception("Invalid
			 * Customer Account"); } else Log.printVerbose(strClassName + ":" +
			 * funcName + " - custAccId = " + custAccId); if (custCompanyName ==
			 * null) { //return; throw new Exception("Invalid Customer Company
			 * Name"); } else Log.printVerbose(strClassName + ":" + funcName + " -
			 * custCompanyPasswd = " + custCompanyPasswd); if (custCompanyPasswd ==
			 * null) { //return; throw new Exception("Invalid Password
			 * entered"); } else Log.printVerbose(strClassName + ":" + funcName + " -
			 * custCompanyPasswd = " + custCompanyPasswd); if
			 * (custCompanyPasswd2 == null) { //return; throw new
			 * Exception("Invalid Password entered"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " -
			 * custCompanyPasswd2 = " + custCompanyPasswd2);
			 */
			Log.printVerbose("Adding new CustCompany");
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
			CustCompany lCustCompany = CustCompanyNut.getObjectByCode(custCompanyCode);
			if (lCustCompany == null)
			{
				// 1. Validate the input params first
				/*
				 * if((custCompanyPasswd.compareTo("") != 0) &&
				 * (custCompanyPasswd2.compareTo("") != 0)) { // Check password
				 * ... TO_DO: more needs to be done here
				 * if(fnCheckPassword(custCompanyPasswd,custCompanyPasswd2) ==
				 * 0) { Log.printDebug(strClassName + ":" + funcName + " -
				 * failed password check."); throw new Exception("failed
				 * password check"); } }
				 */
				// 2. Create the CustCompany object
				// CustAccount custAcc =
				// CustAccountNut.getObjectByCode(custAccCode);
				// Integer custAccId = custAcc.getPkid();
				CustCompanyHome custCompanyHome = CustCompanyNut.getHome();
				CustCompany newCustCompany = custCompanyHome.create(custCompanyCode, custCompanyRegNo, custCompanyName,
						custCompanyDesc, custCompanyAddr1, custCompanyAddr2, custCompanyAddr3, custCompanyZip,
						custCompanyState, custCompanyCountryCode, custCompanyWebUrl, new Integer(custAccId), tsCreate,
						usrid);
				// If we reach this stage, we're successful !
				// populate the "addCustCompany" attribute so re-display the
				// added fields
				// req.setAttribute("custAccId", custAccId);
				req.setAttribute("addCustCompany", newCustCompany);
			} // end if
		} catch (Exception ex)
		{
			Log.printDebug("Add CustCompany for CustAccId = " + custAccId + " -  Failed" + ex.getMessage());
			req.setAttribute("addCustCompanyErrMsg", ex.getMessage());
		} // end try-catch
	} // end fnAddCustCompany

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
} // end class DoCustCompanyAdd
package com.vlee.servlet.customer;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.text.*;
import java.sql.*;
import com.vlee.servlet.main.*;
import com.vlee.util.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.user.*;

public class DoCustCompanyAdd implements Action
{
	String strClassName = "DoCustCompanyAdd";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String custAccId = req.getParameter("custAccId");
		String custCompanyCode = req.getParameter("custCompanyCode");
		if (custAccId == null)
		{
			Log.printDebug("NULL custAccId !!");
		}
		if (custCompanyCode != null)
		{
			// add custCompany
			fnAddCustCompany(servlet, req, res);
			fnGetParams(servlet, req, res);
			return new ActionRouter("cust-setup-added-company-page");
		}
		fnGetParams(servlet, req, res);
		// propagate custAccId
		// String custAccId = req.getParameter("custAccId");
		// req.setAttribute("custAccId", custAccId);
		return new ActionRouter("cust-setup-add-company-page");
	}

	protected void fnGetParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String custAccId = req.getParameter("custAccId");
		req.setAttribute("custAccId", custAccId);
	}

	protected void fnAddCustCompany(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAddCustCompany()";
		// Get the request paramaters
		String custAccId = req.getParameter("custAccId");
		String custCompanyCode = req.getParameter("custCompanyName");
		String custCompanyRegNo = req.getParameter("custCompanyRegNo");
		String custCompanyName = req.getParameter("custCompanyName");
		String custCompanyDesc = req.getParameter("custCompanyDesc");
		String custCompanyAddr1 = req.getParameter("custCompanyAddr1");
		String custCompanyAddr2 = req.getParameter("custCompanyAddr2");
		String custCompanyAddr3 = req.getParameter("custCompanyAddr3");
		String custCompanyZip = req.getParameter("custCompanyZip");
		String custCompanyState = req.getParameter("custCompanyState");
		String custCompanyCountryCode = req.getParameter("custCompanyCountryCode");
		String custCompanyWebUrl = req.getParameter("custCompanyWebUrl");
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
			 * if (custAccId == null) { //return; throw new Exception("Invalid
			 * Customer Account"); } else Log.printVerbose(strClassName + ":" +
			 * funcName + " - custAccId = " + custAccId); if (custCompanyName ==
			 * null) { //return; throw new Exception("Invalid Customer Company
			 * Name"); } else Log.printVerbose(strClassName + ":" + funcName + " -
			 * custCompanyPasswd = " + custCompanyPasswd); if (custCompanyPasswd ==
			 * null) { //return; throw new Exception("Invalid Password
			 * entered"); } else Log.printVerbose(strClassName + ":" + funcName + " -
			 * custCompanyPasswd = " + custCompanyPasswd); if
			 * (custCompanyPasswd2 == null) { //return; throw new
			 * Exception("Invalid Password entered"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " -
			 * custCompanyPasswd2 = " + custCompanyPasswd2);
			 */
			Log.printVerbose("Adding new CustCompany");
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
			CustCompany lCustCompany = CustCompanyNut.getObjectByCode(custCompanyCode);
			if (lCustCompany == null)
			{
				// 1. Validate the input params first
				/*
				 * if((custCompanyPasswd.compareTo("") != 0) &&
				 * (custCompanyPasswd2.compareTo("") != 0)) { // Check password
				 * ... TO_DO: more needs to be done here
				 * if(fnCheckPassword(custCompanyPasswd,custCompanyPasswd2) ==
				 * 0) { Log.printDebug(strClassName + ":" + funcName + " -
				 * failed password check."); throw new Exception("failed
				 * password check"); } }
				 */
				// 2. Create the CustCompany object
				// CustAccount custAcc =
				// CustAccountNut.getObjectByCode(custAccCode);
				// Integer custAccId = custAcc.getPkid();
				CustCompanyHome custCompanyHome = CustCompanyNut.getHome();
				CustCompany newCustCompany = custCompanyHome.create(custCompanyCode, custCompanyRegNo, custCompanyName,
						custCompanyDesc, custCompanyAddr1, custCompanyAddr2, custCompanyAddr3, custCompanyZip,
						custCompanyState, custCompanyCountryCode, custCompanyWebUrl, new Integer(custAccId), tsCreate,
						usrid);
				// If we reach this stage, we're successful !
				// populate the "addCustCompany" attribute so re-display the
				// added fields
				// req.setAttribute("custAccId", custAccId);
				req.setAttribute("addCustCompany", newCustCompany);
			} // end if
		} catch (Exception ex)
		{
			Log.printDebug("Add CustCompany for CustAccId = " + custAccId + " -  Failed" + ex.getMessage());
			req.setAttribute("addCustCompanyErrMsg", ex.getMessage());
		} // end try-catch
	} // end fnAddCustCompany

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
} // end class DoCustCompanyAdd
package com.vlee.servlet.customer;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.text.*;
import java.sql.*;
import com.vlee.servlet.main.*;
import com.vlee.util.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.user.*;

public class DoCustCompanyAdd implements Action
{
	String strClassName = "DoCustCompanyAdd";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String custAccId = req.getParameter("custAccId");
		String custCompanyCode = req.getParameter("custCompanyCode");
		if (custAccId == null)
		{
			Log.printDebug("NULL custAccId !!");
		}
		if (custCompanyCode != null)
		{
			// add custCompany
			fnAddCustCompany(servlet, req, res);
			fnGetParams(servlet, req, res);
			return new ActionRouter("cust-setup-added-company-page");
		}
		fnGetParams(servlet, req, res);
		// propagate custAccId
		// String custAccId = req.getParameter("custAccId");
		// req.setAttribute("custAccId", custAccId);
		return new ActionRouter("cust-setup-add-company-page");
	}

	protected void fnGetParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String custAccId = req.getParameter("custAccId");
		req.setAttribute("custAccId", custAccId);
	}

	protected void fnAddCustCompany(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAddCustCompany()";
		// Get the request paramaters
		String custAccId = req.getParameter("custAccId");
		String custCompanyCode = req.getParameter("custCompanyName");
		String custCompanyRegNo = req.getParameter("custCompanyRegNo");
		String custCompanyName = req.getParameter("custCompanyName");
		String custCompanyDesc = req.getParameter("custCompanyDesc");
		String custCompanyAddr1 = req.getParameter("custCompanyAddr1");
		String custCompanyAddr2 = req.getParameter("custCompanyAddr2");
		String custCompanyAddr3 = req.getParameter("custCompanyAddr3");
		String custCompanyZip = req.getParameter("custCompanyZip");
		String custCompanyState = req.getParameter("custCompanyState");
		String custCompanyCountryCode = req.getParameter("custCompanyCountryCode");
		String custCompanyWebUrl = req.getParameter("custCompanyWebUrl");
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
			 * if (custAccId == null) { //return; throw new Exception("Invalid
			 * Customer Account"); } else Log.printVerbose(strClassName + ":" +
			 * funcName + " - custAccId = " + custAccId); if (custCompanyName ==
			 * null) { //return; throw new Exception("Invalid Customer Company
			 * Name"); } else Log.printVerbose(strClassName + ":" + funcName + " -
			 * custCompanyPasswd = " + custCompanyPasswd); if (custCompanyPasswd ==
			 * null) { //return; throw new Exception("Invalid Password
			 * entered"); } else Log.printVerbose(strClassName + ":" + funcName + " -
			 * custCompanyPasswd = " + custCompanyPasswd); if
			 * (custCompanyPasswd2 == null) { //return; throw new
			 * Exception("Invalid Password entered"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " -
			 * custCompanyPasswd2 = " + custCompanyPasswd2);
			 */
			Log.printVerbose("Adding new CustCompany");
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
			CustCompany lCustCompany = CustCompanyNut.getObjectByCode(custCompanyCode);
			if (lCustCompany == null)
			{
				// 1. Validate the input params first
				/*
				 * if((custCompanyPasswd.compareTo("") != 0) &&
				 * (custCompanyPasswd2.compareTo("") != 0)) { // Check password
				 * ... TO_DO: more needs to be done here
				 * if(fnCheckPassword(custCompanyPasswd,custCompanyPasswd2) ==
				 * 0) { Log.printDebug(strClassName + ":" + funcName + " -
				 * failed password check."); throw new Exception("failed
				 * password check"); } }
				 */
				// 2. Create the CustCompany object
				// CustAccount custAcc =
				// CustAccountNut.getObjectByCode(custAccCode);
				// Integer custAccId = custAcc.getPkid();
				CustCompanyHome custCompanyHome = CustCompanyNut.getHome();
				CustCompany newCustCompany = custCompanyHome.create(custCompanyCode, custCompanyRegNo, custCompanyName,
						custCompanyDesc, custCompanyAddr1, custCompanyAddr2, custCompanyAddr3, custCompanyZip,
						custCompanyState, custCompanyCountryCode, custCompanyWebUrl, new Integer(custAccId), tsCreate,
						usrid);
				// If we reach this stage, we're successful !
				// populate the "addCustCompany" attribute so re-display the
				// added fields
				// req.setAttribute("custAccId", custAccId);
				req.setAttribute("addCustCompany", newCustCompany);
			} // end if
		} catch (Exception ex)
		{
			Log.printDebug("Add CustCompany for CustAccId = " + custAccId + " -  Failed" + ex.getMessage());
			req.setAttribute("addCustCompanyErrMsg", ex.getMessage());
		} // end try-catch
	} // end fnAddCustCompany

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
} // end class DoCustCompanyAdd
