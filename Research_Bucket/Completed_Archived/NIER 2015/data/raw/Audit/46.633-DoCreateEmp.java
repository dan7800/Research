package com.vlee.servlet.employee;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.employee.*;

public class DoCreateEmp implements Action
{
	private String strClassName = "DoCreateEmp";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			Log.printVerbose(strClassName + ": formName = null");
			fnGetInitialParams(servlet, req, res);
			return new ActionRouter("erm-create-employee-page");
		}
		if (formName.equals("createEmp"))
		{
			Log.printVerbose(strClassName + ": formName = createEmp");
			fnGetInitialParams(servlet, req, res);
			fnAdd(servlet, req, res);
		}
		if (formName.equals("deactEmp"))
		{
			Log.printVerbose(strClassName + ": formName = deactEmp");
			fnDeAct(servlet, req, res);
		}
		if (formName.equals("activateEmp"))
		{
			Log.printVerbose(strClassName + ": formName = activateEmp");
			fnActivate(servlet, req, res);
		}
		fnGetInitialParams(servlet, req, res);
		return new ActionRouter("erm-create-employee-page");
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
		// 1. "itrAllEmp" -- for the display of ALL Employee
		// 2. "itrActiveEmp" -- for the display of ALL Active Employee
		// 3. "itrPassiveEmp" -- for the display of ALL Passive(ex) Employee
		// ----------------------- 1. itrAllEmp --------------------------
		EmpDetailsHome lEmpHome = EmpDetailsNut.getHome();
		Collection colAllEmp = null;
		try
		{
			colAllEmp = lEmpHome.findAllObjects();
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("Error msg " + ex.getMessage());
		}
		Iterator itrAllEmp = colAllEmp.iterator();
		Log.printVerbose("Setting attribute itrAllEmp now");
		req.setAttribute("itrAllEmp", itrAllEmp);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
		// -------------------------- end of 1. -------------------------
		// ------------------------- 2. itrActiveEmp ---------------
		Collection colActiveEmp = EmpDetailsNut.getActiveObjects();
		Iterator itrActiveEmp = colActiveEmp.iterator();
		req.setAttribute("itrActiveEmp", itrActiveEmp);
		Log.printVerbose("Setting attribute itrActiveEmp now");
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
		// -------------------------- end of 2. -----------------------------
		// ----------------------- 3. itrPassiveEmp ---------------------------
		Collection colInactiveEmp = EmpDetailsNut.getInactiveObjects();
		Iterator itrInactiveEmp = colInactiveEmp.iterator();
		Log.printVerbose("Setting attribute itrInactiveEmp now");
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
		req.setAttribute("itrPassiveEmp", itrInactiveEmp);
		// ------------------------- end of 3. -----------------------------
	}

	protected void fnAdd(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAdd()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		String staffId = (String) req.getParameter("staffId");
		String firstName = (String) req.getParameter("firstName");
		String lastName = (String) req.getParameter("lastName");
		String pccenterid = (String) req.getParameter("pccenterid");
		// String dob = (String) req.getParameter("dob");
		String sex = (String) req.getParameter("sex");
		String ICNo = (String) req.getParameter("ICNo");
		String strEffDate = (String) req.getParameter("effective_date");
		String dateJoin = (String) req.getParameter("dateJoin");
		// Print results obtained from getParameter()
		// String params = "itemName = " + itemName + "\n";
		// params += "Description = " + Description + "\n";
		// params += "Effective Date = " + EffectiveDate;
		// Log.printVerbose(strClassName + ": Params = \n" + params );
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		if (lUsr == null)
			Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
		if (firstName == null)
			return;
		// if(lastName == null)
		// return ;
		// if(glcode == null)
		// return ;
		// Convert from String to Enum
		// Integer enumPccenterid = new Integer(pccenterid);
		Integer enumPccenterid = new Integer(0);
		// if(lRI == null && lUsr != null)
		if (lUsr != null && firstName != null)
		{
			Log.printVerbose("Adding new Employee");
			EmpDetailsHome lEmpDetailsH = EmpDetailsNut.getHome();
			java.util.Date ldt = new java.util.Date();
			Timestamp tsCreate = new Timestamp(ldt.getTime());
			Timestamp tsUpdate = new Timestamp(ldt.getTime());
			Timestamp tsDateJoin = new Timestamp(ldt.getTime());
			Timestamp tsDateLeft = new Timestamp(ldt.getTime());
			tsUpdate = TimeFormat.createTimeStamp(strEffDate);
			tsDateJoin = TimeFormat.createTimeStamp(dateJoin);
			tsDateLeft = TimeFormat.createTimeStamp("9999-1-1");
			Integer usrid = null;
			// Unused parameters
			String name3 = " ";
			String name4 = " ";
			String ethnic = "unknown";
			String ICType = " ";
			String nationality = " ";
			String addr1 = " ";
			String addr2 = " ";
			String addr3 = " ";
			String countryCode = " ";
			Integer userId = new Integer(0); // onlly if employee is user of
												// the system as well
			String bank_acc = " ";
			String bank1 = " ";
			String bank2 = " ";
			String field1 = " ";
			String field2 = " ";
			String field3 = " ";
			String zip = " ";
			String state = " ";
			try
			{
				usrid = lUsr.getUserId();
			} catch (Exception ex)
			{
				Log.printAudit("User does not exist: " + ex.getMessage());
			}
			// Create the Employee
			try
			{
				lEmpDetailsH.create(staffId, firstName, lastName, name3, name4, new GregorianCalendar(1970, 0, 1), sex,
						ethnic, ICNo, ICType, nationality, addr1, addr2, addr3, zip, state, countryCode, userId,
						tsUpdate, tsCreate, usrid, enumPccenterid, bank_acc, bank1, bank2, field1, field2, field3,
						tsDateJoin, tsDateLeft);
			} catch (Exception ex)
			{
				Log.printDebug("Cannot create Employee Details because " + ex.getMessage());
			}
		}
		// fnGetInitialParams(servlet, req, res);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
	}

	protected void fnDeAct(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnDeAct()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		String EmpPkid = (String) req.getParameter("deactivate");
		if (EmpPkid != null)
		{
			Integer intEmpPkid = new Integer(EmpPkid);
			if (!EmpDetailsNut.deactEmpDetails(intEmpPkid))
			{
				Log.printDebug("Failed to deactivate Employee type: " + EmpPkid);
			} else
			{
				Log.printVerbose("Successfully deactivated Employee: " + EmpPkid);
			}
		}
	}

	protected void fnActivate(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnActivate()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		String EmpPkid = (String) req.getParameter("activate");
		if (EmpPkid != null)
		{
			Integer intEmpPkid = new Integer(EmpPkid);
			if (!EmpDetailsNut.activateEmpDetails(intEmpPkid))
			{
				Log.printDebug("Failed to activate Employee: " + EmpPkid);
			} else
			{
				Log.printVerbose("Successfully activated Employee: " + EmpPkid);
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

public class DoCreateEmp implements Action
{
	private String strClassName = "DoCreateEmp";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			Log.printVerbose(strClassName + ": formName = null");
			fnGetInitialParams(servlet, req, res);
			return new ActionRouter("erm-create-employee-page");
		}
		if (formName.equals("createEmp"))
		{
			Log.printVerbose(strClassName + ": formName = createEmp");
			fnGetInitialParams(servlet, req, res);
			fnAdd(servlet, req, res);
		}
		if (formName.equals("deactEmp"))
		{
			Log.printVerbose(strClassName + ": formName = deactEmp");
			fnDeAct(servlet, req, res);
		}
		if (formName.equals("activateEmp"))
		{
			Log.printVerbose(strClassName + ": formName = activateEmp");
			fnActivate(servlet, req, res);
		}
		fnGetInitialParams(servlet, req, res);
		return new ActionRouter("erm-create-employee-page");
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
		// 1. "itrAllEmp" -- for the display of ALL Employee
		// 2. "itrActiveEmp" -- for the display of ALL Active Employee
		// 3. "itrPassiveEmp" -- for the display of ALL Passive(ex) Employee
		// ----------------------- 1. itrAllEmp --------------------------
		EmpDetailsHome lEmpHome = EmpDetailsNut.getHome();
		Collection colAllEmp = null;
		try
		{
			colAllEmp = lEmpHome.findAllObjects();
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("Error msg " + ex.getMessage());
		}
		Iterator itrAllEmp = colAllEmp.iterator();
		Log.printVerbose("Setting attribute itrAllEmp now");
		req.setAttribute("itrAllEmp", itrAllEmp);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
		// -------------------------- end of 1. -------------------------
		// ------------------------- 2. itrActiveEmp ---------------
		Collection colActiveEmp = EmpDetailsNut.getActiveObjects();
		Iterator itrActiveEmp = colActiveEmp.iterator();
		req.setAttribute("itrActiveEmp", itrActiveEmp);
		Log.printVerbose("Setting attribute itrActiveEmp now");
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
		// -------------------------- end of 2. -----------------------------
		// ----------------------- 3. itrPassiveEmp ---------------------------
		Collection colInactiveEmp = EmpDetailsNut.getInactiveObjects();
		Iterator itrInactiveEmp = colInactiveEmp.iterator();
		Log.printVerbose("Setting attribute itrInactiveEmp now");
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
		req.setAttribute("itrPassiveEmp", itrInactiveEmp);
		// ------------------------- end of 3. -----------------------------
	}

	protected void fnAdd(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAdd()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		String staffId = (String) req.getParameter("staffId");
		String firstName = (String) req.getParameter("firstName");
		String lastName = (String) req.getParameter("lastName");
		String pccenterid = (String) req.getParameter("pccenterid");
		// String dob = (String) req.getParameter("dob");
		String sex = (String) req.getParameter("sex");
		String ICNo = (String) req.getParameter("ICNo");
		String strEffDate = (String) req.getParameter("effective_date");
		String dateJoin = (String) req.getParameter("dateJoin");
		// Print results obtained from getParameter()
		// String params = "itemName = " + itemName + "\n";
		// params += "Description = " + Description + "\n";
		// params += "Effective Date = " + EffectiveDate;
		// Log.printVerbose(strClassName + ": Params = \n" + params );
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		if (lUsr == null)
			Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
		if (firstName == null)
			return;
		// if(lastName == null)
		// return ;
		// if(glcode == null)
		// return ;
		// Convert from String to Enum
		// Integer enumPccenterid = new Integer(pccenterid);
		Integer enumPccenterid = new Integer(0);
		// if(lRI == null && lUsr != null)
		if (lUsr != null && firstName != null)
		{
			Log.printVerbose("Adding new Employee");
			EmpDetailsHome lEmpDetailsH = EmpDetailsNut.getHome();
			java.util.Date ldt = new java.util.Date();
			Timestamp tsCreate = new Timestamp(ldt.getTime());
			Timestamp tsUpdate = new Timestamp(ldt.getTime());
			Timestamp tsDateJoin = new Timestamp(ldt.getTime());
			Timestamp tsDateLeft = new Timestamp(ldt.getTime());
			tsUpdate = TimeFormat.createTimeStamp(strEffDate);
			tsDateJoin = TimeFormat.createTimeStamp(dateJoin);
			tsDateLeft = TimeFormat.createTimeStamp("9999-1-1");
			Integer usrid = null;
			// Unused parameters
			String name3 = " ";
			String name4 = " ";
			String ethnic = "unknown";
			String ICType = " ";
			String nationality = " ";
			String addr1 = " ";
			String addr2 = " ";
			String addr3 = " ";
			String countryCode = " ";
			Integer userId = new Integer(0); // onlly if employee is user of
												// the system as well
			String bank_acc = " ";
			String bank1 = " ";
			String bank2 = " ";
			String field1 = " ";
			String field2 = " ";
			String field3 = " ";
			String zip = " ";
			String state = " ";
			try
			{
				usrid = lUsr.getUserId();
			} catch (Exception ex)
			{
				Log.printAudit("User does not exist: " + ex.getMessage());
			}
			// Create the Employee
			try
			{
				lEmpDetailsH.create(staffId, firstName, lastName, name3, name4, new GregorianCalendar(1970, 0, 1), sex,
						ethnic, ICNo, ICType, nationality, addr1, addr2, addr3, zip, state, countryCode, userId,
						tsUpdate, tsCreate, usrid, enumPccenterid, bank_acc, bank1, bank2, field1, field2, field3,
						tsDateJoin, tsDateLeft);
			} catch (Exception ex)
			{
				Log.printDebug("Cannot create Employee Details because " + ex.getMessage());
			}
		}
		// fnGetInitialParams(servlet, req, res);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
	}

	protected void fnDeAct(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnDeAct()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		String EmpPkid = (String) req.getParameter("deactivate");
		if (EmpPkid != null)
		{
			Integer intEmpPkid = new Integer(EmpPkid);
			if (!EmpDetailsNut.deactEmpDetails(intEmpPkid))
			{
				Log.printDebug("Failed to deactivate Employee type: " + EmpPkid);
			} else
			{
				Log.printVerbose("Successfully deactivated Employee: " + EmpPkid);
			}
		}
	}

	protected void fnActivate(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnActivate()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		String EmpPkid = (String) req.getParameter("activate");
		if (EmpPkid != null)
		{
			Integer intEmpPkid = new Integer(EmpPkid);
			if (!EmpDetailsNut.activateEmpDetails(intEmpPkid))
			{
				Log.printDebug("Failed to activate Employee: " + EmpPkid);
			} else
			{
				Log.printVerbose("Successfully activated Employee: " + EmpPkid);
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

public class DoCreateEmp implements Action
{
	private String strClassName = "DoCreateEmp";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			Log.printVerbose(strClassName + ": formName = null");
			fnGetInitialParams(servlet, req, res);
			return new ActionRouter("erm-create-employee-page");
		}
		if (formName.equals("createEmp"))
		{
			Log.printVerbose(strClassName + ": formName = createEmp");
			fnGetInitialParams(servlet, req, res);
			fnAdd(servlet, req, res);
		}
		if (formName.equals("deactEmp"))
		{
			Log.printVerbose(strClassName + ": formName = deactEmp");
			fnDeAct(servlet, req, res);
		}
		if (formName.equals("activateEmp"))
		{
			Log.printVerbose(strClassName + ": formName = activateEmp");
			fnActivate(servlet, req, res);
		}
		fnGetInitialParams(servlet, req, res);
		return new ActionRouter("erm-create-employee-page");
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
		// 1. "itrAllEmp" -- for the display of ALL Employee
		// 2. "itrActiveEmp" -- for the display of ALL Active Employee
		// 3. "itrPassiveEmp" -- for the display of ALL Passive(ex) Employee
		// ----------------------- 1. itrAllEmp --------------------------
		EmpDetailsHome lEmpHome = EmpDetailsNut.getHome();
		Collection colAllEmp = null;
		try
		{
			colAllEmp = lEmpHome.findAllObjects();
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("Error msg " + ex.getMessage());
		}
		Iterator itrAllEmp = colAllEmp.iterator();
		Log.printVerbose("Setting attribute itrAllEmp now");
		req.setAttribute("itrAllEmp", itrAllEmp);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
		// -------------------------- end of 1. -------------------------
		// ------------------------- 2. itrActiveEmp ---------------
		Collection colActiveEmp = EmpDetailsNut.getActiveObjects();
		Iterator itrActiveEmp = colActiveEmp.iterator();
		req.setAttribute("itrActiveEmp", itrActiveEmp);
		Log.printVerbose("Setting attribute itrActiveEmp now");
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
		// -------------------------- end of 2. -----------------------------
		// ----------------------- 3. itrPassiveEmp ---------------------------
		Collection colInactiveEmp = EmpDetailsNut.getInactiveObjects();
		Iterator itrInactiveEmp = colInactiveEmp.iterator();
		Log.printVerbose("Setting attribute itrInactiveEmp now");
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
		req.setAttribute("itrPassiveEmp", itrInactiveEmp);
		// ------------------------- end of 3. -----------------------------
	}

	protected void fnAdd(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAdd()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		String staffId = (String) req.getParameter("staffId");
		String firstName = (String) req.getParameter("firstName");
		String lastName = (String) req.getParameter("lastName");
		String pccenterid = (String) req.getParameter("pccenterid");
		// String dob = (String) req.getParameter("dob");
		String sex = (String) req.getParameter("sex");
		String ICNo = (String) req.getParameter("ICNo");
		String strEffDate = (String) req.getParameter("effective_date");
		String dateJoin = (String) req.getParameter("dateJoin");
		// Print results obtained from getParameter()
		// String params = "itemName = " + itemName + "\n";
		// params += "Description = " + Description + "\n";
		// params += "Effective Date = " + EffectiveDate;
		// Log.printVerbose(strClassName + ": Params = \n" + params );
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		if (lUsr == null)
			Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
		if (firstName == null)
			return;
		// if(lastName == null)
		// return ;
		// if(glcode == null)
		// return ;
		// Convert from String to Enum
		// Integer enumPccenterid = new Integer(pccenterid);
		Integer enumPccenterid = new Integer(0);
		// if(lRI == null && lUsr != null)
		if (lUsr != null && firstName != null)
		{
			Log.printVerbose("Adding new Employee");
			EmpDetailsHome lEmpDetailsH = EmpDetailsNut.getHome();
			java.util.Date ldt = new java.util.Date();
			Timestamp tsCreate = new Timestamp(ldt.getTime());
			Timestamp tsUpdate = new Timestamp(ldt.getTime());
			Timestamp tsDateJoin = new Timestamp(ldt.getTime());
			Timestamp tsDateLeft = new Timestamp(ldt.getTime());
			tsUpdate = TimeFormat.createTimeStamp(strEffDate);
			tsDateJoin = TimeFormat.createTimeStamp(dateJoin);
			tsDateLeft = TimeFormat.createTimeStamp("9999-1-1");
			Integer usrid = null;
			// Unused parameters
			String name3 = " ";
			String name4 = " ";
			String ethnic = "unknown";
			String ICType = " ";
			String nationality = " ";
			String addr1 = " ";
			String addr2 = " ";
			String addr3 = " ";
			String countryCode = " ";
			Integer userId = new Integer(0); // onlly if employee is user of
												// the system as well
			String bank_acc = " ";
			String bank1 = " ";
			String bank2 = " ";
			String field1 = " ";
			String field2 = " ";
			String field3 = " ";
			String zip = " ";
			String state = " ";
			try
			{
				usrid = lUsr.getUserId();
			} catch (Exception ex)
			{
				Log.printAudit("User does not exist: " + ex.getMessage());
			}
			// Create the Employee
			try
			{
				lEmpDetailsH.create(staffId, firstName, lastName, name3, name4, new GregorianCalendar(1970, 0, 1), sex,
						ethnic, ICNo, ICType, nationality, addr1, addr2, addr3, zip, state, countryCode, userId,
						tsUpdate, tsCreate, usrid, enumPccenterid, bank_acc, bank1, bank2, field1, field2, field3,
						tsDateJoin, tsDateLeft);
			} catch (Exception ex)
			{
				Log.printDebug("Cannot create Employee Details because " + ex.getMessage());
			}
		}
		// fnGetInitialParams(servlet, req, res);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
	}

	protected void fnDeAct(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnDeAct()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		String EmpPkid = (String) req.getParameter("deactivate");
		if (EmpPkid != null)
		{
			Integer intEmpPkid = new Integer(EmpPkid);
			if (!EmpDetailsNut.deactEmpDetails(intEmpPkid))
			{
				Log.printDebug("Failed to deactivate Employee type: " + EmpPkid);
			} else
			{
				Log.printVerbose("Successfully deactivated Employee: " + EmpPkid);
			}
		}
	}

	protected void fnActivate(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnActivate()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		String EmpPkid = (String) req.getParameter("activate");
		if (EmpPkid != null)
		{
			Integer intEmpPkid = new Integer(EmpPkid);
			if (!EmpDetailsNut.activateEmpDetails(intEmpPkid))
			{
				Log.printDebug("Failed to activate Employee: " + EmpPkid);
			} else
			{
				Log.printVerbose("Successfully activated Employee: " + EmpPkid);
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

public class DoCreateEmp implements Action
{
	private String strClassName = "DoCreateEmp";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			Log.printVerbose(strClassName + ": formName = null");
			fnGetInitialParams(servlet, req, res);
			return new ActionRouter("erm-create-employee-page");
		}
		if (formName.equals("createEmp"))
		{
			Log.printVerbose(strClassName + ": formName = createEmp");
			fnGetInitialParams(servlet, req, res);
			fnAdd(servlet, req, res);
		}
		if (formName.equals("deactEmp"))
		{
			Log.printVerbose(strClassName + ": formName = deactEmp");
			fnDeAct(servlet, req, res);
		}
		if (formName.equals("activateEmp"))
		{
			Log.printVerbose(strClassName + ": formName = activateEmp");
			fnActivate(servlet, req, res);
		}
		fnGetInitialParams(servlet, req, res);
		return new ActionRouter("erm-create-employee-page");
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
		// 1. "itrAllEmp" -- for the display of ALL Employee
		// 2. "itrActiveEmp" -- for the display of ALL Active Employee
		// 3. "itrPassiveEmp" -- for the display of ALL Passive(ex) Employee
		// ----------------------- 1. itrAllEmp --------------------------
		EmpDetailsHome lEmpHome = EmpDetailsNut.getHome();
		Collection colAllEmp = null;
		try
		{
			colAllEmp = lEmpHome.findAllObjects();
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("Error msg " + ex.getMessage());
		}
		Iterator itrAllEmp = colAllEmp.iterator();
		Log.printVerbose("Setting attribute itrAllEmp now");
		req.setAttribute("itrAllEmp", itrAllEmp);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
		// -------------------------- end of 1. -------------------------
		// ------------------------- 2. itrActiveEmp ---------------
		Collection colActiveEmp = EmpDetailsNut.getActiveObjects();
		Iterator itrActiveEmp = colActiveEmp.iterator();
		req.setAttribute("itrActiveEmp", itrActiveEmp);
		Log.printVerbose("Setting attribute itrActiveEmp now");
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
		// -------------------------- end of 2. -----------------------------
		// ----------------------- 3. itrPassiveEmp ---------------------------
		Collection colInactiveEmp = EmpDetailsNut.getInactiveObjects();
		Iterator itrInactiveEmp = colInactiveEmp.iterator();
		Log.printVerbose("Setting attribute itrInactiveEmp now");
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
		req.setAttribute("itrPassiveEmp", itrInactiveEmp);
		// ------------------------- end of 3. -----------------------------
	}

	protected void fnAdd(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAdd()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		String staffId = (String) req.getParameter("staffId");
		String firstName = (String) req.getParameter("firstName");
		String lastName = (String) req.getParameter("lastName");
		String pccenterid = (String) req.getParameter("pccenterid");
		// String dob = (String) req.getParameter("dob");
		String sex = (String) req.getParameter("sex");
		String ICNo = (String) req.getParameter("ICNo");
		String strEffDate = (String) req.getParameter("effective_date");
		String dateJoin = (String) req.getParameter("dateJoin");
		// Print results obtained from getParameter()
		// String params = "itemName = " + itemName + "\n";
		// params += "Description = " + Description + "\n";
		// params += "Effective Date = " + EffectiveDate;
		// Log.printVerbose(strClassName + ": Params = \n" + params );
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		if (lUsr == null)
			Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
		if (firstName == null)
			return;
		// if(lastName == null)
		// return ;
		// if(glcode == null)
		// return ;
		// Convert from String to Enum
		// Integer enumPccenterid = new Integer(pccenterid);
		Integer enumPccenterid = new Integer(0);
		// if(lRI == null && lUsr != null)
		if (lUsr != null && firstName != null)
		{
			Log.printVerbose("Adding new Employee");
			EmpDetailsHome lEmpDetailsH = EmpDetailsNut.getHome();
			java.util.Date ldt = new java.util.Date();
			Timestamp tsCreate = new Timestamp(ldt.getTime());
			Timestamp tsUpdate = new Timestamp(ldt.getTime());
			Timestamp tsDateJoin = new Timestamp(ldt.getTime());
			Timestamp tsDateLeft = new Timestamp(ldt.getTime());
			tsUpdate = TimeFormat.createTimeStamp(strEffDate);
			tsDateJoin = TimeFormat.createTimeStamp(dateJoin);
			tsDateLeft = TimeFormat.createTimeStamp("9999-1-1");
			Integer usrid = null;
			// Unused parameters
			String name3 = " ";
			String name4 = " ";
			String ethnic = "unknown";
			String ICType = " ";
			String nationality = " ";
			String addr1 = " ";
			String addr2 = " ";
			String addr3 = " ";
			String countryCode = " ";
			Integer userId = new Integer(0); // onlly if employee is user of
												// the system as well
			String bank_acc = " ";
			String bank1 = " ";
			String bank2 = " ";
			String field1 = " ";
			String field2 = " ";
			String field3 = " ";
			String zip = " ";
			String state = " ";
			try
			{
				usrid = lUsr.getUserId();
			} catch (Exception ex)
			{
				Log.printAudit("User does not exist: " + ex.getMessage());
			}
			// Create the Employee
			try
			{
				lEmpDetailsH.create(staffId, firstName, lastName, name3, name4, new GregorianCalendar(1970, 0, 1), sex,
						ethnic, ICNo, ICType, nationality, addr1, addr2, addr3, zip, state, countryCode, userId,
						tsUpdate, tsCreate, usrid, enumPccenterid, bank_acc, bank1, bank2, field1, field2, field3,
						tsDateJoin, tsDateLeft);
			} catch (Exception ex)
			{
				Log.printDebug("Cannot create Employee Details because " + ex.getMessage());
			}
		}
		// fnGetInitialParams(servlet, req, res);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
	}

	protected void fnDeAct(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnDeAct()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		String EmpPkid = (String) req.getParameter("deactivate");
		if (EmpPkid != null)
		{
			Integer intEmpPkid = new Integer(EmpPkid);
			if (!EmpDetailsNut.deactEmpDetails(intEmpPkid))
			{
				Log.printDebug("Failed to deactivate Employee type: " + EmpPkid);
			} else
			{
				Log.printVerbose("Successfully deactivated Employee: " + EmpPkid);
			}
		}
	}

	protected void fnActivate(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnActivate()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		String EmpPkid = (String) req.getParameter("activate");
		if (EmpPkid != null)
		{
			Integer intEmpPkid = new Integer(EmpPkid);
			if (!EmpDetailsNut.activateEmpDetails(intEmpPkid))
			{
				Log.printDebug("Failed to activate Employee: " + EmpPkid);
			} else
			{
				Log.printVerbose("Successfully activated Employee: " + EmpPkid);
			}
		}
	}
}
