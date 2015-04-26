package com.vlee.servlet.supplier;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.supplier.*;

public class DoAccountAddRem implements Action
{
	private String strClassName = "DoAccountAddRem";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			Log.printVerbose(strClassName + ": formName = null");
			fnGetInitialParams(servlet, req, res);
			return new ActionRouter("supp-setup-addrem-account-page");
		}
		if (formName.equals("addSuppAcc"))
		{
			Log.printVerbose(strClassName + ": formName = addSuppAcc");
			fnAdd(servlet, req, res);
		}
		if (formName.equals("deactSuppAcc"))
		{
			Log.printVerbose(strClassName + ": formName = deactSuppAcc");
			fnDeAct(servlet, req, res);
		}
		if (formName.equals("activateSuppAcc"))
		{
			Log.printVerbose(strClassName + ": formName = activateSuppAcc");
			fnActivate(servlet, req, res);
		}
		fnGetInitialParams(servlet, req, res);
		return new ActionRouter("supp-setup-addrem-account-page");
	}

	protected void fnRemove(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String rmSuppAccountCode = (String) req.getParameter("removeSuppAccount");
		if (rmSuppAccountCode != null)
		{
			SuppAccount lSuppAccountCode = SuppAccountNut.getObjectByCode(rmSuppAccountCode);
			if (lSuppAccountCode != null)
			{
				try
				{
					lSuppAccountCode.remove();
				} catch (Exception ex)
				{
					Log.printDebug("Remove SuppAccount Failed" + ex.getMessage());
				} // end try-catch
			} // end if
		} // end if
	} // end fnRmSuppAccount

	protected void fnGetInitialParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetInitialParams()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		// Need the following :
		// 1. "suppCode" - the new supplier code
		// 2. "itrSuppType" - iterator of all supplier types
		// 3. "itrActiveSuppAccount" - for the display of all active suppliers
		// 4. "itrPassiveSuppAccount" - for the display of all active suppliers
		// 1. suppCode
		// String suppCode = SuppAccountNut.getNextCode();
		// Log.printVerbose("Setting attribute \"suppCode\" now");
		// req.setAttribute("suppCode", suppCode);
		// 2. itrSuppType
		/*
		 * Collection colSuppType = SuppAccountNut.getSuppTypesStr(); Iterator
		 * itrSuppType = colSuppType.iterator(); Log.printVerbose("Setting
		 * attribute itrSuppType now"); req.setAttribute("itrSuppType",
		 * itrSuppType);
		 */
		// 3. itrActiveSuppAccount
		Collection colActiveSuppAccount = SuppAccountNut.getActiveObjects();
		Iterator itrActiveSuppAccount = colActiveSuppAccount.iterator();
		Log.printVerbose("Setting attribute itrActiveSuppAccount now");
		req.setAttribute("itrActiveSuppAccount", itrActiveSuppAccount);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
		// 4. itrPassivaSuppAccount
		Collection colInactiveSuppAccount = SuppAccountNut.getInactiveObjects();
		Iterator itrInactiveSuppAccount = colInactiveSuppAccount.iterator();
		Log.printVerbose("Setting attribute itrPassiveSuppAccount now");
		req.setAttribute("itrPassiveSuppAccount", itrInactiveSuppAccount);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
	}

	protected void fnAdd(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAdd()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		// String suppCode = (String) req.getParameter("suppCode");
		// String suppPkid = (String) req.getParameter("suppPkid");
		String suppName = (String) req.getParameter("suppName");
		String suppDesc = (String) req.getParameter("suppDescription");
		String suppType = (String) req.getParameter("suppType");
		// Print results obtained from getParameter()
		// String params = "SuppAccountPkid = " + suppPkid+ "\n";
		String params = new String(" ");
		params += "SuppAccountName = " + suppName + "\n";
		params += "SuppAccountDesc = " + suppDesc + "\n";
		params += "SuppAccountType = " + suppType;
		Log.printVerbose(strClassName + ": Params = \n" + params);
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		if (lUsr == null)
			Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
		if (suppName == null)
			return;
		if (suppDesc == null)
			return;
		if (suppType == null)
			return;
		// Convert suppType from String to Enum
		Integer enumSuppType = new Integer(suppType);
		// /SuppAccountNut.mapTypeStrToEnum.get(suppType);
		Timestamp tsCreate = com.vlee.util.TimeFormat.getTimestamp();
		// Ensure SuppAccount does not exist
		Integer usrid = null;
		try
		{
			usrid = lUsr.getUserId();
		} catch (Exception ex)
		{
			Log.printAudit("User does not exist: " + ex.getMessage());
		}
		// Create the account here
		try
		{
			java.util.Date theDate = new java.util.Date();
			Long lCode = new Long(theDate.getTime());
			// String suppCode = new String(" ");
			SuppAccountHome lSuppAccountH = SuppAccountNut.getHome();
			lSuppAccountH.create(lCode.toString(), suppName, suppDesc, enumSuppType, tsCreate, usrid);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("Cannot create Supplier Account because " + ex.getMessage());
		}
		/*
		 * Integer iSuppAcc = new Integer(suppPkid); SuppAccount lSuppAccount =
		 * SuppAccountNut.getHandle(iSuppAcc);
		 * 
		 * if(lSuppAccount == null && lUsr != null) { Log.printVerbose("Adding
		 * new SuppAccount"); SuppAccountHome lSuppAccountH =
		 * SuppAccountNut.getHome(); java.util.Date ldt = new java.util.Date();
		 * Timestamp tsCreate = new Timestamp(ldt.getTime()); Integer usrid =
		 * null; try { usrid = lUsr.getUserId(); } catch(Exception ex) {
		 * Log.printAudit("User does not exist: " + ex.getMessage()); }
		 *  // Create the account here try { String suppCode = new String(" ");
		 * lSuppAccountH.create(suppCode, suppName, suppDesc, enumSuppType,
		 * tsCreate, usrid); } catch(Exception ex) { Log.printDebug("Cannot
		 * create Supplier Account because " + ex.getMessage()); }
		 *  }
		 */
		// fnGetInitialParams(servlet, req, res);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
	}

	protected void fnDeAct(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnDeAct()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		String suppPkid = (String) req.getParameter("deactSuppAccount");
		if (suppPkid != null)
		{
			if (!SuppAccountNut.deactSuppAccount(suppPkid))
			{
				Log.printDebug("Failed to deactivate supplier: ");
			} else
			{
				Log.printVerbose("Successfully deactivated supplier: ");
			}
		}
	}

	protected void fnActivate(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnActivate()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		String strSuppPkid = (String) req.getParameter("activateSuppAccount");
		Integer iSuppPkid = new Integer(strSuppPkid);
		if (iSuppPkid != null)
		{
			if (!SuppAccountNut.activateSuppAccount(strSuppPkid))
			{
				Log.printDebug("Failed to activate supplier: " + strSuppPkid);
			} else
			{
				Log.printVerbose("Successfully activated supplier: ");
			}
		}
	}
}
package com.vlee.servlet.supplier;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.supplier.*;

