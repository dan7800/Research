package com.vlee.servlet.customer;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.text.*;
import java.sql.*;
import java.math.BigDecimal;
import com.vlee.servlet.main.*;
import com.vlee.util.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;

public class DoPOSPkgItemAdd implements Action
{
	String strClassName = "DoPOSPkgItemAdd";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName != null)
		{
			if (formName.compareTo("addPOSPkgItem") == 0)
			{
				// add the POS Package Item
				Log.printVerbose(strClassName + ": formName = addPOSPkgItem");
				fnAddPackage(servlet, req, res);
			}
			if (formName.compareTo("remPOSPkgItem") == 0)
			{
				// remove the POS Package Item
				Log.printVerbose(strClassName + ": formName = remPOSPkgItem");
				fnRemPackage(servlet, req, res);
			}
			if (formName.compareTo("deactPOSPkgItem") == 0)
			{
				// Deactivate the POS Package Item
				Log.printVerbose(strClassName + ": formName = deactPOSPkgItem");
				fnDeactPackage(servlet, req, res);
			}
			if (formName.compareTo("actPOSPkgItem") == 0)
			{
				// Activate the POS Package Item
				Log.printVerbose(strClassName + ": formName = actPOSPkgItem");
				fnActivatePackage(servlet, req, res);
			}
		}
		fnGetParams(servlet, req, res);
		return new ActionRouter("cust-pos-add-pkg-items-page");
	}

	protected void fnGetParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetParams()";
		// 1. itrActivePackage
		Collection colActivePackage = PackageNut.getActiveObjects();
		Iterator itrActivePackage = colActivePackage.iterator();
		Log.printVerbose("Setting attribute itrActivePackage now");
		req.setAttribute("itrActivePackage", itrActivePackage);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
		// 2. itrPassivePackage
		Collection colInactivePackage = PackageNut.getInactiveObjects();
		Iterator itrInactivePackage = colInactivePackage.iterator();
		Log.printVerbose("Setting attribute itrPassivePackage now");
		req.setAttribute("itrPassivePackage", itrInactivePackage);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
	}

	protected void fnAddPackage(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAddPackage()";
		// Get the request paramaters
		String pkg_code = req.getParameter("pkg_code");
		String pkg_name = req.getParameter("pkg_name");
		String pkg_description = req.getParameter("pkg_description");
		String pos_uom = req.getParameter("pos_uom");
		// String unit_px_std = req.getParameter("unit_px_std");
		// String unit_px_discounted = req.getParameter("unit_px_discounted");
		// String unit_px_min = req.getParameter("unit_px_min");
		// String effDate_year = req.getParameter("effDate_year");
		// String effDate_month = req.getParameter("effDate_month");
		// String effDate_day = req.getParameter("effDate_day");
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
			if (pkg_code == null)
			{
				// return;
				throw new Exception("Invalid Item Code");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - pkg_code = " + pkg_code);
			if (pkg_name == null)
			{
				// return;
				throw new Exception("Invalid pkg_name");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - pkg_name = " + pkg_name);
			if (pkg_description == null)
			{
				// return;
				throw new Exception("Invalid pkg_description");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - pkg_description = " + pkg_description);
			/*
			 * if (pos_uom == null) { //return; throw new Exception("Invalid
			 * pos_uom"); } else Log.printVerbose(strClassName + ":" + funcName + " -
			 * pos_uom = " + pos_uom); if (unit_px_std == null) { //return;
			 * throw new Exception("Invalid unit_px_std"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " - unit_px_std = " +
			 * unit_px_std); if (unit_px_discounted == null) { //return; throw
			 * new Exception("Invalid unit_px_discounted"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " -
			 * unit_px_discounted = " + unit_px_discounted); if (unit_px_min ==
			 * null) { //return; throw new Exception("Invalid unit_px_min"); }
			 * else Log.printVerbose(strClassName + ":" + funcName + " -
			 * unit_px_min = " + unit_px_min); if (effDate_year == null) {
			 * //return; throw new Exception("Invalid effDate_year"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " - effDate_year = " +
			 * effDate_year); if (effDate_month == null) { //return; throw new
			 * Exception("Invalid effDate_month"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " -
			 * effDate_month = " + effDate_month); if (effDate_day == null) {
			 * //return; throw new Exception("Invalid effDate_day"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " - effDate_day = " +
			 * effDate_day);
			 */
			Log.printVerbose("Adding new POS Package Item");
			java.util.Date ldt = new java.util.Date();
			Timestamp tsCreate = new Timestamp(ldt.getTime());
			Integer usrid = null;
			try
			{
				usrid = lUsr.getUserId();
			} catch (Exception ex)
			{
				Log.printAudit("User does not exist: " + ex.getMessage());
				throw new Exception(
						"Invalid Username. Your session may have expired or you may not be authorised to access the system. Please login again");
			}
			/*
			 * // construct the effective Date long effDateInMillis; try {
			 * effDateInMillis = new GregorianCalendar(new
			 * Integer(effDate_year).intValue(), new
			 * Integer(effDate_month).intValue()-1, new
			 * Integer(effDate_day).intValue()).getTimeInMillis(); }
			 * catch(Exception ex) { throw new Exception(ex.getMessage() + " -
			 * Error in Parsing Date"); }
			 */
			// Create the POS Package Item object
			PackageHome lPkgHome = PackageNut.getHome();
			/*
			 * // Check for duplicated POS Package Item Code if
			 * (POSItemNut.isPOSSvcExist(pkg_code, new
			 * Timestamp(effDateInMillis))) { throw new Exception ("The Package
			 * Code (" + pkg_code + ")" + " with Effective Date (" + new
			 * Timestamp(effDateInMillis).toString() + " already exist. Please
			 * use EDIT Package Code to change the POS Package Code details, or
			 * set a different Effective Date"); }
			 */
			/*
			 * String lItemType = POSItemBean.TYPE_PKG; String lCurrency =
			 * "MYR"; // default
			 */
			com.vlee.ejb.customer.Package lPackage = PackageNut.getObjectByCode(pkg_code);
			if (lPackage != null)
			{
				String rtnMsg = "ERROR: Package aleady exists for the given code, please enter a new code";
				Log.printDebug(rtnMsg);
				throw new Exception(rtnMsg);
			}
			String lUOM = "pkg"; // default unit_of_measure = "pkg"
			Integer lCatId = new Integer(0); // default categoryId=0
			com.vlee.ejb.customer.Package newPackage = lPkgHome.create(pkg_code, pkg_name, pkg_description, lUOM,
					lCatId, tsCreate, usrid);
			// If successful creation of package,
			// continue to create the POSItem Object for the package code
			/*
			 * if (newPackage != null) { // Get the packageItemId Integer
			 * lItemPKId = newPackage.getPkid();
			 * 
			 * POSItemHome lPOSItemHome = POSItemNut.getHome();
			 * 
			 * try { POSItem newPOSItem = lPOSItemHome.create(lItemPKId,
			 * lItemType, lCurrency, new BigDecimal(unit_px_std), new
			 * BigDecimal(unit_px_discounted), new BigDecimal(unit_px_min), new
			 * Timestamp(effDateInMillis), tsCreate, usrid); // if it reaches
			 * here, it has successfully created the POSItem if(newPOSItem !=
			 * null) { String rtnMsg = "Successfully created POSItem for Package
			 * Code = " + pkg_code; Log.printDebug(rtnMsg);
			 * req.setAttribute("rtnMsg", rtnMsg); } } catch (Exception ex) { //
			 * rollback the newly created Package Item newPackage.remove();
			 *  // rethrow throw ex; }
			 *  } // end if (newPackage != null)
			 */
		} // end try
		catch (Exception ex)
		{
			String rtnMsg = "Add Package with code = " + pkg_code + " -  Failed: " + ex.getMessage();
			Log.printDebug(rtnMsg);
			req.setAttribute("rtnMsg", rtnMsg);
		} // end try-catch
	} // end fnAddPackage

	protected void fnRemPackage(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String rmPkgCode = (String) req.getParameter("packageCode");
		if (rmPkgCode != null)
		{
			com.vlee.ejb.customer.Package lPkgCode = PackageNut.getObjectByCode(rmPkgCode);
			if (lPkgCode != null)
			{
				try
				{
					lPkgCode.remove();
				} catch (Exception ex)
				{
					String rtnMsg = "ERROR: Failed to remove Package '"
							+ rmPkgCode
							+ "'"
							+ " because it's still been referenced by other objects."
							+ " Please ensure that all objects currently refering to this package be unlinked before removing this package.";
					req.setAttribute("rtnMsg", rtnMsg);
					Log.printDebug("Remove Package Failed" + ex.getMessage());
				} // end try-catch
			} // end if (lPkgCode !=null)
		} // end if (rmPkgCode != null)
	} // end fnRemPackage

	protected void fnDeactPackage(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String packageCode = (String) req.getParameter("packageCode");
		if (packageCode != null)
		{
			com.vlee.ejb.customer.Package lPackage = PackageNut.getObjectByCode(packageCode);
			if (lPackage != null)
			{
				try
				{
					lPackage.setStatus(PackageBean.STATUS_INACTIVE);
				} catch (Exception ex)
				{
					Log.printDebug("Deactivate Package Failed" + ex.getMessage());
				} // end try-catch
			} // end if
		} // end if
	} // end fnDeactPackage

	protected void fnActivatePackage(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String packageCode = (String) req.getParameter("packageCode");
		if (packageCode != null)
		{
			com.vlee.ejb.customer.Package lPackage = PackageNut.getObjectByCode(packageCode);
			if (lPackage != null)
			{
				try
				{
					lPackage.setStatus(PackageBean.STATUS_ACTIVE);
				} catch (Exception ex)
				{
					Log.printDebug("Activate Package Failed" + ex.getMessage());
				} // end try-catch
			} // end if
		} // end if
	} // end fnDeactPackage
} // end class DoPOSPkgItemAdd
package com.vlee.servlet.customer;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.text.*;
import java.sql.*;
import java.math.BigDecimal;
import com.vlee.servlet.main.*;
import com.vlee.util.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;

public class DoPOSPkgItemAdd implements Action
{
	String strClassName = "DoPOSPkgItemAdd";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName != null)
		{
			if (formName.compareTo("addPOSPkgItem") == 0)
			{
				// add the POS Package Item
				Log.printVerbose(strClassName + ": formName = addPOSPkgItem");
				fnAddPackage(servlet, req, res);
			}
			if (formName.compareTo("remPOSPkgItem") == 0)
			{
				// remove the POS Package Item
				Log.printVerbose(strClassName + ": formName = remPOSPkgItem");
				fnRemPackage(servlet, req, res);
			}
			if (formName.compareTo("deactPOSPkgItem") == 0)
			{
				// Deactivate the POS Package Item
				Log.printVerbose(strClassName + ": formName = deactPOSPkgItem");
				fnDeactPackage(servlet, req, res);
			}
			if (formName.compareTo("actPOSPkgItem") == 0)
			{
				// Activate the POS Package Item
				Log.printVerbose(strClassName + ": formName = actPOSPkgItem");
				fnActivatePackage(servlet, req, res);
			}
		}
		fnGetParams(servlet, req, res);
		return new ActionRouter("cust-pos-add-pkg-items-page");
	}

	protected void fnGetParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetParams()";
		// 1. itrActivePackage
		Collection colActivePackage = PackageNut.getActiveObjects();
		Iterator itrActivePackage = colActivePackage.iterator();
		Log.printVerbose("Setting attribute itrActivePackage now");
		req.setAttribute("itrActivePackage", itrActivePackage);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
		// 2. itrPassivePackage
		Collection colInactivePackage = PackageNut.getInactiveObjects();
		Iterator itrInactivePackage = colInactivePackage.iterator();
		Log.printVerbose("Setting attribute itrPassivePackage now");
		req.setAttribute("itrPassivePackage", itrInactivePackage);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
	}

	protected void fnAddPackage(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAddPackage()";
		// Get the request paramaters
		String pkg_code = req.getParameter("pkg_code");
		String pkg_name = req.getParameter("pkg_name");
		String pkg_description = req.getParameter("pkg_description");
		String pos_uom = req.getParameter("pos_uom");
		// String unit_px_std = req.getParameter("unit_px_std");
		// String unit_px_discounted = req.getParameter("unit_px_discounted");
		// String unit_px_min = req.getParameter("unit_px_min");
		// String effDate_year = req.getParameter("effDate_year");
		// String effDate_month = req.getParameter("effDate_month");
		// String effDate_day = req.getParameter("effDate_day");
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
			if (pkg_code == null)
			{
				// return;
				throw new Exception("Invalid Item Code");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - pkg_code = " + pkg_code);
			if (pkg_name == null)
			{
				// return;
				throw new Exception("Invalid pkg_name");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - pkg_name = " + pkg_name);
			if (pkg_description == null)
			{
				// return;
				throw new Exception("Invalid pkg_description");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - pkg_description = " + pkg_description);
			/*
			 * if (pos_uom == null) { //return; throw new Exception("Invalid
			 * pos_uom"); } else Log.printVerbose(strClassName + ":" + funcName + " -
			 * pos_uom = " + pos_uom); if (unit_px_std == null) { //return;
			 * throw new Exception("Invalid unit_px_std"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " - unit_px_std = " +
			 * unit_px_std); if (unit_px_discounted == null) { //return; throw
			 * new Exception("Invalid unit_px_discounted"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " -
			 * unit_px_discounted = " + unit_px_discounted); if (unit_px_min ==
			 * null) { //return; throw new Exception("Invalid unit_px_min"); }
			 * else Log.printVerbose(strClassName + ":" + funcName + " -
			 * unit_px_min = " + unit_px_min); if (effDate_year == null) {
			 * //return; throw new Exception("Invalid effDate_year"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " - effDate_year = " +
			 * effDate_year); if (effDate_month == null) { //return; throw new
			 * Exception("Invalid effDate_month"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " -
			 * effDate_month = " + effDate_month); if (effDate_day == null) {
			 * //return; throw new Exception("Invalid effDate_day"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " - effDate_day = " +
			 * effDate_day);
			 */
			Log.printVerbose("Adding new POS Package Item");
			java.util.Date ldt = new java.util.Date();
			Timestamp tsCreate = new Timestamp(ldt.getTime());
			Integer usrid = null;
			try
			{
				usrid = lUsr.getUserId();
			} catch (Exception ex)
			{
				Log.printAudit("User does not exist: " + ex.getMessage());
				throw new Exception(
						"Invalid Username. Your session may have expired or you may not be authorised to access the system. Please login again");
			}
			/*
			 * // construct the effective Date long effDateInMillis; try {
			 * effDateInMillis = new GregorianCalendar(new
			 * Integer(effDate_year).intValue(), new
			 * Integer(effDate_month).intValue()-1, new
			 * Integer(effDate_day).intValue()).getTimeInMillis(); }
			 * catch(Exception ex) { throw new Exception(ex.getMessage() + " -
			 * Error in Parsing Date"); }
			 */
			// Create the POS Package Item object
			PackageHome lPkgHome = PackageNut.getHome();
			/*
			 * // Check for duplicated POS Package Item Code if
			 * (POSItemNut.isPOSSvcExist(pkg_code, new
			 * Timestamp(effDateInMillis))) { throw new Exception ("The Package
			 * Code (" + pkg_code + ")" + " with Effective Date (" + new
			 * Timestamp(effDateInMillis).toString() + " already exist. Please
			 * use EDIT Package Code to change the POS Package Code details, or
			 * set a different Effective Date"); }
			 */
			/*
			 * String lItemType = POSItemBean.TYPE_PKG; String lCurrency =
			 * "MYR"; // default
			 */
			com.vlee.ejb.customer.Package lPackage = PackageNut.getObjectByCode(pkg_code);
			if (lPackage != null)
			{
				String rtnMsg = "ERROR: Package aleady exists for the given code, please enter a new code";
				Log.printDebug(rtnMsg);
				throw new Exception(rtnMsg);
			}
			String lUOM = "pkg"; // default unit_of_measure = "pkg"
			Integer lCatId = new Integer(0); // default categoryId=0
			com.vlee.ejb.customer.Package newPackage = lPkgHome.create(pkg_code, pkg_name, pkg_description, lUOM,
					lCatId, tsCreate, usrid);
			// If successful creation of package,
			// continue to create the POSItem Object for the package code
			/*
			 * if (newPackage != null) { // Get the packageItemId Integer
			 * lItemPKId = newPackage.getPkid();
			 * 
			 * POSItemHome lPOSItemHome = POSItemNut.getHome();
			 * 
			 * try { POSItem newPOSItem = lPOSItemHome.create(lItemPKId,
			 * lItemType, lCurrency, new BigDecimal(unit_px_std), new
			 * BigDecimal(unit_px_discounted), new BigDecimal(unit_px_min), new
			 * Timestamp(effDateInMillis), tsCreate, usrid); // if it reaches
			 * here, it has successfully created the POSItem if(newPOSItem !=
			 * null) { String rtnMsg = "Successfully created POSItem for Package
			 * Code = " + pkg_code; Log.printDebug(rtnMsg);
			 * req.setAttribute("rtnMsg", rtnMsg); } } catch (Exception ex) { //
			 * rollback the newly created Package Item newPackage.remove();
			 *  // rethrow throw ex; }
			 *  } // end if (newPackage != null)
			 */
		} // end try
		catch (Exception ex)
		{
			String rtnMsg = "Add Package with code = " + pkg_code + " -  Failed: " + ex.getMessage();
			Log.printDebug(rtnMsg);
			req.setAttribute("rtnMsg", rtnMsg);
		} // end try-catch
	} // end fnAddPackage

	protected void fnRemPackage(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String rmPkgCode = (String) req.getParameter("packageCode");
		if (rmPkgCode != null)
		{
			com.vlee.ejb.customer.Package lPkgCode = PackageNut.getObjectByCode(rmPkgCode);
			if (lPkgCode != null)
			{
				try
				{
					lPkgCode.remove();
				} catch (Exception ex)
				{
					String rtnMsg = "ERROR: Failed to remove Package '"
							+ rmPkgCode
							+ "'"
							+ " because it's still been referenced by other objects."
							+ " Please ensure that all objects currently refering to this package be unlinked before removing this package.";
					req.setAttribute("rtnMsg", rtnMsg);
					Log.printDebug("Remove Package Failed" + ex.getMessage());
				} // end try-catch
			} // end if (lPkgCode !=null)
		} // end if (rmPkgCode != null)
	} // end fnRemPackage

	protected void fnDeactPackage(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String packageCode = (String) req.getParameter("packageCode");
		if (packageCode != null)
		{
			com.vlee.ejb.customer.Package lPackage = PackageNut.getObjectByCode(packageCode);
			if (lPackage != null)
			{
				try
				{
					lPackage.setStatus(PackageBean.STATUS_INACTIVE);
				} catch (Exception ex)
				{
					Log.printDebug("Deactivate Package Failed" + ex.getMessage());
				} // end try-catch
			} // end if
		} // end if
	} // end fnDeactPackage

	protected void fnActivatePackage(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String packageCode = (String) req.getParameter("packageCode");
		if (packageCode != null)
		{
			com.vlee.ejb.customer.Package lPackage = PackageNut.getObjectByCode(packageCode);
			if (lPackage != null)
			{
				try
				{
					lPackage.setStatus(PackageBean.STATUS_ACTIVE);
				} catch (Exception ex)
				{
					Log.printDebug("Activate Package Failed" + ex.getMessage());
				} // end try-catch
			} // end if
		} // end if
	} // end fnDeactPackage
} // end class DoPOSPkgItemAdd
package com.vlee.servlet.customer;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.text.*;
import java.sql.*;
import java.math.BigDecimal;
import com.vlee.servlet.main.*;
import com.vlee.util.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;

public class DoPOSPkgItemAdd implements Action
{
	String strClassName = "DoPOSPkgItemAdd";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName != null)
		{
			if (formName.compareTo("addPOSPkgItem") == 0)
			{
				// add the POS Package Item
				Log.printVerbose(strClassName + ": formName = addPOSPkgItem");
				fnAddPackage(servlet, req, res);
			}
			if (formName.compareTo("remPOSPkgItem") == 0)
			{
				// remove the POS Package Item
				Log.printVerbose(strClassName + ": formName = remPOSPkgItem");
				fnRemPackage(servlet, req, res);
			}
			if (formName.compareTo("deactPOSPkgItem") == 0)
			{
				// Deactivate the POS Package Item
				Log.printVerbose(strClassName + ": formName = deactPOSPkgItem");
				fnDeactPackage(servlet, req, res);
			}
			if (formName.compareTo("actPOSPkgItem") == 0)
			{
				// Activate the POS Package Item
				Log.printVerbose(strClassName + ": formName = actPOSPkgItem");
				fnActivatePackage(servlet, req, res);
			}
		}
		fnGetParams(servlet, req, res);
		return new ActionRouter("cust-pos-add-pkg-items-page");
	}

	protected void fnGetParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetParams()";
		// 1. itrActivePackage
		Collection colActivePackage = PackageNut.getActiveObjects();
		Iterator itrActivePackage = colActivePackage.iterator();
		Log.printVerbose("Setting attribute itrActivePackage now");
		req.setAttribute("itrActivePackage", itrActivePackage);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
		// 2. itrPassivePackage
		Collection colInactivePackage = PackageNut.getInactiveObjects();
		Iterator itrInactivePackage = colInactivePackage.iterator();
		Log.printVerbose("Setting attribute itrPassivePackage now");
		req.setAttribute("itrPassivePackage", itrInactivePackage);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
	}

	protected void fnAddPackage(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAddPackage()";
		// Get the request paramaters
		String pkg_code = req.getParameter("pkg_code");
		String pkg_name = req.getParameter("pkg_name");
		String pkg_description = req.getParameter("pkg_description");
		String pos_uom = req.getParameter("pos_uom");
		// String unit_px_std = req.getParameter("unit_px_std");
		// String unit_px_discounted = req.getParameter("unit_px_discounted");
		// String unit_px_min = req.getParameter("unit_px_min");
		// String effDate_year = req.getParameter("effDate_year");
		// String effDate_month = req.getParameter("effDate_month");
		// String effDate_day = req.getParameter("effDate_day");
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
			if (pkg_code == null)
			{
				// return;
				throw new Exception("Invalid Item Code");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - pkg_code = " + pkg_code);
			if (pkg_name == null)
			{
				// return;
				throw new Exception("Invalid pkg_name");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - pkg_name = " + pkg_name);
			if (pkg_description == null)
			{
				// return;
				throw new Exception("Invalid pkg_description");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - pkg_description = " + pkg_description);
			/*
			 * if (pos_uom == null) { //return; throw new Exception("Invalid
			 * pos_uom"); } else Log.printVerbose(strClassName + ":" + funcName + " -
			 * pos_uom = " + pos_uom); if (unit_px_std == null) { //return;
			 * throw new Exception("Invalid unit_px_std"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " - unit_px_std = " +
			 * unit_px_std); if (unit_px_discounted == null) { //return; throw
			 * new Exception("Invalid unit_px_discounted"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " -
			 * unit_px_discounted = " + unit_px_discounted); if (unit_px_min ==
			 * null) { //return; throw new Exception("Invalid unit_px_min"); }
			 * else Log.printVerbose(strClassName + ":" + funcName + " -
			 * unit_px_min = " + unit_px_min); if (effDate_year == null) {
			 * //return; throw new Exception("Invalid effDate_year"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " - effDate_year = " +
			 * effDate_year); if (effDate_month == null) { //return; throw new
			 * Exception("Invalid effDate_month"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " -
			 * effDate_month = " + effDate_month); if (effDate_day == null) {
			 * //return; throw new Exception("Invalid effDate_day"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " - effDate_day = " +
			 * effDate_day);
			 */
			Log.printVerbose("Adding new POS Package Item");
			java.util.Date ldt = new java.util.Date();
			Timestamp tsCreate = new Timestamp(ldt.getTime());
			Integer usrid = null;
			try
			{
				usrid = lUsr.getUserId();
			} catch (Exception ex)
			{
				Log.printAudit("User does not exist: " + ex.getMessage());
				throw new Exception(
						"Invalid Username. Your session may have expired or you may not be authorised to access the system. Please login again");
			}
			/*
			 * // construct the effective Date long effDateInMillis; try {
			 * effDateInMillis = new GregorianCalendar(new
			 * Integer(effDate_year).intValue(), new
			 * Integer(effDate_month).intValue()-1, new
			 * Integer(effDate_day).intValue()).getTimeInMillis(); }
			 * catch(Exception ex) { throw new Exception(ex.getMessage() + " -
			 * Error in Parsing Date"); }
			 */
			// Create the POS Package Item object
			PackageHome lPkgHome = PackageNut.getHome();
			/*
			 * // Check for duplicated POS Package Item Code if
			 * (POSItemNut.isPOSSvcExist(pkg_code, new
			 * Timestamp(effDateInMillis))) { throw new Exception ("The Package
			 * Code (" + pkg_code + ")" + " with Effective Date (" + new
			 * Timestamp(effDateInMillis).toString() + " already exist. Please
			 * use EDIT Package Code to change the POS Package Code details, or
			 * set a different Effective Date"); }
			 */
			/*
			 * String lItemType = POSItemBean.TYPE_PKG; String lCurrency =
			 * "MYR"; // default
			 */
			com.vlee.ejb.customer.Package lPackage = PackageNut.getObjectByCode(pkg_code);
			if (lPackage != null)
			{
				String rtnMsg = "ERROR: Package aleady exists for the given code, please enter a new code";
				Log.printDebug(rtnMsg);
				throw new Exception(rtnMsg);
			}
			String lUOM = "pkg"; // default unit_of_measure = "pkg"
			Integer lCatId = new Integer(0); // default categoryId=0
			com.vlee.ejb.customer.Package newPackage = lPkgHome.create(pkg_code, pkg_name, pkg_description, lUOM,
					lCatId, tsCreate, usrid);
			// If successful creation of package,
			// continue to create the POSItem Object for the package code
			/*
			 * if (newPackage != null) { // Get the packageItemId Integer
			 * lItemPKId = newPackage.getPkid();
			 * 
			 * POSItemHome lPOSItemHome = POSItemNut.getHome();
			 * 
			 * try { POSItem newPOSItem = lPOSItemHome.create(lItemPKId,
			 * lItemType, lCurrency, new BigDecimal(unit_px_std), new
			 * BigDecimal(unit_px_discounted), new BigDecimal(unit_px_min), new
			 * Timestamp(effDateInMillis), tsCreate, usrid); // if it reaches
			 * here, it has successfully created the POSItem if(newPOSItem !=
			 * null) { String rtnMsg = "Successfully created POSItem for Package
			 * Code = " + pkg_code; Log.printDebug(rtnMsg);
			 * req.setAttribute("rtnMsg", rtnMsg); } } catch (Exception ex) { //
			 * rollback the newly created Package Item newPackage.remove();
			 *  // rethrow throw ex; }
			 *  } // end if (newPackage != null)
			 */
		} // end try
		catch (Exception ex)
		{
			String rtnMsg = "Add Package with code = " + pkg_code + " -  Failed: " + ex.getMessage();
			Log.printDebug(rtnMsg);
			req.setAttribute("rtnMsg", rtnMsg);
		} // end try-catch
	} // end fnAddPackage

	protected void fnRemPackage(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String rmPkgCode = (String) req.getParameter("packageCode");
		if (rmPkgCode != null)
		{
			com.vlee.ejb.customer.Package lPkgCode = PackageNut.getObjectByCode(rmPkgCode);
			if (lPkgCode != null)
			{
				try
				{
					lPkgCode.remove();
				} catch (Exception ex)
				{
					String rtnMsg = "ERROR: Failed to remove Package '"
							+ rmPkgCode
							+ "'"
							+ " because it's still been referenced by other objects."
							+ " Please ensure that all objects currently refering to this package be unlinked before removing this package.";
					req.setAttribute("rtnMsg", rtnMsg);
					Log.printDebug("Remove Package Failed" + ex.getMessage());
				} // end try-catch
			} // end if (lPkgCode !=null)
		} // end if (rmPkgCode != null)
	} // end fnRemPackage

	protected void fnDeactPackage(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String packageCode = (String) req.getParameter("packageCode");
		if (packageCode != null)
		{
			com.vlee.ejb.customer.Package lPackage = PackageNut.getObjectByCode(packageCode);
			if (lPackage != null)
			{
				try
				{
					lPackage.setStatus(PackageBean.STATUS_INACTIVE);
				} catch (Exception ex)
				{
					Log.printDebug("Deactivate Package Failed" + ex.getMessage());
				} // end try-catch
			} // end if
		} // end if
	} // end fnDeactPackage

	protected void fnActivatePackage(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String packageCode = (String) req.getParameter("packageCode");
		if (packageCode != null)
		{
			com.vlee.ejb.customer.Package lPackage = PackageNut.getObjectByCode(packageCode);
			if (lPackage != null)
			{
				try
				{
					lPackage.setStatus(PackageBean.STATUS_ACTIVE);
				} catch (Exception ex)
				{
					Log.printDebug("Activate Package Failed" + ex.getMessage());
				} // end try-catch
			} // end if
		} // end if
	} // end fnDeactPackage
} // end class DoPOSPkgItemAdd
