package com.vlee.servlet.test;

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

public class DoTopConStkTx implements Action
{
	private String strClassName = "DoTopConStkTx";
	private static Task curTask = null;

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		if (curTask != null)
		{
			req.setAttribute("curTask", curTask);
			return new ActionRouter("test-migrate-topcondb-pending-page");
		}
		// Get processing start time
		Timestamp tsStart = TimeFormat.getTimestamp();
		try
		{
			// Connect to topcon DB
			String url = "jdbc:postgresql://localhost:5432/topcon";
			Connection con = DriverManager.getConnection(url, "jboss", "jboss");
			String jbossURL = "jdbc:postgresql://localhost:5432/wsemp";
			Connection jbossCon = DriverManager.getConnection(jbossURL, "jboss", "jboss");
			Statement topconStmt = con.createStatement();
			Statement jbossStmt = jbossCon.createStatement();
			/*******************************************************************
			 * GLOBAL VARIABLES
			 ******************************************************************/
			Integer iDefLocId = new Integer(1000);
			Integer iDefSvcCtrId = new Integer(1);
			Integer iDefPCCenterId = new Integer(1000);
			Integer iDefCondId = new Integer(StockNut.STK_COND_GOOD);
			Integer iDefCashAccId = new Integer(1000);
			String strDefCurr = "MYR";
			HashMap hmCurr = new HashMap();
			hmCurr.put(new Integer(1), "MYR");
			hmCurr.put(new Integer(2), "USD");
			hmCurr.put(new Integer(3), "SGD");
			hmCurr.put(new Integer(4), "4");
			hmCurr.put(new Integer(5), "5");
			hmCurr.put(new Integer(6), "6");
			hmCurr.put(new Integer(7), "7");
			/*******************************************************************
			 * SESSION ATTRIBUTES
			 ******************************************************************/
			HttpSession session = req.getSession();
			User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
			Integer usrid = null;
			if (lUsr == null)
			{
				Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
				return new ActionRouter("test-migrate-topcondb-page");
			}
			try
			{
				usrid = lUsr.getUserId();
			} catch (Exception ex)
			{
				Log.printAudit("User does not exist: " + ex.getMessage());
			}
			/*
			 * THE LOGIC =========
			 * 
			 * for each row in topcon.stktx if (qtyin != 0) Assume stockIn else
			 * Assume stockOut
			 * 
			 * txtype mapping: S = Sales I = ??? /// sales return!! R = Purchase
			 * O = ??? /// Purchase return!! A = Adjustments T = Transfer W =
			 * Write-off ??
			 * 
			 */
			// Clear inv_stock_delta, and set inv_stock.bal = 0.00
			/*
			 * String clearStockDelta = "delete from inv_stock_delta"; String
			 * resetStockBal = "update inv_stock set bal = 0";
			 * jbossStmt.executeUpdate(clearStockDelta);
			 * jbossStmt.executeUpdate(resetStockBal);
			 */
			// Ensure we have the 2 unique warehouses
			Location newLoc = LocationNut.getObjectByCode("00");
			if (newLoc == null)
			{
				newLoc = LocationNut.getHome().create(LocationBean.NS_TRADING, "", "00", "Evaluation (00)",
						"Topcon Instruments (M) Sdn Bhd", new Integer(0), new Integer(0), TimeFormat.getTimestamp(),
						usrid);
			}
			Location newLoc2 = LocationNut.getObjectByCode("99");
			if (newLoc2 == null)
			{
				newLoc2 = LocationNut.getHome().create(LocationBean.NS_TRADING, "", "99", "Evaluation (99)",
						"Topcon Instruments (M) Sdn Bhd", new Integer(0), new Integer(0), TimeFormat.getTimestamp(),
						usrid);
			}
			Integer offset = new Integer(0);
			// Integer limit = new Integer(0);
			// Integer offset = null;
			Integer limit = null;
			String query = "select * from stktx";
			if (offset != null)
				query += " offset " + offset.intValue();
			if (limit != null)
				query += " limit " + limit.intValue();
			ResultSet rs = topconStmt.executeQuery(query);
			curTask = new Task("Stock Data Migration", rs.getFetchSize());
			int count = offset.intValue();
			while (rs.next())
			{
				curTask.increment();
				curTask.setTimeElapsed(TimeFormat.getTimestamp().getTime() - tsStart.getTime());
				Log.printDebug("Processing StkTx " + ++count);
				String strStkId = rs.getString("stkid");
				String strWhId = rs.getString("whid");
				String strTxDate = rs.getString("txdate");
				String strTxType = rs.getString("txtype");
				String strDocNo = rs.getString("docno");
				BigDecimal bdQtyIn = rs.getBigDecimal("qtyin");
				BigDecimal bdQtyOut = rs.getBigDecimal("qtyout");
				BigDecimal bdUCost = rs.getBigDecimal("ucost");
				String strComments = rs.getString("comments");
				String strStatus = rs.getString("status");
				String strFCur = rs.getString("fcur");
				BigDecimal bdFExRate = rs.getBigDecimal("fexrate");
				BigDecimal bdFUCost = rs.getBigDecimal("fucost");
				String strSCode = rs.getString("scode");
				BigDecimal bdSalesPrice = rs.getBigDecimal("sp");
				// BigDecimal bdItemTotal = rs.getBigDecimal("itemtotal");
				// Get the locId
				Integer thisLocId = iDefLocId;
				if (!strWhId.equals(""))
				{
					Location thisLoc = LocationNut.getObjectByCode(strWhId);
					if (thisLoc != null)
						thisLocId = thisLoc.getPkid();
				}
				// Integer thisStkId = null;
				// Get the corresponding itemId and stockId
				Item thisItem = ItemNut.getObjectByCode(strStkId);
				if (thisItem == null)
				{
					Log.printDebug(count + ": Item Code " + strStkId + " doesn't exist!! Creating a temp one ...");
					/** *** BEGIN: CREATE THE ITEM BASED ON STKTX **** */
					/*
					 * thisItem = ItemNut.getHome().create(
					 * strStkId,strStkId,strStkId, new Integer(1000), new
					 * Integer(0), new Integer(ItemBean.INV_TYPE_INVENTORY),
					 * TimeFormat.getTimestamp(),usrid);
					 */ItemObject itemObj = new ItemObject();
					// populate the properties here!!
					itemObj.code = strStkId;
					itemObj.name = strStkId;
					itemObj.description = strStkId;
					itemObj.userIdUpdate = usrid;
					itemObj.enumInvType = new Integer(ItemBean.INV_TYPE_INVENTORY);
					/*
					 * itemObj.priceList = sellingPrice; itemObj.priceSale =
					 * sellingPrice; itemObj.priceDisc1 = sellingPrice;
					 * itemObj.priceDisc2 = sellingPrice; itemObj.priceDisc3 =
					 * sellingPrice; itemObj.priceMin = costPrice;
					 * itemObj.fifoUnitCost = costPrice; itemObj.maUnitCost =
					 * costPrice; itemObj.waUnitCost = costPrice;
					 * itemObj.lastUnitCost = costPrice;
					 * itemObj.replacementUnitCost = costPrice;
					 */
					// itemObj.preferredSupplier = suppAccObj.pkid;
					thisItem = ItemNut.fnCreate(itemObj);
					// Then populate the corresponding stock table
					Stock auxStk = StockNut.getObjectBy(thisItem.getPkid(), thisLocId, iDefCondId, "");
					if (auxStk == null)
					{
						StockObject newObj = new StockObject();
						newObj.itemId = thisItem.getPkid();
						newObj.locationId = thisLocId;
						newObj.accPCCenterId = iDefPCCenterId;
						newObj.userIdUpdate = usrid;
						auxStk = StockNut.fnCreate(newObj);
					}
					// thisStkId = auxStk.getPkid();
					/** *** END: CREATE THE ITEM BASED ON CDHISD **** */
				} // end if (thisitem == null)
				// Get the StockId
				Stock thisStk = StockNut.getObjectBy(thisItem.getPkid(), thisLocId, iDefCondId, "");
				if (thisStk == null)
				{
					StockObject newObj = new StockObject();
					newObj.itemId = thisItem.getPkid();
					newObj.locationId = thisLocId;
					newObj.accPCCenterId = iDefPCCenterId;
					newObj.userIdUpdate = usrid;
					thisStk = StockNut.fnCreate(newObj);
				}
				BigDecimal thisUnitPrice = null;
				BigDecimal thisUnitPrice2 = null;
				BigDecimal thisQty = null;
				if (bdQtyIn.signum() == 0 && strTxType.equals("S"))
				{
					thisQty = bdQtyOut.negate();
					thisUnitPrice = bdSalesPrice;
					thisUnitPrice2 = bdSalesPrice.divide(bdFExRate, 2);
				} else if (strTxType.equals("R")) // purchase
				{
					thisQty = bdQtyIn;
					thisUnitPrice = bdUCost;
					thisUnitPrice2 = bdFUCost;
				} else
				{
					// Assume everything else to be some form of adjustment
					// in which case, it depends on whether qtyIn or qtyOut,
					// if qtyIn>0, reversing sales, take SP
					// else take ucost
					if (bdQtyIn.signum() != 0)
					{
						thisQty = bdQtyIn;
						thisUnitPrice = bdSalesPrice;
						thisUnitPrice2 = bdSalesPrice.divide(bdFExRate, 2);
					} else
					{
						thisQty = bdQtyOut.negate();
						thisUnitPrice = bdUCost;
						thisUnitPrice2 = bdFUCost;
					}
				}
				// Create a new StockDeltaObject and start populating it.
				StockDeltaObject thisStkDeltaObj = new StockDeltaObject();
				thisStkDeltaObj.namespace = "";
				thisStkDeltaObj.txnType = strTxType;
				// thisStkDeltaObj.txnCode = "";
				// thisStkDeltaObj.serial = "";
				// thisStkDeltaObj.personInCharge = new Integer("0");
				thisStkDeltaObj.processNode = StockDeltaBean.PNODE_DEFAULT;
				thisStkDeltaObj.stockId = thisStk.getPkid();
				// refStockId = new Integer("0");
				thisStkDeltaObj.itemId = thisItem.getPkid();
				thisStkDeltaObj.quantity = thisQty;
				thisStkDeltaObj.currency = strDefCurr;
				thisStkDeltaObj.unitPrice = thisUnitPrice;
				thisStkDeltaObj.currency2 = (String) hmCurr.get(new Integer(strFCur));
				thisStkDeltaObj.unitPrice2 = thisUnitPrice2;
				thisStkDeltaObj.txnTime = TimeFormat.createTimeStamp(strTxDate, "MM/dd/yy HH:mm:ss");
				// thisStkDeltaObj.schTime = TimeFormat.getTimestamp();
				thisStkDeltaObj.remarks = strComments + "(docno=" + strDocNo + ",scode=" + strSCode + ")";
				thisStkDeltaObj.intReserved1 = new Integer(0);
				thisStkDeltaObj.strReserved1 = strDocNo; // / Old DocNo
				thisStkDeltaObj.strReserved2 = strSCode; // / Old
															// customer/supplier
															// code
				thisStkDeltaObj.strReserved3 = strStatus; // / Old Status
				// thisStkDeltaObj.strReserved4 = ""; /// container
				// thisStkDeltaObj.entityTable = "";
				// thisStkDeltaObj.entityId = new Integer(0);
				// thisStkDeltaObj.docTable = "";
				// thisStkDeltaObj.docKey = new Long("0");
				// thisStkDeltaObj.state = StockDeltaBean.STATE_CREATED;
				// thisStkDeltaObj.status = StockDeltaBean.STATUS_ACTIVE;
				thisStkDeltaObj.userIdEdit = usrid;
				// thisStkDeltaObj.timeEdit = TimeFormat.getTimestamp();
				// finally, commit this to inv_stock_delta
				StockNut.execTxn(thisStkDeltaObj);
			} // end while (rs.next())
		} catch (Exception ex)
		{
			ex.printStackTrace();
			req.setAttribute("strErrMsg", "Error occurred while migrating Stk Txn: " + ex.getMessage());
		}
		Log.printVerbose("***** END: MIGRATION OF STOCK TXN *****");
		// Get processing End time
		Timestamp tsEnd = TimeFormat.getTimestamp();
		req.setAttribute("task", "MIGRATE STOCK TXN");
		req.setAttribute("tsStart", tsStart);
		req.setAttribute("tsEnd", tsEnd);
		// Clean up curTask;
		curTask = null;
		return new ActionRouter("test-migrate-topcondb-page");
	}
}
package com.vlee.servlet.test;

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

