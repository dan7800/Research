/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.customer;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.vlee.ejb.customer.PackageInvItem;
import com.vlee.ejb.customer.PackageInvItemHome;
import com.vlee.ejb.customer.PackageInvItemNut;
import com.vlee.ejb.inventory.ItemBean;
import com.vlee.ejb.inventory.ItemHome;
import com.vlee.ejb.inventory.ItemNut;
import com.vlee.ejb.inventory.ItemObject;
import com.vlee.ejb.user.User;
import com.vlee.ejb.user.UserNut;
import com.vlee.servlet.main.Action;
import com.vlee.servlet.main.ActionRouter;
import com.vlee.util.Log;
import com.vlee.util.TimeFormat;

public class DoPOSAddInvToPkg implements Action
{
	private String strClassName = "DoPOSAddInvToPkg";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Get the formName
		String formName = (String) req.getParameter("formName");
		if (formName != null)
		{
			Log.printVerbose("formName = " + formName);
			if (formName.compareTo("addInvToPkg") == 0)
			{
				// Propagate the formName back
				// req.setAttribute("formName", "addInvToPkg");
				fnAddItemToPkg(servlet, req, res);
				return new ActionRouter("cust-pos-added-inv-to-pkg-page");
			}
			if (formName.compareTo("showAddItems") == 0)
			{
				// Propagate the formName back
				// req.setAttribute("formName", "showAddItems");
				fnGetAllInvItems(servlet, req, res);
			}
		}
		return new ActionRouter("cust-pos-add-inv-to-pkg-page");
	}

	protected void fnGetAllInvItems(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetAllInvItems()";
		// propagate the packageId to the receiving JSP
		String packageId = req.getParameter("packageId");
		String packageCode = req.getParameter("packageCode");
		req.setAttribute("packageId", packageId);
		req.setAttribute("packageCode", packageCode);
		try
		{
			ItemHome lPkgHome = (ItemHome) ItemNut.getHome();
			if (lPkgHome != null)
			{
				String lSortBy = ItemBean.ITEM_CODE;
				// Vector vecItemCodes = new Vector();
				Vector vecItemPkid = new Vector();
				Vector vecItemNames = new Vector();
				// Collection colItemObj = (Collection)
				// lPkgHome.getAllCodeName(lSortBy);
				Collection colItemObj = ItemNut.getActiveInvValObjects(lSortBy);
				/*
				 * if (colPkg != null) { for (Iterator itr = colPkg.iterator();
				 * itr.hasNext();) { com.vlee.ejb.customer.Item lCustAcc =
				 * (com.vlee.ejb.customer.Item) itr.next();
				 * vecItemCodes.add(lCustAcc.getCode());
				 * vecItemNames.add(lCustAcc.getName()); }
				 *  }
				 */
				// proof of concept, split colItemObj into vecItemCode and
				// vecItemName
				for (Iterator itemObjItr = colItemObj.iterator(); itemObjItr.hasNext();)
				{
					ItemObject lItemObj = (ItemObject) itemObjItr.next();
					// vecItemCodes.add(lItemObj.getCode());
					// vecItemNames.add(lItemObj.getName());
					// vecItemCodes.add(lItemObj.code);
					vecItemPkid.add(lItemObj.pkid);
					vecItemNames.add(lItemObj.name);
				}
				if (vecItemPkid != null && vecItemNames != null)
				{
					Log.printVerbose(strClassName + ":" + funcName + " - vecItemPkid = " + debugVector(vecItemPkid));
					Log.printVerbose(strClassName + ":" + funcName + " - vecItemNames = " + debugVector(vecItemNames));
					req.setAttribute("vecItemPkid", vecItemPkid);
					req.setAttribute("vecItemNames", vecItemNames);
				}
			}
		} catch (Exception ex)
		{
			Log.printDebug(strClassName + ":" + funcName + " - " + ex.getMessage());
			return;
		}
	}

	protected void fnAddItemToPkg(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAddItemToPkg()";
		// Get the required parameters
		String itemsToAdd = (String) req.getParameter("itemsToAdd");
		String itemQuantities = (String) req.getParameter("itemQty");
		String packageId = (String) req.getParameter("packageId");
		String lPkgCode = (String) req.getParameter("packageCode");
		try
		{
			if (itemsToAdd == null || packageId == null)
			{
				String rtnMsg = "Failed to obtain all parameters required to proceed";
				throw new Exception(rtnMsg);
			}
			HttpSession session = req.getSession();
			User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
			if (lUsr == null)
			{
				Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
				throw new Exception("Invalid Username. Your session " + "may have expired or you may not be"
						+ " authorised to access the system. Please login again");
			}
			Timestamp tsCreate = TimeFormat.getTimestamp();
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
			// Now start the adding ....
			Vector vecItemsAdded = new Vector();
			String strQty;
			BigDecimal qtyToAdd;
			qtyToAdd = new BigDecimal(itemQuantities);
			Integer lItemId = new Integer(itemsToAdd);
			/**
			 * NEED TO CHECK IF PACKAGE ITEM LINK EXISTS FIRST, IF SO,
			 * EDIT ONLY THE QTY FIELD AND NOT ADD ANOTHER ROW!!!
			 */
			// create the package-item link
			PackageInvItemHome lPkgInvHome = PackageInvItemNut.getHome();
			PackageInvItem lPkgInv = lPkgInvHome.create(new Integer(packageId), lItemId, qtyToAdd, "", tsCreate, usrid);
			// populate all successful additions
			if (lPkgInv != null)
			{
				// vecItemsAdded.add(itemsToAdd);
				vecItemsAdded.add(lItemId);
			}
			// if it reached here, it's probably added the items successfully
			String rtnMsg = "Successfully added the following inventory items to package " + lPkgCode;
			req.setAttribute("packageId", packageId);
			req.setAttribute("packageCode", lPkgCode);
			req.setAttribute("vecItemsAdded", vecItemsAdded);
			req.setAttribute("rtnMsg", rtnMsg);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			String rtnMsg = "Failed to Add Items to Package " + lPkgCode + ". Reason = " + ex.getMessage();
			Log.printDebug(rtnMsg);
			req.setAttribute("rtnMsg", rtnMsg);
			Boolean error = new Boolean(true);
			req.setAttribute("error", error);
		}
	} // fnAddItemToPkg

	private String debugVector(Vector vec)
	{
		String debugStr = "";
		for (Iterator itr = vec.iterator(); itr.hasNext();)
		{
			debugStr += itr.next().toString() + "\n";
		}
		return debugStr;
	}
}
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.customer;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import java.math.BigDecimal;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.inventory.*;

