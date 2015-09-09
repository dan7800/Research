package com.vlee.servlet.customer;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;

public class DoCustAccAddRem implements Action
{
	private String strClassName = "DoCustAccAddRem";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			Log.printVerbose(strClassName + ": formName = null");
			fnGetCustTypes(servlet, req, res);
			// fnGetInitialParams(servlet, req, res);
			return new ActionRouter("cust-setup-addrem-custacc-page");
		}
		if (formName.equals("printableList"))
		{
			Log.printVerbose(strClassName + ": formName = printableList");
			fnGetInitialParams(servlet, req, res);
			return new ActionRouter("cust-popup-printable-customer-list-page");
		}
		if (formName.equals("addCustAcc"))
		{
			Log.printVerbose(strClassName + ": formName = addCustAcc");
			fnAdd(servlet, req, res);
		}
		if (formName.equals("deactCustAcc"))
		{
			Log.printVerbose(strClassName + ": formName = deactCustAcc");
			fnDeAct(servlet, req, res);
		}
		if (formName.equals("activateCustAcc"))
		{
			Log.printVerbose(strClassName + ": formName = activateCustAcc");
			fnActivate(servlet, req, res);
		}
		fnGetCustTypes(servlet, req, res);
		// fnGetInitialParams(servlet, req, res);
		return new ActionRouter("cust-setup-addrem-custacc-page");
	}

	protected void fnRemove(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String rmCustAccountCode = (String) req.getParameter("removeCustAccount");
		if (rmCustAccountCode != null)
		{
			CustAccount lCustAccountCode = CustAccountNut.getObjectByCode(rmCustAccountCode);
			if (lCustAccountCode != null)
			{
				try
				{
					lCustAccountCode.remove();
				} catch (Exception ex)
				{
					Log.printDebug("Remove CustAccount Failed" + ex.getMessage());
				} // end try-catch
			} // end if
		} // end if
	} // end fnRmCustAccount

	protected void fnGetCustTypes(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetCustTypes()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		// 2. itrCustType
		Collection colCustType = CustAccountNut.getCustTypesStr();
		Iterator itrCustType = colCustType.iterator();
		Log.printVerbose("Setting attribute itrCustType now");
		req.setAttribute("itrCustType", itrCustType);
	}

	protected void fnGetInitialParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetInitialParams()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		// Need the following :
		// 1. "custCode" - the new customer code
		// 2. "itrCustType" - iterator of all customer types
		// 3. "itrActiveCustAccount" - for the display of all active customers
		// 4. "itrPassiveCustAccount" - for the display of all active customers
		/*
		 * // 1. custCode String custCode = CustAccountNut.getNextPkid(); //
		 * make code = pkid Log.printVerbose("Setting attribute \"custCode\"
		 * now"); req.setAttribute("custCode", custCode);
		 */
		// 2. itrCustType
		// fnGetCustTypes(servlet,req,res);
		// 3. itrActiveCustAccount
		Collection colActiveCustAccount = CustAccountNut.getActiveObjects();
		Iterator itrActiveCustAccount = colActiveCustAccount.iterator();
		Log.printVerbose("Setting attribute itrActiveCustAccount now");
		req.setAttribute("itrActiveCustAccount", itrActiveCustAccount);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
		// 4. itrPassivaCustAccount
		Collection colInactiveCustAccount = CustAccountNut.getInactiveObjects();
		Iterator itrInactiveCustAccount = colInactiveCustAccount.iterator();
		Log.printVerbose("Setting attribute itrPassiveCustAccount now");
		req.setAttribute("itrPassiveCustAccount", itrInactiveCustAccount);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
	}

	protected void fnAdd(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAdd()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		// String custCode = (String) req.getParameter("custCode");
		String custName = (String) req.getParameter("custName");
		String custDesc = (String) req.getParameter("custDescription");
		String custType = (String) req.getParameter("custType");
		// Print results obtained from getParameter()
		// String params = "CustAccountCode = " + custCode + "\n";
		String params = "CustAccountName = " + custName + "\n";
		params += "CustAccountDesc = " + custDesc + "\n";
		params += "CustAccountType = " + custType;
		Log.printVerbose(strClassName + ": Params = \n" + params);
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		if (lUsr == null)
			Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
		// if(custCode == null)
		// return ;
		if (custName == null)
			return;
		if (custDesc == null)
			return;
		if (custType == null)
			return;
		// Convert custType from String to Enum
		Integer enumCustType = (Integer) CustAccountNut.mapTypeStrToEnum.get(custType);
		// Ensure CustAccount does not exist
		// CustAccount lCustAccount = CustAccountNut.getObjectByCode(custCode);
		if (lUsr != null)
		{
			Log.printVerbose("Adding new CustAccount");
			CustAccountHome lCustAccountH = CustAccountNut.getHome();
			java.util.Date ldt = new java.util.Date();
			Timestamp tsCreate = new Timestamp(ldt.getTime());
			Long lCode = new Long(ldt.getTime());
			Integer usrid = null;
			try
			{
				usrid = lUsr.getUserId();
			} catch (Exception ex)
			{
				Log.printAudit("User does not exist: " + ex.getMessage());
			}
			// Create the account here
			CustAccount custAcc = null;
			try
			{
				custAcc = lCustAccountH.create(lCode.toString(), custName, custDesc, enumCustType, tsCreate, usrid);
				if (custAcc == null)
				{
					String errMsg = "Failed to create Customer Account";
					req.setAttribute("errMsg", errMsg);
				} else
				{
					// Derive the custType in String
					String strCustType = (String) CustAccountNut.mapTypeEnumToStr.get(custAcc.getAccType());
					req.setAttribute("custId", custAcc.getPkid());
					req.setAttribute("custName", custAcc.getName());
					req.setAttribute("custDesc", custAcc.getDescription());
					req.setAttribute("custType", strCustType);
				}
			} catch (Exception ex)
			{
				String errMsg = "Failed to create Customer Account: " + ex.getMessage();
				Log.printDebug(errMsg);
				req.setAttribute("errMsg", errMsg);
			}
		}
		// fnGetInitialParams(servlet, req, res);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
	}

	protected void fnDeAct(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnDeAct()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		Integer custPkid = new Integer(req.getParameter("deactCustAccount"));
		if (custPkid != null)
		{
			if (!CustAccountNut.deactCustAccount(custPkid))
			{
				Log.printDebug("Failed to deactivate customer: " + custPkid);
			} else
			{
				Log.printVerbose("Successfully deactivated customer: " + custPkid);
			}
		}
	}

	protected void fnActivate(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnActivate()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		Integer custPkid = new Integer(req.getParameter("activateCustAccount"));
		if (custPkid != null)
		{
			if (!CustAccountNut.activateCustAccount(custPkid))
			{
				Log.printDebug("Failed to activate customer: " + custPkid);
			} else
			{
				Log.printVerbose("Successfully activated customer: " + custPkid);
			}
		}
	}
}
package com.vlee.servlet.customer;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;