public class DoTopConStkTx implements Action
{
	private String strClassName = "DoTopConStkTx";
	private static Task curTask = null;

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		if (curTask != null)
		{
			req.setAttribute("curTask", curTask);
			return new ActionRouter("test-migrate-topcondb-pending-page");
		}
		// Get processing start time
		Timestamp tsStart = TimeFormat.getTimestamp();
		try
		{
			// Connect to topcon DB
			String url = "jdbc:postgresql://localhost:5432/topcon";
			Connection con = DriverManager.getConnection(url, "jboss", "jboss");
			String jbossURL = "jdbc:postgresql://localhost:5432/wsemp";
			Connection jbossCon = DriverManager.getConnection(jbossURL, "jboss", "jboss");
			Statement topconStmt = con.createStatement();
			Statement jbossStmt = jbossCon.createStatement();
			/*******************************************************************
			 * GLOBAL VARIABLES
			 ******************************************************************/
			Integer iDefLocId = new Integer(1000);
			Integer iDefSvcCtrId = new Integer(1);
			Integer iDefPCCenterId = new Integer(1000);
			Integer iDefCondId = new Integer(StockNut.STK_COND_GOOD);
			Integer iDefCashAccId = new Integer(1000);
			String strDefCurr = "MYR";
			HashMap hmCurr = new HashMap();
			hmCurr.put(new Integer(1), "MYR");
			hmCurr.put(new Integer(2), "USD");
			hmCurr.put(new Integer(3), "SGD");
			hmCurr.put(new Integer(4), "4");
			hmCurr.put(new Integer(5), "5");
			hmCurr.put(new Integer(6), "6");
			hmCurr.put(new Integer(7), "7");
			/*******************************************************************
			 * SESSION ATTRIBUTES
			 ******************************************************************/
			HttpSession session = req.getSession();
			User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
			Integer usrid = null;
			if (lUsr == null)
			{
				Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
				return new ActionRouter("test-migrate-topcondb-page");
			}
			try
			{
				usrid = lUsr.getUserId();
			} catch (Exception ex)
			{
				Log.printAudit("User does not exist: " + ex.getMessage());
			}
			/*
			 * THE LOGIC =========
			 * 
			 * for each row in topcon.stktx if (qtyin != 0) Assume stockIn else
			 * Assume stockOut
			 * 
			 * txtype mapping: S = Sales I = ??? /// sales return!! R = Purchase
			 * O = ??? /// Purchase return!! A = Adjustments T = Transfer W =
			 * Write-off ??
			 * 
			 */
			// Clear inv_stock_delta, and set inv_stock.bal = 0.00
			/*
			 * String clearStockDelta = "delete from inv_stock_delta"; String
			 * resetStockBal = "update inv_stock set bal = 0";
			 * jbossStmt.executeUpdate(clearStockDelta);
			 * jbossStmt.executeUpdate(resetStockBal);
			 */
			// Ensure we have the 2 unique warehouses
			Location newLoc = LocationNut.getObjectByCode("00");
			if (newLoc == null)
			{
				newLoc = LocationNut.getHome().create(LocationBean.NS_TRADING, "", "00", "Evaluation (00)",
						"Topcon Instruments (M) Sdn Bhd", new Integer(0), new Integer(0), TimeFormat.getTimestamp(),
						usrid);
			}
			Location newLoc2 = LocationNut.getObjectByCode("99");
			if (newLoc2 == null)
			{
				newLoc2 = LocationNut.getHome().create(LocationBean.NS_TRADING, "", "99", "Evaluation (99)",
						"Topcon Instruments (M) Sdn Bhd", new Integer(0), new Integer(0), TimeFormat.getTimestamp(),
						usrid);
			}
			Integer offset = new Integer(0);
			// Integer limit = new Integer(0);
			// Integer offset = null;
			Integer limit = null;
			String query = "select * from stktx";
			if (offset != null)
				query += " offset " + offset.intValue();
			if (limit != null)
				query += " limit " + limit.intValue();
			ResultSet rs = topconStmt.executeQuery(query);
			curTask = new Task("Stock Data Migration", rs.getFetchSize());
			int count = offset.intValue();
			while (rs.next())
			{
				curTask.increment();
				curTask.setTimeElapsed(TimeFormat.getTimestamp().getTime() - tsStart.getTime());
				Log.printDebug("Processing StkTx " + ++count);
				String strStkId = rs.getString("stkid");
				String strWhId = rs.getString("whid");
				String strTxDate = rs.getString("txdate");
				String strTxType = rs.getString("txtype");
				String strDocNo = rs.getString("docno");
				BigDecimal bdQtyIn = rs.getBigDecimal("qtyin");
				BigDecimal bdQtyOut = rs.getBigDecimal("qtyout");
				BigDecimal bdUCost = rs.getBigDecimal("ucost");
				String strComments = rs.getString("comments");
				String strStatus = rs.getString("status");
				String strFCur = rs.getString("fcur");
				BigDecimal bdFExRate = rs.getBigDecimal("fexrate");
				BigDecimal bdFUCost = rs.getBigDecimal("fucost");
				String strSCode = rs.getString("scode");
				BigDecimal bdSalesPrice = rs.getBigDecimal("sp");
				// BigDecimal bdItemTotal = rs.getBigDecimal("itemtotal");
				// Get the locId
				Integer thisLocId = iDefLocId;
				if (!strWhId.equals(""))
				{
					Location thisLoc = LocationNut.getObjectByCode(strWhId);
					if (thisLoc != null)
						thisLocId = thisLoc.getPkid();
				}
				// Integer thisStkId = null;
				// Get the corresponding itemId and stockId
				Item thisItem = ItemNut.getObjectByCode(strStkId);
				if (thisItem == null)
				{
					Log.printDebug(count + ": Item Code " + strStkId + " doesn't exist!! Creating a temp one ...");
					/** *** BEGIN: CREATE THE ITEM BASED ON STKTX **** */
					/*
					 * thisItem = ItemNut.getHome().create(
					 * strStkId,strStkId,strStkId, new Integer(1000), new
					 * Integer(0), new Integer(ItemBean.INV_TYPE_INVENTORY),
					 * TimeFormat.getTimestamp(),usrid);
					 */ItemObject itemObj = new ItemObject();
					// populate the properties here!!
					itemObj.code = strStkId;
					itemObj.name = strStkId;
					itemObj.description = strStkId;
					itemObj.userIdUpdate = usrid;
					itemObj.enumInvType = new Integer(ItemBean.INV_TYPE_INVENTORY);
					/*
					 * itemObj.priceList = sellingPrice; itemObj.priceSale =
					 * sellingPrice; itemObj.priceDisc1 = sellingPrice;
					 * itemObj.priceDisc2 = sellingPrice; itemObj.priceDisc3 =
					 * sellingPrice; itemObj.priceMin = costPrice;
					 * itemObj.fifoUnitCost = costPrice; itemObj.maUnitCost =
					 * costPrice; itemObj.waUnitCost = costPrice;
					 * itemObj.lastUnitCost = costPrice;
					 * itemObj.replacementUnitCost = costPrice;
					 */
					// itemObj.preferredSupplier = suppAccObj.pkid;
					thisItem = ItemNut.fnCreate(itemObj);
					POSItem auxPOSItem = POSItemNut.getPOSItem(thisItem.getPkid(), POSItemBean.TYPE_INV);
					if (auxPOSItem == null)
					{
						// BigDecimal bdQuotedPrice = (bdSalesPrice.signum() ==
						// 0)?bdUCost:bdSalesPrice;
						POSItemObject posObj = new POSItemObject();
						posObj.itemFKId = thisItem.getPkid();
						posObj.itemType = POSItemBean.TYPE_INV;
						posObj.currency = "MYR";
						posObj.unitPriceStd = bdSalesPrice;
						posObj.unitPriceDiscounted = bdSalesPrice;
						posObj.unitPriceMin = bdSalesPrice;
						posObj.userIdUpdate = usrid;
						auxPOSItem = POSItemNut.fnCreate(posObj);
						/*
						 * auxPOSItem = POSItemNut.getHome().create(
						 * thisItem.getPkid(),POSItemBean.TYPE_INV,
						 * "MYR",bdSalesPrice,new BigDecimal(0),new
						 * BigDecimal(0),
						 * TimeFormat.getTimestamp(),TimeFormat.getTimestamp(),usrid);
						 */}
					// Then populate the corresponding stock table
					Stock auxStk = StockNut.getObjectBy(thisItem.getPkid(), thisLocId, iDefCondId, "");
					if (auxStk == null)
					{
						StockObject newObj = new StockObject();
						newObj.itemId = thisItem.getPkid();
						newObj.locationId = thisLocId;
						newObj.accPCCenterId = iDefPCCenterId;
						newObj.userIdUpdate = usrid;
						auxStk = StockNut.fnCreate(newObj);
					}
					// thisStkId = auxStk.getPkid();
					/** *** END: CREATE THE ITEM BASED ON CDHISD **** */
				} // end if (thisitem == null)
				// Get the StockId
				Stock thisStk = StockNut.getObjectBy(thisItem.getPkid(), thisLocId, iDefCondId, "");
				if (thisStk == null)
				{
					StockObject newObj = new StockObject();
					newObj.itemId = thisItem.getPkid();
					newObj.locationId = thisLocId;
					newObj.accPCCenterId = iDefPCCenterId;
					newObj.userIdUpdate = usrid;
					thisStk = StockNut.fnCreate(newObj);
				}
				BigDecimal thisUnitPrice = null;
				BigDecimal thisUnitPrice2 = null;
				BigDecimal thisQty = null;
				if (bdQtyIn.signum() == 0 && strTxType.equals("S"))
				{
					thisQty = bdQtyOut.negate();
					thisUnitPrice = bdSalesPrice;
					thisUnitPrice2 = bdSalesPrice.divide(bdFExRate, 2);
				} else if (strTxType.equals("R")) // purchase
				{
					thisQty = bdQtyIn;
					thisUnitPrice = bdUCost;
					thisUnitPrice2 = bdFUCost;
				} else
				{
					// Assume everything else to be some form of adjustment
					// in which case, it depends on whether qtyIn or qtyOut,
					// if qtyIn>0, reversing sales, take SP
					// else take ucost
					if (bdQtyIn.signum() != 0)
					{
						thisQty = bdQtyIn;
						thisUnitPrice = bdSalesPrice;
						thisUnitPrice2 = bdSalesPrice.divide(bdFExRate, 2);
					} else
					{
						thisQty = bdQtyOut.negate();
						thisUnitPrice = bdUCost;
						thisUnitPrice2 = bdFUCost;
					}
				}
				// Create a new StockDeltaObject and start populating it.
				StockDeltaObject thisStkDeltaObj = new StockDeltaObject();
				thisStkDeltaObj.namespace = "";
				thisStkDeltaObj.txnType = strTxType;
				// thisStkDeltaObj.txnCode = "";
				// thisStkDeltaObj.serial = "";
				// thisStkDeltaObj.personInCharge = new Integer("0");
				thisStkDeltaObj.processNode = StockDeltaBean.PNODE_DEFAULT;
				thisStkDeltaObj.stockId = thisStk.getPkid();
				// refStockId = new Integer("0");
				thisStkDeltaObj.itemId = thisItem.getPkid();
				thisStkDeltaObj.quantity = thisQty;
				thisStkDeltaObj.currency = strDefCurr;
				thisStkDeltaObj.unitPrice = thisUnitPrice;
				thisStkDeltaObj.currency2 = (String) hmCurr.get(new Integer(strFCur));
				thisStkDeltaObj.unitPrice2 = thisUnitPrice2;
				thisStkDeltaObj.txnTime = TimeFormat.createTimeStamp(strTxDate, "MM/dd/yy HH:mm:ss");
				// thisStkDeltaObj.schTime = TimeFormat.getTimestamp();
				thisStkDeltaObj.remarks = strComments + "(docno=" + strDocNo + ",scode=" + strSCode + ")";
				thisStkDeltaObj.intReserved1 = new Integer(0);
				thisStkDeltaObj.strReserved1 = strDocNo; // / Old DocNo
				thisStkDeltaObj.strReserved2 = strSCode; // / Old
															// customer/supplier
															// code
				thisStkDeltaObj.strReserved3 = strStatus; // / Old Status
				// thisStkDeltaObj.strReserved4 = ""; /// container
				// thisStkDeltaObj.entityTable = "";
				// thisStkDeltaObj.entityId = new Integer(0);
				// thisStkDeltaObj.docTable = "";
				// thisStkDeltaObj.docKey = new Long("0");
				// thisStkDeltaObj.state = StockDeltaBean.STATE_CREATED;
				// thisStkDeltaObj.status = StockDeltaBean.STATUS_ACTIVE;
				thisStkDeltaObj.userIdEdit = usrid;
				// thisStkDeltaObj.timeEdit = TimeFormat.getTimestamp();
				// finally, commit this to inv_stock_delta
				StockNut.execTxn(thisStkDeltaObj);
			} // end while (rs.next())
		} catch (Exception ex)
		{
			ex.printStackTrace();
			req.setAttribute("strErrMsg", "Error occurred while migrating Stk Txn: " + ex.getMessage());
		}
		Log.printVerbose("***** END: MIGRATION OF STOCK TXN *****");
		// Get processing End time
		Timestamp tsEnd = TimeFormat.getTimestamp();
		req.setAttribute("task", "MIGRATE STOCK TXN");
		req.setAttribute("tsStart", tsStart);
		req.setAttribute("tsEnd", tsEnd);
		// Clean up curTask;
		curTask = null;
		return new ActionRouter("test-migrate-topcondb-page");
	}
}
package com.vlee.servlet.test;

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

