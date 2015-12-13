package com.vlee.servlet.employee;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.employee.*;

public class DoRIAddRem implements Action
{
	private String strClassName = "DoRIAddRem";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			Log.printVerbose(strClassName + ": formName = null");
			fnGetInitialParams(servlet, req, res);
			return new ActionRouter("erm-remuneration-addrem-item-page");
		}
		if (formName.equals("addRI"))
		{
			Log.printVerbose(strClassName + ": formName = addRI");
			fnGetInitialParams(servlet, req, res);
			fnAdd(servlet, req, res);
		}
		if (formName.equals("deactRemItemType"))
		{
			Log.printVerbose(strClassName + ": formName = deactRemItemType");
			fnDeAct(servlet, req, res);
		}
		if (formName.equals("activateRemItemType"))
		{
			Log.printVerbose(strClassName + ": formName = activateCustAcc");
			fnActivate(servlet, req, res);
		}
		fnGetInitialParams(servlet, req, res);
		return new ActionRouter("erm-remuneration-addrem-item-page");
	}

	/*
	 * protected void fnRemove(HttpServlet servlet, HttpServletRequest req,
	 * HttpServletResponse res) { String rmCustAccountCode =
	 * (String)req.getParameter("removeCustAccount"); if(rmCustAccountCode !=
	 * null) { CustAccount lCustAccountCode =
	 * CustAccountNut.getObjectByCode(rmCustAccountCode); if(lCustAccountCode
	 * !=null) { try { lCustAccountCode.remove(); } catch(Exception ex) {
	 * Log.printDebug("Remove CustAccount Failed" + ex.getMessage()); } // end
	 * try-catch } // end if } // end if } // end fnRmCustAccount
	 */
	protected void fnGetInitialParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetInitialParams()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		// Need the following :
		// 1. "itrAllRI" -- for the display of ALL Type of Remuneration Item
		// 2. "itrActiveRI" -- for the display of ALL Active Remuneration Item
		// Type
		// 3. "itrPassiveRI" -- for the display of ALL Passive Type of
		// Remuneration Item
		// ----------------------- 1. itrAllRI --------------------------
		EmpRemunerationItemTypeHome lRIHome = EmpRemunerationItemTypeNut.getHome();
		Collection colAllRI = null;
		try
		{
			colAllRI = lRIHome.findAllObjects();
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("Error msg " + ex.getMessage());
		}
		Iterator itrAllRI = colAllRI.iterator();
		Log.printVerbose("Setting attribute itrAllRI now");
		req.setAttribute("itrAllRI", itrAllRI);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
		// -------------------------- end of 1. -------------------------
		// ------------------------- 2. itrActiveRI ---------------
		Collection colActiveRemItemType = EmpRemunerationItemTypeNut.getActiveObjects();
		Iterator itrActiveRI = colActiveRemItemType.iterator();
		req.setAttribute("itrActiveRI", itrActiveRI);
		Log.printVerbose("Setting attribute itrActiveRI now");
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
		// -------------------------- end of 2. -----------------------------
		// ----------------------- 3. itrPassiveRI ---------------------------
		Collection colInactiveRemItemType = EmpRemunerationItemTypeNut.getInactiveObjects();
		Iterator itrInactiveRI = colInactiveRemItemType.iterator();
		Log.printVerbose("Setting attribute itrInactiveRI now");
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
		req.setAttribute("itrPassiveRI", itrInactiveRI);
		// ------------------------- end of 3. -----------------------------
	}

	protected void fnAdd(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAdd()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		String itemNo = (String) req.getParameter("itemNo");
		String itemName = (String) req.getParameter("itemName");
		String Description = (String) req.getParameter("Description");
		String strEffDate = (String) req.getParameter("effective_date");
		String glcode = (String) req.getParameter("Glcode");
		// Print results obtained from getParameter()
		String params = "itemName = " + itemName + "\n";
		params += "Description = " + Description + "\n";
		// params += "Effective Date = " + EffectiveDate;
		Log.printVerbose(strClassName + ": Params = \n" + params);
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		if (lUsr == null)
			Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
		if (itemName == null)
			return;
		if (Description == null)
			return;
		// if(glcode == null)
		// return ;
		// Convert from String to Enum
		Integer enumGlcode = new Integer(glcode);
		// Ensure Remuneration Item does not exist
		// EmpRemunerationItemType lRI =
		// EmpRemunerationItemTypeNut.getObjectByCode(itemName);
		// if(lRI == null && lUsr != null)
		if (lUsr != null && itemName != null)
		{
			Log.printVerbose("Adding new Remuneration Item");
			EmpRemunerationItemTypeHome lEmpRemunerationItemTypeH = EmpRemunerationItemTypeNut.getHome();
			java.util.Date ldt = new java.util.Date();
			Timestamp tsCreate = new Timestamp(ldt.getTime());
			Timestamp tsEffDate = new Timestamp(ldt.getTime());
			tsEffDate = TimeFormat.createTimeStamp(strEffDate);
			Integer usrid = null;
			String type = "Nil";
			try
			{
				usrid = lUsr.getUserId();
			} catch (Exception ex)
			{
				Log.printAudit("User does not exist: " + ex.getMessage());
			}
			// Create the Remuneration Item
			try
			{
				lEmpRemunerationItemTypeH.create(itemName, Description, tsEffDate, tsCreate, usrid, enumGlcode);
			} catch (Exception ex)
			{
				Log.printDebug("Cannot create Remuneration Item Type because " + ex.getMessage());
			}
		}
		// fnGetInitialParams(servlet, req, res);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
	}

	protected void fnDeAct(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnDeAct()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		String RemItemType = (String) req.getParameter("deactivate");
		if (RemItemType != null)
		{
			Integer intRemItemType = new Integer(RemItemType);
			if (!EmpRemunerationItemTypeNut.deactRemItemType(intRemItemType))
			{
				Log.printDebug("Failed to deactivate remuneration item type: " + RemItemType);
			} else
			{
				Log.printVerbose("Successfully deactivated remuneration item type: " + RemItemType);
			}
		}
	}

	protected void fnActivate(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnActivate()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		String RemItemType = (String) req.getParameter("activate");
		if (RemItemType != null)
		{
			Integer intRemItemType = new Integer(RemItemType);
			if (!EmpRemunerationItemTypeNut.activateRemItemType(intRemItemType))
			{
				Log.printDebug("Failed to activate Rem Item Type: " + RemItemType);
			} else
			{
				Log.printVerbose("Successfully activated Rem Item Type: " + RemItemType);
			}
		}
	}
}
package com.vlee.servlet.employee;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.employee.*;

