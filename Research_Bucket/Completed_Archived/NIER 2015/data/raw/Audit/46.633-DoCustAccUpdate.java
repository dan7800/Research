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

public class DoCustAccUpdate implements Action
{
	String strClassName = "DoCustAccUpdate";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// update custAccount
		fnUpdateCustAccount(servlet, req, res);
		// repopulate custAccountList
		// fnGetCustAccountCodeList(servlet, req, res);
		// return new ActionRouter("inv-setup-edit-custAccount-page");
		return new ActionRouter("cust-redirect-setup-edit-custacc-page");
	}

	protected void fnUpdateCustAccount(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateCustAccount()";
		// Get the request paramaters
		String custAccountPkid = req.getParameter("custAccountPkid");
		String custAccountName = req.getParameter("custAccountName");
		String custAccountDesc = req.getParameter("custAccountDesc");
		String custAccountAccType = req.getParameter("custAccountAccType");
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
		if (custAccountPkid == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - custAccountPkid = " + custAccountPkid);
		if (custAccountName == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - custAccountName = " + custAccountName);
		if (custAccountDesc == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - custAccountDesc = " + custAccountDesc);
		if (custAccountAccType == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - custAccountAccType = " + custAccountAccType);
		if (custAccountPkid != null)
		{
			CustAccount lCustAccount = CustAccountNut.getHandle(new Integer(custAccountPkid));
			if (lCustAccount != null)
			{
				try
				{
					// lCustAccount.setCustAccountCode(custAccountCode);
					lCustAccount.setName(custAccountName);
					lCustAccount.setDescription(custAccountDesc);
					// map custAccountAccType from string back to int
					Integer accType = (Integer) CustAccountNut.mapTypeStrToEnum.get(custAccountAccType);
					lCustAccount.setAccType(accType);
					// and then update the lastModified and userIdUpdate fields
					lCustAccount.setLastUpdate(tsCreate);
					lCustAccount.setUserIdUpdate(usrid);
					// populate the "editCustAccount" attribute so re-display
					// the edited fields
					req.setAttribute("editCustAccount", lCustAccount);
					req.setAttribute("itrAllAccTypeStr", CustAccountNut.getCustTypesStr().iterator());
					Integer custAccId = lCustAccount.getPkid();
					// get CustUser info
					fnGetEditCustUser(servlet, req, res, custAccId);
					// get Company info
					fnGetEditCustCompany(servlet, req, res, custAccId);
					// get Vehicle info
					fnGetEditVehicle(servlet, req, res, custAccId);
				} catch (Exception ex)
				{
					Log.printDebug("Update CustAccount: " + custAccountPkid + " -  Failed" + ex.getMessage());
				} // end try-catch
			} // end if
		} // end if
	} // end fnUpdateCustAccount

	protected void fnGetEditCustUser(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res,
			Integer custAccountId)
	{
		CustAccountUser custAccountUser = CustAccountUserNut.getObjectByCustAccId(custAccountId);
		if (custAccountUser != null)
		{
			try
			{
				Integer custUserId = custAccountUser.getCustUserId();
				CustUser custUser = CustUserNut.getHandle(custUserId);
				req.setAttribute("editCustUser", custUser);
			} catch (Exception ex)
			{
				Log.printDebug("Error in loading editCustUser");
			}
		}
	}

	protected void fnGetEditCustCompany(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res,
			Integer custAccountId)
	{
		CustCompany custCompany = CustCompanyNut.getObjectByCustAccId(custAccountId);
		if (custCompany != null)
		{
			try
			{
				req.setAttribute("editCustCompany", custCompany);
			} catch (Exception ex)
			{
				Log.printDebug("Error in loading editCustCompany");
			}
		}
	}

	protected void fnGetEditVehicle(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res,
			Integer custAccountId)
	{
		// Alex (02/13): No longer true that 1 CustAcc -> 1 Vehicle, it's
		// one-many now ..
		// Vehicle custVehicle = VehicleNut.getObjectByCustAccId(custAccountId);
		Vector vecVehEJB = new Vector(VehicleNut.getCollectionByField(VehicleBean.CUST_ACCOUNT_ID, custAccountId
				.toString()));
		if (vecVehEJB != null)
		{
			try
			{
				req.setAttribute("vecVehEJB", vecVehEJB);
				// req.setAttribute("editCustVehicle", custVehicle);
			} catch (Exception ex)
			{
				Log.printDebug("Error in loading editCustVehicle");
			}
		}
	}
	/*
	 * protected void fnGetCustAccountCodeList(HttpServlet servlet,
	 * HttpServletRequest req, HttpServletResponse res) { Vector
	 * custAccountCodes = new Vector(); Collection colCustAccounts =
	 * CustAccountNut.getAllObjects(); try {
	 * 
	 * for(Iterator itr = colCustAccounts.iterator(); itr.hasNext(); ) {
	 * CustAccount i = (CustAccount) itr.next(); Log.printVerbose("Adding: " +
	 * i.getCustAccountCode()); custAccountCodes.add(i.getCustAccountCode()); }
	 * req.setAttribute("itrCustAccountCode", custAccountCodes.iterator()); }
	 * catch (Exception ex) { Log.printDebug("Error in getting list of
	 * custAccountcodes: " + ex.getMessage()); } }
	 */
} // end class DoInvCustAccountUpdate
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

public class DoCustAccUpdate implements Action
{
	String strClassName = "DoCustAccUpdate";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// update custAccount
		fnUpdateCustAccount(servlet, req, res);
		// repopulate custAccountList
		// fnGetCustAccountCodeList(servlet, req, res);
		// return new ActionRouter("inv-setup-edit-custAccount-page");
		return new ActionRouter("cust-redirect-setup-edit-custacc-page");
	}

	protected void fnUpdateCustAccount(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateCustAccount()";
		// Get the request paramaters
		String custAccountPkid = req.getParameter("custAccountPkid");
		String custAccountName = req.getParameter("custAccountName");
		String custAccountDesc = req.getParameter("custAccountDesc");
		String custAccountAccType = req.getParameter("custAccountAccType");
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
		if (custAccountPkid == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - custAccountPkid = " + custAccountPkid);
		if (custAccountName == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - custAccountName = " + custAccountName);
		if (custAccountDesc == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - custAccountDesc = " + custAccountDesc);
		if (custAccountAccType == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - custAccountAccType = " + custAccountAccType);
		if (custAccountPkid != null)
		{
			CustAccount lCustAccount = CustAccountNut.getHandle(new Integer(custAccountPkid));
			if (lCustAccount != null)
			{
				try
				{
					// lCustAccount.setCustAccountCode(custAccountCode);
					lCustAccount.setName(custAccountName);
					lCustAccount.setDescription(custAccountDesc);
					// map custAccountAccType from string back to int
					Integer accType = (Integer) CustAccountNut.mapTypeStrToEnum.get(custAccountAccType);
					lCustAccount.setAccType(accType);
					// and then update the lastModified and userIdUpdate fields
					lCustAccount.setLastUpdate(tsCreate);
					lCustAccount.setUserIdUpdate(usrid);
					// populate the "editCustAccount" attribute so re-display
					// the edited fields
					req.setAttribute("editCustAccount", lCustAccount);
					req.setAttribute("itrAllAccTypeStr", CustAccountNut.getCustTypesStr().iterator());
					Integer custAccId = lCustAccount.getPkid();
					// get CustUser info
					fnGetEditCustUser(servlet, req, res, custAccId);
					// get Company info
					fnGetEditCustCompany(servlet, req, res, custAccId);
					// get Vehicle info
					fnGetEditVehicle(servlet, req, res, custAccId);
				} catch (Exception ex)
				{
					Log.printDebug("Update CustAccount: " + custAccountPkid + " -  Failed" + ex.getMessage());
				} // end try-catch
			} // end if
		} // end if
	} // end fnUpdateCustAccount

	protected void fnGetEditCustUser(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res,
			Integer custAccountId)
	{
		CustAccountUser custAccountUser = CustAccountUserNut.getObjectByCustAccId(custAccountId);
		if (custAccountUser != null)
		{
			try
			{
				Integer custUserId = custAccountUser.getCustUserId();
				CustUser custUser = CustUserNut.getHandle(custUserId);
				req.setAttribute("editCustUser", custUser);
			} catch (Exception ex)
			{
				Log.printDebug("Error in loading editCustUser");
			}
		}
	}

	protected void fnGetEditCustCompany(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res,
			Integer custAccountId)
	{
		CustCompany custCompany = CustCompanyNut.getObjectByCustAccId(custAccountId);
		if (custCompany != null)
		{
			try
			{
				req.setAttribute("editCustCompany", custCompany);
			} catch (Exception ex)
			{
				Log.printDebug("Error in loading editCustCompany");
			}
		}
	}

	protected void fnGetEditVehicle(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res,
			Integer custAccountId)
	{
		// Alex (02/13): No longer true that 1 CustAcc -> 1 Vehicle, it's
		// one-many now ..
		// Vehicle custVehicle = VehicleNut.getObjectByCustAccId(custAccountId);
		Vector vecVehEJB = new Vector(VehicleNut.getCollectionByField(VehicleBean.CUST_ACCOUNT_ID, custAccountId
				.toString()));
		if (vecVehEJB != null)
		{
			try
			{
				req.setAttribute("vecVehEJB", vecVehEJB);
				// req.setAttribute("editCustVehicle", custVehicle);
			} catch (Exception ex)
			{
				Log.printDebug("Error in loading editCustVehicle");
			}
		}
	}
	/*
	 * protected void fnGetCustAccountCodeList(HttpServlet servlet,
	 * HttpServletRequest req, HttpServletResponse res) { Vector
	 * custAccountCodes = new Vector(); Collection colCustAccounts =
	 * CustAccountNut.getAllObjects(); try {
	 * 
	 * for(Iterator itr = colCustAccounts.iterator(); itr.hasNext(); ) {
	 * CustAccount i = (CustAccount) itr.next(); Log.printVerbose("Adding: " +
	 * i.getCustAccountCode()); custAccountCodes.add(i.getCustAccountCode()); }
	 * req.setAttribute("itrCustAccountCode", custAccountCodes.iterator()); }
	 * catch (Exception ex) { Log.printDebug("Error in getting list of
	 * custAccountcodes: " + ex.getMessage()); } }
	 */
} // end class DoInvCustAccountUpdate
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

public class DoCustAccUpdate implements Action
{
	String strClassName = "DoCustAccUpdate";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// update custAccount
		fnUpdateCustAccount(servlet, req, res);
		// repopulate custAccountList
		// fnGetCustAccountCodeList(servlet, req, res);
		// return new ActionRouter("inv-setup-edit-custAccount-page");
		return new ActionRouter("cust-redirect-setup-edit-custacc-page");
	}

	protected void fnUpdateCustAccount(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateCustAccount()";
		// Get the request paramaters
		String custAccountPkid = req.getParameter("custAccountPkid");
		String custAccountName = req.getParameter("custAccountName");
		String custAccountDesc = req.getParameter("custAccountDesc");
		String custAccountAccType = req.getParameter("custAccountAccType");
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
		if (custAccountPkid == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - custAccountPkid = " + custAccountPkid);
		if (custAccountName == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - custAccountName = " + custAccountName);
		if (custAccountDesc == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - custAccountDesc = " + custAccountDesc);
		if (custAccountAccType == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - custAccountAccType = " + custAccountAccType);
		if (custAccountPkid != null)
		{
			CustAccount lCustAccount = CustAccountNut.getHandle(new Integer(custAccountPkid));
			if (lCustAccount != null)
			{
				try
				{
					// lCustAccount.setCustAccountCode(custAccountCode);
					lCustAccount.setName(custAccountName);
					lCustAccount.setDescription(custAccountDesc);
					// map custAccountAccType from string back to int
					Integer accType = (Integer) CustAccountNut.mapTypeStrToEnum.get(custAccountAccType);
					lCustAccount.setAccType(accType);
					// and then update the lastModified and userIdUpdate fields
					lCustAccount.setLastUpdate(tsCreate);
					lCustAccount.setUserIdUpdate(usrid);
					// populate the "editCustAccount" attribute so re-display
					// the edited fields
					req.setAttribute("editCustAccount", lCustAccount);
					req.setAttribute("itrAllAccTypeStr", CustAccountNut.getCustTypesStr().iterator());
					Integer custAccId = lCustAccount.getPkid();
					// get CustUser info
					fnGetEditCustUser(servlet, req, res, custAccId);
					// get Company info
					fnGetEditCustCompany(servlet, req, res, custAccId);
					// get Vehicle info
					fnGetEditVehicle(servlet, req, res, custAccId);
				} catch (Exception ex)
				{
					Log.printDebug("Update CustAccount: " + custAccountPkid + " -  Failed" + ex.getMessage());
				} // end try-catch
			} // end if
		} // end if
	} // end fnUpdateCustAccount

	protected void fnGetEditCustUser(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res,
			Integer custAccountId)
	{
		CustAccountUser custAccountUser = CustAccountUserNut.getObjectByCustAccId(custAccountId);
		if (custAccountUser != null)
		{
			try
			{
				Integer custUserId = custAccountUser.getCustUserId();
				CustUser custUser = CustUserNut.getHandle(custUserId);
				req.setAttribute("editCustUser", custUser);
			} catch (Exception ex)
			{
				Log.printDebug("Error in loading editCustUser");
			}
		}
	}

	protected void fnGetEditCustCompany(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res,
			Integer custAccountId)
	{
		CustCompany custCompany = CustCompanyNut.getObjectByCustAccId(custAccountId);
		if (custCompany != null)
		{
			try
			{
				req.setAttribute("editCustCompany", custCompany);
			} catch (Exception ex)
			{
				Log.printDebug("Error in loading editCustCompany");
			}
		}
	}

	protected void fnGetEditVehicle(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res,
			Integer custAccountId)
	{
		// Alex (02/13): No longer true that 1 CustAcc -> 1 Vehicle, it's
		// one-many now ..
		// Vehicle custVehicle = VehicleNut.getObjectByCustAccId(custAccountId);
		Vector vecVehEJB = new Vector(VehicleNut.getCollectionByField(VehicleBean.CUST_ACCOUNT_ID, custAccountId
				.toString()));
		if (vecVehEJB != null)
		{
			try
			{
				req.setAttribute("vecVehEJB", vecVehEJB);
				// req.setAttribute("editCustVehicle", custVehicle);
			} catch (Exception ex)
			{
				Log.printDebug("Error in loading editCustVehicle");
			}
		}
	}
	/*
	 * protected void fnGetCustAccountCodeList(HttpServlet servlet,
	 * HttpServletRequest req, HttpServletResponse res) { Vector
	 * custAccountCodes = new Vector(); Collection colCustAccounts =
	 * CustAccountNut.getAllObjects(); try {
	 * 
	 * for(Iterator itr = colCustAccounts.iterator(); itr.hasNext(); ) {
	 * CustAccount i = (CustAccount) itr.next(); Log.printVerbose("Adding: " +
	 * i.getCustAccountCode()); custAccountCodes.add(i.getCustAccountCode()); }
	 * req.setAttribute("itrCustAccountCode", custAccountCodes.iterator()); }
	 * catch (Exception ex) { Log.printDebug("Error in getting list of
	 * custAccountcodes: " + ex.getMessage()); } }
	 */
} // end class DoInvCustAccountUpdate