public class DoAccountAddRem implements Action
{
	private String strClassName = "DoAccountAddRem";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			Log.printVerbose(strClassName + ": formName = null");
			fnGetInitialParams(servlet, req, res);
			return new ActionRouter("supp-setup-addrem-account-page");
		}
		if (formName.equals("addSuppAcc"))
		{
			Log.printVerbose(strClassName + ": formName = addSuppAcc");
			fnAdd(servlet, req, res);
		}
		if (formName.equals("deactSuppAcc"))
		{
			Log.printVerbose(strClassName + ": formName = deactSuppAcc");
			fnDeAct(servlet, req, res);
		}
		if (formName.equals("activateSuppAcc"))
		{
			Log.printVerbose(strClassName + ": formName = activateSuppAcc");
			fnActivate(servlet, req, res);
		}
		fnGetInitialParams(servlet, req, res);
		return new ActionRouter("supp-setup-addrem-account-page");
	}

	protected void fnRemove(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String rmSuppAccountCode = (String) req.getParameter("removeSuppAccount");
		if (rmSuppAccountCode != null)
		{
			SuppAccount lSuppAccountCode = SuppAccountNut.getObjectByCode(rmSuppAccountCode);
			if (lSuppAccountCode != null)
			{
				try
				{
					lSuppAccountCode.remove();
				} catch (Exception ex)
				{
					Log.printDebug("Remove SuppAccount Failed" + ex.getMessage());
				} // end try-catch
			} // end if
		} // end if
	} // end fnRmSuppAccount

	protected void fnGetInitialParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetInitialParams()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		// Need the following :
		// 1. "suppCode" - the new supplier code
		// 2. "itrSuppType" - iterator of all supplier types
		// 3. "itrActiveSuppAccount" - for the display of all active suppliers
		// 4. "itrPassiveSuppAccount" - for the display of all active suppliers
		// 1. suppCode
		// String suppCode = SuppAccountNut.getNextCode();
		// Log.printVerbose("Setting attribute \"suppCode\" now");
		// req.setAttribute("suppCode", suppCode);
		// 2. itrSuppType
		/*
		 * Collection colSuppType = SuppAccountNut.getSuppTypesStr(); Iterator
		 * itrSuppType = colSuppType.iterator(); Log.printVerbose("Setting
		 * attribute itrSuppType now"); req.setAttribute("itrSuppType",
		 * itrSuppType);
		 */
		// 3. itrActiveSuppAccount
		Collection colActiveSuppAccount = SuppAccountNut.getActiveObjects();
		Iterator itrActiveSuppAccount = colActiveSuppAccount.iterator();
		Log.printVerbose("Setting attribute itrActiveSuppAccount now");
		req.setAttribute("itrActiveSuppAccount", itrActiveSuppAccount);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
		// 4. itrPassivaSuppAccount
		Collection colInactiveSuppAccount = SuppAccountNut.getInactiveObjects();
		Iterator itrInactiveSuppAccount = colInactiveSuppAccount.iterator();
		Log.printVerbose("Setting attribute itrPassiveSuppAccount now");
		req.setAttribute("itrPassiveSuppAccount", itrInactiveSuppAccount);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
	}

	protected void fnAdd(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAdd()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		// String suppCode = (String) req.getParameter("suppCode");
		// String suppPkid = (String) req.getParameter("suppPkid");
		String suppName = (String) req.getParameter("suppName");
		String suppDesc = (String) req.getParameter("suppDescription");
		String suppType = (String) req.getParameter("suppType");
		// Print results obtained from getParameter()
		// String params = "SuppAccountPkid = " + suppPkid+ "\n";
		String params = new String(" ");
		params += "SuppAccountName = " + suppName + "\n";
		params += "SuppAccountDesc = " + suppDesc + "\n";
		params += "SuppAccountType = " + suppType;
		Log.printVerbose(strClassName + ": Params = \n" + params);
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		if (lUsr == null)
			Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
		if (suppName == null)
			return;
		if (suppDesc == null)
			return;
		if (suppType == null)
			return;
		// Convert suppType from String to Enum
		Integer enumSuppType = new Integer(suppType);
		// /SuppAccountNut.mapTypeStrToEnum.get(suppType);
		Timestamp tsCreate = com.vlee.util.TimeFormat.getTimestamp();
		// Ensure SuppAccount does not exist
		Integer usrid = null;
		try
		{
			usrid = lUsr.getUserId();
		} catch (Exception ex)
		{
			Log.printAudit("User does not exist: " + ex.getMessage());
		}
		// Create the account here
		try
		{
			java.util.Date theDate = new java.util.Date();
			Long lCode = new Long(theDate.getTime());
			// String suppCode = new String(" ");
			SuppAccountHome lSuppAccountH = SuppAccountNut.getHome();
			lSuppAccountH.create(lCode.toString(), suppName, suppDesc, enumSuppType, tsCreate, usrid);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("Cannot create Supplier Account because " + ex.getMessage());
		}
		/*
		 * Integer iSuppAcc = new Integer(suppPkid); SuppAccount lSuppAccount =
		 * SuppAccountNut.getHandle(iSuppAcc);
		 * 
		 * if(lSuppAccount == null && lUsr != null) { Log.printVerbose("Adding
		 * new SuppAccount"); SuppAccountHome lSuppAccountH =
		 * SuppAccountNut.getHome(); java.util.Date ldt = new java.util.Date();
		 * Timestamp tsCreate = new Timestamp(ldt.getTime()); Integer usrid =
		 * null; try { usrid = lUsr.getUserId(); } catch(Exception ex) {
		 * Log.printAudit("User does not exist: " + ex.getMessage()); }
		 *  // Create the account here try { String suppCode = new String(" ");
		 * lSuppAccountH.create(suppCode, suppName, suppDesc, enumSuppType,
		 * tsCreate, usrid); } catch(Exception ex) { Log.printDebug("Cannot
		 * create Supplier Account because " + ex.getMessage()); }
		 *  }
		 */
		// fnGetInitialParams(servlet, req, res);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
	}

	protected void fnDeAct(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnDeAct()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		String suppPkid = (String) req.getParameter("deactSuppAccount");
		if (suppPkid != null)
		{
			if (!SuppAccountNut.deactSuppAccount(suppPkid))
			{
				Log.printDebug("Failed to deactivate supplier: ");
			} else
			{
				Log.printVerbose("Successfully deactivated supplier: ");
			}
		}
	}

	protected void fnActivate(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnActivate()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		String strSuppPkid = (String) req.getParameter("activateSuppAccount");
		Integer iSuppPkid = new Integer(strSuppPkid);
		if (iSuppPkid != null)
		{
			if (!SuppAccountNut.activateSuppAccount(strSuppPkid))
			{
				Log.printDebug("Failed to activate supplier: " + strSuppPkid);
			} else
			{
				Log.printVerbose("Successfully activated supplier: ");
			}
		}
	}
}
package com.vlee.servlet.supplier;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.supplier.*;