public class DoRIAddRem implements Action
{
	private String strClassName = "DoRIAddRem";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			Log.printVerbose(strClassName + ": formName = null");
			fnGetInitialParams(servlet, req, res);
			return new ActionRouter("erm-remuneration-addrem-item-page");
		}
		if (formName.equals("addRI"))
		{
			Log.printVerbose(strClassName + ": formName = addRI");
			fnGetInitialParams(servlet, req, res);
			fnAdd(servlet, req, res);
		}
		if (formName.equals("deactRemItemType"))
		{
			Log.printVerbose(strClassName + ": formName = deactRemItemType");
			fnDeAct(servlet, req, res);
		}
		if (formName.equals("activateRemItemType"))
		{
			Log.printVerbose(strClassName + ": formName = activateCustAcc");
			fnActivate(servlet, req, res);
		}
		fnGetInitialParams(servlet, req, res);
		return new ActionRouter("erm-remuneration-addrem-item-page");
	}

	/*
	 * protected void fnRemove(HttpServlet servlet, HttpServletRequest req,
	 * HttpServletResponse res) { String rmCustAccountCode =
	 * (String)req.getParameter("removeCustAccount"); if(rmCustAccountCode !=
	 * null) { CustAccount lCustAccountCode =
	 * CustAccountNut.getObjectByCode(rmCustAccountCode); if(lCustAccountCode
	 * !=null) { try { lCustAccountCode.remove(); } catch(Exception ex) {
	 * Log.printDebug("Remove CustAccount Failed" + ex.getMessage()); } // end
	 * try-catch } // end if } // end if } // end fnRmCustAccount
	 */
	protected void fnGetInitialParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetInitialParams()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		// Need the following :
		// 1. "itrAllRI" -- for the display of ALL Type of Remuneration Item
		// 2. "itrActiveRI" -- for the display of ALL Active Remuneration Item
		// Type
		// 3. "itrPassiveRI" -- for the display of ALL Passive Type of
		// Remuneration Item
		// ----------------------- 1. itrAllRI --------------------------
		EmpRemunerationItemTypeHome lRIHome = EmpRemunerationItemTypeNut.getHome();
		Collection colAllRI = null;
		try
		{
			colAllRI = lRIHome.findAllObjects();
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("Error msg " + ex.getMessage());
		}
		Iterator itrAllRI = colAllRI.iterator();
		Log.printVerbose("Setting attribute itrAllRI now");
		req.setAttribute("itrAllRI", itrAllRI);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
		// -------------------------- end of 1. -------------------------
		// ------------------------- 2. itrActiveRI ---------------
		Collection colActiveRemItemType = EmpRemunerationItemTypeNut.getActiveObjects();
		Iterator itrActiveRI = colActiveRemItemType.iterator();
		req.setAttribute("itrActiveRI", itrActiveRI);
		Log.printVerbose("Setting attribute itrActiveRI now");
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
		// -------------------------- end of 2. -----------------------------
		// ----------------------- 3. itrPassiveRI ---------------------------
		Collection colInactiveRemItemType = EmpRemunerationItemTypeNut.getInactiveObjects();
		Iterator itrInactiveRI = colInactiveRemItemType.iterator();
		Log.printVerbose("Setting attribute itrInactiveRI now");
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
		req.setAttribute("itrPassiveRI", itrInactiveRI);
		// ------------------------- end of 3. -----------------------------
	}

	protected void fnAdd(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAdd()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		String itemNo = (String) req.getParameter("itemNo");
		String itemName = (String) req.getParameter("itemName");
		String Description = (String) req.getParameter("Description");
		String strEffDate = (String) req.getParameter("effective_date");
		String glcode = (String) req.getParameter("Glcode");
		// Print results obtained from getParameter()
		String params = "itemName = " + itemName + "\n";
		params += "Description = " + Description + "\n";
		// params += "Effective Date = " + EffectiveDate;
		Log.printVerbose(strClassName + ": Params = \n" + params);
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		if (lUsr == null)
			Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
		if (itemName == null)
			return;
		if (Description == null)
			return;
		// if(glcode == null)
		// return ;
		// Convert from String to Enum
		Integer enumGlcode = new Integer(glcode);
		// Ensure Remuneration Item does not exist
		// EmpRemunerationItemType lRI =
		// EmpRemunerationItemTypeNut.getObjectByCode(itemName);
		// if(lRI == null && lUsr != null)
		if (lUsr != null && itemName != null)
		{
			Log.printVerbose("Adding new Remuneration Item");
			EmpRemunerationItemTypeHome lEmpRemunerationItemTypeH = EmpRemunerationItemTypeNut.getHome();
			java.util.Date ldt = new java.util.Date();
			Timestamp tsCreate = new Timestamp(ldt.getTime());
			Timestamp tsEffDate = new Timestamp(ldt.getTime());
			tsEffDate = TimeFormat.createTimeStamp(strEffDate);
			Integer usrid = null;
			String type = "Nil";
			try
			{
				usrid = lUsr.getUserId();
			} catch (Exception ex)
			{
				Log.printAudit("User does not exist: " + ex.getMessage());
			}
			// Create the Remuneration Item
			try
			{
				lEmpRemunerationItemTypeH.create(itemName, Description, tsEffDate, tsCreate, usrid, enumGlcode);
			} catch (Exception ex)
			{
				Log.printDebug("Cannot create Remuneration Item Type because " + ex.getMessage());
			}
		}
		// fnGetInitialParams(servlet, req, res);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
	}

	protected void fnDeAct(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnDeAct()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		String RemItemType = (String) req.getParameter("deactivate");
		if (RemItemType != null)
		{
			Integer intRemItemType = new Integer(RemItemType);
			if (!EmpRemunerationItemTypeNut.deactRemItemType(intRemItemType))
			{
				Log.printDebug("Failed to deactivate remuneration item type: " + RemItemType);
			} else
			{
				Log.printVerbose("Successfully deactivated remuneration item type: " + RemItemType);
			}
		}
	}

	protected void fnActivate(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnActivate()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		String RemItemType = (String) req.getParameter("activate");
		if (RemItemType != null)
		{
			Integer intRemItemType = new Integer(RemItemType);
			if (!EmpRemunerationItemTypeNut.activateRemItemType(intRemItemType))
			{
				Log.printDebug("Failed to activate Rem Item Type: " + RemItemType);
			} else
			{
				Log.printVerbose("Successfully activated Rem Item Type: " + RemItemType);
			}
		}
	}
}
package com.vlee.servlet.employee;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.employee.*;