public class DoPOSAddInvToPkg implements Action
{
	private String strClassName = "DoPOSAddInvToPkg";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Get the formName
		String formName = (String) req.getParameter("formName");
		if (formName != null)
		{
			Log.printVerbose("formName = " + formName);
			if (formName.compareTo("addInvToPkg") == 0)
			{
				// Propagate the formName back
				// req.setAttribute("formName", "addInvToPkg");
				fnAddItemToPkg(servlet, req, res);
				return new ActionRouter("cust-pos-added-inv-to-pkg-page");
			}
			if (formName.compareTo("showAddItems") == 0)
			{
				// Propagate the formName back
				// req.setAttribute("formName", "showAddItems");
				fnGetAllInvItems(servlet, req, res);
			}
		}
		return new ActionRouter("cust-pos-add-inv-to-pkg-page");
	}

	protected void fnGetAllInvItems(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetAllInvItems()";
		// propagate the packageId to the receiving JSP
		String packageId = req.getParameter("packageId");
		String packageCode = req.getParameter("packageCode");
		req.setAttribute("packageId", packageId);
		req.setAttribute("packageCode", packageCode);
		try
		{
			ItemHome lPkgHome = (ItemHome) ItemNut.getHome();
			if (lPkgHome != null)
			{
				String lSortBy = ItemBean.ITEM_CODE;
				// Vector vecItemCodes = new Vector();
				Vector vecItemPkid = new Vector();
				Vector vecItemNames = new Vector();
				// Collection colItemObj = (Collection)
				// lPkgHome.getAllCodeName(lSortBy);
				Collection colItemObj = ItemNut.getActiveInvValObjects(lSortBy);
				/*
				 * if (colPkg != null) { for (Iterator itr = colPkg.iterator();
				 * itr.hasNext();) { com.vlee.ejb.customer.Item lCustAcc =
				 * (com.vlee.ejb.customer.Item) itr.next();
				 * vecItemCodes.add(lCustAcc.getCode());
				 * vecItemNames.add(lCustAcc.getName()); }
				 *  }
				 */
				// proof of concept, split colItemObj into vecItemCode and
				// vecItemName
				for (Iterator itemObjItr = colItemObj.iterator(); itemObjItr.hasNext();)
				{
					ItemObject lItemObj = (ItemObject) itemObjItr.next();
					// vecItemCodes.add(lItemObj.getCode());
					// vecItemNames.add(lItemObj.getName());
					// vecItemCodes.add(lItemObj.code);
					vecItemPkid.add(lItemObj.pkid);
					vecItemNames.add(lItemObj.name);
				}
				if (vecItemPkid != null && vecItemNames != null)
				{
					Log.printVerbose(strClassName + ":" + funcName + " - vecItemPkid = " + debugVector(vecItemPkid));
					Log.printVerbose(strClassName + ":" + funcName + " - vecItemNames = " + debugVector(vecItemNames));
					req.setAttribute("vecItemPkid", vecItemPkid);
					req.setAttribute("vecItemNames", vecItemNames);
				}
			}
		} catch (Exception ex)
		{
			Log.printDebug(strClassName + ":" + funcName + " - " + ex.getMessage());
			return;
		}
	}

	protected void fnAddItemToPkg(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAddItemToPkg()";
		// Get the required parameters
		String itemsToAdd = (String) req.getParameter("itemsToAdd");
		String itemQuantities = (String) req.getParameter("itemQty");
		String packageId = (String) req.getParameter("packageId");
		String lPkgCode = (String) req.getParameter("packageCode");
		try
		{
			if (itemsToAdd == null || packageId == null)
			{
				String rtnMsg = "Failed to obtain all parameters required to proceed";
				throw new Exception(rtnMsg);
			}
			HttpSession session = req.getSession();
			User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
			if (lUsr == null)
			{
				Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
				throw new Exception("Invalid Username. Your session " + "may have expired or you may not be"
						+ " authorised to access the system. Please login again");
			}
			Timestamp tsCreate = TimeFormat.getTimestamp();
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
			// Now start the adding ....
			Vector vecItemsAdded = new Vector();
			String strQty;
			BigDecimal qtyToAdd;
			qtyToAdd = new BigDecimal(itemQuantities);
			Integer lItemId = new Integer(itemsToAdd);
			/**
			 * TODO: NEED TO CHECK IF PACKAGE ITEM LINK EXISTS FIRST, IF SO,
			 * EDIT ONLY THE QTY FIELD AND NOT ADD ANOTHER ROW!!!
			 */
			// create the package-item link
			PackageInvItemHome lPkgInvHome = PackageInvItemNut.getHome();
			PackageInvItem lPkgInv = lPkgInvHome.create(new Integer(packageId), lItemId, qtyToAdd, "", tsCreate, usrid);
			// populate all successful additions
			if (lPkgInv != null)
			{
				// vecItemsAdded.add(itemsToAdd);
				vecItemsAdded.add(lItemId);
			}
			// if it reached here, it's probably added the items successfully
			String rtnMsg = "Successfully added the following inventory items to package " + lPkgCode;
			req.setAttribute("packageId", packageId);
			req.setAttribute("packageCode", lPkgCode);
			req.setAttribute("vecItemsAdded", vecItemsAdded);
			req.setAttribute("rtnMsg", rtnMsg);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			String rtnMsg = "Failed to Add Items to Package " + lPkgCode + ". Reason = " + ex.getMessage();
			Log.printDebug(rtnMsg);
			req.setAttribute("rtnMsg", rtnMsg);
			Boolean error = new Boolean(true);
			req.setAttribute("error", error);
		}
	} // fnAddItemToPkg

	private String debugVector(Vector vec)
	{
		String debugStr = "";
		for (Iterator itr = vec.iterator(); itr.hasNext();)
		{
			debugStr += itr.next().toString() + "\n";
		}
		return debugStr;
	}
}
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.customer;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.vlee.ejb.customer.PackageInvItem;
import com.vlee.ejb.customer.PackageInvItemHome;
import com.vlee.ejb.customer.PackageInvItemNut;
import com.vlee.ejb.inventory.ItemBean;
import com.vlee.ejb.inventory.ItemHome;
import com.vlee.ejb.inventory.ItemNut;
import com.vlee.ejb.inventory.ItemObject;
import com.vlee.ejb.user.User;
import com.vlee.ejb.user.UserNut;
import com.vlee.servlet.main.Action;
import com.vlee.servlet.main.ActionRouter;
import com.vlee.util.Log;
import com.vlee.util.TimeFormat;

