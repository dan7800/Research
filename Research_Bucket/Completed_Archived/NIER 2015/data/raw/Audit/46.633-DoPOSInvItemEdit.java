/*==========================================================
 *
 * Copyright  of Vincent Lee (vlee@vlee.net,
 *      vincent@wavelet.biz). All Rights Reserved.
 * (http://www.wavelet.biz)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
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

public class DoPOSInvItemEdit implements Action
{
	String strClassName = "DoPOSInvItemEdit";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName != null)
		{
			if (formName.compareTo("updateInventory") == 0)
			{
				// update the POS Inventory Item
				fnUpdateItem(servlet, req, res);
				// fnGetParams(servlet, req, res);
			}
		}
		fnGetParams(servlet, req, res);
		return new ActionRouter("cust-pos-edit-inv-items-page");
	}

	protected void fnGetParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetParams()";
		/*
		 * Need to populate 2 things: [1] (Item) editInv, given the itemCode [2]
		 * (POSItem) posInvItem corresponding to the editInv [ if exist ]
		 */
		try
		{ // super huge try block
			// Obtain the itemId from itemCode
			String lInvCode = req.getParameter("itemCode");
			Item lItem = ItemNut.getObjectByCode(lInvCode);
			if (lItem != null)
			{
				req.setAttribute("editInv", lItem);
				// Now try to obtain the posInvItem corresponding to editInv
				// Alex: 07/20/03 - need to cater for non-inventory
				String lPOSInvType = POSItemBean.TYPE_INV;
				if (lItem.getEnumInvType().intValue() == ItemBean.INV_TYPE_NONSTK)
					lPOSInvType = POSItemBean.TYPE_NSTK;
				else if (lItem.getEnumInvType().intValue() == ItemBean.INV_TYPE_TRADEIN)
					lPOSInvType = POSItemBean.TYPE_TRADEIN_SELL;
				POSItem posInvItem = POSItemNut.getPOSInvItem(lItem.getPkid(), lPOSInvType);
				if (posInvItem != null)
				{
					req.setAttribute("posInvItem", posInvItem);
				}
			} // end if (lItem != null)
		} catch (Exception ex)
		{
			Log.printDebug(strClassName + ":" + funcName + " - " + ex.getMessage());
		}
	} // end fnGetParams

	protected void fnUpdateItem(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateItem()";
		// Get the request paramaters
		String itemId = (String) req.getParameter("itemId");
		String itemCode = (String) req.getParameter("itemCode");
		String itemType = (String) req.getParameter("itemType");
		// String itemName = (String) req.getParameter("itemName");
		// String itemDesc = (String) req.getParameter("itemDesc");
		// String itemUOM = req.getParameter("itemUOM");
		String currency = req.getParameter("currency");
		String stdPrice = req.getParameter("stdPrice");
		String discPrice = req.getParameter("discPrice");
		String minPrice = req.getParameter("minPrice");
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
			if (itemCode == null)
			{
				// return;
				throw new Exception("Invalid Item Code");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - itemCode = " + itemCode);
			/*
			 * if (itemName == null) { //return; throw new Exception("Invalid
			 * itemName"); } else Log.printVerbose(strClassName + ":" + funcName + " -
			 * itemName = " + itemName); if (itemDesc == null) { //return; throw
			 * new Exception("Invalid itemDesc"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " - itemDesc = " +
			 * itemDesc); if (itemUOM == null) { //return; throw new
			 * Exception("Invalid itemUOM"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " - itemUOM = " +
			 * itemUOM);
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
			Log.printVerbose("Editing Item Inventory Item ... ");
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
			// Get the Item
			Item lInv = ItemNut.getHandle(new Integer(itemId));
			if (lInv == null)
			{
				throw new Exception("Cannot Edit Null Item");
			}
			/*
			 * // Edit the Item Details here lInv.setCode(itemCode);
			 * lInv.setName(itemName); lInv.setDescription(itemDesc);
			 * lInv.setUnitMeasure(itemUOM); lInv.setLastUpdate(tsCreate);
			 * lInv.setUserIdUpdate(usrid);
			 */
			// Try to get the corresponding POSItem
			// Alex: 07/20/03 - Need to cater for non-inventory as well
			String lPOSInvType = POSItemBean.TYPE_INV;
			if (Integer.parseInt(itemType) == ItemBean.INV_TYPE_NONSTK)
				lPOSInvType = POSItemBean.TYPE_NSTK;
			else if (Integer.parseInt(itemType) == ItemBean.INV_TYPE_TRADEIN)
				lPOSInvType = POSItemBean.TYPE_TRADEIN_SELL;
			POSItem lPOSItem = POSItemNut.getPOSInvItem(new Integer(itemId), lPOSInvType);
			// Edit the posItem if exist, else create a new row for it
			if (lPOSItem != null)
			{
				lPOSItem.setCurrency(currency);
				lPOSItem.setUnitPriceStd(new BigDecimal(stdPrice));
				lPOSItem.setUnitPriceDiscounted(new BigDecimal(discPrice));
				lPOSItem.setUnitPriceMin(new BigDecimal(minPrice));
				lPOSItem.setLastUpdate(tsCreate);
				lPOSItem.setUserIdUpdate(usrid);
				// if it reaches here, it has successfully edited the POSItem
				String rtnMsg = "Successfully edited POSItem for Item Code = " + itemCode;
				Log.printDebug(rtnMsg);
				req.setAttribute("rtnMsg", rtnMsg);
			} else
			{
				// Create a new row for it
				String lItemType = POSItemBean.TYPE_INV;
				String lCurrency = "MYR"; // default
				Integer lCatId = new Integer(0); // default
				POSItemObject posObj = new POSItemObject();
				posObj.itemFKId = new Integer(itemId);
				Log.printVerbose(" the foreign keyyyyyyyyyyyyy is " + posObj.itemFKId.toString());
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
					String rtnMsg = "Successfully created POSItem for Item Code = " + itemCode;
					Log.printDebug(rtnMsg);
					req.setAttribute("rtnMsg", rtnMsg);
				}
			} // end if(lPOSItem != null)
		} // end try
		catch (Exception ex)
		{
			ex.printStackTrace();
			String rtnMsg = "Update POSItem for Item Code = " + itemCode + " -  Failed: " + ex.getMessage();
			Log.printDebug(rtnMsg);
			req.setAttribute("rtnMsg", rtnMsg);
		} // end try-catch
	} // end fnUpdateItem
} // end class DoPOSInvItemEdit
/*==========================================================
 *
 * Copyright  of Vincent Lee (vlee@vlee.net,
 *      vincent@wavelet.biz). All Rights Reserved.
 * (http://www.wavelet.biz)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
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

public class DoPOSInvItemEdit implements Action
{
	String strClassName = "DoPOSInvItemEdit";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName != null)
		{
			if (formName.compareTo("updateInventory") == 0)
			{
				// update the POS Inventory Item
				fnUpdateItem(servlet, req, res);
				// fnGetParams(servlet, req, res);
			}
		}
		fnGetParams(servlet, req, res);
		return new ActionRouter("cust-pos-edit-inv-items-page");
	}

	protected void fnGetParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetParams()";
		/*
		 * Need to populate 2 things: [1] (Item) editInv, given the itemCode [2]
		 * (POSItem) posInvItem corresponding to the editInv [ if exist ]
		 */
		try
		{ // super huge try block
			// Obtain the itemId from itemCode
			String lInvCode = req.getParameter("itemCode");
			Item lItem = ItemNut.getObjectByCode(lInvCode);
			if (lItem != null)
			{
				req.setAttribute("editInv", lItem);
				// Now try to obtain the posInvItem corresponding to editInv
				// Alex: 07/20/03 - need to cater for non-inventory
				String lPOSInvType = POSItemBean.TYPE_INV;
				if (lItem.getEnumInvType().intValue() == ItemBean.INV_TYPE_NONSTK)
					lPOSInvType = POSItemBean.TYPE_NSTK;
				else if (lItem.getEnumInvType().intValue() == ItemBean.INV_TYPE_TRADEIN)
					lPOSInvType = POSItemBean.TYPE_TRADEIN_SELL;
				POSItem posInvItem = POSItemNut.getPOSInvItem(lItem.getPkid(), lPOSInvType);
				if (posInvItem != null)
				{
					req.setAttribute("posInvItem", posInvItem);
				}
			} // end if (lItem != null)
		} catch (Exception ex)
		{
			Log.printDebug(strClassName + ":" + funcName + " - " + ex.getMessage());
		}
	} // end fnGetParams

	protected void fnUpdateItem(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateItem()";
		// Get the request paramaters
		String itemId = (String) req.getParameter("itemId");
		String itemCode = (String) req.getParameter("itemCode");
		String itemType = (String) req.getParameter("itemType");
		// String itemName = (String) req.getParameter("itemName");
		// String itemDesc = (String) req.getParameter("itemDesc");
		// String itemUOM = req.getParameter("itemUOM");
		String currency = req.getParameter("currency");
		String stdPrice = req.getParameter("stdPrice");
		String discPrice = req.getParameter("discPrice");
		String minPrice = req.getParameter("minPrice");
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
			if (itemCode == null)
			{
				// return;
				throw new Exception("Invalid Item Code");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - itemCode = " + itemCode);
			/*
			 * if (itemName == null) { //return; throw new Exception("Invalid
			 * itemName"); } else Log.printVerbose(strClassName + ":" + funcName + " -
			 * itemName = " + itemName); if (itemDesc == null) { //return; throw
			 * new Exception("Invalid itemDesc"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " - itemDesc = " +
			 * itemDesc); if (itemUOM == null) { //return; throw new
			 * Exception("Invalid itemUOM"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " - itemUOM = " +
			 * itemUOM);
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
			Log.printVerbose("Editing Item Inventory Item ... ");
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
			// Get the Item
			Item lInv = ItemNut.getHandle(new Integer(itemId));
			if (lInv == null)
			{
				throw new Exception("Cannot Edit Null Item");
			}
			/*
			 * // Edit the Item Details here lInv.setCode(itemCode);
			 * lInv.setName(itemName); lInv.setDescription(itemDesc);
			 * lInv.setUnitMeasure(itemUOM); lInv.setLastUpdate(tsCreate);
			 * lInv.setUserIdUpdate(usrid);
			 */
			// Try to get the corresponding POSItem
			// Alex: 07/20/03 - Need to cater for non-inventory as well
			String lPOSInvType = POSItemBean.TYPE_INV;
			if (Integer.parseInt(itemType) == ItemBean.INV_TYPE_NONSTK)
				lPOSInvType = POSItemBean.TYPE_NSTK;
			else if (Integer.parseInt(itemType) == ItemBean.INV_TYPE_TRADEIN)
				lPOSInvType = POSItemBean.TYPE_TRADEIN_SELL;
			POSItem lPOSItem = POSItemNut.getPOSInvItem(new Integer(itemId), lPOSInvType);
			// Edit the posItem if exist, else create a new row for it
			if (lPOSItem != null)
			{
				lPOSItem.setCurrency(currency);
				lPOSItem.setUnitPriceStd(new BigDecimal(stdPrice));
				lPOSItem.setUnitPriceDiscounted(new BigDecimal(discPrice));
				lPOSItem.setUnitPriceMin(new BigDecimal(minPrice));
				lPOSItem.setLastUpdate(tsCreate);
				lPOSItem.setUserIdUpdate(usrid);
				// if it reaches here, it has successfully edited the POSItem
				String rtnMsg = "Successfully edited POSItem for Item Code = " + itemCode;
				Log.printDebug(rtnMsg);
				req.setAttribute("rtnMsg", rtnMsg);
			} else
			{
				// Create a new row for it
				String lItemType = POSItemBean.TYPE_INV;
				String lCurrency = "MYR"; // default
				Integer lCatId = new Integer(0); // default
				POSItemObject posObj = new POSItemObject();
				posObj.itemFKId = new Integer(itemId);
				Log.printVerbose(" the foreign keyyyyyyyyyyyyy is " + posObj.itemFKId.toString());
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
					String rtnMsg = "Successfully created POSItem for Item Code = " + itemCode;
					Log.printDebug(rtnMsg);
					req.setAttribute("rtnMsg", rtnMsg);
				}
			} // end if(lPOSItem != null)
		} // end try
		catch (Exception ex)
		{
			ex.printStackTrace();
			String rtnMsg = "Update POSItem for Item Code = " + itemCode + " -  Failed: " + ex.getMessage();
			Log.printDebug(rtnMsg);
			req.setAttribute("rtnMsg", rtnMsg);
		} // end try-catch
	} // end fnUpdateItem
} // end class DoPOSInvItemEdit
/*==========================================================
 *
 * Copyright  of Vincent Lee (vlee@vlee.net,
 *      vincent@wavelet.biz). All Rights Reserved.
 * (http://www.wavelet.biz)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
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

public class DoPOSInvItemEdit implements Action
{
	String strClassName = "DoPOSInvItemEdit";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName != null)
		{
			if (formName.compareTo("updateInventory") == 0)
			{
				// update the POS Inventory Item
				fnUpdateItem(servlet, req, res);
				// fnGetParams(servlet, req, res);
			}
		}
		fnGetParams(servlet, req, res);
		return new ActionRouter("cust-pos-edit-inv-items-page");
	}

	protected void fnGetParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetParams()";
		/*
		 * Need to populate 2 things: [1] (Item) editInv, given the itemCode [2]
		 * (POSItem) posInvItem corresponding to the editInv [ if exist ]
		 */
		try
		{ // super huge try block
			// Obtain the itemId from itemCode
			String lInvCode = req.getParameter("itemCode");
			Item lItem = ItemNut.getObjectByCode(lInvCode);
			if (lItem != null)
			{
				req.setAttribute("editInv", lItem);
				// Now try to obtain the posInvItem corresponding to editInv
				// Alex: 07/20/03 - need to cater for non-inventory
				String lPOSInvType = POSItemBean.TYPE_INV;
				if (lItem.getEnumInvType().intValue() == ItemBean.INV_TYPE_NONSTK)
					lPOSInvType = POSItemBean.TYPE_NSTK;
				else if (lItem.getEnumInvType().intValue() == ItemBean.INV_TYPE_TRADEIN)
					lPOSInvType = POSItemBean.TYPE_TRADEIN_SELL;
				POSItem posInvItem = POSItemNut.getPOSInvItem(lItem.getPkid(), lPOSInvType);
				if (posInvItem != null)
				{
					req.setAttribute("posInvItem", posInvItem);
				}
			} // end if (lItem != null)
		} catch (Exception ex)
		{
			Log.printDebug(strClassName + ":" + funcName + " - " + ex.getMessage());
		}
	} // end fnGetParams

	protected void fnUpdateItem(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateItem()";
		// Get the request paramaters
		String itemId = (String) req.getParameter("itemId");
		String itemCode = (String) req.getParameter("itemCode");
		String itemType = (String) req.getParameter("itemType");
		// String itemName = (String) req.getParameter("itemName");
		// String itemDesc = (String) req.getParameter("itemDesc");
		// String itemUOM = req.getParameter("itemUOM");
		String currency = req.getParameter("currency");
		String stdPrice = req.getParameter("stdPrice");
		String discPrice = req.getParameter("discPrice");
		String minPrice = req.getParameter("minPrice");
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
			if (itemCode == null)
			{
				// return;
				throw new Exception("Invalid Item Code");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - itemCode = " + itemCode);
			/*
			 * if (itemName == null) { //return; throw new Exception("Invalid
			 * itemName"); } else Log.printVerbose(strClassName + ":" + funcName + " -
			 * itemName = " + itemName); if (itemDesc == null) { //return; throw
			 * new Exception("Invalid itemDesc"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " - itemDesc = " +
			 * itemDesc); if (itemUOM == null) { //return; throw new
			 * Exception("Invalid itemUOM"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " - itemUOM = " +
			 * itemUOM);
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
			Log.printVerbose("Editing Item Inventory Item ... ");
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
			// Get the Item
			Item lInv = ItemNut.getHandle(new Integer(itemId));
			if (lInv == null)
			{
				throw new Exception("Cannot Edit Null Item");
			}
			/*
			 * // Edit the Item Details here lInv.setCode(itemCode);
			 * lInv.setName(itemName); lInv.setDescription(itemDesc);
			 * lInv.setUnitMeasure(itemUOM); lInv.setLastUpdate(tsCreate);
			 * lInv.setUserIdUpdate(usrid);
			 */
			// Try to get the corresponding POSItem
			// Alex: 07/20/03 - Need to cater for non-inventory as well
			String lPOSInvType = POSItemBean.TYPE_INV;
			if (Integer.parseInt(itemType) == ItemBean.INV_TYPE_NONSTK)
				lPOSInvType = POSItemBean.TYPE_NSTK;
			else if (Integer.parseInt(itemType) == ItemBean.INV_TYPE_TRADEIN)
				lPOSInvType = POSItemBean.TYPE_TRADEIN_SELL;
			POSItem lPOSItem = POSItemNut.getPOSInvItem(new Integer(itemId), lPOSInvType);
			// Edit the posItem if exist, else create a new row for it
			if (lPOSItem != null)
			{
				lPOSItem.setCurrency(currency);
				lPOSItem.setUnitPriceStd(new BigDecimal(stdPrice));
				lPOSItem.setUnitPriceDiscounted(new BigDecimal(discPrice));
				lPOSItem.setUnitPriceMin(new BigDecimal(minPrice));
				lPOSItem.setLastUpdate(tsCreate);
				lPOSItem.setUserIdUpdate(usrid);
				// if it reaches here, it has successfully edited the POSItem
				String rtnMsg = "Successfully edited POSItem for Item Code = " + itemCode;
				Log.printDebug(rtnMsg);
				req.setAttribute("rtnMsg", rtnMsg);
			} else
			{
				// Create a new row for it
				String lItemType = POSItemBean.TYPE_INV;
				String lCurrency = "MYR"; // default
				Integer lCatId = new Integer(0); // default
				POSItemObject posObj = new POSItemObject();
				posObj.itemFKId = new Integer(itemId);
				Log.printVerbose(" the foreign keyyyyyyyyyyyyy is " + posObj.itemFKId.toString());
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
					String rtnMsg = "Successfully created POSItem for Item Code = " + itemCode;
					Log.printDebug(rtnMsg);
					req.setAttribute("rtnMsg", rtnMsg);
				}
			} // end if(lPOSItem != null)
		} // end try
		catch (Exception ex)
		{
			ex.printStackTrace();
			String rtnMsg = "Update POSItem for Item Code = " + itemCode + " -  Failed: " + ex.getMessage();
			Log.printDebug(rtnMsg);
			req.setAttribute("rtnMsg", rtnMsg);
		} // end try-catch
	} // end fnUpdateItem
} // end class DoPOSInvItemEdit
