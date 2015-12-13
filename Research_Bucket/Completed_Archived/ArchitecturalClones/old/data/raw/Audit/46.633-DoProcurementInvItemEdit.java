/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.info)
 *
 * This software is the proprietary information of Wavelet Technology,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.procurement;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.text.*;
import java.sql.*;
import java.math.BigDecimal;
import com.vlee.servlet.main.*;
import com.vlee.util.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;

public class DoProcurementInvItemEdit implements Action
{
	String strClassName = "DoProcurementInvItemEdit";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName != null)
		{
			if (formName.compareTo("updateInventory") == 0)
			{
				// update the Purchase Inventory Item
				fnUpdateItem(servlet, req, res);
				// fnGetParams(servlet, req, res);
			}
		}
		fnGetParams(servlet, req, res);
		return new ActionRouter("supp-procurement-edit-inv-items-page");
	}

	// //////////////////////////////////////////////////////
	protected void fnGetParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetParams()";
		/*
		 * Need to populate 2 things: [1] (Item) editInv, given the itemCode [2]
		 * (PurchaseItem) purchaseInvItem corresponding to the editInv [ if
		 * exist ]
		 */
		try
		{ // super huge try block
			// Obtain the itemId from itemCode
			String lInvCode = req.getParameter("itemCode");
			Item lItem = ItemNut.getObjectByCode(lInvCode);
			if (lItem != null)
			{
				req.setAttribute("editInv", lItem);
				// Now try to obtain the purchaseInvItem corresponding to
				// editInv
				// Alex: 07/20/03 - need to cater for non-inventory
				String lPurchaseInvType = PurchaseItemBean.TYPE_INV;
				if (lItem.getEnumInvType().intValue() == ItemBean.INV_TYPE_NONSTK)
					lPurchaseInvType = PurchaseItemBean.TYPE_NSTK;
				PurchaseItem purchaseInvItem = PurchaseItemNut.getPurchaseInvItem(lItem.getPkid(), lPurchaseInvType);
				if (purchaseInvItem != null)
				{
					req.setAttribute("purchaseInvItem", purchaseInvItem);
				}
			} // end if (lItem != null)
		} catch (Exception ex)
		{
			Log.printDebug(strClassName + ":" + funcName + " - " + ex.getMessage());
		}
	} // end fnGetParams

	// ///////////////////////////////////////////////////////
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
			// Try to get the corresponding PurchaseItem
			// Alex: 07/20/03 - Need to cater for non-inventory as well
			String lPurchaseInvType = PurchaseItemBean.TYPE_INV;
			if (Integer.parseInt(itemType) == ItemBean.INV_TYPE_NONSTK)
				lPurchaseInvType = PurchaseItemBean.TYPE_NSTK;
			PurchaseItem lPurchaseItem = PurchaseItemNut.getPurchaseInvItem(new Integer(itemId), lPurchaseInvType);
			// Edit the posItem if exist, else create a new row for it
			if (lPurchaseItem != null)
			{
				lPurchaseItem.setCurrency(currency);
				lPurchaseItem.setUnitPriceStd(new BigDecimal(stdPrice));
				lPurchaseItem.setUnitPriceDiscounted(new BigDecimal(discPrice));
				lPurchaseItem.setUnitPriceMin(new BigDecimal(minPrice));
				lPurchaseItem.setLastUpdate(tsCreate);
				lPurchaseItem.setUserIdUpdate(usrid);
				// if it reaches here, it has successfully edited the
				// PurchaseItem
				String rtnMsg = "Successfully edited PurchaseItem for Item Code = " + itemCode;
				Log.printDebug(rtnMsg);
				req.setAttribute("rtnMsg", rtnMsg);
			} else
			{
				// Create a new row for it
				String lItemType = PurchaseItemBean.TYPE_INV;
				String lCurrency = "MYR"; // default
				Integer lCatId = new Integer(0); // default
				PurchaseItemObject purObj = new PurchaseItemObject();
				purObj.itemFKId = new Integer(itemId);
				purObj.itemType = lItemType;
				purObj.currency = lCurrency;
				purObj.unitPriceStd = new BigDecimal(stdPrice);
				purObj.unitPriceDiscounted = new BigDecimal(discPrice);
				purObj.unitPriceMin = new BigDecimal(minPrice);
				purObj.userIdUpdate = usrid;
				PurchaseItem newPurchaseItem = PurchaseItemNut.fnCreate(purObj);
				if (newPurchaseItem != null)
				{
					String rtnMsg = "Successfully created PurchaseItem for Item Code = " + itemCode;
					Log.printDebug(rtnMsg);
					req.setAttribute("rtnMsg", rtnMsg);
				}
			} // end if(lPurchaseItem != null)
		} // end try
		catch (Exception ex)
		{
			String rtnMsg = "Update PurchaseItem for Item Code = " + itemCode + " -  Failed: " + ex.getMessage();
			Log.printDebug(rtnMsg);
			req.setAttribute("rtnMsg", rtnMsg);
		} // end try-catch
	} // end fnUpdateItem
} // end class DoPurchaseInvItemEdit
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.info)
 *
 * This software is the proprietary information of Wavelet Technology,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.procurement;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.text.*;
