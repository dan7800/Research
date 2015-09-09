/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.info)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.inventory;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import java.math.BigDecimal;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;

public class DoInvItemPriceEdit implements Action
{
	private String strClassName = "DoInvItemPriceEdit";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Check if itemCode is supplied
		String itemCode = (String) req.getParameter("itemCode");
		String formName = (String) req.getParameter("formName");
		if (formName != null && (formName.compareTo("updateItemPrice") == 0))
		{
			fnUpdateItemPrice(servlet, req, res);
		}
		if (itemCode != null)
			// find the item to be edited
			fnGetEditParams(servlet, req, res, itemCode);
		// list all itemCodes
		// fnGetItemCodeList(servlet, req, res);
		return new ActionRouter("inv-setup-edit-item-pricing-page");
	}

	protected void fnGetEditParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res, String itemCode)
	{
		Item lItem = ItemNut.getObjectByCode(itemCode);
		if (lItem != null)
		{
			// Need to set the following attributes
			// 1. (String) strItemCode
			// 2. (String) strCondition
			// 3. (String) strUnitCost
			// 4. (String) strCurr
			// 5. (String) strEffDate
			// 6. (String) strLastUpdate
			// get the item condition
			String lItemCond = (String) req.getParameter("itemCond");
			// map cond str to enum
			// Integer lCondId = (Integer) mapConditionStrToEnum.get(itemCond);
			try
			{
				req.setAttribute("strItemCode", itemCode);
				req.setAttribute("strCondition", lItemCond);
				// find the corresponding item price row, given itemCode and
				// itemCond
				ItemPrice lItemP = ItemPriceNut.getObjectByItemAndCond(lItem.getPkid(), lItemCond);
				if (lItemP != null)
				{
					req.setAttribute("strUnitCost", lItemP.getUnitCost().toString());
					req.setAttribute("strCurrency", lItemP.getCurrency());
					// convert timestamp to yyyy-mm-dd
					req.setAttribute("strEffDate", TimeFormat.strDisplayDate(lItemP.getEffectiveDate()));
					req.setAttribute("strLastUpdate", TimeFormat.strDisplayDate(lItemP.getLastUpdate()));
				} else
				{
					req.setAttribute("strUnitCost", "");
					req.setAttribute("strCurrency", "");
					// convert timestamp to yyyy-mm-dd
					req.setAttribute("strEffDate", "");
					req.setAttribute("strLastUpdate", "<not set>");
				}
			} catch (Exception ex)
			{
				Log.printDebug(strClassName + " : Failed to load edit item price params - " + ex.getMessage());
			}
		}
	}

	protected void fnUpdateItemPrice(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateItemPrice()";
		// Get the request paramaters
		String itemCode = req.getParameter("itemCode");
		String itemCond = req.getParameter("itemCond");
		String unitCost = req.getParameter("unitCost");
		String currency = req.getParameter("currency");
		String effDate = req.getParameter("effDate");
		if (itemCode == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - itemCode = " + itemCode);
		if (itemCond == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - itemCond = " + itemCond);
		if (unitCost == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - unitCost = " + unitCost);
		if (currency == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - currency = " + currency);
		if (effDate == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - effDate = " + effDate);
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
		if (itemCode != null)
		{ // if 01
			try
			{
				ItemPrice lItemPrice = ItemPriceNut.getObjectByItemAndCond(itemCode, itemCond);
				if (lItemPrice != null)
				{ // if 02
					lItemPrice.setUnitCost(new BigDecimal(unitCost));
					lItemPrice.setCurrency(currency);
					// ensure effDate is in the format: yyyy-mm-dd
					// hh:mm:ss.fffffffff
					String suffix = " 00:00:00.000000000";
					lItemPrice.setEffectiveDate(Timestamp.valueOf(effDate.trim() + suffix));
					// and then update the lastModified and userIdUpdate fields
					lItemPrice.setLastUpdate(tsCreate);
					lItemPrice.setUserIdUpdate(usrid);
				} else
				{
					// Add the itemPrice
					ItemPriceHome itemPriceHome = ItemPriceNut.getHome();
					Item item = ItemNut.getObjectByCode(itemCode);
					if (itemPriceHome != null)
					{ // if 03
						ItemPrice newItemPrice = itemPriceHome.create(item.getPkid(), StockNut
								.mapCondStrToEnum(itemCond), new BigDecimal(unitCost), currency, TimeFormat
								.createTimeStamp(effDate), tsCreate, usrid);
					} // end if 03
				} // end if 02
			} catch (Exception ex)
			{
				Log.printDebug("Update ItemPrice: " + itemCode + " failed with exception: " + ex.getMessage());
			} // end try-catch
		} // end if 01
	} // end fnUpdateItemPrice

	protected void fnGetItemCodeList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Vector itemCodes = new Vector();
		Collection colItems = ItemNut.getAllObjects();
		try
		{
			for (Iterator itr = colItems.iterator(); itr.hasNext();)
			{
				Item i = (Item) itr.next();
				Log.printVerbose("Adding: " + i.getItemCode());
				itemCodes.add(i.getItemCode());
			}
			req.setAttribute("itrItemCode", itemCodes.iterator());
		} catch (Exception ex)
		{
			Log.printDebug("Error in getting list of itemcodes: " + ex.getMessage());
		}
	}
}
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.info)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.inventory;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import java.math.BigDecimal;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;

public class DoInvItemPriceEdit implements Action
{
	private String strClassName = "DoInvItemPriceEdit";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Check if itemCode is supplied
		String itemCode = (String) req.getParameter("itemCode");
		String formName = (String) req.getParameter("formName");
		if (formName != null && (formName.compareTo("updateItemPrice") == 0))
		{
			fnUpdateItemPrice(servlet, req, res);
		}
		if (itemCode != null)
			// find the item to be edited
			fnGetEditParams(servlet, req, res, itemCode);
		// list all itemCodes
		// fnGetItemCodeList(servlet, req, res);
		return new ActionRouter("inv-setup-edit-item-pricing-page");
	}

	protected void fnGetEditParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res, String itemCode)
	{
		Item lItem = ItemNut.getObjectByCode(itemCode);
		if (lItem != null)
		{
			// Need to set the following attributes
			// 1. (String) strItemCode
			// 2. (String) strCondition
			// 3. (String) strUnitCost
			// 4. (String) strCurr
			// 5. (String) strEffDate
			// 6. (String) strLastUpdate
			// get the item condition
			String lItemCond = (String) req.getParameter("itemCond");
			// map cond str to enum
			// Integer lCondId = (Integer) mapConditionStrToEnum.get(itemCond);
			try
			{
				req.setAttribute("strItemCode", itemCode);
				req.setAttribute("strCondition", lItemCond);
				// find the corresponding item price row, given itemCode and
				// itemCond
				ItemPrice lItemP = ItemPriceNut.getObjectByItemAndCond(lItem.getPkid(), lItemCond);
				if (lItemP != null)
				{
					req.setAttribute("strUnitCost", lItemP.getUnitCost().toString());
					req.setAttribute("strCurrency", lItemP.getCurrency());
					// convert timestamp to yyyy-mm-dd
					req.setAttribute("strEffDate", TimeFormat.strDisplayDate(lItemP.getEffectiveDate()));
					req.setAttribute("strLastUpdate", TimeFormat.strDisplayDate(lItemP.getLastUpdate()));
				} else
				{
					req.setAttribute("strUnitCost", "");
					req.setAttribute("strCurrency", "");
					// convert timestamp to yyyy-mm-dd
					req.setAttribute("strEffDate", "");
					req.setAttribute("strLastUpdate", "<not set>");
				}
			} catch (Exception ex)
			{
				Log.printDebug(strClassName + " : Failed to load edit item price params - " + ex.getMessage());
			}
		}
	}

	protected void fnUpdateItemPrice(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateItemPrice()";
		// Get the request paramaters
		String itemCode = req.getParameter("itemCode");
		String itemCond = req.getParameter("itemCond");
		String unitCost = req.getParameter("unitCost");
		String currency = req.getParameter("currency");
		String effDate = req.getParameter("effDate");
		if (itemCode == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - itemCode = " + itemCode);
		if (itemCond == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - itemCond = " + itemCond);
		if (unitCost == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - unitCost = " + unitCost);
		if (currency == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - currency = " + currency);
		if (effDate == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - effDate = " + effDate);
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
		if (itemCode != null)
		{ // if 01
			try
			{
				ItemPrice lItemPrice = ItemPriceNut.getObjectByItemAndCond(itemCode, itemCond);
				if (lItemPrice != null)
				{ // if 02
					lItemPrice.setUnitCost(new BigDecimal(unitCost));
					lItemPrice.setCurrency(currency);
					// ensure effDate is in the format: yyyy-mm-dd
					// hh:mm:ss.fffffffff
					String suffix = " 00:00:00.000000000";
					lItemPrice.setEffectiveDate(Timestamp.valueOf(effDate.trim() + suffix));
					// and then update the lastModified and userIdUpdate fields
					lItemPrice.setLastUpdate(tsCreate);
					lItemPrice.setUserIdUpdate(usrid);
				} else
				{
					// Add the itemPrice
					ItemPriceHome itemPriceHome = ItemPriceNut.getHome();
					Item item = ItemNut.getObjectByCode(itemCode);
					if (itemPriceHome != null)
					{ // if 03
						ItemPrice newItemPrice = itemPriceHome.create(item.getPkid(), StockNut
								.mapCondStrToEnum(itemCond), new BigDecimal(unitCost), currency, TimeFormat
								.createTimeStamp(effDate), tsCreate, usrid);
					} // end if 03
				} // end if 02
			} catch (Exception ex)
			{
				Log.printDebug("Update ItemPrice: " + itemCode + " failed with exception: " + ex.getMessage());
			} // end try-catch
		} // end if 01
	} // end fnUpdateItemPrice

	protected void fnGetItemCodeList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Vector itemCodes = new Vector();
		Collection colItems = ItemNut.getAllObjects();
		try
		{
			for (Iterator itr = colItems.iterator(); itr.hasNext();)
			{
				Item i = (Item) itr.next();
				Log.printVerbose("Adding: " + i.getItemCode());
				itemCodes.add(i.getItemCode());
			}
			req.setAttribute("itrItemCode", itemCodes.iterator());
		} catch (Exception ex)
		{
			Log.printDebug("Error in getting list of itemcodes: " + ex.getMessage());
		}
	}
}
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.info)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.inventory;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import java.math.BigDecimal;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;

public class DoInvItemPriceEdit implements Action
{
	private String strClassName = "DoInvItemPriceEdit";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Check if itemCode is supplied
		String itemCode = (String) req.getParameter("itemCode");
		String formName = (String) req.getParameter("formName");
		if (formName != null && (formName.compareTo("updateItemPrice") == 0))
		{
			fnUpdateItemPrice(servlet, req, res);
		}
		if (itemCode != null)
			// find the item to be edited
			fnGetEditParams(servlet, req, res, itemCode);
		// list all itemCodes
		// fnGetItemCodeList(servlet, req, res);
		return new ActionRouter("inv-setup-edit-item-pricing-page");
	}

	protected void fnGetEditParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res, String itemCode)
	{
		Item lItem = ItemNut.getObjectByCode(itemCode);
		if (lItem != null)
		{
			// Need to set the following attributes
			// 1. (String) strItemCode
			// 2. (String) strCondition
			// 3. (String) strUnitCost
			// 4. (String) strCurr
			// 5. (String) strEffDate
			// 6. (String) strLastUpdate
			// get the item condition
			String lItemCond = (String) req.getParameter("itemCond");
			// map cond str to enum
			// Integer lCondId = (Integer) mapConditionStrToEnum.get(itemCond);
			try
			{
				req.setAttribute("strItemCode", itemCode);
				req.setAttribute("strCondition", lItemCond);
				// find the corresponding item price row, given itemCode and
				// itemCond
				ItemPrice lItemP = ItemPriceNut.getObjectByItemAndCond(lItem.getPkid(), lItemCond);
				if (lItemP != null)
				{
					req.setAttribute("strUnitCost", lItemP.getUnitCost().toString());
					req.setAttribute("strCurrency", lItemP.getCurrency());
					// convert timestamp to yyyy-mm-dd
					req.setAttribute("strEffDate", TimeFormat.strDisplayDate(lItemP.getEffectiveDate()));
					req.setAttribute("strLastUpdate", TimeFormat.strDisplayDate(lItemP.getLastUpdate()));
				} else
				{
					req.setAttribute("strUnitCost", "");
					req.setAttribute("strCurrency", "");
					// convert timestamp to yyyy-mm-dd
					req.setAttribute("strEffDate", "");
					req.setAttribute("strLastUpdate", "<not set>");
				}
			} catch (Exception ex)
			{
				Log.printDebug(strClassName + " : Failed to load edit item price params - " + ex.getMessage());
			}
		}
	}

	protected void fnUpdateItemPrice(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateItemPrice()";
		// Get the request paramaters
		String itemCode = req.getParameter("itemCode");
		String itemCond = req.getParameter("itemCond");
		String unitCost = req.getParameter("unitCost");
		String currency = req.getParameter("currency");
		String effDate = req.getParameter("effDate");
		if (itemCode == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - itemCode = " + itemCode);
		if (itemCond == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - itemCond = " + itemCond);
		if (unitCost == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - unitCost = " + unitCost);
		if (currency == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - currency = " + currency);
		if (effDate == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - effDate = " + effDate);
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
		if (itemCode != null)
		{ // if 01
			try
			{
				ItemPrice lItemPrice = ItemPriceNut.getObjectByItemAndCond(itemCode, itemCond);
				if (lItemPrice != null)
				{ // if 02
					lItemPrice.setUnitCost(new BigDecimal(unitCost));
					lItemPrice.setCurrency(currency);
					// ensure effDate is in the format: yyyy-mm-dd
					// hh:mm:ss.fffffffff
					String suffix = " 00:00:00.000000000";
					lItemPrice.setEffectiveDate(Timestamp.valueOf(effDate.trim() + suffix));
					// and then update the lastModified and userIdUpdate fields
					lItemPrice.setLastUpdate(tsCreate);
					lItemPrice.setUserIdUpdate(usrid);
				} else
				{
					// Add the itemPrice
					ItemPriceHome itemPriceHome = ItemPriceNut.getHome();
					Item item = ItemNut.getObjectByCode(itemCode);
					if (itemPriceHome != null)
					{ // if 03
						ItemPrice newItemPrice = itemPriceHome.create(item.getPkid(), StockNut
								.mapCondStrToEnum(itemCond), new BigDecimal(unitCost), currency, TimeFormat
								.createTimeStamp(effDate), tsCreate, usrid);
					} // end if 03
				} // end if 02
			} catch (Exception ex)
			{
				Log.printDebug("Update ItemPrice: " + itemCode + " failed with exception: " + ex.getMessage());
			} // end try-catch
		} // end if 01
	} // end fnUpdateItemPrice

	protected void fnGetItemCodeList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Vector itemCodes = new Vector();
		Collection colItems = ItemNut.getAllObjects();
		try
		{
			for (Iterator itr = colItems.iterator(); itr.hasNext();)
			{
				Item i = (Item) itr.next();
				Log.printVerbose("Adding: " + i.getItemCode());
				itemCodes.add(i.getItemCode());
			}
			req.setAttribute("itrItemCode", itemCodes.iterator());
		} catch (Exception ex)
		{
			Log.printDebug("Error in getting list of itemcodes: " + ex.getMessage());
		}
	}
}
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.info)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.inventory;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import java.math.BigDecimal;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;

public class DoInvItemPriceEdit implements Action
{
	private String strClassName = "DoInvItemPriceEdit";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Check if itemCode is supplied
		String itemCode = (String) req.getParameter("itemCode");
		String formName = (String) req.getParameter("formName");
		if (formName != null && (formName.compareTo("updateItemPrice") == 0))
		{
			fnUpdateItemPrice(servlet, req, res);
		}
		if (itemCode != null)
			// find the item to be edited
			fnGetEditParams(servlet, req, res, itemCode);
		// list all itemCodes
		// fnGetItemCodeList(servlet, req, res);
		return new ActionRouter("inv-setup-edit-item-pricing-page");
	}

	protected void fnGetEditParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res, String itemCode)
	{
		Item lItem = ItemNut.getObjectByCode(itemCode);
		if (lItem != null)
		{
			// Need to set the following attributes
			// 1. (String) strItemCode
			// 2. (String) strCondition
			// 3. (String) strUnitCost
			// 4. (String) strCurr
			// 5. (String) strEffDate
			// 6. (String) strLastUpdate
			// get the item condition
			String lItemCond = (String) req.getParameter("itemCond");
			// map cond str to enum
			// Integer lCondId = (Integer) mapConditionStrToEnum.get(itemCond);
			try
			{
				req.setAttribute("strItemCode", itemCode);
				req.setAttribute("strCondition", lItemCond);
				// find the corresponding item price row, given itemCode and
				// itemCond
				ItemPrice lItemP = ItemPriceNut.getObjectByItemAndCond(lItem.getPkid(), lItemCond);
				if (lItemP != null)
				{
					req.setAttribute("strUnitCost", lItemP.getUnitCost().toString());
					req.setAttribute("strCurrency", lItemP.getCurrency());
					// convert timestamp to yyyy-mm-dd
					req.setAttribute("strEffDate", TimeFormat.strDisplayDate(lItemP.getEffectiveDate()));
					req.setAttribute("strLastUpdate", TimeFormat.strDisplayDate(lItemP.getLastUpdate()));
				} else
				{
					req.setAttribute("strUnitCost", "");
					req.setAttribute("strCurrency", "");
					// convert timestamp to yyyy-mm-dd
					req.setAttribute("strEffDate", "");
					req.setAttribute("strLastUpdate", "<not set>");
				}
			} catch (Exception ex)
			{
				Log.printDebug(strClassName + " : Failed to load edit item price params - " + ex.getMessage());
			}
		}
	}

	protected void fnUpdateItemPrice(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateItemPrice()";
		// Get the request paramaters
		String itemCode = req.getParameter("itemCode");
		String itemCond = req.getParameter("itemCond");
		String unitCost = req.getParameter("unitCost");
		String currency = req.getParameter("currency");
		String effDate = req.getParameter("effDate");
		if (itemCode == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - itemCode = " + itemCode);
		if (itemCond == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - itemCond = " + itemCond);
		if (unitCost == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - unitCost = " + unitCost);
		if (currency == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - currency = " + currency);
		if (effDate == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - effDate = " + effDate);
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
		if (itemCode != null)
		{ // if 01
			try
			{
				ItemPrice lItemPrice = ItemPriceNut.getObjectByItemAndCond(itemCode, itemCond);
				if (lItemPrice != null)
				{ // if 02
					lItemPrice.setUnitCost(new BigDecimal(unitCost));
					lItemPrice.setCurrency(currency);
					// ensure effDate is in the format: yyyy-mm-dd
					// hh:mm:ss.fffffffff
					String suffix = " 00:00:00.000000000";
					lItemPrice.setEffectiveDate(Timestamp.valueOf(effDate.trim() + suffix));
					// and then update the lastModified and userIdUpdate fields
					lItemPrice.setLastUpdate(tsCreate);
					lItemPrice.setUserIdUpdate(usrid);
				} else
				{
					// Add the itemPrice
					ItemPriceHome itemPriceHome = ItemPriceNut.getHome();
					Item item = ItemNut.getObjectByCode(itemCode);
					if (itemPriceHome != null)
					{ // if 03
						ItemPrice newItemPrice = itemPriceHome.create(item.getPkid(), StockNut
								.mapCondStrToEnum(itemCond), new BigDecimal(unitCost), currency, TimeFormat
								.createTimeStamp(effDate), tsCreate, usrid);
					} // end if 03
				} // end if 02
			} catch (Exception ex)
			{
				Log.printDebug("Update ItemPrice: " + itemCode + " failed with exception: " + ex.getMessage());
			} // end try-catch
		} // end if 01
	} // end fnUpdateItemPrice

	protected void fnGetItemCodeList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Vector itemCodes = new Vector();
		Collection colItems = ItemNut.getAllObjects();
		try
		{
			for (Iterator itr = colItems.iterator(); itr.hasNext();)
			{
				Item i = (Item) itr.next();
				Log.printVerbose("Adding: " + i.getItemCode());
				itemCodes.add(i.getItemCode());
			}
			req.setAttribute("itrItemCode", itemCodes.iterator());
		} catch (Exception ex)
		{
			Log.printDebug("Error in getting list of itemcodes: " + ex.getMessage());
		}
	}
}
