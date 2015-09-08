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

public class DoCustCompanyUpdate implements Action
{
	String strClassName = "DoCustCompanyUpdate";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// update custCompany
		fnUpdateCustCompany(servlet, req, res);
		// propagate custAccId
		String custAccId = req.getParameter("custAccId");
		req.setAttribute("custAccId", custAccId);
		return new ActionRouter("cust-setup-updated-company-page");
	}

	protected void fnUpdateCustCompany(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateCustCompany()";
		// Get the request paramaters
		String custAccId = req.getParameter("custAccId");
		String custCompanyPkid = req.getParameter("custCompanyPkid");
		String custCompanyCode = req.getParameter("custCompanyCode");
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
		 * if (custAccId == null) return; else Log.printVerbose(strClassName +
		 * ":" + funcName + " - custAccId = " + custAccId); if (custUserPkid ==
		 * null) return; else Log.printVerbose(strClassName + ":" + funcName + " -
		 * custUserPkid = " + custUserPkid); if (custUserName == null) return;
		 * else Log.printVerbose(strClassName + ":" + funcName + " -
		 * custUserNewPasswd = " + custUserNewPasswd); if (custUserNewPasswd ==
		 * null) return; else Log.printVerbose(strClassName + ":" + funcName + " -
		 * custUserNewPasswd = " + custUserNewPasswd); if (custUserNewPasswd2 ==
		 * null) return; else Log.printVerbose(strClassName + ":" + funcName + " -
		 * custUserNewPasswd2 = " + custUserNewPasswd2);
		 */
		try
		{
			// Get the custAccId first
			// CustAccount custAcc =
			// CustAccountNut.getObjectByCode(custAccCode);
			// Integer custAccId = custAcc.getPkid();
			CustCompany lCustCompany = CustCompanyNut.getHandle(new Integer(custCompanyPkid));
			if (lCustCompany != null)
			{
				lCustCompany.setCode(custCompanyCode);
				lCustCompany.setRegNo(custCompanyRegNo);
				lCustCompany.setName(custCompanyName);
				lCustCompany.setDescription(custCompanyDesc);
				lCustCompany.setAddr1(custCompanyAddr1);
				lCustCompany.setAddr2(custCompanyAddr2);
				lCustCompany.setAddr3(custCompanyAddr3);
				lCustCompany.setZip(custCompanyZip);
				lCustCompany.setState(custCompanyState);
				lCustCompany.setCountryCode(custCompanyCountryCode);
				lCustCompany.setWebUrl(custCompanyWebUrl);
				// and then update the lastModified and userIdUpdate fields
				lCustCompany.setLastUpdate(tsCreate);
				lCustCompany.setUserIdUpdate(usrid);
				// populate the "editCustCompany" attribute so re-display the
				// edited fields
				// req.setAttribute("custAccId", custAccId);
				req.setAttribute("editCustCompany", lCustCompany);
			} // end if
		} catch (Exception ex)
		{
			Log.printDebug("Update CustCompany for CustAccId = " + custAccId + " -  Failed" + ex.getMessage());
		} // end try-catch
	} // end fnUpdateCustCompany

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
} // end class DoCustCompanyUpdate
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

public class DoCustCompanyUpdate implements Action
{
	String strClassName = "DoCustCompanyUpdate";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// update custCompany
		fnUpdateCustCompany(servlet, req, res);
		// propagate custAccId
		String custAccId = req.getParameter("custAccId");
		req.setAttribute("custAccId", custAccId);
		return new ActionRouter("cust-setup-updated-company-page");
	}

	protected void fnUpdateCustCompany(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateCustCompany()";
		// Get the request paramaters
		String custAccId = req.getParameter("custAccId");
		String custCompanyPkid = req.getParameter("custCompanyPkid");
		String custCompanyCode = req.getParameter("custCompanyCode");
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
		 * if (custAccId == null) return; else Log.printVerbose(strClassName +
		 * ":" + funcName + " - custAccId = " + custAccId); if (custUserPkid ==
		 * null) return; else Log.printVerbose(strClassName + ":" + funcName + " -
		 * custUserPkid = " + custUserPkid); if (custUserName == null) return;
		 * else Log.printVerbose(strClassName + ":" + funcName + " -
		 * custUserNewPasswd = " + custUserNewPasswd); if (custUserNewPasswd ==
		 * null) return; else Log.printVerbose(strClassName + ":" + funcName + " -
		 * custUserNewPasswd = " + custUserNewPasswd); if (custUserNewPasswd2 ==
		 * null) return; else Log.printVerbose(strClassName + ":" + funcName + " -
		 * custUserNewPasswd2 = " + custUserNewPasswd2);
		 */
		try
		{
			// Get the custAccId first
			// CustAccount custAcc =
			// CustAccountNut.getObjectByCode(custAccCode);
			// Integer custAccId = custAcc.getPkid();
			CustCompany lCustCompany = CustCompanyNut.getHandle(new Integer(custCompanyPkid));
			if (lCustCompany != null)
			{
				lCustCompany.setCode(custCompanyCode);
				lCustCompany.setRegNo(custCompanyRegNo);
				lCustCompany.setName(custCompanyName);
				lCustCompany.setDescription(custCompanyDesc);
				lCustCompany.setAddr1(custCompanyAddr1);
				lCustCompany.setAddr2(custCompanyAddr2);
				lCustCompany.setAddr3(custCompanyAddr3);
				lCustCompany.setZip(custCompanyZip);
				lCustCompany.setState(custCompanyState);
				lCustCompany.setCountryCode(custCompanyCountryCode);
				lCustCompany.setWebUrl(custCompanyWebUrl);
				// and then update the lastModified and userIdUpdate fields
				lCustCompany.setLastUpdate(tsCreate);
				lCustCompany.setUserIdUpdate(usrid);
				// populate the "editCustCompany" attribute so re-display the
				// edited fields
				// req.setAttribute("custAccId", custAccId);
				req.setAttribute("editCustCompany", lCustCompany);
			} // end if
		} catch (Exception ex)
		{
			Log.printDebug("Update CustCompany for CustAccId = " + custAccId + " -  Failed" + ex.getMessage());
		} // end try-catch
	} // end fnUpdateCustCompany

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
} // end class DoCustCompanyUpdate
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

public class DoCustCompanyUpdate implements Action
{
	String strClassName = "DoCustCompanyUpdate";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// update custCompany
		fnUpdateCustCompany(servlet, req, res);
		// propagate custAccId
		String custAccId = req.getParameter("custAccId");
		req.setAttribute("custAccId", custAccId);
		return new ActionRouter("cust-setup-updated-company-page");
	}

	protected void fnUpdateCustCompany(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateCustCompany()";
		// Get the request paramaters
		String custAccId = req.getParameter("custAccId");
		String custCompanyPkid = req.getParameter("custCompanyPkid");
		String custCompanyCode = req.getParameter("custCompanyCode");
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
		 * if (custAccId == null) return; else Log.printVerbose(strClassName +
		 * ":" + funcName + " - custAccId = " + custAccId); if (custUserPkid ==
		 * null) return; else Log.printVerbose(strClassName + ":" + funcName + " -
		 * custUserPkid = " + custUserPkid); if (custUserName == null) return;
		 * else Log.printVerbose(strClassName + ":" + funcName + " -
		 * custUserNewPasswd = " + custUserNewPasswd); if (custUserNewPasswd ==
		 * null) return; else Log.printVerbose(strClassName + ":" + funcName + " -
		 * custUserNewPasswd = " + custUserNewPasswd); if (custUserNewPasswd2 ==
		 * null) return; else Log.printVerbose(strClassName + ":" + funcName + " -
		 * custUserNewPasswd2 = " + custUserNewPasswd2);
		 */
		try
		{
			// Get the custAccId first
			// CustAccount custAcc =
			// CustAccountNut.getObjectByCode(custAccCode);
			// Integer custAccId = custAcc.getPkid();
			CustCompany lCustCompany = CustCompanyNut.getHandle(new Integer(custCompanyPkid));
			if (lCustCompany != null)
			{
				lCustCompany.setCode(custCompanyCode);
				lCustCompany.setRegNo(custCompanyRegNo);
				lCustCompany.setName(custCompanyName);
				lCustCompany.setDescription(custCompanyDesc);
				lCustCompany.setAddr1(custCompanyAddr1);
				lCustCompany.setAddr2(custCompanyAddr2);
				lCustCompany.setAddr3(custCompanyAddr3);
				lCustCompany.setZip(custCompanyZip);
				lCustCompany.setState(custCompanyState);
				lCustCompany.setCountryCode(custCompanyCountryCode);
				lCustCompany.setWebUrl(custCompanyWebUrl);
				// and then update the lastModified and userIdUpdate fields
				lCustCompany.setLastUpdate(tsCreate);
				lCustCompany.setUserIdUpdate(usrid);
				// populate the "editCustCompany" attribute so re-display the
				// edited fields
				// req.setAttribute("custAccId", custAccId);
				req.setAttribute("editCustCompany", lCustCompany);
			} // end if
		} catch (Exception ex)
		{
			Log.printDebug("Update CustCompany for CustAccId = " + custAccId + " -  Failed" + ex.getMessage());
		} // end try-catch
	} // end fnUpdateCustCompany

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
} // end class DoCustCompanyUpdate
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

public class DoCustCompanyUpdate implements Action
{
	String strClassName = "DoCustCompanyUpdate";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// update custCompany
		fnUpdateCustCompany(servlet, req, res);
		// propagate custAccId
		String custAccId = req.getParameter("custAccId");
		req.setAttribute("custAccId", custAccId);
		return new ActionRouter("cust-setup-updated-company-page");
	}

	protected void fnUpdateCustCompany(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateCustCompany()";
		// Get the request paramaters
		String custAccId = req.getParameter("custAccId");
		String custCompanyPkid = req.getParameter("custCompanyPkid");
		String custCompanyCode = req.getParameter("custCompanyCode");
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
		 * if (custAccId == null) return; else Log.printVerbose(strClassName +
		 * ":" + funcName + " - custAccId = " + custAccId); if (custUserPkid ==
		 * null) return; else Log.printVerbose(strClassName + ":" + funcName + " -
		 * custUserPkid = " + custUserPkid); if (custUserName == null) return;
		 * else Log.printVerbose(strClassName + ":" + funcName + " -
		 * custUserNewPasswd = " + custUserNewPasswd); if (custUserNewPasswd ==
		 * null) return; else Log.printVerbose(strClassName + ":" + funcName + " -
		 * custUserNewPasswd = " + custUserNewPasswd); if (custUserNewPasswd2 ==
		 * null) return; else Log.printVerbose(strClassName + ":" + funcName + " -
		 * custUserNewPasswd2 = " + custUserNewPasswd2);
		 */
		try
		{
			// Get the custAccId first
			// CustAccount custAcc =
			// CustAccountNut.getObjectByCode(custAccCode);
			// Integer custAccId = custAcc.getPkid();
			CustCompany lCustCompany = CustCompanyNut.getHandle(new Integer(custCompanyPkid));
			if (lCustCompany != null)
			{
				lCustCompany.setCode(custCompanyCode);
				lCustCompany.setRegNo(custCompanyRegNo);
				lCustCompany.setName(custCompanyName);
				lCustCompany.setDescription(custCompanyDesc);
				lCustCompany.setAddr1(custCompanyAddr1);
				lCustCompany.setAddr2(custCompanyAddr2);
				lCustCompany.setAddr3(custCompanyAddr3);
				lCustCompany.setZip(custCompanyZip);
				lCustCompany.setState(custCompanyState);
				lCustCompany.setCountryCode(custCompanyCountryCode);
				lCustCompany.setWebUrl(custCompanyWebUrl);
				// and then update the lastModified and userIdUpdate fields
				lCustCompany.setLastUpdate(tsCreate);
				lCustCompany.setUserIdUpdate(usrid);
				// populate the "editCustCompany" attribute so re-display the
				// edited fields
				// req.setAttribute("custAccId", custAccId);
				req.setAttribute("editCustCompany", lCustCompany);
			} // end if
		} catch (Exception ex)
		{
			Log.printDebug("Update CustCompany for CustAccId = " + custAccId + " -  Failed" + ex.getMessage());
		} // end try-catch
	} // end fnUpdateCustCompany

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
} // end class DoCustCompanyUpdate
