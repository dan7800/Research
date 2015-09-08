/*==========================================================
 *
 * Copyright of Vincent Lee (vlee@vlee.net,
 *      vincent@wavelet.biz). All Rights Reserved.
 * (http://www.wavelet.biz)
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
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;

public class DoInvItemAddRem implements Action
{
	private String strClassName = "DoInvItemAddRem";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("inv-setup-addrem-item-page");
		}
		if (formName.equals("addItem"))
		{
			Log.printVerbose(strClassName + ": formName = addItem");
			try
			{
				fnAddItem(servlet, req, res);
				return new ActionRouter("inv-setup-edit-item-page");
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
				return new ActionRouter("inv-setup-addrem-item-page");
			}
		}
		return new ActionRouter("inv-setup-addrem-item-page");
	}

	protected void fnAddItem(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		ItemObject newObj = new ItemObject();
		String funcName = "fnAddItem()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		String itemCode = (String) req.getParameter("itemCode");
		//[[JOB-JOE-07050903
		itemCode = itemCode.trim();
		//JOB-JOE-07050903]]
		if (itemCode == null || itemCode.length() < 3)
		{
			throw new Exception("Item Code must have at least 3 characters!! ");
		}
/*		itemCode = StringManup.stripAndUpper(itemCode);
		itemCode = StringManup.removeSymbols(itemCode);*/
		// TKW20070228: Commented the lines above and replaced with the line below to stop spaces being stripped.
		itemCode = StringManup.convertUpper(itemCode);
		
		itemCode = StringManup.removeSymbolsExceptSpace(itemCode);
		// End TKW20070228
		newObj.code = itemCode;
		Item itemEJB2 = ItemNut.getObjectByCode(itemCode);
		
		String priceList = (String)req.getParameter("priceList");
		String priceMin = (String)req.getParameter("priceMin");

				
		if (itemEJB2 != null)
		{
			throw new Exception("The item code exists in the database!! ");
		}
		
		//if (priceMin.compareTo(priceList)> 0)
		//{