public class DoCustAccAddRem implements Action
{
	private String strClassName = "DoCustAccAddRem";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			Log.printVerbose(strClassName + ": formName = null");
			fnGetCustTypes(servlet, req, res);
			// fnGetInitialParams(servlet, req, res);
			return new ActionRouter("cust-setup-addrem-custacc-page");
		}
		if (formName.equals("printableList"))
		{
			Log.printVerbose(strClassName + ": formName = printableList");
			fnGetInitialParams(servlet, req, res);
			return new ActionRouter("cust-popup-printable-customer-list-page");
		}
		if (formName.equals("addCustAcc"))
		{
			Log.printVerbose(strClassName + ": formName = addCustAcc");
			fnAdd(servlet, req, res);
		}
		if (formName.equals("deactCustAcc"))
		{
			Log.printVerbose(strClassName + ": formName = deactCustAcc");
			fnDeAct(servlet, req, res);
		}
		if (formName.equals("activateCustAcc"))
		{
			Log.printVerbose(strClassName + ": formName = activateCustAcc");
			fnActivate(servlet, req, res);
		}
		fnGetCustTypes(servlet, req, res);
		// fnGetInitialParams(servlet, req, res);
		return new ActionRouter("cust-setup-addrem-custacc-page");
	}

	protected void fnRemove(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String rmCustAccountCode = (String) req.getParameter("removeCustAccount");
		if (rmCustAccountCode != null)
		{
			CustAccount lCustAccountCode = CustAccountNut.getObjectByCode(rmCustAccountCode);
			if (lCustAccountCode != null)
			{
				try
				{
					lCustAccountCode.remove();
				} catch (Exception ex)
				{
					Log.printDebug("Remove CustAccount Failed" + ex.getMessage());
				} // end try-catch
			} // end if
		} // end if
	} // end fnRmCustAccount

	protected void fnGetCustTypes(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetCustTypes()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		// 2. itrCustType
		Collection colCustType = CustAccountNut.getCustTypesStr();
		Iterator itrCustType = colCustType.iterator();
		Log.printVerbose("Setting attribute itrCustType now");
		req.setAttribute("itrCustType", itrCustType);
	}

	protected void fnGetInitialParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetInitialParams()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		// Need the following :
		// 1. "custCode" - the new customer code
		// 2. "itrCustType" - iterator of all customer types
		// 3. "itrActiveCustAccount" - for the display of all active customers
		// 4. "itrPassiveCustAccount" - for the display of all active customers
		/*
		 * // 1. custCode String custCode = CustAccountNut.getNextPkid(); //
		 * make code = pkid Log.printVerbose("Setting attribute \"custCode\"
		 * now"); req.setAttribute("custCode", custCode);
		 */
		// 2. itrCustType
		// fnGetCustTypes(servlet,req,res);
		// 3. itrActiveCustAccount
		Collection colActiveCustAccount = CustAccountNut.getActiveObjects();
		Iterator itrActiveCustAccount = colActiveCustAccount.iterator();
		Log.printVerbose("Setting attribute itrActiveCustAccount now");
		req.setAttribute("itrActiveCustAccount", itrActiveCustAccount);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
		// 4. itrPassivaCustAccount
		Collection colInactiveCustAccount = CustAccountNut.getInactiveObjects();
		Iterator itrInactiveCustAccount = colInactiveCustAccount.iterator();
		Log.printVerbose("Setting attribute itrPassiveCustAccount now");
		req.setAttribute("itrPassiveCustAccount", itrInactiveCustAccount);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
	}

	protected void fnAdd(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAdd()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		// String custCode = (String) req.getParameter("custCode");
		String custName = (String) req.getParameter("custName");
		String custDesc = (String) req.getParameter("custDescription");
		String custType = (String) req.getParameter("custType");
		// Print results obtained from getParameter()
		// String params = "CustAccountCode = " + custCode + "\n";
		String params = "CustAccountName = " + custName + "\n";
		params += "CustAccountDesc = " + custDesc + "\n";
		params += "CustAccountType = " + custType;
		Log.printVerbose(strClassName + ": Params = \n" + params);
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		if (lUsr == null)
			Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
		// if(custCode == null)
		// return ;
		if (custName == null)
			return;
		if (custDesc == null)
			return;
		if (custType == null)
			return;
		// Convert custType from String to Enum
		Integer enumCustType = (Integer) CustAccountNut.mapTypeStrToEnum.get(custType);
		// Ensure CustAccount does not exist
		// CustAccount lCustAccount = CustAccountNut.getObjectByCode(custCode);
		if (lUsr != null)
		{
			Log.printVerbose("Adding new CustAccount");
			CustAccountHome lCustAccountH = CustAccountNut.getHome();
			java.util.Date ldt = new java.util.Date();
			Timestamp tsCreate = new Timestamp(ldt.getTime());
			Long lCode = new Long(ldt.getTime());
			Integer usrid = null;
			try
			{
				usrid = lUsr.getUserId();
			} catch (Exception ex)
			{
				Log.printAudit("User does not exist: " + ex.getMessage());
			}
			// Create the account here
			CustAccount custAcc = null;
			try
			{
				custAcc = lCustAccountH.create(lCode.toString(), custName, custDesc, enumCustType, tsCreate, usrid);
				if (custAcc == null)
				{
					String errMsg = "Failed to create Customer Account";
					req.setAttribute("errMsg", errMsg);
				} else
				{
					// Derive the custType in String
					String strCustType = (String) CustAccountNut.mapTypeEnumToStr.get(custAcc.getAccType());
					req.setAttribute("custId", custAcc.getPkid());
					req.setAttribute("custName", custAcc.getName());
					req.setAttribute("custDesc", custAcc.getDescription());
					req.setAttribute("custType", strCustType);
				}
			} catch (Exception ex)
			{
				String errMsg = "Failed to create Customer Account: " + ex.getMessage();
				Log.printDebug(errMsg);
				req.setAttribute("errMsg", errMsg);
			}
		}
		// fnGetInitialParams(servlet, req, res);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
	}

	protected void fnDeAct(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnDeAct()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		Integer custPkid = new Integer(req.getParameter("deactCustAccount"));
		if (custPkid != null)
		{
			if (!CustAccountNut.deactCustAccount(custPkid))
			{
				Log.printDebug("Failed to deactivate customer: " + custPkid);
			} else
			{
				Log.printVerbose("Successfully deactivated customer: " + custPkid);
			}
		}
	}

	protected void fnActivate(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnActivate()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		Integer custPkid = new Integer(req.getParameter("activateCustAccount"));
		if (custPkid != null)
		{
			if (!CustAccountNut.activateCustAccount(custPkid))
			{
				Log.printDebug("Failed to activate customer: " + custPkid);
			} else
			{
				Log.printVerbose("Successfully activated customer: " + custPkid);
			}
		}
	}
}
package com.vlee.servlet.customer;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;