public class DoAccountAddRem implements Action
{
	private String strClassName = "DoAccountAddRem";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			Log.printVerbose(strClassName + ": formName = null");
			fnGetInitialParams(servlet, req, res);
			return new ActionRouter("supp-setup-addrem-account-page");
		}
		if (formName.equals("addSuppAcc"))
		{
			Log.printVerbose(strClassName + ": formName = addSuppAcc");
			fnAdd(servlet, req, res);
		}
		if (formName.equals("deactSuppAcc"))
		{
			Log.printVerbose(strClassName + ": formName = deactSuppAcc");
			fnDeAct(servlet, req, res);
		}
		if (formName.equals("activateSuppAcc"))
		{
			Log.printVerbose(strClassName + ": formName = activateSuppAcc");
			fnActivate(servlet, req, res);
		}
		fnGetInitialParams(servlet, req, res);
		return new ActionRouter("supp-setup-addrem-account-page");
	}

	protected void fnRemove(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String rmSuppAccountCode = (String) req.getParameter("removeSuppAccount");
		if (rmSuppAccountCode != null)
		{
			SuppAccount lSuppAccountCode = SuppAccountNut.getObjectByCode(rmSuppAccountCode);
			if (lSuppAccountCode != null)
			{
				try
				{
					lSuppAccountCode.remove();
				} catch (Exception ex)
				{
					Log.printDebug("Remove SuppAccount Failed" + ex.getMessage());
				} // end try-catch
			} // end if
		} // end if
	} // end fnRmSuppAccount

	protected void fnGetInitialParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetInitialParams()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		// Need the following :
		// 1. "suppCode" - the new supplier code
		// 2. "itrSuppType" - iterator of all supplier types
		// 3. "itrActiveSuppAccount" - for the display of all active suppliers
		// 4. "itrPassiveSuppAccount" - for the display of all active suppliers
		// 1. suppCode
		// String suppCode = SuppAccountNut.getNextCode();
		// Log.printVerbose("Setting attribute \"suppCode\" now");
		// req.setAttribute("suppCode", suppCode);
		// 2. itrSuppType
		/*
		 * Collection colSuppType = SuppAccountNut.getSuppTypesStr(); Iterator
		 * itrSuppType = colSuppType.iterator(); Log.printVerbose("Setting
		 * attribute itrSuppType now"); req.setAttribute("itrSuppType",
		 * itrSuppType);
		 */
		// 3. itrActiveSuppAccount
		Collection colActiveSuppAccount = SuppAccountNut.getActiveObjects();
		Iterator itrActiveSuppAccount = colActiveSuppAccount.iterator();
		Log.printVerbose("Setting attribute itrActiveSuppAccount now");
		req.setAttribute("itrActiveSuppAccount", itrActiveSuppAccount);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
		// 4. itrPassivaSuppAccount
		Collection colInactiveSuppAccount = SuppAccountNut.getInactiveObjects();
		Iterator itrInactiveSuppAccount = colInactiveSuppAccount.iterator();
		Log.printVerbose("Setting attribute itrPassiveSuppAccount now");
		req.setAttribute("itrPassiveSuppAccount", itrInactiveSuppAccount);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
	}

	protected void fnAdd(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAdd()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		// String suppCode = (String) req.getParameter("suppCode");
		// String suppPkid = (String) req.getParameter("suppPkid");
		String suppName = (String) req.getParameter("suppName");
		String suppDesc = (String) req.getParameter("suppDescription");
		String suppType = (String) req.getParameter("suppType");
		// Print results obtained from getParameter()
		// String params = "SuppAccountPkid = " + suppPkid+ "\n";
		String params = new String(" ");
		params += "SuppAccountName = " + suppName + "\n";
		params += "SuppAccountDesc = " + suppDesc + "\n";
		params += "SuppAccountType = " + suppType;
		Log.printVerbose(strClassName + ": Params = \n" + params);
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		if (lUsr == null)
			Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
		if (suppName == null)
			return;
		if (suppDesc == null)
			return;
		if (suppType == null)
			return;
		// Convert suppType from String to Enum
		Integer enumSuppType = new Integer(suppType);
		// /SuppAccountNut.mapTypeStrToEnum.get(suppType);
		Timestamp tsCreate = com.vlee.util.TimeFormat.getTimestamp();
		// Ensure SuppAccount does not exist
		Integer usrid = null;
		try
		{
			usrid = lUsr.getUserId();
		} catch (Exception ex)
		{
			Log.printAudit("User does not exist: " + ex.getMessage());
		}
		// Create the account here
		try
		{
			java.util.Date theDate = new java.util.Date();
			Long lCode = new Long(theDate.getTime());
			// String suppCode = new String(" ");
			SuppAccountHome lSuppAccountH = SuppAccountNut.getHome();
			lSuppAccountH.create(lCode.toString(), suppName, suppDesc, enumSuppType, tsCreate, usrid);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("Cannot create Supplier Account because " + ex.getMessage());
		}
		/*
		 * Integer iSuppAcc = new Integer(suppPkid); SuppAccount lSuppAccount =
		 * SuppAccountNut.getHandle(iSuppAcc);
		 * 
		 * if(lSuppAccount == null && lUsr != null) { Log.printVerbose("Adding
		 * new SuppAccount"); SuppAccountHome lSuppAccountH =
		 * SuppAccountNut.getHome(); java.util.Date ldt = new java.util.Date();
		 * Timestamp tsCreate = new Timestamp(ldt.getTime()); Integer usrid =
		 * null; try { usrid = lUsr.getUserId(); } catch(Exception ex) {
		 * Log.printAudit("User does not exist: " + ex.getMessage()); }
		 *  // Create the account here try { String suppCode = new String(" ");
		 * lSuppAccountH.create(suppCode, suppName, suppDesc, enumSuppType,
		 * tsCreate, usrid); } catch(Exception ex) { Log.printDebug("Cannot
		 * create Supplier Account because " + ex.getMessage()); }
		 *  }
		 */
		// fnGetInitialParams(servlet, req, res);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
	}

	protected void fnDeAct(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnDeAct()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		String suppPkid = (String) req.getParameter("deactSuppAccount");
		if (suppPkid != null)
		{
			if (!SuppAccountNut.deactSuppAccount(suppPkid))
			{
				Log.printDebug("Failed to deactivate supplier: ");
			} else
			{
				Log.printVerbose("Successfully deactivated supplier: ");
			}
		}
	}

	protected void fnActivate(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnActivate()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		String strSuppPkid = (String) req.getParameter("activateSuppAccount");
		Integer iSuppPkid = new Integer(strSuppPkid);
		if (iSuppPkid != null)
		{
			if (!SuppAccountNut.activateSuppAccount(strSuppPkid))
			{
				Log.printDebug("Failed to activate supplier: " + strSuppPkid);
			} else
			{
				Log.printVerbose("Successfully activated supplier: ");
			}
		}
	}
}
package com.vlee.servlet.supplier;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.supplier.*;