public class DoRIAddRem implements Action
{
	private String strClassName = "DoRIAddRem";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			Log.printVerbose(strClassName + ": formName = null");
			fnGetInitialParams(servlet, req, res);
			return new ActionRouter("erm-remuneration-addrem-item-page");
		}
		if (formName.equals("addRI"))
		{
			Log.printVerbose(strClassName + ": formName = addRI");
			fnGetInitialParams(servlet, req, res);
			fnAdd(servlet, req, res);
		}
		if (formName.equals("deactRemItemType"))
		{
			Log.printVerbose(strClassName + ": formName = deactRemItemType");
			fnDeAct(servlet, req, res);
		}
		if (formName.equals("activateRemItemType"))
		{
			Log.printVerbose(strClassName + ": formName = activateCustAcc");
			fnActivate(servlet, req, res);
		}
		fnGetInitialParams(servlet, req, res);
		return new ActionRouter("erm-remuneration-addrem-item-page");
	}

	/*
	 * protected void fnRemove(HttpServlet servlet, HttpServletRequest req,
	 * HttpServletResponse res) { String rmCustAccountCode =
	 * (String)req.getParameter("removeCustAccount"); if(rmCustAccountCode !=
	 * null) { CustAccount lCustAccountCode =
	 * CustAccountNut.getObjectByCode(rmCustAccountCode); if(lCustAccountCode
	 * !=null) { try { lCustAccountCode.remove(); } catch(Exception ex) {
	 * Log.printDebug("Remove CustAccount Failed" + ex.getMessage()); } // end
	 * try-catch } // end if } // end if } // end fnRmCustAccount
	 */
	protected void fnGetInitialParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetInitialParams()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		// Need the following :
		// 1. "itrAllRI" -- for the display of ALL Type of Remuneration Item
		// 2. "itrActiveRI" -- for the display of ALL Active Remuneration Item
		// Type
		// 3. "itrPassiveRI" -- for the display of ALL Passive Type of
		// Remuneration Item
		// ----------------------- 1. itrAllRI --------------------------
		EmpRemunerationItemTypeHome lRIHome = EmpRemunerationItemTypeNut.getHome();
		Collection colAllRI = null;
		try
		{
			colAllRI = lRIHome.findAllObjects();
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("Error msg " + ex.getMessage());
		}
		Iterator itrAllRI = colAllRI.iterator();
		Log.printVerbose("Setting attribute itrAllRI now");
		req.setAttribute("itrAllRI", itrAllRI);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
		// -------------------------- end of 1. -------------------------
		// ------------------------- 2. itrActiveRI ---------------
		Collection colActiveRemItemType = EmpRemunerationItemTypeNut.getActiveObjects();
		Iterator itrActiveRI = colActiveRemItemType.iterator();
		req.setAttribute("itrActiveRI", itrActiveRI);
		Log.printVerbose("Setting attribute itrActiveRI now");
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
		// -------------------------- end of 2. -----------------------------
		// ----------------------- 3. itrPassiveRI ---------------------------
		Collection colInactiveRemItemType = EmpRemunerationItemTypeNut.getInactiveObjects();
		Iterator itrInactiveRI = colInactiveRemItemType.iterator();
		Log.printVerbose("Setting attribute itrInactiveRI now");
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
		req.setAttribute("itrPassiveRI", itrInactiveRI);
		// ------------------------- end of 3. -----------------------------
	}

	protected void fnAdd(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAdd()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		String itemNo = (String) req.getParameter("itemNo");
		String itemName = (String) req.getParameter("itemName");
		String Description = (String) req.getParameter("Description");
		String strEffDate = (String) req.getParameter("effective_date");
		String glcode = (String) req.getParameter("Glcode");
		// Print results obtained from getParameter()
		String params = "itemName = " + itemName + "\n";
		params += "Description = " + Description + "\n";
		// params += "Effective Date = " + EffectiveDate;
		Log.printVerbose(strClassName + ": Params = \n" + params);
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		if (lUsr == null)
			Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
		if (itemName == null)
			return;
		if (Description == null)
			return;
		// if(glcode == null)
		// return ;
		// Convert from String to Enum
		Integer enumGlcode = new Integer(glcode);
		// Ensure Remuneration Item does not exist
		// EmpRemunerationItemType lRI =
		// EmpRemunerationItemTypeNut.getObjectByCode(itemName);
		// if(lRI == null && lUsr != null)
		if (lUsr != null && itemName != null)
		{
			Log.printVerbose("Adding new Remuneration Item");
			EmpRemunerationItemTypeHome lEmpRemunerationItemTypeH = EmpRemunerationItemTypeNut.getHome();
			java.util.Date ldt = new java.util.Date();
			Timestamp tsCreate = new Timestamp(ldt.getTime());
			Timestamp tsEffDate = new Timestamp(ldt.getTime());
			tsEffDate = TimeFormat.createTimeStamp(strEffDate);
			Integer usrid = null;
			String type = "Nil";
			try
			{
				usrid = lUsr.getUserId();
			} catch (Exception ex)
			{
				Log.printAudit("User does not exist: " + ex.getMessage());
			}
			// Create the Remuneration Item
			try
			{
				lEmpRemunerationItemTypeH.create(itemName, Description, tsEffDate, tsCreate, usrid, enumGlcode);
			} catch (Exception ex)
			{
				Log.printDebug("Cannot create Remuneration Item Type because " + ex.getMessage());
			}
		}
		// fnGetInitialParams(servlet, req, res);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
	}

	protected void fnDeAct(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnDeAct()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		String RemItemType = (String) req.getParameter("deactivate");
		if (RemItemType != null)
		{
			Integer intRemItemType = new Integer(RemItemType);
			if (!EmpRemunerationItemTypeNut.deactRemItemType(intRemItemType))
			{
				Log.printDebug("Failed to deactivate remuneration item type: " + RemItemType);
			} else
			{
				Log.printVerbose("Successfully deactivated remuneration item type: " + RemItemType);
			}
		}
	}

	protected void fnActivate(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnActivate()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		String RemItemType = (String) req.getParameter("activate");
		if (RemItemType != null)
		{
			Integer intRemItemType = new Integer(RemItemType);
			if (!EmpRemunerationItemTypeNut.activateRemItemType(intRemItemType))
			{
				Log.printDebug("Failed to activate Rem Item Type: " + RemItemType);
			} else
			{
				Log.printVerbose("Successfully activated Rem Item Type: " + RemItemType);
			}
		}
	}
}
package com.vlee.servlet.employee;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.employee.*;