import java.sql.*;
import java.math.BigDecimal;
import com.vlee.servlet.main.*;
import com.vlee.util.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;

public class DoProcurementInvItemEdit implements Action
{
	String strClassName = "DoProcurementInvItemEdit";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName != null)
		{
			if (formName.compareTo("updateInventory") == 0)
			{
				// update the Purchase Inventory Item
				fnUpdateItem(servlet, req, res);
				// fnGetParams(servlet, req, res);
			}
		}
		fnGetParams(servlet, req, res);
		return new ActionRouter("supp-procurement-edit-inv-items-page");
	}

	// //////////////////////////////////////////////////////
	protected void fnGetParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetParams()";
		/*
		 * Need to populate 2 things: [1] (Item) editInv, given the itemCode [2]
		 * (PurchaseItem) purchaseInvItem corresponding to the editInv [ if
		 * exist ]
		 */
		try
		{ // super huge try block
			// Obtain the itemId from itemCode
			String lInvCode = req.getParameter("itemCode");
			Item lItem = ItemNut.getObjectByCode(lInvCode);
			if (lItem != null)
			{
				req.setAttribute("editInv", lItem);
				// Now try to obtain the purchaseInvItem corresponding to
				// editInv
				// Alex: 07/20/03 - need to cater for non-inventory
				String lPurchaseInvType = PurchaseItemBean.TYPE_INV;
				if (lItem.getEnumInvType().intValue() == ItemBean.INV_TYPE_NONSTK)
					lPurchaseInvType = PurchaseItemBean.TYPE_NSTK;
				PurchaseItem purchaseInvItem = PurchaseItemNut.getPurchaseInvItem(lItem.getPkid(), lPurchaseInvType);
				if (purchaseInvItem != null)
				{
					req.setAttribute("purchaseInvItem", purchaseInvItem);
				}
			} // end if (lItem != null)
		} catch (Exception ex)
		{
			Log.printDebug(strClassName + ":" + funcName + " - " + ex.getMessage());
		}
	} // end fnGetParams

	// ///////////////////////////////////////////////////////
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
			// Try to get the corresponding PurchaseItem
			// Alex: 07/20/03 - Need to cater for non-inventory as well
			String lPurchaseInvType = PurchaseItemBean.TYPE_INV;
			if (Integer.parseInt(itemType) == ItemBean.INV_TYPE_NONSTK)
				lPurchaseInvType = PurchaseItemBean.TYPE_NSTK;
			PurchaseItem lPurchaseItem = PurchaseItemNut.getPurchaseInvItem(new Integer(itemId), lPurchaseInvType);
			// Edit the posItem if exist, else create a new row for it
			if (lPurchaseItem != null)
			{
				lPurchaseItem.setCurrency(currency);
				lPurchaseItem.setUnitPriceStd(new BigDecimal(stdPrice));
				lPurchaseItem.setUnitPriceDiscounted(new BigDecimal(discPrice));
				lPurchaseItem.setUnitPriceMin(new BigDecimal(minPrice));
				lPurchaseItem.setLastUpdate(tsCreate);
				lPurchaseItem.setUserIdUpdate(usrid);
				// if it reaches here, it has successfully edited the
				// PurchaseItem
				String rtnMsg = "Successfully edited PurchaseItem for Item Code = " + itemCode;
				Log.printDebug(rtnMsg);
				req.setAttribute("rtnMsg", rtnMsg);
			} else
			{
				// Create a new row for it
				String lItemType = PurchaseItemBean.TYPE_INV;
				String lCurrency = "MYR"; // default
				Integer lCatId = new Integer(0); // default
				PurchaseItemObject purObj = new PurchaseItemObject();
				purObj.itemFKId = new Integer(itemId);
				purObj.itemType = lItemType;
				purObj.currency = lCurrency;
				purObj.unitPriceStd = new BigDecimal(stdPrice);
				purObj.unitPriceDiscounted = new BigDecimal(discPrice);
				purObj.unitPriceMin = new BigDecimal(minPrice);
				purObj.userIdUpdate = usrid;
				PurchaseItem newPurchaseItem = PurchaseItemNut.fnCreate(purObj);
				if (newPurchaseItem != null)
				{
					String rtnMsg = "Successfully created PurchaseItem for Item Code = " + itemCode;
					Log.printDebug(rtnMsg);
					req.setAttribute("rtnMsg", rtnMsg);
				}
			} // end if(lPurchaseItem != null)
		} // end try
		catch (Exception ex)
		{
			String rtnMsg = "Update PurchaseItem for Item Code = " + itemCode + " -  Failed: " + ex.getMessage();
			Log.printDebug(rtnMsg);
			req.setAttribute("rtnMsg", rtnMsg);
		} // end try-catch
	} // end fnUpdateItem
} // end class DoPurchaseInvItemEdit
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.info)
 *
 * This software is the proprietary information of Wavelet Technology,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.procurement;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.text.*;