public class DoAccountAddRem implements Action
{
	private String strClassName = "DoAccountAddRem";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			Log.printVerbose(strClassName + ": formName = null");
			fnGetInitialParams(servlet, req, res);
			return new ActionRouter("supp-setup-addrem-account-page");
		}
		if (formName.equals("addSuppAcc"))
		{
			Log.printVerbose(strClassName + ": formName = addSuppAcc");
			fnAdd(servlet, req, res);
		}
		if (formName.equals("deactSuppAcc"))
		{
			Log.printVerbose(strClassName + ": formName = deactSuppAcc");
			fnDeAct(servlet, req, res);
		}
		if (formName.equals("activateSuppAcc"))
		{
			Log.printVerbose(strClassName + ": formName = activateSuppAcc");
			fnActivate(servlet, req, res);
		}
		fnGetInitialParams(servlet, req, res);
		return new ActionRouter("supp-setup-addrem-account-page");
	}

	protected void fnRemove(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String rmSuppAccountCode = (String) req.getParameter("removeSuppAccount");
		if (rmSuppAccountCode != null)
		{
			SuppAccount lSuppAccountCode = SuppAccountNut.getObjectByCode(rmSuppAccountCode);
			if (lSuppAccountCode != null)
			{
				try
				{
					lSuppAccountCode.remove();
				} catch (Exception ex)
				{
					Log.printDebug("Remove SuppAccount Failed" + ex.getMessage());
				} // end try-catch
			} // end if
		} // end if
	} // end fnRmSuppAccount

	protected void fnGetInitialParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetInitialParams()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		// Need the following :
		// 1. "suppCode" - the new supplier code
		// 2. "itrSuppType" - iterator of all supplier types
		// 3. "itrActiveSuppAccount" - for the display of all active suppliers
		// 4. "itrPassiveSuppAccount" - for the display of all active suppliers
		// 1. suppCode
		// String suppCode = SuppAccountNut.getNextCode();
		// Log.printVerbose("Setting attribute \"suppCode\" now");
		// req.setAttribute("suppCode", suppCode);
		// 2. itrSuppType
		/*
		 * Collection colSuppType = SuppAccountNut.getSuppTypesStr(); Iterator
		 * itrSuppType = colSuppType.iterator(); Log.printVerbose("Setting
		 * attribute itrSuppType now"); req.setAttribute("itrSuppType",
		 * itrSuppType);
		 */
		// 3. itrActiveSuppAccount
		Collection colActiveSuppAccount = SuppAccountNut.getActiveObjects();
		Iterator itrActiveSuppAccount = colActiveSuppAccount.iterator();
		Log.printVerbose("Setting attribute itrActiveSuppAccount now");
		req.setAttribute("itrActiveSuppAccount", itrActiveSuppAccount);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
		// 4. itrPassivaSuppAccount
		Collection colInactiveSuppAccount = SuppAccountNut.getInactiveObjects();
		Iterator itrInactiveSuppAccount = colInactiveSuppAccount.iterator();
		Log.printVerbose("Setting attribute itrPassiveSuppAccount now");
		req.setAttribute("itrPassiveSuppAccount", itrInactiveSuppAccount);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
	}

	protected void fnAdd(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAdd()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		// String suppCode = (String) req.getParameter("suppCode");
		// String suppPkid = (String) req.getParameter("suppPkid");
		String suppName = (String) req.getParameter("suppName");
		String suppDesc = (String) req.getParameter("suppDescription");
		String suppType = (String) req.getParameter("suppType");
		// Print results obtained from getParameter()
		// String params = "SuppAccountPkid = " + suppPkid+ "\n";
		String params = new String(" ");
		params += "SuppAccountName = " + suppName + "\n";
		params += "SuppAccountDesc = " + suppDesc + "\n";
		params += "SuppAccountType = " + suppType;
		Log.printVerbose(strClassName + ": Params = \n" + params);
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		if (lUsr == null)
			Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
		if (suppName == null)
			return;
		if (suppDesc == null)
			return;
		if (suppType == null)
			return;
		// Convert suppType from String to Enum
		Integer enumSuppType = new Integer(suppType);
		// /SuppAccountNut.mapTypeStrToEnum.get(suppType);
		Timestamp tsCreate = com.vlee.util.TimeFormat.getTimestamp();
		// Ensure SuppAccount does not exist
		Integer usrid = null;
		try
		{
			usrid = lUsr.getUserId();
		} catch (Exception ex)
		{
			Log.printAudit("User does not exist: " + ex.getMessage());
		}
		// Create the account here
		try
		{
			java.util.Date theDate = new java.util.Date();
			Long lCode = new Long(theDate.getTime());
			// String suppCode = new String(" ");
			SuppAccountHome lSuppAccountH = SuppAccountNut.getHome();
			lSuppAccountH.create(lCode.toString(), suppName, suppDesc, enumSuppType, tsCreate, usrid);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("Cannot create Supplier Account because " + ex.getMessage());
		}
		/*
		 * Integer iSuppAcc = new Integer(suppPkid); SuppAccount lSuppAccount =
		 * SuppAccountNut.getHandle(iSuppAcc);
		 * 
		 * if(lSuppAccount == null && lUsr != null) { Log.printVerbose("Adding
		 * new SuppAccount"); SuppAccountHome lSuppAccountH =
		 * SuppAccountNut.getHome(); java.util.Date ldt = new java.util.Date();
		 * Timestamp tsCreate = new Timestamp(ldt.getTime()); Integer usrid =
		 * null; try { usrid = lUsr.getUserId(); } catch(Exception ex) {
		 * Log.printAudit("User does not exist: " + ex.getMessage()); }
		 *  // Create the account here try { String suppCode = new String(" ");
		 * lSuppAccountH.create(suppCode, suppName, suppDesc, enumSuppType,
		 * tsCreate, usrid); } catch(Exception ex) { Log.printDebug("Cannot
		 * create Supplier Account because " + ex.getMessage()); }
		 *  }
		 */
		// fnGetInitialParams(servlet, req, res);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
	}

	protected void fnDeAct(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnDeAct()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		String suppPkid = (String) req.getParameter("deactSuppAccount");
		if (suppPkid != null)
		{
			if (!SuppAccountNut.deactSuppAccount(suppPkid))
			{
				Log.printDebug("Failed to deactivate supplier: ");
			} else
			{
				Log.printVerbose("Successfully deactivated supplier: ");
			}
		}
	}

	protected void fnActivate(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnActivate()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		String strSuppPkid = (String) req.getParameter("activateSuppAccount");
		Integer iSuppPkid = new Integer(strSuppPkid);
		if (iSuppPkid != null)
		{
			if (!SuppAccountNut.activateSuppAccount(strSuppPkid))
			{
				Log.printDebug("Failed to activate supplier: " + strSuppPkid);
			} else
			{
				Log.printVerbose("Successfully activated supplier: ");
			}
		}
	}
}
