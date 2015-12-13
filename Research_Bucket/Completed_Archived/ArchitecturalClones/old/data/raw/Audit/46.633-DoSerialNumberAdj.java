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

import java.math.BigDecimal;
import java.util.Vector;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.vlee.ejb.inventory.Item;
import com.vlee.ejb.inventory.ItemNut;
import com.vlee.ejb.inventory.ItemObject;
import com.vlee.ejb.inventory.SerialNumberDelta;
import com.vlee.ejb.inventory.SerialNumberDeltaBean;
import com.vlee.ejb.inventory.SerialNumberDeltaNut;
import com.vlee.ejb.inventory.SerialNumberDeltaObject;
import com.vlee.ejb.inventory.Stock;
import com.vlee.ejb.inventory.StockBean;
import com.vlee.ejb.inventory.StockNut;
import com.vlee.ejb.inventory.StockObject;
import com.vlee.ejb.user.AuditTrailBean;
import com.vlee.ejb.user.AuditTrailNut;
import com.vlee.ejb.user.AuditTrailObject;
import com.vlee.servlet.main.Action;
import com.vlee.servlet.main.ActionRouter;
import com.vlee.util.TimeFormat;
import com.vlee.util.QueryObject;


public class DoSerialNumberAdj implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = (String) req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("inv-serial-number-adj-page");
		}
		if (formName.equals("getStock"))
		{
			try
			{
				fnGetStock(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("getStock", ex.getMessage());
			}
		}
		if (formName.equals("adjSerial"))
		{
			try
			{
				fnAdjustSerial(servlet, req, res);
				req.setAttribute("alertSuccess", "Successfully added the serial number!");
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		return new ActionRouter("inv-serial-number-adj-page");
	}

	protected void fnGetStock(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		String itemCode = req.getParameter("itemCode");
		req.setAttribute("itemCode", itemCode);
		String locationId = req.getParameter("locationId");
		req.setAttribute("locationId", locationId);
		if (itemCode == null || itemCode.length() < 1)
		{
			return;
		}
		Item itemEJB = ItemNut.getObjectByCode(itemCode);
		if (itemEJB == null)
		{
			return;
		}
		Integer itemId = null;
		try
		{
			itemId = itemEJB.getPkid();
		} catch (Exception ex)
		{
		}
		;
		if (itemId == null)
		{
			throw new Exception("Invalid Item Code");
		}
		Integer location = null;
		try
		{
			location = new Integer(locationId);
		} catch (Exception ex)
		{
			throw new Exception("Invalid Location!");
		}
		Stock stkEJB = StockNut.getObjectBy(itemId, location, StockBean.COND_DEF);
		if (stkEJB == null)
		{
			throw new Exception("There's no stock at this location!");
		}
		try
		{
			ItemObject itemObj = itemEJB.getObject();
			if (itemObj.serialized == false)
			{
				throw new Exception("This item is not serialized!!");
			}
		} catch (Exception ex)
		{
			throw new Exception(ex.getMessage());
		}
		StockObject stkObj = null;
		try
		{
			stkObj = stkEJB.getObject();
		} catch (Exception ex)
		{
			throw new Exception("Error getting stock object!");
		}
		req.setAttribute("stkObj", stkObj);
		Vector vecSerial = new Vector(SerialNumberDeltaNut.getSerialByStock(stkObj.pkid, 1));
		req.setAttribute("vecSerial", vecSerial);
	}

	protected void fnAdjustSerial(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		Integer userId = (Integer) session.getAttribute("userId");
		String stockId = req.getParameter("stockId");
		req.setAttribute("stockId", stockId);
		String serial = req.getParameter("serial");
		req.setAttribute("serial", serial);
		String qty = req.getParameter("qty");
		req.setAttribute("qty", qty);
		String remarks = req.getParameter("remarks");
		req.setAttribute("remarks", remarks);
		BigDecimal bdQty = new BigDecimal(qty);
		StockObject stkObj = null;
		try
		{
			stkObj = StockNut.getObject(new Integer(stockId));
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		ItemObject itmObj = ItemNut.getObject(stkObj.itemId);
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = (Integer) session.getAttribute("userId");
			atObj.auditType = AuditTrailBean.TYPE_USAGE;
			atObj.time = TimeFormat.getTimestamp();
			atObj.remarks = "serial-adj: ITEMCODE:" + itmObj.code + " ";
			atObj.remarks += " Serial:" + serial + " qty:" + qty + " rmks:" + remarks;
			AuditTrailNut.fnCreate(atObj);
		}
		req.setAttribute("itemCode", itmObj.code);
		req.setAttribute("locationId", stkObj.locationId.toString());
		if (serial.length() < 2)
		{
			throw new Exception("Serial number is too short!");
		}
		
		//[[JOB-JOE
		// Checking logic:
		// Case 1: quantity already available
		// - Allow decrement
		// - Disallow increment
		// Case 2: Quantity not available
		// - Allow increment
		// - Disallow decrement

		boolean adjCheckOk = false;
		String deltaOpDesc = "Serial #" + serial + ": ";
		
		//[[Copy-pasted from com/vlee/bean/pos/CreatePurchaseReturnSession.java
		QueryObject querySerial = new QueryObject(new String[] {
					SerialNumberDeltaBean.SERIAL + " = '" + serial + "' ",
					SerialNumberDeltaBean.STOCKID + " = '" + stockId + "' "});
		querySerial.setOrder(" ORDER BY " + SerialNumberDeltaBean.TXN_TIME + " ");
		
		Vector vecSerial = new Vector(SerialNumberDeltaNut.getObjects(querySerial));
		
		BigDecimal qtyAvailable = new BigDecimal(0);
		
		for (int cnt1 = 0; cnt1 < vecSerial.size(); cnt1++)
		{
				SerialNumberDeltaObject sndObj = (SerialNumberDeltaObject) vecSerial.get(cnt1);
				
				System.out.println("DoSerialNumberAdj::fnAdjustSerial(): serial quantity : "+sndObj.quantity.toString());	
				
				qtyAvailable = qtyAvailable.add(sndObj.quantity);
				
				System.out.println("DoSerialNumberAdj::fnAdjustSerial(): qtyAvailable : "+qtyAvailable.toString());	
		}
		
		//Copy-pasted]]
		SerialNumberDeltaObject sndObj = new SerialNumberDeltaObject();
		sndObj.namespace = SerialNumberDeltaBean.NS_INVENTORY;
		sndObj.txnType = SerialNumberDeltaBean.TT_ADJUSTMENT;
		// sndObj.txnCode = "";
		sndObj.serialNumber = serial;
		sndObj.personInCharge = userId;
		// sndObj.processNode = new
		// Integer("0");//SerialNumberDeltaBean.PNODE_DEFAULT ;
		sndObj.stockId = stkObj.pkid;
		sndObj.itemId = stkObj.itemId;
		sndObj.itemCode = itmObj.code;
		
		
		if(qtyAvailable.compareTo(new BigDecimal(1)) >= 0 && bdQty.signum() < 0 )
		{
			sndObj.quantity = bdQty;//bdQty.multiply(new BigDecimal(-1));
			adjCheckOk = true;
			deltaOpDesc += " present quantity is " + qtyAvailable.toString() + ", allowed to -1";
			
		} 
		else if(qtyAvailable.compareTo(new BigDecimal(1)) < 0 && bdQty.signum() > 0)
		{
			sndObj.quantity = bdQty;//bdQty.multiply(new BigDecimal(+1));
			adjCheckOk = true;
			deltaOpDesc += " present quantity is " + qtyAvailable.toString() + ", allowed to +1";
			
		} 
		else
		{
			deltaOpDesc += " present quantity is " + qtyAvailable.toString() + ", delta operation not permitted";
			throw new Exception(deltaOpDesc);
		}
		
		System.out.println(deltaOpDesc);
		
		
		
		sndObj.currency = "";
		sndObj.unitPrice = stkObj.unitCostMA;
		sndObj.currency2 = sndObj.currency;
		sndObj.unitPrice2 = sndObj.unitPrice;
		sndObj.txnTime = TimeFormat.getTimestamp();
		sndObj.remarks = remarks;
		// intReserved1 = new Integer(0);
		// strReserved1 = ""; /// permit
		// strReserved2 = ""; /// document Number (for reference only)
		// strReserved3 = ""; /// vessel
		// strReserved4 = ""; /// container
		// sndObj.entityTable = "";
		// sndObj.entityId =
		sndObj.docTable = "inv_sn_adj";
		// sndObj.docKey = this.mPkid; /// !!!! need to set this later !!!
		// sndObj.state = SerialNumberDeltaBean.STATE_CREATED;
		// sndObj.status = SerialNumberDeltaBean.STATUS_ACTIVE;
		sndObj.userIdEdit = userId;
		// sndObj.timeEdit = TimeFormat.getTimestamp();
		
		if(adjCheckOk==true)
		{
			SerialNumberDelta sndEJB = SerialNumberDeltaNut.fnCreate(sndObj);
			if (sndEJB == null)
			{
				throw new Exception("Failed to create Serial Number!");
			}
		}
		
		
		
		//JOB-JOE]]
		/*Vector*/ vecSerial = new Vector(SerialNumberDeltaNut.getSerialByStock(stkObj.pkid, 1));
		req.setAttribute("vecSerial", vecSerial);
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

import java.math.BigDecimal;
import java.util.Vector;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.vlee.ejb.inventory.Item;
import com.vlee.ejb.inventory.ItemNut;
import com.vlee.ejb.inventory.ItemObject;
import com.vlee.ejb.inventory.SerialNumberDelta;
import com.vlee.ejb.inventory.SerialNumberDeltaBean;
import com.vlee.ejb.inventory.SerialNumberDeltaNut;
import com.vlee.ejb.inventory.SerialNumberDeltaObject;
import com.vlee.ejb.inventory.Stock;
import com.vlee.ejb.inventory.StockBean;
import com.vlee.ejb.inventory.StockNut;
import com.vlee.ejb.inventory.StockObject;
import com.vlee.ejb.user.AuditTrailBean;
import com.vlee.ejb.user.AuditTrailNut;
import com.vlee.ejb.user.AuditTrailObject;
import com.vlee.servlet.main.Action;
import com.vlee.servlet.main.ActionRouter;
import com.vlee.util.TimeFormat;

public class DoSerialNumberAdj implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = (String) req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("inv-serial-number-adj-page");
		}
		if (formName.equals("getStock"))
		{
			try
			{
				fnGetStock(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("getStock", ex.getMessage());
			}
		}
		if (formName.equals("adjSerial"))
		{
			try
			{
				fnAdjustSerial(servlet, req, res);
				req.setAttribute("alertSuccess", "Successfully added the serial number!");
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		return new ActionRouter("inv-serial-number-adj-page");
	}

	protected void fnGetStock(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		String itemCode = req.getParameter("itemCode");
		req.setAttribute("itemCode", itemCode);
		String locationId = req.getParameter("locationId");
		req.setAttribute("locationId", locationId);
		if (itemCode == null || itemCode.length() < 1)
		{
			return;
		}
		Item itemEJB = ItemNut.getObjectByCode(itemCode);
		if (itemEJB == null)
		{
			return;
		}
		Integer itemId = null;
		try
		{
			itemId = itemEJB.getPkid();
		} catch (Exception ex)
		{
		}
		;
		if (itemId == null)
		{
			throw new Exception("Invalid Item Code");
		}
		Integer location = null;
		try
		{
			location = new Integer(locationId);
		} catch (Exception ex)
		{
			throw new Exception("Invalid Location!");
		}
		Stock stkEJB = StockNut.getObjectBy(itemId, location, StockBean.COND_DEF);
		if (stkEJB == null)
		{
			throw new Exception("There's no stock at this location!");
		}
		try
		{
			ItemObject itemObj = itemEJB.getObject();
			if (itemObj.serialized == false)
			{
				throw new Exception("This item is not serialized!!");
			}
		} catch (Exception ex)
		{
			throw new Exception(ex.getMessage());
		}
		StockObject stkObj = null;
		try
		{
			stkObj = stkEJB.getObject();
		} catch (Exception ex)
		{
			throw new Exception("Error getting stock object!");
		}
		req.setAttribute("stkObj", stkObj);
		Vector vecSerial = new Vector(SerialNumberDeltaNut.getSerialByStock(stkObj.pkid, 1));
		req.setAttribute("vecSerial", vecSerial);
	}

	protected void fnAdjustSerial(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		Integer userId = (Integer) session.getAttribute("userId");
		String stockId = req.getParameter("stockId");
		req.setAttribute("stockId", stockId);
		String serial = req.getParameter("serial");
		req.setAttribute("serial", serial);
		String qty = req.getParameter("qty");
		req.setAttribute("qty", qty);
		String remarks = req.getParameter("remarks");
		req.setAttribute("remarks", remarks);
		BigDecimal bdQty = new BigDecimal(qty);
		StockObject stkObj = null;
		try
		{
			stkObj = StockNut.getObject(new Integer(stockId));
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		ItemObject itmObj = ItemNut.getObject(stkObj.itemId);
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = (Integer) session.getAttribute("userId");
			atObj.auditType = AuditTrailBean.TYPE_USAGE;
			atObj.time = TimeFormat.getTimestamp();
			atObj.remarks = "serial-adj: ITEMCODE:" + itmObj.code + " ";
			atObj.remarks += " Serial:" + serial + " qty:" + qty + " rmks:" + remarks;
			AuditTrailNut.fnCreate(atObj);
		}
		req.setAttribute("itemCode", itmObj.code);
		req.setAttribute("locationId", stkObj.locationId.toString());
		if (serial.length() < 2)
		{
			throw new Exception("Serial number is too short!");
		}
		SerialNumberDeltaObject sndObj = new SerialNumberDeltaObject();
		sndObj.namespace = SerialNumberDeltaBean.NS_INVENTORY;
		sndObj.txnType = SerialNumberDeltaBean.TT_ADJUSTMENT;
		// sndObj.txnCode = "";
		sndObj.serialNumber = serial;
		sndObj.personInCharge = userId;
		// sndObj.processNode = new
		// Integer("0");//SerialNumberDeltaBean.PNODE_DEFAULT ;
		sndObj.stockId = stkObj.pkid;
		sndObj.itemId = stkObj.itemId;
		sndObj.itemCode = itmObj.code;
		sndObj.quantity = bdQty;
		sndObj.currency = "";
		sndObj.unitPrice = stkObj.unitCostMA;
		sndObj.currency2 = sndObj.currency;
		sndObj.unitPrice2 = sndObj.unitPrice;
		sndObj.txnTime = TimeFormat.getTimestamp();
		sndObj.remarks = remarks;
		// intReserved1 = new Integer(0);
		// strReserved1 = ""; /// permit
		// strReserved2 = ""; /// document Number (for reference only)
		// strReserved3 = ""; /// vessel
		// strReserved4 = ""; /// container
		// sndObj.entityTable = "";
		// sndObj.entityId =
		sndObj.docTable = "inv_sn_adj";
		// sndObj.docKey = this.mPkid; /// !!!! need to set this later !!!
		// sndObj.state = SerialNumberDeltaBean.STATE_CREATED;
		// sndObj.status = SerialNumberDeltaBean.STATUS_ACTIVE;
		sndObj.userIdEdit = userId;
		// sndObj.timeEdit = TimeFormat.getTimestamp();
		SerialNumberDelta sndEJB = SerialNumberDeltaNut.fnCreate(sndObj);
		if (sndEJB == null)
		{
			throw new Exception("Failed to create Serial Number!");
		}
		Vector vecSerial = new Vector(SerialNumberDeltaNut.getSerialByStock(stkObj.pkid, 1));
		req.setAttribute("vecSerial", vecSerial);
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

import java.math.BigDecimal;
import java.util.Vector;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.vlee.ejb.inventory.Item;
import com.vlee.ejb.inventory.ItemNut;
import com.vlee.ejb.inventory.ItemObject;
import com.vlee.ejb.inventory.SerialNumberDelta;
import com.vlee.ejb.inventory.SerialNumberDeltaBean;
import com.vlee.ejb.inventory.SerialNumberDeltaNut;
import com.vlee.ejb.inventory.SerialNumberDeltaObject;
import com.vlee.ejb.inventory.Stock;
import com.vlee.ejb.inventory.StockBean;
import com.vlee.ejb.inventory.StockNut;
import com.vlee.ejb.inventory.StockObject;
import com.vlee.ejb.user.AuditTrailBean;
import com.vlee.ejb.user.AuditTrailNut;
import com.vlee.ejb.user.AuditTrailObject;
import com.vlee.servlet.main.Action;
import com.vlee.servlet.main.ActionRouter;
import com.vlee.util.TimeFormat;

public class DoSerialNumberAdj implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = (String) req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("inv-serial-number-adj-page");
		}
		if (formName.equals("getStock"))
		{
			try
			{
				fnGetStock(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("getStock", ex.getMessage());
			}
		}
		if (formName.equals("adjSerial"))
		{
			try
			{
				fnAdjustSerial(servlet, req, res);
				req.setAttribute("alertSuccess", "Successfully added the serial number!");
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		return new ActionRouter("inv-serial-number-adj-page");
	}

	protected void fnGetStock(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		String itemCode = req.getParameter("itemCode");
		req.setAttribute("itemCode", itemCode);
		String locationId = req.getParameter("locationId");
		req.setAttribute("locationId", locationId);
		if (itemCode == null || itemCode.length() < 1)
		{
			return;
		}
		Item itemEJB = ItemNut.getObjectByCode(itemCode);
		if (itemEJB == null)
		{
			return;
		}
		Integer itemId = null;
		try
		{
			itemId = itemEJB.getPkid();
		} catch (Exception ex)
		{
		}
		;
		if (itemId == null)
		{
			throw new Exception("Invalid Item Code");
		}
		Integer location = null;
		try
		{
			location = new Integer(locationId);
		} catch (Exception ex)
		{
			throw new Exception("Invalid Location!");
		}
		Stock stkEJB = StockNut.getObjectBy(itemId, location, StockBean.COND_DEF);
		if (stkEJB == null)
		{
			throw new Exception("There's no stock at this location!");
		}
		try
		{
			ItemObject itemObj = itemEJB.getObject();
			if (itemObj.serialized == false)
			{
				throw new Exception("This item is not serialized!!");
			}
		} catch (Exception ex)
		{
			throw new Exception(ex.getMessage());
		}
		StockObject stkObj = null;
		try
		{
			stkObj = stkEJB.getObject();
		} catch (Exception ex)
		{
			throw new Exception("Error getting stock object!");
		}
		req.setAttribute("stkObj", stkObj);
		Vector vecSerial = new Vector(SerialNumberDeltaNut.getSerialByStock(stkObj.pkid, 1));
		req.setAttribute("vecSerial", vecSerial);
	}

	protected void fnAdjustSerial(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		Integer userId = (Integer) session.getAttribute("userId");
		String stockId = req.getParameter("stockId");
		req.setAttribute("stockId", stockId);
		String serial = req.getParameter("serial");
		req.setAttribute("serial", serial);
		String qty = req.getParameter("qty");
		req.setAttribute("qty", qty);
		String remarks = req.getParameter("remarks");
		req.setAttribute("remarks", remarks);
		BigDecimal bdQty = new BigDecimal(qty);
		StockObject stkObj = null;
		try
		{
			stkObj = StockNut.getObject(new Integer(stockId));
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		ItemObject itmObj = ItemNut.getObject(stkObj.itemId);
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = (Integer) session.getAttribute("userId");
			atObj.auditType = AuditTrailBean.TYPE_USAGE;
			atObj.time = TimeFormat.getTimestamp();
			atObj.remarks = "serial-adj: ITEMCODE:" + itmObj.code + " ";
			atObj.remarks += " Serial:" + serial + " qty:" + qty + " rmks:" + remarks;
			AuditTrailNut.fnCreate(atObj);
		}
		req.setAttribute("itemCode", itmObj.code);
		req.setAttribute("locationId", stkObj.locationId.toString());
		if (serial.length() < 2)
		{
			throw new Exception("Serial number is too short!");
		}
		SerialNumberDeltaObject sndObj = new SerialNumberDeltaObject();
		sndObj.namespace = SerialNumberDeltaBean.NS_INVENTORY;
		sndObj.txnType = SerialNumberDeltaBean.TT_ADJUSTMENT;
		// sndObj.txnCode = "";
		sndObj.serialNumber = serial;
		sndObj.personInCharge = userId;
		// sndObj.processNode = new
		// Integer("0");//SerialNumberDeltaBean.PNODE_DEFAULT ;
		sndObj.stockId = stkObj.pkid;
		sndObj.itemId = stkObj.itemId;
		sndObj.itemCode = itmObj.code;
		sndObj.quantity = bdQty;
		sndObj.currency = "";
		sndObj.unitPrice = stkObj.unitCostMA;
		sndObj.currency2 = sndObj.currency;
		sndObj.unitPrice2 = sndObj.unitPrice;
		sndObj.txnTime = TimeFormat.getTimestamp();
		sndObj.remarks = remarks;
		// intReserved1 = new Integer(0);
		// strReserved1 = ""; /// permit
		// strReserved2 = ""; /// document Number (for reference only)
		// strReserved3 = ""; /// vessel
		// strReserved4 = ""; /// container
		// sndObj.entityTable = "";
		// sndObj.entityId =
		sndObj.docTable = "inv_sn_adj";
		// sndObj.docKey = this.mPkid; /// !!!! need to set this later !!!
		// sndObj.state = SerialNumberDeltaBean.STATE_CREATED;
		// sndObj.status = SerialNumberDeltaBean.STATUS_ACTIVE;
		sndObj.userIdEdit = userId;
		// sndObj.timeEdit = TimeFormat.getTimestamp();
		SerialNumberDelta sndEJB = SerialNumberDeltaNut.fnCreate(sndObj);
		if (sndEJB == null)
		{
			throw new Exception("Failed to create Serial Number!");
		}
		Vector vecSerial = new Vector(SerialNumberDeltaNut.getSerialByStock(stkObj.pkid, 1));
		req.setAttribute("vecSerial", vecSerial);
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

import java.math.BigDecimal;
import java.util.Vector;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.vlee.ejb.inventory.Item;
import com.vlee.ejb.inventory.ItemNut;
import com.vlee.ejb.inventory.ItemObject;
import com.vlee.ejb.inventory.SerialNumberDelta;
import com.vlee.ejb.inventory.SerialNumberDeltaBean;
import com.vlee.ejb.inventory.SerialNumberDeltaNut;
import com.vlee.ejb.inventory.SerialNumberDeltaObject;
import com.vlee.ejb.inventory.Stock;
import com.vlee.ejb.inventory.StockBean;
import com.vlee.ejb.inventory.StockNut;
import com.vlee.ejb.inventory.StockObject;
import com.vlee.ejb.user.AuditTrailBean;
import com.vlee.ejb.user.AuditTrailNut;
import com.vlee.ejb.user.AuditTrailObject;
import com.vlee.servlet.main.Action;
import com.vlee.servlet.main.ActionRouter;
import com.vlee.util.TimeFormat;

public class DoSerialNumberAdj implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = (String) req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("inv-serial-number-adj-page");
		}
		if (formName.equals("getStock"))
		{
			try
			{
				fnGetStock(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("getStock", ex.getMessage());
			}
		}
		if (formName.equals("adjSerial"))
		{
			try
			{
				fnAdjustSerial(servlet, req, res);
				req.setAttribute("alertSuccess", "Successfully added the serial number!");
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		return new ActionRouter("inv-serial-number-adj-page");
	}

	protected void fnGetStock(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		String itemCode = req.getParameter("itemCode");
		req.setAttribute("itemCode", itemCode);
		String locationId = req.getParameter("locationId");
		req.setAttribute("locationId", locationId);
		if (itemCode == null || itemCode.length() < 1)
		{
			return;
		}
		Item itemEJB = ItemNut.getObjectByCode(itemCode);
		if (itemEJB == null)
		{
			return;
		}
		Integer itemId = null;
		try
		{
			itemId = itemEJB.getPkid();
		} catch (Exception ex)
		{
		}
		;
		if (itemId == null)
		{
			throw new Exception("Invalid Item Code");
		}
		Integer location = null;
		try
		{
			location = new Integer(locationId);
		} catch (Exception ex)
		{
			throw new Exception("Invalid Location!");
		}
		Stock stkEJB = StockNut.getObjectBy(itemId, location, StockBean.COND_DEF);
		if (stkEJB == null)
		{
			throw new Exception("There's no stock at this location!");
		}
		try
		{
			ItemObject itemObj = itemEJB.getObject();
			if (itemObj.serialized == false)
			{
				throw new Exception("This item is not serialized!!");
			}
		} catch (Exception ex)
		{
			throw new Exception(ex.getMessage());
		}
		StockObject stkObj = null;
		try
		{
			stkObj = stkEJB.getObject();
		} catch (Exception ex)
		{
			throw new Exception("Error getting stock object!");
		}
		req.setAttribute("stkObj", stkObj);
		Vector vecSerial = new Vector(SerialNumberDeltaNut.getSerialByStock(stkObj.pkid, 1));
		req.setAttribute("vecSerial", vecSerial);
	}

	protected void fnAdjustSerial(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		Integer userId = (Integer) session.getAttribute("userId");
		String stockId = req.getParameter("stockId");
		req.setAttribute("stockId", stockId);
		String serial = req.getParameter("serial");
		req.setAttribute("serial", serial);
		String qty = req.getParameter("qty");
		req.setAttribute("qty", qty);
		String remarks = req.getParameter("remarks");
		req.setAttribute("remarks", remarks);
		BigDecimal bdQty = new BigDecimal(qty);
		StockObject stkObj = null;
		try
		{
			stkObj = StockNut.getObject(new Integer(stockId));
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		ItemObject itmObj = ItemNut.getObject(stkObj.itemId);
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = (Integer) session.getAttribute("userId");
			atObj.auditType = AuditTrailBean.TYPE_USAGE;
			atObj.time = TimeFormat.getTimestamp();
			atObj.remarks = "serial-adj: ITEMCODE:" + itmObj.code + " ";
			atObj.remarks += " Serial:" + serial + " qty:" + qty + " rmks:" + remarks;
			AuditTrailNut.fnCreate(atObj);
		}
		req.setAttribute("itemCode", itmObj.code);
		req.setAttribute("locationId", stkObj.locationId.toString());
		if (serial.length() < 2)
		{
			throw new Exception("Serial number is too short!");
		}
		SerialNumberDeltaObject sndObj = new SerialNumberDeltaObject();
		sndObj.namespace = SerialNumberDeltaBean.NS_INVENTORY;
		sndObj.txnType = SerialNumberDeltaBean.TT_ADJUSTMENT;
		// sndObj.txnCode = "";
		sndObj.serialNumber = serial;
		sndObj.personInCharge = userId;
		// sndObj.processNode = new
		// Integer("0");//SerialNumberDeltaBean.PNODE_DEFAULT ;
		sndObj.stockId = stkObj.pkid;
		sndObj.itemId = stkObj.itemId;
		sndObj.itemCode = itmObj.code;
		sndObj.quantity = bdQty;
		sndObj.currency = "";
		sndObj.unitPrice = stkObj.unitCostMA;
		sndObj.currency2 = sndObj.currency;
		sndObj.unitPrice2 = sndObj.unitPrice;
		sndObj.txnTime = TimeFormat.getTimestamp();
		sndObj.remarks = remarks;
		// intReserved1 = new Integer(0);
		// strReserved1 = ""; /// permit
		// strReserved2 = ""; /// document Number (for reference only)
		// strReserved3 = ""; /// vessel
		// strReserved4 = ""; /// container
		// sndObj.entityTable = "";
		// sndObj.entityId =
		sndObj.docTable = "inv_sn_adj";
		// sndObj.docKey = this.mPkid; /// !!!! need to set this later !!!
		// sndObj.state = SerialNumberDeltaBean.STATE_CREATED;
		// sndObj.status = SerialNumberDeltaBean.STATUS_ACTIVE;
		sndObj.userIdEdit = userId;
		// sndObj.timeEdit = TimeFormat.getTimestamp();
		SerialNumberDelta sndEJB = SerialNumberDeltaNut.fnCreate(sndObj);
		if (sndEJB == null)
		{
			throw new Exception("Failed to create Serial Number!");
		}
		Vector vecSerial = new Vector(SerialNumberDeltaNut.getSerialByStock(stkObj.pkid, 1));
		req.setAttribute("vecSerial", vecSerial);
	}
}
