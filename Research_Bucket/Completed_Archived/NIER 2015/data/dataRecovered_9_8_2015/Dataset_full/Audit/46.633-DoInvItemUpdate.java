package com.vlee.servlet.inventory;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.text.*;
import java.sql.Timestamp;
import com.vlee.servlet.main.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;

public class DoInvItemUpdate implements Action
{
	String strClassName = "DoInvItemUpdate";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// update item
		fnUpdateItem(servlet, req, res);
		// repopulate itemList
		// fnGetItemCodeList(servlet, req, res);
		// return new ActionRouter("inv-setup-edit-item-page");
		return new ActionRouter("inv-redirect-setup-edit-item-page");
	}

	protected void fnUpdateItem(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateItem()";
		// Get the request paramaters
		String itemCode = req.getParameter("itemCode");
		String itemName = req.getParameter("itemName");
		String itemDesc = req.getParameter("itemDesc");
		String itemUOM = req.getParameter("itemUOM");
		String itemInvType = req.getParameter("itemInvType");
		String catName = req.getParameter("itemCategoryName");
		if (itemCode == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - itemCode = " + itemCode);
		if (itemName == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - itemName = " + itemName);
		if (itemDesc == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - itemDesc = " + itemDesc);
		if (itemUOM == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - itemUOM = " + itemUOM);
		if (itemInvType == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - itemInvType = " + itemInvType);
		if (catName == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - itemCategoryName = " + catName);
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
		// Get the primary key of category given "catName"
		Integer catPkid;
		try
		{
			catPkid = CategoryNut.getObjectByName(catName).getPkid();
		} catch (Exception ex)
		{
			Log.printDebug(strClassName + ":" + funcName + " - Error while retrieving Category for category name = "
					+ catName);
			return;
		}
		if (itemCode != null)
		{
			Item lItem = ItemNut.getObjectByCode(itemCode);
			if (lItem != null)
			{
				try
				{
					lItem.setItemCode(itemCode);
					lItem.setName(itemName);
					lItem.setDescription(itemDesc);
					lItem.setUnitOfMeasureStr(itemUOM);
					lItem.setInvTypeStr(itemInvType);
					lItem.setCategoryId(catPkid);
					// and then update the lastModified and userIdUpdate fields
					lItem.setLastUpdate(tsCreate);
					lItem.setUserIdUpdate(usrid);
					// populate the "editItem" attribute so re-display the
					// edited fields
					req.setAttribute("editItem", lItem);
				} catch (Exception ex)
				{
					Log.printDebug("Update Item: " + itemCode + " -  Failed" + ex.getMessage());
				} // end try-catch
			} // end if
		} // end if
	} // end fnUpdateItem
	/*
	 * protected void fnGetItemCodeList(HttpServlet servlet, HttpServletRequest
	 * req, HttpServletResponse res) { Vector itemCodes = new Vector();
	 * Collection colItems = ItemNut.getAllObjects(); try {
	 * 
	 * for(Iterator itr = colItems.iterator(); itr.hasNext(); ) { Item i =
	 * (Item) itr.next(); Log.printVerbose("Adding: " + i.getItemCode());
	 * itemCodes.add(i.getItemCode()); } req.setAttribute("itrItemCode",
	 * itemCodes.iterator()); } catch (Exception ex) { Log.printDebug("Error in
	 * getting list of itemcodes: " + ex.getMessage()); } }
	 */
} // end class DoInvItemUpdate
package com.vlee.servlet.inventory;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.text.*;
import java.sql.Timestamp;
import com.vlee.servlet.main.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;

public class DoInvItemUpdate implements Action
{
	String strClassName = "DoInvItemUpdate";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// update item
		fnUpdateItem(servlet, req, res);
		// repopulate itemList
		// fnGetItemCodeList(servlet, req, res);
		// return new ActionRouter("inv-setup-edit-item-page");
		return new ActionRouter("inv-redirect-setup-edit-item-page");
	}

	protected void fnUpdateItem(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateItem()";
		// Get the request paramaters
		String itemCode = req.getParameter("itemCode");
		String itemName = req.getParameter("itemName");
		String itemDesc = req.getParameter("itemDesc");
		String itemUOM = req.getParameter("itemUOM");
		String itemInvType = req.getParameter("itemInvType");
		String catName = req.getParameter("itemCategoryName");
		if (itemCode == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - itemCode = " + itemCode);
		if (itemName == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - itemName = " + itemName);
		if (itemDesc == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - itemDesc = " + itemDesc);
		if (itemUOM == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - itemUOM = " + itemUOM);
		if (itemInvType == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - itemInvType = " + itemInvType);
		if (catName == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - itemCategoryName = " + catName);
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
		// Get the primary key of category given "catName"
		Integer catPkid;
		try
		{
			catPkid = CategoryNut.getObjectByName(catName).getPkid();
		} catch (Exception ex)
		{
			Log.printDebug(strClassName + ":" + funcName + " - Error while retrieving Category for category name = "
					+ catName);
			return;
		}
		if (itemCode != null)
		{
			Item lItem = ItemNut.getObjectByCode(itemCode);
			if (lItem != null)
			{
				try
				{
					lItem.setItemCode(itemCode);
					lItem.setName(itemName);
					lItem.setDescription(itemDesc);
					lItem.setUnitOfMeasureStr(itemUOM);
					lItem.setInvTypeStr(itemInvType);
					lItem.setCategoryId(catPkid);
					// and then update the lastModified and userIdUpdate fields
					lItem.setLastUpdate(tsCreate);
					lItem.setUserIdUpdate(usrid);
					// populate the "editItem" attribute so re-display the
					// edited fields
					req.setAttribute("editItem", lItem);
				} catch (Exception ex)
				{
					Log.printDebug("Update Item: " + itemCode + " -  Failed" + ex.getMessage());
				} // end try-catch
			} // end if
		} // end if
	} // end fnUpdateItem
	/*
	 * protected void fnGetItemCodeList(HttpServlet servlet, HttpServletRequest
	 * req, HttpServletResponse res) { Vector itemCodes = new Vector();
	 * Collection colItems = ItemNut.getAllObjects(); try {
	 * 
	 * for(Iterator itr = colItems.iterator(); itr.hasNext(); ) { Item i =
	 * (Item) itr.next(); Log.printVerbose("Adding: " + i.getItemCode());
	 * itemCodes.add(i.getItemCode()); } req.setAttribute("itrItemCode",
	 * itemCodes.iterator()); } catch (Exception ex) { Log.printDebug("Error in
	 * getting list of itemcodes: " + ex.getMessage()); } }
	 */
} // end class DoInvItemUpdate
package com.vlee.servlet.inventory;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.text.*;
import java.sql.Timestamp;
import com.vlee.servlet.main.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;

public class DoInvItemUpdate implements Action
{
	String strClassName = "DoInvItemUpdate";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// update item
		fnUpdateItem(servlet, req, res);
		// repopulate itemList
		// fnGetItemCodeList(servlet, req, res);
		// return new ActionRouter("inv-setup-edit-item-page");
		return new ActionRouter("inv-redirect-setup-edit-item-page");
	}

	protected void fnUpdateItem(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateItem()";
		// Get the request paramaters
		String itemCode = req.getParameter("itemCode");
		String itemName = req.getParameter("itemName");
		String itemDesc = req.getParameter("itemDesc");
		String itemUOM = req.getParameter("itemUOM");
		String itemInvType = req.getParameter("itemInvType");
		String catName = req.getParameter("itemCategoryName");
		if (itemCode == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - itemCode = " + itemCode);
		if (itemName == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - itemName = " + itemName);
		if (itemDesc == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - itemDesc = " + itemDesc);
		if (itemUOM == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - itemUOM = " + itemUOM);
		if (itemInvType == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - itemInvType = " + itemInvType);
		if (catName == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - itemCategoryName = " + catName);
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
		// Get the primary key of category given "catName"
		Integer catPkid;
		try
		{
			catPkid = CategoryNut.getObjectByName(catName).getPkid();
		} catch (Exception ex)
		{
			Log.printDebug(strClassName + ":" + funcName + " - Error while retrieving Category for category name = "
					+ catName);
			return;
		}
		if (itemCode != null)
		{
			Item lItem = ItemNut.getObjectByCode(itemCode);
			if (lItem != null)
			{
				try
				{
					lItem.setItemCode(itemCode);
					lItem.setName(itemName);
					lItem.setDescription(itemDesc);
					lItem.setUnitOfMeasureStr(itemUOM);
					lItem.setInvTypeStr(itemInvType);
					lItem.setCategoryId(catPkid);
					// and then update the lastModified and userIdUpdate fields
					lItem.setLastUpdate(tsCreate);
					lItem.setUserIdUpdate(usrid);
					// populate the "editItem" attribute so re-display the
					// edited fields
					req.setAttribute("editItem", lItem);
				} catch (Exception ex)
				{
					Log.printDebug("Update Item: " + itemCode + " -  Failed" + ex.getMessage());
				} // end try-catch
			} // end if
		} // end if
	} // end fnUpdateItem
	/*
	 * protected void fnGetItemCodeList(HttpServlet servlet, HttpServletRequest
	 * req, HttpServletResponse res) { Vector itemCodes = new Vector();
	 * Collection colItems = ItemNut.getAllObjects(); try {
	 * 
	 * for(Iterator itr = colItems.iterator(); itr.hasNext(); ) { Item i =
	 * (Item) itr.next(); Log.printVerbose("Adding: " + i.getItemCode());
	 * itemCodes.add(i.getItemCode()); } req.setAttribute("itrItemCode",
	 * itemCodes.iterator()); } catch (Exception ex) { Log.printDebug("Error in
	 * getting list of itemcodes: " + ex.getMessage()); } }
	 */
} // end class DoInvItemUpdate
package com.vlee.servlet.inventory;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.text.*;
import java.sql.Timestamp;
import com.vlee.servlet.main.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;

public class DoInvItemUpdate implements Action
{
	String strClassName = "DoInvItemUpdate";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// update item
		fnUpdateItem(servlet, req, res);
		// repopulate itemList
		// fnGetItemCodeList(servlet, req, res);
		// return new ActionRouter("inv-setup-edit-item-page");
		return new ActionRouter("inv-redirect-setup-edit-item-page");
	}

	protected void fnUpdateItem(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateItem()";
		// Get the request paramaters
		String itemCode = req.getParameter("itemCode");
		String itemName = req.getParameter("itemName");
		String itemDesc = req.getParameter("itemDesc");
		String itemUOM = req.getParameter("itemUOM");
		String itemInvType = req.getParameter("itemInvType");
		String catName = req.getParameter("itemCategoryName");
		if (itemCode == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - itemCode = " + itemCode);
		if (itemName == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - itemName = " + itemName);
		if (itemDesc == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - itemDesc = " + itemDesc);
		if (itemUOM == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - itemUOM = " + itemUOM);
		if (itemInvType == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - itemInvType = " + itemInvType);
		if (catName == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - itemCategoryName = " + catName);
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
		// Get the primary key of category given "catName"
		Integer catPkid;
		try
		{
			catPkid = CategoryNut.getObjectByName(catName).getPkid();
		} catch (Exception ex)
		{
			Log.printDebug(strClassName + ":" + funcName + " - Error while retrieving Category for category name = "
					+ catName);
			return;
		}
		if (itemCode != null)
		{
			Item lItem = ItemNut.getObjectByCode(itemCode);
			if (lItem != null)
			{
				try
				{
					lItem.setItemCode(itemCode);
					lItem.setName(itemName);
					lItem.setDescription(itemDesc);
					lItem.setUnitOfMeasureStr(itemUOM);
					lItem.setInvTypeStr(itemInvType);
					lItem.setCategoryId(catPkid);
					// and then update the lastModified and userIdUpdate fields
					lItem.setLastUpdate(tsCreate);
					lItem.setUserIdUpdate(usrid);
					// populate the "editItem" attribute so re-display the
					// edited fields
					req.setAttribute("editItem", lItem);
				} catch (Exception ex)
				{
					Log.printDebug("Update Item: " + itemCode + " -  Failed" + ex.getMessage());
				} // end try-catch
			} // end if
		} // end if
	} // end fnUpdateItem
	/*
	 * protected void fnGetItemCodeList(HttpServlet servlet, HttpServletRequest
	 * req, HttpServletResponse res) { Vector itemCodes = new Vector();
	 * Collection colItems = ItemNut.getAllObjects(); try {
	 * 
	 * for(Iterator itr = colItems.iterator(); itr.hasNext(); ) { Item i =
	 * (Item) itr.next(); Log.printVerbose("Adding: " + i.getItemCode());
	 * itemCodes.add(i.getItemCode()); } req.setAttribute("itrItemCode",
	 * itemCodes.iterator()); } catch (Exception ex) { Log.printDebug("Error in
	 * getting list of itemcodes: " + ex.getMessage()); } }
	 */
} // end class DoInvItemUpdate