//			throw new Exception("The List Price must be greater than the minimum price!! ");
		//}
		
		
		
		
		String eanCode = (String) req.getParameter("eanCode");
		String upcCode = (String) req.getParameter("upcCode");
		String isbnCode = (String) req.getParameter("isbnCode");
		String itemName = (String) req.getParameter("itemName");
		eanCode = eanCode.trim().toUpperCase();
		upcCode = upcCode.trim().toUpperCase();
		isbnCode = isbnCode.trim().toUpperCase();
		itemName = itemName.trim();
		if (eanCode.length() > 4)
		{
			ItemObject eanItmObj = ItemNut.getObjectByEAN(eanCode);
			if (eanItmObj != null)
			{
				throw new Exception("The EAN Code exists in the database!!");
			}
		}
		if (upcCode.length() > 4)
		{
			ItemObject upcItmObj = ItemNut.getObjectByUPC(upcCode);
			if (upcItmObj != null)
			{
				throw new Exception("The UPC Code exists in the database!!");
			}
		}
		if (isbnCode.length() > 4)
		{
			ItemObject isbnItmObj = ItemNut.getObjectByISBN(isbnCode);
			if (isbnItmObj != null)
			{
				throw new Exception("The ISBN Code exists in the database!!");
			}
		}
		if (itemName == null || itemName.length() < 3)
		{
			throw new Exception("Item Name must have at least 3 characters!! ");
		}
		if (itemName.length() > 999)
		{
			throw new Exception("Item Name must not have more than 1000 characters!! ");
		}
		newObj.eanCode = eanCode;
		newObj.upcCode = upcCode;
		newObj.isbnCode = isbnCode;
		newObj.name = itemName;
		newObj.description = (String) req.getParameter("itemDescription");
		newObj.remarks1 = req.getParameter("remarks1");
		newObj.remarks2 = req.getParameter("remarks2");
		newObj.keywords = req.getParameter("keywords");
	
		String itemUOM = req.getParameter("itemUOM");
		if (itemUOM == null || itemUOM.length() < 2)
		{
			throw new Exception("Unit of Measure must have at least 2 characters!! ");
		} else
		{
			newObj.uom = itemUOM.toUpperCase();
		}
		newObj.enumInvType = new Integer(ItemBean.INV_TYPE_INVENTORY);
		String catId = req.getParameter("itemCategoryId");
		newObj.categoryId = new Integer(catId);
		newObj.category1 = req.getParameter("itemCategory1");
		newObj.category2 = req.getParameter("itemCategory2");
		newObj.category3 = req.getParameter("itemCategory3");
		newObj.category4 = req.getParameter("itemCategory4");
		newObj.category5 = req.getParameter("itemCategory5");
		String serialized = req.getParameter("serialized");
		if (serialized.equals("true"))
		{
			newObj.serialized = true;
		} else
		{
			newObj.serialized = false;
		}
		String itemType1 = req.getParameter("itemType1");
		if (itemType1 != null)
		{
			newObj.itemType1 = itemType1;
		}
		String itemCurrency = req.getParameter("itemCurrency");
		newObj.priceList = new BigDecimal(req.getParameter("priceList"));
		newObj.priceSale = new BigDecimal(req.getParameter("priceSale"));
		newObj.priceDisc1 = new BigDecimal(req.getParameter("priceDisc1"));
		newObj.priceDisc2 = new BigDecimal(req.getParameter("priceDisc2"));
		newObj.priceDisc3 = new BigDecimal(req.getParameter("priceDisc3"));
		newObj.priceMin = new BigDecimal(req.getParameter("priceMin"));
		//newObj.maUnitCost = new BigDecimal(req.getParameter("maUnitCost"));
		newObj.replacementUnitCost = new BigDecimal(req.getParameter("replacementCost"));
		newObj.reserved1 = req.getParameter("reserved1");
		newObj.reserved2 = req.getParameter("reserved2");

		newObj.weight = new BigDecimal(req.getParameter("weight"));
		newObj.length = new BigDecimal(req.getParameter("length"));
		newObj.width = new BigDecimal(req.getParameter("width"));
		newObj.depth = new BigDecimal(req.getParameter("depth"));
		newObj.thresholdQtyReorder = new BigDecimal(req.getParameter("qtyReorder"));
		newObj.thresholdQtyMaxQty = new BigDecimal(req.getParameter("qtyMaxQty"));
		newObj.minOrderQty = new BigDecimal(req.getParameter("qtyMinQty"));
		newObj.leadTime = new Long(req.getParameter("qtyLeadTimeQty"));		
		
		
		newObj.tax_rate = new BigDecimal(req.getParameter("taxRate"));
		newObj.tax_option = req.getParameter("taxOption").toString();

		String rebateMethod = req.getParameter("rebateMethod");
		if (rebateMethod != null && rebateMethod.equals("absolute"))
		{
			newObj.rebate1Price = new BigDecimal(req.getParameter("rebate1Price"));
			if (newObj.priceList.signum() > 0)
			{
				newObj.rebate1Pct = newObj.rebate1Price.divide(newObj.priceList, 12, BigDecimal.ROUND_HALF_EVEN);
			}
		}
		if (rebateMethod != null && rebateMethod.equals("percent"))
		{
			newObj.rebate1Pct = new BigDecimal(req.getParameter("rebate1Pct"));
			newObj.rebate1Pct = newObj.rebate1Pct.divide(new BigDecimal(100), 12, BigDecimal.ROUND_HALF_EVEN);
			newObj.rebate1Price = newObj.priceList.multiply(newObj.rebate1Pct);
		}
		newObj.rebate1Start = TimeFormat.createTimestamp(req.getParameter("rebate1Start"));
		newObj.rebate1End = TimeFormat.createTimestamp(req.getParameter("rebate1End"));
		String prodReq = req.getParameter("productionRequired");
		if (prodReq != null && prodReq.equals("true"))
		{
			newObj.productionRequired = true;
		} else
		{
			newObj.productionRequired = false;
		}
		String delReq = req.getParameter("deliveryRequired");
		if (delReq != null && delReq.equals("true"))
		{
			newObj.deliveryRequired = true;
		} else
		{
			newObj.deliveryRequired = false;
		}
		
		// 20080522 Jimmy
		newObj.outQty = new BigDecimal(req.getParameter("outQty"));
		newObj.outUnit = req.getParameter("outUnit");
		newObj.inQty = new BigDecimal(req.getParameter("inQty"));
		newObj.inUnit = req.getParameter("inUnit");
		newObj.innQty = new BigDecimal(req.getParameter("innQty"));
		newObj.innUnit = req.getParameter("innUnit");
		newObj.inmQty = new BigDecimal(req.getParameter("inmQty"));
		newObj.inmUnit = req.getParameter("inmUnit");
		
		if (newObj.inmQty.intValue() > 0) {
			newObj.uom = newObj.inmUnit;
		}else if (newObj.innQty.intValue() > 0) {
			newObj.uom = newObj.innUnit;
		}else if (newObj.inQty.intValue() > 0) {
			newObj.uom = newObj.inUnit;
		}
		
		HttpSession session = req.getSession();
		newObj.userIdUpdate = (Integer) session.getAttribute("userId");
		String lCurrency = "MYR"; // default
		Item itemEJB = ItemNut.fnCreate(newObj);

		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = (Integer) session.getAttribute("userId");
			atObj.auditType = AuditTrailBean.TYPE_CONFIG;
			atObj.time = TimeFormat.getTimestamp();
			atObj.remarks = "item-code-create: ITEMCODE:" + newObj.code + " ";
			atObj.tc_entity_table = ItemBean.TABLENAME;
			atObj.tc_entity_id = itemEJB.getPkid();
			atObj.tc_action = AuditTrailBean.TC_ACTION_CREATE;
			AuditTrailNut.fnCreate(atObj);
		}
		
		req.setAttribute("itemObj", newObj);
	}
}
/*==========================================================
 *
 * Copyright of Vincent Lee (vlee@vlee.net,
 *      vincent@wavelet.biz). All Rights Reserved.
 * (http://www.wavelet.biz)
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
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;

public class DoInvItemAddRem implements Action
{
	private String strClassName = "DoInvItemAddRem";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("inv-setup-addrem-item-page");
		}
		if (formName.equals("addItem"))
		{
			Log.printVerbose(strClassName + ": formName = addItem");
			try
			{
				fnAddItem(servlet, req, res);
				return new ActionRouter("inv-setup-edit-item-page");
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
				return new ActionRouter("inv-setup-addrem-item-page");
			}
		}
		return new ActionRouter("inv-setup-addrem-item-page");
	}

	protected void fnAddItem(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		ItemObject newObj = new ItemObject();
		String funcName = "fnAddItem()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		String itemCode = (String) req.getParameter("itemCode");
		if (itemCode == null || itemCode.length() < 3)
		{
			throw new Exception("Item Code must have at least 3 characters!! ");
		}
		itemCode = StringManup.stripAndUpper(itemCode);
		itemCode = StringManup.removeSymbols(itemCode);
		newObj.code = itemCode;
		Item itemEJB2 = ItemNut.getObjectByCode(itemCode);
		if (itemEJB2 != null)
		{
			throw new Exception("The item code exists in the database!! ");
		}
		String eanCode = (String) req.getParameter("eanCode");
		String upcCode = (String) req.getParameter("upcCode");
		String isbnCode = (String) req.getParameter("isbnCode");
		String itemName = (String) req.getParameter("itemName");
		eanCode = eanCode.trim().toUpperCase();
		upcCode = upcCode.trim().toUpperCase();
		isbnCode = isbnCode.trim().toUpperCase();
		itemName = itemName.trim();
		if (eanCode.length() > 4)
		{
			ItemObject eanItmObj = ItemNut.getObjectByEAN(eanCode);
			if (eanItmObj != null)
			{
				throw new Exception("The EAN Code exists in the database!!");
			}
		}
		if (upcCode.length() > 4)
		{
			ItemObject upcItmObj = ItemNut.getObjectByUPC(upcCode);
			if (upcItmObj != null)
			{
				throw new Exception("The UPC Code exists in the database!!");
			}
		}
		if (isbnCode.length() > 4)
		{
			ItemObject isbnItmObj = ItemNut.getObjectByISBN(isbnCode);
			if (isbnItmObj != null)
			{
				throw new Exception("The ISBN Code exists in the database!!");
			}
		}
		if (itemName == null || itemName.length() < 3)
		{
			throw new Exception("Item Name must have at least 3 characters!! ");
		}
		if (itemName.length() > 99)
		{
			throw new Exception("Item Name must not have more than 100 characters!! ");
		}
		newObj.eanCode = eanCode;
		newObj.upcCode = upcCode;
		newObj.isbnCode = isbnCode;
		newObj.name = itemName;
		newObj.description = (String) req.getParameter("itemDescription");
		String itemUOM = req.getParameter("itemUOM");
		if (itemUOM == null || itemUOM.length() < 2)
		{
			throw new Exception("Unit of Measure must have at least 2 characters!! ");
		} else
		{
			newObj.uom = itemUOM.toUpperCase();
		}
		newObj.enumInvType = new Integer(ItemBean.INV_TYPE_INVENTORY);
		String catId = req.getParameter("itemCategoryId");
		newObj.categoryId = new Integer(catId);
		newObj.category1 = req.getParameter("itemCategory1");
		newObj.category2 = req.getParameter("itemCategory2");
		newObj.category3 = req.getParameter("itemCategory3");
		newObj.category4 = req.getParameter("itemCategory4");
		newObj.category5 = req.getParameter("itemCategory5");
		String serialized = req.getParameter("serialized");
		if (serialized.equals("true"))
		{
			newObj.serialized = true;
		} else
		{
			newObj.serialized = false;
		}
		String itemType1 = req.getParameter("itemType1");
		if (itemType1 != null)
		{
			newObj.itemType1 = itemType1;
		}
		String itemCurrency = req.getParameter("itemCurrency");
		newObj.priceList = new BigDecimal(req.getParameter("priceList"));
		newObj.priceSale = new BigDecimal(req.getParameter("priceSale"));
		newObj.priceDisc1 = new BigDecimal(req.getParameter("priceDisc1"));
		newObj.priceDisc2 = new BigDecimal(req.getParameter("priceDisc2"));
		newObj.priceDisc3 = new BigDecimal(req.getParameter("priceDisc3"));
		newObj.priceMin = new BigDecimal(req.getParameter("priceMin"));
		newObj.maUnitCost = new BigDecimal(req.getParameter("maUnitCost"));
		newObj.replacementUnitCost = new BigDecimal(req.getParameter("replacementCost"));
		newObj.reserved1 = req.getParameter("reserved1");

		newObj.weight = new BigDecimal(req.getParameter("weight"));
		newObj.length = new BigDecimal(req.getParameter("length"));
		newObj.width = new BigDecimal(req.getParameter("width"));
		newObj.depth = new BigDecimal(req.getParameter("depth"));
		String rebateMethod = req.getParameter("rebateMethod");
		if (rebateMethod != null && rebateMethod.equals("absolute"))
		{
			newObj.rebate1Price = new BigDecimal(req.getParameter("rebate1Price"));
			if (newObj.priceList.signum() > 0)
			{
				newObj.rebate1Pct = newObj.rebate1Price.divide(newObj.priceList, 12, BigDecimal.ROUND_HALF_EVEN);
			}
		}
		if (rebateMethod != null && rebateMethod.equals("percent"))
		{
			newObj.rebate1Pct = new BigDecimal(req.getParameter("rebate1Pct"));
			newObj.rebate1Pct = newObj.rebate1Pct.divide(new BigDecimal(100), 12, BigDecimal.ROUND_HALF_EVEN);
			newObj.rebate1Price = newObj.priceList.multiply(newObj.rebate1Pct);
		}
		newObj.rebate1Start = TimeFormat.createTimestamp(req.getParameter("rebate1Start"));
		newObj.rebate1End = TimeFormat.createTimestamp(req.getParameter("rebate1End"));
		String prodReq = req.getParameter("productionRequired");
		if (prodReq != null && prodReq.equals("true"))
		{
			newObj.productionRequired = true;
		} else
		{
			newObj.productionRequired = false;
		}
		String delReq = req.getParameter("deliveryRequired");
		if (delReq != null && delReq.equals("true"))
		{
			newObj.deliveryRequired = true;
		} else
		{
			newObj.deliveryRequired = false;
		}
		HttpSession session = req.getSession();
		newObj.userIdUpdate = (Integer) session.getAttribute("userId");
		String lCurrency = "MYR"; // default
		Item itemEJB = ItemNut.fnCreate(newObj);
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = (Integer) session.getAttribute("userId");
			atObj.auditType = AuditTrailBean.TYPE_CONFIG;
			atObj.time = TimeFormat.getTimestamp();
			atObj.remarks = "item-code-create: ITEMCODE:" + newObj.code + " ";
			AuditTrailNut.fnCreate(atObj);
		}
		POSItemObject posObj = new POSItemObject();
		posObj.itemFKId = newObj.pkid;
		posObj.itemType = POSItemBean.TYPE_INV;
		posObj.currency = lCurrency;
		posObj.unitPriceStd = newObj.priceList;
		posObj.unitPriceDiscounted = newObj.priceDisc1;
		posObj.unitPriceMin = newObj.maUnitCost;
		posObj.userIdUpdate = newObj.userIdUpdate;
		POSItem posEJB = POSItemNut.fnCreate(posObj);
		PurchaseItemObject purObj = new PurchaseItemObject();
		purObj.itemFKId = newObj.pkid;
		purObj.itemType = POSItemBean.TYPE_INV;
		purObj.currency = lCurrency;
		purObj.unitPriceStd = newObj.replacementUnitCost;
		purObj.unitPriceDiscounted = newObj.priceDisc1;
		purObj.unitPriceMin = newObj.maUnitCost;
		purObj.userIdUpdate = newObj.userIdUpdate;
		PurchaseItem purEJB = PurchaseItemNut.fnCreate(purObj);
		req.setAttribute("itemObj", newObj);
	}
}
/*==========================================================
 *
 * Copyright of Vincent Lee (vlee@vlee.net,
 *      vincent@wavelet.biz). All Rights Reserved.
 * (http://www.wavelet.biz)
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
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;

public class DoInvItemAddRem implements Action
{
	private String strClassName = "DoInvItemAddRem";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("inv-setup-addrem-item-page");
		}
		if (formName.equals("addItem"))
		{
			Log.printVerbose(strClassName + ": formName = addItem");
			try
			{
				fnAddItem(servlet, req, res);
				return new ActionRouter("inv-setup-edit-item-page");
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
				return new ActionRouter("inv-setup-addrem-item-page");
			}
		}
		return new ActionRouter("inv-setup-addrem-item-page");
	}

	protected void fnAddItem(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		ItemObject newObj = new ItemObject();
		String funcName = "fnAddItem()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		String itemCode = (String) req.getParameter("itemCode");
		if (itemCode == null || itemCode.length() < 3)
		{
			throw new Exception("Item Code must have at least 3 characters!! ");
		}
		itemCode = StringManup.stripAndUpper(itemCode);
		itemCode = StringManup.removeSymbols(itemCode);
		newObj.code = itemCode;
		Item itemEJB2 = ItemNut.getObjectByCode(itemCode);
		if (itemEJB2 != null)
		{
			throw new Exception("The item code exists in the database!! ");
		}
		String eanCode = (String) req.getParameter("eanCode");
		String upcCode = (String) req.getParameter("upcCode");
		String isbnCode = (String) req.getParameter("isbnCode");
		String itemName = (String) req.getParameter("itemName");
		eanCode = eanCode.trim().toUpperCase();
		upcCode = upcCode.trim().toUpperCase();
		isbnCode = isbnCode.trim().toUpperCase();
		itemName = itemName.trim();
		if (eanCode.length() > 4)
		{
			ItemObject eanItmObj = ItemNut.getObjectByEAN(eanCode);
			if (eanItmObj != null)
			{
				throw new Exception("The EAN Code exists in the database!!");
			}
		}
		if (upcCode.length() > 4)
		{
			ItemObject upcItmObj = ItemNut.getObjectByUPC(upcCode);
			if (upcItmObj != null)
			{
				throw new Exception("The UPC Code exists in the database!!");
			}
		}
		if (isbnCode.length() > 4)
		{
			ItemObject isbnItmObj = ItemNut.getObjectByISBN(isbnCode);
			if (isbnItmObj != null)
			{
				throw new Exception("The ISBN Code exists in the database!!");
			}
		}
		if (itemName == null || itemName.length() < 3)
		{
			throw new Exception("Item Name must have at least 3 characters!! ");
		}
		if (itemName.length() > 99)
		{
			throw new Exception("Item Name must not have more than 100 characters!! ");
		}
		newObj.eanCode = eanCode;
		newObj.upcCode = upcCode;
		newObj.isbnCode = isbnCode;
		newObj.name = itemName;
		newObj.description = (String) req.getParameter("itemDescription");
		String itemUOM = req.getParameter("itemUOM");
		if (itemUOM == null || itemUOM.length() < 2)
		{
			throw new Exception("Unit of Measure must have at least 2 characters!! ");
		} else
		{
			newObj.uom = itemUOM.toUpperCase();
		}
		newObj.enumInvType = new Integer(ItemBean.INV_TYPE_INVENTORY);
		String catId = req.getParameter("itemCategoryId");
		newObj.categoryId = new Integer(catId);
		newObj.category1 = req.getParameter("itemCategory1");
		newObj.category2 = req.getParameter("itemCategory2");
		newObj.category3 = req.getParameter("itemCategory3");
		newObj.category4 = req.getParameter("itemCategory4");
		newObj.category5 = req.getParameter("itemCategory5");
		String serialized = req.getParameter("serialized");
		if (serialized.equals("true"))
		{
			newObj.serialized = true;
		} else
		{
			newObj.serialized = false;
		}
		String itemType1 = req.getParameter("itemType1");
		if (itemType1 != null)
		{
			newObj.itemType1 = itemType1;
		}
		String itemCurrency = req.getParameter("itemCurrency");
		newObj.priceList = new BigDecimal(req.getParameter("priceList"));
		newObj.priceSale = new BigDecimal(req.getParameter("priceSale"));
		newObj.priceDisc1 = new BigDecimal(req.getParameter("priceDisc1"));
		newObj.priceDisc2 = new BigDecimal(req.getParameter("priceDisc2"));
		newObj.maUnitCost = new BigDecimal(req.getParameter("maUnitCost"));
		newObj.replacementUnitCost = new BigDecimal(req.getParameter("replacementCost"));
		newObj.weight = new BigDecimal(req.getParameter("weight"));
		newObj.length = new BigDecimal(req.getParameter("length"));
		newObj.width = new BigDecimal(req.getParameter("width"));
		newObj.depth = new BigDecimal(req.getParameter("depth"));
		String rebateMethod = req.getParameter("rebateMethod");
		if (rebateMethod != null && rebateMethod.equals("absolute"))
		{
			newObj.rebate1Price = new BigDecimal(req.getParameter("rebate1Price"));
			if (newObj.priceList.signum() > 0)
			{
				newObj.rebate1Pct = newObj.rebate1Price.divide(newObj.priceList, 12, BigDecimal.ROUND_HALF_EVEN);
			}
		}
		if (rebateMethod != null && rebateMethod.equals("percent"))
		{
			newObj.rebate1Pct = new BigDecimal(req.getParameter("rebate1Pct"));
			newObj.rebate1Pct = newObj.rebate1Pct.divide(new BigDecimal(100), 12, BigDecimal.ROUND_HALF_EVEN);
			newObj.rebate1Price = newObj.priceList.multiply(newObj.rebate1Pct);
		}
		newObj.rebate1Start = TimeFormat.createTimestamp(req.getParameter("rebate1Start"));
		newObj.rebate1End = TimeFormat.createTimestamp(req.getParameter("rebate1End"));
		String prodReq = req.getParameter("productionRequired");
		if (prodReq != null && prodReq.equals("true"))
		{
			newObj.productionRequired = true;
		} else
		{
			newObj.productionRequired = false;
		}
		String delReq = req.getParameter("deliveryRequired");
		if (delReq != null && delReq.equals("true"))
		{
			newObj.deliveryRequired = true;
		} else
		{
			newObj.deliveryRequired = false;
		}
		HttpSession session = req.getSession();
		newObj.userIdUpdate = (Integer) session.getAttribute("userId");
		String lCurrency = "MYR"; // default
		Item itemEJB = ItemNut.fnCreate(newObj);
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = (Integer) session.getAttribute("userId");
			atObj.auditType = AuditTrailBean.TYPE_CONFIG;
			atObj.time = TimeFormat.getTimestamp();
			atObj.remarks = "item-code-create: ITEMCODE:" + newObj.code + " ";
			AuditTrailNut.fnCreate(atObj);
		}
		POSItemObject posObj = new POSItemObject();
		posObj.itemFKId = newObj.pkid;
		posObj.itemType = POSItemBean.TYPE_INV;
		posObj.currency = lCurrency;
		posObj.unitPriceStd = newObj.priceList;
		posObj.unitPriceDiscounted = newObj.priceDisc1;
		posObj.unitPriceMin = newObj.maUnitCost;
		posObj.userIdUpdate = newObj.userIdUpdate;
		POSItem posEJB = POSItemNut.fnCreate(posObj);
		PurchaseItemObject purObj = new PurchaseItemObject();
		purObj.itemFKId = newObj.pkid;
		purObj.itemType = POSItemBean.TYPE_INV;
		purObj.currency = lCurrency;
		purObj.unitPriceStd = newObj.replacementUnitCost;
		purObj.unitPriceDiscounted = newObj.priceDisc1;
		purObj.unitPriceMin = newObj.maUnitCost;
		purObj.userIdUpdate = newObj.userIdUpdate;
		PurchaseItem purEJB = PurchaseItemNut.fnCreate(purObj);
		req.setAttribute("itemObj", newObj);
	}
}
/*==========================================================
 *
 * Copyright of Vincent Lee (vlee@vlee.net,
 *      vincent@wavelet.biz). All Rights Reserved.
 * (http://www.wavelet.biz)
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
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;

public class DoInvItemAddRem implements Action
{
	private String strClassName = "DoInvItemAddRem";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("inv-setup-addrem-item-page");
		}
		if (formName.equals("addItem"))
		{
			Log.printVerbose(strClassName + ": formName = addItem");
			try
			{
				fnAddItem(servlet, req, res);
				return new ActionRouter("inv-setup-edit-item-page");
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
				return new ActionRouter("inv-setup-addrem-item-page");
			}
		}
		return new ActionRouter("inv-setup-addrem-item-page");
	}

	protected void fnAddItem(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		ItemObject newObj = new ItemObject();
		String funcName = "fnAddItem()";
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		String itemCode = (String) req.getParameter("itemCode");
		if (itemCode == null || itemCode.length() < 3)
		{
			throw new Exception("Item Code must have at least 3 characters!! ");
		}
		itemCode = StringManup.stripAndUpper(itemCode);
		itemCode = StringManup.removeSymbols(itemCode);
		newObj.code = itemCode;
		Item itemEJB2 = ItemNut.getObjectByCode(itemCode);
		if (itemEJB2 != null)
		{
			throw new Exception("The item code exists in the database!! ");
		}
		String eanCode = (String) req.getParameter("eanCode");
		String upcCode = (String) req.getParameter("upcCode");
		String isbnCode = (String) req.getParameter("isbnCode");
		String itemName = (String) req.getParameter("itemName");
		eanCode = eanCode.trim().toUpperCase();
		upcCode = upcCode.trim().toUpperCase();
		isbnCode = isbnCode.trim().toUpperCase();
		itemName = itemName.trim();
		if (eanCode.length() > 4)
		{
			ItemObject eanItmObj = ItemNut.getObjectByEAN(eanCode);
			if (eanItmObj != null)
			{
				throw new Exception("The EAN Code exists in the database!!");
			}
		}
		if (upcCode.length() > 4)
		{
			ItemObject upcItmObj = ItemNut.getObjectByUPC(upcCode);
			if (upcItmObj != null)
			{
				throw new Exception("The UPC Code exists in the database!!");
			}
		}
		if (isbnCode.length() > 4)
		{
			ItemObject isbnItmObj = ItemNut.getObjectByISBN(isbnCode);
			if (isbnItmObj != null)
			{
				throw new Exception("The ISBN Code exists in the database!!");
			}
		}
		if (itemName == null || itemName.length() < 3)
		{
			throw new Exception("Item Name must have at least 3 characters!! ");
		}
		if (itemName.length() > 99)
		{
			throw new Exception("Item Name must not have more than 100 characters!! ");
		}
		newObj.eanCode = eanCode;
		newObj.upcCode = upcCode;
		newObj.isbnCode = isbnCode;
		newObj.name = itemName;
		newObj.description = (String) req.getParameter("itemDescription");
		String itemUOM = req.getParameter("itemUOM");
		if (itemUOM == null || itemUOM.length() < 2)
		{
			throw new Exception("Unit of Measure must have at least 2 characters!! ");
		} else
		{
			newObj.uom = itemUOM.toUpperCase();
		}
		newObj.enumInvType = new Integer(ItemBean.INV_TYPE_INVENTORY);
		String catId = req.getParameter("itemCategoryId");
		newObj.categoryId = new Integer(catId);
		newObj.category1 = req.getParameter("itemCategory1");
		newObj.category2 = req.getParameter("itemCategory2");
		newObj.category3 = req.getParameter("itemCategory3");
		newObj.category4 = req.getParameter("itemCategory4");
		newObj.category5 = req.getParameter("itemCategory5");
		String serialized = req.getParameter("serialized");
		if (serialized.equals("true"))
		{
			newObj.serialized = true;
		} else
		{
			newObj.serialized = false;
		}
		String itemType1 = req.getParameter("itemType1");
		if (itemType1 != null)
		{
			newObj.itemType1 = itemType1;
		}
		String itemCurrency = req.getParameter("itemCurrency");
		newObj.priceList = new BigDecimal(req.getParameter("priceList"));
		newObj.priceSale = new BigDecimal(req.getParameter("priceSale"));
		newObj.priceDisc1 = new BigDecimal(req.getParameter("priceDisc1"));
		newObj.priceDisc2 = new BigDecimal(req.getParameter("priceDisc2"));
		newObj.priceDisc3 = new BigDecimal(req.getParameter("priceDisc3"));
		newObj.priceMin = new BigDecimal(req.getParameter("priceMin"));
		newObj.maUnitCost = new BigDecimal(req.getParameter("maUnitCost"));
		newObj.replacementUnitCost = new BigDecimal(req.getParameter("replacementCost"));
		newObj.reserved1 = req.getParameter("reserved1");

		newObj.weight = new BigDecimal(req.getParameter("weight"));
		newObj.length = new BigDecimal(req.getParameter("length"));
		newObj.width = new BigDecimal(req.getParameter("width"));
		newObj.depth = new BigDecimal(req.getParameter("depth"));
		String rebateMethod = req.getParameter("rebateMethod");
		if (rebateMethod != null && rebateMethod.equals("absolute"))
		{
			newObj.rebate1Price = new BigDecimal(req.getParameter("rebate1Price"));
			if (newObj.priceList.signum() > 0)
			{
				newObj.rebate1Pct = newObj.rebate1Price.divide(newObj.priceList, 12, BigDecimal.ROUND_HALF_EVEN);
			}
		}
		if (rebateMethod != null && rebateMethod.equals("percent"))
		{
			newObj.rebate1Pct = new BigDecimal(req.getParameter("rebate1Pct"));
			newObj.rebate1Pct = newObj.rebate1Pct.divide(new BigDecimal(100), 12, BigDecimal.ROUND_HALF_EVEN);
			newObj.rebate1Price = newObj.priceList.multiply(newObj.rebate1Pct);
		}
		newObj.rebate1Start = TimeFormat.createTimestamp(req.getParameter("rebate1Start"));
		newObj.rebate1End = TimeFormat.createTimestamp(req.getParameter("rebate1End"));
		String prodReq = req.getParameter("productionRequired");
		if (prodReq != null && prodReq.equals("true"))
		{
			newObj.productionRequired = true;
		} else
		{
			newObj.productionRequired = false;
		}
		String delReq = req.getParameter("deliveryRequired");
		if (delReq != null && delReq.equals("true"))
		{
			newObj.deliveryRequired = true;
		} else
		{
			newObj.deliveryRequired = false;
		}
		HttpSession session = req.getSession();
		newObj.userIdUpdate = (Integer) session.getAttribute("userId");
		String lCurrency = "MYR"; // default
		Item itemEJB = ItemNut.fnCreate(newObj);
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = (Integer) session.getAttribute("userId");
			atObj.auditType = AuditTrailBean.TYPE_CONFIG;
			atObj.time = TimeFormat.getTimestamp();
			atObj.remarks = "item-code-create: ITEMCODE:" + newObj.code + " ";
			AuditTrailNut.fnCreate(atObj);
		}
		POSItemObject posObj = new POSItemObject();
		posObj.itemFKId = newObj.pkid;
		posObj.itemType = POSItemBean.TYPE_INV;
		posObj.currency = lCurrency;
		posObj.unitPriceStd = newObj.priceList;
		posObj.unitPriceDiscounted = newObj.priceDisc1;
		posObj.unitPriceMin = newObj.maUnitCost;
		posObj.userIdUpdate = newObj.userIdUpdate;
		POSItem posEJB = POSItemNut.fnCreate(posObj);
		PurchaseItemObject purObj = new PurchaseItemObject();
		purObj.itemFKId = newObj.pkid;
		purObj.itemType = POSItemBean.TYPE_INV;
		purObj.currency = lCurrency;
		purObj.unitPriceStd = newObj.replacementUnitCost;
		purObj.unitPriceDiscounted = newObj.priceDisc1;
		purObj.unitPriceMin = newObj.maUnitCost;
		purObj.userIdUpdate = newObj.userIdUpdate;
		PurchaseItem purEJB = PurchaseItemNut.fnCreate(purObj);
		req.setAttribute("itemObj", newObj);
	}
}