public class DoCustAccAddRem implements Action
{
	private String strClassName = "DoCustAccAddRem";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			Log.printVerbose(strClassName + ": formName = null");
			fnGetCustTypes(servlet, req, res);
			// fnGetInitialParams(servlet, req, res);
			return new ActionRouter("cust-setup-addrem-custacc-page");
		}
		if (formName.equals("printableList"))
		{
			Log.printVerbose(strClassName + ": formName = printableList");
			fnGetInitialParams(servlet, req, res);
			return new ActionRouter("cust-popup-printable-customer-list-page");
		}
		if (formName.equals("addCustAcc"))
		{
			Log.printVerbose(strClassName + ": formName = addCustAcc");
			fnAdd(servlet, req, res);
		}
		if (formName.equals("deactCustAcc"))
		{
			Log.printVerbose(strClassName + ": formName = deactCustAcc");
			fnDeAct(servlet, req, res);
		}
		if (formName.equals("activateCustAcc"))
		{
			Log.printVerbose(strClassName + ": formName = activateCustAcc");
			fnActivate(servlet, req, res);
		}
		fnGetCustTypes(servlet, req, res);
		// fnGetInitialParams(servlet, req, res);
		return new ActionRouter("cust-setup-addrem-custacc-page");
	}

	protected void fnRemove(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String rmCustAccountCode = (String) req.getParameter("removeCustAccount");
		if (rmCustAccountCode != null)
		{
			CustAccount lCustAccountCode = CustAccountNut.getObjectByCode(rmCustAccountCode);
			if (lCustAccountCode != null)
			{
				try
				{
					lCustAccountCode.remove();
				} catch (Exception ex)
				{
					Log.printDebug("Remove CustAccount Failed" + ex.getMessage());
				} // end try-catch
			} // end if
		} // end if
	} // end fnRmCustAccount

	protected void fnGetCustTypes(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetCustTypes()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		// 2. itrCustType
		Collection colCustType = CustAccountNut.getCustTypesStr();
		Iterator itrCustType = colCustType.iterator();
		Log.printVerbose("Setting attribute itrCustType now");
		req.setAttribute("itrCustType", itrCustType);
	}

	protected void fnGetInitialParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetInitialParams()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		// Need the following :
		// 1. "custCode" - the new customer code
		// 2. "itrCustType" - iterator of all customer types
		// 3. "itrActiveCustAccount" - for the display of all active customers
		// 4. "itrPassiveCustAccount" - for the display of all active customers
		/*
		 * // 1. custCode String custCode = CustAccountNut.getNextPkid(); //
		 * make code = pkid Log.printVerbose("Setting attribute \"custCode\"
		 * now"); req.setAttribute("custCode", custCode);
		 */
		// 2. itrCustType
		// fnGetCustTypes(servlet,req,res);
		// 3. itrActiveCustAccount
		Collection colActiveCustAccount = CustAccountNut.getActiveObjects();
		Iterator itrActiveCustAccount = colActiveCustAccount.iterator();
		Log.printVerbose("Setting attribute itrActiveCustAccount now");
		req.setAttribute("itrActiveCustAccount", itrActiveCustAccount);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
		// 4. itrPassivaCustAccount
		Collection colInactiveCustAccount = CustAccountNut.getInactiveObjects();
		Iterator itrInactiveCustAccount = colInactiveCustAccount.iterator();
		Log.printVerbose("Setting attribute itrPassiveCustAccount now");
		req.setAttribute("itrPassiveCustAccount", itrInactiveCustAccount);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
	}

	protected void fnAdd(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAdd()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		// String custCode = (String) req.getParameter("custCode");
		String custName = (String) req.getParameter("custName");
		String custDesc = (String) req.getParameter("custDescription");
		String custType = (String) req.getParameter("custType");
		// Print results obtained from getParameter()
		// String params = "CustAccountCode = " + custCode + "\n";
		String params = "CustAccountName = " + custName + "\n";
		params += "CustAccountDesc = " + custDesc + "\n";
		params += "CustAccountType = " + custType;
		Log.printVerbose(strClassName + ": Params = \n" + params);
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		if (lUsr == null)
			Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
		// if(custCode == null)
		// return ;
		if (custName == null)
			return;
		if (custDesc == null)
			return;
		if (custType == null)
			return;
		// Convert custType from String to Enum
		Integer enumCustType = (Integer) CustAccountNut.mapTypeStrToEnum.get(custType);
		// Ensure CustAccount does not exist
		// CustAccount lCustAccount = CustAccountNut.getObjectByCode(custCode);
		if (lUsr != null)
		{
			Log.printVerbose("Adding new CustAccount");
			CustAccountHome lCustAccountH = CustAccountNut.getHome();
			java.util.Date ldt = new java.util.Date();
			Timestamp tsCreate = new Timestamp(ldt.getTime());
			Long lCode = new Long(ldt.getTime());
			Integer usrid = null;
			try
			{
				usrid = lUsr.getUserId();
			} catch (Exception ex)
			{
				Log.printAudit("User does not exist: " + ex.getMessage());
			}
			// Create the account here
			CustAccount custAcc = null;
			try
			{
				custAcc = lCustAccountH.create(lCode.toString(), custName, custDesc, enumCustType, tsCreate, usrid);
				if (custAcc == null)
				{
					String errMsg = "Failed to create Customer Account";
					req.setAttribute("errMsg", errMsg);
				} else
				{
					// Derive the custType in String
					String strCustType = (String) CustAccountNut.mapTypeEnumToStr.get(custAcc.getAccType());
					req.setAttribute("custId", custAcc.getPkid());
					req.setAttribute("custName", custAcc.getName());
					req.setAttribute("custDesc", custAcc.getDescription());
					req.setAttribute("custType", strCustType);
				}
			} catch (Exception ex)
			{
				String errMsg = "Failed to create Customer Account: " + ex.getMessage();
				Log.printDebug(errMsg);
				req.setAttribute("errMsg", errMsg);
			}
		}
		// fnGetInitialParams(servlet, req, res);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
	}

	protected void fnDeAct(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnDeAct()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		Integer custPkid = new Integer(req.getParameter("deactCustAccount"));
		if (custPkid != null)
		{
			if (!CustAccountNut.deactCustAccount(custPkid))
			{
				Log.printDebug("Failed to deactivate customer: " + custPkid);
			} else
			{
				Log.printVerbose("Successfully deactivated customer: " + custPkid);
			}
		}
	}

	protected void fnActivate(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnActivate()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		Integer custPkid = new Integer(req.getParameter("activateCustAccount"));
		if (custPkid != null)
		{
			if (!CustAccountNut.activateCustAccount(custPkid))
			{
				Log.printDebug("Failed to activate customer: " + custPkid);
			} else
			{
				Log.printVerbose("Successfully activated customer: " + custPkid);
			}
		}
	}
}
package com.vlee.servlet.customer;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;