import java.sql.*;
import java.math.BigDecimal;
import com.vlee.servlet.main.*;
import com.vlee.util.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;

public class DoProcurementInvItemEdit implements Action
{
	String strClassName = "DoProcurementInvItemEdit";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName != null)
		{
			if (formName.compareTo("updateInventory") == 0)
			{
				// update the Purchase Inventory Item
				fnUpdateItem(servlet, req, res);
				// fnGetParams(servlet, req, res);
			}
		}
		fnGetParams(servlet, req, res);
		return new ActionRouter("supp-procurement-edit-inv-items-page");
	}

	// //////////////////////////////////////////////////////
	protected void fnGetParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetParams()";
		/*
		 * Need to populate 2 things: [1] (Item) editInv, given the itemCode [2]
		 * (PurchaseItem) purchaseInvItem corresponding to the editInv [ if
		 * exist ]
		 */
		try
		{ // super huge try block
			// Obtain the itemId from itemCode
			String lInvCode = req.getParameter("itemCode");
			Item lItem = ItemNut.getObjectByCode(lInvCode);
			if (lItem != null)
			{
				req.setAttribute("editInv", lItem);
				// Now try to obtain the purchaseInvItem corresponding to
				// editInv
				// Alex: 07/20/03 - need to cater for non-inventory
				String lPurchaseInvType = PurchaseItemBean.TYPE_INV;
				if (lItem.getEnumInvType().intValue() == ItemBean.INV_TYPE_NONSTK)
					lPurchaseInvType = PurchaseItemBean.TYPE_NSTK;
				PurchaseItem purchaseInvItem = PurchaseItemNut.getPurchaseInvItem(lItem.getPkid(), lPurchaseInvType);
				if (purchaseInvItem != null)
				{
					req.setAttribute("purchaseInvItem", purchaseInvItem);
				}
			} // end if (lItem != null)
		} catch (Exception ex)
		{
			Log.printDebug(strClassName + ":" + funcName + " - " + ex.getMessage());
		}
	} // end fnGetParams

	// ///////////////////////////////////////////////////////
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
			// Try to get the corresponding PurchaseItem
			// Alex: 07/20/03 - Need to cater for non-inventory as well
			String lPurchaseInvType = PurchaseItemBean.TYPE_INV;
			if (Integer.parseInt(itemType) == ItemBean.INV_TYPE_NONSTK)
				lPurchaseInvType = PurchaseItemBean.TYPE_NSTK;
			PurchaseItem lPurchaseItem = PurchaseItemNut.getPurchaseInvItem(new Integer(itemId), lPurchaseInvType);
			// Edit the posItem if exist, else create a new row for it
			if (lPurchaseItem != null)
			{
				lPurchaseItem.setCurrency(currency);
				lPurchaseItem.setUnitPriceStd(new BigDecimal(stdPrice));
				lPurchaseItem.setUnitPriceDiscounted(new BigDecimal(discPrice));
				lPurchaseItem.setUnitPriceMin(new BigDecimal(minPrice));
				lPurchaseItem.setLastUpdate(tsCreate);
				lPurchaseItem.setUserIdUpdate(usrid);
				// if it reaches here, it has successfully edited the
				// PurchaseItem
				String rtnMsg = "Successfully edited PurchaseItem for Item Code = " + itemCode;
				Log.printDebug(rtnMsg);
				req.setAttribute("rtnMsg", rtnMsg);
			} else
			{
				// Create a new row for it
				String lItemType = PurchaseItemBean.TYPE_INV;
				String lCurrency = "MYR"; // default
				Integer lCatId = new Integer(0); // default
				PurchaseItemObject purObj = new PurchaseItemObject();
				purObj.itemFKId = new Integer(itemId);
				purObj.itemType = lItemType;
				purObj.currency = lCurrency;
				purObj.unitPriceStd = new BigDecimal(stdPrice);
				purObj.unitPriceDiscounted = new BigDecimal(discPrice);
				purObj.unitPriceMin = new BigDecimal(minPrice);
				purObj.userIdUpdate = usrid;
				PurchaseItem newPurchaseItem = PurchaseItemNut.fnCreate(purObj);
				if (newPurchaseItem != null)
				{
					String rtnMsg = "Successfully created PurchaseItem for Item Code = " + itemCode;
					Log.printDebug(rtnMsg);
					req.setAttribute("rtnMsg", rtnMsg);
				}
			} // end if(lPurchaseItem != null)
		} // end try
		catch (Exception ex)
		{
			String rtnMsg = "Update PurchaseItem for Item Code = " + itemCode + " -  Failed: " + ex.getMessage();
			Log.printDebug(rtnMsg);
			req.setAttribute("rtnMsg", rtnMsg);
		} // end try-catch
	} // end fnUpdateItem
} // end class DoPurchaseInvItemEdit