public class DoPOSAddInvToPkg implements Action
{
	private String strClassName = "DoPOSAddInvToPkg";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Get the formName
		String formName = (String) req.getParameter("formName");
		if (formName != null)
		{
			Log.printVerbose("formName = " + formName);
			if (formName.compareTo("addInvToPkg") == 0)
			{
				// Propagate the formName back
				// req.setAttribute("formName", "addInvToPkg");
				fnAddItemToPkg(servlet, req, res);
				return new ActionRouter("cust-pos-added-inv-to-pkg-page");
			}
			if (formName.compareTo("showAddItems") == 0)
			{
				// Propagate the formName back
				// req.setAttribute("formName", "showAddItems");
				fnGetAllInvItems(servlet, req, res);
			}
		}
		return new ActionRouter("cust-pos-add-inv-to-pkg-page");
	}

	protected void fnGetAllInvItems(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetAllInvItems()";
		// propagate the packageId to the receiving JSP
		String packageId = req.getParameter("packageId");
		String packageCode = req.getParameter("packageCode");
		req.setAttribute("packageId", packageId);
		req.setAttribute("packageCode", packageCode);
		try
		{
			ItemHome lPkgHome = (ItemHome) ItemNut.getHome();
			if (lPkgHome != null)
			{
				String lSortBy = ItemBean.ITEM_CODE;
				// Vector vecItemCodes = new Vector();
				Vector vecItemPkid = new Vector();
				Vector vecItemNames = new Vector();
				// Collection colItemObj = (Collection)
				// lPkgHome.getAllCodeName(lSortBy);
				Collection colItemObj = ItemNut.getActiveInvValObjects(lSortBy);
				/*
				 * if (colPkg != null) { for (Iterator itr = colPkg.iterator();
				 * itr.hasNext();) { com.vlee.ejb.customer.Item lCustAcc =
				 * (com.vlee.ejb.customer.Item) itr.next();
				 * vecItemCodes.add(lCustAcc.getCode());
				 * vecItemNames.add(lCustAcc.getName()); }
				 *  }
				 */
				// proof of concept, split colItemObj into vecItemCode and
				// vecItemName
				for (Iterator itemObjItr = colItemObj.iterator(); itemObjItr.hasNext();)
				{
					ItemObject lItemObj = (ItemObject) itemObjItr.next();
					// vecItemCodes.add(lItemObj.getCode());
					// vecItemNames.add(lItemObj.getName());
					// vecItemCodes.add(lItemObj.code);
					vecItemPkid.add(lItemObj.pkid);
					vecItemNames.add(lItemObj.name);
				}
				if (vecItemPkid != null && vecItemNames != null)
				{
					Log.printVerbose(strClassName + ":" + funcName + " - vecItemPkid = " + debugVector(vecItemPkid));
					Log.printVerbose(strClassName + ":" + funcName + " - vecItemNames = " + debugVector(vecItemNames));
					req.setAttribute("vecItemPkid", vecItemPkid);
					req.setAttribute("vecItemNames", vecItemNames);
				}
			}
		} catch (Exception ex)
		{
			Log.printDebug(strClassName + ":" + funcName + " - " + ex.getMessage());
			return;
		}
	}

	protected void fnAddItemToPkg(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAddItemToPkg()";
		// Get the required parameters
		String itemsToAdd = (String) req.getParameter("itemsToAdd");
		String itemQuantities = (String) req.getParameter("itemQty");
		String packageId = (String) req.getParameter("packageId");
		String lPkgCode = (String) req.getParameter("packageCode");
		try
		{
			if (itemsToAdd == null || packageId == null)
			{
				String rtnMsg = "Failed to obtain all parameters required to proceed";
				throw new Exception(rtnMsg);
			}
			HttpSession session = req.getSession();
			User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
			if (lUsr == null)
			{
				Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
				throw new Exception("Invalid Username. Your session " + "may have expired or you may not be"
						+ " authorised to access the system. Please login again");
			}
			Timestamp tsCreate = TimeFormat.getTimestamp();
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
			// Now start the adding ....
			Vector vecItemsAdded = new Vector();
			String strQty;
			BigDecimal qtyToAdd;
			qtyToAdd = new BigDecimal(itemQuantities);
			Integer lItemId = new Integer(itemsToAdd);
			/**
			 * NEED TO CHECK IF PACKAGE ITEM LINK EXISTS FIRST, IF SO,
			 * EDIT ONLY THE QTY FIELD AND NOT ADD ANOTHER ROW!!!
			 */
			// create the package-item link
			PackageInvItemHome lPkgInvHome = PackageInvItemNut.getHome();
			PackageInvItem lPkgInv = lPkgInvHome.create(new Integer(packageId), lItemId, qtyToAdd, "", tsCreate, usrid);
			// populate all successful additions
			if (lPkgInv != null)
			{
				// vecItemsAdded.add(itemsToAdd);
				vecItemsAdded.add(lItemId);
			}
			// if it reached here, it's probably added the items successfully
			String rtnMsg = "Successfully added the following inventory items to package " + lPkgCode;
			req.setAttribute("packageId", packageId);
			req.setAttribute("packageCode", lPkgCode);
			req.setAttribute("vecItemsAdded", vecItemsAdded);
			req.setAttribute("rtnMsg", rtnMsg);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			String rtnMsg = "Failed to Add Items to Package " + lPkgCode + ". Reason = " + ex.getMessage();
			Log.printDebug(rtnMsg);
			req.setAttribute("rtnMsg", rtnMsg);
			Boolean error = new Boolean(true);
			req.setAttribute("error", error);
		}
	} // fnAddItemToPkg

	private String debugVector(Vector vec)
	{
		String debugStr = "";
		for (Iterator itr = vec.iterator(); itr.hasNext();)
		{
			debugStr += itr.next().toString() + "\n";
		}
		return debugStr;
	}
}