public class DoRIAddRem implements Action
{
	private String strClassName = "DoRIAddRem";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			Log.printVerbose(strClassName + ": formName = null");
			fnGetInitialParams(servlet, req, res);
			return new ActionRouter("erm-remuneration-addrem-item-page");
		}
		if (formName.equals("addRI"))
		{
			Log.printVerbose(strClassName + ": formName = addRI");
			fnGetInitialParams(servlet, req, res);
			fnAdd(servlet, req, res);
		}
		if (formName.equals("deactRemItemType"))
		{
			Log.printVerbose(strClassName + ": formName = deactRemItemType");
			fnDeAct(servlet, req, res);
		}
		if (formName.equals("activateRemItemType"))
		{
			Log.printVerbose(strClassName + ": formName = activateCustAcc");
			fnActivate(servlet, req, res);
		}
		fnGetInitialParams(servlet, req, res);
		return new ActionRouter("erm-remuneration-addrem-item-page");
	}

	/*
	 * protected void fnRemove(HttpServlet servlet, HttpServletRequest req,
	 * HttpServletResponse res) { String rmCustAccountCode =
	 * (String)req.getParameter("removeCustAccount"); if(rmCustAccountCode !=
	 * null) { CustAccount lCustAccountCode =
	 * CustAccountNut.getObjectByCode(rmCustAccountCode); if(lCustAccountCode
	 * !=null) { try { lCustAccountCode.remove(); } catch(Exception ex) {
	 * Log.printDebug("Remove CustAccount Failed" + ex.getMessage()); } // end
	 * try-catch } // end if } // end if } // end fnRmCustAccount
	 */
	protected void fnGetInitialParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetInitialParams()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		// Need the following :
		// 1. "itrAllRI" -- for the display of ALL Type of Remuneration Item
		// 2. "itrActiveRI" -- for the display of ALL Active Remuneration Item
		// Type
		// 3. "itrPassiveRI" -- for the display of ALL Passive Type of
		// Remuneration Item
		// ----------------------- 1. itrAllRI --------------------------
		EmpRemunerationItemTypeHome lRIHome = EmpRemunerationItemTypeNut.getHome();
		Collection colAllRI = null;
		try
		{
			colAllRI = lRIHome.findAllObjects();
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("Error msg " + ex.getMessage());
		}
		Iterator itrAllRI = colAllRI.iterator();
		Log.printVerbose("Setting attribute itrAllRI now");
		req.setAttribute("itrAllRI", itrAllRI);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
		// -------------------------- end of 1. -------------------------
		// ------------------------- 2. itrActiveRI ---------------
		Collection colActiveRemItemType = EmpRemunerationItemTypeNut.getActiveObjects();
		Iterator itrActiveRI = colActiveRemItemType.iterator();
		req.setAttribute("itrActiveRI", itrActiveRI);
		Log.printVerbose("Setting attribute itrActiveRI now");
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
		// -------------------------- end of 2. -----------------------------
		// ----------------------- 3. itrPassiveRI ---------------------------
		Collection colInactiveRemItemType = EmpRemunerationItemTypeNut.getInactiveObjects();
		Iterator itrInactiveRI = colInactiveRemItemType.iterator();
		Log.printVerbose("Setting attribute itrInactiveRI now");
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
		req.setAttribute("itrPassiveRI", itrInactiveRI);
		// ------------------------- end of 3. -----------------------------
	}

	protected void fnAdd(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAdd()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		String itemNo = (String) req.getParameter("itemNo");
		String itemName = (String) req.getParameter("itemName");
		String Description = (String) req.getParameter("Description");
		String strEffDate = (String) req.getParameter("effective_date");
		String glcode = (String) req.getParameter("Glcode");
		// Print results obtained from getParameter()
		String params = "itemName = " + itemName + "\n";
		params += "Description = " + Description + "\n";
		// params += "Effective Date = " + EffectiveDate;
		Log.printVerbose(strClassName + ": Params = \n" + params);
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		if (lUsr == null)
			Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
		if (itemName == null)
			return;
		if (Description == null)
			return;
		// if(glcode == null)
		// return ;
		// Convert from String to Enum
		Integer enumGlcode = new Integer(glcode);
		// Ensure Remuneration Item does not exist
		// EmpRemunerationItemType lRI =
		// EmpRemunerationItemTypeNut.getObjectByCode(itemName);
		// if(lRI == null && lUsr != null)
		if (lUsr != null && itemName != null)
		{
			Log.printVerbose("Adding new Remuneration Item");
			EmpRemunerationItemTypeHome lEmpRemunerationItemTypeH = EmpRemunerationItemTypeNut.getHome();
			java.util.Date ldt = new java.util.Date();
			Timestamp tsCreate = new Timestamp(ldt.getTime());
			Timestamp tsEffDate = new Timestamp(ldt.getTime());
			tsEffDate = TimeFormat.createTimeStamp(strEffDate);
			Integer usrid = null;
			String type = "Nil";
			try
			{
				usrid = lUsr.getUserId();
			} catch (Exception ex)
			{
				Log.printAudit("User does not exist: " + ex.getMessage());
			}
			// Create the Remuneration Item
			try
			{
				lEmpRemunerationItemTypeH.create(itemName, Description, tsEffDate, tsCreate, usrid, enumGlcode);
			} catch (Exception ex)
			{
				Log.printDebug("Cannot create Remuneration Item Type because " + ex.getMessage());
			}
		}
		// fnGetInitialParams(servlet, req, res);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
	}

	protected void fnDeAct(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnDeAct()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		String RemItemType = (String) req.getParameter("deactivate");
		if (RemItemType != null)
		{
			Integer intRemItemType = new Integer(RemItemType);
			if (!EmpRemunerationItemTypeNut.deactRemItemType(intRemItemType))
			{
				Log.printDebug("Failed to deactivate remuneration item type: " + RemItemType);
			} else
			{
				Log.printVerbose("Successfully deactivated remuneration item type: " + RemItemType);
			}
		}
	}

	protected void fnActivate(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnActivate()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		String RemItemType = (String) req.getParameter("activate");
		if (RemItemType != null)
		{
			Integer intRemItemType = new Integer(RemItemType);
			if (!EmpRemunerationItemTypeNut.activateRemItemType(intRemItemType))
			{
				Log.printDebug("Failed to activate Rem Item Type: " + RemItemType);
			} else
			{
				Log.printVerbose("Successfully activated Rem Item Type: " + RemItemType);
			}
		}
	}
}