public class DoTopConStkTx implements Action
{
	private String strClassName = "DoTopConStkTx";
	private static Task curTask = null;

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		if (curTask != null)
		{
			req.setAttribute("curTask", curTask);
			return new ActionRouter("test-migrate-topcondb-pending-page");
		}
		// Get processing start time
		Timestamp tsStart = TimeFormat.getTimestamp();
		try
		{
			// Connect to topcon DB
			String url = "jdbc:postgresql://localhost:5432/topcon";
			Connection con = DriverManager.getConnection(url, "jboss", "jboss");
			String jbossURL = "jdbc:postgresql://localhost:5432/wsemp";
			Connection jbossCon = DriverManager.getConnection(jbossURL, "jboss", "jboss");
			Statement topconStmt = con.createStatement();
			Statement jbossStmt = jbossCon.createStatement();
			/*******************************************************************
			 * GLOBAL VARIABLES
			 ******************************************************************/
			Integer iDefLocId = new Integer(1000);
			Integer iDefSvcCtrId = new Integer(1);
			Integer iDefPCCenterId = new Integer(1000);
			Integer iDefCondId = new Integer(StockNut.STK_COND_GOOD);
			Integer iDefCashAccId = new Integer(1000);
			String strDefCurr = "MYR";
			HashMap hmCurr = new HashMap();
			hmCurr.put(new Integer(1), "MYR");
			hmCurr.put(new Integer(2), "USD");
			hmCurr.put(new Integer(3), "SGD");
			hmCurr.put(new Integer(4), "4");
			hmCurr.put(new Integer(5), "5");
			hmCurr.put(new Integer(6), "6");
			hmCurr.put(new Integer(7), "7");
			/*******************************************************************
			 * SESSION ATTRIBUTES
			 ******************************************************************/
			HttpSession session = req.getSession();
			User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
			Integer usrid = null;
			if (lUsr == null)
			{
				Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
				return new ActionRouter("test-migrate-topcondb-page");
			}
			try
			{
				usrid = lUsr.getUserId();
			} catch (Exception ex)
			{
				Log.printAudit("User does not exist: " + ex.getMessage());
			}
			/*
			 * THE LOGIC =========
			 * 
			 * for each row in topcon.stktx if (qtyin != 0) Assume stockIn else
			 * Assume stockOut
			 * 
			 * txtype mapping: S = Sales I = ??? /// sales return!! R = Purchase
			 * O = ??? /// Purchase return!! A = Adjustments T = Transfer W =
			 * Write-off ??
			 * 
			 */
			// Clear inv_stock_delta, and set inv_stock.bal = 0.00
			/*
			 * String clearStockDelta = "delete from inv_stock_delta"; String
			 * resetStockBal = "update inv_stock set bal = 0";
			 * jbossStmt.executeUpdate(clearStockDelta);
			 * jbossStmt.executeUpdate(resetStockBal);
			 */
			// Ensure we have the 2 unique warehouses
			Location newLoc = LocationNut.getObjectByCode("00");
			if (newLoc == null)
			{
				newLoc = LocationNut.getHome().create(LocationBean.NS_TRADING, "", "00", "Evaluation (00)",
						"Topcon Instruments (M) Sdn Bhd", new Integer(0), new Integer(0), TimeFormat.getTimestamp(),
						usrid);
			}
			Location newLoc2 = LocationNut.getObjectByCode("99");
			if (newLoc2 == null)
			{
				newLoc2 = LocationNut.getHome().create(LocationBean.NS_TRADING, "", "99", "Evaluation (99)",
						"Topcon Instruments (M) Sdn Bhd", new Integer(0), new Integer(0), TimeFormat.getTimestamp(),
						usrid);
			}
			Integer offset = new Integer(0);
			// Integer limit = new Integer(0);
			// Integer offset = null;
			Integer limit = null;
			String query = "select * from stktx";
			if (offset != null)
				query += " offset " + offset.intValue();
			if (limit != null)
				query += " limit " + limit.intValue();
			ResultSet rs = topconStmt.executeQuery(query);
			curTask = new Task("Stock Data Migration", rs.getFetchSize());
			int count = offset.intValue();
			while (rs.next())
			{
				curTask.increment();
				curTask.setTimeElapsed(TimeFormat.getTimestamp().getTime() - tsStart.getTime());
				Log.printDebug("Processing StkTx " + ++count);
				String strStkId = rs.getString("stkid");
				String strWhId = rs.getString("whid");
				String strTxDate = rs.getString("txdate");
				String strTxType = rs.getString("txtype");
				String strDocNo = rs.getString("docno");
				BigDecimal bdQtyIn = rs.getBigDecimal("qtyin");
				BigDecimal bdQtyOut = rs.getBigDecimal("qtyout");
				BigDecimal bdUCost = rs.getBigDecimal("ucost");
				String strComments = rs.getString("comments");
				String strStatus = rs.getString("status");
				String strFCur = rs.getString("fcur");
				BigDecimal bdFExRate = rs.getBigDecimal("fexrate");
				BigDecimal bdFUCost = rs.getBigDecimal("fucost");
				String strSCode = rs.getString("scode");
				BigDecimal bdSalesPrice = rs.getBigDecimal("sp");
				// BigDecimal bdItemTotal = rs.getBigDecimal("itemtotal");
				// Get the locId
				Integer thisLocId = iDefLocId;
				if (!strWhId.equals(""))
				{
					Location thisLoc = LocationNut.getObjectByCode(strWhId);
					if (thisLoc != null)
						thisLocId = thisLoc.getPkid();
				}
				// Integer thisStkId = null;
				// Get the corresponding itemId and stockId
				Item thisItem = ItemNut.getObjectByCode(strStkId);
				if (thisItem == null)
				{
					Log.printDebug(count + ": Item Code " + strStkId + " doesn't exist!! Creating a temp one ...");
					/** *** BEGIN: CREATE THE ITEM BASED ON STKTX **** */
					/*
					 * thisItem = ItemNut.getHome().create(
					 * strStkId,strStkId,strStkId, new Integer(1000), new
					 * Integer(0), new Integer(ItemBean.INV_TYPE_INVENTORY),
					 * TimeFormat.getTimestamp(),usrid);
					 */ItemObject itemObj = new ItemObject();
					// populate the properties here!!
					itemObj.code = strStkId;
					itemObj.name = strStkId;
					itemObj.description = strStkId;
					itemObj.userIdUpdate = usrid;
					itemObj.enumInvType = new Integer(ItemBean.INV_TYPE_INVENTORY);
					/*
					 * itemObj.priceList = sellingPrice; itemObj.priceSale =
					 * sellingPrice; itemObj.priceDisc1 = sellingPrice;
					 * itemObj.priceDisc2 = sellingPrice; itemObj.priceDisc3 =
					 * sellingPrice; itemObj.priceMin = costPrice;
					 * itemObj.fifoUnitCost = costPrice; itemObj.maUnitCost =
					 * costPrice; itemObj.waUnitCost = costPrice;
					 * itemObj.lastUnitCost = costPrice;
					 * itemObj.replacementUnitCost = costPrice;
					 */
					// itemObj.preferredSupplier = suppAccObj.pkid;
					thisItem = ItemNut.fnCreate(itemObj);
					POSItem auxPOSItem = POSItemNut.getPOSItem(thisItem.getPkid(), POSItemBean.TYPE_INV);
					if (auxPOSItem == null)
					{
						// BigDecimal bdQuotedPrice = (bdSalesPrice.signum() ==
						// 0)?bdUCost:bdSalesPrice;
						POSItemObject posObj = new POSItemObject();
						posObj.itemFKId = thisItem.getPkid();
						posObj.itemType = POSItemBean.TYPE_INV;
						posObj.currency = "MYR";
						posObj.unitPriceStd = bdSalesPrice;
						posObj.unitPriceDiscounted = bdSalesPrice;
						posObj.unitPriceMin = bdSalesPrice;
						posObj.userIdUpdate = usrid;
						auxPOSItem = POSItemNut.fnCreate(posObj);
						/*
						 * auxPOSItem = POSItemNut.getHome().create(
						 * thisItem.getPkid(),POSItemBean.TYPE_INV,
						 * "MYR",bdSalesPrice,new BigDecimal(0),new
						 * BigDecimal(0),
						 * TimeFormat.getTimestamp(),TimeFormat.getTimestamp(),usrid);
						 */}
					// Then populate the corresponding stock table
					Stock auxStk = StockNut.getObjectBy(thisItem.getPkid(), thisLocId, iDefCondId, "");
					if (auxStk == null)
					{
						StockObject newObj = new StockObject();
						newObj.itemId = thisItem.getPkid();
						newObj.locationId = thisLocId;
						newObj.accPCCenterId = iDefPCCenterId;
						newObj.userIdUpdate = usrid;
						auxStk = StockNut.fnCreate(newObj);
					}
					// thisStkId = auxStk.getPkid();
					/** *** END: CREATE THE ITEM BASED ON CDHISD **** */
				} // end if (thisitem == null)
				// Get the StockId
				Stock thisStk = StockNut.getObjectBy(thisItem.getPkid(), thisLocId, iDefCondId, "");
				if (thisStk == null)
				{
					StockObject newObj = new StockObject();
					newObj.itemId = thisItem.getPkid();
					newObj.locationId = thisLocId;
					newObj.accPCCenterId = iDefPCCenterId;
					newObj.userIdUpdate = usrid;
					thisStk = StockNut.fnCreate(newObj);
				}
				BigDecimal thisUnitPrice = null;
				BigDecimal thisUnitPrice2 = null;
				BigDecimal thisQty = null;
				if (bdQtyIn.signum() == 0 && strTxType.equals("S"))
				{
					thisQty = bdQtyOut.negate();
					thisUnitPrice = bdSalesPrice;
					thisUnitPrice2 = bdSalesPrice.divide(bdFExRate, 2);
				} else if (strTxType.equals("R")) // purchase
				{
					thisQty = bdQtyIn;
					thisUnitPrice = bdUCost;
					thisUnitPrice2 = bdFUCost;
				} else
				{
					// Assume everything else to be some form of adjustment
					// in which case, it depends on whether qtyIn or qtyOut,
					// if qtyIn>0, reversing sales, take SP
					// else take ucost
					if (bdQtyIn.signum() != 0)
					{
						thisQty = bdQtyIn;
						thisUnitPrice = bdSalesPrice;
						thisUnitPrice2 = bdSalesPrice.divide(bdFExRate, 2);
					} else
					{
						thisQty = bdQtyOut.negate();
						thisUnitPrice = bdUCost;
						thisUnitPrice2 = bdFUCost;
					}
				}
				// Create a new StockDeltaObject and start populating it.
				StockDeltaObject thisStkDeltaObj = new StockDeltaObject();
				thisStkDeltaObj.namespace = "";
				thisStkDeltaObj.txnType = strTxType;
				// thisStkDeltaObj.txnCode = "";
				// thisStkDeltaObj.serial = "";
				// thisStkDeltaObj.personInCharge = new Integer("0");
				thisStkDeltaObj.processNode = StockDeltaBean.PNODE_DEFAULT;
				thisStkDeltaObj.stockId = thisStk.getPkid();
				// refStockId = new Integer("0");
				thisStkDeltaObj.itemId = thisItem.getPkid();
				thisStkDeltaObj.quantity = thisQty;
				thisStkDeltaObj.currency = strDefCurr;
				thisStkDeltaObj.unitPrice = thisUnitPrice;
				thisStkDeltaObj.currency2 = (String) hmCurr.get(new Integer(strFCur));
				thisStkDeltaObj.unitPrice2 = thisUnitPrice2;
				thisStkDeltaObj.txnTime = TimeFormat.createTimeStamp(strTxDate, "MM/dd/yy HH:mm:ss");
				// thisStkDeltaObj.schTime = TimeFormat.getTimestamp();
				thisStkDeltaObj.remarks = strComments + "(docno=" + strDocNo + ",scode=" + strSCode + ")";
				thisStkDeltaObj.intReserved1 = new Integer(0);
				thisStkDeltaObj.strReserved1 = strDocNo; // / Old DocNo
				thisStkDeltaObj.strReserved2 = strSCode; // / Old
															// customer/supplier
															// code
				thisStkDeltaObj.strReserved3 = strStatus; // / Old Status
				// thisStkDeltaObj.strReserved4 = ""; /// container
				// thisStkDeltaObj.entityTable = "";
				// thisStkDeltaObj.entityId = new Integer(0);
				// thisStkDeltaObj.docTable = "";
				// thisStkDeltaObj.docKey = new Long("0");
				// thisStkDeltaObj.state = StockDeltaBean.STATE_CREATED;
				// thisStkDeltaObj.status = StockDeltaBean.STATUS_ACTIVE;
				thisStkDeltaObj.userIdEdit = usrid;
				// thisStkDeltaObj.timeEdit = TimeFormat.getTimestamp();
				// finally, commit this to inv_stock_delta
				StockNut.execTxn(thisStkDeltaObj);
			} // end while (rs.next())
		} catch (Exception ex)
		{
			ex.printStackTrace();
			req.setAttribute("strErrMsg", "Error occurred while migrating Stk Txn: " + ex.getMessage());
		}
		Log.printVerbose("***** END: MIGRATION OF STOCK TXN *****");
		// Get processing End time
		Timestamp tsEnd = TimeFormat.getTimestamp();
		req.setAttribute("task", "MIGRATE STOCK TXN");
		req.setAttribute("tsStart", tsStart);
		req.setAttribute("tsEnd", tsEnd);
		// Clean up curTask;
		curTask = null;
		return new ActionRouter("test-migrate-topcondb-page");
	}
}
package com.vlee.servlet.test;

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

