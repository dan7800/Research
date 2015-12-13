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

public class DoPOSPkgItemEdit implements Action
{
	String strClassName = "DoPOSPkgItemEdit";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName != null)
		{
			if (formName.compareTo("updatePackage") == 0)
			{
				// update the POS Package Item
				fnUpdatePackage(servlet, req, res);
				// fnGetParams(servlet, req, res);
			}
			if (formName.compareTo("remItemsFromPkg") == 0)
			{
				fnRemItems(servlet, req, res);
			}
			if (formName.compareTo("remSvcFromPkg") == 0)
			{
				fnRemServices(servlet, req, res);
			}
		}
		fnGetParams(servlet, req, res);
		return new ActionRouter("cust-pos-edit-pkg-items-page");
	}

	protected void fnGetParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetParams()";
		/*
		 * Need to populate 4 things: [1] (Package) editPkg, given the
		 * packageCode [2] (POSItem) posPkgItem corresponding to the editPkg [
		 * if exist ] [3] (Vector) vecItemCodes, vecItemNames, vecInvQuantities
		 * [4] (Vector) vecSvcCodes, vecSvcNames, vecSvcQuantities
		 * 
		 */
		Vector vecItemCodes = new Vector();
		Vector vecItemNames = new Vector();
		Vector vecInvQuantities = new Vector();
		Vector vecSvcCodes = new Vector();
		Vector vecSvcNames = new Vector();
		Vector vecSvcQuantities = new Vector();
		try
		{ // super huge try block
			// Obtain the packageId from packageCode
			String lPkgCode = req.getParameter("packageCode");
			com.vlee.ejb.customer.Package lPackage = PackageNut.getObjectByCode(lPkgCode);
			// Get all items attached to this package
			if (lPackage != null)
			{
				req.setAttribute("editPkg", lPackage);
				// Now try to obtain the posPkgItem corresponding to editPkg
				POSItem posPkgItem = POSItemNut.getPOSItem(lPackage.getPkid(), POSItemBean.TYPE_PKG);
				if (posPkgItem != null)
				{
					req.setAttribute("posPkgItem", posPkgItem);
				}
				Collection colPkgItems = PackageInvItemNut.getObjectsByPkgId(lPackage.getPkid());
				if (colPkgItems != null)
				{
					// for each pkgItem row, obtain the invItemId, then get the
					// Item object, and the itemCode and itemName
					for (Iterator itr = colPkgItems.iterator(); itr.hasNext();)
					{
						PackageInvItem lPkgItem = (PackageInvItem) itr.next();
						Integer lInvItemId = lPkgItem.getInvItemId();
						Item lItem = ItemNut.getHandle(lInvItemId);
						if (lItem == null)
						{
							throw new Exception("Null item for itemId = " + lInvItemId);
						}
						// populate the vecItemCodes and vecItemNames
						vecItemCodes.add(lItem.getItemCode());
						vecItemNames.add(lItem.getName());
						vecInvQuantities.add(lPkgItem.getQuantity());
					} // end for (Iterator itr = colPkgItems.iterator();
						// itr.hasNext();)
				} // end if (colPkgItems != null)
				Collection colPkgSvcs = PackageServiceNut.getObjectsByPkgId(lPackage.getPkid());
				if (colPkgSvcs != null)
				{
					// for each pkgItem row, obtain the serviceId, then get the
					// Service object, and the serviceCode and serviceName
					for (Iterator itr = colPkgSvcs.iterator(); itr.hasNext();)
					{
						PackageService lPkgSvc = (PackageService) itr.next();
						Integer lSvcId = lPkgSvc.getServiceId();
						Service lSvc = ServiceNut.getHandle(lSvcId);
						if (lSvc == null)
						{
							throw new Exception("Null item for serviceId = " + lSvcId);
						}
						// populate the vecItemCodes and vecItemNames
						vecSvcCodes.add(lSvc.getCode());
						vecSvcNames.add(lSvc.getName());
						vecSvcQuantities.add(lPkgSvc.getQuantity());
					} // end for (Iterator itr = colPkgSvcs.iterator();
						// itr.hasNext();)
				} // end if (colPkgSvcs != null)
				// Now try to obtain the posPkgItem corresponding to editPkg
				// OK I'm stuck here !!! without the Effective Date,
				// how to get a unique posPkgItem???
				// populate the attributes to be sent to the JSPs
				req.setAttribute("vecItemCodes", vecItemCodes);
				req.setAttribute("vecItemNames", vecItemNames);
				req.setAttribute("vecInvQuantities", vecInvQuantities);
				req.setAttribute("vecSvcCodes", vecSvcCodes);
				req.setAttribute("vecSvcNames", vecSvcNames);
				req.setAttribute("vecSvcQuantities", vecSvcQuantities);
			} // end if (lPackage != null)
		} catch (Exception ex)
		{
			Log.printDebug(strClassName + ":" + funcName + " - " + ex.getMessage());
		}
	} // end fnGetParams

	protected void fnRemItems(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnRemItems()";
		// Get the required params
		String packageId = req.getParameter("packageId");
		String packageCode = req.getParameter("packageCode");
		String[] removeItems = req.getParameterValues("removeItems");
		String rtnMsg;
		try
		{
			if (packageId == null || packageCode == null || removeItems == null)
			{
				rtnMsg = "Null packageId, packageCode or removeItems";
				throw new Exception(rtnMsg);
			}
			HttpSession session = req.getSession();
			User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
			if (lUsr == null)
			{
				Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
				throw new Exception(
						"Invalid Username. Your session may have expired or you may not be authorised to access the system. Please login again");
			}
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
			for (int i = 0; i < removeItems.length; i++)
			{
				// Obtain the itemId to remove
				Item lItem = ItemNut.getObjectByCode(removeItems[i]);
				Integer lItemId = lItem.getPkid();
				// Get the packageItem link
				PackageInvItem lPkgInv = PackageInvItemNut.getObjectByPkgAndInv(new Integer(packageId), lItemId);
				if (lPkgInv != null)
				{
					lPkgInv.remove();
				} else
				{
					Log.printDebug("Package: " + packageCode + " and " + "Item: " + removeItems[i] + " not found!");
				} // end if (lPkgInv != null)
			} // end for(int i=0; i<removeItems.length; i++)
		} catch (Exception ex)
		{
			rtnMsg = "Failed to Remove Items from package '" + packageCode + "'. Reason = ";
			rtnMsg += ex.getMessage();
			Log.printDebug(rtnMsg);
			req.setAttribute("rtnMsg", rtnMsg);
		}
		// 
	} // end fnRemItems()

	protected void fnRemServices(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnRemServices()";
		// Get the required params
		String packageId = req.getParameter("packageId");
		String packageCode = req.getParameter("packageCode");
		String[] removeServices = req.getParameterValues("removeServices");
		String rtnMsg;
		try
		{
			if (packageId == null || packageCode == null || removeServices == null)
			{
				rtnMsg = "Null packageId, packageCode or removeServices";
				throw new Exception(rtnMsg);
			}
			HttpSession session = req.getSession();
			User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
			if (lUsr == null)
			{
				Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
				throw new Exception(
						"Invalid Username. Your session may have expired or you may not be authorised to access the system. Please login again");
			}
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
			for (int i = 0; i < removeServices.length; i++)
			{
				// Obtain the serviceId to remove
				Service lSvc = ServiceNut.getObjectByCode(removeServices[i]);
				Integer lSvcId = lSvc.getPkid();
				// Get the packageService link
				PackageService lPkgSvc = PackageServiceNut.getObjectByPkgAndSvc(new Integer(packageId), lSvcId);
				if (lPkgSvc != null)
				{
					lPkgSvc.remove();
				} else
				{
					Log.printDebug("Package: " + packageCode + " and " + "Service: " + removeServices[i]
							+ " not found!");
				} // end if (lPkgSvc != null)
			} // end for(int i=0; i<removeServices.length; i++)
		} catch (Exception ex)
		{
			rtnMsg = "Failed to Remove Services from package '" + packageCode + "'. Reason = ";
			rtnMsg += ex.getMessage();
			Log.printDebug(rtnMsg);
			req.setAttribute("rtnMsg", rtnMsg);
		}
		//
	} // end fnRemServices()

	protected void fnUpdatePackage(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdatePackage()";
		// Get the request paramaters
		String packageId = (String) req.getParameter("packageId");
		String packageCode = (String) req.getParameter("packageCode");
		String packageName = (String) req.getParameter("packageName");
		String packageDesc = (String) req.getParameter("packageDesc");
		// String pos_uom = req.getParameter("pos_uom");
		String currency = req.getParameter("currency");
		String stdPrice = req.getParameter("stdPrice");
		String discPrice = req.getParameter("discPrice");
		String minPrice = req.getParameter("minPrice");
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
			if (packageCode == null)
			{
				// return;
				throw new Exception("Invalid Item Code");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - packageCode = " + packageCode);
			if (packageName == null)
			{
				// return;
				throw new Exception("Invalid packageName");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - packageName = " + packageName);
			if (packageDesc == null)
			{
				// return;
				throw new Exception("Invalid packageDesc");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - packageDesc = " + packageDesc);
			/*
			 * if (pos_uom == null) { //return; throw new Exception("Invalid
			 * pos_uom"); } else Log.printVerbose(strClassName + ":" + funcName + " -
			 * pos_uom = " + pos_uom);
			 */
			if (currency == null)
			{
				// return;
				throw new Exception("Invalid currency");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - currency = " + currency);
			if (stdPrice == null)
			{
				// return;
				throw new Exception("Invalid stdPrice");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - stdPrice = " + stdPrice);
			if (discPrice == null)
			{
				// return;
				throw new Exception("Invalid discPrice");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - discPrice = " + discPrice);
			if (minPrice == null)
			{
				// return;
				throw new Exception("Invalid minPrice");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - minPrice = " + minPrice);
			/*
			 * if (effDate_year == null) { //return; throw new
			 * Exception("Invalid effDate_year"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " - effDate_year = " +
			 * effDate_year); if (effDate_month == null) { //return; throw new
			 * Exception("Invalid effDate_month"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " -
			 * effDate_month = " + effDate_month); if (effDate_day == null) {
			 * //return; throw new Exception("Invalid effDate_day"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " - effDate_day = " +
			 * effDate_day);
			 */
			Log.printVerbose("Editing Package Inventory Item ... ");
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
			// Get the Package
			com.vlee.ejb.customer.Package lPkg = PackageNut.getHandle(new Integer(packageId));
			if (lPkg == null)
			{
				throw new Exception("Cannot Edit Null Package");
			}
			// Edit the Package Details here
			lPkg.setCode(packageCode);
			lPkg.setName(packageName);
			lPkg.setDescription(packageDesc);
			// lPkg.setUnitMeasure(pkg_uom);
			lPkg.setLastUpdate(tsCreate);
			lPkg.setUserIdUpdate(usrid);
			// Try to get the corresponding POSItem
			POSItem lPOSItem = POSItemNut.getPOSItem(new Integer(packageId), POSItemBean.TYPE_PKG);
			// Edit the posItem if exist, else create a new row for it
			if (lPOSItem != null)
			{
				lPOSItem.setCurrency(currency);
				lPOSItem.setUnitPriceStd(new BigDecimal(stdPrice));
				lPOSItem.setUnitPriceDiscounted(new BigDecimal(discPrice));
				lPOSItem.setUnitPriceMin(new BigDecimal(minPrice));
				lPOSItem.setLastUpdate(tsCreate);
				lPOSItem.setUserIdUpdate(usrid);
			} else
			{
				// Create a new row for it
				String lItemType = POSItemBean.TYPE_PKG;
				String lCurrency = "MYR"; // default
				Integer lCatId = new Integer(0); // default
				POSItemObject posObj = new POSItemObject();
				posObj.itemFKId = new Integer(packageId);
				posObj.itemType = lItemType;
				posObj.currency = lCurrency;
				posObj.unitPriceStd = new BigDecimal(stdPrice);
				posObj.unitPriceDiscounted = new BigDecimal(discPrice);
				posObj.unitPriceMin = new BigDecimal(minPrice);
				posObj.userIdUpdate = usrid;
				POSItem newPOSItem = POSItemNut.fnCreate(posObj);
				// if it reaches here, it has successfully created the POSItem
				if (newPOSItem != null)
				{
					String rtnMsg = "Successfully created POSItem for Package Code = " + packageCode;
					Log.printDebug(rtnMsg);
					req.setAttribute("rtnMsg", rtnMsg);
				}
			} // end if (lPOSItem != null)
		} // end try
		catch (Exception ex)
		{
			String rtnMsg = "Update POSItem for Package Code = " + packageCode + " -  Failed: " + ex.getMessage();
			Log.printDebug(rtnMsg);
			req.setAttribute("rtnMsg", rtnMsg);
		} // end try-catch
	} // end fnUpdatePackage

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
} // end class DoPOSPkgItemEdit
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

public class DoPOSPkgItemEdit implements Action
{
	String strClassName = "DoPOSPkgItemEdit";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName != null)
		{
			if (formName.compareTo("updatePackage") == 0)
			{
				// update the POS Package Item
				fnUpdatePackage(servlet, req, res);
				// fnGetParams(servlet, req, res);
			}
			if (formName.compareTo("remItemsFromPkg") == 0)
			{
				fnRemItems(servlet, req, res);
			}
			if (formName.compareTo("remSvcFromPkg") == 0)
			{
				fnRemServices(servlet, req, res);
			}
		}
		fnGetParams(servlet, req, res);
		return new ActionRouter("cust-pos-edit-pkg-items-page");
	}

	protected void fnGetParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetParams()";
		/*
		 * Need to populate 4 things: [1] (Package) editPkg, given the
		 * packageCode [2] (POSItem) posPkgItem corresponding to the editPkg [
		 * if exist ] [3] (Vector) vecItemCodes, vecItemNames, vecInvQuantities
		 * [4] (Vector) vecSvcCodes, vecSvcNames, vecSvcQuantities
		 * 
		 */
		Vector vecItemCodes = new Vector();
		Vector vecItemNames = new Vector();
		Vector vecInvQuantities = new Vector();
		Vector vecSvcCodes = new Vector();
		Vector vecSvcNames = new Vector();
		Vector vecSvcQuantities = new Vector();
		try
		{ // super huge try block
			// Obtain the packageId from packageCode
			String lPkgCode = req.getParameter("packageCode");
			com.vlee.ejb.customer.Package lPackage = PackageNut.getObjectByCode(lPkgCode);
			// Get all items attached to this package
			if (lPackage != null)
			{
				req.setAttribute("editPkg", lPackage);
				// Now try to obtain the posPkgItem corresponding to editPkg
				POSItem posPkgItem = POSItemNut.getPOSItem(lPackage.getPkid(), POSItemBean.TYPE_PKG);
				if (posPkgItem != null)
				{
					req.setAttribute("posPkgItem", posPkgItem);
				}
				Collection colPkgItems = PackageInvItemNut.getObjectsByPkgId(lPackage.getPkid());
				if (colPkgItems != null)
				{
					// for each pkgItem row, obtain the invItemId, then get the
					// Item object, and the itemCode and itemName
					for (Iterator itr = colPkgItems.iterator(); itr.hasNext();)
					{
						PackageInvItem lPkgItem = (PackageInvItem) itr.next();
						Integer lInvItemId = lPkgItem.getInvItemId();
						Item lItem = ItemNut.getHandle(lInvItemId);
						if (lItem == null)
						{
							throw new Exception("Null item for itemId = " + lInvItemId);
						}
						// populate the vecItemCodes and vecItemNames
						vecItemCodes.add(lItem.getItemCode());
						vecItemNames.add(lItem.getName());
						vecInvQuantities.add(lPkgItem.getQuantity());
					} // end for (Iterator itr = colPkgItems.iterator();
						// itr.hasNext();)
				} // end if (colPkgItems != null)
				Collection colPkgSvcs = PackageServiceNut.getObjectsByPkgId(lPackage.getPkid());
				if (colPkgSvcs != null)
				{
					// for each pkgItem row, obtain the serviceId, then get the
					// Service object, and the serviceCode and serviceName
					for (Iterator itr = colPkgSvcs.iterator(); itr.hasNext();)
					{
						PackageService lPkgSvc = (PackageService) itr.next();
						Integer lSvcId = lPkgSvc.getServiceId();
						Service lSvc = ServiceNut.getHandle(lSvcId);
						if (lSvc == null)
						{
							throw new Exception("Null item for serviceId = " + lSvcId);
						}
						// populate the vecItemCodes and vecItemNames
						vecSvcCodes.add(lSvc.getCode());
						vecSvcNames.add(lSvc.getName());
						vecSvcQuantities.add(lPkgSvc.getQuantity());
					} // end for (Iterator itr = colPkgSvcs.iterator();
						// itr.hasNext();)
				} // end if (colPkgSvcs != null)
				// Now try to obtain the posPkgItem corresponding to editPkg
				// OK I'm stuck here !!! without the Effective Date,
				// how to get a unique posPkgItem???
				// populate the attributes to be sent to the JSPs
				req.setAttribute("vecItemCodes", vecItemCodes);
				req.setAttribute("vecItemNames", vecItemNames);
				req.setAttribute("vecInvQuantities", vecInvQuantities);
				req.setAttribute("vecSvcCodes", vecSvcCodes);
				req.setAttribute("vecSvcNames", vecSvcNames);
				req.setAttribute("vecSvcQuantities", vecSvcQuantities);
			} // end if (lPackage != null)
		} catch (Exception ex)
		{
			Log.printDebug(strClassName + ":" + funcName + " - " + ex.getMessage());
		}
	} // end fnGetParams

	protected void fnRemItems(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnRemItems()";
		// Get the required params
		String packageId = req.getParameter("packageId");
		String packageCode = req.getParameter("packageCode");
		String[] removeItems = req.getParameterValues("removeItems");
		String rtnMsg;
		try
		{
			if (packageId == null || packageCode == null || removeItems == null)
			{
				rtnMsg = "Null packageId, packageCode or removeItems";
				throw new Exception(rtnMsg);
			}
			HttpSession session = req.getSession();
			User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
			if (lUsr == null)
			{
				Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
				throw new Exception(
						"Invalid Username. Your session may have expired or you may not be authorised to access the system. Please login again");
			}
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
			for (int i = 0; i < removeItems.length; i++)
			{
				// Obtain the itemId to remove
				Item lItem = ItemNut.getObjectByCode(removeItems[i]);
				Integer lItemId = lItem.getPkid();
				// Get the packageItem link
				PackageInvItem lPkgInv = PackageInvItemNut.getObjectByPkgAndInv(new Integer(packageId), lItemId);
				if (lPkgInv != null)
				{
					lPkgInv.remove();
				} else
				{
					Log.printDebug("Package: " + packageCode + " and " + "Item: " + removeItems[i] + " not found!");
				} // end if (lPkgInv != null)
			} // end for(int i=0; i<removeItems.length; i++)
		} catch (Exception ex)
		{
			rtnMsg = "Failed to Remove Items from package '" + packageCode + "'. Reason = ";
			rtnMsg += ex.getMessage();
			Log.printDebug(rtnMsg);
			req.setAttribute("rtnMsg", rtnMsg);
		}
		// 
	} // end fnRemItems()

	protected void fnRemServices(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnRemServices()";
		// Get the required params
		String packageId = req.getParameter("packageId");
		String packageCode = req.getParameter("packageCode");
		String[] removeServices = req.getParameterValues("removeServices");
		String rtnMsg;
		try
		{
			if (packageId == null || packageCode == null || removeServices == null)
			{
				rtnMsg = "Null packageId, packageCode or removeServices";
				throw new Exception(rtnMsg);
			}
			HttpSession session = req.getSession();
			User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
			if (lUsr == null)
			{
				Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
				throw new Exception(
						"Invalid Username. Your session may have expired or you may not be authorised to access the system. Please login again");
			}
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
			for (int i = 0; i < removeServices.length; i++)
			{
				// Obtain the serviceId to remove
				Service lSvc = ServiceNut.getObjectByCode(removeServices[i]);
				Integer lSvcId = lSvc.getPkid();
				// Get the packageService link
				PackageService lPkgSvc = PackageServiceNut.getObjectByPkgAndSvc(new Integer(packageId), lSvcId);
				if (lPkgSvc != null)
				{
					lPkgSvc.remove();
				} else
				{
					Log.printDebug("Package: " + packageCode + " and " + "Service: " + removeServices[i]
							+ " not found!");
				} // end if (lPkgSvc != null)
			} // end for(int i=0; i<removeServices.length; i++)
		} catch (Exception ex)
		{
			rtnMsg = "Failed to Remove Services from package '" + packageCode + "'. Reason = ";
			rtnMsg += ex.getMessage();
			Log.printDebug(rtnMsg);
			req.setAttribute("rtnMsg", rtnMsg);
		}
		//
	} // end fnRemServices()

	protected void fnUpdatePackage(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdatePackage()";
		// Get the request paramaters
		String packageId = (String) req.getParameter("packageId");
		String packageCode = (String) req.getParameter("packageCode");
		String packageName = (String) req.getParameter("packageName");
		String packageDesc = (String) req.getParameter("packageDesc");
		// String pos_uom = req.getParameter("pos_uom");
		String currency = req.getParameter("currency");
		String stdPrice = req.getParameter("stdPrice");
		String discPrice = req.getParameter("discPrice");
		String minPrice = req.getParameter("minPrice");
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
			if (packageCode == null)
			{
				// return;
				throw new Exception("Invalid Item Code");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - packageCode = " + packageCode);
			if (packageName == null)
			{
				// return;
				throw new Exception("Invalid packageName");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - packageName = " + packageName);
			if (packageDesc == null)
			{
				// return;
				throw new Exception("Invalid packageDesc");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - packageDesc = " + packageDesc);
			/*
			 * if (pos_uom == null) { //return; throw new Exception("Invalid
			 * pos_uom"); } else Log.printVerbose(strClassName + ":" + funcName + " -
			 * pos_uom = " + pos_uom);
			 */
			if (currency == null)
			{
				// return;
				throw new Exception("Invalid currency");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - currency = " + currency);
			if (stdPrice == null)
			{
				// return;
				throw new Exception("Invalid stdPrice");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - stdPrice = " + stdPrice);
			if (discPrice == null)
			{
				// return;
				throw new Exception("Invalid discPrice");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - discPrice = " + discPrice);
			if (minPrice == null)
			{
				// return;
				throw new Exception("Invalid minPrice");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - minPrice = " + minPrice);
			/*
			 * if (effDate_year == null) { //return; throw new
			 * Exception("Invalid effDate_year"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " - effDate_year = " +
			 * effDate_year); if (effDate_month == null) { //return; throw new
			 * Exception("Invalid effDate_month"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " -
			 * effDate_month = " + effDate_month); if (effDate_day == null) {
			 * //return; throw new Exception("Invalid effDate_day"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " - effDate_day = " +
			 * effDate_day);
			 */
			Log.printVerbose("Editing Package Inventory Item ... ");
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
			// Get the Package
			com.vlee.ejb.customer.Package lPkg = PackageNut.getHandle(new Integer(packageId));
			if (lPkg == null)
			{
				throw new Exception("Cannot Edit Null Package");
			}
			// Edit the Package Details here
			lPkg.setCode(packageCode);
			lPkg.setName(packageName);
			lPkg.setDescription(packageDesc);
			// lPkg.setUnitMeasure(pkg_uom);
			lPkg.setLastUpdate(tsCreate);
			lPkg.setUserIdUpdate(usrid);
			// Try to get the corresponding POSItem
			POSItem lPOSItem = POSItemNut.getPOSItem(new Integer(packageId), POSItemBean.TYPE_PKG);
			// Edit the posItem if exist, else create a new row for it
			if (lPOSItem != null)
			{
				lPOSItem.setCurrency(currency);
				lPOSItem.setUnitPriceStd(new BigDecimal(stdPrice));
				lPOSItem.setUnitPriceDiscounted(new BigDecimal(discPrice));
				lPOSItem.setUnitPriceMin(new BigDecimal(minPrice));
				lPOSItem.setLastUpdate(tsCreate);
				lPOSItem.setUserIdUpdate(usrid);
			} else
			{
				// Create a new row for it
				String lItemType = POSItemBean.TYPE_PKG;
				String lCurrency = "MYR"; // default
				Integer lCatId = new Integer(0); // default
				POSItemObject posObj = new POSItemObject();
				posObj.itemFKId = new Integer(packageId);
				posObj.itemType = lItemType;
				posObj.currency = lCurrency;
				posObj.unitPriceStd = new BigDecimal(stdPrice);
				posObj.unitPriceDiscounted = new BigDecimal(discPrice);
				posObj.unitPriceMin = new BigDecimal(minPrice);
				posObj.userIdUpdate = usrid;
				POSItem newPOSItem = POSItemNut.fnCreate(posObj);
				// if it reaches here, it has successfully created the POSItem
				if (newPOSItem != null)
				{
					String rtnMsg = "Successfully created POSItem for Package Code = " + packageCode;
					Log.printDebug(rtnMsg);
					req.setAttribute("rtnMsg", rtnMsg);
				}
			} // end if (lPOSItem != null)
		} // end try
		catch (Exception ex)
		{
			String rtnMsg = "Update POSItem for Package Code = " + packageCode + " -  Failed: " + ex.getMessage();
			Log.printDebug(rtnMsg);
			req.setAttribute("rtnMsg", rtnMsg);
		} // end try-catch
	} // end fnUpdatePackage

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
} // end class DoPOSPkgItemEdit
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

public class DoPOSPkgItemEdit implements Action
{
	String strClassName = "DoPOSPkgItemEdit";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName != null)
		{
			if (formName.compareTo("updatePackage") == 0)
			{
				// update the POS Package Item
				fnUpdatePackage(servlet, req, res);
				// fnGetParams(servlet, req, res);
			}
			if (formName.compareTo("remItemsFromPkg") == 0)
			{
				fnRemItems(servlet, req, res);
			}
			if (formName.compareTo("remSvcFromPkg") == 0)
			{
				fnRemServices(servlet, req, res);
			}
		}
		fnGetParams(servlet, req, res);
		return new ActionRouter("cust-pos-edit-pkg-items-page");
	}

	protected void fnGetParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetParams()";
		/*
		 * Need to populate 4 things: [1] (Package) editPkg, given the
		 * packageCode [2] (POSItem) posPkgItem corresponding to the editPkg [
		 * if exist ] [3] (Vector) vecItemCodes, vecItemNames, vecInvQuantities
		 * [4] (Vector) vecSvcCodes, vecSvcNames, vecSvcQuantities
		 * 
		 */
		Vector vecItemCodes = new Vector();
		Vector vecItemNames = new Vector();
		Vector vecInvQuantities = new Vector();
		Vector vecSvcCodes = new Vector();
		Vector vecSvcNames = new Vector();
		Vector vecSvcQuantities = new Vector();
		try
		{ // super huge try block
			// Obtain the packageId from packageCode
			String lPkgCode = req.getParameter("packageCode");
			com.vlee.ejb.customer.Package lPackage = PackageNut.getObjectByCode(lPkgCode);
			// Get all items attached to this package
			if (lPackage != null)
			{
				req.setAttribute("editPkg", lPackage);
				// Now try to obtain the posPkgItem corresponding to editPkg
				POSItem posPkgItem = POSItemNut.getPOSItem(lPackage.getPkid(), POSItemBean.TYPE_PKG);
				if (posPkgItem != null)
				{
					req.setAttribute("posPkgItem", posPkgItem);
				}
				Collection colPkgItems = PackageInvItemNut.getObjectsByPkgId(lPackage.getPkid());
				if (colPkgItems != null)
				{
					// for each pkgItem row, obtain the invItemId, then get the
					// Item object, and the itemCode and itemName
					for (Iterator itr = colPkgItems.iterator(); itr.hasNext();)
					{
						PackageInvItem lPkgItem = (PackageInvItem) itr.next();
						Integer lInvItemId = lPkgItem.getInvItemId();
						Item lItem = ItemNut.getHandle(lInvItemId);
						if (lItem == null)
						{
							throw new Exception("Null item for itemId = " + lInvItemId);
						}
						// populate the vecItemCodes and vecItemNames
						vecItemCodes.add(lItem.getItemCode());
						vecItemNames.add(lItem.getName());
						vecInvQuantities.add(lPkgItem.getQuantity());
					} // end for (Iterator itr = colPkgItems.iterator();
						// itr.hasNext();)
				} // end if (colPkgItems != null)
				Collection colPkgSvcs = PackageServiceNut.getObjectsByPkgId(lPackage.getPkid());
				if (colPkgSvcs != null)
				{
					// for each pkgItem row, obtain the serviceId, then get the
					// Service object, and the serviceCode and serviceName
					for (Iterator itr = colPkgSvcs.iterator(); itr.hasNext();)
					{
						PackageService lPkgSvc = (PackageService) itr.next();
						Integer lSvcId = lPkgSvc.getServiceId();
						Service lSvc = ServiceNut.getHandle(lSvcId);
						if (lSvc == null)
						{
							throw new Exception("Null item for serviceId = " + lSvcId);
						}
						// populate the vecItemCodes and vecItemNames
						vecSvcCodes.add(lSvc.getCode());
						vecSvcNames.add(lSvc.getName());
						vecSvcQuantities.add(lPkgSvc.getQuantity());
					} // end for (Iterator itr = colPkgSvcs.iterator();
						// itr.hasNext();)
				} // end if (colPkgSvcs != null)
				// Now try to obtain the posPkgItem corresponding to editPkg
				// OK I'm stuck here !!! without the Effective Date,
				// how to get a unique posPkgItem???
				// populate the attributes to be sent to the JSPs
				req.setAttribute("vecItemCodes", vecItemCodes);
				req.setAttribute("vecItemNames", vecItemNames);
				req.setAttribute("vecInvQuantities", vecInvQuantities);
				req.setAttribute("vecSvcCodes", vecSvcCodes);
				req.setAttribute("vecSvcNames", vecSvcNames);
				req.setAttribute("vecSvcQuantities", vecSvcQuantities);
			} // end if (lPackage != null)
		} catch (Exception ex)
		{
			Log.printDebug(strClassName + ":" + funcName + " - " + ex.getMessage());
		}
	} // end fnGetParams

	protected void fnRemItems(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnRemItems()";
		// Get the required params
		String packageId = req.getParameter("packageId");
		String packageCode = req.getParameter("packageCode");
		String[] removeItems = req.getParameterValues("removeItems");
		String rtnMsg;
		try
		{
			if (packageId == null || packageCode == null || removeItems == null)
			{
				rtnMsg = "Null packageId, packageCode or removeItems";
				throw new Exception(rtnMsg);
			}
			HttpSession session = req.getSession();
			User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
			if (lUsr == null)
			{
				Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
				throw new Exception(
						"Invalid Username. Your session may have expired or you may not be authorised to access the system. Please login again");
			}
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
			for (int i = 0; i < removeItems.length; i++)
			{
				// Obtain the itemId to remove
				Item lItem = ItemNut.getObjectByCode(removeItems[i]);
				Integer lItemId = lItem.getPkid();
				// Get the packageItem link
				PackageInvItem lPkgInv = PackageInvItemNut.getObjectByPkgAndInv(new Integer(packageId), lItemId);
				if (lPkgInv != null)
				{
					lPkgInv.remove();
				} else
				{
					Log.printDebug("Package: " + packageCode + " and " + "Item: " + removeItems[i] + " not found!");
				} // end if (lPkgInv != null)
			} // end for(int i=0; i<removeItems.length; i++)
		} catch (Exception ex)
		{
			rtnMsg = "Failed to Remove Items from package '" + packageCode + "'. Reason = ";
			rtnMsg += ex.getMessage();
			Log.printDebug(rtnMsg);
			req.setAttribute("rtnMsg", rtnMsg);
		}
		// 
	} // end fnRemItems()

	protected void fnRemServices(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnRemServices()";
		// Get the required params
		String packageId = req.getParameter("packageId");
		String packageCode = req.getParameter("packageCode");
		String[] removeServices = req.getParameterValues("removeServices");
		String rtnMsg;
		try
		{
			if (packageId == null || packageCode == null || removeServices == null)
			{
				rtnMsg = "Null packageId, packageCode or removeServices";
				throw new Exception(rtnMsg);
			}
			HttpSession session = req.getSession();
			User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
			if (lUsr == null)
			{
				Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
				throw new Exception(
						"Invalid Username. Your session may have expired or you may not be authorised to access the system. Please login again");
			}
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
			for (int i = 0; i < removeServices.length; i++)
			{
				// Obtain the serviceId to remove
				Service lSvc = ServiceNut.getObjectByCode(removeServices[i]);
				Integer lSvcId = lSvc.getPkid();
				// Get the packageService link
				PackageService lPkgSvc = PackageServiceNut.getObjectByPkgAndSvc(new Integer(packageId), lSvcId);
				if (lPkgSvc != null)
				{
					lPkgSvc.remove();
				} else
				{
					Log.printDebug("Package: " + packageCode + " and " + "Service: " + removeServices[i]
							+ " not found!");
				} // end if (lPkgSvc != null)
			} // end for(int i=0; i<removeServices.length; i++)
		} catch (Exception ex)
		{
			rtnMsg = "Failed to Remove Services from package '" + packageCode + "'. Reason = ";
			rtnMsg += ex.getMessage();
			Log.printDebug(rtnMsg);
			req.setAttribute("rtnMsg", rtnMsg);
		}
		//
	} // end fnRemServices()

	protected void fnUpdatePackage(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdatePackage()";
		// Get the request paramaters
		String packageId = (String) req.getParameter("packageId");
		String packageCode = (String) req.getParameter("packageCode");
		String packageName = (String) req.getParameter("packageName");
		String packageDesc = (String) req.getParameter("packageDesc");
		// String pos_uom = req.getParameter("pos_uom");
		String currency = req.getParameter("currency");
		String stdPrice = req.getParameter("stdPrice");
		String discPrice = req.getParameter("discPrice");
		String minPrice = req.getParameter("minPrice");
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
			if (packageCode == null)
			{
				// return;
				throw new Exception("Invalid Item Code");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - packageCode = " + packageCode);
			if (packageName == null)
			{
				// return;
				throw new Exception("Invalid packageName");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - packageName = " + packageName);
			if (packageDesc == null)
			{
				// return;
				throw new Exception("Invalid packageDesc");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - packageDesc = " + packageDesc);
			/*
			 * if (pos_uom == null) { //return; throw new Exception("Invalid
			 * pos_uom"); } else Log.printVerbose(strClassName + ":" + funcName + " -
			 * pos_uom = " + pos_uom);
			 */
			if (currency == null)
			{
				// return;
				throw new Exception("Invalid currency");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - currency = " + currency);
			if (stdPrice == null)
			{
				// return;
				throw new Exception("Invalid stdPrice");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - stdPrice = " + stdPrice);
			if (discPrice == null)
			{
				// return;
				throw new Exception("Invalid discPrice");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - discPrice = " + discPrice);
			if (minPrice == null)
			{
				// return;
				throw new Exception("Invalid minPrice");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - minPrice = " + minPrice);
			/*
			 * if (effDate_year == null) { //return; throw new
			 * Exception("Invalid effDate_year"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " - effDate_year = " +
			 * effDate_year); if (effDate_month == null) { //return; throw new
			 * Exception("Invalid effDate_month"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " -
			 * effDate_month = " + effDate_month); if (effDate_day == null) {
			 * //return; throw new Exception("Invalid effDate_day"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " - effDate_day = " +
			 * effDate_day);
			 */
			Log.printVerbose("Editing Package Inventory Item ... ");
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
			// Get the Package
			com.vlee.ejb.customer.Package lPkg = PackageNut.getHandle(new Integer(packageId));
			if (lPkg == null)
			{
				throw new Exception("Cannot Edit Null Package");
			}
			// Edit the Package Details here
			lPkg.setCode(packageCode);
			lPkg.setName(packageName);
			lPkg.setDescription(packageDesc);
			// lPkg.setUnitMeasure(pkg_uom);
			lPkg.setLastUpdate(tsCreate);
			lPkg.setUserIdUpdate(usrid);
			// Try to get the corresponding POSItem
			POSItem lPOSItem = POSItemNut.getPOSItem(new Integer(packageId), POSItemBean.TYPE_PKG);
			// Edit the posItem if exist, else create a new row for it
			if (lPOSItem != null)
			{
				lPOSItem.setCurrency(currency);
				lPOSItem.setUnitPriceStd(new BigDecimal(stdPrice));
				lPOSItem.setUnitPriceDiscounted(new BigDecimal(discPrice));
				lPOSItem.setUnitPriceMin(new BigDecimal(minPrice));
				lPOSItem.setLastUpdate(tsCreate);
				lPOSItem.setUserIdUpdate(usrid);
			} else
			{
				// Create a new row for it
				String lItemType = POSItemBean.TYPE_PKG;
				String lCurrency = "MYR"; // default
				Integer lCatId = new Integer(0); // default
				POSItemObject posObj = new POSItemObject();
				posObj.itemFKId = new Integer(packageId);
				posObj.itemType = lItemType;
				posObj.currency = lCurrency;
				posObj.unitPriceStd = new BigDecimal(stdPrice);
				posObj.unitPriceDiscounted = new BigDecimal(discPrice);
				posObj.unitPriceMin = new BigDecimal(minPrice);
				posObj.userIdUpdate = usrid;
				POSItem newPOSItem = POSItemNut.fnCreate(posObj);
				// if it reaches here, it has successfully created the POSItem
				if (newPOSItem != null)
				{
					String rtnMsg = "Successfully created POSItem for Package Code = " + packageCode;
					Log.printDebug(rtnMsg);
					req.setAttribute("rtnMsg", rtnMsg);
				}
			} // end if (lPOSItem != null)
		} // end try
		catch (Exception ex)
		{
			String rtnMsg = "Update POSItem for Package Code = " + packageCode + " -  Failed: " + ex.getMessage();
			Log.printDebug(rtnMsg);
			req.setAttribute("rtnMsg", rtnMsg);
		} // end try-catch
	} // end fnUpdatePackage

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
} // end class DoPOSPkgItemEdit