public class DoCustAccAddRem implements Action
{
	private String strClassName = "DoCustAccAddRem";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			Log.printVerbose(strClassName + ": formName = null");
			fnGetCustTypes(servlet, req, res);
			// fnGetInitialParams(servlet, req, res);
			return new ActionRouter("cust-setup-addrem-custacc-page");
		}
		if (formName.equals("printableList"))
		{
			Log.printVerbose(strClassName + ": formName = printableList");
			fnGetInitialParams(servlet, req, res);
			return new ActionRouter("cust-popup-printable-customer-list-page");
		}
		if (formName.equals("addCustAcc"))
		{
			Log.printVerbose(strClassName + ": formName = addCustAcc");
			fnAdd(servlet, req, res);
		}
		if (formName.equals("deactCustAcc"))
		{
			Log.printVerbose(strClassName + ": formName = deactCustAcc");
			fnDeAct(servlet, req, res);
		}
		if (formName.equals("activateCustAcc"))
		{
			Log.printVerbose(strClassName + ": formName = activateCustAcc");
			fnActivate(servlet, req, res);
		}
		fnGetCustTypes(servlet, req, res);
		// fnGetInitialParams(servlet, req, res);
		return new ActionRouter("cust-setup-addrem-custacc-page");
	}

	protected void fnRemove(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String rmCustAccountCode = (String) req.getParameter("removeCustAccount");
		if (rmCustAccountCode != null)
		{
			CustAccount lCustAccountCode = CustAccountNut.getObjectByCode(rmCustAccountCode);
			if (lCustAccountCode != null)
			{
				try
				{
					lCustAccountCode.remove();
				} catch (Exception ex)
				{
					Log.printDebug("Remove CustAccount Failed" + ex.getMessage());
				} // end try-catch
			} // end if
		} // end if
	} // end fnRmCustAccount

	protected void fnGetCustTypes(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetCustTypes()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		// 2. itrCustType
		Collection colCustType = CustAccountNut.getCustTypesStr();
		Iterator itrCustType = colCustType.iterator();
		Log.printVerbose("Setting attribute itrCustType now");
		req.setAttribute("itrCustType", itrCustType);
	}

	protected void fnGetInitialParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetInitialParams()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		// Need the following :
		// 1. "custCode" - the new customer code
		// 2. "itrCustType" - iterator of all customer types
		// 3. "itrActiveCustAccount" - for the display of all active customers
		// 4. "itrPassiveCustAccount" - for the display of all active customers
		/*
		 * // 1. custCode String custCode = CustAccountNut.getNextPkid(); //
		 * make code = pkid Log.printVerbose("Setting attribute \"custCode\"
		 * now"); req.setAttribute("custCode", custCode);
		 */
		// 2. itrCustType
		// fnGetCustTypes(servlet,req,res);
		// 3. itrActiveCustAccount
		Collection colActiveCustAccount = CustAccountNut.getActiveObjects();
		Iterator itrActiveCustAccount = colActiveCustAccount.iterator();
		Log.printVerbose("Setting attribute itrActiveCustAccount now");
		req.setAttribute("itrActiveCustAccount", itrActiveCustAccount);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
		// 4. itrPassivaCustAccount
		Collection colInactiveCustAccount = CustAccountNut.getInactiveObjects();
		Iterator itrInactiveCustAccount = colInactiveCustAccount.iterator();
		Log.printVerbose("Setting attribute itrPassiveCustAccount now");
		req.setAttribute("itrPassiveCustAccount", itrInactiveCustAccount);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
	}

	protected void fnAdd(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAdd()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		// String custCode = (String) req.getParameter("custCode");
		String custName = (String) req.getParameter("custName");
		String custDesc = (String) req.getParameter("custDescription");
		String custType = (String) req.getParameter("custType");
		// Print results obtained from getParameter()
		// String params = "CustAccountCode = " + custCode + "\n";
		String params = "CustAccountName = " + custName + "\n";
		params += "CustAccountDesc = " + custDesc + "\n";
		params += "CustAccountType = " + custType;
		Log.printVerbose(strClassName + ": Params = \n" + params);
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		if (lUsr == null)
			Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
		// if(custCode == null)
		// return ;
		if (custName == null)
			return;
		if (custDesc == null)
			return;
		if (custType == null)
			return;
		// Convert custType from String to Enum
		Integer enumCustType = (Integer) CustAccountNut.mapTypeStrToEnum.get(custType);
		// Ensure CustAccount does not exist
		// CustAccount lCustAccount = CustAccountNut.getObjectByCode(custCode);
		if (lUsr != null)
		{
			Log.printVerbose("Adding new CustAccount");
			CustAccountHome lCustAccountH = CustAccountNut.getHome();
			java.util.Date ldt = new java.util.Date();
			Timestamp tsCreate = new Timestamp(ldt.getTime());
			Long lCode = new Long(ldt.getTime());
			Integer usrid = null;
			try
			{
				usrid = lUsr.getUserId();
			} catch (Exception ex)
			{
				Log.printAudit("User does not exist: " + ex.getMessage());
			}
			// Create the account here
			CustAccount custAcc = null;
			try
			{
				custAcc = lCustAccountH.create(lCode.toString(), custName, custDesc, enumCustType, tsCreate, usrid);
				if (custAcc == null)
				{
					String errMsg = "Failed to create Customer Account";
					req.setAttribute("errMsg", errMsg);
				} else
				{
					// Derive the custType in String
					String strCustType = (String) CustAccountNut.mapTypeEnumToStr.get(custAcc.getAccType());
					req.setAttribute("custId", custAcc.getPkid());
					req.setAttribute("custName", custAcc.getName());
					req.setAttribute("custDesc", custAcc.getDescription());
					req.setAttribute("custType", strCustType);
				}
			} catch (Exception ex)
			{
				String errMsg = "Failed to create Customer Account: " + ex.getMessage();
				Log.printDebug(errMsg);
				req.setAttribute("errMsg", errMsg);
			}
		}
		// fnGetInitialParams(servlet, req, res);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
	}

	protected void fnDeAct(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnDeAct()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		Integer custPkid = new Integer(req.getParameter("deactCustAccount"));
		if (custPkid != null)
		{
			if (!CustAccountNut.deactCustAccount(custPkid))
			{
				Log.printDebug("Failed to deactivate customer: " + custPkid);
			} else
			{
				Log.printVerbose("Successfully deactivated customer: " + custPkid);
			}
		}
	}

	protected void fnActivate(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnActivate()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		Integer custPkid = new Integer(req.getParameter("activateCustAccount"));
		if (custPkid != null)
		{
			if (!CustAccountNut.activateCustAccount(custPkid))
			{
				Log.printDebug("Failed to activate customer: " + custPkid);
			} else
			{
				Log.printVerbose("Successfully activated customer: " + custPkid);
			}
		}
	}
}