public class DoTopConStkTx implements Action
{
	private String strClassName = "DoTopConStkTx";
	private static Task curTask = null;

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		if (curTask != null)
		{
			req.setAttribute("curTask", curTask);
			return new ActionRouter("test-migrate-topcondb-pending-page");
		}
		// Get processing start time
		Timestamp tsStart = TimeFormat.getTimestamp();
		try
		{
			// Connect to topcon DB
			String url = "jdbc:postgresql://localhost:5432/topcon";
			Connection con = DriverManager.getConnection(url, "jboss", "jboss");
			String jbossURL = "jdbc:postgresql://localhost:5432/wsemp";
			Connection jbossCon = DriverManager.getConnection(jbossURL, "jboss", "jboss");
			Statement topconStmt = con.createStatement();
			Statement jbossStmt = jbossCon.createStatement();
			/*******************************************************************
			 * GLOBAL VARIABLES
			 ******************************************************************/
			Integer iDefLocId = new Integer(1000);
			Integer iDefSvcCtrId = new Integer(1);
			Integer iDefPCCenterId = new Integer(1000);
			Integer iDefCondId = new Integer(StockNut.STK_COND_GOOD);
			Integer iDefCashAccId = new Integer(1000);
			String strDefCurr = "MYR";
			HashMap hmCurr = new HashMap();
			hmCurr.put(new Integer(1), "MYR");
			hmCurr.put(new Integer(2), "USD");
			hmCurr.put(new Integer(3), "SGD");
			hmCurr.put(new Integer(4), "4");
			hmCurr.put(new Integer(5), "5");
			hmCurr.put(new Integer(6), "6");
			hmCurr.put(new Integer(7), "7");
			/*******************************************************************
			 * SESSION ATTRIBUTES
			 ******************************************************************/
			HttpSession session = req.getSession();
			User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
			Integer usrid = null;
			if (lUsr == null)
			{
				Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
				return new ActionRouter("test-migrate-topcondb-page");
			}
			try
			{
				usrid = lUsr.getUserId();
			} catch (Exception ex)
			{
				Log.printAudit("User does not exist: " + ex.getMessage());
			}
			/*
			 * THE LOGIC =========
			 * 
			 * for each row in topcon.stktx if (qtyin != 0) Assume stockIn else
			 * Assume stockOut
			 * 
			 * txtype mapping: S = Sales I = ??? /// sales return!! R = Purchase
			 * O = ??? /// Purchase return!! A = Adjustments T = Transfer W =
			 * Write-off ??
			 * 
			 */
			// Clear inv_stock_delta, and set inv_stock.bal = 0.00
			/*
			 * String clearStockDelta = "delete from inv_stock_delta"; String
			 * resetStockBal = "update inv_stock set bal = 0";
			 * jbossStmt.executeUpdate(clearStockDelta);
			 * jbossStmt.executeUpdate(resetStockBal);
			 */
			// Ensure we have the 2 unique warehouses
			Location newLoc = LocationNut.getObjectByCode("00");
			if (newLoc == null)
			{
				newLoc = LocationNut.getHome().create(LocationBean.NS_TRADING, "", "00", "Evaluation (00)",
						"Topcon Instruments (M) Sdn Bhd", new Integer(0), new Integer(0), TimeFormat.getTimestamp(),
						usrid);
			}
			Location newLoc2 = LocationNut.getObjectByCode("99");
			if (newLoc2 == null)
			{
				newLoc2 = LocationNut.getHome().create(LocationBean.NS_TRADING, "", "99", "Evaluation (99)",
						"Topcon Instruments (M) Sdn Bhd", new Integer(0), new Integer(0), TimeFormat.getTimestamp(),
						usrid);
			}
			Integer offset = new Integer(0);
			// Integer limit = new Integer(0);
			// Integer offset = null;
			Integer limit = null;
			String query = "select * from stktx";
			if (offset != null)
				query += " offset " + offset.intValue();
			if (limit != null)
				query += " limit " + limit.intValue();
			ResultSet rs = topconStmt.executeQuery(query);
			curTask = new Task("Stock Data Migration", rs.getFetchSize());
			int count = offset.intValue();
			while (rs.next())
			{
				curTask.increment();
				curTask.setTimeElapsed(TimeFormat.getTimestamp().getTime() - tsStart.getTime());
				Log.printDebug("Processing StkTx " + ++count);
				String strStkId = rs.getString("stkid");
				String strWhId = rs.getString("whid");
				String strTxDate = rs.getString("txdate");
				String strTxType = rs.getString("txtype");
				String strDocNo = rs.getString("docno");
				BigDecimal bdQtyIn = rs.getBigDecimal("qtyin");
				BigDecimal bdQtyOut = rs.getBigDecimal("qtyout");
				BigDecimal bdUCost = rs.getBigDecimal("ucost");
				String strComments = rs.getString("comments");
				String strStatus = rs.getString("status");
				String strFCur = rs.getString("fcur");
				BigDecimal bdFExRate = rs.getBigDecimal("fexrate");
				BigDecimal bdFUCost = rs.getBigDecimal("fucost");
				String strSCode = rs.getString("scode");
				BigDecimal bdSalesPrice = rs.getBigDecimal("sp");
				// BigDecimal bdItemTotal = rs.getBigDecimal("itemtotal");
				// Get the locId
				Integer thisLocId = iDefLocId;
				if (!strWhId.equals(""))
				{
					Location thisLoc = LocationNut.getObjectByCode(strWhId);
					if (thisLoc != null)
						thisLocId = thisLoc.getPkid();
				}
				// Integer thisStkId = null;
				// Get the corresponding itemId and stockId
				Item thisItem = ItemNut.getObjectByCode(strStkId);
				if (thisItem == null)
				{
					Log.printDebug(count + ": Item Code " + strStkId + " doesn't exist!! Creating a temp one ...");
					/** *** BEGIN: CREATE THE ITEM BASED ON STKTX **** */
					/*
					 * thisItem = ItemNut.getHome().create(
					 * strStkId,strStkId,strStkId, new Integer(1000), new
					 * Integer(0), new Integer(ItemBean.INV_TYPE_INVENTORY),
					 * TimeFormat.getTimestamp(),usrid);
					 */ItemObject itemObj = new ItemObject();
					// populate the properties here!!
					itemObj.code = strStkId;
					itemObj.name = strStkId;
					itemObj.description = strStkId;
					itemObj.userIdUpdate = usrid;
					itemObj.enumInvType = new Integer(ItemBean.INV_TYPE_INVENTORY);
					/*
					 * itemObj.priceList = sellingPrice; itemObj.priceSale =
					 * sellingPrice; itemObj.priceDisc1 = sellingPrice;
					 * itemObj.priceDisc2 = sellingPrice; itemObj.priceDisc3 =
					 * sellingPrice; itemObj.priceMin = costPrice;
					 * itemObj.fifoUnitCost = costPrice; itemObj.maUnitCost =
					 * costPrice; itemObj.waUnitCost = costPrice;
					 * itemObj.lastUnitCost = costPrice;
					 * itemObj.replacementUnitCost = costPrice;
					 */
					// itemObj.preferredSupplier = suppAccObj.pkid;
					thisItem = ItemNut.fnCreate(itemObj);
					POSItem auxPOSItem = POSItemNut.getPOSItem(thisItem.getPkid(), POSItemBean.TYPE_INV);
					if (auxPOSItem == null)
					{
						// BigDecimal bdQuotedPrice = (bdSalesPrice.signum() ==
						// 0)?bdUCost:bdSalesPrice;
						POSItemObject posObj = new POSItemObject();
						posObj.itemFKId = thisItem.getPkid();
						posObj.itemType = POSItemBean.TYPE_INV;
						posObj.currency = "MYR";
						posObj.unitPriceStd = bdSalesPrice;
						posObj.unitPriceDiscounted = bdSalesPrice;
						posObj.unitPriceMin = bdSalesPrice;
						posObj.userIdUpdate = usrid;
						auxPOSItem = POSItemNut.fnCreate(posObj);
						/*
						 * auxPOSItem = POSItemNut.getHome().create(
						 * thisItem.getPkid(),POSItemBean.TYPE_INV,
						 * "MYR",bdSalesPrice,new BigDecimal(0),new
						 * BigDecimal(0),
						 * TimeFormat.getTimestamp(),TimeFormat.getTimestamp(),usrid);
						 */}
					// Then populate the corresponding stock table
					Stock auxStk = StockNut.getObjectBy(thisItem.getPkid(), thisLocId, iDefCondId, "");
					if (auxStk == null)
					{
						StockObject newObj = new StockObject();
						newObj.itemId = thisItem.getPkid();
						newObj.locationId = thisLocId;
						newObj.accPCCenterId = iDefPCCenterId;
						newObj.userIdUpdate = usrid;
						auxStk = StockNut.fnCreate(newObj);
					}
					// thisStkId = auxStk.getPkid();
					/** *** END: CREATE THE ITEM BASED ON CDHISD **** */
				} // end if (thisitem == null)
				// Get the StockId
				Stock thisStk = StockNut.getObjectBy(thisItem.getPkid(), thisLocId, iDefCondId, "");
				if (thisStk == null)
				{
					StockObject newObj = new StockObject();
					newObj.itemId = thisItem.getPkid();
					newObj.locationId = thisLocId;
					newObj.accPCCenterId = iDefPCCenterId;
					newObj.userIdUpdate = usrid;
					thisStk = StockNut.fnCreate(newObj);
				}
				BigDecimal thisUnitPrice = null;
				BigDecimal thisUnitPrice2 = null;
				BigDecimal thisQty = null;
				if (bdQtyIn.signum() == 0 && strTxType.equals("S"))
				{
					thisQty = bdQtyOut.negate();
					thisUnitPrice = bdSalesPrice;
					thisUnitPrice2 = bdSalesPrice.divide(bdFExRate, 2);
				} else if (strTxType.equals("R")) // purchase
				{
					thisQty = bdQtyIn;
					thisUnitPrice = bdUCost;
					thisUnitPrice2 = bdFUCost;
				} else
				{
					// Assume everything else to be some form of adjustment
					// in which case, it depends on whether qtyIn or qtyOut,
					// if qtyIn>0, reversing sales, take SP
					// else take ucost
					if (bdQtyIn.signum() != 0)
					{
						thisQty = bdQtyIn;
						thisUnitPrice = bdSalesPrice;
						thisUnitPrice2 = bdSalesPrice.divide(bdFExRate, 2);
					} else
					{
						thisQty = bdQtyOut.negate();
						thisUnitPrice = bdUCost;
						thisUnitPrice2 = bdFUCost;
					}
				}
				// Create a new StockDeltaObject and start populating it.
				StockDeltaObject thisStkDeltaObj = new StockDeltaObject();
				thisStkDeltaObj.namespace = "";
				thisStkDeltaObj.txnType = strTxType;
				// thisStkDeltaObj.txnCode = "";
				// thisStkDeltaObj.serial = "";
				// thisStkDeltaObj.personInCharge = new Integer("0");
				thisStkDeltaObj.processNode = StockDeltaBean.PNODE_DEFAULT;
				thisStkDeltaObj.stockId = thisStk.getPkid();
				// refStockId = new Integer("0");
				thisStkDeltaObj.itemId = thisItem.getPkid();
				thisStkDeltaObj.quantity = thisQty;
				thisStkDeltaObj.currency = strDefCurr;
				thisStkDeltaObj.unitPrice = thisUnitPrice;
				thisStkDeltaObj.currency2 = (String) hmCurr.get(new Integer(strFCur));
				thisStkDeltaObj.unitPrice2 = thisUnitPrice2;
				thisStkDeltaObj.txnTime = TimeFormat.createTimeStamp(strTxDate, "MM/dd/yy HH:mm:ss");
				// thisStkDeltaObj.schTime = TimeFormat.getTimestamp();
				thisStkDeltaObj.remarks = strComments + "(docno=" + strDocNo + ",scode=" + strSCode + ")";
				thisStkDeltaObj.intReserved1 = new Integer(0);
				thisStkDeltaObj.strReserved1 = strDocNo; // / Old DocNo
				thisStkDeltaObj.strReserved2 = strSCode; // / Old
															// customer/supplier
															// code
				thisStkDeltaObj.strReserved3 = strStatus; // / Old Status
				// thisStkDeltaObj.strReserved4 = ""; /// container
				// thisStkDeltaObj.entityTable = "";
				// thisStkDeltaObj.entityId = new Integer(0);
				// thisStkDeltaObj.docTable = "";
				// thisStkDeltaObj.docKey = new Long("0");
				// thisStkDeltaObj.state = StockDeltaBean.STATE_CREATED;
				// thisStkDeltaObj.status = StockDeltaBean.STATUS_ACTIVE;
				thisStkDeltaObj.userIdEdit = usrid;
				// thisStkDeltaObj.timeEdit = TimeFormat.getTimestamp();
				// finally, commit this to inv_stock_delta
				StockNut.execTxn(thisStkDeltaObj);
			} // end while (rs.next())
		} catch (Exception ex)
		{
			ex.printStackTrace();
			req.setAttribute("strErrMsg", "Error occurred while migrating Stk Txn: " + ex.getMessage());
		}
		Log.printVerbose("***** END: MIGRATION OF STOCK TXN *****");
		// Get processing End time
		Timestamp tsEnd = TimeFormat.getTimestamp();
		req.setAttribute("task", "MIGRATE STOCK TXN");
		req.setAttribute("tsStart", tsStart);
		req.setAttribute("tsEnd", tsEnd);
		// Clean up curTask;
		curTask = null;
		return new ActionRouter("test-migrate-topcondb-page");
	}
}
